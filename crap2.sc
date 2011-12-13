
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

	sl_layout = GUI.hLayoutView.new(window, Rect(0,0,1500,60*16));

	~rah.(sl_layout);
	window.front;
	window.view.keyDownAction = { ~rah.(sl_layout); window.view.focus(true) };
	sl_layout.children[1].children[1] = ~make_cell.(sl_layout.children[1], "hahah");


};

~rah = { arg parent;
	var layout;

	parent.removeAll;
	8.do {
		layout = GUI.vLayoutView.new(parent, Rect(0,0,(160),60*16));

		16.do {

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










(
  SynthDef(\bassD, {|out = 0, hit_dur, amp, pan|
 
    var ringmod, noise, lpf, hpf, lpf_env, hpf_env, noise_env, env, panner, 
      pitch_env, slew, trig, sh;
  
  
    lpf_env = EnvGen.kr(Env.perc(0.05, 56.56, 12, -4));
    hpf_env = EnvGen.kr(Env.perc(0.05, 48.54, 12, -6));
    noise_env = EnvGen.kr(Env.perc(0.0001, 0.032, 1, -8));
    pitch_env = EnvGen.kr(Env.perc(0.07, hit_dur, 12, -2));
  
    env = EnvGen.kr(Env.perc(0.00005, hit_dur, amp, -2), doneAction: 2);
  
   trig = Impulse.ar(0.45/hit_dur, 1.0.rand);
  sh = Dwhite(-6, 6,inf);
  slew =  Lag.ar(Demand.ar(trig, 0, sh), hit_dur/1.7);
  
    ringmod = LFTri.ar(
        (31 + slew + LFTri.ar((27 + pitch_env).midicps, 4.0.rand, 60)).midicps, 
        4.0.rand); // 5 octave log range
    noise = PinkNoise.ar(noise_env);

    lpf = RLPF.ar(ringmod, (56.56 + lpf_env).midicps, 0.5);
    hpf = RHPF.ar(lpf +  noise, (48.54 + hpf_env).midicps, 0.5);
  
    panner = Pan2.ar(hpf, pan, env);
  
    Out.ar(out, panner);
  }).store;
)

s.boot

(

 Conductor.make({arg cond, freq1, freq2, lpf_f, hpf_f, lpf_d, hpf_d, noise_d, dur, db;
  freq1.spec_(\freq, 200+660.rand);
  freq2.spec_(\freq, 200+660.rand);
  lpf_f.spec_(\freq, 100+200.rand);
  hpf_f.spec_(\freq, 200+660.rand);
  lpf_d.sp(1, 0.0001, 1.5, 0, 'linear');
  hpf_d.sp(1, 0.0001, 1.5, 0, 'linear');
  dur.sp(1, 0.0001, 2, 0, 'linear');
  noise_d.sp(0.1, 0.00001, 1, 0, 'linear');
  db.spec_(\db, 02.ampdb);
  

  
  cond.pattern_(
   Pbind(
    \instrument, \bassD,
    \db,   db,
    \freq1,   freq1,
    \freq2,  freq2,
    \lpf_f,   lpf_f,
    \hpf_f,   hpf_f,
    \lpf_d,   lpf_d,
    \hpf_d,   hpf_d,
    \noise_d,  noise_d, 
    \dur,  dur    
   )
  )
 }).show;
)




(

 SynthDef(\bassD, {|out = 0, freq1, freq2, lpf_f, hpf_f, lpf_d, hpf_d, noise_d, dur, amp|
 
  var ringmod, noise, lpf, hpf, lpf_env, hpf_env, noise_env, env, panner, 
   pitch_env, slew, imp;
  
  
  lpf_env = EnvGen.kr(Env.perc(0.05, lpf_d, 12, -4));
  hpf_env = EnvGen.kr(Env.perc(0.05, hpf_d, 12, -6));
  noise_env = EnvGen.kr(Env.perc(0.0001, noise_d, 1, -8));
  pitch_env = EnvGen.kr(Env.perc(0.07, dur, 12, -2));
  
  env = EnvGen.kr(Env.perc(0.00005, dur, amp, -2), doneAction: 2);
  
  imp = Dust.ar(0.45/dur, 12) - 6;
  slew =  Lag.ar(imp, dur/1.7);
  
  ringmod = LFTri.ar((freq2.cpsmidi + slew + 
     LFTri.ar((freq1.cpsmidi + pitch_env).midicps, 4.0.rand, 60)
    ).midicps, 4.0.rand); // 5 octave log range
  noise = PinkNoise.ar(noise_env);

  lpf = RLPF.ar(ringmod, (lpf_f.cpsmidi + lpf_env).midicps, 0.5);
  hpf = RHPF.ar(lpf +  noise, (hpf_f.cpsmidi + hpf_env).midicps, 0.5);
  
  panner = Pan2.ar(hpf, 0, env);
  
  Out.ar(out, panner);
 }).store;

 Conductor.make({arg cond, freq1, freq2, lpf_f, hpf_f, lpf_d, hpf_d, noise_d, dur, db;
  freq1.spec_(\freq, 200+660.rand);
  freq2.spec_(\freq, 200+660.rand);
  lpf_f.spec_(\freq, 100+200.rand);
  hpf_f.spec_(\freq, 200+660.rand);
  lpf_d.sp(1, 0.0001, 1.5, 0, 'linear');
  hpf_d.sp(1, 0.0001, 1.5, 0, 'linear');
  dur.sp(1, 0.0001, 2, 0, 'linear');
  noise_d.sp(0.1, 0.00001, 1, 0, 'linear');
  db.spec_(\db, 02.ampdb);
  

  
  cond.pattern_(
   Pbind(
    \instrument, \bassD,
    \db,   db,
    \freq1,   freq1,
    \freq2,  freq2,
    \lpf_f,   lpf_f,
    \hpf_f,   hpf_f,
    \lpf_d,   lpf_d,
    \hpf_d,   hpf_d,
    \noise_d,  noise_d, 
    \dur,  dur    
   )
  )
 }).show;
 
)




(
SynthDef(\mybass1, { arg out=0, pan=0, gate=1, preamp=0.1, amp=0.1, freq=20, freq_fc=100;
	
	var ou;

	ou = SinOsc.ar(freq);
	ou = LPF.ar(ou, freq_fc);
	ou = ou * EnvGen.ar(~make_adsr.(\adsr),gate,doneAction:2);
	ou = Pan2.ar(ou,pan,preamp);
	ou = Limiter.ar(ou, amp);
	Out.ar(out, ou);

}).add;
)
(
Pbind(
	\instrument, \mybass1,
	\dur, 1
).play
)

s.boot


(
SynthDef(\sin1, { arg out=0, pan=0, gate=1, sustain=0.5, amp=0.1, freq=200;
	
	var ou;

	ou = Pulse.ar(freq);
	ou = ou * EnvGen.ar(Env.linen(0.01,sustain,0.01,1),doneAction:2);
	ou = Pan2.ar(ou,pan,amp);
	Out.ar(out, ou);

}).add;
SynthDef(\sin2, { arg out=0, pan=0, gate=1, sustain=0.5, amp=0.1, freq=200,
					maxdtime=0.2, dtime=0.2, decay=2;
	
	var ou;

	ou = Pulse.ar(freq);
	ou = ou * EnvGen.ar(Env.linen(0.01,sustain,0.01,1),doneAction:2);
	ou = Pan2.ar(ou,pan,amp);
	ou = CombL.ar(ou, maxdtime, dtime, decay, 1, ou);
	Out.ar(out, ou);

}).add;

SynthDef(\lpf, { arg in=0, out=0, gate=1, freqfm=4, freq_fc=200;
	
	var ou;

	ou = In.ar(in,2);
	ou = LPF.ar(ou, SinOsc.ar(freqfm)*50+freq_fc);
	Linen.kr(gate,0,1,0,doneAction:2);
	//DetectSilence.ar(ou,0.001,0.01,doneAction:2);
	Out.ar(out, ou);

}).add;

SynthDef(\echo, { arg out=0, in=0, maxdtime=0.2, release=1, dtime=0.2, decay=2, gate=1;
        var env, ou;
        env = Linen.kr(gate, 0.05, 1, decay, 14);
        in = In.ar(in, 2);
		ou = CombL.ar(in, maxdtime, dtime, decay, 1, in);
		//DetectSilence.ar(ou,0.001,0.1,doneAction:2);
        Out.ar(out, ou);
}, [\ir, \ir, \ir, 0.1, 0.1, 0]).add;


)


(
~gen = Group.new;
~effects = Group.new(~gen, \addAfter);
~bus = Bus.audio(s, 2);
)

Synth(\lpf, [\in, ~bus],target:~effects);
Synth(\sin1, [\out, ~bus], target: ~gen);
Synth(\sin1, [\out, 0], target: ~gen);

~bus

(
~gen = Group.new;
~effects = Group.new(~gen, \addAfter);
~bus = Bus.audio(s, 2);
~bus2 = Bus.audio(s, 2);
Ppar([
	Pbind(
		\instrument, \sin1,
		\group, ~gen,
		\out, ~bus,
		\dur, 1
	),
	Pbind(
		\instrument, \lpf,
		\in, ~bus,
		\group, ~effects,
		\dur, 1
	),
]);
Ppar([
	Pbind(
		\instrument, \sin1,
		\freq, 150,
		\sustain, 0.01,
		\group, ~gen,
		\out, 0,
		\out, ~bus2,
		\dur, 0.5
	),
	Pbind(
		\instrument, \echo,
		//\freqfm, 2,
		\dtime, 0.1,
		\in, ~bus2,
		\group, ~effects,
		\dur, 5
	),
]).play;
)
XOut

s.queryAllNodes; // note the default group (ID 1)
s.boot

(
)
Node
(
~sp = { arg pattern, effects, patfx;
	var ef, bu;
	Pspawner({ |spawner|
		bu = Bus.audio(s,2);
		ef = Synth(effects, [\in, bu, \out, 0], target:1, addAction:\addAfter);
		spawner.seq(Ppar([
			Pset(\id, ef.nodeID, patfx),	
			Pset(\out, bu, pattern),
		]));
		ef.release
	});
};


)

(
~a = EventPatternProxy.new;
~a.source =
	Pbind(
		\instrument, \sin1,
		\freq, 250,
		\sustain, 0.1,
		\out, 0,
		\dur, 1.5
	);

~b = Pbind(
	\type, \set,
	\dtime, 0.051,
	\instrument, \echo
);
~x = ~sp.(~a.source, \echo, ~b);
~x.play;
)

(
a = Synth(\sin1, [\sustain,10]);
Pbind(
	\type, \set,
	\id, a.nodeID,
	\instrument, \sin1,
	\freq, Pseq([1,2,3,4],inf)*100,
	\dur, 1
).play;
)


(
b = Bus.audio(s,2);
g = Group.after(1);
x = ~short_ppar.([
	Pbind(
		\type, \note,
		\instrument, \sin1,
		\out, b,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4])*100,
		\dur, 0.5
	),
	Pmono(
		\echo,
		\in, b,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 9,
		\dtime, 0.1,
		\group, g
	)
]).play;
~sc = SimpleController(x);
~sc.put(\stopped, {
	"clear".debug;
	g.free;
	b.free
});

)


(
~short_ppar = { arg list;
	Pspawner({ |spawner|
		var str;
		list.do({ arg pat;
			str = CleanupStream(pat.asStream, {
				spawner.suspendAll;
			});
			spawner.par(str);
		})
	});
};
)
EnvGen
(
~pfx = { arg pat, effects;
	Pspawner({ |spawner|
		var str, bus, group;
		bus = Bus.audio(s,2);
		group = Group.after(1);
		pat = Pset(\out, bus, pat);
		effects = Pset(\in, bus, effects);
		effects = Pset(\group, group, effects);
		str = CleanupStream(pat.asStream, {
			spawner.suspendAll;
			bus.free;
		});
		spawner.par(str);
		spawner.par(effects)
	});
};
~pfx2 = { arg pat, effects;
	Pspawner({ |spawner|
		var str, pbus, pgroup, leffect;
		pbus = Bus.audio(s,2);
		pat = Pset(\out, pbus, pat);
		str = CleanupStream(pat.asStream, {
			spawner.suspendAll;
			pbus.free;
		});
		spawner.par(str);
		pgroup = 1;
		effects[..effects.size-2].do { arg ef;
			ef = Pset(\in, pbus, ef);
			pbus = Bus.audio(s,2);
			ef = Pset(\out, pbus, ef);
			pgroup = Group.after(pgroup);
			ef = Pset(\group, pgroup, ef);
			spawner.par(ef)
		};
		leffect = effects.last;
		leffect = Pset(\in, pbus, leffect);
		pgroup = Group.after(pgroup);
		leffect = Pset(\group, pgroup, leffect);
		spawner.par(leffect)
	});
};
~pfx3 = { arg pat, controls, effects;
	Pspawner({ |spawner|
		var str, pbus, pgroup, leffect;
		pbus = Bus.audio(s,2);
		pat = Pset(\out, pbus, pat);
		str = CleanupStream(pat.asStream, {
			spawner.suspendAll;
			pbus.free;
		});
		spawner.par(str);
		pgroup = 1;
		effects[..effects.size-2].do { arg ef;
			ef = Pset(\in, pbus, ef);
			pbus = Bus.audio(s,2);
			ef = Pset(\out, pbus, ef);
			pgroup = Group.after(pgroup);
			ef = Pset(\group, pgroup, ef);
			spawner.par(ef)
		};
		leffect = effects.last;
		leffect = Pset(\in, pbus, leffect);
		pgroup = Group.after(pgroup);
		leffect = Pset(\group, pgroup, leffect);
		spawner.par(leffect)
	});
};
)

s.queryAllNodes; // note the default group (ID 1)

(
x = ~pfx.(
	Pbind(
		\type, \note,
		\instrument, \sin1,
		//\out, b,
		//\group, g,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4])*100,
		\dur, 0.1
	),
	Pmono(
		\echo,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 5,
		\dtime, 0.1
		//\group, g
	)
);

x.play;
)
Ppar([Pseq([x,Event.silent(5)],inf),y]).play

(
x = ~pfx2.(
	Pbind(
		\type, \note,
		\instrument, \sin1,
		//\out, b,
		//\group, g,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4])*100,
		\dur, 0.1
	),
	[
	Pmono(
		\echo,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 1,
		\dtime, 0.1
		//\group, g
	),
	Pmono(
		\echo,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 10,
		\dtime, 0.51
		//\group, g
	)
	]
);

x.play;
)
s.queryAllNodes; // note the default group (ID 1)
s.boot
~a = EventPatternProxy.new;
~a.source = x;
~a.play
~a.stop
(
~a = EventPatternProxy.new;
~b = EventPatternProxy.new;
~p = Pbind(\degree, Pseq([0,1,2,3,4,5],inf), \dur, 0.3, \legato, 0.2);
~q = Pbind(\degree, Pseq([0,1,2,3,4,5]-2,inf), \dur, 0.6, \legato, 0.1, \amp, 0.5);
~ef1 = Pmono(
		\echo,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 5,
		\dtime, 0.1
	);
~ef2 = Pmono(
		\echo,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 1,
		\dtime, 0.2
	);
)
(
~a.source = ~pfx.(~p, ~ef1);
~b.source = ~pfx.(~q, ~ef2);
~a.play;
~b.play;
)
Bu
Bus.freeAll


(
	y = Pbind(
		\type, \note,
		\instrument, \sin1,
		\out, 0,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4],inf)*100,
		\dur, 1
	);
	y.play
)



a = Bus.new(\audio, 16, 2)
a.free
a.index

b = Bus.audio(s,2);
Group


(
SynthDef(\echo, { arg out=0, maxdtime=0.2, dtime=0.2, decay=2, gate=1;
        var env, in;
        env = Linen.kr(gate, 0.05, 1, decay, 2);
        in = In.ar(out, 2);
        XOut.ar(out, env, CombL.ar(in * env, maxdtime, dtime, decay, 1, in));
}, [\ir, \ir, 0.1, 0.1, 0]).add;

SynthDef(\distort, { arg out=0, pregain=40, amp=0.2, gate=1;
        var env;
        env = Linen.kr(gate, 0.05, 1, 0.1, 2);
        XOut.ar(out, env, (In.ar(out, 2) * pregain).distort * amp);
}, [\ir, 0.1, 0.1, 0]).add;

SynthDef(\wah, { arg out=0, gate=1;
        var env, in;
        env = Linen.kr(gate, 0.05, 1, 0.4, 2);
        in = In.ar(out, 2);
        XOut.ar(out, env, RLPF.ar(in, LinExp.kr(LFNoise1.kr(0.3), -1, 1, 200, 8000), 0.1).softclip * 0.8);
}, [\ir, 0]).add;
)



(
g = Group.new;
h = Group.before(g);
x = ~short_ppar.([
	Pbind(
		\type, \note,
		\instrument, \sin1,
		//\out, b,
		\group, g,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4])*100,
		\dur, 0.1
	),
	Pmono(
		\echo,
		//\in, b,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 5,
		\group, g,
		\dtime, 0.1
		//\group, g
	)
]);

Ppar([
	x,
	Pbind(
		\type, \note,
		\instrument, \sin1,
		//\out, b,
		\sustain, 0.1,
		\group, h,
		\amp, 1,
		\freq, Pseq([1,2,3,4])*110,
		\dur, 0.5
	),

]).play;
)

(
g = Group.before(1);
b = Bus.audio(s,2);
x = ~short_ppar.([
	Pbind(
		\type, \note,
		\instrument, \sin1,
		//\out, b,
		//\group, g,
		\out, b,
		\sustain, 0.1,
		\freq, Pseq([1,2,3,4])*100,
		\dur, 0.1
	),
	Pmono(
		\echo,
		\in, b,
		\release, 1,
		\dtime, Pseq([0.3,0.2,0.1],inf),
		\decay, 5,
		\group, g,
		\dtime, 0.1
		//\group, g
	)
]);

x.play;
)
s.queryAllNodes; // note the default group (ID 1)
Bus






h.query.debug("plo");
h.isPlaying
Node
(
Pbind(
	\type, \note,
	\instrument, \sin1,
	//\out, b,
	\sustain, 0.1,
	\group, h,
	\amp, 0.5,
	\freq, Pseq([1,2,3,4],inf)*110,
	\dur, 0.5
).play
)



a = PathName.new("sounds");
a.files.do { arg x; ["wav"].includesEqual(x.extension).debug }
a.files.do { arg x; ("wav" === x.extension).debug }

(
SynthDef(\sin5, { arg out=0, pan=0, gate=1, width=0.5,sustain=0.5, amp=0.1, freq=200;
	
	var ou;

	ou = Pulse.ar(freq,width);
	ou = ou * EnvGen.ar(Env.linen(0.01,sustain,0.01,1),doneAction:2);
	ou = Pan2.ar(ou,pan,amp);
	Out.ar(out, ou);

}).add;
)
(
a = Pbind(
	\type, \note,
	\instrument, \sin1,
	//\out, b,
	\sustain, 0.1,
	\sustain, Pseq([0.1,0.3],2),
	\amp, 0.5,
	\freq, Pseg(Pseq([1,2,3,4],inf),0.7,0)*110,
	\dur, 0.5
);
Pn(a).play
)


(
a = Signal.newClear(512);
a.waveFill({ arg x, i; sin(x).max(0) }, 0, 3pi);
a.plot;
)

s.boot

a = [1, 6, 2, -5, 2].plot2;
a.editMode = true;


b = Buffer.alloc(s,1024)
(1..2).sum


x= 1
(1..2).collect({arg k; sin(x*k)}).sum
sin(x)+sin(2*x)
(
a = Signal.newClear(512);
a.waveFill({ arg x, i; sin(x)+sin(2*x) }, 0, 2pi);
a.waveFill({ arg x, i; (1..6).collect({arg k; sin((x*sin(x+k))*k)}).sum }, 0, 3pi);
a.plot;
b.loadCollection(a.asWavetable);

SynthDef("help-Osc",{ arg out=0,freq=100,bufnum=0;
		var ou,fou,ou2;
		//fou = Osc.kr(bufnum, 0.1, 0, 0.5)*90+100;
		fou = SinOsc.kr(43.5)*90+freq;
		ou = Osc.ar(bufnum, [0.1,-1,0]+(freq+(SinOsc.kr(1)*5)), 0, 0.5).sum;
		//ou = Pulse.ar(200);
		ou = RLPF.ar(ou, fou.lag(0.1),0.03);
		//ou = BPF.ar(ou, 200,SinOsc.kr(488)+1.1*0.3);
		ou2 = BPF.ar(ou, freq*1.5,SinOsc.kr(5,0.1)+1.1*0.7);
		ou = BPF.ar(ou, freq*0.5,SinOsc.kr(4)+1.1*0.7)+ou2;
		ou = ou ! 2;
        Out.ar(out,ou)

}).play(s,[\out, 0, \bufnum, b.bufnum]);
)



// embedding in another GUI
(
w = Window("plot panel", Rect(20, 30, 520, 250));
Slider.new(w, Rect(10, 10, 490, 20)).resize_(2).action_ { |v|
		v.value.debug("val");
        a.value = (0..(max(v.value,0.1) * 80).asInteger).scramble;
        w.refresh;
};
z = CompositeView(w, Rect(10, 35, 490, 200)).background_(Color.rand(0.7)).resize_(5);
a = Plotter("plot", parent: z).value_([0, 1, 2, 3, 4].scramble * 100);
w.front;
)

(0..(0.51*80).asInteger)

(
a = Plotter("the plot", Rect(600, 30, 600, 400));
a.value = (0..100).normalize(0, 8pi).sin;
)

a.value = { |i| (0..90) % (i + 12) + ( (0..90) % (i + 2 * 1) ) }.dup(3);
a.value = (0..12).squared;
a.plotMode = \points; a.refresh;
a.plotMode = \levels; a.refresh;
a.plotMode = \plines; a.refresh;

a.domainSpecs = [[0, 115, \lin, 1]]; a.refresh;

a.parent.close; // close window
a.makeWindow;   // open it again

a.refresh
a.value = { (0..70).scramble }.dup(3);
a.plotMode = \linear; a.refresh;
a.value = { |i| (0..2000).normalize(0, 4pi + i).sin } ! 4; // lots of values, test efficiency
a.value = { |i| (0..10000).normalize(0, 8pi + i).sin } ! 3; // lots of values, test efficiency
a.value = { (0..140).scramble } ! 7;

a.value = { |i| (0..90).normalize(0, 8pi + (i*2pi)).sin } ! 2 * [400, 560] + 700;
a.value = { |i| (_ + 2.0.rand).dup(100).normalize(0, 8pi + i).sin } ! 2 * 400 + 700;


// multi channel expansion of single values
a.value = { |i| (_ + 2.0.rand).dup(100).normalize(0, 8pi + i).sin *.t [1, 2, 3] } ! 2 * 400 + 700;
a.value = { |i| (0..10) **.t [1, 1.2, 1.3, 1.5] * (3.5 ** i) }.dup(3);

a.parent.bounds = Rect(400, 100, 500, 700);
a.parent.bounds = Rect(600, 30, 500, 300);

a.superpose = true;
a.value = { |i| (0..20) * (3.5 ** i) }.dup(5);
a.superpose = false;

// specs

a.value = (50..90).midicps.scramble;
a.specs = \freq; a.refresh;
a.value = (1..60).scramble.neg;
a.specs = \db; a.refresh;

a.value = { |i| { exprand(1e3, (10 ** (i + 8))) }.dup(90) }.dup(3);
a.value = { { exprand(1e3, 1e9) }.dup(90) }.dup(3);
a.specs = [[1e3, 1e10, \exp], [1e3, 1e20, \exp], [1e3, 1e30, \exp]]; a.refresh;
a.domainSpecs = [[0, 5], [-8, 100], [-1, 1]]; a.refresh;


// Array:plot
(
a = (4 ** (-5..0)).postln.plot2;
a.specs = \delay; a.refresh;
a.domainSpecs = [0, 10, \lin, 0, 0, " Kg"].asSpec; a.refresh;
);

a.domainSpecs = [0.1, 10, \exponential, 0, 0, " Kg"].asSpec; a.refresh;
a.domainSpecs = [-10, 10, \lin, 0, 0, " Kg"].asSpec; a.refresh;


a = [(0..100) * 9, (200..1300) * 2, (200..1000)/ 5].plot2;
a.superpose = true;

a = [[0, 1.2, 1.5], [0, 1.3, 1.5, 1.6], [0, 1.5, 1.8, 2, 6]].midiratio.plot2;
a.plotMode = \levels; a.refresh;
a.superpose = false;


// Function:plot
a = { SinOsc.ar([700, 357]) * SinOsc.ar([400, 476]) * 0.2 }.plot2;
a = { SinOsc.ar([700, 357] *0.02) * SinOsc.ar([400, 476]) * 0.3 }.plot2(0.2, minval: -1);
a = { SinOsc.ar(440) }.plot2(1);


// Env:plot
Env.perc(0.4, 0.6).plot2;
Env.new({ 1.0.rand2 }! 8, { 1.0.rand } ! 7, \sin).plot2;

// Buffer:plot
b = Buffer.read(s, "sounds/SinedPink.aiff");
                // "sounds/SinedPink.aiff" contains SinOsc on left, PinkNoise on right
b.plot2;
b.free;
