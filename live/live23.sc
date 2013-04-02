



s.meter
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
Mdef.main.save_project("live23");
Mdef.main.load_project("live23");

Debug.enableDebug = true;
Debug.enableDebug = false;

(
Pdef(\perc1_kick4, Pbind(
	\speed, 0.5,

));

)
(

SynthDef(\ChicagoPad2, { |out = 0, freq = 440, freq2=660, freq3=528, cutoff = 500, amp = 0.2, gate=1|



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



}).store;

SynthDef(\ricky1, { |out = 0, freq = 440, freq2=660, freq3=528, cutoff = 500, amp = 0.2, gate=1|



	var snd;



	freq = freq + SinOsc.kr(freq*1/2, 0, freq/2, 0);



	snd = Saw.ar([freq, freq+1, freq-1, freq2, freq3] * [0.99,1])*0.1;



	snd = snd + VarSaw.ar(0.99*[freq, freq+1, freq-1, freq2, freq3, freq2+1, freq3+1],0, LFTri.kr(7.13).range(0.00,0.1))*0.1;





	snd = Mix(snd);

	//snd = FreeVerb.ar(snd, 0.51,10,0.1)*2;

	//snd = snd *8;

	//snd = RLPF.ar(snd, SinOsc.kr([0.1,4.2], 0, [1700,480], [4000,700,5000])/[20.51,20], SinOsc.ar(0.1,1.5*pi)+1.05)/3;

	snd = Mix(snd);



	snd = MoogFF.ar(snd, SinOsc.kr(0.08, 0.5, cutoff/10, cutoff), 3, 0);



	snd = snd * EnvGen.ar(Env.adsr(0.4, 0.01,0.8,0.4), gate, doneAction:2);



	snd = [snd , AllpassC.ar(snd, 0.45, 0.05+SinOsc.ar(0.11).range(0,0.045), 0.3)];

	snd = snd * amp;

	Out.ar(out, snd);



}).store;
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
			//1,1,1,1, 1,1,1,1,
			1,0,0,0, 0,0,0,0,
		]),
		kick7: Pseq([
			0,0,1,0, 1,0,0,0, 
			0,0,0,0, 1,1,0,0, 
			0,0,0,0, 0,0,0,0, 
			1,0,1,0, 0,0,0,0, 
		]),
		kick0: Pseq([0,1,0,0, 0,0,0,0]),
		//kick10: Pseq([1,0,1,0, 1,0,1,0]),
		//kick12: Pseq([0,1,1,0, 0,0,1,1]),
		//kick14: Pseq([0,1,0,1, 0,1,0,1]),
		//kick15: Pseq([1,0,1, 0,1,0,1,0]),
		//kick16: Pseq([1,0,1,0, 1,0,1,0]),
		kick13: Pseq([1,0,0,1, 1,0,0,1]),
		//kick2: Pseq([0,0,1,1, 0,1,0,0]),
		//stick1: Pseq([1,0,0,0, 1,0,0,0])
	)
)
);

(
~make_perc.(\perc1,
	(
		kick4: Pseq([
			//1,1,1,1, 1,1,1,1,
			0,0,0,0, 1,0,1,0,
			0,0,0,0, 0,0,0,0,
		])*1,
		kick12: Pseq([
			0,0,0,0, 0,0,0,0,
			0,0,0,0, 1,0,1,0,
		])*1,
		kick8: Pseq([
			0,0,0,1, 0,0,1,0
		])*1,
		kick7: Pseq([
			1,0,1,0, 0,1,0,0, 
			0,1,0,0, 0,1,0,1, 
		]) * 1,
		kick0: Pseq([1,0,1,0, 0,0,0,0])*1,
		//kick10: Pseq([1,0,1,0, 1,0,1,0]),
		//kick12: Pseq([0,1,1,0, 0,0,1,1]),
		//kick14: Pseq([0,1,0,1, 0,1,0,1]),
		//kick15: Pseq([1,0,1, 0,1,0,1,0]),
		//kick16: Pseq([1,0,1,0, 1,0,1,0]),
		kick13: Pseq([
			1,0,0,0, 0,0,0,1,
			1,0,0,1, 0,1,0,1,
		])*0,
		//stick1: Pseq([1,0,0,0, 1,0,0,0])
	)
)
);

(

Mdef(\chipad, Pbind(

	\instrument, \ChicagoPad2,

	\degree, Pseq([0,8,2],inf),

	\root, -4,

	\freq2, Pkey(\freq) / 2 * Pseq([3/2, 2/7],inf),

	\freq3, Pkey(\freq) / 2 * Pseq([5/6, 4/5, 5/6],inf),

	\legato, 1,
	\mylegato, 1,

	\cutoff, Pseg(Pseq([10000,0100],inf),10),

	\dur, 4,

	\amp, 0.2

));

);

(
Mdef(\ricky1, Pbind(

	\instrument, \ricky1,

	\degree, Pstep(Pseq([0,8,2],inf),4) + Pseq([0,2,4,\r,7,6,5,4],inf),

	\root, -4,

	\octave, 7,

	\freq2, Pkey(\freq) + 4,

	\freq3, Pkey(\freq) * 2 - 4,

	\legato, 1,
	\mylegato, 1,

	\cutoff, Pseg(Pseq([10000,0100],inf),10),

	\dur, 1/4,

	\amp, 0.2

));
);

(
Mdef(\group, Ppar([
	Pdef("perc1_line"),
	Pdef(\chipad),
	Pdef(\ricky1),
]))
);

Mdef.show(\group)
