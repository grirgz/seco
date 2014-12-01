~load_curve_in_buffer = { arg buffer, curvefunc;
	var size = buffer.numFrames;
	//size.debug("load_curve_in_buffer: size");
	//buffer.debug("buffer");
	buffer.loadCollection(FloatArray.fill(size, { arg i;
		curvefunc.(i/size)
	}),0, { 
		//"done".debug;
	})
};

~load_curve_in_wavetable_buffer = { arg buffer, curvefunc;
	var size = buffer.numFrames;
	var wt, sig;
	sig = Signal.newClear(size/2);
	sig.waveFill(curvefunc, 0, 1);

	//size.debug("load_curve_in_wavetable_buffer: size");
	//buffer.debug("buffer");
	buffer.loadCollection(sig.asWavetable,0, { 
		//"done".debug;
	});
};

~load_curvelist_in_buffer = { arg buffer, curvefunclist, curve_amps;
	var size = buffer.numFrames;
	var slicesize = (size/curvefunclist.size).asInteger;
	//size.debug("load_curvelist_in_buffer: size");
	//buffer.debug("buffer");
	curvefunclist.do { arg curvefunc, idx;
		buffer.loadCollection(FloatArray.fill(slicesize, { arg i;
			curvefunc.(i/slicesize).linlin(-1,1,0,1) * curve_amps.wrapAt(idx)
		}), idx*slicesize, { 
			//"done".debug;
		})
	}
};

~load_sample_as_signal = { arg path;
	var file, sig;


	file = SoundFile.openRead(path);

	sig = Signal.newClear(file.numFrames);
	file.readData(sig);
	file.close; // close the file
	sig;
};

~load_sample_in_wavetable_buffer = { arg buffer, path;
	var sig;
	sig = ~load_sample_as_signal.(path);
	~load_signal_in_wavetable_buffer.(buffer, sig);

};

~load_sample_as_sigfunc = { arg path;
	var sig;
	sig = ~load_sample_as_signal.(path);
	{ arg x; 
		x = x % 1;
		sig[ x * sig.size ]
	};

};


~load_signal_in_wavetable_buffer = { arg buffer, sig;
	var size, fsize;
	// resamp the table to have a pow of 2 (bigger to avoid aliassing)
	// if u read many diff samples choose a bigger pow of 2
	size = buffer.numFrames;
	fsize = sig.size;
	//[fsize, size/2].debug("load_sample_in_wavetable_buffer: resampling");
	sig = sig.resamp1(size/2).as(Signal);

	// Convert it to a Wavetable
	sig = sig.asWavetable;

	buffer.loadCollection(sig);

};

~curvebank_builtin = (

//	saw4: { arg x; // bugged
//		var y;
//		y = sin(sqrt(x)/(x*2)).linlin(0,1,0,1);
//		y;
//	},
	name: "Builtin curves",

	////////////// methods

	get_keys: { arg self;
		var keys;
		//self.debug("curvebank: get_keys");
		keys = self.keys;
		keys = keys - Set[\get_keys, \known, \name];
		keys.asArray.sort;
	},
	
	//////////////// curves

	expsin: { arg x;
		var y;
		x = x % 1;
		y = sin(exp(x*2)*sin(x));
		y;
	},
	saw1: { arg x;
		var y;
		y = x % 1;
		y*2-1;
	},
	square1: { arg x;
		var y;
		x = x % 1;
		y = if(x<0.5) { -1 } { 1 };
		y;
	},
	triangle1: { arg x;
		var y;
		x = x % 1;
		y = if(x<0.5) { 2*x } { 2*(1-x) };
		y*2-1;
	},
	sin1: { arg x;
		var y;
		y = sin(x*2pi);
		y;
	},
	truesin1: { arg x;
		var y;
		y = sin(x*2pi);
		y;
	},
	sin2: { arg x;
		var y;
		y = sin(x*2pi*2);
		y;
	},
	sin4: { arg x;
		var y;
		y = sin(x*2pi*4);
		y;
	},

	// performer curves

	line1: { arg x;
		var y;
		y = x;
		y*2-1;
	},
	negline1: { arg x;
		var y;
		y = 1-x;
		y*2-1;
	},
	exp1: { arg x;
		var y;
		x = x % 1;
		y = x**2;
		y = y*2-1;
		y;
	},
	negexp1: { arg x;
		var y;
		x = x % 1;
		x = 1 - x;
		y = x**2;
		y = y*2-1;
		y;
	},
	exp2: { arg x;
		var y;
		x = x % 1;
		x = 1 - x;
		y = x**2;
		y = 0-y+1;
		y = y*2-1;
	},
	negexp2: { arg x;
		var y;
		x = x % 1;
		y = x**2;
		y = 0-y+1;
		y = y*2-1;
	},
	demisin2: { arg x;
		var y;
		x = x % 1;
		x = 1-x;
		y = sin(x*pi+(pi/2));
	},
	negdemisin2: { arg x;
		var y;
		x = x % 1;
		y = sin(x*pi+(pi/2));
	},
	unit: { arg x;
		1
	},
	negsquare1: { arg x;
		var y;
		x = x % 1;
		y = if(x<0.5) { 1 } { -1 };
		y;
	},
	demisin: { arg x;
		var y;
		x = x % 1;
		y = sin(x*pi);
		y = y*2-1;
	},

	pic1: { arg x;
		var y;
		var xx;
		var d = 1;
		xx = x % (1/d);
		xx = xx * d;
		xx = 1 - xx;
		y = xx ** 1.5 * (1+(sin(x*d*pi*0.7)/12));
		y = y*2-1;
	},
	pic2: { arg x;
		var y;
		var xx;
		var d = 2;
		xx = x % (1/d);
		xx = xx * d;
		xx = 1 - xx;
		y = xx ** 1.5 * (1+(sin(x*d*pi*0.7)/12));
		y = y*2-1;
	},
	pic4: { arg x;
		var y;
		var xx;
		var d = 4;
		xx = x % (1/d);
		xx = xx * d;
		xx = 1 - xx;
		y = xx ** 1.5 * (1+(sin(x*d*pi*0.7)/12));
		y = y*2-1;
	},

);
~curvebank_builtin.known = false; // FIXME: should be "know", is it required ? 

~class_wavetable_manager = (
	new: { arg self;
		self = self.deepCopy;

		self.wt_dict = ~curvebank_builtin;
	
		self;
	},

	get_wavetable: { arg self, key;
		self.wt_dict[key]
	},

	get_names: { arg self;
		self.wt_dict[\get_keys].(self.wt_dict);
	},
);

~class_wavetable_file = (
	new: { arg self;
		self = self.deepCopy;

		self;
	},
	
	new_from_data: { arg self, data;
		if(data.class == String) {
			~class_wavetable_sample_file.new(data);
		} {
			//TODO: curvebank lib
			~class_wavetable_sigfunc_file.new(~curvebank, data[1]);
		}
	
	},

	label: { arg self;
	
	},


	folders: { arg self;
	
	},

	files: { arg self;
	
	},

	load_in_wavetable_buffer: { arg self, buffer;
	
	},

	as_signal: { arg self;
	
	}

);

~class_wavetable_sample_file = (
	new: { arg self, path;
		self = self.deepCopy;
		//"huuu1".debug;
		self.pathname = PathName.new(path);
		self.file_kind = \sample;
		if(self.pathname.isFolder) {
			self.kind = \folder;
			if(self.pathname.fullPath.last != $/) {
				self.pathname = PathName.new(self.pathname.fullPath ++ "/");
			}
		} {
			if(self.pathname.isFile) {
				self.kind = \file;
			} {
				"ERROR: Wavetable file not found: %".format(path).postln;
			}
		};
		self;
	},

	save_data: { arg self;
		// TODO: when multiples curvebank, write a curvebank library
		self.pathname.fullPath;
	},

	label: { arg self;
		//self.pathname.fileName.debug("Label");
		if(self.kind == \folder) {
			self.pathname.folderName ++ "/";
		} {
			self.pathname.fileName;
		}
	},


	folders: { arg self;
		self.pathname.folders.collect { arg x; ~class_wavetable_sample_file.new(x.fullPath) };
	},

	files: { arg self;
		self.pathname.files.collect { arg x; ~class_wavetable_sample_file.new(x.fullPath) };
	},

	load_in_wavetable_buffer: { arg self, buffer;
		var path;
		if(self.kind == \file) {
			path = self.pathname.fullPath;
			~load_sample_in_wavetable_buffer.(buffer, path);
		};
	},

	load_in_signal: { arg self, signal;
		var sf, sig;
		if(self.kind == \file) {
			sf = SoundFile.openRead(self.pathname.fullPath);
			sf.readData(signal);
			sf.close;
			signal;
		}
	},

	as_sigfunc: { arg self, signal;
		if(self.kind == \file) {
			~load_sample_as_sigfunc.(self.pathname.fullPath);
		}
	}

);

~class_wavetable_sigfunc_file = (
	new: { arg self, bank, funcname;
		self = self.deepCopy;
		self.bank = bank;
		self.funcname = funcname;
		self.file_kind = \sigfunc;
		if(funcname.isNil) {
			self.kind = \folder;
		} {
			self.kind = \file;
		};
		self;
	},

	label: { arg self;
		//self.debug("class_wavetable_sigfunc_file: label");
		if(self.kind == \folder) {
			(self.bank.name ?? "Nameless sigbank") ++ "/";
		} {
			self.funcname
		}
	},

	save_data: { arg self;
		// TODO: when multiples curvebank, write a curvebank library
		[self.label, self.funcname];
	},


	folders: { arg self;
		// sigbank dont implemented sub-banks
		[]
	},

	files: { arg self;
		if(self.kind == \folder) {
			self.bank[\get_keys].(self.bank).collect { arg name;
				~class_wavetable_sigfunc_file.new(self.bank, name);
			}
		} {
			[]
		};
	},

	load_in_wavetable_buffer: { arg self, buffer;
		if(self.kind == \file) {
			~load_curve_in_wavetable_buffer.(buffer, self.bank[self.funcname]);
		};
	},

	load_in_signal: { arg self, signal;
		var sf, sig;
		if(self.kind == \file) {
			sig = signal.waveFill(self.bank[self.funcname]);
			sig;
		}
	},

	as_sigfunc: { arg self, signal;
		if(self.kind == \file) {
			self.bank[self.funcname];
		}
	}

);


~class_load_wavetable_dialog = (
	apply_done: false,
	new: { arg self, apply_action, cancel_action, path, single=false;
		// FIXME: path is not used
		var wtpath, librarylist, folderlist, filelist, filepath, selectedlist = List.new, selectedfile;
		var paths;
		self = self.deepCopy;
		//"ciiion0".debug;

		self.numframes = 4096*2;
		self.buffer = Buffer.alloc(s, self.numframes);
		self.signal = Signal.newClear(self.numframes/2);

		//paths = ~passive_config.wavetable_paths;
		// FIXME: hardcoded
		paths = [
			// put here path to directory containing wavetable files (simple short .wav)
			"~/Musique/archwavetable/Architecture Waveforms 2010 Wav24/Architecture Waveforms 2010 Wav24",
			"~/Musique/Arty_Wavetable_Pack/",
		];

		librarylist = [
			~class_wavetable_sigfunc_file.new(~curvebank_builtin),
		] ++ paths.collect { arg path;
			~class_wavetable_sample_file.new(path);
		};
		//folderlist = PathName.new(wtpath).folders;
		folderlist = [];
		//"con0".debug;

		self.window = Window.new("choose wavetable", Rect(0,0,1300,400));
		self.window.onClose = {
			self.buffer.free;
			self.synthnode.release;
			if(self.apply_done.not) {
				cancel_action.();
			}
		};
		self.layout = HLayoutView.new(self.window, Rect(0,0,1300,400));

		//"con1".debug;
		self.librarylistview = ListView.new(self.layout, Rect(0,0,300,400));
		self.librarylistview.items = librarylist.collect{ arg folder; folder.label };
		self.librarylistview.action = { arg view, b, c;
			//[a, b, c].debug("libaction");
			folderlist = librarylist[view.value].folders;
			//librarylist[view.value].debug("library elm");
			//folderlist.debug("folderlist");
			self.folderlistview.items = folderlist.collect { arg file; file.label };
			self.folderlistview.value = 0;
			self.folderlistview.action.value(self.folderlistview);
		};

		//"con2".debug;
		self.folderlistview = ListView.new(self.layout, Rect(0,0,300,400));
		self.folderlistview.items = folderlist.collect{ arg folder; folder.label };
		self.folderlistview.action = { arg view, b, c;
			//[view, b, c].debug("folderaction");
			//view.value = view.value ?? 0;
			//view.value = 0;
			//view.value.debug("view value");
			if(view.value.notNil and: {folderlist[view.value].notNil}) {
				filelist = folderlist[view.value].files;
				self.filelistview.items = filelist.collect { arg file; file.label };
				self.filelistview.value = 0;
				self.filelistview.action.value(self.filelistview);
			} {
				if(librarylist[self.librarylistview.value].notNil) {
					filelist = librarylist[self.librarylistview.value].files;
					self.filelistview.items = filelist.collect { arg file; file.label };
					self.filelistview.value = 0;
					self.filelistview.action.value(self.filelistview);
				}

			}
		};

		//"con3".debug;
		self.filelistview = ListView.new(self.layout, Rect(0,0,300,400));
		//self.filelistview.items = ["bla", "rah"];
		self.filelistview.action = { arg view, b, c;
			//[view, b, c].debug("filelist action");
			selectedfile = filelist[view.value];
			self.display_file(selectedfile);
		};

		self.right_layout = VLayoutView.new(self.layout, Rect(0,0,300,400));

		self.buttons_layout = HLayoutView.new(self.right_layout, Rect(0,0,300,20));

		//"con4".debug;
		self.but_play = Button.new(self.buttons_layout, Rect(0,0,50,20));
		self.but_play.states = [["Play"]];
		self.but_play.action = {
			//self.synthnode.debug("synth");
			if(self.synthnode.isNil) {
				self.displayed_file.load_in_wavetable_buffer(self.buffer);
				self.synthnode = { Osc.ar(self.buffer, MouseX.kr(20, 380), mul:0.02) ! 2  }.play;
				self.but_play.states = [["Stop"]];
			} {
				//"iou".debug;
				self.synthnode.release;
				self.synthnode = nil;
				self.but_play.states = [["Play"]];
			}
		};

		//"con5".debug;
		self.but_add = Button.new(self.buttons_layout, Rect(0,0,50,20));
		self.but_add.states = [["+ Add"]];
		self.but_add.action = {
			if(single) {
				selectedlist = [selectedfile];
			} {
				selectedlist.add(selectedfile);
			};
			self.selectedlistview.items = selectedlist.collect(_.label);
		};

		self.but_add = Button.new(self.buttons_layout, Rect(0,0,50,20));
		self.but_add.states = [["- Rem"]];
		self.but_add.action = {
			selectedlist.removeAt(self.selectedlistview.value);
			self.selectedlistview.items = selectedlist.collect(_.label);
		};

		StaticText.new(self.buttons_layout, Rect(0,0,10,20)); //spacer

		//"con6".debug;
		self.but_movedown = Button.new(self.buttons_layout, Rect(0,0,50,20));
		self.but_movedown.states = [["Down"]];
		self.but_movedown.action = {
			var pos, newpos, item;
			pos = self.selectedlistview.value;
			item = selectedlist.removeAt(pos);
			selectedlist.insert((pos+1).clip(0,selectedlist.size), item);
			self.selectedlistview.value = (pos+1).clip(0,selectedlist.size-1);
			self.selectedlistview.items = selectedlist.collect(_.label);
		};

		self.but_moveup = Button.new(self.buttons_layout, Rect(0,0,50,20));
		self.but_moveup.states = [["Up"]];
		self.but_moveup.action = {
			var pos, newpos, item;
			pos = self.selectedlistview.value;
			item = selectedlist.removeAt(pos);
			selectedlist.insert((pos-1).clip(0,selectedlist.size), item);
			self.selectedlistview.value = (pos-1).clip(0,selectedlist.size-1);
			self.selectedlistview.items = selectedlist.collect(_.label);
		};

		self.selectedlistview = ListView.new(self.right_layout, Rect(0,0,300,150));
		self.selectedlistview.items = selectedlist.collect(_.label);
		self.selectedlistview.action = { arg view, b, c;
			//[a, b, c].debug("action");
			self.display_file(selectedlist[self.selectedlistview.value]);
		};
		
		//"con7".debug;
		self.plotter = Plotter("plot", parent: self.right_layout);

		self.but_apply = Button.new(self.right_layout, Rect(0,0,80,20));
		self.but_apply.states = [["Apply"]];
		self.but_apply.action = {
			if((selectedlist.size < 1) and: {self.displayed_file.notNil}) {
				selectedlist = [self.displayed_file];
			} {
				// noop
			};
			apply_action.(selectedlist);
			self.apply_done = true;
			self.window.close;
		};

		//self.pack_layout = VLayoutView.new(self.layout, Rect(0,0,300,400));


		//self.pack_title = TextField.new(self.pack_layout, Rect(0,0,300,20));

		//self.packlistview = ListView.new(self.pack_layout, Rect(0,0,300,360));
		//self.packlistview.items = selectedlist.collect(_.fileName);
		//self.packlistview.action = { arg view, b, c;
		//	[a, b, c].debug("action");
		//	self.display_file(selectedlist[self.selectedlistview.value]);
		//};


		self.folderlistview.value = 0;
		self.librarylistview.value = 0;
		//"con8".debug;
		//self.librarylistview.debug("libview");
		self.librarylistview.action.value(self.librarylistview);
		//"con9".debug;

		self.window.front;

		self;
	},

	display_file: { arg self, file;
		var sf, sig;
		self.displayed_file = file;
		file.load_in_signal(self.signal);
		self.plotter.value = self.signal.as(Array);
		self.window.refresh;
		if(self.synthnode.notNil) {
			~load_signal_in_wavetable_buffer.(self.buffer, self.signal);
		};
	}

);

