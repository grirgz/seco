(
s.waitForBoot({
"/home/ggz/code/sc/seco/main.sc".load;
TempoClock.tempo = 1.5;

~synthlib = [
	\audiotrack_expander,
	\lead2,
	\pulsepass,
	\flute1,
	\miaou1,
	\ringbpf1,
	\piano2,
	\pmosc,
	\monosampler,
	\stereosampler,
	\ss_comb,
	\ss_combfreq,
].collect({ arg i; i -> i });

~effectlib = [
	\echo
].collect({arg i; i -> i });

~samplelib = [
	"sounds/perc1.wav",
	"sounds/pok1.wav",
	"sounds/amen-break.wav",
	"sounds/default.wav"
];

~seq = ~mk_sequencer.value;
~seq.load_patlib( ~synthlib );
~seq.load_effectlib( ~effectlib );
//~seq.set_presetlib_path("mypresets2");
~seq.append_samplelib_from_path("sounds/" );
~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );
~seq.make_gui;

});
)

~seq.save_project("blablareggae");
~seq.load_project("blablareggae");
(
SynthDef(\lead2, {	arg out=0, freq = 100, pan=0, amp=0.1, mdetune=1.004, gate=1, rq=0.1, fratio = 1, fbase=400, wet=1, fbfreq=100, fbamp=0.8, fbpamp=1, rt=0.4; 
	var fb, ou, filtenv;
	//gate = LFPulse.ar(1);
	ou = LFSaw.ar(freq * [1, mdetune]).sum;
	filtenv = EnvGen.ar(Env.adsr(0.01,0.25,0.07,0.3), gate, freq * fratio, fbase, doneAction:0);
	ou = RLPF.ar(ou, filtenv, rq);
	fb = LocalIn.ar(1) + ou;
	fb = HPF.ar(fb, fbfreq);
	LocalOut.ar(fb * fbamp);
	fb = Limiter.ar(fb, amp);
	fb = SelectX.ar(wet, [ou, fb*fbpamp]);
	fb = fb * EnvGen.ar(Env.adsr(0.001,0.4,0.9,rt), gate, doneAction:2);
	fb = Pan2.ar(fb, pan, amp);
	Out.ar(out, fb);
}).store;

SynthDef(\echo, { arg out=0, in=0, maxdtime=0.6, dtime=0.2, decay=2, wet=1, gate=1;
        var env, ou;
        env = Linen.kr(gate, 0.05, 1, decay, doneAction:14);
        in = In.ar(in, 2);
		ou = CombL.ar(in, maxdtime, dtime, decay, 1, in);
		ou = SelectX.ar(wet, [in,ou]);
        Out.ar(out, ou);
}, [\ir, \ir, \ir, 0.1, 0.1, 0]).store;
SynthDef(\record_input, { arg out = 0, bufnum = 0, sustain;
		var input, env;
        input = SoundIn.ar([0,1]);
		env = EnvGen.kr(Env.linen(0,sustain,0), doneAction:2); // stop recording after dur..
        RecordBuf.ar(input, bufnum, doneAction: 0, run:env, loop: 0);
}).store;
SynthDef(\record_input_mono, { arg out = 0, bufnum = 0, sustain;
		var input, env;
        input = SoundIn.ar([0,1]).sum;
		env = EnvGen.kr(Env.linen(0,sustain,0), doneAction:2); // stop recording after dur..
        RecordBuf.ar(input, bufnum, doneAction: 0, run:env, loop: 0);
}).store;

SynthDef(\ringbpf1, { arg out=0, gate=1, freq=200, mod_freqratio=5, mod_ampratio= 0.5, amp=0.1, modulator_amp=1, pan=0, ffreqdetune=0.1, rq=0.1;
	var modulator, panner, result, env;

	env = EnvGen.ar(~make_adsr.(\adsr),gate, doneAction:2);
	modulator = Pulse.ar(mod_freqratio*([1.017,0.989]*freq), add: mod_ampratio*amp, mul: modulator_amp);
	result = SinOsc.ar([1.01,0.998,1.024]*freq, mul: modulator).sum;
	result = BPF.ar(result, freq+ffreqdetune, rq);
	result = result * env;
	panner = Pan2.ar(result, pan,amp);
	Out.ar(out, panner);
}).store;


SynthDef(\miaou1, { arg out=0, amp=0.1, gate=1, pan=0, panfreq=1/3, noiserate = 12, noisedetune=0.1, freq = 180;
	var env, ou, cpan;

	cpan = LFNoise1.kr(panfreq)+pan;
	env = EnvGen.kr(~make_adsr.(\adsr),gate, doneAction:2);

	ou = Blip.ar(LFNoise0.ar([1.01,0.99,1]*noiserate, noisedetune*freq, freq), (env)*12 + 1);
	ou = Pan2.ar(ou, cpan).sum * env * amp;
	Out.ar(out, ou);
}).store;

SynthDef(\piano2, { arg out=0, amp=0.1, pan=0, freq=200, gate=1;
	var strike, env, noise, pitch, delayTime, detune;
	var ou;

	strike = Impulse.ar(0.01);
	env = Decay2.ar(strike, 0.008, 0.04);
	pitch = freq.cpsmidi;

	ou = Mix.ar(Array.fill(3, { arg i;

		detune = #[-0.02, 0, 0.05, 0.01].at(i);
		delayTime = 1 / (pitch + detune).midicps;

		noise = LFNoise2.ar(3000, env);
		CombL.ar(noise, delayTime, delayTime, 100)
	}));
	ou = ou * EnvGen.ar(~make_adsr.(\adsr),gate,doneAction:2);
	ou = Pan2.ar(ou,pan,amp);
	Out.ar(out, ou);
}).store;

SynthDef(\flute1,{ arg out=0, pan=0, amp=0.1, gate=1, noise=1, freq=200, rq=0.1;
	var ou, env;
	//ou = WhiteNoise.ar(1);
	env = EnvGen.kr(~make_adsr.(\adsr),gate, doneAction:2);
	ou = PinkNoise.ar(noise);
	ou = BPF.ar(ou, freq*[1.0146,0.9987,1], rq);
	ou = Pan2.ar(ou.sum, pan) * env * amp;
	Out.ar(out,ou);

}).store;

SynthDef(\monosampler, {| out = 0, amp=0.6, pan=0, bufnum = 0, gate = 1, pos = 0, speed = 1, loop=0|

	var player,env;
	env = EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2) * amp;
	player = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:2, loop: loop);
	player = Pan2.ar(player, pan, amp);
	Out.ar(out, player * env);

}, metadata:(specs:(
	bufnum: (numchan: 1)
))).store;

SynthDef(\stereosampler, {| out = 0, amp=0.6, bufnum = 0, gate = 1, pos = 0, speed = 1, loop=0|

	var player,env;
	env =  EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2);
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:2, loop: loop);
	player = player * env * amp;
	Out.ar(out, player);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;

// experiment

SynthDef(\pulsepass,{ arg out=0, gate=1, amp=0.1, pan=0, noise=1, freq=250, bpffratio1=1, bpffratio2=1, bpfrq1=1, bpfrq2=1, apdec1=1, apdec2=1;
	var nois, gen, genenv, ou, ou2;

	gen = Pulse.ar([1.01,1,0.996998]*freq);
	nois = PinkNoise.ar(noise);
	gen = gen+nois;
	genenv = gen * EnvGen.ar(~make_adsr.(\adsr_pre),gate,doneAction:0);
	ou = genenv;
	ou = AllpassL.ar(ou, 1/30, (freq*0.99).reciprocal,apdec1);
	ou = AllpassL.ar(ou, 1/30, (freq*1.09).reciprocal,apdec2);
	ou = ou.sum;
	ou2 = BPF.ar(genenv, freq*bpffratio1,bpfrq1);
	ou = BPF.ar(ou, freq*bpffratio2,bpfrq2);
	ou = ou + ou2;
	ou = ou * EnvGen.ar(~make_adsr.(\adsr),gate,doneAction:2);

	ou = Pan2.ar(ou,pan, amp);
	Out.ar(out, ou);
}, metadata:(specs:(
	noise: ControlSpec(0, 5, \lin, 0.0001, 0),
	bpffratio1: ControlSpec(0, 3, \lin, 0, 1),
	bpffratio2: ControlSpec(0, 3, \lin, 0, 1),
	bpfrq1: ControlSpec(0.000001, 3, \exp, 0, 1),
	bpfrq2: ControlSpec(0.000001, 3, \exp, 0, 1),
	apdec1: ControlSpec(0.000001, 3, \exp, 0, 1),
	apdec2: ControlSpec(0.000001, 3, \exp, 0, 1)
))).store;




)

s.nodeTree
s.boot
