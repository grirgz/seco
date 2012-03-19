(

~event_rel_to_abs = { arg li;	
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

		

		get_main: { arg self; main },

		is_next_free: { arg self, kind;
			self.next_free[kind] < ~general_sizes.midi_cc[kind]
		},

		assign_first: { arg self, kind, param;
			var ccpath;	
			//"HEINNNN".debug;
			if(self.is_next_free(kind)) { //FIXME: hardcoded value
				ccpath = [kind, self.next_free[kind]];
				//[param.name, ccpath, self.next_free].debug("III: assign_first: assigning midi");
				main.commands.bind_param(ccpath, param);
				//main.commands.get_param_binded_ccpath(param).debug("III: assigned ccpath verification");
				self.next_free[kind] = self.next_free[kind] + 1;
				true;
			} {
				("Too much assigned param: no midi controller left."+param.name).warn;
				false;
			};
		},

		assign_adsr: { arg self, param;
			var ccpath;	
			//param.name.debug("midi_center: assign_adsr");
			~adsr_point_order.do { arg key, i;
				ccpath = [\slider, i+4];
				main.commands.bind_param(ccpath, param.get_param(key));
			};
		},

		get_ccpath_assigned_with_given_param: { arg self, param;
			main.commands.get_param_binded_ccpath(param);
		},

		clear_assigned: { arg self, kind;
			self.next_free[kind] = 0;
		},

		get_control_val: { arg self, ccpath;
			//[ccpath, self.cc_states[ccpath]].debug("get_control_val");
			if(self.cc_states[ccpath].isNil) {
				0
			} {
				self.cc_states[ccpath]
			}
		},

		set_control_val: { arg self, ccpath, val;
			//[ccpath, val].debug("===================midi_center: set_control_val");
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

		install_pad_responders: { arg self;
			self.nonr = List.new;
			
			[\pad].do { arg cctype; ~keycode.cakewalk[cctype].do { arg keycode, i;
				var ccpath = [\midi, 0, keycode + 1000]; // add offset to difference from cc
				self.nonr.add( 
					NoteOnResponder ({ arg src, chan, num, veloc;
						main.commands.handle_midi_key(main.model.current_panel, ccpath);
					},
					nil,
					nil,
					keycode,
					nil
					)
				)
			}};
		},

		uninstall_pad_responders: { arg self;
			self.nonr.do { arg x; x.remove; };
			self.nonr = nil;
		},

		uninstall_responders: { arg self;
			self.ccresp.do { arg x; x.remove };
			self.ccresp = nil;
		}
	);
	CCResponder.removeAll;
	NoteOnResponder.removeAll;
	mc.install_responders;
	mc;
};

~get_midi_control_handler = { arg midi_center, param;
	// param interface: name, set_norm_val, get_norm_val, changed, spec
	// changed notifications: midi_val(number), blocked(bool), label
	(
		blocked: \inf,

		set_val: { arg self, val, cell=nil;
			//[param.name, val].debug("get_midi_control_handler:set_val");
			self.unblock_do {
				//debug("get_midi_control_handler:set_val: not blocked");
				//self.get_midi_norm_val.debug("BLAAA1");
				//TODO: implement cell setting
				param.set_norm_val(val);
				//1.debug("get_midi_control_handler:set_val");
				//TODO: recording of val ?
			};
			//2.debug("get_midi_control_handler:set_val");
			param.changed(\midi_val, self.get_midi_val);
			//3.debug("get_midi_control_handler:set_val");
		},

		get_midi_val: { arg self;
			//[self.get_ccpath, midi_center.get_control_val(self.get_ccpath), param.spec.map(midi_center.get_control_val(self.get_ccpath))].debug("get_midi_val");
			param.spec.map(midi_center.get_control_val(self.get_ccpath));
		},

		get_midi_norm_val: { arg self;
			//[param.name, midi_center.get_ccpath_assigned_with_given_param(param)].debug("get_midi_norm_val");
			midi_center.get_control_val(self.get_ccpath);
		},

		get_ccpath: { arg self;
			//self.ccpath;
			//[param.name, midi_center.get_param_binded_ccpath(param)].debug("III: get_ccpath");
			midi_center.get_ccpath_assigned_with_given_param(param);
		},

		label: { arg self;
			//param.name.debug("get_midi_control_handler: label");
			if(self.get_ccpath.isNil) {
				"X"
			} {
				//(self.get_ccpath[0].asString[0].asString ++ (self.get_ccpath[1]+1).asString).debug("get_midi_control_handler: label: label choosed");
				self.get_ccpath[0].asString[0].asString ++ (self.get_ccpath[1]+1).asString;
			}
		},

		set_ccpath: { arg self, val;
			//param.name.debug("get_midi_control_handler: set_ccpath (deprecated method??)");
			self.ccpath = val;
			self.refresh;
		},

		unblock_do: { arg self, fun;
			var midi_val, param_val;
			var main;

			//1.debug("unblock_do: midi_val, param_val");
			midi_val = self.get_midi_norm_val;
			param_val = param.get_norm_val;

			//[midi_val, param_val].debug("unblock_do: midi_val, param_val");
			
			//self.get_midi_norm_val.debug("BLAAA2");

			main = midi_center.get_main;
		
			switch(self.blocked,
				\not, fun,
				\sup, {
					if( midi_val <= param_val , {
						//self.get_midi_norm_val.debug("BLAAA3");
						self.blocked = \not;
						//self.get_midi_norm_val.debug("BLAAA7");
						param.changed(\blocked, false);
						//self.get_midi_norm_val.debug("BLAAA8");
						fun.value;
						//self.get_midi_norm_val.debug("BLAAA9");
					});
				},
				\inf, {
					if( midi_val >= param_val , {
						//self.get_midi_norm_val.debug("BLAAA4");
						//[main.commands.ccpathToParam.keys, main.commands.paramToCcpath.values].debug("FFFFFFFFFFFFUUUUUUUUUUU");
						self.blocked = \not;
						//[main.commands.ccpathToParam.keys, main.commands.paramToCcpath.values].debug("FFFFFFFFFFFFUUUUUUUUUUU2");
						//self.get_midi_norm_val.debug("BLAAA5");
						param.changed(\blocked, false);
						//self.get_midi_norm_val.debug("BLAAA6");
						fun.value;
						//self.get_midi_norm_val.debug("BLAAA6.5");
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
			//param.name.debug("get_midi_control_handler: refresh");
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


~do_record_session = { arg main, preclap_action, postclap_action, start_action, end_action, preend_action={};
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
			(dur-0.01).wait;
			preend_action.();
			0.01.wait;
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

// should be named make_midi_note_recorder
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
		lastnote: nil,
		recording: false,
		tclock: nil,

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
				self.track = List.new;
				self.start_tempo_recording(
					// end_action
					{ 
						var sum = 0;

						nline.set_next_notes(self.track, pman.get_record_length);
						player.mute(false);
						nline.changed(\recording, false);

						self.track.debug("66666666666666666666666666666- this is record!!!!");

						self.track.do { arg no;
							sum = sum + no.dur;
						};
						sum.debug("total sum");
						finish_action.();
					},
					// preend_action
					{
						"**** preend_action".debug;
						if(pman.node_is_playing(player)) {
							"**** preend action: node is playing: seting wait note".debug;
							nline.set_wait_note(self.track[0]);
						}
					}
				);
				nline.changed(\recording, true); 
				player.mute(true);
			}
		},

		cancel_recording: { arg self;
			self.nonr.remove;
			self.noffr.remove;
			self.livebook.keysValuesDo { arg k, v; v.release }; // free last synths
			self.recording = false;
		},

		start_tempo_recording: { arg self, action, preend_action;

			~do_record_session.(main,
				// preclap_action
				{ arg tclock;
					self.tclock = tclock;
					if(self.enable_pretrack) { self.start_immediate_recording; };
				},
				// postclap_action
				{
					if(self.enable_pretrack) { 
						// TODO: check if pretrack works with assigning track to player note list at the begining
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
				},
				// preend_action
				preend_action
			);

		},

		start_immediate_recording: { arg self;
			var record_start_time;
			var latency = s.latency + 0.2;
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
					curtime: self.tclock.beats - latency
					//curtime: self.tclock.beats // ben pourquoi y'a plus de latency ?
				);
				if(note.curtime < 0) { "Negative curtime!!".debug; note.curtime = 0 };
				if(slotnum.notNil) { note.slotnum = slotnum };

				if(self.lastnote.isNil, {
					// first note
					start_silence = self.tclock.beats - latency - record_start_time;
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
					self.lastnote.dur = note.curtime - self.lastnote.curtime;
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
				note.sustain = self.tclock.beats - latency - note.curtime;
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
			
			// if noteoff occurs after recording end, set to sustain until track end
			self.track.do { arg no;
				if(no.sustain.isNil) {
					no.sustain = now - no.curtime
				}
			};

			self.recording = false;
			self.lastnote = nil;
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

~make_midi_cc_recorder = { arg player, main;
	var prec, livesynth, pman;
	NoteOnResponder.removeAll;
	NoteOffResponder.removeAll;
	pman = main.play_manager;

	prec = (
		ccr: nil,
		recording: false,
		tclock: nil,
		paramlist: nil,
		recblobs: nil,

		get_paramlist: { arg self;
			var param, ccpath, res = List.new;
			player.get_all_args.do { arg paramname;
				param = player.get_arg(paramname);
				if(param.classtype == \control) {
					ccpath = param.midi.get_ccpath;
					if(ccpath.notNil && {ccpath[0] == \knob}) {
						res.add(param);
					}
				}
			};
			res;
		},

		get_selected_paramlist: { arg self;
			[ player.get_arg(player.get_selected_param) ];
		},

		player_start_tempo_recording: { arg self, finish_action={};
			var nline;

			self.paramlist = self.get_selected_paramlist;
			self.recblobs = self.paramlist.collect { arg param;
				~make_midi_cc_recorder_blob.(param);
			};
			"STARTCCRECORD!!".debug; 
			if(self.recording == true) {
				"recorder: already recording!!!".debug;
			} {
				self.recording = true;
				self.start_tempo_recording(
					// end_action
					{ 
						finish_action.();
					},
					// preend_action
					{
						"**** preend_action".debug;
					}
				);
			}
		},

		cancel_recording: { arg self;
			self.recblobs.do { arg recblob;
				recblob.stop_immediate_recording;
			};
		},

		start_tempo_recording: { arg self, action, preend_action;

			~do_record_session.(main,
				// preclap_action
				{ arg tclock;
					//self.tclock = tclock;
				},
				// postclap_action
				{
				},
				// start_action
				{ arg tclock;
					self.tclock = tclock;
					self.recblobs.do { arg recblob;
						recblob.start_immediate_recording(tclock);
					}
				},
				// end_action
				{
					self.recblobs.do { arg recblob;
						recblob.stop_immediate_recording;
						recblob.save_record_to_param;
					};
					action.value;
				},
				// preend_action
				preend_action
			);

		}


	);
	prec;

};

~ccpath_to_ccnum = { arg ccpath;
	~keycode.cakewalk[ccpath[0]][ccpath[1]]
};

~make_midi_cc_recorder_blob = { arg param;
	(
		recording: false,
		tclock: nil,

		start_immediate_recording: { arg self, tclock;
			var record_start_time;
			var latency = s.latency + 0.2;
			var ccnum;
			self.tclock = tclock;
			record_start_time = self.tclock.beats;
			self.recording = true;
			self.track = List.new;
			self.lastnote = nil;
			
			param.midi.get_ccpath.debug("make_midi_cc_recorder_blob: start_immediate_recording: param.midi.get_ccpath");

			ccnum = ~ccpath_to_ccnum.(param.midi.get_ccpath);
			[param.name, ccnum].debug("!!!!!CC RECORDING!!!!!! start");
			self.ccr = CCResponder ({ arg src, chan, num, val;
					var note, firstnote;
					var start_silence;
					var slotnum = nil;
					val = val/127;

					//[TempoClock.beats, TempoClock.nextTimeOnGrid(EventPatternProxy.defaultQuant,0), EventPatternProxy.defaultQuant].debug("nc,tc,dq");

					// TODO: change param
					param.bus.set_norm_val(val);
					
					[src, chan, num, val].debug("cc msg");
					note = (
						val: val,	// FIXME: store as normalised or mapped value ? (currently normalised)
						curtime: self.tclock.beats - latency
						//curtime: self.tclock.beats // ben pourquoi y'a plus de latency ?
					);
					if(note.curtime < 0) { "Negative curtime!!".debug; note.curtime = 0 };

					if(self.lastnote.isNil, {
						// first note
						start_silence = self.tclock.beats - latency - record_start_time;
						//start_silence = self.tclock.beats - record_start_time;

						if(start_silence < 0, { "Negative dur!!".warn; start_silence = 0; });
						firstnote = (
							val: param.get_val,
							start_silence: start_silence,
							default_start_silence: start_silence,
							start_offset: 0,
							end_offset: 0,
							dur: start_silence
						);
						self.track.add(firstnote);
					}, {
						self.lastnote.dur = note.curtime - self.lastnote.curtime;
					});
					self.lastnote = note;
					self.track.add(note);
					note.debug("nonr note");
				},
				nil, // any source,
				nil, // any chan,
				ccnum, // only current param ccnum
				nil // any val
			);
		},

		stop_immediate_recording: { arg self;
			var now = self.tclock.beats;
			var sum = 0;
			now.debug("end recording: NOW");
			self.ccr.remove;
			if(self.lastnote.notNil, {
				self.lastnote.dur = now - self.lastnote.curtime;
				self.track[0].default_end_silence = self.lastnote.dur;
				self.track[0].end_silence = self.lastnote.dur;
			});

			self.track = self.track.reject({ arg no; 
				if(no.dur == 0) {
					no.debug("reject note!");
					true;
				} {
					false;
				}
			});
			"notes kept:".debug;
			self.track.printAll;

			self.track.do { arg no;
				sum = sum + no.dur;
			};
			sum.debug("TOTAL SUM");
			
			self.recording = false;
			self.lastnote = nil;
		},

		save_record_to_param: { arg self;
			param.recordbus.set_record(self.track);
		}


	);

};


~make_midi_liveplayer = { arg player, main;
	var prec, livesynth, pman;
	NoteOnResponder.removeAll;
	NoteOffResponder.removeAll;
	pman = main.play_manager;

	prec = (
		nonr: nil,
		noffr: nil,
		livebook: Dictionary.new,

		start_liveplay: { arg self;
			var record_start_time;
			livesynth = player.get_piano;
			self.livebook = Dictionary.new;

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
			};
			self.noffr = NoteOffResponder { arg src, chan, num, veloc;
				var note;
				[self.livebook[[chan,num]], [src, chan, num, veloc]].debug("note off");
				self.livebook[[chan,num]].release;
			};
		},

		stop_liveplay: { arg self;
			"".debug("end liveplay: NOW");
			self.nonr.remove;
			self.noffr.remove;
			self.livebook.keysValuesDo { arg k, v; v.release }; // free last synths
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
					player.get_arg(\stepline).seq.change({ [1] });
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
			self.buf = Buffer.alloc(s, 44100 * pman.get_record_length_in_seconds, 2); 
			self.buf.bufnum.debug("************************created buffer");

			~do_record_session.(main,
				// preclap_action
				{ arg tclock;
					"TTpreclap".debug;
					self.tclock = tclock;
				},
				// postclap_action
				{
					"TTpostclap".debug;
				},
				// start_action
				{ arg tclock;
					"TTstart".debug;
					self.tclock = tclock;
					self.start_immediate_recording(pman.get_record_length);
				},
				// end_action
				{
					"TTend".debug;
					self.stop_immediate_recording;
					action.value;
				}
			);
			//[tc, self.tclock, TempoClock.new(tempo), tempo].debug("creation de cette foutue horloge");
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


