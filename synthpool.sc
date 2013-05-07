SynthDef(\bufsin1, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, bufnum, pos=0, finepos=0, range=0.01;
	var ou;
	var osc;
	var bufsig;
	var phase;
	osc = SinOsc.ar(freq*[1.001,0.999,1]);
	//osc = osc.sum;
	phase = osc * range + pos + finepos;

	bufsig = BufRd.ar(2, bufnum, phase*BufFrames.ir(bufnum), 1);
	bufsig = bufsig.sum;
	ou = bufsig;
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}, metadata:(specs:(
	finepos: ControlSpec(-0.001,0.001,\lin, 0, 1),
	range: ControlSpec(-0.01,0.01,\lin, 0, 1),
	//pos: \bipolar,
))).store;
//////////////////// modulators

SynthDef(\gater, { arg out=0, amp=1, gate=1, tesustain=0.1, t_gtrig=1;
	var ou;
	var tsustain = tesustain;
	ou = Trig.kr(t_gtrig,tsustain) * amp - 0.1;
	Out.kr(out, ou);
}).store;




SynthDef(\gated_asr, { arg out=0, amp=1, gate=1, attack=0.1, release=0.1, envgate=1;
	var ou;
	envgate.poll;
	ou = EnvGen.ar(Env.asr(attack,1,release),envgate,doneAction:0) * amp;
	Out.kr(out, ou);
}, metadata:(specs:(
	envgate: ControlSpec(0,1,\lin, 0, 1)
))).store;


SynthDef(\lfo1, { arg out=0, freq=1, amp=1;
	var sig = SinOsc.kr(freq);
	sig = sig * amp;
	Out.kr(out, sig);
}, metadata:(specs:(
	freq: \lofreq.asSpec
))).store;

SynthDef(\lfo_tri, { arg out=0, freq=1;
	var sig = LFTri.kr(freq);
	Out.kr(out, sig);
}, metadata:(specs:(
	freq: \lofreq.asSpec
))).store;

SynthDef(\lfo_asr, { arg out=0, freq=1, gate=1, attackTime=0.1, releaseTime=0.1, doneAction=0;
	var sig = SinOsc.kr(freq);
	sig = sig * EnvGen.ar(Env.asr(attackTime,1,releaseTime),gate,doneAction:doneAction);
	Out.kr(out, sig);
}, metadata:(specs:(
	freq: \lofreq.asSpec
))).store;

SynthDef(\line1, { arg out=0, duration=0.5;
	var sig = Line.kr(0, 1, duration);
	Out.kr(out, sig);
}, metadata:(specs:(
	duration: ControlSpec(0.001,4,\lin, 0, 1)
))).store;


SynthDef(\adsr1, { arg out, attack, gate=1, doneAction=0;
	var sig = EnvGen.kr(Env.adsr(attack,0.1,1,0.1), gate, doneAction:doneAction);
	Out.kr(out, sig);
}).add;

SynthDef(\comb1, { arg in, out, mix=0.5, maxdelaytime=0.4, delaytime=0.4, decaytime=2, gate=1;
	//var sig = EnvGen.kr(Env.adsr(attack,0.1,1,0.1), gate, doneAction:doneAction);
	var sig, sigwet;
	sigwet = In.ar(in, 2);
	sig = CombL.ar(sigwet, maxdelaytime, delaytime, decaytime);
	sig = SelectX.ar(mix, [sigwet, sig]);
	Out.ar(out, sig);
}).store;

//SynthDef(\modenv, { |out, val=0, t_trig=1, gate=1, tsustain, curve=0, doneAction=0|
//       var start = In.kr(out, 1);
//	   var sig;
//	   start.poll;
//	   sig = EnvGen.kr(Env([start, val], [tsustain], curve), t_trig, doneAction: doneAction);
//	   //sig.poll;
//       ReplaceOut.kr(out, sig);
//}).store;

// stolen from  Bjorn Westergard


SynthDef(\dubecho,{|out=0, in=0, length = 1, fb = 0.8, sep = 0.012, mix=0.5, hpfreq=400, lpfreq=5000, noisefreq=12, delayfac=0,
		offset=0, rotate=0, shift=0|
	var input = In.ar(in, 2);
	var output;
	//length = length.lag(0.01);
	//length = LPF.kr(length, 10);
	fb = LPF.kr(fb, 1);
	sep = LPF.kr(fb, 1);
	delayfac = LPF.kr(fb, 1);
	output = input + Fb({

		arg feedback; // this will contain the delayed output from the Fb unit

		var left,right;
		var magic;
		feedback = Limiter.ar(feedback, 1);
		magic = LeakDC.ar(feedback*fb + input);
		magic = HPF.ar(magic, hpfreq); // filter's on the feedback path
		magic = LPF.ar(magic, lpfreq);
		magic = magic.tanh; // and some more non-linearity in the form of distortion
		//#left, right = magic; // let's have named variables for the left and right channels
		magic = FreqShift.ar(magic, [0-shift,shift]);
		#left, right = magic; 
		//#left, right = Rotate2.ar(left, right, rotate); 
		magic = [
			DelayC.ar(left, 1, 
				(LFNoise2.ar(noisefreq).range(delayfac*sep,sep)+offset).clip(0,1)
			), 
			DelayC.ar(right, 1, 
				(LFNoise2.ar(noisefreq).range(sep,sep*delayfac)-offset).clip(0,1)
			)
		]; // In addition to the main delay handled by the feedback quark, this adds separately modulated delays to the left and right channels, which with a small "sep" value creates a bit of spatialization

	},length);
	output = SelectX.ar(mix,[input, output]);
	output = Rotate2.ar(output[0], output[1], rotate); 
	Out.ar(out, output);
}, metadata: (
	specs: (
		sep: ControlSpec.new(0.0001,1, \exp, 0, 0),
		fb: ControlSpec.new(0.0001,2, \lin, 0, 0),
		delayfac: ControlSpec.new(0,0.9999, \lin, 0, 0),
		offset: ControlSpec.new(-0.1,0.1, \lin, 0, 0),
		shift: ControlSpec.new(-1000,1000, \lin, 0, 0),
		rotate: \bipolar.asSpec,
	)
)).store;

SynthDef(\dubecho_orig,{|out=0, in=0, length = 1, fb = 0.8, sep = 0.012, mix=0.5|
	var input = In.ar(in, 2);
	var output = input + Fb({

		arg feedback; // this will contain the delayed output from the Fb unit

		var left,right;
		var magic = LeakDC.ar(feedback*fb + input);
		magic = HPF.ar(magic, 400); // filter's on the feedback path
		magic = LPF.ar(magic, 5000);
		magic = magic.tanh; // and some more non-linearity in the form of distortion
		#left, right = magic; // let's have named variables for the left and right channels
		magic = [DelayC.ar(left, 1, LFNoise2.ar(12).range(0,sep)), DelayC.ar(right, 1, LFNoise2.ar(12).range(sep,0))]; // In addition to the main delay handled by the feedback quark, this adds separately modulated delays to the left and right channels, which with a small "sep" value creates a bit of spatialization

	},length);
	output = SelectX.ar(mix,[output, input]);
	Out.ar(out, output);
}).store;

SynthDef(\guitar2, { arg out=0, freq=200, release=4, amp=0.1, pan = 0, doneAction=2;
	var ou, pluck, period, string;
	freq = freq * [0.99,1,2,0.98];
	pluck = PinkNoise.ar(Decay.kr(Line.kr(1, 0, 0.05), 0.05));
	period = freq.reciprocal;
	string = CombL.ar(pluck, period, period, 4);
	ou = LeakDC.ar(LPF.ar(string, 12000));
	ou = Splay.ar(ou, XLine.ar(0.1,1,0.3));
	ou = ou * XLine.ar(1,1/1000,release, doneAction:doneAction);
	Out.ar(out, Pan2.ar(ou, pan, amp) * 0.4);
} ).store;

SynthDef(\guitar, { arg out=0, freq=200, release=2, amp=0.1, pan = 0, doneAction=2;
	var ou, pluck, period, string;
	freq = freq * [0.99,1,2,0.98];
	ou = SinOsc.ar(freq);
	ou = Splay.ar(ou, XLine.ar(0.1,1,0.3));
	ou = ou * XLine.ar(1,1/1000,release, doneAction:doneAction);
	Out.ar(out, Pan2.ar(ou, pan, amp) * 0.4);
} ).store;

SynthDef(\ch, { | out=0, decay = 3, amp = 0.1, freqfactor = 1, doneAction=2, delayfactor=1, shift=(-200) |
	var sig = WhiteNoise.ar;
	var del;
	sig = LPF.ar(sig, (12000*freqfactor).clip(10,15000));
	//sig = sig + DelayC.ar(sig, 0.1,LFNoise2.ar(1/2).range(0.001,0.01)*delayfactor);
	del = DelayC.ar(sig, 0.1,LFNoise2.ar(1/2).range(0.001,0.01)*delayfactor);
	del = FreqShift.ar(del, shift);
	sig = sig + del;
	sig = HPF.ar(sig, (4000*freqfactor).clip(10,15000), 0.05);
	sig = sig * EnvGen.kr(Env.perc(0.01,decay*0.8), doneAction:doneAction);
	Out.ar(out, 15 * sig.dup * amp);
}, metadata: (
	specs: (
		shift: ControlSpec.new((-2000),2000, \lin, 0, 0)
	)
)).store;

//////////////////// synths

	SynthDef(\string, { | out=0 gate=1 freq=1000 |
		var aEnv, osc, flt;
		aEnv = EnvGen.kr(Env.asr(0.2, 1, 0.5), gate, doneAction: 2);
		osc = Saw.ar([LFCub.kr(0.3, Rand(0, 1), freq * 0.003, freq), freq, LFCub.kr(0.7, Rand(0, 1), freq * 0.001, freq)]);
		flt = LPF.ar(osc, 1500, aEnv);
		Out.ar(out, flt);
	}).add;

	SynthDef(\bass, { | out=0 gate=1 freq |
		var aEnv, fEnv, osc, flt;
		aEnv = EnvGen.kr(Env.asr(0, 1, 1), gate, doneAction: 2);
		fEnv = EnvGen.kr(Env.perc(0, 3), levelScale: 6000);
		osc = Mix([Saw.ar(freq * [1, 1.005]), Pulse.ar(freq / 2, 0.5)]);
		flt = LPF.ar(osc, fEnv + 100, aEnv);
		Out.ar(out, flt);
	}).add;

(
SynthDef(\strings, { arg out, freq=440, amp=0.1, gate=1, pan, freqLag=0.2;
					var env, in, delay, f1, f2;
					f1 = freq.lag(freqLag);
					f2 = freq.lag(freqLag * 0.5);
					delay = 0.25 / f2;
					env = Env.asr(0, 1, 0.3);
					in = WhiteNoise.ar(180);
					in = CombL.ar(in, delay, delay, 1);
					in = Resonz.ar(in, f1, 0.001).abs;
					in = in * EnvGen.kr(env, gate, doneAction:2);
					Out.ar(out, Pan2.ar(in, pan, amp));
}).add;
)
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
}).store;
)

(
SynthDef('kicklank', { arg out=0, gate=1, release=0.3, pan=0, amp=0.1, distamp=20, wet=0.1, attack=0.005;
    var freqs, ringtimes, signal, imp, distsig;
	imp = Impulse.ar(0.2, 0, 0.1);
    freqs = Control.names([\freqs]).kr([081,82,83,84, 85]);
    ringtimes = Control.names([\ringtimes]).kr([1, 1, 1, 1, 1]/2);
    signal = DynKlank.ar(`[freqs, nil, ringtimes ], imp);
	distsig = (signal*distamp).tanh /4;
	signal = SelectX.ar(wet, [signal, distsig]);
	signal = signal * 2.5;
	signal = signal * EnvGen.ar(Env.perc(attack,release),gate,doneAction:2);
	signal = Pan2.ar(signal, pan, amp);
    Out.ar(out, signal);
}).store;
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
}).store;

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
}).store;

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
}).store;

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
}).store;


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
}).store;

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
 
}).store;

SynthDef(\saxo, { |out, freq=440, amp=0.1, gate=1|
	var num = 16;
	var harms = Array.series(num, 1, 1) * Array.exprand(num, 0.995, 1.001);
	var snd = SinOsc.ar(freq * SinOsc.kr(Rand(2.0,5.0),0,Rand(0.001, 0.01),1) * harms, mul:Array.geom(num, 1, 0.63));
	snd = Splay.ar(snd);
	snd = BBandPass.ar(snd, freq * XLine.kr(0.1,4,0.01), 2);
	snd = snd * amp * EnvGen.ar(Env.adsr(0.001, 0.2, 0.7, 0.2), gate, doneAction:2);
	Out.ar(out, snd!2);
}).store;

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
}).store;

SynthDef(\lead2, {	arg out=0, freq = 100, pan=0, amp=0.1, mdetune=1.004, gate=1, rq=0.1, fratio = 1, fbase=400, wet=1, fbfreq=100, fbamp=0.8, fbpamp=1; 
	var fb, ou, filtenv;
	ou = LFSaw.ar(freq * [1, mdetune]).sum;
	filtenv = EnvGen.ar(Env.adsr(0.01,0.25,0.07,0.3), gate, 1, fbase, doneAction:0) * freq * Lag.kr(fratio,0.1);
	ou = RLPF.ar(ou, filtenv, rq);
	fb = LocalIn.ar(1) + ou;
	fb = HPF.ar(fb, fbfreq);
	LocalOut.ar(fb * fbamp);
	fb = Limiter.ar(fb, amp);
	fb = SelectX.ar(wet, [ou, fb*fbpamp]);
	fb = fb * EnvGen.ar(\adsr.kr(Env.adsr(0.001,0.4,0.9,0.1)), gate, doneAction:2);
	fb = Pan2.ar(fb, pan, amp);
	Out.ar(out, fb);
}).store;

SynthDef(\lead3, {	arg out=0, freq = 100, pan=0, amp=0.1, mdetune=1.004, gate=1, rq=0.1, fratio = 1, fbase=400, wet=1, fbfreq=100, fbamp=0.8, fbpamp=1; 
	var fb, ou, filtenv;
	ou = LFSaw.ar(freq * [1, mdetune]).sum;
	filtenv = EnvGen.ar(\adsr_filter.kr(Env.adsr(0.01,0.25,0.07,0.3)), gate, 1, fbase, doneAction:0) * freq * Lag.kr(fratio,0.1);
	ou = RLPF.ar(ou, filtenv, rq);
	fb = LocalIn.ar(1) + ou;
	fb = HPF.ar(fb, fbfreq);
	LocalOut.ar(fb * fbamp);
	fb = Limiter.ar(fb, amp);
	fb = SelectX.ar(wet, [ou, fb*fbpamp]);
	fb = fb * EnvGen.ar(\adsr.kr(Env.adsr(0.001,0.4,0.9,0.1)), gate, doneAction:2);
	fb = Pan2.ar(fb, pan, amp);
	Out.ar(out, fb);
}).store;

// yep, an organ with a sub bass tone :D
SynthDef(\organ, { |out, freq=440, amp=0.1, gate=1|
    var snd;
    snd = Splay.ar(SinOsc.ar(freq*Array.geom(4,1,2), mul:1/4));
    snd = snd + SinOsc.ar(freq/2, mul:0.4)!2;
    snd = snd * EnvGen.ar(Env.asr(0.001,1,0.01), gate, doneAction:2);
    OffsetOut.ar(out, snd * amp);
}).store;

SynthDef(\monosampler, {| out = 0, amp=0.1, pan=0, bufnum = 0, gate = 1, pos = 0, speed = 1, loop=0, doneAction=2|

	var player,env;
	env = EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:doneAction) * amp;
	player = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:doneAction, loop: loop);
	player = Pan2.ar(player, pan, amp * 2);
	Out.ar(out, player * env);

}, metadata:(specs:(
	bufnum: (numchan: 1)
))).store;

SynthDef(\stereosampler, {| out = 0, amp=0.1, bufnum = 0, gate = 1, pos = 0, speed = 1, loop=0, doneAction=2, pan=0|

	var player,env;
	env =  EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:doneAction);
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:doneAction, loop: loop);
	player = Pan2.ar(player, pan, amp * 2);
	Out.ar(out, player);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;

SynthDef(\stereosampler_sec, {| out = 0, amp=0.1, buf = 0, gate = 1, start=0, end=1, speed = 1, loop=0|

	var player,env;
	var rate;
	var bufnum = buf;
	env =  EnvGen.kr(Env([0,1,1,0],[0.01,(end-start)/speed,0.1]), gate, doneAction:2);
	//BufFrames.kr(bufnum).poll;
	rate = BufRateScale.kr(bufnum) * speed;
	player = PlayBuf.ar(2, bufnum, rate, 1, startPos: (start*BufSampleRate.kr(bufnum)).poll, doneAction:2, loop: loop);
	player = player * env * amp;
	Out.ar(out, player);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;
)


///////////////////// effects

(
SynthDef(\echo, { arg out=0, in=0, maxdtime=0.6, dtime=0.2, decay=2, wet=1, gate=1;
        var env, ou;
        env = Linen.kr(gate, 0.05, 1, decay, doneAction:14);
        in = In.ar(in, 2);
		ou = CombL.ar(in, maxdtime, dtime, decay, 1, in);
		ou = SelectX.ar(wet, [in,ou]);
        Out.ar(out, ou);
}, [\ir, \ir, \ir, 0.1, 0.1, 0]).store;


)
