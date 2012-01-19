(
~merge_notetracks = { arg no1, no2;
	var ano1, ano2, res;
	var makeabs, makerel, time = 0, last = 0, elm;
	makerel = { arg li;
		var res = List.new, elm, time;
		0.for(li.size-1) { arg x;
			elm = li[x].copy;
			if( x == (li.size-1) ) {
			} {
				elm.dur = li[x+1].time - li[x].time
			};
			elm.time = nil; // tidy up
			res.add(elm);
		};
		res;
	};
	makeabs = { arg li;	
		var res = List.new, elm, time;
		0.for(li.size-1) { arg x;
			x.debug("iter");
			elm = li[x].copy;
			if(x == 0) {
				elm.time = 0;
			} {
				elm.time = li[x-1].dur + res[x-1].time;
			};
			res.add(elm);
		};
		res;
	};
	ano1 = makeabs.(no1);
	ano2 = makeabs.(no2);
	
	res = ano1 ++ ano2;
	res.sort({ arg a, b; 
		if(a.time == b.time) {
			// always put header in first in case of equality
			a.start_silence.notNil
		} {
			a.time < b.time
		}
	});
	res = makerel.(res);

	res = res[1..]; // remove double rest
	res[0].start_silence = res[0].dur;
	res[0].end_silence = res.last.dur;

	res;

};

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
			[\slider,\knob].do { arg cctype; ~keycode.cakewalk[cctype].do { arg keycode, i;

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
			};
			[\button, \toggle].do { arg cctype; ~keycode.cakewalk[cctype].do { arg keycode, i;

					self.ccresp.add( CCResponder({ |src,chan,num,value|
							var ccpath = [\midi, 0, keycode];
							var val = value/127;
							if(val == 1) {
								main.commands.handle_midi_key(main.model.current_panel, ccpath);
							};
						},
						nil, // any source
						nil, // any channel
						keycode, // any CC number
						nil // any value
						)
					)
				}
			};

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
	// param interface: name, set_norm_val, get_norm_val, changed, spec
	// changed notifications: midi_val(number), blocked(bool), label
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


~do_record_session = { arg main, preclap_action, postclap_action, start_action, end_action;
	var tc, supertc;
	var session, supersession;
	var pman = main.play_manager;
	var dur = pman.get_record_length;
	var metronome = pman.use_metronome;
	var metronome_dur = pman.syncclap_dur;

	if(main.play_manager.is_playing) {
		main.play_manager.start_new_session;
		tc = main.play_manager.get_clock;

		session = Task {
			if (metronome) { pman.start_metronome(tc, dur) };
			start_action.(tc);
			main.play_manager.changed(\head_state, \record);
			dur.wait;
			end_action.();
		};
		session.play(tc, quant:dur);
	} {
		supertc = TempoClock.new(pman.get_tempo);
		session = Task {
			if (metronome) { pman.start_metronome(tc, dur) };
			start_action.(tc);
			main.play_manager.changed(\head_state, \record);
			dur.wait;
			end_action.();
		};
		supersession = Task {
			preclap_action.(supertc);
			pman.start_metronome(supertc, metronome_dur);
			metronome_dur.wait;
			postclap_action.();

			main.play_manager.set_record_length(dur); // changed by pman.start_metronome
			main.play_manager.start_new_session;
			tc = main.play_manager.get_clock;
			session.play(tc, quant:dur);
		};
		supersession.play(supertc, quant:1);
	};
};

~make_midi_recorder = { arg player, main;
	var prec, livesynth, pman;
	NoteOnResponder.removeAll;
	NoteOffResponder.removeAll;
	pman = main.play_manager;

	prec = (
		nonr: nil,
		noffr: nil,
		track: List.new,
		pretrack: List.new,
		enable_pretrack: false,
		book: Dictionary.new,
		livebook: Dictionary.new,
		metronome_dur: 4,
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

		player_start_tempo_recording: { arg self, finish_action={};
			var nline;

			nline = if(player.get_mode == \sampleline) {
				player.get_arg(\sampleline);
			} {
				player.get_arg(\noteline);
			};
			"STARTRECORD!!".debug; 
			if(self.recording == true) {
				"recorder: already recording!!!".debug;
			} {
				self.recording = true;
				player.current_mode.debug("player.current_mode");
				nline.name.debug("nline.name");
				self.start_tempo_recording({ 
					var sum = 0;

					nline.changed(\recording, false);
					self.track.debug("66666666666666666666666666666- this is record!!!!");

					self.track.do { arg no;
						sum = sum + no.dur;
					};
					sum.debug("total sum");
					nline.set_notes(self.track);
					nline.mute(false);
					nline.changed(\notes);
					finish_action.();
				});
				nline.changed(\recording, true); 
				nline.mute(true);
			}
		},

		cancel_recording: { arg self;
			self.stop_immediate_recording;
		},

		start_tempo_recording: { arg self, action;

			~do_record_session.(main,
				// preclap_action
				{ arg tclock;
					self.tclock = tclock;
					if(self.enable_pretrack) { self.start_immediate_recording; };
				},
				// postclap_action
				{
					if(self.enable_pretrack) { 
						self.stop_immediate_recording; 
						self.pretrack = self.track;
						self.track = List.new;
					};
				},
				// start_action
				{ arg tclock;
					self.tclock = tclock;
					self.start_immediate_recording;
				},
				// end_action
				{
					// processing early notes (putting them at the end of the loop)
					if(self.pretrack.size > 0) {
						self.pretrack.debug("66666666666666666666666this is pretrack");
						self.track.debug("66666666666666666666666this is track before merging");
						self.pretrack = self.pretrack.collect { arg x; x.ptdur = x.dur };
						self.pretrack[0].dur = self.pretrack[0].dur + (pman.get_record_length - pman.syncclap_dur);
						self.track = ~merge_notetracks.(self.track, self.pretrack);
					} {
						"666666666666666666666666".debug("no pretrack");	
					};

					self.stop_immediate_recording;
					action.value;
				}
			);

			//[tc, self.tclock, TempoClock.new(tempo), tempo].debug("creation de cette foutue horloge");
//			if(main.play_manager.is_playing) {
//				main.play_manager.start_new_session;
//				tc = main.play_manager.get_clock;
//				self.tclock = tc;
//				tc.debug("jcomprendpas");
//
//				session = Task {
//					//self.start_metronome(tempo);
//					//4.wait;
//
//					"2jcomprendpas".debug;
//					if (metro) { pman.start_metronome(tc, dur) };
//					self.start_immediate_recording;
//					main.play_manager.changed(\head_state, \record);
//					"3jcomprendpas".debug;
//					dur.wait;
//					"4jcomprendpas".debug;
//					self.stop_immediate_recording;
//					//self.stop_metronome; // auto-stop
//					action.value;
//				};
//				session.play(tc, quant:dur);
//			} {
//				supertc = TempoClock.new(tempo);
//				session = Task {
//					//self.start_metronome(tempo);
//					//4.wait;
//
//					"2jcomprendpas".debug;
//					self.start_immediate_recording;
//					main.play_manager.changed(\head_state, \record);
//					"3jcomprendpas".debug;
//					dur.wait;
//					"4jcomprendpas".debug;
//					self.stop_immediate_recording;
//
//					//self.stop_metronome;
//
//					// processing early notes (putting them at the end of the loop)
//					if(self.pretrack.size > 0) {
//						self.pretrack.debug("66666666666666666666666this is pretrack");
//						self.track.debug("66666666666666666666666this is track before merging");
//						self.pretrack = self.pretrack.collect { arg x; x.ptdur = x.dur };
//						self.pretrack[0].dur = self.pretrack[0].dur + (dur - self.metronome_dur);
//						self.track = ~merge_notetracks.(self.track, self.pretrack);
//					} {
//						"666666666666666666666666".debug("no pretrack");	
//					};
//
//					action.value;
//				};
//				supersession = Task {
//					pman.start_metronome(supertc, self.metronome_dur);
//					self.metronome_dur.debug("START METRONOME");
//
//					self.tclock = supertc;
//					if(self.enable_pretrack) { self.start_immediate_recording; };
//
//					self.metronome_dur.wait;
//
//					if(self.enable_pretrack) { 
//						self.stop_immediate_recording;
//						self.pretrack = self.track;
//						self.track = List.new;
//					};
//
//					//main.play_manager.changed(\stop_counter); // pre-metronome counter
//					main.play_manager.set_record_length(dur); // pre-metronome counter
//					dur.debug("START ACTUAL RECORDING");
//					if (metro.not) { self.stop_metronome };
//					main.play_manager.start_new_session;
//					tc = main.play_manager.get_clock;
//					self.tclock = tc;
//					tc.debug("jcomprendpas");
//
//					session.play(tc, quant:dur);
//				};
//				supersession.play(supertc, quant:1);
//
//			};
		},

		start_immediate_recording: { arg self;
			var record_start_time;
			livesynth = player.get_piano;
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
				var slotnum = nil;

				//[TempoClock.beats, TempoClock.nextTimeOnGrid(EventPatternProxy.defaultQuant,0), EventPatternProxy.defaultQuant].debug("nc,tc,dq");
				if(chan == 9) {
					// pad button pressed
					slotnum = ~samplekit_manager.midinote_to_slot(num);
					self.livebook[[chan, num]] = livesynth.value(slotnum, veloc/127);
				} {
					self.livebook[[chan, num]] = livesynth.value(num.midicps, veloc/127);
				};
				
				[src, chan, num, veloc].debug("note on");
				note = (
					midinote: num,
					velocity: veloc/127,
					curtime: self.tclock.beats - s.latency
					//curtime: self.tclock.beats // ben pourquoi y'a plus de latency ?
				);
				if(note.curtime < 0) { "Negative curtime!!".debug; note.curtime = 0 };
				if(slotnum.notNil) { note.slotnum = slotnum };

				if(self.lastnote.isNil, {
					// first note
					start_silence = self.tclock.beats - s.latency - record_start_time;
					//start_silence = self.tclock.beats - record_start_time;

					if(start_silence < 0, { "Negative dur!!".warn; start_silence = 0; });
					firstnote = (
						midinote: \rest,
						slotnum: \rest,
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
				self.livebook[[chan,num]].release;
				note = self.book[num];
				self.book.removeAt(num);
				note.debug("noffr note");
				self.book.debug("noffr book");
				note.sustain = self.tclock.beats - s.latency - note.curtime;
			};
		},

		stop_immediate_recording: { arg self;
			var now = self.tclock.beats;
			now.debug("end recording: NOW");
			self.nonr.remove;
			self.noffr.remove;
			self.livebook.keysValuesDo { arg k, v; v.release }; // free last synths
			if(self.lastnote.notNil, {
				self.lastnote.dur = now - self.lastnote.curtime;
				self.track[0].default_end_silence = self.lastnote.dur;
				self.track[0].end_silence = self.lastnote.dur;
			});
			self.recording = false;
			self.lastnode = nil;
		},



		start_recording: { arg self;
			if(self.recording, {
				"already recording!!!".debug;
			}, {
				livesynth = player.get_piano;
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
						velocity: veloc/127,
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

~make_audio_recorder = { arg player, main;
	var pman = main.play_manager;

	var obj = (
		recording: false,
		buf: nil,
		metronome: { arg self; { arg tempo=1; Ringz.ar(Impulse.ar(tempo), [401, 400], 1/tempo) } },
		tclock: nil,

		start_metronome: { arg self, tempo;
			self.metro_player = self.metronome(tempo).play(args:[\tempo, tempo]);
			self.tbclock = TempoBusClock(self.metro_player, tempo);
		},

		stop_metronome: { arg self;
			self.metro_player.release;
		},
		player_start_tempo_recording: { arg self, finish_action={};
			var nline;

			"START AUDIO RECORD!!".debug; 
			if(self.recording == true) {
				"recorder: already recording!!!".debug;
			} {
				self.recording = true;
				if(player.get_arg(\bufnum).has_custom_buffer) {
					player.get_arg(\bufnum).buffer.debug("************************freeing buffer");
					player.get_arg(\bufnum).buffer.free;
				};
				self.start_tempo_recording({ 
					player.get_arg(\bufnum).set_custom_buffer(self.buf, "AudioInput");
					player.get_arg(\dur).change_kind(\scalar);
					player.get_arg(\dur).set_val(main.play_manager.get_record_length);
					main.get_clock.debug("quoiiiiiiiiiiiii");
					player.get_arg(\sustain).set_val(main.play_manager.get_record_length / main.play_manager.get_clock.tempo);
					finish_action.();
				});
			}
			
		},

		start_tempo_recording: { arg self, action;
			var tc, supertc;
			var session, supersession;
			self.buf = Buffer.alloc(s, 44100 * pman.get_record_length, 2); 
			self.buf.bufnum.debug("************************created buffer");

			~do_record_session.(main,
				// preclap_action
				{ arg tclock;
					self.tclock = tclock;
				},
				// postclap_action
				{
				},
				// start_action
				{ arg tclock;
					self.tclock = tclock;
					self.start_immediate_recording;
				},
				// end_action
				{
					self.stop_immediate_recording;
					action.value;
				}
			);
			//[tc, self.tclock, TempoClock.new(tempo), tempo].debug("creation de cette foutue horloge");
//			if(main.play_manager.is_playing) {
//				main.play_manager.start_new_session;
//				tc = main.play_manager.get_clock;
//				self.tclock = tc;
//				tc.debug("jcomprendpas");
//
//				session = Task {
//					//self.start_metronome(tempo);
//					//4.wait;
//					 // FIXME: somewhere, 0.2s are lost :s
//					"2jcomprendpas".debug;
//					if (metro) { self.start_metronome };
//					self.start_immediate_recording(dur);
//					main.play_manager.changed(\head_state, \record);
//					"3jcomprendpas".debug;
//					dur.wait;
//					"4jcomprendpas".debug;
//					self.stop_immediate_recording;
//					self.stop_metronome;
//					action.value;
//				};
//				session.play(tc, quant:dur);
//			} {
//				supertc = TempoClock.new(tempo);
//				session = Task {
//					//self.start_metronome(tempo);
//					//4.wait;
//
//					"2jcomprendpas".debug;
//					self.start_immediate_recording(dur);
//					main.play_manager.changed(\head_state, \record);
//					"3jcomprendpas".debug;
//					dur.wait;
//					"4jcomprendpas".debug;
//					self.stop_immediate_recording;
//					self.stop_metronome;
//
//					action.value;
//				};
//				supersession = Task {
//					self.start_metronome(tempo);
//
//					self.tclock = supertc;
//					//self.start_immediate_recording;
//
//					4.wait;
//
//					//self.stop_immediate_recording;
//					//self.pretrack = self.track;
//					//self.track = List.new;
//
//					if (metro.not) { self.stop_metronome };
//					main.play_manager.start_new_session;
//					tc = main.play_manager.get_clock;
//					self.tclock = tc;
//					tc.debug("jcomprendpas");
//
//					session.play(tc, quant:dur);
//				};
//				supersession.play(supertc, quant:1);
//
//			};
		},

		start_immediate_recording: { arg self, dur=1;
			"in start_immediate_recording".debug;
			if(self.buf.notNil) {
				self.recnode = Pbind(
					\instrument, \record_input, 
					\bufnum, self.buf,
					\dur, dur,
					\sustain, dur / main.play_manager.get_clock.tempo,
					\monitor, Pn(1,1)
				).trace.play;
			} {
				"make_audio_recorder: buf is nil".debug;
			}
		},
		stop_immediate_recording: { arg self;
			if(self.recnode.notNil) {
				self.recnode.stop;
			} {
				"make_audio_recorder: recnode is nil".debug;
			}
		}

	);
	obj;
};

)


///////////////////////////////////////////////////////////////////////////
///////////		MIDI (deprecated)
///////////////////////////////////////////////////////////////////////////
//
//~midi_interface = (
//	// TODO: obsolete, ported to midi_center
//	next_free: (
//		slider: 0,
//		knob: 0
//	),
//	registry: List[],
//
//	cc_val: Dictionary[
//		[\slider, 0] -> 0
//
//	],
//
//	assign_adsr: { arg self, param;
//		var col = Dictionary.new;
//		var mc = ();
//		~adsr_point_order.do { arg key, i;
//			col[key] = ~make_midi_control.([\slider, i+4], self.cc_val, param.get_param(key))
//		};
//		mc.col = col;
//		mc.kind = \adsr;
//		mc.destroy = { arg self; self.col.do(_.destroy) };
//		self.registry.add( mc );
//		mc;
//	},
//
//	assign_master: { arg self, param;
//		var mc;
//		mc = ~make_midi_control.([\slider, 8], self.cc_val, param);
//		self.registry.add( mc );
//		mc;
//	},
//
//	assign_first: { arg self, kind, param;
//		var mc;
//		mc = ~make_midi_control.([kind, self.next_free[kind]], self.cc_val, param);
//		self.registry.add( mc );
//		// FIXME: set max free val (adsr hold the last sliders)
//		self.next_free[kind] = self.next_free[kind] + 1;
//		mc;
//	},
//
//	clear_assigned: { arg self, kind;
//		self.next_free[kind] = 0;
//		self.registry.copy.do { arg mc;
//			// FIXME: what is this kind member ?
//			if(mc.kind == kind, {
//				mc.destroy;
//				self.registry.remove(mc);
//			});
//		};
//	}
//
//);
//
//
//~make_midi_control = { arg ccid, cc_val, param; 
//	// param interface:
//	// - classtype
//	// - get_norm_val
//	// - set_norm_val
//	// - selected
//	// - spec
//	var midi;
//	var sc_param;
//	var get_midi_val = { cc_val[ccid] ?? 0.5};
//	var param_val = { param.get_norm_val };
//
//	//param.debug("make_midi_control param");
//
//	midi = (
//		blocked: \not,
//		ccid: nil,
//		midi_val: get_midi_val.(),
//		label: \void,
//		name: param.name,
//		recording: false,
//		mapped_val: { arg self;
//			param.spec.map(get_midi_val.())
//		},
//
//		refresh: { arg self;
//			self.changed(\label);
//			self.changed(\blocked);
//			self.changed(\midi_val);
//		},
//
//		set_value: { arg self, cc_value;
//			cc_val[ccid] = cc_value;
//			self.midi_val = cc_value;
//			self.changed(\midi_val);
//
//			if(self.recording.not, {
//				if(ccid.notNil, {
//					self.unblock_do({
//						param.set_norm_val(cc_value);
//					});
//				}, {
//					param.set_norm_val(cc_value);
//				});
//			});
//		},
//
//		set_midi_value: { arg self, cc_value;
//			cc_val[ccid] = cc_value;	
//			self.midi_val = cc_value;
//			self.block;
//			self.changed(\midi_val);
//		},
//
//		get_midi_val: { arg self; get_midi_val.() },
//
//		unblock_do: { arg self, fun;
//			var cc_value, param_value;
//
//		
//			//param.debug("make_midi_control.unblock_do param");
//			cc_value = get_midi_val.();
//			param_value = param.get_norm_val.();
//			param.name.debug("midi unblock param name");
//			param_value.debug("midi unblock param value");
//
//			switch(self.blocked,
//				\not, fun,
//				\sup, {
//					if( cc_value <= param_value , {
//						self.blocked = \not;
//						self.changed(\blocked);
//						fun.value;
//					});
//				},
//				\inf, {
//					if( cc_value >= param_value , {
//						self.blocked = \not;
//						self.changed(\blocked);
//						fun.value;
//					});
//				}
//			);
//
//		},
//		
//		block: { arg self;
//			var cc_value, param_value;
//
//			cc_value = get_midi_val.();
//			param_value = param.get_norm_val.();
//			//param.name.debug("midi block param name");
//			//param_value.debug("midi block param value");
//
//			case 
//				{ cc_value > param_value } {
//					self.blocked = \sup;
//					self.changed(\blocked);
//				}
//				{ cc_value < param_value } {
//					self.blocked = \inf;
//					self.changed(\blocked);
//				}
//				{ true } {
//					self.blocked = \not;
//					self.changed(\blocked);
//				}
//		},
//
//		assign_cc: { arg self, ccid;
//			self.ccid = ccid;
//			 if(ccid.notNil, {
//				self.label = (ccid[0].asString[0] ++ (ccid[1]+1)).asSymbol;
//				self.changed(\label);
//				self.ccresp = CCResponder({ |src,chan,num,value|
//						//[src,chan,num,value].debug("==============CCResponder");
//						//param.classtype.debug("ccresp current_kind");
//						//param.selected.debug("ccresp selected");
//						//cc_val.debug("ccresp cc_val");
//						if(param.classtype == \adsr, {
//							if(param.selected == 1, {
//								self.set_value(value/127);
//							}, {
//								self.set_midi_value(value/127);
//							});
//						}, {
//							self.set_value(value/127);
//						});
//					},
//					nil, // any source
//					nil, // any channel
//					~cakewalk[ccid[0]][ccid[1]], // any CC number
//					nil // any value
//				);
//				}, {
//					"assigning nil ccid".debug("WARNING");
//					"void"
//				});
//
//		},
//
//		destroy: { arg self;
//			self.ccresp.remove;
//		}
//	);
//
//	midi.assign_cc(ccid);
//	midi.block.();
//
//	sc_param = SimpleController(param);
//	sc_param.put(\kind, { arg obj; midi.block.() });
//	sc_param.put(\selected_cell, { arg obj; "BLOK1".debug; midi.block.() });
//	sc_param.put(\selected, { arg obj; "BLOK2".debug; midi.block.() });
//
//
//	midi;
//
//};
//
//~make_midi_kb_control = { arg player, editplayer;
//	// record cc change dynamicaly in param cells with piano record button
//	// TODO: obsolete, must recode, disabled
//	var nonr, noffr, param, destroy, rec, localknob;
//	
//	// init
//	NoteOnResponder.removeAll; // TODO: must save others responders ?
//
//	nonr = NoteOnResponder { arg src, chan, num, veloc;
//		param = editplayer.controller.get_selected_param.();
//		param.set_val(num.midicps);
//	};
//
//	rec = CCResponder({ |src,chan,num,value|
//			var param = editplayer.controller.get_selected_param;
//			var tickline;
//			tickline = player.get_arg(\stepline); 
//
//			if(param.midi.notNil && param.midi.recording.notNil, {
//				if(param.midi.recording, {
//					param.midi.recording = false;
//					param.midi.changed(\recording);
//					tickline.tick = { 
//						//"TICK!!!".postln;
//					};
//				}, {
//					param.midi.recording = true;
//					param.midi.changed(\recording);
//					// TODO : make the rec works with noteline
////					if(param.noteline, {
////						TempoClock.schedAbs(TempoClock.beats.roundUp( player.get_arg(\segdur).get_val ).debug("noteline tick"), {
////							param.midi.get_midi_val.debug("set TICK");
////							param.seq.set_norm_val(param.midi.get_midi_val, idx);
////							if(param.midi.recording, {
////								TempoClock.beats.roundUp( player.get_arg(\segdur).get_val )
////							}, {
////								nil // dont reschedule
////							}
////						})
////					}, {
//						tickline.tick = { arg obj, idx;
//							param.midi.get_midi_val.debug("set TICK");
//							param.seq.set_norm_val(param.midi.get_midi_val, idx);
//						};
////					});
//				});
//			});
//		},
//		nil, // any source
//		nil, // any channel
//		~cakewalk.button[7], // record button
//		nil // any value
//	);
//
//	localknob = CCResponder({ |src,chan,num,value|
//			//[src,chan,num,value].debug("==============CCResponder");
//			var param = editplayer.controller.get_selected_param;
//			if(param.midi.notNil, {
//				param.midi.set_value(value/127);
//			});
//		},
//		nil, // any source
//		nil, // any channel
//		~cakewalk.knob[8], // last knob
//		nil // any value
//	);
//
//	destroy = { 
//		nonr.remove;
//		rec.remove;
//		localknob.remove;
//	};
//	destroy;
//
//};
