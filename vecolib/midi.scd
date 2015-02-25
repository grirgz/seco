

{
	var lib = currentEnvironment;

	~class_midi_cc_controller = (
		source_uid: nil,
		// TODO: refactor to include source_uid in constructor
		new: { arg self, ccnum, name=\midicc, channel=1, kind=\cc;
			self = self.deepCopy;
		
			[ccnum, name, channel, kind].debug("========================class_midi_cc_controller.new");
			self.tolerance = 0.03;
			self.name = name;
			self.kind = kind;
			self.channel = channel;
			self.ccnum = ccnum;
			self.midi_val = -10;
			self.changed(\new_map);
			self.target_wrappers = List.new;
			self.install_midi_responder;
			self.init;

			
			self;
		},

		// maybe not necessary
		//source_uid_: { arg self, val;
		//	self.source_uid = val;
		//	self.install_midi_responder;
		//},

		init: { arg self;
			self.get_val = self[\target_get];
			self.get_norm_val = self[\target_norm_get];
			self.set_val = self[\target_set];
			self.set_norm_val = self[\target_norm_set];
		},

		spec: { arg self; self.target_spec },
		label: { arg self; self.target_string },

		is_mapped: { arg self;
			//[self.name,self.target_wrappers].debug("target_string");
			if(self.target_wrappers.size > 0) {
				true
			} {
				false
			}
		},

		target_spec: { arg self;
			if(self.target_wrappers.size > 0) {
				if(self.target_wrappers.size > 1) {
					debug("should not call this because i dont know how to handle multi specs :(");
					nil.dsldfklsdfkljsldfkjs;
				} {
					self.target_wrappers[0].target_spec;
				}
			} {
				debug("target_spec called but there is no wrappers :(");
				"xyz";
			}
		},

		target_macrokey: { arg self;
			if(self.target_wrappers.size > 0) {
				if(self.target_wrappers.size > 1) {
					self.target_wrappers.collect(_.target_macrokey);
				} {
					self.target_wrappers[0].target_macrokey;
				}
			} {
				debug("macrokey called but there is no wrappers :(");
				"xyz";
			}
		},

		target_string: { arg self;
			//[self.name,self.target_wrappers].debug("target_string");
			if(self.target_wrappers.size > 0) {
				if(self.target_wrappers.size > 1) {
					"Mixed"
				} {
					self.target_wrappers[0].target_string
				}
			} {
				"_"
			}
		},

		target_norm_get: { arg self;
			if(self.target_wrappers.size > 0) {
				if(self.target_wrappers.size > 1) {
					self.target_wrappers[0].target_norm_get
				} {
					self.target_wrappers[0].target_norm_get
				}
			} {
				0
			}
		},
	
		target_get: { arg self;
			if(self.target_wrappers.size > 0) {
				if(self.target_wrappers.size > 1) {
					self.target_wrappers[0].target_norm_get
				} {
					self.target_wrappers[0].target_get
				}
			} {
				0
			}
		},

		target_norm_set: { arg self, val;
			self.target_wrappers.do { arg wrapper;
				wrapper.target_norm_set(val)
			}
		},

		target_set: { arg self, val;
			self.target_wrappers.do { arg wrapper;
				wrapper.target_set(val)
			}
		},

		target_key: { arg self;
			if(self.target_wrappers.size > 0) {
				if(self.target_wrappers.size > 1) {
					self.target_wrappers[0].target_key
				} {
					//self.target_wrappers[0].target_key
					\mixed
				}
			} {
				\_
			}
			
		},
	

		install_midi_responder: { arg self;
			var defname = "midiboard_%".format(self.name).asSymbol;
			if(self.kind == \cc) {
				self.responder = MIDIdef.cc(defname, { arg val,b,c,d;
					[defname, val, b, c, d].debug("responder");
					self.target_wrappers.do { arg wrapper;
						wrapper.handle_midi_message(val, b,c,d);
					};
					//self.midi_val = val/127; 
				}, self.ccnum, self.channel, self.source_uid);
				self.responder.permanent = MIDIBoard.permanent;

			} {
				self.responder = MIDIdef.noteOn(defname, { arg val,b,c,d;
					[defname, val, b, c, d].debug("responder");
					self.target_wrappers.do { arg wrapper;
						wrapper.handle_midi_message(val, b,c,d);
					};
					self.midi_val = val/127;
				}, self.ccnum, self.channel, self.source_uid);
				self.responder.permanent = MIDIBoard.permanent;
			};
		},

		new_wrapper: { arg self, args, kind=\cc;
				var obj = args[0];
				var key = args[1];
				var spec = args[2];
				"wh2at".debug;
				switch(obj.class,
					Event, {
						"wh3at".debug;
						switch(obj.event_class,
							\morpher, {
								lib[\class_midi_cc_controller_for_morpher].new(self, obj, key, spec);
							},
							{
								if(kind == \cc) {
									lib[\class_midi_cc_controller_for_patpreset].new(self, obj, key, spec);
								} {
									"Error: MIDIBoard.map: midinum is not a cc".postln;
									nil
								}
							}
						)
					},
					Ndef, {
						"wh4at".debug;
						if(kind == \cc) {
							if(key == "vol") {
								lib[\class_midi_cc_controller_for_ndef_volume].new(self, obj, key, spec);
							} {
								lib[\class_midi_cc_controller_for_ndef].new(self, obj, key, spec);
							};
						} {
							"Error: MIDIBoard.map: midinum is not cc".postln;
							nil
						}
					},
					Pdef, {
						"wh4at".debug;
						if(kind == \cc) {
							lib[\class_midi_cc_controller_for_pdef].new(self, obj, key, spec);
						} {
							"Error: MIDIBoard.map: midinum is not cc".postln;
							nil
						}
					},
					Volume, {
						"wh4at".debug;
						if(kind == \cc) {
							lib[\class_midi_cc_controller_for_volume].new(self, obj, key, spec);
						} {
							"Error: MIDIBoard.map: midinum is not cc".postln;
							nil
						}
					},
					Function, {
						"wh5at".debug;
						lib[\class_midi_cc_controller_for_button].new(self, obj, key, spec);
					}
				);
		},

		map: { arg self ...arglist;
			var domap;
			self.target_wrappers = List.new;
			arglist.debug("map: arglist");
			domap = { arg args;
				var wrapper;
				args.debug("map: domap: args");
				wrapper = self.new_wrapper(args, self.kind);
				if(wrapper.notNil) {
					self.target_wrappers.add(wrapper);
					self.changed(\map);
				};
			};
			"wh6at".debug;
			if(arglist[0].isSequenceableCollection) {
				arglist[0].do { arg args;
					"wh7at".debug;
					domap.(args)
				}
			} {
				"wh8at".debug;
				domap.(arglist)
			};
		},

		unmap: { arg self;
			self.target_wrappers = List.new;
			self.changed(\map);
		
		}
	);

	~class_midi_responder_wrapper = (
		new: { arg self, parent;
			self = self.deepCopy;
		
			self.my_parent = {parent};
			self.init;
			
		
			self;
		},

		init: { arg self;
			self.get_val = self[\target_get];
			self.get_norm_val = self[\target_norm_get];
			self.set_val = self[\target_set];
			self.set_norm_val = self[\target_norm_set];
		},

		spec: { arg self; self.target_spec },
		label: { arg self; self.target_string },

		target_spec_type: { arg self;
			\scalar
		},

		midi_val_: { arg self, val;
			self.my_parent.midi_val = val;
		},

		midi_val: { arg self;
			self.my_parent.midi_val;
		},

		tolerance: { arg self;
			self.my_parent.tolerance;
		},

		get_spec: { arg self ... args;
			lib[\get_spec].(*args);
		},

		target_string: {
			"_"
		},

		target_norm_get: {
			0
		},

		target_get: {
			0
		},

		handle_midi_cc_message: { arg self, val, b,c,d;
			var curval;
			if(self.target.notNil) {
				var midi_val = self.target_spec.map(self.midi_val);
				var normval;
				val = val/127;
				normval = val;
				[self.name, val].debug("1");
				val = self.target_spec.map(val);
				[self.name, val].debug("2");
				curval = self.target_get;
				[self.target_string, midi_val, val, curval].debug("target, midiOLD, midiCUR, PRESET");
				[midi_val, curval, midi_val == curval, (midi_val - curval), (midi_val - curval).abs < 0.001  ].debug("COPLEMETNET A LA MASSE");
				if((curval - midi_val).abs < 0.001) {
					debug("not blocked");
					self.target_set(val);
					self.changed(\val);
					self.my_parent.changed(\val);
				} {
					if( (val - curval).abs < (self.target_spec.range * self.tolerance )) {
						debug("unblock!");
						self.target_set(val);
						self.changed(\val);
						self.my_parent.changed(\val);
					} {
						debug("blocked!");
					}
				};
				self.midi_val = self.target_spec.unmap(val);
			} 
		},

		handle_midi_cc_message_no_unmap: { arg self, val, b,c,d;
			var curval;
			if(self.target.notNil) {
				var midi_val = self.target_spec.map(self.midi_val);
				var normval;
				val = val/127;
				normval = val;
				[self.name, val].debug("1");
				val = self.target_spec.map(val);
				[self.name, val].debug("2");
				curval = self.target_get;
				[self.target_string, midi_val, val, curval].debug("target, midiOLD, midiCUR, PRESET");
				[midi_val, curval, midi_val == curval, (midi_val - curval), (midi_val - curval).abs < 0.001  ].debug("COPLEMETNET A LA MASSE");
				if((curval - midi_val).abs < 0.001) {
					debug("not blocked");
					self.target_set(val);
					self.changed(\val);
					self.my_parent.changed(\val);
				} {
					if( (val - curval).abs < (self.target_spec.range * self.tolerance )) {
						debug("unblock!");
						self.target_set(val);
						self.changed(\val);
						self.my_parent.changed(\val);
					} {
						debug("blocked!");
					}
				};
				self.midi_val = normval;
			} 
		},

		handle_midi_cc_messageOLD: { arg self, val, b,c,d;
			var curval;
			if(self.target.notNil) {
				val = val/127;
				//[self.name, val].debug("1");
				if(self.target_spec.class == Event and: { self.target_spec.no_inverse.notNil }) {
					// NOOP
				} {
					// why do that ?
					val = self.target_spec.unmap(self.target_spec.map(val));
				};
				//[self.name, val].debug("2");
				curval = self.target_norm_get;
				//[self.target_string, self.midi_val, val, curval].debug("target, midiOLD, midiCUR, PRESET");
				//[self.midi_val, curval, self.midi_val == curval, (self.midi_val - curval), (self.midi_val - curval).abs < 0.001  ].debug("COPLEMETNET A LA MASSE");
				if((curval-self.midi_val).abs < 0.001) {
					//debug("not blocked");
					self.target_norm_set(val);
					self.changed(\val);
					self.my_parent.changed(\val);
				} {
					if( (val-curval).abs < self.tolerance ) {
						//debug("unblock!");
						self.target_norm_set(val);
						self.changed(\val);
						self.my_parent.changed(\val);
					} {
						debug("blocked!");
					}
				};
			} 
		},

		get_bus_from_map: { arg self, map;
			var rate;
			var index;
			map = map.asString;
			switch(map[0],
				$c, {
					rate = \control;
				},
				$a, {
					rate = \audio;
				}, {
					map.debug("get_bus_from_map: error, not a bus");
				}
			);
			index = map[1..].asInteger;
			Bus.new(rate, index, 1, s);
		},

	);

	~class_midi_cc_controller_for_button = (
		parent: lib[\class_midi_responder_wrapper],

		new: { arg self, parent, obj, key, spec;
			self = self.deepCopy;

			self.my_parent = {parent};
			self.target = {obj};
			self.target_spec = spec ?? \unipolar.asSpec;
			self.target_key = {key};
			self.init;
		
			

			self;
		},

		handle_midi_message: { arg self, val, b,c,d;
			if(self.kind == \note) {
				self.handle_midi_note_button_message(val,b,c,d);
			} {
				self.handle_midi_cc_button_message(val, b,c,d);
			};
		},

		target_string: { arg self;
			"Button, %, %".format(self.target.asCompileString, self.target_key.asCompileString)
		},

		handle_midi_cc_button_message: { arg self, val, b,c,d;
			//var defname = "cc_%_%".format(self.channel, self.ccnum).asSymbol;
			var curval;
			["cc_but", val,b,c,d].postln;
			val.debug("bordelval-1");
			if(self.target.notNil) {
				val = val/127;
				val.debug("bordelval0");
				if(val == 1) {
					val.debug("bordelval1");
					self.target.value; // target is a user function because of button mode
				} {
					val.debug("bordelval");
					if(val == 0) {
						val.debug("bordelval2");
						self.target_key.value; // target_key is a user function because of button mode
					}
				};
			}; 
			val.debug("bordelval3");
		},

		handle_midi_note_button_message: { arg self, val, b,c,d;
			var curval;
			[val,b,c,d].postln;
			if(self.target.notNil) {
				[val,b,c,d].postln;
				self.target.value;
			} 
		},
	);

	~class_midi_cc_controller_for_patpreset = (
		parent: lib[\class_midi_responder_wrapper],
		new: { arg self, parent, obj, key, spec;
			self = self.deepCopy;

			self.my_parent = {parent};
			self.target_key = key;
			self.target = {obj};
		
			if(spec.notNil) {
				self.target_spec = spec;
			} {
				if(self.target.get_active_score.specs_dict[key].notNil) {
					self.target_spec = self.target.get_active_score.specs_dict[key];
				} {
					self.target_spec = self.get_spec(self.target_key);
				}
			};
			
			self.handle_midi_message = lib[\class_midi_responder_wrapper][\handle_midi_cc_message];
			self.init;
		
			self;
		},

		target_set: { arg self, val;
			self.target.set_val(self.target_key, val)
		},

		target_string: { arg self;
			if(self.target.isNil) {
				"PatPreset nil";
			} {
				"PatPreset (%%), %".format(self.target.node_uname, self.target.name, self.target_key)
			}
		},

		target_norm_set: { arg self, val;
			//"1".debug;
			self.target.set_val(self.target_key, self.target_spec.map(val));
			//"11".debug;
		},

		target_get: { arg self;
			self.target.get_val(self.target_key)
		},

		target_norm_get: { arg self;
			var val;
			val = self.target.get_val(self.target_key);
			val.debug("target_norm_get: val");
			self.target_spec.unmap(val);
		},
	);

	~class_midi_cc_controller_for_ndef = (
		parent: lib[\class_midi_responder_wrapper],

		new: { arg self, parent, obj, key, spec;
			self = self.deepCopy;

			self.my_parent = {parent};
			self.target_key = key;
			self.target = {obj};
		
			self.target_spec = spec ?? self.get_spec(self.target_key);
			
			self.handle_midi_message = lib[\class_midi_responder_wrapper][\handle_midi_cc_message];
			self.init;
		
			self;
		},

		get_spec: { arg self, key;
			var spec;
			spec = self.target.getSpec(key);
			spec = spec ?? {
				lib[\get_spec].(key);
			};
			spec;
		},

		install_midi_responder: { arg self; self.install_midi_cc_responder },

		target_set: { arg self, val;
			self.target.set(self.target_key, val)
		},

		target_string: { arg self;
			"Ndef (%), %".format(self.target !? (_.key), self.target_key)
		},

		target_symbol: { arg self;
			\ndef -> self.target.key
		},

		target_macrokey: { arg self;
			var sym;
			sym = self.target_symbol;
			[sym.key, sym.value, self.target_key]; 
		},

		target_norm_set: { arg self, val;
			//"1".debug;
			self.target.set(self.target_key, self.target_spec.map(val));
			//"11".debug;
		},

		target_get: { arg self;
			var res;
			if(self.target_key.notNil) {
				res = self.target.get(self.target_key);
				if(res.isNil) {
					debug("target_get: % is nil, returning 0".format(self.target_string));
					0;
				} {
					res;
				};
			} {
				debug("target_get: %: key is nil, returning 0".format(self.target_string));
				0;

			}
		},

		target_norm_get: { arg self;
			var val;
			if(self.target_key.notNil) {
				val = self.target.get(self.target_key);
				//val.debug("target_norm_get: val");
				if(val.isNil) {
					debug("target_get: % is nil, returning 0".format(self.target_string));
					0;
				} {
					self.target_spec.unmap(val);
				};
			} {
				debug("target_get: %: key is nil, returning 0".format(self.target_string));
				0;

			}
		},
	);

	~class_midi_cc_controller_for_ndef_volume = (
		parent: lib[\class_midi_responder_wrapper],
		new: { arg self, parent, obj, key, spec;
			self = self.deepCopy;

			self.my_parent = {parent};
			self.target_key = key;
			self.target = obj;
		
			self.target_spec = spec ?? ControlSpec(0,4,'lin',0,1);
			
			self.handle_midi_message = lib[\class_midi_responder_wrapper][\handle_midi_cc_message];
			self.init;
		
			self;
		},

		install_midi_responder: { arg self; self.install_midi_cc_responder },

		target_set: { arg self, val;
			self.target.vol = val
		},

		target_string: { arg self;
			"Ndef (%), %".format(self.target !? (_.key), self.target_key)
		},

		target_norm_set: { arg self, val;
			//"1".debug;
			self.target_set(self.target_spec.map(val));
			//"11".debug;
		},

		target_get: { arg self;
			self.target.vol
		},

		target_norm_get: { arg self;
			var val;
			val = self.target_get;
			val.debug("target_norm_get: val");
			self.target_spec.unmap(val);
		},
	);

	~class_midi_cc_controller_for_morpher = (
		parent: lib[\class_midi_responder_wrapper],
		new: { arg self, parent, obj, key, spec;
			self = self.deepCopy;
			spec = spec ?? \unipolar.asSpec;

			self.my_parent = {parent};
			self.target_key = key;
			self.target = {obj};
		
			self.target_spec = spec ?? self.get_spec(self.target_key);
			
			self.handle_midi_message = lib[\class_midi_responder_wrapper][\handle_midi_cc_message];
			self.init;
		
			self;
		},

		target_set: { arg self, val;
			self.target.set_val(self.target_key, val)
		},

		target_string: { arg self;
			if(self.target.isNil) {
				"Morpher nil";
			} {
				"Morpher (%%), %".format(self.target.node_uname, self.target.name, self.target_key)
			}
		},

		target_symbol: { arg self;
			\morph -> self.target.key
		},

		target_macrokey: { arg self;
			var sym;
			sym = self.target_symbol;
			[sym.key, sym.value, self.target_key]; 
		},

		target_norm_set: { arg self, val;
			//"1".debug;
			self.target.set_val(self.target_key, self.target_spec.map(val));
			//"11".debug;
		},

		target_get: { arg self;
			self.target.get_val(self.target_key)
		},

		target_norm_get: { arg self;
			var val;
			val = self.target.get_val(self.target_key);
			val.debug("target_norm_get: val");
			self.target_spec.unmap(val);
		},
	);

	~class_midi_cc_controller_for_volume = (
		parent: lib[\class_midi_responder_wrapper],
		new: { arg self, parent, obj, key, spec;
			self = self.deepCopy;

			self.my_parent = {parent};
			self.target_key = key;
			self.target = {obj};
		
			self.target_spec = ControlSpec(-inf, 6.0, 'db', 0.0, -inf, " dB");
			
			self.handle_midi_message = lib[\class_midi_responder_wrapper][\handle_midi_cc_message];
			self.init;
		
			self;
		},

		target_set: { arg self, val;
			self.target.volume = val
		},

		target_string: { arg self;
			if(self.target.isNil) {
				"Volume nil";
			} {
				"Volume";
			}
		},

		target_symbol: { arg self;
			\volume
		},

		target_macrokey: { arg self;
			var sym;
			sym = self.target_symbol;
			[sym.key, sym.value, self.target_key]; 
		},

		target_norm_set: { arg self, val;
			//"1".debug;
			self.target_set(self.target_spec.map(val));
			//"11".debug;
		},

		target_get: { arg self;
			self.target.volume
		},

		target_norm_get: { arg self;
			var val;
			val = self.target_get;
			val.debug("target_norm_get: val");
			self.target_spec.unmap(val);
		},
	);

	~class_midi_cc_controller_for_pdef = (
		parent: lib[\class_midi_responder_wrapper],

		new: { arg self, parent, obj, key, spec;
			self = self.deepCopy;

			self.my_parent = {parent};
			self.target_key = key;
			self.target = {obj};
		
			self.target_spec = spec ?? self.get_spec(self.target_key);

			self.update_cache;
			
			self.handle_midi_message = lib[\class_midi_responder_wrapper][\handle_midi_cc_message];
			self.init;
		
			self;
		},

		get_spec: { arg self, key;
			var spec;
			var specs;
			specs = self.target.getHalo(\spec);
			if(specs.isNil) {
				self.target.checkSpec;
				specs = self.target.getHalo(\spec);
				specs.parent = nil;
			};
			spec = self.target.getSpec(key);
			spec = spec ?? {
				lib[\get_spec].(key, self.target.getHalo(\instrument));
			};
			spec;
		},

		target_val_cache: { arg self;
			var res;
			res = self.target.getHalo(\cache_+++self.target_key);
			if(res.isNil) {
				self.update_cache;
				res = self.target.getHalo(\cache_+++self.target_key);
				if(res.isNil) {
					debug("MAIS PUTAIN DE BORDEL!!!");
				}
			};
			res;
		},

		target_val_cache_: { arg self, val;
			self.target.addHalo(\cache_+++self.target_key, val)
		},

		update_cache: { arg self;
			var res;
			self.target_val_cache = if(self.target_key.notNil) {
				res = self.target.get(self.target_key);
				if(res.isNil) {
					debug("target_get: % is nil, returning 0".format(self.target_string));
					0;
				} {
					if(res.class == Symbol) {
						var bus = self.get_bus_from_map(res);
						bus.getSynchronous;
					} {
						res;
					}
				};
			} {
				debug("target_get: %: key is nil, returning 0".format(self.target_string));
				0;

			};
			self.target_val_cache.debug("target_val_cache: update_cache");
		},

		install_midi_responder: { arg self; self.install_midi_cc_responder },

		target_set: { arg self, val;
			var curval = self.target.get(self.target_key);
			if(curval.class == Symbol) {
				var bus = self.get_bus_from_map(curval);
				bus.set(val);
			} {
				self.target.set(self.target_key, val)
			};
			self.target_val_cache = val;
			self.target_val_cache.debug("target_val_cache: set");
		},

		target_string: { arg self;
			"Pdef (%), %".format(self.target !? (_.key), self.target_key)
		},

		target_symbol: { arg self;
			\pdef -> self.target.key
		},

		target_macrokey: { arg self;
			var sym;
			sym = self.target_symbol;
			[sym.key, sym.value, self.target_key]; 
		},

		target_norm_set: { arg self, val;
			var mappedval;
			//"1".debug;
			mappedval = self.target_spec.map(val);
			self.target_set(mappedval);
			//"11".debug;
		},

		target_get: { arg self;
			var res;
			if(self.target_key.notNil) {
				res = self.target.get(self.target_key);
				if(res.isNil) {
					debug("target_get: % is nil, returning 0".format(self.target_string));
					0;
				} {
					if(res.class == Symbol) {
						//var bus = self.get_bus_from_map(res);
						self.target_val_cache.debug("target_val_cache: get");
						self.target_val_cache;
						//bus.getSynchronous;
					} {
						res;
					}
				};
			} {
				debug("target_get: %: key is nil, returning 0".format(self.target_string));
				0;

			}
		},

		target_norm_get: { arg self;
			var val;
			if(self.target_key.notNil) {
				val = self.target.get(self.target_key);
				//val.debug("target_norm_get: val");
				if(val.isNil) {
					debug("target_get: % is nil, returning 0".format(self.target_string));
					0;
				} {
					if(val.class == Symbol) {
						//var bus = self.get_bus_from_map(val);
						//val = bus.getSynchronous;
						self.target_val_cache.debug("target_val_cache: norm_get");
						val = self.target_val_cache;
					};
					self.target_spec.unmap(val);
				};
			} {
				debug("target_get: %: key is nil, returning 0".format(self.target_string));
				0;

			}
		},
	);

	~class_midi_array_controller_for_pdef = (
		parent: lib[\class_midi_responder_wrapper],

		new: { arg self, parent, obj, key, spec;
			self = self.deepCopy;

			self.my_parent = {parent};
			self.target_key = key;
			self.target = obj;

			self.pattern_mode = true;
		
			self.target_spec = spec ?? self.get_spec(self.target_key);

			self.update_cache;
			
			self.handle_midi_message = lib[\class_midi_responder_wrapper][\handle_midi_cc_message];
			self.init;
		
			self;
		},

		get_spec: { arg self, key;
			var spec;
			var specs;
			specs = self.target.getHalo(\spec);
			if(specs.isNil) {
				self.target.checkSpec;
				specs = self.target.getHalo(\spec);
				specs.parent = nil;
			};
			spec = self.target.getSpec(key);
			spec = spec ?? {
				lib[\get_spec].(key, self.target.getHalo(\instrument));
			};
			spec;
		},

		target_val_cache: { arg self;
			var res;
			res = self.target.getHalo(\cache_+++self.target_key);
			if(res.isNil) {
				self.update_cache;
				res = self.target.getHalo(\cache_+++self.target_key);
				if(res.isNil) {
					debug("MAIS PUTAIN DE BORDEL!!!");
				}
			};
			res;
		},

		target_val_cache_: { arg self, val;
			self.target.addHalo(\cache_+++self.target_key, val)
		},

		update_cache: { arg self;
			var res;
			self.target_val_cache = if(self.target_key.notNil) {
				res = self.target.get(self.target_key);
				if(res.isNil) {
					debug("update_cache: % is nil, returning 0".format(self.target_string));
					0;
				} {
					if(res.class == Symbol) {
						var bus = self.get_bus_from_map(res);
						bus.getSynchronous;
					} {
						self.nestoff(res);
					}
				};
			} {
				debug("update_cache: %: key is nil, returning 0".format(self.target_string));
				0;

			};
			self.target_val_cache.debug("target_val_cache: update_cache");
		},

		install_midi_responder: { arg self; self.install_midi_cc_responder },

		target_string: { arg self;
			"Pdef (%), %".format(self.target !? (_.key), self.target_key)
		},

		target_symbol: { arg self;
			\pdef -> self.target.key
		},

		target_macrokey: { arg self;
			var sym;
			sym = self.target_symbol;
			[sym.key, sym.value, self.target_key]; 
		},

		target_norm_set: { arg self, val;
			var mappedval;
			//"1".debug;
			mappedval = self.target_spec.map(val);
			self.target_set(mappedval);
			//"11".debug;
		},

		target_set: { arg self, val;
			var curval = self.target.get(self.target_key);
			if(curval.class == Symbol) {
				var bus = self.get_bus_from_map(curval);
				bus.set(val);
			} {
				self.target.set(self.target_key, self.neston(val))
			};
			self.target_val_cache = val;
			self.target_val_cache.debug("target_val_cache: set");
		},

		nestoff: { arg self, val;
			if(val[0].isSequenceableCollection) {
				val[0];
			} {
				val;
			}
		},

		neston: { arg self, val;
			if(self.pattern_mode) {
				[ val ]
			} {
				val
			}
		},

		default_value: { arg self;
			[0]
		},

		target_get: { arg self;
			var res;
			var retval;
			retval = if(self.target_key.notNil) {
				res = self.target.get(self.target_key);
				if(res.isNil) {
					debug("target_get: % is nil, returning 0".format(self.target_string));
					self.default_value;
				} {
					if(res.class == Symbol) {
						//var bus = self.get_bus_from_map(res);
						self.target_val_cache.debug("target_val_cache: get");
						self.target_val_cache;
						//bus.getSynchronous;
					} {
						self.nestoff(res);
					}
				};
			} {
				debug("target_get: %: key is nil, returning 0".format(self.target_string));
				self.default_value;

			};
			retval.debug("target_get: retval");
			retval;
		},

		target_norm_get: { arg self;
			var val;
			self.target_spec.unmap(self.target_get)
		},
	);

	~class_midi_env_controller_for_pdef = (
		parent: lib[\class_midi_array_controller_for_pdef],

		get_xy: { arg self;
			var x = List.new;
			var y = List.new;
			var env = self.get_val;
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

		default_value: { arg self;
			Env([0,1],[1],[0])
		},

		nestoff: { arg self, val;
			if(val[0].class == Env) {
				val[0];
			} {
				val;
			}
		},


		get_norm_xy: { arg self, time_scale;
			var x = List.new;
			var y = List.new;
			var env = self.get_norm_val;
			var times, levels;
			var res;
			time_scale = time_scale ?? self.time_scale;
			times = env.times;
			levels = env.levels;
			x = times;
			y = levels;
			x = x.insert(0, 0);
			res = [x.integrate.asArray * time_scale,y.asArray];
			res
		},

		map_curves: { arg self, curves;
			
		},

		map_levels: { arg self, levels;
			levels.collect { arg level, x;
				self.target_spec.levels[x].map(level)
			};
		},

		map_times: { arg self, times;
			times.collect { arg time, x;
				self.target_spec.times[x].map(time)
			};
		},

		set_norm_level: { arg self, index, val;
			var spec = self.target_spec.levels[index];
			var env = self.get_val;
			if(env.levels[index].notNil) {
				env.levels[index] = spec.map(val);
				env.levels = env.levels;
				true;
			} {
				false;
			}
		},

		set_norm_time: { arg self, index, val;
			var spec = self.target_spec.times[index];
			var env = self.get_val;
			if(env.times[index].notNil) {
				env.times[index] = spec.map(val);
				env.times = env.times;
				true;
			} {
				false;
			}
		},

		set_norm_curve: { arg self, index, val;
			var spec = self.target_spec.curves[index];
			var env = self.get_val;
			if(env.curves.class == Float) {
				env.curves = spec.map(val);
				true;
			} {
				if(env.curves[index].notNil) {
					env.curves[index] = spec.map(val);
					env.curves = env.curves;
					true;
				} {
					false;
				}
			};
		},

		set_norm_xy: { arg self, val, time_scale;
			var times, levels;
			var env;
			var oldenv;
			//self.parent_controller.is_present = true;
			time_scale = time_scale ?? self.time_scale;
			#times, levels = val;
			debug("111");
			times = times.copy;
			times.removeAt(0);
			debug("112");
			times = times / time_scale;
			times = times.differentiate;
			debug("113");
			times = self.map_times(times);
			levels = self.map_levels(levels);
			debug("114");
			oldenv = self.get_val;
			debug("115");
			env = Env(levels, times, oldenv.curves, oldenv.releaseNode, oldenv.loopNode);
			debug("116");
			self.set_val(env);
			debug("117");
		},
	);

}.value;
