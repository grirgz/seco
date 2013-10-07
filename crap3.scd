
(
Ndef(\bli, {
	var sig = LFSaw.ar(100);
	sig = sig + Fb({ arg fb;
		var si;
		si = (fb * 1.41) + sig;
		si;
	},0.01);
	sig ! 2;
}).play;
)
