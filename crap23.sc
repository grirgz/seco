
(
SynthDef(\modularLine, { |mod, index, synthDur, carrA, carrB, att, sus, rel|
var guts, env;
        guts = PMOsc.ar(Line.kr(carrA, carrB, synthDur, doneAction:2), mod, Line.kr(0.0, index,13, doneAction:2),0,0.1).tanh;
   env =  EnvGen.ar(Env.linen(att, sus, rel, 1, \cubed));                             
   Out.ar(0, guts*env !2);      
    }).add;

    SynthDef(\modularFlat, { |mod, index, synthDur, carrA, carrB, att, sus, rel|
var guts, env;
        guts = PMOsc.ar(carrA, mod, Line.kr(0.0, index,13, doneAction:2),0,0.1).tanh;
   env =  EnvGen.ar(Env.linen(att, sus, rel, 1, \cubed));                             
   Out.ar(0, guts*env!2);      
    }).add;
)

(
var func;
func = { |carrA, carrB, att=2, sus=5, rel=9|
var synthDur;
synthDur = att + sus + rel + 2;
 
r = Routine({
 	5.do({ 
 	a = Synth([\modularLine, \modularFlat].choose, [\carrA, 2000.0.rand, \carrB, 2000.0.rand, \mod, 800.0.rand, \index, 12.0.rand, \att, att, \sus, sus, \rel, rel, \synthDur, synthDur]);
5.wait;

b = Synth([\modularLine, \modularFlat].choose, [\carrA, 2000.0.rand, \carrB, 2000.0.rand, \mod, 800.0.rand, \index, 12.0.rand, \att, att, \sus, sus, \rel, rel, \synthDur, synthDur]);
5.wait;
c = Synth([\modularLine, \modularFlat].choose, [\carrA, 2000.0.rand, \carrB, 2000.0.rand, \mod, 800.0.rand, \index, 12.0.rand, \att, att, \sus, sus, \rel, rel, \synthDur, synthDur]);
5.wait;
        
        d = Synth([\modularLine, \modularFlat].choose, [\carrA, 2000.0.rand, \carrB, 2000.0.rand, \mod, 800.0.rand, \index, 12.0.rand, \att, att, \sus, sus, \rel, rel, \synthDur, synthDur]);
5.wait;
e = Synth([\modularLine, \modularFlat].choose, [\carrA, 2000.0.rand, \carrB, 2000.0.rand, \mod, 800.0.rand, \index, 12.0.rand, \att, att, \sus, sus, \rel, rel, \synthDur, synthDur]);
5.wait;
}); 
 	});
r.play;
};
func.value(2000.0.rand,2000.0.rand);
)

a.free;
b.free;
c.free;
d.free;
e.free; 
