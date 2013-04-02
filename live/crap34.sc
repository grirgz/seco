
(
SynthDef(\lfo, { arg out=0, amp=0.1, gate=1, pan=0, freq=2, range=100, carrier=300;
	var ou;
	ou = SinOsc.kr(freq)*range+carrier;
	ou = ou * EnvGen.kr(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	Out.kr(out, ou);
}).add;
)

~bus = Bus.control(s,1)
Synth(\lfo, [\freq, 4, \out, ~bus])

(
Pdef(\plop, Pbind(
	\instrument, \default,
	\freq, ~bus.asMap,
	\dur, 1,
	\amp, 0.1
)).play;
);

{SinOsc.ar}.play

(
SynthDef(\lead3, {	arg out=0, freq = 100, pan=0, amp=0.1, mdetune=1.004, gate=1, rq=0.1, fratio = 1, fbase=400, wet=1, fbfreq=100, fbamp=0.8, fbpamp=1; 
	var fb, ou, filtenv;
	ou = LFSaw.ar(freq * [1, mdetune]).sum;
	filtenv = EnvGen.ar(\adsr_filter.kr(Env.adsr(0.01,0.25,0.07,0.3)), gate, 1, fbase, doneAction:0) * freq * Lag.kr(fratio,0.1);
	ou = RLPF.ar(ou, filtenv, rq);
	fb = ou;
	fb = fb * EnvGen.ar(\adsr.kr(Env.adsr(0.001,0.4,0.9,0.1)), gate, doneAction:2);
	fb = Pan2.ar(fb, pan, amp);
	Out.ar(out, fb);
}).add;
)
