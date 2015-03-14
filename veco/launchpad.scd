
//~launchpad_device_id = 1572864;
//~launchpad_device_id = 1835008;

//~get_device_uid = { arg string1, string2;
//	var dev = MIDIIn.findPort (string1, string2);
//	if(dev.notNil) {
//		dev.uid;
//	} {
//		nil
//	}
//};
//
//~launchpad_device_id = ~get_device_uid.("Launchpad S-Launchpad S MIDI 1", "Launchpad S-Launchpad S MIDI 1");
//~icon_device_id = ~get_device_uid.("iCON iControls V2.00-iCON iControls V2.00 MIDI 1", "iCON iControls V2.00-iCON iControls V2.00 MIDI 1");
//
//if(~launchpad_device_id.notNil) {
//	MIDIOut(0).connect( MIDIClient.destinations.detectIndex { arg x; x.device == "Launchpad S-Launchpad S MIDI 1" } );
//	MIDIIn.connect(0, ~launchpad_device_id);
//};
//if(~icon_device_id.notNil) {
//	MIDIOut(0).connect( MIDIClient.destinations.detectIndex { arg x; x.device == "iCON iControls V2.00-iCON iControls V2.00 MIDI 1" } );
//	MIDIIn.connect(0, ~icon_device_id);
//};


//MIDIClient.destinations;
//MIDIdef.noteOn(\test, { arg ... args; args.debug("plop note"); });
//MIDIdef.cc(\testcc, { arg ... args; args.debug("plop cc"); });

~launchpad = (
	new: { arg self, controller;
		self = self.deepCopy;
	
		self.modifier_names = #[up, down, left, right, session, user1, user2, mixer];
		self.modifiers = ();
		debug("NEW class_launchpad");
		self.midiout = MIDIOut(0);

		self.init_midi_layout;
		self.controller = { controller.value };
		self.gridsize = Point(8,8);
		
		CmdPeriod.add({  self.sceneset.cleanup_function });
	
		self;
	},

	sceneset: { ~veco },

	state: { arg self;
		self.sceneset.state_leds;
	},

	buttons: [0,1,2,3,4,5,6,7] + 104,
	mode_buttons: [0,1,2,3,4,5,6,7] + 104,
	launcher_buttons: [0,1,2,3,4,5,6,7] * 16 + 8,

	is_launcher_button: { arg self, midinote;
		midinote % 16 == 8
	},

	colors: (
			red: 0x0f,
			red2: 0x0e,
			dimred: 0x0d,
			green: 0x3c,
			green2: 0x2c,
			dimgreen: 0x1c,
			yellow: 0x3e,
			orange: 0x2f,
			amber: 0x3f,
			off: 0,
	),

	init_midi_layout: { arg self;
		
		// only includes launch buttons (last column)
		self.gridlayout = 9.collect { arg x;
			8.collect { arg y;
				//[x,y].debug("x,y");
				//x+(y*16).debug("res");
				x+(y*16)
			}
		};
		//self.gridlayout.debug("gridlayout");
		self.gridlayout;
	},

	color_to_velocity: { arg self, color=63;
		if(color.isNumber) {
			color
		} {
			self.colors[color] ?? 63
		}
		
	},

	gridpos_to_midi: { arg self, pos;
		var res;
		res = self.gridlayout[pos.x][pos.y];
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
		// launch buttons (last column)
		~launchpad_gui.set_cc_led(pos, color);
		if(pos < 50) {
			pos = pos + 104;
		};
		color = self.color_to_velocity(color);
		self.midiout.control(0, pos, color)
	},

	set_mode_led: { arg self, pos, color;
		~launchpad_gui.set_mode_led(pos, color);
		self.set_cc_led(pos, color)
	},

	set_led: { arg self, pos, color;
		var midi;
		//[pos,color].debug("set_led: pos, color");
		if(pos.class == Point) {
			midi = self.gridpos_to_midi(pos);
		} {
			midi = pos;
			pos = self.midi_to_gridpos(pos);
		};
		~launchpad_gui.set_led(pos, color);
		//[midi, pos,color].debug("set_led: midi, pos, color 2");
		self.state[pos.x,pos.y] = color;
		//[pos,color].debug("set_led: pos, color 3");
		color = self.color_to_velocity(color);
		//[0,midi,color].debug("send noteOn");
		self.midiout.noteOn(0, midi, color);

	},
);

~launchpad_scene = (
	parent: ~launchpad,
	color_mode_on: \yellow,
	color_mode_off: \off,

	mode_name: \not_initialized,

	active_row: { arg self;
		// seems deprecated
		self.sceneset.active_row
	},

	set_active_row: { arg self, idx;
		// seems deprecated
		idx.debug("launchpad_scene_subbuffer: active_row");
		self.sceneset.active_row = idx;
	},


	line_do: { arg self, fun;
		// board_offset is the first index of current slice
		self.controller.board_size.x.do { arg x, n;
			x = x + self.controller.board_offset.x;
			fun.(x, n);
		}
	},

	slices_do: { arg self, fun;
		var slice_is_active;
		var board_offset = self.controller.board_offset.x;
		var in_stop_exclusion_range = self.controller[\in_stop_exclusion_range];
		var is_excluded_fun = { arg x;

			(x >= board_offset and: { x < (board_offset + self.controller.board_size.x) }) or: {
				if(in_stop_exclusion_range.notNil) {
					in_stop_exclusion_range.(x)
				} {
					false;
				}
			}

		};
		var is_excluded;
		self.controller.msize.x.do { arg x;
			if( is_excluded_fun.(x) ) 
			{
				is_excluded = true;
			} {
				is_excluded = false;
			};
			fun.(x, is_excluded);
		}
		
	},

	column_do: { arg self, fun;
		self.controller.board_size.y.do { arg y;
			y = y + self.controller.board_offset.y;
			fun.(y);
		}
	},


	////////////////////////// coordinates and clips

	///////////////

	// - gridpos_to_clip
	// - abspos_to_clip // not used
	// - gridpos_to_abspos
	// - abspos_to_gridpos

	//gridpos_to_node_and_clip: { arg self, pos;
	//	var node;
	//	var clip;
	//	pos = self.controller.clip_offset + pos;
	//	node = self.controller.get_node_by_index(pos.x);
	//	if(node.notNil) {
	//		clip = node.get_clip_by_index(pos.y);
	//	};
	//	[node, clip];
	//},

	get_node_by_index: { arg self, idx;
		self.controller.get_node_by_index(idx);
	},

	gridpos_to_abspos: { arg self, gpos;
		var pos;
		if(self.controller.pane_rect.notNil and: { self.controller.pane_rect.contains(gpos) }) {
			pos = self.controller.pane_offset + gpos - self.controller.pane_rect.leftTop;
		} {
			pos = self.controller.clip_offset + gpos;
		};
		pos;
	},

	abspos_to_node_and_clip: { arg self, pos;
		var node;
		var res;
		node = self.get_node_by_index(pos.x);
		if(node.notNil) {
			res = node.get_clip_by_index(pos.y, false);
		};
		[node, res]
	},

	abspos_to_buf: { arg self, pos;
		self.abspos_to_node_and_clip(pos)[1]
	},

	gridpos_to_node_and_clip: { arg self, gpos;
		self.abspos_to_node_and_clip(self.gridpos_to_abspos(gpos));
	},


	absolute_point_to_buf: { arg self, pos;
		self.abspos_to_buf(pos);
	},

	gridpos_to_buf: { arg self, pos;
		var node;
		var res;
		self.abspos_to_buf(self.gridpos_to_abspos(pos));
	},


	abspos_to_gridpos: { arg self, abspos, inrect=true;
		// FIXME: this doesnt handle pane_rect, cause display bugs like copy mode higlighting the wrong cell
		var pos;
		var rect = Rect(0,0,8,8);

		[abspos , self.controller.clip_offset].debug("abspos - self.controller.clip_offset");
		pos = abspos - self.controller.clip_offset;
		[self.controller.clip_offset, abspos, pos, rect, rect.contains(pos)].debug("abspos_to_gridpos: offset, abs, pos, rect, contains");
		if(rect.contains(pos) or: {inrect != true}) {
			pos
		} {
			nil
		}
	},

	is_abspos_visible: { arg self, abspos;
		// FIXME: doesnt handle pane_rect
		var gridpos;
		var os = self.controller.clip_offset;
		abspos.x.inclusivelyBetween(os.x, os.x+7) and: {
			abspos.y.inclusivelyBetween(os.y, os.y+7);
		};
	},

	midi_to_offset_gridpos: { arg self, midi;
		self.midi_to_gridpos(midi) + self.controller.clip_offset;
	},

	offset_pos: { arg self, pos;
		pos + self.controller.clip_offset
	},

	launcher_pos_to_node: { arg self, pos;
		var nodeidx;
		nodeidx = pos.y + self.controller.clip_offset.x;
		self.controller.get_node_by_index(nodeidx);
	},

	////////////////////////// midi

	install_midi_responder: { arg self;
		var launchpad_channel = 0;
		var launchpad_id = ~launchpad_device_id;
		~launchpad_gui_current_scene = self;
		MIDIdef.noteOn(\launchpadOn, { arg vel, midinote;
			[self.mode_name, midinote].debug("midinote: noteOn responder");
			self.response_on(midinote);
		}, nil, launchpad_channel, launchpad_id).permanent_(true);
		MIDIdef.noteOff(\launchpadOff, { arg vel, midinote;
			[self.mode_name,midinote].debug("midinote: noteOFF responder");
			self.response_off(midinote);
		}, nil, launchpad_channel, launchpad_id).permanent_(true);
		MIDIdef.cc(\launchpadCC, { arg vel, midinote;
			if(vel == 127) {
				[self.mode_name,midinote].debug("midinote: ccOn responder");
				self.handle_modifiers_on(midinote);
				self.response_cc_on(midinote);
			} {
				[self.mode_name,midinote].debug("midinote: ccOff responder");
				self.handle_modifiers_off(midinote);
				self.response_cc_off(midinote);
			}
		}, nil, launchpad_channel, launchpad_id).permanent_(true);
	},

	update_display: { arg self;
		var midi;
		"updatedisp".debug;
		self.render_state;
		"updatedispmodest".debug;
		self.render_mode_state;
		"uFINpdatedispmodest".debug;
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
			\copy, {
				0
			},
			\paste, {
				1
			},
			\cut, {
				2
			},
			\link, {
				3
			},
			\clip, {
				4 
			},
			\seq, {
				5 
			},
			\edit, {
				6
			},
			\mixer, {
				7
			},
			\mixer_mod, {
				// Maybe must show another color to be more clear
				7
			},
			{
				self.controller.mode.debug("render_mode_state: unknown mode");

			}
		);
		8.do { arg x;
			self.set_cc_led(x, if(x == on) { self.color_mode_on } { self.color_mode_off })
		}
	},

	clip_point: { arg self, point, rect;
		point = point.copy;
		point.x = point.x.clip(rect.left, rect.right);
		point.y = point.y.clip(rect.top, rect.bottom);
		point;
	},

	handle_modifiers_on: { arg self, midi;
		var idx = midi - 104;
		var modname = self.modifier_names[idx];
		self.modifiers[modname] = true;
	},

	handle_modifiers_off: { arg self, midi;
		var idx = midi - 104;
		var modname = self.modifier_names[idx];
		self.modifiers[modname] = false;
	},

	handle_grid_move: { arg self, midinote;
		var os = self.controller.clip_offset;
		var res;
		var count = 1;
		var matrix = Rect(0,0,64,64);
		var rosae = (
			up: Point(0,-1),
			down: Point(0,1),
			left: Point(-1,0),
			right: Point(1,0),
		);
		var move = { arg dir;
			self.controller.clip_offset = self.clip_point(os + (rosae[dir]*count), matrix);
			self.render_state;
			true;
		};
		if(self.modifiers[\session] == true) {
			count = 8;
		};
		res = switch(midinote,
			self.buttons[0], { // up
				move.(\up);
			},
			self.buttons[1], { // down
				move.(\down);
			},
			self.buttons[2], { // left
				move.(\left);
			},
			self.buttons[3], { // right
				move.(\right);
			}, {
				false
			}
		);
		if(res == true and: { self.modifiers[\session] == true }) {
			self.has_moved = true;
		} {
			self.has_moved = false;
		};
	},

	activate: { arg self, update=true;
		[self.mode_name, "ACTIVATION"].debug;
		if(self.sceneset.active_scene.notNil) {
			self.sceneset.active_scene.deactivate;
		};
		"ACTIVATION2".debug;
		self.install_midi_responder;
		"ACTIVATION3".debug;
		self.sceneset.old_active_scene = self.sceneset.active_scene;
		"ACTIVATION4".debug;
		self.sceneset.active_scene = self;
		"ACTIVATION5".debug;
		self.activate_hook;
		"ACTIVATION6".debug;
		if(update) {
		"ACTIVATION7".debug;
			self.update_display;
		};
		"ACTIVATION8".debug;
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

//////////////////


// main scene
~launchpad_scene_clip = (
	parent: ~launchpad_scene,

	//state_to_color: ~launchpad_scene_tile[\state_to_color],

	mode_name: \clip,

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
		//"launchpad_scene_clip.render_state".debug;
		9.do { arg x;
			8.do { arg y;
				self.render_pos(Point(x,y));
			}
		};
	},

	render_column: { arg self, x;
		8.do { arg y; 
			self.render_pos(Point(x,y));
		}
	},

	render_line: { arg self, y;
		8.do { arg x; 
			self.render_pos(Point(x,y));
		}
	},

	color_active: \red,

	render_pos: { arg self, pos;
		var color_off = \off,
			color_present = \orange,
			color_active = self.color_active,
			color_playing = \green;
		var color;
		var node, buf;
		if( pos.x == 8 ) {
			// launcher pad, do nothing
		} {

			#node, buf = self.gridpos_to_node_and_clip(pos);
			if(buf.isNil) {
				color = color_off;
				//[pos].debug("launchpad_scene_clip: render_pos: buf is nil");
				[pos];
			} {


				//[buf.uname, buf.path, buf.is_present, buf.is_playing, buf.is_active].debug("launchpad_scene_clip: render_pos");

				color = if(buf.is_present == true) {
					switch(buf.response_mode,
						\exectoggle, {
							if(buf.exectoggle_state == 1) {
								// is on
								color_playing;
							} {
								// is off
								color_active;
							}
						},
						\exec, {
							if(buf.is_pressed == true) {
								\amber
							} {
								\yellow
							}
						},
						{
							if(buf.is_active == true) {
								if(buf.response_mode == \execchoose) {
									color_playing
								} {
									if(node.is_playing == true) {
										color_playing;
									} {
										color_active;
									}
								};
							} {
								if(buf.response_mode == \execchoose) {
									\amber;
								} {
									color_present;
								}
							}
						}
					
					)
				} {
					color_off;
				};

				self.set_led(pos, color);
			}
		}

	},


	response_cc_on: { arg self, midinote;
		var res;
		var os = self.controller.clip_offset;
		[self.mode_name, midinote].debug("MIDINOTE CoNTROL ON");
		self.current_cc_on = midinote;
		//if([\clip, \edit, \mixer].includes(self.controller.mode)) {
		if(self.controller.mode == \clip) {
			self.handle_grid_move(midinote);
		};

		if(res.isNil) {
			res = switch(midinote,
				self.buttons[4], {
					//self.render_state;
					nil;
				},
				self.buttons[5], {
					self.controller.mode = \seq;
					self.sceneset.seq_chooser;
				},
				self.buttons[7], {
					self.controller.mode = \mixer_mod;
					self.sceneset.mixer_mod;
				},
				{
					nil;
				}
			);
		};
		if(res.notNil) { res.activate };
		self.render_mode_state;
	},

	response_cc_off: { arg self, midinote;
		var res;
		[self.mode_name, midinote].debug("MIDINOTE CoNTROL OFF");
		self.current_cc_on = midinote;
		//[self.controller.mode, self.sceneset.old_active_scene.mode_name].debug("response_cc_off: controller current mode 1");
		if(self.skip_first_time == true) {
			"launchpad_scene_clip".debug("skip_first_time");
			self.skip_first_time = false;
			// why old_active_scene could be not_initialized ? where in the code I init this to ~launchpad_scene ?
			// maybe mode_name is not defined in all scenes
			if(self.sceneset.old_active_scene.notNil) {
				if(self.sceneset.old_active_scene.mode_name != \not_initialized) {
					if(self.sceneset.old_active_scene.mode_name == \mixer_mod) {
						// hack
						// is executed after project switch, do nothing instead of activating the mixer_mod mode
						"response_cc_off: get rid of mixer_mod mode".debug;
						res = nil;
						self.controller.mode = \clip;
					} {

						res = self.sceneset.old_active_scene;
						if(res.mode_name.notNil) {
							// TODO: verify if active_launchpad.mode_name is always the current mode and set it in .activate
							self.controller.mode = res.mode_name;
						}
					}
				}
			}
		} {
			res = switch(midinote,
				self.buttons[4], {
					if(self.has_moved == true) {
						nil;
					} {
						self.controller.mode = \clip;
						self.sceneset.clip;
					}
				},
				self.buttons[6], {
					self.controller.mode = \edit;
					self.sceneset.edit;
				},
				self.buttons[7], { 
					self.controller.mode = \mixer;
					self.sceneset.mixer;
				},
				{
					nil;
				}
			);
		};
		[self.controller.mode, res.isNil].debug("response_cc_off: controller current mode 2, res isNil ?");
		if(res.notNil) { res.activate };
		self.render_mode_state;
	},

	play_row: { arg self, opos, stop_empty=true, stop_slices=false;
			var y;
			var off_continue_playing, off_stop_playing;
			y = opos.y;

			off_stop_playing = { arg x;
				var node = self.controller.get_node_by_index(x);
				[self.controller.clip_offset, Point(x, opos.y), self.is_abspos_visible(Point(x, opos.y))].debug("bordelYO");
				if(self.is_abspos_visible(Point(x, opos.y))) {
					node.stop_node(true, {
						self.render_column(x - self.controller.clip_offset.x);
					});
				} {
					node.stop_node(true);
				}
			};


			if(stop_empty.not) {
				off_stop_playing = nil;
			};

			self.line_do { arg x;
				var node = self.controller.get_node_by_index(x);
				if(node.notNil) {
					var clip = node.get_clip_by_index(y);
					if(clip.notNil and: {clip.is_present}) {
						clip.response_on;
					} {
						off_stop_playing.value(x);
					}
				};
			};

			if(stop_slices) {
				self.slices_do { arg x, is_excluded;
					if(is_excluded.not) {
						off_stop_playing.(x);
					}
				};
			};

			self.render_state;
		
	},

	response_launcher_on: { arg self, midinote, pos;
		var opos = self.controller.clip_offset + pos;
		var stop_empty = self.modifiers[\session] == false;
		[midinote,pos,opos,stop_empty].debug("launchpad_scene_clip.response_launcher_on");
		if(self[\response_launcher_on_hook].notNil) {
			self.response_launcher_on_hook(pos, opos, stop_empty.not)
		} {
			var stop_slices = (self.controller.mode_play_row == \stop_slices);
			self.play_row(opos, stop_empty, stop_slices);
		}
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		var opos;
		midinote.debug("launchpad_scene_clip.response_on");

		if(self.is_launcher_button(midinote)) {
			self.response_launcher_on(midinote, pos);
		} {
			midinote.debug("launchpad_scene_clip.response_on: go!");
			if(self.modifiers[\session] == true) {
				self.controller.clip_offset = pos * 8;
			} {
				#node, clip = self.gridpos_to_node_and_clip(pos);
				if(clip.is_present) {
					clip.response_on;
				} {
					node.stop_node(true, {
						self.render_column(pos.x);
					});
				};
				//self.render_column(pos.x);
			};
			self.render_state;
		};
	},

	response_off: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		var opos;
		midinote.debug("launchpad_scene_clip.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			//midinote.debug("launchpad_scene_clip.response_on: go!");
			if(self.modifiers[\session] == true) {
			} {
				#node, clip = self.gridpos_to_node_and_clip(pos);
				if(clip.is_present) {
					clip.response_off;
					self.render_pos(pos);
				} {
					//node.stop_node(true, {
					//	self.render_column(pos.x);
					//});
				};
				//self.render_column(pos.x);
			};
			//self.render_state;
		}
	},

);
// end main scene



~launchpad_scene_seq_chooser = (
	parent: ~launchpad_scene_clip,
	not_default_mode: false,

	mode_name: \seq_chooser,

	response_cc_off: { arg self, midinote;
		var res;
		self.not_default_mode.debug("launchpad_scene_seq_chooser.response_cc_off");
		if(self.not_default_mode != true) {
			self.controller.set_seq_active_mode;
		};
		self.not_default_mode = false;

		self.controller.mode = \seq; // why not seq_chooser ? bug ?
		res = self.sceneset.seq;
		if(res.notNil) { res.activate };
		// TODO: indicate mode of seq with arrow led
		//self.render_mode_state;
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_seq_chooser.response_on");

		pos = self.offset_pos(pos);

		if(self.is_launcher_button(midinote)) {
			self.controller.set_seq_row_mode(pos.y)
		} {
			self.controller.set_seq_column_mode(pos.x)
		};
		self.not_default_mode = true;
	},
);

~launchpad_scene_seq = (
	parent: ~launchpad_scene,
	color_on: \red,
	color_off: \off,
	color_disabled: \dimred,
	color_end: \dimgreen,

	mode_name: \seq,

	seq_buffers: { arg self, x;
		if(x.isNil) {
			8.collect { arg x;
				var point = self.controller.active_seq_points[x];
				self.absolute_point_to_buf(point);
			}
		} {
			var point = self.controller.active_seq_points[x];
			self.absolute_point_to_buf(point);
		}
	},

	deactivate: { arg self;
		self.seq_buffers.do { arg buf; buf.save_data; }
	},

	render_state: { arg self;
		debug("launchpad_scene_seq.render_state");
		8.do { arg x;
			self.render_line(x)
		}
	},

	activate_hook: { arg self;
		self.controller.seq_offset = 0;
	},

	render_line: { arg self, y;
		var os;
		var seq;
		//os = self.controller.seq_offset;
		//seq = self.seq_buffers(y).stepseq.drop(os).keep(8);
		8.do { arg x;
			self.render_pos(Point(x, y));
		}

	},

	render_pos: { arg self, pos;
		//var val;
		//var color;
		//var buf;
		//self.seq_buffers(pos.y).debug("render_pos: seq_buffers");
		//buf = self.seq_buffers(pos.y);
		//val = buf.stepseq[pos.x];
		//color = if(buf.is_present) { self.color_on } { self.color_disabled };
		//self.set_led(pos, if(val == 1) { color } { self.color_off })
		var x = pos.x, y = pos.y;
		var val = self.get_relpos_seq(y, x);
		var color;
		var color_on;
		color_on = if(self.seq_buffers(y).is_present.not) {
			self.color_disabled;
		} {
			self.color_on;
		};
		color = switch(val,
			nil, {
				self.color_end;
			},
			1, {
				color_on;
			},
			0, {
				self.color_off;
			}
		);
		self.set_led(Point(x,y), color);
	},

	handle_grid_move: { arg self, midinote;
		var os = self.controller.seq_offset;
		var count = 1;
		var rosae = (
			left: -1,
			right: 1,
		);
		var move = { arg dir;
			self.controller.seq_offset = (os + (rosae[dir]*8)).clip(0,64);
			[self.controller.seq_offset,os, dir, rosae[dir]].debug("seqos,os");
			self.render_state;
			nil;
		};
		switch(midinote,
			self.buttons[0], { // up
			},
			self.buttons[1], { // down
			},
			self.buttons[2], { // left
				move.(\left);
			},
			self.buttons[3], { // right
				move.(\right);
			}, {
				nil
			}
		);
		self.controller.seq_offset.debug("seq_offset");
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		self.handle_grid_move(midi);
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	//set_relpos_seq: { arg self, y, idx, val;
	//	var seq = self.seq_buffers(y).stepseq;
	//	idx = idx + self.controller.seq_offset;
	//	if(seq[idx].notNil) {
	//		seq[idx] = val;
	//	};
	//},

	set_relpos_seq: { arg self, y, idx, val;
		var buf = self.seq_buffers(y);
		var seq = buf.stepseq;
		idx = idx + self.controller.seq_offset;
		if(seq[idx].notNil) {
			buf.set_step(idx, val);
		};
	},

	toggle_relpos_seq: { arg self, y, idx;
		var buf = self.seq_buffers(y);
		var seq = self.seq_buffers(y).stepseq;
		var val;
		idx = idx + self.controller.seq_offset;
		if(seq[idx].notNil) {
			val = if(seq[idx] == 1) { 0 } { 1 };
			buf.set_step(idx, val);
		};
	},
	
	get_relpos_seq: { arg self, y, idx;
		var seq = self.seq_buffers(y).stepseq;
		var size = self.seq_buffers(y).stepseq_size;
		idx = idx + self.controller.seq_offset;
		// i think i should use a global stepseq_size to solve the bug
		if((size.isNil or: {(idx < size)}) and: {seq[idx].notNil}) {
		//if(seq[idx].notNil) {
			seq[idx];
		};
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		var seq;
		var idx;
		midinote.debug("launchpad_scene_seq.response_on");

		if(self.is_launcher_button(midinote)) {
			clip = self.seq_buffers(pos.y);
			clip.response_on;
		} {
			self.toggle_relpos_seq(pos.y, pos.x);
			self.render_pos(pos);
		}
	},

);

~launchpad_scene_edit = (
	parent: ~launchpad_scene,

	mode_name: \edit,

	render_state: { arg self;
		self.sceneset.clip.render_state;
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		if(self.controller.mode == \edit) {
			self.handle_grid_move(midi);
		};
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
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

~launchpad_scene_mixer = (
	parent: ~launchpad_scene,

	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	color_edit: \yellow,
	mode_name: \mixer,

	render_state: { arg self;
		8.do { arg x; self.render_column(x) }
	},

	render_column: { arg self, x;
		var node;
		var ox;
		var off_column = { arg x;
			8.do { arg y;
				self.set_led(Point(x, y), if(y == 3) { self.color_edit } { \off });
			};
		};
		ox = self.controller.clip_offset.x + x;
		node = self.controller.get_node_by_index(ox);
		if(node.notNil) {

			if(node.is_present) {
				if(node.is_playing) {
					self.set_led(Point(x, 0), \green);
				} {
					self.set_led(Point(x, 0), \red);
				};
				self.set_led(Point(x, 1), if(node.is_muted) { \dimred } { \dimgreen });
				self.set_led(Point(x, 2), \orange);
				self.set_led(Point(x, 3), self.color_edit);
				self.set_led(Point(x, 4), \dimred);
				3.do { arg y;
					self.set_led(Point(x, y+5), \off);
				};
			} {
				off_column.(x);
			};
		} {
			//[x, ox].debug("launchpad_scene_mixer.render_column: node is nil, x, ox");
			off_column.(x);
		};


	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		if(self.controller.mode == \mixer) {
			self.handle_grid_move(midi);
			self.sceneset.clip.has_moved = self.has_moved;
		};
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			if(self.modifiers[\session] == true) {
				self.controller.clip_offset = pos * 8;
				self.sceneset.clip.has_moved = true;
				self.render_state;
			} {
				self.sceneset.clip.has_moved = false;
				midinote.debug("launchpad_scene_mixer.response_on: go!");
				opos = self.offset_pos(pos);
				node = self.get_node_by_index(opos.x);
				switch(pos.y,
					0, {
						node.toggle_play;
					},
					1, {
						node.mute_node;
					},
					2, {
						node.execute;
					},
					3, {
						node.open_in_vim;
					},
					4, {
						node.fadeout;
					}
				);
				self.render_column(pos.x);
			}

		}
	},

	response_off: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			midinote.debug("launchpad_scene_mixer.response_on: go!");
			opos = self.offset_pos(pos);
			node = self.get_node_by_index(opos.x);
			switch(pos.y,
				1, {
					node.unmute_node;
				},
			);
			self.render_column(pos.x);
		}
	},

);

////////////// copy, paste, cut, link

~launchpad_scene_mixer_mod = (
	parent: ~launchpad_scene,

	mode_name: \mixer_mod,

	// ** launcher layout
	// - copy
	// - paste
	// - delete

	render_state: { arg self;
		//self.sceneset.clip.render_state;
	},


	response_cc_on: { arg self, midi;
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	activate_hook: { arg self;
		"launchpad_scene_mixer_mod".debug("activate_hook");
		self.sceneset.mode_old_active_scene = self.sceneset.old_active_scene;
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		var res;
		midinote.debug("launchpad_scene_mixer_mod.response_on");

		if(self.is_launcher_button(midinote)) {
			res = switch(pos.y,
				0, { // copy
					self.controller.mode = \copy;
					self.sceneset.mode_copy;
				},
				1, { // paste
					self.controller.mode = \paste;
					self.sceneset.mode_paste;
				},
				2, { // cut
					self.controller.mode = \cut;
					self.sceneset.mode_cut;
				},
				3, { // cut
					self.controller.mode = \link;
					self.sceneset.mode_link;
				},
				{
					nil
				}
			
			);
			if(res.notNil) { 
				res.skip_first_time = true;
				res.activate;
			};
		} {
			self.sceneset.clip.skip_first_time = true;
			self.sceneset.quickpad.response_on(midinote);
		}
	},

);

~launchpad_scene_mode_copy = (
	parent: ~launchpad_scene,
	color_clipboard: \red2,
	clipboard_action: \copy,

	mode_name: \mode_copy,

	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	render_state: { arg self;
		self.sceneset.clip.render_state;
		self.render_clipboard;
	},

	render_clipboard: { arg self;
		var pos, abspos, clip;
		debug("launchpad_scene_mode_copy: begin render_clipboard");
		switch(self.controller.clipboard_mode,
			\row, {
				clip = self.controller.clipboard_clip[0];
				pos = self.abspos_to_gridpos(clip.get_abspos, false);
				if(pos.y.inclusivelyBetween(0,7)) {
					//self.blink_led(self.launcher_buttons[pos.y], \red, \green);
					self.set_led(self.launcher_buttons[pos.y], self.color_clipboard);
				}
			},
			\column, {
				self.controller.clipboard_clip.do { arg clip;
					if(clip.notNil) {
						abspos = clip.get_abspos;
						if(self.is_abspos_visible(abspos)) {
							pos = self.abspos_to_gridpos(abspos, false);
							//self.blink_led(self.launcher_buttons[pos.y], \red, \green);
							self.set_led(pos, self.color_clipboard);
						}
					}
				}
			},
			\active, {
				//self.controller.clipboard_clip.do { arg clip_source, x;
				//	[clip_source.isNil, x].debug("launchpad_scene_mode_copy.render_clipboard: active, clipsourceisnil, x");
				//	if(clip_source.notNil) {
				//		abspos = clip_source.get_abspos;
				//		pos = self.abspos_to_gridpos(abspos);
				//		if(pos.notNil) {
				//			self.set_led(pos, self.color_clipboard);
				//		}
				//	}
				//};
			},
			\clip, {
				"e1".debug;
				clip = self.controller.clipboard_clip[0];
				"e2".debug;
				if(clip.notNil) {
				"e3".debug;
				clip.keys.debug("clip");
				clip.uname.debug("clipuname");
				clip.path.debug("clipdata");
				clip.eventclass.debug("clipeventclass");
				clip.clips.class.debug("clipclips");
				clip[\get_abspos].debug("clipabspos[]");
				clip.get_abspos.debug("clipabspos");
					pos = self.abspos_to_gridpos(clip.get_abspos, true);
				"e4".debug;
					if(pos.notNil) {
						//self.blink_led(pos, \red, \green);
				"e5".debug;
						self.set_led(pos, self.color_clipboard);
					};
				"e6".debug;
				}
			},
		);
		debug("launchpad_scene_mode_copy: end render_clipboard");
	},


	response_cc_on: { arg self, midi;
		//self.render_state;
		//self.unblink_led;
		self.handle_grid_move(midi);
		self.sceneset.clip.response_cc_on(midi);
	},

	activate_hook: { arg self;
		var node, clip;
		self.controller.clipboard_action = self.clipboard_action;
		if(self.sceneset.mode_old_active_scene === self.sceneset.mixer) {
			self.controller.clipboard_mode = \column;
		} {
			// -- copy active
			var clips = List.new;
			self.controller.clipboard_mode = \active;
			self.line_do { arg x, n;
				node = self.get_node_by_index(x);
				if(node.notNil and: { node.is_playing == true }) {
					clips.add(node.get_clip_by_index(node.active_clip_index));
				} {
					clips.add(nil);
				}
			};
			self.controller.clipboard_clip = clips;
		};
		self.controller.clipboard_mode.debug("================!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!================= clipboard_mode");
	},

	response_cc_off: { arg self, midi;

		if(self.skip_first_time != false) {
			self.skip_first_time = false;
		} {
			self.sceneset.clip.response_cc_off(midi);
		}
	},

	row_selected: { arg self, midinote, pos;
			var clip;
		
			var y = pos.y + self.controller.clip_offset.y;
			var clips = List.new;
			"bla".debug("kjk");
			self.line_do { arg x;
				clip = self.absolute_point_to_buf(Point(x, y));
				clips.add(clip);
			};
			self.controller.clipboard_clip = clips;
			"bla".debug("kjk2");
			self.controller.clipboard_clip.debug("launchpad_scene_mode_copy.response_on: copy row: clipboard_clip");
			self.controller.clipboard_mode = \row;
			"bla".debug("kjk3");
			self.render_state;
			"bla".debug("kjk4");
	},

	column_selected: { arg self, midinote, pos;
		var clip;
		var x = pos.x + self.controller.clip_offset.x;
		self.controller.clipboard_clip = self.controller.board_size.y.collect { arg y;
			y = y + self.controller.board_offset.y;
			clip = self.absolute_point_to_buf(Point(x, y));
			if(clip.is_present) {
				clip;
			} {
				nil;
			}
		};
		self.render_state;
		
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mode_copy.response_on");

		if(self.is_launcher_button(midinote)) {
			// -- copy row
			self.row_selected(midinote, pos);
		} {
			midinote.debug("launchpad_scene_mode_copy.response_on: go!");
			if(self.controller.clipboard_mode == \column) {
				// -- copy column
				self.column_selected(midinote, pos);
			} {
				// -- copy cell
				"t1".debug;
				self.controller.clipboard_mode = \clip;
				"t2".debug;
				clip = self.gridpos_to_buf(pos);
				"t3".debug;
				self.controller.clipboard_clip = [clip];
				"t4".debug;
				self.render_state;
				"t5".debug;
			}
		}
	},

);

~launchpad_scene_mode_cut = (
	parent: ~launchpad_scene_mode_copy,
	color_clipboard: \red2,
	clipboard_action: \cut,
	mode_name: \mode_cut,

);

~launchpad_scene_mode_link = (
	parent: ~launchpad_scene_mode_copy,
	color_clipboard: \red2,
	clipboard_action: \link,
	mode_name: \mode_link,

	column_selected: { arg self, midinote, pos;
		self.render_state;
		
	},

);

~launchpad_scene_mode_paste = (
	parent: ~launchpad_scene_mode_copy,

	mode_name: \mode_paste,
	color_pasted: \yellow,
	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	render_state: { arg self;
		self.sceneset.clip.render_state;
		self.render_clipboard;
	},

	render_clipboard: { arg self;
		self.sceneset.mode_copy.render_clipboard;
	},

	activate_hook: { arg self;
	},


	response_cc_on: { arg self, midi;
		//self.render_state;
		//self.sceneset.mode_copy.unblink_led;
		self.handle_grid_move(midi);
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		if(self.skip_first_time != false) {
			self.skip_first_time = false;
		} {
			self.sceneset.clip.response_cc_off(midi);
		}
	},

	paste_link: { arg self, midinote, pos;
		var clip_source, clip_dest;
		var clip_source_abspos, clip_dest_abspos;
		var write_header;
		var header, cell;
		var clips_to_spawner_code;
		clip_source = self.controller.clipboard_clip[0];
		clip_dest = self.gridpos_to_buf(pos);

		// TODO put the code out of interface!

		clips_to_spawner_code = { arg clips, index;
			var cell;
			cell = List[
				"~spawnclips.(["
			];
			clips.do { arg clip;
				if(clip.notNil and: { clip.is_present }) {
					cell.add(
						"\t%, %,".format(clip.parent_buffer.bare_uname.asCompileString, index ?? {clip.get_index})
					)
				}
			};
			cell.add("]);");
			cell = cell.join("\n");
			cell;
		};

		write_header = { arg tracks;
			if(clip_dest.parent_buffer.is_present.not) {
				header = [
					"~name = %;".format(clip_dest.parent_buffer.uname.asCompileString),
					"",
					"Veco(~name).lib.tracks = %;".format(tracks.asCompileString),
					"",
					"Pdef(~name, ",
					"	~clipspawner.() <>",
					"	Pdef(~name+++\\pre) <> Pbind(",
					"		\\isRest, true,",
					"		\\track, Veco(~name).lib.tracks,",
					"		\\dur, 4,",
					"	)",
					");",
				].join("\n");
				File.use(clip_dest.parent_buffer.path, "w", { arg file;
					file.write(header);
				});
				clip_dest.parent_buffer.execute;
			};
		};

		if(self.controller.clipboard_mode == \column) {
			var tracks = List.new;
			if(clip_dest.is_present.not) {
				self.controller.clipboard_clip.do { arg clip;
					if(clip.notNil) {
						tracks.add(clip.parent_buffer.uname);
					}
				};
				//cell = List[
				//	"Pdef(~name+++\\pre, Pbind(",
				//	"	\\clip, Pseq([~index], inf),",
				//	"	\\dur, 256,",
				//	"));",
				//];
				//cell = cell.join("\n");
				cell = "~spawn_cliprow.(~index);\n";


				write_header.(tracks);

				// TODO: write a cell for each row not empty
				File.use(clip_dest.path, "w", { arg file;
					file.write(cell);
				});

				self.set_led(pos, self.color_pasted);
			}
		} {
			var header;

			if(self.controller.clipboard_mode == \active) {
				if( clip_dest.is_present.not ) {
					cell = clips_to_spawner_code.(self.controller.clipboard_clip);
					write_header.();

					File.use(clip_dest.path, "w", { arg file;
						file.write(cell);
					});

					self.set_led(pos, self.color_pasted);
				}
			};

		};


	},

	cell_selected: { arg self, midinote, pos;
		var clip_source, clip_dest;
		var clip_source_abspos, clip_dest_abspos;
		var clip_dest_gridpos;
		var node_source, node_dest;
		var dest_x, source_x;
		var first;
		var opos;
		var buf;
		var node, clip;
		if(self.controller.clipboard_action == \link) {
			self.paste_link(midinote, pos);
		} {

			if(self.controller.clipboard_mode == \column) {

				// -- paste column

				first = self.controller.clipboard_clip.detect({ arg item; item.notNil });
				if(first.notNil) {
					clip_source_abspos = first.get_abspos;
					source_x = clip_source_abspos.x;
					dest_x = pos.x + self.controller.clip_offset.x;
					node_source = first.parent_buffer;
					node_dest = self.get_node_by_index(dest_x);

					if(self.controller.clipboard_action == \cut and: { node_dest.is_present }) {
						// NOOP
					} {
						node_dest.load_data_from_node(node_source);
						self.controller.clipboard_clip.do { arg clip_source;
							//clip_source = self.controller.clipboard_clip[0];
							if(clip_source.notNil) {
								clip_source_abspos = clip_source.get_abspos;
								clip_dest_abspos = Point(dest_x, clip_source_abspos.y);
								clip_dest_gridpos = self.abspos_to_gridpos(clip_dest_abspos);
								clip_dest = self.absolute_point_to_buf(clip_dest_abspos);
								if(clip_source.is_present and: { clip_dest.is_present.not }) {
									clip_dest.load_data_from_clip(clip_source);
									if(self.is_abspos_visible(clip_dest_abspos)) {
										self.set_led(clip_dest_gridpos, self.color_pasted);
									};
									if(self.controller.clipboard_action == \cut) {
										clip_source.delete_self;
									};
								}
							}
						};
						if(self.controller.clipboard_action == \cut) {
							node_source.delete_self;
						};

					}
				};

			} {

				// -- paste cell

				clip_source = self.controller.clipboard_clip[0];
				clip_dest = self.gridpos_to_buf(pos);
				if(clip_source.is_present and: { clip_dest.is_present.not }) {
					clip_dest.parent_buffer.load_data_from_node(clip_source.parent_buffer);
					clip_dest.load_data_from_clip(clip_source);
					self.set_led(pos, self.color_pasted);
					if(self.controller.clipboard_action == \cut) {
						clip_source.delete_self;
					};
				}
			}
		};
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mode_copy.response_on");

		if(self.is_launcher_button(midinote)) {
			var y = pos.y + self.controller.clip_offset.y;
			var clip_source, clip_dest;
			var dest_abspos, dest_gridpos;
			if(self.controller.clipboard_action == \link) {

			} {

				if(self.controller.clipboard_mode == \row) {

					// -- paste row

					self.line_do { arg x, n;
						dest_abspos = Point(x, y);
						dest_gridpos = self.abspos_to_gridpos(dest_abspos);
						clip_source = self.controller.clipboard_clip[n];
						if(clip_source.notNil) {

							clip_dest = self.absolute_point_to_buf(dest_abspos);
							if(clip_source.is_present and: { clip_dest.is_present.not }) {
								clip_dest.parent_buffer.load_data_from_node(clip_source.parent_buffer);
								clip_dest.load_data_from_clip(clip_source);
								if(dest_gridpos.notNil) {
									self.set_led(dest_gridpos, self.color_pasted);
								};
								if(self.controller.clipboard_action == \cut) {
									clip_source.delete_self;
								};
							}
						}
					};
				} {
					if(self.controller.clipboard_mode == \active) {

						// -- paste active

						self.line_do { arg x, n;

							clip_source = self.controller.clipboard_clip[n];
							if(clip_source.notNil) {

								dest_abspos = Point(x, y);
								dest_gridpos = self.abspos_to_gridpos(dest_abspos);
								clip_dest = self.absolute_point_to_buf(dest_abspos);
								if(clip_source.is_present  and: {  clip_dest.is_present.not }) {
									clip_dest.parent_buffer.load_data_from_node(clip_source.parent_buffer);
									clip_dest.load_data_from_clip(clip_source);
									if(dest_gridpos.notNil) {
										self.set_led(dest_gridpos, self.color_pasted);
									};
									if(self.controller.clipboard_action == \cut) {
										clip_source.delete_self;
									};
								}
							}
						};
					}
				}
			}
		} {
			self.cell_selected(midinote, pos);
		};
		// quit paste mode on first paste
		self.sceneset.clip.activate;
	},

);


//////////////////// 

~launchpad_scene_codepads = (
	parent: ~launchpad_scene,

	mode_name: \codepads,

	// quickpad is a codepad
	// the quickpad is never .activate but response_on is called directly, so no scene change
	// when calling response_on, skip_first_time is applied to clip before so the off event don't activate mixer scene

	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	render_state: { arg self;
		self.controller.gridsize.x.do { arg x;
			self.controller.gridsize.y.do { arg y;
				var pos = Point(x,y);
				if(self.controller.get_codepad(pos).is_present) {
					self.set_led(pos, \red);
				} {
					self.set_led(pos, \off);
				}
			}
		}
	},

	response_cc_on: { arg self, midi;
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			self.controller.get_codepad(pos).response_on;
		}
	},

	response_off: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			self.controller.get_codepad(pos).response_off;
		}
	},

);

~launchpad_scene_score_preset_display = (
	parent: ~launchpad_scene,

	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	color_present: \red,
	color_current: \green,
	mode_name: \score_preset,

	render_state: { arg self;
		8.do { arg y; 
			self.render_row(y) 
		}
	},

	activate_display: { arg self, score_collection, score_view;
		score_view.debug("launchpad_scene_choose_score_preset_display: activate_display: score_view");
		self.score_collection = score_collection;
		self.score_view = score_view;
		self.activate;
	},

	get_current_score_preset: { arg self;
		self.score_view.controller;
	},

	get_current_score_preset_pos: { arg self;
		var score = self.score_view.controller;
		Point(score.subindex, score.index);
	},

	render_row: { arg self, y;
		var oy = self.controller.clip_offset.y + y;
		8.do { arg x;
			var scorecol = self.score_collection;
			if(scorecol.is_score_present(oy, x) == true) {
				[self.source_preset_pos, Point(x, oy)].debug("choose_score_preset: render_row");
				if(self.get_current_score_preset_pos == Point(x, oy)) {
					self.set_led(Point(x,y), self.color_current);
				} {
					self.set_led(Point(x,y), self.color_present);
				}
			} {
				if(self.get_current_score_preset_pos == Point(x, oy)) {
					self.set_led(Point(x,y), \dimgreen);
				} {
					self.set_led(Point(x,y), \off);
				}
			}
		};
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		self.handle_grid_move(midi);
		self.sceneset.clip.has_moved = self.has_moved;
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			if(self.modifiers[\session] == true) {
				self.controller.clip_offset = pos * 8;
				self.sceneset.clip.has_moved = true;
				self.render_state;
			} {
				self.sceneset.clip.has_moved = false;
				midinote.debug("launchpad_scene_mixer.response_on: go!");
				opos = self.offset_pos(pos);
				// TODO: make special preset offset for x
				self.score_view.debug("response_on: score_view");
				self.score_view.set_controller(self.score_collection.get(opos.y, pos.x));
				self.render_state;
			}

		}
	},

	response_off: { arg self, midinote;
	},

);

~launchpad_scene_choose_score_preset_display = (
	parent: ~launchpad_scene_score_preset_display,

	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	color_present: \green,
	color_source: \red,
	color_selected: \yellow,
	mode_name: \score_preset,

	render_state: { arg self;
		8.do { arg y; 
			self.render_row(y) 
		}
	},

	activate_display: { arg self, score, score_view, set_function;
		score_view.debug("launchpad_scene_choose_score_preset_display: activate_display: score_view");
		self.score_collection = score.parent_collection;
		self.source_preset_pos = Point(score.subindex, score.index);
		self.score_view = score_view;
		self.activate;
		self.set_selected_score_preset = set_function;
	},

	render_row: { arg self, y;
		var oy = self.controller.clip_offset.y + y;
		self.score_view.debug("render_row: score_view");
		8.do { arg x;
			var scorecol = self.score_collection;
			if(scorecol.is_score_present(oy, x) == true) {
				[self.source_preset_pos, Point(x, oy)].debug("choose_score_preset: render_row");
				if(self.source_preset_pos == Point(x, oy)) {
					self.set_led(Point(x,y), self.color_source);
				} {
					self.set_led(Point(x,y), self.color_present);
				}
			} {
				self.set_led(Point(x,y), \off);
			}
		};
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		var selpos;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			if(self.modifiers[\session] == true) {
				self.controller.clip_offset = pos * 8;
				self.sceneset.clip.has_moved = true;
				self.render_state;
			} {
				self.sceneset.clip.has_moved = false;
				midinote.debug("launchpad_scene_mixer.response_on: go!");
				opos = self.offset_pos(pos);
				// TODO: make special preset offset for x
				//self.score_view.set_controller(self.score_collection.get(opos.y, pos.x));
				self.render_state;
				self.set_led(pos, self.color_selected);
				self.set_selected_score_preset(Point(pos.x, opos.y));

			}

		}
	},

);

~launchpad_scene_score_notes_display = (
	parent: ~launchpad_scene,

	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	color_present: \red,
	color_futur_present: \yellow,
	color_current: \green,
	mode_name: \score_notes,

	render_state: { arg self;
		self.render_slices;
	},

	activate_display: { arg self, score, recorder;
		self.score = score;
		self.recorder = score.recorder;
		self.selected_score_pos = max(self.recorder.slices.keep(-8).size-2, 0);
		[self.score, self.recorder].debug("sc, re");
		[self.score.recorder.keys].debug("score, recor, recor2");
		self.activate;
	},

	get_futur_slice_count: { arg self;
		// not used
		var recorder = self.score.recorder;
		var slice_dur = recorder.slice_dur;
		(recorder.get_current_recording_time / slice_dur).asInteger + 1;
	},

	render_slices: { arg self;
		var oy = self.controller.clip_offset.y + y;
		var recorder = self.score.recorder;
		var slices;
		var y = 0;
		if(recorder.is_recording) {

		} {
			slices = recorder.get_slices;
			slices = slices.keep(-8);
			8.do { arg x;
				var slice = slices[x];
				if(slice.notNil) {
					if(self.selected_score_pos == x) {
						self.set_led(Point(x,y), self.color_current);
					} {
						self.set_led(Point(x,y), self.color_present);
					}
				} {
					self.set_led(Point(x,y), \off);
				}
			};
		}
	},

	install_button_responders: { arg self;
		
	},

	response_cc_on: { arg self, midi;
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	response_on_dispatcher: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);

		if(self.is_launcher_button(midinote)) {
			self.response_launcher_on(midinote, pos);
		} {
			self.response_button_on(midinote, pos);
		}
	},

	set_selected_score: { arg self, x;
		if(self.is_score_present(x)) {
			var abspos;
			self.selected_score_pos = x;
			abspos = max(self.recorder.slices.size - 8, 0) + x;

			self.score.set_event_score(self.recorder.slices[abspos]);
			self.render_slices;
		}
	},

	is_score_present: { arg self, x;
		var recorder = self.score.recorder;
		var slices = recorder.get_slices;
		slices[x].notNil;
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			if(pos.y == 0) {
				self.set_selected_score(pos.x)
			}
		}
	},

	response_off: { arg self, midinote;
	},

);


/////////// TODO
// - navigation with session
//	- move by 8 with session + arrow
//	- move to page with session + pad
// - navigation in seq
// - use launcher in seq
// - implement muting

	//unblink_led: { arg self;
	//	if(self.blinker.notNil) {
	//		//debug("MAIS STOP BORDEL=============================");
	//		self.blinker.stop;
	//		switch(self.controller.clipboard_mode,
	//			\clip, {
	//				self.sceneset.clip.render_pos(self.blinker_pos);
	//			},
	//			{
	//				self.set_led(self.blinker_pos, \off);
	//			}
	//		);
	//	} {
	//		//debug("JUSTE PAS POSSIBLE -----------------------------");

	//	};
	//},

	//blink_led: { arg self, pos, color1, color2, time=0.5;
	//	self.unblink_led;
	//	self.blinker_pos = pos;
	//	self.blinker = Task({
	//		10.do {
	//			self.set_led(pos, color1);
	//			time.wait;
	//			self.set_led(pos, color2);
	//			time.wait;
	//		};
	//	});
	//	self.blinker.play;
	//},


////////////// not used anymore

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
