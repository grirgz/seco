

(

~make_cell = { arg parent, label;
	var bt;

	bt = GUI.button.new(parent, Rect(50,50,200,30));
	bt.states = [
		[ label, Color.black, Color.white],
		[ label, Color.white, Color.black],
	];
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

~in_range = { arg datalist, address;
	if(address.isNil, {
		false;
	}, {
		((address.bank * 32) + (address.x * 4) + address.y) < datalist.size
	})
};

~matrix_view = { arg parent, controller;

	var sl_layout, ps_col_layout, curbank, address;
	var width = 1350;

	sl_layout = GUI.hLayoutView.new(parent, Rect(0,0,width,60*6));

	//parent.view.background = ~editplayer_color_scheme.background;
	~make_view_responder.(sl_layout, controller, (

		redraw: {
			controller.model.debug("rederaw");
			sl_layout.removeAll;
			sl_layout.focus(true);
			controller.model.datalist.clump(4).clump(8)[controller.model.bank].do { arg col;
				ps_col_layout = GUI.vLayoutView.new(sl_layout, Rect(0,0,(160),60*6));
				ps_col_layout.background = ~editplayer_color_scheme.control;

				col.do { arg label;
					~make_cell.(ps_col_layout, label);
				};
			}
		},

		cell: { arg obj, msg, ad, label;
			
			if(~in_range.(controller.model.datalist, ad), {
				~set_cell_label.(sl_layout.children[ad.x].children[ad.y], label)
			});

		},

		selection: { arg obj, msg, oldsel;
			var sel = controller.model.selection;

			sel.debug("selection");

			if(~in_range.(controller.model.datalist, oldsel), {
				sl_layout.children[oldsel.x].children[oldsel.y].value = 0;
			});
			if(~in_range.(controller.model.datalist, sel), {
				sl_layout.children[sel.x].children[sel.y].value = 1;
			});
		}
		

	))
};


~class_matrix_chooser = (
	new: { arg self, action, winname="Matrix";
		var parent;
		self = self.deepCopy;
		"zarb".debug("oui");
		self.window = parent = Window.new(winname, Rect(100,Window.screenBounds.height-400, 1320,300));
		
		self.action = action;
		self.model.patlist.debug("patlist");
		
		parent.view.keyDownAction = { arg view, char, modifiers, u, k; 
			u.debug("slooooooooooooo u");
			modifiers.debug("slooooooooooooo modifiers");
			//self.kb_handler.debug("kb");
			self.kb_handler[[modifiers,u]].value
		};
		"zarb".debug("oui2");

		self.set_bindings;

		self;
	},

	model: (
		datalist: [],
		selection: nil,
		bank: 0
	),

	kb_handler: Dictionary.new,

	address_to_index: { arg self, ad;
		(ad.bank * 32) + (ad.x * 4) + ad.y;
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
		self.model.datalist[ (self.model.bank * 32) + (x * 4) + y ];
	},

	get_cell_by_address: { arg self, ad;
		self.model.datalist[ (ad.bank * 32) + (ad.x * 4) + ad.y ];
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
		if(~in_range.(self.model.datalist, sel), {
			oldsel = self.model.selection;
			self.model.selection = sel;
			self.changed(\selection, oldsel);
			self.selected(self.get_cell_by_address(sel), self.window, sel);
		});
	},

	set_cell: { arg self, address, val;
		self.model.datalist[ self.address_to_index(address) ] = val;
		self.changed(\cell, address, val)
	},

	set_bindings: { arg self;
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

		~matrix_view.(self.window, self);
		self.window.front;

	},

	// to be overloaded

	selected: { arg self, sel, win, address;
		sel.debug("selected");
		if(self.oldsel == sel, {
			self[\action].(sel);
			win.close;
		}, {
			self.oldsel = sel;	
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
			(ad.bank * 32) + (ad.x * 4) + ad.y;
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
			self.model.datalist[ (self.model.bank * 32) + (x * 4) + y ];
		},

		get_cell_by_address: { arg self, ad;
			self.model.datalist[ (ad.bank * 32) + (ad.x * 4) + ad.y ];
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
			if(~in_range.(self.model.datalist, sel), {
				oldsel = self.model.selection;
				self.model.selection = sel;
				self.changed(\selection, oldsel);
				callbacks.selected(self.get_cell_by_address(sel), self.window, sel);
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

		main.samplekit_manager.get_samplelist_from_samplekit(samplekit).do { arg sam;
			self.samples[PathName.new(sam).fileName] = sam;
			samplelist.add(PathName.new(sam).fileName);
		};
		self.set_datalist( samplelist );
		self.show_window;
		self;
	},

	selected: { arg self, sel, win, address;
		sel.debug("selected");
		if(self.oldsel == sel, {
			self[\action].(self.samples[sel]);
			win.close;
		}, {
			self.oldsel = sel;	
		});
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

)
