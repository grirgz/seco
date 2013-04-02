
( 
SynthDef(\trianglewavebells,{|out= 0 freq = 440 amp = 0.1 gate=1 lforate = 10 lfowidth= 0.0 cutoff= 100 rq=0.5 pan=0.0
			wet=0.8|  
	 
	var osc1, osc2, vibrato, filter, env;  
	var ou;
		 
	vibrato = SinOsc.ar(lforate,Rand(0,2.0));  
		 
	osc1 = Saw.ar(vibrato*0.7*freq+freq); 
		 
	//Saw a bit rough, possibly slighter smoother: 
	//osc1 = DPW4Saw.ar(freq*(1.0+(lfowidth*vibrato)),0.5); 
		 
	//osc2 = Mix(LFTri.ar((freq.cpsmidi+[11.9,12.1]).midicps));  
		 
	//filter = (osc1+(osc2*0.5))*0.5; //no filter version 
	filter = RHPF.ar(osc1,cutoff,rq); 	 
	ou = filter;
	//ou = CombC.ar(ou, 0.4, 0.4, 1.8);
	//ou = CombC.ar(ou, 0.4, 0.04, 1.8);
	ou = FreeVerb.ar(ou, wet, 0.9, 0.1);


	
		 
	env = EnvGen.ar(Env.adsr(0.01,0.1,1.0,0.5),gate,doneAction:2);  
		 
	Out.ar(out,Pan2.ar(ou*env*amp,pan));  
		 
}).add;  
) 
 
 
( 
Pdef(\plop, Pbind( 
	\instrument, \trianglewavebells, 
	\sustain,0.1, 
	\amp, 0.2, 
	//\midinote,Prand([0,7,3,0, 8,7,8,5, 7,12,5,3, 12,7,15,-5]+40,inf) + Prand([10,1,5,-10,20],inf), 
	\note,Prand([0,2b,4],inf), 
	\root, Pstep([0,1],2,inf),
	//\wet, Pseg(Pseq([0,0.5],inf),3) + Pfunc{ 0.5.rand },
	\lfowidth, 0.010, 
	\lforate, 1, 
	\rq, 0.1, 
	\cutoff,Pn(Pseries(60,10,9),inf),
	\dur, Pfunc { 1.rand  } + Prand([0.1,0.2,0.14]/2,inf),
)).play 
) 

(
Pdef(\maj, Ptpar([
	0, Pset(\amp, Pseg(Pseq([0,0.5,0],1),Pseq([10,10]),Pseq([1,-1])), Pdef(\plop)),
	0, Pbind(
])
).play
)
 


		Pdef(\kick, Pbind(
			\instrument, \stereosampler,
			\bufnum, Mdef.dsample(\kick1),
			\dur, 0.125,
			\amp, 1.0
		);
