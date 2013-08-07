~name = \rah;

~buffers

(
	~name.dump.debug("ouais");
	Pdef(~name,
		Pbind(
			\instrument, \osc1,
			\ffreq, 100,
			\degree, Pseq([0,1,2,3,4,5],inf),
			\octave, 2,
			\dur, 0.25,
		).trace
	)
)
