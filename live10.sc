
(
s.waitForBoot{
~seq = Mdef.init;
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
//~seq.set_presetlib_path("mypresets2");
~seq.append_samplelib_from_path("sounds/" );
~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );



Mdef.side_gui;
}
)
Mdef(\froid, Pbind(
	\instrument, \lead2
));
Mdef(\chaud, Pbind(
	\instrument, \lead2
));
Mdef(\sampleme, Pbind(
	\instrument, \monosampler
));

(
Mdef(\bleu, Pbind(
	\instrument, \lead2
));
)
(
Mdef(\sampleme, Pbind(
	\instrument, \monosampler
));
)
