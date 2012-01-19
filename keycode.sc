
// ==========================================
// input keycode definition
// ==========================================

~kbpad8x4 = [
	[ 38, 233, 34, 39, 40, 45, 232, 95 ],
	[97, 122, 101, 114, 116, 121, 117, 105 ],
	[113, 115, 100, 102, 103, 104, 106, 107 ],
	[119, 120, 99, 118, 98, 110, 44, 59 ]
];
~kbpad8x4_flat = ~kbpad8x4.flat;
~kbcalphanum = {
	var dict = Dictionary.new;
	//TODO: only for Ctrl (262144) modifier, do others
	//NOTE: ^W close the window
	var keycodes = [
			[ 38, 233, 34, 39, 40, 45, 232, 31, 231, 224, 41, 61 ],
			[ 1, 26, 5, 18, 20, 25, 21, 9, 15, 16, 36 ],
			[ 17, 19, 4, 6, 7, 8, 10, 11, 12, 13, 249, 42], 
			[60, 24, 3, 22, 2, 14, 44, 59, 58, 33] //FIXME: complete keycodes
	];
	var alnum = [
		"1234567890)=",
		"azertyuiop^$",
		"qsdfghjklmù*",
		"<wxcvbn,;:!"
	];
	keycodes.do { arg row, rowidx;	
		row.do { arg kc, kcidx;
			dict[ alnum[rowidx][kcidx].asString ] = kc;
		};
	};
	dict;
}.value;
~kbaalphanum = {
	var dict = Dictionary.new;
	//NOTE: only for Alt and no modifier
	var keycodes = [
			[ 38, 233, 34, 39, 40, 45, 232, 31, 231, 224, 41, 61 ],
			[97, 122, 101, 114, 116, 121, 117, 105, 111,112, 36 /* ^ not working */, 36 ],
			[113, 115, 100, 102, 103, 104, 106, 107, 108, 109, 249, 42 ],
			[60, 119, 120, 99, 118, 98, 110, 44, 59 ] //FIXME: complete keycodes
	];
	var alnum = [
		"1234567890)=",
		"azertyuiop^$",
		"qsdfghjklmù*",
		"<wxcvbn,;:!"
	];
	keycodes.do { arg row, rowidx;	
		row.do { arg kc, kcidx;
			dict[ alnum[rowidx][kcidx].asString ] = kc;
		};
	};
	dict;
}.value;
~kb8x2line = [
	38, 97, 233, 122, 34, 101, 39, 114, 40, 166, 45, 121, 232, 117, 95, 105
];
~kbnumline = [
	38, 233, 34, 39, 40, 45, 232, 95, 231, 224, 41, 61
];
~kbnumpad = [
	48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
];
~numpad = (
	plus: 43,
	minus: 45
);
~modifiers = (
	fx: 8388608,
	ctrlfx: 8650752,
	ctrl: 262144,
	shift: 131072,
	alt: 524288
);
~kbfx = [
	// modifiers = 8388608
	63236, 63237, 63238, 63239, 63240, 63241, 63242, 63243, 63244, 63245, 63246, 63247
	//49,50,51,52,53,54,55,56,57,58
];
~kbarrow = (
	// modifiers = 8388608
	left: 63234,
	right: 63235,
	up: 63232,
	down: 63233
);
~kbspecial = (
	delete: 127,
	enter: 13,
	escape: 27,
	point: 46
);
~cakewalk = (
	\knob: [
		16,17,18,19, 20,21,22,23, 24
	],
	\button: [
		34,35,36,37, 38,39,40,41, 42
	],
	\toggle: [
		43,44,45,46
	],
	\slider: [
		25,26,27,28, 29,30,31,32, 33
	]
);
~midi = {
	var dico = Dictionary.new;
	~cakewalk.collect { arg v, k;
		//"v:".postln;
		//v.postln;
		//"k:".postln;
		//k.postln;
		v.do { arg raw, i;
			dico[raw] = [k, i];
		};
	};
	dico;
}.value;




~keycode = Environment.make({
	~kbpad8x4 = [
		[ 38, 233, 34, 39, 40, 45, 232, 95 ],
		[97, 122, 101, 114, 116, 121, 117, 105 ],
		[113, 115, 100, 102, 103, 104, 106, 107 ],
		[119, 120, 99, 118, 98, 110, 44, 59 ]
	];
	~kbpad8x4_flat = ~kbpad8x4.flat;
	~kbcalphanum = {
		var dict = Dictionary.new;
		//TODO: only for Ctrl (262144) modifier, do others
		//NOTE: ^W close the window
		var keycodes = [
				[ 38, 233, 34, 39, 40, 45, 232, 31, 231, 224, 41, 61 ],
				[ 1, 26, 5, 18, 20, 25, 21, 9, 15, 16, 36 ],
				[ 17, 19, 4, 6, 7, 8, 10, 11, 12, 13, 249, 42], 
				[60, 24, 3, 22, 2, 14, 44, 59, 58, 33] //FIXME: complete keycodes
		];
		var alnum = [
			"1234567890)=",
			"azertyuiop^$",
			"qsdfghjklmù*",
			"<xcvbn,;:!"
		];
		keycodes.do { arg row, rowidx;	
			row.do { arg kc, kcidx;
				dict[ alnum[rowidx][kcidx].asString ] = kc;
			};
		};
		dict;
	}.value;
	~kbaalphanum = {
		var dict = Dictionary.new;
		//NOTE: only for Alt and no modifier
		var keycodes = [
				[ 38, 233, 34, 39, 40, 45, 232, 31, 231, 224, 41, 61 ],
				[97, 122, 101, 114, 116, 121, 117, 105, 111,112, 36 /* ^ not working */, 36 ],
				[113, 115, 100, 102, 103, 104, 106, 107, 108, 109, 249, 42 ],
				[60, 119, 120, 99, 118, 98, 110, 44, 59 ] //FIXME: complete keycodes
		];
		var alnum = [
			"1234567890)=",
			"azertyuiop^$",
			"qsdfghjklmù*",
			"<wxcvbn,;:!"
		];
		keycodes.do { arg row, rowidx;	
			row.do { arg kc, kcidx;
				dict[ alnum[rowidx][kcidx].asString ] = kc;
			};
		};
		dict;
	}.value;
	~kb8x2line = [
		38, 97, 233, 122, 34, 101, 39, 114, 40, 166, 45, 121, 232, 117, 95, 105
	];
	~kbnumline = [
		38, 233, 34, 39, 40, 45, 232, 95, 231, 224, 41, 61
	];
	~kbcnumline = [ // for control // FIXME: why the fuck 8 changes with control ???
		38, 233, 34, 39, 40, 45, 232, 31, 231, 224, 41, 61
	];
	~kbnumpad = [
		48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
	];
	~numpad = (
		plus: 43,
		minus: 45
	);
	~mod = (
		fx: 8388608,
		ctrlfx: 8650752,
		ctrl: 262144,
		shift: 131072,
		arrow: 8388608,
		altshift: 655360,
		ctrlaltshift: 917504,
		alt: 524288

	);
	~kbfx = [
		// modifiers = 8388608
		63236, 63237, 63238, 63239, 63240, 63241, 63242, 63243, 63244, 63245, 63246, 63247
		//49,50,51,52,53,54,55,56,57,58
	];
	~kbarrow = (
		// modifiers = 8388608
		left: 63234,
		right: 63235,
		up: 63232,
		down: 63233
	);
	~kbspecial = (
		delete: 127,
		enter: 13,
		escape: 27,
		point: 46
	);
	~cakewalk = (
		\knob: [
			16,17,18,19, 20,21,22,23, 24
		],
		\button: [
			34,35,36,37, 38,39,40,41, 42
		],
		\toggle: [
			43,44,45,46
		],
		\slider: [
			25,26,27,28, 29,30,31,32, 33
		],
		\pad: [ // in order of label number
			36, 38, 42, 43, 46, 47, 50, 49,
		]
	);
	~midi = {
		var dico = Dictionary.new;
		~cakewalk.keysValuesDo { arg k, v;
			v.do { arg raw, i;
				dico[raw] = [k, i];
			};
		};
		dico;
	}.value;
	~midi_cc = {
		var dico = Dictionary.new;
		~cakewalk.reject({ arg x; x == \pad }).keysValuesDo { arg k, v;
			v.do { arg raw, i;
				dico[raw] = [k, i];
			};
		};
		dico;
	}.value;
	~midi_note = {
		var dico = Dictionary.new;
		~cakewalk.pad.do { arg v, i;
			dico[v] = [\pad, i];
		};
		dico;
	}.value;
}).as(Event);

~shortcut = (
	kb_handler: Dictionary.new,					// panel -> shortcut keycode -> action function
	midi_handler: Dictionary.new,
	actions: MultiLevelIdentityDictionary.new,  // path -> action function
	config: MultiLevelIdentityDictionary.new,   // path -> shortcut keycode
	commands: Dictionary.new,					// panel -> shortcut keycode -> path
	modes: Dictionary.new,						// path -> restore function

	add_shortcut: { arg self, path, default_shortcut=nil, action;
		var shortcut;
		self.actions.put(*path++[action]);
		self.config.put(*path ++ [ self.config.at(*path) ?? default_shortcut ]); //FIXME: why never overwrite shortcut ?
	},

	set_action: { arg self, path, action;
		var panel = path[0];
		self.actions.put(*path++[action]);
		if ( self.commands[panel][self.config.at(*path)] == path ) {
			path.debug("already enabled, so enforce");
			self.enable(path);
		};
	},

	remove_panel: { arg self, panel;
		self.kb_handler[panel].do { arg sc;
			self.commands.removeAt(sc);
		};
		self.kb_handler.removeAt(panel);
		self.midi_handler[panel].do { arg sc;
			self.commands.removeAt(sc);
		};
		self.midi_handler.removeAt(panel);

		self.actions.removeAt(panel);
		self.config.removeAt(panel);

		self.modes.copy.keys.do { arg path;
			if(path[0] == panel) {
				self.modes.removeAt(path)
			}
		};
		
	},

	enable: { arg self, path;
		var action, shortcut, panel = path[0];
		//path.debug("enabling path");
		shortcut = self.config.at(*path);
		action = self.actions.at(*path);
		//shortcut.debug("shortcut");
		//action.debug("action");
		if(shortcut.notNil, {
			if(self.commands[panel].isNil) {
				self.commands[panel] = Dictionary.new;
			};
			self.commands[panel][shortcut] = path;
			switch(shortcut[0],
				\kb, {
					self.kb_handler[panel] = self.kb_handler[panel] ?? Dictionary.new;
					self.kb_handler[panel][shortcut] = action;
					//[path, shortcut].debug("path enabled");
				},
				\midi, {
					//[path, shortcut].debug("midi path enabled");
					//self.commands.debug("commands");
					self.midi_handler[panel] = self.midi_handler[panel] ?? Dictionary.new;
					self.midi_handler[panel][shortcut] = action;
				})
		})

	},

	disable: { arg self, path;
		var shortcut, panel = path[0];

		shortcut = self.config.at(*path);

		if(shortcut.notNil, {
			self.commands[panel][shortcut] = nil;
			switch(shortcut[0],
				\kb, {
					self.kb_handler[panel] = self.kb_handler[panel] ?? Dictionary.new;
					self.kb_handler[panel][shortcut] = nil;
					//[path, shortcut].debug("path disabled");
				},
				\midi, {
					self.midi_handler[panel] = self.midi_handler[panel] ?? Dictionary.new;
					self.midi_handler[panel][shortcut] = nil;
				})
		})

	},

	enable_mode: { arg self, path;
		var restorefun;
		if( self.modes[path].notNil ) {
			path.debug("Mode already enabled");
		} {
			restorefun = self.overload_mode(path);
			self.modes[path] = restorefun;
		}
	},

	disable_mode: { arg self, path;
		if( self.modes[path].notNil ) {
			self.modes[path].();
			self.modes[path] = nil;
		} {
			path.debug("Mode already disabled");
		}
	},


	overload_mode: { arg self, path;
		var restorefun=nil;
		var panel = path[0];
		self.commands.debug("overload_mode:commands");
		self.config.leafDoFrom(path, { arg leafpath, val;
			var oldpath;
			oldpath = self.commands[panel][ self.config.at(*leafpath) ];
			[oldpath, leafpath, self.config.at(*leafpath)].debug("oldpath, path, scap");

			if( oldpath.isNil, {
				restorefun = restorefun.addFunc({ self.disable(leafpath) });
			}, {
				restorefun = restorefun.addFunc({ self.enable(oldpath) });
			});

			self.enable(leafpath);
		});
		restorefun;
	},

	add_enable: { arg self, path, default_shortcut=nil, action;
		self.add_shortcut(path, default_shortcut, action);
		self.enable(path);
	},

	matrix_add_enable: { arg self, path, prefix, matrix, action;
		matrix.do { arg i, y;
			i.do { arg j, x; 
				self.add_enable(path++[x,y], prefix ++ [j], { action.(x,y) })
			};
		};
	},

	array_add_enable: { arg self, path, prefix, array, action;
		array.do { arg i, x;
			self.add_enable(path++[x], prefix ++ [i], { action.(x) })
		};
	},

	array_set_action: { arg self, path, size, action;
		size.do { arg i;
			self.set_action(path++[i], { action.(i) })
		};
	},


	
	set_shortcut: { arg self, path, shortcut;
		self.config.put(*path++[shortcut]);
	},

	array_set_shortcut: { arg self, path, prefix, array;
		array.do { arg i, x;
			self.set_shortcut(path++[x], prefix ++ [i])
		};
	},

	get_kb_responder: { arg self, name;
		name.debug("giving panel responder");
		{ arg view, char, modifiers, u, k; 
			[name, modifiers, u].debug("KEYBOARD INPUT");
			self.handle_key(name, [\kb, modifiers,u]);
		};
	},

	bind_param: { arg self, ccpath, param;
		var panel = \midi;
		if(self.midi_handler[panel].isNil) { self.midi_handler[panel] = Dictionary.new };
		param.midi.set_ccpath(ccpath);
		self.midi_handler[panel][ccpath] = { arg val; 
			[param.name, val].debug("bind_param function: set_val");
			param.midi.set_val(val);
		};
	},

	handle_cc: { arg self, ccpath, val;
		var panel = \midi;
		if(self.midi_handler[panel].isNil) { self.midi_handler[panel] = Dictionary.new };
		[ccpath, val].debug("keycode: handle_cc");
		self.midi_handler[panel][ccpath].(val);
	},

	handle_midi_key: { arg self, panel, shortcut;
		var fun;
		//self.kb_handler.debug("handle_key: kb_handler");
		[shortcut, panel].debug("current shortcut panel");
		self.commands[panel][shortcut].debug("shortcut of path called");
		fun = self.midi_handler[panel][shortcut];
		
		//[fun, fun.def, fun.def.sourceCode].debug("function");
		if(fun.isNil, { "handle_key: nil function".warn; nil }, { fun.value; 1 })
	},

	handle_key: { arg self, panel, shortcut;
		var fun;
		//self.kb_handler.debug("handle_key: kb_handler");
		panel.debug("current shortcut panel");
		self.commands[panel][shortcut].debug("shortcut of path called");
		fun = self.kb_handler[panel][shortcut];
		
		//[fun, fun.def, fun.def.sourceCode].debug("function");
		if(fun.isNil, { "handle_key: nil function".warn; nil }, { fun.value; 1 })
	}


);


