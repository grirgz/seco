(
a = {
        var localin, saw, scalar, pitchshift, localout;

        localin =       LocalIn.ar(
                                localin:        2
                                );

        saw =   LFSaw.ar(
                                freq:   localin,
                                iphase: 0,
                                mul:    localin,
                                add:    1
                                );

        scalar =        saw.range(
                                low:    0.25,
                                hi:     4.0
                                );

        pitchshift =    PitchShift.ar(
                                in:             SinOscFB.ar(
                                                        freq:           scalar**[ 2, 2.1 ],
                                                        feedback:       saw
                                                        ),
                                windowSize:     [0.05 , 0.03 ],
                                pitchRatio:     scalar
                                );

        localout =      LocalOut.ar(
                                channelsArray:  pitchshift
                                );

        pitchshift;

}.play;
)
a.stop

{l=LocalIn.ar(2);k=LFSaw.ar(l,0,l,1);j=k.range(0.25,4.0);s=PitchShift.ar(SinOscFB.ar(j**[l,k],k),[0.05,0.03],j);LocalOut.ar(s);s}.play;

s
