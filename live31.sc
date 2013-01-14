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
].collect({arg i; i -> i });

~effectlib = [
	\modenv,
	\lfo1,
	\line1,
].collect({arg i; i -> i });

~samplelib = [
	"sounds/perc1.wav",
	"sounds/pok1.wav",
	"sounds/amen-break.wav",
	"sounds/default.wav"
];
~seq.load_patlib( ~synthlib );
~seq.load_effectlib( ~effectlib );
~seq.load_modlib( ~effectlib );
~seq.set_presetlib_path("mypresets2");
~seq.append_samplelib_from_path("sounds/" );
~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );

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


Mdef.main.save_project("live20");
Mdef.main.load_project("live20");

Debug.enableDebug = true;
Debug.enableDebug = false;

(
SynthDef(\lfo1, { arg out=0, freq=1;
	var sig = SinOsc.kr(freq);
	Out.kr(out, sig);
}, metadata:(specs:(
	freq: \lofreq.asSpec
))).store;

SynthDef(\line1, { arg out=0, duration=0.5;
	var sig = Line.kr(0, 1, duration);
	Out.kr(out, sig);
}, metadata:(specs:(
	duration: ControlSpec(0.001,4,\lin, 0, 1)
))).store;


SynthDef(\adsr1, { arg out, attack, gate=1, doneAction=0;
	var sig = EnvGen.kr(Env.adsr(attack,0.1,1,0.1), gate, doneAction:doneAction);
	Out.kr(out, sig);
}).add;

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

SynthDef(\comb1, { arg in, out, mix=0.5, maxdelaytime=0.4, delaytime=0.4, decaytime=2, gate=1;
	//var sig = EnvGen.kr(Env.adsr(attack,0.1,1,0.1), gate, doneAction:doneAction);
	var sig, sigwet;
	sigwet = In.ar(in, 2);
	sig = CombL.ar(sigwet, maxdelaytime, delaytime, decaytime);
	sig = SelectX.ar(mix, [sigwet, sig]);
	Out.ar(out, sig);
}).store;

SynthDef(\modenv, { |out, val=0, t_trig=1, gate=1, tsustain, curve=0, doneAction=0|
       var start = In.kr(out, 1);
	   var sig;
	   //start.poll;
	   sig = EnvGen.kr(Env([start, val], [tsustain], curve), t_trig, doneAction: doneAction);
	   sig.poll;
       ReplaceOut.kr(out, sig);
}).store;

)

if(~t.notNil) {~t.window.close};
~t = ~class_group_tracks_controller.new(Mdef.main, Mdef.node(\s1_part1_sect1_var1));
~t.make_gui;

Mdef.node(\s1_part1_sect1_var1)


if(~t.notNil) {~t.window.close};
~t = ~class_step_track_controller.new;
~t.make_gui;

~t.notes.get_rel_notes
~t.notes.get_abs_notes(2,3)
~t.notes
~t.notes.set_notes(~default_step_scoreline)
~t.notes.abs_start
~t.notes.abs_end
~event_rel_to_abs.(~default_step_scoreline)
~t.notes.notes.reject{ arg no; no.time == 5 }
~t.notes.notes.class
~t.notes.get_rel_notes

~notes = ~make_notescore




"~/code/sc/seko/crap38.sc".standardizePath.load

Mdef.node("piano2_l1061").get_arg(\stepline).seq.get_val(3)
Mdef.node("piano2_l1089").get_arg(\stepline).get_val(3)
Mdef.node("piano2_l1089").get_arg(\stepline).get_val(gtgt1)
Mdef.node("piano2_l1089").get_arg(\stepline).seq

nil !? 4


~seq = Mdef.force_init(true);
(
"/home/tytel/code/sc/seco/tracks.sc".load;
a = ~class_blabla.new;
a.make_gui;

)

a.notescore.notes
a.notescore.abs_end
a.notescore.set_end(8)
a.notescore.get_rel_notes.do(_.postln)


a = ObjectTable.new
i = a.add("plop")
a.at(i)
i = ObjectTable.add("plop")
ObjectTable.at(i)

(
	(
	midinote_to_notename: { arg self, midinote;
		var notenames = [ "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" ];
		var octave = ((midinote / 12) - 1).asInteger;
		var name = notenames[ midinote % 12 ];
		name ++ octave.asString;
	
	},
	).midinote_to_notename(2)
)

(
"/home/tytel/code/sc/seco/modulation.sc".load;
a = ~class_modulated_param_view.new;
~windowize.(a.make_gui);

)

(
"/home/tytel/code/sc/seco/modulation.sc".load;
a = ~class_modulator_body_basic.new;
~windowize.(a.make_gui);

)

(
"/home/ggz/code/sc/seco/modulation.sc".load;
a = ~class_effect_mini_view.new;
~windowize.(a.make_gui);

)

a.track_view[\block_size_y]
(
"/home/ggz/code/sc/seco/tracks.sc".load;
a = ~class_curve_track_controller.new;
a.make_gui;
~windowize.(a.layout);

)
a.changed(\notes)
a.track_view.timeline.refresh
a.notescore.get_rel_notes.do(_.postln); 1

(
~seq = Mdef.force_init(true);
Mdef(\rouge, Pbind(
	\instrument, \piano2
));
~pl = Mdef.node(\rouge);
~pa = ~pl.get_arg(\freq);
~class_modulation_controller.new(Mdef.main, ~pl, ~pa);
)

Mdef.node(\rouge).modulation.get_modulator_node(0)



Mdef.node("osc1_l1045").modulation.get_modulation_mixers[\ffreq].dump
Mdef.node("lfo1_l1046").modulation.get_modulation_mixers[\freq].dump

Mdef.node("modenv_l1052").get_arg(\noteline).get_note(2)


VarLag

(
SynthDef(\modenv, { |out, firstsynth=0, firstval=0, t_trig=1, gate=1, tsustain, val=0, curve=0, doneAction=0|
       var start = Select.kr(firstsynth, [In.kr(out, 1), firstval]);
	   var sig;
	   //start.poll;
	   //sig = EnvGen.kr(Env([start, val], [tsustain], curve), t_trig, doneAction: doneAction);
	   sig = EnvGen.kr(Env([start, val], [tsustain], curve), t_trig, doneAction: doneAction);
	   //sig = VarLag.kr(val, tsustain);
	   sig.poll;
       ReplaceOut.kr(out, sig);
}).store;
)

b = Bus.control(s, 1)
b.free
b.set(4)
b.get({ arg bus; bus.postln; })
(
b.set(0);
 a = Pmono(
 	\modenv,
	\out, b.index,
	\firstsynth, Pseq([1, Pn(0)]),
	\type, \rest,
	\val, Pseq([2,1,3]),
	\firstval, 1,
	\dur, Pseq([Pn(4)],inf),
	\t_trig, 1,
	\tsustain, Pkey(\dur),

	 );
 ~a = Pbind(
 	\dur, 4,
	\sustain, 0.1,
	\instrument, \default,
 
	 );

	 //a.trace.play
	 Ppar([a, ~a]).trace.play;


)

TempoClock.default.tempo

(
 a = Pmono(
 	\osc1,
	\freq, Pseq([\rest,100,200,\rest,400]),
	\ffreq, 800,
	\rq, Pseq([1,0.1],inf),
	\dur, 2,
	 );

	 //a.trace.play
	 Ppar([a]).trace.play;


)




~seq = Mdef.force_init(true);
~seq.side
Mdef.side_gui;
(

"/home/ggz/code/sc/seco/classinstr.sc".load;
~player = ~seq.node_manager.make_livenode_from_libnode("ci mosc");
Mdef.node(~player).external_player.make_gui;
//~windowize.(~ci.layout);
)


x = 4;

a = SimpleController.new(x)
b = SimpleController.new(x)
a.put(\val, { "bla".postln; });
b.put(\val, { "bli".postln; });

x.changed(\val)


Mdef.node("ci osc_l1002").external_player.static_data[\spectrum].changed(\val)
a = SimpleController(Mdef.node("ci osc_l1002").external_player.static_data[\spectrum])

Mdef.node("ci osc_l1002").external_player.set_static_responders

