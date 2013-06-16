
~gui = ~hlayout.([
	~vlayout.(~guis.(\rest)),
	\tension, 
	~vlayout.([ \tension_rand, \tension_add ]),
	\double,
	\out_note_view,
]);

~params = (
	tension_rand: ControlSpec(0.1,0.5,\lin,0,0.1),
	tension_add: ~specs.(\tension),
	double: ~specs.(\integer, \notes),
	out_note_view: XXX,
	seq1: ~specs.(\multislider, \freq.asSpec)
	lfo1_freq_3: \freq.asSpec,
);

~views = (
	out_note_view: ~note_view.(\main)
)

~mods = (
	lfo1: ~make_mod.(\lfo)
)

~double_notes = { arg notes;
	notes.collect { arg no
		no.midinote = no.midinote * ~param.(\double)
	}
};

~nodes = (
	main: (
			pat: { arg self, pat; 
					Pbind(
							\freq, Pkey(\freq),
							//\tension, Pkey(\tension) + 0.05,
							\tension, Pfunc { arg ev; ev[\tension].rand + 0.05 },
							//\tension, 0.15,
					) <> pat;
			},
			notes: { arg self, notes;
				Pseq([
					~double_notes.(notes)
				])
			},
			params: (
				tension: Pfunc { ~param.(\tension_rand).rand + ~param.(\tension_add) },
				loss: Pseq([~mod.(\lfo1), ~mod.(\lfo2)],inf),
				ffreq: ~synth.({ arg i;
					EnvGen.kr(Env.asr(0.1,1,i.release),i.gate))
				}, \release),
				release: Pseq([1,2],inf),
				freq: ~setbus.({ arg in; (in.cpsmidi + ~param.(\pitchbend)).midicps })
			),
	),       

	lfo1: (
		params: (
			lfofreq: Pseq([1,2,~param.(\lfo1_freq_3)],inf),
		)
		
	),
	lfo2: (
		pat: { arg self, pat;
			Pbind(\lfofreq, Pkey(\lfofreq)) <> ~mod.(\lfo1)
		},
		params: (
			lfofreq: Pfunc
		)
	)
)




refresh modes:
	- static: rebuild synthdef
	- pattern: rebuild sourcepat
	- notes: rebuild notescore
	- bus: update bus value



(
	~seq1 = [1,2,3,4,5,6];
	Pbind(
		\instrument, \default,
		\degree, Pn(Plazy({Pseq(~seq1)})),
		\dur, 1,
		\amp, 0.1
	).play;
	

)
	~seq1 = [1,2];

