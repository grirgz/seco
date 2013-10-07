
(
SynthDef(\sin, { arg out=0, sendbus, send, amp=0.1, gate=1, pan=0, freq=200;
	var sig;
	sig = SinOsc.ar(freq);
	sig = sig * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	sig = Pan2.ar(sig, pan, amp);
	Out.ar(out, sig);
	Out.ar(sendbus, sig * send);
}).add;

SynthDef(\disto, { arg in, send=0.5, out=0, sendbus, amp=0.1, gate=1, pan=0, freq=200;
	var sigin, sig;
	sigin = In.ar(in, 2);
	sig = sigin.distort * SinOsc.kr(4).range(0,1);
	Out.ar(out, sig);
	Out.ar(sendbus, sig * send);
}).add;

SynthDef(\reverb, { arg in, out=0, amp=0.1, gate=1, pan=0, freq=200;
	var sig;
	sig = In.ar(in, 2);
	sig = sig * (SinOsc.ar(300)/47);
	sig = CombL.ar(sig, 0.1,[0.051,0.3],1.0) + sig;
	Out.ar(out, sig);
}).add;

SynthDef(\monitor, { arg in0, in1, in2, out=0, amp=0.1, gate=1, pan=0, freq=200;
	var sig;
	sig = [
		In.ar(in0, 2),
		In.ar(in1, 2),
		In.ar(in2, 2),
	].sum / 3;
	Out.ar(out, sig);
}).add;
)

(
	~aux1 = Bus.audio(s, 2);
	~aux2 = Bus.audio(s, 2);
	~out0 = Bus.audio(s, 2);
	~out1 = Bus.audio(s, 2);
	~out2 = Bus.audio(s, 2);
)

(
	~fx1 = Group.after;
	~fx2 = Group.after(~fx1);
	~monitor = Group.after(~fx2);
	~sin = Synth(\sin, [\out, ~out0, \sendbus, ~aux1, \send, 0.5]);
	~disto = Synth(\disto, [\in, ~aux1, \out, ~out1, \sendbus, ~aux2, \send, 0.5], ~fx1);
	~reverb = Synth(\reverb, [\in, ~aux2, \out, ~out2], ~fx2);
	~monitor = Synth(\monitor, [\out, 0, \in0, ~out0, \in1, ~out1, \in2, ~out2], ~monitor);
)

~sin.set(\send, 0)
~disto.set(\send, 0)



p.clear

~sin = { LFSaw.ar(300 * [1,1.0001]) ! 2 }
~sin.play;
~disto = { arg send=1.0; RLPF.ar(~sin.ar, 7000, 0.3) * send  };
~disto.play;
~reverb = { arg send=1.0; ~disto.ar * SinOsc.ar(5).range(0,2) * send  };
~reverb.play;

~disto.set(\send, 0);
~reverb.set(\send, 1);


Control

p = ProxySpace.push;

(

	
~src1 = { LFSaw.ar(100) ! 2 };
~src2 = { LFSaw.ar(200) ! 2 };
~src3 = { LFSaw.ar(300) ! 2 };
~src4 = { LFSaw.ar(400) ! 2 };

~fx1[0] = { Silent.ar ! 2 };
~fx2[0] = { Silent.ar ! 2 };
~fx3[0] = { Silent.ar ! 2 };
~fx4[0] = { Silent.ar ! 2 };
~fx1[100] = \filter -> { arg in; RLPF.ar(in, 550 * SinOsc.kr(1).range(0.5,1)) };
~fx2[100] = \filter -> { arg in; RLPF.ar(in, 550 * SinOsc.kr(2).range(0.5,1)) };
~fx3[100] = \filter -> { arg in; RLPF.ar(in, 550 * SinOsc.kr(3).range(0.5,1)) };
~fx4[100] = \filter -> { arg in; RLPF.ar(in, 550 * SinOsc.kr(4).range(0.5,1)) };

~out.play;
~out = { Silent.ar ! 2 };
v[\routing] = 1 ! (3*4);
v[\src] = [~src1, ~src2, ~fx1, ~fx2];
v[\out] = [~out, ~fx1, ~fx2];
//v[\out].do { arg out, outx;
//	out.play;
//	v[\src].do { arg in, inx;
//		var key = \routing ++ ((outx * 3) + inx);
//		key = key.asSymbol;
//		p[key] = { arg amp; amp };
//	};
//};
)
(
v[\out].do { arg out, outx;
	out.play;
	v[\src].do { arg in, inx;
		//var key = \routing ++ ((outx * 3) + inx);
		var key = \amp ++ inx;
		key = key.asSymbol;

		out[inx] = { in.ar * key.kr(0) };
	};
};
)

(

	//v[\out].do { arg out; out.play };
	~out.play;
	v[\make_mixer] = {
		var win;
		var hlay, vlay;
		win = Window.new("hein");
		
		vlay = VLayout.new;
		win.layout = vlay;

		v[\out].do { arg out, outx;
			hlay = HLayout.new;
			vlay.add(hlay);
			v[\src].do { arg in, inx;
				var knob;
				//var key = \routing ++ ((outx * 3) + inx);
				var key = \amp ++ inx;
				key = key.asSymbol;

				knob = Knob.new;
				knob.action = { arg k;
					out.set(key, k.value);
				};
				knob.value = out.get(key);
				hlay.add(knob);
			};
		};

		win.front;
	};
	v[\make_mixer].value;
)





~out = { arg routing;
	8.do { arg x;

	
	}

};

~bla = 4

~bla3 = [1,2,3]
~bla3[1] = 4
~bla[1]
~bla3
~rah = { SinOsc.ar(~bla3.kr[1].kr) };

~cc = \control -> [1,2,3]
~cc.ar(1,3)
~rah = { SinOsc.ar(~cc.kr(1,2)) };
~rah.play
~mm = \mix -> [100,200,300]
BusPlug
Control

NodeProxy
p = ProxySpace.push;
~cc = \control -> [100,200,300]
~cc = \control -> [100,200,500]
~cc.put(2, 100)
~cc[0]
~cc.source.value[0] = 500
~cc.set(\0, 500)
~rah = { SinOsc.ar(~cc.kr(1,2)) ! 2 };
~rah = nil
~rah.clear
~rah.play



2 * -1

~nodes = [100,200,300]
~nodes.source[0] = 500
~nodes.clear
~rah = { SinOsc.ar(~nodes.kr(1,2)) ! 2 };
~rah.play









////////////////////////////////////



(
	v = ();

	v[\make_mixer] = { arg space, numsrc, numfx;
		var inlist = List.new, 
			outlist = List.new;

		space[\out] = { Silent.ar ! 2 };
		outlist.add(space[\out]);

		"kjkj".debug("1");
		numsrc.do { arg srcx;
			var key = (\src ++ srcx).asSymbol;
			space[key] = { Silent.ar ! 2 };
			inlist.add(space[key]);
		};
		"kjkj".debug("2");
		numfx.do { arg fxx;
			var key, out_key;
			fxx = fxx+1;
			key = (\fx ++ fxx).asSymbol;
			out_key = (\fxout ++ fxx).asSymbol;
			space[out_key] = { Silent.ar ! 2 }; 
			inlist.add(space[out_key]);
			outlist.add(space[key]);
		};
		"kjkj".debug("3");

		outlist.do { arg out, outx;
			//out.play;
			inlist.do { arg in, inx;
				var key = \amp ++ inx;
				key = key.asSymbol;

				out[inx] = { in.ar(2) * key.kr(0) };
			};
		};
		"kjkj".debug("4");

		{ v[\make_mixer_gui].(space, numsrc, numfx) };
	};

	v[\make_mixer_gui] = { arg space, numsrc, numfx;
		var win;
		var hlay, vlay;
		var inlist = List.new, 
			outlist = List.new;

		outlist.add(p[\out]);

		numsrc.do { arg srcx;
			var key = (\src ++ srcx).asSymbol;
			inlist.add(space[key]);
		};
		numfx.do { arg fxx;
			var key;
			fxx = fxx + 1;
			key = (\fx ++ fxx).asSymbol;
			inlist.add(space[key]);
			outlist.add(space[key]);
		};

		win = Window.new("hein");
		
		vlay = VLayout.new;
		win.layout = vlay;

		outlist.do { arg out, outx;
			var label;
			hlay = HLayout.new;
			vlay.add(hlay);
			label = StaticText.new;
			hlay.add(label);
			if(outx == 0) {
				label.string = "out";
			} {
				label.string = "fx" ++ (outx);
			};
			inlist.do { arg in, inx;
				var knob;
				var key = \amp ++ inx;
				key = key.asSymbol;

				knob = Knob.new;
				knob.action = { arg k;
					out.set(key, k.value);
				};
				knob.value = out.get(key);
				hlay.add(knob);
			};
		};

		win.front;
		
	};
)



p.clear

(
	p = ProxySpace.push;
	v[\gui] = v[\make_mixer].(p, 2, 2);
	~saw0 = { LFSaw.ar(100) ! 2 };
	~saw1 = { LFSaw.ar(200) ! 2 };

	~rlpf1 = { RLPF.ar(\in.ar(0!2), 550 * SinOsc.kr(1).range(0.5,1)) };
	~rlpf2 = { RLPF.ar(\in.ar(0!2), 550 * SinOsc.kr(2).range(0.5,1)) };

	
)
~rlpf1
~rlpf2
~fx1
~fx2
~fxout1
~fxout2
~out

(
	
	~src0 = ~saw0;
	~src1 = ~saw1;
	~rlpf1.map(\in, ~fx1); ~fxout1 = ~rlpf1;
	~rlpf2.map(\in, ~fx2); ~fxout2 = ~rlpf2;

	~out.play;
)
	v[\gui].();

	~out.play;
	~out.stop;
	~fxout0.stop

	~fx1.play
	~fx1.stop
	~fxout1.play
	~fxout1.stop
	~rlpf1.play
	~rlpf1.unmap(\in)
	~rlpf1



	~mfx.clear

	~mfx[0] = { LFSaw.ar(200) ! 2 }
	~mfx[100] = \filter -> { arg in; RLPF.ar(in, 550 * SinOsc.kr(2).range(0.5,1)) };
	~mfx[100] = \filter -> ~rlpf1
	~mfx[100] = ~rlpf1
	~mfx[100] = nil
	~mfx[100] = \filter -> { DC.ar(0)!2 }
	~rlpf1.map(\in, ~mfx)
	~rlpf1 <<> ~mfx
	~mfx.play
	~mfx.stop
	~rlpf1 = { RLPF.ar(\in.ar(0!2), 550 * SinOsc.kr(8).range(0.5,1)) };
	~rlpf1.clear
	~rlpf1.play
	~rlpf1.stop


	~blaout.clear
	~blaout = { LFSaw.ar(100) ! 2}
	~blaout.play
	~bla = { LFSaw.ar(101) ! 2 }
	~blaout = ~bla



	~mfx[0] = { LFSaw.ar(100) ! 2 }
	~mfx.play
	~mfx.stop

	~rlpf1 = { RLPF.ar(\in.ar(0!2), 750 * SinOsc.kr(8).range(0.5,1)) };

	~rlpf1.map(\in, ~mfx); ~ofx = ~rlpf1
	~ofx.play;
	~out

////////////////////////////////////
////////////////////////////////////
////////////////////////////////////



(
	v = ();

	v[\make_mixer] = { arg space, numsrc, numfx;
		var inlist = List.new, 
			outlist = List.new;

		space[\out] = { Silent.ar ! 2 };
		outlist.add(space[\out]);

		"kjkj".debug("1");
		numsrc.do { arg srcx;
			var key = (\src ++ srcx).asSymbol;
			space[key] = { Silent.ar ! 2 };
			inlist.add(space[key]);
		};
		"kjkj".debug("2");
		numfx.do { arg fxx;
			var key, out_key;
			fxx = fxx+1;
			key = (\fx ++ fxx).asSymbol;
			out_key = (\fxout ++ fxx).asSymbol;
			space[out_key] = { Silent.ar ! 2 }; 
			inlist.add(space[out_key]);
			outlist.add(space[key]);
		};
		"kjkj".debug("3");

		outlist.do { arg out, outx;
			//out.play;
			inlist.do { arg in, inx;
				var key = \amp ++ inx;
				key = key.asSymbol;

				out[inx] = { in.ar(2) * key.kr(0) };
			};
		};
		"kjkj".debug("4");

		(
			gui: { v[\make_mixer_gui].(space, inlist, outlist) };
			midi: { v[\make_mixer_midi].(space, inlist, outlist) };
		)
	};

	v[\make_mixer_gui] = { arg space, inlist, outlist;
		var win;
		var hlay, vlay;

		win = Window.new("hein");
		
		vlay = VLayout.new;
		win.layout = vlay;

		outlist.do { arg out, outx;
			var label;
			hlay = HLayout.new;
			vlay.add(hlay);
			label = StaticText.new;
			hlay.add(label);
			if(outx == 0) {
				label.string = "out";
			} {
				label.string = "fx" ++ (outx);
			};
			inlist.do { arg in, inx;
				var knob;
				var key = \amp ++ inx;
				key = key.asSymbol;

				knob = Knob.new;
				knob.action = { arg k;
					out.set(key, k.value);
				};
				knob.value = out.get(key);
				hlay.add(knob);
			};
		};

		win.front;
		
	};

	v[\make_mixer_midi] = { arg space, inlist, outlist;

		var midi_numbers = [
			1,2,3,4, 5,6,7,8,
			1,2,3,4, 5,6,7,8,
			1,2,3,4, 5,6,7,8,
			1,2,3,4, 5,6,7,8,
		];

		4.do { arg outx;
			4.do { arg inx;
				var knob;
				var key = \amp ++ inx;
				key = key.asSymbol;

				MIDIdef(\mixer ++ )
				knob.action = { arg k;
					out.set(key, k.value);
				};
				knob.value = out.get(key);
				hlay.add(knob);
			};
		};

		win.front;
		
	};

)

	p = ProxySpace.push;


(
	// setup mixer

	v[\mixer] = v[\make_mixer].(p, 2, 2);  // two sources and two fx

	// define sources and effects
	
	~saw0 = { LFSaw.ar(100) ! 2 };
	~saw1 = { LFSaw.ar(200) ! 2 };

	~rlpf1 = { RLPF.ar(\in.ar(0!2), 550 * SinOsc.kr(1).range(0.5,1)) };
	~rlpf2 = { RLPF.ar(\in.ar(0!2), 550 * SinOsc.kr(2).range(0.5,1)) };

	// put sources and effects in mixer
	
	~src0 = ~saw0;
	~src1 = ~saw1;
	~rlpf1.map(\in, ~fx1); ~fxout1 = ~rlpf1; // redirect fx input in rlpf and redirect rlpf output in fxout
	~rlpf2.map(\in, ~fx2); ~fxout2 = ~rlpf2;

	// show gui

	v[\mixer].gui;
	v[\mixer].midi;

	// monitor mixer out

	~out.play;
)

