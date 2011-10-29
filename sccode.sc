s.boot

(
Ndef(\a, { |freq=2000, rate=1, width=0.6|
	LFPulse.kr(rate, 0, 4/6) *
	LFPulse.kr(rate * 6, 0, width) *
	Pulse.ar(freq, 0.3, 0.1)
}).play
)



2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43


        

        

            

(
SynthDef(\dollar, { |out, freq = 440, amp = 0.1, pan = 0, sustain = 1.0|
	var env = EnvGen.kr(Env([amp, amp, 0], [sustain, 0.01]), doneAction: 2);
	var decay = freq ** -0.5 * 100;
	var string = CombL.ar(Decay2.ar(Impulse.ar(0), 0.0001, 0.001), 1/freq, 1/freq, decay);
	var damped = string; //LPF.ar(string, Line.kr(11000, 3000, decay));
	OffsetOut.ar(out, Pan2.ar(damped * env, pan));
}).add;
);

(
SynthDef(\euro, { |out, freq = 440, amp = 0.1, pan = 0, sustain = 1.0|
	var env = EnvGen.kr(Env.perc(0.001, sustain), doneAction: 2);
	var decay = freq ** -0.5 * 100;
	var thing;
	freq = freq * (1..7).nthPrime;
	thing = SinOsc.ar(freq , 0, amp * AmpComp.kr(freq)).mean;
	OffsetOut.ar(out, Pan2.ar(thing * env, pan));
}).add;
);


(
Pdef(\stock, { |note = 0, sustain = 1|
	Pbind(
		\instrument, [\dollar, \euro].choose,
		\note, note.value + Pseq((12..0).curdle(0.1).scramble.flat * 3), 
		\dur, sustain.value / 12
	)
})
);

(
Pdef(\ett, 
	Pbind(
		\type, \phrase,
		\instrument, \stock,
		\dur, 4,
		\legato, Prand((1..5), inf),
		\note, Prand((0..12), inf) + Prand([[0, 2], [0, 6], [0, 7], 0, 0], inf)
	)
).play
)



(
play{
    p=LFPulse;
    n=LFNoise1;
    Limiter.ar(
        GVerb.ar(
            LPF.ar(LeakDC.ar(
                mean({|n|(p.ar(n*1e2+50*p.kr(4-n/[4,7,5]).sum+3e2)*p.kr(n+1*2,0,0.8).lag(0.1))}!3)
            ),4000) * 0.4, 200, 9, 0.6, 0.5, 40, 1, 0.15, 0.25
        ).madd(0.8) + GVerb.ar(
            {|i|BPF.ar(PinkNoise.ar(Decay.ar(Impulse.ar(2,0.95+n.kr(1,0.08)),rrand(0.2,0.4),2)).tanh,600*i+1200,0.4)}.dup(6).sum*1.2,
            90, 3, 0.7, 0.5, 30, 1, 0.2, 0.3
        ) + Splay.ar(
            {|i|Ringz.ar(LPF.ar(Impulse.ar(6,n.kr(1,0.08)),4000),6*i+40+n.kr(30,2),0.5,n.kr(3).range(4,8)).tanh}.dup(3),0.5
        ).madd(0.3)
    )
}
)

play{p=LFPulse;cos(p.ar(p.kr(2)+p.kr(3)*[50,51]).lag(2e-3)+mean({|n|(p.ar(n*1e2+50*p.kr(4-n/[2,7,4]).sum+3e2)*p.kr(n+1*2,0,0.8))}!3))}

n=LFNoise1;Ndef(\x,{a=SinOsc.ar(65,Ndef(\x).ar*n.ar(0.1,3),n.ar(3,6)).tanh;9.do{a=AllpassL.ar(a,0.3,{0.2.rand+0.1}!2,5)};a.tanh}).play


(
Ndef(\a, {
	var src, loc, delay=0.01;
	src = Decay2.ar(Impulse.ar(1/8), LFNoise2.kr(4).range(0.001, 0.0001),
	LFNoise2.kr(4).range(0.005, 0.05))!2
	* Saw.ar(LFNoise2.kr(4).exprange(40, 4000));
	loc = BPF.ar(LocalIn.ar(2) + [src, 0], LFNoise0.kr(1/4).range(100, 1200).lag, 3.2);
	loc = GVerb.ar(loc, 100, 4, mul:0.02, add:loc);
	loc = Compander.ar(loc, loc, 0.9, 1, 0.001);
	loc = (loc * 0.501).tanh;
	loc = Compander.ar(loc, loc, 0.1, 1, 0.1);	
	loc = DelayC.ar(loc, delay * 2, delay * 1);
	LocalOut.ar(loc.reverse * 0.49);
	Out.ar(0, loc * 0.9)
}).play;

Ndef(\b, {
	var src, loc, del;
	del = 1/100;
	src = Decay2.ar(TDuty.ar(1/4,0,Dseq([1, 0.5, 0.3, 0.1],inf)), 
	LFNoise2.kr(4).range(0.001, 0.0001), LFNoise2.kr(4).range(0.005, 0.05))!2
	* Saw.ar(LFNoise2.kr(200).exprange(40, 14000));
	loc = BPF.ar(LocalIn.ar(2) + [src, 0], LFNoise2.kr(0.1).range(40, 400), 3.2);
	loc = GVerb.ar(loc, 100, 4, mul:0.02, add:loc);
	loc = Compander.ar(loc, loc, 0.9, 1, 0.001);
	loc = (loc * 0.51).tanh;
	loc = Compander.ar(loc, loc, 0.1, 1, 0.1);	
	loc = DelayC.ar(loc, del * 2, del * 1);
	LocalOut.ar(loc.reverse * 0.45);
	Out.ar(0, Compander.ar(loc,loc,0.1, 1, 0.05) * 1.2)
}).play;

Ndef(\c, {
	var src, loc, del=1/4;
	src = Decay2.ar(Impulse.ar(1), 0.0001, 0.05) * SinOsc.ar(60);
	loc = src + LocalIn.ar(2);
	loc = BPF.ar(loc, 800, 3.2).softclip;
	loc = DelayN.ar(loc, del, [0, del]);
	LocalOut.ar(loc.reverse * 0.99);
	Out.ar(0, loc * 2)
}).play;
)


(
SynthDef(\dubecho,{|length = 1, fb = 0.8, sep = 0.012|
var input = In.ar(0, 2);
var output = input + Fb({

arg feedback; // this will contain the delayed output from the Fb unit

var left,right;
var magic = LeakDC.ar(feedback*fb + input);
magic = HPF.ar(magic, 400); // filter's on the feedback path
magic = LPF.ar(magic, 5000);
magic = magic.tanh; // and some more non-linearity in the form of distortion
#left, right = magic; // let's have named variables for the left and right channels
magic = [DelayC.ar(left, 1, LFNoise2.ar(12).range(0,sep)), DelayC.ar(right, 1, LFNoise2.ar(12).range(sep,0))]; // In addition to the main delay handled by the feedback quark, this adds separately modulated delays to the left and right channels, which with a small "sep" value creates a bit of spatialization

},length);
ReplaceOut.ar(0, output);
}).store;
)

// Example Usage
~echo = Synth(\dubecho, [\length, TempoClock.default.tempo*(3/8), \fb, 0.7, \sep, 0.0012], addAction: \addToTail);
~echo.free;
~echo.set(\gate, 0);



(
SynthDef(\pluck, {|freq = 440, dur = 1|
var klang,note;
dur = dur * 2;
note = freq.cpsmidi;
note = note + (LFNoise2.ar(20).range(-1,1) * (1/8));
klang = SinOsc.ar((note * [1,1.002]).midicps, phase: VarSaw.ar(note.midicps, width: Line.ar(1,0.2,dur))) * 0.3;
klang = klang * XLine.ar(1,1/10000,dur,doneAction:2);
Out.ar(0, klang);
}).store;
)

(
Pbind(
\instrument, \pluck,
\degree, Pn(Pseries(0, Pwrand([Pwhite(-3,3,inf).round(1),1],[32,1],inf), 4),inf)-1 + [0,2,-1,-14],
\dur, Pseq([4, [5,2,1]].convertRhythm / 2, inf),
\strum, Pwhite(0,1/8,inf),
\octave, [5,6]
).play;
)


(
play {
    Limiter.ar(
        tanh(
            3 * GVerb.ar(
                HPF.ar(
                    PinkNoise.ar(0.08+LFNoise1.kr(0.3,0.02))+LPF.ar(Dust2.ar(LFNoise1.kr(0.2).range(40,50)),7000),
                    400
                ),
                250,100,0.25,drylevel:0.3
            ) * Line.kr(0,1,10)
        ) + (
            GVerb.ar(
                LPF.ar(
                    10 * HPF.ar(PinkNoise.ar(LFNoise1.kr(3).clip(0,1)*LFNoise1.kr(2).clip(0,1) ** 1.8), 20)
                    ,LFNoise1.kr(1).exprange(100,2500)
                ).tanh,
               270,30,0.7,drylevel:0.5
            ) * Line.kr(0,0.7,30)
        )
    )
};
)


(
SynthDef("additivePad", 
    { arg numOfCycles = 100;
       var lfo;
		var lfo2;
		var lfo3;
       lfo = SinOsc.kr([Rand(0.5,1000),Rand(0.5,1000)], 1.5pi, 0.5, 0.5);
		lfo3 = SinOsc.kr([Rand(0.1,0.5),Rand(0.1,0.5)], 1.5pi, 0.5, 0.5);
		lfo2 = SinOsc.kr([(Rand(0.5,1000) * lfo) * lfo3, (Rand(0.5,1000) * (1 - lfo)) * (1 - lfo3)], 1.5pi, 0.5, 0.5);
        Out.ar(
            0, 
            Mix.arFill(
                numOfCycles,
                { 
                    SinOsc.ar(
                        Rand(40,1000),
                        0, 
                        (numOfCycles.reciprocal * 0.75) * lfo2)
                }
            ) * EnvGen.kr(Env.perc(Rand(0.1,30), Rand(0.1,30)), doneAction: 2)
        )
    }
).send(s); 
)

(
r = Routine({
	200.do({
		x = Synth.new("additivePad");   
		10.wait;
	});
});
)

r.play;



TempoClock.default.tempo = 156/60;
TempoClock.default.tempo =1

(
t = Bus.audio(Server.default, 2);

SynthDef(\kick1507, { | dur = 0.15, amp = 1, truncate_freq = 300 |
	var sig = HPF.ar(
		LPF.ar(
			SinOsc.ar( EnvGen.ar( Env.perc( 0, dur, curve: -6), levelScale: 1300) ),
			truncate_freq
		),
		15
	);
	var env = EnvGen.ar( Env.perc( 0.001, dur, curve: 5), doneAction: 2, levelScale: amp );
	Out.ar(0, sig * env ! 2);
}).add;

SynthDef(\bass1507, { | dur = 0.15, gate = 1, freq = 50 |
	var sig = Blip.ar( freq, 5, 0.9 );
	var env = EnvGen.ar( Env.sine( dur ), gate, doneAction: 2 );
	Out.ar(0, sig * env ! 2);
}).add;

SynthDef(\bass1607, { | dur = 0.15, gate = 1, freq = 50, amp = 1, index = 10 |
	var sig = PMOsc.ar( freq, freq + 5, index );
	var env = EnvGen.ar( Env.triangle( dur ), gate, amp, doneAction: 2 );
	Out.ar(0, sig * env ! 2);
}).add;

x = {
	var sig = Limiter.ar(
		In.ar(t, 2);
	);
	Out.ar(0, sig); 
}.play;
)


(
Pbind(*[
	instrument: \kick1507,
	delta: 1,
	dur: TempoClock.default.tempo.reciprocal / 4,
	amp: Pseq( [
		Pseq([1], 16),
		Pseq([0.6, 1], 8),  
		Pseq([1], 16),
		Pseq([0.7, 0.7, 0.85, 1], 8)
	], inf)
]).play(quant:[1,0,0]);
)

(
Pbind(*[
	instrument: \bass1507,
	delta: 0.25,
	dur: TempoClock.default.tempo.reciprocal / 4,
    freq: Pseq([\rest, Pseq([(33,34.5..45)], 3)], inf),
]).play(quant:[1,0,0]);
)

(
Pbind(*[
	instrument: \bass1607,
	delta: 0.25,
	dur: TempoClock.default.tempo.reciprocal / 4,
    freq: Pseq([\rest, Pseq([(50,51..60)], 3)], inf),
]).play(quant:[1,0,0]);
)

// kick from above?
(
Pbind(*[
	instrument: \kick1507,
	delta: Pseq(Array.geom(540, 1, 1.015).reciprocal.reverse),
	truncate_freq: Pseq(Array.series(540, 300, 2).reverse),
	dur: Pkey(\delta)/4,
	amp: 1
]).play;
)



(
SynthDef(\brumm, { |dur = 1, pan = 2, freq = 40, index = 20|
	var sig = Pan2.ar(
		Blip.ar(freq, index),
		pan
	) * EnvGen.ar(Env.perc(0.01, dur), doneAction: 2);
	Out.ar(0, sig);
}).add;
)

(
Pbind(*[
	dur: Pseq([1.2, 0.2, 0.5, Pseries(0.1, -0.001, 100)], inf),
	pan: Pxrand(#[-0.5, 0.5, 0.1, -0.1, 0.8, -1], inf),
	freq: 40,
	instrument: \brumm
]).play();
)

(
Pbind(*[
	dur: Pseq([1.2, 0.2, 0.5, Pseries(0.1, -0.001, 100)], inf),
	pan: Pxrand(#[-0.5, 0.5, 0.1, -0.1, 0.8, -1], inf),
	freq: Pseq([Pseries(140, 0.5, 200)], inf),
	instrument: \brumm
]).play;
)

(
Pbind(*[
	dur: Pseq(Signal.chebyFill(1024, Array.rand(10, 0.0, 10.0)).abs + 0.05, inf),
	pan: Pxrand(#[-0.5, 0.5, 0.1, -0.1, 0.8, -1], inf),
	freq: Pseq(Signal.chebyFill(1024, Array.rand(10, 0.0, 10.0)).abs * 18000 + 120, inf),
	instrument: \brumm
]).play;
)


(
Pbind(*[
	dur: Pseq([1.2, 0.2, 0.5, Pseries(0.1, -0.002, 50)], inf),
	pan: Pxrand(#[-0.5, 0.5, 0.1, -0.1, 0.8, -1, 1, -0.8], inf),
	freq: Pseq([Pseries(740, 0.5, 200), Pxrand(Array.series(740, 0.5, 200), 50)], inf),
	instrument: \brumm
]).play;
)












~amen = Buffer.read(s, "sounds/amen-break.wav");

(
SynthDef(\loop, {| out = 0, amp=0.1, bufnum = 0, gate = 1, pos = 0, speed = 1, freq = 0, endfreq = 0.001, sustain=0.5, wobble = 3, boost = 1|

	var player,env;
	freq = XLine.ar(freq,endfreq,sustain/4);
	freq = freq.cpsmidi + (LFNoise2.ar(3).range(-1,1) * (1/12));
	freq = freq.midicps;
	env = Env.adsr(0.0001,0.01, 1, sustain/8, 1);
	amp = EnvGen.kr(env, gate, doneAction:2) * amp;
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, Impulse.ar(freq), startPos: (pos*BufFrames.kr(bufnum)) + Rand(0,20), doneAction:2, loop: 1) * boost;
	player = RLPF.ar(player, SinOsc.ar(wobble/sustain).range(20000,80), XLine.ar(0.2,0.9,sustain)) * boost;
	Out.ar(out, player);

}).store;

SynthDef(\compressor, {
	var in,compressed;
	in = In.ar(0,2);
	compressed = Compander.ar(in, in, 0.1, 1, 1/8, 0.002, 0.01);
	ReplaceOut.ar(0, compressed * 4);
}).store;
)

TempoClock.default.tempo = 16/~amen.duration;

~rhythm = Pseq([8,[2,[1,[1,1,[1,1!3],[1,1!4]]],[1,1!4]]].convertRhythm,1); // Evaluate one of these before playing the Pbind
~rhythm = Pseq([8,[[2,[2,1,[1,1!12]]],[1,[1,1,[1,1!3],[1,1!4]]],[1,1!4]]].convertRhythm,1);
~rhythm = Pseq([8,1!16].convertRhythm,1);

(
Pbind(
\instrument, \loop,
\bufnum, a.bufnum,
\out, 0,
\freq, Pstep([43.midicps,0,0,0],2,inf),
\endfreq, Pstep([40.midicps/2,0,0,0],2,inf),
\wobble, Pstep([Prand([1/2,2,1,1/3]),0,0,0],2,inf),
\pos, Pstep(Array.series(8, 0, 1),1,inf)/8,
\legato, 1,
\dur, Pn(Plazy({~rhythm}))
).play;
)

(
Pbind(
\instrument, \loop,
\bufnum, a.bufnum,
\out, 0,
\freq, Pstep([43.midicps,0,0,0],2,inf),
\endfreq, Pstep([40.midicps/2,0,0,0],2,inf),
\wobble, Pstep([Prand([1/2,2,1,1/3]),0,0,0],2,inf),
\pos, Pstep(Array.series(8, 0, 1),1,inf)/8,
\legato, 1,
\dur, Pn(Plazy({~rhythm}))
).play;
)


(
( 'instrument': \loop, 'speed': 1, 'dur': 0.25, \gate:1,
  'sustain': 0.5, 'segdur': 0.25, 'wobble': 3, 'boost': 1, 'pos': 0,
  'out': 0, 'legato': 0.5, 'stretchdur': 1, 'freq': 0, 'bufnum': 1,
  'endfreq': 0.0010000000474975 ).play
)

~comp = Synth(\compressor, addAction: \addToTail);

~comp.free;
