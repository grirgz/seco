
"plop".postln;
thisProcess.nowExecutingPath.debug("path");


(
Pbind(
	\degree, Pseq([1,2,3,4]),
	\dur, 1,
	\play, { 
		~freq.value.postln;
	}

).play
)


(
~a = (
bla_: { arg self, val; self[\bla] = val; val.debug("set"); }
)
)
~a.bla = 1
~a

~
