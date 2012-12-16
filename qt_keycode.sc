(
~qt_mod = (
	262144: \ctrl,
	524288: \alt,
	131072: \shift,
	// numpad
	2097152: 0,
	2359296: \ctrl,
	2621440: \alt,
	2228224: \shift,

);
)

(
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
	};
	ret.asSymbol;
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

	w = Window.new;
	w.view.keyDownAction = ~qt_keycode_to_keysymbol;
	w.front;
)



Mdef.main.commands.commands[\side]
Mdef.main.commands.kb_handler[\side][[\kb, 0, \f1]]
