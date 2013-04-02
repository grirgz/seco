(

~score_view = { arg parent, seq, paramlist;
	var midi;
	var width = 1200;
	var row_layout;
	var sc_seq;
	var seq_messages;

	row_layout = GUI.vLayoutView.new(parent, Rect(0,0,(width+10),800));
	row_layout.background = ~editplayer_color_scheme.background;

	~midi_interface.clear_assigned(\slider);
	~midi_interface.clear_assigned(\knob);

	CCResponder.removeAll; // FIXME: must not remove other useful CCResponders

	seq_messages = Dictionary.newFrom((
		paramlist: { arg self;
			var player;
			var master;
			var midi;

			row_layout.removeAll;

			"hah33333333ahahahjkkj".debug;
			self.get_paramlist.do { arg paramasso, i;
				var hihi1 = \bla;
				var player_name = paramasso.key.name;
				var param = paramasso.value;
				paramasso.key.name.debug("=======paramasso");
				paramasso.value.name.debug("=======paramasso val");


				player = paramasso.key;

				if(param.name == \noteline, {
					midi = ~piano_recorder.(player);
					param.midi = midi;
					~make_noteline_view.(row_layout, seq.make_param_display(param, i), param);
				},{
					param.midi = nil;
					"hahahahahjkkj".debug;
					~make_stepline_view.(row_layout, seq.make_param_display(param, i), param, midi);
				});

			};
		}
	));

	sc_seq = SimpleController(seq);
	~init_controller.(sc_seq, seq_messages);
	seq.refresh.();

	// remove func

	row_layout.onClose = {
		sc_seq.remove;
	};

};

~make_score = { arg main, parent, kb_handler;

	var seq;

	seq = (

		model: (
			param_offset: 0@0,
			max_cells: 32,
			bank:0,
			selected_param: 0; //FIXME: load it from main
		),

		make_param_display: { arg parentself, param, idx;
			(
				get_bank: { arg self;
					main.context.get_selected_bank
				},
				get_player_bank: { arg self;
					parentself.model.bank;
				},
				selected: { arg self;
					if(idx.debug("idx") == seq.model.selected_param.debug("sel"), {1}, {0});
				},
				max_cells: { arg self;
					seq.model.max_cells;	
				},
				get_selected_cell: { arg self;
					param.get_selected_cell;
				},
				show_midibloc: false,
				name: { arg self;
					var pname;
					pname.debug("pname");
					param.dump.debug("param");
					pname = seq.get_paramlist[idx].key.name;
					pname.debug("pname");
				},
				noteline_numbeats: 32,
				cell_width: 30,
				width: 200,
				height: 30,
				name_width: { arg self;
					80;
				}
			);
		},

		get_param_at: { arg self, idx;
			self.get_paramlist[idx].value;
		},

		get_selected_param: { arg self;
			self.get_param_at( seq.model.selected_param )
		},

		get_selected_player: { arg self;
			self.get_paramlist[seq.model.selected_param].key;
		},

		get_player_from_name: { arg self, name;
			main.model.livenodepool[name];
		},

		// control API

		select_param: { arg self, idx;
			var param, oldparam;
			if(idx < (self.get_paramlist.size), { 
				oldparam = self.get_param_at(seq.model.selected_param);
				param = self.get_param_at(idx);
				seq.model.selected_param = idx;
				//main.state.panel.seqpanel.selected_player = self.get_selected_player;
				//main.state.panel.seqpanel.selected_player_idx = idx;
				oldparam.changed(\selected);
				param.changed(\selected);
			}, {
				idx.debug("selected param out of range");
			});
		},

		get_offset_amount: { arg self;
			var dur;
			dur = self.get_selected_player.get_arg(\dur).preset;
			dur.val[dur.selected_cell];
		},

		select_cell: { arg self, idx;
			var sel, dur, player;
			sel = self.get_selected_param.();
			player = self.get_selected_player;
			switch( sel.classtype,
				\stepline, {
					sel.toggle_cell(idx);
				}, 
				\noteline, {
					sel.set_start_offset(idx*self.get_offset_amount);
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
					self.get_selected_param.add_cells( self.get_selected_param.get_cells.deepCopy);
				},
				\noteline, {
					par.set_end_offset(par.get_end_offset+self.get_offset_amount);
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
					par.set_end_offset(par.get_end_offset-self.get_offset_amount);
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
		},

		get_paramlist: { arg self, offset=nil;
			var list = List.new;
			var player_instance;
			offset = offset ?? self.model.param_offset;
			main.context.get_selected_node_set[offset.y..].do( { arg nodegroup;
				if (nodegroup.name != \void_FIXME) { // FIXME: required ?
					nodegroup.children.do { arg nodename;
						var node;
						if(nodename != \voidplayer, {
							//node.debug("get_paramlist:node");
							node = main.get_node(nodename);
							if(node.kind == \player) {
								list.add(node -> node.get_arg(node.get_mode));
							}
						});
					};
				}
			});
			//list.do{ arg i; i.dump.debug("get_paramlist list") };
			list;
		},

		update_title: { arg self;
			main.set_window_title("score: bank:"++  self.get_bank ++"; player bank:"++ self.get_player_bank ++ "; offset:" ++ self.model.param_offset.y);
		},

		get_bank: { arg self;
			main.context.get_selected_bank
		},

		get_player_bank: { arg self;
			self.model.bank;
		},
		
		set_player_bank: { arg self, bank;
			self.model.bank = bank;
			self.get_paramlist.do { arg asso;
				asso.value.changed(\cells);
			};
			self.update_title;
		},

		set_bank: { arg self, bank;
			//FIXME: this function is obsolete
			var player_instance;
			//main.state.panel.seqpanel.bank = bank;
			self.get_paramlist.do { arg asso;
				player_instance = asso.key;
				player_instance.set_bank(bank);
			};
			//main.state.panel.seqpanel.bank = bank;
		},

		refresh: { arg self;
			self.update_title;
			self.changed(\paramlist);
		},

		init: { arg self;
		
			//main.state.panel.seqpanel.selected_player = self.get_selected_player;

			main.commands.array_add_enable([\score, \select_offset], [\kb, ~keycode.mod.ctrl], ~keycode.kbnumpad, { arg x; 
				self.model.param_offset = 0@x;
				self.update_title;
				self.changed(\paramlist);
			});

			main.commands.array_add_enable([\score, \select_cell], [\kb, 0], ~keycode.kbpad8x4_flat, { arg i; 
				seq.select_cell((self.get_player_bank*self.model.max_cells)+i) 
			});

			main.commands.array_add_enable([\score, \select_param], [\kb, ~keycode.mod.alt], ~keycode.kbnumline, { arg i; self.select_param(i) });

			main.commands.add_enable([\score, \set_noteline_mode], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["n"]], {
				self.get_selected_player.set_mode(\noteline);
				self.changed(\paramlist);
			});

			main.commands.add_enable([\score, \set_stepline_mode], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["b"]], {
				self.get_selected_player.set_mode(\stepline);
				self.changed(\paramlist);
			});

			main.commands.add_enable([\score, \start_recording], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["r"]], {
				var midi;
				"STARTRECORD!!".debug; 
				midi = self.get_selected_param.midi;
				if(midi.notNil, {
					midi.start_recording.();
					midi.changed(\recording); 
				});
			});

			main.commands.add_enable([\score, \stop_recording], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["t"]], {
				var sum = 0;
				var sel = self.get_selected_param;
				var midi = sel.midi;
				if(midi.notNil, {
					midi.stop_recording.();
					midi.changed(\recording);
					midi.track.debug("66666666666666666666666666666- this is record!!!!");

					midi.track.do { arg no;
						sum = sum + no.dur;
					};
					sum.debug("total sum");
					sel.notes = midi.track;
					sel.changed(\notes);
				})
			});
			// playing

			main.commands.add_enable([\score, \play_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[4]], { self.get_selected_player.node.play });
			main.commands.add_enable([\score, \stop_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[5]], { self.get_selected_player.node.stop });

			// cells 

			main.commands.add_enable([\score, \add_cell_bar], [\kb, 0, ~keycode.numpad.plus], { seq.add_cell_bar.() });
			main.commands.add_enable([\score, \remove_cell_bar], [\kb, ~keycode.mod.ctrl, ~keycode.numpad.plus], { seq.remove_cell_bar.() });

			main.commands.add_enable([\score, \remove_cell_bar], [\kb, ~keycode.mod.ctrl, ~keycode.numpad.plus], { seq.remove_cell_bar.() });

			main.commands.array_add_enable([\score, \change_bank], [\kb, 0], ~keycode.kbnumpad, { arg idx; self.set_player_bank(idx); });

			// show panel

			~make_panel_shortcuts.(main, \score);

			// make view

			~score_view.(parent, seq);
		}

	);
	seq.init;
	seq;


};


)


