
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
		"v:".postln;
		v.postln;
		"k:".postln;
		k.postln;
		v.do { arg raw, i;
			dico[raw] = [k, i];
		};
	};
	dico;
}.value;

~shortcut = (
	kb_handler: Dictionary.new,
	midi_handler: Dictionary.new,
	actions: MultiLevelIdentityDictionary.new,
	config: MultiLevelIdentityDictionary.new,

	add: { arg self, path, default_shortcut=nil, action;
		var shortcut;
		self.actions.put(*path++[action]);
		self.config.put(*path ++ [ self.config.at(*path) ?? default_shortcut ]);
	},

	enable: { arg self, path;
		var action, shortcut, panel = path[0];
		shortcut = self.config.at(*path);
		action = self.actions.at(*path);
		if(shortcut.notNil, {
			switch(shortcut[0],
				\kb, {
					self.kb_handler[panel] = self.kb_handler[panel] ?? Dictionary.new;
					self.kb_handler[panel][shortcut] = action;
				},
				\midi, {
					self.midi_handler[panel] = self.midi_handler[panel] ?? Dictionary.new;
					self.midi_handler[panel][shortcut] = action;
				})
		})

	},

	enable_mode: { arg self, path;
		self.config.leafDoFrom(path, { arg path, val;
			self.enable(path);
		});
	},

	add_enable: { arg self, path, default_shortcut=nil, action;
		self.add(path, default_shortcut, action);
		self.enable(path);
	},
	
	change: { arg self, path, shortcut;
		self.config.put(*path++[shortcut]);
	},

	get_kb_responder: { arg self, name;
		

	},

	handle_key: { arg self, panel, shortcut;
		var fun = self.kb_handler[panel][shortcut];
		if(fun.isNil, { nil }, { fun.value; 1 })
	};

);
