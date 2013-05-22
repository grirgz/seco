DynKlank



(
SynthDef(\kicklank2, {  arg gate=1;
	var sig;
	var ex;
	var imp;
	var osc;
	var freqenv;
	var ffreq;
	var center_ffreq;
	var env;
	imp = Impulse.ar(1, 0, 0.1);
	ex = WhiteNoise.ar(0.001);
	ex = PinkNoise.ar(0.002);
	ex = Impulse.ar(1/2, 0, 0.1);
	ex = Trig.ar(gate, 0.0001);

	osc = 0;
	osc = LFPulse.ar(XLine.kr(300,60,0.01));
	osc = osc * 0.7;
	//ex = ex + osc;
	ex = ex;
	freqenv = EnvGen.kr(Env([40.5,40.0001,5],[0.1,0.1]), gate);
	freqenv = 1;
	//ex = ex * EnvGen.ar(Env.linen(0.0001,0.01,0.1),imp);
	sig = DynKlank.ar(`[[103, 102,71, 73, 53,30,150], nil, [2, 1, 1.5,3, 1,3]/1], 
		//Impulse.ar(1, 0, 0.1), 
		ex,
		1,
		//LFNoise1.ar(15).range(0.1,49) ,
		freqenv,
		//LFNoise1.ar(15).range(-0.1,1)*4 ,
		//1, 
	);
	//sig = sig * EnvGen.ar(Env([0,1,0],[0.00001,1.5], [0,-8]),DelayN.ar(ex, 0.3,0.0001), doneAction:2);
	//sig = sig + (osc * 80);
	//sig = sig.clip(0.1) + sig;
	//sig = sig + sig.tanh;
	sig = [sig/1];
	//sig = [osc];
	//sig = sig.tanh * 4;
	//sig = sig * 47;
	//sig = sig * 477;
	ffreq = 0;
	center_ffreq = 1;
	ffreq = ((0..4).normalize*2-1).mirror * center_ffreq * ffreq/2 + (ffreq/2 + 30);
	ffreq.debug("ffreq");
	ffreq = 1790;
	//sig = LPF.ar(sig, [1100,400,4100,100,4400,590,1100]);
	//sig = [sig.distort, sig.tanh];
	//sig = sig.distort; sig = sig * 7;
    //env = Env([-1, -0.1, -0.3, 0.1, -0.9,1], [ 0.4666, 0.1, 0.66, 0.5,0.1668 ].normalize, \lin, -1.0);
    env = Env([-1, -0.7, 0.7,1], [ 0.7666, 7.06, 0.7668 ].normalize, \exp, -1.0);
    sig = IEnvGen.ar(env, sig); sig = sig * 77;
	sig = LPF.ar(sig, ffreq, 0.1); sig = sig ;
	//sig = sig.clip2(0.51);
	//sig = sig.tanh;
	//sig = sig * 7;
	//sig = LPF.ar(sig, XLine.kr(1000,30,0.2));
	sig = sig * 0.05;
	sig = sig * EnvGen.ar(Env([0,1,0],[0.00001,0.8], [0,-8]),gate, doneAction:2);
	//sig = osc;
	//sig = sig ! 2;
	sig = sig.flat;
	sig = Splay.ar(sig, 1, 1, 0);
	Out.ar(0, sig)
}).add;
)


(
{
    var sin = SinOsc.ar(440, 0, MouseX.kr(0, 1));
    // use offset so negative values of SinOsc will map into the Env
    var env = Env([-1, -0.7, 0.7, 1], [ 0.8666, 0.2666, 0.8668 ], \lin, -1.0);
    IEnvGen.ar(env, sin) * 0.1
}.play;
)



Array.series(8,1,-0.30+(1/90))
1/9
((0..3).normalize*2-1).mirror * -1 * 500 + 600
((0..7)/3.5)
((0..7).normalize*2)

(
SynthDef(\kick1, { |out=0, amp=0.1, pan=0|
	var env0, env1, env1m, son;
	
	env0 =  EnvGen.ar(Env.new([0.5, 1, 0.5, 0], [0.005, 0.06, 0.26], [-4, -2, -4]), doneAction:2);
	env1 = EnvGen.ar(Env.new([110, 59, 29], [0.005, 0.29], [-4, -5]));
	env1m = env1.midicps;
	
	son = 0;
	//son = LFPulse.ar(env1m, 0, 0.5, 1, -0.5);
	//son = son + WhiteNoise.ar(1);
	//son = LPF.ar(son, env1m*1.5, env0);
	son = son + SinOsc.ar(env1m, 0.5, env0);
	
	son = son * 1.2;
	//son = son.clip2(1);
	
	OffsetOut.ar(out, Pan2.ar(son * amp));
}).store;
)


(
Pdef(\plop, Pbind(
	\instrument, \kick1,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 0.1
)).play;
);

(
Pdef(\plop2, Pbind(
	\instrument, \kicklank2,
	\degree, Pseq([0],inf),
	\dur, Pseq([Pn(1/2,7),Pn(1/4,2)],inf),
	\amp, 0.1
)).play;
);
