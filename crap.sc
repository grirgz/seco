s.boot
s.gui
s.makeGui
s.scope
s.freqscope
s.class.methods.do(_.postln)
s.quit
s.plotTree
s.meter

(
~a = Pbind(
	\freq, Prout({
			Pseg([3,7,5,1,10],1.7,'linear',inf).embedInStream;	
		}),
	\dur, 2,
	\bla, Pfunc({ arg e; e[\freq].postln; })
);
~a.play
)

(
~a = Pbind(
	\freq, Prout({
			var ti = 1.7, dur = 2;
			100.do { arg i;
				(i*(dur/ti)).debug("index");
				[3,7,5,1,10].blendAt(i*(dur/ti)).yield;
			};
		}),
	\bla, Pfunc({ arg e; e[\freq].postln; })
);
~a.play
)
(
~a = Pbind(
	\freq, Pseq([7,10,2,5],inf),
	\bla, Pfunc({ arg e; e[\freq].postln })
);
~a.play
)
(
~a = Pbind(
	\freq, Pseq([207,310],inf)
);
~a.play
)


(
~a = (
	a: "plopplpopo",
	bla: { arg self; self.a.postln }
);
~a.bla = { arg x; x.postln; "hhh".postln };
~a.bla


)


~a = [4,7];
~a.wrapPut(44, 47)

45 % ~a.size

List[4,7,6].isList

(
~a = CCResponder({ |src,chan,num,value|
		[src,chan,num,value].debug("==============CCResponder");
},

		nil, // any source
		nil, // any channel
		nil, // last knob
		nil // any value

)
)

(
        c = TouchResponder({ |src,chan,val|
                [src,chan,val].postln;
                },
                nil, // any source
                nil, // any channel
                nil // any value
        )
)


(
        c = ProgramChangeResponder({ |src,chan,val|
                [src,chan,val].postln;
                },
                nil, // any source
                nil, // any channel
                nil // any value
        )
)


(
c = NoteOnResponder({ |src,chan,note,vel|
                [src,chan,note,vel].postln;
        });
)
NoteOnResponder.removeAll




(
SynthDef(\echo, { arg out=0, maxdtime=0.2, dtime=0.2, decay=2, gate=1;
        var env, in;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        in = In.ar(out, 2);
        XOut.ar(out, env, CombL.ar(in * env, maxdtime, dtime, decay, 1, in));
}, [\ir, \ir, 0.1, 0.1, 0]).add;

SynthDef(\distort, { arg out=0, pregain=40, amp=0.2, gate=1;
        var env;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        XOut.ar(out, env, (In.ar(out, 2) * pregain).distort * amp);
}, [\ir, 0.1, 0.1, 0]).add;

SynthDef(\wah, { arg out=0, gate=1;
        var env, in;
        env = Linen.kr(gate, 0.05, 1, 0.4, 2);
        in = In.ar(out, 2);
        XOut.ar(out, env, RLPF.ar(in, LinExp.kr(LFNoise1.kr(0.3), -1, 1, 200, 8000), 0.1).softclip * 0.8);
}, [\ir, 0]).add;
)

(
var p, q, r, o;
p = Pbind(\degree, Prand((0..7),12), \dur, 0.3, \legato, 0.2);

q = Pfx(p, \echo, \dtime, 0.2, \decay, 3);

r = Pfx(q, \distort, \pregain, 20, \amp, 0.25);

o = Pfx(r, \wah);

Pseq([p, q, r, o], 2).play;
)

~a = EventPatternProxy.new
~p = Pbind(\degree, Pseq([0,1,2,3,4,5],inf), \dur, 0.3, \legato, 0.2);
~a.source = ~p
~a.play;
~a.quant = 0.3 * 6;
~a.source = Pfx(~p, \echo, \dtime, 0.2, \decay, 3)
~a.source = Pfx(~p, \distort, \pregain, 180, \amp, 0.25);

s.quit

s.boot

~p = Pbind(\degree, Prand((0..7),inf), \dur, 0.3, \legato, 0.2, \out, 0);
~p.play
~e = Pbind(\instrument, \echo, \legato, 0.2, \out, 3);
~e.play
Synth(\echo, addAction:\addToTail)



(
SynthDef(\echo, { arg out=0, maxdtime=0.2, dtime=0.2, decay=2, gate=1;
        var env, in;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        in = In.ar(out, 2);
        XOut.ar(out, env, CombL.ar(in * env, maxdtime, dtime, decay, 1, in));
}, [\ir, \ir, 0.1, 0.1, 0]).add;

SynthDef(\distort, { arg out=0, pregain=40, amp=0.2, gate=1;
        var env;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        XOut.ar(out, env, (In.ar(out, 2) * pregain).distort * amp);
}, [\ir, 0.1, 0.1, 0]).add;

SynthDef(\wah, { arg out=0, gate=1;
        var env, in;
        env = Linen.kr(gate, 0.05, 1, 0.4, 2);
        in = In.ar(out, 2);
        XOut.ar(out, env, RLPF.ar(in, LinExp.kr(LFNoise1.kr(0.3), -1, 1, 200, 8000), 0.1).softclip * 0.8);
}, [\ir, 0]).add;
)

(
var p, q, r, o;
p = Pbind(\degree, Prand((0..7),12), \dur, 0.3, \legato, 0.2);

q = Pfxb(p, \echo, \dtime, 0.2, \decay, 3);

r = Pfxb(q, \distort, \pregain, 20, \amp, 0.25);

o = Pfxb(r, \wah);

Pseq([p, q, r, o], 2).play;
)


// test order of effect chain
(
SynthDef(\echo, { arg out=0, maxdtime=0.2, dtime=0.2, decay=2, gate=1;
        var env, in;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        in = In.ar(out, 2);
        XOut.ar(out, env, CombL.ar(in * env, maxdtime, dtime, decay, 1, in));
}, [\ir, \ir, 0.1, 0.1, 0]).add;

SynthDef(\addbeeps, { arg out=0, gate=1;
        var env;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        XOut.ar(out, env, (In.ar(out, 2) + (LFPulse.kr(1, 0, 0.3) * SinOsc.ar(1000) * 0.4) ));
}, [\ir, 0.1, 0.1, 0]).add;
)

(
var a;
a = Pbind(\degree, Prand((0..7),12), \dur, 0.3, \legato, 0.2);
a = Pfxb(a, \echo, \dtime, 0.2, \decay, 3);
a = Pfxb(a, \addbeeps);
a.play;
)

(
var a;
a = Pbind(\degree, Prand((0..7),12), \dur, 0.3, \legato, 0.2);
a = Pfxb(a, \addbeeps);
a = Pfxb(a, \echo, \dtime, 0.2, \decay, 3);
a.play;
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
b.setEnv(Env.adsr(0.02, 0.2, 0.25, 1, 1, 0));

// make the first point unmoveable
(
b.setEditable(0,false);
)

(
~adsr_event_to_env = { arg adsr;
	Env.performWithEnvir(\adsr, adsr)
};

~make_env_view = { arg parent, default_val;
	var env, view;

	env = EnvelopeView(parent, Rect(0, 0, 230, 80))
        .drawLines_(true)
        .selectionColor_(Color.red)
        .drawRects_(true)
        .resize_(5)
        .step_(0.05)
        .thumbSize_(5);

	view = (
		env: env,
		set_env: { arg self, env_val;
			env.setEnv( ~adsr_event_to_env.(env_val) )
		}
	);

	view.set_env(default_val);
	view;

};

~adsr = (
	attackTime:0.1,
	decayTime:0.2,
	sustainLevel:0.3,
	releaseTime:0.4,
	curve:0
);

)

~win = Window.new;
~make_env_view.(~win, ~adsr);
~win.front


Env.adsr(0.02, 0.2, 0.25, 1, 1, 0).asArray
Env.adsr(0.02, 0.2, 0.25, 1, 1, 0).plot
Env.new( Env.adsr(0.02, 0.2, 0.25, 1, 1, 0).asArray ).plot
Env.adsr(0.02, 0.2, 0.25, 1, 1, 0).asArray.asEnv
Env.adsr(0.02, 0.2, 0.25, 1, 1, 0).array
Env.
Env( Env.adsr(0.02, 0.2, 0.25, 1, 1, 0).asArray ).plot


~a = { arg a, b; [a,b].postln; };
~e = (a:4, d:7);
~a.valueWithEnvir(~e)
Env.performWithEnvir(\adsr, (attackTime:7))
