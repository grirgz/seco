Server.default = s = Server.internal; s.boot;

b = Buffer.alloc(s, 512, 1, { |buf| buf.chebyMsg([1,0,1,1,0,1])});

(
{
        Shaper.ar(
                b,
                SinOsc.ar(300, 0, Line.kr(0,1,6)),
                0.5
        )
}.scope;
)

b.free;



Env.new([-1,0,1],[0.1,0.1],[2,-2]).plot;
Env.new([-1,0,1],[0.1,0.1],[8,-8]).plot;


69.midicps

KeyResponder

(
c = NoteOnResponder({ |src,chan,note,vel|
                [src,chan,note,note.midicps,vel].postln;
});
c.learn; // wait for the first note
)
NoteOnResponder.removeAll

67.midicps / 60.midicps

(60.midicps * (1+(1/2))).cpsmidi

{ LFNoise1.ar(1, 0.25,1) }.scope;

x = { MoogFF.ar(Pulse.ar([121.1,121,120.9]), MouseY.kr(100, 10000, 1), MouseX.kr(0, 4)).sum ! 2 }.play(s);
x = { BPF.ar(Pulse.ar([121.1,121,120.09]), MouseY.kr(100, 10000, 1)+LFNoise1.ar(4,100,101), MouseX.kr(0, 1)+LFNoise1.ar(1,0.1,0.11)).sum }.play(s);
x = { BPF.ar(Pulse.ar([121.1,121,120.09]), MouseY.kr(100, 10000, 1), MouseX.kr(0.0001, 1)).sum }.play(s);
x = { RLPF.ar(Pulse.ar([121.1,121,120.09]), MouseY.kr(100, 10000, 1), MouseX.kr(0.0001, 1)).sum }.play(s);

(
x = { 
	var ou = RLPF.ar(Pulse.ar([121.1,121,120.09]), MouseY.kr(100, 10000, 1), MouseX.kr([0.0001,0.5], 1)).sum;
	ou = Limiter.ar(ou);
	ou = ou ! 2
}.play(s);
)

AllPass0
CombL

(
{
        z = WhiteNoise.ar(0.2);
        z + AllpassN.ar(z, 1, XLine.kr(0.0001, 0.01, 20), 0.2)
}.play
)

(
SynthDef("aSynth",{
 arg lagLev = 0.2, freq= 440, cutoff = 500, gate = 0.5;
 var osc1 = Mix.fill(8, { SinOsc.ar(freq.lag(lagLev) + (freq*0.01.rand), 0, 0.05) });
 var filterEnv = EnvGen.ar(Env.adsr(0.02, 0.1, 0.05, 1), gate, doneAction:2);
 var filterOutput = MoogFF.ar(osc1, cutoff * filterEnv, 3.3);    Out.ar(0, filterOutput); 


}).store
)

(
PmonoArtic("aSynth",
 \freq, Pseq([440,330,440,330,550,770,880], inf),
 \legato, Pwrand(#[0.5, 1.0], #[0.1, 0.9], inf),
 \dur, 0.3,
 \cutoff, Pwhite(5000, 10000, inf)



).play
)


(
Ndef(\bla,{
	arg freq=200, rq=0.1;
	var ou;
	//ou = WhiteNoise.ar(1);
	ou = PinkNoise.ar(1);
	ou = BPF.ar(ou, freq*[1.0146,0.9987,1], rq);

}).play
)
Ndef(\bla).edit

(
Ndef(\blax,{
arg freq=250, ifreq=(1/2);
var gen, genenv, ou, ou2;
//ou = Klang.ar(`[({100.rand + 10}!10)+freq,1,0]);
//ou2 = WhiteNoise.ar(1);
gen = Pulse.ar([1.01,1,0.996998]*freq);
genenv = gen * EnvGen.ar(Env.perc(0.001,0.9),gate:Impulse.ar(ifreq));
ou = genenv;
//ou = AllpassL.ar(ou, 2, freq.reciprocal,0.01);
ou = AllpassL.ar(ou, 2, (freq*0.99).reciprocal,1.51);
ou = AllpassL.ar(ou, 2, (freq*1.09).reciprocal,1.51);
//ou = AllpassL.ar(ou, 2, 0.02,0.1) + ou;
//ou = AllpassL.ar(ou, 2, 0.001,2.5);
//ou = AllpassL.ar(ou, 2, 0.01,1.5);
//ou = AllpassL.ar(ou, 2, 0.051,4.5);
ou = ou.sum;
ou2 = BPF.ar(genenv, freq,10.10);
//ou2 = 0;
ou = BPF.ar(ou, freq*2,1);
//ou = 0;
ou = ou + ou2;

ou = ou ! 2;

}).play
)
(
Ndef(\blax,{
arg freq=250;
var gen, genenv, ou, ou2;



ou = ou ! 2;

}).play
)
s.boot
s.queryAllNodes
