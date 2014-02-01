
{
	var lib = currentEnvironment;

	~class_midi_cc_controller = (
		source_uid: nil,
		// TODO: refactor to include source_uid in constructor
		new: { arg self, ccnum, name=\midicc, channel=1, kind=\cc;
			self = self.deepCopy;
		
			self.tolerance = 0.03;
			self.name = name;
			self.kind = kind;
			self.channel = channel;
			self.ccnum = ccnum;
			self.midi_val = -10;
			self.changed(\new_map);
			
			self;
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


		install_midi_cc_responder: { arg self;
			//var defname = "cc_%_%".format(self.channel, self.ccnum).asSymbol;
			var defname = "midiboard_%".format(self.name).asSymbol;
			self.responder = MIDIdef.cc(defname, { arg val,b,c,d;
				var curval;
				if(self.target.notNil) {
					val = val/127;
					[self.name, val,b,c,d].debug("1");
					val = self.target_spec.unmap(self.target_spec.map(val));
					[self.name, val,b,c,d].debug("2");
					curval = self.target_norm_get;
					[self.target_string, self.midi_val, val, curval].debug("target, midiOLD, midiCUR, PRESET");
					[self.midi_val, curval, self.midi_val == curval, (self.midi_val - curval), (self.midi_val - curval).abs < 0.001  ].debug("COPLEMETNET A LA MASSE");
					if((curval-self.midi_val).abs < 0.001) {
						debug("not blocked");
						self.target_norm_set(val);
						self.changed(\val);
					} {
						if( (val-curval).abs < self.tolerance ) {
							debug("unblock!");
							self.target_norm_set(val);
							self.changed(\val);
						} {
							debug("blocked!");
						}
					};
					self.midi_val = val;
				} 
			}, self.ccnum, self.channel, self.source_uid);
			self.responder.permanent = MIDIBoard.permanent;

		},

		map: { arg self ...args;
			var obj = args[0];
			var key = args[1];
			var spec = args[2];
			self.target = {obj};
			self.target_key = key;
			switch(self.target.class,
				Event, {
					"444".debug;
					//if(self.target.get_active_score.vals[self.target_key])
					if(self.kind == \cc) {
						self.putAll(lib[\class_midi_cc_controller_for_patpreset]);
						if(spec.notNil) {
							self.target_spec = spec;
						} {
							if(self.target.get_active_score.specs_dict[key].notNil) {
								self.target_spec = self.target.get_active_score.specs_dict[key];
							} {
								self.target_spec = \widefreq.asSpec;
							}
						};
						self.install_midi_responder;
						self.changed(\map);
					} {
						"Error: note is not a cc control"
					}
				},
				Ndef, {
					"445".debug;
					if(self.kind == \cc) {
						if(self.target_key == "vol") {
							self.putAll(lib[\class_midi_cc_controller_for_ndef_volume]);
							self.target_spec = spec ?? ControlSpec(0,4,'lin',0,1);
							self.install_midi_responder;
						} {
							self.putAll(lib[\class_midi_cc_controller_for_ndef]);
							self.target_spec = spec ?? \widefreq.asSpec;
							self.install_midi_responder;
						};
						self.changed(\map);
					} {
						"Error: note is not a cc control"
					}
				},
				Function, {
					"446".debug;
					self.putAll(lib[\class_midi_cc_controller_for_button]);
					self.target_spec = spec ?? \unipolar.asSpec;
					self.target_key = {key};
					self.install_midi_responder;
					self.changed(\map);
				}
			);
		},

		unmap: { arg self;
			self.target = nil;
			self.target_key = nil;
			self.target_spec = nil;
			self.changed(\map);
		
		}
	);

	~class_midi_cc_controller_for_button = (
		install_midi_responder: { arg self;
			if(self.kind == \note) {
				self.install_midi_note_button_responder;
			} {
				self.install_midi_cc_button_responder;
			};
			self.responder.permanent = MIDIBoard.permanent;
		},

		target_string: { arg self;
			"Button, %, %".format(self.target.asCompileString, self.target_key.asCompileString)
		},

		install_midi_cc_button_responder: { arg self;
			//var defname = "cc_%_%".format(self.channel, self.ccnum).asSymbol;
			var defname = "midiboard_%".format(self.name).asSymbol;
			self.responder = MIDIdef.cc(defname, { arg val,b,c,d;
				var curval;
				["cc_but", val,b,c,d].postln;
				val.debug("bordelval-1");
				if(self.target.notNil) {
					val = val/127;
					val.debug("bordelval0");
					if(val == 1) {
						val.debug("bordelval1");
						self.target.value;
					} {
						val.debug("bordelval");
						if(val == 0) {
							val.debug("bordelval2");
							self.target_key.value;
						}
					};
				}; 
				val.debug("bordelval3");
			}, self.ccnum, self.channel, self.source_uid)
		},

		install_midi_note_button_responder: { arg self;
			//var defname = "note_%_%".format(self.channel, self.ccnum).asSymbol;
			var defname = "midiboard_%".format(self.name).asSymbol;
			self.responder = MIDIdef.noteOn(defname, { arg val,b,c,d;
				var curval;
				defname.debug("responder");
				[val,b,c,d].postln;
				if(self.target.notNil) {
					//val = val/127;
					[val,b,c,d].postln;
					self.target.value;
				} 
			}, self.ccnum, self.channel, self.source_uid)
		},
	);

	~class_midi_cc_controller_for_patpreset = (

		install_midi_responder: { arg self; self.install_midi_cc_responder },

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
			"1".debug;
			self.target.set_val(self.target_key, self.target_spec.map(val));
			"11".debug;
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

		install_midi_responder: { arg self; self.install_midi_cc_responder },

		target_set: { arg self, val;
			self.target.set(self.target_key, val)
		},

		target_string: { arg self;
			"Ndef (%), %".format(self.target !? (_.key), self.target_key)
		},

		target_norm_set: { arg self, val;
			"1".debug;
			self.target.set(self.target_key, self.target_spec.map(val));
			"11".debug;
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
				val.debug("target_norm_get: val");
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

		install_midi_responder: { arg self; self.install_midi_cc_responder },

		target_set: { arg self, val;
			self.target.vol = val
		},

		target_string: { arg self;
			"Ndef (%), %".format(self.target !? (_.key), self.target_key)
		},

		target_norm_set: { arg self, val;
			"1".debug;
			self.target_set(self.target_spec.map(val));
			"11".debug;
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

}.value;
