
(
"~/.local/share/SuperCollider/Extensions/seco/seco/eventscore.scd".standardizePath.load;
SynthDef(\rlsaw, { arg out=0, amp=0.1, gate=1, pan=0, spread=0.8, freq=200, doneAction=2, ffreq=500, rq=0.3;
	var sig, sig1, sig2, sig3;
	sig = LFSaw.ar(freq);
	sig = RLPF.ar(sig, ffreq, rq);
	sig = sig * EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.1,0.8,1.4)),gate,doneAction:doneAction);
	sig = Splay.ar(sig, spread, amp, pan);
	Out.ar(out, sig);
}).add;
)

(
	~event_recorder = (
		new: { arg self;
			self = self.deepCopy;
		
			
			self.recclock = TempoClock.default;
			self.recclock.debug("gnill");
			self.is_recording = false;
		
			self;
		},

		start_recording: { arg self;
			debug("start_recording");
			self.start_time = self.recclock.beats;
			self.event_score = ~make_notescore.();
			self.start_immediate_recording;
			self.is_recording = true;
		},

		stop_recording: { arg self;
			debug("stop_recording");
			self.stop_time = self.recclock.beats;
			self.stop_immediate_recording;
			self.event_score.set_end(self.stop_time - self.start_time);
			self.is_recording = false;
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


	Pdef(\bla, Pbind(
		\instrument, \rlsaw,
		\freq, Pseq([100,200,300],inf),
		\ffreq, Pseq([400,800,1300],inf),
	));

	~piano_stream = Pdef(\bla).source.asStream;

	~piano_recorder = (
		parent: ~event_recorder,
		new: { arg self;
			self = ~event_recorder[\new].(self);
		
			
		
			self;
		},

		start_immediate_recording: { arg self;
			var player;
			var notes = Array.newClear(128);
			MIDIdef.noteOn(\pianoOn, {arg ...args; 
				var veloc, num, chan;
				var ev;
				#veloc, num = args;
				[veloc, num].debug("lkj");
				args.debug("mynoteon");
				ev = ~piano_stream.next(Event.default);
				//ev = Pbind(\freq, num.midicps) <> ev;
				ev[\freq] = num.midicps;
				ev[\type] = \on;
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
				notes[num][\type] = \off;
				self.add_event_off(num);
				args.debug("mynoteoff");
				notes[num].play;
			},nil,1); 
			
		},
	);

	~recorder = ~piano_recorder.new;
	~recorder.start_recording;

)
	~recorder.stop_recording;
	~notes = ~recorder.event_score.get_rel_notes
	~recorder.event_score.get_notes
	~recorder.event_score.get_notes_pattern(false)

(
	~pat = Pbind(
		\midinote, Pkey(\event_key),
		\hein, Pfunc{ arg ev; ev.debug("quoi!!"); },
	) <> Pseq(~notes);
	~pat.play;
)
~pat
~notes
(
	~patt = Pbind(

		\hein, Pfunc{ arg ev; ev.debug("quoi!!"); },
	) <> Pseq([
		(
			freq: 200,
		),
		(
			freq: 300,
		),
	]);
~patt.play
)

(
	~patt = Pbind(

		//\midinote, Pkey(\event_key),
		\hein, Pfunc{ arg ev; ev.debug("quoi!!"); },
		\freq, Pfunc{ arg ev; 
			if(ev[\event_key].isNil) {
				50.midicps;
			} {
				ev[\event_key].debug("quoi!!").midicps;
			}
		},
	) <> Pseq(~notes);
~patt.play
)

//MIDIdef.noteOn(\noteOn, {arg ...args; args.debug("mynoteon")},nil,1); // match any noteOn


~ev = ~piano_stream.next(Event.default)
~ev[\type] = \on;
~a = ~ev.play
~a.stop
~a[\type] = \off
~a.play
