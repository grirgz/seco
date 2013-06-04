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

	\zegrainer,
	\sampleosc1,
	\sampleosc2,
	//"ci oscmaster",
	//"ci op_matrix",
	//"ci moscfaderfilter",
	//"ci sin",
	//\lead2,
	//\pulsepass,
	//\flute1,
	//\miaou1,
	//\ringbpf1,
	//\piano2,
	//\pmosc,

	\osc1,
	\guitar,
	\guitar2,
	\ch,

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

Mdef.main.save_project("live31.dark");
Mdef.main.load_project("live31.dark");

Mdef.main.save_project("live31.jeudi");
Mdef.main.load_project("live31.jeudi");

Mdef.main.save_project("live31.lugubre");
Mdef.main.load_project("live31.lugubre");

Mdef.main.save_project("live31.zouk");
Mdef.main.load_project("live31.zouk");

Mdef.main.save_project("live31.mytest6");
Mdef.main.load_project("live31.mytest6");

Mdef.main.save_project("live31.zouk2");
Mdef.main.load_project("live31.zouk2");

Mdef.main.save_project("live31.drum1");
Mdef.main.load_project("live31.drum1");

Mdef.main.save_project("live31.drum2");
Mdef.main.load_project("live31.drum2");

Mdef.main.save_project("live31.dub2");
Mdef.main.load_project("live31.dub2");

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
"~/code/sc/seco/nodematrix.sc".standardizePath.load;
~nmp = ~class_nodematrix_panel.new(Mdef.main);
~w = ~nmp.make_gui;
~nmp.set_parent_node(Mdef.node(\s1_part1_sect1_var1))

)

{ SoundIn.ar([0,1]) }.play

(
~nmp = ~class_nodematrix_panel.new(Mdef.main);
~w = ~nmp.make_gui;
~nmp.set_parent_node(Mdef.node(\s1_part1_sect1_var1))

)
Mdef.node(\s1_part1_sect1_var1)
Mdef.node("ci dadsr_kr_l1073").get_arg(\attack_time).get_val
Mdef.node_by_index(0).get_arg(\noteline).get_scoreset.current_sheet
Mdef.node_by_index(0).get_arg(\).get_scoreset.current_sheet
~a = Mdef.node("ci custom_env_l1029").save_data
~a.args.keys
~b = Mdef.node_by_index(1).save_data
~b.args.keys

Mdef.main.model.livenodepool.keys

~b = Bus.new(\control, 756, 1);
~b.get({arg val; val.debug("val")})

~a = Mdef.node("ci custom_env_l1033").external_player.build_synthdef
~a = Mdef.node("ci custom_env_l1033").external_player.synthdef_name
~a = Mdef.node("ci custom_env_l1033")[\uname]

(
	~midinote_to_notename = { arg midinote;
		var notenames = [ "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" ];
		var octave = ((midinote / 12)).asInteger;
		var name = notenames[ midinote % 12 ];
		name ++ octave.asString;
		//midinote.asString;
	
	}
)
~midinote_to_notename.(64)
Mdef.node(\s1_part1_sect1_var3).children
Mdef.node("ci osc3filter2_l1013").uname
Mdef.node("ci osc3filter2_l1020").modulation.modulation_mixers.keysValuesDo { arg key, val; [key, val.get_slots].debug }
Mdef.node("ci dadsr_kr_l1014")
Mdef.main.panels.side.select_slot(1)
Mdef.main.panels.side.reload_selected_slot
Mdef.main.freeze_do { Mdef.main.panels.side.changed(\player) }
Mdef.node("osc1_l1031").changed()

(

SynthDef(\zegrainer_old, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, mbufnum,
						gdur=0.2, trate=12, grate=1, pos=0, rate = 1, durgap, randframes=0.01, randrate=1, doneAction=0, finepos=0;
	var ou;
	var dur, clk;
	var epos;
	var bufnum = mbufnum;
	var randclk;
	var pitch;
	var sr, phasor;
	dur = 1/trate;
	//trate = 1/max(0.001,(dur - durgap));
	clk = Impulse.kr(trate);
	randclk = Impulse.kr(randrate * trate);
	//pos = MouseX.kr(0,1);
	//epos = EnvGen.ar(~make_adsr.(\adsr_pos,Env.adsr(0.01,0.1,0.8,0.1)),gate);
	//prate = EnvGen.ar(~make_adsr.(\adsr_prate,Env.adsr(0.01,0.1,0.8,0.1)),gate);
	//pos = (pos+epos) * BufDur.kr(bufnum) + TRand.kr(0, 0.01, clk);
	//pos = epos * BufDur.kr(bufnum) + TRand.kr(0, 0.01, clk);
	//pos = pos * BufDur.kr(bufnum);
	//pos = pos + finepos;
	//pos = pos * BufDur.kr(bufnum) + TRand.kr(0,randframes,randclk);
	//rate = Demand.kr(clk, 0, Dseq([0.5,1,2],inf)) * rate;
	//rate = Demand.kr(clk, 0, Dseq([0.5,1,2],inf)) * rate;
	//dur = dur+durgap;
	dur = 12/trate;
	pan = WhiteNoise.kr(0.6);
	sr = SampleRate.ir;
	//(rate.sign + 1 / 2).poll;
	//rate.abs.poll;
	phasor = Select.ar(rate.sign + 1 /2, [
		pos - Phasor.ar(0, rate.abs / sr / BufDur.kr(bufnum), 0, 1),
		Phasor.ar(0, rate.abs / sr / BufDur.kr(bufnum), 0, 1)+pos,
	]);
	//phasor = pos - Phasor.ar(0, rate.abs / sr / BufDur.kr(bufnum), 0, 1);
	//phasor = Phasor.ar(0, rate.abs / sr / BufDur.kr(bufnum), 0, 1);
	phasor.poll;
	//pos = phasor + pos;
	//pos.poll;
	pos = phasor;
	pos = pos * BufDur.kr(bufnum) + finepos + TRand.kr(0, 0.01, clk);
	pos.poll;
	//ou = TGrains.ar(2, clk, bufnum, 1, pos % BufDur.kr(bufnum), dur, pan, 1);
	ou = TGrains.ar(2, clk, bufnum, grate, pos.clip(0, BufDur.kr(bufnum)), dur, pan, 1);
	ou = ou * EnvGen.ar(Env.adsr(0.005,0.1,0.8,0.5),gate,doneAction:doneAction);
	//ou = Pan2.ar(ou, pan, amp * 8);
	//pitch = Tartini.kr(ou);
	//pitch = Pitch.kr(ou);
	//pitch.poll;
	ou = Splay.ar(ou, 1, amp);
	Out.ar(out, ou);
}, metadata: (specs:(
	gdur: ControlSpec(0.000001,2,\exp,0,0.1),
	finepos: ControlSpec(-0.3,0.3,\lin, 0, 0),
	durgap: ControlSpec(-0.5,0.5,\lin,0,0),
	randframes: ControlSpec(0.000001,1,\exp,0,0.01),
	rate: ControlSpec(-8,8,\lin,0,0),
	grate: ControlSpec(-8,8,\lin,0,0),

))).store;
)

(
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

SynthDef(\sampleosc2, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, bufnum, pos=0.7, finepos=0;
	var ou, bufsig;
	var clk;
	var randframes=0.110001;
	var randrate=1;
	var randclk;
	//var pos=0.7;
	var mfreq;
	var dur=0.4;
	var rate=(1/11), trate=12;

	pos = pos+finepos;

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
}, metadata:(specs:(
	finepos: ControlSpec(-0.01,0.01,\lin, 0, 1),
))).store;

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


(
~pc = {"bla".postln;};
CmdPeriod.add(~pc);
CmdPeriod.remove(~pc);
)


(
var bla = "blo";
var bli = { bla.postln; };
bli.value;
bla = "rah";
bli.value;


)


BufferPool.get_forced_stereo_sample(\samplekit, "/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/kick_Dry_b.flac")


Mdef.node_by_index(0).modulation.get_modulators.dump
Mdef.node("lfo1_l1014")
Mdef.node_by_index(0).modulation.get_modulator_name_from_source_slot(0)
Mdef.node_by_index(0).modulation.get_modulator_name(0).dump

Mdef.node_by_index(0).external_player.master.dadsr.dump
Mdef.node_by_index(1).external_player.master.dadsr.data[\attack_time].get_val
Mdef.node_by_index(0).external_player.master.dadsr.data[\attack_time].get_val
Mdef.node_by_index(0).data[\attack_time].get_val
Mdef.node_by_index(1).data[\attack_time].get_val



Instance of Dictionary {    (0x1ecba1f0, gc=E4, fmt=00, flg=00, set=03)
  instance variables [2]
    array : instance of Array (0x14fc5690, size=32, set=5)
    size : Integer 1
}
Dictionary[ (0 -> lfo1_l1014) ]



(
 Task({

"bla".debug;
0.2.wait;
"bila".debug;
 }).play;

)



~a = EventPatternProxy.new;

(

~a.source = Pbind(
	\instrument, \osc1,
	\freq, Pseq([200,150],inf),
);


)
	~a.play
	~a.stop
	~a.resume

	~c = TempoClock.new
	~a.clock = ~c
	~c.tempo = 4
	TempoClock.default.tempo=4
	TempoClock.default.tempo
	TempoClock.default=~c
	TempoClock.beats
	TempoClock.beats = 1
	
Mdef.main.samplekit_manager.slot_to_bufnum(0)
~b = 
~b2 = BufferPool.get_forced_stereo_sample(\bla, "sounds/a11wlk01.wav")

(
w = Window.new("soundfile test", Rect(200, 300, 740, 100));
a = SoundFileView.new(w, Rect(20,20, 700, 60));

f = SoundFile.new;
f.openRead(Platform.resourceDir +/+ "sounds/a11wlk01.wav");
a.setBackground = Color(0, 0, 0.2, 0.9);
a.waveColors = Color(0, 0, 0.2, 0.9) ! 4;
//f.inspect;

a.soundfile = f;
a.read(0, f.numFrames);
a.elasticMode = true;

a.timeCursorOn = true;
a.timeCursorColor = Color.red;
a.timeCursorPosition = 2050;
a.drawsWaveForm = true;
a.gridOn = true;
a.gridResolution = 0.2;

w.front;
)
(
w = Window.new("soundfile test", Rect(200, 300, 740, 100));
a = SoundFileView.new(w, Rect(20,20, 700, 60));

f = SoundFile.new;
f.openRead(Platform.resourceDir +/+ "sounds/a11wlk01.wav");
f.inspect;

a.soundfile = f;
a.read(0, f.numFrames);
a.elasticMode = true;

a.timeCursorOn = true;
a.timeCursorColor = Color.red;
a.timeCursorPosition = 2050;
a.drawsWaveForm = true;
a.gridOn = true;
a.gridResolution = 0.2;

w.front;
)


~keycode.actions
Mdef.main.commands.actions.at(*[\nodematrix, \select_row])

(
		Mdef.main.commands.copy_action_list(\nodematrix, \midi, [
			\select_row,
			\select_column,
		]);
)


(
SynthDef(\dubecho,{|out=0, in=0, length = 1, fb = 0.8, sep = 0.012, mix=0.5, hpfreq=400, lpfreq=5000, noisefreq=12, delayfac=0,
		offset=0, rotate=0, shift=0|
	var input = In.ar(in, 2);
	var output;
	//length = length.lag(0.01);
	//length = LPF.kr(length, 10);
	fb = LPF.kr(fb, 1);
	sep = LPF.kr(fb, 1);
	delayfac = LPF.kr(fb, 1);
	output = input + Fb({

		arg feedback; // this will contain the delayed output from the Fb unit

		var left,right;
		var magic;
		feedback = Limiter.ar(feedback, 1);
		magic = LeakDC.ar(feedback*fb + input);
		magic = HPF.ar(magic, hpfreq); // filter's on the feedback path
		magic = LPF.ar(magic, lpfreq);
		magic = magic.tanh; // and some more non-linearity in the form of distortion
		//#left, right = magic; // let's have named variables for the left and right channels
		magic = FreqShift.ar(magic, [0-shift,shift]);
		#left, right = magic; 
		#left, right = Rotate2.ar(left, right, rotate); 
		magic = [
			DelayC.ar(left, 1, 
				(LFNoise2.ar(noisefreq).range(delayfac*sep,sep)+offset).clip(0,1)
			), 
			DelayC.ar(right, 1, 
				(LFNoise2.ar(noisefreq).range(sep,sep*delayfac)-offset).clip(0,1)
			)
		]; // In addition to the main delay handled by the feedback quark, this adds separately modulated delays to the left and right channels, which with a small "sep" value creates a bit of spatialization

	},length);
	output = SelectX.ar(mix,[input, output]);
	//output = Rotate2.ar(output[0], output[1], rotate); 
	Out.ar(out, output);
}, metadata: (
	specs: (
		sep: ControlSpec.new(0.0001,1, \exp, 0, 0),
		fb: ControlSpec.new(0.0001,2, \lin, 0, 0),
		delayfac: ControlSpec.new(0,0.9999, \lin, 0, 0),
		offset: ControlSpec.new(-0.1,0.1, \lin, 0, 0),
		shift: ControlSpec.new(-1000,1000, \lin, 0, 0),
		rotate: \bipolar.asSpec,
	)
)).store;
)
