
(
s.waitForBoot{
//~seq = Mdef.force_init(true);
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
~ff = Pfunc({ arg ev; if(ev[\stepline1] == 1) { 1 } { \rest } });

Debug.enableDebug = false;
Mdef.sampledict((
		kick0: "0.wav",
		kick1: "1.wav",
		kick2: "2.wav",
		kick3: "3.wav",
		kick4: "4.wav",
		kick5: "5.wav",
		kick6: "6.wav",
		kick7: "7.wav",
		kick8: "8.wav",
		kick9: "9.wav",
		kick10: "10.wav",
		kick11: "11.wav",
		kick12: "12.wav",
		kick13: "13.wav",
		kick14: "14.wav",
		kick15: "15.wav",
		kick16: "16.wav",
		kick17: "17.wav",
		kick18: "18.wav",
		kick19: "19.wav",
	),
	"/home/ggz/Musique/recording"
);

Mdef.samplekit(\deskkick, 20.collect{arg i; "/home/ggz/Musique/recording" +/+ i ++ ".wav"});
//Mdef.main.samplekit_manager.get_samplekit_bank.keys

~make_perc = { arg prefix, score, base_prefix=nil;
	var ppar = List.new;
	var key_list = List.new;
	if(base_prefix.isNil) { base_prefix = prefix };
	Mdef.main.node_manager.freeze_gui(true);
	score.keys.do { arg key;


		var name = (prefix++ "_" ++ key).asSymbol;
		var base_name = (base_prefix++ "_" ++ key).asSymbol;
		var scorepat = Pbind(
			\instrument, \stereosampler,
			\bufnum, score[key].collect({ arg x; if(x > 0) { Mdef.dsample(key) } { Rest() } }),
			\dur, 0.125,
			\amp, 1.0
		);
		var pat = if(Pdef(base_name).source.notNil) { Pdef(base_name) <> scorepat } { scorepat };
		ppar.add(pat);
		Mdef(name++"_score", pat, \stereosampler);
		key_list.add(name++"_score");
	};
	Mdef(prefix++"_line", Ppar(key_list.collect({ arg x; Pdef(x) })));
	Mdef.main.node_manager.freeze_gui(false);
	Mdef.show(prefix++"_line");
	ppar;
};

}
)


Mdef.main.save_project("live26");
Mdef.main.load_project("live26");







( 
SynthDef(\trianglewavebells,{|out= 0 freq = 440 amp = 0.1 gate=1 lforate = 10 lfowidth= 0.0 cutoff= 100 rq=0.5 pan=0.0
			wet=0.8|  
	 
	var osc1, osc2, vibrato, filter, env;  
	var ou;
		 
	vibrato = SinOsc.ar(lforate,Rand(0,2.0));  
		 
	osc1 = Saw.ar(vibrato*0.7*freq+freq); 
		 
	//Saw a bit rough, possibly slighter smoother: 
	//osc1 = DPW4Saw.ar(freq*(1.0+(lfowidth*vibrato)),0.5); 
		 
	//osc2 = Mix(LFTri.ar((freq.cpsmidi+[11.9,12.1]).midicps));  
		 
	//filter = (osc1+(osc2*0.5))*0.5; //no filter version 
	filter = RHPF.ar(osc1,cutoff,rq); 	 
	ou = filter;
	//ou = CombC.ar(ou, 0.4, 0.4, 1.8);
	//ou = CombC.ar(ou, 0.4, 0.04, 1.8);
	ou = FreeVerb.ar(ou, wet, 0.9, 0.1);


	
		 
	env = EnvGen.ar(Env.adsr(0.01,0.1,1.0,0.5),gate,doneAction:2);  
		 
	Out.ar(out,Pan2.ar(ou*env*amp,pan));  
		 
}).add;  

Instr(\noisy, { arg amp=0.1, gate=1, pan=0, lfreq=200, hfreq=600, timeScale=1;
	var ou;
	ou = WhiteNoise.ar(1);
	ou = BPF.ar(ou, lfreq);
	ou = LPF.ar(ou, hfreq);
	ou = ou * EnvGen.ar(\env.kr(Env([0,0.5,0.8,0.2,0],[10,5,7,5].normalizeSum)),gate,timeScale:timeScale, doneAction:2);
	ou = (ou * 70).distort / 2;
	ou = Pan2.ar(ou, pan, amp);
	//Out.ar(out, ou);
}).addSynthDef;

Instr(\noisenv, { arg gate=1, timeScale=1;
	var ou;
	var lfreq = EnvGen.ar(\lfreqenv.kr(Env([100,300,500,0,0,0],[0.2,0.3,0,0,0].normalizeSum)), gate, timeScale:timeScale);
	var hfreq = EnvGen.ar(\hfreqenv.kr(Env([100,300,500,0,0,0]+400,[0.2,0.3,0,0,0].normalizeSum)), gate, timeScale:timeScale);
	Instr(\noisy).wrap((lfreq:lfreq, hfreq:hfreq, gate:gate, timeScale:timeScale));
}).addSynthDef;

Instr(\noisenvrlpf, { arg freq=400, rq=0.5, gate=1, timeScale=1;
	var ou;
	ou = Instr(\noisenv).wrap((gate:gate, timeScale:timeScale));
	ou = RLPF.ar(ou, freq, rq);
}).addSynthDef;

Instr(\noisenvrlpfenv, { arg gate=1, timeScale=1;
	var ou;
	var freq = EnvGen.ar(\freqenv.kr(Env([100,300,500,0,0,0],[0.2,0.3,0,0,0].normalizeSum)), gate, timeScale:timeScale);
	var rq = EnvGen.ar(\rqenv.kr(Env([100,300,500,0,0,0]+400,[0.2,0.3,0,0,0].normalizeSum)), gate, timeScale:timeScale);
	ou = Instr(\noisenvrlpf).wrap((gate:gate, timeScale:timeScale, freq:freq, rq:rq));
}).addSynthDef;

Instr(\noisenvrlpfenv3, { arg gate=1, timeScale=1;
	var ou;
	var freq = EnvGen.ar(\freqenv.kr(Env([100,300,500,0,0,0],[0.2,0.3,0,0,0].normalizeSum)), gate, timeScale:timeScale);
	var rq = EnvGen.ar(\rqenv.kr(Env([100,300,500,0,0,0]+400,[0.2,0.3,0,0,0].normalizeSum)), gate, timeScale:timeScale);
	ou = Instr(\noisenv).wrap((gate:gate, timeScale:timeScale));
	ou = Instr(\noisenvrlpf).wrap((gate:gate, timeScale:timeScale, freq:freq, rq:rq));
}).addSynthDef;

SynthDef(\noisyperc, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, decay=0.41,
		lfreq0=30, hfreq0=170, amp0=1, decay0=0.29,
		lfreq1=100, hfreq1=400, amp1=0.5, decay1=0.11,
		lfreq2=2000, hfreq2=2200, amp2=1, decay2=0.9,
		lfreq3=5800, hfreq3=10400, amp3=0.3, decay3=0.7
		;
	var ou, ou0, ou1, ou2, ou3, ou4;
	ou = WhiteNoise.ar(1);

	//ou0 = SinOsc.ar([40,40.1,30,80,20.1,50,60,70,55,130,130.01]).sum;
	//ou0 = SinOsc.ar(\freq0.kr([80,70.01,60])).sum;
	ou0 = SinOsc.ar([80,70.01,60]).sum;
	//ou0 = DelayC.ar(ou3, 0.1,0.1) + ou3;
	ou0 = HPF.ar(ou0, 30);
	ou0 = LPF.ar(ou0, 174);
	ou0 = ou0 * amp0;
	ou0 = ou0 * EnvGen.ar(Env.perc(0.00001,decay0),gate,doneAction:0);

	ou1 = HPF.ar(ou, lfreq1);
	ou1 = LPF.ar(ou1, hfreq1);
	ou1 = (ou1 * 70).distort * amp1;
	ou1 = ou1 * EnvGen.ar(Env.perc(0.001,decay1),gate,doneAction:0);

	ou2 = HPF.ar(ou, lfreq2);
	ou2 = LPF.ar(ou2, hfreq2);
	ou2 = ou2 * amp2;
	ou2 = ou2 * EnvGen.ar(\env2.kr(Env([0,0,0.5,0.3,0],[0.01,0.001,0.2,0.1])),gate,timeScale:decay2,doneAction:0);


	ou3 = HPF.ar(ou, lfreq3);
	ou3 = LPF.ar(ou3, hfreq3);
	ou3 = (ou3 * 70).distort * amp3;
	ou3 = ou3 * EnvGen.ar(\env3.kr(Env([0,0,0.5,0.1,0],[0.01,0.01,0.2,0.6])),gate,timeScale:decay3,doneAction:0);
	//ou3 = ou3 * EnvGen.ar(Env.perc(0.001,0.80),gate,doneAction:0);


	ou = 
		 ou0
		+ ou1 
		+ ou2 
		+ ou3 
		;
	//ou = ou1 + ou4 + ou3;
	ou = ou * EnvGen.ar(Env.perc(0.001,decay),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)

(
Pdef(\bruit, Pbind(
	\instrument, \noisy,
	\degree, Pseq([0],inf),
	\env, [Env([0,0.5,0.8,0.2,0],[10,5,7,5])],
	\lfreq, 100,
	\hfreq, 400,
	\sustain, 14,
	\dur, Pn(28,1),
	\amp, 0.4
)).play;
);

(
Pdef(\bruit2, Pbind(
	\instrument, \noisy,
	\env, [Env([0,0.5,0.8,0.2,0],[10,5,7,5].normalizeSum)],
	\lfreq, Pseq([100,700, 3000],inf),
	\hfreq, Pseq([400,1000, 5000],inf),
	\sustain, 1,
	\timeScale, Pkey(\sustain),
	\dur, Pn(2,inf),
	\amp, 0.4
)).play;
);

(
Pdef(\bruiteur, Pbind(
	\instrument, \noisenv,
	\env, [Env([0,0.5,0.8,0.2,0],[5,5,7,5].normalizeSum)],
	\lfreqenv, [Env([200,5000,100,1000,200],[1,1,5,2].normalizeSum)],
	\hfreqenv, [Env([600,10000,300,1100,17000],[1,1,5,2].normalizeSum)],
	//\sustain, 1/2,
	\legato, 1,
	\timeScale, Pkey(\sustain),
	\dur, Pn(4,inf),
	\amp, 0.4
)).play;
);

(
Pdef(\bruiteur, Pbind(
	\instrument, \noisenv,
	\env, [Env([0,0.5,0.8,0.2,0],[5,5,7,5].normalizeSum)],
	\lfreqenv, [Env([200,500,500,500],[1,1,1].normalizeSum)],
	\hfreqenv, [Env([600,20000,2000,600],[1,1,1].normalizeSum)],
	//\sustain, 1/2,
	\legato, 1,
	\timeScale, Pkey(\sustain),
	\dur, Pn(4,inf),
	\amp, 0.4
)).play;
);

(
Pdef(\bruiteur, Pbind(
	\instrument, \noisenvrlpf,
	\env, [Env([0,0.5,0.8,0.2,0],[5,5,7,5].normalizeSum)],
	\lfreqenv, [Env([200,500,500,500],[1,1,1].normalizeSum)],
	\hfreqenv, [Env([600,20000,2000,600],[1,1,1].normalizeSum)],
	//\sustain, 1/2,
	\legato, 1,
	\rq, 0.2,
	\freq, 200,
	\timeScale, Pkey(\sustain),
	\dur, Pn(4,inf),
	\amp, 0.4
)).play;
);

(
Pdef(\bruiteur, Pbind(
	\instrument, \noisenvrlpfenv,
	\env, [Env([0,0.5,0.8,0.2,0],[5,5,7,5].normalizeSum)],
	\lfreqenv, [Env([200,500,500,500],[1,1,1].normalizeSum)],
	\hfreqenv, [Env([600,20000,2000,600],[1,1,1].normalizeSum)],
	\freqenv, [Env([100,7000,2200,1000],[1,1,1].normalizeSum)],
	\rqenv, [Env([0.2,2,0.05,0.5],[1,1,1].normalizeSum)],
	//\sustain, 1/2,
	\legato, 1,
	\rq, 0.2,
	\freq, 200,
	\timeScale, Pkey(\sustain),
	\dur, Pn(4,inf),
	\amp, 0.4
)).play;
);
 
( 
Pdef(\oiseau, Pbind( 
	\instrument, \trianglewavebells, 
	\sustain,0.01, 
	\amp, 0.2, 
	//\midinote,Prand([0,7,3,0, 8,7,8,5, 7,12,5,3, 12,7,15,-5]+40,inf) + Prand([10,1,5,-10,20],inf), 
	\note,Prand([0,2b,4],inf)+Prand([-12,0,0,12],inf), 
	\root, Pstep([0,1],2,inf),
	\octave, 4,
	//\wet, Pseg(Pseq([0,0.5],inf),3) + Pfunc{ 0.5.rand },
	\lfowidth, 0.0010, 
	\lforate, 00.01, 
	\wet, 0.1,
	\rq, 0.7, 
	\cutoff,Pn(Pseries(60,10,9),inf),
	\dur, 0.5,
)).play 
) 
 
( 
Pdef(\plop, Pbind( 
	\instrument, \trianglewavebells, 
	\sustain,0.1, 
	\amp, 0.2, 
	//\midinote,Prand([0,7,3,0, 8,7,8,5, 7,12,5,3, 12,7,15,-5]+40,inf) + Prand([10,1,5,-10,20],inf), 
	\note,Prand([0,2b,4],inf), 
	\root, Pstep([0,1],2,inf),
	//\wet, Pseg(Pseq([0,0.5],inf),3) + Pfunc{ 0.5.rand },
	\lfowidth, 0.010, 
	\lforate, 1, 
	\rq, 0.1, 
	\cutoff,Pn(Pseries(60,10,9),inf),
	\dur, Pfunc { 1.rand  } + Prand([0.1,0.2,0.14]/2,inf),
)).play 
) 

( 
Pdef(\plop2, Pbind( 
	\instrument, \trianglewavebells, 
	\sustain,2.1, 
	\amp, 0.2, 
	//\midinote,Prand([0,7,3,0, 8,7,8,5, 7,12,5,3, 12,7,15,-5]+40,inf) + Prand([10,1,5,-10,20],inf), 
	\note,Prand([0,2b,4],inf), 
	\root, Pstep([0,1],2,inf),
	\octave, 4,
	//\wet, Pseg(Pseq([0,0.5],inf),3) + Pfunc{ 0.5.rand },
	\lfowidth, 0.010, 
	\lforate, 1, 
	\rq, 0.1, 
	\cutoff,Pn(Pseries(60,10,9),inf),
	\dur, Pfunc { 1.rand + 2 },
)).play 
) 

~fio =  { arg tin, tout, pat; Pfindur(tin+tout, Pmul(\amp, Pseg(Pseq([0,1,0],1),Pseq([tin,tout]),Pseq([1,-1])), pat)) };
(
Pdef(\maj, Ptpar([
	0, ~fio.(10,10, Pdef(\plop)),
	0, ~fio.(10,10, Pdef(\kick)),
	0, ~fio.(10,10, Pdef(\kick2)),
	7, ~fio.(15,15, Pdef(\oiseau)),
	14, ~fio.(10,10, Pdef(\plop2)),
	12, ~fio.(10,10, Pdef(\kick2)),
	17, ~fio.(10,10, Pdef(\kick4)),
	25, Pdef(\bruit),
])
).play
)
 

(
Mdef(\bruitperc, Pbind(
	\instrument, \noisyperc,
	\degree, Pseq([0],inf),
	//\lfreq1, 100,
	//\hfreq1, 400,
	//\decay1, 0.2,
	//\amp1, 0.3,

	\lfreq2, 2000,
	\hfreq2, 2200,
	\decay2, 0.9,
	\amp2, 0.8,

	\lfreq3, 5000,
	\hfreq3, 11000,
	\decay3, 0.7,
	\amp3, 0.2,

	\decay, 0.41,

	//\stepline, Pseq([1,0,0,0, 1,1,0,1],inf),
	//\type, ~tf,

	\dur, 1/8,
	\amp, 0.4
)).play;
);


(
Mdef(\kick, Pbind(
	\instrument, \stereosampler,
	\bufnum, Mdef.dsample(\kick1),
	//\stepline, Pseq([1,0,0,0,1,0,0,0],inf),
	//\type, ~tf,
	\dur, 0.125,
	\amp, 1.0
)).play;
);

(
Mdef(\kick2, Pbind(
	\instrument, \stereosampler,
	\bufnum, Mdef.dsample(\kick8),
	//\stepline, Pseq([0,1,0,1, 0,1,0,1],inf),
	//\type, ~tf,
	\dur, 0.1250,
	\amp, 1.0
)).play;
);

(
Mdef(\kick3, Pbind(
	\instrument, \stereosampler,
	\bufnum, Mdef.dsample(\kick4),
	//\stepline, Pseq([1,1,0,1, 0,1,1,0],inf),
	//\type, ~tf,
	\dur, 0.1250,
	\amp, 1.0
)).play;
);

(
Mdef(\kick4, Pbind(
	\instrument, \stereosampler,
	\bufnum, Mdef.dsample(\kick6),
	//\stepline, Pseq([1,0,0,0, 1,1,0,0],inf),
	//\type, ~tf,
	\dur, 0.1250,
	\amp, 1.0
)).play;
);


Mdef.showdef(\cpar4)

