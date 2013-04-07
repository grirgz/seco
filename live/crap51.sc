(
{
        var mod = SinOsc.ar([0.1,0.15,0.2]).range(0,1);
		var sig = LFPulse.ar([1.01,2.01,4.01]).range(1,10);
		var sig2 = LFNoise1.ar(0.08).range(0.99,1.01);
        var saw = LFSaw.ar([10,12,15]*sig*sig2);
        var out = (saw - ((saw>0)*mod)+(mod*0.5));
        Splay.ar(out)*0.1;
}.play;

{
        var mod = SinOsc.ar([0.1,0.15,0.2]).range(0,1);
		var sig = LFPulse.ar(1).range(1,10);
		var sig2 = LFTri.ar(0.1,[0,1,2,3]).range(1,10*sig);
		var sig3 = LFNoise1.ar(0.05).range(0.99,1.01);
        var saw = SinOsc.ar(LFSaw.ar([10,12,15]*sig2*sig3).range(200,300));
        var out = (saw - ((saw>0)*mod)+(mod*0.5));
        Splay.ar(out)*0.1;
}.play
)



(
{
	var base_freq = LFNoise2.kr(1!2).exprange(35,60);
	Pan2.ar(
		SinOsc.ar( 
			EnvGen.ar(
				Env(
					[ 
						base_freq,
						LFNoise2.kr(1/2!2).exprange(400,6500),
						LFNoise2.kr(1/2!2).exprange(35,125),
						base_freq
					],
					[
						LFNoise2.kr(1/2!2).exprange(0.001,0.008),
						LFNoise2.kr(1/2!2).exprange(0.008,0.13),
						LFNoise2.kr(1/2!2).exprange(0.01,0.5)
					], 
					[
						LFNoise2.kr(1/4!2).exprange(1/5,5), 
						LFNoise2.kr(1/4!2).range(-19,-6), 
						LFNoise2.kr(1/4!2).range(-5,5)
					]
				).circle
			), 0,
			LFNoise2.kr(LFNoise2.kr(1/20!2).exprange(1/9,9)).range(0.05,1)
			//EaseInBounce.ar(SinOsc.ar(LFNoise2.kr(1/10!2).exprange(1/5,5)).range(0,1))
		),
		LFTri.ar(
			LFNoise2.kr(1/20).exprange(1/10,10), [0,2], 
			SinOsc.kr(LFNoise2.kr(1/10!2).exprange(1/5,5)).range(0.3,1)
		)
	).mean;
}.play
)




(
var creature, creatures = 100;

s.waitForBoot { 

	SynthDef(\creature,{| out = 16, freq = 0.1, phaseMul = 50, pan = 0.0, amp = 0.2 |
		var sig = 0;

		3.do {
			sig = SinOsc.ar( 
				freq,
				phaseMul * sig
			);
		};

		sig = HPF.ar( sig, 80 );

		DetectSilence.ar( sig, 0.0001, 0.08, 2);

		Out.ar( out,  Pan2.ar( LeakDC.ar( sig ), pan, amp ) )
	}).add;

	SynthDef(\reverb,{| inbus = 16, out = 0, mix = 0.5, room = 0.5, damp = 0.5, amp = 1.0, dur = 2 |
		var sig;

		sig = In.ar( inbus, 2 ) * XLine.kr( 0.000001, 1, dur );

		sig = FreeVerb2.ar(
			sig[ 0 ],
			sig[ 1 ],
			mix,
			room,
			damp,
			amp
		);

		Out.ar( out, sig )
	}).add;

	SynthDef(\noise,{| out = 0, amp = 0.1 |
		var sig;

		sig = PinkNoise.ar( amp ! 2 );

		Out.ar( out, sig )
	}).add;

	creature =	Plazy {
		var number, freq, phase, dur, amp;

		if ( 0.68.coin ){
			number = 1.0.rand;
			phase = number.linlin( 0, 1, 69, 193 );
			freq = number.linlin( 0, 1, 0.37, 0.047 ).rrand( number.linlin( 0, 1, 0.6, 0.086 ) );
			freq = Pseq( [ freq ],  20.rrand( 50 )  );
			dur =  [ 
				Pseq(
					[
						0.15.rrand( 0.2 ), 
						Pwhite( 1.0, 4.0, 1 )
					], 
					inf 
				),
				Pgauss( 3.rrand( 4.0 ), 1.0, 1 )
			].wchoose( [ 0.05, 0.95 ] );
			amp = [ 
				rrand( -35.0, -28.0 ), 
				rrand( -23.0, -21.0 ), 
				rrand( -15.0, -10.0 ) 
			].wchoose( [ 0.94, 0.0525, 0.0075 ] ).dbamp;

			Pbind(
				\instrument, \creature,
				\phaseMul, phase,
				\freq, freq,
				\amp, amp,
				\pan, 1.0.rand2,
				\dur, dur,
				\out, 16
			)
		}{
			(
				\type: \rest,
				\dur: rrand( 5.0, 10.0 )
			)
		}
	};

	s.sync;

	Synth.tail( 1, \reverb, [
		\room, 0.43, \mix, 0.21, \damp, 0.9, \amp, 1.5 
	] );
	Synth.head( 1, \noise, [ 
		\amp, creatures.linlin( 1, 100, 0.004, 0.013 ), 
		\out, 16 
	] );
	Ppar( Pn( creature, inf ) ! creatures ).play;

}
)




(
{
	var mod = SinOsc.ar([0.1,0.15,0.2]).range(0,1);
	var saw = LFSaw.ar([10,12,15]);
	var out = (saw - ((saw>0)*mod)+(mod*0.5));
	Splay.ar(out)*0.4;
}.play;

{
	var mod = SinOsc.ar([0.1,0.15,0.2]).range(0,1);
	var saw = SinOsc.ar(LFSaw.ar([10,12,15]).range(200,300));
	var out = (saw - ((saw>0)*mod)+(mod*0.5));
	Splay.ar(out)*0.4;
}.play
)


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

// Example Usage
//~echo = Synth(\dubecho, [\length, TempoClock.default.tempo*(3/8), \fb, 0.7, \sep, 0.0012], addAction: \addToTail);
//~echo.free;
//~echo.set(\gate, 0);


SynthDef(\guitar2, { arg freq, amp, pan = 0;
var out, pluck, period, string;
freq = freq * [0.99,1,2,0.98];
pluck = PinkNoise.ar(Decay.kr(Line.kr(1, 0, 0.05), 0.05));
period = freq.reciprocal;
string = CombL.ar(pluck, period, period, 4);
out = LeakDC.ar(LPF.ar(string, 12000));
out = Splay.ar(out, XLine.ar(0.1,1,0.3));
out = out * XLine.ar(1,1/1000,4, doneAction:2);
Out.ar(0, Pan2.ar(out, pan, amp) * 0.4);
} ).send(s);

SynthDef(\guitar, { arg freq, amp, pan = 0;
var out, pluck, period, string;
freq = freq * [0.99,1,2,0.98];
out = SinOsc.ar(freq);
out = Splay.ar(out, XLine.ar(0.1,1,0.3));
out = out * XLine.ar(1,1/1000,2, doneAction:2);
Out.ar(0, Pan2.ar(out, pan, amp) * 0.4);
} ).send(s);

SynthDef(\ch, { | decay = 3, amp = 1, freq = 440 |
f = WhiteNoise.ar;
f = LPF.ar(f, 12000);
//f = f+ DelayC.ar(f, 0.1,LFNoise2.ar(1/2).range(0.001,0.01));
f = HPF.ar(f, 4000, 0.05);
f = f * EnvGen.kr(Env.perc(0.01,decay*0.8), doneAction:2);
Out.ar(0, 15 * f.dup * amp);
}).store;

SynthDef(\kick, { | decay = 0.03, amp = 1, freq = 40 |
var f = SinOsc.ar(freq*XLine.ar(1,1/4,0.1));
f = f * EnvGen.kr(Env.perc(0.0001,decay), doneAction:2);
Out.ar(0,f.dup * amp * 1);
}).store;

TempoClock.default.tempo = 90/60

Ppar([
	Pbind(
		\instrument, \guitar2,
		\strum, 2/Pstep([32,16,3,12],2,inf),
		\degree, Pseq([
				[0,2,4,6],
				[-1,2,4,6],
				[0,2,4,6],
				[0,2,4,6]-2,		
			],inf),
		\octave, 5,
		\dur, 1,
		\decay, Pseq([1],inf),
	),
			Ppar([
		Pbind(\instrument, \kick, \dur, 1/4, \decay, Pstep([2,0.3],1/4,inf) * 0.7, \octave, 4, \amp, Pstep([1,0,0,1, 0,0,1,0, 0,0.2,1,0, 0,0,0,0],1/4,inf)),
		Pbind(\instrument, \ch,
			\decay, 0.3,
		\dur, 1/Pstep([8,4,4,4,1,1,6,1],1,inf),
		\decay, Pn(Pseg([0.3,0.1],1)) / 2,
		\amp, Pstep([0,0,0,0, 1,0,0,0, 0,0,0,0, 1,0,0,0],1/4,inf) + (0.1*Pn(Pseg([0,0.4],1)))
	)
		])
]).play;

// Add Bjorn's Patented Dub Echo:
~echo = Synth(\dubecho, [\length, TempoClock.default.beatDur*(3/4), \fb, 0.7, \sep, 0.0012], addAction: \addToTail);
~echo.free






// intervals within spectrum on ~b

~b = 50;
(

x = Pbind(
        \freq, Ptuple([Prand((1..10), inf), Prand((1..5), inf)]) * Pfunc { ~b },
        \dur, 0.25,
        \amp, 0.05,
        \legato, 5
).play;
)

// modulations

~b = 85
~b = 105
~b = 125
~b = 70
~b = 95



(
SynthDef(\hh, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, release=0.2, num=1, chokebus, lpfreq=7500, hpfreq=150;
	var ou;
	var choke = In.kr(chokebus,1);
	ou = WhiteNoise.ar(1);
	ou = LPF.ar(ou, 7500);
	ou = HPF.ar(ou, 150);
	ou = ou * EnvGen.ar(Env.perc(0.001,release),gate,doneAction:2);
	ou = ou * EnvGen.ar(Env.cutoff(0.01), choke != num, doneAction:2);
	Out.kr(chokebus, Trig.kr(num,0.001));
	ou = Pan2.ar(ou, pan, amp);
	//ReplaceOut.ar(out, ou);
	Out.ar(out, ou);
}).add;
)


~chokebus = Bus.control(s, 1)
~chokebus.set(0)

TempoClock.default.tempo = 1.5
(
Pdef(\hh, Pbind(
	\instrument, \hh,
	\degree, Pseq([0],inf),
	\dur, 2,
	\addAction, \addToTail,
	\release, 1.7,
	\amp, 0.1
)).play(quant:2);
);
Pdef(\hh).stop

(
Pdef(\hh2, Pbind(
	\instrument, \hh,
	\degree, Pseq([0],inf),
	\dur, 2.0,
	\addAction, \addToTail,
	\lpfreq, 15000,
	\hpfreq, 1500,
	\release, 0.1,
	\amp, 0.1
)).play(quant:[2,0.5]);
);

Pdef(\hh2).stop
