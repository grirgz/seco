
~make_seq_widget = { arg parent, seq, paramlist;
	var midi;
	var width = 1200;
	var row_layout;
	var sc_seq;
	var seq_messages;

	row_layout = GUI.vLayoutView.new(parent, Rect(0,0,(width+10),800));
	row_layout.background = ~editplayer_color_scheme.background;

	~midi_interface.clear_assigned(\slider);
	~midi_interface.clear_assigned(\knob);

	CCResponder.removeAll; // FIXME: must not remove other useful CCResponders

	seq_messages = Dictionary.newFrom((
		paramlist: { arg self;
			var player;
			var master;
			var midi;

			row_layout.removeAll;

			"hah33333333ahahahjkkj".debug;
			self.get_paramlist.do { arg paramasso, i;
				var hihi1 = \bla;
				var player_name = paramasso.key.name;
				var param = paramasso.value;
				paramasso.key.name.debug("=======paramasso");
				paramasso.value.name.debug("=======paramasso val");


				player = paramasso.key;

				if(param.name == \noteline, {
					midi = ~piano_recorder.(player);
					param.midi = midi;
					~make_noteline_view.(row_layout, seq.make_param_display(param, i), param);
				},{
					param.midi = nil;
					"hahahahahjkkj".debug;
					~make_stepline_view.(row_layout, seq.make_param_display(param, i), param, midi);
				});

			};
		}
	));

	sc_seq = SimpleController(seq);
	~init_controller.(sc_seq, seq_messages);
	seq.refresh.();

	// remove func

	row_layout.onClose = {
		sc_seq.remove;
	};

};

~make_matrix_edit_view = { arg main, controller;

	var win, vlayout, hlayout;
	var winsize= 700@800;
	var paramsize= 170@26;

	win = Window.new(bounds:Rect(750,0,winsize.x,winsize.y));
	win.view.keyDownAction = main.commands.get_kb_responder(\side);

	controller.window = win;
	win.front;
	win;

};

// view with the basic 8 miniparams representing the groupnode
~make_group_node_view = { arg main, controller, parent, display;

	var mini_param_group_widget;
	var extparam_group_widget;
	var group_title;
	var vlayout;

	vlayout = VLayoutView.new(parent, Rect(3,0,display.winsize.x-13,display.winsize.y));
	group_title = ~make_player_title_bar_view.(vlayout, display.winsize.x@20);

	mini_param_group_widget = ~make_mini_param_group_widget.(vlayout, display.groupnode_row_count, display);
	//sep = VLayoutView(vlayout, Rect(0,0,winsize.x-158,paramsize.y*2+35));
	//2.do {
	//	hlayout = HLayoutView.new(sep, Rect(0,0,winsize.x-150,paramsize.y+15));
	//	4.do {
	//		groupview_list.add( ~make_mini_param_view.(hlayout, paramsize) );
	//	};
	//};


};


// view listing all nodes of a group (more than 8)
~make_nodegroup_view = { arg main, controller;

	var win, vlayout, hlayout;
	var winsize= 700@800;
	var paramsize= 170@26;
	var sep, sep_bar;
	var paramview_list = List.new;
	var extparamview_list = List.new;
	var groupview_list = List.new;
	var extparam_layout, inner_extparam_layout;
	var player_responder;
	var group_responder;
	var player_title;
	var group_title;

	win = Window.new(bounds:Rect(750,0,winsize.x,winsize.y));
	win.view.keyDownAction = main.commands.get_kb_responder(\side);

	controller.window = win;

	vlayout = VLayoutView.new(win, Rect(3,0,winsize.x-13,winsize.y));
	///// status view

	~make_status_view_horizontal.(vlayout, main.play_manager, winsize.x@20);

	player_title = ~make_player_title_bar_view.(vlayout, winsize.x@20);

	group_title = ~make_player_title_bar_view.(vlayout, winsize.x@20);

	4.do { 
		sep = VLayoutView(vlayout, Rect(0,0,winsize.x-158,paramsize.y*2+35));
		2.do {
			hlayout = HLayoutView.new(sep, Rect(0,0,winsize.x-150,paramsize.y+15));
			4.do {
				groupview_list.add( ~make_mini_param_view.(hlayout, paramsize) );
			};
		};
		sep.background = Color.newHex("B4C1CA");
		sep = StaticText.new(vlayout,Rect(0,0,10,3));
	};

	~make_view_responder.(vlayout, controller, (
		player: { arg self;
			var player;
			player = self.get_current_player;
			if(player.notNil) {

				if(player.name != player.uname) {
					player_title.set_name("% (%)".format(player.name, player.uname));
				} {
					player_title.set_name(player.name);
				};
				~notNildo.(player.defname) { arg name;
					player_title.set_instrument("instr:"+name)
				};

				player.name.debug("side_view responder: player");
				player_responder.remove;
				player_responder = ~make_view_responder.(vlayout, player, (
					mode: { arg self;
						//controller.changed(\paramlist);
						//controller.changed(\extparamlist);
					},

					redraw_node: { arg self;
						player.get_arg(\amp).changed(\playingstate, player.name, player.get_playing_state);
					}
						
				));
				//controller.changed(\paramlist);
				//controller.changed(\extparamlist);
				player.get_arg(\amp).changed(\selected);
			}
		},

		group: { arg self, msg;
			var group;
			"sideview_responder: group".debug;
			group = self.get_current_group;

			if(group.name != group.uname) {
				group_title.set_name("% (%)".format(group.name, group.uname));
			} {
				group_title.set_name(group.name);
			};
			~notNildo.(group.kind) { arg name;
				if(group.subkind.notNil) {
					group_title.set_instrument("kind: % (%)".format(name, group.subkind))
				} {
					group_title.set_instrument("kind: %".format(name))
				}
			};

			group_responder.remove;
			group_responder = ~make_view_responder.(vlayout, group, (
				redraw: { arg self;
					"group_responder: redraw".debug;
					controller.changed(\group_items);
				},

				selected_child: { arg self, msg, idx;
					"group_responder: selected_child".debug;
					[idx, self.selected_child_index].debug("idx, selected_child_index");
					if(idx.notNil) { //FIXME: why could it be nil ?
						if(idx == self.selected_child_index) {
							if(groupview_list[idx].notNil) {
								groupview_list[idx].selected(true);
							}
						} {
							if(groupview_list[idx].notNil) {
								groupview_list[idx].selected(false);
							}
						}
					}

				}
					
			));
			controller.changed(\group_items);
		},

		group_items: { arg self;
			var display;
			var group, children;
			"sideview_responder: group_items".debug;
			group = self.get_current_group;
			children = group.get_children_and_void;
			groupview_list.do { arg groupview, x; // FIXME: hardcoded
				var child = children[x];
				var is_empty = false;
				var amp;
				if(child.notNil) {
					amp = child.get_arg(\amp);
					if( amp.isNil ) {
						"group_items: amp is nil or child is nil".debug;
						amp = ~make_empty_param.(self.get_main);
					};
					display = controller.make_param_display(amp, child);
					display.set_parent_group(group);
					groupview_list[x].set_group_param(child, amp, display);
				} {
					groupview.clear_view;
				};
			};
			"FIN sideview_responder".debug;


		},

		mode: { arg self, msg;
			switch(self.model.current_mode,
				\group, {
					//sep_bar.background = Color.newHex("54516A");
				},
				{
					//sep_bar.background = Color.black;
				}
			)

		}
	));

	vlayout.focus;
	win.front;
	win;


};


~make_matrix_controller = { arg main;

	(
		make_gui: { arg self;
			~make_matrix_edit_view.(main, self);

		},
		window: nil,

	)

};

//a = ~make_matrix_controller.( Mdef.main );
//a.make_gui;

//~make_player_edit_view = { arg main, controller;
//};

~make_side_view_BACKUP = { arg main, controller;

	var win, vlayout, hlayout;
	var winsize= 700@800;
	var paramsize= 170@26;
	var sep, sep_bar;
	var paramview_list = List.new;
	var extparamview_list = List.new;
	var groupview_list = List.new;
	var extparam_layout, inner_extparam_layout;
	var player_responder;
	var group_responder;
	var player_title;
	var group_title;

	win = Window.new(bounds:Rect(750,0,winsize.x,winsize.y));
	win.view.keyDownAction = main.commands.get_kb_responder(\side);

	controller.window = win;

	vlayout = VLayoutView.new(win, Rect(3,0,winsize.x-13,winsize.y));
	///// status view

	~make_status_view_horizontal.(vlayout, main.play_manager, winsize.x@20);

	player_title = ~make_player_title_bar_view.(vlayout, winsize.x@20);

	///// mini param view

	3.do {
		sep = VLayoutView(vlayout, Rect(0,0,winsize.x-158,paramsize.y*2+35));
		2.do {
			hlayout = HLayoutView.new(sep, Rect(0,0,winsize.x-150,paramsize.y+15));
			4.do {
				paramview_list.add( ~make_mini_param_view.(hlayout, paramsize) );
			};
		};
		sep.background = Color.newHex("B4C1CA");
		sep = StaticText.new(vlayout,Rect(0,0,10,3));
		//sep.background = ~color_scheme.control2;
	};

	///// extended param view

	//extparam_layout = VLayoutView.new(vlayout, Rect(0,0,winsize.x,(paramsize.y+11)*8));
	extparam_layout = VLayoutView.new(vlayout, Rect(0,0,winsize.x,(paramsize.y+11)*7));
	inner_extparam_layout = VLayoutView.new(extparam_layout);

	///// groupnode view

	sep_bar = StaticText.new(vlayout,Rect(0,0,10,08));
	sep_bar.background = Color.newHex("54516A");
	sep_bar.background = Color.black;
	sep = StaticText.new(vlayout,Rect(0,0,10,3));

	group_title = ~make_player_title_bar_view.(vlayout, winsize.x@20);

	sep = VLayoutView(vlayout, Rect(0,0,winsize.x-158,paramsize.y*2+35));
	2.do {
		hlayout = HLayoutView.new(sep, Rect(0,0,winsize.x-150,paramsize.y+15));
		4.do {
			groupview_list.add( ~make_mini_param_view.(hlayout, paramsize) );
		};
	};

	~make_view_responder.(vlayout, controller, (
		player: { arg self;
			var player;
			player = self.get_current_player;
			if(player.notNil) {

				if(player.name != player.uname) {
					player_title.set_name("% (%)".format(player.name, player.uname));
				} {
					player_title.set_name(player.name);
				};
				~notNildo.(player.defname) { arg name;
					player_title.set_instrument("instr:"+name)
				};

				player.name.debug("side_view responder: player");
				player_responder.remove;
				player_responder = ~make_view_responder.(vlayout, player, (
					mode: { arg self;
						controller.changed(\paramlist);
						controller.changed(\extparamlist);
					},

					redraw_node: { arg self;
						player.get_arg(\amp).changed(\playingstate, player.name, player.get_playing_state);
					}
						
				));
				controller.changed(\paramlist);
				controller.changed(\extparamlist);
				player.get_arg(\amp).changed(\selected);
			}
		},

		paramlist: { arg self, msg;
			var param;
			var args;
			var display;
			var player = controller.current_player;
			var args2;
			//player.get_ordered_args.do  arg param_name, idx;


			args = self.get_paramlist_splited;
			args2 = args[1] ++ args[2];
			
			paramview_list.do.do { arg view, idx;
				var param_name;
				if(idx < 8) {
					param_name = args[0][idx];
				} {
					param_name = args2[idx-8];
				};
				if(param_name.notNil) {
					param = player.get_arg(param_name);
					display = controller.make_param_display(param);
					view.set_param(param, display);
				} {
					view.clear_view;
				}
			};

		},

		extparamlist: { arg self, msg;
			var param;
			var args;
			var display;
			var paramview;
			var player = controller.current_player;

			inner_extparam_layout.remove;
			debug("REMOVING!!!");
			inner_extparam_layout = VLayoutView.new(extparam_layout, Rect(0,0,winsize.x,500));
			extparamview_list = List.new;

			args = self.get_extparamlist;
			args.debug("make_side_view: handler: extparamlist");
			args.do { arg param_name, idx;
				param = player.get_arg(param_name);
				paramview = ~make_extparam_view.(main, self, inner_extparam_layout, player, param);
				extparamview_list.add(paramview);
			};

		},

		nodegroup: { arg self, msg;
			var group;
			"sideview_responder: group".debug;
			group = self.get_current_group;

			if(group.name != group.uname) {
				group_title.set_name("% (%)".format(group.name, group.uname));
			} {
				group_title.set_name(group.name);
			};
			~notNildo.(group.kind) { arg name;
				if(group.subkind.notNil) {
					group_title.set_instrument("kind: % (%)".format(name, group.subkind))
				} {
					group_title.set_instrument("kind: %".format(name))
				}
			};

			group_responder.remove;
			group_responder = ~make_view_responder.(vlayout, group, (
				redraw: { arg self;
					"group_responder: redraw".debug;
					controller.changed(\group_items);
				},

				selected_child: { arg self, msg, idx;
					"group_responder: selected_child".debug;
					[idx, self.selected_child_index].debug("idx, selected_child_index");
					if(idx.notNil) { //FIXME: why could it be nil ?
						if(idx == self.selected_child_index) {
							if(groupview_list[idx].notNil) {
								groupview_list[idx].selected(true);
							}
						} {
							if(groupview_list[idx].notNil) {
								groupview_list[idx].selected(false);
							}
						}
					}

				}
					
			));
			controller.changed(\group_items);
		},

		group_items: { arg self;
			var display;
			var group, children;
			"sideview_responder: group_items".debug;
			group = self.get_current_group;
			children = group.get_children_and_void;
			groupview_list.do { arg groupview, x; // FIXME: hardcoded
				var child = children[x];
				var is_empty = false;
				var amp;
				if(child.notNil) {
					amp = child.get_arg(\amp);
					if( amp.isNil ) {
						"group_items: amp is nil or child is nil".debug;
						amp = ~make_empty_param.(self.get_main);
					};
					display = controller.make_param_display(amp, child);
					display.set_parent_group(group);
					groupview_list[x].set_group_param(child, amp, display);
				} {
					groupview.clear_view;
				};
			};
			"FIN sideview_responder".debug;


		},

		mode: { arg self, msg;
			switch(self.model.current_mode,
				\group, {
					sep_bar.background = Color.newHex("54516A");
				},
				{
					sep_bar.background = Color.black;
				}
			)

		}



	));


	vlayout.focus;
	win.front;
	win;

};
