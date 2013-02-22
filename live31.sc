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

(
Mdef.samplekit(\castor, [
	"~/Musique/beast.wav".standardizePath,
	"~/Musique/blabla2.wav".standardizePath,
]);
)

Mdef.main.model.bus_mode_enabled = true

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

Mdef.main.save_project("live31.reg1");
Mdef.main.load_project("live31.reg1");

Mdef.main.save_project("live31.reg2");
Mdef.main.load_project("live31.reg2");

Debug.enableDebug = true;
Debug.enableDebug = false;

s.latency = 3
s.latency = 1.2
s.latency = 1.0
s.latency = 0.2
s.latency = 0.4
s.latency = 0.6
s.latency

Mdef.node(\s1_part1_sect1_var3).children
Mdef.node("ci osc3filter2_l1013").uname
Mdef.node("ci osc3filter2_l1020").modulation.modulation_mixers.keysValuesDo { arg key, val; [key, val.get_slots].debug }
Mdef.node("ci dadsr_kr_l1014")

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

SynthDef(\zegrainer, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, mbufnum,
						gdur=0.2, trate=12, pos=0, rate = 1, durgap, randframes=0.01, randrate=1, doneAction=0;
	var ou;
	var dur, clk;
	var epos;
	var bufnum = mbufnum;
	var randclk;
	dur = gdur;
	//trate = 1/max(0.001,(dur - durgap));
	clk = Impulse.kr(trate);
	randclk = Impulse.kr(randrate * trate);
	//pos = MouseX.kr(0,1);
	//epos = EnvGen.ar(~make_adsr.(\adsr_pos,Env.adsr(0.01,0.1,0.8,0.1)),gate);
	//prate = EnvGen.ar(~make_adsr.(\adsr_prate,Env.adsr(0.01,0.1,0.8,0.1)),gate);
	//pos = (pos+epos) * BufDur.kr(bufnum) + TRand.kr(0, 0.01, clk);
	//pos = epos * BufDur.kr(bufnum) + TRand.kr(0, 0.01, clk);
	//pos = pos * BufDur.kr(bufnum);
	pos = pos * BufDur.kr(bufnum) + TRand.kr(0,randframes,randclk);
	rate = Demand.kr(clk, 0, Dseq([0.5,1,2],inf)) * rate;
	ou = TGrains.ar(2, clk, bufnum, rate, pos, dur, pan, 0.1);
	ou = ou * EnvGen.ar(Env.adsr(0.005,0.1,0.8,0.5),gate,doneAction:doneAction);
	ou = Pan2.ar(ou, pan, amp * 8);
	Out.ar(out, ou);
}, metadata: (specs:(
	gdur: ControlSpec(0.000001,2,\exp,0,0.1),
	randframes: ControlSpec(0.000001,1,\exp,0,0.01),

))).store;
)
(

SynthDef(\sampleosc1, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, bufnum;
	var ou, bufsig;
	bufsig = PlayBuf.ar(2, bufnum, 1/1, 0, 0.1*BufDur.kr(bufnum), loop:1);
	//freq = (freq * bufsig) + (freq/2);
	//ou = SinOsc.ar(freq) * (1+bufsig/2);
	//ou = SinOsc.ar(freq + bufsig, bufsig);
	//PitchShift.ar(bufsig, 0.1, freqatio)
	ou = LFPulse.ar(freq, 0, bufsig mod: 0.1);
	ou = ou + (bufsig*1);
	//ou = (bufsig*4);
	ou = RLPF.ar(ou, bufsig*1*freq+freq*4, 0.1);
	//ou = LFPulse.ar(freq, 0, bufsig mod: 0.1);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;

SynthDef(\sampleosc2, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, bufnum;
	var ou, bufsig;
	var clk;
	var randframes=0.110001;
	var randrate=1;
	var randclk;
	var pos=0.7;
	var mfreq;
	var dur=0.4;
	var rate=(1/11), trate=12;
	clk = Impulse.kr(trate);
	
	randclk = Impulse.kr(randrate * trate);
	//pos = MouseX.kr(0,1);
	//epos = EnvGen.ar(~make_adsr.(\adsr_pos,Env.adsr(0.01,0.1,0.8,0.1)),gate);
	//prate = EnvGen.ar(~make_adsr.(\adsr_prate,Env.adsr(0.01,0.1,0.8,0.1)),gate);
	//pos = (pos+epos) * BufDur.kr(bufnum) + TRand.kr(0, 0.01, clk);
	//pos = epos * BufDur.kr(bufnum) + TRand.kr(0, 0.01, clk);
	//pos = pos * BufDur.kr(bufnum);
	pos = pos * BufDur.kr(bufnum) + TRand.kr(0,randframes,randclk);
	//pos = pos * BufDur.kr(bufnum);
	//rate = Demand.kr(clk, 0, Dseq([0.5,1,2],inf)) * rate;
	//rate = freq.cpsmidi.midiratio * rate;
	bufsig = TGrains.ar(2, clk, bufnum, rate * [1,1.001], pos, dur, pan, 0.1);
	//bufsig = TGrains.ar(2, bufnum, 1/1, 0, 0.1*BufDur.kr(bufnum), loop:1);
	mfreq = (freq * bufsig.clip(-1,1) *9) + freq;
	ou = LFSaw.ar(mfreq * [1,1.01]);
	ou = ou /2;
	//ou = bufsig * 5;
	//ou = SinOsc.ar(freq + bufsig, bufsig);
	//ou = LFPulse.ar(freq, 0, bufsig mod: 0.1);
	//ou = ou + (bufsig*1);
	//ou = (bufsig*4);
	//ou = RLPF.ar(ou, bufsig*1*freq+freq*4, 0.1);
	ou = RLPF.ar(ou, freq*4, 0.3);
	ou = ou.sum;
	//ou = LFPulse.ar(freq, 0, bufsig mod: 0.1);
	ou = ou * EnvGen.ar(Env.adsr(0.4,0.1,0.8,0.4),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;

)
TGrains

(
Pdef(\plop, Pbind(
	\instrument, \sampleosc2,
	\degree, Pseq([0,-2,-4],inf),
	\bufnum, ~b2.bufnum,
	\dur, 1,
	\amp, 0.1
)).play;
);

(
Pdef(\plop, Pbind(
	\instrument, \zegrainer,
	\bufnum, 5,
	\degree, Pseq([0],inf),
	\pos, 0,
	\dur, 4,
	\amp, 0.4
).trace).play;
);

~path = "sounds/a11wlk01.wav";
~path = "/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/kick_Dry_b.flac"
(
var numchan;
numchan = SoundFile.use(~path, { arg f;
	f.numChannels;
});
)

~wb = Buffer.read(s, ~path, 0,-1)
~wb.numChannels


Buffer.
~b = BufferPool.get_forced_stereo_sample(\bla, "/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/kick_Dry_b.flac")
~b2 = BufferPool.get_mono_sample(\bla, "/home/ggz/Musique/beast.wav")
~b2 = BufferPool.get_forced_stereo_sample(\bla, "sounds/a11wlk01.wav")
~b2 = BufferPool.get_mono_sample(\bla, "sounds/a11wlk01.wav")
~b.play
~b2.play

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



(
SoundFile.use("~/Musique/beast.wav".standardizePath, { arg f;
	f.numChannels.debug("numchan");

})
)


{ SinOsc.ar(500) }.play



(

~b = BufferPool.get_forced_stereo_sample(\bla, "/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/kick_Dry_b.flac")
~b2 = BufferPool.get_forced_stereo_sample(\bla, "/home/ggz/Musique/beast.wav")
~b3 = BufferPool.get_forced_stereo_sample(\bla, "sounds/a11wlk01.wav")
)
~b2
~b3
(

 Ndef(\plop, {
 	var bufsig;
	var bufnum = ~b2;
	var phase, phase2;
	var osc;

	osc = SinOsc.ar(MouseX.kr(0.2,500));
	phase = osc * 0.001;
	//osc = phase * osc;
	phase2 = SinOsc.ar(MouseX.kr(0.2,500)) * 0.0001;
	phase = phase + phase2 + MouseY.kr(0.2,0.25);

	bufsig = BufRd.ar(2, bufnum, phase*BufFrames.ir(bufnum), 1);
	bufsig;
	//SinOsc.ar(100)

 }).play


)


(
w = Window.new.front;
a = TextField(w, Rect(10, 10, 150, 20));
a.string = "hi there";
a.action = {arg field; field.value.postln; };

)


~class_ci_osc3filter2.new(Mdef.main, )

Mdef.node("ci osc3filter2_l1005").external_player.get_static_data.keys



