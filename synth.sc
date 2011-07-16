
(
~synthDef_adsr = { arg name, func;
	SynthDef(name, { arg out=0, amp=0.1, gate=1, sustain=0.5;
		var env, envctl, ou;

		env = Env.adsr(0.02, 0.2, 0.25, 0.1, 1, -4);
		envctl = Control.names([\adsr]).kr( env.asArray );
		ou = SynthDef.wrap(func, prependArgs:[gate, sustain]) * EnvGen.kr(envctl, gate, doneAction:2);
		Out.ar(out, ou * amp);
	}).store;
};

~make_adsr = { arg argname;
		var env;
		env = Env.adsr(0.02, 0.2, 0.25, 0.1, 1, -4);
		Control.names([argname]).kr( env.asArray );
};

)
