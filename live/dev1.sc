
// embedInStream dans un Prout
(
s.waitForBoot{
//"/home/ggz/code/sc/abcparser.sc".load;
//"/home/ggz/code/sc/seco/classinstr.sc".load;
Window.closeAll;
~seq = Mdef.force_init(true);
~synthlib = [
	\seqnode,
	\parnode,
	\audiotrack_expander,
	\monosampler,
	\stereosampler,
	\ss_comb,
	\ss_combfreq,

	"ci op_matrix2",
	"ci mosc",
	"ci moscfilter",
	"ci moscfilter_modfx",
	"ci osc3filter2",
	"ci bufosc_filt",
	"ci bufosc_filt_spread",
	"ci samplerfilter2",

	\bufsin1,
	\zegrainer,
	\sampleosc1,
	\sampleosc2,

	\osc1,
	\guitar,
	\guitar2,
	\ch,

	\kick1,
	\kick2,
	\kick3,
	\kicklank,
].collect({ arg i; i -> i });
~seq.load_patlib( ~synthlib );


~effectlib = [
	\comb1,
	\p_reverb,
	\p_flanger,
	\p_chorus,
	\p_delay,
	\p_comb,
	"ci insertfx3",
	\dubecho,
].collect({arg i; i -> i });

~modlib = [
	\setbus,
	\modenv,
	\gater,
	\gated_asr,
	\lfo1,
	\lfo_tri,
	\lfo_asr,
	\line1,
	"ci mod_osc",
	"ci mod_envosc",
	"ci dadsr_kr",
	"ci custom_env",
].collect({arg i; i -> i });

//~samplelib = [
//	"sounds/perc1.wav",
//	"sounds/pok1.wav",
//	"sounds/amen-break.wav",
//	"sounds/default.wav"
//];
~seq.load_effectlib( ~effectlib );
~seq.load_modlib( ~modlib );
~seq.set_presetlib_path("mypresets");
//~seq.append_samplelib_from_path("sounds/" );
//~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
//~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );

//Mdef.samplekit(\deskkick, 20.collect{arg i; "/home/ggz/Musique/recording" +/+ i ++ ".wav"});
//Mdef.main.model.bus_mode_enabled = false;

Mdef.side_gui;



~tf = Pfunc({ arg ev; if(ev[\stepline] == 1) { \note } { \rest } });
~ff = Pfunc({ arg ev; if(ev[\stepline1] == 1) { 1 } { \rest } });

//Debug.enableDebug = false;
~windowize = { arg layout;
	var win;
	win = Window.new;
	win.layout = layout;
	win.front;
};

//Mdef.samplekit(\castor, [
//	"~/Musique/beast.wav".standardizePath,
//	"~/Musique/blabla2.wav".standardizePath,
//]);

Mdef.main.samplekit_manager.parse_samplekit_dir;
Mdef.main.samplekit_manager.parse_samplekit_dir("~/Musique/hydrogenkits/");

}
)


Platform.resourceDir

Set.newFrom(["bla", "bla"])

["bla", "rah"].includesEqual("bla")
["bla", "rah"].drop(-1)

(
var myPath;
myPath = PathName.new("~/Musique/samplekit/");
"ob".debug;
myPath.folders.do { arg dir;
	var samplekit_name;
	var samples = List.new;
	"iob".debug;
	//dir.postln;
	samplekit_name = dir.folderName;
	samplekit_name.debug("gueule");
	dir.files.do { arg file;
		samples.add( file.fullPath )
	};
	samples.debug("samples");
	Mdef.samplekit(samplekit_name.asSymbol, samples);
	///dir.
};
)

(
~parse_samplekit_dir = { arg samplekit_dir;
	var path;
	samplekit_dir = samplekit_dir ?? "~/Musique/samplekit/";
	path = PathName.new(samplekit_dir);
	path.folders.do { arg dir;
		var samplekit_name;
		var samples = List.new;
		samplekit_name = dir.folderName;
		dir.files.do { arg file;
			samples.add( file.fullPath )
		};
		Mdef.samplekit(samplekit_name.asSymbol, samples);
	};
};
)


(
)

Mdef.main.model.bus_mode_enabled = true
Mdef.main.model.bus_mode_enabled = false
Mdef.main.model.bus_mode_enabled


Mdef.main.save_project("dev1");
Mdef.main.load_project("dev1");

Mdef.main.save_project("books");
Mdef.main.load_project("books");

Mdef.main.save_project("dev1.test1");
Mdef.main.load_project("dev1.test1");

Mdef.main.save_project("testxruns");
Mdef.main.load_project("testxruns");

Mdef.main.save_project("noxruns");
Mdef.main.load_project("noxruns");

Mdef.main.save_project("happypunk");
Mdef.main.load_project("happypunk");

Mdef.main.play_manager

Debug.enableDebug = true;
Debug.enableDebug = false;

s.latency = 3
s.latency = 1.2
s.latency = 1.0
s.latency = 0.2
s.latency = 0.4
s.latency = 0.6
s.latency

{ SinOsc.ar(100) }.play



Mdef.sample(\)

Mdef.node("setbus_l1011").get_arg(\scoreline).get_scoreset.get_notescore.notes
Mdef.node("osc1_l1073").get_macro_args
Mdef.node("osc1_l1073")

(
SynthDef(\plop, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, tsustain, t_trig=1;
	var ou;
	ou = SinOsc.ar(freq);
	//tsustain.poll;
	//Trig.kr(t_trig,tsustain);
	ou = ou * EnvGen.ar(Env.linen(0.4,tsustain,0.4),t_trig,doneAction:0);
	ou.poll;
	ou = ou * EnvGen.ar(Env.adsr(0.4,0.1,0.8,0.4),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)


(
 Pmono(\plop,
 	\freq, Pseq([100,200],4),
	\dur, 2,
	\legato, 0.1,
	\tsustain, Pkey(\sustain) / Ptempo(),
 ).trace.play

)
TempoClock.default.tempo = 2


Mdef.node_by_index(0).build_real_sourcepat

Mdef.main.commands.commands.keys


(

~resp = Mdef.main.commands.make_binding_responder(\bla, 
	[\test, {
		"TEST".postln;

	} ]

);

~kbresp = ~resp.get_kb_responder(\bla)
~resp
)

