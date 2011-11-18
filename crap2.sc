
a = Pbind(\dur, 0.1)
a.play

(
~make_cell = { arg parent, label;
	var bt;

	bt = GUI.button.new(parent, Rect(50,50,200,30));
	bt.states = [
		[ label, Color.black, Color.white],
		[ label, Color.white, Color.black],
	];
	bt.value = 0;
	bt;

};
// first test, remove and recreate widgets
~bla = {
	var window, sl_layout;
	window = Window.new("bla", Rect(0,0,1500,400));	

	sl_layout = GUI.hLayoutView.new(window, Rect(0,0,1500,60*6));

	~rah.(sl_layout);
	window.front;
	window.view.keyDownAction = { ~rah.(sl_layout); window.view.focus(true) };
	sl_layout.children[1].children[1] = ~make_cell.(sl_layout.children[1], "hahah");


};

~rah = { arg parent;
	var layout;

	parent.removeAll;
	8.do {
		layout = GUI.vLayoutView.new(parent, Rect(0,0,(160),60*6));

		4.do {

				~make_cell.(layout, "blaaa"); // FIXME: should show name instead of uname
		}
	}

};

~bla.()

)


(
// second test, recreate the whole window
~bla = {
	var window, sl_layout;
	window = Window.new("bla", Rect(0,0,1500,400));	

	sl_layout = GUI.hLayoutView.new(window, Rect(0,0,1500,60*6));

	~rah.(sl_layout);
	window.front;
	window.view.keyDownAction = { window.close; ~bla.(); };


};

~rah = { arg parent;
	var layout;

	//parent.removeAll;
	8.do {
		layout = GUI.vLayoutView.new(parent, Rect(0,0,(160),60*6));
		layout.background = ~editplayer_color_scheme.control;

		4.do {

				~make_cell.(layout, "blaaa"); // FIXME: should show name instead of uname
		}
	}

};

~bla.()

)

(
// third test, recreate the whole window but don't close the old
~bla = {
	var window, sl_layout;
	window = Window.new("bla", Rect(0,0,1500,400));	

	sl_layout = GUI.hLayoutView.new(window, Rect(0,0,1500,60*6));

	~rah.(sl_layout);
	window.front;
	window.view.keyDownAction = { ~bla.(); };


};

~rah = { arg parent;
	var layout;

	//parent.removeAll;
	8.do {
		layout = GUI.vLayoutView.new(parent, Rect(0,0,(160),60*6));

		4.do {

				~make_cell.(layout, "blaaa"); // FIXME: should show name instead of uname
		}
	}

};

~bla.()

)

s.boot








(
~default_noteline = [ // FIXME: crash when no notes
	(
		midinote: \rest,
		sustain: 0.1,
		start_silence: 0.5,
		default_start_silence: 0.5,
		start_offset: 0,
		end_offset: 0,
		dur: 0.5
	),
	(
		midinote: 64,
		sustain: 0.1,
		dur: 0.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 1.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 1.0
	),
	(
		midinote: 67,
		sustain: 0.1,
		dur: 0.5
	),
];

~find_next = { arg dur, notes;
	var res=0, vidx=0, last=nil, delta=0, prevdelta=0;
	notes[1..].do { arg x, n;
		[n, res, vidx, last, dur].debug("begin n res vidx last dur");
		if( res < dur ) {
			res = x.dur + res;
			vidx = vidx + 1;
			last = x.dur;
		};
		[n, res, vidx, last].debug("end");
	};
	delta = res - dur;
	[ delta, vidx ];
};

~find_prev = { arg dur, notes;
	var res=0, vidx=0, last=nil, delta=0, prevdelta=0;
	dur = ~total_dur.( notes[1..(notes.lastIndex-1)] ) - dur;

	notes[1..].do { arg x, n;
		[n, res, vidx, last, dur].debug("begin n res vidx last dur");
		if( res <= dur ) {
			res = x.dur + res;
			vidx = vidx + 1;
			last = x.dur;
		};
		[n, res, vidx, last].debug("end");
	};
	delta = res - dur;
	if(last.isNil) {
		prevdelta = nil
	} {
		last.debug("last");
		prevdelta = last - delta;
	};
	[ prevdelta, vidx-1 ];
};

~find_nexti = { arg dur, notes;
	var res = ~calc_delta.(dur, notes);
	[res[0], res[2]]
};
~find_previ = { arg dur, notes;
	var res;
	dur = ~total_dur.( notes[1..(notes.lastIndex-1)] ) - dur;
	dur.debug("dur");
	res = ~calc_delta.(dur, notes);
	[res[1], res[2]-1]
};


~total_dur = { arg notes;
	var res=0;
	notes.do { arg x; res = x.dur + res; };
	res;
};

//~find_next.(0.0, ~default_noteline);
~find_next.(3.00, ~default_noteline).debug("next");
~find_prev.(3, ~default_noteline).debug("prev");

)

~total_dur.(~default_noteline);
~total_dur.([~default_noteline[1]]);

)
[~default_noteline[1]][1..-1]
[~default_noteline][3..0]

s.boot

[1,5,7].lastIndex
(1..10)
(1..10)[8..-8]





:
q = Pbind(\note, Pseq([1,2,3,4,5,6,7,8], inf), \dur, 0.5);
r = Pbind(\note, Pseq([1,2,3,4,5,6,7,8]+10), \lag, 0.25, \dur, 0.5);

v = VarGui(stream: [q,r], quant: 0.5).gui;q = Pbind(\note, Pseq([1,2,3,4,5,6,7,8], inf), \dur, 0.5);
r = Pbind(\note, Pseq([1,2,3,4,5,6,7,8]+10), \lag, 0.25, \dur, 0.5);

v = VarGui(stream: [q,r], quant: 0.5).gui;


Quarks.gui
Main.version
s.boot

Help(Quarks)
Help(Quarks)
Help.gui

EnvGen


(

SynthDef(\synth_0, { |devFactor = 0.1, devFreq = 10, amp = 0.1|

var freq = XLine.kr(400, 1200, 10); 

Out.ar(0, SinOsc.ar(SinOsc.ar(devFreq, 0, devFactor * freq, freq), mul: amp).dup(2) * EnvGate.new) 

}).add;

)

// VarGui to play synth of above definition and set synth args, 

// Player functionality with different stop / renew modes.

// Notification must be on.

// Note that pause, stop and resume actions cause sending of run / free messages, thus possibly audible clicks.

// To avoid that work with amp = 0 or use a gated envelope and a gate control slider with only values 0 and 1 or

// use Synths of limited duration as in Example (2)

(

VarGui(

synthCtr: [\devFactor, [0, 0.99, \lin, 0.01, 0.5], 

\devFreq, [1, 100, \lin, 0.1, 70],

\amp, [0, 0.3, \lin, 0.001, 0.1]], 

synth: \synth_0).gui;

)




(

SynthDef(\synth_1a, { |freq = 400, preAmp = 5, amp = 0.1|

var src = SinOsc.ar(freq, 0, mul: preAmp).tanh ! 2;

Out.ar(0, src * amp * EnvGen.ar(Env.perc, doneAction: 2))

}).add;



// There are different ways to read a sequence of values from a settable array assigned to an envir variable.

// A flexible solution is the use of a wrapping Plazy, so the EventStreamPlayer can be played in an 

// arbitrary environment, from which ~seq will be taken.


p = Pbind(

\instrument, \synth_1a,

\preAmp, Pfunc { ~preAmp },

\amp, Pfunc { ~amp },

\midinote, Plazy { Pseq(~seq, inf) } + Pfunc { ~seqAdd }, 

\dur, 0.2

);



// ~seq will be set and read in the new Environment, which will be generated at VarGui init time.


~specPairs = [\seq, [36, 60, \lin, 1, 36] ! 5, 

\seqAdd, [0, 12, \lin, 1, 0],

\amp, [0, 0.3, \lin, 0.01, 0.1],

\preAmp, [5, 50, \lin, 0.01, 20]];


v = VarGui(~specPairs, stream: p).gui;

)



// An alternative way to use a function in a Pbind is to collect indices


(

r = Pbind(

\instrument, \synth_1a,

\preAmp, Pfunc { ~preAmp },

\amp, Pfunc { ~amp },

\midinote, Pseq((0..4), inf).collect { |i| ~seq[i] } + Pfunc { ~seqAdd },

// shorter: \midinote, Pseq((0..4), inf).collect(~seq[_]) + Pfunc { ~seqAdd },

\dur, 0.2

);


v = VarGui(~specPairs, stream: r).gui;

)




// Be aware in what Environments you are defining and setting:


(

q = Pbind(

\instrument, \synth_1a,

\preAmp, Pfunc { ~preAmp }, // values to be got at runtime from EventStreamPlayer's environment

\amp, Pfunc { ~amp },

// ~seq given in the currentEnvironment, Pseq demands a non-empty collection at time of Pattern definition 

\midinote, Pseq(~seq = 36!5, inf) + Pfunc { ~seqAdd },

\dur, 0.2

);



// You can do it this way if you give the information that ~seq should be set in currentEnvironment

// by passing an EventStreamPlayer instantiated in currentEnvironment.



~specPairs = [\seq, [36, 60, \lin, 1, 36] ! 5, 

\seqAdd, [0, 12, \lin, 1, 0],

\amp, [0, 0.3, \lin, 0.01, 0.1],

\preAmp, [5, 50, \lin, 0.01, 20]];


v = VarGui(~specPairs, stream: q.asESP).gui; // .asESP: convenience method for .asEventStreamPlayer

)



// See what happens if you pass the pattern as stream arg:

// it makes all variables to be set in a new Environment generated at VarGui init time,

// so ~seqAdd, ~amp and ~preAmp can be set with effect, but not ~seq, 

// as the Pseq stream will read from its list ~seq in currentEnvironment


v = VarGui(~specPairs, stream: q).gui; // WRONG !



// compare


(

v.envirs.postln;

currentEnvironment;

)

(
SynthDef(\sawbpfenv, { arg out=0, gate=1, amp=0.1, freq=300, rq=0.5;
	var ou, env, envf;
	envf = ~make_genadsr.(\genadsr, gate);
	ou = Saw.ar(freq+(envf*100));
	env = EnvGen.ar(~make_adsr.(\adsr).poll, gate, doneAction:2);
	//ou = BPF.ar(ou, envf, rq);
	ou = BPF.ar(ou, freq+10, rq);
	ou = ou * env * amp;
	Out.ar(out, ou);

}).add;
)


(
~genadsr_event_to_env_array = { arg adsr;
	Env.performWithEnvir(\adsr, adsr).asArray ++ [adsr.levelScale, adsr.levelBias, adsr.timeScale]
};

~default_adsr = (
	attackTime:0.1,
	decayTime:0.2,
	sustainLevel:0.3,
	releaseTime:0.4,
	curve:0
);
~default_genadsr = (
	attackTime:0.1,
	decayTime:0.2,
	sustainLevel:0.3,
	releaseTime:0.4,
	curve:0,
	levelScale:0.7,
	levelBias: 0.10,
	timeScale: 0.9
);

a = Pbind(
	\instrument, \sawbpfenv,
	\genadsr, Pfunc({[ ~genadsr_event_to_env_array.(~default_genadsr) ]}),
	\adsr, Pfunc({[ ~adsr_event_to_env.(~default_adsr).asArray ]}),
	\dur, 1
);

a.play

)

(
SynthDef(\loop1, {| out = 0, amp=0.1, bufnum = 0, gate = 1, pos = 0, speed = 1, freq = 0, endfreq = 0.001, sustain=0.5, wobble = 3, boost = 1|

	var player,env;
	freq = XLine.ar(freq,endfreq,sustain/4);
	freq = freq.cpsmidi + (LFNoise2.ar(3).range(-1,1) * (1/12));
	freq = freq.midicps;
	env =  EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2) * amp;
	//env = Linen.kr(gate, 0.1,1,0.1,doneAction:2);

	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, Impulse.ar(freq), startPos: (pos*BufFrames.kr(bufnum)) + Rand(0,20), doneAction:2, loop: 1) * boost;
	player = RLPF.ar(player, SinOsc.ar(wobble/sustain).range(20000,80), XLine.ar(0.2,0.9,sustain)) * boost;
	//player = SinOsc.ar(200);
	Out.ar(out, player * env);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;
)
a = Synth(\loop1, [\adsr, [Env.adsr(0.1, 0.1, 1, 0.1, 1, 0).asArray ]])
a.release

1+1
Synth.freeAll








(
// Note: 'p' is not a Pbind after this statement!
// it's an EventStreamPlayer
p = Pbind(\degree, Pseries(0, 1, 8), \dur, 0.25).play;

c = SimpleController(p).put(\stopped, {
       "This function is running at the end of the pattern.".postln;
       c.remove;  // clean up garbage
});
)

To know when the last one of a group of event stream players has ended, I would keep them in a dictionary:

~players = IdentityDictionary.new;

... and write a little function to do the bookkeeping.

~playOne = { |pattern, quant, action|
       var esp, simplec;
       esp = pattern.play(quant: quant);
       simplec = SimpleController(esp).put(\stopped, { |... args|
               simplec.remove;  // clean up garbage
               ~players.removeAt(esp);
               action.valueArray(args);
       });
       ~players.put(esp, simplec);  // bookkeeping
       esp
};

Then your action function can check the size of ~players. If the size is 0, then the last stream player was removed and you can go ahead with the action.

(
var act = {
       if(~players.size == 0) { "all done".postln } { "one is done".postln };
};

p = Pbind(\degree, Pseries(0, 1, 8), \dur, 0.25);

fork {
       5.do {
               ~playOne.value(p, nil, act);
               rrand(0.1, 0.3).wait;
       }
};
)





(
// Note: 'p' is not a Pbind after this statement!
// it's an EventStreamPlayer
p = Pbind(\degree, Pseries(0, 1, 4)-5, \lag, 0.1, \dur, 0.5);
q = Pbind(\degree, Pseries(0, 1, 8), \dur, 0.5);

~playpar = { arg list;
	var player, controller, dict = Dictionary.new, playing = list.size;

	list.do { arg pat, i;
		player = pat.play;
		controller = SimpleController(player).put(\stopped, {
			playing.debug("playing");
			if(playing <= 1) {
				list.do(_.stop);	
				i.debug("stop all");
			} {
				i.debug("repeating");
				playing = playing -1;
				pat.play;
			};
		});

	}

};

~playpar.([p, q]);

)



(
SynthDef(\fmbump,
   {
       arg freq = 220, mod_ratio = 0.5, car_ratio = 1, amp_att = 0.01, amp_dec = 0.01, amp_sust = 2, amp_rel = 3, mod_att = 0.01, mod_dec = 0.01, mod_sust = 2, mod_rel = 3, mul = 0.1;
       var amp_env, freq_env, mod_env, mod_osc, car_osc, source, sig, out;
       amp_env = EnvGen.ar(Env.new(
       [0.00001, 1, 0.95, 0.95, 0.00001],
       [amp_att, amp_dec, amp_sust, amp_rel], 'exponential'), doneAction: 2);
       freq_env = EnvGen.ar(Env.new(
       [0.02, 0.00001, 0.00001, 0.01, -0.01],
       [amp_att, amp_dec, amp_sust, amp_rel], 'linear'));
//the modulation envelope
       mod_env = EnvGen.ar(Env.new(
       [200, 200, 1, 1, 1],
       [mod_att, mod_dec, mod_sust, mod_rel], 'exponential'));
       mod_osc = SinOsc.ar(freq * mod_ratio) * mod_env;
       car_osc = SinOsc.ar(freq * car_ratio, mod_osc);
       source = car_osc;
       sig = source;
       out = source * amp_env * mul ! 2;
       Out.ar(0, out);
   }).add
   )

(
x = Synth(\fmbump, [\freq, 100, \amp_att, 0.001, \amp_dec, 0.001, \amp_sust, 0.1, \amp_rel, 1, \mod_att, 1, \mod_dec, 10, \mod_sust, 1, \mod_rel, 1]);
)




(
a = Pbind(\degree, Pseq([1, 2, 3], inf), \dur, Prand([0.2, 0.4], inf));
b = Pbind(\detune, Pseq([-30, 0, [0, 40]], inf), \dur, 0.1);
b = Pbind(\degree, Pkey(\degree)+4, \dur, 0.1);
c = b <> a;
c.play; // see that the \dur key of a is overridden by b
)

(
"xterm".systemCmd;
"plop".postln;
)

"gnome-terminal -e 'vim arg'".unixCmd({"connerie".debug});

(
var pat = 2;
~pat = (pat:32);
~pat.use({"~pat".interpret})
)
Environment




(
var tmp, file;
tmp = "tempfile -p seco".unixCmdGetStdOut;
tmp = tmp.split($\n)[0];
tmp.debug("tmp");
//fork {
	if(true) {
		file = File.new(tmp, "w");
		file.write("plop\n");
		file.close;
	};
	//0.1.wait;
	("xterm -r -fn 10x20 -e \"vim "++tmp++"\"").unixCmd({
		var file, code, res;
		"hein".debug;
		//fork { 
			//4.1.wait;
			file = File.new(tmp, "r");
			code = file.readAllString;
			file.close;
			code.debug("code");
		//}
	});
//};
)

(
var tmp, file;
tmp = "tempfile -p seco".unixCmdGetStdOut;
("("++tmp.split($\n)[0]++")").postln;
)









(
var patterns;
a = Pbind(\note, Pseq([1,2,3,4]), \dur, 0.5);
b = Pbind(\note, Pseq([1,2,3,4,5,6,7,8]+10), \lag, 0.1, \dur, 0.5);
x = PatternProxy.new;
y = PatternProxy.new;
x.source = a;
y.source = b;
patterns = [x,y];
p = Pspawner({ |spawner|
       var streams = List.new;
       patterns.do { |pattern|
               var stream;
               stream = CleanupStream(pattern.asStream, {
                       streams.remove(stream);
                       if(streams.isEmpty) {
                               spawner.suspendAll
                       } {
                               spawner.par(Pn(pattern, inf))
                       };
               });
               streams.add(stream);
               spawner.par(stream);
       };
});


Pseq([p, p]).play
)
x.play
x.asStream





(
a = Pbind(\note, Pseq([1,2,3,4],inf), \dur, 0.5);
x = EventPatternProxy.new;
x.source = a;
x.play;
q = x.player;
e = SimpleController(q);
e.put(\stopped,  { "stop!!".debug });
)
x.stop
q.play
q.stop


y = Spawner.new
b = y.par(a)
b.play


(
var patterns;
a = Pbind(\note, Pseq([1,2,3,4]), \dur, 0.5);
b = Pbind(\note, Pseq([1,2,3,4,5,6,7,8]+10), \lag, 0.1, \dur, 0.5);
a = Pbind(\note, Pseq([1,2,3,4,5,6,7,8]), \dur, 0.5);
x = PatternProxy.new;
y = PatternProxy.new;
x.source = a;
y.source = b;
patterns = [x,y];

~obj = \plop;

p = Pspawner({ |spawner|
       var streams = List.new;
	   var sc = SimpleController(~obj);
	   sc.put(\stop, { arg obj, msg, num;
			spawner.suspend(streams[num]);
		    streams.remove(streams[num]);
	   });
       patterns.do { |pattern, i|
               var stream;
               stream = CleanupStream(pattern.asStream, {
						i.debug("stop");
                       streams.remove(stream);
                       if(streams.isEmpty) {
                               spawner.suspendAll;
							   sc.remove;
                       } {
                               spawner.par(Pn(pattern, inf))
                       };
               });
               streams.add(stream);
			   i.debug("play");
               spawner.par(stream);
       };
});


z = Pseq([p, p],2);
//z = Pbind(\type, Pfunc({ arg ev; if(~mute == 1) { \rest } { \note } })) <> z;
z.play
)
~mute = 1;
~mute = 0;
x.source.mute

~obj.changed(\stop, 0);
a.asStream === a.asStream


(
~pcontinue = { arg seq, continue;
	Pspawner({ arg spa;
		block { arg break;
			loop {
				spa.seq(seq);
				if( continue.value.not ) { break.value };
			};
		}
	});
};
~cont = Ref(true);
~pcontinue.(Pseq([a,b]),~cont).play;
)
~cont.value = false;

(
var patterns;
a = Pbind(\note, Pseq([1,2,3,4],5), \dur, 0.5);
b = Pbind(\note, Pseq([1,2,3,4,5,6,7,8]+10,5), \lag, 0.1, \dur, 0.5);
x = PatternProxy.new;
y = PatternProxy.new;
x.source = a;
y.source = b;
patterns = [x,y];
p = Pspawner({ |spawner|
       var streams = List.new;
       patterns.do { |pattern|
               var stream;
               stream = CleanupStream(pattern.asStream, {
                       streams.remove(stream);
					   "plop".postln;
               });
               streams.add(stream);
               spawner.par(stream);
       };
});


z = p.play;
k = SimpleController(z);
k.put(\stopped, { "ploiiip".debug });
)

z.stop

(
~find_children = { arg main, node;
	var res = Set.new;
	if([\parnode, \seqnode].includes(node.kind) ) {
		node.get_children.do { arg childname;
			if( res.includes(childname) ) {
				childname.debug("find_children: loop");
			} {
				~notNildo.(main.get_node(childname), { arg child;
					res.addAll(thisFunction.(main, child))
				});
			};
		}
	} {
		if( res.includes(node.uname) ) {
			node.uname.debug("find_children: loop");
		} {
			res.add(node.uname);
		};
	};
	res;
};

//~bla = Dictionary.new;
//~main = (get_node: { arg self, node; ~bla[node] });
//~bla[\haha] = (



)


a = (bla:4);
x = (blax:9);
b = (parent:[x,a], rah:5);
c = (parent:a, rah:5);
b.bla = 7
b.bla
c.bla
a.bla

(
var patterns;
~t = Pif(Pkey(\muted) > 0, \rest, \note);
a = Pbind(\muted, Pfunc({ arg ev; ev[\muted].debug("ra") ?? 0}), \type, ~t, \note, Pseq([1,2,3,4],1), \dur, 0.5);
b = Pbind(\muted, 0, \type, ~t, \note, Pseq([1,2,3,4,5,6,7,8]+10,1), \lag, 0.1, \dur, 0.5);
x = PatternProxy.new;
y = PatternProxy.new;
x.source = a;
y.source = b;
patterns = [x,y];
p = Pspawner({ |spawner|
       var streams = List.new;
       patterns.do { |pattern|
               var stream;
			   "bla".debug;
               spawner.par(pattern);
			   "endbla".debug;
       };
});
p = p <> Pbind(\muted, 1);
//p = Psetpre(\muted, 1, p);

//z = p.play;
z = p.play;
k = SimpleController(z);
k.put(\stopped, { "ploiiip".debug });
)
s.boot
