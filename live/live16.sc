
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


~tf = Pfunc({ arg ev; if(ev[\stepline] == 1) { \note } { \rest } });
~ff = Pfunc({ arg ev; if(ev[\stepline1] == 1) { 100 } { \rest } });
//Debug.enableDebug = false;

Mdef.side_gui;
}
)
Mdef.main.save_project("live16");
Mdef.main.load_project("live16");

Debug.enableDebug = true;
Debug.enableDebug = false;

// Part1 (
	(
	Mdef(\kick1, Pbind(
		\instrument, \kick2,
		\stepline, Pseq([
			1,1,1,1, 1,1,1,1,
			1,0,0,1, 1,0,0,1,

		]),
		\fratio, Pseg(Pseq([0.1,7],inf),2),
		\type, ~tf,
	));
	);

	(
	Mdef(\snare1, Pbind(
		\instrument, \kraftySnr,
		\freq, Pseq([1,2,3]*1000,inf),
		\decay, Pseq([0.1,0.2,0.3],inf),
		\stepline, Pseq([
			0,1,0,1, 1,0,1,0,
			1,1,0,1, 0,1,0,1,

		]),
		\type, ~tf,
	));
	);

	(
	Mdef(\pad2, Pbind(
		\instrument, \pad1,
		\degree, Pseq([[1,3,5],[0,2b,5]],inf),
		\sustain, 2,
		\dur, 2,
	));
	);

	(
	Mdef(\bass, Pbind(
		\instrument, \base_tri,
	));
	);
	(
	Mdef(\vil, Pbind(
		\instrument, \awi,
		\degree, Pseq([
			1,3,\r,\r, \r,0,0,2b,
			\r,\r,\r,\r, \r,\r,\r,\r, 
			1,\r,1,5, \r,5,0,2b,
			\r,\r,\r,\r, \r,\r,\r,\r, 
		],inf),
		\dur, 1/8,
		\adsr_imp, [
			Env.adsr(0.11,0.11,4.15,0.01,0.05)
		],
		\ffratio, 1.01,
		\imp_ratio, Pseq([1,7.2],inf),
		\rqscale, 0.1,
		\saw_ratio, 1.03,
		\mylegato, 1,
		\legato, 1.5,
		\amp, 0.8
	));
	);
	(
	Mdef(\stri, Pbind(
		\instrument, \trop,
		\degree, Pseq([
			Pxrand([1,5,3,\r],4),
			Pxrand([0,2b,5,\r],4),
		],inf),
		\dur, 1/8,
		\amp, 0.1
	));
	);
//)

// Part 2 (
	(
	Mdef(\pad3, Pbind(
		\instrument, \pad1,
		\degree, Pseq([
			Pseq([
				[1,3,5],
				[8,1,5]
			],2),
			Pseq([
				[0,2b,5],
				[7,2b,5]
			],2),
		],inf),
		\repeat, 4,
		\sustain, 2/5,
		\dur, 2/4,
	));
	);

	(
	Mdef(\kick2, Pbind(
		\instrument, \kick2,
		\stepline, Pseq([
			1,0,1,0, 1,0,1,0,

		]),
		\fratio, Pseg(Pseq([0.1,7],inf),2),
		\type, ~tf,
	));
	);

	(
	Mdef(\snare2, Pbind(
		\instrument, \kraftySnr,
		\freq, Pseq([1,2,3]*1000,inf),
		\decay, Pseq([0.1,0.2,0.3],inf),
		\stepline, Pseq([
			0,1,0,1, 0,1,0,1,
		]),
		\type, ~tf,
	));
	);
//)

	(
	Mdef(\nobass, Pbind(
		\instrument, \trop,
		\amp, 0.1
	) <> Pdef(\bass), \trop);
	);

(
Mdef(\part1, Ppar([
	Pdef(\kick1),
	Pdef(\snare1),
	Pdef(\pad2),
	Pdef(\bass),
	Pdef(\stri),
]));
);

(
Mdef(\part2, Ppar([
	Pdef(\kick2),
	Pdef(\snare2),
	Pdef(\pad3),
	Pdef(\bass),
]));
);

(
Mdef(\sect1, Ppar([
	Pdef(\part1),
	Pdef(\part2),
]));
);

Mdef.node(\par1).children = [\kick1, \snare1, \pad2]
Mdef.main.panels.side.set_current_group(Mdef.main.get_node(\par1))

(
SynthDef(\pad1, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, rq=4, timeScale=1;
	var ou, adsr_delay, delaymod;
	//ou = LFSaw.ar(freq*[1,1.01,0.99],LFSaw.ar(2).range(0,2));
	//ou = LFSaw.ar(freq,LFSaw.ar(2).range(0,2));
	//adsr_delay = EnvGen.kr(~make_adsr.(\adsr_delay, Env.adsr(0.21,0.6,0.15,1.21,0.01)),gate);
	adsr_delay = EnvGen.kr(Env([0.01,0.05,0.101,0.01],[0.5,0.5,0.5]),gate,timeScale:1);
	ou = 0;
	delaymod = SinOsc.ar(freq).range(0.01,0.001);
	//ou = ou + DelayL.ar(ou,0.01,[0.02,0.001,LFSaw.ar(freq).range(0,0.02)]);
	//ou = ou.sum;
	//ou = RLPF.ar(ou,freq*2,rq*6);
	ou = SinOsc.ar(freq) + ou;
	ou = LFSaw.ar(freq);
	ou = ou + DelayL.ar(ou, 0.11,delaymod);
	//ou = LPF.ar(ou, freq*16);
	//ou = LPF.ar(ou, freq*16);
	ou = BPF.ar(ou, freq*Array.geom(4,0.25,2), adsr_delay*rq).sum;
	//ou = Limiter.ar(ou, amp);
	ou = ou * EnvGen.ar(Env.adsr(0.1,0.4,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).store;
);

(
SynthDef(\base_tri, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou, width, ffreq, rq;
	width = LFSaw.ar(freq/8).range(0,1);
	width = EnvGen.ar(Env.asr(0.3,1,0.3),gate).range(0.2,0.8);
	ffreq = EnvGen.ar(Env.asr(0.1,1,0.3),gate).range(freq/2,freq*8);
	//rq = EnvGen.ar(Env.asr(0.5,1,0.3),gate) * 1.1 + 5;
	rq = EnvGen.ar(Env.asr(0.5,1,0.3),gate).range(0.51,0.15);
	ou = LFTri.ar(freq*[1,1.0001,0.99], width).sum;
	//ou = RLPF.ar(ou, ffreq*[1,1.0001,0.99], rq*[1,1.0001,0.99]).sum;
	//rq = 0.01;
	ou = RLPF.ar(ou, ffreq*[1,1.0001,0.99], rq*[1,1.0001,0.99]).sum;
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = ou / 2;
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).store;
)
(
SynthDef(\joli, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	freq = LFSaw.ar(1).range(freq*1.01,freq*0.99);
	ou = LFSaw.ar(freq*[1,1.01,0.99],LFSaw.ar(2).range(0,2));
	//ou = ou + DelayL.ar(ou,0.01,[0.02,0.001,LFSaw.ar(freq).range(0,0.02)]);
	ou = ou.sum;
	//ou = RLPF.ar(ou,freq*4,4.4);
	ou = RLPF.ar(ou, freq*5, 0.1);
	ou = ou * EnvGen.ar(Env.adsr(0.7,0.4,0.8,1.7),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).store;
)
(
SynthDef(\awi, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, imp_ratio=1, saw_ratio=1, fratio=0.01, rqscale=1, rqbias=0.1, ffratio=4, ffbias=1;
	var ou, trig, adsr_imp, adsr_saw, adsr_rq, adsr_ff;
	adsr_imp = EnvGen.kr(~make_adsr.(\adsr_imp, Env.adsr(0.41,0.2,0.15,0.01)),gate,levelScale:freq*imp_ratio);
	adsr_saw = EnvGen.kr(~make_adsr.(\adsr_saw, Env.adsr(0.41,0.2,0.15,0.01)),gate,levelScale:freq*saw_ratio);
	adsr_rq = EnvGen.kr(~make_adsr.(\adsr_rq, Env.adsr(0.41,0.2,0.15,0.01)),gate,levelScale:rqscale, levelBias:rqbias);
	adsr_ff = EnvGen.kr(~make_adsr.(\adsr_ff, Env.adsr(0.41,0.2,0.15,0.01)),gate,levelScale:ffratio*freq,levelBias:ffbias);
	trig = Impulse.ar(adsr_imp);
	freq = LFSaw.ar(adsr_saw).range(freq*(1+fratio),freq*(1-fratio));
	ou = GrainSin.ar(1, trig, 0.001,freq*[1,1.01]).sum;
	//ou = BPF.ar(ou, adsr_ff*ffratio*freq+freq, adsr_rq);
	ou = BPF.ar(ou, adsr_ff, adsr_rq);
	ou = ou * EnvGen.ar(~make_adsr.(\adsr,Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).store;
)
(
SynthDef(\flip, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou, trig, seq, env, tfreq;
	trig = CoinGate.kr(0.5, Impulse.kr(20));
	seq = Dseq([1,1/2,2,1]/2,inf);
	tfreq = Demand.kr(trig, 0, seq);
	env = EnvGen.ar(Env.perc(0.4),trig,levelScale:tfreq,levelBias:0.5);
	
	ou = LFSaw.ar(freq*[env*1.01,env]).sum;
	ou = MoogFF.ar(ou, freq*[1,2], 0.001).sum * 4;
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).store;

)
(
SynthDef(\trop, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou, noise, env;
	env = EnvGen.ar(Env.adsr(0.2,0.1,1.0,0.1,0.1),gate);
	noise = LFNoise1.ar(XLine.ar(5,20,0.4)).range(0.01,2);
	ou = SinOsc.ar(freq*(1..8)).clip(0.1);
	ou = ou / SinOsc.ar(freq/(1..8)).clip(0.1);
	ou = ou.sum;
	ou = BPF.ar(ou, freq*(env+0.5*2), noise);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate.poll,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).store;
)

(
Pdef(\flip, Pbind(
	\instrument, \flip,
	\degree, Pseq([0,2,4],inf),
	\dur, 1,
	\amp, 0.1
)).play;
);



{Pulse.ar(900,0.5,LFPulse.kr(XLine.kr(1,5,9),0,0.1,0.1))!2}.play;//Sievert Crisis // Craftwife_KR-9000, Tue, 22 May 2012 04:34:58
@redFrik RMX 
a=Demand;b=SinOsc;play{b.ar(a.ar(t=Saw.ar([5.5,6.01]),0,Dseq(0!7++200,inf)),b.ar(a.ar(t,0,Dshuf((0..2)*89,inf)).lag(0.004)))/2} // Thomas, Mon, 21 May 2012 19:14:37
play{s=Saw;w=LFNoise0.kr(2,1,3).round;n=0.5;c=LFPulse.kr([2*w,w],0,[n,n]);c*s.ar(50*w)+FreeVerb.ar(c*s.ar(300*w.lag(1)),n,1)*s.kr(2)} // Dominic Ward, Sat, 19 May 2012 00:35:24
{a=[1,4,8];r=LFNoise0.kr(0.25).range(4);(Decay.ar(Impulse.ar(a),1/(r*r))*(Saw.ar([82,207,277]*r.round))).sum.clip2(0.1)}.play // Dominic Ward, Fri, 18 May 2012 00:28:46
{BPF.ar(GrayNoise.ar(1),LFPulse.kr(0.5,0,0.1,12000,1000),0.3)!2}.play;//Heavy rain coming from a distance // Craftwife_KR-9000, Thu, 17 May 2012 13:50:45
play{RLPF.ar(PMOsc.ar(Pulse.ar(40,0.1,20,30), LFSaw.ar(120,20,25), LFNoise1.kr(4,20,24)).dup, LFNoise0.kr(4,20,45), 0.0000001)} // 林　士斌, Thu, 17 May 2012 11:10:23
{var s={|a,b|Duty.kr(Drand(a,inf),0,Dxrand(b,inf))};Blip.ar([s.((1..2),(2..9)),s.((1..2)/3,(5..9))],500)*s.((1..3),1)}.play // David Stutz ☯, Wed, 16 May 2012 19:03:21
{var s={|a,b|Duty.kr(Drand(a,inf),0,Dxrand(b,inf))};Blip.ar(s.((1..2)/3,(2..9)/2),500)*s.((1..7),1)}.play // David Stutz ☯, Wed, 16 May 2012 18:54:10
play{a=Mix(Array.fill(75,{|i|SinOsc.ar(rrand(1,50)*i+10,0,LFNoise1.kr([1.8,2.3]))}))*0.02;CombL.ar(a,15,SinOsc.ar([0.1,0.11],0,0.5,0.6),10)} // Schemawound, Wed, 16 May 2012 03:52:57
b=LFNoise2;{a=LocalIn.ar;c=Line.kr(0,9,99);140.do{d=Rand(0.1,1.0);a=SinOsc.ar(b.kr(b.kr(d,1,2),990,999)*(d*c*a+1))};LocalOut.ar(a);a!2}.play // Click Nilson, Tue, 15 May 2012 22:50:28
b=LFNoise0;{a=LocalIn.ar;c=Line.kr(0,3,99);140.do{d=Rand(0.1,1.0);a=SinOsc.ar(b.kr(b.kr(d,3,5),990,999)*(d*c*a+1))};LocalOut.ar(a);a!2}.play // Click Nilson, Tue, 15 May 2012 22:49:29
play{a=SinOsc;b=(1..9);Splay.ar(a.ar(b*55).clip(a.ar(2/b,0,0.5),a.ar(3/b,0,0.5,1))*a.ar(b*55+(4/b),0,a.ar(1/b,0,6)).tanh)/5} // Fredrik Olofsson, Tue, 15 May 2012 12:29:07
{{SinOsc.ar([70,73].midicps*SinOsc.ar(0.1))+SinOsc.ar([75,77].midicps)}*LFNoise1.ar(SinOsc.ar(0.1,0,15,30))}.play // peeq, Mon,


{Pulse.ar(900,0.5,LFPulse.kr(XLine.kr(1,5,9),0,0.1,0.1))!2}.play;
{Pulse.ar(900,0.5,LFPulse.kr(XLine.kr(1,5,9),0,0.1,0.1))!2}.play;

(
play{a=SinOsc;b=(1..9);Splay.ar(a.ar(b*55).clip(a.ar(2/b,0,0.5),a.ar(3/b,0,0.5,1))*a.ar(b*55+(4/b),0,a.ar(1/b,0,6)).tanh)/5}
)


a = Synth(\trop)
a.release
(
Pdef(\plopi, Pbind(
	\instrument, \trop,
	\degree, Pseq([
		Pxrand([1,5,3,\r],4),
		Pxrand([0,2b,5,\r],4),
	],inf),
	\dur, 1/8,
	\amp, 0.1
)).play;
);
s.queryAllNodes
s = Server.default



(
{
	SinOsc.ar(100).

}.play
)



(
SynthDef(\minou, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	freq = EnvGen.ar(Env.asr(0.5,1,0.5),gate).range(freq/2,freq);
	ou = FMGrain.ar(trigger: 0, dur: 1, carfreq: 440, modfreq: 200, index: 1, mul: 1, add: 0);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)


(
Pdef(\minou, Pbind(
	\instrument, \minou,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 0.1
)).play;
);



(
{
var delay1, delay2, source; 

//k from -1 to 1

//in samples
delay1= 100; 
delay2= 40;

source= WhiteNoise.ar(0.5)*EnvGen.ar(Env([1,1,0],[(delay1+delay2)/SampleRate.ir,0.0]), Impulse.kr(MouseY.kr(1,4)));

TwoTube.ar(source,MouseX.kr(-1,1),0.99,delay1,delay2); 
}.play

)



(
{
    var f, sf;
    sf = K2A.ar(MouseX.kr > 0.5) > 0;
    f = Ball.ar(sf, MouseY.kr(0.01, 20, 1), 0.01);
    f = f * 10 + 500;
    SinOsc.ar(f, 0, 0.2)
}.play;
)



(
)

(
Pdef(\kanki, Pbind(
	\instrument, \kicklank,
	\amp, 0.7,
)).play;
)
(
Pdef(\kank, Pbind(
	\instrument, \kank,
	\degree, Pseq([0],inf),
	\freqs, Pseq([
		[[051,85,90,100, 0120]],
		[[081,82,83,84, 85]],
		[[081,82,83,84, 85]-10],
		[[081,82,83,84, 85]+10],
	],inf),
	\ringtimes, Pseq([
		[[0.05,0.02,0.05,0.05, 0.1]],
		[1!5],
		[1!5]*4,
	],inf),
	\release, 0.08,
	\attack, 0.0091,
	\distamp, 10,
	\dry, 0.0,
	\dur, 1/2,
	\sustain, 0.15,
	\amp, 0.7,
)).play;
);

a = Synth('help-dynKlank');

a.setn(\freqs, Array.rand(4, 500, 2000));
a.setn(\ringtimes, Array.rand(4, 0.2, 4) );
a.setn(\freqs, [70,60,50,100]);
s.boot
s.meter
