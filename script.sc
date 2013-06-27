
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

~class_player_compositor = (
	data: IdentityDictionary.new,
	show_paramlist: List.new,
	inline_synthfun: IdentityDictionary.new,
	inline_synthfun_thunk: IdentityDictionary.new,
	archive_data: [\inline_synthfun, \show_paramlist, \param_declaration],
	mod_declaration: IdentityDictionary.new,
	new: { arg self, player;
		self = self.deepCopy;

		self.get_player = { player };
		self.get_main = { player.get_main };
	
		self;
	},

	save_data: { arg self;
		var data = ();
		data.data = ();
		self.data.keysValuesDo { arg key, val;
			data.data[key] = val.save_data;
		};
		~save_archive_data.(self, self.archive_data, data);
	},

	load_data: { arg self, data;
		~load_archive_data.(self, self.archive_data, data);
		self.declare_params(data.param_declaration);

		self.data.keysValuesDo { arg key, val;
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
		self.window.front;
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
		var main = self.get_main;
		var node = self.get_player;
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

	},

	declare_mods: { arg self, mods;
		var node = self.get_player;
		var main = self.get_main;
		var new_mod_keys = mods.keys - self.mod_declaration.keys;
		var removed_mod_keys = self.mod_declaration.keys - mods.keys;
		new_mod_keys.do { arg key;
			var modname, mod;
			var val = mods[key];
			var modlibname = val.value;
			var modkind = val.key;
			key.debug("declare_mods");
			if(main.node_exists(key).not) {
				val.debug("declare_mods2");
				modname = main.node_manager.make_livenode_from_libmodnode(modlibname, node.uname, key);
				key.debug("declare_mods3");
				mod = main.get_node(modname);
				mod.modulation.set_mod_kind(\pattern);

				switch(modkind, 
					\normal, {

					},
					\note, {
						mod.set_mode(\noteline);
						// FIXME: should be a property of scoreset
						mod.get_scoreset.get_current_sheet.remove_first_rest_if_not_needed = true;

					}
				);
			}
		};
		removed_mod_keys.do { arg key;
			main.free_node(key);
		};
		self.mod_declaration = mods;
	
	},

	vpattern: { arg self;
		(compositor: self);
	},

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

~get_node_name = { arg node;
	if(node.isSymbolWS or: { node.isString }) {
		node;
	} {
		node.uname;
	};
};

~params_synth = { arg params;
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
		comp.declare_params(params);
	};
};

~declare_mods = { arg mods;
	var main = Mdef.main;
	var node = ~node;
	if(node.notNil) {
		var comp = node.compositor;
		"bordel".debug;
		comp.declare_mods(mods);
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

~params_mod = { arg params;
	var main = Mdef.main;
	var node = ~node;
	if(node.notNil) {
		var comp = node.compositor;
		params.keysValuesDo { arg key, val;
			var modname;
			var mod;

			modname = val;
			node.get_arg(key).set_modulation_mode(true);
			node.modulation.get_modulation_mixer(key).carrier_modulator_name = modname;

		}
	};
};

~carmod = { arg val; val };

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

~decmod = { arg val; (\normal -> val) };

~note_mod = { arg val; (\note -> val) };

~show_note_editor = { arg node;
	var display;
	var ltracks;
	node = ~get_node.(node);
	display = Mdef.main.panels.side.track_display;
	ltracks = ~class_line_tracks_controller.new(Mdef.main, node, ~display);
	ltracks.make_gui;

};

~reload_node = { arg node;
	var newnode, oldnode;
	node = ~get_node.(node);
	newnode = node.clone;
	oldnode = Mdef.main.model.livenodepool[~get_node_name.(node)];
	Mdef.main.model.livenodepool[\my_detune_scorizer] = newnode;
	oldnode.destructor;
};

) 

~reload_node.("zegrainer_l1026")

(
	~mynode = Mdef.node_by_index(0);
	~reset_node.(~mynode);
	~compose_node.(~mynode, {
		//~show_params.([
		//	\osc1_detune,
		//]);
		~declare_mods.((
			my_detune_scorizer: ~note_mod.(\detune_scorizer),
		));
		//~declare_params.((
		//	osc1_detune_freq: ~syntharg.(\widefreq.asSpec, 2),
		//	//freq_fm: ~syntharg.(\widefreq.asSpec, 0),
		//	freq_modfreq: ~syntharg.(ControlSpec(0.01,40,\exp,0,1), 1),
		//	freq_index: ~syntharg.(\unipolar.asSpec, 2),
		//));

		//~params_synth.((
		//	//osc1_detune: { arg in; 
		//	//	LFSaw.ar(
		//	//		\osc1_detune_freq.kr(2) + ([-1,0,1]*\pitch_spread.kr)
		//	//	) * (in/2) + in 
		//	//},
		//	//freq: { arg in; freq + (\freq_fm.ar(0) * \freq_index.kr(0.5)*freq) }
		//	freq: { arg in; in + (SinOsc.ar(\freq_modfreq.kr(2)*in) * \freq_index.kr(0.5)*in) }
		//));

		~params_mod.((
			osc1_detune: ~carmod.(\my_detune_scorizer),
		))

		
	});

)
(
	~mynode = Mdef.node(\my_detune_scorizer);
	~reset_node.(~mynode);
	~compose_node.(~mynode, {
		~show_params.([
			\lag,
		]);
	});
	~mynode.compositor.make_gui;

)

(
	~mynode = Mdef.node_by_index(0);
	~mynode.compositor = ~class_player_compositor.new(~mynode);
	~mynode.compositor.score_sheet_index = 0;

	~mynode.play_node(~mynode.compositor);

)
~mynode.get_arg(\noteline).get_scoreset.get_sheets[2]
(
	~mynode.compositor.score_sheet_index = 1

	~mynode.play_node(~mynode.compositor)

)
	~mynode.compositor.window.front

~show_note_editor.(\my_detune_scorizer);

	Mdef.node(\my_detune_scorizer).compositor.make_gui;


69.midicps


~mynode.rebuild_arg_list
~mynode.compositor

~mynode.external_player.param[\osc1_wt].set_val_uname(\LFSaw)
~mynode.external_player.param[\osc0_wt].set_val_uname(\SinOsc)
~mynode.play_node
	~reset_node.(~mynode);

	~mynode.compositor.inline_synthfun_thunk[\osc1_detune].value(4)


	(
		~node = Mdef.node(\my_detune_scorizer);
		~data = ~node.save_data;
		~nnode = ~node.clone
		~old = Mdef.main.model.livenodepool[\my_detune_scorizer];
		Mdef.main.model.livenodepool[\my_detune_scorizer] = ~nnode
		~old.destructor


	)


(
SynthDef(\lfo_line, { arg out=0, amp=0.1, gate=1, freq=0.01, start=200, end=500;
	var sig;
	sig = SinOsc.kr(freq).range(start, end);
	Out.kr(out, sig);
}).add;
)

(
SynthDef(\note_it, { arg out=0, amp=0.1, gate=1, freq=200;
	var sig;
	//sig = Lag.kr(freq, 0.1);
	sig = freq;
	//sig.poll;
	Out.kr(out, sig);
}).add;

SynthDef(\detune_scorizer, { arg out=0, amp=0.1, gate=1, freq=200, lag=0.1;
	var sig;
	//sig = Lag.kr(freq, 0.1);
	freq = Lag.kr(freq, lag);
	sig = freq.cpsmidi - 60;
	//sig.poll;
	Out.kr(out, sig);
}).add;
)

	Mdef.main.panels.side.get_current_group.children
(
	~mynode_name = Mdef.main.node_manager.make_livenode_from_libnode(\osc1);
	Mdef.main.panels.side.get_current_group.set_children_name(0, ~mynode_name);
	~mynode = Mdef.node_by_index(0);
	~mymod_name = Mdef.main.node_manager.make_livenode_from_libmodnode(\lfo_line, ~mynode.uname);
	~mymod = Mdef.node(~mymod_name);
	~mymod.get_arg(\start).set_val(200);
	~mymod.get_arg(\end).set_val(7000);
	~mymod.get_arg(\freq).set_val(0.2);
	~mymod.modulation.set_mod_kind(\pattern);
	~mynode.get_arg(\ffreq).set_modulation_mode(true);
	~mynode.get_arg(\dur).set_val(1);

	~mynode.modulation.get_modulation_mixer(\ffreq).carrier_modulator_name = ~mymod_name;
	~mynode.modulation.update_modulation_pattern;
	~mynode.play_node;
)
	~mynode.modulation.get_modulation_mixer(\ffreq).carrier_modulator_name = nil

	~mymod.set_mode(\noteline)
(
	~mynode_name = Mdef.main.node_manager.make_livenode_from_libnode(\osc1);
	Mdef.main.panels.side.get_current_group.set_children_name(0, ~mynode_name);
	~mynode = Mdef.node_by_index(0);
	~mymod_name = Mdef.main.node_manager.make_livenode_from_libmodnode(\note_it, ~mynode.uname);
	~mymod = Mdef.node(~mymod_name);
	~mymod.set_mode(\noteline);
	~mymod.modulation.set_mod_kind(\pattern);
	~mynode.get_arg(\ffreq).set_modulation_mode(true);
	~mynode.get_arg(\dur).set_val(1);

	~mynode.modulation.get_modulation_mixer(\ffreq).carrier_modulator_name = ~mymod_name;
	~mynode.modulation.update_modulation_pattern;
	~mynode.play_node;
)
	~mynode.get_arg(\freq).set_modulation_mode(true);
	~mynode.get_arg(\freq).set_modulation_mode(false);
	~mynode.modulation.get_modulation_mixer(\ffreq).carrier_modulator_name = nil;
	~mynode.modulation.get_modulation_mixer(\freq).carrier_modulator_name = nil;
	~mynode.modulation.get_modulation_mixer(\freq).carrier_modulator_name = ~mynode_name;

(
	~mymod = Mdef.node(\my_detune_scorizer);
	~mymod.set_mode(\noteline);
	~mymod.get_scoreset.get_current_sheet.remove_first_rest_if_not_needed = true;

)
(
	~display = Mdef.main.panels.side.track_display;
							~ltracks = ~class_line_tracks_controller.new(Mdef.main, ~mymod, ~display);
							~ltracks.make_gui

)

	~mymod.get_scoreset.get_current_sheet.remove_first_rest_if_not_needed = true;
	~mymod.get_scoreset.get_current_sheet.remove_first_rest_if_not_needed
	~mymod.get_scoreset.update_notes
	~mymod.get_scoreset.get_notes
	~mymod.get_scoreset.notescore.remove_first_rest_if_not_needed
	~ndate = ~mymod.get_scoreset.notescore.save_data
	~mymod.get_scoreset.notescore = ~make_notescore.()
	~mymod.get_scoreset.notescore.load_data(~ndate)
	~mymod.get_scoreset.notescore.remove_first_rest_if_not_needed = true;
	~mymod.get_scoreset.notescore.get_rel_notes

	~myns = ~make_notescore.()
	~myns.set_notes(~default_noteline3.deepCopy)
	~myns.get_rel_notes
	~myns.remove_first_rest_if_not_needed
	~myns.remove_first_rest_if_not_needed = true;
	~myns.no_first_rest
	Y

	[1,2,3].drop(1)



	(
	SynthDef(\plop, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
		var sig;
		sig = SinOsc.ar(freq.lag(3));
		sig = sig * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
		sig = Pan2.ar(sig, pan, amp);
		Out.ar(out, sig);
	}).add;
	)

	a = Synth(\plop, [\freq, 200])
	a.set(\freq, 600)
	a.set(\freq, 200)
	Synth(\plop, [\freq, 200])


	(
		
	Pmono(\plop,
		\freq, Pseq([100,200],inf),
		\dur, 5,
	
	).play
	)

	Mdef.main.commands

(
"~/code/sc/seco/player_display.sc".standardizePath.load;
//~mynode = ~get_node.("osc1_l1002");
	~mynode = Mdef.node_by_index(0);
~pd = ~class_player_display.new(Mdef.main, ~mynode);
~pd.make_gui;
)
(
	
	~display = (
		winsize: 700@800,
		player_view_y: 550,
		paramsize:170@26,
		mini_param_row_count: 3,
		groupnode_row_count: 1,
	);
	Task{ 
		~pv = ~class_player_view.new(Mdef.main, ~pd, nil, ~display); 
		~windowize.(HLayout(~pv.vlayout))
	}.play(AppClock)
)



(
	'la.bla': \bla
)











	

(
	~freq = 100;
		~truecomp = (

		sfreq: 800

		);
	~ev = (
		parent: ~truecomp,
	);
p = EventPatternProxy.new;
p.source = (Pbind(
	\instrument, \osc1,
	\degree, Pseq([0,1,2,3,4,5,6],inf),
	\ffreq, Pfunc { arg ev;
		ev[\sfreq].postln;
		"kj".postln;
		1000;
		ev.compo.sfreq
	},
	\dur, 1,
	\amp, 0.1
) <> (compo:~ev)).trace;
p.play;
)
e = Environment.new;
e[\freq] = 300;
p.envir = (parent:~ev);
p.defaultEvent = (parent:~ev)

~ev.sfreq = 2000
