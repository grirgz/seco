
// embedInStream dans un Prout
// steal dubecho ! http://sccode.org/1-h
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
	\modenv,
	\lfo1,
	\lfo_tri,
	\lfo_asr,
	\line1,
	"ci mod_osc",
	"ci mod_envosc",
	"ci dadsr_kr",
	"ci custom_env",
].collect({arg i; i -> i });

~samplelib = [
	"sounds/perc1.wav",
	"sounds/pok1.wav",
	"sounds/amen-break.wav",
	"sounds/default.wav"
];
~seq.load_effectlib( ~effectlib );
~seq.load_modlib( ~modlib );
~seq.set_presetlib_path("mypresets");
~seq.append_samplelib_from_path("sounds/" );
~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );

Mdef.samplekit(\deskkick, 20.collect{arg i; "/home/ggz/Musique/recording" +/+ i ++ ".wav"});
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

Mdef.samplekit(\castor, [
	"~/Musique/beast.wav".standardizePath,
	"~/Musique/blabla2.wav".standardizePath,
]);

}
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

Mdef.main.save_project("madhouse");
Mdef.main.load_project("madhouse");

Mdef.main.save_project("dev1.test1");
Mdef.main.load_project("dev1.test1");

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





(
~build_spread_array = { arg unisono;
	var z, ret;
	var gen_cell = { arg i; 
		var cell;
		cell = (i+1)/z;
		if(i.odd) {
			cell = 0-cell;
		};
		cell;
	};

	if(unisono.asInteger.odd) {
		z = (unisono-1 / 2).asInteger;
		ret = z.collect(gen_cell);
		ret = 0-ret.reverse ++ 0 ++ ret;
	} {
		z = (unisono / 2).asInteger;
		ret = z.collect(gen_cell);
		ret = 0-ret.reverse ++ ret;
	};
};
)

~build_spread_array.(4)
