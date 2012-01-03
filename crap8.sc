
(
a = { |tempo=1| Ringz.ar(Impulse.ar(tempo), [401, 400], 1/tempo) };
a.play;
t = TempoBusClock(a);
t.tempo = 5;

//Task { loop { "klink".postln; 1.wait } }.play(t);
);

(
a = { |tempo=1| Ringz.ar(Impulse.ar(tempo), [401, 400], 1/tempo) }.play(args:[\tempo,5]);
t = TempoBusClock(a,5);

//Task { loop { "klink".postln; 1.wait } }.play(t);
);


s.boot

TempoClock.tempo


(
a = Pbind(
	\note, Pseq([1,2,3,4],inf),
	\dur, 0.5
);
Ppar([Pset(\dur, 0.6, a), a]).play

)

(
var sum = 0;
a = List[ ( 'velocity': 0, 'start_silence': 0.40773289203644, 'end_offset': 0, 'dur': 0.40773289203644,
  'midinote': \rest, 'sustain': 0.1, 'start_offset': 0, 'default_start_silence': 0.40773289203644, 'default_end_silence': 6.6967190027237,
  'end_silence': 6.6967190027237 ), ( 'midinote': 53, 'curtime': 4.4066932678223, 'velocity': 97, 'dur': 0.89554810523987,
  'sustain': 0.27002263069153 ), ( 'midinote': 54, 'curtime': 5.3022413730621, 'velocity': 105, 'dur': 6.6967190027237,
  'sustain': 0.23547291755676 ) ];
b = List[ ( 'velocity': 0, 'start_silence': 0.20773289203644, 'end_offset': 0, 'dur': 0.20773289203644,
  'midinote': \rest, 'sustain': 0.1, 'start_offset': 0, 'default_start_silence': 0.40773289203644, 'default_end_silence': 6.6967190027237,
  'end_silence': 6.6967190027237 ), ( 'midinote': 63, 'curtime': 4.4066932678223, 'velocity': 97, 'dur': 0.89554810523987,
  'sustain': 0.27002263069153 ), ( 'midinote': 63, 'curtime': 5.3022413730621, 'velocity': 105, 'dur': 6.6967190027237,
  'sustain': 0.23547291755676 ) ];


a = List[ ( 'sustain': 0.1, 'start_offset': 0, 'default_end_silence': 1.1193073511124, 'midinote': \rest,
  'dur': 2.0287976741791, 'end_offset': 0, 'default_start_silence': 2.0287976741791, 'end_silence': 1.1193073511124, 'velocity': 0,
  'start_silence': 2.0287976741791, 'slotnum': \rest ), ( 'velocity': 0.094488188976378, 'dur': 0.85189497470856, 'midinote': 43, 'curtime': 2.0287976741791,
  'sustain': 0.22347736358643, 'slotnum': 0 ), ( 'velocity': 0.96062992125984, 'dur': 1.1193073511124, 'midinote': 43, 'curtime': 2.8806926488876,
  'sustain': 0.23701930046082, 'slotnum': 0 ) ];
b = List[ ( 'sustain': 0.1, 'start_offset': 0, 'default_end_silence': 5.0855154514313, 'midinote': \rest,
  'dur': 0, 'end_offset': 0, 'default_start_silence': 0, 'end_silence': 5.0855154514313, 'velocity': 0,
  'start_silence': 0, 'slotnum': \rest ), ( 'velocity': 1, 'dur': 0.87620632648468, 'midinote': 43, 'curtime': 0,
  'sustain': 0.15927402973175, 'slotnum': 0 ), ( 'velocity': 1, 'dur': 0.98833823204041, 'midinote': 43, 'curtime': 0.87620632648468,
  'sustain': 0.23097467422485, 'slotnum': 0 ), ( 'velocity': 1, 'dur': 1.0499399900436, 'midinote': 43, 'curtime': 1.8645445585251,
  'sustain': 0.17548191547394, 'slotnum': 0 ), ( 'velocity': 1, 'dur': 5.0855154514313, 'midinote': 43, 'curtime': 2.9144845485687,
  'sustain': 0.14106738567352, 'slotnum': 0 ) ];


~merge_note = { arg no1, no2;
	var ano1, ano2, res;
	var makeabs, makerel, time = 0, last = 0, elm;
	makerel = { arg li;
		var res = List.new, elm, time;
		0.for(li.size-1) { arg x;
			elm = li[x].copy;
			if( x == (li.size-1) ) {
			} {
				elm.dur = li[x+1].time - li[x].time
			};
			elm.time = nil; // tidy up
			res.add(elm);
		};
		res;
	};
	makeabs = { arg li;	
		var res = List.new, elm, time;
		0.for(li.size-1) { arg x;
			x.debug("iter");
			elm = li[x].copy;
			if(x == 0) {
				elm.time = 0;
			} {
				elm.time = li[x-1].dur + res[x-1].time;
			};
			res.add(elm);
		};
		res;
	};
	ano1 = makeabs.(no1);
	ano2 = makeabs.(no2);
	
	res = ano1 ++ ano2;
	res.sort({ arg a, b; 
		if(a.time == b.time) {
			a.start_silence.notNil
		} {
			a.time < b.time
		}
	});
	res = makerel.(res);

	res = res[1..]; // remove double rest
	res[0].start_silence = res[0].dur;
	res[0].end_silence = res.last.dur;

	res;

};
c = ~merge_note.(b,a);
c.do(_.postln);
c.do { arg x; sum = sum + x.dur };
sum.debug("dur");

)

(
var sum = 0;
b.do { arg x; sum = sum + x.dur };
sum
)


(
a = NoteOnResponder { arg src, chan, num, veloc;
	[src, chan, num, veloc].debug("noteon");
}

)
(
b = NoteOffResponder { arg src, chan, num, veloc;
	[src, chan, num, veloc].debug("noteoff");
}
)
b.remove






(

a = (
	y: 1,
	bla: { arg self, x=7; self.y = self.y + 40; x.debug("blablalb"); }
);
b = (
	parent: a,
	y: 5,
	bla: { arg self, x=2;
		"niark".debug;
		self.parent[\bla].(self, x);
		self.y.debug("y");
	}
);
b.bla

)


(

w = Window.new("soundfile test", Rect(200, 200, 850, 400));

a = SoundFileView.new(w, Rect(20,20, 700, 60));

f = SoundFile.new;

f.openRead("sounds/a11wlk01.wav");

a.soundfile = f;

a.read(0, f.numFrames);

w.front;

)



(
w = Window.new.front;
g = EZSlider( w,         // parent
              390@20,    // bounds
              " test ",  // label
              \freq,     // controlSpec
              { |ez| (ez.value.asString ++" is the value of " ++ ez).postln } // action
);
g.setColors(Color.grey,Color.white)
);

// Simplest version, no parent view, so a window is created
(
        g = EZSlider(label:" test ");
        g.action_({ |ez| (ez.value.asString ++" is the value of " ++ ez).postln });
);






(

m=nil;
//m=2@2;                // uncomment this for margin

/////////////////
/// Layout \horz

(               // all features, small font
                g=EZSlider(nil, 400@14," freq  ", \freq,unitWidth:30, numberWidth:60,layout:\horz, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180,50);
                g.font_(Font("Helvetica",10));
);

(               // no unitView
                g=EZSlider(nil, 400@16," freq  ", \freq,unitWidth:0, numberWidth:60,layout:\horz, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180, -20);
);
(               // no label, so use window name as label
                g=EZSlider(nil, 400@16, nil, \freq,unitWidth:0, numberWidth:60,layout:\horz, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180, -90);
                g.window.name="Freq";
);

/////////////////
/// Layout \line2

(               // all features
                g=EZSlider(nil, 300@42," freq  ", \freq,unitWidth:30, numberWidth:60,layout:\line2, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180,-160);
);

(               // no unitView, with label
                g=EZSlider(nil, 300@42," freq  ", \freq,unitWidth:0, numberWidth:60,layout:\line2, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180,-260);
);

(               // no label
                g=EZSlider(nil, 300@42,nil, \freq, unitWidth:30, numberWidth:60,layout:\line2, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180,-360);
                g.window.name="Freq";
);

(               // no lablel, so use window name as label
                g=EZSlider(nil, 150@42,"Freq", \freq,unitWidth:0, numberWidth:60,layout:\line2, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.black, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(-180,-460);
                g.window.name="Freq";
);

/////////////////
/// Layout \vert

(               // all features, small font
                g=EZSlider(nil, 45@300," Vol  ", \db.asSpec.step_(0.01),unitWidth:30, numberWidth:60,layout:\vert, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(250,50);
                g.font_(Font("Helvetica",10));
);
(               // no label, small font
                g=EZSlider(nil, 45@300, nil, \db.asSpec.step_(0.01),unitWidth:30, numberWidth:60,layout:\vert, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(310,50);
                g.font_(Font("Helvetica",10));
);
(               // no Units small font
                g=EZSlider(nil, 45@300, " Vol", \db.asSpec.step_(0.01),unitWidth:0, numberWidth:60,layout:\vert, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(370,50);
                g.font_(Font("Helvetica",10));
);
(               // no unitView, no Units small font
                g=EZSlider(nil, 45@300, nil, \db.asSpec.step_(0.01),unitWidth:0, numberWidth:60,layout:\vert, margin: m);
                g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
                        Color.white, Color.yellow,nil,nil, Color.grey(0.7));
                g.window.bounds = g.window.bounds.moveBy(430,50);
                g.font_(Font("Helvetica",10));
);

)




///////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////


// Sound example
(
// start server
s.waitForBoot({

var w, startButton, noteControl, cutoffControl, resonControl;
var balanceControl, ampControl;
var node, cmdPeriodFunc;

// define a synth
SynthDef("window-test", { arg note = 36, fc = 1000, rq = 0.25, bal=0, amp=0.4, gate = 1;
                var x;
                x = Mix.fill(4, {
                        LFSaw.ar((note + {0.1.rand2}.dup).midicps, 0, 0.02)
                });
                x = RLPF.ar(x, fc, rq).softclip;
                x = RLPF.ar(x, fc, rq, amp).softclip;
                x = Balance2.ar(x[0], x[1], bal);
                x = x * EnvGen.kr(Env.cutoff, gate, doneAction: 2);
                Out.ar(0, x);
        }, [0.1, 0.1, 0.1, 0.1, 0.1, 0]
).add;




// make the window
w = Window("another control panel", Rect(20, 400, 440, 180));
w.front; // make window visible and front window.
w.view.decorator = FlowLayout(w.view.bounds);
w.view.decorator.gap=2@2;

// add a button to start and stop the sound.
startButton = Button(w, 75 @ 20);
startButton.states = [
        ["Start", Color.black, Color.green(0.7)],
        ["Stop", Color.white, Color.red(0.7)]
];
startButton.action = {|view|
                if (view.value == 1) {
                        // start sound
                        node = Synth( "window-test", [
                                "note", noteControl.value,
                                "fc", cutoffControl.value,
                                "rq", resonControl.value,
                                "bal", balanceControl.value,
                                "amp", ampControl.value.dbamp ]);
                } {
                        // set gate to zero to cause envelope to release
                        node.release; node = nil;
                };
};

// create controls for all parameters
w.view.decorator.nextLine;
noteControl = EZSlider(w, 430 @ 20, "Note ", ControlSpec(24, 60, \lin, 1, 36, \note),
        {|ez| node.set( "note", ez.value )}, unitWidth:30)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);

w.view.decorator.nextLine;
cutoffControl = EZSlider(w, 430 @ 20, "Cutoff ", ControlSpec(200, 5000, \exp,0.01,1000,\Hz),
        {|ez| node.set( "fc", ez.value )}, unitWidth:30)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);

w.view.decorator.nextLine;
resonControl = EZSlider(w, 430 @ 20, "Reson ", ControlSpec(0.1, 0.7,\lin,0.001,0.2,\rq),
        {|ez| node.set( "rq", ez.value )}, unitWidth:30)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);

w.view.decorator.nextLine;
balanceControl = EZSlider(w, 430 @ 20, "Balance ", \bipolar,
        {|ez| node.set( "bal", ez.value )},  unitWidth:30)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);

w.view.decorator.nextLine;
ampControl = EZSlider(w, 430 @ 20, "Amp ", \db,
        {|ez| node.set( "amp", ez.value.dbamp )}, -6, unitWidth:30)
                .setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey, Color.white, Color.yellow);


// set start button to zero upon a cmd-period
cmdPeriodFunc = { startButton.value = 0; };
CmdPeriod.add(cmdPeriodFunc);

// stop the sound when window closes and remove cmdPeriodFunc.
w.onClose = {
        node.free; node = nil;
        CmdPeriod.remove(cmdPeriodFunc);
};
});
)




((0..10) / 10).collect { |num| num.quantize(1, 0.3, 0.5) }.postcs.plot2;
((0..10) / 10).collect { |num| num.quantize(1, 0.6, 0.5) }.postcs.plot2;
((0..10) / 10).collect { |num| num.quantize(1, 1.0, 0.5) }.postcs.plot2;
((0..10) / 10).collect({arg x; x+0.1.rand}).postcs.collect { |num| num.quantize(0.1, 0.1, 0.9) }.postcs; 1;
