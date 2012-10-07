
(
~object = (
	new: { arg self;
		var obj = self.deepCopy;
		obj.init;
		obj
	}
);

~new = { arg obj;
		obj = obj.deepCopy;
		obj.init;
		obj
};

~class_plop = (
	parent: ~object,
	voiture: [4],
	init: { arg self;
		"plop: je suis init".postln;
		self.voiture[0] = 10;
	},
	plop_fun: { arg self; "plopfun".postln; }
);

~class_plop2 = (
	init: { arg self;
		"plop2: je suis init".postln;
	}
);
~class_plop3 = (
	voiture: [4],
	new: { arg self, bla;
		self = self.deepCopy;
		self.bla = bla;
		"plop3: je suis init".postln;
		self;
	}
);

~class_blah = (
	parent: ~class_plop,
	init: { arg self;
		//self.parent.init;
		"blah: je suis init".postln;
		self.voiture.debug("voiture");
	},
	blah_fun: { arg self; "blahfun".postln; }
);
~init_parent = { arg self, bla; self.parent[\new].(self, *bla); };
~class_blah3 = (
	parent: ~class_plop3,
	new: { arg self, bla;
		//self = self.parent[\new].(self, bla);
		self = ~init_parent.(self, [bla]);
		"blah3: je suis init".postln;
		self.voiture.debug("voiture");
		self.bla.debug("bla");
		self;
	},
	blah_fun: { arg self; "blahfun".postln; }
);
		
)

~plop = ~class_plop.new;
~plop = ~new.(~class_plop2);
~blah = ~class_blah.new;
~blah.postln;
~blah.proto
Event.parentEvents.pairsDo { arg x,y; (x->y).postln };
~blah.pairsDo { arg x,y; (x->y).postln }; "(parent -> ".post; ~blah.parent.pairsDo { arg x,y; (x->y).postln }; ")".postln;
~blah.postln; ~blah.parent.postln; ~blah.parent.parent.postln; ~blah.parent.parent.parent.postln;
~blah.parent
~blah[\init].postcs


PClass(\Bla, (
	parent: Pclass(\Rah),
	init: { arg self;
	
	}
))

PClass.new(\Bla)

\Event.asClass


~bla = 4
~bla
{ ^~bla }.value


a = { arg *rah; rah.postln; }

~blah = ~class_blah.new;
~blah2 = ~class_blah.new;

~blah = ~class_blah3.new(\rah);
~blah2 = ~class_blah3.new;

~blah[\blah_fun].dump
~blah2[\blah_fun].dump

~blah.voiture[0] = 5
~blah2.voiture

a = "bla";
"bla".dump
a.dump
b = a.deepCopy
b.dump


[[4,5,6],[7,8,9]].do.do { arg x; x.debug("x"); };


(
var bla;
bla = (
	haha: 74,
	hihi: { bla.haha + 100 }.value,
)
)



a = (bla:{ arg self; self.postln; })
a[\bla].(5)
a.bla

a.player = { arg self; "bla".postln; };
a.mode
a.redraw
a.paramlist
a.extparamlist
a.redraw_node
a.group.group
a.nodegroup
