

s.boot; // If you haven't already booted


x = { Ringz.ar(PinkNoise.ar(0.1), {exprand(300, 20000)}.dup(100)).mean }.play;


x.free; // To stop



(

x = {

var driver, cutoffenv, freqs, res;

cutoffenv = EnvGen.ar(Env.perc(0, 0.5)) * 20000 + 10;

driver = LPF.ar(WhiteNoise.ar(0.1), cutoffenv);

freqs  = {exprand(300, 20000)}.dup(100);

res    = Ringz.ar(driver, freqs).mean.dup

}.play;

)


x.free;




(

x = {

var lodriver, locutoffenv, hidriver, hicutoffenv, freqs, res;

locutoffenv = EnvGen.ar(Env.perc(0.25, 5)) * 20000 + 10;

lodriver = LPF.ar(WhiteNoise.ar(0.1), locutoffenv);

hicutoffenv = 10001 - (EnvGen.ar(Env.perc(1, 3)) * 10000);

hidriver = HPF.ar(WhiteNoise.ar(0.1), hicutoffenv);

hidriver = hidriver * EnvGen.ar(Env.perc(1, 2, 0.25));

freqs  = {exprand(300, 20000)}.dup(100);

res    = Ringz.ar(lodriver + hidriver, freqs).mean.dup

}.play;

)



(

x = {

var lodriver, locutoffenv, hidriver, hicutoffenv, freqs, res;

locutoffenv = EnvGen.ar(Env.perc(0.5, 5)) * 20000 + 10;

lodriver = LPF.ar(WhiteNoise.ar(0.1), locutoffenv);

hicutoffenv = 10001 - (EnvGen.ar(Env.perc(1, 3)) * 10000);

hidriver = HPF.ar(WhiteNoise.ar(0.1), hicutoffenv);

hidriver = hidriver * EnvGen.ar(Env.perc(1, 2, 0.25));

freqs  = {exprand(300, 20000)}.dup(100);

res    = Ringz.ar(lodriver + hidriver, freqs).mean;

((res * 1) + (lodriver * 2)).dup;

}.play;

)



(

x = {

var lodriver, locutoffenv, hidriver, hicutoffenv, freqs, res, thwack;

locutoffenv = EnvGen.ar(Env.perc(0.5, 5)) * 20000 + 10;

lodriver = LPF.ar(WhiteNoise.ar(0.1), locutoffenv);

hicutoffenv = 10001 - (EnvGen.ar(Env.perc(1, 3)) * 10000);

hidriver = HPF.ar(WhiteNoise.ar(0.1), hicutoffenv);

hidriver = hidriver * EnvGen.ar(Env.perc(1, 2, 0.25));

thwack = EnvGen.ar(Env.perc(0.001,0.001,1));

freqs  = {exprand(300, 20000)}.dup(100);

res    = Ringz.ar(lodriver + hidriver + thwack, freqs).mean;

((res * 1) + (lodriver * 2) + thwack).dup;

}.play;

)


x.free;



//adapted from 2.18 Vibrations of a Stiff String, p61, Thomas D. Rossing and Neville H. Fletcher (1995) Principles of Vibration and Sound. New York: Springer-Verlag 

(

var modes,modefreqs,modeamps;

var mu,t,e,s,k,f1,l,c,a,beta,beta2,density;

var decaytimefunc;

var material;


material= \nylon; // \steel


//don't know values of E and mu for a nylon/gut string

//so let's try steel


//radius 1 cm

a=0.06;


s=pi*a*a;


//radius of gyration

k=a*0.1;



if (material ==\nylon,{


e=2e+7; 


density=2000; 


},{//steel


e= 2e+11; // 2e+7; //2e+11 steel;


//density p= 7800 kg m-3 

//linear density kg m = p*S


density=7800; 

});


mu=density*s;


t=100000;


c= (t/mu).sqrt; //speed of sound on wave


l=1.1; //0.3


f1= c/(2*l);


beta= (a*a/l)*((pi*e/t).sqrt);


beta2=beta*beta;


modes=10;


modefreqs= Array.fill(modes,{arg i; 

var n,fr;

n=i+1;


fr=n*f1*(1+beta+beta2+(n*n*pi*pi*beta2*0.125));


if(fr>21000, {fr=21000}); //no aliasing


fr

});


decaytimefunc= {arg freq;

var t1,t2,t3;

var m,calc,e1dive2;


//VS p 50 2.13.1 air damping


m=(a*0.5)*((2*pi*freq/(1.5e-5)).sqrt);


calc= 2*m*m/((2*(2.sqrt)*m)+1);


t1= (density/(2*pi*1.2*freq))*calc;


e1dive2=0.01; //a guess!


t2= e1dive2/(pi*freq);


//leave G as 1

t3= 1.0/(8*mu*l*freq*freq*1);


1/((1/t1)+(1/t2)+(1/t3))

};


modeamps=Array.fill(modes,{arg i; decaytimefunc.value(modefreqs.at(i))});


modefreqs.postln;

modeamps.postln;


{

var output;

//EnvGen.ar(Env.new([0.001,1.0,0.9,0.001],[0.001,0.01,0.3],'exponential'),WhiteNoise.ar)

//could slightly vary amps and phases with each strike?


output=EnvGen.ar(

Env.new([0,1,1,0],[0,10,0]),doneAction:2)*

//slight initial shape favouring lower harmonics- 1.0*((modes-i)/modes)

Mix.fill(modes,{arg i; XLine.ar(1.0,modeamps.at(i),10.0)*SinOsc.ar(modefreqs.at(i),0,1.0/modes)});


Pan2.ar(output,0)

}.play;


)



(

// this shows the building of the piano excitation function used below

{

var strike, env, noise;

strike = Impulse.ar(0.01);

env = Decay2.ar(strike, 0.008, 0.04);

noise = LFNoise2.ar(3000, env);

[strike, K2A.ar(env), noise]

}.plot(0.03); //.scope

)



(

// hear the energy impulse alone without any comb resonation

{

var strike, env, noise;

strike = Impulse.ar(0.01);

env = Decay2.ar(strike, 0.008, 0.04);

noise = LFNoise2.ar(3000, env);

10*noise

}.scope

)




//single strike with comb resonation 


(

{

var strike, env, noise, pitch, delayTime, detune;

strike = Impulse.ar(0.01);

env = Decay2.ar(strike, 0.008, 0.04);

pitch = (36 + 54.rand); 

Pan2.ar(

// array of 3 strings per note

Mix.ar(Array.fill(3, { arg i;

// detune strings, calculate delay time :

detune = #[-0.05, 0, 0.04].at(i);

delayTime = 1 / (pitch + detune).midicps;

// each string gets own exciter :

noise = LFNoise2.ar(3000, env); // 3000 Hz was chosen by ear..

CombL.ar(noise, // used as a string resonator

delayTime, // max delay time

delayTime, // actual delay time

6) // decay time of string

})),

(pitch - 36)/27 - 1 // pan position: lo notes left, hi notes right

)

}.scope

)




(

// synthetic piano patch (James McCartney)

var n;

n = 6; // number of keys playing

play({

Mix.ar(Array.fill(n, { // mix an array of notes

var delayTime, pitch, detune, strike, hammerEnv, hammer;

// calculate delay based on a random note

pitch = (36 + 54.rand); 

strike = Impulse.ar(0.1+0.4.rand, 2pi.rand, 0.1); // random period for each key

hammerEnv = Decay2.ar(strike, 0.008, 0.04); // excitation envelope

Pan2.ar(

// array of 3 strings per note

Mix.ar(Array.fill(3, { arg i;

// detune strings, calculate delay time :

detune = #[-0.05, 0, 0.04].at(i);

delayTime = 1 / (pitch + detune).midicps;

// each string gets own exciter :

hammer = LFNoise2.ar(3000, hammerEnv); // 3000 Hz was chosen by ear..

CombL.ar(hammer, // used as a string resonator

delayTime, // max delay time

delayTime, // actual delay time

6) // decay time of string

})),

(pitch - 36)/27 - 1 // pan position: lo notes left, hi notes right

)

}))

})

)





(
Display.make({ arg thisDisplay, carrier_freq, modulator_freq,
amp, modulator_amp, pan;
carrier_freq.spec_(\freq);
modulator_freq.spec_(ControlSpec(0.1, 20000, \exp, 0, 1));
amp.spec_(\amp);
modulator_amp.spec_(\amp);
pan.spec_(\pan);
thisDisplay.synthDef_({ arg carrier_freq, modulator_freq,
amp, modulator_amp, pan;
var
modulator, panner, result;
modulator = SinOsc.ar(modulator_freq,
mul: modulator_amp);
result = SinOsc.ar(carrier_freq,
mul: modulator * amp);
panner = Pan2.ar(result, pan);
Out.ar(0, panner);
}, [\carrier_freq, carrier_freq, \modulator_freq,
modulator_freq, \amp, amp, \modulator_amp, modulator_amp,
\pan, pan]);
thisDisplay.name_("Ring Modulation");
}).show;
)





(
SynthDef(\ring1, { arg out=0, freq=200, modulator_freq=5, amp=0.1, modulator_amp=1, pan=0;
var modulator, panner, result, env;

env = ~make_adsr.(\adsr);
modulator = SinOsc.ar(modulator_freq, mul: modulator_amp);
result = SinOsc.ar(carrier_freq, mul: modulator * amp);
result = result * env;
panner = Pan2.ar(result, pan);
panner;
//Out.ar(0, panner);
}).store;
)



        // Setting envir variables in a Tdef:
(
Tdef(\text).set(\note, [0, 2, 7], \dur, { [0.1, 0.2, 0.4].choose }, \pan, 0, \amp, 0.1);

w = Window("EZTexts", Rect(200, 400, 304, 120)).front;
w.addFlowLayout;

TdefGui(Tdef(\text), 0, parent: w);

e = EnvirGui(Tdef(\text).envir, 4, parent: w);

Tdef(\text, { |ev|
        var mydur;
        loop {
                mydur = ev.dur;
                (note: ev.note, dur: mydur, amp: ev.amp, pan: ev.pan).postln.play;
                mydur.wait;
        }
}).play;
)

        // or equivalently, use the built-in EnvirGui in TdefGui:
TdefGui(Tdef(\text), 4);

Tdef(\text).set(\yuhu, Prand([2, 3, 5, 8, 13], inf), \magic, [\abra, \cadabra]);

Tdef(\text).clear;



// simple example
g = NdefParamGui.new(nil, 5);   // empty with 5 slots
g.object_(Ndef(\a));            // put in a nodeproxy
Ndef(\a, { |freq = 300, amp = 0.2| SinOsc.ar(freq) * amp });
Ndef(\a).set(\freq, 2000);      // add a setting

g.object_(nil);                 // put in a nodeproxy

g.object_(Ndef(\a));            // put in a nodeproxy
Ndef(\a).set(\amp, 0.125);      // add a setting
Ndef(\a, { |freq = 300, amp = 0.2| SinOsc.ar(freq) * amp });
Ndef(\a).set(\freq, 234);       // add a setting

Ndef(\a).play
Ndef(\lfo, { LFNoise0.kr([12, 8], 200).sum + 500 });
Ndef(\a).map(\freq, Ndef(\lfo));// mapped proxies are shown


//Multichannel controls are shown in EZText

Ndef(\a, { |freqs = #[300, 303], pan, amp = 0.2, moddd = 0.1| SinOsc.ar(freqs).sum * amp });
Ndef(\a).play
Ndef(\a).setn(\freqs, [300, 350])
Ndef(\a).setn(\freqs, [330, 350])
Ndef(\a).set(\harm, 123)

Ndef(\a).nodeMap.clear

Ndef(\lfos, { LFNoise0.kr([12, 8], 200) + 500 });
Ndef(\a).map(\freqs, Ndef(\lfos))

g.parent.close



(
{
var vibrato;
vibrato = SinOsc.kr(6, mul: 0.02, add: 1);
SinOsc.ar(
freq: MouseY.kr(3200, 200, lag: 0.5, warp: 1) *
vibrato, //Vibrato
mul: abs(MouseX.kr(0.02, 1)) //Amplitude
)
}.play
)

(
//Vibrato
{
var depthChange, vibrato;
depthChange = Line.kr(0, 5, 3);
vibrato = SinOsc.ar(freq: 5, mul: depthChange, add: 440);
SinOsc.ar(
vibrato,
mul: 0.5)
}.play
)
(
{ //SinOsc (sci-fi)
var lfo;
lfo = SinOsc.ar(freq: 10,mul: 100, add: 400);
SinOsc.ar(lfo, mul: 0.5)
}.play
)
mul: 100, add: 400);
(
{ //Pulse (phone ring)
var lfo;
lfo = LFPulse.ar(freq: 15, mul: 200, add: 1000);
SinOsc.ar(lfo, mul: 0.5)
}.play
)
(
{ //Saw
var lfo;
lfo = LFSaw.ar(freq: 2, mul: -100, add: 600);
SinOsc.ar(lfo, mul: 0.5)
}.play
)
(
{ //Noise (computer)
var lfo;
lfo = LFNoise0.ar(freq: [28, 27], mul: 1000, add: 2000);
SinOsc.ar(lfo, mul: 0.5)
}.play
)
(
{ //Noise (manic birds)
var lfo;
lfo = LFNoise1.ar(freq: [28, 27], mul: 400, add: 2000);
SinOsc.ar(lfo, mul: 0.5)
}.play
)


(
//frequency linked to envelope length
//high notes short, low long
{
var frequency;
Mix.ar(
{
frequency = rrand(100, 5000);
Pan2.ar(
SinOsc.ar(
frequency,
mul: EnvGen.kr(
Env.perc(0.001, 500/frequency),
Dust.kr(0.05),
0.2
)
),
rrand(-1.0, 1.0)
)
}.dup(100)
)
}.play
)
(
//frequency linked to decay length
//basically the same code but more compact
//low notes short, high long
{var frequency;
Mix.ar({
frequency = rrand(100, 3000);
Pan2.ar(SinOsc.ar(frequency,
mul: EnvGen.kr(Env.perc(0.001, frequency/1000),
Dust.kr(0.05), 0.2)), rrand(-1.0, 1.0)) }.dup(100))}.play
)
(//high notes short, low long
{var frequency;
Mix.ar({
frequency = rrand(100, 3000);
Pan2.ar(SinOsc.ar(frequency,
mul: EnvGen.kr(Env.perc(200/frequency, 0.0001),
Dust.kr(0.05), 0.2)), rrand(-1.0, 1.0)) }.dup(100))}.play
)
(//low notes short, high long
{var frequency;
Mix.ar({
frequency = rrand(100, 1000);
Pan2.ar(SinOsc.ar(frequency,
mul: EnvGen.kr(Env.perc(frequency/500, 0001),
Dust.kr(0.05), 0.05)), rrand(-1.0, 1.0)) }.dup(100))}.play
)



(
{
f = 100;
[
SinOsc.ar(f*1, mul: 1), SinOsc.ar(f*2, mul: 1/2),
SinOsc.ar(f*3, mul: 1/3), SinOsc.ar(f*4, mul: 1/4),
SinOsc.ar(f*5, mul: 1/5), SinOsc.ar(f*6, mul: 1/6),
SinOsc.ar(f*7, mul: 1/7), SinOsc.ar(f*8, mul: 1/8),
SinOsc.ar(f*9, mul: 1/9), SinOsc.ar(f*10, mul: 1/10),
SinOsc.ar(f*11, mul: 1/11), SinOsc.ar(f*12, mul: 1/12)
]
}.scope(12)
)


(

{
	var speed=70, freq=200;
	freq = MouseX.kr(20,2000);
	freq = SinOsc.kr(1,add:1.1).explin(0.1,2,20,1000);
	Mix.new(Array.fill(8, { arg m;
		n=m+1;
		SinOsc.ar(freq*n, LFNoise0.kr(rrand(speed, speed*2)), 0.5, 0.5)/n;
	}));
}.play
)



(
{
f = 100;
t = Impulse.kr(1/3);
Mix.ar([
SinOsc.ar(f*1, mul: EnvGen.kr(Env.perc(0, 1.4), t)/1),
SinOsc.ar(f*2, mul: EnvGen.kr(Env.perc(0, 1.1), t)/2),
SinOsc.ar(f*3, mul: EnvGen.kr(Env.perc(0, 2), t)/3),
SinOsc.ar(f*4, mul: EnvGen.kr(Env.perc(0, 1), t)/4),
SinOsc.ar(f*5, mul: EnvGen.kr(Env.perc(0, 1.8), t)/5),
SinOsc.ar(f*6, mul: EnvGen.kr(Env.perc(0, 2.9), t)/6),
SinOsc.ar(f*7, mul: EnvGen.kr(Env.perc(0, 4), t)/7),
SinOsc.ar(f*8, mul: EnvGen.kr(Env.perc(0, 0.3), t)/8),
SinOsc.ar(f*9, mul: EnvGen.kr(Env.perc(0, 1), t)/9),
SinOsc.ar(f*10, mul: EnvGen.kr(Env.perc(0, 3.6), t)/10),
SinOsc.ar(f*11, mul: EnvGen.kr(Env.perc(0, 2.3), t)/11),
SinOsc.ar(f*12, mul: EnvGen.kr(Env.perc(0, 1.1), t)/12)
])*0.5
}.scope(1)
)


(
{
SinOsc.ar([100,200,300])
}.play
)
(
{
Mix.new(SinOsc.ar([100,200,300]))
}.play
)


( // Let it run for a while, the strikes are random
{
var trigger, partials = 12;
trigger = Dust.kr(3/7);
Pan2.ar(
Mix.ar(
{
SinOsc.ar(exprand(50.0, 4000)) *
EnvGen.kr(
Env.perc(0, rrand(0.2, 3.0)),
trigger,
1.0.rand
)
}.dup(partials)
)/partials,
1.0.rand2
)
}.play
)


(
{
var dur = 6, base, aenv, fenv, out, trig;
base = Rand(40, 100);
trig = SinOsc.ar(1/10);
out = Mix.fill(15,{
var thisDur;
thisDur = dur * rrand(0.5, 1.0);
aenv = EnvGen.kr(Env.perc(0, thisDur), trig);
fenv = EnvGen.kr(Env.new([0, 0, 1, 0], [0.25*thisDur, 0.75*thisDur, 0]), trig);
Pan2.ar(SinOsc.ar( Rand(base, base * 12) *
LFNoise1.kr(10, mul: 0.02 * fenv, add: 1), // freq
mul: aenv // amp
), ([1, -1].choose) * fenv)
}) * 0.05;
out
}.play(s);
{
var dur = 6, base, aenv, fenv, out, trig, detune;
base = Rand(40, 60);
detune = 0.1; // increase this number to detune the second bell
trig = SinOsc.ar(1/10, pi);
out = Mix.fill(15,
{ arg count;
var thisDur;
thisDur = dur * rrand(0.5, 1.0);
aenv = EnvGen.kr(Env.perc(0, thisDur), trig);
fenv = EnvGen.kr(Env.new([1, 1, 0, 1], [0.05*thisDur, 0.95*thisDur, 0]), trig);
Pan2.ar(SinOsc.ar( base*(count+1+ detune.rand) *
LFNoise1.kr(10, mul: 0.02 * fenv, add: 1), // freq
mul: aenv // amp
), ([1, -1].choose) * fenv)
}) * 0.05;
out
}.play(s);
)


(
{
var aenv, fenv, out, trig, dur, base;
dur = rrand(1.0, 6.0);
base = exprand(100, 1000);
trig = Impulse.kr(1/6);
out = Mix.ar(
Array.fill(15,{
arg count;
var thisDur;
thisDur = dur * rrand(0.5, 1.0);
aenv = EnvGen.kr(
Env.new([0, 1, 0.4, 1, 0], [0, 0.5, 0.5, 0]), trig,
timeScale: thisDur);
fenv = EnvGen.kr(
Env.new([0, 0, 0.5, 0.5, 0], [0.25, 0.5, 0.25, 0]),
trig, timeScale: thisDur);
Pan2.ar(SinOsc.ar( Rand(base, base * 12) *
LFNoise1.kr(10, mul: 0.1 * fenv, add: 1), // freq
mul: aenv // amp
), ([1, -1].choose) * fenv)
})
) * EnvGen.kr(Env.linen(0, dur, 0), Impulse.kr(6), timeScale: dur,
levelScale: 0.05, doneAction: 2);
out*0.3;
}.play;
)

{Mix.fill(1000, {SinOsc.ar(rrand(1.0, 20000))})*0.01}.play
{WhiteNoise.ar(1)}.play


(
{
var signal, filter, cutoff, resonance;
signal = PinkNoise.ar(mul: 0.7);
cutoff = MouseX.kr(40, 10000, 1);
resonance = MouseY.kr(0.01, 2.0);
RHPF.ar(signal, cutoff, resonance)}.scope(1)
)
(
{
var signal, filter, cutoff, resonance;
signal = PinkNoise.ar(mul: 0.7);
cutoff = MouseX.kr(40, 10000, 1);
resonance = MouseY.kr(0.01, 2.0);
RLPF.ar(signal, cutoff, resonance)}.scope(1)
)
(
{
var signal, filter, cutoff, resonance;
signal = PinkNoise.ar(mul: 0.7);
cutoff = MouseX.kr(40, 10000, 1);
resonance = MouseY.kr(0.01, 2.0);
BPF.ar(signal, cutoff, resonance)}.scope(1)
)
{RLPF.ar(PinkNoise.ar(0.3), LFNoise0.kr([12, 12], 500, 500), 0.02)}.play


(
{
Klank.ar(
`[[100, 200, 300, 400, 500, 600, 700, 800, 900, 1000], //freq array
[0.05, 0.2, 0.04, 0.06, 0.11, 0.01, 0.15, 0.03, 0.15, 0.2]],
PinkNoise.ar(MouseX.kr(0.01, 0.1)))
}.scope(1)
)
(
{
Klank.ar(
`[Array.series(10, 50, 50),
Array.series(10, 1.0, -0.1)],
ClipNoise.ar(0.01)
)
}.scope(1)
)


(
{
Klank.ar(
`[{exprand(60, 10000)}.dup(15)],
PinkNoise.ar(0.005)
)
}.scope(1);
)


(
{
var burstEnv, att = 0, burstLength = 0.0001, signal; //Variables
burstEnv = Env.perc(0, burstLength); //envelope times
signal = PinkNoise.ar(EnvGen.kr(burstEnv, gate: Impulse.kr(1))); //Noise burst
signal;
}.play
)


(
{
var chime, freqSpecs, burst, totalHarm = 10;
var burstEnv, att = 0, burstLength = 0.0001;
freqSpecs = `[
{rrand(100, 1200)}.dup(totalHarm), //freq array
{rrand(0.3, 1.0)}.dup(totalHarm).normalizeSum.round(0.01), //amp array
{rrand(2.0, 4.0)}.dup(totalHarm)]; //decay rate array
burstEnv = Env.perc(0, burstLength); //envelope times
burst = PinkNoise.ar(EnvGen.kr(burstEnv, gate: Impulse.kr(1))); //Noise burst
Klank.ar(freqSpecs, burst)*MouseX.kr(0.1, 0.8)
}.scope(1)
)


(
{
var chime, freqSpecs, burst, totalHarm = 10;
var burstEnv, att = 0, burstLength = 0.0001;
freqSpecs = `[
{rrand(1, 30)*200}.dup(totalHarm),
{rrand(0.1, 0.9)}.dup(totalHarm).normalizeSum,
{rrand(1.0, 3.0)}.dup(totalHarm)];
burstEnv = Env.perc(0, burstLength);
burst = PinkNoise.ar(EnvGen.kr(burstEnv, gate: Impulse.kr(1)));
Klank.ar(freqSpecs, burst)*MouseX.kr(0.1, 0.8)
}.scope(1)
)


(
{
var totalInst, totalPartials, baseFreq, ampControl, chimes, cavern;
totalInst = 5; //Total number of chimes
totalPartials = 12; //Number of partials in each chime
baseFreq = rrand(200, 1000); //Base frequency for chimes
chimes =
Mix.ar(
{
Pan2.ar(
Klank.ar(`[
{baseFreq*rrand(1.0, 12.0)}.dup(totalPartials),
Array.rand(totalPartials, 0.3, 0.9),
Array.rand(totalPartials, 0.5, 6.0)],
Decay.ar(
Dust.ar(0.2, 0.02), //Times per second, amp
0.001, //decay rate
PinkNoise.ar //Noise
)), 1.0.rand2) //Pan position
}.dup(totalInst)
);
cavern =
Mix.ar(
{
var base;
base = exprand(50, 500);
Klank.ar(
`[ //frequency, amplitudes, and decays
{rrand(1, 24) * base *
rrand(1.0, 1.1)}.dup(totalPartials),
Array.rand(10, 1.0, 5.0).normalizeSum
],
GrayNoise.ar( [rrand(0.03, 0.1), rrand(0.03, 0.1)])
)*max(0, LFNoise1.kr(3/rrand(5, 20), mul: 0.005))
}.dup(5));
cavern + chimes
}.play
)


(
{
var totalPartials = 4;
Mix.ar(
{
var base;
base = exprand(50, 10000);
Pan2.ar(
Klank.ar(
`[ //frequency, amplitudes, and decays
{rrand(1, 24) * base *
rrand(1.0, 1.1)}.dup(totalPartials),
Array.rand(10, 1.0, 5.0).normalizeSum
],
GrayNoise.ar( rrand(0.03, 0.1))
)*max(0, SinOsc.kr(6/rrand(1, 10), mul: 0.005)),
LFNoise1.kr(1))
}.dup(8));
}.play
)


(
{PMOsc.ar(LFNoise0.kr([9, 9], 300, 700),
LFNoise0.kr([9, 9], 500, 700),
LFNoise0.kr([9, 9], 6, 12),
mul: 0.5
)}.scope(1)
)


(
{
SinOsc.ar(
Latch.kr(
LFSaw.kr(Line.kr(0.1, 20, 60), 0, 500, 600),
Impulse.kr(10)),
mul: 0.3 //Volume of Blip
)
}.scope(1)
)







s.boot


(
Ndef(\plop, { arg freq=50, amp=0.4, noise=10, freq2=100, attime=0.01, dctime=0.1, ctime=5, ffreqr=0.5, rq=0.1;
	var ou, ou1, ou2, gate;
	gate = Impulse.ar(1/5);
	//ou = Decay2.ar(gate,attime,dctime);
	//ou = EnvGen.ar(Env.perc(0.00001,1.5),gate);
	ou = 1;
	ou2 = Pulse.ar(freq*[1.0154,1,0.99874,0.9789]).sum;
	//ou2 = 0;
	ou1 = PinkNoise.ar(noise);
	ou1 = 0;
	ou = ou * (ou2 + ou1);
	//ou = SinOsc.ar(freq2) * ou;
	//ou = Pulse.ar(freq2) * ou;
	//ou = ou * WhiteNoise.ar(1);
	//ou = CombL.ar(ou, (freq+10).reciprocal,(freq*[1.01,0.9987,1.0123]).reciprocal,ctime).sum;
	//ou = CombL.ar(ou, (freq+10).reciprocal,(freq*([1.01,0.9987,1.0123]+0.01)).reciprocal,5);
	//ou = Decay.ar(Impulse.ar(1),1)*ou;
	ou = EnvGen.ar(Env.perc(0.00001,5.5),gate) * ou;
	ou = Decay2.ar(gate,0.00001,50)*ou;
	ou = BPF.ar(ou,freq*ffreqr,rq);
	ou = ou * amp * AmpComp.kr(freq,30,1.23);

	ou = ou!2;

}).play;
)
NdefGui(Ndef(\rla),12)



Pdef(\a, Pbind(\freq, Prand((1..16) * 55, inf)));
Pdef(\a).play;
t = PdefGui(Pdef(\a), 4);
Pdef(\a).set(\dur, 0.125, \amp, 0.05);

(
Ndef(\bla, {
	var aa = Pulse.kr(100.2,0.5,100,200);
	LFSaw.ar(aa);
	//SinOsc.ar(500)*SinOsc.ar(300,0,0.1,SinOsc.kr(1,0,0.1,1))
}).play

)
(
Ndef(\rla, { arg freq = 100, amp=0.1, att=0.1,dc=0.1, dec=0.1,rq=0.5,ffr=0.9, fshift= (-100);
	var ou;
	freq = freq*[1.01,1,0.99];
	RandSeed.kr(Impulse.ar(1),1244);
	ou = PinkNoise.ar(1);
	ou = ou * Decay2.ar(Impulse.ar(1), att, dc);
	ou = DynKlank.ar(`[(1..15)*freq,1,dec],ou);
	ou = FreqShift.ar(ou, fshift);
	ou = BPF.ar(ou,freq*ffr,rq);
	ou = ou.sum;
	ou = ou * amp;
	ou = Limiter.ar(ou,0.9);
	ou = ou!2;
	//SinOsc.ar(500)*SinOsc.ar(300,0,0.1,SinOsc.kr(1,0,0.1,1))
}).play

)
(
SynthDef(\rla, { arg freq = 100, amp=0.1, att=0.1,dc=0.1, dec=0.1,rq=0.5,ffr=0.9, fshift= (-100);
	var ou;
	freq = freq*[1.01,1,0.99];
	RandSeed.kr(Impulse.ar(1),1244);
	ou = PinkNoise.ar(1);
	ou = ou * Decay2.ar(Impulse.ar(1), att, dc);
	ou = DynKlank.ar(`[(1..15)*freq,1,dec],ou);
	ou = FreqShift.ar(ou, fshift);
	ou = BPF.ar(ou,freq*ffr,rq);
	ou = ou.sum;
	ou = ou * amp;
	ou = Limiter.ar(ou,0.9);
	ou = ou!2;
	Out.ar(0,ou);
	//SinOsc.ar(500)*SinOsc.ar(300,0,0.1,SinOsc.kr(1,0,0.1,1))
}).add

)

(
Pdef(\ni, Pbind(
	\instrument, \rla,
	\dur, 1
)).play
)
)







(
{
t = Impulse.kr(5);
o = SinOsc.ar(TRand.kr(2000, 4000, t), mul: EnvGen.kr(Env.perc(0.001, 0.1), t))*0.1;
Mix.ar(
	Pan2.ar(
		CombL.ar(o, 2.0,
			Array.fill(5, {rrand(0.2, 1.9)}),
			1/5
		),
		Array.fill(5, {1.0.rand2})
	)
);
}.play
)
// Compare with
(
{
t = Impulse.kr(Array.fill(5, {rrand(4.0, 7.0)}));
Mix.ar(
	Pan2.ar(
		SinOsc.ar(TRand.kr(2000, 4000, t), mul: EnvGen.kr(Env.perc(0.001, 0.1), t))*0.1,
		Array.fill(5, {1.0.rand2})
));
}.play
)




(//Original patch
{
var rate = 12, att = 0.1, decay = 0.2, offset = 180;
var env, out, pan;
pan = LFNoise1.kr(1/(decay+att))-1;
env = EnvGen.kr(Env.perc(att, decay));
out = Pan2.ar(
Blip.ar(LFNoise0.ar([1.01,0.99,1]*rate, 10, offset),
(env)*12 + 1, 0.3).sum,
pan)*env;
out
}.play
)

(//Original patch
{
var rate = 12, att = 0, decay = 5.0, offset = 400;
var env, out, pan;
pan = LFNoise1.kr(1/3);
env = EnvGen.kr(Env.perc(att, decay));
out = Pan2.ar(
Blip.ar(offset, (env)*12 + 1, 0.3), pan)*env;
out = LPF.ar(out, 700, 0.7);
out
}.play
)



(
SynthDef.new("KSpluck3",
{ //Beginning of Ugen function
arg midiPitch=50, art=0.5;
var burstEnv, att = 0, dec = 0.01, legalPitches; //Variable declarations
var out, delayTime;
delayTime = [midiPitch, midiPitch + 12].midicps.reciprocal;
burstEnv = EnvGen.kr(Env.perc(att, dec));
out = PinkNoise.ar([burstEnv, burstEnv]); //Noise burst
out = CombL.ar(out, delayTime, delayTime,
art, add: out); //Echo chamber
out = RLPF.ar(out, LFNoise1.kr(2, 2000, 2100), 0.1); //Filter
DetectSilence.ar(out, doneAction:2);
Out.ar(0, out*0.8)
}
).play;
)

(
SynthDef.new("KSpluck3",
{ //Beginning of Ugen function
arg midiPitch=50, art=0.5;
var burstEnv, att = 0, dec = 0.01, legalPitches; //Variable declarations
var out, delayTime;
delayTime = [midiPitch, midiPitch + 12].midicps.reciprocal;
burstEnv = EnvGen.kr(Env.perc(att, dec));
out = PinkNoise.ar([burstEnv, burstEnv]); //Noise burst
out = CombL.ar(out, delayTime, delayTime,
art, add: out); //Echo chamber
out = RLPF.ar(out, LFNoise1.kr(2, 2000, 2100), 0.1); //Filter
DetectSilence.ar(out, doneAction:2);
Out.ar(0, out*0.8)
}
).play;
)





(
{
arg freq=200;
var ou, ou2;
ou = Klang.ar(`[({100.rand}!10)+freq,1,0]);
ou = ou * EnvGen.ar(Env.perc(0.01,0.001));
ou2 = WhiteNoise.ar(1);
ou2 = EnvGen.ar(Env.perc(0.01,2);
ou = CombL.ar(ou, 2, 0.01,0.08) + WhiteNoise.ar(1);
ou = RLPF.ar(ou, freq, 0.8);



}.play
)







