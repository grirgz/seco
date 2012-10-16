


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

//Debug.enableDebug = false;
Debug.enableDebug = true;
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

Debug.enableDebug = false;
Debug.enableDebug = true;

Mdef.main.save_project("live28");
Mdef.main.load_project("live28");


(
Instr(\wop, { arg amp=0.1, gate=1, spread=1, pan=0, freq=200, modfratio=0.5, fratio=0.5;
	var ou;
	ou = SinOsc.ar(Instr(\oscform).wrap((freq:modfratio*freq))*freq*fratio*[1.6,1,0.99,1.011,1.1]+freq);
	ou = ou * EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:2);
	ou = Splay.ar(ou, spread, amp, pan);
}).addSynthDef;

Instr(\wopi, { arg amp=0.1, gate=1, spread=1, pan=0, freq=200, modfratio=0.5, fratio=0.5, delay=0;
	var ou, ou2;
	ou = SinOsc.ar(Instr(\oscform).wrap((freq:modfratio*freq))*freq*fratio*[1.6,1,0.99,1.011,1.1]+freq);
	ou2 = CombC.ar(ou,0.3,[0.015,0.03,0.0105],0.14);
	ou2 = ou2 * EnvGen.ar(Env.dadsr(0.03,0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = ou + ou2;
	ou = ou * EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:2);
	ou = Splay.ar(ou, spread, amp, pan);
	ou = DelayC.ar(ou, delay, delay);
}).addSynthDef;

Instr(\wopenv, { arg gate=1, freq=200, fmratio=0.5;
	var ou;
	var modfratio, fratio;
	freq = EnvGen.kr(\freq_env.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate) * fmratio * freq + freq;
	modfratio = EnvGen.kr(\modfratio_env.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate);
	fratio = EnvGen.kr(\fratio_env.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate);
	Instr(\wopi).wrap((gate:gate, modfratio:modfratio, fratio:fratio, freq:freq))
}).addSynthDef;
)
~group = Group.new;
~group.deepFree
(
Mdef(\plop0, ~penvcontrol.(Pbind(
	\instrument, \wopenv,
	\freq, Pseq([0,0,1,0,1,0,0,0],inf) + [7, 4] * 10,
	\pfreq_env, Pseq([
		Env.adsr(0.1,0.1,0.05,0.2),
		Env.adsr(0.7,0.1,0.45,0.2),
	],inf),
	\freq_env, Pfunc{arg in; [ in[\pfreq_env] ] },
	\fmratio, Ref(Env([0.6,0.2,0.8,0.1],[0.5,0.1,0.4])),
	\modfratio_env, [Env.adsr(0.7,0.4,0.75,0.2)],
	\fratio_env, [Env.adsr(0.7,0.5,0.75,0.2)],
	\modfratio, Ref(Env([0.6,0.2,0.8,0.1,0.9,0.1],[0.1,0.5,0.4,0.2,0.8])),
	\fratio, 1.7,
	\group, ~group,
	\dur, Pseq([1,1,1,1,1,1,1,1, 1,1,1,1]/4,inf),
	\pamp, Pseq([1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\spread, Pseq([0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\pan, Pseq([0,-0.2,0.91,1,1,1.7,1,0.9,1, 1.1,1,1,1]-1,inf) + (Prand((1,2..100),inf)/500),
	\amp, Pkey(\pamp)*0.1
)));
);


(
Mdef(\plop1, ~penvcontrol.(Pbind(
	\instrument, \wopenv,
	\freq, Pseq([4,2,7,0,1,5,5,8],inf) + [9, 7] * 4,
	\pfreq_env, Pseq([
		Env.adsr(0.4,0.1,0.95,0.2),
		Env.adsr(0.4,0.1,0.05,0.2),
		Env.adsr(0.7,0.1,0.45,0.2),
	],inf),
	\freq_env, Pfunc{arg in; [ in[\pfreq_env] ] },
	\fmratio, Ref(Env([0.6,0.2,0.8,0.1],[0.5,0.1,0.4])),
	\modfratio_env, [Env.adsr(0.1,0.4,0.75,0.2)],
	\fratio_env, [Env.adsr(0.7,0.1,0.75,0.2)],
	\modfratio, Ref(Env([0.1,0.2,0.8,0.1,0.9,0.1],[0.1,0.5,0.4,0.2,0.8])),
	\fratio, 1.7,
	\dur, Pseq([1,1,1,1,1,1,1,1, 1,1,1,1]/4,inf),
	\pamp, Pseq([1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\spread, Pseq([0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\pan, Pseq([0,-0.2,0.91,1,1,1.7,1,0.9,1, 1.1,1,1,1]-1,inf) + (Prand((1,2..100),inf)/500),
	\amp, Pkey(\pamp)*0.1
)));
);

(
Mdef(\plop2, ~penvcontrol.(Pbind(
	\instrument, \wopenv,
	//\stut, Pseq([1,1,1,Prand([1,1,1,4]),1,1,1,Prand([1,4])],inf),
	\stut, 1,
	\freq, Pstutter(Pkey(\stut),
		Pseq([4,2,7,0,1,5,5,8],inf) + [4, 1, -1] * 20 * Pseq([1,Prand([\r,1,1]),1,Prand([\r,1])],inf),
	),
	\pfreq_env, Pseq([
		Env.adsr(0.4,0.1,0.95,0.2),
		Env.adsr(0.4,0.1,0.05,0.2),
		Env.adsr(0.7,0.1,0.45,0.2),
	],inf),
	\freq_env, Pfunc{arg in; [ in[\pfreq_env] ] },
	\fmratio, Ref(Env([0.6,0.2,1.8,0.1,1.2,0.1,1],[0.1,0.1,0.8,0.1,0.9,2])),
	\modfratio_env, [Env.adsr(0.1,0.4,0.75,0.2)],
	\fratio_env, [Env.adsr(0.7,0.4,0.75,0.2)],
	\modfratio, Ref(Env([0.8,0.4,0.8,0.1,0.9,0.1],[0.1,0.5,0.4,0.2,0.8])),
	\fratio, 0.7,
	\dur, Pseq([1,1,1,1,1,1,1,1, 1,1,1,1]/2,inf) / Pkey(\stut),
	\pamp, Pseq([1,1,1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1]/2,inf),
	\spread, Pseq([1,1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\pan, Pseq([0,0.1,0,-0.2,0.91,1,1,1.7,1,0.9,1, 1.1,1,1,1]-1,inf) + (Prand((1,2..100),inf)/500),
	\delay, [0,0.03],
	\amp, Pkey(\pamp)*0.1
)));
);

(
Mdef(\plop3_l1201, ~penvcontrol.(Pbind(
	\instrument, \wopenv,
	//\stut, Pseq([1,1,1,Prand([1,1,1,4]),1,1,1,Prand([1,4])],inf),
	\stut, 1,
	\freq, Pstutter(Pkey(\stut),
		Pseq([4,2,7,0,1,5,5,8],inf) + [4, 1, -1] * 20 * Pseq([1,Prand([\r,1,1]),1,Prand([\r,1])],inf),
	),
	\pfreq_env, Pseq([
		Env.adsr(0.4,0.1,0.95,0.2),
		Env.adsr(0.4,0.1,0.05,0.2),
		Env.adsr(0.7,0.1,0.45,0.2),
	],inf),
	\freq_env, Pfunc{arg in; [ in[\pfreq_env] ] },
	\fmratio, Ref(Env([0.6,0.2,1.8,0.1,1.2,0.1,1],[0.1,0.1,0.8,0.1,0.9,2])),
	\modfratio_env, [Env.adsr(0.1,0.4,0.75,0.2)],
	\fratio_env, [Env.adsr(0.7,0.4,0.75,0.2)],
	\modfratio, Ref(Env([0.8,0.4,0.8,0.1,0.9,0.1],[0.1,0.5,0.4,0.2,0.8])),
	\fratio, 0.7,
	\legato, Pseg(Pseq([1,0],inf),7.4),
	\dur, Pseq([1,1,1,1,1,1,1,1, 1,1,1,1]/2,inf) / Pkey(\stut),
	\pamp, Pseq([1,1,1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1]/2,inf),
	\spread, Pseq([1,1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\pan, Pseq([0,0.1,0,-0.2,0.91,1,1,1.7,1,0.9,1, 1.1,1,1,1]-1,inf) + (Prand((1,2..100),inf)/500),
	\delay, [0,0.03],
	\amp, Pkey(\pamp)*0.1
)));
);

(
Mdef(\moo, ~penvcontrol.(Pbind(
	\instrument, \wopenv,
	//\stut, Pseq([1,1,1,Prand([1,1,1,4]),1,1,1,Prand([1,4])],inf),
	\stut, 1,
	\freq, Pstutter(Pkey(\stut),
		Pseq([14,2,7,10,1,15,7,8],inf) + [4, 1, -1] * 10 * Pseq([1,Prand([\r,1,1]),1,Prand([\r,1])],inf),
	),
	\pfreq_env, Pseq([
		Env.adsr(0.4,0.1,0.95,0.2),
		Env.adsr(0.4,0.1,0.05,0.2),
		Env.adsr(0.7,0.1,0.45,0.2),
	],inf),
	\freq_env, Pfunc{arg in; [ in[\pfreq_env] ] },
	\fmratio, Ref(Env([0.6,0.2,1.8,0.1,1.2,0.1,1],[0.1,0.1,0.8,0.1,0.9,2])),
	\modfratio_env, [Env.adsr(0.1,0.4,0.75,0.2)],
	\fratio_env, [Env.adsr(0.7,0.4,0.75,0.2)],
	\modfratio, Ref(Env([0.1,0.4,0.8,0.1,0.9,0.1],[0.1,0.5,0.4,0.2,0.8])),
	\fratio, 0.7,
	\legato, Pseg(Pseq([1,0],inf),7.4),
	\dur, Pseq([1,1,1,1,1,1,1,1, 1,1,1,1]/4,inf) / Pkey(\stut),
	\pamp, Pseq([1,1,1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1]/2,inf),
	\spread, Pseq([1,1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\pan, Pseq([0,0.1,0,-0.2,0.91,1,1,1.7,1,0.9,1, 1.1,1,1,1]-1,inf) + (Prand((1,2..100),inf)/500),
	\delay, [0,0.03],
	\amp, Pkey(\pamp)*0.1
)));
);

(
Mdef(\moo2, ~penvcontrol.(Pbind(
	\instrument, \wopenv,
	//\stut, Pseq([1,1,1,Prand([1,1,1,4]),1,1,1,Prand([1,4])],inf),
	\stut, 1,
	\freq, Pstutter(Pkey(\stut),
		Pseq([14,2,7,10,1,15,7,8],inf) + [4, 1, -1] * 10 * Pseq([1,Prand([\r,1,1]),1,Prand([\r,1])],inf),
	),
	\pfreq_env, Pseq([
		Env.adsr(0.4,0.1,0.95,0.2),
		Env.adsr(0.4,0.1,0.05,0.2),
		Env.adsr(0.7,0.1,0.45,0.2),
	],inf),
	\freq_env, Pfunc{arg in; [ in[\pfreq_env] ] },
	\fmratio, Ref(Env([1.6,0.2,0.8,0.4,1.2,0.1,1],[0.4,0.1,0.5,0.1,0.9,2])),
	\modfratio_env, [Env.adsr(0.1,0.4,0.75,0.2)],
	\fratio_env, [Env.adsr(0.7,0.4,0.75,0.2)],
	\modfratio, Ref(Env([0.1,0.4,0.8,0.1,0.9,0.1],[0.1,0.5,0.4,0.2,0.8])),
	\fratio, Ptuple([Prand([0.7,1]),Prand([1,7])],inf),
	\legato, Pseg(Pseq([1,0],inf),7.4),
	\dur, Pseq([1,1,1,1,1,1,1,1, 1,1,1,1]/4,inf) / Pkey(\stut),
	\pamp, Pseq([1,1,1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1]/2,inf),
	\spread, Pseq([1,1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\pan, Pseq([0,0.1,0,-0.2,0.91,1,1,1.7,1,0.9,1, 1.1,1,1,1]-1,inf) + (Prand((1,2..100),inf)/500),
	\delay, [0,0.03],
	\amp, Pkey(\pamp)*0.1
)));
);






(
Mdef(\plop0, Pbind(
	\instrument, \wopenv,
	\freq, Pseq([0,0,1,0,1,0,0,0],inf) + [7, 4] * 10,
	\pfreq_env, Pseq([
		Env.adsr(0.1,0.1,0.05,0.2),
		Env.adsr(0.7,0.1,0.45,0.2),
	],inf),
	\freq_env, Pfunc{arg in; [ in[\pfreq_env] ] },
	\fmratio, 0.6,
	\modfratio_env, [Env.adsr(0.7,0.4,0.75,0.2)],
	\fratio_env, [Env.adsr(0.7,0.5,0.75,0.2)],
	\modfratio, 0.6,
	\fratio, 1.7,
	\group, ~group,
	\dur, Pseq([1,1,1,1,1,1,1,1, 1,1,1,1]/4,inf),
	\pamp, Pseq([1,0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\spread, Pseq([0.91,1,1,1,1,0.9,1, 1.1,1,1,1],inf),
	\pan, Pseq([0,-0.2,0.91,1,1,1.7,1,0.9,1, 1.1,1,1,1]-1,inf) + (Prand((1,2..100),inf)/500),
	\amp, Pkey(\pamp)*0.1
));
);
