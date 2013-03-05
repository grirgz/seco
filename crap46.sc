(
var downPos, childDrag, childStartPos = 0@0;
v = View(nil, Rect(0, 0, 200, 200));
v.mouseDownAction_({ |view, x, y|
downPos = x@y;
if (childDrag.notNil) {
childStartPos = childDrag.bounds.origin;
};
});
v.mouseUpAction_({ |view, x, y|
childDrag = nil;
});
v.mouseMoveAction_({ |view, x, y|
if (childDrag.notNil) {
var pt = x@y;
var diff = pt - downPos;
var newPos = diff + childStartPos;
childDrag.moveTo(newPos.x, newPos.y)
};
});
10.do{|i|
var a;
var rect = Rect((v.bounds.width-40).rand, (v.bounds.height-40).rand, rrand(20,40), rrand(20,40));
if (0.5.coin) {
a = View(v, rect);
} {
a = StaticText(v, rect.width_(50).height_(20)).string_("Object" ++ i);
};
a.background_(Color.rand);
a.mouseDownAction_({ |view| childDrag = view; false });
};
v.front;
)

