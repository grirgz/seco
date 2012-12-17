s.meter
(
s.waitForBoot{
//"/home/ggz/code/sc/abcparser.sc".load;
~seq = Mdef.force_init(true);
"/home/ggz/code/sc/seco/tracks.sc".load;
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
Mdef.main.save_project("live20");
Mdef.main.load_project("live20");

Debug.enableDebug = true;
Debug.enableDebug = false;

(
~smp = {
	var path =  "/home/ggz/Musique";
	var files = (
		//sacrifice: "sacrifice.wav",
		//beast: "beast.wav",
		violon: "violon.wav",
	);
	var dico = Dictionary.new;
	dico[\mono] = Dictionary.new;
	files.keysValuesDo { arg key,val;
		dico[key] = Buffer.read(s, path+/+val);
		dico[\mono][key] = Buffer.readChannel(s, path+/+val, channels:[0]);
	};
	dico;
}.value;
);

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

(
~drum =  { arg basename, score;
	Mdef((basename++"_kick").asSymbol, Pdef(\kick) <> Pbind(\repeat, 0, \stepline1, Pn(Plazy{ score[\kick] })),\kickDrum);
	Mdef((basename++"_snare").asSymbol, Pdef(\snare) <> Pbind(\repeat, 0, \stepline1, Pn(Plazy{ score[\snare] })),\kraftySnr);
	Mdef((basename++"_hihat").asSymbol, Pdef(\hihat) <> Pbind(\repeat, 0, \stepline1, Pn(Plazy{ score[\hihat] })),\hihat);
};
);

(
~drum.(\drum1, (
	kick: Pseq([
		1,0,0,0, 0,0,0,0,
		1,0,0,0, 0,0,0,0
	]),
	snare: Pseq([
		0,0,0,0, 1,0,0,0,
	]),
	hihat: Pseq([
		0,0,1,0, 0,0,1,0,
		0,1,1,0, 0,0,1,0
	]),
))
);

(
~drum.(\drum1, (
	kick: Pseq([
		1,0,0,0, 0,0,0,0,
	]),
	snare: Pseq([
		0,0,0,0, 1,0,0,0,
	]),
	hihat: Pseq([
		0,0,0,0, 0,0,0,0
	]),
))
);

(
~drum.(\drum1, (
	kick: Pseq([
		1,1,1,0, 0,0,0,0,
		0,0,0,0, 0,0,0,0,
	]),
	snare: Pseq([
		0,0,0,0, 1,1,0,1,
		0,1,1,0, 1,1,0,1,
	]),
	hihat: Pseq([
		0,0,1,0, 0,1,1,0,
		0,1,1,0, 0,0,1,0
	]),
))
);

(
~drum.(\drum1, (
	kick: Pseq([
		1,0,1,0, 0,0,0,0,
		0,0,0,0, 0,0,0,0,
	]),
	snare: Pseq([
		0,0,0,0, 1,1,0,1,
	]),
	hihat: Pseq([
		0,0,1,0, 0,0,1,0
	]),
))
);

(
~drum.(\drum1, (
	kick: Pseq([
		1,0,0,0, 1,0,0,0,
		1,0,0,0, 1,0,0,0,
		1,0,0,0, 1,0,0,0,
		1,0,0,0, 1,0,1,0,
	]),
	snare: Pseq([
		0,0,0,0, 0,0,0,0,
		0,0,0,0, 0,0,0,0,
	]),
	hihat: Pseq([
		0,0,1,0, 0,0,1,0
	]),
))
);

(
Mdef(\bla, 
~abc_to_pbind.("[c4eg][c4ega][B4eg][B4egb]",8) <>
Pbind(
	\instrument, \lead2,
	\repeat, 0,
	\stretch, 1/4,
	\rq, 0.091,
	\legato, 0.8,
	\fbase, Pseg(Pseq([5000,100],inf),8),
	\fbase, 500,
	\amp, 0.031
), \lead2);
);

(
Mdef(\bla2, 
~abc_to_pbind.("c2eg c1ega B2eg B1egb",8) <>
Pbind(
	\instrument, \lead2,
	\stretch, 1/4,
	\rq, 0.1,
	\repeat, 0,
	\fbase, 5000,
	\fbase, Pseg(Pseq([100,500]*2,inf),8),
	\amp, 0.031
), \lead2);
);

(
Mdef(\bla2, 
Pbind(
	\stepline1, Pseq([1,1,0,1,1,0,0,1],inf),
	\degree, Pstutter(2, Pkey(\degree) * ~ff)
) <>
~abc_to_pbind.("c2eg c1ega B2eg B1egb",8) <>
Pbind(
	\instrument, \lead2,
	\repeat, 0,
	\stretch, 1/8,
	\rq, 0.1,
	\fbase, 5000,
	\fbase, Pseg(Pseq([100,500]*2,inf),8),
	\amp, 0.031
), \lead2);
);

(
Mdef(\bla2, 
Pbind(
	\stepline1, Pseq([1,1,0,1,1,0,0,1],inf),
	\degree, Pstutter(1, Pkey(\degree) * ~ff)
) <>
~abc_to_pbind.("c2eg c1ega B2eg B1egb",8) <>
Pbind(
	\instrument, \lead2,
	\repeat, 0,
	\stretch, 1/4,
	\rq, 0.1,
	\fbase, 5000,
	\fbase, Pseg(Pseq([100,500]*2,inf),8),
	\amp, 0.031
), \lead2);
);

(
Mdef(\bla2, 
Pbind(
	\stepline1, Pseq([1,1,1,1,1,1,1,1],inf),
	\degree, Pstutter(1, Pkey(\degree) * ~ff)
) <>
~abc_to_pbind.("ceg2 cacc B2Gg B1egb",8) <>
Pbind(
	\instrument, \lead2,
	\repeat, 0,
	\stretch, 1/4,
	\rq, 0.1,
	\fbase, 5000,
	\fbase, Pseg(Pseq([100,500]*2,inf),8),
	\amp, 0.031
), \lead2);
);




(
Mdef(\kicksmp, Pbind(
	\instrument, \monosampler,
	\stepline1, Pseq([
		1,0,1,0, 0,0,0,0,	
		1,0,0,0, 0,0,0,0,	
	]),
	\freq, ~ff,
	\dur, 1/8,
));
);

(
Mdef(\snaresmp, Pbind(
	\instrument, \monosampler,
	\stepline1, Pseq([
		0,0,0,0, 1,0,0,0,	
		0,1,0,0, 1,0,0,0,	
	]),
	\speed, Pseq([1,1.1],inf).stutter(8),
	\freq, ~ff,
	\dur, 1/8,
));
);


(
Mdef(\hhsmp, Pbind(
	\instrument, \monosampler,
	\stepline1, Pseq([
		0,0,1,0, 0,0,1,0,	
	]),
	\speed, Pseq([1,1.1,0.9],inf).stutter(1),
	\amp, 0.5,
	\amp, Pkey(\amp)*Pseq([0.5,0.8,0.7,0.5],inf),
	\freq, ~ff,
	\dur, 1/8,
));
);

(0+(2*7)).degreeToKey(Scale.new,4).midicps
(48).degreeToKey(Scale.major).midicps

(
l = [0, 1, 5, 9, 11]; // pentatonic scale
(1, 2..15).collect{|i|
    i.degreeToKey(l, 12)
};
)


(
Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0],inf),
	//\freq, (0+(5*7)).degreeToKey(Scale.major).midicps,
	\dur, 1,
	\amp, 0.1
)).trace.play;
);

12.midiratio
(
Mdef(\violon, Pbind(
	\instrument, \player5,
	\buf, ~smp[\violon],
	//\stepline1, Pseq([ 0,0,1,0, 0,0,1,0,	]),
	//\speed, Pseq([1,1.1,0.9],inf).stutter(1),
	\amp, 0.4,
	//\ffreq, (Pseq([0,-1],inf)+(2*7)).degreeToKey(Scale.major).midicps,
	\rate, (Pseq([0,-1],inf)+6).degreeToKey(Scale.major).midiratio,
	\legato, 1,
	\mylegato, 1,
	//\amp, Pkey(\amp)*Pseq([0.5,0.8,0.7,0.5],inf),
	//\freq, ~ff,
	\dur, 2,
));
);

(
Mdef(\violon2, 
//~abc_to_pbind.("[cEg][Fac][dfA][GBd]",8) <>
~abc_to_pbind.("[ceg][cega][Beg][Begb]",8) <>
Pbind(
	\instrument, \player3,
	\buf, ~smp[\violon],
	\stretch, 1,
	\adsr, [ Env.adsr(0.1,0.4,0.51,0.5) ],
	//\buf, 9,
	//\stepline1, Pseq([ 0,0,1,0, 0,0,1,0,	]),
	//\speed, Pseq([1,1.1,0.9],inf).stutter(1),
	\amp, 0.4,
	\legato, 0.5,
	\mylegato, 2,
	//\amp, Pkey(\amp)*Pseq([0.5,0.8,0.7,0.5],inf),
	//\freq, ~ff,
	\dur, 2,
),\player3);
);

~smp[\stereo][\violon]
~smp[\violon]

(
Mdef(\par2, Ppar([
	Mdef(\violon),
	Mdef(\violon2),
	Mdef(\drum1),
	Mdef(\bla2),
]))
);

(
Mdef(\drum1, Ppar([
	Mdef(\drum1_kick),
	Mdef(\drum1_snare),
	Mdef(\drum1_hihat),
]))
);

Mdef.main.node_manager.set_default_group(\par2);
Mdef.main.panels.side.set_current_group(Mdef.node(\par2));
Mdef.showdef(\par1)
Mdef.showdef(\par2)
