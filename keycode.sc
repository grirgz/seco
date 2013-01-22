
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

~twoWayDictionary = {
	var dic;
	dic = (
		valkey2: Dictionary.new,
		keyval2: Dictionary.new,
		keyval: Dictionary.new,
		valkey: Dictionary.new,

		bind: { arg self, key, val;
			var oldval, oldkey;
			oldval = self.keyval[key];
			oldkey = self.valkey[val];
			if(oldkey.notNil) { self.keyval[oldkey] = nil; };
			if(oldval.notNil) { self.valkey[oldval] = nil; };
			self.keyval[key] = val;
			self.valkey[val] = key;
		},

		unbind_key: { arg self, key;
			var val;
			val = self.keyval[key];
			self.keyval[key] = nil;
			self.valkey[val] = nil;
		},

		unbind_val: { arg self, val;
			var key;
			key = self.valkey[val];
			self.keyval[key] = nil;
			self.valkey[val] = nil;
		},

		get_val_by_key: { arg self, key;
			self.keyval[key]
		},

		get_key_by_val: { arg self, val;
			self.valkey[val]
		}

	);
	dic;

};

~keygroups = Environment.make({
	//~kbpad8x4 = [
	//	"1234567890)=",
	//	"azertyuiop^$",
	//	"qsdfghjklm%*",
	//	"<xcvbn,;:!"
	//];
	~numline = "1234567890)=";
	~numpad = (0..9).collect { arg x; (\np ++ x).asSymbol };
	~fx = (1..12).collect { arg x; (\f ++ x).asSymbol };
});

~keycode = Environment.make({
	~mouse = (
		left_click: 0,
		right_click: 1,
		middle_click: 2,
	);
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
			"qsdfghjklm%*",
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
		//NOTE: only for Alt modifier
		var keycodes = [
				[ 38, 233, 34, 39, 40, 45, 232, 31, 231, 224, 41, 61 ],
				[97, 122, 101, 114, 116, 121, 117, 105, 111,112, 94 /* ^ not working in swing */, 36 ],
				[113, 115, 100, 102, 103, 104, 106, 107, 108, 109, 249, 42 ],
				[60, 119, 120, 99, 118, 98, 110, 44, 59, 58, 33 ]
		];
		var alnum = [
			"1234567890)=",
			"azertyuiop^$",
			"qsdfghjklm%*",
			"<wxcvbn,;:!"
		];
		keycodes.do { arg row, rowidx;	
			row.do { arg kc, kcidx;
				dict[ alnum[rowidx][kcidx].asString ] = kc;
			};
		};
		dict;
	}.value;
	~kbraalphanum = ~kbaalphanum.invert;

	~kbsaalphanum = {
		var dict = Dictionary.new;
		//NOTE: only for Alt and Shift modifier
		var keycodes = [
				[ 38, 233, 34, 39, 40, 45, 232, 31, 231, 224, 41, 61 ],
				[97, 122, 101, 114, 116, 121, 117, 105, 111,112, 94 /* ^ not working in swing*/, 36 ],
				[113, 115, 100, 102, 103, 104, 106, 107, 108, 109, 249, 42 ],
				[60, 119, 120, 99, 118, 98, 110, 44, 59, 58, 33  ]
		]-32;
		var alnum = [
			"1234567890)=",
			"azertyuiop^$",
			"qsdfghjklm%*", // ù replaced by % because multibyte cause offset
			"<wxcvbn,;:!"
		];
		keycodes.do { arg row, rowidx;	
			row.do { arg kc, kcidx;
				dict[ alnum[rowidx][kcidx].asString ] = kc;
			};
		};
		dict;
	}.value;
	~kbrsaalphanum = ~kbsaalphanum.invert;
	~kb8x2line = [
		38, 97, 233, 122, 34, 101, 39, 114, 40, 166, 45, 121, 232, 117, 95, 105
	];
	~kbnumline = [
		38, 233, 34, 39, 40, 45, 232, 95, 231, 224, 41, 61
	];
	~kbrnumline = [
		// 11 = ")", 12 = "="
		1,2,3,4,5,6,7,8,9,0,")","="
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
	~midi_mod = [ \hold ];
	~mod = (
		0: 0,
		fake: 12345,
		fx: 8388608,
		ctrlfx: 8650752,
		ctrl: 262144,
		shift: 131072,
		arrow: 8388608,
		altshift: 655360,
		ctrlalt: 786432,
		ctrlshift: 393216,
		ctrlshiftfx: 8781824,
		ctrlaltshift: 917504,
		ctrlaltshiftfx: 9306112,
		numpad: 2097152,
		alt: 524288

	);
	~rmod = { 
		var res = ~mod.invert;
		res[8650752] = \ctrl; 
		res[8519680] = \shift; 
		res[8781824] = \ctrlshift; 

		res;
	}.value;
	~kbfx = [
		// modifiers = 8388608
		63236, 63237, 63238, 63239, 63240, 63241, 63242, 63243, 63244, 63245, 63246, 63247
		//49,50,51,52,53,54,55,56,57,58
	];
	~kbqtfx = [
		// modifiers = 8388608
		16777264, 16777265, 16777266, 16777267, 16777268, 16777269, 16777270, 16777271, 16777272, 16777273, 16777274, 16777275
		//49,50,51,52,53,54,55,56,57,58
	];
	~kbarrow = (
		// modifiers = 8388608
		left: 63234,
		right: 63235,
		up: 63232,
		down: 63233
	);
	~midispecial = (
		begin: 34,
		left: 35,
		right: 36,
		end: 37,
		stop: 38,
		play: 39,
		pause: 40,
		record: 41,
		hold: 42,

		b1: 43,
		b2: 44,
		b3: 45,
		b4: 46
	);
	~kbqtnumpad = (
		npslash: 47,
		npstar: 42,
		npminus: 45,
		npplus: 43
	);
	~kbspecial = (
		delete: 127,
		enter: 13,
		escape: 27,
		point: 46,
		left: 63234,
		right: 63235,
		up: 63232,
		down: 63233
	);
	~kbspecial.putAll(~kbqtnumpad);
	~kbqtspecial = (
		space: 32,
		enter: 16777220,
		npenter: 16777221,
		backspace: 16777219,
		escape: 16777216,
		point: 46,
		left: 16777234,
		right: 16777236,
		up: 16777235,
		down: 16777237,
		insert: 16777222,
		delete: 16777223,
		home: 16777232,
		end: 16777233,
		pageup: 16777238,
		pagedown: 16777239,
		windows: 16777250,
		scrolllock: 16777254,
		pause: 16777224,
		tab: 16777217,
		square: 178,
		menu: 16777301,
		hothome: 16777360
	);
	~kbrqtspecial = ~kbqtspecial.invert;
	~kbfxdict = {
		var dico = Dictionary.new;
		~kbfx.do { arg kc, i;
			dico[("f"++(i+1)).asSymbol] = kc;
		};
		~kbspecial.putAll(dico);
		dico
	}.value;
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
		\pad: [ // in order of label number // FIXME: order incorect
			36, 38, 42, 43, 46, 47, 50, 49,
		]
	);
	~midipads = [ // in top-down order
			43, 47, 50, 49,
			36, 38, 42, 46,
	] + 1000; // offset to difference from cc
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
	//ccpathdict: ~twoWayDictionary.(),			// key:ccpath <-> val:param
	ccpathToParam: Dictionary.new,
	paramToCcpath: IdentityDictionary.new,
	midi_modifier: 0,

	add_shortcut: { arg self, path, default_shortcut=nil, action;
		var shortcut;
		self.actions.put(*path++[action]);
		self.config.put(*path ++ [ self.config.at(*path) ?? default_shortcut ]); //FIXME: why never overwrite shortcut ? because shortcut already set in conf before
	},

	set_action: { arg self, path, action;
		var panel = path[0];
		self.actions.put(*path++[action]);
		if(self.commands[panel].isNil) {
			self.commands[panel] = Dictionary.new;
		};
		if ( self.commands[panel][self.config.at(*path)] == path ) {
			path.debug("already enabled, so enforce");
			self.enable(path);
		};
	},

	copy_action: { arg self, frompath, topath, enable=true;
		[frompath, topath].debug("copy_action");

		if(self.actions.at(*frompath).class == IdentityDictionary) {
			self.actions.leafDoFrom(frompath, { arg leafpath, val;
				self.copy_action(leafpath, topath ++ ~find_path_difference.(frompath, leafpath));
			});
		} {
			self.actions.at(*frompath).debug("action");
			self.set_action(topath, self.actions.at(*frompath));
			if(enable) {
				self.enable(topath)
			};
		};
	},

	copy_action_list: { arg self, panel, prefix, paths;
		~copy_action_bindings.(self, panel, prefix, paths);
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

		if(shortcut.class == IdentityDictionary) {
			self.config.leafDoFrom(path, { arg leafPath, val; self.enable(leafPath); });
		} {
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
		}

	},

	disable: { arg self, path;
		var shortcut, panel = path[0];

		shortcut = self.config.at(*path);
		if(shortcut.class == IdentityDictionary) {
			self.config.leafDoFrom(path, { arg leafPath, val; self.disable(leafPath); });
		} {
			shortcut.debug("disabling shortcut");

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
		};

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

//~rah = MultiLevelIdentityDictionary.new;
//~rah[\bla,\niak] = 4
//~rah[\bla,\rah,\goui] = 4
//~rah.leafDoFrom([\bla], { arg x, y; [x,y].postcs });
//~rah[\bla,\niak].nodeType
//~rah[\bla,\rah,\goui].class

	overload_mode: { arg self, path;
		var restorefun=nil;
		var panel = path[0];
		//self.commands.debug("overload_mode:commands");
		self.config.leafDoFrom(path, { arg leafpath, val;
			var oldpath;
			oldpath = self.commands[panel][ self.config.at(*leafpath) ];
			//[oldpath, leafpath, self.config.at(*leafpath)].debug("oldpath, path, scap");

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

	array_set_action_enable: { arg self, path, size, action;
		size.do { arg i;
			self.set_action(path++[i], { action.(i) });
			self.enable(path++[i]);
		};
	},
	
	set_shortcut: { arg self, path, shortcut;
		//self.config.put(*path++[shortcut]);
		[path, shortcut.asCompileString].debug("set_shortcut");
		self.config.put(*path++[shortcut.deepCopy]);
	},

	array_set_shortcut: { arg self, path, prefix, array;
		array.do { arg i, x;
			self.set_shortcut(path++[x], prefix ++ [i])
		};
	},

	parse_action_bindings: { arg self, panel, actions;
		~parse_action_bindings.(self, panel, actions);
	},

	get_kb_responder: { arg self, name;
		name.debug("giving panel responder");

		if(GUI.current == QtGUI) {
			self.qt_get_kb_responder(name);
		} {
			{ arg view, char, modifiers, u, k; 
				[name, modifiers, u, k].debug("KEYBOARD INPUT (name, mod, unicode, keycode(not used))");
				self.handle_key(name, [\kb, modifiers,u]);
			};
		}
	},

	qt_get_kb_responder: { arg self, name;
		name.debug("giving panel responder (qt)");
		{ arg view, char, modifiers, u, k; 
			var res;
			[name, modifiers, u, k].debug("KEYBOARD INPUT");

			res = ~qt_keycode_to_keysymbol.(view, char, modifiers, u, k);
			if( res.notNil ) {
				self.handle_key(name, [\kb] ++ res);
			} {
				"~qt_keycode_to_keysymbol yielded no result".debug;
			}
		};
	},

	bind_param: { arg self, ccpath, param;
		var panel = \midi;
		var oldparam;
		"I---I bind_param".debug;
		if(self.midi_handler[panel].isNil) { self.midi_handler[panel] = Dictionary.new };
		oldparam = self.ccpathToParam[ccpath];

		//param.midi.get_param.debug("verif param");
		[param.name, ccpath].debug("assigning ccpath to param");

		self.ccpathToParam[ccpath] = param;
		self.paramToCcpath[param] = ccpath;
		if(oldparam.notNil && (oldparam != param)) {
			self.paramToCcpath[oldparam] = nil;
			self.get_param_binded_ccpath(oldparam).debug("ce n'est point possible");
			oldparam.name.debug("refreshing oldparam");
			oldparam.midi.refresh;
		};
		//param.midi.get_ccpath.debug("verif");
		//param.midi.get_param.debug("verif param");
		param.name.debug("refreshing param");
		param.midi.refresh;
		self.midi_handler[panel][ccpath] = { arg val; 
			[param.name, val].debug("bind_param function: set_val");
			param.midi.set_val(val);
		};
		"I---I end bind_param".debug;
	},

	get_param_binded_ccpath: { arg self, param;
		//[param.name, self.ccpathdict.get_key_by_val(param)].debug("shortcut: get_param_binded_ccpath");
		//self.ccpathdict.get_key_by_val(param)
		self.paramToCcpath[param]
	},

	ccpath_to_param: { arg self, ccpath;
		self.ccpathToParam[ccpath];
	},

	param_to_ccpath: { arg self, param;
		self.paramToCcpath[param];
	},

	set_midi_modifier: { arg self, mod, val;
		[mod, val].debug("keycode.set_midi_modifier: mod, val");
		if(val == 1) {
			self.midi_modifier = mod;
		} {
			self.midi_modifier = 0;
		}
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
		shortcut[1] = self.midi_modifier;
		if(self.midi_handler[panel].isNil) { self.midi_handler[panel] = Dictionary.new };
		[shortcut, panel].debug("current shortcut panel");
		self.commands[panel][shortcut].debug("shortcut of path called");
		fun = self.midi_handler[panel][shortcut];
		
		//[fun, fun.def, fun.def.sourceCode].debug("function");
		if(fun.isNil, { "handle_key: nil function".warn; nil }, { fun.value; 1 })
	},

	handle_key: { arg self, panel, shortcut;
		var fun;
		//self.kb_handler.debug("handle_key: kb_handler");
		[panel, shortcut.asCompileString].debug("current shortcut panel");
		shortcut = shortcut.deepCopy;
		if(self.commands[panel].notNil) {
			self.commands[panel][shortcut].debug("shortcut of path called");
			//self.commands[panel].debug("commands[panel]");
			fun = self.kb_handler[panel][shortcut];
			
			//[fun, fun.def, fun.def.sourceCode].debug("function");
			if(fun.isNil, { "handle_key: nil function".warn; nil }, { fun.value; 1 })
		} {
			panel.debug("ERROR: bindings of panel are not defined");
		};
	}


);


~string_to_symbol_list = { arg str;
	if(str.class == Symbol) {
		[str]
	} {
		str.split($.).collect(_.asSymbol);
	}
};

~get_modifer = { arg binding;
	var realmod;
	var key = binding[3];
	var mod = binding[2];
	var kind = binding[1];
	if(~keycode.midi_mod.includes(mod)) {
		mod
	} {
		case
			{ kind == \midi } {
				realmod = 0;	
			}
			{ ~keycode.kbarrow.keys.includes(key) } {
				realmod = (
					0: \arrow
					//shift: \fxshift,
				)[mod];
				if(realmod.isNil) {
					[mod,key].debug("ERROR: modifier arrow not found");
					realmod = \fake;
				};
			}
			{ ~keycode.kbfxdict.keys.includes(key) } {
				realmod = (
					0: \fx,
					//shift: \fxshift,
					ctrl: \ctrlfx,
					ctrlshift: \ctrlshiftfx,
					ctrlaltshift: \ctrlaltshiftfx,
				)[mod];
				if(realmod.isNil) {
					[mod,key].debug("ERROR: modifier fx not found");
					realmod = \fake;
				}
			} {
				realmod = mod
			};
		[mod,key,realmod].debug("get_modifer");
		~keycode.mod[realmod];
	}
};

~get_keycode = { arg binding;
	var realkey;
	var kind = binding[1];
	var key = binding[3];
	var mod = binding[2];
	if( key.isString && (key.size < 3) ) {
		case
			{ [\ctrl, \ctrlaltshift, \ctrlshift, \ctrlalt].includes(mod) } {
				realkey = ~keycode.kbcalphanum[key];
			}
			{ [\shift, \altshift].includes(mod) } {
				realkey = ~keycode.kbsaalphanum[key];
			}
			{
				realkey = ~keycode.kbaalphanum[key];
			}
		
	} {
		if(kind == \midi) {
			realkey = ~keycode.midispecial[key];
		} {
			realkey = ~keycode.kbspecial[key];
		};
		if(realkey.isNil) {
			realkey = ~keycode[key];
		};
	};
	realkey;
};


~parse_bindings = { arg commands, bindings;
	"**begin parsing bindings".debug;
	bindings.keysValuesDo { arg panel, blist;
		blist.do { arg binding;
			var kc = ~get_keycode.(binding);
			if(kc.isArray) {
				[[panel] ++ ~string_to_symbol_list.(binding[0]), [ binding[1], ~get_modifer.(binding) ], ~get_keycode.(binding)].postcs;
				commands.array_set_shortcut([panel] ++ ~string_to_symbol_list.(binding[0]), [ binding[1], ~get_modifer.(binding) ], ~get_keycode.(binding))
			} {
				[[panel] ++ ~string_to_symbol_list.(binding[0]), [ binding[1], ~get_modifer.(binding), ~get_keycode.(binding) ]].postcs;
				commands.set_shortcut([panel] ++ ~string_to_symbol_list.(binding[0]), [ binding[1], ~get_modifer.(binding), ~get_keycode.(binding) ])
			}
		}

	};
	"**end parsing bindings".debug;
};

~qt_parse_bindings = { arg commands, bindings;
	"**begin parsing bindings".debug;
	bindings.keysValuesDo { arg panel, blist;
		blist.do { arg binding;
			var kc = ~get_keycode.(binding);
			if(~qt_symbol_to_keygroup.(binding[3]).notNil) {
				[binding[3], ~qt_symbol_to_keygroup.(binding[3])].debug("qt_parse_bindings: array");
				commands.array_set_shortcut(
					[panel] ++ ~string_to_symbol_list.(binding[0]), 
					[ binding[1], binding[2] ],
					~qt_symbol_to_keygroup.(binding[3])
				)
			} {
				commands.set_shortcut(
					[panel] ++ ~string_to_symbol_list.(binding[0]),
					[ binding[1], binding[2], binding[3] ]
				)
			}
		}

	};
	"**end parsing bindings".debug;

};

if(GUI.current == QtGUI) {
	~parse_bindings = ~qt_parse_bindings;
};

~parse_action_bindings = { arg commands, panel, actions;
	var path;
	var process_line;

	process_line = { arg action;
		path = [panel] ++ ~string_to_symbol_list.(action[0]);
		action.postcs;
		if(action.last == \disabled) {
			if(action[1].isInteger) {
				commands.array_set_action(path, action[1], action[2]);
			} {
				commands.set_action(path, action[1]);
			}
		} {
			if(action[1].isInteger) {
				commands.array_set_action_enable(path, action[1], action[2]);
			} {
				commands.add_enable(path, nil, action[1]);
			}
		}
	};
	panel.debug("CCCCC parse_action_bindings");
	actions.do { arg action;
		if(action[0].class == Array) {
			action[0].do { arg path;
				process_line.([path] ++ action[1..]);
			}
		} {
			process_line.(action)
		}
	}
};

~copy_action_bindings = { arg commands, panel, prefix, paths;
	paths.do { arg path;
		path = ~string_to_symbol_list.(path);
		commands.copy_action([panel]++path, [panel, prefix] ++ path)
	};
};

////////////////////////////////////// Qt keycodes ///////////////////////////////////////

~string_to_string_list = { arg str;
	str.asList.collect(_.asString);
};

~keygroups = (
	kbnumline: ~string_to_string_list.("1234567890)="),
	kbnumpad: "0123456789".asList.collect{ arg ch; "np" ++ ch.asString },
	kbpad8x4_flat: ~string_to_string_list.("12345678azertyuiqsdfghjkwxcvbn,;"),
	midipads: ~string_to_symbol_list.("56781234"),
);

~qt_symbol_to_keygroup = { arg symbol;
	~keygroups[symbol];
};


~modifier_to_symbol = { arg mod;
	var ret = "";
	if(mod.isCtrl) {
		ret = "ctrl";
	};
	if(mod.isAlt) {
		ret = ret ++ "alt";
	};
	if(mod.isShift) {
		ret = ret ++ "shift";
	};
	if(ret == "") {
		ret = 0;
	} {
		ret.asSymbol;
	};
};

~qt_keycodes = {
	var dict = Dictionary.new;
	var kc = (
		fx: [67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 95, 96],
		numline: [ 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21 ],
		numpad: [90, 87, 88, 89, 83, 84, 85, 79, 80, 81],
		punctuation: [34, 35, 48, 51, 94, 58, 59, 60, 61],
		alpha: [
			24, 25, 26, 27, 28, 29, 30, 31, 32, 33,
			38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
			52, 53, 54, 55, 56, 57
		]
	);
	var special = (
		left: [9, 49, 22, 23, 36, 66, 50, 62, 37, 133, 64, 65, 92, 135, 105],
		center: [78, 127, 118, 110, 112, 119, 115, 117, 111, 113, 116, 114],
		right: [77, 106, 63, 82, 86, 91, 104],
	);

	12.do { arg x;
		dict[kc.fx[x]] = "f%".format(x+1).asSymbol;
	};
	"1234567890)=".do { arg ch, x;
		dict[kc.numline[x]] = ch.asString;
	};
	"01234567890".do { arg ch, x;
		dict[kc.numpad[x]] = "np"++ch.asString;
	};
	"^$%*<,;:!".do { arg ch, x;
		dict[kc.punctuation[x]] = ch.asString;
	};

	"azertyuiopqsdfghjklmwxcvbn".do { arg ch, x;
		dict[kc.alpha[x]] = ch.asString;
	};

	[
		\scrolllock, \pause,
		\insert, \home, \pageup,
		\delete, \end, \pagedown,
		\up, \left, \down, \right
	].do { arg sy, x;
		dict[special.center[x]] = sy;
	};

	[
		\escape,
		\square, \backscape,
		\tab, \enter,
		\capslock, \leftshift, \rightshift,
		\leftctrl, \meta, \alt, \space, \altgr, \menu, \rightctrl
	].do { arg sy, x;
		dict[special.left[x]] = sy;
	};

	[
		\numlock, \div, \mul, \minus,
		\plus, \nppoint, \npenter
	].do { arg sy, x;
		dict[special.right[x]] = sy;
	};

	dict
}.value;

~qt_altgr_unicode = {
	var ret = Dictionary.new;
	var uc = [
		711, 126, 35, 123, 91, 124, 96, 92, 94, 64, 93, 125, 
		230, 226, 8364, 234, 254, 255, 251, 238, 339, 244, 126, 248, 
		228, 223, 235, 8216, 8217, 240, 252, 239, 320, 246, 180, 96,
		8804, 171, 187, 169, 8239, 8595, 172, 191, 215, 247, 161,
	];

	ret[185] = \square;
	"1234567890)=azertyuiop^$qsdfghjklm%*<wxcvbn,;:!".do { arg ch, x;
		ret[uc[x]] = ch.asString;
	};
	ret;

}.value;

~qt_keycode_to_keysymbol = { arg view, char, modifiers, unicode, keycode;
	var fxtest, fxsymbol, modsymbol, keysymbol;
	var onlymodifer;
	[char, modifiers, unicode, keycode].postcs;
	onlymodifer = [
		\leftshift, \rightshift,
		\leftctrl, \meta, \alt, \altgr, \rightctrl
	];


	if(GUI.scheme == QtGUI) {
		
		modsymbol = ~modifier_to_symbol.(modifiers);
		keysymbol = ~qt_keycodes[keycode];
		if(~qt_altgr_unicode[unicode].notNil) {
			keysymbol = ~qt_altgr_unicode[unicode];
			modsymbol = \altgr;
		};
		if(onlymodifer.includes(keysymbol)) {
			modsymbol = \mod;
		}

	};
	[modsymbol, keysymbol].postcs;
	//"%, ".format(unicode).postln;

};
