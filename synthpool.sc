//////////////////// tool synthdefs

SynthDef(\freeze_recorder, { arg inbus=0, out=0, amp=1, gate=1, buffer, doneAction=2;
	var ou;
	var bufnum = buffer;
	var env;
	var in = In.ar(inbus, 2);
	in = in * amp;
	env =  EnvGen.kr(Env.asr(0.0001,1,0.0001), gate, doneAction:doneAction);
	RecordBuf.ar(in, bufnum, doneAction: doneAction, loop: 0);
}).store;

SynthDef(\freeze_player, { arg out=0, amp=1, gate=1, doneAction=2, buffer;
	var player,env;
	var bufnum = buffer;
	var speed = 1;
	var pos = 0;
	var loop = 0;
	//env =  EnvGen.kr(Env.asr(0.0001,1,0.0001), gate, doneAction:doneAction);
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:doneAction, loop: loop);
	//player = player * env * amp;
	player = player * amp;
	Out.ar(out, player);

}).store;




//////////////////// modulators

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
SynthDef(\modenv, { |out, firstsynth=0, firstval=0, t_trig=1, gate=1, tsustain, val=0, curve=0, doneAction=0|
       var start = Select.kr(firstsynth, [In.kr(out, 1), firstval]);
	   var sig;
	   //start.poll;
	   //sig = EnvGen.kr(Env([start, val], [tsustain], curve), t_trig, doneAction: doneAction);
	   sig = EnvGen.kr(Env([start, val], [tsustain], curve), t_trig, doneAction: doneAction);
	   //sig = VarLag.kr(val, tsustain);
	   //sig.poll;
       ReplaceOut.kr(out, sig);
}).store;

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
	player = Pan2.ar(player, pan, amp);
	Out.ar(out, player * env);

}, metadata:(specs:(
	bufnum: (numchan: 1)
))).store;

SynthDef(\stereosampler, {| out = 0, amp=0.1, bufnum = 0, gate = 1, pos = 0, speed = 1, loop=0, doneAction=2|

	var player,env;
	env =  EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:doneAction);
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:doneAction, loop: loop);
	player = player * env * amp;
	Out.ar(out, player);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;

SynthDef(\stereosampler_sec, {| out = 0, amp=0.1, buf = 0, gate = 1, start=0, end=1, speed = 1, loop=0|

	var player,env;
	var rate;
	var bufnum = buf;
	env =  EnvGen.kr(Env([0,1,1,0],[0.01,(end-start)/speed,0.1]), gate, doneAction:2);
	BufFrames.kr(bufnum).poll;
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
