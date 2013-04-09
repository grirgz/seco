:a!
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
	Pbind(
		\instrument, \creature,
		\phaseMul, 51,
		\freq, 0.2,
		\amp, 0.1,
		\pan, 0.1,
		\dur, 1,
		\out, 0
	).play
)
