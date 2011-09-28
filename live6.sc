s.boot;



(
s.waitForBoot({

"/home/ggz/code/sc/seco/seco.sc".loadDocument;
~seq = ~mk_sequencer.value;
~seq.test_player(\BigKick);

});

)
~seq.current_test_player.as_event
(
s.waitForBoot({
"/home/ggz/code/sc/seco/seco.sc".loadDocument;

~synthlib = [
	\sinadsr,
	\vowel,
	\kickTrig1,
	\snTrig1,
	\boom1,
	\KSpluck,
	\KSpluck2,
	\KSpluck3,
	\hihat
].collect({ arg i; i -> i });

~seq = ~mk_sequencer.value;
~seq.load_patlib( ~synthlib );
~seq.make_gui;

});
)

(
~seq.save_project("projperc2");
~seq.load_project("projperc2");


)

(
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
(
~btl_cells = GUI.hLayoutView.new(Window.new, Rect(0,0,100,100));
~bt = Button.new(~btl_cells)
~btl_cells.pad
~btl_cells.jinsets;
)
nil.value = 4
