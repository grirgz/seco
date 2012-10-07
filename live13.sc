s.meter
(
s.waitForBoot{
~seq = Mdef.force_init(true);
~synthlib = [
	\audiotrack_expander,
	\lead2,
	\pulsepass,
	\flute1,
	\miaou1,
	\ringbpf1,
	\piano2,
	\pmosc,
	\monosampler,
	\stereosampler,
	\ss_comb,
	\ss_combfreq,
].collect({ arg i; i -> i });

~effectlib = [
	\echo
].collect({arg i; i -> i });

~samplelib = [
	"sounds/perc1.wav",
	"sounds/pok1.wav",
	"sounds/amen-break.wav",
	"sounds/default.wav"
];
~seq.load_patlib( ~synthlib );
~seq.load_effectlib( ~effectlib );
~seq.set_presetlib_path("mypresets2");
~seq.append_samplelib_from_path("sounds/" );
~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );


~tf = Pfunc({ arg ev; if(ev[\stepline] == 1) { \note } { \rest } });
~ff = Pfunc({ arg ev; if(ev[\stepline1] == 1) { 100 } { \rest } });
//Debug.enableDebug = false;

Mdef.side_gui;
}
)

Mdef.main.save_project("merdier");
Mdef.main.load_project("merdier");
Debug.enableDebug = true;
Debug.enableDebug = false;
[2,4,6,1].collect { arg deg; deg.postln; Scale.major.degreeToFreq(deg, 60.midicps, 1)}
[2,4,7,1].collect { arg deg; deg.postln;}

(
Mdef.env(\mytest, Pbind(
 \instrument, \default,
 \freq, Ref(Env([700,200,300,400],[1,0.5,1],0)),
 \amp, Ref(Env([0.1,1,0.1],[0.5,2],0)),
 \dur, Pn(1,4)
));
)
Scale.major.degreeToFreq(2, 60.midicps, 1);  
(
Mdef.env(\merdier, Pbind(
 \instrument, \lead2,
 //\freq, Ref(Env([2,4,0,4].collect { arg deg; Scale.major.degreeToFreq(deg, 60.midicps, 1)},[1,0.5,1,1],4)),
 \octave, 4,
 \degree, Pseq([[0,2,4],[2,4,6]]),
 //\degree, Ref(Env([7,2,3,4,1],[1,0.5,1,10],4)),
 \rq, Ref(Env([0.1,1,0.1],[0.5,2],0)),
 \wet, Ref(Env([0,1,0.5],[2,2,0.1,0.5],0)),
 \mdetune, Ndef(\bla,{ SinOsc.kr(SinOsc.kr(1).range(0.5,5)).range(0.5,1.5) }),
 \fbase, Ref(Env([1.7,1,3.4,4]*700,[0.5,0.2,1],0)),
 \mylegato, 1,
 \legato, 0.90,
 \amp, 0.3,
 \dur, Pn(4,2)
));
)
Pdef(\merdier).stop
Pn(Pdef(\mytest),inf).play


Mdef.main.model.latency = 0

(
Mdef(\kick, Pbind(
	\instrument, \kick2,
	\release, Pseq([0.26,0.01,0.78,0.2],inf),
	\amp, 0.5,
	\stepline, Pseq([
		1,0,0,0, 1,0,0,0,
		1,0,1,0, 1,0,1,0,
		1,0,0,0, 1,0,0,0,
		1,1,0,1, 1,0,1,0,
	],inf),
	\type, ~tf,
	\fratio, Pseq([0.1,0.5,2,1.5],inf),
	\dur, Pseq([0.1,0.1]*2.5/2,inf),
));
);

(
Mdef(\kick3, Pbind(
	\instrument, \kick2,
	\release, 0.2,
	\amp, 0.1,
	\stepline1, Pseq([
		1,0,1,0, 1,0,1,0,
	],inf),
	//\freq, ~ff,
	\fratio, 1.1,
	\dur, Pseq([0.1,0.1]*2.5/2,inf),
));
);
s.queryAllNodes
(
Mdef(\mymy, Ppar([
	Pdef(\kick3)
]))
);

(
Mdef(\snare2, Pbind(
	\instrument, \kraftySnr,
	\freq, Pseq([2000,3000,4000,2000]*2,inf),
	\decay, Pseq([0.26,0.07,0.18,0.2],inf),
	\amp, 1.5,
	\stepline, Pseq([
		0,0,1,0, 0,0,1,1,
		1,0,1,0, 0,0,1,0,
		1,1,1,0, 1,0,1,1,
		1,0,1,0, 1,1,0,1,
	],inf),
	\type, ~tf,
	\rq, Pseq([0.7,1.5,2,1.5],inf),
	\dur, Pseq([0.1,0.1]*2.5/2,inf),
));
);

(
Mdef(\sax2, Pbind(
	\instrument, \sax,
	\root, 0,
	\octave, [3,2],
	\rq, Pseq([1.8,2],inf),
	\degree, Pseq([0,2], inf),
	\mylegato, 1,
	\hdelta, 1.05,
	\legato, 0.7,
	\amp, 0.2,
	\dur, 4.0
));
);
(
Mdef(\organ, Pbind(
	\instrument, \organ,
	\root, 0,
	\octave, [5,4],
	\rq, Pseq([1.8,2],inf),
	\degree, Pseq([Pseq([0,1,5,4],2), Pseq([2,3,7,4],2)], inf).stutter(2),
	\stepline, Pseq([
		1,
		1,1,0,1, 0,1,1,1
	],inf),
	\mylegato, 1,
	\hdelta, 1.05,
	\legato, 0.7,
	\repeat, 2,
	\amp, 0.2,
	\dur, 1.0/4
));
);
(
Mdef(\lead, Pbind(
	\instrument, \lead,
	\root, 0,
	\octave, 5,
	\rq, Pseq([1.8,2],inf),
	\repeat, 0,
	\degree, Pseq([0,2], inf),
	\mylegato, 1,
	\hdelta, 1.05,
	\legato, 1,
	\amp, 0.2,
	\dur, 4
));
);

(
Mdef(\perc, Ppar([
	Pdef(\kick),
	Pdef(\snare2),
]))

);

(
Mdef(\melo, Ppar([
	Pdef(\lead),
	Pdef(\organ),
	Pdef(\sax2),
]))

);


(
SynthDef(\compressor, { arg out=0, amp=0.1, threshold=0.5, slopeBelow=1, slopeAbove=1, clampTime=0.01, relaxTime=0.1;
	var ou;
	ou = In.ar(0,2);
	//ou = ou * 0.1;
	ou = Compander.ar(ou, ou, threshold, slopeBelow, slopeAbove, clampTime, relaxTime);
	ou = ou * amp;
	//ou = [0,0];
	ReplaceOut.ar(out, ou);

}).add;
)
Quarks.gui
(
~comp = Synth.tail(s, \compressor);
~comp.set(\threshold, 0.5);
~comp.set(\slopeBelow, 1.0);
~comp.set(\slopeAbove, 1/48);
~comp.set(\clampTime, 0.002);
~comp.set(\amp, 0.5);
)

(
Mdef(\part1, Ppar([
	Pdef(\perc),
	Pdef(\melo),
]))

);

Mdef.node(\par1).children.remove(\sax)
Mdef.node(\par1).children
a = [4,5];
a.remove(4)
a


Object.enableDebug
Debug


Array.series(16, 1, 1) * Array.exprand(16, 0.995, 1.001);



(
SynthDef(\ctlPoint, { |outbus, value, time, curve|
       var     start = In.kr(outbus, 1);
       ReplaceOut.kr(outbus, EnvGen.kr(Env([start, value], [time], curve), doneAction: 2));
}).store;
)

~bus = Bus.control(s,1);
~bus.set(100);
(
Pbind(
	\instrument, \ctlPoint,
	\outbus, ~bus,
	\value, Pseq([1,2,3,4,2,1])*50,
	\curve, 0,
	\time, Pfunc({arg ev; ev[\dur] / TempoClock.default.tempo }),
	\dur, 1,
).play
)

a = Synth(\lead2, [\amp, 0.5, \freq, ~bus.asMap])


Quarks.gui


Pbind(
	\type, \envnote,
	\instrument, \lead2,
	\freq, Pvenv(Pseq([2,1,4])),
	\rq, Pvenv(Pseq([1,2])),
	\dur, 1,

)

(
Event.addEventType(\myNote, { |server|
	"plop".postln;
    ~type = \note;
    ~play.value;
});
)

(
Pbind(
	\type, \myNote,
	\instrument, \lead2,
	\freq, Pseq([1,2,5,4]*50,inf),
	\dur, 1,
	\finish, { "fini".postln }

).play
)

~env = { arg in; in };


a = Env([1,2,3],[5,4])
a.dump

(
~pb = Pbind(
 \instrument, \lead2,
 \freq, 200,
 \rq, 0.1,
 \dur, 1
)
)

(
~my = {
var res = List.new;
~pb.patternpairs.pairsDo { arg key, val;
	res.add(key); res.add(val)
};
Pbind(*res)
}
)
~pb2 = ~my.(~pb);
~pb2.play
~pb.play
~pb[\freq]

Ref(Env([1,2,3],[5,4])).class == Ref


(
a = Pproto({
    ~bus = (type: \controlBus).yield;
	~bus.dump;
},
    Ppar([
		Pbind(*[
			instrument:    \default,
			freq: Pfunc { arg ev; var bus = Bus.new(\control,ev[\bus][\out],1,s); bus.asMap },
			dur:        1,
		])
	])
);
a.trace.play;
)
s.boot

(
a = Pfset({
    ~bus = Bus.control(s,1);
	~bus.debug("bus created");
	~bus.set(300);
},
    Ppar([
		Pbind(*[
			instrument:    \default,
			freq: Pfunc { arg ev; ev[\bus].asMap },
			dur:        1,
		])
	]),
{
	~bus.debug("cleanup");
	~bus.free;
}
);
a.trace.play;
)

(
~pb = ~penvcontrol.(Pbind(
 \instrument, \default,
 \freq, Ref(Env([100,400,500],[1,0.5],0)),
 \amp, Ref(Env([0.1,1,0.1],[2,2],0)),
 \sustain, 1,
 //\amp, 0.5,
 \dur, Pn(1,2)
));
~pb2 = Ppar([
	Pn(~pb,inf),
	Pbind(
		\freq, 800,
		\sustain, 0.1,
		\dur, 2,
	)
	
]);
~pbc = ~pb2.trace.play
)
~pbc.stop
~pb.play

thisThread.clock

"ljk".debug
(
~penvcontrol = { arg pat;
	var buskeydict = Dictionary.new;
	var respat = List.new;
	var ctlpatlist = List.new;
	var makebusmap;

	"pcontrol start".debug;
	makebusmap = { arg key;
		Pfunc { arg ev; [key, ev[key]].debug("pfunc"); ev[key].asMap }
	};
	
	pat.patternpairs.pairsDo { arg key,val;
		var buskey;
		var env;
		var cbus;
		var ctlpat;
		if(val.class == Ref) {
			buskey = "bus_" ++ key;
			respat.add(key);
			respat.add(makebusmap.(buskey));
			env = val.value;
			buskeydict[buskey] = env.levels[0];
			cbus.set(env.levels[0]);
			ctlpat = Pbind(
				\instrument, \ctlPoint,
				\value, Pseq(env.levels[1..],inf),
				\time, Pseq(env.times,inf) / Pfunc({thisThread.clock.tempo}),
				\mykey, buskey,
				\group, Pfunc { arg ev; ev[\busgroup] },
				\outbus, Pfunc { arg ev; buskey.debug("ctlpat!!"); ev[buskey].index },
				\curve, env.curves,
				\dur, Pseq(env.times,inf)
			);
			ctlpatlist.add(ctlpat);
		} {
			respat.add(key);
			respat.add(val);
		}
	};

	respat.debug("respat");

	Pfset({
			buskeydict.debug("penvcontrol init pfset");
			buskeydict.keysValuesDo { arg key, val;
				currentEnvironment[key] = Bus.control(s, 1);
				currentEnvironment[key].set(val);
			};
			//if( currentEnvironment[\busgroup].isNil ) {
				currentEnvironment[\busgroup] = Group.new;
			//};
		},
		Pfpar([Pbind(*respat)] ++ ctlpatlist),
		{
			buskeydict.debug("penvcontrol cleanup pfset");
			buskeydict.keysValuesDo { arg key, val;
				currentEnvironment[key].free;
			};
			currentEnvironment[\busgroup].freeAll;
			currentEnvironment[\busgroup].free;
			//currentEnvironment[\busgroup] = nil;
		}
	)
};
)

currentEnvironment
(
~pcontrol2 = { arg pat;
	"plop".postln;
	Pspawner({ |spawner|
		var blist = List.new;
		var respat = List.new;
		var ctlpatlist = List.new;
		var ctlpat;
		var cbus;
		var env;
		var clock = TempoClock.default;
		var str;

		"pcontrol start".debug;
		
		pat.patternpairs.pairsDo { arg key,val;
			if(val.class == Ref) {
				respat.add(key);
				cbus = Bus.control(s,1);
				blist.add(cbus);
				respat.add(cbus.asMap);
				env = val.value;
				cbus.set(env.levels[0]);
				ctlpat = Pbind(
					\instrument, \ctlPoint,
					\value, Pseq(env.levels[1..],inf),
					\time, Pseq(env.times,inf) / Pfunc({clock.tempo}),
					\outbus, cbus,
					\curve, env.curves,
					\dur, Pseq(env.times,inf)
				);
				ctlpatlist.add(ctlpat);
			} {
				respat.add(key);
				respat.add(val);
			}
		};
		respat.debug("respat");

		str = CleanupStream(Pbind(*respat).asStream, {
			"cleanup".debug;
			//spawner.suspendAll;
			blist.do(_.free);
		});
		spawner.par(str);
		ctlpatlist.do { arg pat;
			spawner.par(pat);
		};
	});
};
)

(
SynthDef(\ctlPoint, { |outbus, value, time, curve|
       var     start = In.kr(outbus, 1);
       ReplaceOut.kr(outbus, EnvGen.kr(Env([start, value], [time], curve), doneAction: 2));
}).add;
)

~busfreq = Bus.control(s);
~busrq = Bus.control(s);
(
Ppar([
	Pbind(
		\instrument, \lead2,
		\freq, ~busfreq.asMap,
		\rq, ~busrq.asMap,
		\amp, 0.5,
		\dur, 1
	),
	Pbind(
		\instrument, \ctlPoint,
		\outbus, ~busfreq,
		\value, Pseq([100,200,300],inf),
		\curve, 0,
		\time, Pfunc({arg ev; ev[\dur] / TempoClock.default.tempo }),
		\dur, Pseq([1,0.5],inf),
	),
	Pbind(
		\instrument, \ctlPoint,
		\outbus, ~busrq,
		\value, Pseq([1,2,3]/2,inf),
		\curve, 0,
		\time, Pfunc({arg ev; ev[\dur] / TempoClock.default.tempo }),
		\dur, Pseq([5,4],inf),
	),
]).play
)


