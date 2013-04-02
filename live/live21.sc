

s.meter
(
s.waitForBoot{
~seq = Mdef.force_init(true);
~synthlib = [
	\audiotrack_expander,
	\lead2,
	\pulsepass,
	\flute1,
	\miaou1,
	\ringbpf1,
	\piano2,
	\pmosc,
	\monosampler,
	\stereosampler,
	\ss_comb,
	\ss_combfreq,
].collect({ arg i; i -> i });

~effectlib = [
	\echo
].collect({arg i; i -> i });

~samplelib = [
	"sounds/perc1.wav",
	"sounds/pok1.wav",
	"sounds/amen-break.wav",
	"sounds/default.wav"
];
~seq.load_patlib( ~synthlib );
~seq.load_effectlib( ~effectlib );
~seq.set_presetlib_path("mypresets2");
~seq.append_samplelib_from_path("sounds/" );
~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );

Mdef.side_gui;



~tf = Pfunc({ arg ev; if(ev[\stepline] == 1) { \note } { \rest } });
~ff = Pfunc({ arg ev; if(ev[\stepline1] == 1) { 1 } { \rest } });

//Debug.enableDebug = false;

}
)
Mdef.main.save_project("live21");
Mdef.main.load_project("live21");

Debug.enableDebug = true;
Debug.enableDebug = false;

(
Mdef.sampledict((
		sacrifice: "sacrifice.wav",
		beast: "beast.wav",
		violon: "violon.wav",
		canabis: "canabis.wav",
	),
	"/home/ggz/Musique"
);
);

(
SynthDef(\noisebpf, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, rq=0.1, bsustain=1, detuneratio = 1;
	var ou;
	ou = WhiteNoise.ar(1);
	freq = freq * detuneratio;
	rq = EnvGen.kr(NamedControl.kr(\rqenv, Env([1,0.1,2,1],[0.1,0.8,0.1].normalizeSum)), gate, timeScale:bsustain);
	ou = BPF.ar(ou, freq, rq) * sqrt(1/rq);
	ou = BPF.ar(ou, freq, rq) * sqrt(1/rq);
	ou = ou.distort;
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).store;


SynthDef(\saw, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, detune=0.01, oscselect=0.5, detuneratio=1;
	var ou, ou2;
	freq = freq * detuneratio;
	ou = LFSaw.ar(freq*[1+detune,1,1-detune]).sum;
	ou2 = LFTri.ar(freq*2*[1+detune,1,1-detune]).sum;
	ou = SelectX.ar(oscselect, [ou, ou2]);
	ou = ou * EnvGen.ar(Env.adsr(0.001,0.1,0.8,0.01),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).store;
)


(
Mdef(\noise4,
	~penvcontrol.(
		Pbind(
			\instrument, \noisebpf,
			\degree, Pseq([0,2],inf),
			//\detuneratio, Ref(Env([0.9,1,1.1,2],[0.1,0.1,0.8])),
			//\freq, Pkey(\freq) * Pkey(\mfreq),
			//\freq, 500,
			//\rqenv, Ref(Env.[1,
			\rqenv, [ Env([1,0.1,0.1,1],[0.4,0.6,0.4].normalizeSum,2) ],
			\bsustain, ~bsustain.(),
			\legato, 0.91,
			\amp, 0.2,
			\dur, 4,
		)
	)
)
);


(
Mdef(\saw2, Pbind(
	\instrument, \saw,
	\degree, Pseq([0,0,\r,0,0,0,\r,\r],inf)+Pstep(Pseq([0,2],inf),4),
	\detune, 0.0001,
	\octave, 3,
	\oscselect, 1,
	\oscselect, 0.9,
	\mylegato, 1,
	\legato, 0.8,
	\dur, 1/8,
	\amp, 0.0201
));
);

(
Mdef(\saw2_p2, Pbind(
	\instrument, \saw,
	\degree, Pseq([0,0,\r,0,0,0,\r,\r],inf)+Pstep(Pseq([0,2,3,4,Pseq([0,4],4)],inf),Pseq([4,4,2,2,Pn(1,8)],inf)),
	\detune, 0.0001,
	\octave, 3,
	\oscselect, 1,
	\oscselect, 0.9,
	\mylegato, 1,
	\legato, 0.8,
	\dur, 1/8,
	\amp, 0.0201
));
);

(
Mdef(\saw3, ~penvcontrol.(Pbind(
	\instrument, \saw,
	\degree, Pseq([0,0,2,2],inf),
	\detune, 0.0001,
	\octave, 4,
	\oscselect, 1,
	\oscselect, 0.4,
	\detuneratio, Ref(Env([1,1.5,0.5],[0.2,0.2],[9,0])),
	\sustain, 0.8,
	\dur, 2,
	\amp, 0.0201
)));
);

(
~saw3dur = 1;
Mdef(\saw3_p2, ~penvcontrol.(Pbind(
	\instrument, \saw,
	\degree, Pseq([0,0,2,2],inf),
	\detune, 0.0001,
	\octave, 4,
	\oscselect, 1,
	\oscselect, 0.4,
	\detuneratio, Ref(Env([1,1.5,0.5,1/3],[0.2,0.2,1].normalizeSum * ~saw3dur,[9,0])),
	//\sustain, 0.98,
	\dur, ~saw3dur,
	\amp, 0.0201
)));
);

(
Mdef(\lead4, Pn(~penvcontrol.(Pbind(
	\instrument, \lead2,
	//\degree, Pseq([0,0,2,2],inf),
	\octave, 4,
	//\detuneratio, Ref(Env([1,1.5,0.5],[0.4,0.1],[9,0])),
	\fbase, Ref(Env([1,0.5,2]*5000, [0.1,0.5,0.1,0.8])),
	//\fbase, 5000,
	\rq, 0.1,
	\repeat, 0,
	\legato, 0.8,
	\stretch, 1/2,
	\amp, 0.301
), ~abcpbind.("cg/g_/g g/gb_/b cgc/g/b bab[gc]"))) );
);




(
Pdef(\kick1, Pbind(
	\instrument, \kickDrum,
//	\stepline1, Pseq([
//		1,0,0,0, 1,0,0,0,
//		1,0,0,0, 1,0,1,0,
//	],inf),
	\freq, ~ff * Prand([1300,1200,1400,1440],inf),
	//\freq, Pkey(\freqi) * 440,
	\rq, 5,
	\dur, 1/8,
	\sustain, 0.01,
	\amp, 0.6
));
);

(
Pdef(\snare2, Pbind(
	\instrument, \kraftySnr,
//	\stepline1, Pseq([
//		0,0,0,0, 1,0,0,0,
//		0,0,0,0, 1,1,0,0,
//	],inf),
	\freq, ~ff * Prand([1300,1200,1400,1440]/2,inf),
	//\freq, Pkey(\freqi) * 440,
	\rq, 5,
	\dur, 1/8,
	\sustain, 0.001,
	\decay, 0.1,
	\amp, 0.6, //* Pseq([1,0.5,1,0.5],inf)
));
);

(
Pdef(\hihat1, Pbind(
	\instrument, \hihat,
//	\stepline1, Pseq([
//		0,1,0,1, 0,1,1,0,
//		0,1,0,0, 0,0,0,1,
//	],inf),
	\freq, ~ff * Prand([1300,1200,1400,1440]*2,inf),
	//\freq, Pkey(\freqi) * 440,
	\repeat, 0,
	\rq, 5,
	\dur, 1/8,
	\sustain, 0.005,
	\decay, 0.01,
	\amp, 0.4 * Pseq([0.8,0.5,1,0.5],inf)
));
);


(
~drum =  { arg basename, score;
	Mdef((basename++"_kick").asSymbol, Pdef(\kick1) <> Pbind(\repeat, 0, \stepline1, Pn(Plazy{ score[\kick] })) ,\kickDrum);
	Mdef((basename++"_snare").asSymbol, Pdef(\snare2) <> Pbind(\repeat, 0, \stepline1, Pn(Plazy{ score[\snare] })),\kraftySnr);
	Mdef((basename++"_hihat").asSymbol, Pdef(\hihat1) <> Pbind(\repeat, 0, \stepline1, Pn(Plazy{ score[\hihat] })),\hihat);
};
);

(
~drum.(\drum1, (
	kick: Pseq([
		1,0,0,0, 1,0,0,0,
		1,0,0,0, 1,0,1,0,
	]),
	snare: Pseq([
		0,0,0,0, 1,0,0,0,
		0,0,0,0, 1,1,0,0,
	]),
	hihat: Pseq([
		0,1,0,1, 0,1,1,0,
		0,1,0,0, 0,0,0,1,
	]),
))
);

(
~drum.(\drum2, (
	kick: Pseq([
		1,0,0,0, 1,0,0,0,
		1,0,0,0, 1,0,0,0,
	]),
	snare: Pseq([
		0,0,1,0, 0,0,0,1,
		0,0,1,0, 0,1,0,1,
		0,0,1,0, 0,0,0,1,
		0,0,1,0, 0,1,1,1,
	]),
	hihat: Pseq([
		1,0,0,1, 0,1,0,0,
	],inf),
))
);


(
Mdef(\cana2, Pbind(
	\instrument, \stereosampler_sec,
	\buf, Mdef.dsample(\canabis),
	\start, 9,
	\end, 13.5,
	\speed, 1,
	\degree, Pseq([0],inf),
	\dur, 8,
	\amp, 1.1
));
);
BufferPool.paths
BufferPool.release_client(\samplekit)
BufferPool.buffers
BufferPool.annotations.leafDo({arg x, y, z; [x,y,z].debug("k")}); 1
(
BufferPool.annotations.leafDo({ arg x, y;
	[x,y].debug("x, y");
	if( x[1] == \samplekit ) { 
		x[0].debug("to free");
		BufferPool.release(x[0], x[1]);
	}
})
)
1.free
Mdef.dsample(\canabis)
Mdef.dmsample(\canabis)
(
Mdef(\cana3, Pbind(
	\instrument, \stereosampler,
	\bufnum, Mdef.dsample(\canabis),
	\speed, 1,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 1.1
));
);

(
Pdef(\canax, Pbind(
	\instrument, \stereosampler,
	\bufnum, 0,
	\speed, 1,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 1.1
)).play;
);

(
Mdef(\cana4, Pbind(
	\instrument, \stereosampler_sec,
	\buf, Mdef.dsample(\canabis),
	\start, 9,
	\end, 13.5,
	\speed, 1,
	\degree, Pseq([0],inf),
	\dur, 8,
	\amp, 1.1
));
);

(
Mdef(\cana5, Pbind(
	\instrument, \stereosampler_sec,
	\buf, Mdef.dsample(\canabis),
	\start, 9,
	\end, 13.5,
	\speed, 1,
	\degree, Pseq([0],inf),
	\dur, 8,
	\amp, 1.1
));
);

(
Mdef(\group_intro, Ppar([
	Pdef(\noise4),
	Pdef(\saw2),
	Pdef(\saw3),
	//Pdef(\cana2),
	//Pdef(\lead4),
]));
);

(
Mdef(\group_background, Ppar([
	Pdef(\noise4),
	Pdef(\saw2),
	Pdef(\saw3),
]));
);

(
Mdef(\group_perctrankil, Ppar([
	Pdef(\drum1_kick),
	Pdef(\drum1_snare),
	Pdef(\drum1_hihat),
]));
);

(
Mdef(\group_percpulse, Ppar([
	Pdef(\drum2_kick),
	Pdef(\drum2_snare),
	Pdef(\drum2_hihat),
]));
);

(
Mdef(\mgroup1, Ppar([
	Pdef(\group_background),
	Pdef(\group_perctrankil),
	Pdef(\group_percpulse),
	Pdef(\cana2),
]));
);


(
Mdef(\group_intro_boom, Ppar([
	Pdef(\noise4),
	Pdef(\saw2),
	Pdef(\saw3),
	Pdef(\drum1_kick),
	Pdef(\drum1_snare),
	Pdef(\drum1_hihat),
	//Pdef(\cana2),
	//Pdef(\lead4),
]));
);

(
Mdef(\group_pulse, Ppar([
	Pdef(\noise4),
	Pdef(\saw2),
	Pdef(\saw3),
	Pdef(\drum2_kick),
	Pdef(\drum2_snare),
	Pdef(\drum2_hihat),
	//Pdef(\cana2),
	//Pdef(\lead4),
]));
);

(
Mdef(\group3, Ppar([
	Pdef(\noise4),
	Pdef(\saw2),
	Pdef(\saw3),
	Pdef(\drum2_kick),
	Pdef(\drum2_snare),
	Pdef(\drum2_hihat),
]));
);

(
Mdef(\group3, Ppar([
	Pdef(\noise4),
	Pdef(\saw2),
	Pdef(\saw3),
	Pdef(\cana2),
	//Pdef(\cana2),
	//Pdef(\lead4),
]));
);

(
Mdef(\group_s1, Pseq([
	Pdef(\group1),
	Pdef(\group2),
	Pdef(\group3),
]));
);

(
Mdef(\group_m1, Pseq([
	Pdef(\cana2),
]));
);

(
Mdef(\group_p1, Ppar([
	Pdef(\cana2),
]));
);

(
Mdef(\group_p2, Ppar([
	Pdef(\cana2),
	Pdef(\cana3),
]));
);

Mdef.showdef(\group_m1);
Mdef.show(\group_m1);
Mdef.show(\group2);
Mdef.show(\group_p1);
Mdef.show(\group_p2);


