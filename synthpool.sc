(
SynthDef(\sax, { |out, freq=440, amp=0.1, gate=1, rq=2, frs=0.1, fre=4, frt=0.01, hdelta=0.001|
	var num = 16;
	var harms, snd;
	harms = Array.series(num, 1, 1) * Array.exprand(num, 1 - hdelta, 1 + hdelta);
	//harms = [1];
	snd = SinOsc.ar(freq * SinOsc.kr(Rand(0.200,4.705),0,Rand(0.01, 0.01),1) * harms, mul:Array.geom(num, 1, 0.73));
	snd = Splay.ar(snd, 1);
	snd = BBandPass.ar(snd, freq * XLine.kr(frs,fre,frt), rq);
	snd = snd * amp * EnvGen.ar(Env.adsr(0.001, 0.2, 0.7, 0.2), gate, doneAction:2);
	Out.ar(out, snd!2);
}).add;
)

(
SynthDef(\kick1, { |out=0, amp=0.1, pan=0|
	var env0, env1, env1m, son;
	
	env0 =  EnvGen.ar(Env.new([0.5, 1, 0.5, 0], [0.005, 0.06, 0.26], [-4, -2, -4]), doneAction:2);
	env1 = EnvGen.ar(Env.new([110, 59, 29], [0.005, 0.29], [-4, -5]));
	env1m = env1.midicps;
	
	son = LFPulse.ar(env1m, 0, 0.5, 1, -0.5);
	son = son + WhiteNoise.ar(1);
	son = LPF.ar(son, env1m*1.5, env0);
	son = son + SinOsc.ar(env1m, 0.5, env0);
	
	son = son * 1.2;
	son = son.clip2(1);
	
	OffsetOut.ar(out, Pan2.ar(son * amp));
}).add;

SynthDef(\kick2, { |out=0, amp=0.1, pan=0, release=0.26, fratio=1.5|
	var env0, env1, env1m, son;
	var son2;
	
	env0 =  EnvGen.ar(Env.new([0.5, 1, 0.5, 0], [0.005, 0.06, release], [-4, -2, -4]), doneAction:0);
	env1 = EnvGen.ar(Env.new([110, 59, 29], [0.005, 0.29], [-4, -5]));
	env1m = env1.midicps;
	
	son = LFPulse.ar(env1m, 0, 0.5, 1, -0.5);
	son = son + WhiteNoise.ar(1);
	son = LPF.ar(son, env1m*fratio, env0);
	son = son + SinOsc.ar(env1m, 0.5, env0);
	
	son = son * 1.2;
	son = son.clip2(1);

	son2 = GVerb.ar(son, 51, 2, 0.50, 0.5, drylevel:0) / 2;
	son = SelectX.ar(0.0, [son, son2]);
	//son = son2;
	DetectSilence.ar(son, doneAction:2);
	son = son * amp;
	//son = Pan2.ar(son);
	
	OffsetOut.ar(out, son);
}).add;

SynthDef(\kick3, { |out=0, amp=0.1, pan=0, release=0.26, fratio=1.5|
	var env0, env1, env1m, son;
	
	env0 =  EnvGen.ar(Env.new([0.5, 1, 0.5, 0], [0.005, 0.06, release], [-4, -2, -4]), doneAction:2);
	env1 = EnvGen.ar(Env.new([110, 59, 29], [0.005, 0.29], [-4, -5]));
	env1m = env1.midicps;
	
	son = LFPulse.ar(env1m, 0, 0.5, 1, -0.5);
	son = son + WhiteNoise.ar(1);
	son = LPF.ar(son, env1m*fratio, env0);
	son = son + SinOsc.ar(env1m, 0.5, env0);
	
	son = son * 1.2;
	son = son.clip2(1);

	son = son * amp;
	son = Pan2.ar(son, pan);
	
	OffsetOut.ar(out, son);
}).add;

SynthDef(\snare1, { |out=0, amp=0.1, pan=0, release=0.26, fratio=1.5|
	var env0, env1, env1m, son;
	
	env0 =  EnvGen.ar(Env.new([0.5, 1, 0.5, 0], [0.005, 0.06, release], [-4, -2, -4]), doneAction:2);
	env1 = EnvGen.ar(Env.new([110, 59, 29], [0.005, 0.29], [-4, -5]));
	env1m = env1.midicps;
	
	son = LFPulse.ar(env1m, 0, 0.5, 1, -0.5);
	son = son + WhiteNoise.ar(1);
	son = LPF.ar(son, env1m*fratio, env0);
	son = son + SinOsc.ar(env1m, 0.5, env0);
	
	son = son * 1.2;
	son = son.clip2(1);
	
	OffsetOut.ar(out, Pan2.ar(son * amp));
}).add;


SynthDef(\kraftySnr, { |amp = 1, freq = 2000, rq = 3, decay = 0.3, pan, out|
	var sig2;
    var    sig = PinkNoise.ar(amp),
        env = EnvGen.kr(Env.perc(0.01, decay), doneAction: 2);
	sig2 = sig;
	//sig = sig + DelayL.ar(BPF.ar(sig2, freq*[XLine.ar(0.01,0.41,0.001),1], 0.1).sum, 0.1,0.01);
	sig = sig + (Decay.ar(Impulse.ar(1),0.01) * LFSaw.ar(freq/2)/2);
    sig = BPF.ar(sig, freq*[1,1.01,0.99,0.4], rq*[0.1,1.1,0.9], env).sum;
	sig = sig * amp;
    Out.ar(out, Pan2.ar(sig, pan))
}).add;

SynthDef("snare1", { 
	arg out=0, gate=1, pan=0, amp=0.1, freq=111;
	var mid, ou, ou2, env1, env2;

	ou = LFTri.ar(freq);
	ou = FreqShift.ar(ou, [175, 224]).sum;
	env1 = EnvGen.ar(~make_adsr.(\adsr),gate, doneAction:2);

	env2 = EnvGen.ar(~make_adsr.(\adsr_short),gate);
	ou2 = SinOsc.ar([330,180]).sum*env2;

	ou = ou + ou2;
	ou = ou * env1;
	ou = Pan2.ar(ou,pan,amp);


	Out.ar(out,ou.dup)
 
}).add;

SynthDef(\saxo, { |out, freq=440, amp=0.1, gate=1|
	var num = 16;
	var harms = Array.series(num, 1, 1) * Array.exprand(num, 0.995, 1.001);
	var snd = SinOsc.ar(freq * SinOsc.kr(Rand(2.0,5.0),0,Rand(0.001, 0.01),1) * harms, mul:Array.geom(num, 1, 0.63));
	snd = Splay.ar(snd);
	snd = BBandPass.ar(snd, freq * XLine.kr(0.1,4,0.01), 2);
	snd = snd * amp * EnvGen.ar(Env.adsr(0.001, 0.2, 0.7, 0.2), gate, doneAction:2);
	Out.ar(out, snd!2);
}).add;

// should be more like a gated synth, but this one gives the rhythmic element
// remember to pass the bps from the language tempo!
SynthDef(\lead, { |out, freq=440, amp=0.1, gate=1, bps=2|
    var snd;
    var seq = Demand.kr(Impulse.kr(bps*4), 0, Dseq(freq*[1,3,2], inf)).lag(0.01);
    snd = LFSaw.ar(freq*{rrand(0.995, 1.005)}!4);
    snd = Splay.ar(snd);
    snd = MoogFF.ar(snd, seq, 0.5);
    snd = snd * EnvGen.ar(Env.asr(0.01,1,0.01), gate, doneAction:2);
    OffsetOut.ar(out, snd * amp);
}).add;

// yep, an organ with a sub bass tone :D
SynthDef(\organ, { |out, freq=440, amp=0.1, gate=1|
    var snd;
    snd = Splay.ar(SinOsc.ar(freq*Array.geom(4,1,2), mul:1/4));
    snd = snd + SinOsc.ar(freq/2, mul:0.4)!2;
    snd = snd * EnvGen.ar(Env.asr(0.001,1,0.01), gate, doneAction:2);
    OffsetOut.ar(out, snd * amp);
}).add;

)
