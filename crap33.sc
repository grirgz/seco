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

