p = ProxySpace.push
Pdef(\x).stop;
Pdef(\x).play;
s.meter;
(
SynthDef(\drums, {|out = 0, gate=1, bassLevel = 1 , snareLevel = 1, hatLevel = 1, 
					tomLevel = 0, pan1 = 0, pan2 = 0,release=0.5,freq1=800, frq0= 40, frq1 = 50|
	var env3, env4, bass, snare, hat, tom, bassOut, snareOut, hatOut, tomOut, mixer;
	var sig;
	bass= SinOsc.ar(frq0)+LFPulse.ar(frq1)+Impulse.ar(1);
	bassOut = Pan2.ar(bass * 1, pan1, bassLevel).distort ;
	snare = WhiteNoise.ar;
	snareOut =Pan2.ar( FBSineN.ar(SampleRate.ir/100,1, Line.kr(0.01, 40, 1) % snare, 1, 0.1), pan2, snareLevel);
	hat = RLPF.ar(WhiteNoise.ar, MouseX.kr(1e2,2e4,1), 0.2, 1);
	hatOut = Pan2.ar(hat, pan1, hatLevel) * 0.1 ;
	mixer = Mix.new([bassOut,snareOut,hatOut]) * 0.7;
	sig = bassOut * EnvGen.kr(Env.perc(0.002,1, 1, -2), 1, doneAction:2);
	Out.ar(0, Limiter.ar( sig * 0.2,1,0.01));
	//Out.ar(0, Limiter.ar(0.1 * snareOut* EnvGen.kr(Env.perc(0.002,0.1, 1, -2), 1, doneAction:2); ,1,0.01));
	//Out.ar(0, Limiter.ar(0.3 * hatOut* EnvGen.kr(Env.perc(0.002,1, 1, -2), 1, doneAction:2); ,1,0.01));
}).store;
)

(
Pdef(\x,
	Pbind( 
		\instrument, \drums,
		\dur, 1.4,
		\snareLevel, 0,
		\hatLevel, 0,
		//\bassLevel, Pseq ([a] ,inf), 
		//\freq1,Pseq([50,20,10,5000,20,100,500,20,10,10,20,30,40],inf),
		//\frq1,Pseq([40,150,180,200,40,150,180,200],inf),
		//\frq0,Pseq([80,80,80,80,60,80,80,60,80,80,80,80],inf),
		//\snareLevel, Pseq ([b],inf),\hatLevel, Pseq ([c],inf)
	);

).play
)
p.clock.tempo = 1.2;


Env.perc(0.002,1, 1, -2).plot





Pdef(\x).stop;
Pdef(\x).play;
s.meter;
(
SynthDef(\drums, {|out = 0, bassLevel = 0 , snareLevel = 0, hatLevel = 0, tomLevel = 0, pan1 = 0, pan2 = 0,release=0.5,freq1=800, frq0= 40, frq1 = 50|
var env3, env4, bass, snare, hat, tom, bassOut, snareOut, hatOut, tomOut, mixer;
bass= SinOsc.ar(frq0)+LFPulse.ar(frq1)+Impulse.ar(1);
bassOut = Pan2.ar(bass * 1, pan1, bassLevel).distort ;
snare = WhiteNoise.ar;
snareOut =Pan2.ar( FBSineN.ar(SampleRate.ir/100,1, Line.kr(0.01, 40, 1) % snare, 1, 0.1), pan2, snareLevel);
hat = RLPF.ar(WhiteNoise.ar, MouseX.kr(1e2,2e4,1), 0.2, 1);
hatOut = Pan2.ar(hat, pan1, hatLevel) * 0.1 ;
//mixer = Mix.new([bassOut,snareOut,hatOut]) * 0.7;
Out.ar(0, Limiter.ar(bassOut* EnvGen.kr(Env.perc(0.002,1, 1, -2), 1, doneAction:2); ,1,0.01));
Out.ar(2, Limiter.ar(snareOut* EnvGen.kr(Env.perc(0.002,0.1, 1, -2), 1, doneAction:0); ,1,0.01));
Out.ar(4, Limiter.ar(hatOut* EnvGen.kr(Env.perc(0.002,1, 1, -2), 1, doneAction:0); ,1,0.01));
}).store;
p.clock.tempo = 1.2;
Pdef(\x,Pbind( \instrument, \drums, \dur, 0.2,
\bassLevel, Pseq ([a] ,inf), \freq1,Pseq([50,20,10,5000,20,100,500,20,10,10,20,30,40],inf),\frq1,Pseq([40,150,180,200,40,150,180,200],inf),\frq0,Pseq([80,80,80,80,60,80,80,60,80,80,80,80],inf),
\snareLevel, Pseq ([b],inf),\hatLevel, Pseq ([c],inf)));
)




(
	
	Pdef(\plop, Pbind(
		\instrument, \default,
		\note, Pwhite(0,40,inf),
		\octave, 3,
		\dur, Pwhite(0.001,0.5,inf),
		\amp, 0.1
	)).play;
)

(
Ndef(\bla, {
	var sig;
	sig = In.ar(0,2);
	sig = CombL.ar(sig, 0.5,0.05,1);
	sig = (sig*45).tanh;
	sig = sig / 12;

}).play
)


CuspN


{ CuspN.ar(MouseX.kr(20, SampleRate.ir), 1.0, 1.99) * 0.3 ! 2 }.play(s);

{ SinOsc.ar(CuspN.ar(40, MouseX.kr(0.9,1.1,1), MouseY.kr(1.8,2,1))*800+900)*0.4 ! 2 }.play(s);



SynthDef(${1:plop}, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var sig;
	sig = SinOsc.ar(freq);
	sig = sig * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	sig = Pan2.ar(sig, pan, amp);
	Out.ar(out, sig);
}).add;
