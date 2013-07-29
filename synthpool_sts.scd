
(
SynthDef(\prophet5pwmstrings,{|out= 0 freq = 440 amp = 0.1 gate=1 lforate = 10 lfowidth= 0.5 cutoff= 12000 rq=0.5 pan = 0.0|  
	 
	var lfo, pulse, filter, env;  
		 
	lfo = LFTri.kr(lforate*[1,1.01],Rand(0,2.0)!2);  
		 
	pulse = Pulse.ar(freq*[1,1.01],lfo*lfowidth+0.5);  
		 
	filter = RLPF.ar(pulse,cutoff,rq); 	 
		 
	env = EnvGen.ar(Env.adsr(0.01,0.0,1.0,0.5),gate,doneAction:2);  
		 
	Out.ar(out,Pan2.ar(Mix(filter)*env*amp*0.5,pan));  
		 
}).store; 

SynthDef(\winwoodlead,{|out= 0 freq = 440 amp = 0.1 gate=1 cutoff=8000 rq=0.8 lfowidth=0.01 lforate= 8 lagamount=0.01 pan=0.0|  
	 
	var pulse, filter, env, lfo;  
		 
	lfo = LFTri.kr(lforate,Rand(0,2.0)!2);  
		 
	pulse = Mix(Pulse.ar((freq.lag(lagamount))*[1,1.001]*(1.0+(lfowidth*lfo)),[0.2,0.19]))*0.5;
	  
	filter =  RLPF.ar(pulse,cutoff,rq);

	//remove low end
	filter = BLowShelf.ar(filter,351,1.0,-9);
 
	env = EnvGen.ar(Env.adsr(0.01,0.0,0.9,0.05),gate,doneAction:2);  
		 
	Out.ar(out,Pan2.ar(filter*env*amp,pan));  
		 
}).store;  

Instr(\singleoscwobble,{|freq = 440 amp = 0.1 gate=1 lforate = 10 lfowidth= 0.5 cutoff= 12000 rq=0.5 pan=0.0|  
	 
	var lfo, pulse, filter, env;  
	var ou;
		 
	lfo = LFTri.kr(lforate,Rand(0,2.0)!2);  
		 
	pulse = Pulse.ar(freq*(1.0+(lfowidth*lfo)),0.5);  
		 
	filter = RLPF.ar(pulse,cutoff,rq); 	 
		 
	env = EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.0,1.0,0.5)),gate,doneAction:2);  
		 
	ou = Pan2.ar(filter*env*amp*0.5,pan);
		 
}).storeSynthDef;  
 

Instr(\singleoscwobble_chorus, { arg gate=1;
	var ou;
	ou = Instr(\singleoscwobble).wrap((gate:gate));
	ou = Instr(\choruseffect).wrap((in:ou, gate:gate));

}).storeSynthDef;

SynthDef(\delayeffect, {|out =0, in, gate= 1, wet=1| 
	var source = in;
	var delay;  
	var env = Linen.kr(gate, 0.1, 1, 0.1, 2); 
	var ou;
	 
	delay= CombC.ar(source,0.25,0.25,2.0);  
	ou = SelectX.ar(wet*env, [source,delay]);
		 
	Out.ar(out, ou);  
		 
},[\kr,\ar]).store; 

)
(

Instr(\choruseffect, {|in gate=1 wet=1| 
	//var source = In.ar(in,2);  
	var source = in;
	var chorus;  
	//var env = Linen.kr(gate, 0.1, 1, 0.1, 2); 
	var ou;
	 
	chorus= Mix.fill(7, { 
		
		var maxdelaytime= rrand(0.005,0.02); 
		
		DelayC.ar(source, maxdelaytime,LFNoise1.kr(Rand(4.5,10.5),0.25*maxdelaytime,0.75*maxdelaytime) ) 
			 
	}); 
	ou = SelectX.ar(wet, [source,chorus]);

		 
		 
},[\ar]);
)



(
var instr_name = \choruseffect;
SynthDef(instr_name, { arg out = 0;
	var ou;
	ou = SynthDef.wrap(Instr(instr_name).func, [\ar]);
	ou = Out.ar(out, ou);
}).store;
)
