
s.meter
(
s.waitForBoot{
~seq = Mdef.force_init(true);
~synthlib = [
	\audiotrack_expander,
	\lead2,
	\pulsepass,
	\flute1,
	\miaou1,
	\ringbpf1,
	\piano2,
	\pmosc,
	\monosampler,
	\stereosampler,
	\ss_comb,
	\ss_combfreq,
].collect({ arg i; i -> i });

~effectlib = [
	\echo
].collect({arg i; i -> i });

~samplelib = [
	"sounds/perc1.wav",
	"sounds/pok1.wav",
	"sounds/amen-break.wav",
	"sounds/default.wav"
];
~seq.load_patlib( ~synthlib );
~seq.load_effectlib( ~effectlib );
~seq.set_presetlib_path("mypresets2");
~seq.append_samplelib_from_path("sounds/" );
~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );


~tf = Pfunc({ arg ev; if(ev[\stepline] == 1) { \note } { \rest } });
~ff = Pfunc({ arg ev; if(ev[\stepline1] == 1) { 100 } { \rest } });
//Debug.enableDebug = false;

Mdef.side_gui;
}
)
Mdef.main.save_project("live17");
Mdef.main.load_project("live17");

Debug.enableDebug = true;
Debug.enableDebug = false;

(
s.boot.doWhenBooted{
b = Buffer.alloc(s, 1024, 1);
c = Buffer.read(s,"sounds/a11wlk01.wav");
}
)

// spectral delay - here we use a DelayN UGen to delay the bins according to MouseX location
(
Ndef(\ava, {
	var in, chain, v;
	var ou, delay;
	in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
	chain = FFT(b, in);
	
	v = MouseX.kr(0.1, 1);
	delay = MouseY.kr(0.01, 1);
	
	chain = chain.pvcollect(b.numFrames, {|mag, phase, index|
		mag + DelayN.kr(mag, 1, v);
	}, frombin: 0, tobin: 256, zeroothers: 1);
	
	ou = 0.5 * IFFT(chain).dup;
}).play;
)

(
Ndef(\buf_grain_test, {arg gate = 1, amp = 1, sndbuf=c, envbuf=(-1);
	var pan, env, freqdev, pos;
	// use mouse x to control panning
	pan = MouseY.kr(-1, 1);
	pos = MouseX.kr(0, 1);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1),
		gate,
		levelScale: amp,
		doneAction: 2);
		GrainBuf.ar(2, Impulse.kr(70), 0.1, sndbuf, pan, pos, 2, pan, envbuf) * env
}).play

)



(
SynthDef(\basstri, { arg out=0, freq=80, gate=1, amp=0.1, pan=0, 
		lpfscale=1000, lpfbias=1000, bpfscale=7500, bpfbias=100,
		rqscale=2, rqbias=0.1, selectlbf=0.5;
	var bass=1;
	var ou, env, env1, env2, env3, sig1, sig2;
	env = EnvGen.ar(~make_adsr.(\adsr, Env.adsr(0.05,0.1,0.5,0.1)),gate,levelScale:1,doneAction:2);
	env1 = EnvGen.ar(~make_adsr.(\adsr_lpf, Env.adsr(0.4,0.4,0.5,0.1)),gate,levelScale:lpfscale,levelBias:lpfbias);
	env2 = EnvGen.ar(~make_adsr.(\adsr_bpf, Env.adsr(0.2,0.4,0.1,0.1)),gate,levelScale:bpfscale,levelBias:bpfbias);
	env3 = EnvGen.ar(~make_adsr.(\adsr_rq, Env.adsr(0.1,0.4,0.1,0.1)),gate,levelScale:rqscale,levelBias:rqbias);
	ou=LFTri.ar(freq);
	sig1 = LPF.ar(ou,env1);
	sig2 = BPF.ar(ou,env2,env3);
	ou = SelectX.ar(selectlbf, [sig1,sig2]);
	ou = ou * env;
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).store
)

s.boot
(
Mdef(\bass, Pbind(
	\instrument, \basstri,
	//\degree, Pseq([0,0,0,\r,2,\r,2,\r]+2,inf),
	\octave, 3,
	\sustain, 0.04,
	\dur, 1/4,
	//\amp, 0.8
)).play;
);
Mdef.scorepat(\bass).postcs
Mdef.scoreset(\bass).get_notes
Mdef.scorepat("lead2_l1003").postcs
Mdef.node("lead2_l1003")

Pseq([ ( 'sustain': 0, 'dur': 0.0, 'slotnum': 'rest', 'midinote': 'rest' ),
 ( 'midinote': 77, 'velocity': 0.13385826771654, 'dur': 0.32249507904053, 'sustain': 0.065526103973389 ),
 ( 'midinote': 71, 'velocity': 0.062992125984252, 'dur': 0.47602295875549, 'sustain': 0.22200989723206 ),
 ( 'midinote': 74, 'velocity': 0.66141732283465, 'dur': 0.47099089622498, 'sustain': 0.17199611663818 ),
 ( 'midinote': 76, 'velocity': 0.29133858267717, 'dur': 0.47298502922058, 'sustain': 0.1779990196228 ),
 ( 'midinote': 77, 'velocity': 0.48818897637795, 'dur': 0.69299101829529, 'sustain': 0.27500104904175 ),
 ( 'midinote': 71, 'velocity': 0.31496062992126, 'dur': 0.24400901794434, 'sustain': 0.32396101951599 ),
 ( 'midinote': 74, 'velocity': 0.43307086614173, 'dur': 0.45497894287109, 'sustain': 0.18394708633423 ),
 ( 'midinote': 76, 'velocity': 0.23622047244094, 'dur': 0.46798205375671, 'sustain': 0.19002509117126 ),
 ( 'midinote': 77, 'velocity': 0.22834645669291, 'dur': 0.50999999046326, 'sustain': 0.25004696846008 ),
 ( 'midinote': 76, 'velocity': 0.43307086614173, 'dur': 0.49094104766846, 'sustain': 0.23299813270569 ),
 ( 'midinote': 74, 'velocity': 0.41732283464567, 'dur': 0.63301491737366, 'sustain': 0.16099691390991 ),
 ( 'midinote': 71, 'velocity': 0.078740157480315, 'dur': 0.46996998786926, 'sustain': 0.15603399276733 ),
 ( 'midinote': 71, 'velocity': 0.48818897637795, 'dur': 0.47803211212158, 'sustain': 0.2450430393219 ),
 ( 'midinote': 71, 'velocity': 0.55905511811024, 'dur': 0.49999499320984, 'sustain': 0.15097594261169 ),
 ( 'midinote': 71, 'velocity': 0.64566929133858, 'dur': 1.3155919551849, 'sustain': 0.15495800971985 )
])

40.00.linlin(0,1,1000,2000)

(
l = [0, 1, 5, 9, 11]; // pentatonic scale
(60, 61..75).collect { |i| i.keyToDegree(l, 12).round }
)

Scale.directory
ScaleInfo.scales.printAll
Scale.aeolian
(
Pdef(\blal, Pbind(
	\instrument, \lead2,
	//\midinote, Pkey(\midinote)+Pgate(Pseq([0,3,4],inf), 1, \scoregate),
	//\midinote, Pfunc { arg ev; ev[\midinote].postln; if(ev[\midinote] >= 75) { ev[\midinote] + 4;\rest } { ev[\midinote] } },
	//\freq, Pfunc { arg ev; if(ev[\midinote] == \rest) { \rest } { ev[\midinote].midicps.sqrt*2}},
	\midinote, Pfunc { arg ev; 
		var scale;
		//scale = [0,1,5,9,11];
		scale = Scale.enigmatic;
		if(ev[\midinote] == \rest) { \rest } { 
			(ev[\midinote]-12).keyToDegree(scale,12).round.degreeToKey(scale,12) + 0
		}
	},
	\octave, 3,
	\dur, Pkey(\dur)/2,
	\amp, Pkey(\amp)*4,
	) <> Pn(Mdef.scorepat(\bass).collect{ arg ev; ev[\dur] = ev[\dur].round(1/8)},inf,\scoregate)
).play
)

Pdef(\bla, Pbind(
	\instrument, \lead2,
	\dur, Pkey(\dur)/2,
	\amp, Pkey(\amp)*4,
	) <> Mdef.scorepat("lead2_l1003")
).play


 ( 'sustain': 1, 'dur': 1.0, 'slotnum': 'rest', 'midinote': 69 ).play


Vocoder
Help.gui
s.boot



//just noise residual

d=Buffer.read(s,"sounds/a11wlk01.wav");

(
Ndef(\fun, {

        var in, fft, output;

        in=PlayBuf.ar(1,d,BufRateScale.kr(d),1,0,1);

		output = WalshHadamard.ar(in, MouseX.kr(0,64));
		output = RLPF.ar(output, 400,0.1);
        Out.ar(0,Pan2.ar(output));
}).play

)
Document.new("this is the title", "this is the text");


//frequency multiplication and shift with formant preservation


{
}.play

(
{

        var in, fft, output;

        in=PlayBuf.ar(1,d,BufRateScale.kr(d),1,0,1);

        output=SMS.ar(in, 60,60, 4.0,0.2, MouseX.kr(0.5,4), MouseY.kr(0,1000), 1.0);

        Out.ar(0,Pan2.ar(output[0]));
}.play
)




//transient detection via Pitch hasFreq output
//could have freq input and transient detection input to SMS to control rendering
(
{

var in, fft, sines, noise, freq, hasFreq;

//in= SoundIn.ar(0);
in=PlayBuf.ar(1,d,BufRateScale.kr(d),1,0,1);

#freq, hasFreq= Pitch.kr(in);

#sines, noise=SMS.ar(in, 50,50, 8, 1.0, MouseX.kr(0.5,4));

Pan2.ar(sines*(hasFreq.lag(0.01,0.01)) + LPF.ar(noise,MouseY.kr(100,10000,'exponential')),0.0)
}.play
)


//alternative; only pass into SMS if not a transient region
(
{

var in, fft, sines, noise, freq, hasFreq;

//in= SoundIn.ar(0);
in=PlayBuf.ar(1,d,BufRateScale.kr(d),1,0,1);

#freq, hasFreq= Pitch.kr(in);

#sines, noise=SMS.ar(if(hasFreq,in, Silent.ar), 50,50, 8, 1.0, MouseX.kr(0.5,4));

if(hasFreq,Pan2.ar(sines + LPF.ar(noise,MouseY.kr(100,10000,'exponential')), 0.0),0.25*in)

}.play
)






//having fun
(
{

        var in, fft, output;

        in=PlayBuf.ar(1,d,BufRateScale.kr(d),1,0,1);

        output=SMS.ar(in, 60,60, 4.0,0.2, MouseX.kr(0.5,4), MouseY.kr(0.0001,10000,'exponential'), LFNoise0.kr(2,0.5,0.5).round(1));

        Out.ar(0,Pan2.ar(AllpassC.ar(output[0],0.05,LFNoise2.kr(10,0.02,0.025),0.5) + CombL.ar(output[1],0.1,0.1,2),0.0));
}.play
)





//having even more fun
(
{
        var in, fft, output;

        in=SoundIn.ar(0);

        output=SMS.ar(in, 60,60, 4.0,0.1, MouseX.kr(0.01,10), MouseY.kr(-1000,1000), 0.0);

        Out.ar(0,Pan2.ar(CombL.ar(output[1],0.02,0.02,0.5) + output[0]));
}.play
)






//testing IFFT resynthesis
(
{

        var in, fft, output;

        in=PlayBuf.ar(1,d,BufRateScale.kr(d),1,0,1);

        output=SMS.ar(in, 300,MouseY.kr(1,300), 10.0,0.01, MouseX.kr(0.5,4), 0, 1.0, 1);

        Out.ar(0,output);
}.play
)



//just  sines

(
{

        var in, fft, output;

        in=PlayBuf.ar(1,d,BufRateScale.kr(d),1,0,1);

        output=SMS.ar(in, 500,500, MouseX.kr(1.0,10.0).round(1.0),MouseY.kr(0.001,20.0,'exponential'), 1.0, 1.0,0,1);

        Out.ar(0,Pan2.ar(output[0]));
}.play

)




//having even more fun, this time with IFFT
(
{
        var in, fft, output;

        in=SoundIn.ar(0);

        output=SMS.ar(in, 200,200, 4.0,0.1, MouseX.kr(0.01,10), MouseY.kr(-1000,1000), 0.0, useifft:1);

        Out.ar(0,Pan2.ar(CombL.ar(output[1],0.02,0.02,0.5) + output[0]));
}.play
)





//experimenting with ampmult
(
{
        var in, fft, output;

        in=SoundIn.ar(0);

        output=SMS.ar(in, 200,200, 4.0,0.01,MouseY.kr(0.01,100,'exponential'),0, formantpreserve:1.0, useifft:1, ampmult:MouseX.kr(0.0,10.0));

        Out.ar(0,output);
}.play
)






(
Ndef(\clari,{
StkClarinet.ar (freq: 140, reedstiffness: 34, noisegain: 8, vibfreq: 101, vibgain: 21, breathpressure: 14, trig: 1, mul: 1, add: 0) ! 2
}).play
)




// before anything else

s = Server.local.boot;

b = Buffer.read(s, "sounds/a11wlk01.wav");


// simple feedback delay

(

SynthDef('help-switchdelay-1', { arg out=0, bufnum;

Out.ar(out,

Pan2.ar(

SwitchDelay.ar(

PlayBuf.ar(numChannels: 1, bufnum: bufnum, loop: 0) * 0.5,

delaytime: 0.4,

delayfactor: 0.6,

wetlevel: 0.7

)

)

);

}).send(s);

)


x = Synth('help-switchdelay-1', [\bufnum, b]);

x.free;



// this time, change the buffer read pointer periodically.

(

SynthDef('help-switchdelay-2', { arg out=0, bufnum, delaytime;

Out.ar(out,

Pan2.ar(

SwitchDelay.ar(

PlayBuf.ar(numChannels: 1, bufnum: bufnum, loop: 0) * 0.5,

wetlevel: 0.6,

delaytime: Select.kr(

Stepper.kr(Impulse.kr(0.5), 0, 0, 3),

#[ 0.02, 0.1, 0.725, 0.25 ]Â 

)

)

)

);

}).send(s);

)


x = Synth('help-switchdelay-2', [\bufnum, b, \loop, 0, \delaytime, 2.0]);

x.free;




{(NTube.ar(WhiteNoise.ar, 0.97,`[0.5,-0.7],`[0.01,0.02,0.01])*0.1).dup}.play



//can get it sound like respiration!Â 

{(NTube.ar(WhiteNoise.ar*SinOsc.ar(0.5),`[0.97,1.0,1.0,1.0,0.97],`[0.5,MouseY.kr(-1.0,1.0),0.2],`([0.01,0.02,0.01,0.005]*MouseX.kr(0.01,1.0)))*0.1).dup}.play



{(NTube.ar(PinkNoise.ar*SinOsc.ar(0.25),`[0.97,1.0,1.0,1.0,1.0,0.97],`[0.5,MouseY.kr(-1.0,1.0),0.2,-0.4],`([0.01,0.02,0.01,0.005,0.05]*MouseX.kr(0.001,1.0,'exponential')))*0.1).dup}.play



//tap on microphone in 16 beat and move mouse around...Â 

{(NTube.ar(SoundIn.ar,`[0.97,1.0,1.0,1.0,1.0,0.97],`[0.5,MouseY.kr(-1.0,1.0),0.2,-0.4],`([0.01,0.02,0.01,0.005,0.05]*MouseX.kr(0.001,1.0,'exponential')))*0.5).dup}.play



//delays; why stereo? warning: quite piercing

{(NTube.ar(Impulse.ar(MouseX.kr(16,1600))*MouseY.kr(0.0,1.0),`(Array.rand(11,0.95,0.99)),`(Array.series(9,0.8,-0.1)),`(Array.rand(10,0.01,0.05)) )*0.025).dup}.play




//can end up doing a huge amount of recirculation warning: quite piercing

{Limiter.ar(NTube.ar(Impulse.ar([MouseX.kr(16,1600), MouseX.kr(17,2700)])*MouseY.kr(0.0,1.0),`([0.87]++(0.99.dup(9))++[0.87]),`(Array.rand(9,0.8,1.0)),`(Array.fill(10,{0.01})) )*0.1,0.9,0.01)*0.1}.play



//can end up doing a huge amount of recirculationÂ 

{((Limiter.ar(NTube.ar(Impulse.ar(440)*MouseX.kr(0.0,1.0),MouseY.kr(0.0,0.99),`(Array.rand(99,0.0,1.0)),`(Array.rand(100,0.0001,0.01)) ),0.99,0.01).min(1.0).max(-1.0))*0.1).dup(2)}.play



//dynamic changing of loss factors is great

(

{

var my= MouseY.kr(0.0,0.99);


Limiter.ar(NTube.ar(PinkNoise.ar*EnvGen.ar(Env.perc(0.01,0.05),MouseX.kr(0.0,1.0)>0.5),my,`(Array.rand(49,0.0,1.0)),`(Array.rand(50,0.0001,0.01)) ),0.99,0.01).min(1.0).max(-1.0)

}.play

)




//1-D vocal tract model: data for Ah sound for cross-sectional areas of vocal tract (see http://www-users.york.ac.uk/~dtm3/vocaltract.html and associated publications)

//a=FileReader.read("/Users/nickcollins/Desktop/VowelAreaFunctions/MRI/JASAPaper/A-bart.txt");Â 

//

//b= Array.fill(a.size.div(2),{|i| a[2*i][0]});Â 

//b.size

//c= b[0..43].asFloat


//run at higher sampling rate?Â 


(

var areassource= [ 0.45, 0.2, 0.26, 0.21, 0.32, 0.3, 0.33, 1.05, 1.12, 0.85, 0.63, 0.39, 0.26, 0.28, 0.23, 0.32, 0.29, 0.28, 0.4, 0.66, 1.2, 1.05, 1.62, 2.09, 2.56, 2.78, 2.86, 3.02, 3.75, 4.6, 5.09, 6.02, 6.55, 6.29, 6.27, 5.94, 5.28, 4.7, 3.87, 4.13, 4.25, 4.27, 4.69, 5.03 ];

var areas;Â 

var loss, karray, delayarray;Â 


//convert to sequence of kÂ 


//average length of human male vocal tract 16.9cm (14.1cm adult female)Â  speed of sound 340.29 m/s. So delay of vocal tract isÂ 

//0.169/340.29 = 0.00049663522289812 seconds

//0.0005*44100 is about 22 samples, so less than one sample per section of the throat if more than 22 measurements used!Â 

//need higher sampling rate, or less sections in model


//Loy p347, p358, Kelly Lochbaum junctions used in TubeN

//k= (Z1-Z0)/(Z1+Z0); //Z inversely proportional to AÂ 

//k= ((A0-A1)/(A0A1))/((A0+A1)/(A0A1)) ie similar relation for ZÂ 


//take every 4th

areas= Array.fill(11,{|i| areassource[4*i]});Â 


//about 2 samples delay for each section!Â 


loss=0.99;Â 


karray= Array.fill(10,{|i| (areas[i]-areas[i+1])/(areas[i]+areas[i+1])});Â 


//delayarray= Array.fill(11,{0.00049663522289812/11.0});Â 

delayarray= Array.fill(11,{0.000046}); //any smaller and Nyquist problems arise...Â 


//Impulse too predictable, need a richer low pass filtered and frequency modulated glottal oscillationÂ 

//Dust.ar(MouseX.kr(100,400),0.9,0.1*PinkNoise.ar)

{

Limiter.ar(NTube.ar(PinkNoise.ar(0.3),loss, `karray, `delayarray , 0.5),0.99,0.01).min(1.0).max(-1.0)

}.play


)





//Next patch only works properly at sampling rate of 192kHz!Â 

(

var areassource= [ 0.45, 0.2, 0.26, 0.21, 0.32, 0.3, 0.33, 1.05, 1.12, 0.85, 0.63, 0.39, 0.26, 0.28, 0.23, 0.32, 0.29, 0.28, 0.4, 0.66, 1.2, 1.05, 1.62, 2.09, 2.56, 2.78, 2.86, 3.02, 3.75, 4.6, 5.09, 6.02, 6.55, 6.29, 6.27, 5.94, 5.28, 4.7, 3.87, 4.13, 4.25, 4.27, 4.69, 5.03 ];

var areas;Â 

var loss, karray, delayarray;Â 


areas= Array.fill(44,{|i| areassource[i]});Â 


loss=0.99;Â 


karray= Array.fill(43,{|i| (areas[i]-areas[i+1])/(areas[i]+areas[i+1])});Â 


delayarray= Array.fill(44,{0.00049663522289812/44.0});Â 


{

Limiter.ar(NTube.ar(Decay.ar(Impulse.ar(MouseX.kr(10,200)+LFNoise1.kr(7,4),0.0,0.5),MouseY.kr(0.01,0.2)),loss, `karray, `delayarray , 0.5),0.99,0.01).min(1.0).max(-1.0)

}.play

)






//loud hammering

(

{

var delays, source, loss, k;Â 

var trigger;

trigger= Impulse.kr(MouseY.kr(1,10));


loss=`(Array.fill(7,{EnvGen.ar(Env([rrand(0.95,1.0),rrand(0.95,1.0),rrand(0.5,0.9),rrand(0.0,0.1)],[0.1,rrand(0.05,0.5),rrand(0.05,0.5)]),trigger)}));Â 

k= `(Array.fill(5,{rrand(0.7,1.0)}));

delays=`(Array.fill(6,{exprand(0.01,0.2)}));Â 


delays.value.sum.postln;


source= WhiteNoise.ar(0.5)*EnvGen.ar(Env([1,1,0],[delays.value.sum,0.0]), trigger);


Out.ar(0,Pan2.ar(Limiter.ar(NTube.ar(source,loss, k, delays),0.99,0.01).min(1.0).max(-1.0),0.0));Â 

}.play


)




//could be piercing if sine frequencies put higher, also potentially high CPU cost, be carefulÂ 

(

var n=7;Â 


SynthDef(\ntubefx,{|out=0|


ReplaceOut.ar(out, Limiter.ar(In.ar(0,2),0.99,0.01))

}).send(s);Â 


SynthDef(\ntubehelp,{|out=0, dur=0.5, pan=0.0, amp=0.1, lagtime=0.1, freq=440|

var env;Â 

var lossarray, karray, delaylengtharray;Â 

//Decay2.ar(Impulse.ar(freq),lagtime,0.01)

var source= SinOsc.ar(freq)*(0.95+(Line.kr(0,1,0.2)*0.05*BrownNoise.ar));Â 


env= EnvGen.ar(Env([0,1,0.8,0.8,0],[0.01,0.01,dur,0.5]),doneAction:2);Â 


lossarray = Control.names([\lossarray]).ir(Array.rand(n+1, 0.8,0.99));

karray= Control.names([\karray]).ir(Array.rand(n-1, -0.5,0.5));

delaylengtharray= Control.names([\delaylengtharray]).ir(Array.rand(n, 0.01,0.05));

Out.ar(out,Pan2.ar(LeakDC.ar(env*Limiter.ar(NTube.ar(amp*source,`lossarray, `karray, `delaylengtharray),0.99,0.01).min(1.0).max(-1.0)),pan));Â 


}).send(s);Â 

)



(

var n=7;Â 

var group= Group.basicNew(s,1);Â 


t.stop;

t={

var durs;Â 

var inverted;Â 

var range= rrand(0.1,1.0);Â 

var minloss= rrand(0.7,0.98);

var maxloss= (minloss+rrand(0.0,0.1)).min(0.99);Â 

var maxdur= rrand(0.001,0.05);Â 

var fx= Synth.tail(group,\ntubefx);Â 


durs= [0.01,0.1,0.2,0.5,1.0];Â 

inverted= durs.reverse.normalizeSum;Â 


inf.do{


if(0.1.coin) {range= rrand(0.1,1.0);};Â 

if(0.07.coin) {minloss= rrand(0.8,0.98); maxloss= (minloss+rrand(0.0,0.1)).min(0.99);};Â 

if(0.05.coin) {maxdur=exprand(0.0025,0.05)};Â 


a= Synth.head(group,\ntubehelp,[\dur, rrand(0.1,3.0), \freq, exprand(1,1000).round(30.0)+(3.rand2),\lagtime, rrand(0.001,0.1), \pan, rrand(-0.5,0.5), \amp, exprand(0.01,0.3), \lossarray, Array.rand(n+1, minloss,maxloss), \karray, Array.rand(n-1,range.neg,range), \delaylengtharray, Array.rand(n, 0.001, maxdur)]);


//

//s.bind({

//a.set(\freq, exprand(1,4000),\lagtime, rrand(0.001,0.1), \pan, rrand(-0.1,0.1), \amp, exprand(0.01,0.5));

//a.setn(\lossarray, Array.rand(n+1, minloss,maxloss));

//a.setn(\karray, Array.rand(n-1,range.neg,range));

//a.setn(\delaylengtharray, Array.rand(n, 0.001, maxdur));

//});Â 



durs.wchoose(inverted).wait;

};


}.fork;

)




{ MoogVCF.ar(

Pulse.ar([40,121],[0.3,0.7]),SinOsc.kr(LFNoise0.kr(0.42).range(0.001,2.2)).range(30,4200),Â 

0.83)}.play(s);



a = {Metro.ar(60, 1)}.play

a.free;


a = {Decay.kr(Metro.kr(XLine.kr(60, 120, 5), 1)) * SinOsc.ar(440, 0, 0.1)}.play;

a.free;

(

a = {

Decay.kr(

Metro.kr(

LFNoise2.kr(0.2).range(30, 240),Â 

Dseq([1, 0.25, 0.5, 0.25], inf))) *Â 

SinOsc.ar(440, 0, 0.1)

}.play;

)

a.free;




s = Server.internal;

s.boot;


(

// This filters a simple sine wave, producing a chaotic result

x = {

var sig, out;

sig = SinOsc.ar(660);

out = Friction.ar(sig, friction: 5.41322e-5, mass: 8.05501);

Pan2.ar(out, 0, 0.1);

}.scope

)


x.free;


(

// Modulate the parameters by moving the mouse. Left speaker is original (modulated) sine wave, right speaker is filtered.

x = {

var sig, out;

sig = SinOsc.ar((LFPulse.kr(0.5) + LFPulse.kr(0.33)).range(220, 660).lag(0.1));

out = Friction.ar(sig, friction: MouseX.kr(0.00001, 0.03, 1), mass: MouseY.kr(0.2, 10, 1));

[sig, out] * 0.1;

}.scope

)


x.free;


(

// Some Ringz oscillators, each with a _separate_ Friction1, then merged to create a "rusty" klank.

// Note the way the effect changes as the sound dies away.

x = {

var imp, klank, rusty;

imp = Impulse.ar(1, 0, 0.1);

klank = Ringz.ar(imp, [800, 1071, 1153, 1723]);


rusty = Friction.ar(klank,Â 

friction: 1.75584e-5,Â 

mass: 2.69789);


Pan2.ar(rusty.sum)

}.play(s);

)


x.free;


(

// In this one we can play with the DC offset and the spring stiffness

x = {

var sig, out;

sig = SinOsc.ar(330) + MouseX.kr(0.01, 10, 1);


out = Friction.ar(sig, friction: 5.41322e-5, mass: 8.05501,Â 

spring: MouseY.kr(0,1));

Pan2.ar(out * 0.1);

}.scope

)


x.free;


(

// Similar, but this time as a filter for a control-rate signal.Â 

// Converts boring sinusoidal freq undulation into something much more interesting...

x = {

var sig, out;

sig = LFPar.kr(33) + MouseX.kr(0.01, 10, 1);


out = Friction.kr(sig, friction: 5.41322e-5, mass: 8.05501,Â 

spring: MouseY.kr(0,1));

out = SinOsc.ar(out.range(150,500));

Pan2.ar(out * 0.1);

}.scope

)


x.free;


{ Streson.ar(LFSaw.ar([220, 180], 0, mul:EnvGen.kr(Env.asr(0.5, 1, 0.02), 1.0) * 0.2), LinExp.kr(LFCub.kr(0.1, 0.5*pi), -1, 1, 280, 377).reciprocal, 0.9, 0.3) }.play
{ Streson.ar(LFSaw.ar(120, 0, mul:EnvGen.kr(Env.asr(0.5, 1, 0.02), LFPulse.ar(1.0,0,0.1)) * 0.2), MouseX.kr(0.001,1),MouseY.kr(0.01,1) ) }.play

{ Streson.ar(LFSaw.ar([120,121], 0, mul:EnvGen.kr(Env.asr(0.5, 1, 0.02), LFPulse.ar(1.0,0,0.1)) * 0.2).sum ! 2, MouseX.kr(0.001,1),MouseY.kr(0.01,1) ) }.play



{SineShaper.ar(SinOsc.ar([400, 404], 0, 0.2), MouseX.kr(0, 1))}.play
{SineShaper.ar(LFSaw.ar([400, 401], 0, 0.2).sum !2, MouseX.kr(0, 1))}.play

{SineShaper.ar(SoundIn.ar, MouseX.kr(0, 1))}.play




{Disintegrator.ar(SinOsc.ar([400, 404], 0, 0.2), MouseX.kr(0, 1), MouseY.kr(0, 1))}.play

{Disintegrator.ar(SoundIn.ar, MouseX.kr(0, 44100), MouseY.kr(0, 1))}.play

(
{
	var ou, sig1;
	sig1 = SinOsc.ar([400, 401], 0, 0.2).sum;
	ou = Disintegrator.ar(sig1, MouseX.kr(0, 1), MouseY.kr(0, 1));
	ou = ou - sig1;
	//ou = BPF.ar(ou, 400, 7.25);
	ou = SinOsc.ar(ou.range(100,500));
	ou !2;
}.play
)


{CrossoverDistortion.ar(SinOsc.ar([400, 404], 0, 0.2), MouseX.kr(0, 1), MouseY.kr(0, 1))}.play

{CrossoverDistortion.ar(SoundIn.ar, MouseX.kr(0, 1), MouseY.kr(0, 1))}.play

{ RLPFD.ar(Mix(LFSaw.ar([120, 180], 0, 0.33)), LinExp.kr(LFCub.kr(0.1, 0.5*pi), -1, 1, 280, 1500), 0.6, 0.5, mul:3).dup }.play



(
{
    NLFiltN.ar(
        LFSaw.ar([120, 180], 0, mul:0.1),
        0.5,
        -0.04,
        0.8,
        0.2,
        LFCub.kr(0.2, [0, 0.5 * pi], 63, 103)
    )
}.play
)

(
{
    NLFiltN.ar(
        LFSaw.ar(XLine.kr([60, 90], [360, 540], 20), 0, mul:0.1),
        0.0,
        0.0,
        0.7,
        0.4,
        LFCub.kr(0.2, [0, 0.5 * pi], 3, 9)
    )
}.play
)

(
// internal mirroring keeps it from blowing up and makes for some gross distortion
// this is a little loud
{
    NLFiltN.ar(
        LFPulse.ar([100, 150], mul:0.1),
        LFNoise2.kr(1).range(0.3, 0.5),
        0.2,
        0.7,
        0.4,
        LFCub.kr(0.2, [0, 0.5 * pi], 100, 400)
    )
}.play
)

(
Ndef(\bla,{
	var ou;
}).play
)


(
SynthDef(\nlfiltn, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, ffreq=500, rq=0.5;
	var ou;
	ou = LFPulse.ar(freq*[1,1.01], mul:0.1).sum;
    ou = NLFiltN.ar(
        ou,
        0.35,
        -0.3,
        0.95,
        0.2,
        LFCub.kr(0.2, [0, 0.5 * pi], 50, 100)
    );
	ou = RLPFD.ar(ou, ffreq, rq);
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)

(
Pdef(\plop, Pbind(
	\instrument, \nlfiltn,
	\degree, Pseq([0,2,4],inf),
	\ffreq, 1700,
	\rq, 0.4,
	\dur, 1,
	\amp, 0.4
)).play;
);
