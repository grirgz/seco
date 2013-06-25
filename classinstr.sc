//////////////////////////////////////////////////////
////////////// Core Class Instrs
//////////////////////////////////////////////////////

~class_ci_namer = (
	new: { arg self, prefix="";
		self = self.deepCopy;

		self.prefix = prefix;
	
		self;
	},

	breed: { arg self, prefix="";
		var newself;
		newself = self.deepCopy;

		newself.parent_namer = self;
		newself.prefix = prefix;
	
		newself;
	},

	abs_name: { arg self, name;
		var res;
		res = (self.prefix ++ name).asSymbol;
		if(self.parent_namer.notNil) {
			self.parent_namer.abs_name( res );
		} {
			res;
		};
	},

	rel_name: { arg self, name;
		(self.prefix ++ name).asSymbol
	},
);

~class_instr = (
	static_data: IdentityDictionary.new,
	ordered_args: List[],
	simple_args: (),
	data: IdentityDictionary.new,
	args_prefix: "",
	args_suffix: "",
	synthdef_name_suffix: "",
	synthdef_basename: "s",
	archive_data: [\synthdef_name_suffix],
	is_effect: false,
	freeze_build_synthdef: false,
	is_top_classinstr: false,

	local_static_data: IdentityDictionary.new,
	local_data: IdentityDictionary.new,
	local_ordered_args: List.new,

	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
	
		self;
	},

	make_gui: { arg self;
		var win;
		Task{
			self.freeze_build_synthdef_do {
				win = Window.new("Edit %".format(self.get_player.get_label));
				self.make_layout;
				win.layout = self.layout;
				self.window = win;
				self.make_bindings;
				//FIXME: set on focus
				self.set_all_bus_mode(true);
				self.window.view.onClose = self.window.view.onClose.addFunc({
					debug("CLOSING");
					self.set_all_bus_mode(false)
				});
				self.window.view.keyDownAction = self.binding_responder.get_kb_responder(\classinstr, self);
				win.front;
			};
		}.play(AppClock)
	},

	save_data: { arg self;
		var data;
		data = ();
		data.static_data = ();
		self.static_data.keysValuesDo { arg key, datum;
			data.static_data[key] = datum.save_data;
		};
		self.archive_data.do { arg key;
			data[key] = self[key];
		};
		data;
	},

	load_data: { arg self, data;
		data.static_data.keysValuesDo { arg key, val;
			self.static_data[key].load_data(val)
		};
		self.archive_data.do { arg key;
			self[key] = data[key];
		};
		self.build_synthdef;
	},


	///////////////// param management

	init_top_classinstr: { arg self;
		self.is_top_classinstr = true;
	},

	build_standard_data: { arg self;
		var data;
		if(self.standard_data.isNil) {
			data = IdentityDictionary.new;
			data[\freq] = ~make_control_param.(self.get_main, self.get_player, \freq, \scalar, 209, \freq.asSpec);
			data[\velocity] = ~make_control_param.(self.get_main, self.get_player, \velocity, \scalar, 0.5, \unipolar.asSpec);
			self.standard_data = data;
		};
	},

	rebuild_arg_list: { arg self, rebuild_synthdef=true;
		self.synthdef_name.debug("class_instr: rebuild_arg_list =================================");
		self.ordered_args = List.new;
		self.data = IdentityDictionary.new;
		self.static_data = IdentityDictionary.new;
		
		self.ordered_args = self.ordered_args.addAll(self.local_ordered_args);
		self.data.putAll(self.local_ordered_args);
		self.static_data.putAll(self.local_static_data);

		//self.ordered_args.debug("ordered_args1");
		self.data.keys.debug("data1");
		self.static_data.keys.debug("static_data1");

		self.local_modules.do { arg mod;
			mod.rebuild_arg_list;
			self.static_data.putAll(mod.get_static_data);
			self.ordered_args = self.ordered_args.addAll(mod.get_ordered_args);
			self.data.putAll(mod.get_ordered_args);
		};

		self.data.keys.debug("data2");
		self.static_data.keys.debug("static_data2");

		self.param = ();
		self.param.putAll(self.ordered_args);
		self.param.putAll(self.static_data);

		if(self.is_top_classinstr) {

			self.build_standard_data;
			self.data.putAll(self.standard_data);

			self.set_static_responders;
			self.set_param_abs_labels;
			if(rebuild_synthdef==true) {
				self.build_synthdef;
			};
		};


		self.synthdef_name.debug(" -- FINN class_instr: rebuild_arg_list =================================");
		self.data;
	},

	get_ordered_args: { arg self;
		var res = List.new;
		res = self.ordered_args.clump(2).collect { arg keyval;
			keyval[0].debug("get_ordered_args ùùùùùùùùùùùùùùùù");
			[self.rel_namer(keyval[0]), keyval[1]]
			//[keyval[0], keyval[1]]
		};
		res.flat;
	},

	get_ordered_args_names: { arg self;
		self.ordered_args.clump(2).flop[0] ++ self.simple_args.keys
	},

	get_static_data: { arg self;
		var res = IdentityDictionary.new;
		self.static_data.keysValuesDo { arg key, val;
			res[self.rel_namer(key)] = val;
		};
		res;
	},

	get_data: { arg self;
		// FIXME: who use this ?
		var res = IdentityDictionary.new;
		self.data.keysValuesDo { arg key, val;
			res[self.rel_namer(key)] = val;
		};
		res;
	},

	set_static_responders: { arg self;
		var resp = Dictionary.new;
		self.static_responders.do(_.remove);
		//self.static_data.debug("class_instr.set_static_responders");
		self.static_data.keysValuesDo { arg name, datum;
			var sc;
			sc = SimpleController.new(datum);
			[name, datum.name].debug("class_instr.set_static_responders: param name");
			sc.put(\val, {
				[name, datum.name].debug("static_responder");
				self.build_synthdef;
			});
			resp[name] = sc;
		};
		self.static_responders = resp;
	},

	set_param_abs_labels: { arg self;
		self.data.keysValuesDo { arg name, datum;
			[name, datum.name].debug("class_instr.set_param_abs_labels: param name");
			if(datum.classtype == \control) {
				datum.set_abs_label(name);
				datum.name = name;
				[datum.name,name].debug("ABSLABEL");
			}
		}
	},

	set_all_bus_mode: { arg self, enable;
		// called when instanciating gui
		self.data.keysValuesDo { arg name, datum;
			[name, datum.name].debug("class_instr.set_all_bus_mode: param name");
			if(datum.classtype == \control) {
				datum.set_bus_mode(enable);
			}
		}
	},

	get_synthargs: { arg self, args=();
		var i;
		i = args.copy;
		self.synthdef_name.debug("class_instr: get_synthargs =================================");
		i.keys.debug("class_instr: herited args");

		self.data.keysValuesDo { arg name, datum;
			var control_name;
			[name, datum.name].debug("class_instr.get_synthargs: data");
			if(i[name].isNil) {
				control_name = self.abs_namer(name);
				[control_name, name, datum.default_value].debug("accepted");
				switch(datum.param_rate,
					\tr, {
						i[name] = control_name.tr(datum.default_value);
					},
					{
						i[name] = control_name.kr(datum.default_value);
					}
				);
				if(self.get_player.compositor.notNil) {
					//[name, datum.name, control_name].debug("setting argthunk +++++++++++++");
					i[name] = self.get_player.compositor.compose_synth_param(control_name, i[name]);
				}
			}
		};
		self.simple_args.keysValuesDo { arg name, def;
			[name, def].debug("class_instr.get_synthargs: simple arg");
			if(i[name].isNil) {
				name.debug("accepted");
				if(def == \void) {
					i[name] = name.kr;
				} {
					i[name] = name.kr(def);
				}
			}
		};
		self.static_data.keysValuesDo { arg name, datum;
			[name, datum.name].debug("class_instr.get_synthargs: static_data");
			i[name] = switch(datum.classtype,
				\kind_chooser, {
					{ arg self; datum.get_val_uname; }
				}, 
				{
					{ arg self; datum.get_val; }
				}
			);
		};
		//i.keysValuesDo { arg key, val; [key, val].debug("class_instr.get_synthargs") };
		self.synthdef_name.debug(" -- FINN class_instr: get_synthargs =================================");
		i;
	},

	///////// data building helpers

	help_build_data: { arg self, datalist=[], static_datalist=(), modules;
		//debug("BEGIN help_build_data");
		//self.ordered_args.debug("ordered_args");
		self.local_modules = modules;
		self.local_ordered_args = datalist;
		self.local_static_data = static_datalist;

		self.param = ();
		self.param.putAll(self.local_ordered_args);
		self.param.putAll(self.local_static_data);
	},

	make_control_params: { arg self, params_data;
		params_data.collect { arg datum;
			var name, spec, default;
			#name, spec, default = datum;
			spec = if(spec.isNil) {
				if(self.get_specs[name].notNil) {
					self.get_specs[name]
				} {
					if(name.asSpec.notNil) {
						name.asSpec;
					} {
						\widefreq.asSpec;
					}
				}
			} {
				spec.asSpec;
			};
			default = default ?? { spec.default };
			[name, ~make_control_param.(self.get_main, self.get_player, name, \scalar, default, spec)];
		}.flat;
	},


	////////////////////// cinstr building helpers

	bypass: { arg self, val, fun, in;
		if(val == 1) {
			fun.value;
		} {
			in;
		}
	},

	get_spread_kind_variants: { arg self;
		[
			(
				name: "Normal",
				uname: \normal,
			),
			(
				name: "Interlaced",
				uname: \interlaced,
			),
		]
	},

	build_spread_array_by_kind: { arg self, unisono, kind=\normal;
		switch(kind,
			\interlaced, {
				self.build_spread_array_interlaced(unisono);
			},
			{
				self.build_spread_array(unisono);
			}

		)
	},

	build_spread_array: { arg self, unisono;
		var z, ret;
		if(unisono.asInteger.odd) {
			z = (unisono-1 / 2).asInteger;
			ret = z.collect { arg i; (i+1)/z };
			ret = 0-ret.reverse ++ 0 ++ ret;
		} {
			z = (unisono / 2).asInteger;
			ret = z.collect { arg i; (i+1)/z };
			ret = 0-ret.reverse ++ ret;
		};
	},

	build_spread_array_interlaced: { arg self, unisono;
		var z, ret;
		var gen_cell = { arg i; 
			var cell;
			cell = (i+1)/z;
			if(i.odd) {
				cell = 0-cell;
			};
			cell;
		};

		if(unisono.asInteger.odd) {
			z = (unisono-1 / 2).asInteger;
			ret = z.collect(gen_cell);
			ret = 0-ret.reverse ++ 0 ++ ret;
		} {
			z = (unisono / 2).asInteger;
			ret = z.collect(gen_cell);
			ret = 0-ret.reverse ++ ret;
		};
		ret;
	},

	build_freq_spread_array: { arg self, i, freq;
		if(i.enable_pitch_spread == 1) {
			var array = self.build_spread_array_by_kind(i.voices, i.spread_kind);
			array.debug("spread array");
			freq = (freq.cpsmidi + (i.pitch_spread * array)).midicps;
		} {
			freq = freq ! i.voices;
		};
		freq;
	},

	build_spread_array_for_param_with_args: { arg self, paramkey;
		//TODO
	},

	build_spread_array_for_param: { arg self, i, key, enabled_key, spread_key;
		var enabled, paramval, param, param_spread, res;
		enabled_key = enabled_key ?? "enable_%_spread".format(key).asSymbol;
		spread_key = spread_key ?? "%_spread".format(key).asSymbol;
		enabled = i[enabled_key].();
		paramval = i[key].();
		param = self.param[key];
		param_spread = i[spread_key].();
		res;
		[enabled_key, enabled].debug("BOUBOU");
		if(enabled == 1) {
			var array = self.build_spread_array_by_kind(i.voices, i.spread_kind);
			[array, param_spread, param.spec.range].debug("build_spread_array_for_param: BOUH: range");
			res = (paramval + (param_spread * param.spec.range * array));
		} {
			res = paramval ! i.voices;
		};
		res;
	},


	//////////////////////

	destructor: { arg self;
		// self.data is freed by root player
		self.static_data.keysValuesDo { arg name, datum;
			datum.destructor;
		}
	},

	make_bindings: { arg self;
		self.binding_responder = self.get_main.commands.make_binding_responder(\classinstr, 
		//self.get_main.commands.parse_action_bindings(\classinstr, 
		
			self.get_main.panels.side.get_shared_bindings ++
			self.get_main.panels.side.get_windows_bindings ++ [

			[\close_window, { 
				self.window.close;
			}],

			[\play_selected, { 
				self.get_player.play_node;
			}], 

			[\stop_selected, { 
				self.get_player.stop_node;
			}],

			[\edit_selected_param, { 
				if(~global_controller.current_param.notNil) {
					debug("class_instr: edit_selected_param");
					~class_player_display.edit_param_value(self.get_main, self.get_player, ~global_controller.current_param);
				}
			}],

			[\edit_modulator, {
				debug("classinstr: edit_modulator");
				if(~global_controller.current_param.notNil) {
					debug("classinstr: edit_modulator: current_param notnil");
					self.get_main.panels.side[\edit_modulator_callback].(self.get_player, ~global_controller.current_param);
				}
			}],

			[\change_param_kind, { 
				if(~class_player_display.param_types.param_mode.includes(~global_controller.current_param.name).not) {
					~class_param_kind_chooser.new(self.get_main, { arg sel;
						~class_player_display.change_param_kind(sel, ~global_controller.current_param);
					})
				}
			}],

			[\assign_global_midi_knob, { 
				// factorize with side and modulation
				var param = ~global_controller.current_param;
				var node;
				if(param.notNil) {
					if(param.classtype == \range) {
						node = self.parent_player_ctrl;
						self.get_main.panels.side[\binding_assign_global_midi_knob].(node, self.param_ctrl, param.target_slot);
					} {
						//node = self.get_current_player;
						self.get_main.panels.side[\binding_assign_global_midi_knob].(param.get_player, param)
					}
				}
			}],

			[\assign_midi_knob, { 
				if(~global_controller.current_param.notNil) {
					var param = ~global_controller.current_param;
					self.get_main.panels.side[\binding_assign_midi_knob].(param);
				};
			}],

			[\panic, { 
				self.get_main.panic;
			}],
		])
	},


	rel_namer: { arg self, name;
		if(self.namer.isNil) {
			name;
		} {
			self.namer.rel_name(name);
		}
	},

	abs_namer: { arg self, name;
		if(self.namer.isNil) {
			name;
		} {
			self.namer.abs_name(name)
		}
	},

	make_namer: { arg self, prefix="";
		if(self.namer.isNil) {
			~class_ci_namer.new(prefix);
		} {
			self.namer.breed(prefix)
		}
	},

	get_specs: (
		// (minval, maxval, warp, step, default, units)
		wt_pos: ControlSpec(0, 1, \lin, 0, 0),
		onoff: ControlSpec(0, 1, \lin, 1, 1),
		velocity: \unipolar.asSpec,
		ktr: \unipolar.asSpec,
		unipolarexp: ControlSpec(0.0005, 1, \exp, 0, 1),
		pitch: ControlSpec(-64,64, \lin, 0, 0, "midi"),
		rate: \widefreq.asSpec,
		bufrate: ControlSpec(-16,16, \lin, 0, 1, ""),
		glidefade: \unipolar.asSpec,
		env: ControlSpec(0, 16, 'linear', 0, 0.1, ""),
		boost: ControlSpec(-500, 100, 'lin', 0, 0),
		amp: ControlSpec(0, 1, 'amp', 0, 0.1, ""),
		preamp: ControlSpec(0, 16, 'amp', 0, 1, ""),
		wideamp: ControlSpec(0, 6, 'amp', 0, 0.1, ""),
		crush: ControlSpec(1, 31, 'lin', 0, 1, ""),
		smalldelay: ControlSpec(0, 0.02, 'lin', 0, 0.001, ""),
		envamp: ControlSpec(0, 1, 'amp', 0, 1, ""),
		sustain: ControlSpec(0.001, 4, \exp, 0, 0.25),
	),

	freeze_build_synthdef_do: { arg self, fun;
		self.freeze_build_synthdef = true;
		fun.();
		self.freeze_build_synthdef = false;
	},

	build_synthdef: { arg self, rate=\ar;
		var synthdef_name;
		//self.synthdef_name = self.synthdef_basename ++ self.synthdef_name_suffix;
		if(self.freeze_build_synthdef.not) {

			if(self.get_player.compositor.notNil) {
				self.get_player.compositor.reset_inline_synthfun_thunks;
			};

			self.synthdef_name = "%_%".format(self.synthdef_basename, self.get_player.uname);
			self.synthdef_name.debug("REBUILD SYNTH");
			rate = self.synth_rate ?? rate;
			SynthDef(self.synthdef_name, { arg out=0;
				var sig;

				sig = self.synthfun.();

				Out.performList(rate, [out, sig]);

			}).add;

			if(self.is_effect) {
				self.get_player.build_sourcepat;
				self.get_player.build_real_sourcepat;
			};
		};
	},
);

~class_ci_freq = (
	parent: ~class_instr,
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_sin;
		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		self.help_build_data(
			[
				freq: ~make_control_param.(main, player, \freq, \scalar, 210, \freq.asSpec),
			]
		);
	},


	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;

			sig = 0;

			sig = sig * EnvGen.ar(Env.adsr(0.1,0.1,1,0.1), i.gate, doneAction:i.doneAction);
			sig;

		}
	
	},
);

//////////////////////////////////////////////////////
////////////// Base Class Instrs
//////////////////////////////////////////////////////

~class_ci_osc = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_osc;
		self.build_data;

		self.simple_args = (freq:\void, gate:1, doneAction:2);
	
		self;
	},

	get_spectrum_variants: { arg self;
		[
			(
				name: "Normal",
				uname: \normal,
			),
			(
				name: "Width",
				uname: \width,
			),
			(
				name: "Phase",
				uname: \phase,
			),
			(
				name: "Bend",
				uname: \bend,
			),
			(
				name: "Formant",
				uname: \formant,
			),
			(
				name: "Clip",
				uname: \clip,
			),
			(
				name: "Wrap",
				uname: \wrap,
			),
			(
				name: "Fold",
				uname: \fold,
			),
			(
				name: "Tanh",
				uname: \tanh,
			),
			(
				name: "Distort",
				uname: \distort,
			),
		]
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		var wt, wt_range, wt_classic, wt_pos;
		self.synthdef_name.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%% build data");
		wt_pos = ~make_control_param.(main, player, \wt_pos, \scalar, 0, \unipolar.asSpec.copy);
		wt = ~class_param_wavetable_controller.new(player, \wt, wt_pos);
		wt_range = wt.get_wt_range_controller;
		wt_classic = wt.get_wt_classic_controller;
		
		self.help_build_data(
			[
				//freq: ~make_control_param.(main, player, \freq, \scalar, 201, \freq.asSpec),
				detune: ~make_control_param.(main, player, \detune, \scalar, 0, specs[\pitch]),
				wt: wt,
				wt_pos: wt_pos,
				intensity: ~make_control_param.(main, player, \intensity, \scalar, 0, \unipolar.asSpec),
				oscamp: ~make_control_param.(main, player, \oscamp, \scalar, 0.5, \amp.asSpec),
			],
			(
				spectrum: ~class_param_kind_chooser_controller.new(\spectrum, self.get_spectrum_variants),
				wt_range: wt_range,
				wt_classic: wt_classic,
				enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
			)
		);
	},

	make_layout: { arg self;
		var knobs = [\detune, \wt_pos, \intensity, \oscamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		self.module_loader.debug("MODLO ====================");
		frame_view = ~class_ci_frame_view.new("Osc1", 
			self.knobs, self.param[\enabled], self.param[\wt], self.param[\spectrum], nil, nil, self.module_loader
		);
		self.layout = frame_view.layout;
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			i.debug("III");
			//args.debug("ARGS");
			//i.freq.debug("FREQ");
			//i.freq.poll(label:"osc_freq");
			//\freq.kr.poll(label:"osc_ barefreq");

			sig = Instr(\ci_oscillator).value((
				midinote:i.freq.cpsmidi, 
				detune:i.detune,
				wt:i.wt,
				wt_position:i.wt_pos,
				wt_range:i.wt_range,
				wt_classic:i.wt_classic,
				spectrum:i.spectrum,
				intensity:i.intensity,
				amp:i.oscamp, 
			));
			sig = self.bypass(i.enabled, sig, DC.ar(0));
			i.oscamp.debug("OSCAMP");


			sig;

		}
	
	},
);

~class_ci_oscfader = (
	parent: ~class_instr,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_oscfader;

		self.osc = ~class_ci_osc.new(main, player, self.make_namer);

		self.build_data;
	
		self;
	},

	get_spectrum_variants: { arg self;
		[
			(
				name: "Normal",
				uname: \normal,
			),
			(
				name: "Bend",
				uname: \bend,
			),
			(
				name: "Formant",
				uname: \formant,
			),
			(
				name: "Clip",
				uname: \clip,
			),
			(
				name: "Wrap",
				uname: \wrap,
			),
			(
				name: "Fold",
				uname: \fold,
			)
		]
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var wt, wt_range, wt_pos;
		
		self.help_build_data(
			[
				outmix: ~make_control_param.(main, player, \outmix, \scalar, 0.5, \unipolar.asSpec),
			],
			nil,
			[
				self.osc
			]
		)
	},

	make_layout: { arg self;
		var knobs = [\detune, \wt_pos, \intensity, \oscamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			var rel_name = self.osc.rel_namer(name);
			rel_name.debug("RELNAME");
			self.osc.param[name];
		};
		frame_view = ~class_ci_frame_view.new("Osc1", self.knobs, self.osc.param[\enabled], self.osc.param[\wt], self.osc.param[\spectrum], self.param[\outmix]);
		self.layout = frame_view.layout;
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig1, sig2, sig;

			sig = self.osc.synthfun.(args);
			sig1 = SelectX.ar(i.outmix, [sig, DC.ar(0)]);
			sig2 = SelectX.ar(i.outmix, [DC.ar(0), sig]);
			//sig1 = sig;
			//sig2 = sig;

			[sig1, sig2];
			//[sig, sig];
			//sig

		}
	
	},
);

~class_ci_wrapfader = (
	parent: ~class_instr,
	new: { arg self, main, player, namer, input_class;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_oscfader;

		self.osc = input_class.new(main, player, self.make_namer);

		self.build_data;
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		
		self.help_build_data(
			[
				outmix: ~make_control_param.(main, player, \outmix, \scalar, 0.5, \unipolar.asSpec),
			],
			nil,
			[ self.osc ]
		)
	},

	make_layout: { arg self;
		self.layout = self.osc.make_layout(self.param[\outmix]);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig1, sig2, sig;

			sig = self.osc.synthfun.(args);
			sig1 = SelectX.ar(i.outmix, [sig, DC.ar(0)]);
			sig2 = SelectX.ar(i.outmix, [DC.ar(0), sig]);

			[sig1, sig2];

		}
	
	},
);

~class_ci_filter = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_filter;

		self.simple_args = (freq:\void, gate:1, doneAction:2);

		self.build_data;
	
		self;
	},

	get_variants: { arg self;
		var specs = self.get_specs;
		[
			(
				name: "LPF",
				uname: \lpf,
				args: ["Cutoff"],
				specs: [specs[\pitch]]
			),
			(
				name: "RLPF",
				uname: \rlpf,
				args: ["Cutoff", "RQ"],
				specs: [specs[\pitch], \rq]
			),
			(
				name: "HPF",
				uname: \hpf,
				args: ["Cutoff"],
				specs: [specs[\pitch]]
			),
			(
				name: "RHPF",
				uname: \rhpf,
				args: ["Cutoff", "RQ"],
				specs: [specs[\pitch], \rq]
			),
			(
				name: "BPF",
				uname: \bpf,
				args: ["Cutoff", "RQ"],
				specs: [specs[\pitch], \rq]
			),
			(
				name: "LP DM1",
				uname: \lpdm1,
				args: ["Cutoff", "Resonance", "Noise"],
				specs: [specs[\pitch], \unipolar, specs[\smalldelay]]
			),
			(
				name: "HP DM1",
				uname: \hpdm1,
				args: ["Cutoff", "Resonance", "Noise"],
				specs: [specs[\pitch], \unipolar, specs[\smalldelay]]
			),
			(
				name: "MoogLader",
				uname: \mooglader,
				args: ["Cutoff", "Resonance"],
				specs: [specs[\pitch], \unipolar]
			),
			(
				name: "MoogFF",
				uname: \moogff,
				args: ["Cutoff", "Gain"],
				specs: [specs[\pitch], ControlSpec(0,4,\lin,0,2)]
			),
			(
				name: "Ramp",
				uname: \ramp,
				args: ["Lag"],
				specs: [specs[\delay]]
			),
			(
				name: "Comb",
				uname: \comb,
				args: ["Max Delay", "Delay", "Decay"],
				specs: [\delay, \delay, \decay]
			),
		]
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		var sc;
		var filterkind;

		filterkind = ~class_param_kind_chooser_controller.new(\filterkind, self.get_variants);
		sc = SimpleController.new(filterkind);
		sc.put(\val, {
			var idx = filterkind.get_val;
			var variant = self.get_variants[idx];
			variant.args.do { arg name, i;
				//[self.data.keys, "arg%".format(i+1).asSymbol].debug("RESPONDER");
				self.param["arg%".format(i+1).asSymbol].set_label(name);
				self.param["arg%".format(i+1).asSymbol].set_spec(variant.specs[i].asSpec.copy);
			};
		});

		self.help_build_data(
			[
				arg1: ~make_control_param.(main, player, \arg1, \scalar, 60, \midinote.asSpec.copy),
				arg2: ~make_control_param.(main, player, \arg2, \scalar, 0.5, \unipolar.asSpec.copy),
				arg3: ~make_control_param.(main, player, \arg3, \scalar, 0, \unipolar.asSpec.copy),
				filteramp: ~make_control_param.(main, player, \filteramp, \scalar, 0.8, \unipolar.asSpec.copy),
			],
			(
				filterkind: filterkind,
				enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
			)
		);

		filterkind.changed(\val);
	},

	make_layout: { arg self;
		var knobs = [\arg1, \arg2, \arg3];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		self.module_loader.debug("MODLO ====================");
		frame_view = ~class_ci_frame_view.new("Filter", 
			self.knobs, self.param[\enabled], self.param[\filterkind], nil, self.param[\filteramp], nil, self.module_loader
		);
		self.layout = frame_view.layout;
		self.layout;
	},

	synthfun: { arg self;
		{ arg in, args;
			var i = self.get_synthargs(args);
			var sig;

			sig = Instr(\ci_filter).value((
				in:in,
				kind:i.filterkind,
				arg1:i.arg1,
				arg2:i.arg2,
				arg3:i.arg3,
				freq:i.freq,
			));

			sig = self.bypass(i.enabled, sig, in);
			sig = sig * i.filteramp;

			sig;

		}
	
	},
);

~class_ci_insertfx = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_insertfx;
		self.build_data;

		self.simple_args = (freq:\void, gate:1, doneAction:2);
	
		self;
	},

	get_variants: { arg self;
		var specs = self.get_specs;
		[
			(
				name: "Freqshift",
				uname: \freqshift,
				args: ["Wet/Dry", "Shift"],
				specs: [\unipolar, specs[\pitch]]
			),
			(
				name: "Delay",
				uname: \simpledelay,
				args: ["Wet/Dry", "Delay"],
				specs: [\unipolar, \delay]
			),
			(
				name: "Hold",
				uname: \samplehold,
				args: ["Wet/Dry", "Pitch"],
				specs: [\unipolar, specs[\pitch]]
			),
			(
				name: "Bitcrusher",
				uname: \bitcrusher,
				args: ["Wet/Dry", "Crush"],
				specs: [\unipolar, specs[\crush]]
			),
			(
				name: "Decimator",
				uname: \decimator,
				args: ["Wet/Dry", "Samplerate", "Bit depth"],
				specs: [\unipolar, specs[\unipolarexp], specs[\crush]],
			),
			(
				name: "Smooth Decimator",
				uname: \smoothdecimator,
				args: ["Wet/Dry", "Samplerate", "Smoothing"],
				specs: [\unipolar, specs[\unipolarexp], \unipolar],
			),
			(
				name: "Filter",
				uname: \simplefilter,
				args: ["HP freq", "LP freq"],
				specs: [specs[\pitch], specs[\pitch]]
			),
			(
				name: "SineShaper",
				uname: \sinshaper,
				args: ["Wet/Dry", "Drive"],
				specs: [\unipolar, \unipolar]
			),
			(
				name: "ParaShaper",
				uname: \parashaper,
				args: ["Wet/Dry", "Drive"],
				specs: [\unipolar, \unipolar]
			),
			(
				name: "Hard clipper",
				uname: \hardclipper,
				args: ["Wet/Dry", "Drive"],
				specs: [\unipolar, \unipolar]
			),
			(
				name: "Tanh",
				uname: \tanh,
				args: ["Wet/Dry", "Preamp", "Postamp"],
				specs: [\unipolar, specs[\preamp], specs[\preamp]]
			),
			(
				name: "Distort",
				uname: \distort,
				args: ["Wet/Dry", "Preamp", "Postamp"],
				specs: [\unipolar, specs[\preamp], specs[\preamp]]
			),
		]
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var wt, wt_range, wt_pos;
		var specs = self.get_specs;
		var sc;
		var insertfxkind;
		self.synthdef_name.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%% build data");

		insertfxkind = ~class_param_kind_chooser_controller.new(\insertfxkind, self.get_variants);
		sc = SimpleController.new(insertfxkind);
		sc.put(\val, {
			var idx = insertfxkind.get_val;
			var variant = self.get_variants[idx];
			variant.args.do { arg name, i;
				[self.param.keys, "arg%".format(i+1).asSymbol].debug("RESPONDER");
				self.param["arg%".format(i+1).asSymbol].set_label(name);
				self.param["arg%".format(i+1).asSymbol].set_spec(variant.specs[i].asSpec.copy);
			};
		});

		self.help_build_data(
			[
				arg1: ~make_control_param.(main, player, \arg1, \scalar, 60, \midinote.asSpec.copy),
				arg2: ~make_control_param.(main, player, \arg2, \scalar, 0.5, \unipolar.asSpec.copy),
				arg3: ~make_control_param.(main, player, \arg3, \scalar, 0, \unipolar.asSpec.copy),
			],
			(
				insertfxkind: insertfxkind,
				enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 0),
			)
		);

		insertfxkind.changed(\val);
	},

	make_layout: { arg self;
		var knobs = [\arg1, \arg2, \arg3];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		frame_view = ~class_ci_frame_view.new("InsertFx", self.knobs, self.param[\enabled], self.param[\insertfxkind]);
		self.layout = frame_view.layout;
		//self.layout = HLayout();
		self.layout;
	},

	synthfun: { arg self;
		{ arg in, args;
			var i = self.get_synthargs(args);
			var sig;

			sig = Instr(\ci_insertfx).value((
				in:in,
				kind:i.insertfxkind,
				arg1:i.arg1,
				arg2:i.arg2,
				arg3:i.arg3,
				ktr:i.freq.cpsmidi,
			));
			i.enabled.debug("ENABLED");
			sig = self.bypass(i.enabled, sig, in);

			//sig = sig * EnvGen.ar(Env.adsr(0.1,0.1,1,0.1), i.gate, doneAction:i.doneAction);
			sig;

		}
	
	},
);

~class_ci_dadsr = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.namer = { namer };
		self.get_player = { player };
		self.synthdef_name = \ci_dadsr;
		self.build_data;

		self.simple_args = (gate:1, doneAction:2, freq:\void, velocity:\void);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var specs = self.get_specs;
		var player = self.get_player;
		self.help_build_data(
			[
				delay: ~make_control_param.(main, player, \delay, \scalar, 0, specs[\env]),
				attack_time: ~make_control_param.(main, player, \attack_time, \scalar, 0.1, specs[\env]),
				decay_time: ~make_control_param.(main, player, \decay_time, \scalar, 0.1, specs[\env]),
				sustain_level: ~make_control_param.(main, player, \sustain_level, \scalar, 0.5, specs[\envamp]),
				release_time: ~make_control_param.(main, player, \release_time, \scalar, 0.1, specs[\env]),
				curve: ~make_control_param.(main, player, \curve, \scalar, 1, ControlSpec(-10,10,\lin,0,1)),
				velocity_mix: ~make_control_param.(main, player, \velocity_mix, \scalar, 0.1, \unipolar.asSpec),
				ampcomp: ~make_control_param.(main, player, \ampcomp, \scalar, 0.1, \unipolar.asSpec),
			]
		)
	},

	make_layout: { arg self;
		var knobs = [\delay, \attack_time, \decay_time, \sustain_level, \release_time, \curve];
		var faders = [\ampcomp, \velocity_mix];
		var frame_view;
		var env_view;
		var layout;
		var knobs_layouts;
		knobs_layouts = knobs.collect { arg name;
			~class_ci_modknob_view.new(self.param[name]).layout;
			//ModKnob.new.asView;
		};
		self.faders = faders.collect { arg name;
			~class_ci_modslider_view.new(self.param[name], Rect(0,0,30,100)).layout;
		};
		//self.layout = VLayout(
		layout = VLayout(
			HLayout(*
				self.faders ++
				[
					env_view = EnvelopeView.new; 0.01.wait; env_view
				]
			),
			HLayout(*
				knobs_layouts
				//[HLayout.new]
			)
		);
		knobs.do { arg name;
			~make_view_responder.(env_view, self.param[name], (
				val: {
					env_view.setEnv( Env.dadsr(
						self.param[\delay].get_val,
						self.param[\attack_time].get_val,
						self.param[\decay_time].get_val,
						self.param[\sustain_level].get_val,
						self.param[\release_time].get_val,
						1,
						self.param[\curve].get_val
					) )
				}
			), true)
		};
		//self.layout;
		layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			var ampcomp, velcomp;

			ampcomp = (AmpCompA.kr(i.freq) * i.ampcomp) + (1 * (1-i.ampcomp));
			//ampcomp = (AmpCompA.kr(100) * i.ampcomp) + (1 * (1-i.ampcomp));
			//velcomp = (1 * i.velocity_mix) + (1 * (1 - i.velocity_mix));
			velcomp = (i.velocity * i.velocity_mix) + (1 * (1 - i.velocity_mix));
			//velcomp.poll;

			sig = EnvGen.ar(Env.dadsr(
				i.delay,
				i.attack_time,
				i.decay_time,
				i.sustain_level,
				i.release_time,
				1,
				i.curve
			), i.gate, doneAction:i.doneAction);
			sig = sig * ampcomp * velcomp;
			sig;

		}
	
	},
);

~class_ci_dadsr_operator = (
	parent: ~class_ci_dadsr,

	make_layout: { arg self;
		var knobs = [\velocity_mix, \delay, \attack_time, \decay_time, \sustain_level, \release_time, \curve];
		var labels = ["vel", "del", "A", "D", "S", "R", "cve"];
		var frame_view;
		var env_view;
		var layout;
		var knobs_layouts;
		knobs_layouts = knobs.collect { arg name;
			~class_ci_simpleknob_view.new(self.param[name]).layout;
		};

		knobs.do { arg knob, i;
			self.param[knob].set_label(labels[i])
		};

		

		layout = 
			HLayout(*
				[env_view = EnvelopeView.new] ++
				knobs_layouts
			);
		knobs.do { arg name;
			~make_view_responder.(env_view, self.param[name], (
				val: {
					env_view.setEnv( Env.dadsr(
						self.param[\delay].get_val,
						self.param[\attack_time].get_val,
						self.param[\decay_time].get_val,
						self.param[\sustain_level].get_val,
						self.param[\release_time].get_val,
						1,
						self.param[\curve].get_val
					) )
				}
			), true)
		};
		layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			args.debug("SYY args");
			i.doneAction.debug("SYY donac");

			//i.doneAction.poll;

			sig = EnvGen.ar(Env.dadsr(
				i.delay,
				i.attack_time,
				i.decay_time,
				i.sustain_level,
				i.release_time,
				1,
				i.curve
			), i.gate, doneAction:i.doneAction);
			sig;

		}
	
	},

);

~class_ci_dadsr_kr = (
	parent: ~class_ci_dadsr,
	synth_rate: \kr,
);

~class_ci_master_env = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_master_env;
		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		self.help_build_data(
			[
				pan: ~make_control_param.(main, player, \pan, \scalar, 0, \pan.asSpec),
				spread: ~make_control_param.(main, player, \spread, \scalar, 1, \unipolar.asSpec),
				amp: ~make_control_param.(main, player, \amp, \scalar, 0.1, \amp.asSpec),
			]

		)
	},

	make_layout: { arg self;
		var knobs = [\pan, \spread, \amp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		frame_view = ~class_ci_frame_view.new("Master", self.knobs, nil, nil, nil);
		self.layout = frame_view.layout;
		self.layout;
	},

	synthfun: { arg self;
		{ arg in, args;
			var i = self.get_synthargs(args);
			var is = self.get_staticargs;
			var sig;

			sig = Splay.ar(in, i.spread, i.amp, i.pan);
			sig = sig * EnvGen.ar(Env.adsr(0.1,0.1,1,0.1), i.gate, doneAction:i.doneAction);
			sig;

		}
	
	},
);

~class_ci_master_dadsr = (
	// FIXME: add a namer
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player, showspread=false;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_master_dadsr;
		self.showspread = showspread;

		self.dadsr = ~class_ci_dadsr.new(main,player);
		self.simple_args = (gate:1, doneAction:2);

		self.build_data;

	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		self.help_build_data(
			[
				pan: ~make_control_param.(main, player, \pan, \scalar, 0, \pan.asSpec),
				spread: ~make_control_param.(main, player, \spread, \scalar, 0, \unipolar.asSpec),
				amp: ~make_control_param.(main, player, \amp, \scalar, 0.1, \amp.asSpec),
			],
			nil,
			[
				self.dadsr;
			]
		)
	},

	make_layout: { arg self;
		var knobs = [\pan, \amp];
		var frame_view;
		if(self.showspread) {
			knobs = knobs.add(\spread);
		};
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		frame_view = ~class_ci_frame_view.new("Master", self.knobs, nil, nil, nil);
		self.layout = frame_view.layout;
		self.layout;
	},

	make_layout_env: { arg self;
		self.dadsr.make_layout;
	},

	synthfun: { arg self;
		{ arg in, args;
			var i = self.get_synthargs(args);
			var sig;

			sig = Splay.ar(in, i.spread, i.amp, i.pan);
			sig = sig * self.dadsr.synthfun.();
			sig;

		}
	
	},
);

~class_ci_ienv_matrix = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player, ienv_controls;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_ienv_matrix;

		self.ienv_presets = ~class_param_ienv_presets_controller.new;
		self.ienv_presets.add_preset(\user1, [[64,64,64,64], [1,2,3]/3*128]);
		self.ienv_presets.add_preset(\user2, [[64,64,64,64], [1,2,3]/3*128]);

		self.build_data;

	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;

		// TODO: finish it

		//self.help_build_data(
		//		[
		//			pan: ~make_control_param.(main, player, \pan, \scalar, 0, \pan.asSpec),
		//			spread: ~make_control_param.(main, player, \spread, \scalar, 0, \unipolar.asSpec),
		//			amp: ~make_control_param.(main, player, \amp, \scalar, 0.1, \amp.asSpec),
		//		],
		//		self.dadsr;
		//	],
		//	[
		//		// FIXME: should be a dict, no ?
		//		self.ienv_controls.collect { arg name;
		//			[
		//				name.asSymbol,
		//				{ var a; a = ~class_param_ienv_proxy_controller.new(self.ienv_presets.get_preset(\off)); a.set_curve(self.ienv_presets.get_preset(\off)); a }.value,
		//			]
		//		
		//		}.flat
		//	]
		//)
	},

	make_layout: { arg self;
		var knobs = [\pan, \amp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		frame_view = ~class_ci_frame_view.new("Master", self.knobs, nil, nil, nil);
		self.layout = frame_view.layout;
		self.layout;
	},

	make_layout_env: { arg self;
		self.dadsr.make_layout;
	},

	synthfun: { arg self;
		{ arg in, args;
			var i = self.get_synthargs(args);
			var is = self.get_staticargs;
			var sig;

			sig = Splay.ar(in, i.spread, i.amp, i.pan);
			sig = sig * self.dadsr.synthfun.();
			sig;

		}
	
	},
);

~class_ci_noise = (
	parent: ~class_instr,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	get_variants: { arg self;
		[
			(
				name: "White",
				uname: \white,
			),
			(
				name: "Brown",
				uname: \brown,
			),
			(
				name: "Gray",
				uname: \gray,
			),
			(
				name: "Clip",
				uname: \clip,
			),
			(
				name: "Pink",
				uname: \pink,
			),
		]
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		self.help_build_data(
			[
				amp: ~make_control_param.(main, player, \amp, \scalar, 0.1, \amp.asSpec),
			],
			(
				kind: ~class_param_kind_chooser_controller.new(\kind, self.get_variants),
				enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 0),
			)
		)
	},

	make_layout: { arg self, fader;
		var knobs = [\amp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		frame_view = ~class_ci_frame_view.new("Noise", self.knobs, self.param[\enabled], self.param[\kind], nil, fader);
		self.layout = frame_view.layout;
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;

			sig = Instr(\ci_noise).value((
				kind: i.kind,
				amp: i.amp,
			));
			sig = self.bypass(i.enabled, sig, DC.ar(0));
			sig;

		}
	
	},
);

~class_ci_operator = (
	parent: ~class_instr,
	new: { arg self, main, player, namer, name="Op A";
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.name = name;

		//self.dadsr = ~class_ci_dadsr_operator.new(main, player, self.make_namer);
		self.dadsr = ~class_ci_composite_env.new(main, player, self.make_namer);
		//self.dadsr = ~class_ci_dadsr.new(main, player);

		self.build_data;
		self.simple_args = (freq:\void, gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		var wt, wt_range, wt_classic;
		wt =  ~class_param_wavetable_controller.new(player, \wt, nil, true);
		wt_range = wt.get_wt_range_controller;
		wt_classic = wt.get_wt_classic_controller;
		debug("~class_ci_operator: build_data");
		self.help_build_data(
			self.make_control_params([
				[\amp, \amp, 0.5],
				[\pan, \pan, 0.0],
				//[\velocity, \unipolar, 0.0],
				[\ratio, ControlSpec(0.0000001, 20, \exp, 0, 1), 1],
				[\offset, ControlSpec(0.0000001, 18000, \exp, 0, 1), 0],
				[\phase, ControlSpec(0, 2, \lin, 0, 1), 0],
			]) ++ [
				wt: wt
			],
			(
				enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
				wt_classic: wt_classic,
				wt_range: wt_range,
			),
			[
				self.dadsr
			],
		)
	},

	gui_knobs: [\amp, \pan, \ratio, \offset, \phase],

	make_layout: { arg self, fader;
		var knobs;
		var frame_view;
		var frame;
		var display = (knobsize:30@30);
		knobs = self.gui_knobs;
		self.knobs = knobs.collect { arg name;
			//~class_ci_simpleknob_view.new(self.param[name]).layout;
			~class_ci_modknob_view.new(self.param[name], display).layout;
		};
		frame = View.new;
		frame.background = Color.gray(0.5);
		frame.layout = HLayout(
			VLayout(*
				[
					HLayout(
						StaticText.new.string_(self.name),
						~class_ci_popup_view.new(self.param[\wt]).layout
					),
					HLayout(*self.knobs)
				].flatten,
			),
			self.dadsr.make_layout
		);
		
		self.layout = frame;
		self.layout;
	},

	synthfun: { arg self;
		{ arg in=0, args;
			var i = self.get_synthargs(args);
			var sig;
			var freq;
			"class_ci_operator: synthfun".debug;
			
			freq = (i.freq * i.ratio + i.offset);
			freq = freq * (1 + in);

			//sig = SinOsc.ar(freq * (1 + in));
			sig = Instr(\ci_oscillator).value((
				midinote:freq.cpsmidi, 
				detune:0,
				wt:i.wt,
				wt_position:0,
				wt_range:0,
				wt_classic:i.wt_classic,
				phase:i.phase,
				spectrum:\normal,
				intensity:0,
				amp:i.amp, 
			));
			sig = sig * self.dadsr.synthfun.((doneAction:0));
			//sig = sig * i.amp;
			//sig.poll;
			sig = self.bypass(i.enabled, sig, DC.ar(0));
			sig;

		}
	
	},

);

~class_ci_custom_env = (
	parent: ~class_instr,
	synth_rate: \kr,
	new: { arg self, main, player, namer, name="custom env", display;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.name = name;
		self.display = display ?? ~class_track_display.new;

		self.build_data;
		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		var wt, wt_range, wt_classic;
		var env = ~class_param_custom_env_controller.new(\env);

		self.custom_env_controller = ~class_custom_env_track_controller.new(self.get_player, env, self.display);

		self.help_build_data(
			[
				env: env
			]
		)
	},

	make_layout: { arg self, fader;
		var cenv_view;
		cenv_view = self.custom_env_controller.make_gui;
		self.layout = VLayout(
			cenv_view;
		);
		
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			var numlevels = 16;

			sig = EnvGen.ar(i.env, i.gate, doneAction:i.doneAction);

			sig;

		}
	
	},

);

~class_ci_composite_env = (

	parent: ~class_instr,
	new: { arg self, main, player, namer, name="custom env", display;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.name = name;
		self.display = display ?? ~class_track_display.new;

		self.custom_env = ~class_ci_custom_env.new(main, player, self.make_namer);
		self.dadsr = ~class_ci_dadsr_operator.new(main, player, self.make_namer("dadsr_"));

		self.build_data;
		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	get_variants: { arg self;
		var specs = self.get_specs;
		[
			(
				name: "DADSR",
				uname: \dadsr,
			),
			(
				name: "Constant",
				uname: \const,
			),
			(
				name: "Custom",
				uname: \custom_env,
			),
		]
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		var wt, wt_range, wt_classic;
		var env_kind = ~class_param_kind_chooser_controller.new(\env_kind, self.get_variants);

		self.help_build_data(
			[],
			(
				env_kind: env_kind
			),
			[
				self.dadsr,
				self.custom_env,
			],
		)
	},

	make_layout: { arg self;
		var layout;
		var menu;
		var dadsr_view = View.new;
		var custom_env_view = View.new;
		var env_view_layout = StackLayout.new;
		var const_layout = StaticText.new.string_("constant:1");
		menu = ~class_ci_popup_view.new(self.param[\env_kind], nil, { arg popup;
			env_view_layout.index = self.param[\env_kind].get_val;
		});
		//menu.action; 
		layout = VLayout(
			menu.layout,
			env_view_layout,
		);
		dadsr_view.layout = self.dadsr.make_layout;
		custom_env_view.layout = self.custom_env.make_layout;
		env_view_layout.add(dadsr_view);
		env_view_layout.add(const_layout);
		env_view_layout.add(custom_env_view);
		menu.action; // FIXME: should be called when refreshing gui, no ?
		self.layout = layout;
		layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;

			i.doneAction.debug("==============================================DONEACTION");
			//i.doneAction.poll;

			sig = switch(i.env_kind,
				\dadsr, {
					self.dadsr.synthfun.(args)
				},
				\const, {
					1
				},
				\custom_env, {
					self.custom_env.synthfun.(args)
				},
				{
					debug("class_ci_composite_env: menu marche pas");
					self.dadsr.synthfun.(args)
				},
			);
			//sig.poll;

			sig;

		}
	
	},
);

~class_ci_bufosc = (
	parent: ~class_instr,
	synth_rate: \ar,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_;
		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;

		self.help_build_data(
			self.make_control_params([
				[\bufpos, \unipolar, 0],
				[\finepos, ControlSpec(-10,10,\lin, 0, 1), 0],
				[\range, ControlSpec(-100,100,\lin, 0, 1), 100],
			]) ++ [
				bufnum: ~make_buf_param.(\bufnum, "sounds/default.wav", player, \bufnum.asSpec),
				samplekit: ~make_samplekit_param.(\samplekit),
				sampleline: ~make_sampleline_param.(\sampleline),
			],
			(
				enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
			),
			[
				//self.insertfxs,
			],
		);
	},

	make_layout: { arg self;
		var knobs = [\bufpos, \finepos, \range];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		frame_view = ~class_ci_frame_view.new("Bufosc", self.knobs, self.param[\enabled], nil, nil);
		self.layout = HLayout(frame_view.layout);
		self.layout;
	},

	synthfun: { arg self;
		{ arg in, args;
			var i = self.get_synthargs(args);
			var sig = 0;
			var bufnum = i.bufnum;
			var bufsig;
			var osc, phase;
			
			osc = in;
			//osc = osc.sum;
			phase = osc * i.range + (i.bufpos * BufFrames.ir(bufnum)) + i.finepos;

			debug("ca suffi bordel");
			bufsig = BufRd.ar(2, bufnum, phase, 1);
			bufsig.debug("ca suffi bordel1");
			sig = bufsig.collect { arg chan;
				chan.sum;
			};
			sig.debug("ca suffi bordel2");
			sig;
		}
	},

);

~class_ci_sampler = (
	parent: ~class_instr,
	synth_rate: \ar,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_;
		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;

		self.help_build_data(
			self.make_control_params([
				[\bufpos, \unipolar, 0],
				//[\bufrate, ControlSpec(-16,-16, \lin, 0, 1, ""), 1],
				[\bufrate, specs[\bufrate], 1],
				[\bufloop, specs[\onoff], 0],
				[\bufamp, specs[\wideamp], 0.5],
			]) ++ [
				bufnum: ~make_buf_param.(\bufnum, "sounds/default.wav", player, \bufnum.asSpec),
				samplekit: ~make_samplekit_param.(\samplekit),
				sampleline: ~make_sampleline_param.(\sampleline),
			],
			(
				enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
			),
			[
				//self.insertfxs,
			],
		);
	},

	make_layout: { arg self, fader;
		var knobs = [\bufrate, \bufpos, \bufamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		frame_view = ~class_ci_frame_view.new("Sampler", self.knobs, self.param[\enabled], nil, nil, fader);
		self.layout = HLayout(frame_view.layout);
		self.layout;
	},

	synthfun: { arg self;
		{ arg in, args;
			var i = self.get_synthargs(args);
			var sig = 0;
			var bufnum = i.bufnum;
			var bufsig;
			var osc, phase;
			
			debug("ca suffi bordel");
			//bufsig = PlayBuf.ar(2, bufnum, i.rate * BufRateScale.kr(bufnum), 1, i.bufpos * BufFrames.kr(bufnum), i.loop);
			bufsig = PlayBuf.ar(2, bufnum, i.bufrate * BufRateScale.kr(bufnum), 1, i.bufpos * BufFrames.kr(bufnum), i.bufloop);
			//bufsig = PlayBuf.ar(2, bufnum, 1 * BufRateScale.kr(bufnum), 1, 0 * BufFrames.kr(bufnum), 0);
			//bufsig = SinOsc.ar(200);
			bufsig.debug("ca suffi bordel1");
			//sig = bufsig.collect { arg chan;
			//	chan.sum;
			//};
			sig = bufsig;
			sig = self.bypass(i.enabled, sig, DC.ar(0));
			sig.debug("ca suffi bordel2");
			sig;
		}
	},

);

//////////////////////////////////////////////////////
////////////// Inline Class Instrs
//////////////////////////////////////////////////////

~class_ci_inline_base = (
	parent: ~class_instr,
	synth_rate: \ar,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_inline_base;
		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	refresh: { arg self;
		self.changed(\inline_node);
	},

	free_inlinde_node: { arg self;
		if(self.inline_node.notNil) {
			// module data are freed because they are in self.data since rebuild_arg_list
			self.inline_node.destructor;
			self.inline_node.data.keysValuesDo { arg name, datum;
				datum.destructor;
			};
		}
	},

	make_inline_node: { arg self, name, update=true;
		debug("class_param_inlinefx_controller: make_inline_node");
		self.free_inlinde_node;
		if(name == \empty) {
			self.local_modules = [];
			self.inline_node = nil;
		} {
			self.inline_node = self.get_main.node_manager.make_inline_node(name, self.get_player);
			self.local_modules = [self.inline_node];
		};
		if(update == true) {
			self.get_player.rebuild_arg_list;
		};
		self.changed(\inline_node);
	},

	get_inline_node: { arg self;
		self.inline_node
	},

	make_layout: { arg self;
		var frame_view;
		frame_view = ~class_inline_container_view.new(self);
		self.layout = HLayout(frame_view.layout);
		self.layout;
	},

	inline_synthfun: { arg self;
		// TO BE OVERLOADED
	},

	load_inline_node: { arg self;
		// TO BE OVERLOADED
	},

	build_data: { arg self;
		// TO BE OVERLOADED
	},

	synthfun: { arg self;
		// TO BE OVERLOADED
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;

			sig = LFSaw.ar(i.freq);
			sig = self.inline_synthfun.(sig, args);
			sig = sig * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),i.gate,doneAction:i.doneAction);
			sig = Pan2.ar(sig, 0, i.amp);
			sig;
		}
	},

);

~class_ci_inlinefx = (
	parent: ~class_ci_inline_base,
	synth_rate: \ar,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_inlinefx;
		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	load_inline_node: { arg self;
		debug("class_param_inlinefx_controller: load_inline_node");
		self.get_main.node_manager.load_inlinefx_node({ arg name;
			self.make_inline_node(name);
		})
	},

	inline_synthfun: { arg self;
		if(self.inline_node.isNil) {
			{ arg in; in; }
		} {
			self.inline_node.synthfun;
		}
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		var inlinefx;

		//self.make_inline_node("ci empty_inlinefx_node", false);

		//self.help_build_data(
		//	self.make_control_params([
		//		[\mix, \unipolar, 0.5],
		//		[\amp, \amp, 0.1],
		//	]),
		//	nil,
		//);
	},

	synthfun: { arg self;
		{ arg in, args;
			var i = self.get_synthargs(args);
			var sig;
			
			sig = self.inline_synthfun.(sig, args);
			sig;
		}
	},

);

~class_ci_inlinegen = (
	parent: ~class_ci_inline_base,
	synth_rate: \ar,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_inlinegen;
		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	load_inline_node: { arg self;
		debug("class_param_inlinefx_controller: load_inline_node");
		self.get_main.node_manager.load_inlinegen_node({ arg name;
			self.make_inline_node(name);
		})
	},

	inline_synthfun: { arg self;
		if(self.inline_node.isNil) {
			//FIXME: use synth_rate rate ?
			{ arg args; DC.ar(0); }
		} {
			self.inline_node.synthfun;
		}
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		var inlinefx;

		//self.make_inline_node("ci empty_inlinefx_node", false);

		//self.help_build_data(
		//	self.make_control_params([
		//		[\mix, \unipolar, 0.5],
		//		[\amp, \amp, 0.1],
		//	]),
		//	nil,
		//);
	},

	make_layout: { arg self;
		var frame_view;
		//frame_view = ~class_inline_frame_view.new(self);
		frame_view = ~class_inline_container_view.new(self);
		self.layout = HLayout(frame_view.layout);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			
			sig = self.inline_synthfun.(args);
			sig;
		}
	},
);

//////////////////////////////////////////////////////
////////////// Extensions Class Instrs
//////////////////////////////////////////////////////


~class_ci_internal_modulator = (
	parent: ~class_instr,
	synth_rate: \ar,
	new: { arg self, main, player, namer, oscs;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.oscs = { oscs };
		self.synthdef_name = \ci_internal_modulator;

		self.modulation_keys = [\ring, \phase,\wt_pos];

		self.build_data;
		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		debug("class_ci_internal_modulator.build_data");

		self.enabled_ctrl = IdentityDictionary.new;
		self.enabled_static_data = IdentityDictionary.new;
		self.mod_ctrl = IdentityDictionary.new;
		self.mod_ordererd_args = List.new;

		self.modulation_keys.collect { arg key;
			var name;
			self.enabled_ctrl[key] = self.oscs.collect {  arg osc, idx;
				var param;
				var name = "enabled_mod_%_osc_%".format(key, idx).asSymbol;
				param = ~class_param_static_controller.new(name, specs[\onoff], 0);
				self.enabled_static_data[ name ] = param;
				param;
			};
			name = "intmod_%".format(key).asSymbol;
			self.mod_ctrl[key] = ~make_control_param.(main, player, name, \scalar, 0, \unipolar.asSpec);
			self.mod_ordererd_args.add(name);
			self.mod_ordererd_args.add(self.mod_ctrl[key]);
			
		};

		self.help_build_data(
			self.mod_ordererd_args ++
			self.make_control_params([
				[\intmod_pitch, specs[\pitch], 0.5],
			]),
			self.enabled_static_data,
			[
				//self.insertfxs,
			],
		);
	},

	make_layout: { arg self;
		var modkinds;
		var intmod;
		modkinds = self.modulation_keys.collect { arg key;
			(
				label: key.asString,
				mod_ctrl: self.mod_ctrl[key],
				oscs: self.enabled_ctrl[key],
			)
		};
		//modkinds.debug("ouais c'est le bordel");
		intmod = ~class_internal_modulator_gui.new(self.param[\intmod_pitch], modkinds);
		self.layout = intmod.layout;
		self.layout;
	},

	modulate: { arg self, key, idx, in;
		[key, idx].debug("class_ci_internal_modulator.modulate");
		{ arg args;
			var i = self.get_synthargs(args);
			var osc;

			osc = SinOsc.ar((i.intmod_pitch + i.freq.cpsmidi).midicps);
			//osc = self.osc_ugen;

			if(self.enabled_ctrl[key][idx].get_val == 1) {
				switch(key,
					\phase, {
						in + (osc * i.intmod_phase * 2 * in)
					},
					\ring, {
						in * (osc * i.intmod_ring)
					},
					\wt_pos, {
						in
						// TODO: casse les couilles
					},
					{
						in
					}
				)
			} {
				in
			}

		}
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var osc;

			osc = SinOsc.ar((i.intmod_pitch + i.freq.cpsmidi).midicps);
			self.osc_ugen = osc;
			osc;

		}
	},

);

~class_ci_gens_filter2 = (
	parent: ~class_instr,
	new: { arg self, main, player, namer, oscs;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_osc3filter2;
		self.simple_args = (freq:\void, gate:1, doneAction:2);

		self.oscs = oscs.(self);

		self.filters = 2.collect { arg idx;
			~class_ci_filter.new(main, player, self.make_namer("filter%_".format(idx)));
		};
		self.insertfxs = 2.collect { arg idx;
			~class_ci_insertfx.new(main, player, self.make_namer("insfx%_".format(idx)));
		};

		self.internal_modulator = ~class_ci_internal_modulator.new(main, player, self.make_namer, self.oscs);

		self.voices_keys = [\pitch_spread, \wt_pos_spread, \osc_intensity_spread, \arg1_spread];
		self.voices_panel = ~class_voices_panel.new(self, self.voices_keys);

		self.master = ~class_ci_master_dadsr.new(main, player);
		self.modules = [self.master, self.internal_modulator] ++ self.oscs ++ self.filters ++ self.insertfxs;

		self.tab_panel = ~class_ci_tabs_modfx.new(self, main, player, 
			[
				"Master Env", {  self.master.make_layout_env },
				"Routing", {  self.make_layout_routing },
				//"Voices", {  self.make_layout_voices },
				"Voices", {  self.voices_panel.make_layout },
			]
		);

		self.build_data;
	
		self;
	},

	load_data: { arg self, data;

		/// backward compatibility

		var rename_data = Dictionary.new;
		rename_data = (
			enable_wtpos_spread: \enable_wt_pos_spread,
			//wtpos_spread: \wt_pos_spread, // not static
		);

		data.static_data.keysValuesDo { arg key, val;
			[key].debug("class_ci_gens_filter2: load_data");

			if(rename_data[key].notNil) {
				key = rename_data[key]
			};
			self.static_data[key].load_data(val)
		};
		self.archive_data.do { arg key;
			self[key] = data[key];
		};
		self.build_synthdef;
	},

	build_freq_spread_array: { arg self, i, freq;
		if(i.enable_pitch_spread == 1) {
			var array = self.build_spread_array_by_kind(i.voices, i.spread_kind);
			array.debug("spread array");
			freq = (freq.cpsmidi + (i.pitch_spread * array)).midicps;
		} {
			freq = freq ! i.voices;
		};
		freq;
	},

	build_wt_pos_spread_array: { arg self, i, wtrange, wtpos;
		[wtrange, wtpos, i.enable_wt_pos_spread, i.voices, i.wt_pos_spread, i].debug("build_wt_pos_spread_array");
		if(i.enable_wt_pos_spread == 1) {
			var array = self.build_spread_array_by_kind(i.voices, i.spread_kind);
			(wtrange * array).debug("build_wtpos_spread_array: array");
			wtpos = (wtpos + (i.wt_pos_spread * wtrange * array));
		} {
			wtpos = wtpos ! i.voices;
		};
		wtpos;
	},

	build_intensity_spread_array: { arg self, i, intensity;
		//[wtrange, wtpos, i.enable_wtpos_spread, i.voices, i.wtpos_spread, i].debug("build_wtpos_spread_array");
		if(i.enable_osc_intensity_spread == 1) {
			var array = self.build_spread_array_by_kind(i.voices, i.spread_kind);
			//(wtrange * array).debug("build_wtpos_spread_array: array");
			intensity = (intensity + (i.osc_intensity_spread * array));
		} {
			intensity = intensity ! i.voices;
		};
		intensity;
	},


	get_route_insfx_variants: { arg self;
		[
			(
				name: "Before filter 1",
				uname: \before_filter1,
			),
			(
				name: "Before filter 2",
				uname: \before_filter2,
			),
			(
				name: "After filter 1",
				uname: \after_filter1,
			),
			(
				name: "After filter 2",
				uname: \after_filter2,
			),
			(
				name: "Between filters",
				uname: \between_filters,
			),
			(
				name: "Before pan",
				uname: \before_pan,
			),
			(
				name: "In feedback",
				uname: \in_feedback,
			)
		]
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		var static_voices_params = ();
		var static_data;
		var ordered_args;
		var filtermix = ~make_control_param.(main, player, \filtermix, \scalar, 0.5, \unipolar.asSpec);
		var filterparseq = ~make_control_param.(main, player, \filterparseq, \scalar, 0.5, \unipolar.asSpec);
		//self.ordered_args = self.modules.collect({ arg a; a.get_ordered_args }).flat;
		ordered_args = [
			filtermix: filtermix,
			filterparseq: filterparseq,
			//pitch_spread: ~make_control_param.(main, player, \pitch_spread, \scalar, 0, \bipolar.asSpec),
			//wtpos_spread: ~make_control_param.(main, player, \wtpos_spread, \scalar, 0, \bipolar.asSpec),

			feedback: ~make_control_param.(main, player, \feedback_outmix, \scalar, 0.0, ControlSpec(0,1.5,\lin, 0, 0)),
			feedback_outmix: ~make_control_param.(main, player, \feedback_outmix, \scalar, 0.5, \unipolar.asSpec),
		];
		filtermix.set_label("Filter Mix");
		filterparseq.set_label("Par Seq");

		self.voices_keys.do { arg key;
			var enable_key = "enable_%".format(key).asSymbol;
			static_voices_params[enable_key] = 
				~class_param_static_controller.new(enable_key, specs[\onoff], 0);
		};

		static_data = (
			route_insertfx1: ~class_param_kind_chooser_controller.new(\route_insertfx1, self.get_route_insfx_variants, "Insert Fx 1"),
			route_insertfx2: ~class_param_kind_chooser_controller.new(\route_insertfx2, self.get_route_insfx_variants, "Insert Fx 2"),
			//enable_pitch_spread: ~class_param_static_controller.new(\enable_pitch_spread, specs[\onoff], 0),
			//enable_wtpos_spread: ~class_param_static_controller.new(\enable_wtpos_spread, specs[\onoff], 0),
			voices: ~class_param_static_controller.new(\voices, ControlSpec(1,16,\lin,1), 1),
			spread_kind: ~class_param_kind_chooser_controller.new(\spread_kind, self.get_spread_kind_variants, "Spread kind"),

			enable_feedback: ~class_param_static_controller.new(\enable_feedback, specs[\onoff], 0),
		);
		static_data.putAll(static_voices_params);

		self.ordered_args.clump(2).do { arg keyval;
			keyval[0].debug("ORDERED ARGS:key");
			keyval[1].name.debug("ORDERED ARGS");
		};


		//self.static_data = IdentityDictionary.new;
		//self.modules.collect { arg mo;
		//	self.static_data.putAll(mo.get_static_data);
		//};
		//self.data = IdentityDictionary.newFrom(self.ordered_args);

		

		self.help_build_data(
			self.make_control_params(
				[
					[\freq, \freq, 202],
					[\pitchbend, specs[\pitch], 0],
				] 
				++
				self.voices_keys.collect ({ arg key;
					[key, \bipolar, 0]
				})
			) 
			++ ordered_args,
			static_data,
			self.modules,
		);

	},

	make_layout_routing: { arg self;
		var route_data = [
			\route_insertfx1,
			\route_insertfx2,
		].collect({ arg x; self.param[x] });

		var routing_layout = GridLayout.columns(
			route_data.collect{ arg x; 
				StaticText.new
					.string_(x.label ?? x.name);
			} ++ [nil],
			route_data.collect{ arg x; 
				~class_ci_popup_view.new(x).layout;
			} ++ [nil],
		);
		routing_layout.setColumnStretch(1,1);
		routing_layout;
	},

	//make_layout_voices: { arg self;
	//	HLayout(
	//		VLayout(
	//			StaticText.new
	//				.string_("Voices:"),
	//			TextField.new
	//				.string_(self.param[\voices].get_val)
	//				.action_({ arg field;
	//					self.param[\voices].set_val(field.string.asInteger)
	//				}),
	//			nil
	//		),
	//		[VLayout(
	//			HLayout(
	//				Button.new
	//					.states_([["Off"],["On"]])
	//					.value_(self.param[\enable_pitch_spread].get_val)
	//					.action_({ arg bt; 
	//						self.param[\enable_pitch_spread].set_val(bt.value);
	//					}),
	//				{
	//					var slider;
	//					slider = ~class_ci_modslider_view.new(self.data[\pitch_spread],Rect(0,0,300,20));
	//					slider.namelabel.minWidth_(100);
	//					slider.layout;
	//				}.value

	//			),
	//			HLayout(
	//				Button.new
	//					.states_([["Off"],["On"]])
	//					.value_(self.param[\enable_wtpos_spread].get_val)
	//					.action_({ arg bt; 
	//						self.param[\enable_wtpos_spread].set_val(bt.value);
	//					}),
	//				{
	//					var slider;
	//					slider = ~class_ci_modslider_view.new(self.data[\wtpos_spread],Rect(0,0,300,20));
	//					slider.namelabel.minWidth_(100);
	//					slider.layout;
	//				}.value

	//			),
	//			HLayout(
	//				Button.new
	//					.states_([["Off"],["On"]])
	//					.enabled_(false)
	//					.value_(1)
	//					,
	//				{
	//					var slider;
	//					slider = ~class_ci_modslider_view.new(self.data[\spread],Rect(0,0,300,20));
	//					slider.namelabel.minWidth_(100);
	//					slider.layout;
	//				}.value
	//				//~class_ci_modslider_view.new(self.param[\spread],Rect(0,0,300,20)).layout
	//			), 
	//			nil
	//		
	//		), stretch:1],

	//	)
	//},

	make_tab_panel: { arg self;
		self.tab_panel.make_layout;
	},

	make_layout_feedback: { arg self;
		var knobs = [\feedback];
		var frame_view;
		var layout;
		knobs = knobs.collect { arg name;
			self.param[name];
		};
		frame_view = ~class_ci_frame_view.new(
			"Feedback", knobs, self.param[\enable_feedback], nil, nil, self.param[\feedback_outmix]
		);
		layout = HLayout(frame_view.layout);
		layout;
	},


	make_layout: { arg self;
		self.layout = HLayout(
				debug("****************************************** LAYOUT oscs");
			VLayout(*
				self.oscs.collect({arg x;[x.make_layout, stretch:0]}) ++
				[
					self.make_layout_feedback,
					[nil, stretch:1],
				]
			),
				debug("****************************************** LAYOUT filters");
			VLayout(*
				[HLayout(
					HLayout(
						~class_ci_modslider_view.new(self.param[\filterparseq]).layout,
						VLayout(*
							self.filters.collect(_.make_layout) ++
							[nil]
						),
						~class_ci_modslider_view.new(self.param[\filtermix]).layout,
					),
						//debug("****************************************** LAYOUT master");
					VLayout(*
						[
							HLayout(
								[self.internal_modulator.make_layout],
								[self.master.make_layout, stretch:0],
							)
						] ++
						//[self.make_layout_routing, stretch:0],
							//debug("****************************************** LAYOUT fx");
						[HLayout(*
							self.insertfxs.collect(_.make_layout)
						)] ++
						[
							[nil, stretch:1],
						]
					)
				)] ++
				[[
						debug("****************************************** LAYOUT tabs");
					self.make_tab_panel, stretch:0
				]] ++
				[nil]
			),
			VLayout(
			),
			nil,
		);
		self.layout;
	},

	insert_effect: { arg self, i, in, pos;
		if(i.route_insertfx1 == pos) {
			//TODO: ktr, arg3
			in = self.insertfxs[0].synthfun.(in);
		};
		if(i.route_insertfx2 == pos) {
			in = self.insertfxs[1].synthfun.(in);
		};
		in;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig, sig1, sig2;
			var rsig;
			var oscs;
			var f1_in, f2_in;
			var fmiddle_in;
			var f1_out, f2_out;
			var feedback, feedback1 = 0, feedback2 = 0;
			var dout;
			var freq;

			//[1,2,4].sum
			//[[1,2],[2,4],[4,6]].sum
			//[[[1.1,2.1],[1,2]],[[2.1,4.1],[2,4]]].flop[0].sum

			freq = (i.freq.cpsmidi + i.pitchbend).midicps;
			freq = self.build_freq_spread_array(i, freq);

			oscs = self.oscs.collect { arg osc, idx;
				var osc_freq;
				var wtpos;
				var intensity;
				var res;

				osc_freq = self.internal_modulator.modulate(\phase, idx, freq).((freq:freq));
				//wtpos = self.internal_modulator.modulate(\wt_pos, idx, wtpos).((freq:freq));

				if(osc.param[\wt_range].notNil) {
					var oscargs = osc.get_synthargs(args);
					wtpos = self.build_wt_pos_spread_array(i, oscargs.wt_range, oscargs.wt_pos);
					intensity = self.build_intensity_spread_array(i, oscargs.intensity);
				};

				res = osc.synthfun.((freq:osc_freq, wt_pos:wtpos, intensity:intensity));
				res = self.internal_modulator.modulate(\ring, idx, res).((freq:freq));
				res;
			};
			rsig = oscs[0];
			oscs.debug("OSCS");
			oscs = oscs.flop;


			if(i.enable_feedback == 1) {
				feedback = LocalIn.ar(1) * i.feedback;
				feedback = feedback.clip(-1,1);

				feedback = self.insert_effect(i, feedback, \in_feedback);
				feedback = self.insert_effect(i, feedback, \in_feedback);

				feedback1 = SelectX.ar(i.feedback_outmix, [feedback, DC.ar(0)]);
				feedback2 = SelectX.ar(i.feedback_outmix, [DC.ar(0), feedback]);
			};
			

			f1_in = oscs[1].sum + feedback1;
			f2_in = oscs[0].sum + feedback2;

			//dout = f1_in + f2_in;

			f1_in.debug("F1IN1");
			// before effect
			f1_in = self.insert_effect(i, f1_in, \before_filter1);
			f2_in = self.insert_effect(i, f2_in, \before_filter2);

			f1_in.debug("F1IN2");
			//dout = f1_in + f2_in;
			//dout.poll;

			// filtering 1
			f1_out = self.filters[0].synthfun.(f1_in);
			f1_out.debug("F1OUT1");

			// parseq
			fmiddle_in = SelectX.ar(i.filterparseq, [f1_out, DC.ar(0)]);
			f1_out = SelectX.ar(i.filterparseq, [DC.ar(0), f1_out]);


			// middle effect
			fmiddle_in = self.insert_effect(i, fmiddle_in, \between_filters);

			//parseq
			f2_in = f2_in + fmiddle_in;

			// filtering 2
			f2_out = self.filters[1].synthfun.(f2_in);

			// after effect
			f1_out = self.insert_effect(i, f1_out, \after_filter1);
			f2_out = self.insert_effect(i, f2_out, \after_filter2);

			// mix
			sig = SelectX.ar(i.filtermix, [f2_out, f1_out]);

			// end effect
			sig = self.insert_effect(i, sig, \before_pan);

			//sig = sig ! 2;
			if(i.enable_feedback == 1) {
				LocalOut.ar(sig.sum);
			};

			sig = self.master.synthfun.(sig);

			sig;

		}
	
	},
);

~class_ci_tabs_modfx = (
	
	new: { arg self, classinstr, main, player, tabs;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.classinstr = { classinstr };
		self.dummy_param = ~class_param_static_controller.new("dummy", \freq.asSpec);
		self.tabs = tabs;

		self;
		
	},

	make_layout: { arg self;
		self.make_tab_panel;
	},

	make_layout_effects: { arg self;
		self.fx_ctrl = ~class_embeded_effects_controller.new(self.get_main, self.get_player);
		self.fx_ctrl.window = { self.classinstr.window };
		self.fx_ctrl.layout;
	},

	make_layout_compositor: { arg self;
		if(self.get_player.compositor.notNil) {
			self.get_player.compositor.make_layout;
		}
	},

	make_tab_panel: { arg self;
		var header, body, layout;
		var content;
		var modview;
		var tab_views = List.new;
		self.modulation_controller = ~class_embeded_modulation_controller.new(self.get_main, self.get_player, nil, self.dummy_param);
		self.modulation_controller.make_bindings;
		self.modulation_controller.window = { self.classinstr.window };
		modview = ~class_embeded_modulation_view.new(self.modulation_controller);
		self.modulation_controller.main_view = modview;

		content = 
			self.tabs ++ [
				"Compo", {  self.make_layout_compositor }, 
				"Effects", {  self.make_layout_effects }, // should be last
		];
		self.tabs_count = content.size/2;
		content = content.clump(2).flop;
		debug("NUIT 1");
		body = StackLayout(*
			content[1].collect { arg co;
				var view;
				view = View.new;
				tab_views.add(view);
				view;
			} ++ [
				View.new.layout_(modview.body_layout),
				//View.new.layout_(modview.mod_body.layout),
			]
		);
		debug("NUIT 1.5");
		tab_views.do { arg view, idx;
			view.layout = content[1][idx].value
		};
		debug("NUIT 2");
		layout = VLayout(
			HLayout(*
				content[0].collect { arg co, idx;
					debug("NUIT 3");
					Button.new
						.states_([[co]])
						.action_({ 
							idx.debug("class_ci_tabs_modfx.make_tab_panel: tab action: idx");
							body.index = idx;
							if(idx == (self.tabs_count-1)) { 
								//self.classinstr.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\effects);
								self.fx_ctrl.set_keydown_responder(\effects);
							} {
								self.classinstr.window.view.keyDownAction = 
									//self.get_main.commands.get_kb_responder(\classinstr, self.classinstr);
									self.classinstr.binding_responder.get_kb_responder(\classinstr);
							}
						})
				}
			),
			modview.tab_layout,
			body,
		
		);
		debug("NUIT 4");
		self.tab_panel_stack_layout = body;
		modview.show_body_layout = { arg myself;
			debug("modview: show_body_layout");
			self.tab_panel_stack_layout.index = self.tabs_count;
		};
		layout;
	},

);

