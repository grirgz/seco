(
~mpdef = { arg name, pat;
	var node, main;

	if(pat.notNil) {
		if(~seq.notNil && {pat.notNil}) {
			main = ~seq;

			if(pat.class == Pbind) {
				if(main.node_exists(name)) {
					node = main.get_node(name);
					node.set_input_pattern(pat);
					Pdef(name, node.vpattern);
				} {
					node = ~make_player_from_pbind.(main, pat);
					if(node.notNil) {
						node.name = name;
						node.uname = name;
						main.add_node(node);
						main.focus_mpdef(node);
						Pdef(name, node.vpattern);
					} {
						"ERROR: player could not be created".debug;
						nil;
					};
				}
			} {
				"ERROR: pat class not understood".debug;
				Pdef(name, pat);
			}

		} {
			"ERROR no seq".debug;
			Pdef(name, pat);
		}
	} {
		Pdef(name)
	}
};
)


(
a = Pbind(
	\bla, \pulsepass,
	\instrument, Pkey(\bla),
	\dur, 0.5,
	\amp, 0.2
)
)

a.patternpairs.clump(2)

(
b = Pdef(\rah, Pbind(
	\bla, \pulsepass,
	\degree, Pseq([1,2,3,4,5,6,7,8],inf),
	\instrument, Pkey(\bla),
	\dur, 0.5,
	\amp, 0.2
))
)
b = Pdef(\rah, a);
a.patternpairs[5].key
a.play
b.play
b.quant = 2
b.source.patternpairs

c = Pbindef(\rah, \dur, 1.0); 
c.play
Pdef(\rah).play
Pchain

(
SynthDef(\lead, {	arg out=0, freq = 100, pan=0, amp=0.1, detune=1.1, gate=1, rq=0.1, fratio = 1, fbase=20; 
	var ou, filtenv;
	//gate = LFPulse.ar(1);
	ou = LFSaw.ar(freq * [1, detune]).sum;
	filtenv = EnvGen.ar(Env.adsr(0.01,0.35,0.07,0.3), gate, freq * fratio, fbase, doneAction:0);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.4,0.1,0.4), gate, doneAction:2);
	ou = RLPF.ar(ou, filtenv, rq);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add
)
(
SynthDef(\lead2, {	arg out=0, freq = 100, pan=0, amp=0.1, mdetune=1.004, gate=1, rq=0.1, fratio = 1, fbase=20, wet=1, fbfreq=100, fbamp=0.8, fbpamp=1, rt=0.4; 
	var fb, ou, filtenv;
	//gate = LFPulse.ar(1);
	ou = LFSaw.ar(freq * [1, mdetune]).sum;
	filtenv = EnvGen.ar(Env.adsr(0.01,0.25,0.07,0.3), gate, freq * fratio, fbase, doneAction:0);
	ou = RLPF.ar(ou, filtenv, rq);
	fb = LocalIn.ar(1) + ou;
	fb = HPF.ar(fb, fbfreq);
	LocalOut.ar(fb * fbamp);
	fb = Limiter.ar(fb, amp);
	fb = SelectX.ar(wet, [ou, fb*fbpamp]);
	fb = fb * EnvGen.ar(Env.adsr(0.01,0.4,0.1,rt), gate, doneAction:2);
	fb = Pan2.ar(fb, pan, amp);
	Out.ar(out, fb);
}).add
)
Splay

(
~mpdef.(\ko, Pbind(
	\instrument, \lead2,
	\degree, Pseq([1,4,4,1],inf),
	\octave, 3,
	\legato, 0.7,
	\fratio, 0.1,
	\fbase, 100,
	\rq, Pseq([1,1.7],inf),
	\dur, 0.5,
	\detune, 1.007,
	\amp, 0.2
));
)

BusPool
Pdef(\nio, Pn(Pdef(\ko))).play
Pdef(\fio).play

Pdef(\fio, Pbind(
	\instrument, \lead2,
	\degree, Pseq([1,4,4,1],inf),
	\octave, 3,
	\legato, 0.7,
	\fratio, 0.1,
	\fbase, 400,
	\rq, Pseq([0.4,0.7],inf),
	\dur, 0.5,
	\wet, 0.7,
	\detune, 1.007,
	\amp, 0.2
));

Pbind(\bla, 1).class == Pbind
Pdef(\bla).class == Pbind
1.degreeToKey(Scale.major)
(
"/home/ggz/code/sc/seco/main.sc".load;
~seq = ~mk_sequencer.value;
~mpdef.(\ko, Pbind(
	\instrument, \lead2,
	\freq, Pseq([44,55,55,54,47,54],inf).midicps,
	\legato, 0.7,
	\fratio, 0.1,
	\fbase, 100,
	\rq, Pseq([1,1.7],inf),
	\dur, 0.5,
	\detune, 1.007,
	\amp, 0.2
));
~seq.edit_mpdef(\ko)
)
~seq.get_node(\rah).vpattern.play
~seq.edit_mpdef(\nia)
GUI.swing

Pdef(\kah, ~seq.get_node(\rah).vpattern)
Pdef(\kah).play


b = EventPatternProxy.new;
b.source = Pbind(\freq, 100)
(
a = Pbind(
	\freq, Pkey(\freq)
) <> b
)
a.trace.play


(
(Pbind(
	\freq, Prout { arg ev; 2.do { ev = ev[\freq].yield } }
) <> Pbind(
	\freq, Pseq([100,200],inf)
)).play
)

(
SynthDef(\brass, {	arg out=0, freq = 100, pan=0, amp=0.1, mdetune=1.004, gate=1, rq=0.1, fratio = 1, fbase=20, rt=0.4,
		vibfreq=4, vibratio=0.02, pwidth=0.5, pwdetune=0.1,
		mix=0.71, room=0.5, damp=0.5;
	var vibrato, ou, filtenv;
	//gate = LFPulse.ar(1);
	vibrato = SinOsc.kr(vibfreq) * vibratio * freq * EnvGen.kr(Env.dadsr(0.1,0.1,0.1,0.5,0.1), gate);
	//vibrato = 0;
	ou = LFPulse.ar(freq * [1, mdetune] + vibrato, 0, pwidth * [1,1+pwdetune]).sum;
	filtenv = EnvGen.ar(Env.adsr(0.3,0.15,0.77,0.1,1,[4,0]), gate, freq * fratio, fbase, doneAction:0);
	ou = RLPF.ar(ou, filtenv, rq);
	ou = ou * EnvGen.ar(Env.adsr(0.1,0.1,0.8,rt), gate, doneAction:0);
	ou = FreeVerb.ar(
		ou,
		mix, // mix 0-1
		room, // room 0-1
		damp // damp 0-1 duh
	); // fan out...
	DetectSilence.ar(ou, doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)

(
"/home/ggz/code/sc/seco/main.sc".load;
~seq = ~mk_sequencer.value;
~mpdef.(\bleu, Pbind(
	\instrument, \brass
));
~seq.edit_mpdef(\bleu)
)
