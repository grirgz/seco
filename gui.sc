
~windowize = { arg layout;
	var win;
	win = Window.new;
	win.layout = layout;
	win.front;
};

~windowize_task = { arg layout;
	Task{
		var win;
		win = Window.new;
		win.layout = layout.value;
		win.front;
	}.play(AppClock)
};

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
				knob.mode = \horiz;
				knob.asView.debug("VIEW");
				knob.asView.minSize_(10@10);
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
	new: { arg self, controller, display=();
		var modmixer;
		self = self.deepCopy;

		self.display = { display };
		self.set_controller( controller );
		self.display.knobsize =  self.display.knobsize ?? (50@50) ;
		self.make_gui;
	
		self;
	},

	make_responders: { arg self;
		self.ctrl_responder.remove;
		self.mixer_responder.remove;
		if(self.controller.notNil and: { self.responder_anchor.notNil }) {

			self.ctrl_responder = ~make_class_responder.(self, self.responder_anchor, self.controller, [
				\midi_val, \val, \label,
			]);

			if(self.modmixer.notNil) {
				self.mixer_responder = ~make_class_responder.(self, self.responder_anchor, self.modmixer, [
					\range, \connection
				]);
			};

		};
		
	},

	////////// responders

	val: { arg self;
		{
			self.vallabel.string = self.controller.get_val.asFloat.asStringPrec(~general_sizes.float_precision);
			self.knob.value = self.controller.get_norm_val;
		}.defer;
	},

	midi_val: { arg self, ctrl, msg, val;
		{
			self.controller.midi.get_midi_norm_val.debug("class_ci_modknob_view:midi_val");
			self.knob.midi_value = self.controller.midi.get_midi_norm_val;
			self.knob.refresh;
		}.defer;
	},

	label: { arg self;
		(self.controller.isNil).debug("class_ci_modknob_view.label: controller.isnil ?");
		if(self.controller.isNil) {
			if(self.namelabel.notNil) {
				self.namelabel.string = "";
			}
		} {
			self.controller.label.debug("class_ci_modknob_view: LABEL");
			self.controller.name.debug("class_ci_modknob_view: LABEL2");
			self.namelabel.string = self.controller.label ?? self.controller.name;
		}
	},

	range: { arg self, obj, msg, idx;
		idx.debug("set range");
		{
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
		}.defer;
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
		(controller.isNil).debug("class_ci_modknob_view.set_controller: isnil ?");
		self.controller = { controller };
		if(self.controller.notNil) {
			if(self.controller.get_player.notNil and: { self.controller.get_player.modulation.notNil }) {
				modmixer = self.controller.get_player.modulation.get_modulation_mixer(self.controller.name);
				self.modmixer = { modmixer };
			}
		} {
			self.label;
		};
		self.make_responders;
	},

	clear_slot: { arg self, idx;
		idx.debug("class_ci_modknob_view: clear_slot");
		if(self.modmixer.notNil) {
			self.modmixer.disconnect_slot(idx);
			self.modmixer.set_range(idx, 0);
		};
	},	

	toggle_mute_slot: { arg self, idx;
		idx.debug("class_ci_modknob_view: toggle_mute_slot");
		if(self.modmixer.notNil) {
			if(self.modmixer.is_slot_muted(idx)) {
				self.modmixer.mute_slot(idx, false);
			} {
				self.modmixer.mute_slot(idx, true);
			}
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
				knob.asView.minSize_(self.display.knobsize);
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
			if(self.controller.notNil) {
				self.controller.name.debug("set global_controller");
				~global_controller.current_param = self.controller;
			};
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
		{
			self.vallabel.string = self.controller.get_val.asFloat.asStringPrec(~general_sizes.float_precision);
			self.knob.value = self.controller.get_norm_val;
		}.defer;
	},

	label: { arg self;
		{
			self.controller.label.debug("class_ci_modknob_view: LABEL");
			self.controller.name.debug("class_ci_modknob_view: LABEL2");
			self.namelabel.string = self.controller.label ?? self.controller.name;
		}.defer
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

		self.action = { action.(self.popup) };
		self.popup = PopUpMenu.new(nil, size);
		self.popup.action = { arg popup;
			if(controller.notNil) {
				self.controller.set_val(self.popup.value);
			} {
				debug("class_popup_view: controller is nil");
			};
			action.(popup);
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

~make_module_loader_button = { arg action;
	Button.new
		.states_(
			[["(o)"]]
		)
		.maxWidth_(20)
		.action_( action );
};

~onoff_button = { arg ctrl;
	var onoff;
	onoff = Button.new
		.states_([
			["Off"],
			["On"],
		])
		.value_(ctrl.get_val)
		.action_({
			ctrl.set_val(onoff.value)
		})
		.maxWidth_(25)
		;
	onoff;
};

~class_ci_frame_view = (
	//FIXME: should be named ~class_frame_view
	new: { arg self, label, knobs_ctrl, onoff_ctrl, popup1_ctrl, popup2_ctrl, fader_ctrl, display, loader;
		self = self.deepCopy;

		self.label_string = label;
		self.knobs_ctrl = { knobs_ctrl };
		self.onoff_ctrl = { onoff_ctrl };
		self.popup1_ctrl = { popup1_ctrl };
		self.popup2_ctrl = { popup2_ctrl };
		self.fader_ctrl = { fader_ctrl };
		self.display = { display };
		self.loader = { loader };
		self.make_gui;


		self;
	},

	val: { arg self;
		if(self.fader_ctrl.notNil) {
			self.fader.value = self.fader_ctrl.get_norm_val;
		}
	},

	make_body: { arg self;
		self.body = HLayout(*
				self.knobs.collect(_.layout)
		);
		self.body;
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
		self.header = header;
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

		if(self.loader.notNil) {
			header.add(
				~make_module_loader_button.(self.loader),
				1,
				stretch: 0
			);
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
		body = self.make_body;
		vheader = View.new;
		vheader.layout = header;
		vheader.background = ~color_scheme.frame_header;

		vframe.background = ~color_scheme.frame_body;
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

~class_frame_view_custom = (
	parent: ~class_ci_frame_view,
	new: { arg self, label, body, onoff_ctrl;
		self = self.deepCopy;

		self.label_string = label;
		self.body_ctrl = { body };
		self.onoff_ctrl = { onoff_ctrl };
		self.make_gui;


		self;
	},

	val: { arg self;
		if(self.fader_ctrl.notNil) {
			self.fader.value = self.fader_ctrl.get_norm_val;
		}
	},

	make_body: { arg self;
		self.body = self.body_ctrl.value;
		self.body;
	},
);


~class_inline_container_view = (

	new: { arg self, inline, display;
		var inline_node;
		self = self.deepCopy;

		self.inline_ctrl = { inline };
		//inline_node = inline.get_inline_node;

		self.make_gui;

		self.responder = ~make_class_responder.(self, self.body_view, self.inline_ctrl, [ \inline_node ]);

		self;
	},

	inline_node: { arg self;
		if(self.inline_ctrl.get_inline_node.notNil) {
			self.inline_ctrl.set_all_bus_mode(true);
			self.inline_ctrl.get_inline_node.module_loader = {
				{
					self.inline_ctrl.load_inline_node;
				}
			};
			self.reload_body;
			self.body_view.onClose = self.body_view.onClose.addFunc{
				self.inline_ctrl.set_all_bus_mode(false);
			}
		} {
			self.reload_body;
		}
	},

	make_body: { arg self;
		debug("class_inline_container_view:make_body");
		self.body_view = View.new;
		self.reload_body;
		self.body_view;
	},

	reload_body: { arg self;
		Task({
			debug("class_inline_container_view:reload_body");
			self.body_view.removeAll;
			if(self.inline_ctrl.get_inline_node.isNil) {
				self.body = View.new;
				self.body.maxHeight = 30;
				self.body.background = ~color_scheme.frame_header;
				self.body.layout = HLayout(
					[
						~make_module_loader_button.({
							self.inline_ctrl.load_inline_node;
						}),
						align: \left
					]
				);
				self.body.layout.margins = 3;
			} {
				self.body = self.inline_ctrl.get_inline_node.make_layout;
			};
			self.body_view.layout = HLayout(self.body);
			self.body_view;
		}).play(AppClock)
	},

	make_gui: { arg self;
		debug("class_inline_container_view:make_gui");
		self.layout = self.make_body;
		self.layout;
		
	},

);

~class_inline_frame_view = (
	parent: ~class_ci_frame_view,
	new: { arg self, inline, display;
		var inline_node;
		self = self.deepCopy;

		// TODO: get controllers from inline controller
		self.inline_ctrl = { inline };
		inline_node = inline.get_inline_node;

		self.label_string = inline_node.get_label;
		self.onoff_ctrl = inline_node.get_onoff_controller;
		self.popup1_ctrl = inline_node.get_popup1_controller;
		self.popup2_ctrl = inline_node.get_popup2_controller;
		self.fader_ctrl = inline_node.get_fader_controller;
		self.display = inline_node.get_display;

		self.make_gui;

		self.responder = ~make_class_responder.(self, self.body_view, self.inline_ctrl, [ \inline_node ]);

		self;
	},

	inline_node: { arg self;
		self.inline_ctrl.set_all_bus_mode(true);
		self.reload_body;
		self.body_view.onClose = self.body_view.onClose.addFunc{
			self.inline_ctrl.set_all_bus_mode(false);
		}
	},

	make_body: { arg self;
		debug("class_inline_frame_view:make_body");
		self.body_view = View.new;
		self.body_view;
	},

	reload_body: { arg self;
		Task({
			debug("class_inline_frame_view:reload_body");
			self.body_view.removeAll;
			self.body = self.inline_ctrl.get_inline_node.get_body_layout;
			self.body_view.layout = self.body;
			self.body_view;
		}).play(AppClock)
	},

	make_gui: { arg self;
		debug("class_inline_frame_view:make_gui");
		~class_ci_frame_view[\make_gui].(self);
		self.header.insert(
			Button.new
				.states_(
					[["IN"]]
				)
				.action_({
					self.inline_ctrl.load_inline_node;
				}),
			1,
			stretch: 0
		);
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
			view.set_controller( controllers[idx] );
		};
		
	},

	make_layout: { arg self;
		self.controllers_views = self.controllers.collect { arg ctrl;
			~class_ci_modknob_view.new(ctrl);
		};
		self.responder_anchor = self.controllers_views[0].namelabel;
		self.layout = HLayout( * self.controllers_views.collect(_.layout) );
		self.layout;
	},
);

~class_voices_panel = (
	new: { arg self, ci, keys;
		self = self.deepCopy;

		self.key_list = keys;
		self.classinstr = { ci };
	
		self;
	},

	make_layout: { arg self;
		var make_voice_control;
		make_voice_control = { arg key;
			var enable_key = "enable_%".format(key).asSymbol;
			enable_key.debug("class_voices_panel.make_layout: enable_key");
			HLayout(
				Button.new
					.states_([["Off"],["On"]])
					.value_(self.classinstr.param[enable_key].get_val)
					.action_({ arg bt; 
						self.classinstr.param[enable_key].set_val(bt.value);
					}),
				{
					var slider;
					slider = ~class_ci_modslider_view.new(self.classinstr.param[key], Rect(0,0,300,20));
					slider.namelabel.minWidth_(100);
					slider.layout;
				}.value
			)
		};
		self.layout = HLayout(
			VLayout(
				StaticText.new
					.string_("Voices:"),
				TextField.new
					.string_(self.classinstr.param[\voices].get_val)
					.action_({ arg field;
						self.classinstr.param[\voices].set_val(field.string.asInteger)
					}),
				self.spread_kind_layout = ~class_ci_popup_view.new(self.classinstr.param[\spread_kind]);
					self.spread_kind_layout.layout,
				nil
			),
			[
				VLayout(
					* self.key_list.collect { arg key;
						make_voice_control.(key)
					} ++ [
					
						HLayout(
							Button.new
								.states_([["Off"],["On"]])
								.enabled_(false)
								.value_(1)
								,
							{
								var slider;
								slider = ~class_ci_modslider_view.new(self.classinstr.param[\spread],Rect(0,0,300,20));
								slider.namelabel.minWidth_(100);
								slider.layout;
							}.value
							//~class_ci_modslider_view.new(self.data[\spread],Rect(0,0,300,20)).layout
						), 
						nil
					]
				), 
				stretch:1
			],

		);
		self.layout;
	},
);

~class_midi_slider = (
	new: { arg self, controller, orientation=\horizontal;
		self = self.deepCopy;

		self.orientation = orientation;
		self.make_layout;
		self.set_controller(controller);
	
		self;
	},

	set_controller: { arg self, controller;
		if(controller.notNil) {
			debug("class_midi_slider: set_controller");
			self.controller = { controller };	
			self.responder.remove;
			self.responder = ~make_class_responder.(self, self.responder_anchor, self.controller, [ \label, \val ]);
		} {
			debug("class_midi_slider: set_controller (isNil)");
			self.midi_label.string = "X";
			self.slider.value = 0.5;
		}
	},

	label: { arg self;
		{
			debug("class_midi_slider: label responder");
			self.midi_label.string_(self.controller.midi.label);
		}.defer;
	},

	val: { arg self;
		{
			debug("class_midi_slider: val responder");
			self.slider.value = self.controller.get_norm_val;	
		}.defer
	},

	make_layout: { arg self;
		var layout;
		if(self.orientation == \horizontal) {
			layout = HLayout.new;
		};
		self.midi_label = StaticText.new.string_("X");
		self.slider = Slider.new
			.orientation_(self.orientation)
			.focusGainedAction_({
				if(self.controller.notNil) {
					self.controller.name.debug("global_controller is now: ");
					~global_controller.current_param = self.controller;
				}
			})
			.action_({
				if(self.controller.notNil) {
					self.controller.set_norm_val(self.slider.value);
					self.slider_action;
				}
			});

		self.responder_anchor = self.midi_label;

		layout.add( self.midi_label );
		layout.add( self.slider );
		self.layout = layout;
		self.layout;
	},
);

~class_internal_modulator_gui = (
	new: { arg self, pitch_ctrl, modkinds;
		self = self.deepCopy;
	
		
		self.modkinds = { modkinds };
		self.pitch_ctrl = { pitch_ctrl };
		self.make_layout;
	
		self;
	},

	make_layout: { arg self;
		var frame;

		var pitch_knob;
		var mod_knob;
		var modkinds = self.modkinds;
		

		pitch_knob = ~class_ci_modknob_view.new(self.pitch_ctrl);
		mod_knob =   ~class_ci_modknob_view.new;

		frame = ~class_frame_view_custom.new("Internal modulator", {
			HLayout(
				pitch_knob.layout,
				mod_knob.layout,
				GridLayout.rows(*
					modkinds.collect { arg modkind;
						[ 
							Button.new
								.states_([
									[modkind.label, nil, Color.white],
									[modkind.label, nil, Color.gray],
								])
								.action_({ arg but;
									if(self.selected_button.notNil) {
										self.selected_button.value = 0;
									};
									mod_knob.set_controller(modkind.mod_ctrl);
									self.selected_button = but;
									self.selected_button.value = 1;
								})
						] ++ modkind.oscs.collect { arg osc, idx;
							[osc, modkind.label, idx].debug("OSC");
							~onoff_button.(osc);
						};
					}
				)
			)
			
		}
		);
		self.layout = HLayout(frame.layout);
		self.layout;
		
	},
);
