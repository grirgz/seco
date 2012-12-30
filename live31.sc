~seco_dir_path
Quarks.gui
(
~seco_dir_path = if( thisProcess.nowExecutingPath.notNil) {
	thisProcess.nowExecutingPath.dirname
} {
	"~/code/sc/seco/".standardizePath
};
)
Ptempo
(
~seco_dir_path = "~/code/sc/seco/".standardizePath;
s.waitForBoot{
//"/home/ggz/code/sc/abcparser.sc".load;
~seq = Mdef.force_init(true);
//"/home/ggz/code/sc/seco/tracks.sc".load;
~synthlib = [
	\audiotrack_expander,
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

}
)


Mdef.main.save_project("live20");
Mdef.main.load_project("live20");

Debug.enableDebug = true;
Debug.enableDebug = false;

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
"/home/tytel/code/sc/seco/modulation.sc".load;
a = ~class_modulation_view.new;
~windowize.(a.make_gui);

)


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
