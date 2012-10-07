440 * 6/5

(
SynthDef(\ChicagoPad, { |out = 0, freq = 440, cutoff = 500, amp = 0.2, gate=1|

	var snd;

	freq = freq + SinOsc.kr(0.1, 0, 1, 20);

	snd = Saw.ar([freq, freq+1, freq-1, freq*3/2 /2, freq*6/5 /2])*0.1;

	snd = snd + VarSaw.ar(0.99*[freq, freq+1, freq-1,freq*3/2 /2, freq*6/5 /2],0, LFTri.kr(0.3).range(0.25,0.9))*0.1;

	snd = Mix(snd);

	snd = RLPF.ar(snd, SinOsc.kr(0.1, 0, 100, 5000), 0.1);

	snd = GVerb.ar(snd ,40, 10, 0.6, 0.6, -3, -9, -11)*0.2;

	

	//snd = RLPF.ar(snd, SinOsc.kr(0.08, 0.5, cutoff/10, cutoff), 0.2);

	snd = MoogFF.ar(snd, SinOsc.kr(0.08, 0.5, cutoff/10, cutoff), 3, 0);

	snd = DelayC.ar(snd, 1.5, 1,0.8);

	//snd = snd * EnvGen.ar(Env.linen(0.001, 0.01,0.01,1), doneAction:2);
	snd = snd * EnvGen.ar(Env.adsr(0.4, 0.01,0.8,0.4), gate, doneAction:2);

	Out.ar(out, [snd, AllpassC.ar(snd, 0.5, 0.05, 0.3)]*amp);

}).add;
)


(
SynthDef(\ChicagoPad, { |out = 0, freq = 440, freq2=660, freq3=528, cutoff = 500, amp = 0.2, gate=1, spread=1, center=0|

	var snd;

	freq = freq + SinOsc.kr(freq*1/2, 0, freq/2, 0);

	snd = Saw.ar([freq, freq+1, freq-1, freq2, freq3] * [0.99,1])*0.1;

	snd = snd + VarSaw.ar(0.99*[freq, freq+1, freq-1, freq2, freq3, freq2+1, freq3+1],0, LFTri.kr(7.13).range(0.00,0.1))*0.1;


	snd = Mix(snd);
	//snd = GVerb.ar(snd ,40, 10, [0.7,0.1], 0.1, -3, -9, -11)*0.2;
	//snd = GVerb.ar(snd ,40, 20, 0.1, 0.1, 3, 9, 11,1)*0.4;
	snd = FreeVerb.ar(snd, 0.51,10,0.1)*2;
	snd = snd *8;
	snd = RLPF.ar(snd, SinOsc.kr([0.1,4.2], 0, [1700,480], [4000,700,5000])/[20.51,20], SinOsc.ar(0.1,1.5*pi)+1.05)/3;
	//snd = [AllpassC.ar(snd, 0.15, 0.05+SinOsc.ar(0.15).range(0,0.01), 0.3), snd];
	//snd = snd  ! 2;
	snd = Mix(snd);
	//snd = snd.sum;


	

	//snd = RLPF.ar(snd, SinOsc.kr(0.08, 0.5, cutoff/10, cutoff), 0.2);

	snd = MoogFF.ar(snd, SinOsc.kr(0.08, 0.5, cutoff/10, cutoff), 3, 0);

	//snd = DelayC.ar(snd, 1.5, 1,0.8);

	snd = snd * EnvGen.ar(Env.adsr(0.4, 0.01,0.8,0.4), gate, doneAction:2);


	//Out.ar(out, [snd, AllpassC.ar(snd, 0.5, 0.05, 0.3)]*amp);
	//snd = snd ! 2;
	snd = [snd , AllpassC.ar(snd, 0.45, 0.05+SinOsc.ar(0.11).range(0,0.045), 0.3)];
	spread = SinOsc.ar(1).range(0.2, 4.5);
	spread = 1;
	center = SinOsc.ar(1).range(-0.7, 0.7);
	center = 0;
	//snd = Splay.ar(snd, spread, amp, center);
	snd = snd * amp;
	Out.ar(out, snd);

}).add;
)

(
Pdef(\plop2, Pbind(
	\instrument, \ChicagoPad,
	\degree, Pseq([0,4,0,3,1],inf),
	\root, -4,
	\octave, 5,
	//\degree, 0,
	\freq2, Pkey(\freq)*(3/2) / 2,
	//\freq3, Pkey(\freq)*(5/4) / 2,
	\freq3, Pkey(\freq)*(5/6) / 2,
	\legato, 1,
	\cutoff, 4500,
	\spread, 0,
	\dur, 4,
	\amp, 0.2
)).play;
);
(
Pdef(\mel, Pbind(
	\instrument, \lead2,
	\degree_step, Pstep(Pseq([0],inf),4),
	\degree, Pseq([0,\r, 2b,4, \r, 0,0,0],inf)+Pkey(\degree_step),
	\root, -4,
	\fratio, 2.51,
	\fbfreq, 100,
	\fbase, 100,
	\wet, 0.2,
	\rq, 0.1,
	\rt, Pseq([0.1,0.1,0.4,0.4,0.8],inf),
	\rq, Pseq([0.1,0.1,0.4,0.4,0.8]/2,inf),
	\legato, 0.4,
	\dur, 0.125,
	\amp, 0.01
)).play;
);
s.meter

(
SynthDef(\ChicagoPad, { |out = 0, freq = 440, freq2=660, freq3=528, cutoff = 500, amp = 0.2, gate=1|

	var snd;

	freq = freq + SinOsc.kr(freq*1/2, 0, freq/2, 0);

	snd = Saw.ar([freq, freq+1, freq-1, freq2, freq3] * [0.99,1])*0.1;

	snd = snd + VarSaw.ar(0.99*[freq, freq+1, freq-1, freq2, freq3, freq2+1, freq3+1],0, LFTri.kr(7.13).range(0.00,0.1))*0.1;


	snd = Mix(snd);
	snd = FreeVerb.ar(snd, 0.51,10,0.1)*2;
	snd = snd *8;
	snd = RLPF.ar(snd, SinOsc.kr([0.1,4.2], 0, [1700,480], [4000,700,5000])/[20.51,20], SinOsc.ar(0.1,1.5*pi)+1.05)/3;
	snd = Mix(snd);

	snd = MoogFF.ar(snd, SinOsc.kr(0.08, 0.5, cutoff/10, cutoff), 3, 0);

	snd = snd * EnvGen.ar(Env.adsr(0.4, 0.01,0.8,0.4), gate, doneAction:2);

	snd = [snd , AllpassC.ar(snd, 0.45, 0.05+SinOsc.ar(0.11).range(0,0.045), 0.3)];
	snd = snd * amp;
	Out.ar(out, snd);

}).add;
)

(
Pdef(\plop2, Pbind(
	\instrument, \ChicagoPad,
	\degree, Pseq([0,4,0,3,1],inf),
	\root, -4,
	\freq2, Pkey(\freq)*(3/2) / 2,
	\freq3, Pkey(\freq)*(5/6) / 2,
	\legato, 1,
	\cutoff, 4500,
	\dur, 4,
	\amp, 0.2
)).play;
);

(
Instr(\saw_gen, { |freq = 440, freq2=660, freq3=528, saw_width=0.5|

	var snd;

	snd = Saw.ar([freq, freq+1, freq-1] * [0.99,1])*0.1;
	snd = snd + VarSaw.ar(0.99*[freq, freq+1, freq-1],0, saw_width)*0.1;
	snd = Mix(snd);
});

Instr(\jupi1, {
	var ou;
	ou = Instr(\saw_gen).wrap;
	Instr(\chicago_filter_select).wrap((in:ou));

}).addSynthDef;

Instr(\chicago_gen, { |freq = 440, freq2=660, freq3=528, saw_width=0.5|

	var snd;

	snd = Saw.ar([freq, freq+1, freq-1, freq2, freq3] * [0.99,1])*0.1;
	snd = snd + VarSaw.ar(0.99*[freq, freq+1, freq-1, freq2, freq3, freq2+1, freq3+1],0, saw_width)*0.1;
	snd = Mix(snd);
});

Instr(\sawpad, { |gate=1, pan=0, amp=0.1|

	var snd;
	snd = Instr(\chicago_gen).wrap;
	snd = Pan2.ar(snd, pan, amp);
	snd = snd * EnvGen.ar(\adsr.kr(Env.adsr(0.4, 0.01,0.8,0.4)), gate, doneAction:2);
}).addSynthDef;


Instr(\chicago_filter, { |in, moog_cut = 500, moog_gain=3, amp = 0.2, gate=1, rlpf_cut=2000, rlpf_rq=0.1, preamp=10, 
							verbmix=0.51, verbroom=10, verbdamp=0.1, allpass_delay=0.05, allpass_decay=0.3|

	var snd, snd2;
	
	snd = in;
	snd = FreeVerb.ar(snd, verbmix,verbroom,verbdamp);
	snd = RLPF.ar(snd, rlpf_cut, rlpf_rq);

	snd = MoogFF.ar(snd, moog_cut, moog_gain, 0);
	snd = snd * preamp;

	snd2 = AllpassC.ar(snd, allpass_delay, allpass_delay, allpass_decay);
	snd2 = snd2 * EnvGen.ar(Env.dadsr(allpass_delay,0.1,0.1,1,0.1),gate,doneAction:0);
	snd2 = SelectX.ar(Line.ar(0,1,allpass_delay*1.5), [snd, snd2]);
	//snd2 = SelectX.ar(0.5, [snd, snd2]);

	snd = [snd , snd2];
	snd = Splay.ar(snd, SinOsc.ar([0.32,0.1]).range(-1,1).sum / 2, 2, 0);
	snd = snd * EnvGen.ar(\adsr.kr(Env.adsr(0.4, 0.4,0.8,0.4)), gate, doneAction:2);
	snd = snd * amp;
});

Instr(\chicago_filter_select, { |in, moog_cut = 500, moog_gain=3, amp = 0.2, gate=1, rlpf_cut=2000, rlpf_rq=0.1, preamp=10, 
							verbmix=0.51, verbroom=10, verbdamp=0.1, allpass_delay=0.05, allpass_decay=0.3,
							mix_verb=1, mix_rlpf=1, mix_moog=1, mix_allpass=1|

	var snd, sndf, snd2;
	
	snd = in;
	sndf = FreeVerb.ar(snd, verbmix,verbroom,verbdamp);
	snd = SelectX.ar(mix_verb, [snd, sndf]);
	sndf = RLPF.ar(snd, rlpf_cut, rlpf_rq);
	snd = SelectX.ar(mix_rlpf, [snd, sndf]);

	sndf = MoogFF.ar(snd, moog_cut, moog_gain, 0);
	snd = SelectX.ar(mix_moog, [snd, sndf]);
	snd = snd * preamp;

	snd2 = AllpassC.ar(snd, allpass_delay, allpass_delay, allpass_decay);
	snd2 = snd2 * EnvGen.ar(Env.dadsr(allpass_delay,0.1,0.1,1,0.1),gate,doneAction:0);
	snd2 = SelectX.ar(Line.ar(0,1,allpass_delay*1.5) * mix_allpass, [snd, snd2]);
	//snd2 = SelectX.ar(0.5, [snd, snd2]);

	snd = [snd , snd2];
	snd = Splay.ar(snd, 1, 2, 0);
	snd = snd * EnvGen.ar(\adsr.kr(Env.adsr(0.4, 0.4,0.8,0.4)), gate, doneAction:2);
	snd = snd * amp;
});

Instr(\chicago_pad, {
	var in;
	in = Instr(\chicago_gen).wrap(());
	Instr(\chicago_filter).wrap((in:in));
}).addSynthDef;

Instr(\chicago_pad_select, {
	var in;
	in = Instr(\chicago_gen).wrap(());
	Instr(\chicago_filter_select).wrap((in:in));
}).addSynthDef;

Instr(\chicago_pad2, { arg freq=440, rlpf_cut=6800, gate=1, rlpf_rq=1.1;
	var in;
	freq = freq + SinOsc.kr(freq*1/2, 0, freq/2, 0);
	rlpf_cut = SinOsc.ar([0.3,0.4]).range(0,1) * rlpf_cut;
	rlpf_cut = rlpf_cut.sum;
	rlpf_cut = EnvGen.ar(Env.adsr(1.1,1.1,0.21,0.4),gate,doneAction:0) * rlpf_cut + 200;
	rlpf_rq = 1 - EnvGen.ar(Env.adsr(1.1,3.1,0.11,0.4),gate,doneAction:0) * rlpf_rq + 0.05;
	in = Instr(\chicago_gen).wrap((freq:freq));
	Instr(\chicago_filter).wrap((in:in,rlpf_cut:rlpf_cut, gate:gate, rlpf_rq:rlpf_rq));
}).addSynthDef;
)

(
Pdef(\plop2, Pbind(
	\instrument, \chicago_pad2,
	\degree, Pseq([0,4,0,3,1],inf),
	\root, -4,
	\freq2, Pkey(\freq)*(3/2) / 2,
	\freq3, Pkey(\freq)*(5/6) / 2,
	\legato, 0.51,
	\cutoff, 4500,
	\dur, 0.4,
	\amp, 0.2
)).play;
);

(
Pdef(\plop2, Pbind(
	\instrument, \chicago_pad_select,
	\degree, Pseq([0,4,0,3,1],inf),
	\root, -4,
	\mix_verb, 0.8,
	\mix_moog, 0.0,
	\mix_rlpf, 0.7,
	\mix_allpass, 0.0,
	\freq2, Pkey(\freq)*(3/2) / 2,
	\freq3, Pkey(\freq)*(5/6) / 2,
	\legato, 1.0,
	\rlpf_cut, 500,
	\rlpf_rq, 1.500,
	\moog_cut, 500,
	\dur, 4,
	\amp, 0.1
)).play;
);


(
Pdef(\plop3, (Pbind(
	\instrument, \jupi1,
	//\degree, Pseq([0,4,0,3,1],inf),
	//\root, -4,
	\mix_verb, 0.5,
	\mix_moog, 0.7,
	\mix_rlpf, Pseg(Pseq([0.8,0.1],inf),10),
	\mix_allpass, 0.0,
	\legato, 0.8,
	\rlpf_cut, 500,
	\rlpf_rq, 0.500,
	\moog_cut, 500,
	//\dur, 0.125,
	\stretch, 1/8,
	\amp, 0.1
) <> 
	Pseq([
		~abcpbind.("f_d_ f2_B",1),
		~abcpbind.("f_d_ f2_G",1),
		~abcpbind.("g_e_ f2_b",1),
	],inf)
	).trace
).play;
);


DynKlank

(
Instr(\wouwou, { arg freq=200, pan=0, amp=0.1, fratio=0.5, gate=1, modscale=1;
	var ou;
	freq = Lag.kr(freq,2);
	modscale = Lag.kr(modscale,2);
	ou = SinOsc.ar([1,4.54,1.6]*modscale)*fratio*freq+freq;
	ou = SinOsc.ar(ou);
	ou = ou * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
}).addSynthDef;

Instr(\tchou, { arg freq=200, pan=0, amp=0.1, freqscale=(1/4), modscale=1, gate=1;
	var ou, ou2;
	ou = WhiteNoise.ar(amp);
	ou2 = SinOsc.ar([1.214,4.54,1.6]*modscale)*0.5+1;
	ou = DynKlank.ar(`[[800, 1071, 1153, 1723], nil, [1, 1, 1, 1]], ou, freqscale:freqscale) * ou2;
	ou = ou * 0.3;
	ou = ou * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
}).addSynthDef;
);

(
Pdef(\plop, Pmono(
	\wouwou,
	\degree, Pseq([0,1,4],inf),
	\modscale, Pseq([1,4,0.2],inf),
	\fratio, 0.8,
	\legato, 1,
	\dur, 4,
	\amp, 0.1
)).play;
);

(
Pdef(\plop2, Pmono(
	\tchou,
	\degree, Pseq([0],inf),
	\fratio, Pseq([0.1,0.8],inf),
	\legato, 1,
	\dur, 4,
	\amp, 0.1
)).play;
);
