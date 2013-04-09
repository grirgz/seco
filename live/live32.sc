
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
	\osc1,
	\guitar,
	\guitar2,
	\ch,
	\creature,
	"ci oscmaster",
	"ci op_matrix",
	"ci mosc",
	"ci moscfilter",
	"ci moscfilter_modfx",
	"ci moscfaderfilter",
	"ci osc3filter2",
	"ci sin",
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
	\zegrainer,

	\kick1,
	\kick2,
	\kick3,
	\kicklank,
].collect({ arg i; i -> i });

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
	"ci dadsr_kr",
	"ci custom_env",
].collect({arg i; i -> i });

~samplelib = [
	"sounds/perc1.wav",
	"sounds/pok1.wav",
	"sounds/amen-break.wav",
	"sounds/default.wav"
];
~seq.load_patlib( ~synthlib );
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


}
)

(
Mdef.samplekit(\castor, [
	"~/Musique/beast.wav".standardizePath,
	"~/Musique/blabla2.wav".standardizePath,
]);
)

Mdef.main.model.bus_mode_enabled = true
Mdef.main.model.bus_mode_enabled = false
Mdef.main.model.bus_mode_enabled


Debug.enableDebug = true;
Debug.enableDebug = false;

s.latency = 3
s.latency = 1.2
s.latency = 1.0
s.latency = 0.2
s.latency = 0.4
s.latency = 0.6
s.latency

Mdef.main.model.latency = 0.1



(
Ndef(\plop, { arg freq=200, pan=0, amp=0.1;
	var ou, freqs;
	var mod, car=200;
	var modosc;
	freqs = Array.fill(10, { arg i;
		(i+1) + (LFNoise0.ar(0.1).range(1,2))
	});
	mod = LFTri.kr(0.01).range(0.01,0.5);

	modosc = LFTri.ar(freqs/50);
	ou = modosc*mod*car+car;
	ou = LFSaw.ar(ou) * modosc;
	ou = Splay.ar(ou, 1, amp);
}).play;
);
