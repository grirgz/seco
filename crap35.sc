(
var stack;
w = Window();
~layout = VLayoutView.new(w, Rect(0,0,200,200));
~but = Button(~layout).states_([["One"],["Two"],["Three"]]).action_({ |b| stack.index = b.value });
stack = StackLayout.new(~layout,
        TextView().string_("This is a chunk of text..."),
        TextView().string_("...and this is another..."),
        TextView().string_("...and another.")
    );
w.front;
)
