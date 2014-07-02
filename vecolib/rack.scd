
(
{
	var lib = currentEnvironment;

	~class_rack = (
		event_class: \morpher,
		new: { arg self, node_name, name, specs;
			self = self.deepCopy;
			"heheyy==================================".debug;
		
			self.node_uname = node_name;
			self.name = name;
			self.set_params(specs);
			self.vals = IdentityDictionary.new;
			self.vals[\morph] = 0;
			
		
			self;
		},

		new_from_object: { arg self, object;
			var specs;
			switch(object.class,
				Ndef, {
					var specs = List.new;
					var name, node_name;
					object.controlNames.collect { arg con, x;
						specs.add([object, con.name])
					};
					node_name = \what;
					name = \hey;
					self.new(node_name, name, specs);
				},
				Pdef, {
					var node_name, name;
					var specs = List.new;
					var keys = Set.new;
					var instrument;
					var exclude_keys = [\out, '?']; // TODO: make it global
					node_name = \what;
					name = \hey;
					instrument = object.getHalo(\instrument);
					if(instrument.notNil) {
						var ispecs;
						ispecs = lib[\synthdef_name_to_specs2].(instrument);
						ispecs.debug("specs xxx");
						ispecs.clump(2).do { arg pairs;
							var key, spec;
							#key, spec = pairs;
							pairs.debug("pairs!!!");
							if(exclude_keys.includes(key).not) {
								pairs.debug("pairs!!!2");
								keys.add(pairs[0]);
								specs.add([object, key, spec]);
								if(object.get(key).isNil) {
									var default;
									default = switch(spec.class,
										XEnvSpec, {
											[spec.default]
										},
										XArraySpec, {
											[spec.default]
										},
										{
											spec.default;
										}
									);
									object.set(key, default);
								};
							}
						};
					};
					if(object.envir.notNil) {
						object.envir.keysValuesDo { arg key, val;
							if(keys.includes(key).not) {
								specs.add([object, key]);
							}
						};
					};
					self.new(node_name, name, specs);
				}
			);
		},

		set_params: { arg self, specs;
			if(specs[0].isSequenceableCollection.not) {
				specs = [specs]
			};
			self.params = specs;
			self.controllers = Dictionary.new;
			self.params_keys = List.new;
			self.params.do { arg param;
				var macro;
				var key;
				self.controllers.debug("kj");
				macro = lib[\class_macro_controller].new(param);
				key = macro.target_macrokey;
				[key,macro].debug("set_params: key, macro");
				self.controllers[key] = macro;
				self.params_keys.add(key)
			};
			
		},

		params_do: { arg self, fun;
			self.params_keys.do { arg paramkey, x;
				fun.(self.params[x], paramkey, self.controllers[paramkey]);
			};
		},

		save_preset: { arg self;
			var dict = Dictionary.new;
			self.controllers.keysValuesDo { arg key, cont, x;
				dict[key] = cont.target_get;
			};
			dict;
		},

		load_preset: { arg self, preset;
			preset.keysValuesDo { arg key, val;
				self.controllers[key].target_set(val);
			}
		},

		set_preset_list: { arg self, list;
			self.preset_list = list;
		},

		set_val: { arg self, key, val;
			self.vals[key] = val;
			switch(key,
				\morph, {
					self.morph(val, self.preset_list)
				}
			)
		},

		get_val: { arg self, key;
			self.vals[key]
		},

		morph: { arg self, morph_val, preset_list;
			var morphfactor = morph_val * (preset_list.size -1);
					//preset_list.debug("hhX");
			debug("hey");
			self.controllers.keysValuesDo { arg key, val;
				var vals, morphed;
				block { arg break;

					//preset_list.debug("hh");
					vals = preset_list.collect({ arg preset;
						if(preset.isNil) {
							key.debug("preset is nil: break!!");
							break.value
						} {
							preset[key]
						}
					});
					vals = vals.select(_.notNil);
					if(vals.size == 0) {
						key.debug("no param in preset: break!!");
						break.value;
					};
					morphfactor = morph_val * (vals.size-1);
					key.debug("morph key");
					vals.debug("morph vals");
					morphfactor.debug("morph factor");
					morphed = vals.blendAt(morphfactor);
					self.controllers[key].target_set(morphed);
				}
			};
			
		},

		map_midi_OLD: { arg self;
			var bindings = (1..8).collect(\k+++_);
			var x = 0;
			self.controllers.keysValuesDo { arg ckey, controller;
				var obj, key, spec;
				obj = controller.target;
				key = controller.target_key;
				spec = controller.target_spec;
				if(spec.isNil or: {spec.class == ControlSpec}) {
					MIDIBoard.map(bindings[x], obj, key, spec);
					x = x + 1;
				}
			}
		},

		map_midi: { arg self;
			var bindings = (1..8).collect(\k+++_);
			var x = 0;
			self.params.do { arg line;
				var obj, key, spec;
				#obj, key, spec = line;
				if(spec.isNil or: {spec.class == ControlSpec}) {
					MIDIBoard.map(bindings[x], obj, key, spec);
					x = x + 1;
				}
			}
		},

		make_window: { arg self;
			if(self.view.notNil) {
				self.view.window.close;
			};
			self.view = lib[\class_rack_view].new(self);
		},

		edit: { arg self;
			self.make_window
			
		},
	);

	~class_macro_controller = (
		parent: lib[\class_midi_cc_controller],
		new: { arg self, specs, name=\midicc, kind=\cc;
			self = self.deepCopy;
		
			[name, kind].debug("========================class_midi_cc_controller.new");
			self.tolerance = 0.03;
			self.name = name;
			self.kind = kind;
			self.changed(\new_map);
			self.target_wrappers = List.new;
			self.init;

			if(specs.notNil) {
				specs.debug("class_macro_controller: map specs");
				self.map(*specs);
			};

			
			self;
		},

		new_wrapper: { arg self, args, kind=\cc;
				var obj = args[0];
				var key = args[1];
				var spec = args[2];
				var parent = self;
				"wh2at".debug;
				switch(obj.class,
					Event, {
						"wh3at".debug;
						switch(obj.event_class,
							\morpher, {
								lib[\class_midi_cc_controller_for_morpher].new(self, obj, key, spec);
							},
							{
								lib[\class_midi_controller_for_patpreset].new(self, obj, key, spec);
							}
						)
					},
					Ndef, {
						"wh4at".debug;
						lib[\class_midi_controller_for_ndef].new(self, obj, key, spec);
					},
					Pdef, {
						"wh4at".debug;
						switch(spec.class,
							ControlSpec, {
								self.kind = \cc;
								lib[\class_midi_cc_controller_for_pdef].new(parent, obj, key, spec);
							},
							XArraySpec, {
								self.kind = \array;
								lib[\class_midi_array_controller_for_pdef].new(parent, obj, key, spec);
							},
							XEnvSpec, {
								self.kind = \env;
								lib[\class_midi_env_controller_for_pdef].new(parent, obj, key, spec);
							}, 
							Nil, {
								self.kind = \cc;
								lib[\class_midi_cc_controller_for_pdef].new(parent, obj, key, spec);
							},
							{
								//spec.class"ERROR: 5547744".postln;
								"ERROR: new_wrapper: spec not valid: %".format(spec).postln;
							}
						)
					},
					Function, {
						"wh5at".debug;
						// TODO
						lib[\class_midi_cc_controller_for_button].new(self, obj, key, spec);
					}
				);
		},
	);

	/////////////////////////////////////////////////////

	~void_controller = (
		target_string: "_",
		target_get: 0,
		target_norm_get: 0,
		get_val: 0,
		get_norm_val: 0,
		spec: \unipolar.asSpec,
		label: "_",
	);

	~class_rack_control_scalar_view = (
		new: { arg self, controller, parent;
			self = self.deepCopy;

			controller.debug("WHATTTTTTTTTTTTTTTT");
			if(controller.isNil) {
				controller = lib[\void_controller];
			};

			self.controller = { controller };
			self.precision = 0.0001;
			self.parent_view = {parent};
		
			self;
		},

		set_controller: { arg self, controller;
			if(controller.isNil) {
				controller = lib[\void_controller];
			};
			self.controller = { controller };
			self.update_view;
		},

		val: { arg self;
			var prec = self.precision;
			self.controller.get_val.debug("class_control_scalar_view: val handler");
			{
				var val = self.controller.get_val;
				val.debug("class_control_scalar_view: val");
				self.val_label.string = val.round(prec);
				self.controller.get_norm_val.debug("class_control_scalar_view: norm val");
				self.slider.value = self.controller.get_norm_val;
			}.defer;
		},

		update_view: { arg self;
			var prec = self.precision;
			{
				var val = self.controller.get_val;
				val.debug("class_control_scalar_view: update_view val");
				self.key_label.string = self.controller.label.asString;
				self.controller.get_norm_val.debug("class_control_scalar_view: update_view norm val");
				if(val.isNumber) {
					self.val_label.string = val.round(self.precision);
					self.slider.value = self.controller.get_norm_val;
				} {
					self.val_label.string = val.asString;
				};
			}.defer;
		},

		make_layout: { arg self;
			
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = self.precision;
			"what!!!!!!!!!! oscour!!!".debug;
			name = self.controller.label;
			//spec = self.controller.spec;
			//spec = spec ?? name.asSpec ?? \widefreq.asSpec;

			label = StaticText.new;
			//label.string = name.asString;
			name.debug("make_layout:name");
			label.minWidth = 160;

			val = StaticText.new;
			val.minWidth = 100;
			//val.string = self.controller.get_val.round(prec);

			slider = Slider.new(nil, Rect(20, 60, 150, 20));
			//slider.value = spec.unmap(self.controller.get_val);

			self.val_label = val;
			self.key_label = label;
			self.slider = slider;

			self.update_view;

			slider.action = { arg sli;
				var val;
				[sli.value, sli.value].debug("class_rack_control_scalar_view: slideraction: norm val, val");
				val = self.controller.get_val;
				if(val.isNumber) {
					self.controller.set_norm_val(sli.value);
					self.val_label.string = val.round(self.precision);
				} {
					self.val_label.string = val.asString;
				}
			};

			layout = HLayout.new(
				label, slider, val
			);
			self.layout = layout;

			if(self.parent_view.notNil) {
				lib[\make_class_responder].(self, self.parent_view.window, self.controller, [\val]);
			};

			layout;
		},
	);

	~class_rack_control_scalar_knob_view = (
		parent: lib[\class_rack_control_scalar_view],

		make_layout: { arg self;
			
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = self.precision;
			var font;
			"what!!!!!!!!!! oscour!!! knob".debug;
			name = self.controller.label;
			//spec = self.controller.spec;
			//spec = spec ?? name.asSpec ?? \widefreq.asSpec;
			font = Font.default;
			font.size = 11;

			label = StaticText.new;
			label.font = font;
			label.align = \center;
			//label.string = name.asString;
			name.debug("make_layout:name");
			//label.minWidth = 160;

			val = StaticText.new;
			val.font = font;
			val.align = \center;
			//val.minWidth = 100;
			//val.string = self.controller.get_val.round(prec);

			slider = Knob.new(nil);
			//slider.value = spec.unmap(self.controller.get_val);

			self.val_label = val;
			self.key_label = label;
			self.slider = slider;

			self.update_view;

			slider.action = { arg sli;
				[sli.value, sli.value].debug("class_rack_control_scalar_knob_view: slideraction: norm val, val");
				val = self.controller.get_val;
				if(val.isNumber) {
					self.controller.set_norm_val(sli.value);
					self.val_label.string = val.round(self.precision);
				} {
					self.val_label.string = val.asString;
				}
			};

			layout = VLayout.new(
				label, slider, val
			);
			self.layout = layout;

			if(self.parent_view.notNil) {
				lib[\make_class_responder].(self, self.parent_view.window, self.controller, [\val]);
			};

			layout;
		},

	);

	~class_rack_control_array_view = (
		new: { arg self, controller;
			self = self.deepCopy;

			self.controller = { controller };
			self.precision = 0.001;
		
			self;
		},

		set_controller: { arg self, controller;
			self.controller = { controller };
			self.update_view;
		},

		update_view: { arg self;
			//var spec = self.controller.spec.value[1];
			var prec = self.precision;
			{
				self.key_label.string = self.controller.label.asString;
				self.val_label.string = self.controller.get_val.round(prec);
				self.slider.value = self.controller.get_norm_val;
			}.defer;
		},

		make_layout: { arg self;
			
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = 0.001;
			"what!!!!!!!!!! oscour!!! array".debug;
			name = self.controller.label;
			if(spec.class != XArraySpec) {
				spec.debug("class_rack_control_array_view: make_layout: spec not valid");
			};
			//spec = self.controller.spec;
			//spec = spec ?? name.asSpec ?? \widefreq.asSpec;

			label = StaticText.new;
			//label.string = name.asString;
			name.debug("make_layout:name");
			label.minWidth = 160;

			val = StaticText.new;
			val.minWidth = 100;
			//val.string = self.controller.get_val;

			slider = MultiSliderView.new(nil, Rect(20, 60, 150, 20));
			//slider.value = spec.unmap(self.controller.get_val);

			self.val_label = val;
			self.key_label = label;
			self.slider = slider;

			self.update_view;

			slider.action = { arg sli;
				// TODO: beware, controller dont have spec :(
				//var spec = self.controller.spec.value[1];
				self.controller.set_norm_val(sli.value);
				self.val_label.string = self.controller.get_val.round(self.precision).asCompileString;
			};

			layout = VLayout.new(
				label, slider, val
			);
			self.layout = layout;
			layout;
		},
	);

	~class_rack_control_env_view = (
		new: { arg self, controller;
			self = self.deepCopy;
		
			self.controller = {controller.target_wrappers[0] };
		
			self;
		},

		set_controller: { arg self, controller;
			self.controller = { controller.target_wrappers[0] };
			self.update_view;
		},

		update_view: { arg self;
			{
				self.key_label.string = self.controller.label.asString;
				self.update_env;
				self.update_label;
				self.env_view.selectIndex(1);
				self.update_knobs;
			}.defer;
		},

		update_label: { arg self;
			self.env_label.string_(
				"%".format(
					lib[\env_to_string].(self.controller.get_val),
				)
			);
		},
		
		update_env: { arg self;
			self.env_view.value_(self.controller.get_norm_xy);
		},

		update_knobs: { arg self;
			var index = self.env_view.index;
			var env = self.controller.get_norm_val;
			var levels = env.levels;
			var times = env.times;
			if(levels[index].notNil) {
				self.knob_level.value = levels[index];
			};
			if(times[index].notNil) {
				self.knob_time.value = times[index];
			};
		},

		make_layout: { arg self;
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = 0.001;
			var knob_layout;
			var knob_size = 15;
			var level_spec, time_spec, curve_spec;
			"what!!!!!!!!!! oscour!!! env".debug;
			name = self.controller.label;

			//level_spec = self.controller.level_spec;
			//time_spec = self.controller.time_spec;
			//curve_spec = self.controller.curve_spec;

			label = StaticText.new;
			label.string = name.asString;
			label.minWidth = 160;

			val = StaticText.new;
			val.minWidth = 100;
			debug("zz1");
			val.string = self.controller.get_val;
			debug("zz2");
			

			slider = EnvelopeView.new(nil, Rect(0, 0, 230, 80))
				.drawLines_(true)
				.selectionColor_(Color.red)
				.drawRects_(true)
				.step_(0)
				.thumbSize_(10)
				.keepHorizontalOrder_(true)
				.value_(self.controller.get_norm_xy);
			debug("zz3");
			self.env_view = slider;
			self.env_view.selectIndex(1);
			self.env_view.action = { arg env;
				debug("xxx");
				if(env.index == 0 and: { env.x > 0 }) {
					env.x = 0;
					debug("1xxx");
				};
					debug("2xxx");
				self.controller.set_norm_xy(env.value);
					debug("3xxx");
				self.update_label;
					debug("4xxx");
				self.update_knobs;
					debug("5xxx");
			};
			debug("zz10");
			self.env_view.mouseDownAction = { arg view, x, y, mod;
				self.mouse_down_point = Point(x,y);
				self.mouse_down_index = view.index;
				self.mouse_down_time_scale = self.controller.time_scale;
			};
			self.env_view.mouseUpAction = { arg view, x, y, mod;
				self.update_knobs;
				if(mod.isShift) {
					self.env_view.selectIndex(self.mouse_down_index)
				}
			};
			self.env_view.mouseMoveAction = { arg view, x, y, mod;
				var val;
				if(mod.isShift) {
					val = x - self.mouse_down_point.x;
					self.controller.time_scale = self.mouse_down_time_scale + (val/500);
					self.update_env;
				}
			};
			self.env_view.keyDownAction = {
				self.update_knobs;
			};
			debug("zz15");

			self.knob_level = Knob.new
				.mode_(\vert)
				.maxWidth_(knob_size);
			self.knob_time = Knob.new
				.mode_(\vert)
				.maxWidth_(knob_size);
			self.knob_curve = Knob.new
				.mode_(\vert)
				.maxWidth_(knob_size);

			self.knob_level.action = { arg knob;
				var index = self.env_view.index;
				var res;
				res = self.controller.set_norm_level(index, knob.value);
				if(res) {
					self.update_label;
					self.env_view.y = knob.value;
				}
			};

			self.knob_time.action = { arg knob;
				var index = self.env_view.index;
				var res;
				res = self.controller.set_norm_time(index, knob.value);
				if(res) {
					var xy;
					xy = self.controller.get_norm_xy;
					xy.debug("XY!!!!");
					self.env_view.value_(xy);
					self.env_view.selectIndex(index);
					self.update_label;
				}
			};

			self.knob_curve.action = { arg knob;
				var index = self.env_view.index;
				var res = self.controller.set_norm_curve(index, knob.value);
				if(res) {
					self.update_label;
					self.env_view.curves = self.controller.get_val.curves;
				}
			};

			self.env_label = StaticText.new
				.font_(Font("Arial",10))
				.string_("--");

			self.val_label = self.env_label;
			self.key_label = label;

			knob_layout = HLayout.new(
				self.knob_level,
				self.knob_time,
				self.knob_curve,
				self.env_label
			);


			debug("zz20");
			self.update_label;
			self.update_knobs;
			debug("zz21");

			layout = VLayout.new(
				label, slider, knob_layout
			);
			self.layout = layout;
			layout;
			
		},

		make_window: { arg self;
			lib[\windowize].(self.make_layout);
		},
	);

	//////////////////////////

	~make_view_from_macro = { arg macro, parent;
		//var macro;
		debug("make_view_from_macrokey: FUCK");
		macro.target_spec_type.debug("make_view_from_macrokey: spec_type");
		switch(macro.kind,
			\cc, {
				"fuck".debug;
				lib[\class_rack_control_scalar_view].new(macro, parent);
			},
			\array, {
				lib[\class_rack_control_array_view].new(macro, parent);
			},
			\env, {
				lib[\class_rack_control_env_view].new(macro, parent);
			},
			{
				macro.kind.debug("Error, % kind not recognised");
				nil;
			}
		)
	};

	~make_view_from_macrokey = { arg macrokey;
		var macro;
		macro = lib[\class_macro_controller].new(macrokey);
		lib[\make_view_from_macro].(macro)
	};

	~class_rack_view = (
		new: { arg self, controller;
			self = self.deepCopy;
		
			self.controller = { controller };
			self.set_bus_mode(true);
			self.control_views = Dictionary.new;
			self.make_window;
		
			self;
		},

		set_bus_mode: { arg self, set=true;
			self.controller.set_bus_mode(set);
		},

		//set_controller: { arg self, controller;
		//	self.set_bus_mode(false);
		//	self.controller = { controller };
		//	self.set_bus_mode(true);
		//	self.controller.params.keysValuesDo { arg key, val;
		//		self.control_views[key].set_controller(val);
		//	};
		//	self.update_view;
		//},

		update_view: { arg self;
		//	{
		//		self.window.name = "Preset Editor -- node: %, index: %, subidx: %".format(
		//			self.controller.node_uname, 
		//			self.controller.index,
		//			self.controller.subindex
		//		);
		//	}.defer;
		},

		make_window: { arg self;
			var win;
			var layout;
			//win = Window.new("preset", Rect(200,200,500,550));
			Task({

				win = Window.new;
				self.window = win;
				win.view.keyDownAction = { arg view, char, modifiers, u, k; 
					[char, modifiers, u, k].debug("KEYBOARD INPUT");
					if( u == 27 ) { view.close() };
					//if( k == 67 ) { // F1
					//	self.controller.activate_display(self);
					//};
					//if( k == 68 ) { // F2
					//	self.controller.duplicate_current_score(self);
					//};
					//if( k == 69 ) { // F3
					//	self.controller.write_current_score_to_destination(self);
					//};
					//if( k == 70 ) { // F3
					//	self.controller.set_current_score_as_default(self);
					//};
					//if( u == ~keycode.kbspecial.enter ) { validate_action.(); close_window.(); };
				};

				self.layout = layout = HLayout.new;
				self.slider_layout = VLayout.new;
				self.array_layout = VLayout.new;

				layout.add(self.slider_layout);
				layout.add(self.array_layout);

				self.controller.params_do { arg macrokey, macrosymbol, ctrl;
					var key;
					var view;
					var kind;
					key = macrosymbol;
					kind = ctrl.kind;
					//if(\kind)
					[macrosymbol, kind].debug("class_rack_view: make_window: macrosymbol, kind");

					view = lib[\make_view_from_macro].(ctrl, self);
					self.array_layout.add(view.make_layout);
					0.01.wait;
					self.control_views[key] = view;
				};

				self.array_layout.add(nil);
				self.slider_layout.add(nil);

				self.update_view;

				win.onClose = {
					self.set_bus_mode(false);
					self.controller.save_data;
				};
				win.layout = layout;
				win.front;
			}).play(AppClock);
			
		},
	);

}.value;
)
