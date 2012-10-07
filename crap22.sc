//loud

(
~beautybuf = Buffer.alloc(s, s.sampleRate * 1, 1);

SynthDef(\mm_beauty,
{
        arg delays = #[0.4, 0.7, 0.8, 0.9], pans = #[-1, -0.7, 0.6, 1],
noiseamp = 0.03, hpf = #[20, 60], average = #[8000, 14000], srate = 44100,
smooths = #[300, 400], lpf = #[13000, 15000];
        var phase, feedback, sig, local;
        local = LocalIn.ar(2);
        sig = WhiteNoise.ar(noiseamp);
        phase = DelTapWr.ar(~beautybuf, sig + local);
        feedback = DelTapRd.ar(~beautybuf, phase, delays);
        feedback = Pan2.ar(feedback[0], pans[0]) + Pan2.ar(feedback[1],
pans[1]) + Pan2.ar(feedback[2], pans[2]) + Pan2.ar(feedback[3], pans[3]);
        feedback = HPF.ar(feedback, hpf);
        feedback = feedback * (0.02 /
(Lag.ar(AverageOutput.ar(abs(feedback),
Impulse.kr((average/srate).reciprocal)), smooths/srate).clip(0.0001, 1)));
        feedback = LPF.ar(feedback, lpf);
        LocalOut.ar(feedback);
        Out.ar(0, (feedback));
}
).store;

)

//run one by one:

a = Synth(\mm_beauty); // sounds weird, why?
a.set(\delays, [0.2, 0.3, 0.4, 0.5] * 0.35); //THERE we go
a.set(\delays, [0.2, 0.3, 0.4, 0.5] * 0.15); //THERE we go
a.set(\delays, [0.2, 0.3, 0.4, 0.5] * 1.05); //THERE we go
a.set(\lpf, [4300, 4000]);

a.set(\lpf, [300, 400]);
a.set(\hpf, [300, 900]);
a.set(\hpf, [3000, 1900]);
a.free;

