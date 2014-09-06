(
~makePattern =  {|name, csize, x|
	var a, z;
	var aname = ("seq_" ++ name ).asSymbol;
	z = Window("sequencer "++name, Rect(0, 0, 400, 200)).front;
	if(Archive.global.at(aname).isNil) {
		a = 0 ! csize ! x.size;
	} {
		#a, csize, x = Archive.global.at(aname)
	};
	a.do{ |rows, i|
          rows.do{ |cell, j|
			Button(z, Rect(j*25, i*25, 25, 25))
                    .states_([
                            ["-", Color.black, Color.white],
                            ["O", Color.white, Color.black],
                    ])
                    .action_{ |v|
                            a[i][j] = v.value;
							Archive.global.put(aname, [a, csize, x]);
					}
					.value_(a[i][j]);
		};
	};
	Ppar(a.collect({|c, r| Pbind(
		\instrument, x[r],
		\freq, Pif(Pseq(a[r]).coin, 100+(a.size-100*r), \rest),
		\dur, 0.25
	)}));
};

~clearPattern = { arg name;
	var aname = ("seq_" ++ name ).asSymbol;
	Archive.global.put(aname, nil);
};

~makeChainPattern =  {|x|
	var a, z;
	z = Window("sequencer", Rect(0, 0, 400, 200)).front;
	a = 0 ! 16 ! x.size;
	a.do{ |rows, i|
          rows.do{ |cell, j|
			Button(z, Rect(j*25, i*25, 25, 25))
                    .states_([
                            ["-", Color.black, Color.white],
                            ["O", Color.white, Color.black],
                    ])
                    .action_{ |v|
                            a[i][j] = v.value;
        };
		};
	};
	Ppar(a.collect({|c, r|
		x[r] <>
		Pbind(
			\freq, Pif(Pseq(a[r]).coin, 100+(a.size-100*r), \rest),
			\dur, 0.25
		)
	}));
};

~makeSuperPattern = { arg plist, pname=\plop;
	var b, butlist, layout;
	Pdef(pname, Pseq([Event.silent])).play;
	b = Window("Super sequencer", Rect(0, 0, 400, 200)).front;
	butlist = List.new;
	layout = HLayout.new;
	plist.do{ |cell, j|
		var but = Button.new;
		layout.add(but);
		b.view.layout = layout;
		butlist.add(but);
		but.states_([
			[".", Color.black, Color.white],
			["X", Color.white, Color.black],
		])
		.action_{ |v|
			var list;
			list = List.new;
			butlist.do({ arg but, idx;
				if(but.value == 1){
					list.add(
						Ppar([
							plist[idx],
							Prout({
								~update_buttons.(butlist, idx)
							});
						]) ;
					);
				};
				if(list.size == 0){
					list = [Event.silent]
				};

				Pdef(pname, Pseq(list,inf));
			})
			;};
	};
};

~update_buttons = {|list,idx|
	list.do({ arg but, butidx;
		var states;
		var val;
		states = if(butidx == idx) {
			[
				[".", Color.black, Color.red],
				["X", Color.white, Color.red],
			]
		} {
			[
				[".", Color.black, Color.white],
				["X", Color.white, Color.black],
			]
		};
		{
			val = but.value;
			but.states = states;
			but.value = val;
		}.defer;
	})
};

)

(
 ~synthdef_list =  [\kick, \snare, \hats, \peep];
 ~synthdef_list2 = [\kick, \snare, \hats, \peep];


//~pat1 = ~makeChainPattern.([
//	Pbind(\cutoff, Pseq([1,2,3,4]*200,inf))
//]);
~pat1 = ~makePattern.(\a, 16, ~synthdef_list);
~pat2 = ~makePattern.(\b, 16, ~synthdef_list2);
~pat3 = ~makePattern.(\c, 16, ~synthdef_list2);
~pat4 = ~makePattern.(\d, 16, ~synthdef_list2);
~plist = [~pat1, ~pat2, ~pat3, ~pat4];


Pdef(\plop).clear;

~makeSuperPattern.(~plist, \plop);

)

~clearPattern.(\a)

s.meter;

s.plotTree;


Archive.global.put(\hello, 10)
Archive.global.at(\hello)
Archive.global
Archive.write
Archive.read
