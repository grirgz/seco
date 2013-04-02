s.boot
(
SynthDef(\sin1, { arg out=0, pan=0, gate=1, sustain=0.5, amp=0.1, freq=200;
	
var ou;

	ou = Pulse.ar(freq);
	ou = ou * EnvGen.ar(Env.linen(0.01,sustain,0.01,1),doneAction:2);
	ou = Pan2.ar(ou,pan,amp);
	Out.ar(out, ou);

}).add;

SynthDef(\echo, { arg out=0, in=0, maxdtime=0.6, release=1, dtime=0.2, decay=2, gate=1;
        var env, ou;
        env = Linen.kr(gate, 0.05, 1, decay, doneAction:14);
        in = In.ar(in, 2);
		ou = CombL.ar(in, maxdtime, dtime, decay, 1, in);
        Out.ar(out, ou);
}, [\ir, \ir, \ir, 0.1, 0.1, 0]).add;

SynthDef(\addbeeps, { arg out=0, in, gate=1;
        var env;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        XOut.ar(out, env, (In.ar(in, 2) + (LFPulse.kr(1, 0, 0.3) * SinOsc.ar(1000) * 0.4) ));
}, [\ir, 0.1]).add;
)


(
// this function take a pattern and a list of Pmono pattern as effects
~pfx2 = { arg pat, effects;
	Pspawner({ |spawner|
		var str, pbus, pgroup, leffect;
		var blist = List.new;
		// create a bus and set the pat to write on it
		pbus = Bus.audio(s,2).debug("first bus");
		blist.add(pbus);
		pat = Pset(\out, pbus, pat);
		// when the pattern end, free all the effects
		str = CleanupStream(pat.asStream, {
			"cleanup".debug;
			spawner.suspendAll;
			//pbus.free;
			//glist.do(_.free);
		});
		spawner.par(str);
		pgroup = 1;
		// for each effect, set \in to read from the previous effect and \out to write to the next effect
		// and create a group to maintain order-of-execution
		// then launch it in parralel with the pat
		effects[..effects.size-2].do { arg ef;
			ef = Pset(\in, pbus, ef);
			pbus = Bus.audio(s,2);
			blist.add(pbus);
			ef = Pset(\out, pbus, ef);
			pgroup = Group.after(pgroup);
			ef = Pset(\group, pgroup, ef);
			spawner.par(ef)
		};
		// the last effect should write on bus 0 so don't set its \out
		leffect = effects.last;
		leffect = Pset(\in, pbus, leffect);
		pgroup = Group.after(pgroup);
		pgroup.register;
		pgroup.addDependant( { arg grp, status;
			if(status == \n_end) {
				"fin!!".debug;
				blist.do(_.free)
			}
		});
		leffect = Pset(\group, pgroup, leffect);
		spawner.par(leffect)
	});
};
)


(
x = ~pfx2.(
	Pbind(
		\type, \note,
		\instrument, \sin1,
		\amp, 0.05,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4])*100,
		\dur, 0.1
	),
	[
	Pmono(
		\echo,
		\release, 1,
		\decay, 0.51,
		\dtime, 0.1
	)
	,
	Pmono(
		\echo,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 1,
		\dtime, 0.51
	)
	]
);

Pseq([x,x,x]).play;
)

s.queryAllNodes;
s.nodeTree
s.plotTree

Bus.freeAll


~free_all_buses = {
	var bus;
	200.do { arg i;
		bus = Bus.new (rate: 'audio', index: i, numChannels: 2, server:s);
		bus.free
	}
};
~free_all_buses.value




g = Group.new(1)
g.register
g.addDependant({arg aa, bb; [aa,bb].debug });
g.free
g.onFree( { "plop".debug } );
g
w = NodeWatcher.register(g,true)
g.isPlaying
g.free
w.respond({"plop".debug}, \n_end)
s.queryAllNodes;

(
x = ~pfx2.(
	Pbind(
		\type, \note,
		\instrument, \sin1,
		\amp, 0.05,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4])*100,
		\dur, 0.5
	),
	[
	Pmono(
		\echo,
		\release, 1,
		\decay, 1.81,
		\dtime, 0.152
	)
	]
);

y = ~pfx2.(
	Ppar([
		Pseq([x,x,x]),
		Pbind(
			\type, \note,
			\instrument, \sin2,
			\amp, 0.05,
			\sustain, 0.2,
			\freq, Pseq([4,1,2,4])*050,
			\dur, 0.7
		)
	]),

	[
	Pmono(
		\echo,
		\release, 1,
		\decay, 5.81,
		\dtime, 0.752
	),
	Pmono(
		\echo,
		\release, 1,
		\decay, 2.81,
		\dtime, 0.152
	)
	]
);
y.play;

)



(
SynthDef(\echo, { arg out=0, in, maxdtime=0.2, dtime=0.2, decay=2, gate=1;
        var env, in;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        in = In.ar(in, 2);
        XOut.ar(out, env, CombL.ar(in * env, maxdtime, dtime, decay, 1, in));
}, [\ir, \ir, 0.1, 0.1, 0]).add;

SynthDef(\addbeeps, { arg out=0, gate=1;
        var env;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        XOut.ar(out, env, (In.ar(out, 2) + (LFPulse.kr(1, 0, 0.3) *
SinOsc.ar(1000) * 0.4) ));
}, [\ir, 0.1]).add;
)

First run this :

(
~a = EventPatternProxy.new;
~b = EventPatternProxy.new;
~p = Pbind(\degree, Pseq([0,1,2,3,4,5],inf), \dur, 0.3, \legato, 0.2);
~q = Pbind(\degree, Pseq([0,1,2,3,4,5]-2,inf), \dur, 0.6, \legato, 0.1, \amp, 0.5);
)

Then..

(
~a.source = Pfxb(~p, \echo, \pregain, 180, \amp, 0.25);
~b.source = Pfxb(~q, \addbeeps, \pregain, 180, \amp, 0.25);
~a.play;
~b.play;
) 
(
~a.source = ~pfx2.(~p, [Pmono(\echo, \pregain, 180, \amp, 0.25)]);
~b.source = ~pfx2.(~q, [Pmono(\addbeeps, \pregain, 180, \amp, 0.25)]);
~a.play;
~b.play;
) 


oooooooooooo
oo



















(
SynthDef(\dsn_pulse, {
       arg out,freq=440,min=(-1),max=1,amp=1;
       var sig;

       sig = Pulse.ar(freq,2,1) * amp;
	   //sig = SinOsc.ar(200);

       Out.ar(out,sig);
}).add;

SynthDef(\dsn_lpf, {
       arg out,freq=10000;
       var sig;

       sig = LPF.ar(\in.ar(0),freq);

       Out.ar(out,sig);
}).add;

SynthDef(\dsn_lfsine, {
       arg out,lfreq=2,min=(-1),max=1,amp=1;
       var sig;

       sig = SinOsc.ar(lfreq,0,max-min,min) * amp;

       Out.ar(out,sig);
}).add;

p = ProxySpace.new;
)


// I guess this should be evaluated line for line
p[\pulse1].setn(\freq,220);
p[\pulse1] = \dsn_pulse;

p[\lpf1].setn(\freq,100);
p[\lpf1] = \dsn_lpf;

p[\lfsine1].setn(\freq,10,\min,220,\max,1000);
p[\lfsine1] = \dsn_lfsine;

p[\lpf1].map(\in,p[\pulse1])
p[\lpf1].map(\freq,p[\lfsine1])
p[\lpf1].unmap(\freq)


p[\lpf1].play
p[\pulse1].play



{a=SinOsc.ar(_);b=LFPulse.ar(_);f=Stepper.ar(b.(93+(124*a.(1.2))));a.(400!2/f,a.(11)).tanh*Mux2.ar(b.(5,pi/5),b.(7,pi/7),b.(0.2)).lag}.play



















(
~initBusesAndGroups = {
       ~synthGroup = Group.new(s);
       ~effectGroup = Group.after(~synthGroup);
       ~reverbBus = Bus.audio(s, 2);
       ~vocoderBus = Bus.audio(s, 2);
       ~masterBus = Bus.audio(s, 2);
};
)
(
SynthDef(\wind, { arg amp = 1, gate = 1, outBus = 0, panGate = 1;
       var env, osc, out, modosc, modamp;

       env = EnvGen.kr(
               Env(
                       levels: [0, 0.5, 0.5, 0.01],
                       times: [5, 0.1, 10],
                       releaseNode: 1),
               gate: gate,
               doneAction: 2);

       osc = BrownNoise.ar();
       modosc = LFNoise1.kr(1/LFNoise0.kr(0.5,mul:2.5, add:7.5)).range(150, 700);

       modamp = LFNoise1.kr(1/LFNoise0.kr(0.5).range(5, 10)).range(0.0, 1.0);

       out = HPF.ar(osc, 300);
       out = LPF.ar(out, modosc*1.2);
       out = Resonz.ar(out, modosc, 0.03);
       out =  Pan2.ar(out*env*amp*modamp, SinOsc.kr(Rand(1/20, 1/10)));

       Out.ar(outBus, out*0.5);
}).add;

SynthDef(\dark_wind, { arg outBus = 0;
       var ampEnv, filterEnv, out;

       ampEnv = EnvGen.kr(Env(
               levels: [0, 0.3, 0.5, 1, 0.3, 1, 0.2, 0.9, 0.05, 0],
               times:    [20, 5, 0.6, 10, 3, 10, 2, 20, 30]),
               doneAction: 2);

       //filterEnvs tider är anpassade till ampEnv
       filterEnv = EnvGen.kr(Env(
               levels: [200, 200, 5000, 1000, 3000, 400, 2000, 200],
               times: [25, 2, 10, 3, 10, 2, 10]));

       out = Resonz.ar(LPF.ar(BrownNoise.ar(), filterEnv), 40, 0.5);
       out = HPF.ar(out, 25);
       Out.ar(outBus, Pan2.ar(out)*ampEnv*0.5);
}).add;

SynthDef(\bird, { arg amp = 1, outBus = 0;
       var ampenv, freqenv, out, startfreq = Rand(1500, 2000), pan, length,
       direction;

       length = Rand(3, 6);

       direction = [Line.kr(Rand(-0.9, -0.1), 1, length),
               Line.kr(Rand(0.9, 0.1), -1, length),
               Rand(-0.5, 0.5)];

       ampenv = EnvGen.kr(
               Env( levels: [0.0001, Rand(0.5, 0.7), 0.4, 0.01]*0.6,
                       times: [length/2, 0.2, length/2],
                       curve: 'exponential'),
               doneAction: 2);

       freqenv = EnvGen.kr(
               Env( levels: [startfreq, startfreq*Rand(0.9, 0.7), startfreq,
startfreq*0.75],
                       times: [0.6, 0.08, 3],
                       curve: 'linear',
                       loopNode: 0,
                       releaseNode: 2
               ),
               timeScale: Rand(0.8, 1)
       );

       pan = Select.kr(Rand(0, 2).round(1),direction);

       out = Resonz.ar(Saw.ar(freqenv)*0.2, freqenv, Rand(0.05, 0.3));
       //out = MidEQ.ar(out, freq: 700, rq: 0.2, db: (freqenv/startfreq)*10);
	   //out = Saw.ar(freqenv)*0.2;

       Out.ar(outBus, Pan2.ar(out*(ampenv-0.0001)*amp, pan));
}).add;

SynthDef(\reverb, { arg inBus = 100, outBus = 0;
       var out, signal;
       signal = In.ar(inBus, 2);
       out = FreeVerb2.ar(signal[0], signal[1],
               mix: 0.2,
               room: 0.9,
               damp: 0.6);
       XOut.ar(outBus, xfade: 0.5, channelsArray: out);
}).add;

SynthDef(\master, { arg gate1 = 1, gate2 = 1, inBus = 50;
       var filterEnv, ampEnv, out, releaseEnv;

       filterEnv = EnvGen.ar(Env(
               levels: [400, 500, 1000, 20000],
               times: [30, 20, 5],
               releaseNode: 1),
               gate: gate1);

       ampEnv = EnvGen.kr(Env(
               levels: [0, 0, 1],
               times: [25, 30]));

       releaseEnv = EnvGen.kr(Env(
               levels: [1.0, 0.0],
               times: [10],
               releaseNode: 0),
               gate: gate2,
               doneAction: 2);

       out = LPF.ar(In.ar(inBus, 2), filterEnv)*ampEnv;

       Out.ar(0, out);
}).add;
)

~initBusesAndGroups.value();

(
Task({
       ~master = Synth(\master, [\inBus, ~masterBus], target: ~effectsGroup,
addAction: \addAfter);
       Synth(\reverb, [\outBus, ~masterBus, \inBus, ~reverbBus], target:
~effectGroup);
       //Synth(\dark_wind);
       1.wait;
       ~master.set(\gate1, 0);
       1.do {
               //Synth(\wind, [\outBus, ~reverbBus], target: ~synthGroup);
               1.wait;
       };
       ~birds = Task({
               inf.do {
                       rrand(0.4, 10).wait;
                       exprand(1, 5).do {
                               Synth(\bird, [\outBus, ~reverbBus], target: ~synthGroup);
                               rrand(0.2, 0.7).wait;
                       }
               };
       }).play;
}).play;
)









(
SynthDef(\noise, { arg out=0, pan=0, amp=0.1, gate=1, freq=200, rq=0.1, nfreq=10;
	var bou, ou, env, dtrig, benv;
	bou = Mix.fill(5, {
		dtrig = Dust.ar(50);
		env = EnvGen.ar(Env.linen(0.01,0.01,0.01), gate:dtrig);
		ou = WhiteNoise.ar(1);
		rq = LFNoise1.ar(nfreq).range(rq*0.9,rq*1.1);
		freq = LFNoise1.ar(nfreq+0.1).range(freq*0.9,freq*1.1);
		ou = BPF.ar(ou, freq, rq);
		ou = ou * env;
	});
	bou = bou * EnvGen.ar(Env.adsr(0.1,0.1,1,0.1), gate, doneAction:2);
	bou = Pan2.ar(bou, pan, amp);
	Out.ar(out,bou);

}).add
)

(
Pdef(\a, Pbind(
	\instrument, \noise,
	\rq, 0.051,
	\nfreq, 100,
	\degree, Pseq([[1,3],[2,4], 5, 7, Prand([19,17,20])],inf),
	\octave, 4,
	\dur, Pseq([0.1, 0.2, 0.05],inf)
)).play
)




	z = Array.newClear(128);	
	w = Window.new;
	c = Slider(w,Rect(0,0,100,30));
	c.keyDownAction = { arg view,char,modifiers,unicode,keycode;
		[char, modifiers, unicode, keycode].postln;
		};		
	w.front;

	GUI.swing

(
	r.remove;
	r = CCResponder({ arg a,b,x,d;
		
		AppClock.sched(0.0,{ 
				["AppClock has been playing for ",a,b,x,d].postln;
				c.value = d/127;
		});
		"blliiaaa".postln;
	});
)

b = (bla:788);
b.bla = 44
b.bla = 777
b

a = Dictionary.new
a[[\knob, 0]] = 7
a[[\knob, 0]]

a[b] = 4
a[b]


Array.interpolation(5, 3.2, 20.5);



a = [1, 4, 1, 6, 4, 5, 2, 7, 8, 4];
b = a.copy;
a.sort;
c = List.new;
a.size.do{ |i|
       c.add(Array.interpolation(8, a[i], b[i]));
};
c.asArray;
c.flop.printAll
