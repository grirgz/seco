ULib.startup;
GUI.qt
GUI.swing
s.boot



b = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");

(
SynthDef("help-magMul", { arg out=0;
        var inA, chainA, inB, chainB, chain;
        inA = WhiteNoise.ar(0.2);
        inB = LFSaw.ar(100, 0, 0.2);
        chainA = FFT(LocalBuf(2048), inA);
        chainB = FFT(LocalBuf(2048), inB);
        chain = PV_MagMul(chainA, chainB);
        Out.ar(out, 0.1 * IFFT(chain).dup);
}).play(s);
)

(
SynthDef("help-magMul2", { arg out=0, soundBufnum=2;
        var inA, chainA, inB, chainB, chain;
		var ou;
        inA = LFSaw.ar([100, 150], 0, 0.2);
        inA = LFSaw.ar(100, 0, 0.2);
		inA = SinOsc.ar([400,500]);

        inA = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum)*1/2, loop: 1);
        inB = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        chainA = FFT(LocalBuf(2048), inA);
        chainB = FFT(LocalBuf(2048), inB);
        chain = PV_MagMul(chainA, chainB);
		ou = IFFT(chain);
		ou = LPF.ar(ou, 500);
        Out.ar(out,  0.43 * ou ! 2);
}).play(s, [\soundBufnum, b.bufnum]);
)

b.free


(
SynthDef("help-mul", { arg out=0;
        var inA, chainA, inB, chainB, chain ;
        inA = SinOsc.ar(500, 0, 0.5);
        inB =  SinOsc.ar(Line.kr(100, 400, 5), 0, 0.5);
        chainA = FFT(LocalBuf(2048), inA);
        chainB = FFT(LocalBuf(2048), inB);
        chain = PV_Mul(chainA, chainB);
        Out.ar(out,  0.1 * IFFT(chain).dup);
}).play(s);
s.scope;
)

(
SynthDef("help-mul2", { arg out=0;
        var inA, chainA, inB, chainB, chain ;
        inA = SinOsc.ar(500, 0, 0.5) * Line.kr;
        inB = LFNoise1.ar(20);
        chainA = FFT(LocalBuf(2048), inA);
        chainB = FFT(LocalBuf(2048), inB);
        chain = PV_Mul(chainA, chainB);
        Out.ar(out,  0.1 * IFFT(chain).dup);
}).play(s);
s.scope;
)



(
SynthDef("help-add", { arg out=0, soundBufnum;
        var inA, chainA, inB, chainB, chain ;
		var ou;
        inA = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        inB =  PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum) * 0.5, loop: 1);
        chainA = FFT(LocalBuf(2048), inA);
        chainB = FFT(LocalBuf(2048), inB);
        chain = PV_Add(chainA, chainB);
		ou = IFFT(chain);
		ou = inA + inB;
        Out.ar(out,  0.1 * ou.dup);
}).play(s, [\soundBufnum, b.bufnum]);
)



        d = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");

//// crossfade between original and magmul-ed whitenoise
(
x = { var in, in2, chain, chainB, chainC;
        in = PlayBuf.ar(1, d, BufRateScale.kr(d), loop: 1) * 2;
        in2 = WhiteNoise.ar;
        chain = FFT(LocalBuf(2048), in);
        chainB = FFT(LocalBuf(2048), in2);
        chainC = PV_Copy(chain, LocalBuf(2048));
        chainB = PV_MagMul(chainB, chainC);
        XFade2.ar(IFFT(chain), IFFT(chainB) * 0.1, SinOsc.kr(0.1, 1.5pi)) ! 2;
}.play(s);
)
x.free;




//// as previous but with Blip for 'vocoder' cross synthesis effect
(
x = { var in, in2, chain, chainB, chainC;
        in = PlayBuf.ar(1, d, BufRateScale.kr(d), loop: 1) * 2;
        in2 = Blip.ar(100, 50);
        chain = FFT(LocalBuf(2048), in);
        chainB = FFT(LocalBuf(2048), in2);
        chainC = PV_Copy(chain, LocalBuf(2048));
        chainB = PV_MagMul(chainB, chainC);
        XFade2.ar(IFFT(chain), IFFT(chainB) * 0.1, SinOsc.ar(0.1)) ! 2;
}.play(s);
)
x.free;


//// Spectral 'pan'
(
x = { var in, chain, chainB, pan;
        in = PlayBuf.ar(1, d, BufRateScale.kr(d), loop: 1);
        chain = FFT(LocalBuf(2048), in);
        chainB = PV_Copy(chain, LocalBuf(2048));
        pan = MouseX.kr(0.001, 1.001, 'exponential') - 0.001;
        chain = PV_BrickWall(chain, pan);
        chainB = PV_BrickWall(chainB, -1 + pan);
        0.5 * IFFT([chain, chainB]);
}.play(s);
)
x.free;


(
s.waitForBoot {
        b = Buffer.alloc(s,2048,1);
        c = Buffer.alloc(s,2048,1);
        d = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");
        e = Buffer.alloc(s,2048,1);
        f = Buffer.alloc(s,2048,1);
}
)


//// proof of concept
(
x = { var inA, chainA, inB, chainB, chain;
        inA = LFClipNoise.ar(100);
        chainA = FFT(b, inA);
        chainB = PV_Copy(chainA, c);
        IFFT(chainA) - IFFT(chainB); // cancels to zero so silent!
}.play(s);
)
x.free;
// IFFTed frames contain the same windowed output data
b.plot(\b, Rect(200, 430, 700, 300)); c.plot(\c, Rect(200, 100, 700, 300));
GUI.qt



//// Multiple Magnitude plots
(
x = { var in, chain, chainB, chainC;
        in = WhiteNoise.ar;
        chain = FFT(b, in);
        PV_Copy(chain, LocalBuf(2048)); // initial spectrum
        chain = PV_RectComb(chain, 20, 0, 0.2);
        PV_Copy(chain, LocalBuf(2048)); // after comb
        2.do({chain = PV_MagSquared(chain)});
        PV_Copy(chain, LocalBuf(2048)); // after magsquared
        0.00001 * Pan2.ar(IFFT(chain));
}.play(s);
)
x.free;

(
c.getToFloatArray(action: { arg array;
        var z, x;
        z = array.clump(2).flop;
        // Initially data is in complex form
        z = [Signal.newFrom(z[0]), Signal.newFrom(z[1])];
        x = Complex(z[0], z[1]);
        {x.magnitude.plot('Initial', Rect(200, 560, 700, 200))}.defer
});
e.getToFloatArray(action: { arg array;
        var z, x;
        z = array.clump(2).flop;
        // RectComb doesn't convert, so it's still complex
        z = [Signal.newFrom(z[0]), Signal.newFrom(z[1])];
        x = Complex(z[0], z[1]);
        {x.magnitude.plot('After RectComb', Rect(200, 330, 700, 200))}.defer
});
f.getToFloatArray(action: { arg array;
        var z, x;
        z = array.clump(2).flop;
        // MagSquared converts to Polar
        x = Signal.newFrom(z[0]); // magnitude first
        {x.plot('After MagSquared', Rect(200, 100, 700, 200))}.defer
})
)

[b, c, d, e, f].do(_.free); // free the buffers



s.boot;

(
b = Buffer.alloc(s,2048,1);
c = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");
)

(
SynthDef("help-binStretch", { arg out=0, bufnum=0;
        var in, chain;
        in = LFSaw.ar([200,200+SinOsc.kr(0.1)], 0, 0.2).sum;
        chain = FFT(bufnum, in);
        chain = PV_BinShift(chain, MouseX.kr(0.25, 4, \exponential) );
        Out.ar(out, 0.5 * IFFT(chain).dup);
}).play(s,[\out, 0, \bufnum, b.bufnum]);
)

(
SynthDef("help-binStretch2", { arg out=0, bufnum=0, soundBufnum=2;
        var in, chain;
        //in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        in = AudioIn.ar(1,0.5);
        chain = FFT(bufnum, in);
        chain = PV_BinShift(chain, MouseX.kr(0.25, 4, \exponential) );
        Out.ar(out, 0.5 * IFFT(chain).dup);
}).play(s,[\out, 0, \bufnum, b.bufnum, \soundBufnum, c.bufnum]);
)

(
SynthDef("help-binStretch2", { arg out=0, bufnum=0, soundBufnum=2;
        var in, chain1, chain2;
		var in2, in3, ou;
        //in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        in = AudioIn.ar(1,0.5);
		in2 = LPF.ar(in, 500);
		in3 = HPF.ar(in, 500);
        chain1 = FFT(LocalBuf(2048), in);
        chain1 = PV_BinShift(chain1, MouseX.kr(0.25, 4, \exponential) );
        chain2 = FFT(LocalBuf(2048), in);
        chain2 = PV_BinShift(chain2, MouseY.kr(0.25, 4, \exponential) );
		ou = IFFT(chain1) + IFFT(chain2);
        Out.ar(out, 0.5 * ou.dup);
}).play(s,[\out, 0, \bufnum, b.bufnum, \soundBufnum, c.bufnum]);
)

(
SynthDef("help-binShift", { arg out=0, bufnum=0;
        var in, chain;
        in = LFSaw.ar(200, 0, 0.2);
        chain = FFT(bufnum, in);
        chain = PV_BinShift(chain, 1, MouseX.kr(-128, 128) );
        Out.ar(out, 0.5 * IFFT(chain).dup);
}).play(s,[\out, 0, \bufnum, b.bufnum]);
)

(
SynthDef("help-binShift2", {  arg out=0, bufnum=0, soundBufnum=2;
        var in, chain;
        in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        chain = FFT(bufnum, in);
        chain = PV_BinShift(chain, 1, MouseX.kr(-128, 128) );
        Out.ar(out, 0.5 * IFFT(chain).dup);
}).play(s,[\out, 0, \bufnum, b.bufnum, \soundBufnum, c.bufnum]);
)



(
SynthDef("help-binWipe", { arg out=0;
        var inA, chainA, inB, chainB, chain;
        inA = WhiteNoise.ar(0.2);
        inB = LFSaw.ar(100, 0, 0.2);
        chainA = FFT(LocalBuf(2048), inA);
        chainB = FFT(LocalBuf(2048), inB);
        chain = PV_BinWipe(chainA, chainB, MouseX.kr(-1, 1));
        Out.ar(out, 0.1 * IFFT(chain).dup);
}).play(s);
)

(
SynthDef("help-binWipe2", { arg out=0, soundBufnum=2;
        var inA, chainA, inB, chainB, chain;
        inA = WhiteNoise.ar(0.2);
        inA = LFSaw.ar(100, 0, 0.2);
        inB = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        chainA = FFT(LocalBuf(2048), inA);
        chainB = FFT(LocalBuf(2048), inB);
        chain = PV_BinWipe(chainA, chainB, MouseX.kr(-1, 1));
        Out.ar(out, 0.1 * IFFT(chain).dup);
}).play(s, [\soundBufnum, c]);
)




//explore the effect
(
SynthDef("conformer1", { arg soundBufnum;
        var in, chain;
        in = AudioIn.ar(1,0.5);
        //in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        chain = FFT(LocalBuf(1024), in);
        chain=PV_ConformalMap(chain, MouseX.kr(-1.0,1.0), MouseY.kr(-1.0,1.0));
        Out.ar(0, Pan2.ar(IFFT(chain),0));
}).add;
)

a = Synth("conformer1", [\soundBufnum, c])
a.free

(
SynthDef("conformer2", {
        var in, chain, out;
        in = Mix.ar(LFSaw.ar(SinOsc.kr(Array.rand(3,0.1,0.5),0,10,[1,1.1,1.5,1.78,2.45,6.7]*220),0,0.3));
        chain = FFT(LocalBuf(2048), in);
        chain=PV_ConformalMap(chain, MouseX.kr(0.01,2.0, 'exponential'), MouseY.kr(0.01,10.0, 'exponential'));
        out=IFFT(chain);

        Out.ar(0, Pan2.ar(CombN.ar(out, 0.1, 0.1, 10, 0.5, out), 0, 0.3));
}).add;
)

a = Synth("conformer2")
a.free




(
b = Buffer.alloc(s,2048,1);
c = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");
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





b = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");


(
SynthDef("help-magFreeze", { arg out=0;
        var in, chain;
        in = SinOsc.ar(LFNoise1.kr(5.2,250,400));
        chain = FFT(LocalBuf(2048), in);
        // moves in and out of freeze
        chain = PV_MagFreeze(chain, SinOsc.kr(0.2) );
        Out.ar(out, 0.1 * IFFT(chain).dup);
}).play(s);

)

(
//trig with MouseY
SynthDef("help-magFreeze2", { arg out=0, soundBufnum=2;
        var in, chain;
        in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        chain = FFT(LocalBuf(2048), in);
        //chain = PV_MagFreeze(chain, MouseY.kr > 0.5 );
        chain = PV_MagFreeze(chain, SinOsc.kr(5)+0.5 );
        Out.ar(out, 0.1 * IFFT(chain).dup);
}).play(s,[\soundBufnum, b]);
)



b = Buffer.alloc(s,2048,1);

(
SynthDef("help-rectcomb", { arg out=0, bufnum=0;
        var in, chain;
        in = {WhiteNoise.ar(0.2)}.dup;
        chain = FFT(bufnum, in);
        chain = PV_RectComb(chain, 8, LFTri.kr(0.097, 0, 0.4, 0.5),
                LFTri.kr(0.24, 0, -0.5, 0.5));
        Out.ar(out, IFFT(chain).dup);
}).play(s,[\out, 0, \bufnum, b.bufnum]);
)

(
SynthDef("help-rectcomb2", { arg out=0, bufnum=0;
        var in, chain;
		var ou, env;
        in = WhiteNoise.ar(0.2);
		env = EnvGen.ar(Env.new([0.0,0.8,0.1],[0.19,0.01]),LFPulse.ar(2, 0, 0.4));
        chain = FFT(bufnum, in);
        chain = PV_RectComb(chain,  1, env, 0.1);
		ou = IFFT(chain);
		ou = LPF.ar(ou, 1700);
        Out.ar(out, ou.dup);
}).play(s,[\out, 0, \bufnum, b.bufnum]);
)




//series of numbers
Server.default.boot;
(
SynthDef(\beep, { |freq=440, cutoff=1000, pan=0|
	Out.ar(0, Pan2.ar(LPF.ar(Saw.ar(freq*Array.rand(2,0.99,1.01)).sum, cutoff*Array.rand(5,0.8,1.2)).sum, pan)) }).send(s);
)

//from 1 to 100
//while number>1
//if even: divide by 2, else multiply by 3 and add 1 
//remember highest value, remember longest series, both with their startnumber
//startnumber->cutoff, number-> frequency, pan mid
//startnumber->cutoff, highest value -> frequency, pan right
//startnumber->cutoff, longest series -> cutoff, pan left
(
var highest=1,  longest=1, starthighest=1, startlongest=1;
var numberbeep, highestbeep, longestbeep;
numberbeep=Synth(\beep, [\freq, 200, \cutoff, 200, \pan, 0]);
longestbeep=Synth(\beep, [\freq, 200, \cutoff, 200, \pan, -1]);
highestbeep=Synth(\beep, [\freq, 200, \cutoff, 200, \pan, 1]);
Routine({
	for (50, 100, { arg startnumber;
		var number, length=1, maximum;
		number=startnumber;
		maximum=startnumber;
		(number.asString+" ").post;
		numberbeep.set(\cutoff, 100*startnumber);
		while ( {number>1},{
			if (number.asInteger.even, {number=(number/2)}, {number=3*number+1});
			length=length+1;
			if (number>maximum, {maximum=number});
			numberbeep.set(\freq, 30+number);
			longestbeep.set(\freq, 75*length);
			highestbeep.set(\freq, 30+maximum);
			0.1.wait;
		});
		if(length>longest, {longest=length; startlongest=startnumber});
		if (maximum>highest, {highest=maximum; starthighest=startnumber});
		longestbeep.set(\cutoff, 100*longest);
		highestbeep.set(\cutoff, highest*2);
			("length: "+length.asString).post;
			(" maximum: " + maximum.asString).postln;
	});
	postf("longest series % at % \n", longest, startlongest);
	postf("highest value % at % \n", highest, starthighest);
	numberbeep.set(\freq, 30, \cutoff, 10000);
	longestbeep.set(\freq, 75*longest, \cutoff, 100*startlongest);
	highestbeep.set(\freq, 30+highest, \cutoff, 100*starthighest);
	10.wait;		
	numberbeep.free;
	highestbeep.free;
	longestbeep.free;
}).play;
)





(
b = Buffer.alloc(s,2048,1);
c = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");
d = Buffer.alloc(s,2048,1);
)

(
SynthDef(\help_pvconj, {  arg out=0, bufnum=0, soundBufnum=2;
        var in, chain;
        in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        chain = FFT(bufnum, in);
        chain = PV_Conj(chain);
        // Original is left, conj is right
        Out.ar(out, 0.3 * [in, IFFT(chain)]);
}).play(s,[\out, 0, \bufnum, b, \soundBufnum, c]);
)

(
SynthDef(\help_pvconj2, {  arg out=0, bufnum=0, soundBufnum=2;
        var in, chainA, chainB;
        in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
        chainA = FFT(bufnum, in);
        chainB = PV_Copy(chainA, d);
        chainB = PV_Conj(chainB);
        // Now we have the original and conjugate, what happens if we add them?
        Out.ar(out, 0.3 * (IFFT(PV_Add(chainA, chainB)).dup));
}).play(s,[\out, 0, \bufnum, b, \soundBufnum, c]);
)


s.boot
(
s.doWhenBooted {
c = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");
}
)

(
x = {
        var fftsize = 1024;
        var in, chain, in2, chain2, out;
        in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
        chain = FFT(LocalBuf(fftsize), in);

        // in2 = PlayBuf.ar(1, e, BufRateScale.kr(e), loop: 1);
        // JMcC babbling brook
        in2 = ({
                RHPF.ar(OnePole.ar(BrownNoise.ar, 0.99), LPF.ar(BrownNoise.ar, 14)
                        * 400 + 500, 0.03, 0.003)}!2)
                        + ({RHPF.ar(OnePole.ar(BrownNoise.ar, 0.99), LPF.ar(BrownNoise.ar, 20)
                        * 800 + 1000, 0.03, 0.005)}!2
                )
                        * 4;
        chain2 = FFT(LocalBuf(fftsize), in2);

        chain = chain.pvcalc2(chain2, fftsize, {|mags, phases, mags2, phases2|
                [mags * mags2 / 10, phases2 + phases]
        }, frombin: 0, tobin: 125, zeroothers: 0);

        out = IFFT(chain);
        Out.ar(0, 0.5 * out.dup);
}.play(s);
)
x.free;


(
Ndef(\bla, {
	var ou, bla;
	ou = Mix.fill(10, { arg i;
		i = i*100+50;
		SinOsc.ar(
			//RHPF.ar(OnePole.ar(SinOsc.ar(SinOsc.ar(140).range(10,500)), 0.99), LPF.ar(BrownNoise.ar, 44).exprange(50,500), 0.101, 0.3).range(1+i,2+i)
			SinOsc.ar(i)
		)
	}) ! 2;
	bla = LPF.ar(BrownNoise.ar, 74);
	ou = ou * 100;
	//ou = RLPF.ar(ou, bla.range(100,1500), LPF.ar(bla,15.99).range(0.001,2));
	//ou = RLPF.ar(ou, bla.range(170,500),0.1);
	//ou = LPF.ar(ou, 500);
	ou;
}).play
)
(
{
                OnePole.ar(BrownNoise.ar, 0.9919) ! 2
}.play
)


(
SynthDef(\fm1, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, fmod=0.25, modbias=0, modratio=0.9, rq=0.001, fdelta=0.0005, mix=1, modmix=0;
	var ou, ou2, ou3, modulator;
	var env;
	var mul;
	var env1, env2, env3;
	//env = EnvGen.ar(Env.new([0,1,0],[0.001,0.05],-9,1),gate,doneAction:2);

	env = EnvGen.ar(~make_adsr.(\adsr),gate, doneAction:2);
	env1 = EnvGen.ar(Env.new([1,1/10,1],[0.01,0.42],0,1),gate,levelBias:modbias,levelScale:freq*fmod);
	env2 = EnvGen.ar(Env.new([0.0,1,0],[0.001,0.4],0,1),gate,levelBias:0,levelScale:modratio);
	env3 = EnvGen.ar(Env.new([0,1,0],[0.0001,0.44]),gate,levelBias:0,levelScale:0.1);
	//env1 = freq*fmod+modbias;
	//env2 = modratio;
	//freq = SinOsc.ar(11)*(freq*env3)+freq;
	//amp = SinOsc.ar(11,0.5pi)*(amp*0.2)+amp;
	//delta = 0.0005;
	mul = [1];
	mul = [1+fdelta,1,1-fdelta,1+(fdelta/2)];
	modulator = [
		SinOsc.ar(env1),
		LFTri.ar(env1),
		LFSaw.ar(env1),
	];
	modulator = SelectX.ar(modmix, modulator);
	ou = (LFPulse.ar(modulator*(freq*env2)+freq*mul)/mul.size).sum;
	ou2 = (SinOsc.ar(modulator*(freq*env2)+freq*mul)/mul.size).sum;
	ou3 = (LFSaw.ar(modulator*(freq*env2)+freq*mul)/mul.size).sum;
	ou = SelectX.ar(mix*3, [ou,ou2, ou3]);
	//ou = RLPF.ar(ou, freq*[0.25,0.5,1,2], rq, [0.01,0.041,0.7,0.1]).sum;
	ou = RLPF.ar(ou, freq, rq, 1);
	//ou = (ou/4 ).distort;
	ou = ou /2;
	ou = ou * env;
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
},metadata:(
	specs:(
		modmix: \mix
	)
)).add;
)

(
Pdef(\plop, Pbind(
	\instrument, \test1,
	\degree, Pseq([0,2,4,2]+0,inf)+Pseg(Pseq([4,0,1,3],inf),2,\step),
	\degree, Pseq([0,\rest],inf),
	\degree, Pseq([0,Prand([\rest,2,7]),4,2]+0,inf)+Pseg(Pseq([4,0,3,1],inf),2,\step),
	\mtranspose, 1,
	\fdelta, 0.0001,
	\octave, 5,
	\fmod, Pseg(Pseq([1/4,1/2,1/8],inf),1.8),
	\fmod, 1.6514,
	\fmod, 2/1,
	\mix, Pseg(Pseq([0,1],inf),2),
	\modmix, Pseg(Pseq([0,1],inf),0.71),
	//\modmix, 0.5,
	\modbias, 000,
	\modratio, 00.990,
	\rq, 0.57,
	\legato, 0.5,
	\dur, 2/8,
	\amp, 0.05
)).play;
)

s.levels
s.meter
s.boot

~seq.focus_window
~seq.panels.side.window.view.focus(true)
(
Mdef(\bla, Pbind(
	\instrument, \fm1
))
)

~seq.model.velocity_ratio = 1.0
