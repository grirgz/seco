
(

~make_sample_cell = { arg parent, label;
	var bt;

	bt = GUI.button.new(parent, Rect(50,50,200,30));
	bt.states = [
		[ label, Color.black, Color.white],
	];
	bt.value = 0;

};

~patlib_view = { arg parent, controller;

	var sl_layout, ps_col_layout, curbank, address;
	var width = 1350;

	sl_layout = GUI.hLayoutView.new(parent, Rect(0,0,width,60*6));

	//parent.view.background = ~editplayer_color_scheme.background;
	~make_view_responder.(sl_layout, controller, (

		redraw: {
			controller.model.debug("rederaw");
			sl_layout.removeAll;
			sl_layout.focus(true);
			controller.model.patlist.clump(4).clump(8)[controller.model.bank].do { arg col;
				ps_col_layout = GUI.vLayoutView.new(sl_layout, Rect(0,0,(160),60*6));
				ps_col_layout.background = ~editplayer_color_scheme.control;

				col.do { arg label;
					~make_sample_cell.(ps_col_layout, label);
				};
			}
		}
	))
};



~make_patlib = { arg main;

	var obj;

	obj = (

		model: (
			//samplelist: {arg i; "sounds/default" ++ i }!50,
			//patlist: main.model.patlist,
			patlist: ~synthlib.collect { arg asso; asso.key },
			bank: 0
		),

		kb_handler: Dictionary.new,

		refresh: { arg self;
			self.changed(\redraw);
		},

		set_bank: { arg self, idx;
			self.model.bank = idx;
			self.changed(\redraw);
		},

		get_pat_xy: { arg self, x, y;
			self.model.patlist[ (self.model.bank * 32) + (x * 4) + y ];
		},

		choose_pat: { arg self, action;
			~kbpad8x4.do { arg line, iy;
				line.do { arg key, ix;
					self.kb_handler[[0, key]] = { action.(self.get_pat_xy(ix, iy),self.window )};
				}
			};

			~kbnumpad.do { arg keycode, idx;
				self.kb_handler[[0, keycode]] = { self.set_bank(idx) };
			};
		},

		init: { arg self;
			var parent;
			"zarb".debug("oui");
			self.window = parent = Window.new("Pattern Lib",Rect(100,Window.screenBounds.height-400, 1220,300));
			
			self.model.patlist.debug("patlist");
			~patlib_view.(parent, self);

			
			parent.view.keyDownAction = { arg view, char, modifiers, u, k; 
				u.debug("slooooooooooooo u");
				modifiers.debug("slooooooooooooo modifiers");
				self.kb_handler.debug("kb");
				self.kb_handler[[modifiers,u]].value
			};
			parent.front;
			"zarb".debug("oui2");

		}

	);
	obj.init;
	obj;

};

~choose_pat = { arg main, action;
	var sl;
	sl = ~make_patlib.(main);
	sl.choose_pat(action);
};

~choose_pat.(nil, { arg x; x.debug("yeak") });

)
