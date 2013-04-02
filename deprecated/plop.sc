(

 Ndef(\bla2, {
 	var sig;
	var osig = WhiteNoise.ar(1);
	//sig = SineShaper.ar(osig, MouseY.kr(0,1));
	sig = osig * Dust.ar(100);
	sig = SelectX.ar(MouseX.kr(0,1), [sig, osig]);
	sig ! 2;

 }

).play
)


(
a = Pbind(
	\dur, 0.5,
	\freq, Pseq([300, 400, 500]-50,2),
);
a = a <> Pbind(
	\cur, \plop,
	\freq, Pseq([300, 400, 500]),

);
a.play
)
(
a = Pbind(
	\dur, 0.5,
	\freq, Pseq([300, 400, 500],2),
);
b = a <> Pbind(
	\freq, Pseq([300, 400, 500]),

);
b.play
)

(
a = Pbind(
	\dur, 0.5,
);
b = a <> Pbind(
	\freq, Pseq([300, 400, 500]),

);

b = Psetpre(\freq, Pseq([300, 400, 500], 2), b);
b.play
)



(
{ SoundIn.ar(0) }.play
)
