g

(
//must have power of two framesize- FFT size will be sorted by Convolution to be double this
//maximum is currently a=8192 for FFT of size 16384
a=2048;
//s = Server.local;
//kernel buffer
g = Buffer.alloc(s,a,1);
)


~b = BufferPool.get_mono_sample(\bla, "/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/kick_Dry_b.flac")
~b = BufferPool.get_mono_sample(\bla, "sounds/a11wlk01.wav")
(
//random impulse response
g.set(0,1.0);
200.do({arg i; g.set(a.rand, 1.0.rand)});


        { var input, kernel;

        //input=AudioIn.ar(1);
		var sig, sig2;
        input= PlayBuf.ar(1,~b.bufnum,BufRateScale.kr(~b.bufnum),1,0,1);
        kernel= PlayBuf.ar(1,g.bufnum,BufRateScale.kr(g.bufnum),1,0,1);
		sig = Convolution.ar(input,kernel, 2*a, 0.5);
		sig = SelectX.ar(MouseX.kr(0,1), [sig, input]);

        Out.ar(0,sig);
         }.play;

)




b = Buffer.alloc( s, 2048, 1, _.zeroMsg );
(
        x = { arg i_kernel, density = 100, trigPeriod = 5.0, cutOff = 1000, minFreq = 200, maxFreq = 2000;
                var input, trigFreq, recTrig, irSig, convTrig, convTrigs, bufFrames, conv1, conv2;

                input           = LPF.ar( Dust2.ar( density ), cutOff );
                trigFreq                = trigPeriod.reciprocal;
                recTrig         = Impulse.kr( trigFreq );
                irSig           = Saw.ar( TExpRand.kr( minFreq, maxFreq, recTrig ), 0.4 );
                RecordBuf.ar( irSig, i_kernel, recTrig, loop: 0, trigger: recTrig );
                convTrig                = TDelay.kr( recTrig, BufDur.ir( i_kernel ));
                // split updates across two triggers. Note that [ 1, 0 ] creates
                // a MultiChannel expansion!
                convTrigs               = PulseDivider.kr( convTrig, 2, [ 1, 0 ]);
                bufFrames               = BufFrames.ir( i_kernel );
                // create the two alternatingly updated convolution ugens
                #conv1, conv2   = Convolution2.ar( input, i_kernel, convTrigs, bufFrames );

                XFade2.ar( conv1, conv2, LFTri.kr( trigFreq * 0.5, 1 )) ! 2;
        }.play( s, [ \i_kernel, b ]);
)

x.set( \trigPeriod, 0.1 );      // fast changes
x.set( \trigPeriod, 10.0 );     // slow changes
x.free; // delete synth




( // allocate three buffers
b = Buffer.alloc(s,2048);
c = Buffer.alloc(s,2048);
d = Buffer.alloc(s,2048);

b.zero;
c.zero;
d.zero;
)

(
50.do({ |it| c.set(20*it+10, 1.0.rand); });
3.do({ |it| b.set(400*it+100, 1); });
20.do({ |it| d.set(40*it+20, 1); });
)


(
SynthDef( "conv-test", { arg kernel, trig=0;
        var input;

        input=Impulse.ar(1);

        //must have power of two framesize
        Out.ar(0,Convolution2.ar(input,kernel,trig,2048, 0.5)!2);
}).send(s)

)

(
SynthDef( "conv-test", { arg kernel, trig=0;
        var input;
		var sig;

        input=SoundIn.ar([2,3]);
		sig = Convolution2.ar(input,kernel,trig,2048, 0.5)/8;
		sig = SelectX.ar(MouseX.kr(0,1), [sig, input]);

        //must have power of two framesize
        Out.ar(0,sig);
}).add

)

b = Buffer.read(s, "/home/ggz/Musique/sc/ir/King Tubby/kingtubby-fm1a.wav")
b = Buffer.read(s, "/home/ggz/Musique/sc/ir/Classic/classic-fm1b.wav")
b = Buffer.read(s, "/home/ggz/Musique/sc/ir/King Tubby/kingtubby-tm1a.wav")

x = Synth.new("conv-test",[\kernel,b.bufnum]);

// changing the buffer number:
x.set(\kernel,b.bufnum);
x.set(\trig,0);
x.set(\trig,1); // after this trigger, the change will take effect.
x.set(\kernel,d.bufnum);
x.set(\trig,0);
x.set(\trig,1); // after this trigger, the change will take effect.

d.zero;
40.do({ |it| d.set(20*it+10, 1); });// changing the buffers' contents
x.set(\trig,0);
x.set(\trig,1); // after this trigger, the change will take effect.

x.set(\kernel,b.bufnum);
x.set(\trig,0);
x.set(\trig,1); // after this trigger, the change will take effect.


{ SoundIn.ar([0,1])  }.play
{ SoundIn.ar([2,3])  }.play




(

        { var input, kernel;

        input=AudioIn.ar(1) / 8;
        kernel= Mix.ar(LFSaw.ar([300,500,800,1000]*MouseX.kr(1.0,2.0),0,1.0));

        //must have power of two framesize
        Out.ar(0,Convolution.ar(input,kernel, 1024, 0.5) / 18 !2);
         }.play;

)
(1,4..20)
(

        { var input, kernel;
			var sig;

        input=AudioIn.ar(3) / 8;
        kernel= LFSaw.ar(MouseX.kr(50,500) * (1,4..20)/15,0,1.0);
		kernel = kernel.sum;
		//kernel = RLPF.ar(kernel, MouseY.kr(50,1500), 0.2);

		sig = Convolution.ar(input,kernel, 1024, 0.5) / 18;
		sig = SelectX.ar(MouseY.kr(0,1), [sig, input]);
        //must have power of two framesize
        Out.ar(0,sig  ! 2);
         }.play;

)


b
