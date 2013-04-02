s.boot;


(
Document.dir = "/home/ggz/code/sc/";
)

(

SynthDef(\pgrain, { arg out = 0, freq=800, sustain=0.001, amp=0.5, pan = 0;
	var window;
	window = Env.sine(sustain, amp * AmpCompA.kr(freq));
	Out.ar(out, 
	Pan2.ar(
	SinOsc.ar(freq),
	pan
	) * EnvGen.ar(window, doneAction:2)
	)
}).store;

SynthDef(\noiseGrain, 
{ arg out = 0, freq=800, sustain=0.001, amp=0.5, pan = 0;
var window;
window = Env.perc(0.002, sustain, amp * AmpCompA.kr(freq));
Out.ar(out, 
Pan2.ar(
Ringz.ar(PinkNoise.ar(0.1), freq, 2.6),
pan
) * EnvGen.ar(window, doneAction:2)
)
}
).store;

SynthDef(\bubblebub, {	|out=0, t_trig=1, attack=0.01, decay=0.08, pitchcurvelen=0.1, freq=1000, doneAction=2, amp=0.1|
	var pitch, son;
	amp   = amp * EnvGen.ar(Env.perc(attack, decay).delay(0.003), t_trig, doneAction: doneAction);
	pitch = freq * EnvGen.ar(Env.new([0,0,1],[0,1]).exprange(1, 2.718), t_trig, timeScale: pitchcurvelen);
	son = SinOsc.ar(pitch);
	// high-pass to remove any lowpitched artifacts, scale amplitude
	son = HPF.ar(son, 500) * amp * 10;
	Out.ar(out, son);
}).store;

SynthDef("tish", { arg freq = 1200, rate = 2;

var osc, trg;

trg = Decay2.ar(Impulse.ar(rate,0,0.3), 0.01, 0.3);

osc = {WhiteNoise.ar(trg)}.dup;

Out.ar(0, osc); // send output to audio bus zero.

}).store;


SynthDef(\bass, { |gate = 1, t_trig = 1, freq=100, freqlag = 0.1, ffreq = 1500, rq = 0.1, filtAttack = 0,
		amp = 1, out = 0|
	var	sig = Saw.ar(Lag.kr(freq, freqlag) * [1, 1.005]).sum,
		fenv = EnvGen.kr(Env([0, filtAttack, 0], [0.01, 0.2], -3), t_trig) + 1;
	sig = RLPF.ar(sig, ffreq * fenv, rq, amp)
		* EnvGen.kr(Env.adsr(0.01, 0.2, 0.5, 0.08), (gate > 0) - (t_trig > 0), doneAction: 2);
	Out.ar(out, sig ! 2);
}, metadata: (specs: (
	ffreq: \freq,
	t_trig: ControlSpec(0, 1, \lin, 1, 1, "trig"),
	freqlag: \delay,
	filtAttack: \delay
)) ).store;

)

~get_spec.(\sustain)

(

"seco22.sc".loadDocument;


~patlib = [

\bubblebub -> { arg amp=0.1, pitchcurvelen=0.5, type, stepline = #[1,1,0,1], freq = 500;
	Pbind(
		\instrument, \bubblebub,
		\pitchcurvelen, pitchcurvelen,
		\freq, freq,
		\stepline, stepline,
		\amp, amp,
		\dur, 0.25,
		\type, type
	)
},
\noiseGrain -> { arg amp=0.1, type, stepline = #[1,1,0,1], freq = 300;
	Pbind(
		\instrument, \noiseGrain,
		\freq, freq,
		\stepline, stepline,
		\amp, amp,
		\dur, 0.5,
		\type, type
	)
},
\pgrain -> { arg amp=0.1, type, stepline = #[1,1,0,1], freq = 300;
	Pbind(
		\instrument, \pgrain,
		\freq, freq,
		\stepline, stepline,
		\amp, amp,
		\dur, 0.5,
		\type, type
	)
},
\bass -> { arg amp=0.1, type, ffreq=1500, rq=0.1, stepline = #[1,1,0,1], freq = 300;
	Pbind(
		\instrument, \bass,
		\freq, freq,
		\ffreq, ffreq,
		\rq, rq,
		\stepline, stepline,
		\amp, amp,
		\dur, 0.5,
		\type, type
	)
},

];

// TODO: j'ai changé les fonctions de gui pour decaler mais j'ai oublié que fallais decaler uniquement pour \parlive...

~synthlib = [
\bass -> \bass,
\pgrain -> \pgrain
];

~seq = ~mk_sequencer.value;
~seq.load_patlib( ~synthlib );
~seq.make_gui;
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
