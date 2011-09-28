(

~seq_view = { arg parent, seq, paramlist;
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
				var player_name = paramasso.key;
				var param = paramasso.value;


				player = seq.get_player_from_name(player_name);

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

~make_seq = { arg main, parent, kb_handler;

	var seq;

	seq = (

		model: (
			param_offset: 0@0,
			max_cells: 32,
			selected_param: main.state.panel.seqpanel.selected_player_idx;
		),

		make_param_display: { arg self, param, idx;
			(
				get_bank: { arg self;
					main.state.panel.seqpanel.bank
				},
				selected: { arg self;
					if(idx.debug("idx") == seq.model.selected_param.debug("sel"), {1}, {0});
				},
				max_cells: { arg self;
					seq.model.max_cells;	
				},
				get_selected_cell: {
					param.get_selected_cell;
				},
				show_midibloc: false,
				name: { arg self;
					var pname;
					pname.debug("pname");
					param.dump.debug("param");
					pname = seq.get_paramlist[idx].key;
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
			var pname;
			pname = self.get_paramlist[seq.model.selected_param].key;
			main.model.livenodepool[pname];
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
				main.state.panel.seqpanel.selected_player = self.get_selected_player;
				main.state.panel.seqpanel.selected_player_idx = idx;
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
			main.model.parlive.sortedKeysValuesDo( { arg key, val;
				if( ~compare_point.(key, offset).not, {
					val.data.do { arg pname;
						if(pname != 0, {
							player_instance = main.model.livenodepool[pname];
							if( player_instance.noteline, {
								list.add(pname -> player_instance.get_arg(\noteline));
							},{
								list.add(pname -> player_instance.get_arg(\stepline));
							});
						});
					};
				})
			}, ~compare_point);
			list.do{ arg i; i.dump.debug("get_paramlist list") };
			list;
		},

		get_bank: { arg self;
			main.state.panel.seqpanel.bank
		},

		set_bank: { arg self, bank;
			var player_instance;
			main.state.panel.seqpanel.bank = bank;
			self.get_paramlist.do { arg asso;
				player_instance = main.model.livenodepool[asso.key];
				player_instance.set_bank(bank);
			};
			main.state.panel.seqpanel.bank = bank;
		},

		refresh: { arg self;
			self.changed(\paramlist);
		},

		init: { arg self;
		
			main.state.panel.seqpanel.selected_player = self.get_selected_player;

			~kbnumpad.do { arg kc, i; kb_handler[[0, kc]] = { 
				self.model.param_offset = 0@i;
				self.changed(\paramlist);
			}};

			~kbpad8x4_flat.do { arg kc, i; kb_handler[[0, kc]] = { 
				seq.select_cell((self.get_bank*self.model.max_cells)+i) 
			} };

			~kbnumline.do { arg kc, i; kb_handler[[~modifiers.alt, kc]] = { self.select_param(i) } };


			kb_handler[[~modifiers.alt, ~kbaalphanum["n"]]] = { 
				self.get_selected_player.set_noteline(true);
				self.changed(\paramlist);
			};
			kb_handler[[~modifiers.alt, ~kbaalphanum["b"]]] = { 
				self.get_selected_player.set_noteline(false);
				self.changed(\paramlist);
			};
			

			kb_handler[[~modifiers.alt, ~kbaalphanum["r"]]] = {
				var midi;
				"STARTRECORD!!".debug; 
				midi = self.get_selected_param.midi;
				if(midi.notNil, {
					midi.start_recording.();
					midi.changed(\recording); 
				});
			};
			kb_handler[[~modifiers.alt, ~kbaalphanum["t"]]] = { 
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
			};
			// playing

			kb_handler[ [~modifiers.fx, ~kbfx[4]] ] = { self.get_selected_player.node.play };
			kb_handler[ [~modifiers.fx, ~kbfx[5]] ] = { self.get_selected_player.node.stop };

			// cells 

			kb_handler[[0, ~numpad.plus]] = { seq.add_cell_bar.() };
			kb_handler[[~modifiers.ctrl, ~numpad.plus]] = { seq.remove_cell_bar.() };

			~kbnumpad.do { arg keycode, idx;
				kb_handler[[0, keycode]] = { self.set_bank(idx) };
			};

			// make view

			~seq_view.(parent, seq);
		}

	);
	seq.init;
	seq;


};


)
