(
var stack;
w = Window().layout_( VLayout(
    Button().states_([["One"],["Two"],["Three"]]).action_({ |b| stack.index = b.value }),
    stack = StackLayout(
        TextView().string_("This is a chunk of text..."),
        TextView().string_("...and this is another..."),
        TextView().string_("...and another.")
    );
)).front;
)



SynthDef(\sin, {
        arg freq=#[200,300,400],gate=1;
        var env,sig;

        env = Linen.kr(gate,doneAction:2);

        sig = Mix(SinOsc.ar(freq))*0.125*env;

        Out.ar(0,sig);
}).add;

Pbind(
        \instrument,\sin,
        \dur,1.5,
		\legato, 0.2,
        \freq,[[500,600,700]]
).play



~bla = (x:4);

~resp = (update:{arg self, a, b; [a,b].debug("respi")})
~bla.addDependant(~resp)
~bla.changed(\rah)


Sheet({ arg f;
        Tempo.default.gui(f); // move the slider, it works
		})


(
w  = Window.new();
w.front;
w.layout = VLayout.new(*[

	3.collect { arg i;
		HLayout.new(*
			3.collect { arg j;
				StaticText.new.string_("plop"++i++j);
			}
		);
	},
	3.collect { arg i;
		HLayout.new(*
			3.collect { arg j;
				StaticText.new.string_("blay"++i++j);
			}
		);
	},
	nil

].flat);

)




(
w  = Window.new();
w.front;
a = UserView.new;
b = UserView.new;
a.background = Color.red;
b.background = Color.red;
a.maxSize = 800@30;
b.maxSize = 800@30;
w.layout = VLayout.new(
	[
		HLayout.new(
				[StaticText.new.string_("plop1"), stretch:0, align: \top],
				//StaticText.new.string_("plop2")
				[a, stretch:1]
		),
		stretch: 0,
		align: \topLeft,
	],
	[
		HLayout.new(
				[StaticText.new.string_("plop2"), stretch:0, align: \top],
				//StaticText.new.string_("plop2")
				b
		),
		stretch: 0,
		align: \topLeft,
	],
);
w.layout.add(nil)

)
