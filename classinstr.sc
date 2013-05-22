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

	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
	
		self;
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

	set_all_bus_mode: { arg self, enable;
		self.data.keysValuesDo { arg name, datum;
			[name, datum.name].debug("class_instr.set_all_bus_mode: param name");
			if(datum.classtype == \control) {
				datum.set_bus_mode(enable);
			}
		}
	},

	destructor: { arg self;
		self.static_data.keysValuesDo { arg name, datum;
			datum.destructor;
		}
	},

	init_top_classinstr: { arg self;
		self.set_static_responders;
		self.set_param_abs_labels;
		self.data[\freq] = ~make_control_param.(self.get_main, self.get_player, \freq, \scalar, 200, \freq.asSpec);
		self.data[\velocity] = ~make_control_param.(self.get_main, self.get_player, \velocity, \scalar, 0.5, \unipolar.asSpec);
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
		wideamp: ControlSpec(0, 6, 'amp', 0, 0.1, ""),
		crush: ControlSpec(1, 31, 'lin', 0, 1, ""),
		smalldelay: ControlSpec(0, 0.02, 'lin', 0, 0.001, ""),
		envamp: ControlSpec(0, 1, 'amp', 0, 1, ""),
		sustain: ControlSpec(0.001, 4, \exp, 0, 0.25),
	),

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
		var res = IdentityDictionary.new;
		self.data.keysValuesDo { arg key, val;
			res[self.rel_namer(key)] = val;
		};
		res;
	},

	get_ordered_args: { arg self;
		var res = List.new;
		res = self.ordered_args.clump(2).collect { arg keyval;
			[self.rel_namer(keyval[0]), keyval[1]]
			//[keyval[0], keyval[1]]
		};
		res.flat;
	},

	help_build_data: { arg self, datalist, static_datalist;
		datalist.do { arg data;
			if(data.isSequenceableCollection) {
				self.ordered_args = self.ordered_args ++ data;
			} {
				self.ordered_args = self.ordered_args ++ data.get_ordered_args;
			}
		};
		self.data.putAll(self.ordered_args);
		static_datalist.do { arg data;
			if(data[\get_static_data].notNil) {
				self.static_data.putAll(data.get_static_data)
			} {
				self.static_data.putAll(data)
			}
		};
		self.param = ();
		self.param.putAll(self.data);
		self.param.putAll(self.static_data);
	},

	help_build_data2: { arg self, modules, datalist=[], static_datalist=();
		//debug("BEGIN help_build_data2");
		//self.ordered_args.debug("ordered_args");
		modules.do { arg mod;
			self.ordered_args = self.ordered_args ++ mod.get_ordered_args;
			self.static_data.putAll( mod.get_static_data );
		};
		//self.ordered_args.do { arg x; x.debug("=========================\nordered_args"); };

		self.ordered_args = self.ordered_args ++ datalist;
		self.data.putAll(self.ordered_args);

		self.static_data.putAll(static_datalist);

		self.param = ();
		self.param.putAll(self.data);
		self.param.putAll(self.static_data);
	},

	get_synthargs: { arg self, args=();
		var i;
		i = args.copy;
		self.data.keysValuesDo { arg name, datum;
			var control_name;
			if(i[name].isNil) {
				control_name = self.abs_namer(name);
				switch(datum.param_rate,
					\tr, {
						i[name] = control_name.tr(datum.default_value);
					},
					{
						i[name] = control_name.kr(datum.default_value);
					}
				);
			}
		};
		self.simple_args.keysValuesDo { arg name, def;
			if(i[name].isNil) {
				if(def == \void) {
					i[name] = name.kr;
				} {
					i[name] = name.kr(def);
				}
			}
		};
		self.static_data.keysValuesDo { arg name, datum;
			i[name] = switch(datum.classtype,
				[name, datum.name].debug("class_instr.get_synthargs: param name");
				\kind_chooser, {
					{ arg self; datum.get_val_uname; }
				}, 
				{
					{ arg self; datum.get_val; }
				}
			);
		};
		i;
	},

	set_static_responders: { arg self;
		var resp = Dictionary.new;
		self.static_responders.do(_.remove);
		self.static_data.debug("class_instr.set_static_responders");
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

	freeze_build_synthdef_do: { arg self, fun;
		self.freeze_build_synthdef = true;
		fun.();
		self.freeze_build_synthdef = false;
	},

	build_synthdef: { arg self, rate=\ar;
		var synthdef_name;
		//self.synthdef_name = self.synthdef_basename ++ self.synthdef_name_suffix;
		if(self.freeze_build_synthdef.not) {

			self.synthdef_name = "%_%".format(self.synthdef_basename, self.get_player.uname);
			self.synthdef_name.debug("REBUILD SYNTH");
			rate = self.synth_rate ?? rate;
			SynthDef(self.synthdef_name, { arg out=0;
				var sig;

				sig = self.synthfun.();

				Out.performList(rate, [out, sig]);

			}).add;

			if(self.is_effect) {
				self.get_player.build_sourcepat_finalize;
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
		self.ordered_args = [
			freq: ~make_control_param.(main, player, \freq, \scalar, 200, \freq.asSpec),
		];
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
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

		self.simple_args = (gate:1, doneAction:2);
	
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
		var specs = self.get_specs;
		var wt, wt_range, wt_classic, wt_pos;
		self.synthdef_name.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%% build data");
		wt_pos = ~make_control_param.(main, player, \wt_pos, \scalar, 0, \unipolar.asSpec.copy);
		wt = ~class_param_wavetable_controller.new(player, \wt, wt_pos);
		wt_range = wt.get_wt_range_controller;
		wt_classic = wt.get_wt_classic_controller;

		
		self.ordered_args = [
			freq: ~make_control_param.(main, player, \freq, \scalar, 200, \freq.asSpec),
			detune: ~make_control_param.(main, player, \detune, \scalar, 0, specs[\pitch]),
			wt: wt,
			wt_pos: wt_pos,
			intensity: ~make_control_param.(main, player, \intensity, \scalar, 0, \unipolar.asSpec),
			oscamp: ~make_control_param.(main, player, \oscamp, \scalar, 0.5, \amp.asSpec),
		];
		self.static_data = IdentityDictionary.newFrom((
			spectrum: ~class_param_kind_chooser_controller.new(\spectrum, self.get_spectrum_variants),
			wt_range: wt_range,
			wt_classic: wt_classic,
			enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
		));
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		//self.data[\detune].scalar.set_bus_mode(true);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\detune, \wt_pos, \intensity, \oscamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new("Osc1", self.knobs, self.static_data[\enabled], self.data[\wt], self.static_data[\spectrum]);
		self.layout = frame_view.layout;
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			i.debug("III");
			args.debug("ARGS");
			i.freq.debug("FREQ");

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
		
		self.ordered_args = self.osc.get_ordered_args ++ [
			outmix: ~make_control_param.(main, player, \outmix, \scalar, 0.5, \unipolar.asSpec),
		];
		self.static_data = self.osc.get_static_data;
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\detune, \wt_pos, \intensity, \oscamp];
		var frame_view;
		self.data.keys.debug("DATA");
		self.data.values.collect(_.name).debug("DATA2");
		self.data.values.collect(_.label).debug("DATA3");
		self.knobs = knobs.collect { arg name;
			var rel_name = self.osc.rel_namer(name);
			rel_name.debug("RELNAME");
			self.osc.data[name];
		};
		frame_view = ~class_ci_frame_view.new("Osc1", self.knobs, self.osc.static_data[\enabled], self.osc.data[\wt], self.osc.static_data[\spectrum], self.data[\outmix]);
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
		
		self.ordered_args = self.osc.get_ordered_args ++ [
			outmix: ~make_control_param.(main, player, \outmix, \scalar, 0.5, \unipolar.asSpec),
		];
		self.static_data = self.osc.get_static_data;
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout: { arg self;
		self.layout = self.osc.make_layout(self.data[\outmix]);
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
		self.build_data;

		self.simple_args = (freq:\void, gate:1, doneAction:2);
	
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

		self.ordered_args = [
			arg1: ~make_control_param.(main, player, \arg1, \scalar, 60, \midinote.asSpec.copy),
			arg2: ~make_control_param.(main, player, \arg2, \scalar, 0.5, \unipolar.asSpec.copy),
			arg3: ~make_control_param.(main, player, \arg3, \scalar, 0, \unipolar.asSpec.copy),
			filteramp: ~make_control_param.(main, player, \filteramp, \scalar, 0.8, \unipolar.asSpec.copy),
		];
		filterkind = ~class_param_kind_chooser_controller.new(\filterkind, self.get_variants);
		self.static_data = IdentityDictionary.newFrom((
			filterkind: filterkind,
			enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
		));
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		sc = SimpleController.new(filterkind);
		sc.put(\val, {
			var idx = filterkind.get_val;
			var variant = self.get_variants[idx];
			variant.args.do { arg name, i;
				[self.data.keys, "arg%".format(i+1).asSymbol].debug("RESPONDER");
				self.data["arg%".format(i+1).asSymbol].set_label(name);
				self.data["arg%".format(i+1).asSymbol].set_spec(variant.specs[i].asSpec.copy);
			};
		});
		filterkind.changed(\val);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\arg1, \arg2, \arg3];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new("Filter", 
			self.knobs, self.static_data[\enabled], self.static_data[\filterkind], nil, self.data[\filteramp]
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
			)
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

		self.ordered_args = [
			arg1: ~make_control_param.(main, player, \arg1, \scalar, 60, \midinote.asSpec.copy),
			arg2: ~make_control_param.(main, player, \arg2, \scalar, 0.5, \unipolar.asSpec.copy),
			arg3: ~make_control_param.(main, player, \arg3, \scalar, 0, \unipolar.asSpec.copy),
		];
		insertfxkind = ~class_param_kind_chooser_controller.new(\insertfxkind, self.get_variants);
		self.static_data = IdentityDictionary.newFrom((
			insertfxkind: insertfxkind,
			enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 0),
		));
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		sc = SimpleController.new(insertfxkind);
		sc.put(\val, {
			var idx = insertfxkind.get_val;
			var variant = self.get_variants[idx];
			variant.args.do { arg name, i;
				[self.data.keys, "arg%".format(i+1).asSymbol].debug("RESPONDER");
				self.data["arg%".format(i+1).asSymbol].set_label(name);
				self.data["arg%".format(i+1).asSymbol].set_spec(variant.specs[i].asSpec.copy);
			};
		});
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\arg1, \arg2, \arg3];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new("InsertFx", self.knobs, self.static_data[\enabled], self.static_data[\insertfxkind]);
		self.layout = frame_view.layout;
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
		self.ordered_args = [
			delay: ~make_control_param.(main, player, \delay, \scalar, 0, specs[\env]),
			attack_time: ~make_control_param.(main, player, \attack_time, \scalar, 0.1, specs[\env]),
			decay_time: ~make_control_param.(main, player, \decay_time, \scalar, 0.1, specs[\env]),
			sustain_level: ~make_control_param.(main, player, \sustain_level, \scalar, 0.5, specs[\envamp]),
			release_time: ~make_control_param.(main, player, \release_time, \scalar, 0.1, specs[\env]),
			curve: ~make_control_param.(main, player, \curve, \scalar, 1, ControlSpec(-10,10,\lin,0,1)),
			velocity_mix: ~make_control_param.(main, player, \velocity_mix, \scalar, 0.1, \unipolar.asSpec),
			ampcomp: ~make_control_param.(main, player, \ampcomp, \scalar, 0.1, \unipolar.asSpec),
		];
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\delay, \attack_time, \decay_time, \sustain_level, \release_time, \curve];
		var faders = [\ampcomp, \velocity_mix];
		var frame_view;
		var env_view;
		var layout;
		var knobs_layouts;
		knobs_layouts = knobs.collect { arg name;
			~class_ci_modknob_view.new(self.data[name]).layout;
			//ModKnob.new.asView;
		};
		self.faders = faders.collect { arg name;
			~class_ci_modslider_view.new(self.data[name], Rect(0,0,30,100)).layout;
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
			~make_view_responder.(env_view, self.data[name], (
				val: {
					env_view.setEnv( Env.dadsr(
						self.data[\delay].get_val,
						self.data[\attack_time].get_val,
						self.data[\decay_time].get_val,
						self.data[\sustain_level].get_val,
						self.data[\release_time].get_val,
						1,
						self.data[\curve].get_val
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
			~class_ci_simpleknob_view.new(self.data[name]).layout;
		};

		knobs.do { arg knob, i;
			self.data[knob].set_label(labels[i])
		};

		

		layout = 
			HLayout(*
				[env_view = EnvelopeView.new] ++
				knobs_layouts
			);
		knobs.do { arg name;
			~make_view_responder.(env_view, self.data[name], (
				val: {
					env_view.setEnv( Env.dadsr(
						self.data[\delay].get_val,
						self.data[\attack_time].get_val,
						self.data[\decay_time].get_val,
						self.data[\sustain_level].get_val,
						self.data[\release_time].get_val,
						1,
						self.data[\curve].get_val
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
		self.ordered_args = [
			pan: ~make_control_param.(main, player, \pan, \scalar, 0, \pan.asSpec),
			spread: ~make_control_param.(main, player, \spread, \scalar, 1, \unipolar.asSpec),
			amp: ~make_control_param.(main, player, \amp, \scalar, 0.1, \amp.asSpec),
		];
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\pan, \spread, \amp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
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
				[
					pan: ~make_control_param.(main, player, \pan, \scalar, 0, \pan.asSpec),
					spread: ~make_control_param.(main, player, \spread, \scalar, 0, \unipolar.asSpec),
					amp: ~make_control_param.(main, player, \amp, \scalar, 0.1, \amp.asSpec),
				],
				self.dadsr;
			],
		)
	},

	make_layout: { arg self;
		var knobs = [\pan, \amp];
		var frame_view;
		if(self.showspread) {
			knobs = knobs.add(\spread);
		};
		self.knobs = knobs.collect { arg name;
			self.data[name];
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
		self.help_build_data(
			[
				[
					pan: ~make_control_param.(main, player, \pan, \scalar, 0, \pan.asSpec),
					spread: ~make_control_param.(main, player, \spread, \scalar, 0, \unipolar.asSpec),
					amp: ~make_control_param.(main, player, \amp, \scalar, 0.1, \amp.asSpec),
				],
				self.dadsr;
			],
			[
				// FIXME: should be a dict, no ?
				self.ienv_controls.collect { arg name;
					[
						name.asSymbol,
						{ var a; a = ~class_param_ienv_proxy_controller.new(self.ienv_presets.get_preset(\off)); a.set_curve(self.ienv_presets.get_preset(\off)); a }.value,
					]
				
				}.flat
			]
		)
	},

	make_layout: { arg self;
		var knobs = [\pan, \amp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
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
				[
					amp: ~make_control_param.(main, player, \amp, \scalar, 0.1, \amp.asSpec),
				],
			],
			[
				(
					kind: ~class_param_kind_chooser_controller.new(\kind, self.get_variants),
					enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 0),
				)
			]
		)
	},

	make_layout: { arg self, fader;
		var knobs = [\amp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new("Noise", self.knobs, self.static_data[\enabled], self.static_data[\kind], nil, fader);
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
		self.help_build_data2(
			[
				self.dadsr
			],
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
			)
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
			//~class_ci_simpleknob_view.new(self.data[name]).layout;
			~class_ci_modknob_view.new(self.data[name], display).layout;
		};
		frame = View.new;
		frame.background = Color.gray(0.5);
		frame.layout = HLayout(
			VLayout(*
				[
					HLayout(
						StaticText.new.string_(self.name),
						~class_ci_popup_view.new(self.data[\wt]).layout
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
				[
					env: env
				]
			],
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

		self.help_build_data2(
			[
				self.dadsr,
				self.custom_env,
			],
			[],
			(
				env_kind: env_kind
			)
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

		self.help_build_data2(
			[
				//self.insertfxs,
			],
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
			)
		);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\bufpos, \finepos, \range];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new("Bufosc", self.knobs, self.static_data[\enabled], nil, nil);
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

		self.help_build_data2(
			[
				//self.insertfxs,
			],
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
			)
		);
		self.data.copy;
	},

	make_layout: { arg self, fader;
		var knobs = [\bufrate, \bufpos, \bufamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new("Sampler", self.knobs, self.static_data[\enabled], nil, nil, fader);
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
////////////// Extensions Class Instrs
//////////////////////////////////////////////////////



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
		self.master = ~class_ci_master_dadsr.new(main, player);
		self.modules = [self.master] ++ self.oscs ++ self.filters ++ self.insertfxs;

		self.tab_panel = ~class_ci_tabs_modfx.new(self, main, player, 
			[
				"Master Env", {  self.master.make_layout_env },
				"Routing", {  self.make_layout_routing },
				"Voices", {  self.make_layout_voices },
			]
		);

		self.build_data;
	
		self;
	},

	build_freq_spread_array: { arg self, i, freq;
		if(i.enable_pitch_spread == 1) {
			var array = self.build_spread_array(i.voices);
			array.debug("spread array");
			freq = (freq.cpsmidi + (i.pitch_spread * array)).midicps;
		} {
			freq = freq ! i.voices;
		};
		freq;
	},

	build_wtpos_spread_array: { arg self, i, wtrange, wtpos;
		[wtrange, wtpos, i.enable_wtpos_spread, i.voices, i.wtpos_spread, i].debug("build_wtpos_spread_array");
		if(i.enable_wtpos_spread == 1) {
			var array = self.build_spread_array(i.voices);
			(wtrange * array).debug("build_wtpos_spread_array: array");
			wtpos = (wtpos + (i.wtpos_spread * wtrange * array));
		} {
			wtpos = wtpos ! i.voices;
		};
		wtpos;
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
		var filtermix = ~make_control_param.(main, player, \filtermix, \scalar, 0.5, \unipolar.asSpec);
		var filterparseq = ~make_control_param.(main, player, \filterparseq, \scalar, 0.5, \unipolar.asSpec);
		self.ordered_args = self.modules.collect({ arg a; a.get_ordered_args }).flat;
		self.ordered_args = self.ordered_args ++ [
			filtermix: filtermix,
			filterparseq: filterparseq,
			pitch_spread: ~make_control_param.(main, player, \pitch_spread, \scalar, 0, \bipolar.asSpec),
			wtpos_spread: ~make_control_param.(main, player, \wtpos_spread, \scalar, 0, \bipolar.asSpec),

			feedback: ~make_control_param.(main, player, \feedback_outmix, \scalar, 0.0, ControlSpec(0,1.5,\lin, 0, 0)),
			feedback_outmix: ~make_control_param.(main, player, \feedback_outmix, \scalar, 0.5, \unipolar.asSpec),
		];
		filtermix.set_label("Filter Mix");
		filterparseq.set_label("Par Seq");


		self.static_data = (
			route_insertfx1: ~class_param_kind_chooser_controller.new(\route_insertfx1, self.get_route_insfx_variants, "Insert Fx 1"),
			route_insertfx2: ~class_param_kind_chooser_controller.new(\route_insertfx2, self.get_route_insfx_variants, "Insert Fx 2"),
			enable_pitch_spread: ~class_param_static_controller.new(\enable_pitch_spread, specs[\onoff], 0),
			enable_wtpos_spread: ~class_param_static_controller.new(\enable_wtpos_spread, specs[\onoff], 0),
			voices: ~class_param_static_controller.new(\voices, ControlSpec(1,16,\lin,1), 1),

			enable_feedback: ~class_param_static_controller.new(\enable_feedback, specs[\onoff], 0),
		);

		self.ordered_args.clump(2).do { arg keyval;
			keyval[0].debug("ORDERED ARGS:key");
			keyval[1].name.debug("ORDERED ARGS");
		};

		//self.static_data = IdentityDictionary.new;
		self.modules.collect { arg mo;
			self.static_data.putAll(mo.get_static_data);
		};
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout_routing: { arg self;
		var route_data = [
			\route_insertfx1,
			\route_insertfx2,
		].collect({ arg x; self.static_data[x] });

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

	make_layout_voices: { arg self;
		HLayout(
			VLayout(
				StaticText.new
					.string_("Voices:"),
				TextField.new
					.string_(self.static_data[\voices].get_val)
					.action_({ arg field;
						self.static_data[\voices].set_val(field.string.asInteger)
					}),
				nil
			),
			[VLayout(
				HLayout(
					Button.new
						.states_([["Off"],["On"]])
						.value_(self.static_data[\enable_pitch_spread].get_val)
						.action_({ arg bt; 
							self.static_data[\enable_pitch_spread].set_val(bt.value);
						}),
					{
						var slider;
						slider = ~class_ci_modslider_view.new(self.data[\pitch_spread],Rect(0,0,300,20));
						slider.namelabel.minWidth_(100);
						slider.layout;
					}.value

				),
				HLayout(
					Button.new
						.states_([["Off"],["On"]])
						.value_(self.static_data[\enable_wtpos_spread].get_val)
						.action_({ arg bt; 
							self.static_data[\enable_wtpos_spread].set_val(bt.value);
						}),
					{
						var slider;
						slider = ~class_ci_modslider_view.new(self.data[\wtpos_spread],Rect(0,0,300,20));
						slider.namelabel.minWidth_(100);
						slider.layout;
					}.value

				),
				HLayout(
					Button.new
						.states_([["Off"],["On"]])
						.enabled_(false)
						.value_(1)
						,
					{
						var slider;
						slider = ~class_ci_modslider_view.new(self.data[\spread],Rect(0,0,300,20));
						slider.namelabel.minWidth_(100);
						slider.layout;
					}.value
					//~class_ci_modslider_view.new(self.data[\spread],Rect(0,0,300,20)).layout
				), 
				nil
			
			), stretch:1],

		)
	},

	make_tab_panel: { arg self;
		self.tab_panel.make_layout;
	},

	make_layout_feedback: { arg self;
		var knobs = [\feedback];
		var frame_view;
		var layout;
		knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new(
			"Feedback", knobs, self.static_data[\enable_feedback], nil, nil, self.data[\feedback_outmix]
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
						~class_ci_modslider_view.new(self.data[\filterparseq]).layout,
						VLayout(*
							self.filters.collect(_.make_layout) ++
							[nil]
						),
						~class_ci_modslider_view.new(self.data[\filtermix]).layout,
					),
						//debug("****************************************** LAYOUT master");
					VLayout(*
						[
							[self.master.make_layout, stretch:0],
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

			//[1,2,4].sum
			//[[1,2],[2,4],[4,6]].sum
			//[[[1.1,2.1],[1,2]],[[2.1,4.1],[2,4]]].flop[0].sum

			oscs = self.oscs.collect { arg osc;
				var freq;
				var wtpos;
				freq = self.build_freq_spread_array(i, i.freq);
				if(osc.static_data[\wt_range].notNil) {
					var oscargs = osc.get_synthargs(args);
					wtpos = self.build_wtpos_spread_array(i, oscargs.wt_range, oscargs.wt_pos);
				};
				osc.synthfun.((freq:freq, wt_pos:wtpos));
			};
			rsig = oscs[0];
			oscs.debug("OSCS");
			oscs = oscs.flop;

			if(i.enable_feedback == 1) {
				feedback = LocalIn.ar(1) * i.feedback;
				feedback = feedback.clip(-1,1);
				feedback1 = SelectX.ar(i.feedback_outmix, [feedback, DC.ar(0)]);
				feedback2 = SelectX.ar(i.feedback_outmix, [DC.ar(0), feedback]);
			};
			

			f1_in = oscs[1].sum + feedback1;
			f2_in = oscs[0].sum + feedback2;

			f1_in.debug("F1IN1");
			// before effect
			f1_in = self.insert_effect(i, f1_in, \before_filter1);
			f2_in = self.insert_effect(i, f2_in, \before_filter2);

			f1_in.debug("F1IN2");

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

//////////////////////////////////////////////////////
////////////// End Class Instrs
//////////////////////////////////////////////////////

////////////// Oscs


~class_ci_sin = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
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
		var data = Dictionary.new;
		var main = self.get_main;
		var player = self.get_player;
		self.ordered_args = [
			freq: ~make_control_param.(main, player, \freq, \scalar, 200, \freq.asSpec),
			detune: ~make_control_param.(main, player, \detune, \scalar, 64, \midinote.asSpec),
		];
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\freq, \detune];
		self.knobs = knobs.collect { arg name;
			~class_ci_modknob_view.new(self.data[name]);
		};
		self.layout = VLayout(
			HLayout(
				self.label = StaticText.new
					.string_("Osc1");
					self.label,
			),
			HLayout(*
				self.knobs.collect(_.layout)
			)
		);
		self.layout;
	},

	synthfun: { arg self;
		//Instr(self.synthdef_name, { 
		{ arg args;
			//var input = Dictionary.new;
			var i = self.get_synthargs(args);
			var midinote;
			var sig;

			//self.data.keysValuesDo { arg name, datum;
			//	input[name] = name.kr(datum.default_value);
			//};
			//sig = self.ci_lfo.synthfun.((freq1:freq));

			sig = SinOsc.ar(
				(i.freq.cpsmidi + i.detune).midicps
			);
			sig = sig * EnvGen.ar(Env.adsr(0.1,0.1,1,0.1), i.gate, doneAction:i.doneAction);
			sig;

		}
	
	},
);

~class_ci_mosc = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_mosc;
		self.namer = { namer };
		//self.simple_args = (freq: 200, gate:1, doneAction:2);
		self.simple_args = (gate:1, doneAction:2);

		self.osc = ~class_ci_osc.new(main, player, self.make_namer("bla_"));
		//self.osc = ~class_ci_osc.new(main, player, self.make_namer);
		self.master = ~class_ci_master_env.new(main, player);

		self.build_data;
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		self.ordered_args = self.master.get_ordered_args ++ self.osc.get_ordered_args;
		self.static_data = self.osc.get_static_data;
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\detune, \wt_pos, \intensity, \oscamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		self.layout = HLayout(
			[self.osc.make_layout, stretch:0],
			[self.master.make_layout, stretch:0],
			nil,
		);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var is = self.get_staticargs;
			var sig;

			sig = self.osc.synthfun.((freq:i.freq));
			//sig = self.osc.synthfun.((freq:i.freq));
			sig = self.master.synthfun.(sig);

			sig;

		}
	
	},
);

~class_ci_moscfilter = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_moscfilter;
		self.simple_args = (gate:1, doneAction:2);

		self.osc = ~class_ci_osc.new(main, player);
		self.filter = ~class_ci_filter.new(main, player);
		self.master = ~class_ci_master_env.new(main, player);

		self.build_data;
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		self.ordered_args = self.master.ordered_args ++ self.osc.ordered_args ++ self.filter.ordered_args;
		self.static_data = IdentityDictionary.new;
		self.static_data.putAll(self.osc.static_data);
		self.static_data.putAll(self.filter.static_data);
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout: { arg self;
		self.layout = HLayout(
			[self.osc.make_layout, stretch:0],
			[self.filter.make_layout, stretch:0],
			[self.master.make_layout, stretch:0],
			nil,
		);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var is = self.get_staticargs;
			var sig;

			sig = self.osc.synthfun.();
			sig = self.filter.synthfun.(sig);
			sig = self.master.synthfun.(sig);

			sig;

		}
	
	},
);

~class_ci_moscfaderfilter = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_moscfaderfilter;
		self.simple_args = (gate:1, doneAction:2);

		self.osc = ~class_ci_oscfader.new(main, player, self.make_namer);
		self.filter = ~class_ci_filter.new(main, player);
		self.master = ~class_ci_master_env.new(main, player);

		self.build_data;
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		self.ordered_args = self.master.get_ordered_args ++ self.osc.get_ordered_args ++ self.filter.get_ordered_args;
		self.static_data = IdentityDictionary.new;
		self.static_data.putAll(self.osc.get_static_data);
		self.static_data.putAll(self.filter.get_static_data);
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout: { arg self;
		self.layout = HLayout(
			[self.osc.make_layout, stretch:0],
			[self.filter.make_layout, stretch:0],
			[self.master.make_layout, stretch:0],
			nil,
		);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var is = self.get_staticargs;
			var sig;

			sig = self.osc.synthfun.();
			//sig = self.filter.synthfun.(sig);
			sig = self.master.synthfun.(sig);

			sig;

		}
	
	},
);
		~rah = (
			new: { arg sel, funi;
				sel = sel.deepCopy;
				sel.funi = [funi];
				sel;
			},
			make_layout: { arg sel;
				sel.funi[0].value
				//{self.master.make_layout_env}.value
			}

		);

~class_ci_moscfilter_modfx = (
	parent: ~class_ci_moscfilter,
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_moscfilter;
		self.simple_args = (gate:1, doneAction:2);

		self.osc = ~class_ci_osc.new(main, player);
		self.filter = ~class_ci_filter.new(main, player);
		self.master = ~class_ci_master_dadsr.new(main, player);

		self.tab_panel = ~class_ci_tabs_modfx.new(self, main, player, 
			[
				"Master Env", {  self.master.make_layout_env },
			]
		);

		self.build_data;
	
		self;
	},

	make_layout: { arg self;
		var layout = ~class_ci_moscfilter[\make_layout].(self);
		self.layout = VLayout(
			layout,
			self.tab_panel.make_layout,
			nil,
		);
		self.layout;
		
	},
);

~class_ci_oscmaster = (
	parent: ~class_ci_moscfilter,
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_moscfilter;
		self.simple_args = (gate:1, doneAction:2);

		self.osc = ~class_ci_osc.new(main, player);
		self.filter = ~class_ci_filter.new(main, player);
		self.master = ~class_ci_master_dadsr.new(main, player);

		//self.tab_panel = ~class_ci_tabs_modfx.new(self, main, player, 
		//	[
		//		"Master Env", {  self.master.make_layout_env },
		//	]
		//);

		self.build_data;
	
		self;
	},

	make_layout: { arg self;
		var layout = ~class_ci_moscfilter[\make_layout].(self);
		self.layout = VLayout(
			layout,
			self.master.make_layout_env,
			nil,
		)
		
	},
);

~class_ci_osc3filter2 = (
	new: { arg self, main, player, namer;
		var oscs;
		var make_oscs = { arg self;
			oscs = 3.collect { arg idx;
				~class_ci_oscfader.new(main, player, self.make_namer("osc%_".format(idx)));
			};
			oscs = oscs ++ [
				//~class_ci_noise.new(main, player, self.make_namer("noise1_"));
				~class_ci_wrapfader.new(main, player, self.make_namer("noise1_"), ~class_ci_noise);
			];

		};


		~class_ci_gens_filter2.new(main, player, namer, make_oscs);
	

	},
);

~class_ci_samplerfilter2 = (
	new: { arg self, main, player, namer;
		var oscs;
		var make_oscs = { arg self;
			oscs = [
				~class_ci_wrapfader.new(main, player, self.make_namer, ~class_ci_sampler),
				~class_ci_oscfader.new(main, player, self.make_namer("osc_"))
			];
			oscs = oscs ++ [
				//~class_ci_noise.new(main, player, self.make_namer("noise1_"));
				~class_ci_wrapfader.new(main, player, self.make_namer("noise1_"), ~class_ci_noise);
			];
			oscs

		};


		~class_ci_gens_filter2.new(main, player, namer, make_oscs);
	

	},
);

~class_ci_op_matrix = (
	parent: ~class_instr,

	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		"maisou1".debug;

		self.op_names = ["A","B","C","D","E", "F"];
		self.operators = self.op_names.collect { arg name;
		//self.operators = ["A"].collect { arg name;
			~class_ci_operator.new(main, player, self.make_namer("op%_".format(name)))
		};
		"maisou2".debug;

		self.master = ~class_ci_master_dadsr.new(main, player);
		"maisou3".debug;

		self.build_data;
		"maisou4".debug;
		self.simple_args = (gate:1, doneAction:2);
		"maisou5".debug;
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		debug("~class_ci_op_matrix: build_data");
		self.help_build_data(
			self.operators ++ [
				//self.make_control_params([
				//	[\amp, \amp, 0.1]
				//]) ++ [
				[
					opmatrix: ~class_param_opmatrix_controller.new(\opmatrix, \unipolar, self.op_names.size@(self.op_names.size+2)),
				],
				self.master,
			],
			self.operators ++ [

			]
		)
	},

	make_layout: { arg self;
		var opcount = self.op_names.size;
		var make_cell;
		var make_opcell;

		"makelayout1".debug;

		make_cell = { arg x, y;
			var val;
			val = self.data[\opmatrix].get_cell_val(x,y);
			if(val == -1) {
				val == ""
			};
			NumberBox.new
				.value_( val )
				//.string_("")
				.clipLo_(0)
				.clipHi_(1)
				.step_(0.01)
				.scroll_step_(0.1)
				.ctrl_scale_(0.1)
				.minDecimals_(2)
				.maxDecimals_(3)
				.action_({ arg field;
					var val;
					if(field.value == 0) {
						field.background = Color.white;
					} {
						field.background = Color.gray(0.5);
					};
					val = field.value;
					self.data[\opmatrix].set_cell_val(x,y, val)
				})
		};

		make_opcell = { arg x, y;
			View.new
				.layout_( HLayout(
					StaticText.new.string_(self.op_names[y]),
					make_cell.(x, y)
				))
				.background_(Color.gray)
				//.minWidth_(150)
		};
		"makelayout2".debug;

		self.matrix_layout = GridLayout.rows(*
			opcount.collect { arg y;
				opcount.collect { arg x;
					if(x == y) {
						make_opcell.(x,y);
					} {
						make_cell.(x,y);
					};
				};
			} ++ [
				opcount.collect { arg x;
					make_cell.(x,opcount);
				},
				opcount.collect { arg x;
					make_cell.(x,opcount+1);
				},
			];
		);
		"makelayout2h".debug;

		self.oplayout = VLayout(*
			self.operators.collect({ arg op; op.make_layout }),
		);
		self.layout = HLayout(
			self.oplayout,
			VLayout(
				self.matrix_layout,
				self.master.make_layout,
				self.master.make_layout_env,
				nil,
			),
			nil
		);
		"makelayout3h".debug;
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i;
			var sig;
			var op_ins, op_outs;
			var opcount = self.op_names.size;
			var ops = Array.newClear(opcount);
			var opmatrix;
			opmatrix = \opmatrix.kr(
				Array.fill(opcount * (opcount + 2), 0);
			);
			if(args.isNil) { args = () };
			args[\opmatrix] =  opmatrix;
			i = self.get_synthargs(args);
			"class_ci_op_matrix: synthfun".debug;

			op_ins = LocalIn.ar(opcount);
			self.operators.do { arg op, idx;
				var in = 0;
				idx.debug("op do idx");
				idx.do { arg i;
					var sfactor;
					var factor;
					sfactor = self.data[\opmatrix].get_cell_val(i, idx);
					if(sfactor >= 0) {
						factor = opmatrix[i + (idx * opcount)];
						in = in + (ops[i] * factor)
					}
				};
				(opcount - idx).do { arg i;
					var sfactor;
					var factor;
					sfactor = self.data[\opmatrix].get_cell_val(i + idx, idx);
					if(sfactor >= 0) {
						factor = opmatrix[i + idx + (idx * opcount)];
						in = in + (op_ins[i+idx] * factor)
					}
				};
				ops[idx] = op.synthfun.(in, args);
			};
			"fin".debug;
			LocalOut.ar(ops);

			"fin2".debug;
			sig = 0;
			opcount.do { arg idx;
				var factor;
				var sfactor;
				sfactor = self.data[\opmatrix].get_cell_val(idx, opcount);
				if(sfactor >= 0) {
					factor = opmatrix[opcount * opcount + idx];
					sig = sig + ( ops[idx] * factor );
				};
			};
			"fin3".debug;

			//sig = self.operators[0].synthfun.(0, args);
			sig = self.master.synthfun.(sig);
			"fin4".debug;

			//sig = sig * i.amp;

			sig;

		}
	
	},

);

~class_ci_op_matrix2 = (
	parent: ~class_instr,

	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		"maisou1".debug;

		self.op_names = ["A","B","C","D","E", "F"];
		self.extra_op_names = ["F1", "F2", "X1", "X2"];
		self.xop_names = self.op_names ++ self.extra_op_names;
		self.matrix_size = self.xop_names.size @ (self.xop_names.size + 2);

		self.operators = self.op_names.collect { arg name;
		//self.operators = ["A"].collect { arg name;
			~class_ci_operator.new(main, player, self.make_namer("op%_".format(name)), name)
		};
		"maisou2".debug;


		self.insertfxs = 2.collect { arg i;
			~class_ci_insertfx.new(main, player, self.make_namer("fx%_".format(i)));
		};

		self.filters = 2.collect { arg i;
			~class_ci_filter.new(main, player, self.make_namer("filter%_".format(i)));
		};

		self.xoperators = [
			self.operators,
			self.filters,
			self.insertfxs,
		].flatten;

		self.master = ~class_ci_master_dadsr.new(main, player, true);
		"maisou3".debug;

		self.tab_panel = ~class_ci_tabs_modfx.new(self, main, player, 
			[
				"Master Amp", {  HLayout(self.master.make_layout) },
				"Master Env", {  self.master.make_layout_env },
				"Filters", {
					HLayout(*
						self.filters.collect { arg mod;
							mod.make_layout;
						};
					);
				},
				"insertFXs", {  
					HLayout(*
						self.insertfxs.collect { arg fx;
							fx.make_layout;
						};
					);
				},
			]
		);


		self.build_data;
		"maisou4".debug;
		self.simple_args = (gate:1, doneAction:2);
		"maisou5".debug;
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		debug("~class_ci_op_matrix: build_data");
		self.help_build_data2(
			[
				self.xoperators,
				self.master,
			].flatten,
			[
				opmatrix: ~class_param_opmatrix_controller.new(\opmatrix, \unipolar, self.matrix_size),
			],
		);
	},

	make_layout: { arg self;
		var opcount = self.xop_names.size;
		var make_cell;
		var make_opcell;

		"makelayout1".debug;

		make_cell = { arg x, y, pancell=false;
			var val;
			var low = if(pancell) { -1 } { 0 };
			val = self.data[\opmatrix].get_cell_val(x,y);
			if(val == -1) {
				val == ""
			};
			NumberBox.new
				//.string_("")
				.clipLo_(low)
				.clipHi_(1)
				.step_(0.01)
				.scroll_step_(0.1)
				.ctrl_scale_(0.1)
				.minDecimals_(2)
				.maxDecimals_(3)
				.font_(Font("Arial",11))
				.action_({ arg field;
					var val;
					if(field.value == 0) {
						field.background = Color.white;
					} {
						field.background = Color.gray(0.5);
					};
					val = field.value;
					self.data[\opmatrix].set_cell_val(x,y, val)
				})
				.valueAction_( val )
		};

		make_opcell = { arg x, y;
			View.new
				.layout_( 
					HLayout(
						StaticText.new
							.font_(Font("Arial",11))
							.string_(self.xop_names[y]),
						make_cell.(x, y)
					).margins_(1)
				)
				.background_(Color.gray)
				//.minWidth_(150)
		};
		"makelayout2".debug;

		self.matrix_layout = GridLayout.rows(*
			opcount.collect { arg y;
				opcount.collect { arg x;
					if(x == y) {
						make_opcell.(x,y);
					} {
						make_cell.(x,y);
					};
				};
			} ++ [
				opcount.collect { arg x;
					make_cell.(x,opcount);
				},
				opcount.collect { arg x;
					make_cell.(x,opcount+1, true);
				},
			];
		);
		"makelayout2h".debug;

		self.oplayout = VLayout(*
			self.operators.collect({ arg op; op.make_layout }),
		);
		self.layout = HLayout(
			self.oplayout,
			VLayout(
				self.matrix_layout,
				//self.master.make_layout,
				//self.master.make_layout_env,
				self.tab_panel.make_layout,
				nil,
			),
			nil
		);
		"makelayout3h".debug;
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i;
			var sig;
			var sigarr;
			var op_ins, op_outs;
			var opcount = self.xop_names.size;
			var ops = Array.newClear(opcount);
			var opmatrix;
			opmatrix = \opmatrix.kr(
				Array.fill(opcount * (opcount + 2), 0);
			);
			if(args.isNil) { args = () };
			args[\opmatrix] =  opmatrix;
			i = self.get_synthargs(args);
			"class_ci_op_matrix: synthfun".debug;

			op_ins = LocalIn.ar(opcount);
			self.xoperators.do { arg op, idx;
				var in = 0;
				idx.debug("op do idx");
				idx.do { arg i;
					var sfactor;
					var factor;
					sfactor = self.data[\opmatrix].get_cell_val(i, idx);
					if(sfactor >= 0) {
						factor = opmatrix[i + (idx * opcount)];
						in = in + (ops[i] * factor)
					}
				};
				(opcount - idx).do { arg i;
					var sfactor;
					var factor;
					sfactor = self.data[\opmatrix].get_cell_val(i + idx, idx);
					if(sfactor >= 0) {
						factor = opmatrix[i + idx + (idx * opcount)];
						in = in + (op_ins[i+idx] * factor)
					}
				};
				ops[idx] = op.synthfun.(in, args);
			};
			"fin".debug;
			LocalOut.ar(ops);

			"fin2".debug;
			sigarr = List.new;
			sig = 0;
			opcount.do { arg idx;
				var factor;
				var panpos;
				var opsig;
				var sfactor;
				sfactor = self.data[\opmatrix].get_cell_val(idx, opcount);
				if(sfactor >= 0) {
					factor = opmatrix[opcount * opcount + idx];
					panpos = opmatrix[opcount * opcount + opcount + idx];
					//sig = sig + ( ops[idx] * factor );
					opsig = Pan2.ar(( ops[idx] * factor ), panpos, 1);
					sig = sig + opsig;
					//sigarr.add( ops[idx] * factor );
				};
			};
			"fin3".debug;
			
			//sig = Splay.ar(sigarr.asArray, 1, 1, 0);
			//sig = sigarr.asArray;

			//sig = self.operators[0].synthfun.(0, args);
			sig = self.master.synthfun.(sig);
			"fin4".debug;

			//sig = sig * i.amp;

			sig;

		}
	
	},

);

~class_ci_bufosc_filt = (
	parent: ~class_instr,
	synth_rate: \ar,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_;
		self.osc = ~class_ci_osc.new(main, player, self.make_namer("osc_"));
		self.filter = ~class_ci_filter.new(main, player, self.make_namer);
		self.bufosc = ~class_ci_bufosc.new(main, player, self.make_namer);

		self.master = ~class_ci_master_dadsr.new(main, player);

		self.tab_panel = ~class_ci_tabs_modfx.new(self, main, player, 
			[
				"Master Env", {  self.master.make_layout_env },
			]
		);
		self.build_data;

		self.simple_args = (freq:\void, gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;

		self.help_build_data2(
			[
				self.osc,
				self.bufosc,
				self.filter,
				self.master,
			],
			self.make_control_params([
				[\freq, \freq, 200],
			])
		);
		self.data.copy;
	},

	make_layout: { arg self;
		self.layout = HLayout(
			VLayout(
				self.osc.make_layout,
				self.bufosc.make_layout,
			),
			VLayout(
				HLayout(
					self.filter.make_layout,
					self.master.make_layout,
				),
				self.tab_panel.make_layout,
			)
		);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			
			//sig = SinOsc.ar(i.freq);
			sig = self.osc.synthfun.((freq:i.freq));
			sig = self.bufosc.synthfun.(sig, args);
			sig = self.filter.synthfun.(sig, args);
			sig = self.master.synthfun.(sig, args);
			sig;
		}
	},

);

~class_ci_bufosc_filt_spread = (
	parent: ~class_instr,
	synth_rate: \ar,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_;
		self.osc = ~class_ci_osc.new(main, player, self.make_namer("osc_"));
		self.filter = ~class_ci_filter.new(main, player, self.make_namer);
		self.bufosc = ~class_ci_bufosc.new(main, player, self.make_namer);

		self.insertfxs = 2.collect { arg i;
			~class_ci_insertfx.new(main, player, self.make_namer("fx%_".format(i)));
		};

		self.voices_keys = [\pitch_spread, \wt_pos_spread, \range_spread, \finepos_spread, \osc_intensity_spread, \arg1_spread];
		self.voices_panel = ~class_voices_panel.new(self, self.voices_keys);

		self.master = ~class_ci_master_dadsr.new(main, player);

		self.tab_panel = ~class_ci_tabs_modfx.new(self, main, player, 
			[
				"Master Env", {  self.master.make_layout_env },
				"Spread", {  self.voices_panel.make_layout },
				"Routing", {  self.make_layout_routing },
			]
		);
		self.build_data;

		self.simple_args = (freq:\void, gate:1, doneAction:2);
	
		self;
	},

	get_route_insfx_variants: { arg self;
		[
			(
				name: "After osc",
				uname: \after_osc,
			),
			(
				name: "After Bufosc",
				uname: \after_bufosc,
			),
			(
				name: "Before pan",
				uname: \before_pan,
			),
		]
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		var static_voices_params = ();
		var static_data;

		self.voices_keys.do { arg key;
			var enable_key = "enable_%".format(key).asSymbol;
			static_voices_params[enable_key] = 
				~class_param_static_controller.new(enable_key, specs[\onoff], 0);
		};

		static_data = (
			voices: ~class_param_static_controller.new(\voices, ControlSpec(1,16,\lin,1), 1),
			spread_kind: ~class_param_kind_chooser_controller.new(\spread_kind, self.get_spread_kind_variants, "Spread kind"),
			route_insertfx1: ~class_param_kind_chooser_controller.new(\route_insertfx1, self.get_route_insfx_variants, "Insert Fx 1"),
			route_insertfx2: ~class_param_kind_chooser_controller.new(\route_insertfx2, self.get_route_insfx_variants, "Insert Fx 2"),
		);
		static_data.putAll(static_voices_params);


		self.help_build_data2(
			[
				self.osc,
				self.bufosc,
				self.filter,
				self.master,
				self.insertfxs,
			].flatten,
			self.make_control_params([
				[\freq, \freq, 200],
			] ++ self.voices_keys.collect { arg key;
				[key, \bipolar, 0]
			}) ++ [
			],
			static_data
		);
		self.data.copy;
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
		[wtrange, wtpos, i.enable_wt_pos_spread, i.voices, i.wt_pos_spread, i].debug("build_wtpos_spread_array");
		if(i.enable_wt_pos_spread == 1) {
			var array = self.build_spread_array_by_kind(i.voices, i.spread_kind);
			[array, (wtrange * array)].debug("build_wtpos_spread_array: array, mul");
			wtpos = (wtpos + (i.wt_pos_spread * wtrange * array));
		} {
			wtpos = wtpos ! i.voices;
		};
		wtpos;
	},

	build_spread_array_for_param: { arg self, i, key;
		var enabled_key = "enable_%_spread".format(key).asSymbol;
		var enabled = i[enabled_key].();
		var paramval = i[key].();
		var param = self.param[key];
		var param_spread = i["%_spread".format(key).asSymbol].();
		var res;
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

	make_layout: { arg self;
		self.layout = HLayout(
			VLayout(*[
				self.osc.make_layout,
				self.bufosc.make_layout,
				self.insertfxs.collect { arg fx; fx.make_layout },
			].flatten),
			VLayout(
				HLayout(
					self.filter.make_layout,
					self.master.make_layout,
				),
				self.tab_panel.make_layout,
			)
		);
		self.layout;
	},

	make_layout_routing: { arg self;
		var route_data = [
			\route_insertfx1,
			\route_insertfx2,
		].collect({ arg x; self.static_data[x] });

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
		VLayout(
			routing_layout,
			nil
		);
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
			var sig;
			var oscargs = self.osc.get_synthargs;

			args = args ?? ();
			args.freq = self.build_freq_spread_array(i, i.freq);
			args.wt_pos = self.build_wt_pos_spread_array(i, oscargs.wt_range, oscargs.wt_pos);
			args.range = self.build_spread_array_for_param(i, \range);
			args.finepos = self.build_spread_array_for_param(i, \finepos);
			args.arg1 = self.build_spread_array_for_param(i, \arg1);
			args.intensity = self.build_spread_array_for_param(i, \osc_intensity);
			
			sig = self.osc.synthfun.(args);

			sig = self.insert_effect(i, sig, \after_osc);

			sig = self.bufosc.synthfun.(sig, args);

			sig = self.insert_effect(i, sig, \after_bufosc);

			sig = self.filter.synthfun.(sig, args);

			sig = self.insert_effect(i, sig, \before_pan);

			sig = self.master.synthfun.(sig, args);
			sig;
		}
	},

);

////////////// Modulators

~class_ci_mod_osc_propor = (
	parent: ~class_ci_osc,
	make_layout: { arg self;
		self.layout = HLayout(
			~class_ci_osc[\make_layout].(self)
		);
		self.layout;
	},

);

~class_ci_mod_osc = (
	parent: ~class_ci_osc,
	synth_rate: \kr,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_osc;
		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
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

		
		self.ordered_args = [
			modfreq: ~make_control_param.(main, player, \modfreq, \scalar, 200, \lofreq.asSpec),
			//detune: ~make_control_param.(main, player, \detune, \scalar, 0, specs[\pitch]),
			wt: wt,
			wt_pos: wt_pos,
			intensity: ~make_control_param.(main, player, \intensity, \scalar, 0, \unipolar.asSpec),
			oscamp: ~make_control_param.(main, player, \oscamp, \scalar, 0.5, \amp.asSpec),
		];
		self.static_data = IdentityDictionary.newFrom((
			spectrum: ~class_param_kind_chooser_controller.new(\spectrum, self.get_spectrum_variants),
			wt_range: wt_range,
			wt_classic: wt_classic,
			enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
		));
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		//self.data[\detune].scalar.set_bus_mode(true);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\modfreq, \wt_pos, \intensity, \oscamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new("Osc1", self.knobs, self.static_data[\enabled], self.data[\wt], self.static_data[\spectrum]);
		self.layout = HLayout(frame_view.layout);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			i.debug("III");
			args.debug("ARGS");
			i.freq.debug("FREQ");

			sig = Instr(\ci_oscillator).value((
				midinote:i.modfreq.cpsmidi, 
				detune:0,
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

~class_ci_mod_envosc = (
	parent: ~class_instr,
	synth_rate: \kr,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_mod_envosc;

		self.osc = ~class_ci_operator.new(main, player, self.make_namer, "");
		self.osc.gui_knobs =  [\amp, \modfreq, \phase];

		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;

		self.help_build_data2(
			[
				self.osc
			],
			self.make_control_params([
				[\modfreq, \lofreq, 1],
			])
		);
		self.osc.data[\modfreq] = self.data[\modfreq];
		self.data.copy;
	},

	make_layout: { arg self;
		self.layout = HLayout(
			self.osc.make_layout;
		);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			
			sig = self.osc.synthfun.(0, (freq:i.modfreq, offset:0, ratio:1));
			//sig = SinOsc.kr(i.modfreq);
			sig;
		}
	},

);

~class_ci_selfgated_env = (
	parent: ~class_instr,
	synth_rate: \kr,
	modulation_kind: \pattern,
	player_mode: \scoreline,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_;

		self.compenv = ~class_ci_composite_env.new(main, player, self.make_namer);

		self.build_data;

		self.simple_args = (gate:1, doneAction:2);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;

		self.help_build_data2(
			[
				//self.insertfxs,
				self.compenv
			],
			[
				gtrig: ~class_param_gtrig.new(\gtrig)
			] ++
			self.make_control_params([
				[\tesustain, specs[\sustain], 0.25],
			])
		);
		self.data.copy;
	},

	make_layout: { arg self;
		self.track_display = ~class_track_display.new;
		self.curve_track_controller = ~class_step_track_controller.new(self.get_player, self.track_display);
		self.layout = VLayout(
			self.curve_track_controller.make_gui,
			~class_ci_modknob_view.new(self.data[\tesustain]).layout,
			self.compenv.make_layout
		);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			var envgate;


			envgate = Trig.kr(i.gtrig,i.tesustain);

			sig = self.compenv.synthfun.((gate:envgate, doneAction:0));
			//sig = self.compenv.synthfun.(args);
			//sig.poll;
			
			sig;
		}
	},

);

////////////// Effects

~class_ci_insertfx3 = (
	// used as an effect
	parent: ~class_instr,
	is_effect: true,

	new: { arg self, main, player, namer;
		self = self.deepCopy;

		debug("waf1");
		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };

		debug("waf2");
		self.insertfxs = 3.collect { arg i;
			debug("waf2_"++i);
			~class_ci_insertfx.new(main, player, self.make_namer("fx%_".format(i)));
		};
		debug("waf3");

		self.build_data;
		debug("waf4");
		//self.simple_args = (gate:1, doneAction:2);
		self.simple_args = (gate:1, in: ~silent_audio2_bus.index);
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		var specs = self.get_specs;
		debug("~class_ci_insertfx3: build_data");
		self.help_build_data2(
			self.insertfxs,
			self.make_control_params([
				[\mix, \unipolar, 0.5],
			])
		);
		//self.data.keys.debug("class_ci_insertfx3: build_data: keys");
		self.data.copy;
	},

	make_layout: { arg self;
		self.layout = HLayout( *
			self.insertfxs.collect { arg fx; fx.make_layout }
		);
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			var in;
			
			in = In.ar(i.in, 2);

			sig = in;

			self.insertfxs.collect { arg fx;
				sig = fx.synthfun.(sig, args)
			};

			sig = SelectX.ar(i.mix, [in, sig]);
			sig;

		}
	
	},
);

//////////////////////////////////////////////////////

~classinstr_lib = (
	sin: ~class_ci_sin,
	oscmaster: ~class_ci_oscmaster,
	osc: ~class_ci_osc,
	mosc: ~class_ci_mosc,
	moscfilter: ~class_ci_moscfilter,
	moscfilter_modfx: ~class_ci_moscfilter_modfx,
	moscfaderfilter: ~class_ci_moscfaderfilter,
	osc3filter2: ~class_ci_osc3filter2,
	samplerfilter2: ~class_ci_samplerfilter2,
	op_matrix: ~class_ci_op_matrix,
	op_matrix2: ~class_ci_op_matrix2,
	bufosc_filt: ~class_ci_bufosc_filt,
	bufosc_filt_spread: ~class_ci_bufosc_filt_spread,

	// modulators

	dadsr_kr: ~class_ci_dadsr_kr,
	custom_env: ~class_ci_custom_env,
	mod_osc: ~class_ci_mod_osc,
	mod_envosc: ~class_ci_mod_envosc,
	selfgated_env: ~class_ci_selfgated_env,

	// effects

	insertfx3: ~class_ci_insertfx3,

);

//////////////////////////////////////////////////////
////////////// Instrs
//////////////////////////////////////////////////////

Instr(\ci_oscillator, { 
	arg spectrum, wt_range=0, wt_classic=\void, amp=0.1, midinote=200, wt=0, detune=0.0, wt_position=0, intensity=1, phase=nil;
	var ou, endfreq;
	var formantfreq;
	var mul = 1;
	var width = 0.5;
	endfreq = (midinote + detune).midicps;
	//endfreq.poll;
	//spectrum.debug("spectrum");
	wt_position.debug("WT_POSITION");
	switch(spectrum,
		\bend, {
			endfreq = SinOsc.ar(endfreq).range(0,8)*(intensity)*endfreq + endfreq;
			// modulo
		},
		\formant, {
			formantfreq = endfreq;
			//endfreq = formantfreq * ((intensity * 8) + 1);
			endfreq = formantfreq * (2 ** (intensity-0.5 *8).trunc);
			mul = SinOsc.ar(formantfreq);
		}
	);
	if(wt_classic != \void) {
		if(spectrum == \width) {
			width = intensity;
		};
		ou = Instr(\ci_classic_oscillator).value((kind:wt_classic, freq:endfreq, width:width, phase:phase)) * mul;
	} {
		if(phase.isNil) {
			phase = 0;
		};
		if(wt_range == 0) {
			ou = Osc.ar(wt, endfreq, phase*pi) * mul;
		} {
			ou = VOsc.ar(wt+(wt_position.clip(0,wt_range)), endfreq, phase*pi) * mul;
		};
	};
	switch(spectrum,
		\wrap, {
			//endfreq = SinOsc.ar(endfreq).range(0,8)*(intensity)*endfreq + endfreq;
			// modulo
			ou = (ou * (intensity*2+1)).wrap(-1,1)
		},
		\fold, {
			//endfreq = SinOsc.ar(endfreq).range(0,8)*(intensity)*endfreq + endfreq;
			// modulo
			ou = (ou * (intensity*2+1)).fold(-1,1)
		},
		\clip, {
			//endfreq = SinOsc.ar(endfreq).range(0,8)*(intensity)*endfreq + endfreq;
			// modulo
			ou = (ou * (intensity*2+1)).clip(-1,1)
		},
	);
	ou = ou * amp;
	//ou.poll;
	//[freq, detune, amp].debug("p_oscillator: frq, detune, amp");
	ou;
}, [NonControlSpec(), NonControlSpec(), NonControlSpec()]);

Instr(\ci_classic_oscillator, { arg kind=\void, freq=200, phase=nil, width=0.5;
	if(phase.isNil) {
		phase = 0;
	};
	switch(kind,
		\SinOsc, {
			SinOsc.ar(freq, phase*pi)
		},
		\LFSaw, {
			LFSaw.ar(freq, phase, width)
		},
		\LFPulse, {
			LFPulse.ar(freq, phase/2, width)
		},
		\LFTri, {
			LFTri.ar(freq, phase*2)
		},
		{
			"WARNING: ci_classic_oscillator: kind not found".debug;
			SinOsc.ar(freq, phase*pi)
		}
	);
}, [NonControlSpec()]);

Instr(\ci_filter, { arg in, kind, arg1, arg2, arg3, freq;
	var sig;
	var compute_arg1 = { (arg1+freq.cpsmidi).clip(0,128).midicps;  };
	//kind.debug("p_filter: kind");
	sig = switch(kind,
		\lpf, {
			arg1 = compute_arg1.();
			LPF.ar(in, arg1);
		},
		\bpf, {
			arg1 = compute_arg1.();
			BPF.ar(in, arg1, arg2)
		},
		\hpf, {
			arg1 = compute_arg1.();
			HPF.ar(in, arg1)
		},
		\rlpf, {
			arg1 = compute_arg1.();
			RLPF.ar(in, arg1, arg2)
		},
		\rhpf, {
			arg1 = compute_arg1.();
			RHPF.ar(in, arg1, arg2)
		},
		\lpdm1, {
			arg1 = compute_arg1.();
			DFM1.ar(in, arg1, arg2, 1, 0, arg3);
		},
		\hpdm1, {
			arg1 = compute_arg1.();
			DFM1.ar(in, arg1, arg2, 1, 1, arg3);
		},
		\mooglader, {
			arg1 = compute_arg1.();
			MoogLadder.ar(in, arg1, arg2);
		},
		\moogff, {
			arg1 = compute_arg1.();
			MoogFF.ar(in, arg1, arg2, arg3);
		},
		\ramp, {
			Ramp.ar(in, arg1);
		},
		\comb, {
			CombL.ar(in, arg1, arg2, arg3);
		}
	);
	sig;

},[\audio, NonControlSpec()]);

/////////// insert effects

Instr(\ci_insertfx, { arg kind, in, arg1, arg2, arg3, ktr;
	var sig;
	//kind.debug("p_effect: kind");
	sig = switch(kind,
		\freqshift, {
			Instr(\p_freqshift).value((in:in, mix:arg1, shift:arg2, ktr:ktr));
		},
		\simpledelay, {
			Instr(\p_simpledelay).value((in:in, mix: arg1, delay:arg2));
		},
		\samplehold, {
			Instr(\p_samplehold).value((in:in, mix: arg1, pitch:arg2, ktr:ktr));
		},
		\bitcrusher, {
			Instr(\p_bitcrusher).value((in:in, mix: arg1, crush:arg2));
		},
		\decimator, {
			Instr(\p_decimator).value((in:in, mix: arg1, samplerate:arg2, bitdepth:arg3));
		},
		\smoothdecimator, {
			Instr(\p_smoothdecimator).value((in:in, mix: arg1, samplerate:arg2, smooth:arg3));
		},
		\simplefilter, {
			Instr(\p_simplefilter).value((in:in, hpfreq:arg1, lpfreq:arg2, ktr:ktr));
		},
		\sinshaper, {
			Instr(\p_sinshaper).value((in:in, mix: arg1, drive:arg2));
		},
		\parashaper, {
			Instr(\p_parashaper).value((in:in, mix: arg1, drive:arg2));
		},
		\hardclipper, {
			Instr(\p_hardclipper).value((in:in, mix: arg1, drive:arg2));
		},
		{
			kind.debug("p_ins_effect: ERROR: effect kind not found");
			in;
		}
	);
	//[in, sig].debug("p_ins_effect: in sig");
	sig;

}, [NonControlSpec(), \audio]);



Instr(\p_freqshift, { arg in, mix, shift, ktr=0;
	var sig;
	sig = FreqShift.ar(in, (shift + ktr).midicps);
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

Instr(\p_simpledelay, { arg in, mix, delay;
	var sig;
	sig = DelayL.ar(in, delay, delay);
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

Instr(\p_samplehold, { arg in, mix, pitch, ktr=0;
	var sig;
	var gate;
	gate = LFPulse.ar((pitch + ktr).midicps);
	sig = Gate.ar(in, gate);
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

Instr(\p_bitcrusher, { arg in, mix, crush;
	var sig;
	sig = Decimator.ar(in, SampleRate.ir*(crush/31), crush);
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

Instr(\p_decimator, { arg in, mix, samplerate, bitdepth;
	var sig;
	sig = Decimator.ar(in, SampleRate.ir * samplerate, bitdepth * 32);
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

Instr(\p_smoothdecimator, { arg in, mix, samplerate, smooth;
	var sig;
	sig = Decimator.ar(in, SampleRate.ir * samplerate, smooth);
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

Instr(\p_simplefilter, { arg in, lpfreq, hpfreq, ktr=0;
	var sig;
	sig = LPF.ar(in, (ktr + lpfreq).midicps);
	sig = HPF.ar(sig, (ktr + hpfreq).midicps);
}, [\audio]);

Instr(\p_sinshaper, { arg in, mix, drive;
	var sig;
	sig = SineShaper.ar(in, 1-drive);
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

Instr(\p_parashaper, { arg in, mix, drive;
	var sig;
	//TODO
	sig = SineShaper.ar(in, 1-drive);
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

Instr(\p_hardclipper, { arg in, mix, drive;
	/// FIXME: fucking too strange bug
	var sig;
	//mix.poll;
	drive = 1-drive;
	//sig = in.clip(0-drive, drive);
	//drive = 0.001;
	//sig = in.clip(0-drive,drive);
	sig = Clip.ar(in, 0-drive, drive);
	//sig.poll;
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

/////////// effects

Instr(\p_reverb, { arg in, mix, room, damp, gate=1, amp=1;
	var sig;
	in = In.ar(in, 2);
	sig = FreeVerb.ar(in, mix, room, damp);
	sig = sig * amp;
	//sig.poll;
}, [\audio]).storeSynthDef([\ar]);


Instr(\p_flanger, { arg in, fbbus, mix, rate, feedback, depth, gate=1;
	var sig;
	var maxdelay = depth;
	var lfo;
	var ou;
	in = In.ar(in, 2);
	ou = Fb({ arg fb;
		fb = fb * feedback;
	
		lfo = SinOsc.kr(rate).range(0,1)*depth;
		sig = DelayL.ar(in+fb, maxdelay, lfo);
		ou = SelectX.ar(mix, [in, sig]);
	});
	ou;
}, [\audio])
.storeSynthDef([\ar], metadata:(
	specs: (
		rate: ControlSpec(0,4,\lin,0,0),
		depth: ControlSpec(0,4,\lin,0,0),
	)

));

Instr(\p_chorus, { arg in, mix=0, rate, offset, depth, gate=1;
	var sig;
	var lfo;
	var delay = [10,15,20,25]/1000;
	in = In.ar(in, 2);
	lfo = SinOsc.ar(rate).range(0,1) * depth;
	delay = delay * lfo;
	sig = in;
	sig = DelayL.ar(in, delay+depth+0.01, delay+lfo+offset);
	sig = sig.sum;
	SelectX.ar(mix, [in, sig]);
}, [\audio])
.storeSynthDef([\ar], metadata:(
	specs: (
		rate: ControlSpec(0.0000001,4,\exp,0,0),
		depth: ControlSpec(0.0000001,4,\exp,0,0),
		offset: ControlSpec(0.0000001,1,\exp,0,0),
	)
));

//Instr(\p_phaser, { arg in, mix=0, rate, feedback, depth;
//	var sig;
//	var maxdelay = depth;
//	var lfo;
//	var ou;
//	var rq = 1;
//	var bands = [200,400,800,1600,4000,8000];
//	ou = in + Fb({ arg fb;
//		var fbou;
//		fb = fb * feedback;
//
//		lfo = SinOsc.kr(rate).range(0,1)*depth;
//
//		fbou = Mix.fill(bands, { arg freq;
//			sig = fb;
//			sig = BPF.ar(sig, freq, rq);
//			sig = DelayL.ar(in, maxdelay, lfo);
//		});
//		fbou = SelectX.ar(mix, [in, ou]);
//		fbou;
//	});
//	ou;
//}, [\audio])
//	.storeSynthDef([\ar]);

Instr(\p_delay, { arg in, mix, damp, delay_left, delay_right, gate=1;
	var sig;
	var sigl, sigr;
	in = In.ar(in, 2);
	sig = DelayL.ar(in, [delay_left,delay_right], [delay_left, delay_right]);
	sig = LPF.ar(sig, damp);
	SelectX.ar(mix, [in, sig]);
}, [\audio])
.storeSynthDef([\ar], metadata:(
	specs: (
		damp: ControlSpec(0,4,\lin,0,0),
		delay_left: ControlSpec(0,4,\lin,0,0),
		delay_right: ControlSpec(0,1,\lin,0,0),
	)
));


Instr(\p_comb, { arg in, mix, delay, offset, decay, gate=1;
	var sig;
	var sigl, sigr;
	in = In.ar(in, 2);
	sig = CombL.ar(in, 0.4, LPF.kr([delay, delay+offset],1), decay);
	//CheckBadValues.ar(sig,0,1);
	SelectX.ar(mix, [in, sig]);
}, [\audio])
//.storeSynthDef([\ar], metadata:(
.storeSynthDef([], metadata:(
	specs: (
		delay: ControlSpec(0,4,\lin,0,0),
		decay: ControlSpec(0,4,\lin,0,0),
		offset: ControlSpec(0,1,\lin,0,0),
	)
));

///////////////////////////////////

Instr(\ci_noise, { arg kind, amp=0.1;
	//TODO
	var sig;
	sig = switch(kind,
		\white, {
			WhiteNoise.ar(amp);
		},
		\pink, {
			PinkNoise.ar(amp);
		},
		\brown, {
			BrownNoise.ar(amp);
		},
		\gray, {
			GrayNoise.ar(amp);
		},
		\clip, {
			ClipNoise.ar(amp);
		},
		{
			//kind.debug("p_noise: ERROR: noise kind not found");
			WhiteNoise.ar(amp);
		}
	);
	sig;

}, [NonControlSpec()]);
