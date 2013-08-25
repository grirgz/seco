~execute_till_special_end = { arg path;
	var res = path;
	if(File.exists(res)) {
		var file = File.open(res, "r");
		var code = file.readAllString;
		var end = code.find("// END");
		file.close;
		if(end.notNil) {
			code = code.keep(end);
		};
		res.debug("Loading buffer file");
		try {
			code.interpret;
		} { arg e;
			e.debug("ExC");
			e.throw;
			res.debug("Error when loading buffer file");
		};
	}
};

~class_veco = (
	new: { arg self;
		var unames;
		self = self.deepCopy;
		debug("NEW class_veco");
	
		unames =
			"12345678" ++
			"azertyui" ++
			"qsdfghjk" ++
			"wxcvbnVP";
		unames = unames.asList.collect(_.asSymbol);
		self.node_unames = unames;
		self.nodes = IdentityDictionary.new;

		self.init;
	
		self;
	},

	index_to_uname: { arg self, idx;
		self.node_unames[idx];
	},

	uname_to_index: { arg self, uname;
		// FIXME: node uname only
		self.node_unames.indexOf(uname)
	},

	open_project: { arg self, path;
		s.boot;
		Pdef.clear;
		self.project_path = path.standardizePath;
		self.project_path.debug("class_veco_buffer_manager.open_project: path");
		self.nodes = IdentityDictionary.new;
		self.node_unames.do { arg uname;
			self.nodes[uname] = ~class_node_buffer.new(self, uname);
		};
		self.init_project;
		self.load_data;
	},

	get_node_by_index: { arg self, idx;
		[idx].debug("get_node_by_index");
		self.nodes[self.index_to_uname(idx)]
	},

	get_node_by_uname: { arg self, uname;
		self.nodes[uname];
	},

	open_buffer: { arg self, idx, name;
		self.nodes[self.index_to_uname(idx)].open_buffer;
	},

	set_buffer_name: { arg self, idx, name;
		self.nodes[self.index_to_uname(idx)].set_name(name);
	},


	init_project: { arg self;
		var res;
		res = "%/init.scd".format(self.project_path).standardizePath;
		res.debug("Loading PROJECT init file");
		~execute_till_special_end.(res);
		self.nodes.keysValuesDo { arg uname, buf;
			buf.execute;
		};
	},

);

~class_veco_sceneset = (
	parent: ~class_veco,

	init: { arg self;
		debug("INIT class_veco_sceneset");
		self.clip = { ~launchpad_scene_clip.new(self) };
		self.edit = { ~launchpad_scene_edit.new(self) };
		self.seq = { ~launchpad_scene_seq.new(self) };
		self.seq_chooser = { ~launchpad_scene_seq_chooser.new(self) };
		self.mixer = { ~launchpad_scene_mixer.new(self) };
		self.mixer_mod = { ~launchpad_scene_mixer_mod.new(self) };
		self.mode_copy = { ~launchpad_scene_mode_copy.new(self) };
		self.mode_paste = { ~launchpad_scene_mode_paste.new(self) };
		self.mode = \clip;
		self.active_seq_points = 8.collect { arg x; Point(x,0) };
		self.clip_offset = Point(0,0);
		
	},

	data_path: { arg self;
		self.project_path +/+ "data";
	},

	load_data: { arg self;
		PathName(self.data_path).files.do { arg file;
			file.fullPath.load;
		}
	},


	set_seq_column_mode: { arg self, x;
		self.active_seq_points = 8.collect { arg y; Point(x,y) };
	},

	set_seq_row_mode: { arg self, y;
		self.active_seq_points = 8.collect { arg x; Point(x,y) };
	},

	set_seq_active_mode: { arg self;
		var ox = self.clip_offset.x;
		self.active_seq_points = 8.collect { arg x; 
			var y = self.get_node_by_index(x+ox).active_clip_index;
			Point(ox+x,y)
		};
	},

	get_nodeclip_by_uname: { arg self, uname;
		var node_uname,idx, node; 
		#node_uname, idx = uname.asString.split($.);
		idx = idx.asInteger-1;
		node_uname = node_uname.asSymbol;
		node = self.get_node_by_uname(node_uname);
		node.get_clip_by_index(idx);
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
		File.copy(from_path, to_path);
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

	path: { arg self;
		self.main.project_path +/+ (self.uname++".scd").asString;
	},

	data_path: { arg self;
		var root = self.main.project_path +/+ "data";
		var path = root +/+ (self.uname++".scd").asString;
		if(File.exists(root).not) {
			File.mkdir(root);
		};
		path;
	},

	execute_till_special_end: { arg self;
		var res = self.path;
		~execute_till_special_end.(self.path);
	},

	default_buffer_code: { arg self;
		var res = "~name = \\" ++ self.uname ++ ";\n\n";
		res;
	},

	open_buffer: { arg self;
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
	},

	execute: { arg self;
		//self.path.load;
		self.execute_till_special_end;
	},

	is_present: { arg self;
		var path = self.path;
		[self.uname, path].debug("class_code_buffer.is_present");
		if(File.exists(path)) {
			if(File.fileSize(path) > 20) {
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

	new: { arg self, main, uname;
		self = ~class_code_buffer[\new].(self, main, uname);
	
		//self.clips = 8.collect {  arg x;
		//	//var cuname = "%.c%".format(uname, x).asSymbol;
		//	var cuname = "%.%".format(uname, x).asSymbol;
		//	var clip;
		//	clip = ~class_node_clip.new(main, cuname, self);
		//	clip;
		//};
		self.clips = SparseArray.new(64, nil);
		self.active_clip_index = 0;
	
		self;
	},

	get_name: { arg self;
		self.name ?? self.uname.asSymbol
	},

	set_name: { arg self, name;
		self.name = name;
	},

	open_in_vim: { arg self;
		var uname = self.uname;
		var idx = self.main.uname_to_index(uname);
		"vim --servername scvim --remote-send '<Esc>:call SCveco_open_buffer(\"%\", %)<Enter>'".format(uname, idx).unixCmd;
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

	get_node_player: { arg self;
		Pdef(self.get_name);
	},

	is_playing: { arg self;
		self.get_node_player.isPlaying;
	},

	play_node: { arg self;
		self.uname.debug("class_node_buffer.play_node");
		self.get_node_player.play;
	},

	stop_node: { arg self;
		self.uname.debug("class_node_buffer.stop_node");
		self.get_node_player.stop;
	},

	mute_node: { arg self;
		self.get_node_player.mute;
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

	get_clip_by_index: { arg self, idx, create=true;
		//if(self.clips[idx].isNil and: create) {
		if(self.clips[idx].isNil) {
			var uname  = self.uname;
			var cuname = "%.%".format(uname, idx+1).asSymbol;
			var clip;
			clip = ~class_node_clip.new(self.main, cuname, self);
			self.clips[idx] = clip;
		};
		self.clips[idx];
	},

	isrest: { arg self, repeat=inf;
		Prout { arg ev;
			repeat.do {
				var x = 0;
				var next = self.get_clip_by_index(self.active_clip_index).stepseq[x];
				//self.active_clip_index.debug("isrest: active_clip_index");
				//self.get_clip_by_index(self.active_clip_index).stepseq.debug("isrest: stepseq");
				while {next.notNil} {
					var nextbool = if(next == 1) { false } { true };
					ev = nextbool.yield;
					x = x + 1;
					next = self.get_clip_by_index(self.active_clip_index).stepseq[x];
				}
			}
		};
	},

);

~class_node_clip = (
	parent: ~class_code_buffer,

	new: { arg self, main, uname, parent;
		self = ~class_code_buffer[\new].(self, main, uname);
	
		self.parent_buffer = {parent};
	
		self;
	},

	stepseq: [1,1,0,0, 0,0,0,0],

	save_data: { arg self;
		if(self.is_present) {
			File.use(self.data_path, "w") { arg file;
				var code;
				code = "Veco.save(%).stepseq = %;\n".format(self.uname.asCompileString, self.stepseq.asCompileString);
				file.write(code);
			};
		}
	},

	load_data: { arg self;
		if(self.is_present) {
			self.data_path.load;
		};
	
	},

	load_data_from_clip: { arg self, clip;
		if(self.is_present.not) {
			var code;
			self.stepseq = clip.stepseq.deepCopy;
			File.copy(clip.path, self.path);
		}
	},

	get_abspos: { arg self;
		var x, y;
		x = self.main.uname_to_index(self.parent_buffer.uname);
		y = self.parent_buffer.clips.indexOf(self);
		Point(x,y)
	},

	execute: { arg self;
		var path;
		self.define_variables;
		self.execute_till_special_end;
	},

	play_node: { arg self;
		var res = self.execute;
		[self.uname,res].debug("class_node_clip.play_node: res");
		if(res == \stop) {
			self.parent_buffer.stop_node;
		} {
			self.parent_buffer.play_node;
		}
	},

	response_on: { arg self;
		self.parent_buffer.active_clip_index = self.parent_buffer.clips.indexOf(self);
		self.parent_buffer.active_clip_index.debug("class_node_clip.response_on: active_clip_index");
		self.play_node;
	},

	is_active: { arg self;
		self.parent_buffer.active_clip_index == self.parent_buffer.clips.indexOf(self);
	},

	define_variables: { arg self;
		var idx = 1; // TODO
		~name = self.parent_buffer.get_name;
		~namex = self.parent_buffer.get_name +++ idx;
		~index = idx; //TODO
	},

	open_in_vim: { arg self;
		var uname = self.uname;
		self.define_variables;
		"vim --servername scvim --remote-send '<Esc>:drop %<Enter>'".format(self.path).unixCmd;
	},


	toggle_play: { arg self;
	},
);

///////////////////////////////

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
