~veco.load_project("/home/ggz/code/sc/seco/vlive/v15")
//"vim --servername scvim --remote-tab c.sc".unixCmd


(
	~gen_vim_bindings = {
		var res = "";
		var keys =
			"&é\"'(-è_" ++
			//"12345678" ++
			"azertyui" ++
			"qsdfghjk" ++
			"wxcvbn,;";
		var files = 
			"12345678" ++
			"azertyui" ++
			"qsdfghjk" ++
			"wxcvbnVP";
		keys.do { arg key, x;
			res = res ++ "map <F9>% <Esc>:drop %.scd\n".format(key, files[x].asString)
		};
		res.debug("res:");

	};
	~gen_vim_bindings.()
)


MIDIdef

MIDIOut.noteOn(1, 60, 64)

m = MIDIOut(0);

m.noteOn(0, 110, 60);
m.control(0, 110, 60);
m.noteOn(0, 61, 60);
m.noteOff(1, 61, 60);
m.allNotesOff(16);


(
Task({
	127.do { arg i;
		m.noteOn(0, 1, i);
		i.postln;
		0.5.wait;
	}
}).play
)


~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\launchpad, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	~mididict[midinote].stop;
	pat = Pbind(
		\note, Pseq([
			//Pseq([0,\r,0,0].scramble,2),
			//Pseq([0,\r,\r,\r].scramble),
			Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
		],inf),
		\ctranspose, midinote-60,
		\dur, 1/8,
	);
	m.noteOn(0, midinote, [15,60,62,47].choose);
	pat = pat.trace.play(quant:[1/8]);
	~mididict[midinote] = pat;

}); // match any noteOn
MIDIdef.noteOff(\launchpadoff, {arg vel, midinote;
	var note = ~mididict[midinote];
	[vel, midinote].postln;
	TempoClock.play({ 
		note.stop;
		m.noteOn(0, midinote, 0);
	}, quant:0.5)

}); // match any noteOn
)
MIDIdef.freeAls

(
~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\launchpad, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	"vim --servername SCVIM --remote-send <Esc>:call SCveco_open_buffer("%", %)<Enter>".format(midinote, midinote).unixCmd;

}); // match any noteOn
MIDIdef.noteOff(\launchpadoff, {arg vel, midinote;

}); // match any noteOn
)
"vim --servername SCVIM --remote-send '<Esc>:call SCveco_open_buffer(\"7\", 6)<Enter>'".unixCmd

(

//~sceneset.tile.set_cc_led(2, \yellow);
//~sceneset.tile.set_led(1@1, \yellow);

)

(


)

~sceneset.state_mode_leds

(
Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0],inf) * Pn(Plazy({ ~la.pseqrow(0) }), inf),
	\dur, 1/4,
	\amp, 0.1
)).play;
Pdef(\plop2, Pbind(
	\instrument, \default,
	\degree, Pseq([2],inf) * Pn(Plazy({ ~la.pseqrow(1) }), inf),
	\octave, 4,
	\dur, 1/4,
	\amp, 0.1
)).play;
Pdef(\plop3, Pbind(
	\instrument, \default,
	\degree, Pseq([4],inf) * Pn(Plazy({ ~la.pseqrow(2) }), inf),
	\octave, 3,
	\dur, 1/4,
	\amp, 0.1
)).play;
);	

Pbindef(\plop3, \ctranspose, 2)

(
~con.automate({

	SinOsc.kr(1).range(100,3000)
})
)

(
~trans = Pbind(
	\mtranspose, Pfunc({ ~la.get_range_state(7) }),
);
~con = GenericGlobalControl.new;
~con.set(300);


Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0],inf) * Pn(Plazy({ ~la.pseqrow(0) }), inf),
	\dur, 1/4,
	\amp, 0.1
) <> ~trans).play;
Pdef(\plop2, Pbind(
	\instrument, \osc1,
	\degree, Pseq([2],inf) * Pn(Plazy({ ~la.pseqrow(1) }), inf),
	//\ffreq, ~con,
	//\ffreq, Pfunc({ ~la.get_range_state(6).debug("rs") }) * 300 + ~con, 
	\ffreq, Pfunc({ ~la.get_range_state(6).debug("rs") }) * 300 + 100, 
	\octave, 4,
	\dur, 1/4,
	\amp, 0.1
) <> ~trans).play;
Pdef(\plop3, Pbind(
	\instrument, \default,
	\degree, Pseq([4],inf) * Pn(Plazy({ ~la.pseqrow(2) }), inf),
	\octave, 3,
	\dur, 1/4,
	\amp, 0.1
) <> ~trans).play;

Pdef()
);	

P9tempo


// "a" is an array-of-arrays
a = { { 100.0.rand  }.dup(100)  }.dup(100);
// "b" is an equivalent Array2D, made using the "fromArray" class method
b = Array2D.fromArray(100,100, a.flat);

// Accessing
a[15][22]
b[15, 22]



// Speed comparison 1: random access
bench { 1000.do(a[100.rand][100.rand]) }
bench { 1000.do(b[100.rand, 100.rand]) }

// Speed comparison 2: iteration
bench { 1000.do(a.do { |row| row.do { |item| item * 2 } }) }
bench { 1000.do(b.do { |item| item * 2 }) }

a = Array2D.new(2,4)
a[1,3] = 1
Array2D.new(1,3)



Veco.force_init
~veco.buffers.names.findKeyForValue('e')
~veco.name_to_index('e')

(

		a = Task({
			var self = ~veco.mixer_mod;
			var pos = 8;
			var color1 = \green;
			var color2 = \red;
			var time = 0.5;
			20.do {
				self.set_led(pos, color1);
				time.wait;
				self.set_led(pos, color2);
				time.wait;
			}
		});
		a.play
)
a.stop



(
	
	var a;
	var f1, f2;
	~a = [1,0];
	f1 = { ~a };
	~a = [0,1];
	f2 = { ~a };
	f1.().postln;
	f2.().postln;

)

Sdef(~name, \sampler, \sampler, \voices);

Sdef(~name, \sampler).at(~index);

Samplekit(~name, [
	"a11.wav",
	["a11.wav", 0.1],
	Pbind(
		\instrument, ~name,
		\freq, Pseq([100,200],inf)
	),
	Sdef(~name, \mypbindpreset).at(1),
	Pdef(\bla)
])

(
Sdef(~namex, \sampler).pat <> Pbind(
	\speed, Pseq([1,2,0.5],inf)
)
);

(
Sdef(~namex, \sampler).pat <> Pbind(
	\score, Sdef(~namex, \sampler).sampler_seq,
	\bufnum, Pfunc({ arg ev; (\default, ev[\score][\bufnum]) })
	\speed, Pseq([1,2,0.5],inf)
)
);


Sdef(~name, \sampler).edit
Sdef(~name, \sampler).record



(
	~bla = (
		asPattern: { arg self;
			Pseq([1,2,3]*100,inf)
		},

	);
	Pbind(
		\freq, ~bla,
	).play
)
~bla.asPattern





Sdef(~name, \mymultislider, \array, \freq.asSpec)

Pdef(~name+++\pre, Pbind(
	\freq, Pseq(Sdef(~namex, \mymultislider).array,inf)
))


EnvSpec

Sdef(~name, \myenv, \env, )




(
{

var in, fft, entropy;
var amplitude; 

in= SoundIn.ar; 

amplitude = Amplitude.kr(in); 

fft = FFT(LocalBuf(1024), in);

entropy=SpectralEntropy.kr(fft,1024,10);//10 bands

entropy = entropy * (amplitude.min(0.2)*5.0); //scale by amplitude to avoid low amplitude noise issues

entropy.poll; 

//Out.ar(0,Pan2.ar(0.1*Blip.ar((entropy[0])*200,entropy[1].sqrt))); 

Out.ar(0,Splay.ar(0.1*Blip.ar((entropy)*200,entropy.sqrt)));
}.play
)




(
	~record_duration = 8;
	
)

(
	~arec = ~audio_recorder.new;
	~arec.start_recording;
)

(
	~play_audio_recording = { arg buf;
		Pbind(
			\instrument, \audiotrack,
			\sustain, 8,
			\dur, 8,
			\bufnum, buf,
		).play(quant:Pdef.defaultQuant);

	};
	~play_audio_recording.(~arec.buf);
)

s.sampleRate
(instrument: \record_input).play
Synth(\record_input, [\sustain, 4])
Pdef.defaultQuant
TempoClock.default.tempo



Sdef(~name, \myaudio, \audio, 8)

Sdef(~name, \myaudio).record
Sdef(~namex, \myaudio).record(16)


(
Pdef(~name+++\pre, <> Pbind(
	\bufnum, Sdef(~namex, \myaudio).bufnum,
	\speed, Pseq([1,2,0.5],inf)
))
);
//~a = ~class_node_preset.new(\default, [\dur, \dur.asSpec]);
//~a.edit;
//~a.pbind.trace.play;


(
	~ctrl = ~env_controller.new(Env([80,1500.1,90.51,300,400],[0.1,0.2,0.1,0.1]), [0, \freq.asSpec]);
)

(
SynthDef(\ftest, { arg out=0, amp=0.1, gate=1, pan=0, spread=0.8, freq=200, doneAction=2;
	var sig, sig1, sig2, sig3;
	freq = freq + EnvGen.kr(\fenv.kr(Env([80,500.1,90.51,300,400],[0.1,0.2,0.1,0.1])), gate);
	sig = SinOsc.ar(freq);
	sig = sig * EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:doneAction);
	sig = Splay.ar(sig, spread, amp, pan);
	Out.ar(out, sig);
}).add;
);

(

Pdef(\plop, Pbind(
	\instrument, \ftest,
	\fenv, Pfunc ({ ~ctrl.get_env  }),
	\freq, 200,
	\dur, 1,
	\amp, 0.1
)).play;
)

~ctrl.edit;

a = Env([0,1,0],[0.1,0.1])
a.times[0] = 0.2
a.times
a.curves
a.pairs
a.array

\freq.asSpec.map([0.1,0.2,0.3])

(
{ SinOsc.ar(
    EnvGen.kr(
        Env([6000, 700, 100], [1, 1], ['exp', 'lin']).circle.postcs)
    ) * 0.1
    + Impulse.ar(1) }.play;
)

(
{ SinOsc.ar(
    EnvGen.kr(
        Env([6000, 700, 100], [1, 1], ['exp', 'lin']).circle(1).postcs,
        MouseX.kr > 0.5)
    ) * 0.1
    + Impulse.ar(1) }.play;
)




(
// use shift-click to keep a node selected
w = Window("envelope", Rect(150 , Window.screenBounds.height - 250, 250, 100)).front;
w.view.decorator = FlowLayout(w.view.bounds);

b = EnvelopeView(w, Rect(0, 0, 230, 80))
    .drawLines_(true)
    .selectionColor_(Color.red)
    .drawRects_(true)
    .resize_(5)
    .step_(0.05)
    .action_({arg b; [b.index, b.value].postln})
    .thumbSize_(5)
    .value_([[0.0, 0.1, 0.5, 1.0],[0.1,1.0,0.8,0.0]]);
w.front;
)

// show grid
b.gridOn_(true);

// show Env
b.setEnv(Env.asr(0.5,1, 0.2));

// make the first point unmoveable
(
b.setEditable(0,false);
)

[0.1,0.5,0.9].differentiate




(
	w = Window.new("The Eightfold Path").front;


	b = Slider(w, Rect(10,10,180,20));
	b.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
		if(modifiers.isShift, {	
			m = ListView(w,Rect(10,10,180,200));


			m.items = [
				"right view","right thinking","right mindfulness","right speech",
				"right action","right diligence","right concentration","right livelihood"
			];


			m.action = { arg menu;
				[menu.value, menu.item].postln;	
				m.remove;
			};
			m.mouseDown(x, y, modifiers, buttonNumber, clickCount);
		});
	};
)



(
	

	~class_control_adsr = (
		kind: \env,
		new: { arg self, parent, name, spec;
			self = self.deepCopy;
		
			self.parent_controller = { parent };
			spec = spec.value.deepCopy;
			self.spec = spec ?? [nil, \freq.asSpec, ControlSpec(0.01,8,\exp,0,0.1), ControlSpec(-9,9,\lin,0,0), Env.adsr(0.1,0.1,0.8,0.1)];

			self.name = name;

			self.spec[1] = self.spec[1] ?? \unipolar.asSpec;
			self.spec[2] = self.spec[2] ?? ControlSpec(0.01,8,\exp,0,0.1);
			self.spec[3] = self.spec[3] ?? ControlSpec(-9,9,\lin,0,0);

			self.get_env = self.spec[4] ?? Env.adsr(0.1,0.1,0.8,0.1);
			self.level_spec = self.spec[1];
			self.time_spec = self.spec[2];
			self.curve_spec = self.spec[3];
		
			self;
		},

		get_val: { arg self;
			self.get_env;
		},

		set_val: { arg self, val;
			self.parent_controller.is_present = true;
			self.get_env = val;
		},

		label: { arg self;
			self.name;
		},

		edit: { arg self;
			~env_editor.new(self).make_window;
		},

		vpattern: { arg self;
			Pfunc { self.get_env }
		},

	);
	~class_control_adsr_view = (
		new: { arg self, controller;
			self = self.deepCopy;
		
			self.controller = {controller};
		
			self;
		},

		set_controller: { arg self, controller;
			self.controller = { controller };
			self.update_view;
		},

		update_view: { arg self;
			{
				self.key_label.string = self.controller.label.asString;
				self.update_env;
				self.update_label;
				self.env_view.selectIndex(1);
				self.update_knobs;
			}.defer;
		},

		update_label: { arg self;
			self.env_label.string_(
				"%".format(
					~env_to_string.(self.controller.get_env),
				)
			);
		},
		
		update_env: { arg self;
			self.env_view.value_(self.controller.get_norm_xy);
		},

		update_knobs: { arg self;
			var index = self.env_view.index;
			var levels = self.controller.get_env.levels;
			var times = self.controller.get_env.times;
			var level_spec = self.controller.level_spec;
			var time_spec = self.controller.time_spec;
			if(levels[index].notNil) {
				self.knob_level.value = level_spec.unmap(levels[index]);
			};
			if(times[index].notNil) {
				self.knob_time.value = time_spec.unmap(times[index]);
			};
		},

		make_layout: { arg self;
			var params, layout, sliders, label;
			var time_spec, level_spec;
			time_spec = self.controller.time_spec;
			level_spec = self.controller.level_spec;
			curve_spec = self.controller.curve_spec;
			params = [
				attack: time_spec,
				decay: time_spec,
				sustain: level_spec,
				release: level_spec,
				curve: curve_spec,
			];

			layout = VLayout.new;

			params.clump(2).do { arg pair;
				var key, val;
				var playout, plabel, slider;
				#key, val = pair;
				playout = HLayout.new;

				plabel = StaticText.new;
				plabel.string = key.asString[0].asString;

				slider = Slider.new;
				slider.orientation = \horizontal;
				slider.action = { arg slider;
					slider.value

				}

				playout.add(plabel);
				playout.add(slider);
				layout.add(playout);
			};

			label = StaticText.new;
			label.string = "Env.adsr....";
			layout.add(label);
			layout;
			
		},

		make_window: { arg self;
			~windowize.(self.make_layout);
		},
	);
)


(
	
	~adsr = {
		var params, layout, sliders, label;
		var time_spec, level_spec;
		time_spec = \unipolar.asSpec;
		level_spec = \unipolar.asSpec;
		params = [
			attack: time_spec,
			decay: time_spec,
			sustain: level_spec,
			release: level_spec,
			curve: level_spec,
		];

		layout = VLayout.new;

		params.clump(2).do { arg pair;
			var key, val;
			var playout, plabel, slider;
			#key, val = pair;
			playout = HLayout.new;
			plabel = StaticText.new;
			plabel.string = key.asString[0].asString;
			slider = Slider.new;
			slider.orientation = \horizontal;
			playout.add(plabel);
			playout.add(slider);
			layout.add(playout);
		};

		label = StaticText.new;
		label.string = "Env.adsr....";
		layout.add(label);
		layout;
	};
	~windowize.(~adsr.())
)


~a = Env.adsr(0.1,0.1,0.8,0.1)
~a.times
~a.levels
