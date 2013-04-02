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

~mod = [1,2,3,4,5,6];
~f = { ev = ~mod.blendAt((ev[\elapsed]/ev[\segdur]) % (~mod.size -1)).yield;

(
~a = Pbind(
	\curnote, Prout({
		var val, idx;
		10.do {
			idx = 0;
			val = ~rec[idx];
			while( { val.notNil } , { 
				val.yield;
				idx = idx + 1;
				val = ~rec[idx];
			});
		};
	}),
	#[midinote, dur, sustain], Pfunc({ arg ev;
		ev[\curnote].debug("curnote");
		#[note, dur, sustain].collect { arg key; ev[\curnote][key] }.debug("hein");
	}),
	\segdur, 1,
	\elapsed, Ptime.new,
	\mod, Prout({ arg ev;
		10000.do {
		}
	})
);
~a.trace(\mod).play
)

	\mod, Pfunc({ arg ev;
		~mod.blendAt((ev[\elapsed]/ev[\segdur]) % (~mod.size -1))
	})
(
~bla = (
bla:4

)
)
~bla.tryPerform(\bla)


(
r = Routine { arg in;

	10.do { arg i;
		in.postln;
		in = i.yield;
	}

};
)
r.next
r


(
~rec = [
	(
		note: 64,
		sustain: 0.1,
		dur: 0.5
	),
	(
		note: 65,
		sustain: 0.1,
		dur: 0.4
	),
	(
		note: 66,
		sustain: 0.1,
		dur: 0.9
	),
	(
		note: 67,
		sustain: 0.1,
		dur: 0.1
	),
	(
		note: 68,
		sustain: 0.1,
		dur: 1.5
	),
	(
		note: 69,
		sustain: 0.1,
		dur: 0.6
	)
];
)

(bla:44).range


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

SynthDef(\echo, { arg out=0, in=0, maxdtime=0.2, release=1, dtime=0.2, decay=2, gate=1;
        var env, ou;
        env = Linen.kr(gate, 0.05, 1, decay, 2);
        in = In.ar(in, 2);
		ou = CombL.ar(in, maxdtime, dtime, decay, 1, in);
		//DetectSilence.ar(ou,0.001,0.1,doneAction:2);
        Out.ar(out, ou);
}, [\ir, \ir, \ir, 0.1, 0.1, 0]).add;

SynthDef(\addbeeps, { arg out=0, in=0, gate=1;
        var env, ou;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
		ou = In.ar(in, 2) + (LFPulse.kr(1, 0, 0.3) * SinOsc.ar(1000) * 0.4);
        Out.ar(out, ou);
}, [\ir, 0.1]).add;

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

(
~a = EventPatternProxy.new;
~b = EventPatternProxy.new;
~p = Pbind(\degree, Pseq([0,1,2,3,4,5],inf), \dur, 0.3, \legato, 0.2);
~q = Pbind(\degree, Pseq([0,1,2,3,4,5]-2,inf), \dur, 0.6, \legato, 0.1, \amp, 0.5);
)
(
~a.source = Pfxb(~p, \echo, \amp, 0.25);
~a.play;
~a.source = Pfxb(~p, \echo, \amp, 0.25);
~a.play;
)
(
~b.source = Pfxb(~q, \addbeeps, \pregain, 180, \amp, 0.25);
~b.play;
~b.source = Pfxb(~q, \addbeeps, \pregain, 180, \amp, 0.25);
~b.play;
)
~a.source = ~p
~b.source = ~q;
~a.quant = 0.3 * 6;

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
}, [\ir, 0.1]).add;
)

(
var a;
a = Pbind(\degree, Prand((0..7),12), \dur, 0.3, \legato, 0.2);
a = Pfx(a, \echo, \dtime, 0.2, \decay, 3);
a = Pfx(a, \addbeeps);
a.play;
)

(
var a;
a = Pbind(\degree, Prand((0..7),12), \dur, 0.3, \legato, 0.2);
a = Pfx(a, \addbeeps);
a = Pfx(a, \echo, \dtime, 0.2, \decay, 3);
a.play;
)

(
a = Pbind(\degree, Prand((0..7),12), \dur, 0.3, \legato, 0.2);
b = Pbind(\degree, Prand((0..7),12)-6, \dur, 0.3, \legato, 0.2);
p = Pfxb(a, \addbeeps);
q = Pfxb(b, \echo, \dtime, 0.2, \decay, 3);
Ppar([p,q]).play;
)




(
// use shift-click to keep a node selected
w = Window("envelope", Rect(150 , Window.screenBounds.height - 250, 250, 100)).front;
w.view.decorator = FlowLayout(w.view.bounds);

b = EnvelopeView(w, Rect(0, 0, 230, 80))
        .drawLines_(false)
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
b.grid = 0.1@0.1;
b.gridColor = Color.red

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


~a = (plop: { arg self; self.postln; "tro".postln });
~b = Dictionary[ \plop -> { arg self; self.postln; "dess".postln } ]
~b[\plop].value
~b[\plop]
~a[\plop].value(~a)

~c = ~b.as(Event)
~c.plop



(
SynthDef(\ploo, { arg out, sustain=1, gate=1;
	var ou;
	ou = SinOsc.ar(XLine.kr(10,500, sustain*2, doneAction:2));
	//ou = SinOsc.ar(300);

	
	Out.ar(out, ou * EnvGen.kr(Env.cutoff(0.1), gate, doneAction:2));

}).add;
)


(
Pbind(
	\instrument, \ploo,
	\legato, 0.1,
	\dur, 1.1
).play

)freq



(
var winenv;
// a custom envelope 
winenv = Env([0, 1, 0], [0.5, 0.5], [8, -8]);
z = Buffer.sendCollection(s, winenv.discretize, 1);

SynthDef(\sin_grain_test, {arg gate = 1, amp = 1, envbuf;
        var pan, env, freqdev;
        // use mouse x to control panning
        pan = MouseX.kr(-1, 1);
        // use WhiteNoise and mouse y to control deviation from center pitch
        freqdev = WhiteNoise.kr(MouseY.kr(0, 400));
        env = EnvGen.kr(
                Env([0, 1, 0], [1, 1], \sin, 1),
                gate,
                levelScale: amp,
                doneAction: 2);
        Out.ar(0,
                GrainSin.ar(2, Dust.kr(10), 0.1, 440 + freqdev, pan, envbuf) * env)
        }).send(s);

)
s.scope
// use built-in env
x = Synth(\sin_grain_test, [\envbuf, -1])

// switch to the custom env
x.set(\envbuf, z)
x.set(\envbuf, -1);

x.set(\gate, 0);

(
Ndef(\plop, { 
	var ou;
	ou = GrainSin.ar(2, Dust.kr(1), 0.01, WhiteNoise.kr(1)+SinOsc.kr(10+SinOsc.kr(1)*5)*240 , 0, -1) ;
	ou = ou + GrainSin.ar(2, Dust.kr(1), 0.1, SinOsc.kr(100+SinOsc.kr(1)*5)*440 , 0, -1) ;
	10.do { arg i; ou = ou + GrainSin.ar(2, Dust.kr(i), 0.1, SinOsc.kr(100+SinOsc.kr(1)*5)*440 , 0, -1) } ;
	ou = LPF.ar(ou, SinOsc.kr(2)*100+400);
})
)

(
Ndef(\plop, { 
	var ou, tri, tri2;
	ou = SinOsc.ar( 200*[0.98, 0.85, 1.14, 4.54, 3.42] );
	tri = Dust.kr(5);
	tri2 = Dust.kr(SinOsc.kr(1)*5+15);
	ou = SinOsc.ar(200*WhiteNoise.kr(0.1)+TRand.kr(50,400, tri2)) * EnvGen.kr( Env([600, 700, 100, 200]/1000, [0.4, 0.2, 0.5], 'lin'), gate:tri ) * 2;
	ou = Pan2.ar(ou, SinOsc.kr(TRand.kr(0.1,10,tri)));
})
)


s.sendMsg("/b_allocRead", 0, "sounds/a11wlk01.wav");

(
Ndef(\plop, {
	var ou , tri;
	tri = Impulse.kr(8);
	ou = BufRd.ar(1, 0, SinOsc.ar(0.1) * BufFrames.ir(0));
	ou = Latch.ar(ou, tri);
})

)

(
Ndef(\plop, {
	var ou , tri, la;
	tri = Impulse.kr(8);
	la = Latch.ar(WhiteNoise.kr(1), tri) * 2;
	ou = SinOsc.ar(200+la);
})

)
Ndef(\plop).play

Server.internal.boot;
(
// used to lag pitch
{
        SinOsc.ar(              // sine wave
                Ramp.kr(                        // lag the modulator
                        LFPulse.kr(4, 0, 0.5, 50, 400), // frequency modulator
                        Line.kr(0, 1, 15)                               // modulate lag time
                ), 
                0,      // phase
                0.3     // sine amplitude
        ) 
}.scope;
)

// Compare
(
var pulse;
{
        pulse = LFPulse.kr(8.772);
        Out.kr(0,[Ramp.kr(pulse, 0.025), Lag.kr(pulse, 0.025), pulse]);
}.scope;
)
LADPSA.listPlugins;



(
Pbind(
	\plop, 100,
	\freq, Prout({ arg ev; ev[\plop] })
).play
)


(
f = { |array|
       var     curves = Array(array.size div: 4 - 1);
       (6, 10 .. array.size).do { |i|
               if(array[i] == 5) {
                       curves.add(array[i+1])
               } {
                       curves.add(Env.shapeNames.findKeyForValue(array[i]))
               }
       };
       Env(array[0, 4 .. ], array[5, 9 .. ], curves,
               if(array[2] == -99) { nil } { array[2] },
               if(array[3] == -99) { nil } { array[3] }
       );
};

f.(Env.linen.asArray).postcs
)


(
3.do { arg i;
	SynthDef(\synth ++ i, { Out.ar(0, SinOsc.ar(100*(i+1))*EnvGen.kr(Env.perc, doneAction:2)) }).add
}
)
(
p = #[ o, b, e];
~synths = (
	o: \synth0,
	b: \synth1,
	e: \synth2
);
t = Task({
    p.do({arg i;
        Synth(~synths[i]);
        1.wait;
	})
})
)
       
       
t.start;

p.do(_.dump)



{Impulse.ar(EnvGen.kr(Env([10, 1700, 300], [0.1, 2]), doenAction: 2), 0.2)}.play



(
{
       var randomFreq, ampEnv, riseCurve, doAdder, doArray;
       doAdder = 0;
       doArray = [77, 74, 72, 70, 65, 62, 60, 58, 53, 50, 46, 34].midicps;
       randomFreq = EnvGen.kr(Env([1,1,0.007], [8, 6], [0, -4]), 1);
       ampEnv = EnvGen.kr(Env([0.07,0.07,0.21], [8, 6], [0, 1]), 1);
       doArray.do({ |item| doAdder = Pan2.ar(Saw.ar((LFNoise2.kr(1.3, 100, 230)*randomFreq) +
               EnvGen.kr(Env([0,0,item],[8, 6], [0, -3]), 1)), LFNoise2.kr(1.3)) + doAdder; });
       Out.ar(0, doAdder*ampEnv);
}.play
)


{t="With my prayers";999.do{|i|{MoogFF.ar(Saw.ar(t[i.postln%15].ascii),i%99*99)*EnvGen.kr(Env.perc,doneAction:2)}.play;0.2.wait}}.fork
{t="f ckfl fmf a ei";999.do{|i|{MoogFF.ar(Saw.ar(t[i.postln%15].ascii),i%99*99)*EnvGen.kr(Env.perc,doneAction:2)}.play;0.2.wait}}.fork


// resize behaviours
// use the PopUpMenus to mix resize modes
(
var a;

a = { |i|
        var w, b, x,k,t,p;
        k=i;
        i = i + 1;
        w = Window("resize:"+i, Rect(10 + (k%3 * 220), Window.screenBounds.height- [250,460,670].at(k/3), 200, 180));
        b = w.view.bounds;
        x = CompositeView(w, w.view.bounds.insetBy(20,20))
                .background_(Color.rand)
                .resize_(i);
        
        y = CompositeView(x, x.bounds.moveTo(0,0).insetBy(20,20)) 
                .background_(Color.rand)
                .resize_(i);
                
        y.decorator = FlowLayout(y.bounds).gap_(0.0 @ 0.0);

        t = StaticText(y, Rect(0, 0, 40, 40))
                .background_(Color.rand.alpha_(0.8))
                .resize_(i)
                .string_(i)
                .font_(Font("Helvetica", 26));
        
        p=PopUpMenu(y,40@40).items_((1..9).collect(_.asString)).value_(i-1).resize_(i)
                        .action_{|m| t.string_((m.value+1).asString); [p,t].do(_.resize_(m.value+1))};
        
        w.front;
        w.onClose = {a.do(_.close) };

} ! 9;
)


// the popupmenu contains the various modes

(
w = Window("soundfile test", Rect(200, 200, 720, 250));

p = PopUpMenu(w, Rect(10,10,80,24))
        .items_( Array.fill(9, {arg i; (i+1).asString;}) )
        .action_({ arg sbs;
                a.resize_(sbs.value+1);
        });

f = SoundFile.new;
f.openRead("sounds/a11wlk01.wav".absolutePath);

a = SoundFileView(w, Rect(10,40, 700, 180))
        .soundfile_(f)
        .readWithTask(0, f.numFrames, showProgress: false )
        .resize_(1);

w.front;
)




TempoClock.default.tempo
TempoClock.default.beatsPerBar
EventPatternProxy.defaultQuant

TempoClock.default.nextTimeOnGrid(1,-1)

~b = TempoClock.default.beats
TempoClock.nextBar(~b)
TempoClock.nextBar()
~b

TempoClock.default.nextBar(~b)
~rel_nextTimeOnGrid.(6.1,2)
Main.version
(
~rel_nextTimeOnGrid = { arg beats, quant = 1, phase = 0;
				var baseBarBeat = TempoClock.baseBarBeat;
                if (quant == 0) { beats + phase };
                if (phase < 0) { phase = phase % quant };
                roundUp(beats - baseBarBeat - (phase % quant), quant) + baseBarBeat + phase
};
)

(3) % 5

[\bla, 54] ++ List[\truc, 58]












(
~width = 900;
~height = 100;
~beats = 8;
w = Window.new("ParaSpace", Rect(10, 500, ~width+50, ~height+50)).front;

a = ParaSpace.new(w, bounds: Rect(15, 15, ~width, ~height));
)


a.createNode(180,130)

a.deleteNode(0)

a.createNode(122,33)


(
var sum = 0;
var totaldur = 0, minnote=127, maxnote=0;
a.clearSpace;
~default_noteline.do { arg no, idx;
	minnote = min(no.midinote, minnote);
	maxnote = max(no.midinote, maxnote);
	totaldur = no.dur + totaldur
};

[totaldur, minnote, maxnote].debug("stat");
~default_noteline.do { arg no, idx;
	[idx, no].debug("note");
	a.createNode(sum/totaldur * (totaldur/~beats) * ~width, no.midinote.linlin(minnote-1,maxnote+1,0,~height));
	sum = sum + no.dur;
};

)


(
a.setBackgrDrawFunc_({
var se = ~beats;
var wi = ~width/se;
var he = ~height;

Pen.color = Color.gray(0.7);
Pen.addRect(Rect(1*wi,0,2*wi,he));
Pen.fill;

Pen.beginPath;
Pen.color = Color.black;
se.do{|i|
		Pen.line((i*wi)@0, (i*wi)@(he+10));
};
Pen.stroke;

});
)
(
a.setBackgrDrawFunc_({

Pen.color = Color.blue;

Pen.beginPath;
20.do{|i| 
	if( i>8 and: (i<14), {
		Pen.fillColor = Color.red;
		Pen.line((i*20)@0, (i*20)@280);
	});
};
Pen.fill;
Pen.method

Pen.beginPath;
20.do{|i| 
	if( i>8 and: (i<14), {
	}, {
		Pen.color = Color.blue;
		Pen.line((i*20)@0, (i*20)@280);
	});
	
};
Pen.color = Color.red;

2.do{|i| Pen.line(0@(i*20), 380@(i*20))};

});
)
(
a.setBackgrDrawFunc_({


});
)


(
w=Window.new.front;
g = EZKnob( w,        // parent
            50@90,    // bounds
            " test ", // label
            \freq,    // controlSpec
            { |ez| (ez.value.asString ++" is the value of " ++ ez).postln } // action
);
g.setColors(Color.grey, Color.white)
);

// Simplest version, no parent view, so a window is created
(
        g = EZKnob(label:" test ");
        g.action_({ |ez| (ez.value.asString ++" is the value of " ++ ez).postln });
);

MasterEQ(2); 
Quarks.gui

Note("C3").freq
Note(freq:440).name
Note(freq:272).name




(

w = Window( "RoundButton skins" ).front;

w.addFlowLayout;


RoundButton.skin = nil; // remove old one(s)


RoundButton.useWithSkin( 

( radius: 2,

extrude: false,

background: Color.gray(0.3),

font: Font( Font.defaultSansFace, 11 ).boldVariant,

stringColor: Color.yellow

), {

a = RoundButton( w, 60@18 ).label_( "skinned" );

b = RoundButton( w, 40@18 ).label_( "bye" );

});


c = RoundButton( w, 60@18 ).label_( "default" );

)



( // random gradients

x = { Color.rand(0,0.8).alpha_( [1.0.rand ** 2, 1].choose ) };

w = Window( "gradients", Rect(200,200, 250, 120 ) ).front.decorate;

7.do({ |i| RoundSlider( w, 30@100 )

.knobColor_( Gradient( x.(), x.(), [\h, \v].choose ) )

.background_( Gradient( x.(), x.(), [\h, \v].choose ) )

.hilightColor_( Gradient( x.(), x.(), [\h, \v].choose ) )

.value_( i.linlin( 0,6,0.2,0.8) )

});

)



(

w = Window( "knobSize", Rect(100,100, 120, 250 ) ).front.decorate;

c = RoundSlider( w, 50@200 ).value_(1).knobSize_( 1 );

d = RoundSlider( w, 50@200 ).value_(1).knobSize_( 1 );

c.action_({ |sl| d.knobSize = sl.value; }); // moving the slider changes the knobSize of the other

d.action_({ |sl| c.knobSize = sl.value; });

)



//// example 1: orientation and styling

(

w = Window( "string", Rect(100,100, 216, 180 ) ).front;

w.addFlowLayout;

c = RoundSlider( w, 50@150 );

d = RoundSlider( w, 150@50 );

c.string = "String 1";

d.string = "String 2";

)


( // change color

c.stringColor = Color.green(0.5);

d.stringColor = Color.green(0.5);

)


( // change font

c.font = Font( "Times-Bold", 12 );

d.font = Font( "Times-Bold", 12 );

)


// change orientation (\v, \h, \up, \down or an angle)

c.stringOrientation = \v;

c.stringOrientation = \h;

c.stringOrientation = \up;

c.stringOrientation = 0.3pi;

c.stringOrientation = pi; // upside down


// change alignment

d.align = \left;

d.align = \right;

d.align = \center;


////

//// example 2: dynamic string

(

w = Window( "RoundSlider", Rect( 300,150,220,220) ).front;  

b = RoundSlider( w,  Rect( 30,30,160,40) ).value_(0.75)

.action_({ |sl| sl.string = "value: %".format(sl.value.round(0.001) ); });

b.stringColor = Color.blue(0.25).alpha_(0.75);

b.doAction;

)


w = ServerRecordWindow( s );
MIDIWindow();


(
w = Window.new("EZScroller test", Rect(100, 400,200, 100)).front;       
// 5 displays
v = { |i| DragBoth.new(w, Rect(0, i * 20, 100, 20)) }.dup(5);
// 12 items
a = (1..12);

e = EZScroller(w, Rect(100,0,14,100), v.size, a.size, { |sc|
        var startIndex = sc.value.asInteger.postcs;
        v.do { |drag, i| drag.object_( a[ (startIndex) + i] ? ""); };
        e.visible_(sc.numItems > sc.maxItems); // hide when not useful
});
e.doAction;
)
// change list a, update ezscroller
a = (1..4); e.numItems_(a.size); e.doAction;

a = (1..8); e.numItems_(a.size); e.doAction;



(       // basic use
        w=Window.new.front;
        g=EZKnob(w, 50@90," test  ", \freq,{|a| a.value.postln});
        g.setColors(Color.grey,Color.white);
);


// lots of knobs on on view
(
w=Window.new.front;
w.view.decorator=FlowLayout(w.view.bounds);
w.view.decorator.gap=2@2;

20.do{
        EZKnob(w, 180@24," Freq ", \freq,unitWidth:30,initVal:6000.rand,layout:\horz)
        .setColors(Color.grey,Color.white)
//        .font_(Font("Helvetica",11));

};
);

Window.closeAll  // use this to close all the windows

/////////////////////////////////////////////////////////////////
////////// click these parentheses to see all features and layouts 

(   
m=nil;
m=2@2;          // comment this for no margin

/////////////////
/// Layout \line2

(               // all features, small font
                g=EZKnob(nil, 128@40," freq  ", \freq,unitWidth:20,layout:\line2, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180,50);
                g.font_(Font("Helvetica",10));
);

(               // no unitView
                g=EZKnob(nil, 118@40," freq  ", \freq,unitWidth:0,layout:\line2, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180, -40);
);
(               // no label, so use window name as label
                g=EZKnob(nil, 118@30, nil, \freq,labelWidth:100, unitWidth:20,layout:\line2, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180, -130);
                g.window.name="Freq";
);

/////////////////
/// Layout \horz


(               // all features
                g=EZKnob(nil, 200@28," freq  ", \freq,unitWidth:30,layout:\horz, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(0,50);
);

(               // no unitView
                g=EZKnob(nil, 160@28," freq  ", \freq,layout:\horz, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(0, -30);
);
(               // no label, so use window name as label
                g=EZKnob(nil, 120@28, nil, \freq ,layout:\horz, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(0, -110);
                g.window.name="Freq";
);



/////////////////
/// Layout \vert

(               // all features
                g=EZKnob(nil, 82@82," freq  ", \freq,unitWidth:18,layout:\vert, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.font_(Font("Helvetica", 10));
                g.window.bounds = g.window.bounds.moveBy(220,50);
);

(               // no unitView, with label
                g=EZKnob(nil, 70@90," freq  ", \freq,unitWidth:0,layout:\vert, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(220,-90);
);

(               // no label
                g=EZKnob(nil, 120@60,nil, \freq, unitWidth:30,layout:\vert, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(220,-230);
                g.window.name="Freq";
);

(               // no lablel, so use window name as label
                g=EZKnob(nil, 120@60,nil, \freq,unitWidth:0,layout:\vert, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(220,-340);
                g.window.name="Freq";
);


/////////////////
/// Layout \vert2

(               // all features
                g=EZKnob(nil, 82@82," freq  ", \freq,unitWidth:18,layout:\vert2, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.font_(Font("Helvetica", 10));
                g.window.bounds = g.window.bounds.moveBy(350,50);
);

(               // no unitView, with label
                g=EZKnob(nil, 70@90," freq  ", \freq,unitWidth:0,layout:\vert2, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(350,-90);
);

(               // no label
                g=EZKnob(nil, 120@60,nil, \freq, unitWidth:30,layout:\vert2, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(350,-230);
                g.window.name="Freq";
);

(               // no lablel, so use window name as label
                g=EZKnob(nil, 120@60,nil, \freq,unitWidth:0,layout:\vert2, margin: m);
                g.setColors(Color.grey,Color.white,Color.grey, 
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(350,-340);
                g.window.name="Freq";
);


)       
        
                


///////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////
                

// Sound example
(
// start server
s.waitForBoot({

var w, startButton, noteControl, cutoffControl, resonControl;
var balanceControl, ampControl;
var node, cmdPeriodFunc;

// define a synth
SynthDef("window-test", { arg note = 36, fc = 1000, rq = 0.25, bal=0, amp=0.4, gate = 1;
                var x;
                x = Mix.fill(4, {
                        LFSaw.ar((note + {0.1.rand2}.dup).midicps, 0, 0.02)
                });
                x = RLPF.ar(x, fc, rq).softclip;
                x = RLPF.ar(x, fc, rq, amp).softclip;
                x = Balance2.ar(x[0], x[1], bal);
                x = x * EnvGen.kr(Env.cutoff, gate, doneAction: 2);
                Out.ar(0, x);
        }, [0.1, 0.1, 0.1, 0.1, 0.1, 0]
).load(s);




// make the window
w = Window("another control panel", Rect(20, 400, 230, 250));
w.front; // make window visible and front window.
w.view.decorator = FlowLayout(w.view.bounds);
w.view.decorator.gap=2@2;

// add a button to start and stop the sound.
startButton = Button(w, 75 @ 20);
startButton.states = [
        ["Start", Color.black, Color.green(0.7)],
        ["Stop", Color.white, Color.red(0.7)]
];
startButton.action = {|view|
                if (view.value == 1) {
                        // start sound
                        node = Synth( "window-test", [
                                "note", noteControl.value,
                                "fc", cutoffControl.value,
                                "rq", resonControl.value,
                                "bal", balanceControl.value,
                                "amp", ampControl.value.dbamp ]);
                } {
                        // set gate to zero to cause envelope to release
                        node.release; node = nil;
                };
};

// create controls for all parameters
w.view.decorator.nextLine;
noteControl = EZKnob(w, 220 @ 32, "Note ", ControlSpec(24, 60, \lin, 1, 36, \note),
        {|ez| node.set( "note", ez.value )}, unitWidth:30,layout:\horz)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);
        
w.view.decorator.nextLine;
cutoffControl = EZKnob(w, 220 @ 32, "Cutoff ", ControlSpec(200, 5000, \exp,0.01,1000,\Hz),
        {|ez| node.set( "fc", ez.value )}, unitWidth:30,layout:\horz)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);
        
w.view.decorator.nextLine;
resonControl = EZKnob(w, 220 @ 32, "Reson ", ControlSpec(0.1, 0.7,\lin,0.001,0.2,\rq),
        {|ez| node.set( "rq", ez.value )}, unitWidth:30,layout:\horz)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);
        
w.view.decorator.nextLine;
balanceControl = EZKnob(w, 220 @ 32, "Balance ", \bipolar,
        {|ez| node.set( "bal", ez.value )},  unitWidth:30,layout:\horz)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);
        
w.view.decorator.nextLine;
ampControl = EZKnob(w, 220 @ 32, "Amp ", \db,
        {|ez| node.set( "amp", ez.value.dbamp )}, -6, unitWidth:30,layout:\horz)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);
        

// set start button to zero upon a cmd-period
cmdPeriodFunc = { startButton.value = 0; };
CmdPeriod.add(cmdPeriodFunc);

// stop the sound when window closes and remove cmdPeriodFunc.
w.onClose = {
        node.free; node = nil;
        CmdPeriod.remove(cmdPeriodFunc);
};
});
)


//////////////////////////////
// more examples
// these mimick the original  EZKnob layout and colors

(
w = Window("EZKnob", Rect(380,400,300,180)).front;
w.view.decorator = FlowLayout(w.view.bounds);
k = EZKnob(w, 42 @ 74, "Knob", action: { arg knb; knb.value.postln; }, margin:2@2, labelHeight:16);
k.view.background_(Color.grey.alpha_(0.4));
)
k.centered_(true)
k.value=0.5;
k.visible_(false)
k.visible_(true)

k.enabled_(false)
k.value = 0.1
k.enabled
k.enabled_(true)
k.value = 0.25

(
w = Window("EZKnob", Rect(380,400,300,180)).front;
w.view.decorator = FlowLayout(w.view.bounds, gap: 1@1);
StaticText(w, (42 * 4 + 3) @ 16).string_("EZKnob Cluster").background_(Color.blue(0.1,0.1));
w.view.decorator.nextLine;
a = [
                EZKnob(w, 42 @ 74, "knob 1", margin:2@2, labelHeight:16),
                EZKnob(w, 42 @ 74, "knob 2", controlSpec: \freq, margin:2@2, labelHeight:16),
                EZKnob(w, 42 @ 74, "knob 3", controlSpec: \pan, margin:2@2, labelHeight:16).round_(0.001),
                EZKnob(w, 42 @ 74, "knob 4", controlSpec: \rq, margin:2@2, labelHeight:16)
        ];
a.do{arg a;a.view.background_(Color.grey.alpha_(0.4))};
)
// a now holds the array of knobs
a
a[0].value
a[3].value_(0.5)
a.collect(_.value );



s.boot


1/120
s.latency;

(
4.collect { arg i;
	Synth(\default, [\freq, [i.linexp(0, 44, 0.1, 0.03)*1000,  i ,  i.linlin(0, 44, 0.01, 0.3)*1000].choose.debug * 100  ])
}
)


s.volume.volume



23.12 % 0.25


23.49.roundUp(0.25)


(

w = Window.new.front;

a = StaticText(w, Rect(10, 10, 200, 60));
a.background = Color.red;
a.string = "Rolof's\r\n Rolex";
StaticText.gui
)


(

w = Window.new("Text View Example",Rect(100,Window.screenBounds.height-400, 520,300)).front;

t = TextView(w.asView,Rect(10,10, 500,200))

.focus(true);

)


// Using the Window you just created, try these in succession, and test how the text view responds

t.mouseUpAction_{|it, x, y, modifiers, buttonNumber, clickCount, pos| [pos].postln};

t.hasVerticalScroller_(true);

t.hasHorizontalScroller_(true);

t.autohidesScrollers_(true);


t.open("Help/GUI/Main-GUI/Button.html"); // load an html file


// selective editing and formatting

t.setStringColor (Color.red, 5, 220);

t.setFont (Font("Courier",12), 5, 120);

t.setString ("\nA replacement String\n", 12, 120);


// compare with these methods, which change everything

t.font_(Font("Courier",14));

t.stringColor_(Color.blue);





(
a = {
       var ampfade = SinOsc.kr(0.5).range(0, 0.1),
               modfreq = EnvGen.kr(Env([166, 166, 110], [6, 6], \sin));
       [
               SinOsc.ar(SinOsc.ar(modfreq) * 300 + 166),
               SinOsc.ar(166)
       ] * [ampfade, 0.1 - ampfade]
}.play;
)


s.freqscope

s.boot

(
c = TouchResponder({ |src,chan,bla,value|
                ["AT",src,chan,bla,value].postln;
        },
		nil,
		nil,
		nil);
        //c.learn; // wait for the first touch message
)

TouchResponder.removeAll
s.boot

(
        c = CCResponder({ |src,chan,num,value|
                ["CC",src,chan,num,value].postln;
                },
                nil, // any source
                nil, // any channel
                nil, // any CC number
                nil // any value
        )
)

)


Ndef(\def, \default);
Ndef(\def).makeGui
n = NdefGui.new(options: NdefGui.big);
n.object_(Ndef(\def));




{Formants.ar(150, Vowel(\a))}.play // is equal to

{Formants.ar(150, Vowel(\a), unfold: true).sum }.play

{Formants.ar(150, Vowel(\a), unfold: true)}.play // on headphones you'll ony hear formant1 in chan1 and formant2 in chan2 the rest is in chan 3..5

{Formants.ar([100, 200], Vowel(\u), unfold: false)}.play // you hear 100 Hz U on chan1 and 200 Hz on chan2

 

{Formants.ar(100, [Vowel(\a), Vowel(\e)], unfold: true)}.play // \a on chan1 and \e on chan2 

{Formants.ar([150, 200], [Vowel(\a, \bass), Vowel(\i, \counterTenor)], unfold: true)}.play // \a 150 Hz chan1  \i 200 Hz chan2

using freqMods, ampMods, widthMods:

{Formants.ar(70, Vowel(\e), freqMods: SinOsc.kr(1,0,0.1,1) )}.play; 

{Formants.ar(70, Vowel(\e), freqMods: [SinOsc.kr(1,0,0.2,1), SinOsc.kr(1.1,0,0.2,1), SinOsc.kr(1.2,0,0.2,1), SinOsc.kr(1.3,0,0.2,1), SinOsc.kr(1.4,0,0.2,1) ] )}.play; 

{Formants.ar(70, Vowel(\e), ampMods: SinOsc.kr(1,0,0.5,1) )}.play; 

{Formants.ar(70, Vowel(\e), ampMods: [SinOsc.kr(1,0,0.5,1), SinOsc.kr(1.1,0,0.5,1), SinOsc.kr(1.2,0,0.5,1), SinOsc.kr(1.3,0,0.5,1), SinOsc.kr(1.4,0,0.5,1) ] )}.play; 

{Formants.ar(70, Vowel(\e), widthMods: SinOsc.kr(1,0,0.99,1) )}.play; 

{Formants.ar(70, Vowel(\e), widthMods: [SinOsc.kr(1,0,0.99,1), SinOsc.kr(1.1,0,0.99,1), SinOsc.kr(1.2,0,0.99,1), SinOsc.kr(1.3,0,0.99,1), SinOsc.kr(1.4,0,0.99,1) ] )}.play; 

all together now:

{Formants.ar(70, Vowel(\e), freqMods: SinOsc.kr(1,0,0.1,1),  ampMods: SinOsc.kr(1.1,0,0.5,1),  widthMods: SinOsc.kr(1.2,0,0.999,1) )}.play; 

(

{Formants.ar(70, Vowel(\u), 

freqMods: [SinOsc.kr(1,0,0.2,1), SinOsc.kr(1.1,0,0.2,1), SinOsc.kr(1.2,0,0.2,1), SinOsc.kr(1.3,0,0.2,1), SinOsc.kr(1.4,0,0.2,1) ],  

ampMods: [SinOsc.kr(1,0,0.5,1), SinOsc.kr(1.1,0,0.5,1), SinOsc.kr(1.2,0,0.5,1), SinOsc.kr(1.3,0,0.5,1), SinOsc.kr(1.4,0,0.5,1) ],  

widthMods: [SinOsc.kr(1,0,0.99,1), SinOsc.kr(1.1,0,0.99,1), SinOsc.kr(1.2,0,0.99,1), SinOsc.kr(1.3,0,0.99,1), SinOsc.kr(1.4,0,0.99,1) ])

}.play; 

)

(

{Formants.ar(SinOsc.ar(5, 0, 10, 80), Vowel(\i), 

freqMods: [SinOsc.kr(1,0,0.2,1), SinOsc.kr(1.1,0,0.2,1), SinOsc.kr(1.2,0,0.2,1), SinOsc.kr(1.3,0,0.2,1), SinOsc.kr(1.4,0,0.2,1) ],  

ampMods: [SinOsc.kr(1,0,0.5,1), SinOsc.kr(1.1,0,0.5,1), SinOsc.kr(1.2,0,0.5,1), SinOsc.kr(1.3,0,0.5,1), SinOsc.kr(1.4,0,0.5,1) ],  

widthMods: [SinOsc.kr(1,0,0.99,1), SinOsc.kr(1.1,0,0.99,1), SinOsc.kr(1.2,0,0.99,1), SinOsc.kr(1.3,0,0.99,1), SinOsc.kr(1.4,0,0.99,1) ])

}.play; 

)



use unfolding for independently spatialized formants of one vowel (use Mouse)

{ [Formants.ar(100, Vowel(\o), unfold: true),  {|i| SinOsc.ar(MouseY.kr(0.2,1), ((i*MouseX.kr(0,pi*0.5))) )}!5 ].flop.collect({|args| Pan2.ar( args[0], args[1] ) }).sum }.play


and more complex with the modulation from above (use Mouse)

(

{ [Formants.ar(60, Vowel(\o).brightenRel(MouseY.kr(0,2), 1), 

freqMods: [SinOsc.kr(1,0,0.2,1), SinOsc.kr(1.1,0,0.2,1), SinOsc.kr(1.2,0,0.2,1), SinOsc.kr(1.3,0,0.2,1), SinOsc.kr(1.4,0,0.2,1) ],  

ampMods: [SinOsc.kr(1,0,0.5,1), SinOsc.kr(1.1,0,0.5,1), SinOsc.kr(1.2,0,0.5,1), SinOsc.kr(1.3,0,0.5,1), SinOsc.kr(1.4,0,0.5,1) ],  

widthMods: [SinOsc.kr(1,0,0.99,1), SinOsc.kr(1.1,0,0.99,1), SinOsc.kr(1.2,0,0.99,1), SinOsc.kr(1.3,0,0.99,1), SinOsc.kr(1.4,0,0.99,1) ]),  

{|i| SinOsc.ar((i+1)/MouseX.kr(0.2,1), ((i*pi)/10) )}!5 ]

.flop.collect({|args| Pan2.ar( args[0], args[1] ) }).sum }.play

)



BPFStack.ar(in,  vowel,  freqMods, ampMods, widthMods, unfold )

Formants is a Stack of the BPF filters it needs an in to chew on and either an arrays of  #freqs,  #dBs and #widths or a Vowel.

in - exciting Signal. Default value is nil.

BPFStack.ar(Impulse.ar(1), Vowel(\a)) 

~freq = 100;

{BPFStack.ar(Decay.ar(Saw.ar(~freq, 0.1), ~freq.reciprocal), Vowel(\a)) * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play

{BPFStack.ar(Decay.ar(Saw.ar(~freq, 0.1), ~freq.reciprocal), Vowel(\e)) * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play

{BPFStack.ar(Decay.ar(Saw.ar(~freq, 0.1), ~freq.reciprocal), Vowel(\i)) * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play

{BPFStack.ar(Decay.ar(Saw.ar(~freq, 0.1), ~freq.reciprocal), Vowel(\o)) * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play

{BPFStack.ar(Decay.ar(Saw.ar(~freq, 0.1), ~freq.reciprocal), Vowel(\u)) * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play

{BPFStack.ar(ClipNoise.ar(0.3), Vowel(\a))  * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play

{BPFStack.ar(ClipNoise.ar(0.3), Vowel(\e))  * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play

{BPFStack.ar(ClipNoise.ar(0.3), Vowel(\i))  * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play

{BPFStack.ar(ClipNoise.ar(0.3), Vowel(\o))  * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play

{BPFStack.ar(ClipNoise.ar(0.3), Vowel(\u))  * EnvGen.kr(Env.perc, 2.0, doneAction: 2)}.play



and more complex with the modulation from above (use Mouse)

(

{ [BPFStack.ar(ClipNoise.ar(0.1), Vowel(\i).brightenExp(MouseY.kr(0,2), 1), 

freqMods: [SinOsc.kr(1,0,0.2,1), SinOsc.kr(1.1,0,0.2,1), SinOsc.kr(1.2,0,0.2,1), SinOsc.kr(1.3,0,0.2,1), SinOsc.kr(1.4,0,0.2,1) ],  

ampMods: [SinOsc.kr(1,0,0.5,1), SinOsc.kr(1.1,0,0.5,1), SinOsc.kr(1.2,0,0.5,1), SinOsc.kr(1.3,0,0.5,1), SinOsc.kr(1.4,0,0.5,1) ],  

widthMods: [SinOsc.kr(1,0,0.99,1), SinOsc.kr(1.1,0,0.99,1), SinOsc.kr(1.2,0,0.99,1), SinOsc.kr(1.3,0,0.99,1), SinOsc.kr(1.4,0,0.99,1) ]),  

{|i| SinOsc.ar((i+1)/MouseX.kr(0.2,1), ((i*pi)/10) )}!5 ]

.flop.collect({|args| Pan2.ar( args[0], args[1] ) }).sum }.play

)




Examples


the following example show how Vowels can be used within a SynthDef:


(

SynthDef(\vowblend,{|freq = 100, b1 = 0.5, b2 = 0.5, b3 = 0.5, b4 = 0.5 bright = 0, pan = 0|

var va = Vowel(\a, \bass),

    ve = Vowel(\e, \tenor),

    vi = Vowel(\i, \counterTenor),

    vo = Vowel(\o, \alto),

    vu = Vowel(\u, \soprano),

    sig;

   

    sig =  Formants.ar(

    freq, 

    va

    .blend(ve, b1)

.blend(vi, b2)

.blend(vo, b3)

.blend(vu, b4)

.brightenExp(bright, 1) 

) 

* EnvGen.kr(Env.perc, 3.0, doneAction: 2);  


Out.ar(0,  Pan2.ar(sig, pan, 0.1));

}).add

)



(

Task({  32.do({ arg i; 

Synth(\vowblend, [

\pan, i.linlin(0,32, -1, 1 ),

\freq, i.linlin(0,32, 30, 66 ).midicps,

\b1, 2.rand,

\b2, 2.rand, 

\b3, 2.rand,

\b4, 2.rand, 

\bright,  1.5.rand

]); 

0.25.wait


});}).play

)



using addControls to create buses 


(

x = SynthDef(\test, {

Out.ar(0, 

Formants.ar(420, Vowel(\a).addControls(3)) * 0.01

)

}).play

)


x.inspect


x.setn(*Vowel(\i).asKeyValuePairs(3));



the following example show how Vowels can be used within JITLib style:



NdefMixer(s);


Ndef(\vowel, {Formants.ar(200, Vowel(\a, \soprano))  * 0.01 }).play


(

Ndef(\vowel, {

Formants.ar(200, 

Vowel(\o, \soprano)

.blend(Vowel(\i, \tenor), SinOsc.kr(10).range(0,1))) * 0.01

}).play

)



(

Ndef(\vowel, {

Formants.ar(LFNoise0.kr(10).exprange(100, 400), 

Vowel(\o, \soprano)

.brightenExp(SinOsc.kr(2).range(0,1), 1),

unfold: true

).mean * 0.01

}).play

)



Ndef(\vowel, {

Formants.ar(200, Vowel(\a, \soprano).addControls(4)) * 0.01

}).play


Ndef(\vowel).setn(*Vowel(\e, \bass).asKeyValuePairs(4).flatten)

Ndef(\vowel).setn(*Vowel(\u).asKeyValuePairs(4).flatten)



Ndef(\vowel, {

Formants.ar(200, Vowel(\a, \soprano), unfold: true).scramble.keep(2) * 0.1

}).play


Ndef(\vowel).free(2)


Ndef(\vowel).fadeTime = 4;


(

Ndef(\vowel, {

Formants.ar([1, 2, 4] * 240 * {LFNoise1.kr(5, 0.003, 1)}!3, Vowel(\a, [\bass, \tenor, \soprano]), 

freqMods: LFNoise1.ar(4*[0.1, 0.2, 0.3, 0.4, 0.5].scramble, 0.1, ampMods: [1, 1, 1, 0]

)).sum * 0.1

}).play

)




// FUN:



( // CPU demanding 

~freqs = {|i| 50 * 1.5.pow(i) }!9;

~numChan = 2;

r = Routine{

var sustain = 8, transition = 3, overlap = 4;

var period = 1.5 * 2.sqrt;

var harms, amps;

0.5.wait;

inf.do{

harms = {|i| (i+1) * ~freqs.choose }!60;

amps  = Vowel([\a,\e,\i,\o,\u].choose,[\bass,\tenor,\counterTenor,\alto,\soprano].choose).ampAt(harms);

{ PanAz.ar(~numChan, DynKlank.ar( `[~freqs,amps,amps],

Decay.ar(Impulse.ar( exp(1)/5.0 ), SinOsc.kr( pi/9.0, 1.0.rand ).range(0.05,0.7) )   ) *

EnvGen.kr(Env.new([-40,-20, -30,-40].dbamp, [2/5.0, 1/5.0,2/5.0],'exponential'), 1.0, timeScale: 35, levelScale: 0.1, doneAction: 2) ,SinOsc.kr(0.5, 1.0.rand) )}.play;

period.wait;

}

};

r.play;

)

r.stop; //stop spawning new synths



(

Ndef(\vowel).fadeTime = 5;

Ndef(\vowel, {

var freqs, dBs, widths, out;

var baseFreq = LFNoise0.kr([5, 10] * 0.1).round(0.1).exprange(50, 200) * [2, 1.01];

#freqs, dBs, widths =  (Vowel(\i, \soprano).blend(Vowel(\o, \bass), LFNoise1.kr(0.1265))).blend(Vowel(\e, \bass), LFNoise1.kr(10)).asArray;

//freqs = freqs * SinOsc.ar([0.1, 0.2, 0.3, 0.4].scramble, Rand(), 0.1, 1);

freqs = freqs * LFNoise1.ar([0.1, 0.2, 0.3, 0.4].scramble, 0.1, 1);

out = [freqs, widths, dBs.dbamp].flop.collect{ |args| 

Formant.ar(baseFreq, *args); 

}.flop;


out = out.collect{|vocal|

Splay.ar(vocal)

}.sum.postln;


out

* LFPulse.ar([9, 9.01], 0, 0.4).range(0, 1).lag(0.01, 0.5) 

* LFPulse.ar(0.1, [0, 0.35], [0.9, 0.8]).lag(0.01)


 * 0.1


}).play

)






(

Ndef(\vowel).fadeTime = 5;

Ndef(\vowel, {

	var freqs, dBs, widths, out;
	var baseFreq = LFNoise0.kr([5, 10] * 0.1).round(0.1).exprange(50, 200) * [2, 1.01];
	baseFreq = 100;
	baseFreq = LFNoise0.kr([5, 10] * 0.1).round(0.1).exprange(50, 200) * [2, 1.01];

	#freqs, dBs, widths =  (Vowel(\i, \soprano).blend(Vowel(\o, \bass), 1)).blend(Vowel(\e, \bass), 1).asArray;

	//freqs = freqs * SinOsc.ar([0.1, 0.2, 0.3, 0.4].scramble, Rand(), 0.1, 1);

	//freqs = freqs * LFNoise1.ar([0.1, 0.2, 0.3, 0.4].scramble, 0.1, 1);

	out = [freqs, widths, dBs.dbamp].flop.collect{ |args| 
		Formant.ar(baseFreq, *args); 
	}.flop;

	//out = out.collect{|vocal|
	//		Splay.ar(vocal)
	//}.sum.postln;

	out.sum
	//* LFPulse.ar([9, 9.01], 0, 0.4).range(0, 1).lag(0.01, 0.5) 
	//* LFPulse.ar(0.1, [0, 0.35], [0.9, 0.8]).lag(0.01)
	* 0.1


}).play

)

{ LFNoise0.ar(5000).exprange(50,200)*0.001}.scope
s.scope
s.freqscope




{ Formants.ar(150,  Vowel(\a, \bass).blend(Vowel(\u, \bass), [MouseX.kr(0,1), MouseY.kr(0,1), SinOsc.kr(0.5, 0, 0.5, 0.5)] ) ) * 0.1  }.play

{ Formants.ar(150,  Vowel(\e, \bass).blend(Vowel(\i, \bass), MouseX.kr(0,1), MouseY.kr(0,1), SinOsc.kr(0.5, 0, 0.5, 0.5) ) ) * 0.1  }.play

{ Formants.ar(150,  Vowel(\i, \bass).blend(Vowel(\o, \bass), MouseX.kr(0,1), MouseY.kr(0,1), SinOsc.kr(0.5, 0, 0.5, 0.5) ) ) * 0.1  }.play




Ndef(\bla).free(2)

(
{
var freq, ratioNum, ratioDenum; //declare two variables
ratioNum = 3; //assign numerator
ratioDenum = 2; //assign denominator
freq = MouseX.kr(1,440, warp: 1); //freq is mouse control
LFSaw.ar(
[freq, freq*(ratioNum/ratioDenum)], 0,
0.3)*0.01
}.scope(2)
)



(
// this version has no lag so delay time jumps around with clicks
{
       var input, delayTime, outDelay;
       // input from sound card
       input = SoundIn.ar(0);
       // delay time jumps every 2 seconds
       delayTime = Demand.kr(Impulse.kr(1/2), 0, Dseq([0.4, 0.6, 0.9, 0.3, 0.7], inf));
       // CombC - delay with feedback
       CombC.ar(input, 2, delayTime, 6) + input;
}.play;
)

(
// this version has a lag so delaytime smoothly changes with no clicks
{
       var input, delayTime, laggedDelayTime, outDelay;
       // input from sound card
       input = SoundIn.ar(0);
       // delay time jumps every 4 seconds
//       delayTime = Demand.kr(Impulse.kr(1/4), 0, Dseq([0.4, 0.6, 0.9, 0.3, 0.7], inf));
       delayTime = MouseX.kr(0.01, 1);
       // lagged delay time - takes 3 seconds to reach new value
       laggedDelayTime = Lag.kr(delayTime, MouseY.kr(0.001,3));
       // CombC - delay with feedback
       CombC.ar(input, 2, laggedDelayTime, 6) + input;
}.play;
)




(
{
       var     x = LFPulse.ar(100);
       [x, Lag.ar(x, 0.008)]
}.plot(duration: 0.05);
)



(
SynthDef(\BigKick, {
| 
out = 0, pitch = 60, pan = 0, 
amp = 1, click = 1, gate = 1,
decay = 1, pDecaySpeed = 0.5, pDecayDepth = 20,
attack = 0.0005
|

//Local Vars
var osc, oscPitchEnv, oscAmpEnv, oscPitchDecay;
var noise, noiseampEnv;
var output;
//Osc
oscPitchDecay = (attack + decay) * pDecaySpeed;
oscPitchEnv = EnvGen.kr(Env.perc(0, oscPitchDecay, pDecayDepth, 0));
oscAmpEnv = EnvGen.kr(Env.perc(attack, decay, amp, -4), doneAction:2);
osc = SinOsc.ar((pitch + oscPitchEnv), mul: oscAmpEnv);
//Noise
noiseampEnv = EnvGen.kr(Env.perc(0.0005, 0.03, (amp * click) * 0.3, -4));
noise = WhiteNoise.ar(mul: noiseampEnv);

//Out
output = Pan2.ar(osc + noise, pan);
Out.ar(out, output);
}).add;

)


(
Pdef(\kick, Pbind(
	\instrument, \BigKick,
	\decay, 0.5,
	\pitch, 60,
	\pDecaySpeed, 2.1,
	\pDecayDepth, 10,
	\click, 1,
	\dur, 1
)).play
)


play{LFPar.ar(LFNoise1.kr(300) > 0 * 20 + 070, 0, 2).tanh}





(
var win, view;
var run = true;
q = ();

q.branches = List[];

q.minWanderStep = 1.0184;
q.maxWanderStep = 0.1702;
q.minGrowthRate = 10.6214;
q.maxGrowthRate = 11.8251;
q.minShrinkRate = 0.99656;
q.maxShrinkRate = 0.91265;
q.branchProbability = 0.05;
q.minDivergence = 1.3268;
q.maxDivergence = 1.3885;
q.maxConcurrent = 500;
q.numBranches = 6;
q.minRadius = 0.15;
q.maxRadius = 70;

q.makeBranch = {
	arg env, x, y, theta, radius, scale = 1.0, generation = 1;

	(
		x: x, 
		y: y,
		ox:x, 
		oy: y,
		x1: nil, x2: nil,
		y1: nil, y2: nil,
		scale: 1.0,
		theta: theta,
		oTheta:theta,
		radius:radius,
		generation:1,
		growing:true,
		age:0,
		wanderStep: rrand(q.minWanderStep, q.maxWanderStep),
		growthRate: rrand(q.minGrowthRate, q.maxGrowthRate),
		shrinkRate: rrand(q.minShrinkRate, q.maxShrinkRate),
		fRender: {
			arg that, context;
			var scale, radius;
			if(that.growing,
				{
					scale = that.scale;
					radius = that.radius * scale;

					// Draw outline
					Pen.line(that.ox@that.oy,that.x@that.y);
					
					// not in qt...
					if((GUI.scheme == "CocoaGUI") and: (radius > 5.0), {
					 	Pen.setShadow(1@1, scale, Color.new(0,0,0,0.05));
					});
					
					Pen.width = radius + scale;
					Pen.strokeColor = Color.black;
					Pen.capStyle = 1; //round
					Pen.stroke();
					
					// Draw fill
					Pen.line(that.ox@that.oy, that.x@that.y);

					Pen.width = radius;
					Pen.strokeColor = Color.white;
					Pen.capStyle = 1; //round
					Pen.stroke();
				});
		},
		fUpdate: {
			arg that;
			var theta, scale, radius, branch, offset;
			if(that.growing,
				{
					that.ox = that.x;
					that.oy = that.y;
					that.oTheta = that.theta;

					that.theta = that.theta + rrand(that.wanderStep * -1,
						that.wanderStep);
					
					that.x = that.x + (cos(that.theta) 
						* that.growthRate * that.scale);
					that.y = that.y + (sin(that.theta) 
						* that.growthRate * that.scale);

					that.scale = that.scale * that.shrinkRate;

					if(
						(q.branches.size < q.maxConcurrent)
						and:
						(1.0.rand < q.branchProbability),
						{
							offset = rrand(q.minDivergence,
								q.maxDivergence);
							theta = that.theta 
							+ (offset * [1,-1].choose);
							
							scale = that.scale * 0.95;
							radius = that.radius * scale;

							branch = q.makeBranch(
								that.x, that.y, theta, radius, scale);

							branch.generation = that.generation + 1;
							q.branches.add(branch);
						});

					if((that.radius * that.scale) <= q.minRadius, {
						that.growing = false;
					});

					that.age = (that.age + 1);							
				})
		}
	)
};

q.makeRecursion = {
	arg env;

	(
		//started: false,
		fSpawn: {
			arg env, x,  y;
			var theta, radius;
			q.branches = List[];
			q.numBranches.do{
				arg i;
				theta = (i / q.numBranches) * 2pi;
				radius = q.maxRadius;
				q.branches.add(q.makeBranch(x, y, theta - (pi/2), radius));
			}
		},
		fUpdate: {
			arg env;
			var index;
			var numBranches = q.branches.size;
			q.branches.do{
				arg branch, i;
				branch.fUpdate;
				branch.fRender;
			};
			//strip dead branches
			
			numBranches.do{
				|i|
				index = numBranches - (i + 1);
				if(q.branches[index].growing.not,
					{
						q.branches.removeAt(index);
					})
			}
		}
	)	
};

r = q.makeRecursion;
r.fSpawn(350,350);
win = Window(
	"grow! (click to restart)", Rect(10, 10, 700, 700)
);
win.onClose = { run = false; };
view = UserView(win, 700@700).drawFunc_({ r.fUpdate }).clearOnRefresh_(false).mouseDownAction_({ |v,x,y| view.clearDrawing; r.fSpawn(x,y) });
win.front;
{ while { run } { win.refresh; 0.05.wait } }.fork(AppClock)
)







(
var screen = Window.screenBounds,
	height = (screen.height * 0.8).asInteger,
	width = (screen.width * 0.8).asInteger,
	win = Window(\aliasing, Rect.aboutPoint(screen.center, width / 2, height / 2 + 20)),
	sinPts = 400, sampPts = 20,
	freq = 1, fsl,
	sinColor = Color.red, sampColor = Color.black;

win.drawHook = {
	var pt;
	pt = Point(0, height/2);
	Pen.color_(sampColor)
		.moveTo(pt);
	(1..sampPts).do { |x|
		Pen.moveTo(pt);
		pt = Point(x * (width / sampPts), sin(x * freq / sampPts * 2pi).linlin(-1, 1, height, 0));
		Pen.lineTo(pt).stroke
			.fillRect(Rect.aboutPoint(pt, 3, 3));		
	};

	Pen.color_(sinColor)
		.moveTo(Point(0, height/2));
	(1..sinPts).do { |x|
		Pen.lineTo(Point(
			x * (width / sinPts),
			sin(x * freq / sinPts * 2pi).linlin(-1, 1, height, 0)
		));
	};
	Pen.stroke;
};

fsl = EZSlider(win, Rect(5, height+10, width-10, 20), "freq:", [1, 20], { |view| freq = view.value; win.refresh }, 1, initAction: true);

win.front;
)


play{a=Impulse;tanh(a.kr(8).lag*Crackle.ar(LFSaw.kr(3).abs.lag*1.8)+GVerb.ar([a.kr(2)+a.kr(4,0.5)].lag*Blip.ar(4.9,7,0.4)!2,1,1)*5)}



(
//LET'S PAINT!
//select all and compile

var point, red, green, blue, win, view, colorTask;
var redChange, greenChange, blueChange;

//rate of change of each color component
//mess around with these values for fun
//zero results in no color change
redChange = 0.01;
greenChange = 0.015;
blueChange = 0.02;

//default starting RGB values
//these values can be changed as well
//maintain range of 0≤x≤1
red=0; green=0.33; blue=0.67;

//window creation
win = Window("FANCY ARTWORK", resizable:true, border:false);
win.fullScreen;
win.onClose = {
	if( 
		colorTask.isPlaying,
		{colorTask.stop},
		{}
	);
};

//userview creation
view = UserView(win, Window.screenBounds);
view.clearOnRefresh = false;
view.background = Color.white;

//any click sets point as current mouse location
//left-click does nothing special
//right-click clears palette
view.mouseDownAction = {
	|v, x, y, mod, butNum, clkCnt|
	point = [x,y];
	if(butNum == 1,
		{
			view.drawFunc_({nil});
			view.clearDrawing;
			view.refresh},{}
	);
};

//mouse drag redefines userview drawFunc
//Pen draws line from old point to current point
//then sets old point equal to current point
view.mouseMoveAction = {
	|v, x, y, mod|
	view.drawFunc = {
		Pen.strokeColor = Color.new(
			red.fold(0,1),
			green.fold(0,1),
			blue.fold(0,1)
		);
		Pen.width = 3;
		Pen.line(point.asPoint,x@y);
		point = [x,y];
		Pen.stroke;
		};
	win.refresh;
};

//RGB values wrap through range 0≤x<2
//and are folded into 0≤x≤1 via mouseMove function
//thus RGB values oscillate linearly, out of phase with
//one another, back and forth from 0 to 1
colorTask = Task({
	{
		red = (red + redChange)%2;
		green = (green + greenChange)%2;
		blue = (blue + blueChange)%2;
		0.05.wait; //arbitrary wait time
	}.loop;
});

//comment out for no color change
colorTask.start;

win.front;
)



// a drum part
play{b=SinOsc.kr(1);a=Line.kr(0.1,2*pi,37);FreeVerb.ar(SinOsc.ar(b*340!2*Impulse.kr(b*680),LFPulse.kr(2,0,pi/2),LFPulse.kr(3,0,1/pi*a)))}

//sticky morning clock
play{c=LFPulse.kr(0.5);b=SinOsc.kr(0.0034);a=Line.kr(0.1,2pi,37);FreeVerb.ar(Blip.ar(a*340!2*Impulse.kr(b*34/pi),c*TIRand.kr(0,113,c),c))}

// another drum part
play{a=Impulse;b=SinOsc;a.kr(6).lag*Crackle.ar(LFSaw.kr(5).abs.lag2)+a.ar(7).lag*b.ar(222*b.kr(29))+([a.kr(2,0,4)+a.kr(1)].lag*b.ar(70!2))}

/// i like to play these two on series
play{a=Impulse;a.kr(8).lag2*Crackle.ar(LFSaw.kr(5).abs.lag2)+GVerb.ar([a.kr(2,0,4).lag+a.kr(1,pi/5).lag]*Blip.ar(5,2,0.2)!2,6,0.4)}
///
play{a=Impulse;a.kr(8).lag2*Crackle.ar(LFSaw.kr(7).abs.lag3)+GVerb.ar([a.kr(2,0,4).lag+a.kr(1,pi/1.2).lag]*SinOsc.ar(20)!2,6,0.4,0.4)}


fork{loop{play{f=_*3.pow(17.rand/13);e=EnvGen.ar(Env.perc,1,0.3,0,1,2);PMOsc.ar(f.([438,442]),f.(880),f.(e))*e};[1/6,1/3].choose.wait}}


(
Ndef(\z, {

	// get fed back signal and add a little noise to get things going
	var sig = Ndef(\z).ar + WhiteNoise.ar(0.001!2);
	var a, k, delfreq, minfreqs, freqs, dry;
		
	// delay due to distance from amp - I chose 0.05s, or 20Hz
	delfreq = 20;
	sig = DelayN.ar(sig,1/10-ControlDur.ir,1/delfreq-ControlDur.ir);

	// guitar string frequencies - for some reason I had to pitch them down
	// a few octaves to get a good sound.
	// open strings
	//// freqs = (64+[0,5,10,15,19,24]).midicps/8;
	// e minor
	freqs = (64+[0,7,12,15,19,24]).midicps/4;
	
	// whammy bar modulates freqs:
	minfreqs = freqs*0.5;
	freqs = freqs*MouseButton.kr(1,0.75,4);
	
	// 6 comb filters emulate the strings' resonances
	sig = CombN.ar(sig!6,1/minfreqs,1/freqs,8).mean;

	// a little filtering... mouse Y controls amp
	sig = LPF.ar(sig,8000);
	sig = HPF.ar(sig*MouseY.kr(0,5),80);

	// and some not too harsh distortion - mouse X controls damping
	sig = RLPFD.ar(sig,MouseX.kr(200,10000,1)*[1,1.1],0.1,0.5);
	sig = sig + sig.mean;

	// and finally a spot of reverb
	dry = sig;
	10.do {
		d = 0.2.rand;
		sig = AllpassN.ar(sig,d,d,5);
	};
	(dry + sig)*0.125;

}).play;
)


(
// Pulse doesn't respond well to being frequency modulated - loads of weird low freq noise
{ Pulse.ar(SinOsc.ar(XLine.kr(1,8000,20)).range(1,2000))*0.2 !2}.play
)

(
// LFPulse doesn't sound like an analogue synth due to loads of aliasing
{ LFPulse.ar(SinOsc.ar(XLine.kr(1,8000,20)).range(1,2000))-0.5*0.4 !2}.play
)

(
// But you can also produce a good approximation of a pulse wave by clipping a 
// high-amplitude sine wave.  Clipper8 (from sc3-plugins) allows you to do this 
// in a band-limited way.  This sounds much more like a frequency modulated analogue
// oscillator.
{ Clipper8.ar(SinOsc.ar(SinOsc.ar(XLine.kr(1,8000,20)).range(0,2000), 0, 10), -0.2, 0.2) !2}.play
// note: there are two SinOscs - the inner one is the modulator and the outer one is
// clipped to make the pulse wave
)

(
// with PWM too
{ Clipper8.ar(SinOsc.ar(SinOsc.ar(XLine.kr(1,8000,20)).range(50,1000), 0, 10)+LFTri.ar(8,mul:9.9), -0.2, 0.2) !2}.play
)


// up
(
play{
	x = BPF.ar(
		PinkNoise.ar(0.2!2),
		100,
		0.2
	)*Line.kr(1,0,1);
	Fb({
		|fb|
		FreqShift.ar(fb+x, 5);
	})
}
)


// down
(
play{
	x = BPF.ar(
		PinkNoise.ar(0.2!2),
		8000,
		0.2
	)*Line.kr(1,0,1);
	Fb({
		|fb|
		FreqShift.ar(fb+x, -4);
	})
}
)


// this is the new version!
(
s = Server.default;
s.boot;
)
(
SynthDef(\bglass, {
	
	|t_trig = 1.0, audioOutBus = 0|
	
	var major_hit_times, major_hit_deltas, major_hit_levels, major_hit_impulse_levels, major_hit_impulses;
	var major_hit_amp, major_hit_envGen, major_hit_out;
	var klank_freq_array, klank_out;
	var noise, noise_out;
	var additional_tinkles;
	var initial_impact_pitch_envGen, initial_impact_amp_envGen, initial_impact;
	var comb_out, output;
	
	var last_time;
		
	major_hit_times = [0.02, 0.1, 0.21, 0.28, 0.32, 0.48, 0.6, 0.69];
	major_hit_deltas = [];
	last_time = 0.0;
	major_hit_times.size.do { |i|
		major_hit_deltas = major_hit_deltas ++ 
			[
			SampleDur.ir, 
			((major_hit_times[i] - last_time) * TRand.kr(0.4, 1.6, t_trig)) - SampleDur.ir
			];
		
		last_time = major_hit_times[i];
	};
	major_hit_levels = [1.0, 0.3, 0.7, 0.4, 0.65, 0.87, 0.27, 0.4];
	major_hit_levels.size.do { |i|
		
		major_hit_levels[i] = major_hit_levels[i] * TRand.kr(0.7, 1.0, t_trig);
		
	};
	major_hit_impulse_levels = [major_hit_levels, 0 ! major_hit_times.size].lace(major_hit_times.size * 2);
	major_hit_impulses = Duty.ar(
		Dseq(major_hit_deltas, 1), K2A.ar(t_trig), Dseq(major_hit_impulse_levels, 1)
		);	
	major_hit_amp = Gate.ar(major_hit_impulses, major_hit_impulses);
	major_hit_envGen = EnvGen.ar(Env.perc(0.0, 0.03, 1, -9), major_hit_impulses) * major_hit_amp;
	major_hit_out = major_hit_envGen * WhiteNoise.ar * 0.6;
	major_hit_out = major_hit_out + major_hit_impulses;
	
	klank_freq_array = [1383, 2831, 3252, 3446, 4547, 4600, 4832, 5863, 6813, 8683, 11196];
	klank_freq_array.size.do { |i|
	
		klank_freq_array[i] = klank_freq_array[i] * TRand.kr(0.8, 1.2,  Impulse.kr(20));
	};
	klank_out = DynKlank.ar(`[klank_freq_array, nil, 0.2], major_hit_out * 0.05);
	klank_out = DelayC.ar(klank_out, 0.2, 0.009);

	noise = BrownNoise.ar + (WhiteNoise.ar * 0.3) + (PinkNoise.ar * 0.6);
	noise = noise * 0.1;
	noise = noise + Dust.ar(70, 1);
	noise_out = noise * LagUD.ar(major_hit_impulses, 0.0, 0.842);
	
	additional_tinkles = 
	DynKlank.ar(`[ Array.series(8, 1200, 179), nil, 0.7], Dust.ar(14), TRand.kr(2.9, 3.1, Impulse.kr(28)) )
	+
	DynKlank.ar(`[ Array.series(8, 1200, 179), nil, 0.13], Dust.ar(11, 0.7), TRand.kr(1.4, 2.2, Impulse.kr(15)) );
	
	additional_tinkles = additional_tinkles * 
		EnvGen.ar(Env.linen(0.15, 0.3, 0.3, 1.0, -2), t_trig, timeScale: TRand.kr(0.9, 1.12, t_trig));
	
	initial_impact_pitch_envGen = EnvGen.ar(Env.perc(0.001, 0.03, 1.0, -7), t_trig);
	initial_impact_amp_envGen = EnvGen.ar(Env.perc(0.0, 0.04, 1.0, -9), t_trig);
	initial_impact = SinOsc.ar(initial_impact_pitch_envGen.exprange(20, 4000) * TRand.kr(0.96, 1.03, t_trig)) * 0.5;
	initial_impact = initial_impact + LFNoise1.ar(6800, 1.0) * initial_impact_amp_envGen;
	initial_impact = HPF.ar(initial_impact, 100);
	initial_impact = initial_impact + CombC.ar(initial_impact, 0.2, 0.04, 0.2, 0.12);
	output = (klank_out * 0.5) + (major_hit_out * 0.45);
	output = (output * 0.86) + DelayC.ar(output, 0.2, 0.01);
	output = output + (noise_out * 0.32) + (additional_tinkles * 0.044) + (initial_impact * 0.2);
	
	comb_out = CombC.ar(output, 0.1, [0.028, 0.031], 0.52, 0.23);
	comb_out = LPF.ar(comb_out, 3000);
	comb_out = HPF.ar(comb_out, 110);
	output = output + comb_out;
	Out.ar(audioOutBus, output);
	
}).send(s);

)
x = Synth(\bglass, [\audioOutBus, 0], s);
x.set(\t_trig, 1.0);



///////////////////////////////////////////////////////////////////////////


// and now the previous attempt
(
s = Server.default;
s.boot;
)

(



SynthDef(\bglass, {
	
	|t_trig = 1.0, audioOutBus = 0|
	
	var major_hit_times, major_hit_deltas, major_hit_levels, major_hit_impulse_levels, major_hit_impulses;
	var major_hit_amp, major_hit_envGen, major_hit_out;
	var klank_freq_array, klank_out;
	var noise, noise_out;
	var additional_tinkles;
	var initial_impact_pitch_envGen, initial_impact_amp_envGen, initial_impact;
	var output;
	
	var last_time;
		
	major_hit_times = [0.02, 0.1, 0.21, 0.28, 0.32, 0.48, 0.6, 0.69];
	major_hit_deltas = [];
	last_time = 0.0;
	major_hit_times.size.do { |i|
		major_hit_deltas = major_hit_deltas ++ 
			[
			SampleDur.ir, 
			(major_hit_times[i] - last_time) - SampleDur.ir
			];
		
		last_time = major_hit_times[i];
	};
	
	major_hit_levels = [1.0, 0.3, 0.7, 0.4, 0.65, 0.87, 0.27, 0.4];
	major_hit_impulse_levels = [major_hit_levels, 0 ! major_hit_times.size].lace(major_hit_times.size * 2);
	
	major_hit_impulses = Duty.ar(
		Dseq(major_hit_deltas, 1), K2A.ar(t_trig), Dseq(major_hit_impulse_levels, 1)
		);	

	major_hit_amp = Gate.ar(major_hit_impulses, major_hit_impulses);
		
	major_hit_envGen = EnvGen.ar(Env.perc(0.0, 0.03, 1, -9), major_hit_impulses) * major_hit_amp;
	major_hit_out = major_hit_envGen * WhiteNoise.ar * 0.6;
	
	major_hit_out = major_hit_out + major_hit_impulses;
	
	klank_freq_array = [1383, 2831, 3252, 3446, 4547, 4600, 4832, 5863, 6813, 8683, 11196];
	klank_freq_array.size.do { |i|
	
		klank_freq_array[i] = klank_freq_array[i] * TRand.kr(0.9, 1.1,  Impulse.kr(20));
	};
	klank_out = DynKlank.ar(`[klank_freq_array, nil, 0.2], major_hit_out * 0.05);
	klank_out = DelayC.ar(klank_out, 0.2, 0.009);

	noise = BrownNoise.ar + (WhiteNoise.ar * 0.3) + (PinkNoise.ar * 0.6);
	noise = noise * 0.1;
	noise = noise + Dust.ar(70, 1);
	noise_out = noise * LagUD.ar(major_hit_impulses, 0.0, 0.842);
	
	additional_tinkles = DynKlank.ar(`[ Array.series(8, 1200, 179), nil, 0.7], Dust.ar(18), TRand.kr(2.9, 3.1, Impulse.kr(20)) );
	
	additional_tinkles = additional_tinkles * EnvGen.ar(Env.linen(0.15, 0.3, 0.3, 1.0, -2), t_trig);
	
	initial_impact_pitch_envGen = EnvGen.ar(Env.perc(0.001, 0.03, 1.0, -7), t_trig);
	initial_impact_amp_envGen = EnvGen.ar(Env.perc(0.0, 0.04, 1.0, -9), t_trig);
	initial_impact = SinOsc.ar(initial_impact_pitch_envGen.exprange(20, 4000)) * 0.5;
	initial_impact = initial_impact + LFNoise1.ar(6800, 1.0) * initial_impact_amp_envGen;
	initial_impact = HPF.ar(initial_impact, 100);
	initial_impact = initial_impact + CombC.ar(initial_impact, 0.2, 0.04, 0.2, 0.12);
	output = (klank_out * 0.5) + (major_hit_out * 0.45) + (noise_out * 0.12) + (additional_tinkles * 0.044) + (initial_impact * 0.2);
	
	output = output ! 2;
	Out.ar(audioOutBus, output);
	
}).send(s);


)


x = Synth(\bglass, [\audioOutBus, 0], s);
x.set(\t_trig, 1.0);



b = Buffer.alloc(s, 512, 1);
b.sine1(1.0, true, false, true);

(
Ndef(\feedbacker, {|resetRate = 100, freq = 100, lowFreq = 100, rq = 0.1|
var src = OscN.ar(b, freq);
BufWr.ar(src, b, Phasor.ar(Impulse.ar(resetRate), BufRateScale.kr(b) * 1, 0, BufFrames.kr(b)));
src
})
)

(
Ndef(\feedbacker, {|resetRate = 100, freq = 100, lowFreq = 100, rq = 0.1|
var src = OscN.ar(b, freq);
BufWr.ar(src, b, Phasor.ar(Impulse.ar(resetRate), BufRateScale.kr(b) * 1, 0, BufFrames.kr(b)));
src = RLPF.ar(src, lowFreq, rq);
src
})
)


Ndef(\feedbacker).pause
Ndef(\feedbacker).resume

(
Ndef(\feedbacker, {|resetRate = 100, freq = 100|
var src = OscN.ar(b, freq);
BufWr.ar(src, b, min(BufFrames.kr(b), Phasor.ar(Impulse.ar(resetRate), BufRateScale.kr(b) * 0.25, 0, 4* BufFrames.kr(b))));
src
}).play


)





NdefMixer(s)



/* Markov Chain Experiment II
   Jacob Joaquin

   Get a visual representation of the Markov Chain here:
   http://codehop.com/supercollider-markov-chain/
*/

(
// Synthesizer
SynthDef(\my_synth, {|dur = 1.0, amp = 1.0, freq = 440|
	var env = EnvGen.ar(Env.new([1, 0.1, 0], [0.06, dur - 0.06]), doneAction: 2);
	Out.ar([0, 1], SinOsc.ar([freq * 0.995, freq * 1.005], 0, env * amp))
}).add;		

// Create task
t = Task({
	// Set attributes of each node
	// [freq, dur, [[next_state, weighted_random],…]]
	var node_list = [
		[60, 1, [[1, 2]]],
		[62, 0.5, [[0, 1], [2, 1]]],
		[63, 1, [[0, 1], [3, 1]]],
		[65, 0.5, [[0, 1], [3, 4], [4, 1]]],
		[67, 1, [[5, 1]]],
		[70, 1.5, [[4, 1], [6, 2]]],
		[69, 1, [[4, 1], [7, 2]]],
		[72, 0.5, [[4, 1], [7, 4], [0, 2]]]
	];

	var node_index = 0;
	var bps = 133.0 / 60.0;  // Beats per second
	
	inf.do({
		var weight = 0;
		var random;
		var accumulator;
		var node = node_list[node_index];		
		var freq = node[0].midicps;
		var dur = node[1] / bps;
		var paths = node[2];
		
		// Get total statistical weight of connected nodes
		(0 .. paths.size - 1).do {|i| weight = weight + paths[i][1]};
		
		// Generate random value for choosing next node
		random = weight.rand;

		// Choose next node based on statistical weights
		accumulator = paths[0][1];
				
		node_index = block {|break|
			paths.size.do {|i|
				if ((random < accumulator), {
					break.value(paths[i][0])
				}, {
					accumulator = accumulator + paths[i + 1][1]					
				})
			}
		};
		
		// Play
		Synth(\my_synth, [\dur, dur, \amp, -3.dbamp, \freq, freq]);					
		dur.wait;
	})
});

t.start;
)




(
s = Server.default;
s.boot;
)

(
x = SynthDef("more_fun_snare", {
	
	arg audioOutBus = 0;
	
	var env_amp, env_pitch;
	var envGen_amp;
	var noise, pulse_cluster, envGen_hihat, out_hihat;
	var sine_noise;
	var flick, envGen_flickPitch, envGen_flickAmp;
	var trig_flick, gapTime_flick;
	var lastFlick, envGen_lastFlickPitch, envGen_lastFlickAmp, lastFlickDelayTime;
	
	var output;
	
	env_amp = Env([1.0, 1.0, 0.0], [0.0, 0.8]);
	envGen_amp = EnvGen.ar(env_amp, doneAction: 2);
	
	noise = BPF.ar(WhiteNoise.ar, 214, 0.1, 0.5);
	noise = noise + BPF.ar(WhiteNoise.ar, 1157, 0.1, 0.5);
	noise = noise + RHPF.ar(WhiteNoise.ar, 3000, 0.1, 0.2) * 0.2;
	
	pulse_cluster = Mix.fill(6, {
		LFPulse.ar(ExpRand(250, 360), Rand(0, 0.99), Rand(0.3, 0.7));
	});
	pulse_cluster = RHPF.ar(pulse_cluster, 8000, 0.1, 0.2);
	pulse_cluster = LPF.ar(pulse_cluster, 8000);
	
	envGen_hihat = EnvGen.ar(Env.perc(0.001, 0.85, 0.5, -7));
	
	sine_noise = SinOsc.ar(1131 + BrownNoise.ar.range(-90, 90)) * 0.025;
	
	out_hihat = (pulse_cluster + noise + sine_noise) * envGen_hihat * 0.7;
	
	gapTime_flick = 0.019 * TRand.kr(0.999, 1.001, 1.0);
	trig_flick = Impulse.ar(gapTime_flick.reciprocal) * EnvGen.ar(Env([1, 1, 0], [gapTime_flick * 2, 0]));
	
	envGen_flickPitch = EnvGen.ar(Env.perc(0.003, 0.024, 1.0, -4.2), 1.0, levelScale: 5300, levelBias: 50);
	envGen_flickAmp = EnvGen.ar(Env.perc(0.0, 0.04, 1.0, 5));
	flick = SinOsc.ar(envGen_flickPitch * Rand(0.999, 1.001), Rand(0, 6.2)) * envGen_flickAmp * 0.15;
	flick = flick + 
		HPF.ar(
			SinOsc.ar(envGen_flickPitch * 4.9 * Rand(0.999, 1.001), Rand(0, 6.2)) * envGen_flickAmp * 0.1,
			2000
			);
	flick = flick + CombC.ar(flick, 0.2, gapTime_flick, 0.1, 0.55);
	flick = LPF.ar(flick, 3000);
	flick = HPF.ar(flick, 140);
	
	
	envGen_lastFlickPitch = EnvGen.ar(Env.perc(0.0026, 0.022, 1.0, -7), 1.0, levelScale: 3300, levelBias: 90);
	envGen_lastFlickAmp = EnvGen.ar(Env.perc(0.0, 0.035, 1.0, 2));
	lastFlick = SinOsc.ar(envGen_lastFlickPitch * Rand(0.999, 1.001)) * envGen_lastFlickAmp * 0.8;
	
	lastFlickDelayTime = 0.054 * TRand.kr(0.99, 1.01, 1.0);
	
	lastFlick = CombC.ar(lastFlick, 0.2, lastFlickDelayTime, 0.02) * 0.09;
	lastFlick = HPF.ar(lastFlick, 200);
	output = (flick + lastFlick + out_hihat) *  envGen_amp;
	output = LPF.ar(output, 6000);
	output = output + BPF.ar(output, 6000, 0.1, 0.6);
	output = output + (trig_flick * 0.4);
	Out.ar(audioOutBus, output ! 2);
	
}).send(s);
)

(
{

100.do {
	
	Synth("more_fun_snare", [\audioOutBus, 0], s);
	(60 / 120 / 0.5).wait;
	
};


}.fork;
)


(
{
var time = 8;
var freq = (40-12).midicps;
var a = VarSaw.ar(freq/2, width: XLine.ar(0.5,1,time)).range(0,XLine.ar(1,1/1000,time));
var tone = SinOsc.ar(freq).fold(-1*a,a);
Out.ar(0, tone.dup);
}.play;
)



(
{

z = ( ( SinOsc.ar( ((LFSaw.kr(-0.05,1) +1) *100 + 20 ), LFTri.kr(40,0,1), LFSaw.kr(1.5) ) 
	* LFTri.kr(rrand(0.005,0.03),3.99.rand,0.2) ) + 
( LFPulse.ar(60,0,0.5,Pulse.ar(0.5,0.5,0.1)) * LFTri.kr(rrand(0.005,0.03),3.99.rand,0.5) ) +
( SinOsc.ar(65,0,LFPulse.kr(0.66666,0.5,0.3)*0.3) * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) )+
( LFPar.ar(120,0,LFPulse.kr(0.5,0.5,0.2)*0.3)  * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) ) +
( LFCub.ar(185,0,LFPulse.kr(0.5,0.74,0.2)*0.3)  * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) ) +
( SinOsc.ar(365,0,LFPulse.kr(1.33333333,0,0.1)*0.3) * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) ) +
( FreeVerb.ar(LFTri.ar(260,0,LFPulse.kr(0.5,0.25,0.01)*0.3),0.5,0.8,0.1) * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) ) +
( SinOsc.ar(800,0,LFPulse.kr(2,0,0.1)*0.1) * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) ) +
( LFPar.ar(820,0,LFPulse.kr(1.666666,0,0.1)*0.1) * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) ) +
( WhiteNoise.ar( LFPulse.kr(2,0.5,0.001,1) )/2 * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) ) +
( WhiteNoise.ar( LFPulse.kr(1,0.5,0.001,1) )/2 * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) ) +
( WhiteNoise.ar( LFPulse.kr(1.666666,0.5,0.001,1) )/4 * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) ) +
( WhiteNoise.ar( LFPulse.kr(1.333333,0.5,0.001,1) )/4 * LFTri.kr(rrand(0.005,0.03),3.99.rand,1) )  );

FreeVerb.ar(
	((CombC.ar(
		DelayN.ar(
			DelayN.ar(z, 0.5, [0.5,0.75], 1, z), 
		0.5, 0.5, 1, z), 
	0.1, LFNoise0.kr(1,0.05,0.001).abs, 2, z))+z),
0.1,1,0.1); 

}.play
)
TempoClock.default.tempo=1




(
{ 
	Limiter.ar(
		GVerb.ar(
			(	
				BPF.ar(
					WhiteNoise.ar([0.07,0.07]) + Blip.ar([13,19], 200.rand, mul:0.5),
					SinOsc.kr(
						SinOsc.kr([1/108,1/109]).range(1/108, 1/13)
					).exprange(10, 23000),
					PMOsc.kr(1/54,1/216, 3).range(0.1, 2)
				) 
				* 
				SinOsc.ar(Array.rand(20, 1/216, 1), mul: Array.rand(20, 0.2, 1)).reshape(10,2)
			).sum,
			roomsize:10,
			damping: PMOsc.kr(1/27, 1/108, 3).range(0.5, 1), 
			drylevel: SinOsc.kr(1/9).range(0.1, 1)
		)
		+
		GVerb.ar(
			Pan2.ar(
				LPF.ar(
					DynKlank.ar(
						`[
							Array.rand(6, 600, 4000).collect({|freq|
								SinOsc.kr(1/108).range(freq/2,freq)
							}), 
							nil, 
							Array.rand(6, 1/108, 1/27).collect({|freq|
								SinOsc.kr(freq).range(1/108,1/3)
							})
						],
						Limiter.ar(
							Dust.ar( SinOsc.kr(1/256).exprange(1/27, 3), TRand.kr(0.15, 0.25, Dust.kr(1/9))) 
							+ 
							Impulse.ar( SinOsc.kr(1/108).exprange(1/54, 3), 0, TRand.kr(0.6, 0.8, Dust.kr(1/3)))
						)
					),
					1700,
					LFPar.kr(1/27).exprange(0.05, 0.2)
				),
				SinOsc.kr(1/9).range(-0.2, 0.2)
			),
			roomsize: 30,
			drylevel: 0.5
		)
	)
}.play;
)



SynthDef(\test, {arg roomsize, revtime, damping, inputbw, spread = 15, drylevel, earlylevel,
                taillevel;
        var a = Resonz.ar(
                Array.fill(4, {Dust.ar(2)}), 1760 * [1, 2, 4, 8], 0.01).sum * 10;
//      var a = SoundIn.ar(0);
//      var a = PlayBuf.ar(1, 0);
        Out.ar(0, GVerb.ar(
                a,
                roomsize, 
                revtime, 
                damping, 
                inputbw, 
                spread, 
                drylevel.dbamp,
                earlylevel.dbamp, 
                taillevel.dbamp,
                roomsize, 0.3) + a)}).load(s)
        
s.scope(2);

// bathroom
a = Synth(\test, [\roomsize, 5, \revtime, 0.6, \damping, 0.62, \inputbw, 0.48, \drylevel -6, \earlylevel, -11, \taillevel, -13]);
a.free;

//living room
a = Synth(\test, [\roomsize, 16, \revtime, 1.24, \damping, 0.10, \inputbw, 0.95, \drylevel -3, \earlylevel, -15, \taillevel, -17]);
a.free;

//church
a = Synth(\test, [\roomsize, 80, \revtime, 4.85, \damping, 0.41, \inputbw, 0.19, \drylevel -3, \earlylevel, -9, \taillevel, -11]);
a.free;

// cathedral
a = Synth(\test, [\roomsize, 243, \revtime, 1, \damping, 0.1, \inputbw, 0.34, \drylevel -3, \earlylevel, -11, \taillevel, -9]);
a.free

// canyon
a = Synth(\test, [\roomsize, 300, \revtime, 103, \damping, 0.43, \inputbw, 0.51, \drylevel -5, \earlylevel, -26, \taillevel, -20]);
a.free;



(
SynthDef(\singverb, {
	arg out=0, amp=0.1, roomsize=16, revtime=1.24, damping=0.10, inputbw=0.95, spread = 15, drylevel= -3, earlylevel= -15,
                taillevel= -17,
				freq=200, gate=1;

		var ou, env;

		ou = SinOsc.ar(freq);
		env = EnvGen.kr(~make_adsr.(\adsr),gate,doneAction:2);
		ou = GVerb.ar(
                ou,
                roomsize, 
                revtime, 
                damping, 
                inputbw, 
                spread, 
                drylevel.dbamp,
                earlylevel.dbamp, 
                taillevel.dbamp,
                roomsize, 0.3
			) + ou;
		ou = ou*env*amp;


        Out.ar(out, ou)
}).add
)

(
SynthDef(\sincomb, {
	arg out=0, amp=0.1,
		maxdelay=0.2, delay=0.2, decay=1,
				freq=200, gate=1;

		var ou, env;

		ou = SinOsc.ar(freq);
		env = EnvGen.kr(~make_adsr.(\adsr),gate,doneAction:2);
		ou = CombC.ar(ou,maxdelay,delay,decay);
		ou = ou*env*amp;


        Out.ar(out, ou)
}).add
)



play({ PMOsc.ar(Line.kr(600, 900, 5), 600, 3, 0, 0.1) }); // modulate carfreq

play({ PMOsc.ar(300, Line.kr(600, 900, 5), 3, 0, 0.1) }); // modulate modfreq

play({ PMOsc.ar(300, 550, Line.ar(0,20,8), 0, 0.1) }); // modulate index

(
e = Env.linen(2, 5, 2);
Routine.run({ 
        loop({
                play({
                        LinPan2.ar(EnvGen.ar(e) *
                                PMOsc.ar(2000.0.rand,800.0.rand, Line.kr(0.0, 12.0.rand,9),0,0.1), 1.0.rand2)});
                2.wait;
        })
}))





(
SynthDef(\pmosc, {
	arg out=0, amp=0.1, freq=200, freqmod=10, ffreqcar=200, ffreqmod=100, rq=0.1, gate=1;

		var ou, env, envcar, envmod, envidx, envffreq, envrq;

		envcar = freq;
		envmod = EnvGen.kr(~make_adsr.(\adsr_mod),gate) * freqmod;
		envidx = EnvGen.kr(~make_adsr.(\adsr_idx),gate) * 2pi;
		envffreq = EnvGen.kr(~make_adsr.(\adsr_ffreq),gate) * ffreqmod + ffreqcar;
		envrq = EnvGen.kr(~make_adsr.(\adsr_rq),gate) * rq;
		env = EnvGen.kr(~make_adsr.(\adsr),gate,doneAction:2);
		ou = PMOsc.ar(envcar, envmod, envidx);

		ou = RLPF.ar(ou, envffreq, envrq);

		ou = ou*env*amp;


        Out.ar(out, ou)
}).add
)



a = BufferPool.get_sample(\player1,"sounds/amen-break.wav")
a = BufferPool.get_sample(\player2,"sounds/amen-break.wav")
BufferPool.retain(a,\seco2,\player2);
BufferPool.itemCount(a)
BufferPool.release(a,\player1)
BufferPool.release(a,\player2)

s.boot



(
var filechooser, ff, pp, bb, xx;

filechooser = JSCWindow( "SelectSoundFile", Rect.new( 128, 64, 400, 435 ));
 ff   = JavaObject( 'javax.swing.JFileChooser' );
 pp   = JSCPlugView( filechooser, Rect( 0, 0, 400, 400 ), ff );
 bb   = JSCButton( filechooser , Rect( 320, 401, 80, 30 )).states_([[ "OK" ]]);

 ff.setMultiSelectionEnabled(false);  // user can select several files
 ff.setControlButtonsAreShown( false );
 ff.setFileSelectionMode( 0 );  // user can select files only
 filechooser.front;
 // filechooser.visible = false;
 bb.action = {arg b;
    xx = JavaObject.newFrom(ff, \getSelectedFile);
    xx.toString.postln;
    filechooser.visible = false;
 };
)






(
w = Window.new.front;
a = StaticText(w, Rect(10, 10, 200, 40));
a.string = "an instance of String\nand this is another instance";
)

[1, 1, 1].normalizeSum.postln;

f = { arg x, a, b; (x*a)+((1-x)*b) }
g = { arg x, y, a, b, c, d; (y*((x*a)+((1-x)*b))) + ((1-y)*( (x*c)+((1-x)*d)))}
f.(1, 23, 141)

g.(0,0,23,141,12,58)
g.(0,1,23,141,12,58)
g.(1,0,23,141,12,58)
g.(1,1,23,141,12,58)
g.(0.1,1,23,141,12,58)


(
h = { arg n, controls, data;

	if(n <= 1, {
		(controls[0] * data[0]) + ((1-controls[0]) * data[1])
	}, {
		arg a;
		a = (((2**n)/2)-1).asInt;
		(controls[0] * thisFunction.(n-1, controls[1..], data[..a]))
		+ ((1-controls[0]) * thisFunction.(n-1, controls[1..], data[(a+1)..])) 

	})

}
)

h.(2, [1,0], [10,25,50,75])
h.(3, [1,1,1], a)

h.(2, [0,0], [23,141,12,58])
h.(2, [0,1], [23,141,12,58]) 
h.(2, [1,0], [23,141,12,58])
h.(2, [1,1], [23,141,12,58])

a = {arg i; i}!8
a[2..]
a[..(1+4)]
a[..b]
a[c..]
a[..4]
b = (((2**3)/2)-1).asInt
b = 2**3/2-1

c = (((2**3)/2)).asInt


f





o = ObjectTable.new

p = (bla:47, rah:[1,5,7])
o.add(p)
o
p.rah[2]=764
o.getID(p)
o.at(1058)
o.do { arg i; i.postln }


o = IdentitySet.new
o.add(p)
q = p.copy
o.add(q)
o
r = p.deepCopy
o.add(r)



a = Pseq([Pbind(\freq, Pseq([300,400]), \dur, 1), b])
a.play
b = Ppar([Pbind(\freq, Pseq([600,700]), \dur, 1), a])









// read a soundfile
s.boot;
b = Buffer.read(s, "sounds/hydrogen/GMkit/clap_Dry_c.flac");

// now play it
(
 x = SynthDef(\help_Buffer, { arg out = 0, bufnum;
	 Out.ar( out,
		 PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum))
	 )
 }).play(s,[\bufnum, b]);
)
x.free; b.free;


