
(
SynthDef(\scansynth1, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var in, ou;
	var spring = [01,1.2,1,05,0.5,0.5,8];
	var damp = [0.8,0.7,0.1,0.2,0.1,0.2,0.9];
	var springs;
	var sel;
	in = LocalIn.ar(1) * 0.95 + Trig.ar(gate, 0.01);
	ou = in;
	springs = spring.collect { arg spr, i;
		ou = Spring.ar(ou, spr, damp[i]);
	};
	LocalOut.ar(ou);
	sel = LFSaw.ar(freq).range(0, spring.size-1);
	ou = SelectX.ar(sel, springs);
	ou = ou * EnvGen.ar(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)

(
Pdef(\plop, Pbind(
	\instrument, \scansynth1,
	\degree, Pseq([0,2,4],inf),
	\dur, 1,
	\amp, 0.1
)).play;
)



{ SinOsc.ar }.play


w = Window.new;
~tl = MxTimeGui.new
~tl.gui(w)
w.front

(
~model = (
		channels: [
			(units:[])
		]
		up:
);
~tl = MxTimeGui(~model);
~tl.gui;
)




(
SynthDef(\scansynth1, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
    var in, ou;
    var spring = (7.42).dup(10);
    var damp = (0.7764812).dup(10);
    var springs;
    var sel;
	spring = 0.1;
	damp = spring/01;
	spring = spring.dup(10);
	damp = damp.dup(10);
    in = LPF.ar(LocalIn.ar(1), XLine.ar(1,05,0.8)) * 0.90 + (LPF.ar(Trig.ar(gate, 0.01),010) * WhiteNoise.ar.range(0.98,1));
    ou = in;
    springs = spring.collect { arg spr, i;
        ou = Spring.ar(ou, spr, damp[i]);
    };
    LocalOut.ar(ou);
    //sel = VarSaw.ar(freq * [0.999,1,0.499,2,1.01] * 0.5, width: LFNoise2.ar(1/2).range(0.2,0.5)).range(0, spring.size-1);
	sel = LFSaw.ar(freq * [0.999,1,0.499,2,1.01] * 1.0).range(0, spring.size-1);
	ou = SelectX.ar(sel, springs);
	ou = ou.sum;
    //ou = Splay.ar(ou, 1);
    ou = ou * EnvGen.ar(\adsr.kr(Env.adsr(0.3,0.1,0.8,0.8)),gate,doneAction:2);
	ou = ou * amp;
    ou = Pan2.ar(ou, pan, 1);
    Out.ar(out, ou);
}).add;
)

(
Pdef(\plop, Pbind(
    \instrument, \scansynth1,
    \scale, Scale.minor,
    //\degree, Pseq([0,2,4],inf) + [0,2,4,-12],
	\degree, Pseq([0,2,4],inf),
    \strum, 1/8,
    \dur, 1,
    \amp, 2,
    \legato, 0.5
)).play;
)







0
:

(N:"plop")



(
SynthDef(\scansynth1, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
    var in, ou;
    var spring = (0.12).dup(40);
    var damp = (0.0564812).dup(40);
    var springs;
    var sel;
    in = LPF.ar(LocalIn.ar(1), XLine.ar(1,10,0.3)) * 0.97 + (LPF.ar(Trig.ar(gate, 0.01),200) * WhiteNoise.ar.range(0.98,1));
    ou = in;
    springs = spring.collect { arg spr, i;
        ou = Spring.ar(ou, spr, damp[i]);
    };
    LocalOut.ar(ou);
    sel = VarSaw.ar(freq * [0.999,1,0.499,2,1.01] * 0.5, width: LFNoise2.ar(1/2).range(0.2,0.5)).range(0, spring.size-1);
    ou = Splay.ar(SelectX.ar(sel, springs), 1);
    ou = ou * EnvGen.ar(\adsr.kr(Env.adsr(0.3,0.1,0.8,0.1)),gate,doneAction:2);
    ou = Pan2.ar(ou, pan, amp);
    Out.ar(out, ou);
}).add;
)

(
Pdef(\plop, Pbind(
    \instrument, \scansynth1,
    \scale, Scale.minor,
    \degree, Pseq([0,2,4],inf) + [0,2,4,-12],
    \strum, 3/8,
    \dur, 3,
    \amp, 2,
    \legato, 1.4
)).play;
)




 // http://twitter.com/#!/alln4tural/status/99846300173991936
// http://soundcloud.com/tengototen/esoteric-tweet


fork{loop{h=[5,7,8].choose*(2**(2..8).choose);play{Splay.ar({SinOsc.ar(exprand(h,h+(h/64)),0,0.1)}!64)*LFGauss.ar(9,1/4,0,0,2)};2.wait}};

// really i wanted to do this:
fork{loop{h=([33,38,40].choose.midicps)*(2**((0 .. 5).choose));play{Splay.ar({SinOsc.ar(exprand(h-(h/256),h+(h/256)),0,0.1)}!64)*LFGauss.ar(19,1/4,0,0,2)};4.wait}};
// or more like a constant drone:
fork{5.do{h=([33,38,40].choose.midicps)*(2**((0 .. 4).choose));play{Splay.ar({SinOsc.ar(exprand(h-(h/64),h+(h/64)),0,0.1)}!8)*LFGauss.ar(19,1/4,0,0,2)};0.25.wait}};
// primes
fork{loop{h=(4 .. 100).choose.nthPrime*(2**(0..3).choose);play{Splay.ar({SinOsc.ar(exprand(h-(h/256),h+(h/256)),0,0.1)}!64)*LFGauss.ar(19,1/4,0,0,2)};2.wait}}; 
// Fibonacci
fork{loop{h=(List.fib(15).choose)*(2**(0..4).choose);play{Splay.ar({SinOsc.ar(exprand(h-(h/64),h+(h/64)),0,0.1)}!64)*LFGauss.ar(19,1/4,0,0,2)};2.wait}}; 

// but they were too long.
// __________________________
// inspired by http://sctweets.tumblr.com/post/8379573991/sinosc 
// (http://soundcloud.com/rukano)



Pspawner{|sp|6.do{|i|sp.par(Pbind(*[degree:Pseq(((0..i)*2),inf),octave:7-i,dur:0.2*(2**i)]))};sp.seq}.trace.play //#supercollider #sc140



:a!



(

var seed = thisThread.randSeed = 1e8.rand.debug('seed');

{|dur = 30|

	var voices = 2.pow(1.rrand(4)),

	sig = DelayC.ar({ Pan2.ar(

		PMOsc.performList(\ar, {

			var freq = Lag.ar(LFSaw.ar([10,15]/((1..5).choose*[1,2,4].choose)).exprange(

				{200.rrand(400)}!2, {500.rrand(1100)}!2

				* Line.kr(0.5.rrand(2), 3.rrand(8.0),dur)

			));

			[freq, freq/[2,4].choose + SinOsc.kr([10,25],0,5), XLine.kr(0.1,1.rrand(3.0),dur/1.rrand(2.0))]

		}.()).sum/2,

		[SinOsc,LFTri].choose.kr({4.0.rrand(11)}!2 * [XLine,Line].choose.kr(1,4,dur))

	).sum/2 

	* LFGauss.kr(XLine.kr(\beat.kr, \beat.kr / \beat_change.kr, dur) / [0.5,1,2,4].choose, 0.3) 

	* EnvGen.kr([

		Env.perc(0.1,dur),

		Env.sine(dur),

		Env.performList(\new, {|len| [

			[0]++Array.rand(len,0.1,1)++[0],

			Array.rand(len,0.1,1).normalizeSum * dur,

			Array.rand(len-1,-3.0,3.0)+[0]

		]}.(2.rrand(10)))

	].choose) } ! voices, 4, { 0.1.rrand(4) } ! voices, 1 / voices).sum * Line.kr(1,1,dur+5,doneAction:2);

	sig = Rotate2.ar(sig[0],sig[1], LFSaw.kr(Duty.kr(dur/4.rrand(16), 0, Drand([-1*\beat.kr,\beat.kr].reciprocal,inf))));

}.play(args:[

	dur:1.rrand(25.0).debug('dur'), 

	beat:1.rrand(6)*(1..4).reciprocal.choose/4, 

	beat_change:1.rrand(8)

])

)



// really early first approach...
(
x.free;
x = {
	var note = Duty.kr(
		6/3.2,
		0, 
		Dseq([45, 45, 48, 41], inf));
	var snd = Saw.ar(note.midicps * SinOsc.kr(7.421).range(0.998, 1.001)) * -12.dbamp!2;
	var trigs = TDuty.kr(
		Dseq([1, 2, 3]/3.2, inf),
		0,
		1);
	var lvls = Duty.kr(
		Dseq([1, 2, 3]/3.2, inf),
		0,
		Dseq([-9, -3, -6], inf));
	snd = snd * lvls.dbamp.lag2(0.1);
	snd = snd * EnvGen.kr(Env.perc(0.3, 0.3), trigs);
	snd = RLPF.ar(snd, EnvGen.kr(Env.perc(0.5, 0.2), trigs) * 3201 + 2311, 0.2);
	snd = LPF.ar(snd, 3000);
	snd = snd * (1 + Latch.ar(WhiteNoise.ar(-8.dbamp), Dust.ar(830))).lag2(0.01);
	snd = snd + LPF.ar(CombC.ar(snd, 0.4, 0.33, 0.52, -4.dbamp), 932);
	snd = FreeVerb2.ar(snd, snd, 0.21, 0.93, 0.9);
	snd * -12.dbamp;
}.play;
)





(
~tasks = (
     N: {
	 	10.do {
            arg i;
            x = Synth(\grain_1);
            0.5.wait;
        }
	},
	P: {
		10.do {
            arg i;
            x = Synth(\grain_2);
            0.5.wait;
		}
	}
);

j = [ \N, \P];

h = Task({
    j.do({ arg i, o;  //
        ~tasks[i].value;
        1.wait;
	})
});

)

h.play;



//--simple example
(
s.waitForBoot{
RedMst.clear;
RedMst.clock= TempoClock(140/60);
RedMst.quant= 8;
RedMstGUI3(20);

RedTrk(
        \bass,
        Pbind(\degree, Pseq([0, 5], inf), \octave, 4),
        #[0, 1, 2, 3]                                                           
);
RedTrk(
        \melody,
        Pbind(\degree, Pseq([0, 5, 2, 3, 3, 1], inf), \dur, Pseq([0.25, 0.25, 0.5], inf)),
        #[1, 2, 5]
);
RedTrk(
        \melody2,
        Pbind(\degree, Pseq([7, 6, 4, 3], inf), \dur, 2, \octave, 6),
        #[2, 3]
);
}
)

RedMst.play;
RedMst.next;
RedMst.next;
RedMst.quant= 2;

RedMstGUI(64);                                          //add more guis
RedMstGUI(24, GUI.skins.default);
RedMstGUI(9);

RedMst.next;
RedMst.stop;
RedMst.clear;





(
        {|freq = 080, rq=0.08|
                        Out.ar(0,
                                Pan2.ar(
                                        BPF.ar(WhiteNoise.ar * Line.kr(5, 0, 0.02), freq, rq)
                                )
                        )
        }.play;
)



(
Instr(\plop, { arg amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	ou = SinOsc.ar(freq);
	ou = ou * EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
}).addSynthDef;

)

Mdef.main

(
t = TempoClock.new;
~book = Mdef.main.node_manager.make_groupplayer();
8.do {
	var song = ~make_parplayer.(Mdef.main);
	~book.add_children(song);
	1.do { 
		var section = ~make_parplayer.(Mdef.main);
		song.add_children(section);
		1.do { 
			var variant = ~make_parplayer.(Mdef.main);
			section.add_children(variant);
		}

	}
};
t.beats
)
t
~book = nil


[\song1, \section1, \variant1]
[0,0,0]

~group = (
	selected_child: 0,
	children: SparseArray.newClear(~general_sizes.children_per_groupnode, \voidplayer),

	get_child: { arg self, num;
		var child;
		if(num.isNil) {
			child = self.selected_child;
		} {
			if(num == 0) {
				child = self;
			} {
				child = self.children[num];
				if(child == \voidplayer) {
					main.node_manager.make_groupplayer(\)
				} {

				}
			}
		}
	}
);

~bla = 1000.collect {  ~group.deepCopy };

(


~sm = ~make_song_manager.(Mdef.main);
//~sm.get_path([1]);
)
~sm.get_path([1,1,1],true).uname;
~sm.get_path([1,3,0],true).uname;
~sm.change_section(0).uname
~sm.change_part(1).uname
~sm.get_current_path;

~sm.current_song.get_childname_by_index(1)



(
p = Pbind(
	\instrument, \default,
	\freq, 200,
	\dur, 1,
	\amp, 0.1
).play;
)
p.originalStream.get_receiver


Mdef.node(\s1_part1_sect1_var1).song_path
Mdef.node("s1_part1_sect1_var1").children
~newln = Mdef.main.node_manager.duplicate_livenode(Mdef.node("monosampler_l1084").uname)

Mdef.node(\s1_part1_sect1_var1).add_children(~newln)



