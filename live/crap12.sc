
(
~make_edit_number_view = { 
	var name="plop";
	var win = Window.new(name,Rect(500,500,200,100));
	var layout = GUI.hLayoutView.new(win, Rect(0,0,200,100));
	var ez;

	ez = EZSlider(layout, 150@42, name, \freq.asSpec ,unitWidth:0, numberWidth:60,layout:\line2, margin: 1@1);
	ez.value = 90;

	ez.action = { arg ezs; ezs.value.debug("make_edit_number_view: action called") };

	layout.keyDownAction = { arg view, char, modifiers, u, k; 
		["tempo", modifiers, u].debug("KEYBOARD INPUT");
		if( u == 13 ) { ez.value.debug("lay val"); win.close };
	};
	ez.numberView.keyDownAction = { arg view, char, modifiers, u, k; 
		["tempo", modifiers, u].debug("KEYBOARD INPUT");
		if( u == 13 ) { ez.value.debug("ez val"); win.close };
	};
	win.front;

};

~make_edit_number_view.()
)



a = (x:7);
b = Set[a];
b.includes(a)
a.x = 8
[a, b, (x:8) == (x:8), b.as(Array).includes(a)]

a = (x:7);
b = IdentitySet[a];
b.includes(a)
a.x = 8
[a, b, (x:8) === (x:8), b.includes(a)]
s.boot
~a = [3,2,3];
(
~b = [
	(a:2,b:4),
	(a:5,b:8),
	(a:7,b:9)
]
)

(
Pn(Pbind(
	\notes, Plazy{ Pseq(~b) },
	\degree, Pfunc{ arg ev; ev[\notes].a },
	\dur, 1
)).trace.play
)


nil.fuck

(
Pn(Pbind(
	\degree, nil,
	\dur, 1
),inf).play
)










GUI.qt
GUI.swing
Quarks.gui
s.boot


w = Window.new("GUI Introduction").layout_(
        VLayout(
                HLayout( Button(), TextField(), Button() ),
                TextView()
        )
).front;


Help.dumpInterface
Help.gui
Help.searchGUI("Pn")
Help.all
Help.rebuildTree
HelpBrowser.
SCDoc.renderAll



(
// By James McCartney
var w, h = 700, v = 700, seed, run = true, phase = 0;
w = Window("wedge", Rect(40, 40, h, v), false);
w.view.background = Color.rand(0,0.3);
w.onClose = { run = false }; // stop the thread on close
w.front;
u=UserView(w, w.view.bounds.insetBy(50,50));
u.resize = 5;
u.background_(Color.rand);
// store an initial seed value for the random generator
seed = Date.seed;
u.drawFunc = {
        Pen.width = 2;
        Pen.use {
                // reset this thread's seed for a moment
                thisThread.randSeed = Date.seed;
                // now a slight chance of a new seed or background color
                if (0.006.coin) { seed = Date.seed; };
                if (0.02.coin) { w.view.background = Color.rand(0,0.3); };
                // either revert to the stored seed or set the new one
                thisThread.randSeed = seed;
                // the random values below will be the same each time if the seed has not changed
                // only the phase value has advanced
                Pen.translate(h/2, v/2);
                // rotate the whole image
                // negative random values rotate one direction, positive the other
                Pen.rotate(phase * 1.0.rand2);
                // scale the rotated y axis in a sine pattern
                Pen.scale(1, 0.3 * sin(phase * 1.0.rand2 + 2pi.rand) + 0.5 );
                // create a random number of annular wedges
                rrand(6,24).do {
                        Pen.color = Color.rand(0.0,1.0).alpha_(rrand(0.1,0.7));
                        Pen.beginPath;
                        Pen.addAnnularWedge(Point(0,0), a = rrand(60,300), a + 50.rand2, 2pi.rand 

                                + (phase * 2.0.rand2), 2pi.rand);
                        if (0.5.coin) {Pen.stroke}{Pen.fill};
                };
        };
};

// fork a thread to update 20 times a second, and advance the phase each time
{ while { run } { u.refresh; 0.05.wait; phase = phase + 0.01pi;} }.fork(AppClock)

)



(
~default_ccrecord = [
	(dur:0.1, val:0.5),
	(dur:0.5, val:0.2),
	(dur:0.4, val:0.1),
	(dur:0.2, val:0.3),
	(dur:0.1, val:0.9),
];
)
0.5.range(10, 20)
v.bounds.x
(
w=Window.new;
v=UserView(w, w.view.bounds.insetBy(50,50));
v.resize = 5;
v.background_(Color.rand);
v.drawFunc={|uview|
	var cur = 0;		
	var totdur = 0;
	var size = v.bounds.debug;
	var evs = ~event_rel_to_abs.(~bla);
	evs.debug("evs");
	//Pen.moveTo(0@(1-evs[0].val*size.height));
	Pen.moveTo(10@10);
	~bla.do { arg ev; totdur = ev.dur + totdur; };
	evs.do { arg ev;
		cur = cur + ev.dur;
		Pen.lineTo((ev.time/totdur * size.width) @ (1-ev.val*size.height));
		a = [(ev.time/totdur * size.width) , (1-ev.val*size.height)].debug("point ev");


	};
	Pen.lineTo((a[0]-1)@(a[1]-1));
	Pen.stroke;
};
v.mouseDownAction={v.refresh};
w.front;
)


g = Group.new
h = Group.new
s.queryAllNodes
h
(
Pdef(\bla, Pbind(
	\instrument, \pulsepass,
	\freq, 200,
	\group, g,
	\dur, 4,
	\amp, 0.1
));
)
(
Pdef(\bla2, Pbind(
	\instrument, \pulsepass,
	\freq, 400,
	\group, h,
	\dur, 8,
	\amp, 0.1
));
)

Pdef(\bla).play
Pdef(\bla).stop
Pdef(\bla2).play
Pdef(\bla2).stop

(
Pdef(\rah, Pbind(
	\type, \set,
	\id, g,
	\freq, Prand([100,150,210,300],inf),
	\dur, 0.1
))
)
(
Pdef(\rah, Pbind(
	\type, \set,
	\id, h,
	\freq, Pseq([100,150,210,300],inf),
	\dur, 0.1
))
)
(
Pdef(\rah, Pbind(
	\type, \rest,
	\args, [\set],
	\freq, Prand([100,150,210,300],inf),
	\dur, 0.1
))
)
Pdef(\rah).play;
Pdef(\rah).stop;
play


g = Group.new;

p = Pbind(
       \dur, 5,
       \freq, 220,
	   \group, g
).play;


Then you can use ~id for message style single setting

s.sendMsg("/n_set", ~id, "freq", 700)


or setting with Patterns
g
Pbind(
       \type, \set,
       \id, g,
       \dur, 0.05,
       \freq, Pxrand((1..7),4) * 220,
       \args, [\freq]  // not necessary with \freq here
).play;

s.queryAllNodes


f

(
a = Pbind( \instrument,  Prand (x , inf).trace, \freq, Pseq(k, 1) * 20, \dur, Pseq(k, inf)/2 ,    \do, Pfunc { |e| thisThread.clock.sched(0, { ~id = e[\id][0] }) }) ;
b = Pbind( \type, \set, \id , Pfunc { ~id },  \freq, Pseq(k, 1) * 102, \dur, Pseq(k, inf)/8 ,   \args, [\freq] );
Ppar([ a, b ]).play;
)



(
SynthDef(\osc,{ arg out=0, bufnum=0, numbufs = 8, sustain = 1, freq = 500, amp = 0.1, pan = 0;
        var audio;
        audio = Osc.ar(bufnum, freq);
        audio = EnvGen.ar(Env.linen(0.01, 0.90,0.9), 1, timeScale: sustain, doneAction: 2) * audio;
        audio = Pan2.ar(audio, pan, amp);
        OffsetOut.ar(out, audio);
}).add;
)

(
b = Pproto({
        ~bufnum = (type: \sine1, amps: 1.0/[1,2,3,4,5,6] ).yield;
        },
        Ppar([
                Pbind(*[
                        instrument:     \osc,
                        freq:           Pwhite(1, 16) * 100,
                        detune:         Pfunc { Array.fill(3.rand + 1, {3.0.rand}) },
                        dur:            Prand([2,2,2.5,1],10),
                        db:             Pn(Pstep([-10, -20, -20, -15, -20, -20, -20], 0.5) ),
                        legato:         Pwhite(0.0,1).linexp(0,1,0.1, 3)
                ]),
                Pbind(*[
                        type:           \sine1,
                        amps:           Pseg(Pfunc{ | ev | Array.fill(10, {1.0.rand}) }, 1),
                        numOvertones:   Pseg(Pwhite(0, 9), 10).asInteger,
                        amps:           Pfunc{ | ev | ev[\amps].copyRange(0, ev[\numOvertones]) },
                        dur:            0.05,
                        bufNum:         Pkey(\bufnum)
                ])
        ])
);
b.play
)



(
SynthDef(\shaper,{ arg out=0, bufnum=0, numbufs = 8, sustain = 1, freq = 500, amp = 0.1, pan = 0;
        var audio;
        audio = SinOsc.ar(freq);
        audio = EnvGen.ar(Env.linen(0.4, 0.50,0.9), 1, timeScale: sustain, doneAction: 2) * audio;
        audio = Shaper.ar(bufnum, audio);
        audio = Pan2.ar(audio, pan, amp);
        OffsetOut.ar(out, LeakDC.ar(audio));
}).add;

c = Pproto({
        ~bufnum = (type: \cheby, amps: 1.0/[1,2,3,4,5,6] ).yield;
        },
        Ppar([
                Pbind(*[
                        instrument:     \shaper,
                        freq:           Pwhite(1, 16) * 150,
                        detune:         Pfunc { Array.fill(3.rand + 1, {3.0.rand}) },
                        dur:            Prand([2,2,2.5,1],inf),
                        db:             Pn(Pstep([-10, -20, -20, -15, -20, -20, -20], 0.5) ),
                        legato:         Pwhite(0.0,1).linexp(0,1,1.1, 5)
                ]),
                Pbind(*[
                        type:           \cheby,
                        amps:           Pseg(Pfunc{ | ev | Array.fill(10, {1.0.rand}) }, 4),
                        dur:            0.05
                ])
        ])
);
c.play
)



(
// this kick drum doesn't sound so good on cheap speakers
// but if your monitors have decent bass, it's electro-licious
SynthDef(\kik, { |basefreq = 50, ratio = 7, sweeptime = 0.05, preamp = 1, amp = 1,
                decay1 = 0.3, decay1L = 0.8, decay2 = 0.15, out|
        var     fcurve = EnvGen.kr(Env([basefreq * ratio, basefreq], [sweeptime], \exp)),
                env = EnvGen.kr(Env([1, decay1L, 0], [decay1, decay2], -4), doneAction: 2),
                sig = SinOsc.ar(fcurve, 0.5pi, preamp).distort * env * amp;
        Out.ar(out, sig ! 2)
}).add;

SynthDef(\kraftySnr, { |amp = 1, freq = 2000, rq = 3, decay = 0.3, pan, out|
        var     sig = PinkNoise.ar(amp),
                env = EnvGen.kr(Env.perc(0.01, decay), doneAction: 2);
        sig = BPF.ar(sig, freq, rq, env);
        Out.ar(out, Pan2.ar(sig, pan))
}).add;

~commonFuncs = (
                // save starting time, to recognize the last bar of a 4-bar cycle
        init: {
                if(~startTime.isNil) { ~startTime = thisThread.clock.beats };
        },
                // convert the rhythm arrays into patterns
        pbindPairs: { |keys|
                var     pairs = Array(keys.size * 2);
                keys.do({ |key|
                        if(key.envirGet.notNil) { pairs.add(key).add(Pseq(key.envirGet, 1)) };
                });
                pairs
        },
                // identify rests in the rhythm array
                // (to know where to stick notes in)
        getRestIndices: { |array|
                var     result = Array(array.size);
                array.do({ |item, i|
                        if(item == 0) { result.add(i) }
                });
                result
        }
);
)

(
TempoClock.default.tempo = 104 / 60;

~kikEnvir = (
        parent: ~commonFuncs,
                // rhythm pattern that is constant in each bar
        baseAmp: #[1, 0, 0, 0,  0, 0, 0.7, 0,  0, 1, 0, 0,  0, 0, 0, 0] * 0.5,
        baseDecay: #[0.15, 0, 0, 0,  0, 0, 0.15, 0,  0, 0.15, 0, 0,  0, 0, 0, 0],
        addNotes: {
                var     beat16pos = (thisThread.clock.beats - ~startTime) % 16,
                        available = ~getRestIndices.(~baseAmp);
                ~amp = ~baseAmp.copy;
                ~decay2 = ~baseDecay.copy;
                        // if last bar of 4beat cycle, do busier fills
                if(beat16pos.inclusivelyBetween(12, 16)) {
                        available.scramble[..rrand(5, 10)].do({ |index|
                                        // crescendo
                                ~amp[index] = index.linexp(0, 15, 0.2, 0.5);
                                ~decay2[index] = 0.15;
                        });
                } {
                        available.scramble[..rrand(0, 2)].do({ |index|
                                ~amp[index] = rrand(0.15, 0.3);
                                ~decay2[index] = rrand(0.05, 0.1);
                        });
                }
        }
);

~snrEnvir = (
        parent: ~commonFuncs,
        baseAmp: #[0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0] * 1.5,
        baseDecay: #[0, 0, 0, 0,  0.7, 0, 0, 0,  0, 0, 0, 0,  0.4, 0, 0, 0],
        addNotes: {
                var     beat16pos = (thisThread.clock.beats - ~startTime) % 16,
                        available = ~getRestIndices.(~baseAmp),
                        choice;
                ~amp = ~baseAmp.copy;
                ~decay = ~baseDecay.copy;
                if(beat16pos.inclusivelyBetween(12, 16)) {
                        available.scramble[..rrand(5, 9)].do({ |index|
                                ~amp[index] = index.linexp(0, 15, 0.5, 1.8);
                                ~decay[index] = rrand(0.2, 0.4);
                        });
                } {
                        available.scramble[..rrand(1, 3)].do({ |index|
                                ~amp[index] = rrand(0.15, 0.3);
                                ~decay[index] = rrand(0.2, 0.4);
                        });
                }
        }
);

~hhEnvir = (
        parent: ~commonFuncs,
        baseAmp: 15 ! 16,
        baseDelta: 0.25 ! 16,
        addNotes: {
                var     beat16pos = (thisThread.clock.beats - ~startTime) % 16,
                        available = (0..15),
                        toAdd;
                        // if last bar of 4beat cycle, do busier fills
                ~amp = ~baseAmp.copy;
                ~dur = ~baseDelta.copy;
                if(beat16pos.inclusivelyBetween(12, 16)) {
                        toAdd = available.scramble[..rrand(2, 5)]
                } {
                        toAdd = available.scramble[..rrand(0, 1)]
                };
                toAdd.do({ |index|
                        ~amp[index] = ~doubleTimeAmps;
                        ~dur[index] = ~doubleTimeDurs;
                });
        },
        doubleTimeAmps: Pseq(#[15, 10], 1),
        doubleTimeDurs: Pn(0.125, 2)
);


~kik = Penvir(~kikEnvir, Pn(Plazy({
        ~init.value;
        ~addNotes.value;
        Pbindf(
                Pbind(
                        \instrument, \kik,
                        \preamp, 0.4,
                        \dur, 0.25,
                        *(~pbindPairs.value(#[amp, decay2]))
                ),
                        // default Event checks \freq --
                        // if a symbol like \rest or even just \,
                        // the event is a rest and no synth will be played
                \freq, Pif(Pkey(\amp) > 0, 1, \rest)
        )
}), inf)).play(quant: 4);

~snr = Penvir(~snrEnvir, Pn(Plazy({
        ~init.value;
        ~addNotes.value;
        Pbindf(
                Pbind(
                        \instrument, \kraftySnr,
                        \dur, 0.25,
                        *(~pbindPairs.value(#[amp, decay]))
                ),
                \freq, Pif(Pkey(\amp) > 0, 5000, \rest)
        )
}), inf)).play(quant: 4);

~hh = Penvir(~hhEnvir, Pn(Plazy({
        ~init.value;
        ~addNotes.value;
        Pbindf(
                Pbind(
                        \instrument, \kraftySnr,
                        \rq, 0.06,
                        \amp, 15,
                        \decay, 0.04,
                        *(~pbindPairs.value(#[amp, dur]))
                ),
                \freq, Pif(Pkey(\amp) > 0, 12000, \rest)
        )
}), inf)).play(quant: 4);
)

// stop just before barline
t = TempoClock.default;
t.schedAbs(t.nextTimeOnGrid(4, -0.001), {
        [~kik, ~snr, ~hh].do(_.stop);
});







(
// this SynthDef has a strong attack, emphasizing the articulation
SynthDef(\sawpulse, { |out, freq = 440, gate = 0.5, plfofreq = 6, mw = 0, ffreq = 2000, rq = 0.3, freqlag = 0.05, amp = 1|
        var sig, plfo, fcurve;
        plfo = SinOsc.kr(plfofreq, mul:mw, add:1);
        freq = Lag.kr(freq, freqlag) * plfo;
        fcurve = EnvGen.kr(Env.adsr(0, 0.3, 0.1, 20), gate);
        fcurve = (fcurve - 1).madd(0.7, 1) * ffreq;
        sig = Mix.ar([Pulse.ar(freq*1.002, 0.2), Pulse.ar(freq, 0.9), Saw.ar(freq*0.998), Saw.ar(freq*1.001), Saw.ar(freq*1.007)]);
        sig = RLPF.ar(sig, fcurve, [rq,rq*2.41]).sum
                * EnvGen.kr(Env.adsr(0.04, 0.2, 0.6, 0.1), gate, doneAction:2)
                * amp;
        Out.ar(out, sig ! 2)
}).add;
)

(
TempoClock.default.tempo = 128/60;

// Pmul does only one thing here: take ~amp from each event
// and replace it with ~amp * 0.4
p = Pmul(\amp, 0.4, Pfsm([
        #[0, 3, 1],             // starting places
        PmonoArtic(\sawpulse,
                \midinote, Pseq([78, 81, 78, 76, 78, 76, 72, 71, 69, 66], 1),
                \dur, Pseq(#[0.25, 1.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25], 1),
                \sustain, Pseq(#[0.3, 1.2, 0.3, 0.2, 0.3, 0.2, 0.3, 0.2, 0.3, 0.2],1 ),
                \amp, Pseq(#[1, 0.5, 0.75, 0.5, 0.75, 0.5, 0.75, 0.5, 0.75, 0.5], 1),
                \mw, Pseq([0, 0.03, Pseq(#[0], inf)], 1)
        ), #[1, 2, 3, 4, 7],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([64, 66, 69, 71, 72, 73], 1),
                \dur, Pseq(#[0.25], 6),
                \sustain, Pseq(#[0.3, 0.2, 0.2, 0.2, 0.3, 0.2], 1),
                \amp, Pseq(#[1, 0.5, 0.5, 0.5, 0.5, 0.5], 1),
                \mw, 0
        ), #[1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([69, 71, 69, 66, 64, 69, 71, 69], 1),
                \dur, Pseq(#[0.125, 0.625, 0.25, 0.25, 0.25, 0.25, 0.25, 0.75], 1),
                \sustain, Pseq(#[0.2, 0.64, 0.2, 0.2, 0.2, 0.3, 0.3, 0.75], 1),
                \amp, Pseq(#[0.5, 0.75, 0.5, 0.5, 0.5, 1, 0.5, 0.5], 1),
                \mw, 0
        ), #[0, 1, 1, 1, 1, 3, 3, 3, 3, 5],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([72, 73, 76, 72, 71, 69, 66, 71, 69], 1),
                \dur, Pseq(#[0.25, 0.25, 0.25, 0.083, 0.083, 0.084, 0.25, 0.25, 0.25], 1),
                \sustain, Pseq(#[0.3, 0.2, 0.2, 0.1, 0.07, 0.07, 0.2, 0.3, 0.2], 1),
                \amp, Pseq(#[1, 0.5, 0.5, 1, 0.3, 0.3, 0.75, 0.75, 0.5], 1),
                \mw, 0
        ), #[1, 1, 1, 1, 3, 3, 4, 4, 4],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([64, 66, 69, 71, 72, 73, 71, 69, 66, 71, 69, 66, 64, 69], 1),
                \dur, Pseq(#[0.25, 0.25, 0.25, 0.25, 0.125, 0.375, 0.166, 0.166, 0.168,
                                0.5, 0.166, 0.166, 0.168, 0.5], 1),
                \sustain, Pseq(#[0.3, 0.2, 0.2, 0.2, 0.14, 0.4, 0.2, 0.2, 0.2, 0.6, 0.2, 0.2, 0.2, 0.5],1),
                \amp, Pseq(#[0.5, 0.5, 0.6, 0.8, 1, 0.5, 0.5, 0.5, 0.5, 1,
                        0.5, 0.5, 0.5, 0.45], 1),
                \mw, 0
        ), #[0, 1, 1, 1, 1, 3, 3, 5],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([72, 73, 76, 78, 81, 78, 83, 81, 84, 85], 1),
                \dur, Pseq(#[0.25, 0.25, 0.25, 0.25, 0.5, 0.5, 0.5, 0.5, 0.125, 1.125], 1),
                \sustain, Pseq(#[0.3, 0.2, 0.2, 0.2, 0.95, 0.25, 0.95, 0.25, 0.2, 1.13], 1),
                \amp, Pseq(#[0.7, 0.5, 0.5, 0.5, 0.7, 0.5, 0.8, 0.5, 1, 0.5], 1),
                \mw, Pseq([Pseq(#[0], 9), 0.03], 1)
        ), #[6, 6, 6, 8, 9, 10, 10, 10, 10, 11, 11, 13, 13],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([83, 81, 78, 83, 81, 78, 76, 72, 73, 78, 72, 72, 71], 1),
                \dur, Pseq(#[0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,
                                0.25, 2], 1),
                \sustain, Pseq(#[0.3, 0.3, 0.2, 0.3, 0.3, 0.3, 0.2, 0.3, 0.2, 0.3, 0.2, 0.3, 2], 1),
                \amp, Pseq(#[0.5, 0.5, 0.5, 0.8, 0.5, 0.5, 0.5, 0.8, 0.5, 0.8, 0.5,
                                1, 0.4], 1),
                \mw, Pseq([Pseq([0], 12), 0.03], 1)
        ), #[0, 7, 7, 7, 7, 7, 3, 3, 3, 3],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([69, 71, 72, 71, 69, 66, 64, 69, 71], 1),
                \dur, Pseq(#[0.25, 0.25, 0.25, 0.25, 0.166, 0.167, 0.167, 0.25, 0.25], 1),
                \sustain, Pseq(#[0.2, 0.2, 0.3, 0.2, 0.2, 0.2, 0.14, 0.3, 0.2], 1),
                \amp, Pseq(#[0.5, 0.5, 0.8, 0.5, 0.5, 0.5, 0.5, 0.8, 0.5], 1)
        ), #[3, 3, 3, 4, 4, 5],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([84, 85, 84, 84, 88, 84, 83, 81, 83, 81, 78, 76, 81, 83], 1),
                \dur, Pseq(#[0.125, 0.535, 0.67, 1.92, 0.25, 0.166, 0.167, 0.167,
                                0.25, 0.25, 0.25, 0.25, 0.25, 0.25], 1),
                \sustain, Pseq(#[0.2, 3.12, 0.2, 0.2, 0.2, 0.2, 0.2, 0.15, 0.3, 0.2, 0.2, 0.2,
                                0.3, 0.2], 1),
                \amp, Pseq(#[1, 0.8, 0.8, 0.8, 1, 1, 0.8, 0.8, 1, 0.8, 0.8, 0.8,
                                1, 0.8], 1),
                \mw, Pseq([0, 0.005, 0.005, 0.06, Pseq(#[0], 10)], 1)
        ), #[10, 10, 10, 11, 11, 11, 11, 12, 12, 12],

                // same as #4, 8va
        PmonoArtic(\sawpulse,
                \midinote, Pseq(([64, 66, 69, 71, 72, 73, 71, 69, 66, 71, 69, 66, 64, 69]+12), 1),
                \dur, Pseq(#[0.25, 0.25, 0.25, 0.25, 0.125, 0.375, 0.166, 0.166, 0.168,
                                0.5, 0.166, 0.166, 0.168, 0.5], 1),
                \sustain, Pseq(#[0.3, 0.2, 0.2, 0.2, 0.14, 0.4, 0.2, 0.2, 0.2, 0.6, 0.2, 0.2, 0.2, 0.5],1),
                \amp, Pseq(#[0.5, 0.5, 0.6, 0.8, 1, 0.5, 0.5, 0.5, 0.5, 1,
                        0.5, 0.5, 0.5, 0.45], 1),
                \mw, 0
        ), #[11, 11, 11, 11, 11, 12, 12],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([81, 84, 83, 81, 78, 76, 81, 83], 1),
                \dur, Pseq(#[0.25], 8),
                \sustain, Pseq(#[0.2, 0.3, 0.3, 0.2, 0.3, 0.2, 0.3, 0.2], 1),
                \amp, Pseq(#[0.5, 1, 0.5, 0.5, 0.6, 0.5, 0.8, 0.5], 1),
                \mw, 0
        ), #[0, 9, 9, 11, 11, 12, 12, 12, 12, 12],

                // same as #1, 8va
        PmonoArtic(\sawpulse,
                \midinote, Pseq(([64, 66, 69, 71, 72, 73]+12), 1),
                \dur, Pseq(#[0.25], 6),
                \sustain, Pseq(#[0.3, 0.2, 0.2, 0.2, 0.3, 0.2], 1),
                \amp, Pseq(#[1, 0.5, 0.5, 0.5, 0.5, 0.5], 1),
                \mw, 0
        ), #[6, 6, 8, 9, 9, 9, 9, 10, 10, 10, 10, 13, 13, 13],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([78, 81, 83, 78, 83, 84, 78, 84, 85], 1),
                \dur, Pseq(#[0.25, 0.25, 0.5, 0.25, 0.25, 0.5, 0.25, 0.25, 1.75], 1),
                \sustain, Pseq(#[0.2, 0.3, 0.2, 0.2, 0.3, 0.2, 0.2, 0.3, 1.75], 1),
                \amp, Pseq(#[0.4, 0.8, 0.5, 0.4, 0.8, 0.5, 0.4, 1, 0.8], 1),
                \mw, Pseq([Pseq([0], 8), 0.03], 1)
        ), #[8, 13, 13],

        PmonoArtic(\sawpulse,
                \midinote, Pseq([88, 84, 83, 81, 83, 81, 78, 76, 81, 83], 1),
                \dur, Pseq(#[0.25, 0.166, 0.167, 0.167,
                                0.25, 0.25, 0.25, 0.25, 0.25, 0.25], 1),
                \sustain, Pseq(#[0.2, 0.2, 0.2, 0.15, 0.3, 0.2, 0.2, 0.2,
                                0.3, 0.2], 1),
                \amp, Pseq(#[1, 1, 0.8, 0.8, 1, 0.8, 0.8, 0.8,
                                1, 0.8], 1),
                \mw, 0
        ), #[10]
], inf)).play;
)

p.stop;



(
var     bufCount;
~midinotes = (39, 46 .. 88);
bufCount = ~midinotes.size;

fork {
                // record the samples at different frequencies
        b = Buffer.allocConsecutive(~midinotes.size, s, 44100 * 2, 1);
        SynthDef(\sampleSource, { |freq = 440, bufnum|
                var     initPulse = Impulse.kr(0),
                        mod = SinOsc.ar(freq) * Decay2.kr(initPulse, 0.01, 3) * 5,
                        car = SinOsc.ar(freq + (mod*freq)) * Decay2.kr(initPulse, 0.01, 2.0);
                RecordBuf.ar(car, bufnum, loop: 0, doneAction: 2);
        }).send(s);
        s.sync;
                // record all 8 buffers concurrently
        b.do({ |buf, i|
                Synth(\sampleSource, [freq: ~midinotes[i].midicps, bufnum: buf]);
        });
};
o = OSCFunc({ |msg|
        bufCount = bufCount - 1;
        if(bufCount == 0) {
                "done recording".postln;
                o.free;
        };
}, '/n_end', s.addr);

SynthDef(\multiSampler, { |out, bufnum, bufBase, baseFreqBuf, freq = 440, amp = 1|
        var     buf1 = bufnum.floor,
                buf2 = buf1 + 1,
                xfade = (bufnum - buf1).madd(2, -1),
                basefreqs = Index.kr(baseFreqBuf, [buf1, buf2]),
                playbufs = PlayBuf.ar(1, bufBase + [buf1, buf2], freq / basefreqs, loop: 0, doneAction: 2),
                sig = XFade2.ar(playbufs[0], playbufs[1], xfade, amp);
        Out.ar(out, sig ! 2)
}).add;

~baseBuf = Buffer.alloc(s, ~midinotes.size, 1, { |buf| buf.setnMsg(0, ~midinotes.midicps) });
)

(
TempoClock.default.tempo = 1;
p = Pbind(
        \instrument, \multiSampler,
        \bufBase, b.first,
        \baseFreqBuf, ~baseBuf,
        \degree, Pseries(0, Prand(#[-2, -1, 1, 2], inf), inf).fold(-11, 11),
        \dur, Pwrand([0.25, Pn(0.125, 2)], #[0.8, 0.2], inf),
        \amp, Pexprand(0.1, 0.5, inf),
                // some important conversions
                // identify the buffer numbers to read
        \freq, Pfunc { |ev| ev.use(ev[\freq]) },
        \bufnum, Pfunc({ |ev| ~midinotes.indexInBetween(ev[\freq].cpsmidi) })
                .clip(0, ~midinotes.size - 1.001)
).play;
)

p.stop;
b.do(_.free); ~baseBuf.free;
