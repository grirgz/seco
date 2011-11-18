
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
		// [attackTime, decayTime, sustainLevel, releaseTime, peakLevel, curve]
		env = Env.adsr(0.02, 0.2, 0.25, 0.1, 1, -4);
		Control.names([argname]).kr( env.asArray );
};

~make_rgenadsr = { arg argname, gate, carrier, scale, doneAction=0;
		var env, con, envgen;
		env = Env.adsr(0.02, 0.2, 0.25, 0.1, 1, -4);
		con = Control.names([argname]).kr( env.asArray );
		envgen = EnvGen.ar(con, gate, levelScale:(carrier*scale), levelBias: carrier, doneAction:doneAction);
		envgen;
};

~make_genadsr = { arg argname, gate, doneAction=0;
		var env, envctl;
		// [attackTime, decayTime, sustainLevel, releaseTime, peakLevel, curve, levelScale, levelBias, timeScale]
		env = Env.adsr(0.02, 0.2, 0.25, 0.1, 1, -4);
		envctl = Control.names([argname]).kr( env.asArray ++ [1, 0, 1] );
		EnvGen.kr(envctl[..15], gate, envctl[16], envctl[17], envctl[18], doneAction);
};

~crossfade = { arg n, controls, data;

	var a;
	if(n <= 1, {
		(controls[0] * data[0]) + ((1-controls[0]) * data[1])
	}, {
		a = (((2**n)/2)-1).asInt;
		(controls[0] * thisFunction.(n-1, controls[1..], data[..a]))
		+ ((1-controls[0]) * thisFunction.(n-1, controls[1..], data[(a+1)..])) 

	})

};

~acrossfade = { arg controls, data_arrays;
	data_arrays.flop.collect { arg arr;
		~crossfade.(controls.size, controls, arr)
	};

};

)

