().at



~p = ~make_player_from_synthdef.(\sine);
~p.node.play
~p.set_noteline(true)
~p.get_arg(\rangex).change_kind(\seq)


(
SynthDef(\rangebug, { arg freq=200, range=100;
	var ou;
	ou = SinOsc.ar(XLine.kr(freq, range));
	ou = ou * EnvGen.kr(Env.linen, timeScale: 0.2, doneAction:2);
	Out.ar(0, ou);
}).add;
)

(instrument: \rangebug, range:100).play


~p = ~make_player_from_synthdef.(\sinadsr);
~p.set_noteline(true)
~p.node.play
~p.get_arg(\range).change_kind(\seq)

~f = ~p.get_piano
~r = ~f.value
~r.release


(
~pp = EventPatternProxy.new;
~pp.source = Pbind(
	\instrument, \rangebug,
	\freq, 200,
	\range, 100,
);
~pp.play;

)


[\bla, \rah].difference([\bla, \rah])
