

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

Debug.enableDebug = true;
Debug.enableDebug = false;

(
Mdef(\kick1, Pbind(
	\instrument, \kick2,
	\dur, 1,
	\amp, 0.1
));
);

(
Mdef(\snare1, Pbind(
	\instrument, \kraftySnr,
	\dur, 1,
));
);

(
Mdef.sampler(\smp, [ 
	[\nsample, \kick1],
	[\nsample, \kick],
	[\nsample, \snare],
	[\nsample, \snare1],
	[\nsample, "monosampler_l1063"]
]);
);



(
Mdef(\kick2, Pbind(
	\instrument, \kick2,
	\dur, 1,
	\amp, 0.1
));
);


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
}).add;
)

(
Pdef(\awi2, Pbind(
	\instrument, \awi,
	\degree, Pseq([0],inf),
	\dur, 1,
	\imp_ratio, 0.2,
	\legato, 0.8,
	\amp, 0.1
)).play;
);

(
Pdef(\awi, Pbind(
	\instrument, \awi,
	\adsr_imp, Pseq([
		[ Env.adsr(0.01,0.8,0.814,1.01,2) ],
		//[ Env.adsr(0.41,0.2,0.15,0.01) ],
	],inf),
	\fratio, 0.41,
	//\sawpeak, Pseq([1,2,3,4,5],inf),
	\blendsaw, Pseq([0,1,2,3]/3,inf),
	\adsr_saw, 
	//Pseq([
		Pfunc{ arg ev; [ Env.adsr(0.01,0.02,0.85,1.01,0.2).blend(
			Env.adsr(0.11,0.02,0.85,1.01,7.2),
			ev[\blendsaw]
		) ] },
		//[ Env.adsr(0.01,0.2,0.15,0.01) ],
	//],inf),
	\adsr_rq, Pseq([
		[ Env.adsr(0.11,0.11,7.15,1.01,0.1) ],
	],inf),
	\adsr_ff, Pseq([
		[ Env.adsr(0.01,0.02,1.05,0.01,1.0).range(1,2) ],
	],inf),
	\ffratio, 7,
	\rqbias, 0.41,
	\rqratio, 4,
	\imp_ratio, Pseq([20,40,1,030.5]/1,inf),
	\saw_ratio, 4.5,
	\octave, 3,
	//\degree, Pseq([0,2,4,2, 3,5,7,5],inf),
	\degree, Pseq([
		Pshuf([0,2,4,\r]),
	],inf)+Pseg(Pseq([3,0,3,4],inf),4,\step),
	\legato, 2.5,
	//\degree, Pseq([0,2,\r,2, 3,\r,7,5]+5,inf),
	\dur, 1/16,
	\amp, 0.4
)).play;
);

{ SinOsc.ar(XLine.ar(50,2000,10)) ! 2 }.play
GUI.swing
~sw = SW_Scope.new;
~sw.window.front
Quarks.gui


(
SynthDef(\rabi, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	ou = LFSaw.ar(freq*[1,1.01,0.99],LFSaw.ar(2).range(0,2));
	ou = ou + DelayL.ar(ou,0.01,[0.02,0.001,LFSaw.ar(freq).range(0,0.02)]);
	ou = ou.sum;
	ou = RLPF.ar(ou,freq*2,0.4);
	ou = ou * EnvGen.ar(Env.adsr(0.7,0.4,0.8,1.7),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)
Array.geom(5,0.25,2)
(
SynthDef(\rabi2, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, rq=0.4, timeScale=1;
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
	ou = BPF.ar(ou, freq*Array.geom(4,0.25,2), adsr_delay*4).sum;
	//ou = Limiter.ar(ou, amp);
	ou = ou * EnvGen.ar(Env.adsr(0.1,0.4,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)

(
SynthDef(\rabi3, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, rq=0.4, timeScale=1, modfratio=1, modcar=0.01, modindex=0.1;
	var ou, adsr_delay, delaymod;
	//ou = LFSaw.ar(freq*[1,1.01,0.99],LFSaw.ar(2).range(0,2));
	//ou = LFSaw.ar(freq,LFSaw.ar(2).range(0,2));
	//adsr_delay = EnvGen.kr(~make_adsr.(\adsr_delay, Env.adsr(0.21,0.6,0.15,1.21,0.01)),gate);
	adsr_delay = EnvGen.kr(Env([0.01,0.05,0.101,0.01],[0.5,0.5,0.5]),gate,timeScale:1);
	ou = 0;
	delaymod = SinOsc.ar(freq*modfratio) * modindex*modcar + modcar;
	//ou = ou + DelayL.ar(ou,0.01,[0.02,0.001,LFSaw.ar(freq).range(0,0.02)]);
	//ou = ou.sum;
	//ou = RLPF.ar(ou,freq*2,rq*6);
	ou = SinOsc.ar(freq) + ou;
	ou = LFSaw.ar(freq);
	//ou = EnvGen.kr(Env.perc(0.0001,0.0001),gate)*ou + DelayL.ar(ou, 2.91,delaymod);
	ou = DelayL.ar(ou, 2.91,delaymod * Line.ar(0,1,modcar + modindex*modcar));
	//ou = LPF.ar(ou, freq*16);
	//ou = LPF.ar(ou, freq*16);
	//ou = BPF.ar(ou, freq*Array.geom(4,0.25,2), adsr_delay*4).sum;
	//ou = BPF.ar(ou, freq*Array.geom(4,0.25,2), rq).sum;
	ou = BPF.ar(ou, freq, rq);
	//ou = Limiter.ar(ou, amp);
	ou = ou * EnvGen.ar(Env.adsr(0.1,0.4,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)
Ptempo
(
Mdef(\rabi, Pbind(
	\instrument, \rabi2,
	\degree, Pseq([[0,2,4]],inf)+Pseg(Pseq([3,0,3,4],inf),4,\step),
	\octave, 4,
	\dur, 4,
	\timeScale, Pkey(\dur)/Ptempo(),
	\mylegato, 1,
	\legato, [0.6,0.7,0.5]+0.35,
	\amp, 0.1
)).play;
);
(
Mdef(\jacob, Pbind(
	\instrument, \rabi2,
));
);
(
Mdef(\jacob3, Pbind(
	\instrument, \rabi3,
));
);

(
Pdef(\par, Ppar([
	Pdef(\rabi),
	Pdef(\awi),
])).play
)

s.meter

Scale.directory



(
{
    e = Env.perc(0.01,0.5,1);
    t = Dust.kr(200,1,10);
    a = EnvGen.kr(e,t);
    b = Blip.ar(a);
    f = MouseX.kr(0, 400.76765);
    SinOsc.ar(f, b, mul:t);
   
   
}.scope
)




(

SynthDef(\DistKlangBlip, {

| out = 0, release = 1, freq1 = 1, freq2 = 5, freq3 = 8,  freqMult = 4 |

var env = Linen.kr(Impulse.kr(0), 0.01, 1, release, doneAction:2);

var freqs = ([freq1, freq2, freq3] * freqMult).midicps;

var klang = Klang.ar(`[freqs, nil, nil ], 1, 0);

Out.ar(out, klang.tanh!2 * env * 0.5)

}).add

)

(

Pbind(*[

instrument: \DistKlangBlip,

dur: Pxrand([0.25, 0.5], inf) * 0.5,

release: Pkey(\dur),

freqMult: Pxrand((3..12), inf),

freq2: Prand([4,5], inf),

freq3: Prand([8,12], inf)

]).play;

)      



(
{
	var r, sig, mod;
	//Control rate sinwave modulator (1Hz)
	r = SinOsc.kr(1);
	//Create sin(t) and cos(t) both modulated by r
	sig = SinOsc.ar(440, mul: r, phase:[0,pi/2]);
	//Calculate r
	mod = sqrt(Mix(sig.squared));
	//Test it out with a new carrier
	SinOsc.ar(880+(400*mod),mul:mod);
}.play
)


s.boot
a = SynthDef(\bla, { arg freq; SinOsc.ar(freq) }).build
a.storeArgs
a.myfuncv

a = Instr(\blia, { arg freq; SinOsc.ar(freq) }).add

p = Pmono(\blia, \type, \instr, \dur, 0.2, \freq, Pwhite(1,8) * 100 ).play
