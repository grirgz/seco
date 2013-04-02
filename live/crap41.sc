(
//drumsynth - kick is default  slavefreq is a ratio of freq
SynthDef(\drumsynth, { arg freq = 440, slavefreq = 0.5, mr = 0.2,sr = 0.2, ml = 1, sl = 1, 
	namt =1,nr = 0.2, nctf = 500, nq = 0.2, bndamt = 1,bnda = 0, bndr = 0.2,clicklevel = 1; 
	var sig;
	sig = RLPF.ar(WhiteNoise.ar(Line.ar(clicklevel, 0, 0.02)),3500,0.2)
	+
	RLPF.ar(WhiteNoise.ar(1),nctf * EnvGen.kr(Env.perc(0,nr,namt,-4), doneAction: 0),nq)
	+       //master
	EnvGen.kr(Env.perc(0,mr,ml,-4), doneAction: 2)
	* //bend env below
	SinOsc.ar( freq * EnvGen.kr(Env.perc(bnda,bndr,bndamt,-4), doneAction: 0),1)
	+
	//slave
	EnvGen.kr(Env.perc(0,sr,sl,-4), doneAction: 0)
	*
	SinOsc.ar(freq * slavefreq * EnvGen.kr(Env.perc(bnda,bndr,bndamt,-4), doneAction: 0)   ,1 );

	Out.ar(0, sig)
},
	variants: (snare: [ 
		freq: 1940,
		slavefreq:  0.5, mr:  0.2,sr:  0.2,ml:  0.1, sl:  0,namt: 0.2,nr:  0.3,
		nctf:  23500, nq: 1, bndamt:  0,bnda:  0, bndr:  0.2,clicklevel: 0 
	])
).add

)


x = Synth.new(\drumsynth);
x = Synth.new('drumsynth.snare');


