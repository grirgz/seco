~class_basic_player_view = (

	new: { arg self, player_display;
		var player;
		self = self.deepCopy;
		self.player_display = { player_display };

		self.make_layout;
		~make_class_responder.(self, self.param_group.layout, self.player_display, [
			\player
		]);
		self.player;
		//player = self.player_display.get_current_player;
		//self.player_responder = ~make_class_responder.(self, self.responder_anchor, player, [\mode], false);

		self;
	},

	selected_slot: { arg self;
		//self.modulation_ctrl.selected_slot.debug("class_modulator_body_basic: selected_slot: mod: selected_slot");
		//self.player_display.set_keydown_responder(\modulator);
	},

	player: { arg self;
		var player = self.player_display.get_current_player;
		player.uname.debug("class_basic_player_view: player responder");
		self.show_body_layout;
		if(player.notNil) {
			self.current_player = player;

			if(player.name != player.uname) {
				self.player_label.string_("% (%)".format(player.name, player.uname));
			} {
				self.player_label.string_(player.name);
			};

			self.player_responder.remove;
			self.player_responder = ~make_class_responder.(self, self.responder_anchor, player, [\mode], false);

		}

	},

	mode: { arg self;
		debug("class_basic_player_view: mode responder");
		self.update_param_group;
	},

	update_param_group: { arg self;
		var player = self.player_display.get_current_player;
		self.get_controller = { player };
		self.param_group.paramview_list.do { arg view, idx;
			var param_name;
			var param, display;
			param_name = self.player_display.get_param_name_by_display_idx(idx);
			param_name.debug("class_modulator_body_basic: set_controller: param_name");
			[self.player_display.current_player.uname, player.uname].debug("class_modulator_body_basic: set_controller: current_player, controller");
			if(param_name.notNil) {
				param = player.get_arg(param_name);
				display = self.player_display.make_param_display(param);
				view.set_param(param, display);
			} {
				view.clear_view;
			};
			//0.01.wait;
		};
	},

	switch_body_view: { arg self;
		self.show_custom_view = self.show_custom_view.not;
		self.show_body_layout;
	},

	show_body_layout: { arg self;
		var extplayer = self.player_display.get_current_player.external_player;
		Task{
			var extlayout;
			if(extplayer.notNil and: { self.show_custom_view }) {
				// FIXME: external player should have custom gui
				self.stack_layout.index = 1;
				extlayout = extplayer.make_layout;
				self.custom_view.children.do(_.remove);
				self.custom_view.layout = extlayout;
			} {
				self.update_param_group;
				self.stack_layout.index = 0;
			}
		}.play(AppClock)
	},

	make_layout: { arg self;

		self.player_label = StaticText.new;
		self.param_group = ~make_mini_param_group_widget.(nil, 4, (param_per_line:4));
		self.responder_anchor = self.param_group;
		self.param_group.debug("cassecouill");
		self.param_group_layout = VLayout.new;
		self.param_group_layout.add(self.param_group.layout);
		self.custom_view = View.new;
		self.stack_layout = StackLayout(
			View.new.layout_(self.param_group_layout).minHeight_(300),
			self.custom_view;
		);
		self.show_body_layout;
		self.layout = VLayout(
			self.player_label,
			self.stack_layout,
		);
		self.layout
	},

);


~class_player_display = (
	param_types: (
		//param_status_group: List[\amp, \dur, \segdur, \stretchdur, \repeat, \mbufnum, \bufnum, \samplekit],
		param_status_group: List[\amp, \dur, \velocity, \stretchdur, \repeat, \mbufnum, \bufnum, \samplekit],
		param_order: List[\sustain, \pan, \attack, \release, \adsr, \freq],
		param_mode: [\scoreline, \stepline, \noteline, \sampleline, \nodeline],
		param_kinds: [\scalar, \seq, \seg, \modulation, \scoreseq, \synchrone, \synchrone_rate, \bus, \recordbus, \pkey, \preset],
		param_no_midi: { arg self; self.param_field_group ++ [\mbufnum, \bufnum, \samplekit] ++ self.param_mode; },
		param_reject: { arg self; [\segdur, \out, \instrument, \tsustain, \type, \gate, \agate, \t_trig, \legato, \doneAction] ++ self.param_mode; },
		param_accepted_displayed_kind: { arg self; self.param_mode ++ [\control, \samplekit, \adsr, \buf, \array]},
		param_midi_reject: { arg self; Set.newFrom(self.param_reject ++ self.param_no_midi); },
		group_types: [\parnode, \seqnode],

		// deprecated
		param_field_group: List[\dur, \segdur, \stretchdur, \repeat],
		param_slider_group: List[\amp, \legato, \pan, \attack, \sustain, \release],
	),

	model: (
		//param_no_midi: param_types.param_no_midi,
		select_offset: 0,
		max_cells: 8,
		current_mode: \param,
		current_edit_mode: \param,
		colselect_mode: true,
		midi_knob_offset: 0
	),

	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		if(player.notNil) {
			self.set_current_player(player)
		};
		self.player_ctrl = { self.get_current_player };

		self;
	},

	get_current_player: { arg self;
		self.current_player;
	},

	set_current_player: { arg self, player, index;
		// set player object
		var oldplayer;
		[if(self.current_player.notNil) {self.current_player.uname}, player.uname].debug("XXXXX player_display: set_current_player: cur, new");
		if(self.current_player !== player) {
			//if(index.notNil) {
			//	self.get_current_group.select_child_at(index);
			//} {
			//	self.get_current_group.select_child(player.uname);
			//};
			//if(oldplayer.notNil) {
			//	main.freeze_do { oldplayer.get_arg(\amp).changed(\selected); };
			//};
			
			self.current_player = player;
			self.current_player_callback;
			self.current_player.uname.debug("set_current_player: player set");
			//self.assign_midi;
			//main.freeze_do { self.changed(\player); };
			self.changed(\player)
		}

	},

	get_selected_param: { arg self; 
		var player = self.get_current_player;
		var param_name = player.get_selected_param;
		player.get_arg(param_name);
	},

	select_param: { arg self, index;
		var oldsel, sel;
		var pl;
		var param;
		var player = self.get_current_player;
		var main = self.get_main;
		if(player.notNil) {
			player.uname.debug("side: select_param: player");
			//self.model.debug("c'est dingue!!!!");
			//oldsel = self.model.selected_param;
			sel = self.get_param_name_by_display_idx(index);
			//pl = self.get_paramlist_splited;
			//if(index < 8) {
			//	sel = pl[0][index];
			//} {
			//	sel = (pl[1]++pl[2])[index-8];
			//};
			if(sel.notNil) {
				player.select_param(sel);
				self.model.selected_param = player.get_arg(sel);
				~global_controller.current_param = self.model.selected_param;

				if(self.param_types.param_mode.includes(sel)) {
					"enable change_player_mode".debug;
					main.commands.enable_mode([\side, \change_player_mode]);
				} {
					"disable change_player_mode".debug;
					main.commands.disable_mode([\side, \change_player_mode]);
				};

				param = player.get_arg(sel);
				if(param.classtype == \adsr) {
					main.midi_center.assign_adsr(param);
				};
			} {
				index.debug("no param to select here");
			};
		} {
			debug("ERROR: side: select_param: current_player is nil");
		};

	},

	change_param_kind: { arg self, kind, param;
		param = param ?? self.get_selected_param;
		if(param.classtype == \control, {
			if(kind == \pkey) {
				param.set_pkey_mode(param.pkey_mode.not);
			} {
				param.change_kind(kind);
			};
			self.changed(\extparamlist);
		});
		if(param.classtype == \buf, {
			if(kind == \pkey) {
				param.set_pkey_mode(param.pkey_mode.not);
				self.changed(\extparamlist);
			};
		});
	},

	make_param_display: { arg editplayer, param, player=nil;
		player = player ?? editplayer.get_current_player;
		(
			extparam_content_size: 590@100,
			set_parent_group: { arg self, group;
				self.parent_group = group;
			},
			get_bank: { arg self;
				//player.get_bank.debug("display.get_bank");
				[player.uname, player.get_bank].debug("side: make_param_display: get bank");
				player.get_bank;
			},
			get_player_bank: { arg self;
				player.get_bank.debug("side: make_param_display: get player bank");
				player.get_bank;
			},
			selected: { arg self;
				if(self.parent_group.notNil) {
					self.parent_group.name.debug("le fameux group");

					[player.name, self.parent_group.selected_child].debug("group: il s'interroge s'il est selectioné");
					if( self.parent_group.selected_child == player.uname ) { 1 } { 0 } // FIXME: name or uname ?
					//0;
				} {
					[param.extname, param.name, player.get_selected_param].debug("il s'interroge s'il est selectioné");
					if( player.get_selected_param_object === param ) { 1 } { 0 }
				}
			},
			max_cells: { arg self;
				editplayer.model.max_cells;	
			},
			get_selected_cell: {
				param.get_selected_cell;
			},
			name: { arg self;
				"chu dans name".debug;
				param.name;
			},
			slider_width: 100,
			background_color: ~editplayer_color_scheme.control,
			show_midibloc: false,
			width: 200,
			height: 30,
			name_width: { arg self;
				50;
			}
		);
	},

	get_paramlist_splited: { arg self;
		var player = self.current_player;
		var mode;
		var args;
		var args1;
		var args2;
		var args3;
		if(player.uname == \voidplayer) {
			[[], []];
		} {
			args = player.get_ordered_args;
			args = args.reject { arg x; self.param_types.param_reject.includes(x) };
			args = args.reject { arg x; x.asString.beginsWith("macro") };
			mode = player.get_mode;
			args = ~sort_by_template.(args, self.param_types.param_status_group ++ self.param_types.param_order);

			if(player.kind == \player) {
				args = [mode] ++ args;
			};

			// FIXME: handle legato
			args = args.reject { arg x; x == \legato };

			args1 = args.select { arg x; ([mode] ++ self.param_types.param_status_group).includes(x) };
			args2 = args.reject { arg x; ([mode] ++ self.param_types.param_status_group).includes(x) };
			args3 = self.get_effects_paramlist;

			[args1, args2, args3];
		}
	},

	get_paramlist_macros: { arg self;
		var player = self.current_player;
		var mode;
		var args;
		var args1;
		var args2;
		var args3;
		if(player.uname == \voidplayer) {
			[];
		} {
			args = player.get_ordered_args;
			args = args.select { arg x; x.asString.beginsWith("macro") };
		}
	},

	get_paramlist: { arg self;
		var player = player ?? self.current_player;
		var mode;
		var args;
		var res;
		
		args = self.get_paramlist_splited;
		res = args[0] ++ self.get_paramlist_macros ++ args[1] ++ args[2];
		res[..24] // FIXME: hardcoded
	},

	get_param_name_by_display_idx_OLD: { arg self, idx;
		var args, args2, args3;
		var param_name;
		args = self.get_paramlist_splited;
		args2 = args[1] ++ args[2];
		args3 = self.get_paramlist_macros;

		if(idx < 8) {
			param_name = args[0][idx];
		} {
			if(idx < 16) {
				if(args3[idx-8].notNil) {
					param_name = args3[idx-8];
				} {
					param_name = args2[idx-8];
				}
			} {
				if(args3[idx-16].notNil) {
					param_name = args2[idx-16];
				} {
					param_name = args2[idx-8];
				}
			}
		};
		param_name;
	},

	get_param_name_by_display_idx: { arg self, idx;
		var param_name;
		var player = self.current_player;
		var pat_args, dis_args;
		pat_args = player.get_pattern_args ?? [];
		dis_args = player.get_displayable_args ?? [];

		if(idx < 8) {
			param_name = pat_args[idx];
		} {
			param_name = dis_args[idx-8];
		};
		param_name;
	},

	edit_param_value: { arg self, main, player, param, alternate=false;
		param = param ?? { self.get_selected_param };
		main = main ?? self.get_main;
		debug("class_player_display: edit_param_value");
		switch(param.classtype,
			\adsr, {
				~class_adsr_view.new(param).make_window;

			},
			\control, {
				~make_edit_number_view.(main, "edit param", param, [\knob, 0]);
			},
			\array, {
				~class_param_array_view.new(main, param).make_window;
			},
			\buf, {
				var pl = player ?? self.get_current_player;
				//~choose_sample.(main, { arg buf; param.set_val(buf);  }, pl.get_arg(\samplekit).get_val)
				if(alternate == true) {
					~open_file_dialog.({ arg file;
						if(file.notNil) {
							file = ~samplekit_manager.bogus_absolute_path_to_relative_path(file);
							param.set_val(file);
						}
					})
				} {
					self.get_current_group.identityHash.debug("edit_selected_param: nodegroup: identityHash");
					~class_sample_chooser.new(main, 
						{ arg buf; param.set_val(buf);  },
						pl.get_arg(\samplekit).get_val,
						pl,
						self.get_current_group, // not used
						param.name
					);
				};
			},
			\samplekit, {
				~class_samplekit_chooser.new(main, { arg kit; param.set_val(kit);  })

			}
		);
	},

	edit_selected_param: { arg self, main, player, param, alternate=false;
		self.edit_param_value(main, player, param, alternate)
	},

	make_gui: { arg self;
		Task({
			self.window = Window.new("Player display");
			self.player_view = ~class_basic_player_view.new(self);
			self.window.layout = self.player_view.layout;
			self.make_default_bindings;
			self.window.front;
		}).play(AppClock);
	},


	set_keydown_responder: { arg self, key;
		if(self.window.notNil) {
			self.window.view.keyDownAction = if(self.binding_responder.notNil) {
				key.debug("set_keydown_responder:using binding_responder");
				self.binding_responder.get_kb_responder(key);
			} {
				self.get_main.commands.get_kb_responder(key, self);
			};
		} {
			debug("Cant set keydown responder: window is nil")
		}
	},

	make_default_bindings: { arg self;
		var key = \modulator;
		self.binding_responder = self.get_main.commands.make_binding_responder(key, 
			self.get_bindings
		);
		self.set_keydown_responder(key);
	},

	get_bindings: { arg self, get_top_player, get_param;
		[
			[\close_window, {
				self.window.close;
			}],

			[\play_selected, {
				self.player_ctrl.play_node;
			}],

			[\stop_selected, {
				self.player_ctrl.stop_node;
			}],

			[\panic, {
				self.player_ctrl.get_main.panic;
			}],


			[\select_param, 32, { arg i;
				self.select_param(i)
			}],

			[\change_param_kind, {
				if(self.param_types.param_mode.includes(self.get_selected_param.name).not) {
					~class_param_kind_chooser.new(self.get_main, { arg sel;
						self.change_param_kind(sel);
					})
				}
			}],

			[\change_modulated_param_kind, {
				var param = self.param_ctrl;
				if(param.notNil) {
					~class_symbol_chooser.new(self.get_main, [\scalar,\modulation], { arg kind;
						param.change_kind(kind);
					}, param.current_kind)
				}
			}],

			[\change_mod_kind, {
				var player = self.get_current_player;
				if(player.notNil and: {player.uname != \voidplayer}) {
					~class_symbol_chooser.new(self.get_main, [\note,\pattern], { arg kind;
						player.modulation.set_mod_kind(kind);
					}, player.modulation.mod_kind)
				}
			}],

			[\select_param_preset, 8, { arg idx;
				// FIXME: factorize with side and effect
				var param = self.get_selected_param;
				switch( param.classtype,
					\control, {
						if( param.current_kind == \preset ) {
							param.select_cell(idx);
						} 
					}
				);
			}],

			[\change_player_mode, {
				~class_player_mode_chooser.new(self.get_main, { arg sel;
					self.get_current_player.set_mode(sel);
				})
			}],


		];
	
	},

);


~class_central_player_display = (
	parent: ~player_display,

	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_main = { main };
		if(player.notNil) {
			self.set_current_player(player)
		};

		self;
	},

	current_player_callback: { arg self;
		self.get_main.midi_bindings_manager.assign_player_macros; // FIXME: not the best place to call this
	},

);
