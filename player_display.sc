
~class_player_display = (
	param_types: (
		param_status_group: List[\amp, \dur, \segdur, \stretchdur, \repeat, \mbufnum, \bufnum, \samplekit],
		param_order: List[\sustain, \pan, \attack, \release, \adsr, \freq],
		param_mode: [\scoreline, \stepline, \noteline, \sampleline, \nodeline],
		param_kinds: [\scalar, \seq, \seg, \modulation, \synchrone, \bus, \recordbus, \pkey],
		param_no_midi: { arg self; self.param_field_group ++ [\mbufnum, \bufnum, \samplekit] ++ self.param_mode; },
		param_reject: { arg self; [\out, \instrument, \tsustain, \type, \gate, \agate, \t_trig, \doneAction] ++ self.param_mode; },
		param_kind_accept: [\control],
		param_midi_reject: { arg self; Set.newFrom(self.param_reject ++ self.param_no_midi++ self.param_mode); },

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

		self.get_maim = { main };
		if(player.notNil) {
			self.set_current_player(player)
		};

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

	set_keydown_responder: { arg self, key;
		if(self.window.notNil) {
			self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(key, self);
		} {
			debug("Cant set keydown responder: window is nil")
		}
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

	change_param_kind: { arg self, kind;
		var param = self.get_selected_param;
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

	get_param_name_by_display_idx: { arg self, idx;
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

	edit_param_value: { arg self, main, player, param;
		param = param ?? { self.get_selected_param };
		main = main ?? self.get_main;
		debug("class_player_display: edit_param_value");
		switch(param.classtype,
			\control, {
				~make_edit_number_view.(main, "edit param", param, [\knob, 0]);
			},
			\buf, {
				var pl = player ?? self.get_current_player;
				//~choose_sample.(main, { arg buf; param.set_val(buf);  }, pl.get_arg(\samplekit).get_val)
				self.get_current_group.identityHash.debug("edit_selected_param: nodegroup: identityHash");
				~class_sample_chooser.new(main, 
					{ arg buf; param.set_val(buf);  },
					pl.get_arg(\samplekit).get_val,
					pl,
					self.get_current_group, // not used
					param.name
				)
			},
			\samplekit, {
				~class_samplekit_chooser.new(main, { arg kit; param.set_val(kit);  })

			}
		);
	},

	edit_selected_param: { arg self;
		self.edit_param_value
	
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

		];
	
	},

);


~class_central_player_display = (
	parent: ~player_display,

	new: { arg self, main, player;
		self = self.deepCopy;

		self.get_maim = { main };
		if(player.notNil) {
			self.set_current_player(player)
		};

		self;
	},

	current_player_callback: { arg self;
		self.get_main.midi_bindings_manager.assign_player_macros; // FIXME: not the best place to call this
	},

);
