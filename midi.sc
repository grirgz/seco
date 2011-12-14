(

~midi_center = { arg main;
	var mc = (
		cc_states: Dictionary.new,

		next_free: (
			slider: 0,
			knob: 0
		),

		assign_first: { arg self, kind, param;
			var ccpath;	
			"HEINNNN".debug;
			if(self.next_free[kind] < 8) { //FIXME: hardcoded value
				ccpath = [kind, self.next_free[kind]];
				[param.name, ccpath, self.next_free].debug("assigning midi");
				main.commands.bind_param(ccpath, param);
				self.next_free[kind] = self.next_free[kind] + 1;
			} {
				("Too much assigned param: no midi controller left."+param.name).error;
			};
		},

		assign_adsr: { arg self, param;
			var ccpath;	
			param.name.debug("midi_center: assign_adsr");
			~adsr_point_order.do { arg key, i;
				ccpath = [\slider, i+4];
				main.commands.bind_param(ccpath, param.get_param(key));
			};
		},

		clear_assigned: { arg self, kind;
			self.next_free[kind] = 0;
		},

		get_control_val: { arg self, ccpath;
			if(self.cc_states[ccpath].isNil) {
				0
			} {
				self.cc_states[ccpath]
			}
		},

		set_control_val: { arg self, ccpath, val;
			[ccpath, val].debug("===================midi_center: set_control_val");
			self.cc_states[ccpath] = val;
		},

		get_midi_control_handler: { arg self, param; ~get_midi_control_handler.(self, param) },

		install_responders: { arg self;
			self.uninstall_responders;
			self.ccresp = List.new;
			~keycode.cakewalk.keysValuesDo { arg cctype, ccarray;
				ccarray.do { arg keycode, i;

					self.ccresp.add( CCResponder({ |src,chan,num,value|
							var ccpath = [cctype, i];
							var val = value/127;
							self.set_control_val(ccpath, val);
							main.commands.handle_cc(ccpath, val);
						},
						nil, // any source
						nil, // any channel
						keycode, // any CC number
						nil // any value
						)
					)
				}
			}

		},

		uninstall_responders: { arg self;
			self.ccresp.do { arg x; x.remove };
			self.ccresp = nil;
		}
	);
	CCResponder.removeAll;
	mc.install_responders;
	mc;
};

~get_midi_control_handler = { arg midi_center, param;
	(
		blocked: \sup,

		set_val: { arg self, val, cell=nil;
			[param.name, val].debug("get_midi_control_handler:set_val");
			self.unblock_do {
				debug("get_midi_control_handler:set_val: not blocked");
				//TODO: implement cell setting
				param.set_norm_val(val);
				1.debug("get_midi_control_handler:set_val");
				//TODO: recording of val ?
			};
			2.debug("get_midi_control_handler:set_val");
			param.changed(\midi_val, self.get_midi_val);
			3.debug("get_midi_control_handler:set_val");
		},

		get_midi_val: { arg self;
			[self.get_ccpath, midi_center.get_control_val(self.get_ccpath), param.spec.map(midi_center.get_control_val(self.get_ccpath))].debug("get_midi_val");
			param.spec.map(midi_center.get_control_val(self.get_ccpath));
		},

		get_midi_norm_val: { arg self;
			midi_center.get_control_val(self.get_ccpath);
		},

		get_ccpath: { arg self;
			self.ccpath;
		},

		label: { arg self;
			param.name.debug("get_midi_control_handler: label");
			if(self.ccpath.isNil) {
				"X"
			} {
				(self.get_ccpath[0].asString[0].asString ++ (self.get_ccpath[1]+1).asString).debug("get_midi_control_handler: label: label choosed");
				self.get_ccpath[0].asString[0].asString ++ (self.get_ccpath[1]+1).asString;
			}
		},

		set_ccpath: { arg self, val;
			param.name.debug("get_midi_control_handler: set_ccpath");
			self.ccpath = val;
			self.refresh;
		},

		unblock_do: { arg self, fun;
			var midi_val, param_val;

			1.debug("unblock_do: midi_val, param_val");
			midi_val = self.get_midi_norm_val;
			param_val = param.get_norm_val;

			[midi_val, param_val].debug("unblock_do: midi_val, param_val");
		
			switch(self.blocked,
				\not, fun,
				\sup, {
					if( midi_val <= param_val , {
						self.blocked = \not;
						param.changed(\blocked, false);
						fun.value;
					});
				},
				\inf, {
					if( midi_val >= param_val , {
						self.blocked = \not;
						param.changed(\blocked, false);
						fun.value;
					});
				}
			);

		},

		block: { arg self;
			var midi_val, param_val;

			midi_val = self.get_midi_norm_val;
			param_val = param.get_norm_val;

			case 
				{ midi_val > param_val } {
					self.blocked = \sup;
					param.changed(\blocked, true);
				}
				{ midi_val < param_val } {
					self.blocked = \inf;
					param.changed(\blocked, true);
				}
				{ true } {
					self.blocked = \not;
					param.changed(\blocked, false);
				};

		},

		refresh: { arg self;
			param.name.debug("get_midi_control_handler: refresh");
			self.block;
			param.changed(\label);
			param.changed(\midi_val, self.get_midi_val);
		}

	);
};

~rel_nextTimeOnGrid = { arg beats, quant = 1, phase = 0;
				var baseBarBeat = TempoClock.baseBarBeat;
                if (quant == 0) { beats + phase };
                if (phase < 0) { phase = phase % quant };
                roundUp(beats - baseBarBeat - (phase % quant), quant) + baseBarBeat + phase
};

~make_midi_recorder = { arg player;
	var prec, livesynth = player.get_piano;
	NoteOnResponder.removeAll;
	NoteOffResponder.removeAll;

	prec = (
		nonr: nil,
		noffr: nil,
		track: List.new,
		book: Dictionary.new,
		livebook: Dictionary.new,
		lastnote: nil,
		recording: false,
		metronome: { arg self; { arg tempo=1; Ringz.ar(Impulse.ar(tempo), [401, 400], 1/tempo) } },
		tclock: nil,

		start_metronome: { arg self, tempo;
			self.metro_player = self.metronome(tempo).play(args:[\tempo, tempo]);
			self.tbclock = TempoBusClock(self.metro_player, tempo);
		},

		stop_metronome: { arg self;
			self.metro_player.release;
		},

		start_overdubing: { arg self;

		},

		start_tempo_recording: { arg self, dur=2, tempo=1, metro=false, action;
			var tc = TempoClock.new(tempo);
			var session;
			self.tclock = tc;
			[tc, self.tclock, TempoClock.new(tempo), tempo].debug("creation de cette foutue horloge");

			session = Task {
				self.start_metronome(tempo);
				4.wait;
				if (metro.not) { self.stop_metronome };
				self.start_immediate_recording;
				(dur * tc.beatsPerBar).wait;
				self.stop_immediate_recording;
				action.value;
			};
			session.play(tc);
		},

		start_immediate_recording: { arg self;
			var record_start_time;
			if(self.recording, {
				"already recording!!!".debug;
			}, {
				self.tclock.debug("lecture de cette foutue horloge");
				record_start_time = self.tclock.beats;
				record_start_time.debug("NOW: should be 4 beats");
				self.recording = true;
				self.track = List.new;
				self.book = Dictionary.new;
				self.livebook = Dictionary.new;
				self.lastnote = nil;


				self.nonr = NoteOnResponder { arg src, chan, num, veloc;
					var note, firstnote;
					var start_silence;

					//[TempoClock.beats, TempoClock.nextTimeOnGrid(EventPatternProxy.defaultQuant,0), EventPatternProxy.defaultQuant].debug("nc,tc,dq");
					self.livebook[num] = livesynth.value(num.midicps, veloc/127);
					
					[src, chan, num, veloc].debug("note on");
					note = (
						midinote: num,
						velocity: veloc,
						curtime: self.tclock.beats - s.latency
					);
					if(self.lastnote.isNil, {
						// first note
						start_silence = self.tclock.beats - s.latency - record_start_time;
						if(start_silence < 0, { "Negative dur!!".warning; start_silence = 0; });
						firstnote = (
							midinote: \rest,
							velocity: 0,
							sustain: 0.1,
							start_silence: start_silence,
							default_start_silence: start_silence,
							start_offset: 0,
							end_offset: 0,
							dur: start_silence
						);
						self.track.add(firstnote);
					}, {
						self.lastnote.dur = note.curtime - self.lastnote.curtime
					});
					self.book[num] = note;
					self.lastnote = note;
					self.track.add(note);
					note.debug("nonr note");
				};
				self.noffr = NoteOffResponder { arg src, chan, num, veloc;
					var note;
					self.livebook[num].release;
					note = self.book[num];
					self.book.removeAt(num);
					note.debug("noffr note");
					self.book.debug("noffr book");
					note.sustain = self.tclock.beats - s.latency - note.curtime;
				};
			})
		},

		stop_immediate_recording: { arg self;
			var now = self.tclock.beats;
			now.debug("end recording: NOW");
			if(self.recording, {
				self.nonr.remove;
				self.noffr.remove;
				if(self.lastnote.notNil, {
					self.lastnote.dur = now - self.lastnote.curtime;
					self.track[0].default_end_silence = self.lastnote.dur;
					self.track[0].end_silence = self.lastnote.dur;
				});
				self.recording = false;
			}, {
				"already stoped!!!!".debug;
			})
		},



		start_recording: { arg self;
			if(self.recording, {
				"already recording!!!".debug;
			}, {
				self.recording = true;
				self.track = List.new;
				self.book = Dictionary.new;
				self.livebook = Dictionary.new;
				self.lastnote = nil;

				self.nonr = NoteOnResponder { arg src, chan, num, veloc;
					var note, firstnote;
					var start_silence;

					[TempoClock.beats, TempoClock.nextTimeOnGrid(EventPatternProxy.defaultQuant,0), EventPatternProxy.defaultQuant].debug("nc,tc,dq");
					self.livebook[num] = livesynth.value(num.midicps, veloc/127);
					
					[src, chan, num, veloc].debug("note on");
					note = (
						midinote: num,
						velocity: veloc,
						curtime: TempoClock.beats - s.latency
					);
					if(self.lastnote.isNil, {
						// first note
						start_silence = note.curtime - (TempoClock.nextTimeOnGrid(EventPatternProxy.defaultQuant,0) - EventPatternProxy.defaultQuant);
						firstnote = (
							midinote: \rest,
							velocity: 0,
							sustain: 0.1,
							start_silence: start_silence,
							default_start_silence: start_silence,
							start_offset: 0,
							end_offset: 0,
							dur: start_silence
						);
						if(firstnote.dur < 0, { "Negative dur!!".warning; firstnote.dur = 0; });
						self.track.add(firstnote);
					}, {
						self.lastnote.dur = note.curtime - self.lastnote.curtime
					});
					self.book[num] = note;
					self.lastnote = note;
					self.track.add(note);
					note.debug("nonr note");
				};
				self.noffr = NoteOffResponder { arg src, chan, num, veloc;
					var note;
					self.livebook[num].release;
					note = self.book[num];
					self.book.removeAt(num);
					note.debug("noffr note");
					self.book.debug("noffr book");
					note.sustain = TempoClock.beats - s.latency - note.curtime;
				};
			})
		},

		stop_recording: { arg self;
			if(self.recording, {
				self.nonr.remove;
				self.noffr.remove;
				if(self.lastnote.notNil, {
					self.lastnote.dur = ~rel_nextTimeOnGrid.(self.lastnote.curtime, EventPatternProxy.defaultQuant,0) - self.lastnote.curtime;
					self.track[0].default_end_silence = self.lastnote.dur;
					self.track[0].end_silence = self.lastnote.dur;
				});
				self.recording = false;
			}, {
				"already stoped!!!!".debug;
			})
		}


	);
	prec;

};
)
