
(

p = Pbind(\degree, Pseries(0, 1, 8), \dur, Plazy { Pn(rrand(0.08, 0.15), inf) }, \amp, 0.001);

~clocks = IdentitySet.new;

~run = { |pattern|
    var clock = TempoClock.new;
    ~clocks.add(clock);
    pattern.play(clock: clock);
};

55.do { ~run.(p) };

)

~clocks.do { |clock| clock.stop }; ~clocks.clear;

(
	p = Pbind(\degree, Pseries(0, 1, 8), \dur, Plazy { Pn(rrand(0.08, 0.15), inf) }, \amp, 0.001);

	~run = {

		var clock = TempoClock.new;
		Pspawner({ arg spawner;
			var slower = 0;
			10.do {
				slower = slower + 1;
			};
			100.do {
				spawner.par(p)
			};
		}).play(clock:clock, quant:1)

	};
	~run.();
)
(
	p = Pbind(\degree, Pseries(0, 1, 8), \dur, Plazy { Pn(rrand(0.10, 0.15), inf) }, \amp, 0.001);

	~run = {

		//var clock = TempoClock.new;
		var slower = 0;
		var iter = 5000000;
		["1", thisThread.clock.beats, thisThread.clock.elapsedBeats].postln;
		//["1", thisThread.clock.beats].postln;
		iter.do {
			slower = slower + 1;
		};
		["2", thisThread.clock.beats, thisThread.clock.elapsedBeats].postln;
		//["2", thisThread.clock.beats].postln;
		s.sync;
		Pspawner({ arg spawner;
			"play".postln;
			34.do {
				spawner.par(p)
			};
		//}).play(clock:clock, quant:1)
		}).play(quant:1);
		["3", thisThread.clock.beats, thisThread.clock.elapsedBeats].postln;
		//["3", thisThread.clock.beats].postln;

	};
	//~run.();
	Task( {  ~run.(); }).play(TempoClock.default)
)



167/8
21*8

(
	p = Pbind(\degree, Pseries(0, 1, 8), \dur, Plazy { Pn(rrand(0.10, 0.15), inf) }, \amp, 0.001);

	~run = {

		//var clock = TempoClock.new;
		var slower = 0;
		var iter = 5;
		iter.do {
			slower = slower + 1;
		};
		Pspawner({ arg spawner;
			"play".postln;
			550.do {
				spawner.par(p)
			};
		//}).play(clock:clock, quant:1)
		}).play(quant:1)

	};
	~run.();
)

(
	p = Pbind(\degree, Pseries(0, 1, 8), \dur, Plazy { Pn(rrand(0.08, 0.15), inf) }, \amp, 0.001);

	~run = {

		var clock = TempoClock.new;
		//var clock = TempoClock.default;
		Pspawner({ arg spawner;
			100.do {
				spawner.par(p)
			};
		}).play(clock:clock, quant:1)

	};
	~run.();
)
