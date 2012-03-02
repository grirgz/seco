

(
a = Pbind(
	\bla, \pulsepass,
	\instrument, Pkey(\bla),
	\dur, 0.5,
	\amp, 0.2
)
)
(
b = Pdef(\rah, Pbind(
	\bla, \pulsepass,
	\degree, Pseq([1,2,3,4,5,6,7,8],inf),
	\instrument, Pkey(\bla),
	\dur, 0.5,
	\amp, 0.2
))
)
b = Pdef(\rah, a);
a.patternpairs[5].key
a.play
b.play
b.quant = 2
b.source.patternpairs

c = Pbindef(\rah, \dur, 1.0); 
c.play
Pdef(\rah).play
Pchain

(
SynthDef(\lead, {	arg out=0, freq = 100, pan=0, amp=0.1, detune=1.1, gate=1, rq=0.1, fratio = 1, fbase=20; 
	var ou, filtenv;
	//gate = LFPulse.ar(1);
	ou = LFSaw.ar(freq * [1, detune]).sum;
	filtenv = EnvGen.ar(Env.adsr(0.01,0.35,0.07,0.3), gate, freq * fratio, fbase, doneAction:0);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.4,0.1,0.4), gate, doneAction:0);
	ou = RLPF.ar(ou, filtenv, rq);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add
)

(
~mpdef.(\rah, Pbind(
	//\instrument, \lead,
	\freq, 070,
	\legato, 0.7,
	\fratio, 0.1,
	\fbase, 100,
	\rq, Pseq([1,1.7],inf),
	\dur, 0.5,
	\detune, 1.007,
	\amp, 0.2
));
)

Pbind(\bla, 1).class == Pbind
Pdef(\bla).class == Pbind

(
~mpdef = { arg name, pat;

	if(pat.class == Pbind) {
		
	}

};
)
