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

	open_project: { arg self, path;
		self.project_path = path.standardizePath;
		self.project_path.debug("class_veco_buffer_manager.open_project: path");
		self.nodes = IdentityDictionary.new;
		self.node_unames.do { arg uname;
			self.nodes[uname] = ~class_node_buffer.new(self, uname);
		};
		self.init_project;
	},

	get_node_by_index: { arg self, idx;
		[idx].debug("get_node_by_index");
		self.nodes[self.index_to_uname(idx)]
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
		
	},

	get_buffer_at_point: { arg self, point;
		var idx, node;
		[point].debug("get_buffer_at_point");
		idx = point.x + (point.y*8);
		node = self.get_node_by_index(idx);
		[point, idx, node].debug("get_buffer_at_point");
		node;
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
			if(File.fileSize(path) > 30) {
				true
			} {
				false
			}
		} {
			false
		}
	},

	open_in_vim: { arg self;
		var uname = self.uname;
		var idx = 1;
		"vim --servername scvim --remote-send '<Esc>:call SCveco_open_buffer(\"%\", %)<Enter>'".format(uname, idx).unixCmd;
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
	
		self.clips = 8.collect {  arg x;
			//var cuname = "%.c%".format(uname, x).asSymbol;
			var cuname = "%.%".format(uname, x).asSymbol;
			var clip;
			clip = ~class_node_clip.new(main, cuname, self);
			clip;
		};
	
		self;
	},

	get_name: { arg self;
		self.name ?? self.uname.asSymbol
	},

	set_name: { arg self, name;
		self.name = name;
	},

	is_present: { arg self;
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

	get_clip_by_index: { arg self, idx;
		self.clips[idx]
	},

);

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

~class_node_clip = (
	parent: ~class_code_buffer,

	new: { arg self, main, uname, parent;
		self = ~class_code_buffer[\new].(self, main, uname);
	
		self.parent_buffer = {parent};
	
		self;
	},

	execute: { arg self;
		var path;
		~name = self.parent_buffer.get_name;
		self.execute_till_special_end;
	},

	toggle_play: { arg self;
		self.execute;
		self.parent_buffer.play_node;
	},
);
