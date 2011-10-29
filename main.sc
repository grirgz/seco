
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

	header_cell: Color.newHex("BBBBA9")

);

~make_view_responder = { arg parent, model, message_responders; 
	var controller;

	controller = SimpleController(model);

	Dictionary.newFrom(message_responders).keysValuesDo { arg key, val;
		controller.put(key, val)
	};

	parent.onClose = parent.onClose.addFunc { controller.remove };

	model.refresh()
};

// ==========================================
// INCLUDES
// ==========================================

[
	"synth",
	"keycode", 
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




~main_view = { arg controller;

	var window, sl_layout, ps_col_layout, curbank, address;
	var width = 1350, height = 800;
	var parent;

	
	window = GUI.window.new("seq", Rect(50, 50, width, height));
	window.view.decorator = FlowLayout(window.view.bounds); // notice that FlowView refers to w.view, not w
	//parent = window;

	sl_layout = GUI.hLayoutView.new(window, Rect(0,0,width,60*6));
	//parent = window;
	parent = sl_layout;

	//parent.view.background = ~editplayer_color_scheme.background;
	~make_view_responder.(parent, controller, (

		panel: { arg obj, msg, panel;
			block { arg break;
				panel.debug("main view: changing to panel");
				"pan0".debug;
				if([\seqlive, \parlive].includes(panel), {
					"pan1".debug;
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
							if (controller.panels.parlive.get_selected_cell.name == \void) { 
								"FORBIDDEN: can't edit empty player".inform;
								break.value 
							};
							"pan3".debug;
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
		spacekind: \par, // par, seq
		selected_node: ~make_empty_parnode.(),

		get_selected_node: { arg self;
			self.selected_node;
		},

		get_selected_node_set: { arg self;
			switch(self.spacekind,
				\par, {
					//main.panels.parlive.model.datalist.debug("context:get_selected_node_set:datalist"); // FIXME: hardcoded values
					main.panels.parlive.model.datalist[(self.parbank*8)..][..11]; // FIXME: hardcoded values
				},
				\seq, {
					main.panels.seqlive.model.datalist[(self.seqbank*8)..][..11]; // FIXME: hardcoded values
				}
			)
		},

		get_selected_bank: { arg self;
			switch(self.spacekind,
				\par, {
					self.parbank
				},
				\seq, {
					self.seqbank
				}
			)

		}
	);

};

~mk_sequencer = {

	var main;

	main = (
		model: (
			current_panel: \parlive,
			clipboard: nil,


			nodelib: nil,
			presetlib: nil,
			samplelib: nil,

			patpool: nil,
			livenodepool: nil
		),

		context: \to_init,

		archive_livenodepool: { arg self, projpath;
			var dict = Dictionary.new;
			self.model.livenodepool.keysValuesDo { arg key, val;
				(key -> [val.defname, val.save_data]).writeArchive(projpath++"/livenode_"++key);
			};
		},

		unarchive_livenodepool: { arg self, projpath;
			var path;
			var pool = Dictionary.new;
			path = PathName.new(projpath);
			path.entries.do { arg file;
				var fullname, name, asso;
				fullname = file.fullPath;
				name = file.fileName;
				if(name[..8] == "livenode_", {
					asso = Object.readArchive(fullname);
					pool[asso.key] = ~make_player_from_synthdef.(asso.value[0]);
					pool[asso.key].load_data( asso.value[1] );
				});
			};
			pool;
		},

		save_project: { arg self, name;
			var proj;

			("mkdir "++name).unixCmd;
			proj = ();
			proj.patlib = self.model.patlib;
			proj.patpool = self.model.patpool;
			proj.parlive = self.model.parlive;
			proj.state = self.state;
			proj.volume = s.volume.volume;

			self.archive_livenodepool(name);
			
			proj.writeArchive(name++"/core");

		},
		
		load_project: { arg self, name;
			var proj;
			proj = Object.readArchive(name++"/core");
			if(proj.notNil, {
				self.model.patlib = proj.patlib.debug("patlib=============================");
				self.model.patpool = proj.patpool.debug("patpool===============================");
				self.model.livenodepool = self.unarchive_livenodepool(name).debug("livenodepool==================");
				self.state = proj.state.debug("state====================================");
				self.model.parlive = proj.parlive.debug("parlive=================================");
				s.volume.volume = proj.volume;
				//self.model = pro[0];
				//self.state = pro[1];
			}, {
				("Project `"++name++"' can't be loaded").postln
			});
		},

		panels: (
			//seqlive: ~make_seqlive.(main),
			seqlive: \to_init,
			parlive: \to_init
		),
		
		load_patlib: { arg self, patlist;
			var patpool, patlib, bank = 0, ix = 0, iy = 0;
			patpool = Dictionary.new;

			patlib = ~matrix3_from_list.(patlist, { arg asso;
				patpool[asso.key] = asso.value;
				asso.key;
			});
			self.model.patlib = patlib;
			self.model.patlist = patlist.collect { arg asso; asso.key };
			self.model.patpool = patpool;
		},

		load_samplelib: { arg self, samplelist;
			self.model.samplelist = samplelist;
			self.model.samplelib = samplelist;

		},

		show_panel: { arg self, panel;
			self.model.current_panel = panel;
			self.changed(\panel, panel)
		},

		commands: ~shortcut,

		refresh: { arg self;
			"refresh called".debug;
			self.changed(\panel, self.model.current_panel);
		},

		make_gui: { arg self;
			self.main_view = ~main_view.(self);
		},

		init: { arg self;
			self.panels.parlive = ~make_parlive.(self);
			self.context = ~make_context.(main);

		}

	);
	main.init;
	main

};


