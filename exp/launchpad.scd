
MIDIdef

MIDIOut.noteOn(1, 60, 64)

m = MIDIOut(0);

m.noteOn(0, 1, 60);
m.noteOn(0, 61, 60);
m.noteOff(1, 61, 60);
m.allNotesOff(16);


(
Task({
	127.do { arg i;
		m.noteOn(0, 1, i);
		i.postln;
		0.5.wait;
	}
}).play
)


(
~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\launchpad, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	~mididict[midinote].stop;
	pat = Pbind(
		\note, Pseq([
			//Pseq([0,\r,0,0].scramble,2),
			//Pseq([0,\r,\r,\r].scramble),
			Pseq([0] ++ ((0 ! 8) ++ (\r ! 8)).drop(1).scramble),
		],inf),
		\ctranspose, midinote-60,
		\dur, 1/8,
	);
	m.noteOn(0, midinote, [15,60,62,47].choose);
	pat = pat.trace.play(quant:[1/8]);
	~mididict[midinote] = pat;

}); // match any noteOn
MIDIdef.noteOff(\launchpadoff, {arg vel, midinote;
	var note = ~mididict[midinote];
	[vel, midinote].postln;
	TempoClock.play({ 
		note.stop;
		m.noteOn(0, midinote, 0);
	}, quant:0.5)

}); // match any noteOn
)
MIDIdef.freeAls

(
~mididict = IdentityDictionary.new;
MIDIdef.noteOn(\launchpad, {arg vel, midinote;
	var pat, pat2;
	[vel, midinote].postln;

	"vim --servername SCVIM --remote-send <Esc>:call SCveco_open_buffer("%", %)<Enter>".format(midinote, midinote).unixCmd;

}); // match any noteOn
MIDIdef.noteOff(\launchpadoff, {arg vel, midinote;

}); // match any noteOn
)
"vim --servername SCVIM --remote-send '<Esc>:call SCveco_open_buffer(\"7\", 6)<Enter>'".unixCmd

~launchpad_gridstate = Array2D.new(8,8)

(
~launchpad = (
	new: { arg self;
		self = self.deepCopy;
	
		self.midiout = MIDIOut(0);

		self.init_midi_layout;
		
	
		self;
	},

	state: { arg self;
		~launchpad_gridstate;
	},

	colors: (
			red: 15,
			green: 60,
			yellow: 62,
			orange: 47,
			amber: 63,
			off: 0,
	),

	init_midi_layout: { arg self;
		
		self.gridlayout = 8.collect { arg x;
			8.collect { arg y;
				[x,y].debug("x,y");
				x+(y*16).debug("res");
			}
		};
		self.gridlayout.debug("gridlayout");
	},

	color_to_velocity: { arg self, color;
		if(color.isNumber) {
			color
		} {
			self.colors[color] ?? 63
		}
		
	},

	gridpos_to_midi: { arg self, pos;
		self.gridlayout[pos.x][pos.y].debug("midipos");
	},

	midi_to_gridpos: { arg self, midi;
		var x, y;
		x = midi % 16;
		y = (midi / 16).asInteger;
		Point(x, y);
	},

	set_led: { arg self, pos, color;
		var midi;
		if(pos.class == Point) {
			midi = self.gridpos_to_midi(pos);
		} {
			midi = pos;
			pos = self.midi_to_gridpos(pos);
		};
		self.state[pos.x,pos.y] = color;
		color = self.color_to_velocity(color);
		[0,midi,color].debug("send noteOn");
		self.midiout.noteOn(0, midi, color);

	},
);
~launchpad_scene = (
	parent: ~launchpad,

	install_midi_responder: { arg self;
		MIDIdef.noteOn(\launchpadOn, { arg vel, midinote;
			self.response_on(midinote);
		});
		MIDIdef.noteOff(\launchpadOff, { arg vel, midinote;
			self.response_off(midinote);
		})
	},

	activate: { arg self;
		self.install_midi_responder;
	},

	deactivate: { arg self;
		// TODO
	},

	response_on: { arg self;
		
	},

	response_off: { arg self;
		
	},
);

~launchpad_scene_simple = (
	parent: ~launchpad_scene,

	//state: Array2D.new(8,8),
	color_on: \red,
	color_off: \off,
	range_state: Array.fill(8, 0),
	ranges: [6,7],

	get_range_state: { arg self, index;
		self.range_state[index];
	},

	set_range_state: { arg self, index, val;
		self.range_state[index] = val;
	},

	response_on: { arg self, midinote;
		var pos;
		var color;
		"resp: on".debug;
		pos = self.midi_to_gridpos(midinote);
		pos.debug("pos");
		color = \red;
		if(self.ranges.includes(pos.y)) {
			8.do { arg x;
				self.set_led(self.gridpos_to_midi(Point(x, pos.y)), \off);
			};
			self.set_led(midinote, \green);
			self.set_range_state(pos.y, pos.x);
		} {
			if(self.state[pos.x,pos.y] == color) {
				color = \off;
			};
			self.set_led(midinote, color);
		};
		self.state[pos.x,pos.y].debug("state");
	},

	pseqrow: { arg self, y;
		Pseq(8.collect { arg x;
			if(self.state[x, y] == self.color_on) {
				1
			} {
				\rest
			}
		})
	},

	response_off: { arg self;
		
	},
);

~launchpad_scene_tile = (
	parent: ~launchpad_scene,

	//state: Array2D.new(8,8),
	color_on: \red,
	color_off: \off,
	range_state: Array.fill(8, 0),
	ranges: [6,7],

	get_range_state: { arg self, index;
		self.range_state[index];
	},

	set_range_state: { arg self, index, val;
		self.range_state[index] = val;
	},

	midinote_to_tile_index: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		pos = pos.x + (pos.y * 8);
		pos;
	},

	response_on: { arg self, midinote;
		var tile = self.midinote_to_tile_index(midinote);
		var res;
		~veco.vim_open_buffer(tile);
		tile.debug("response_on: tile");
		res = ~veco.toggle_play_node_by_index(tile);
		res.debug("res:::!!!!");
		if(res == true) {
			~la.set_led(midinote, \green);
		} {
			~la.set_led(midinote, \off);
		};
	},

	pseqrow: { arg self, y;
		Pseq(8.collect { arg x;
			if(self.state[x, y] == self.color_on) {
				1
			} {
				\rest
			}
		})
	},

	response_off: { arg self;
		
	},
);
~la = ~launchpad_scene_tile.new;
~la.activate;
~la.set_led(0@0, \yellow);

)

(
Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0],inf) * Pn(Plazy({ ~la.pseqrow(0) }), inf),
	\dur, 1/4,
	\amp, 0.1
)).play;
Pdef(\plop2, Pbind(
	\instrument, \default,
	\degree, Pseq([2],inf) * Pn(Plazy({ ~la.pseqrow(1) }), inf),
	\octave, 4,
	\dur, 1/4,
	\amp, 0.1
)).play;
Pdef(\plop3, Pbind(
	\instrument, \default,
	\degree, Pseq([4],inf) * Pn(Plazy({ ~la.pseqrow(2) }), inf),
	\octave, 3,
	\dur, 1/4,
	\amp, 0.1
)).play;
);	

Pbindef(\plop3, \ctranspose, 2)

(
~con.automate({

	SinOsc.kr(1).range(100,3000)
})
)

(
~trans = Pbind(
	\mtranspose, Pfunc({ ~la.get_range_state(7) }),
);
~con = GenericGlobalControl.new;
~con.set(300);


Pdef(\plop, Pbind(
	\instrument, \default,
	\degree, Pseq([0],inf) * Pn(Plazy({ ~la.pseqrow(0) }), inf),
	\dur, 1/4,
	\amp, 0.1
) <> ~trans).play;
Pdef(\plop2, Pbind(
	\instrument, \osc1,
	\degree, Pseq([2],inf) * Pn(Plazy({ ~la.pseqrow(1) }), inf),
	//\ffreq, ~con,
	//\ffreq, Pfunc({ ~la.get_range_state(6).debug("rs") }) * 300 + ~con, 
	\ffreq, Pfunc({ ~la.get_range_state(6).debug("rs") }) * 300 + 100, 
	\octave, 4,
	\dur, 1/4,
	\amp, 0.1
) <> ~trans).play;
Pdef(\plop3, Pbind(
	\instrument, \default,
	\degree, Pseq([4],inf) * Pn(Plazy({ ~la.pseqrow(2) }), inf),
	\octave, 3,
	\dur, 1/4,
	\amp, 0.1
) <> ~trans).play;

Pdef()
);	
