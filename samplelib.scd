
(


~samplekit_bank = Dictionary[
	\default -> [
		"sounds/hydrogen/GMkit/kick_Dry_b.flac",
		"sounds/hydrogen/GMkit/sn_Wet_b.flac",
		"sounds/hydrogen/GMkit/sn_Jazz_c.flac",
		"sounds/hydrogen/GMkit/hhp_Dry_a.flac",
		"sounds/hydrogen/GMkit/cym_Jazz.flac",
		"sounds/hydrogen/GMkit/clap_Dry_c.flac",
		"sounds/hydrogen/GMkit/stick_Woody.flac",
		"sounds/hydrogen/GMkit/hhc_Rock_b.flac",
		"sounds/hydrogen/GMkit/misc_Cowbell.flac",
		"sounds/hydrogen/GMkit/tom_Rock_mid.flac",
		"sounds/hydrogen/GMkit/cra_Rock_a.flac",
		"sounds/hydrogen/GMkit/hhc_Dry_a.flac",
		"sounds/hydrogen/GMkit/tom_Rock_hi.flac",
		"sounds/hydrogen/GMkit/cym_Rock_b.flac",
		"sounds/hydrogen/GMkit/tom_Rock_lo.flac",
		"sounds/hydrogen/GMkit/cra_Jazz.flac",

		"sounds/hydrogen/HardElectro1/PowR_HH_2.flac",
		"sounds/hydrogen/HardElectro1/CHH_1.flac",
		"sounds/hydrogen/HardElectro1/HardHse_K_03_B.flac",
		"sounds/hydrogen/HardElectro1/PowR_HH_1.flac",
		"sounds/hydrogen/HardElectro1/Hard_CHH_02.flac",
		"sounds/hydrogen/HardElectro1/yFX_3.flac",
		"sounds/hydrogen/HardElectro1/HardHse_K_02.flac",
		"sounds/hydrogen/HardElectro1/FX_Chh_01.flac",
		"sounds/hydrogen/HardElectro1/PowR_BD_1.flac",
		"sounds/hydrogen/HardElectro1/yFX_8.flac",
		"sounds/hydrogen/HardElectro1/xFX_6.flac",
		"sounds/hydrogen/HardElectro1/Hard_Hse_OHH_1.flac",
		"sounds/hydrogen/HardElectro1/Hard_CHH_01.flac",
		"sounds/hydrogen/HardElectro1/PowR_SN_1.flac",
		"sounds/hydrogen/HardElectro1/Clap_1.flac",
		"sounds/hydrogen/HardElectro1/Amp_Clap_1.flac",
	]
];

~empty_sample_path = "sounds/hydrogen/GMkit/emptySample.flac";

~samplekit_manager = (
	samplekit_part: 0,
	samplekit_bank: ~samplekit_bank,
	sampledict: Dictionary.new,
	sample_extensions: ["aiff","wav","flac"],

	slot_to_bufnum: { arg self, slot, samplekit, channels=\stereo;
		var path;
		var bufnum;
		//[slot, samplekit].debug("slot_to_bufnum");
		if(slot == \rest) {
			//BufferPool.get_sample(\samplekit, ~empty_sample_path).bufnum; // FIXME: find a way to not play at all;
			path =  ~empty_sample_path;
		} {
			path = self.slot_to_path(samplekit, slot);
		};
		if(channels == \mono) {
			bufnum = BufferPool.get_mono_sample(\samplekit, path).bufnum;		
		} {
			bufnum = BufferPool.get_forced_stereo_sample(\samplekit, path).bufnum;		
		};
		[ path, channels, bufnum].debug("slot_to_bufnum: channels");
		bufnum;
	},

	slot_to_path: { arg self, samplekit, slot, use_empty=true;
		var path;
		if(self.samplekit_bank[samplekit].isNil or: {self.samplekit_bank[samplekit][slot].isNil}) {
			samplekit.debug("error: samplekit_manager: slot_to_bufnum: no such samplekit or slotnum!");
			if(use_empty) {
				path =  ~empty_sample_path;
			} {
				path = nil;
			}
		} {
			path = self.samplekit_bank[samplekit][slot];
			if(path.isString) {
				path
			} {
				path = path[0]
			};
		};
		path = self.path_to_absolute_path(path);
		path;
	},

	path_to_absolute_path: { arg self, path;
		if(path.notNil and: {PathName(path).isRelativePath}) {
			path = ~seco_root_path +/+ path;
		};
		path;
	},

	bogus_absolute_path_to_relative_path: { arg self, path, crash_on_error=true;
		var oldval = path;
		var pathname = PathName(oldval);
		var newval = oldval;
		if(pathname.isRelativePath.not) {

			//"~/Musique/samplekit/bla/bli.wav".standardizePath.findReplace("~/Musique/samplekit".standardizePath, ~seco_root_path +/+ "samplekit")
			var oldmusique_path = "/home/ggz/Musique";
			var oldabs_path = "/home/ggz/Musique/sc";
			var oldsound_path = "/home/ggz/share/SuperCollider";
			var abspath;
			oldval.debug("make_buf_param: load_data: Warning: sample has not a relative path");

			case
				{ oldval.beginsWith(oldabs_path) } {
					newval = pathname.asRelativePath(oldabs_path)
				}
				{ oldval.beginsWith(oldmusique_path) } {
					newval = pathname.asRelativePath(oldmusique_path)
				}
				{ oldval.beginsWith(oldsound_path) } {
					newval = pathname.asRelativePath(oldsound_path)
				};

			abspath = ~seco_root_path +/+ newval;
			if(abspath.pathExists != \file and: { crash_on_error == true }) {
				[oldval, newval, abspath].debug("Error: Can't find sample in old path and can't find it in new path");
				"Error".errorerror
			};
		};
		newval
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
	get_samplekit_part: { arg self;
		self.samplekit_part;
	},
	
	add_samplekit: { arg self, name, samplekit;
		self.samplekit_bank[name] = samplekit;
	},

	get_samplekit_bank: { arg self; self.samplekit_bank },

	get_samplelist_from_samplekit: { arg self, samplekit;
		samplekit.debug("samplekit_manager.get_samplelist_from_samplekit: samplekit:");
		//self.samplekit_bank[samplekit].collect{ arg path; self.path_to_absolute_path(path) }
		self.samplekit_bank[samplekit];
	},

	parse_samplekit_dir: { arg self, samplekit_dir;
		var path;
		samplekit_dir = samplekit_dir ?? (~seco_root_path +/+ "samplekit/");
		path = PathName.new(samplekit_dir);
		path.folders.do { arg dir;
			var samplekit_name;
			var samples = List.new;
			var sklist = PathName(dir.fullPath +/+ "samplekit.list" );
			if(sklist.isFile) {
				// FIXME: handle layers
				var file;
				file = File(sklist.fullPath,"r");
				file.readAllString.split($\n).drop(-1).do { arg smp;
					var smpath = dir.fullPath +/+ smp;
					if(samples.includesEqual(smpath).not) {
						samples.add(smpath)
					}
				};
				//[dir.fullPath, samples.asCompileString].debug("samples");
				file.close;
			} {
				dir.files.do { arg file;
					var fpath = file.fullPath;
					if(self.sample_extensions.includes(fpath).not) {
						samples.add( fpath );
					}
				};
				samples = samples.sort;
			};
			samples = samples.collect { arg smp; 
				var out;
				//~seco_root_path.debug("root");
				//smp.debug("in");
				out = PathName(smp).asRelativePath(~seco_root_path_nude);
				//out.debug("out");
				out;
			};
			samplekit_name = dir.folderName;
			//[samplekit_name, samples].debug("parse_samplekit_dir: added samplekit");
			self.add_samplekit(samplekit_name.asSymbol, samples);
		};
	},


	append_samplelist_to_samplekit: { arg self, samplekit, samplelist;
		self.samplekit_bank[samplekit] = self.samplekit_bank[samplekit] ++ samplelist 
	},

	append_samplelist_to_samplekit_from_path: { arg self, samplekit, path;
		var dir, entries;
		dir = PathName.new(path);
		entries = dir.files.select({arg x; self.sample_extensions.includesEqual(x.extension) }).collect(_.fullPath);
		self.append_samplelist_to_samplekit(samplekit, entries);
	},

	add_to_sampledict: { arg self, dict, prefix=nil;
		if(prefix.notNil) {
			dict.keysValuesDo { arg key, val;
				self.sampledict[key] = prefix +/+ val;
			}
		} {
			self.sampledict.putAll(dict)
		}
	},

	buffer_from_sampledict: { arg self, name;
		if(self.sampledict[name].notNil) {
			BufferPool.get_sample(\samplekit, self.sampledict[name]);
		} {
			name.debug("Error: buffer_from_sampledict: no such sample");
			0
		}
	},

	mono_buffer_from_sampledict: { arg self, name;
		if(self.sampledict[name].notNil) {
			BufferPool.get_mono_sample(\samplekit, self.sampledict[name])
		} {
			name.debug("Error: buffer_from_sampledict: no such sample");
			0
		}
	},

	midinote_to_slot: { arg self, midinote;
		~keycode.midi.debug("midinote_to_slot: keycode.midi");
		~keycode.midi_note[midinote][1] + (self.samplekit_part * 8)
	}
);

)

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
