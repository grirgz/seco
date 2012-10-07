

s.meter
(
s.waitForBoot{
~seq = Mdef.force_init(true);
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
~seq.load_patlib( ~synthlib );
~seq.load_effectlib( ~effectlib );
~seq.set_presetlib_path("mypresets2");
~seq.append_samplelib_from_path("sounds/" );
~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );

Mdef.side_gui;



~tf = Pfunc({ arg ev; if(ev[\stepline] == 1) { \note } { \rest } });
~ff = Pfunc({ arg ev; if(ev[\stepline1] == 1) { 100 } { \rest } });

//Debug.enableDebug = false;

}
)
Mdef.main.save_project("live18");
Mdef.main.load_project("live18");

Debug.enableDebug = true;
Debug.enableDebug = false;

(
~smp = {
	var path =  "/home/ggz/Musique";
	var files = (
		sacrifice: "sacrifice.wav",
		beast: "beast.wav",
	);
	var dico = Dictionary.new;
	dico[\mono] = Dictionary.new;
	files.keysValuesDo { arg key,val;
		dico[key] = Buffer.read(s, path+/+val);
		dico[\mono][key] = Buffer.readChannel(s, path+/+val, channels:[0]);
	};
	dico;
}.value;
)

(
SynthDef(\sampler, {| out = 0, amp=0.1, buf= 0, gate = 1, pos = 0, speed = 1, loop=0|

	var bufnum = buf;
	var player,env;
	env =  EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2);
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:2, loop: loop);
	player = player * env * amp;
	Out.ar(out, player);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;
)

(
SynthDef(\grainer, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, buf,
						gdur=2, trate=12, pos=0, prate = 1, durgap;
	var ou;
	var dur, clk;
	var epos;
	var bufnum = buf;
	dur = gdur;
	//trate = 1/max(0.001,(dur - durgap));
	clk = Impulse.kr(trate);
	pos = MouseX.kr(0,1);
	epos = EnvGen.ar(~make_adsr.(\adsr_pos,Env.adsr(0.01,0.1,0.8,0.1)),gate);
	prate = EnvGen.ar(~make_adsr.(\adsr_prate,Env.adsr(0.01,0.1,0.8,0.1)),gate);
	//pos = (pos+epos) * BufDur.kr(bufnum) + TRand.kr(0, 0.01, clk);
	pos = epos * BufDur.kr(bufnum) + TRand.kr(0, 0.01, clk);
	ou = TGrains.ar(2, clk, bufnum, prate, pos, dur, pan, 0.1);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.01),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp * 2);
	Out.ar(out, ou);
}).store;
)
(

SynthDef(\grainpitcher, { arg out=0, amp=0.1, freq=200, pos=0, gate=1, buf=b, gdur=0.84, rq=18.7;
	var dfreq, hasFreq, bfreq, sig;
        var trate, dur, sig2, sig3, trate2, grate;
		var sig3amp;
		var bufnum = buf;
        //trate = MouseY.kr(2,120,1);
		trate = 120;
        dur = gdur;
		dur = 0.1;
        //z = Blip.ar(Line.kr(800,400,1), 6, 0.1);
        //PitchShift.ar(z, 0.02, Line.kr(0,0.2,1), 0, 0.0001) ! 2
        //PitchShift.ar(z, 0.002, 800/freq, 0, 0.0001) *0.2*hasFreq ! 2
		//pos = LFNoise0.ar(1) * 0.001 + pos;
		//pos = MouseX.kr(0,1);
        sig2 = TGrains.ar(2, Impulse.ar(trate), bufnum, 1, pos*BufDur.kr(bufnum), dur, 0, 0.1).sum;
		# dfreq, hasFreq = Pitch.kr(sig2, peakThreshold:0.7);
		grate = (freq/dfreq).clip(0.001,10);
		trate2 = trate * grate / 1.52;
		trate2 =  1/(dur/grate)*8;
		dur = dur / grate;
		dur = 0.1;
        sig = TGrains.ar(2, Impulse.ar(trate2), bufnum, grate, pos*BufDur.kr(bufnum), dur, 0, 0.1);
		//sig = SinOsc.ar(freq);
		//sig = sig2;
		sig3 = SinOsc.ar(freq);
		sig = BPF.ar(sig, freq,rq);
		sig = BPF.ar(sig, freq*2,rq) + sig;
		sig3amp = Amplitude.kr(sig, 0.4,0.4);
		sig = sig + (sig3*sig3amp);
		//sig = (sig3*sig3amp);
		sig = sig * EnvGen.kr(Env.adsr(0.101,0.1,0.81,0.1), gate, doneAction:2);
		sig = sig * amp * 2;
		sig = sig ! 2;
		Out.ar(out, sig);

}).add
)
(
Pdef(\kirby2, Pbind(
	\instrument, \grainpitcher,
	\buf, ~smp[\mono][\sacrifice],
	\degree, Pseq([0,2,4,2],inf),
	\octave, 4,
	\rq, 1,
	\pos, 0.8217,
	\pos, Pseg(Pseq([0.1,0.11]+0.71,inf),10),
	//\sustain, 0.4,
	\legato, 1,
	\dur, 1/4,
	\amp, 7.8
)).play
);
(
Pdef(\kirbyfault, Pbind(
	\instrument, \lead2,
	\buf, ~smp[\mono][\sacrifice],
	\degree, Pseq([0,2,4,2],inf),
	\octave, 4,
	\pos, 0.2,
	\dur, 1/4,
	\amp, 0.2
)).play
);
(
Mdef(\gsacri, Pbind(
	\instrument, \grainer,
	\buf, ~smp[\mono][\sacrifice],
	\pos, Pseg(Pseq([0.1,0.9],inf),5),
	//\pos, 0.4,
	\adsr_pos, Pfunc { arg ev; [Env([0.2,0.4,0.0]+ev[\pos],[1/2,1])] }, 
	\adsr_prate, [Env([1,2,0.1],[0.2,0.4]) ], 
	\gdur, 0.1,
	\durgap, 0.0,
	\prate, 0.4,
	\trate, 120,
	//\sustain, 4,
	\dur, 1,
	\amp, 0.7
))
);
(
Mdef(\sacrifi, Pbind(
	\instrument, \sampler,
	\buf, ~smp[\sacrifice],
	\speed, 0.4,
	\sustain, 4,
	\dur, 4,
	\amp, 0.7
))
);

(
Mdef(\kicki, Pbind(
	\instrument, \mykick,
	\dur, 1,
	\amp, 0.7
)).play;
);

(
Mdef(\snarei, Pbind(
	\instrument, \SOSsnare,
	\decay, 0.051,
	\stepline, Pseq([
		0,1,0,1,
	],inf),
	\type, ~tf,
	\dur, 1/2,
	\amp, 0.1
)).play;
);
