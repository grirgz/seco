



// ==========================================
// PLAYER FACTORY
// ==========================================

~class_abstract_node = (

	///////// ordered args

	get_ordered_args: { arg self;
		~sort_by_template.(self.data.keys, self.get_desc.controls.collect { arg x; x.name });
	},

	get_pattern_args: { arg self;
		self.pattern_args
	},

	get_macro_args: { arg self;
		self.macro_args
	},

	get_displayable_args: { arg self;
		self.displayable_args
	},

	update_ordered_args: { arg self;
		var args;
		var param_types = ~class_player_display.param_types;
		var mode;
		
		args = self.get_ordered_args;
		args = args.reject { arg x; param_types.param_reject.includes(x) };
		args = args.reject { arg x; x.asString.beginsWith("macro") };
		args = args.select { arg x; 
			var param;
			param = self.get_arg(x);
			param_types.param_accepted_displayed_kind.includes(param.classtype) 
		};
		args = ~sort_by_template.(args, param_types.param_status_group ++ param_types.param_order);

		mode = self.get_mode;

		if(self.kind == \player) {
			args = [mode] ++ args;
		};

		// FIXME: handle legato
		//args = args.reject { arg x; x == \legato }; // removed in param_types.param_reject

		self.pattern_args = args.select { arg x; ([mode] ++ param_types.param_status_group).includes(x) };
		self.displayable_args = args.reject { arg x; ([mode] ++ param_types.param_status_group).includes(x) };
		self.macro_args = args.select { arg x; 
			self.get_arg(x).midi.notNil and: {
				([mode] ++ param_types.param_status_group).includes(x).not
			}
		};
		self.macro_args = self.macro_args.keep(8); // FIXME: hardcoded
	},
);

~class_synthdef_player = (
	parent: ~class_abstract_node,
	bank: 0,
	uname: nil,
	node: EventPatternProxy.new,
	to_destruct: List.new,
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
	temp_muted: false,
	available_modes: [\stepline, \noteline, \scoreline, \sampleline],
	archive_param_data: [\control, \stepline, \adsr, \noteline, \scoreline, \nodeline, \sampleline, \buf, \wavetable,\custom_env, \samplekit],
	archive_data: [\sourcewrapper, \instrname, \defname, \name, \uname, \kind, \subkind, \bank],
	effects: List.new,
	is_effect: false,
	ccbus_set: IdentitySet.new,
	env_mode: false,

	new: { arg self, main, instr, data=nil;
		var desc;
		var defname = instr;
		desc = SynthDescLib.global.synthDescs[defname];

		self = self.deepCopy;

		self.defname = defname;
		self.instrname = instr;

		if(desc.isNil, {
			("ERROR: make_player_from_synthdef: SynthDef not found: "++defname).error
		});
		defname.debug("loading player from");

		self.get_main = { arg self; main };
		self.get_desc = { arg self; desc };

		self.init(data);
		self;
	},

	get_label: { arg self;
		if(self.name != self.uname) {
			"% (%)".format(self.name, self.uname)
		} {
			"%".format(self.name)
		}
	},

	init: { arg self, data;
		var colpreset;
		
		self.sourcewrapper = "Pbind(\n\t\\freq, Pkey(\\freq)\n) <> ~pat;\nfalse";

		self.modulation = ~class_modulation_manager.new(self);
		self.effects = ~class_effect_manager.new(self);

		self.data = {
				// use args and defaults values from synthdef to build data dict
				// if data dict given, deep copy it instead
				var dict;
				dict = Dictionary.new;
				if( data.isNil, {
					self.get_desc.controls.do({ arg control;
						var name = control.name.asSymbol;
						//control.name.debug("making player data name");
						//control.defaultValue.debug("making player data");
						//control.defaultValue.isArray.debug("making player data");
						case
							{ (name == \adsr) || name.asString.containsStringAt(0, "adsr_") } {
								dict[name] = ~make_adsr_param.(
									self.get_main,
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
								//	self.get_main,
								//	self,
								//	name,
								//	\scalar,
								//	control.defaultValue,
								//	~get_spec.(name, self.defname)
								//)
							}
							{ (name == \bufnum) || name.asString.containsStringAt(0, "bufnum_") } {
								dict[name] = ~make_buf_param.(name, "sounds/default.wav", self, ~get_special_spec.(name, self.defname));
								if(dict[\samplekit].isNil) { // prevent multiple creation
									dict[\samplekit] = ~make_samplekit_param.(\samplekit);
									dict[\sampleline] = ~make_sampleline_param.(\sampleline);
								};
								self.to_destruct.add(dict[name]); //FIXME: make it real
							}
							{ (name == \mbufnum) || name.asString.containsStringAt(0, "mbufnum_") } {
								// mono bufnum
								dict[name] = ~make_buf_param.(name, "sounds/default.wav", self, ~get_special_spec.(name, self.defname), \mono);
								if(dict[\samplekit].isNil) { // prevent multiple creation
									dict[\samplekit] = ~make_samplekit_param.(\samplekit);
									dict[\sampleline] = ~make_sampleline_param.(\sampleline);
								};
								self.to_destruct.add(dict[name]); //FIXME: make it real
							}
							//default
							{ true } { 
								dict[name] = ~make_control_param.(
									self.get_main,
									self,
									name,
									\scalar,
									control.defaultValue,
									~get_spec.(name, self.defname)
								)
							};
					});
				}, {
					dict = data.deepCopy;
				});
				dict;
		}.value;

		self.build_standard_args;

		if(self.is_audiotrack) {
			self.data[\amp].change_kind(\bus)
		};

		//TODO: handle t_trig arguments

		// load default preset
		colpreset = try { self.get_main.model.colpresetlib[self.defname][0] };
		if(colpreset.notNil) {
			self.load_column_preset(colpreset, \scalar);
		};

		self.build_sourcepat;
		self.build_real_sourcepat;
	},

	get_scoreset: { arg self;
		var mode = self.get_mode;
		if(mode == \stepline) {
			mode = \scoreline
		};
		self.get_arg(mode).get_scoreset;
	},

	destructor: { arg self;
		// FIXME: implement it correctly
		self.to_destruct.do { arg i;
			i.destructor;
		};
		self.data.keysValuesDo { arg name, datum;
			datum.destructor;
		};
		self.name = "FREED";
		self.to_destruct = [];
	},

	clone: { arg self;
		var pl;
		var options = (copy_subplayers: true);
		pl = ~make_player.(self.get_main,self.instrname);
		pl.load_data( self.save_data(options).deepCopy, options );
		pl;
	},

	////////////////// modulation

	////////////////// effects

	//set_effects: { arg self, fxlist;
	//	self.effects = fxlist.reject({arg fx; self.get_main.node_exists(fx).not }).asList;
	//	self.build_real_sourcepat;
	//},

	//add_effect: { arg self, fx;
	//	self.effects.add(fx);
	//	self.build_real_sourcepat;
	//},


	//get_effects: { arg self;
	//	self.effects;
	//},

	////////////////// wrapper

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
		//res.postcs;
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

	////////////////// mode

	set_mode: { arg self, val;
		if(self.current_mode != val) {
			if(val == \sampleline && (self.get_arg(\sampleline).isNil)) {
				"player: Can't set sampleline mode: not a sample player".inform;	
			} {
				if(self.get_selected_param == self.current_mode) {
					self.select_param(val);
				};
				self.current_mode = val;
				self.data.keysValuesDo { arg key, datum;
					datum.update_vpattern;
				};
				self.update_ordered_args;
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

	set_env_mode: { arg self, val = true;
		self.env_mode = val;
		self.env_mode.debug("SET ENV MODE!!!");
		self.build_real_sourcepat; // FIXME: already called just before setting env_mode (at init)
	},

	////////////////// params

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
	set_arg: ~player_set_arg,

	map_arg: { arg self, argName, val;
		argName.debug("mapping hidden!!!");
		~get_spec.(argName, self.defname).map(val);
	},

	unmap_arg: { arg self, argName, val;
		~get_spec.(argName, self.defname).unmap(val);
	},

	get_args: { arg self;
		self.data.keys
	},

	get_all_args: { arg self;
		var res;
		res = OrderedIdentitySet.new;
		res.addAll(self.get_args);
		self.effects.do { arg fx, i;
			res.addAll(self.get_main.get_node(fx).get_args.collect { arg ar;
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
			self.get_main.get_node(self.effects[fxnum]).get_arg(argname);
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

	get_duration: { arg self;
		// TODO: return correct value for others modes
		var dur;
		var repeat;
		dur = switch(self.current_mode,
			\stepline, {
				self.get_arg(\stepline).get_cells.size * self.get_arg(\dur).get_val
			},
			{
				self.get_arg(self.current_mode).get_duration;
			}
		);
		repeat = self.get_arg(\repeat).get_val;
		if(repeat == 0) { repeat = 10 };
		dur * repeat;
	},


	////////////////// save/load

	save_data: { arg self, options;
		var argdat;
		var data = ();
		data.args = ();
		self.get_args.do { arg key;
			argdat = self.get_arg(key);	
			if(self.archive_param_data.includes(argdat.classtype), {
				data.args[key] = argdat.save_data
			})
		};
		//options.debug("player SAVE DATA");
		data.modulation = self.modulation.save_data(options);
		data.effects = self.effects.save_data(options);
		data.current_mode = self.get_mode;
		self.archive_data.do { arg key;
			if(self[key].notNil) {
				data[key] = self[key]
			}
		};
		data;
	},

	save_data_without_score: { arg self, options;
		var data, keys;
		data = self.save_data(options);
		keys = [\current_mode, \dur, \segdur, \velocity, \stretchdur, \sustain, \amp] ++ self.available_modes;
		data.current_mode = nil;
		keys.do { arg key;
			data.args[key] = nil
		};
		data;
	},

	save_data_preset: { arg self;
		var options = IdentityDictionary.new;
		options[\copy_subplayers] = true;
		self.save_data_without_score(options)
	},

	load_data_preset: { arg self, data;
		var options = IdentityDictionary.new;
		options[\copy_subplayers] = true;
		options[\dont_overwrite_uname] = true;
		self.load_data(data, options)
	},

	load_data: { arg self, data, options;
		var argdat;
		var archive_data;
		options = options ?? IdentityDictionary.new;

		if(options[\dont_overwrite_uname].notNil and: { options[\dont_overwrite_uname].notNil }) {
			archive_data = self.archive_data.reject { arg key; [\uname, \name].includes(key) };
		} {
			archive_data = self.archive_data
		};

		self.get_args.do { arg key;
			argdat = self.get_arg(key);	
			if(self.archive_param_data.includes(argdat.classtype), {
				if( data.args[key].notNil ) {
					argdat.load_data( data.args[key] )
				}
			})
		};
		self.modulation.load_data(data.modulation, options);
		self.effects.load_data(data.effects, options);
		archive_data.do { arg key;
			if(data[key].notNil) {
				self[key] = data[key]
			}
		};
		if(data.current_mode.notNil) {
			self.set_mode(data.current_mode);
		};
		self.set_wrapper_code(data.sourcewrapper);
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

	////////////////// automation

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

	////////////////// pattern

	get_dur_pattern: { arg self, add_freq=true;
		var arglist = List.new;
		var val;
		var patkeys;
		arglist.add(\current_mode);
		arglist.add(Pfunc{ self.get_mode });
		arglist.add(\muted); arglist.add(Pfunc({ self.muted or: self.temp_muted }));
		patkeys = [\repeat] ++ self.available_modes ++ [\sustain, \stretchdur, \segdur,  \velocity, \dur, \type];
		if(add_freq) {
			patkeys = patkeys ++ [\freq];
		};
		patkeys.do { arg key;
			if(self.data[key].notNil) {
				val = self.get_arg(key);
				if(val.notNil) {
					arglist.add(key);
					arglist.add(val.vpattern);
				}
			}
		};
		arglist.debug("++++++++++++++++++++++++++++ ARGLIST");

		Pbind(* arglist )
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

	build_standard_args: { arg self;
			self.data[\noteline] = self.data[\noteline] ?? ~make_noteline_param.(\noteline);
			self.data[\scoreline] = self.data[\scoreline] ?? ~make_scoreline_param.(\scoreline);

			self.data[\dur] = self.data[\dur] ?? 
				~make_control_param.(self.get_main, self, \dur, \scalar, 0.5, ~get_spec.(\dur, self.defname));
			self.data[\segdur] = self.data[\segdur] ??
				~make_control_param.(self.get_main, self, \segdur, \scalar, 0.5, ~get_spec.(\dur, self.defname));
			self.data[\velocity] = self.data[\velocity] ??
				~make_control_param.(self.get_main, self, \velocity, \scalar, 0.8, \unipolar.asSpec);
			self.data[\stretchdur] = self.data[\stretchdur] ??
				~make_control_param.(self.get_main, self, \stretchdur, \scalar, 1, ~get_spec.(\dur, self.defname));
			self.data[\legato] = self.data[\legato] ??
				~make_control_param.(self.get_main, self, \legato, \scalar, 0.5, ~get_spec.(\legato, self.defname));
			self.data[\sustain] = self.data[\sustain] ??
				~make_control_param.(self.get_main, self, \sustain, \scalar, 0.5, ~get_spec.(\sustain, self.defname));
			self.data[\repeat] = self.data[\repeat] ?? 
				// FIXME: should be simple numeric param
				~make_control_param.(self.get_main, self, \repeat, \scalar, 1, ~get_spec.(\repeat, self.defname));

			self.data[\stepline] = self.data[\stepline] ?? ~make_stepline_param.(\stepline, 1 ! 8 );
		if(self.is_effect.not) {
			self.data[\type] = ~make_type_param.(\type);
		};
		self.data[\instrument] = self.data[\instrument] ?? ~make_literal_param.(\instrument, self.defname);
	},

	build_sourcepat: { arg self;
	
		var dict = Dictionary.new;
		var list = List[];
		var prio, reject;
		// FIXME: move list to class_player_display
		prio = [\repeat, \instrument, \stretchdur] ++ self.available_modes ++ [
			\type, \samplekit, \mbufnum, \bufnum, \dur, \segdur, \velocity, \legato, \sustain
		];
		reject = [];
		if(self.is_effect) {
			reject = [\instrument];
		};

		list.add(\elapsed); list.add(Ptime.new);
		list.add(\current_mode); list.add(Pfunc({ self.get_mode }));
		list.add(\muted); list.add(Pfunc({ self.muted or: self.temp_muted }));
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
		if(self.additional_data.notNil) {
			self.additional_data.keysValuesDo { arg key, val;
				list.add(key); list.add(val);
			};
		};
		//list.debug("maked pbind list");
		//[\type, \stepline, \instrument].do { arg x; list.add(x); list.add(dict[x]) };
		list.debug("maked pbind list");
		//Pbind(*list).dump;
		self.sourcepat_list = list;

		self.update_ordered_args; // dont found a better place for this


		self.build_sourcepat_finalize;
	},

	build_sourcepat_finalize: { arg self;
		var list = self.sourcepat_list;
		self.sourcepat = if(self.is_effect) {
			Pmono(self.data[\instrument].get_val, *list)
		} {
			//DebugPbind(*list); //debug
			Pbind(*list); //debug
		}
	},

	build_real_sourcepat: { arg self;
		var res, list;
		var chain;
		"entering build_real_sourcepat".debug;
		// stages of building pattern:
		// - wrapping in wrapper code
		// - apply input pattern
		// - wrapping in external wrapper (external player like Passive)
		// - modulation pspawner, compose with ppatch and put in a kind of Ppar with:
		//	- modulations patterns
		//	- effects patterns
		//	- recorded automation patterns (TO BE DONE)

		// FIXME: currently, modulation and effect patterns don't use input, wrapper and external wrapper patterns


		res = if(self.wrapper.notNil) {
			self.wrapper;
		} {
			self.sourcepat;
		};

		if(self.input_pattern.notNil) {
			//"entering build_real_sourcepat: making input pattern".debug;
			//self.input_pattern.source.postcs;
			//res.postcs;
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


		//res = if(self.effects.size > 0) {
		//	~pfx.(
		//		res,
		//		self.effects.collect { arg fx;
		//			//fx.debug("effect");
		//			self.get_main.get_node(fx).vpattern.postcs;
		//		}
		//	)
		//} {
		//	res;
		//};

		if(self.external_player.notNil) {
			res = self.external_wrap(res);
		};

		res.debug("REAL_SOURCEPAT");
		//res.postcs;

		self.modulation.update_modulation_pattern(res);
		res = self.modulation.make_modulation_pattern;

		// add bus setting
		//if(self.ccbus_set.size > 0) {
		//	list = self.ccbus_set.as(Array).collect({ arg x; x.recordbus.vpattern }).reject(_.isNil) ++ [res];
		//	//list.debug("******** build_real_sourcepat: ppar list");
		//	res = Ppar( list )
		//};

		self.real_sourcepat = res.trace; //DEBUG
		//self.real_sourcepat = res;
		//self.real_sourcepat = res.postcs.trace; //DEBUG
		//self.real_sourcepat = res.postcs;
	},

	vpattern: { arg self;
		//self.wrapper.debug("vpattern called: wrapper");
		self.real_sourcepat;
	},

	vpattern_loop: { arg self;
		Pn(self.vpattern, ~general_sizes.safe_inf);
		//Pn(self.vpattern, 1); // debug
		//self.vpattern; //debug
	},

	////////////////// playing state

	set_playing_state: { arg self, state;
		[self.uname, state].debug("player: set_playing_state");
		self.playing_state = state;
		self.changed(\redraw_node);
	},

	get_playing_state: { arg self;
		switch(self.muted or: self.temp_muted,
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

	////////////////// live playing

	get_piano: { arg self, kind=\normal;
		// FIXME: handle mono bufnum
		var exclu, list = List[];
		exclu = [
			\instrument, \noteline, \scoreline, \sampleline, \samplekit, \repeat, \stretchdur, \stepline, 
			\type, \dur, \segdur, \velocity, \legato, \sustain,
			\amp, \bufnum, \mbufnum, \freq,
		];
		self.data.keys.difference(exclu).do { arg key;
			var val = self.data[key].vpiano ?? self.data[key].vpattern;
			list.add(key); list.add( val ) 
		};
		if(kind == \nsample) {
				[\freq, \bufnum, \mbufnum].do { arg paramname;
					if(self.data[paramname].notNil) {
						list.add(paramname); list.add( self.data[paramname].vpiano ?? self.data[paramname].vpattern );
					};
				};
				{ arg slotnum, veloc=1; 
					veloc = veloc ?? 1;
					Synth(self.data[\instrument].vpiano, (
						[\amp, self.get_main.calcveloc(self.data[\amp].vpiano.value, veloc) ] ++
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
							\amp, self.get_main.calcveloc(self.data[\amp].vpiano.value, veloc) ] ++
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
					var spatch;
					veloc = veloc ?? 1;
					[self.data[\amp].vpiano.value, veloc].debug("CESTLA?");
					if(freq.isNil) { "get_piano: why freq is nil ?".debug; };
					self.data[\instrument].vpiano.value.debug("making Synth()");

					spatch = self.modulation.vpiano;

					Synth(self.data[\instrument].vpiano.value, (
						[
							\amp, self.get_main.calcveloc(self.data[\amp].vpiano.value, veloc),
							\freq, freq,
						] 
						++ list.collect(_.value(spatch))).debug("arg listHHHHHHHHHHHHHHHHHHHHHHHHHHH")
					) 
				}

			};
		}

	},

	////////////////// playing/stopping

	prepared_node: { arg self;
		self.node.source = self.vpattern;
		self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++setting source");
		self.node;
	},

	play_node: { arg self;
		self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++setting source(inf)");
		self.get_main.play_manager.play_node(self.uname);
		//self.node.play;
	},

	stop_node: { arg self, use_quant;
		self.get_main.play_manager.stop_node(self.uname, use_quant);
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

	temp_mute: { arg self, val=true;
		if(val != self.temp_muted) {
			self.temp_muted = val;
			if(self.is_audiotrack) {
				//FIXME: temp_mute on audiotrack ?
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


);

~class_cinstr_player = (
	parent: ~class_synthdef_player,
	new: { arg self, main, instr, data; 
		var cinstr;
		self = self.deepCopy;

		"ion est la".debug;
		//self.external_player = ~class_passive_controller.new(UniqueID.next.asString);
		self.instrname = instr;
		cinstr = ~classinstr_lib[instr.replace("ci ", "").asSymbol];

		self.get_main = { arg self; main };
		self.external_player = cinstr.new(main, self);
		self.defname = self.external_player.synthdef_name;
		self.external_player.init_top_classinstr;
		self.to_destruct.add(self.external_player);
		self.is_effect = self.external_player.is_effect ?? false;

		"on est la".debug;
		//self.external_player.build_synthdef;
		self.init(data);

		if(self.external_player.modulation_kind.notNil) {
			self.modulation.set_mod_kind(self.external_player.modulation_kind);
		};

		if(self.external_player.player_mode.notNil) {
			self.set_mode(self.external_player.player_mode);
		};

		"on est la2".debug;
		self.data[\dur].select_cell(2);
		//self.data[\repeat].set_val(0);

		"on est la3".debug;
		self;
	},

	clone: { arg self;
		var pl;
		var options = (copy_subplayers: true);
		pl = ~class_cinstr_player.new(self.get_main,self.instrname);
		pl.load_data( self.save_data(options).deepCopy, options );
		pl;
	},

	uname_: { arg self, uname;
		//self.external_player.synthdef_name_suffix = "_"++uname.replace("passive ", "");
		self[\uname] = uname;
		self.external_player.synthdef_name_suffix = "_"++uname;
		"BEFORE".debug;
		self.external_player.build_synthdef;
		"AFTER".debug;
		self.defname = self.external_player.synthdef_name;
		"BUoUUU".debug;
		self.build_sourcepat;
		self.build_real_sourcepat;
		"BUUUU".debug;
	},

	save_data: { arg self, options;
		var data;
		data = ~class_synthdef_player[\save_data].(self, options);
		data.external_data = self.external_player.save_data(options);
		data;
	},

	load_data: { arg self, data, options;
		self.external_player.freeze_build_synthdef_do {
			~class_synthdef_player[\load_data].(self, data, options);
			self.external_player.load_data(data.external_data, options);
		};
		if(options.notNil) {
			if(options[\build_synthdef] == false) {
				// do not build
			} {
				self.external_player.build_synthdef;
			};
		} {
			self.external_player.build_synthdef;
		};
	},

	get_ordered_args: { arg self;
		~sort_by_template.(self.data.keys, self.external_player.get_ordered_args_names);
	},

	external_wrap: { arg self, pat; pat },

	init: { arg self, data;
		var additional;
		var reject;
		var dict = Dictionary.new;
		var name;
		var macro_ctrl;

		self.sourcewrapper = "Pbind(\n\t\\freq, Pkey(\\freq)\n) <> ~pat;\nfalse";

		self.modulation = ~class_modulation_manager.new(self);
		self.effects = ~class_effect_manager.new(self);

		self.data = self.external_player.data.copy;

		self.build_standard_args;
		self.data[\instrument] = ~make_dynamic_literal_param.(\instrument, { self.external_player.synthdef_name });
		//self.data[\dur].set_val(2);

		self.build_sourcepat;
		self.build_real_sourcepat;
	
	},


);

~class_passive_player = (
	parent: ~class_synthdef_player,
	new: { arg self, main, instr, data; 
		self = self.deepCopy;

		"ion est la".debug;
		//self.external_player = ~class_passive_controller.new(UniqueID.next.asString);
		self.instrname = instr;
		self.external_player = ~class_passive_controller.new;
		self.to_destruct.add(self.external_player);
		"on est la".debug;
		self.external_player.load_preset_by_uname(instr.replace("passive ", ""));
		self.defname = self.external_player.synthdef_name;
		self.get_main = { arg self; main };

		//self.external_player.build_synthdef;
		self.init(data);

		self;
	},

	uname_: { arg self, uname;
		self.external_player.synthdef_name_suffix = "_"++uname.replace("passive ", "");
		self.external_player.build_synthdef;
		self.defname = self.external_player.synthdef_name;
		self[\uname] = uname;
		self.build_sourcepat;
		self.build_real_sourcepat;
	},

	init: { arg self, data;
		var additional;
		var reject;
		var dict = Dictionary.new;
		var name;
		var macro_ctrl;
		8.do { arg idx;
			name = "macro%_control".format(idx+1).asSymbol;
			macro_ctrl = self.external_player.get_arg(name);
			dict[name] = ~make_macro_control_param.(
				self.get_main,
				macro_ctrl,
				self,
				name,
				\scalar,
				macro_ctrl.get_val,
				macro_ctrl.model.spec
			)

		};

		name = \amp;
		macro_ctrl = self.external_player.get_arg(name);
		dict[name] = ~make_macro_control_param.(
			self.get_main,
			macro_ctrl,
			self,
			name,
			\scalar,
			macro_ctrl.get_val,
			macro_ctrl.model.spec
		);

		name = \freq;
		dict[name] = ~make_control_param.(
			self.get_main,
			self,
			name,
			\scalar,
			300,
			~get_spec.(name, self.defname)
		);

		self.data = dict;

		self.build_standard_args;
		self.data[\instrument] = ~make_dynamic_literal_param.(\instrument, { self.external_player.synthdef_name });
		self.data[\dur].set_val(2);

		reject = Set[\freq, \velocity, \instrument];

		additional = Dictionary.new;
		self.external_player.synthdef_args.keys.difference(reject).do { arg key;
			additional[key] = self.external_player.synthdef_args[key];
		};

		additional[\velocity] = 0.5;
		additional[\doneAction] = 14;
		additional[\compute_freq] = Pfunc { arg ev;
			var dict = self.external_player.compute_freq(ev[\freq]);
			ev[\freq].debug("freq");
			dict.debug("dict");
			ev.putAll(dict);
			1;
		};

		self.additional_data = additional;

		self.build_sourcepat;
		self.build_real_sourcepat;
	
	},


	get_ordered_args: { arg self;
		var macros;
		macros = 8.collect { arg x;
			"macro%_control".format(x+1).asSymbol;
		};
		~sort_by_template.(self.data.keys, macros);
	},

	external_wrap: { arg self, pat;
		Pspawner({ |spawner|
			var str, pbus, pgroup, leffect;
			var fxpat;
			var mpat;
			var note_group;
			var make_fx_group;
			var current_fx_group;
			var blist = List.new;

			note_group = Group.new(s);

			self.external_player.rebuild_synthdef = false;
			self.free_note_group = true;

			mpat = Pset(\modulation_bus, Pfunc{ arg ev;
				var group = Group.new(note_group);
				var modbus = 8.collect { arg x; Bus.audio(s, 1) };
				var watcher;
				var endfunc;
				ev[\group] = group.nodeID;
				watcher = NodeWatcher.register(group);

				"appel".debug;

				endfunc = { arg obj, what;
					[obj, what].debug("what ?");
					if(what == \n_end) {
						"fin du group note solo".debug;
						group.releaseDependants;
						modbus.do { arg x; x.free };
					}
				};
				group.addDependant(endfunc);
				[modbus.collect{ arg x; x.index }]
			}, pat);

			str = CleanupStream(mpat.asStream, {
				"cleanup du pattern".debug;
				spawner.suspendAll;
				//pbus.free;
				//glist.do(_.free);
			});

			make_fx_group = {
				var fx_group;
				fx_group = Group.after(note_group).register;
				fx_group.addDependant( { arg grp, status;
					[grp, status].debug("pourquoi ?");
					if(status == \n_end) {
						"fin des groupes fx et notes".debug;
						fx_group.releaseDependants;
						if(self.free_note_group) {
							note_group.free;
						} {
							self.free_note_group = true;
						};
					}
				});
				fx_group;
			};

			current_fx_group = make_fx_group.();

			fxpat = PmonoArtic(*(
				[
					self.external_player.synthdef_fx_name, 
					\doneAction, 14,
					\group, Pfunc { current_fx_group },
					\legato, Pfunc { arg ev;
						if(self.external_player.rebuild_synthdef) {
							"fx_pat: rebuild synthdef".debug;
							self.external_player.rebuild_synthdef = false;
							current_fx_group = make_fx_group.();
							self.free_note_group = false;
							0.999;
						} {
							"fx_pat: no rebuild".debug;
							1
						};
					},
				] ++ self.external_player.synthdef_args.getPairs
			));

			spawner.par(fxpat);
			spawner.par(str);
		});
	
	}

);

~class_modenv_player = (
	parent: ~class_synthdef_player,
	subkind: \modenv,
	new: { arg self, main, instr, data=nil;
		var desc;
		var notescore, notes;
		var defname = instr;
		desc = SynthDescLib.global.synthDescs[defname];
		self = self.deepCopy;
		
		self.instrname = instr;
		self.defname = defname;

		if(desc.isNil, {
			("ERROR: make_player_from_synthdef: SynthDef not found: "++defname).error
		});
		defname.debug("loading modenv player from");

		self.get_main = { arg self; main };
		self.get_desc = { arg self; desc };


		self.init(data);

		self.modulation.set_mod_kind(\pattern);

		self.data[\tsustain] = ~class_param_tsustain_controller.new(\tsustain);
		//self.data[\val] = ~class_param_scorekey_controller.new(self, \val);
		self.data[\val] = ~class_param_modenv_val_controller.new(self, \val);
		self.set_mode(\noteline);
		notescore = ~make_notescore.();
		notes = [
			(
				val: 0.5,
				sustain: 0.1,
				dur: 2.0
			),
			(
				val: 0.5,
				sustain: 0.1,
				dur: 2.0
			),
		];

		notescore.set_notes(notes);
		notescore.no_first_rest = true;
		notescore.set_end(16);
		//self.get_arg(\noteline).get_scoreset.set_notescore(notescore);
		self.get_arg(\noteline).get_scoreset.set_current_sheet(notescore);
		self.get_arg(\val).set_notes(notescore.get_rel_notes);
		self.data[\firstsynth] = nil;
		self.data[\firstval] = nil;


		self.build_sourcepat;
		self.build_real_sourcepat;

		self.make_fake_external_player;
		debug("fin make_fake_external_player");

		self;
	},

	make_fake_external_player: { arg self;
		// fake external player to have custom gui
		self.track_display = ~class_track_display.new;
		self.curve_track_controller = ~class_curve_track_controller.new(self, self.track_display, \val);
		self.external_player = (make_layout: { arg se; self.make_layout }); 

	},

	make_layout: { arg self;
		 self.layout = VLayout(
		 	self.curve_track_controller.make_gui
		 );
		 self.external_player.layout = self.layout;
		 self.layout;
		
	},

	load_data: { arg self, data, options;
		var notescore;
		~class_synthdef_player[\load_data].(self, data, options);
		notescore = self.get_arg(\noteline).get_scoreset.get_notescore;
		self.get_arg(\val).set_notes(notescore.get_rel_notes);
	},
);

~class_setbus_player = (
	parent: ~class_synthdef_player,
	subkind: \setbus,
	new: { arg self, main, instr, data=nil;
		var desc;
		var notescore, notes;
		var defname = instr;
		desc = SynthDescLib.global.synthDescs[defname];
		self = self.deepCopy;
		
		self.instrname = instr;
		self.defname = defname;

		if(desc.isNil, {
			("ERROR: make_player_from_synthdef: SynthDef not found: "++defname).error
		});
		defname.debug("loading modenv player from");

		self.get_main = { arg self; main };
		self.get_desc = { arg self; desc };


		self.init(data);

		self.modulation.set_mod_kind(\pattern);
		self.set_mode(\scoreline);

		self.get_arg(\val).change_kind(\scoreseq);
		notescore = ~make_notescore.();
		notes = [
			(
				val: 0.0,
				sustain: 0.1,
				dur: 2.0
			),
			(
				val: 0.5,
				sustain: 0.1,
				dur: 2.0
			),
		];

		notescore.set_notes(notes);
		notescore.no_first_rest = true;
		notescore.set_end(4);
		self.get_arg(\scoreline).get_scoreset.set_notescore(notescore);

		self.build_sourcepat;
		self.build_real_sourcepat;

		self.make_fake_external_player;
		debug("fin make_fake_external_player");

		self;
	},

	make_fake_external_player: { arg self;
		// fake external player to have custom gui
		self.track_display = ~class_track_display.new;
		self.curve_track_controller = ~class_scoreseq_track_controller.new(self, self.track_display, \val);
		self.external_player = (make_layout: { arg se; self.make_layout }); 

	},

	make_layout: { arg self;
		 self.layout = VLayout(
		 	self.curve_track_controller.make_gui
		 );
		 self.external_player.layout = self.layout;
		 self.layout;
		
	},

	//load_data: { arg self, data, options;
	//	var notescore;
	//	~class_synthdef_player[\load_data].(self, data, options);
	//	notescore = self.get_arg(\noteline).get_scoreset.get_notescore;
	//	self.get_arg(\val).set_notes(notescore.get_rel_notes);
	//},
);

~class_gater_player = (
	parent: ~class_synthdef_player,
	subkind: \setbus,
	new: { arg self, main, instr, data=nil;
		var desc;
		var notescore, notes;
		var defname = instr;
		desc = SynthDescLib.global.synthDescs[defname];
		self = self.deepCopy;
		
		self.instrname = instr;
		self.defname = defname;

		if(desc.isNil, {
			("ERROR: make_player_from_synthdef: SynthDef not found: "++defname).error
		});
		defname.debug("loading modenv player from");

		self.get_main = { arg self; main };
		self.get_desc = { arg self; desc };


		self.init(data);


		self.data[\gtrig] = ~class_param_gtrig.new(\gtrig);
		self.modulation.set_mod_kind(\pattern);
		self.set_mode(\scoreline);

		self.build_sourcepat;
		self.build_real_sourcepat;

		self.make_fake_external_player;
		debug("fin make_fake_external_player");

		self;
	},

	make_fake_external_player: { arg self;
		// fake external player to have custom gui
		self.track_display = ~class_track_display.new;
		self.curve_track_controller = ~class_step_track_controller.new(self, self.track_display);
		self.external_player = (make_layout: { arg se; self.make_layout }); 

	},

	make_layout: { arg self;
		 self.layout = VLayout(
		 	self.curve_track_controller.make_gui
		 );
		 self.external_player.layout = self.layout;
		 self.layout;
		
	},

	//load_data: { arg self, data, options;
	//	var notescore;
	//	~class_synthdef_player[\load_data].(self, data, options);
	//	notescore = self.get_arg(\noteline).get_scoreset.get_notescore;
	//	self.get_arg(\val).set_notes(notescore.get_rel_notes);
	//},
);

~make_player_from_synthdef = { arg main, defname, data=nil;
	// changed messages: \redraw_node, \mode
	var player;
	player = ~class_synthdef_player.new(main, defname, data);
	player;
};

~make_player_from_pattern = { arg main, pat;
	var player = ~make_player_from_synthdef.(main, \default);
	player.sourcepat = {
		var dict = Dictionary.new;
		var list = List[];
		var prio, reject;

		list.add(\muted); list.add(Pfunc({ player.muted or: player.temp_muted }));
		list.add(\type); list.add(
			Pfunc({ arg ev;
				if(ev[\muted]) {
					\rest
				} {
					ev[\type]
				}
			})
		);
		Pbind(*list);
	}.value;

	player.set_input_pattern(pat);
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
				elm.debug("############===================================== set pkey");
				player.get_args.debug(">>>========== args");
				param = player.get_arg(elm[0]);
				if(param.notNil) {
					param.name.debug("===================================== set pkey");
					if(elm[1].isNumber) {
						param.set_val(elm[1])
					};
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
			//if[\stretchdur, \segdur, 
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

~special_players = (
	modenv: ~class_modenv_player,
	setbus: ~class_setbus_player,
	gater: ~class_gater_player,
);

~make_player = { arg main, instr, data=nil;
	var player = nil;
	case
		{ instr.isString and: {instr.beginsWith("passive ")}} {
			player = ~class_passive_player.new(main, instr, data)
		}
		{ instr.isString and: {instr.beginsWith("ci ")}} {
			player = ~class_cinstr_player.new(main, instr, data)
		}
		{ instr.isSymbolWS || instr.isString } {
			player = ~special_players[instr];
			if(player.notNil) {
				player = player.new(main, instr, data);
			} {
				switch(instr,
					\seqnode, {
						player = ~make_seqplayer.(main);
					},
					\parnode, {
						player = ~make_parplayer.(main);
					},
					{
						player = ~make_player_from_synthdef.(main,instr, data);
					}
				);
			}
		} 
		{ instr.isFunction } {
			// FIXME: instr should be a symbol or string
			player = ~make_player_from_patfun.(instr, data);
		}
		{ ("ERROR: player type not recognized:"++instr).postln }
	;
	player;
};

~make_empty_player = (
	name: \voidplayer,
	uname: \voidplayer,
	get_label: "voidplayer",
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
	// changed messages: \redraw, \redraw_node, \selected_child, \expset_mode
	var pplayer;
	pplayer = (
		//children: SparseArray.newClear(8, ~empty_player),
		parent: ~class_abstract_node,
		children: SparseArray.newClear(~general_sizes.children_per_groupnode, \voidplayer),
		kind: \parnode,
		name: \new,
		uname: \new,
		data: Dictionary.new,
		archive_data: [\children, \kind, \subkind, \name, \uname, \instrname, \selected_child, \selected_child_index, \expset_mode],
		archive_classtype: [\control, \stepline, \adsr, \noteline, \sampleline, \samplekit, \nodeline, \buf],
		playlist: List.new,
		playing_state: \stop,
		muted: false,
		get_main: { main },
		selected_child: \none,
		selected_child_index: 0,
		expset_mode: false,

		init: { arg self;
			self.data[\repeat] = self.data[\repeat] ?? ~make_control_param.(main, self, \repeat, \scalar, 1, ~get_spec.(\repeat));
			self.data[\amp] = self.data[\amp] ?? ~make_control_param.(main, self, \amp, \scalar, 1, ~get_spec.(\amp)); // dummy param FTM
			self.get_arg(\repeat).get_val.debug("init repeat.get_val");
			self.update_ordered_args;
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

		///// selection

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

		get_selected_param_object: { arg self;
			self.get_arg(self.selected_param);
		},

		set_name_of_selected_child: { arg self, name;
			name.debug("groupnode: set_selected_child");
			self.children[self.selected_child_index] = name;
			self.refresh;
		},
		set_selected_child: { arg self, name; self.set_name_of_selected_child(name); },

		get_selected_childname: { arg self;
			self.children[self.selected_child_index]
		},

		select_child_index: { arg self, index;
			self.selected_child_index = index;	
		},

		select_child_at: { arg self, index, force_select=false;
			//var oldidx;
			//oldidx = self.selected_child_index;
			 if(self.children[index].notNil and: {
				 force_select or: {
					self.selected_child_index != index
				 } 
			 }) {
				self.selected_child_index = index;
				self.selected_child = self.children[index]; // pas besoin normalement
				//self.changed(\selected_child, oldidx);
				self.changed(\selected_child, index);
				true;
			} {
				false;
			}
		},


		select_child: { arg self, name;
			var oldidx;
			self.selected_child = name;
			oldidx = self.selected_child_index;
			self.selected_child_index = self.children.detectIndex { arg i; i == name };
			self.changed(\selected_child, oldidx);
			self.changed(\selected_child, self.selected_child_index);
		},

		///// setting children

		set_children_name: { arg self, index, name, refresh=true;
			self.children[index] = name;
			//self.changed(\children, index, main.get_node(name)); //TODO: use individual containers
			if(refresh) { self.refresh };
		},

		add_children: { arg self, name;
			name.debug("groupplayer.add_children");
			self.children = self.children.add(name);
			self.refresh;
		},

		add_child_replace_void: { arg self, name;
			block { arg break;
				self.children.do { arg childname, i;
					if(childname == \voidplayer) {
						self.set_children_name(i, name);
						break.value;
					}
				
				}
			}
		},

		///// getting children

		get_childname_by_index: { arg self, index;
			self.children[index];
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

		get_children_names: { arg self;
			var res;
			res = self.children.reject({ arg na; [\void, \voidplayer].includes(na) });
			res;
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
			[self.uname, self.identityHash, res.collect(_.uname)].debug("groupplayer: get_view_children: name, hash, children");
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

		get_selected_child: { arg self;
				
		},

		////// playing

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

		////////////

		refresh: { arg self;
			self.changed(\redraw, self, self.get_view_children);
			self.changed(\selected_child, self.selected_child_index);
			if(self.expset_mode) {
				main.play_manager.expset_manager.add_expset(self.uname, self.get_children_names)
			}
		},

		set_expset_mode: { arg self, val;
			//TODO: remove expset when freeing node
			if(val != self.expset_mode) {
				self.expset_mode = val;
				self.changed(\expset_mode);
				if(val) {
					main.play_manager.expset_manager.add_expset(self.uname, self.get_children_names)
				} {
					main.play_manager.expset_manager.del_expset(self.uname)
				}
			}
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
			Ppar(self.get_children_nodes2);
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
			PnNilSafe(self.vpattern, ~general_sizes.safe_inf,3);
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
			self.data[\amp] = self.data[\amp] ?? ~make_control_param.(main, self, \amp, \scalar, 1, ~get_spec.(\amp)); // dummy param FTM
			self.get_arg(\repeat).get_val.debug("init repeat.get_val");
			self.update_ordered_args;
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

// without seco libs
~ppar = { arg par, repeat; //par: node list
	Pn(Pspawner({ |spawner|
		var streams = List.new;
		par.do { |node, i|
			var stream;
			stream = CleanupStream(node.asStream, { // use vpattern to avoid Pn(vpattern)
				streams.remove(stream);
				if(streams.isEmpty) {
					spawner.suspendAll;
				} {
					spawner.par(Pn(node, ~general_sizes.safe_inf))
				};
			});
			streams.add(stream);
			spawner.par(stream);
		};
	}),repeat);
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


~setDictionary = {
	var dic;
	dic = (
		keyval: Dictionary.new, // key -> val set
		valkey: Dictionary.new, // val -> key set

		bind_set: { arg self, key, vals;
			var oldvals, oldkeys;
			self.unbind_key(key);
			self.keyval[key] = vals;
			vals.do { arg val;
				if(self.valkey[val].isNil) {
					self.valkey[val] = Set.new;
				};
				self.valkey[val].add(key);
			};
		},

		unbind_key: { arg self, key;
			var vals;
			vals = self.keyval[key];
			vals.do { arg val;
				if(self.valkey[val].notNil) { self.valkey[val].remove(key) };
			};
			self.keyval[key] = nil;
		},

		get_vals_by_key: { arg self, key;
			self.keyval[key]
		},

		get_keys_by_val: { arg self, val;
			self.valkey[val]
		}

	);
	dic;

};

//a = ~exclusive_play_set.()
//
//a.add_expset(\perc, [\kick1, \snare1, \hihat])
//a.add_expset(\perc2, [\kick2, \snare, \hihat])
//a.add_expset(\melo, [\bass, \lead, \pad])
//a.set_dict
//a.set_selected_child
//a.get_nodes_to_stop(\hihat, Set[\kick2, \snare1, \hihat, \kick, \pad])
//a.set_dict.get_keys_by_val(\kick2)
//a.set_dict.get_vals_by_key(\perc2)
//

