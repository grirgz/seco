(

	b = Buffer.read(s, "~/Musique/sc/tmpsounds/d1.wav".standardizePath);

p = SoundFile.collectIntoBuffers("~/Musique/sc/tmpsounds/samples/*".standardizePath);

)



(

~bpm = 190;

~n = 8;

~dur = 60 / ~bpm / 2;



SynthDef(\sampler, { arg buff = b, pos, len, g_size, sel_stream,

	st_size = 0.5;

	var slice, rd, out, dry, // slices vars

	rd1, rd2, hann1, hann2, pos1, pos2, gr1, gr2, // t_stretch vars

	st, stut; // stutter

	slice = BufFrames.kr(buff) / ~n;



	// read slices



	rd = Line.ar(pos * slice, (pos * slice) + (slice * len),

		~dur * len, doneAction: 2);

	dry = BufRd.ar(2,buff,rd);



	// time stretch



	rd1 = LFSaw.ar(1000 / g_size, 1, 0.5, 0.5);

	hann1 = (rd1 * pi).sin;

	pos1 = Latch.ar(Line.ar(pos * slice, (pos * slice) + slice,

		~dur * len, doneAction: 2), rd1 > 0.99);

	gr1 = BufRd.ar(2,buff, (rd1 * 44.1 * g_size) + pos1) * hann1;



	rd2 = Wrap.ar(rd1 + 0.5);

	hann2 = (rd2 * pi).sin;

	pos2 = Latch.ar(Line.ar(pos * slice, (pos * slice) + slice,

		~dur * len, doneAction: 2), rd2 > 0.99);

	gr2 = BufRd.ar(2,buff, (rd2 * 44.1 * g_size) + pos2) * hann2;



	// stt



	stut = LFSaw.ar((1000 / (slice / 44.1)) / st_size ,1, 0.5, 0.5);

	st = BufRd.ar(2, buff, (slice * pos) + (stut * slice * st_size)) * 0.75;



	out = Select.ar(sel_stream, [dry, gr1 + gr2, st]);

	Out.ar(2, out);

}).add;



SynthDef(\p_shift, {arg pitch;

	var in, rd1, rd2, hann1, hann2, snd1, snd2;

	in = In.ar(2,2);

	rd1 = LFSaw.ar(pitch, 1, 0.5, 0.5);

	hann1 = (rd1 * pi).sin;

	rd2 = Wrap.ar(rd1 + 0.5);

	hann2 = (rd2 * pi).sin;

	snd1 = AllpassL.ar(in, 1, rd1 * 0.01,0) * hann1;

	snd2 = AllpassL.ar(in, 1, rd2 * 0.01,0) * hann2;

	Out.ar(0, snd1 + snd2);

}).add;

)



(

var len, buff, sel, size, count, st_size, pitch, sh = Synth(\p_shift);

~fX = Task({

	inf.do({

		if(rrand(0,10) > 8,{pitch = [-50,50].choose}, {pitch = 10});

		if(rrand(0,10) < 4, {buff = p[rrand(0,p.size - 1)]},

			{buff = b});

		count = rrand(0,50);

		if(count < 40, {sel = 0},

			if((count > 39) && (count < 45),{size = rrand(20,50); sel = 1},

				if((count > 44) && (count < 51),

					{st_size = [0.5, 0.25, 0.125].choose; sel = 2}

		)));

		~dur.wait;

	});

}).play;



~t = Task({

	256.do({

		len = rrand(2,3);
		len = 1;

		sh.set(\pitch, pitch);

		Synth(\sampler, [\g_size, 90, \sel_stream ,2,

			\buff, b, \pos, 0, \len, len, \st_size, 0.5]);

		(~dur * len).wait;

	});

}).play;

)
