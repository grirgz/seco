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
