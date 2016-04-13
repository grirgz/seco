
(
~launchpad_scene_sendpad = (
	parent: ~launchpad_scene,

	mode_name: \mode_sendpad,
	display_mode: \send,

	set_model: { arg self, model, mode=\send;
		self.display_mode = mode;
		self.mymodel = model;
	},

	render_state: { arg self;
		8.do { arg x;
			self.render_column(x)
		}
	},

	render_column: { arg self, x;
		8.do { arg y;
			self.render_pos(Point(x,y));
		}
	},

	render_pos: { arg self, pos;
		var name;
		var send;
		var node;
		var gridpos = pos;
		gridpos.debug("launchpad_scene_sendpad.render_pos: gridpos");
		self.gridpos_to_abspos(gridpos).debug("launchpad_scene_sendpad.render_pos: abspos");
		node = self.controller.get_node_by_index( self.gridpos_to_abspos(gridpos).x );
		if(self.mymodel.is_present(node)) {
			name = node.uname;
			if(self.display_mode == \send) {
				send = self.mymodel.get_send(name, gridpos.y);
				[gridpos, name, send].debug("launchpad_scene_sendpad: render_pos");
				if(send > 0) {
					self.set_led(gridpos, \green);
				} {
					self.set_led(gridpos, \dimgreen);
				}
			} {
				var in = self.mymodel.get_in(name);
				[gridpos, name, in].debug("launchpad_scene_sendpad: render_pos: in");
				if(in == gridpos.y) {
					self.set_led(gridpos, \red);
				} {
					self.set_led(gridpos, \dimred);
				}
			};
		} {
			self.set_led(gridpos, \off);
		}
		
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		//self.unblink_led;
		self.handle_grid_move(midi);
		//self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;

		//self.sceneset.clip.response_cc_off(midi);

		if(self.skip_first_time != false) {
			self.skip_first_time = false;
		} {
			self.sceneset.clip.response_cc_off(midi);
		}
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_sendpad.response_on");

		if(self.is_launcher_button(midinote)) {
			// NOOP
		} {
			if(self.modifiers[\session] == true) {
				self.controller.clip_offset = pos * 8;
				self.render_state;
			} {
				var name = self.controller.get_node_by_index( self.gridpos_to_abspos(pos).x ).uname;
				[name, pos].debug("launchpad_scene_sendpad.response_on: go!");
				if(self.display_mode == \send) {
					self.mymodel.toggle_send(name, pos.y);
					self.render_pos(pos);
				} {
					self.mymodel.set_in(name, pos.y);
					self.render_column(pos.x);
				}
			}
		}
	},

);

~class_sendpad = (
	new: { arg self, offset=150;
		self = self.deepCopy;
	
		
		self.offset = offset;
		self.busprefix = \in;
		self.ndefsuffix = \mixer;
		self.fxsuffix = \fx;

		self.special_bus = 7;

		self.busdict = IdentityDictionary.new;
		8.collect({ arg x;
			self.busdict[BusDef(self.busprefix+++x, \audio).index] = x;
			Spec.add(\wet+++(self.offset+x), ControlSpec(0,1,\lin));
		});
	
		self;
	},

	is_present: { arg self, node;
		node.is_present and: {
			Ndef(node.uname+++self.ndefsuffix).source.notNil
		}
	},

	install_sender: { arg self, name, x, force=false;
		var wasnil = false;
		if( Ndef(name+++self.ndefsuffix).at(self.offset+x).isNil ) {
			wasnil = true;
		};
		if(force or: wasnil) {
			Ndef(name+++self.ndefsuffix).put(self.offset+x, \filterIn -> { arg in;
				Out.ar(BusDef(self.busprefix+++x, \audio), in);
				in;
			});
			if( wasnil ) {
				Ndef(name+++self.ndefsuffix).set(\wet+++(self.offset+x), 0);
			};
		}
	},

	set_send: { arg self, name, x, mix;
		var pos = self.offset;
		self.install_sender(name, x);
		if(x == self.special_bus) {
			Ndef(name+++self.ndefsuffix).set(\amp, mix);
		} {
			Ndef(name+++self.ndefsuffix).set(\wet+++(pos+x), mix);
		}
	},

	get_send: { arg self, name, x;
		var pos = self.offset;
		if(x == self.special_bus) {
			var res = Ndef(name+++self.ndefsuffix).get(\amp);
			if(res.isNil) {
				0;
			} {
				res;
			}
		} {
			if( Ndef(name+++self.ndefsuffix).at(pos+x).isNil ) {
				0
			} {
				Ndef(name+++self.ndefsuffix).get(\wet+++(pos+x));
			}
		}
	},

	toggle_send: { arg self, name, x;
		if(self.get_send(name, x) > 0) {
			self.set_send(name, x, 0)
		} {
			self.set_send(name, x, 1)
		}
	},

	set_in: { arg self, name, x;
		Ndef(name+++self.fxsuffix).set(\inbus, BusDef(self.busprefix+++x, \audio));
	},

	get_in: { arg self, name;
		var res;
		res = Ndef(name+++self.fxsuffix).get(\inbus);
		if(res.class == Bus) {
			res = res.index;
		};
		res.debug("get_in");
		res = self.bus_to_index(res);
		res;
	},

	bus_to_index: { arg self, bus;
		self.busdict[bus]
	},

	get_send_param: { arg self, name, x;
		var pos = self.offset;
		if(x == self.special_bus) {
			var res = Param(Ndef(name+++self.ndefsuffix), \amp);
			res;
		} {
			Param(Ndef(name+++self.ndefsuffix), \wet+++(pos+x));
		}
		
	},

	get_send_column_param: { arg self, name;
		8.collect{ arg x;
			self.get_send_param(name, x)
		}
	},

	get_send_screen_param: { arg self;
		var params = List.new;
		8.do { arg nidx;
			var node =  Veco.main.get_node_by_index(Veco.main.clip_offset.x+nidx);
			var uname = node.uname;
			if(node.is_present) {
				params.addAll(self.get_send_column_param(uname));
			}
		};
		params;
	},

	edit: { arg self;
		~class_send_view.new;
	},
);

~class_send_view = (
	new: { arg self;
		var hlayout;
		self = self.deepCopy;
	

		hlayout = HLayout.new;
		self.labels = List.new;
		self.knobs = 8.collect { arg x;
			var vlayout = VLayout.new;
			var label = StaticText.new;
			label.font = Font.new.size_(10);
			self.labels.add(label);
			vlayout.add(label);
			hlayout.add(vlayout);
			8.collect { arg y;
				var knob = Knob.new;
				vlayout.add(knob);
				knob;
			} 
		};

		self.window = Window.new;

		self.window.layout = hlayout;
		self.window.view.keyDownAction = { arg ...args;
			//args.debug("args");
			if(args[3].dump == 27) {
				self.window.close;
			} {
				self.update_knobs;
			};
		};
		self.update_knobs;
		self.window.front;
		self.window.alwaysOnTop = true;
	
		self;
	},

	update_knobs: { arg self;
		"class_send_view:udpate_knobs XX".debug;
		8.do { arg x;
			var node = Veco.main.get_node_by_index(Veco.main.clip_offset.x + x);
			var params = ~veco_sendpad.get_send_column_param(node.uname);
			var in_idx = ~veco_sendpad.get_in(node.uname);
			self.labels[x].string = node.uname;
			if(node.is_present) {
				8.do { arg y;
					self.knobs[x][y].enabled = true;
					if(in_idx == y) {
						self.knobs[x][y].background = Color.new(0.11,0.1,0.4,0.4);
					} {
						self.knobs[x][y].background = Color.white;
					};
					self.knobs[x][y].mapParam(params[y])
				}
			} {
				8.do { arg y;
					self.knobs[x][y].unmapParam;
					self.knobs[x][y].background = Color.white;
					self.knobs[x][y].enabled = false;
				}
			};
		};
	},
);

~veco_sendpad = ~class_sendpad.new;
//~scene_sendpad = ~launchpad_scene_sendpad.new(Veco.main);
//~scene_sendpad.set_model(~sendpad, \send);
//~scene_sendpad.activate;
//~veco_send_view = ~class_send_view.new;
);

