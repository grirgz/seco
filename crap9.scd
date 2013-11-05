(
ControlSpec.specs.addAll([\latchlfo -> ControlSpec(0.1, 24, \lin, 0.1, 6, "
latchlfo")]);

SynthDef("Latch", { arg lowfreq=10, latchlfo = 0.1;
        var trig, env, out;
trig = Impulse.kr(lowfreq);
env = Linen.kr(trig, 0, 1, 1/lowfreq);
out = SinOsc.ar(Latch.kr(LFSaw.kr(latchlfo, 0, [500, 250], 600), trig), mul:
env);
Out.ar(0, out)
}).add;

SynthDescLib.global[\Latch].makeWindow;
)

=====

(
SynthDef("SH_Variations",
{
// out is included as an argument so you
// can switch the output to other tracks if
// in Logic (if using Soundflower).
arg speed=6, out=0;
var env, trig, set, setSize=10, mix;
set = Array.fill(setSize, {rrand(1.1, 2.7)}).round(0.001).post;
trig = Impulse.kr(speed);
env = Linen.kr(trig, 0, 1, 1/speed);
c = Latch.kr(
                LFSaw.kr(Demand.kr(Impulse.kr(1/5), 0, Drand(set, inf)),
                0, 0.5, 0.5),
                trig);
f = (c*48+24).round(1).midicps;
// Tone down the ratio
r = LFNoise1.kr(1/5, 2.0, 3.0).round(0.1);
mix = PMOsc.ar(f, f*r, c*5+6 * env, mul: env);
// add an on/off decision + amplitude variation
mix = mix * max(0, TRand.kr(-1.0, 1.0, trig));
// Kanon
// mix = [mix, CombL.ar(mix, 2, 2)]
// Triple Kanon
// mix = FreeVerb.ar(mix, LFNoise0.kr(1/3, 0.3, 0.3), 0.6);
mix=Pan2.ar(mix, -1) + Pan2.ar(CombL.ar(mix, 2, 2), 0) +
Pan2.ar(CombL.ar(mix, 4, 4), 1);
Out.ar(out, mix*0.3)
}).add
)

SynthDescLib.global[\SH_Variations].makeWindow

=======


(
SynthDef("SH_PMv",
{|speed = 9|
var freq, latchrate, index, ratio, env, mix;
speed = 9;
latchrate = speed*LFNoise0.kr(1/10, mul: 0.03, add: 1.6);
index = Latch.kr(
        LFSaw.kr(latchrate, mul: 5, add: 6),
        Impulse.kr(speed)
        );
freq = Latch.kr(
        LFSaw.kr(latchrate,
                mul: max(0, LFNoise1.kr(1/5, 24, 10)),
                add: LFNoise0.kr(1/7, 12, 60)),
        Impulse.kr(speed)
        ).round(1).midicps;
ratio = LFNoise1.kr(1/10, mul: 2.0, add: 5.0);

env = EnvGen.kr(Env.perc(0, LFNoise0.kr(speed, 1, 1.5)/speed),
Impulse.kr(speed), LFNoise1.kr([5, 5], 2, 1).max(0).min(0.8));
mix = PMOsc.ar(
        [freq, freq * 1.5],
        freq*ratio, index, mul: env);
                Out.ar(0, FreeVerb2.ar(mix.at(0), mix.at(1), 0.2, 0.6))
}).add;

SynthDescLib.global[\SH_PMv].makeWindow;
)



// where did I leave my keys?

(

var n = 30, m = 5;

Ndef(\x).clear(3);

Ndef(\y, { Splay.ar(Ndef.ar(\x, m, LFNoise1.kr(1/m).range(0, n - m - 1))) }).play; 

Ndef(\x, { 

	{ 

		var a = Ndef.ar(\x, 1, LFNoise0.kr(Rand()) * Rand() + Rand() * n);

		a + 0.1 * SinOsc.ar(ExpRand(0.2, 1000)) 

	} ! n

}); 

)



// where did I put my keys?

(

var n = 30, m = 5;

Ndef(\x).clear(3);

Ndef(\y, { Splay.ar(Ndef.ar(\x, m, LFNoise1.kr(1/m).range(0, n - m - 1))) }).play; 

Ndef(\x, { 

	{ 

		var a = Ndef.ar(\x, 1, LFNoise0.kr(Rand()) * Rand() + Rand() * n);

		SinOsc.ar(ExpRand(0.2, 1000), a) 

	} ! n

}); 

)
