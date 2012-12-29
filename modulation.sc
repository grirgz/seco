
~class_modulation_view = (
	new: { arg self, player_ctrl, param_ctrl;
		self = self.deepCopy;
		self.player_ctrl = { player_ctrl };
		self.param_ctrl = { param_ctrl };

		self.make_gui;
	
		self;
	},

	make_tabs: { arg self;
		var make_tab_button = { arg idx;
			var lay;
			idx = idx + 1;
			lay = HLayout(
				Button.new.states_([
					["%: %".format(idx, "-")]
				]),
				DragSource.new
			);
			lay;
		};
		self.tab_layout = HLayout(*
			self.tab_buttons = 8.collect { arg idx;
					make_tab_button.(idx)
				};
				self.tab_buttons
		);
		self.tab_layout;
	},

	make_gui: { arg self;
		self.layout = VLayout(
			self.make_tabs,
			HLayout(
				self.mod_param = ~class_modulated_param_view.new(self.player_ctrl, self.param_ctrl); self.mod_param.layout,
				[VLayout(
					self.mod_header = ~class_modulator_header_view.new; self.mod_header.make_gui,
					self.mod_body = ~class_modulator_body_basic.new; self.mod_body.make_gui,
				), stretch:1]
			)
		);
		self.layout
	},

	make_window: { arg self;
		self.window = Window.new;
		self.window.layout = self.make_gui;
		self.window.front;
		self.window;
	}
);

~class_modulator_header_view = (
	new: { arg self;
		self = self.deepCopy;
		//self.make_gui;
		self;
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
	new: { arg self;
		self = self.deepCopy;
		//self.make_gui;
		self;
	},

	set_controller: { arg self, controller;
		self.get_controller = { controller };
	
	},

	make_gui: { arg self;
		self.param_group = ~make_mini_param_group_widget.(nil, 3, ());
		self.layout = VLayout.new;
		self.layout.add(self.param_group.layout);
		self.layout
	}

);

~class_modulated_param_view = (
	new: { arg self, modmixer_ctrl, player_ctrl, param_ctrl;
		self = self.deepCopy;
		self.modmixer_ctrl = { modmixer_ctrl };
		self.player_ctrl = { player_ctrl };
		self.param_ctrl = { param_ctrl };

		self.make_gui;

		self.label.string = "%: %".format(player_ctrl.name, param_ctrl.name);

		~make_class_responder.(self, self.label, modmixer_ctrl.get_param, [
			\val
		]);

		~make_class_responder.(self, self.label, modmixer_ctrl, [
			\selected_slot
		]);

		self;
	},

	////////// responders

	val: { arg self, obj;
		self.modknob.value = obj.get_norm_val;
		self.val_label.string = obj.get_val.asString;
	},

	selected_slot: { arg self, obj;
		self.slots.do { arg but, idx;
			but.states = [
				[obj.get_modulator_name, if(obj.selected_slot == idx) { Color.gray } { Color.clear }]
			]
		};
	
	},

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
								})
						};
						self.slots;
				);
				self.bt_layout,

			self.modknob = ModKnob.new;
				self.modknob.asView.maxSize = 100@100;
				self.modknob.asView.minSize = 50@50;
				[self.modknob.asView, align:\center],

			self.val_label = StaticText.new
				.string_("12345.12");
				[self.val_label, align:\center],

			self.range_layout = HLayout(
					TextField.new,
					TextField.new,
				);
				self.range_layout,

			self.range_slider = Slider.new
				.orientation_(\horizontal);
				self.range_slider,

			self.center_slider = Slider.new
				.orientation_(\horizontal);
				self.center_slider,

			nil
		);
		self.layout;
	},
);

/////////////////////////////// controllers

~class_modulation_controller = (
	new: { arg self, player_ctrl, param_ctrl;
		self = self.deepCopy;

		self.player_ctrl = { player_ctrl };
		self.param_ctrl = { param_ctrl };
		self.make_gui;
	
		self;
	},

	make_gui: { arg self;
		self.main_view = ~class_modulation_view.new(self.player_ctrl, self.param_ctrl);
		self.window = self.main_view.make_window;
	}

);

~windowize = { arg layout;
	var win;
	win = Window.new;
	win.layout = layout;
	win.front;
};

