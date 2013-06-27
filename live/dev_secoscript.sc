
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



//////////////////////////
// tracks


~tlist = [
	\modenv_l1011,
	\modenv_l1020,
	\modenv_l1021,
];

~global_vars = (
	tseqs: ~tlist.collect {~pseq_tracks.()}
)

~tseqs = ~tlist.collect { arg idx;
	~pseq_track.(ysize:~tlist[idx].get_scoreset.get_sheets.size, dict: { arg y; ~tlist[idx].get_scoreset.get_sheet(y) })
}

~gui = {
	VLayout(
		*~tseqs.collect(_.layout)
	)
}


/////////////////////////

(
	~win = Window.new;
	~win.
	~win.front;
)

Mdef.node("gater_l1077")


~bla = (rah: (gaa:nil));

if((~bla !? _.rah !? _.gaa).notNil) { "bla".postln; }

(
Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 0.1
) <> (vpattern:()).vpattern).trace.play;
);
