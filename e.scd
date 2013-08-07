~name = \e;


(
SynthDef(~name, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, time=0.05, spread=0.9, minfreq=50, ffreq=500,
	res=0.1
	;
	var sig;
	freq = EnvGen.ar(Env([1,0.5,0,0.5], [time,0.1,0.01]),gate,doneAction:0).range(minfreq, freq) ;
	sig = LFSaw.ar(freq * \freqtab.kr(1!10));
	sig = RLPF.ar(sig, ffreq * \ffreqtab.kr(1!10), 0.5);
	sig = (sig*30).tanh/2;
	sig = sig * EnvGen.ar(Env.adsr(0.001,0.1,0.1,0.1),gate,doneAction:2);
	sig = CombL.ar(sig, 0.1,res,1.1);
	sig = Splay.ar(sig, spread, amp, pan);
	Out.ar(out, sig);
}).add;
)

(
Pdef(~name, Pbind(
	\instrument, ~name,
	\rh, Pseq([0,1,0,0],inf),
	\freq, Pseq([20,200,1000,1000],inf) * Pkey(\rh),
	\freqtab, Pfunc {  [[0.1,0.5,1.1,0.03, 4.2, 0.5,0.54,0.65]] * rrand(0.1,4) },
	\dur, Pseq([0.1],inf),
	\amp, 0.3,
)).play;
);
