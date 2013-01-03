
~class_modulated_param_view = (
	new: { arg self, modmixer_ctrl, player_ctrl, param_ctrl;
		self = self.deepCopy;
		self.modmixer_ctrl = { modmixer_ctrl };
		self.player_ctrl = { player_ctrl };
		self.param_ctrl = { param_ctrl };
		self.modulation = { player_ctrl.modulation };

		self.make_gui;

		self.label.string = "%: %".format(player_ctrl.name, param_ctrl.name);

		
		self.label.string.debug("class_modulated_param_view: label");
		modmixer_ctrl.debug("class_modulated_param_view: modmixer_ctrl");
		modmixer_ctrl.name.debug("class_modulated_param_view: modmixer_ctrl.name");

		~make_class_responder.(self, self.label, modmixer_ctrl.get_param, [
			\val
		]);

		~make_class_responder.(self, self.label, modmixer_ctrl, [
			\selected_slot, \connection, \range,
		]);

		self;
	},

	////////// responders

	val: { arg self, obj;
		self.modknob.value = obj.get_norm_val;
		self.val_label.string = obj.get_val.asStringPrec(6);
	},

	selected_slot: { arg self, obj;
		self.slots.do { arg but, idx;
			but.states = [
				[obj.get_modulator_node_name(idx) ?? "-", Color.black, if(obj.selected_slot == idx) { Color.gray } { Color.white }]
			]
		};
	
	},

	range: { arg self;
		var range, norm_val;
		var val, midval;
		range = self.modmixer_ctrl.get_range(self.modmixer_ctrl.selected_slot);
		norm_val = self.param_ctrl.get_norm_val;
		
		debug("class_modulated_param_view");
		self.modknob.set_range(self.modmixer_ctrl.selected_slot, range);
		self.modknob.refresh;
		self.range_slider.value = range/2 + 0.5;
		val = self.param_ctrl.spec.map(norm_val + range);
		midval = self.param_ctrl.spec.map(norm_val);
		//self.range_low.string = (if(range < 0) { range  } { 0.0 }).asStringPrec(6);
		self.range_low.string = (if(range < 0) { val  } { midval }).asStringPrec(6);
		//self.range_high.string = (if(range > 0) { val  } { 0.0 }).asStringPrec(6);
		self.range_high.string = (if(range > 0) { val  } { midval }).asStringPrec(6);
	},

	connection: { arg self, obj; self.selected_slot(obj) },

	///////////////

	make_gui: { arg self;
		self.layout = VLayout(
			self.label = StaticText.new
				//.string_("%: %".format(self.player_ctrl.name, param_ctrl.name))
				.string_("player: param");
				self.label,

			self.bt_layout = HLayout(*
					self.slots = 3.collect { arg idx;
							Button.new
								.states_([
									["LFO1"]
								])
								.action_({
									self.modmixer_ctrl.select_slot(idx);
									self.modulation.select_slot(self.modmixer_ctrl.get_modulator_name(idx));
								})
								.receiveDragHandler_({
									self.modulation.connect_modulator(View.currentDrag, self.param_ctrl.name, idx);
									View.currentDrag.debug("CURRENTFRAF");

								})
						};
						self.slots;
				);
				self.bt_layout,

			self.modknob = ModKnob.new;
				self.modknob.asView.maxSize = 100@100;
				self.modknob.asView.minSize = 50@50;
				self.modknob.action = { arg knob;
					self.param_ctrl.set_norm_val(self.modknob.value);
				};
				[self.modknob.asView, align:\center],

			self.val_label = StaticText.new
				.string_("12345.12");
				[self.val_label, align:\center],

			self.range_layout = HLayout(
					self.range_low = TextField.new; self.range_low,
					self.range_high = TextField.new; self.range_high,
				);
				self.range_layout,

			self.range_slider = Slider.new
				.orientation_(\horizontal)
				.action_({ arg slider;
					self.modmixer_ctrl.set_range(self.modmixer_ctrl.selected_slot, slider.value - 0.5 * 2);
				});
				self.range_slider,

			self.center_slider = Slider.new
				.orientation_(\horizontal);
				self.center_slider,

			nil
		);
		self.layout;
	},
);

~class_modulator_header_view = (
	new: { arg self, player_display, modulation_ctrl;
		self = self.deepCopy;
		self.player_display = { player_display };
		self.make_gui;
		self.modulation_ctrl = { modulation_ctrl };
		player_display.get_current_player.uname.debug("class_modulator_header_view: player uname");
		~make_class_responder.(self, self.label, self.modulation_ctrl, [
			\selected_slot, \modulator
		]);

		self;
	},

	modulator: { arg self;
		self.label.string = self.player_display.get_current_player.uname;
		//TODO: mod_kind
	},

	selected_slot: { arg self;
		self.label.string = self.player_display.get_current_player.uname;
		//TODO: mod_kind
	},

	make_gui: { arg self;
		self.layout = HLayout(
			self.label = StaticText.new
				.string_("LFO1");
				self.label,

			self.kind_label = StaticText.new
				.string_("Note");
				self.kind_label,

		);
		self.layout
	}

);

~class_modulator_body_basic = (
	new: { arg self, player_display, modulation_ctrl;
		self = self.deepCopy;
		//self.make_gui;
		self.modulation_ctrl = { modulation_ctrl };

		self.make_gui;
		self.player_display = { player_display };

		~make_class_responder.(self, self.param_group.layout, modulation_ctrl, [
			\selected_slot, \modulator
		]);
		self;
	},

	selected_slot: { arg self;
		var nodename;
		self.modulation_ctrl.selected_slot.debug("selected_slot: mod: selected_slot");
		self.modulation_ctrl.get_modulator_node(self.modulation_ctrl.selected_slot).uname.debug("selected_slot: modnode name");
		self.set_controller(self.modulation_ctrl.get_modulator_node(self.modulation_ctrl.selected_slot));
	},

	modulator: { arg self;
		self.modulation_ctrl.selected_slot.debug("modulator: selected_slot");
		self.modulation_ctrl.get_modulator_node(self.modulation_ctrl.selected_slot).uname.debug("modulator: modnode name");
		self.set_controller(self.modulation_ctrl.get_modulator_node(self.modulation_ctrl.selected_slot));
	},

	set_controller: { arg self, player;
		self.get_controller = { player };
		self.player_display.set_current_player(player);
		self.param_group.paramview_list.do { arg view, idx;
			var param_name;
			var param, display;
			param_name = self.player_display.get_param_name_by_display_idx(idx);
			param_name.debug("class_modulator_body_basic: set_controller: param_name");
			if(param_name.notNil) {
				param = player.get_arg(param_name);
				display = self.player_display.make_param_display(param);
				view.set_param(param, display);
			} {
				view.clear_view;
			}
		};
	},

	make_gui: { arg self;
		self.param_group = ~make_mini_param_group_widget.(nil, 3, ());
		self.layout = VLayout.new;
		self.layout.add(self.param_group.layout);
		self.layout
	}

);

~class_modulation_view = (
	new: { arg self, player_display, modmixer_ctrl, player_ctrl, param_ctrl;
		self = self.deepCopy;
		self.player_display = { player_display };
		self.player_ctrl = { player_ctrl };
		self.param_ctrl = { param_ctrl };
		self.modmixer_ctrl = { modmixer_ctrl };
		self.modulation_ctrl = { player_ctrl.modulation };

		debug("class_modulation_view.new");

		//self.make_gui;
		self.make_window;
		
		//self.tab_buttons[0].children.debug("=======================================");

		~make_class_responder.(self, self.tab_buttons[0][0], self.modulation_ctrl, [
			\selected_slot, \modulator
		]);
		debug("class_modulation_view.new: fin");
	
		self;
	},

	selected_slot: { arg self;
		self.tab_buttons.do { arg butlay, idx;
			idx.debug("class_modulation_view: selected_slot");
			butlay[1].states_([
				[self.modulation_ctrl.get_modulator_name(idx) ?? "-", Color.black, if(self.modulation_ctrl.selected_slot == idx) { Color.gray } { Color.white }]
			])
		};
	},

	modulator: { arg self; self.selected_slot; },


	make_tabs: { arg self;
		var make_tab_button = { arg idx;
			var lay;
			lay = [
				DragSource.new.object_(idx),
				Button.new
					.states_([
						["-"]
					])
					.action_({
						self.modulation_ctrl.select_slot(idx);
					}),
			];
			lay;
		};
		self.tab_buttons = 8.collect { arg idx;
			make_tab_button.(idx)
		};
		self.tab_layout = HLayout(*
			self.tab_buttons.flat
		);
		self.tab_layout;
	},

	make_gui: { arg self;
		self.layout = VLayout(
			self.make_tabs,
			HLayout(
				self.mod_param = ~class_modulated_param_view.new(self.modmixer_ctrl, self.player_ctrl, self.param_ctrl); self.mod_param.layout,
				[VLayout(
					self.mod_header = ~class_modulator_header_view.new(self.player_display, self.modulation_ctrl); self.mod_header.layout,
					self.mod_body = ~class_modulator_body_basic.new(self.player_display, self.modulation_ctrl); self.mod_body.layout,
				), stretch:1]
			)
		);
		self.layout
	},

	make_window: { arg self;
		self.window = Window.new("Modulation");
		self.window.layout = self.make_gui;
		self.window.front;
		self.window;
	}
);


/////////////////////////////// controllers


~class_player_display = (
	param_types: (
		param_field_group: List[\dur, \segdur, \stretchdur, \repeat],
		param_slider_group: List[\amp, \legato, \pan, \attack, \sustain, \release],
		param_status_group: List[\amp, \dur, \segdur, \stretchdur, \repeat, \bufnum, \samplekit],
		param_order: List[\sustain, \pan, \attack, \release, \adsr, \freq],
		param_mode: [\scoreline, \stepline, \noteline, \sampleline, \nodeline],
		param_no_midi: { arg self; self.param_field_group ++ [\bufnum, \samplekit] ++ self.param_mode; },
		param_reject: { arg self; [\out, \instrument, \type, \gate, \agate, \t_trig] ++ self.param_mode; },
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

	get_current_player: { arg self;
		self.current_player;
	},

	set_current_player: { arg self, player, index;
		// set player object
		var oldplayer;
		player.uname.debug("XXXXX player_display: set_current_player");
		if(self.current_player != player) {
			//if(index.notNil) {
			//	self.get_current_group.select_child_at(index);
			//} {
			//	self.get_current_group.select_child(player.uname);
			//};
			//if(oldplayer.notNil) {
			//	main.freeze_do { oldplayer.get_arg(\amp).changed(\selected); };
			//};
			self.current_player = player;
			//self.assign_midi;
			//main.freeze_do { self.changed(\player); };
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

);

~class_modulation_controller = (
	parent: ~class_player_display,
	new: { arg self, main, player_ctrl, param_ctrl;
		var modmixer;
		self = self.deepCopy;

		self.get_main = { main };
		self.player_ctrl = { player_ctrl };
		self.param_ctrl = { param_ctrl };

		self.param_ctrl.name.debug("class_modulation_controller param name");

		modmixer = player_ctrl.modulation.get_modulation_mixer(self.param_ctrl.name);
		self.modmixer_ctrl = { modmixer };

		self.model.param_no_midi = self.param_types.param_no_midi;
		self.current_player = player_ctrl;
	
		self.make_bindings;
		self.make_gui;
	
		self;
	},

	make_gui: { arg self;
		self.main_view = ~class_modulation_view.new(self, self.modmixer_ctrl, self.player_ctrl, self.param_ctrl);
		self.window = self.main_view.window;
		self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\modulator);
	},

	make_bindings: { arg self;

		self.get_main.commands.parse_action_bindings(\modulator, [

			[\close_window, {
				self.window.close;
			}],

			[\load_modulator, {
				~class_symbol_chooser.new(self.get_main, [\lfo1], { arg libmodnodename;
					var nodename;
					var mod = self.player_ctrl.modulation;
					nodename = self.get_main.node_manager.make_livenode_from_libmodnode(libmodnodename);
					mod.set_modulator_name(mod.selected_slot, nodename);
				})
			}],

			[\select_param, 32, { arg i;
				self.select_param(i)
			}],
		]);
	
	},

);

~windowize = { arg layout;
	var win;
	win = Window.new;
	win.layout = layout;
	win.front;
};

////////////////////////////////
