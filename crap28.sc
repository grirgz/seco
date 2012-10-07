(
~notes = [12,8,7,0,[-12,4,12],[-12,4,12],[-12,4,12],[-11,5,12],[-11,5,12],[-9,7,12],7]+48;
~durs   = [0.5,0.5,0.5,0.5,1.5,1.0,1.0,1.0,1.0,2.0,0.5]/2;

~notespatternproxy = PatternProxy(Pseq(~notes,inf));
~durspatternproxy  = PatternProxy(Pseq(~durs,inf));

~rhodespat = Pfx(
        Pbind(
                \instrument,    \everythingrhodes,
                \midinote,      ~notespatternproxy,
                \dur,           ~durspatternproxy,
                \cutoff,Pstutter(11,Pn(Pseries(500,500,4),inf))
        ),
        \choruscompresseffect
);
)

x = ~rhodespat.play;


~notespatternproxy.source = Pseq(~notes,inf);
~notespatternproxy.source = Pxrand(~notes,inf);
~notespatternproxy.source = Pseq(~notes.stutter.scramble,inf);
~notespatternproxy.source = Pxrand(~notes + 7,inf);
~notespatternproxy.source = Pseq(~notes.stutter,inf);
~notespatternproxy.source = Pseq(~notes.stutter(3),inf);
~notespatternproxy.source = Pseq(~notes-5,inf);
~notespatternproxy.source = Pseq(~notes,inf);
~notespatternproxy.source = Pseq(~notes.pyramid(5),inf);
~notespatternproxy.source = Pseq(~notes.pyramid(7) + 3,inf);
~notespatternproxy.source = Pseq(~notes.mirror,inf);
~notespatternproxy.source = Pseq(~notes.permute(7),inf);
~notespatternproxy.source = Pseq(~notes.reverse,inf);
~notespatternproxy.source = Pseq(~notes,inf);

~durspatternproxy.source = Pseq(~durs,inf);
~durspatternproxy.source = Pseq(~durs*2,inf);
~durspatternproxy.source = Pxrand(~durs,inf);
~durspatternproxy.source = Pxrand(~durs/2,inf);
~durspatternproxy.source = Pxrand(~durs/4,inf);
