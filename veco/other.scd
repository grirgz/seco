
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
)
);
