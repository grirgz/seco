s.boot
(
a = Signal.fill(256, { |i|
        var t = i/255.0;
        t + (0.1 * (max(t, 0.1) - 0.1) * sin(2pi * t * 80 + sin(2pi * 25.6 * t)))
})
);
GUI.qt
a.plot

d = (a.copy.reverse.neg) ++(Signal[0])++ a;

d.plot

d.size  //must be buffer size/2 + 1, so 513 is fine



b = Buffer.alloc(s, 1024, 1);

// or, for an arbitrary transfer function, create the data at 1/2 buffer size + 1
t = Signal.fill(513, { |i| i.linlin(0.0, 512.0, -1.0, 1.0) });
t = Signal.fill(513, { |i| 
	var a;
	a = if(i.inclusivelyBetween(0,100)) {
		i.linlin(0.0, 512.0, 1.0, -2.0)
	} {
		i.linlin(0.0, 512.0, -0.3, 1.4)
	};
	sin(a*i)
});

// linear function
t.plot

// t.asWavetable will convert it to the official Wavetable format at twice the size
b.sendCollection(t.asWavetableNoWrap);  // may also use loadCollection here

// shaper has no effect because of the linear transfer function
(
{       var     sig = Shaper.ar(b, SinOsc.ar(440, 0, 0.4));
        sig ! 2
}.scope;
)


// now for a twist
(
a = Signal.fill(256, { |i|
        var t = i/255.0;
        t + (0.1 * (max(t, 0.1) - 0.1) * sin(2pi * t * 80 + sin(2pi * 25.6 * t)))
})
);

(
a = Signal.fill(256, { |i|
        var t = i/255.0;
        t + (0.5 * (max(t, 0.1) - 0.1) * sin(2pi * t * 80 + sin(2pi * 25.6 * t)))
})
);

a.plot

d = (a.copy.reverse.neg) ++(Signal[0])++ a;

d.plot

d.size  //must be buffer size/2 + 1, so 513 is fine

b.sendCollection(d.asWavetableNoWrap);  // may also use loadCollection here

b.plot // wavetable format!

// test shaper
(
{
        Shaper.ar(
                b,
                SinOsc.ar(SinOsc.kr(1).range(50,300), 0.5, Line.kr(0,0.9,6))
        )
}.scope
)





b = Buffer.alloc(s, 1024, 1);

//size must be power of two plus 1
t = Signal.chebyFill(513,[1, 0.5, 1, 0.125]);

// linear function
t.plot

// t.asWavetableNoWrap will convert it to the official Wavetable format at next power of two size
b.sendCollection(t.asWavetableNoWrap);  // may also use loadCollection here

b.plot

(
{       var     sig = Shaper.ar(b, LFSaw.ar(440, 0, 0.4));
       var     sig2 = LFSaw.ar(440, 0, 0.4);
        [sig, sig2]
}.scope;
)
(
{       var     sig = SinOsc.ar(440, 0, 0.4);
        sig ! 2
}.scope;
)

b.free;



(

Ndef(\conv, { var input, kernel;
			var ou, bla;

        input=LFSaw.ar(500);
        input=SinOsc.ar(400);
        kernel= Mix.new(LFSaw.ar([10,11,65]*10*MouseX.kr(1.0,5.0),0,1.0));
        kernel= LFPulse.ar(SinOsc.ar(150).range(1,100));
        kernel= Sweep.ar(1,5);
        //kernel= SinOsc.ar(Impulse.ar(1).range(100,200) + LFNoise1.ar(1).range(10,100));
        kernel= SinOsc.ar(LFNoise1.ar(1).range(100,500), LFNoise1.ar(2.1).range(0,1));
        kernel= LFSaw.ar(SinOsc.ar(100).range(700,750));
		ou = Convolution.ar(input,kernel, 1024, 0.5);
		bla = SinOsc.ar(ou.range(ou.range(1,50),100));
		//ou = Convolution.ar(ou,kernel, 1024, 0.5);
		ou = SinOsc.ar(ou.range(190,70));
		//ou = LFSaw.ar(ou.range(190,700));
		//ou = Convolution.ar(ou,bla, 1024, 0.5);
		//ou = 0;
		ou = BPF.ar(ou,570,0.01)*8;
		bla = SinOsc.ar(200);
		ou = Convolution.ar(ou,bla, 1024, 0.5);
		//ou = SinOsc.ar(ou+kernel.range(0,100));
		//ou = LFSaw.ar(ou);
		//ou = ou.clip;
		//ou = BPF.ar(ou,500,0.01);
		//ou = FreqShift.ar(ou, 100-kernel);
        //must have power of two framesize
        Out.ar(0,ou!2);
}).play;

)



(
{
i= K2A.ar(MouseButton.kr(0,1,0)) > 0;
m = Spring.ar(i, 100, 0.1);
n = Spring.ar(m, 5, 0.006);
h = Spring.ar(n, 0.1, 0.0017);
j = Spring.ar(h, 0.01, 0.003);
k = Spring.ar(j, 10, 0.0001);

DemandEnvGen.ar(
       Dseq([m, n, h, j, k], inf ),
       SampleDur.ir * 10.5,
       5,
       -4
)}.play
)



  (
    {
       
    i= K2A.ar(MouseButton.kr(0,1,0)) > 0;
       
    m = Spring.ar(i, 1, 0.86);
     n = Spring.ar(m, 1, 0.86);
     h = Spring.ar(n, 1  , 0.86);
     j = Spring.ar(h, 1  , 0.86);
     k = Spring.ar(j, 1  , 0.86);
            DemandEnvGen.ar(

                 Dseq([m, n, h, j, k], inf ),
                SampleDur.ir * 10.5,
                5,
                -4
            );
       
   
    }.play
)



(
{
i= K2A.ar(MouseButton.kr(0,1,0)) > 0;
m = Spring.ar(i, 80, 0.002);
n = Spring.ar(m, 25, 0.007);
h = Spring.ar(n, 1, 0.00017);
j = Spring.ar(h, 1.234, 0.00003);
k = Spring.ar(j, 15, 0.00005);

x = (DemandEnvGen.ar(
       Dseq([m, n, h, j, k].scramble, inf ),
       SampleDur.ir * TExpRand.kr(4, 100, i) * (SinOsc.kr(0.085).range(0.95, 1.04)),
       5,
       -2
).fold(-0.2, 0.2) * 30.542).tanh;
[x, DelayN.ar(x, 0.001, 0.001)]
}.play
)



(
{
i= K2A.ar(MouseButton.kr(0,1,0)) > 0;
m = Spring.ar(i, 80, 0.002);
n = Spring.ar(m, 25, 0.007);
h = Spring.ar(n, 1, 0.00017);
j = Spring.ar(h, 1.234, 0.00003);
k = Spring.ar(j, 15, 0.00005);

x = (DemandEnvGen.ar(
       Dseq([m, n, h, j, k].scramble, inf ),
       SampleDur.ir * TExpRand.kr(4, 100, i) * (SinOsc.kr(0.085).range(0.95, 1.04)),
       5,
       TRand.kr(-8, 8, i)
).fold(-0.2, 0.2) * 30.542).tanh;
[x, DelayN.ar(x, 0.001, 0.001)]
}.play
)




(
{
i= K2A.ar(MouseButton.kr(0,1,0)) > 0;
j = 50.collect { i =  Spring.ar(i, exprand(1, 10000), exprand(0.0001, 0.1)) };
DemandEnvGen.ar(
       Dseq(j, inf ),

       SampleDur.ir * 10.5,
       5,
       -4
)}.play
)



(
{
i= K2A.ar(MouseButton.kr(0,1,0)) > 0;
j = 29.collect { i =  Spring.ar(i, exprand(1, 19000), exprand(0.00001, 0.1)) };
DemandEnvGen.ar(
       Dseq(j.scramble, inf ),


       SampleDur.ir * Dseq(j.scramble, inf ) * 12,

       5,
       -4
)}.play
)

Quarks.gui



l = LinearSpline([1@1,2@2,3@3,4@4])


l.interpolate




l = LinearSpline([1@1,2.2@1.8,2.9@3.1,4@4])


l.interpolate



(

l = LinearSpline([1@1,2.2@1.8,2.9@3.1,4@4]);


l.gui

)


(


l = LinearSpline([1@1,2@2,3@3,4@4]);


l.gui

)



looped

(



l = LinearSpline([ [ 1, 1 ], [ 1.5625, 3.55 ], [ 2.7757352941176, 4.425 ], [ 3.2904411764706, 1.825 ] ], true);


l.gui

)

GUI.qtkkkkkkkk
s.boot



// animate

(

b = BSpline([ Point(0,0), Point(0.58963874282376, 0.4134375), Point(2.2682499386103, 0.826875), Point(4.8180390967671, 0.013125) ], 2.3870967741935,false);

b.gui(nil,1000@200);

n = NumberEditor.new;

n.gui;

b.animate(n,'value_');

)



(


b = BezierSpline(

Point(0.0,-30.0),

  [ ],

Point(1.0,30),

  [],

false

);


g = b.gui(nil,1000@300,ControlSpec(-44,44),ControlSpec(0,8,step:0.25));

)


(

b = BezierSpline(

0@0,

  [ 0.2@0.4, 0.5@0.9  ],

1@1,

  [],

false

);


g = b.gui(nil,1000@300);

g.spec = ControlSpec(0,1,step:0.1);

g.setDomainSpec( ControlSpec(0,8,step:0.25) );

)


(


b = BezierSpline(

Point(0.0,-30.0),

  [ ],

Point(1.0,30),

  [],

false

);


g = b.gui(nil,1000@300);

g.spec = ControlSpec(-44,44);

g.domainSpec = ControlSpec(0,8,step:0.25);

)







s.boot

{ Klank.ar(`["SC.app, the bell tolls for thee.".collectAs(_.ascii.midicps, Set).asArray, nil, 6 ! 17],Impulse.ar(0, 0, 0.1), 0.25) }.play;
{ Klank.ar(`["va te faire foutre freddy.".collectAs(_.ascii.midicps, Set).asArray, nil, 6 ! 17],Impulse.ar(0, 0, 0.1), 0.25) }.play;
{ Klank.ar(`["haha petite bite".collectAs(_.ascii.midicps, Set).asArray, nil, 6 ! 17],Impulse.ar(0, 0, 0.1), 0.25) }.play;


(
~ltn = { arg notes, rep;
	var le = "0123456abcdefghijklmnopqrstuvwxyz";
	var di = Dictionary.new;
	var res;
	le.do { arg l, x;
		//[l, x].postcs;
		di[l] = x;
	};
	di[$ ] = \rest;
	res = notes.as(Array).collect{ arg n; di[n] };
	//res.postcs;
	Pseq(res, rep);
};
)
$7.asInteger
~ltn.("abc")

(
Pbind(
	\degree, Pseq([5]),
	\octave, 5
).play
)
(
Pdef(\bla, Pbind(
	\note, ~ltn.("abab ifi", inf)-12,
	\amp, 0.1,
	\dur, 0.25
)).play
)
(
Pdef(\bla2, Pbind(
	\instrument, \lead2,
	\note, ~ltn.("acf fhjf", inf)-12,
	\amp, 0.05,
	\fbase, Pseg(Pseq([500,10],inf),8),
	\rq, Pseg(Pseq([1,0.2],inf),8),
	\wet, 0.5,
	//\rq, 1,
	\dur, 0.25
)).play
)
(
Pdef(\bla3, Pbind(
	\instrument, \lead2,
	//\note, ~ltn.("acf fhjf", inf)-12,
	\freq, Pseq([100,190,100,150,\r,100,\r,150],inf),
	\amp, 0.1,
	\fbase, Pseg(Pseq([5.1,100],inf),8),
	//\rq, Pseg(Pseq([1,0.2],inf),8),
	\wet, 0.8,
	\rq, 0.01,
	\dur, 0.125
)).play
)

Pdef.defaultQuant = 2;




{Mix.fill(100, {arg a = 5 + a; Pan2.ar(SinOsc.ar(10 * a, 0, Dust.kr(0.01 + (a * 0.009), 0.5)), {rrand(-0.2, 0.2)})})}.play;



{Mix.fill(100, {arg a = 1 + a; Pan2.ar(SinOsc.ar(70 * a, 0, Dust.kr(0.01 + (a * 0.009), 0.5)), {rrand(-0.2, 0.2)})})}.play;



SynthDef("snTrig1", { arg levSn=1, t_trig=0, sustain=0.125, panPos=0, amp=1,
	out=0;
	var snEnv, ou;
	var snareEnv;
	snareEnv = Env.linen(0.001, 1.9, 0.099, 1);
	snEnv=EnvGen.ar(snareEnv,1, doneAction:2, timeScale: sustain, levelScale: levSn);
	ou =Pan2.ar(Decay2.kr(t_trig, 0.005, 0.25, FSinOsc.ar(38.midicps, 0.3)+ 		BrownNoise.ar(0.4)),panPos);
 
	Out.ar(out, ou*snEnv * amp);
}).store;


Mdef(\snare2, Pbind(\instrument, \snTrig1));
Mdef(\snare4, Pbind(\instrument, \MidiDrum));
Mdef(\snare1, Pbind(\instrument, \mysnare));

SynthDescLib.global.synthDescs[\mysnare].metadata[\variants]
SynthDescLib.global.synthDescs[\mysnare].controlDict[\popamp].dump
SynthDescLib.global.synthDescs[\mysnare].def.variants = (bla:(popamp: 0.12345))
SynthDescLib.global.synthDescs[\mysnare].def.dump
SynthDescLib.global.synthDescs[\branle].def.variants = (casse: [freqo:100])
SynthDescLib.global.synthDescs[\branle].def.dump
SynthDescLib.global.synthDescs['branle.burne'].def.dump
SynthDescLib.global.synthDescs.keys.printAll

SynthDef(\mysnare)

SynthDef(\branle, { arg freqo=500;
	Out.ar(0, SinOsc.ar(freqo));
},variants:(burne:[freqo:100])).add;

Synth("bla.rah");

Pbind(\instrument, 'branle.casse',
	\variant, (casse:[freqo:100]),
	\freqi, 100
).play

(\bla).dereference.dump

SynthDef.new("MidiDrum", { |vel=100, x=64, y=64,out=0|
	// resonant snare sound
	var sndbuf= Buffer.readChannel(s,
	"/home/andy/Desktop/music/supercollider/samples/84001__s
	andyrb__KBSD_C42_VELOCITY9.wav", channels:0);
	var rq=10**((16-y) / 41);
	var env,amp;
	var noteMin=30;//54;
	var noteMax=128;//66;
	var note=(x*(noteMax-noteMin)/127)+noteMin;
	//note=(note/12).floor*12;
	vel=vel+((127-note)/40)+((127-y)/50);
	amp=((vel-96)/3).dbamp;
	env=EnvGen.kr(Env.triangle(1,4),1,doneAction:2);
	Out.ar(out,amp*env*Pan2.ar(RLPF.ar(PlayBuf.ar(1,sndbuf),
	note.floor.midicps, rq ), 0) );
} ).store;





SynthDef.new("MidiDrum", { |amp=0.1, vel=100, x=64, y=64,out=0, attack=0.01, release=0.5|
	// synth drum with pink noise, comb delay line and low pass filter.
	var rq=10**((y-40) / 41);
	var env;
	var noteMin=55; // 200Hz
	var noteMax=128;//66;
	var note=(x*(noteMax-noteMin)/127)+noteMin;
	var baseFreq=100;
	//amp=16*((vel-96)/3).dbamp;
	env=EnvGen.kr(Env.perc(attack,release,1),1,doneAction:2);
	Out.ar(out,amp*env*Pan2.ar(LPF.ar(CombC.ar(PinkNoise.ar(
	0.1),1,1/baseFreq,rq),note.midicps), 0) );
} ).store;


SynthDef.new("MidiDrum", { |vel=100, x=64, y=64, out=0|
	// bass drum patch with variable square wave / saw wave ratio.
	var baseFreq=50, baseDelayMin=0.1, baseDelayMax=3, baseAmp=1,
	attack=0.01;
	var baseFreqMod=1, harmLPFreqMin=baseFreq,
	harmLPFreqMax=baseFreq*10;
	var fmBaseFreq=500, fmModSig=250, fmAmp=0.5, fmDelay;
	var amp,ampEnv, baseFreqEnv, harmSig, harmLPFreq, baseDelay, sawRatio,
	oscSig;
	baseDelay=0.5; //((y/128)*(baseDelayMax-baseDelayMin))+baseDelayMin;
	fmDelay=baseDelay/5;
	amp=((vel-32)/3).dbamp;
	harmLPFreq=((x/128)*(harmLPFreqMax-harmLPFreqMin))+harmLPFreqMin;
	ampEnv=amp*EnvGen.kr(Env.perc(attack,baseDelay,baseAmp),1,doneAction:2)
	;
	baseFreqEnv=EnvGen.kr(Env.perc(attack,baseDelay,baseFreqMod,'sine'));
	sawRatio=(y/128);
	oscSig=sawRatio*LFTri.ar(baseFreq+baseFreqEnv)+(1-
	sawRatio)*Saw.ar(baseFreq+baseFreqEnv);
	harmSig=LPF.ar(oscSig,harmLPFreq);
	Out.ar(out,Pan2.ar(ampEnv*harmSig,0));
} ).store;



SynthDef.new("MidiDrum", { |vel=100, x=64, y=64, out=0|
	// snare drum from synth secrets (based on roland 909).
	// different version with fixed noise delay and low pass filter.
	var part1Freq=180, part1Amp=0.1, part2Freq=330, part2Amp=0.05,
	minDistortPow=0, maxDistortPow=3, partDelay=0.7;
	var attack=0.01, noiseLPFreq=10000, noiseHPFreq=2000, noiseAmp1=0.005,
	noiseAmp2Ratio=2;
	var noiseDelay=0.4;
	var partSig, partEnv, amp, noiseEnv, noiseSig1, noiseSig2, noiseSig,
	outSig,lpFreq;
	var distort;
	distort=10**(((x/128)*(maxDistortPow-minDistortPow))+minDistortPow);
	amp=((vel/4)-28).dbamp;
	partEnv=amp*EnvGen.kr(Env.perc(attack,partDelay,1),1,doneAction:2);
	partSig=part1Amp*atan(SinOsc.ar(part1Freq, 0, distort))
	+part2Amp*atan(SinOsc.ar(part2Freq,0,distort));
	noiseSig1=noiseAmp1*LPF.ar(WhiteNoise.ar(1),noiseLPFreq);
	noiseSig2=(amp**0)*HPF.ar(noiseSig1*noiseAmp2Ratio,noiseHPFreq);
	noiseSig=(noiseSig1+noiseSig2);
	noiseEnv=amp*EnvGen.kr(Env.perc(attack,noiseDelay,1),1,doneAction:0);
	lpFreq=((y*3/5)+51).midicps;
	outSig=RLPF.ar(partSig*partEnv+noiseSig*noiseEnv,lpFreq,0.5);
	Out.ar(out,Pan2.ar(outSig,0));
} ).store;





// create a def with some variants
(
SynthDef(\vartest, {|out=0, freq=440, amp=0.2, a = 0.01, r = 1|
        // the EnvGen with doneAction: 2 frees the synth automatically when done
        Out.ar(out, SinOsc.ar(freq, 0, EnvGen.kr(Env.perc(a, r, amp), doneAction: 2)));
}, variants: (alpha: [a: 0.5, r: 0.5], beta: [a: 3, r: 0.01], gamma: [a: 0.01, r: 4])
).add;
)

// now make some synths. First using the arg defaults
Synth(\vartest);

// now the variant defaults
Synth('vartest.alpha');
Synth('vartest.beta');
Synth('vartest.gamma');

// override a variant
Synth('vartest.alpha', [\release, 3, \freq, 660]);
