
~b4 = Buffer.read(s,"ventregris.wav");

Synth("player",  [\buf, ~b4]);

Pdef(\plopi).trace.play


Pdef(\simple).play
(

Mdef(\simple, Pbind(\degree, 0) <> Pbind(\instrument, \pulsepass, \dur, 0.5),\pulsepass)
)
(
Pdef(\simple, 
Pfunc({ arg ev; 
	ev.debug("EV"); 
	if(ev.as(Dictionary).includesKey(\degree).debug("INC?")) {
		ev.removeAt(\freq);
	};
	ev.debug("EV2"); 
}) <>
Pbind(\freq, 100, \degree, 0, \noise, Pfunc({ arg ev; 
	ev.postcs;
	ev.includesKey(\noise).debug("key");
	ev[\noise] ?? 1;
}) ) <> Pbind(\noise, 1, \instrument, \pulsepass, \dur, 0.5),\pulsepass).play


)

s.meter

(
Pdef(\plopi, 
	Pbind(
		\degree, Pkey(\degree)+Pseg(Pseq([0,1,3,0],1,0),2,\step),
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
		\buf, ~b4,
		\legato, 0.8,
		\dur, 0.5,
		\amp, 0.8
	)
).play;
)
(
Mdef(\plopi, 
	Pbind(
		//\degree, Pseq([0,2,4,2],inf)+Pseg(Pseq([0, 0,3,4,3],1),1,\step),
		\degree, Pkey(\degree)+Pseg(Pseq([0, 0,1,3,0],2,0),2,\step),
		\repeat, 0,
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
		\buf, ~b4,
		\legato, 0.8,
		\dur, 0.5,
		\amp, 0.8
	),
	instr:\player2
);
Mdef(\vito, Pbind(
	\instrument, \player4,
	\startPos, Pseq([0.01,0.12,0.5,0.52],inf)+Pseg(Pseq([0,0.1],inf),1),
	\attack, 0.001,
	\release, 0.001,
	\buf, ~b4,
	\sustain, 0.05,
	\dur, 0.125,
	\amp, 0.5
));
Mdef(\vito2, Pbind(
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
));
Mdef(\gruik, Pbind(
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
));
Mdef(\vito3, Pbind(
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
));
Mdef(\vito4, Pbind(
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
));
Mdef(\vito5, Pbind(
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
));
)

(
Mdef(\gris, Ppar([
	Mdef(\vito2),
	Mdef(\plopi),
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

