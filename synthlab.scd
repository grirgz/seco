
(

SynthDef("player3", { |buf, freq=200, pan=0.0, amp=1.0, gate=1, rq=0.1, rq2=0.1|
	var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf)*Rand(-1,1).sign, startPos:BufFrames.kr(buf)*Rand(0,0.5), loop:1, doneAction:0);
	sig = sig.sum;
	sig = Mix.fill(5, { arg nn;
		nn = (nn+1)*100;
		FreqShift.ar(BPF.ar(sig, freq+nn, rq), 0 - nn)
	});
	sig = BPF.ar(sig, freq, rq2);
	sig = Normalizer.ar(sig, amp);
	sig = sig * EnvGen.ar(~make_adsr.(\adsr, Env.adsr(0.1,0.1,0.8,0.8)),gate,doneAction:2);
	Out.ar(0, Pan2.ar(sig, pan, amp));
}).add;

SynthDef("player5", { |buf, freq=200, pan=0.0, amp=1.0, startPos=0, gate=1, rq=0.1, rq2=0.1, attack=0.1, release=0.1, shift=0, rate=1, ffreq=1000|
	var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf)*rate, startPos:BufFrames.kr(buf)*startPos, loop:1, doneAction:0);
	sig = FreqShift.ar(sig, shift);
	sig = BPF.ar(sig, ffreq, rq);
	sig = (sig*15).distort;
	sig = sig * EnvGen.ar(Env.asr(attack,0.8,release),gate,doneAction:2);
	Out.ar(0, Pan2.ar(sig, pan, amp));
}).add;


SynthDef(\clipnoise, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, rq=0.1;
	var ou;
	var ou2;
	ou = ClipNoise.ar(1);
	freq = SinOsc.kr( EnvGen.ar(Env.asr(0.81,1,0.4),gate)*10)*0.4 * freq + freq;
	//ou2 = RHPF.ar(ou, freq*[1,2,1/2], rq,mul:0.01).sum;
	ou2 = RLPF.ar(ou, freq*[1,2,1/2], EnvGen.ar(Env.adsr(0.1,0.1,1,0.1),gate)*rq+0.001,mul:0.01).sum;
	ou = BPF.ar(ou2, freq*[1,2,4], rq*189).sum;
	ou = ou.clip;
	//ou = ou + ou2;
	//ou = ou2;
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	ou = Limiter.ar(ou);
	Out.ar(out, ou);
}).add;

SynthDef(\noisy, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, rq=0.1;
	var ou;
	var ou2;
	var wet;
	ou = ClipNoise.ar(1);
	ou2 = WhiteNoise.ar(1);
	wet = EnvGen.ar(Env.adsr(0.4,0.01,1,0.2),gate);
	//wet = 1;
	rq = EnvGen.ar(Env.adsr(0.1,0.4,0.001,0.8,0.1,2),gate,doneAction:2) * rq * 1 + 0.0001;
	ou = SelectX.ar(wet, [ou, ou2]);
	//freq = SinOsc.kr( EnvGen.ar(Env.asr(0.81,1,0.4),gate)*10)*0.4 * freq + freq;
	//ou2 = RHPF.ar(ou, freq*[1,2,1/2], rq,mul:0.01).sum;
	//ou2 = RLPF.ar(ou, freq*[1,2,1/2], EnvGen.ar(Env.adsr(0.1,0.1,1,0.1),gate)*rq+0.001,mul:0.01).sum;
	ou = BPF.ar(ou, freq*[1,2,4], rq).sum;
	ou = ou + DelayL.ar(ou,0.001);
	ou = ou + DelayL.ar(ou,0.001);
	ou = ou / 3;
	//ou = ou.clip;
	//ou = ou + ou2;
	//ou = ou2;
	ou = ou * EnvGen.ar(Env.adsr(0.1,0.01,0.8,0.4),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	//ou = Limiter.ar(ou);
	Out.ar(out, ou);
}).add;
)

(
Pdef(\clipnoise, Pbind(
	\instrument, \clipnoise,
	\rq, 0.001,
	\degree, Pseq([0,1,4,8],inf),
	\octave, 3,
	\legato, 0.6,
	\dur, 1/2,
	\amp, 48.1
)).play;
);
Pdef.defaultQuant = 1

(
Pdef(\clipnoise, Pbind(
	\instrument, \noisy,
	\rq, 0.001,
	\degree, Pseq([0,1,4,8],inf),
	\octave, 3,
	\legato, 0.6,
	\dur, 1/2,
	\amp, 478.1
)).play;
);


(
SynthDef(\noiseenvir, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	ou = SinOsc.ar(freq);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add; 
)


(
Ndef(\noiseenvir, { arg low=7000, high=15000;
	var ou;
	ou = WhiteNoise.ar(1);
	ou = HPF.ar(ou, low);
	ou = LPF.ar(ou, high);
	ou = ou ! 2;

}).play
)
(
Ndef(\noiseenvir, { arg low=7000, high=15000;
	var ou;
	ou = PinkNoise.ar(1);
	ou = HPF.ar(ou, low);
	ou = LPF.ar(ou, high);
	ou = ou ! 2;

}).play
)

Ndef(\noiseenvir).set(\low, 7000, \high, 15000); // aerosol
Ndef(\noiseenvir).set(\low, 3000, \high, 7000); // bacon
Ndef(\noiseenvir).set(\low, 1000, \high, 3000); // rain
Ndef(\noiseenvir).set(\low, 500, \high, 1000); // river
Ndef(\noiseenvir).set(\low, 250, \high, 500); // train cabin
Ndef(\noiseenvir).set(\low, 50, \high, 250); // thunder
Ndef(\noiseenvir).set(\low, 1, \high, 50); // explosion rumble


// effect idea: process higher frequency differently than lower ones

(
SynthDef("shh", {
	arg out=0, amp=1, sustain=0.5, freq=440, modf=9, noise=0.5, pan=0;
	var ou;
	ou = WhiteNoise.ar(noise);
	ou = LPF.ar(ou, SinOsc.kr(modf)+1*freq)
	* EnvGen.kr(Env.perc(0.001,sustain), doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou)
}).store;
)



(
Pdef(\plop, Pbind(
	\instrument, \shh,
	\degree, Pseq([0],inf),
	\modf, 45,
	\dur, 1,
	\amp, 0.4
)).play;
);


(
SynthDef("shintro", {
	arg out=0, amp=1, gate=1, freq=440, modf=9, noise=0.5, pan=0, start=0.01, end=1, tsustain=1, at=0.1, sl=1, rt=0.1, rq=0.1;
	var ou;
	ou = WhiteNoise.ar(noise);
	//ou = LPF.ar(ou, XLine.ar(start, end, tsustain)*freq);
	//rq = rq * XLine.ar(0.4, 4, tsustain);
	ou = BPF.ar(ou, XLine.ar(start, end, tsustain)*freq, rq);
	ou = ou * Linen.kr(gate, at, sl, rt, doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou)
}).store;
)

(
Pdef(\plop, Pbind(
	\instrument, \shintro,
	\degree, Pseq([0],inf),
	\start, 1,
	\end, 0.001,
	\at, 0.5001,
	\rq, 1,
	\freq, 80,
	\rt, 0.01,
	\dur, 8,
	\tsustain, Pkey(\sustain) / Ptempo(),
	\amp, 0.4
)).play;
);


{ LFPulse.ar(500) * 0.1 }.play;
{ Pulse.ar(500) * 0.1 }.play;

(
Instr(\oscform, { arg freq, sinamp=1, sawamp=0, pulseamp=0, width=0.5, phase=0;
	var ou, sinosc, sawosc, pulseosc;
	sinosc = SinOsc.ar(freq, phase) * sinamp;
	pulseosc = LFPulse.ar(freq, phase, width) * sawamp;
	sawosc = LFSaw.ar(freq, phase) * pulseamp;
	ou = sinosc + pulseosc + sawosc;
}).storeSynthDef;

Instr(\sawcisse, { arg amp=0.1, gate=1, pan=0, freq=200, ffreq=5000, rq=0.5, mix=0.5, verbmix=0.5, freqmix=0.5,
			ffreq_base=1000, ffreq_base2=1000, rq2=0.5, fmmix=0.5;
	var ou;
	var ou2;
	var ffreqenv, ffreqenv2;
	var freq1, freq2;
	var freq_env;
	var freq_mod;


	freq_env = EnvGen.ar(Env([1,Rand(0.99,1.01),1,1,Rand(0.99,1.01)],[1,1,1,18].normalizeSum),gate,levelScale:freq, timeScale:1.5);
	freq_mod = freq_env;
	//freq_mod = [SelectX.ar(fmmix, [K2A.ar(freq), K2A.ar(freq)]), SelectX.ar(fmmix, [K2A.ar(freq), K2A.ar(freq*2)])];
	//freq_mod = [freq, freq*2];
	//freq = [freq/2 * (SinOsc.ar(freq/[4,2.011].sum)*1) + freq, freq/2,freq,freq*2].sum;
	//freq2 = [freq, freq];
	//freq = SelectX.ar(freqmix, [freq1, freq2]);

	ou = Instr(\oscform).wrap((freq:freq_mod*[1,1.0072,1.0024])).sum;
	//ou = ((ou*1000).distort * 1);
	//ffreq = freq * EnvGen.ar(Env.adsr(5.7,2.1,0.5,0.1),gate,levelScale:-4,levelBias:8);
	//ffreq = freq * 2;
	ffreqenv = EnvGen.ar(Env.adsr(1.00,0.8,0.5,0.4),gate) * ffreq + ffreq_base;
	ou = RLPF.ar(ou, ffreqenv, rq);
	


	ou2 = LFSaw.ar(freq_mod*[1,1.007,1.00410,0.999]).sum;
	ffreqenv2 = (1-EnvGen.ar(Env.adsr(1.8,1.8,0.1,0.1),gate)) * ffreq + ffreq_base2;
	//ou2 = (ou2*10).distort;
	ou2 = RHPF.ar(ou2, ffreqenv2, rq2);
	ou2 = LPF.ar(ou2, freq*2);

	//mix = LFNoise1.ar(4).range(0,0.71);
	ou = SelectX.ar(mix,[ou, ou2]);

	//verbmix = 0.4;
	ou = ou * EnvGen.ar(\adsr.kr(Env.adsr(0.71,0.1,0.8,0.7)),gate,doneAction:0);
	ou = FreeVerb.ar(ou, verbmix, 0.801, 0.0);
	DetectSilence.ar(ou,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
}).addSynthDef;

)

(
Pdef(\plop, Pbind(
	\instrument, \sawcisse,
	\degree, 0,
	//\degree, [0,2,4],
	\degree, Ptuple([0,Prand([0,2,4]+21,inf),Prand([0,2,4]+14,inf),4+7,2,4],inf),
	\octave, 3,
	\mtranspose, Pstep(Pseq([0,4,2,5,3,6,4,0],inf),4),
	\root, 2,
	\legato, 1.0,
	//\mtranspose, Pstep(Pseq([0,3,1,4-7, 0,4,7,0],inf),4),
	\mix, 0.0,
	//\rq, [0.04,0.2,0.76,0.1,0.5],
	\sinamp, 0.8,
	\pulseamp, 0.051,
	\sawamp, 0.0,
	\width, 0.50,
	\rq, 0.1,
	\ffreq, 1000,
	\ffreq_base, Plazy{ var base = Prand([100, 1000, 5000]); Ptuple([base, base, base],inf) },
	\ffreq, Plazy{ var base = Prand([100, 1000, 5000]); Ptuple([base, base, base],inf) },
	\rq, Plazy{ var base = Prand([0.1, 0.5, 0.7]); Ptuple([base, base, base],inf) },

	\rq2, 0.7,
	//\mix, Pseg(Pseq([0,1],inf),8),
	\ffreq_base2, 500,
	//\mix, [0.20,0.00,0.51,0.8,0.1],
	\verbmix, 0.4,
	\dur, 1/2 * 8,
	\amp, 0.02
)).play;
);

(
Pbindef(\bla, 
	\instrument, \sawcisse,
	\degree, 0,
	\degree, Ptuple([0,2,4,4+7,Prand([0,2,4]+21,inf),Prand([0,2,4]+14,inf)],inf),
	\octave, 3,
	\mtranspose, Pstep(Pseq([0,4,2,5,3,6,4,0],inf),4),
	\root, 2,
	\legato, 1.0,
	//\mtranspose, Pstep(Pseq([0,3,1,4-7, 0,4,7,0],inf),4),
	\ffreq, Pkey(\freq)*4,
	\fmmix, 1.0,
	\mix, 0,
	//\rq, [0.04,0.2,0.76,0.1,0.5],
	\sinamp, 0.1,
	\pulseamp, 0.451,
	\sawamp, 0.4,
	\width, 0.50,
	\rq, 0.1,
	\ffreq_base, Pkey(\ffreq)/8,

	\rq2, 0.1,
	//\mix, Pseg(Pseq([0,1],inf),8),
	\ffreq_base2, Pkey(\ffreq)/2,
	//\mix, [0.20,0.00,0.51,0.8,0.1],
	\verbmix, 0.4,
	\dur, 1/2 * 8,
	\amp, 0.02
).play;
);

(
Pbindef(\plop, 
	\instrument, \sawcisse,
	//\degree, 0,
	//\degree, Ptuple([0,2,4,4+7,Prand([0,2,4]+21,inf),Prand([0,2,4]+14,inf)],inf),
	//\octave, 4,
	//\mtranspose, Pstep(Pseq([0,4,2,5,3,6,4,0],inf),4),
	\degree, Ptuple([0,2,4],inf),
	\root, 3,
	\legato, 1.0,
	//\mtranspose, Pstep(Pseq([0,3,1,4-7, 0,4,7,0],inf),4),
	\fmmix, 0.0,
	\mix, 0.00,
	//\rq, [0.04,0.2,0.76,0.1,0.5],
	\sinamp, 0.0,
	\pulseamp, 0.051,
	\sawamp, 0.4,
	\width, 0.10,
	\rq, 0.1,
	\ffreq, Pkey(\freq)*5,
	\ffreq_base, Pkey(\ffreq)/4,

	\rq2, 0.8,
	//\mix, Pseg(Pseq([0,1],inf),8),
	\ffreq_base2, Pkey(\ffreq)/8,
	//\mix, [0.20,0.00,0.51,0.8,0.1],
	\verbmix, 0.0,
	\dur, 1/2 * 8,
	\amp, 0.01
).play;
);

(
Pbindef(\plop, 
	\instrument, \sawcisse,
	//\degree, Ptuple([0,2,4,4+7,Prand([0,2,4]+21,inf),Prand([0,2,4]+14,inf)],inf),
	\degree, Ptuple([0,2,4],inf),
	\octave, 3,
	\mtranspose, Pstep(Pseq([0,4,2,5,3,6,4,0],inf),4),
	\amp, 0.01
).play;
);

(

Instr(\sawtroi, { arg amp=0.1, gate=1, pan=0, freq=200, verbmix=0.5, room=0.8, damp=0.1, freqdiff=0.001,
		ffreq1=2000, ffreq1_base=500, rq1=0.5, amp1=0.1,
		ffreq2=2000, ffreq2_base=500, rq2=0.5, amp2=0.1,
		ffreq3=2000, ffreq3_base=500, rq3=0.5, amp3=0.1;

	var ou;
	var ou2, ou3, ou1;
	var fenv1, fenv2, fenv3;
	ou = LFSaw.ar(freq*[1,1+freqdiff,1-freqdiff-0.001]);
	fenv1 = EnvGen.ar(\env1.ar(Env.adsr(0.11,0.4,0.11,0.1)),gate,doneAction:0) * ffreq1 + ffreq1_base;
	fenv2 = EnvGen.ar(\env2.ar(Env.adsr(0.1,0.22,0.1,0.4)),gate,doneAction:0) * ffreq2 + ffreq2_base;
	fenv3 = EnvGen.ar(\env3.ar(Env.adsr(0.2,0.1,0.7,0.4)),gate,doneAction:0) * ffreq3 + ffreq3_base;
	ou1 = RLPF.ar(ou, fenv1, rq1);
	ou2 = RLPF.ar(ou, fenv2, rq2);
	ou3 = RLPF.ar(ou, fenv3, rq3);
	ou = (ou1 * amp1) + (ou2 * amp2) + (ou3 * amp3);
	ou = ou.sum * EnvGen.ar(Env.adsr(0.1,0.4,0.81,0.4),gate,doneAction:0);
	ou = FreeVerb.ar(ou, verbmix, room, damp);
	DetectSilence.ar(ou,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);


}).addSynthDef;
)

(
Pdef(\saaa, Pbind(
	\instrument, \sawtroi,
	\degree, Pseq([0,2,4,2,4,2,0,2],inf),
	\mtranspose, Pstep(Pseq([0,4,2,5,3,6,4,0],inf),3),
	\root, -3,
	\octave, 3,
	\legato, 0.4,

	\freqdiff, 0.001,
	//\ffreq1, Pkey(\freq)*9,
	\ffreq1, 4000,
	\rq1, 0.7,
	\amp1, 0.1,

	\ffreq2, 2000,
	\rq2, 0.1,
	\amp2, 0.1,
	\ffreq2_base, 2000,

	\ffreq3, 1790,
	\rq3, 0.1,
	\amp3, 0.1,

	\verbmix, 0.4,
	\room, 0.4,
	\damp, 0.0,

	\dur, 1/12,
	\amp, 0.1
)).play;
)


(
Instr(\noiseman, { arg amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	ou = WhiteNoise.ar(1);
	ou = DynKlank.ar(`[[100,200,300,400,500],{1.0.rand}!5,{1.0.rand+0.001}!5],ou, LFNoise1.ar(5).range(0.9,1.1), LFNoise1.ar(5).range(1,101)) /8;
	ou = LPF.ar(ou, 1500);
	ou = HPF.ar(ou, 150);
	ou = ou * EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
}).addSynthDef;
)


(
Pdef(\plop, Pbind(
	\instrument, \noiseman,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 0.1
)).play;
);












(
SynthDef(\note_env, { arg out=0, gate=1, doneAction=2;
	var ou;
	var numlevels = 16;
	ou = EnvGen.ar(\env.kr(Env([0,1,0] ++ (0!numlevels),[0.1,0.1] ++ (0!numlevels))),gate,doneAction:doneAction);
	Out.ar(out, ou);
}).add;
)
