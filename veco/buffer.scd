///////////////////////////
// open_project iter on nodes by node_unames to create and .load_data each node
// when the view (launchpad:clip) try to get a clip, it is created if don't exist in node.get_clip_by_index
//
//
///////////////////////////

~execute_till_special_end = { arg path, skip_first_assign=false;
	var res = path;
	path.debug("Try loading buffer file");
	if(File.exists(res)) {
		var file;
		var code;
		var end;
		File.use(res, "r", { arg file;
			code = file.readAllString;
		});
		end = code.find("\n// END");
		if(end.notNil) {
			code = code.keep(end+1);
		};

		if(skip_first_assign) {
			if(code.beginsWith("~name =")) {
				//code.debug("BEFORE=======================================================================");
				code = code.drop(code.find("\n")+1);
				//code.debug("AFTER=======================================================================");
			}
		};

		res.debug("Loading buffer file");
		try {
			code.interpret;
		} { arg e;
			e.debug("ExC");
			e.throw;
			res.debug("Error when loading buffer file");
		};
	} {
		path.debug("execute_till_special_end: File don't exists");
	}
};

~strip_end_slash = { arg path;
	if(path.last == $/) {
		path = path.drop(-1)
	};
	path;
};

~class_veco = (
	new: { arg self;
		var unames;
		var more_unames;
		self = self.deepCopy;
		debug("NEW class_veco");
	
		unames =
			"12345678" ++
			"azertyui" ++
			"qsdfghjk" ++
			"wxcvbnVP";
		more_unames = 32.collect { arg x; (x + 32).asSymbol };
		unames = unames.asList.collect(_.asSymbol);
		unames = unames ++ more_unames;
		//unames = unames.collect({ arg x;  })
		self.node_unames = unames;
		self.nodes = IdentityDictionary.new;
		//self.lib_path = "~/.local/share/SuperCollider/Extensions/seco/seco/vlive/lib/".standardizePath;
		self.lib_path = "~/code/sc/seco/vlive/lib/".standardizePath;
		self.prefixed_uname_mode = true;

		self.init;
	
		self;
	},

	uname_to_puname: { arg self, uname;
		// puname is uname prefixed with project name
		if(self.prefixed_uname_mode) {
			(self.project_name ++ "-" ++ uname).asSymbol
		} {
			uname;
		}
		//self.prefixed_node_unames = unames.collect({ arg x; self.project_name })
	},

	name: { arg self;
		self.project_name.asSymbol;
	},

	puname_to_uname: { arg self, uname;
		var res;
		var inuname = uname;
		res = if(self.prefixed_uname_mode) {
			uname = uname.asString;
			if(uname.beginsWith(self.project_name)) {
				uname = uname.replace(self.project_name++ "-", "")
			};
			uname.asSymbol;
		} {
			uname;
		};
		//[inuname, res].debug("puname_to_uname");
		res;
	},

	index_to_uname: { arg self, idx;
		self.uname_to_puname(self.node_unames[idx]);
	},

	uname_to_index: { arg self, uname;
		// FIXME: node uname only
		self.node_unames.indexOf(self.puname_to_uname(uname))
	},

	project_destructor: { arg self;
		if(self.project_path.notNil) {
			self.save_data;
		};
		//Pdef.clear;
		BufferPool.release_client(\veco_audio);
	},

	open_project: { arg self, path;
		var user_path;
		var project_name;
		s.boot;
		~initialize_jack.();
		//self.project_destructor;

		self.project_path = path.standardizePath;
		self.project_path = ~strip_end_slash.(self.project_path);

		user_path = ~strip_end_slash.(Veco.user_path);
		project_name = PathName(self.project_path).asRelativePath(user_path);
		project_name = project_name.replace("/", "");
		self.project_name = project_name;

		~veco_project_path = self.project_path;
		self.project_path.debug("class_veco_buffer_manager.open_project: path");
		self.nodes = IdentityDictionary.new;
		self.node_unames.do { arg uname;
			uname = self.uname_to_puname(uname);
			uname.debug("open_project: create node");
			// create buffers objects and load saved data (mainly stepseq and amp)
			"hh1".debug;
			self.nodes[uname] = ~class_node_buffer.new(self, uname);
			"hh2".debug;
			self.nodes[uname].load_data;
			"hh3".debug;
		};
		"init project".debug("===");
		self.init_project; // load init files and execute node buffers
		//self.load_data;
	},

	open_project_in_vim: { arg self, name;
		"vim --servername scvim --remote-send '<Esc>:VecoOpenProject %<Enter>'".format(name).unixCmd;
	},

	get_node_by_index: { arg self, idx;
		//[idx].debug("get_node_by_index");
		var res;
		if(idx < self.msize.x) {
			res = self.nodes[self.index_to_uname(idx)]
			//[idx,res.uname].debug("get_node_by_index: home board");
		} {
			// other project mode
			var target_project;
			target_project = self.pane_target_project;
			if(target_project.notNil and:{ target_project.class == Environment }) {
				res = target_project[\veco].get_node_by_index(idx - self.msize.y);
				[idx,res.uname, target_project[\veco].project_name].debug("get_node_by_index: alien board");
			};
			res;
		};
		res;
	},

	split_puname: { arg self, in;
		var pnode, clip;
		var project, node;
		// format of a puname: project-node.clip
		in.debug("split_puname");
		in = in.asString;
		#pnode, clip = in.split($.);
		#project, node = in.split($-);
		[pnode, clip, project, node].debug("[pnode, clip, project, node]");
		if(node.isNil) {
			node = project;
			project = nil;
		};
		(
			is_puname: project.notNil,
			project: project.asString,
			node: node.asSymbol,
			project_node: pnode.asSymbol,
			clip: clip,
		).debug("split_puname: suname");
	},

	get_node_by_uname: { arg self, uname;
		// this parent class is used by no one and this function is redefined, stop using it

		//var res;
		//var project_name;
		//var suname;

		//suname = self.split_puname(uname);
		//if(
		//	suname.is_puname and: {
		//		suname.project != self.project_name and: {
		//			suname.project == self.pane_target_project[\veco].project_name
		//		}
		//	}
		//) {
		//	res = self.pane_target_project[\veco].get_node_by_uname(uname)
		//} {
		//	if(suname.is_puname.not) {
		//		uname = self.uname_to_puname(uname);
		//	};
		//	res = self.nodes[uname];
		//};

		//res;
	},

	open_buffer: { arg self, idx, name;
		self.nodes[self.index_to_uname(idx)].open_buffer;
	},

	// just open the file based on filename in vim
	//open_subbuffer: { arg self, uname, idx;
	//	self.get_node_by_uname(uname).get_clip_by_index(idx).open_buffer
	//},

	set_buffer_name: { arg self, idx, name;
		// ignore ~name init
		//self.nodes[self.index_to_uname(idx)].set_name(name);
	},


	init_project: { arg self;
		var res;
		var data;
		res = "%/init.scd".format(self.project_path).standardizePath;
		data = "%/init.scd".format(self.data_path).standardizePath;
		res.debug("Loading PROJECT init file");
		~execute_till_special_end.(res);
		~execute_till_special_end.(data);
		self.nodes.keysValuesDo { arg uname, buf;
			buf.execute;
		};
	},

);

~class_veco_sceneset = (
	parent: ~class_veco,
	trash_path: "/tmp/",


	clip_offset: { arg self;
		self.var_clip_offset;
	},

	clip_offset_: { arg self, val;
		self.var_clip_offset = val;
		self.changed(\clip_offset);
	},

	save_data: { arg self;
		var file = self.data_path +/+ "init.scd";
		File.use(file, "w", { arg fh;
			fh.write("Veco.main.clip_offset = " ++ self.var_clip_offset.asCompileString++";\n");
		});
	},

	clipboard_clip: { arg self;
		//self.var_clipboard_clip
		"get clipboard!!!!".debug;
		topEnvironment[\veco_clipboard_clip];
	},

	clipboard_clip_: { arg self, val;
		//self.var_clipboard_clip
		"set clipboard!!!!".debug;
		topEnvironment[\veco_clipboard_score_storage] = ~score_storage;
		topEnvironment[\veco_clipboard_clip] = val;
	},

	clipboard_mode_: { arg self, val;
		topEnvironment[\veco_clipboard_mode] = val;
	},

	clipboard_mode: { arg self;
		topEnvironment[\veco_clipboard_mode];
	},

	clipboard_action_: { arg self, val;
		topEnvironment[\veco_clipboard_action] = val;
	},

	clipboard_action: { arg self;
		topEnvironment[\veco_clipboard_action];
	},



	init: { arg self;
		var aa, bb, ab;
		var scenes;
		debug("INIT class_veco_sceneset");

		scenes = ();
		self.scenes = scenes;
		scenes.clip = { ~launchpad_scene_clip.new(self) };
		scenes.edit = { ~launchpad_scene_edit.new(self) };
		scenes.seq = { ~launchpad_scene_seq.new(self) };
		scenes.seq_chooser = { ~launchpad_scene_seq_chooser.new(self) };
		scenes.mixer = { ~launchpad_scene_mixer.new(self) };
		scenes.mixer_mod = { ~launchpad_scene_mixer_mod.new(self) };
		scenes.mode_copy = { ~launchpad_scene_mode_copy.new(self) };
		scenes.mode_cut = { ~launchpad_scene_mode_cut.new(self) };
		scenes.mode_link = { ~launchpad_scene_mode_link.new(self) };
		scenes.mode_paste = { ~launchpad_scene_mode_paste.new(self) };
		scenes.launchpad = { ~launchpad_scene.new(self) };
		scenes.quickpad = { ~launchpad_scene_codepads.new(self.quickpad_controller) };
		scenes.score_preset = { ~launchpad_scene_score_preset_display.new(self) };
		scenes.choose_score_preset = { ~launchpad_scene_choose_score_preset_display.new(self) };
		scenes.score_notes = { ~launchpad_scene_score_notes_display.new(self) };

		self.quickpad_controller = ~class_codepads.new;
		self.piano_recorder = ~class_piano_recorder.new;
		self.recorder_view = ~class_recorder_view.new;
		self.quickpad_controller.veco = {self};

		self.scenes.keysValuesDo { arg key, val;
			var scene = self.scenes[key].value;
			scene.scene_name = key;
			self[key] = { scene };
		};

		self.pane_rect = Rect(100,100,100,100);
		self.pane_offset = Point(0,32);
		self.pane_target_project = nil;

		self.mode_immortal_mixers = true;
		self.mixers_main_fadetime = 0.5;

		self.mode = \clip;
		self.mode_push_again = \stop; // deprecated
		self.mode_play_row = \stop_slices;
		self.clip_response_mode = \toggle;
		self.active_seq_points = 8.collect { arg x; Point(x,0) };
		self.msize = Point(64,64);
		self.var_clip_offset = Point(0,0);
		//self.board_offset = Point(0,0);
		self.board_size = Point(64/2,64);
		self.fadeout_time = 4;
		self.seq_offset = 0;

		self.master_bus = 0;
		
	},

	open_file_in_editor: { arg self, path;
		if(Platform.ideName == "scvim") {
			"vim --servername scvim --remote-send '<Esc>:drop %<Enter>'".format(path).unixCmd;
		} {
			if(File.exists(path).not) {
				var file;
				file = File.new(path, "w");
				file.write("\n");
				file.close;
			}; 
			path.openDocument;
		}
	},

	delete_node: { arg self, uname;
		var node = self.nodes[uname];
		if(node.notNil) {
			node.uname.debug("nooooo!!! deleting a node file!!!");
			File.copy(node.path, self.trash_path +/+ "%.%.%".format(
				self.project_name, PathName.new(node.path).fileName, Date.localtime.stamp
			));
			File.delete(node.path);

			File.copy(node.data_path, self.trash_path +/+ "%.data.%.%".format(
				self.project_name, PathName.new(node.path).fileName, Date.localtime.stamp
			));
			File.delete(node.data_path);

			// deleting nodes is handled by caller

			//self.clips[idx] = nil;
			// TODO: delete score too
			// score ?
			// - sdef
				// call delete clip on score manager
				// - audio file
				// - preset
			// - stepseq
			// -
			self.nodes[uname] = ~class_node_buffer.new(self, uname);
		}
	},

	board_offset: { arg self;
		// board_offset is the first index of current slice
		Point(
			(self.clip_offset.x / self.board_size.x).asInteger * self.board_size.x,
			(self.clip_offset.y / self.board_size.y).asInteger * self.board_size.y,
		)
	},

	get_node_by_uname: { arg self, uname;
		// get node by uname or index, get clip by clip_uname or point
		var res;
		uname.debug("veco.get_node_by_uname: uname");
		if(uname.isNumber) {
			res = self.get_node_by_index(uname);
		};
		if(uname.class == Point) {
			res = self.get_node_by_index(uname.x).get_clip_by_index(uname.y);
		};
		if(res.isNil) {
			if(self.prefixed_uname_mode) {
				var project_name;
				var suname;

				suname = self.split_puname(uname);
				suname.debug("suname");
				[suname.project.asCompileString, self.project_name.asCompileString].debug("supro, selpro");
				self.pane_target_project.class.debug("pantarpro");
				if(self.pane_target_project.notNil and: {self.pane_target_project[\veco].notNil}) {
					self.pane_target_project[\veco].project_name.asCompileString.debug("pataproname");
				};
				if(
					self.pane_target_project.notNil and: {
						suname.is_puname and: {
							suname.project != self.project_name and: {
								suname.project == self.pane_target_project[\veco].project_name
							}
						}
					}
				) {
					suname.debug("suname2");
					res = self.pane_target_project[\veco].get_node_by_uname(uname);
					res.class.debug("res0");
				} {
					if(suname.is_puname.not) {
						suname = self.split_puname(self.uname_to_puname(uname));
					};
					uname.debug("uname3");
					suname.debug("suname3");
					self.nodes.keys.asArray.sort.debug("nodes");
					res = self.nodes[suname.project_node];
					res.class.debug("res1");
					if(res.notNil and: {suname.clip.notNil}) {
						res = res.get_clip_by_index(suname.clip.asInteger - 1);
						res.class.debug("res2");
					};
				};

			} {
				res = self.nodes[uname];
				res.class.debug("resX");
			};
		};
		res.class.debug("mais hein????????");
		if(res.isNil) {
			uname.debug("Error: uname not found");
			//"uname not found".throw;
			"wtf".debug;
			res = nil;
		};
		res;
	},

	get_nodeclip_by_uname: { arg self, uname;
		var node_uname,idx, node; 
		#node_uname, idx = uname.asString.split($.);
		node_uname.debug("get_node_by_uname: node_uname");
		if(idx.isNil) {
			~uname.debug("Error: uname is not in a nodeclip uname format").postln;
			"uname is not in a nodeclip uname format".throw;
			nil;
		} {
			self.get_node_by_uname(uname);
		}
	},

	data_path: { arg self;
		var root = self.project_path +/+ "data";
		if(File.exists(root).not) {
			File.mkdir(root);
		};
		root;
	},

	load_data: { arg self;
		// load at clip init instead

		//PathName(self.data_path).files.do { arg file;
		//	if(file.extension == "scd") {
		//		file.fullPath.load;
		//	}
		//}
	},


	set_seq_column_mode: { arg self, x;
		self.active_seq_points = 8.collect { arg y; Point(x,y+self.clip_offset.y) };
	},

	set_seq_row_mode: { arg self, y;
		self.active_seq_points = 8.collect { arg x; Point(x+self.clip_offset.x,y) };
	},

	set_seq_active_mode: { arg self;
		var ox = self.clip_offset.x;
		self.active_seq_points = 8.collect { arg x; 
			var y = self.get_node_by_index(x+ox).active_clip_index;
			Point(ox+x,y)
		};
	},


	//get_buffer_at_point: { arg self, point;
	//	var idx, node;
	//	[point].debug("get_buffer_at_point");
	//	idx = point.x + (point.y*8);
	//	node = self.get_node_by_index(idx);
	//	[point, idx].debug("get_buffer_at_point");
	//	node;
	//},

	copy_buffer: { arg self, from_path, to_path;
		// used in load_data_from_clip
		File.copy(from_path, to_path);
		[from_path,to_path].debug("copying buffer file");
		self.disable_tags(to_path);
	},

	disable_tags_in_string: { arg self, code;
		code.replace("\n// TAGS:", "\n// xTAGS:");
	},

	disable_tags: { arg self, path;
		if(File.exists(path)) {
			var code;
			File.use(path, "r", { arg file;
				//"bed".debug;
				code = file.readAllString;
				//"afd".debug;
			});
			code = self.disable_tags_in_string(code);
			File.use(path, "w", { arg file;
				//"bed".debug;
				file.write(code);
				//"afd".debug;
			});
		} {
			( "ERROR: file not found: "++ path ).postln;
		};
		
	},

);

~class_code_buffer = (
	new: { arg self, main, uname;
		self = self.deepCopy;
	
		self.uname = uname;
		self.main = { main };

		self.init;
		
	
		self;
	},

	bare_uname: { arg self;
		self.main.puname_to_uname(self.uname);
	},

	path: { arg self;
		self.main.project_path +/+ (self.bare_uname++".scd").asString;
	},

	data_path: { arg self;
		var root = self.main.data_path;
		var path = root +/+ (self.bare_uname++".scd").asString;
		path;
	},

	execute_till_special_end: { arg self;
		var res = self.path;
		~execute_till_special_end.(self.path);
	},

	execute_till_special_end_skip_first_line: { arg self;
		var res = self.path;
		~execute_till_special_end.(self.path,true);
	},

	default_buffer_code: { arg self;
		var res = "~name = \\" ++ self.uname ++ ";\n\n";
		res;
	},

	open_buffer: { arg self;
		// called by vim when opening a buffer
		// put default code if file is new and define local variables
		var path = self.path;
		var file;
		if(File.exists(path)) {
			path.debug("open_buffer: already exists");
		} {
			path.debug("open_buffer: first time");
			file = File.new(path, "w");
			file.write(self.default_buffer_code);
			file.close;
		};
		self.define_variables;
	},

	execute: { arg self;
		//self.path.load;
		self.execute_till_special_end;
	},

	is_present: { arg self;
		var path = self.path;
		//[self.uname, path].debug("class_code_buffer.is_present");
		if(File.exists(path)) {
			if(File.fileSize(path) > 15) {
				true
			} {
				false
			}
		} {
			false
		}
	},


	response_on: { arg self;
		self.execute;
	},

	response_off: { arg self;
		
	},
);

~class_node_buffer = (
	parent: ~class_code_buffer,
	eventclass: "class_node_buffer",

	envir_keys: [\name],

	new: { arg self, main, uname;
		self = ~class_code_buffer[\new].(self, main, uname);
	
		//self.clips = 8.collect {  arg x;
		//	//var cuname = "%.c%".format(uname, x).asSymbol;
		//	var cuname = "%.%".format(uname, x).asSymbol;
		//	var clip;
		//	clip = ~class_node_clip.new(main, cuname, self);
		//	clip;
		//};
		self.data = ();
		self.data.amp_level = 1;
		self.clips = SparseArray.new(64, nil);
		self.active_clip_index = 0;
		self.val_play_node_hook_dict = IdentityDictionary.new;
		self.val_stop_node_hook_dict = IdentityDictionary.new;
		self.mixer_sends = List.new;
		self.has_mixer = false;
		self.lib = (); // disposable store for the user
	
		self;
	},

	execute: { arg self;
		//self.path.load;
		if(self.main.prefixed_uname_mode) {
			self.envir_do {
				self.execute_till_special_end_skip_first_line;
			}
		} {
			self.envir_do {
				self.execute_till_special_end;
			}
		}
	},

	clip_response_mode: { arg self;
		if(self.val_clip_response_mode.isNil) {
			self.main.clip_response_mode
		} {
			self.val_clip_response_mode
		}
	},

	clip_response_mode_: { arg self, val;
		if(self.val_clip_response_mode != val) {
			self.val_clip_response_mode = val;
			//self.save_data; // not really needed since nodes are executed at project start
		} 
	},

	master_bus: { arg self;
		self.main.master_bus;
	},

	get_name: { arg self;
		self.name ?? self.uname.asSymbol
	},

	set_name: { arg self, name;
		self.name = name;
	},

	define_variables: { arg self;
		var idx = self.get_index;
		self.variables = ();
		self.envir_keys.do { arg key;
			self.variables[key] = currentEnvironment[key];
		};
		~name = self.get_name;
	},

	envir_do: { arg self, fun;
		self.define_variables;
		fun.();
		self.undefine_variables;
	},

	//open_in_vim_OLD: { arg self;
	//	var uname = self.uname;
	//	var idx = self.main.uname_to_index(uname);
	//	"vim --servername scvim --remote-send '<Esc>:call SCveco_open_buffer(\"%\", %)<Enter>'".format(uname, idx).unixCmd;
	//},

	open_in_vim: { arg self;
		// compat
		self.open_in_editor;
	},

	open_in_editor: { arg self;
		var uname = self.uname;
		self.define_variables;
		self.main.open_file_in_editor(self.path);
	},

	pdef_is_present: { arg self;
		var name;
		name = self.get_name;
		if(name.notNil) {
			if(Pdef(name).source.notNil) {
				true
			} {
				false
			}
		} {
			false
		}
	},

	delete_self: { arg self;
		self.main.delete_node(self.uname);
	},

	delete_clip: { arg self, idx;
		var clip = self.clips[idx];
		if(clip.notNil) {
			clip.uname.debug("nooooo!!! deleting a file!!!");
			File.copy(clip.path, self.main.trash_path +/+ "%.%.%".format(
				self.main.project_name, PathName.new(clip.path).fileName, Date.localtime.stamp
			));
			File.delete(clip.path);

			File.copy(clip.data_path, self.main.trash_path +/+ "%.data.%.%".format(
				self.main.project_name, PathName.new(clip.path).fileName, Date.localtime.stamp
			));
			File.delete(clip.data_path);

			self.clips[idx] = nil;
			// TODO: delete score too
			// score ?
			// - sdef
				// call delete clip on score manager
				// - audio file
				// - preset
			// - stepseq
			// -
		}
	},

	load_data_from_node: { arg self, node;
		if(self.is_present.not and: { File.exists(node.path) }) {
			var code;
			var code_header;
			var new_code_header;
			var code_body;
			File.use(node.path, "r", { arg file;
				code = file.readAllString;
			});
			code = code.split($\n);
			code_header = code[0];
			code_body = code.drop(1);
			//code_header.interpret;
			new_code_header = "~name = %;".format(self.uname.asCompileString);
			code = ([new_code_header] ++ code_body).join($\n);
			//self.stepseq = clip.stepseq.deepCopy;
			code = self.main.disable_tags_in_string(code);
			File.use(self.path, "w", { arg file;
				file.write(code);
			});
			self.data = node.data.deepCopy;
			self.save_data;
			self.execute;
		}
	},

	save_data: { arg self;
		if(self.is_present) {
			File.use(self.data_path, "w") { arg file;
				var code;
				code = "Veco.save(%).amp_level = %;\n".format(self.bare_uname.asCompileString, self.data.amp_level);
				file.write(code);
			};
		}
	},

	load_data: { arg self;
		self.data_path.debug("load_data: data_path");
		if(self.is_present) {
			self.data_path.debug("load_data: now load");
			self.data_path.load;
			self.data_path.debug("load_data: end load");
		};
	
	},

	get_node_player: { arg self;
		Pdef(self.get_name);
	},

	is_playing: { arg self;
		var player = self.get_node_player;
		self.get_node_player.hasEnded.not and: {  self.get_node_player.isPlaying };
		
		// Pdef and Ndef API are too incompatible to be polymorphed here, so always use Pdef for the moment

		//if(player.class == Pdef) {
		//	self.get_node_player.hasEnded.not and: {  self.get_node_player.isPlaying };
		//} {
		//	self.get_node_player.isPlaying
		//}
		//self.get_node_player.wasStopped.not;
	},

	play_node: { arg self;
		self.uname.debug("class_node_buffer.play_node");
		self.play_node_hook;
		self.play_node_hook_dict;
		self.get_node_player.play;
	},

	add_play_node_hook: { arg self, key, fun;
		self.val_play_node_hook_dict[key] = fun;
	},

	play_node_hook_dict: { arg self;
		self.val_play_node_hook_dict.keysValuesDo { arg key, val;
			val.value;
		}
	},

	play_node_hook: { arg self;
		
	},

	stop_node: { arg self, use_quant, quant_fun;
		self.uname.debug("class_node_buffer.stop_node");
		if(use_quant == true) {
			TempoClock.default.play({
				self.get_node_player.stop;
				self.stop_node_hook;
				self.stop_node_hook_dict;
				quant_fun.value;
				nil;
			}, self.get_node_player.quant);
			//Task({
			//	self.get_node_player.stop;
			//	quant_fun.value;
			//}).play(TempoClock.default, self.get_node_player.quant);
		} {
			//self.get_node_player.player.xstop(self.get_node_player.quant);
			//self.get_node_player.player.stop(self.get_node_player.quant);
			self.get_node_player.stop;
			self.stop_node_hook;
			self.stop_node_hook_dict;
		}
	},

	add_stop_node_hook: { arg self, key, fun;
		self.val_stop_node_hook_dict[key] = fun;
	},

	stop_node_hook_dict: { arg self;
		self.val_stop_node_hook_dict.keysValuesDo { arg key, val;
			val.value(self.uname);
		}
	},


	stop_node_hook: { arg self;
		
	},

	fadeout: { arg self, time;
		self.get_node_player.player.xstop(time ?? self.main.fadeout_time);
	},

	mute_node: { arg self;
		self.get_node_player.player.mute;
	},

	unmute_node: { arg self;
		self.get_node_player.player.unmute;
	},

	toggle_mute: { arg self;
		var pl = self.get_node_player.player;
		if(pl.notNil) {
			if(pl.muteCount == 1) {
				pl.unmute;
			} {
				pl.mute;
			}
		}
	},

	is_muted: { arg self;
		var pl = self.get_node_player.player;
		if(pl.notNil) {
			pl.muteCount == 1
		} {
			false
		}
	},


	toggle_play: { arg self;
		self.uname.debug("class_node_buffer.toggle_play");
		if(self.is_playing != true) {
			self.play_node;
		} {
			self.stop_node;
		};
		
	},

	response_on: { arg self;
		self.toggle_play;
	},

	/////////////// access

	get_clip_by_index: { arg self, idx, create=true;
		//if(self.clips[idx].isNil and: create) {

		if(self.clips[idx].isNil or: { self.clips[idx].eventclass == \class_empty_clip }) {
			var uname  = self.uname;
			var cuname = "%.%".format(uname, idx+1).asSymbol;
			var clip;
			if(idx < self.main.msize.y) {
				clip = ~class_node_clip.new(self.main, cuname, self);
			} {
				var proj;
				var ppath;
				var target_uname ;
				var target_cuname;
				proj = self.main.pane_target_project;
				if(proj.notNil and: { proj.class == Environment }) {
					ppath = proj[\veco].project_path;
					target_uname = PathName(ppath+/+"").asRelativePath((Veco.user_path+/+"").drop(-1)).drop(-1);
					target_cuname = "%.%".format(self.bare_uname, idx+1 - self.main.pane_offset.y).asSymbol;
					clip = ~class_shortcut_clip.new(self.main, cuname, self, ppath, target_cuname);
				} {
					clip = ~class_empty_clip.new(self.main, cuname,self);
				}
			};
			self.clips[idx] = clip;
			clip.load_data;
		};
		//[idx, create, self.clips[idx].path].debug("class_node_buffer.get_clip_by_index");
		self.clips[idx];
	},

	/////////////// step sequencer part

	stepseq: { arg self;
		var clip = self.get_clip_by_index(self.active_clip_index);
		clip.stepseq;
	},

	isrest: { arg self, size = 8, repeat=inf;
		var seq;
		var clip;
		Prout { arg ev;
			repeat.do {
				var x = 0;
				var next;
				//var next = self.get_clip_by_index(self.active_clip_index).stepseq[x];
				clip = self.get_clip_by_index(self.active_clip_index);
				seq = clip.stepseq;
				//clip.stepseq_size = size; // parent pattern set wrong sizeat the same time
				if(seq.size < size) {
					clip.stepseq = seq.extend(size, 0);
					seq = clip.stepseq;
				};
				next = seq[x];
				//self.active_clip_index.debug("isrest: active_clip_index");
				//self.get_clip_by_index(self.active_clip_index).stepseq.debug("isrest: stepseq");
				while { x < size} {
					var nextbool = if(next == 1) { false } { true };
					ev = nextbool.yield;
					x = x + 1;
					next = self.get_clip_by_index(self.active_clip_index).stepseq[x];
				}
			}
		};
	},



	//////////// mixer part

	mixer_node: { arg self;
		var name = self.uname;
		Ndef(name+++\mixer);
	},

	make_simple_mixer: { arg self, send_list;
		var name = self.uname;
		var make_out;
		send_list = send_list ?? [~fx1, ~fx2, ~fx3, ~fx4];
		BusDef(name+++\mixer, \audio, 2);
		self.add_play_node_hook(\mixer, { 
			Ndef(name+++\mixer).parentGroup = ~get_mixer_group.();
			//Ndef(name+++\mixer).play;
			Ndef(name+++\mixer).play(self.master_bus);
		});
		self.add_stop_node_hook(\mixer, { Ndef(name+++\mixer).stop });
		self.has_mixer = true;

		make_out = { arg i, sig, send;
			if(send_list[i].notNil) {
				Out.ar(send_list[i], sig * send);
			}
		};


		Ndef(name+++\mixer).isource = { arg preamp=1, amp=1, send1=0, send2=0, send3=0, send4=0;
			var in = In.ar(BusDef(name+++\mixer), 2);
			var sig;
			sig = in;
			sig = preamp * in;

			make_out.(0, sig, send1);
			make_out.(1, sig, send2);
			make_out.(2, sig, send3);
			make_out.(3, sig, send4);
			sig = sig * amp;
			sig;
		};

	},

	make_mixer: { arg self, send_list, mixer_mode;
		var name = self.uname;
		var make_out;
		var make_mix;
		var mode;

		if(mixer_mode.notNil) {
			self.mixer_mode = mixer_mode;
		};
		if(self.mixer_mode.isNil) {
			self.mixer_mode = \default;
		};
		mode = self.mixer_mode;

		send_list = send_list ?? [~fx1, ~fx2, ~fx3, ~fx4];
		BusDef(name+++\mixer, \audio, 2);
		self.add_play_node_hook(\mixer, { 
			Ndef(name+++\mixer).parentGroup = ~get_mixer_group.();
			Ndef(name+++\mixer).play(self.master_bus);
		});
		self.add_stop_node_hook(\mixer, { 
			if(self.main.mode_immortal_mixers == false) {
				var fadetime;
				fadetime = Ndef(name+++\mixer).getHalo(\fadeOut) ?? Ndef(name+++\mixer).fadeTime;
				Ndef(name+++\mixer).end(fadetime + self.main.mixers_main_fadetime)
			};
		});
		self.has_mixer = true;

		make_out = { arg i, sig, send;
			var mamp = 1;
			if(send_list[i].notNil) {
				Out.ar(send_list[i], sig * send);
			}
		};
		
		make_mix = { arg i, amp, send;
			i = i + 1;
			if(mode == (\mix+++i) or: { mode == (\premix+++i) }) {
				1-send
			} {
				amp;
			};
		};


		Ndef(name+++\mixer).put(0, { arg preamp=1;
			var in = In.ar(BusDef(name+++\mixer), 2);
			var sig;
			sig = preamp * in;
			//if(mode == \preamp or: { mode.asString.beginsWith("premix") }) {
			//	sig = \mamp.kr(1);
			//};
			sig;
		});

		Ndef(name+++\mixer).put(100, \filter -> { arg in, send1=0, send2=0, send3=0, send4=0;
			var sig;
			//xfade = xfade.clip(0,1);
			sig = in;
			sig = sig;

			make_out.(0, sig, send1);
			make_out.(1, sig, send2);
			make_out.(2, sig, send3);
			make_out.(3, sig, send4);

			in;
		});

		Ndef(name+++\mixer).put(200, \filter -> { arg in, amp=1, send1=0, send2=0, send3=0, send4=0;
			var sig;

			amp = make_mix.(0, amp, send1);
			amp = make_mix.(1, amp, send2);
			amp = make_mix.(2, amp, send3);
			amp = make_mix.(3, amp, send4);

			sig = in * amp;
			//if(mode == \amp or: { mode.asString.beginsWith("mix") }) {
			//	sig = \mamp.kr(1);
			//};
			sig;

		})

	},

	//// amp (pattern)

	amp_level: { arg self, in;
		//[self.uname, self.data.amp_level].debug("amp_level");
		self.data.amp_level;
	},

	get_norm_amp_level: { arg self;
		~amp_level_spec.unmap(self.data.amp_level);
	},

	get_amp_level: { arg self;
		self.data.amp_level;
	},

	set_amp_level: { arg self, val;
		self.set_amp_level_hook(val);
		self.data.amp_level = val;
	},

	set_norm_amp_level: { arg self, val;
		self.set_amp_level( ~amp_level_spec.map(val));
	},

	//// volume (Ndef)

	volume_level: { arg self, in;
		//[self.uname, self.data.amp_level].debug("amp_level");
		//self.data.volume_level;
		self.mixer_node.vol;
	},

	get_norm_volume_level: { arg self;
		self.mixer_node.vol;
		//~volume_level_spec.unmap(self.data.amp_level);
	},

	get_volume_level: { arg self;
		//self.data.volume_level;
		self.mixer_node.vol;
	},

	set_volume_level: { arg self, val;
		//self.data.volume_level = val;
		self.mixer_node.vol = val;
	},

	set_norm_volume_level: { arg self, val;
		//self.data.amp_level = ~amp_level_spec.map(val);
		self.mixer_node.vol = val;
	},

	//// send

	set_send_level: { arg self, idx, val;
		Ndef(self.uname+++\mixer).set(\send+++(idx+1), val);
	},

	get_send_level: { arg self, idx;
		Ndef(self.uname+++\mixer).get(\send+++(idx+1));
	},

	mixer: { arg self;
		var bus;
		var dict = self.val_play_node_hook_dict; 
		bus = case
			{ dict[\fx].notNil } {
				BusDef(self.uname+++\fx)
			}
			{  dict[\mixer].notNil } {
				BusDef(self.uname+++\mixer)
			};

		if(bus.notNil) {
			Pbind(
				//\amp, Pfunc({ arg ev; self.amp_level * ev[\amp]})
				\out, bus,
				\amp, Pfunc({ arg ev; self.amp_level })
			)
		} {
			Pbind(
				//\amp, Pfunc({ arg ev; self.amp_level * ev[\amp]})
				\amp, Pfunc({ arg ev; self.amp_level })
			)
		}
	},

);

~amp_level_spec = ControlSpec(0.001,4,\exp,0,1);

~class_node_clip = (
	parent: ~class_code_buffer,
	eventclass: "class_node_clip",

	new: { arg self, main, uname, parent;
		self = ~class_code_buffer[\new].(self, main, uname);
	
		self.parent_buffer = {parent};
		self.lib = ();
	
		self;
	},
	
	is_present: { arg self;
		var path = self.path;
		//[self.uname, path].debug("class_code_buffer.is_present");
		if(self[\execute_hook].notNil) {
			true
		} {
			if(File.exists(path)) {
				if(File.fileSize(path) > 15) {
					true
				} {
					false
				}
			} {
				false
			}
		}
	},


	response_off: { arg self;
		self.is_pressed = false;
		self.response_off_hook(self.parent_buffer.uname, self.uname, self.get_index);
	},

	data: { arg self;
		// compat, TODO: move stepseq in data
		self;
	},

	get_index: { arg self;
		self.parent_buffer.clips.indexOf(self);
	},

	stepseq: [1,1,0,0, 0,0,0,0],
	stepseq_size: nil,
	envir_keys: [\name, \namex, \index, \target_name, \target_namex, \target_index],

	set_step: { arg self, idx, val;
		self.stepseq[idx] = val;
		self.set_step_hook(idx, val);
	},

	save_data: { arg self;
		if(self.is_present) {
			File.use(self.data_path, "w") { arg file;
				var code;
				code = "Veco.save(%).stepseq = %;\n".format(self.bare_uname.asCompileString, self.stepseq.asCompileString);
				//if(self.response_mode != \toggle) {
				if(self.val_response_mode.notNil) {
					code = code ++ "Veco.save(%).response_mode = %;\n"
						.format(self.bare_uname.asCompileString, self.response_mode.asCompileString);
				};
				file.write(code);
			};
		}
	},

	load_data: { arg self;
		//// not called by anyone, open project just run data/*.scd files
		if(self.is_present) {
			self.data_path.load;
			[self.node_uname, self.get_index].debug("class_node_clip.load_data: index");
			Sdef.load_clip_data(self);
		};
	
	},

	node_uname: { arg self;
		self.parent_buffer.uname
	},

	delete_self: { arg self;
		self.parent_buffer.delete_clip(self.get_index);
	},

	load_data_from_clip: { arg self, clip;
		if(self.is_present.not) {
			var code;
			self.stepseq = clip.stepseq.deepCopy;
			self.val_response_mode = clip.val_response_mode;
			self.main.copy_buffer(clip.path, self.path);

			// score

			clip.save_data; // FIXME: this save can lose data if the source clip has not loaded data correctly and then is copied
			Sdef.load_data_from_clip(clip, self);


			self.save_data;
		};
	},

	get_abspos: { arg self;
		var x, y;
		debug("node clip: begin get_abspos");
		x = self.main.uname_to_index(self.parent_buffer.uname);
		y = self.parent_buffer.clips.indexOf(self);

		[x,y].debug("node clip: get_abspos");
		Point(x,y)
	},

	execute: { arg self;
		var path;
		if(self[\execute_hook].notNil) {
			self.envir_do {
				self.execute_hook;
			}
		} {
			self.envir_do {
				self.execute_till_special_end;
			}
		}
	},

	play_node: { arg self;
		var res;
		[self.uname,res].debug("class_node_clip.play_node: res");
		// FIXME: should be disabled due to a bug with Ndef which don't play if \set is run before
		//res = self.execute;
		//if(res == \stop) {
		//	self.parent_buffer.stop_node;
		//} {
		//	self.parent_buffer.play_node;
		//}
		self.parent_buffer.play_node;
		res = self.execute;
	},

	stop_node: { arg self, use_quant, stop_func;
		self.parent_buffer.stop_node(use_quant, stop_func);
	},

	response_mode: { arg self;
		if(self.val_response_mode.isNil) {
			self.parent_buffer.clip_response_mode
		} {
			self.val_response_mode
		}
	},

	response_mode_: { arg self, val;
		if(self.val_response_mode != val) {
			self.val_response_mode = val;
			self.save_data;
		} 
	},

	response_unselect: { arg self;
		[self.node_uname,self.get_index].debug("response_unselect!!!!");
		self.response_unselect_hook;
	},

	activate_clip: { arg self;
		self.parent_buffer.old_active_clip_index = self.parent_buffer.active_clip_index;
		self.parent_buffer.active_clip_index = self.parent_buffer.clips.indexOf(self);
		self.parent_buffer.active_clip_index.debug("class_node_clip.response_on: active_clip_index");
		if(self.parent_buffer.old_active_clip_index != self.parent_buffer.active_clip_index) {
			if(self.parent_buffer.old_active_clip_index.notNil) {
				self.parent_buffer.get_clip_by_index(self.parent_buffer.old_active_clip_index).response_unselect;
			}
		};
	},

	response_on_play: { arg self;
		self.activate_clip;
		self.play_node;
	},

	response_on_stop: { arg self, stop_func;
		self.parent_buffer.stop_node(true, stop_func);
	},

	response_on_toggle: { arg self, stop_func;
		if(self.is_active and: { self.parent_buffer.is_playing } ) {
			self.response_on_stop(stop_func);
		} {
			self.response_on_play;
		};
	},

	stop_hook: {},

	response_on: { arg self, stop_func;
		[self.response_mode].debug("RESPO?SE ONN");
		switch(self.response_mode,
			\toggle, {
				self.response_on_toggle(stop_func);
			},
			\play, {
				self.response_on_play;
			},
			\exec, {
				self.is_pressed = true;
				self.execute;
			},
			\execchoose, {
				self.is_pressed = true;
				self.activate_clip;
				self.execute;
			},
			\exectoggle, {
				self.is_pressed = true;
				self.execute;

				// commented because doing it in the clip file to be able to exec without using launchpad
				//if(self.exectoggle_state == 1) {
				//	// is on, put off
				//	self.exectoggle_state = 0;
				//	self.stop_hook(self.parent_buffer.uname, self.uname, self.get_index);
				//} {
				//	// is off, put on
				//	self.exectoggle_state = 1;
				//	self.execute;
				//}
			}
		)
	},

	is_active: { arg self;
		self.parent_buffer.active_clip_index == self.parent_buffer.clips.indexOf(self);
	},

	define_variables: { arg self;
		var idx = self.get_index;
		self.variables = ();
		self.envir_keys.do { arg key;
			self.variables[key] = currentEnvironment[key];
		};
		~name = self.parent_buffer.get_name;
		~namex = self.uname;
		~index = idx; 
		~target_name = ~name;
		~target_namex = ~target_namex;
		~target_index = ~target_index; 
	},

	envir_do: { arg self, fun;
		self.define_variables;
		fun.();
		self.undefine_variables;
	},

	undefine_variables: { arg self;
		self.envir_keys.do { arg key;
			currentEnvironment[key] = self.variables[key];
		};
	},

	open_in_vim: { arg self;
		// compat
		self.open_in_editor;
	},

	open_in_editor: { arg self;
		var uname = self.uname;
		self.define_variables;
		self.main.open_file_in_editor(self.path);
	},


	toggle_play: { arg self;
	},

	isrest: { arg self, size = 8, repeat=inf;
		var seq;
		var clip;
		clip = self;
		seq = clip.stepseq;
		clip.stepseq_size = size; // parent pattern set wrong sizeat the same time
		if(seq.size < size) {
			clip.stepseq = seq.extend(size, 0);
			seq = clip.stepseq;
		};
		Prout { arg ev;
			repeat.do {
				var x = 0;
				var next;
				//var next = self.get_clip_by_index(self.active_clip_index).stepseq[x];
				clip = self;
				//clip.debug("clip.isrest: clip");
				seq = clip.stepseq;
				next = seq[x];
				//self.active_clip_index.debug("isrest: active_clip_index");
				//self.get_clip_by_index(self.active_clip_index).stepseq.debug("isrest: stepseq");
				while { x < size} {
					var nextbool = if(next == 1) { false } { true };
					ev = nextbool.yield;
					x = x + 1;
					next = clip.stepseq[x];
				}
			}
		};
	},
);

////////////////////// shortcut
// pretend to be part of a project but instead read and write a file in another project

~class_shortcut_node = (
	parent: ~class_node_buffer,

	new: { arg self, main, uname, target_project_path, target_uname;
		self = ~class_node_buffer[\new].(self, main, uname);
	
		self.target_project_path = target_project_path;
		self.target_uname = target_uname;
	
		self;
	},

	save_data: { arg self;
		"class_shortcut_node: no ! dont save data!".debug;
	},

	path: { arg self;
		(self.target_project_path+/+( self.target_uname++".scd" )).asString;
	},

	data_path: { arg self;
		var path = ( self.target_project_path +/+ "data" +/+ (self.target_uname++".scd") ).asString;
		path;
	},

	load_data: { arg self;
		// called manually
		Veco.save_hook = {
			self.data;
		};
		self.data_path.debug("node: load_data: data_path");
		self.data_path.load;
		Veco.save_hook = nil;
	},

);

~class_shortcut_clip = (
	parent: ~class_node_clip,

	new: { arg self, main, uname, parent, target_project_path, target_uname;
		self = ~class_node_clip[\new].(self, main, uname, parent);
	
		self.target_project_path = target_project_path;
		self.target_uname = target_uname;
	
		self;
	},

	define_variables: { arg self;
		// TODO: change it
		var idx = self.get_index;
		self.variables = ();
		self.envir_keys.do { arg key;
			self.variables[key] = currentEnvironment[key];
		};
		~name = self.parent_buffer.get_name;
		~namex = self.uname;
		~index = idx; 
		~target_name = ~name;
		~target_namex = ~target_namex;
		~target_index = ~target_index; 
	},

	save_data: { arg self;
		"class_shortcut_clip: no ! dont save data!".debug;
	},

	path: { arg self;
		(self.target_project_path+/+( self.target_uname++".scd" )).asString;
	},

	data_path: { arg self;
		var path = ( self.target_project_path +/+ "data" +/+ (self.target_uname++".scd") ).asString;
		path;
	},

	load_data: { arg self;
		// called manually
		Veco.save_hook = {
			self.data;
		};
		self.data_path.debug("load_data: data_path");
		self.data_path.load;
		Veco.save_hook = nil;
	},
	
);

~class_empty_clip = (
	parent: ~class_node_clip,
	eventclass: \class_empty_clip,

	// RISK: can't replace with real clip since it already exists
	new: { arg self, main, uname, parent, target_project_path, target_uname;
		self = ~class_node_clip[\new].(self, main, uname, parent);
	
	
		self;
	},

	save_data: { arg self;
		"class_empty_clip: no ! dont save data!".debug;
	},
	response_off: {},

	load_data_from_clip: {},

	is_present: {false},
	is_active: {false},
	execute: {false},

	path: { arg self;
		"emptyxx";
	},

	data_path: { arg self;
		"dataemptyxx"
	},

	load_data: { arg self;
	},
	
);

/////////////////////////////// codepads


~class_codepads = (
	new: { arg self;
		self = self.deepCopy;
	
		self.gridsize = Point(8,8);
		self.codepads = IdentityDictionary.new;
		self.gridsize.x.do { arg x;
			self.gridsize.y.do { arg y;
				var key = self.pos_to_symbol(Point(x,y));
				self.codepads[key] = ~class_virtual_buffer.new(self, key)
			}
		};
	
		self;
	},

	get_codepad: { arg self, pos;
		var key;
		key = self.pos_to_symbol(pos);
		self.codepads[key];
	},

	pos_to_symbol: { arg self, pos;
		"%@%".format(pos.x,pos.y).asSymbol;
	},
);

~class_virtual_buffer = (
	parent: ~class_code_buffer,

	is_present: { arg self;
		self[\payload].notNil
	},

	record_on_event: { arg self;
		self.main.recorder.add_event_on((
			event_key: self.uname,
		));
	},

	record_off_event: { arg self;
		self.main.recorder.add_event_off(self.uname);
	},

	execute: { arg self;
		self.payload;
	},

	response_off: { arg self;
		self.payload_off;
	},

);

/////////////////////////////// not used

~class_node_subbuffer = (
	parent: ~class_code_buffer,

	new: { arg self, main, uname, parent;
		self = ~class_code_buffer[\new].(self, main, uname);
	
		self.parent_buffer = { parent };
	
		self;
	},

	execute: { arg self;
		var path;
		~name = self.parent_buffer.get_name;
		self.execute_till_special_end;
	},
	
);
