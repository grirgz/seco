
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
	res.sort({ arg a, b; a.time < b.time });
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
