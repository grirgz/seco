"=============================================================================================================================================".postln;
"	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////".postln;
"	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////".postln;
"	  						Loading Sequencer...".postln;
"	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////".postln;
"	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////".postln;
"=============================================================================================================================================".postln;


~toggle_value = { arg value;
	if( value.isNil, { value = 0 });
	((value+1) % 2);
};

~toggle_button = { arg button;
	button.value = ((button.value+1) % 2);
};


~compare_point = { arg a, b;
	case
		{ a.x == b.x } { a.y < b.y }
		{ a.x < b.x }
};


~prefix_array = { arg prefix, array;

	array.collect { arg e;
		prefix ++ [e];
	};
};

~general_sizes = (
	bank: 10,
	children_per_groupnode: 16,
	children_part_per_groupnode: 4, // keyboard can access to only 4 children at the same time
	groupnode_per_bank: 8
);


~matrix3_from_list = { arg list, collectfun = { arg x; x };
	var banklist = List[], collist = List[], celllist = List[];
	var bankidx = 0, colidx = 0, cellidx = 0;
	list.do { arg asso;
		if( cellidx >= 4, {
			if( colidx >= 8, {
				banklist.add( collist );
				collist = List[];
				colidx = 0;
				bankidx = bankidx + 1;
			});
			collist.add( celllist );
			colidx = colidx + 1;
			cellidx = 0;
			celllist = List[];
		});
		celllist.add( collectfun.(asso) );
		cellidx = cellidx + 1;
	};
	banklist.add( collist );
	collist.add( celllist );
	banklist;

};

~init_controller = { arg controller, messages;
	messages.keysValuesDo { arg key, val; controller.put(key, val) };
};

~editplayer_color_scheme = (
	background: Color.newHex("94A1BA"),
	control: Color.newHex("6F88BA"),
	led: Color.newHex("A788BA")
);

~color_scheme = (
	background: Color.newHex("94A1BA"),
	control: Color.newHex("6F88BA"),
	control2: Color.newHex("6F889A"),
	led: Color.newHex("A788BA"),

	header_cell: Color.newHex("BBBBA9")

);

~make_view_responder = { arg parent, model, message_responders, auto_refresh=true; 
	var controller;

	controller = SimpleController(model);

	Dictionary.newFrom(message_responders).keysValuesDo { arg key, val;
		controller.put(key, val)
	};

	parent.onClose = parent.onClose.addFunc { controller.remove };

	if(auto_refresh) { model.refresh() };
};

// this function take a pattern and a list of Pmono pattern as effects
~pfx = { arg pat, effects;
	Pspawner({ |spawner|
		var str, pbus, pgroup, leffect;
		var blist = List.new;
		// create a bus and set the pat to write on it
		pbus = Bus.audio(s,2).debug("first bus");
		blist.add(pbus);
		pat = Pset(\out, pbus, pat);
		// when the pattern end, free all the effects
		str = CleanupStream(pat.asStream, {
			"cleanup".debug;
			spawner.suspendAll;
			//pbus.free;
			//glist.do(_.free);
		});
		spawner.par(str);
		pgroup = 1;
		// for each effect, set \in to read from the previous effect and \out to write to the next effect
		// and create a group to maintain order-of-execution
		// then launch it in parralel with the pat
		effects[..effects.size-2].do { arg ef;
			ef = Pset(\in, pbus, ef);
			pbus = Bus.audio(s,2);
			blist.add(pbus);
			ef = Pset(\out, pbus, ef);
			pgroup = Group.after(pgroup);
			ef = Pset(\group, pgroup, ef);
			spawner.par(ef)
		};
		// the last effect should write on bus 0 so don't set its \out
		leffect = effects.last;
		leffect = Pset(\in, pbus, leffect);
		pgroup = Group.after(pgroup);
		pgroup.register;
		pgroup.addDependant( { arg grp, status;
			if(status == \n_end) {
				"fin!!".debug;
				blist.do(_.free)
			}
		});
		leffect = Pset(\group, pgroup, leffect);
		spawner.par(leffect)
	});
};

// ==========================================
// INCLUDES
// ==========================================

[
	"synth",
	"keycode", 
	"midi",
	"param",
	"samplelib",
	"player",
	"matrix",
	"hmatrix",
	"editplayer",
	"mixer",
	"score"
].do { arg file;
	("Loading " ++ file ++".sc...").inform;
	("/home/ggz/code/sc/seco/"++file++".sc").loadDocument;
};
"Done loading.".inform;


// ==========================================
// SEQUENCER FACTORY
// ==========================================


~edit_value = { arg input, action, name="Edit value";
	var window, text;
	window = GUI.window.new(name, Rect(500, 500, 150, 40));
	text = TextField(window, Rect(0, 0, 150, 40));
	text.string = input;

	text.action = {arg field; action.(field.value); window.close };
	text.keyDownAction = { arg view, char, modifiers, u, k; 
		[name, modifiers, u].debug("KEYBOARD INPUT");
		if( u == ~keycode.kbspecial.escape ) { window.close };
	};
	window.front;
};

~main_view = { arg controller;

	var window, sl_layout, ps_col_layout, curbank, address;
	var width = 1350, height = 800;
	var parent;

	
	window = GUI.window.new("seq", Rect(50, 50, width, height));
	window.view.decorator = FlowLayout(window.view.bounds); // notice that FlowView refers to w.view, not w
	//parent = window;

	sl_layout = GUI.hLayoutView.new(window, Rect(0,0,width,height));
	//parent = window;
	parent = sl_layout;

	//parent.view.background = ~editplayer_color_scheme.background;
	~make_view_responder.(parent, controller, (

		title: { arg obj, msg, title;
			window.name = title;
		},

		panel: { arg obj, msg, panel;
			block { arg break;
				panel.debug("main view: changing to panel");
				"pan0".debug;
				if([\seqlive, \parlive].includes(panel), {
					"pan1".debug;
					controller.context.set_spacekind(panel);
					parent.removeAll;
					controller.panels[panel].make_gui(parent)	

				}, {
					switch(panel,
						\mixer, {
							"pan2".debug;
							parent.removeAll;
							~make_mixer.(controller, parent);
							"pan2,5".debug;
						},
						\editplayer, {
							"pan3".debug;
							if (controller.context.get_selected_node.name == \voidplayer) { 
								"FORBIDDEN: can't edit empty player".inform;
								break.value 
							};
//							if (controller.context.get_selected_node.kind != \player) { 
//								"FORBIDDEN: can't edit groupnode currently".inform;
//								break.value 
//							};
							"pan3.5".debug;
							parent.removeAll;
							~make_editplayer.(controller, parent);
						},
						\score, {
							"pan4".debug;
							parent.removeAll;
							~make_score.(controller, parent);
						}
					)
				});
				"pan5".debug;
				window.view.keyDownAction = controller.commands.get_kb_responder(panel);
				"pan6".debug;
			}
		}
	));
	window.front;

};

~make_context = { arg main;

	var context;
	context = (
		parbank: 0,
		seqbank: 0,
		spacekind: \parlive, // parlive, seqlive
		selected_node: ~make_empty_parnode.(),

		get_selected_node: { arg self;
			self.selected_node;
		},

		set_selected_node: { arg self, val;
			val.uname.debug("context.set_selected_node");
			self.selected_node = val;
		},

		set_spacekind: { arg self, val;
			self.spacekind = val;
		},

		set_bank: { arg self, bank, panel=nil;
			panel = panel ?? self.spacekind;
			switch(panel,
				\parlive, {
					self.parbank = bank
				},
				\seqlive, {
					self.seqbank = bank
				}
			)
		},

		get_selected_node_set: { arg self;
			switch(self.spacekind,
				\parlive, {
					//main.panels.parlive.model.datalist.debug("context:get_selected_node_set:datalist"); // FIXME: hardcoded values
					main.panels.parlive.model.datalist[(self.parbank*8)..][..11].reject({arg x; x == \void}); // FIXME: hardcoded values
				},
				\seqlive, {
					main.panels.seqlive.model.datalist[(self.seqbank*8)..][..11].reject({arg x; x == \void}); // FIXME: hardcoded values
				}
			)
		},

		get_selected_bank: { arg self;
			switch(self.spacekind,
				\parlive, {
					self.parbank
				},
				\seqlive, {
					self.seqbank
				}
			)

		}
	);

};

~notNildo = { arg obj, functrue, funcfalse;
	if(obj.notNil) {
		functrue.value(obj);
	} {
		funcfalse.value(obj);
	};
};

~mk_sequencer = {

	var main;

	main = (
		model: (
			current_panel: \parlive,
			clipboard: nil,

			nodelib: nil,
			presetlib: Dictionary.new,
			presetlib_path: nil,
			colpresetlib: Dictionary.new,

			patlist: nil,
			patpool: Dictionary.new,
			samplelist: List.new,

			livenodepool: Dictionary.new

		),

		commands: ~shortcut,

		set_window_title: { arg self, title;
			self.changed(\title, title);
		},

		get_node: { arg self, name;
			var node;
			//name.debug("========get_node name");
			node = self.model.livenodepool[name];
			if(node.isNil) { ("Node not found:"+name).error };
			//node.debug("========get_node node");
			node;
		},

		node_exists: { arg self, name, functrue=nil, funcfalse=nil;
			var node;
			node = self.model.livenodepool[name];
			if(node.notNil) {
				if(functrue.isNil) { true } { functrue.value(node) };
			} {
				if(funcfalse.isNil) { false } { funcfalse.value(node) };
			};
		},

		make_livenodename_from_libnodename: { arg self, name;
			self.find_free_name( { name++"_l"++UniqueID.next; } )
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
			//main.model.livenodepool.keys.debug("livenodepool");
			main.model.livenodepool[newlivenodename] = main.model.livenodepool[livenodename].clone;
			main.model.livenodepool[newlivenodename].name = newlivenodename;
			main.model.livenodepool[newlivenodename].uname = newlivenodename;
			newlivenodename;
		},

		add_node: { arg self, node;
			self.model.livenodepool[node.uname] = node
		},

		context: \to_init,

		archive_livenodepool: { arg self, projpath, pool=nil;
			var dict = Dictionary.new;
			"HH".debug;
			if(pool.isNil) {
				pool = self.model.livenodepool
			};
			pool.keysValuesDo { arg key, val;
				switch(val.kind,
					\player, {
						(key -> (
							kind: \synthnode,
							defname: val.defname,
							data: val.save_data
						)).writeArchive(projpath++"/livenode_"++key);
					},
					\seqnode, {
						(key -> (
							kind: \seqnode,
							data: val.save_data
						)).writeArchive(projpath++"/seqnode_"++key);
					},
					\parnode, {
						(key -> (
							kind: \parnode,
							data: val.save_data
						)).writeArchive(projpath++"/parnode_"++key);
					}
				)
			};
			"HH".debug;
		},

		unarchive_livenodepool: { arg self, projpath;
			var path;
			var pool = Dictionary.new;
			"FF".debug;
			path = PathName.new(projpath);
			"FF".debug;
			path.entries.do { arg file;
				var fullname, name, asso;
				file.debug("unarchive_livenodepool file");
				fullname = file.fullPath;
				name = file.fileName;

				if(name.containsStringAt(0, "livenode_"), {
					asso = Object.readArchive(fullname);
					asso.key.debug("unarchive_livenodepool livenode");
					if(asso.key == \voidplayer) {
						pool[asso.key] = ~empty_player.()
					} {
						pool[asso.key] = ~make_player_from_synthdef.(self, asso.value.defname);
						pool[asso.key].load_data( asso.value.data );
						pool[asso.key].name = asso.key;
						pool[asso.key].uname = asso.key;
					}
				});
				if(name.containsStringAt(0, "parnode_"), {
					asso = Object.readArchive(fullname);
					asso.key.debug("unarchive_livenodepool parnode");
					pool[asso.key] = ~make_parplayer.(self);
					pool[asso.key].load_data( asso.value.data );
					pool[asso.key].name = asso.key;
					pool[asso.key].uname = asso.key;
				});
				if(name.containsStringAt(0, "seqnode_"), {
					asso = Object.readArchive(fullname);
					asso.key.debug("unarchive_livenodepool seqnode");
					pool[asso.key] = ~make_seqplayer.(self);
					pool[asso.key].load_data( asso.value.data );
					pool[asso.key].name = asso.key;
					pool[asso.key].uname = asso.key;
				});
			};
			"FF".debug;
			pool;
		},

		save_project: { arg self, name;
			var proj, projpath;

			proj = ();
			proj.name = name;

			proj.patlist = self.model.patlist;
			proj.patpool = self.model.patpool;

			proj.samplelist = self.model.samplelist;

			proj.volume = s.volume.volume;

			proj.panels = ();
			proj.panels.parlive = self.panels.parlive.save_data;
			proj.panels.seqlive = self.panels.seqlive.save_data;

			fork {
				name.debug("Saving project");
				projpath = "projects/"++name;
				("mkdir "++projpath).unixCmd;
				1.wait;
				//TODO: save context

				self.archive_livenodepool(projpath);
				
				proj.writeArchive(projpath++"/core");
			}

		},
		
		load_project: { arg self, name;
			var proj, projpath;
			projpath = "projects/"++name;
			proj = Object.readArchive(projpath++"/core");

			name.debug("Loading project");

			if(proj.notNil, {
				self.model.patlist = proj.patlist.debug("patlib=============================");
				self.model.patpool = proj.patpool.debug("patpool===============================");
				self.model.samplelist = proj.samplelist;
				s.volume.volume = proj.volume;


				self.model.livenodepool = self.unarchive_livenodepool(projpath);
				self.model.livenodepool.keys.debug("unarchived livenodepool keys");
				//TODO: load context

				self.panels.parlive.load_data(proj.panels.parlive);
				self.panels.seqlive.load_data(proj.panels.seqlive);


				self.refresh;
			}, {
				("Project `"++name++"' can't be loaded").postln
			});

		},

		set_presetlib_path: { arg self, name;
			self.model.presetlib_path = name;
			self.load_presets(name);
		},

		save_presetlib: { arg self;
			if(self.model.presetlib_path.notNil) {
				self.save_presets(self.model.presetlib_path);
			}
		},

		save_presets: { arg self, name;
			var pool = Dictionary.new, proj, projpath;

			self.model.presetlib.keysValuesDo { arg key, val;
				val.do { arg nodename;
					if(nodename != \empty && self.node_exists(nodename)) {
						pool[nodename] = self.get_node(nodename);
					};
				}
			};

			proj = ();
			proj.colpresetlib = self.model.colpresetlib;
			proj.presetlib = self.model.presetlib;
			//proj.presetlib.keys.debug("save_presets: presetlib");

			if(name.size > 1) {
				fork {
					name.debug("Saving presets");
					projpath = "projects/presets/"++name;
					//("rmdir "++projpath).unixCmd { // too dangerous :-O
					("mkdir "++projpath).unixCmd {
						self.archive_livenodepool(projpath, pool);
						proj.writeArchive(projpath++"/core");
					}
				}
			}


		},

		load_presets: { arg self, name;
			var pool, proj, projpath;
			name.debug("Loading presets");

			projpath = "projects/presets/"++name;
			proj = Object.readArchive(projpath++"/core");

			if(proj.notNil, {

				self.model.colpresetlib = proj.colpresetlib;
				//self.model.colpresetlib.debug("colpresetlib");
				self.model.presetlib = proj.presetlib;

				pool = self.unarchive_livenodepool(projpath);
				pool.keys.debug("presetpool.keys==================");
				pool.keysValuesDo { arg key, val;
					if( self.model.livenodepool[key].notNil ) {
						key.debug("Warning, name conflict in loading preset");
					};
					self.model.livenodepool[key] = val;
				};

			}, {
				("Presets `"++name++"' can't be loaded").postln
			});

		},

		quick_save_project: { arg self;
			
			fork {
				("rm -rf projects/quicksave").unixCmd;
				1.wait;
				self.save_project("quicksave");
			};

		},

		quick_load_project: { arg self;
			
			self.load_project("quicksave");

		},

		panels: (
			//seqlive: ~make_seqlive.(main),
			seqlive: \to_init,
			parlive: \to_init
		),
		
		load_patlib: { arg self, patlist;
			var patpool = Dictionary.new;

			patlist.do { arg asso;
				patpool[asso.key] = asso.value;
			};

			self.model.patlist = patlist.collect { arg asso; asso.key };
			self.model.patpool = patpool;
		},

		load_effectlib: { arg self, fxlist;
			var patpool = Dictionary.new;

			fxlist.do { arg asso;
				patpool[asso.key] = asso.value;
			};

			self.model.effectlist = fxlist.collect { arg asso; asso.key };
			self.model.effectpool = patpool;
		},

		load_samplelib: { arg self, samplelist;
			self.model.samplelist = samplelist;
		},

		append_samplelib: { arg self, samplelist;
			self.model.samplelist = self.model.samplelist ++ samplelist;
		},

		load_samplelib_from_path: { arg self, path;
			var dir, entries;
			dir = PathName.new(path);
			entries = dir.files.select({arg x; ["aiff","wav","flac"].includesEqual(x.extension) }).collect(_.fullPath);
			self.load_samplelib(entries);
		},

		append_samplelib_from_path: { arg self, path;
			var dir, entries;
			dir = PathName.new(path);
			entries = dir.files.select({arg x; ["aiff","wav","flac"].includesEqual(x.extension) }).collect(_.fullPath);
			self.append_samplelib(entries);
		},

		show_panel: { arg self, panel;
			if( panel != self.model.current_panel ) {
				self.model.current_panel = panel;
				self.changed(\panel, panel)
			};
		},

		panic: { arg self;
			thisProcess.stop;
		},


		refresh: { arg self;
			"refresh called".debug;
			self.changed(\panel, self.model.current_panel);
		},

		init_synthdesclib: { arg self;
			SynthDesc.mdPlugin = TextArchiveMDPlugin; // plugin to store metadata on disk when storing the synthdef
			SynthDescLib.global.read("synthdefs/*.scsyndef");
		},

		test_player: { arg self, libnodename;
			var player, ep, name;
			self.model.patpool[libnodename] = libnodename;
			name = main.make_livenode_from_libnode(libnodename);
			player = main.get_node(name);
			self.current_test_player = player;
			self.model.current_panel = \editplayer;
			self.context.set_selected_node(player);

			self.main_view = ~main_view.(self);
			//self.window = self.make_window.value;

			//self.make_kb_handlers;
			//self.kb_handler[ [~modifiers.fx, ~kbfx[4]] ] = { player.node.play };
			//self.kb_handler[ [~modifiers.fx, ~kbfx[5]] ] = { player.node.stop };

			//ep = ~make_editplayer.(self, player, self.window, self.kb_handler);
			//self.make_editplayer_handlers(ep);
			//self.window.view.focus(true);
		},

		make_gui: { arg self;
			self.main_view = ~main_view.(self);
		},

		init: { arg self;
			self.init_synthdesclib;
			"ijensuisla".debug;
			self.add_node(~empty_player);
			"jensuisla".debug;
			~parse_bindings.(main.commands,~bindings);
			self.play_manager = ~make_playmanager.(self);
			self.panels.parlive = ~make_parlive.(self);
			self.panels.seqlive = ~make_seqlive.(self);
			self.context = ~make_context.(main);
			self.midi_center = ~midi_center.(self);

		}

	);
	main.init;
	main

};

~make_panel_shortcuts = { arg main, panel, cleanup=false;
	var cl;
	if(cleanup) {
		cl = { 
			if(main.model.current_panel != panel) {
				main.commands.remove_panel(panel) 
			};
		};
	} {
		cl = nil;
	};
	main.commands.add_enable([panel, \show_panel, \parlive], [\kb, ~keycode.mod.fx, ~keycode.kbfx[8]], { cl.(); main.show_panel(\parlive) });
	main.commands.add_enable([panel, \show_panel, \seqlive], [\kb, ~keycode.mod.ctrlfx, ~keycode.kbfx[8]], { cl.(); main.show_panel(\seqlive) });
	main.commands.add_enable([panel, \show_panel, \mixer], [\kb, ~keycode.mod.fx, ~keycode.kbfx[9]], { cl.(); main.show_panel(\mixer) });
	main.commands.add_enable([panel, \show_panel, \score], [\kb, ~keycode.mod.fx, ~keycode.kbfx[10]], { cl.(); main.show_panel(\score) });
	main.commands.add_enable([panel, \show_panel, \editplayer], [\kb, ~keycode.mod.fx, ~keycode.kbfx[11]], { cl.(); main.show_panel(\editplayer) });
};



