(
~a = Pseq([
	(
		midinote: 12
	),
	Pbind(*[
		midinote: Pseq([10,11])
	]),

],inf);

~b = ~a <> Pbind(\transpose, Pseq([3,4],inf));
//~b = ~a;
~b.asStream.nextN(10, Event.default)
)

- sheet
- modulators nodes
- effects nodes
- midifilter nodes
