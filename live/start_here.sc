
(
	
s.waitForBoot {

	// load and init the Seco code, return a reference to the main object
	// the main object is also stored in Mdef.main

	~seq = Mdef.init; 
	~seq = Mdef.force_init(true); 

	// add synthdefs to the Seco synthdef library
	// this could be evaluated at any time later to update the list

	~synthdef_lib = [
		\kick1,
		\osc1,
	];
	~seq.load_patlib( ~synthdef_lib );

	// add synthdefs used as modulators to the Seco synthdef modulator library

	~modlib = [
		\lfo1,
		\line1,
	];
	~seq.load_modlib( ~modlib );

	// add synthdefs used as effects to the Seco synthdef modulator library

	~effectlib = [
		\comb1,
		\p_reverb,
		\p_flanger,
	];
	~seq.load_effectlib( ~effectlib );

	// add samples to the sample library (see the chapter below)

	Mdef.main.samplekit_manager.parse_samplekit_dir;

	// show the main gui

	Mdef.side_gui;

};

)
