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

~pdynarray = { arg fun, userepeat=false;
	Prout({ arg ev;
		var idx;
		var val = 0;
		var repeat;
		userepeat.debug("=============================pdynarray:userepeat");
		ev[\current_mode].debug("pdynarray:current_mode");
		switch(userepeat,
			false, {
				repeat = inf;	
			},
			\noteline, {
				if( ev[\current_mode] == \noteline ) {
					repeat = ev[\repeat];
					if(repeat == 0) { repeat = inf };
				} {
					repeat = inf;
				};
			},
			\stepline, {
				if( ev[\current_mode] == \stepline ) {
					repeat = ev[\repeat];
					if(repeat == 0) { repeat = inf };
				} {
					repeat = inf;
				};
			},
			\sampleline, {
				if( ev[\current_mode] == \sampleline ) {
					repeat = ev[\repeat];
					if(repeat == 0) { repeat = inf };
				} {
					repeat = inf;
				};
			}
		);
		repeat.debug("pdynarray:repeat");
		repeat.do {
			idx = 0;
			val = fun.(idx);
			//[val, idx].debug("pdynarray val idx");
			while( { val.notNil } , { 
				ev = val.yield;
				idx = idx + 1;
				val = fun.(idx);
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
			Pif( Pkey(\stepline) > 0 , \note, \rest) // WTF with == ?????
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
	var ret;
	argName.dump;
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
// PARAM VIEW
// ==========================================

~make_status_view = { arg parent, play_manager;
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

	refresh_pos = { arg clock;
		if(play_manager.is_playing || play_manager.is_recording) {
			clock.beatInBar.debug("beatInBar");
			bt[clock.beatInBar].background = if(play_manager.is_near_end) { Color.red } { Color.green };
			bt.wrapAt(clock.beatInBar -1).background = Color.black;
			pos.string = play_manager.get_rel_beat.round(0.01).asString 
						+ "/" + play_manager.get_record_length.asString
						+ "|" + clock.beats.asString; // FIXME: find a way to zero pad
			bar.range = play_manager.get_rel_beat / play_manager.get_record_length;
		} {
			pos.string = "0 /" + play_manager.get_record_length.asString + "| 0";
			bar.range = 0;
		}
	};



	clock.postln;
	task = TaskProxy.new;
	task.source = { 
		//play_manager.start_pos = clock.beats; // debug
		var clock = play_manager.get_clock;
		bt.do { arg x; x.background = Color.black };
		10000.do { //FIXME: fake loop
			refresh_pos.(clock);
			1.wait;
		}
	};

	vlayout.onClose = vlayout.onClose.addFunc { "close".postln; task.stop };

	~make_view_responder.(vlayout, play_manager, (
		tempo: { arg obj, msg, tempo;
			temp.string = "Tempo: " ++ play_manager.get_bpm_tempo.asString;
		},
		pos: { arg obj, msg, position;
			refresh_pos.(play_manager.get_clock);
		},
		visual_metronome: { arg self;
			"start_counter!!!".debug;
			if(self.visual_metronome_enabled) {
				task.play(play_manager.get_clock,quant:1);
			} {
				task.stop;
			}
		},
		head_state: { arg obj, msg, state;
			switch(state,
				\prepare, { hstatetxt.string = "Prepare"; hstatetxt.background = Color.new255(255, 165, 0) },
				\record, { hstatetxt.string = "Rec"; hstatetxt.background = Color.red },
				\overdub, { hstatetxt.string = "Dub"; hstatetxt.background = Color.new255(205, 92, 92) },
				\play, { hstatetxt.string = "Play"; hstatetxt.background = Color.green },
				\stop, { hstatetxt.string = "Stop"; hstatetxt.background = Color.clear }
			);
		}

	));

};

~make_edit_number_view = { arg main, name, param, midi_cc;
	var win = Window.new(name,Rect(500,500,200,100));
	var layout = GUI.hLayoutView.new(win, Rect(0,0,200,100));
	var ez, midi_val;

	ez = EZSlider(layout, 150@42, name, param.spec ,unitWidth:0, numberWidth:60,layout:\line2, margin: 1@1);
	ez.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.black, Color.yellow,nil,nil, Color.grey(0.7));
	midi_val = StaticText.new(layout,Rect(0,0,200,100));
	midi_val.string = param.midi.get_midi_val.asString;
	ez.value = param.get_val;

	ez.action = { arg ezs; param.set_val(ezs.value) };

	~make_view_responder.(win, param, (
		val: { arg obj, msg;
			"make_edit_number_view: val responder".debug;
			param.get_val.debug("val:");
			ez.value = param.get_val;
		},
		midi_val: { arg obj, msg, val;
			midi_val.string = val.asString;
		},
		blocked: { arg obj, msg, blocked;
			midi_val.background = if(blocked.not, { Color.green }, { ~editplayer_color_scheme.led });
		}
	));

	main.commands.bind_param(midi_cc, param);

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

~make_tempo_edit_view = { arg main, midi_cc;
	var param = ~make_tempo_param.(main, \tempo, main.play_manager.get_clock);
	~make_edit_number_view.(main, "tempo", param, midi_cc);
};

~make_quant_edit_view = { arg main, midi_cc;
	var param = ~make_quant_param.(main, \quant, EventPatternProxy);
	~make_edit_number_view.(main, "quant", param, midi_cc);
};

~make_barrecord_edit_view = { arg main, midi_cc;
	var param = ~make_barrecord_param.(main, \barrecord, EventPatternProxy);
	~make_edit_number_view.(main, "barrecord", param, midi_cc);
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
			//newval.debug("newval==============");
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
		width = 900,
		height = 60,
		beats = 8,
		paraspace;
	var param_messages, midi_messages, sc_midi, sc_param;
	var midi = param.midi;
	var ps_bg_drawf;



	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+30),height));
	row_layout.background = ~editplayer_color_scheme.background;


	bt_name = ~make_name_button.(row_layout, display.name, xsize:display.name_width, ysize:height);
	txt_rec = GUI.staticText.new(row_layout, Rect(0,0,30,height));
	txt_rec.string = "Stop";


	paraspace = ParaSpace.new(row_layout, bounds: Rect(15, 15, width, height));

	ps_bg_drawf = { arg wid, he, dur, numsep;
		var vdur = dur - param.get_start_silence - param.get_end_silence;
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
			Pen.color = Color.blue(1);
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

	~make_view_responder.(parent, param, (
		selected: { arg self;
			bt_name.value = display.selected;
		},
		notes: { arg self;

			var totaldur = 0, minnote=127, maxnote=0;
			var numsep;
			var soff = param.get_start_silence;
			var eoff = param.get_end_silence;
			var sum = 0;
			var notes = self.get_notes;
			//var playeddur = self.total_dur( notes[1..(notes.lastIndex-1)] ) + (notes[0].dur - param.get_start_silence);
			var playeddur = self.total_dur( notes );
			playeddur.debug("playeddur");
			

			paraspace.clearSpace;
			self.get_notes.do { arg no, idx;
				[idx, no].debug("calcul totaldur: note");
				if(no.midinote.isSymbol.not, {
					minnote = min(no.midinote, minnote);
					maxnote = max(no.midinote, maxnote);
				});
				totaldur = no.dur + totaldur
			};

			[totaldur, minnote, maxnote].debug("stat");

			numsep = display.noteline_numbeats ?? max(roundUp(totaldur+1),5); // one sep per beat + offset + marge
			
			paraspace.setBackgrDrawFunc_(ps_bg_drawf.(width, height, playeddur, numsep));

			self.get_notes.do { arg no, idx;
				[idx, no].debug("note");
				if(no.midinote.isSymbol.not, {
					paraspace.createNode(sum/numsep * width, no.midinote.linlin(minnote-1,maxnote+1,height,0));
				});
				if(self.notequant.notNil) {
					sum = sum + no.dur.round(self.notequant);
				} {
					sum = sum + no.dur;
				}
			};
		},

		recording: { arg self, msg, recording;
			if( recording, {
				txt_rec.string = "Rec";
				txt_rec.background = ~editplayer_color_scheme.led;
			}, {
				txt_rec.string = "Stop";
				txt_rec.background = Color.clear;
			});
		}

	));
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
	var max_cells = display.max_cells; // FIXME: already defined in editplayer
	var inrange;

	"mais quoiiii".debug;

	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+10),height));
	row_layout.background = ~editplayer_color_scheme.control;

	bt_name = ~make_name_button.(row_layout, display.name, display.name_width ?? btnamesize, height);

	if(display.show_midibloc, {
		midibloc = GUI.hLayoutView.new(row_layout, Rect(0,0,150,height));
		txt_midi_label = GUI.staticText.new(midibloc, Rect(0,0,15,height));
		txt_midi_val = GUI.staticText.new(midibloc, Rect(0,0,75,height));

		slider = GUI.slider.new(midibloc, Rect(0,0,60,height));
	},{
		// spacer
		txt_midi_val = GUI.staticText.new(row_layout, Rect(0,0,30,height));
	});

	btl_cells = GUI.hLayoutView.new(row_layout, Rect(0,0,width,height));

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

	~make_view_responder.(parent, param, (
		selected: { arg self;
			bt_name.value = display.selected;
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

		val: { arg self, msg, cellidx;
			var newval;
			cellidx.debug("val handler: cellidx");
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
		},

		cells: { arg self; 
			var cells, bank, start, range, sel;
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
			range.debug("cells");
			cells[ start..((start+max_cells)-1) ].debug("cells");
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
						sel.debug("selected cell");
						btl_cells.children.wrapAt( sel % max_cells ).debug("wrapat");
						btl_cells.children.wrapAt( sel % max_cells ).value = 1;
					})
				}
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

		cells: { arg self, msg, cellidx;

			param.get_val.debug("simplecontrol val");
			~seq.debugme2 = param;
			txtval.string = param.get_val;
		}

	));
};

// only used in score.sc for the moment, editplayer use make_control_view
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

	~make_step_cell = { arg parent, label, width, height;
		var lb;
		lb = StaticText.new(parent, width@height);
		if(label % 8 == 0, {
			lb.string = (label / 8)+1;
		}, {
			lb.string = "";	
		});
		lb.align = \center;
		//lb.stringColor = ~editplayer_color_scheme.control;
		lb.stringColor = Color.white;
		lb;
	};
	~set_step_state = { arg cell, state;
		if(state == 1, { 
			cell.background = Color.black;
		}, {
			cell.background = Color.grey;
		})
	};

	"mais quoiiii".debug;

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
		txt_midi_val = GUI.staticText.new(row_layout, Rect(0,0,30,height));
	});

	btl_cells = GUI.hLayoutView.new(row_layout, Rect(0,0,width,height));

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
			cellidx.debug("val handler: cellidx");
			self.get_cells[cellidx].debug("val handler: cellidx value");
			newval = self.get_cells[cellidx];
			~set_step_state.(btl_cells.children[ cellidx % max_cells ], newval);
			//self.debug("val changed");
			if(self.classtype == \stepline, { 
				~set_step_state.(btl_cells.children[ cellidx % max_cells ], newval);
			},{
				if(slider.nolNil, {
					slider.value = self.spec.unmap(newval);
				});
			});
			//btl_cells.children[ cellidx ].states.debug("======heeeeerreeeee");
			//btl_cells.children[ cellidx ] = newval;
		},

		cells: { arg self; 
			var cells, bank, start, range, sel;
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
// ==========================================
// PARAM FACTORY
// ==========================================


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
		spec: ~spec_adsr,
		sub_midi: Dictionary.new, // store midi control handler specific to each point of the adsr
		sub_param: Dictionary.new,
		selected: 0,

		save_data: { arg self;
			var data = ();
			[\name, \classtype, \selected, \spec, \val].do {
				arg key;
				data[key] = self[key];
			};
			data;
		},

		load_data: { arg self, data;
			[\name, \classtype, \selected, \spec, \val].do {
				arg key;
				self[key] = data[key];
			};
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

		get_param: { arg self, key;
			self.sub_param[key];
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

			Pfunc({
				[ ~adsr_event_to_env.(self.val).asArray ]
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

~make_dur_param = { arg name, default_value;


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

~make_binded_number_param = { arg main, name, spec, get_fun, set_fun;
	var res = (
		name: name,
		//val: { arg self; tempoclock.tempo },
		classtype: \binded_number,
		spec: spec,

		get_norm_val: { arg self;
			self.spec.unmap(get_fun.())
		},
		set_norm_val: { arg self, val;
			val.debug("make_tempo_param: set_norm_val");
			set_fun.(self.spec.map(val));
			self.changed(\val);
		},

		get_val: { arg self;
			get_fun.();
		},

		set_val: { arg self, val;
			set_fun.(val);
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


~make_buf_param = { arg name, default_value, player, spec;

	var param;

	param = (
		val: nil,
		name: name,
		classtype: \buf,
		current_kind: \sample,
		has_custom_buffer: false,
		spec: nil,
		selected: 0,
		buffer: nil,

		save_data: { arg self;
			var data = ();
			[\name, \classtype, \selected, \spec, \val].do {
				arg key;
				data[key] = self[key];
			};
			data.debug("make_buf_param: save_data: data");
			data;
		},

		load_data: { arg self, data;
			[\name, \classtype, \selected, \spec].do {
				arg key;
				self[key] = data[key];
			};
			self.set_val(data[\val]);
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
			self.buffer = BufferPool.get_sample(player.uid, path);
		},

		set_custom_buffer: { arg self, buf, name;
			self.val = name;
			self.has_custom_buffer = true;
			self.buffer = buf;
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
				repeat.do {
					self.current_kind.debug("2222222222222222222222 entering bufnum vpattern loop");
					switch(player.get_mode,
						\sampleline, {
							~samplekit_manager.slot_to_bufnum(ev[\sampleline].slotnum, ev[\samplekit]).debug("bufnum::::");
							ev = ~samplekit_manager.slot_to_bufnum(ev[\sampleline].slotnum, ev[\samplekit]).yield;
						},
						// else
						{
							ev = self.buffer.bufnum.yield;
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

~empty_note = (
	midinote: \rest,
	sustain: 0.1,
	dur: 0.01
);


~default_noteline3 = [ // FIXME: crash when no notes
	(
		midinote: \rest,
		sustain: 0.1,
		start_silence: 0.5,
		default_start_silence: 0.5,
		end_silence: 2.0,
		default_end_silence: 2.0,
		start_offset: 0,
		end_offset: 0,
		dur: 0.5
	),
	(
		midinote: 68,
		sustain: 0.1,
		dur: 1.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 2.0
	),
];
~default_noteline = [ // FIXME: crash when no notes
	(
		midinote: \rest,
		sustain: 0.1,
		start_silence: 0.5,
		default_start_silence: 0.5,
		end_silence: 0.5,
		default_end_silence: 0.5,
		start_offset: 0,
		end_offset: 0,
		dur: 0.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 0.9
	),
	(
		midinote: 67,
		sustain: 0.1,
		dur: 0.1
	),
	(
		midinote: 68,
		sustain: 0.1,
		dur: 1.5
	),
	(
		midinote: 64,
		sustain: 0.1,
		dur: 0.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 0.5
	),
];
~default_noteline2 = [
	(
		midinote: 64,
		sustain: 0.1,
		dur: 0.5
	),
	(
		midinote: 65,
		sustain: 0.1,
		dur: 0.4
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 0.9
	),
	(
		midinote: 67,
		sustain: 0.1,
		dur: 0.1
	),
	(
		midinote: 68,
		sustain: 0.1,
		dur: 1.5
	),
	(
		midinote: 69,
		sustain: 0.1,
		dur: 0.6
	)
];

~default_sampleline = [ // FIXME: crash when no notes
	(
		slotnum: \rest,
		sustain: 0.1,
		start_silence: 0.5,
		default_start_silence: 0.5,
		end_silence: 2.0,
		default_end_silence: 2.0,
		start_offset: 0,
		end_offset: 0,
		dur: 0.5
	),
	(
		slotnum: 0,
		sustain: 0.1,
		dur: 1.5
	),
	(
		slotnum: 0,
		sustain: 0.1,
		dur: 2.0
	),
];

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

		save_data: { arg self;
			var data = ();
			[\name, \classtype, \selected, \spec, \val].do {
				arg key;
				data[key] = self[key];
			};
			data.debug("make_buf_param: save_data: data");
			data;
		},

		load_data: { arg self, data;
			[\name, \classtype, \selected, \spec].do {
				arg key;
				self[key] = data[key];
			};
			self.set_val(data[\val]);
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

~make_parent_recordline_param = { arg name, default_value=[];
	var ret;
	ret = (
		name: name,
		classtype: \noteline,
		selected_cell: 0,
		selected: 0,
		default_val: default_value.asList,
		notes: ~default_noteline3.deepCopy,
		start_offset: 0,
		end_offset: 0,
		muted: false,
		archive_data: [\name, \classtype, \selected, \selected_cell, \default_val, \notes, \start_offset, \end_offset, \notequant],
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

		get_notes: { arg self;
			var no;
			no = self.vnotes.deepCopy;
			//no[0].dur+self.start_offset;
			//no.last.dur+self.end_offset;
			no;
		},

		set_notes: { arg self, val;
			self.notes = val;
			self.update_note_dur;
		},

		get_note: { arg self, idx;
			var no;
			if( self.vnotes.size > 0 && {self.muted.not}) {
				no = self.vnotes[idx].deepCopy;
				if( no.notNil && (self.notequant.notNil) ) {
					no.dur = no.dur.round(self.notequant);
					no;
				} {
					no
				}
			} {
				"noteline_param: get_node: No notes".inform;
				if(idx == 0) {
					~empty_note;
				} {
					nil;
				};
			}
		},

		total_dur: { arg self, notes;
			var res=0;
			notes.do { arg x; res = x.dur + res; };
			res;
		},

		update_note_dur: { arg self;
			var find_next, find_prev, delta, prevdelta, idx, previdx;
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

				#delta, idx = find_next.(self.notes[0].start_offset, self.notes);
				#prevdelta, previdx = find_prev.(self.notes[0].end_offset, self.notes);
				[delta, idx, prevdelta, previdx].debug("delta, idx, prevdelta, previdx");
				self.notes[0].dur = self.notes[0].start_silence + delta;
				self.vnotes = [self.notes[0]] ++ self.notes[idx..previdx].deepCopy;
				self.vnotes[self.vnotes.lastIndex].dur = self.notes[0].end_silence + prevdelta;
				self.vnotes.debug("update_note_dur: vnotes");
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
					self.changed(\notes);
				}
			}
					
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
			if(self.notes.size > 0) {
				self.notes[0].start_silence;
			}

		},

		get_end_silence: { arg self;
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
			self.notequant = val;
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
		vpattern: { arg self; 
			~pdynarray.( { arg idx; self.tick; self.classtype.debug("classtype!!!!!!!!"); self.get_note(idx) }, self.classtype );
		};

	);
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
	ret;
};

~make_noteline_param = { arg name, default_value=[];
	var ret;
	ret = (
		parent: ~make_parent_recordline_param.(name, default_value),
		classtype: \noteline,
		notes: ~default_noteline3.deepCopy
	);
	ret;
};

~make_stepline_param = { arg name, default_value;
	var ret;
	ret = (
		name: name,
		classtype: \stepline,
		selected_cell: 0,
		selected: 0,
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

		set_val: { arg self, val;
			self.seq.val[ self.get_selected_cell.() ] = if(val > 1, { 1 },{ 0 });
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
			self.changed(\cells);
			self.changed(\selected);
		},
		vpattern: { arg self; 
			"--------making vpattern of stepline".debug;
			~pdynarray.( { arg idx; 
				self.tick(idx); 
				if(self.seq.val[idx] == 1, { [idx, TempoClock.beats, TempoClock.beatInBar].debug("----------TICK step"); });
				self.seq.val[idx];
			}, \stepline );
			//Pseq([1,1,1,1],inf);
		};
	);
	ret;
};

~make_type_param = { arg name;
	var ret;
	ret = (
		name: name,
		classtype: \type,
		refresh: { arg self; },
		vpattern: { arg self; 
			Pfunc({ arg ev;
				if(ev[\muted]) {
					\rest
				} {
					if(ev[\stepline] > 0) {
						\note
					} {
						\rest
					}
				}
			})
		}
	);
	ret;
};

~make_volume_param = { arg name, main;
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

~make_control_param = { arg main, player, name, kind, default_value, spec;
	var param;
	var bar_length = 4;

	param = (
		name: name,
		classtype: \control,
		current_kind: kind,
		spec: spec,
		selected: 0,	 // bool
		selected_cell: 0,
		bar_length: bar_length,
		default_val: default_value,
		archive_data: [\name, \classtype, \current_kind, \spec, \selected, \selected_cell, \default_val, \noteline],

		save_data: { arg self;
			var data = ();
			self.archive_data.do {
				arg key;
				data[key] = self[key];
			};
			[\seq, \scalar, \preset].do { arg kind;
				data[kind] = ();
				[\val, \selected_cell].do { arg key;
					data[kind][key] = self[kind][key];
				}
			};
			data;
		},

		load_data: { arg self, data;
			self.archive_data.do {
				arg key;
				self[key] = data[key];
			};
			[\seq, \scalar, \preset].do { arg kind;
				[\val, \selected_cell].do { arg key;
					 self[kind][key] = data[kind][key];
				}
			};
		},

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
			selected_cell: 0, // always 0
			val: if(default_value.isArray, { default_value[0] }, { default_value }),

			set_val: { arg self, val, idx=nil;
				self.val = val;
				param.changed(\val, 0);
			},
			get_val: { arg self; self.val },

			set_norm_val: { arg self, norm_val;
				self.val = param.spec.map(norm_val);
				param.changed(\val, 0);
			},
			get_norm_val: { arg self;
				param.spec.unmap(self.val);
			},

			get_cells: { arg self; [self.val] },

			select_cell: { arg self, idx; param.seq.selected_cell = idx }, // when changing kind, correct cell is selected in colselect mode
			get_selected_cell: { arg self; 0 },
			add_cells: {},
			remove_cells: {}
		),

		bus: (
			// dummy functions
			selected_cell: 0, // always 0
			val: if(default_value.isArray, { default_value[0] }, { default_value }),
			busindex: nil,

			get_bus: { arg self;
				self.bus;
			},

			set_bus: { arg self, val;
				self.bus = val;
			},

			set_val: { arg self, val, idx=nil;
				self.val = val;
				param.changed(\val, 0);
			},
			get_val: { arg self; self.val },

			set_norm_val: { arg self, norm_val;
				self.val = param.spec.map(norm_val);
				param.changed(\val, 0);
			},
			get_norm_val: { arg self;
				param.spec.unmap(self.val);
			},

			get_cells: { arg self; [self.val] },

			select_cell: { arg self, idx; param.seq.selected_cell = idx }, // when changing kind, correct cell is selected in colselect mode
			get_selected_cell: { arg self; 0 },
			add_cells: {},
			remove_cells: {}
			

		),
		

		// preset subobject here
		//		need a corresponding spec

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
		
		change_kind: { arg self, kind;
			self.current_kind = kind;
			if(kind == \seq && { self.seq.initialized.not } ) { self.seq.init(self.scalar.get_val) };
			[name, kind].debug("CHANGED KIND!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			self.changed(\kind);
		},

		refresh: { arg self;
			self.changed(\kind);
			self.changed(\selected);
			self.changed(\cells);
			self.midi.refresh;
		},

		vpiano: { arg self;
			{
				switch(self.current_kind,
					\preset, { self.preset.val[self.preset.selected_cell] },
					//default
					{ self.scalar.val }
				);
			}
		},

		vpattern: { arg self; 
			var segf, scalf, pref, scalm;
			switch(self.name,
				\dur, {
					segf = { arg ev;
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
					segf = { arg ev;
						ev[\noteline].midinote.midicps;	
					};
					scalm = { arg ev; self.scalar.val };
					scalf = pref = segf;
				},
				\sustain, {
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
				var repeat = 1000000;
				var idx, val=0;
				repeat.do {
					//[name, ev].debug("################################## prout!!!!");
					//ev.dump;
					switch( self.current_kind,
						\scalar, {
							//ev.debug("=========== in scalar ev");
							8.do {		// hack to be in phase when changing kind (should be the size of stepline)
								switch(player.get_mode,
									\sampleline, {
										[name, scalm.value(ev)].debug("############# scalm!!");
										ev = scalm.value(ev).yield;
										//ev = 1.yield;
									},
									\noteline, {
										ev = scalf.value(ev).yield;
									},
									\stepline, {
										ev = self.scalar.val.yield;
									}
								);
							}
							//ev.debug("=========== in scalar ev END");
						},
						\seg, {
							[
								ev[\elapsed], ev[\segdur], 
								ev[\elapsed]/ev[\segdur], 
								(self.seg.val.size),
								(ev[\elapsed]/ev[\segdur]) % (self.seg.val.size),
								self.seg.val.blendAt((ev[\elapsed]/ev[\segdur]) % (self.seg.val.size))
							].debug("seggggggggggggggggg: elapsed, segdur, size, el/dur, el/dur%size, res");
							ev = (self.seg.val++[self.seg.val[0]]).blendAt((ev[\elapsed]/ev[\segdur]) % (self.seg.val.size)).yield;
						},
						\seq, {
							if(player.get_mode == \noteline, {
								//ev.debug("=========== in noteline ev");
								//segf.debug("=========== in noteline segf");
								//segf.value(ev).debug("=========== in noteline");
								ev = segf.value(ev).yield;
							}, {
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
							self.bus.get_bus.asMap;
						}
					);
				}
			});
			//Prout({
		//		inf.do { self.scalar.get_val.yield };
			//});
			//Pseq([self.get_val],inf);
			//Pseq([self.scalar.get_val],inf);
		}
	);
	// init
	param.preset = param.seq.deepCopy;
	param.seg = param.seq;

	param.midi = main.midi_center.get_midi_control_handler(param);

	// \dur special case
	if([\dur,\segdur, \stretchdur].includes(name), {
		param.change_kind(\preset);
		param.preset.val = List[ 4, 2, 1, 0.5, 0.25, 0.125, 0.0625 ];
		if(name == \stretchdur, {
			param.select_cell(2);
		}, {
			param.select_cell(4);
		});
	});

	// return object
	param;
};
