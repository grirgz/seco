
	~get_next_record_time = { arg clock, quant, prerecdur;
		var rectime, time_til_rectime;
		var prerecord_time;
		rectime = clock.nextTimeOnGrid(quant);
		time_til_rectime = rectime - clock.beats;
		//[~c.beats, ~rectime, ~t_til_rectime].debug("beats, next, t_til_rectime");
		if(time_til_rectime < prerecdur) {
			//"recalc!".debug;
			rectime = rectime + ((prerecdur/quant).asInteger+1 * quant);
			time_til_rectime = rectime - clock.beats;
		};
		prerecord_time = rectime - prerecdur;
		//[clock.beats, clock.nextTimeOnGrid(quant), rectime, time_til_rectime].debug("2: beats, next, rectime, t_til_rectime");
		[prerecord_time, rectime]
	};

	~class_event_recorder = (
		new: { arg self;
			self = self.deepCopy;
		
			
			self.recclock = TempoClock.default;
			self.recclock.debug("gnill");
			self.is_recording = false;
			self.is_prerecording = false;
		
			self;
		},

		get_quant: { arg self;
			Pdef.defaultQuant;
		},

		record_dur: 8,
		slice_dur: 8,

		start_recording: { arg self, record_dur, slice_dur, stop_func;
			var quant;
			var visual_timer;
			var visual_timer_player;
			var timer_start_beat;
			var next_time_on_grid;
			var overlap_time = ~veco_record_overlap_time;
			var dur;
			var time_til_recording_start;
			var prerecord_dur;
			var record_time;
			var prerecord_time;

			if((self.is_recording or: { self.is_prerecording }).not) {

				self.is_prerecording = true;

				self.stop_func = stop_func;

				prerecord_dur = 4;
				if(prerecord_dur / self.recclock.tempo <= 2) {
					prerecord_dur = prerecord_dur * 2;
				};
				prerecord_dur.debug("prerecord_dur");


				quant = self.get_quant;
				dur = record_dur ?? self.record_dur;
				slice_dur = slice_dur ?? self.slice_dur;

				debug("start_recording");
				self.event_score = ~make_notescore.();


				#prerecord_time, record_time = ~get_next_record_time.(self.recclock, quant, prerecord_dur);
				self.start_time = record_time;

				// prerecording
				self.recclock.schedAbs( prerecord_time, {
						self.changed(\prerecording_started);
						nil;
					}
				);

				// recording
				self.recclock.schedAbs( record_time, {
					self.changed(\recording_started);
					self.is_recording = true;
					self.is_prerecording = false;
					self.start_immediate_recording;
					nil;
				});

				// stop recording
				if(dur != inf) {
					self.recclock.schedAbs(record_time + dur, { 
						self.stop_recording;
						nil;
					});
				};
			}
		},

		get_slices: { arg self;
			self.slices = self.event_score.split_in_slices(self.slice_dur);
			self.slices;
		},

		get_current_recording_time: { arg self;
			// FIXME: is_recording is true but actual recording not started yet could cause bugs
			[self.recclock.beats, self.stop_time, self.start_time].debug("get_current_recording_time: beats, stop, start");
			if(self.is_recording) {
				self.recclock.beats - self.start_time;
			} {
				self.stop_time - self.start_time;
			}
		},

		stop_recording: { arg self;
			self.stop_time = self.recclock.beats;
			self.is_recording = false;
			debug("stop_recording");
			self.changed(\recording_stopped);
			"kjk".debug;
			"1kjk".debug;
			[self.stop_time, self.start_time].debug("kjxxx");
			self.event_score.set_end(self.stop_time - self.start_time);
			"2kjk".debug;
			self.stop_immediate_recording;
			self[\stop_func].(self);
		},

		add_event_on: { arg self, event;
			var time;
			if(self.is_recording) {
				time = self.recclock.beats - self.start_time;
				self.event_score.add_note(event, time, event.event_key)
			}
		},

		add_event_off: { arg self, key;
			var time;
			if(self.is_recording) {
				time = self.recclock.beats - self.start_time;
				self.event_score.book.debug("BOOK");
				self.event_score.add_noteoff(key, time)
			}
		},

	);

	~class_recorder_view = (
		new: { arg self;
			var parent;
			self = self.deepCopy;

		
			self;
		},

		set_recorder: { arg self, recorder;
		
			self.destructor;

			self.recorder = recorder;
			~make_class_responder.(self, self, recorder, [\recording_started, \recording_stopped, \prerecording_started]);
			CmdPeriod.doOnce({ self.destructor; });
			
		},

		recclock: { arg self; self.recorder.recclock },

		destructor: { arg self;
			debug("class_recorder_view: destructor");
			self[\onClone].value;
		},

		prerecording_started: { arg self;
			debug("recview: prerecording_started");
			4.do { arg x;
				Veco.main.launchpad.set_mode_led(x, \off);
			};
			self.prerecording = true;
			self.visual_timer.play;
		},

		recording_started: { arg self;
			debug("recview: recording_started");
			self.prerecording = false;
		},

		recording_stopped: { arg self;
			debug("recview: recording_stopped");
			self.visual_timer_task.stop;
			4.do { arg x;
				Veco.main.launchpad.set_mode_led(x, \orange);
			};
			self.destructor;
		},

		reset_slice_row: { arg self;
			8.do { arg x;
				x.debug("hih");
				Veco.main.launchpad.set_led(Point(x,0), \off);
			};
		},

		visual_timer: { arg self;
			var quant = self.recorder.get_quant;
			self.visual_timer_task = Task({ 
				var x=self.recclock.beatInBar.asInteger;
				var slicenum;
				debug("RAHHHHHHHH");
				//self.reset_slice_row;
				inf.do {
					//[self.recclock.beats, self.recclock.beatInBar, self.recclock.nextTimeOnGrid(quant)].debug("RAHHHHHHHH2");
					Veco.main.launchpad.set_mode_led((x-1).wrap(0,3), \off);
					if(self.prerecording) {
						Veco.main.launchpad.set_mode_led(x, \green);
					} {
						Veco.main.launchpad.set_mode_led(x, \red);
					};
					//x.debug("onlight=x");
					if(self.recorder.is_recording) {
						slicenum = (self.recorder.get_current_recording_time / self.recorder.slice_dur);
						//[slicenum, slicenum.asInteger, slicenum % 8, x].debug("should i reset ? : slicenum, slice%8, x");
						//if(slicenum % 8 == 0 and: { x == 0 }) {
						if(slicenum % 8 == 0) {
							self.reset_slice_row;
						};
						Veco.main.launchpad.set_led(Point(slicenum%8,0), \red);
					};
					1.wait;
					x = x+1 % 4;
				}
			});
			self.visual_timer_task;
		},
	);


	~class_piano_recorder = (
		parent: ~class_event_recorder,
		new: { arg self;
			self = ~class_event_recorder[\new].(self);
		
			
		
			self;
		},

		set_note_pattern: { arg self, pat;
			self.note_pattern = pat;
		},

		start_immediate_recording: { arg self;
			var player;
			var notes = Array.newClear(128);
			self.notes_book = notes;
			self.note_stream = self.note_pattern.asStream;
			MIDIdef.noteOn(\pianoOn, {arg ...args; 
				var veloc, num, chan;
				var ev;
				#veloc, num = args;
				[veloc, num].debug("lkj");
				args.debug("mynoteon");
				ev = self.note_stream.next(Event.default);
				//ev = Pbind(\freq, num.midicps) <> ev;
				ev[\freq] = num.midicps;
				ev[\type] = \on;
				ev[\isRest] = false;
				ev[\latency] = \a;
				ev.debug("ev1");
				notes[num] = ev;
				self.add_event_on((
					event_key: num,
				));
				player = ev.play;
				player.debug("player");
			},nil,1); 
			MIDIdef.noteOff(\pianoOff, {arg ...args; 
				var veloc, num, chan;
				var ev;
				#veloc, num = args;
				if(notes[num].notNil) {
					notes[num][\type] = \off;
					self.add_event_off(num);
					args.debug("mynoteoff");
					notes[num].play;
					notes[num] = nil;
				} {
					num.debug("skiped false note off");
				};
			},nil,1); 

			
		},

		stop_immediate_recording: { arg self;
			MIDIdef.noteOn(\pianoOn).free;
			MIDIdef.noteOff(\pianoOff).free;
			self.notes_book.do { arg note;
				if(note.notNil) {
					note[\type] = \off;
					note.debug("kill last note");
					note.play;
				}
			};
			self.get_slices;
			
		},
	);

