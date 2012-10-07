////////#superCollider///////////

///// audio @ http://archive.org/details/ChiptuneBreakz

///

(

Tdef(\cheaptune,{

     var sig2,sig1,sig3,a,rel;	

	sig1=[8,12,16,20];

	sig2=2/sig1.scramble;

	sig3=2*sig1.scramble;

	rel=Pxrand([0,0,0,0,0,0,0,1],inf).asStream;

	inf.do{

	        a=[0.125,0.25,0.5,0.5,0.25,0.125,1].choose;

		x=[60,67,75,79,94].scramble;

		play{EnvGen.ar(Env.perc(0.01,a+rel.next),doneAction:2)*

				AY.ar(x.midicps*a*sig1.choose,x*sig2.choose.midicps,a*sig3.choose.midicps,0.25,3,15,10,7)};

                play{EnvGen.ar(Env.perc(0.01,a/2),Duty.kr(Dseq([0.5,0.25],inf),0,Dxrand([0,1],inf)),doneAction:2)*

				BrownNoise.ar(0.6)!2};

                play{EnvGen.ar(Env.perc(0.01,a/4),Duty.kr(Dseq([0.25,0.5],inf),0,Dseq([0,1],inf)),doneAction:2)*

				LFNoise0.kr(16)*WhiteNoise.ar(0.2)!2};

		a.wait

	}

});

Tdef(\cheaptune).play;

)

s.record;

s.stopRecording;

//



(

Server.local.waitForBoot({

(

z.free;

z = Buffer.alloc(s, 512, 1);

z.sine1(1.0 / [1, 2, 3, 4], true, true, true);



 fork( { loop {

 SynthDef("g1",{ arg out=0,bufnum=0,dur=1,rate=1,pos=0,sdens=1,edens=1;

     var dens = Line.kr(sdens,edens,dur);

     var trig = [LFNoise0,SinOsc,Impulse,LFPulse,LFSaw].choose.ar(Line.kr(sdens,edens,dur));

     //var env = EnvGen.kr(Env.perc(0.0001.rrand(0.1),dur*0.5.rrand(2)),doneAction:2);

     var env = EnvGen.kr(Env.perc(0.0001.rrand(0.1),dur*(0.5.rrand(4))),doneAction:2);

     Out.ar(out,

         GrainBuf.ar(2,trig,1/dens,bufnum,rate,pos)*env;

         )

     }).add();

  [1,2,4,8,16,32].choose.postln.wait;

 }

 }

 );





a = Pbind(\instrument,\g1,\dur,Pseq([Pseq([0.25],32),Pseq([0.25,0.125,0.125],32),Pseq([0.125],32)],inf),\sdens,Pseq([9000,1000,500]/10,inf),\edens,Prand([Pseq([9000,1000,500]/10,4),Pseq([1],1)],inf),\rate,Pfunc({-10.0.rrand(10)}),\pos,Pfunc({1.0.rand}),\bufnum,z.bufnum);

b = Pbind(\instrument,\g1,\dur,Pseq([4],inf),\sdens,Pseq([9000,1000,500]/100,inf),\edens,Prand([Pseq([9000,1000,500]/10,1),Pseq([1],3)],inf),\rate,Pfunc({-10.0.rrand(10)}),\pos,Pfunc({-10.0.rrand(10)}),\bufnum,z.bufnum);

c = Pbind(\instrument,\g1,\dur,Pseq([4/3],inf),\sdens,Pseq([9000,1000,500,25],inf),\edens,Prand([Pseq([9000,1000,500,25],1),Pseq([1],4)],inf),\rate,Pfunc({-100.0.rrand(100)}),\pos,Pfunc({-10.0.rrand(10)}),\bufnum,z.bufnum);





a =a.play;

b =b.play;

c =c.play;



)

});

)




// NX102

// 2012-08-07_23_05_45



// La ment no té límits.



// Use with Jitlib:



p = ProxySpace.push(s.boot);



// Read one sample on your collection:



b = Buffer.read(s,Platform.systemAppSupportDir +/+"/sounds/a11wlk01.wav",bufnum:1);


// Play and fun:



~nx102.play;


(

~nx102 = {

	Pan2.ar(LeakDC.ar(Normalizer.ar(

				BPeakEQ.ar(BPeakEQ.ar(BPeakEQ.ar(BPeakEQ.ar(FreeVerb.ar(

					RHPF.ar(RLPF.ar(

					Pulse.ar(

						(PlayBuf.kr(1,1,Rand(50,100),1,0,1)*Rand(100,1000)).clip(

							Rand(5,12),Rand(12,20))) 

						,Rand(100,5000),Rand(0.005,0.1))

						,Rand(5000,20000),Rand(0.005,0.1))

					,0.0,0.0,0.0)

		 ,100,6.0,6),1000,6.0,0),5000,6.0,0),10000,6.0,6)

		 ,(1.25/0.999)*1.0,0.1)),0)

};

)


~nx102 = nil;

b.free;



// this was going to be drum and bass but I got carried away



(

fork{

	// this is going to be a convoluted step seq

	var i = 0;

	

	loop{ 

		var tempo, step;

		tempo = [

			(3,5..21).reciprocal.choose,

			1/11,

			1/7,

			1/5,

			1

		].wchoose([0.1,17,0.4,0.3,0.25].normalizeSum);

		

		i = i + 1;

		step = i % 16;

				

		play{

			// not sure I used all of these.

			var scale, bdm, hh, sd, bd, bda, sda, hha, bdseq, sdseq, hhseq, reva, revb, rev,

				mix;

						

			scale = Scale.harmonicMinor.degrees+26;

			

			// bd sequence

			bdseq =[ {2.rand}.dup(16), 

				[1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0]

			].choose;

			

			// bd amp env

			bda = EnvGen.kr(Env.perc(0.01,tempo*6.0.rand,bdseq[step]), doneAction: 2);

			

			// bass freq modulation

			bdm = {LFNoise2.kr(tempo.rand,0.25,1)}.dup;

			

			// bd

			bd = LPF.ar(Pulse.ar(scale.choose.midicps,0.5).distort*bdm,1e4.rand).sum * bda; 

			bd = bd.tanh;

			5.do{bd = CombC.ar(bd, 0.5, 0.1.rand, 5.0.rand)+bd};

			5.do{bd = AllpassC.ar(bd, 0.5, 0.1.rand, 5.0.rand)+bd};

			

			// sd seq

			sdseq = [ [0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0], 

				{2.rand}.dup(16) 

			].choose;

			

			// sd amp env

			sda = EnvGen.kr(Env.perc(0.01,0.1,sdseq[step]));



			// sd

			sd = PinkNoise.ar(2).distort * sda.lag(0.1.rand);

			5.do{sd = AllpassC.ar(sd, 0.5, [0.25.rand,0.05.rand].choose, 1.5.rand)+sd};

					

			// hh seq

			hhseq = [{2.rand}.dup(16),

				[0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1]

			].choose;

			

			// hh amp env

			hha = EnvGen.kr(Env.perc(0.01,0.01,hhseq[step]));



			// hh

			hh = HPF.ar(WhiteNoise.ar(0.7),8000) * hha;

	

			// drums together

			mix = bd + sd + hh;

						

			// rev

			reva = GVerb.ar(mix,1.0.rand.max(0.1),1.0.rand).sum;

			revb = GVerb.ar(mix,1.0.rand.max(1),3.0.rand).sum;

			rev = XFade2.ar(reva,revb,LFNoise0.kr(tempo));

			

			// pannnn

			mix = PanAz.ar(5,rev,LFNoise0.kr(tempo*2));

			mix = Limiter.ar(mix);

			

			// raussssss

			Out.ar(0,mix.clip);

		};

	tempo.wait;

	};	

}

)










(

SynthDef(\ChicagoPad2, { |out = 0, freq = 440, freq2=660, freq3=528, cutoff = 500, amp = 0.2, gate=1|



	var snd;



	freq = freq + SinOsc.kr(freq*1/2, 0, freq/2, 0);



	snd = Saw.ar([freq, freq+1, freq-1, freq2, freq3] * [0.99,1])*0.1;



	snd = snd + VarSaw.ar(0.99*[freq, freq+1, freq-1, freq2, freq3, freq2+1, freq3+1],0, LFTri.kr(7.13).range(0.00,0.1))*0.1;





	snd = Mix(snd);

	snd = FreeVerb.ar(snd, 0.51,10,0.1)*2;

	snd = snd *8;

	snd = RLPF.ar(snd, SinOsc.kr([0.1,4.2], 0, [1700,480], [4000,700,5000])/[20.51,20], SinOsc.ar(0.1,1.5*pi)+1.05)/3;

	snd = Mix(snd);



	snd = MoogFF.ar(snd, SinOsc.kr(0.08, 0.5, cutoff/10, cutoff), 3, 0);



	snd = snd * EnvGen.ar(Env.adsr(0.4, 0.01,0.8,0.4), gate, doneAction:2);



	snd = [snd , AllpassC.ar(snd, 0.45, 0.05+SinOsc.ar(0.11).range(0,0.045), 0.3)];

	snd = snd * amp;

	Out.ar(out, snd);



}).add;

SynthDef(\ricky1, { |out = 0, freq = 440, freq2=660, freq3=528, cutoff = 500, amp = 0.2, gate=1|



	var snd;



	freq = freq + SinOsc.kr(freq*1/2, 0, freq/2, 0);



	snd = Saw.ar([freq, freq+1, freq-1, freq2, freq3] * [0.99,1])*0.1;



	snd = snd + VarSaw.ar(0.99*[freq, freq+1, freq-1, freq2, freq3, freq2+1, freq3+1],0, LFTri.kr(7.13).range(0.00,0.1))*0.1;





	snd = Mix(snd);

	//snd = FreeVerb.ar(snd, 0.51,10,0.1)*2;

	//snd = snd *8;

	//snd = RLPF.ar(snd, SinOsc.kr([0.1,4.2], 0, [1700,480], [4000,700,5000])/[20.51,20], SinOsc.ar(0.1,1.5*pi)+1.05)/3;

	snd = Mix(snd);



	snd = MoogFF.ar(snd, SinOsc.kr(0.08, 0.5, cutoff/10, cutoff), 3, 0);



	snd = snd * EnvGen.ar(Env.adsr(0.4, 0.01,0.8,0.4), gate, doneAction:2);



	snd = [snd , AllpassC.ar(snd, 0.45, 0.05+SinOsc.ar(0.11).range(0,0.045), 0.3)];

	snd = snd * amp;

	Out.ar(out, snd);



}).add;
)



(

Pdef(\plop2, Pbind(

	\instrument, \ChicagoPad2,

	\degree, Pseq([0,8,2],inf),

	\root, -4,

	\freq2, Pkey(\freq) / 2 * Pseq([3/2, 2/7],inf),

	\freq3, Pkey(\freq) / 2 * Pseq([5/6, 4/5, 5/6],inf),

	\legato, 1,

	\cutoff, Pseg(Pseq([10000,0100],inf),10),

	\dur, 4,

	\amp, 0.2

)).play;

);

(
Pdef(\plop3, Pbind(

	\instrument, \ricky1,

	\degree, Pstep(Pseq([0,8,2],inf),4) + Pseq([0,2,4,\r,7,6,5,4],inf),

	\root, -4,

	\octave, 7,

	\freq2, Pkey(\freq) + 4,

	\freq3, Pkey(\freq) * 2 - 4,

	\legato, 1,

	\cutoff, Pseg(Pseq([10000,0100],inf),10),

	\dur, 1/4,

	\amp, 0.2

)).play;
)

(
Pdef(\group, Ppar([
	Pdef(\plop2),
	Pdef(\plop3)
])).play
)
