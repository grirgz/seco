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
		archive_param_data: [\control, \stepline, \adsr, \noteline, \nodeline, \sampleline, \buf],
		archive_data: [\current_mode, \effects],
		effects: List.new,
		is_effect: false,
		ccbus_set: IdentitySet.new,
		env_mode: false,

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


		get_piano: { arg self, kind=\normal;
			var veloc_ratio = main.model.velocity_ratio;
			var exclu, list = List[];
			exclu = [\instrument, \noteline,  \sampleline, \samplekit, \repeat, \stretchdur, \stepline, \type, \dur, \segdur, \legato, \sustain,
					\amp, \bufnum, \freq,
			];
			self.data.keys.difference(exclu).do { arg key;
				var val = self.data[key].vpiano ?? self.data[key].vpattern;
				list.add(key); list.add( val ) 
			};
			if(kind == \nsample) {
					[\freq, \bufnum].do { arg paramname;
						if(self.data[paramname].notNil) {
							list.add(paramname); list.add( self.data[paramname].vpiano ?? self.data[paramname].vpattern );
						};
					};
					{ arg slotnum, veloc=1; 
						veloc = veloc ?? 1;
						Synth(self.data[\instrument].vpiano, (
							[\amp, main.calcveloc(self.data[\amp].vpiano.value, veloc) ] ++
								list.collect(_.value)).debug("nsample arg listHHHHHHHHHHHHHHHHHHHHHHHHHHH")
						) 
					}
			} {
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
								\amp, main.calcveloc(self.data[\amp].vpiano.value, veloc) ] ++
								list.collect(_.value)).debug("sampleline arg listHHHHHHHHHHHHHHHHHHHHHHHHHHH")) 
					}
				} {
					//FIXME: why freq could be nil ?
					//if(self.data[\freq].notNil) {
					//	list.add(\freq); list.add( freq ?? self.data[\freq].vpiano ?? self.data[\freq].vpattern );
					//};
					if(self.data[\bufnum].notNil) {
						list.add(\bufnum); list.add( self.data[\bufnum].vpiano ?? self.data[\bufnum].vpattern );
					};
					{ arg freq, veloc=1; 
						veloc = veloc ?? 1;
						[self.data[\amp].vpiano.value, veloc].debug("CESTLA?");
						if(freq.isNil) { "get_piano: why freq is nil ?".debug; };
						Synth(self.data[\instrument].vpiano, (
							[
								\amp, main.calcveloc(self.data[\amp].vpiano.value, veloc),
								\freq, freq,
							] 
							++ list.collect(_.value)).debug("arg listHHHHHHHHHHHHHHHHHHHHHHHHHHH")
						) 
					}

				};
			}

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
				if([\adsr].includes(val.classtype)) {
					data[key] = val.get_val;
				};
			};
			data;
		},

		load_column_preset: { arg self, data, kind=\scalar;
			self.data.keysValuesDo { arg key, val;
				if( data[key].notNil ) {
					[key, val.current_kind, kind].debug("load_column_preset");
					if( val.current_kind == kind ) {
						[key, data[key]].debug("load_column_preset vraiment");
						if([\adsr].includes(val.classtype)) {
							val.set_all_val( data[key] );
						} {
							val.set_val(data[key]);
						};
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

		set_env_mode: { arg self, val = true;
			self.env_mode = val;
			self.env_mode.debug("SET ENV MODE!!!");
			self.build_real_sourcepat; // FIXME: already called just before setting env_mode (at init)
		},

		build_real_sourcepat: { arg self;
			var res, list;
			var chain;
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
				chain = Pfunc({ arg ev; 
					//ev.debug("EV"); 
					var evd = ev.as(Dictionary);
					if(evd.includesKey(\degree)) {
						ev.removeAt(\freq);
					} {
						if(evd.includesKey(\midinote)) {
							ev.removeAt(\freq);
						};
					};
					if(evd.includesKey(\mylegato)) { // FIXME: find a way to switch between legato and sustain
						ev.removeAt(\sustain);
					};
					ev;
					//ev.debug("EV2"); 
				}) <> res;
				self.env_mode.debug("*************************** ENV MODE ???");
				res = if(self.env_mode) {
					"*************************** ENV MODE ENABLED!!!".debug;
					~penvcontrol.(self.input_pattern, chain);
				} {
					chain <> self.input_pattern;
				}
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

		stop_node: { arg self, use_quant;
			main.play_manager.stop_node(self.uname, use_quant);
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
			// not used anymore: use vpattern_loop instead
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
			if( self.get_arg(name).notNil ) {
				oldsel = self.selected_param;
				name.debug("player selected_param");
				self.selected_param = name;
				self.get_arg(oldsel).changed(\selected);
				self.get_arg(name).changed(\selected);
			} {
				[self.uname, name].debug("can't select param: not found");
			}
		},

		get_selected_param: { arg self;
			self.selected_param;
		},

		get_selected_param_object: { arg self;
			self.get_arg(self.selected_param);
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
		archive_classtype: [\control, \stepline, \adsr, \noteline, \sampleline, \nodeline, \buf],
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

		stop_node: { arg self, use_quant;
			main.play_manager.stop_node(self.uname, use_quant);
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

		get_ordered_args: { arg self;
			~sort_by_template.(self.data.keys, [\nodeline, \amp, \repeat]);
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
				if(self.archive_classtype.includes(argdat.classtype), {
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
				if(self.archive_classtype.includes(argdat.classtype), {
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
		kind: \player,
		subkind: \nodesampler,
		samplechildren: List.new,
		responders: List.new,

		init: { arg self;
			self.data[\repeat] = self.data[\repeat] ?? ~make_control_param.(main, self, \repeat, \scalar, 1, ~get_spec.(\repeat));
			self.data[\nodeline] = self.data[\nodeline] ?? ~make_nodeline_param.(\nodeline);
			self.data[\dur] = self.data[\dur] ?? ~make_control_param.(main, self, \dur, \scalar, 0.25, ~get_spec.(\dur));
			//self.data[\noteline] = self.data[\noteline] ?? ~make_noteline_param.(\noteline);
			self.get_arg(\repeat).get_val.debug("init repeat.get_val");
		},

		set_samplechildren: { arg self, list;
			var sc;
			self.samplechildren = list;
			self.children = List.new;
			self.responders.do { _.remove };
			self.responders = List.new;
			list.do { arg child, slotnum;
				switch(child[0],
					\nsample, {
						~notNildo.(main.get_node(child[1])) { arg node;
							self.children.add(child[1]);
							sc = SimpleController(self.data[\nodeline]);
							node.set_mode(\noteline);
							sc.put(\notes, { arg self, msg, val;
								var notescore = self.scoreset.get_notescore.filter_by_slot( slotnum );
								node.get_arg(\noteline).scoreset.set_notescore( notescore );
								//node.get_arg(\noteline).scoreset.add_to_history( notescore );
							});
							self.responders.add( sc );
						};
					}
				)
			};
		},

		get_mode: { arg self;
			\nodeline
		},

		get_piano: { arg self;
			{ arg slotnum, veloc;
				var child = self.samplechildren[slotnum];
				if(child.notNil) {
					switch(child[0],
						\nsample, {
							~notNildo.(main.get_node(child[1])) { arg node;
								node.get_piano(\nsample).value(slotnum, veloc);
							};
						}
					)
				}
			}
		},

		vpattern: { arg self, noreplay=true;
			var repeat;
			repeat = self.get_arg(\repeat).get_val.debug("vpattern repeat.get_val");
			self.uname.debug("vpattern");
			
			Pn(~par_spawner.(main.play_manager, self.children.collect { arg x; main.get_node(x) }), repeat);
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
