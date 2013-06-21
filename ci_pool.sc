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
		self.help_build_data(
			[
				freq: ~make_control_param.(main, player, \freq, \scalar, 203, \freq.asSpec),
				detune: ~make_control_param.(main, player, \detune, \scalar, 64, \midinote.asSpec),
			];
		)
	},

	make_layout: { arg self;
		var knobs = [\freq, \detune];
		self.knobs = knobs.collect { arg name;
			~class_ci_modknob_view.new(self.param[name]);
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

			//self.param.keysValuesDo { arg name, datum;
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
		self.help_build_data(
			nil,
			nil,
			[
				self.master,
				self.osc,
			]
		)
	},

	make_layout: { arg self;
		var knobs = [\detune, \wt_pos, \intensity, \oscamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
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
		self.help_build_data(
			nil,
			nil,
			[
				self.master,
				self.osc,
				self.filter,
			]
		)
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
		self.help_build_data(
			nil,
			nil,
			[
				self.master,
				self.osc,
				self.filter,
			]
		)
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
			[
				opmatrix: ~class_param_opmatrix_controller.new(\opmatrix, \unipolar, self.op_names.size@(self.op_names.size+2)),
			],
			nil,
			self.operators ++ [
				self.master,
			],
		)
	},

	make_layout: { arg self;
		var opcount = self.op_names.size;
		var make_cell;
		var make_opcell;

		"makelayout1".debug;

		make_cell = { arg x, y;
			var val;
			val = self.param[\opmatrix].get_cell_val(x,y);
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
					self.param[\opmatrix].set_cell_val(x,y, val)
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
					sfactor = self.param[\opmatrix].get_cell_val(i, idx);
					if(sfactor >= 0) {
						factor = opmatrix[i + (idx * opcount)];
						in = in + (ops[i] * factor)
					}
				};
				(opcount - idx).do { arg i;
					var sfactor;
					var factor;
					sfactor = self.param[\opmatrix].get_cell_val(i + idx, idx);
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
				sfactor = self.param[\opmatrix].get_cell_val(idx, opcount);
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
		self.help_build_data(
			[
				opmatrix: ~class_param_opmatrix_controller.new(\opmatrix, \unipolar, self.matrix_size),
			],
			nil,
			[
				self.xoperators,
				self.master,
			].flatten,
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
			val = self.param[\opmatrix].get_cell_val(x,y);
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
					self.param[\opmatrix].set_cell_val(x,y, val)
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
					sfactor = self.param[\opmatrix].get_cell_val(i, idx);
					if(sfactor >= 0) {
						factor = opmatrix[i + (idx * opcount)];
						in = in + (ops[i] * factor)
					}
				};
				(opcount - idx).do { arg i;
					var sfactor;
					var factor;
					sfactor = self.param[\opmatrix].get_cell_val(i + idx, idx);
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
				sfactor = self.param[\opmatrix].get_cell_val(idx, opcount);
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

		self.help_build_data(
			self.make_control_params([
				[\freq, \freq, 204],
			]),
			nil,
			[
				self.osc,
				self.bufosc,
				self.filter,
				self.master,
			],
		);
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


		self.help_build_data(
			self.make_control_params([
				[\freq, \freq, 205],
			] ++ self.voices_keys.collect { arg key;
				[key, \bipolar, 0]
			}) ++ [
			],
			static_data,
			[
				self.osc,
				self.bufosc,
				self.filter,
				self.master,
				self.insertfxs,
			].flatten,
		);
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

		self.help_build_data(
			[
				modfreq: ~make_control_param.(main, player, \modfreq, \scalar, 206, \lofreq.asSpec),
				//detune: ~make_control_param.(main, player, \detune, \scalar, 0, specs[\pitch]),
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
			);
		)
	},

	make_layout: { arg self;
		var knobs = [\modfreq, \wt_pos, \intensity, \oscamp];
		var frame_view;
		self.knobs = knobs.collect { arg name;
			self.param[name];
		};
		frame_view = ~class_ci_frame_view.new("Osc1", self.knobs, self.param[\enabled], self.param[\wt], self.param[\spectrum]);
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

		self.help_build_data(
			self.make_control_params([
				[\modfreq, \lofreq, 1],
			]),
			nil,
			[
				self.osc
			],
		);
		self.osc.param[\modfreq] = self.param[\modfreq]; // so the parent gui can display the modfreq param
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

		self.help_build_data(
			[
				gtrig: ~class_param_gtrig.new(\gtrig)
			] ++
			self.make_control_params([
				[\tesustain, specs[\sustain], 0.25],
			]),
			nil,
			[
				//self.insertfxs,
				self.compenv
			],
		);
	},

	make_layout: { arg self;
		self.track_display = ~class_track_display.new;
		self.curve_track_controller = ~class_step_track_controller.new(self.get_player, self.track_display);
		self.layout = VLayout(
			self.curve_track_controller.make_gui,
			~class_ci_modknob_view.new(self.param[\tesustain]).layout,
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
		self.help_build_data(
			self.make_control_params([
				[\mix, \unipolar, 0.5],
			]),
			nil,
			self.insertfxs,
		);
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

////////////// Inline

~class_ci_inline_genfx = (
	parent: ~class_instr,
	new: { arg self, main, player, namer;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = { player };
		self.namer = { namer };
		self.synthdef_name = \ci_;

		self.simple_args = (freq:\void, gate:1, doneAction:2);
	
		self.ingen = ~class_ci_inlinegen.new(main, player, self.make_namer);
		self.infx = ~class_ci_inlinefx.new(main, player, self.make_namer);
		self.master = ~class_ci_master_dadsr.new(main, player);

		self.build_data;
	
		self;
	},

	build_data: { arg self;
		self.help_build_data([], nil, [
			self.ingen,
			self.infx,
			self.master,
		])
	},

	make_layout: { arg self;
		self.layout = HLayout(
			self.ingen.make_layout,
			self.infx.make_layout,
			self.master.make_layout,
		)
	},

	synthfun: { arg self;
		{ arg args;
			var sig;
			var i = self.get_synthargs(args);
			sig = self.ingen.inline_synthfun.(args);
			//sig.debug("SIGGGGGG");
			//i.keys.debug("iiiiiiiiiiiiiiiiiSIGGGGGG");
			//i.keysValuesDo { arg key, val;
			//	val.poll(label:key.asString);
			//};
			//sig = SinOsc.ar(i.freq);

			//\freq.kr.poll(label:"FREQ");
			//i.freq.poll(label:"i.Freq");
			//sig.poll;
			//SinOsc.ar(i.freq);
			//sig.poll;
			sig = self.infx.inline_synthfun.(sig, args);
			//sig.poll;
			sig = self.master.synthfun.(sig);
			//sig.poll;
			sig;
		}
	},
);


//////////////////////////////////////////////////////
////////////// Class Instrs Lib
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

	// nodes using inline nodes

	inlinefx: ~class_ci_inlinefx,
	inlinegen: ~class_ci_inlinegen,
	inline_genfx: ~class_ci_inline_genfx,

	// inline gen nodes

	ingen_osc: ~class_ci_osc,

	// inline fx nodes

	infx_filter: ~class_ci_filter,

);

//////////////////////////////////////////////////////
////////////// Instrs
//////////////////////////////////////////////////////

Instr(\ci_oscillator, { 
	arg spectrum, wt_range=0, wt_classic=\void, amp=0.1, midinote=207, wt=0, detune=0.0, wt_position=0, intensity=1, phase=nil;
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
		if(spectrum == \phase) {
			phase = intensity;
		};
		ou = Instr(\ci_classic_oscillator).value((kind:wt_classic, freq:endfreq, width:width, phase:phase, generic_arg:wt_position)) * mul;
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
		\tanh, {
			ou = (ou * (intensity*8+1)).tanh;
		},
		\distort, {
			ou = (ou * (intensity*8+1)).distort;
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

Instr(\ci_classic_oscillator, { arg kind=\void, freq=208, phase=nil, width=0.5, generic_arg=1;
	if(phase.isNil) {
		phase = 0;
	};
	switch(kind,
		\SinOsc, {
			phase = generic_arg;
			SinOsc.ar(freq, phase*pi)
		},
		\LFSaw, {
			phase = generic_arg;
			LFSaw.ar(freq, phase)
		},
		\LFCub, {
			phase = generic_arg;
			LFCub.ar(freq, phase)
		},
		\LFPar, {
			phase = generic_arg;
			LFPar.ar(freq, phase)
		},
		\Blip, {
			width = generic_arg;
			Blip.ar(freq, width.exprange(1,100))
		},
		\Formant, {
			var freqspec = \freq.asSpec;
			Formant.ar(freq, freqspec.map(generic_arg), freqspec.map(width));
		},
		\LFPulse, {
			phase = generic_arg;
			LFPulse.ar(freq, phase/2, width)
		},
		\LFTri, {
			phase = generic_arg;
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
		\tanh, {
			Instr(\p_tanh).value((in:in, mix: arg1, preamp:arg2, postamp:arg3));
		},
		\distort, {
			Instr(\p_distort).value((in:in, mix: arg1, preamp:arg2, postamp:arg3));
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

Instr(\p_tanh, { arg in, mix, preamp=1, postamp=1;
	var sig = in;
	sig = sig * preamp;
	sig = sig.tanh;
	sig = sig * postamp;
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

Instr(\p_distort, { arg in, mix, preamp=1, postamp=1;
	var sig = in;
	sig = sig * preamp;
	sig = sig.distort;
	sig = sig * postamp;
	SelectX.ar(mix, [in, sig]);
}, [\audio]);

/////////// effects

Instr(\p_reverb, { arg in, mix, room, damp, gate=1, amp=1;
	var sig;
	in = In.ar(in, 2);
	sig = FreeVerb.ar(in, mix, room, damp);
	sig = sig * (((1-mix) * 1) + (mix * amp));
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
		rate: ControlSpec(0.00001,4,\exp,0,0),
		depth: ControlSpec(0.00001,4,\exp,0,0),
		offset: ControlSpec(0.00001,1,\exp,0,0),
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

