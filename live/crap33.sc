// Granular display by T.Maisey.

// Position in the sound file is (obviously) on the x-axis, amplitude on the y-axis.

// The grain's transparency shows its position in its envelope (window).

// The speed of the grain shows pitch.

// ----------------------------------------------



// Requires use of Qt gui kit.:

GUI.qt;



(

var soundFile, buffer, window, fileView, grainView, granary, displayGrain;



soundFile = SoundFile.openRead(Platform.resourceDir +/+ "sounds/a11wlk01.wav");

buffer = Buffer.readChannel(s, soundFile.path, channels: [0]);



{

SynthDef(\grain, {

		|bufNum = 0, pos, dur, env = 0.05, pitch = 1, pan = 0, amp = 0.5|

		var envl, sig;

		envl = EnvGen.kr(Env.linen(env, dur, env, amp, -3), doneAction: 2);

		sig = PlayBuf.ar(1, bufNum, pitch, 0, pos.linlin(0,1,0,BufFrames.kr(bufNum))) * envl;



		Out.ar(0, Pan2.ar(sig, pan));

}).add;



	s.sync;

}.fork;



~pos = { rrand(0.2, 0.6) };

~dur = { rrand(0.1, 0.5) };

~env = { rrand(0.3, 0.5) };

~amp = { rrand(0.1, 0.7) };

~pitch = { rrand(0.5, 2) }; // pitch is a ratio

~pan = { rrand(-0.8, 0.8) };

~wait = { 0.1 }; // time between grains



~playTask = Task({ loop {

	var pos, pitch, amp, dur, env;

	Synth(\grain,

		[

			\bufNum, buffer,

			\pos, pos = ~pos.value,

			\pitch, pitch = ~pitch.value,

			\amp, amp = ~amp.value,

			\dur, dur = ~dur.value,

			\env, env = ~env.value,

			\pan, ~pan.value,

		]);

	displayGrain.value(pos, pitch, amp, dur, env);



	~wait.value.wait;

}});





// Grain display



window = Window.new("Grain display", Rect(200, 300, 740, 300));





fileView = SoundFileView().readFile(soundFile).gridOn_(false);

grainView = UserView().resize_(5);



window.layout = StackLayout(fileView, grainView).mode_(\stackAll).index_(1);

window.onClose = {p.stop};

window.front;



// Where information about currently playing grains is stored for drawFunc to access:

granary = ();



// Add a grain to the granary:

displayGrain = {|pos, pitchRatio, amp, dur, env|

	var time, routine;



// I've limited it to displaying 30 grains, but I don't see why it couldn't do more.

	if (granary.size < 30, {



		time = Date.getDate().bootSeconds;

		routine = Routine {

			granary.put(time.asSymbol, [pos, pitchRatio, amp, dur, env]);

			(dur + (env * 2)).wait;

			granary.removeAt(time.asSymbol);

		}.play

	});

};



grainView.animate = true;



grainView.drawFunc = {

	var width, height, now, fileDur;



	width = grainView.bounds.width;

	height = grainView.bounds.height;

	now = Date.getDate().bootSeconds;

	fileDur = soundFile.duration;



	Pen.fillColor = Color.white;

	Pen.strokeColor = Color.black;



	granary.keysValuesDo {|k, v|

		var xP, yP, delta, alpha;



		delta = now - k.asFloat;

		xP = (width * v[0]) + (width * ((delta * v[1]) / fileDur));

		yP = (height * (1 - v[2]));



		if( delta < (v[3] + v[4]),

			{ alpha = (delta/v[4]).clip(0,1) },

			{ alpha = 1 - ((delta - (v[3] + v[4])) / v[4]) }

		);



		Pen.translate(xP, yP); // moveTo doesn't seem to work in this context, so translate...



		Pen.addOval(Rect(0,0,10,10));

		Pen.alpha = alpha;

		Pen.fillStroke;



		Pen.translate(xP.neg, yP.neg); // ...and translate back.

	};

};



// Start the grains:

p = ~playTask.play;



)



// Try some different granular parameters:

(

~pos = { rrand(0.3, 0.8) };

~dur = { rrand(0.01, 0.05) };

~env = { 0.02 };

~amp = { rrand(0.2, 0.6) };

~pitch = { rrand(2, 4.0) };

~pan = { rrand(-0.8, 0.8) };

~wait = { 0.01 }; // time between grains

)



// Or how about these:

(

~pos = { rrand(0, 0.5) };

~dur = { rrand(0.1, 0.2) };

~env = { rrand(0.3, 0.5) };

~amp = { rrand(0.2, 0.8) };

~pitch = { rrand(0.2, 0.7) };

~pan = { rrand(-0.8, 0.8) };

~wait = { 0.1 };

)



// To stop the grains, close the grain display window or evaluate:

p.stop;

(
x = {
        Pan2.ar(AY.ar(
                tonea:  MouseY.kr(10, 3900, 1),
                toneb:  MouseX.kr(10, 3900, 1),
                control:        3,
                vola:   14,
                volb:   14,
                volc:   0,
                mul: 0.1
        ))
}.play;
)

{SinOsc.ar(400)}.play



// Now to define a synth which can be used in patterns etc
(
SynthDef(\ay1, { | freqa=440, freqb=550, freqc=660, vola=15, volb=0, volc=0, chink=1, wobbly=1, pan=0, amp=0.1, gate=1|
        var ay, chinkenv, wobblyenv;

        chinkenv = if(chink>0, EnvGen.kr(Env.new([0.76125, 0.76125, 1, 1], [0.15, 0.5, 0.1], 0, 4, 4)), 1);
        //chinkenv = if(chink>0, {EnvGen.kr(Env.new([2, 2, 1, 1], [0.05, 0, 0.1], 0, 4, 4))}, {1});
        wobblyenv = LFPulse.kr(10, 0.5, mul:wobbly).range(0.5, 1);

        # freqa, freqb, freqc = [freqa, freqb, freqc] * [1, wobblyenv, wobblyenv] * chinkenv;
        ay = AY.ar(AY.freqtotone(freqa), AY.freqtotone(freqb), AY.freqtotone(freqc),
                0, 3, vola, volb, volc, mul: amp);
        ay = ay * EnvGen.kr(Env.asr(0.01, 1, 0.05), gate, doneAction:2);
        Out.ar(0, Pan2.ar(ay, pan));
}).add;
)

x = Synth(\ay1, [\wobbly, 0, \chink, 1, \tonea, 1000.rand]);
x.free;

SynthDescLib.read;

// Use the synth in a jerky lo-fi pattern of some sort...
(
Pbind(
        \instrument, \ay1,
        \freqa, Pseq((#[55, 55, 57, 58, 57, 55, 58, 50]-12).midicps, inf),
        \freqb, Pseq([
                                        Pseq( (#[55, 55, 54, 55, 54, 55, 58, 57]+12).midicps, 2),
                                        Prand((#[55, 55, 54, 55, 54, 55, 58, 57]+12).midicps, 2)
                                ], inf),
        \dur,   Pseq(#[3, 0.5, 0.5, 1.5, 0.5, 1, 1, 4] * 0.4, inf),
        \wobbly,        Pstutter(8 * 4, Prand(#[0, 1], inf)),
        \vola,  15,
        \volb,  14,
        //\chink, Pseq([1,1,0,0,0,0],inf),
        \chink, 1,
        \amp,   0.4
).play
)




( // random colors

w = Window( "colorful", Rect(200,200, 250, 120 ) ).front.decorate;

7.do({ |i| SmoothSlider( w, 30@100 )

.knobColor_( Color.rand(0,0.8).alpha_( [1,0.5,1,1,0,1,0][i] ) )

.background_( Color.rand(0,0.8).alpha_( [1,0.5,0,1,1,0,0][i] ) )

.hilightColor_( Color.rand(0,0.8).alpha_( [1,0.5,1,0,1,0,1][i] ) )

.value_( i.linlin( 0,6,0.2,0.8) )

});

)



( // random colors

w = Window( "colorful", Rect(200,200, 250, 120 ) ).front.decorate;

7.do({ |i| RoundSlider( w, 30@100 )

.knobColor_( Color.rand(0,0.8).alpha_( [1,0.5,1,1,0,1,0][i] ) )

.background_( Color.rand(0,0.8).alpha_( [1,0.5,0,1,1,0,0][i] ) )

.hilightColor_( Color.rand(0,0.8).alpha_( [1,0.5,1,0,1,0,1][i] ) )

.value_( i.linlin( 0,6,0.2,0.8) )

});

)


