(
s.waitForBoot{
//"/home/ggz/code/sc/abcparser.sc".load;
//"/home/ggz/code/sc/seco/classinstr.sc".load;
~seq = Mdef.force_init(true);
~synthlib = [
	\audiotrack_expander,
	\osc1,
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
].collect({ arg i; i -> i });

~effectlib = [
	\comb1,
	\p_reverb,
	\p_flanger,
	\p_chorus,
	\p_delay,
	\p_comb,
].collect({arg i; i -> i });

~modlib = [
	\modenv,
	\lfo1,
	\lfo_tri,
	\lfo_asr,
	\line1,
	"ci dadsr_kr",
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
Mdef.main.save_project("live31.tintin");
Mdef.main.load_project("live31.tintin");

Mdef.main.save_project("live31.tintin-pink");
Mdef.main.load_project("live31.tintin-pink");

Mdef.main.save_project("live31.zetest1");
Mdef.main.load_project("live31.zetest1");

Mdef.main.save_project("live31.indus");
Mdef.main.load_project("live31.indus");

Mdef.main.save_project("live31.nimp");
Mdef.main.load_project("live31.nimp");

Mdef.main.save_project("live31.soft");
Mdef.main.load_project("live31.soft");

Mdef.main.save_project("live31.boom");
Mdef.main.load_project("live31.boom");

Mdef.main.save_project("live31.perc");
Mdef.main.load_project("live31.perc");

Mdef.main.save_project("live31.perc2");
Mdef.main.load_project("live31.perc2");

Debug.enableDebug = true;
Debug.enableDebug = false;

s.latency = 3
s.latency = 1.2
s.latency = 1.0
s.latency = 0.2


(
SynthDef(\osc1, { arg out, gate=1, freq=300, amp=0.1, ffreq=200, rq=0.1, attack=0.1, release=0.1, doneAction=2;
	var sig = LFSaw.ar(freq);
	var env = EnvGen.kr(Env.adsr(attack,0.1,1,release), gate, doneAction:doneAction);
	sig = RLPF.ar(sig, ffreq, rq);
	//sig = sig + SinOsc.ar(ffreq);
	//ffreq.poll;
	//rq.poll;
	sig = sig * env;
	sig = sig ! 2;
	sig = sig * amp;
	Out.ar(out, sig);
}).store;

)



(

~g = Group.new;
~g.register;
~g.addDependant({ arg grp, status;
	[grp,status].debug("free");
	if(status == \n_end) {
		grp.releaseDependants;
		debug("release");
	}

});
~g.onFree({ "bla" });
)
~g.free



Mdef.node("ci osc3filter2_l1066").modulation.get_modulators[nil]
Mdef.node("ci osc3filter2_l1066").modulation.get_modulation_mixer(0).get_slots
Mdef.node("ci osc3filter2_l1066").modulation.get_modulation_mixers.keys
Mdef.node("ci osc3filter2_l1066").modulation.get_modulation_mixer(\filter1_arg1).get_slots[0].name
Mdef.node("ci osc3filter2_l1066").modulation.get_modulation_mixer(\ampcomp).get_slots
Mdef.node("ci osc3filter2_l1066").modulation.get_modulation_mixers.keysValuesDo { arg key, val; [key, val.get_slots].debug; }



(
 Task{
 	debug("bla");
	1.wait;
	w=Window.new;
	~s= StaticText.new(w);
	w.front;
	1.wait;
	~s.string = "blaaa";



	debug("rah")

 }.play(AppClock)
)
AppClock
