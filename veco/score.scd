
~event_score = { arg synthdef_name, name, specs;

};

~class_event_score = (
	new: { arg self, synthdef_name, name, specs;
		self = self.deepCopy;
	
		
	
		self;
	},

);

(
~edit_variable = { arg name, spec;
	var win, val, slider, label, layout;
	spec = spec ?? name.asSpec ?? \widefreq.asSpec;
	win = Window.new(name, Rect(200,200,500,50));
	label = StaticText.new;
	label.string = name.asString;
	val = StaticText.new;
	val.minWidth = 150;
	val.string = currentEnvironment[name];
	slider = Slider.new(nil, Rect(20, 60, 150, 20));
	slider.value = spec.unmap(currentEnvironment[name]);
	slider.action = {
		arg sli;
		currentEnvironment[name] = spec.map(sli.value);
		val.string = currentEnvironment[name];
	};
	layout = HLayout.new(
		label, slider, val
	);
	win.layout = layout;
	win.front;
};

//~myvar = 30;
//~edit_variable.(\myvar);
);

(
~edit_sdef_variable = { arg node_name, name, spec;
	var win, val, slider, label, layout;
	spec = spec ?? name.asSpec ?? \widefreq.asSpec;
	win = Window.new(name, Rect(200,200,500,50));
	label = StaticText.new;
	label.string = name.asString;
	val = StaticText.new;
	val.minWidth = 150;
	val.string = Sdef.get_value(node_name, name);
	slider = Slider.new(nil, Rect(20, 60, 150, 20));
	slider.value = spec.unmap(Sdef.get_value(node_name, name));
	slider.action = {
		arg sli;
		Sdef.set_value(node_name, name, spec.map(sli.value));
		val.string = Sdef.get_value(node_name, name);
	};
	layout = HLayout.new(
		label, slider, val
	);
	win.layout = layout;
	win.front;
};

//~myvar = 30;
//~edit_variable.(\myvar);
);

(

~mixer_channel = (
	new: { arg self;
		self = self.deepCopy;
	
			
	
		self;
	},

	set_controller: { arg self, controller;
		if(self.controller.notNil) {
			self.controller.save_data;
		};
		self.controller = controller;
	},

	update_view: { arg self;
		var prec = 0.0001;
		{
			if(self.controller.isNil) {
				self.slider.enabled = false;
				self.fader_slider.enabled = false;
				self.label.string = "----";
			} {
				self.slider.enabled = true;
				self.fader_slider.enabled = true;
				self.slider.value = self.controller.get_norm_amp_level.asFloat;
				self.fader_slider.value = self.controller.get_norm_volume_level.asFloat;
				self.val.string = self.controller.get_amp_level.round(prec);
				self.label.string = self.controller.get_name;
				self.update_send_knobs;
			};
		}.defer;
	},

	update_send_knobs: { arg self;
		self.knobs.do { arg kn, x;
			var val;
			val = self.controller.get_send_level(x);
			if(val.isNil) {
				kn.value = 0;
			} {
				kn.value = self.controller.get_send_level(x);
			}
		};
	},

	make_send_knobs: { arg self;
		self.knobs = List.new;
		
		4.do { arg x;
			var kn = Knob.new;
			self.knobs.add(kn);
			kn.action = { arg kn;
				kn.value;
				self.controller.set_send_level(x, kn.value);
			}
		};
		self.send_layout = VLayout.new(*self.knobs);
		self.send_layout;
	},

	make_layout: { arg self;
		var width = 80;
		var prec = 0.0001;
		self.layout = VLayout.new;
		self.layout_view = View.new;
		self.layout_in = VLayout.new;
		self.layout_view.background = Color.fromHexString("9ac1b3");
		self.slider = Slider.new(nil, Rect(0,0,width,300));
		self.fader_slider = Slider.new(nil, Rect(0,0,width,300));
		self.fader_slider.knobColor = Color.blue;
		self.label = StaticText.new(nil, Rect(0,0,width,30));
		self.val = StaticText.new(nil, Rect(0,0,width,30));
		self.val.minWidth = width;
		self.val.maxWidth = width;

		self.slider_layout = HLayout.new;

		// amp (pattern)

		self.slider.action = { arg sli;
			self.controller.set_norm_amp_level(sli.value);
			self.val.string = self.controller.get_amp_level.round(prec);
		};
		self.slider.keyDownAction = { arg x, y, z;
			[x,y,z].debug("xyz");
			y.dump;
			if(y == $d) {
				self.controller.set_amp_level(1.0);
				self.update_view;
			}
		};
		self.slider_layout.add(self.slider);

		// volume (Ndef)

		self.fader_slider.action = { arg sli;
			self.controller.set_norm_volume_level(sli.value);
		};
		self.fader_slider.keyDownAction = { arg x, y, z;
			[x,y,z].debug("xyzw");
			y.dump;
			if(y == $d) {
				self.controller.set_volume_level(1.0);
				self.update_view;
			}
		};
		self.slider_layout.add(self.fader_slider);

		self.layout.add(self.layout_view);
		self.layout_view.layout = self.layout_in;
		self.layout_in.add(self.label);
		self.layout_in.add(self.slider_layout);
		self.layout_in.add(self.val);
		self.layout_in.add(self.make_send_knobs);
		self.update_view;
		self.layout;
	},

);

~mixer_gui = (
	new: { arg self, controller;
		self = self.deepCopy;
	
		self.controller = controller;
		self.make_window;
	
		self;
	},

	update_view: { arg self;
		var xos = self.controller.clip_offset.x;
		8.do { arg x;
			var node = self.controller.get_node_by_index(x+xos);
			self.channels[x].set_controller(node);
			self.channels[x].update_view;
		};
	},

	clip_offset: { arg self;
		self.update_view;
	},

	make_window: { arg self;
		self.window = Window.new("Mixer", Rect(300,300,700,500));
		self.layout = HLayout.new;

		self.channels = 8.collect { arg i;
			var ch = ~mixer_channel.new;
			self.layout.add(ch.make_layout);
			ch;
		};
		self.window.layout = self.layout;
		self.responder_anchor = self.window;
		~make_class_responder.(self, self.responder_anchor, self.controller, [
			\clip_offset
		]);
		self.window.onClose = self.window.onClose.addFunc({
			8.do { arg x;
				if(self.channels[x].controller.notNil) {
					self.channels[x].controller.save_data;
				}
			}
		});
		self.window.view.keyDownAction = { arg x, y, z;
			//[x,y,y.ascii,z].debug("xyz");
			if(y.ascii == 27) {
				self.window.close;
			};
		};
		self.update_view;
		self.window.front;
	},
	
);

);






