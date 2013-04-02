


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
~ff = Pfunc({ arg ev; if(ev[\stepline1] == 1) { 1 } { \rest } });

//Debug.enableDebug = false;
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
			\dur, 0.25,
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
Mdef.main.save_project("live22");
Mdef.main.load_project("live22");

Debug.enableDebug = true;
Debug.enableDebug = false;

(
Pdef(\perc1_kick4, Pbind(
	\speed, 0.5,

));

)

///// test perc
~test_perc = { arg name; Pbind(\instrument, \stereosampler, \bufnum, Mdef.dsample(name), \dur, Pn(1,1), \amp, 1).play };
~test_perc.(\kick0);
~test_perc.(\kick1);
~test_perc.(\kick2);
~test_perc.(\kick3);
~test_perc.(\kick4);
~test_perc.(\kick5);
~test_perc.(\kick6);
~test_perc.(\kick7);
~test_perc.(\kick8);
~test_perc.(\kick9);
~test_perc.(\kick10);
~test_perc.(\kick11);
~test_perc.(\kick12);
~test_perc.(\kick13);
~test_perc.(\kick14);
~test_perc.(\kick15);
~test_perc.(\kick16);
~test_perc.(\kick17);
~test_perc.(\kick18);
~test_perc.(\kick19);

/////


(
~make_perc.(\perc1,
	(
		kick4: Pseq([
			1,0,1,0, 0,0,1,0,
			1,0,1,0, 0,0,0,0
		]),
		kick7: Pseq([
			0,0,0,0, 1,0,0,1, 
			0,1,0,0, 1,0,0,0
		]),
		kick0: Pseq([0,0,0,0, 0,0,0,1]),
		//kick10: Pseq([1,0,1,0, 1,0,1,0]),
		//kick12: Pseq([0,1,1,0, 0,0,1,1]),
		//kick14: Pseq([0,1,0,1, 0,1,0,1]),
		//kick15: Pseq([1,0,1, 0,1,0,1,0]),
		//kick16: Pseq([1,0,1,0, 1,0,1,0]),
		kick13: Pseq([0,1,1,0, 1,1,0,0]),
		//kick2: Pseq([0,0,1,1, 0,1,0,0]),
		//stick1: Pseq([1,0,0,0, 1,0,0,0])
	)
)
)
(
~make_perc.(\perc1,
	(
		kick7: Pseq([1,0,1,0, 1,0,1,0]),
		kick4: Pseq([
			0,0,0,0, 0,1,0,0, 
			0,1,0,0, 0,1,0,1
		]),
		kick0: Pseq([0,0,0,0, 0,0,0,1]),
		//kick10: Pseq([1,0,1,0, 1,0,1,0]),
		//kick12: Pseq([0,1,1,0, 0,0,1,1]),
		//kick14: Pseq([0,1,0,1, 0,1,0,1]),
		//kick15: Pseq([1,0,1, 0,1,0,1,0]),
		kick16: Pseq([1,0,1,0, 1,0,1,0]),
		kick13: Pseq([0,1,0,1, 1,0,1,0]),
		//kick2: Pseq([0,0,1,1, 0,1,0,0]),
		stick1: Pseq([1,0,0,0, 1,0,0,0])
	)
)
)
(
~make_perc.(\perc2,
	(
		kick7: Pseq([1,0,0,0, 1,0,0,0]),
		kick4: Pseq([
			0,1,0,0, 0,1,1,0, 
			1,0,1,0, 1,0,1,1
		]),
		kick0: Pseq([0,0,0,0, 0,0,0,1]),
		//kick10: Pseq([1,0,1,0, 1,0,1,0]),
		//kick12: Pseq([0,1,1,0, 0,0,1,1]),
		//kick14: Pseq([0,1,0,1, 0,1,0,1]),
		//kick15: Pseq([1,0,1, 0,1,0,1,0]),
		kick16: Pseq([1,0,1,0, 1,0,1,0]),
		kick13: Pseq([0,1,0,1, 1,0,1,0]),
		//kick2: Pseq([0,0,1,1, 0,1,0,0]),
		stick1: Pseq([1,0,0,0, 1,0,0,0])
	)
)
)

(
Mdef(\percs, Ppar([
	Pdef(\perc1_line),
	Pdef(\perc2_line),
]));
);
Mdef.show(\percs)

~abcpbind.("abc")

(
Mdef(\bla1, Pbind(
	\instrument, \lead2,
	\octave, 3,

) <> ~abcpbind.("cef_ facc cef_ fdcc"),\lead2);
);
(
Mdef(\bla2, Pbind(
	\instrument, \lead2,
	\octave, 5,
	\dur, 0.125,

) <> ~abcpbind.("cecf cfce fefc fcec"),\lead2);
);

	Mdef(\bla2, Pbind(\instrument, \lead2));
(
15.do { arg x;
	Mdef(\bla ++ x, Pbind(\instrument, \lead2));
}
)
Mdef(\group, Ppar([Pdef(\bla1),Pdef(\bla2)]))
Mdef.show(\group)




(
// example synthdef
SynthDef(\lead2, {	arg out=0, freq = 100, pan=0, amp=0.1, mdetune=1.004, gate=1, rq=0.1, fratio = 1, fbase=400, wet=1, fbfreq=100, fbamp=0.8, fbpamp=1, rt=0.4; 
	var fb, ou, filtenv;
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
)

(
// event morphing
~morph = { arg ev_start, ev_end, time, repeat=1;
	var ev_res;

	if(ev_start.isArray) { ev_start = Event.newFrom(ev_start) };
	if(ev_end.isArray) { ev_end = Event.newFrom(ev_end) };

	ev_res = ();
	ev_start.keys.do { arg key;
		ev_res[key] = Pseg(Pseq([ev_start[key], ev_end[key]],repeat), time);
	};
	Pbind(*ev_res.asKeyValuePairs);
};

// pattern morphing
~morphpat = { arg pat1, pat2, mpat;
	var spat1, spat2, smpat;
	var ev1, ev2, morph;
	var res_ev;
	var rout;
	var exclu = Set[\dur, \instrument];
	spat1 = pat1.asStream;
	spat2 = pat2.asStream;
	smpat = mpat.asStream;
	rout = Routine {
		block { arg break;
			loop {
				ev1 = spat1.next(Event.default);
				ev2 = spat2.next(Event.default);
				morph = smpat.next;
				if(ev1.isNil or: { ev2.isNil or: { morph.isNil }}) { 
					break.value;
				} {
					res_ev = ();
					ev1.keys.difference(exclu).do { arg key;
						res_ev[key] = (ev1[key] * morph) + (ev2[key] * (1 - morph));
					};
					res_ev.debug("res_ev");
					res_ev.yield;
				}
			}
		}
	};
	rout
};

)

// we want to morph from this pattern
(
Pdef(\plop, Pbind(
	\instrument, \lead2,
	\degree, Pseq([0,2,4],inf),
	\fratio, 9,
	\sustain, 0.1,
	\wet, 0.8,
	\fbfreq, 4100,
	\fbpamp, 4,
	\rq, 0.2,
	\dur, 0.25,
	\amp, 0.1
)).play;
);

// to this pattern
(
Pdef(\plop, Pbind(
	\instrument, \lead2,
	\degree, Pseq([0,2,4],inf),
	\fratio, 2.4,
	\sustain, 0.21,
	\wet, 0.1,
	\fbfreq, 4180,
	\fbpamp, 1,
	\rq, 2.01,
	\dur, 0.25,
	\amp, 0.2
)).play;
);


(
// event morphing
Pdef(\plop, Pbind(
	\instrument, \lead2,
	\degree, Pseq([0,2,4,Prand([0,2,4,7,9,-5,-3])],inf),
	\mtranspose, Pstep(Pseq([0,3,1,4, 0,2,1,3],inf),2),
	//\mtranspose, Pstep(Pseq([0,2,1,3],inf),4),
	\dur, 0.25,
	\sustain, Pkey(\sustain) * 4,
	\amp, 0.1
) <> ~morph.([ // work also with event notation
	\fratio, 2.4,
	\sustain, 0.21,
	\wet, 0.1,
	\fbfreq, 180,
	\fbpamp, 1,
	\rq, 2.01,
], [
	\fratio, 9,
	\sustain, 0.1,
	\wet, 0.8,
	\fbfreq, 4800,
	\fbpamp, 4,
	\rq, 0.2,
], 10,inf)).play;
)


(
// pattern morphing
~pat1 = Pbind(
	\fratio, Pseq([10,2.4],inf),
	\sustain, 0.21,
	\wet, 0.1,
	\fbfreq, Pseq([4000,180],inf),
	\fbpamp, 1,
	\rq, 2.01,
);

~pat2 = Pbind(
	\fratio, 9,
	\sustain, 0.1,
	\wet, 0.8,
	\fbfreq, Pseq([200,4800],inf),
	\fbpamp, 4,
	\rq, 0.2,
);
p = ~morphpat.(~pat1, ~pat2, Pseg(Pseq([0, 1],inf),10)); // third argument: 0 = first pattern, 1 = second pattern
(Pbind(
	\instrument,\lead2,
	\degree, Pseq([0,2,4,Prand([0,2,4,7,9,-5,-3])],inf),
	\mtranspose, Pstep(Pseq([0,3,1,4, 0,2,1,3],inf),2),
	\sustain, Pkey(\sustain) * 4,
	\dur, 0.25,
) <> p).trace.play;
)



Pbind(*(degree: Pseq([0,2,4],inf)).asKeyValuePairs).play
(degree: Pseq([0,2,4],inf)).play
p
(degree: Pseq([0,2,4],inf)).asKeyValuePairs
