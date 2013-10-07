~execute_till_special_end = { arg path;
	var res = path;
	if(File.exists(res)) {
		var file;
		var code;
		var end;
		File.use(res, "r", { arg file;
			code = file.readAllString;
		});
		end = code.find("// END");
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
		self.node_unames = unames;
		self.nodes = IdentityDictionary.new;
		//self.lib_path = "~/.local/share/SuperCollider/Extensions/seco/seco/vlive/lib/".standardizePath;
		self.lib_path = "~/code/sc/seco/vlive/lib/".standardizePath;

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

	project_destructor: { arg self;
		Pdef.clear;
		BufferPool.release_client(\veco_audio);
	},

	open_project: { arg self, path;
		s.boot;
		~initialize_jack.();
		self.project_destructor;

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
		//[idx].debug("get_node_by_index");
		self.nodes[self.index_to_uname(idx)]
	},

	get_node_by_uname: { arg self, uname;
		var res = self.nodes[uname];
		res;
	},

	open_buffer: { arg self, idx, name;
		self.nodes[self.index_to_uname(idx)].open_buffer;
	},

	// just open the file based on filename in vim
	//open_subbuffer: { arg self, uname, idx;
	//	self.get_node_by_uname(uname).get_clip_by_index(idx).open_buffer
	//},

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


	clip_offset: { arg self;
		self.var_clip_offset;
	},

	clip_offset_: { arg self, val;
		self.var_clip_offset = val;
		self.changed(\clip_offset);
	},

	clipboard_clip: { arg self;
		//self.var_clipboard_clip
		"get clipboard!!!!".debug;
		~veco_clipboard_clip;
	},

	clipboard_clip_: { arg self, val;
		//self.var_clipboard_clip
		"set clipboard!!!!".debug;
		~veco_clipboard_score_storage = ~score_storage;
		~veco_clipboard_clip = val;
	},



	init: { arg self;
		var aa, bb, ab;
		debug("INIT class_veco_sceneset");

		self.clip = { ~launchpad_scene_clip.new(self) };
		self.edit = { ~launchpad_scene_edit.new(self) };
		self.seq = { ~launchpad_scene_seq.new(self) };
		self.seq_chooser = { ~launchpad_scene_seq_chooser.new(self) };
		self.mixer = { ~launchpad_scene_mixer.new(self) };
		self.mixer_mod = { ~launchpad_scene_mixer_mod.new(self) };
		self.mode_copy = { ~launchpad_scene_mode_copy.new(self) };
		self.mode_paste = { ~launchpad_scene_mode_paste.new(self) };
		self.launchpad = { ~launchpad_scene.new(self) };

		[\clip, \edit, \seq, \seq_chooser, \mixer, \mixer_mod, \mode_copy, \mode_paste, \launchpad].do { arg key;
			var scene = self[key].value;
			self[key] = { scene };
		};

		self.mode = \clip;
		self.active_seq_points = 8.collect { arg x; Point(x,0) };
		self.msize = Point(64,64);
		self.var_clip_offset = Point(0,0);
		//self.board_offset = Point(0,0);
		self.board_size = Point(64/2,64);
		self.fadeout_time = 4;
		self.seq_offset = 0;
		
	},

	board_offset: { arg self;
		Point(
			(self.clip_offset.x / self.board_size.x).asInteger * self.board_size.x,
			(self.clip_offset.y / self.board_size.y).asInteger * self.board_size.y,
		)
	},

	get_node_by_uname: { arg self, uname;
		var res = self.nodes[uname];
		if(res.isNil) {
			res = self.get_nodeclip_by_uname(uname)
		};
		res;
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

	get_nodeclip_by_uname: { arg self, uname;
		var node_uname,idx, node; 
		#node_uname, idx = uname.asString.split($.);
		if(idx.isNil) {
			"Error: uname is not in a nodeclip uname format".postln;
			nil;
		} {
			idx = idx.asInteger-1;
			node_uname = node_uname.asSymbol;
			node = self.get_node_by_uname(node_uname);
			node.get_clip_by_index(idx);
		}
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
		var root = self.main.data_path;
		var path = root +/+ (self.uname++".scd").asString;
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
				code = "Veco.save(%).amp_level = %;\n".format(self.uname.asCompileString, self.data.amp_level);
				file.write(code);
			};
		}
	},

	load_data: { arg self;
		if(self.is_present) {
			self.data_path.load;
		};
	
	},

	get_node_player: { arg self;
		Pdef(self.get_name);
	},

	is_playing: { arg self;
		self.get_node_player.hasEnded.not and: {  self.get_node_player.isPlaying };
		//self.get_node_player.wasStopped.not;
	},

	play_node: { arg self;
		self.uname.debug("class_node_buffer.play_node");
		self.play_node_hook;
		self.get_node_player.play;
	},

	play_node_hook: { arg self;
		
	},

	stop_node: { arg self, use_quant, quant_fun;
		self.uname.debug("class_node_buffer.stop_node");
		if(use_quant == true) {
			TempoClock.default.play({
				self.get_node_player.stop;
				self.stop_node_hook;
				quant_fun.value;
				nil;
			}, self.get_node_player.quant);
			//Task({
			//	self.get_node_player.stop;
			//	quant_fun.value;
			//}).play(TempoClock.default, self.get_node_player.quant);
		} {
			//self.get_node_player.player.xstop(self.get_node_player.quant);
			self.get_node_player.player.stop(self.get_node_player.quant);
			self.stop_node_hook;
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

	get_clip_by_index: { arg self, idx, create=true;
		//if(self.clips[idx].isNil and: create) {
		if(self.clips[idx].isNil) {
			var uname  = self.uname;
			var cuname = "%.%".format(uname, idx+1).asSymbol;
			var clip;
			clip = ~class_node_clip.new(self.main, cuname, self);
			self.clips[idx] = clip;
			clip.load_data;
		};
		self.clips[idx];
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
		self.data.amp_level = val;
	},

	set_norm_amp_level: { arg self, val;
		self.data.amp_level = ~amp_level_spec.map(val);
	},

	mixer: { arg self;
		Pbind(
			//\amp, Pfunc({ arg ev; self.amp_level * ev[\amp]})
			\amp, Pfunc({ arg ev; self.amp_level })
		)
	},

);

~amp_level_spec = ControlSpec(0.001,4,\exp,0,1);

~class_node_clip = (
	parent: ~class_code_buffer,

	new: { arg self, main, uname, parent;
		self = ~class_code_buffer[\new].(self, main, uname);
	
		self.parent_buffer = {parent};
	
		self;
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
	envir_keys: [\name, \namex, \index],

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
		// no! now load in clip.new
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

	load_data_from_clip: { arg self, clip;
		if(self.is_present.not) {
			var code;
			self.stepseq = clip.stepseq.deepCopy;
			File.copy(clip.path, self.path);

			// score

			clip.save_data;
			Sdef.load_data_from_clip(clip, self);


			self.save_data;
		};
	},

	get_abspos: { arg self;
		var x, y;
		x = self.main.uname_to_index(self.parent_buffer.uname);
		y = self.parent_buffer.clips.indexOf(self);
		Point(x,y)
	},

	execute: { arg self;
		var path;
		self.envir_do {
			self.execute_till_special_end;
		}
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
		self.parent_buffer.old_active_clip_index = self.parent_buffer.active_clip_index;
		self.parent_buffer.active_clip_index = self.parent_buffer.clips.indexOf(self);
		self.parent_buffer.active_clip_index.debug("class_node_clip.response_on: active_clip_index");
		self.play_node;
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
		var uname = self.uname;
		self.define_variables;
		"vim --servername scvim --remote-send '<Esc>:drop %<Enter>'".format(self.path).unixCmd;
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
