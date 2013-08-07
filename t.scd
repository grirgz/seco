~name = \t;


(
SynthDef(~name, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, time=0.05, spread=0.9, minfreq=50, ffreq=500,
	res=0.1
	;
	var sig;
	freq = EnvGen.ar(Env([1,0.5,0,0.5], [time,0.1,0.01]),gate,doneAction:0).range(minfreq, freq) ;
	sig = LFSaw.ar(freq * \freqtab.kr(1!10) * LFNoise0.ar([0.1,0.2,0.3,3.4]*18).range(00.1,2));
	sig = sig * EnvGen.ar(Env.adsr(0.001,0.1,0.1,0.1),gate,doneAction:2);
	sig = sig + Fb({
		sig = RLPF.ar(sig, 4500);
	},0.001);
	sig = Splay.ar(sig, spread, amp, pan);
	Out.ar(out, sig);
}).add;
)

(
Pdef(~name, Pbind(
	\instrument, ~name,
	\rh, Pseq([1],inf),
	\res, Pseq([0.1,0.1,0.05],inf),
	\freq, Pseq([800,1000,3000,200],inf) * Pkey(\rh),
	\freqtab, Pfunc {  [[0.1,0.5,1.1,0.03, 4.2, 0.5,0.54,0.65]] * rrand(0.1,4) },
	\legato, 0.9,
	\dur, Pseq([8]*1,inf),
	\amp, 0.4
)).play;
);
Pdef(~name).stop

(
	Ndef(~name, { arg amp=0.1, gate=1, pan=0, freq=400, time=0.05, spread=0.1, minfreq=50, ffreq=500,
		res=0.1
		;
		var mod1, mod2;
		var sig;
		freq = EnvGen.ar(Env([1,0.5,0,0.5], [time,0.1,0.01]),gate,doneAction:0).range(minfreq, freq) ;
		sig = LFSaw.ar(freq * \freqtab.kr(1!10) * LFNoise0.ar([0.1,0.2,0.3,3.4]*18).range(00.1,2));
		sig = sig * EnvGen.ar(Env.adsr(0.001,0.1,0.1,0.1),gate,doneAction:2);
		sig = sig + Fb({ arg fb;
			sig = sig + (fb * 1.8);
			mod1 = SinOsc.ar(0.6).range(0.1,41);
			mod1 = LFNoise0.ar(0.4).range(0.01,55) + mod1;
			sig = RLPF.ar(sig, 300*mod1, 0.3);
			sig = sig.tanh;
			//sig = DynKlank.ar(`[[500,500.1,500.2,60,60.01]/3*SinOsc.ar([0.1,80,1,0.05,10]).range(0.9,1.1), nil, [1,1,1,1]], sig);
			//sig = DynKlank.ar(`[[50,200,300,100,80,1000,2000,7000]*SinOsc.ar([0.1,80,1,0.05,10]).range(0.9,1.1), nil, [1,1,1,1]], sig);
			//sig = DynKlank.ar(`[[50,200,300,100,80,1000,2000,7000]*SinOsc.ar([0.1,80,1,0.05,10]).range(0.9,1.1), nil, [1,1,1,1]], sig);
			//sig = DynKlank.ar(`[[500,500.1,500.2,60,60.01]/4, nil, [1,1,1,1]], sig);
			sig = DelayC.ar(sig, 0.1, 0.00001 * SinOsc.ar(SinOsc.ar(0.480).range(0.1,480)+LFNoise0.ar(0.5).range(2,300)).range(0.01,11));
			sig;
			//sig = DelayC.ar(sig, 0.1, 0.01 * SinOsc.ar([0.3,0.4,0.5,0.6,1,2]*mod1).range(0.1,8));
			//sig = DelayC.ar(sig, 0.1, 0.01 * SinOsc.ar([0.3,0.4,0.5,0.6,1,2]*mod1).range(0.1,8));
		},0.3);
		sig = sig * 0.4;
		sig = Limiter.ar(sig, 0.8);
		sig = Splay.ar(sig, spread, amp, pan);
	}).play;
)
