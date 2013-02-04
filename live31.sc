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


Mdef.main.save_project("live31.3");
Mdef.main.load_project("live31.3");

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

Mdef.node(\s1_part1_sect1_var1).kind


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

Mdef.node("ci dadsr_kr_l1025").external_player.synthdef_basename == ""
Mdef.node("ci dadsr_kr_l1025").defname

Mdef.node("ci dadsr_kr_l1080").uname

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


Mdef.main.midi_center.fixed_bindings[[\knob, 1]].name
Mdef.main.midi_center.fixed_bindings
~p = Mdef.node(Mdef.node(\s1_part1_sect1_var1).children[0]).get_arg(\attack)
Mdef.main.midi_center.get_ccpath_assigned_with_given_param(~p)
Mdef.main.commands.ccpath_to_param([\knob, 7]).name

x = 4;

a = SimpleController.new(x)
b = SimpleController.new(x)
a.put(\val, { "bla".postln; });
b.put(\val, { "bli".postln; });

x.changed(\val)


Mdef.node("ci osc_l1002").external_player.static_data[\spectrum].changed(\val)
a = SimpleController(Mdef.node("ci osc_l1002").external_player.static_data[\spectrum])

Mdef.node("ci osc_l1002").external_player.set_static_responders


SynthDescLib.at(\ci_osc3filter2).controls.collect(_.name)


a = IdentityDictionary.newFrom([rah: (bla:4)])
b = a.copy
a[\rah].dump
b[\rah].dump
b.rah.dump

Mdef.main.model.modnodelib



(

Mdef.main.save_project("live31_test12");
)


Mdef.main.load_project("live31_test12");

~d = Mdef.node("ci osc3filter2_l1066").external_player

Mdef.node("ci moscfilter_l1004").get_arg(\wt).get_val
~d = Mdef.node("ci osc3filter2_l1035").save_data
~d = Mdef.node("ci moscfilter_l1004").get_arg(\wt).save_data
Mdef.node("ci moscfilter_l1005").get_arg(\wt).load_data(~d)
Mdef.node("ci moscfilter_l1005").get_arg(\wt).get_val

Mdef.node("ci osc3filter2_l1036").load_data(~d)
~d.static_data
~d.external_player

~d = Mdef.node("ci mosc_l1033").data[\wt].save_data
~d = Mdef.node("ci mosc_l1034").data[\wt].load_data(~d)


kkk
~d = Mdef.node("osc1_l1023").modulation.get_modulation_mixer(\ffreq)
~d = Mdef.node("osc1_l1023").modulation

~d = Mdef.node(Mdef.node(\s1_part1_sect1_var1).children[1]).get_arg(\speed).spec

(
~class_ci_ienv_view = (
	new: { arg self, controller;
		self = self.deepCopy;

		self.curve = [[25,50,70,110], [0,10,70,128]];
		self.controller = { controller };
		self.make_gui;
		self.update_curve;

		self;
	},

	set_curve: { arg self, curve;
		self.curve = curve;
		self.controller.get_current_ienv.set_curve(self.curve);
		self.update_curve;
	},

	update_curve: { arg self;
		var levels, times;
		var time = 0;
		var curve = self.curve;
		self.ienv_view.clearSpace;
		#levels, times = curve;
		levels.do { arg lev, idx;
			time = times[idx]/128;
			self.ienv_view.createNode1(time,1-(lev/128));
			if(idx > 0) {
				self.ienv_view.createConnection(idx-1, idx);
			};
		};
	},

	make_matrix: { arg self;
		var presets = self.controller.get_presets_names;
		GridLayout.rows(*
			[
				[nil] ++ presets.collect { arg name;
					Button.new
						.states_([[name]])
				}
			] ++
			self.controller.get_ienv_controllers_list.collect { arg ctrl;
				[
					Button.new
						.states_([[ctrl.get_label]])
				] ++
				presets.collect { arg name, idx;
					Button.new
						.states_([[idx]])
				}
			} ++ [nil]
		)
	},

	make_gui: { arg self;
		self.ienv_view = ParaTimeline.new;
		self.ienv_view.userView.minSize_(200@150);
		self.node_shape = "circle";
		self.node_align = \center;
		self.ienv_view.setShape_(self.node_shape);
		self.ienv_view.nodeAlign_(self.node_align);
		self.ienv_view.nodeTrackAction = { arg node;
			var pos = self.ienv_view.getNodeLoc1(node.spritenum);
			pos = pos[0] @ pos[1];
			pos = pos.round(1/128);
			switch(node.spritenum,
				0, {
					self.ienv_view.setNodeLoc1_( node.spritenum, 0, pos.y );
				},
				3, {
					self.ienv_view.setNodeLoc1_( node.spritenum, 1, pos.y );
				},
				{
					self.ienv_view.setNodeLoc1_( node.spritenum, pos.x, pos.y);
				}
			);
		};
		self.ienv_view.nodeUpAction = {
			//self.curve = 
			var nodes = self.ienv_view.getNodeStates[0].collect({ arg node;
				var pos;
				pos = (node/self.ienv_view.bounds.extent);
				pos = [pos.x, 1-pos.y] * 128;
				pos = pos.round(1);
				pos;
			});
			//nodes.debug("note state before sort");
			nodes = nodes.sort({ arg a, b; a[0] < b[0] });
			//nodes.debug("note state after sort");
			nodes = nodes.flop.reverse;
			//nodes.debug("note state");
			self.curve = nodes;
			self.update_curve;

		};
		self.ienv_view.refresh;
		self.layout = HLayout(
			self.ienv_view.userView,
			self.make_matrix,
		);
		self.layout;
		
	},
);
b = ~class_param_ienv_presets_controller.new;
b.add_preset(\off, [[25,50,70,110], [0,10,70,128]]);
b.add_preset(\linear, [[25,50,70,110], [0,10,70,128]]);
b.add_preset(\user1, [[25,50,70,110], [0,10,70,128]]);
b.add_ienv_controller(\osc1, \off);
b.add_ienv_controller(\osc2, \off);
a = ~class_ci_ienv_view.new(b);
~windowize.(a.layout);
)



(
w=Window().layout_( GridLayout.rows(c
    [Slider2D(), Slider2D(), [Slider(), rows:2]],
    [Slider2D(), Slider2D()],
    [[Slider().orientation_(\horizontal), columns:2]]
)).front;
)

(
w=Window().layout_( GridLayout.columns(
	3.collect{ Button.new.states_([["kkkkkkkkkkkkk"]]) },
	3.collect{ Button.new },
	3.collect{ Button.new },
)).front;
)

(
a=	 HLayout(

 	StaticText.new.string_("TTTTTTTTTTTTTT")
		 );

 w.layout_(

a
	 )
	 
)
a.remove
a.parent.children
a.parent.layout

(
w.layout.remove
)
w.layout = HLayout.new
w.layout
w.layout.add(StaticText.new.string_("kj"))
w.layout.children
w.view.children.do(_.remove)
w.view.children = nil
w.view.children



(
a = ModSlider.new(nil, Rect(0,0,30,300));
v = VLayout(
		StaticText.new.string_("lkjlkj"),
		[a.asView, align:\center]
	);
~windowize.(v);

)
(

	var label, knob, vallabel;
		var layout = VLayout.new(
			label = StaticText.new
				.align_(\centered)
				.string_("kjkj")
				;
				[label, stretch:0, align:\center],
			knob = ModLayoutSlider.new(nil, Rect(0,0,350,700)); 
				debug("BLLA");
				knob.asView.debug("SLIDERVIEW");
				knob.asView.minSizeHint.asRect.debug("hint");
				//knob.asView.minSize_(self.view_size.extent);
				//knob.minSize_(5@5);
				//knob.maxSize_(350@350);
				//[knob.asView, stretch: 1, align:\left],
				knob.rangeview.minWidth_(12);
				//knob.rangeview.minSize_(80@480);
				knob.maxSize_(800@880);
				knob.rangeview.maxSize_(800@880);
				knob.slider.maxSize_(800@880);
				knob.background = Color.red;
				//knob.slider.maxWidth_(5);
				[knob.asView, stretch: 3, align:\topLeft],
				//[knob.asView, stretch: 1],
			vallabel = StaticText.new
				.string_("12354")
				.font_(Font("Arial",11))
				.align_(\center)
				.minWidth_(75)
				//.minWidth_(75);
				;
				[vallabel, stretch:0, align:\center],
			nil
		);
~windowize.(layout);
)
