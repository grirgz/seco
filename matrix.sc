

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



~make_matrix = { arg main, callbacks;

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
				callbacks.selected(self.get_cell_by_address(sel), self.window);
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

			self.kb_handler[[0, ~kbspecial.escape]] = { self.window.close; };
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
			self.window = parent = Window.new("Matrix",Rect(100,Window.screenBounds.height-400, 1220,300));
			
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

~bla = (
	loop: [
		\loop_1,
		\loop_2,
		\loop_3
	]
);

~choose_libnode = { arg main, action;
	var sl;
	var callbacks;
	var oldsel = nil;

	callbacks = (
		selected: { arg self, sel, win;
			sel.debug("selected");
			if(oldsel == sel, {
				action.(sel);
				win.close;
			}, {
				oldsel = sel;	
			});
		},
		load: { arg self, sel, patwin;
			//sl.set_datalist(main.presetpool[sel])
			var presets, callbacks, datalist;
			var oldsel = nil;
			datalist = ~bla[sel];
			if(datalist.isNil, {
				"No preset for this pat".inform;
			}, {
				callbacks = (
					selected: { arg self, sel, win;
						sel.debug("selected");
						if(oldsel == sel, {
							action.(sel);
							win.close;
							patwin.close;
						}, {
							oldsel = sel;	
						});
					}
				);
				presets = ~make_matrix.(main, callbacks);
				presets.set_datalist( ~bla[sel] );
				presets.choose_cell({ arg sel, win; });
				presets.show_window;
			});
		}

	);
	sl = ~make_matrix.(main, callbacks);
	sl.choose_cell({ arg sel, win;
			
	});
	sl.set_datalist( ~synthlib.collect { arg asso; asso.key } );
	sl.show_window;
};

~choose_sample = { arg main, action;
	var sl;
	var callbacks;
	var oldsel = nil;

	callbacks = (
		selected: { arg self, sel, win;
			sel.debug("selected");
			if(sel.notNil && oldsel == sel, {
				action.(sel);
				win.close;
			}, {
				oldsel = sel;	
			});
		}

	);
	sl = ~make_matrix.(main,callbacks);
	sl.choose_cell(action);
	//sl.set_datalist( {arg i;"sounds/bla.wav"++i}!10 );
	sl.set_datalist( a );
	sl.show_window;
};

~save_pat = { arg main, action;
	var sl;
	var sa;
	var callbacks;
	var oldsel = nil;

	callbacks = (
		selected: { arg self, sel, win;
			sel.debug("selected");
			if(sel.notNil && oldsel == sel, {
				action.(sel);
				sl.set_cell(sl.model.selection, "plop47");
				win.close;
			}, {
				oldsel = sel;	
			});
		}

	);
	sl = ~make_matrix.(main, callbacks);
	sl.choose_cell(action);
	sa = SparseArray.new;
	sa[10*32] = nil;

	sl.set_datalist( sa );
	sl.show_window;
};

//~choose_pat.(nil, { arg x; x.debug("yeak") });
//~choose_sample.(nil, { arg x; x.debug("yeak") });
//~save_pat.(nil, { arg x; x.debug("yeak") });

)
