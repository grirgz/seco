(
~make_name_button = { arg parent, label;
	var bt;
	bt = GUI.button.new(parent, Rect(50,50,50,50));
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

~make_val_button = { arg parent, label;
	var bt;
	bt = GUI.button.new(parent, Rect(0,0,60,50));
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

~init_controller = { arg controller, messages;
	messages.keysValuesDo { arg key, val; controller.put(key, val) };
};


~pretty_print_freq = { arg freq;


};

~make_control_view = { arg parent, player, param, midi;
	var param_messages,
		midi_messages,
		row_layout,
		bloc,
		midibloc,
		btl_cells,
		bt_name,
		slider,
		new_cell,
		width = 1200;
	var txt_midi_label, txt_midi_val;
	var sc_param, sc_midi;
	var max_cells = 8; // FIXME: already defined in editplayer
	var inrange;

	row_layout = GUI.hLayoutView.new(parent, Rect(0,0,(width+10),60));
	row_layout.background = Color.rand;

	bt_name = ~make_name_button.(row_layout, param.name);

	bloc = GUI.vLayoutView.new(row_layout, Rect(0,0,150,60));
	midibloc = GUI.hLayoutView.new(bloc, Rect(0,0,150,30));
	txt_midi_label = GUI.staticText.new(midibloc, Rect(0,0,75,30));
	txt_midi_val = GUI.staticText.new(midibloc, Rect(0,0,75,30));

	slider = GUI.slider.new(bloc, Rect(0,0,100,30));

	btl_cells = GUI.hLayoutView.new(row_layout, Rect(0,0,width,30));

	inrange = { arg sel=nil;
		var start;
		start = max_cells * player.get_bank.();
		sel = sel ?? param.get_selected_cell;
		start.debug("inrange====");
		sel.debug("inrange");
		(sel >= start && (sel < (start+max_cells))).debug("inrange");
		(sel >= start && (sel < (start+max_cells)))
	};

	param_messages = Dictionary.newFrom((
		selected: { arg self;
			bt_name.value = self.selected;
		},

		selected_cell: { arg self, msg, oldsel;
			
			if(oldsel.notNil && inrange.(oldsel), {
				btl_cells.children[ oldsel % max_cells ].value = 0;
			});
			if(inrange.(), {
				btl_cells.children[ self.get_selected_cell % max_cells ].value = 1;
			});
		},

		val: { arg self, msg, cellidx;
			var newval;
			self.get_cells[cellidx].debug("val handler: cellidx");
			newval = self.get_cells[cellidx];
			~change_button_label.(btl_cells.children[ cellidx % max_cells ], newval);
			//self.debug("val changed");
			if(self.classtype == \stepline, { 
				btl_cells.children[ cellidx % max_cells ].value = newval
			},{
				slider.value = self.spec.unmap(newval);
			});
			//btl_cells.children[ cellidx ].states.debug("======heeeeerreeeee");
			//btl_cells.children[ cellidx ] = newval;
		},

		cells: { arg self; 
			var cells, bank, start, range, sel;
			"cells removeAll===================".debug;
			btl_cells.removeAll;
			"END cells removeAll===================".debug;

			bank = player.get_bank.();

			cells = self.get_cells.();
			cells.debug("cellls============");
			start = max_cells * bank;
			range = (start..((start+max_cells)-1));
			range.debug("cells");
			cells[ start..((start+max_cells)-1) ].debug("cells");
			cells[ start..((start+max_cells)-1) ].do { arg cell, i;
				~make_val_button.(btl_cells, cell);
				if(self.classtype== \stepline, {
					btl_cells.children[i].value = cell
				});
			};
			if(self.classtype == \control, {
				sel = self.get_selected_cell;
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
			txt_midi_val.background = if(self.blocked == \not, { Color.green }, { Color.red });
		},
		recording: { arg self;
			txt_midi_label.background = if(self.recording, { Color.red }, { Color.green(alpha:1) });
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

	// remove func

	row_layout.onClose = {
		param.name.debug("=========== view closed");
		sc_param.remove;
		if(midi.notNil, { sc_midi.remove });
	};
};

~make_editplayer_view = { arg parent, player, param_order;
	var midi;
	var width = 1200;
	var row_layout;

	row_layout = GUI.vLayoutView.new(parent, Rect(0,0,(width+10),800));
	row_layout.background = Color.blue;

	~midi_interface.clear_assigned(\slider);
	~midi_interface.clear_assigned(\knob);

	CCResponder.removeAll; // FIXME: must not remove other useful CC

	param_order.do { arg param_name, i;
		var param = player.get_arg(param_name);
		if([\instrument, \type, \out, \agate].includes(param_name), {
			// skip
		}, {
			case
				{ [\stepline].includes(param_name) } {
					midi = nil;
				}
				{ [\legato, \amp, \dur].includes(param_name)} {
					midi = ~midi_interface.assign_first(\slider, param);
				}
				{ true } {
					midi = ~midi_interface.assign_first(\knob, param);
				};
			param.midi = midi;
			~make_control_view.(row_layout, player, param, midi);
		});
	};

};

~midi_interface = (
	next_free: (
		slider: 0,
		knob: 0
	),
	registry: List[],

	cc_val: Dictionary[
		[\slider, 0] -> 0

	],

	assign_first: { arg self, kind, param;
		var mc;
		mc = ~make_midi_control.([kind, self.next_free[kind]], self.cc_val, param);
		self.registry.add( mc );
		self.next_free[kind] = self.next_free[kind] + 1;
		mc;
	},

	clear_assigned: { arg self, kind;
		self.next_free[kind] = 0;
		self.registry.copy.do { arg mc;
			if(mc.kind == kind, {
				mc.destroy;
				self.registry.remove(mc);
			});
		};
	}

);


~make_midi_control = { arg ccid, cc_val, param; 
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
				self.label = (ccid[0] ++ (ccid[1]+1)).asSymbol;
				self.changed(\label);
				self.ccresp = CCResponder({ |src,chan,num,value|
						//[src,chan,num,value].debug("==============CCResponder");
						self.set_value(value/127);
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
	sc_param.put(\selected_cell, { arg obj; midi.block.() });


	midi;

};

~make_midi_kb_control = { arg player, editplayer;
	var nonr, noffr, param, destroy, rec;
	
	// init
	NoteOnResponder.removeAll; // TODO: must save others responders ?

	nonr = NoteOnResponder { arg src, chan, num, veloc;
		param = editplayer.controller.get_selected_param.();
		param.set_val(num.midicps);
	};

	rec = CCResponder({ |src,chan,num,value|
			var param = editplayer.controller.get_selected_param;
			var stepline = player.get_arg(\stepline);
			if(param.midi.notNil && param.midi.recording.notNil, {
				if(param.midi.recording, {
					param.midi.recording = false;
					param.midi.changed(\recording);
					stepline.tick = { "TICK!!!".postln; };
				}, {
					param.midi.recording = true;
					param.midi.changed(\recording);
					stepline.tick = { arg obj, idx;
						param.midi.get_midi_val.debug("set TICK");
						param.seq.set_norm_val(idx, param.midi.get_midi_val);
					};
				});
			});
		},
		nil, // any source
		nil, // any channel
		~cakewalk.button[7], // record button
		nil // any value
	);

	destroy = { 
		nonr.remove;
		rec.remove;
	};
	destroy;

};

~make_editplayer = { arg player, parent, kb_handler;
	var ep;
	"============making editplayer".debug;
	ep = (
		model: (
				param_order: List[\amp, \stepline, \freq, \dur, \legato],
				max_cells: 8,
				selected_param: 0

		),


		controller: { arg editplayer; (

			get_param_at: { arg self, idx;
				player.get_arg( editplayer.model.param_order[idx] )
			},

			get_selected_param: { arg self;
				self.get_param_at( editplayer.model.selected_param )
			},

			// control API

			select_param: { arg self, idx;
				var param;
				editplayer.model.param_order.debug("editplayer.select_param param_order");
				if(idx < (editplayer.model.param_order.size), { 
					param = self.get_param_at(idx);
					self.get_selected_param.deselect_param.();
					param.select_param.();
					editplayer.model.selected_param = idx;
				}, {
					idx.debug("selected param out of range");
				});
			},

			select_cell: { arg self, idx;
				var sel;
				sel = self.get_selected_param.();
				if( sel.classtype == \stepline, {
					sel.toggle_cell(idx);
				}, {
					sel.select_cell(idx);
				});
			},

			set_value: { arg self, val;
				self.get_selected_param.set_value(val);
			},

			add_cell_bar: { arg self;
				var bar_length = 4;
				var par = self.get_selected_param;
				var defval = self.get_selected_param.default_val;

				self.get_selected_param.debug("editplayer.controller.add_cell_bar get sel param");

				if(par.classtype == \stepline,{
					self.get_selected_param.add_cells( par.default_val[..(bar_length-1)]);
				},{
					self.get_selected_param.add_cells( par.default_val ! bar_length);
				});
			},

			remove_cell_bar: { arg self;
				var bar_length = 4;
				self.get_selected_param.remove_cells(bar_length);
			},

			change_kind: { arg self, kind;
				var param = self.get_selected_param;
				if(param.classtype == \control, {
					self.get_selected_param.change_kind(kind);
				});
			};


		)},


		init: { arg editplayer, player, parent, kb_handler;
			var param_order = editplayer.model.param_order.dump, selected;
			"========== init editplayer ============".debug;

			// param order

			param_order.dump.debug("init param_order");
			player.get_args.do { arg key; if(param_order.includes(key).not, { param_order.add(key) }) };
			param_order.deepCopy.do { arg key; if(player.get_args.includes(key).not, { param_order.remove(key) }) };
			param_order.debug("init done param_order");
			param_order = param_order.reject({ arg x; [\out, \instrument, \type, \gate].includes(x) });
			param_order.dump.debug("init done2 param_order");
			editplayer.model.param_order.debug("init abs param_order");
			editplayer.model.param_order = param_order;
			editplayer.model.param_order.debug("init abs param_order");
			editplayer.model.param_order = param_order;
			editplayer.model.param_order.dump.debug("init abs param_order");
		

			~make_editplayer_view.(parent, player, param_order);

			// select param

			selected = param_order.do.detectIndex { arg i; player.get_arg(i).selected == 1 };
			editplayer.controller.select_param(selected ?? 1);

			// midi keys

			~make_midi_kb_control.(player, editplayer);

			// kb shotcuts

			~kbnumline.do { arg kc, i; kb_handler[[0, kc]] = { 
				editplayer.controller.select_cell((player.get_bank*editplayer.model.max_cells)+i) 
			} };
			~kbnumline.do { arg kc, i; kb_handler[[~modifiers.alt, kc]] = { editplayer.controller.select_param(i) } };
			kb_handler[[~modifiers.alt, ~kbaalphanum["a"]]] = { editplayer.controller.change_kind(\seq) };
			kb_handler[[~modifiers.alt, ~kbaalphanum["s"]]] = { editplayer.controller.change_kind(\scalar) };

			kb_handler[[0, ~numpad.plus]] = { editplayer.controller.add_cell_bar.() };
			kb_handler[[~modifiers.ctrl, ~numpad.plus]] = { editplayer.controller.remove_cell_bar.() };

			~kbnumpad.do { arg keycode, idx;
				kb_handler[[0, keycode]] = { player.set_bank(idx) };
			};

		}
	);
	ep.init(player, parent, kb_handler);
	ep;

};

)

