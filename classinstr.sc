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
				self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\classinstr, self);
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
	},

	make_bindings: { arg self;
		self.get_main.commands.parse_action_bindings(\classinstr, 
		
			self.get_main.panels.side.get_shared_bindings ++
			self.get_main.panels.side.get_windows_bindings ++ [

			[\close_window, { arg self;
				self.window.close;
			}],

			[\play_selected, { arg self;
				self.get_player.play_node;
			}], 

			[\stop_selected, { arg self;
				self.get_player.stop_node;
			}],

			[\edit_selected_param, { arg self;
				if(~global_controller.current_param.notNil) {
					debug("class_instr: edit_selected_param");
					~class_player_display.edit_param_value(self.get_main, self.get_player, ~global_controller.current_param);
				}
			}],

			[\assign_midi_knob, { arg self;
				if(~global_controller.current_param.notNil) {
					var param = ~global_controller.current_param;
					self.get_main.panels.side[\binding_assign_midi_knob].(param);
				};
			}],

			[\panic, { arg self;
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
		pitch: ControlSpec(-64,64, \lin, 0, 0, "midi"),
		rate: \widefreq.asSpec,
		glidefade: \unipolar.asSpec,
		env: ControlSpec(0, 16, 'linear', 0, 0.1, ""),
		boost: ControlSpec(-500, 100, 'lin', 0, 0),
		amp: ControlSpec(0, 1, 'amp', 0, 0.1, ""),
		wideamp: ControlSpec(0, 6, 'amp', 0, 0.1, ""),
		crush: ControlSpec(1, 31, 'lin', 1, 1, ""),
		smalldelay: ControlSpec(0, 0.02, 'lin', 0, 0.001, ""),
		envamp: ControlSpec(0, 1, 'amp', 0, 1, ""),
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
				i[name] = control_name.kr(datum.default_value);
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

		self.simple_args = (gate:1, doneAction:2);
	
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
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_master_dadsr;

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
		self.help_build_data(
			[
				self.make_control_params([
					[\amp, \amp, 0.5],
					[\pan, \pan, 0.0],
					//[\velocity, \unipolar, 0.0],
					[\ratio, ControlSpec(0.0000001, 20, \exp, 0, 1), 1],
					[\offset, ControlSpec(0.0000001, 18000, \exp, 0, 1), 0],
				]) ++ [
					wt: wt
				],
				self.dadsr
			],
			[
				(
					enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
					wt_classic: wt_classic,
					wt_range: wt_range,
				)
			]
		)
	},

	make_layout: { arg self, fader;
		var knobs = [\amp, \pan, \ratio, \offset];
		var frame_view;
		var frame;
		self.knobs = knobs.collect { arg name;
			~class_ci_simpleknob_view.new(self.data[name]).layout;
		};
		frame = View.new;
		frame.background = Color.gray(0.5);
		frame.layout = HLayout(*
			[
				VLayout(
					StaticText.new.string_(self.name),
					~class_ci_popup_view.new(self.data[\wt]).layout

				)
			] ++
			self.knobs ++
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
		menu = ~class_ci_popup_view.new(self.param[\env_kind], nil, { arg popup;
			env_view_layout.index = popup.value;
		});
		layout = VLayout(
			menu.layout,
			env_view_layout,
		);
		dadsr_view.layout = self.dadsr.make_layout;
		custom_env_view.layout = self.custom_env.make_layout;
		env_view_layout.add(dadsr_view);
		env_view_layout.add(custom_env_view);
		self.layout = layout;
		layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;

			i.doneAction.debug("==============================================DONEACTION");
			i.doneAction.poll;

			sig = switch(i.env_kind,
				\dadsr, {
					self.dadsr.synthfun.(args)
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
		];
		filtermix.set_label("Filter Mix");
		filterparseq.set_label("Par Seq");


		self.static_data = (
			route_insertfx1: ~class_param_kind_chooser_controller.new(\route_insertfx1, self.get_route_insfx_variants, "Insert Fx 1"),
			route_insertfx2: ~class_param_kind_chooser_controller.new(\route_insertfx2, self.get_route_insfx_variants, "Insert Fx 1"),
			enable_pitch_spread: ~class_param_static_controller.new(\enable_pitch_spread, specs[\onoff], 0),
			enable_wtpos_spread: ~class_param_static_controller.new(\enable_wtpos_spread, specs[\onoff], 0),
			voices: ~class_param_static_controller.new(\enable_pitch_spread, ControlSpec(1,16,\lin,1), 1),
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

	//make_layout_effects: { arg self;
	//	self.fx_ctrl = ~class_embeded_effects_controller.new(self.get_main, self.get_player);
	//	self.fx_ctrl.window = { self.window };
	//	self.fx_ctrl.layout;
	//},

	make_tab_panel: { arg self;
		self.tab_panel.make_layout;
	},

	//make_tab_panel_OLD: { arg self;
	//	var header, body, layout;
	//	var content;
	//	var modview;
	//	var custom_view = View.new;
	//	var tab_views = List.new;
	//	self.tab_custom_view = custom_view;
	//	self.modulation_controller = ~class_embeded_modulation_controller.new(self.get_main, self.get_player, nil, self.data[\filtermix]);
	//	self.modulation_controller.make_bindings;
	//	self.modulation_controller.window = { self.window };
	//	modview = ~class_embeded_modulation_view.new(self.modulation_controller);
	//	self.modulation_controller.main_view = modview;

	//	//content = [
	//	//	"Master Env", self.master.make_layout_env,
	//	//	"Routing", self.make_layout_routing,
	//	//	"Voices", self.make_layout_voices,
	//	//	"Effects", self.make_layout_effects,
	//	//];
	//	content = [
	//		"Master Env", {  self.master.make_layout_env },
	//		"Routing", {  self.make_layout_routing },
	//		"Voices", {  self.make_layout_voices },
	//		"Effects", {  self.make_layout_effects },
	//	];
	//	self.tabs_count = content.size/2;
	//	content = content.clump(2).flop;
	//	debug("NUIT 1");
	//	body = StackLayout(*
	//		content[1].collect { arg co;
	//			//View.new.layout_(co)
	//			var view;
	//			view = View.new;
	//			tab_views.add(view);
	//			view;
	//		} ++ [
	//			View.new.layout_(modview.body_layout),
	//			//custom_view,
	//		]
	//	);
	//	tab_views.do { arg view, idx;
	//		//{
	//			view.layout = content[1][idx].value;
	//			//0.01.wait;
	//		//}.defer( 1+idx )
	//	};
	//	debug("NUIT 2");
	//	layout = VLayout(
	//		HLayout(*
	//			content[0].collect { arg co, idx;
	//				debug("NUIT 3");
	//				//0.02.wait;
	//				Button.new
	//					.states_([[co]])
	//					.action_({ 
	//						body.index = idx;
	//						if(idx == 3) { //FIXME: hardcoded
	//							self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\effects);
	//						} {
	//							self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\classinstr, self);
	//						}
	//					})
	//			}
	//		),
	//		modview.tab_layout,
	//		body,
	//	
	//	);
	//	debug("NUIT 4");
	//	self.tab_panel_stack_layout = body;
	//	modview.show_body_layout = { arg myself;
	//		debug("modview: show_body_layout");
	//		self.tab_panel_stack_layout.index = self.tabs_count;
	//	};
	//	//modview.show_body_layout = { arg myself;
	//	//	var extplayer = myself.controller.get_current_player.external_player;
	//	//	myself.controller.get_current_player.uname.debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: curplayer");
	//	//	self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\modulator);
	//	//	if(extplayer.notNil) {
	//	//		// FIXME: external player should have custom gui
	//	//		self.tab_panel_stack_layout.index = 5;
	//	//		extplayer.make_layout;
	//	//		self.tab_custom_view.children.do(_.remove);
	//	//		debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: before cusheader");
	//	//		self.custom_header_view = ~class_modulator_header_view.new_without_responders(myself.controller); 
	//	//		debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: after cusheader");
	//	//		self.tab_custom_view.layout_(
	//	//			VLayout(
	//	//				[self.custom_header_view.layout, stretch:0],
	//	//				extplayer.layout,
	//	//			)
	//	//		);
	//	//		self.custom_header_view.selected_slot;
	//	//		debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: after view cusheader");
	//	//		debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: last view cusheader");
	//	//	} {
	//	//		self.tab_panel_stack_layout.index = 4;
	//	//	}
	//	//};
	//	layout;
	//},


	make_layout: { arg self;
		self.layout = HLayout(
				debug("****************************************** LAYOUT oscs");
			VLayout(*
				self.oscs.collect({arg x;[x.make_layout, stretch:0]}) ++
				[[nil, stretch:1]]
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
			f1_in = oscs[1].sum;
			f2_in = oscs[0].sum;

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
							body.index = idx;
							if(idx == (self.tabs_count-1)) { 
								self.classinstr.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\effects);
							} {
								self.classinstr.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\classinstr, self);
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
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.synthdef_name = \ci_mosc;
		self.simple_args = (gate:1, doneAction:2);

		self.osc = ~class_ci_osc.new(main, player);
		self.master = ~class_ci_master_env.new(main, player);

		self.build_data;
	
		self;
	},

	build_data: { arg self;
		var main = self.get_main;
		var player = self.get_player;
		self.ordered_args = self.master.ordered_args ++ self.osc.ordered_args;
		self.static_data = self.osc.static_data;
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

			sig = self.osc.synthfun.();
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

~class_ci_insertfx3 = (
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
	op_matrix: ~class_ci_op_matrix,

	// modulators

	dadsr_kr: ~class_ci_dadsr_kr,
	custom_env: ~class_ci_custom_env,

	// effects

	insertfx3: ~class_ci_insertfx3,

);

//////////////////////////////////////////////////////
////////////// Instrs
//////////////////////////////////////////////////////

Instr(\ci_oscillator, { arg spectrum, wt_range=0, wt_classic=\void, amp=0.1, midinote=200, wt=0, detune=0.0, wt_position=0, intensity=1;
	var ou, endfreq;
	var formantfreq;
	var mul = 1;
	endfreq = (midinote + detune).midicps;
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
		ou = Instr(\ci_classic_oscillator).value((kind:wt_classic, freq:endfreq)) * mul;
	} {
		if(wt_range == 0) {
			ou = Osc.ar(wt, endfreq) * mul;
		} {
			ou = VOsc.ar(wt+(wt_position.clip(0,wt_range)), endfreq) * mul;
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
	//[freq, detune, amp].debug("p_oscillator: frq, detune, amp");
	ou;
}, [NonControlSpec(), NonControlSpec(), NonControlSpec()]);

Instr(\ci_classic_oscillator, { arg kind=\void, freq=200, phase=0, width=0.5;
	switch(kind,
		\SinOsc, {
			SinOsc.ar(freq, phase)
		},
		\LFSaw, {
			LFSaw.ar(freq, phase)
		},
		\LFPulse, {
			LFPulse.ar(freq, phase)
		},
		\LFTri, {
			LFTri.ar(freq, phase)
		},
		{
			"WARNING: ci_classic_oscillator: kind not found".debug;
			SinOsc.ar(freq, phase)
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

Instr(\ci_insertfx, { arg kind, in, arg1, arg2, ktr;
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

Instr(\p_reverb, { arg in, mix, room, damp;
	in = In.ar(in, 2);
	FreeVerb.ar(in, mix, room, damp);
}, [\audio]).storeSynthDef([\ar]);


Instr(\p_flanger, { arg in, fbbus, mix, rate, feedback, depth;
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

Instr(\p_chorus, { arg in, mix=0, rate, offset, depth;
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

Instr(\p_delay, { arg in, mix, damp, delay_left, delay_right;
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


Instr(\p_comb, { arg in, mix, delay, offset, decay;
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
