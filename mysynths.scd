(


SynthDef(\miaou1, { arg out=0, amp=0.1, gate=1, pan=0, panfreq=1/3, noiserate = 12, noisedetune=0.1, freq = 180;
	var env, ou, cpan;

	cpan = LFNoise1.kr(panfreq)+pan;
	env = EnvGen.kr(~make_adsr.(\adsr),gate, doneAction:2);

	ou = Blip.ar(LFNoise0.ar([1.01,0.99,1]*noiserate, noisedetune*freq, freq), (env)*12 + 1);
	ou = Pan2.ar(ou, cpan).sum * env * amp;
	Out.ar(out, ou);
}).store

)
