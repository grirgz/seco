
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
Ptempo
(
Pdef(\rabi, Pbind(
	\instrument, \rabi,
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
)).play;
);

(
Pdef(\par, Ppar([
	Pdef(\rabi),
	Pdef(\awi),
])).play
)

s.meter


Help("instr").gui
Help.gui

Instr

(
	Instr(\saw, { arg freq=200,amp=0.5;
		LFSaw.ar(freq,0.0, amp)
	});

	Instr(\rlpf, { arg in,freq=200,rq=0.1;
		RLPF.ar(in, freq, rq);
	});

	Instr(\ppp, { arg freq=200;
		var ou;
		ou = Instr.ar(\saw,[freq,0.5]);
		Instr.ar(\rlpf, [ou, freq, 0.1]) ! 2
	});
)

SynthDescLib.global.at(\rabi2).def.inspect


~dn = Instr(\ppp).asDefName
~dn = Instr(\ppp).asSynthDef.add
~dn.name
(
Pdef(\pam, Pbind(
	\instrument, ~dn.name,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 0.1
)).play;
);

Patch(\ppp).play

(
a = SynthDef(\joe, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
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
a.dump

Instr(\saw).func.postcs


(
a = { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	ou = \bla.kr(1);
	ou = LFSaw.ar(freq*[1,1.01,0.99],LFSaw.ar(2).range(0,2));
	ou = ou + DelayL.ar(ou,0.01,[0.02,0.001,LFSaw.ar(freq).range(0,0.02)]);
	ou = ou.sum;
	ou = RLPF.ar(ou,freq*2,0.4);
	ou = ou * EnvGen.ar(Env.adsr(0.7,0.4,0.8,1.7),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}
)

a.def.argNames


{ arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	ou = LFSaw.ar(freq*[1,1.01,0.99],LFSaw.ar(2).range(0,2));
	ou = ou + DelayL.ar(ou,0.01,[0.02,0.001,LFSaw.ar(freq).range(0,0.02)]);
	ou = ou.sum;
	ou = RLPF.ar(ou,freq*2,0.4);
	ou = ou * EnvGen.ar(Env.adsr(0.7,0.4,0.8,1.7),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}

Help.gui
AudioSpec()
(
	Instr(\saw, { arg freq=200,amp=0.5;
		LFSaw.ar(freq,0.0, amp);
	}).add;

	Instr(\rlpf, { arg in,freq=200,rq=0.1;
		RLPF.ar(in, freq, rq);
	},[AudioSpec()]).add;

	SynthDef(\ppp, { arg out=0, freq=200;
		var ou;
		ou = Instr(\saw).ar(freq, 0.5);
		ou = Instr(\rlpf).ar(ou, freq) ! 2;
		Out.ar(out, ou);
	}).add;

)
Control
Pbind(\instrument, \ppp,\freq,400,\rq, 0.04,\ffratio, 1).play;
{ SinOsc.ar }.play
s.boot
a = { arg bla, rah; bla + rah }
a.valueWithEnvir((bla:2,rah:4));

(
	Instr(\saw, { arg freq=400,amp=0.1;
		LFSaw.ar(freq,0.0, amp);
	}).add;

	Instr(\rlpf, { arg in,freq=400,rq=0.1;
		RLPF.ar(in, freq, rq);
	},[\audio]).add;

    SynthDef(\ppp, { arg out=0, freq=200, rq=0.1, gate=1, ffratio=1;
        var ou;
        //ou = Instr(\saw).value((freq:freq, amp:0.5));
        ou = Instr(\saw).value(freq, 0.5);
        //ou = Instr(\rlpf).value((in:ou, freq:freq*ffratio,rq:rq)) ! 2;
        ou = Instr(\rlpf).ar(ou, freq*ffratio,rq) ! 2;
		ou = ou * EnvGen.ar(Env.adsr(0.1,0.1,1,0.1),gate,doneAction:2);
        Out.ar(out, ou);
    }).add;

)
Main.version
\plop.asDefName
s.boot


(
SynthDef(\pulse, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
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
}).add;
)

(
Mdef(\pulse, Pbind(
	\instrument, \pulse,
	\octave, 3,
	\degree, Pseq([1,3,5, 4,6b,8],inf),
	\dur, Pxrand([1/4,1/8],inf),
	\amp, 0.1
)).play;
);

s.boot

(
{
	i=WhiteNoise.ar(mul: 0.5);
	4.do{
		i=MoogFF.ar(i, MouseX.kr(50, 15000, 1), MouseY.kr(2, 0))
	};
	i;
}.play;
)

4.do { "plop" }

(
SynthDef(\moogstack, {
var sig=WhiteNoise.ar(mul: 0.5);
4.do{sig=MoogFF.ar(sig, MouseX.kr(50, 15000, 1), MouseY.kr(2, 0))};
Out.ar([0,1],sig)
}).add;
)
Synth(\moogstack);
