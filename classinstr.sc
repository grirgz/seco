//////////////////////////////////////////////////////
////////////// GUI parts
//////////////////////////////////////////////////////

~class_ci_modknob_view = (
	new: { arg self, controller;
		self = self.deepCopy;

		self.controller = { controller };
		self.make_gui;

		~make_class_responder.(self, self.label, self.controller, [
			\val, \label,
		]);
	
		self;
	},

	val: { arg self;
		self.vallabel.string = self.controller.get_val.asFloat.asStringPrec(~general_sizes.float_precision);
		self.knob.value = self.controller.get_norm_val;
	},

	label: { arg self;
		self.controller.label.debug("class_ci_modknob_view: LABEL");
		self.namelabel.string = self.controller.label ?? self.controller.name;
	},

	make_gui: { arg self;
		var label;
		var knob;
		var vallabel;
		self.layout = VLayout(
			label = StaticText.new
				.string_(self.controller.label ?? self.controller.name);
				[label, stretch:0, align:\center],
			knob = ModKnob.new; 
				knob.action_({ 
					self.controller.set_norm_val(knob.value)
				});
				knob.asView.minSize_(60@60);
				[knob.asView, stretch: 0, align:\center],
			vallabel = StaticText.new
				.string_("12354")
				.font_(Font("Arial",11))
				.align_(\center)
				//.minWidth_(75);
				;
				[vallabel, stretch:0, align:\center],
			nil
		);
		self.namelabel = label;
		self.knob = knob;
		self.vallabel = vallabel;
		//self.layout.minHeight = 65;
		self.layout;
	},
);

~class_ci_popup_view = (
	new: { arg self, controller, size, action;
		self = self.deepCopy;
		self.controller = controller;
		size = size ?? (80@20);

		self.popup = PopUpMenu.new(nil, size);
		self.popup.action = { arg popup;
			action.(popup);
			if(controller.notNil) {
				self.controller.set_val(self.popup.value);
			} {
				debug("class_popup_view: controller is nil");
			};
		};
		//[controller.model.uname, controller.menu_items].debug("class_popup_view");
		self.popup.items = try { 
			//controller.get_menu_items_names.debug("-----------controller.get_menu_items_names");
			controller.get_menu_items_names;
		} { 
			["default"]
		};

		self.layout = self.popup;

		~make_class_responder.(self, self.popup, self.controller, [ \val, \menu_items ]);
		self;
	},

	val: { arg self;
		self.popup.value = self.controller.get_val;
	},

	menu_items: { arg self;
		self.popup.items = self.controller.get_menu_items_names;
	},
);

~class_ci_frame_view = (
	new: { arg self, label, knobs_ctrl, onoff_ctrl, popup1_ctrl, popup2_ctrl;
		self = self.deepCopy;

		self.label_string = label;
		self.knobs_ctrl = { knobs_ctrl };
		self.onoff_ctrl = { onoff_ctrl };
		self.popup1_ctrl = { popup1_ctrl };
		self.popup2_ctrl = { popup2_ctrl };
		self.make_gui;

		self;
	},

	make_gui: { arg self;
		var knobs = [\detune, \wt_pos, \intensity, \oscamp];
		var header, vheader;
		var body, vbody;
		var vframe;
		self.knobs = self.knobs_ctrl.collect { arg ctrl;
			~class_ci_modknob_view.new(ctrl);
		};
		header = HLayout(
				self.onoff = Button.new
					.states_([
						["On"],
						["Off"]
					])
					.maxWidth_(25)
					;
					self.onoff,
				self.label = StaticText.new
					.string_(self.label_string);
					self.label, 
		);
		if(self.popup1_ctrl.notNil) {
			header.add(
				self.popup1 = ~class_ci_popup_view.new(self.popup1_ctrl);
					self.popup1.layout,
			)
		};
		if(self.popup2_ctrl.notNil) {
			header.add(
				self.popup2 = ~class_ci_popup_view.new(self.popup2_ctrl);
					self.popup2.layout,
			)
		};
		body = HLayout(*
				self.knobs.collect(_.layout)
		);
		vheader = View.new;
		vheader.layout = header;
		vheader.background = Color.gray(0.6);

		vframe = View.new;
		vframe.background = Color.gray(0.7);
		vframe.layout = VLayout(
			vheader,
			body,
		);
		vframe.layout.margins = 5;
		header.margins = 3;
		//vframe.layout.spacing = 1;
		self.layout = vframe;
		self.layout;
	},
);

//////////////////////////////////////////////////////
////////////// Base Class Instrs
//////////////////////////////////////////////////////

~class_instr = (
	static_data: [],
	new: { arg self;
		self = self.deepCopy;
	
		self;
	},

	//bypass: { arg self, in, kind, fun;
	//	if(enabled[kind]) {
	//		fun.value;
	//	} {
	//		in;
	//	}
	//},

	make_gui: { arg self;
		var win;
		win = Window.new;
		self.make_layout;
		win.layout = self.layout;
		self.window = win;
		win.front;
	},

	get_specs: (
		// (minval, maxval, warp, step, default, units)
		wt_pos: ControlSpec(0, 1, \lin, 0, 0),
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

	get_synthargs: { arg self, args=();
		var i;
		i = ();
		self.data.keysValuesDo { arg name, datum;
			var control_name;
			control_name = (self.args_prefix ++ name ++ self.args_suffix).asSymbol;
			i[name] = args[name] ?? control_name.kr(datum.default_value);
		};
		self.simple_args.keysValuesDo { arg name, def;
			if(def == \void) {
				i[name] = name.kr;
			} {
				i[name] = name.kr(def);
			}
		};
		self.static_data.keysValuesDo { arg name, datum;
			i[name] = switch(datum.classtype,
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
		//self.static_responders.do(_.remove);
		self.static_data.debug("class_instr.set_static_responders");
		self.static_data.keysValuesDo { arg name, datum;
			var sc;
			sc = SimpleController.new(datum);
			datum.name.debug("class_instr.set_static_responders: param name");
			sc.put(\val, {
				name.debug("static_responder");
				self.build_synthdef;
			});
			resp[name] = sc;
		};
		self.static_responders = resp;
	},

	build_synthdef: { arg self;
		SynthDef(self.synthdef_name, { arg out=0;
			var sig;

			sig = self.synthfun.();

			Out.ar(out, sig);

		}).add
	},
);

~class_ci_freq = (
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
			var is = self.get_staticargs;
			var sig;

			sig = 0;

			sig = sig * EnvGen.ar(Env.adsr(0.1,0.1,1,0.1), i.gate, doneAction:i.doneAction);
			sig;

		}
	
	},
);

~class_ci_osc = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
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
		var wt, wt_range, wt_pos;
		self.synthdef_name.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%% build data");
		wt_pos = ~make_control_param.(main, player, \wt_pos, \scalar, 0, \unipolar.asSpec.copy);
		wt = ~class_param_wavetable_controller.new(player, \wt, wt_pos);
		wt_range = wt.get_wt_range_controller;
		
		self.ordered_args = [
			freq: ~make_control_param.(main, player, \freq, \scalar, 200, \freq.asSpec),
			detune: ~make_control_param.(main, player, \detune, \scalar, 0, \midinote.asSpec),
			wt: wt,
			wt_pos: wt_pos,
			intensity: ~make_control_param.(main, player, \intensity, \scalar, 0, \unipolar.asSpec),
			oscamp: ~make_control_param.(main, player, \oscamp, \scalar, 0.5, \amp.asSpec),
		];
		self.static_data = IdentityDictionary.newFrom((
			spectrum: ~class_param_kind_chooser_controller.new(\spectrum, self.get_spectrum_variants),
			wt_range: wt_range,
		));
		self.data = IdentityDictionary.newFrom(self.ordered_args);
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\detune, \wt_pos, \intensity, \oscamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new("Osc1", self.knobs, nil, self.data[\wt], self.static_data[\spectrum]);
		self.layout = frame_view.layout;
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;

			sig = Instr(\ci_oscillator).value((
				midinote:i.freq.cpsmidi, 
				detune:i.detune,
				wt:i.wt,
				wt_position:i.wt_pos,
				wt_range:i.wt_range,
				spectrum:i.spectrum,
				intensity:i.intensity,
				amp:i.oscamp, 
			));

			sig = sig ! 2;

			//sig = sig * EnvGen.ar(Env.adsr(0.1,0.1,1,0.1), i.gate, doneAction:i.doneAction);
			sig;

		}
	
	},
);

~class_ci_filter = (
	parent: ~class_instr,
	args_prefix: "",
	args_suffix: "",
	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
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
				args: ["Cutoff", "Resonance"],
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
				args: ["Cutoff", "Resonance"],
				specs: [specs[\pitch], \rq]
			),
			(
				name: "BPF",
				uname: \bpf,
				args: ["Cutoff", "Resonance"],
				specs: [specs[\pitch], \rq]
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
		var wt, wt_range, wt_pos;
		var sc;
		var filterkind;
		self.synthdef_name.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%% build data");
		wt_pos = ~make_control_param.(main, player, \wt_pos, \scalar, 0, \unipolar.asSpec.copy);
		wt = ~class_param_wavetable_controller.new(player, \wt, wt_pos);
		wt_range = wt.get_wt_range_controller;
		

		self.ordered_args = [
			arg1: ~make_control_param.(main, player, \arg1, \scalar, 60, \midinote.asSpec.copy),
			arg2: ~make_control_param.(main, player, \arg2, \scalar, 0.5, \unipolar.asSpec.copy),
			arg3: ~make_control_param.(main, player, \arg3, \scalar, 0, \unipolar.asSpec.copy),
		];
		filterkind = ~class_param_kind_chooser_controller.new(\filterkind, self.get_variants);
		self.static_data = IdentityDictionary.newFrom((
			filterkind: filterkind
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
		self.data.copy;
	},

	make_layout: { arg self;
		var knobs = [\arg1, \arg2, \arg3];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.data[name];
		};
		frame_view = ~class_ci_frame_view.new("Filter", self.knobs, nil, self.static_data[\filterkind]);
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

			//sig = sig * EnvGen.ar(Env.adsr(0.1,0.1,1,0.1), i.gate, doneAction:i.doneAction);
			sig;

		}
	
	},
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
			spread: ~make_control_param.(main, player, \spread, \scalar, 0, \unipolar.asSpec),
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
		var knobs = [\detune, \wt_pos, \intensity, \amp];
		self.knobs = knobs.collect { arg name;
			~class_ci_modknob_view.new(self.data[name]);
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
		var knobs = [\detune, \wt_pos, \intensity, \amp];
		self.knobs = knobs.collect { arg name;
			~class_ci_modknob_view.new(self.data[name]);
		};
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

//////////////////////////////////////////////////////

~classinstr_lib = (
	sin: ~class_ci_sin,
	osc: ~class_ci_osc,
	mosc: ~class_ci_mosc,
	moscfilter: ~class_ci_moscfilter,
);

//////////////////////////////////////////////////////
////////////// Instrs
//////////////////////////////////////////////////////

Instr(\ci_oscillator, { arg spectrum, wt_range=0, amp=0.1, midinote=200, wt=0, detune=0.0, wt_position=0, intensity=1;
	var ou, endfreq;
	var formantfreq;
	var mul = 1;
	endfreq = (midinote + detune).midicps;
	//spectrum.debug("spectrum");
	switch(spectrum,
		\bend, {
			endfreq = SinOsc.ar(endfreq).range(0,8)*(intensity)*endfreq + endfreq;
			// modulo
		},
		\formant, {
			intensity.poll;
			formantfreq = endfreq;
			//endfreq = formantfreq * ((intensity * 8) + 1);
			endfreq = formantfreq * (2 ** (intensity-0.5 *8).trunc);
			mul = SinOsc.ar(formantfreq);
		}
	);
	if(wt_range == 0) {
		ou = Osc.ar(wt, endfreq) * mul;
	} {
		ou = VOsc.ar(wt+(wt_position.clip(0,wt_range)), endfreq) * mul;
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
}, [NonControlSpec(), NonControlSpec()]);

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
		\comb, {
			CombL.ar(in, arg1, arg2, arg3);
		}
	);
	sig;

},[\audio, NonControlSpec()]);
