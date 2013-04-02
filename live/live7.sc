s.boot

(
s.waitForBoot({
"/home/ggz/code/sc/seco/main.sc".loadDocument;

~synthlib = [
	\sinadsr,
	\vowel,
	\kickTrig1,
	\snTrig1,
	\boom1,
	\KSpluck,
	\KSpluck2,
	\KSpluck3,
	\hihat,
	\formant,
	\formant2,
	\snare,
	\loop
].collect({ arg i; i -> i });

~samplelib = [
	"sounds/amen-break.wav",
	"sounds/default.wav"
];

~seq = ~mk_sequencer.value;
~seq.load_patlib( ~synthlib );
~seq.load_samplelib( ~samplelib );
~seq.make_gui;

});
)

~seq.save_project("blablareggae");
~seq.load_project("projtest8");


~seq.debugme.node.source.repeats
~seq.debugme2.get_val
~seq.debugme.data[\repeat].get_val


q 
q = Pbind(\dur, 0.1).trace
q = Pbind(\dur, Pn(0.1,10))
q = Pbind(\dur, 0.5)
r = Pbind(\freq, 200, \dur, 0.5)
q.play
Pfin(1, a).play
a = EventPatternProxy.new
b = EventPatternProxy.new
c = EventPatternProxy.new
b.source = q
c.source = r
b.play
c.play
b.isPlaying
b.player.isPlaying
b.isPlaying
b.stop
c.play
Pfin(1, b).play
b.source = Pfindur(~a, a)
~a = 4

b.source = Pn((freq:\rest),1)
b.play
b.isP
Psilence(0.1)
SynthDescLib.global.synthDescs.keys.do (_.postln)

Pseq([Psync(a, 1, 2), Psync(a, 1, 2)]).play


a = EventPatternProxy.new
b = EventPatternProxy.new
c = EventPatternProxy.new
n = EventPatternProxy.new
m = Pbind(\freq, 70, \sustain, 0.01, \dur, 0.5); n.source = m; n.play;
q = Pbind(\note, Pseq([1,2,3,4,5,6,7,8]), \dur, 0.5)
r = Pbind(\note, Pseq([1,2,3,4,5,6,7,8]+10), \lag, 0.1, \dur, 0.5)
a.source = Pn(Ppar([b, c]),inf);
b.source = q
c.source = r
a.play;
a.source = Pn(Ppar([b, c]),inf); b.source = q; c.source = r; a.play;
a.source = Ppar([b, c],inf); b.source = q; c.source = r; a.play;
a.source = Ppar([Pn(b,inf), Pn(c,inf)],inf); b.source = q; c.source = r; a.play;
b.stop
b.stop; b.source = nil; 
b.source = nil; 
b.source = nil; b.stop;
b.source = q
EventPatternProxy.defaultQuant = 4

b.source = q; b.play;
b.source = Pn(q,inf);



a = EventPatternProxy.new
b = EventPatternProxy.new
c = EventPatternProxy.new
EventPatternProxy.defaultQuant = 2

q = Pbind(\note, Pseq([1,2,3,4,5,6,7,8],1), \dur, 0.5)
r = Pbind(\note, Pseq([1,2,3,4,5,6,7,8]+10,1), \lag, 0.1, \dur, 0.5)

a.source = Ppar([b, c],inf); b.source = q; c.source = r; a.play;    // play this, it run in loop
a.source = Ppar([b ],inf); b.source = q; c.source = r; a.play;    // play this, it run in loop
a.source = Ppar([c ],inf); b.source = q; c.source = r; a.play;    // play this, it run in loop

b.source = nil;  // before the end of the b pattern, stop it
b.source = q

b.stop
b.isPlaying
b.player.isPlaying

a.quant
a.playQuant
b.quantBeat = 8

{ SinOsc.ar }.play



a = EventPatternProxy.new
b = EventPatternProxy.new
c = EventPatternProxy.new
EventPatternProxy.defaultQuant = 4

q = Pbind(\note, Pseq([1,2,3,4,5,6,7,8],1), \dur, 0.5)
r = Pbind(\note, Pseq([1,2,3,4]+10,1), \lag, 0.1, \dur, 0.5)

a.source = Ppar([b, c],inf); b.source = q; c.source = r; a.play;    // play this, it run in loop

b.source = nil;  // before the end of the b pattern, stop it
b.source = q

a.quant
a.playQuant
b.quantBeat = 8



(
SynthDef(\loop, {| out = 0, amp=0.1, bufnum = 0, gate = 1, pos = 0, speed = 1, freq = 0, endfreq = 0.001, sustain=0.5, wobble = 3, boost = 1|

	var player,env;
	freq = XLine.ar(freq,endfreq,sustain/4);
	freq = freq.cpsmidi + (LFNoise2.ar(3).range(-1,1) * (1/12));
	freq = freq.midicps;
	env = Env.adsr(0.0001,0.01, 1, sustain/8, 1);
	amp = EnvGen.kr(env, gate, doneAction:2) * amp;
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, Impulse.ar(freq), startPos: (pos*BufFrames.kr(bufnum)) + Rand(0,20), doneAction:2, loop: 1) * boost;
	player = RLPF.ar(player, SinOsc.ar(wobble/sustain).range(20000,80), XLine.ar(0.2,0.9,sustain)) * boost;
	Out.ar(out, player);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;

SynthDef(\formant, { arg out=0, amp=0.1, gate=1, freq=200, formfreq=300, bw=10;

var ou;
ou = Formant.ar(freq, formfreq, bw);
ou = ou * Linen.kr(gate, doneAction:2);
Out.ar(out, ou*amp);


}, metadata:(specs:(
	formfreq: \widefreq.asSpec,
	bw: \widefreq.asSpec
))).add;

SynthDef(\formant2, { arg out=0, amp=0.1, gate=1, freq=200, formfreq=300, bandw=10, rq=0.1;

	var ou, ou2;
	ou = WhiteNoise.ar(1);
	ou = Mix.fill(4, { arg num;
		var stack=2;
		var lou = ou;
		num = num+1;
		stack.do ({ arg i;
			lou = BPF.ar(lou, freq*num, rq);
		});
		lou;
	});
	ou = ou * Linen.kr(gate, doneAction:2) * 10;
	Out.ar(out, ou*amp);
}, metadata:(specs:(
	formfreq: \freq.asSpec,
	bandw: \freq.asSpec
))).add;

SynthDef(\saw, { arg out=0, amp=0.1, gate=1, freq=200, formfreq=300, bw=10;

var ou;
ou = Saw.ar(freq);
ou = ou * Linen.kr(gate, doneAction:2);
Out.ar(out, ou*amp);


}).add;


SynthDef(\vowel, { arg out=0, amp=0.1, gate=1, freq=200, rqfreq=100, rqoffset=10;
var env, env2, envf, enva, ou, ou2;
env = ~make_adsr.(\adsr);
env2 = ~make_adsr.(\adsr_vowel);
envf = EnvGen.kr(env2, gate);
enva = EnvGen.kr(env, gate,doneAction:2);
ou = Formants.ar(freq,  Vowel(\i, \bass).blend(Vowel(\o, \bass), envf + LFNoise0.kr([5,5.1],mul:0.1,add:1).lag(0.1))) ;
ou = PinkNoise.ar(1)*0.5 + ou;
ou2 = BPF.ar(ou, freq+LFNoise0.kr([1.12,5.2],1,20*enva).lag(0.1), SinOsc.kr(rqfreq*enva+rqoffset,0,1,4.1))*5;
ou = Klank.ar(`[[1.1],[0.1],[0.5]],ou,700) + ou2;
ou = ou*enva*amp;
Out.ar(out, ou);

}).store;

SynthDef(\sinadsr, {
    arg out=0, amp=0.1, gate=1, freq=440;
    var ou;
    var env, envctl;

    env = Env.adsr(0.02, 0.2, 0.25, 0.1, 1, -4);
    //env = Env.newClear(6);

    envctl = Control.names([\adsr]).kr( env.asArray );
    ou = SinOsc.ar( freq);
    ou = ou * EnvGen.kr(envctl, gate, doneAction:2);
    Out.ar(out, ou * amp)
}).store;

SynthDef("kickTrig1", { arg levK=1, t_trig=0, sustain=0.125, f1=36.7, f2=73.4, amp=0.1, out=0;
	var kEnv, ou;
	var kickEnv;
	kickEnv = Env.linen(0.001, 1.9, 0.099, 1);
	kEnv=EnvGen.ar(kickEnv,1, doneAction:2, timeScale: sustain, levelScale: levK);
	ou =Pan2.ar(Decay2.kr(t_trig, 0.005, 0.45, 
	FSinOsc.ar(f1, 0.4)+FSinOsc.ar(f2, 0.2)),0);
 
	Out.ar(out, ou * kEnv * amp);
}).store;
SynthDef("snTrig1", { arg levSn=1, t_trig=0, sustain=0.125, panPos=0, amp=0.1,
	out=0;
	var snEnv, ou;
	var snareEnv;
	snareEnv = Env.linen(0.001, 1.9, 0.099, 1);
	snEnv=EnvGen.ar(snareEnv,1, doneAction:2, timeScale: sustain, levelScale: levSn);
	ou =Pan2.ar(Decay2.kr(t_trig, 0.005, 0.25, FSinOsc.ar(38.midicps, 0.3)+ 		BrownNoise.ar(0.4)),panPos);
 
	Out.ar(out, ou*snEnv * amp);
}).store;
SynthDef("hihat", { arg out=0, amp=0.1, sustain=0.5, freq=440, noise=0.5,
	at=0.001, rq=1.9;
	var ou;
	ou = WhiteNoise.ar(noise);
	ou = ou * EnvGen.kr(Env.perc(at, sustain),doneAction:2);
	ou = Resonz.ar(ou, freq, rq);
 
	Out.ar(out, ou*amp);
}).store;


 
SynthDef("boom1", { 
	arg out=0, t_trig=1, attack=0.005, release=1.25, noise=1, f_base=70, 
		mod=4, range=300, carrier=400, amp=0.1;
 
	d = Decay2.kr(t_trig, attack, release, BrownNoise.ar(noise) 
		+ SinOsc.ar(f_base)
		+ SinOsc.ar(SinOsc.kr(mod,1.0)*range+carrier)
	);
	DetectSilence.ar(in: d, amp: 0.001, time: 0.1, doneAction: 2);
	Out.ar(out,d*amp)
 
}).store;

SynthDef("perc1", { 
	arg out=0, attack=0.005, release=1.25, lpf=50, bpf=100, bprq=0.1,
		ratio=4, range=300, carrier=400, amp=0.1;
	var mid, ou, env;

	mid = SinOsc.ar(SinOsc.ar(carrier*ratio)*range+carrier);
	mid = BPF.ar(mid, bpf, bprq);
 
 	ou = Klang.ar( `[[50,86,93,136,182],nil,nil] );
	ou = LPF.ar(ou, lpf);
	ou = mid + ou;

	env = EnvGen.kr(Env.perc(attack, release), doneAction:2);
	ou = ou * env * amp;

	Out.ar(out,ou)
 
}).store;

SynthDef("snare1", { 
	arg out=0, attack=0.005, release=1.25, relratio=0.5, amp=0.1;
	var mid, ou, ou2, env1, env2;

	ou = LFTri.ar(111);
	ou = FreqShift.ar(ou, [175, 224]).sum;
	env1 = EnvGen.ar(Env.perc(attack, release), doneAction:2);

	env2 = EnvGen.ar(Env.perc(attack, release*relratio));
	ou2 = SinOsc.ar([330,180]).sum*env2;

	ou = ou + ou2;
	ou = ou * env1 * amp;


	Out.ar(out,ou.dup)
 
}).store;



SynthDef("KSpluck", { arg freq = 200, delayDecay = 1.0, attack=0.0001, release=0.001, amp=0.1, out=0;
	var burstEnv, att = attack, dec = release;
	var signalOut, delayTime;
	delayTime = [freq, freq * 2].reciprocal;
	burstEnv = EnvGen.kr(Env.perc(att, dec));
	signalOut = PinkNoise.ar(burstEnv);
	signalOut = CombL.ar(signalOut, delayTime, delayTime, delayDecay, add: signalOut);
	DetectSilence.ar(signalOut, doneAction:2);
	Out.ar(out, signalOut*amp)
}).store;

SynthDef("KSpluck3", { arg freq = 200, delayDecay = 1.0, attack=0.1, release=0.1, amp=0.1, out=0;
	var burstEnv, att = attack, dec = release;
	var signalOut, delayTime;
	delayTime = [freq, freq * 2].reciprocal;
	burstEnv = EnvGen.kr(Env.perc(att, dec));
	signalOut = PinkNoise.ar(10);
	signalOut = CombL.ar(signalOut, delayTime, delayTime, delayDecay, add: signalOut);
	signalOut = signalOut * burstEnv;
	signalOut = signalOut * amp * 0.1;
	DetectSilence.ar(signalOut, doneAction:2);
	Out.ar([0,1], signalOut)
}).store;

SynthDef("KSpluck2", { arg freq = 200, noise=10, bpratio=0.9, rq=0.1, delayDecay = 1.0, attack=0.1, release=0.1, amp=0.1, out=0;
	var burstEnv, att = attack, dec = release;
	var signalOut, delayTime;
	delayTime = [freq, freq * 2].reciprocal;
	burstEnv = EnvGen.kr(Env.perc(att, dec));
	signalOut = WhiteNoise.ar(noise);
	signalOut = BPF.ar(signalOut, freq*bpratio, rq);
	signalOut = CombL.ar(signalOut, delayTime, delayTime, [delayDecay, delayDecay*1.0.rand], add: signalOut);
	signalOut = signalOut * burstEnv;
	signalOut = signalOut * amp * 0.01;
	DetectSilence.ar(signalOut, doneAction:2);
	Out.ar(out, signalOut.dup)
}).store;


)
