~class_veco_buffer_manager = (
	project_path: "~/.local/share/SuperCollider/Extensions/seco/seco/vlive/v1",
	//root_path: "~/.local/share/SuperCollider/Extensions/seco/seco/vlive/",
	//projects: IdentityDictionary.n
	counter: IdentityDictionary.new,
	buffers: (
		keys:
			"&2\"'(-7_" ++
			//"&é\"'(-è_" ++
			//"12345678" ++
			"azertyui" ++
			"qsdfghjk" ++
			"wxcvbn,;",
		files:
			"12345678" ++
			"azertyui" ++
			"qsdfghjk" ++
			"wxcvbnVP",
		buffer_count: 32,
		names: IdentityDictionary.new, // index to name
		nodes: IdentityDictionary.new, // index to playing objects
		buffer_presence: Array2D.new(8,8),
		subbuffer_presence: Array2D.new(8,8),
	),

	default_buffer_code: { arg self, name;
		var res = "~name = \\" ++ name ++ ";\n\n";
		//res = res + "(\nPdef(~name, Pbind(\n\t\\degree, 0,\n));\n);\n");
		res;
	},

	new: { arg self;
		self = self.deepCopy;
	
		self.nodes_viewer = ~class_veco_nodes_viewer.new(self);

		self.buffers.files.do { arg name, idx;
			name = name.asString;
			self.buffers.names[idx] = name.asSymbol;
		};
		self.update_buffer_presence;
		Mdef.init;
	
		self;
	},

	open_project: { arg self, path;
		//self.project_path = (self.root_path +/+ path).standardizePath;
		self.project_path = path.standardizePath;
		self.project_path.debug("class_veco_buffer_manager.open_project: path");
		self.names = IdentityDictionary.new;
		self.nodes = IdentityDictionary.new;
		self.init_project;
	},

	init_project: { arg self;
		var res;
		res = "%/init.scd".format(self.project_path).standardizePath;
		res.debug("Loading PROJECT init file");
		self.exec_until_special_end(res);
		self.buffers.buffer_count.do { arg idx;
			self.init_buffer(idx)
		};
	},

	exec_until_special_end: { arg self, file;
		var res = file;
		if(File.exists(res)) {
			var file = File.open(res, "r");
			var code = file.readAllString;
			var end = code.find("// END");
			file.close;
			if(end.notNil) {
				code = code.keep(end);
			};
			res.debug("Loading buffer init file");
			try {
				code.interpret;
			} { arg e;
				e.debug("ExC");
				e.throw;
				res.debug("Error when loading buffer init file");
			};
		}
	},

	init_buffer: { arg self, idx;
		var res;
		res = "%/%.scd".format(self.project_path, self.buffers.files[idx]).standardizePath;
		self.exec_until_special_end(res)
	},


	index_to_buffer_path: { arg self, idx, subidx;
		var res;
		if(subidx.notNil) {
			res = "%/%.%.scd".format(self.project_path, self.buffers.files[idx], subidx).standardizePath;
		} {
			res = "%/%.scd".format(self.project_path, self.buffers.files[idx]).standardizePath;
		};
		[idx, res].debug("index_to_buffer_path:");
		res;
	},

	name_to_index: { arg self, name;
		var res = self.buffers.names.findKeyForValue(name);
		if(res.isNil) {
			res = self.buffers.files
		};
		res;
	},

	get_buffer_presence: { arg self;
		self.buffers.buffer_presence;
	},
	
	get_subbuffer_presence: { arg self, idx;
		self.update_subbuffer_presence(idx);
		self.buffers.subbuffer_presence;
	},

	update_buffer_presence: { arg self;
		32.do { arg idx;
			var name = self.index_to_name(idx);
			var x = idx % 8;
			var y = (idx / 8).asInteger;
			name.debug("update_buffer_presence");
			name = name ?? self.buffers.files[idx].asSymbol;
			name.debug("update_buffer_presence 2");
			if(name.notNil) {
				if(Pdef(name).source.notNil) {
					self.buffers.buffer_presence[x,y] = 1
				} {
					self.buffers.buffer_presence[x,y] = 0
				}
			} {
				self.buffers.buffer_presence[x,y] = 0
			}
		}
	},

	update_subbuffer_presence: { arg self, line_index;
		64.do { arg idx;
			var path;
			//var name = self.index_to_name(idx);
			var x = idx % 8;
			var y = (idx / 8).asInteger;
			path = self.index_to_buffer_path(line_index * 8 + y, x+1);
			if(File.exists(path)) {
				if(File.fileSize(path) > 3) {
					self.buffers.subbuffer_presence[x,y] = 1
				} {
					self.buffers.subbuffer_presence[x,y] = 0
				}
			} {
				self.buffers.subbuffer_presence[x,y] = 0
			}
		}
	},

	index_to_name: { arg self, idx;
		self.buffers.names[idx]
	},

	open_buffer: { arg self, idx, name;
		var file;
		var path;
		[idx, name].debug("class_veco_buffer_manager: open_buffer: idx, name");
		self.buffers.names.debug("names");

		path = self.index_to_buffer_path(idx);
		if(File.exists(path)) {

			path.debug("open_buffer: already exists");
		} {
			file = File.new(path, "w");
			path.debug("open_buffer: first time");
			file.write(self.default_buffer_code(self.buffers.files[idx]));
			file.close;
		};
	},

	set_buffer_name: { arg self, idx, name;
		[idx, name].debug("class_veco_buffer_manager.set_buffer_name: idx, name");
		self.buffers.names[idx] = name;
	},

	exec_subbuffer: { arg self, idx, subidx;
		var file;
		var name = self.index_to_name(idx) ?? self.buffers.files[idx];

		// subidx start to one
		//subidx = subidx - 1;
		//if(subidx == 1) {
		//	self.init_buffer(idx)
		//} {
		//	~name = name;
		//	file = self.index_to_buffer_path(idx, subidx);
		//	file.debug("exec_subbuffer: loading");
		//	file.load;
		//	file.debug("exec_subbuffer: end loading");
		//};

		~name = name;
		file = self.index_to_buffer_path(idx, subidx);
		file.debug("exec_subbuffer: loading");
		self.exec_until_special_end(file);
		//file.load;
		file.debug("exec_subbuffer: end loading");
	},

	vim_open_buffer: { arg self, idx;
		var file = self.buffers.files[idx];
		"vim --servername scvim --remote-send '<Esc>:call SCveco_open_buffer(\"%\", %)<Enter>'".format(file, idx).unixCmd;
	},

	vim_open_subbuffer: { arg self, idx, subidx;
		var path = self.index_to_buffer_path(idx, subidx);
		[idx, subidx].debug("vim_open_subbuffer: idx, subidx");
		// FIXME: escape spaces
		"vim --servername scvim --remote-send '<Esc>:drop %<Enter>'".format(path).debug("SUBBBBUUNUIX").unixCmd;
	},

	copy_buffer: { arg self, src_idx, dst_idx;
		
		var srcfile = File.new(self.index_to_buffer_path(src_idx), "r");
		var dstfile = File.new(self.index_to_buffer_path(dst_idx), "w");
		var str;
		str = srcfile.read;
		srcfile.close;
		dstfile.write(str);
		dstfile.close;
	},

	show_gui: { arg self;
		self.nodes_viewer.show_window;
	},


	toggle_play_node_by_index: { arg self, index;
		var node;
		var name;
		var i = index;
		var res;
		i.debug("play node:");
		name = self.buffers.names[i];
		name = name ?? self.buffers.files[i].asSymbol;
		if(name != nil) {
			node= self.buffers.nodes[name];
			name.debug("play node:");
			node = node ?? Pdef(name);
			if(self.counter[i] == 1) {
				node.stop;
				self.counter[i] = 0;
				res = false;
			} {
				node.play;
				self.counter[i] = 1;
				res = true;
			}
		};
		res;
	},


);

~class_veco_nodes_viewer = (
	counter: IdentityDictionary.new,
	lastnode: 0,
	new: { arg self, buffer_manager;
		self = self.deepCopy;
	
		self.buffer_manager = { buffer_manager };
		
		self.get_main = { Mdef.main };
	
		self;
	},

	show_window: { arg self;
		if(self.window.notNil) {
			self.window.close;
		};
		self.make_window;
	},

	make_layout: { arg self;
		var layout;
		layout = VLayout(
			self.buffer_manager.buffers.names

		);
		self.layout = layout
	},

	make_window: { arg self;
		self.window = Window.new("nodes viewer");
		self.binding_responder = self.get_main.commands.make_binding_responder(\nodes_viewer, [
			[\close_window, {
				self.window.close;
			}],
			[\play_node, 32, { arg i;
				self.buffer_manager.toggle_play_node_by_index(i);
				self.lastnode = i;
			}],
			[\exec_subbuffer, 8, { arg i;
				self.buffer_manager.exec_subbuffer(self.lastnode, i);
			}],
		]);
		self.window.view.keyDownAction = self.binding_responder.get_kb_responder(\nodes_viewer);
		self.window.front;
		
	},

);

