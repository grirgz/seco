~name = \bla;


(
	~name.debug("ouais");
	Pdef(~name,
		Pbind(
			\degree, Pseq([0,1,2,3,4,5],inf),
			\dur, 0.25,
			\instrument, \default,
		).trace
	)
)
