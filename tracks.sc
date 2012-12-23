
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

~class_basic_track_view = (
	block_top_padding: {arg self; self.track_size_y/10},
	track_size_x: 800,
	track_size_y: 30,
	beat_size_x: 50,
	block_size_y: 20,
	header_size_x: 100,
	//timerule_size: 1234@1234,

	//block_dict: Dictionary.new, // spritenum -> block_index
	//track_dict: Dictionary.new, // spritenum -> track_index

	//current_selected_track: 0,

	init: { arg self, parent, controller;
		debug("class_basic_track_view.new");
		self.controller = { arg self; controller };
		self.parent_view = parent;
		self.as_view = self.make_gui;
		self.main_responder = ~make_class_responder.(self, self.parent_view, controller, [
			\notes
		]);
		self.main_responder = ~make_class_responder.(self, self.parent_view, controller.display, [
			\gridstep, \gridlen
		]);
	},
	
	draw_background_function: { arg self, size, beatlen;
		{
			var i;
			size = size ?? (self.track_size_x@self.track_size_y);
			beatlen = beatlen ?? self.beat_size_x;
			// x lines
			~draw_beat_grid.(size, beatlen);

			// end line

			i = self.controller.get_end;
			if(i.notNil) {
				Pen.color = Color.red;
				Pen.line((i*self.beat_size_x)@0, (i*self.beat_size_x)@( self.track_size_y )); Pen.stroke
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
			var pos_x = (x/self.beat_size_x);
			self.controller.toggle_note(pos_x);
			self.move_list = Set.new;
		}
	},

	mouse_move_action: { arg self;
		{ arg view, x, y, modifiers;
			var pos_x = (x/self.beat_size_x);
			pos_x.debug("mouseMoveAction");
			pos_x = pos_x.trunc(self.controller.display.gridstep);
			if(self.move_list.includes(pos_x).not) {
				if(modifiers.isCtrl) {
					self.controller.remove_note(pos_x);
				} {
					self.controller.add_note(pos_x);
				};
				self.move_list.add(pos_x);
			};
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
		self.timeline.userView;
		//self.vlayout;
	
	},

	note_to_point: { arg self, note;
		Point(note.time*self.beat_size_x, self.block_top_padding);
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


);

~class_step_track_view = ( // scoreline
	parent: ~class_basic_track_view,
	
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
			totaldur = no.dur + totaldur
		};
		self.minnote = minnote;
		self.maxnote = maxnote;
		self.block_size_y = min(self.track_size_y/(maxnote - minnote), 10);
	
	},

	note_to_point: { arg self, note;
		Point(note.time*self.beat_size_x, note.midinote.linlin(self.minnote-1, self.maxnote+1, self.track_size_y, 0));
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

	new: { arg self, node;
		self = self.deepCopy;

		self.display = (
			gridlen: 16,
			gridstep: 1/4,
		);
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

~class_note_track_controller = (

	new: { arg self, node;
		self = self.deepCopy;

		self.display = (
			gridlen: 16,
			gridstep: 1/4,
		);
		
		self.get_node = node;
		self.scoreset = node.get_arg(\noteline).get_scoreset;
	
		self;
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

	new: { arg self, node;
		self = self.deepCopy;

		self.node_object = node;
		self.get_node = node;
		self.stepline = node.get_arg(\stepline);

		self.display = (
			gridlen: 16,
			gridstep: self.node_object.get_arg(\dur).get_val;
		);
		
	
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
		iabstime = abstime.trunc(self.display.gridstep);
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

/////////////////////////////// 

~class_group_tracks_controller = (
	new: { arg self, main, group, display;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_group = group;
		self.display = display;

		self.make_bindings;
	
		self;
	},

	get_tracks: { arg self;
		var res = List.new;
		self.get_group.children.do { arg name;
			var node, ctrl;
			if(name != \voidplayer) {
				node = self.get_main.get_node(name);
				ctrl = switch(node.get_mode,
					\stepline, {
						~class_stepline_track_controller.new(node);
					},
					\scoreline, {
						~class_step_track_controller.new(node);
					},
					\noteline, {
						~class_note_track_controller.new(node);
					},
					\sampleline, {
						~class_note_track_controller.new(node);
					}
				);
				if(ctrl.notNil) {
					res.add(ctrl)
				}
			}
		};
		res;
	},

	make_bindings: { arg self;
	
			self.get_main.commands.parse_action_bindings(\group_tracks, [


				[\close_window, {
					self.window.close;
				
				}],
			]);
	},

	make_gui: { arg self;
		self.window = Window.new("steptrack", Rect(300,300,900,300));
		self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\group_tracks);
		self.vlayout = VLayout.new;
		self.window.view.layout = self.vlayout;
		self.get_tracks.do { arg track;
			var hlayout;
			hlayout = HLayout(
				[StaticText.new.string_(" "++track.get_node.name).minSize_(200@30).maxSize_(200@30).background_(Color.gray(0.5)), stretch: 0, align:\center],
				track.make_gui(self.window)
			);
			//hlayout = track.make_gui(self.window);
			//self.vlayout.add(track.make_gui(self.window), 0, \topLeft)
			self.vlayout.add(hlayout, 0, \topLeft)
		};
		self.vlayout.add(nil, 1);

		self.window.front;
	},

);


//////////////////////////////////// piano roll editor


~class_blabla = (
	new: { arg self;
		self = self.deepCopy;
	
		self.notescore = ~make_notescore.();
		self.notescore.set_notes(~default_noteline3);

		self;
	},

	make_gui: { arg self;
		self.window = Window.new("Piano roll", Rect(100,100,600,600));
		self.parent_view = self.window;
		self.roll = ~class_piano_roll_editor.new(self.parent_view, self);
		self.window.front;
	
	},


	get_notes: { arg self;
		self.notescore.get_rel_notes;
	},

);

~class_piano_roll_editor = (
	piano_band_size_x: 30,
	roll_size: 1500@1500,
	beat_size_x: 50,
	block_size_y: { arg self; self.roll_size.y/128 },
	track_size_y: { arg self; self.block_size_y },
	block_dict: Dictionary.new;
	//roll_size: 400@400,

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
		self.parent_view = parent;
		self.as_view = self.make_gui;
		self.main_responder = ~make_class_responder.(self, self.parent_view, controller, [
			\notes
		]);
		//self.main_responder = ~make_class_responder.(self, self.parent_view, controller.display, [
		//	\gridstep, \gridlen
		//]);
	},

	note_to_point: { arg self, note;
		var x, y;
		y = note.midinote * self.block_size_y;
		x = note.time * self.beat_size_x;
		x@y;
	},

	mouse_down_action: { arg self;
		{ arg view, x, y, modifiers, buttonNumber, clickCount;
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

			current_pos = tl.getNodeLoc(node.spritenum);

			newx = current_pos[0].trunc(self.beat_size_x);
			newy = current_pos[1].trunc(self.track_size_y);

			new_time = ( current_pos[0].trunc(self.beat_size_x)/self.beat_size_x ).asInteger;
			new_midinote = ( current_pos[1].trunc(self.track_size_y)/self.track_size_y ).asInteger;

			block = self.block_dict[node.spritenum];
			block.midinote = new_midinote;
			block.time = new_time;
			tl.setNodeLoc_( node.spritenum, newx, newy );
		}
	},

	make_piano_roll: { arg self;
		self.piano_roll = UserView.new(nil, Rect(0,0,self.piano_band_size_x, self.roll_size.y));
		self.piano_roll.minSize = self.piano_band_size_x@ self.roll_size.y;
		self.piano_roll.background = Color.yellow;
		self.piano_roll;
	},

	make_note_view: { arg self;
		var timeline;
		"0".debug;
		self.timeline = ParaTimeline.new(self.parent_view, Rect(0,0,self.roll_size.x,self.roll_size.y));
		timeline = self.timeline;
		//self.timeline.userView.background = Color.yellow;
		timeline.userView.minSize = self.roll_size.x@self.roll_size.y;
		//self.timeline.maxHeight = 30;
		timeline.mouseDownAction = self.mouse_down_action;
		timeline.mouseMoveAction = self.mouse_move_action;
		timeline.nodeTrackAction = self.node_track_action;
		"1".debug;
		timeline.setBackgrDrawFunc_({
			Pen.color = Color.gray(0.5);
			128.do { arg y;
				y = self.block_size_y * y;
				Pen.line(0@y, self.roll_size.x@y);
			};
			Pen.stroke;
			Pen.use {
				~draw_beat_grid.((self.roll_size.x-self.piano_band_size_x)@self.roll_size.y, self.beat_size_x)
			}
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

				self.timeline.createNode(pos.x, pos.y);
				self.timeline.setNodeSize_(spritenum, self.block_size_y);
				self.timeline.paraNodes[spritenum].setLen = note.sustain * self.beat_size_x;
				self.timeline.paraNodes[spritenum].temp = pos;

				self.block_dict[spritenum] = note;
				spritenum = spritenum + 1;

			}

		};



	},

	make_gui: { arg self;
		var canvas;
		self.scrollview = ScrollView.new(self.parent_view, Rect(0,0,500,500));
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
	},


);


