(
play {
    var depthMod = Saw.kr(0.05).exprange(0.05, 0.5);
    var sig = HPF.ar(
        ({ |k|
            Pan2.ar(({ |i|
                SinOsc.ar(
                    i * k / (k + 1),
                    BPF.ar(
                        LFTri.ar(2 ** (i + k) / 256)
                        / Decay.ar(
                            Impulse.ar(0.5 ** i / (k + 1) * 0.15),
                            [k * i + 1, k * i + 1 * 2],
                            3 ** k
                        )
                        % (2 * pi)
                        * Saw.ar(i/96).range(0, LFTri.kr(0.05 + i + k).exprange(0.05, 0.5)),
                        (i + k + 13).nthPrime * depthMod * 0.0625,
                        LFTri.kr((i + k + 1).reciprocal * 0.25).exprange(0.005, 2.0)
                    )
                ).sum} ! 4).product
                * (k+1).reciprocal
                * LFSaw.kr(
                    (k + 5).nthPrime.reciprocal, k/7 * 2
                ).exprange(-24.dbamp, 1),
                LFTri.ar((k + 1).nthPrime * 10)
            )
        } ! 16).sum * -12.dbamp,
        40
    );
    var siggap = Amplitude.ar(sig).reciprocal.min(0.0625);
    var verb = GVerb.ar(
        sig.sum,
        roomsize: [80, 135, 283],
        revtime: depthMod * 2,
        drylevel: 0,
        taillevel: 0.dbamp * siggap,
        earlyreflevel: -6.dbamp * siggap,
    ).sum;
    verb + sig
}
)


(
Ndef(\plop, { arg freq=200, pan=0, amp=0.1;
	var ou;
	var ffreq;
	freq = SinOsc.kr((1..30)*0.1).range(10,500);
	ffreq = SinOsc.kr((1..5)*1.01).range(10,1500);
	ou = LFSaw.ar(freq);
	ou = ou.tanh;
	ou = RLPF.ar(ou, ffreq);
	ou = Splay.ar(ou, 1, amp, pan);
}).play;
);

(
Ndef(\plop, { arg freq=200, pan=0, amp=0.1;
	var ou;
	var ffreq;
	var size = 40;
	var width = SinOsc.kr(0.1).range(0.01,0.7) * SinOsc.kr(0.3).range(0.01,0.7) ;
	var phase = SinOsc.kr(0.21).range(1,5);
	ou = size.collect { arg x;
		var lsig;
		var lsig2;
		x = x*2+20;
		lsig = 5.collect { arg y;
			var phase = SinOsc.kr(0.21+y/10).range(1,5);
			var width = SinOsc.kr(0.1).range(0.01+y/10,0.7);
			y = y*2 + 1;
			LFSaw.ar(x.midicps-y) * LFPulse.kr(1,x/y/phase%1, width).range(0,1);
		};
		//lsig2 = LFSaw.ar(x.midicps+3) * LFPulse.kr(1,x/phase%1, width).range(0,1);
		//lsig = lsig.tanh;
		lsig2 = RLPF.ar(lsig, x.midicps * 2);
		lsig = RLPF.ar(lsig, x.midicps / 2, 0.1);
		lsig2 + lsig;
	};
	ou = Splay.ar(ou, 1, amp, pan);
}).play;
);
