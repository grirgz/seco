s.boot
s.gui
s.makeGui
s.scope
s.freqscope
s.class.methods.do(_.postln)
s.quit
s.plotTree
s.meter

(
~a = Pbind(
	\freq, Prout({
			Pseg([3,7,5,1,10],1.7,'linear',inf).embedInStream;	
		}),
	\dur, 2,
	\bla, Pfunc({ arg e; e[\freq].postln; })
);
~a.play
)

(
~a = Pbind(
	\freq, Prout({
			var ti = 1.7, dur = 2;
			100.do { arg i;
				(i*(dur/ti)).debug("index");
				[3,7,5,1,10].blendAt(i*(dur/ti)).yield;
			};
		}),
	\bla, Pfunc({ arg e; e[\freq].postln; })
);
~a.play
)
(
~a = Pbind(
	\freq, Pseq([7,10,2,5],inf),
	\bla, Pfunc({ arg e; e[\freq].postln })
);
~a.play
)
(
~a = Pbind(
	\freq, Pseq([207,310],inf)
);
~a.play
)


(
~a = (
	a: "plopplpopo",
	bla: { arg self; self.a.postln }
);
~a.bla = { arg x; x.postln; "hhh".postln };
~a.bla


)


~a = [4,7];
~a.wrapPut(44, 47)

45 % ~a.size

List[4,7,6].isList
