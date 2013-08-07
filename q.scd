~name = \q;

(
SynthDef(~name, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var sig;
	sig = SinOsc.ar(freq);
	sig = sig * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	sig = Pan2.ar(sig, pan, amp);
	Out.ar(out, sig);
}).add;
)

(
Pdef((~name+"kj").asSymbol, Pbind(
	\instrument, ~name,
	\degree, Pseq([0,1,2,3],inf),
	//\dur, Pseq([0.1,1,0.1,0.2].scramble,inf),
	\dur, 0.1,
));
)

Pdef((~name+"kj").asSymbol).play
