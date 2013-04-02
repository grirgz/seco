
(
s.waitForBoot{
~seq = Mdef.force_init;
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
}
)

(
Mdef("pulsepass_l1035", Pbind(
	\octave, 5

))
)
~seq.save_project("belleinconnue")
~seq.load_project("belleinconnue")
~seq.load_project("pasla1")
~seq.load_project("saveme2")

~seq.save_project("testsaveme2")
~seq.load_project("testsaveme2")

~nn = Mdef.node("monosampler_l1011")
~nn2 = Mdef.node("monosampler_l1002")
~nn2.get_arg(\sampleline)

~data = ~nn.get_arg(\sampleline).save_data
~nn2.get_arg(\sampleline).load_data(~data)

~nn.

~nn = Mdef.node("pulsepass_l1035")
~nn.get_arg(\sampleline).scoreset.notescore.notes.printAll
~nn.get_arg(\sampleline).scoreset.notescore.notes.pop
~nn.get_arg(\sampleline).scoreset.notescore.cut_exceeding_notes;
Mdef.node("monosampler_l1027").get_arg(\sampleline).scoreset.notescore.abs_end
Mdef.node("monosampler_l1027").get_arg(\sampleline).scoreset.notescore.get_rel_notes.printAll
Mdef.node("monosampler_l1027").get_arg(\sampleline).scoreset.update_notes
Mdef.node("monosampler_l1027").get_arg(\sampleline).scoreset.get_notes.printAll

Mdef.node(\snare).get_arg(\freq).vpiano.value

~seq.get_node("pulsepass_l1001").get_arg(\noteline).scoreset.get_notes

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

(
Mdef(\mygroup, Ppar([
	Mdef(\froid),
	Mdef(\bleu),
]))

)

(

Mdef(\snare, Pbind(
	\instrument, \mysnare
));
Mdef(\kick, Pbind(
	\instrument, \mykick2
));
Mdef.sampler(\smp, [ 
	[\nsample, \kick],
	[\nsample, \snare]
	[\nsample, "monosampler_l1063"]
]);
)

~seq.node_manager.add_node_to_default_group(~seq.get_node(\smp))
~a = ~make_nodesampler.(~seq)
~a = ~make_parplayer.(~seq)
~a.name = "plop"
~a.uname = ~a.name
~seq.add_node(~a)
~seq.node_manager.add_node_to_default_group(~a)

~seq.node_manager.default_group.add_children(~a.name)

~a.name = "plop"


{SinOsc.ar}.play


Mdef(\bleu, Pbind(\sustain, Pkey(\sustain)*0.5) <> Mdef("monosampler_l1001", Pbind(
	\stretch, 1
)))

Mdef("monosampler_l1001", Pbind(
	\stretch, 1
))



~seq.save_project("testsave12");
~seq.load_project("testsave12");

~seq.load_project("walking");

~seq.model.livenodepool["monosampler_l1001"].vpattern.play
~seq.mod
Pdef("monosampler_l1001").play

~seq.model.livenodepool[\bleu].set_mode(\noteline)
~pl = ~seq.model.livenodepool[\bleu];
~nl = ~seq.model.livenodepool[\bleu].get_arg(\noteline)
~nl.changed(\notes)
~nl.set_next_notes(~default_noteline, 8)
~nl.set_next_notes_as_current_notes

~mr = ~make_midi_recorder.(~pl, ~seq)
~mr.player_start_tempo_recording

~seq.model.livenodepool["pulsepass_l1027"].node.play
~seq.model.livenodepool["pulsepass_l1029"].node.play
~seq.model.livenodepool["pulsepass_l1029"].node = EventPatternProxy.new;
~seq.model.livenodepool["pulsepass_l1029"].node.source = ~seq.model.livenodepool["pulsepass_l1029"].vpattern
~pp = EventPatternProxy.new;
~pp.source = ~seq.model.livenodepool["pulsepass_l1029"].vpattern
~pp.play

~seq.save_project("testsave4");
~seq.load_project("testsave4");
~seq.panels.side.current_player.uname
~seq.panels.side.current_group.uname
~seq.model.livenodepool["pulsepass_l1007"].play_node
~seq.panels.side.current_player








(
Mdef(\kick, Pbind(
	\instrument, \mykick2
));
)


s.boot

(
SynthDef(\mykick2, { arg out=0, amp=0.1, gate=1, spread=0.5, pan=0, freq=200,
						bodyamp=0.401, bodyat=0.001, bodyrt=0.11,
						popamp=0.401, popat=0.001, poprt=0.11,
						attack=0.01, release=0.1,
						freq2=200
						;
	var ou;
	var freqenv;

	var ou2;
	var freqenv2;
	var ampenv2;
	var ampenv1;

	var ou3;
	var freqenv3;

	var ou4;

	var ou5, ou6, ou7;
	// body
	freqenv = EnvGen.ar(Env.new([ 100, 50, 30 ], [0.001, 0.2]),gate,doneAction:0);
	ou = SinOsc.ar(freqenv);
	ou = ou * EnvGen.ar(Env.new([0,bodyamp,0],[bodyat,bodyrt]),gate,doneAction:0);

	// pop
	freqenv2 = EnvGen.ar(Env.new([ 10, 130, 140, 70 ], [0.001,0.01,0.141]),gate,doneAction:0);
	ou2 = SinOsc.ar(freqenv2);
	ampenv2 = EnvGen.ar(Env.new([0,popamp,0],[popat,poprt]),gate,doneAction:0);
	ou2 = ou2 * ampenv2;

	// click
	freqenv3 = SinOsc.ar(10).range(30,80);
	ou3 = SinOsc.ar(freqenv3);
	ou3 = LPF.ar(ou3, 170, 0.1);
	ou3 = ou3 * EnvGen.ar(Env.new([0,0.71,0],[0.1,0.10]),gate,doneAction:0);

	ou4 = PinkNoise.ar(1);
	ou4 = LPF.ar(ou4, 170, 5.1);
	ou4 = ou4 * EnvGen.ar(Env.new([0,0.001,0],[0.001,0.41],-9),gate,doneAction:0);

	ou = ou + ou2 + ou3 + ou4;
	
	ou = ou * EnvGen.ar(Env.new([0,1,0],[attack,release],[-1,-4]),gate,doneAction:2);
	ou = Splay.ar(ou!2, spread, amp, pan);
	Out.ar(out, ou);

}, metadata:(specs:(
	bodyamp: \amp,
	popamp: \amp,
	bodyat: \attack,
	bodyrt: \attack,
	popat: \attack,
	poprt: \attack,
	attack: \attack,
	release: \attack,
	spread: \spread
))).store;
)



SynthDef(\mykick, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	var freqenv;

	var ou2;
	var freqenv2;
	var ampenv2;
	var ampenv1;

	var ou3;
	var freqenv3;

	var ou4;

	var ou5, ou6, ou7;
	// body
	freqenv = EnvGen.ar(Env.new([ 100, 50, 30 ], [0.001, 0.2]),gate,doneAction:0);
	ou = SinOsc.ar(freqenv);
	ou = ou * EnvGen.ar(Env.new([0,1.401,0],[0.001,0.11]),gate,doneAction:0);

	// pop
	freqenv2 = EnvGen.ar(Env.new([ 10, 1013, 140, 70 ], [0.011,0.03,0.141]),gate,doneAction:0);
	//ou2 = SinOsc.ar(freqenv2+SinOsc.kr(100.1).range(01,15));
	ou2 = SinOsc.ar(freqenv2);
	//ou2 = LPF.ar(ou2, 0170);
	ampenv2 = EnvGen.ar(Env.new([0,0.400,0],[0.0011,0.111]),gate,doneAction:0);
	ou2 = ou2 * ampenv2;
	//ou2 = 0;

	// click
	freqenv3 = SinOsc.ar(10).range(30,80);
	ou3 = SinOsc.ar(freqenv3);
	ou3 = LPF.ar(ou3, 170, 0.1);
	ou3 = ou3 * EnvGen.ar(Env.new([0,7.71,0],[0.1,0.10]),gate,doneAction:0);
	//ou3 = 0;

	ou4 = PinkNoise.ar(1);
	//ou4 = BPF.ar(ou4, 50, 0.4);
	ou4 = LPF.ar(ou4, 170, 5.1);
	ou4 = ou4 * EnvGen.ar(Env.new([0,0.001,0],[0.001,0.41],-9),gate,doneAction:0);

	ou = ou + ou2 + ou3 + ou4;
	ou6 = ou;
	ampenv1 = EnvGen.ar(Env.new([0,0.511,0],[0.0001,0.5],-9),gate,doneAction:0);
	ou = ou + ((ou*7).clip*ampenv1);
	ou = ou + ((ou*7).distort*ampenv1);
	ou5 = ou;
	//ou = LPF.ar(ou5, 0100);
	//ou = ou + BPF.ar(ou5, 0400, 0.1, 0.7);
	//ou = ou + BPF.ar(ou5, 0100, 0.7, 0.7);
	//ou = ou + ou2 + ou3 + ou4;
	//ou = ou/2;
	//ou = ou6 + (ou/1);
	//ou = ou4 + ou2;
	//ou = Klank.ar(`[{ 80.rand + 40 }!18], ou, decayscale:0.2);
	//ou = ou/10 + ou6;
	//ou = ou6;
	//ou = ou6;
	//ou = ou ;
	ou = ou6;

	//ou = Pan2.ar(ou, pan, amp);
	ou = Splay.ar(ou!2, 0.1, amp, 0);
	ou = Compander.ar(ou, ou,
        thresh: MouseX.kr(0.01, 1),
        slopeBelow: -0.70,
        slopeAbove: -1.4,
        clampTime: 0.1,
        relaxTime: 0.1
    );
	ou7 = ou6;
	ou6 = ou;
	ou = BPF.ar(ou6, 0060, 0.8);
	ou = ou + BPF.ar(ou6, 0100, 0.7);
	ou = ou + BPF.ar(ou6, 0200, 0.1, 0.4);
	//ou = Slew.ar(ou, 1000, 1000);
	//ou = ou7;
	ou = ou * 2;
	ou = ou + ((ou*700).tanh*ampenv1);
	ou = FreqShift.ar(HPF.ar(ou,700), -100);
	//ou = LPF.ar(ou, 400) + ou7;
	ou = LPF.ar(ou, 200) + (ou7/8);
	//ou = ou7;
	ou = Compander.ar(ou, ou,
        thresh: MouseX.kr(0.001, 1),
        slopeBelow: 0.11,
        slopeAbove: 0.11,
        clampTime: 0.0004,
        relaxTime: 0.011
    );
	ou = Limiter.ar(ou,amp);
	ou = ou * EnvGen.ar(Env.new([0,0.71,0],[0.107,0.20],[-1,-4]),gate,doneAction:0);
	ou = ou * 1.7;
	DetectSilence.ar(ou, doneAction:2);
	
	Out.ar(out, ou);
}).add;


(
Pdef(\mok, Pbind(
	\instrument, \mykick,
	\freq, 200,
	\dur, Pseq([Pn(1/4,5),Pn(1/4/2,2),Pn(1/4,2)],inf)*1.2,
	//\logato, 0.1,
	\amp, 0.8
)).play;
)

TempoClock.default.tempo = 1

Pbind(\midinote, Pseq([69, 76, 74, 76, 81, 76]), \dur, Pseq(0.5!5++2)).play; // sorry, too trite






(
p = Pspawn(Pbind(
                // Pbind returned by Pfunc is not embedded, just placed in the event
                // So, it can be spawned
        \pattern, Pfunc { Pbind(\degree, Pseries(rrand(0, 10), #[-1, 1].choose, rrand(4, 10)), \dur, 0.125) },
        //\delta, Pwhite(1, 5, inf) * 0.125,
		\dur, 1.0,
        \method, \par
)).play;
)


// Same, using a dictionary of patterns, changing dur rhythm also
(
var     patternChoices = (
        up: { Pbind(\instrument, \pulsepass, \degree, Pseries(rrand(-4, 5), 1, rrand(4, 10)), \dur, 0.125) },
        down: { Pbind(\instrument, \lead2, \rq, 0.7, \wet, 0.5, \octave, 5, \amp, 0.11, \degree, Pseries(rrand(4, 11), -1, rrand(4, 10)), \dur, 0.125 * 4/3) }
);

p = Pdef(\bla, Pspawn(Pbind(
        \pattern, Prand([\up, \down], inf),
        \delta, Pwhite(1, 5, inf) * 0.125,
        \method, \par
), (dict: patternChoices))).play;
)














(
SynthDef(\mysnare, { arg out=0, amp=0.1, gate=1, spread=0.5, pan=0, freq=200,
						bodyamp=0.401, bodyat=0.001, bodyrt=0.11,
						popamp=0.401, popat=0.001, poprt=0.11,
						attack=0.01, release=0.1,
						freq2=200, dist=70,
						noiseamp = 0.2, preamp=1, compthres=0.5
						;
	var ou;
	var freqenv;

	var ou2 = 0;
	var freqenv2;
	var ampenv2;
	var ampenv1;

	var ou3 = 0;
	var freqenv3;

	var ou4 = 0;
	var ou1;
	var ou10 = 0;

	var ou5, ou6, ou7;
	var base = freq;
	var ratio = 0.200;
	// bo
	freqenv = EnvGen.ar(Env.new([ 1.0, 1.5, 1 ], [0.06, 0.1]),gate,levelScale:1,doneAction:0);
	ou = SinOsc.ar(LFSaw.kr(200)*freqenv+(base*[1.01,1,0.99])).sum;
	ou = SinOsc.ar(freqenv+base*[1.01,1,0.99]).sum;
	ou = LFSaw.ar(freqenv*base);
	ou = SinOsc.ar(freqenv*base);
	ou = ou * EnvGen.ar(Env.new([0,bodyamp,0],[bodyat,bodyrt],0),gate,doneAction:0);
	ou1 = ou;
	//ou1 = 0;

	ou10 = WhiteNoise.ar(1);
	ou10 = BPF.ar(ou10, Line.kr(40,01000,0.03), Line.kr(0.6,0.5,0.03));
	ou10 = ou10 * EnvGen.ar(Env.new([0,1.71,0.5,0],[0.0001,0.01,0.092]),gate,doneAction:0);

	ou2 = WhiteNoise.ar(1);
	ou2 = BPF.ar(ou2, 200, 0.1);
	ou2 = ou2 * EnvGen.ar(Env.new([0,1.7,0],[0.0001,0.11]),gate,doneAction:0);

	freqenv = EnvGen.ar(Env.new([ 1.0, 0.0001 ], [0.02]),gate,doneAction:0);
	freqenv2 = EnvGen.ar(Env.new([ 1.0, 0.0001 ], [0.05]),gate,doneAction:0);
	ou3 = WhiteNoise.ar(1);
	//ou4 = BPF.ar(ou3, 7500, 0.1);
	//ou4 = ou4 + BPF.ar(ou3, 2500, 0.2);
	ou4 = LPF.ar(ou3, Line.kr(2500,7000,0.07), 1.2);
	//ou4 = LPF.ar(ou4, freqenv*4900);
	//ou4 = HPF.ar(ou4, freqenv2*8900);
	//ou4 = BRF.ar(ou4, 1200,Line.kr(0.1,1.4,0.15));
	//ou4 = BRF.ar(ou4, 200,0.4);
	ou4 = ou4 * EnvGen.ar(Env.new([0,noiseamp,0],[0.06,0.12],[0,7]),gate,doneAction:0);

	ou = ou + ou2 + ou3 + ou4;
	//ou4 = ou4 + ((ou1*dist).distort/(dist/2));
	ou = ou4 + ou2 + ou1 + ou10;
	//ou = ou2;
	//ou = ou4 ;
	ou = LPF.ar(ou, XLine.kr(15000,6000,0.25));
	//ou = BRF.ar(ou, XLine.kr(100,15100,0.10),0.18);
	//ou = HPF.ar(ou, 100);
	//ou = ou1;
	ou = ou * preamp;

	ou = Compander.ar(ou, ou,
        thresh: compthresh,
        slopeBelow: 2.01,
        slopeAbove: 2.01,
        clampTime: 0.0001,
        relaxTime: 0.00001
    );
	//ou = ou/16;
	ou = ou * EnvGen.ar(Env.new([0,1,0.5,0],[attack,0.1,release],[-1,-9]),gate,doneAction:0);
	ou = FreeVerb.ar(
		ou,
		0.2, // mix 0-1
		0.01, // room 0-1
		0.1 // damp 0-1 duh
	); // fan out...
	//ou = CombL.ar(ou, 1, 0.01, 0.05) * 1;
	//ou = CombL.ar(ou, 1, 0.08, 0.1) * 1;
	//ou = CombL.ar(ou, 1, 0.08, 0.4) * 1;
	DetectSilence.ar(ou,doneAction:2);
	
	ou = Splay.ar(ou!2, spread, amp, pan);
	Out.ar(out, ou);

}, metadata:(specs:(
	bodyamp: \amp,
	popamp: \amp,
	bodyat: \attack,
	bodyrt: \attack,
	popat: \attack,
	poprt: \attack,
	attack: \attack,
	release: \attack,
	noiseamp: \amp,
	preamp: \amp,
	spread: \spread
))).store;
)

(
SynthDef(\mysnare2, { arg out=0, amp=0.1, gate=1, spread=0.5, pan=0, freq=200,
						bodyamp=0.401, bodyat=0.001, bodyrt=0.11,
						popamp=0.401, popat=0.001, poprt=0.11,
						attack=0.01, release=0.1,
						freq2=200
						;
	var ou;
	var freqenv;

	var ou2 = 0;
	var freqenv2;
	var ampenv2;
	var ampenv1;

	var ou3 = 0;
	var freqenv3;

	var ou4 = 0;

	var ou5, ou6, ou7;
	var base = 200;
	var ratio = 0.200;
	// body
	freqenv = EnvGen.ar(Env.new([ 0.9, 0.1, 0 ], [0.1, 0.1]),gate,levelScale:base/2,doneAction:0);
	ou = SinOsc.ar(LFSaw.kr(200)*freqenv+(base*[1.01,1,0.99])).sum;
	ou = ou * EnvGen.ar(Env.new([0,bodyamp,0],[bodyat,bodyrt]),gate,doneAction:0);

	ou2 = WhiteNoise.ar(1);
	ou2 = ou2 * EnvGen.ar(Env.new([0,0.21,0],[0.001,0.02]),gate,doneAction:0);

	ou3 = WhiteNoise.ar(1);
	ou3 = BPF.ar(ou3, 70, 0.1);
	ou3 = ou3 * EnvGen.ar(Env.new([0,0.7,0],[0.01,0.11]),gate,doneAction:0);

	ou4 = WhiteNoise.ar(1);
	ou4 = HPF.ar(ou4, 400);
	ou4 = ou4 * EnvGen.ar(Env.new([0,0.1,0],[0.01,0.21]),gate,doneAction:0);

	ou = ou + ou2 + ou3 + ou4;
	ou = ou4;

	ou = Compander.ar(ou, ou,
        thresh: MouseX.kr(0.001, 1),
        slopeBelow: 0.41,
        slopeAbove: 0.01,
        clampTime: 0.4,
        relaxTime: 0.11
    );
	ou = ou/16;
	
	ou = ou * EnvGen.ar(Env.new([0,1,0],[attack,release],[-1,-4]),gate,doneAction:2);
	ou = Splay.ar(ou!2, spread, amp, pan);
	Out.ar(out, ou);

}, metadata:(specs:(
	bodyamp: \amp,
	popamp: \amp,
	bodyat: \attack,
	bodyrt: \attack,
	popat: \attack,
	poprt: \attack,
	attack: \attack,
	release: \attack,
	spread: \spread
))).store;
)
