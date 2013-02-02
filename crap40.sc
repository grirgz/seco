
(
SynthDef("helpscore",{ arg freq = 440;
        Out.ar(0,
                SinOsc.ar(freq, 0, 0.2) * Line.kr(1, 0, 0.5, doneAction: 2)
        )
}).store;
)


(
~a = Pbind(
	\instrument, \default,
	\freq, 200,
	\dur, Pseq([1,0.5,0.2],5),
	\amp, 0.1
).asScore(5);
)

~a.recordNRT("plop.score", "plop.aiff")

~a.render("plop.aiff", 5)



SynthDescLib.read;

// new pattern
(
p = Pbind(
		\instrument, \helpscore,
        \dur, Prand([0.3, 0.5], inf),
        \freq, Prand([200, 300, 500],inf)
);
)

// make a score from the pattern, 4 beats long
z = p.asScore(4.0);

z.score.postcs;
z.play;

// rendering a pattern to sound file directly:

// render the pattern to aiff (4 beats)

p.render("asScore-Help.aif", 4.0);

