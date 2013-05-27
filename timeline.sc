// TODO:
// - keyboard shortcuts
//		- play/stop
// - banks
// - node_chooser banks
// - solo/mute buttons
// - add block shortcut
// - current playing cursor
// - stop cursor
// - pause_cursor
// - get block size from node


~class_timerule_view = (

	new: { arg self, parent, controller, header_size_x, beat_size_x=10, view_size;
		self = self.deepCopy;

		self.view_size = view_size ?? 800@30;
		self.parent = parent;
		self.header_size_x = header_size_x;

		self.controller = controller;
		self.beat_size_x = beat_size_x;
		self.make_gui;
		self.main_responder = ~make_class_responder.(self, self.view, controller, [
			\play_cursor,
		], false);


		self;
	},

	make_gui: { arg self;
	
		var beat_size_x = self.beat_size_x;
		var width = self.view_size.x;
		var height = self.view_size.y;
		//self.view = UserView.new(self.parent, Rect(self.header_size_x, 0, width, height));
		self.view = UserView.new(nil, Rect(self.header_size_x, 0, width, height));
		self.view.fixedHeight = height;
		//self.view.background = Color.blue;
		self.view.drawFunc = {

				Pen.color = Color.red;
				Pen.line((self.controller.play_cursor*beat_size_x)@0, (self.controller.play_cursor*beat_size_x)@height); 
				Pen.stroke;

				(width/beat_size_x).asInteger.do{|i| 
					Pen.color = Color.black;
					[i, self.controller.get_display_range].debug("class_timeline_view: drawFunc: before, i, range");
					j = i + self.controller.get_display_range[0];
					[i, self.controller.get_display_range].debug("class_timeline_view: drawFunc: i, range");
					if(i >= 0) {

						case
							{ j%32==0 } { 
								Pen.color = Color.blue;
								Pen.line((i*beat_size_x)@0, (i*beat_size_x)@height); Pen.stroke
							}
							{ j%8==0 } { 
							
								Pen.line((i*beat_size_x)@0, (i*beat_size_x)@height); Pen.stroke
							}
							{ j%4==0 } { 
							
								j.asString.drawAtPoint(Point(i*beat_size_x-5, height/8-5));

								Pen.line((i*beat_size_x)@(height/2), (i*beat_size_x)@height); Pen.stroke
							}
							//
							{ 
								Pen.line((i*beat_size_x)@(3*height/4), (i*beat_size_x)@height); Pen.stroke
							}

					
					}
				};
		};
	},

	play_cursor: { arg self, controller;
		controller = self.controller;
		{
			self.view.refresh;
		}.defer;
	
	}

);

// FIXME: moving blocks doesnt work anymore because get_abs_notes copy notes
~class_timeline_view = (
	
	block_top_padding: {arg self; self.track_size_y/10},
	track_size_x: 800,
	track_size_y: 30,
	beat_size_x: 10,
	block_size_y: 25,
	header_size_x: 100,
	timerule_size: 1234@27,

	block_dict: Dictionary.new, // spritenum -> block_index
	//track_dict: Dictionary.new, // spritenum -> track_index

	current_selected_track: 0,

	new: { arg self, controller;
		self = self.deepCopy;

		debug("class_timeline_view.new");
		self.controller = controller;
		self.make_bindings;
		self.make_gui;
		self.main_responder = ~make_class_responder.(self, self.window.view, controller, [
			\tracks, \blocks, \redraw, \insert_cursor,
		]);
		self;
	},

	make_gui: { arg self;
	
		self.timerule_size = (self.track_size_x+20+self.header_size_x)@(self.track_size_y-4);

		self.window = Window.new("Timeline", Rect(10, 500, 
			self.track_size_x+20+self.header_size_x, 
			self.track_size_y*(self.controller.tracks.size+1)+10+self.timerule_size.y
		));
		self.window.view.keyDownAction = self.controller.get_main.commands.get_kb_responder(\timeline);
		//self.tracks(self.controller);
		//self.window.layout = self.layout;
		self.window.front;
		self.window;
	
	},

	// responders
	
	tracks_OLD: { arg self, controller;
		//var tl;
		//var stext;
		//controller = controller ?? self.controller;

		//debug("timeline_view.tracks");
	
		//self.layout.remove;
		//self.layout = HLayoutView.new(self.window, Rect(0, 0,
		//	self.track_size_x+10 + self.header_size_x,
		//	self.track_size_y*( controller.tracks.size+1 )
		//));

		//self.headerlayout = VLayoutView.new(self.layout, Rect(0, 0, self.header_size_x+5, self.track_size_y*(controller.tracks.size+1)));

		//stext = StaticText.new(self.headerlayout, Rect(0,0,self.header_size_x,self.track_size_y-4));
		//stext.background = Color.white;
		//stext.string = "timerule";

		//debug("timeline_view.tracks2");

		//self.track_headers = controller.tracks.collect { arg track;
		//	stext = StaticText.new(self.headerlayout, Rect(0,0,self.header_size_x,self.track_size_y-4));
		//	if(controller.current_track == i) {

		//	} {
		//	
		//	};
		//	debug("timeline_view.tracks2 1");
		//	stext.background = Color.gray(0.5);
		//	stext.string = track.name.asString;
		//	stext;
		//};

		//debug("timeline_view.tracks2 2");

		//self.vlayout = VLayoutView.new(self.layout, Rect(0,0,
		//	self.track_size_x+10 + self.header_size_x,
		//	self.track_size_y*(self.controller.tracks.size+1)+10+self.timerule_size.y
		//));
		//self.timerule_view = ~class_timerule_view.new(self.vlayout, self.controller, self.header_size_x, self.beat_size_x, self.timerule_size);

		//debug("timeline_view.tracks2 3");

		//self.timeline = ParaTimeline.new(self.vlayout, bounds: Rect(20, 20, self.track_size_x, self.track_size_y * controller.tracks.size));
		//self.timeline.keyDownAction = self.controller.get_main.commands.get_kb_responder(\timeline);

		//debug("timeline_view.tracks2 4");
		//self.timeline.mouseDownAction = { arg view, x, y, modifiers, buttonNumber, clickCount;
		//	var pos_x = (x/self.beat_size_x).asInteger;
		//	var pos_y = (y/self.track_size_y).asInteger;
		//	buttonNumber.debug("class_timeline_view: tracks: buttonNumber");
		//	if( (modifiers & 0x00040000) != 0) {	// == 262401 == ctrl
		//		self.controller.add_block_from_lib(pos_y, pos_x);

		//	} {
		//		switch(buttonNumber,
		//			3, {
		//				self.controller.set_start_cursor(pos_x);
		//			},
		//			1, {
		//				//self.controller.set_insert_cursor(pos_x, pos_y);
		//			}
		//		);
		//	}
		//};
		//debug("timeline_view.tracks2 5");
		//self.blocks(controller);
		//debug("timeline_view.tracks2 6");

		//tl = self.timeline;

		//debug("timeline_view.tracks 3");

		//tl.nodeTrackAction_({arg node;
		//	var old_track_index, new_track_index, old_block_index, new_block_time;
		//	var newx, newy;

		//	newx = tl.getNodeLoc(node.spritenum)[0].trunc(self.beat_size_x);
		//	newy = tl.getNodeLoc(node.spritenum)[1].trunc(self.track_size_y) + self.block_top_padding;
		//	//newx = tl.getNodeLoc(node.spritenum)[0].round(self.beat_size_x);
		//	//newy = tl.getNodeLoc(node.spritenum)[1].round(self.track_size_y) + self.block_top_padding;

		//	new_track_index = ( tl.getNodeLoc(node.spritenum)[1].trunc(self.track_size_y)/self.track_size_y ).asInteger;
		//	new_block_time = ( tl.getNodeLoc(node.spritenum)[0].trunc(self.beat_size_x)/self.beat_size_x ).asInteger;

		//	controller.timeline_score.move_block(self.block_dict[node.spritenum], new_track_index, new_block_time);
		//	tl.setNodeLoc_( node.spritenum, newx, newy );

		//});

		//self.timeline.setBackgrDrawFunc_({
		//	// play cursor
		//	var cursors_pos;
		//	//cursors_pos = (TempoClock.default.beats % 32 / 32 * self.track_size_x).asInteger;
		//	cursors_pos = self.controller.start_cursor*self.beat_size_x;
		//	Pen.color = Color.red;
		//	Pen.line(cursors_pos@0, cursors_pos@( self.track_size_y*controller.tracks.size )); Pen.stroke;

		//	// x lines
		//	(self.track_size_x/self.beat_size_x).asInteger.do{|i| 
		//		Pen.color = if(i%8==0) {
		//			Color.gray(0.2);
		//		} {
		//			Color.gray(0.8);
		//		};
		//		Pen.line((i*self.beat_size_x)@0, (i*self.beat_size_x)@( self.track_size_y*controller.tracks.size )); Pen.stroke
		//	};

		//	// y lines
		//	controller.tracks.size.do{|i| 
		//		Pen.color = Color.gray(0.2);
		//		Pen.line(0@(i*self.track_size_y), self.track_size_x@(i*self.track_size_y)); Pen.stroke
		//	};

		//});
		//self.window.view.focus(true);
		//debug("timeline_view.tracks end");

	},

	point_to_notepoint: { arg self, point;
		var np = Point(0,0);
		np.y = ((point.y - self.block_top_padding) / self.track_size_y).asInteger;
		np.x = ( point.x / self.beat_size_x ).asInteger;
		np;
	},

	make_bindings: { arg self;
		self.controller.get_main.commands.parse_action_bindings(\track_timeline, [
			[\remove_notes, {
				self.timeline.selNodes.copy.do { arg snode;
					self.controller.remove_block(self.block_dict[snode.spritenum], false);
				};
				self.controller.changed(\blocks);
			}]

		])
	},


	tracks: { arg self, controller;
		var tl;
		var stext;
		var label_size_y = self.track_size_y - 5;
		controller = controller ?? self.controller;

		if(self.timeline.notNil) {
			debug("--- $$$$$$$$$$$$$ uuuuuuuuuuuuuuuuuuuu $$$$$$$$$$$$$$$$$$$ ");
			//nil.sdklfj;
		};

		debug("timeline_view.tracks");

		stext = StaticText.new;
		stext.background = Color.white;
		stext.fixedHeight = label_size_y;
		stext.string = "timerule";
	
		//self.layout.remove;
		self.window.view.removeAll;

		self.headerlayout = VLayout.new(
			stext;
		);

		self.vlayout = VLayout.new;


		debug("timeline_view.tracks2");

		self.track_headers = controller.tracks.collect { arg track;
			stext = StaticText.new;
			if(controller.current_track == i) {

			} {
			
			};
			debug("timeline_view.tracks2 1");
			stext.background = Color.gray(0.5);
			stext.string = track.name.asString;
			stext.fixedHeight = label_size_y;
			stext;
			self.headerlayout.add(stext);

		};
		self.headerlayout.add(nil);

		debug("timeline_view.tracks2 2");

		self.timerule_view = ~class_timerule_view.new(self.vlayout, self.controller, self.header_size_x, self.beat_size_x, self.timerule_size);
		self.vlayout.add(self.timerule_view.view, 0);

		debug("timeline_view.tracks2 3");

		self.timeline = ParaTimeline.new(self.vlayout, bounds: Rect(20, 20, self.track_size_x, self.track_size_y * controller.tracks.size));
		//self.timeline = ParaTimeline.new;
		self.timeline.createNode(0,0);
		self.timeline.createNode(10,10);
		self.timeline.keyDownAction = self.controller.get_main.commands.get_kb_responder(\timeline);
		//self.timeline.userView.background = Color.red;
		self.vlayout.add(self.timeline.userView, 1);

		self.moving_notes = IdentitySet.new;

		debug("timeline_view.tracks2 4");

		self.timeline.keyDownAction = self.controller.get_kb_responder;

		self.timeline.mouseDownAction = { arg view, x, y, modifiers, buttonNumber, clickCount;
			var pos_x = (x/self.beat_size_x).asInteger;
			var pos_y = (y/self.track_size_y).asInteger;
			// FIXME: use point_to_notepoint
			self.mouse_data = ( mod: modifiers, but: buttonNumber, click: clickCount, pos: Point(pos_x, pos_y) );
			buttonNumber.debug("class_timeline_view: tracks: buttonNumber");
			~panel_mouse_responder.(modifiers, buttonNumber, clickCount, \timeline_mouse_down, (
				create_block: {
					self.controller.add_block_from_lib(pos_y, pos_x);
				},
				create_group_block: {
					self.controller.add_block_group_from_lib(pos_y, pos_x);
				},
				remove_block: {
					self.controller.remove_block_playing_at_abstime(x/self.beat_size_x, pos_y);
					//var selnodes = self.timeline.selNodes.copy;
					//if( selnodes.size == 0 ) {
					//	selnodes = [node]
					//};
					//self.timeline.selNodes.copy.do { arg snode;
					//	self.controller.remove_block(pos_x, pos_y, false);
					//	//self.timeline.deleteNode(snode.spritenum);
					//	//tl.setNodeLoc_( snode.spritenum, snode.temp.x, snode.temp.y );
					//};
				},
				add_last_block: {
					self.controller.add_last_block_copy(pos_y, pos_x);
				},
				set_start: { // misnamed, due to shared mouse bindings
					self.controller.set_start_cursor(pos_x);
				}
			));
		};
		debug("timeline_view.tracks2 5");
		self.blocks(controller);
		debug("timeline_view.tracks2 6");

		tl = self.timeline;

		debug("timeline_view.tracks 3");

		tl.nodeTrackAction_({arg node;
			var old_track_index, new_track_index, old_block_index, new_block_time;
			var newx, newy;

			//~panel_mouse_responder.(self.mouse_data[\mod], self.mouse_data[\but], self.mouse_data[\click], \timeline_on_node, (
			//	move_block: {
			//		newx = tl.getNodeLoc(node.spritenum)[0].trunc(self.beat_size_x);
			//		newy = tl.getNodeLoc(node.spritenum)[1].trunc(self.track_size_y) + self.block_top_padding;
			//		self.moving_notes.add(node.spritenum);
			//		tl.setNodeLoc_( node.spritenum, newx, newy );
			//	}
			//));

			self.timeline.selNodes.do { arg node;
			
				newx = tl.getNodeLoc(node.spritenum)[0].trunc(self.beat_size_x);
				newy = tl.getNodeLoc(node.spritenum)[1].trunc(self.track_size_y) + self.block_top_padding;
				self.moving_notes.add(node.spritenum);
				tl.setNodeLoc_( node.spritenum, newx, newy );
			}

		});

		self.timeline.mouseUpAction = {
			self.moving_notes.do { arg snum;
				var point;
				var notepoint;
				var loc = self.timeline.getNodeLoc(snum);
				point = Point(loc[0], loc[1]);

				notepoint = self.point_to_notepoint(point);
				self.controller.move_block(self.block_dict[snum], notepoint.y, notepoint.x, false);
			};
			self.controller.changed(\notes);
			self.moving_notes = IdentitySet.new;
		};

		self.timeline.setBackgrDrawFunc_({
			// play cursor
			var cursors_pos;
			//cursors_pos = (TempoClock.default.beats % 32 / 32 * self.track_size_x).asInteger;
			cursors_pos = ( self.controller.start_cursor - self.controller.get_display_range[0] ) *self.beat_size_x;
			Pen.color = Color.red;
			Pen.line(cursors_pos@0, cursors_pos@( self.track_size_y*controller.tracks.size )); Pen.stroke;

			// x lines
			(self.track_size_x/self.beat_size_x).asInteger.do{|i| 
				j = i + self.controller.get_display_range[0];
				Pen.color = if(j%8==0) {
					Color.gray(0.2);
				} {
					Color.gray(0.8);
				};
				Pen.line((i*self.beat_size_x)@0, (i*self.beat_size_x)@( self.track_size_y*controller.tracks.size )); Pen.stroke
			};

			// y lines
			controller.tracks.size.do{|i| 
				Pen.color = Color.gray(0.2);
				Pen.line(0@(i*self.track_size_y), self.track_size_x@(i*self.track_size_y)); Pen.stroke
			};

		});

		self.window.view.focus(true);
		debug("timeline_view.tracks end");

		self.layout = HLayout.new( 
			[self.headerlayout, stretch:0],
			[self.vlayout, stretch:1],
			//self.timeline.userView
		);

		self.window.layout = self.layout;

		self.layout;

	},

	blocks: { arg self;
		var spritenum = 0;
		var controller;
		debug("timeline_view: blocks");
		controller = self.controller;
		self.timeline.clearSpace;
		self.block_dict = Dictionary.new;
		//self.controller.display_range;
		controller.get_blocks.do { arg block, i;
			var uname, name, name_string;
			var pos;
			if(block.time >= self.controller.get_display_range[0] and: {
				block.time < self.controller.get_display_range[1]
			}) {
				var display_time = block.time - self.controller.get_display_range[0];

				uname = block.nodename;
				name = self.controller.get_main.get_node(uname).name;
				name_string = if(uname != name) {
					"%\n%".format(name, uname);
				} {
					"%".format(uname);
				};
				[spritenum, block].debug("timeline_view: blocks: creating block");

				pos = Point(
					display_time*self.beat_size_x,
					block.track_index*self.track_size_y+self.block_top_padding
				);
				self.timeline.createNode(pos.x, pos.y);
				self.timeline.setNodeString_(spritenum, name_string);
				self.timeline.setNodeSize_(spritenum, self.block_size_y);
				self.timeline.paraNodes[spritenum].setLen = block.sustain * self.beat_size_x;
				self.timeline.paraNodes[spritenum].temp = pos;

				self.block_dict[spritenum] = block;

				spritenum = spritenum + 1;
			}
		}
	
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

~class_timeline_score = (

	parent: ~make_notescore.(),
	tracks: List.new,

	new: { arg self, main;
		self = self.deepCopy;

		self.get_main = { arg self; main };
		self;
	},

	add_track: { arg self, name="new track";
		var track;
		track = (name:name, number:self.tracks.size);
		self.tracks.add( track );
		self.changed(\tracks);
		track;
	},

	add_block: { arg self, blockname, track_index, abstime;
		var block, id;
		block = ();
		block.time = abstime;
		block.nodename = blockname;
		block.track_index = track_index;
		block.sustain = 8;
		self.notes.add(block);
		id = block.identityHash;
		self.changed(\blocks);
		id;
	},

	remove_block_playing_at_abstime: { arg self, time, track_index;
		self.remove_notes_playing_at_abstime(time, { arg note;
		   note.track_index == track_index;

		})
	},

	move_block: { arg self, block, new_track_index, new_block_time;
		var old_block, new_block_index;
		[ block, new_track_index, new_block_time ].debug("class_timeline.move_block: block, new_track_index, new_block_time");
		block.track_index = new_track_index;
		block.time = new_block_time;

	},

	filter_by_track_index: { arg self, track_index;
		var res = self.deepCopy;
		//[slotnum, res.notes].debug("filter_by_slot: before");
		res.notes = res.notes.select { arg no;
			no.track_index == track_index
		};
		//res.notes.debug("filter_by_slot: after");
		res;
	},

);

~class_player = (

	uname: nil,
	name: nil,
	node: EventPatternProxy.new,
	kind: \player,	// should be classtype, but backward compat...
	sourcepat: nil,
	selected_param: \stepline,
	playing_state: \stop,
	muted: false,
	archive_param_data: [\control, \stepline, \adsr, \noteline, \nodeline, \sampleline, \buf],
	archive_data: [\current_mode, \effects],

	new: { arg self, main;
		self = self.deepCopy;
		//self.get_main = { arg self; main };

		self;
	},

);

~class_timeline = (

	parent: ~class_player,
	timeline_score: nil,
	start_cursor: 0,
	end_cursor: nil,
	insert_cursor: 0,
	play_cursor: 0,
	current_track: 0,
	display_range: [0,50],
	node: EventPatternProxy.new,


	new: { arg self, main;
		self = self.parent[\new].(self, main);
		self.get_main = { arg self; main };
		self.timeline_score = ~class_timeline_score.(main);
		self.bindings;

		self;
	},

	save_data: { arg self;
		var data = IdentityDictionary.new;
		data[\score] = self.timeline_score.save_data;
		data;
	},

	load_data: { arg self, data;
		self.timeline_score.load_data(data[\score]);
	},

	get_kb_responder: { arg self;
		self.get_main.commands.get_kb_responder(\track_timeline);
	},

	get_display_range: { arg self;
		self.display_range;
	},

	forward_in_timeline: { arg self;
		self.display_range = self.display_range + 1;
		self.changed(\blocks);
		self.changed(\play_cursor);
	},

	backward_in_timeline: { arg self;
		if(self.display_range[0] > 0) {
			self.display_range = self.display_range - 1;
			self.changed(\blocks);
			self.changed(\play_cursor);
		}
	},

	set_start_cursor: { arg self, pos;
		self.start_cursor = pos;	
		//self.changed(\start_cursor);
		self.changed(\redraw);
	},

	set_insert_cursor: { arg self, pos, track_index;
		self.insert_cursor = pos;	
		self.current_track = track_index;	
		self.changed(\insert_cursor);
	},

	tracks: { arg self; self.timeline_score.tracks },

	add_track: { arg self, name;
		var res;
		res = self.timeline_score.add_track(name);
		self.changed(\tracks);
		res;
	},

	add_block: { arg self, blockname, track_index, abstime, update=true;
		var res;
		res = self.timeline_score.add_block(blockname, track_index, abstime);
		if(update) {
			self.changed(\blocks);
		};
		res;
	},

	add_block_group: { arg self, blockname, track_index, abstime;
		var main = self.get_main;
		var node = main.get_node(blockname);
		var maxdur = 0;
		var childs = IdentityDictionary.new;
		//if(node.subkind == \parnode) {
			node.children.do { arg childname;
				var child;
				var dur;
				if(childname != \voidplayer) {

					childname.debug("class_timeline: add_block_group: childname");
					child = main.get_node(childname);
					dur = child.get_duration;
					maxdur = max(maxdur, dur);
					childs[childname] = dur;
				}
			};
			childs.keys.do { arg childname, i;
				( maxdur/childs[childname] ).asInteger.do { arg time;
					self.add_block(childname, track_index+i, abstime + ( time*childs[childname] ), false)
				}

			};
			self.changed(\blocks);

		//}
	},

	add_block_group_from_lib: { arg self, track_index, abstime;
		~class_node_group_chooser.new(self.get_main, { arg blockname;
			//self.last_blockname = blockname;
			self.add_block_group(blockname, track_index, abstime);
		})
	},

	add_block_from_lib: { arg self, track_index, abstime;
		~class_node_chooser.new(self.get_main, { arg blockname;
			self.last_blockname = blockname;
			self.add_block(blockname, track_index, abstime);
		})
	},

	add_last_block_copy: { arg self, track_index, abstime;
		if(self.last_blockname.notNil) {
			self.add_block(self.last_blockname, track_index, abstime);
		}
	},

	move_block: { arg self, block, new_track_index, new_block_time, refresh=true;
		var old_block, new_block_index;
		[ block, new_track_index, new_block_time ].debug("class_timeline.move_block: block, new_track_index, new_block_time");
		block.track_index = new_track_index;
		block.time = new_block_time;
		self.timeline_score.set_abs_notes(self.current_notes);
		if(refresh) {
			self.changed(\blocks);
		}
	},

	remove_block_playing_at_abstime: { arg self, time, track_index, refresh=true;
		self.timeline_score.remove_block_playing_at_abstime(time, track_index);
		if(refresh) {
			self.changed(\tracks);
		}
	},

	remove_block: { arg self, block, refresh=true;
		self.timeline_score.remove_notes_at_abstime(block.time, { arg note;
			note.track_index == block.track_index;
		});
		if(refresh) {
			self.changed(\tracks);
		}
	},

	refresh: { arg self;
		self.changed(\tracks);
	},

	get_blocks: { arg self;
		var blocks;
		//var end_cursor = if(end_cursor.isNil) {
		//	nil
		//} {
		//	self.end_cursor-self.start_cursor
		//};
		blocks = self.timeline_score.get_abs_notes(0, nil);
		self.current_notes = blocks;
		blocks.debug("class_timeline: get_blocks: blocks before");
		blocks.do { arg note;
			if(note.nodename.notNil) {
				note.sustain = (self.get_main.get_node(note.nodename).get_duration) ?? note.sustain;
			};
			note;
		};
		blocks.debug("class_timeline: get_blocks: blocks after");
		blocks;
	},

	vpattern: { arg self, track_index, start=0, dur=nil;
		var notes;
		if(track_index.isNil) {
			notes = self.timeline_score.get_rel_notes(start, dur);
		} {
			notes = self.timeline_score.filter_by_track_index(track_index).get_rel_notes(start, dur);
		};
		Pspawn(
			Pbind(
				\patternline, Pseq(notes),
				\pattern, Pfunc{ arg in; 
					var nodename = in[\patternline][\nodename];
					if(nodename.isNil) {
						\rest;
					} {
						self.get_main.get_node(nodename).vpattern;
					}
				},
				\delta, Pfunc{ arg in; 
					in[\patternline][\dur];
				},
				\method, \par,
			)
			
		)
	
	},

	play_node: { arg self;
		var esp, sc;
		var dur;
		dur = if(self.end_cursor.isNil) {
			nil
		} {
			self.end_cursor - self.start_cursor;
		};
		//if(self.play_cursor_task.notNil) {
		//	self.play_cursor_task.stop.reset;
		//};
		debug("make play_cursor_task");
		self.play_cursor_task = Task {
			self.play_cursor = self.start_cursor;
			(dur ?? ~general_sizes.safe_inf).do {
				self.changed(\play_cursor);
				self.play_cursor.debug("play cursor");
				1.wait;
				self.play_cursor = self.play_cursor + 1;
			};
		};
		if(self.node.isPlaying.not) {
			var clock;
			clock = self.get_main.play_manager.get_clock;
			self.node.source = self.vpattern(nil, self.start_cursor, dur);
			debug("playing play_cursor_task");
			self.play_cursor_task_player = self.play_cursor_task.play(clock, quant:1);
			debug("playing node");
			self.node.play(clock, quant:1);
			esp = self.node.player;
			self.esp_sc = SimpleController(esp);
			self.esp_sc.put(\stopped, {
				self.play_cursor_task_player.stop.reset;
				"class_timeline: node stoped".debug;
				self.node.stop;
				self.esp_sc.remove;
			});

		}
	},

	stop_node: { arg self;
		self.node.stop;
		self.play_cursor_task_player.stop.reset;
	},

	bindings: { arg self;
  
		self.get_main.commands.parse_action_bindings(\timeline, [

			[\play_timeline, {
				self.play_node;
			}],
			[\forward_in_timeline, {
				self.forward_in_timeline;
			}],
			[\backward_in_timeline, {
				self.backward_in_timeline;
			}],
			[\stop_timeline, {
				self.stop_node;
			}],
			[\close_timeline, {
				self.timeline_view.window.close;
				self.window = nil;
			}],
		]);
	},

	make_gui: { arg self;
		if(self.window.isNil) {
			self.timeline_view = ~class_timeline_view.new(self);	
			self.window = self.timeline_view.window;
		} {
			self.window.front;
		}
	},
	
);


