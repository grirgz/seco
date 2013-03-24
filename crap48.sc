// still have to work out the right modulation index calculation

q = q ? ();
q.numOscs = 5;

(

Ndef(\fmMatrix, {
	var numOscs = q.numOscs;
	
	var oscs;
	var modFreqs = \modFreqs.kr({|i| 1000}!numOscs);
	var modIndex = \modIndex.kr({|i|   0}!(2**numOscs)).clump(numOscs);
	var amps     = \amps    .kr({|i|
		(i == (numOscs-1)).if({1}, {0}) }!numOscs);
	var tmpOsc;
	
	oscs = modFreqs.inject([], {|oscArray, freq, i|
		tmpOsc = SinOscFB.ar(
			freq
			+ oscArray.inject(0, {|sum, osc, j| 
				sum + (osc * modIndex[i][j].linlin(0, 3, 0, 10000))
			}),
			modIndex[i][i]
		);
		
		oscArray ++ tmpOsc;
	}); // end inject
	
	(oscs * amps).sum;
})
)

Ndef(\fmMatrix).gui

(
var specs = (
	modFreqs: [0.1, 10000, \exp, 0].asSpec,
	modIndex: [0, 3, \lin, 0].asSpec;
);
var modIndex     = {0!q.numOscs}!q.numOscs;
var modFreqState = 1000!q.numOscs;
var ampState     = 0!q.numOscs;

q.win = Window.new("FM Matrix", Rect(100, 100, q.numOscs * 30, 800)).decorate.front;


StaticText(q.win, Rect(10, 10, 200, 20)).string_("modIndex");
q.win.view.decorator.nextLine;
q.numOscs.do{|i|
	(i+1).do{|j|
		var ez;
		
		ez = EZKnob(q.win, Rect(25, 25, 25, 50), 
			controlSpec: specs[\modIndex],
			initAction: true
		)
		.action_{|knob| 
			// state[i][j] = specs[key].map(knob.value);
			modIndex[i][j] = knob.value;
			Ndef(\fmMatrix).setn(\modIndex, modIndex.flat);
		};
		ez.knobView.mode_(\vert);
	};
	q.win.view.decorator.nextLine;
};
q.win.view.decorator.nextLine;

StaticText(q.win, Rect(10, 10, 200, 20)).string_("modFreqs");
q.win.view.decorator.nextLine;
q.numOscs.do{|i|
	var ez;
	ez = EZKnob(q.win, Rect(0, 0, 25, 50), 
		controlSpec: specs[\modFreqs],
		initAction: true
	)
	.action_{|knob|
		modFreqState[i] = knob.value;
		Ndef(\fmMatrix).setn(\modFreqs, modFreqState);
	};
	ez.knobView.mode_(\vert);
};

q.win.view.decorator.nextLine;
StaticText(q.win, Rect(10, 10, 200, 20)).string_("amps");
q.win.view.decorator.nextLine;
q.numOscs.do{|i|
	EZSlider(q.win, Rect(0, 0, 25, 100), label: i, layout: 'vert').action_{|slider|
		ampState[i] = slider.value;
		Ndef(\fmMatrix).setn(\amps, ampState);
	}
}
)

Env
