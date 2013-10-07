a = ()
a.b = ()
a.b.c = 8
a.b.d = 3
a
a.b.atFail(\d, {a.b.d = 9})
a



    (
     
    ~beat2 = Routine {
     
            16.do({ arg a;
                    var tableau =  [1,2,3,4];
           var tableau1 =  [100,200,300];
                    tableau.do{
                            arg x,y ;
                            h             = Array.series(2, 0+tableau1[y], 100*x);
     
                            b = Button(w, Rect(h, h[1], 100, 100));
                            //rrand(0.01,0.05).wait;
                            rrand(1,5).wait;
                    };
     
            });
            w.front;
    };
    )
     
    tableau = [0,0]
    tableau = [0,100]
    tableau = [0,200]
    tableau = [0,300]
    tableau = [100,0]
    tableau = [100,100]
    tableau = [100,200]
    tableau = [100,300]
    tableau = [200,0]
    tableau = [200,100]
    tableau = [200,200]
    tableau = [200,300]
    tableau = [300,0]
    tableau = [300,100]
    tableau = [300,200]
    tableau = [300,300]
     
    s.plotTree;
    p = ProxySpace.push();
     
    b = Button(w, Rect(x, x, 100, 100))
    b.value=1;
     
    (
    w = Window.new("The Four Noble Truths");
     
     
    b = Button(w, Rect(0, 0, 100, 100))
            .states_([
                        ["DISTORTION1", Color.black, Color.red],
                        ["DISTORTION2", Color.white, Color.black],
                        ["DISTORTION3", Color.red, Color.white],
                        ["OFF", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                        butt.value.postln;
            if(butt.value == 0){
                    ~dist = SynthDef(\distort, {
                            arg out=0, pregain=100, amp=1, gate=1;
                            var env;
                            env = Linen.kr(gate, 0.05, 1, 0.1, 2);
                            XOut.ar(2,env,(In.ar(12, 2) * pregain).distort * amp)! 2 // fan out...
                    }, [\ir, 0.1, 0.1, 0.1])
            };
            if(butt.value == 1){
                    ~dist=  SynthDef(\distortion, {
                            arg outputChannels = 0, inputChannels = 12, drive = 1, distortionType = 1, outAmp = 1;
     
                            // a ** b = pow(a, b)
     
                            var distortedOutput;
                            distortedOutput = In.ar(inputChannels, 2) * (((drive ** 2) + 0.02) * 50);
     
                            distortedOutput = SelectX.ar
                            (
                                    distortionType,
                                    [
                                            distortedOutput.softclip,
                                            distortedOutput.distort,
                                            distortedOutput.clip(-1, 1),
                                            distortedOutput.fold(-1, 1)
                                    ]
                            );
     
                            distortedOutput = distortedOutput * ((outAmp ** 2) * (1 - (drive / 2.6)));
     
                            Out.ar(outputChannels, distortedOutput);
            })};
            if(butt.value == 2){
                    ~dist = SynthDef(\distort, { arg out=0, pregain=100, amp=1, gate=1;
     
                            var env,dec,sum;
                            var amount = MouseX.kr(-0.1, 0.1);
                            var k = 2*amount/(1-amount);
                            env = Linen.kr(gate, 0.05, 1, 0.1, 2);
                            dec = CrossoverDistortion.ar(In.ar(12, 2) * pregain,LFNoise2.kr(2,1,20000));
                            sum =MidEQ.ar(HPF.ar((1+k)*dec/(1+(k*dec.abs)), [300, 390])*0.5, 120, 0.7, 8);
                            XOut.ar(0,env,(sum).distort * amp)! 2 // fan out...
                            }, [\ir, 0.1, 0.1, 0.1]
                    )
            };
            if(butt.value == 3){
                    "03".postln;
                    ~dist.free;
            };
            });
    c = Button(w, Rect(100, 0, 100, 100))
    .states_([
            ["REVERB", Color.black, Color.red],
            ["DELAY", Color.white, Color.black],
            ["FEEDBACK", Color.red, Color.white],
            ["OFF", Color.blue, Color.clear]
    ])
    .action_({ arg butt;
            butt.value.postln;
                    if(butt.value == 0){
                    ~dist1 = SynthDef(\distort, {
                            arg out=0, pregain=100, amp=1, gate=1;
                            var env;
                            env = Linen.kr(gate, 0.05, 1, 0.1, 2);
                            XOut.ar(2,env,(In.ar(0, 2) * pregain).distort * amp)! 2 // fan out...
                    }, [\ir, 0.1, 0.1, 0.1])
            };
            if(butt.value == 1){
                    ~dist1=  SynthDef(\distortion, {
                            arg outputChannels = 2, inputChannels = 0, drive = 1, distortionType = 1, outAmp = 1;
     
                            // a ** b = pow(a, b)
     
                            var distortedOutput;
                            distortedOutput = In.ar(inputChannels, 2) * (((drive ** 2) + 0.02) * 50);
     
                            distortedOutput = SelectX.ar
                            (
                                    distortionType,
                                    [
                                            distortedOutput.softclip,
                                            distortedOutput.distort,
                                            distortedOutput.clip(-1, 1),
                                            distortedOutput.fold(-1, 1)
                                    ]
                            );
     
                            distortedOutput = distortedOutput * ((outAmp ** 2) * (1 - (drive / 2.6)));
     
                            Out.ar(outputChannels, distortedOutput);
            })};
            if(butt.value == 2){
                    ~dist1 = SynthDef(\distort, { arg out=0, pregain=100, amp=1, gate=1;
     
                            var env,dec,sum;
                            var amount = MouseX.kr(-0.1, 0.1);
                            var k = 2*amount/(1-amount);
                            env = Linen.kr(gate, 0.05, 1, 0.1, 2);
                            dec = CrossoverDistortion.ar(In.ar(0, 2) * pregain,LFNoise2.kr(2,1,20000));
                            sum =MidEQ.ar(HPF.ar((1+k)*dec/(1+(k*dec.abs)), [300, 390])*0.5, 120, 0.7, 8);
                            XOut.ar(2,env,(sum).distort * amp)! 2 // fan out...
                            }, [\ir, 0.1, 0.1, 0.1]
                    )
            };
            if(butt.value == 3){
                    "03".postln;
                    ~dist1.free;
            };
    });
    d = Button(w, Rect(200, 0, 100, 100))
    .states_([
            ["CHORUS", Color.black, Color.red],
            ["FLANGER", Color.white, Color.black],
            ["PHASER", Color.red, Color.white],
            ["OFF", Color.blue, Color.clear]
    ])
    .action_({ arg butt;
            butt.value.postln;
    });
    e = Button(w, Rect(300, 0, 100, 100))
    .states_([
            ["DISTORTION4", Color.black, Color.red],
            ["DISTORTION5", Color.white, Color.black],
            ["DISTORTION6", Color.red, Color.white],
            ["OFF", Color.blue, Color.clear]
    ])
    .action_({ arg butt;
            butt.value.postln;
    });
    f = Button(w, Rect(0, 100, 100, 100))
    .states_([
            ["there is suffering", Color.black, Color.red],
            ["the origin of suffering", Color.white, Color.black],
            ["the cessation of suffering", Color.red, Color.white],
            ["there is a path to cessation of suffering", Color.blue, Color.clear]
    ])
    .action_({ arg butt;
            butt.value.postln;
    });
    i = Button(w, Rect(100, 100, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    j = Button(w, Rect(200, 100, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    k = Button(w, Rect(300, 100, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    g = Button(w, Rect(0, 200, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    l = Button(w, Rect(100, 200, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    m = Button(w, Rect(200, 200, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    n = Button(w, Rect(300, 200, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    h = Button(w, Rect(0, 300, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    o = Button(w, Rect(100, 300, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    r = Button(w, Rect(200, 300, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    q = Button(w, Rect(300, 300, 100, 100))
            .states_([
                ["there is suffering", Color.black, Color.red],
                ["the origin of suffering", Color.white, Color.black],
                ["the cessation of suffering", Color.red, Color.white],
                ["there is a path to cessation of suffering", Color.blue, Color.clear]
            ])
            .action_({ arg butt;
                butt.value.postln;
            });
    w.front;
    )


	(
	
		~make_matrix = { arg xsize=4, ysize=4;

			var vlayout;
			var hlayout;
			var win;

			vlayout = VLayout.new;
			win = Window.new("bla");
			win.layout = vlayout;

			xsize.do { arg x;
				hlayout = HLayout.new;
				vlayout.add(hlayout);
				ysize.do { arg y;
				
					var but = Button.new
					.states_([
						["there is suffering", Color.black, Color.red],
						["the origin of suffering", Color.white, Color.black],
						["the cessation of suffering", Color.red, Color.white],
						["there is a path to cessation of suffering", Color.blue, Color.clear]
					])
					.action_({ arg butt;
						butt.value.postln;
					});
					hlayout.add(but);
				}
			};
			win.front;
		};
		~make_matrix.()
	)
