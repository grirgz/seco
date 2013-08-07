~name = \a;

(
Pdef(~name, Pbind(
	\degree, Pseq([0,1,2],inf),
));
)
Pdef(~name).play
(
SynthDef(\rii, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, time=0.05, spread=0.9, minfreq=50, ffreq=500,
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
	\instrument, \rii,
	//\rh, Pseq([1,0,1,0, 1,0,1,1],inf),
	\freq, Pn(Pwhite(50,90,8)) * Pseq([1,8],inf),
	\res, Pseq([0.1,0.2,0.01,1/Pkey(\freq) * 8, 0.1],inf),
	\ffreq, Pseq([500,5000],inf),
	\freqtab, Pfunc {  [[0.1,0.5,1.1,0.3, 4.2, 0.5,0.54,0.65]] * rrand(0.1,1) },
	\time, Pseq([0.1,0.05,1,0.2],inf)*8,
	\dur, Pseq([0.1,1,0.1,0.1,0.1,0.1,0.1,0.1,0.1],inf)*2,
	\dur, Pseq([0.2,0.4,0.8],inf),
	\amp, 0.1,
)).play;
);

Pdef(~name).stop
