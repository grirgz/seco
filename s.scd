~name = \s;


(
SynthDef(~name, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var sig;
	var mfreq = SinOsc.ar(freq*42.1).range(0.1, freq);
	sig = LFSaw.ar(mfreq);
	sig = RLPF.ar(sig, freq*7.4);
	sig = sig * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	sig = Pan2.ar(sig, pan, amp);
	Out.ar(out, sig);
}).add;
)

(
Pdef(~name, Pbind(
	\instrument, ~name,
	\degree, Pseq([0,1,2,3],inf) * Prand([1,1,1,\r],inf),
	\legato, 0.5,
	\dur, Pseq([0.1,1,0.1,0.2].scramble,inf),
	\amp, 0.2,
	//\dur, 0.1,
));
)

Pdef(~name).play
