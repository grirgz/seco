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
