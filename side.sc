(

~make_player_title_bar_view = { arg parent, size;
	var layout;
	var txt_name, txt_instr;
	layout = HLayoutView.new(parent, Rect(0,0,size.x,size.y));
	txt_name = StaticText(layout, Rect(0,0,250,size.y));
	txt_instr = StaticText(layout, Rect(0,0,150,size.y));
	(
		set_name: { arg self, name;
			txt_name.string = name;
		},

		set_instrument: { arg self, instr;
			txt_instr.string = instr;
		}
	)
};

~make_mini_param_view = { arg parent, size;
	var vlayout, hlayout;
	var bt_name, txt_midi_label, kind, txt_midi_val, paramval, slider;
	var bsize = 23;
	var font;
	var param_view, param_responder;
	var paramval_size = Rect(0,0,(size.x-(bsize*2))/2,size.y/2);
	var slider_action = { arg param;
		{ param.set_norm_val(slider.value) }
	};
	font = Font.default;
	font.size = 12;
	font.setDefault;

	vlayout = VLayoutView.new(parent, Rect(0,0,size.x,size.y+5));
	bt_name = StaticText.new(vlayout, Rect(0,0,size.x,size.y/2));
	hlayout = HLayoutView.new(vlayout, Rect(0,0,size.x,size.y/2));
	slider = Slider(vlayout, Rect(0,0,size.x,5));
	txt_midi_label = StaticText.new(hlayout, Rect(0,0,bsize-7,size.y/2));
	kind = StaticText.new(hlayout, Rect(0,0,bsize,size.y/2));
	txt_midi_val = StaticText.new(hlayout, Rect(0,0,(size.x-(bsize*2)-15)/2,size.y/2));
	paramval = StaticText.new(hlayout, paramval_size);

	slider.keyDownAction = {};

	bt_name.string = "";
	txt_midi_label.string = "";
	kind.string = "";
	txt_midi_val.string = "";
	paramval.string = "";

	vlayout.background = ~color_scheme.background;
	//bt_name.background = ~color_scheme.control;
	//bt_name.background = Color.newHex("343154");
	bt_name.background = Color.newHex("54516A");
	bt_name.stringColor = Color.white;
	bt_name.font = font.boldVariant;
	//txt_midi_label.background = ~color_scheme.control;
	txt_midi_label.stringColor = Color.white;
	//kind.background = ~color_scheme.control;
	kind.stringColor = Color.white;
	txt_midi_val.background = ~color_scheme.control;
	txt_midi_val.stringColor = Color.white;
	paramval.background = ~color_scheme.control;
	paramval.stringColor = Color.white;

	param_responder = { arg display; (
		selected: { arg self;
			param_view.selected(display.selected == 1);
		},

		val: { arg self, msg, cellidx;
			var newval;
			paramval.string = self.get_val(cellidx);

			if(slider.notNil, {
				slider.value = self.get_norm_val(cellidx) ?? 0;
			});
		},

		selected_cell: { arg self;
			self.changed(\val);
		},

		kind: { arg self;
			kind.string = if(self.pkey_mode.notNil and: {self.pkey_mode}) {
				"KEY"
			} {
				if([\stepline,\sampleline,\noteline].includes(self.classtype)) {
					""
				} {
					switch(self.current_kind,
						\seq, { "seq" },
						\seg, { "sg" },
						\scalar, { "sca" },
						\bus, { "bus" },
						\recordbus, { "rbu" },
						\preset, { "pre" },
						{ "???" }
					)
				}
			};
		},

		label: { arg self;
			txt_midi_label.string = self.midi.label;
		},
		midi_val: { arg self, msg, val;
			txt_midi_val.string = val;
		},
		blocked: { arg self, msg, blocked;
			txt_midi_val.background = if(blocked.not, { ~color_scheme.led_ok }, { ~editplayer_color_scheme.led });
		},
		recording: { arg self, msg, recording;
			txt_midi_label.background = if(recording, { ~editplayer_color_scheme.led }, { Color.clear });
		}
	)};

	param_view = (
		responder: nil,
		display_mode: \number,
		
		set_display_mode: { arg self, mode;
			if(self.display_mode != mode) {
				if(mode == \text) {
					self.display_mode = mode;
					txt_midi_val.visible = false;
					txt_midi_label.visible = false;
					kind.visible = false;

					paramval.bounds = Rect(0,0,(size.x),size.y/2);
				} {
					self.display_mode = mode;
					txt_midi_val.visible = true;
					txt_midi_label.visible = true;
					kind.visible = true;

					paramval.bounds = paramval_size;

				}

			}

		},

		selected: { arg self, bool;
			if(bool) {
				self.name.debug("mini_param_group_widget: selected: je suis select");
				bt_name.debug("bt_name");
				bt_name.background = Color.newHex("B4B1BA");
			} {
				self.name.debug("mini_param_group_widget: selected: je suis DEselect");
				bt_name.background = Color.newHex("54516A");
			}
		},

		clear_view: { arg self;
			bt_name.string = "";
			txt_midi_val.string = "";
			txt_midi_label.string = "";
			kind.string = "";
			paramval.string = "";

		},

		set_param: { arg self, param, display;
			bt_name.string = param.name;

			self.kind = \param;
			
			if( self.responder.notNil ) {
				self.responder.remove
			};
			[param.name, param.classtype].debug("OOOOOOOOOOOOOOOOO");
			if([\buf, \samplekit].includes(param.classtype)) {
				self.set_display_mode(\text);
			} {
				self.set_display_mode(\number);
			};

			slider.action = slider_action.(param);

			self.responder = ~make_view_responder.(vlayout, param, param_responder.(display));
		},

		set_group_param: { arg self, player, param, display;
			var resp;
			self.kind = \player;
			bt_name.string = player.name;
			
			if( self.responder.notNil ) {
				self.responder.remove
			};

			resp = param_responder.(display);
			resp.putAll((
				kind: { arg self;
					kind.string = switch(player.kind,
						\player, { "pla" },
						\parnode, { "par" },
						\seqnode, { "seq" },
						{ "???" }
					)
				},
				playingstate: { arg self, msg, name, state;
					state = switch(state,
						\play, { " >" },
						\stop, { "  " },
						\mute, { "M>" },
						\mutestop, { "M " },
						{ "? " }
					);


					bt_name.string = state ++ " " ++ name;
				}

			));
			resp[\selected] = nil;

			slider.action = slider_action.(param);

			self.responder = ~make_view_responder.(vlayout, param, resp);
		}

	)

};

~make_extparam_view = { arg main, controller, parent, player, param;
	var param_name = param.name;
	var info_layout = parent;
	var row_layout = parent;
	case
		{ [\adsr].includes(param_name) || param_name.asString.containsStringAt(0,"adsr_") } {
			~make_env_control_view.(row_layout, player, controller.make_param_display(param), param);
		}
		{ [\repeat].includes(param_name) } {
			~make_simple_control_view.(info_layout, controller.make_param_display(param), param);
		}
		{ [\samplekit].includes(param_name) } {
			~make_string_param_view.(info_layout, controller.make_param_display(param), param);
		}
		{ [\dur].includes(param_name) } {
			//if(player.noteline.not, {
				~make_simple_control_view.(info_layout, controller.make_param_display(param), param);
			//})
		}
		{ [\bufnum].includes(param_name)|| param_name.asString.containsStringAt(0,"bufnum_") } {
			~make_bufnum_view.(info_layout, controller.make_param_display(param), param);
		}
		{ [\segdur, \stretchdur].includes(param_name) } {
			//if(player.noteline, {
				~make_simple_control_view.(info_layout, controller.make_param_display(param), param);
			//});
		}
		{ controller.param_types.param_mode.includes(param_name) } {
			[player.get_mode, param_name].debug("LLLLLLLLLLLL making line");
			if(player.get_mode == param_name, {
				param_name.debug("LLLLLLLLLLLL making line vraiment");
				~make_line_view.(param_name, row_layout, controller.make_param_display(param), param);
			});
		}
		{ [\legato, \amp, \pan, \attack, \release, \sustain].includes(param_name)} {
			~make_control_view.(info_layout, controller.make_param_display(param), param);
		}
		{ true } {
			"standard param".debug;
			~make_control_view.(row_layout, controller.make_param_display(param).debug("il a quoi le display"), param);
		};
};

~make_mini_param_group_widget = { arg parent, rows, display;
	var sep, sep2;
	var hlayout, vlayout;
	var size;
	var paramview_list = List.new;
	display.winsize= display.winsize ?? (700@800);
	display.paramsize= display.paramsize ?? (170@26);

	size = rows ?? 3;

	vlayout = VLayoutView.new(parent, Rect(3,0,display.winsize.x-13,display.paramsize.y+15*2*size+35)); //FIXME: scaling is wrong

	size.do {
		sep = VLayoutView(vlayout, Rect(0,0,display.winsize.x-158,display.paramsize.y*2+35));
		2.do {
			hlayout = HLayoutView.new(sep, Rect(0,0,display.winsize.x-150,display.paramsize.y+15));
			4.do {
				paramview_list.add( ~make_mini_param_view.(hlayout, display.paramsize) );
			};
		};
		sep.background = Color.newHex("B4C1CA");
		sep2 = StaticText.new(vlayout,Rect(0,0,10,3));
		//sep.background = ~color_scheme.control2;
	};

	(
		sep: sep,
		sep2: sep2,
		vlayout: vlayout,
		hlayout: hlayout,
		paramview_list: paramview_list,
	)
};

~make_extparam_group_widget = { arg main, parent, display;
	var extparam_layout;
	var inner_extparam_layout;
	extparam_layout = VLayoutView.new(parent, Rect(0,0,display.winsize.x,(display.paramsize.y+11)*7));
	inner_extparam_layout = VLayoutView.new(extparam_layout);
	(
		extparam_layout: extparam_layout,
		inner_extparam_layout: inner_extparam_layout,
		extparamview_list: List.new,
		make_extparam_view: { arg self, controller, player, param;
			~make_extparam_view.(main, controller, self.inner_extparam_layout, player, param);
		},

	)
};

~make_stepmatrix_widget = { arg main, parent, display;

	var layout;
	var sublayout;
	layout = VLayoutView.new(parent, Rect(0,0,display.winsize.x,(display.paramsize.y+11)*7));
	sublayout = VLayoutView.new(layout, Rect(0,0,display.winsize.x,(display.paramsize.y+11)*7));
	(
		layout: layout,
		sublayout: sublayout,
		viewlist: List.new,
		playerlist: List.new,
		reset_layout: { arg self;
			self.sublayout.remove;
			self.sublayout = VLayoutView.new(self.layout, Rect(0,0,display.winsize.x,(display.paramsize.y+11)*7));
			self.playerlist = List.new;
			self.viewlist = List.new;
		},
		make_line: { arg self, controller, player, parent_group;
			var param = player.get_arg(player.get_mode);
			var pdisplay = controller.make_param_display_matrix(param, player);
			pdisplay.name = player.name;
			pdisplay.set_parent_group(parent_group);
			~make_line_view2.(player.get_mode, self.sublayout, pdisplay, param);
		},

	)


};

~class_player_view = (
	new: { arg self, main, controller, parent, display;
		self = self.deepCopy;

		self.display = display;
		//self.main = main; // FIXME: vicious circle ?
		self.controller = controller;
		self.vlayout = VLayoutView.new(parent, Rect(3,0,display.winsize.x-13,display.player_view_y));
		self.player_title = ~make_player_title_bar_view.(self.vlayout, display.winsize.x@20);
		self.mini_param_group_widget = ~make_mini_param_group_widget.(self.vlayout, display.mini_param_row_count, display);
		//self.extparam_group_widget = ~make_extparam_group_widget.(main, self.vlayout, display);
		self.ext_view = ~make_switch_view.(self.vlayout, (display.winsize.x-13)@display.player_view_y, \params, (
			params: { arg layout; "XOX create params".debug; ~make_extparam_group_widget.(main, layout, display); },
			matrix: { arg layout; "XOX create params".debug; ~class_groupnode_matrix_view.new(main, controller, layout, display); },
		));

		"class_player_view: new: before making responder".debug;
		self.main_responder = ~make_class_responder.(self, self.vlayout, controller, [
			\player, \paramlist, \extparamlist, \edit_mode,
		]);
		"class_player_view: new: after making responder".debug;

		self;
	},

	////// main responders

	player: { arg self, controller;
		var player;
		controller = controller ?? self.controller;
		"class_player_view: player".debug;
		player = controller.get_current_player;
		if(player.notNil) {
			self.current_player = player;

			if(player.name != player.uname) {
				self.player_title.set_name("% (%)".format(player.name, player.uname));
			} {
				self.player_title.set_name(player.name);
			};
			~notNildo.(player.defname) { arg name;
				self.player_title.set_instrument("instr:"+name)
			};

			player.name.debug("side_view responder: player");
			self.player_responder.remove;
			"class_player_view: player: before making responder".debug;
			self.player_responder = ~make_class_responder.(self, self.vlayout, player, [
				\mode, 
			]);
			"class_player_view: player: after making responder".debug;
			self.paramlist;
			self.extparamlist;
			player.get_arg(\amp).changed(\selected);
		}
	},

	paramlist: { arg self, controller;
		var param;
		var args;
		var display;
		var player = self.current_player;
		var args2;

		"class_player_view: paramlist".debug;
		//player.get_ordered_args.do { arg param_name, idx;
		controller = controller ?? self.controller;

		args = controller.get_paramlist_splited;
		args2 = args[1] ++ args[2];
		
		self.mini_param_group_widget.paramview_list.do { arg view, idx;
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

	extparamlist: { arg self, controller;
		var param;
		var args;
		var display;
		var paramview;
		var player = self.current_player;
		var extparam_group_widget;

		if(self.ext_view.current_key == \params) {
			extparam_group_widget = self.ext_view.current_view;	

			"class_player_view: extparamlist".debug;
			controller = controller ?? self.controller;

			extparam_group_widget.inner_extparam_layout.remove;
			debug("REMOVING!!!");
			extparam_group_widget.inner_extparam_layout = VLayoutView.new(extparam_group_widget.extparam_layout, Rect(0,0,self.display.winsize.x,500));
			extparam_group_widget.extparamview_list = List.new;

			args = controller.get_extparamlist;
			args.debug("make_side_view: handler: extparamlist");
			args.do { arg param_name, idx;
				param = player.get_arg(param_name);
				paramview = extparam_group_widget.make_extparam_view(controller, player, param);
				extparam_group_widget.extparamview_list.add(paramview);
			};
		}


	},

	edit_mode: { arg self, obj;
		"class_player_view: controller: edit_mode".debug;
		//self.paramlist;
		//self.extparamlist;
		switch(self.controller.model.current_edit_mode,
			\params, {
				self.ext_view.switch_to_view(\params);
				self.extparamlist;
				//sep_bar.background = Color.newHex("54516A");
			},
			\matrix, {
				self.ext_view.switch_to_view(\matrix);
			},
			{
				self.ext_view.switch_to_view(\params);
				self.extparamlist;
				//sep_bar.background = Color.black;
			}
		)
	},

	/////// player responders

	mode: { arg self, obj;
		"class_player_view: player: mode".debug;
		self.paramlist;
		self.extparamlist;
	},

);

~class_groupnode_view = (
	new: { arg self, main, controller, parent, display;
		self = self.deepCopy;

		self.display = display;
		self.controller = controller;
		self.vlayout = VLayoutView.new(parent, Rect(3,0,display.winsize.x-13,display.winsize.y));
		self.group_title = ~make_player_title_bar_view.(self.vlayout, display.winsize.x@20);

		self.mini_param_group_widget = ~make_mini_param_group_widget.(self.vlayout, display.groupnode_row_count, display);

		"class_groupnode_view: new: before making responder".debug;
		self.main_responder = ~make_class_responder.(self, self.vlayout, controller, [
			\nodegroup, \group_items,
		]);
		"class_groupnode_view: new: after making responder".debug;

		self;
	},

	////// main responders

	nodegroup: { arg self, controller;
		var group;
		"class_groupnode_view.update_responder: group".debug;

		controller = controller ?? self.controller;

		group = controller.get_current_group;
		self.current_group = group;

		if(group.name != group.uname) {
			//self.group_title.set_name("%/%/% (%)".format(controller.get_part_name, controller.get_section_name, group.name, group.uname));
			self.group_title.set_name("% (%)".format(group.name, group.uname));
		} {
			//self.group_title.set_name("%/%/%".format(controller.get_part_name, controller.get_section_name, group.name));
			self.group_title.set_name("%".format(group.name));
		};
		~notNildo.(group.kind) { arg name;
			if(group.subkind.notNil) {
				self.group_title.set_instrument("kind: % (%)".format(name, group.subkind))
			} {
				self.group_title.set_instrument("kind: %".format(name))
			}
		};

		"class_groupnode_view: group: before making responder".debug;
		self.group_responder.remove;
		self.group_responder = ~make_class_responder.(self, self.vlayout, group, [
			\redraw,
			\selected_child,
		]);
		"class_groupnode_view: group: after making responder".debug;
		self.group_items;
	},

	group_items: { arg self, controller;
		var display;
		var group, children;

		controller = controller ?? self.controller;

		"XXXXXXXXXXXXXXXXXXXXXXXXXXX class_groupnode_view.update_group_items".debug;

		group = self.current_group;
		children = group.get_children_and_void;
		self.children_responder.do{_.remove};
		self.mini_param_group_widget.paramview_list.do { arg groupview, x; // FIXME: hardcoded
			var child = children[x];
			var is_empty = false;
			var amp;
			if(child.notNil) {
				amp = child.get_arg(\amp);
				if( amp.isNil ) {
					"group_items: amp is nil or child is nil".debug;
					amp = ~make_empty_param.(controller.get_main);
				};
				display = controller.make_param_display(amp, child);
				display.set_parent_group(group);
				groupview.set_group_param(child, amp, display);

				self.children_responder.add(~make_class_responder.(self, self.vlayout, child, [
					\redraw_node,
				]));
			} {
				groupview.clear_view;
			};
		};
		"FIN class_groupnode_view.update_group_items".debug;

	},

	///// group responders

	redraw: { arg self, controller;
		"class_groupnode_view: group: redraw".debug;
		self.group_items;
	},

	selected_child: { arg self, controller, msg, idx;
		var groupview_list = self.mini_param_group_widget.paramview_list;
		"class_groupnode_view: group: selected_child".debug;
		[idx, controller.selected_child_index].debug("idx, selected_child_index");
		if(idx.notNil) { //FIXME: why could it be nil ?
			if(idx == controller.selected_child_index) {
				if(groupview_list[idx].notNil) {
					groupview_list[idx].selected(true);
				}
			} {
				if(groupview_list[idx].notNil) {
					groupview_list[idx].selected(false);
				}
			}
		}

	},

	///// children responders

	redraw_node: { arg self, obj;
		var player;
		player = obj;
		player.uname.debug("class_player_view: player: redraw_node");
		player.get_arg(\amp).changed(\playingstate, player.name, player.get_playing_state);
	},
);

~class_groupnode_matrix_view = (
	parent: ~class_groupnode_view,
	new: { arg self, main, controller, parent, display;
		self = self.deepCopy;

		self.display = display;
		self.controller = controller;
		self.vlayout = VLayoutView.new(parent, Rect(3,0,display.winsize.x-13,display.winsize.y));

		self.stepmatrix = ~make_stepmatrix_widget.(main, self.vlayout, display);


		"class_groupnode_view: new: before making responder".debug;
		self.main_responder = ~make_class_responder.(self, self.vlayout, controller, [
			\nodegroup, \group_items,
		]);
		"class_groupnode_view: new: after making responder".debug;

		self;
	},

	////// main responders

	nodegroup: { arg self, controller;
		var group;
		"class_groupnode_view.update_responder: group".debug;

		controller = controller ?? self.controller;

		group = controller.get_current_group;
		self.current_group = group;

		"class_groupnode_view: group: before making responder".debug;
		self.group_responder.remove;
		self.group_responder = ~make_class_responder.(self, self.vlayout, group, [
			\redraw,
			\selected_child,
		]);
		"class_groupnode_view: group: after making responder".debug;
		self.group_items;
	},

	group_items: { arg self, controller;
		var display;
		var group, children;

		controller = controller ?? self.controller;

		"XXXXXXXXXXXXXXXXXXXXXXXXXXX class_groupnode_view.update_group_items".debug;

		group = self.current_group;
		children = group.get_children_and_void;
		self.stepmatrix.reset_layout;
		children.do { arg child, x; // FIXME: hardcoded
			var view;
			child.uname.debug("°°°°°°°°° stepmatrix: making child");
			self.stepmatrix.make_line(controller, child, group);
			self.stepmatrix.playerlist.add(child);
		};
		"FIN class_groupnode_view.update_group_items".debug;

	},

	///// group responders

	redraw: { arg self, controller;
		"class_groupnode_view: group: redraw".debug;
		self.group_items;
	},

	selected_child: { arg self, controller, msg, idx;
		var list = self.stepmatrix.playerlist;
		"class_groupnode_view: group: selected_child".debug;
		[idx, controller.selected_child_index].debug("idx, selected_child_index");
		if(idx.notNil) { //FIXME: why could it be nil ?
			if(list[idx].notNil) {
				idx.debug("class_groupnode_matrix_view: selected_child:");
				list[idx].get_arg(list[idx].get_mode).changed(\selected);
			}
		}

	},

);

~make_switch_view = { arg parent, size, default_key, viewlist;
	var view = (
		viewlist: viewlist,
		layout: VLayoutView.new(parent, Rect(0,0,size.x,size.y)),
		current_view: nil,
		current_key: nil,
		switch_to_view: { arg self, key;
			if(key != self.current_key) {
				self.current_key = key;
				self.sublayout.remove;
				self.sublayout = VLayoutView.new(self.layout, Rect(0,0,size.x,size.y));
				self.current_view = self.viewlist[key].value(self.sublayout);
			}
		},
	);
	view.switch_to_view(default_key);
	view;

};

~make_side_view = { arg main, controller;
	var win, vlayout, hlayout;
	var sep, sep_bar;
	var paramview_list = List.new;
	var extparamview_list = List.new;
	var groupview_list = List.new;
	var extparam_layout, inner_extparam_layout;
	var player_responder;
	var group_responder;
	var group_title;
	var player_edit_view, group_node_view;
	var display;
	var edit_view;

	var winsize= 700@800;
	var paramsize= 170@26;

	display = (
		winsize: 700@800,
		player_view_y: 550,
		paramsize:170@26,
		mini_param_row_count: 3,
		groupnode_row_count: 1,
	);

	win = Window.new(bounds:Rect(750,0,display.winsize.x,display.winsize.y));
	win.view.keyDownAction = main.commands.get_kb_responder(\side);

	controller.window = win;

	vlayout = VLayoutView.new(win, Rect(3,0,display.winsize.x-13,display.winsize.y+200));
	///// status view

	~make_status_view_horizontal.(vlayout, main.play_manager, display.winsize.x@20);

	///// player edit view

	player_edit_view = ~class_player_view.new(main, controller, vlayout, display);

	//edit_view = ~make_switch_view.(vlayout, (display.winsize.x-13)@display.player_view_y, \params, (
	//	params: { arg layout; "XOX create params".debug; ~class_player_view.new(main, controller, layout, display); },
	//	matrix: { arg layout; "XOX create params".debug; ~class_groupnode_matrix_view.new(main, controller, layout, display); },
	//));

	///// separator

	sep_bar = StaticText.new(vlayout,Rect(0,0,10,08));
	sep_bar.background = Color.newHex("54516A");
	sep_bar.background = Color.black;
	sep = StaticText.new(vlayout,Rect(0,0,10,3));

	///// groupnode view

	group_node_view = ~class_groupnode_view.new(main, controller, vlayout, display);

	~make_view_responder.(vlayout, controller, (

		//mode: { arg self, msg;
		//	"sideview_responder: mode".debug;
		//	switch(self.model.current_mode,
		//		\group, {
		//			edit_view.switch_to_view(\params);
		//			sep_bar.background = Color.newHex("54516A");
		//		},
		//		\matrix, {
		//			edit_view.switch_to_view(\matrix);
		//		},
		//		{
		//			edit_view.switch_to_view(\params);
		//			sep_bar.background = Color.black;
		//		}
		//	)

		//},

	));

	vlayout.focus;
	win.front;
	win;
};


~make_side_panel = { arg main;
	var side;
	var param_types;

	param_types = (
		param_field_group: List[\dur, \segdur, \stretchdur, \repeat],
		param_slider_group: List[\amp, \legato, \pan, \attack, \sustain, \release],
		param_status_group: List[\amp, \dur, \segdur, \stretchdur, \repeat, \bufnum, \samplekit],
		param_order: List[\sustain, \pan, \attack, \release, \adsr, \freq],
		param_mode: [\stepline, \noteline, \sampleline, \nodeline],
		param_no_midi: { arg self; self.param_field_group ++ [\bufnum, \samplekit] ++ self.param_mode; },
		param_reject: { arg self; [\out, \instrument, \type, \gate, \agate, \t_trig] ++ self.param_mode; },
	);

	side = (
		archive_data: [\model],
		param_types: param_types,

		get_main: { arg self; main },

		model: (
			param_no_midi: param_types.param_no_midi,
			select_offset: 0,
			max_cells: 8,
			current_mode: \param,
			current_edit_mode: \param,
			colselect_mode: true,
			midi_knob_offset: 0
		),

		save_data: { arg self;
			var data = Dictionary.new;
			self.archive_data.do { arg key;
				data[key] = self[key];
			};
			data[\current_player] = self.current_player.uname;
			data[\current_group] = self.current_group.uname;
			data;
		},

		load_data: { arg self, data;
			var group;
			self.archive_data.do { arg key;
				self[key] = data[key];
			};
			group = main.get_node(data[\current_group]);
			self.set_current_group( group );
			self.set_current_player( main.get_node(group.selected_child), group.selected_child_index );
			self.refresh;
		},

		make_param_display: { arg editplayer, param, player=nil;
			player = player ?? editplayer.get_current_player;
			(
				extparam_content_size: 590@100,
				set_parent_group: { arg self, group;
					self.parent_group = group;
				},
				get_bank: { arg self;
					//player.get_bank.debug("display.get_bank");
					[player.uname, player.get_bank].debug("side: make_param_display: get bank");
					player.get_bank;
				},
				get_player_bank: { arg self;
					player.get_bank.debug("side: make_param_display: get player bank");
					player.get_bank;
				},
				selected: { arg self;
					if(self.parent_group.notNil) {
						self.parent_group.name.debug("le fameux group");

						[player.name, self.parent_group.selected_child].debug("group: il s'interroge s'il est selectioné");
						if( self.parent_group.selected_child == player.uname ) { 1 } { 0 } // FIXME: name or uname ?
						//0;
					} {
						[param.extname, param.name, player.get_selected_param].debug("il s'interroge s'il est selectioné");
						if( player.get_selected_param_object === param ) { 1 } { 0 }
					}
				},
				max_cells: { arg self;
					editplayer.model.max_cells;	
				},
				get_selected_cell: {
					param.get_selected_cell;
				},
				name: { arg self;
					"chu dans name".debug;
					param.name;
				},
				slider_width: 100,
				background_color: ~editplayer_color_scheme.control,
				show_midibloc: false,
				width: 200,
				height: 30,
				name_width: { arg self;
					50;
				}
			);
		},

		make_param_display_matrix: { arg self, param, player;
			var display;
			display = self.make_param_display(param, player);
			display.max_cells = 16;
			display.cell_width = 30;
			display;
		},

		get_paramlist_splited: { arg self;
			var player = self.current_player;
			var mode;
			var args;
			var args1;
			var args2;
			var args3;
			if(player.uname == \voidplayer) {
				[[], []];
			} {
				args = player.get_ordered_args;
				args = args.reject { arg x; param_types.param_reject.includes(x) };
				mode = player.get_mode;
				args = ~sort_by_template.(args, param_types.param_status_group ++ param_types.param_order);

				if(player.kind == \player) {
					args = [mode] ++ args;
				};

				// FIXME: handle legato
				args = args.reject { arg x; x == \legato };

				args1 = args.select { arg x; ([mode] ++ param_types.param_status_group).includes(x) };
				args2 = args.reject { arg x; ([mode] ++ param_types.param_status_group).includes(x) };
				args3 = self.get_effects_paramlist;

				[args1, args2, args3];
			}
		},

		get_effects_paramlist: { arg self, num;
			var player = self.get_current_player;
			var res = List.new;
			player.get_effects.do { arg fxname, i;
				var fx = main.get_node(fxname);
				res.addAll( self.get_effect_paramlist(fx, i) );
			};
			res;
		},

		get_effect_paramlist: { arg self, effect, num;
			//var reject = [\in, \out];
			var reject = [\gate, \out, \instrument];
			var prio = [\dry, \wet];
			num.debug("get_effect_paramlist");
			effect.get_args.difference(reject).collect { arg argname;
				(argname.asString ++ "_fx" ++ num.asString).asSymbol
			};
		},

		get_paramlist: { arg self;
			var player = self.current_player;
			var mode;
			var args;
			
			self.get_paramlist_splited.flat;
		},

		get_extparamlist: { arg self;
			var args;
			var param;
			var res;
			var player = self.current_player;
			args = self.get_paramlist;
			args.select({ arg param_name;
				param = player.get_arg(param_name);
				if(([\control]++param_types.param_mode).includes(param.classtype)) { 
					res = switch(param.current_kind,
						\scalar, { false },
						\bus, { false },
						\preset, { false },
						// else
						{ true }
					);
				} {
					res = false
				};
				if(param.classtype == \adsr) {
					res = true
				};
				[param_name, res].debug("get_extparamlist: select:");
				res;
			});
		},

		assign_midi_mixer: { arg self;
			var param;
			var player;
			var offset = self.model.midi_knob_offset;
			var kind = \slider;

			self.get_current_group.children.do { arg child_name;
				//player.name.debug("assign_midi player.name");
				//offset.debug("assign_midi offset");
				//param_name.debug("param_name");
				player = main.get_node(child_name);

				if(player.uname != \voidplayer) {

					main.midi_center.assign_first(kind, player.get_arg(\amp));

				}
			};

		},

		assign_midi: { arg self;
			main.midi_center.clear_assigned(\slider);
			main.midi_center.clear_assigned(\knob);
			if(self.model.current_mode == \mixer) {
				self.assign_midi_mixer;
				self.assign_midi_params;
			} {
				self.assign_midi_params;
			}
		},

		assign_midi_params: { arg self;
			var param;
			var player = self.current_player;
			var offset = self.model.midi_knob_offset;
			var kind = \knob;
			if(player.notNil) {
				[player.name].debug("side.assign_midi");

				self.get_paramlist.do { arg param_name;
					player.name.debug("assign_midi player.name");
					offset.debug("assign_midi offset");
					param_name.debug("param_name");
					param = player.get_arg(param_name);

					if(main.midi_center.is_next_free(kind).not) {
						kind = \slider;
					};

					case
						{ [\adsr].includes(param_name) || param_name.asString.containsStringAt(0,"adsr_") } {
							//TODO: working ?
							main.midi_center.assign_adsr(param)
						}
						{ \amp == param_name } {
							if(self.model.current_mode != \mixer) {
								param.debug("midi assign: amp param");
								param.midi.get_param.debug("verif param1");
								main.commands.bind_param([\knob, 8], param);
							};
						}
						{ self.model.param_no_midi.includes(param_name) } {
							// no midi
						}
						//{ self.param_types.param_slider_group.includes(param_name)} {
						//		main.midi_center.assign_first(\slider, param);
						//}
						{ true } {
							if(param.midi.notNil) {
								if(offset <= 0) {
									main.midi_center.assign_first(kind, param);
									//[offset, param.name].debug("assign_midi assign param");
								} {
									offset = offset - 1;
									//offset.debug("assign_midi offset<");
								};
							}
						};
				};
			}

		},

		///// mode
		
		set_current_mode: { arg self, mode;
			if(mode != self.model.current_mode) {
				self.model.current_mode = mode;
				self.changed(\mode);
			}
		},

		set_edit_mode: { arg self, mode;
			if(mode != self.model.current_edit_mode) {
				self.model.current_edit_mode = mode;
				self.changed(\edit_mode);
			}
		},


		///// param

		get_selected_param: { arg self; 
			var player = self.get_current_player;
			var param_name = player.get_selected_param;
			player.get_arg(param_name);
		},

		select_param: { arg self, index;
			var oldsel, sel;
			var pl;
			var param;
			var player = self.get_current_player;
			if(player.notNil) {
				player.uname.debug("side: select_param: player");
				//self.model.debug("c'est dingue!!!!");
				//oldsel = self.model.selected_param;
				pl = self.get_paramlist_splited;
				if(index < 8) {
					sel = pl[0][index];
				} {
					sel = (pl[1]++pl[2])[index-8];
				};
				if(sel.notNil) {
					player.select_param(sel);
					self.model.selected_param = player.get_arg(sel);

					if(param_types.param_mode.includes(sel)) {
						"enable change_player_mode".debug;
						main.commands.enable_mode([\side, \change_player_mode]);
					} {
						"disable change_player_mode".debug;
						main.commands.disable_mode([\side, \change_player_mode]);
					};

					param = player.get_arg(sel);
					if(param.classtype == \adsr) {
						main.midi_center.assign_adsr(param);
					};
				} {
					index.debug("no param to select here");
				};
			} {
				debug("ERROR: side: select_param: current_player is nil");
			};

		},

		get_param_at: { arg self, idx;
			var player = self.get_current_player;
			player.get_arg( self.get_paramlist[idx] )
		},

		change_param_kind: { arg self, kind;
			var param = self.get_selected_param;
			if(param.classtype == \control, {
				if(kind == \pkey) {
					param.set_pkey_mode(param.pkey_mode.not);
				} {
					param.change_kind(kind);
				};
				self.changed(\extparamlist);
			});
			if(param.classtype == \buf, {
				if(kind == \pkey) {
					param.set_pkey_mode(param.pkey_mode.not);
					self.changed(\extparamlist);
				};
			});
		},

		select_cell: { arg self, idx, stepline;
			var sel, dur;
			var player = self.get_current_player;
			if(stepline.notNil) {
				player.uname.debug("side: select_cell: current player");
				sel = player.get_arg(\stepline);
				if(sel.isNil) {
					"ERROR: no stepline argument, should fix something in code ?"
				};
			} {
				sel = self.get_selected_param;
			};

			switch( sel.classtype,
				\stepline, {
					sel.toggle_cell(idx);
				}, 
				\noteline, {
					//dur = player.get_arg(\dur).preset;
					//sel.set_start_offset(idx*dur.val[dur.selected_cell]);
				},
				\control, {
					if( self.model.colselect_mode ) {
						if( sel.current_kind == \preset ) {
							sel.select_cell(idx);
						} {
							self.get_paramlist.do { arg par;
								sel = player.get_arg(par);
								if( sel.classtype == \control && {sel.current_kind != \preset } ) {
									sel.select_cell(idx);
								}
							};
						}
					} {
						sel.select_cell(idx);
					};
				}
			);
		},

		edit_selected_param: { arg self;
			var param = self.get_selected_param;
			switch(param.classtype,
				\control, {
					~make_edit_number_view.(main, "edit param", param, [\knob, 0]);
				},
				\buf, {
					var pl = self.get_current_player;
					//~choose_sample.(main, { arg buf; param.set_val(buf);  }, pl.get_arg(\samplekit).get_val)
					self.get_current_group.identityHash.debug("edit_selected_param: nodegroup: identityHash");
					~class_sample_chooser.new(main, 
						{ arg buf; param.set_val(buf);  },
						pl.get_arg(\samplekit).get_val,
						pl,
						self.get_current_group,
						param.name
					)
				},
				\samplekit, {
					~class_samplekit_chooser.new(main, { arg kit; param.set_val(kit);  })

				}
			);
		},

		///// player

		set_current_player: { arg self, player, index;
			// set player object
			var oldplayer;
			player.uname.debug("XXXXX side: set_current_player");
			if(self.current_player != player) {
				oldplayer = main.get_node(self.get_current_group.selected_child);
				if(index.notNil) {
					self.get_current_group.select_child_at(index);
				} {
					self.get_current_group.select_child(player.uname);
				};
				self.get_current_group.selected_child.debug("side.set_current_player: group: selected_child");
				self.get_current_group.selected_child_index.debug("side.set_current_player: group: selected_child_index");
				if(oldplayer.notNil) {
					main.freeze_do { oldplayer.get_arg(\amp).changed(\selected); };
				};
				self.current_player = player;
				"fin de set_current_player".debug;
				self.assign_midi;
				if(param_types.param_mode.includes(player.get_selected_param)) {
					"enable change_player_mode".debug;
					main.commands.enable_mode([\side, \change_player_mode]);
				} {
					"disable change_player_mode".debug;
					main.commands.disable_mode([\side, \change_player_mode]);
				};
				main.freeze_do { self.changed(\player); };
			}

		},

		get_current_player: { arg self;
			// return player object
			self.current_player 
		},

		remove_current_player: { arg self;
			var group = self.get_current_group;
			var player = self.get_current_player;
			var index;
			var name;
			name = player.uname;
			if(name != \voidplayer) {
				index = group.children.detectIndex { arg i; i == name };
				group.set_children_name(index, \voidplayer);
			} {
				"can't remove voidplayer".debug;
			};
		},

		///// group

		select_group_item: { arg self, index;
			var player, oldplayer;
			player = self.get_current_group.get_children_and_void[index];
			if(player.notNil) {
				self.set_current_player(player, index);
			} {
				"ERROR: side: select_group_item: selected player is nil"
			}
		},

		set_current_group: { arg self, group;
			var group_types = [\parnode, \seqnode]; // TODO: define it more globally. add seqnode
			var curplayer, curplayer_index;
			group.uname.debug("side.set_current_group: group");
			if(group_types.includes(group.kind)) {
				if(group != self.model.current_group) {
					self.current_group = group;
					self.current_group.selected_child.debug("side:set_current_group: selected_child");
					curplayer_index = self.current_group.selected_child_index;
					curplayer = self.current_group.get_children_and_void[ curplayer_index ];
					self.set_current_player(curplayer, curplayer_index);
					main.freeze_do { self.changed(\nodegroup); };
				};
			} {
				group.kind.debug("Error: node is not a kind of group");
			};
		},

		get_current_group: { arg self; self.current_group },


		////// supergroup

		refresh_group: { arg self;
			self.set_current_group(self.song_manager.get_current_group);
		},

		get_part_name: { arg self;
			self.song_manager.get_selected_part.name;	
		},

		get_section_name: { arg self;
			self.song_manager.get_selected_section.name;	
		},

		select_group: { arg self, index;
			var supergroup;
			var group;
			supergroup = self.get_current_supergroup;
			if(index == 0) {
				self.set_current_group(supergroup);
			} {
				group = supergroup.get_children[index-1];
				if(group.notNil) {
					self.set_current_group(group);
				} {
					index.debug("side: select group: no group here");
				}
			}
		},

		copy_group: { arg self, index;
			//TODO
			var curgroup = self.get_current_group;
			var supergroup = self.get_current_supergroup;
			var newgroup = supergroup.children[index-1];
			if((newgroup == \voidplayer) or: {main.get_node(newgroup).get_children_and_void.size==1}) {
				curgroup.debug("side.copy_group: curgroup");
				newgroup = main.node_manager.duplicate_node_recursive(curgroup.uname, (\cpar ++ index).asSymbol);
				supergroup.set_children_name(index-1, newgroup);
			} {
				index.debug("side.copy_group: groupnode already exist");
			}

		},

		set_current_supergroup: { arg self, group;
			self.current_supergroup = group;
			self.changed(\supergroup);
		},

		get_current_supergroup: { arg self;
			self.current_supergroup;
		},

		/////////// macro

		add_sample_batch: { arg self;
			var player = self.get_current_player;	

		
		},


		//////////

		refresh: { arg self;
			self.changed(\nodegroup);
			self.changed(\player);
			self.changed(\edit_mode);
			"FIN side_panel:refresh"
		},

		add_cell_bar: { arg self;
			var bar_length = 4;
			var dur;
			var par = self.get_selected_param;
			var defval = self.get_selected_param.default_val;

			self.get_selected_param.debug("editplayer.controller.add_cell_bar get sel param");

			switch(par.classtype,
				\stepline, {
					self.get_selected_param.add_cells( par.default_val[..(bar_length-1)]);
				},
				\noteline, {
				//	dur = player.get_arg(\dur).preset;
				//	par.set_end_offset(par.get_end_offset+dur.val[dur.selected_cell]);
				//	par.get_end_offset.debug("set end offset");
				},
				\control, {
					self.get_selected_param.add_cells( par.default_val ! bar_length);
				}
			);
		},

		remove_cell_bar: { arg self;
			var bar_length = 4, dur;
			var par = self.get_selected_param;
			switch(par.classtype,
				\stepline, {
					self.get_selected_param.remove_cells(bar_length);
				},
				\noteline, {
				//	dur = player.get_arg(\dur).preset;
				//	par.set_end_offset(par.get_end_offset-dur.val[dur.selected_cell]);
				},
				\control, {
					self.get_selected_param.remove_cells(bar_length);
				}
			);
		},


		make_gui: { arg self;
			self.window = ~make_side_view.(main, self);
			//self.window = ~make_side_view_BACKUP.(main, self);
			//self.window = ~make_nodegroup_view.(main, self);
		},

		init: { arg self;
			//self.model.selected_param = \amp;
			var supergroup;
			var current_group;

			self.timeline = ~class_timeline.new(main);
			8.do { arg i; self.timeline.add_track("Track "++i);};

			self.song_manager = ~make_song_manager.(main);
			current_group = self.song_manager.get_current_group;

			main.model.current_panel = \side;
			main.midi_center.install_pad_responders;
			main.get_node(\voidplayer).debug("VOIDPLAYUER");

			//~panel.set_current_player(~seq.get_node(\ko));

			//supergroup = main.node_manager.make_groupplayer(\superseq1, \seq);
			//supergroup.set_expset_mode(true);

			//8.do { arg i;
			//	var parname = (\par ++ (i + 1)).asSymbol;
			//	main.node_manager.make_groupplayer(parname, \par);
			//	main.get_node(\superseq1).add_children(parname);
			//};

			main.node_manager.set_default_group(current_group.uname);
			self.set_current_group(current_group);
			//self.set_current_supergroup(main.get_node(\superseq1));

			"OU SUOSJE".debug;

			main.commands.parse_action_bindings(\side, [


				[\open_timeline, {
					self.timeline.make_gui;
				}],

				///////// macro

				[\add_sample_batch, {
					self.add_sample_batch;
				}],

				//////////////

				[\select_param, 32, { arg i;
					self.select_param(i)
				}],

				[\pad_select_param, 8, { arg i;
					self.select_param(i+self.model.select_offset)
				}],

				[\select_param_cell, 8, { arg i;
					self.select_cell(i);
				}],

				[\matrix_select_param_cell, 32, { arg i;
					self.select_cell(i, \stepline);
				}],

				[\select_player, 10, { arg i;
					self.select_group_item(i-1)
				}],

				[\matrix_select_player, 8, { arg i;
					self.select_group_item(i)
				}],

				[\pad_select_player, 8, { arg i;
					self.select_group_item(i)
				}],

				[\select_group, 10, { arg i;
					self.select_group(i)
				}],

				[\select_variant, 10, { arg i;
					self.set_current_group(self.song_manager.change_variant(i));
				}],

				[\select_section, 10, { arg i;
					self.set_current_group(self.song_manager.change_section(i));
				}],

				[\select_part, 10, { arg i;
					self.set_current_group(self.song_manager.change_part(i));
				}],

				[\increase_select_offset, {
					self.model.select_offset = (self.model.select_offset + 8).clip(0,32)
					//FIXME: hardcoded limit
				}],

				[\decrease_select_offset, {
					self.model.select_offset = (self.model.select_offset - 8).clip(0,32)
					//FIXME: hardcoded limit
				}],

				[\increase_samplekit_part, {
					main.samplekit_manager.set_samplekit_part( (main.samplekit_manager.get_samplekit_part + 1).clip(0,3) );
					//FIXME: hardcoded limit
				}],

				[\decrease_samplekit_part, {
					main.samplekit_manager.set_samplekit_part( (main.samplekit_manager.get_samplekit_part - 1).clip(0,3) )
					//FIXME: hardcoded limit
				}],


				[\play_group, {
					self.get_current_group.play_node;
				}], 

				[\stop_group, {
					self.get_current_group.stop_node;
				}], 

				[\play_selected, {
					self.song_manager.update_expset(self.get_current_group.selected_child_index);
					self.get_current_player.play_node;
				}], 

				[\stop_selected, {
					self.get_current_player.stop_node;
				}],

				[\stop_selected_quant, {
					self.get_current_player.stop_node(true);
				}],

				[\mute_selected, {
					self.get_current_player.mute;
				}],

				[\unmute_selected, {
					self.get_current_player.mute(false);
				}],

				[\solo_selected, {
					main.play_manager.solo_node(self.get_current_player.uname);
				}],

				[\unsolo_selected, {
					main.play_manager.unsolo_node(self.get_current_player.uname);
				}],

				[\toggle_solo_selected, {
					var uname = self.get_current_player.uname;
					if(main.play_manager.is_in_solo_mode) {
						main.play_manager.unsolo_node(uname);
					} {
						main.play_manager.solo_node(uname);
					}
				}],

				[\toggle_player_recording, {
					var player;
					"toggle_player_recording".debug;
					player = self.get_current_player;
					player.name.debug("toggle_player_recording: player!!");
					if(player.notNil) {
						main.node_manager.toggle_recording(player);
					}
				}],

				[\toggle_param_recording, { 
					if(main.play_manager.is_recording.not) {
						main.node_manager.start_cc_recorder(self.get_current_player);
					} {
						main.node_manager.cancel_cc_recording;
					}
				}],

				[\panic, {
					main.panic
				}],

				[\toggle_metronome, {
					if(main.play_manager.use_metronome == false) {
						main.play_manager.use_metronome = true;
					} {
						main.play_manager.use_metronome = false;
					}
				}],

				[\edit_tempo, {
					~make_tempo_edit_view.(main, [\knob, 0]);
				}],

				[\edit_quant, {
					~make_quant_edit_view.(main, [\knob, 0]);
				}],

				[\edit_barrecord, {
					~make_barrecord_edit_view.(main, [\knob, 0]);
				}],

				[\edit_wrapper, {
					var player = self.get_current_player;
					player.edit_wrapper;
				}],

				[\forward_in_record_history, {
					var player = self.get_current_player;
					var nline;
					if(player.is_audiotrack) {
						nline = player.get_arg(\bufnum);
						nline.forward_in_record_history;
					} {
						if(player.get_mode != \stepline) {
							nline = player.get_arg(player.get_mode);
							nline.scoreset.forward_in_history;
							if(main.play_manager.node_is_playing(player).not) {
								nline.forward_to_next_notescore;
							}
						};
					}
				}],

				[\backward_in_record_history, {
					var player = self.get_current_player;
					var nline;
					if(player.is_audiotrack) {
						nline = player.get_arg(\bufnum);
						nline.backward_in_record_history;
					} {
						if(player.get_mode != \stepline) {
							nline = player.get_arg(player.get_mode);
							nline.scoreset.backward_in_history;
							if(main.play_manager.node_is_playing(player).not) {
								nline.forward_to_next_notescore;
							}
						};
					}
				}],

				[\edit_selected_param, {
					self.edit_selected_param;
				}],

				///// new node

				[\remove_node, { 
					self.remove_current_player;
				}],

				[\cut_node, {
					var player = self.get_current_player;
					main.node_manager.cut_node(player);
					self.remove_current_player;
				}],

				[\copy_node, {
					var player = self.get_current_player;
					main.node_manager.copy_node(player);
				}],

				[\copy_group, 10, { arg i;
					self.copy_group(i);
				}],

				[\paste_node, {
					var player = self.get_current_player;
					var group = self.get_current_group;
					var nodename;
					nodename = main.node_manager.paste_node(player);
					if(nodename.notNil) {
						group.set_selected_child(nodename);
						self.set_current_player(main.get_node(nodename));
					};
				}],

				[\load_node_from_lib, {
					var group = self.get_current_group;
					main.node_manager.load_libnode { arg nodename;
						group.set_selected_child(nodename);
						self.set_current_player(main.get_node(nodename));
					};
				}],

				[\create_default_node, {
					var nodename;
					var group = self.get_current_group;
					nodename = main.node_manager.create_default_livenode;
					group.set_selected_child(nodename);
					self.set_current_player(main.get_node(nodename));
				}],

				[\reload_player, {
					var player = self.get_current_player;
					var newplayer;
					newplayer = main.node_manager.reload_player(player);
					self.set_current_player(newplayer);

				}],

				[\load_colpreset, {
					var player = self.get_current_player;
					main.node_manager.load_column_preset(player);
				}],

				[\save_colpreset, {
					var player = self.get_current_player;
					main.node_manager.save_column_preset(player);
				}],

				///// eventline

				[\set_notequant, { 
					var delta;
					var param;
					var player;
					player = self.get_current_player;
					if([\sampleline, \noteline, \nodeline].includes(player.get_mode)) {
						param = player.get_arg(player.get_mode);
						delta = player.get_arg(\dur).get_val;
						if(param.get_notequant.isNil) {
							param.set_notequant(delta)
						} {
							param.set_notequant(nil)
						}
					}
				}],

				///// cells

				[\add_cell_bar, {
					self.add_cell_bar.() 
				}],
				[\remove_cell_bar, {
					self.remove_cell_bar.() 
				}],

				///// effects

				[\add_effect, {
					var player = self.get_current_player;
					main.node_manager.load_effectnode(player);
					self.changed(\paramlist);
				}],

				///// global modes

				["set_edit_mode.matrix", {
					"matrix".debug("mode");
					self.set_edit_mode(\matrix);
					//main.commands.disable([\side, \select_player]);
					main.commands.disable([\side, \select_param_cell]);
					main.commands.disable([\side, \select_param]);
					main.commands.disable([\side, \pad_select_param]);

					//main.commands.enable([\side, \matrix_select_player]);
					main.commands.enable([\side, \matrix_select_param_cell]);
				}],

				["set_edit_mode.params", {
					"matrix".debug("mode");
					self.set_edit_mode(\params);
					//main.commands.disable([\side, \select_player]);
					main.commands.enable([\side, \select_param_cell]);
					main.commands.enable([\side, \select_param]);
					main.commands.enable([\side, \pad_select_param]);

					//main.commands.enable([\side, \matrix_select_player]);
					main.commands.disable([\side, \matrix_select_param_cell]);
				}],

				["set_global_mode.liveplay", {
					"liveplay".debug("mode");
					self.set_current_mode(\liveplay);
					main.node_manager.start_midi_liveplayer(self.get_current_player);
					self.assign_midi;
					//main.commands.disable([\side, 
				}],
				["set_global_mode.param", {
					"param".debug("mode");
					self.set_current_mode(\param);
					main.node_manager.stop_midi_liveplayer;
					//main.commands.disable([\side, \select_player]);
					main.commands.disable([\side, \pad_select_player]);
					//main.commands.disable([\side, \matrix_select_player]);
					main.commands.disable([\side, \matrix_select_param_cell]);

					main.commands.enable([\side, \select_param]);
					main.commands.enable([\side, \pad_select_param]);
					main.commands.enable([\side, \select_param_cell]);
					self.assign_midi;
				}],
				["set_global_mode.group", {
					"group".debug("mode");
					self.set_current_mode(\group);
					main.node_manager.stop_midi_liveplayer;
					//main.commands.disable([\side, \select_param]);
					main.commands.disable([\side, \pad_select_param]);
					//main.commands.disable([\side, \matrix_select_player]);

					main.commands.enable([\side, \matrix_select_param_cell]);
					main.commands.enable([\side, \select_player]);
					main.commands.enable([\side, \pad_select_player]);
					//main.commands.enable([\side, \select_param_cell]);
					self.assign_midi;
				}],

				["set_global_mode.mixer", {
					"group".debug("mode");
					self.set_current_mode(\mixer);
					main.node_manager.stop_midi_liveplayer;
					main.commands.disable([\side, \select_param]);
					main.commands.disable([\side, \pad_select_param]);
					main.commands.disable([\side, \matrix_select_player]);
					main.commands.disable([\side, \matrix_select_param_cell]);

					main.commands.enable([\side, \select_player]);
					main.commands.enable([\side, \pad_select_player]);
					main.commands.enable([\side, \select_param_cell]);
					self.assign_midi;
				}],
			]);

			main.commands.parse_action_bindings(\side, 
				[\scalar, \seq, \seg, \bus, \recordbus, \pkey].collect { arg kind;
					["change_param_kind."++kind.asString, {
						self.change_param_kind(kind);
					}]
				}
			);

			main.commands.parse_action_bindings(\side, 
				[\stepline, \sampleline, \noteline].collect { arg kind;
					["change_player_mode."++kind.asString, {
						self.get_current_player.set_mode(kind);
					}, \disabled]
				}
			);

			main.commands.copy_action_list(\side, \midi, [
				\play_selected,
				\stop_selected,
				\stop_selected_quant,
				\mute_selected,
				\play_group,
				\stop_group,
				"set_global_mode.liveplay",
				"set_global_mode.param",
				"set_global_mode.group",
				"set_global_mode.mixer",
			]);

			main.commands.actions.at(*[\side, \set_global_mode, \param]).();
		}

	);
	side.init;
	side;
}

)
