

(
~launchpad_scene_pianopad = (
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
		var black = [0,1,1,0, 1,1,1,0];
		gridpos.debug("launchpad_scene_sendpad.render_pos: gridpos");
		if(pos.y.even) {
			if(black[pos.x] == 1) {
				self.set_led(gridpos, \yellow);
			} {
				self.set_led(gridpos, \off);
			}
		} {
			self.set_led(gridpos, \green);
		}
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		//self.unblink_led;
		//self.handle_grid_move(midi);
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

	midinote_to_note: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var black = [-1,1,3,0, 6,8,10,0];
		var white = [0,2,4,5, 7,9,11,12];
		midinote.debug("midinote_to_note: midinote");

		if(pos.y.even) {
			black[pos.x] + ( pos.y/2 * 12)
		} {
			white[pos.x] + ( pos.y-1/2 * 12 )
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
				//self.controller.clip_offset = pos * 8;
				//self.render_state;
			} {
				self.mymodel.note_on(self.midinote_to_note(midinote), 1);
			}
		}
	},

	response_off: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_sendpad.response_on");

		if(self.is_launcher_button(midinote)) {
			// NOOP
		} {
			if(self.modifiers[\session] == true) {
				//self.controller.clip_offset = pos * 8;
				//self.render_state;
			} {
				self.mymodel.note_off(self.midinote_to_note(midinote), 1);
			}
		}
		
	}

);

~class_note_recorder = (
	new: { arg self, inpat, outpat;
		self = self.deepCopy;
	
		self.inpat = inpat;
		self.outpat = outpat;
		
		self.note_array = List.newClear(128);
		self.start_responding;

		self.activate;
	
		self;
	},

	activate: { arg self;
		var pianopad;
		pianopad = ~launchpad_scene_pianopad.new(Veco.main);
		pianopad.set_model(self);
		pianopad.activate;
		self.pianopad = pianopad;
		
	},

	start_responding: { arg self;
		self.event_stream = self.inpat.asStream;
		self.event_stream.debug("bordel");
	},

	note_on: { arg self, note, velocity=1;
		var event;
		//[self.event_stream, self.event_stream.next(Event.default)].debug("et alors ?");
		event = self.event_stream.next(Event.default);
		event[\latency] = Ref(nil);
		event[\latency] = 0.00001;
		event[\type] = \note;
		event[\sustain] = 50;
		event[\isRest] = false;
		event[\freq] = nil;
		event[\note] = note;
		event.debug("cur event note on");
		self.note_array[note+10] = event.play;
		//self.note_array[note] = Synth(\default);
	},

	note_off: { arg self, note, velocity=1;
		"noteoff!!!".debug;
		if(self.note_array[note+10].notNil) {
			self.note_array[note+10].release;
			self.note_array[note+10] = nil;
		} {
			note.debug("what ?")
		}
	},


);


~rec = ~class_note_recorder.new(
	Pbind(
		\instrument, \default,
		\octave, 2,
		\latency, 0.7,
	)
	
);
//~rec.note_on(50);

)

s.latency = 0.20001

~rec.note_off(50);
x = (type: \on, latency:\no ).play;
x.release

