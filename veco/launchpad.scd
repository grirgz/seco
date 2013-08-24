
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
				//[x, y, val].debug("state_leds");
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
		//[val,self.state_buffers[x,y], pos].debug("state_to_color: play, buf, pos");
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
		self.current_cc_on = midinote;
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
		self.current_cc_on = nil;
		res = switch(midinote,
			self.buttons[5], {
				"scene SEQ!!".debug;
				self.set_cc_led(self.buttons[5], \yellow);
				self.set_cc_led(self.buttons[6], \off);
				self.set_cc_led(self.buttons[7], \off);
				self.sceneset.seq;
			},
			self.buttons[6], {
				self.set_cc_led(self.buttons[5], \off);
				self.set_cc_led(self.buttons[6], \yellow);
				self.set_cc_led(self.buttons[7], \off);
				self.sceneset.subbuffer;
			},
			self.buttons[7], {
				self.set_cc_led(self.buttons[5], \off);
				self.set_cc_led(self.buttons[6], \off);
				self.set_cc_led(self.buttons[7], \yellow);
				self;
			},
			{

			}
		);
		if(res.notNil) { res.activate };
	},

	response_launcher_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var ledpos;
		var res;
		var tilex, tiley, tile;
		tilex = pos.y;
		tiley = self.active_row;
		tile = self.gridpos_to_tile_index(Point(tilex, tiley));
		[midinote, pos, tilex, tiley, tile, self.current_cc_on, self.state_edit].debug("[midinote, pos, tilex, tiley, tile, self.current_cc_on, self.state_edit]");
		if(self.current_cc_on == self.buttons[6]) {
			self.sceneset.subbuffer.set_active_row(pos.y);
			self.sceneset.subbuffer.activate;
		} {
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
		}
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

			self.sceneset.tile.response_launcher_on(midinote);
			//if(self.state_edit == 1) {
			//	~veco.vim_open_buffer(tile);
			//	tile.debug("response_on: tile");
			//} {
			//	ledpos = Point(pos.y, self.active_row);
			//	res = ~veco.toggle_play_node_by_index(tile);
			//	[pos,ledpos,res].debug("res:::!!!! pos,ledpos,res");
			//	if(res == true) {
			//		self.state_play[ledpos.x,ledpos.y] = 1;
			//		self.set_led(midinote, self.state_to_color(ledpos));
			//	} {
			//		self.state_play[ledpos.x,ledpos.y] = 0;
			//		self.set_led(midinote, self.state_to_color(ledpos));
			//	};
			//}
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

~launchpad_scene_seq_old = (
	parent: ~launchpad_scene,

	//state: Array2D.new(8,8),
	color_on: \red,
	color_off: \off,
	range_state: Array.fill(8, 0),
	ranges: [6,7],

	state_buffers: { arg self; ~veco.get_buffer_presence },

	seq_state: { arg self; self.sceneset.state_seq }, 
	reset_seq_state: { arg self; self.sceneset.state_seq = Array2D.new(8,8) },

	get_range_state: { arg self, index;
		self.range_state[index];
	},


	set_range_state: { arg self, index, val;
		self.range_state[index] = val;
	},

	render_state: { arg self;
		var active_row = self.active_row;
		self.seq_state.rowsDo { arg column, x;
			column.do { arg val, y;
				[x, y, val].debug("launchpad_scene_seq: render_state: seq_state");
				self.state[x,y] = if(val == 1) { self.color_on } { \off };
			}
		};
		self.state_buffers.rowAt(active_row).do { arg val, x;
			self.state[8, x] = self.sceneset.tile.state_to_color(Point(x, active_row));
		};
	},

	response_on: { arg self, midinote;
		var pos;
		var color;
		"resp: on".debug;
		pos = self.midi_to_gridpos(midinote);
		if(self.is_launcher_button(midinote)) {
			self.sceneset.tile.response_launcher_on(midinote);
		} {
			self.toggle_cell(midinote, pos);
		};
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

	pryn: { arg self, name, repeat=inf;
		var idx = ~veco.name_to_index(name);
		[name, idx].debug("pryn");
		idx = idx % 8;
		Pn(Plazy({
			self.pseqrow(idx)
		}),repeat);
	},

	response_off: { arg self;
		
	},
);

//////////////////

~launchpad = (
	new: { arg self, controller;
		self = self.deepCopy;
	
		debug("NEW class_launchpad");
		self.midiout = MIDIOut(0);

		self.init_midi_layout;
		self.controller = { controller };
		
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
			dimgreen: 47,
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
		var res = self.gridlayout[pos.x][pos.y];
		//res.debug("midipos");
		res;
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

	active_row: { arg self;
		self.sceneset.active_row
	},

	set_active_row: { arg self, idx;
		idx.debug("launchpad_scene_subbuffer: active_row");
		self.sceneset.active_row = idx;
	},

	gridpos_to_tile_index: { arg self, pos;
		pos = pos.x + (pos.y * 8);
		pos;
	},

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
		//self.state.rowsDo { arg column, x;
		//	column.keep(9).do { arg color, y;
		//		//[color,x,y].debug("update_display: COLOR");
		//		if(color.isNil) {
		//			color = 0;
		//		};
		//		//[color,x,y].debug("update_display: COLOR2");
		//		color = self.color_to_velocity(color);
		//		midi = self.gridpos_to_midi(Point(x,y));
		//		//[midi,color].debug("update_display: midi, color");
		//		if(midi.notNil) {
		//			self.midiout.noteOn(0, midi, color);
		//		}
		//	}
		//};
		//self.sceneset.state_mode_leds.do { arg val, x;
		//	self.set_cc_led(x, val)
		//};
	},

	render_mode_state: { arg self;
		//self.sceneset.state_mode_leds.debug("render_mode_state");
		//self.sceneset.debug("render_mode_state");
		//self.sceneset.state_mode_leds[4] = if(self.sceneset.state_edit == 1) { self.color_mode_on } { self.color_mode_off };
		//self.sceneset.state_mode_leds[5] = if(self.sceneset.active_scene === self.sceneset.seq) { self.color_mode_on } { self.color_mode_off };
		//self.sceneset.state_mode_leds[6] = if(self.sceneset.active_scene === self.sceneset.subbuffer) { self.color_mode_on } { self.color_mode_off };
		//self.sceneset.state_mode_leds[7] = if(self.sceneset.active_scene === self.sceneset.tile) { self.color_mode_on } { self.color_mode_off };
		//self.sceneset.state_mode_leds.debug("end render_mode_state");
		var on = switch(self.controller.mode,
			\clip, {
				4 
			},
			\seq, {
				5 
			},
			\edit, {
				6
			},
			{

			}
		);
		8.do { arg x;
			self.set_cc_led(x, if(x == on) { self.color_mode_on } { self.color_mode_off })
		}
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

	gridpos_to_buf: { arg self, pos;
		var node = self.controller.get_node_by_index(pos.x);
		var res;
		if(node.notNil) {
			res = node.get_clip_by_index(pos.y, false);
		};
		res;
	},

	gridpos_to_node_and_clip: { arg self, pos;
		var node = self.controller.get_node_by_index(pos.x);
		var clip;
		if(node.notNil) {
			clip = node.get_clip_by_index(pos.y);
		};
		[node, clip];
	},
);

//////////////////

~launchpad_scene_clip = (
	parent: ~launchpad_scene,

	//state_to_color: ~launchpad_scene_tile[\state_to_color],

	render_state: { arg self;
		////~veco.geupdate_buffer_presence;
		//~veco.update_buffer_presence;
		//self.state_subbuffers.rowsDo { arg column, x;
		//	column.do { arg val, y;
		//		[x, y, val].debug("launchpad_scene_subbuffer: render_state: subbuffer_state");
		//		self.state[x,y] = if(val == 1) { self.color_on } { self.color_off };
		//	}
		//};
		//self.state_buffers.rowAt(self.active_row).do { arg val, x;
		//	self.state[8, x] = self.state_to_color(Point(x, self.active_row));
		//}
		"launchpad_scene_clip.render_state".debug;
		8.do { arg x;
			8.do { arg y;
				self.render_pos(Point(x,y));
			}
		};
	},

	render_pos: { arg self, pos;
		var buf = self.gridpos_to_buf(pos);
		var color_off = \off,
			color_present = \dimred,
			color_active = \dimgreen,
			color_playing = \green;
		var color;
		if(buf.isNil) {
			color = color_off;
			[pos].debug("launchpad_scene_clip: render_pos: buf is nil");
		} {


			[buf.uname, buf.path, buf.is_present, buf.is_playing, buf.is_active].debug("launchpad_scene_clip: render_pos");

			color = if(buf.is_present == true) {
				if(buf.is_active == true) {
					if(buf.is_playing == true) {
						color_playing;
					} {
						color_active;
					}
				} {
					color_present;
				}
			} {
				color_off;
			};

			self.set_led(pos, color);
		}

	},

	response_cc_on: { arg self, midinote;
		var res;
		midinote.debug("MIDINOTE CoNTROL");
		self.current_cc_on = midinote;
		res =switch(midinote,
			self.buttons[4], {
				self.render_state;
				nil;
			},
			{
				nil;
			}
		);
		if(res.notNil) { res.activate };
	},

	response_cc_off: { arg self, midinote;
		var res;
		midinote.debug("MIDINOTE CoNTROL");
		self.current_cc_on = midinote;
		res =switch(midinote,
			self.buttons[4], {
				self.controller.mode = \clip;
				self.sceneset.clip;
			},
			self.buttons[5], {
				self.controller.mode = \seq;
				self.sceneset.seq;
			},
			self.buttons[6], {
				self.controller.mode = \edit;
				self.sceneset.edit;
			},
			{

			}
		);
		if(res.notNil) { res.activate };
		self.render_mode_state;
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_clip.response_on");

		if(self.is_launcher_button(midinote)) {
			midinote.debug("launchpad_scene_clip.response_on: is a launcher");
			64.do { arg x;
				#node, clip = self.gridpos_to_node_and_clip(Point(x, pos.y));
				if(clip.notNil and: {clip.is_present}) {
					clip.play_node;
				}
			}

		} {
			midinote.debug("launchpad_scene_clip.response_on: go!");
			#node, clip = self.gridpos_to_node_and_clip(pos);
			if(clip.is_present) {
				clip.play_node;
			} {
				node.stop_node;
			};
			self.render_pos(pos);
		}
	},

);

~launchpad_scene_seq = (
	parent: ~launchpad_scene,
	color_on: \red,
	color_off: \off,
	color_disabled: \dimred,

	active_buffers: { arg self, x;
		if(x.isNil) {
			8.collect { arg x;
				var point = self.controller.active_buffer_points[x];
				self.gridpos_to_buf(point);
			}
		} {
			var point = self.controller.active_buffer_points[x];
			self.gridpos_to_buf(point);
		}
	},

	render_state: { arg self;
		debug("launchpad_scene_seq.render_state");
		8.do { arg x;
			self.render_line(x)
		}
	},

	render_line: { arg self, y;
		if(self.active_buffers(y).stepseq.isNil) {
			8.do { arg val, x;
				self.set_led(Point(x,y), self.color_disabled)
			}
		} {
			self.active_buffers(y).stepseq.do { arg val, x;
				self.set_led(Point(x,y), if(val == 1) { self.color_on } { self.color_off })
			}
		}
	},

	render_pos: { arg self, pos;
		var val;
		self.active_buffers(pos.y).debug("render_pos: active_buffers");
		val = self.active_buffers(pos.y).stepseq[pos.x];
		self.set_led(pos, if(val == 1) { self.color_on } { self.color_off })
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	launcher_pos_to_node: { arg self, pos;
		var nodeidx = pos.y;
		self.controller.get_node_by_index(nodeidx);
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_seq.response_on");

		if(self.is_launcher_button(midinote)) {
			//node = self.launcher_pos_to_node(pos);
			//if(node.notNil) {
			//	node.open_in_vim;
			//};
			//self.render_pos(pos);
		} {
			pos.debug("pos");
			self.active_buffers(pos.y).debug("QUOUOUUO");
			self.active_buffers(pos.y).stepseq[pos.x] = if(self.active_buffers(pos.y).stepseq[pos.x] == 1) { 0 } { 1 };
			self.active_buffers(pos.y).stepseq[pos.x].debug("val");
			self.render_pos(pos);
		}
	},

);

~launchpad_scene_edit = (
	parent: ~launchpad_scene,

	render_state: { arg self;
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	launcher_pos_to_node: { arg self, pos;
		var nodeidx = pos.y;
		self.controller.get_node_by_index(nodeidx);
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_clip.response_on");

		if(self.is_launcher_button(midinote)) {
			node = self.launcher_pos_to_node(pos);
			if(node.notNil) {
				node.open_in_vim;
			};
			//self.render_pos(pos);
		} {
			midinote.debug("launchpad_scene_clip.response_on: go!");
			#node, clip = self.gridpos_to_node_and_clip(pos);
			if(clip.notNil) {
				clip.open_in_vim;
			};
			self.render_pos(pos);
		}
	},

);
