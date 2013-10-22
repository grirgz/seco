(
SynthDef(\sound, {
	arg i_bus = 0, amp = 0.1, freq = 440, sustain = 1, pan = 0;
	var env, osc, out;
	env = EnvGen.kr(Env.perc(0.01, sustain, amp),doneAction:2);
	osc = SinOsc.ar([freq, freq * 1.005, freq * 0.995], mul:env);
	out = Pan2.ar(osc, pan);
	Out.ar(i_bus, out);
}).send(s);
)

(
~amp = 0.1;
a = Pdef(\a,
	Pbind(
		\instrument, \sound,
		\amp, ~amp * 1.5,
		\degree, Pseq([2, 4, 5, 6, 1] - 1, inf),
		\dur, Pseq([1, 1, 1, 1, 2] / 4, inf)
	)
).quant_([3, 0, 0, 1]);

b = Pdef(\b,
	Pbind(
		\instrument, \sound,
		\amp, ~amp,
		\degree, Pseq([8, 6, 5, 3, 4, 6] - 1, inf),
		\dur, Pseq([1, 1, 1, 1, 1, 1] / 4, inf)
	)
).quant_([3, 0, 0, 1]);

c = Pdef(\c,
	Pbind(
		\instrument, \sound,
		\amp, ~amp * 0.6,
		\degree, Pseq([1, 6, 8, 6, Pwrand([11,13], [2, 1].normalizeSum)] - 1, inf),
		\amp, Pseq([0, 1, 1, 1, 1] / 10, inf),
		\dur, Pseq([8, 1, 1, 1, 1] / 8, inf),
		\sustain, 0.1
	)
).quant_([3, 0, 0, 1]);
)

(
SystemClock.sched(0, Routine({
	a.play; 12.yield;
	b.play; 12.yield;
	c.play; 12.yield;
	c.pause; 6.yield;
	a.pause; 6.yield;

	b.pause; a.play; 12.yield;
	b.play; 12.yield;
	c.play; 12.yield;
	c.pause; a.pause; 6.yield;
	b.pause; 6.yield;
}));
)
