
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
	//adsr_ff = EnvGen.kr(~make_adsr.(\adsr_ff, Env.adsr(0.41,0.2,0.15,0.01)),gate);
	trig = Impulse.ar(adsr_imp);
	freq = LFSaw.ar(adsr_saw).range(freq*(1+fratio),freq*(1-fratio));
	ou = GrainSin.ar(1, trig, 0.001,freq*[1,1.01]).sum;
	//ou = BPF.ar(ou, adsr_ff*ffratio*freq+freq, adsr_rq);
	//ou = BPF.ar(ou, ffratio*freq, adsr_rq);
	ou = ou * EnvGen.ar(~make_adsr.(\adsr,Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)

(
Pdef(\awi, Pbind(
	\instrument, \awi,
	\adsr_imp, Pseq([
		[ Env.adsr(0.41,0.1,0.014,0.01) ],
		//[ Env.adsr(0.41,0.2,0.15,0.01) ],
	],inf),
	\fratio, 0.41,
	\adsr_saw, Pseq([
		[ Env.adsr(0.01,0.02,0.85,0.01,7.1) ],
		//[ Env.adsr(0.01,0.2,0.15,0.01) ],
	],inf),
	\adsr_rq, Pseq([
		[ Env.adsr(0.41,0.2,0.05,0.01,7.1) ],
	],inf),
	\ffratio, 0.4,
	\rqbias, 0.01,
	\imp_ratio, 40.5,
	\saw_ratio, 10.5,
	\degree, Pseq([0,2,4],inf),
	\dur, 1,
	\amp, 0.1
)).play;
);

{ SinOsc.ar(XLine.ar(50,2000,10)) ! 2 }.play
GUI.swing
~sw = SW_Scope.new;
~sw.window.front
