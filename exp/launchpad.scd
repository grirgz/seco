
MIDIdef

MIDIOut.noteOn(1, 60, 64)

m = MIDIOut(0);

m.noteOn(0, 110, 60);
m.control(0, 110, 60);
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


(

~launchpad = (
	new: { arg self;
		self = self.deepCopy;
	
		self.midiout = MIDIOut(0);

		self.init_midi_layout;
		
		CmdPeriod.add({  self.sceneset.cleanup_function });
	
		self;
	},

	sceneset: { ~sceneset },

	state: { arg self;
		self.sceneset.state_leds;
	},

	buttons: [0,1,2,3,4,5,6,7] + 104,

	is_launcher_button: { arg self, midinote;
		midinote % 16 == 8
	},

	colors: (
			red: 15,
			dimred: 0x0d,
			green: 60,
			yellow: 62,
			orange: 47,
			amber: 63,
			off: 0,
	),

	init_midi_layout: { arg self;
		
		self.gridlayout = 9.collect { arg x;
			8.collect { arg y;
				[x,y].debug("x,y");
				x+(y*16).debug("res");
			}
		};
		self.gridlayout.debug("gridlayout");
	},

	color_to_velocity: { arg self, color=63;
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

	set_cc_led: { arg self, pos, color;
		if(pos < 50) {
			pos = pos + 104;
		};
		color = self.color_to_velocity(color);
		self.midiout.control(0, pos, color)
	},

	set_led: { arg self, pos, color;
		var midi;
		[pos,color].debug("set_led: pos, color");
		if(pos.class == Point) {
			midi = self.gridpos_to_midi(pos);
		} {
			midi = pos;
			pos = self.midi_to_gridpos(pos);
		};
		[midi, pos,color].debug("set_led: midi, pos, color 2");
		self.state[pos.x,pos.y] = color;
		[pos,color].debug("set_led: pos, color 3");
		color = self.color_to_velocity(color);
		[0,midi,color].debug("send noteOn");
		self.midiout.noteOn(0, midi, color);

	},
);

~launchpad_scene = (
	parent: ~launchpad,
	color_mode_on: \yellow,
	color_mode_off: \off,

	install_midi_responder: { arg self;
		MIDIdef.noteOn(\launchpadOn, { arg vel, midinote;
			midinote.debug("midinote: noteOn responder");
			self.response_on(midinote);
		}).permanent_(true);
		MIDIdef.noteOff(\launchpadOn, { arg vel, midinote;
			midinote.debug("midinote: noteOn responder");
			self.response_on(midinote);
		}).permanent_(true);
		MIDIdef.cc(\launchpadCC, { arg vel, midinote;
			if(vel == 127) {
				midinote.debug("midinote: ccOn responder");
				self.response_cc_on(midinote);
			} {
				midinote.debug("midinote: ccOff responder");
				self.response_cc_off(midinote);
			}
		}).permanent_(true);
	},

	update_display: { arg self;
		var midi;
		self.render_state;
		self.render_mode_state;
		self.state.rowsDo { arg column, x;
			column.keep(9).do { arg color, y;
				[color,x,y].debug("update_display: COLOR");
				if(color.isNil) {
					color = 0;
				};
				[color,x,y].debug("update_display: COLOR2");
				color = self.color_to_velocity(color);
				midi = self.gridpos_to_midi(Point(x,y));
				[midi,color].debug("update_display: midi, color");
				if(midi.notNil) {
					self.midiout.noteOn(0, midi, color);
				}
			}
		};
		self.sceneset.state_mode_leds.do { arg val, x;
			self.set_cc_led(x, val)
		};
	},

	render_mode_state: { arg self;
		self.sceneset.state_mode_leds.debug("render_mode_state");
		self.sceneset.debug("render_mode_state");
		self.sceneset.state_mode_leds[4] = if(self.sceneset.state_edit == 1) { self.color_mode_on } { self.color_mode_off };
		self.sceneset.state_mode_leds[5] = if(self.sceneset.active_scene === self.sceneset.seq) { self.color_mode_on } { self.color_mode_off };
		self.sceneset.state_mode_leds[6] = if(self.sceneset.active_scene === self.sceneset.tile) { self.color_mode_on } { self.color_mode_off };
		self.sceneset.state_mode_leds.debug("end render_mode_state");
	},

	activate: { arg self, update=true;
		"ACTIVATION".debug;
		self.install_midi_responder;
		self.sceneset.active_scene = self;
		if(update) {
			self.update_display;
		}
	},

	deactivate: { arg self;
		// TODO
	},

	response_on: { arg self;
		
	},

	response_off: { arg self;
		
	},

	response_cc_on: { arg self, midi;
		self.sceneset.tile.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.tile.response_cc_off(midi);
	},
);

~launchpad_scene_tile = (
	parent: ~launchpad_scene,

	//state: Array2D.new(8,8),
	color_play: \green,
	color_off: \off,
	color_buffer: \dimred,
	color_buffer_play: 0x3d,
	range_state: Array.fill(8, 0),
	ranges: [6,7],
	state_edit: { arg self; self.sceneset.state_edit },

	state_play: { arg self; self.sceneset.state_play },
	//state_buffers: { arg self; self.scenestate.state_buffers },
	state_buffers: { arg self; ~veco.get_buffer_presence },

	reset_play_state: { arg self; 
		self.sceneset.state_play = Array2D.new(8,8);
		~veco.counter = IdentityDictionary.new;
		self.update_display;
	},
	
	render_state: { arg self;
		var cleanup;
		~veco.update_buffer_presence;
		self.state_play.rowsDo { arg column, x;
			column.do { arg val, y;
				[x, y, val].debug("state_leds");
				self.state[x,y] = self.state_to_color(Point(x,y))
			}
		};
		8.do { arg y;
			self.state[8,y] = self.color_off;
		};
	},



	state_to_color: { arg self, pos;
		var x = pos.x;
		var y = pos.y;
		var val = self.state_play[x,y];
		[val,self.state_buffers[x,y], pos].debug("state_to_color: play, buf, pos");
		if(val == 1) { 
			if(self.state_buffers[x,y] == 1) {
				self.color_buffer_play
			} {
				self.color_play

			}
		} { 
			if(self.state_buffers[x,y] == 1) {
				self.color_buffer
			} {
				self.color_off
			}
		};
	},

	midinote_to_tile_index: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		pos = pos.x + (pos.y * 8);
		pos;
	},

	response_cc_on: { arg self, midinote;
		var res;
		midinote.debug("MIDINOTE CoNTROL");
		res =switch(midinote,
			self.buttons[4], {
				if(self.state_edit == 1) {
					self.set_cc_led(self.buttons[4], \off);
					self.sceneset.state_edit = 0;
					//self;
					nil;
				} {

					self.set_cc_led(self.buttons[4], \yellow);
					self.sceneset.state_edit = 1;
					//self;
					nil;
				}
			},
			{

			}
		);
		if(res.notNil) { res.activate };
	},

	response_cc_off: { arg self, midinote;
		var res;
		midinote.debug("MIDINOTE CoNTROL off");
		res = switch(midinote,
			self.buttons[5], {
				"scene SEQ!!".debug;
				self.set_cc_led(self.buttons[5], \yellow);
				self.set_cc_led(self.buttons[6], \off);
				self.sceneset.seq;
			},
			self.buttons[6], {
				self.set_cc_led(self.buttons[5], \off);
				self.set_cc_led(self.buttons[6], \yellow);
				self;
			},
			{

			}
		);
		if(res.notNil) { res.activate };
	},

	response_on: { arg self, midinote;
		var tile = self.midinote_to_tile_index(midinote);
		var pos = self.midi_to_gridpos(midinote);
		var res;
		midinote.debug("tile: response_on: midinote");
		if(self.is_launcher_button(midinote)) {

			midinote.debug("tile: response_on: launcher!!!");
			self.sceneset.subbuffer.set_active_row(pos.y);
			self.sceneset.subbuffer.activate;

		} {

			if(self.state_edit == 1) {
				~veco.vim_open_buffer(tile);
				tile.debug("response_on: tile");
			} {
				res = ~veco.toggle_play_node_by_index(tile);
				res.debug("res:::!!!!");
				if(res == true) {
					self.state_play[pos.x,pos.y] = 1;
					self.set_led(midinote, self.state_to_color(pos));
				} {
					self.state_play[pos.x,pos.y] = 0;
					self.set_led(midinote, self.state_to_color(pos));
				};
			}
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

~launchpad_scene_subbuffer = (
	parent: ~launchpad_scene,
	color_on: \red,
	color_off: \off,
	color_play: \green,
	color_buffer: \dimred,
	color_buffer_play: 0x3d,

	state_subbuffers: { arg self; ~veco.get_subbuffer_presence(self.active_row) },
	state_buffers: { arg self; ~veco.get_buffer_presence },
	state_play: { arg self; self.sceneset.state_play },
	state_edit: { arg self; self.sceneset.state_edit },

	state_to_color: ~launchpad_scene_tile[\state_to_color],

	render_state: { arg self;
		//~veco.geupdate_buffer_presence;
		~veco.update_buffer_presence;
		self.state_subbuffers.rowsDo { arg column, x;
			column.do { arg val, y;
				[x, y, val].debug("launchpad_scene_subbuffer: render_state: subbuffer_state");
				self.state[x,y] = if(val == 1) { self.color_on } { self.color_off };
			}
		};
		self.state_buffers.rowAt(self.active_row).do { arg val, x;
			self.state[8, x] = self.state_to_color(Point(x, self.active_row));
		}
	},


	set_active_row: { arg self, idx;
		idx.debug("launchpad_scene_subbuffer: active_row");
		self.active_row = idx;
	},

	gridpos_to_tile_index: { arg self, pos;
		pos = pos.x + (pos.y * 8);
		pos;
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var ledpos;
		var res;
		var tilex, tiley, tile;
		tilex = pos.y;
		tiley = self.active_row;
		tile = self.gridpos_to_tile_index(Point(tilex, tiley));
		self.state_edit.debug("launchpad_scene_subbuffer: response_on: state_edit. BODREL");
		if(self.is_launcher_button(midinote)) {

			if(self.state_edit == 1) {
				~veco.vim_open_buffer(tile);
				tile.debug("response_on: tile");
			} {
				ledpos = Point(pos.y, self.active_row);
				res = ~veco.toggle_play_node_by_index(tile);
				[pos,ledpos,res].debug("res:::!!!! pos,ledpos,res");
				if(res == true) {
					self.state_play[ledpos.x,ledpos.y] = 1;
					self.set_led(midinote, self.state_to_color(ledpos));
				} {
					self.state_play[ledpos.x,ledpos.y] = 0;
					self.set_led(midinote, self.state_to_color(ledpos));
				};
			}
		} {
			if(self.state_edit == 1) {
				~veco.vim_open_subbuffer(tile, pos.x+1);
				[tile,pos].debug("response_on: open subbuffer: tile, pos");
			} {
				~veco.exec_subbuffer(tile, pos.x+1);
				pos.debug("response_on: exec subbuffer: pos");
			}
		}
	},

);

~launchpad_scene_seq = (
	parent: ~launchpad_scene,

	//state: Array2D.new(8,8),
	color_on: \red,
	color_off: \off,
	range_state: Array.fill(8, 0),
	ranges: [6,7],

	seq_state: { arg self; self.sceneset.state_seq }, 
	reset_seq_state: { arg self; self.sceneset.state_seq = Array2D.new(8,8) },

	get_range_state: { arg self, index;
		self.range_state[index];
	},


	set_range_state: { arg self, index, val;
		self.range_state[index] = val;
	},

	render_state: { arg self;
		self.seq_state.rowsDo { arg column, x;
			column.do { arg val, y;
				[x, y, val].debug("launchpad_scene_seq: render_state: seq_state");
				self.state[x,y] = if(val == 1) { self.color_on } { \off };
			}
		};
	},

	response_on: { arg self, midinote;
		var pos;
		var color;
		"resp: on".debug;
		pos = self.midi_to_gridpos(midinote);
		self.toggle_cell(midinote, pos);
	},

	toggle_cell: { arg self, midi, pos;
		var color;
		color = self.color_on;
		if(self.seq_state[pos.x,pos.y] == 1) {
			self.seq_state[pos.x,pos.y] = 0;
			self.set_led(midi, \off);
		} {
			self.seq_state[pos.x,pos.y] = 1;
			self.set_led(midi, color);
		}
	},

	pseqrow: { arg self, y, repeat=1;
		Pseq(8.collect { arg x;
			if(self.seq_state[x, y] == 1) {
				false
			} {
				true
			}
		},repeat)
	},

	prythm: { arg self, y, repeat=inf;
		Pn(Plazy({
			self.pseqrow(y)
		}),repeat);
	},

	response_off: { arg self;
		
	},
);

~sceneset = (
	tile: ~launchpad_scene_tile.new,
	subbuffer: ~launchpad_scene_subbuffer.new,
	seq: ~launchpad_scene_seq.new,
).putAll(~scenestate);
~sceneset.seq.activate;
//~sceneset.tile.set_cc_led(2, \yellow);
//~sceneset.tile.set_led(1@1, \yellow);

)

(

~scenestate = (
	state_edit: 0,
	state_seq: Array2D.new(8,8),
	state_leds: Array2D.new(9,8),
	state_mode_leds: Array.fill(8, \off),
	state_play: Array2D.new(8,8),
	state_buffers: Array2D.new(8,8),
	state_subbuffers: Array2D.new(8,8),
	cleanup_function: { 
		~sceneset.tile.reset_play_state 
	}
);

)

~sceneset.state_mode_leds

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

P9tempo


// "a" is an array-of-arrays
a = { { 100.0.rand  }.dup(100)  }.dup(100);
// "b" is an equivalent Array2D, made using the "fromArray" class method
b = Array2D.fromArray(100,100, a.flat);

// Accessing
a[15][22]
b[15, 22]



// Speed comparison 1: random access
bench { 1000.do(a[100.rand][100.rand]) }
bench { 1000.do(b[100.rand, 100.rand]) }

// Speed comparison 2: iteration
bench { 1000.do(a.do { |row| row.do { |item| item * 2 } }) }
bench { 1000.do(b.do { |item| item * 2 }) }

a = Array2D.new(2,4)
a[1,3] = 1
Array2D.new(1,3)
