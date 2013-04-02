(
Instr(\noiseSrc, { arg amp;
                var sig;
                sig = WhiteNoise.ar(0.3)*amp;
});


Instr(\SinOscModule, { arg speed, low = 0.1, high = 0.9 ;
   SinOsc.kr(speed).range(low, high);
});

Instr(\sin_noiseSrc, { arg speed=1, low=0, high=1;
	var amp;
	amp = Instr(\SinOscModule).value((speed:speed, low:low, high:high));
	Instr(\noiseSrc).value((amp: amp))
});

)

Patch(\sin_noiseSrc).play
