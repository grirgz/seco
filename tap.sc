
(
SynthDef(\bufplayer, { arg out=0, amp=0.1, gate=1, freq=200, buf;
	var ou;
	ou = PlayBuf.ar(1, buf, BufRateScale.kr(buf), 1, 0, 1);
	Out.ar(out, ou*amp);
}).store;
SynthDef(\bufplayer2, { arg out=0, amp=0.1, gate=1, freq=200, buf, loop=0;
	var ou;
	ou = PlayBuf.ar(2, buf, BufRateScale.kr(buf), 0, 0, loop, doneAction:2);
	Out.ar(out, ou*amp);
}).store;
SynthDef(\tapreader, { arg out=0, in=0, amp=0.1, gate=1, freq=200, buf;
	var ou;
	ou = PlayBuf.ar(1, buf, BufRateScale.kr(buf), trigger:0, startPos:0, loop:1);
	ou = ou + In.ar(in);
	Out.ar(out, ou);
}).add;

SynthDef(\tapwriter, { arg out=0, in=0, amp=0.1, gate=1, buf;
	var ou;
	ou = In.ar(in);
	RecordBuf.ar(ou, buf, offset:0, recLevel:1.0, preLevel:0, run:1, loop:1, trigger:1);
	Out.ar(out, ou ! 2);
}).add;

SynthDef(\fx_dist, { arg out=0, in=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	ou = In.ar(out);
	ou = ou.distort;
	ReplaceOut.ar(out, ou);
}).add;

SynthDef(\fx_delay, { arg out=0, in=0, amp=0.8, gate=1, pan=0, freq=200, maxdelaytime=0.3, delaytime=0.1, decaytime=0.1;
	var ou;
	ou = In.ar(out);
	ou = AllpassL.ar(ou, maxdelaytime, delaytime, decaytime) + ou;
	ou = amp * ou;
	ReplaceOut.ar(out, ou);
}).add;

SynthDef(\fx_tremolo, { arg out=0, in=0, amp=1, gate=1, pan=0, freq=4, minamp=0.1;
	var ou;
	ou = In.ar(out);
	ou = ou * SinOsc.ar(freq).range(minamp,amp);
	ReplaceOut.ar(out, ou);
}).add;
  
SynthDef(\sin, { arg out=0, amp=0.1, gate=1, freq=200;
	var ou;
	ou = SinOsc.ar(freq);
	ou = ou * EnvGen.ar(Env.asr(0.1,1,0.1), gate, doneAction:2);
	Out.ar(out, ou);
}).add;

)
s.boot
(
~buf1 = Buffer.alloc(s, s.sampleRate * 5, 1);
~synthbus1 = Bus.audio(s, 1);
~fxbus1 = Bus.audio(s, 1);
~outbus1 = Bus.audio(s, 1);
)
Synth(\bufplayer, [\buf, ~buf1])
~buf1.zero;
(
~bla = {
	~tapreader = Synth(\tapreader, [\in, ~synthbus1, \out, ~outbus1, \buf, ~buf1]);
	~fx = Synth.tail(1, \fx_dist, [\in, ~fxbus1, \out, ~outbus1]);
	Synth.tail(1, \tapwriter, [\in, ~outbus1, \out, 0, \buf, ~buf1]);

};

)
~fx.free
~bla.value
~fx.free; ~fx = Synth.after(~tapreader, \fx_delay, [\out, ~outbus1]);
~fx.free; ~fx = Synth.after(~tapreader, \fx_dist, [\out, ~outbus1]);
~fx.free; ~fx = Synth.after(~tapreader, \fx_tremolo, [\out, ~outbus1]);
s.queryAllNodes
(
	Pbind(
		\instrument, \sin,
		\freq, 350,
		\out, ~synthbus1,
		\dur, Pn(0.1,1)
	).play
)
