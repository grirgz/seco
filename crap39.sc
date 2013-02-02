

play{({|k|({|i|y=SinOsc;y.ar(i*k*k,y.ar(i*k**i/[4,5])*Decay.kr(Dust.kr(1/4**i),y.ar(0.1)+1*k+i,k*999))}!8).product}!16).sum}//#supercollider



p = Platform.resourceDir +/+ "sounds/a11wlk01.wav";

b = Buffer.read(s, p);


(

{
	var sig;
	var freq;

	freq = MouseX.kr(0,590);
	sig = LFPulse.ar( freq , 0, 
		PlayBuf.ar(

			1,

			bufnum: b.bufnum,

			rate: LFNoise1.kr([0.2, 0.2]) * BufRateScale.kr(b.bufnum),

			loop: 1

		) * MouseY.kr(0.5,100),
		//* LFNoise1.kr(0.1).exprange(4, 20),
		0.25 //* Decay2.kr(Dust.kr(2.0), 0.01, 2.0)
	);
	sig = RLPF.ar(sig, freq,0.2);

}.play

)



// mnml rmx
x.free;x=play{({|k|({|i|y=SinOsc;y.ar(i*k*k,y.ar(i*k**i/{(2**(-2..8)).choose}!3)*Decay.kr(Dust.kr(1/4**i),y.ar(0.1)+k+i,k*999))}!8).product}!8).sum}
