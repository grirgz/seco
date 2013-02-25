
~draw_beat_grid = { arg size, beatlen;
	(size.x/beatlen).asInteger.do{|i| 
		case
			{i%8==0} {
				Pen.color = Color.black;
				Pen.width = 3;
			}
			{i%4==0} {
				Pen.color = Color.black;
				Pen.width = 2;
			}
			{
				Pen.color = Color.gray(0.0);
				Pen.width = 1;
			};
		Pen.line((i*beatlen)@0, (i*beatlen)@( size.y )); Pen.stroke
	};
};

~draw_stepx_grid = { arg size, beatlen, gridstep;
	var gridstepx = gridstep.x*beatlen;
	(size.x/gridstepx).asInteger.do{|i| 
		Pen.color = Color.gray;
		Pen.line((i*gridstepx)@0, (i*gridstepx)@( size.y ));
		Pen.stroke
	};
};

~draw_stepy_grid = { arg size, gridstep;
	var gridstepy = gridstep.y;
	(1/gridstepy).asInteger.do{|i| 
		Pen.color = Color.gray(1-gridstepy-0.2);
		Pen.line(0@(i*size.y*gridstepy), size.x@(i*size.y*gridstepy));
		Pen.stroke
	};
};

~class_basic_track_view = (
	block_top_padding: {arg self; self.track_size_y/10},
	track_size_x: 800,
	track_size_y: 60,
	beat_size_x: 50,
	block_size_y: 20,
	header_size_x: 100,
	node_shape: "rect",
	node_align: \topLeft,
	//timerule_size: 1234@1234,

	//block_dict: Dictionary.new, // spritenum -> block_index
	//track_dict: Dictionary.new, // spritenum -> track_index

	//current_selected_track: 0,

	init: { arg self, parent, controller;
		debug("class_basic_track_view.new");
		self.controller = { arg self; controller };
		self.update_sizes;
		self.parent_view = parent;
		self.make_gui;
		self.as_view = self.layout;
		self.main_responder = ~make_class_responder.(self, self.layout, controller, [
			\notes, \background,
		]);
		self.main_responder = ~make_class_responder.(self, self.layout, controller.display, [
			\gridstep, \gridlen
		]);
	},
	
	draw_background_function: { arg self, size, beatlen;
		{
			var i;
			size = size ?? (self.track_size_x@self.track_size_y);
			//beatlen = beatlen ?? self.beatlen;
			beatlen = self.beatlen;
			// x lines
			~draw_stepx_grid.(size, beatlen, self.controller.display.gridstep);
			~draw_beat_grid.(size, beatlen);

			// end line

			i = self.controller.get_end;
			if(i.notNil) {
				Pen.color = Color.red;
				Pen.line((i*self.beatlen)@0, (i*self.beatlen)@( self.track_size_y )); Pen.stroke
			};
		}
	},

	make_track: { arg self;
		var tl;
		var move_list;
	
		//self.vlayout = VLayoutView.new(self.parent_view, Rect(0,0,self.track_size_x*2, self.track_size_y*2));
		//self.vlayout.background = Color.red;
		//self.timeline = ParaTimeline.new(self.vlayout, bounds: Rect(0, 0, self.track_size_x, self.track_size_y));
		self.timeline = ParaTimeline.new(self.parent_view, bounds: Rect(0, 0, self.track_size_x, self.track_size_y));
		self.timeline.setShape_(self.node_shape);
		self.timeline.nodeAlign_(self.node_align);
		self.timeline.userView.minSize = self.track_size_x@self.track_size_y;
		//self.timeline.maxHeight = 30;
		self.timeline.mouseDownAction = self.mouse_down_action;
		self.timeline.mouseMoveAction = self.mouse_move_action;
		self.timeline.nodeTrackAction = self.node_track_action;
		self.timeline.setBackgrDrawFunc = self.draw_background_function;
	},

	/////// actions

	mouse_down_action: { arg self;
		{ arg view, x, y, modifiers, buttonNumber, clickCount;
			var pos_x = (x/self.beatlen);
			[x, pos_x].debug("mouse_down_action: x, pos_x");
			[modifiers, buttonNumber].debug("mouse_down_action: buttonNumber");
			~mouse_responder.(modifiers, buttonNumber, clickCount, (
				toggle_note: {
					self.controller.toggle_note(pos_x);
					self.move_list = Set.new;
				},
				set_end: {
					pos_x = pos_x.round(self.controller.display.gridstep.x);
					self.controller.set_end(pos_x)
				},
			));

		}
	},

	mouse_move_action: { arg self;
		{ arg view, x, y, modifiers;
			var pos_x = (x/self.beatlen);
			if(self.move_list.notNil) {
				pos_x.debug("mouseMoveAction");
				pos_x = pos_x.trunc(self.controller.display.gridstep.x);
				if(self.move_list.includes(pos_x).not) {
					if(modifiers.isCtrl) {
						self.controller.remove_note(pos_x);
					} {
						self.controller.add_note(pos_x);
					};
					self.move_list.add(pos_x);
				};
			}
		};
	},

	node_track_action: { arg self;
		{arg node;
			self.timeline.setNodeLoc_( node.spritenum, node.temp.x, node.temp.y );
		}
	},

	///////////

	make_gui: { arg self;
	
		self.make_track;
		self.notes;
		//self.timeline.keyDownAction = self.controller.get_main.commands.get_kb_responder(\step_track);
		self.layout = self.timeline.userView;
		self.layout;
		//self.vlayout;
	
	},

	note_to_point: { arg self, note;
		Point(note.time*self.beatlen, self.block_top_padding);
	},

	update_sizes: { arg self;
		var controller = self.controller;
		self.handle_size = 8;
		self.view_size = (self.track_size_x@self.track_size_y);
		self.view_size1 = 1@1;
		self.track_size = self.view_size-self.handle_size;
		self.track_size1 = (1-(self.handle_size/self.view_size.x)) @ (1-(self.handle_size/self.view_size.y));
		self.scaling = Point(self.track_size1.x/controller.display.gridlen, 1);
		self.offset = Point(controller.display.offset*self.scaling.x,0);
		self.beatlen = (self.track_size.x/self.controller.display.gridlen);
		[self.beatlen, self.controller.display.gridlen].debug("class_basic_track_view: update_sizes: beatlen, gridlen");
		//self.gridstep1 = controller.display.gridstep * (self.view_size1.x/self.controller.display.gridlen);
		self.gridstep1 = Point(
			controller.display.gridstep.x * (self.track_size1.x/self.controller.display.gridlen),
			controller.display.gridstep.y * self.track_size1.y // FIXME: gridlen not y
		);
	},

	///////////
	// responders

	background: { arg self;
		self.timeline.refresh;	
	},	

	gridlen: { arg self;
		self.update_sizes;
		self.notes;
		self.timeline.refresh;
	},

	gridstep: { arg self;
		self.controller.display.gridstep.debug("class_basic_track_view: gridstep");
		self.gridlen;
	},

	note_to_length: { arg self, note;
		note.sustain * self.beatlen;
	},

	notes: { arg self, controller;
		var tl;
		var stext;
		var spritenum = 0;
		var notes;
		controller = controller ?? self.controller;

		controller.get_notes.debug("class_basic_track_view: notes");

		self.timeline.clearSpace;

		notes = controller.get_notes;
		self.scan_notes(notes);

		notes.do { arg note;
			var pos;
			note.debug("class_basic_track_view: notes: note");
			if(note.type != \rest) {
				pos = self.note_to_point(note);

				self.timeline.createNode(pos.x, pos.y);
				self.timeline.setNodeSize_(spritenum, self.handle_size);
				self.timeline.paraNodes[spritenum].setLen = self.note_to_length(note);
				self.timeline.paraNodes[spritenum].temp = pos;

				//self.block_dict[spritenum] = block;
				spritenum = spritenum + 1;
			}

		};



	},


	redraw: { arg self, controller;
		self.tracks;
	},


);

~class_step_track_view = ( // scoreline
	parent: ~class_basic_track_view,

	note_to_length: { arg self, note;
		//min(note.sustain, self.controller.display.gridstep.x) * self.beatlen;
		note.sustain * self.beatlen;
	},
	
	new: { arg self, parent, controller;
		self = self.deepCopy;
		self.init(parent, controller);
		self;
	},

);

~class_stepline_track_view = (
	parent: ~class_basic_track_view,
	
	new: { arg self, parent, controller;
		self = self.deepCopy;
		self.init(parent, controller);
		self;
	},
);

~class_piano_roll_editor = (
	parent: ~class_basic_track_view,
	piano_band_size_x: 30,
	roll_size: 1500@1500,
	beat_size_x: 50,
	block_size_y: { arg self; self.roll_size.y/128 },
	track_size_y: { arg self; self.block_size_y },
	view_size_x: 1500,
	view_size_y: 1500,
	block_dict: Dictionary.new,
	moving_notes: Set.new,
	resizing_notes: Set.new,
	//roll_size: 400@400,

	update_sizes: { arg self;
		var controller = self.controller;
		self.handle_size = 12;
		self.view_size = (1500@(self.handle_size * 128));
		self.view_size1 = 1@1;
		self.track_size = self.view_size-self.handle_size;
		self.track_size1 = (1-(self.handle_size/self.view_size.x)) @ (1-(self.handle_size/self.view_size.y));
		self.scaling = Point(self.track_size1.x/controller.display.gridlen, 1);
		self.offset = Point(controller.display.offset*self.scaling.x,0);
		self.beatlen = (self.track_size.x/self.controller.display.gridlen);
		[self.beatlen, self.controller.display.gridlen].debug("class_basic_track_view: update_sizes: beatlen, gridlen");
		//self.gridstep1 = controller.display.gridstep * (self.view_size1.x/self.controller.display.gridlen);
		self.gridstep1 = Point(
			controller.display.gridstep.x * (self.track_size1.x/self.controller.display.gridlen),
			self.view_size1.y/128
		);
	},

	new: { arg self, parent, controller;
		self = self.deepCopy;

		self.parent_view = parent;
		self.init(parent, controller);
		self.notes;
	
		self;
	},

	init: { arg self, parent, controller;
		debug("class_basic_track_view.new");
		self.controller = { arg self; controller };
		self.update_sizes;
		self.parent_view = parent;
		self.as_view = self.make_gui;
		self.main_responder = ~make_class_responder.(self, self.scrollview, controller, [
			\notes, \background
		]);
		self.main_responder = ~make_class_responder.(self, self.scrollview, controller.get_node.get_arg(\noteline), [
			\notes
		]);
		self.main_responder = ~make_class_responder.(self, self.scrollview, controller.display, [
			\gridstep, \gridlen
		]);
		//self.main_responder = ~make_class_responder.(self, self.parent_view, controller.display, [
		//	\gridstep, \gridlen
		//]);
	},

	midinote_to_notename: { arg self, midinote;
		var notenames = [ "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" ];
		var octave = ((midinote / 12)).asInteger;
		var name = notenames[ midinote % 12 ];
		name ++ octave.asString;
		//midinote.asString;
	
	},

	//note_to_point: { arg self, note;
	//	var x, y;
	//	y = note.midinote * self.block_size_y;
	//	x = note.time * self.beat_size_x;
	//	x@y;
	//},

	note_to_point: { arg self, note;
		// TODO: spec unmapping
		Point(note.time*self.scaling.x,  1-(note.midinote /128));
	},

	point_to_notepoint: { arg self, point;
		var x, y;
		x = point.x / self.scaling.x;
		//x = point.x/self.beat_size_x;
		y = (1-point.y);
		y = y * 128;
		x@y;
	},

	mouse_down_action: { arg self;
		{ arg view, x, y, modifiers, buttonNumber, clickCount;
			var pos, notepos;
			var round_notepos;
			var trunc_notepos;
			trunc_notepos = {
				var pos, notepos;
				pos = x@y;
				pos = pos/self.view_size;
				pos.x = pos.x.trunc(self.gridstep1.x);
				pos.y = pos.y.trunc(self.gridstep1.y);
				pos.debug("mouse_down_action: pos");
				notepos = self.point_to_notepoint(pos);
				notepos
			};
			self.current_modifiers = modifiers;
			self.current_mouse_down_pos = trunc_notepos.();

			[modifiers, buttonNumber].debug("mouse_down_action: buttonNumber");
			// 0 left, 1: right, 2: middle
			~mouse_responder.(modifiers, buttonNumber, clickCount, (
						create_note: {
							notepos = trunc_notepos.();
							self.controller.add_note(notepos);
						},
						remove_note: {
							//FIXME: use remove_note function
							notepos = trunc_notepos.();
							self.controller.remove_note(notepos);
						},
						set_end: {
							pos = x@y;
							pos = pos/self.view_size;
							pos.x = pos.x.round(self.gridstep1.x);
							pos.debug("mouse_down_action: set_end: pos");
							notepos = self.point_to_notepoint(pos);
							notepos.debug("mouse_down_action: set_end: notepos");
							self.controller.set_end(notepos.x);
						},
			));
		}
	},

	remove_note: { arg self, apos, update=true;
		// FIXME: round is used when deleting selected notes, trunc when deleting with mouse
		var pos, notepos;
		var trunc_notepos;
		var x, y;

		apos.debug("class_piano_roll_editor: remove_note: apos");
		#x, y = apos;
		[x, y].debug("class_piano_roll_editor: remove_note: x,y");
		trunc_notepos = {
			var pos, notepos;
			[x, y].debug("class_piano_roll_editor: remove_note: x,y 2");
			pos = x@y;
			pos.debug("class_piano_roll_editor: remove_note: pos1");
			pos = pos/self.view_size;
			pos.debug("class_piano_roll_editor: remove_note: pos2");
			pos.x = pos.x.round(self.gridstep1.x);
			pos.debug("class_piano_roll_editor: remove_note: pos3");
			pos.y = pos.y.round(self.gridstep1.y);
			pos.debug("class_piano_roll_editor: remove_note: pos4");
			notepos = self.point_to_notepoint(pos);
			notepos.debug("class_piano_roll_editor: remove_note: notepos");
			notepos
		};
		notepos = trunc_notepos.();
		self.controller.remove_note(notepos, update);
	},

	make_track_notes_bindings: { arg self;
		self.controller.set_track_notes_bindings(
			[
				[\remove_notes, {
					var timeline = self.timeline;
					timeline.selNodes.do({arg box; 
						[box].debug("key_down_action: selNodes do box");
						timeline.paraNodes.copy.do({arg node, i; 
							[node, i].debug("key_down_action: paraNodes do");
							if(box === node, {
								[node, i, box, node.spritenum, box.spritenum, timeline.getNodeLoc1(node.spritenum)].debug("key_down_action:  do");
								self.remove_note(timeline.getNodeLoc(node.spritenum), false);
								//timeline.deleteNode(i);
							});
						})
					});
					if(timeline.selNodes.size > 0) {
						self.controller.refresh;
					}

				}],

				[\copy_notes, {
					var timeline = self.timeline;
					self.clipboard_notes = List.new;
					timeline.selNodes.do({ arg node;
						self.clipboard_notes.add(self.block_dict[node.spritenum] );
					});
					self.clipboard_notes.debug("copied notes");
				
				}],
				[\cut_notes, {
					var timeline = self.timeline;
					self.clipboard_notes = List.new;
					timeline.selNodes.do({ arg node;
						self.clipboard_notes.add(self.block_dict[node.spritenum] );
					});
					self.clipboard_notes.debug("copied notes");
				
					timeline.selNodes.do({arg box; 
						[box].debug("key_down_action: selNodes do box");
						timeline.paraNodes.copy.do({arg node, i; 
							[node, i].debug("key_down_action: paraNodes do");
							if(box === node, {
								[node, i, box, node.spritenum, box.spritenum, timeline.getNodeLoc1(node.spritenum)].debug("key_down_action:  do");
								self.remove_note(timeline.getNodeLoc(node.spritenum), false);
								//timeline.deleteNode(i);
							});
						})
					});
					if(timeline.selNodes.size > 0) {
						self.controller.refresh;
					}
				}],
				[\paste_notes, {
					var first_note;
					if(self.clipboard_notes.size > 0) {
						first_note = self.clipboard_notes[0];
						
						self.clipboard_notes.do { arg note;
							if(note.time < first_note.time) {
								first_note = note;
							}
						};
						self.clipboard_notes.do { arg note;
							var notepoint = Point.new;
							notepoint.x = note.time - first_note.time + self.current_mouse_down_pos.x;
							notepoint.y = note.midinote;
							notepoint.debug("pasted note");
							self.controller.add_note(notepoint, note.sustain);
						};
					};
				}],
				[\paste_notes_in_place, {
					var first_note, first_pitch;
					if(self.clipboard_notes.size > 0) {
						first_note = self.clipboard_notes[0];
						first_pitch = self.clipboard_notes[0];
						
						self.clipboard_notes.do { arg note;
							if(note.time < first_note.time) {
								first_note = note;
							}
						};
						self.clipboard_notes.do { arg note;
							if(note.midinote < first_pitch.midinote) {
								first_pitch = note;
							}
						};
						self.clipboard_notes.do { arg note;
							var notepoint = Point.new;
							notepoint.x = note.time - first_note.time + self.current_mouse_down_pos.x;
							notepoint.y = note.midinote - first_pitch.midinote + self.current_mouse_down_pos.y;
							notepoint.debug("pasted note in place");
							self.controller.add_note(notepoint, note.sustain);
						};
					};
				}],
			]

		)
	
	},

	key_down_action: { arg self;
		self.controller.get_track_kb_responder;
		
		//{ arg me, key, modifiers, unicode;
		//	var timeline = self.timeline;
		//	[key, modifiers, unicode].debug("class_piano_roll_editor: key_down_action: key, mod, uc");

		//	if(unicode == 127, {
		//		timeline.selNodes.do({arg box; 
		//			[box].debug("key_down_action: selNodes do box");
		//			timeline.paraNodes.copy.do({arg node, i; 
		//				[node, i].debug("key_down_action: paraNodes do");
		//				if(box === node, {
		//					[node, i, box, node.spritenum, box.spritenum, timeline.getNodeLoc1(node.spritenum)].debug("key_down_action:  do");
		//					self.remove_note(timeline.getNodeLoc(node.spritenum), false);
		//					//timeline.deleteNode(i);
		//				});
		//			})
		//		});
		//		if(timeline.selNodes.size > 0) {
		//			self.controller.refresh;
		//		}
		//	});
		//}
	
	},

	mouse_up_action: { arg self;
		var tl = self.timeline;
		{ arg view, x, y, modifiers, buttonNumber, clickCount;
			var newx, newy, notepoint;
			var block, current_pos;
			self.current_modifiers = nil;
			self.moving_notes.debug("piano edit: mouse_up_action: moving_notes list");
			self.moving_notes.do { arg snum;
				block = self.block_dict[snum];
				[block, nil, notepoint].debug("block point notepoint");
				current_pos = self.timeline.getNodeLoc1(snum);
				newx = current_pos[0].clip(0,1).trunc(self.gridstep1.x);
				newx.debug("newx");
				newy = current_pos[1].clip(0,1-self.margin).trunc(1/128);
				[block, newx@newy, notepoint].debug("block point notepoint");
				notepoint = self.point_to_notepoint(newx@newy);
				[block, newx@newy, notepoint].debug("block point notepoint");
				self.controller.move_note(block, notepoint.x, notepoint.y);
			};
			self.resizing_notes.do { arg snum;
				var temp_notepoint;
				var newsustain;
				var new_midinote, new_time;
				var newx, newy;
				var current_pos;
				var block;
				var notepoint;
				var temp_pos;


				self.margin = 0; // FIXME: wtf here ?

				//current_pos = tl.getNodeLoc1(snum);
				current_pos = x@y / self.view_size;
				current_pos.debug("mouse_up_action: x, y");

				temp_pos = self.timeline.paraNodes[snum].temp;
				//newx = current_pos[0].clip(0,1).trunc(self.gridstep1.x);
				//newy = current_pos[1].clip(0,1-self.margin).trunc(1/128);
				newx = current_pos.x.clip(0,1).trunc(self.gridstep1.x);
				newy = current_pos.y.clip(0,1-self.margin).trunc(1/128);

				notepoint = self.point_to_notepoint(newx@newy);

				temp_notepoint = self.point_to_notepoint(temp_pos);
				[notepoint.x, temp_notepoint.x, self.controller.display.gridstep.x].debug("notepooint, temp_notepoint, gridstepx");
				newsustain = notepoint.x - temp_notepoint.x + self.controller.display.gridstep.x;

				newsustain = newsustain.clip(0.01,20);
				block = self.block_dict[snum];
				self.controller.set_note_key(block, \sustain, newsustain);
			
			};
			self.moving_notes = Set.new;
			self.resizing_notes = Set.new;
		}
	},

	mouse_move_action: { arg self;
		{ arg view, x, y, modifiers;
		};
	},

	//key_down_action: { arg self;
	//	{ arg me, key, modifiers, unicode;
	//	};
	//},

	node_track_action2: { arg self;
		var tl = self.timeline;
		{ arg node;
			var new_midinote, new_time;
			var newx, newy;
			var current_pos;
			var block;

			current_pos = tl.getNodeLoc1(node.spritenum);

			newx = current_pos[0].trunc(self.gridstep1.x);
			newy = current_pos[1].trunc(self.gridstep1.y);

			new_time = ( current_pos[0].trunc(self.beat_size_x)/self.beat_size_x ).asInteger;
			new_midinote = ( current_pos[1].trunc(self.track_size_y)/self.track_size_y ).asInteger;

			block = self.block_dict[node.spritenum];
			self.controller.move_note(block, new_time, new_midinote);
			tl.setNodeLoc_( node.spritenum, newx, newy );
		}
	},

	node_track_action: { arg self;
		var tl = self.timeline;
		{ arg node;
			var new_midinote, new_time;
			var newx, newy;
			var current_pos;
			var block;
			var notepoint;
			var temp_pos;

			self.margin = 0; // FIXME: wtf here ?

			current_pos = tl.getNodeLoc1(node.spritenum);

			temp_pos = self.timeline.paraNodes[node.spritenum].temp;
			newx = current_pos[0].clip(0,1).trunc(self.gridstep1.x);
			newy = current_pos[1].clip(0,1-self.margin).trunc(1/128);

			notepoint = self.point_to_notepoint(newx@newy);

			if(self.current_modifiers.notNil and: { self.current_modifiers.isShift }) {
				//resize
				//FIXME: bug when multiple notes selected
				var newlen = newx - temp_pos.x + self.gridstep1.x;
				newlen = newlen * self.track_size.x;
				newlen = newlen.abs;
				self.resizing_notes = [node.spritenum];
				tl.setNodeLoc1_( node.spritenum, temp_pos.x, temp_pos.y );
				self.timeline.paraNodes[node.spritenum].setLen = newlen;
			} {
				//move
				debug("move");
				
				self.moving_notes.add(node.spritenum);
				tl.setNodeLoc1_( node.spritenum, newx, newy );

			}
			
		}
	},

	make_piano_roll: { arg self;
		self.piano_roll = UserView.new(nil, Rect(0,0,self.piano_band_size_x, self.track_size.y));
		self.piano_roll.minSize = self.piano_band_size_x@ self.track_size.y;
		self.piano_roll.background = Color.yellow;
		self.piano_roll.drawFunc_({
			
			128.do { arg y;
				var yy;
				//y = 128 - y;
				yy = self.handle_size * y;
				Pen.stringInRect(self.midinote_to_notename(128 - y), Rect(0,yy,self.piano_band_size_x,yy+self.handle_size))
			};

		});
		self.piano_roll;
	},

	draw_background_function: { arg self, size, beatlen;
		{
			var i;
			//size = size ?? (self.track_size_x@self.track_size_y);
			size = self.roll_size;
			//beatlen = beatlen ?? self.beatlen;
			beatlen = self.beatlen;
			// x lines
			~draw_stepx_grid.(size, beatlen, self.controller.display.gridstep);
			~draw_beat_grid.(size, beatlen);

			// end line

			//i = self.controller.get_end / 2;
			i = self.controller.get_end;
			if(i.notNil) {
				Pen.color = Color.red;
				Pen.line((i*self.beatlen)@0, (i*self.beatlen)@( self.track_size.y )); Pen.stroke
			};
		}
	},

	make_note_view: { arg self;
		var timeline;
		"0".debug;
		self.make_track_notes_bindings;
		self.timeline = ParaTimeline.new(self.parent_view, Rect(0,0,self.view_size.x,self.view_size.y));
		timeline = self.timeline;
		//self.timeline.userView.background = Color.yellow;
		timeline.userView.minSize = self.view_size;
		//self.timeline.maxHeight = 30;
		timeline.mouseDownAction = self.mouse_down_action;
		timeline.keyDownAction = self.key_down_action;
		timeline.mouseUpAction = self.mouse_up_action;
		timeline.mouseMoveAction = self.mouse_move_action;
		timeline.nodeTrackAction = self.node_track_action;
		"1".debug;
		timeline.setBackgrDrawFunc_({
			var i;
			Pen.color = Color.gray(0.5);
			128.do { arg y;
				var yy = self.handle_size * y;
				if(y % 12 == 0) {
					//y.debug("y 12");
					Pen.color = Color.gray(0.4);
				} {
					//y.debug("y");
					Pen.color = Color.gray(0.8);
				};
				Pen.line(0@yy, self.track_size.x@yy);
				Pen.stroke;
			};
			Pen.use {
				self.draw_background_function.();
			};
			////Pen.stroke;
			//Pen.use {
			//	~draw_beat_grid.((self.roll_size.x-self.piano_band_size_x)@self.roll_size.y, self.beat_size_x)
			//};
			//i = self.controller.get_end;
			//if(i.notNil) {
			//	Pen.color = Color.red;
			//	Pen.line((i*self.beat_size_x)@0, (i*self.beat_size_x)@( self.roll_size.y )); Pen.stroke
			//};
		});
	},

	notes: { arg self, controller;
		var tl;
		var stext;
		var spritenum = 0;
		var notes;
		//controller = controller ?? self.controller;
		controller = self.controller;

		controller.get_notes.debug("class_piano_roll_editor: notes");

		self.timeline.clearSpace;

		self.block_dict = Dictionary.new;
	
		notes = controller.get_notes;
		self.scan_notes(notes);

		notes.do { arg note;
			var pos;
			note.debug("class_piano_roll_editor: notes: note");
			if(note.type != \rest) {
				pos = self.note_to_point(note);

				self.timeline.createNode1(pos.x, pos.y);
				self.timeline.setNodeSize_(spritenum, self.handle_size);
				self.timeline.paraNodes[spritenum].setLen = note.sustain * self.beatlen;
				self.timeline.paraNodes[spritenum].temp = pos;

				self.block_dict[spritenum] = note;
				spritenum = spritenum + 1;

			}

		};



	},

	make_gui: { arg self;
		var canvas;
		//self.scrollview = ScrollView.new(self.parent_view, Rect(0,0,500,500));
		self.scrollview = ScrollView.new;
		self.scrollview.minHeight_(600);
		canvas = View.new;
		self.make_piano_roll;
		self.make_note_view;
		self.timeline.userView;
		self.layout_view = HLayout.new(
			self.piano_roll,
			self.timeline.userView
		);
		canvas.layout = self.layout_view;
		self.scrollview.canvas = canvas;
		self.scrollview;
		self.layout = VLayout(
			self.scrollview,
			Button.new,
		);
		self.layout;
	},
);

~class_sampleline_track_view = (
	parent: ~class_basic_track_view,
	piano_band_size_x: 30,
	roll_size: 800@1500,
	beat_size_x: 50,
	block_size_y: { arg self; self.roll_size.y/self.get_noterange },
	track_size_y: { arg self; self.block_size_y },
	view_size_x: 800,
	view_size_y: 1500,
	block_dict: Dictionary.new,
	moving_notes: List.new,
	//roll_size: 400@400,

	get_noterange: { arg self;
		self.controller.noterange[1] - self.controller.noterange[0]
	},

	update_sizes: { arg self;
		var controller = self.controller;
		self.handle_size = 12;
		self.view_size = (self.view_size_x@(self.handle_size * self.get_noterange));
		self.view_size1 = 1@1;
		self.track_size = self.view_size-self.handle_size;
		self.track_size1 = (1-(self.handle_size/self.view_size.x)) @ (1-(self.handle_size/self.view_size.y));
		self.scaling = Point(self.track_size1.x/controller.display.gridlen, 1);
		self.offset = Point(controller.display.offset*self.scaling.x,0);
		self.beatlen = (self.track_size.x/self.controller.display.gridlen);
		[self.beatlen, self.controller.display.gridlen].debug("class_basic_track_view: update_sizes: beatlen, gridlen");
		//self.gridstep1 = controller.display.gridstep * (self.view_size1.x/self.controller.display.gridlen);
		self.gridstep1 = Point(
			controller.display.gridstep.x * (self.track_size1.x/self.controller.display.gridlen),
			self.view_size1.y/self.get_noterange
		);
	},

	new: { arg self, parent, controller;
		self = self.deepCopy;

		self.parent_view = parent;
		self.init(parent, controller);
		self.notes;
	
		self;
	},

	init: { arg self, parent, controller;
		debug("class_basic_track_view.new");
		self.controller = { arg self; controller };
		self.update_sizes;
		self.parent_view = parent;
		self.as_view = self.make_gui;
		self.main_responder = ~make_class_responder.(self, self.timeline.userView, controller, [
			\notes, \background
		]);
		self.main_responder = ~make_class_responder.(self, self.timeline.userView, controller.display, [
			\gridstep, \gridlen
		]);
		//self.main_responder = ~make_class_responder.(self, self.parent_view, controller.display, [
		//	\gridstep, \gridlen
		//]);
	},

	note_to_point: { arg self, note;
		// TODO: spec unmapping
		// TODO: fix noterange
		note.debug("class_sampleline_track_view: note_to_point: note");
		Point(note.time*self.scaling.x,  (note[self.controller.notekey] / self.get_noterange));
	},

	point_to_notepoint: { arg self, point;
		var x, y;
		x = point.x / self.scaling.x;
		//x = point.x/self.beat_size_x;
		//y = (1-point.y);
		y = point.y;
		y = y * self.get_noterange;
		x@y;
	},

	mouse_down_action: { arg self;
		{ arg view, x, y, modifiers, buttonNumber, clickCount;
			var pos, notepos;
			var round_notepos;
			var trunc_notepos;
			trunc_notepos = {
				var pos, notepos;
				pos = x@y;
				pos = pos/self.view_size;
				pos.x = pos.x.trunc(self.gridstep1.x);
				pos.y = pos.y.trunc(self.gridstep1.y);
				pos.debug("mouse_down_action: pos");
				notepos = self.point_to_notepoint(pos);
				notepos
			};
			self.current_modifiers = modifiers;

			[modifiers, buttonNumber].debug("mouse_down_action: buttonNumber");
			// 0 left, 1: right, 2: middle
			~mouse_responder.(modifiers, buttonNumber, clickCount, (
						create_note: {
							notepos = trunc_notepos.();
							self.controller.add_note(notepos);
						},
						remove_note: {
							notepos = trunc_notepos.();
							self.controller.remove_note(notepos);
						},
						set_end: {
							pos = x@y;
							pos = pos/self.view_size;
							pos.x = pos.x.round(self.gridstep1.x);
							pos.debug("mouse_down_action: set_end: pos");
							notepos = self.point_to_notepoint(pos);
							notepos.debug("mouse_down_action: set_end: notepos");
							self.controller.set_end(notepos.x);
						},
			));
		}
	},

	mouse_up_action: { arg self;
		{ arg view, x, y, modifiers, buttonNumber, clickCount;
			var newx, newy, notepoint;
			var block, current_pos;
			self.current_modifiers = nil;
			self.moving_notes.do { arg snum;
				block = self.block_dict[snum];
				[block, nil, notepoint].debug("block point notepoint");
				current_pos = self.timeline.getNodeLoc1(snum);
				newx = current_pos[0].clip(0,1).trunc(self.gridstep1.x);
				newx.debug("newx");
				newy = current_pos[1].clip(0,1-self.margin).trunc(1/self.get_noterange);
				[block, newx@newy, notepoint].debug("block point notepoint");
				notepoint = self.point_to_notepoint(newx@newy);
				[block, newx@newy, notepoint].debug("block point notepoint");
				self.controller.move_note(block, notepoint.x, notepoint.y);
			};
			self.moving_notes = List.new;
		}
	},

	mouse_move_action: { arg self;
		{ arg view, x, y, modifiers;
		};
	},

	node_track_action: { arg self;
		var tl = self.timeline;
		{ arg node;
			var new_midinote, new_time;
			var newx, newy;
			var current_pos;
			var block;
			var notepoint;
			var temp_pos;

			self.margin = 0;
			//self.margin = self.handle_size;

			current_pos = tl.getNodeLoc1(node.spritenum);

			//newx = current_pos[0].trunc(self.beat_size_x);
			//newy = current_pos[1].trunc(self.track_size_y);
			current_pos.debug("current_pos");
			temp_pos = self.timeline.paraNodes[node.spritenum].temp;
			temp_pos.debug("temp_pos");
			//if(temp_pos.x == 0) {
			//	current_pos.debug("current_pos == 0");
			//	newx = temp_pos.x;
			//} {
			//	newx = current_pos[0].clip(0,1).trunc(self.gridstep1.x);
			//};
			newx = current_pos[0].clip(0,1).trunc(self.gridstep1.x);
			newx.debug("newx");
			newy = current_pos[1].clip(0,1-self.margin).trunc(1/self.get_noterange);

			self.current_modifiers.debug("current_modifiers");
			notepoint = self.point_to_notepoint(newx@newy);
			notepoint.debug("notepoint");
			if(self.current_modifiers.notNil and: { self.current_modifiers.isShift }) {
				//resize
				var temp_notepoint = self.point_to_notepoint(temp_pos);
				var newlen = newx - temp_pos.x + self.gridstep1.x;
				var newsustain = notepoint.x - temp_notepoint.x + self.controller.display.gridstep.x;
				newlen = newlen * self.track_size.x;
				newlen = newlen.abs;
				newsustain = newsustain.clip(0.01,20);
				[newlen, newsustain].debug("newlen, newsustain");
				block = self.block_dict[node.spritenum];
				self.controller.set_note_key(block, \sustain, newsustain);
				self.timeline.paraNodes[node.spritenum].setLen = newlen;
				tl.setNodeLoc1_( node.spritenum, temp_pos.x, temp_pos.y );
			
			} {
				//move
				debug("move");
				
				self.moving_notes.add(node.spritenum);
				tl.setNodeLoc1_( node.spritenum, newx, newy );

			}
			
		}
	},

	draw_background_function: { arg self, size, beatlen;
		{
			var i;
			//size = size ?? (self.track_size_x@self.track_size_y);
			size = self.roll_size;
			//beatlen = beatlen ?? self.beatlen;
			beatlen = self.beatlen;
			// x lines
			~draw_stepx_grid.(size, beatlen, self.controller.display.gridstep);
			~draw_beat_grid.(size, beatlen);

			// end line

			//i = self.controller.get_end / 2;
			i = self.controller.get_end;
			if(i.notNil) {
				Pen.color = Color.red;
				Pen.line((i*self.beatlen)@0, (i*self.beatlen)@( self.track_size.y )); Pen.stroke
			};
		}
	},

	make_note_view: { arg self;
		var timeline;
		"0".debug;
		self.timeline = ParaTimeline.new(self.parent_view, Rect(0,0,self.view_size.x,self.view_size.y));
		timeline = self.timeline;
		//self.timeline.userView.background = Color.yellow;
		timeline.userView.minSize = self.view_size;
		//self.timeline.maxHeight = 30;
		timeline.mouseDownAction = self.mouse_down_action;
		timeline.mouseUpAction = self.mouse_up_action;
		timeline.mouseMoveAction = self.mouse_move_action;
		timeline.nodeTrackAction = self.node_track_action;
		"1".debug;
		timeline.setBackgrDrawFunc_({
			var i;
			Pen.color = Color.gray(0.5);
			self.get_noterange.do { arg y;
				var yy = self.handle_size * y;
				if(y % 12 == 0) {
					//y.debug("y 12");
					Pen.color = Color.gray(0.4);
				} {
					//y.debug("y");
					Pen.color = Color.gray(0.8);
				};
				Pen.line(0@yy, self.track_size.x@yy);
				Pen.stroke;
			};
			Pen.use {
				self.draw_background_function.();
			};
			////Pen.stroke;
			//Pen.use {
			//	~draw_beat_grid.((self.roll_size.x-self.piano_band_size_x)@self.roll_size.y, self.beat_size_x)
			//};
			//i = self.controller.get_end;
			//if(i.notNil) {
			//	Pen.color = Color.red;
			//	Pen.line((i*self.beat_size_x)@0, (i*self.beat_size_x)@( self.roll_size.y )); Pen.stroke
			//};
		});
	},

	notes: { arg self, controller;
		var tl;
		var stext;
		var spritenum = 0;
		var notes;
		controller = controller ?? self.controller;

		controller.get_notes.debug("class_basic_track_view: notes");

		self.timeline.clearSpace;

		self.block_dict = Dictionary.new;
	
		notes = controller.get_notes;
		self.scan_notes(notes);

		notes.do { arg note;
			var pos;
			note.debug("class_basic_track_view: notes: note");
			if(note.type != \rest) {
				pos = self.note_to_point(note);

				self.timeline.createNode1(pos.x, pos.y);
				self.timeline.setNodeSize_(spritenum, self.handle_size);
				self.timeline.paraNodes[spritenum].setLen = note.sustain * self.beatlen;
				self.timeline.paraNodes[spritenum].temp = pos;

				self.block_dict[spritenum] = note;
				spritenum = spritenum + 1;

			}

		};



	},

	make_gui: { arg self;
		self.make_note_view;
		self.layout = self.timeline.userView;
		self.layout;
	},
);

~class_note_track_view = (
	parent: ~class_basic_track_view,
	
	new: { arg self, parent, controller;
		self = self.deepCopy;
		"new: class_note_track_view".debug;
		self.init(parent, controller);
		self;
	},

	/////// actions

	mouse_down_action: { arg self;
		{ arg view, x, y, modifiers, buttonNumber, clickCount;
		}
	},

	mouse_move_action: { arg self;
		{ arg view, x, y, modifiers;
		};
	},

	///////////
	// responders

	scan_notes: { arg self, notes;
		var minnote=127, maxnote=0, totaldur=0;
		notes.do { arg no, idx;
			[idx, no].debug("calcul totaldur: note");
			if(no.midinote.isSymbolWS.not, {
				minnote = min(no.midinote, minnote);
				maxnote = max(no.midinote, maxnote);
			});
			//totaldur = no.dur + totaldur
		};
		self.minnote = minnote;
		self.maxnote = maxnote;
		//self.block_size_y = min(self.track_size_y/(maxnote - minnote), 10);
	
	},

	note_to_point: { arg self, note;
		Point(note.time*self.beatlen, note.midinote.linlin(self.minnote-1, self.maxnote+1, self.track_size.y, 0));
	},
);

~class_curve_track_view = ( 
	parent: ~class_basic_track_view,
	notekey: \val,
	block_size_y: 8,
	scaling: (1/4)@1,
	gridstepB: 1@1,

	
	new: { arg self, parent, controller, notekey;
		self = self.deepCopy;
		self.controller = { controller };
		self.notekey = notekey ?? self.notekey;
		self.node_shape = "circle";
		self.node_align = \center;
		self.init(parent, controller);
		self.timeline.refresh;
		self;
	},

	mouse_down_action: { arg self;
		{ arg view, x, y, modifiers, buttonNumber, clickCount;
			var pos, notepos;
			var round_notepos;
			var trunc_notepos;
			trunc_notepos = {
				var pos, notepos;
				pos = x@y;
				pos = pos/self.view_size;
				pos.x = pos.x.trunc(self.gridstep1.x);
				pos.y = pos.y.trunc(self.gridstep1.y);
				pos.debug("mouse_down_action: pos");
				notepos = self.point_to_notepoint(pos);
				notepos
			};

			[modifiers, buttonNumber].debug("mouse_down_action: buttonNumber");
			// 0 left, 1: right, 2: middle
			~mouse_responder.(modifiers, buttonNumber, clickCount, (
						create_note: {
							notepos = trunc_notepos.();
							self.controller.add_note(notepos);
						},
						remove_note: {
							notepos = trunc_notepos.();
							self.controller.remove_note(notepos);
						},
						set_end: {
							pos = x@y;
							pos = pos/self.view_size;
							pos.x = pos.x.round(self.gridstep1.x);
							pos.debug("mouse_down_action: set_end: pos");
							notepos = self.point_to_notepoint(pos);
							notepos.debug("mouse_down_action: set_end: notepos");
							self.controller.set_end(notepos.x);
						},
			));
		}
	},

	mouse_move_action: { arg self;
		{ arg view, x, y, modifiers;
		};
	},

	node_track_action: { arg self;
		{arg node;
			//self.timeline.setNodeLoc_( node.spritenum, node.temp.x, node.temp.y );
		}
	},

	draw_background_function: { arg self, size, beatlen;
		{
			var i;
			//size = size ?? (self.track_size_x@self.track_size_y);
			size = size ?? (self.track_size.x@self.track_size.y);
			//beatlen = beatlen ?? self.beat_size_x;
			//beatlen = beatlen ?? (self.track_size.x/self.controller.display.gridlen);
			beatlen = self.beatlen;
			// x lines
			~draw_stepx_grid.(size, beatlen, self.controller.display.gridstep);
			~draw_stepy_grid.(size, self.controller.display.gridstep);
			~draw_beat_grid.(size, beatlen);

			// middle line

			Pen.line(0@(self.track_size.y/2),self.track_size.x@(self.track_size.y/2));
			Pen.stroke;

			// bottom line

			Pen.line(0@self.track_size.y,self.track_size.x@self.track_size.y);
			Pen.stroke;

			// end line

			Pen.line(self.track_size.x@0,self.track_size.x@self.track_size.y);
			Pen.stroke;

			i = self.controller.get_end;
			if(i.notNil) {
				Pen.color = Color.red;
				Pen.line((i*self.beatlen)@0, (i*self.beatlen)@( self.track_size_y )); 
				Pen.stroke;
			};

			self.draw_curve_lines;
		}
	},

	draw_curve_lines: { arg self;
		var notes;
		var first;
		var offset = 0;
		//notes = self.controller.get_notes;
		notes = self.controller.current_notes;
		//self.scan_notes(notes);

		//3.d/o { arg j;
		Pen.use {
			Pen.color = Color.red;

			block { arg break;
			
				20.do { arg j;
				
					notes.do { arg note;
						//Pen.lineTo(self.note_to_point(note) * (self.track_size.x@self.track_size.y));
						Pen.lineTo(self.note_to_point(note) * self.view_size);
					};
					first = self.note_to_point(notes.first);

					//Pen.lineTo((1@first.y) * (self.track_size.x@self.track_size.y));
					i = (self.controller.get_end * self.beatlen) ?? (self.track_size1.x * self.view_size.x);
					Pen.lineTo(i@(first.y * self.view_size.y));
					Pen.stroke;

					offset = i + offset;

					if(offset > self.view_size.x) {
						offset.debug("BREAK!!");
						break.value;
					};

					Pen.translate(i);
				}
			}
		}
	
	},

	///////////
	// responders

	notes: { arg self, controller;
		var tl;
		var stext;
		var spritenum = 0;
		var notes;
		controller = controller ?? self.controller;

		controller.get_notes.debug("class_basic_track_view: notes");

		self.timeline.clearSpace;

		self.block_dict = Dictionary.new;
	
		notes = controller.get_notes;
		self.scan_notes(notes);

		notes.do { arg note;
			var pos;
			note.debug("class_basic_track_view: notes: note");
			if(note.type != \rest) {
				pos = self.note_to_point(note);
				pos.debug("class_curve_track_view.notes: pos");

				self.timeline.createNode1(pos.x, pos.y);
				self.timeline.setNodeSize_(spritenum, self.block_size_y);
				self.block_size_y.debug("block_size_y");
				self.timeline.paraNodes[spritenum].setLen = self.handle_size ;
				self.timeline.paraNodes[spritenum].temp = pos;

				self.block_dict[spritenum] = note;
				spritenum = spritenum + 1;

			}

		};



	},

	scan_notes: { arg self, notes;
		var minnote=inf, maxnote=(-inf), totaldur=0;
		var key = self.notekey;
		notes.do { arg no, idx;
			[idx, no].debug("calcul totaldur: note");
			if(no[key].isSymbolWS.not, {
				minnote = min(no[key], minnote);
				maxnote = max(no[key], maxnote);
			});
			//totaldur = no.dur + totaldur
		};
		self.minnote = minnote;
		self.maxnote = maxnote;
		self.margin = self.block_size_y/self.track_size_y;
		//self.block_size_y = min(self.track_size_y/(maxnote - minnote), 10);
	
	},

	node_track_action: { arg self;
		var tl = self.timeline;
		{ arg node;
			var new_midinote, new_time;
			var newx, newy;
			var current_pos;
			var block;
			var notepoint;
			var temp_pos;

			current_pos = tl.getNodeLoc1(node.spritenum);

			//newx = current_pos[0].trunc(self.beat_size_x);
			//newy = current_pos[1].trunc(self.track_size_y);
			current_pos.debug("current_pos");
			temp_pos = self.timeline.paraNodes[node.spritenum].temp;
			temp_pos.debug("temp_pos");
			if(temp_pos.x == 0) {
				current_pos.debug("current_pos == 0");
				newx = temp_pos.x;
			} {
				newx = current_pos[0].clip(0,1).trunc(self.gridstep1.x);
			};
			newx.debug("newx");
			newy = current_pos[1].clip(0,1-self.margin).trunc(self.gridstep1.y);
			
			notepoint = self.point_to_notepoint(newx@newy);
			notepoint.debug("notepoint");
			
			block = self.block_dict[node.spritenum];
			self.controller.move_note(block, notepoint.x, notepoint.y);
			tl.setNodeLoc1_( node.spritenum, newx, newy );
		}
	},

	note_to_point: { arg self, note;
		// TODO: spec unmapping
		Point(note.time*self.scaling.x, (1-note[self.notekey]).linlin(0,1,0,1-self.margin));
	},

	point_to_notepoint: { arg self, point;
		var x, y;
		x = point.x / self.scaling.x;
		//x = point.x/self.beat_size_x;
		y = point.y.linlin(0,1-self.margin, 0, 1);
		y = (1-y);
		x@y;
	},

);

~class_multitrack_view = (
	new: { arg self, controller;
		self = self.deepCopy;

		self.controller = { controller };
		self.make_gui;
	
		self;
	},

	tracks: { arg self;
		//TODO
		
	},

	make_gui: { arg self;
		self.vlayout = VLayout.new;
		self.controller.get_tracks.do { arg track;
			var hlayout;
			hlayout = HLayout(
				[
					StaticText.new
						.string_(" "++track.get_node.name)
						.minSize_(200@30)
						.maxSize_(200@30)
						.background_(Color.gray(0.5)),
					stretch: 0,
					align:\center
				],
				//track.make_gui(self.window)
				track.make_gui
			);
			//hlayout = track.make_gui(self.window);
			//self.vlayout.add(track.make_gui(self.window), 0, \topLeft)
			self.vlayout.add(hlayout, 0, \topLeft)
		};
		self.vlayout.add(nil, 1);
		self.layout = self.vlayout;
		self.layout;
	},

);

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Controllers
/////////////////////////////////////////////////////////////////////////////////////////////////////////////

~default_step_scoreline = [
	(
		sustain: 0.5,
		velocity: 0.8,
		dur: 0.5
	),
	(
		sustain: 0.5,
		velocity: 0.8,
		dur: 0.5
	),
	(
		sustain: 0.5,
		velocity: 0.8,
		dur: 0.5
	),
];

~class_step_track_controller = (

	new: { arg self, node, display;
		self = self.deepCopy;

		//self.display = (
		//	gridlen: 16,
		//	gridstep: 1/4,
		//);
		self.display = display;
		self.get_node = node;
		
		//self.notes = ~make_notescore.();
		self.scoreset = node.get_arg(\scoreline).get_scoreset;
		//self.notes.set_notes(~default_step_scoreline);
		//self.notes.set_end(16);
	
		self;
	},

	get_notescore: { arg self;
		self.scoreset.get_notescore;
	},

	get_notes: { arg self;
		self.get_notescore.get_abs_notes;
	},

	set_end: { arg self, val;
		self.scoreset.get_notescore.set_end(val);
		self.scoreset.update_notes;
		//self.changed(\background);
		self.changed(\notes);
	},

	get_end: { arg self;
		self.scoreset.get_notescore.get_end.debug("get_end");
		self.scoreset.get_notescore.get_end - self.scoreset.get_notescore.abs_start
	},

	add_note: { arg self, abstime, sustain=0.5;
		var note;
		[abstime, sustain].debug("class_step_track_controller: add_note");
		if(abstime < self.get_end) {
			note = (
				sustain: self.display.gridstep.x,
				//sustain: 0.1,
			);
			self.get_notescore.add_note(note, abstime);
			self.get_notescore.debug("add_note: get_notes");
			self.get_notescore.notes.debug("add_note: notescore.notes");
			self.scoreset.update_notes;
			self.changed(\notes);
		} {
			debug("class_step_track_controller: past end: noop");
		}

	},

	toggle_note: { arg self, abstime;
		var iabstime;
		[abstime].debug("class_step_track_controller: toggle_note");
		iabstime = abstime.trunc(self.display.gridstep.x);
		if(iabstime < self.get_end) {
			if(self.get_notescore.is_note_playing_at_abstime(abstime)) {
			//if(self.get_notescore.is_note_playing_at_abstime(iabstime - self.display)) {
				self.remove_note(abstime); 
			} {
				self.add_note(iabstime); 
			}
		} {
			debug("class_step_track_controller: past end: noop");
		}
	},

	remove_note: { arg self, abstime;
		[abstime].debug("class_step_track_controller: remove_note");
		self.get_notescore.remove_notes_playing_at_abstime(abstime);
		self.scoreset.update_notes;
		self.changed(\notes);
	},

	make_gui: { arg self, parent;
		self.track_view = ~class_step_track_view.new(parent, self);
		self.track_view.as_view;
	},

);

//~class_note_track_controller = (
//
//	new: { arg self, node, display;
//		self = self.deepCopy;
//
//		//self.display = (
//		//	gridlen: 16,
//		//	gridstep: 1/4,
//		//);
//		self.display = display;
//		
//		self.get_node = node;
//		self.scoreset = node.get_arg(\noteline).get_scoreset;
//	
//		self;
//	},
//
//	set_end: { arg self, val;
//		self.scoreset.get_notescore.set_end(val);
//		self.scoreset.update_notes;
//		self.changed(\background);
//	},
//
//	get_end: { arg self;
//		self.scoreset.get_notescore.get_end.debug("get_end");
//		self.scoreset.get_notescore.get_end - self.scoreset.get_notescore.abs_start
//	},
//
//	get_notescore: { arg self;
//		self.scoreset.get_notescore;
//	},
//
//	get_notes: { arg self;
//		self.get_notescore.get_abs_notes;
//	},
//
//
//	make_gui: { arg self, parent;
//		self.track_view = ~class_note_track_view.new(parent, self);
//		self.track_view.as_view;
//	},
//
//);

~class_note_track_controller = (

	new: { arg self, node, display;
		self = self.deepCopy;

		//self.display = (
		//	gridlen: 16,
		//	gridstep: 1/4,
		//);
		self.display = display;
		
		self.get_node = node;
		self.scoreset = node.get_arg(\noteline).get_scoreset;
	
		self;
	},

	set_end: { arg self, val;
		self.scoreset.get_notescore.set_end(val);
		self.scoreset.update_notes;
		self.changed(\background);
	},

	get_end: { arg self;
		self.scoreset.get_notescore.get_end.debug("get_end");
		self.scoreset.get_notescore.get_end - self.scoreset.get_notescore.abs_start
	},

	get_notescore: { arg self;
		self.scoreset.get_notescore;
	},

	get_notes: { arg self;
		self.get_notescore.get_abs_notes;
	},


	make_gui: { arg self, parent;
		self.track_view = ~class_note_track_view.new(parent, self);
		self.track_view.as_view;
	},

);

~class_stepline_track_controller = (

	new: { arg self, node, display;
		self = self.deepCopy;

		self.node_object = node;
		self.get_node = node;
		self.stepline = node.get_arg(\stepline);

		//self.display = (
		//	gridlen: 16,
		//	gridstep: self.node_object.get_arg(\dur).get_val;
		//);
		self.display = display;
		
	
		self;
	},

	get_notescore: { arg self;
		self.scoreset.get_notescore;
	},

	get_notes: { arg self;
		var dur, sustain; 
		var notes;
		//FIXME: what if modulated sustain or dur ?
		dur = self.node_object.get_arg(\dur).get_val;
		//sustain = self.node_object.get_arg(\sustain).get_val;
		sustain = dur;
		notes = self.stepline.seq.val.collect { arg step, idx;
			var note;
			note = (
				type: if(step == 1) { \note } { \rest },
				time: idx * dur,
				dur: dur,
				sustain: sustain,
			)
		};
		notes.debug("class_stepline_track_controller: notes");
		notes;

	},
	
	get_end: { arg self;
		var dur;
		dur = self.node_object.get_arg(\dur).get_val;
		self.stepline.seq.val.size * dur;
	},
	
	set_end: { arg self, val;
		var count;
		var dur;
		var cursize; 
		var newsize;
		var delta;
		dur = self.node_object.get_arg(\dur).get_val;
		val = val.round(self.display.gridstep.x);
		cursize = self.stepline.get_cells.size;
		newsize = val / dur;
		delta = (newsize - cursize).asInteger;
		if(delta > 0) {
			self.stepline.add_cells({ arg x; self.stepline.seq.val.wrapAt(x) } ! delta);
			self.changed(\notes);
		};
		if(delta < 0) {
			self.stepline.remove_cells(delta.abs);
			self.changed(\notes);
		};
		val.debug("class_stepline_track_controller.set_end");
		//self.stepline.add_cells([])

	
	},

	add_note: { arg self, abstime, sustain=0.5;
		var note;
		var dur;
		var idx;
		[abstime, sustain].debug("class_stepline_track_controller: add_note");
		dur = self.node_object.get_arg(\dur).get_val;
		idx = abstime/dur;
		if(idx.inclusivelyBetween(0, self.stepline.seq.val.size-1)) {
			self.stepline.set_val(1,idx);
		};
		[abstime/dur, self.stepline.seq.val].debug("add_note: idx, array");
		self.changed(\notes);
	},

	remove_note: { arg self, abstime;
		var dur;
		[abstime].debug("class_stepline_track_controller: remove_note");
		dur = self.node_object.get_arg(\dur).get_val;
		self.stepline.set_val(0,abstime/dur);
		self.changed(\notes);
	},

	toggle_note: { arg self, abstime;
		var iabstime;
		var dur;
		var idx;
		[abstime].debug("class_stepline_track_controller: toggle_note");
		dur = self.node_object.get_arg(\dur).get_val;
		iabstime = abstime.trunc(self.display.gridstep.x);
		//[iabstime/dur, self.stepline.get_val(iabstime/dur)].debug("class_stepline_track_controller: get_val: idx, val");
		idx = iabstime/dur;
		if(idx.inclusivelyBetween(0, self.stepline.seq.val.size-1)) {
			if(self.stepline.get_val(idx) == 1) {
				self.remove_note(iabstime); 
			} {
				self.add_note(iabstime); 
			}
		}
	},

	make_gui: { arg self, parent;
		self.track_view = ~class_stepline_track_view.new(parent, self);
		self.track_view.as_view;
	},

);

~class_piano_track_controller = (
	new: { arg self, node, display;
		self = self.deepCopy;

		//self.display = (
		//	gridlen: 16,
		//	gridstep: 1/4,
		//);
		//self = self.deepCopy;
	
		//self.notescore = ~make_notescore.();
		//self.notescore.set_notes(~default_noteline3);
		self.display = display;
		
		self.get_node = {node};
		self.scoreset = node.get_arg(\noteline).get_scoreset;

		self;
	},

	refresh: { arg self;
		self.scoreset.update_notes;
		//self.changed(\notes);
	},

	make_gui: { arg self;
		//self.window = Window.new("Piano roll", Rect(100,100,600,600));
		//self.parent_view = self.window;
		self.parent_view = nil;
		self.roll = ~class_piano_roll_editor.new(self.parent_view, self);
		self.layout = self.roll.layout;
		self.layout;
		//self.window.front;
	
	},

	get_track_kb_responder: { arg self;
		self.get_node.get_main.commands.get_kb_responder(\track_notes)
	},

	set_track_notes_bindings: { arg self, bindings;
		self.get_node.get_main.commands.parse_action_bindings(\track_notes, bindings);
	},

	get_notescore: { arg self;
		self.scoreset.get_notescore;
	},

	get_notes: { arg self;
		self.current_notes = self.scoreset.notescore.get_abs_notes;
		self.current_notes.debug("get_notes");
	},

	move_note: { arg self, note, time, midinote;
		self.current_notes.do { arg no, i; [i, no].debug("before move_note: note") };
		note.time = time;
		[note.midinote, midinote].debug("move_note: moving note before, new midinote");
		note.midinote = midinote;
		note.midinote.debug("move_note: moving note after");
		self.current_notes.do { arg no, i; [i, no].debug("after move_note: note") };
		self.scoreset.get_notescore.set_abs_notes(self.current_notes);
		self.scoreset.update_notes;
	},

	set_note_key: { arg self, note, key, val;
		self.current_notes.do { arg no, i; [i, no].debug("before set_note_key: note") };
		note.debug("set_note_key: before");
		note[key] = val;
		note.debug("set_note_key: after");
		self.current_notes.do { arg no, i; [i, no].debug("after set_note_key: note") };
		self.scoreset.get_notescore.set_abs_notes(self.current_notes);
		self.scoreset.update_notes;
	},

	remove_note: { arg self, notepoint, update=true;
		var cons;
		[notepoint.x].debug("class_step_track_controller: remove_note");
		cons = { arg no;
			no.midinote == notepoint.y;
		};
		self.get_notescore.remove_notes_playing_at_abstime(notepoint.x, cons);
		if(update) {
			self.refresh
			//self.scoreset.update_notes;
			//self.changed(\notes);
		}
	},

	add_note: { arg self, notepoint, sustain=nil;
		var note;
		var midinote;
		var abstime;
		abstime = notepoint.x;
		midinote = notepoint.y;
		sustain = sustain ?? self.display.gridstep.x;
		[abstime, sustain].debug("class_step_track_controller: add_note");
		if(abstime < self.get_end) {
			note = (
				midinote: midinote,
				sustain: sustain,
			);
			self.get_notescore.add_note(note, abstime);
			self.get_notescore.debug("add_note: get_notes");
			self.get_notescore.notes.debug("add_note: notescore.notes");
			self.refresh;
			//self.scoreset.update_notes;
			//self.changed(\notes);
		} {
			debug("class_step_track_controller: past end: noop");
		}

	},

	set_end: { arg self, val;
		val.debug("controller: set_end");
		self.scoreset.get_notescore.set_end(val);
		//self.scoreset.update_notes;
		self.changed(\background);
		//self.changed(\notes);
		self.refresh;
	},

	get_end: { arg self;
		self.scoreset.get_notescore.get_end.debug("get_end");
		self.scoreset.get_notescore.get_end - self.scoreset.get_notescore.abs_start
	},

);

~class_sampleline_track_controller = (
	new: { arg self, node, display;
		self = self.deepCopy;

		//self.display = (
		//	gridlen: 16,
		//	gridstep: 1/4,
		//);
		//self = self.deepCopy;
	
		//self.notescore = ~make_notescore.();
		//self.notescore.set_notes(~default_noteline3);
		self.display = display;
		
		self.get_node = {node};
		self.scoreset = node.get_arg(\sampleline).get_scoreset;
		self.notekey = \slotnum;
		self.noterange = [0,8];

		self;
	},

	make_gui: { arg self;
		//self.window = Window.new("Piano roll", Rect(100,100,600,600));
		//self.parent_view = self.window;
		self.parent_view = nil;
		self.roll = ~class_sampleline_track_view.new(self.parent_view, self);
		self.layout = self.roll.layout;
		self.layout;
		//self.window.front;
	
	},

	get_notescore: { arg self;
		self.scoreset.get_notescore;
	},

	get_notes: { arg self;
		self.current_notes = self.scoreset.notescore.get_abs_notes;
		self.current_notes.debug("get_notes");
	},

	move_note: { arg self, note, time, noteval;
		note.time = time;
		note[self.notekey] = noteval;
		self.scoreset.get_notescore.set_abs_notes(self.current_notes);
		self.scoreset.update_notes;
	},

	set_note_key: { arg self, note, key, val;
		note[key] = val;
		self.scoreset.get_notescore.set_abs_notes(self.current_notes);
		self.scoreset.update_notes;
	},

	remove_note: { arg self, notepoint;
		var cons;
		[notepoint.x].debug("class_step_track_controller: remove_note");
		cons = { arg no;
			no[self.notekey] == notepoint.y;
		};
		self.get_notescore.remove_notes_playing_at_abstime(notepoint.x, cons);
		self.scoreset.update_notes;
		self.changed(\notes);
	},

	add_note: { arg self, notepoint, sustain=0.5;
		var note;
		var keyval;
		var abstime;
		abstime = notepoint.x;
		keyval = notepoint.y;
		[abstime, sustain].debug("class_step_track_controller: add_note");
		if(abstime < self.get_end) {
			note = (
				sustain: self.display.gridstep.x,
			);
			note[self.notekey] = keyval;
			self.get_notescore.add_note(note, abstime);
			self.get_notescore.debug("add_note: get_notes");
			self.get_notescore.notes.debug("add_note: notescore.notes");
			self.scoreset.update_notes;
			self.changed(\notes);
		} {
			debug("class_step_track_controller: past end: noop");
		}

	},

	set_end: { arg self, val;
		val.debug("controller: set_end");
		self.scoreset.get_notescore.set_end(val);
		self.scoreset.update_notes;
		self.changed(\background);
		self.changed(\notes);
	},

	get_end: { arg self;
		self.scoreset.get_notescore.get_end.debug("get_end");
		self.scoreset.get_notescore.get_end - self.scoreset.get_notescore.abs_start
	},
);

~default_curveline = [ // FIXME: crash when no notes
	(
		velocity: 0.5,
		sustain: 0.1,
		dur: 0.5
	),
	(
		velocity: 0.8,
		sustain: 0.1,
		dur: 1.5
	),
	(
		velocity: 0.0,
		sustain: 0.1,
		dur: 1.0
	),
];

~class_curve_track_controller = (

	new: { arg self, node, display, notekey;
		self = self.deepCopy;

		//self.display = (
		//	gridlen: 16,
		//	gridstep: (1/4)@0.1,
		//	offset: 0@0,
		//);
		self.display = display;

		self.notekey = notekey ?? \velocity;
		
		self.get_node = {node};
		self.notescore = node.get_arg(\noteline).get_scoreset.get_notescore;
		//self.scoreset = node.get_arg(\noteline).get_scoreset;
		//self.scoreset = node.get_arg(\scoreline).get_scoreset;
		//self.notescore = ~make_notescore.();
		//self.notescore.set_notes(~default_curveline);
		//self.notescore.set_end(16);
	
		self;
	},

	get_notescore: { arg self;
		self.notescore;
	},

	get_notes: { arg self;
		debug("class_curve_track_controller: move note");
		self.current_notes = self.notescore.get_abs_notes;
		self.current_notes;
	},

	set_end: { arg self, val;
		self.notescore.set_end(val);
		self.update_notes;
		self.changed(\background);
		self.changed(\notes);
	},

	get_end: { arg self;
		self.notescore.get_end.debug("get_end");
	},

	update_notes: { arg self;
		self.get_node.get_arg(\val).set_notes(self.notescore.get_rel_notes);
		self.get_node.get_arg(\noteline).get_scoreset.update_notes;
	},

	move_note: { arg self, note, time, notekey;
		debug("class_curve_track_controller: move note");
		note.time = time;
		note[self.notekey] = notekey;
		self.notescore.set_abs_notes(self.current_notes);
		self.update_notes;
	},

	add_note: { arg self, pos;
		var note;
		[pos].debug("class_curve_track_controller.add_note");
		note = (
			sustain: 0.1,
	    );
		note[self.notekey] = pos.y;
		self.notescore.add_note(note, pos.x);
		self.update_notes;
		self.changed(\notes);
	
	},

	remove_note: { arg self, notepos;
		self.notescore.remove_notes_at_abstime(notepos.x, { arg no; no[self.notekey] == notepos.y });
		self.update_notes;
		self.changed(\notes);
	},

	make_gui: { arg self, parent;
		self.track_view = ~class_curve_track_view.new(parent, self, self.notekey);
		//self.track_view = ~class_note_track_view.new(parent, self);
		self.layout = self.track_view.layout;
		self.layout.debug("class_curve_track_controller.layout");
		self.layout;
	},

);

/////////////////////////////// 

~make_recordline_track_controller = { arg node, display;
	var ctrl;

	ctrl = switch(node.get_mode,
		\stepline, {
			~class_stepline_track_controller.new(node, display);
		},
		\scoreline, {
			~class_step_track_controller.new(node, display);
		},
		\noteline, {
			~class_note_track_controller.new(node, display);
		},
		\sampleline, {
			~class_sampleline_track_controller.new(node, display);
		}
	);

	ctrl;
};

~class_track_display = (
	gridlen: 16,
	gridstep: (1/4)@0.0625,
	offset: 0@0,

	new: { arg self;
		self = self.deepCopy;
	
		self;
	},

	increase_gridlen: { arg self;
		self.gridlen = self.gridlen * 2;
		self.changed(\gridlen);
	},

	decrease_gridlen: { arg self;
		self.gridlen = self.gridlen / 2;
		self.changed(\gridlen);
	},

	increase_gridstep_x: { arg self;
		self.gridstep.x = self.gridstep.x * 2;
		self.changed(\gridstep);
	},

	decrease_gridstep_x: { arg self;
		self.gridstep.x = self.gridstep.x / 2;
		self.changed(\gridstep);
	},

	increase_gridstep_y: { arg self;
		self.gridstep.y = self.gridstep.y * 2;
		self.changed(\gridstep);
	},

	decrease_gridstep_y: { arg self;
		self.gridstep.y = self.gridstep.y / 2;
		self.changed(\gridstep);
	},

	get_bindings: { arg self;
		[
			[\increase_gridstep_x, {
				self.increase_gridstep_x;
			}],
			[\decrease_gridstep_x, {
				self.decrease_gridstep_x;
			}],
			[\increase_gridstep_y, {
				self.increase_gridstep_y;
			}],
			[\decrease_gridstep_y, {
				self.decrease_gridstep_y;
			}],
			[\increase_gridlen, {
				self.increase_gridlen;
			}],
			[\decrease_gridlen, {
				self.decrease_gridlen;
			}],
		]
	},
);

~class_group_tracks_controller = (
	new: { arg self, main, group, display;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_group = {group};
		self.display = display;
		//self.display = ~class_track_display.new;

	
		self;
	},

	get_tracks: { arg self;
		var res = List.new;
		self.get_group.children.do { arg name;
			var node, ctrl;
			if(name != \voidplayer) {
				node = self.get_main.get_node(name);
				ctrl = ~make_recordline_track_controller.(node, self.display);
				if(ctrl.notNil) {
					res.add(ctrl)
				}
			}
		};
		res;
	},

	make_bindings: { arg self;
	
			self.get_main.commands.parse_action_bindings(\group_tracks, 

				self.display.get_bindings ++ 
				self.get_main.panels.side.get_shared_bindings ++ 
				self.get_main.panels.side.get_windows_bindings ++ [

				[\close_window, {
					self.window.close;
				
				}],
			]);
	},

	make_gui: { arg self;
		self.make_bindings;
		self.window = Window.new("Group Tracks", Rect(300,300,900,300));
		self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\group_tracks);
		self.multitrack_view = ~class_multitrack_view.new(self);
		self.window.view.layout = self.multitrack_view.layout;

		self.window.front;
	},

);

~class_player_tracks_controller = (
	new: { arg self, main, player, display;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = {player};
		self.display = display;
		//self.display = ~class_track_display.new;

	
		self;
	},

	get_tracks: { arg self;
		var res = List.new;
		var player = self.get_player;
		res.add(~make_recordline_track_controller.(player, self.display));
		player.modulation.get_modulators.do { arg modname;
			var node;
			var track;
			node = self.get_main.get_node(modname);
			if(node.defname == \modenv) {
				track = ~class_curve_track_controller.new(node, self.display, \val);
				res.add(track);
			};
		
		};
		res;
	},

	make_bindings: { arg self;
	
		self.get_main.commands.parse_action_bindings(\player_tracks, 
			self.get_main.panels.side.get_shared_bindings ++ 
			self.display.get_bindings ++ 
			self.get_main.panels.side.get_windows_bindings ++ [
			[\close_window, {
				self.window.close;
			
			}],

			[\play_selected, {
				self.get_player.play_node;
			}], 

			[\stop_selected, {
				self.get_player.stop_node;
			}],

			[\panic, {
				self.get_main.panic;
			}],
		]);
	},

	make_gui: { arg self;
		self.make_bindings;
		self.window = Window.new("Player Tracks", Rect(300,300,900,300));
		self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\player_tracks);
		self.multitrack_view = ~class_multitrack_view.new(self);
		self.window.view.layout = self.multitrack_view.layout;

		self.window.front;
	},

);

~class_line_tracks_controller = (
	new: { arg self, main, player, display;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = {player};
		self.display = display;
		//self.display = ~class_track_display.new;

	
		self;
	},

	get_tracks: { arg self;
		var res = List.new;
		var player = self.get_player;
		res.add(~class_piano_track_controller.new(player, self.display));
		res;
	},

	make_bindings: { arg self;
	
		self.get_main.commands.parse_action_bindings(\line_tracks, 
			self.get_main.panels.side.get_shared_bindings ++
			self.display.get_bindings ++ 
			self.get_main.panels.side.get_windows_bindings ++ [
			[\close_window, {
				self.window.close;
			
			}],

			[\play_selected, {
				self.get_player.play_node;
			}], 

			[\stop_selected, {
				self.get_player.stop_node;
			}],

			[\panic, {
				self.get_main.panic;
			}],
		]);
	},

	make_gui: { arg self;
		self.make_bindings;
		self.window = Window.new("line tracks", Rect(300,300,900,300));
		self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\line_tracks);
		self.multitrack_view = ~class_multitrack_view.new(self);
		self.window.view.layout = self.multitrack_view.layout;

		self.window.front;
	},

);

//////////////////////////////////// piano roll editor




