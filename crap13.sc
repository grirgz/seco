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
