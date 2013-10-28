
(
SynthDef(\plop, { arg out=0, amp=0.1, gate=1, pan=0, spread=0.8, freq=200, doneAction=2, ffreq=200, rq=0.3;
	var sig, sig1, sig2, sig3;
	sig = LFSaw.ar(freq);
	sig = RLPF.ar(sig, ffreq, rq);
	sig = sig * EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:doneAction);
	sig = Splay.ar(sig, spread, amp, pan);
	Out.ar(out, sig);
}).add;
);

// le pattern dure 16/4 beats avant de recommencer au debut
Pdef(\plop).quant = [16/4]; // attend le recommencement du pattern pour changer
Pdef(\plop).quant = [16/4,0,0,1/2]; // fast-forward le pattern pour le mettre en synchro 1/2 beat apres avoir executé le pdef (outset = 1/2)
Pdef(\plop).quant = [16,4]; // fast-forward le pattern pour le mettre en synchro 1/2 beat apres avoir executé le pdef (outset = 1/2)
Pdef(\plop2).quant = [16,0]; // fast-forward le pattern pour le mettre en synchro 1/2 beat apres avoir executé le pdef (outset = 1/2)

(
Pdef(\plop, Pbind(
	\instrument, \plop,
	\freq, Pseq([200],inf)*Pn(Pseries(1,0.1,32)),
	\ffreq, 700,
	\dur, 1/4,
	\amp, 0.1
)).play;
)

(
Pdef(\plop, Pbind(
	\instrument, \default,
	\freq, Pseq([200],inf)*Pn(Pseries(1,0.1,32)),
	\ffreq, 400,
	\dur, 1/4,
	\amp, 0.1
)).play;
)

(
Pdef(\plop2, Pbind(
	\instrument, \plop,
	\freq, Pseq([200],inf)*Pn(Pseries(1,0.1,32)),
	\ffreq,800,
	\dur, 1/4,
	\amp, 0.1
)).play;
)
