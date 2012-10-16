
(

var data;

w = Window.new("ParaSpace", Rect(10, 500, 400, 300)).front;

a = ParaSpace.new(w, bounds: Rect(20, 20, 360, 260));


9.do({arg d, i; a.createNode1(0.5, (i/10)+0.1).setNodeString_(i, (i+1).asString);
a.setNodeSize_(i, 20);
a.paraNodes[i].setLen = 100;
a.paraNodes[i].temp = (i/10)+0.1;

});


8.do({arg i; a.createConnection(i, i+1)});


a.setBackgrDrawFunc_({

10.do{|i| 
//Color.blue(alpha:0.2).set; 

Pen.line((i*36)@0, (i*36)@280); Pen.stroke};

13.do{|i| 
//Color.blue(alpha:0.2).set; 

Pen.line(0@(i*20), 380@(i*20)); Pen.stroke};

});


a.nodeTrackAction_({arg node;

a.setNodeLoc1_(node.spritenum, a.getNodeLoc1(node.spritenum)[0].round(0.1), a.getNodeLoc1(node.spritenum)[1].round(0.1))

});

)

(
p = Pspawn(Pbind(
        // Pbind returned by Pfunc is not embedded, just placed in the event
        // So, it can be spawned
    \pattern, Pseq([
		Prout { Pbind(\degree, Pseries(rrand(0, 10), #[-1, 1].choose, rrand(4, 10)), \dur, 0.125).yield },
		\rest,
		Prout { Pbind(\degree, Pseries(rrand(0, 10), #[-1, 1].choose, rrand(4, 10)), \dur, 0.25).yield },
		],inf),
    \delta, 1,
    \method, \par
)).play;
)

{SinOsc.ar}.play



Mdef.main

(
Mdef.force_init;
Mdef(\blabla, Pbind(
	\instrument, \lead2,
	\degree, Pseq([0,2,4,2],1),
	\dur, 0.25
));
Mdef(\blabla2, Pbind(
	\instrument, \default,
	\degree, Pseq([0,1,2,1],1),
	\dur, 0.25
));
~tl = ~class_timeline.new(Mdef.main);
5.do { arg i; ~tl.add_track("track"++i);};
~tl.make_gui;
)
~tl.add_block(\blabla, 0, 1);
~tl.add_block(\blabla2, 1, 4);
~tl.add_track("trackbla");
~tl.tracks
~tl.timerule_view.view.refresh
~tl.timeline_view.timerule_view.view.refresh
~tl.changed(\tracks)
~tl.timeline_view.timeline.refresh

Mdef.node("miaou1_l1007").get_duration
Mdef.node(\blabla).get_duration

Mdef.main.panels.side.song_manager.get_section_matrix(0,0)
Mdef.main.node_manager.has_node

(

Mdef.force_init;
Mdef.main.panels.side.song_manager.get_section_matrix(0,0)
)
Mdef.main.panels.side.song_manager.


a = [[0,1,2,3],[4,5,6,7]]
a.invert(1)
a.flop

~tl.play_timeline;

~tl.timeline_score.get_abs_notes
~tl.timeline_score.get_rel_notes
~tl.timeline_score.notes
~tl.timeline_score.compute_end(false)


~tl.timeline_view.tracks(~tl)
~t1.vpattern.pattern.postcs
~t1.vpattern.play
~t2.vpattern.play
~t1.get_rel_notes


a = (a:2)
a.bla = 45
a.identityHash




(
var width = 400, height = 400, mx = 0, my = 0, pt, r;

w = Window("animation and mouse interaction", Rect(100, 200, width, height), false);

u = UserView(w, Rect(0, 0, width, height));
u.background = Color.black;
u.animate = true; //animate this view

// allocate data in advance, for optimization:
pt = Point();
r = Rect();

u.drawFunc = {
        Pen.fillColor = Color.green;
        Pen.stringAtPoint(u.frameRate.asString, Point(10, 10)); // display frame rate
        Pen.stringAtPoint(u.frame.asString, Point(10, 30)); // display frame counter
        Pen.color = Color.white;
        pt.x=mx;
        pt.y=my;
        100.do{|i|
                Pen.moveTo(pt);
                pt.x = sin(u.frame*0.04.neg+i)*(5*i)+mx; //use .frame to drive animation
                pt.y = cos(u.frame*0.05+i)*(5*i)+my;
                r.left=pt.x;
                r.top=pt.y;
                r.width=i;
                r.height=i;
                Pen.lineTo(pt);
                Pen.fillStroke;
                Pen.addOval(r);
                Pen.fillStroke;
        };
};
u.mouseDownAction = {|v, x, y|
        mx = x;
        my = y;
};
u.mouseMoveAction = u.mouseDownAction;
w.front;
)

u.animate = false; //animation can be paused and resumed
u.animate = true;
w.close; //stops animation

(
var width = 800, height = 20, mx = 0, my = 0, pt, r;
var bounds = Rect(100, 200, width+10, height+10);
w = Window("animation and mouse interaction", Rect(100, 200, width, height), false);
~uv = UserView.new(w, Rect(0, 0, width, height));
~uv.drawFunc = {

		var beat_size_x = 10;
        Pen.fillColor = Color.green;
		(width/beat_size_x).asInteger.do{|i| 
			Pen.color = Color.black;
			case
				{ i%32==0 } { 
					Pen.color = Color.blue;
					Pen.line((i*beat_size_x)@0, (i*beat_size_x)@height); Pen.stroke
				}
				{ i%8==0 } { 
				
					Pen.line((i*beat_size_x)@0, (i*beat_size_x)@height); Pen.stroke
				}
				{ i%4==0 } { 
				
					Pen.line((i*beat_size_x)@(height/2), (i*beat_size_x)@height); Pen.stroke
				}
				//
				{ 
					Pen.line((i*beat_size_x)@(3*height/4), (i*beat_size_x)@height); Pen.stroke
				}

			
		};
};
w.front;
)


