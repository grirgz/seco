
~class_step_track_view = (
	
	block_top_padding: {arg self; self.track_size_y/10},
	track_size_x: 800,
	track_size_y: 30,
	beat_size_x: 50,
	block_size_y: 20,
	header_size_x: 100,
	timerule_size: 1234@1234,

	block_dict: Dictionary.new, // spritenum -> block_index
	track_dict: Dictionary.new, // spritenum -> track_index

	current_selected_track: 0,

	new: { arg self, parent, controller;
		self = self.deepCopy;

		debug("class_step_scoreline_view.new");
		self.controller = { arg self; controller };
		self.parent_view = parent;
		self.as_view = self.make_gui;
		self.main_responder = ~make_class_responder.(self, self.parent_view, controller, [
			\notes
		]);
		self.main_responder = ~make_class_responder.(self, self.parent_view, controller.display, [
			\gridstep, \gridlen
		]);
		self;
	},

	make_gui: { arg self;
	
		self.make_track;
		self.notes;
		//self.timeline.keyDownAction = self.controller.get_main.commands.get_kb_responder(\step_track);
		self.timeline;
		//self.vlayout;
	
	},

	make_track: { arg self;
		var tl;
		var move_list;
	
		//self.vlayout = VLayoutView.new(self.parent_view, Rect(0,0,self.track_size_x*2, self.track_size_y*2));
		//self.vlayout.background = Color.red;
		//self.timeline = ParaTimeline.new(self.vlayout, bounds: Rect(0, 0, self.track_size_x, self.track_size_y));
		self.timeline = ParaTimeline.new(self.parent_view, bounds: Rect(0, 0, self.track_size_x, self.track_size_y));
		//self.timeline.maxHeight = 30;
		self.timeline.mouseDownAction = { arg view, x, y, modifiers, buttonNumber, clickCount;
			var pos_x = (x/self.beat_size_x);
			buttonNumber.debug("class_timeline_view: tracks: buttonNumber");
			self.controller.toggle_note(pos_x);
			move_list = Set.new;
		};
		self.timeline.mouseMoveAction = { arg view, x, y, modifiers;
			var pos_x = (x/self.beat_size_x);
			pos_x.debug("mouseMoveAction");
			pos_x = pos_x.trunc(self.controller.display.gridstep);
			if(move_list.includes(pos_x).not) {
				if(modifiers.isCtrl) {
					self.controller.remove_note(pos_x);
				} {
					self.controller.add_note(pos_x);
				};
				move_list.add(pos_x);
			};
		};

		tl = self.timeline;

		debug("timeline_view.tracks 3");

		tl.nodeDownAction_({arg node;
			var posx = tl.getNodeLoc(node.spritenum)[0];
			posx = posx/self.beat_size_x;
			posx.debug("node down nodeloc");
			//self.controller.remove_note(posx);
			//self.controller.toggle_note(pos_x);

		});

		tl.nodeTrackAction_({arg node;
			var old_track_index, new_track_index, old_block_index, new_block_time;
			var newx, newy;
			tl.getNodeLoc(node.spritenum)[0].debug("nodeloc");
			//self.controller.remove_note(tl.getNodeLoc(node.spritenum)[0]).debug("bla");
			tl.setNodeLoc_( node.spritenum, node.temp.x, node.temp.y );

		//	newx = tl.getNodeLoc(node.spritenum)[0].trunc(self.beat_size_x);
		//	newy = tl.getNodeLoc(node.spritenum)[1].trunc(self.track_size_y) + self.block_top_padding;
		//	//newx = tl.getNodeLoc(node.spritenum)[0].round(self.beat_size_x);
		//	//newy = tl.getNodeLoc(node.spritenum)[1].round(self.track_size_y) + self.block_top_padding;

		//	new_track_index = ( tl.getNodeLoc(node.spritenum)[1].trunc(self.track_size_y)/self.track_size_y ).asInteger;
		//	new_block_time = ( tl.getNodeLoc(node.spritenum)[0].trunc(self.beat_size_x)/self.beat_size_x ).asInteger;

		//	controller.timeline_score.move_block(self.block_dict[node.spritenum], new_track_index, new_block_time);
		//	tl.setNodeLoc_( node.spritenum, newx, newy );

		});

		self.timeline.setBackgrDrawFunc_({
			// play cursor
			var cursors_pos;
			var controller = self.controller;
			//cursors_pos = (TempoClock.default.beats % 32 / 32 * self.track_size_x).asInteger;
			//cursors_pos = self.controller.start_cursor*self.beat_size_x;
			//Pen.color = Color.red;
			//Pen.line(cursors_pos@0, cursors_pos@( self.track_size_y*controller.tracks.size )); Pen.stroke;

			// x lines
			(self.track_size_x/self.beat_size_x).asInteger.do{|i| 
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
				Pen.line((i*self.beat_size_x)@0, (i*self.beat_size_x)@( self.track_size_y )); Pen.stroke
			};

			// y lines
			//controller.tracks.size.do{|i| 
			//	Pen.color = Color.gray(0.2);
			//	Pen.line(0@(i*self.track_size_y), self.track_size_x@(i*self.track_size_y)); Pen.stroke
			//};

		});

	
	},

	// responders

	notes: { arg self, controller;
		var tl;
		var stext;
		var spritenum = 0;
		controller = controller ?? self.controller;

		controller.get_notes.debug("class_step_track_view: notes");

		self.timeline.clearSpace;

		controller.get_notes.do { arg note;
			var pos;
			//note.debug("class_step_track_view: notes: note");
			if(note.type != \rest) {
				pos = Point(note.time*self.beat_size_x, self.block_top_padding);

				self.timeline.createNode(pos.x, pos.y);
				self.timeline.setNodeSize_(spritenum, self.block_size_y);
				self.timeline.paraNodes[spritenum].setLen = note.sustain * self.beat_size_x;
				self.timeline.paraNodes[spritenum].temp = pos;

				//self.block_dict[spritenum] = block;
				spritenum = spritenum + 1;
			}

		};



	},


	redraw: { arg self, controller;
		self.tracks;
	},

	//insert_cursor: { arg self, controller;
	//	self.track_headers[self.current_selected_track].background = Color.gray(0.5);
	//	self.track_headers[controller.current_track].background = Color.blue;
	//	self.current_selected_track = controller.current_track;
	//},

);


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

	new: { arg self, node;
		self = self.deepCopy;

		self.display = (
			gridlen: 16,
			gridstep: 1/4,
		);
		
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

	add_note: { arg self, abstime, sustain=0.5;
		var note;
		[abstime, sustain].debug("class_step_track_controller: add_note");
		note = (
			sustain: self.display.gridstep,
		);
		self.get_notescore.add_note(note, abstime);
		self.get_notescore.debug("add_note: get_notes");
		self.get_notescore.notes.debug("add_note: notescore.notes");
		self.scoreset.update_notes;
		self.changed(\notes);
	},

	add_note_if_empty: { arg self, abstime, sustain=0.5;
		if(self.get_notescore.is_note_playing_at_abstime(abstime).not) {
			self.add_note(abstime.trunc(self.display.gridstep), sustain);
		}
	},

	toggle_note: { arg self, abstime;
		var iabstime;
		[abstime].debug("class_step_track_controller: toggle_note");
		iabstime = abstime.trunc(self.display.gridstep);
		 if(self.get_notescore.is_note_playing_at_abstime(abstime)) {
			self.remove_note(abstime); 
		 } {
			self.add_note(iabstime); 
		 }
	
	},

	remove_note: { arg self, abstime;
		[abstime].debug("class_step_track_controller: remove_note");
		self.get_notescore.remove_notes_playing_at_abstime(abstime);
		self.changed(\notes);
	},

	make_gui: { arg self, parent;
		self.track_view = ~class_step_track_view.new(parent, self);
		self.track_view.as_view;
	},

);

~class_note_tracks_controller = (
	
	new: { arg self, node, display;
		self = self.deepCopy;

		self.ctrl_node = node;
		self.display = display;
		self.display.show_name_bloc = false;

		self;
	},

	make_gui: { arg self, parent;
		self.track_view = ~make_noteline_view.(parent, self.display, self.ctrl_node.get_arg(\noteline));
		self.track_view;
	},


);

~class_group_tracks_controller = (
	new: { arg self, main, group, display;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_group = group;
		self.display = display;
	
		self;
	},

	get_tracks: { arg self;
		var res = List.new;
		self.get_group.children.do { arg name;
			var node, ctrl;
			if(name != \voidplayer) {
				node = self.get_main.get_node(name);
				ctrl = switch(node.get_mode,
					\scoreline, {
						~class_step_track_controller.new(node);
					},
					\noteline, {
						~class_note_tracks_controller.new(node, self.display);
					},
					\sampleline, {
						~class_note_tracks_controller.new(node, self.display);
					}
				);
				if(ctrl.notNil) {
					res.add(ctrl)
				}
			}
		};
		res;
	},

	make_gui: { arg self;
		self.window = Window.new("steptrack", Rect(300,300,900,300));
		self.vlayout = VLayout.new;
		self.window.view.layout = self.vlayout;
		self.get_tracks.do { arg track;
			//self.vlayout.add(track.make_gui(self.window), 0, \topLeft)
			self.vlayout.add(track.make_gui(self.window), 2, \topLeft)
		};

		self.window.front;
	},

);
