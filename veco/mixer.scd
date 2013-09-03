// mixer definitions
(
MixerChannelDef(\mix1x4, 1, 4, 
   SynthDef(\mxb1x4, { |busin, busout, xpos, ypos, level|
      var   sig = In.ar(busin, 1);
      sig = Pan4.ar(sig, xpos, ypos, level);
      Out.ar(busout, sig);
      ReplaceOut.ar(busin, sig);
   }),
   (xpos: { |name| MixerControl(name, nil, 0, \bipolar) },
   ypos: { |name| MixerControl(name, nil, 0, \bipolar) },
   level: { |name| MixerControl(name, nil, 0.75, \amp) })
);

d = MixerGUIDef(Point(50, 330), 
   [MixerMuteWidget, MixerRecordWidget, MixerPresendWidget, Mixer2DPanWidget,
      MixerLevelSlider, MixerLevelNumber, MixerPostsendWidget, MixerNameWidget,
      MixerOutbusWidget],
   [Rect(0, 0, 20, 20),
   Rect(30, 0, 20, 20), 
   Rect(0, 25, 50, 30), 
   Rect(0, 65, 50, 50),
   Rect(10, 125, 30, 100), 
   Rect(0, 230, 50, 15), 
   Rect(0, 250, 50, 30), 
   Rect(0, 285, 50, 20),
   Rect(0, 310, 50, 20)
]);

MixerChannelDef(\mix1x4).guidef = d;
)

// now create the mixer and the gui
m = MixerChannel(\test, s, 1, 4);
MixingBoard(\test, nil, m);

// play some sound
a = m.play({ SinOsc.ar(Lag.kr(LFNoise0.kr(8).range(200, 800), 0.07), 0) });

// use the mouse to move the panner around in the box

// automate and watch
m.automate(\xpos, { LFNoise1.kr(0.2) });
m.automate(\ypos, { LFNoise1.kr(0.3) });

m.watch(\xpos); m.watch(\ypos)

m.outbus;

(
Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0,2,4,3,2,1],inf) + Pseq([1,2,3,4],inf) + Pwhite(0,1),
	\octave, 4,
	\dur, 1/4,
	\legato, 0.01,
	\out, m.inbus,
	\amp, 0.1
)).play;
);




(
SynthDef(\echo, { arg out=0, in=0, amp=0.1, gate=1, pan=0, freq=200;
	var sig;
	sig = In.ar(in, 2);
	sig = CombL.ar(sig, 0.2,0.2,1);
	Out.ar(out, sig);
}).add;
);


(
Ndef(\echo, { arg outbus=0, in=0, amp=0.1;
	var sig;
	sig = In.ar(outbus, 2);
	sig = CombL.ar(sig, 0.2,0.001,1);
	ReplaceOut.ar(outbus, sig);
}).play;
)
Ndef(\echo).set(\outbus, n.inbus.index)
n.playfx(Ndef(\echo))

n.outbus.index
n.inbus
m.inbus
m.outbus




s = Server.internal; s.boot;


SynthDef.new("pinkfilt", {

arg	outbus,	// outbus is the standard name used by MixerChannel

freq, rq;

Out.ar(outbus, RLPF.ar(PinkNoise.ar, freq, rq));

}).send(s);


m = MixerChannel.new("pinkfilt", s);  // defaults to mono-to-stereo


a = m.play("pinkfilt", [\freq, 2000, \rq, 0.02]);

~master = MixerChannel(\master, s, 2, 2, level: 0.2);

m.free;	// all synths disappear



m = Array.fill(5, { arg i; MixerChannel("test" ++ (i+1), s) });

n = MixerChannel("essai", s);

z = MixingBoard("test board", nil, m, n); 
