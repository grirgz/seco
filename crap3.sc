
(
SynthDef(\sin1, { arg out=0, pan=0, gate=1, sustain=0.5, amp=0.1, freq=200;
	
	var ou;

	ou = Pulse.ar(freq);
	ou = ou * EnvGen.ar(Env.linen(0.01,sustain,0.01,1),doneAction:2);
	ou = Pan2.ar(ou,pan,amp);
	Out.ar(out, ou);

}).add;

SynthDef(\sin3, { arg out=0, pan=0, gate=1, sustain=0.5, amp=0.1, freqi=200;
	
	var ou, freq = freqi;

	ou = Pulse.ar(freq);
	ou = ou * EnvGen.ar(Env.linen(0.01,sustain,0.01,1),doneAction:2);
	ou = Pan2.ar(ou,pan,amp);
	Out.ar(out, ou);

}).add;

SynthDef(\sin2, { arg out=0, pan=0, gate=1, sustain=0.5, amp=0.1, freq=200,
					maxdtime=0.2, dtime=0.2, decay=2;
	
	var ou;

	ou = Pulse.ar(freq);
	ou = ou * EnvGen.ar(Env.linen(0.01,sustain,0.01,1),doneAction:2);
	ou = Pan2.ar(ou,pan,amp);
	ou = CombL.ar(ou, maxdtime, dtime, decay, 1, ou);
	Out.ar(out, ou);

}).add;

SynthDef(\lpf, { arg in=0, out=0, gate=1, freqfm=4, freq_fc=200;
	
	var ou;

	ou = In.ar(in,2);
	ou = LPF.ar(ou, SinOsc.ar(freqfm)*50+freq_fc);
	Linen.kr(gate,0,1,0,doneAction:2);
	//DetectSilence.ar(ou,0.001,0.01,doneAction:2);
	Out.ar(out, ou);

}).add;

SynthDef(\echo, { arg out=0, in=0, maxdtime=0.2, release=1, dtime=0.2, decay=2, gate=1;
        var env, ou;
        env = Linen.kr(gate, 0.05, 1, decay, 2);
        in = In.ar(in, 2);
		ou = CombL.ar(in, maxdtime, dtime, decay, 1, in);
		//DetectSilence.ar(ou,0.001,0.1,doneAction:2);
        Out.ar(out, ou);
}, [\ir, \ir, \ir, 0.1, 0.1, 0]).add;

SynthDef(\sinosc, { arg out=0, gate=1, amp=0.1, carrier=200, freq=5;
        var env, ou;
        env = Linen.kr(gate, 0, 1, 0, 14);
		ou = SinOsc.kr(freq);
        Out.kr(out, ou*(amp*carrier)+carrier);
}, [\ir,0,0.5,0.4,0.4]).add;
SynthDef(\sinosc3, { arg out=0, gate=1, amp=0.1, carrier=200, freq=5;
        var env, ou;
        env = Linen.kr(gate, 0, 1, 0, 2);
		ou = SinOsc.kr(freq);
        Out.kr(out, ou*(amp*carrier)+carrier);
}, [\ir,0,0.5,0.4,0.4]).add;

SynthDef(\sinosc2, { arg out=0, gate=1, amp=0.1, carrier=200, freq=5, outlag=0.1;
        var env, ou;
        env = Linen.kr(gate, 0, 1, 0, 14);
		ou = SinOsc.kr(freq);
		ou = Lag.kr(ou, outlag);
        Out.kr(out, ou*(amp*carrier)+carrier);
}, [\ir]).add;

)



s.queryAllNodes; // note the default group (ID 1)
s.boot






(
~short_ppar = { arg list;
	Pspawner({ |spawner|
		var str;
		list.do({ arg pat;
			str = CleanupStream(pat.asStream, {
				spawner.suspendAll;
			});
			spawner.par(str);
		})
	});
};
)

(
~pfx = { arg pat, effects;
	Pspawner({ |spawner|
		var str, bus, group;
		bus = Bus.audio(s,2);
		group = Group.after(1);
		pat = Pset(\out, bus, pat);
		effects = Pset(\in, bus, effects);
		effects = Pset(\group, group, effects);
		str = CleanupStream(pat.asStream, {
			spawner.suspendAll;
			bus.free;
		});
		spawner.par(str);
		spawner.par(effects)
	});
};
~pfx2 = { arg pat, effects;
	Pspawner({ |spawner|
		var str, pbus, pgroup, leffect;
		pbus = Bus.audio(s,2);
		pat = Pset(\out, pbus, pat);
		str = CleanupStream(pat.asStream, {
			"cleaunppp".debug;
			spawner.suspendAll;
			pbus.free;
		});
		spawner.par(str);
		pgroup = 1;
		effects[..effects.size-2].do { arg ef;
			ef = Pset(\in, pbus, ef);
			pbus = Bus.audio(s,2);
			ef = Pset(\out, pbus, ef);
			pgroup = Group.after(pgroup);
			ef = Pset(\group, pgroup, ef);
			spawner.par(ef)
		};
		leffect = effects.last;
		leffect = Pset(\in, pbus, leffect);
		pgroup = Group.after(pgroup);
		leffect = Pset(\group, pgroup, leffect);
		spawner.par(leffect)
	});
};
~pfx3 = { arg pat, controls, effects;
	Pspawner({ |spawner|
		var str, pbus, pgroup, leffect;
		pbus = Bus.audio(s,2);
		pat = Pset(\out, pbus, pat);
		str = CleanupStream(pat.asStream, {
			spawner.suspendAll;
			pbus.free;
		});
		spawner.par(str);
		pgroup = 1;
		effects[..effects.size-2].do { arg ef;
			ef = Pset(\in, pbus, ef);
			pbus = Bus.audio(s,2);
			ef = Pset(\out, pbus, ef);
			pgroup = Group.after(pgroup);
			ef = Pset(\group, pgroup, ef);
			spawner.par(ef)
		};
		leffect = effects.last;
		leffect = Pset(\in, pbus, leffect);
		pgroup = Group.after(pgroup);
		leffect = Pset(\group, pgroup, leffect);
		spawner.par(leffect)
	});
};
)

s.queryAllNodes; // note the default group (ID 1)

(
x = ~pfx.(
	Pbind(
		\type, \note,
		\instrument, \sin1,
		//\out, b,
		//\group, g,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4])*100,
		\dur, 0.1
	),
	Pmono(
		\echo,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 5,
		\dtime, 0.1
		//\group, g
	)
);

x.trace.play;
)
Ppar([Pseq([x,Event.silent(5)],inf),y]).play

(
x = ~pfx2.(
	Pbind(
		\type, \note,
		\instrument, \sin1,
		//\out, b,
		//\group, g,
		\amp, 0.05,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4])*100,
		\dur, 2.1
	),
	[
//	Pmono(
//		\echo,
//		\release, 1,
//		\dtime, Pseq([0.3,0.2,0.1],inf),
//		\decay, 0.51,
//		\dtime, 0.1
//		//\group, g
//	)
//	,
//	Pmono(
//		\echo,
//		\release, 1,
//		\dtime, Pseq([0.3,0.2,0.1],inf),
//		\decay, 1,
//		\dtime, 0.51
//		//\group, g
//	)
	]
);

Pn(x,3).play;
)
s.queryAllNodes; // note the default group (ID 1)
s.boot
~a = EventPatternProxy.new;
~a.source = x;
Pn(~a,inf).play
~a.stop
(
~a = EventPatternProxy.new;
~b = EventPatternProxy.new;
~p = Pbind(\degree, Pseq([0,1,2,3,4,5],inf), \dur, 0.3, \legato, 0.2);
~q = Pbind(\degree, Pseq([0,1,2,3,4,5]-2,inf), \dur, 0.6, \legato, 0.1, \amp, 0.5);
~ef1 = Pmono(
		\echo,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 5,
		\dtime, 0.1
	);
~ef2 = Pmono(
		\echo,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 1,
		\dtime, 0.2
	);
)
(
~a.source = ~pfx.(~p, ~ef1);
~b.source = ~pfx.(~q, ~ef2);
~a.play;
~b.play;
)


(
	y = Pbind(
		\type, \note,
		\instrument, \sin1,
		\out, 0,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4],inf)*100,
		\dur, 1
	);
	y.play
)


/////////////////////////////////////////////////////////////////////::
// controls




(
~bus = Bus.control(s, 1);
~group = Group.before(1);
~short_ppar.([
	Pbind(
		\instrument, \sin3,
		\freqi, ~bus.asMap,
		\legato, 1,
		\dur, Pn(2,2)
	),
	PmonoArtic(
		\sinosc3,
		\carrier, Pseq([400,100],inf),
		\freq, Pseq([20,2],inf),
		\amp, Pseq([0.5,0.2],inf),
		\out, ~bus,
		\outlag, 1,
		\legato,0.1,
		\group, ~group,
		\dur, 0.7
	),
]).play;
)
Plag
SynthDef
EnvGen
Synth
Group

(
~bus = Bus.control(s, 1);
Synth(\sinosc, [\freq, 2, \out, ~bus],target:~group);
Synth(\sin1, [\freq, ~bus.asMap, \sustain, 1]);
)
Synth(\sin1, [\freq, 200, \sustain, 1]);
s.queryAllNodes; // note the default group (ID 1)
Synth(\sinosc, [\freq, 2, \out, ~bus])
~bus.scope
PmonoArtic
Pmono

(
~pfx3 = { arg pat, controls, effects;
	Pspawner({ |spawner|
		var str, pbus, pgroup, leffect;



		pbus = Bus.audio(s,2);
		pat = Pset(\out, pbus, pat);
		str = CleanupStream(pat.asStream, {
			spawner.suspendAll;
			pbus.free;
		});
		spawner.par(str);
		pgroup = 1;
		effects[..effects.size-2].do { arg ef;
			ef = Pset(\in, pbus, ef);
			pbus = Bus.audio(s,2);
			ef = Pset(\out, pbus, ef);
			pgroup = Group.after(pgroup);
			nw = NodeWatcher.register(pgroup);
			ef = Pset(\group, pgroup, ef);
			spawner.par(ef)
		};
		leffect = effects.last;
		leffect = Pset(\in, pbus, leffect);
		pgroup = Group.after(pgroup);
		leffect = Pset(\group, pgroup, leffect);
		spawner.par(leffect)
	});
};
)
