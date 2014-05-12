{
	var lib = currentEnvironment;

	// FIXME: use lib var instead of ~ everywhere (only inside classes)

	///// helpers

	~make_class_responder = { arg self, parent, model, list, auto_refresh=true;
		var controller;

		controller = SimpleController(model);
		list.do { arg method;
			controller.put(method, { arg ... args; self[method].(self, *args) });
		};

		parent.onClose = parent.onClose.addFunc { controller.remove };

		if(auto_refresh) { model.refresh() };

		controller;
	};

	~get_spec = { arg argName, defname=nil, default_spec=\widefreq;
		var spec = nil;
		//if(argName.beginsWith("adsr") or: argName.beginsWith("env"))
		try { 
			spec = if( SynthDescLib.global.synthDescs[defname].metadata.specs[argName].notNil, {
				var sp;
				sp = SynthDescLib.global.synthDescs[defname].metadata.specs[argName];
				if(sp.class == Association) {
					// \env -> [\freq.asSpec, \dur.asSpec]
					// \array -> [8, \unipolar.asSpec, default_val]
					sp;
				} {
					sp.asSpec;
				};
			})
		};
		if(spec.isNil, {
			if( argName.asSpec.notNil, {
				spec = argName.asSpec;
			}, {
				spec = default_spec.asSpec;
			});
		});
		//[argName, spec].debug("get_spec");
		spec;
	};
		
	~synthdef_name_to_specs = { arg defname;
		var specs;
		specs = List.new;

		SynthDescLib.global.synthDescs[defname].controls.do { arg x; 
			var spec;
			specs.add(x.name);
			spec = lib[\get_spec].(x.name, defname).copy;
			if(spec.class != Association and: { x.defaultValue.isSequenceableCollection }) {
				if(x.name == \adsr) {
					//specs.add(\env -> [nil, nil, nil, nil, x.defaultValue.copy]);
					// TODO: retrieve real default value
					specs.add(\env -> [nil, nil, nil, nil, Env.adsr(0.01,0.1,0.8,0.1)]);

				} {
					specs.add(\array -> [x.defaultValue.size, \unipolar.asSpec.copy, x.defaultValue.copy]);
				}
			} {
				if(spec.class == Association) {
					switch(spec.key,
						\env, {
							//spec.value[4] = x.defaultValue; // TODO: array to env
							specs.add(spec.copy);
						},
						\array, {
							spec.value[2] = x.defaultValue.copy;
							specs.add(spec.copy);
						}
					)
				} {
					spec.default = x.defaultValue.copy;
					specs.add(spec.copy);
				}
			};
		};
		specs;
	};

	~synthdef_name_to_specs2 = { arg defname;
		var specs;
		specs = List.new;

		if(SynthDescLib.global.synthDescs[defname].notNil) {

			SynthDescLib.global.synthDescs[defname].controls.do { arg x; 
				var spec;
				specs.add(x.name);
				spec = lib[\get_spec].(x.name, defname).copy;
				if([XEnvSpec, XArraySpec].includes(spec.class)) {
					specs.add(spec);
				} {
					if(spec.class != Association and: { x.defaultValue.isSequenceableCollection }) {
						if(x.name == \adsr) {
							//specs.add(\env -> [nil, nil, nil, nil, x.defaultValue.copy]);
							// TODO: retrieve real default value
							//specs.add(\env -> [nil, nil, nil, nil, Env.adsr(0.01,0.1,0.8,0.1)]);
							var default = Env.adsr(0.1,0.1,0.8,0.1);

							specs.add(XEnvSpec(\unipolar ! 4, ControlSpec(0.0001,8,\exp,0,0.1), \bipolar, default));
						} {
							specs.add(XArraySpec(\unipolar.asSpec.copy ! x.defaultValue.size, x.defaultValue.copy));
						}
					} {
						if(spec.class == Association) {
							switch(spec.key,
								\env, {
									//spec.value[4] = x.defaultValue; // TODO: array to env
									specs.add(spec.copy);
								},
								\array, {
									spec.value[2] = x.defaultValue.copy;
									specs.add(spec.copy);
								}
							)
						} {
							spec.default = x.defaultValue.copy;
							specs.add(spec.copy);
						}
					};
				}
			};
		};
		specs;
	};

	~windowize = { arg layout;
		var win;
		win = Window.new;
		win.layout = layout;
		win.front;
	};

	~env_to_string = { arg env;
		var prec = 5;
		"Env(%, %, %)".format(
			env.levels.collect({ arg x; x.asFloat.asStringPrec(prec)}),
			env.times.collect({arg x; x.asFloat.asStringPrec(prec)}),
			env.curves
		);
	};

	///// controls

	~class_control_ancestor = (
		valpattern: { arg self;
			self.vpattern;
		},

		buspattern: { arg self;
			self.vpattern;
		},

		get_event_val: { arg self;
			self.get_val;
		},

	);

	~class_control_scalar = (
		// FIXME: use lib var instead of ~ everywhere
		parent: ~class_control_ancestor,
		kind: \scalar,
		bus_mode: false,

		bus_mode_enabled: { arg self;
			//self.parent_controller.bus_mode_enabled
			true
			
		},

		new: { arg self, parent, name, spec, val;
			self = self.deepCopy;
		
			self.parent_controller = { parent };
			self.name = name;
			self.spec = spec;
			self.val = val ?? spec.default;
		
			self;
		},

		make_layout: { arg self;
			lib[\class_control_scalar_view].new(self).make_layout;
		},

		label: { arg self;
			self.name;
		},

		get_pat_val: { arg self;
			// return a scalar or a bus
			if(self.bus_mode_enabled and: self.bus_mode) {
				self.bus.asMap;
			} {
				if(self.bus.notNil) {
					self.bus.free;
					self.bus = nil;
				};
				self.val;	
			}
		},

		get_val: { arg self;
			// return a scalar
			self.val;	
		},

		get_norm_val: { arg self;
			self.spec.unmap(self.val);
		},

		set_bus_mode: { arg self, val;
			if(val != false) {
				self.bus_mode = true;
				if(self.bus.isNil) {
					self.bus = Bus.control(s, 1);
					self.bus.set(self.val);
				} {
					self.bus.set(self.val);
				}
			} {
				self.bus_mode = false;
			}
		},

		set_val: { arg self, val;
			self.parent_controller.is_present = true;
			self.val = val;
			//[self.bus_mode, self.bus, self.val].debug("scalar.set_val:mode, bus, val");
			if(self.bus_mode == true) {
				self.bus.set(val);
			};
			self.changed_hook(val);
			self.changed(\val);
		},

		vpattern: { arg self;
			// return a scalar or a bus
			if(self.name == \amp) {
				Pfunc { arg ev; self.get_val * ev[\amp] };
			} {
				Pfunc { self.get_pat_val };
			}
		},

		valpattern: { arg self;
			// return a scalar
			if(self.name == \amp) {
				Pfunc { arg ev; self.get_val * ev[\amp] };
			} {
				Pfunc { arg ev; self.get_val };
			}
		},

		buspattern: { arg self;
			// set busmode and return a bus
			if(self.name == \amp) {
				Pfunc { arg ev; self.get_val * ev[\amp] };
			} {
				self.set_bus_mode;
				Pfunc { self.get_pat_val };
			}
		},

	);

	~class_control_array = (
		parent: ~class_control_ancestor,
		kind: \array,
		new: { arg self, parent, name, spec, val;
			self = self.deepCopy;
		
			self.parent_controller = { parent };
			self.name = name;
			spec = spec.deepCopy;
			self.spec = spec;
			self.val = val.deepCopy ?? spec.value[2] ?? { spec.value[1].default ! spec.value[0] };
		
			self;
		},

		label: { arg self;
			self.name;
		},

		get_val: { arg self;
			self.val;	
		},

		set_val: { arg self, val;
			self.parent_controller.is_present = true;
			self.val = val;
		},
		
		get_event_val: { arg self;
			[self.get_val];
		},


		vpattern: { arg self;
			Pfunc { [ self.get_val ] };
		},
	);
	
	~class_control_env = (
		parent: ~class_control_ancestor,
		kind: \env,
		new: { arg self, parent, name, spec, val;
			self = self.deepCopy;
		
			self.parent_controller = { parent };
			spec = spec.value.deepCopy;
			self.spec = spec ?? [3, \freq.asSpec, ControlSpec(0.01,8,\exp,0,0.1), ControlSpec(-9,9,\lin,0,0), Env([100,200,50],[0.1,0.1])];

			self.name = name;

			self.spec[1] = self.spec[1] ?? \unipolar.asSpec;
			self.spec[2] = self.spec[2] ?? ControlSpec(0.01,8,\exp,0,0.1);
			self.spec[3] = self.spec[3] ?? ControlSpec(-9,9,\lin,0,0);

			self.get_env = val.deepCopy ?? self.spec[4] ?? { Env(
				self.spec[1].default ! self.spec[0], 
				self.spec[2].default ! (self.spec[0]-1),
				self.spec[3].default,
			) };

			self.level_spec = self.spec[1];
			self.time_spec = self.spec[2];
			self.curve_spec = self.spec[3];
		
			self;
		},

		get_val: { arg self;
			self.get_env;
		},

		set_val: { arg self, val;
			self.parent_controller.is_present = true;
			self.get_env = val;
		},

		label: { arg self;
			self.name;
		},

		get_xy: { arg self;
			var x = List.new;
			var y = List.new;
			var env = self.get_env;
			env.levels.do { arg lvl, n;
				y.add(lvl);
				if(n == 0) {
					x.add(0);
				} {
					x.add(env.times[n-1])
				}
			};
			[x.integrate.asArray,y.asArray]
		},
		time_scale: 1/2,

		get_norm_xy: { arg self, time_scale;
			var x = List.new;
			var y = List.new;
			var env = self.get_env;
			var times, levels;
			var res;
			time_scale = time_scale ?? self.time_scale;
			times = self.time_spec.unmap(env.times);
			levels = self.level_spec.unmap(env.levels);
			x = times;
			y = levels;
			x = x.insert(0, 0);
			res = [x.integrate.asArray * time_scale,y.asArray];
			res
		},

		set_norm_xy: { arg self, val, time_scale;
			var times, levels;
			self.parent_controller.is_present = true;
			time_scale = time_scale ?? self.time_scale;
			#times, levels = val;
			times = times.copy;
			times.removeAt(0);
			times = times / time_scale;
			times = times.differentiate;
			times = self.time_spec.map(times);
			levels = self.level_spec.map(levels);
			self.get_env.times = times;
			self.get_env.levels = levels;
		},

		edit: { arg self;
			lib[\env_editor].new(self).make_window;
		},

		vpattern: { arg self, repeat=inf;
			Pfuncn ({ [self.get_env] }, repeat);
		},

		get_event_val: { arg self;
			[self.get_env];
		},
	);

	///// views

	~class_control_scalar_view = (
		new: { arg self, controller, parent;
			self = self.deepCopy;

			self.controller = { controller };
			self.precision = 0.0001;
			self.parent_view = {parent};
		
			self;
		},

		set_controller: { arg self, controller;
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
				self.val_label.string = val.round(prec);
				self.controller.get_norm_val.debug("class_control_scalar_view: update_view norm val");
				self.slider.value = self.controller.get_norm_val;
			}.defer;
		},

		make_layout: { arg self;
			
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = self.precision;
			name = self.controller.label;
			spec = self.controller.spec;
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
				[sli.value, spec.map(sli.value)].debug("class_control_scalar_view: slideraction: norm val, val");
				self.controller.set_val(spec.map(sli.value));
				self.val_label.string = self.controller.get_val.round(self.precision);
			};

			layout = HLayout.new(
				label, slider, val
			);
			self.layout = layout;

			lib[\make_class_responder].(self, self.parent_view.window, self.controller, [\val]);

			layout;
		},
	);

	~class_control_array_view = (
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
			var spec = self.controller.spec.value[1];
			var prec = self.precision;
			{
				self.key_label.string = self.controller.label.asString;
				self.val_label.string = self.controller.get_val.round(prec);
				self.slider.value = spec.unmap(self.controller.get_val);
			}.defer;
		},

		make_layout: { arg self;
			
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = 0.001;
			name = self.controller.label;
			spec = self.controller.spec.value[1];
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
				var spec = self.controller.spec.value[1];
				self.controller.set_val(spec.map(sli.value));
				self.val_label.string = self.controller.get_val.round(self.precision).asCompileString;
			};

			layout = VLayout.new(
				label, slider, val
			);
			self.layout = layout;
			layout;
		},
	);

	~class_control_env_view = (
		new: { arg self, controller;
			self = self.deepCopy;
		
			self.controller = {controller};
		
			self;
		},

		set_controller: { arg self, controller;
			self.controller = { controller };
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
					lib[\env_to_string].(self.controller.get_env),
				)
			);
		},
		
		update_env: { arg self;
			self.env_view.value_(self.controller.get_norm_xy);
		},

		update_knobs: { arg self;
			var index = self.env_view.index;
			var levels = self.controller.get_env.levels;
			var times = self.controller.get_env.times;
			var level_spec = self.controller.level_spec;
			var time_spec = self.controller.time_spec;
			if(levels[index].notNil) {
				self.knob_level.value = level_spec.unmap(levels[index]);
			};
			if(times[index].notNil) {
				self.knob_time.value = time_spec.unmap(times[index]);
			};
		},

		make_layout: { arg self;
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = 0.001;
			var knob_layout;
			var knob_size = 15;
			var level_spec, time_spec, curve_spec;
			name = self.controller.label;

			level_spec = self.controller.level_spec;
			time_spec = self.controller.time_spec;
			curve_spec = self.controller.curve_spec;

			label = StaticText.new;
			label.string = name.asString;
			label.minWidth = 160;

			val = StaticText.new;
			val.minWidth = 100;
			val.string = self.controller.get_val;
			

			slider = EnvelopeView.new(nil, Rect(0, 0, 230, 80))
				.drawLines_(true)
				.selectionColor_(Color.red)
				.drawRects_(true)
				.step_(0)
				.thumbSize_(10)
				.keepHorizontalOrder_(true)
				.value_(self.controller.get_norm_xy);
			self.env_view = slider;
			self.env_view.selectIndex(1);
			self.env_view.action = { arg env;
				if(env.index == 0 and: { env.x > 0 }) {
					env.x = 0;
				};
				self.controller.set_norm_xy(env.value);
				self.update_label;
				self.update_knobs;
			};
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
				var levels = self.controller.get_env.levels;
				if(levels[index].notNil) {
					levels[index] = level_spec.map(knob.value);
					self.controller.get_env.levels = levels;
					self.env_view.y = knob.value;
					self.update_label;
				};
			};
			self.knob_time.action = { arg knob;
				var index = self.env_view.index;
				var times = self.controller.get_env.times;
				if(times[index].notNil) {
					times[index] = time_spec.map(knob.value);
					self.controller.get_env.times = times;
					self.env_view.value_(self.controller.get_norm_xy);
					self.env_view.selectIndex(index);
					self.update_label;
				}
			};
			self.knob_curve.action = { arg knob;
				self.controller.get_env.curves = curve_spec.map(knob.value);
				self.env_view.curves = curve_spec.map(knob.value);
				self.update_label;
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


			self.update_label;
			self.update_knobs;

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


	~class_node_preset_view = (
		new: { arg self, controller;
			self = self.deepCopy;
		
			self.controller = { controller };
			self.set_bus_mode(true);
			self.control_views = IdentityDictionary.new;
			self.make_window;
		
			self;
		},

		set_bus_mode: { arg self, set=true;
			self.controller.set_bus_mode(set);
		},

		set_controller: { arg self, controller;
			self.set_bus_mode(false);
			self.controller = { controller };
			self.set_bus_mode(true);
			self.controller.vals.keysValuesDo { arg key, val;
				key.debug("class_node_preset_view: set_controller: key");
				if(self.control_views[key].isNil) {
					[self.controller.node_uname, self.controller.name].debug("Warning: class_node_preset_view: control view is Nil");
				} {
					self.control_views[key].set_controller(val);
				}
			};
			self.update_view;
		},

		update_view: { arg self;
			{
				self.window.name = "Preset Editor -- node: %, index: %, subidx: %".format(
					self.controller.node_uname, 
					self.controller.index,
					self.controller.subindex
				);
			}.defer;
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
					if( k == 67 ) { // F1
						self.controller.activate_display(self);
					};
					if( k == 68 ) { // F2
						self.controller.duplicate_current_score(self);
					};
					if( k == 69 ) { // F3
						self.controller.write_current_score_to_destination(self);
					};
					if( k == 70 ) { // F3
						self.controller.set_current_score_as_default(self);
					};
					//if( u == ~keycode.kbspecial.enter ) { validate_action.(); close_window.(); };
				};

				self.layout = layout = HLayout.new;
				self.slider_layout = VLayout.new;
				self.array_layout = VLayout.new;

				layout.add(self.slider_layout);
				layout.add(self.array_layout);

				self.controller.specs.clump(2).do { arg pair;
					var key, ctrl;
					var view;
					key = pair[0];
					ctrl = self.controller.vals[key];
					switch(ctrl.kind,
						\array, {
							view = lib[\class_control_array_view].new(ctrl, self);
							self.array_layout.add(view.make_layout);
						},
						\env, {
							view = lib[\class_control_env_view].new(ctrl, self);
							self.array_layout.add(view.make_layout);
						},
						\scalar, {
							view = lib[\class_control_scalar_view].new(ctrl, self);
							self.slider_layout.add(view.make_layout);
						},
						{
							ctrl.kind.debug("Error, % kind not recognised");
							view = nil;
						}
					);
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

	////////////////////////////////////////


	~class_preset = (
		new: { arg self, specs;
			var extra_specs = [];
			var parent;
			self = self.deepCopy;

			parent = (
				spec: specs
			);

			self.name = parent.name;
			self.node_uname = parent.node_uname;
			//parent.spec.debug("pSPECS");
			self.specs = parent.spec;
			self.index = 0;
			self.subindex = 0;
			self.parent_collection = { parent };
			self.vals = IdentityDictionary.new;
		
			self.build_vals;
			self.build_pbind;
			self.is_present = false;


			//self.load_data; // doesnt exists, called on collection level

			self;
		},


		new_from_score: { arg self, parent, index, subindex, score;
			var specs;
			var extra_specs = [];
			self = self.deepCopy;


			self.name = parent.name;
			self.node_uname = parent.node_uname;
			//parent.spec.debug("pSPECS from score");
			self.specs = parent.spec;
			self.index = index;
			self.subindex = subindex;
			self.parent_collection = { parent };
			self.vals = IdentityDictionary.new;
		
			//self.build_vals;

			self.load_data_from_score(score);
		
			self;
		},

		load_data_from_score: { arg self, score;
			self.set_data(score.save_data_to_struct.deepCopy)
		},

		rebuild: { arg self, clear_old=false;
			self.specs = self.parent_collection.spec;
			self.specs.debug("rebuild: specs");
			self.build_vals(clear_old);
			self.build_pbind;
		},

		//load_data_from_struct: { arg self, struct;
		//	self.specs = struct.specs;
		//	self.data = struct.data;
		//},

		save_data_to_struct: { arg self;
			var specs, data;
			specs = self.specs;
			data = self.specs.clump(2).collect { arg pair;
				var key = pair[0];
				var val = self.vals[key];
				[key, val.get_val]
			};
			(
				version: 1,
				specs: specs,
				data: data.flatten,
			)
		},

		save_data: { arg self;
			self.parent_collection.save_data;
		},

		set_data: { arg self, data;
			self.is_present = true;
			self.data = data;
			self.specs = data.specs;
			self.build_vals;
			data.data.clump(2).do { arg pair;
				var key = pair[0];
				var val = pair[1];
				self.vals[key].set_val(val);
			};
			self.build_pbind;
		},

		build_vals: { arg self, clear_old=false;
			var specs = self.specs;
			var vals;
			specs.debug("SPECS");
			self.specs.debug("SPECS: sfou dmagueul");
			if(specs.isSymbolWS) {
				specs = self.synthdef_name_to_specs(specs);
				specs.debug("SPECS: after synthdef_name_to_specs");
			};

			//if(extra_specs.isSymbol) {
			//	specs = self.synthdef_name_to_specs(specs)
			//};
			//specs = specs ++ extra_specs;
			self.specs = specs;
			vals = IdentityDictionary.new;
			self.specs_dict = IdentityDictionary.new;
			specs.clump(2).do { arg pair;
				var key = pair[0];
				var spec = pair[1];
				var oldval = if(self.vals[key].notNil) { self.vals[key].get_val } { nil };
				//[key, oldval].debug("build_vals: key, oldval");
				self.specs_dict[key] = spec;
				if(spec.class == Association) {
					//
					switch(spec.key,
						\array, {
							vals[key] = lib[\class_control_array].new(self, key, spec, oldval)
						},
						\env, {
							vals[key] = lib[\class_control_env].new(self, key, spec, oldval)
						},
						\adsr, {

						}
					
					);
				} {
					vals[key] = lib[\class_control_scalar].new(self, key, spec, oldval);
				};
			};
			//vals.keys.debug("VALS");
			if(clear_old) {
				self.vals = vals;
			} {
				self.vals.putAll(vals);
			}
			
		},

		label: { arg self;
			"%.%.%".format(self.node_uname, self.index+1, self.name);
		},

		synthdef_name_to_specs: { arg self, defname;
			var reject = [\doneAction, \out, \gate, '?', \bufnum];
			var res = lib[\synthdef_name_to_specs].(defname);
			res = res.clump(2).reject({ arg x; 
				//x[0].dump.postln; 
				reject.includes(x[0])
			}).flatten;
			res;
		},


		edit: { arg self;
			self.score_view = lib[\class_node_preset_view].new(self);
		},

		write_current_score_to_destination: { arg self, view;
			var pos, idx, subidx;
			var dest_score;
			pos = self.score_preset_destination_pos;
			if(pos.notNil) {
				idx = pos.y;
				subidx = pos.x;
				dest_score = self.parent_collection.get(idx, subidx);
				dest_score.load_data_from_score(self);
				self.score_preset_destination_pos = nil;
				view.set_controller(dest_score);
				self.activate_display(view);
			} {
				"write_current_score_to_destination: dest pos is nil".debug;
			}
		},

		set_current_score_as_default: { arg self;
			[self.index, self.subindex].debug("set_current_score_as_default");
			self.parent_collection.set_active_subindex(self.index, self.subindex);
		},

		set_bus_mode: { arg self, set=true;
			self.vals.keysValuesDo { arg key, val;
				val.set_bus_mode(set);
			};
		},

		build_pbind: { arg self;
			var binds = List.new;
			self.specs.clump(2).do { arg pair;
				var key = pair[0];
				var val = self.vals[key];
				//[pair, key, val, self.vals.keys].debug("PAIR");
				if(val.notNil) {
					//[pair].debug("PAIRiiii");
					binds.add(key);
					binds.add(val.vpattern);
				} {

				}
			};
			//binds = binds.flat;
			self.build_apbind; // what ??
			self.build_valpbind; // no busses
			self.pbind = Pbind(*binds);
			self.pbind;
		},

		buspbind: { arg self;
			self.set_bus_mode(true);
			self.pbind;
		},

		build_apbind: { arg self;
			// what is it for ???
			var binds = List.new;
			self.specs.clump(2).do { arg pair;
				var key = pair[0];
				var val = self.vals[key];
				//[pair, key, val, self.vals.keys].debug("PAIR");
				if(val.notNil) {
					//[pair].debug("PAIRiiii");
					binds.add(key);
					binds.add(val.vpattern);
				} {

				}
			};
			//binds = binds.flat;
			self.apbind = Pbind(*binds);
			self.apbind;
		},

		build_valpbind: { arg self;
			// this pattern never yield a bus
			var binds = List.new;
			self.specs.clump(2).do { arg pair;
				var key = pair[0];
				var val = self.vals[key];
				//[pair, key, val, self.vals.keys].debug("PAIR");
				if(val.notNil) {
					//[pair].debug("PAIRiiii");
					binds.add(key);
					binds.add(val.valpattern);
				} {

				}
			};
			//binds = binds.flat;
			self.valpbind = Pbind(*binds);
			self.valpbind;
		},

		get_event: { arg self;
			var binds = List.new;
			var event;
			self.specs.clump(2).do { arg pair;
				var key = pair[0];
				var val = self.vals[key];
				//[pair, key, val, self.vals.keys].debug("PAIR");
				if(val.notNil) {
					//[pair].debug("PAIRiiii");
					binds.add(key);
					binds.add(val.get_event_val);
				} {

				}
			};
			//binds = binds.flat;
			event = ();
			event.putPairs(binds);
			event;
		},

		print_pbind: { arg self;
			var binds = List.new;
			var ret;
			self.specs.clump(2).do { arg pair;
				var key = pair[0];
				var val = self.vals[key];
				//[pair, key, val, self.vals.keys].debug("PAIR");
				if(val.notNil) {
					//[pair].debug("PAIRiiii");
					binds.add(key);
					binds.add(val.get_event_val);
				} {

				}
			};
			//binds = binds.flat;
			ret = Pbind(*binds).asCompileString;
			ret = ret
				.replace(" '", "\n\t'")
				.replace("Pbind(", "Pbind(\n\t")
				.drop(-1) ++ "\n)";
			ret.postln;
		},

		set_val: { arg self, key, val;
			self.vals[key].set_val(val)
		},

		get_val: { arg self, key;
			self.vals[key].get_val
		},

		get_active_score: { arg self; self },
	);

	~class_preset_morpher = (
		parent: lib[\class_preset],
		new: { arg self, specs;
			var extra_specs = [];
			var parent;
			self = self.deepCopy;

			parent = (
				spec: specs
			);

			self.name = parent.name;
			self.node_uname = parent.node_uname;
			//parent.spec.debug("pSPECS");
			self.specs = parent.spec;
			self.index = 0;
			self.subindex = 0;
			self.parent_collection = { parent };
			self.vals = IdentityDictionary.new;
		
			self.build_vals;
			self.build_pbind;
			self.is_present = false;


			//self.load_data; // doesnt exists, called on collection level

			self;
		},

		set_targets: { arg self, list;
			self.targets = list;
		},

		set_morph: { arg self, morph;
			self.morph_value = morph;
			self.update_morphing;
		},

		update_morphing: { arg self;
			var morph;
			if(self.morph_value.isNil) {
				self.morph_value = 0;
			};
			morph = self.morph_value * (self.targets.size-1);
			self.vals.keysValuesDo { arg key, val;
				var morphed;
				var vals = self.targets.collect({ arg target;
					if(target.isNil) {
						"Error: set_morph: target is nil".debug;
						0
					} {
						//[target.keys, key].debug("update_morphing: target, key");
						target.get_val(key)
					}
				});
				morphed = vals.blendAt(morph);
				if(key == \adsr) {
					[self.morph_name, vals, morphed].debug("self.update_morphing: adsr")
				};
				val.set_val(morphed);
			};
			
		},



	);

}.value;
