
(
s.waitForBoot{
~seq = Mdef.force_init;
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
}
)
~seq.play_manager.reset_state
(
Mdef(\plopi, Pbind(
	\instrument, \monosampler,
	\slotnum, Pseq([21,25,24,20],inf),
	//\degree, Pseq([0,2,4,8],inf),
	\sustain, 0.01,
	\stepline, Pn(Plazy { Pseq([1,0.6,0.2,1].collect { arg i; if(i.coin) { 1 } { 0 } }) },10),
	//\stepline, Pwalk([1,1,1,1,0,1,0,1,1,0,0], Pseq([1,1,1,2,1,2,1],inf), Pseq([1,1,-1],inf)),
	\speed, Pwalk(Array.series(20,0.1,0.1), Pseq([-4,1,1,8],inf), Pseq([1,-1],inf)),
	//\stepline, Pseq([1,1,0,1],inf),
	//\dur, Pseq([0.5,[1,1,1,1]].convertRhythm,inf),
	//\dur, Pseq([1,-1],inf),
	\dur, 1/8,
	\amp, 0.1
));
)

(
SynthDef(\beam, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, sustain=1, mix=0.5, rq=0.5, mix_rlpf=0.5, fmidx= 0.8;
	var ou, ou2;
	var time = sustain;
	var env, env2, env3;
	var ouwrap, oufold, ouclip;
	//ou = VarSaw.ar(freq*2, width: XLine.ar(0.5,1,time)).range(0,XLine.ar(1,1/1000,time))*40;
	env = EnvGen.ar(Env([420,700,1000],[0.4,1.3]),gate);
	env2 = EnvGen.ar(Env([1,1/1000],[0.21],0,1),gate);
	env3 = EnvGen.ar(Env([1,1/1000,1],[0.101,0.101],0),gate);
	fmidx = env3;
	env = SinOsc.ar(freq*[1,7.450,4,1/2,1/4])*freq*fmidx+freq;
	ou = VarSaw.ar(env, width: XLine.ar(0.5,1,time)).sum.range(0,env2);
	ouwrap = SinOsc.ar(freq).wrap(-1*ou,ou);
	oufold = SinOsc.ar(freq).fold(-1*ou,ou);
	ouclip = SinOsc.ar(freq).clip(-1*ou,ou);
	ou = ouwrap - ouclip;
	ou = ouclip;
	ou = ouwrap;
	ou = oufold;
	ou = (oufold - ouclip / ouwrap).softclip;
	ou = ouclip - (oufold*2)+(1-ouwrap);
	ou = SelectX.ar(mix*3, [ou, ouwrap, oufold, ouclip]);
	ou2 = RLPF.ar(ou, freq*[1,2,4,1/2,1/4], rq).sum;
	ou = SelectX.ar(mix_rlpf, [ou, ou2]);
	//ou = SinOsc.ar(ou*(0.9*freq)+freq);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)
GUI.swing
(
Pdef(\beami, Pbind(
	\instrument, \beam,
	\degree, Pseq([0,2,4,3,5,7],inf),
	//\freq, (40-12).midicps,
	//\freq, 200,
	\dur, 0.25*4,
	\fmidx, 0.1,
	\mix_rlpf, 1.5,
	\rq, 4.1,
	\mix, 0.7,
	\amp, 1.1
)).play;
)

(
{

var time = 8;

var freq = (40-12).midicps;

var a = VarSaw.ar(freq/2, width: XLine.ar(0.5,1,time)).range(0,XLine.ar(1,1/1000,time));

var tone = SinOsc.ar(freq).fold(-1*a,a);

Out.ar(0, tone.dup);

}.play;

)

(
Mdef(\perc, Pbind(
	\instrument, \monosampler,
	\reapeat, 0,
	\sustain, 0.005,
	//\legato, 0.01,
	\slotnum, Pseq([0,7,2,7],inf),
	\dur, Pseq([1,[1,1,1,1]].convertRhythm,inf),
	\amp, 0.7
));
)
