//"vim --servername scvim --remote-tab c.sc".unixCmd


(
	~gen_vim_bindings = {
		var res = "";
		var keys =
			"&é\"'(-è_" ++
			//"12345678" ++
			"azertyui" ++
			"qsdfghjk" ++
			"wxcvbn,;";
		var files = 
			"12345678" ++
			"azertyui" ++
			"qsdfghjk" ++
			"wxcvbnVP";
		keys.do { arg key, x;
			res = res ++ "map <F9>% <Esc>:drop %.scd\n".format(key, files[x].asString)
		};
		res.debug("res:");

	};
	~gen_vim_bindings.()
)

(

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
		names: IdentityDictionary.new,
		nodes: IdentityDictionary.new,
	),

	new: { arg self;
		self = self.deepCopy;
	
		self.nodes_viewer = ~class_veco_nodes_viewer.new(self);

		Mdef.init;
	
		self;
	},

	open_project: { arg self, path;
		//self.project_path = (self.root_path +/+ path).standardizePath;
		self.project_path = path.standardizePath;
		self.project_path.debug("class_veco_buffer_manager.open_project: path");
		self.names = IdentityDictionary.new;
		self.nodes = IdentityDictionary.new;
	},

	index_to_buffer_path: { arg self, idx;
		var res = "%/%.scd".format(self.project_path, self.buffers.files[idx]).standardizePath;
		[idx, res].debug("index_to_buffer_path:");
		res;
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
			file.write("~name = \\" ++ self.buffers.files[idx] ++ ";\n\n(\nPdef(~name, Pbind(\n\t\\degree, 0,\n));\n)\n");
			file.close;
		};
	},

	set_buffer_name: { arg self, idx, name;
		[idx, name].debug("class_veco_buffer_manager.set_buffer_name: idx, name");
		self.buffers.names[idx] = name;
	},

	vim_open_buffer: { arg self, idx;
		var file = self.buffers.files[idx];
		"vim --servername scvim --remote-send '<Esc>:call SCveco_open_buffer(\"%\", %)<Enter>'".format(file, idx).unixCmd;
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
				self.buffer_manager.toggle_play_node_by_index(i)
			}],
		]);
		self.window.view.keyDownAction = self.binding_responder.get_kb_responder(\nodes_viewer);
		self.window.front;
		
	},

);

~veco  = ~class_veco_buffer_manager.new;
)

~veco.buffers
~veco.project_path


(
~a = (
	//bla: 4,
	bla: { arg self; self[\bla] },
	bla_: { arg self, val;
		"setbla".debug;
		self[\bla] = val;
	},
	rah: { arg self;
		"kdkfjd".postln;
	},
)
)
~a.bla
~a.rah
