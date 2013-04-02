( 
var w, status, limit, buttons, controls = [], one_button, data, synths, one_synth, synths_generator, 
density_one = 1/4, density_many = 1/10, type_distribution = [0.85, 0.15], // tweak it to get more or less dense pattern grid
task, resolution, direction, pos = 0, step = 1, border = 1, dims = [16,16]; // tweak dims to change size of grid
w = Window("rand-n-step+", Rect(50,250,dims[0]*22+10+250,dims[1]*22+60)); // window init
status = StaticText(w, Rect(5, w.bounds.height - 20, w.bounds.width, 20));
limit = { ReplaceOut.ar(0, Limiter.ar(In.ar(0,2))) }.play( addAction:\addToTail ); // limiter
data = Array2D(dims[1],dims[0]); // prepare data
// and buttons
one_button = { | b, density = 0.1 |
	b.valueAction = 0; // reset
	density.coin.if({ b.valueAction = [1,2].wchoose(type_distribution) }); // tweak it
};
synths = Array.fill(dims[1], { () });
buttons = Array.fill(dims[1], { |l|
	controls = controls.add([ // control buttons
		Button( w, Rect( 10 + (22*dims[0]), 35 + (22*l), 20, 20) ).states_([['m'],['u']]).action_({ |b| // mute / unmute
			synths[l].gate = b.value.asBoolean.not.binaryValue;
		}).mouseOverAction_({ status.string = 'mute/unmute' }),
		Button( w, Rect( 10 + (22*(dims[0]+1)), 35 + (22*l), 20, 20) ).states_([['p']]).action_({ // dice pattern line
			buttons[l].do({ |b| one_button.(b, density_one) }); // tweak it
		}).mouseOverAction_({ status.string = 'randomize pattern' }),
		Button( w, Rect( 10 + (22*(dims[0]+2)), 35 + (22*l), 20, 20) ).states_([['s']]).action_({ // dice one synth
			synths[l] = one_synth.(l);
		}).mouseOverAction_({ status.string = 'randomize synth' }),
		Slider( w, Rect( 10 + (22*(dims[0]+3)), 35 + (22*l), 60, 20) ).action_({ |b| // synth amp
			synths[l].amp = b.value.linexp(0,1,1/16,16);
		}).mouseOverAction_({ status.string = 'tweak synth amp' }),
		Slider( w, Rect( 10 + (22*(dims[0]+3)+60), 35 + (22*l), 60, 20) ).action_({ |b| // synth stretch
			synths[l].stretch = b.value.linexp(0,1,1/8,8);
		}).mouseOverAction_({ status.string = 'tweak synth stretch' }),
		Slider( w, Rect( 10 + (22*(dims[0]+3)+120), 35 + (22*l), 60, 20) ).action_({ |b| // synth pan
			synths[l].pan = b.value.linlin(0,1,-1,1);
		}).mouseOverAction_({ |b| status.string = 'tweak synth pan ' })
	]);
	Array.fill(dims[0], { |i| // grid
		Button( w, Rect( 5 + (22*i), 35 + (22*l), 20, 20) ).states_([ ['-'], ['+'], ['%'] ]).action_({ 
			|b| data[l,i] = b.value 
		}).mouseOverAction_({ status.string = '"%" makes sound with 0.5 probability' }); 
	});
});
// synth gen functions and initialization
one_synth = { |i| // tweak this function to (generate and) return other synthdef names
	var name = 'rstp'++i, pan = -1.0.rand2;
	SynthDef(name, { |index = 0, amp = 1, stretch = 1, pan = 0| // args: horizontal position in grid, amplitude and stretch correction, pan
		var sig = Pan2.ar( // tweak sig to get different sound texture
			PMOsc.ar(80.exprand(10000), 1.exprand(200), 1.exprand(20)),
			pan,
			EnvGen.kr(Env(Array.rand(4, 0, 0.05.rrand(0.4)).add(0), Array.rand(3, 0.1, 1.2).add(0.1), 5.rand2), levelScale: amp, timeScale: stretch, doneAction: 2)
		);
		Out.ar(0, sig);
	}).add;
	controls[i][3].valueAction_(1.explin(1/16,16,0,1));
	controls[i][4].valueAction_(1.explin(1/8,8,0,1));
	controls[i][0].valueAction_(0);
	controls[i][5].valueAction_(pan.linlin(-1,1,0,1));
	(name: name, gate: 1, amp: 1, stretch: 1, pan: pan);
};
synths_generator = { Array.fill(dims[1], { |i| synths[i] = one_synth.(i) } ) };
synths_generator.();
// step task
task = Task({
	inf.do({
		pos = (pos + step).mod(dims[0]);
		dims[1].do({ |l|
			(buttons[l] @@ pos).font_(Font("sans", 20));
			(buttons[l] @@ (pos-step)).font_(Font("sans", 14));
			synths[l].gate.asBoolean.if({
				var args = [index: pos, amp: synths[l].amp, stretch: synths[l].stretch * TempoClock.tempo.reciprocal * resolution.reciprocal, pan: synths[l].pan ];
				switch( data[l,pos],
					1, { Synth(synths[l].name, args) },
					2, { 0.5.coin.if({ Synth(synths[l].name, args) }) }
				);  
			});
		});
		switch( pos,
			0,             { (border == -1 && step == -1).if({ direction.valueAction = 0 }) },
			(dims[0] - 1), { (border == -1 && step ==  1).if({ direction.valueAction = 1 }) }
		);	
		(TempoClock.default.tempo.reciprocal / resolution).yield;
	});
}, AppClock).play(quant:[0]);
// app buttons
Button(w, Rect(5,5, w.bounds.width - 10 / 7, 20)).states_([['reset']]).action_({ |b|
	synths_generator.();
	buttons.flat.do({ |b| one_button.(b, 0) }); // tweak it
}).mouseOverAction_({ status.string = 'reset everything' });
Button(w, Rect(w.bounds.width - 10 / 7 * 1 + 5, 5, w.bounds.width - 10 / 6, 20)).states_([['lucky?']]).action_({ |b| // lazy patterns
	buttons.flat.do({ |b| one_button.(b, density_many) }); // tweak it
}).mouseOverAction_({ status.string = 'create random pattern grid' });
Button(w, Rect(w.bounds.width - 10 / 7 * 2 + 5, 5, w.bounds.width - 10 / 7, 20)).states_([['noisy?']]).action_({ |b|
	synths_generator.();
}).mouseOverAction_({ status.string = 'randomize all synths' });
Button(w, Rect(w.bounds.width - 10 / 7 * 3 + 5, 5, w.bounds.width - 10 / 7, 20)).states_([['pause'],['play']]).action_({ |b|
	b.value.asBoolean.if({ task.pause }, { task.resume(quant:[0]) });
}).mouseOverAction_({ status.string = 'play/pause' });
direction = Button(w, Rect(w.bounds.width - 10 / 7 * 4 + 5, 5, w.bounds.width - 10 / 7, 20)).states_([['r-t-l'],['l-t-r']]).action_({ |b|
	b.value.asBoolean.if({ step = -1 }, { step = 1 });
}).mouseOverAction_({ status.string = 'change playing direction' });
Button(w, Rect(w.bounds.width - 10 / 7 * 5 + 5, 5, w.bounds.width - 10 / 7, 20)).states_([['fold'],['wrap']]).action_({ |b|
	b.value.asBoolean.if({ border = -1 }, { border = 1 });
}).mouseOverAction_({ status.string = 'behavior on the grid border' });
Slider(w, Rect(w.bounds.width - 10 / 7 * 6 + 5, 5, w.bounds.width - 10 / 7, 20)).action_({ |b|
	resolution = b.value.linlin(0, 1, 1, 8).quantize(1, 1);
	status.string = 'resolution: ' ++ resolution;
}).valueAction_(4.linlin(1,8,0,1)).mouseOverAction_({ status.string = 'change grid resulution' });
// show
w.front.onClose = { task.stop; limit.free };
status.string_('hello, point something to get hint, hopefully..');
)

41.asBoolean
Main.version
s.boot

true.binaryValue



////////#superCollider///////////
//////////////////////10_dic_2011
///////  paisaje Espanol
///// audio @ http://www.archive.org/details/PaisajeEspanol
/////// escala  A| B| C| D| E| F| G#| A  menorArmonica    
///////        57|59|60|62|64|65| 68|69 //////////////
s.boot;
s.scope;
s.meter;
FreqScope.new;
//
////////
// *
(//fx
SynthDef(\master,{|gate=1|
	var fx,env;
		fx=In.ar(20);
		fx=GVerb.ar(fx,24,3,0.2,mul:0.3);
		env=EnvGen.ar(Env.asr(0.01,0.98,0.01),gate,doneAction:2);
	Limiter.ar(Out.ar(0,Pan2.ar(fx*env,[0.92,-0.92])),0.75);
}).add;
);
(//acordes
SynthDef(\inst,{|gate=1,nota1,nota2,nota3,nota4,rel|
	var cuerda,env;
		cuerda=HPF.ar(
			Limiter.ar((LFTri.ar(nota1.midicps,0,0.7)+SinOsc.ar(nota2.midicps,0.2,0.6)+
			SinOsc.ar(nota3.midicps,-0.2,0.5)+SinOsc.ar(nota4.midicps,0.1,0.5))
				+LFTri.ar(Mix(nota1.midicps,nota2.midicps,nota3.midicps,nota4.midicps),0,0.5),
			0.9),
			200,0.75);
		env=EnvGen.ar(Env.perc(0.015,rel),gate,doneAction:2);
	Out.ar([0,20],Pan2.ar(cuerda*env,[0.1,-0.9]))
}).add;
);
(//melodia
SynthDef(\inst2,{|gate=1,nota1|
	var cuerda2,env;
		cuerda2=Limiter.ar(SinOsc.ar(nota1.midicps,0,0.1)+
				Saw.ar(nota1.midicps,0.1,0.06)+
					RLPF.ar(
				LFCub.ar(nota1.midicps,0.1,0.1)+
				LFTri.ar(nota1.midicps,0.1,0.2),
					Phasor.kr(Impulse.kr(1),60,10000),
					0.5),
			0.2);
		env=EnvGen.ar(Env.perc(0.01,0.15),gate,doneAction:2);
	Out.ar(20,Pan2.ar(cuerda2*env,[0.9,-0.82]))
}).add;
);
////
(
Tdef(\acordes,{
	var c=0,fund,primerg,segundog,tercerg,cuartog,quintog,sextog,septimog,tiempo,trans,release;
	Synth(\master);//sinte master
	~acorde=Array.newClear(4);//1a,3a,5ta,7ma
	~a=[45,47,48,50,52,53,56,57,59,60,62,64,65,68,69,71,72,74,76,77,80,81,83,84,86,88,89,92,93];//escala 4 octavas
        primerg=~a[0];//1er grado
	segundog=~a[1];//2do grado
	tercerg=~a[2];//3er grado
	cuartog=~a[3];//4to grado
	quintog=~a[4];//5to grado
	sextog=~a[5];//6to grado
	septimog=~a[6];//7mo grado
	tiempo=Pwrand([0.25,0.5,0.75,1,2],[0.35,0.45,0.05,0.1,0.05],inf).asStream;
	release=Pwrand([0.25,0.5,0.75,1],[0.1,0.5,0.2,0.2],inf).asStream;
	inf.do{
		c=c+1;
		fund=Array.series(7,0);//fundamentales posibles la primera octava
		~numfund=fund.scramble.pyramid.mirror2.reverse[c%fund.size];//grado de la escala
		//~numfund.postln;
		~acorde.put(0,~a[~numfund]);//pone en la tonica algun grado de la escala
		trans=[0,0,12,24,36,0].choose;
		//trans.postln;
		if((~acorde[0]+trans)==(primerg+trans),{//Am7
				~acorde.put(1,~a[~numfund]+3);//menor
				~acorde.put(2,~a[~numfund]+7);//5ta justa
				~acorde.put(3,~a[~numfund]+11)//7ma mayor
				}
		);
		if((~acorde[0]+trans)==(segundog+trans),{//Bm5b7m
				~acorde.put(1,~a[~numfund]+3);//menor
				~acorde.put(2,~a[~numfund]+6);//5ta disminuida
				~acorde.put(3,~a[~numfund]+10)//7ma menor
				}
		);
		if((~acorde[0]+trans)==(tercerg+trans),{//C5#7
				~acorde.put(1,~a[~numfund]+4);//mayor
				~acorde.put(2,~a[~numfund]+8);//5ta aumentada
				~acorde.put(3,~a[~numfund]+11)//7ma mayor
				}
		);
		if((~acorde[0]+trans)==(cuartog+trans),{//Dm7m
				~acorde.put(1,~a[~numfund]+3);
				~acorde.put(2,~a[~numfund]+7);
				~acorde.put(3,~a[~numfund]+10)
				}
		);
		if((~acorde[0]+trans)==(quintog+trans),{//E7m
				~acorde.put(1,~a[~numfund]+4);
				~acorde.put(2,~a[~numfund]+7);
				~acorde.put(3,~a[~numfund]+10)
				}
		);
		if((~acorde[0]+trans)==(sextog+trans),{//F7
				~acorde.put(1,~a[~numfund]+4);
				~acorde.put(2,~a[~numfund]+7);
				~acorde.put(3,~a[~numfund]+11)
				}
		);
		if((~acorde[0]+trans)==(septimog+trans),{//G#5b7dis
				~acorde.put(1,~a[~numfund]+3);
				~acorde.put(2,~a[~numfund]+6);
				~acorde.put(3,~a[~numfund]+9)//7ma disminuida
				}
		);
		~acorde.postln;
		~ins=Synth(\inst);
		~ins.set(\nota1,~acorde[0]);
		~ins.set(\nota2,~acorde[1]);
		~ins.set(\nota3,~acorde[2]);
		~ins.set(\nota4,~acorde[3]);
		~ins.set(\rel,release.next);
		tiempo.next.wait
		}
});
);
(
Tdef(\melodia,{
	var c=0,quinta,
		tiempo=Pwrand([0.25,0.5,1,0.0125],[0.35,0.6,0.25,0.025],inf).asStream;
	inf.do{
		~amel=[57,59,60,62,64,65,68,69,71,72,74,76,77,80,81,83,84,86,88,89,92,93];//3 octavas
		c=c+1;
		~numel=~amel.scramble[c%~amel.size];
		~numel.postln;
		quinta=[0,0,7,12,0].choose;//se transpone una quinta u octava
		~ins2=Synth(\inst2);
		~ins2.set(\nota1,~numel+quinta);
		tiempo.next.wait
		}
});
);
(//
Tdef(\acordes).play;
Tdef(\melodia).play;
)
Tdef(\melodia).stop;
Tdef(\acordes).stop;












(
Ndef(\bla,{
var ou;
var ph = MouseX.kr(30,700);
var ph2 = MouseY.kr(1,100);
var im = Impulse.kr(1/2);
var env = EnvGen.kr(Env.perc(0.0001,0.1),im);
var env2 = EnvGen.kr(Env.perc(0.1,0.001),im);
ou = LFSaw.ar(ph) * env;
ou = CombN.ar(ou,0.1, 1/(env * ph + ph2),2);
ou = CombN.ar(ou,0.1, 1/ph2,2);
ou = ou * 0.1;
ou = ou !2;

}).play
)

b = Buffer.read(s, "sounds/pok2.wav");

(
Ndef(\plop, {
	var ou , tri, la, src;
	tri = Impulse.kr(8);
	src = PlayBuf.ar(1, b, BufRateScale.kr(b), loop:0);
	ou = src;
	//src = AllpassN.ar(src, 1, XLine.kr(1,1/20,0.2),5.1);
	src = AllpassN.ar(src, 1, SinOsc.kr(8)*0.1+1.1001,5.1);
	//src = AllpassN.ar(src, 1, XLine.kr(0.1,1/70,0.1),0.5) - src;
	//src = AllpassN.ar(src, 1, XLine.kr(0.2,1/170,0.1),0.5) - src;
	//src = AllpassN.ar(src, 1, XLine.kr(0.1,1/50,0.5),1.5) - src;
	ou = ou - src;
	ou = Limiter.ar(ou);

	//src = PlayBuf.ar(1, b, BufRateScale.kr(b), loop:0);

}).play
)



(
b = Buffer.alloc(s,2048,1);
c = Buffer.read(s,"sounds/a11wlk01.wav");
)

(
//trig with MouseY
SynthDef("help-diffuser", { arg out=0, bufnum=0 ;
        var in, chain;
        in = Mix.ar(SinOsc.ar(200 * (1..10), 0, Array.fill(10, {rrand(0.1, 0.2)}) ));
        chain = FFT(bufnum, in);
        chain = PV_Diffuser(chain, MouseY.kr > 0.5 );
        Out.ar(out, 0.5 * IFFT(chain).dup);
}).play(s,[\out, 0, \bufnum, b.bufnum ]);
)

(
//trig with MouseY
SynthDef("help-diffuser2", { arg out=0, bufnum=0, soundBufnum=2;
        var in, chain;
        in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        chain = FFT(bufnum, in);
        chain = PV_Diffuser(chain, MouseY.kr > 0.5 );
        Out.ar(out, 0.5 * IFFT(chain).dup);
}).play(s,[\out, 0, \bufnum, b.bufnum, \soundBufnum, c.bufnum]);
)

Formlet
HPZ1
Integrator
MidEQ


a = Signal.sineFill(512, [1]);
a.plot

b = Signal.sineFill(512, [1]);
b.waveFill({arg x, i; a.wrapAt(x.postln*2) },0,512)
~signalFill.(b, b, [1,0.5,0.5]).plot
~signalFill.(b, a, [0,1])
b = b.atan
b.plot2
b.atan
c = Signal.sineFill(512, [1,0.5,0.5]);
[b,c].do(_.plot)
[b,d].plot2
d = ~signalLag.(b,100);
(
~signalFill = { arg sigthis, sigin, amps;
	if(sigthis == sigin) {
		sigin = sigin.copy
	};
	sigthis.waveFill({arg x;
		amps.collect { arg amp, i;
			sigin.wrapAt(x*(i+1)) * amp
		}.sum;
	}, 0, sigthis.size);
	sigthis.normalize;
};
)
(
~signalLag = { arg sig, size=4;
	var rs = sig[0] ! size;
	var newsig;
	newsig = sig.collect { arg x, i;
		rs.insert(0,x);
		rs.pop;
		rs.sum / size;
	};
	newsig.as(Signal);
}
)

k = Signal[1,2,3,4]
j = Signal[0,0,0,0]
~signalFill.(j, k, [0,1])
k.lowpass(1000.004)
k.pop(0)
k.insert(0,7)
k.sum
d.as(Signal)

~buf = Buffer.alloc(s, 1024, 1);
~nbuf = Buffer.allocConsecutive(3,s,1024);

~buf.loadCollection(e.asWavetable);
~nbuf[0].loadCollection(b.asWavetable);
~nbuf[1].loadCollection(d.asWavetable);
~signalSpectre.(e)
e = Signal.newClear(512);
e.waveFill({arg x; 2**( 0 - (x**2)/0.001)},-2,2)
(

SynthDef("help-Osc",{ arg out=0,freq=100,bufnum=0;
		var ou,fou,ou2;
		ou = VOsc.ar(bufnum+MouseX.kr(0,1), freq);
		ou = ou ! 2;
        Out.ar(out,ou)

}).play(s,[\out, 0, \bufnum, ~nbuf[0].bufnum]);
)
(

SynthDef("help-Osc",{ arg out=0,freq=500,bufnum=0;
		var ou,fou,ou2;
		ou = COsc.ar(bufnum, MouseX.kr(30,freq));
		ou = ou ! 2;
        Out.ar(out,ou)

}).play(s,[\out, 0, \bufnum, ~buf.bufnum]);
)



(
var size = 512, real, imag, cosTable, complex;

real = Signal.newClear(size);
                // some harmonics
real.sineFill2([[8], [13, 0.5], [21, 0.25], [55, 0.125, 0.5pi]]);
                // add a little noise
real.overDub(Signal.fill(size, { 0.2.bilinrand }));

imag = Signal.newClear(size);
cosTable = Signal.fftCosTable(size);

complex = fft(real, imag, cosTable);
[real, imag, (complex.magnitude) / 100 ].flop.flat
        .plot2("fft", Rect(0, 0, 512 + 8, 500), numChannels: 3);
)


(
~signalSpectre = { arg sig;
	var real, imag, cosTable, complex, size;
	size = sig.size;
	real = sig;

	imag = Signal.newClear(size);
	cosTable = Signal.fftCosTable(size);
	complex = fft(real, imag, cosTable);

	[real, imag, (complex.magnitude) / 100 ].flop.flat
			.plot2("fft", Rect(0, 0, 812 + 8, 700), numChannels: 3);
};

)



( 
var w, status, limit, buttons, controls = [], one_button, data, synths, one_synth, synths_generator, 
density_one = 1/4, density_many = 1/10, type_distribution = [0.85, 0.15], // tweak it to get more or less dense pattern grid
task, resolution, direction, pos = 0, step = 1, border = 1, dims = [16,16]; // tweak dims to change size of grid
w = Window("rand-n-step+", Rect(50,250,dims[0]*22+10+250,dims[1]*22+60)); // window init
status = StaticText(w, Rect(5, w.bounds.height - 20, w.bounds.width, 20));
limit = { ReplaceOut.ar(0, Limiter.ar(In.ar(0,2))) }.play( addAction:\addToTail ); // limiter
data = Array2D(dims[1],dims[0]); // prepare data
// and buttons
one_button = { | b, density = 0.1 |
	b.valueAction = 0; // reset
	density.coin.if({ b.valueAction = [1,2].wchoose(type_distribution) }); // tweak it
};
synths = Array.fill(dims[1], { () });
buttons = Array.fill(dims[1], { |l|
	controls = controls.add([ // control buttons
		Button( w, Rect( 10 + (22*dims[0]), 35 + (22*l), 20, 20) ).states_([['m'],['u']]).action_({ |b| // mute / unmute
			synths[l].gate = b.value.booleanValue.not.binaryValue;
		}).mouseOverAction_({ status.string = 'mute/unmute' }),
		Button( w, Rect( 10 + (22*(dims[0]+1)), 35 + (22*l), 20, 20) ).states_([['p']]).action_({ // dice pattern line
			buttons[l].do({ |b| one_button.(b, density_one) }); // tweak it
		}).mouseOverAction_({ status.string = 'randomize pattern' }),
		Button( w, Rect( 10 + (22*(dims[0]+2)), 35 + (22*l), 20, 20) ).states_([['s']]).action_({ // dice one synth
			synths[l] = one_synth.(l);
		}).mouseOverAction_({ status.string = 'randomize synth' }),
		Slider( w, Rect( 10 + (22*(dims[0]+3)), 35 + (22*l), 60, 20) ).action_({ |b| // synth amp
			synths[l].amp = b.value.linexp(0,1,1/16,16);
		}).mouseOverAction_({ status.string = 'tweak synth amp' }),
		Slider( w, Rect( 10 + (22*(dims[0]+3)+60), 35 + (22*l), 60, 20) ).action_({ |b| // synth stretch
			synths[l].stretch = b.value.linexp(0,1,1/8,8);
		}).mouseOverAction_({ status.string = 'tweak synth stretch' }),
		Slider( w, Rect( 10 + (22*(dims[0]+3)+120), 35 + (22*l), 60, 20) ).action_({ |b| // synth pan
			synths[l].pan = b.value.linlin(0,1,-1,1);
		}).mouseOverAction_({ |b| status.string = 'tweak synth pan ' })
	]);
	Array.fill(dims[0], { |i| // grid
		Button( w, Rect( 5 + (22*i), 35 + (22*l), 20, 20) ).states_([ ['-'], ['+'], ['%'] ]).action_({ 
			|b| data[l,i] = b.value 
		}).mouseOverAction_({ status.string = '"%" makes sound with 0.5 probability' }); 
	});
});
// synth gen functions and initialization
one_synth = { |i| // tweak this function to (generate and) return other synthdef names
	var name = 'rstp'++i, pan = -1.0.rand2;
	SynthDef(name, { |index = 0, amp = 1, stretch = 1, pan = 0| // args: horizontal position in grid, amplitude and stretch correction, pan
		var sig = Pan2.ar( // tweak sig to get different sound texture
			PMOsc.ar(80.exprand(10000), 1.exprand(200), 1.exprand(20)),
			pan,
			EnvGen.kr(Env(Array.rand(4, 0, 0.05.rrand(0.4)).add(0), Array.rand(3, 0.1, 1.2).add(0.1), 5.rand2), levelScale: amp, timeScale: stretch, doneAction: 2)
		);
		Out.ar(0, sig);
	}).add;
	controls[i][3].valueAction_(1.explin(1/16,16,0,1));
	controls[i][4].valueAction_(1.explin(1/8,8,0,1));
	controls[i][0].valueAction_(0);
	controls[i][5].valueAction_(pan.linlin(-1,1,0,1));
	(name: name, gate: 1, amp: 1, stretch: 1, pan: pan);
};
synths_generator = { Array.fill(dims[1], { |i| synths[i] = one_synth.(i) } ) };
synths_generator.();
// step task
task = Task({
	inf.do({
		pos = (pos + step).mod(dims[0]);
		dims[1].do({ |l|
			(buttons[l] @@ pos).font_(Font("sans", 20));
			(buttons[l] @@ (pos-step)).font_(Font("sans", 14));
			synths[l].gate.booleanValue.if({
				var args = [index: pos, amp: synths[l].amp, stretch: synths[l].stretch * TempoClock.tempo.reciprocal * resolution.reciprocal, pan: synths[l].pan ];
				switch( data[l,pos],
					1, { Synth(synths[l].name, args) },
					2, { 0.5.coin.if({ Synth(synths[l].name, args) }) }
				);  
			});
		});
		switch( pos,
			0,             { (border == -1 && step == -1).if({ direction.valueAction = 0 }) },
			(dims[0] - 1), { (border == -1 && step ==  1).if({ direction.valueAction = 1 }) }
		);	
		(TempoClock.default.tempo.reciprocal / resolution).yield;
	});
}, AppClock).play(quant:[0]);
// app buttons
Button(w, Rect(5,5, w.bounds.width - 10 / 7, 20)).states_([['reset']]).action_({ |b|
	synths_generator.();
	buttons.flat.do({ |b| one_button.(b, 0) }); // tweak it
}).mouseOverAction_({ status.string = 'reset everything' });
Button(w, Rect(w.bounds.width - 10 / 7 * 1 + 5, 5, w.bounds.width - 10 / 6, 20)).states_([['lucky?']]).action_({ |b| // lazy patterns
	buttons.flat.do({ |b| one_button.(b, density_many) }); // tweak it
}).mouseOverAction_({ status.string = 'create random pattern grid' });
Button(w, Rect(w.bounds.width - 10 / 7 * 2 + 5, 5, w.bounds.width - 10 / 7, 20)).states_([['noisy?']]).action_({ |b|
	synths_generator.();
}).mouseOverAction_({ status.string = 'randomize all synths' });
Button(w, Rect(w.bounds.width - 10 / 7 * 3 + 5, 5, w.bounds.width - 10 / 7, 20)).states_([['pause'],['play']]).action_({ |b|
	b.value.booleanValue.if({ task.pause }, { task.resume(quant:[0]) });
}).mouseOverAction_({ status.string = 'play/pause' });
direction = Button(w, Rect(w.bounds.width - 10 / 7 * 4 + 5, 5, w.bounds.width - 10 / 7, 20)).states_([['r-t-l'],['l-t-r']]).action_({ |b|
	b.value.booleanValue.if({ step = -1 }, { step = 1 });
}).mouseOverAction_({ status.string = 'change playing direction' });
Button(w, Rect(w.bounds.width - 10 / 7 * 5 + 5, 5, w.bounds.width - 10 / 7, 20)).states_([['fold'],['wrap']]).action_({ |b|
	b.value.booleanValue.if({ border = -1 }, { border = 1 });
}).mouseOverAction_({ status.string = 'behavior on the grid border' });
Slider(w, Rect(w.bounds.width - 10 / 7 * 6 + 5, 5, w.bounds.width - 10 / 7, 20)).action_({ |b|
	resolution = b.value.linlin(0, 1, 1, 8).quantize(1, 1);
	status.string = 'resolution: ' ++ resolution;
}).valueAction_(4.linlin(1,8,0,1)).mouseOverAction_({ status.string = 'change grid resulution' });
// show
w.front.onClose = { task.stop; limit.free };
status.string_('hello, point something to get hint, hopefully..');
)


a = ProcMod.new(Env([0, 1, 0], [1, 1], \sin, 1), server: s);
s.boot
