
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

~make_adsr = { arg argname, default=nil;
		var env;
		// [attackTime, decayTime, sustainLevel, releaseTime, peakLevel, curve]
		env = default ?? Env.adsr(0.02, 0.2, 0.25, 0.1, 1, -4);
		//Control.names([argname]).kr( env.asArray );
		argname.kr(default);
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

SynthDef(\modenv, { |out, firstsynth=0, firstval=0, t_trig=1, gate=1, tsustain, val=0, curve=0, doneAction=0|
	var start = Select.kr(firstsynth, [In.kr(out, 1), firstval]);
	var sig;
	//start.poll;
	//sig = EnvGen.kr(Env([start, val], [tsustain], curve), t_trig, doneAction: doneAction);
	sig = EnvGen.kr(Env([start, val], [tsustain], curve), t_trig, doneAction: doneAction);
	//sig = VarLag.kr(val, tsustain);
	//sig.poll;
	ReplaceOut.kr(out, sig);
}).store;

SynthDef(\setbus, { |out, val=0, gate=1, doneAction=2|
	
	Out.kr(out, val);
}, metadata:(specs:(
	val: \unipolar,
))).store;

SynthDef(\freeze_recorder, { arg inbus=0, out=0, amp=1, gate=1, buffer, doneAction=2;
	var ou;
	var bufnum = buffer;
	var env;
	var in = In.ar(inbus, 2);
	in = in * amp;
	env =  EnvGen.kr(Env.asr(0.0001,1,0.0001), gate, doneAction:doneAction);
	RecordBuf.ar(in, bufnum, doneAction: doneAction, loop: 0);
}).store;

SynthDef(\freeze_player, { arg out=0, amp=1, gate=1, doneAction=2, buffer;
	var player,env;
	var bufnum = buffer;
	var speed = 1;
	var pos = 0;
	var loop = 0;
	//env =  EnvGen.kr(Env.asr(0.0001,1,0.0001), gate, doneAction:doneAction);
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:doneAction, loop: loop);
	//player = player * env * amp;
	player = player * amp;
	Out.ar(out, player);

}).store;


SynthDef(\audiotrack, { arg out = 0, amp=0.20, bufnum = 0, sustain, delay=0.046;
        var playbuf, ou;
        playbuf = PlayBuf.ar(2,bufnum,startPos:BufSampleRate.kr(bufnum)*delay,doneAction:0);
        //playbuf = PlayBuf.ar(2,bufnum,startPos:0,doneAction:0);
		//ou = playbuf * EnvGen.ar(Env.asr(0.01,1,0.01), gate, doneAction:2);
		ou = playbuf * EnvGen.ar(Env.linen(0.001,sustain,0.001), doneAction:2);
        Out.ar(out, ou * amp);
}).store;

SynthDef(\audiotrack_noisegate, { arg out = 0, amp=0.20, bufnum = 0, sustain, noisegate=0.0, at=0.2, rt=0.2, delay=0.046;
        var playbuf, ou;
        playbuf = PlayBuf.ar(2,bufnum,startPos:BufSampleRate.kr(bufnum)*delay,doneAction:0);
        //playbuf = PlayBuf.ar(2,bufnum,startPos:0,doneAction:0);
		//ou = playbuf * EnvGen.ar(Env.asr(0.01,1,0.01), gate, doneAction:2);
		ou = playbuf;
		//ou = ((ou < noisegate) * ou) + ((ou > noisegate) * (ou*100).tanh.distort);
		//ou = (ou*100).distort;
		ou = ou * EnvGen.ar(Env.linen(0.001,sustain,0.001), doneAction:2);
		ou = (Amplitude.kr(ou,at,rt) > noisegate) * ou;
        Out.ar(out, ou * amp);
}, metadata:(specs:(
	noisegate: ControlSpec(0, 0.1, \lin, 0, 0),
	at: ControlSpec(0, 1, \lin, 0.0001, 0),
	rt: ControlSpec(0, 1, \lin, 0.0001, 0)
))).store;

SynthDef(\audiotrack_expander, { arg out = 0, amp=0.20, bufnum = 0, sustain, 
			delay=0.046, fadein=0.001, fadeout=0.001,
			wet=1, threshold=0.0, slopeBelow=1, slopeAbove=1, clampTime=0, relaxTime=0;
        var playbuf, ou, cou;
        playbuf = PlayBuf.ar(2,bufnum,startPos:BufSampleRate.kr(bufnum)*delay,doneAction:0);
		ou = playbuf;
		ou = ou * EnvGen.ar(Env.linen(fadein,sustain,fadeout), doneAction:2);
		cou = Compander.ar(ou, ou, threshold, slopeBelow, slopeAbove, clampTime, relaxTime);
		ou = SelectX.ar(wet, [ou, cou]);
        Out.ar(out, ou * amp);
}, metadata:(specs:(
	delay: ControlSpec(0, 0.1, \lin, 0, 0),
	fadein: ControlSpec(0, 0.01, \lin, 0, 0),
	fadeout: ControlSpec(0, 0.01, \lin, 0, 0),
	threshold: ControlSpec(0, 0.1, \lin, 0, 0),
	clampTime: ControlSpec(0, 1, \lin, 0.0001, 0),
	relaxTime: ControlSpec(0, 1, \lin, 0.0001, 0)
))).store;

SynthDef(\metronome, { arg out=0, amp=1, gate=1, freq=220, pan=0;
	var ou;
	ou = SinOsc.ar([freq,freq/2])+LFSaw.ar(freq+0.1);
	ou = LPF.ar(ou,freq,0.1);
	ou = ou * EnvGen.ar(Env.asr(0.0001, 1, 0.01),gate, doneAction:2);
	ou = Pan2.ar(ou,pan,amp*3);
	Out.ar(out,ou);
}).store;

SynthDef(\record_input, { arg out = 0, bufnum = 0, sustain;
		var input, env;
        input = SoundIn.ar([0,1]);
		env = EnvGen.kr(Env.linen(0,sustain,0), doneAction:2); // stop recording after dur..
        RecordBuf.ar(input, bufnum, doneAction: 0, run:env, loop: 0);
}).store;

SynthDef(\record_input_mono, { arg out = 0, bufnum = 0, sustain;
		var input, env;
        input = SoundIn.ar([0,1]).sum;
		env = EnvGen.kr(Env.linen(0,sustain,0), doneAction:2); // stop recording after dur..
        RecordBuf.ar(input, bufnum, doneAction: 0, run:env, loop: 0);
}).store;
)
