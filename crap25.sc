(
SynthDef(\ctlPoint, { |outbus, value, time, curve|
       var     start = In.kr(outbus, 1);
       ReplaceOut.kr(outbus, EnvGen.kr(Env([start, value], [time], curve), doneAction: 2));
}).store;

// can't Pchain the resulting pattern, so use the "chain" argument to chain the inner pattern
~penvcontrol = { arg pat, chain=nil;
	var buskeydict = Dictionary.new;
	var respat = List.new;
	var ctlpatlist = List.new;
	var pbindpat;
	var makebusmap;

	makebusmap = { arg key;
		Pfunc { arg ev; ev[key].asMap }
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

	Pfset({
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
			buskeydict.keysValuesDo { arg key, val;
				currentEnvironment[key].free;
			};
			currentEnvironment[\busgroup].freeAll;
			currentEnvironment[\busgroup].free;
		}
	)
};

)


(
~pb = ~penvcontrol.(Pbind(
 \instrument, \default,
 \freq, Ref(Env([100,400,500],[1,0.5],0)),
 \amp, Ref(Env([0.1,1,0.1],[2,2],0)),
 \sustain, 1,
 //\dur, 1,
 \dur, Pn(1,2) // use finite pattern to restart env in sync
));
//~pb.play
Pn(~pb).play

)

