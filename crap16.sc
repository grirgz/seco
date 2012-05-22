play{a=Mix(Array.fill(75,{|i|SinOsc.ar(rrand(1,50)*i+10,0,LFNoise1.kr([1.8,2.3]))}))*0.02;CombL.ar(a,15,SinOsc.ar([0.1,0.11],0,0.5,0.6),10)}
o = Server.default.options;
o.memSize = 8192 * 2
o.memSize
s.quit
s.boot



(
w = Window("screensaver", Window.screenBounds).front;
w.view.keyDownAction = { w.close };
u = UserView(w, w.view.bounds);
u.background = Color.black;
u.animate = true;
u.clearOnRefresh = false;
u.drawFunc = { |u|
	var width = u.bounds.width;
	var height = u.bounds.height;
	var size = 2;
	Pen.fillColor = Color.black.alpha_(sin(u.frame).abs/100);
	Pen.fillRect(u.bounds);
	Pen.strokeColor = Color(u.frame/1e3%1,sin(u.frame/100).abs,cos(u.frame/666).abs,0.1);
	Pen.rotate(u.frame/(360/4), width/2, height/2);
	Pen.scale(size, size);
	Pen.use{
		Pen.moveTo(Point(0,0));
		100.do{ |i| Pen.lineTo(Point(u.frame * cos(i) % 400, u.frame / sin(i) % 400)) };
		Pen.stroke;
	};
};

)
GUI.qt



o = Server.local.options;
{CombC.ar(Mix(SinOsc.ar((1..20)*6.12))* SinOsc.ar([SinOsc.ar(15.4,0,20),SinOsc.ar(1.9,0,37)])* SinOsc.ar([500,400]),1,0.01,10)*0.01}.play



{CombC.ar(Mix(SinOsc.ar((1..20)*145.12))* SinOsc.ar([SinOsc.ar(0.14,0,40),SinOsc.ar(0.19,0,37)])* SinOsc.ar([0.023,0.012]),1,0.1,10)*0.09}.play



{(CombC.ar([Mix(SinOsc.ar((40..50)*7.23,(1..10)/10)),Mix(SinOsc.ar((40..50)*6.41,(1..10)/10))],10,SinOsc.ar(0.0001,0,10),2)*0.02)}.play

s.quit
s.boot


(
////////////////////////////////////////////////////////////////
// EPIC SAX GUY SynthDefs
// http://www.youtube.com/watch?v=KHy7DGLTt8g
// Not yet there... but hearable

// sounds more like a trumpet/horn after the failure and cheesyness of the stk sax
SynthDef(\sax, { |out, freq=440, amp=0.1, gate=1|
	var num = 16;
	var harms = Array.series(num, 1, 1) * Array.exprand(num, 0.995, 1.001);
	var snd = SinOsc.ar(freq * SinOsc.kr(Rand(2.0,5.0),0,Rand(0.001, 0.01),1) * harms, mul:Array.geom(num, 1, 0.63));
	snd = Splay.ar(snd);
	snd = BBandPass.ar(snd, freq * XLine.kr(0.1,4,0.01), 2);
	snd = snd * amp * EnvGen.ar(Env.adsr(0.001, 0.2, 0.7, 0.2), gate, doneAction:2);
	Out.ar(out, snd!2);
}).add;

// should be more like a gated synth, but this one gives the rhythmic element
// remember to pass the bps from the language tempo!
SynthDef(\lead, { |out, freq=440, amp=0.1, gate=1, bps=2|
    var snd;
    var seq = Demand.kr(Impulse.kr(bps*4), 0, Dseq(freq*[1,3,2], inf)).lag(0.01);
    snd = LFSaw.ar(freq*{rrand(0.995, 1.005)}!4);
    snd = Splay.ar(snd);
    snd = MoogFF.ar(snd, seq, 0.5);
    snd = snd * EnvGen.ar(Env.asr(0.01,1,0.01), gate, doneAction:2);
    OffsetOut.ar(out, snd * amp);
}).add;

// yep, an organ with a sub bass tone :D
SynthDef(\organ, { |out, freq=440, amp=0.1, gate=1|
    var snd;
    snd = Splay.ar(SinOsc.ar(freq*Array.geom(4,1,2), mul:1/4));
    snd = snd + SinOsc.ar(freq/2, mul:0.4)!2;
    snd = snd * EnvGen.ar(Env.asr(0.001,1,0.01), gate, doneAction:2);
    OffsetOut.ar(out, snd * amp);
}).add;

// from the synth def pool
SynthDef(\kick, { |out=0, amp=0.1, pan=0|
	var env0, env1, env1m, son;
	
	env0 =  EnvGen.ar(Env.new([0.5, 1, 0.5, 0], [0.005, 0.06, 0.26], [-4, -2, -4]), doneAction:2);
	env1 = EnvGen.ar(Env.new([110, 59, 29], [0.005, 0.29], [-4, -5]));
	env1m = env1.midicps;
	
	son = LFPulse.ar(env1m, 0, 0.5, 1, -0.5);
	son = son + WhiteNoise.ar(1);
	son = LPF.ar(son, env1m*1.5, env0);
	son = son + SinOsc.ar(env1m, 0.5, env0);
	
	son = son * 1.2;
	son = son.clip2(1);
	
	OffsetOut.ar(out, Pan2.ar(son * amp));
}).add;

// full of fail:

//SynthDef(\sax, { |out, freq=440, amp=0.1, gate=1|
//	var r_stiff = 67;
//	var r_ap = 63;
//	var noise = 10;
//	var pos = 20;
//	var vibf = 20;
//	var vibg = 1;
//	var press = 85;
//	var snd = StkSaxofony.ar(freq, r_stiff, r_ap, noise, pos, vibf, vibg, press, 1, amp);
//	snd = snd * EnvGen.ar(Env.adsr(0.001, 0.2, 0.7, 0.2), gate, doneAction:2);
//	Out.ar(out, snd!2);
//}).add;


)

////////////////////////////////////////////////////////////////
// EPIC SAX GUY TUNE
// http://www.youtube.com/watch?v=KHy7DGLTt8g
// ... still needs a nice gated pad

(
TempoClock.default.tempo = 2.1;

Pdef(\kick).quant = 4;
Pdef(\organ).quant = 4;
Pdef(\sax).quant = 4;
Pdef(\lead).quant = 4;

////////////////////////////////////////////////////////////////
Pdef(\kick, Pbind(\instrument, \kick, \dur, 1, \amp, 1)).play;

Pdef(\organ, Pbind(
	\instrument, \organ,
	\octave, [3,4],
	\root, 3,
	\scale, Scale.minor,
	\degree, Pstutter(3, Pseq([0,-2,2,4], inf)),
	\amp, 0.3,
	\dur, Pseq([1.5,1.5,1], inf)
)).play;

Pdef(\lead, Pbind(
	\instrument, \lead,
	\octave, [5,6],
	\root, 3,
	\scale, Scale.minor,
	\degree, Pseq([0,2,0,4], inf),
	\amp, 0.2,
	\bps, TempoClock.default.tempo,
	\dur, 4
)).play;

// needs more articulation...
Pdef(\sax, Pbind(
	\instrument, \sax,
	\root, 3,
	\scale, Scale.minor,
	\octave, 5,
	\legato, 0.75,
	\amp, Pwhite(0.9,1.0, inf),
	\degree, Pseq([Pseq([Pn(4,4),3,4],2), Pseq([4,6,4,3,2,0,0,1,2,0])], inf),
	\dur, Pseq([Pseq([2,1/2,Pn(1/4,3),3/4],2), Pseq([1.5,1,1,1,1,Pn(0.5,5)])], inf)
)).play;
)




(
fork{

	// notes sequence

	var seq = Pxrand([0,3,5,7,8],inf).asStream;

	loop{

		var dur = 8;

		var num = 8;

		var root = 36;

		var freq = (seq.next+root).midicps;

		var spread = rrand(0.4,0.8);

		var attack = rrand(0.05, 0.3);

		

		// play the cluster

		play{

			var harm = Array.geom(num, 1, 1.5);

			var harma = Array.geom(num, 0.5, 0.8);

			var detune = Array.fill(num, { LFNoise2.kr(1,0.01,1) });

			var source = PinkNoise.ar;

			var bandwidth = Rand(0.001,0.01);

			var generator = [

				SinOsc.ar(freq*harm*detune, mul:harma*0.3).scramble,

				Resonz.ar(source, freq*harm*detune, bandwidth, mul:harma).scramble * 50

			].wchoose([0.2,0.8]);

			var snd = Splay.ar(generator,spread);

			snd * LFGauss.ar(dur, attack, loop:0, doneAction:2);

		};

		dur.wait;

	};

	

};
)
(



// global triple super gverb

{

	var in = In.ar(0,2);

	in = (in*0.2) + GVerb.ar(in, 220, 12, mul:0.6);

	in = (in*0.2) + GVerb.ar(in, 220, 12, mul:0.6);

	in = (in*0.2) + GVerb.ar(in, 220, 12, mul:0.6);

	ReplaceOut.ar(0, Limiter.ar(LeakDC.ar(in)))

}.play(addAction:\addToTail)
)



(
Ndef(\plop, {
	arg freq = 300;
	var ou, sig1;
	ou = SinOsc.ar(2 * EnvGen.kr(Env([1,100,4],[0.1,4])));
	sig1 = ou;
	ou = SinOsc.ar(300 + (ou * XLine.kr(1,125,4)));
	ou = ou * SinOsc.ar(XLine.kr(1,45,4));
	ou = CombL.ar(ou, 4, 1, XLine.kr(4,0.1,4));
	ou = BPF.ar(ou, freq * Array.series(10,1,0.10), 0.1).sum;
	ou = BPF.ar(ou, freq * Array.series(10,1,0.10) - 50, 0.7).sum;
	ou = LPF.ar(ou, sig1.range(50, freq));
	ou = ou !2;

}).play
)
(
Ndef(\plop, {
	arg freq = 300;
	var ou, sig1, sig2;
	sig1 = SinOsc.ar(1.4).range(0.1,10);
	sig2 = SinOsc.ar(0.4);
	ou = LFSaw.ar(freq * ((Array.series(10, -0.5, 0.01) * SinOsc.kr(sig1,add:1.01)) + 1));
	//ou = ou / 4;
	//ou = BPF.ar(ou, freq * Array.series(3,1,0.10) - 50, 0.1).sum;
	ou = BPF.ar(ou, freq, 0.7).sum;
	//ou = Slew.ar(ou, 1000,100);
	//ou = LPF.ar(ou, sig2.range(100,freq));
	//ou = HPF.ar(ou, 400);
	ou = ou + DelayL.kr(ou, 0.11,0.01 * sig2.abs + 0.001);
	ou = ou.clip;
	//ou = ou * 4;
	ou = ou !2;

}).play
)


s.boot


{ SinOsc.ar }.play


(

Ndef(\ro_v19, {

	var freq = 40.rrand(90).debug('base freq');

	var seed = thisThread.randSeed = 1000000000.rand.debug('seed');

	var sig = Splay.ar({

		var i = 4.exprand(40).asInteger;

		Median.ar(

			3.exprand(18),

			EnvGen.ar(

				Env( // wave form

					[0] ++ Array.fill(i-1, { 

						[LFNoise2,LFNoise0,SinOsc,LFPulse,LFSaw].choose.kr(0.1.exprand(10), mul: 0.1.rrand(1))

					}) ++ [0],

					Array.rand(i-1, 0.1,1.0).normalizeSum,

					[[-5.0.rrand(5),\sin].choose] ++ Array.fill(i-2, {

						[\sin ! 1.rrand(6), -5.0.rrand(5) ! 1.rrand(4)].flat.choose

					}) ++ [[-5.0.rrand(5),\sin].choose]

				).circle,

				timeScale: Duty.kr( // freq

					Drand([4,8,16,24], inf), 0,

					Dwrand([1,2,4,8,16,32,64]*freq, ((14..7) ** 1.rrand(3)).normalizeSum, inf) // octave

					* 0.5.coin.if({ Duty.kr( // note

						[Dxrand, Drand].choose.new([0.125,0.25,0.5,1,2,4,6,8,12], inf), 0,

						Dshuf({ (0..11).choose.midiratio } ! 1.exprand(8), inf)

					) }, { (0..11).choose.midiratio })

				).reciprocal

			)

		) * EnvGen.kr( // rhythm

			Env.perc(

				0.01.exprand(0.4) * 0.25.coin.if({ Duty.kr( // atk

					Drand([0.5,1,2,4,8,12,16], inf), 0,

					Dshuf({ [1,2,3,4].choose } ! 1.exprand(8), inf)

				) }, { 1 }),

				0.1.exprand(4) * 0.75.coin.if({ Duty.kr( // release

					Drand([0.5,1,2,4,8,12,16], inf), 0,

					Dshuf({ [1,2,4,8,16].choose.reciprocal } ! 1.exprand(8), inf)

				) }, { 1 }),

				LFNoise2.kr(0.1.exprand(10)).range(0.05,1), // appearance

				LFNoise2.kr(0.1.exprand(10)).range(-4,4),

			).circle

		);

	} ! 3.rrand(9).debug('source'));

	BHiShelf.ar(

		GVerb.ar(

			Rotate2.ar(sig[0], sig[1], Duty.kr(Drand([2,4,8],inf), 0, Drand([-1,1,0],inf)) * LFSaw.kr(0.1.exprand(1))),

			10.rrand(150).debug('room')

		),

		LFNoise2.kr(0.1.exprand(0.5)).range(4000,6500),

		LFNoise2.kr(0.1.exprand(1)).range(1,3),

		LFNoise2.kr(0.1.exprand(1)).range(-24,-16)

	) * 3;

}).play

)

s.boot



//additive synthesis bell
//port of the pure-data example: D07.additive.pd
(
s.waitForBoot{
	SynthDef(\risset2, {|out= 0, pan= 0, freq= 400, amp= 0.1, dur= 2, t_trig= 1|
		var amps= #[1, 0.67, 1, 1.8, 2.67, 1.67, 1.46, 1.33, 1.33, 1, 1.33];
		var durs= #[1, 0.9, 0.65, 0.55, 0.325, 0.35, 0.25, 0.2, 0.15, 0.1, 0.075];
		var frqs= #[0.56, 0.56, 0.92, 0.92, 1.19, 1.7, 2, 2.74, 3, 3.76, 4.07];
		var dets= #[0, 1, 0, 1.7, 0, 0, 0, 0, 0, 0, 0];
		var src= Mix.fill(11, {|i|
			var env= EnvGen.ar(Env.perc(0.005, dur*durs[i], amps[i], -4.5), t_trig);
			SinOsc.ar(freq*frqs[i]+dets[i], 0, amp*env);
		});
		DetectSilence.ar(src, doneAction:2);
		Out.ar(out, Pan2.ar(src, pan));
	}).add;
};
)

a= Synth(\risset2, [\freq, 72.midicps, \dur, 4])
a.set(\t_trig, 1)
a.set(\freq, 100.midicps, \dur, 3, \t_trig, 1)
a.set(\freq, 60.midicps, \dur, 1, \t_trig, 1)
a.set(\freq, 90.midicps, \dur, 0.5, \t_trig, 1)
a.free

(
Routine({
	var a= Synth(\risset2);
	20.do{
		var dur= 0.2.exprand(3.0);
		var fre= 60.0.exprand(5000.0);
		("dur:"+dur+"fre:"+fre).postln;
		a.set(\t_trig, 1, \freq, fre, \dur, dur);
		dur.wait;
	};
	a.free;
	"done".postln;
}).play;
)


(
Mdef(\bla, Pbind(
	\instrument, \risset2,
	\freq, 60,
	\dur, 0.5,
))
)
