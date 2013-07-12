MIDIClient.initialized
// embedInStream dans un Prout
(
s.waitForBoot{
//"/home/ggz/code/sc/abcparser.sc".load;
//"/home/ggz/code/sc/seco/classinstr.sc".load;
Window.closeAll;
~seq = Mdef.force_init(true);
~seq.init_midi;
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

	//"ci inlinefx",
	"ci inlinegen",
	"ci inline_genfx",

	\bufsin1,
	\zegrainer,
	\sampleosc1,
	\sampleosc2,

	\osc1,
	\guitar,
	\guitar2,
	\ch,
	\membraneHex,

	\kick1,
	\kick2,
	\kick3,
	\kicklank,
].collect({ arg i; i -> i });
~seq.load_patlib( ~synthlib );


~effectlib = [
	\comb1,
	//\freeverb,
	\p_reverb,
	\p_flanger,
	\p_chorus,
	\p_delay,
	\p_comb,
	"ci insertfx3",
	\dubecho,
	\dubecho_inmix,
	\dubecho_orig,
	\limiter,
	\bufstut,
	\multitap8,
	\ir_reverb,
].collect({arg i; i -> i });
~seq.load_effectlib( ~effectlib );

~modlib = [
	\setbus,
	\modenv,
	\gater,
	\gated_asr,
	\lfo1,
	\lfo_tri,
	\lfo_asr,
	\line1,
	\varline1,
	"ci mod_osc",
	"ci mod_envosc",
	"ci dadsr_kr",
	"ci custom_env",
	"ci selfgated_env",
].collect({arg i; i -> i });
~seq.load_modlib( ~modlib );

~inlinegenlib = [
	\empty,
	"ci ingen_osc",
	"ci sin",
];
~seq.load_inlinegenlib( ~inlinegenlib );

~inlinefxlib = [
	\empty,
	"ci infx_filter",
];
~seq.load_inlinefxlib( ~inlinefxlib );


//~samplelib = [
//	"sounds/perc1.wav",
//	"sounds/pok1.wav",
//	"sounds/amen-break.wav",
//	"sounds/default.wav"
//];
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
Mdef.main.samplekit_manager.parse_samplekit_dir(~seco_root_path +/+ "hydrogenkits/");

}
)
~seco_root_path +/+ "hydrogenkits/"
SynthDescLib.global.synthDescs[\gater]
SynthDescLib.global.synthDescs['s_ci selfgated_env_l1036']
SynthDescLib.global.synthDescs[\rah]
"~/code/sc/seco/classinstr.sc".standardizePath.load
"~/code/sc/seco/synthpool.sc".standardizePath.load
0.04*32
1/32

(
SynthDef(\rah, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	var trig;
	trig = \bla.tr(1);
	ou = SinOsc.ar(freq);
	ou = ou * EnvGen.ar(Env.linen(0.1,1,0.1),trig,doneAction:0);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)

a  = Synth(\rah)
a.set(\bla, 1)

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

Mdef.main.save_project("acidbass");
Mdef.main.load_project("acidbass");

Mdef.main.save_project("dubpourri");
Mdef.main.load_project("dubpourri");

Mdef.main.save_project("dubpourri2");
Mdef.main.load_project("dubpourri2");

Mdef.main.save_project("noisy");
Mdef.main.load_project("noisy");

Mdef.main.save_project("reggaeton_drum");
Mdef.main.load_project("reggaeton_drum");

Mdef.main.save_project("reggae_pourri");
Mdef.main.load_project("reggae_pourri");

Mdef.main.save_project("truc");
Mdef.main.load_project("truc");

Mdef.main.save_project("bizzare_1");
Mdef.main.load_project("bizzare_1");

Mdef.main.save_project("bizzare_2");
Mdef.main.load_project("bizzare_2");

Mdef.main.save_project("bizzare_3");
Mdef.main.load_project("bizzare_3");

Mdef.main.save_project("rampant");
Mdef.main.load_project("rampant");

Mdef.main.save_project("rampant2");
Mdef.main.load_project("rampant2");

Mdef.main.save_project("cuisine");
Mdef.main.load_project("cuisine");

Mdef.main.save_project("bizzare_3_line");
Mdef.main.load_project("bizzare_3_line");

Mdef.main.save_project("prog1");
Mdef.main.load_project("prog1");

Mdef.main.save_project("testperf");
Mdef.main.load_project("testperf");

Mdef.main.save_project("prog2");
Mdef.main.load_project("prog2");

Mdef.main.save_project("profondeur");
Mdef.main.load_project("profondeur");

Mdef.main.save_project("hurlement");
Mdef.main.load_project("hurlement");


Mdef.main.save_project("testmove");
Mdef.main.load_project("testmove");

Mdef.main.save_project("sousmarin");
Mdef.main.load_project("sousmarin");

Mdef.main.save_project("guitare1");
Mdef.main.load_project("guitare1");

Mdef.main.save_project("Bprog1");
Mdef.main.load_project("Bprog1");

Mdef.main.save_project("Bprog2");
Mdef.main.load_project("Bprog2");

Mdef.main.save_project("space1");
Mdef.main.load_project("space1");

Mdef.main.play_manager

Debug.enableDebug = true;
Debug.enableDebug = false;

"bla".findReplace("niark", "blu")

s.latency = 3
s.latency = 1.2
s.latency = 1.0
s.latency = 0.2
s.latency = 0.4
s.latency = 0.6

BufRd
{ BufRd.ar(1, 0, SinOsc.ar(1) * BufFrames.ir(0)) }.play;

s.latency

"/home/ggz/Musique/sc/vipere/ventregris.wav".pathExists

{ SinOsc.ar(100) }.play


Mdef.node("setbus_l1003")

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
Mdef.node_by_index(0).wrapper
Pwhite

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



a = ~make_seqplayer.(Mdef.main)
a.get_displayable_args
a.update_ordered_args




(
 a = (
 	bla: 3,
	rah: 8,
	arr: [1,2,3],
	cho: \bla,
	gett: { arg self; self[self.cho].debug("result") },

	 );

 b = (
 	parent: a,
	cho: \rah,
 
	 )

)



a.gett
b.gett
b.bla = 2

a.rah = 7

b.arr[0]= 100
a

(
SynthDef(\freeverb, { arg out=0, in, mix=0.5, gate=1, room=0.5, damp=0;
	var sig;
	in = In.ar(in, 2);
	sig = FreeVerb.ar(in, mix, room, damp);
	out.poll;
	Out.ar(out, sig);
}).store;

SynthDef(\freeverb2, { arg out=0, in, mix=0.5, gate=1, room=0.5, damp=0;
	var sig;
	in = In.ar(in, 2);
	sig = FreeVerb.ar(in, mix, room, damp);
	out.poll;
	Out.ar(out, sig);
}).store;
)



(
Pmono(\freeverb2,

	\dur, Pn(1,2)
).play

)




"~/code/sc/seco/synthpool.sc".standardizePath.load
"~/code/sc/seco/classinstr.sc".standardizePath.load






~b1 = Bus.audio(s,2);
~b2 = Bus.audio(s,2);
(
~fxgroup = Group.new(s);
~addaction = 1;
~freeverb = Pmono(\freeverb,
	\in, ~b1,
	\mix, 0.5,
	\addAction, ~addaction,
	\group, ~fxgroup,
	\room, 0.5,
	\dur, Pn(0.25,8),
	\out, ~b2,
);
~freeverb2 = Pmono(\freeverb2,
	\in, ~b2,
	\mix, 0.5,
	\group, ~fxgroup,
	\addAction, ~addaction,
	\room, 0.5,
	\dur, Pn(0.25,8),
	\out, 0,
);

~pat = Pbind(
	\dur, Pn(0.25,8),
	\out, ~b1,
);

//~mainpat = Pspawner({ arg spawner;
//	spawner.par(~pat);
//	[~freeverb, ~freeverb2].do { arg pat;
//		spawner.par(pat)
//	};
//
//});

~mainpat = Ppar([
	~pat,
	~freeverb2,
	~freeverb,
]);

//~pat.play;
//~freeverb.play;
//~freeverb2.play;
Pn(~mainpat,8).play



) 



MIDI

~seq.panels.side.timeline.changed(\blocks)
~seq.panels.side.timeline.timeline_view.timeline.createNode(30,30)
~seq.panels.side.timeline.timeline_view.timeline


(
~tl = ParaTimeline.new;
~tl.userView.background = Color.red;
~win = Window.new;
~win.layout = VLayout.new(~tl.userView);
~win.front;



)
~tl.createNode(30,30);

{ SoundIn.ar([0,1]) }.play


<<<<<<< HEAD
(
Instr(\ci_noise, { arg kind, amp=0.1;
	//TODO
	var sig;
	sig = switch(kind,
		\white, {
			WhiteNoise.ar(amp);
		},
		\pink, {
			PinkNoise.ar(amp);
		},
		\brown, {
			BrownNoise.ar(amp);
		},
		\gray, {
			GrayNoise.ar(amp);
		},
		\clip, {
			ClipNoise.ar(amp);
		},
		{
			//kind.debug("p_noise: ERROR: noise kind not found");
			WhiteNoise.ar(amp);
		}
	);
	sig;

}, [NonControlSpec()]);
)


(
Instr(\blai, {
	
	Instr(\ci_noise).value((kind:\white))

}).asSynthDef(\bli)
)

Patch(\blai).play

Synth(\bli)
=======
"/home/ggz/.vimrc".pathExists.dump
>>>>>>> f8955caae5e52a8b910dfbd3898470b976933e73

PathName("./l/home").isRelativePath
PathName("/home/ggz/Musique").asRelativePath("/hoime").dump
root: /home/tytel/Musique/sc/
in: /home/tytel/Musique/sc/hydrogenkits/YamahaVintageKit/Szl_Cym_02.flac
out: ../hydrogenkits/YamahaVintageKit/Szl_Cym_02.flac
PathName("/home/tytel/Musique/sc/hydrogenkits/YamahaVintageKit/Szl_Cym_02.flac").asRelativePath("/home/tytel/Musique/sc");

o = Server.default.options;
o.memSize 
o.numPrivateAudioBusChannels
o.numAudioBusChannels


Mdef.node("setbus_l1003").get_arg(\scoreline).get_notes
Mdef.node("setbus_l1003").subkind
Mdef.node("setbus_l1007").get_arg(\scoreline).get_notes
Mdef.node("setbus_l1007").get_arg(\scoreline).get_scoreset.sheets.do { arg sh, i; sh.notNil and:{ sh.notes.debug(i+"============")  }}
Mdef.node("setbus_l1007").get_arg(\scoreline).get_scoreset.current_sheet
Mdef.node("setbus_l1003").get_arg(\scoreline).get_scoreset.current_sheet
Mdef.node("setbus_l1003").get_arg(\scoreline).get_scoreset.sheets.do { arg sh, i; sh.notNil and:{ sh.notes.debug(i+"============")  }}

a = Dictionary.new
a.size
a[\a] = 1
a[\b] = 1
a[\b] = nil
a





Mdef.node("ci osc3filter2_l1021").modulation.get_modulation_mixers.keys.do(_.postln)
Mdef.node("ci osc3filter2_l1021").modulation.modulation_mixers[\wtpos_spread] = nil



(
	
	w = Window.new;
	w.front;
	w.layout = HLayout(
b = Button.new;
	);
	b.states  = [
		["plop\nbla"],
		["plop"],
	];
)




(
	var bla = [1,2,3];
	a = (
		a: bla,
		b: bla,
	);
	b = a.deepCopy;
)
	b.a.dump
	b.b.dump
	a.a.dump
	a.b.dump

	Membrane



// Change MembraneHexagon to MembraneCircle for a different shaped
// circular drum head
s.boot;
s.reboot;
(
{ var excitation = EnvGen.kr(Env.perc,
                            MouseButton.kr(0, 1, 0),
                             timeScale: 1, doneAction: 0
                            ) * PinkNoise.ar(0.4);
  var tension = MouseX.kr(0.01, 0.1);
  var loss = MouseY.kr(0.999999, 0.999, 1);
  MembraneHexagon.ar(excitation, tension, loss);
}.play;
)


(
SynthDef(\membraneHex, { arg out=0, amp=0.1, gate=1, pan=0, timeScale=0.1, noiseamp=0.4, tension=0.05, loss= 0.999,
		doneAction=2;
	var ou;
	var excitation;
	excitation = EnvGen.kr(Env.perc,
		gate,
        timeScale: timeScale, doneAction: doneAction
	) * PinkNoise.ar(noiseamp);
	ou = MembraneHexagon.ar(excitation, tension, loss);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}, metadata:(specs:(
	duration: ControlSpec(0.001,4,\lin, 0, 1),
	tension: ControlSpec(0.01,0.1,\lin, 0, 0.05),
	loss: ControlSpec(0.999,0.999999,\exp, 0, 0.05),
	timeScale: ControlSpec(0.01,1,\lin, 0, 0.1),
	noiseamp: ControlSpec(0.01,1,\lin, 0, 0.4),
))).store;
)






- variable
- pattern
- nodeproxy
- outputproxy

- array
- env
- synthfunc


(
	a = Pseq([(
		freq: Pseq([200, 300],inf)
	)],2);
	a.play


)

(
~side = Mdef.main.panels.side;
~node = ~side.get_current_player;
~param = ~node.get_arg(\pitchbend);
~side[\edit_modulator_callback].(~node, ~param);
)

Line

120.cpsmidi
80.midicps

~param



(
	Mdef.main.model.livenodepool
	Mdef.main.model.song_manager.current_song
	~cs = Mdef.main.panels.side.song_manager.current_song
	~used_nodes = ~find_children.(Mdef.main, ~cs).collect { arg no; no.uname };
	~used_nodes = ~find_children_uname.(Mdef.main, ~cs);
	~all_nodes = Mdef.main.model.livenodepool.keys
	~unused_nodes = ~all_nodes - ~used_nodes
	~unused_nodes.do(_.postln)
	~unused_nodes.do { arg nname;
		Mdef.main.free_node(nname)
	}

)
\bla === \bla
"bla" === "bla"
'ci bla'

(
)

~node_tools.get_unused_nodenames
~node_tools.free_unused_nodes


Mdef.main.play_manager.myclock = TempoClock.default



(
a = Pbind(
	\instrument, \default,
	\freq, 200,
	\dur, 1,
	\amp, 0.1
).play;
)
a.stop


~clock = TempoClock.default;
(
a = EventPatternProxy.new;
a.source = Pbind(
	\instrument, \default,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 0.1
);
				~clock.schedAbs(~clock.nextBar, { 
					~clock.beatsPerBar = 4;
					~clock.beats.debug("start_new_session: new clock beats");
					~clock.hash.debug("hash");
				});
				~clock.schedAbs(~clock.nextBar, { 
					a.play(~clock)
				});
				a.player.postln;
~sc = SimpleController(a);
~sc.put(\stopped, { "NIARK".postln; });
);

a.stop
a.player


(
Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 0.1
)).play;
);

Pdef(\plop).stop




Mdef.main.init_midi

(

SynthDef(\bufstut_glitch, { arg in, out=0, mix=0.5, amp=1, gate=1, pan=0, freq=200, bufdur=1,stutgate=0, stutfreq=4, start_pos=0;
	var ou;
	var bufnum;
	var stutenv;
	var wr_phase, rd_phase, rate=1;
	in = In.ar(in, 2);
	bufnum = LocalBuf(s.sampleRate * 2, 2);
	stutgate = Impulse.kr(stutfreq);

	wr_phase = Phasor.ar(0, BufRateScale.kr(bufnum) * rate, 0, BufFrames.kr(bufnum));
	rd_phase = Phasor.ar(stutgate, BufRateScale.kr(bufnum) * rate, (wr_phase-0.1)*BufSampleRate.kr(bufnum), BufFrames.kr(bufnum));

	BufWr.ar(in, bufnum, wr_phase, loop:1);
	ou = BufRd.ar(2, bufnum, rd_phase, 1);

	ou = SelectX.ar(mix, [in, ou]);
	Out.ar(out, ou * amp);
}, metadata:(specs:(
	stutgate: \unipolar.asSpec,
))).add;
)

s.sampleRate


MultiTap

(
var num = [4,8];
num.do {
	SynthDef("multitap%".format(num).asSymbol, { arg in, out=0, mix=0.5, amp=1, ;
		var sig;
		sig = 
		sig = sig * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
		sig = Pan2.ar(sig, pan, amp);
		Out.ar(out, sig);
	}).add;
}
)


(1..8)*0.125
b = Buffer.alloc(s, s.sampleRate * 0.125 * 9,2);
(
SynthDef(\multitap8, { arg in, out=0, mix=0.5, amp=1, gate=1,
		del1=1, del2=1, del3=1, del4=1, del5=1, del6=1, del7=1, del8=1, workbufnum;
	var sigin, sig;
	var delay = 0.125;
	var numdelay = 8;
	var bufnum;
	bufnum = LocalBuf(s.sampleRate * delay * (numdelay+2), 2);
	bufnum.clear;
	sigin = In.ar(in, 2);
	//bufnum = b;
	sig = MultiTap.ar(Ref( (1..numdelay) * delay), Ref([del1, del2, del3, del4, del5, del6, del7, del8]), sigin, 1, 0, bufnum);
	//sig = sig * EnvGen.ar(Env.asr(0.0001,1,0.0001),gate,doneAction:2);
	sig = SelectX.ar(mix, [sigin, sig]);
	Out.ar(out, sig);
}, metadata:(specs:(
	del1: \amp.asSpec,
	del2: \amp.asSpec,
	del3: \amp.asSpec,
	del4: \amp.asSpec,
	del5: \amp.asSpec,
	del6: \amp.asSpec,
	del7: \amp.asSpec,
	del8: \amp.asSpec,
	//workbufnum: (numChannels:2, numFrames: Buffer.alloc(s, s.sampleRate * 0.125 * 9,2),
)
))).store;
)

(
{
	var buf = LocalBuf(s.sampleRate * 1);
	MultiTap.ar(`[0.1, 0.2, 0.3, 0.4], `[0.1, 0.2, 0.4, 0.8],
		Decay.ar(Dust.ar(2), 0.1, PinkNoise.ar), bufnum: buf)
}.play
)


(
	a = { arg bla=(); bla.postln; 1 };
)
a.(nil)
a.()

(
a = ();
b = [bla: 3, rah:5];
a.putAll(b);
	
)
a

{
	jjj

}

(
	{
		var a = \bla.kr.dump;
		a.name.postln;
		a;
	}.play
)
Named



Synth("s_ci inline_genfx_l1170", [\freq, 300])



(
Dialog.openPanel({ arg path;
        path.postln;
},{
        "cancelled".postln;
});
)

2048*16
(
SynthDef(\ir_reverb, { arg in, out=0, mix=0.5, amp=1, gate=1, mbufnum, t_change_kernel;
	var sigin, sig;
	var kernel = mbufnum;
	sigin = In.ar(in, 2);
	sig = Convolution2.ar(sigin,kernel,t_change_kernel, 2048, 0.5)/8;
	//sig = sig * EnvGen.ar(Env.asr(0.0001,1,0.0001),gate,doneAction:2);
	sig = sig * amp;
	sig = SelectX.ar(mix, [sigin, sig]);
	Out.ar(out, sig);
}, metadata:(specs:(
	t_change_kernel: \unipolar.asSpec,
))).store;
)



~open_file_dialog.({})



SinOsc.ar(\bla.kr)

(

	a = { arg in;

		SinOsc.ar(\bla.kr) * in;
	};
	b = { arg iin; var x = a.(iin); Thunk.new({ x })};
)
b.value(2)

	a = { arg in;

		"calc".postln;
		Rand(100,300) * in;
	};
	b = { arg iin; Thunk.new({ a.(iin) })};
	~a = b.value(4);
	//~a = { Rand(100,1000) };

(
	~a = ArgThunk.new({ arg in; Rand(100,110)*in });
	//~a.value(3);
	
	{
		var a, b, c;
		var sig;

		sig = [
			SinOsc.ar(~a.value(1)),
			SinOsc.ar(~a.value(1)),
			SinOsc.ar(~a.value(6)),
		];
		sig = Splay.ar(sig, 1);
		sig = sig * 0.1


	}.play
)




Fdef(\x, { |x=10| x.rand });

Fdef(\x).value


~mythunk = { arg fun;
	(
		eval: {

		}

	)
	
}


Thunk
(
	{
	
UGen.buildSynthDef.postln;
}.play
)




(

~windowize_task.({~class_internal_modulator_gui.new.make_layout})
)

(
	{
		arg in=300;
		~bla = SinOsc.ar(in);
	}.play
)
~bla = 1
~bla

(
	{
		arg in=300;
		~bla;
	}.play
)



{ SoundIn.ar([0,1]) }.play


SparseArray.newFrom([1,2,3,4,5,6,7,8]).keep(8)
Array.newFrom([1,2,3,4,5,6,7,8]).keep(8)




Mdef.main.panels.side.song_manager.get_path([1,1,1]).children

Mdef.node(\s1_part1_sect1_var1).children


~node = Mdef.node_by_index(0)
~node.get_arg(\repeat).changed(\val)
~node.get_arg(\repeat).classtype
~node.get_arg(\repeat).set_val(10)

~node.data.keys.as(Array).sort.do(_.postln)



(
	
	~task = Task{
		loop {
			"======================= % - % - %".format(s.numUGens, s.numSynths, s.numGroups).postln;
			0.5.wait;
		}
	}.play(AppClock)
)


a = Bus.control
s.controlBusAllocator.debug
ContiguousBlock
ContiguousBlockAllocator.debug



(
Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0,1,2,3,4],inf)+Pwhite(0,1),
	\dur, Pseq(~dur),
	\legato, 0.9,
	\amp, 0.1
)).play;
);

(
Pdef(\plop, Pbind(
	\instrument, \default,
	//\degree, Pseq([0,1,2,3,4],inf)+Pwhite(0,1),
	\degree, Pseq([Pn(1,Pseq([2,5],inf).asStream)],inf),
	\dur, Pseq([Prand([1,2,3]),1]/8,40),
	\dur, Ppatlace([Prand([1,2,3]),1,Pseq([2,4],inf), Pgeom(1,0.9,3)]/8,40),
	\dur, Ppatlace([1, Pn(Pgeom(1,0.9,10),inf)]/8,40),
	\dur, 0.5,
	\legato, 0.9,
	\amp, 0.1
)).trace.play;
);

~dur = 50.collect { arg n; x = sin(n/10).abs + 0.1; x/10 }

(100..1)

a = (bla:1, rah:2)
a.removeAt(\bla)
a

rand
