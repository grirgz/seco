/*

Kaprekar

Take any four-digit number except multiples of 1111. Sort the digits. Make numbers of the digits in ascending and in descending order. Repeat with the difference. After no more than 7 steps you will reach 6174, Kaprekar's Constant. 

If you use three-digit numbers, you will get 495 in no more than 6 steps.

*/

s.boot;



(

//Synthdefs: sinewaves for numbers, with pan

SynthDef( \numbers, {|freq=30, panpos|

		Out.ar(0,

			Pan2.ar(SinOsc.ar(freq, mul: 0.5*EnvGen.kr(Env.linen(0.01, 0.1, 0.01, 0.6), doneAction: 2)),panpos)

		)

}).add;

//sawtooth for digits, filter associated with number of repetitions

SynthDef( \digitsplayer, {|freq=30, cutoff|

		Out.ar(0,

			Pan2.ar(LPF.ar(Saw.ar(freq, mul: 0.5*EnvGen.kr(Env.perc(0.01,0.75), doneAction: 2)), cutoff),0)

		)

}).add;

)



(



var number, theNumber, greatest, smallest;

var digits=Array.newClear(4);





{

g=0; //greatest number of repetitions

for (1, 9998, { arg i;

	if ( i.mod(1111)!= 0, {

		number = i;

		number.postln;

		theNumber=number; //theNumber is used to be able to mess with the value while retaining the value of number

		if (theNumber < 150, {theNumber = theNumber + 150});

		o=Synth( \numbers, [freq: theNumber/5, panpos: 0]);

		0.1.wait;

		a=0; //number of repetitions

		while ( {number != 6174}, {

			digits.put(0, number.mod(10)); //isolate digits

			number=number.div(10);

			digits.put(1, number.mod(10));

			number=number.div(10);

			digits.put(2, number.mod(10));

			number=number.div(10);

			digits.put(3, number.mod(10));

			digits.sort; //sort digits

			//digits.postln;

			greatest=(1000*digits[3])+(100*digits[2])+(10 * digits[1]) + digits[0];

			greatest.post; " ".post;

			

			smallest=digits[3] + (10*digits[2])+(100*digits[1])+(1000*digits[0]);

			smallest.post; " ".post;

			//(digits.mirror2+60).postln;

			

			//play the digits

			Pbind(

			\instrument, \digitsplayer,

			\freq, Pseq(digits.mirror2+60).midicps,

			\cutoff, 500*(a+1),

			\dur, 0.1

			).play;

			0.8.wait;

		

			//play greatest and smallest

			

			if (smallest < 150, {smallest = smallest + 150;

										greatest = greatest + 150});

			o=Synth( \numbers, [freq: smallest/5, panpos: -1]);

			p=Synth( \numbers, [freq: greatest/5, panpos: 1]);

			0.1.wait;

			

			number=greatest-smallest;



			theNumber=number;

			if (theNumber < 150, {theNumber = theNumber + 150});

			o=Synth( \numbers, [freq: theNumber/5, panpos: 0]);

			number.postln;

			0.1.wait;

			a=a+1;

		});

		a.postln;

		if (a>g, {g=a});

	});

});

g.postln;

}.fork;

)
