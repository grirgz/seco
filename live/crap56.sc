p = Pbind(\degree, Pn(Pseries(0, 1, 8),2), \dur, 0.25);

p = Pbind(\degree, Pn(Pseries(0, 1, 8),2), \dur, Pseq([0.25,0.125,0.125],inf));

p.play;  // straight 16ths

(
~swingify = Prout({ |ev|
        var started = thisThread.beats,
        now, adjust;
        while { ev.notNil } {
                now = thisThread.beats - started;
                // an odd number here means we're on an off-beat
                if((now / ev[\swingBase]).round.asInteger.odd) {
                        adjust = ev[\swingBase] * ev[\swingAmount];
                        ev[\timingOffset] = (ev[\timingOffset] ? 0) + adjust;
                        ev[\sustain] = ev.use { ~sustain.value } - adjust;
                } {
                        // will the next note swing?
                        if(((now + ev.delta) / ev[\swingBase]).round.asInteger.odd) {
                                // if yes, then this note needs to be longer
                                ev[\sustain] = ev.use { ~sustain.value } + (ev[\swingBase] *
ev[\swingAmount]);
                        };
                };
                ev = ev.yield;
        };
});
)


Pchain(~swingify, p, (swingBase: 0.25, swingAmount: 1/3)).play;
Pchain(~swingify, p, (swingBase: 0.25, swingAmount: 3/8)).play;
Pchain(~swingify, p, (swingBase: 0.25, swingAmount: 0.0079)).play;

// note duration = twice swingBase, no swing (correct)
Pchain(~swingify, Pstretch(2, p), (swingBase: 0.25, swingAmount: 1/3)).play;

// hear the result of different swing amounts
(
Ppar([
        Pchain(~swingify, p, (swingBase: 0.25, swingAmount: 0.6, pan: -1)),
        Pchain(~swingify, p, (swingBase: 0.25, swingAmount: 0.2, pan: 1, octave: 6))
]).play;
)


(
q = Ppar([
        // walking bass (by an idiot bass player who only chooses notes randomly)
        Pbind(
                \octave, 3,
                \degree, Pwhite(0, 7, inf),
                \dur, 0.5
        ),
        Pseq([
                Pchain(
                        ~swingify,
                        Pbind(
                                \degree, Pseries(-7, 1, 15) +.x Pseq([0, 9], 1),
                                \dur, Pwhite(1, 3, inf) * 0.25
                        ),
                        (swingBase: 0.25, swingAmount: 0.2)
                ),
                Pfuncn({ q.stop; Event.silent(1) }, 1)
        ])
]).play;
)

