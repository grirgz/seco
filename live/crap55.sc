(
{
	var env;
	env = LFPar.ar(0.3,add:1);
	Mix.new([SinOsc.ar(80)*env,SinOsc.ar(40)*env])*Saw.ar(80)*0.5;
}.play;


// Adding dust.

{
var delay1, delay2, source, shift;

//k from -1 to 1

//in samples
delay1= 100;
delay2= 40;

	//source= Dust.ar(300)*4*EnvGen.ar(Env([1,1,0],[(delay1+delay2)/SampleRate.ir,0.0]),);
		//Impulse.kr(MouseY.kr(1,4))););

	source = Dust.ar(5);
	shift = TwoTube.ar(source,1,0.99,delay1,delay2);
	//FreqShift.ar(shift, 400);
	PitchShift.ar(shift, pitchRatio: 0.1);
}.play
)



(
{
var delay1, delay2, source; 

//k from -1 to 1

//in samples
delay1= 200; 
delay2= 40;

source= WhiteNoise.ar(0.5)*EnvGen.ar(Env([1,1,0],[(delay1+delay2)/SampleRate.ir,0.0]), Impulse.kr(MouseY.kr(1,4)));

TwoTube.ar(source,MouseX.kr(-1,1),0.99,delay1,delay2); 
}.play

)




(
SynthDef(\twotube,{arg delay1=100, delay2=50, k=0.0, loss=0.999, dur=0.5, pan=0.0; 
var source; 

//k from -1 to 1

source= WhiteNoise.ar(0.5)*EnvGen.ar(Env([1,1,0,0],[(delay1+delay2)/SampleRate.ir,0.0,1.0]));

Out.ar(0,Pan2.ar(TwoTube.ar(source,k,loss,delay1,delay2)*EnvGen.ar(Env([0,1,1,0],[0.001]++((dur-0.001)*[0.4,0.6])),doneAction:2),pan)); 
}).send(s); 
)



(
t.stop;
t={

inf.do{

Synth(\twotube,[\delay1, rrand(1,300),\delay2, rrand(1,300),\loss, rrand(0.9,0.999),\dur, rrand(0.1,5.0), \pan, rrand(-1,1.0), \k, rrand(-1,1.0)]);

0.5.wait;
};

}.fork;
)

(
Ndef(\plop, {
	var delay1, delay2, source, shift;
	var ka = 1;
	var freq = 50;
	var sig;
	var bufnum = Mdef.sample(\castor, 0);//source = Impulse.ar(freq);
	var rate;
	//k from -1 to 1

	//in samples
	delay1= 40;
	ka = MouseX.kr(-1,1);
	freq = MouseY.kr(40,800);
	rate = MouseY.kr(-1,1)*2;
	rate = 1;
	ka = 0;
	delay2= freq;

	//source= Dust.ar(300)*4*EnvGen.ar(Env([1,1,0],[(delay1+delay2)/SampleRate.ir,0.0]),);
	//Impulse.kr(MouseY.kr(1,4))););
	source = PlayBuf.ar(2, bufnum, rate, loop:1);
	//source = Dust.ar(freq);

	shift = TwoTube.ar(source,ka,0.99,delay1,delay2);
	//FreqShift.ar(shift, -400);
	//shift = PitchShift.ar(shift, pitchRatio: 0.1);
	sig = shift;
	//sig = BPF.ar(sig, freq*20, 0.1);
	sig = sig ! 2;

}).play
)



(
Ndef(\plop, {
	var delay1, delay2, source, shift;
	var ka = 1;
	var freq = 50;
	var sig;
	var bufnum = Mdef.sample(\castor, 0);//source = Impulse.ar(freq);
	var rate;
	var delay;
	var decay;
	//k from -1 to 1

	//in samples
	delay1= 40;
	ka = MouseX.kr(-1,1);
	//delay = MouseX.kr(0.0001,1)/10;
	//delay = 1/MouseX.kr(50,600);
	delay = 1/MouseX.kr(42,80).midicps.round;
	decay = MouseY.kr(0.01,0.8);
	freq = MouseY.kr(40,800);
	rate = MouseY.kr(-1,1)*2;
	rate = 1;
	ka = 0;
	delay2= freq;

	//source= Dust.ar(300)*4*EnvGen.ar(Env([1,1,0],[(delay1+delay2)/SampleRate.ir,0.0]),);
	//Impulse.kr(MouseY.kr(1,4))););
	source = PlayBuf.ar(2, bufnum, rate, loop:1);
	//source = Dust.ar(freq);
	sig = source;

	//sig = sig + DelayC.ar(sig, 0.1, delay);
	sig = sig + AllpassC.ar(sig, 0.1, delay + [0.001,0,0.002] * [1,0.25,4,0.5], decay*[2.01,1,1.52,0.5,4]);

	//shift = TwoTube.ar(source,ka,0.99,delay1,delay2);
	//FreqShift.ar(shift, -400);
	//shift = PitchShift.ar(shift, pitchRatio: 0.1);
	//sig = BPF.ar(sig, freq*20, 0.1);
	sig = Splay.ar(sig)
	//sig = sig ! 2;

}).play
)

(
Ndef(\plop, {
	var delay1, delay2, source, shift;
	var ka = 1;
	var freq = 50;
	var sig;
	var bufnum = Mdef.sample(\castor, 0);//source = Impulse.ar(freq);
	var rate;
	var delay;
	var decay;
	var pointer;
	//k from -1 to 1

	//in samples
	delay1= 40;
	ka = MouseX.kr(-1,1);
	//delay = MouseX.kr(0.0001,1)/10;
	//delay = 1/MouseX.kr(50,600);
	delay = 1/MouseX.kr(42,80).midicps.round;
	decay = MouseY.kr(0.01,0.8);
	freq = MouseY.kr(40,800);
	rate = MouseY.kr(-1,1)*4;
	//rate = 1;
	ka = 0;
	delay2= freq;

	//source= Dust.ar(300)*4*EnvGen.ar(Env([1,1,0],[(delay1+delay2)/SampleRate.ir,0.0]),);
	//Impulse.kr(MouseY.kr(1,4))););
	source = PlayBuf.ar(2, bufnum, rate, loop:1);
	//source = Dust.ar(freq);
	sig = source;

	//sig = sig + DelayC.ar(sig, 0.1, delay);
	//sig = sig + AllpassC.ar(sig, 0.1, delay + [0.001,0,0.002] * [1,0.25,4,0.5], decay*[2.01,1,1.52,0.5,4]);
	//sig = Warp1.ar(2, bufnum, Phasor.kr(Impulse.kr(1), 1 / SampleRate.ir)/BufDur.ir(bufnum),rate);
	//pointer = Line.kr(0,1, BufDur.kr(bufnum));
	//pointer = Sweep.kr(Impulse.ar(BufDur.kr(bufnum).reciprocal), BufSampleRate.kr(bufnum));
	//pointer = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));
	pointer = Phasor.ar(0, 1/BufFrames.kr(bufnum)*2, 0, 1);
	sig = Warp1.ar(2, bufnum, pointer ,rate);
	//sig = BufRd.ar(2, bufnum, pointer);

	//shift = TwoTube.ar(source,ka,0.99,delay1,delay2);
	//FreqShift.ar(shift, -400);
	//shift = PitchShift.ar(shift, pitchRatio: 0.1);
	//sig = BPF.ar(sig, freq*20, 0.1);
	sig = Splay.ar(sig)
	//sig = sig ! 2;

}).play
)




(
var winenv;
// a custom envelope - not a very good one, but you can hear the difference between this
// and the default
winenv = Env([0, 1, 0], [0.5, 0.5], [8, -8]);
b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
z = Buffer.sendCollection(s, winenv.discretize, 1);

SynthDef(\warp, {arg buffer = 0, envbuf = -1;
	var out, pointer, filelength, pitch, env, dir;
	// pointer - move from beginning to end of soundfile over 15 seconds
	pointer = Line.kr(0, 1, 15);
	// control pitch with MouseX
	pitch = MouseX.kr(0.5, 2);
	env = EnvGen.kr(Env([0.001, 1, 1, 0.001], [0.1, 14, 0.9], 'exp'), doneAction: 2);
	out = Warp1.ar(1, buffer, pointer, pitch, 0.1, envbuf, 8, 0.1, 2);
	out = out ! 2;
	Out.ar(0, out * env);
}).send(s);

)

// use built-in env
x = Synth(\warp, [\buffer, b, \envbuf, -1])

// switch to the custom env
x.set([\envbuf, z])
x.set([\envbuf, -1]);

x.free;




{ Gbman2DN.ar(MouseX.kr(20, SampleRate.ir*0.25), MouseX.kr(80, SampleRate.ir), 0.1) }.play(s);

{ Gbman2DN.ar(11025, 22050, -0.7, -2.7, 0.1) }.play(s);

{ Gbman2DN.ar(2200, 8800, 1.2, 2.0002, 0.1) }.play(s);



//MUST be a buffer with data in format [sizeofcurrentsieve,sieveentry1,sieveentry2,...] where currentsize always less than initially allocated buffer length -1. 

{ Sieve1.ar(LocalBuf.newFrom([10]++({0.75.rand}!10)),MouseX.kr(2,100))*0.2}.play

{ Sieve1.ar(LocalBuf.newFrom([88]++({[0.1.rand,0.5.rand].wchoose([0.8,0.2])}!88)),MouseX.kr(1,100))*0.2}.play

b = Buffer.alloc(s,300); 

b.setn(0, [88]++({[0.1.rand,0.5.rand].wchoose([0.8,0.2])}!88))

{ Sieve1.ar(b,MouseX.kr(2,100),0)*0.2}.play

//dynamically swap buffer
b.setn(0, [8]++(1.0!8))

b.setn(0, [8]++({rrand(0.1,1.0)}!8))

b.setn(0, [18]++({[0.0,1.0].choose}!18))

b.setn(0, [188]++({[0.0,rrand(0.96,1.0)].wchoose([0.3,0.7])}!188))



//bass synth
{Out.ar(0,Pan2.ar(DoubleWell3.ar(f:SinOsc.ar(MouseX.kr(0,200),0,MouseY.kr(0.5,4.0))),0.0))}.play

//plotting check of first samples
{DoubleWell3.ar}.plot(0.0015, minval:-1, maxval:1)

//gradually changing 
{Pan2.ar(DoubleWell3.ar(0,0.05,LFSaw.ar(Line.kr(10,1000,10)),Line.kr(0.0,0.3,20)),0.0)}.play

//controlled - midscreen amplitude jump
{Pan2.ar(DoubleWell3.ar(0,0.05,LFSaw.ar(MouseX.kr(10,1000)),MouseY.kr(0.0,0.5)),0.0)}.play



//triggering
{DoubleWell3.ar(Impulse.kr(MouseY.kr(0.01,100, 'exponential')),0.1,LFCub.ar(MouseX.kr(10,1000, 'exponential')),MouseY.kr(0.0,1.0),LFNoise0.kr(2.3,0.2,0.8),LFNoise1.kr(2.37,0.2,0.8))}.play


//AudioIn as forcing
{Pan2.ar(DoubleWell3.ar(0,0.05,LPF.ar(AudioIn.ar(1),100,MouseX.kr(0,1000)),MouseY.kr(0.0,0.5)),0.0)}.play



// default initial params
{ HenonL.ar(MouseX.kr(20, SampleRate.ir)) * 0.2 }.play(s);

// mouse-control of params
{ HenonL.ar(SampleRate.ir/4, MouseX.kr(1,1.4), MouseY.kr(0,0.3)) * 0.2 }.play(s);

// randomly modulate params
(
{ HenonL.ar(
    SampleRate.ir/8,
    LFNoise2.kr(1, 0.2, 1.2),
    LFNoise2.kr(1, 0.15, 0.15)
) * 0.2 }.play(s);
)

// as a frequency control
{ SinOsc.ar(HenonL.ar(10, MouseX.kr(1,1.4), MouseY.kr(0,0.3))*450+10)*0.4 !2}.play(s);
{ SinOsc.ar(HenonL.ar(10, MouseX.kr(1,1.4), MouseY.kr(0,0.3))*450+10)*0.4 !2}.plot(1);
{ HenonL.ar(110, MouseX.kr(1,1.4), MouseY.kr(0,0.3)).softclip -0.25 !2}.plot(4);
{ HenonL.ar(HenonL.ar(2)*200+410, MouseX.kr(1,1.4), MouseY.kr(0,0.3)).softclip -0.25 !2}.play;



a = ControlSpec(-16,-16, \lin, 0, 1, "")
a.map(1)



(
SynthDef(\kick_kong, { arg out=0, amp=0.4, gate=1, pan=0, freq=200;
	var ou;
	var ou1;
	var sig;
	var freqscale;
	var bpfreqs, bprqs;
	//ou = SinOsc.ar(freq);
	freqscale = Line.ar(1,0,0.4);
	ou1 = DynKlang.ar(`[[40,50,60,90,100]], freqscale);
	ou1 = ou1 * 0.05;
	ou = WhiteNoise.ar(0.1);
	ou = BrownNoise.ar(0.1);
	//ou = ou + ou1;
	ou = ou * EnvGen.ar(Env.perc(0.0001,0.4),gate,doneAction:0);
	//ou = DynKlank.ar(`[[50,40,90,100,110,120,130],nil,[0.1,0.1,0.1,0.1]*1], ou );
	bpfreqs = [100,50,100,200,300,1000,400,800] *.t [1,0.2,0.8,4];
	//bpfreqs = bpfreqs.flatten;
	bprqs = [2,1,2,3,2]/4;
	sig = 0;
	sig = bpfreqs.collect { arg ffreq, i;
		var rq = bprqs.wrapAt(i);
		BRF.ar(ou, ffreq, rq)
	};
	ou = sig * 1;
	//ou = ou.clump(4).collect { arg subou; subou.sum };
	//ou = ou.collect { arg subou; subou.sum };
	//ou = ou.sum;
	ou = ou.flatten + ou.sum;
	ou = ou + AllpassC.ar(ou, 0.0001,0.0001*[1,0.9], 0);
	//ou = DynKlang.ar(`[[40,90]], freqscale);
	ou = ou * EnvGen.ar(Env.adsr(0.0001,0.1,0.8,0.1),gate,doneAction:2);
	//ou = ou * 0.5;
	ou = Splay.ar(ou, 1, amp, pan);
	Out.ar(out, ou);
}).add;
)

[1,2,3,4] *.t [1,1.1]
(
Pdef(\plop, Pbind(
	\instrument, \kick_kong,
	\degree, Pseq([0],inf),
	\dur, 1,
	\amp, 0.1
)).play;
);



(
g = File("~/Musique/hydrogenkits/GMkit/samplekit.list".standardizePath,"r");
g.readAllString.split($\n).asCompileString.debug("alo");
g.close;

)
"~/Musique/hydrogenkits/GMKit/samplekit.list".standardizePath




b = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");

// This is very cool!

(
{
	Spring.ar(PlayBuf.ar(1, b, loop: 1)*Saw.ar(4));
}.play;
)
(
{
	PlayBuf.ar(1, b, loop: 1)*Saw.ar(4);
}.play;
)
(
{
	Spring.ar(PlayBuf.ar(1, b, loop: 1), 20);
}.play;
)



(
{
    PlayBuf.ar(1, b, loop: 1)*Saw.ar(4);
}.play;
)
(
fork {
    var size = 3.rrand(10).debug('envelope size');
    {|dur=1, amp=0.8, pan = 0|
        Pan2.ar( 
            SinOsc.ar(
                EnvGen.ar(Env(\freq_l.kr(400!size), \freq_d.kr(0.1!(size-1)), \freq_c.kr(1!(size-1)))), // named controls
                mul: EnvGen.ar(Env.sine(dur, amp), doneAction:2)
            ).tanh, 
            pan
        );
    }.asSynthDef.name_("dzin").add;
    Server.default.sync;
    Pbind(*[
        instrument: \dzin,
        delta: Pseg(Prand(1/2.pow((0..3)), inf), Pwhite(1,4), \lin),
        dur: Pfunc({ thisThread.clock.beatDur }) * Pkey(\delta), // set dur to delta duration
        freq_l: Pcollect(`_, Ptuple({ Pbrown(0,1,0.05).linexp(0,1,40,4e3) } ! size, inf)), // array refs - freq envelope levels
        freq_c: Ptuple({ Pcollect(`_, Ptuple({ Pbrown(-4,4,0.5) } ! (size-1), inf)) }!2, inf), // array of arrayrefs - multichannel expansion w/ different curve levels
        freq_d: Pfunc({ |e| Ref(Array.rand(size-1,0.3,1).normalizeSum * e.dur) }), // sending array ref with total dur = Pkey(\dur)
        pan: Ptuple( { Pbrown() - 0.5 * 2 } ! 2, inf ), // array - multichannel expansion
        amp: Ptuple( { Pbrown() / 10 + 0.1 } ! 2, inf ) // same
    ]).play(TempoClock(156/60));
}
)


(
 {
 var sig;
 sig = Pulse.ar(200);
 [
	sig,
	(sig*5).tanh ,
 ]
 }.plot(0.1)
)

(
 {
 var sig;
 sig = Pulse.ar(200);
 [
	sig,
	//(sig*5).tanh ,
	sig
 ]
 }.play
)
