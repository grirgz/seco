
(
Ndef(\plop, {
	var sig;
	sig = SoundIn.ar([0,1]);
	sig = sig * SinOsc.ar(100);

}).play;
)
