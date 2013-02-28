
~class_nodematrix_view = (
	new: { arg self, controller;
		self = self.deepCopy;

		self.controller = { controller };

	
		self;
	},

	make_responders: { arg self;
		~make_class_responder.(self, self.layout, self.controller, [
			\selection, \parent_node,
		]);
	},

	////////////////////// responders

	selection: { arg self;
		var pos = self.controller.current_column @ self.controller.current_row;
		self.set_cell_state(pos, \normal_selected);
	},

	parent_node: { arg self;
		var parent = self.controller.parent_node;
		parent_node.children.do { arg child, i;
			self.set_column_controller(i, child)
		
		}
	

	},

	/////////////////////

	set_column_controller: { arg self, idx, ctrl;
		var subchildren;
		if(ctrl.kind == \player) {
			subchildren = child.get_arg(\noteline).get_scoreset.sheets;
		} {
			List.new;
		};
		subchildren.do {
			c
		
		}
	},

	make_cell: { arg self, label="cell";
		StaticText.new
			.string_(label)
			.background_(Color.gray)
			;
	},

	get_cell: { arg self, point;
		self.cell_matrix[point.x+1][point.y+1];
	},

	set_cell_state: { arg self, point, state;
		var color;
		color = switch(state,
			\normal, {
				Color.gray(0.1)
			},
			\armed, {
				Color.blue(0.1)
			},
			\normal_selected, {
				Color.gray(0.5)
			},
			\armed_selected, {
				Color.blue(0.5)
			}
		);
		self.get_cell(point).background = color;
	},

	set_cell_label: { arg self, point, label;
		self.get_cell(point).string = label;
	},

	make_layout: { arg self;
		var size = 8@4;
		self.cell_matrix = Array.fill(size.x+1, { arg x;
			Array.fill(size.y+1, { arg y;
				var cell;
				cell = self.make_cell;
				if(x == 0 or: { y == 0 }) {
					cell.string_("header");
					cell.background_(Color.white);
				};
				cell;
			})
		});
		self.layout = GridLayout.columns(*
			(size.x+1).collect { arg x;
				(size.y+1).collect { arg y;
					self.cell_matrix[x][y];
				}
			}
		);
		self.layout;

	},
);

~class_nodematrix_panel_view = (
	new: { arg self, controller;
		self = self.deepCopy;
		self.controller = { controller };
		self.nodematrix_view = ~class_nodematrix_view.new(controller);
	
		self;
	},

	make_layout: { arg self;
		self.layout = VLayout(
			self.nodematrix_view.make_layout;
		);
		self.layout;

	},
);

~class_nodematrix_panel = (
	current_row: 0,
	current_column: 0,
	new: { arg self;
		self = self.deepCopy;
	
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

	self.update_selection: { arg self;
		self.changed(\selection)
	},

	set_parent_node: { arg self, parent;
		self.parent_node = parent;
		self.changed(\parent_node);
	},

	make_gui: { arg self;
		self.window = Window.new("Nodematrix panel");
		self.main_view = ~class_nodematrix_panel_view.new;
		self.window.layout = self.main_view.make_layout;
		self.window.front;
	},
);
