//--redFrik 2013
//req. Canvas3D quark
//use mouse to rotate canvas
//add your own sounds to x, y, z (maybe with InFeedback.ar)
(
//--settings
var width= 640, height= 480;
var fps= 60;
var buffersize= 1024;
var scale= 250;
var perspective= 0.3;
var distance= 2;

//--
var win= Window("3d soundwave canvas", Rect(128, 64, width, height), false).front;
var can= Canvas3D(win, Rect(0, 0, width, height)).scale_(scale).perspective_(perspective).distance_(distance);
var itemCube= Canvas3DItem.cube;
var itemWave= Canvas3DItem.new;
var path= [];
var buffer;
can.add(itemCube);
can.add(itemWave);
s.waitForBoot{
	buffer= Buffer.alloc(s, buffersize, 3);
	s.sync;
	{
		var x= LFCub.ar(42);//sound x
		var y= LFCub.ar(83);//sound y
		var z= SinOsc.ar(LFSaw.kr(0.01).range(50,70));//sound z
		RecordBuf.ar([x, y, z], buffer);
		Silent.ar;
	}.play;
	s.sync;
	can.animate(fps, {|frame|
		buffer.getn(0, buffersize-1*3, {|data| path= data.clump(3)});
		itemWave.paths= [path];
	});
	can.mouseMoveAction= {|v, x, y|
		can.transforms= [
			Canvas3D.mRotateY(x/scale.neg%2pi),
			Canvas3D.mRotateX(y/scale%2pi)
		];
	};
	CmdPeriod.doOnce({win.close; buffer.free});
};
)




(
var duration = 60 * 5;
var freqDrift = { |freq| LFDNoise3.ar(0.2, 0.1, freq.cpsmidi).midicps };
var masterVolume = -2.dbamp;

play {
	var rootFreq = freqDrift.(26.midicps);
	var gate = EnvGen.ar(Env([0, 1, 1, 0], [0.1, duration, 0.25]), gate: 1, doneAction: 0);
	var leveler = LinLin.ar(LFTri.ar(duration.reciprocal, 1), -1, 1, -24, 0).dbamp;
	var sig = HPF.ar(
		({ |k|
			Pan2.ar(({ |i|
				Pulse.ar(
					i * k + 2 / (k + 1) * rootFreq * (i + 1) * 0.0625 * 16,
					LinLin.ar(LFTri.ar(rootFreq * (i + 1) / 1024), -1, 1, 0.5, 0.95)
				)} ! (k/2 + 1).asInteger).product
				* (k + 1).reciprocal
				* LinExp.ar(Blip.ar(
					(k + 5).nthPrime.reciprocal * rootFreq * 0.125, 3), -1, 1, -48.dbamp, 1).neg,
				LFTri.ar((k + 1).nthPrime * rootFreq / 256)
			)
		} ! 16).sum * -12.dbamp,
		40
	);
	var compsig = Compander.ar(
		sig, sig, -48.dbamp, 1, XLine.ar(1, 0.125, duration), mul: Line.ar(0, 36, duration).dbamp
	);
	var final = LeakDC.ar(SinOsc.ar(
		rootFreq * 2,
		GVerb.ar(
			compsig.sum,
			revtime: 2.5,
			drylevel: 0,
			earlyreflevel: 0,
			taillevel: -12.dbamp,
			mul: LinExp.ar(LFTri.ar(duration.reciprocal, 1), -1, 1, pi, pi * 0.25),
			add: compsig
		),
		mul: 0.5 * gate,
	), mul:  leveler);
	var verb = GVerb.ar(final.sum, 300, 4.5, drylevel: 0, earlyreflevel: -6.dbamp, taillevel: -12.dbamp, add: final);
	var env = DetectSilence.ar(gate + verb, doneAction: 2);
	verb * masterVolume
};

play {
	var rootFreq = freqDrift.(Lag.ar(26.midicps * 4 * LinLin.ar(LFTri.ar(duration.reciprocal * 5), -1, 1, 3, 13).floor / LinLin.ar(LFTri.ar(duration.reciprocal * 3), -1, 1, 13, 3).floor, 0.25));
	var gate = EnvGen.ar(Env([0, 1, 1, 0], [0.1, duration * 0.9, 0.25]), gate: 1, doneAction: 0);
	var leveler = LinLin.ar(LFTri.ar(duration.reciprocal * 15, 2), -1, 1, -72, 6).dbamp;
	var sig = HPF.ar(
		({ |k|
			Pan2.ar(({ |i|
				Pulse.ar(
					i * k + 2 / (k + 1) * rootFreq * (i + 1) * 0.0625 * 16,
					LinLin.ar(LFTri.ar(rootFreq * (i + 1) / 1024), -1, 1, 0.5, 0.95)
				)} ! (k/2 + 1).asInteger).product
				* LinExp.ar(Blip.ar(
					(k + 5).nthPrime.reciprocal * rootFreq * 0.125, 3), -1, 1, -48.dbamp, 1).neg,
				LFTri.ar((k + 1).nthPrime * rootFreq / 256)
			)
		} ! 16).sum * -12.dbamp,
		40
	);
	var compsig = Compander.ar(
		sig, sig, -48.dbamp, 1, XLine.ar(1, 0.125, duration), mul: Line.ar(0, 36, duration).dbamp
	);
	var final = LeakDC.ar(SinOsc.ar(
		rootFreq * 4,
		GVerb.ar(
			compsig.sum,
			revtime: 2.5,
			drylevel: 0,
			earlyreflevel: 0,
			taillevel: -12.dbamp,
			mul: Line.ar(pi * 2, pi, duration),
			add: compsig
		),
		mul: 0.5 * gate,
	), mul: leveler * AmpCompA.kr(root: rootFreq * 4));
	var verb = GVerb.ar(final.sum, 300, 4.5, drylevel: 0, earlyreflevel: 0.dbamp, taillevel: -24.dbamp, add: final);
	var env = DetectSilence.ar(gate + verb, doneAction: 2);
	verb * -18.dbamp * masterVolume
};

play {
	var rootFreq = 26.midicps;
	var depthMod = LFSaw.kr(0.05).exprange(0.05, 5.0);
	var gate = EnvGen.ar(Env([0, 1, 1, 0], [0.1, duration * 0.9, 0.25]), gate: 1, doneAction: 0);
	var leveler = LinLin.ar(LFTri.ar(duration.reciprocal, 3), -1, 1, -24, 6).dbamp;
	var sig = HPF.ar(
		({ |k|
			Pan2.ar(({ |i|
				SinOsc.ar(i * k + 2 / (k + 1) * rootFreq * (i + 1))} ! 4).product
				* (k+1).reciprocal
				* LFSaw.kr(
					(k + 5).nthPrime.reciprocal * rootFreq * 0.25, k/7 * 2
				).exprange(-24.dbamp, 1),
				LFTri.ar((k + 1).nthPrime * rootFreq)
			)
		} ! 16).sum * -18.dbamp,
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
		add: sig
	).sum * leveler * gate;
	var env = DetectSilence.ar(verb, doneAction: 2);
	verb * masterVolume
}
)


(
~infiniteHoldReverb = {

	arg feedback=1, gate=1;
	var sig, local;

	//establish input signal
	sig = SoundIn.ar([0,1]);
	sig = sig * EnvGen.kr(Env.asr(0.01,1,0.01),gate);
	local = LocalIn.ar(2)+sig;

	//apply reverb - x.do is no. of taps, other args are input, maxdly, dly, decay
	15.do{local = AllpassN.ar(local, 1, Rand(0.001,0.3), 3)};

	//complete feedback loop -- send audio back to LocalIn
	LocalOut.ar(local*feedback); // multiply by 0 to 1, 0 in no feedback, 1 = 100% feedback

	//non-feedback output to speakers
	//Out.ar(0, sig+local); //with direct input
	Out.ar(0, local);    //without direct input

}.play
)

~infiniteHoldReverb.set(\feedback, 0.25) //can change value here, WHILE IT'S PLAYING to change feedback amount! 
~infiniteHoldReverb.set(\gate, 1) //can change value here, WHILE IT'S PLAYING to change feedback amount! 


(
Ndef(\inffb, {

	arg feedback=1, ingate=1;
	var sig, local;

	//establish input signal
	sig = SoundIn.ar([0,1]);
	sig = sig * EnvGen.kr(Env.asr(0.01,1,0.01),ingate);
	local = sig + Fb { arg fb;
		var fbsig = fb * feedback + sig;

		//apply reverb - x.do is no. of taps, other args are input, maxdly, dly, decay
		15.do{fbsig = AllpassN.ar(fbsig, 1, LinRand(0.001,0.1,-1), 3)};
		fbsig = BPF.ar(fbsig, 800,0.1);

		fbsig;
	};

	Out.ar(0, local);    //without direct input

}).play
)
Ndef(\inffb).set(\feedback, 0.9);

~infiniteHoldReverb.set(\feedback, 0.25) //can change value here, WHILE IT'S PLAYING to change feedback amount! 
~infiniteHoldReverb.set(\feedback, 15) //can change value here, WHILE IT'S PLAYING to change feedback amount! 
~infiniteHoldReverb.set(\gate, 1) //can change value here, WHILE IT'S PLAYING to change feedback amount! 


200000.xrand
200000.rand
LinRand


(
	{
		SinOsc.ar(100) - SinOsc.ar(100.01);
	}.play
)
