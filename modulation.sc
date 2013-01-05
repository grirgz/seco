

// ==========================================
// MODULATION VIEW
// ==========================================

~class_modulated_param_view = (
	new: { arg self, controller;
		self = self.deepCopy;
		self.modmixer_ctrl = { controller.modmixer_ctrl };
		self.player_ctrl = { controller.player_ctrl };
		self.param_ctrl = { controller.param_ctrl };
		self.modulation_ctrl = { controller.modulation_ctrl };
		self.controller = { controller };

		self.make_gui;

		self.label.string = "%: %".format(controller.parent_player_ctrl.name, controller.param_ctrl.name);

		debug("class_modulated_param_view.new");
		
		~make_class_responder.(self, self.label, controller.param_ctrl, [
			\val, \kind
		]);

		~make_class_responder.(self, self.label, controller.modmixer_ctrl, [
			\selected_slot, \connection, \range,
		]);

		debug("class_modulated_param_view.new: fin");

		self;
	},

	////////// responders

	val: { arg self, obj;
		self.modknob.value = obj.get_norm_val;
		self.val_label.string = obj.get_val.asStringPrec(6);
	},

	kind: { arg self, obj;
		self.kind_label.string = "(%)".format(self.controller.param_ctrl.current_kind);
	},

	selected_slot: { arg self, obj;
		self.slots.do { arg but, idx;
			but.states = [
				[self.controller.get_modulator_name_from_target_slot(idx) ?? "-", Color.black, if(obj.selected_slot == idx) { Color.gray } { Color.white }]
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
			HLayout(
				self.label = StaticText.new
					//.string_("%: %".format(self.player_ctrl.name, param_ctrl.name))
					.string_("player: param");
					self.label,
				self.kind_label = StaticText.new
					//.string_("%: %".format(self.player_ctrl.name, param_ctrl.name))
					.string_("(scalar)");
					self.kind_label,
			),

			self.bt_layout = HLayout(*
					self.slots = 3.collect { arg idx;
							Button.new
								.states_([
									["LFO1"]
								])
								.action_({
									self.modmixer_ctrl.select_slot(idx);
									self.modulation_ctrl.select_slot(self.modmixer_ctrl.get_modulator_name(idx));
								})
								.receiveDragHandler_({
									self.controller.connect_modulator(View.currentDrag, idx);
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
	new: { arg self, player_display;
		self = self.deepCopy;
		self.player_display = { player_display };
		self.make_gui;
		self.modulation_ctrl = { player_display.parent_modulation_ctrl };
		player_display.get_current_player.uname.debug("class_modulator_header_view: player uname");
		// FIXME: same modulation_ctrl
		~make_class_responder.(self, self.label, self.modulation_ctrl, [
			\modulator
		]);
		~make_class_responder.(self, self.label, self.player_display.parent_modulation_ctrl, [
			\selected_slot
		]);


		self;
	},

	modulator: { arg self;
		//self.label.string = self.player_display.get_current_player.uname;
		self.selected_slot;
	},

	selected_slot: { arg self;
		var player;
		player = self.player_display.get_current_player;
		if(player.notNil) {
			self.label.string = player.uname;
			if(player.modulation.notNil) {
				if(self.modresponder.notNil) {
					self.modresponder.remove;
				};
				self.modresponder = ~make_class_responder.(self, self.label, player.modulation, [
					\mod_kind
				], false);
				self.kind_label.string = player.modulation.mod_kind;
			}
		};
	},

	mod_kind: { arg self;
		var player;
		debug("handler: mod_kind");
		player = self.player_display.get_current_player;
		if(player.notNil) {
			if(player.modulation.notNil) {
				self.kind_label.string = player.modulation.mod_kind;
			}
		}
	},

	make_gui: { arg self;
		self.layout = HLayout(
			self.label = StaticText.new
				.string_("-");
				self.label,

			self.kind_label = StaticText.new
				.string_("-");
				self.kind_label,

		);
		self.layout
	}

);

~class_modulator_body_basic = (
	new: { arg self, player_display;
		self = self.deepCopy;
		//self.make_gui;
		self.modulation_ctrl = { player_display.modulation_ctrl };

		self.make_gui;
		self.player_display = { player_display };

		~make_class_responder.(self, self.param_group.layout, self.modulation_ctrl, [
			\modulator
		]);
		~make_class_responder.(self, self.param_group.layout, self.player_display.parent_modulation_ctrl, [
			\selected_slot
		]);
		self;
	},

	selected_slot: { arg self;
		var nodename;
		//self.modulation_ctrl.selected_slot.debug("selected_slot: mod: selected_slot");
		//self.modulation_ctrl.get_modulator_node(self.modulation_ctrl.selected_slot).uname.debug("selected_slot: modnode name");
		self.set_controller(self.modulation_ctrl.get_modulator_node(self.player_display.selected_slot));
	},

	modulator: { arg self;
		//self.modulation_ctrl.selected_slot.debug("modulator: selected_slot");
		//self.modulation_ctrl.get_modulator_node(self.modulation_ctrl.selected_slot).uname.debug("modulator: modnode name");
		self.set_controller(self.modulation_ctrl.get_modulator_node(self.player_display.selected_slot));
	},

	set_controller: { arg self, player;
		self.get_controller = { player };
		self.player_display.set_current_player(player); // FIXME: inversion of control
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
	new: { arg self, controller;
		self = self.deepCopy;
		self.player_display = { controller };
		self.player_ctrl = { controller.player_ctrl };
		self.param_ctrl = { controller.param_ctrl };
		self.modmixer_ctrl = { controller.modmixer_ctrl };
		self.modulation_ctrl = { controller.modulation_ctrl };
		self.parent_modulation_ctrl = { controller.parent_modulation_ctrl };
		self.controller = { controller };

		debug("class_modulation_view.new");

		//self.make_gui;
		self.make_window;
		
		//self.tab_buttons[0].children.debug("=======================================");

		~make_class_responder.(self, self.tab_buttons[0][0], self.controller.modulation_ctrl, [
			\modulator
		]);
		~make_class_responder.(self, self.tab_buttons[0][0], self.controller.parent_modulation_ctrl, [
			\selected_slot
		]);
		debug("class_modulation_view.new: fin");
	
		self;
	},

	selected_slot: { arg self;
		self.tab_buttons.do { arg butlay, idx;
			idx.debug("class_modulation_view: selected_slot");
			butlay[1].states_([
				[self.controller.get_modulator_name_from_source_slot(idx) ?? "-", Color.black, if(self.controller.is_slot_selected(idx)) { Color.gray } { Color.white }]
			])
		};
	},

	modulator: { arg self; self.selected_slot; },


	make_tabs: { arg self;
		var make_tab_button = { arg idx;
			var lay;
			lay = [
				DragSource.new.object_(idx).maxWidth_(15),
				Button.new
					.states_([
						["-"]
					])
					.action_({
						self.parent_modulation_ctrl.select_slot(idx);
					}),
			];
			lay;
		};
		self.tab_buttons = 8.collect { arg idx;
			make_tab_button.(idx)
		};
		self.tab_layout = HLayout(*
			self.tab_buttons.collect { arg lay;
				[
					[lay[0], stretch:0],
					[lay[1], stretch:1]
				]
			}.flatten(1)
		);
		self.tab_layout;
	},

	make_gui: { arg self;
		self.layout = VLayout(
			self.make_tabs,
			HLayout(
				self.mod_param = ~class_modulated_param_view.new(self.controller); self.mod_param.layout,
				[VLayout(
					self.mod_header = ~class_modulator_header_view.new(self.controller); self.mod_header.layout,
					self.mod_body = ~class_modulator_body_basic.new(self.controller); self.mod_body.layout,
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


// ==========================================
// MODULATION CONTROLLERS
// ==========================================


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
	selected_slot: 0,

	new: { arg self, main, player_ctrl, parent_player_ctrl, param_ctrl;
		var modmixer;
		self = self.deepCopy;

		//self.parent_player = { parent_player };
		//self.mod_player = { mod_player };

		self.parent_player_ctrl = { parent_player_ctrl ?? player_ctrl };

		self.get_main = { main };
		self.player_ctrl = { player_ctrl };
		self.param_ctrl = { param_ctrl };
		self.modulation_ctrl = { player_ctrl.modulation };
		self.parent_modulation_ctrl = { self.parent_player_ctrl.modulation };

		self.param_ctrl.name.debug("class_modulation_controller param name");

		modmixer = self.parent_player_ctrl.modulation.get_modulation_mixer(self.param_ctrl.name);
		self.modmixer_ctrl = { modmixer };

		self.model.param_no_midi = self.param_types.param_no_midi;
		self.current_player = player_ctrl;
	
		self.make_bindings;
		self.make_gui;
	
		self;
	},

	is_slot_selected: { arg self, idx;
		self.parent_modulation_ctrl.selected_slot == idx;
	},

	select_slot: { arg self, idx;
		self.parent_modulation_ctrl.select_slot(idx);
	},

	selected_slot: { arg self, idx;
		self.parent_modulation_ctrl.selected_slot
	},

	connect_modulator: { arg self, source, target;
		var param_name = self.param_ctrl.name;
		self.parent_modulation_ctrl.connect_modulator(source, param_name, target);
	},

	get_modulator_name_from_source_slot: { arg self, slot;
		self.modulation_ctrl.get_modulator_name(slot)
	},

	get_modulator_name_from_target_slot: { arg self, slot;
		var source;
		source = self.modmixer_ctrl.get_source_slot_from_target_slot(slot);
		source.debug("get_modulator_name_from_target_slot: source");
		self.modulation_ctrl.get_modulator_name(source)
	},

	make_gui: { arg self;
		self.main_view = ~class_modulation_view.new(self);
		self.window = self.main_view.window;
		self.window.view.toFrontAction = { self.make_bindings };
		self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\modulator);
	},

	make_bindings: { arg self;

		self.get_main.commands.parse_action_bindings(\modulator, [

			[\close_window, {
				self.window.close;
			}],

			[\load_modulator, {
				~class_symbol_chooser.new(self.get_main, [\lfo1,\line1], { arg libmodnodename;
					var nodename;
					var mod = self.player_ctrl.modulation;
					nodename = self.get_main.node_manager.make_livenode_from_libmodnode(libmodnodename);
					mod.set_modulator_name(self.selected_slot, nodename);
				})
			}],

			[\edit_modulator, {
				var player = self.get_current_player;
				var param = self.get_selected_param;
				~class_modulation_controller.new(self.get_main, self.player_ctrl, player, param);
			}],

			[\select_player, 10, { arg i;
				self.parent_modulation_ctrl.select_slot((i-1).clip(0,8));
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
				if(player.notNil) {
					~class_symbol_chooser.new(self.get_main, [\note,\pattern], { arg kind;
						player.modulation.set_mod_kind(kind);
					}, player.modulation.mod_kind)
				}
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

// ===========================================
// MODULATION MANAGERS (associated to players)
// ===========================================

~make_modmixer = { arg name, rate=\kr, spec, kind=\normal;
	var sdname = "modmixer_%_%".format(name, spec.asCompileString.hash).asSymbol;
	sdname.debug("make_modmixer");

	SynthDef(sdname, { arg carrier, out, in1=0, range1=0, in2=0, range2=0, in3=0, range3=0;
		var sig1, sig2, sig3;
		var sig;
		var inrate;
		"bla".debug;
		if(kind == \normal) {
			inrate = { arg ... args; In.performList(rate, args) };
		} {
			//inrate = { arg ... args; InFeedback.performList(rate, args) };
			inrate = { arg ... args; In.performList(rate, args) };
		};
		"blai".debug;
		sig1 = inrate.(in1);
		"blaii".debug;
		sig2 = inrate.(in2);
		sig3 = inrate.(in3);
		"blauii".debug;
		
		sig = [
			spec.unmap(carrier),
			sig1 * range1,
			sig2 * range2,
			sig3 * range3,
		].sum;
		sig = spec.map(sig);
		//sig.poll;
		Out.perform(rate, out, sig);

	}).add;
	sdname;
};

~class_effect_manager = (
	effect_list: nil,
	effects_number: 5,
	selected_slot: 0,

	new: { arg self, player;
		self = self.deepCopy;
		self.get_player = { arg self; player };
		self.effect_list = List.newClear(self.effects_number);

		self;	
	},

	select_slot: { arg self, slotidx;
		self.selected_slot = slotidx;
		self.changed(\selected_slot);
	},

	set_effect: { arg self, idx, effect_node_name;
		self.effect_list[idx] = effect_node_name;
	},

	get_effect: { arg self, idx;
		self.effect_list[idx]
	},

	swap_effect: { arg self, idx_source, idx_dest;
		//var tmp = self.effect_list.removeAt(idx_source);
		//self.effect_list.insert(idx_dest, tmp);
		self.effect_list.swap(idx_source, idx_dest);
	},

	vpattern: { arg self;
		
	
	}

);

~class_modulation_mixer_controller = (
	offset: 0,
	modulator: 0,
	slots_number: 3,
	slots: nil,
	selected_slot: 0,


	new: { arg self, name, player;
		self = self.deepCopy;
		
		[name, player.uname].debug("******************* class_modulation_mixer_controller: name, playeruname");
		self.player = { player };
		self.name = name;

		self.slots = Dictionary.new;

		self;
	},

	get_slots: { arg self;
		self.slots
	},

	refresh: { arg self;
		self.changed(\selected_slot);
		self.changed(\connection);
		self.changed(\range);
	},

	get_param: { arg self;
		self.player.get_arg(self.name);
	},

	get_range: { arg self, idx;
		if(self.slots[idx].notNil) {
			self.slots[idx].range
		} {
			0
		}
	},

	set_range: { arg self, idx, range;
	
		[idx, range, self.slots[idx]].debug("class_modulation_mixer_controller: idx, range");
		if(self.slots[idx].notNil) {
			self.slots[idx].range = range;
			self.changed(\range);
		} {
			nil
		}
	},

	connect_slot: { arg self, source_slot, target_slot;
		if(self.slots[target_slot].isNil) {
			self.slots[target_slot] = (range:0)
		};
		self.slots[target_slot].name = source_slot;
		self.changed(\connection);
	},

	disconnect_slot: { arg self, target_slot;
		if(self.slots[target_slot].notNil) {
			self.slots[target_slot].name = nil;
		};
		self.changed(\connection);
	},

	get_modulator_name: { arg self, idx;
		// the name is the key in the modulator dictionnary
		if(self.slots[idx].notNil) {
			self.slots[idx].name
		} {
			nil
		}
	},

	get_source_slot_from_target_slot: { arg self, idx;
		// the name is the key in the modulator dictionnary
		if(self.slots[idx].notNil) {
			self.slots[idx].name
		} {
			nil
		}
	},

	get_modulator_node_name: { arg self, idx;
		if(self.slots[idx].notNil) {
			self.player.modulation.get_modulator_name(self.slots[idx].name)
		}
	},

	select_slot: { arg self, slotidx;
		self.selected_slot = slotidx;
		self.changed(\selected_slot);
	}

);

~class_modulation_manager = (

	modulators: Dictionary.new,
	modulation_mixers: Dictionary.new,
	mod_kind: \note,
	selected_slot: 0,

	new: { arg self, player;
		self = self.deepCopy;
		self.player = { player };
	
		self;
	},

	refresh: { arg self;
		self.changed(\selected_slot);
		self.changed(\mod_kind);
	},

	set_mod_kind: { arg self, kind;
		if(kind != self.mod_kind) {
			self.mod_kind = kind;
			self.changed(\mod_kind)
		}
	},

	connect_modulator: { arg self, source_slot, param_name, target_slot;
		// target: param key name
		// index: target_slot
		// modname: source_slot
		if(self.modulation_mixers[param_name].isNil) {
			self.modulation_mixers[param_name] = ~class_modulation_mixer_controller.new(param_name, self.player);
		};
		self.modulation_mixers[param_name].connect_slot(source_slot, target_slot);
	},

	disconnect_modulator: { arg self, target, index;
		if(self.modulation_mixers[target].notNil) {
			self.modulation_mixers[target].disconnect_slot(index);
		};
	},


	get_modulators: { arg self;
		self.modulators;
	},

	get_modulation_mixer: { arg self, name;
		// FIXME: what if there is no param with this name ?
		if(self.modulation_mixers[name].isNil) {
			self.modulation_mixers[name] = ~class_modulation_mixer_controller.new(name, self.player);
		};
		self.modulation_mixers[name];
	
	},

	get_modulation_mixers: { arg self;
		self.modulation_mixers
	},

	get_modulator_name: { arg self, idx;
		self.modulators[idx];
	},

	get_modulator_node: { arg self, idx;
		var nodename;
		if(idx.isNil) {
			nodename = \voidplayer;
		} {
			nodename = self.modulators[idx] ?? \voidplayer;
		};
		self.player.get_main.get_node(nodename);
	},

	set_modulator_name: { arg self, idx, mod_name;
		self.modulators[idx] = mod_name;
		self.changed(\modulator, idx);
	},

	select_slot: { arg self, slotidx;
		self.selected_slot = slotidx;
		self.changed(\selected_slot);
	},

	make_modulation_pattern: { arg self, source_pattern;
		var free_defer_time = 3; // FIXME: hardcoded
		var out_bus = 0;
		var mainplayer = self.player;
		Pspawner({ arg spawner;
			var str;
			var main_note_pat;
			var mixer_list = List.new;
			var note_modulator_list = List.new;
			var pattern_modulator_list = List.new;
			var effect_list = List.new;
			var effect_pat_list = List.new;
			var effect_inbus_list = List.new;
			var effect_outbus_list = List.new;
			var synth_out_bus;
			var ppatch;
			var rate = \kr;
			var make_note_out_bus;
			var make_modulator_pattern, make_mixer_pattern;
			var walk_modulators;
			var done_modulators = Set.new;
			var allocator_note_pattern;
			var note_bus_alloc_list = List.new;
			"$$$$$$$$$$$$$$$$$$$$ make_modulator_pattern: START".debug;

			ppatch = (
				note_bus: Dictionary.new,
				note_group: Dictionary.new,
				global_bus: Dictionary.new,
				global_group: Dictionary.new,
				get_mod_bus: { arg ppself, prefix, name;
					var bus;
					bus = ppself.note_bus["mixer_%_%".format(prefix, name).asSymbol];
					if(bus.notNil) {
						bus;
					} {
						"mixer_%_%".format(prefix, name).asSymbol.debug("error: modulation bus not found");
						nil
					}
				},

			);

			///////// building effects patterns

			effect_list = mainplayer.effects.effect_list.reject({ arg ef; ef.isNil });

			if(effect_list.size == 0) {
				synth_out_bus = out_bus;
			} {
				ppatch.global_bus[\synth] = Bus.audio(s, 2);
				synth_out_bus = ppatch.global_bus[\synth];
				effect_inbus_list.add(ppatch.global_bus[\synth]);
				(effect_list.size - 1).do { arg idx;
					var bus = Bus.audio(s, 2);
					ppatch.global_bus["effect_%".format(idx).asSymbol] = bus;
					effect_outbus_list.add(bus);
					effect_inbus_list.add(bus);
				};
				effect_outbus_list.add(out_bus);
			};

			effect_list.do { arg effect_name, idx;
				var effect = mainplayer.get_main.get_node(effect_name);
				effect_pat_list.add( effect.vpattern <> Pbind(
					\ppatch, Pfunc{ppatch},
					\group, Pfunc{ arg ev; ev[\ppatch].global_group[\effects] },
					\addAction, \addToTail,
					\in, effect_inbus_list[idx],
					\out, effect_outbus_list[idx],
				))
			};

			///////// functions


			//make_note_out_bus = { arg key, group_name;
			//	note_bus_alloc_list = 
			//	Pfunc{ arg ev;
			//		//var brate = if(rate == \kr) { \control } { \audio };
			//		var brate = \control;
			//		var bus = Bus.alloc(brate, s, 1);
			//		var pp = ev[\ppatch];
			//		//var pp = ppatch;
			//		var group;
			//		[key, group_name].debug("make_note_out_bus: key, group_name");
			//		pp.note_bus[key] = bus;
			//		group = Group.new(pp.global_group[group_name]);
			//		ev[\group] = group;
			//		pp.note_group[key] = group;
			//		bus;
			//	}
			//};

			make_note_out_bus = { arg key, group_name;
				note_bus_alloc_list.add(key);
				Pfunc{ arg ev;
					var brate = \control;
					//var bus = Bus.alloc(brate, s, 1);
					var pp = ev[\ppatch];
					var group;
					[key, group_name].debug("make_note_out_bus: key, group_name");
					group = Group.new(pp.global_group[group_name]);
					ev[\group] = group;
					pp.note_group[key] = group;
					pp.note_bus[key];
				}
			};

			make_modulator_pattern = { arg player, mod, key;
				var modpat;
				var out_bus_name;
				var brate = if(rate == \kr) { \control } { \audio };
				// key is modulator source slot index
				out_bus_name = "mod_%_%".format(player.uname, mod.uname).asSymbol;

				if(mod.modulation.mod_kind == \pattern) {
					ppatch.global_bus[out_bus_name] = Bus.alloc(brate, s, 1);
					modpat = Pmono(mod.get_arg(\instrument).get_val,
							//\ppatch, Pfunc{ppatch},
							\group, Pfunc{ arg ev; ppatch.global_group[\modulator] },
							\out, Pfunc{ arg ev; ppatch.global_bus[out_bus_name] }
						) <> mod.sourcepat <> Pbind(\ppatch, Pfunc{ppatch});
				} {
					modpat = Pbind(
							//\ppatch, Pfunc{ppatch},
							\out, make_note_out_bus.(out_bus_name, \modulator)
						) <> player.get_dur_pattern <> mod.sourcepat <> Pbind(\ppatch, Pfunc{ppatch});
				};
				modpat;

			};

			make_mixer_pattern = { arg player, key, modmixer, kind=\normal;
				// key is pattern key name which is modulated
				var mixer;
				var mixer_synthdef_name;
				var mixerarglist = List.new;
				var mixer_group_name;
				var out_bus_name;
				var spec;

				//if(kind == \normal) {
				//	mixer_group_name = \mixer;
				//} {
				//	mixer_group_name = \fbmixer;
				//};
				mixer_group_name = \mixer;

				spec = player.get_arg(key).spec;

				mixer_synthdef_name = ~make_modmixer.(key, rate, spec, kind);
				out_bus_name = "mixer_%_%".format(player.uname, key).asSymbol;

				mixerarglist = List[
					\instrument, mixer_synthdef_name,
					\ppatch, Pfunc{ppatch},
					\carrier, Pfunc{ player.get_arg(key).get_val },
					\out, make_note_out_bus.(out_bus_name, mixer_group_name),
				];

				modmixer.get_slots.keysValuesDo { arg slotidx, modstruct, idx;
					var in_bus_name = "mod_%_%".format(mainplayer.uname, mainplayer.modulation.get_modulator_name(modstruct.name)).asSymbol;
					idx = idx + 1;
					mixerarglist = (mixerarglist ++ [
						(\in++idx).asSymbol, Pfunc{ arg ev;
							[in_bus_name, modstruct, idx].debug("mixer: in");
							if(mainplayer.modulation.get_modulator_node(modstruct.name).modulation.mod_kind == \pattern) {
								ev[\ppatch].global_bus[in_bus_name];
							} {
								ev[\ppatch].note_bus[in_bus_name];
							}
						},
						(\range++idx).asSymbol, Pfunc{modstruct.range}
					]).asList;
				};
				mixerarglist.debug("make_mixer_pattern: mixerarglist");
				mixer = Pbind(*mixerarglist) <> mainplayer.get_dur_pattern;
				mixer_list.add(mixer);
			};

			///////// building modulators and mixer patterns

			walk_modulators = { arg player, kind=\feedback;
				player.modulation.get_modulation_mixers.keysValuesDo { arg key, modmixer;
					if(modmixer.get_slots.size > 0) {
						modmixer.get_slots.keysValuesDo { arg slotidx, modstruct, idx;
							var modname = mainplayer.modulation.get_modulators[modstruct.name];
							var modnode = mainplayer.get_main.get_node(modname);
							if(done_modulators.includesEqual(modname).not) {
								done_modulators = done_modulators.add(modname);
								if(modnode.modulation.mod_kind == \pattern) {
									pattern_modulator_list.add( make_modulator_pattern.(mainplayer, modnode) )
								} {
									note_modulator_list.add( make_modulator_pattern.(mainplayer, modnode) )
								};
								walk_modulators.(modnode);
							}
						};
						make_mixer_pattern.( player, key, modmixer, kind );
					};
				};
			};

			walk_modulators.( mainplayer, \normal );




			//walk_modulators = { arg player, kind=\feedback;
			//	player.modulation.get_modulators.keysValuesDo { arg key, modname;
			//		var modnode = player.get_main.get_node(modname);
			//		if(modnode.mod_kind == \pattern) {
			//			pattern_modulator_list.add( make_modulator_pattern.(mainplayer, modnode, key) )
			//		} {
			//			note_modulator_list.add( make_modulator_pattern.(mainplayer, modnode, key) )
			//		};
			//		walk_modulators.( modnode );
			//	};

			//	player.modulation.get_modulation_mixers.keysValuesDo { arg key, modmixer;
			//		make_mixer_pattern.( player, key, modmixer, kind )
			//	}
			//};


			///////// creating global groups

			ppatch.global_group[\modulator] = Group.new(s);
			ppatch.global_group[\mixer] = Group.after(ppatch.global_group[\modulator]);
			ppatch.global_group[\synth] = Group.after(ppatch.global_group[\mixer]);
			ppatch.global_group[\effects] = Group.after(ppatch.global_group[\synth]);

			///////// building main pattern

			main_note_pat = Pbind(
					//\ppatch, Pfunc{ppatch},
					\doneAction, 14,
					\out, Pfunc { arg ev; synth_out_bus },
					\group, Pfunc { arg ev;
						var pp = ev[\ppatch];
						var group;
						var note_group;
						var note_bus;
						//var freq = ev[\freq].value(ev);
						debug("synth: group");
						group = Group.new(pp.global_group[\synth]);
						group.register;
						pp.note_group[mainplayer.name] = group;
						note_group = pp.note_group.copy;
						note_bus = pp.note_bus.copy;
						group.addDependant({ arg grp, status;
							[grp, status].debug("dependant");
							if(status == \n_end) {
								"freeing".debug;
								note_group.keysValuesDo { arg gname, gobj;
									gname.debug("free group");
									if(gname != mainplayer.name) {
										gobj.free
									}
								};
								note_bus.keysValuesDo { arg bname, bobj;
									bname.debug("free bus");
									bobj.free;
								};
							}
						});
						group;
					}
				) <> source_pattern <> Pbind(\ppatch, Pfunc{ppatch});
		
			allocator_note_pattern = mainplayer.get_dur_pattern <> Pbind(
				\type, \rest,
				\ppatch, Pfunc{ppatch},
				\alloc, Pfunc{ arg ev;
					note_bus_alloc_list.do { arg key;
						key.debug("alloc note bus");
						ev[\ppatch].note_bus[key] = Bus.control(s, 1);
					}
				}
			);

			[pattern_modulator_list, note_modulator_list, mixer_list, effect_pat_list].debug("pat, not, mix, eff");

			spawner.par(allocator_note_pattern);

			pattern_modulator_list.do { arg pat;
				spawner.par(pat);
			};
			note_modulator_list.do { arg pat;
				spawner.par(pat);
			};
			"bla0".debug;
			mixer_list.do { arg pat;
				spawner.par(pat);
			};
			effect_pat_list.do { arg pat;
				spawner.par(pat);
			};
			//spawner.par(Ppar(note_modulator_list));
			//spawner.par(Ppar(mixer_list));

			"bla1".debug;
			str = CleanupStream(main_note_pat.asStream, {
				"cleanup".debug;
				spawner.suspendAll;
				{
					"defered cleanup".debug;
					ppatch.note_bus.keysValuesDo { arg bname, bobj;
						bname.debug("free bus");
						bobj.free;
					};
					ppatch.global_group.keysValuesDo { arg gname, gobj;
						gname.debug("pattern group free");
						gobj.free;
					};
					ppatch.global_bus.keysValuesDo { arg bname, bobj;
						bname.debug("pattern bus free");
						bobj.free;
					};
					"fin cleanup".debug;
				}.defer(free_defer_time); 
			});
			"bla2".debug;

			spawner.par(str);
			"bla3".debug;
			"$$$$$$$$$$$$$$$$$$$$ make_modulator_pattern: END".debug;
		});
	}


);

// ====================================================================================
// ====================================================================================

// ==========================================
// EFFECTS VIEW
// ==========================================

~class_effect_mini_view = (
	new: { arg self, controller;
		self = self.deepCopy;

		debug("class_effect_mini_view.new");
		
		self.make_gui;
	
		self;
	},

	set_player_controller: { arg self, player;
		self.player_ctrl = {player};
		self.player_responder !? { self.player_responder.remove };
		if(player.notNil) {
			self.player_responder = ~make_class_responder.(self, self.bt_name, player, [
				\redraw_node
			]);
			self.bt_name.states = [
				[self.player_ctrl.name]
			];
		}
	},

	redraw_node: { arg self;
		self.bt_mute.value = if(self.player_ctrl.muted) { 1 } { 0 };
	},

	make_gui: { arg self;
		self.layout = VLayout(
			HLayout(
				self.bt_name = Button.new.states_([["-"]]);
					[self.bt_name, stretch:1],
				[DragSource.new.maxWidth_(15), stretch:0],
				self.bt_mute = Button.new.states_([
						["M", Color.black, Color.white],
						["M", Color.black, Color.gray]
					]);
					[self.bt_mute, stretch:0],
			),
			self.slider_mix = Slider.new
				.orientation_(\horizontal);
				self.slider_mix,
		);
		self.layout;
	},

);

~class_effect_body_basic_view = (
	new: { arg self, player_display;
		self = self.deepCopy;

		self.player_display = { player_display };

		self.make_gui;
		debug("class_effect_body_basic.new");

		~make_class_responder.(self, self.param_group.layout, self.player_display, [
			\player
		]);

		self;
	},

	player: { arg self;
		var player = self.player_display.get_current_player;
		player.name.debug("class_effect_body_basic_view: player.name");
		player.data.keys.debug("class_effect_body_basic_view: player.data.keys");
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

~class_effects_view = (
	mini_views: List.new,

	new: { arg self, controller;
		self = self.deepCopy;

		self.controller = { controller };

		debug("class_effect_view.new");

		self.make_window;

		~make_class_responder.(self, self.window.view, self.controller, [
			\groupnode,
		]);

	
		self;
	},

	groupnode: { arg self;
		self.mini_views.do { arg view, idx;
			var player = self.controller.get_player_at(idx);
			view.set_player_controller(player);
		}
	},

	make_gui: { arg self;
		self.mini_views = List.new;
		self.layout = HLayout(
			[VLayout(*
				self.controller.effects_ctrl.effects_number.collect { arg idx;
					var mv = ~class_effect_mini_view.new(self.controller);
					self.mini_views.add(mv);
					mv.layout;
				} ++ [nil]
			), stretch:0],
			[~class_effect_body_basic_view.new(self.controller).layout, stretch:1]; 

		);
		self.layout;
		
	},

	make_window: { arg self;
		self.window = Window.new("Effects");
		self.window.layout = self.make_gui;
		self.window.front;
		self.window;
	}
);

// ==========================================
// EFFECTS CONTROLLERS
// ==========================================


~class_effects_controller = (
	parent: ~class_player_display,

	new: { arg self, main, player_ctrl;
		var modmixer;
		self = self.deepCopy;

		self.get_main = { main };
		self.player_ctrl = { player_ctrl };
		self.effects_ctrl = { player_ctrl.effects };

		self.model.param_no_midi = self.param_types.param_no_midi;
	
		self.make_bindings;
		self.make_gui;
	
		self;
	},

	is_slot_selected: { arg self, idx;
		self.effects_ctrl.selected_slot == idx;
	},

	select_slot: { arg self, idx;
		var player_name;
		self.effects_ctrl.select_slot(idx);
		player_name = self.effects_ctrl.get_effect(self.effects_ctrl.selected_slot);
		self.set_current_player(self.get_main.get_node(player_name ?? \voidplayer));
	},

	selected_slot: { arg self, idx;
		self.effects_ctrl.selected_slot
	},

	get_player_at: { arg self, idx;
		self.get_main.get_node(self.effects_ctrl.get_effect(idx));
	},


	make_gui: { arg self;
		self.main_view = ~class_effects_view.new(self);
		self.window = self.main_view.window;
		self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\effects);
	},

	make_bindings: { arg self;

		self.get_main.commands.parse_action_bindings(\effects, [

			[\close_window, {
				self.window.close;
			}],

			[\load_effect, {
				~class_symbol_chooser.new(self.get_main, [\comb1], { arg libnodename;
					var nodename;
					var fx = self.player_ctrl.effects;
					nodename = self.get_main.node_manager.make_livenode_from_libfxnode(libnodename);
					fx.set_effect(self.selected_slot, nodename);
					self.set_current_player(self.get_main.get_node(nodename));
					self.changed(\groupnode);
				})
			}],

			[\edit_modulator, {
				var player = self.get_current_player;
				var param = self.get_selected_param;
				~class_modulation_controller.new(self.get_main, self.player_ctrl, player, param);
			}],

			[\select_player, 10, { arg i;
				self.select_slot((i-1).clip(0,4));
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

		]);
	
	},

);
