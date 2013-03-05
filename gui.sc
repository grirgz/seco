
~global_controller = (
	current_param: nil,
);

~class_ci_simpleknob_view = (
	new: { arg self, controller, display;
		var modmixer;
		self = self.deepCopy;

		self.controller = { controller };
		self.display = { display };
		self.make_gui;
	
		self;
	},

	////////// responders

	val: { arg self;
		{
			self.vallabel.string = self.controller.get_val.asFloat.asStringPrec(~general_sizes.float_precision);
			self.knob.value = self.controller.get_norm_val;
		}.defer;
	},

	label: { arg self;
		self.controller.label.debug("class_ci_simpleknob_view: LABEL");
		self.controller.name.debug("class_ci_simpleknob_view: LABEL2");
		self.namelabel.string = self.controller.label ?? self.controller.name;
	},


	/////////// methods

	make_gui: { arg self;
		var label;
		var knob;
		var vallabel;
		var layout;
		//self.layout = VLayout(
		layout = VLayout(
			label = StaticText.new
				.font_(Font("Arial",11))
				.string_(self.controller.label ?? self.controller.name);
				[label, stretch:0, align:\center],
			knob = Knob.new; 
				knob.action_({ 
					self.controller.set_norm_val(knob.value)
				});
				knob.asView.debug("VIEW");
				knob.asView.minSize_(50@50);
				[knob.asView, stretch: 0, align:\center],
			vallabel = StaticText.new
				.string_("12354")
				.font_(Font("Arial",11))
				.align_(\center)
				//.minWidth_(75);
				;
				[vallabel, stretch:0, align:\center],
			nil
		).spacing_(0).margins_(0);

		//knob.focusGainedAction = { arg me;
		//	me.background = Color.gray(0.6);
		//	~global_controller.current_param = self.controller;
		//};

		//knob.focusLostAction = { arg me;
		//	try {
		//		me.background = Color.clear;
		//	}
		//	//~global_controller.current_param = self.controller;
		//};


		self.namelabel = label;
		self.knob = knob;
		self.vallabel = vallabel;
		//self.layout.minHeight = 65;
		//layout.minHeight = 65;
		0.01.wait;
		self.layout = layout;

		~make_class_responder.(self, self.namelabel, self.controller, [
			\val, \label,
		]);

		layout;

	},
);

~class_ci_modknob_view = (
	range_val: 0,
	new: { arg self, controller, display;
		var modmixer;
		self = self.deepCopy;

		self.display = { display };
		self.set_controller( controller );
		self.make_gui;
	
		self;
	},

	////////// responders

	val: { arg self;
		{
			self.vallabel.string = self.controller.get_val.asFloat.asStringPrec(~general_sizes.float_precision);
			self.knob.value = self.controller.get_norm_val;
		}.defer;
	},

	label: { arg self;
		self.controller.label.debug("class_ci_modknob_view: LABEL");
		self.controller.name.debug("class_ci_modknob_view: LABEL2");
		self.namelabel.string = self.controller.label ?? self.controller.name;
	},

	range: { arg self, obj, msg, idx;
		idx.debug("set range");
		if(idx.isNil) {
			3.do { arg idx; // FIXME: hardcoded
				self.modmixer.get_range(idx).debug("range");
				self.knob.set_range(idx, self.modmixer.get_range(idx));
			}
		} {
			self.modmixer.get_range(idx).debug("range");
			self.knob.set_range(idx, self.modmixer.get_range(idx));
		};
		self.knob.refresh;
	},

	connection: { arg self, obj, msg, idx;
		var setco;
		idx.debug("class_ci_modknob_view: connection");
		setco = { arg idx;
			self.modmixer.get_source_slot_from_target_slot(idx).debug("class_ci_modknob_view: connection: source_slot");
			self.slots[idx].string = self.modmixer.get_source_slot_from_target_slot(idx) ?? "";
			if(self.modmixer.is_slot_muted(idx)) {
				debug("slot muted");
				self.slots[idx].stringColor = Color.gray;
			} {
				debug("slot NOT muted");
				self.slots[idx].stringColor = Color.black;
			};
		};
		if(idx.isNil) {
			3.do { arg idx; // FIXME: hardcoded
				setco.(idx);
			}
		} {
			setco.(idx);
		}
	},

	/////////// methods

	set_controller: { arg self, controller;
		var modmixer;
		self.controller = { controller };
		if(self.controller.notNil) {
			modmixer = self.controller.get_player.modulation.get_modulation_mixer(self.controller.name);
			self.modmixer = { modmixer };
		};
		self.make_responders;
	},

	clear_slot: { arg self, idx;
		idx.debug("class_ci_modknob_view: clear_slot");
		self.modmixer.disconnect_slot(idx);
		self.modmixer.set_range(idx, 0);
	},	

	toggle_mute_slot: { arg self, idx;
		idx.debug("class_ci_modknob_view: toggle_mute_slot");
		if(self.modmixer.is_slot_muted(idx)) {
			self.modmixer.mute_slot(idx, false);
		} {
			self.modmixer.mute_slot(idx, true);
		}
	},

	make_modslots: { arg self;
		HLayout(*
			self.slots = 3.collect { arg idx;
				DragSink.new
					.maxSize_(15@15)
					.receiveDragHandler_({ arg dragsink;
						if(self.controller.notNil) {
							dragsink.string = View.currentDrag;
							[View.currentDrag, self.controller.name, idx].debug("CONNECT MOD");
							self.controller.get_player.modulation.connect_modulator(View.currentDrag, self.controller.name, idx);
							self.controller.change_kind(\modulation);
						}
					})
					.mouseDownAction_({ arg view, x, y, modifier, buttonNumber, clickCount;
						buttonNumber.debug("class_mod_slot: mouseDownAction: buttonNumber");
						//self.x_offset = x;
						//self.val_offset = self.modmixer.get_range(idx);
						if(self.modmixer.notNil) {
							switch(buttonNumber,
								~keycode.mouse.middle_click, {
									self.clear_slot(idx);
								},
								~keycode.mouse.right_click, {
									self.toggle_mute_slot(idx);
								},
								{
									self.x_offset = x;
									self.val_offset = self.modmixer.get_range(idx);
								}
							);
						}
					})
					.mouseMoveAction = { arg view, x, y;
						var nx, ro;
						if(self.modmixer.notNil) {
							nx = x - self.x_offset;
							ro = ((nx/100) + self.val_offset).clip(-0.999, 0.999 );
							[x, y, nx, ro].debug("move");
							//self.range_val = ro;
							//self.action; // function set by outside
							self.modmixer.set_range(idx, ro);
						}
					};
			};
			0.01.wait;
			self.slots
		).margins_(1).spacing_(0)
	},

	make_responders: { arg self;
		if(self.controller.notNil and: { self.responder_anchor.notNil }) {

			self.ctrl_responder.remove;
			self.ctrl_responder = ~make_class_responder.(self, self.responder_anchor, self.controller, [
				\val, \label,
			]);

			self.mixer_responder.remove;
			self.mixer_responder = ~make_class_responder.(self, self.responder_anchor, self.modmixer, [
				\range, \connection
			]);

		};
		
	},

	make_gui: { arg self;
		var label;
		var knob;
		var vallabel;
		var layout;
		//self.layout = VLayout(
		layout = VLayout(
			label = StaticText.new
				.font_(Font("Arial",11))
				.string_(
					if(self.controller.notNil) {
						self.controller.label ?? self.controller.name
					} {
						""
					}
				);
				[label, stretch:0, align:\center],
			knob = ModKnob.new; 
				knob.action_({ 
					if(self.controller.notNil) {
						self.controller.set_norm_val(knob.value)
					}
				});
				knob.asView.debug("VIEW");
				knob.asView.minSize_(50@50);
				[knob.asView, stretch: 0, align:\center],
			vallabel = StaticText.new
				.string_("12354")
				.font_(Font("Arial",11))
				.align_(\center)
				//.minWidth_(75);
				;
				[vallabel, stretch:0, align:\center],
			[self.make_modslots, stretch:0, align:\center],
			nil
		).spacing_(0).margins_(0);

		knob.mouse_edit_pixel_range = 2000;
		knob.focusGainedAction = { arg me;
			me.background = Color.gray(0.6);
			~global_controller.current_param = self.controller;
		};

		knob.focusLostAction = { arg me;
			try {
				me.background = Color.clear;
			}
			//~global_controller.current_param = self.controller;
		};


		self.namelabel = label;
		self.knob = knob;
		self.vallabel = vallabel;
		self.responder_anchor = self.namelabel;
		self.make_responders;
		//self.layout.minHeight = 65;
		//layout.minHeight = 65;
		0.01.wait;
		self.layout = layout;

		layout;

		//StaticText.new.string_("FUCK");
		//HLayout.new
	},
);

~class_ci_modslider_view = (
	parent: ~class_ci_modknob_view,
	new: { arg self, controller, size;
		var modmixer;
		self = self.deepCopy;

		self.controller = { controller };
		debug("BLLA1");
		self.view_size = size ?? Rect(0,0,30,250);
		self.make_gui;
		debug("BLLA2");

		//self[\make_modslots] = ~class_ci_modknob_view[\make_modslots];

		~make_class_responder.(self, self.namelabel, self.controller, [
			\val, \label,
		]);

		modmixer = self.controller.get_player.modulation.get_modulation_mixer(self.controller.name);
		self.modmixer = { modmixer };

		~make_class_responder.(self, self.namelabel, modmixer, [
			\range, \connection
		]);
	
		debug("BLLA3");
		self;
	},

	val: { arg self;
		self.vallabel.string = self.controller.get_val.asFloat.asStringPrec(~general_sizes.float_precision);
		self.knob.value = self.controller.get_norm_val;
	},

	label: { arg self;
		self.controller.label.debug("class_ci_modknob_view: LABEL");
		self.controller.name.debug("class_ci_modknob_view: LABEL2");
		self.namelabel.string = self.controller.label ?? self.controller.name;
	},

	make_gui: { arg self;
		var label;
		var knob;
		var vallabel;
		var layout;
		layout = if(self.view_size.height > self.view_size.width) {
			VLayout;
		} {
			HLayout;
		};
		self.layout = layout.new(
			label = StaticText.new
				.align_(\centered)
				.string_(self.controller.label ?? self.controller.name)
				;
				[label, stretch:0, align:\center],
			knob = ModSlider.new(nil, self.view_size); 
				debug("BLLA");
				knob.action_({ 
					self.controller.set_norm_val(knob.value)
				});
				knob.asView.debug("SLIDERVIEW");
				knob.asView.minSize_(self.view_size.extent);
				//knob.asView.minSize_(50@50);
				[knob.asView, stretch: 1, align:\center],
			vallabel = StaticText.new
				.string_("12354")
				.font_(Font("Arial",11))
				.align_(\center)
				.minWidth_(75)
				//.minWidth_(75);
				;
				[vallabel, stretch:0, align:\center],
			[self.make_modslots, stretch:0, align:\center],
			nil
		);
		knob.rangeview.focusGainedAction = { arg me;
			//"fader focusGainedAction".debug;
			me.background = Color.gray(0.6);
			~global_controller.current_param = self.controller;
		};
		knob.rangeview.focusLostAction = { arg me;
			try {
				me.background = Color.clear;
			}
		};

		self.namelabel = label;
		self.knob = knob;
		self.vallabel = vallabel;
		0.01.wait;
		//self.layout.minHeight = 65;
		self.layout;
	},
);

~class_ci_popup_view = (
	new: { arg self, controller, size, action;
		self = self.deepCopy;
		self.controller = {controller};
		size = size ?? (80@20);

		self.popup = PopUpMenu.new(nil, size);
		self.popup.action = { arg popup;
			action.(popup);
			if(controller.notNil) {
				self.controller.set_val(self.popup.value);
			} {
				debug("class_popup_view: controller is nil");
			};
		};
		//[controller.model.uname, controller.menu_items].debug("class_popup_view");
		self.popup.items = try { 
			//controller.get_menu_items_names.debug("-----------controller.get_menu_items_names");
			controller.get_menu_items_names;
		} { 
			["default"]
		};
		self.popup.value = self.controller.get_val;

		self.layout = self.popup;

		~make_class_responder.(self, self.popup, self.controller, [ \val, \menu_items ]);
		0.01.wait;
		self;
	},

	val: { arg self;
		self.popup.value = self.controller.get_val;
	},

	menu_items: { arg self;
		self.popup.items = self.controller.get_menu_items_names;
		self.val;
	},
);

~class_ci_frame_view = (
	new: { arg self, label, knobs_ctrl, onoff_ctrl, popup1_ctrl, popup2_ctrl, fader_ctrl, display;
		self = self.deepCopy;

		self.label_string = label;
		self.knobs_ctrl = { knobs_ctrl };
		self.onoff_ctrl = { onoff_ctrl };
		self.popup1_ctrl = { popup1_ctrl };
		self.popup2_ctrl = { popup2_ctrl };
		self.fader_ctrl = { fader_ctrl };
		self.display = { display };
		self.make_gui;


		self;
	},

	val: { arg self;
		if(self.fader_ctrl.notNil) {
			self.fader.value = self.fader_ctrl.get_norm_val;
		}
	},

	make_gui: { arg self;
		var header, vheader;
		var body, vbody;
		var vframe;
		"ON EST LZ".debug;
		vframe = View.new;
		//self.fader = Slider.new
		//{

		self.knobs = self.knobs_ctrl.collect { arg ctrl;
			~class_ci_modknob_view.new(ctrl, self.display);
		};
		header = HLayout.new;
		if(self.onoff_ctrl.notNil) {
			header.add(
				self.onoff = Button.new
					.states_([
						["Off"],
						["On"],
					])
					.value_(self.onoff_ctrl.get_val)
					.action_({
						self.onoff_ctrl.set_val(self.onoff.value)
					})
					.maxWidth_(25)
					;
					self.onoff,
			)

		};
		header.add(
			self.framelabel = StaticText.new
				.string_(self.label_string);
				self.framelabel, 
		);
		if(self.popup1_ctrl.notNil) {
			header.add(
				self.popup1 = ~class_ci_popup_view.new(self.popup1_ctrl);
					self.popup1.layout,
			)
		};
		if(self.popup2_ctrl.notNil) {
			header.add(
				self.popup2 = ~class_ci_popup_view.new(self.popup2_ctrl);
					self.popup2.layout,
			)
		};
		body = HLayout(*
				self.knobs.collect(_.layout)
		);
		vheader = View.new;
		vheader.layout = header;
		vheader.background = Color.gray(0.6);

		vframe.background = Color.gray(0.7);
		vframe.layout = HLayout(
			VLayout(
				vheader,
				body,
			)
		);
		if(self.fader_ctrl.notNil) {
			vframe.layout.add(
				self.fader = Slider.new
				//self.fader
					.orientation_(\vertical)
					.action_({
						self.fader_ctrl.set_norm_val(self.fader.value)
					})
					;
					self.fader,
				stretch:0
			)
		};
		vframe.layout.margins = 5;
		header.margins = 3;
		self.layout = vframe;
		//vframe.layout.spacing = 1;

		if(self.fader_ctrl.notNil) {
			~make_class_responder.(self, self.layout, self.fader_ctrl, [
				\val,
			]);

		};

		//}.defer( rrand(1,4) );
		0.01.wait;

		self.layout;
	},
);

~class_knob_row = (
	new: { arg self, controllers;
		self = self.deepCopy;
		self.controllers = { controllers };
		self.make_layout;
	
		self;
	},

	make_responders: {

	},

	set_controllers: { arg self, controllers;
		self.controllers = { controllers };
		self.controllers_views.do { arg view, idx;
			view.set_controller = controllers[idx];
		};
		
	},

	make_layout: { arg self;
		self.controllers_views = self.controllers.collect { arg ctrl;
			~class_ci_modknob_view.new(ctrl);
		};
		self.layout = HLayout( * self.controllers_views.collect(_.layout) );
		self.layout;
	},
);
