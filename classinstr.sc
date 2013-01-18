//////////////////////////////////////////////////////
////////////// GUI parts
//////////////////////////////////////////////////////

~class_ci_modknob_view = (
	new: { arg self, controller;
		self = self.deepCopy;

		self.controller = { controller };
		self.make_gui;

		~make_class_responder.(self, self.namelabel, self.controller, [
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
		self.controller.name.debug("class_ci_modknob_view: LABEL2");
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
				knob.asView.debug("VIEW");
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

~class_ci_modslider_view = (
	new: { arg self, controller;
		self = self.deepCopy;

		self.controller = { controller };
		debug("BLLA1");
		self.make_gui;
		debug("BLLA2");

		~make_class_responder.(self, self.namelabel, self.controller, [
			\val, \label,
		]);
	
		debug("BLLA3");
		self;
	},

	val: { arg self;
		self.vallabel.string = self.controller.get_val.asFloat.asStringPrec(~general_sizes.float_precision);
		self.knob.value = self.controller.get_norm_val;
	},

	label: { arg self;
		self.controller.label.debug("class_ci_modknob_view: LABEL");
		self.controller.name.debug("class_ci_modknob_view: LABEL2");
		self.namelabel.string = self.controller.label ?? self.controller.name;
	},

	make_gui: { arg self;
		var label;
		var knob;
		var vallabel;
		self.layout = VLayout(
			label = StaticText.new
				.align_(\centered)
				.string_(self.controller.label ?? self.controller.name);
				[label, stretch:0, align:\center],
			knob = ModSlider.new(nil, Rect(0,0,30,300)); 
				debug("BLLA");
				knob.action_({ 
					self.controller.set_norm_val(knob.value)
				});
				knob.asView.debug("SLIDERVIEW");
				knob.asView.minSize_(20@200);
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
	new: { arg self, label, knobs_ctrl, onoff_ctrl, popup1_ctrl, popup2_ctrl, fader_ctrl;
		self = self.deepCopy;

		self.label_string = label;
		self.knobs_ctrl = { knobs_ctrl };
		self.onoff_ctrl = { onoff_ctrl };
		self.popup1_ctrl = { popup1_ctrl };
		self.popup2_ctrl = { popup2_ctrl };
		self.fader_ctrl = { fader_ctrl };
		self.make_gui;

		if(self.fader_ctrl.notNil) {
			~make_class_responder.(self, self.fader, self.fader_ctrl, [
				\val,
			]);

		};

		self;
	},

	val: { arg self;
		if(self.fader_ctrl.notNil) {
			self.fader.value = self.fader_ctrl.get_norm_val;
		}
	},

	make_gui: { arg self;
		var header, vheader;
		var body, vbody;
		var vframe;
		self.knobs = self.knobs_ctrl.collect { arg ctrl;
			~class_ci_modknob_view.new(ctrl);
		};
		header = HLayout.new;
		if(self.onoff_ctrl.notNil) {
			header.add(
				self.onoff = Button.new
					.states_([
						["Off"],
						["On"],
					])
					.value_(self.onoff_ctrl.get_val)
					.action_({
						self.onoff_ctrl.set_val(self.onoff.value)
					})
					.maxWidth_(25)
					;
					self.onoff,
			)

		};
		header.add(
			self.framelabel = StaticText.new
				.string_(self.label_string);
				self.framelabel, 
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
		vframe.layout = HLayout(
			VLayout(
				vheader,
				body,
			)
		);
		if(self.fader_ctrl.notNil) {
			vframe.layout.add(
				self.fader = Slider.new
					.orientation_(\vertical)
					.action_({
						self.fader_ctrl.set_norm_val(self.fader.value)
					})
					;
					self.fader
			)
		};
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
	static_data: [],
	simple_args: (),
	args_prefix: "",
	args_suffix: "",
	new: { arg self;
		self = self.deepCopy;
	
		self;
	},

	bypass: { arg self, val, fun, in;
		if(val == 1) {
			fun.value;
		} {
			in;
		}
	},

	make_gui: { arg self;
		var win;
		win = Window.new;
		self.make_layout;
		win.layout = self.layout;
		self.window = win;
		win.front;
	},

	//argnamemod: { arg self, prefix="", suffix="", rec=1;
	//	{ arg name;
	//		switch(rec,
	//			2, {
	//				self.parent_argnamemod.(
	//					(prefix ++ name ++ suffix).asSymbol
	//				)
	//			},
	//			{
	//				(prefix ++ name ++ suffix).asSymbol
	//			}
	//		);
	//	}	
	//},
	
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

	get_synthargs: { arg self, args=();
		var i;
		i = ();
		self.data.keysValuesDo { arg name, datum;
			var control_name;
			control_name = self.abs_namer(name);
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

	build_synthdef: { arg self;
		self.synthdef_name.debug("REBUILD SYNTH");
		SynthDef(self.synthdef_name, { arg out=0;
			var sig;

			sig = self.synthfun.();

			Out.ar(out, sig);

		}).add
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
			enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
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
		frame_view = ~class_ci_frame_view.new("Osc1", self.knobs, self.static_data[\enabled], self.data[\wt], self.static_data[\spectrum]);
		self.layout = frame_view.layout;
		self.layout;
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var sig;
			i.debug("III");

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
			//i.oscamp.poll;
			sig = self.bypass(i.enabled, sig, DC.ar(0));
			i.oscamp.debug("OSCAMP");

			//sig.poll;

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

		self.osc = ~class_ci_osc.new(main, player, self.make_namer("rah_"));

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

			sig = self.osc.synthfun.();
			//sig.poll;
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
			enabled: ~class_param_static_controller.new(\enabled, specs[\onoff], 1),
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
				freq:i.freq,
			));
			i.enabled.debug("ENABLED");
			sig = self.bypass(i.enabled, sig, in);

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

		self.osc = ~class_ci_oscfader.new(main, player, self.make_namer("bla_"));
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

~class_ci_osc3filter2 = (
	parent: ~class_instr,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_osc3filter2;
		self.simple_args = (gate:1, doneAction:2);

		self.oscs = 3.collect { arg idx;
			~class_ci_oscfader.new(main, player, self.make_namer("osc%_".format(idx)));
		};
		self.filters = 2.collect { arg idx;
			~class_ci_filter.new(main, player, self.make_namer("filter%_".format(idx)));
		};
		self.insertfxs = 2.collect { arg idx;
			~class_ci_insertfx.new(main, player, self.make_namer("insfx%_".format(idx)));
		};
		self.master = ~class_ci_master_env.new(main, player);
		self.modules = [self.master] ++ self.oscs ++ self.filters ++ self.insertfxs;

		self.build_data;
	
		self;
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
		var filtermix = ~make_control_param.(main, player, \filtermix, \scalar, 0.5, \unipolar.asSpec);
		var filterparseq = ~make_control_param.(main, player, \filterparseq, \scalar, 0.5, \unipolar.asSpec);
		self.ordered_args = self.modules.collect({ arg a; a.get_ordered_args }).flat;
		self.ordered_args = self.ordered_args ++ [
			filtermix: filtermix,
			filterparseq: filterparseq,
		];
		filtermix.set_label("Filter Mix");
		filterparseq.set_label("Par Seq");


		self.static_data = (
			route_insertfx1: ~class_param_kind_chooser_controller.new(\route_insertfx1, self.get_route_insfx_variants, "Insert Fx 1"),
			route_insertfx2: ~class_param_kind_chooser_controller.new(\route_insertfx2, self.get_route_insfx_variants, "Insert Fx 1"),
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

		var routing_layout = HLayout(
			VLayout(*
				route_data.collect{ arg x; 
					StaticText.new
						.string_(x.label ?? x.name);
				};
			),
			VLayout(*
				route_data.collect{ arg x; 
					~class_ci_popup_view.new(x).layout;
				};
			)
		);
		routing_layout;
	},

	make_layout: { arg self;
		self.layout = HLayout(
			VLayout(*
				self.oscs.collect(_.make_layout) ++ [nil]
			),
			VLayout(*
				[HLayout(
					~class_ci_modslider_view.new(self.data[\filterparseq]).layout,
					VLayout(*
						self.filters.collect(_.make_layout) ++
						[nil]
					),
					~class_ci_modslider_view.new(self.data[\filtermix]).layout,
				)] ++
				self.insertfxs.collect(_.make_layout) ++
				[nil]
			),
			VLayout(
				[self.master.make_layout, stretch:0],
				[self.make_layout_routing, stretch:0],
				[nil, stretch:1],
			),
			nil,
		);
		self.layout;
	},

	insert_effect: { arg self, i, in, pos;
		if(i.route_insertfx1 == pos) {
			//TODO: ktr, arg3
			self.insertfxs[0].synthfun.(in);
		} {
			if(i.route_insertfx2 == pos) {
				self.insertfxs[1].synthfun.(in);
			} {
				in;
			}
		}
	},

	synthfun: { arg self;
		{ arg args;
			var i = self.get_synthargs(args);
			var is = self.get_staticargs;
			var sig, sig1, sig2;
			var rsig;
			var oscs;
			var f1_in, f2_in;
			var fmiddle_in;
			var f1_out, f2_out;

			oscs = self.oscs.collect { arg osc;
				osc.synthfun.()
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
			f1_out.poll;

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
			sig = SelectX.ar(i.filtermix, [f1_out, f2_out]);

			// end effect
			sig = self.insert_effect(i, sig, \before_pan);

			sig = sig ! 2;

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
	moscfaderfilter: ~class_ci_moscfaderfilter,
	osc3filter2: ~class_ci_osc3filter2,
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
			//intensity.poll;
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
	//sig.poll;
	sig;

},[\audio, NonControlSpec()]);

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
	var sig;
	//TODO
	drive = 1-drive;
	sig = in.clip(0-drive, drive);
	SelectX.ar(mix, [in, sig]);
}, [\audio]);
