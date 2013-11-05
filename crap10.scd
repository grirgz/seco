
p = ProxySpace.push;
v = ();

(
	v[\bus1] = Bus.audio(s,2);
	v[\bus2] = Bus.audio(s,2);
	v[\bus3] = Bus.audio(s,2);
)

(
~head = { arg fblevel=0.9, freq=300;
	var fb, fbin, sig;
	sig = SinOsc.ar(SinOsc.kr(1) * 0.5 + 1 * freq) * SinOsc.kr(1).range(0,1) * SinOsc.kr(2).range(0,1);
	fbin = InFeedback.ar(v[\bus1], 2);
	fb = fbin * fblevel + sig;
	Out.ar(v[\bus2], fb);
	fb;
};

~body = {
	var sig = InFeedback.ar(v[\bus2], 2);
	sig = Limiter.ar(sig, 0.9,0.1);
	Out.ar(v[\bus3], sig);
	sig;
};

~tail = {
	var sig = ~body.ar(2);
	sig = DelayC.ar(sig, 0.1,0.051);
	Out.ar(v[\bus1], sig);
	sig;
};

~tail.play;

)

~head.set(\fblevel, 4)
~head.set(\freq, 114)
~head.set(\freq, 214)
(
~body = {
	var sig = InFeedback.ar(v[\bus2], 2);
	sig = Limiter.ar(sig, 0.9,0.1);
	sig = sig.clip;
	Out.ar(v[\bus3], sig);
	sig;
};
	
)
~body.fadeTime = 0.01




(
~head = { arg fblevel=0.9, freq=300;
	var fb, fbin, sig;
	sig = SinOsc.ar(SinOsc.kr(1) * 0.5 + 1 * freq) * SinOsc.kr(1).range(0,1) * SinOsc.kr(2).range(0,1);
	fbin = InFeedback.ar()~tail.ar(2);
	fb = fbin * fblevel + sig;
	fb;
};

~body = {
	var sig = ~head.ar(2);
	sig = Limiter.ar(sig, 0.9,0.1);
	sig;
};

~tail = {
	var sig = ~body.ar(2);
	sig = DelayC.ar(sig, 0.1,0.051);
	sig;
};

~tail.play;

)

~head.set(\fblevel, 4)
~head.set(\freq, 114)
(
	
~body = {
	var sig = ~head.ar(2);
	sig = Limiter.ar(sig, 0.9,0.2);
	sig = sig.clip;
	sig;
};
)
~body.fadeTime = 1
