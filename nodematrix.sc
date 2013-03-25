
~class_nodematrix_view = (
	responders: List.newClear(8), // FIXME: hardcoded
	scoreset_responders: List.newClear(8), // FIXME: hardcoded
	old_selection: 0@0,
	old_armed_cells: 0 ! 8, // FIXME: hardcoded

	new: { arg self, controller;
		self = self.deepCopy;

		self.controller = { controller };

		self.make_layout;

	
		self;
	},

	make_responders: { arg self;
		self.controller.debug("make_responders: controller");
		~make_class_responder.(self, self.responder_anchor, self.controller, [
			\selection, \parent_node,
		]);

	},

	////////////////////// responders

	selection: { arg self;
		var pos = self.controller.current_column @ self.controller.current_row;
		var old_state, state;
		self.set_cell_state(self.old_selection, self.controller.get_cell_state(self.old_selection, ""));
		self.old_selection = pos;
		//self.set_cell_state(pos, \armed_selected);
		self.set_cell_state(pos, self.controller.get_cell_state(pos, \selected));
	},

	parent_node: { arg self;
		var parent = self.controller.parent_node;
		debug("class_nodematrix_view: parent_node");
		self.responders.do { arg resp;
			resp.remove
		};
		self.scoreset_responders.do { arg resp;
			resp.remove
		};
		switch(parent.kind,
			\seqnode, {
				parent.children.do { arg child, i;
					child.debug("class_nodematrix_view: parent_node: child");
					if(child.notNil and: {child != \voidplayer}) {
						self.set_row_controller(i, self.controller.get_main.get_node(child))
					}
				};
				self.playmatrix_responder.remove;
				parent.playmatrix_manager.debug("playmatrix_responder");
				self.playmatrix_responder = ~make_class_responder.(self, self.responder_anchor, parent.playmatrix_manager, [
					\arming,
				]);
			},
			\parnode, {
				parent.children.do { arg child, i;
					child.debug("class_nodematrix_view: parent_node: child");
					if(child.notNil and: {child != \voidplayer}) {
						self.set_column_controller(i, self.controller.get_main.get_node(child))
					}
				}
			
			}
		);
	},

	arming: { arg self, ctrl, msg, pos;
		if(pos.class == Point) {
			var old_pos;
			old_pos = pos.x @ self.old_armed_cells[pos.x];
			self.set_cell_state(old_pos, self.controller.get_cell_state(old_pos));
			self.set_cell_state(pos, self.controller.get_cell_state(pos));
			self.old_armed_cells[pos.x] = pos.y;
		} {
			var old_pos;
			var row_index = pos;
			self.old_armed_cells.do { arg old_posy, posx;
				old_pos = posx @ old_posy;
				pos = posx @ row_index;
				self.set_cell_state(old_pos, self.controller.get_cell_state(old_pos));
				self.set_cell_state(pos, self.controller.get_cell_state(pos));
				self.old_armed_cells[pos.x] = pos.y;
			
			};
		}
	},

	/////////////////////

	set_row_controller: { arg self, idx, ctrl;
		var subchildren;
		var responder;
		debug("set_row_controller ***********************");
		if(ctrl.kind == \parnode) {
			responder = (
				label: {
					self.set_cell_label(Point(-1, idx), ctrl.name);
				},
				redraw: {
					subchildren = ctrl.children;
					self.controller.nodematrix_size.x.do { arg x;
						var point = Point(x, idx);
						var label;
						var node;
						var child = subchildren[x];
						if(child.isNil or: {child == \voidplayer}) {
							label = "";
						} {
							node = self.controller.get_main.get_node(child);
							label = node.name;
						};
						self.set_cell_label(point, label);
						self.set_cell_state(point, self.controller.get_cell_state(point));
						//self.old_armed_cells[y] = ctrl.get_scoreset.get_current_sheet_index;
					}
				},
				selected_child: {
					//var sheet_idx = ctrl.get_scoreset.get_current_sheet_index;
					//var point = Point(idx, sheet_idx);
					//var old_point = Point(idx, self.old_armed_cells[idx]);
					//[point, old_point].debug("class_nodematrix_view: set_column_controller: selected_scoresheet: point, old_point");
					//self.set_cell_state(old_point, self.controller.get_cell_state(old_point));
					//self.set_cell_state(point, self.controller.get_cell_state(point));
					//self.old_armed_cells[idx] = sheet_idx;
				},
			);
			//self.responders[idx].remove;
			self.responders[idx] = ~make_view_responder.(self.responder_anchor, ctrl, responder);
			//self.scoreset_responders[idx] = ~make_view_responder.(self.responder_anchor, ctrl.playmatrix_manager, responder);
			ctrl.changed(\label);
			ctrl.changed(\redraw);
			ctrl.changed(\selected_child);
		} {
			"set_column_controller: Error"
		};
	},

	set_column_controller: { arg self, idx, ctrl;
		var subchildren;
		var responder;
		if(ctrl.kind == \player) {
			responder = (
				label: {
					self.set_cell_label(Point(idx, -1), ctrl.name);
				},
				scoresheet: {
					subchildren = ctrl.get_scoreset.sheets;
					subchildren.do { arg child, y;
						var point = Point(idx, y);
						var label;
						if(child.isNil) {
							label = "";
						} {
							label = "sheet %".format(y+1);
						};
						self.set_cell_label(point, label);
						self.set_cell_state(point, self.controller.get_cell_state(point));
						self.old_armed_cells[y] = ctrl.get_scoreset.get_current_sheet_index;
					}
				},
				selected_scoresheet: {
					var sheet_idx = ctrl.get_scoreset.get_current_sheet_index;
					var point = Point(idx, sheet_idx);
					var old_point = Point(idx, self.old_armed_cells[idx]);
					[point, old_point].debug("class_nodematrix_view: set_column_controller: selected_scoresheet: point, old_point");
					self.set_cell_state(old_point, self.controller.get_cell_state(old_point));
					self.set_cell_state(point, self.controller.get_cell_state(point));
					self.old_armed_cells[idx] = sheet_idx;
				},
			);
			//self.responders[idx].remove;
			self.responders[idx] = ~make_view_responder.(self.responder_anchor, ctrl, responder);
			self.scoreset_responders[idx] = ~make_view_responder.(self.responder_anchor, ctrl.get_scoreset, responder);
			ctrl.changed(\label);
			ctrl.changed(\scoresheet);
		} {
			"set_column_controller: Error"
		};
	},

	make_cell: { arg self, label="cell";
		StaticText.new
			.string_(label)
			.background_(Color.gray)
			;
	},

	get_cell: { arg self, point;
		point.debug("class_nodematrix_view: get_cell: point");
		self.cell_matrix.size.debug("cell matrix: num col");
		self.cell_matrix.collect{ arg col; col.size }.debug("cell matrix: col sizes");
		self.cell_matrix[point.x+1][point.y+1];
	},

	set_cell_state: { arg self, point, state;
		var color;
		color = switch(state,
			\normal, {
				Color.gray(0.9)
			},
			\armed, {
				Color.new255(142, 245, 210)
			},
			\normal_selected, {
				Color.gray(0.7)
			},
			\armed_selected, {
				Color.new255(102, 205, 170)
			},
			{
				Color.red
			}
		);
		self.get_cell(point).background = color;
	},

	set_cell_label: { arg self, point, label;
		[point, label].debug("class_nodematrix_view: set_cell_label: point, label");
		self.get_cell(point).string = label;
		[point, label].debug("END class_nodematrix_view: set_cell_label: point, label");
	},

	make_layout: { arg self;
		var size = self.controller.nodematrix_size;
		debug("make_layout: *******************************");
		self.cell_matrix = Array.fill(size.x+1, { arg x;
			Array.fill(size.y+1, { arg y;
				var cell;
				cell = self.make_cell;
				if(x == 0 or: { y == 0 }) {
					//cell.string_("header");
					cell.string_("");
					cell.background_(Color.white);
				};
				cell;
			})
		});
		self.layout = GridLayout.columns(*
			(size.x+1).collect { arg x;
				(size.y+1).collect { arg y;
					if(x != 0 and: { y != 0 }) {
						//self.set_cell_label(Point(x-1, y-1), "cell");
						self.set_cell_label(Point(x-1, y-1), "");
						self.set_cell_state(Point(x-1, y-1), \normal);
					};
					self.cell_matrix[x][y];
				}
			}
		);
		self.responder_anchor = self.cell_matrix[0][0];
		self.make_responders;
		self.layout;

	},
);


~class_nodematrix_knob_row = (
	parent: ~class_knob_row,
	new: { arg self, nodematrix_controller, controllers;
		self = self.deepCopy;
		self.nodematrix_controller = { nodematrix_controller };
		self.controllers = { controllers };
		self.make_layout;
		self.make_responders;
	
		self;
	},

	make_responders: { arg self;

		~make_class_responder.(self, self.responder_anchor, self.nodematrix_controller, [
			\parent_node,
		]);
	},

	///// responders

	parent_node: { arg self;
		
	},
);

~class_nodematrix_panel_view = (
	new: { arg self, controller;
		self = self.deepCopy;
		self.controller = { controller };
		self.nodematrix_view = ~class_nodematrix_view.new(controller);
		self.knobrow_view = ~class_knob_row.new(nil ! 8);
		self.knobrow_view2 = ~class_knob_row.new(nil ! 8);
		self.make_layout;
	
		self;
	},

	make_layout: { arg self;
		self.layout = VLayout(
			self.nodematrix_view.layout,
			self.knobrow_view.layout,
			self.knobrow_view2.layout,
		);
		self.layout;

	},
);


~class_playmatrix_manager = (
	new: { arg self, main, controller, size;
		self = self.deepCopy;

		self.matrix_size = (8@8) ?? size;
		self.arming_matrix = 0 ! self.matrix_size.x;

		self.controller = { controller };
		self.get_main = { main };
		self.get_node = { arg self, name; main.get_node(name) };
	
		self;
	},

	is_stop_using_quant: { arg self;
		self.get_main.model.is_stop_using_quant
	},

	get_child_node: { arg self, pos;
		var groupname, group, nodename, node;
		if(pos.x.notNil and: {pos.y.notNil}) {
			groupname = self.controller.children[pos.y];
			if( groupname.notNil and: {groupname != \voidplayer}) {
				group = self.get_node(groupname);
				nodename = group.children[pos.x];
				if(nodename.notNil and: { nodename != \voidplayer }) {
					node = self.get_node(nodename);
				}
			};
		};
		node;
	},

	///////////// playing

	play_armed_cells: { arg self;
		var node;
		var pos;
		self.arming_matrix.do { arg y, x;
			pos = Point(x, y);
			node = self.get_child_node(pos);
			node !? { node.play_node };
		};
	},

	stop_armed_cells: { arg self, use_quant=true;
		var node;
		var pos;
		self.arming_matrix.do { arg y, x;
			pos = Point(x, y);
			node = self.get_child_node(pos);
			node !? { node.stop_node(self.is_stop_using_quant) };
		};
	},

	arm_cell: { arg self, pos;
		self.arming_matrix[pos.x] = pos.y;
		self.changed(\arming, pos);
	},

	play_cell: { arg self, pos;
		var old_playing;
		var node;
		[pos, self.arming_matrix].debug("&&&&&&&&&&&&&&&& arming matrix");
		old_playing = self.arming_matrix[pos.x];
		self.arm_cell(pos);
		self.arming_matrix.debug("&&&&&&&&&&&&&&&& arming matrix");
		Point(pos.x , old_playing).debug("play_cell =================================");
		node = self.get_child_node(Point(pos.x , old_playing));
		node !? { node.stop_node(true) };
		node = self.get_child_node(pos);
		node !? { node.play_node };
	},

	stop_cell: { arg self, pos;
		var node;
		node = self.get_child_node(pos);
		node !? { node.stop_node(self.is_stop_using_quant) };
	},

	stop_unarm_column: { arg self, col_index;
		// and unarm
		var node;
		var posy = self.arming_matrix[col_index];
		if(posy.notNil) {
			node = self.get_child_node(col_index@posy);
			node !? { node.stop_node(self.is_stop_using_quant) };
			self.arming_matrix[col_index] = nil;
			self.changed(\arming, col_index@posy)
		};
	},

	arm_row: { arg self, row_index;
		self.stop_armed_cells;
		self.arming_matrix = self.arming_matrix.collect { row_index };
		self.changed(\arming, row_index);
		self.play_armed_cells;
	},

	play_row: { arg self, row_index;
		self.arm_row(row_index);
		self.play_armed_cells;
	},

	stop_unarm_row: { arg self, row_index;
		
	},

	play_part: { arg self;
		self.play_armed_cells;
	},

	stop_part: { arg self;
		self.stop_armed_cells;
	},
);

~class_nodematrix_panel = (
	current_row: 0,
	current_column: 0,
	nodematrix_size: 8@8,
	new: { arg self, main;
		self = self.deepCopy;

		self.get_main = { main };
		self.make_bindings;
	
		self;
	},

	select_row: { arg self, y;
		self.current_row = y;
		self.update_selection;
	},

	select_column: { arg self, x;
		self.current_column = x;
		self.update_selection;
	},

	get_selection_point: { arg self;
		self.current_column @ self.current_row;
	},

	index_to_pos: { arg self, index;
		var x = index % self.nodematrix_size.x;
		var y = (index / self.nodematrix_size.x).asInteger;
		x@y;
	},

	select_cell_by_index: { arg self, index;
		var pos = self.index_to_pos(index);
		self.current_column = pos.x;
		self.current_row = pos.y;
		self.update_selection;
	},

	get_group_node: { arg self, index;
		self.get_main.get_node(self.parent_node.children[index]);
	},

	get_cell: { arg self, pos;
		self.get_group_node(pos.x).get_scoreset.get_sheet(pos.y);
	},

	///////////// playing

	arm_cell: { arg self, pos;
		switch(self.parent_node.kind,
			\seqnode, {
				var main = self.get_main;
				//main.get_node(self.parent_node.children[pos.y]).children[pos.x]
			},
			\parnode, {
				var ss;
				ss = self.get_group_node(pos.x).get_scoreset;
				if(ss.notNil) {
					ss.select_sheet(pos.y);
				}
			}
		);
	},

	play_cell: { arg self, pos;
		switch(self.parent_node.kind,
			\seqnode, {
				self.parent_node.playmatrix_manager.play_cell(pos);
			},
			\parnode, {
				self.arm_cell(pos);
				self.get_group_node(pos.x).play_node;
			}
		);
	},

	stop_cell: { arg self, pos;
		switch(self.parent_node.kind,
			\seqnode, {
				self.parent_node.playmatrix_manager.stop_unarm_column(pos.x);
			},
			\parnode, {
				//self.parent_node.playmatrix_manager.stop_unarm_column(pos.x);
				self.get_group_node(pos.x).stop_node;
			}
		);
	},

	arm_row: { arg self, row_index;
		switch(self.parent_node.kind,
			\seqnode, {
			},
			\parnode, {
				self.parent_node.children.do { arg child;
					child.debug("arm_row: child");
					if(child != \voidplayer) {
						self.get_main.get_node(child).get_scoreset.select_sheet(row_index);
					}
				}
			}
		);
	},

	play_row: { arg self, row_index;
		switch(self.parent_node.kind,
			\seqnode, {
				self.parent_node.playmatrix_manager.play_row(row_index);
			},
			\parnode, {
				self.arm_row(row_index);
				self.parent_node.children.do { arg child;
					child.debug("play_row: child");
					if(child != \voidplayer) {
						self.get_main.get_node(child).play_node;
					}
				}
			}
		);
	},

	play_part: { arg self;
		switch(self.parent_node.kind,
			\seqnode, {
				self.parent_node.playmatrix_manager.play_part;
			},
			\parnode, {
				self.parent_node.children.do { arg child;
					if(child != \voidplayer) {
						self.get_main.get_node(child).play_node;
					}

				}
			}
		);
	},

	stop_part: { arg self;
		switch(self.parent_node.kind,
			\seqnode, {
				self.parent_node.playmatrix_manager.stop_part;
			},
			\parnode, {
				self.parent_node.children.do { arg child;
					if(child != \voidplayer) {
						self.get_main.get_node(child).stop_node;
					}
				}
			}
		);
	},

	//////////////////////////// polymorphic functions


	////////////////////////////

	get_cell_state: { arg self, pos, selected;
		var state = \normal;
		var scoreset;
		var nodename;
		var node;
		nodename = self.parent_node.children[pos.x];
		if(nodename.notNil and: {nodename != \voidplayer}) {
			node = self.get_main.get_node(nodename);
			switch(node.kind,
				\player, {
					scoreset = node.get_scoreset;
					if(scoreset.is_sheet_selected(pos.y)) {
						state = \armed;
					} {
						state = \normal;
					};
				},
				\parnode, {
					if(self.parent_node.playmatrix_manager.arming_matrix[pos.x] == pos.y) {
						state = \armed;
					} {
						state = \normal
					}
				}
			);
		} {
			state = \normal;
		};
		switch(selected, 
			nil, {
				if(pos == self.get_selection_point) {
					state = "%_%".format(state, \selected).asSymbol
				}
			},
			\selected, {
				state = "%_%".format(state, selected).asSymbol
			}
		);
		state;
	},

	update_selection: { arg self;
		self.changed(\selection)
	},

	set_parent_node: { arg self, parent;
		self.parent_node = parent;
		if(self.parent_node.playmatrix_manager.isNil) {
			self.parent_node.playmatrix_manager = ~class_playmatrix_manager.new(self.get_main, parent, self.nodematrix_size);
		};
		self.changed(\parent_node);
	},

	make_bindings: { arg self;
		self.get_main.commands.parse_action_bindings(\nodematrix, 
			self.get_main.panels.side.get_windows_bindings ++ [
				[\select_cell, 32, { arg i;
					self.select_cell_by_index(i)
				}],

				[\select_row, 8, { arg i;
					self.select_row(i)
				}],

				[\select_column, 8, { arg i;
					self.select_column(i)
				}],

				["play_cell", { 
					self.play_cell(self.get_selection_point);
				}],

				["stop_cell", { 
					self.stop_cell(self.get_selection_point);
				}],


				["play_row", { 
					self.play_row(self.get_selection_point.y);
				}],

				["play_part", { 
					self.play_part;
				}],

				["stop_part", { 
					self.stop_part;
				}],

				["panic", { 
					self.get_main.panic;
				}],

				[\close_window, {
					self.window.close;
				}],
			]
		);
	
	},

	refresh: { arg self;
		self.changed(\parent_node);
	},

	make_gui: { arg self;
		Task {
			self.window = Window.new("Nodematrix panel");
			self.main_view = ~class_nodematrix_panel_view.new(self);
			self.window.layout = self.main_view.layout;
			self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\nodematrix);
			self.window.front;
		}.play(AppClock)
	},
);
