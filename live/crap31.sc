
(
~pmonopar = { arg pat, par;

	Pfset({
			currentEnvironment[\monogroup] = Group.new;
		},
		Ppar([Pset(\group, Pkey(\monogroup), pat), Pbind(\type, \set, \id, Pkey(\monogroup)) <> par]),
		{
			currentEnvironment[\monogroup].free;
		}
	)

};
)

(
SynthDef(\acid, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, ffreq=400, rq=0.1;
	var ou;
	ou = LFSaw.ar(freq);
	ou = RLPF.ar(ou, ffreq, rq);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)

(
~pmonopar.(
	Pbind(
		\instrument, \acid,
		\freq, 300,
		\legato, 0.8,
		\dur, 2,
	),
		Pbind(
			\args, [\ffreq],
			\ffreq, Pseq([100,1200],inf),
			\dur, 0.5,
		),
).play;

)
(
~pmonopar.(
	Pbind(
		\instrument, \acid,
		\freq, 300,
		\legato, 0.1,
		\dur, 4,
	),
	Ppar([
		Pbind(
			\args, [\ffreq],
			\ffreq, Pseq([100,1200],inf),
			\dur, 0.5,
		),
		Pbind(
			\args, [\rq],
			\rq, Pseq([0.1,1.7],inf),
			\dur, 1.7,
		)
	])
).play;

)

~pmonopar = { arg pat, par;
	var buskeydict = Dictionary.new;
	var respat = List.new;
	var ctlpatlist = List.new;
	var pbindpat;
	var makebusmap;

	"pcontrol start".debug;
	makebusmap = { arg key;
		Pfunc { arg ev; [key, ev[key]].debug("pfunc"); ev[key].asMap }
	};
	
	if(pat.class == EventPatternProxy) {
		pbindpat = pat.source;
	} {
		pbindpat = pat
	};

	pbindpat.patternpairs.pairsDo { arg key,val;
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
				\group, Pkey(\busgroup),
				\outbus, Pfunc { arg ev; ev[buskey].index },
				\curve, env.curves,
				\dur, Pseq(env.times,inf)
			);
			ctlpatlist.add(ctlpat);
		} 
	};

	respat.debug("respat");

	Pfset({
			buskeydict.debug("penvcontrol init pfset");
			buskeydict.keysValuesDo { arg key, val;
				currentEnvironment[key] = Bus.control(s, 1);
				currentEnvironment[key].set(val);
			};
			currentEnvironment[\busgroup] = Group.new;
		},
		Pfpar(
			[
				if(chain.notNil) {
					chain <> Pbind(*respat) <> pat;
				} {
					Pbind(*respat) <> pat;
				}
			]
			++ ctlpatlist
		),
		{
			buskeydict.debug("penvcontrol cleanup pfset");
			buskeydict.keysValuesDo { arg key, val;
				currentEnvironment[key].free;
			};
			currentEnvironment[\busgroup].freeAll;
			currentEnvironment[\busgroup].free;
		}
	)
};
