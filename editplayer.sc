(

~set_numpad_mode = { arg commands, fun;
	var buf = "", restorefun;

	restorefun = commands.overload_mode([\editplayer, \edit_value_mode]);

	commands.array_set_action([\editplayer, \edit_value_mode, \insert_number], 10, { arg i;
		[buf, i, i.asString].debug("ibuf");
		buf = buf ++ i.asString;
		[buf, i, i.asString].debug("buf");
	});
	commands.set_action([\editplayer, \edit_value_mode, \insert_point], { buf = buf ++ "." });
	commands.set_action([\editplayer, \edit_value_mode, \cancel], { restorefun.() });
	commands.set_action([\editplayer, \edit_value_mode, \ok], { restorefun.(); [buf, i, i.asString].debug("ok buf"); fun.(buf) });


};

~rel_nextTimeOnGrid = { arg beats, quant = 1, phase = 0;
				var baseBarBeat = TempoClock.baseBarBeat;
                if (quant == 0) { beats + phase };
                if (phase < 0) { phase = phase % quant };
                roundUp(beats - baseBarBeat - (phase % quant), quant) + baseBarBeat + phase
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

~make_bufnum_view = { arg parent, display, param;

	var txt_buf, bt_name;
	var param_messages, sc_param, 
		width = 1088,
		height = 60;
	var row_layout;

	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+30),height));
	row_layout.background = ~editplayer_color_scheme.background;


	bt_name = ~make_name_button.(row_layout, display.name, xsize:display.name_width, ysize:height);
	txt_buf = GUI.staticText.new(row_layout, Rect(0,0,200,height));
	txt_buf.string = "nil";

	param_messages = Dictionary.newFrom((
		selected: { arg self;
			bt_name.value = display.selected;
		},

		val: { arg self, msg, cellidx;
			var newval;
			txt_buf.string = param.get_val;
		}

	));

	sc_param = SimpleController(param);
	~init_controller.(sc_param, param_messages);
	param.refresh.();


	// remove func

	row_layout.onClose = {
		param.name.debug("=========== view closed");
		sc_param.remove;
	};

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
		width = 1088,
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
		var eoff = param.get_end_offset;
		var sepsize = wid/numsep;
		{
			Pen.color = Color.gray(0.7);
			Pen.addRect(Rect(param.get_start_offset*sepsize,0,(dur+eoff)*sepsize,he));
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

	param_messages = Dictionary.newFrom((
		selected: { arg self;
			bt_name.value = display.selected;
		},
		notes: { arg self;

			var sum = param.get_start_offset;
			var totaldur = 0, minnote=127, maxnote=0;
			var numsep;
			var eoff = param.get_end_offset;
			var soff = param.get_start_offset;
			

			paraspace.clearSpace;
			self.notes.do { arg no, idx;
				if(no.midinote.isSymbol.not, {
					minnote = min(no.midinote, minnote);
					maxnote = max(no.midinote, maxnote);
				});
				totaldur = no.dur + totaldur
			};

			[totaldur, minnote, maxnote].debug("stat");

			numsep = display.noteline_numbeats ?? (totaldur+roundUp(soff+eoff)+1); // one sep per beat + offset + marge
			
			paraspace.setBackgrDrawFunc_(ps_bg_drawf.(width, height, totaldur, numsep));

			self.notes.do { arg no, idx;
				if(no.midinote.isSymbol.not, {
					[idx, no].debug("note");
					paraspace.createNode(sum/numsep * width, no.midinote.linlin(minnote-1,maxnote+1,height,0));
				});
				sum = sum + no.dur;
			};
		};

	));

	midi_messages = Dictionary.newFrom((
		recording: { arg self;
			if( self.recording, {
				txt_rec.string = "Rec";
				txt_rec.background = ~editplayer_color_scheme.led;
			}, {
				txt_rec.string = "Stop";
				txt_rec.background = Color.clear;
			});
		}
	));
	
	sc_param = SimpleController(param);
	~init_controller.(sc_param, param_messages);
	param.refresh.();

	midi.debug("&&&&&&&&&&&&&&&&&&&&&&midi");
	if(midi.notNil, {
		sc_midi = SimpleController(midi);
		~init_controller.(sc_midi, midi_messages);
		midi.refresh.();
	}, {
		"midi is nil".debug;
	});

	// remove func

	row_layout.onClose = {
		param.name.debug("=========== view closed");
		sc_param.remove;
		if(midi.notNil, { sc_midi.remove });
	};
};

~make_env_control_view = { arg parent, player, param;

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



	param_messages = Dictionary.newFrom((
		selected: { arg self;
			bt_name.value = self.selected;
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

	midi_messages = Dictionary.newFrom((
		midi_val: { arg self;
			txt_midi[self.name].string = self.mapped_val;
		},
		blocked: { arg self;
			txt_midi[self.name].background = if(self.blocked == \not, { Color.clear }, { ~editplayer_color_scheme.led });
		}
	));
	
	sc_param = SimpleController(param);
	~init_controller.(sc_param, param_messages);
	param.refresh.();

	midi.debug("&&&&&&&&&&&&&&&&&&&&&&midi");
	midi.col.do { arg midi_point;
		if(midi.notNil, {
			sc_midi = SimpleController(midi_point);
			~init_controller.(sc_midi, midi_messages);
			midi_point.refresh.();
		}, {
			"midi is nil".debug;
		});
	};

	// remove func

	row_layout.onClose = {
		param.name.debug("=========== view closed");
		sc_param.remove;
		if(midi.notNil, { sc_midi.remove });
	};
};

~make_control_view = { arg parent, display, param, midi, param_name=nil, btnamesize=50;
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

		slider = GUI.slider.new(midibloc, Rect(0,0,30,height));
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

	param_messages = Dictionary.newFrom((
		selected: { arg self;
			bt_name.value = display.selected;
		},

		selected_cell: { arg self, msg, oldsel;
			
			oldsel.debug("selected cell oldsel");
			display.name.debug("selected_cell display name");
			if(oldsel.notNil && inrange.(oldsel), {
				btl_cells.children[ oldsel % max_cells ].value = 0;
			});
			if(inrange.(), {
				btl_cells.children[ display.get_selected_cell % max_cells ].value = 1;
			});
		},

		val: { arg self, msg, cellidx;
			var newval;
			cellidx.debug("val handler: cellidx");
			self.get_cells[cellidx].debug("val handler: cellidx value");
			newval = self.get_cells[cellidx];
			~change_button_label.(btl_cells.children[ cellidx % max_cells ], newval);
			//self.debug("val changed");
			if(self.classtype == \stepline, { 
				btl_cells.children[ cellidx % max_cells ].value = newval
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
				sel = display.get_selected_cell;
				if( sel >= start && (sel < (start+max_cells)), {
					btl_cells.children[ sel % max_cells ].value = 1;
				})
			})
		},

		kind: { arg self;
			self.current_kind.debug("make_control_view changed kind");
			self.get_cells.debug("make_control_view.kind get_cells");
			self.changed(\cells);	
		}
	));
	midi_messages = Dictionary.newFrom((
		label: { arg self;
			txt_midi_label.string = self.label;
		},
		midi_val: { arg self;
			txt_midi_val.string = self.mapped_val;
		},
		blocked: { arg self;
			txt_midi_val.background = if(self.blocked == \not, { Color.green }, { ~editplayer_color_scheme.led });
		},
		recording: { arg self;
			txt_midi_label.background = if(self.recording, { ~editplayer_color_scheme.led }, { Color.clear });
		},
		midi_key: { arg self, msg, key;
			param_messages.val(param, msg); //TODO
		}
			


	));
	
	sc_param = SimpleController(param);
	~init_controller.(sc_param, param_messages);
	param.refresh.();

	if(midi.notNil, {
		sc_midi = SimpleController(midi);
		~init_controller.(sc_midi, midi_messages);
		midi.refresh.();
	}, {
		"midi is nil".debug;
	});

	"mais quoiiii".debug;

	// remove func

	row_layout.onClose = {
		param.name.debug("=========== view closed");
		sc_param.remove;
		if(midi.notNil, { sc_midi.remove });
	};
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

	param_messages = Dictionary.newFrom((
		selected: { arg self;
			bt_name.value = display.selected;
		},
		selected_cell: { arg self;
			param.changed(\cells);
		},

		cells: { arg self, msg, cellidx;

			param.get_val.debug("simplecontrol val");
			txtval.string = param.get_val;
		}

	));

	sc_param = SimpleController(param);
	~init_controller.(sc_param, param_messages);
	param.refresh.();

	// remove func

	row_layout.onClose = {
		sc_param.remove;
	};
};


~make_stepline_view = { arg parent, display, param, midi;
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
		start = max_cells * display.get_bank.();
		sel = sel ?? display.get_selected_cell;
		start.debug("inrange====");
		sel.debug("inrange");
		(sel >= start && (sel < (start+max_cells))).debug("inrange");
		(sel >= start && (sel < (start+max_cells)))
	};

	param_messages = Dictionary.newFrom((
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

			bank = display.get_bank.();

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
		}
	));
	midi_messages = Dictionary.newFrom((
		label: { arg self;
			txt_midi_label.string = self.label;
		},
		midi_val: { arg self;
			txt_midi_val.string = self.mapped_val;
		},
		blocked: { arg self;
			txt_midi_val.background = if(self.blocked == \not, { Color.green }, { ~editplayer_color_scheme.led });
		},
		recording: { arg self;
			txt_midi_label.background = if(self.recording, { ~editplayer_color_scheme.led }, { Color.clear });
		},
		midi_key: { arg self, msg, key;
			param_messages.val(param, msg); //TODO
		}
			


	));
	
	sc_param = SimpleController(param);
	~init_controller.(sc_param, param_messages);
	param.refresh.();

	if(midi.notNil, {
		sc_midi = SimpleController(midi);
		~init_controller.(sc_midi, midi_messages);
		midi.refresh.();
	}, {
		"midi is nil".debug;
	});

	"mais quoiiii".debug;

	// remove func

	row_layout.onClose = {
		param.name.debug("=========== view closed");
		sc_param.remove;
		if(midi.notNil, { sc_midi.remove });
	};

};

/////////////////////////////////////////////////////////////////////////
/////////		MIDI
/////////////////////////////////////////////////////////////////////////

~midi_interface = (
	next_free: (
		slider: 0,
		knob: 0
	),
	registry: List[],

	cc_val: Dictionary[
		[\slider, 0] -> 0

	],

	assign_adsr: { arg self, param;
		var col = Dictionary.new;
		var mc = ();
		~adsr_point_order.do { arg key, i;
			col[key] = ~make_midi_control.([\slider, i+4], self.cc_val, param.get_param(key))
		};
		mc.col = col;
		mc.kind = \adsr;
		mc.destroy = { arg self; self.col.do(_.destroy) };
		self.registry.add( mc );
		mc;
	},

	assign_master: { arg self, param;
		var mc;
		mc = ~make_midi_control.([\slider, 8], self.cc_val, param);
		self.registry.add( mc );
		mc;
	},

	assign_first: { arg self, kind, param;
		var mc;
		mc = ~make_midi_control.([kind, self.next_free[kind]], self.cc_val, param);
		self.registry.add( mc );
		// FIXME: set max free val (adsr hold the last sliders)
		self.next_free[kind] = self.next_free[kind] + 1;
		mc;
	},

	clear_assigned: { arg self, kind;
		self.next_free[kind] = 0;
		self.registry.copy.do { arg mc;
			// FIXME: what is this kind member ?
			if(mc.kind == kind, {
				mc.destroy;
				self.registry.remove(mc);
			});
		};
	}

);


~make_midi_control = { arg ccid, cc_val, param; 
	// param interface:
	// - classtype
	// - get_norm_val
	// - set_norm_val
	// - selected
	// - spec
	var midi;
	var sc_param;
	var get_midi_val = { cc_val[ccid] ?? 0.5};
	var param_val = { param.get_norm_val };

	//param.debug("make_midi_control param");

	midi = (
		blocked: \not,
		ccid: nil,
		midi_val: get_midi_val.(),
		label: \void,
		name: param.name,
		recording: false,
		mapped_val: { arg self;
			param.spec.map(get_midi_val.())
		},

		refresh: { arg self;
			self.changed(\label);
			self.changed(\blocked);
			self.changed(\midi_val);
		},

		set_value: { arg self, cc_value;
			cc_val[ccid] = cc_value;
			self.midi_val = cc_value;
			self.changed(\midi_val);

			if(self.recording.not, {
				if(ccid.notNil, {
					self.unblock_do({
						param.set_norm_val(cc_value);
					});
				}, {
					param.set_norm_val(cc_value);
				});
			});
		},

		get_midi_val: { arg self; get_midi_val.() },

		unblock_do: { arg self, fun;
			var cc_value, param_value;

		
			//param.debug("make_midi_control.unblock_do param");
			cc_value = get_midi_val.();
			param_value = param.get_norm_val.();
			param.name.debug("midi unblock param name");
			param_value.debug("midi unblock param value");

			switch(self.blocked,
				\not, fun,
				\sup, {
					if( cc_value <= param_value , {
						self.blocked = \not;
						self.changed(\blocked);
						fun.value;
					});
				},
				\inf, {
					if( cc_value >= param_value , {
						self.blocked = \not;
						self.changed(\blocked);
						fun.value;
					});
				}
			);

		},
		
		block: { arg self;
			var cc_value, param_value;

			cc_value = get_midi_val.();
			param_value = param.get_norm_val.();
			param.name.debug("midi block param name");
			param_value.debug("midi block param value");

			case 
				{ cc_value > param_value } {
					self.blocked = \sup;
					self.changed(\blocked);
				}
				{ cc_value < param_value } {
					self.blocked = \inf;
					self.changed(\blocked);
				}
				{ true } {
					self.blocked = \not;
					self.changed(\blocked);
				}
		},

		assign_cc: { arg self, ccid;
			self.ccid = ccid;
			 if(ccid.notNil, {
				self.label = (ccid[0].asString[0] ++ (ccid[1]+1)).asSymbol;
				self.changed(\label);
				self.ccresp = CCResponder({ |src,chan,num,value|
						//[src,chan,num,value].debug("==============CCResponder");
						//param.classtype.debug("ccresp current_kind");
						//param.selected.debug("ccresp selected");
						//cc_val.debug("ccresp cc_val");
						if(param.classtype == \adsr, {
							if(param.selected == 1, {
								self.set_value(value/127);
							}, {
								// FIXME: don't update gui
								cc_val[ccid] = value/127;	
							});
						}, {
							self.set_value(value/127);
						});
					},
					nil, // any source
					nil, // any channel
					~cakewalk[ccid[0]][ccid[1]], // any CC number
					nil // any value
				);
				}, {
					"assigning nil ccid".debug("WARNING");
					"void"
				});

		},

		destroy: { arg self;
			self.ccresp.remove;
		}
	);

	midi.assign_cc(ccid);
	midi.block.();

	sc_param = SimpleController(param);
	sc_param.put(\kind, { arg obj; midi.block.() });
	sc_param.put(\selected_cell, { arg obj; "BLOK1".debug; midi.block.() });
	sc_param.put(\selected, { arg obj; "BLOK2".debug; midi.block.() });


	midi;

};

~piano_recorder = { arg player;
	var prec, livesynth = player.get_piano;
	NoteOnResponder.removeAll;
	NoteOffResponder.removeAll;

	prec = (
		nonr: nil,
		noffr: nil,
		track: List.new,
		book: Dictionary.new,
		livebook: Dictionary.new,
		lastnote: nil,
		recording: false,

		start_recording: { arg self;
			if(self.recording, {
				"already recording!!!".debug;
			}, {
				self.recording = true;
				self.track = List.new;
				self.book = Dictionary.new;
				self.livebook = Dictionary.new;
				self.lastnote = nil;

				self.nonr = NoteOnResponder { arg src, chan, num, veloc;
					var note, firstnote;

					[TempoClock.beats, TempoClock.nextTimeOnGrid(EventPatternProxy.defaultQuant,0), EventPatternProxy.defaultQuant].debug("nc,tc,dq");
					self.livebook[num] = livesynth.value(num.midicps, veloc/127);
					
					[src, chan, num, veloc].debug("note on");
					note = (
						midinote: num,
						velocity: veloc,
						curtime: TempoClock.beats - s.latency
					);
					if(self.lastnote.isNil, {
						// first note
						firstnote = (
							midinote: \rest,
							velocity: 0,
							sustain: 0.1,
							dur: note.curtime - (TempoClock.nextTimeOnGrid(EventPatternProxy.defaultQuant,0) - EventPatternProxy.defaultQuant) 
						);
						if(firstnote.dur < 0, { "Negative dur!!".warning; firstnote.dur = 0; });
						self.track.add(firstnote);
					}, {
						self.lastnote.dur = note.curtime - self.lastnote.curtime
					});
					self.book[num] = note;
					self.lastnote = note;
					self.track.add(note);
					note.debug("nonr note");
				};
				self.noffr = NoteOffResponder { arg src, chan, num, veloc;
					var note;
					self.livebook[num].release;
					note = self.book[num];
					self.book.removeAt(num);
					note.debug("noffr note");
					self.book.debug("noffr book");
					note.sustain = TempoClock.beats - s.latency - note.curtime;
				};
			})
		},

		stop_recording: { arg self;
			if(self.recording, {
				self.nonr.remove;
				self.noffr.remove;
				if(self.lastnote.notNil, {
					self.lastnote.dur = ~rel_nextTimeOnGrid.(self.lastnote.curtime, EventPatternProxy.defaultQuant,0) - self.lastnote.curtime;
				});
				self.recording = false;
			}, {
				"already stoped!!!!".debug;
			})
		}


	);
	prec;

};
~make_midi_kb_control = { arg player, editplayer;
	var nonr, noffr, param, destroy, rec, localknob;
	
	// init
	NoteOnResponder.removeAll; // TODO: must save others responders ?

	nonr = NoteOnResponder { arg src, chan, num, veloc;
		param = editplayer.controller.get_selected_param.();
		param.set_val(num.midicps);
	};

	rec = CCResponder({ |src,chan,num,value|
			var param = editplayer.controller.get_selected_param;
			var tickline;
			tickline = player.get_arg(\stepline); 

			if(param.midi.notNil && param.midi.recording.notNil, {
				if(param.midi.recording, {
					param.midi.recording = false;
					param.midi.changed(\recording);
					tickline.tick = { 
						//"TICK!!!".postln;
					};
				}, {
					param.midi.recording = true;
					param.midi.changed(\recording);
					// TODO : make the rec works with noteline
//					if(param.noteline, {
//						TempoClock.schedAbs(TempoClock.beats.roundUp( player.get_arg(\segdur).get_val ).debug("noteline tick"), {
//							param.midi.get_midi_val.debug("set TICK");
//							param.seq.set_norm_val(param.midi.get_midi_val, idx);
//							if(param.midi.recording, {
//								TempoClock.beats.roundUp( player.get_arg(\segdur).get_val )
//							}, {
//								nil // dont reschedule
//							}
//						})
//					}, {
						tickline.tick = { arg obj, idx;
							param.midi.get_midi_val.debug("set TICK");
							param.seq.set_norm_val(param.midi.get_midi_val, idx);
						};
//					});
				});
			});
		},
		nil, // any source
		nil, // any channel
		~cakewalk.button[7], // record button
		nil // any value
	);

	localknob = CCResponder({ |src,chan,num,value|
			//[src,chan,num,value].debug("==============CCResponder");
			var param = editplayer.controller.get_selected_param;
			if(param.midi.notNil, {
				param.midi.set_value(value/127);
			});
		},
		nil, // any source
		nil, // any channel
		~cakewalk.knob[8], // last knob
		nil // any value
	);

	destroy = { 
		nonr.remove;
		rec.remove;
		localknob.remove;
	};
	destroy;

};

/////////////////////////////////////////////////////////////////////////
/////////		main
/////////////////////////////////////////////////////////////////////////

~make_editplayer_view = { arg parent, editplayer, player, param_order;
	var midi;
	var width = 1200;
	var height = 800;
	var row_layout, col_layout, info_layout;
	var sc_ep, ep_messages;

	col_layout = GUI.hLayoutView.new(parent, Rect(0,0,width+10,height));
	info_layout = GUI.vLayoutView.new(col_layout, Rect(0,0,300,height));
	row_layout = GUI.vLayoutView.new(col_layout, Rect(0,0,width-200,height));
	row_layout.background = ~editplayer_color_scheme.background;


	ep_messages = Dictionary.newFrom((
		paramlist: { arg self;
			
			~midi_interface.clear_assigned(\slider);
			~midi_interface.clear_assigned(\knob);

			CCResponder.removeAll; // FIXME: must not remove other useful CC

			info_layout.removeAll;
			row_layout.removeAll;

			editplayer.get_paramlist.debug("BEGIN paramlist update");
			editplayer.get_paramlist.do { arg param_name, i;
				var param = player.get_arg(param_name);
				param_name.debug("creation");
				case
					{ [\adsr].includes(param_name) || param_name.asString.containsStringAt(0,"adsr_") } {
						midi = ~midi_interface.assign_adsr(param);
						param.midi = midi;
						~make_env_control_view.(row_layout, player, param);
					}
					{ param_name == \noteline } {
						if(player.noteline, {
							midi = ~piano_recorder.(player);
							param.midi = midi;
							~make_noteline_view.(row_layout, editplayer.make_param_display(param), param);
						});
					}
					{ [\dur].includes(param_name) } {
						if(player.noteline.not, {
							midi = nil;
							param.midi = midi;
							~make_simple_control_view.(info_layout, editplayer.make_param_display(param), param, midi);
						})
					}
					{ [\bufnum].includes(param_name)|| param_name.asString.containsStringAt(0,"bufnum_") } {
						midi = nil;
						param.midi = midi;
						~make_bufnum_view.(info_layout, editplayer.make_param_display(param), param, midi);
					}
					{ [\segdur, \stretchdur].includes(param_name) } {
						if(player.noteline, {
							midi = nil;
							param.midi = midi;
							~make_simple_control_view.(info_layout, editplayer.make_param_display(param), param, midi);
						});
					}
					{ [\stepline].includes(param_name) } {
						if(player.noteline.not, {
							midi = nil;
							param.midi = midi;
							~make_control_view.(row_layout, editplayer.make_param_display(param), param, midi);
						});
					}
					{ [\legato, \amp, \attack, \release, \sustain].includes(param_name)} {
						"argggg".debug;
						midi = ~midi_interface.assign_first(\slider, param);
						"argggg".debug;
						param.midi = midi;
						"argggg".debug;
						~make_control_view.(info_layout, editplayer.make_param_display(param), param, midi);
						"argggg".debug;
					}
					{ true } {
						param.debug("C4ESTQUOILEBNPROGKF");
						midi = ~midi_interface.assign_first(\knob, param);
						param.midi = midi;
						~make_control_view.(row_layout, editplayer.make_param_display(param), param, midi);
					};
			};

		}
	));

	sc_ep = SimpleController(editplayer);
	~init_controller.(sc_ep, ep_messages);

	editplayer.refresh;

	col_layout.onClose = {
		sc_ep.remove;
	};

};

~make_editplayer = { arg main, parent;
	var ep, player;
	"============making editplayer".debug;
	player = main.panels.parlive.get_selected_cell;
	player.debug("editplayer:player");

	ep = (
		player: player,
		model: (
				param_field_group: List[\dur, \segdur, \stretchdur],
				param_status_group: List[\amp, \dur, \segdur, \stretchdur, \legato, \attack, \sustain, \release],
				param_order: List[\stepline, \noteline, \adsr, \freq],
				param_reject: [\out, \instrument, \type, \gate, \agate, \t_trig],
				max_cells: 8,
				selected_param: 0

		),

		make_param_display: { arg self, param;
			(
				get_bank: { arg self;
					player.get_bank;
				},
				selected: { arg self;
					param.selected;
				},
				max_cells: { arg self;
					ep.model.max_cells;	
				},
				get_selected_cell: {
					param.get_selected_cell;
				},
				name: { arg self;
					param.name
				},
				show_midibloc: true,
				width: 200,
				height: 30,
				name_width: { arg self;
					50;
				}
			);
		},

		refresh: { arg self;
			"BEGIN REFRESH".debug;
			self.changed(\paramlist);
		},

		get_paramlist: { arg self;
			var po = self.model.param_order;
			if(player.noteline, {
				po.reject({ arg x; [\dur, \stepline].includes(x) });
			}, {
				po.reject({ arg x; [\segdur, \stretchdur, \noteline].includes(x) });
			});
		},

		get_param_offset: { arg self;
			if(player.noteline, {
				self.get_paramlist.indexOf(\noteline);
			},{
				self.get_paramlist.indexOf(\stepline);
			});
		},

		controller: { arg editplayer; (

			get_param_at: { arg self, idx;
				player.get_arg( editplayer.get_paramlist[idx] )
			},

			get_selected_param: { arg self;
				self.get_param_at( editplayer.model.selected_param )
			},

			// control API

			select_param: { arg self, idx;
				var param;
				editplayer.model.param_order.debug("editplayer.select_param param_order");
				if(idx < (editplayer.get_paramlist.size), { 
					param = self.get_param_at(idx);
					self.get_selected_param.deselect_param.();
					param.select_param.();
					editplayer.model.selected_param = idx;
				}, {
					idx.debug("selected param out of range");
				});
			},

			select_cell: { arg self, idx;
				var sel, dur;
				sel = self.get_selected_param.();
				switch( sel.classtype,
					\stepline, {
						sel.toggle_cell(idx);
					}, 
					\noteline, {
						dur = player.get_arg(\dur).preset;
						sel.set_start_offset(idx*dur.val[dur.selected_cell]);
					},
					\control, {
						sel.select_cell(idx);
					}
				);
			},

			set_value: { arg self, val;
				self.get_selected_param.set_value(val);
			},

			add_cell_bar: { arg self;
				var bar_length = 4;
				var dur;
				var par = self.get_selected_param;
				var defval = self.get_selected_param.default_val;

				self.get_selected_param.debug("editplayer.controller.add_cell_bar get sel param");

				switch(par.classtype,
					\stepline, {
						self.get_selected_param.add_cells( par.default_val[..(bar_length-1)]);
					},
					\noteline, {
						dur = player.get_arg(\dur).preset;
						par.set_end_offset(par.get_end_offset+dur.val[dur.selected_cell]);
						par.get_end_offset.debug("set end offset");
					},
					\control, {
						self.get_selected_param.add_cells( par.default_val ! bar_length);
					}
				);
			},

			remove_cell_bar: { arg self;
				var bar_length = 4, dur;
				var par = self.get_selected_param;
				switch(par.classtype,
					\stepline, {
						self.get_selected_param.remove_cells(bar_length);
					},
					\noteline, {
						dur = player.get_arg(\dur).preset;
						par.set_end_offset(par.get_end_offset-dur.val[dur.selected_cell]);
					},
					\control, {
						self.get_selected_param.remove_cells(bar_length);
					}
				);
			},

			change_kind: { arg self, kind;
				var param = self.get_selected_param;
				if(param.classtype == \control, {
					self.get_selected_param.change_kind(kind);
				});
			};


		)},



		init: { arg editplayer, player, parent;
			var param_order = editplayer.model.param_status_group ++ editplayer.model.param_order, selected;
			"========== init editplayer ============".debug;

			// param order
			param_order = param_order.asList;

			param_order.dump.debug("init param_order");
			player.get_args.debug("init get_args");

			player.get_args.do { arg key;
				key.dump.debug("key");
				if(param_order.includes(key).not, {
					param_order.add(key);
					key.debug("adding key");
				})
			};
			param_order.dump.debug("init param_order2");
			param_order.deepCopy.do { arg key; if(player.get_args.includes(key).not, { param_order.remove(key) }) };
			param_order.dump.debug("init param_order3");
			param_order = param_order.reject({ arg x; editplayer.model.param_reject.includes(x) });
			param_order.dump.debug("init param_order4");
			
			editplayer.model.param_order = param_order;

			editplayer.model.param_order.debug("fin init param_order");

			//player.set_noteline(true);




			// select param

			selected = param_order.do.detectIndex { arg i; player.get_arg(i).selected == 1 };
			editplayer.controller.select_param(selected ?? editplayer.get_param_offset);

			// midi keys

			~make_midi_kb_control.(player, editplayer);

			// kb shotcuts

			main.commands.matrix_add_enable([\parlive, \select_cell], [\kb, 0], ~keycode.kbpad8x4, { arg x, y; editplayer.set_selection(x,y) });
			main.commands.array_add_enable([\parlive, \change_bank], [\kb, 0], ~keycode.kbnumpad, { arg x; editplayer.set_bank(x) });
			main.commands.add_enable([\parlive, \load_libnode], [\kb, ~keycode.mod.fx, ~keycode.kbfx[0]], { editplayer.load_libnode });

			main.commands.array_add_enable([\editplayer, \select_cell], [\kb, 0], ~keycode.kbnumline, { arg i;
				editplayer.controller.select_cell((player.get_bank*editplayer.model.max_cells)+i) 
			});

			// edit value mode

			main.commands.add_enable([\editplayer, \edit_value], [\kb, 0, ~keycode.kbspecial.enter], { 
				var sel = editplayer.controller.get_selected_param;
				sel.name.debug("voici le nom!");
				case
					{ editplayer.model.param_field_group.includes(sel.name)} {
						~set_numpad_mode.(main.commands, { arg val; 
							if(val.interpret.isNumber, { sel.set_val(val.interpret); sel.changed(\cells) });
						});

					}
					{ sel.name == \bufnum || sel.name.asString.containsStringAt(0,"bufnum_")} {
						~choose_sample.(main, { arg buf, w; sel.set_val(buf); w.debug("window").close  })
					};
			});
			main.commands.array_set_shortcut([\editplayer, \edit_value_mode, \insert_number], [\kb, 0], ~keycode.kbnumpad);
			main.commands.set_shortcut([\editplayer, \edit_value_mode, \insert_point], [\kb, 0, ~keycode.kbspecial.point]);
			main.commands.set_shortcut([\editplayer, \edit_value_mode, \cancel], [\kb, 0, ~keycode.kbspecial.escape]);
			main.commands.set_shortcut([\editplayer, \edit_value_mode, \ok], [\kb, 0, ~keycode.kbspecial.enter]);

			//

			main.commands.array_add_enable([\editplayer, \select_param], [\kb, ~keycode.mod.ctrl], ~keycode.kbnumline, { arg i;
				editplayer.controller.select_param(i);
			});

			main.commands.array_add_enable([\editplayer, \select_simple_param], [\kb, ~keycode.mod.alt], ~keycode.kbnumline, { arg i;
				editplayer.controller.select_param(i+editplayer.get_param_offset)
			});

			main.commands.add_enable([\editplayer, \set_seq_kind], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["a"]], {
				editplayer.controller.change_kind(\seq)
			});
			main.commands.add_enable([\editplayer, \set_scalar_kind], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["s"]], {
				editplayer.controller.change_kind(\scalar)
			});

			// noteline

			main.commands.add_enable([\editplayer, \set_noteline_mode], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["n"]], {
				player.set_noteline(true);
				editplayer.changed(\paramlist);
			});

			main.commands.add_enable([\editplayer, \unset_noteline_mode], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["b"]], {
				player.set_noteline(false);
				editplayer.changed(\paramlist);
			});
			
			//~prec = ~piano_recorder.value(player); // debile

			main.commands.add_enable([\editplayer, \start_recording], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["r"]], {
				"STARTRECORD!!".debug; 
				player.get_arg(\noteline).midi.start_recording.();
				player.get_arg(\noteline).midi.changed(\recording); 
			});

			main.commands.add_enable([\editplayer, \stop_recording], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["t"]], {
				var sum = 0;
				var midi = player.get_arg(\noteline).midi;
				midi.stop_recording.();
				midi.changed(\recording);
				midi.track.debug("66666666666666666666666666666- this is record!!!!");

				midi.track.do { arg no;
					sum = sum + no.dur;
				};
				sum.debug("total sum");
				player.get_arg(\noteline).notes = midi.track;
				player.get_arg(\noteline).changed(\notes);
			});

			// cells 

			main.commands.add_enable([\editplayer, \add_cell_bar], [\kb, 0, ~keycode.numpad.plus], {
				editplayer.controller.add_cell_bar.() 
			});
			main.commands.add_enable([\editplayer, \remove_cell_bar], [\kb, ~keycode.mod.ctrl, ~keycode.numpad.plus], {
				editplayer.controller.remove_cell_bar.() 
			});

			main.commands.array_add_enable([\editplayer, \change_bank], [\kb, 0], ~keycode.kbnumpad, { arg idx; player.set_bank(idx) });

			// playing

			main.commands.add_enable([\editplayer, \play_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[4]], { player.node.play });
			main.commands.add_enable([\editplayer, \stop_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[5]], { player.node.stop });

			// change panel

			main.commands.add_enable([\editplayer, \show_panel, \parlive], [\kb, ~keycode.mod.fx, ~keycode.kbfx[8]], { main.show_panel(\parlive) });
			main.commands.add_enable([\editplayer, \show_panel, \mixer], [\kb, ~keycode.mod.fx, ~keycode.kbfx[9]], { main.show_panel(\mixer) });
			main.commands.add_enable([\editplayer, \show_panel, \score], [\kb, ~keycode.mod.fx, ~keycode.kbfx[10]], { main.show_panel(\score) });

			// make view

			~make_editplayer_view.(parent, editplayer, player, param_order);
		}
	);
	ep.init(player, parent);
	ep;

};

)
