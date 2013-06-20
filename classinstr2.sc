/////////////// inlinefx 

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

			\freq.kr.poll(label:"FREQ");
			i.freq.poll(label:"i.Freq");
			sig.poll;
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
