

(
s.waitForBoot{
//~seq = Mdef.force_init(true);
//"~/code/sc/passive/main.scd".standardizePath.load;
~seq = Mdef.force_init(true);
~synthlib = [
	\audiotrack_expander,
	"passive default",
	"passive alien indus",
	"passive lancinant",
	\lead2,
	\lead3,
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
//Mdef.sampledict((
//		kick0: "0.wav",
//		kick1: "1.wav",
//		kick2: "2.wav",
//		kick3: "3.wav",
//		kick4: "4.wav",
//		kick5: "5.wav",
//		kick6: "6.wav",
//		kick7: "7.wav",
//		kick8: "8.wav",
//		kick9: "9.wav",
//		kick10: "10.wav",
//		kick11: "11.wav",
//		kick12: "12.wav",
//		kick13: "13.wav",
//		kick14: "14.wav",
//		kick15: "15.wav",
//		kick16: "16.wav",
//		kick17: "17.wav",
//		kick18: "18.wav",
//		kick19: "19.wav",
//	),
//	"/home/ggz/Musique/recording"
//);

//Mdef.main.model.latency = 0

//Mdef.samplekit(\deskkick, 20.collect{arg i; "/home/ggz/Musique/recording" +/+ i ++ ".wav"});
//Mdef.main.samplekit_manager.get_samplekit_bank.keys

//~make_perc = { arg prefix, score, base_prefix=nil;
//	var ppar = List.new;
//	var key_list = List.new;
//	if(base_prefix.isNil) { base_prefix = prefix };
//	Mdef.main.node_manager.freeze_gui(true);
//	score.keys.do { arg key;
//
//
//		var name = (prefix++ "_" ++ key).asSymbol;
//		var base_name = (base_prefix++ "_" ++ key).asSymbol;
//		var scorepat = Pbind(
//			\instrument, \stereosampler,
//			\bufnum, score[key].collect({ arg x; if(x > 0) { Mdef.dsample(key) } { Rest() } }),
//			\dur, 0.125,
//			\amp, 1.0
//		);
//		var pat = if(Pdef(base_name).source.notNil) { Pdef(base_name) <> scorepat } { scorepat };
//		ppar.add(pat);
//		Mdef(name++"_score", pat, \stereosampler);
//		key_list.add(name++"_score");
//	};
//	Mdef(prefix++"_line", Ppar(key_list.collect({ arg x; Pdef(x) })));
//	Mdef.main.node_manager.freeze_gui(false);
//	Mdef.show(prefix++"_line");
//	ppar;
//};

}
)

Mdef.main.model.latency = 0.1;
Debug.enableDebug = false;
Debug.enableDebug = true;



Mdef.main.save_project("live30");
Mdef.main.load_project("live30");

Mdef.main

(
if(~p.notNil) {~p.external_player.destructor; };
~p = ~class_passive_player.new(Mdef.main);
~p.data[\dur].set_val(2);
)
~p
~class_passive_controller


~p

~p.uname = "plop"
~p.vpattern.play
~p.external_player.make_gui
~p.external_player.rebu
~p.external_player.destructor
~p.data[\dur].get_val

Bus.audio(s,1)


Mdef.node("passive default_l1008").vpattern.play
Mdef.node("passive default_l1008").build_sourcepat
Mdef.node("passive default_l1008").external_player.make_gui
Mdef.node("passive default_l1002").external_player.make_gui
Mdef.node("passive default_l1002").external_player.synthdef_name.dump
Mdef.node("passive default_l1002").get_arg(\instrument)
