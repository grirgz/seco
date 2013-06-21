// ==========================================
// HELP FUNCTIONS
// ==========================================

~pedynscalar = { arg data, key, repeat = 100;
	Prout({ arg ev;
		repeat.do {
			ev = currentEnvironment[data][key].yield;
		};
	});
};

~compute_repeat = { arg ev, userepeat;
		var repeat;
		switch(userepeat,
			false, {
				repeat = ~general_sizes.safe_inf;	
			},
			\noteline, {
				if( ev[\current_mode] == \noteline ) {
					repeat = ev[\repeat];
					if(repeat == 0) { repeat = ~general_sizes.safe_inf };
				} {
					repeat = ~general_sizes.safe_inf;
				};
			},
			\stepline, {
				if( ev[\current_mode] == \stepline ) {
					repeat = ev[\repeat];
					if(repeat == 0) { repeat = ~general_sizes.safe_inf };
				} {
					repeat = ~general_sizes.safe_inf;
				};
			},
			\scoreline, {
				if( ev[\current_mode] == \scoreline ) {
					repeat = ev[\repeat];
					if(repeat == 0) { repeat = ~general_sizes.safe_inf };
				} {
					repeat = ~general_sizes.safe_inf;
				};
			},
			\sampleline, {
				if( ev[\current_mode] == \sampleline ) {
					repeat = ev[\repeat];
					if(repeat == 0) { repeat = ~general_sizes.safe_inf };
				} {
					repeat = ~general_sizes.safe_inf;
				};
			}
		);
		repeat.debug("pdynarray:repeat");
		repeat;
};

~pdynarray = { arg fun, userepeat=false;
	Prout({ arg ev;
		var idx;
		var val = 0;
		var repeat;
		userepeat.debug("=============================pdynarray:userepeat");
		[ev[\instrument], ev[\current_mode]].debug("pdynarray:current_mode");
		repeat = ~compute_repeat.(ev, userepeat);
		repeat.do {
			idx = 0;
			val = fun.(idx, ev);
			//[val, idx].debug("pdynarray val idx");
			while( { val.notNil } , { 
				ev = val.yield;
				idx = idx + 1;
				val = fun.(idx, ev);
			});
		}
	})
};


~make_event_key_reader = { arg argName, self;
	switch(argName, 
		\stepline, { 
			~pdynarray.( { arg idx; self.self.get_arg(argName)[idx] } );
		},
		\type, {
			Pif( Pkey(\stepline) > 0 , \note, \rest) // WTF with == ????? // use Pbinop
		},
		//default:
		{
			//self.data[argName] = PatternProxy.new;
			Prout({ arg ev;
				var repeat = 100000;
				var argdata = self.get_arg(argName);
				var idx, val=0;
				repeat.do {
					switch( argdata.current_kind,
						\scalar, {
							ev = argdata.scalar.val.yield;
						},
						\seq, {
							idx = 0;
							val = argdata.seq.val[idx];
							while( { val.notNil } , { 
								ev = val.yield;
								idx = idx + 1;
								val = argdata.seq.val[idx];
							});
						}
					);
				}
			})
		}
	);
};

~player_get_arg = { arg self, argName;
	// DEPRECATED
	var ret;
	//argName.dump;
	//self.get_args.do { arg an; an.debug("an====").dump };
	ret = if(self.get_args.includes(argName), {
		if([\type, \stepline].includes(argName), {
			self.data[argName];
		}, {
			//self.data[argName].source;
			self.data[argName];
		})
	}, {
		("ERROR: player: no such arg: " ++ argName ++ "!" ++ self).postln;
		nil;
	});
	//ret.debug("get_arg ret");
	ret;
};

~player_set_arg = { arg self, argName, val;
	// DEPRECATED
	if([\type, \stepline].includes(argName), {
		self.data[argName] = val;
	}, {
		//self.data[argName].source = val;
		self.data[argName] = val;
	})
};

// ==========================================
// SPECS
// ==========================================


// ControlSpec(minval = 0, maxval = 1, warp = 'lin', step = 0, default, units)
Spec.add(\tempobpm, ControlSpec(30, 300, \lin, 1, 90, "bpm"));
Spec.add(\tempobps, ControlSpec(0.5, 5, \lin, 1/60, 1.5, "bpm"));
Spec.add(\quant, ControlSpec(0, 64, \lin, 1, 4, "beats"));
Spec.add(\seq_dur, ControlSpec(0, 128, \lin, 4, 8, "beats"));
Spec.add(\dur, ControlSpec(4/128, 4, \lin, 4/64, 0.25, "s"));
Spec.add(\legato, ControlSpec(0, 1.2, \lin, 0, 0.707));
Spec.add(\sustain, ControlSpec(0.001, 5, \lin, 0, 0.2));
Spec.add(\repeat, ControlSpec(0, 100, \lin, 1, 0));
Spec.add(\pos, ControlSpec(0, 1, \lin, 0.0001, 0));
Spec.add(\amp, ControlSpec(0, 3, \lin, 0.0001, 0));
Spec.add(\wet, ControlSpec(0, 1, \lin, 0, 0));
Spec.add(\mix, ControlSpec(0, 1, \lin, 0, 0));
Spec.add(\damp, ControlSpec(0, 1, \lin, 0, 0));
Spec.add(\room, ControlSpec(0, 1, \lin, 0, 0));
Spec.add(\mdetune, ControlSpec(0.1, 2, \lin, 0, 0));
Spec.add(\pwidth, ControlSpec(0, 1, \lin, 0, 0.5));
Spec.add(\pwdetune, ControlSpec(-1, 1, \lin, 0, 0));
Spec.add(\vibratio, ControlSpec(0, 1, \lin, 0, 0));
//Spec.add(\attack, ControlSpec(0.001, 1, \exp, 0.0001, 0));
Spec.add(\spread, ControlSpec(0,1,\lin,0,0.5));

//SynthDescLib.global.synthDescs[\pulsepass].metadata;

~get_spec = { arg argName, defname=nil, default_spec=\widefreq;
	var spec = nil;
	try { 
		spec = if( SynthDescLib.global.synthDescs[defname].metadata.specs[argName].notNil, {
			SynthDescLib.global.synthDescs[defname].metadata.specs[argName].asSpec;
		})
	};
	if(spec.isNil, {
		if( argName.asSpec.notNil, {
			spec = argName.asSpec;
		}, {
			spec = default_spec.asSpec;
		});
	});
	[argName, spec].debug("get_spec");
	spec;
};

~get_special_spec = { arg argName, defname=nil, default_spec=\widefreq;
	var spec = nil;
	try { 
		spec = if( SynthDescLib.global.synthDescs[defname].metadata.specs[argName].notNil, {
			SynthDescLib.global.synthDescs[defname].metadata.specs[argName];
		})
	};
	if(spec.isNil) { [argName, defname].debug("get_special_spec: no spec found") };
	spec;
};

// ==========================================
// Default Data
// ==========================================

~default_ccrecord = [
	(dur:0.1, val:0.5),
	(dur:0.5, val:0.2),
	(dur:0.4, val:0.1),
	(dur:0.2, val:0.3),
	(dur:0.1, val:0.9),
];

// ==========================================
// PARAM VIEW
// ==========================================

~make_status_view = { arg parent, play_manager;
	// DEPRECATED, use make_status_view_horizontal
	var win, vlayout, but_layout, bt, bar, temp, pos, clock, task, color;
	var hstatetxt;
	var refresh_pos;
	var size = 120@200;

	"hein".debug;
	clock = play_manager.get_clock;

	vlayout = VLayoutView(parent, Rect(0,0,size.x,200));
	hstatetxt = StaticText(vlayout, 20);
	temp = StaticText(vlayout, 20);
	temp.string = "Tempo:" + (clock.tempo*60).asString;
	pos = StaticText(vlayout, 20);
	pos.string = "002 / 004 | 2 | 1254";
	but_layout = HLayoutView(vlayout, Rect(0,0,size.x,20));
	bt = { arg i;
		StaticText(but_layout, Rect(0,0,20,20)).background_(Color.black);
	} ! clock.beatsPerBar;
	bar = RangeSlider(vlayout, 40@20);

	refresh_pos = { 
		var posstring, barrange, bib, col;
		var clock = play_manager.get_clock;
		if(play_manager.is_playing || play_manager.is_recording) {
			clock.beatInBar.debug("beatInBar");
			posstring = play_manager.get_rel_beat.round(0.01).asString.padLeft(3, " ")
							+ "/" + play_manager.get_record_length.asString
							+ "|" + clock.beats.asString; // FIXME: find a way to zero pad
			barrange = play_manager.get_rel_beat / play_manager.get_record_length;
			bib = clock.beatInBar;
			col = if(play_manager.is_near_end) { Color.red } { Color.green };
			{
				bt[bib].background = col;
				bt.wrapAt(bib - 1).background = Color.black;
				pos.string = posstring;
				bar.range = barrange;
			}.defer;
			true; // continue
		} {
			play_manager.get_rel_beat.debug("refresh vimetro task: stopped");
			{
				pos.string = "0 /" + play_manager.get_record_length.asString + "| 0";
				bar.range = 0;
			}.defer;
			false; // exit
		}
	};



	clock.postln;
	task = TaskProxy.new;
	task.source = { 
		//play_manager.start_pos = clock.beats; // debug
		var clock = play_manager.get_clock;
		play_manager.get_rel_beat.debug("start vimetro task");
		{	
			bt.do { arg x; x.background = Color.black };
		}.defer;
		block { arg break;
			10000.do { //FIXME: fake loop
				if(refresh_pos.().not) { "BREAK".debug; break.value };
				1.wait;
			}
		}
	};

	parent.onClose = parent.onClose.addFunc { "close".postln; task.stop };

	~make_view_responder.(parent, play_manager, (
		tempo: { arg obj, msg, tempo;
			{
				temp.string = "Tempo: " ++ play_manager.get_bpm_tempo.asString;
			}.defer;
		},
		pos: { arg obj, msg, position;
			refresh_pos.(play_manager.get_clock);
		},
		visual_metronome: { arg self;
			if(self.visual_metronome_enabled) {
				play_manager.get_rel_beat.debug("start vimetro!!!");
				play_manager.get_clock.beats.debug("visual_metronome handler: clock beats");
				play_manager.get_clock.hash.debug("visual_metronome handler: clock hash");
				task.play(play_manager.get_clock,quant:1);
			} {
				play_manager.get_rel_beat.debug("stop vimetro!!!");
				task.stop;
				task.reset;
			}
		},
		head_state: { arg obj, msg, state;
			{
				switch(state,
					\prepare, { hstatetxt.string = "Prepare"; hstatetxt.background = Color.new255(255, 165, 0) },
					\record, { hstatetxt.string = "Rec"; hstatetxt.background = Color.red },
					\overdub, { hstatetxt.string = "Dub"; hstatetxt.background = Color.new255(205, 92, 92) },
					\play, { hstatetxt.string = "Play"; hstatetxt.background = Color.green },
					\stop, { hstatetxt.string = "Stop"; hstatetxt.background = Color.clear }
				);
			}.defer;
		}

	));

};

~make_status_view_horizontal = { arg parent, play_manager, size;
	var win, vlayout, but_layout, bt, bar, temp, pos, clock, task, color;
	var hstatetxt;
	var quanttxt;
	var refresh_pos;
	var fieldsize;
	size = size ?? (120@200);
	fieldsize = (size.x/8)@size.y;


	"hein".debug;
	clock = play_manager.get_clock;

	vlayout = HLayoutView(parent, Rect(0,0,size.x,size.y));
	hstatetxt = StaticText(vlayout, fieldsize);
	temp = StaticText(vlayout, fieldsize);
	temp.string = "Tempo:" + (clock.tempo*60).asString;
	pos = StaticText(vlayout, fieldsize);
	pos.string = "002 / 004 | 2 | 1254";
	but_layout = HLayoutView(vlayout, fieldsize);
	bt = { arg i;
		StaticText(but_layout, Rect(0,0,20,20)).background_(Color.black);
	} ! clock.beatsPerBar;
	bar = RangeSlider(vlayout, fieldsize);

	quanttxt = StaticText(vlayout, fieldsize);

	refresh_pos = {
		var posstring, barrange, bib, col;
		var clock = play_manager.get_clock;
		if(play_manager.is_playing || play_manager.is_recording) {
			clock.beats.debug("refresh_pos: clock beats");
			clock.hash.debug("refresh_pos: clock hash");
			clock.beatInBar.debug("beatInBar");
			posstring = play_manager.get_rel_beat.round(0.01).asString.padLeft(3, " ")
							+ "/" + play_manager.get_record_length.asString
							+ "|" + play_manager.get_total_beat.asString; // FIXME: find a way to zero pad
			barrange = play_manager.get_rel_beat / play_manager.get_record_length;
			bib = clock.beatInBar;
			col = if(play_manager.is_near_end) { Color.red } { Color.green };
			{
				bt[bib].background = col;
				bt.wrapAt(bib - 1).background = Color.black;
				pos.string = posstring;
				bar.range = barrange;
			}.defer;
			true; // continue
		} {
			play_manager.get_rel_beat.debug("refresh vimetro task: stopped");
			{
				pos.string = "0 /" + play_manager.get_record_length.asString + "| 0";
				bar.range = 0;
			}.defer;
			false; // exit
		}
	};

	clock.postln;
	task = TaskProxy.new;
	task.source = { 
		//play_manager.start_pos = clock.beats; // debug
		var clock = play_manager.get_clock;
		play_manager.get_rel_beat.debug("start vimetro task");
		{	
			bt.do { arg x; x.background = Color.black };
		}.defer;
		block { arg break;
			~general_sizes.safe_inf.do { 
				if(refresh_pos.().not) { "BREAK".debug; break.value };
				1.wait;
			}
		}
	};

	parent.onClose = parent.onClose.addFunc { "close".postln; task.stop };

	~make_view_responder.(parent, play_manager, (
		tempo: { arg obj, msg, tempo;
			{
				temp.string = "Tempo: " ++ play_manager.get_bpm_tempo.asString;
			}.defer
		},
		pos: { arg obj, msg, position;
			refresh_pos.(play_manager.get_clock);
		},
		quant: { arg obj;
			{
				quanttxt.string = "Quant: "++EventPatternProxy.defaultQuant;
			}.defer;
		},
		visual_metronome: { arg self;
			if(self.visual_metronome_enabled) {
				play_manager.get_rel_beat.debug("start vimetro!!!");
				play_manager.get_clock.beats.debug("visual_metronome handler: clock beats");
				play_manager.get_clock.hash.debug("visual_metronome handler: clock hash");
				task.play(play_manager.get_clock,quant:1);
			} {
				play_manager.get_rel_beat.debug("stop vimetro!!!");
				task.stop;
				task.reset;
			}
		},
		head_state: { arg obj, msg, state;
			{
				switch(state,
					\prepare, { hstatetxt.string = "Prepare"; hstatetxt.background = Color.new255(255, 165, 0) },
					\record, { hstatetxt.string = "Rec"; hstatetxt.background = Color.red },
					\overdub, { hstatetxt.string = "Dub"; hstatetxt.background = Color.new255(205, 92, 92) },
					\play, { hstatetxt.string = "Play"; hstatetxt.background = Color.green },
					\stop, { hstatetxt.string = "Stop"; hstatetxt.background = Color.clear }
				);

			}.defer
		}

	));

};

~class_edit_number_view = (
	new: { arg main, name, param, midi_cc;

	},
	// TODO

);

~class_simple_edit_number_view = (
	new: { arg self, main, name, value, callback;
		self = self.deepCopy;
		self.get_main = { main };
		self.name = name;
		self.val = value;
		self.callback = callback;
		self.make_bindings;
		self.make_gui;
		self;
	},


	make_bindings: { arg self;
		self.binding_responder = self.get_main.commands.make_binding_responder(\simple_edit_number, [
			[\close_window, {
				self.window.close;
			}],

			[\validate_value, {
				self[\callback].(self.textfield.value);
				self.window.close;
			}]
		

		])
	},

	make_gui: { arg self;
		var layout;
		self.window = Window.new(self.name, Rect(200,200,300,100));
		layout = VLayout.new(
			StaticText.new.string_(self.name),
			self.textfield = TextField.new
				.string_(self.val.asString);
				self.textfield,
		);
		self.window.view.keyDownAction = self.binding_responder.get_kb_responder(\simple_edit_number);
		self.window.layout_(layout);
		self.window.front;

	},

);

~make_tempo_tap_reader = {
	(
		midi_keycode: ~keycode.cakewalk[\pad][0],
		init: { arg self, action;
			self.make_responder;
			self.time_list = nil;
			self.list_size = 4;
			self.action = action;
		},

		make_responder: { arg self;
			self.responder = NoteOnResponder ({ arg src, chan, num, veloc;
					self.tap_action;
				},
				nil,
				nil,
				self.midi_keycode,
				nil
			);
		},

		clear_responder: { arg self;
			self.responder.remove;
		},

		get_time: { arg self;
			Date.getDate.rawSeconds;
		},

		get_relative_time: { arg self;
			(self.get_time - self.initial_time).clip(0, 30);
		},

		tap_action: { arg self;
			if(self.time_list.isNil) {
				self.initial_time = self.get_time;
				self.time_list = 1 ! self.list_size;
			};
			self.time_list.addFirst(self.get_relative_time);
			self.time_list.pop;
			[self.get_relative_time, self.time_list,self.get_time - self.initial_time ].debug("tap_action: reltime, tempoguess, realreltime");
			self.action;
			self.initial_time = self.get_time;
		},

		get_tempo_bps: { arg self;
			1 / (self.time_list.sum / self.list_size);
		},

		get_tempo_bpm: { arg self;
			60 / (self.time_list.sum / self.list_size);
		},
	)

};

~param_kind_to_string = { arg kind;
	switch(kind,
		\seq, { "seq" },
		\seg, { "sg" },
		\scalar, { "sca" },
		\scoreseq, { "sco" },
		\synchrone, { "syn" },
		\synchrone_rate, { "syr" },
		\bus, { "bus" },
		\recordbus, { "rbu" },
		\preset, { "pre" },
		{ "..." }
	)
};

~make_edit_number_view = { arg main, name, param, midi_cc, tempo_midi; // tempo_midi is a hack waiting to be taken out of the general code
	var win = Window.new(name,Rect(500,500,200,100));
	var parent = win;
	var vlayout, hlayout;
	var bt_name, txt_midi_label, kind, txt_midi_val, paramval, slider;
	var bsize = 23;
	var font;
	var param_view, param_responder;
	var size = 200@56;
	var lineheigth = 26;
	var tf_val;
	var validate_action;
	var key_responder;
	var old_param, old_ccpath;
	var close_window;
	var tempotr;
	font = Font.default;
	font.size = 12;
	font.setDefault;

	vlayout = VLayoutView.new(parent, Rect(0,0,size.x,size.y+45));
	bt_name = StaticText.new(vlayout, Rect(0,0,size.x,lineheigth));
	tf_val = TextField.new(vlayout, Rect(0,0,size.x,lineheigth));
	hlayout = HLayoutView.new(vlayout, Rect(0,0,size.x,lineheigth));
	slider = Slider(vlayout, Rect(0,0,size.x,5));
	txt_midi_label = StaticText.new(hlayout, Rect(0,0,bsize-7,size.y/2));
	kind = StaticText.new(hlayout, Rect(0,0,bsize,size.y/2));
	txt_midi_val = StaticText.new(hlayout, Rect(0,0,(size.x-(bsize*2)-15)/2,size.y/2));
	paramval = StaticText.new(hlayout, Rect(0,0,(size.x-(bsize*2))/2,size.y/2));

	bt_name.string = param.name;
	txt_midi_label.string = "";
	kind.string = "";
	txt_midi_val.string = "";
	paramval.string = "";

	vlayout.background = ~color_scheme.background;
	//bt_name.background = ~color_scheme.control;
	//bt_name.background = Color.newHex("343154");
	bt_name.background = Color.newHex("54516A");
	bt_name.stringColor = Color.white;
	bt_name.font = font.boldVariant;
	//txt_midi_label.background = ~color_scheme.control;
	txt_midi_label.stringColor = Color.white;
	//kind.background = ~color_scheme.control;
	kind.stringColor = Color.white;
	txt_midi_val.background = ~color_scheme.control;
	txt_midi_val.stringColor = Color.white;
	paramval.background = ~color_scheme.control;
	paramval.stringColor = Color.white;

	slider.value = param.get_norm_val;
	//tf_val.string = param.get_val;

	old_param = main.commands.ccpath_to_param(midi_cc);
	old_ccpath = main.commands.param_to_ccpath(param);
	main.commands.bind_param(midi_cc, param);

	// tempo temporary UGLY HACK
	if(tempo_midi.notNil) {
		tempotr = ~make_tempo_tap_reader.();
		tempotr.init({ arg self; tf_val.value = self.get_tempo_bpm.asString });
	};


	win.front;

	validate_action = { 
		var val = tf_val.value;
		var env, res;
		if(val.size > 0) {
			env = (t:main.play_manager.get_tempo);
			res = env.use({val.interpret.asFloat});
			res.debug("la valeur");
			param.set_val(res);
		};
	};
	close_window = {
		if(old_param.notNil) {
			old_param.debug("make_edit_number_view: old_param");
			main.commands.bind_param(midi_cc, old_param);
		};
		if(old_ccpath.notNil) {
			main.commands.bind_param(old_ccpath, param);
		};

		if(tempotr.notNil) { tempotr.clear_responder }; // tempo ugly hack

		win.close;
	};
	slider.action = { param.set_norm_val(slider.value) };

	key_responder = { arg view, char, modifiers, u, k; 
		["tempo", modifiers, u].debug("KEYBOARD INPUT");
		if( u == ~keycode.kbspecial.escape ) { close_window.() };
		if( u == ~keycode.kbspecial.enter ) { validate_action.(); close_window.(); };
	};


	tf_val.keyDownAction = key_responder;
	vlayout.keyDownAction = key_responder;
	
	// FIXME: doublon code with side.sc:make_mini_param_view
	param_responder = { arg display; (
		selected: { arg self;
			if(display.selected == 1) {
				self.name.debug("je suis select");
				bt_name.debug("bt_name");
				bt_name.background = Color.newHex("B4B1BA");
			} {
				self.name.debug("je suis DEselect");
				bt_name.background = Color.newHex("54516A");
			}
		},

		val: { arg self, msg, cellidx=0;
			var newval;
			name.debug("param_responder: val");
			{
				paramval.string = self.get_val(cellidx);

				if(slider.notNil, {
					slider.value = self.get_norm_val(cellidx);
				});
			}.defer;
		},

		kind: { arg self;
			kind.string = if(self.pkey_mode.notNil and: {self.pkey_mode}) {
				"KEY"
			} {
				if([\stepline,\scoreline,\sampleline,\noteline].includes(self.classtype)) { // FIXME: modes
					""
				} {
					~param_kind_to_string.(self.current_kind);
				}
			};
		},

		label: { arg self;
			txt_midi_label.string = self.midi.label;
		},
		midi_val: { arg self, msg, val;
			{
				txt_midi_val.string = val;
			}.defer;
		},
		blocked: { arg self, msg, blocked;
			{
				txt_midi_val.background = if(blocked.not, { ~color_scheme.led_ok }, { ~editplayer_color_scheme.led });
			}.defer;
		},
		recording: { arg self, msg, recording;
			txt_midi_label.background = if(recording, { ~editplayer_color_scheme.led }, { Color.clear });
		}
	)};
	
	~make_view_responder.(vlayout, param, param_responder.((selected:1)));

};

~make_tempo_edit_view = { arg main, midi_cc;
	var param = ~make_tempo_param.(main, \tempo, main.play_manager.get_clock);
	~make_edit_number_view.(main, "tempo", param, midi_cc, \tap_tempo);
};

~make_quant_edit_view = { arg main, midi_cc;
	var param = ~make_quant_param.(main, \quant, EventPatternProxy);
	~make_edit_number_view.(main, "quant", param, midi_cc);
};

~make_barrecord_edit_view = { arg main, midi_cc;
	var param = ~make_barrecord_param.(main, \barrecord, EventPatternProxy);
	~make_edit_number_view.(main, "barrecord", param, midi_cc);
};

~make_master_volume_edit_view = { arg main, midi_cc;
	var param = ~make_master_volume_param.(main, \volume);
	~make_edit_number_view.(main, "Master Volume", param, midi_cc);
};

~make_tempo_edit_view2 = { arg main, midi_cc;
	var win = Window.new("tempoo",Rect(500,500,200,100));
	var layout = GUI.hLayoutView.new(win, Rect(0,0,200,100));
	var ez, midi_val, tempoparam;

	ez = EZSlider(layout, 150@42,"Freq", \tempobpm,unitWidth:0, numberWidth:60,layout:\line2, margin: 1@1);
	ez.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.black, Color.yellow,nil,nil, Color.grey(0.7));
	//win.bounds = win.bounds.moveBy(-180,-460);
	win.name = "Edit tempo";
	midi_val = StaticText.new(layout,Rect(0,0,200,100));
	midi_val.string = "0.1";
	ez.value = tempoparam.get_bpm_val;

	ez.action = { arg ezs; tempoparam.set_bpm_val(ezs.value) };

	~make_view_responder.(win, tempoparam, (
		val: { arg obj, msg;
			ez.value = tempoparam.get_bpm_val;
		},
		midi_val: { arg obj, msg, val;
			midi_val.string = (val*60).asString;
		},
		blocked: { arg obj, msg, blocked;
			midi_val.background = if(blocked.not, { Color.green }, { ~editplayer_color_scheme.led });
		}
	));

	main.commands.bind_param([\knob, 0], tempoparam);

	layout.keyDownAction = { arg view, char, modifiers, u, k; 
		["tempo", modifiers, u].debug("KEYBOARD INPUT");
		if( u == ~keycode.kbspecial.escape ) { win.close };
		if( u == ~keycode.kbspecial.enter ) { win.close };
	};
	ez.numberView.keyDownAction = { arg view, char, modifiers, u, k; 
		["tempo", modifiers, u].debug("KEYBOARD INPUT");
		if( u == ~keycode.kbspecial.escape ) { win.close };
		if( u == ~keycode.kbspecial.enter ) { win.close };
	};
	win.front;

};

~make_name_button = { arg parent, label, xsize=50, ysize=50;
	var bt;
	bt = GUI.button.new(parent, Rect(50,50,xsize,ysize));
	bt.action = { 
		parent.focus(true);
		"action!".debug;
	};
	bt.states = [
		[ label, Color.black, Color.white],
		[ label, Color.white, Color.black ],
	];
	bt.value = 0;
	bt;
};

~make_step_button = { arg parent, label;
	var bt;
	bt = GUI.button.new(parent, Rect(50,50,60,50));
	bt.states = [
		[ label, Color.black, Color.white],
		[ label, Color.white, Color.black ],
	];
	bt.value = 0;
	bt;
};

~make_states = { arg label;
	var la = label.asFloat.asStringPrec(6);
	[
		[ la, Color.black, Color.white],
		[ la, Color.white, Color.black ],
	];
};

~make_val_button = { arg parent, label, width=60, height=50;
	var bt;
	bt = GUI.button.new(parent, Rect(0,0,width,height));
	bt.states = ~make_states.(label);
	bt.value = 0;
	bt;
};

~change_button_label = { arg button, label;
	var oldval = button.value;
	button.states = ~make_states.(label);
	button.value = oldval;
	button.refresh;
	//"boutton changed".debug;
};



~pretty_print_freq = { arg freq;
	// learn midi frequencies, lazy you!
	freq;
};

/////////////////////////////////////////////////////////////////////////
/////////		Control views
/////////////////////////////////////////////////////////////////////////


~make_env_view = { arg parent, default_val;
	var env, view;

	env = EnvelopeView(parent, Rect(0, 0, 230, 80))
        .drawLines_(true)
        .selectionColor_(Color.red)
        .drawRects_(true)
        .resize_(5)
        .step_(0.05)
        .thumbSize_(5);

	view = (
		env_view: env,
		set_env: { arg self, env_val;
			env.setEnv( ~adsr_event_to_env.(env_val) )
		}
	);

	view.set_env(default_val);
	view;

};

~make_env_control_view = { arg parent, player, display, param;

	var row_layout,
		bt_name,
		bloc,
		row_midi,
		row_val,
		txt_midi = Dictionary.new,
		txt_val = Dictionary.new,
		row_env,
		env_cell,
		width = 1200;
	var param_messages, midi_messages, sc_midi, sc_param;
	var midi = param.midi;



	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+10),60));
	row_layout.background = ~editplayer_color_scheme.background;


	bt_name = ~make_name_button.(row_layout, param.name);

	bloc = GUI.vLayoutView.new(row_layout, Rect(0,0,300,60));
	row_midi = GUI.hLayoutView.new(bloc, Rect(0,0,300,30));
	row_val = GUI.hLayoutView.new(bloc, Rect(0,0,300,30));


	~adsr_point_order.do { arg k;
		k.debug("creating adsr node");
		txt_midi[k] = GUI.staticText.new(row_midi, Rect(0,0,50,30));
		txt_val[k] = GUI.staticText.new(row_val, Rect(0,0,50,30));
	};

	row_env = GUI.hLayoutView.new(row_layout, Rect(0,0,200,60));

	env_cell = ~make_env_view.(row_env, ~default_adsr);

	param.name.debug("make_env_control_view: param");

	~make_view_responder.(parent, param, (
		selected: { arg self;
			display.selected.debug("env view selected");
			bt_name.value = display.selected;
		},
		val: { arg self, msg, cellidx;
			var newval;
			newval = self.get_val;
			newval.debug("make_env_control_view: newval==============");
			//self.val.debug("val==============");
			//self.debug("self==============");
			newval.keysValuesDo { arg k, v;
				txt_val[k].string = v;
			};
			env_cell.set_env(newval);
		},

		cells: { arg self; 
		},

		kind: { arg self;
		}
	));

	~adsr_point_order.do { arg key;
		~make_view_responder.(parent, param.get_param(key), (
			val: { arg self, msg, cellidx;
				var newval;
				[key, self.get_val].debug("make_env_control_view: val handler");
				txt_val[key].string = self.get_val;
				env_cell.set_env(param.get_val);
			},
			midi_val: { arg self, msg, val;
				[self.name, msg, val].debug("make_env_control_view: midi_val");
				txt_midi[key].string = val;
			},
			blocked: { arg self, msg, blocked;
				txt_midi[key].background = if(blocked.not, { Color.clear }, { ~editplayer_color_scheme.led });
			}
		))
	};
};

~make_bufnum_view = { arg parent, display, param;

	var txt_buf, bt_name;
	var param_messages, sc_param, 
		width = 1088,
		height = display.height;
	var row_layout;

	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+30),height));
	row_layout.background = display.background_color;


	bt_name = ~make_name_button.(row_layout, display.name, xsize:display.name_width, ysize:height);
	txt_buf = GUI.staticText.new(row_layout, Rect(0,0,200,height));
	txt_buf.string = "nil";

	~make_view_responder.(parent, param, (
		selected: { arg self;
			bt_name.value = display.selected;
		},

		val: { arg self, msg, cellidx;
			var newval;
			txt_buf.string = PathName.new(param.get_val).fileName;
		}

	));
};

~make_string_param_view = { arg parent, display, param;

	var txt_buf, bt_name;
	var param_messages, sc_param, 
		width = 1088,
		height = display.height;
	var row_layout;

	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+30),height));
	row_layout.background = display.background_color;


	bt_name = ~make_name_button.(row_layout, display.name, xsize:display.name_width, ysize:height);
	txt_buf = GUI.staticText.new(row_layout, Rect(0,0,200,height));
	txt_buf.string = "nil";

	~make_view_responder.(parent, param, (
		selected: { arg self;
			bt_name.value = display.selected;
		},

		val: { arg self, msg, cellidx;
			var newval;
			txt_buf.string = param.get_val.asString;
		}

	));
};


~make_noteline_view = { arg parent, display, param;
	// notes interface: get_start_silence, get_end_silence, get_notes, total_dur

	var row_layout,
		bt_name,
		bloc,
		row_midi,
		row_val,
		txt_midi = Dictionary.new,
		txt_val = Dictionary.new,
		txt_rec,
		row_env,
		env_cell,
		width = display.extparam_content_size.x ?? 900,
		height = 60,
		beats = 8,
		paraspace;
	var param_messages, midi_messages, sc_midi, sc_param;
	var ps_bg_drawf;

	"MM make_noteline_view".debug;

	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+30),height));
	row_layout.minSize = (width+30)@height;
	row_layout.background = ~editplayer_color_scheme.background;


	if(display.show_name_bloc != false ) {
		bt_name = ~make_name_button.(row_layout, display.name, xsize:display.name_width, ysize:height);
		txt_rec = GUI.staticText.new(row_layout, Rect(0,0,30,height));
		txt_rec.string = "Stop";
	};

	//paraspace = ParaSpace.new(row_layout, bounds: Rect(15, 15, width, height));
	paraspace = ParaTimeline.new(row_layout, bounds: Rect(15, 15, width, height));
	paraspace.userView.minSize = width@height;

	ps_bg_drawf = { arg wid, he, dur, numsep;
		var vdur = dur - (param.get_start_silence ?? 0) - (param.get_end_silence ?? 0);
		var sepsize = wid/numsep;
		var xcursor = 0;
		{
			//Pen.color = Color.gray(0.7);
			//Pen.addRect(Rect(param.get_start_silence*sepsize,0,(dur+eoff)*sepsize,he));

			xcursor = 0;
			Pen.color = Color.gray(0.2);
			Pen.addRect(Rect(0,0,param.get_start_silence*sepsize,he));
			Pen.fill;

			xcursor = param.get_start_silence*sepsize;
			Pen.color = ~color_scheme.control;
			Pen.addRect(Rect(xcursor,0,(vdur*sepsize),he));
			Pen.fill;

			xcursor = xcursor + (vdur*sepsize);
			Pen.color = Color.gray(0.2);
			Pen.addRect(Rect(xcursor,0,(param.get_end_silence*sepsize),he));
			Pen.fill;

			Pen.beginPath;
			Pen.color = Color.black;
			numsep.do{|i|
					Pen.line((i*sepsize)@0, (i*sepsize)@(he+10));
			};
			Pen.stroke;

			Pen.beginPath;
			Pen.color = Color.blue;
			(numsep/4).do{|i|
					Pen.line((i*sepsize*4)@0, (i*sepsize*4)@(he+10));
			};
			Pen.stroke;

			Pen.color = Color.black;
			(numsep/8).do{|i|
					Pen.line((i*sepsize*8+1)@0, (i*sepsize*8+1)@(he+10));
			};
			Pen.stroke;
		}
	};

	//paraspace.setBackgrDrawFunc_(ps_bg_drawf.(width, height, 8));

	~make_view_responder.(row_layout, param, (
		selected: { arg self;
			if(bt_name.notNil) {
				bt_name.value = display.selected;
			}
		},
		notes: { arg self;

			//var totaldur = 0, minnote=127, maxnote=0;
			//var numsep;
			//var soff = param.get_start_silence;
			//var eoff = param.get_end_silence;
			//var sum = 0;
			//var notes = self.get_notes;
			////var playeddur = self.total_dur( notes[1..(notes.lastIndex-1)] ) + (notes[0].dur - param.get_start_silence);
			//var playeddur = self.total_dur( notes );
			//playeddur.debug("playeddur");
			//

			//paraspace.clearSpace;
			//self.get_notes.do { arg no, idx;
			//	[idx, no].debug("calcul totaldur: note");
			//	if(no.midinote.isSymbolWS.not, {
			//		minnote = min(no.midinote, minnote);
			//		maxnote = max(no.midinote, maxnote);
			//	});
			//	totaldur = no.dur + totaldur
			//};

			//[totaldur, minnote, maxnote].debug("stat");

			//numsep = display.noteline_numbeats ?? max(roundUp(totaldur+1),5); // one sep per beat + offset + marge
			//
			//paraspace.setBackgrDrawFunc_(ps_bg_drawf.(width, height, playeddur, numsep));

			//self.get_notes.do { arg no, idx;
			//	[idx, no].debug("note");
			//	if(no.midinote.isSymbolWS.not, {
			//		paraspace.createNode(sum/numsep * width, no.midinote.linlin(minnote-1,maxnote+1,height,0));
			//	});
			//	if(self.notequant.notNil) {
			//		sum = sum + no.dur.round(self.notequant);
			//	} {
			//		sum = sum + no.dur;
			//	}
			//};
		},

		recording: { arg self, msg, recording;
		   {
			   if(txt_rec.notNil) {

					if( recording, {
						txt_rec.string = "Rec";
						txt_rec.background = ~editplayer_color_scheme.led;
					}, {
						txt_rec.string = "Stop";
						txt_rec.background = Color.clear;
					});
			   }
		   }.defer;
		}

	));
	row_layout;
};


~make_control_view = { arg parent, display, param, param_name=nil, btnamesize=50;
	var param_messages,
		midi_messages,
		row_layout,
		bloc,
		midibloc,
		btl_cells,
		bt_name,
		slider,
		new_cell,
		width = 1200,
		height = 30;
	var txt_midi_label, txt_midi_val;
	var sc_param, sc_midi;
	var max_cells = display.max_cells; 
	var content_view;
	var inrange;
	var ccview;
	var make_ccview;
	var make_cellsview;

	"mais quoiiii".debug;
	display.debug("il a quoiiii");
	display[\name].debug("il a quoiiii.name");
	display.name.debug("il a quoiiii.name2");

	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+10),height));
	row_layout.minSize = (width+10)@height;
	row_layout.background = ~editplayer_color_scheme.control;

	if(display.show_name_bloc != false) {
		bt_name = ~make_name_button.(row_layout, display.name, display.name_width ?? btnamesize, height);
	};

	if(display.show_midibloc, {
		midibloc = GUI.hLayoutView.new(row_layout, Rect(0,0,15+75+5+display.slider_width,height));
		txt_midi_label = GUI.staticText.new(midibloc, Rect(0,0,15,height));
		txt_midi_val = GUI.staticText.new(midibloc, Rect(0,0,75,height));

		slider = GUI.slider.new(midibloc, Rect(0,0,display.slider_width,height));
		slider.action = {
			param.set_norm_val(slider.value)
		};
			
	},{
		// spacer
		txt_midi_val = GUI.staticText.new(row_layout, Rect(0,0,30,height));
	});

	content_view = HLayoutView.new(row_layout, 	Rect(0,0,width,height));


	make_cellsview = {
		btl_cells = GUI.hLayoutView.new(content_view, Rect(0,0,width,height));
	};

	//make_cellsview.value;
	parent.onClose = parent.onClose.addFunc{
		"===========================================================".debug;
		param.name.debug("closing param view");
		"===========================================================".debug;
		"===========================================================".debug;
	};

	make_ccview = {
		ccview = UserView(content_view, Rect(30,0,width-450,height));
		//ccview.resize = 5; // TODO: what's that ?
		ccview.background_(Color.rand);

		ccview.drawFunc={|uview|
			var cur = 0;		
			var totdur = 0;
			var size = ccview.bounds.debug;
			var record = param.recordbus.get_record;
			var evs;
			if(record.isNil) {
				record = ~default_ccrecord;
			};
			evs = ~event_rel_to_abs.(record);
			evs.debug("evs");
			Pen.moveTo(0@(1-evs[0].val*size.height));
			//Pen.moveTo(10@10);
			record.do { arg ev; totdur = ev.dur + totdur; };
			evs.do { arg ev;
				Pen.lineTo((ev.time/totdur * size.width) @ (1-ev.val*size.height));
				[(ev.time/totdur * size.width) , (1-ev.val*size.height)].debug("point ev");
			};
			Pen.stroke;
		};
	};

	"mais quoiiii".debug;

	inrange = { arg sel=nil;
		var start;
		start = max_cells * display.get_bank.();
		sel = sel ?? display.get_selected_cell;
		start.debug("inrange====");
		sel.debug("inrange");
		(sel >= start && (sel < (start+max_cells))).debug("inrange");
		(sel >= start && (sel < (start+max_cells)))
	};

	~make_view_responder.(content_view, param, (
		selected: { arg self;
			if(bt_name.notNil) {
				{
					bt_name.value = display.selected;
				}.defer
			};
		},

		selected_cell: { arg self, msg, oldsel;
			
			oldsel.debug("selected cell oldsel");
			display.name.debug("selected_cell display name");
			if(oldsel.notNil && inrange.(oldsel), {
				if( btl_cells.children.size > 0 ) {
					btl_cells.children.wrapAt( oldsel % max_cells ).value = 0;
				};
			});
			if(inrange.(), {
				display.get_selected_cell.debug("selected_cell sel");
				if( btl_cells.children.size > 0 ) {
					btl_cells.children.wrapAt( display.get_selected_cell % max_cells).value = 1;
				}
			});
		},

		val: { arg self, msg, cellidx=0;
			var newval;
			cellidx.debug("val handler: cellidx");
			if(btl_cells.notNil) {
				self.get_cells[cellidx].debug("val handler: cellidx value");
				newval = self.get_cells[cellidx];
				if(btl_cells.children.size > 0) {
					~change_button_label.(btl_cells.children.wrapAt( cellidx % max_cells ), newval);
				};
				//self.debug("val changed");
				if(self.classtype == \stepline, { 
					if(btl_cells.children.size > 0) {
						btl_cells.children.wrapAt( cellidx % max_cells ).value = newval
					}
				},{
					if(slider.notNil, {
						slider.value = self.spec.unmap(newval);
					});
				});
				//btl_cells.children[ cellidx ].states.debug("======heeeeerreeeee");
				//btl_cells.children[ cellidx ] = newval;
			};
		},

		cells: { arg self; 
			var cells, bank, start, range, sel;
			param_name.debug("make_control_view: cells");
			if(btl_cells.notNil) {
				"cells removeAll===================".debug;
				btl_cells.removeAll;
				"END cells removeAll===================".debug;

				bank = display.get_bank.();

				if(self.current_kind == \seg) {
					row_layout.background = ~color_scheme.control2;
				} {
					row_layout.background = ~color_scheme.control;
				};

				cells = self.get_cells.();
				cells.debug("cellls============from "++display.name);
				start = max_cells * bank;
				range = (start..((start+max_cells)-1));
				//range.debug("cells");
				//cells[ start..((start+max_cells)-1) ].debug("cells");
				cells[ start..((start+max_cells)-1) ].do { arg cell, i;
					~make_val_button.(btl_cells, cell, width:display.cell_width??60, height:height);
					if(self.classtype== \stepline, {
						btl_cells.children[i].value = cell
					});
				};
				if(self.classtype == \control, {
					if(btl_cells.children.size > 0) {
						sel = display.get_selected_cell;
						if( sel >= start && (sel < (start+max_cells)), {
							//sel.debug("selected cell");
							//btl_cells.children.wrapAt( sel % max_cells ).debug("wrapat");
							btl_cells.children.wrapAt( sel % max_cells ).value = 1;
						})
					}
				})
			} {
				param.name.debug("cells msg: btl_cells is nil");
			}
		},

		kind: { arg self;
			self.current_kind.debug("make_control_view changed kind");
			self.get_cells.debug("make_control_view.kind get_cells");
			content_view.removeAll;
			if(self.current_kind == \recordbus) {
				make_ccview.value;
				self.changed(\record);	
			} {
				make_cellsview.value;
				self.changed(\cells);	
			};
		},

		record: { arg self;
			ccview.refresh;
		},

		label: { arg self;
			if(display.show_midibloc) {
				txt_midi_label.string = self.midi.label;
			};
		},
		midi_val: { arg self, msg, val;
			if(display.show_midibloc) {
				txt_midi_val.string = val;
			};
		},
		blocked: { arg self, msg, blocked;
			if(display.show_midibloc) {
				txt_midi_val.background = if(blocked.not, { Color.green }, { ~editplayer_color_scheme.led });
			};
		},
		recording: { arg self, msg, recording;
			if(display.show_midibloc) {
				txt_midi_label.background = if(recording, { ~editplayer_color_scheme.led }, { Color.clear });
			};
		},
		midi_key: { arg self, msg, key;
			param_messages.val(param, msg); //TODO
		}
	));
	row_layout;
};

~make_simple_control_view = { arg parent, display, param;
	var width = display.width;
	var height = display.height;
	var param_messages, sc_param;
	var bt_name, row_layout, txtval;

	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+10),height));
	row_layout.background = ~editplayer_color_scheme.control;

	bt_name = ~make_name_button.(row_layout, display.name, display.name_width, height);

	txtval = StaticText.new(row_layout, Rect(15,0,width, height));


	~make_view_responder.(parent, param, (
		selected: { arg self;
			bt_name.value = display.selected;
		},
		selected_cell: { arg self;
			param.changed(\cells);
		},

		kind: { arg self;
			self.changed(\cells);
		},

		cells: { arg self, msg, cellidx;

			param.get_val.debug("simplecontrol val");
			~seq.debugme2 = param;
			txtval.string = param.get_val;
		}

	));
};

// only used in score.sc (and side) for the moment, editplayer use make_control_view
~make_stepline_view = { arg parent, display, param;
	var param_messages,
		midi_messages,
		row_layout,
		bloc,
		midibloc,
		btl_cells,
		bt_name,
		slider,
		new_cell,
		width = 1200,
		height = 30;
	var txt_midi_label, txt_midi_val;
	var sc_param, sc_midi;
	var max_cells = display.max_cells; // FIXME: already defined in editplayer
	var inrange;
	var multiline_layout, btl_cells2;

	~make_step_cell = { arg parent, label, width, height;
		var lb;
		var str;
		lb = Button.new(parent, width@height);
		label.debug("make_step_cell");
		if(label % 8 == 0, {
			(label % 8).debug("label % 8");
			str = (label / 8)+1;
		}, {
			str = "";	
		});
		lb.states = [["x"++str]];
		//lb.align = \center;
		//lb.stringColor = ~editplayer_color_scheme.control;
		//lb.stringColor = Color.white;
		//lb.string ="X";

		lb;
	};
	//~make_step_cell = { arg parent, label, width, height;
	//	var lb;
	//	lb = StaticText.new(parent, width@height);
	//	label.debug("make_step_cell");
	//	if(label % 8 == 0, {
	//		(label % 8).debug("label % 8");
	//		lb.string = (label / 8)+1;
	//	}, {
	//		lb.string = "";	
	//	});
	//	lb.align = \center;
	//	//lb.stringColor = ~editplayer_color_scheme.control;
	//	lb.stringColor = Color.white;
	//	//lb.string ="X";

	//	lb;
	//};
	~set_step_state = { arg cell, state;
		if(state == 1, { 
			cell.background = Color.black;
		}, {
			cell.background = Color.grey;
		})
	};

	"maisiu quoiiii".debug;

	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+10),height));
	row_layout.background = ~editplayer_color_scheme.control;

	bt_name = ~make_name_button.(row_layout, display.name, display.name_width, height);

	if(display.show_midibloc, {
		midibloc = GUI.hLayoutView.new(row_layout, Rect(0,0,150,height));
		txt_midi_label = GUI.staticText.new(midibloc, Rect(0,0,15,height));
		txt_midi_val = GUI.staticText.new(midibloc, Rect(0,0,75,height));

		slider = GUI.slider.new(midibloc, Rect(0,0,30,height));
	},{
		// spacer
		//txt_midi_val = GUI.staticText.new(row_layout, Rect(0,0,30,height));
	});

	multiline_layout = GUI.vLayoutView.new(row_layout, Rect(0,0,width,height-50));
	btl_cells = GUI.hLayoutView.new(multiline_layout, Rect(0,0,width,height/2.1));
	btl_cells2 = GUI.hLayoutView.new(multiline_layout, Rect(0,0,width,height/2.1));

	"mais quoiiii".debug;

	inrange = { arg sel=nil;
		var start;
		start = max_cells * display.get_player_bank.();
		sel = sel ?? display.get_selected_cell;
		start.debug("inrange====");
		sel.debug("inrange");
		(sel >= start && (sel < (start+max_cells))).debug("inrange");
		(sel >= start && (sel < (start+max_cells)))
	};

	~make_view_responder.(parent, param, (
		selected: { arg self;
			bt_name.value = display.selected;
		},

		selected_cell: { arg self, msg, oldsel;
			
			oldsel.debug("selected cell oldsel");
			display.name.debug("selected_cell display name");
			if(oldsel.notNil && inrange.(oldsel), {
				~set_step_state.(btl_cells.children[ oldsel % max_cells ], 0);
			});
			if(inrange.(), {
				~set_step_state.(btl_cells.children[ display.get_selected_cell % max_cells ], 1);
			});
		},

		val: { arg self, msg, cellidx;
			var newval;
			var btl_cells_proxy;
			cellidx.debug("val handler: cellidx");
			self.get_cells[cellidx].debug("val handler: cellidx value");

			if(cellidx <= 15) {
				btl_cells_proxy = btl_cells;
			} {
				btl_cells_proxy = btl_cells2;
			};

			newval = self.get_cells[cellidx];
			~set_step_state.(btl_cells_proxy.children[ cellidx % max_cells ], newval);
			//self.debug("val changed");
			if(self.classtype == \stepline, { 
				~set_step_state.(btl_cells_proxy.children[ cellidx % max_cells ], newval);
			},{
				if(slider.nolNil, {
					slider.value = self.spec.unmap(newval);
				});
			});
			//btl_cells.children[ cellidx ].states.debug("======heeeeerreeeee");
			//btl_cells.children[ cellidx ] = newval;
		},

		cells2: { arg self; 
			var cells, bank, start, range, sel;
			param.name.debug("make_stepline_view: cells2");
			"cells removeAll===================".debug;
			btl_cells.removeAll;
			"END cells removeAll===================".debug;

			bank = display.get_player_bank.();

			cells = self.get_cells.();
			cells.debug("cellls============");
			start = max_cells * bank;
			range = (start..((start+max_cells)-1));
			range.debug("cells");
			cells[ start..((start+max_cells)-1) ].debug("cells");
			cells[ start..((start+max_cells)-1) ].do { arg cell, i;
				~make_step_cell.(btl_cells, start+i, width:display.cell_width??60, height:height);
				if(self.classtype== \stepline, {
					~set_step_state.(btl_cells.children[i], cell);
				});
			};
			if(self.classtype == \control, {
				sel = display.get_selected_cell;
				if( sel >= start && (sel < (start+max_cells)), {
					~set_step_state.(btl_cells.children[ sel % max_cells ], 1);
				})
			})
		},

		cells: { arg self; 
			var cells, bank, start, range, sel;
			var start2, max_cells2;
			param.name.debug("make_stepline_view: cells");
			"cells removeAll===================".debug;
			btl_cells.removeAll;
			btl_cells2.removeAll;
			"END cells removeAll===================".debug;

			bank = display.get_player_bank.();

			cells = self.get_cells.();
			cells.debug("cellls============");
			start = max_cells * bank;
			range = (start..((start+max_cells)-1));
			range.debug("cells");
			cells[ start..((start+max_cells)-1) ].debug("cells");
			cells[ start..((start+max_cells)-1) ].do { arg cell, i;
				~make_step_cell.(btl_cells, start+i, width:display.cell_width??60, height:height);
				if(self.classtype== \stepline, {
					~set_step_state.(btl_cells.children[i], cell);
				});
			};
			start2 = max_cells;
			max_cells2 = start2+max_cells;
			cells[ start2..((start2+max_cells2)-1) ].do { arg cell, i;
				(start2+i).debug("OOOOOOOOO stepline_view: start2+i make_step_cell ");
				~make_step_cell.(btl_cells2, start2+i, width:display.cell_width??60, height:height);
				if(self.classtype== \stepline, {
					~set_step_state.(btl_cells2.children[i], cell);
				});
			};
			if(self.classtype == \control, {
				sel = display.get_selected_cell;
				if( sel >= start && (sel < (start+max_cells)), {
					~set_step_state.(btl_cells.children[ sel % max_cells ], 1);
				})
			})
		},

		kind: { arg self;
			self.current_kind.debug("make_control_view changed kind");
			self.get_cells.debug("make_control_view.kind get_cells");
			self.changed(\cells);	
		},

		label: { arg self;
			txt_midi_label.string = self.midi.label;
		},
		midi_val: { arg self, msg, val;
			txt_midi_val.string = val;
		},
		blocked: { arg self, msg, blocked;
			txt_midi_val.background = if(blocked.not, { Color.green }, { ~editplayer_color_scheme.led });
		},
		recording: { arg self, msg, recording;
			txt_midi_label.background = if(recording, { ~editplayer_color_scheme.led }, { Color.clear });
		},
		midi_key: { arg self, msg, key;
			param_messages.val(param, msg); //TODO
		}
	));

};

~make_line_view = { arg kind, parent, display, param;
	var fun;
	// FIXME: modes
	fun = switch(kind,
		\noteline, { ~make_noteline_view },
		\scoreline, { ~make_noteline_view },
		\stepline, { ~make_control_view },
		\nodeline, { ~make_noteline_view },
		\sampleline, { ~make_noteline_view },
		{ kind.debug("make_line_view: line not understood"); }
	);
	fun.(parent, display, param);
};

~make_line_view2 = { arg kind, parent, display, param;
	var fun;
	// FIXME: modes
	fun = switch(kind,
		\noteline, { ~make_noteline_view },
		\scoreline, { ~make_noteline_view },
		\stepline, { ~make_stepline_view },
		\nodeline, { ~make_noteline_view },
		\sampleline, { ~make_noteline_view },
		{ kind.debug("make_line_view: line not understood"); }
	);
	fun.(parent, display, param);
};

// =======================================================================================
// =======================================================================================
// =======================================================================================
// =======================================================================================
// PARAM FACTORY
// =======================================================================================
// =======================================================================================
// =======================================================================================
// =======================================================================================

//
// Param classes:
//
//- bound to synth structure (trigger synthdef rebuild when changed)
//	- menu: represented by a dropdown menu (static)
//	- numeric: example: control the number of voices
//	- wavetable range: used by VOsc synthdefs
//	- onoff: boolean to enable/disable synthdef feature
//
//- bound to synth control
//	- buffer params
//		- bufnum: depend on samplekit
//			- bufnum_sample ?
//		- wavetable: select a wavetable, store it on buffer, give buf index
//		- waveshape: select a waveshape, store it on buffer, give buf index (not written yet)
//	- standard control: simple scalar (MIDI)
//		- special param
//			- freq
//			- amp
//	- arrayed param:
//		- custom env
//		- adsr
//	- tsustain: use dur and tempo to compute sustain for non-gated synths
//	
//- bound only to pattern
//	- recordline: special param to change player mode and store score
//	- scorekey: use the recordline to retrieve value
//	- type: handle rests
//	- control-like (probably implemented like a synth bounded param)
//		- dur: changed with keyboard shortcut
//			- segdur
//			- stretchdur
//		- sustain (only for gated synthdefs)
//	- samplekit:
//	- literal:
//		just return the value to pattern
//	- dynamic literal:
//		return the value in a pfunc
//	- simple number: use a spec (MIDI)
//	- modenv: special param to handle modenv curves
//	- empty param
//
//- bound to a variable
//	- binded number: use a getter and a setter (MIDI)
//		- quant
//		- master volume
//		- barrecord
//		- tempo
//		- free_defer_time
//
//- midi assignable
//- midi not assignable
//
//- displayed in sidepanel
//- not displayed in sidepanel

~class_param_controller = (
	classtype: \basic_param,
	new: { arg self;
		self = self.deepCopy;
	
		self;
	},

	install_midi: { arg self;
		self.midi = self.get_main.midi_center.get_midi_control_handler(self);
	},

	save_data: { arg self;
		var data = IdentityDictionary.new;
		self.archive_data.do { arg key;
			data[key] = self[key]
		};
		data;
	},

	load_data: { arg self, data;
		data.keysValuesDo { arg key, val;
			self[key] = val;
		}
	},

	vpiano: { arg self;
		{
			self.get_val;
		}
	},

	vpattern: { arg self;
		Pfunc{
			self.name.debug("Error: No pattern defined in this param_controller");
			0;
		}
	},
);

// ==========================================
// SYNTHDEF CONTROL
// ==========================================

~class_param_static_controller = (
	// static
	parent: ~class_param_controller,
	archive_data: [\name, \val, \spec],
	new: { arg self, name, spec, default_value=0;
		self = self.deepCopy;
		self.name = name;
		self.val = default_value;
		self.spec = spec;
	
		self;
	},

	set_val: { arg self, val;
		if(self.val != val) {
			self.val = val;	
			self.changed(\val)
		}
	},

	get_val: { arg self;
		self.val;
	},
);

~class_param_kind_chooser_controller = (
	// static
	parent: ~class_param_controller,
	archive_data: [\model, \name, \menu_items],
	classtype: \kind_chooser,

	model: (
		name: "Filter kind",
		uname: \filter1_kind,
		kind: \filter_kind,
		transmit: \kind,
		val_uname: \bitcrusher,
		val: 0
	),

	new: { arg self, name, variants, label;
		self = self.deepCopy;
		self.name = name;
		self.label = label;

		self.menu_items = variants;
		self.model.val_uname = self.menu_items[self.model.val].uname;
		//self.model.knobs.do { arg knobname;
		//	self.main_controller.get_arg(knobname).set_variant(self.menu_items[self.model.val])
		//};

		self;
	},

	save_data: { arg self;
		self.model;
	},

	load_data: { arg self, data;
		self.model.val = self.menu_items.detectIndex { arg item; item.uname == data.val_uname };
		self.model.val_uname = data.val_uname;
		self.changed(\val)
	},


	get_menu_items_names: { arg self;
		self.menu_items.collect { arg item;
			item.name
		}
	},

	refresh: { arg self;
		self.changed(\val);
		self.changed(\menu_items);
	},

	get_val: { arg self;
		self.model.val;
	},

	get_val_uname: { arg self;
		self.model.val_uname;
	},

	set_val: { arg self, val;
		self.model.val = val;
		self.model.val_uname = self.menu_items[val].uname;
		//self.model.knobs.do { arg knobname;
		//	//[knobname, val, self.menu_items[val]].debug("°°class_pparam_kind_controller: set_property: value: variant");
		//	self.main_controller.get_arg(knobname).set_variant(self.menu_items[val])
		//};
		//self.main_controller.update_arg(self.model.uname);
		[self.name, val].debug("class_param_kind_chooser.set_val");
		self.changed(\val);
	},	
);

~class_param_wavetable_range_controller = (
	// static
	parent: ~class_param_static_controller,
	spec: \unipolar.asSpec.copy,
	new: { arg self, name;
		self = self.deepCopy;
		self.name = name;
	
		self;
	},
);

~class_param_inlinefx_controller = (
	parent: ~class_param_static_controller,
	classtype: \inlinefx,

	new: { arg self, main, player, name;
		self = self.deepCopy;
	
		self.name = name;
		self.get_main = { main };
		self.get_player = { player };
		self.make_inline_node("ci empty_inlinefx_node");
	
		self;
	},

	load_inline_node: { arg self;
		debug("class_param_inlinefx_controller: load_inline_node");
		self.get_main.node_manager.load_inlinefx_node({ arg name;
			self.make_inline_node(name);
		})
	},

	make_inline_node: { arg self, name;
		debug("class_param_inlinefx_controller: make_inline_node");
		self.inline_node = self.get_main.node_manager.make_inline_node(name, self.get_player);
		self.changed(\val);
		self.changed(\inline_node);
	},

	get_inline_node: { arg self;
		self.inline_node
	},
	
	get_val: { arg self;
		self.inline_node.synthfun
	},
);


// ==========================================
// SYNTH CONTROL
// ==========================================

~make_control_param = { arg main, player, name, kind, default_value, spec;
	// changed messages: \selected, \selected_cell, \val, \cells, \record
	
	// modify this to add param kind:
	// here: archive_kind, subevent, update_vpattern,
	// ~param_kind_to_string
	// - side.sc: side.get_extparamlist
	// - player_display.sc: param_kinds
	// - side.sc: make_mini_param_view: player_responder.kind // not anymore, use ~param_kind_to_string
	// - matrix.sc: class_param_kind_chooser // not anymore, use player_display
	
	var param;
	var bar_length = 8;

	//name.debug("---- make_control_param");

	param = (
		name: name,
		label: name,
		classtype: \control,
		current_kind: kind,
		spec: spec,
		selected: 0,	 // bool
		selected_cell: 0,
		bar_length: bar_length,
		default_val: default_value,
		pkey_mode: false,
		muted: false,
		archive_data: [\name, \classtype, \current_kind, \spec, \selected, \selected_cell, \default_val, \noteline, \muted, \pkey_mode],
		archive_kind: [\seq, \scalar, \preset, \bus, \scoreseq, \synchrone, \synchrone_rate, \recordbus], // modulation use scalar data

		get_player: { arg self;
			player
		},

		get_main: { arg self;
			main;
		},

		save_data: { arg self;
			var data = ();
			self.archive_data.do {
				arg key;
				data[key] = self[key];
			};
			self.archive_kind.do { arg kind;
				data[kind] = ();
				[\val, \selected_cell, \record].do { arg key;
					if(self[kind][key].notNil) {
						data[kind][key] = self[kind][key];
					}
				}
			};
			data[\seq][\initialized] = self.seq.initialized;
			data;
		},

		load_data: { arg self, data;
			self.archive_data.do {
				arg key;
				self[key] = data[key];
			};
			self.archive_kind.do { arg kind;
				[\val, \selected_cell, \record].do { arg key;
					if(data[kind].notNil and: {data[kind][key].notNil}) {
						self[kind][key] = data[kind][key];
					}
				}
			};
			self.scalar.set_val( data[\scalar][\val] ); // to update bus in bus mode
			self[\seq][\initialized] = data.seq.initialized;
			self.current_kind = nil; self.change_kind(data[\current_kind]);
		},

		destructor: { arg self;
			self.bus.bus.free;
			self.scalar.bus.free;
		},

		set_label: { arg self, name;
			name.debug("SETLABEL");
			self.label = name;
			self.changed(\label);
		},

		get_label: { arg self, name;
			self.label ?? self.name;
		},

		set_abs_label: { arg self, val;
			//debug("GNI2");
			self.abs_label = val;
			self.changed(\abs_label);
		},

		get_abs_label: { arg self;
			var res;
			//self.debug("GNI");
			res = self.abs_label ?? self.label ?? self.name;
			//self.debug("GNI3");
			res;
		},

		set_spec: { arg self, val;
			self.spec = val;
			self.changed(\val);
		},

		///////////////////////// inline

		set_inline_synthfun: { arg self, val;
			self.inline_synthfun = val;
			self.inline_synthfun_thunk = ArgThunk.new(val);
		},

		get_inline_synthfun: { arg self;
			self[\inline_synthfun];
		},

		get_inline_synthfun_thunk: { arg self;
			self[\inline_synthfun_thunk];
		},

		///////////////////////// kinds

		seq: (
			val: if(default_value.isArray, { default_value.asList }, { (default_value ! bar_length).asList }),
			selected_cell: 0,
			initialized: false,

			init: { arg self, defval=default_value;
				param.default_val = defval;
				self.val = (defval ! bar_length).asList;
				self.initialized = true;
			},

			//FIXME: handle others "out of range" exceptions
			set_norm_val: { arg self, norm_val, idx=nil;
				idx = idx ?? self.selected_cell;
				self.val.wrapPut(idx, param.spec.map(norm_val));
				param.changed(\val, idx % self.val.size);
			},
			get_norm_val: { arg self, idx=nil;
				idx = idx ?? self.selected_cell;
				param.spec.unmap(self.val.wrapAt(idx));
			},
			set_val: { arg self, val, idx=nil;
				idx = idx ?? self.selected_cell;
				self.val.wrapPut(idx, val);
				param.changed(\val, idx % self.val.size);
			},
			get_val: { arg self, idx=nil;
				idx = idx ?? self.selected_cell;
				self.val.wrapAt(idx);
			},

			add_cells: { arg self, cells;
				self.val.addAll(cells);
				param.changed(\cells);
			},

			remove_cells: { arg self, num;
				self.val = self.val[.. (self.val.size - num - 1) ];
				param.changed(\cells);
			},

			get_cells: { arg self; self.val },

			select_cell: { arg self, idx;
				var oldsel = self.selected_cell;
				oldsel.debug("this is oldsel from seq");
				self.selected_cell = idx;
				param.changed(\selected_cell, oldsel); // view clear old selection and call get_selected_cell to get new selection
			},

			get_selected_cell: { arg self;
				self.selected_cell;
			},

			change: { arg self, fun;
				self.val = fun.(self.val);
				param.changed(\cells);
			}
		),

		scalar: (
			//quoi: { "QUOI".debug; }.value,
			muted: false,
			should_free_bus_mode: false,
			bus_mode: false,
			bus_mode_counter: 0,
			selected_cell: 0, // always 0
			val: if(default_value.isArray, { default_value[0] }, { default_value }),

			set_val: { arg self, val, idx=nil;
				self.val = val;
				if(self.bus_mode) {
					self.bus.set(self.val)
				};
				param.changed(\val, 0);
			},

			get_val: { arg self; 
				self.val
			},

			set_norm_val: { arg self, norm_val;
				self.set_val( param.spec.map(norm_val) )
			},

			get_norm_val: { arg self;
				param.spec.unmap(self.val);
			},

			mute: { arg self, val;
				// effect bypassing with mix
				if(self.muted != val) {
					self.muted = val;
					if(val) {
						self.oldval = self.val;
						self.set_val(0);
					} {
						self.set_val(self.oldval);
					}
				}
			},

			get_cells: { arg self; [self.val] },

			select_cell: { arg self, idx; param.seq.selected_cell = idx }, // when changing kind, correct cell is selected in colselect mode
			get_selected_cell: { arg self; 0 },
			add_cells: {},
			remove_cells: {}
		),

		synchrone: (
			muted: false,
			selected_cell: 0, // always 0
			val: if(default_value.isArray, { default_value[0] }, { default_value }),

			set_val: { arg self, val, idx=nil;
				self.val = val;
				param.changed(\val, 0);
			},

			get_val: { arg self; 
				self.val
			},

			set_norm_val: { arg self, norm_val;
				self.set_val( param.spec.map(norm_val) )
			},

			get_norm_val: { arg self;
				param.spec.unmap(self.val);
			},

			mute: { arg self, val;
				// effect bypassing with mix
				if(self.muted != val) {
					self.muted = val;
					if(val) {
						self.oldval = self.val;
						self.set_val(0);
					} {
						self.set_val(self.oldval);
					}
				}
			},

			get_cells: { arg self; [self.val] },

			select_cell: { arg self, idx; param.seq.selected_cell = idx }, // when changing kind, correct cell is selected in colselect mode
			get_selected_cell: { arg self; 0 },
			add_cells: {},
			remove_cells: {}
		
		),

		modulation: (
			selected_cell: 0, // always 0
			val: if(default_value.isArray, { default_value[0] }, { default_value }),

			set_val: { arg self, val, idx=nil;
				param.scalar.set_val(val, idx);
			},
			get_val: { arg self; 
				param.scalar.get_val;
			},

			set_norm_val: { arg self, norm_val;
				param.scalar.set_norm_val(norm_val);
			},
			get_norm_val: { arg self;
				param.scalar.get_norm_val;
			},

			get_cells: { arg self; 
				param.scalar.get_cells;
			},

			select_cell: { arg self, idx; param.seq.selected_cell = idx }, // when changing kind, correct cell is selected in colselect mode
			get_selected_cell: { arg self; 0 },
			add_cells: {},
			remove_cells: {}
		
		),

		recordbus: (
			record: nil,
			initialized: false,

			set_record: { arg self, record;
				if(self.record.isNil) {
					self.record = record;
					player.build_real_sourcepat;
				} {
					self.record = record
				};
				self.changed(\record);
			},

			get_record: { arg self;
				self.record
			},

			init: { arg self, val;
				if(param.bus.bus.isNil) {
					param.bus.bus = Bus.control(s, 1);
				} {
					param.name.debug("recordbus.init: bus already exists");
				};
				param.bus.bus.debug("recordbus: init the bus!");
				if(val.notNil) { self.set_val(val); };
				self.initialized = true;
			},

			get_bus: { arg self;
				param.bus.bus;
			},

			set_bus: { arg self, val;
				param.bus.bus = val;
			},

			set_val: { arg self, val, idx=nil;
				// do nothing because values come from the record
			},

			mute: { arg self, val;
				// FIXME: who call this ? the player when is audiotrack and param is amp
			},

			get_val: { arg self; param.bus.get_val },

			set_norm_val: { arg self, norm_val;
				// do nothing because values come from the record
			},

			get_norm_val: { arg self;
				param.spec.unmap(self.get_val);
			},

			get_cells: { arg self; [self.get_val] },

			select_cell: { arg self, idx; param.seq.selected_cell = idx }, // when changing kind, correct cell is selected in colselect mode
			get_selected_cell: { arg self; 0 },
			add_cells: {},
			remove_cells: {},

			vpattern: { arg self;
				var vals = List.new, durs = List.new;
				// return the pattern which set continously the bus according to record
				if(self.record.notNil) {
					self.record.do { arg note;
						vals.add(note.val);
						durs.add(note.dur);
					};
					Pbind(
						\type, \bus,
						\notes, Plazy({ Pseq(self.record) }),
						\array, Pfunc { arg ev; param.spec.map(ev[\notes].val) },
						\out, self.get_bus,
						\dur, Pfunc { arg ev; ev[\notes].dur }
					)
				} {
					nil
				}
			}
		),

		bus: (
			// dummy functions
			//quoi: { "QUOI".debug; }.value,
			selected_cell: 0, // always 0
			val: if(default_value.isArray, { default_value[0] }, { default_value }),
			busindex: nil,
			muted: false,
			initialized: false,
			bus: nil,

			init: { arg self, val;
				if(self.bus.isNil) {
					self.bus = Bus.control(s, 1);
				} {
					param.name.debug("bus.init: bus already exists");
				};
				self.bus.debug("init the bus!");
				if(val.notNil) { self.set_val(val); };
				self.initialized = true;
			},

			get_bus: { arg self;
				self.bus;
			},

			set_bus: { arg self, val;
				self.bus = val;
			},

			set_val: { arg self, val, idx=nil;
				//[val, self.bus.index].debug(">>>>>>>>>> make_control_param: Setting val");
				self.bus.set(val);
				self.val = val;
				param.changed(\val, 0);
			},

			mute: { arg self, val;
				if(self.muted != val) {
					self.muted = val;
					if(val) {
						self.oldval = self.val;
						self.set_val(0);
					} {
						self.set_val(self.oldval);
					}
				}
			},

			get_val: { arg self; self.val },

			set_norm_val: { arg self, norm_val;
				self.set_val(param.spec.map(norm_val));
				//self.bus.set(self.val);
				//param.changed(\val, 0);
			},
			get_norm_val: { arg self;
				param.spec.unmap(self.val);
			},

			get_cells: { arg self; [self.val] },

			select_cell: { arg self, idx; param.seq.selected_cell = idx }, // when changing kind, correct cell is selected in colselect mode
			get_selected_cell: { arg self; 0 },
			add_cells: {},
			//quoii: { "QUOI3".debug }.value,
			remove_cells: {}
			

		),
		

		// preset subobject here
		//		need a corresponding spec

		/////////////////////////

		select_param: { arg self;
			self.selected = 1;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		// ============== polymorph API

		select_cell: { arg self, idx;
			idx.debug("called select_cell!!!");
			self[self.current_kind].select_cell(idx);
		},

		get_selected_cell: { arg self;
			self[self.current_kind].get_selected_cell
		},

		set_norm_val: { arg self, val;
			self[self.current_kind].set_norm_val(val)
		},

		set_val: { arg self, val;
			self[self.current_kind].set_val(val)
		},

		get_norm_val: { arg self;
			//self.current_kind.debug("control_param.get_norm_val: self.current_kind");
			self[self.current_kind].get_norm_val
		},

		get_val: { arg self;
			self[self.current_kind].get_val
		},

		add_cells: { arg self, cells;
			self[self.current_kind].add_cells(cells)
		},

		remove_cells: { arg self, num;
			self[self.current_kind].remove_cells(num)
		},

		get_cells: { arg self;
			self[self.current_kind].get_cells
		},

		// ===================

		mute: { arg self, val;
			self.muted = val;
		},
		
		change_kind: { arg self, kind;
			[name, kind].debug("change_kind");
			if(kind != self.current_kind) {
				if(self.current_kind == \recordbus) {
					// if old kind was recordbus, remove it from player pattern
					player.remove_ccbus(self);
				};
				self.current_kind = kind;
				if(kind == \seq && { self.seq.initialized.not } ) { self.seq.init(self.scalar.get_val) };
				if(kind == \bus && { self.bus.initialized.not } ) { self.bus.init(self.scalar.get_val) };
				if(kind == \recordbus) {
					"changed to recordbus".debug;
					if (self.recordbus.initialized.not) {
						self.recordbus.init(self.scalar.get_val)
					};
					player.add_ccbus(self);
				};
				[name, kind].debug("CHANGED KIND!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				self.update_vpattern;
				self.changed(\kind);
			}
		},

		set_bus_mode: { arg self, val=true;
			var reject = [\sustain, \dur, \repeat, \stepline, \segdur, \stretchdur];
			if(reject.includes(self.name).not and: {self.get_main.model.bus_mode_enabled}) {
				//if(val != self.scalar.bus_mode) {
					[self.name, val, self.scalar.bus_mode, self.scalar.bus_mode_counter]
						.debug("make_control_param: set_bus_mode: name, val, mode, count");
					if(val) {
						if(self.scalar.bus_mode_counter == 0) {
							debug("make_control_param: set_bus_mode: make bus");
							self.scalar.should_free_bus_mode = false;
							self.scalar.bus = Bus.control(s,1);
							self.scalar.bus.set(self.scalar.val);
							self.scalar.bus_mode = true;
							self.update_vpattern;
						};
						self.scalar.bus_mode_counter = self.scalar.bus_mode_counter + 1;
					} {
						self.scalar.bus_mode_counter = max(self.scalar.bus_mode_counter - 1, 0);
						if(self.scalar.bus_mode_counter == 0) {
							debug("make_control_param: set_bus_mode: free bus");
							self.scalar.bus_mode = false;
							self.scalar.should_free_bus_mode = true;
							//self.scalar.bus.free;
							self.update_vpattern;
							//self.scalar.bus = nil;
						}
					};

				//};
			}
		},

		set_pkey_mode: { arg self, set=true;
			self.pkey_mode = set;
			self.changed(\kind);
		},

		refresh: { arg self;
			self.changed(\kind);
			self.changed(\val);
			//self.changed(\cells);
			self.changed(\selected);
			self.midi.refresh;
		},

		vpiano: { arg self, spatch;
			{
				switch(self.current_kind,
					\preset, { self.preset.val[self.preset.selected_cell] },
					\bus, { self.bus.get_bus.asMap },
					\modulation, {
						if(spatch.notNil) {
							{
								var bus;
								bus = spatch.get_mod_bus(player.uname, self.name);
								if(bus.isNil) {
									[player.uname, self.name].debug("============== vpiano: mod: bus is nil");
									self.scalar.val;
								} {
									bus.asMap;
								}
							}
						} {
							[player.uname, self.name].debug("vpiano: param modulation: spatch not found");
							{ self.scalar.val };
						};

					},
					//default
					{ self.scalar.val }
				);
			}
		},

		update_vpattern: { arg self;
			var kind, mode;
			var pat_val, score_val;
			kind = self.current_kind;
			mode = player.get_mode;
			self.name.debug("class_param_controller: update_vpattern");
			score_val = switch(self.name,
				\dur, {
					{ arg ev;
						if(ev[mode].dur.notNil) {
							//[player.uname, self.name, mode, ev[mode], ev[\stretchdur], ev].debug("CONTROL param: vpattern");
							ev[mode].dur * ev[\stretchdur];
						}
					}
				},
				\freq, {
					{ arg ev;
						//ev.debug("vpattern: freq");
						if(ev[mode].midinote.notNil) {
							ev[mode].midinote.midicps
						}
					};
				},
				\amp, { // FIXME: handle sampleline; does pseg/bus need velocity adjusting ?
					{ arg ev;
						ev.debug("make_control_param: update_vpattern: amp");
						//if(ev[mode].velocity.notNil) {
						//	main.calcveloc(self.scalar.get_val,(ev[mode].velocity ?? 0.5));	
						//}
						nil // velocity should be handled in synthdef
					};
					nil;
				},
				\sustain, {
					if([\scoreline, \sampleline].includes(mode)) {
						nil;
					} {
						{ arg ev;
							if(ev[mode].sustain.notNil) {
								ev[mode].sustain
							} 
						}
					}
				},
				\velocity, {
					{ arg ev;
						if(ev[mode].velocity.notNil) {
							ev[mode].velocity
						} 
					}
				},
				// else
				{
					nil
				}
			);
			pat_val = 
				if(self.pkey_mode) {
					{ arg self, ev;
						if(ev.includesKey(self.name)) {
							ev = ev[self.name].yield;
						} {
							ev = self.scalar.get_val.yield;
						}
					}
				} {
					switch(kind, 
						\scalar, {
							if(self.scalar.should_free_bus_mode) {
								//debug("vpattern: BUS MODE: free bus");
								{ arg self, ev;
									var oldbus = self.scalar.bus;
									self.scalar.should_free_bus_mode = false;
									// FIXME: defer free to avoid glitch bug ?
									self.scalar.bus = nil;
									{
										oldbus.free;
									}.defer(1);
									ev = self.scalar.get_val.yield;
									self.update_vpattern;
									ev;
								}
							} {
								if(self.scalar.bus_mode) {
									if(self.scalar.bus.isNil or: {self.scalar.bus.index.isNil}) {
										debug("~make_control_param: vpattern: scalar: bus_mode: Error: bus not allocated");
										{ arg self, ev;
											ev = self.scalar.get_val.yield;
										}
									} {
										//debug("vpattern: BUS MODE enabled");
										{ arg self, ev;
											ev = self.scalar.bus.asMap.yield;
										}
									};
								} {
									//debug("vpattern: NO BUS MODE");
									{ arg self, ev;
										ev = self.scalar.get_val.yield;
									}
								}
							};
						},
						\scoreseq, {
							{ arg self, ev;
								var val;
								// FIXME: use name, label, abs_label ?
								//val = ev[mode][self.name] ?? self.scalar.get_norm_val;
								val = ev[mode][self.name];
								//ev[mode]
								if(val.isNil) {
									val = self.scalar.get_val;
								} {
									//val = self.spec.map(val);
									val = val * self.scoreseq.get_val;
								};
								ev = val.yield;
							}
						},
						\synchrone, {
							{ arg self, ev;
								ev = (self.synchrone.get_val / thisThread.clock.tempo).yield;
							}
						},
						\synchrone_rate, {
							{ arg self, ev;
								ev = (self.synchrone.get_val * thisThread.clock.tempo).yield;
							}
						},
						\seq, {
							{ arg self, ev;
								var idx, val;
								idx = 0;
								val = self.seq.val[idx];
								while( { val.notNil } , { 
									//ev.debug("seQ: ev");
									ev = val.yield;
									idx = idx + 1;
									val = self.seq.val[idx];
								});
							}
						},
						\seg, {
							//[
							//	ev[\elapsed], ev[\segdur], 
							//	(self.seq.val.size),
							//	ev[\elapsed]/ev[\segdur], 
							//	(ev[\elapsed]/ev[\segdur]) % (self.seq.val.size),
							//	self.seq.val.blendAt((ev[\elapsed]/ev[\segdur]) % (self.seq.val.size))
							//].debug("seggggggggggggggggg: elapsed, segdur, size, el/dur, el/dur%size, res");
							//self.seq.debug("seg: seq");
							//self.seq.val.debug("seg: seq.val");
							//ev.debug("seg: ev");
							//self.seg.debug("seg: seg");
							//ev = (self.seq.val++[self.seq.val[0]]).blendAt((ev[\elapsed]/ev[\segdur]) % (self.seq.val.size)).yield;
							//ev = 500.yield;
							//[
							//	ev[\elapsed], ev[\segdur], 
							//	ev[\elapsed]/ev[\segdur], 
							//	(self.seg.val.size),
							//	(ev[\elapsed]/ev[\segdur]) % (self.seg.val.size),
							//	self.seg.val.blendAt((ev[\elapsed]/ev[\segdur]) % (self.seg.val.size))
							//].debug("seggggggggggggggggg: elapsed, segdur, size, el/dur, el/dur%size, res");
							{ arg self, ev;
								ev = (self.seg.val++[self.seg.val[0]]).blendAt((ev[\elapsed]/ev[\segdur]) % (self.seg.val.size)).yield;
							}
						},
						\preset, {
							{ arg self, ev;
								ev = self.preset.val[self.preset.selected_cell].yield
							}
						},
						\modulation, {
							{ arg self, ev;
								var bus;
								if(ev[\ppatch].notNil) {
									bus = ev[\ppatch].get_mod_bus(player.uname, self.name);
									if(bus.isNil or:{bus.index.isNil}) { // why bus index can be freed ?
										[player.uname, self.name, ev].debug("============== bus is nil: ev");
										ev = self.scalar.val.yield;
									} {
										ev = bus.asMap.yield;
									}
								} {
									[ev, player.uname, self.name].debug("param modulation: ppatch not found");
									ev = self.scalar.val.yield;
								};
							}
						},
						\bus, {
							{ arg self, ev;
								ev = self.bus.get_bus.asMap.yield;
							}
						},
						\recordbus, {
							{ arg self, ev;
								ev = self.bus.get_bus.asMap.yield;
							}
						},
						// else
						{
							{ arg self, ev;
								[param.name, self.current_kind].debug("ERROR: param kind dont match");
								ev = 0.yield;
							}
						}
					)
				};

			self.get_vpattern_value = if(score_val.isNil) {
				pat_val;
			} {
				if(mode == \stepline) {
					pat_val;
				} {
					{ arg self, ev;
						var val;
						val = score_val.(ev);
						if(val.isNil) {
							ev = pat_val.(self, ev);
						} {
							ev = val.yield;
						};
					}
				}
			};
			
		},

		vpattern: { arg self; 
		//vpattern_static: { arg self; 
			self.update_vpattern;

			Prout({ arg ev;
				var repeat = ~general_sizes.safe_inf;
				repeat.do { arg x;
					ev = self.get_vpattern_value(ev);
				}
			});
		},

		vpattern_dyn: { arg self; 
			var score_val;
			var pat_val;
			var get_val;
			var veloc_ratio = main.model.velocity_ratio;
			score_val = switch(self.name,
				\dur, {
					{ arg ev, mode;
						if(ev[mode].dur.notNil) {
							//[player.uname, self.name, mode, ev[mode], ev[\stretchdur], ev].debug("CONTROL param: vpattern");
							ev[mode].dur * ev[\stretchdur];
						}
					}
				},
				\freq, {
					{ arg ev, mode;
						//ev.debug("vpattern: freq");
						if(ev[mode].midinote.notNil) {
							ev[mode].midinote.midicps
						}
					};
				},
				\amp, { // FIXME: handle sampleline; does pseg/bus need velocity adjusting ?
					{ arg ev, mode;
						if(ev[mode].velocity.notNil) {
							main.calcveloc(self.scalar.get_val,(ev[ev[\current_mode]].velocity ?? 0.5));	
						}
					};
				},
				\sustain, {
					{ arg ev, mode;
						if(ev[mode].sustain.notNil and:{mode != \scoreline}) {
							ev[mode].sustain
						} 
					}
				},
				// else
				{
					nil
				}
			);
			pat_val = { arg ev, mode, kind;
				var idx, val;
				if(self.pkey_mode and: { ev.includesKey(self.name) }) {
					ev = ev[self.name].yield;
				} {
					switch(kind, 
						\scalar, {
							if(self.scalar.should_free_bus_mode) {
								//debug("vpattern: BUS MODE: free bus");
								self.scalar.should_free_bus_mode = false;
								self.scalar.bus.free;
								self.scalar.bus = nil;
							};
							if(self.scalar.bus_mode) {
								if(self.scalar.bus.isNil or: {self.scalar.bus.index.isNil}) {
									debug("~make_control_param: vpattern: scalar: bus_mode: Error: bus not allocated");
									ev = self.scalar.get_val.yield;
								} {
									//debug("vpattern: BUS MODE enabled");
									ev = self.scalar.bus.asMap.yield;
								};
							} {
								//debug("vpattern: NO BUS MODE");
								ev = self.scalar.get_val.yield;
							}
						},
						\seq, {
							idx = 0;
							val = self.seq.val[idx];
							while( { val.notNil } , { 
								//ev.debug("seQ: ev");
								ev = val.yield;
								idx = idx + 1;
								val = self.seq.val[idx];
							});
						},
						\seg, {
							//[
							//	ev[\elapsed], ev[\segdur], 
							//	(self.seq.val.size),
							//	ev[\elapsed]/ev[\segdur], 
							//	(ev[\elapsed]/ev[\segdur]) % (self.seq.val.size),
							//	self.seq.val.blendAt((ev[\elapsed]/ev[\segdur]) % (self.seq.val.size))
							//].debug("seggggggggggggggggg: elapsed, segdur, size, el/dur, el/dur%size, res");
							//self.seq.debug("seg: seq");
							//self.seq.val.debug("seg: seq.val");
							//ev.debug("seg: ev");
							//self.seg.debug("seg: seg");
							//ev = (self.seq.val++[self.seq.val[0]]).blendAt((ev[\elapsed]/ev[\segdur]) % (self.seq.val.size)).yield;
							//ev = 500.yield;
							//[
							//	ev[\elapsed], ev[\segdur], 
							//	ev[\elapsed]/ev[\segdur], 
							//	(self.seg.val.size),
							//	(ev[\elapsed]/ev[\segdur]) % (self.seg.val.size),
							//	self.seg.val.blendAt((ev[\elapsed]/ev[\segdur]) % (self.seg.val.size))
							//].debug("seggggggggggggggggg: elapsed, segdur, size, el/dur, el/dur%size, res");
							ev = (self.seg.val++[self.seg.val[0]]).blendAt((ev[\elapsed]/ev[\segdur]) % (self.seg.val.size)).yield;
						},
						\preset, {
							ev = self.preset.val[self.preset.selected_cell].yield
						},
						\modulation, {
							var bus;
							if(ev[\ppatch].notNil) {
								bus = ev[\ppatch].get_mod_bus(player.uname, self.name);
								if(bus.isNil or:{bus.index.isNil}) { // why bus index can be freed ?
									[player.uname, self.name, ev].debug("============== bus is nil: ev");
									ev = self.scalar.val.yield;
								} {
									ev = bus.asMap.yield;
								}
							} {
								[ev, player.uname, self.name].debug("param modulation: ppatch not found");
								ev = self.scalar.val.yield;
							};
						},
						\bus, {
							ev = self.bus.get_bus.asMap.yield;
						},
						\recordbus, {
							ev = self.bus.get_bus.asMap.yield;
						},
						// else
						{
							[param.name, self.current_kind].debug("ERROR: param kind dont match");
							ev = 0.yield;
						}
					)
				}
			};
			get_val = if(score_val.isNil) {
				pat_val;
			} {
				{ arg ev, mode, kind;
					var val;
					if(mode == \stepline) {
						ev = pat_val.(ev, mode, kind);
					} {
						val = score_val.(ev, mode);
						if(val.isNil) {
							ev = pat_val.(ev, mode, kind);
						} {
							ev = val.yield;
						};
					}

				}
			};

			Prout({ arg ev;
				var repeat = ~general_sizes.safe_inf;
				var idx, val=0;
				var kind, mode;
				repeat.do { arg x;
					kind = self.current_kind;
					mode = player.get_mode;
					//[self.name, x, ev].debug("============== ev");
					ev = get_val.(ev, mode, kind);
				}
			});
		},

		vpattern_old: { arg self; 
			var segf, scalf, pref, scalm;
			// segf: noteline seg
			// scalf: noteline scalar
			// scalm: sampleline scalar
			// pref: noteline preset
			var score_val;
			var veloc_ratio = main.model.velocity_ratio;
			switch(self.name,
				\dur, {
					score_val = { arg ev, mode;
						if(ev[mode].dur.notNil) {
							ev[mode].dur * ev[\stretchdur];
						} {
							self.scalar.val
						}
					};
					segf = { arg ev;
						
						//[ev[\noteline].dur, ev[\stretchdur]].debug("vpattern: dur: segf");
						ev[\noteline].dur * ev[\stretchdur];
					};
					scalm = { arg ev;
						//ev[\sampleline]
						ev.debug("dury'a quoi la dedans");
						ev[\sampleline].debug("durjuste lui ce connard");
						ev[\sampleline].dur * ev[\stretchdur];
					};
					scalf = pref = segf;
				},
				\freq, {
					score_val = { arg ev, mode;
						if(ev[mode].midinote.notNil) {
							ev[mode].midinote.midicps
						} {
							self.scalar.val
						}
					};
					segf = { arg ev;
						ev[\noteline].midinote.midicps;	
					};
					scalm = { arg ev; self.scalar.val };
					scalf = pref = segf;
				},
				\amp, { // FIXME: handle sampleline; does pseg/bus need velocity adjusting ?
					score_val = { arg ev, mode;
						if(ev[mode].velocity.notNil) {
							main.calcveloc(self.scalar.get_val,(ev[\noteline].velocity));	
						} {
							self.scalar.val
						}
					};
					segf = { arg ev;
						main.calcveloc(self.scalar.get_val,(ev[\noteline].velocity ?? 0));	
					};
					scalm = { arg ev; self.scalar.val };
					scalf = pref = segf;
				},
				\sustain, {
					score_val = { arg ev, mode;
						if(ev[mode].sustain.notNil) {
							ev[mode].sustain
						} {
							self.scalar.val
						}
					};
					segf = { arg ev;
						ev[\noteline].sustain;	
					};
					scalm = { arg ev;
						//ev[\sampleline]
						ev.debug("y'a quoi la dedans");
						ev[\sampleline].debug("juste lui ce connard");
						ev[\sampleline].sustain;
					};
					scalf = pref = segf;
				},
				// else
				{
					segf = { arg ev;
						//[ev[\elapsed], ev[\segdur], ev.dump].debug("in segggggggggggggggggg");
						self.seq.val.blendAt((ev[\elapsed]/ev[\segdur]) % (self.seq.val.size));
					};
					scalf = { arg ev; self.scalar.val };
					scalm = { arg ev; self.scalar.val };
					pref = { arg ev; self.preset.val[self.preset.selected_cell] };
				}
			);
			Prout({ arg ev;
				var repeat = ~general_sizes.safe_inf;
				var idx, val=0;
				repeat.do {
					//[name, ev].debug("################################## prout!!!!");
					//ev.dump;
					if(self.pkey_mode and: { ev.includesKey(self.name) }) {
						ev = ev[self.name].yield;
					} {
						switch( self.current_kind,
							\scalar, {
								//ev.debug("=========== in scalar ev");
								8.do {		// hack to be in phase when changing kind (should be the size of stepline)
									// FIXME: modes
									switch(player.get_mode,
										\sampleline, {
											[name, scalm.value(ev)].debug("############# scalm!!");
											ev = scalm.value(ev).yield;
											//ev = 1.yield;
										},
										\noteline, {
											ev = scalf.value(ev).yield;
										},
										\scoreline, {
											ev = score_val.value(ev).yield;
										},
										\stepline, {
											ev = self.scalar.val.yield;
										}
									);
								}
								//ev.debug("=========== in scalar ev END");
							},
							\seg, {
								//[
								//	ev[\elapsed], ev[\segdur], 
								//	ev[\elapsed]/ev[\segdur], 
								//	(self.seg.val.size),
								//	(ev[\elapsed]/ev[\segdur]) % (self.seg.val.size),
								//	self.seg.val.blendAt((ev[\elapsed]/ev[\segdur]) % (self.seg.val.size))
								//].debug("seggggggggggggggggg: elapsed, segdur, size, el/dur, el/dur%size, res");
								//ev = (self.seg.val++[self.seg.val[0]]).blendAt((ev[\elapsed]/ev[\segdur]) % (self.seg.val.size)).yield;
								[
									ev[\elapsed], ev[\segdur], 
									ev[\elapsed]/ev[\segdur], 
									(self.seq.val.size),
									(ev[\elapsed]/ev[\segdur]) % (self.seq.val.size),
									self.seq.val.blendAt((ev[\elapsed]/ev[\segdur]) % (self.seq.val.size))
								].debug("seggggggggggggggggg: elapsed, segdur, size, el/dur, el/dur%size, res");
								ev = (self.seq.val++[self.seq.val[0]]).blendAt((ev[\elapsed]/ev[\segdur]) % (self.seq.val.size)).yield;
							},
							\seq, {
								if(player.get_mode == \noteline, {
									//ev.debug("=========== in noteline ev");
									//segf.debug("=========== in noteline segf");
									//segf.value(ev).debug("=========== in noteline");
									ev = segf.value(ev).yield;
								}, {
									if(self.name == \stepline) {
										if(ev[\stepline].notNil) {
											ev[\stepline].debug("stepline not NIL 4444444444444444444444444444444444444444444444");
											ev = ev[\stepline].yield;
										} {
											ev[\stepline].debug("stepline NIL-- 4444444444444444444444444444444444444444444444");
										}
									};
									idx = 0;
									val = self.seq.val[idx];
									while( { val.notNil } , { 
										ev = val.yield;
										idx = idx + 1;
										val = self.seq.val[idx];
									});
								});
								//ev.debug("=========== in seq ev END");
							},
							\preset, {
								switch(player.get_mode,
									\sampleline, {
										[name, scalm.value(ev)].debug("############# scalm!!2");
										//ev = 1.yield;
										ev = scalm.value(ev).yield;
									},
									\noteline, {
										ev = pref.value(ev).yield;
									},
									\stepline, {
										ev = self.preset.val[self.preset.selected_cell].yield
									}
								);
							},
							\bus, {
								self.bus.get_bus.asMap.yield;
							},
							\recordbus, {
								self.bus.get_bus.asMap.yield;
							},
							// else
							{
								[param.name, self.current_kind].debug("ERROR: param kind dont match");
								0.yield;
							}
						);
					}
				}
			});
			//Prout({
		//		inf.do { self.scalar.get_val.yield };
			//});
			//Pseq([self.get_val],inf);
			//Pseq([self.scalar.get_val],inf);
		}
		//quoi: { "QUOI".debug; }.value
	);
	// init
	param.preset = param.seq.deepCopy;
		//"rah1".debug;
	param.seg = param.seq;
	param.synchrone_rate = param.synchrone;
	param.scoreseq = param.scalar;
		//"rah1".debug;

	param.midi = main.midi_center.get_midi_control_handler(param);
		//"rah1".debug;

	// \dur special case
	if([\dur,\segdur, \stretchdur].includes(name), {
		//"rah5".debug;
		param.change_kind(\preset);
		//"rah5".debug;
		//param.preset.val = List[ 4, 2, 1, 0.5, 0.25, 0.125, 0.0625 ];
		param.preset.val = List[ 4, 2, 1, 0.5, 0.25, 0.125, 0.0625 ];
		//"rah5".debug;
		if(name == \stretchdur, {
		//"rah2".debug;
			param.select_cell(2);
		}, {
		//"rah3".debug;
			param.select_cell(4);
		});
	});

	//name.debug("---- make_control_param END");
	// return object
	param;
};

~class_param_gtrig = (
	// used for using scoreline on modulation pattern 
	// 	because gate is always On in Pmono and t_trig trigger even with rest notes
	parent: ~class_param_controller,
	param_rate: \tr,
	default_value: 1,

	new: { arg self, name, scorekey=\type;
		self = self.deepCopy;
		self.name = name;
		self.scorekey = scorekey;
	
		self;
	},

	vpattern: { arg self;
		Pfunc{ arg ev;
			var score = ev[ev[\current_mode]];
			[ev[\current_mode], ev[ev[\current_mode]][\type]].debug("class_param_gtrig");
			if(score[\type] == \rest and: {score[\dur] != 0}) {
				0
			} {
				1
			}
		}
	},
);

~class_param_range = (
	parent: ~class_param_controller,
	// interface:
	// - range
	// - name (should be named slot)

	classtype: \range,
	val: 0,
	norm_val: 0,
	muted: false,
	spec: \bipolar.asSpec,
	archive_data: [\val, \target_slot, \slot_index, \muted],

	new: { arg self, main, modmixer, idx, name;
		self = self.deepCopy;
		
		self.get_main = { main };
		self.modmixer = { modmixer };
		self.target_slot = idx;
		self.install_midi;
		self.name = "% (m%)".format(name ?? modmixer.name, idx);
	
		self;
	},

	range: { arg self;
		self.get_val;
	}, 

	range_: { arg self, val;
		self.set_val(val)
	},

	refresh: { arg self;
		self.changed(\val);
		self.midi.refresh;
	},

	get_val: { arg self;
		self.val;
	},

	set_val: { arg self, val;
		self.val = val;
		self.changed(\val);
		self.modmixer.changed(\range, self.target_slot);
	},

	get_slot_index: { arg self;
		// source_slot
		self.slot_index;
	},

	set_slot_index: { arg self, val;
		self.slot_index = val;
		self.changed(\slot_index);
	},

	set_norm_val: { arg self, val;
		self.val = self.spec.map(val);
		self.changed(\val);
		self.modmixer.changed(\range, self.target_slot);
	},	

	get_norm_val: { arg self;
		self.spec.unmap(self.val);
	},
	
	vpattern: { arg self;
		Pfunc{self.val}
	},

);

////////////////////////// Buffer

~class_param_workbuf_controller = (
	parent: ~class_param_controller,
	classtype: \workbuf,
	new: { arg self, player, name, spec, channels=\stereo;
		self = self.deepCopy;
	
		self.get_player = { player };
		self.buf = BufferPool.alloc(player.uid, name, spec.numFrames, spec.numChannels);
	
		self;
	},

	destructor: { arg self;
		BufferPool.release(self.buf, self.get_player.uid);
	},

	vpattern: { arg self;
		self.buf
	},
);

~make_buf_param = { arg name, default_value, player, spec, channels=\stereo;

	var param;

	param = (
		val: nil,
		name: name,
		classtype: \buf,
		current_kind: \sample,
		channels: channels,
		has_custom_buffer: false,
		spec: nil,
		selected: 0,
		buffer: nil,
		audio_id: nil,
		pkey_mode: false,
		buffer_list_position: 0,
		old_sample_path: "~/Musique/".standardizePath, // used to find moved samples
		archive_data: [\name, \classtype, \selected, \spec, \has_custom_buffer, \buffer_list_position, \pkey_mode],

		destructor: { arg self;
			BufferPool.release_client(player.uid);
			self.buffer_list.do { arg buf; buf.free };
		},

		do_with_buffer_file_path: { arg self, idx, action;
			[idx, self.buffer_list_position].debug("make_buf_param: do_with_buffer_file_path: idx");
			idx = idx ?? self.buffer_list_position;
			self.buffer_file_path.debug("make_buf_param: do_with_buffer_file_path: buffer_file_path");
			if(self.buffer_file_path[idx].notNil) {
				[self.buffer_file_path[idx]].debug("make_buf_param: do_with_buffer_file_path: path");
				action.(self.buffer_file_path[idx]);
			} {
				if(self.has_custom_buffer) { // FIXME: clashing ID, should save when saving project, should save in project dir
					{
						debug("make_buf_param: do_with_buffer_file_path: saving bufs");
						self.save_buffers;
						s.sync;
						debug("make_buf_param: do_with_buffer_file_path: synced");
						action.(self.buffer_file_path[idx])
					}.fork(AppClock)

				} {
					debug("make_buf_param: get_buffer_file_path: not implemented");
					nil;
				};
			}
			
		},

		gen_buffer_path: { arg self, idx;
			var savepath = player.get_main.get_audio_save_path;
			savepath +/+ "audio_%_%_%.wav".format(player.uname, name, idx);
		},

		save_buffers: { arg self, data;
			if(self.has_custom_buffer) { // FIXME: clashing ID, should save when saving project, should save in project dir
				self.buffer_file_path = List.new;
				self.buffer_list.do { arg buf, idx;
					[buf, idx].debug("make_buf_param: save_buffers: buf, idx");
					self.buffer_file_path.add( self.gen_buffer_path(idx) );
					buf.write(self.buffer_file_path[idx], "WAV", "float");
				};
				self.buffer_file_path.debug("make_buf_param: save_buffers: buffer_file_path");
				if(data.notNil) {
					data[\buffer_list_size] = self.buffer_list.size;
				};
			};
		},

		load_buffers: { arg self, size=1;
			if(self.has_custom_buffer) {
				var buflist;
				self.buffer_file_path = List.new;
				size.debug("make_buf_param: load_buffers: size");
				buflist = size.collect { arg idx;
					var path = self.gen_buffer_path(idx);
					[idx, path].debug("make_buf_param: load_buffers: path generated");
					self.buffer_file_path.add( path );
					self.path_to_buffer(path);
				};
				self.set_custom_buffer_list(buflist, "AudioInput", self.buffer_list_position);
			}
		},

		save_data: { arg self;
			var data = ();
			var savepath;
			self.save_buffers(data);
			self.archive_data.do {
				arg key;
				data[key] = self[key];
			};
			data[\val] = self.get_val;
			data.debug("make_buf_param: save_data: data");
			data;
		},

		load_data: { arg self, data;
			var savepath;
			var newval;
			self.archive_data.do {
				arg key;
				self[key] = data[key];
			};
			data.debug("make_buf_param: load_data");
			if(self.has_custom_buffer) { // FIXME: clashing ID, 
				self.load_buffers(data[\buffer_list_size]);
				self.val = data[\val];
			} {
				newval = ~samplekit_manager.bogus_absolute_path_to_relative_path(data[\val]);
				self.set_val(newval);
			}
		},

		select_param: { arg self;
			self.selected = 1;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		get_val: { arg self; self.val },

		new_buffer: { arg self, path;
			[player.uid, path].debug("new_buffer: player.uname, path");
			self.buffer = self.path_to_buffer(path);
		},

		path_to_buffer: { arg self, path;
			var buffer;
			path = ~samplekit_manager.path_to_absolute_path(path);
			if(self.channels == \mono) {
				buffer = BufferPool.get_mono_sample(player.uid, path);
			} {
				buffer = BufferPool.get_forced_stereo_sample(player.uid, path);
			};
			buffer;
		},

		set_custom_buffer: { arg self, buf, name;
			// PRIVATE, should not be used outside because buffer_list should be also set
			[buf, name].debug("make_buf_param: set_custom_buffer: buf, name");
			self.val = name;
			self.has_custom_buffer = true;
			self.buffer = buf;
			self.changed(\val);
		},

		set_custom_buffer_list: { arg self, bufs, name, position=0;
			//FIXME: save it
			[bufs, name, position].debug("make_buf_param: set_custom_buffer_list: bufs, name, position");
			self.buffer_list = bufs;
			self.buffer_list_position = position;
			self.buffer_file_path = List.new;
			self.set_custom_buffer(bufs[position], name++position.asString); //FIXME: hardcoded
		},

		set_current_buffer_num: { arg self, num;
			if(self.buffer_list.notNil and: { self.buffer_list[num].notNil }) {
				self.buffer_list_position = num;
				self.set_custom_buffer(self.buffer_list[num], "AudioInput"++num); //FIXME: hardcoded
			} {
				num.debug("Buffer not found in buffer list");
			}
		},

		forward_in_record_history: { arg self;
			var buffer_list_position = (self.buffer_list_position + 1).clip(0, self.buffer_list.size-1);
			self.set_current_buffer_num(buffer_list_position);
		},

		backward_in_record_history: { arg self;
			var buffer_list_position = (self.buffer_list_position - 1).clip(0, self.buffer_list.size-1);
			self.set_current_buffer_num(buffer_list_position);
		},

		set_val: { arg self, val;
			if(val != self.val, {
				self.new_buffer(val);
				self.val = val;
				self.has_custom_buffer = false;
				self.changed(\val);
			});
			self.val.debug("set_val: val");
		},

		set_pkey_mode: { arg self, set=true;
			self.pkey_mode = set;
			self.changed(\kind);
		},

		refresh: { arg self;
			self.changed(\val);
			self.changed(\selected);
		},

		vpiano: { arg self;
			{ self.buffer.bufnum };	
		},

		vpattern: { arg self;
			
			self.buffer.bufnum.debug("BUFNUM");	
			Prout({ arg ev;
				var repeat = 1000;
				var slotnum;
				repeat.do {
					self.current_kind.debug("2222222222222222222222 entering bufnum vpattern loop");
					switch(player.get_mode,
						\sampleline, {
							if(ev[\slotnum].notNil) {
								slotnum = ev[\slotnum];
							} {
								if(ev[\sampleline].type == \rest) {
									slotnum = 0; // FIXME: should be silent sample
								} {
									slotnum = ev[\sampleline].slotnum;
								}
							};
							//~samplekit_manager.slot_to_bufnum(slotnum, ev[\samplekit], channels).debug("bufnum::::");
							ev = ~samplekit_manager.slot_to_bufnum(slotnum, ev[\samplekit], self.channels).yield;
						},
						// else
						{
							if(self.pkey_mode and: { ev.includesKey(self.name) }) {
								ev = ev[self.name].yield;
							} {
								ev = self.buffer.bufnum.yield;
							}
						}
					);
				};
			});	
			//Pfunc({ "RAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH".debug; 0});
		}

	);
	param.set_val(default_value);
	param;

};
~class_param_wavetable_controller = (

	parent: ~class_param_controller,
	classtype: \wavetable,

	model: (
		buffer_range: 0,
		val: 0,
		val_uname: "curve name",
	),

	new: { arg self, player, name, wt_pos, single=false;
		var cosc_variants;
		self = self.deepCopy;
		self.name = name;
		self.get_player = { player };
		self.single_wavetable = single;
		player.uname.debug("class_param_wavetable_controller: uname");
		//player.get_main.debug("class_param_wavetable_controller: main");
		self.wtman = player.get_main.wavetable_manager;


		self.buffer = Buffer.alloc(s, ~general_sizes.wavetable_buffer_size);

		self.wt_range_controller = ~class_param_wavetable_range_controller.new(\wt_range);
		self.wt_classic_controller = ~class_param_wavetable_classic_osc_controller.new(\wt_classic);
		cosc_variants = self.wt_classic_controller.get_variants;
		self.classic_osc_size = cosc_variants.size;
		self.menu_items = cosc_variants ++ self.wtman.get_names ++ [\custom];

		self.wt_pos_ctrl = wt_pos;

		self.set_curve(self.model.val);

		self;
	},

	destructor: { arg self;
		self.buffer.free;
		self.buffer_array.do(_.free);
	},

	save_data: { arg self;
		var data = ();
		data.model = self.model.deepCopy;
		data.model.pathlist = self.model.pathlist.collect(_.save_data);
		data;
	},

	load_data: { arg self, data;
		self.model.val = self.menu_items.detectIndex { arg item; item == data.model.val_uname };
		[data.model.val_uname, self.model.val].debug("class_param_wavetable_controller: load_data");
		self.model.val_uname = data.model.val_uname;
		self.model.pathlist = data.model.pathlist.collect{ arg dat; ~class_wavetable_file.new_from_data(dat) };
		self.set_curve(self.model.val, true)
	},

	get_menu_items_names: { arg self;
		self.menu_items
	},

	get_wt_range_controller: { arg self;
		self.wt_range_controller;
	},

	get_wt_classic_controller: { arg self;
		self.wt_classic_controller;
	},

	set_wt_pos_controller: { arg self, val;
		self.wt_pos_ctrl = val;
	},

	set_buffer_range: { arg self, range;
		self.model.buffer_range = range;
		self.wt_range_controller.set_val(range);
		//osc_pos_ctrl = self.main_controller.get_arg("osc%_wt_pos".format(self.model.indexes).asSymbol);
		////osc_pos_ctrl.debug("osc_wt_pos ctrl");
		if(self.wt_pos_ctrl.notNil) {
			self.wt_pos_ctrl.spec.maxval = self.model.buffer_range - 0.0001;
		}
	},

	set_curve: { arg self, curve_idx, load=false;
		var curve = self.menu_items[curve_idx];
		var apply_action, cancel_action;
		var was_custom = false;
		var osc_pos_ctrl;
		if(curve == \custom) {
			apply_action = { arg pathlist;
				self.model.pathlist = pathlist;
				//pathlist.debug("class_pparam_wavetable_controller: set_curve: custom: pathlist");
				self.buffer_array.do(_.free);
				self.buffer_array = Buffer.allocConsecutive(pathlist.size, s, ~general_sizes.wavetable_buffer_size);
				self.buffer_array.do { arg buf, idx;
					//self.main_controller.register_buffer(buf, self.model.uname);
					pathlist[idx].load_in_wavetable_buffer(buf);
					//~load_sample_in_wavetable_buffer.(buf, pathlist[idx].fullPath);
				};
				self.set_buffer_range(self.buffer_array.size-1);
				self.model.val_uname = curve;
				self.model.val = curve_idx;
				//self.main_controller.update_arg(self.model.uname);
				self.wt_classic_controller.set_val(\void);
				self.changed(\val);
			};
			cancel_action = {
				self.changed(\val);
			};
			if(load) {
				apply_action.(self.model.pathlist);
			} {
				~class_load_wavetable_dialog.new(apply_action, cancel_action, nil, self.single_wavetable);
			}
		} {

			//if(self.model.val_uname == \custom) {
			//	was_custom = true;
			//}; 
			if(curve_idx < self.classic_osc_size) {
				self.wt_classic_controller.set_val(curve);
			} {
				self.wt_classic_controller.set_val(\void);
				~load_curve_in_wavetable_buffer.(self.buffer, self.wtman.get_wavetable(curve));
			};
			self.model.val_uname = curve;
			self.model.val = curve_idx;
			self.set_buffer_range(1); // 1 so can be used as another argument to classic osc

			//if(was_custom) {
			//	self.main_controller.update_arg(self.model.uname);
			//}
		};
	},

	refresh: { arg self;
		//"wtREFRESH++".debug;
		//self.changed(\set_property, \label, self.model.name);
		//"wtREFRESH++ 2".debug;
		self.changed(\val);
		//"wtREFRESH++ 3".debug;
	},

	get_buffer: { arg self;
		if(self.model.val_uname == \custom) {
			self.buffer_array[0]
		} {
			self.buffer;
		}
	},

	set_val: { arg self, val;
		self.set_curve(val);
	},

	get_val: { arg self;
		self.model.val;
	},

	vpiano: { arg self;
		{
			self.get_buffer.bufnum
		}
	},

	vpattern: { arg self;
		Pfunc {
			self.get_buffer.bufnum
		}
	},

);

~class_param_wavetable_classic_osc_controller = (
	// static
	parent: ~class_param_static_controller,
	spec: \unipolar.asSpec.copy,

	get_variants: { arg self;
		[
			\SinOsc,
			\LFSaw,
			\LFPulse,
			\LFTri,
			\LFCub,
			\LFPar,
			\Blip,
			\Formant,

		]
	},

	new: { arg self, name;
		self = self.deepCopy;
		self.name = name;
	
		self;
	},

	//set_val: { arg self, val;
	//	[self.val, val].debug("class_param_wavetable_controller: set_val");
	//	if(val != self.val) {
	//		self.val = val;	
	//		self.changed(\val)
	//	}
	//},
);

~class_param_ir_kernel_controller = (

	// use normal buffer param for the moment

	//parent: ~class_param_controller,
	//classtype: \kernel,
	//new: { arg self, player, name, spec, channels=\stereo;
	//	self = self.deepCopy;
	//
	//	self.get_player = { player };
	//	//self.buf = //BufferPool.alloc(player.uid, name, spec.numFrames, spec.numChannels);
	//
	//	self;
	//},

	//load_kernel_from_file: { arg self, path;
	//	self.buf = Buffer.read(s, path);
	//	BufferPool.alloc(self.get_player.uid, name, spec.numFrames, spec.numChannels);
	//},

	//destructor: { arg self;
	//	BufferPool.release(self.buf, self.get_player.uid);
	//},

	//vpattern: { arg self;
	//	self.buf
	//},
);


////////////////////////// Arrayed

~default_adsr = (
	attackTime:0.01,
	decayTime:0.1,
	sustainLevel:0.3,
	releaseTime:0.1,
	curve:0
);

~default_genadsr = (
	attackTime:0.1,
	decayTime:0.2,
	sustainLevel:0.3,
	releaseTime:0.4,
	curve:0,
	levelScale:1,
	levelBias: 0,
	timeScale: 1
);

~adsr_point_order = [ \attackTime, \decayTime, \sustainLevel, \releaseTime, \curve ];

~env_time = ControlSpec(0.000001, 5, 'exp', 0, 0.2, "s");
~spec_adsr = (
	attackTime:~env_time,
	decayTime:~env_time,
	sustainLevel:~env_time, //FIXME: not time
	releaseTime:~env_time,
	curve:ControlSpec(-9, 9, 'linear', 1, 0, "")
);


~adsr_event_to_env = { arg adsr;
	Env.performWithEnvir(\adsr, adsr);
};

~genadsr_event_to_env_array = { arg adsr;
	Env.performWithEnvir(\adsr, adsr).asArray ++ [adsr.levelScale, adsr.levelBias, adsr.timeScale]
};

~make_adsr_param = { arg main, player, name, default_value;
	var param, subparam;

	param = (
		val: ~default_adsr.deepCopy,
		name: name,
		classtype: \adsr,
		current_kind: \scalar,
		spec: ~spec_adsr,
		sub_midi: Dictionary.new, // store midi control handler specific to each point of the adsr
		sub_param: Dictionary.new,
		selected: 0,
		pkey_mode: false,

		save_data: { arg self;
			var data = ();
			[\name, \classtype, \selected, \spec, \val].do {
				arg key;
				data[key] = self[key];
			};
			//data[\vals] = self.get_vals;
			data;
		},

		load_data: { arg self, data;
			[\name, \classtype, \selected, \spec, \val].do {
				arg key;
				self[key] = data[key];
			};
			//self.set_vals( data[\vals] );
		},

		set_pkey_mode: { arg self, set=true;
			self.pkey_mode = set;
			self.changed(\kind);
		},


		select_param: { arg self;
			self.selected = 1;
			"env model select_param".debug;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			"env model deselect_param".debug;
			self.changed(\selected);
		},

		get_val: { arg self; self.val },

		get_vals: { arg self;
			self.get_params.collect { arg param;
				self.get_param(param).get_val;
			}
		},

		set_vals: { arg self, val;
			self.get_params.do { arg param, idx;
				self.get_param(param).set_val(val[idx] ?? 0);
			}
		},

		set_all_val: { arg self, vals;
			self.val = vals;
			self.changed(\val);
		},

		get_param: { arg self, key;
			self.sub_param[key];
		},

		get_params: { arg self;
			~adsr_point_order;
		},

		set_val: { arg self, val;
			// should not be used (use get_param(key).set_val)
			["main", val].debug("make_adsr_param: set_val");
		},

		refresh: { arg self;
			self.name.debug("make_adsr_param: refresh");
			self.changed(\val);
			self.changed(\selected);
			self.sub_midi.keysValuesDo { arg key, val;
				[key, val].debug("make_adsr_param: refresh");
				val.refresh
			};
		},

		vpiano: { arg self;
			{
				~adsr_event_to_env.(self.val).asArray
			}
		},

		vpattern: { arg self;

			Pfunc({ arg ev;
				if(self.pkey_mode) {
					if( ev[self.name].notNil ) {
						ev[self.name]
					} {
						[ ~adsr_event_to_env.(self.val).asArray ]
					};
				} {
					[ ~adsr_event_to_env.(self.val).asArray ]
				}
			});
			//[ ~adsr_event_to_env.(self.val).asArray ]
		}

	);

	~adsr_point_order.do { arg key;

		subparam = (
			name: key,
			spec: param.spec[key],
			midi: { arg self; param.sub_midi[key] },
			classtype: { arg kself; param.classtype },
			selected: { arg kself;
				if(player.get_selected_param == name) { 1 } { 0 }
			},
			get_norm_val: { arg kself;
				param.spec[key].unmap( param.val[key] );
			},
			set_norm_val: { arg kself, val;
				[key, val].debug("make_adsr_param: set_val");
				param.val[key] = param.spec[key].map( val );
				kself.changed(\val);
			},
			get_val: { arg kself;
				param.val[key];
			},
			set_val: { arg kself, val;
				[key, val].debug("make_adsr_param: set_val");
				param.val[key] = val;
				kself.changed(\val);
			},
			refresh: { arg kself;
				//FIXME: what a point need to refresh ? (midi)
				key.debug("make_adsr_param: adsr point: refresh");
				kself.midi.refresh;
			}
		);
		param.sub_param[key] = subparam;
		param.sub_midi[key] = main.midi_center.get_midi_control_handler(subparam);
	};
	param;

};

~class_param_custom_env_controller = (
	parent: ~class_param_controller,

	classtype: \custom_env,

	new: { arg self, name;
		var numlevels = 16;
		self = self.deepCopy;
		self.name = name;

		self.default_value = Env([0,1,0] ++ (0!numlevels),[0.1,0.1] ++ (0!numlevels));

		self.notescore = ~make_notescore.();
		self.notescore.set_notes(~default_custom_env_notescore);

		self;
	},

	save_data: { arg self;
		var data = IdentityDictionary.new;
		self.archive_data.do { arg key;
			data[key] = self[key]
		};
		data[\notescore] = self.notescore.save_data;
		data;
	},

	load_data: { arg self, data;
		self.archive_data.do { arg key;
			self[key] = data[key];
		};
		self.notescore.load_data( data[\notescore] );
		self.update_notes;
	},


	get_notescore: { arg self;
		self.notescore
	},

	notescore_to_env: { arg self, notescore;
		var notes;
		var levels = List.new, times = List.new;
		var release_node = nil;
		var loop_node = nil;
		notes = notescore.get_rel_notes;
		notes.do { arg note, i;
			if(note.is_release_node == true) {
				release_node = i;
			};
			if(note.is_loop_node == true) {
				loop_node = i;
			};
			times.add( note.dur );
			levels.add( note.level );
		};
		Env(levels, times, 0, release_node, loop_node);
	},

	update_notes: { arg self;
		self.env_object = self.notescore_to_env(self.notescore)
	},

	vpattern: { arg self;
		Pfunc{ [ self.env_object ] }
	},
);

// ==========================================
// PATTERN CONTROL
// ==========================================

~make_literal_param = { arg name, val;
	(
		name: name,
		classtype: \literal,
		get_val: val,

		refresh: { arg self;
			self.changed(\selected);
		},
		vpiano: val,
		vpattern: val
	);
};

~make_dynamic_literal_param = { arg name, external_get;
	(
		name: name,
		classtype: \dynamic_literal,
		get_val: { arg self;
			self.val = external_get.();
			self.val
		},

		refresh: { arg self;
			self.changed(\selected);
		},
		vpiano: { arg self;
			{
				self.get_val;
			}
		},
		vpattern: { arg self;
			Pfunc { self.get_val };
		},
	);
};

~make_empty_param = { arg main;
	~make_simple_number_param.(main, "voidparam", \amp.asSpec, 0.123);
};

~make_simple_number_param = { arg main, name, spec, default_value;
	var res = (
		name: name,
		val: default_value,
		classtype: \binded_number,
		spec: spec,

		get_norm_val: { arg self;
			self.spec.unmap(self.val)
		},
		set_norm_val: { arg self, val;
			val.debug("make_tempo_param: set_norm_val");
			self.val = self.spec.map(val);
			self.changed(\val);
		},

		get_val: { arg self;
			self.val;
		},

		set_val: { arg self, val;
			self.val = val;
			self.changed(\val);
		},

		refresh: { arg self;
			self.changed(\val);
			self.changed(\selected);
		}
	);
	res.midi = main.midi_center.get_midi_control_handler(res);
	res;
};

~make_samplekit_param = { arg name, default_value=\default;

	var param;

	param = (
		val: nil,
		name: name,
		classtype: \samplekit,
		current_kind: \sample,
		spec: nil,
		selected: 0,
		buffer: nil,
		archive_data: [\name, \classtype, \selected, \spec, \val],

		save_data: { arg self;
			var data = ();
			self.archive_data.do {
				arg key;
				data[key] = self[key];
			};
			data.debug("make_samplekit_param: save_data: data");
			data;
		},

		load_data: { arg self, data;
			self.archive_data.do {
				arg key;
				self[key] = data[key];
			};
			//self.set_val(data[\val]);
		},

		select_param: { arg self;
			self.selected = 1;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		get_val: { arg self; self.val },


		set_val: { arg self, val;
			self.val = val;
			self.changed(\val);
		},

		refresh: { arg self;
			self.changed(\val);
			self.changed(\selected);
		},

		vpiano: { arg self;
			{ self.val };	
		},

		vpattern: { arg self;
			Pfunc({ self.val });	
		}

	);
	param.set_val(default_value);
	param;

};

////////////////////////// Recordline

~make_type_param = { arg name;
	var ret;
	ret = (
		name: name,
		classtype: \type,
		refresh: { arg self; },
		vpattern: { arg self; 
			Pfunc({ arg ev;
				var normal_type = ev[\type] ?? \note;
				if(ev[\muted]) {
					\rest
				} {
					if(ev[\current_mode] == \stepline) {
						if(ev[\stepline] > 0) {
							normal_type
						} {
							\rest
						}
					} {
						if(ev[ev[\current_mode]].type != \rest) {
							normal_type;
						} {
							\rest;
						}
					}
				}
			})
		}
	);
	ret;
};

~make_parent_recordline_param = { arg name, default_value=[];
	var ret;
	ret = (
		name: name,
		classtype: \recordline,
		selected_cell: 0,
		selected: 0,
		default_val: default_value.asList,
		notes: ~default_noteline3.deepCopy,
		start_offset: 0,
		end_offset: 0,
		muted: false,
		archive_data: [\name, \classtype, \selected, \selected_cell, \default_val, \notes, \start_offset, \end_offset, \notequant, \muted],
		notequant: nil,
		vnotes: [],

		save_data: { arg self;
			var data = ();
			self.archive_data.do {
				arg key;
				data[key] = self[key];
			};
			[\seq].do { arg kind;
				data[kind] = ();
				[\val].do { arg key;
					data[kind][key] = self[kind][key];
				}
			};
			data[\scoreset] = self.scoreset.save_data;
			data;
		},

		load_data: { arg self, data;
			self.archive_data.do {
				arg key;
				self[key] = data[key];
			};
			[\seq].do { arg kind;
				[\val].do { arg key;
					 self[kind][key] = data[kind][key];
				}
			};
			self.scoreset.load_data(data[\scoreset]);
			self.refresh;
		},

		seq: (
			val: default_value.asList,
			change: { arg self, fun;
				self.val = fun.(self.val);
			}
		),
		get_cells: { arg self;
			self.seq.val;
		},
		get_selected_cell: { arg self;
			self.selected_cell;
		},
		select_param: { arg self;
			self.selected = 1;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		add_cells: { arg self, cells;
			self.seq.val.addAll(cells);
			self.changed(\cells);
		},

		remove_cells: { arg self, num;
			self.seq.val = self.seq.val[.. (self.seq.val.size - num - 1) ];
			self.changed(\cells);
		},

		set_val: { arg self, val;
			self.seq.val[ self.get_selected_cell.() ] = if(val > 1, { 1 },{ 0 });
		},

		quantify_note2: { arg self, note;
			var res;
			//note.debug("quantify_note input");
			res = note.deepCopy;
			if(self.notequant.notNil) {
				res.dur = res.dur.round(self.notequant);
				res;
			} {
				res;
			};
			//res.debug("quantify_note output");
		},

		get_notes: { arg self;
			self.scoreset.get_notes;
		},

		get_scoreset: { arg self;
			self.scoreset;
		},

		get_notes2: { arg self;
			var no;
			no = self.vnotes.deepCopy;
			//no[0].dur+self.start_offset;
			//no.last.dur+self.end_offset;
			no;
		},

		set_notes: { arg self, val;
			self.scoreset.set_notes(val);
			//self.changed(\notes);
		},
		set_notes2: { arg self, val;
			self.notes = val;
			self.update_note_dur;
		},

		set_next_notes: { arg self, val, dur=nil;
			self.scoreset.set_next_notes(val, dur);
		},

		set_next_notescore: { arg self, val, dur=nil;
			self.scoreset.set_next_notescore_history(val, dur);
		},

		set_next_notes2: { arg self, val, dur=nil;
			if(self.pat_finish_first.notNil) {
				// le pattern a deja commencé, assigner les notes tout de suite
				"**** set_next_notes: assigning next_notes as current notes immediately".debug;
				self.set_notes(val);
				self.pat_finish_first = nil;
			} {
				"**** set_next_notes: preparing next_notes".debug;
				self.next_notes = val;
			};
			self.notes_dur = dur; // used to check is dur is correct after quantization
		},

		set_next_notes_as_current_notes: { arg self;
			self.scoreset.set_next_notes_as_current_notes;
		},

		forward_to_next_notescore: { arg self;
			// called in get_note and midi:player_start_tempo_recording
			self.scoreset.forward_to_next_notescore;
		},

		set_next_notes_as_current_notes2: { arg self;
			self.set_notes(self.next_notes);
			self.next_notes = nil;
			self.wait_note = nil;
		},

		set_wait_note: { arg self, note;
			self.scoreset.set_wait_note(note);	
		},
		set_wait_note2: { arg self, note;
			"**** nline: set_wait_note".debug;
			self.wait_note = self.quantify_note(note);
			self.wait_note[\first_note] = true;
			self.wait_note.debug("wait_note");
			note.debug("originial note");
		},

		get_note: { arg self, idx;
			// FIXME: not used anymore, use whole notescore now
			self.scoreset.get_note(self, idx);
		},

		get_note2: { arg self, idx;
			var no;
			if( self.vnotes.size > 0 && {self.muted.not}) {
				if(idx == 0) {
					// s'il y a deja des next_notes lorsque la note 0 arrive (debut du pattern), c'est que le record a fini _avant_ le pattern
					// s'il n'y en a pas mais qu'il y a quand meme une wait_note c'est que le record va finir _apres_ le pattern
					// s'il n'y a ni l'un ni l'autre, c'est que c'est un banale debut de pattern, et cela doit continuer normalement
					if(self.next_notes.notNil) {
						"******** recording finish first, using next_notes as current notes".debug;
						self.set_next_notes_as_current_notes;
					} {
						if(self.wait_note.notNil) {
							"********* recording not yet finished".debug;
							"***** there is a wait note, using it as first note".debug;
							no = self.wait_note;
							self.wait_note = nil;
							self.pat_finish_first = true;
						} {
							no = self.vnotes[idx].deepCopy;
						};
					}
				} {
					no = self.vnotes[idx].deepCopy;
				};
				no;
			} {
				if(self.muted) {
					"noteline_param: get_note: muted!".inform;
				} {
					if(self.next_notes.notNil) {
							"setting next_notes when no notes found".debug;
							self.set_notes(self.next_notes);
							self.next_notes = nil;
							self.wait_note = nil;
					};
					"noteline_param: get_note: No notes".inform;
				};
				if(idx == 0) {
					~empty_note;
				} {
					nil;
				};
			}
		},

		get_duration: { arg self;
			self.scoreset.get_duration;
		},

		total_dur: { arg self, notes;
			self.scoreset.total_dur(notes);
		},

		total_dur2: { arg self, notes;
			var res=0;
			notes.do { arg x; res = x.dur + res; };
			res;
		},

		update_note_dur: { arg self;
			self.scoreset.update_notes;
		},

		update_note_dur2: { arg self;
			// manage start and end offset and silence and put result notes in self.vnotes
			var find_next, find_prev, delta, prevdelta, idx, previdx;
			var qnotes, normdur, realdur, size;
			"update_note_dur".debug("start");
			if( self.notes.size > 2) {
				find_next = { arg dur, notes;
					var res=0, vidx=0, last=nil, delta=0, prevdelta=0;
					dur.debug("find_next: dur");
					if(dur == 0) {
						delta = 0;
						vidx = 0
					} {
						notes[1..].do { arg x, n;
							[n, res, vidx, last, dur].debug("begin n res vidx last");
							if( res < dur ) {
								res = x.dur + res;
								vidx = vidx + 1;
								last = x.dur;
							};
							[n, res, vidx, last].debug("end");
						};
						delta = res - dur;
					};
					[ delta, vidx+1 ];
				};

				find_prev = { arg dur, notes;
					var res=0, vidx=0, last=nil, delta=0, prevdelta=0;
					dur = self.total_dur( notes[1..(notes.lastIndex-1)] ).debug("total dur") - dur;
					dur.debug("find_prev: dur");

					notes[1..].do { arg x, n;
						[n, res, vidx, last].debug("begin n res vidx last");
						if( res <= dur ) {
							res = x.dur + res;
							vidx = vidx + 1;
							last = x.dur;
						};
						[n, res, vidx, last].debug("end");
					};
					delta = res - dur;
					if(last.isNil) {
						prevdelta = nil
					} {
						last.debug("last");
						prevdelta = last - delta;
					};
					[ prevdelta, vidx ];
				};

				// quantify notes
				self.notes.collect({arg no, x; no.numero = x }); // debug purpose
				self.notes.debug("update_note_dur: original notes");
				qnotes = self.notes.collect{ arg x; self.quantify_note(x) };
				qnotes.debug("update_note_dur: qnotes");
				
				// troncate too long end silence
				normdur = self.total_dur(self.notes);
				if(self.notes_dur.notNil && (normdur != self.notes_dur)) {
					"fucking bug in recording length!!!!!!!!".debug;
				};
				realdur = self.total_dur(qnotes);
				qnotes.last.dur = qnotes.last.dur - (realdur - (self.notes_dur ?? normdur));
				~mydur = self.total_dur(qnotes);
				[self.notes_dur, realdur, normdur, ~mydur].debug("duration::::: notes_dur, real, norm, end");

				if(qnotes.last.dur < 0) {
					"ERROR: mon hack degueux fonctionne pas vraiment et c'est la grosse merde".error;
				};

				// remove duplicates

				size = qnotes.size-1;
				self.vnotes = List.new;
				qnotes.do { arg no, x;
					if(x < size) {
						if((no.dur == 0) && (no.midinote == qnotes[x+1].midinote)) {
							// duplicate: don't add
							no.debug("found duplicate: don't add");
						} {
							self.vnotes.add(no);
						};
					} {
						self.vnotes.add(no);
					}
				};

				self.vnotes.debug("update_note_dur: vnotes");


				// old code to calculate offsets when recording non standard durations

				//#delta, idx = find_next.(self.notes[0].start_offset, self.notes);
				//#prevdelta, previdx = find_prev.(self.notes[0].end_offset, self.notes);
			//	#delta, idx = find_next.(qnotes[0].start_offset, qnotes);
			//	#prevdelta, previdx = find_prev.(qnotes[0].end_offset, qnotes);
			//	[delta, idx, prevdelta, previdx].debug("delta, idx, prevdelta, previdx");
			//	qnotes[0].dur = qnotes[0].start_silence + delta;
			//	self.vnotes = [qnotes[0]] ++ qnotes[idx..previdx].deepCopy;
			//	self.vnotes[self.vnotes.lastIndex].dur = qnotes[0].end_silence + prevdelta;
			//	self.vnotes.debug("update_note_dur: vnotes");
				self.changed(\notes);
			} {
				if(self.notes.size == 2) {
					self.vnotes = self.notes.deepCopy;
					if( (self.notes[0].start_silence + self.notes[0].end_silence) < 0.01 ) {
						"Protection anti infinite loop: setting end_silence to 0.5".error;
						self.notes[0].end_silence = 0.5
					};
					self.vnotes[0].dur = self.notes[0].start_silence;
					self.vnotes[self.vnotes.lastIndex].dur = self.notes[0].end_silence;
					self.vnotes.debug("update_note_dur: vnotes");
					// quantify notes
					self.vnotes = self.vnotes.collect{ arg x; self.quantify_note(x) };
					self.vnotes.debug("update_note_dur: vnotes quant");
					self.changed(\notes);
				}
			};
		},

		set_start_offset: { arg self, val;
			var dur;
			if(self.notes.size > 2 ) {
				dur = self.total_dur( self.notes[1..(self.notes.lastIndex-1)] );
				if( val >= 0 && (val < (dur - self.notes[0].end_offset)) ) {
					[val, dur, self.notes[0].end_offset, dur - self.notes[0].end_offset].debug("set start_offset: val, dur, eo, dur-eo");
					self.notes[0].start_offset = val;
					self.update_note_dur;
				} {
					[val, dur, self.notes[0].end_offset, dur - self.notes[0].end_offset].debug("can't set start_offset: val, dur, eo, dur-eo");
				}
			} {
				"You are stupid!".debug;
			}
		},

		set_end_offset: { arg self, val;
			var dur;
			if(self.notes.size > 0) {
				dur = self.total_dur( self.notes[1..(self.notes.lastIndex-1)] );
				if( val >= 0 && (val < (dur - self.notes[0].start_offset)) ) {
					[val, dur, self.notes[0].end_offset, dur - self.notes[0].end_offset].debug("can't set end_offset: val, dur, so, dur-so");
					self.notes[0].end_offset = val;
					self.update_note_dur;
				} {
					[val, dur, self.notes[0].start_offset, dur - self.notes[0].start_offset].debug("can't set end_offset: val, dur, so, dur-so");
				}
			}
		},

		get_start_offset: { arg self;
			if(self.notes.size > 0) {
				self.notes[0].start_offset;
			}
		},

		get_end_offset: { arg self;
			if(self.notes.size > 0) {
				self.notes[0].end_offset;
			}
		},

		set_start_silence: { arg self, val;

			if(self.notes.size > 0 && (val >= 0)) {
				self.notes[0].start_silence = val;
				self.update_note_dur;
			} {
				[val, self.notes.size].debug("can't set start_silence: val, notessize");
			}
		},

		set_end_silence: { arg self, val;

			if(self.notes.size > 0 && (val >= 0)) {
				self.notes[0].end_silence = val;
				self.update_note_dur;
			} {
				[val, self.notes.size].debug("can't set end_silence: val, notessize");

			};
		},

		get_start_silence: { arg self;
			self.scoreset.get_start_silence;
		},

		get_start_silence2: { arg self;
			if(self.notes.size > 0) {
				self.notes[0].start_silence;
			}

		},

		get_end_silence: { arg self;
			self.scoreset.get_end_silence;
		},

		get_end_silence2: { arg self;
			if(self.notes.size > 0) {
				self.notes[0].end_silence;
			}

		},

		set_first_note_dur: { arg self, val;
			var res = 0, lastdur = 0, vidx = 0;
			if( self.notes.size > 0) {
				self.notes[0].dur = val;
				if( val < 0 ) {
					self.notes.do { arg x;
						if( res < val.neg ) {
							res = x.dur + res;
							vidx = vidx + 1;
							lastdur = x.dur;
						}
					};
					self.notes[0].virtual_start_idx = vidx -1;
					self.notes[0].virtual_start_dur = res - val.neg;
				}
			}
		},

		get_first_note_dur: { arg self;
			self.notes.size.debug("get_first_note_dur self.notes.size");
			if( self.notes.size > 0) {
				self.notes[0].dur;
			} {
				nil
			}
		},

		set_notequant: { arg self, val;
			self.scoreset.set_notequant(val);
		},

		get_notequant: { arg self;
			self.scoreset.get_notequant;
		},
		set_notequant2: { arg self, val;
			self.notequant = val;
			self.update_note_dur;
			self.changed(\notes);
		},

		tick: { arg idx; 
			//"TICK!".postln;
		},

		mute: { arg self, val;
			self.muted = val;
		},

		toggle_cell: { arg self, idx;
			var oldsel;
			[idx, self.get_cells].debug("make_control_param.select_cell idx, selg.get_cells");
			if( idx < self.get_cells.size, {
				//oldsel = self.selected_cell;
				self.selected_cell = idx;
				//self.changed(\selected_cell, oldsel);
				self.seq.val[ idx ] = ~toggle_value.(self.seq.val[ idx ]);
				self.changed(\val, self.selected_cell);
			})
		},
		refresh: { arg self;
			self.update_note_dur;
			self.get_notes.debug("noteline param refresh: get_notes");
			self.changed(\notes);
			self.changed(\selected);
		},

		//vpattern: { arg self; 
			// deprecated: cause time mismatch when adding notes
		//	~pdynarray.( { arg idx, no;
		//		self.tick;
		//		//self.classtype.debug("classtype!!!!!!!!");
		//		no = self.get_note(idx);
		//		//"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX".debug;
		//		//self.classtype.debug("classtype");
		//		//idx.debug("note number");
		//		//no.debug("note");
		//		no;
		//	}, self.classtype );
		//},
		vpattern: { arg self; 
			Prout{ arg ev;
				var repeat = ~compute_repeat.(ev, self.classtype);
				repeat.do {
					var notes = self.scoreset.get_notes;
					notes.do { arg no;
						ev = no.yield;
					}
				}
			};
		},

		init: { arg self;
			self.scoreset = ~make_scoreset_hack.(self);
			self.scoreset.notescore.set_notes(self.notes);
			self.scoreset.notescore.compute_end(true);
			self.scoreset.update_notes; // to take in account default_noteline
		}

	);
	//ret.init; //should init in subclasses
	ret;
};

~make_line_param = { arg name, default_value=[];
	var fun;
	// FIXME: modes
	fun = switch(name,
		\noteline, { ~make_noteline_param },
		\scoreline, { ~make_scoreline_param },
		\stepline, { ~make_stepline_param },
		\nodeline, { ~make_nodeline_param },
		\sampleline, { ~make_sampleline_param },
		{ name.debug("make_line_param: line not understood"); }
	);
	fun.(name, default_value);
};

~make_nodeline_param = { arg name, default_value=[];
	var ret;
	ret = (
		parent: ~make_parent_recordline_param.(name, default_value),
		classtype: \nodeline,
		notes: ~default_sampleline.deepCopy,

		trans_notes: { arg self;
			// make sample slots compatibles with other code using midinotes
			self.notes = self.notes.collect { arg x;
				x.midinote = x.slotnum;
			};
		},

		update_note_dur: { arg self;
			self.trans_notes;
			self.parent[\update_note_dur].(self);
		}
	);
	ret.init;
	ret;
};

~make_sampleline_param = { arg name, default_value=[];
	var ret;
	ret = (
		parent: ~make_parent_recordline_param.(name, default_value),
		classtype: \sampleline,
		notes: ~default_sampleline.deepCopy,

		trans_notes: { arg self;
			// make sample slots compatibles with other code using midinotes
			self.notes = self.notes.collect { arg x;
				x.midinote = x.slotnum;
			};
		},

		update_note_dur: { arg self;
			self.trans_notes;
			self.parent[\update_note_dur].(self);
		}
	);
	ret.init;
	ret;
};

~make_noteline_param = { arg name, default_value=[];
	var ret;
	ret = (
		parent: ~make_parent_recordline_param.(name, default_value),
		classtype: \noteline,
		notes: ~default_noteline3.deepCopy
	);
	ret.init;
	ret;
};

~make_scoreline_param = { arg name, default_value=[];
	var ret;
	ret = (
		parent: ~make_parent_recordline_param.(name, default_value),
		classtype: \scoreline,
		notes: ~default_scoreline.deepCopy
	);
	ret.init;
	ret;
};

~make_stepline_param = { arg name, default_value;
	var ret;
	ret = (
		name: name,
		classtype: \stepline,
		selected_cell: 0,
		selected: 0,
		current_kind: \stepline, // hack to harmonize with control_param
		default_val: default_value.asList,

		save_data: { arg self;
			var data = ();
			[\name, \classtype, \selected, \selected_cell, \default_val].do {
				arg key;
				data[key] = self[key];
			};
			[\seq].do { arg kind;
				data[kind] = ();
				[\val].do { arg key;
					data[kind][key] = self[kind][key];
				}
			};
			data;
		},

		load_data: { arg self, data;
			[\name, \classtype, \selected, \selected_cell, \default_val].do {
				arg key;
				self[key] = data[key];
			};
			[\seq].do { arg kind;
				[\val].do { arg key;
					 self[kind][key] = data[kind][key];
				}
			};
		},

		seq: (
			val: default_value.asList,
			change: { arg self, fun;
				self.val = fun.(self.val);
			}
		),
		get_cells: { arg self;
			self.seq.val;
		},
		get_selected_cell: { arg self;
			self.selected_cell;
		},
		select_param: { arg self;
			self.selected = 1;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		add_cells: { arg self, cells;
			self.seq.val.addAll(cells);
			self.changed(\cells);
		},

		remove_cells: { arg self, num;
			self.seq.val = self.seq.val[.. (self.seq.val.size - num - 1) ];
			self.changed(\cells);
		},

		set_val: { arg self, val, idx;
			idx = idx ?? self.get_selected_cell.();
			self.seq.val[ idx  ] = if(val >= 1, { 1 },{ 0 });
			self.changed(\val, idx);
		},

		get_val: { arg self, idx;
			idx = idx ?? self.get_selected_cell.();
			self.seq.val[ idx  ];
		},


		tick: { arg idx;
		//"TICK!".postln;
		},

		toggle_cell: { arg self, idx;
			var oldsel;
			[idx, self.get_cells].debug("make_control_param.select_cell idx, selg.get_cells");
			if( idx < self.get_cells.size, {
				//oldsel = self.selected_cell;
				self.selected_cell = idx;
				//self.changed(\selected_cell, oldsel);
				self.seq.val[ idx ] = ~toggle_value.(self.seq.val[ idx ]);
				self.changed(\val, self.selected_cell);
			})
		},
		refresh: { arg self;
			"make_stepline_param:refresh".debug;
			self.changed(\kind);
			//self.changed(\cells);
			self.changed(\selected);
			"END make_stepline_param:refresh".debug;
		},
		vpattern: { arg self; 
			"--------making vpattern of stepline".debug;
			~pdynarray.( { arg idx, ev; 
				if(ev[\stepline].notNil) {
					ev[\stepline];
				} {
					self.tick(idx); 
					if(self.seq.val[idx] == 1, { [idx, TempoClock.beats, TempoClock.beatInBar].debug("----------TICK step"); });
					self.seq.val[idx];
				}
			}, \stepline );
			//Pseq([1,1,1,1],inf);
		};
	);
	ret;
};


// ==========================================
// VARIABLE CONTROL
// ==========================================

~class_param_binded_number = (
	name: "noname_param",
	classtype: \binded_number,
	spec: \freq.asSpec,

	new: { arg self, main, name, spec, get_fun, set_fun;
		self = self.deepCopy;

		self.name = name;
		self.spec = spec;
		self.get_main = { main };
		if(get_fun.notNil) {
			self.get_fun = get_fun;
		};
		if(set_fun.notNil) {
			self.set_fun = { arg self, val; set_fun.(val) };
		};
		self.init_midi;

		self;
	},

	init_midi: { arg self;
		self.midi = self.get_main.midi_center.get_midi_control_handler(self);
	},

	get_norm_val: { arg self;
		self.spec.unmap(self.get_fun)
	},
	set_norm_val: { arg self, val;
		val.debug("make_tempo_param: set_norm_val");
		self.set_fun(self.spec.map(val));
		self.changed(\val);
	},

	get_val: { arg self;
		self.get_fun;
	},

	set_val: { arg self, val;
		self.set_fun(val);
		self.changed(\val);
	},

	refresh: { arg self;
		self.changed(\kind);
		self.changed(\val);
		//self.changed(\cells);
		self.changed(\selected);
		self.midi.refresh;
	},
);

~make_binded_number_param = { arg main, name, spec, get_fun, set_fun;
	~class_param_binded_number.new(main, name, spec, get_fun, set_fun)
};

~make_quant_param = { arg main, name, quantobject;
	var set_fun = { arg val; main.play_manager.set_quant(val) };
	var get_fun = { main.play_manager.get_quant };
	var res = (
		parent: ~make_binded_number_param.(main, name, \quant.asSpec, get_fun, set_fun)
	);
	res.midi = main.midi_center.get_midi_control_handler(res);
	res;
};

~make_barrecord_param = { arg main, name, quantobject;
	var set_fun = { arg val; main.play_manager.set_record_length(val); val.debug("make_barrecord_param:set_fun"); };
	var get_fun = { main.play_manager.get_record_length.debug("make_barrecord_param:get_record_length"); };
	var res = (
		parent: ~make_binded_number_param.(main, name, \seq_dur.asSpec, get_fun, set_fun)
	);
	res.midi = main.midi_center.get_midi_control_handler(res);
	res;
};

~make_tempo_param = { arg main, name;
	var set_fun = { arg val; 
		main.play_manager.set_bpm_tempo(val);
	};
	var get_fun = { main.play_manager.get_bpm_tempo };
	var res = (
		parent: ~make_binded_number_param.(main, name, \tempobpm.asSpec, get_fun, set_fun)

	);
	res.midi = main.midi_center.get_midi_control_handler(res);
	res;
};

~make_master_volume_param = { arg main, name;
	var set_fun = { arg val; 
		s.volume.volume = val;
	};
	var get_fun = { s.volume.volume };
	var res = (
		parent: ~make_binded_number_param.(main, name, \db.asSpec, get_fun, set_fun)

	);
	res.midi = main.midi_center.get_midi_control_handler(res);
	res;
};

~class_param_free_defer_time = (
	parent: ~class_param_binded_number,

	new: { arg self, main, modulation;
		self = self.deepCopy;

		self.name = "free_defer_time";
		self.spec = ControlSpec(0,10,\lin,0,1);
		self.get_main = { main };
		self.get_fun = { modulation.get_free_defer_time };
		self.set_fun = { arg self, val; modulation.set_free_defer_time(val) };
		self.init_midi;

		self;
	},

);

~make_volume_param = { arg name, main;
	// deprecated: used in mixer.sc
	var param = (
		classtype: \volume,

		save_data: { arg self;
			var data = Dictionary.new;
			data[\volume] = s.volume.volume;
		},

		load_data: { arg self, data;
			s.volume.volume = data[\volume];
		},

		get_norm_val: { arg self;
			self.spec.unmap(s.volume.volume)
		},
		set_norm_val: { arg self, val;
			s.volume.volume = self.spec.map(val);
			self.changed(\val);
		},
		get_cells: { arg self;
			[s.volume.volume]
		},
		refresh: { arg self;
			self.changed(\cells);
		},
		spec: \db.asSpec,
		selected: 1

	);
	param.midi = main.midi_center.get_midi_control_handler(param);
	param;

};


// ==========================================
// 
// ==========================================



~make_macro_control_param = { arg main, macro_ctrl, player, name, kind, default_value, spec;
	var param = ~make_control_param.(main, player, name, kind, default_value, spec);
	param.external_control = macro_ctrl;
	param.make_responder_translator = { arg self, parent;
		debug("make_responder_translator: init");
		~make_view_responder.(parent, self.external_control, (
			set_property: { arg obj, msg, key, val;
				[msg, key, val].debug("make_responder_translator");
				switch(key,
					\value, {
						self.changed(\val, 0);
					}
				)
			}

		), false)
	};
	param.scalar = (
			//quoi: { "QUOI".debug; }.value,
			selected_cell: 0, // always 0
			val: macro_ctrl.get_val,

			set_val: { arg self, val, idx=nil;
				macro_ctrl.set_val(val);
				param.changed(\val, 0);
			},
			get_val: { arg self;
				macro_ctrl.get_val 
			},

			set_norm_val: { arg self, norm_val;
				macro_ctrl.set_property(\value, norm_val);
				param.changed(\val, 0);
			},
			get_norm_val: { arg self;
				macro_ctrl.get_norm_val 
			},

			get_cells: { arg self; [self.get_val] },

			select_cell: { arg self, idx; param.seq.selected_cell = idx }, // when changing kind, correct cell is selected in colselect mode
			get_selected_cell: { arg self; 0 },
			add_cells: {},
			remove_cells: {}
	);
	param;
};


///////////////////////////////////////////////////////////

~class_param_opmatrix_controller = (
	parent: ~class_param_controller,
	archive_data: [\opmatrix],

	new: { arg self, name, spec, size;
		self = self.deepCopy;
		
		self.opmatrix = Array.fill(size.y, {
			Array.fill(size.x, { 0 })
		});
	
		self;
	},

	save_data: { arg self;
		var data = IdentityDictionary.new;
		self.archive_data.do { arg key;
			data[key] = self[key]
		};
		data;
	},

	load_data: { arg self, data;
		data.keysValuesDo { arg key, val;
			self[key] = val;
		}
	},

	get_cell_val: { arg self, x, y;
		self.opmatrix[y][x];
	},

	set_cell_val: { arg self, x, y, val;
		self.opmatrix[y][x] = val;
	},

	vpiano: { arg self;
		{
			self.get_val;
		}
	},

	vpattern: { arg self;
		Pfunc{
			[
				self.opmatrix.flat
			]
		}
	},
);

~class_param_tsustain_controller = (
	parent: ~class_param_controller,
	archive_data: [\name],
	kind: \tsustain,
	new: { arg self, name;
		self = self.deepCopy;

		self.name = name;
	
		self;
	},

	vpattern: { arg self;
		// TODO: implement legato
		Plazy({ 
			var val;
			val = Pn(Pfunc({ arg ev; ev[\dur]}) / Ptempo());
			val.debug("class_param_tsustain_controller.vpattern: val");
			//ev = val.yield;
			val;
		});
	}
);

~class_param_scorekey_controller = (
	parent: ~class_param_controller,
	archive_data: [\name, \key_name, \val],
	kind: \scorekey,
	new: { arg self, player, name, key;
		self = self.deepCopy;
	
		self.name = name;
		self.player_node = { player };
		self.key_name = key ?? name;

		self;
	},

	vpattern: { arg self;
		Prout({ arg ev;
			var val;
			~general_sizes.safe_inf.do {
				val = ev[self.player_node.get_mode][self.key_name];
				//val.debug("class_param_scorekey_controller.vpattern: val");
				if(val.isNil) {
					//ev.debug("class_param_scorekey_controller: ev");
					val = 0;
				};
				ev = val.yield;
			
			};
		});
	}
);

~class_param_modenv_val_controller = (
	parent: ~class_param_controller,
	archive_data: [\name, \key_name, \val],
	kind: \scorekey,
	new: { arg self, player, name, key;
		self = self.deepCopy;
	
		self.name = name;
		self.player_node = { player };
		self.key_name = key ?? name;
		self.val = [0,1]; //FIXME: not displayed

		self;
	},

	set_notes: { arg self, notes;
		var res = List.new;
		var tmp;
		notes.do { arg note;
			if(note.type != \rest) {
				res.add(note[self.key_name]);
			}
		};
		tmp = res.removeAt(0);
		res.add(tmp);
		self.val = res;
	},

	vpattern: { arg self;
		Prout({ arg ev;
			var idx, val, fun;
			var repeat;
			fun = { arg idx; self.val[idx] };
			ev[\firstsynth] = 1;
			ev[\firstval] = self.val.last;
			repeat = ~general_sizes.safe_inf;
			repeat.do {
				idx = 0;
				val = fun.(idx, ev);
				//[val, idx].debug("pdynarray val idx");
				while( { val.notNil } , { 
					ev = val.yield;
					idx = idx + 1;
					val = fun.(idx, ev);
				});
			};
		});
	}
);


~class_param_ienv_controller = (
	parent: ~class_param_controller,
	curve: [[64,64,64,64], [1,2,3]/3*128],
	new: { arg self, uname;
		self = self.deepCopy;
		self.uname = uname;
	
		self;
	},

	get_env: { arg self;
		Env.new(self.curve[0], self.curve[1][1..]);
	},

	set_curve: { arg self, curve;
		self.curve = curve;
		self.changed(\val)
	},

	get_curve: { arg self;
		self.curve;
	},
);

~class_param_ienv_proxy_controller = (
	parent: ~class_param_controller,
	new: { arg self, label, preset;
		self = self.deepCopy;
		self.set_preset(preset);
		self.label = label;
	
		self;
	},

	get_preset_uname: { arg self;
		self.preset.uname;
	},

	get_label: { arg self;
		self.label;
	},

	set_preset: { arg self, preset;
		self.preset = preset;
	},

	get_env: { arg self;
		self.preset.get_env;
	},

	set_curve: { arg self, curve;
		self.preset.set_curve(curve);
		self.changed(\val)
	},

	get_curve: { arg self;
		self.preset.get_curve;
	},
);

~class_param_ienv_presets_controller = (
	presets: IdentityDictionary.new,
	ienv_controllers: IdentityDictionary.new,
	new: { arg self;
		self = self.deepCopy;

		self;
	},

	add_preset: { arg self, name, val;
		self.presets[name] = ~class_param_ienv_controller.new(name);
		self.presets[name].set_curve(val);
	},

	get_presets_names: { arg self;
		self.presets.keys;
	},

	get_ienv_controllers_list: { arg self;
		self.ienv_controllers.values;
	},

	get_preset: { arg self, name;
		self.presets[name]
	},

	add_ienv_controller: { arg self, name, preset=\off;
		self.ienv_controllers[name] = ~class_param_ienv_proxy_controller.new(name, self.presets[preset]);
	},
);


