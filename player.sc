(


// ==========================================
// PLAYER FACTORY
// ==========================================



~make_player_from_synthdef = { arg main, defname, data=nil;
	// changed messages: \redraw_node, \mode
	var player;
	var desc = SynthDescLib.global.synthDescs[defname];
	if(desc.isNil, {
		("ERROR: make_player_from_synthdef: SynthDef not found: "++defname).error
	});
	defname.debug("loading player from");
	//desc.debug("synthDescs");
	player = (
		bank: 0,
		defname: defname,
		//uname: defname ++ UniqueID.next,
		uname: nil,
		node: EventPatternProxy.new,
		to_destruct: List.new,
		//name: defname,
		name: nil,
		kind: \player,	// should be classtype, but backward compat...
		current_mode: \stepline, // should be current_kind
		uid: UniqueID.next,
		playlist: List.new,
		sourcepat: nil,
		selected_param: \stepline,
		wrapper: nil,
		sourcewrapper: nil,
		playing_state: \stop,
		muted: false,
		archive_param_data: [\control, \stepline, \adsr, \noteline, \buf],
		archive_data: [\current_mode, \effects],
		effects: List.new,
		is_effect: false,
		ccbus_set: IdentitySet.new,

		init: { arg self;
			var colpreset;
			
			self.sourcewrapper = "Pbind(\n\t\\freq, Pkey(\\freq)\n) <> ~pat;\nfalse";

			self.data = {
					// use args and defaults values from synthdef to build data dict
					// if data dict given, deep copy it instead
					var dict;
					dict = Dictionary.new;
					if( data.isNil, {
						desc.controls.do({ arg control;
							var name = control.name.asSymbol;
							//control.name.debug("making player data name");
							//control.defaultValue.debug("making player data");
							//control.defaultValue.isArray.debug("making player data");
							case
								{ (name == \adsr) || name.asString.containsStringAt(0, "adsr_") } {
									dict[name] = ~make_adsr_param.(
										main,
										self,
										name,
										control.defaultValue
									)
								}
								{ name == '?' } { 
									// skip
									// FIXME: what is it for ?
								}
								{ name == 'in' } {
									self.is_effect = true;

									//dict[name] = ~make_control_param.(
									//	main,
									//	self,
									//	name,
									//	\scalar,
									//	control.defaultValue,
									//	~get_spec.(name, defname)
									//)
								}
								{ (name == \bufnum) || name.asString.containsStringAt(0, "bufnum_") } {
									dict[name] = ~make_buf_param.(name, "sounds/default.wav", self, ~get_special_spec.(name, defname));
									if(dict[\samplekit].isNil) { // prevent multiple creation
										dict[\samplekit] = ~make_samplekit_param.(\samplekit);
										dict[\sampleline] = ~make_sampleline_param.(\sampleline);
									};
									self.to_destruct.add(dict[name]); //FIXME: make it real
								}
								//default
								{ true } { 
									dict[name] = ~make_control_param.(
										main,
										self,
										name,
										\scalar,
										control.defaultValue,
										~get_spec.(name, defname)
									)
								};
						});
					}, {
						dict = data.deepCopy;
					});
					dict;
			}.value;

			if(self.is_effect.not) {
				self.data[\noteline] = self.data[\noteline] ?? ~make_noteline_param.(\noteline);

				self.data[\dur] = self.data[\dur] ?? ~make_control_param.(main, self, \dur, \scalar, 0.5, ~get_spec.(\dur, defname));
				self.data[\segdur] = self.data[\segdur] ?? ~make_control_param.(main, self, \segdur, \scalar, 0.5, ~get_spec.(\dur, defname));
				self.data[\stretchdur] = self.data[\stretchdur] ?? ~make_control_param.(main, self, \stretchdur, \scalar, 1, ~get_spec.(\dur, defname));
				self.data[\legato] = self.data[\legato] ?? ~make_control_param.(main, self, \legato, \scalar, 0.5, ~get_spec.(\legato, defname));
				self.data[\sustain] = self.data[\sustain] ?? ~make_control_param.(main, self, \sustain, \scalar, 0.5, ~get_spec.(\sustain, defname));
				self.data[\repeat] = self.data[\repeat] ?? ~make_control_param.(main, self, \repeat, \scalar, 1, ~get_spec.(\repeat, defname));

				self.data[\stepline] = self.data[\stepline] ?? ~make_stepline_param.(\stepline, 1 ! 8 );
				self.data[\type] = ~make_type_param.(\type);
			};
			self.data[\instrument] = self.data[\instrument] ?? ~make_literal_param.(\instrument, defname);

			if(self.is_audiotrack) {
				self.data[\amp].change_kind(\bus)
			};

			//TODO: handle t_trig arguments

			// load default preset
			colpreset = try { main.model.colpresetlib[defname][0] };
			if(colpreset.notNil) {
				self.load_column_preset(colpreset, \scalar);
			};

			self.sourcepat = {
				var dict = Dictionary.new;
				var list = List[];
				var prio, reject;
				prio = [\repeat, \instrument, \stretchdur, \sampleline, \noteline, \stepline, \type, \samplekit, \bufnum, \dur, \segdur, \legato, \sustain];
				reject = [];
				if(self.is_effect) {
					reject = [\instrument];
				};

				list.add(\elapsed); list.add(Ptime.new);
				list.add(\current_mode); list.add(Pfunc({ self.get_mode }));
				list.add(\muted); list.add(Pfunc({ self.muted }));
				prio.difference(reject).do { arg key;
					if(self.data[key].notNil) {
						list.add(key); list.add( self.data[key].vpattern );
					}
				};
				self.data.keys.difference(prio).difference(reject).do { arg key;
					if(self.data[key].notNil) {
						list.add(key); list.add( self.data[key].vpattern );
					}
				};
				//list.debug("maked pbind list");
				//[\type, \stepline, \instrument].do { arg x; list.add(x); list.add(dict[x]) };
				//list.debug("maked pbind list");
				//Pbind(*list).dump;
				if(self.is_effect) {
					Pmono(self.data[\instrument].vpattern, *list)
				} {
					Pbind(*list);
				}
			}.value;
			self.build_real_sourcepat;
		},

		get_main: { arg self;
			main
		},

		set_effects: { arg self, fxlist;
			self.effects = fxlist.reject({arg fx; main.node_exists(fx).not }).asList;
			self.build_real_sourcepat;
		},

		add_effect: { arg self, fx;
			self.effects.add(fx);
			self.build_real_sourcepat;
		},


		get_effects: { arg self;
			self.effects;
		},

		set_playing_state: { arg self, state;
			self.playing_state = state;
			self.changed(\redraw_node);
		},

		get_playing_state: { arg self;
			switch(self.muted,
				true, {
					switch(self.playing_state,
						\play, { \mute },
						\stop, { \mutestop }
					)
				},
				false, {
					switch(self.playing_state,
						\play, { \play },
						\stop, { \stop }
					)
				}
			)
		},


		get_piano: { arg self;
			var veloc_ratio = main.model.velocity_ratio;
			var exclu, list = List[];
			exclu = [\instrument, \noteline, \amp, \bufnum, \freq, \sampleline, \samplekit, \repeat, \stretchdur, \stepline, \type, \dur, \segdur, \legato, \sustain];
			self.data.keys.difference(exclu).do { arg key;
				var val = self.data[key].vpiano ?? self.data[key].vpattern;
				list.add(key); list.add( val ) 
			};
			if(self.get_mode == \sampleline) {
				if(self.data[\freq].notNil) {
					list.add(\freq); list.add( self.data[\freq].vpiano ?? self.data[\freq].vpattern );
				};
				{ arg slotnum, veloc=1; 
					veloc = veloc ?? 1;
					[slotnum, self.data[\samplekit].get_val].debug("slotnum, samplekit get val");
					~samplekit_manager.slot_to_bufnum(slotnum, self.data[\samplekit].get_val).debug("bufnum");
					Synth(self.data[\instrument].vpiano, (
						[\bufnum, ~samplekit_manager.slot_to_bufnum(slotnum, self.data[\samplekit].get_val),
							\amp, self.data[\amp].vpiano.value + (veloc * veloc_ratio) ] ++
							list.collect(_.value)).debug("sampleline arg listHHHHHHHHHHHHHHHHHHHHHHHHHHH")) 
				}
			} {
				if(self.data[\bufnum].notNil) {
					list.add(\bufnum); list.add( self.data[\bufnum].vpiano ?? self.data[\bufnum].vpattern );
				};
				{ arg freq, veloc=1; 
					veloc = veloc ?? 1;
					if(self.data[\freq].notNil) {
						list.add(\freq); list.add( freq ?? self.data[\freq].vpiano ?? self.data[\freq].vpattern );
					};
					[self.data[\amp].vpiano.value, veloc].debug("CESTLA?");
					Synth(self.data[\instrument].vpiano, (
						[\amp, self.data[\amp].vpiano.value + (veloc * veloc_ratio) ] ++
							list.collect(_.value)).debug("arg listHHHHHHHHHHHHHHHHHHHHHHHHHHH")) 
				}

			};

		},

		destructor: { arg self;
			// FIXME: implement it correctly
			self.to_destruct.do { arg i;
				i.destructor;
			}
		},

		edit_wrapper: { arg self;
			var tmp, file;
			tmp = "tempfile -s .sc -p seco".unixCmdGetStdOut;
			tmp = tmp.split($\n)[0];
			tmp.debug("tmp");
			if(self.sourcewrapper.notNil) {
				file = File.new(tmp, "w");
				file.write(self.sourcewrapper);
				file.close;
			};
			("xterm -r -fn 10x20 -e \"vim -c 'set ft=supercollider' "++tmp++"\"").unixCmd({
				var file, code, res;
				file = File.new(tmp, "r");
				code = file.readAllString;
				file.close;
				code.debug("code");
				self.set_wrapper_code(code);
				File.delete(tmp);
			});
		},


		set_wrapper: { arg self, pat, code=nil;
			code.debug("set_wrapper");
			self.wrapper = pat;
			if(code.notNil) { self.sourcewrapper = code };
			self.build_real_sourcepat;
		},

		set_wrapper_code: { arg self, code;
			var res, env;
			env = (pat:self.sourcepat);
			res = env.use({code.interpret});
			res.postcs;
			if(res.notNil) {
				if(res == false) {
					self.set_wrapper(nil, code);
				} {
					self.set_wrapper(res, code);
				}
			} {
				"Interpretation of wrapper file FAILED!".error;
			};
		},

		set_mode: { arg self, val;
			if(self.current_mode != val) {
				if(val == \sampleline && (self.get_arg(\sampleline).isNil)) {
					"player: Can't set sampleline mode: not a sample player".inform;	
				} {
					if(self.get_selected_param == self.current_mode) {
						self.select_param(val);
					};
					self.current_mode = val;
					self.changed(\mode);
				};
			};
		},

		get_mode: { arg self, val;
			self.current_mode;
		},

		is_audiotrack: { arg self;
			self.defname.asString.beginsWith("audiotrack")
		},

		clone: { arg self;
			var pl;
			pl = ~make_player_from_synthdef.(main,defname);
			pl.load_data( self.save_data.deepCopy );
			pl;
		},
		map_arg: { arg self, argName, val;
			argName.debug("mapping hidden!!!");
			~get_spec.(argName, defname).map(val);
		},
		unmap_arg: { arg self, argName, val;
			~get_spec.(argName, defname).unmap(val);
		},

		get_args: { arg self;
			self.data.keys
		},

		get_ordered_args: { arg self;
			~sort_by_template.(self.data.keys, desc.controls.collect { arg x; x.name });
		},

		get_all_args: { arg self;
			var res;
			res = OrderedIdentitySet.new;
			res.addAll(self.get_args);
			self.effects.do { arg fx, i;
				res.addAll(main.get_node(fx).get_args.collect { arg ar;
					(ar++"_fx"++i).asSymbol
				})
			};
			res;
		},

		get_arg: { arg self, key;
			var splited, argname, fxnum;
			splited = key.asString.split($_);
			if(splited.last[0..1] == "fx") {
				fxnum = splited.pop[2].asString.asInteger;
				argname = splited.join("_").asSymbol;
				main.get_node(self.effects[fxnum]).get_arg(argname);
			} {
				self.get_raw_arg(key)
			}
		},

		set_bank: { arg self, bank;
			self.bank = bank;
			self.data.do { arg x; x.changed(\cells); };
		},

		get_bank: { arg self;
			self.bank;
		},

		save_data: { arg self;
			var argdat;
			var data = ();
			data.args = ();
			self.get_args.do { arg key;
				argdat = self.get_arg(key);	
				if(self.archive_param_data.includes(argdat.classtype), {
					data.args[key] = argdat.save_data
				})
			};
			data.name = defname;
			data.defname = defname;
			data.bank = self.bank;
			data.current_mode = self.get_mode;
			data.sourcewrapper = self.sourcewrapper;
			self.archive_data.do { arg key;
				if(self[key].notNil) {
					data[key] = self[key]
				}
			};
			data;
		},

		load_data: { arg self, data;
			var argdat;
			self.get_args.do { arg key;
				argdat = self.get_arg(key);	
				if(self.archive_param_data.includes(argdat.classtype), {
					if( data.args[key].notNil ) {
						argdat.load_data( data.args[key] )
					}
				})
			};
			self.bank = data.bank;
			self.set_wrapper_code(data.sourcewrapper);
			self.set_mode(data.current_mode ?? \stepline);
			self.archive_data.do { arg key;
				if(data[key].notNil) {
					self[key] = data[key]
				}
			};
			self.build_real_sourcepat;
		},

		save_column_preset: { arg self;
			var data = ();
			data.defname = self.defname;
			self.data.keysValuesDo { arg key, val;
				if([\control].includes(val.classtype) ) {
					data[key] = val.get_val;
				};
			};
			data;
		},

		load_column_preset: { arg self, data, kind=\seq;
			self.data.keysValuesDo { arg key, val;
				if( data[key].notNil ) {
					if( val.current_kind == kind ) {
						[key, data[key]].debug("load_column_preset");
						val.set_val(data[key]);
					}
				};
			};
		},

		as_event: { arg self;
			var ev = ();
			self.data.keysValuesDo { arg key, val;
				ev[key] = val.get_val;
			};
			ev;
		},

		add_ccbus: { arg self, param;
			// a param is on recordbus mode, so include a pattern to set the bus while playing
			var vpat;
			param.name.debug("player.add_ccbus");
			self.ccbus_set.add(param);
			self.build_real_sourcepat;
		},

		remove_ccbus: { arg self, param;
			self.ccbus_set.remove(param);
			self.build_real_sourcepat;
		},

		set_input_pattern: { arg self, pat;
			if(self.input_pattern.isNil) {
				self.input_pattern = EventPatternProxy.new;
				self.input_pattern.source = pat;
				self.build_real_sourcepat;
			} {
				self.input_pattern.source = pat;
			}
		},

		build_real_sourcepat: { arg self;
			var res, list;
			"entering build_real_sourcepat".debug;

			res = if(self.wrapper.notNil) {
				self.wrapper;
			} {
				self.sourcepat;
			};

			if(self.input_pattern.notNil) {
				//"entering build_real_sourcepat: making input pattern".debug;
				self.input_pattern.source.postcs;
				res.postcs;
				res = Pfunc({ arg ev; 
					//ev.debug("EV"); 
					if(ev.as(Dictionary).includesKey(\degree)) {
						ev.removeAt(\freq);
					} {
						if(ev.as(Dictionary).includesKey(\midinote)) {
							ev.removeAt(\freq);
						};
					};
					ev;
					//ev.debug("EV2"); 
				}) <> res <> self.input_pattern;
				//res = self.input_pattern; // DEBUG
			};

			res = if(self.effects.size > 0) {
				~pfx.(
					res,
					self.effects.collect { arg fx;
						//fx.debug("effect");
						main.get_node(fx).vpattern.postcs;
					}
				)
			} {
				res;
			};
			// add bus setting
			if(self.ccbus_set.size > 0) {
				list = self.ccbus_set.as(Array).collect({ arg x; x.recordbus.vpattern }).reject(_.isNil) ++ [res];
				//list.debug("******** build_real_sourcepat: ppar list");
				res = Ppar( list )
			};

			self.real_sourcepat = res.trace;
		},

		vpattern: { arg self;
			//self.wrapper.debug("vpattern called: wrapper");
			self.real_sourcepat;
		},

		vpattern_loop: { arg self;
			Pn(self.vpattern, ~general_sizes.safe_inf);
		},

		prepared_node: { arg self;
			self.node.source = self.vpattern;
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++setting source");
			self.node;
		},

		play_node: { arg self;
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++setting source(inf)");
			main.play_manager.play_node(self.uname);
			//self.node.play;
		},

		stop_node: { arg self;
			main.play_manager.stop_node(self.uname);
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++stop");
			//self.node.source = nil;
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++niling source");
		},

		mute: { arg self, val=true;
			if(val != self.muted) {
				self.muted = val;
				if(self.is_audiotrack) {
					self.data[\amp].bus.mute(val);
				};
				self.changed(\redraw_node);
			}
		},

		play_repeat_node: { arg self; 
			var rep;
			rep = self.data[\repeat].get_val;
			self.data[\repeat].set_val(0);
			self.node.play;
			fork {
				1.wait; //FIXME: use quant
				self.data[\repeat].set_val(rep);
			};
		},

		select_param: { arg self, name;
			var oldsel;
			if( self.data[name].notNil ) {
				oldsel = self.selected_param;
				name.debug("player selected_param");
				self.selected_param = name;
				self.data[oldsel].changed(\selected);
				self.data[name].changed(\selected);
			} {
				[self.uname, name].debug("can't select param: not found");
			}
		},

		get_selected_param: { arg self;
			self.selected_param;
		},

		get_raw_arg: ~player_get_arg,
		set_arg: ~player_set_arg

	);
	player.init;
	player;
};

~make_player_from_pbind = { arg main, pat;
	var defname, player, param;
	if(pat.class == Pbind) {
		pat.patternpairs.do { arg elm, x;
			if(elm == \instrument) {
				defname = pat.patternpairs[x + 1];
				player = ~make_player_from_synthdef.(main, defname);
			}
		};
		if(defname.isNil) {
			"ERROR: ~make_player_from_pbind: instrument key not found in pbind".debug;
			nil
		} {
			pat.patternpairs.clump(2).do { arg elm, x;
				param = player.get_arg(elm[0]);
				if(param.notNil) {
					param.set_pkey_mode(true);
				} {
					elm[0].debug("ERROR: ~make_player_from_pbind: param not found");
				}
			};
			player.set_input_pattern(pat);
			player;
		};
	} {
		"ERROR: ~make_player_from_pbind: pat not a pbind".debug;
		nil
	}
};

~make_player_from_pchain = { arg main, pat, instr;
	var defname, player, param;
	defname = instr;
	if(defname.isNil) {
		"ERROR: ~make_player_from_pchain: can't found instrument, guessing NOT IMPLEMENTED".debug;
		nil
	} {
		player = ~make_player_from_synthdef.(main, defname);
		param = player.get_args.do { arg elm, x;
			//if([\stretchdur, \segdur, 
			param = player.get_arg(elm);
			if(param.notNil) {
				param.set_pkey_mode(true);
			} {
				elm.debug("ERROR: ~make_player_from_pchain: param not found");
			}
		};
		player.set_input_pattern(pat);
		player;
	};
};

~make_player_from_colpreset = { arg main, data;
	var pl;
	pl = ~make_player_from_synthdef.(main, data.defname);
	pl.load_column_preset(data, \scalar);
	pl;
};

~make_player_from_patfun = { arg patfun, data=nil;
	var player;
	player = (
		init: { arg self;

			self.data = {
					// use args and defaults values from synthdef to build data dict
					// if data dict given, deep copy it instead
					var dict;
					dict = Dictionary.new;
					if( data.isNil, {
						patfun.argNames.do({ arg argName, idx;
							dict[argName] = patfun.defaultArgs[idx];
						});
					}, {
						dict = data.deepCopy;
					});
					dict;
			}.value;

			self.node.source = patfun.valueArray( patfun.argNames.collect({ arg argName;
				~make_event_key_reader.(argName, self)
			}));
		},
		patfun: { arg self; patfun; },
		clone: { arg self;
			~make_player_from_patfun.(patfun, self.data);
		},
		map_arg: { arg self, argName, val;
			// TODO: how to get synthdef spec
			~get_spec.(argName).map(val);
		},
		unmap_arg: { arg self, argName, val;
			~get_spec.(argName).unmap(val);
		},
		node: EventPatternProxy.new,
		get_arg: ~player_get_arg,
		set_arg: ~player_set_arg
	);
	player.init;
	player;
};


~make_player = { arg main, instr, data=nil;
	var player = nil;
	case
		{ instr.isSymbolWS || instr.isString } {
			player = ~make_player_from_synthdef.(main,instr.asSymbol, data);
		} 
		{ instr.isFunction } {
			player = ~make_player_from_patfun.(instr, data);
		}
		{ ("ERROR: player type not recognized:"++instr).postln }
	;
	player;
};

~make_empty_player = (
	name: \voidplayer,
	uname: \voidplayer,
	get_playing_state: \stop,
	kind: \player
);

// ==========================================
// PARPLAYER FACTORY
// ==========================================

~empty_pattern = Pn((freq:\rest, dur:0.0001),1);

~empty_player = ~make_empty_player; // compat

~make_empty_groupnode = {(
	
	children: SparseArray.newClear(~general_sizes.children_per_groupnode, \voidplayer),
	kind: \parnode,
	name: \void,
	uname: \void,
	get_playing_state: \stop,

	refresh: { arg self;
		var children;
		children = SparseArray.newClear(~general_sizes.children_per_groupnode, ~make_empty_player);
		self.changed(\redraw, self, children);
	}

)};

~make_empty_parnode = {
	var res = ~make_empty_groupnode.();
	res.kind = \parnode;
	res;
};

~make_empty_seqnode = {
	var res = ~make_empty_groupnode.();
	res.kind = \seqnode;
	res;
};

~make_groupplayer = { arg main, children=List[];
	// changed messages: \redraw, \redraw_node, \selected_child
	var pplayer;
	pplayer = (
		//children: SparseArray.newClear(8, ~empty_player),
		children: SparseArray.newClear(~general_sizes.children_per_groupnode, \voidplayer),
		kind: \parnode,
		name: \new,
		uname: \new,
		data: Dictionary.new,
		archive_data: [\children, \kind, \name, \selected_child, \selected_child_index],
		playlist: List.new,
		playing_state: \stop,
		muted: false,
		selected_child: \none,
		selected_child_index: 0,

		init: { arg self;
			self.data[\repeat] = self.data[\repeat] ?? ~make_control_param.(main, self, \repeat, \scalar, 1, ~get_spec.(\repeat));
			self.data[\amp] = self.data[\amp] ?? ~make_control_param.(main, self, \amp, \scalar, 1, ~get_spec.(\amp)); // dummy param FTM
			self.get_arg(\repeat).get_val.debug("init repeat.get_val");
		},

		set_input_pattern: { arg self, pat;
			var res;
			res = SparseArray.newClear(~general_sizes.children_per_groupnode, \voidplayer);
			self.children = pat.list.collect { arg inpat;
				if(inpat.key.notNil) {
					res.add(inpat.key);
				}
			};
			self.children = res;
			self.children.debug("groupplayer: set_input_pattern: children");
			self.changed(\redraw);
		},

		set_input_pattern2: { arg self, pat;
			var res;
			res = pat.list.collect { arg inpat;
				if(inpat.key.notNil) {
					if(self.children.includes(inpat.key).not) {
						self.add_children(inpat)
					}
				}
			};
		},

		set_playing_state: { arg self, state;
			self.playing_state = state;
			self.changed(\redraw_node);
		},

		get_playing_state: { arg self;
			switch(self.muted,
				true, {
					switch(self.playing_state,
						\play, { \mute },
						\stop, { \mutestop }
					)
				},
				false, {
					switch(self.playing_state,
						\play, { \play },
						\stop, { \stop }
					)
				}
			)
		},

		mute: { arg self, val=true;
			if(val != self.muted) {
				self.muted = val;
				self.changed(\redraw_node);
			}
		},

		set_children_name: { arg self, index, name;
			self.children[index] = name;
			//self.changed(\children, index, main.get_node(name)); //TODO: use individual containers
			self.refresh;
		},

		select_child: { arg self, name;
			var oldidx;
			self.selected_child = name;
			oldidx = self.selected_child_index;
			self.selected_child_index = self.children.detectIndex { arg i; i == name };
			self.changed(\selected_child, oldidx);
			self.changed(\selected_child, self.selected_child_index);
		},

		select_child_at: { arg self, index;
			var oldidx;
			oldidx = self.selected_child_index;
			self.selected_child_index = index;
			self.selected_child = self.children[index];
			self.changed(\selected_child, oldidx);
			self.changed(\selected_child, index);
		},

		set_selected_child: { arg self, name;
			name.debug("groupnode: set_selected_child");
			self.children[self.selected_child_index] = name;
			self.refresh;
		},

		add_children: { arg self, name;
			name.debug("groupplayer.add_children");
			self.children = self.children.add(name);
			self.refresh;
		},

		refresh: { arg self;
			self.changed(\redraw, self, self.get_view_children);
			self.changed(\selected_child, self.selected_child_index);
		},

		get_children_nodes: { arg self;
			var list, pl;
			list = List.new;
			self.children.reject({arg x; x == \voidplayer}).do { arg child;
				pl = main.get_node(child);
				if(pl.notNil) { 
					if(pl.kind == \parnode || (pl.kind == \seqnode)) {
						list.add(pl.prepared_node) 
					} {
						list.add(pl.prepared_node) 
					};
				}
			};
			list;
		},

		get_children: { arg self;
			var res;
			res = self.children.reject({ arg na; [\void, \voidplayer].includes(na) }).collect { arg na; main.get_node(na) };
			res = res.reject(_.isNil); // in bankplayer, some children don't exists
			res.collect(_.uname).debug("groupplayer: get_children");
			res.asList;
		},

		get_children_and_void: { arg self;
			var res;
			res = self.children.reject({ arg na; [\void, \voidplayer].includes(na) }).collect { arg na; main.get_node(na) };
			res = res.reject(_.isNil); // in bankplayer, some children don't exists
			res.collect(_.uname).debug("groupplayer: get_children");
			res = res.add(main.get_node(\voidplayer));
			res.asList;
		},

		get_view_children: { arg self;
			var res;
			res = self.children.collect { arg na; 
				if( [\void, \voidplayer].includes(na) ) {
					~empty_player
				} {
					main.get_node(na)
				}
			};
			res.collect(_.uname).debug("groupplayer: get_view_children");
			res.asList;
		},

		get_children_nodes2: { arg self;
			var list, pl;
			list = List.new;
			self.children.do { arg child;
				pl = main.get_node(child);
				if(pl.notNil) { 
					list.add(pl.prepared_node) 
				}
			};
			list;
		},

		get_children_sources: { arg self;
			var list, pl;
			list = List.new;
			self.children.do { arg child;
				pl = main.get_node(child);
				if(pl.notNil) { 
					if(pl.kind == \parnode || (pl.kind == \seqnode)) {
						list.add(pl.vpattern3) 
					} {
						list.add(pl.vpattern) 
					};
				}
			};
			list;
		},

		ppattern: { arg self, list, repeat=1;
			repeat.debug("ppattern.repeat");
			Ppar(list, repeat);
		},

		prepared_node: { arg self;
			self.node.source = self.vpattern;
			self.node;
		},

		new_self: { arg self, main, children=List[];
			~make_parplayer.(main, children);
		},

		vpattern0: { arg self, noreplay=true;
			var list;
			self.uname.debug("==================== making list pattern");
			list = self.get_children_nodes;
			list.debug("vpattern list");
			if(noreplay) { 
				list = list.reject({arg x;
					if( x.class == EventPatternProxy ) {
						x.player.isPlaying
					} {
						false
					};
				})
			};
			list.debug("vpattern list2");
			self.get_arg(\repeat).get_val.debug("vpattern repeat.get_val");
			if( list.size > 0 ) {
				if(self.get_arg(\repeat).get_val == 0) {
					self.ppattern(list, inf);
				} {
					self.ppattern(list, self.get_arg(\repeat).get_val);
				}
			} {
				self.uname.debug("empty list pattern");
				~empty_pattern;
			}
		},

		vpattern2: { arg self, noreplay=true;
			self.uname.debug("vpattern2");
			Ppar(self.get_children_nodes2).postcs;
		},

		vpattern3: { arg self, noreplay=true;
			self.uname.debug("vpattern3");
			Ppar(self.get_children_sources);
		},

		vpattern: { arg self, noreplay=true;
			var repeat;
			repeat = self.get_arg(\repeat).get_val.debug("vpattern repeat.get_val");
			self.uname.debug("vpattern");
			
			Pn(~par_spawner.(main.play_manager, self.get_children), repeat);
		},

		vpattern_loop: { arg self;
			Pn(self.vpattern, ~general_sizes.safe_inf);
		},

		play_node: { arg self;
			//TODO: don't play subpattern if already playing
			self.uname.debug("playing groupnode");
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++setting source(inf)");
			main.play_manager.play_node(self.uname);
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++play");
		},

		stop_node: { arg self;
			main.play_manager.stop_node(self.uname);
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++stop");
			//FIXME: must niling source ?
			//self.get_children.do { arg n; n.stop_node };
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++stoping child nodes");
		},

		clone: { arg self;
			var pl;
			pl = self.new_self(main);
			pl.load_data( self.save_data.deepCopy );
			pl
		},

		get_args: { arg self;
			self.data.keys;
		},

		get_arg: { arg self, argu;
			self.data[argu];
		},
	
		save_data: { arg self;
			var data = ();
			var argdat;
			self.archive_data.do { arg key;
				data[key] = self[key];
			};
			data.args = ();
			self.get_args.do { arg key;
				argdat = self.get_arg(key);	
				if([\control, \stepline, \adsr, \noteline].includes(argdat.classtype), {
					data.args[key] = argdat.save_data
				})
			};
			data;
		},

		load_data: { arg self, data;
			var argdat;
			self.archive_data.do { arg key;
				self[key] = data[key];
			};
			self.get_args.do { arg key;
				argdat = self.get_arg(key);	
				if([\control, \noteline, \stepline, \adsr].includes(argdat.classtype), {
					argdat.load_data( data.args[key] )
				})
			};
		},

		node: EventPatternProxy.new
	);
	// no init
	pplayer;

};

~make_parplayer = { arg main, children=List[];
	var obj;
	obj = ~make_groupplayer.(main, children);
	obj.init();
	obj;
};

~make_seqplayer = { arg main, children=List[];
	var pplayer;
	var obj;
	obj = ~make_groupplayer.(main, children);
	pplayer = (
		kind: \seqnode,

		init: { arg self;
			self.data[\repeat] = self.data[\repeat] ?? ~make_control_param.(main, self, \repeat, \scalar, 1, ~get_spec.(\repeat));
			self.get_arg(\repeat).get_val.debug("init repeat.get_val");
		},

		get_piano: { arg self;
			var veloc_ratio = 0.1;
			var exclu, list = List[];
			var pianos = List.new;

			self.get_children.do { arg child;
				if(child.kind == \player and: { child.name != \voidplayer }) {
					pianos.add(child.get_piano)
				}
			};

			{ arg slotnum, veloc=1;
				var pia;
				veloc = veloc ?? 1;
				pia = pianos[slotnum];
				if(pia.notNil) {
					pia.(nil, veloc);
				}
			};

		},

		vpattern: { arg self;
			var repeat;
			repeat = self.get_arg(\repeat).get_val;
			repeat.debug("ppattern.repeat");
			~seq_spawner.(main.play_manager, self.get_children, repeat);
		},

		new_self: { arg self, main, children=List[];
			~make_seqplayer.(main, children);
		}
	
	);
	pplayer.keysValuesDo { arg key, val;
		obj[key] = val;
	};
	obj.init();
	obj;

};

~make_nodesampler = { arg main, children=List[];
	var pplayer;
	var obj;
	obj = ~make_parplayer.(main, children);
	pplayer = (
		kind: \parnode,
		subkind: \nodesampler,

		init: { arg self;
			self.data[\repeat] = self.data[\repeat] ?? ~make_control_param.(main, self, \repeat, \scalar, 1, ~get_spec.(\repeat));
			self.data[\nodeline] = self.data[\nodeline] ?? ~make_nodeline_param.(main, self, \nodeline);
			self.get_arg(\repeat).get_val.debug("init repeat.get_val");
		},

		vpattern: { arg self, noreplay=true;
			var repeat;
			repeat = self.get_arg(\repeat).get_val.debug("vpattern repeat.get_val");
			self.uname.debug("vpattern");
			
			Pn(~par_spawner.(main.play_manager, self.get_children), repeat);
		},

		new_self: { arg self, main, children=List[];
			~make_nodesampler.(main, children);
		}
	
	);
	pplayer.keysValuesDo { arg key, val;
		obj[key] = val;
	};
	obj.init();
	obj;
};

// ==========================================
// BANKPLAYER FACTORY
// ==========================================

~make_seqbank_player = { arg main, bank;

	var sbank;
	sbank = ~make_parplayer.(main);
	sbank.uname = ("seqbank" ++ bank).asSymbol;
	sbank.name = ("seqbank" ++ bank).asSymbol;
	sbank.children = 8.collect { arg i; ("seq"++(bank*8 + i)).asSymbol };

};

~make_parbank_player = { arg main, bank;

	var sbank;
	sbank = ~make_seqplayer.(main);
	sbank.uname = ("parbank" ++ bank).asSymbol;
	sbank.name = ("parbank" ++ bank).asSymbol;
	sbank.children = 8.collect { arg i; ("par"++(bank*8 + i)).asSymbol };

};

//a = ~make_control_param.(main, self, \repeat, \scalar, 1, ~get_spec.(\repeat))
//a.get_val

// ==========================================
// PLAYMANAGER
// ==========================================

~find_children = { arg main, node;
	var res = Set[];
	node.uname.debug("find_children: entering");
	if([\parnode, \seqnode].includes(node.kind) ) {
		node.get_children.do { arg child;
			child.uname.debug("processing child");
			if(child.uname.isNil) {
				"child is nil".debug;
			} {
				"aa".debug;
				if( res.includes(child.uname) ) {
					child.uname.debug("find_children: loop");
				} {
					"ba".debug;
					res.add(child);
					"ca".debug;
					if([\parnode, \seqnode].includes(child.kind) ) {
						"da".debug;
						res.addAll(~find_children.(main, child));
					};
				};
			};
		};
	};
	//[node.uname, res].debug("result for");
	res; // return real nodes (not names)
};

~make_playmanager = { arg main;
	
	var obj;
	obj = (
		top_nodes: Dictionary.new,
		children_nodes: Set.new,
		solomuted_nodes: Set.new,
		recording: false,
		tempo: TempoClock.default.tempo,
		visual_metronome_enabled: true,

		myclock: TempoClock.default,

		start_pos: 0,
		play_length: 16,
		record_length: 8,
		syncclap_dur: 4,
		use_metronome: false,
		keep_recording_session: false,
		get_clock: { arg self; self.myclock },

		refresh: { arg self;
			self.changed(\head_state, \stop);
			self.changed(\pos);
			self.changed(\quant);
			self.changed(\visual_metronome);
			self.changed(\tempo, self.get_clock.tempo);
		},

		is_near_end: { arg self;
			self.get_rel_beat > (self.get_record_length - 1 - self.myclock.beatsPerBar)
		},

		get_rel_beat: { arg self;
			[self.get_clock.beats, self.start_pos, self.get_record_length,
				((self.get_clock.beats - self.start_pos) % self.get_record_length)
			].debug("beats, spos, reclen, relbeat");
			((self.get_clock.beats - self.start_pos) % self.get_record_length)
		},

		is_recording: { arg self;
			self.recording == true
		},

		set_recording: { arg self, val;
			self.recording = val;
			if(self.is_recording) {
				self.changed(\head_state, \prepare);
			} {
				if(self.is_playing) {
					self.changed(\head_state, \play);
				} {
					self.changed(\stop_counter);
					self.changed(\head_state, \stop);
				}
			};
		},

		get_record_length: { arg self;
			self.record_length;
		},

		get_record_length_in_seconds: { arg self;
			self.get_record_length / self.myclock.tempo;
		},

		set_record_length: { arg self, val;
			val.debug("play_manager.set_record_length");
			self.record_length = val;
			self.changed(\pos);
		},

		set_bpm_tempo: { arg self, val;
			self.myclock.tempo = val/60;
			self.tempo = val/60;
			self.changed(\tempo);
		},

		get_bpm_tempo: { arg self;
			self.myclock.tempo * 60;
		},

		get_tempo: { arg self;
			self.myclock.tempo;
		},

		get_quant: { arg self;
			EventPatternProxy.defaultQuant;
		},

		set_quant: { arg self, val;
			EventPatternProxy.defaultQuant = val;
			self.changed(\quant);
		},

		start_new_session: { arg self;
			if(self.is_playing || self.keep_recording_session) {
				"start_new_session: already playing".debug;
				self.keep_recording_session = false;
				self.changed(\visual_metronome);
			} {
				"start_new_session: new session!!".debug;
				if(self.myclock != TempoClock.default) {
					//self.myclock.stop // TODO: make sure there is no ressource leak
					//FIXME: this stop cause bug, why ?
				};
				self.myclock = TempoClock.new(self.tempo);
				self.myclock.permanent = true;
				self.myclock.beats.debug("start_new_session: new clock beats");
				self.myclock.hash.debug("hash");
				self.start_pos = 0;
				self.changed(\visual_metronome);
				//self.start_visual_metronome;
			}
		},

		start_metronome: { arg self, clock, dur;
			// called in midi.sc: preclap
			var oldclock;
			//oldclock = self.myclock;
			//self.myclock = clock;
			self.start_pos = 0;
			self.set_record_length(dur);
			Task {
				//self.changed(\visual_metronome);
				//self.start_visual_metronome;
				self.start_audio_metronome(clock, dur);
				dur.wait;
				//self.changed(\visual_metronome)
				//self.stop_visual_metronome;
				//self.myclock = oldclock;
			}.play(clock, quant:1);
		},

		start_audio_metronome: { arg self, clock, dur;
			self.audio_metronome = Pbind(\instrument, \metronome,
				\freq, 440,
				\sustain, 0.1,
				\dur, Pn(1,dur)
			).play(clock, quant:1);
		},

		stop_audio_metronome: { arg self;
			self.audio_metronome.stop;
		},

		enable_visual_metrome: { arg self, val=true;
			self.visual_metronome_enabled = val;
			self.changed(\visual_metronome);
		},

		start_visual_metronome: { arg self;
			self.get_rel_beat.debug("pm: start_visual_metronome");
			self.visual_metronome_enabled = true;
			self.changed(\visual_metronome);
		},

		stop_visual_metronome: { arg self;
			self.get_rel_beat.debug("pm: stop_visual_metronome");
			self.visual_metronome_enabled = false;
			self.changed(\visual_metronome);
		},

		is_playing: { arg self;
			self.top_nodes.size > 0
		},

		node_is_playing: { arg self, node;
			self.top_nodes.keys.includes(node.uname) || self.children_nodes.includes(node.uname)
		},

		play_node: { arg self, nodename;
			var esp, sc, children, quant;
			nodename.debug("pm: play_node");
			[self.top_nodes, self.children_nodes].debug("pm: state");
			~notNildo.(main.get_node(nodename), { arg node;
				self.start_new_session;
				children = ~find_children.(main, node);
				if( self.top_nodes.keys.includes(nodename) ) {
					nodename.debug("pm: play_node: already playing, unmuting children");
					children.do { arg child;
						child.mute(false);
					};
				} {
					if( self.children_nodes.includes(nodename) ) {
						nodename.debug("pm: play_node: unmute");
						node.mute(false);
					} {
						nodename.debug("pm: play_node: play!");
						children.do { arg child;
							child.mute(false);
						};
						node.mute(false);
						node.node.source = node.vpattern_loop;
						quant = if(self.is_playing) { self.get_quant } { 1 };
						self.get_rel_beat.debug("pm: play node");
						node.node.play(self.get_clock,quant:quant);
						node.set_playing_state(\play);
						esp = node.node.player;
						//node.debug("owww!");
						children.collect(_.uname).debug("pm: play_node: children");
						sc = SimpleController(esp);
						self.top_nodes[nodename] = (
							esp: esp,
							sc: sc
						);
						sc.put(\stopped, {
							nodename.debug("pm: stop handler called");
							self.top_nodes.removeAt(nodename);
							node.mute(false);
							node.set_playing_state(\stop);
							children.do { arg child;
								self.children_nodes.remove(child.uname);
								child.set_playing_state(\stop);
							};
							children.collect(_.uname).debug("pm: stop handler: children removed");
							if(self.is_playing.debug("isplaying").not) {
								self.changed(\head_state, \stop);
							};
							sc.remove;
							[self.top_nodes, self.children_nodes].debug("pm: end state");
						});
					}
				}
			}); 
			if(self.is_playing.debug("is_playing---------------------------------")) { self.changed(\head_state, \play) };
			[self.top_nodes, self.children_nodes].debug("pm: end state");
		},

		stop_node: { arg self, nodename;
			var children;
			nodename.debug("pm: stop_node");
			[self.top_nodes, self.children_nodes].debug("pm: state");
			~notNildo.(main.get_node(nodename), { arg node;
				if( self.top_nodes.keys.includes(nodename) ) {
					nodename.debug("pm: stop_node: stoping!");
					self.top_nodes[nodename].esp.stop;
				} {
					if( self.children_nodes.includes(nodename) ) {
						nodename.debug("pm: stop_node: mute");
						node.mute(true);
					} {
						nodename.debug("pm: stop_node: not playing, individually stopping children");
						children = ~find_children.(main, node);
						children.do { arg child; 
							child.node.stop;
						};
					}
				}
			}); 

			[self.top_nodes, self.children_nodes].debug("pm: end state");
		},

		solo_node: { arg self, nodename;
			var children, smn = Set[];
			self.top_nodes.keys.union(self.children_nodes).do { arg nname;
				if(nodename != nname) {
					~notNildo.(main.get_node(nname), { arg node;
						if(node.muted.not) {
							smn.add(nname);
							node.mute(true);
						}
					});
				}
			};
			self.solomuted_nodes = self.solomuted_nodes.union(smn);
			main.get_node(nodename).mute(false);
		},

		unsolo_node: { arg self;
			self.solomuted_nodes.do { arg nname;
				main.get_node(nname).mute(false);
			};
			self.solomuted_nodes = Set.new;
		},

		is_in_solo_mode: { arg self;
			self.solomuted_nodes.size != 0
		},


		add_childnode: { arg self, nodename;
			nodename.debug("add_childnode");
			self.children_nodes.add(nodename);
		},

		remove_childnode: { arg self, nodename;
			nodename.debug("remove_childnode");
			self.children_nodes.remove(nodename);
		}
	);
	obj;


};

~par_spawner = { arg pm, par; //par: node list
	par.do{ arg no; no.uname.debug("par_spawner: node"); };
	Pspawner({ |spawner|
		var streams = List.new;
		par.do { |node, i|
			var stream;
			node.uname.debug("par_spawner: inside: node");
			stream = CleanupStream(node.vpattern.asStream, { // use vpattern to avoid Pn(vpattern)
				node.uname.debug("&&&&&&&&&&&&&&&&par_spawner: ending stream");
				streams.remove(stream);
				if(streams.isEmpty) {
					node.uname.debug("&&&&&&&&&&&&&&&&par_spawner: all stream ended");
					spawner.suspendAll;
					par.do{ arg no; 
						no.uname.debug("&&&&&&&&&&&&&par_spawner: stop");
						node.set_playing_state(\stop);
						pm.remove_childnode(no.uname);
					};
				} {
					node.uname.debug("&&&&&&&&&&&&&&&&par_spawner: respawning stream");
					spawner.par(Pn(node.vpattern, ~general_sizes.safe_inf))
				};
			});
			streams.add(stream);
			pm.add_childnode(node.uname);
			node.set_playing_state(\play);
			node.uname.debug("play");
			spawner.par(stream);
		};
	});
};

~seq_spawner = { arg pm, seq, repeat;
	seq.do{ arg no; no.uname.debug("seq_spawner: node"); };
	Pspawner({ |spawner|
		seq.do { |node, i|
			var stream;
			seq.do{ arg no; pm.add_childnode(no.uname) };
			repeat.do {
				node.set_playing_state(\play);
				spawner.seq(node.vpattern);
				node.set_playing_state(\stop);
			};
			seq.do{ arg no; pm.remove_childnode(no.uname) };
		};
	});
};

)
