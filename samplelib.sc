
(

//~make_sample_cell = { arg parent, label;
//	var bt;
//
//	bt = GUI.button.new(parent, Rect(50,50,200,30));
//	bt.states = [
//		[ label, Color.black, Color.white],
//	];
//	bt.value = 0;
//
//};
//
//~samplelib_view = { arg parent, controller;
//
//	var sl_layout, ps_col_layout, curbank, address;
//	var width = 1350;
//
//	sl_layout = GUI.hLayoutView.new(parent, Rect(0,0,width,60*6));
//
//	//parent.view.background = ~editplayer_color_scheme.background;
//	~make_view_responder.(sl_layout, controller, (
//
//		redraw: {
//			controller.model.debug("rederaw");
//			sl_layout.removeAll;
//			sl_layout.focus(true);
//			controller.model.samplelist.clump(4).clump(8)[controller.model.bank].do { arg col;
//				ps_col_layout = GUI.vLayoutView.new(sl_layout, Rect(0,0,(160),60*6));
//				ps_col_layout.background = ~editplayer_color_scheme.control;
//
//				col.do { arg label;
//					~make_sample_cell.(ps_col_layout, label);
//				};
//			}
//		}
//	))
//};
//
//
//
//~make_samplelib = { arg main;
//
//	var obj;
//
//	obj = (
//
//		model: (
//			//samplelist: {arg i; "sounds/default" ++ i }!50,
//			samplelist: main.model.samplelist,
//			bank: 0
//		),
//
//		kb_handler: Dictionary.new,
//
//		refresh: { arg self;
//			self.changed(\redraw);
//		},
//
//		set_bank: { arg self, idx;
//			self.model.bank = idx;
//			self.changed(\redraw);
//		},
//
//		get_sample_xy: { arg self, x, y;
//			self.model.samplelist[ (self.model.bank * 32) + (x * 4) + y ];
//		},
//
//		choose_sample: { arg self, action;
//			~kbpad8x4.do { arg line, iy;
//				line.do { arg key, ix;
//					self.kb_handler[[0, key]] = { action.(self.get_sample_xy(ix, iy),self.window )};
//				}
//			};
//
//			~kbnumpad.do { arg keycode, idx;
//				self.kb_handler[[0, keycode]] = { self.set_bank(idx) };
//			};
//		},
//
//		init: { arg self;
//			var parent;
//			"zarb".debug("oui");
//			self.window = parent = Window.new("Sample Lib",Rect(100,Window.screenBounds.height-400, 1220,300));
//			
//			self.model.samplelist.debug("samplelist");
//			~samplelib_view.(parent, self);
//
//			
//			parent.view.keyDownAction = { arg view, char, modifiers, u, k; 
//				u.debug("slooooooooooooo u");
//				modifiers.debug("slooooooooooooo modifiers");
//				self.kb_handler.debug("kb");
//				self.kb_handler[[modifiers,u]].value
//			};
//			parent.front;
//			"zarb".debug("oui2");
//
//		}
//
//	);
//	obj.init;
//	obj;
//
//};
//
//~choose_sample = { arg main, action;
//	var sl;
//	sl = ~make_samplelib.(main);
//	sl.choose_sample(action);
//};

//~choose_sample.(nil, { arg x; x.debug("yeak") });

~samplekit_bank = Dictionary[
	\default -> [
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/kick_Dry_b.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/sn_Wet_b.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/sn_Jazz_c.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/hhp_Dry_a.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/cym_Jazz.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/clap_Dry_c.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/stick_Woody.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/hhc_Rock_b.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/misc_Cowbell.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/tom_Rock_mid.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/cra_Rock_a.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/hhc_Dry_a.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/tom_Rock_hi.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/cym_Rock_b.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/tom_Rock_lo.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/cra_Jazz.flac",

		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/PowR_HH_2.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/CHH_1.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/HardHse_K_03_B.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/PowR_HH_1.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/Hard_CHH_02.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/yFX_3.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/HardHse_K_02.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/FX_Chh_01.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/PowR_BD_1.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/yFX_8.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/xFX_6.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/Hard_Hse_OHH_1.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/Hard_CHH_01.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/PowR_SN_1.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/Clap_1.flac",
		"/home/ggz/share/SuperCollider/sounds/hydrogen/HardElectro1/Amp_Clap_1.flac",
	]
];

~empty_sample_path = "/home/ggz/share/SuperCollider/sounds/hydrogen/GMkit/emptySample.flac";

~samplekit_manager = (
	samplekit_part: 0,
	samplekit_bank: Dictionary.new,

	slot_to_bufnum: { arg self, slot, samplekit;
		var path;
		if(slot == \rest) {
			BufferPool.get_sample(\samplekit, ~empty_sample_path).bufnum; // FIXME: find a way to not play at all;
		} {
			path = self.samplekit_bank[samplekit][slot];
			if(path.isString.not) {
				path = path[0]
			};
			BufferPool.get_sample(\samplekit, path).bufnum;		
		};
	},

	slot_to_startPos: { arg self, slot, samplekit;
		var path = self.samplekit_bank[samplekit][slot];
		if(path.isString) {
			0
		} {
			path[1]
		};
	},

	set_samplekit_part: { arg self, val;
		self.samplekit_part = val;
	},
	
	add_samplekit: { arg self, name, samplekit;
		self.samplekit_bank[name] = samplekit;
	},

	midinote_to_slot: { arg self, midinote;
		~keycode.midi.debug("midinote_to_slot: keycode.midi");
		~keycode.midi_note[midinote][1] + (self.samplekit_part * 8)
	}
);

)
