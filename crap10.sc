
// tempo view

(
"ibla".debug;

~make_tempo_view = { arg clock_manager;
	var win, vlayout, but_layout, bt, bar, temp, pos, clock, task, color;
	var size = 100@200;

	"hein".debug;
	clock = clock_manager.get_clock;

	win = Window.new("plop", Rect(0,0,size.x,180));
	vlayout = VLayoutView(win, Rect(0,0,size.x,200));
	temp = StaticText(vlayout, 20);
	temp.string = "Tempo:" + clock.tempo.asString;
	pos = StaticText(vlayout, 20);
	pos.string = "002 / 004 | 2 | 1254";
	but_layout = HLayoutView(vlayout, Rect(0,0,size.x,20));
	bt = { arg i;
		StaticText(but_layout, Rect(0,0,20,20)).background_(Color.black);
	} ! clock.beatsPerBar;
	bar = RangeSlider(vlayout, 40@20);
	win.front;



	clock.postln;
	task = clock.playNextBar({ Task({ 
			clock_manager.start_pos = clock.beats; // debug
			10000.do ({ //FIXME: fake loop
				clock.beatInBar.debug("beatInBar");
				bt[clock.beatInBar].background = if(clock_manager.is_near_end) { Color.red } { Color.green };
				bt.wrapAt(clock.beatInBar -1).background = Color.black;
				pos.string = (clock.beats - clock_manager.start_pos).asString 
							+ "/" + clock_manager.play_length.asString
							+ "|" + clock.beats.asString; // FIXME: find a way to zero pad
				bar.range = (clock.beats - clock_manager.start_pos) / clock_manager.play_length;
				1.wait;
			})
		}).play(clock)
	});

	win.onClose = win.onClose.addFunc { "close".postln; task.stop };

	~make_view_responder.(vlayout, clock_manager, (
		tempo: { arg obj, msg, tempo;
			temp.string = "Tempo: " ++ tempo.asString;
		},
		pos: { arg obj, msg, position;
			pos.string = clock.beats.asString;
		}

	));

};
~clock_manager = (
	myclock: TempoClock.new,
	//start_pos: TempoClock.default.beats,
	start_pos: 0,
	play_length: 16,
	get_clock: { arg self; self.myclock },
	is_near_end: { arg self;
		(self.myclock.beats - self.start_pos) > (self.play_length - 1 - self.myclock.beatsPerBar)
	}
);
~clock_manager.myclock.tempo = 0.5;


"bla".debug;
~make_tempo_view.(~clock_manager);
)


a = (0..3);
a.wrapAt(-1)




~buf = Buffer.alloc(s, 44100 * 8, 2);
~buf2 = Buffer.alloc(s, 44100 * 8, 2);
~buf.bufnum

(
SynthDef(\record_input, { arg out = 0, bufnum = 0;
		var input;
        input = SoundIn.ar([0,1]);
        RecordBuf.ar(input, bufnum, doneAction: 2, loop: 0);
}).add;
SynthDef(\audiotrack, { arg out = 0, amp=1.0, bufnum = 0;
        var playbuf;
        playbuf = PlayBuf.ar(2,bufnum,startPos:44100*0.046,doneAction:2);
        Out.ar(out, playbuf * amp);
}).add;
)

Synth(\audiotrack, [\bufnum, 12])

// works!!

(
a = Synth(\record_input, [\bufnum, ~buf]);
b = Pbind(\dur, Pn(1,8), \legato, 0.1).play // metronome
)

(
//verify sync
c = Synth(\audiotrack, [\bufnum, ~buf]);
b = Pbind(\dur, Pn(1,8), \legato, 0.1).play
)

(
//record another buffer in sync
a = Synth(\record_input, [\bufnum, ~buf2]);
c = Synth(\audiotrack, [\bufnum, ~buf]);
)

(
// the three tracks are sync
b = Pbind(\dur, Pn(1,8), \legato, 0.1).play;
c = Synth(\audiotrack, [\bufnum, ~buf]);
c = Synth(\audiotrack, [\bufnum, ~buf2]);
)



// 

(
a = Synth(\record_input, [\bufnum, ~buf]);
b = Pbind(\dur, Pn(1,8), \legato, 0.1).play
)

(
c = Pbind(\instrument, \audiotrack, \bufnum, ~buf, \dur, 8).play;
d = Synth(\audiotrack, [\bufnum, ~buf]);
)



(
c = Pbind(\instrument, \audiotrack, \bufnum, ~buf, \dur, 8).play(quant:8);
TempoClock.default.play({ Synth(\audiotrack, [\bufnum, ~buf]) },quant:8);
)


(
Synth(\default);
)
(
(instrument:\default).play;
Pbind(\dur, 8).play;
)

(
a = Ppar([
	Pbind(\instrument, \record_input, \bufnum, ~buf, \dur, Pn(8,1)),
	Pbind(\dur, Pn(1,8), \legato, 0.1)
]).trace.play;
)






(
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
                roomsize, 0.3) + a)}).add

)
s.scope(2);

//




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
Ndef(\elecguitare, { 
var ou;
//ou = SoundIn.ar([0,1]);
ou = SinOsc.ar(200*(SinOsc.ar(100)*50));
ou = SinOsc.ar(200);
ou = ou * EnvGen.ar(Env.perc(0.01,0.01), gate: Impulse.ar(1));
//ou = ou.dup;
//ou = (Amplitude.kr(ou.sum,0,1.5) > 0.05) * ou;
//ou = ((ou *1000).tanh*1100).distort/10 + ou
//ou = ((ou *1000).tanh*1100).distort/10
//ou = (ou*10).distort;
ou * GVerb.ar (ou, roomsize: 100, revtime: 15.3, damping: 0.1, inputbw: 0.9, spread: 15, drylevel: 1, earlyreflevel: 0.7, taillevel: 0.5, maxroomsize: 300);
ou

}).play;

)





SoundIn.a
{ SoundIn.ar([0,1]) }.plot;
{ SoundIn.ar(0) }.play;
(
{
	var ga, ou;
	//ga = Amplitude.ar(SoundIn.ar(0)) * 10;
	ga = SoundIn.ar(0) * 10;
	ou = SinOsc.ar(300 * SinOsc.kr(15) + (ga * 300).abs + 50);
}.play;
)
s.boot


{100.rand}.dup(8).sort
(
x = Array.rand(50, 0, 10); // initial array of 50 values
r = List.new;
// generate an array of 8 random length without duplicate
while({r.size < 7}) {
	d = (x.size-1).rand;
	if( r.includes(d).not ) {
		r.add(d)
	}
};
r.sort;
// separate
a = -1;
b = x.separate({ a = a +1; if(a == r.first) { r.removeAt(0); true } { false }; });
b.postcs;
b.size;
)
1.coin
[].first
List[]k


x = Array.rand(50, 0, 10); // initial array of 50 values
(
y = Array.rand(8, 1, 10); // 8 arrays of random sizes
y.postln;
y.normalizeSum.postln;
(y.normalizeSum * 50).postln;
(y.normalizeSum * 49).round(1).postln;
z = x.clumps((y.normalizeSum * 49).round(1));
z.postcs;
z.size
)
y

Pbind(\instrument, \plop, \sustain, 2).play;
TempoClock.default.tempo = 3

SynthDef(\plop, { arg sustain;
	sustain.poll;
}).add;


SynthDesc.mdPlugin = TextArchiveMDPlugin; // plugin to store metadata on disk when storing the synthdef
SynthDesc.mdPlugin 

SynthDef(\pulsepass,{ arg out=0, gate=1, amp=0.1, pan=0, noise=1, freq=250, bpffratio1=1, bpffratio2=1, bpfrq1=1, bpfrq2=1, apdec1=1, apdec2=1;
	var nois, gen, genenv, ou, ou2;

	gen = Pulse.ar([1.01,1,0.996998]*freq);
	nois = PinkNoise.ar(noise);
	gen = gen+nois;
	genenv = gen * EnvGen.ar(~make_adsr.(\adsr_pre),gate,doneAction:0);
	ou = genenv;
	ou = AllpassL.ar(ou, 1/30, (freq*0.99).reciprocal,apdec1);
	ou = AllpassL.ar(ou, 1/30, (freq*1.09).reciprocal,apdec2);
	ou = ou.sum;
	ou2 = BPF.ar(genenv, freq*bpffratio1,bpfrq1);
	ou = BPF.ar(ou, freq*bpffratio2,bpfrq2);
	ou = ou + ou2;
	ou = ou * EnvGen.ar(~make_adsr.(\adsr),gate,doneAction:2);

	ou = Pan2.ar(ou,pan, amp);
	Out.ar(out, ou);
}, metadata:(specs:(
	noise: ControlSpec(0, 5, \lin, 0.0001, 0),
	bpffratio1: ControlSpec(0, 3, \lin, 0, 1),
	bpffratio2: ControlSpec(0, 3, \lin, 0, 1),
	bpfrq1: ControlSpec(0.000001, 3, \exp, 0, 1),
	bpfrq2: ControlSpec(0.000001, 3, \exp, 0, 1),
	apdec1: ControlSpec(0.000001, 3, \exp, 0, 1),
	apdec2: ControlSpec(0.000001, 3, \exp, 0, 1)
))).store;


SynthDescLib.global.read("synthdefs/*.scsyndef");
SynthDescLib.global.browse


SynthDescLib.global[\pulsepass].makeWindow;
SynthDescLib.global[\pulsepass]
SynthDescLib.global.synthDescs[\pulsepass].metadata




(
       var size = 512;

       r = Signal.fill(size, { |i| 2/size *i -1});
       c = Signal.fftCosTable( size );
       i = Signal.newClear( size );

       f = fft( r, i, c );

       p = f.phase[size div: 2..];
       m = f.magnitude[..size div: 2];

       h = Signal.sineFill(size, m, p);

       [r,h].flop.flat.plot(numChannels:2)

)
Main.version

Quarks.gui



(

b = BSpline([ [ 0.42695473251029, 2.275 ], [ 1, 1 ], [ 2.5102880658436, 3.1 ], [ 4, 4 ] ]);

b.gui;

// to use X as time we need y values spaced in even X units

d = b.bilinearInterpolate(512);


d.plot2;

x = 5.0/512;

// collect into points for plotting

e = d.collect({ |dd,i| [x * i,dd] });


w = Window(bounds: Rect(40, 40, 800, 800)).front;

a = ScatterView(w, Rect(10, 10, 760, 760), e, ControlSpec(0.0,5.0), ControlSpec(0.0,5.0));

a.drawAxis_(true).drawMethod_(\fillOval).symbolColor_(Color.blue(0.5, 0.5)).symbolSize_(5);


)

(
SynthDef(\metronome, { arg out=0, amp=1, gate=1, freq=220, pan=0;
	var ou;
	ou = SinOsc.ar([freq,freq/2])+LFSaw.ar(freq+0.1);
	ou = LPF.ar(ou,freq,0.1);
	ou = ou * EnvGen.ar(Env.asr(0.0001, 1, 0.01),gate, doneAction:2);
	ou = Pan2.ar(ou,pan,amp*2);
	Out.ar(out,ou);
}).store;
)



(
Pbind(\instrument, \metronome,
	\freq, 440,
	\sustain, 0.1,
	\dur, Pn(1,256)
).play;
)

Date.getDate.rawSeconds
c = (note:40, bla:54)
c.hash
a.hash
b = a.deepCopy
b.hash
a.note = 41




(
c = Condition.new;

Routine {
        1.wait;
        "waited for 1 second".postln;
        1.wait;
        "waited for another second, now waiting for you ... ".postln;
        c.hang;
        "the condition has stopped waiting.".postln;
        1.wait;
        "waited for another second".postln;
        "waiting for you ... ".postln;
        c.hang;
        "the condition has stopped waiting.".postln;
}.play;
)

// continue
c.unhang;



(
c = Condition.new(false);

Routine {
        1.wait;
        "waited for 1 second".postln;
        1.wait;
        "waited for another second, now waiting for you ... ".postln;
        c.wait;
        "the condition has stopped waiting.".postln;
        1.wait;
        "waited for another second".postln;
        "waiting for you ... ".postln;
                c.test = false;
                c.wait;
        "the condition has stopped waiting.".postln;
        1.wait;
        "the end".postln;
}.play;
)

// continue
(
c.test = true;
c.signal;
)

// a typical use is a routine that can pause under certin conditions:
(
c = Condition.new;
fork { loop { 1.wait; "going".postln; c.wait } };
)
c.test = true; c.signal;
c.test = false;
c.test = true;
c.signal


c = Condition.new;
(
Pbind(
	\freq, Prout({
		loop {
			200.yield;
			c.wait;
		}
	}),
	\dur, 1
).play
)
(
Pbind(
	\freq, 200,
	\dur, 1
).play
)











(
~bla = {
	var window, sl_layout;
	var txt;
	window = Window.new("bla", Rect(0,0,1500,400));	

	sl_layout = GUI.hLayoutView.new(window, Rect(0,0,1500,60*16));
	txt = StaticText.new(sl_layout, Rect(0,0,100,100));
	txt.string = "blaaaaaaaaaaaaaa";
	txt = Button.new(sl_layout, Rect(0,0,100,100));
	


	window.view.keyDownAction = { arg view, char, modifiers, unicode, keycode;
		[view, char, modifiers, unicode, keycode].debug("view, char, modifiers, unicode, keycode");
	};
	window.front;
};
~bla.value
)
