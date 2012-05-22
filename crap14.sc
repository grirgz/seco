s.boot



(
SynthDef("autosampler", { |buf, recenable=0|
   var source, gatedsource, onset;

   source = SoundIn.ar([0,1]);

   gatedsource = Compander.ar(source, source,
           thresh: 0.05, // adjust Gate threshold
           slopeBelow: 20,
           slopeAbove: 2,
           clampTime: 0.01,
           relaxTime: 0.1
       );

   onset = SetResetFF.kr(Lag.kr(Amplitude.kr(gatedsource.sum) * recenable, 0.2));

   SendTrig.kr(onset, 1, onset);

   RecordBuf.ar(source, buf, preLevel:0.0, run:1, loop:1, trigger:onset);
}).add;

SynthDef("player", { |buf, pan=0.0, amp=1.0|
       var sig = PlayBuf.ar(2, buf, doneAction:2);
       Out.ar(0, Pan2.ar(sig, pan, amp));
}).add;

SynthDef("player2", { |buf, freq=200, pan=0.0, amp=1.0, gate=1, rq=0.1, rq2=0.1|
	var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf), loop:1, doneAction:0);
	sig = sig.sum;
	sig = Mix.fill(5, { arg nn;
		nn = (nn+1)*100;
		FreqShift.ar(BPF.ar(sig, freq+nn, rq), 0 - nn)
	});
	sig = BPF.ar(sig, freq, rq2);
	sig = Normalizer.ar(sig, amp);
	sig = sig * EnvGen.ar(Env.adsr(0.1,0.1,0.8,0.1),gate,doneAction:2);
	Out.ar(0, Pan2.ar(sig, pan, amp));
}).add;

SynthDef("player3", { |buf, freq=200, pan=0.0, amp=1.0, gate=1, rq=0.1, rq2=0.1|
	var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf)*Rand(-1,1).sign, startPos:BufFrames.kr(buf)*Rand(0,0.5), loop:1, doneAction:0);
	sig = sig.sum;
	sig = Mix.fill(5, { arg nn;
		nn = (nn+1)*100;
		FreqShift.ar(BPF.ar(sig, freq+nn, rq), 0 - nn)
	});
	sig = BPF.ar(sig, freq, rq2);
	sig = Normalizer.ar(sig, amp);
	sig = sig * EnvGen.ar(Env.adsr(0.1,0.1,0.8,0.1),gate,doneAction:2);
	Out.ar(0, Pan2.ar(sig, pan, amp));
}).add;

SynthDef("player4", { |buf, freq=200, pan=0.0, amp=1.0, startPos=0, gate=1, rq=0.1, rq2=0.1, attack=0.1, release=0.1|
	var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf), startPos:BufFrames.kr(buf)*startPos, loop:1, doneAction:0);
	sig = sig * EnvGen.ar(Env.asr(attack,0.8,release),gate,doneAction:2);
	Out.ar(0, Pan2.ar(sig, pan, amp));
}).add;

SynthDef("player5", { |buf, freq=200, pan=0.0, amp=1.0, startPos=0, gate=1, rq=0.1, rq2=0.1, attack=0.1, release=0.1, shift=0, rate=1, ffreq=1000|
	var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf)*rate, startPos:BufFrames.kr(buf)*startPos, loop:1, doneAction:0);
	sig = FreqShift.ar(sig, shift);
	sig = BPF.ar(sig, ffreq, rq);
	sig = (sig*15).distort;
	sig = sig * EnvGen.ar(Env.asr(attack,0.8,release),gate,doneAction:2);
	Out.ar(0, Pan2.ar(sig, pan, amp));
}).add;
)
~b1 = Buffer.alloc(s, s.sampleRate * 1.0, 2);
~b2 = Buffer.alloc(s, s.sampleRate * 5.0, 2);
~b3 = Buffer.alloc(s, s.sampleRate * 5.0, 2);
~b4 = Buffer.alloc(s, s.sampleRate * 10.0, 2);
~b4.write("ventregris.wav","WAV","float")

(
o = OSCresponderNode(s.addr,'/tr',{ arg time,responder,msg;
   [time,responder,msg].postln;
}).add
);

d = Synth("autosampler", [\buf, ~b4, \recenable, 1]);
d.free

d.set(\recenable, 1); // execute and hit the drum
d.set(\recenable, 0); // stop recording

Synth("player2",  [\buf, ~b3, \freq,500, \rq, 0.01]);
Synth("player",  [\buf, ~b3]);
Synth("player",  [\buf, ~b4]);
Synth("player",  [\buf, 0]);

~seq.model.livenodepool.keys
~data = ~seq.get_node("audiotrack_expander_l1050").save_data
~l1052 = ~seq.get_node("audiotrack_expander_l1052")
~l1052.load_data(~data)
~data.postcs
~


{ SoundIn.ar([0,1]) }.play


	Pbind(\degree, Pseg(Pseq([7,1,3,0],inf,0),2,\step),
		\dur, 1	
	).play

	s.boot

Main.version

(
Pdef(\plopi, 
	Pbind(
		\degree, Pkey(\degree)+Pseg(Pseq([0,1,3,0],inf,0),2,\step),
		\dur, Pkey(\dur) * 2
	
	) 
	<> Pdef(\notes)
	<> Pbind(
		\instrument, \player2,
		\degree, Pseq([0,3],inf),
		\degree, Pseq([[0,2,4],[3,5,7],[4,6,8],[3,5,7]].stutter(4),inf),
		\octave, 5,
		\rq, 0.1,
		\rq2, 0.001,
		\buf, ~b2,
		\legato, 0.8,
		\dur, 0.5,
		\amp, 0.8
	)
).play;
)

(
Pdef(\vito, Pbind(
	\instrument, \player4,
	\startPos, Pseq([0.01,0.12,0.5,0.52],inf)+Pseg(Pseq([0,0.1],inf),1),
	\attack, 0.001,
	\release, 0.001,
	\buf, ~b4,
	\sustain, 0.05,
	\dur, 0.125,
	\amp, 0.5
)).play;
)
(
Pdef(\vito2, Pbind(
	\instrument, \player5,
	\startPos, Pseq([0.01,0.12,0.5,0.32],inf)+0.00+Pseg(Pseq([0,0.4],inf),4),
	\attack, 0.001,
	\release, Pseq([0.1,0.05],inf),
	//\rate, 2.11,
	\shift, -200,
	\shift, Ndef(\bla3, { LFSaw.kr(0.5).range(-500,500) }),
	\rate, Ndef(\bla, { SinOsc.kr(0.5).range(0.1,1) }),
	\freq, Pseq([1,\r,\r,1, 1,\r,1,\r],inf),
	\ffreq, 080,
	\rq, 0.01,
	\buf, ~b4,
	\sustain, 0.01,
	\dur, 0.125,
	\amp, 355.7
)).play;
)

(
Pdef(\gruik, Pbind(
	\instrument, \player5,
	\startPos, Pseq([0.020],inf),
	\attack, 0.0001,
	\release, Pseq([0.1,0.05],inf),
	\release, Pseq([0.15],inf),
	//\rate, 2.11,
	\rate, 0.5,
	\freq, Pseq([1,\r,\r,\r],inf),
	\ffreq, 70,
	\rq, 0.4,
	\buf, ~b4,
	\sustain, 0.05,
	\dur, 0.25,
	\amp, 015.7
)).play;
)

(
Pdef(\vito3, Pbind(
	\instrument, \player5,
	\startPos, Pseq([0.120,0.121],inf),
	\attack, 0.0001,
	\release, Pseq([0.1,0.05],inf),
	\release, Pseq([0.15],inf),
	//\rate, 2.11,
	\rate, 0.5,
	\freq, Pseq([1,\r,\r,1, 1,\r,1,\r],inf),
	\ffreq, 70,
	\rq, 0.4,
	\buf, ~b4,
	\sustain, 0.05,
	\dur, 0.125,
	\amp, 005.7
)).play;
)

(
Pdef(\vito4, Pbind(
	\instrument, \player5,
	\startPos, Pseq([0.820,0.821],inf),
	\attack, 0.0001,
	\release, Pseq([0.1,0.05],inf),
	\release, Pseq([0.25,0.15],inf),
	//\rate, 2.11,
	\rate, 0.5,
	\freq, Pseq([
		\r,1,1,\r, \r,1,\r,1,
		\r,1,1,\r, \r,1,\r,1,
		\r,\r,1,\r, \r,1,\r,1,
		\r,1,1,\r, 1,1,\r,1,
	],inf),
	\ffreq, 1570,
	\rq, 5.4,
	\buf, ~b4,
	\sustain, 0.05,
	\dur, 0.25,
	\amp, 000.7
)).play;
)

(
Pdef(\vito5, Pbind(
	\instrument, \player5,
	\startPos, Pseq([0.620],inf),
	\attack, 0.071,
	\release, Pseq([0.1,0.05],inf),
	\release, Pseq([0.10],inf),
	//\rate, 2.11,
	\rate, 0.5,
	\freq, Pseq([
		1,\r, 1,\r, 1,\r, 1,1,
		1,\r, 1,\r, 1,\r, 1,1,
		1,\r, 1,\r, 1,\r, \r,1,
		1,\r, 1,\r, 1,1, 1,1,
		],inf),
	\ffreq, 2570,
	\rq, 0.4,
	\buf, ~b4,
	\sustain, 0.005,
	\dur, 0.125,
	\amp, 012.7
)).play;
)

(
Pdef(\gris, Ppar([
	Pdef(\vito2),
	Pdef(\plopi),
])).play
)

(
~bla = [
	(
		degree: [0, 2, 4],
		dur: 1
	),
	(
		degree: [1,2],
		dur: 1
	)
];
~bla1 = ~gen1.();
~bla1 = ~gen2.();
~bla1 = ~gen3.();
~bla2 = Pdef(\notes, Pseq(({~gen3.()} ! 4).flat,inf));
Pdefn
)

(
~gen1 = {
	var chord, dur;
	chord = [0, 2, 4, 7];
	dur = 1/chord.size;
	chord.collect { arg ch;
		(degree: ch, dur:dur)
	}
};

~gen2 = {
	var chord, dur, res;
	chord = [0, 2, 4, 7];
	dur = 1/chord.size;
	res = List.new;
	res.add(chord.collect { arg ch;
		(degree: ch, dur:dur/4)
	});

	res.add(4.collect { arg ch;
		(degree: chord.choose, dur:dur/4)
	});

	res.add(4.collect { arg ch;
		(degree: chord.choose, dur:dur/4)
	});

	res.add(chord.collect { arg ch;
		(degree: ch, dur:dur/4)
	}.reverse);

	res.flat;
};

~gen3 = {
	var res = ~gen2.();
	res = res.collect { arg no, x;
		if( (x % 8) == 1 ) { no.degree = \rest }; 
		if( (x % 3) == 0 ) { no.degree = \rest }; 
		//if( (x % 3) == 0 ) { oldno.legato = 1 }; 
		if( (x % 8) == 0 ) { no.degree = \rest }; 
		no;
	};
	res;
};



)

1+1
~res.printAll
~res4.free
~res4 = nil
~res2
(
~res4 = 500.collect { arg i;
	~make_player_from_synthdef.(~seq, \lead2)
};
)

0.5.coin
(
100.collect { 
	if(0.5.coin) { 1 } { 0 }
}
)



(// * //watch the postWindow
 a=["m","x","f","k","d","p"];
 x=Array;
 y=x.newClear(6);
~mxfkdp=Task({
	inf.do({
	 	y.put(x.series(6,0,1).choose,a.scramble).postln;
		0.03.wait;
	})
}).play;

m=fork({
	inf.do({
		//"array num:".postcln;
		if(y[x.series(6,0,1).choose]==a, {
		 	"lo encontraste!/found it!".postcln;
			{ GVerb.ar(
					 EnvGen.ar(Env.perc(0.01,8),1,doneAction:2)*
					 Blip.ar(XLine.kr(440,1600,8)),50,10,0.02)
			}.play;
			~mxfkdp.stop;
			m.stop;
		},{
			//"buscando.../seeking...".postcln;
		});
		0.03.wait
	})
});
);
//.SCtweetLong




{CombC.ar(Klang.ar(`[[100,101,1000,1001]],1,0)*0.1,0.33,LFTri.ar(0.1, 0, 0.1, 0.11)+LFTri.ar(0.17, 0, 0.1, 0.22),10)!2}.play;



//Variation

{CombC.ar(Klang.ar(`[[100,101,1000,1001]],1,0)*0.1,1.33,LFTri.ar(0.3, 0, 0.1, 0.71)+LFTri.ar(0.7, 0, 0.1, 0.52),10)!2}.play;


(
PfadeOut(PfadeIn(Pbind(
	\instrument, \pulsepass,
	\freq, 200,
	\dur, 1,
	\bla, Pfunc{"bla".postln},
	\amp, 0.1
),2),2,~bla).play;
)
~bla = inf
~bla = 0
