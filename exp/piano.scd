OSCdef(\test, { |msg, time, addr, recvPort| 
	\unmatching.postln
}, '/chat', n); // def style

MIDIdef()

(
~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\noteOn, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	~mididict[midinote].stop;
	pat = Pbind(
		//\instrument, 's_ci osc3filter2_l1002',
		\type, \note,
		\note, Pseq([
			//Pseq([0,\r,0,0].scramble,2),
			//Pseq([0,\r,\r,\r].scramble),
			Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
		],inf),
		\filter1_arg1, Pseq([30,600,12,28].scramble,inf),
		\ctranspose, midinote-60,
		\dur, 1/8,
	);

	pat2 = Pn(Mdef.node("ci osc3filter2_l1002").sourcepat);
	pat2 = pat2.collect { arg ev; 
		ev.removeAt(\freq); 
		ev.removeAt(\sustain); 
		ev;
	};
	pat = pat <> pat2;
	pat = pat.trace.play(quant:[1/8]);
	~mididict[midinote] = pat;

}); // match any noteOn
MIDIdef.noteOff(\noteOff, {arg vel, midinote;
	var note = ~mididict[midinote].stop;
	[vel, midinote].postln;
	TempoClock.play({ note.stop; }, quant:0.5)

}); // match any noteOn
)
MIDIdef(\test4).free

MIDIdef.noteOn(\noteon, {arg ...args; "noteon".postln; args.postln}); // match any noteOn

Mdef.node("ci osc3filter2_l1002")


// ======================================================================= simple


(
~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\noteOn, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	~mididict[midinote].stop;
	pat = Pbind(
		\note, Pseq([
			//Pseq([0,\r,0,0].scramble,2),
			//Pseq([0,\r,\r,\r].scramble),
			Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
		],inf),
		\ctranspose, midinote-60,
		\dur, 1/8,
	);
	pat = pat.trace.play(quant:[1/8]);
	~mididict[midinote] = pat;

}); // match any noteOn
MIDIdef.noteOff(\noteOff, {arg vel, midinote;
	var note = ~mididict[midinote];
	[vel, midinote].postln;
	TempoClock.play({ note.stop; }, quant:0.5)

}); // match any noteOn
)

MIDIIn.init

// ======================================================================= seco

(
~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\noteOn, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	//if(~mididict[midinote] = nil) {
	//	~mididict[midinote] = List[midinote];
	//} {
	//	~mididict[midinote].add(midinote);
	//};
	~mididict[midinote].stop;
	//~mididict[midinote] = midinote;
	midinote.postln("mn");
	pat = Pbind(
		//\instrument, 's_ci osc3filter2_l1002',
		\type, \note,
		//\note, Pseq([
		//	//Pseq([0,\r,0,0].scramble,2),
		//	//Pseq([0,\r,\r,\r].scramble),
		//	Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
		//],inf),
		\freq, (Pkey(\freq).cpsmidi + midinote-60).midicps,
		\filter1_arg1, Pseq([30,600,12,28].scramble,inf),
		\ctranspose, midinote-60,
		//\dur, 1/8,
	);

	//pat2 = Pn(Mdef.node("ci osc3filter2_l1003").sourcepat);
	pat2 = Mdef.node("ci osc1_l1003").sourcepat;
	pat2 = pat2.collect { arg ev; 
		//ev.removeAt(\freq); 
		//ev.removeAt(\sustain); 
		ev;
	};
	pat = pat <> pat2;
	pat = pat.trace.play(quant:[1/8]);
	~mididict[midinote] = pat;

}); // match any noteOn
MIDIdef.noteOff(\noteOff, {arg vel, midinote;
	var note = ~mididict[midinote];
	("debug"++note++midinote).postln;
	[vel, midinote].postln;
	TempoClock.play({ 
		note.stop;
	}, quant:0.5)

}); // match any noteOn
)

// ======================================================================= bla


(
~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\noteOn, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	~mididict[midinote].stop;
	pat = Pbind(
		//\instrument, 's_ci osc3filter2_l1002',
		\type, \note,
		\note, Pseq([
			//Pseq([0,\r,0,0].scramble,2),
			//Pseq([0,\r,\r,\r].scramble),
			Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
		],inf),
		\filter1_arg1, Pseq([30,600,12,28].scramble,inf),
		\ctranspose, midinote-60,
		\dur, 1/8,
	);

	pat2 = Pn(Mdef.node("ci osc3filter2_l1002").sourcepat);
	pat2 = pat2.collect { arg ev; 
		ev.removeAt(\freq); 
		ev.removeAt(\sustain); 
		ev;
	};
	pat = pat <> pat2;
	pat = pat.trace.play(quant:[1/8]);
	~mididict[midinote] = pat;

}); // match any noteOn
MIDIdef.noteOff(\noteOff, {arg vel, midinote;
	var note = ~mididict[midinote].stop;
	[vel, midinote].postln;
	TempoClock.play({ note.stop; }, quant:0.5)

}); // match any noteOn
)

// ======================================================================= seco
MIDIContro
				MIDIClient.init;

(
~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\noteOn, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	if(~mididict[midinote] = nil) {
		~mididict[midinote] = List[midinote];
	} {
		~mididict[midinote].add(midinote);
	};
	midinote.postln("mn");
	pat = Pbind(
		//\instrument, 's_ci osc3filter2_l1002',
		\type, \note,
		//\note, Pseq([
		//	//Pseq([0,\r,0,0].scramble,2),
		//	//Pseq([0,\r,\r,\r].scramble),
		//	Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
		//],inf),
		\freq, (Pkey(\freq).cpsmidi + midinote-60).midicps,
		\filter1_arg1, Pseq([30,600,12,28].scramble,inf),
		\ctranspose, midinote-60,
		//\dur, 1/8,
	);

	//pat2 = Pn(Mdef.node("ci osc3filter2_l1003").sourcepat);
	pat2 = Mdef.node("ci osc3filter2_l1003").sourcepat;
	pat2 = pat2.collect { arg ev; 
		ev.removeAt(\freq); 
		//ev.removeAt(\sustain); 
		ev;
	};
	pat = pat <> pat2;
	pat = pat.trace.play(quant:[1/8]);
	~mididict[midinote] = pat;

}); // match any noteOn
MIDIdef.noteOff(\noteOff, {arg vel, midinote;
	var note = ~mididict[midinote].stop;
	("debug"++note++midinote).postln;
	[vel, midinote].postln;
	TempoClock.play({ 
		//note.stop;
		~mididict[midinote].copy.do { arg x; x.stop; };
	}, quant:0.5)

}); // match any noteOn
)

// ======================================================================= seco full pattern

(
~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\noteOn, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	//if(~mididict[midinote] = nil) {
	//	~mididict[midinote] = List[midinote];
	//} {
	//	~mididict[midinote].add(midinote);
	//};
	~mididict[midinote].stop;
	//~mididict[midinote] = midinote;
	midinote.postln("mn");
	pat = Pbind(
		//\instrument, 's_ci osc3filter2_l1002',
		\type, \note,
		//\note, Pseq([
		//	//Pseq([0,\r,0,0].scramble,2),
		//	//Pseq([0,\r,\r,\r].scramble),
		//	Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
		//],inf),
		\freq, (Pkey(\freq).cpsmidi + midinote-60).midicps,
		\filter1_arg1, Pseq([30,600,12,28].scramble,inf),
		\ctranspose, midinote-60,
		//\dur, 1/8,
	);

	//pat2 = Pn(Mdef.node("ci osc3filter2_l1003").sourcepat);
	pat2 = Mdef.node("ci osc3filter2_l1003").sourcepat;
	pat2 = pat2.collect { arg ev; 
		//ev.removeAt(\freq); 
		//ev.removeAt(\sustain); 
		ev;
	};
	pat = pat <> pat2;
	pat = pat.trace.play(quant:Mdef.main.play_manager.get_quant);
	~mididict[midinote] = pat;

}); // match any noteOn
MIDIdef.noteOff(\noteOff, {arg vel, midinote;
	var note = ~mididict[midinote];
	("debug"++note++midinote).postln;
	[vel, midinote].postln;
	//TempoClock.play({ 
	//	note.stop;
	//}, quant:0.5)

}); // match any noteOn
)
Mdef.main.play_manager.get_quant

///////////////////////////////////////////////
///////////////////////////////////////////////
///////////////////////////////////////////////
///////////////////////////////////////////////

(
~piano_pattern_launcher = { arg nodename, options, inpat;

	  
	~mididict = IdentityDictionary.new;
	options = options ?? ();
	MIDIdef.noteOn(\noteOn, {arg vel, midinote;
		var pat, pat2;
		var quant;
		quant = options[\quant];
		quant = quant ?? Mdef.main.play_manager.get_quant;

		[vel, midinote].postln;

		//if(~mididict[midinote] = nil) {
		//	~mididict[midinote] = List[midinote];
		//} {
		//	~mididict[midinote].add(midinote);
		//};
		~mididict[midinote].stop;
		//~mididict[midinote] = midinote;
		midinote.postln("mn");
		options[\arp] = options[\arp] ?? \scoreline;
		pat = switch(options[\arp],
			\rand, {

				Pbind(
					//\instrument, 's_ci osc3filter2_l1002',
					//\type, \note,
					\note, Pseq([
						//Pseq([0,\r,0,0].scramble,2),
						//Pseq([0,\r,\r,\r].scramble),
						Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
					],inf),
					//\freq, (Pkey(\freq).cpsmidi + midinote-60).midicps,
					//\filter1_arg1, Pseq([30,600,12,28].scramble,inf),
					\ctranspose, midinote-60,
					\dur, 1/8,
				)
			},
			\scoreline, {

				Pbind(
					//\instrument, 's_ci osc3filter2_l1002',
					//\type, \note,
					//\note, Pseq([
					//	//Pseq([0,\r,0,0].scramble,2),
					//	//Pseq([0,\r,\r,\r].scramble),
					//	Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
					//],inf),
					\freq, (Pkey(\freq).cpsmidi + midinote-60).midicps,
					//\filter1_arg1, Pseq([30,600,12,28].scramble,inf),
					\ctranspose, midinote-60,
					//\dur, 1/8,
				);
			}
		);
		if(inpat.notNil) {
			pat = inpat <> pat;
		};

		//pat2 = Pn(Mdef.node("ci osc3filter2_l1003").sourcepat);
		pat2 = Mdef.node(nodename).sourcepat;
		if(options[\mode] == \gate) {
			pat2 = Pn(pat2);
		};
		pat2 = pat2.collect { arg ev; 
			if(options[\arp] == \rand) {
				ev.removeAt(\freq); 
				ev.removeAt(\sustain); 
			};
			ev;
		};
		pat = pat <> pat2;
		pat = pat.trace.play(quant:quant);
		~mididict[midinote] = pat;

	}); // match any noteOn
	MIDIdef.noteOff(\noteOff, {arg vel, midinote;
		var note;
		("NoteOff: "++midinote).postln;
		options[\mode].postln;
		switch(options[\mode],
			\gate, {
				note = ~mididict[midinote];
				[vel, midinote].postln;
				TempoClock.play({ 
					note.stop;
				}, quant:0.5)
			},
			\trig, {
			}
		)

	}); // match any noteOn

};
~piano_pattern_launcher.("osc1_l1003", (mode:\gate, quant:1/8, arp:\rand));
)
Mdef.main.play_manager.get_quant

~piano_pattern_launcher.("ci osc3filter2_l1003", (mode:\trig, quant:1/8, arp:\scoreline));
