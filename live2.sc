s.boot;


(
SynthDef("piou", {
	arg out=0, amp=1, sustain=0.4; //TODO: is an array ok ?
	var ou;
	ou = SinOsc.ar(
		XLine.kr(500, 50, sustain)*SinOsc.kr(9)+1,
		XLine.kr(000, 10, sustain)
	) * EnvGen.kr(Env.linen(0.1,0.5,0.1), timeScale:sustain, doneAction:2);
	Out.ar(out, ou * amp)
}).store;
 
SynthDef("fm1", {
	arg out=0, amp=1, gate=1, adsr=#[0.02, 0.2, 0.25, 1, 1, -4], freq=440, modf=9;
	var ou;
	ou = SinOsc.ar( SinOsc.kr(modf)+1*freq);
	ou = ou * EnvGen.kr(Env.adsr(*adsr), gate, doneAction:2);
	Out.ar(out, ou * amp)
}).store;
 
SynthDef("fm2", {
	arg out=0, amp=1, sustain=0.5, freq=500, modf=200, modbpf=900;
	var ou;
	ou = SinOsc.ar( SinOsc.kr(modf)+1*freq)
	* EnvGen.kr(Env.linen(0.1,0.5,0.1), timeScale:sustain, doneAction:2);
	ou = BPF.ar(ou, SinOsc.kr(SinOsc.kr(1)+1*900)+1*freq);
	Out.ar(out, ou * amp)
}).store;
 
SynthDef("shh", {
	arg out=0, amp=1, sustain=0.5, freq=440, modf=9, noise=0.5;
	var ou;
	ou = WhiteNoise.ar(noise);
	ou = LPF.ar(ou, SinOsc.kr(modf)+1*freq)
	* EnvGen.kr(Env.perc(0.001,sustain), doneAction:2);
	Out.ar(out, ou * amp)
}).store;
SynthDef("kickTrig1", { arg levK=1, t_trig=0, sustain=0.125, f1=36.7, f2=73.4, amp=1, out=0;
	var kEnv, ou;
	var kickEnv;
	kickEnv = Env.linen(0.001, 1.9, 0.099, 1);
	kEnv=EnvGen.ar(kickEnv,1, doneAction:2, timeScale: sustain, levelScale: levK);
	ou =Pan2.ar(Decay2.kr(t_trig, 0.005, 0.45, 
	FSinOsc.ar(f1, 0.4)+FSinOsc.ar(f2, 0.2)),0);
 
	Out.ar(out, ou * kEnv * amp);
}).store;
SynthDef("snTrig1", { arg levSn=1, t_trig=0, sustain=0.125, panPos=0, amp=1,
	out=0;
	var snEnv, ou;
	var snareEnv;
	snareEnv = Env.linen(0.001, 1.9, 0.099, 1);
	snEnv=EnvGen.ar(snareEnv,1, doneAction:2, timeScale: sustain, levelScale: levSn);
	ou =Pan2.ar(Decay2.kr(t_trig, 0.005, 0.25, FSinOsc.ar(38.midicps, 0.3)+ 		BrownNoise.ar(0.4)),panPos);
 
	Out.ar(out, ou*snEnv * amp);
}).store;
)


(


)

(

Document.dir = "/home/ggz/code/sc/";
"seco22.sc".loadDocument;



~synthlib = [
	\piou,
	\fm1,
	\fm2,
	\kickTrig1,
	\snTrig1
].collect({ arg i; i -> i });

~seq = ~mk_sequencer.value;
~seq.load_patlib( ~synthlib );
~seq.make_gui;
)
(
~seq.save_project("proj1");
~seq.load_project("proj1");


)

"mkdir myproj".unixCmd
(
var myPath;
myPath = PathName.new("./");
myPath.files.postln;
)


SynthDescLib("pgrain").synthDescs
SynthDef("pgrain")
SynthDescLib.global.browse;
SynthDescLib.global.synthDescs[\pgrain].dump;
SynthDescLib.synthDescs[\pgrain].dump;
SynthDescLib.global.synthDescs[\pgrain].controls[0].dump;
SynthDescLib.global.synthDescs[\pgriain].controls[0].defaultValue;
SynthDescLib.global.synthDescs[\pgrain].controlNames
SynthDescLib.global.synthDescs[\basiis].metadata.specs[\t_trig].asSpec

\freq.map
\plop.isSymbol

~p = EventPatternProxy.new
~p.source = \pgrain
~p.play


~d = Dictionary[\bla -> 1 , \rah -> 140]
~d.keys

~li = List.new;
~d.pairsDo({ arg key, val; ~li.add(key); ~li.add(val)})
~li
Pbind(* ~li )
[bla: 4]

(

// Alternative syntax, using a key/pattern array:


Pbind(*[

instrument: \test, 

nharms: Pseq([4, 10, 40], inf), 

dur: Pseq([1, 1, 2, 1]/10, inf), 

#[freq, sustain],  Ptuple([

Pseq( (1..16) * 50, 4), 

Pseq([1/10, 0.5, 1, 2], inf)

]), 

]).play

)

~f = { arg bla=4, raa=5; };
~f.argNames
~f.defaultArgs

~a = nil;
~a = 7;
~b = ~a ?? 4

(
var defname = nil, argName=\sustain;
SynthDescLib.global.synthDescs[defname].notNil and: { SynthDescLib.global.synthDescs[defname].metadata.specs[argName].notNil }
)

~patlib.writeArchive("plop.txt")

a = Array.fill(100, { 100.rand });

a.writeArchive(PathName.tmp ++ "myArray");

b = Object.readArchive(PathName.tmp ++ "myArray");

a == b // true
PathName.tmp




(
SynthDef(\sin, {
	arg out=0, amp=1, gate=1, freq=440;
	var ou;
	var env, envctl;

	env = Env.adsr(0.02, 0.2, 0.25, 0.1, 1, 4);
	//env = Env.newClear(6);

	envctl = Control.names([\adsr]).kr( env.asArray );
	ou = SinOsc.ar( freq);
	ou = ou * EnvGen.kr(envctl, gate, doneAction:2);
	Out.ar(out, ou * amp)
}).add;
)

~p = EventPatternProxy.new;
(

~p.source = Pbind(
	\instrument, \sin,
	\adsr, [ Env.adsr(0.07,0.1, 0.1, 0.9, 0.9, 1).asArray ],
	\legato, 0.5,
	\dur, 2
);
~p.play

)
Env.adsr(0.07,0.01, 0.02, 1.701, 1, 1).test(2).plot
Env.adsr(0.07,0.01, 0.02, 1.701, 1, 1).asArray
