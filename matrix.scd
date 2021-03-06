


~make_matrix_cell = { arg parent, label;
	var bt;

	bt = GUI.button.new(parent, Rect(0,0,160,30));
	bt.states = [
		[ label, Color.black, Color.white],
		[ label, Color.white, Color.black],
	];
	bt.font_(Font("Arial",11));
	bt.value = 0;

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

~matrix_cell_in_range = { arg datalist, address;
	if(address.isNil, {
		false;
	}, {
		((address.bank * 32) + (address.y * 8) + address.x) < datalist.size
	})
};

~matrix_view = { arg parent, controller, matrix_size;

	var sl_layout, ps_col_layout, curbank, address;
	var width = 1050;

	matrix_size = matrix_size ?? (8@4);

	//sl_layout = GUI.hLayoutView.new(parent, Rect(0,0,width,60*6));
	sl_layout = GUI.vLayoutView.new(parent, Rect(0,0,1400,60*6));

	//parent.view.background = ~editplayer_color_scheme.background;
	~make_view_responder.(sl_layout, controller, (

		redraw: {
			controller.model.debug("rederaw");
			sl_layout.removeAll;
			sl_layout.focus(true);
			//controller.model.datalist.clump(matrix_size.y).clump(matrix_size.x)[controller.model.bank].do { arg col;
			controller.model.datalist.clump(matrix_size.x).clump(matrix_size.y)[controller.model.bank].do { arg col;
				//ps_col_layout = GUI.vLayoutView.new(sl_layout, Rect(0,0,(160),60*6));
				ps_col_layout = GUI.hLayoutView.new(sl_layout, Rect(0,0,11,30));
				ps_col_layout.background = ~editplayer_color_scheme.control;

				col.do { arg label;
					if(controller[\data_to_string].notNil) { // the if is for backward compat
						label = controller.data_to_string(label)
					};
					~make_matrix_cell.(ps_col_layout, label);
				};
			}
		},

		cell: { arg obj, msg, ad, label;
			
			if(~matrix_cell_in_range.(controller.model.datalist, ad), {
				~set_cell_label.(sl_layout.children[ad.y].children[ad.x], label)
			});

		},

		selection: { arg obj, msg, oldsel;
			var sel = controller.model.selection;

			sel.debug("selection");
			~matrix_cell_in_range.(controller.model.datalist, sel).debug("selection in range");

			if(~matrix_cell_in_range.(controller.model.datalist, oldsel), {
				sl_layout.children[oldsel.y].children[oldsel.x].value = 0;
			});
			if(~matrix_cell_in_range.(controller.model.datalist, sel), {
				sl_layout.children[sel.y].children[sel.x].value = 1;
			});
		}
		

	))
};


~class_matrix_chooser = (
	// TODO: rewrite that shit
	// datalist should be raw object and view should use function data_to_string
	// selected should have args : data, address
	// action should have args: data, address, idx
	new: { arg self, action, winname="Matrix";
		var parent;
		self = self.deepCopy;
		"zarb".debug("oui");
		self.window = parent = Window.new(winname, Rect(100,Window.screenBounds.height-400, 1320,300));
		
		self.action = action;
		self.model.patlist.debug("patlist");
		
		//parent.view.keyDownAction = { arg view, char, modifiers, u, k; 
		//	u.debug("slooooooooooooo u");
		//	modifiers.debug("slooooooooooooo modifiers");
		//	//self.kb_handler.debug("kb");
		//	self.kb_handler[[modifiers,u]].value
		//};
		parent.view.keyDownAction = ~shortcut.get_kb_responder(\matrix_chooser); // FIXME: should not use global var
		"zarb".debug("oui2");

		//self.set_bindings;
		self.make_bindings;

		self;
	},

	set_window_name: { arg self, name;
		self.window.name = name;
	},

	model: (
		datalist: [],
		selection: nil, // is an address
		bank: 0,
		matrix_size: 8@4,
	),

	kb_handler: Dictionary.new,

	address_to_index: { arg self, ad;
		self.coor_to_index(ad.x, ad.y, ad.bank);
	},

	coor_to_index: { arg self, x, y, bank;
		(bank * self.model.matrix_size.x * self.model.matrix_size.y) + (y * self.model.matrix_size.x) + x;
	},

	refresh: { arg self;
		self.changed(\redraw);
	},

	set_bank: { arg self, idx;
		self.model.bank = idx;
		self.changed(\redraw);
	},

	set_datalist: { arg self, datalist;
		self.model.datalist = datalist;
		self.model.selection = nil;
		self.changed(\redraw);
	},

	get_cell_xy: { arg self, x, y;
		self.model.datalist[ self.coor_to_index(x, y, self.model.bank) ];
	},

	get_cell_by_address: { arg self, ad;
		// deprecated
		self.model.datalist[ self.address_to_index(ad) ];
	},

	get_data_by_address: { arg self, ad;
		self.model.datalist[ self.address_to_index(ad) ];
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
		if(~matrix_cell_in_range.(self.model.datalist, sel), {
			oldsel = self.model.selection;
			self.model.selection = sel;
			self.changed(\selection, oldsel);
			self.selected(self.get_data_by_address(sel), sel);
		});
	},

	set_cell: { arg self, address, val;
		self.model.datalist[ self.address_to_index(address) ] = val;
		self.changed(\cell, address, val)
	},

	bindings_list: { arg self;
		[

			["edit_name", {
				if( self.model.selection.notNil ) {
					self.edit_value;
				}
			}],

			["play_selected", {
				var sel = self.model.selection;
				if( sel.notNil ) {
					self.play_selection(self.get_cell_by_address(sel), self.window, sel);
				}
			}],

			["select_bank", 9, { arg idx;
				idx = idx.clip(1,9);
				self.set_bank(idx);
			}],

			["remove_selected", {
				if(self.model.selection.isNil, {
					"No selection to load".error;
				}, {
					self.remove_selected(self.get_selected_cell)
				});
			}],


			["load_selected", {
				if(self.model.selection.isNil, {
					"No selection to load".error;
				}, {
					self.load(self.get_selected_cell, self.window)
				});
			}],

			["stop_selected", {
				var sel = self.model.selection;
				if( sel.notNil ) {
					self.stop_selection(self.get_cell_by_address(sel), self.window, sel);
				}
			}],

			["create_batch", {
				if( self.model.selection.notNil ) {
					self.create_batch;
				}
			}],

			["select_cell", 32, { arg i;
				self.set_selection(i%8, (i/8).trunc);
			}],

			["select_column", 9, { arg i;
				i = i.clip(1,9)-1;
				self.set_selection(i, self.model.selection.y);
			}],

			["select_row", 8, { arg i;
				i = i.clip(0,8);
				self.set_selection(self.model.selection.x, i);
			}],


			["close_window", {
				self.stop_selection;
				self.window.close;
			}],
		]
	},

	make_bindings: { arg self;
		//FIXME: should not use global var
		~shortcut.parse_action_bindings(\matrix_chooser,
			self.bindings_list
		);
	},

	set_bindings2: { arg self;
		~kbpad8x4.do { arg line, iy;
			line.do { arg key, ix;
				self.kb_handler[[0, key]] = { 
					self.set_selection(ix, iy);
				};
			}
		};

		~kbnumpad.do { arg keycode, idx;
			self.kb_handler[[0, keycode]] = { self.set_bank(idx) };
		};

		self.kb_handler[[~keycode.mod.alt, ~keycode.kbaalphanum["r"]]] = { 
			if( self.model.selection.notNil ) {
				self.edit_value;
			}
		};

		self.kb_handler[[~keycode.mod.alt, ~keycode.kbaalphanum["c"]]] = { 
			if( self.model.selection.notNil ) {
				self.create_batch;
			}
		};

		self.kb_handler[[~keycode.mod.fx, ~keycode.kbfx[4]]] = { 
			var sel = self.model.selection;
			if( sel.notNil ) {
				self.play_selection(self.get_cell_by_address(sel), self.window, sel);
			}
		};
		self.kb_handler[[~keycode.mod.fx, ~keycode.kbfx[5]]] = { 
			var sel = self.model.selection;
			if( sel.notNil ) {
				self.stop_selection(self.get_cell_by_address(sel), self.window, sel);
			}
		};

		self.kb_handler[[0, ~kbspecial.escape]] = { 
			self.stop_selection;
			self.window.close;
		};
		self.kb_handler[[~modifiers.fx, ~kbfx[0]]] = { 
			if(self.model.selection.isNil, {
				"No selection to load".error;
			}, {
				self.load(self.get_selected_cell, self.window)
			});
		};
	},

	show_window: { arg self;

		~matrix_view.(self.window, self, self.model.matrix_size);
		self.window.front;

	},

	// to be overloaded

	data_to_string: { arg self, data;
		data.asString;
	},

	selected: { arg self, data, address;
		var newname;
		var idx;
		idx = self.address_to_index(address);
		if(idx.notNil && {self.oldsel == idx}, {
			self[\action].(data, address, idx);
			self.stop_selection;
			self.window.close;
		}, {
			self.oldsel = idx;	
		});
	},

);

~class_samplekit_chooser = (
	parent: ~class_matrix_chooser,
	new: { arg self, main, action;
		self = self.parent[\new].(self, action, "Choose samplekit");

		self.set_datalist( main.samplekit_manager.get_samplekit_bank.keys.asArray );
		self.show_window;
		self;
	},
);


~class_sample_chooser = (
	parent: ~class_matrix_chooser,
	new: { arg self, main, action, samplekit=\default, player, group, param_name=\bufnum;
		var samplelist = List.new;
		self = self.parent[\new].(self, action, "Choose sample");

		self.samples = Dictionary.new;
		self.get_main = { arg self; main };
		self.player = player;
		self.nodegroup = group;
		self.nodegroup.identityHash.debug("class_sample_chooser: init: nodegroup: identityHash");
		self.param_name = param_name;

		//main.samplekit_manager.get_samplelist_from_samplekit(samplekit).do { arg sam;
		//	self.samples[PathName.new(sam).fileName] = sam;
		//	samplelist.add(PathName.new(sam).fileName);
		//};
		self.set_datalist(
			//samplelist 
			main.samplekit_manager.get_samplelist_from_samplekit(samplekit)
		);
		self.show_window;
		self;
	},

	data_to_string: { arg self, data;
		PathName.new(data).fileName
	},

	play_selection: { arg self, sel, win, ad;
		var pl;
		//pl = main.get_node(main.model.presetlib[defname][sl.address_to_index(ad)]);
		[self.samples[sel], sel].debug("play_selection: sel");
		{
			var buf;
			buf = Buffer.read(s, self.samples[sel]);
			s.sync;
			//TODO: use player.vpiano
			Synth(\monosampler, [\bufnum, buf, \amp, self.player.get_arg(\amp).get_val]).onFree {
				buf.free;
			};
		}.fork;
	},

	create_batch: { arg self;
		var sel = self.get_selected_cell;
		var newnode;
		self.nodegroup.identityHash.debug("class_sample_chooser: nodegroup: identityHash");
		newnode = self.get_main.node_manager.duplicate_livenode(self.player.uname);
		self.get_main.get_node(newnode).get_arg(self.param_name).set_val(self.samples[sel]);
		self.nodegroup.add_children(newnode);
		self.nodegroup.uname.debug("class_sample_chooser: create_batch: nodegroup");
	}

);

~class_node_chooser = (
	parent: ~class_matrix_chooser,

	new: { arg self, main, song_address, action;
	//new: { arg self, main, action;
		self = self.parent[\new].(self, action, "");
		self.name = "Choose node";

		self.model.matrix_size = 8@8;
		self.model.song_address = song_address;
		self.update_window_name;
		self.get_main = { arg self; main };
		self.update_datalist;
		self.show_window;
		self;
	},

	update_window_name: { arg self;
		self.set_window_name("% - part:%, sect:%, var:%".format(
			self.name,
			self.model.song_address.part,
			self.model.song_address.section,
			self.model.song_address.variant,
		));
	},

	set_bank: { arg self, idx;
		self.set_variant(idx)
	},

	set_section: { arg self, val;
		//if(val > 0) {
		//	val = val - 1;
			self.model.song_address.section = val;
			self.update_datalist;
		//}
	},

	set_variant: { arg self, val;
		//if(val > 0) {
		//	val = val - 1;
			self.model.song_address.variant = val;
			self.update_datalist;
		//}
	},

	set_part: { arg self, val;
		//if(val > 0) {
		//	val = val - 1;
			self.model.song_address.part = val;
			self.update_datalist;
		//}
	},

	bindings_list: { arg self;
		~class_matrix_chooser[\bindings_list].(self) ++ [
			[\select_section, 9, { arg idx;
				self.set_section(idx)
			}],

			//[\select_variant, 9, { arg idx; // use select_bank instead
			//	self.set_variant(idx)
			//}],

			[\select_part, 9, { arg idx;
				self.set_part(idx)
			}],
		]
		
	},

	selected: { arg self, data, address;
		var newname;
		var idx;
		idx = self.address_to_index(address);
		if(data != "" and: { idx.notNil && {self.oldsel == idx}}, {
			self[\action].(data, address, idx);
			self.stop_selection;
			self.window.close;
		}, {
			self.oldsel = idx;	
		});
	},

	data_to_string: { arg self, data;
		if(data == "") {
			""
		} {
			var node = self.get_main.get_node(data);
			if(node.name != node.uname) {
				"%\n%".format(node.name, node.uname);
			} {
				data
			}
		}
	},

	update_datalist: { arg self;
		self.update_window_name;
		self.set_datalist( 
			self.get_main.panels.side.song_manager
				.get_section_matrix(self.model.song_address.part-1, self.model.song_address.section-1)
				.collect{ arg name; if(name == \voidplayer) { "" } {name} }
		);
	},

	update_datalist_TODO: { arg self;
		var datalist = List.new;
		var x = self.model.matrix_size.x + 1; // +1 because sparsearray bug
		var y = self.model.matrix_size.y + 1; // +1 because sparsearray bug
		var children;
		self.model.song_address.debug("class_node_group_chooser.update_datalist: address");
		self.update_window_name;
		children = self.get_main.panels.side.song_manager.get_path(
				[self.model.song_address.part,self.model.song_address.section,self.model.song_address.variant],
				false
		//).children.keep(8).do { arg childname;
		).children.do { arg childname;
			var node;
			if(childname == \voidplayer) {
				datalist = datalist ++ (\voidplayer ! (x-1)); // sparsearray bug
			} {
				node = self.get_main.get_node(childname);
				if(node.children.notNil) {

					[childname, node.children.keep(x).size, node.children].debug("childname");
					datalist = datalist ++ node.children.keep(x);
				}
			}
		};
		datalist = datalist.collect{ arg name; if(name == \voidplayer) { "" } {name} };
		self.set_datalist(datalist); 
	},
);

~class_node_group_chooser = (
	parent: ~class_matrix_chooser,

	new: { arg self, main, song_address, action;
		self = self.parent[\new].(self, action, "");
		self.name = "Choose group node";

		self.model.song_address = song_address;
		self.update_window_name;
		self.model.matrix_size = 8@8;
		self.get_main = { arg self; main };
		self.update_datalist;
		self.show_window;
		self;
	},

	update_window_name: { arg self;
		self.set_window_name("% - part:%, sect:%, var:%".format(
			self.name,
			self.model.song_address.part,
			self.model.song_address.section,
			self.model.song_address.variant,
		));
	},

	set_bank: { arg self, idx;
		self.set_variant(idx)
	},

	set_section: { arg self, val;
		//if(val > 0) {
		//	val = val - 1;
			self.model.song_address.section = val;
			self.update_datalist;
		//}
	},

	set_variant: { arg self, val;
		//if(val > 0) {
		//	val = val - 1;
			self.model.song_address.variant = val;
			self.update_datalist;
		//}
	},

	set_part: { arg self, val;
		//if(val > 0) {
		//	val = val - 1;
			self.model.song_address.part = val;
			self.update_datalist;
		//}
	},

	bindings_list: { arg self;
		~class_matrix_chooser[\bindings_list].(self) ++ [
			[\select_section, 9, { arg idx;
				self.set_section(idx)
			}],

			//[\select_variant, 9, { arg idx; // use select_bank instead
			//	self.set_variant(idx)
			//}],

			[\select_part, 9, { arg idx;
				self.set_part(idx)
			}],
		]
		
	},

	update_datalist: { arg self;
		var datalist = List.new;
		var x = self.model.matrix_size.x + 1; // +1 because sparsearray bug
		var y = self.model.matrix_size.y + 1; // +1 because sparsearray bug
		var children;
		self.model.song_address.debug("class_node_group_chooser.update_datalist: address");
		self.update_window_name;
		children = self.get_main.panels.side.song_manager.get_path(
				[self.model.song_address.part,0,0],
				false
		//).children.keep(8).do { arg childname;
		).children.do { arg childname;
			var node;
			if(childname == \voidplayer) {
				datalist = datalist ++ (\voidplayer ! (x-1)); // sparsearray bug
			} {
				node = self.get_main.get_node(childname);
				[childname, node.children.keep(x).size, node.children].debug("childname");
				datalist = datalist ++ node.children.keep(x);
			}
		};
		datalist = datalist.collect{ arg name; if(name == \voidplayer) { "" } {name} };
		self.set_datalist(datalist); 
	},
);

~class_symbol_chooser = (

	parent: ~class_matrix_chooser,
	my_datalist: [
		\void
	],

	new: { arg self, main, list, action, current;
		self = ~class_matrix_chooser[\new].(self, action, "Choose kind");

		self.get_main = { arg self; main };

		// skip chooser and just toggle
		if(current.notNil and: {list.size == 2}) {
			if(current == list[0]) {
				action.(list[1])
			} {
				action.(list[0])
			}
		} {
			self.my_datalist = list ?? self.my_datalist;
			self.set_datalist(self.my_datalist);
			self.show_window;
		};
		self;

	},

);

~class_inlinegen_node_chooser = (
	parent: ~class_symbol_chooser,
	my_datalist: { arg self;
		debug("class_inlinegen_node_chooser: my_datalist");
		self.get_main.model.inlinegenlib;
	},
);

~class_inlinefx_node_chooser = (
	parent: ~class_symbol_chooser,
	my_datalist: { arg self;
		debug("class_inlinefx_node_chooser: my_datalist");
		self.get_main.model.inlinefxlib;
	},
);

~class_player_mode_chooser = (

	parent: ~class_matrix_chooser,
	my_datalist: [
		\stepline,
		\noteline,
		\scoreline,
		\sampleline,
	],
	new: { arg self, main, action;
		self = self.parent[\new].(self, action, "Choose kind");

		self.get_main = { arg self; main };
		self.set_datalist(self.my_datalist);
		self.show_window;
		self;
	},

);

~class_midi_global_bind_chooser = (

	parent: ~class_matrix_chooser,
	my_datalist: [
		\stepline,
		\noteline,
		\scoreline,
		\sampleline,
	],
	new: { arg self, main, action, node, param, modslot;
		var datalist;
		var binman;
		self = self.parent[\new].(self, action, "Choose kind");

		self.get_main = { arg self; main };
		self.get_node = { node };
		self.get_param = { param };
		self.get_modslot = { modslot };
		binman = main.midi_bindings_manager;
		debug("class_midi_global_bind_chooser.new1");
		//self.my_datalist = binman.get_global_binding_list;
		self.stringlist = binman.get_global_binding_list.collect{ arg key; binman.param_key_to_string(key) };
		debug("class_midi_global_bind_chooser.new2");
		datalist = (0..7);
		self.set_datalist(datalist);
		self.show_window;
		debug("class_midi_global_bind_chooser.new3");
		self;
	},

	data_to_string: { arg self, key;
		key.debug("class_midi_global_bind_chooser.data_to_string");
		self.stringlist[key]
		//var binman = self.get_main.midi_bindings_manager;
		//binman.param_key_to_string(key)
	},

	selected: { arg self, data, address;
		//sel.dump.debug("selected");
		var sel = data;
		if(sel != "" and: {self.oldsel == sel}) {
			var path;
			self[\action].(sel);

			path = [self.get_node.uname, self.get_param.name];
			if(self.get_modslot.notNil) {
				path = path.add(self.get_modslot);
			};

			self.get_main.midi_bindings_manager.bind_global_param(path, sel); //FIXME: abs_name ?

			self.window.close;
		} {
			self.oldsel = sel;	
		};
	},

);

~class_param_kind_chooser = (

	parent: ~class_matrix_chooser,
	//my_datalist: [\scalar, \seq, \seg, \modulation, \synchrone, \bus, \recordbus, \pkey],
	my_datalist: ~class_player_display.param_types.param_kinds,
	//my_datalist: ~class_player_display.param_types.param_kinds, // FIXME: should be defined in one place

	new: { arg self, main, action;
		self = self.parent[\new].(self, action, "Choose kind");

		self.get_main = { arg self; main };
		self.set_datalist(self.my_datalist);
		self.show_window;
		self;
	},

);

~class_player_preset_chooser = (
	
	parent: ~class_matrix_chooser,
	new: { arg self, main, player, name, kind, action;
		var datalist;
		self = self.parent[\new].(self, action, name);

		self.liblist = main.model.presetlib[player.instrname];

		datalist = main.model.presetlib[player.instrname];
		if( datalist.isNil ) {
			main.model.presetlib[player.instrname] = SparseArray.newClear(4*8*10, \empty);
			datalist = main.model.presetlib[player.instrname];
		};
		datalist = datalist.collect { arg d; 
			if( d == \empty ) { d } { d.name }
		};

		if(kind == \save) {
			self[\selected] = self[\selected_and_rename];
		};

		self.get_main = { arg self; main };
		self.get_player = { player };
		self.set_datalist(datalist);
		self.show_window;
		self;
	},

	//data_to_string: { arg self, data;
	//	if( data == \empty ) { data } { data.name }
	//},

	play_selection: { arg self;
		var sel = self.model.selection;
		var offset;
		var player;
		offset = self.address_to_index(self.model.selection);
		if(self.current_player.notNil) {
			self.stop_selection;
		};
		self.current_player = ~make_player.(self.get_main, self.liblist[offset].instrname);
		self.current_player.load_data(self.liblist[offset]);
		self.current_stream_player = self.current_player.vpattern.play;
	},

	stop_selection: { arg self;
		if(self.current_player.notNil) {
			self.current_stream_player.stop;
			self.current_player.destructor;
			self.current_player = nil;
		} {

		}
		
	},

	remove_selected: { arg self;
		var player = self.get_player;
		var offset;
		var main = self.get_main;
		self.set_cell(self.model.selection, \empty);
		offset = self.address_to_index(self.model.selection);
		if(main.model.presetlib[player.instrname][offset] != \empty) {
			main.model.presetlib[player.instrname][offset] = \empty;
			main.save_presetlib;
		};
	},

	//selected: { arg self, sel, win, address;
	//	var offset;
	//	offset = self.address_to_index(self.model.selection);
	//	[self.oldsel, sel].debug("selected: oldsel, sel");
	//	if(self.oldsel == sel, {
	//		self[\action].(sel, offset);
	//		win.close;
	//	}, {
	//		self.oldsel = sel;	
	//	});
	//},

	selected_and_rename: { arg self, data, address;
		var newname;
		var idx;
		idx = self.address_to_index(address);
		if(idx.notNil && {self.oldsel == idx}, {
			// confirm by rename
			var oldname = self.get_selected_cell.asString; //should use data_to_string ?
			if(oldname == "empty") {
				oldname = self.get_player.name
			};
			~edit_value.(oldname, { arg newname; 
				//self.set_cell(self.model.selection, newname);
				self.stop_selection;
				self.window.close;
				self[\action].(newname, address, idx);
			})
		}, {
			self.oldsel = idx;	
		});
	},

	edit_value: { arg self;
		var player = self.get_player;
		var offset;
		var main = self.get_main;
		// rename action
		~edit_value.(self.get_selected_cell.asString, { arg newname; 
			self.set_cell(self.model.selection, newname);
			offset = self.address_to_index(self.model.selection);
			if(main.model.presetlib[player.instrname][offset] != \empty) {
				main.model.presetlib[player.instrname][offset].name = newname;
				main.save_presetlib;
			};
		})
	},

);

~class_scoresheet_chooser = (

	parent: ~class_matrix_chooser,
	my_datalist: [\scalar, \seq, \seg, \modulation, \synchrone, \bus, \recordbus, \pkey],
	//my_datalist: ~class_player_display.param_types.param_kinds, // FIXME: should be defined in one place

	new: { arg self, main, player, action;
		var scoreset;
		var sheets;
		var datalist;
		self = self.parent[\new].(self, action, "Choose kind");

		scoreset = player.get_scoreset;

		self.get_main = { arg self; main };

		sheets = scoreset.get_sheets;
		datalist = sheets.collect { arg sh, i;
			if(sh.isNil) {
				"sheet % (void)".format(i+1)
			} {
				"sheet %".format(i+1)
			}
		};

		self.set_datalist(datalist);
		self.show_window;
		self;
	},

);

~class_libnode_chooser = (
	parent: ~class_matrix_chooser,
	new: { arg self, main, action;
		var my_datalist;
		self = ~class_matrix_chooser[\new].(self, action, "Choose libnode");

		self.get_main = { main };
	
		my_datalist = main.model.nodelib;
		self.set_bank(main.node_manager.model.libnode_chooser_bank ?? 0);
		
		self.set_datalist(my_datalist);
		self.show_window;
	
		self;
	},

	bindings_list: { arg self;
		~class_matrix_chooser[\bindings_list].(self) ++ [
			["select_column", 9, { arg idx;
				idx = idx.clip(1,9) - 1;
				self.get_main.node_manager.model.libnode_chooser_bank = idx;
				self.set_bank(idx);
			}],
		]
		
	},
);

/////////////////////// old matrix code

~make_matrix = { arg main, callbacks, winname="Matrix";

	var obj;

	obj = (

		model: (
			//samplelist: {arg i; "sounds/default" ++ i }!50,
			//patlist: main.model.patlist,
			datalist: [],
			selection: nil,
			bank: 0
		),

		kb_handler: Dictionary.new,

		address_to_index: { arg self, ad;
			(ad.bank * 32) + (ad.y * 8) + ad.x;
		},

		refresh: { arg self;
			self.changed(\redraw);
		},

		set_bank: { arg self, idx;
			self.model.bank = idx;
			self.changed(\redraw);
		},

		set_datalist: { arg self, datalist;
			self.model.datalist = datalist;
			self.model.selection = nil;
			self.changed(\redraw);
		},

		get_cell_xy: { arg self, x, y;
			var ad = (
				x: x, y: y, bank: self.model.bank
			);
			self.model.datalist[ self.address_to_index(ad) ];
		},

		get_cell_by_address: { arg self, ad;
			self.model.datalist[ self.address_to_index(ad) ];
		},

		get_selected_cell: { arg self;
			self.get_cell_by_address(self.model.selection)	
		},

		set_selection: { arg self, x, y;
			var ad, oldsel;
			ad =  (
				x: x,
				y: y,
				bank: self.model.bank
			);
			ad.debug("set_selection");
			~matrix_cell_in_range.(self.model.datalist, ad).debug("set_selection: in range");
			if(~matrix_cell_in_range.(self.model.datalist, ad), {
				oldsel = self.model.selection;
				self.model.selection = ad;
				self.changed(\selection, oldsel);
				callbacks.selected(self.get_cell_by_address(ad), self.window, ad);
			});
		},

		set_cell: { arg self, address, val;
			self.model.datalist[ self.address_to_index(address) ] = val;
			self.changed(\cell, address, val)
		},

		choose_cell: { arg self, action;
			~kbpad8x4.do { arg line, iy;
				line.do { arg key, ix;
					self.kb_handler[[0, key]] = { 
						self.set_selection(ix, iy);
					};
				}
			};

			~kbnumpad.do { arg keycode, idx;
				self.kb_handler[[0, keycode]] = { self.set_bank(idx) };
			};

			self.kb_handler[[~keycode.mod.alt, ~keycode.kbaalphanum["r"]]] = { 
				if( self.model.selection.notNil ) {
					callbacks.edit_value;
				}
			};

			self.kb_handler[[~keycode.mod.fx, ~keycode.kbfx[4]]] = { 
				var sel = self.model.selection;
				if( sel.notNil ) {
					callbacks.play_selection(self.get_cell_by_address(sel), self.window, sel);
				}
			};
			self.kb_handler[[~keycode.mod.fx, ~keycode.kbfx[5]]] = { 
				var sel = self.model.selection;
				if( sel.notNil ) {
					callbacks.stop_selection(self.get_cell_by_address(sel), self.window, sel);
				}
			};

			self.kb_handler[[0, ~kbspecial.escape]] = { 
				callbacks.stop_selection;
				self.window.close;
			};
			self.kb_handler[[~modifiers.fx, ~kbfx[0]]] = { 
				if(self.model.selection.isNil, {
					"No selection to load".error;
				}, {
					callbacks.load(self.get_selected_cell, self.window)
				});
			};
		},

		show_window: { arg self;

			~matrix_view.(self.window, self);
			self.window.front;

		},

		init: { arg self;
			var parent;
			"zarb".debug("oui");
			self.window = parent = Window.new(winname, Rect(100,Window.screenBounds.height-400, 1320,300));
			
			self.model.patlist.debug("patlist");

			
			parent.view.keyDownAction = { arg view, char, modifiers, u, k; 
				u.debug("slooooooooooooo u");
				modifiers.debug("slooooooooooooo modifiers");
				//self.kb_handler.debug("kb");
				self.kb_handler[[modifiers,u]].value
			};
			"zarb".debug("oui2");

		}

	);
	obj.init;
	obj;

};

~choose_libnode = { arg main, action, actionpreset;
	var sl;
	var callbacks;
	var oldsel = nil;
	var playlist = List.new;

	callbacks = (
		selected: { arg self, sel, win, address;
			sel.debug("selected");
			if(oldsel == sel, {
				action.(sel);
				win.close;
			}, {
				oldsel = sel;	
			});
		},
		load: { arg self, defname, patwin;
			//sl.set_datalist(main.presetpool[sel])
			var presets, callbacks, datalist;
			var oldsel = nil;
			datalist = main.model.presetlib[defname]; // TODO: load real patlib
			if(datalist.isNil, {
				"No preset for this pat".inform;
			}, {
				callbacks = (
					selected: { arg self, sel, win;
						sel.debug("selected");
						if(oldsel == sel, {
							actionpreset.(sel);
							win.close;
							patwin.close;
						}, {
							oldsel = sel;	
						});
					},
					play_selection: { arg self, sel, win, ad;
						var pl;
						pl = main.get_node(main.model.presetlib[defname][sl.address_to_index(ad)]);
						playlist.add(pl);
						pl.node.play;
					},
					stop_selection: { arg self, sel, win, ad;
						playlist.do { arg pl; pl.node.stop };
						// FIXME: free player resources
						playlist = List.new;
					}
				);
				presets = ~make_matrix.(main, callbacks,winname:"choose preset");
				presets.set_datalist( datalist );
				presets.choose_cell({ arg sel, win; });
				presets.show_window;
			});
		}

	);
	sl = ~make_matrix.(main, callbacks,winname:"choose libnode");
	sl.choose_cell({ arg sel, win;
			
	});
	sl.set_datalist( main.model.patlist );
	sl.show_window;
};

~choose_effect = { arg main, action, actionpreset;
	var sl;
	var callbacks;
	var oldsel = nil;
	var playlist = List.new;

	callbacks = (
		selected: { arg self, sel, win, address;
			sel.debug("selected");
			if(oldsel == sel, {
				action.(sel);
				win.close;
			}, {
				oldsel = sel;	
			});
		},
		load: { arg self, defname, patwin;
			//sl.set_datalist(main.presetpool[sel])
			var presets, callbacks, datalist;
			var oldsel = nil;
			datalist = main.model.fxpresetlib[defname]; // TODO: load real patlib
			if(datalist.isNil, {
				"No preset for this pat".inform;
			}, {
				callbacks = (
					selected: { arg self, sel, win;
						sel.debug("selected");
						if(oldsel == sel, {
							actionpreset.(sel);
							win.close;
							patwin.close;
						}, {
							oldsel = sel;	
						});
					},
					play_selection: { arg self, sel, win, ad;
						var pl;
						pl = main.get_node(main.model.fxpresetlib[defname][sl.address_to_index(ad)]);
						playlist.add(pl);
						pl.node.play;
					},
					stop_selection: { arg self, sel, win, ad;
						playlist.do { arg pl; pl.node.stop };
						// FIXME: free player resources
						playlist = List.new;
					}
				);
				presets = ~make_matrix.(main, callbacks,winname:"choose fxpreset");
				presets.set_datalist( datalist );
				presets.choose_cell({ arg sel, win; });
				presets.show_window;
			});
		}

	);
	sl = ~make_matrix.(main, callbacks,winname:"choose effect");
	sl.choose_cell({ arg sel, win;
			
	});
	sl.set_datalist( main.model.effectlist );
	sl.show_window;
};

~choose_sample = { arg main, action, samplekit;
	var sl;
	var callbacks;
	var oldsel = nil;
	var samples = Dictionary.new;
	var samplelist = List.new;

	callbacks = (
		selected: { arg self, sel, win;
			win.debug("win1");
			sel.debug("selected");
			if(sel.notNil && {oldsel == sel}, {
				win.debug("win2");
				action.(samples[sel]);
				win.debug("win3");
				win.close;
			}, {
				oldsel = sel;	
			});
		},
		play_selection: { arg self, sel, win, ad;
			var pl;
			//pl = main.get_node(main.model.presetlib[defname][sl.address_to_index(ad)]);
			[samples[sel], sel].debug("play_selection: sel");
			{
				var buf;
				buf = Buffer.read(s, samples[sel]);
				s.sync;
				Synth(\monosampler, [\bufnum, buf]).onFree {
					buf.free;
				};
			}.fork;
		},
		stop_selection: { arg self, sel, win, ad;
			// FIXME: free player resources
		}

	);
	sl = ~make_matrix.(main,callbacks,winname:"choose sample");
	sl.choose_cell(action);
	//sl.set_datalist( {arg i;"sounds/bla.wav"++i}!10 );
	samplekit = samplekit ?? \default;
	main.samplekit_manager.get_samplelist_from_samplekit(samplekit).do { arg sam;
		samples[PathName.new(sam).fileName] = sam;
		samplelist.add(PathName.new(sam).fileName);
	};
	sl.set_datalist(samplelist);
	sl.show_window;
};

~save_pat = { arg main, datalist, node, action;
	var sl;
	var sa;
	var callbacks;
	var oldsel = nil;
	var playlist = List.new;

	callbacks = (
		selected: { arg self, sel, win, ad;
			var newname;
			//[ad, sel, oldsel, oldsel === ad].debug("selected: ad, sel, oldsel, oldsel == ad");
			//[sl.address_to_index(ad), if(oldsel.notNil) {sl.address_to_index(oldsel)}].debug("index ad oldsel");
			//(sl.address_to_index(ad) == if(oldsel.notNil) {sl.address_to_index(oldsel)}).debug("==");
			//newname = "new" ++ UniqueID.next;
			var idx;
			idx = sl.address_to_index(ad);
			if(idx.notNil && {oldsel == idx}, {
				action.(sel, sl.address_to_index(ad));
				//sl.set_cell(sl.model.selection, newname);
				self.stop_selection;
				win.close;
			}, {
				oldsel = idx;	
			});
		},
		play_selection: { arg self, sel, win, ad;
			var pl;
			pl = main.get_node(main.model.presetlib[node.defname][sl.address_to_index(ad)]);
			playlist.add(pl);
			pl.node.play;
		},
		stop_selection: { arg self, sel, win, ad;
			playlist.do { arg pl; pl.node.stop };
			// FIXME: free player resources
			playlist = List.new;
		}

	);
	sl = ~make_matrix.(main, callbacks, winname:"SAVE preset");
	sl.set_datalist( datalist ?? [] );
	sl.choose_cell(action);
	sl.show_window;
};


~save_player_column = { arg main, winname, player, datalist, action, rename_action;
	var sl;
	var sa;
	var callbacks;
	var oldsel = nil;
	var playlist = List.new;

	callbacks = (
		selected: { arg self, sel, win, ad;
			var idx;
			sel.debug("selected");
			idx = sl.address_to_index(ad);
			if(idx.notNil && {oldsel == idx}, {
				action.(sel, sl.address_to_index(ad));
				self.stop_selection;
				win.close;
			}, {
				oldsel = idx;	
			});
		},
		play_selection: { arg self;
			var pl, data, offset;
			offset = sl.address_to_index(sl.model.selection);
			data = main.model.colpresetlib[player.defname][offset];
			pl = ~make_player_from_colpreset.(main, data);
			//playlist.add(pl);
			Pfin(1, pl.node).play;
		},
		stop_selection: { arg self;
			playlist.debug("playlist");
			playlist.do { arg pl; pl.node.stop };
			// FIXME: free player resources
			playlist = List.new;
		},
		edit_value: { arg self;
			~edit_value.(sl.get_selected_cell.asString, { arg newname; 
				sl.set_cell(sl.model.selection, newname);
				rename_action.(sl.address_to_index(sl.model.selection), newname);
			})
		}

	);
	sl = ~make_matrix.(main, callbacks, winname:winname);
	sl.set_datalist( datalist );
	sl.choose_cell(action);
	sl.show_window;
};

//~choose_pat.(nil, { arg x; x.debug("yeak") });
//~choose_sample.(nil, { arg x; x.debug("yeak") });
//~save_pat.(nil, { arg x; x.debug("yeak") });

///////////////////////////////////

~file_dialog = { arg action, save=false, path, multiple=false, directory=false, confirm=true;
	var str;
	var result;
	path = path ?? ~seco_root_path;
	str = "";
	str = str ++ if(save) { "--save " } { "" };
	str = str ++ if(multiple) { "--multiple " } { "" };
	str = str ++ if(directory) { "--directory " } { "" };
	str = str ++ if(confirm) { "--confirm-overwrite " } { "" };

	fork{
		result = "zenity --file-selection --filename='%' %".format(path, str).debug("cmd").unixCmdGetStdOut;
		result = result.drop(-1);
		if(result == "") { result = nil };
		action.(result);
	};
};

~save_file_dialog = { arg action, path, multiple=false, directory=false, confirm=true;
	~file_dialog.(action, true, path, false, false, true);
};

~open_file_dialog = { arg action, path, multiple=false, directory=false, confirm=true;
	~file_dialog.(action, false, path, false, false, true);
};
