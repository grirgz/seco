

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

		self.label.string = "%: %".format(controller.parent_player_ctrl.get_label, controller.param_ctrl.name);

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
		self.val_label.string = obj.get_val.asFloat.asStringPrec(6);
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
		self.range;
	
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
									self.controller.select_slot(self.modmixer_ctrl.get_source_slot_from_target_slot(idx));
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
		//self.modulation_ctrl.changed(\selected_slot);
		self.make_responders;

		self;
	},

	new_without_responders: { arg self, player_display;
		self = self.deepCopy;
		self.player_display = { player_display };
		self.make_gui;
		self.modulation_ctrl = { player_display.parent_modulation_ctrl };
		player_display.get_current_player.uname.debug("class_modulator_header_view: player uname");
		//self.modulation_ctrl.changed(\selected_slot);

		self;
	},

	make_responders: { arg self;
		// FIXME: same modulation_ctrl
		~make_class_responder.(self, self.label, self.modulation_ctrl, [
			\modulator
		], false); // false to debug
		~make_class_responder.(self, self.label, self.modulation_ctrl, [
			\selected_slot
		], false); // false to debug
		
	},

	modulator: { arg self;
		//self.label.string = self.player_display.get_current_player.uname;
		"$$ modulator responder call selected_slot".debug;
		self.selected_slot;
	},

	selected_slot: { arg self;
		var player;
		debug("class_modulator_header_view: selected_slot");
		player = self.player_display.get_current_player;
		player.uname.debug("class_modulator_header_view: selected_slot: player uname");
		if(player.notNil) {
			self.label.string = player.get_label;
			self.label.string.debug("class_modulator_header_view: selected_slot: self.label");
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
		self.label.string.debug("class_modulator_header_view: selected_slot: self.label end");
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
		debug("class_modulator_header_view: make_gui");
		self.layout = HLayout(
			self.label = StaticText.new
				.string_("+");
				self.label,

			self.kind_label = StaticText.new
				.string_("+");
				self.kind_label,

		);
		self.layout
	}

);

~class_modulator_body_basic = (
	show_custom_view: true,		
	new: { arg self, player_display;
		self = self.deepCopy;
		//self.make_gui;
		self.modulation_ctrl = { player_display.modulation_ctrl };
		self.player_display = { player_display };

		self.make_gui;
		~make_class_responder.(self, self.param_group.layout, self.player_display, [
			\player
		]);

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
		self.modulation_ctrl.selected_slot.debug("class_modulator_body_basic: selected_slot: mod: selected_slot");
		self.modulation_ctrl.get_modulator_node(self.modulation_ctrl.selected_slot).uname.debug("selected_slot: modnode name");
		//self.set_controller(self.modulation_ctrl.get_modulator_node(self.player_display.selected_slot));
		self.show_body_layout;
	},

	modulator: { arg self;
		self.modulation_ctrl.selected_slot.debug("class_modulator_body_basic: modulator: selected_slot");
		self.modulation_ctrl.get_modulator_node(self.modulation_ctrl.selected_slot).uname.debug("modulator: modnode name");
		//self.set_controller(self.modulation_ctrl.get_modulator_node(self.player_display.selected_slot));
		self.show_body_layout;
	},

	//set_controller: { arg self, player;
	//	self.get_controller = { player };
	//	self.param_group.paramview_list.do { arg view, idx;
	//		var param_name;
	//		var param, display;
	//		param_name = self.player_display.get_param_name_by_display_idx(idx);
	//		param_name.debug("class_modulator_body_basic: set_controller: param_name");
	//		[self.player_display.current_player.uname, player.uname].debug("class_modulator_body_basic: set_controller: current_player, controller");
	//		if(param_name.notNil) {
	//			param = player.get_arg(param_name);
	//			display = self.player_display.make_param_display(param);
	//			view.set_param(param, display);
	//		} {
	//			view.clear_view;
	//		}
	//	};
	//},

	player: { arg self;
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
			self.player_display.get_current_player.uname.debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: curplayer");
			self.player_display.set_keydown_responder(\modulator);
			//0.1.wait;
			if(extplayer.notNil and: { self.show_custom_view }) {
				// FIXME: external player should have custom gui
				self.stack_layout.index = 1;
				extlayout = extplayer.make_layout;
				self.custom_view.children.do(_.remove);
				debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: before cusheader");
				//self.custom_header_view = ~class_modulator_header_view.new_without_responders(myself.controller); 
				debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: after cusheader");
				//self.tab_custom_view.layout_(
				//	VLayout(
				//		[self.custom_header_view.layout, stretch:0],
				//		extplayer.layout,
				//	)
				//);
				//self.custom_header_view.selected_slot;
				self.custom_view.layout = extlayout;
				debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: after view cusheader");
				debug("class_ci_osc3filter2: make_tab_panel: show_body_layout: last view cusheader");
			} {
				self.stack_layout.index = 0;
			}
		}.play(AppClock)
		
	},

	make_gui: { arg self;
		debug("class_modulator_body_basic: make_gui 1");
		self.param_group = ~make_mini_param_group_widget.(nil, 3, ());
		self.param_group_layout = VLayout.new;
		self.param_group_layout.add(self.param_group.layout);
		//0.1.wait;
		debug("class_modulator_body_basic: make_gui 2");
		self.custom_view = View.new;
		//0.1.wait;
		debug("class_modulator_body_basic: make_gui 3");
		self.stack_layout = StackLayout(
			View.new.layout_(self.param_group_layout),
			self.custom_view;
		);
		//0.1.wait;
		debug("class_modulator_body_basic: make_gui 4");
		self.show_body_layout;
		//0.1.wait;
		debug("class_modulator_body_basic: make_gui 5");
		self.layout = self.stack_layout;
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
			]);
			//0.3.wait;
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
						self.controller.select_slot(idx);
					}),
			];
			lay;
		};
		self.tab_buttons = 8.collect { arg idx;
			make_tab_button.(idx)
		};
		self.tab_layout = HLayout(*
			self.tab_buttons.collect { arg lay;
				HLayout(
					[lay[0], stretch:0],
					[lay[1], stretch:1]
				).spacing_(0)
			}
		);
		self.tab_layout;
	},

	make_gui: { arg self;
		self.layout = VLayout(
			self.make_tabs,
			//0.1.wait;
			HLayout(
				self.mod_param = ~class_modulated_param_view.new(self.controller); self.mod_param.layout,
				//0.1.wait;
				[VLayout(
					self.mod_header = ~class_modulator_header_view.new(self.controller); self.mod_header.layout,
					//0.1.wait;
					self.mod_body = ~class_modulator_body_basic.new(self.controller); self.mod_body.layout,
					//0.1.wait;
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

~class_embeded_modulation_view = (
	parent: ~class_modulation_view,
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

		self.make_gui;
		//self.make_window;
		
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

	show_body_layout: { arg self;
		//self.body_stack_layout.index = 4;
	},

	make_gui: { arg self;
		self.make_tabs;
		self.mod_header = ~class_modulator_header_view.new(self.controller); 
		self.header_layout = self.mod_header.layout;
		self.mod_body = ~class_modulator_body_basic.new(self.controller);
		self.body_layout = VLayout(
			[self.header_layout, stretch:0],
			self.mod_body.layout,
		);
	},

	selected_slot: { arg self;
		self.show_body_layout;
		self.tab_buttons.do { arg butlay, idx;
			idx.debug("class_modulation_view: selected_slot");
			butlay[1].states_([
				[self.controller.get_modulator_name_from_source_slot(idx) ?? "-", Color.black, if(self.controller.is_slot_selected(idx)) { Color.gray } { Color.white }]
			]);
			//0.3.wait;
		};
	},
);


// ==========================================
// MODULATION CONTROLLERS
// ==========================================


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

		//self.param_ctrl.name.debug("class_modulation_controller param name");

		modmixer = self.parent_player_ctrl.modulation.get_modulation_mixer(self.param_ctrl.name);
		self.modmixer_ctrl = { modmixer };

		self.model.param_no_midi = self.param_types.param_no_midi;
		//self.current_player = player_ctrl;
		self.select_slot(self.selected_slot);
	
		//self.make_gui;
	
		self;
	},

	is_slot_selected: { arg self, idx;
		self.parent_modulation_ctrl.selected_slot == idx;
	},

	select_slot: { arg self, idx;
		self.modulation_ctrl.get_modulator_node(idx).uname.debug("class_modulation_controller: select_slot: modnode");
		self.set_current_player(
			self.modulation_ctrl.get_modulator_node(idx)
		);
		self.parent_modulation_ctrl.select_slot(idx);
	},

	selected_slot: { arg self, idx;
		self.parent_modulation_ctrl.selected_slot
	},

	connect_modulator: { arg self, source, target;
		var param_name = self.param_ctrl.name;
		self.parent_modulation_ctrl.connect_modulator(source, param_name, target);
	},

	disconnect_modulator: { arg self, target;
		var param_name = self.param_ctrl.name;
		self.parent_modulation_ctrl.disconnect_modulator(param_name, target);
	},

	remove_current_modulator: { arg self, free=false;
		var mod = self.player_ctrl.modulation;
		var modname;
		modname = mod.get_modulator_name(self.selected_slot);
		if(free) {
			self.get_main.free_node(modname);
		};
		mod.set_modulator_name(self.selected_slot, nil);
		self.modmixer_ctrl.changed(\selected_slot)
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
		Task({
			self.make_bindings;
			self.main_view = ~class_modulation_view.new(self);
			self.window = self.main_view.window;
			self.window.view.toFrontAction = { self.make_bindings };
			self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\modulator);
		}).play(AppClock);
	},

	make_bindings: { arg self;

		self.get_main.commands.parse_action_bindings(\modulator, 

			self.get_main.panels.side.get_shared_bindings ++
			self.get_main.panels.side.get_windows_bindings ++ [
			[\close_window, {
				self.window.close;
			}],

			[\load_modulator, {
				~class_symbol_chooser.new(self.get_main, self.get_main.model.modnodelib, { arg libmodnodename;
					var nodename;
					var mod = self.player_ctrl.modulation;
					nodename = self.get_main.node_manager.make_livenode_from_libmodnode(libmodnodename);
					mod.set_modulator_name(self.selected_slot, nodename);
					self.select_slot(self.selected_slot);
				})
			}],

			[\assign_midi_knob, {
				var param = self.get_selected_param;
				self.get_main.panels.side[\binding_assign_midi_knob].(param)
			}],

			[\switch_body_view, {
				self.main_view.mod_body.switch_body_view;
			}],

			[\remove_modulator, {
				self.remove_current_modulator(true)
			}],

			[\edit_selected_param, {
				self.edit_selected_param;
			}],


			[\disconnect_modulator, {
				self.disconnect_modulator(self.modmixer_ctrl.selected_slot);
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

			[\edit_modulator, {
				var player = self.get_current_player;
				var param = self.get_selected_param;
				var side = self.get_main.panels.side;

				param.debug("edit_modulator PARAM");
				if(param.notNil and: {param.classtype == \control}) {
					side[\make_window_panel].(self, \modulation_controller, 
						{ 
							debug("TEST");
							self.modulation_controller.param_ctrl != param
						},
						{
							~class_modulation_controller.new(self.get_main, self.player_ctrl, player, param);
						}
					);
				} {
					debug("ERROR: param classtype can't be modulated: trying side panel param");
					side[\edit_modulator_callback].()
				}
			}],

			[\select_player, 10, { arg i;
				//self.parent_modulation_ctrl.select_slot((i-1).clip(0,8));
				self.select_slot((i-1).clip(0,8));
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

		]);
	
	},

);

~class_embeded_modulation_controller = (
	parent: ~class_modulation_controller,

	make_gui: { arg self;
		//self.main_view = ~class_modulation_view.new(self);
		//self.window = self.main_view.window;
		//self.window.view.toFrontAction = { self.make_bindings };
		//self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\modulator);
	},
);

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
	archive_data: [\effect_list, \selected_slot],

	new: { arg self, player;
		self = self.deepCopy;
		self.get_player = { arg self; player };
		self.effect_list = List.newClear(self.effects_number);

		self;	
	},

	save_data: { arg self, options;
		var data = ();
		self.archive_data.do { arg key;
			data[key] = self[key];
		};
		if(options.notNil) {
			if(options[\copy_subplayers] == true) {
				data[\effect_list] = data[\effect_list].collect { arg nodename;
					if(nodename.notNil and: { nodename != \voidplayer }) {
						self.get_player.get_main.get_node(nodename).save_data(options);
					} {
						nil
					}
				}
			};
		};
		data;
	},

	update_modulation_pattern: { arg self;
		self.get_player.modulation.update_modulation_pattern;
	},

	load_data: { arg self, data, options;
		var main;
		main = self.get_player.get_main;
		self.archive_data.do { arg key;
			self[key] = data[key];
		};
		if(options.notNil) {
			if(options[\copy_subplayers] == true) {
				self[\effect_list] = self[\effect_list].collect { arg nodedata;
					var nodename;
					var defname;
					var node;
					if(nodedata.notNil) {
						// FIXME: can use clone instead, no ?
						nodename = main.node_manager.make_livenode_from_libmodnode(nodedata.instrname);
						node = main.get_node(nodename);
						defname = node.defname;
						node.load_data(nodedata);
						node.uname = nodename;
						node.name = nodename;
						node.defname = defname;

						nodename
					} {
						nil
					}
				}
			};
		};
	
	},

	select_slot: { arg self, slotidx;
		self.selected_slot = slotidx;
		self.changed(\selected_slot);
	},

	set_effect: { arg self, idx, effect_node_name;
		self.effect_list[idx] = effect_node_name;
		self.update_modulation_pattern;
	},

	get_effect: { arg self, idx;
		self.effect_list[idx]
	},

	swap_effect: { arg self, idx_source, idx_dest;
		//var tmp = self.effect_list.removeAt(idx_source);
		//self.effect_list.insert(idx_dest, tmp);
		self.effect_list.swap(idx_source, idx_dest);
		self.update_modulation_pattern;
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
	archive_data: [\name, \slots],


	new: { arg self, name, player;
		// name can be nil when loading data
		self = self.deepCopy;
		
		[name, player.uname].debug("******************* class_modulation_mixer_controller: name, playeruname");
		self.player = { player };
		self.name = name;

		self.slots = Dictionary.new;

		self;
	},

	update_modulation_pattern: { arg self;
		self.player.modulation.update_modulation_pattern;
	},

	save_data: { arg self;
		var data;
		data = IdentityDictionary.new;
		self.archive_data.do { arg key;
			data[key] = self[key]
		};
		data;
	},

	load_data: { arg self, data;
		self.archive_data.do { arg key;
			self[key] = data[key]
		};
	},

	get_slots: { arg self;
		self.slots
	},

	get_used_slots: { arg self;
		var res = Dictionary.new;
		self.slots.keysValuesDo { arg key, val;
			if(val.name != nil and: { val.muted != true }) {
				res[key] = val;
			}
		};
		res;
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
			self.changed(\range, idx);
		} {
			nil
		}
	},

	mute_slot: { arg self, target_slot, val=true;
		self.slots[target_slot].muted = val;
		//self.slots[target_slot.dump].muted.debug("class_modulation_mixer_controller: mute_slot: slot muted");
		self.update_modulation_pattern;
		self.changed(\connection, target_slot);
	},

	is_slot_muted: { arg self, target_slot;
		self.slots[target_slot].debug("class_modulation_mixer_controller: is_slot_muted");
		self.slots[target_slot].notNil and: {
			self.slots[target_slot].muted == true;
		}
	},

	connect_slot: { arg self, source_slot, target_slot;
		if(self.slots[target_slot].isNil) {
			self.slots[target_slot] = (range:0)
		};
		self.slots[target_slot].name = source_slot;
		self.update_modulation_pattern;
		self.changed(\connection, target_slot);
	},

	disconnect_slot: { arg self, target_slot;
		if(self.slots[target_slot].notNil) {
			self.slots[target_slot].name = nil;
		};
		self.update_modulation_pattern;
		self.changed(\connection, target_slot);
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
	archive_data: [\modulators, \mod_kind, \selected_slot],

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

	save_data: { arg self, options;
		var data = IdentityDictionary.new;
		self.archive_data.do { arg key;
			data[key] = self[key];
		};
		options.debug("modulation SAVE_DATA");
		if(options.notNil) {
			if(options[\copy_subplayers] == true) {
				data[\modulators] = data[\modulators].collect { arg nodename;
					if(nodename.notNil and: { nodename != \voidplayer }) {
						self.player.get_main.get_node(nodename).save_data;
					} {
						nil
					}
				}
			};
		};
		data[\modulation_mixers] = Dictionary.new;
		self.modulation_mixers.keysValuesDo { arg key, val;
			data[\modulation_mixers][key] = val.save_data;
			//[key, data[\modulation_mixers][key]].debug("class_modulation_manager: save_data: key, data");
		};
		data;
	},

	load_data: { arg self, data, options;
		var main;
		main = self.player.get_main;
		self.archive_data.do { arg key;
			self[key] = data[key];
		};
		options.debug("modulation LOAD_DATA");
		if(options.notNil) {
			if(options[\copy_subplayers] == true) {
				self[\modulators] = self[\modulators].collect { arg nodedata;
					var nodename;
					var defname;
					var node;
					if(nodedata.notNil) {
						nodename = main.node_manager.make_livenode_from_libmodnode(nodedata.instrname);
						node = main.get_node(nodename);
						defname = node.defname;
						node.load_data(nodedata);
						node.uname = nodename;
						node.name = nodename;
						node.defname = defname;

						nodename
					} {
						nil
					}
				}
			};
		};
		self[\modulation_mixers] = Dictionary.new;
		data[\modulation_mixers].keysValuesDo { arg key, val;
			var modmix = ~class_modulation_mixer_controller.new(nil, self.player);
			modmix.load_data(val);
			self[\modulation_mixers][key] = modmix;
			[key, self[\modulation_mixers][key]].debug("class_modulation_manager: save_data: key, modmix");
		};
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
		self.update_modulation_pattern;
		self.changed(\modulator, idx);
	},

	select_slot: { arg self, slotidx;
		self.selected_slot = slotidx;
		self.changed(\selected_slot);
	},

	//make_spatch: { arg self;

	//	var walk_modulators;
	//	var done_modulators = Set.new;
	//	var mainplayer = self.player;
	//	var note_modulator_list = List.new;
	//	var pattern_modulator_list = List.new;
	//	var make_modulator_vpiano;
	//	var make_note_out_bus;
	//	var note_bus_alloc_list;
	//	var note_group_alloc_list;
	//	var spatch = (
	//		note_bus: Dictionary.new,
	//		note_group: Dictionary.new,
	//		global_bus: Dictionary.new,
	//		global_group: Dictionary.new,
	//	 	get_mod_bus: { arg pself, player_name, param_name;
	//		
	//		}
	//	);

	//	make_note_out_bus = { arg key, group_name;
	//		note_bus_alloc_list.add(key);
	//		note_group_alloc_list.add(key);
	//		Pfunc{ arg ev;
	//			var brate = \control;
	//			//var bus = Bus.alloc(brate, s, 1);
	//			var pp = spatch;
	//			var group;
	//			[key, group_name].debug("vpiano make_note_out_bus: key, group_name");
	//			group = Group.new(pp.global_group[group_name]);
	//			ev[\group] = group;
	//			pp.note_group[key] = group;
	//			pp.note_bus[key];
	//		}
	//	};

	//	make_modulator_vpiano = { arg player, mod, key;
	//		
	//			var modpat;
	//			var out_bus_name;
	//			var brate = if(rate == \kr) { \control } { \audio };
	//			// key is modulator source slot index
	//			out_bus_name = "mod_%_%".format(player.uname, mod.uname).asSymbol;

	//			if(mod.modulation.mod_kind == \pattern) {
	//				//spatch.global_bus[out_bus_name] = Bus.alloc(brate, s, 1);
	//				//modpat = Pmono(mod.get_arg(\instrument).get_val,
	//				//		//\ppatch, Pfunc{ppatch},
	//				//		\group, Pfunc{ arg ev; ppatch.global_group[\modulator] },
	//				//		\out, Pfunc{ arg ev; ppatch.global_bus[out_bus_name] }
	//				//	) <> mod.sourcepat <> Pbind(\ppatch, Pfunc{ppatch});
	//			} {
	//				modpat = mod.get_mod_vpiano(spatch, (
	//					out: make_note_out_bus.(out_bus_name, \modulator),
	//					group: make_note_group.()
	//				))
	//			};
	//			{ modpat; }

	//	};

	//	walk_modulators = { arg player, kind=\feedback;
	//		player.modulation.get_modulation_mixers.keysValuesDo { arg key, modmixer;
	//			if(modmixer.get_slots.size > 0) {
	//				modmixer.get_slots.keysValuesDo { arg slotidx, modstruct, idx;
	//					var modname = mainplayer.modulation.get_modulators[modstruct.name];
	//					var modnode = mainplayer.get_main.get_node(modname);
	//					if(modname.notNil and: {done_modulators.includesEqual(modname).not}) {
	//						done_modulators = done_modulators.add(modname);
	//						if(modnode.modulation.mod_kind == \pattern) {
	//							//pattern_modulator_list.add( make_modulator_pattern.(mainplayer, modnode) )
	//						} {
	//							note_modulator_list.add( make_modulator_vpiano.(mainplayer, modnode) )
	//						};
	//						walk_modulators.(modnode);
	//					}
	//				};
	//				make_mixer_pattern.( player, key, modmixer, kind );
	//			};
	//		};
	//	};

	//		walk_modulators.( mainplayer, \normal );
	//
	//},

	update_modulation_pattern: { arg self, source_pattern=nil;
		var out_bus = 0;
		var mainplayer = self.player;
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
		//var ppatch;
		var rate = \kr;
		var make_note_out_bus;
		var make_modulator_pattern, make_mixer_pattern;
		var walk_modulators;
		var done_modulators = Set.new;
		var allocator_note_pattern;
		var note_bus_alloc_list = List.new;
		var pattern_bus_alloc_list = List.new;
		var pattern_control_bus_alloc_list = List.new;
		var clean_started = false;
		var cleanup_function;
		///////// building effects patterns
		"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% update_modulation_pattern: START".debug;

		source_pattern = self.source_pattern ?? source_pattern;
		self.source_pattern = source_pattern;

		effect_list = mainplayer.effects.effect_list.reject({ arg ef; 
			ef.isNil or: {
				mainplayer.get_main.get_node(ef).muted
			}
		});

		if(effect_list.size == 0) {
			synth_out_bus = \out_bus;
		} {
			//ppatch.global_bus[\synth] = Bus.audio(s, 2);
			pattern_bus_alloc_list.add(\synth);
			//synth_out_bus = ppatch.global_bus[\synth];
			synth_out_bus = \synth;
			effect_inbus_list.add(\synth);
			(effect_list.size - 1).do { arg idx;
				//var bus = Bus.audio(s, 2);
				var bus;
				bus = "effect_%".format(idx).asSymbol;
				pattern_bus_alloc_list.add(bus);
				//ppatch.global_bus["effect_%".format(idx).asSymbol] = bus;
				effect_outbus_list.add(bus);
				effect_inbus_list.add(bus);
			};
			effect_outbus_list.add(\out_bus);
		};

		effect_list.do { arg effect_name, idx;
			var effect = mainplayer.get_main.get_node(effect_name);
			effect_pat_list.add( 
				Pbind(
					\group, Pfunc{ arg ev; ev[\ppatch].global_group[\effects] },
					\addAction, \addToTail,
					\in, Pfunc{ arg ev; ev[\ppatch].global_bus[effect_inbus_list[idx]] },
					\out, Pfunc{ arg ev; ev[\ppatch].global_bus[effect_outbus_list[idx]] },
				) <>
				effect.sourcepat 
				//<>
				//Pbind(
				//	\ppatch, Pfunc{ppatch},
				//)
			)
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
				//ppatch.global_bus[out_bus_name] = Bus.alloc(brate, s, 1);
				pattern_control_bus_alloc_list.add(out_bus_name); //FIXME what about audio rate modulation ?

				modpat = Pmono(mod.get_arg(\instrument).get_val,
						//\ppatch, Pfunc{ppatch},
						\group, Pfunc{ arg ev; ev[\ppatch].global_group[\modulator] },
						\out, Pfunc{ arg ev; ev[\ppatch].global_bus[out_bus_name] }
					) <> mod.sourcepat;
					//<> Pbind(\ppatch, Pfunc{ppatch});
			} {
				modpat = Pbind(
						//\ppatch, Pfunc{ppatch},
						\out, make_note_out_bus.(out_bus_name, \modulator)
					) <> player.get_dur_pattern <> mod.sourcepat;
					//<> Pbind(\ppatch, Pfunc{ppatch});
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
				//\ppatch, Pfunc{ppatch},
				\carrier, Pfunc{ player.get_arg(key).get_val },
				\out, make_note_out_bus.(out_bus_name, mixer_group_name),
			];

			modmixer.get_slots.keysValuesDo { arg slotidx, modstruct, idx;
				var in_bus_name;
				mainplayer.name.debug("make_mixer_pattern: modmixer: mainplayer");
				in_bus_name = "mod_%_%".format(mainplayer.uname, mainplayer.modulation.get_modulator_name(modstruct.name)).asSymbol;
				idx = idx + 1;
				mixerarglist = (mixerarglist ++ [
					(\in++idx).asSymbol, Pfunc{ arg ev;
						var node;
						[in_bus_name, modstruct, idx].debug("mixer: in");
						if(
							node = mainplayer.modulation.get_modulator_node(modstruct.name);
							node.notNil and: {
								node.modulation.notNil and: { 
									modstruct.muted != true 
								}
							}
						) {
							if(mainplayer.modulation.get_modulator_node(modstruct.name).modulation.mod_kind == \pattern) {
								ev[\ppatch].global_bus[in_bus_name];
							} {
								ev[\ppatch].note_bus[in_bus_name];
							}
						} {
							~silent_control_bus;
							// FIXME: is it a problem to return nil here ?
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
					var can_make_mixer = false;
					mainplayer.name.debug("walk_modulators: mainplayer");
					modmixer.get_slots.keysValuesDo { arg slotidx, modstruct, idx;
						var modname = mainplayer.modulation.get_modulators[modstruct.name];
						var modnode = mainplayer.get_main.get_node(modname);
						if(modname.notNil and: {modnode.notNil}) {
							can_make_mixer = true;
							if(done_modulators.includesEqual(modname).not) {
								done_modulators = done_modulators.add(modname);
								if(modnode.modulation.mod_kind == \pattern) {
									pattern_modulator_list.add( make_modulator_pattern.(mainplayer, modnode) )
								} {
									note_modulator_list.add( make_modulator_pattern.(mainplayer, modnode) )
								};
								walk_modulators.(modnode);
							}
						}
					};
					if(can_make_mixer) {
						make_mixer_pattern.( player, key, modmixer, kind );
					}
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



		///////// building main pattern

		main_note_pat = Pbind(
				//\ppatch, Pfunc{ppatch},
				\doneAction, 14,
				\out, Pfunc { arg ev; ev[\ppatch].global_bus[synth_out_bus] },
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
							fork{

								"main_note_pat: freeing".debug;
								note_group.keysValuesDo { arg gname, gobj;
									[gname, gobj].debug("main_note_pat: free note group");
									if(gname != mainplayer.name) {
										gobj.free
									};
									//0.01.wait;

								};
								note_bus.keysValuesDo { arg bname, bobj;
									[bname, bobj].debug("main_note_pat: free note bus");
									bobj.free;
									//0.01.wait;
								};
								grp.releaseDependants;
							}
						}
					});
					group;
				}
			) <> source_pattern;
			//<> Pbind(\ppatch, Pfunc{ppatch});
	
		allocator_note_pattern = mainplayer.get_dur_pattern <> Pbind(
			\type, \rest,
			//\ppatch, Pfunc{ppatch},
			\alloc, Pfunc{ arg ev;
				//ev[\ppatch].note_bus = IdentityDictionary.new;
				//ev[\ppatch].note_group = IdentityDictionary.new;
				note_bus_alloc_list.do { arg key;
					key.debug("alloc note bus");
					ev[\ppatch].note_bus[key] = Bus.control(s, 1);
				}
			}
		);

		[pattern_modulator_list, note_modulator_list, mixer_list, effect_pat_list].debug("pat, not, mix, eff");

		self.pattern_modulator_list = pattern_modulator_list;
		self.note_modulator_list = note_modulator_list;
		self.mixer_list = mixer_list;
		self.effect_pat_list = effect_pat_list;
		self.allocator_note_pattern = allocator_note_pattern;

		self.pattern_bus_alloc_list = pattern_bus_alloc_list;
		self.pattern_control_bus_alloc_list = pattern_control_bus_alloc_list;

		self.main_note_pat = main_note_pat;

		//self.cleanup_function = cleanup_function;
		"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% update_modulation_pattern: STOP".debug;

	},

	make_modulation_pattern: { arg self; //, source_pattern;
		var free_defer_time = 1; // FIXME: hardcoded
		var out_bus = 0;
		var mainplayer = self.player;
		var pspawner;
		pspawner = Pspawner({ arg spawner;
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
			var clean_started = false;
			var cleanup_function;
			var ppatch_pattern;
			var panic_safeguard;
			"$$$$$$$$$$$$$$$$$$$$ make_modulator_pattern: START".debug;


			ppatch = (
				clean_started: false,
				panic_called: false,
				note_bus: Dictionary.new,
				note_group: Dictionary.new,
				global_bus: Dictionary[\out_bus -> out_bus],
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
				cleanup_function: { arg self;
					var ppatch = self;
					"cleanup".debug;
					spawner.suspendAll;
					if(self.clean_started.not) {
					//if(true) {
						self.clean_started = true;
						"sched cleanup".debug;
						{
							//spawner.suspendAll;
							mainplayer.name.debug("defered cleanup");
							if(self.panic_called.not) {
								ppatch.global_group.keysValuesDo { arg gname, gobj;
									[gname, gobj].debug("pattern group free");
									gobj.free;
								};
							};
							CmdPeriod.remove(panic_safeguard);
							ppatch.note_bus.keysValuesDo { arg bname, bobj;
								[bname, bobj].debug("free bus");
								bobj.free;
							};
							ppatch.global_bus.keysValuesDo { arg bname, bobj;
								[bname, bobj].debug("pattern bus free");
								bobj.free;
							};
							"fin cleanup".debug;
						}.defer(free_defer_time + s.latency); 
					}
				};

			);

			panic_safeguard = { ppatch.panic_called = true };
			CmdPeriod.add(panic_safeguard);


			ppatch_pattern = Pbind(
				\ppatch, Pfunc{ppatch}
			);

			pattern_modulator_list = self.pattern_modulator_list;
			note_modulator_list = self.note_modulator_list;
			mixer_list = self.mixer_list;
			effect_pat_list = self.effect_pat_list;
			allocator_note_pattern = self.allocator_note_pattern;

			main_note_pat = self.main_note_pat;

			///////// creating global busses

			self.pattern_bus_alloc_list.debug("pattern_bus_alloc_list");
			self.pattern_control_bus_alloc_list.debug("pattern_control_bus_alloc_list");

			self.pattern_bus_alloc_list.do { arg key;
				ppatch.global_bus[key] = Bus.audio(s, 2);
			};

			self.pattern_control_bus_alloc_list.do { arg key;
				ppatch.global_bus[key] = Bus.control(s);
			};

			///////// creating global groups

			if(pattern_modulator_list.size > 0 or: { note_modulator_list.size > 0 }) {
				ppatch.global_group[\modulator] = Group.new(s);
				ppatch.global_group[\mixer] = Group.after(ppatch.global_group[\modulator]);
			};

			if(ppatch.global_group[\mixer].notNil) {
				ppatch.global_group[\synth] = Group.after(ppatch.global_group[\mixer]);
			} {
				ppatch.global_group[\synth] = Group.new(s);
			};
			
			if(effect_pat_list.size > 0) {
				ppatch.global_group[\effects] = Group.after(ppatch.global_group[\synth]);
			};

			///////////////////// spawning

			[pattern_modulator_list, note_modulator_list, mixer_list, effect_pat_list].debug("pat, not, mix, eff");

			if([pattern_modulator_list, note_modulator_list, mixer_list, effect_pat_list].any{ arg li; li.size > 0 }) {
				spawner.par(allocator_note_pattern <> ppatch_pattern);
			};

			pattern_modulator_list.do { arg pat;
				spawner.par(pat <> ppatch_pattern);
			};
			note_modulator_list.do { arg pat;
				spawner.par(pat <> ppatch_pattern);
			};
			"bla0".debug;
			mixer_list.do { arg pat;
				spawner.par(pat <> ppatch_pattern);
			};
			effect_pat_list.do { arg pat;
				spawner.par(pat <> ppatch_pattern);
			};
			//spawner.par(Ppar(note_modulator_list));
			//spawner.par(Ppar(mixer_list));

			"bla1".debug;
			//str = CleanupStream(main_note_pat.asStream, {
			//	"cleanup".debug;
			//	spawner.suspendAll;
			//	{
			//		"defered cleanup".debug;
			//		ppatch.note_bus.keysValuesDo { arg bname, bobj;
			//			bname.debug("free bus");
			//			bobj.free;
			//		};
			//		ppatch.global_group.keysValuesDo { arg gname, gobj;
			//			gname.debug("pattern group free");
			//			gobj.free;
			//		};
			//		ppatch.global_bus.keysValuesDo { arg bname, bobj;
			//			bname.debug("pattern bus free");
			//			bobj.free;
			//		};
			//		"fin cleanup".debug;
			//	}.defer(free_defer_time); 
			//});
			"bla2".debug;

			//spawner.par(str);

			//spawner.par(main_note_pat);
			
			spawner.par(
				Pfset({},
					main_note_pat <> ppatch_pattern,
					{ "INNER CLEAN".debug; ppatch.cleanup_function; }
				)
			);


			//spawner.par(
			//	Pfset(
			//		{},
			//		main_note_pat,
			//		{
			//			"cleanup".debug;
			//			spawner.suspendAll;
			//			if(clean_started.not) {
			//			//if(true) {
			//				clean_started = true;
			//				"sched cleanup".debug;
			//				{
			//					//spawner.suspendAll;
			//					mainplayer.name.debug("defered cleanup");
			//					ppatch.note_bus.keysValuesDo { arg bname, bobj;
			//						[bname, bobj].debug("free bus");
			//						bobj.free;
			//					};
			//					ppatch.global_group.keysValuesDo { arg gname, gobj;
			//						[gname, gobj].debug("pattern group free");
			//						gobj.free;
			//					};
			//					ppatch.global_bus.keysValuesDo { arg bname, bobj;
			//						[bname, bobj].debug("pattern bus free");
			//						bobj.free;
			//					};
			//					"fin cleanup".debug;
			//				}.defer(free_defer_time + s.latency); 
			//			}
			//		}
			//	)
			//);
			"bla3".debug;
			"$$$$$$$$$$$$$$$$$$$$ make_modulator_pattern: END".debug;
		});
		//Pfset({}, pspawner, { "CLEAN".debug; self[\cleanup_function].value });
	}


);

// ====================================================================================
// ====================================================================================

// ==========================================
// EFFECTS VIEW
// ==========================================

~class_effect_mini_view = (
	new: { arg self, controller, idx;
		self = self.deepCopy;

		debug("class_effect_mini_view.new");
		self.player_idx = idx;
		self.controller = { controller };
		
		self.make_gui;
	
		self;
	},

	set_player_controller: { arg self, player;
		var arg_mix;
		self.player_ctrl = {player};
		self.player_responder !? { self.player_responder.remove };
		if(player.notNil) {
			self.player_responder = ~make_class_responder.(self, self.bt_name, player, [
				\redraw_node
			]);
			arg_mix = player.get_arg(\mix);
			if(arg_mix.notNil) {
				self.slider_mix.enabled = true;
				self.slider_mix.value = arg_mix.scalar.get_norm_val;
			} {
				self.slider_mix.enabled = false;
			};
			self.bt_name.states = [
				[self.player_ctrl.name],
				[self.player_ctrl.name, Color.black, Color.gray]
			];
			self.update_selection;
		} {
			self.slider_mix.enabled = false;
			self.bt_name.states = [
				["-"],
				["-", Color.black, Color.gray]
			];
			self.update_selection;
		}
	},

	redraw_node: { arg self;
		self.bt_mute.value = if(self.player_ctrl.muted) { 1 } { 0 };
		self.update_selection;
	},

	update_selection: { arg self;
		self.bt_name.value = if(self.controller.is_slot_selected(self.player_idx)) { 1 } { 0 };
	},

	make_gui: { arg self;
		self.layout = VLayout(
			HLayout(
				[DragBoth.new
					.maxWidth_(15)
					.object_(self.player_idx)
					.receiveDragHandler_({ 
						self.controller.effects_ctrl.swap_effect(View.currentDrag, self.player_idx);
						self.controller.changed(\player);
						self.controller.changed(\groupnode);
					})
					, stretch:0],
				self.bt_name = Button.new
					.states_([
						["-"],
						["-", Color.black, Color.gray]
					])
					.keyDownAction_(self.controller.get_button_keydownaction) 
					.action_({
						self.controller.select_slot(self.player_idx);
					});
					[self.bt_name, stretch:1],
				self.bt_mute = Button.new
					.maxWidth_(15)
					.states_([
						["M", Color.black, Color.white],
						["M", Color.black, Color.gray]
					])
					.action_({
						self.controller.toggle_mute_slot(self.player_idx)
					});
					[self.bt_mute, stretch:0],
			),
			self.slider_mix = Slider.new
				.orientation_(\horizontal)
				.action_({
					self.controller.set_mix(self.player_idx, self.slider_mix.value)
				});
				self.slider_mix,
		);
		self.update_selection;
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
		debug("class_effect_body_basic_view: player responder");
		self.update_param_group(player);
	},

	update_param_group: { arg self, player;
		if(player.notNil) {
			player.name.debug("class_effect_body_basic_view: player.name");
			//player.data.keys.debug("class_effect_body_basic_view: player.data.keys");
			self.param_group.paramview_list.do { arg view, idx;
				var param_name;
				var param, display;
				param_name = self.player_display.get_param_name_by_display_idx(idx);
				param_name.debug("class_effect_body_basic_view: set_controller: param_name");
				if(param_name.notNil) {
					param = player.get_arg(param_name);
					display = self.player_display.make_param_display(param);
					view.set_param(param, display);
				} {
					view.clear_view;
				};
				//0.35.wait;
			};
		}
		
	},

	make_gui: { arg self;
		self.param_group = ~make_mini_param_group_widget.(nil, 3, ());
		self.layout = VLayout.new;
		self.layout.add(self.param_group.layout);
		self.layout
	}

);

~class_effect_body_custom_view = (
	parent: ~class_effect_body_basic_view,
	show_custom_view: true,		

	make_gui: { arg self;
		
		self.param_group = ~make_mini_param_group_widget.(nil, 3, ());
		self.param_group_layout = VLayout.new;
		self.param_group_layout.add(self.param_group.layout);

		self.custom_view = View.new;

		self.stack_layout = StackLayout(
			View.new.layout_(self.param_group_layout),
			self.custom_view;
		);

		self.show_body_layout;

		self.layout = self.stack_layout;
		self.layout
	},

	player: { arg self;
		self.show_body_layout;
	},

	switch_body_view: { arg self;
		self.show_custom_view = self.show_custom_view.not;
		self.show_body_layout;
	},

	show_body_layout: { arg self;
		var player;
		var extplayer;
		player = self.player_display.get_current_player;

		if(player.notNil) {
			extplayer = player.external_player;

			Task{
				var extlayout;
				debug("class_effect_body_custom_view: show_body_layout");
				self.player_display.set_keydown_responder(\effects);
				debug("class_effect_body_custom_view: show_body_layout1");

				if(extplayer.notNil and: { self.show_custom_view }) {
					debug("class_effect_body_custom_view: show_body_layout2");
					// FIXME: external player should have custom gui
					self.stack_layout.index = 1;
					extlayout = extplayer.make_layout;
					debug("class_effect_body_custom_view: show_body_layout3");

					self.custom_view.children.do(_.remove);
					debug("class_effect_body_custom_view: show_body_layout4");

					self.custom_view.layout = extlayout;
					debug("class_effect_body_custom_view: show_body_layout5");
				} {
					debug("class_effect_body_custom_view: show_body_layout6");
					self.update_param_group(player);
					debug("class_effect_body_custom_view: show_body_layout7");
					self.stack_layout.index = 0;
				};
				debug("class_effect_body_custom_view: END show_body_layout");
			}.play(AppClock)
		
		}
		
	},

);

~class_effects_view = (
	mini_views: List.new,
	old_selection: 0,

	new: { arg self, controller;
		self = self.deepCopy;

		self.controller = { controller };

		debug("class_effect_view.new");

		//self.make_window;
		self.make_gui;


	
		self;
	},

	groupnode: { arg self;
		self.mini_views.do { arg view, idx;
			var player = self.controller.get_player_at(idx);
			view.set_player_controller(player);
		}
	},

	selected_slot: { arg self;
		self.mini_views[self.old_selection].update_selection;
		self.old_selection = self.controller.selected_slot;
		self.mini_views[self.controller.selected_slot].update_selection;
	},

	make_responders: { arg self, parent;
		~make_class_responder.(self, parent, self.controller, [
			\groupnode,
		]);

		~make_class_responder.(self, parent, self.controller.effects_ctrl, [
			\selected_slot,
		]);
	},

	make_gui: { arg self;
		var dependant;
		self.mini_views = List.new;
		self.body_object = ~class_effect_body_custom_view.new(self.controller);
		self.layout = HLayout(
			[VLayout(*
				self.controller.effects_ctrl.effects_number.collect { arg idx;
					var mv = ~class_effect_mini_view.new(self.controller, idx);
					//0.3.wait;
					self.mini_views.add(mv);
					mv.layout;
				} ++ [nil]
			), stretch:0],
			//[~class_effect_body_basic_view.new(self.controller).layout, stretch:1]; 
			[self.body_object.layout, stretch:1]; 

		);

		dependant = self.mini_views[0].bt_name;
		self.make_responders(dependant);

		self.layout;
		
	},

	make_window: { arg self;
		self.window = Window.new("Effects");
		self.window.layout = self.layout;
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
		self = self.deepCopy;

		self.get_main = { main };
		self.player_ctrl = { player_ctrl };
		self.get_player = { player_ctrl };
		self.effects_ctrl = { player_ctrl.effects };

		self.model.param_no_midi = self.param_types.param_no_midi;
	
		//self.make_gui;
		self.select_slot(self.selected_slot);
	
		self;
	},

	refresh: { arg self;
		self.changed(\groupnode);
		self.changed(\player);
	},

	is_slot_selected: { arg self, idx;
		self.effects_ctrl.selected_slot == idx;
	},

	select_slot: { arg self, idx;
		var player_name;
		self.effects_ctrl.select_slot(idx);
		player_name = self.effects_ctrl.get_effect(self.selected_slot);
		self.set_current_player(self.get_main.get_node(player_name ?? \voidplayer));
	},

	mute_slot: { arg self, idx;
		var player;
		player = self.get_player_at(idx);
		if(player.notNil) {
			player.mute(true)
		}
	},

	toggle_mute_slot: { arg self, idx;
		var player;
		player = self.get_player_at(idx);
		if(player.notNil) {
			player.mute(player.muted.not);
			self.effects_ctrl.update_modulation_pattern;
		}
	},

	set_mix: { arg self, idx, val;
		var player;
		player = self.get_player_at(idx);
		if(player.notNil) {
			player.get_arg(\mix).scalar.set_norm_val(val);
		}
	},

	remove_current_effect: { arg self, free=false;
		var nodename;
		if(free) {
			nodename = self.effects_ctrl.get_effect(self.selected_slot);
			self.get_main.free_node(nodename);
		};
		self.effects_ctrl.set_effect(self.selected_slot, nil);
		self.select_slot(self.selected_slot);
		self.changed(\groupnode);
	},

	selected_slot: { arg self;
		self.effects_ctrl.selected_slot
	},

	get_player_at: { arg self, idx;
		self.get_main.get_node(self.effects_ctrl.get_effect(idx));
	},


	make_gui: { arg self;
		Task({
			self.make_bindings;
			self.main_view = ~class_effects_view.new(self);
			self.window = self.main_view.make_window;
			self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\effects);
		}).play(AppClock);
	},

	make_bindings: { arg self;

		self.get_main.commands.parse_action_bindings(\effects, 
			self.get_main.panels.side.get_shared_bindings ++
			self.get_main.panels.side.get_windows_bindings ++ [

			[\close_window, {
				self.window.close;
			}],

			[\edit_selected_param, {
				self.edit_selected_param;
			}],

			[\load_effect, {
				~class_symbol_chooser.new(self.get_main, self.get_main.model.effectlib, { arg libnodename;
					var nodename;
					var fx = self.player_ctrl.effects;
					nodename = self.get_main.node_manager.make_livenode_from_libfxnode(libnodename);
					fx.set_effect(self.selected_slot, nodename);
					self.set_current_player(self.get_main.get_node(nodename));
					self.changed(\groupnode);
				})
			}],

			[\switch_body_view, {
				self.main_view.body_object.switch_body_view;
			}],

			[\edit_modulator, {
				var player = self.get_current_player;
				var param = self.get_selected_param;
				var side = self.get_main.panels.side;

				param.debug("edit_modulator PARAM");
				if(param.notNil and: {param.classtype == \control}) {
					side[\make_window_panel].(self, \modulation_controller, 
						{ 
							self.modulation_controller.param_ctrl != param
						},
						{
							~class_modulation_controller.new(self.get_main, self.player_ctrl, player, param);
						}
					);
				} {
					debug("ERROR: param classtype can't be modulated: trying side panel param");
					side[\edit_modulator_callback].()
				}
			}],

			[\assign_midi_knob, {
				var param = self.get_selected_param;
				self.get_main.panels.side[\binding_assign_midi_knob].(param)
			}],

			[\remove_effect, {
				self.remove_current_effect(true);
			}],

			[\select_player, 10, { arg i;
				self.select_slot((i-1).clip(0,4));
			}],

			[\select_param, 32, { arg i;
				self.select_param(i)
			}],

			[\play_selected, {
				self.player_ctrl.play_node;
			}], 

			[\stop_selected, {
				self.player_ctrl.stop_node;
			}],

			[\panic, {
				self.get_main.panic;
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

~class_embeded_effects_controller = (
	parent: ~class_effects_controller,

	new: { arg self, main, player_ctrl;
		self = self.deepCopy;

		self.get_main = { main };
		self.player_ctrl = { player_ctrl };
		self.effects_ctrl = { player_ctrl.effects };

		self.model.param_no_midi = self.param_types.param_no_midi;
	
		self.make_bindings;
		self.make_gui;
		self.select_slot(self.selected_slot);
	
		self;
	},

	get_button_keydownaction: { arg self;
		{ arg view, char, modifiers, unicode, keycode;
			var symbol;
			debug("RESPO");
			symbol = ~qt_keycode_to_keysymbol.(char, modifiers, unicode, keycode);
			switch(symbol,
				[0, \f1], {
					~class_symbol_chooser.new(self.get_main, self.get_main.model.effectlib, { arg libnodename;
						var nodename;
						var fx = self.player_ctrl.effects;
						nodename = self.get_main.node_manager.make_livenode_from_libfxnode(libnodename);
						fx.set_effect(self.selected_slot, nodename);
						self.set_current_player(self.get_main.get_node(nodename));
						self.changed(\groupnode);
					})
				}
			)

		}
	},

	make_gui: { arg self;
		self.main_view = ~class_effects_view.new(self);
		self.layout = self.main_view.layout;
	},

)
