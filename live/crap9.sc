s.boot

{ In.ar(1)*110 }.play
Exception.debug = false; 
if(nil.isNil) { Error("assert").throw }
"oo".inform


(
 var clock, sf, cut;
 
 TempoClock.default.tempo_(180/60);
 clock = ExternalClock(TempoClock.default); 
 clock.play;
 
 sf= BBCutBuffer("sounds/amen-break.wav",32);

 Routine.run({
  s.sync; // this tells the task to wait

  cut = BBCut2(CutBuf3(sf, 0.4), BBCutProc11(8, 4, 2, 2, 0.2)).play(clock);

  30.wait; //  // let things run for 30 seconds
  
  cut.stop;
  cut.free;
 })
)
M6ain.version


(

w = Window.new.front;

a = RangeSlider(w, Rect(20, 80, 120, 30))

.lo_(0.2)

.range_(0.4)

.action_({ |slider|

[\sliderLOW, slider.lo, \sliderHI, slider.hi].postln;

});

)


(

n=20;

w = Window.new.front;

m = MultiSliderView(w,Rect(10,10,n*13+2,100)); //default thumbWidth is 13

m.value = Array.fill(n, {|v| v*0.05}); // size is set automatically when you set the value

m.action = { arg q;

q.value.postln;

};

)


( // all features, small font

g=EZRanger(nil, 400@16," freq  ", \freq,

initVal:[100.rand,200+2000.rand],unitWidth:30, numberWidth:60,layout:\horz, margin:2@2);

g.setColors(Color.grey,Color.white, Color.grey(0.7),

Color.grey, Color.white, Color.yellow, background:Color.grey(0.7), 

knobColor: HiliteGradient(Color.grey, Color.white));

g.window.bounds = g.window.bounds.moveBy(-180,50);

g.font_(Font("Helvetica",10));

);


( // no unitView

g=EZRanger(nil, 400@16," freq  ", \freq,initVal:[100.rand,200+2000.rand],

unitWidth:0, numberWidth:60,layout:\horz, margin:2@2);

g.setColors(Color.grey,Color.white, Color.grey(0.7),

Color.grey, Color.white, Color.yellow, background:Color.grey(0.7), 

knobColor: HiliteGradient(Color.grey, Color.white));

g.window.bounds = g.window.bounds.moveBy(-180, -20);

g.font_(Font("Helvetica",10));

);

( // no label, so use window name as label

g=EZRanger(nil, 400@16, nil, \freq,initVal:[100.rand,200+2000.rand],

unitWidth:0, numberWidth:60,layout:\horz, margin:2@2);

g.setColors(Color.grey,Color.white, Color.grey(0.7),

Color.grey, Color.white, Color.yellow, background:Color.grey(0.7), 

knobColor: HiliteGradient(Color.grey, Color.white));

g.window.bounds = g.window.bounds.moveBy(-180, -90);

g.window.name="Freq";

g.font_(Font("Helvetica",10));

);



b = Buffer.

(
( 'instrument': \monosampler, 'noteline': ( 'dur': 0.01, 'midinote': \rest, 'sustain': 0.1 ), 'gate': 1, 'loop': 0, 
  'segdur': 0.25, 'current_mode': \stepline, 'out': 0, 'stepline': 1, 'muted': false, 
  'bufnum': 1, 'repeat': 1, 'type': \note, 'dur': 1, 'speed': 1, 
  'amp': 0.60000002384186, 'sustain': 0.5, 'pan': 0, 'pos': 0, 'legato': 0.5, 
  'elapsed': 4, 'samplekit': \default, 'stretchdur': 1, 'sampleline': ( 'dur': 0.01, 'midinote': \rest, 'sustain': 0.1 ) ).play
)


s.queryAllNodes



b = Buffer.read(s, "/home/ggz/Musique/pppp-Track_2-1.wav");

// now play it
(
x = SynthDef(\help_Buffer, { arg out = 0, bufnum;
	Out.ar( out,
		PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), loop:1) ! 2
	)
}).play(s,[\bufnum, b]);
)
x.free; b.free;
