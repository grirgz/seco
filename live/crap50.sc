(
{
        var mod = Line.kr(0,1,10);
        var saw = LFSaw.ar(150);
        var sig = saw - ((saw>0)*mod)+(mod*0.5);
		sig ! 2
//}.plot(1)
}.play
)
