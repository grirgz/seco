(
s.waitForBoot{
Window.closeAll;
~seq = Mdef.force_init(true);
//~seq.init_midi;
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

~seq.set_presetlib_path("mypresets");

Mdef.side_gui;

~windowize = { arg layout;
	var win;
	win = Window.new;
	win.layout = layout;
	win.front;
};

Mdef.main.samplekit_manager.parse_samplekit_dir;
Mdef.main.samplekit_manager.parse_samplekit_dir(~seco_root_path +/+ "hydrogenkits/");

}
)
