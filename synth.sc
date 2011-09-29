
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

