s.boot;
s.gui
s.quit


(
	var punch =	{
		arg osc, n=10;
		{
			arg out=0, amp=0.1, agate=1, freq=500, sustain=0.2, lpf=120, rq=0.9;
			var ou, li;
			li = XLine.kr(freq, 1, sustain);
			ou = Mix.fill(n, { |i| osc.value(li + (i * 0.1)) });
			ou = LPF.ar(ou, lpf, rq);
			ou = ou * EnvGen.kr(Env.perc(0.000001,sustain), doneAction:2 );
			Out.ar(out, ou * amp * agate);
		}
	};
	SynthDef("punch_saw", punch.value(LFSaw.ar(_))).store;
	SynthDef("punch_sin", punch.value(SinOsc.ar(_))).store;
	SynthDef("punch_pulse", punch.value(LFPulse.ar(_))).store;
	SynthDef("punch_blip", punch.value(Blip.ar(_,9))).store;

~synthDef_adsr = { arg name, func;
	SynthDef(name, { arg out=0, amp=0.1, gate=1, sustain=0.5;
		var env, envctl, ou;

		env = Env.adsr(0.02, 0.2, 0.25, 0.1, 1, -4);
		envctl = Control.names([\adsr]).kr( env.asArray );
		ou = SynthDef.wrap(func, prependArgs:[gate, sustain]) * EnvGen.kr(envctl, gate, doneAction:2);
		Out.ar(out, ou * amp);
	}).add;
};

~synthDef_adsr.(\piouadsr, { arg gate, sustain, start_freq=500, end_freq=50, modf=9, modc=1;
	var ou;
	ou = SinOsc.ar(
		XLine.kr(start_freq, end_freq, sustain)*SinOsc.kr(modf)+modc,
		XLine.kr(000, 10, sustain)
	); 
	ou;
});

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
}).add;

SynthDef("piou", {
	arg out=0, amp=0.1, sustain=0.4; //TODO: is an array ok ?
	var ou;
	ou = SinOsc.ar(
		XLine.kr(500, 50, sustain)*SinOsc.kr(9)+1,
		XLine.kr(000, 10, sustain)
	) * EnvGen.kr(Env.linen(0.1,0.5,0.1), timeScale:sustain, doneAction:2);
	Out.ar(out, ou * amp)
}).store;
 
SynthDef("fm2", {
	arg out=0, amp=0.1, sustain=0.5, freq=500, modf=200, modbpf=900;
	var ou;
	ou = SinOsc.ar( SinOsc.kr(modf)+1*freq)
	* EnvGen.kr(Env.linen(0.1,0.5,0.1), timeScale:sustain, doneAction:2);
	ou = BPF.ar(ou, SinOsc.kr(SinOsc.kr(1)+1*modbpf)+1*freq);
	Out.ar(out, ou * amp)
}).store;
 
SynthDef("shh", {
	arg out=0, amp=0.1, sustain=0.5, freq=440, modf=9, noise=0.5;
	var ou;
	ou = WhiteNoise.ar(noise);
	ou = LPF.ar(ou, SinOsc.kr(modf)+1*freq)
	* EnvGen.kr(Env.perc(0.001,sustain), doneAction:2);
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


SynthDef("klankgeo", {
	arg out=0, amp=0.1, sustain=3.0, freq=140, at=1.5, rt=1.5, rq=0.5;
	var ou;
	ou = Klank.ar(`[Array.geom(10,freq,2), nil, [1, 1, 1, 1]], PinkNoise.ar(0.007));
	ou = ou * EnvGen.kr(Env.linen(at,1,rt), timeScale:sustain, doneAction:2);
	ou = BPF.ar(ou, freq, rq);
	Out.ar(out, ou*amp);
}).store;
SynthDef("kick", {
	arg out=0, amp=0.1, sustain=0.125, t_trig=1, shift= -550; 
	var ou, env;
	ou = SinOsc.ar(600) + SinOsc.ar(500) + BrownNoise.ar(0.51);
	ou = FreqShift.ar(ou, shift);
	ou = ou * Decay.kr(t_trig, sustain);
	env = EnvGen.ar(Env.perc(0.00001,sustain),doneAction:2);
	ou = ou * amp;
	Out.ar(out, ou);
}).store;

SynthDef("bass_Ex", { 
	arg out, freq = 1000, sustain = 1, pan = 0, cut = 4000, rez = 0.8, amp = 1;
	var ou; 
	ou = Pan2.ar( 
		RLPF.ar( SinOsc.ar(freq,0.05), cut, rez),
		pan
	) * EnvGen.kr(Env.linen(0.01, 1, 0.3), timeScale:sustain,
	doneAction:2);
	ou = ou * AmpComp.kr(freq, (3*12+6).midicps);
	ou = ou * amp;
	Out.ar(out, ou);
}).store;

SynthDef("mixpass", {
	arg out=0, amp=0.1, sustain=3.0, freq=140, at=1.5, rt=1.5, pow=8, maxdelay=0.2,
	delay=0.2, decay=5;
	var ou;
	ou = Mix.fill(10, {
		|ii| var i=ii+1; SinOsc.ar(freq*(sin(i**pow)+i))/10
	});
	ou = ou * EnvGen.kr(Env.linen(at,sustain,rt), doneAction:2);
	//ou = AllpassN.ar(ou, maxdelay, delay, decay);
	ou = BPF.ar(ou, freq, 1.9);
	ou = ou * amp;
	DetectSilence.ar(ou, time:2, doneAction:2);
	Out.ar(out, ou);
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
	arg out=0, t_trig=1, att=0.005, rel=1.25, noise=1, f_base=70, 
		mod=4, range=300, carrier=400, amp=0.1;
 
	d = Decay2.kr(t_trig, att, rel, BrownNoise.ar(noise) 
		+ SinOsc.ar(f_base)
		+ SinOsc.ar(SinOsc.kr(mod,1.0)*range+carrier)
	);
	DetectSilence.ar(in: d, amp: 0.001, time: 0.1, doneAction: 2);
	Out.ar(out,d*amp)
 
}).store;
 
SynthDef(\distort, { arg out=0, pregain=40, amp=0.2, gate=1;
 var env;
 env = Linen.kr(gate, 0.05, 1, 0.1, 2);
 XOut.ar(out, env, (In.ar(out, 2) * pregain).distort * amp);
}, [\ir, 0.1, 0.1, 0]).store;
 
SynthDef(\felix, { arg out=0, freq=940, amp=0.9, gate=1;
 var env, ou;
 env = Linen.kr(gate, 0.05, 1, 0.1, 2);
 ou = In.ar(out, 2);
 ou = BPF.ar(ou, freq, 1.1);
 XOut.ar(out, env, ou);
}, [\ir, 0.1, 0.1, 0]).store;
 
Instr("plop", { arg out=0, num=3, freq=200;
	var ou;
	ou = Mix.fill(num, {|i|
		LFSaw.ar(freq+(0*4)) })*EnvGen.kr(Env.perc,doneAction:2);
	ou = LPF.ar(ou, 100);
	Out.ar(out, ou)
});
 
 
SynthDef("bulum", { arg out=0, amp=0.1, agate=1, freq=200, nfreq=10, 
	at=1, sustain=1, rt=0.1, range=100;
	var env, env2, ou;
	env = EnvGen.kr(Env.linen(rt,1,rt,range), timeScale:sustain);
	env2 = EnvGen.kr(Env.linen(rt,1,rt), timeScale:sustain, doneAction:2);
	ou = SinOsc.ar(freq+(LFNoise0.kr(nfreq)*env))*env2;
	Out.ar(out, ou * amp * agate);
}).store;
 
SynthDef("sine", { arg out=0, amp=0.1, agate=1, freq=200, nfreq=10, 
	at=1, sustain=1, rt=0.1, rangex=100;
	var env, env2, ou;
	env = EnvGen.kr(Env.linen(at,1,rt,nfreq), timeScale:sustain);
	env2 = EnvGen.kr(Env.linen(at,1,rt), timeScale:sustain, doneAction:2);
	ou = SinOsc.ar(SinOsc.kr(env)+1*rangex+freq)*env2;
	Out.ar(out, ou * amp * agate);
}).store;
	SynthDef(\psiou, {
		arg out, amp=0.1;
		Out.ar(out,
			SinOsc.ar(LPF.kr(SinOsc.kr(LFSaw.kr(80)*XLine.kr(100,8,3)),300)*XLine.kr(800,1,15))
			* EnvGen.kr(Env.linen(1,1,1),doneAction:2) * amp
		);
 
	}).store;
 
	SynthDef("lpsaw", {
		arg out=0, freq=440, sustain=1, amp=0.1, at=0.1, rt=0.1;
		var ou;
		ou = LFSaw.ar(freq);
		ou = LPF.ar(ou, freq);
		ou = ou * EnvGen.ar(Env.linen(at, 1, rt), doneAction:2, timeScale:sustain);
		Out.ar(out, ou * amp);
	}).store;
	SynthDef("lpsawlfo", {
		arg out=0, freq=440, sustain=1, amp=0.1, at=0.1, rt=0.1, lfo=4,
		bw=20;
		var ou;
		ou = LFSaw.ar(SinOsc.kr(lfo)+1*bw+freq);
		ou = LPF.ar(ou, freq);
		ou = ou * EnvGen.ar(Env.linen(at, 1, rt), doneAction:2, timeScale:sustain);
		Out.ar(out, ou * amp);
	}).store;
	SynthDef("lppulse", {
		arg out=0, freq=440, sustain=1, amp=0.1, at=0.1, rt=0.1;
		var ou;
		ou = LFPulse.ar(freq);
		ou = LPF.ar(ou, freq);
		ou = ou * EnvGen.ar(Env.linen(at, 1, rt), doneAction:2, timeScale:sustain);
		Out.ar(out, ou * amp);
	}).store;
	SynthDef("lpsawpulse", {
		arg out=0, freq=440, sustain=1, amp=0.1, at=0.1, rt=0.1;
		var ou;
		ou = Mix.ar([LFPulse.ar(freq), LFSaw.ar(freq)]);
		ou = LPF.ar(ou, freq);
		ou = ou * EnvGen.ar(Env.linen(at, 1, rt), doneAction:2, timeScale:sustain);
		Out.ar(out, ou * amp);
	}).store;
	SynthDef("lpsawpulsenoise", {
		arg out=0, freq=440, sustain=1, amp=0.1, at=0.1, rt=0.1;
		var ou;
		//ou = PinkNoise.ar(5.51);
		ou = BrownNoise.ar(1.01);
		//ou = Ringz.ar(ou, freq, 0.5);
		ou = BPF.ar(ou, freq, 0.051);
		ou = BPF.ar(ou, freq, SinOsc.kr(0.1)+1*0.951);
		ou = ou * EnvGen.ar(Env.linen(at, 1, rt), doneAction:2, timeScale:sustain);
		Out.ar(out, ou * amp*29);
	}).store;
	SynthDef("lpsawpulsenoise", {
		arg out=0, freq=440, sustain=1, amp=0.1, at=0.1, rt=0.1;
		var ou;
		//ou = PinkNoise.ar(5.51);
		ou = BrownNoise.ar(1.01);
		//ou = Ringz.ar(ou, freq, 0.5);
		ou = BPF.ar(ou, freq, 0.051);
		ou = BPF.ar(ou, freq, XLine.kr(1.0,0.01,0.3));
		ou = ou * EnvGen.ar(Env.linen(at, 1, rt), doneAction:2, timeScale:sustain);
		Out.ar(out, ou * amp*29);
	}).store;
	SynthDef("elpsawpulse", {
		arg out=0, freq=440, sustain=1, amp=0.1, at=0.1, rt=0.1;
		var ou, env, env2;
		ou = Mix.ar([LFPulse.ar(freq), LFSaw.ar(freq)]);
		env = Env.linen(at,1,rt);
		env2 = Env.linen(at,1,rt);
		ou = LPF.ar(ou, freq/*EnvGen.kr(env, 0, freq, 1, sustain)*/);
		//ou = LPF.ar(ou, XLine.kr(1000,0,5.1));
		ou = ou * EnvGen.ar(env2, doneAction:2, timeScale:sustain);
		Out.ar(out, ou * amp);
	}).store;

)
Synth(\elpsawpulse)

(

SynthDef(\play_from_to, { arg out, bufnum, from=0.0, to=1.0, sustain=1.0;
        var env;
        env = EnvGen.ar(Env.linen(0.01, sustain, 0.01), 1, doneAction:2);
        Out.ar(out,
                BufRd.ar(1, bufnum,
                        Line.ar(from, to, sustain) * BufFrames.kr(bufnum)
                ) * env
        )


}).add;


SynthDef(\pgrain,
        { arg out = 0, freq=800, sustain=0.001, amp=0.5, pan = 0;
                var window;
                window = Env.sine(sustain, amp * AmpCompA.kr(freq));
                Out.ar(out,
                        Pan2.ar(
                                SinOsc.ar(freq),
                                pan
                        ) * EnvGen.ar(window, doneAction:2)
                )
        }
).add;

SynthDef(\noiseGrain,
        { arg out = 0, freq=800, sustain=0.001, amp=0.5, pan = 0;
                var window;
                window = Env.perc(0.002, sustain, amp * AmpCompA.kr(freq));
                Out.ar(out,
                        Pan2.ar(
                                Ringz.ar(PinkNoise.ar(0.1), freq, 2.6),
                                pan
                        ) * EnvGen.ar(window, doneAction:2)
                )
        }
).add;

)

(
s.waitForBoot({
"/home/ggz/code/sc/seco/seco.sc".loadDocument;

~synthlib = [
	\sinadsr,
	\piouadsr,
	"piou",
	"fm2",
	"shh",
	"kickTrig1",
	"snTrig1",
	"klankgeo",
	"kick",
	"bass_Ex",
	"mixpass",
	"hihat",
	"punch_saw",
	"punch_sin",
	"punch_pulse",
	"punch_blip",
	"boom1",
	\distort,
	\felix,
	"bulum",
	"sine",
	\psiou,
	"lpsaw",
	"lpsawlfo",
	"lppulse",
	"lpsawpulse",
	"lpsawpulsenoise",
	"lpsawpulsenoise",
	"elpsawpulse"
].collect({ arg i; i -> i });

~seq = ~mk_sequencer.value;
~seq.load_patlib( ~synthlib );
~seq.make_gui;

});
)
(
~seq.save_project("proj2");
~seq.load_project("proj2");


)

Platform.userAppSupportDir
Platform.userAppSupportDir
Archive.archiveDir
s.boot
Archive.archiveDir
"plop".writeArchive("plop");
Archive.global[\bla] = "plop";
Archive.write("niark")


~bla = (
	value: 4,
	action: { arg self, 
);

SynthDescLib.global.browse;
