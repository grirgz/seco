(

~make_cell = { arg parent, label;
	var bt;

	bt = GUI.button.new(parent, Rect(50,50,200,30));
	bt.states = [
		[ label, Color.black, Color.white],
		[ label, Color.white, Color.black],
	];
	bt.value = 0;
	bt;

};
~make_header_cell = { arg parent, label;
	var bt;

	bt = GUI.button.new(parent, Rect(50,50,200,30));
	bt.states = [
		[ label, Color.black, ~color_scheme.header_cell],
		[ label, Color.white, Color.black],
	];
	bt.value = 0;
	bt;

};

~set_cell_label = { arg cell, label;
	var val;
	val = cell.value;
	cell.states = [
		[ label, Color.black, Color.white],
		[ label, Color.white, Color.black],
	];
	cell.value = val;
};

~in_range = { arg datalist, address;
	if(address.isNil, {
		false;
	}, {
		((address.bank * 32) + (address.x * 4) + address.y) < datalist.size
	})
};

~in_line_range = { arg datalist, address;
	if(address.isNil, {
		false;
	}, {
		((address.bank * 8) + (address.x)) < datalist.size
	})
};

~node_column_view = { arg parent, controller;

	var layout, ps_col_layout, curbank, address;
	var width = 1350;

	layout = GUI.vLayoutView.new(parent, Rect(0,0,(160),60*6));
	layout.background = ~editplayer_color_scheme.control;

	//parent.view.background = ~editplayer_color_scheme.background;
	~make_view_responder.(layout, controller, (

		redraw: { arg obj, msg, header, datalist;
			var cell;
			controller.debug("rederaw column");
			//datalist.debug("==========================datalist");
			layout.removeAll;
			//layout.focus(true);
			cell = ~make_header_cell.(layout, header);
			datalist.do { arg node;

				~make_cell.(layout, node.name);
			}
		},

		header_label: { arg obj, msg, val;
			~set_cell_label.(layout.children[0], val)
		},

		cell_label: { arg obj, msg, x, val;
			~set_cell_label.(layout.children[x+1], val)
		},

		header_selection: { arg obj, msg, selected;
			layout.children[0].value = selected;
		},

		cell_selection: { arg obj, msg, x, selected;
			layout.children[x+1].value = selected;
		}

	));
	controller.changed(\redraw, controller.name, controller.children);
};

~hmatrix_view = { arg parent, controller;

	var sl_layout, ps_col_layout, curbank, address;
	var width = 1350;

	sl_layout = GUI.hLayoutView.new(parent, Rect(0,0,width,60*6));
	sl_layout.background = ~editplayer_color_scheme.background;

	//parent.view.background = ~editplayer_color_scheme.background;
	~make_view_responder.(sl_layout, controller, (

		redraw: { arg obj, msg, datalist;
			//controller.model.debug("rederaw hmatrix");
			"".debug("rederaw hmatrix");
			datalist = controller.model.datalist;
			//datalist.debug("==========================datalist");
			sl_layout.removeAll;
			//sl_layout.focus(true);
			datalist.size.debug("datalist size");
			controller.model.bank.debug("bank");
			//~node_column_view.(sl_layout, datalist[0]);
			datalist[(controller.model.bank*8)..(((controller.model.bank+1)*8)-1)].do { arg node;
				~node_column_view.(sl_layout, node);
			};
			parent.focus(true);

		}

	))
};

~empty_player = (
	name: \void,
	kind: \player
);

~make_empty_parnode = {(
	
	children: SparseArray.newClear(8, ~empty_player),
	kind: \parnode,
	name: \void

)};


~make_parlive = { arg main;

	var obj;

	obj = (

		model: (
			//samplelist: {arg i; "sounds/default" ++ i }!50,
			//patlist: main.model.patlist,
			datalist: (8*9).collect( ~make_empty_parnode),
			selection: (x:0, y:0, bank:0),
			bank: 0,
			offset: 0@0
		),

		kb_handler: Dictionary.new,

		address_to_index: { arg self, ad;
			(ad.bank * 32) + (ad.x * 4) + ad.y;
		},

		refresh: { arg self;
			var sel = self.model.selection;
			self.changed(\redraw);
			sel.debug("refresh sel");
			if(sel.notNil, {
				if(sel.bank == self.model.bank, {
					self.set_selection(sel.x, sel.y);
				});
			});
		},

		set_bank: { arg self, idx;
			idx.debug("set bank");
			self.model.bank = idx;
			self.refresh;
		},

		set_datalist: { arg self, datalist;
			self.model.datalist = datalist;
			self.model.selection = nil;
			self.refresh;
		},

		get_cell_by_address: { arg self, ad;
			if(ad.y == -1, {
				self.model.datalist[ (ad.bank * 8) + ad.x];
			}, {
				self.model.datalist[ (ad.bank * 8) + ad.x].children[ad.y];
			})
		},

		get_selected_cell: { arg self;
			self.get_cell_by_address(self.model.selection)	
		},

		set_selection: { arg self, x, y;
			var sel, oldsel;
			sel =  (
				x: x,
				y: y,
				bank: self.model.bank
			);
			"set_selection:sel".debug(sel);
			if(
					(((sel.bank * 8) + (sel.x)) < self.model.datalist.size)
					&& { self.model.datalist[sel.bank*8 + sel.x].notNil }
					&& { sel.y < self.model.datalist[sel.bank*8 + sel.x].children.size }
				,{
					oldsel = self.model.selection;
					self.model.selection = sel;
					if(oldsel.notNil, {
						if(oldsel.y == -1, {
							self.model.datalist[oldsel.bank*8 + oldsel.x].changed(\header_selection, 0);
						}, {
							self.model.datalist[oldsel.bank*8 + oldsel.x].changed(\cell_selection, oldsel.y, 0);
						});
					});
					if(sel.y == -1, {
						self.model.datalist[sel.bank*8 + sel.x].changed(\header_selection, 1);
					}, {
						self.model.datalist[sel.bank*8 + sel.x].changed(\cell_selection, sel.y, 1);
					});
			});
		},

		set_cell: { arg self, address, val;
			var name;
			name = if(val.isNil, { "" }, {val.name});
			if(address.y == -1, {
				self.model.datalist[ address.bank * 8 + address.x ] = val;
				self.changed(\cell_label, address.x, name);
			}, {
				self.model.datalist[ address.bank * 8 + address.x ].children[address.y] = val;
				self.model.datalist[ address.bank * 8 + address.x ].changed(\cell_label, address.y, name);
			});
		},


		play_selected: { arg self;
			self.get_selected_cell.node.play	
		},

		stop_selected: { arg self;
			self.get_selected_cell.node.stop	
		},

		make_livenodename_from_libnodename: { arg self, name;
			// TODO: handle name collision
			name++"_l"++UniqueID.next;

		},

		make_newlivenodename_from_livenodename: { arg self, name;
			// TODO: make it real
			name[ .. name.findBackwards("_l")  ] ++ "l" ++ UniqueID.next;
		},

		make_livenode_from_libnode: { arg self, libnodename;
			var livenodename;
			var player;
			livenodename = self.make_livenodename_from_libnodename(libnodename);
			player = ~make_player.(main.model.patpool[libnodename]);
			player.name = livenodename;
			player;
		},

		duplicate_livenode: { arg self, livenodename;
			var newlivenodename, newlivenode, newlivenode_pdict;
			newlivenodename = self.make_newlivenodename_from_livenodename(livenodename);
			newlivenodename.debug("newlivenodename");
			livenodename.debug("livenodename");
			self.model.livenodepool.debug("livenodepool");
			self.model.livenodepool[newlivenodename] = self.model.livenodepool[livenodename].clone;
			self.model.livenodepool[newlivenodename].name = newlivenodename;
			newlivenodename;
		},

		load_libnode: { arg self;
			var livenode;
			if(self.model.selection.y == -1, {
				"Can't load libnode in header".error;
			}, {
				~choose_libnode.(main, { arg libnodename; 
					livenode = self.make_livenode_from_libnode(libnodename);
					self.set_cell(self.model.selection, livenode)
				});
			})
		},

		stop_all: { arg self;
			self.model.datalist.do { arg i; i.node.stop	 };
		},

		delete_node: { arg self;
			var livenode;
			if(self.model.selection.y == -1, {
				"Can't delete node in header".error;
			}, {
				self.set_cell(self.model.selection, nil)
			})

		},

		make_gui: { arg self, parent;
			~hmatrix_view.(parent, self);
		},

		init: { arg self;
			var parent;
			
			//self.model.datalist[8*9] = nil;
			//self.model.datalist.debug("patlist");
			main.commands.matrix_add_enable([\parlive, \select_cell], [\kb, 0], ~keycode.kbpad8x4, { arg x, y; self.set_selection(x,y) });
			main.commands.array_add_enable([\parlive, \select_header], [\kb, ~keycode.mod.alt], ~keycode.kbnumline[..7], { arg x; self.set_selection(x,-1) });

			main.commands.array_add_enable([\parlive, \change_bank], [\kb, 0], ~keycode.kbnumpad, { arg x; self.set_bank(x) });

			main.commands.add_enable([\parlive, \load_libnode], [\kb, ~keycode.mod.fx, ~keycode.kbfx[0]], { self.load_libnode });
			main.commands.add_enable([\parlive, \delete_node], [\kb, ~keycode.mod.fx, ~keycode.kbfx[2]], { self.delete_node });

			main.commands.add_enable([\parlive, \play_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[4]], { self.play_selected });
			main.commands.add_enable([\parlive, \stop_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[5]], { self.stop_selected });
			main.commands.add_enable([\parlive, \stop_all], [\kb, ~keycode.mod.fx, ~keycode.kbfx[7]], { self.stop_all });



			main.commands.add_enable([\parlive, \show_panel, \mixer], [\kb, ~keycode.mod.fx, ~keycode.kbfx[9]], { main.show_panel(\mixer) });
			main.commands.add_enable([\parlive, \show_panel, \score], [\kb, ~keycode.mod.fx, ~keycode.kbfx[10]], { main.show_panel(\score) });
			main.commands.add_enable([\parlive, \show_panel, \editplayer], [\kb, ~keycode.mod.fx, ~keycode.kbfx[11]], { main.show_panel(\editplayer) });

		}

	);
	obj.init;
	obj;

};

)



