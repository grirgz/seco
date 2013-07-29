"=============================================================================================================================================".postln;
"	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////".postln;
"	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////".postln;
"	  						Loading Sequencer...".postln;
"	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////".postln;
"	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////".postln;
"=============================================================================================================================================".postln;

~seco_dir_path = if( thisProcess.nowExecutingPath.notNil) {
	thisProcess.nowExecutingPath.dirname
} {
	"~/code/sc/seco/".standardizePath
};

~remove_trailing_slash = { arg path;
	if(path.last == $/) {
		path.drop(-1)
	} {
		path
	}
};

~seco_root_path = "~/Musique/sc/".standardizePath;
~seco_root_path_nude = ~remove_trailing_slash.(~seco_root_path);

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
	midi_cc: (
		knob: 8,
		slider: 8
	),
	safe_inf: 580,
	bank: 10,
	float_precision: 6,
	children_per_groupnode: 16,
	children_part_per_groupnode: 4, // keyboard can access to only 4 children at the same time
	groupnode_per_bank: 8,
	wavetable_buffer_size: 2048,
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
//~editplayer_color_scheme = (
//	background: Color.white,
//	control: Color.white,
//	led: Color.white
//);

~color_scheme = (
	background: Color.newHex("94A1BA"),
	control: Color.newHex("6F88BA"),
	control2: Color.newHex("6F889A"),
	led: Color.newHex("A788BA"),
	led_ok: Color.newHex("A7BBBA"),

	header_cell: Color.newHex("BBBBA9"),

	frame_header: Color.gray(0.6),
	frame_body: Color.gray(0.7),

);
//~color_scheme = (
//	background: Color.white,
//	control: Color.white,
//	control2: Color.white,
//	led: Color.white,
//
//	header_cell: Color.white
//
//);

~make_view_responder = { arg parent, model, message_responders, auto_refresh=true; 
	var controller;

	controller = SimpleController(model);

	Dictionary.newFrom(message_responders).keysValuesDo { arg key, val;
		controller.put(key, val)
	};

	parent.onClose = parent.onClose.addFunc { 
		parent.debug("make_view_responder: close parent");
		//model.changed(\destructor);
		controller.remove;
	};

	if(auto_refresh) { model.refresh() };

	controller;
};

~make_class_responder = { arg self, parent, model, list, auto_refresh=true;
	var controller;

	controller = SimpleController(model);
	list.do { arg method;
		controller.put(method, { arg ... args; self[method].(self, *args) });
	};

	parent.onClose = parent.onClose.addFunc { controller.remove };

	if(auto_refresh) { model.refresh() };

	controller;
};

~sort_by_template = { arg list, template;
	var res = List.new;
	template.do { arg i;
		if(list.includes(i)) {
			res.add(i);
		}
	};
	list.do { arg i;
		if(res.includes(i).not) {
			res.add(i)
		}
	};
	res;

};

~find_path_difference = { arg path1, path2;
	var res = List.new;
	var size = max(path1.size, path2.size);
	size.do { arg x;
		if(path1[x] == path2[x]) {
		} {
			if(path1[x].notNil) {
				res.add(path1[x])
			} {
				res.add(path2[x])
			}
		}
	};
	res;
};

~save_archive_data = { arg self, list, data=nil;
	data = data ?? Dictionary.new;
	list.do { arg key;
		if(self[key].notNil) {
			data[key] = self[key]
		}
	};
	data
};

~load_archive_data = { arg self, list, data;
	list.do { arg key;
		if(data[key].notNil) {
			self[key] = data[key]
		}
	};
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
				pgroup.releaseDependants;
				blist.do(_.free)
			}
		});
		leffect = Pset(\group, pgroup, leffect);
		spawner.par(leffect)
	});
};

~bsustain = { Pkey(\sustain) / Ptempo() };

~penvcontrol = { arg pat, chain=nil;
	var buskeydict = Dictionary.new;
	var respat = List.new;
	var ctlpatlist = List.new;
	var pbindpat;
	var makebusmap;

	"pcontrol start".debug;
	makebusmap = { arg key;
		Pfunc { arg ev; [key, ev[key]].debug("pfunc"); ev[key].asMap }
	};
	
	if(pat.class == EventPatternProxy) {
		pbindpat = pat.source;
	} {
		pbindpat = pat
	};

	pbindpat.patternpairs.pairsDo { arg key,val;
		var buskey;
		var env;
		var cbus;
		var ctlpat;
		if(val.class == Ref) {
			buskey = "bus_" ++ key;
			respat.add(key);
			respat.add(makebusmap.(buskey));
			env = val.value;
			buskeydict[buskey] = env.levels[0];
			cbus.set(env.levels[0]);
			ctlpat = Pbind(
				\instrument, \ctlPoint,
				\value, Pseq(env.levels[1..],inf),
				\time, Pseq(env.times,inf) / Pfunc({thisThread.clock.tempo}),
				\group, Pkey(\busgroup),
				\outbus, Pfunc { arg ev; ev[buskey].index },
				\curve, env.curves,
				\dur, Pseq(env.times,inf)
			);
			ctlpatlist.add(ctlpat);
		} 
	};

	respat.debug("respat");

	Pfset({
			buskeydict.debug("penvcontrol init pfset");
			buskeydict.keysValuesDo { arg key, val;
				currentEnvironment[key] = Bus.control(s, 1);
				currentEnvironment[key].set(val);
			};
			currentEnvironment[\busgroup] = Group.new;
		},
		Pfpar(
			[
				if(chain.notNil) {
					chain <> Pbind(*respat) <> pat;
				} {
					Pbind(*respat) <> pat;
				}
			]
			++ ctlpatlist
		),
		{
			buskeydict.debug("penvcontrol cleanup pfset");
			buskeydict.keysValuesDo { arg key, val;
				currentEnvironment[key].free;
			};
			currentEnvironment[\busgroup].freeAll;
			currentEnvironment[\busgroup].free;
		}
	)
};


if(~silent_control_bus.isNil) {
	~silent_control_bus = Bus.control(s, 1);
	~silent_control_bus.set(0);
};

if(~silent_audio2_bus.isNil) {
	~silent_audio2_bus = Bus.audio(s, 2);
	//~silent_audio2_bus.set(0);
};

// ==========================================
// INCLUDES
// ==========================================

[
	"abcparser",
	"synth",
	"bindings", 
	"keycode", 
	"wavetable",
	"eventscore",
	"player_display",
	"midi",
	"param",
	"samplelib",
	"node_manager",
	"gui",
	"classinstr",
	"ci_pool",
	"player",
	"matrix",
	//"hmatrix",
	//"editplayer",
	//"mixer",
	//"score",
	//"sidematrix",
	"timeline",
	"tracks",
	"modulation",
	"side",
	"nodematrix",
].do { arg file;
	("Loading " ++ file ++".scd...").inform;
	(~seco_dir_path +/+ file++".scd").load;
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

	controller.window = window;
	
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

		focus_window: { arg self, msg, title;
			window.view.focus(true);
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
			root_path: ~seco_root_path,
			current_panel: \parlive,
			clipboard: nil,
			
			freeze_gui: false, // disable mdef gui updates

			velocity_ratio: 0.3,
			velocity_ratio_pad: 0.7,


			nodelib: List.new,
			modnodelib: List.new,
			presetlib: Dictionary.new,
			presetlib_path: nil,
			colpresetlib: Dictionary.new,

			patlist: List.new,
			patpool: Dictionary.new,
			samplelist: List.new,

			inlinegenlib: List.new,
			inlinefxlib: List.new,

			livenodepool: Dictionary.new,

			bus_mode_enabled: true,

			is_stop_using_quant: true,

			audio_overlap_time: 0.002,
			latency: 0.0,
			audio_latency: 0.046,

			multi_output: false,

			// library

			library_node_pool: Dictionary.new,
			library_node_list: List.new,
			library_fxnode_pool: Dictionary.new,
			library_fxnode_list: List.new,
			library_modnode_pool: Dictionary.new,
			library_modnode_list: List.new,
			library_inlinegen: List.new,
			library_inlinefx: List.new,

		),
		archive_modules: [\play_manager, \midi_bindings_manager],

		commands: ~shortcut,

		calcveloc: { arg self, amp, veloc, type=nil, ratio=nil;
			type = type ?? \piano;
			ratio = ratio ?? if(type == \pad) { self.model.velocity_ratio } { self.model.velocity_ratio_pad };
			[amp, ratio, veloc, (amp + (amp * ratio * (veloc-0.5)))].debug("calcveloc");
			amp + (amp * ratio * (veloc-0.5));
		},

		set_window_title: { arg self, title;
			self.changed(\title, title);
		},

		destructor: { arg self;
			self.model.livenodepool.keysValuesDo { arg name, player;
				player.destructor;
			};
			self.play_manager.freeze_buffer.free;
		},

		get_node: { arg self, name;
			var node;
			//name.debug("========get_node name");
			node = self.model.livenodepool[name];
			if(node.isNil) { 
				("Node not found:"+name).error; 
				//nil.lskdjks;
			};
			//node.debug("========get_node node");
			node;
		},

		free_node: { arg self, name;
			var node;
			node = self.get_node(name);
			self.model.livenodepool[name] = nil;
			node.destructor;
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
					if( self.node_exists(newname).not ) { break.value };
					newname.debug("Name exist already");
				};
				"make_livenodename_from_libnodename: Error, can't find free name".error;
			};
			newname;
		},


		make_newlivenodename_from_livenodename: { arg self, name;
			self.find_free_name( { 
				var idx= name.asString.findBackwards("_l");
				if(idx.notNil) {
					name[ .. name.asString.findBackwards("_l")  ] ++ "l" ++ UniqueID.next;
				} {
					name ++ "_l" ++ UniqueID.next;
				}
			})
		},

		make_livenode_from_libnode: { arg self, libnodename;
			var livenodename;
			var player;
			livenodename = self.make_livenodename_from_libnodename(libnodename);
			player = ~make_player.(main, self.model.patpool[libnodename]);
			player.name = livenodename;
			player.uname = livenodename;
			self.add_node(player);
			player.uname;
		},

		duplicate_livenode: { arg self, livenodename;
			var newlivenodename, newlivenode, newlivenode_pdict;
			newlivenodename = self.make_newlivenodename_from_livenodename(livenodename);
			newlivenodename.debug("newlivenodename");
			livenodename.debug("livenodename");
			//main.model.livenodepool.keys.debug("livenodepool");
			self.model.livenodepool[newlivenodename] = self.model.livenodepool[livenodename].clone;
			if(self.model.livenodepool[livenodename].name == self.model.livenodepool[livenodename].uname) {
				self.model.livenodepool[newlivenodename].name = newlivenodename;
			};
			self.model.livenodepool[newlivenodename].uname = newlivenodename;
			newlivenodename;
		},

		add_node: { arg self, node;
			self.model.livenodepool[node.uname] = node
		},

		context: \to_init,

		make_node_from_data: { arg self, data;
			var node;
			var options = (build_synthdef:false); // FIXME: should be named build_synthdef_and_sourcepat
			switch(data.kind,
				\player, {
					switch(data.subkind,
						\nodesampler, {
							node = ~make_nodesampler.(self);
							node.load_data(data)
						},
						{
							if(data.uname != \voidplayer) {
								node = ~make_player.(self, data.instrname);
								node.load_data(data, options);
							} {
								node = nil;
							}
						}
					)
				},
				\seqnode, {
					node = ~make_seqplayer.(self);
					node.load_data(data);
				},
				\parnode, {
					node = ~make_parplayer.(self);
					node.load_data(data);
				},
				{
					data.kind.debug("ERROR: make_node_from_data: kind not known")
				}
			
			);
			node;
		},

		make_data_from_node: { arg self, node;
			node.save_data;
		},

		archive_livenodepool: { arg self, projpath, pool=nil;
			var dict = Dictionary.new;
			var data;
			"--Begin archive_livenodepool".debug;
			if(pool.isNil) {
				pool = self.model.livenodepool
			};
			pool.keysValuesDo { arg key, val;
				if(val.uname != \voidplayer) {
					data = self.make_data_from_node(val);
					data.writeArchive(projpath+/+"node_"++key);
				}
			};
			"--End archive_livenodepool".debug;
		},

		unarchive_livenodepool: { arg self, projpath;
			var path;
			var pool = Dictionary.new;
			"--Begin unarchive_livenodepool".debug;
			path = PathName.new(projpath);
			path.entries.do { arg file;
				var fullname, name, data, node;
				//try {
					file.debug("unarchive_livenodepool file");
					fullname = file.fullPath;
					name = file.fileName;

					if(name.containsStringAt(0, "node_")) {
						data = Object.readArchive(fullname);
						data.uname.debug("unarchive_livenodepool node");
						node = self.make_node_from_data(data);
						if(node.notNil) {
							pool[node.uname] = node;
						} {
							name.debug("Loaded node is nil");
						}
					} {
						name.debug("unarchive_livenodepool: don't load unknown file");
					};
				//} { arg error;
				//	[file, error].debug("Error occured when loading file");
				//}
			};
			"--End unarchive_livenodepool".debug;
			pool;
		},

		rebuild_all_synthdef_and_sourcepat: { arg self;
			self.model.livenodepool.keysValuesDo { arg name, node;
				if(node.external_player.notNil) {
					node.external_player.build_synthdef;
					if(node.is_effect.not) { // build_synthdef already call build_real_sourcepat if is effect
						node.build_real_sourcepat;
					}
				} {
					node.build_real_sourcepat;
				};
			};
		},

		get_audio_save_path: { arg self;
			if(self.model.project_path.isNil) {
				"/tmp/"
			} {
				self.model.project_path
			}
		},

		save_current_project: { arg self;
			self.save_project(self.model.project_name);
		},

		set_project_name: { arg self, name;
			self.model.project_name = name
		},

		get_project_name: { arg self;
			self.model.project_name;
		},

		save_project: { arg self, name;
			var proj, projpath;

			proj = ();
			proj.name = name;

			proj.patlist = self.model.patlist;
			proj.patpool = self.model.patpool;

			proj.samplelist = self.model.samplelist;

			proj.volume = s.volume.volume;

			self.archive_modules.do { arg module;
				if(main[module].notNil) {
					proj[module] = main[module].save_data;
				}
			};

			proj.panels = ();
			//proj.panels.parlive = self.panels.parlive.save_data; // deprecated
			//proj.panels.seqlive = self.panels.seqlive.save_data;
			proj.panels.side = self.panels.side.save_data;

			fork {
				name.debug("Saving project");
				projpath = self.model.root_path +/+ "projects/"++name;
				self.model.project_path = projpath;
				if(projpath.pathExists == \folder) {
					("mv '%' '/tmp/%_%'".format(projpath, name, Date.gmtime.rawSeconds.asInteger)).debug("move command");
					("mv '%' '/tmp/%_%'".format(projpath, name, Date.gmtime.rawSeconds.asInteger)).unixCmd;
					2.wait;
				};
				("mkdir '%'".format(projpath)).unixCmd;
				1.wait;
				//TODO: save context

				self.archive_livenodepool(projpath);
				
				self.model.project_path = nil;
				proj.writeArchive(projpath++"/core");
				"########## PROJECT % SAVED ###########".format(projpath).postln;
			}

		},
		
		load_project: { arg self, name;
			var proj, projpath;
			projpath = self.model.root_path +/+ "projects/"++name;
			proj = Object.readArchive(projpath++"/core");

			name.debug("Loading project");

			if(proj.notNil, {
				self.model.patlist = proj.patlist.debug("patlib=============================");
				self.model.patpool = proj.patpool.debug("patpool===============================");
				self.model.samplelist = proj.samplelist;
				s.volume.volume = proj.volume;


				self.model.project_path = projpath;

				self.model.livenodepool = self.unarchive_livenodepool(projpath);
				self.rebuild_all_synthdef_and_sourcepat;
				self.add_node(~empty_player);
				self.model.livenodepool.keys.debug("unarchived livenodepool keys");

				self.archive_modules.do { arg module;
					if(proj[module].notNil) {
						main[module].load_data(proj[module]);
					}
				};

				//TODO: load context
				self.model.project_path = nil;

				//self.panels.parlive.load_data(proj.panels.parlive);
				//self.panels.seqlive.load_data(proj.panels.seqlive);
				self.panels.side.load_data(proj.panels.side);


				self.refresh;
				"########## PROJECT % LOADED ###########".format(projpath).postln;
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
					projpath = self.model.root_path +/+ "projects/presets/"++name;
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

			projpath = self.model.root_path +/+ "projects/presets/"++name;
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
			var path = self.model.root_path +/+ "projects/quicksave";
			
			//fork {
				//("rm -rf \"%\"".format(path)).debug("removing old quicksavepath").unixCmd;
				//1.wait;
				self.save_project("quicksave");
			//};

		},

		quick_load_project: { arg self;
			
			self.load_project("quicksave");

		},

		////////////////////////// Library

		lib_to_pool_and_list: { arg self, lib;
			var patpool = Dictionary.new;
			var patlist = List.new;

			lib.do { arg asso;
				if(asso.class == Association) {
					patpool[asso.key] = asso.value;
					patlist.add(asso.key);
				} {
					patpool[asso] = asso;
					patlist.add(asso);
				}
			};

			[patpool, patlist]
		},

		load_node_library: { arg self, lib;
			var patlist, patpool;
			#patpool, patlist = self.lib_to_pool_and_list(lib);

			self.model.patlist = patlist;
			self.model.patpool = patpool;
			self.model.nodelib = self.model.patlist;
			self.model.library_node_pool = patpool;
			self.model.library_node_list = patlist;
		},

		load_fxnode_library: { arg self, lib;
			var patlist, patpool;
			#patpool, patlist = self.lib_to_pool_and_list(lib);

			self.model.effectlist = patlist;
			self.model.effectlib = patlist;
			self.model.effectpool = patpool;
			self.model.library_fxnode_pool = patpool;
			self.model.library_fxnode_list = patlist;
		},

		load_modnode_library: { arg self, lib;
			var patlist, patpool;
			#patpool, patlist = self.lib_to_pool_and_list(lib);

			self.model.modnodelib = patlist;
			self.model.library_modnode_pool = patpool;
			self.model.library_modnode_list = patlist;
		},

		load_inlinegen_library: { arg self, lib;
			var list = lib;
			self.model.inlinegenlib = list;
			self.model.library_inlinegen = list;
		},

		load_inlinefx_library: { arg self, lib;
			var list = lib;
			self.model.inlinefxlib = list;
			self.model.library_inlinegen = list;
		},

		/// backward compatibility
		
		load_patlib: { arg self, inpatlist;
			self.load_node_library(inpatlist)
		},

		load_nodelib: { arg self, libnodelist;
			self.load_patlib(libnodelist);
		},

		load_effectlib: { arg self, fxlist;
			self.load_fxnode_library(fxlist);
		},

		load_modlib: { arg self, list;
			self.load_modnode_library(list);
		},

		load_inlinegenlib: { arg self, list;
			self.load_inlinegen_library(list);
		},

		load_inlinefxlib: { arg self, list;
			self.model.inlinefxlib = list;
		},

		load_samplelib: { arg self, samplelist;
			self.model.samplelist = samplelist;
		},

		append_samplelib: { arg self, samplelist;
			self.samplekit_manager.append_samplelist_to_samplekit(\default, samplelist);
			//self.model.samplelist = self.model.samplelist ++ samplelist;
		},

		load_samplelib_from_path: { arg self, path;
			var dir, entries;
			dir = PathName.new(path);
			entries = dir.files.select({arg x; ["aiff","wav","flac"].includesEqual(x.extension) }).collect(_.fullPath);
			self.load_samplelib(entries);
		},

		append_samplelib_from_path: { arg self, path;
			self.samplekit_manager.append_samplelist_to_samplekit_from_path(\default, path);
			//var dir, entries;
			//dir = PathName.new(path);
			//entries = dir.files.select({arg x; ["aiff","wav","flac"].includesEqual(x.extension) }).collect(_.fullPath);
			//self.append_samplelib(entries);
		},

		////////////////////////// 

		panels: (
			//seqlive: ~make_seqlive.(main),
			seqlive: \to_init,
			parlive: \to_init
		),

		show_panel: { arg self, panel;
			if( panel != self.model.current_panel ) {
				self.model.current_panel = panel;
				self.changed(\panel, panel)
			};
		},

		panic: { arg self;
			thisProcess.stop;
			self.play_manager.reset_state;
		},


		refresh: { arg self;
			"refresh called".debug;
			self.changed(\panel, self.model.current_panel);
		},

		init_synthdesclib: { arg self;
			SynthDesc.mdPlugin = TextArchiveMDPlugin; // plugin to store metadata on disk when storing the synthdef
			SynthDescLib.global.read(SynthDef.synthDefDir ++ "*.scsyndef");
			//SynthDescLib.global.synthDescs.keys.printAll;
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

		edit_mpdef: { arg self, name;
			var player, ep;
			player = main.get_node(name);
			self.current_test_player = player;
			self.model.current_panel = \editplayer;
			self.context.set_selected_node(player);

			self.main_view = ~main_view.(self);
		},

		freeze_do: { arg self, fun;
			if(self.model.freeze_gui.not) { fun.value }
		},


		make_gui: { arg self;
			self.main_view = ~main_view.(self);
		},

		make_side_gui: { arg self;
			self.panels.side.make_gui;
		},

		close_side_gui: { arg self;
			if(self.panels.side.window.notNil) {
				self.panels.side.window.close;
			}
		},

		focus_window: { arg self;
			self.changed(\focus_window);
		},

		init_midi: { arg self;
			if(MIDIClient.initialized == false) {
				MIDIClient.init;
			};
			"aconnect -x && aconnect 'A-PRO':1 'SuperCollider'".unixCmd;
		},

		init: { arg self;
			self.init_synthdesclib;
			"ijensuisla".debug;
			self.add_node(~empty_player);
			"jensuisla".debug;
			~parse_bindings.(main.commands,~bindings);
			self.node_manager = ~make_node_manager.(self);
			self.central_player_display = ~class_central_player_display.new(self);
			self.midi_bindings_manager = ~class_midi_bindings_manager.new(self);
			self.samplekit_manager = ~samplekit_manager;
			self.wavetable_manager = ~class_wavetable_manager.new;
			self.midi_center = ~midi_center.(self);
			self.play_manager = ~make_playmanager.(self);
			self.context = ~make_context.(main);
			self.panels.parlive = ~make_parlive.(self);
			self.panels.seqlive = ~make_seqlive.(self);
			self.panels.side = ~make_side_panel.(self);

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



