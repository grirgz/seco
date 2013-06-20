
(
~t = Pbind(\type, Pfunc{ arg ev; if(ev[\index] == ~index) { \note } { \rest }});
~p = Ppar(
	12.collect { arg n; ~t <> Pbind(\note, Pseq([1,2,3,2],inf)+n, \index, n, \dur, 0.2) }
);
~p.play;

)

~index = 1;
~index = 8;

(
~t = Pbind(\type, Pfunc{ arg ev; if(ev[\index] == ~index) { \note } { \rest }});
~p = Ppar(
	12.collect { arg n; ~t <> Pbind(\note, Pseq([1,2,3,2],inf)*(Pn(Plazy{ ~m.(n) })), \index, n, \dur, 0.2) }
);
~p.play;

)

~m = { arg n; n };
~m = { arg n; Pseq([n-10,n/2,n,n*4,n/3,3,n*2,n,n+5]) };

~index = Pseq([1,2],inf);
~index = nil
~index = 1
~index = 5



~pats = (0..12).collect { | n, i |
        Pbind(*[note: n, finish: { if (~index != i) { ~note = \rest} }, dur: 0.2])
};

Pchain(Pbind(*[ index: Pstep(Pwhite(0, 12,4), Prand([0.8,0.1],inf),inf)]), Ppar(~pats)).play;



{  3 * ~n }.writeArchive("/tmp/plop")
Archive
f = Object.readArchive("/tmp/plop")
Ppar(f[\pats]).play
f[\index] = 1
~n = 5
f.value
f

~pats.asCompileString
e = Environment.new
f = Environment.new
e

~n
e.parent = currentEnvironment
f.parent = currentEnvironment
e
e.use { ~n.postln; ~n = 10; ~n.postln; }
e.use { currentEnvironment.parent.postln; }
f.use { currentEnvironment.parent.postln; }

e.writeArchive("/tmp/plop")

e = currentEnvironment

~pats = nil
