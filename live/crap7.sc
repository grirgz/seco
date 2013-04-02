// a simple synth

s.boot;

Ndef(\test, { |pFreq=100, pWidth=0.5| LPF.ar(LFPulse.ar(pFreq, 0, pWidth), 1500) * 0.1 });

Ndef(\test).play;

Ndef(\test).scope;


( // controlspecs for its params

Spec.add(\pFreq, [2, 200, \exp]);

Spec.add(\pWidth, [0.01, 0.5, \exp]);

)

// an editor to see the params changing

NdefGui(Ndef(\test), 5);

Ndef(\test).set(\pFreq, \pFreq.asSpec.map(0.5));

Ndef(\test).set(\pWidth, \pWidth.asSpec.map(0.5));


*new(key, ctlMap)

// make a controlLoop to record and play back slider movement:

// prepare for two normalized controls (range 0 - 1).

// ctlMap defines the actions to take when a control event 

// is played back. here, \x sets the parameter \pFreq, 

// and y will set parameter \pWidth (later). 
Quarks.gui
(

c = CtLoop(\mytest, 

(

x: { |val| Ndef(\test).set(\pFreq, \pFreq.asSpec.map(val)) },

y: { |val| Ndef(\test).set(\pWidth, \pWidth.asSpec.map(val)) }

)

);

)


// a gui to control one synth param, and to record movements

(

EZSlider(nil, nil, \ctLooptest, nil, { |sl| 

var normVal =  sl.value; // 

// keep new events if recording

c.recordEvent(\x, normVal); 

// set synth param

Ndef(\test).set(\pFreq, \pFreq.asSpec.map(normVal)) 

});

)


c.startRec; // wiggle slider now

c.stopRec; // stop when done


c.play; // see slider in NodeProxyEditor for recorded movement

c.stop;



// use the same CtLoop with a 2D controller:

(

w = Window("play me").front;

t = Slider2D(w, Rect(0,0,400,400))

.action_({ |sl| 

var x, y; x = sl.x; y = sl.y;

c.recordEvent(\x, x);

c.recordEvent(\y, y);

Ndef(\test).set(

\pFreq, \pFreq.asSpec.map(x), 

\pWidth, \pWidth.asSpec.map(y) 

);

});

)


c.startRec; // wiggle 2dslider now

c.stopRec; // stop when done


// playback options:

c.play;

c.togglePlay;

c.togglePlay;


// playback is looped by default 

c.reverse;

c.forward;

c.flip; // toggle loop direction

c.flip;


c.tempo = 2; // faster

c.tempo = 0.5; // slower

c.tempo = 1; // orig


// play only part of the loop:

c.start = 0.3; // a segment within range 0.0-1.0. 

// percentage is of the number of events, not of loop duration


c.start = 1.0.rand; // 

c.length = 1.0.rand; // 

c.length = 0.1.rand; // 


// full loop

c.start_(0).length_(1);


// 'sequence jitter':  loop index moves as before, 

// the actual event played is chosen within jitter range near the loop index.

c.jitter = 0.1; // +- 10% loop length jitter

c.jitter = 0.2;

c.jitter = 0.0; // orig. sequence


c.resetLoop; // reset to defaults;




// gesture range can be rescaled in several ways


// turn on gesture rescaling

c.rescaled_(true);


c.invert; // invert around middle axis

c.up; // that is right side up

c.flipInv; // toggle inversion on/off

c.flipInv;



c.scaler = 2; // make gesture bigger

c.scaler = 1; // orig size

c.scaler = 0.5; // smaller

c.scaler = 0.2; // or even smaller


c.shift = 0.1; // shift its range up

c.shift = 0.2;

c.shift = 0.3;

// Q: maybe rescaling could optionally go outside the spec range?


c.resetScaling; // back to orig



c.rescaled_(false);

c.scaler = 0.1; // no effect when rescaled is false


c.rescaled_(true); // should rescale to tiny now

c.scaler = 2; // and big again.



c.dontRescale(\x); // except a control name from rescaling

// so here, x will not be rescaled, but y will.

c.nonRescalableCtls;


c.dontRescale(\x, \y);

c.doRescale(\x, \y); // make sure they all rescale again


c.list; // the list of recorded control events 

c.clear; // clear it


c.startRec; // this also clears the list;

c.stopRec;


c.list.printAll;

c.play;



CtLoop is more about capturing multidimensional gestures, e.g from a Wii or Gamepad joysticks, 

faderboxes etc. It is not about fader automation, but rather exploring and juggling with 

recorded movements as performance.


It is used e.g. in the GamePad class, and could be used with lots of HID, MIDI, 

OSC etc controllers. It will likely move to the Modality quark.





s.boot; // server must be booted, then:


(

GamePad.startHID; // builds the setup automatically.

p = p ?? { ProxySpace(s); }; p.push; // make a proxyspace but only if needed.

)


// Now switch your gamepad to analog mode, and test that you get numbers:


GamePad.verbose = true; // move joysticks to see e.g. [ hid, 19, 0.95686274509804 ] posted.

GamePad.verbose = false; 


// GamePad.stop; // stop HID event loop to turn gamePad off.



// Then you can put a proxy in room 0

(

// a simple proxy : modulating formant frequency, root and number of harmonics

~formsing = { arg formfrq=500, root=300, harm=20, amp=0.4, lag= 0.2;

var in = Blip.ar(root.lag(lag), harm.lag(lag), 0.1);

Formlet.ar(in, formfrq.lag(lag) * [1, 1.06], 0.005, 0.04) 

* 5 * amp.lag(lag);

};

// make sure it plays sound

~formsing.play(vol: 0.25);



// make controlspecs for its parameters:

Spec.specs.put(\formfrq, ControlSpec(100, 5000, \exp));

Spec.specs.put(\root, ControlSpec(5, 500, \exp));

Spec.specs.put(\harm, ControlSpec(1, 256, \exp));



Spec.add(\formfrq, [100, 5000, \exp]);


// make a GUI for the ProxySpace:

ProxyMixer(p);


// then put the proxy into the GamePad, at wing 0 + room 0; 

// wings are in groups of 4 proxies each. 

GamePad.putProxy(0, 

\formsing, 

(

joyRY: { arg val; ~formsing.group.set(\amp, \amp.asSpec.map(val)) }, joyLX: { arg val; ~formsing.group.set(\formfrq, \formfrq.asSpec.map(val)) }, joyLY: { arg val; ~formsing.group.set(\root, \root.asSpec.map(val)) }, joyRX: { arg val; ~formsing.group.set(\harm,\harm.asSpec.map(val)) } )

); 

)




// Look for the devices that are attached:
GeneralHID.buildDeviceList;
// Get the list of devices:
d = GeneralHID.deviceList;
// Check which devices have been found:
GeneralHID.postDevices;
// Pick the 6th device and open it and create an instance of it:
a = GeneralHID.open( d[5] )
d.printAll
// Get info on the device:
a.info;



GeneralHID.startEventLoop
// Get the capabilities of the device in a readable format:
a.caps;
// there are different types of slots:
// button (type 1), has only on/off (1/0) states
// relative (type 2), counts up or down (scrollwheel for example)
// absolute (type 3), continuous value between 0 and 1
// some other may show up on Linux ( Syn (type 0) and Miscellaneous (type 4), but these are generally not very useful).
// See if data is coming in:
a.debug_( true );
// Stop it:
a.debug_( false );
// Debugging can be turned on for each slot individually, if necessary:
//(IBM trackpoint)
a.slots[1].at( 272 ).debug_( true );
// (external mouse on macbook pro)
a.slots[3][1].debug_(true);
(external mouse on ibm thinkpad)
a.slots[2][1].debug_(true);
// Turn it off again: // (IBM trackpoint)
a.slots[1].at( 272 ).debug_( false );
//(external mouse on macbook pro)
a.slots[3][48].debug_(false);
//(external mouse on ibm thinkpad)
a.slots[3][1].debug_(false);

// You can also create a generic gui to see what is coming in:
a.makeGui;

a.slots.keys
a.slots[0]

// The current value of a slot can be checked:
a.slots[1].at( 272 ).value;
a.slots[2].at( 1 ).value;
a.slots[3][1].value
//If the slot is an LED, you can set the value:
a.slots[11][0].value = 1;
a.slots[11][0].value = 0;

// Actions can be mapped to each slot individually.
a.slots[1].at( 278 ).action_( { "hello".postln; } );
a.slots[1].at( 273 ).action_( { "hi".postln; } );
a.slots[3].at( 1 ).action_( { "hi".postln; } );
// with an input to the function
a.slots[3].at( 1 ).action_( { |v| "hi, my value is ".post; v.value.postln; } );
a.slots[1].at( 272 ).action_( { |v| "hi, my value is ".post; v.value.postln; } );

// To stop the action, assign it to an empty function.
a.slots[1].at( 272 ).action_( {} );
a.slots[1].at( 273 ).action_( {} );
a.slots[3].at( 1 ).action_( {} );

// you can access slots, by giving them a key:
a.add( \lx, [3,0] );
a[\lx].debug_( true );
// the last item in the output array, now shows the key
a[\lx].debug_( false );
// save the spec for future use:
a.spec.save( "Impact_help" );
// find a spec defined previously for this device:
c = a.findSpec;
// set it:
a.setSpec( c[0] );
// more info on this in the [GeneralHIDSpec] helpfile
// If the server is running you can create a control bus for the HID data to go to, so that a synth can immediately read the data:
s = Server.local.boot;
// To create the bus:
a.slots[1].at( 272 ).createBus( s ); a.slots[2].at( 8 ).createBus( s );

SynthDef( \hidbus_help, { |out=0,amp=0|
        Out.ar( out, SinOsc.ar( 300, 0, 0.01*amp.abs ) );
}).add;
)
x = Synth.new( \hidbus_help );
x.map( \amp, a.slots[2].at( 8 ).bus );
x.free;

( // a nicer version:
SynthDef( \hidbus_help, { |out=0,amp=0,amp2=0|
        Out.ar( out, SinOsc.ar( 300, 0, 0.01*amp.abs.lag( 0.1 ) * amp2.lag(0.01,0.99) ) );
}).add;
)
x = Synth.new( \hidbus_help );
x.map( \amp, a.slots[2].at( 8 ).bus );
x.map( \amp2, a.slots[1].at( 272 ).bus );
x.free;

( // an even nicer version:
SynthDef( \hidbus_help, { |out=0,freqadd=0,amp=0|
        Out.ar( out, SinOsc.ar( 300 + (freqadd.lag(0.2,1)*40), 0, 0.2*amp.lag(0.01,0.99) ) );
}).add;
)
x = Synth.new( \hidbus_help );
x.map( \freqadd, a.slots[2].at( 8 ).bus );
x.map( \amp, a.slots[1].at( 272 ).bus );
x.free;

// To free the bus:
a.slots[1].at( 272 ).freeBus;
a.slots[2].at( 8 ).freeBus;
// Close the device after use:
a.close;
GeneralHID.stopEventLoop



a = Bus.control(s)
b = Bus.control(s)
a.get { arg i; ("bla:"+i).postln; }
b.get { arg i; ("bla:"+i).postln; }
(
	
	(
		type: \bus,
		array: 3,
		out: b,
		dur: 4
	).play
)
