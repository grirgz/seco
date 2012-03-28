(


~make_extparam_view = { arg main, controller, parent, player, param;
	var param_name = param.name;
	var info_layout = parent;
	var row_layout = parent;
	case
		{ [\adsr].includes(param_name) || param_name.asString.containsStringAt(0,"adsr_") } {
			~make_env_control_view.(row_layout, player, controller.make_param_display(param), param);
		}
		{ param_name == \noteline } {
			if(player.current_mode == \noteline, {
				//TODO: do it in controller.assign_midi
				//midi = ~piano_recorder.(player);
				//param.midi = midi;
				~make_noteline_view.(row_layout, controller.make_param_display(param), param);
			});
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
		{ [\stepline].includes(param_name) } {
			if(player.current_mode == \stepline, {
				~make_control_view.(row_layout, controller.make_param_display(param), param);
			});
		}
		{ [\sampleline].includes(param_name) } {
			if(player.current_mode == \sampleline, {
				~make_noteline_view.(row_layout, controller.make_param_display(param), param);
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

~make_side_view = { arg main, controller;

	var win, vlayout, hlayout;
	var winsize= 700@800;
	var paramsize= 170@26;
	var sep;
	var paramview_list = List.new;
	var extparamview_list = List.new;
	var groupview_list = List.new;
	var extparam_layout, inner_extparam_layout;
	var player_responder;
	var group_responder;

	win = Window.new(bounds:Rect(750,0,winsize.x,winsize.y));
	win.view.keyDownAction = main.commands.get_kb_responder(\side);

	vlayout = VLayoutView.new(win, Rect(3,0,winsize.x-13,winsize.y));
	///// status view

	~make_status_view_horizontal.(vlayout, main.play_manager, winsize.x@20);

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

	extparam_layout = VLayoutView.new(vlayout, Rect(0,0,winsize.x,(paramsize.y+11)*8));
	inner_extparam_layout = VLayoutView.new(extparam_layout);

	///// groupnode view

	sep = StaticText.new(vlayout,Rect(0,0,10,08));
	sep.background = Color.newHex("54516A");
	sep.background = Color.black;
	sep = StaticText.new(vlayout,Rect(0,0,10,3));

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
			//player.get_ordered_args.do { arg param_name, idx;


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
			args.do { arg param_name, idx;
				param = player.get_arg(param_name);
				paramview = ~make_extparam_view.(main, self, inner_extparam_layout, player, param);
				extparamview_list.add(paramview);
			};

		},

		group: { arg self, msg;
			var group;
			"sideview_responder: group".debug;
			group = self.get_current_group;
			group_responder.remove;
			group_responder = ~make_view_responder.(vlayout, group, (
				redraw: { arg self;
					"group_responder: redraw".debug;
					controller.changed(\group_items);
				},

				selected_child: { arg self, msg, idx;
					if(idx == self.selected_child_index) {
						groupview_list[idx].selected(true);
					} {
						groupview_list[idx].selected(false);
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
			children[..7].do { arg child, x; // FIXME: hardcoded
				var amp = child.get_arg(\amp);
				if( amp.isNil ) {
					"group_items: amp is nil".debug;
					amp = ~make_empty_param.(self.get_main);
				};
				display = controller.make_param_display(amp, child);
				display.set_parent_group(group);
				groupview_list[x].set_group_param(child, amp, display);
			};
			"FIN sideview_responder".debug;


		}



	));


	vlayout.focus;
	win.front;

};

~make_mini_param_view = { arg parent, size;
	var vlayout, hlayout;
	var bt_name, txt_midi_label, kind, txt_midi_val, paramval, slider;
	var bsize = 23;
	var font;
	var param_view, param_responder;
	var paramval_size = Rect(0,0,(size.x-(bsize*2))/2,size.y/2);
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
				self.name.debug("je suis select");
				bt_name.debug("bt_name");
				bt_name.background = Color.newHex("B4B1BA");
			} {
				self.name.debug("je suis DEselect");
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

			self.responder = ~make_view_responder.(vlayout, param, resp);
		}

	)


};

~make_node_manager = { arg main;
	(
		model: { main.model },

		default_newnode: [\libnode, \pulsepass],

		get_node: main[\get_node],
		node_exists: main[\node_exists],
		make_livenodename_from_libnodename: main[\make_livenodename_from_libnodename],

		find_free_name: main[\find_free_name],
		make_newlivenodename_from_livenodename: main[\make_newlivenodename_from_livenodename],
		make_livenode_from_libnode: main[\make_livenode_from_libnode],
		duplicate_livenode: main[\duplicate_livenode],

		make_groupplayer: { arg self, name, kind=\par;
			var pl;
			if(kind == \par) {
				pl = ~make_parplayer.(main);
			} {
				pl = ~make_seqplayer.(main);
			};
			pl.name = name;
			pl.uname = name;
			main.add_node(pl);
			pl;
		},

		start_tempo_recorder: { arg self, player, kind=\limited;
			// TODO: handle when recording finish (play recorded track along what is playing ?)
			var finish = {
				main.play_manager.set_recording(false);
			};
			kind = \unlimited; // DEBUG;
			if(main.play_manager.is_recording.not) {
				case
					{ player.kind == \player && (player.name != \voidplayer) } {
						main.play_manager.set_recording(true);
						player.name.debug("start_tempo_recorder: player");
						if(player.is_audiotrack) {
							"start_tempo_recorder:audio recorder player!!".debug;
							self.recorder = ~make_audio_recorder.(player, main);
							self.recorder.player_start_tempo_recording(finish);
						} {
							if(player.get_mode == \stepline) {
								if(player.get_arg(\sampleline).notNil) {
									player.set_mode(\sampleline);
								} {
									player.set_mode(\noteline); 
								}
							};
							self.recorder = ~make_midi_recorder.(player, main);
							self.recorder.player_start_tempo_recording(finish, kind);
						}
					}
					{ player.subkind == \nodesampler } {
						main.play_manager.set_recording(true);
						player.name.debug("start_tempo_recorder: nodesampler");
						self.recorder = ~make_midi_recorder.(player, main);
						self.recorder.player_start_tempo_recording(finish, kind);
					}
					{
						"INFO: grouplive: start_tempo_recorder: selected cell is not a player".inform;
					}
			} {
				"hmatrix: already recording".debug;
			};
		},

		cancel_recording: { arg self;
			if(main.play_manager.is_recording == true) {
				main.play_manager.set_recording(false);
				self.recorder.cancel_recording; // FIXME: bug with self.recording being nil
			} {
				"hmatrix: not recording".debug;
			};
		},

		toggle_recording: { arg self, player;
			if(main.play_manager.is_recording == true) {
				//self.cancel_recording;
				self.recorder.stop_unlimited_recording;
			} {
				self.start_tempo_recorder(player);
			}
		},

		start_cc_recorder: { arg self, player;
			// TODO: handle when recording finish (play recorded track along what is playing ?)
			var finish = {
				main.play_manager.set_recording(false);
				//main.play_manager.keep_recording_session = true; // to play in sync with recording clock (see play_manager.start_new_session)
				//player.play_node;
			};
			if(main.play_manager.is_recording.not) {
				if(player.kind == \player && (player.name != \voidplayer)) {
					main.play_manager.set_recording(true);
					player.name.debug("start_cc_recorder: player");
					self.cc_recorder = ~make_midi_cc_recorder.(player, main);
					self.cc_recorder.player_start_tempo_recording(finish);
				} {
					"INFO: grouplive: start_cc_recorder: selected cell is not a player".inform;
				}
			} {
				"node_manager: already cc recording".debug;
			};
		},

		cancel_cc_recording: { arg self;
			if(main.play_manager.is_recording == true) {
				self.cc_recorder.cancel_recording;
				main.play_manager.set_recording(false);
			} {
				"node_manager: not cc recording".debug;
			};
		},

		start_midi_liveplayer: { arg self, player;
			if(player.kind == \player && (player.name != \voidplayer)) {
				if(player.is_audiotrack) {
					"start_midi_liveplayer:not midi".debug;
				} {
					self.midi_liveplayer = ~make_midi_liveplayer.(player, main);
					self.midi_liveplayer.start_liveplay;
				}
			} {
				"INFO: grouplive: start_midi_liveplayer: selected cell is not a player".inform;
			}
		},

		stop_midi_liveplayer: { arg self;
			if(self.midi_liveplayer.notNil) {
				self.midi_liveplayer.stop_liveplay;
				main.midi_center.install_pad_responders; // FIXME: find a way to not have to call this
			} {
				"Midi liveplayer not started: can't stop it".debug;
			}
		},

		set_default_group: { arg self, node_name;
			var group;
			group = main.get_node(node_name);
			group.name.debug("set_default_group");
			if(group.notNil) {
				self.default_group = group;
			}
		},

		add_node_to_default_group: { arg self, node;
			self.default_group.add_children(node.name);

		},

		reload_player: { arg self, player;
			var newplayer = player.clone;
			newplayer.uname = player.uname;
			newplayer.name = player.name;
			main.add_node(newplayer);
			newplayer;
		},

		duplicate_node: { arg self, nodename;
			var name, pl, num;
			pl = self.get_node(nodename).clone;
			pl.name = name;
			pl.uname = name;
			//TODO: clone children
			main.add_node(pl);
			pl.uname;
		},

		load_libnode: { arg self, action;
			~choose_libnode.(main, { arg libnodename, livenodename; 
				var nodename;
				"load_libnode: first func".debug;
				self.default_newnode = [\libnode, libnodename];
				nodename = main.make_livenode_from_libnode(libnodename);
				action.(nodename)

			}, { arg livenodename;
				var nodename;
				self.default_newnode = [\livenode, livenodename];
				nodename = main.duplicate_livenode(livenodename);
				action.(nodename)
			});
		},

		create_default_livenode: { arg self;
			var livenodename;
			switch(self.default_newnode[0],
				\libnode, {
					livenodename = main.make_livenode_from_libnode(self.default_newnode[1]);
				},
				\livenode, {
					livenodename = main.duplicate_livenode(self.default_newnode[1]);
				}
			);
			livenodename;
		},

		save_column_preset: { arg self, player;
			//TODO: handle synth args change
			var datalist;
			datalist = main.model.colpresetlib[player.defname];
			if( datalist.isNil ) {
				main.model.colpresetlib[player.defname] = SparseArray.newClear(4*8*10, \empty);
				datalist = main.model.colpresetlib[player.defname];
			};
			datalist = datalist.collect { arg d; 
				if( d == \empty ) { d } { d.name }
			};
			~save_player_column.(main, "SAVE column preset", player, datalist, { arg sel, offset;
				var name;
				if( sel == \empty ) {
					name = player.defname ++ "_c" ++ UniqueID.next;
				} {
					name = sel;
				};
				main.model.colpresetlib[player.defname][offset] = player.save_column_preset;
				main.model.colpresetlib[player.defname][offset].name = name;
				main.save_presetlib;
			}, { arg offset, newname;
				// rename action
				if(main.model.colpresetlib[player.defname][offset] != \empty) {
					main.model.colpresetlib[player.defname][offset].name = newname;
					main.save_presetlib;
				};
			});
		},

		load_column_preset: { arg self, player;
			var datalist;
			datalist = main.model.colpresetlib[player.defname];
			if( datalist.isNil ) {
				main.model.colpresetlib[player.defname] = SparseArray.newClear(4*8*10, \empty);
				datalist = main.model.colpresetlib[player.defname];
			};
			datalist = datalist.collect { arg d; 
				if( d == \empty ) { d } { d.name }
			};
			~save_player_column.(main, "LOAD column preset", player, datalist, { arg sel, offset;
				// load action
				if( sel == \empty ) {
					"load_column_preset: Can't load empty preset".error;
				} {
					player.load_column_preset(main.model.colpresetlib[player.defname][offset]);
				}
			}, { arg offset, newname;
				// rename action
				if(main.model.colpresetlib[player.defname][offset] != \empty) {
					main.model.colpresetlib[player.defname][offset].name = newname;
					main.save_presetlib;
				};
			});
		},

		make_fxnode_from_libnode: { arg self, libnodename;
			var livenodename;
			var player;
			livenodename = self.make_livenodename_from_libnodename(libnodename);
			player = ~make_player.(main, main.model.effectpool[libnodename]);
			player.debug("maked effect");
			player.name = livenodename;
			player.uname = livenodename;
			main.add_node(player);
			player.uname.debug("make_fxnode_from_libnode: player.uname");
		},

		duplicate_fxnode: { arg self, livenodename;
			//TODO
		},

		load_effectnode: { arg self, player, action;
			var livenode;
			~choose_effect.(main, { arg libnodename, livenodename; 
				//self.model.default_newnode = [\libnode, libnodename];
				livenodename = self.make_fxnode_from_libnode(libnodename);
				player.add_effect(livenodename);
				action.();
			}, { arg livenodename;
				//self.model.default_newnode = [\livenode, livenodename];
				livenodename = self.duplicate_fxnode(livenodename);
				livenodename.debug("actionpreset");
				player.add_effect(livenodename);
				action.();
			});
		},

		copy_node: { arg self, node;
			var uname, address;
			uname = node.uname;
			uname.debug("copied node1");
			if(uname == \void || (uname == \voidplayer)) {
				"Can't copy empty player".error;
			} {
				uname.debug("copied node2");
				main.model.clipboard = uname;
				main.model.clipboard.debug("copied node");
			};
		},

		paste_node: { arg self;
			var node;
			if( main.model.clipboard.isNil ) {
				"Can't paste: clipboard is empty".error;
			} {
				node = main.get_node(main.model.clipboard);
				if( node.kind == \player ) {
					self.duplicate_livenode(main.model.clipboard);
				} {
					"paste_node: paste groupnode: not implemented".debug;
					nil;
				};
			};

		},

		mpdef: { arg self, name, pat, instr=nil;
			var node;

			if(pat.notNil) {
					case
						{ pat.class == Pbind } {
							if(main.node_exists(name)) {
								node = main.get_node(name);
								node.set_input_pattern(pat);
								main.panels.side.set_current_player(node);
								Pdef(name, node.vpattern);
							} {
								node = ~make_player_from_pbind.(main, pat);
								if(node.notNil) {
									node.name = name;
									node.uname = name;
									main.add_node(node);
									main.node_manager.add_node_to_default_group(node);
									main.panels.side.set_current_player(node);
									//main.focus_mpdef(node);
									Pdef(name, node.vpattern);
								} {
									"ERROR: player could not be created".debug;
									nil;
								};
							}

						}
						{ pat.class == Pchain } {
							if(main.node_exists(name)) {
								node = main.get_node(name);
								node.set_input_pattern(pat);
								main.panels.side.set_current_player(node);
								Pdef(name, node.vpattern);
							} {
								node = ~make_player_from_pchain.(main, pat, instr);
								if(node.notNil) {
									node.name = name;
									node.uname = name;
									main.add_node(node);
									main.node_manager.add_node_to_default_group(node);
									main.panels.side.set_current_player(node);
									//main.focus_mpdef(node);
									Pdef(name, node.vpattern);
								} {
									"ERROR: player could not be created".debug;
									nil;
								};
							}

						}
						{ pat.class == Ppar } {

							if(main.node_exists(name)) {
								node = main.get_node(name);
								node.set_input_pattern(pat);
								main.panels.side.set_current_group(node);
								Pdef(name, node.vpattern);
							} {
								node = ~make_parplayer.(main);
								if(node.notNil) {
									node.name = name;
									node.uname = name;
									main.add_node(node);
									node.set_input_pattern(pat);
									main.panels.side.set_current_group(node);
									//main.focus_mpdef(node);
									Pdef(name, node.vpattern);
								} {
									"ERROR: player could not be created".debug;
									nil;
								};
							}
						}
						{
							"ERROR: pat class not understood".debug;
							Pdef(name, pat);
						}
			} {
				Pdef(name)
			}
		},

		add_node: main[\add_node]
	)
};

~make_side_panel = { arg main;
	var side;
	var param_types;

	param_types = (
				param_field_group: List[\dur, \segdur, \stretchdur, \repeat],
				param_slider_group: List[\amp, \legato, \pan, \attack, \sustain, \release],
				param_status_group: List[\amp, \dur, \segdur, \stretchdur, \repeat, \bufnum, \samplekit],
				param_order: List[\sustain, \pan, \attack, \release, \adsr, \freq],
				param_reject: [\out, \instrument, \type, \gate, \agate, \t_trig],
				param_mode: [\stepline, \noteline, \sampleline]
	);
	param_types.param_no_midi = param_types.param_field_group ++ [\bufnum, \samplekit] ++ param_types.param_mode;
	param_types.param_reject = param_types.param_reject ++ param_types.param_mode;

	side = (
		archive_data: [\model],

		get_main: { arg self; main },

		model: (
			param_no_midi: param_types.param_no_midi,
			select_offset: 0,
			max_cells: 8,
			current_mode: \param,
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
					player.get_bank;
				},
				get_player_bank: { arg self;
					player.get_bank;
				},
				selected: { arg self;
					if(self.parent_group.notNil) {
						self.parent_group.name.debug("le fameux group");

						[player.name, self.parent_group.selected_child].debug("group: il s'interroge s'il est selectioné");
						//if( self.parent_group.selected_child == player.name ) { 1 } { 0 } // FIXME: name or uname ?
						0;
					} {
						[param.name, player.get_selected_param].debug("il s'interroge s'il est selectioné");
						if( player.get_selected_param == param.name ) { 1 } { 0 }
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

		get_paramlist_splited: { arg self;
			var player = self.current_player;
			var mode;
			var args;
			var args1;
			var args2;
			var args3;
			if(player.uname == \voidplayer) {
				[[], []]
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
				if(([\control, \adsr]++param_types.param_mode).includes(param.classtype)) { 
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
					//player.name.debug("assign_midi player.name");
					//offset.debug("assign_midi offset");
					//param_name.debug("param_name");
					param = player.get_arg(param_name);

					if(main.midi_center.is_next_free(kind).not) {
						kind = \slider;
					};

					case
						{ [\adsr].includes(param_name) || param_name.asString.containsStringAt(0,"adsr_") } {
							//TODO: working ?
							main.midi_center.assign_adsr(param)
						}
						{ \amp == param_name }
						{
							if(self.model.current_mode != \mixer) {
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
							if(offset <= 0) {
								main.midi_center.assign_first(kind, param);
								//[offset, param.name].debug("assign_midi assign param");
							} {
								offset = offset - 1;
								//offset.debug("assign_midi offset<");
							};
						};
				};
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
			//self.model.debug("c'est dingue!!!!");
			//oldsel = self.model.selected_param;
			pl = self.get_paramlist_splited;
			if(index < 8) {
				sel = pl[0][index];
			} {
				sel = pl[1][index-8];
			};
			if(sel.notNil) {
				player.select_param(sel);
			} {
				index.debug("no param to select here");
			};
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
		},

		select_cell: { arg self, idx;
			var sel, dur;
			var player = self.get_current_player;
			sel = self.get_selected_param;
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
					~choose_sample.(main, { arg buf; param.set_val(buf);  })
				},
				\samplekit, {

				}
			);
		},

		///// player

		set_current_player: { arg self, player, index;
			// set player object
			var oldplayer;
			if(self.current_player != player) {
				oldplayer = main.get_node(self.get_current_group.selected_child);
				if(index.notNil) {
					self.get_current_group.select_child_at(index);
				} {
					self.get_current_group.select_child(player.uname);
				};
				if(oldplayer.notNil) {
					oldplayer.get_arg(\amp).changed(\selected);
				};
				self.current_player = player;
				self.assign_midi;
				if(param_types.param_mode.includes(player.get_selected_param)) {
					"enable change_player_mode".debug;
					main.commands.enable_mode([\side, \change_player_mode]);
				} {
					"disable change_player_mode".debug;
					main.commands.disable_mode([\side, \change_player_mode]);
				};
				self.changed(\player);
			}

		},

		get_current_player: { arg self;
			// return player object
			self.current_player 
		},

		///// group

		select_group_item: { arg self, index;
			var player, oldplayer;
			player = self.get_current_group.get_children_and_void[index];
			if(player.notNil) {
				self.set_current_player(player, index);
			}
		},

		set_current_group: { arg self, group;
			if(group != self.model.current_group) {
				self.current_group = group;
				self.changed(\group);
			};
		},

		get_current_group: { arg self; self.current_group },

		refresh: { arg self;
			self.changed(\group);
			self.changed(\player);
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
			~make_side_view.(main, self);
		},

		init: { arg self;
			//self.model.selected_param = \amp;

			main.model.current_panel = \side;
			main.midi_center.install_pad_responders;
			main.get_node(\voidplayer).debug("VOIDPLAYUER");

			//~panel.set_current_player(~seq.get_node(\ko));

			main.node_manager.make_groupplayer(\par1, \par);
			main.node_manager.set_default_group(\par1);
			self.set_current_group(main.get_node(\par1));

			"OU SUOSJE".debug;

			main.commands.parse_action_bindings(\side, [
				[\select_param, 32, { arg i;
					self.select_param(i)
				}],

				[\pad_select_param, 8, { arg i;
					self.select_param(i+self.model.select_offset)
				}],

				[\select_param_cell, 8, { arg i;
					self.select_cell(i);
				}],

				[\select_player, 8, { arg i;
					self.select_group_item(i)
				}],

				[\pad_select_player, 8, { arg i;
					self.select_group_item(i)
				}],

				[\increase_select_offset, {
					self.model.select_offset = (self.model.select_offset + 8).clip(0,32)
					//FIXME: hardcoded limit
				}],

				[\decrease_select_offset, {
					self.model.select_offset = (self.model.select_offset - 8).clip(0,32)
					//FIXME: hardcoded limit
				}],

				[\play_group, {
					self.get_current_group.play_node;
				}], 

				[\stop_group, {
					self.get_current_group.stop_node;
				}], 

				[\play_selected, {
					self.get_current_player.play_node;
				}], 

				[\stop_selected, {
					self.get_current_player.stop_node;
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
					var nline = if(player.get_mode == \sampleline) { player.get_arg(\sampleline) } { player.get_arg(\noteline) };
					nline.scoreset.forward_in_history;
					if(main.play_manager.node_is_playing(player).not) {
						nline.forward_to_next_notescore;
					}
				}],

				[\backward_in_record_history, {
					var player = self.get_current_player;
					var nline = if(player.get_mode == \sampleline) { player.get_arg(\sampleline) } { player.get_arg(\noteline) };
					nline.scoreset.backward_in_history;
					if(main.play_manager.node_is_playing(player).not) {
						nline.forward_to_next_notescore;
					}
				}],

				[\edit_selected_param, {
					self.edit_selected_param;
				}],

				///// new node

				[\copy_node, {
					var player = self.get_current_player;
					main.node_manager.copy_node(player);
				}],

				[\paste_node, {
					var player = self.get_current_player;
					var group = self.get_current_group;
					var nodename;
					nodename = main.node_manager.paste_node(player);
					group.set_selected_child(nodename);
					self.set_current_player(main.get_node(nodename));
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
					if([\sampleline, \noteline].includes(player.get_mode)) {
						param = player.get_arg(player.get_mode);
						delta = player.get_arg(\dur).get_val;
						if(param.notequant.isNil) {
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

				["set_global_mode.liveplay", {
					"liveplay".debug("mode");
					self.model.current_mode = \liveplay;
					main.node_manager.start_midi_liveplayer(self.get_current_player);
					self.assign_midi;
					//main.commands.disable([\side, 
				}],
				["set_global_mode.param", {
					"param".debug("mode");
					self.model.current_mode = \param;
					main.node_manager.stop_midi_liveplayer;
					main.commands.disable([\side, \select_player]);
					main.commands.disable([\side, \pad_select_player]);
					main.commands.enable([\side, \select_param]);
					main.commands.enable([\side, \pad_select_param]);
					self.assign_midi;
				}],
				["set_global_mode.group", {
					"group".debug("mode");
					self.model.current_mode = \group;
					main.node_manager.stop_midi_liveplayer;
					main.commands.disable([\side, \select_param]);
					main.commands.disable([\side, \pad_select_param]);
					main.commands.enable([\side, \select_player]);
					main.commands.enable([\side, \pad_select_player]);
					self.assign_midi;
				}],

				["set_global_mode.mixer", {
					"group".debug("mode");
					self.model.current_mode = \mixer;
					main.node_manager.stop_midi_liveplayer;
					main.commands.disable([\side, \select_param]);
					main.commands.disable([\side, \pad_select_param]);
					main.commands.enable([\side, \select_player]);
					main.commands.enable([\side, \pad_select_player]);
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
				\mute_selected,
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
