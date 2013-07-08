     Ndef(\pat, { PanAz.ar(32, SinOscFB.ar(18, 1.4), SinOscFB.ar(0.2, 1.4), 1, 2) });
    Ndef(\mat, { x = Ndef.ar(\pat); Splay.ar(SinOscFB.ar({ exprand(1.0, 11e3) } ! 64.rand, x, x)) }).play;
    Ndef(\pat, { PanAz.ar(32, SinOscFB.ar(Delay1.ar(Ndef.ar(\mat))*0.5+0.5*18, 1.4), SinOscFB.ar(0.2, 1.4), 1, 2) });

Ndef(\pat, { PanAz.ar(32, SinOscFB.ar(Delay1.ar(Ndef.ar(\mat))*0.5+SinOscFB.ar(0.2, Ndef.ar(\mat))*18, 1.4), SinOscFB.ar(0.2, Ndef.ar(\mat)), 1, 2) });


Synth	
