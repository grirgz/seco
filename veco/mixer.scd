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
   level: { |name| MixerControl(name, nil, 0.75, \amp) }));

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
