
(

~class_custom_tracks_controller = (
	new: { arg self, main, player, display, tracks;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = {player};
		self.display = display;
		self.custom_tracks = {tracks};
		//self.custom_tracks.debug("bordel");
		//self.display = ~class_track_display.new;

	
		self;
	},

	get_tracks: { arg self;
		var res = List.new;
		debug("class_custom_tracks_controller.get_tracks");
		res = self.custom_tracks.collect { arg track;
			track.uname.debug("bordel");
			~make_recordline_track_controller.(track, self.display);
		};
		self.tracks = res;
		res;
	},

	make_bindings: { arg self;
	
		self.get_main.commands.parse_action_bindings(\player_tracks, 
			self.get_main.panels.side.get_shared_bindings ++ 
			self.display.get_bindings ++ 
			self.get_main.panels.side.get_windows_bindings ++ [
			[\close_window, {
				self.window.close;
			
			}],

			[\select_scoresheet, 8, { arg i;
				self.get_player.get_scoreset.select_sheet(i);
				self.tracks[0].changed(\label);
			}],

			[\save_scoresheet, {
				var player = self.get_player;
				~class_scoresheet_chooser.new(self.get_main, player, { arg data, ad, idx;
					var sheet;
					var scoreset = player.get_scoreset;
					var ns;
					ns = scoreset.get_notescore;
					scoreset.set_sheet(idx, ns);
					self.get_player.get_scoreset.select_sheet(idx);
					self.tracks[0].changed(\label);
				});
			}],

			[\play_selected, {
				self.get_player.play_node;
			}], 

			[\stop_selected, {
				self.get_player.stop_node;
			}],

			[\panic, {
				self.get_main.panic;
			}],
		]);
	},

	make_gui: { arg self;
		self.make_bindings;
		self.window = Window.new("Custom Tracks", Rect(300,300,900,300));
		self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\player_tracks, self);
		self.multitrack_view = ~class_multitrack_view.new(self);
		self.window.view.layout = self.multitrack_view.layout;

		self.window.front;
	},
);

~tracks = { arg player, tracks;
	var main = Mdef.main;
	var display = main.panels.side.track_display;
	tracks = tracks.collect { arg track;
		if(track.isSymbolWS or: { track.isString }) {
			main.get_node(track);
		} {
			track;
		}
	};
	~class_custom_tracks_controller.new(main, player, display, tracks)

};
)

"plop".isString

(
~mynode = Mdef.node("osc1_l1002");
~ctrl = ~tracks.(~mynode, [
	~mynode,
	"stereosampler_l1003"
]);
~ctrl.make_gui;
)



(
	~class_player_compositor = (
		data: IdentityDictionary.new,
		show_paramlist: List.new,
		inline_synthfun: IdentityDictionary.new,
		inline_synthfun_thunk: IdentityDictionary.new,
		archive_data: [\inline_synthfun, \show_paramlist, \param_declaration],
		new: { arg self, player;
			self = self.deepCopy;

			self.get_player = { player };
		
			self;
		},

		save_data: { arg self;
			var data = ();
			data.data = ()
			self.data.keysValuesDo { arg key, val;
				data.data[key] = val.save_data;
			};
			~save_archive_data.(self, self.archive_data, data);
		},

		load_data: { arg self, data;
			~load_archive_data.(self, self.archive_data, data);
			self.declare_params(data.param_declaration);

			self.data.keysValuesDo: { arg key, val;
				if(data.data[key].notNil) {
					self.data[key].load_data(data.data[key])
				}
			}
			
		},

		get_arg: { arg self, key;
			self.data[key]
		},

		has_arg: { arg self, key;
			self.data[key].notNil
		},

		destructor: { arg self;
			self.data.keysValuesDo { arg key, val;
				val.destructor;
			};
		},

		make_layout: { arg self;
			var count = 0;
			var vlayout = VLayout.new;
			var hlayout = HLayout.new;
			var datalist = List.new;
			vlayout.add(hlayout);

			Task{

				self.data.keysValuesDo { arg key, val;
					hlayout.add(
						~class_ci_modknob_view.new(val).layout;
					);
					count = count+1;
					if(count == 8) {
						count = 0;
						hlayout = HLayout.new;
						vlayout.add(hlayout)
					}
				};
				if(self.show_paramlist.size > 0) {
					hlayout = HLayout.new;
					vlayout.add(hlayout);
					self.show_paramlist.do { arg key;
						var val;
						val = self.get_player.data[key];
						hlayout.add(
							~class_ci_modknob_view.new(val).layout;
						);
					};
				};

			}.play(AppClock);
			self.layout = vlayout;
			self.layout;
			
		},

		make_gui: { arg self;
			self.window = Window.new("Compositor");
			self.make_layout;
			self.window.layout = self.layout;
		},

		///////////////////////// inline

		set_inline_synthfun: { arg self, key, val;
			self.inline_synthfun[key] = val;
			self.inline_synthfun_thunk[key] = ArgThunk.new(val);
		},

		reset_inline_synthfun_thunks: { arg self;
			self.inline_synthfun.keysValuesDo { arg key, val;
				self.inline_synthfun_thunk[key] = ArgThunk.new(self.inline_synthfun[key])
			}
		},

		compose_synth_param: { arg self, key, val;
			if(self.inline_synthfun_thunk[key].notNil) {
				key.debug("class_player_compositor.compose_synth_param: composing");
				self.inline_synthfun_thunk[key].value(val)
			} {
				val;
			}
		},

		declare_params: { arg self, params;
			var comp = self;
			self.param_declaration = params;
			params.keysValuesDo { arg key, val;
				switch(val.classtype,
					\syntharg, {
						if( comp.has_arg(key).not ) {
							comp.data[key] = ~make_control_param.(main, node, key, \scalar, val.default_value, val.spec);
						} {
							comp.data[key].spec = val.spec;
							comp.data[key].default_val = val.default_value;
						}
					}
				)
			};
			(comp.data.keys - params.keys).do { arg key;
				comp.data[key].destructor;
				comp.data[key] = nil;
			}

		};


	);

	~reset_node = { arg node;
		node = ~get_node.(node);
		if(node.compositor.notNil) {
			node.compositor.destructor;
			node.compositor = nil;
		};
	};

	~get_node = { arg node;
		var main = Mdef.main;
		if(node.isSymbolWS or: { node.isString }) {
			node = main.get_node(node);
			
		} {
			node;
		};
		node;
	};

	~params = { arg params;
		var main = Mdef.main;
		var node = ~node;
		if(node.notNil) {
			var comp = node.compositor;
			params.keysValuesDo { arg key, val;
				comp.set_inline_synthfun( key, val );
			}
		};
	};

	~declare_params = { arg params;
		var main = Mdef.main;
		var node = ~node;
		if(node.notNil) {
			var comp = node.compositor;
		};
	};

	~show_params = { arg paramlist;
		var main = Mdef.main;
		var node = ~node;
		node.compositor.show_paramlist = paramlist;
	};

	~syntharg = { arg spec, default_val;
		(
			classtype: \syntharg,
			spec: spec,
			default_value: default_val,
		)
	};

	~compose_node = { arg node, fun;
		var env;
		env = Environment.new;
		env.parent = currentEnvironment;
		env[\node] = ~get_node.(node);
		if(node.compositor.isNil) {
			node.compositor = ~class_player_compositor.new(node);
		};
		env.use {
			fun.value;
		};
		env[\node].rebuild_arg_list;
	};

	~sarg = { arg name;
		

	};
) 

(
	~mynode = Mdef.node_by_index(0);
	//~reset_node.(~mynode);
	~compose_node.(~mynode, {
		~show_params.([
			\osc1_detune,
		]);
		~declare_params.((
			osc1_detune_freq: ~syntharg.(\widefreq.asSpec, 2),
			//freq_fm: ~syntharg.(\widefreq.asSpec, 0),
			freq_modfreq: ~syntharg.(ControlSpec(0.01,40,\exp,0,1), 1),
			freq_index: ~syntharg.(\unipolar.asSpec, 2),
		));

		~params.((
			osc1_detune: { arg in; 
				LFSaw.ar(
					\osc1_detune_freq.kr(2) + ([-1,0,1]*\pitch_spread.kr)
				) * (in/2) + in 
			},
			//freq: { arg in; freq + (\freq_fm.ar(0) * \freq_index.kr(0.5)*freq) }
			freq: { arg in; in + (SinOsc.ar(\freq_modfreq.kr(2)*in) * \freq_index.kr(0.5)*in) }
		))
		
	});

)
~mynode.rebuild_arg_list
~mynode.compositor

~mynode.external_player.param[\osc1_wt].set_val_uname(\LFSaw)
~mynode.external_player.param[\osc0_wt].set_val_uname(\SinOsc)
~mynode.play_node
~mynode.compositor.make_gui
	~reset_node.(~mynode);

	~mynode.compositor.inline_synthfun_thunk[\osc1_detune].value(4)
