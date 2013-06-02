(
{
	var a, b, c, d, n, e, f,  out;

	a = Impulse.ar(8)*1.5;

	b = WhiteNoise.ar * Env([1.0,1.0,0.0],[0.01,0.01],\step).ar(0, Impulse.ar(1 )) ;
	b = FreeVerb.ar(b, 0.5, 0.4)*2.5;

	c = SinOsc.ar(40) * Env.perc(0.01,0.2).ar(0, TDuty.ar(Dseq([1/4,1/2,1/8,1/8],inf)));
	5.do{ c = (c.distort + c)*0.75};
	c = c * 1.5;

	d = LPF.ar(Saw.ar([20,47]).sum , XLine.ar(4000,200,0.5)) * Env.perc.ar(0, Impulse.ar(1/16)) * 0.5;
	d = (GVerb.ar( d , roomsize:10, revtime:6) * 200).clip(-1.0,1.0) * 0.3;

	n = 12;
	e = ( Saw.ar( 40*(1..n) * ({ LFNoise1.ar(0.1).range(1,1.01) } ! n) ) *
		({ LFNoise1.ar(0.1).range(0.0,1.0) }!n)).sum * 10;
	e = CombC.ar(e, 0.1, 0.1, 4) + e;
	e = e.tanh * 0.3 * SinOsc.ar(0.05).range(0.5,1.0);
	e = e.dup;
	e = e * SinOsc.ar(0.03).range(0.2,1.0) * 0.5;

	f = Blip.ar(100) * Blip.ar(100) * Env([0.0,0.0,1.0],[8,8],[\step,\linear,\step])
	.ar(0, Impulse.ar(1/16)) * 2 ;

	out = ((a + b + c + f) ! 2) + d + e;
	out = out * 0.2

}.play
)



(
fork{
	//~media = (samples:"/home/ggz/Musique/hydrogenkits/Roland TR-808/")
	//---------Source Files---------
	~numOfPatterns = 66;
	~synthDefToUse = \bufPlay2; //Change to \bufPlay2 to use stereo samples
	~rate = 0.1; //Rate of playback
	//~source = [ // Create an array of your samples. ~media.samples points at my base sample directory and is defined in my startup file.
	//	~media.samples ++ "808/kicks/808 kick 6.wav",
	//	~media.samples ++ "808/snares/808 snare 6.wav",
	//	~media.samples ++ "808/hats/808 hat 2.wav",
	//	~media.samples ++ "808/other/808 cowbell.wav",
	//	~media.samples ++ "808/other/808 clap.wav",
	//	~media.samples ++ "808/other/808 rim.wav",
	//	~media.samples ++ "808/other/808 maraca.wav"
	//];
	//~source = "/home/ggz/Musique/hydrogenkits/Roland TR-808/*.flac".pathMatch.keep(8);
	~source = "/home/ggz/Musique/hydrogenkits/HardElectro1/*.wav".pathMatch.keep(8);
	//~source = "/home/ggz/Musique/hydrogenkits/EasternHop-1/*.wav".pathMatch.keep(8);
	Buffer.freeAll;
	~buffers = ~source.collect{|source| Buffer.read(s, source)};
	~patterns = ~numOfPatterns.collect{|val| Pbind(*[dur: Pn(1/val, val)])};
	~randPattern = Prand(~patterns, 1);
	~patterns = ~buffers.collect{|buf| Pbind(*[bufnum: buf]) <> ~randPattern};
	SynthDef(\bufPlay, {|out = 0, bufnum, rateScale = 1, amp = 1| Out.ar(out, PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rateScale, doneAction: 2) * amp!2);}).add;
	SynthDef(\bufPlay2, {|out = 0, bufnum, rateScale = 1, amp = 1| Out.ar(out, PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * rateScale, doneAction: 2) * amp);}).add;
	s.sync;
	Ppar(~patterns, inf).play(TempoClock(~rate, 0), protoEvent: (instrument: ~synthDefToUse, amp: 0.25));  //Change to /bufPlay2 for stereo samples
};
)





(
Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0,2,\r,0, 2,3,0,1, \r, 0,5,4,0, 2,\r,0,1],inf),
	\mtranspose, Pstep(Pseq([0,3,4,3],inf),0.5),
	\lag, Pseq([0,0.1,0,-0.1]/4,inf),
	\dur, 0.125,
	\amp, 0.1
)).play;
)
