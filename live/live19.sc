

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
Mdef.main.save_project("live18");
Mdef.main.load_project("live18");

Debug.enableDebug = true;
Debug.enableDebug = false;

(
~smp = {
	var path =  "/home/ggz/Musique";
	var files = (
		sacrifice: "sacrifice.wav",
		beast: "beast.wav",
	);
	var dico = Dictionary.new;
	dico[\mono] = Dictionary.new;
	files.keysValuesDo { arg key,val;
		dico[key] = Buffer.read(s, path+/+val);
		dico[\mono][key] = Buffer.readChannel(s, path+/+val, channels:[0]);
	};
	dico;
}.value;
)

(
Pdef(\hihat, Pbind(
	\instrument, Pseq([
		"hihat",
	],inf),
	\freq, ~ff * Prand([1300,1200,1400,1440,1000,1100],inf),
	//\freq, Pkey(\freqi) * 440,
	\rq, 5,
	\dur, 1/8,
	\sustain, 0.01,
	\amp, 0.5 * Pseq([1,1,2,1]/2,inf)
)).play;
);


(
Pdef(\kick, Pbind(
	\instrument, Pseq([
		"kickDrum",
	],inf),
	\freq, ~ff * Prand([1300,1200,1400,1440],inf),
	//\freq, Pkey(\freqi) * 440,
	\rq, 5,
	\dur, 1/8,
	\sustain, 0.01,
	\amp, 0.6
)).play;
);


(
Pdef(\snare, Pbind(
	\instrument, Pseq([
		"kraftySnr",
	],inf),
	\freq, ~ff * Prand([1300,1200,1400,1440],inf),
	//\freq, Pkey(\freqi) * 440,
	\rq, 5,
	\dur, 1/8,
	\sustain, 0.01,
	\amp, 0.6 * Pseq([1,0.5,1,0.5],inf)
)).play;
);

EventPatternProxy.quant=8

(
Pdefn(\percscore1, [
	Pseq([1,0,0,0],inf),
	Pseq([0,0,1,0],inf),
	Pseq([1,1,1,1],inf),
]);
)
(
~percscore1 = [
	Pseq([1,0,0,0],inf),
	Pseq([0,0,1,0],inf),
	Pseq([1,1,1,1],inf),
];
)
(
~percscore1 = [
	Pseq([1,1,0,0],inf),
	Pseq([0,0,1,0],inf),
	Pseq([1,0,1,1],inf),
];
)
(
~percscore1 = Environment.make{
	~kick = Pseq([
		1,0,0,0, 0,0,0,0,
		0,0,1,0, 0,0,0,0,
	]);
	~snare = Pseq([
		0,0,0,0, 1,0,0,0,
	]);
	~hihat = Pseq([
		0,1,1,1,
	]);
};
)
(
~percscore1 = Environment.make{
	~kick = Pseq([
		1,0,0,0, 0,0,0,0,
		1,0,1,0, 0,0,0,0,
	]);
	~snare = Pseq([
		0,0,0,0, 1,0,0,0,
		0,1,0,0, 1,0,1,0,
	]);
	~hihat = Pseq([
		0,1,1,1, 0,1,0,1,
		0,0,0,1, 0,0,1,1,1,
	]);
};
)
(
~percscore1 = Environment.make{
	~kick = Pseq([
		1,0,0,0, 0,0,0,1,
		1,0,1,0, 0,0,1,1,
	]);
	~snare = Pseq([
		0,0,0,0, 1,0,0,0,
		0,1,1,0, 1,0,1,0,
	]);
	~hihat = Pseq([
		0,1,1,1, 0,1,1,0,
		0,1,1,1, 0,1,0,1,
		0,0,1,0, 0,0,0,1,
	]);
};
)
(
~percscore1 = Environment.make{
	~kick = Pseq([
		1,0,1,1, 0,0,0,0,
		1,1,1,0, 0,0,1,0,
	]);
	~snare = Pseq([
		0,0,0,0, 1,0,0,0,
		0,0,0,0, 1,1,1,0,
		0,0,0,1, 1,0,0,0,
	]);
	~hihat = Pseq([
		0,1,0,1, 0,0,1,0,
	]);
};
)
(
~percscore1 = Environment.make{
	~kick = Pseq([
		1,0,0,0, 0,0,0,0,
	]);
	~snare = Pseq([
		0,0,0,0, 1,0,0,0,
	]);
	~hihat = Pseq([
		0,0,1,0, 0,0,1,0,
	]);
};
)


(
~aa = [Pseq([0,4])];
Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, 0,
	\degree, PLseq(\aa,inf),
	\dur, 1,
	\amp, 0.7
)).play;
);
Pdefn
(
 Pdef(\myperc5,  Ppar( 
	[\kick, \snare, \hihat].collect { arg name, idx;
		 Pdef(name) <> Pbind(*[\stepline1,Pn(Plazy{ ~percscore1[name] })] );
	}).trace  
).play;
);
(
 Pdef(\myperc,  Ppar( 
	[\kick, \snare, \hihat].collect { arg name, idx;
		 Psetpre(\stepline1, Plazy{ ~percscore1[name] }, Pdef(name));
	}).trace  
).play;
);

(
 Mdef(\myperc6,  Pbind() <> Ppar( 
	[\kick, \snare, \hihat].collect { arg name, idx;
		 Pdef(name) <> Pbind(*[\stepline1,Pn(Plazy{ ~percscore1[name] })] );
	})  
);
);

(
	~kick = Pseq([
		1,0,0,0,
		0,0,0,0,
		0,0,1,0,
		0,0,0,0,
	],inf);
	~snare = Pseq([
		0,0,0,0,
		1,0,0,0,
	],inf);
	~hihat = Pseq([
		0,1,1,1,
	],inf);
)
Environment
~percscore1.push
~percscore1.pop
~percscore1
Environment.pop
().push
currentEnvironment[\hihat]
(
Pdef(\myperc, Ppar( 
	[\kick, \snare, \hihat].collect { arg name, idx;
		 Psetpre(\stepline1, PL(name,inf), Pdef(name));
	})
).play;
);

(
Pdef(\myperc3, Ppar( 
	[\kick, \snare, \hihat].collect { arg name, idx;
		 Psetpre(\stepline1, Pn(Plazy{ currentEnvironment[name]}), Pdef(name));
	})
).play;
);


(
Mdef(\lead, Pbind(
	\instrument, \lead2,
	\degree, Pseq([[0,2,4],[-1,2,4]],inf),
	\dur, 1,
	\stretch, 2,
	\amp, 0.1
)).play;
);
(
Mdef(\lead2, Pbind(
	\instrument, \lead2,
	\degree, Pseq([
		Pseq([0,2],4), Pseq([-1,4],4),		
	],inf),
	\repeat, 0,
	\fbase, 100,
	\stretch, 2,
	\sustain, 0.1,
	\rq, 7.1,
	\dur, 1/8,
	\amp, 0.1
)).play;
);
