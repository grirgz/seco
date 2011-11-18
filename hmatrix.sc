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

~debug = 0;

~make_node_view = { arg parent, controller;


	var bt, set_states;

	//~debug = ~debug + 1;
	//if( ~debug > 100 ) {
	//	"bla".throw;
	//};
	bt = GUI.button.new(parent, Rect(50,50,200,30));

	set_states = {
		var oldval, state;
		oldval = bt.value;
		state = switch(controller.get_playing_state,
			\play, {
				"I>";
			},
			\stop, {
				"[]";
			},
			\mutestop, {
				"M]";
			},
			\mute, {
				"M>";
			}
		);
		if( controller.kind == \player ) {
			bt.states = [
				[ state + controller.uname, Color.black, Color.white],
				[ state + controller.uname, Color.white, Color.black],
			];
		} {
			bt.states = [
				[ state + controller.uname, Color.black, ~color_scheme.header_cell],
				[ state + controller.uname, Color.white, Color.black],
			];
		};
		bt.value = oldval;
	};
	~make_view_responder.(bt, controller, (
		redraw_node: {
			set_states.();
			//bt.value = 0;
		},
		selected: { arg obj, msg, selected;
			bt.value = selected;
		}
	), auto_refresh:false);

	// groupplayer is represented with a column view and a button view, 
	// so avoid infinite loop by avoiding a call to controller.refresh
	controller.changed(\redraw_node); 


};

~node_column_view = { arg parent, controller;

	var layout, ps_col_layout, curbank, address;
	var width = 1350;

	layout = GUI.vLayoutView.new(parent, Rect(0,0,(160),60*6));
	if(controller.kind == \seqnode) {
		layout.background = ~color_scheme.control2;
	} {
		layout.background = ~color_scheme.control;
	};
	//controller.debug("creating node_column_view");

	//parent.view.background = ~editplayer_color_scheme.background;
	~make_view_responder.(layout, controller, (

		redraw: { arg obj, msg, header, datalist;
			var cell;
			//NOTE: only called on creation
			//controller.debug("rederaw column");
			//datalist.debug("==========================datalist");
			layout.removeAll;
			//layout.focus(true);
			cell = ~make_node_view.(layout, header);
			datalist.do { arg node;
				//node.uname.debug("redraw column node");
				~make_node_view.(layout, node); // FIXME: should show name instead of uname
			}
		},

		children: { arg obj, msg, index, node;
			//layout.children[index] = ~make_node_view.(
			//TODO: use individual containers
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
	//controller.changed(\redraw, controller, controller.get_view_children);
};

~hmatrix_view = { arg parent, controller;

	var sl_layout, ps_col_layout, curbank, address;
	var width = 1350;

	sl_layout = GUI.hLayoutView.new(parent, Rect(0,0,width,60*6));
	sl_layout.background = ~editplayer_color_scheme.background;

	//parent.view.background = ~editplayer_color_scheme.background;
	~make_view_responder.(sl_layout, controller, (

		redraw: { arg obj, msg;

			"".debug("rederaw hmatrix");
			sl_layout.removeAll;

			controller.get_datalist_by_bank(controller.model.bank).do { arg node;
				//node.uname.debug("hmatrix_view redraw groupnode");
				~node_column_view.(sl_layout, node);
			};
			parent.focus(true);

		}, 

		header_selection: { arg obj, msg, x, selected;
			sl_layout.children[x].children[0].value = selected;
		},

		cell_selection: { arg obj, msg, x, y, selected;
			sl_layout.children[x].children[y+1].value = selected;
		}


	))
};


~make_grouplive = { arg main, panel;

	var obj;

	obj = (

		model: (
			//samplelist: {arg i; "sounds/default" ++ i }!50,
			//patlist: main.model.patlist,
			//datalist: (8*9).collect( ~make_empty_parnode),
			panel: panel,
			numbank: 10,
			datalist: List.fill(8*10, { \void }), // sparsearray ?

			selection: (x:0, y:0, bank:0),
			bank: 0,
			offset: 0@0
		),

		// ==== datalist management

		get_node: { arg self, name;
			main.get_node(name);
		},

		unvoid_list: { arg self, list;
			list.collect(self.unvoid(_))
		},

		unvoid: { arg self, elem;
			if ( elem == \void ) {
				~make_empty_parnode.();
			} {
				elem;
			}
		},

		get_datalist: { arg self;
			self.unvoid_list(self.model.datalist);
		},

		get_datalist_by_bank: { arg self, bank;
			if(bank < self.model.numbank) {
				self.unvoid_list(self.model.datalist[(bank*8) .. (bank+1*8-1)])
			}
		},

		address_to_index: { arg self, ad;
			(ad.bank * 8) + ad.x;
		},

		address_in_range: { arg self, sel;
			var cell;
			if ( self.address_to_index(sel) < self.model.datalist.size ) {
				cell = self.model.datalist[self.address_to_index(sel)];
				if( cell == \void ) {
					cell = self.unvoid(cell);
				};
				(sel.y < cell.children.size) && (sel.y > -2);
			} {
				false
			};
		},

		get_cell: { arg self, ad;
			if( self.address_in_range(ad) ) {
				if(ad.y == -1, {
					self.unvoid(self.model.datalist[self.address_to_index(ad)]);
				}, {
					main.get_node( self.unvoid(self.model.datalist[self.address_to_index(ad)]).children[ad.y] );
				})
			} {
				"get_cell_by_address: Cell out of range".error;
				nil;
			}
		},

		get_cell_uname: { arg self, ad;
			var col = self.model.datalist[self.address_to_index(ad)];
			if( self.address_in_range(ad) ) {
				if(ad.y == -1, {
					if ( col != \void ) {
						col.uname;	
					} {
						col
					}
				}, {
					ad.debug("get_cell_uname address");
					self.unvoid(self.model.datalist[self.address_to_index(ad)]).children.debug("get_cell_uname");
					self.unvoid(self.model.datalist[self.address_to_index(ad)]).children[ad.y].debug("bordel");
					self.unvoid(self.model.datalist[self.address_to_index(ad)]).children[ad.y];
				})
			} {
				"get_cell_by_address: Cell out of range".error;
				nil;
			}

		},

		get_column: { arg self, ad;
			if( self.address_in_range(ad) ) {
				self.model.datalist[self.address_to_index(ad)];
			} {
				"get_cell_by_address: Cell out of range".error;
				nil;
			}
		},

		set_cell: { arg self, address, val;
			var name, cell;
			if( self.address_in_range(address) ) {
				//name = if(val.isNil, { "" }, {val.name});
				name = val;
				if(address.y == -1, {
					self.model.datalist[ self.address_to_index(address)] = main.get_node(val);
					self.refresh;
				}, {
					cell = self.model.datalist[ self.address_to_index(address) ];
					if(cell == \void) {
						self.model.datalist[self.address_to_index(address)] = self.make_groupplayer(self.address_to_index(address));
						self.model.datalist[self.address_to_index(address)].set_children_name(address.y, val);
						//self.model.datalist[self.address_to_index(address)].changed(\cell_label, address.y, name);
						//self.changed(\redraw_column, address.x)
						self.refresh;
					} {
						//self.model.datalist[self.address_to_index(address)].children[address.y] = val;
						//self.model.datalist[self.address_to_index(address)].changed(\cell_label, address.y, name);
						self.model.datalist[self.address_to_index(address)].set_children_name(address.y, val);
						self.refresh_selection;
					}
				});
			} {
				"get_cell_by_address: Cell out of range".error;
				nil;
			}
		},

		set_bank: { arg self, idx;
			idx.debug("set bank");
			self.model.bank = idx;
			main.context.set_bank(idx);
			self.refresh;
		},

		set_datalist: { arg self, datalist;
			self.model.datalist = datalist;
			self.model.selection = nil;
			self.refresh;
		},


		get_selected_cell: { arg self;
			self.get_cell(self.model.selection);
		},

		set_selected_cell: { arg self, val;
			self.set_cell(self.model.selection, val);
			main.context.set_selected_node(self.get_selected_cell);
		},

		set_selection: { arg self, x, y;
			var sel, oldsel;
			sel =  (
				x: x,
				y: y,
				bank: self.model.bank
			);
			"set_selection:sel".debug(sel);
			if( self.address_in_range(sel) ) {
				oldsel = self.model.selection;
				self.model.selection = sel;
				main.context.set_selected_node(self.get_selected_cell);
				if(oldsel.notNil, {
					self.do_selection(oldsel, 0);	
				});
				self.do_selection(sel, 1);
			} {
			};
		},

		do_selection: { arg self, sel, val;
			if(sel.y == -1, {
				self.changed(\header_selection, sel.x, val);
			}, {
				self.changed(\cell_selection, sel.x, sel.y, val);
			});
		},

		refresh_selection: { arg self;
			self.do_selection(self.model.selection, 1);
		},

		refresh: { arg self;
			var sel = self.model.selection;
			self.changed(\redraw);
			main.set_window_title(self.model.panel ++ ": bank" + self.model.bank);
			sel.debug("refresh sel");
			if(sel.notNil, {
				if(sel.bank == self.model.bank, {
					self.set_selection(sel.x, sel.y);
				});
			});
		},

		save_data: { arg self;
			var data = ();
			data.model = self.model.copy();
			data.model.datalist = self.model.datalist.collect { arg d; 
				if( d == \void ) {
					d;
				} {
					d.uname
				}
			};
			data;
		},

		load_data: { arg self, data;
			data.model.debug("load_data model");
			self.model = data.model;
			self.model.datalist = self.model.datalist.collect { arg name;
				if(name == \void) {
					name
				} {
					self.get_node(name) 
				}
			};
		},

		play_selected: { arg self;
			self.get_selected_cell.play_node;
		},

		stop_selected: { arg self;
			self.get_selected_cell.stop_node;
		},

		make_groupplayer: { arg self, x=0;
			var name, pl;
			name = ("par"++x).asSymbol;
			pl = ~make_parplayer.(main);
			pl.name = name;
			pl.uname = name;
			main.add_node(pl);
			pl;
		},

		find_free_name: { arg self, makename;
			var newname;
			block { arg break; 
				1000.do {
					newname = makename.();
					if( main.get_node(newname).isNil ) { break.value };
					newname.debug("Name exist already");
				};
				"make_livenodename_from_libnodename: Error, can't find free name".error;
			};
			newname;
		},

		make_livenodename_from_libnodename: { arg self, name;
			self.find_free_name( { name++"_l"++UniqueID.next; } )
		},

		make_newlivenodename_from_livenodename: { arg self, name;
			self.find_free_name( { 
				name[ .. name.findBackwards("_l")  ] ++ "l" ++ UniqueID.next;
			})
		},

		make_livenode_from_libnode: { arg self, libnodename;
			var livenodename;
			var player;
			livenodename = self.make_livenodename_from_libnodename(libnodename);
			player = ~make_player.(main, main.model.patpool[libnodename]);
			player.name = livenodename;
			player.uname = livenodename;
			main.add_node(player);
			player.uname;
		},

		duplicate_livenode: { arg self, livenodename;
			var newlivenodename, newlivenode, newlivenode_pdict;
			newlivenodename = self.make_newlivenodename_from_livenodename(livenodename);
			newlivenodename.debug("newlivenodename");
			livenodename.debug("livenodename");
			main.model.livenodepool.debug("livenodepool");
			main.model.livenodepool[newlivenodename] = main.model.livenodepool[livenodename].clone;
			main.model.livenodepool[newlivenodename].name = newlivenodename;
			main.model.livenodepool[newlivenodename].uname = newlivenodename;
			newlivenodename;
		},

		duplicate_groupnode: { arg self, groupnodename, x;
			var name, pl;
			name = (self.model.panel.asString[..2]++x).asSymbol;
			pl = self.get_node(groupnodename).clone;
			pl.name = name;
			pl.uname = name;
			main.add_node(pl);
			pl.uname;
		},

		load_libnode: { arg self;
			var livenode;
			if(self.model.selection.y == -1, {
				"Can't load libnode in header".error;
			}, {
				~choose_libnode.(main, { arg libnodename, livenodename; 
					livenodename = self.make_livenode_from_libnode(libnodename);
					self.set_selected_cell(livenodename);
				}, { arg livenodename;
					livenodename = self.duplicate_livenode(livenodename);
					livenodename.debug("actionpreset");
					self.set_selected_cell(livenodename);
				});
			})
		},

		copy_node: { arg self;
			var uname, address;
			address = self.model.selection;
			uname = self.get_cell_uname(address);
			uname.debug("copied node1");
			if(uname == \void || (uname == \voidplayer)) {
				"Can't copy empty player".error;
			} {
				uname.debug("copied node2");
				main.model.clipboard = uname;
				main.model.clipboard.debug("copied node");
			};
		},

		paste_node: { arg self;
			var node;
			var address = self.model.selection;
			if( main.model.clipboard.isNil ) {
				"Can't paste: clipboard is empty".error;
			} {
				node = main.get_node(main.model.clipboard);
				if(address.y == -1) {
					if( node.kind == \player ) {
						"Can't paste on header".error;
					} {
						// parplayer and seqplayer
						self.set_selected_cell(self.duplicate_groupnode(main.model.clipboard, address.x));
					};
				} {
					if( node.kind == \player ) {
						self.set_selected_cell(self.duplicate_livenode(main.model.clipboard));
					} {
						// parplayer and seqplayer
						self.set_selected_cell(main.model.clipboard);
					};
				};
			};

		},

		save_livenode: { arg self;
			var datalist, node;
			if(self.model.selection.y == -1, {
				"save parnode: TODO".error;
			}, {
				node = self.get_selected_cell;
				if( node.kind == \player ) {
					if( main.model.presetlib[node.defname] == nil ) {
						main.model.presetlib[node.defname] = SparseArray.newClear(4*8*10, \empty);
					};
					datalist = main.model.presetlib[node.defname];

					~save_pat.(main, datalist, node, { arg name, offset; 
						main.model.presetlib[node.defname][offset] = self.duplicate_livenode(node.uname);
						// FIXME: persistence ?
					});
				} {
					"save_livenode: can't save parnode currently".error;
				}
			})


		},

		stop_all: { arg self;
			//self.model.datalist.do { arg i; i.node.stop	 };
			main.panic;
		},

		delete_node: { arg self;
			var livenode;
			if(self.model.selection.y == -1, {
				"Can't delete node in header".error;
			}, {
				self.set_cell(self.model.selection, nil)
			})

		},

		insert_seqbanknode: { arg self, bank;
			self.set_selected_cell(self.get_banknode(bank, \seqlive));
		},

		insert_parbanknode: { arg self, bank;
			self.set_selected_cell(self.get_banknode(bank, \parlive));
		},

		get_banknode: { arg self, bank, type;
			var node;
			var name = switch(type,
				\parlive, { ("parbank"++bank).asSymbol; },
				\seqlive, { ("seqbank"++bank).asSymbol; }
			);
			if(main.node_exists(name).not) {
				node = switch(self.model.panel,
					\parlive, { ~make_parbank_player.(main, bank); },
					\seqlive, { ~make_seqbank_player.(main, bank); }
				);
				main.add_node(node);
			};
			name;
		},

		play_bank: { arg self;
			var bank = self.model.bank;
			main.get_node(self.get_banknode(bank, self.model.panel)).play_node;
		},

		stop_bank: { arg self;
			var bank = self.model.bank;
			main.get_node(self.get_banknode(bank, self.model.panel)).stop_node;
		},

		solo_selected: { arg self;
			main.playmanager.solo_node(self.get_selected_cell.uname)
		},

		unsolo_selected: { arg self;
			main.playmanager.unsolo_node
		},

		make_gui: { arg self, parent;
			//self.debug("======debile");
			self.init_commands;
			~hmatrix_view.(parent, self);
		},

		init_commands: { arg self;

			var panel = self.model.panel;

			main.commands.matrix_add_enable([panel, \select_cell], [\kb, 0], ~keycode.kbpad8x4, { arg x, y; self.set_selection(x,y) });
			main.commands.array_add_enable([panel, \select_header], [\kb, ~keycode.mod.alt], ~keycode.kbnumline[..7], { arg x; self.set_selection(x,-1) });

			main.commands.array_add_enable([panel, \change_bank], [\kb, 0], ~keycode.kbnumpad, { arg x; self.set_bank(x) });

			main.commands.add_enable([panel, \load_libnode], [\kb, ~keycode.mod.fx, ~keycode.kbfx[0]], { self.load_libnode });
			main.commands.add_enable([panel, \save_livenode], [\kb, ~keycode.mod.fx, ~keycode.kbfx[1]], { self.save_livenode });
			main.commands.add_enable([panel, \delete_node], [\kb, ~keycode.mod.fx, ~keycode.kbfx[2]], { self.delete_node });

			main.commands.add_enable([panel, \insert_seqbanknode], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["s"]], { 
				var restorefun;
				restorefun = main.commands.overload_mode([panel, \select_bank_number_mode]);
				main.commands.array_set_action([panel, \select_bank_number_mode, \bank_number], 10, { arg i; self.insert_seqbanknode(i); restorefun.() });
				main.commands.set_action([panel, \select_bank_number_mode, \cancel], { restorefun.() });
			});

			main.commands.add_enable([panel, \insert_parbanknode], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["p"]], { 
				var restorefun;
				restorefun = main.commands.overload_mode([panel, \select_bank_number_mode]);
				main.commands.array_set_action([panel, \select_bank_number_mode, \bank_number], 10, { arg i; self.insert_parbanknode(i); restorefun.() });
				main.commands.set_action([panel, \select_bank_number_mode, \cancel], { restorefun.() });
			});

			main.commands.array_set_shortcut([panel, \select_bank_number_mode, \bank_number], [\kb, 0], ~keycode.kbnumpad);
			main.commands.set_shortcut([panel, \select_bank_number_mode, \cancel], [\kb, 0, ~keycode.kbspecial.escape]);


			main.commands.add_enable([panel, \copy_node], [\kb, ~keycode.mod.ctrl, ~keycode.kbcalphanum["c"]], { self.copy_node });
			main.commands.add_enable([panel, \paste_node], [\kb, ~keycode.mod.ctrl, ~keycode.kbcalphanum["v"]], { self.paste_node });

			main.commands.add_enable([panel, \play_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[4]], { self.play_selected });
			main.commands.add_enable([panel, \stop_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[5]], { self.stop_selected });
			main.commands.add_enable([panel, \play_bank], [\kb, ~keycode.mod.ctrlfx, ~keycode.kbfx[4]], { self.play_bank });
			main.commands.add_enable([panel, \stop_bank], [\kb, ~keycode.mod.ctrlfx, ~keycode.kbfx[5]], { self.stop_bank });
			main.commands.add_enable([panel, \stop_all], [\kb, ~keycode.mod.fx, ~keycode.kbfx[7]], { self.stop_all });

			main.commands.add_enable([panel, \solo_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[6]], { self.solo_selected });
			main.commands.add_enable([panel, \unsolo_selected], [\kb, ~keycode.mod.ctrlfx, ~keycode.kbfx[6]], { self.unsolo_selected });

			main.commands.add_enable([panel, \quick_save_project], [\kb, ~keycode.mod.ctrlaltshift, ~keycode.kbcalphanum["s"]], { main.quick_save_project });
			main.commands.add_enable([panel, \quick_load_project], [\kb, ~keycode.mod.ctrlaltshift, ~keycode.kbcalphanum["l"]], { main.quick_load_project });


			~make_panel_shortcuts.(main, panel);
		},

		init: { arg self;
			var parent;
			
			//self.model.datalist[8*9] = nil;
			//self.model.datalist.debug("patlist");
			self.init_commands;

		}

	);
	// no init;
	obj;

};

~make_parlive = { arg main;

	var obj;
	obj = ~make_grouplive.(main, \parlive);
	obj.init;
	obj;
};


~make_seqlive = { arg main;
	var obj, obj2;

	obj = ~make_grouplive.(main, \seqlive); // warning: calling parent init
	obj2 = (
		make_groupplayer: { arg self, x=0;
			var name, pl;
			name = ("seq"++x).asSymbol;
			pl = ~make_seqplayer.(main);
			pl.name = name;
			pl.uname = name;
			main.add_node(pl);
			pl;
		},

		unvoid: { arg self, elem;
			if ( elem == \void ) {
				~make_empty_seqnode.();
			} {
				elem;
			}
		}
		
	);
	obj2.keysValuesDo { arg key, val;
		obj[key] = val;
	};
	obj.init;
	obj;

};

)



