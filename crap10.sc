
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


















SoundIn.a
{ SoundIn.ar(0) }.plot;
(
{
	var ga, ou;
	//ga = Amplitude.ar(SoundIn.ar(0)) * 10;
	ga = SoundIn.ar(0) * 10;
	ou = SinOsc.ar(300 * SinOsc.kr(15) + (ga * 300).abs + 50);
}.play;
)
s.boot




Pbind(\instrument, \plop, \sustain, 2).play;
TempoClock.default.tempo = 3

SynthDef(\plop, { arg sustain;
	sustain.poll;
}).add;


SynthDesc.mdPlugin = TextArchiveMDPlugin; // plugin to store metadata on disk when storing the synthdef
SynthDesc.mdPlugin = 

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


SynthDescLib.global.read("synthdefs/");
SynthDescLib.global.browse


SynthDescLib.global[\pulsepass].makeWindow;




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
