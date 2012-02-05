(

~set_numpad_mode = { arg commands, fun;
	var buf = "", restorefun;

	restorefun = commands.overload_mode([\editplayer, \edit_value_mode]);

	commands.array_set_action([\editplayer, \edit_value_mode, \insert_number], 10, { arg i;
		[buf, i, i.asString].debug("ibuf");
		buf = buf ++ i.asString;
		[buf, i, i.asString].debug("buf");
	});
	commands.set_action([\editplayer, \edit_value_mode, \insert_point], { buf = buf ++ "." });
	commands.set_action([\editplayer, \edit_value_mode, \cancel], { restorefun.() });
	commands.set_action([\editplayer, \edit_value_mode, \ok], { restorefun.(); [buf, i, i.asString].debug("ok buf"); fun.(buf) });


};




/////////////////////////////////////////////////////////////////////////
/////////		main
/////////////////////////////////////////////////////////////////////////

~make_editplayer_view = { arg parent, main, editplayer, player, param_order;
	var midi;
	var width = 1300;
	var height = 1300;
	var row_layout, col_layout, info_layout;
	var sc_ep, ep_messages, sc_player, player_messages;
	var status;

	col_layout = GUI.hLayoutView.new(parent, Rect(0,0,width+10,height));
	info_layout = GUI.vLayoutView.new(col_layout, Rect(0,0,300,height));
	row_layout = GUI.vLayoutView.new(col_layout, Rect(0,0,width-200,height));
	row_layout.background = ~editplayer_color_scheme.background;

	debug("DEBIN make_editplayer_view");

	ep_messages = Dictionary.newFrom((
		paramlist: { arg self;
			
			//~midi_interface.clear_assigned(\slider);
			//~midi_interface.clear_assigned(\knob);

			///CCResponder.removeAll; // FIXME: must not remove other useful CC

			info_layout.removeAll;
			row_layout.removeAll;

			editplayer.get_paramlist.debug("BEGIN paramlist update");
			editplayer.get_paramlist.do { arg param_name, i;
				var param = player.get_arg(param_name);
				param.debug("make_editplayer_view, param");
				param_name.debug("creation");
				case
					{ [\adsr].includes(param_name) || param_name.asString.containsStringAt(0,"adsr_") } {
						~make_env_control_view.(row_layout, player, editplayer.make_param_display(param), param);
					}
					{ param_name == \noteline } {
						if(player.current_mode == \noteline, {
							//TODO: do it in editplayer.assign_midi
							//midi = ~piano_recorder.(player);
							//param.midi = midi;
							~make_noteline_view.(row_layout, editplayer.make_param_display(param), param);
						});
					}
					{ [\repeat].includes(param_name) } {
						~make_simple_control_view.(info_layout, editplayer.make_param_display(param), param);
					}
					{ [\samplekit].includes(param_name) } {
						~make_string_param_view.(info_layout, editplayer.make_param_display(param), param);
					}
					{ [\dur].includes(param_name) } {
						//if(player.noteline.not, {
							~make_simple_control_view.(info_layout, editplayer.make_param_display(param), param);
						//})
					}
					{ [\bufnum].includes(param_name)|| param_name.asString.containsStringAt(0,"bufnum_") } {
						~make_bufnum_view.(info_layout, editplayer.make_param_display(param), param);
					}
					{ [\segdur, \stretchdur].includes(param_name) } {
						//if(player.noteline, {
							~make_simple_control_view.(info_layout, editplayer.make_param_display(param), param);
						//});
					}
					{ [\stepline].includes(param_name) } {
						if(player.current_mode == \stepline, {
							~make_control_view.(row_layout, editplayer.make_param_display(param), param);
						});
					}
					{ [\sampleline].includes(param_name) } {
						if(player.current_mode == \sampleline, {
							~make_noteline_view.(row_layout, editplayer.make_param_display(param), param);
						});
					}
					{ [\legato, \amp, \pan, \attack, \release, \sustain].includes(param_name)} {
						~make_control_view.(info_layout, editplayer.make_param_display(param), param);
					}
					{ true } {
						"standard param".debug;
						~make_control_view.(row_layout, editplayer.make_param_display(param).debug("il a quoi le display"), param);
					};
			};

			status = ~make_status_view.(info_layout, main.play_manager);

		}
	));

	player_messages = Dictionary.newFrom((
		mode: ep_messages[\paramlist]
	));

	sc_ep = SimpleController(editplayer);
	sc_player = SimpleController(player);
	~init_controller.(sc_ep, ep_messages);
	~init_controller.(sc_player, player_messages);
	debug("pourquoi il searreteee");
	editplayer.refresh;
	debug("2pourquoi il searreteee");

	parent.onClose = parent.onClose.addFunc {
		sc_ep.remove;
		sc_player.remove;
	};

};

~make_editplayer = { arg main, parent;
	var ep, player, param_types;
	"============making editplayer".debug;
	player = main.context.get_selected_node;
	//player.debug("editplayer:player");
	param_types = (
				param_field_group: List[\dur, \segdur, \stretchdur, \repeat],
				param_slider_group: List[\amp, \legato, \pan, \attack, \sustain, \release],
				param_status_group: List[\amp, \dur, \segdur, \stretchdur, \repeat, \legato, \pan, \attack, \sustain, \release, \bufnum, \samplekit],
				param_order: List[\stepline, \noteline, \sampleline, \adsr, \freq],
				param_reject: [\out, \instrument, \type, \gate, \agate, \t_trig]
	);
	param_types.param_no_midi = param_types.param_field_group ++ [\bufnum, \samplekit];

	ep = (
		player: player,
		param_types: param_types,
		model: (
				param_field_group: param_types.param_field_group,
				param_status_group: param_types.param_status_group,
				param_order: param_types.param_order,
				param_reject: param_types.param_reject,
				param_no_midi: param_types.param_no_midi,
				max_cells: 8,
				colselect_mode: true, // select whole column mode
				midi_knob_offset: 0,
				selected_param: 0

		),

		make_param_display: { arg editplayer, param;
			(
				get_bank: { arg self;
					player.get_bank;
				},
				get_player_bank: { arg self;
					player.get_bank;
				},
				selected: { arg self;
					[param.name, player.get_selected_param].debug("il s'interroge s'il est selectioné");
					if( player.get_selected_param == param.name ) { 1 } { 0 }
				},
				max_cells: { arg self;
					ep.model.max_cells;	
				},
				get_selected_cell: {
					param.get_selected_cell;
				},
				name: { arg self;
					"chu dans name".debug;
					param.name;
				},
				background_color: ~editplayer_color_scheme.control,
				show_midibloc: true,
				width: 200,
				height: 30,
				name_width: { arg self;
					50;
				}
			);
		},

		refresh: { arg self;
			"BEGIN REFRESH".debug;
			self.changed(\paramlist);
			self.assign_midi;
			main.set_window_title("editplayer: "++ player.name ++": bank" + player.get_bank);
			main.play_manager.refresh;
		},

		set_bank: { arg self, bank;
			player.set_bank(bank);
			main.set_window_title("editplayer: "++ player.name ++": bank" + player.get_bank);
		},

		get_paramlist: { arg self;
			var po = self.model.param_order;
			var res = List.new;
			res.addAll(
				if( player.kind == \player ) {
					po = switch(player.current_mode,
						\sampleline, { po.reject({ arg x; [\noteline, \stepline].includes(x) }); },
						\noteline, { po.reject({ arg x; [\stepline, \sampleline].includes(x) }); },
						\stepline, { po.reject({ arg x; [\noteline, \sampleline].includes(x) }); }
					);

				} {
					po;
				}
			);
			player.get_effects.do { arg fxname, i;
				var fx = main.get_node(fxname);
				res.addAll( self.get_effect_paramlist(fx, i) );
			};
			res;
		},

		get_effect_paramlist: { arg self, effect, num;
			//var reject = [\in, \out];
			var reject = [\gate, \out, \instrument];
			var prio = [\dry];
			num.debug("get_effect_paramlist");
			effect.get_args.difference(reject).collect { arg argname;
				(argname.asString ++ "_fx" ++ num.asString).asSymbol
			};
		},

		assign_midi: { arg self;
			var param;
			var offset = self.model.midi_knob_offset;
			main.midi_center.clear_assigned(\slider);
			main.midi_center.clear_assigned(\knob);

			self.get_paramlist.do { arg param_name;
				player.name.debug("assign_midi player.name");
				offset.debug("assign_midi offset");
				param = player.get_arg(param_name);

				case
					{ [\adsr].includes(param_name) || param_name.asString.containsStringAt(0,"adsr_") } {
						//TODO: working ?
						main.midi_center.assign_adsr(param)
					}
					{ [\sampleline, \noteline, \stepline].includes(param_name) } {
						//TODO
						//midi = ~piano_recorder.(player);
					}
					{ self.model.param_no_midi.includes(param_name) } {
						// no midi
					}
					{ self.param_types.param_slider_group.includes(param_name)} {
							main.midi_center.assign_first(\slider, param);
					}
					{ true } {
						if(offset <= 0) {
							main.midi_center.assign_first(\knob, param);
							[offset, param.name].debug("assign_midi assign param");
						} {
							offset = offset - 1;
							offset.debug("assign_midi offset<");
						};
					};
			};

		},

		get_param_offset: { arg self;
			if( player.kind == \player ) {
				self.get_paramlist.indexOf(player.current_mode);
			} {
				0
			}
		},

		controller: { arg editplayer; (

			get_param_at: { arg self, idx;
				player.get_arg( editplayer.get_paramlist[idx] )
			},

			get_selected_param: { arg self;
				self.get_param_at( editplayer.model.selected_param )
			},

			// control API

			select_param: { arg self, idx;
				var param, restorefun = {};
				if(idx.isNil) { Error("editplayer: select_param: idx is nil").throw };
				editplayer.model.param_order.debug("editplayer.select_param param_order");
				idx.debug("selecting param number");
				editplayer.get_paramlist.debug("select_param: paramlist");
				if(idx < (editplayer.get_paramlist.size), { 
					param = self.get_param_at(idx);
					player.select_param(param.name);
					editplayer.model.selected_param = idx;
					if(param.classtype == \noteline) {
						main.commands.enable_mode([\editplayer, \noteline]);
					} {
						main.commands.disable_mode([\editplayer, \noteline]);
					};
					if(param.classtype == \adsr) {
						main.midi_center.assign_adsr(param);
					};
				}, {
					idx.debug("selected param out of range");
				});
			},

			select_cell: { arg self, idx;
				var sel, dur;
				sel = self.get_selected_param.();
				switch( sel.classtype,
					\stepline, {
						sel.toggle_cell(idx);
					}, 
					\noteline, {
						//dur = player.get_arg(\dur).preset;
						//sel.set_start_offset(idx*dur.val[dur.selected_cell]);
					},
					\control, {
						if( editplayer.model.colselect_mode ) {
							if( sel.current_kind == \preset ) {
								sel.select_cell(idx);
							} {
								editplayer.get_paramlist.do { arg par;
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

			set_value: { arg self, val;
				self.get_selected_param.set_value(val);
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

			change_kind: { arg self, kind;
				var param = self.get_selected_param;
				if(param.classtype == \control, {
					self.get_selected_param.change_kind(kind);
				});
			};


		)},

		save_column_preset: { arg self;
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

		load_column_preset: { arg self;
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
			livenodename = main.make_livenodename_from_libnodename(libnodename);
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

		load_effectnode: { arg self;
			var livenode;
			~choose_effect.(main, { arg libnodename, livenodename; 
				//self.model.default_newnode = [\libnode, libnodename];
				livenodename = self.make_fxnode_from_libnode(libnodename);
				player.add_effect(livenodename);
				self.changed(\paramlist);
			}, { arg livenodename;
				//self.model.default_newnode = [\livenode, livenodename];
				livenodename = self.duplicate_fxnode(livenodename);
				livenodename.debug("actionpreset");
				player.add_effect(livenodename);
				self.changed(\paramlist);
			});
		},


		decrease_first_note_dur: { arg self;
			var dur, pl;
			pl = player.get_arg(\noteline);
			dur = pl.get_first_note_dur; 
			pl.set_first_note_dur(dur - player.get_arg(\dur).get_val);
			pl.changed(\notes);
		},

		increase_first_note_dur: { arg self;
			var dur, pl;
			pl = player.get_arg(\noteline);
			dur = pl.get_first_note_dur; 
			pl.set_first_note_dur(dur + player.get_arg(\dur).get_val);
			pl.changed(\notes);
		},

		solo_selected: { arg self;
			main.play_manager.solo_node(player.uname)
		},

		unsolo_selected: { arg self;
			main.play_manager.unsolo_node
		},

		start_tempo_recorder: { arg self;
			// TODO: handle when recording finish (play recorded track along what is playing ?)
			var finish = {
				main.play_manager.set_recording(false);
				main.play_manager.keep_recording_session = true; // to play in sync with recording clock (see play_manager.start_new_session)
				player.play_node;
			};
			if(main.play_manager.is_recording.not) {
				if(player.kind == \player && (player.name != \voidplayer)) {
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
						self.recorder.player_start_tempo_recording(finish);
					}
				} {
					"INFO: grouplive: start_tempo_recorder: selected cell is not a player".inform;
				}
			} {
				"hmatrix: already recording".debug;
			};
		},

		cancel_recording: { arg self;
			if(main.play_manager.is_recording == true) {
				self.recorder.cancel_recording;
				main.play_manager.set_recording(false);
			} {
				"hmatrix: not recording".debug;
			};
		},

		start_midi_liveplayer: { arg self;
			if(player.kind == \player && (player.name != \voidplayer)) {
				if(player.is_audiotrack) {
					"start_midi_liveplayer:not midi".debug;
				} {
					self.midi_liveplayer = ~make_midi_liveplayer.(player, main);
					self.midi_liveplayer.start_liveplay;
				}
			} {
				"INFO: editplayer: start_midi_liveplayer: selected player is not a note player".inform;
			}

		},

		init: { arg editplayer, player, parent;
			var noteline, delta;
			var param_order = editplayer.model.param_status_group ++ editplayer.model.param_order, selected;
			"========== init editplayer ============".debug;
			player.uname.debug("editplayer.init:player.uname");
			thisThread.exceptionHandler.postcs;

			// param order
			param_order = param_order.asList;

			param_order.debug("init param_order");
			player.get_args.debug("init get_args");

			player.get_args.do { arg key;
				//key.dump.debug("key");
				if(param_order.includes(key).not, {
					param_order.add(key);
					//key.debug("adding key");
				})
			};
			param_order.debug("init param_order2");
			player.get_args.debug("player.get_args");
			param_order.deepCopy.do { arg key; if(player.get_args.includes(key).not, { param_order.remove(key) }) };
			param_order.debug("init param_order3");
			param_order = param_order.reject({ arg x; editplayer.model.param_reject.includes(x) });
			param_order.debug("init param_order4");
			
			editplayer.model.param_order = param_order;

			editplayer.model.param_order.debug("fin init param_order");

			//player.set_noteline(true);




			// kb shotcuts
			"kb shortcut".debug;

			main.commands.array_add_enable([\editplayer, \select_cell], [\kb, 0], ~keycode.kbnumline, { arg i;
				editplayer.controller.select_cell((player.get_bank*editplayer.model.max_cells)+i) 
			});

			// edit value mode

			main.commands.add_enable([\editplayer, \edit_value], [\kb, 0, ~keycode.kbspecial.enter], { 
				var sel = editplayer.controller.get_selected_param;
				sel.name.debug("voici le nom!");
				case
					{ editplayer.model.param_field_group.includes(sel.name)} {
						~set_numpad_mode.(main.commands, { arg val; 
							if(val.interpret.isNumber, { sel.set_val(val.interpret); sel.changed(\cells) });
						});

					}
					{ sel.name == \bufnum || sel.name.asString.containsStringAt(0,"bufnum_")} {
						~choose_sample.(main, { arg buf; sel.set_val(buf);  })
					};
			});
			main.commands.array_set_shortcut([\editplayer, \edit_value_mode, \insert_number], [\kb, 0], ~keycode.kbnumpad);
			main.commands.set_shortcut([\editplayer, \edit_value_mode, \insert_point], [\kb, 0, ~keycode.kbspecial.point]);
			main.commands.set_shortcut([\editplayer, \edit_value_mode, \cancel], [\kb, 0, ~keycode.kbspecial.escape]);
			main.commands.set_shortcut([\editplayer, \edit_value_mode, \ok], [\kb, 0, ~keycode.kbspecial.enter]);

			main.commands.add_enable([\editplayer, \solo_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[6]], { editplayer.solo_selected });
			main.commands.add_enable([\editplayer, \unsolo_selected], [\kb, ~keycode.mod.ctrlfx, ~keycode.kbfx[6]], { editplayer.unsolo_selected });

			//

			main.commands.array_add_enable([\editplayer, \select_simple_param], [\kb, ~keycode.mod.ctrl], ~keycode.kbcnumline, { arg i;
				editplayer.controller.select_param(i);
			});

			main.commands.array_add_enable([\editplayer, \select_param], [\kb, ~keycode.mod.alt], ~keycode.kbnumline, { arg i;
				editplayer.controller.select_param(i+editplayer.get_param_offset)
			});

			main.commands.add_enable([\editplayer, \set_seq_kind], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["a"]], {
				editplayer.controller.change_kind(\seq)
			});
			main.commands.add_enable([\editplayer, \set_scalar_kind], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["s"]], {
				editplayer.controller.change_kind(\scalar)
			});
			main.commands.add_enable([\editplayer, \set_seg_kind], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["g"]], {
				editplayer.controller.change_kind(\seg)
			});
			main.commands.add_enable([\editplayer, \set_bus_kind], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["u"]], {
				editplayer.controller.change_kind(\bus)
			});

			// player mode selection
			"editplayer shorcut 1".debug;

			main.commands.add_enable([\editplayer, \set_noteline_mode], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["n"]], {
				player.set_mode(\noteline);
				editplayer.controller.select_param(editplayer.get_param_offset);
				//editplayer.refresh
			});

			main.commands.add_enable([\editplayer, \set_stepline_mode], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["b"]], {
				player.set_mode(\stepline);
				editplayer.controller.select_param(editplayer.get_param_offset);
				//editplayer.refresh
			});

			main.commands.add_enable([\editplayer, \set_sampleline_mode], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["m"]], {
				if(player.get_arg(\sampleline).notNil) {
					player.set_mode(\sampleline);
					editplayer.controller.select_param(editplayer.get_param_offset);
					//editplayer.refresh
				} {
					"editplayer: Can't set sampleline mode: not a sample player".inform;	
				};
			});
			
			//~prec = ~piano_recorder.value(player); // debile

			////////////////// recording

			main.commands.add_enable([\editplayer, \toggle_recording], [\midi, 0, ~keycode.cakewalk.button[7]], { 
				if(main.play_manager.is_recording.not) {
					editplayer.start_tempo_recorder;
				} {
					editplayer.cancel_recording;
				}
			});

			main.commands.add_enable([\editplayer, \toggle_metronome], [\kb, ~keycode.mod.altshift, ~keycode.kbsaalphanum["m"]], {
				if(main.play_manager.use_metronome == false) {
					main.play_manager.use_metronome = true;
				} {
					main.play_manager.use_metronome = false;
				}
			});


			main.commands.add_enable([\editplayer, \start_tempo_recording], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["r"]], {
				editplayer.start_tempo_recorder;
			});
			main.commands.add_enable([\editplayer, \cancel_recording], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["t"]], {
				editplayer.cancel_recording;
			});

		//	main.commands.add_enable([\editplayer, \start_recording], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["r"]], {
		//		"STARTRECORD!!".debug; 
		//		player.get_arg(\noteline).midi.start_recording.();
		//		player.get_arg(\noteline).midi.changed(\recording); 
		//		player.get_arg(\noteline).mute(true);
		//	});

		//	main.commands.add_enable([\editplayer, \stop_recording], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["t"]], {
		//		var sum = 0;
		//		var midi = player.get_arg(\noteline).midi;
		//		midi.stop_recording.();
		//		midi.changed(\recording, false);
		//		midi.track.debug("66666666666666666666666666666- this is record!!!!");

		//		midi.track.do { arg no;
		//			sum = sum + no.dur;
		//		};
		//		sum.debug("total sum");
		//		player.get_arg(\noteline).set_notes(midi.track);
		//		player.get_arg(\noteline).mute(false);
		//		player.get_arg(\noteline).changed(\notes);
		//	});

			main.commands.add_enable([\editplayer, \set_notequant], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["q"]], { 
				var delta;
				var param;
				if([\sampleline, \noteline].includes(player.get_mode)) {
					param = player.get_arg(player.get_mode);
					delta = player.get_arg(\dur).get_val;
					if(param.notequant.isNil) {
						param.set_notequant(delta)
					} {
						param.set_notequant(nil)
					}
				}
			});

			// edit wrapper

			main.commands.add_enable([\editplayer, \edit_wrapper], [\kb, ~keycode.mod.alt, ~keycode.kbaalphanum["w"]], { player.edit_wrapper });

			// noteline
			"editplayer shorcut 2".debug;
			
			noteline = player.get_arg(\noteline);
			if( noteline.notNil ) {
				delta = { player.get_arg(\dur).get_val };

				main.commands.add_shortcut([\editplayer, \noteline, \increase_start_silence], [\kb, 0, ~keycode.kbnumpad[3]], { 
					noteline.set_start_silence(noteline.get_start_silence + delta.())
				});
				main.commands.add_shortcut([\editplayer, \noteline, \decrease_start_silence], [\kb, 0, ~keycode.kbnumpad[1]], {
					noteline.set_start_silence(noteline.get_start_silence - delta.())
				});

				main.commands.add_shortcut([\editplayer, \noteline, \increase_end_silence], [\kb, 0, ~keycode.kbnumpad[6]], {
					noteline.set_end_silence(noteline.get_end_silence + delta.())
				});
				main.commands.add_shortcut([\editplayer, \noteline, \decrease_end_silence], [\kb, 0, ~keycode.kbnumpad[4]], {
					noteline.set_end_silence(noteline.get_end_silence - delta.())
				});

				main.commands.add_shortcut([\editplayer, \noteline, \increase_start_offset], [\kb, ~keycode.mod.ctrl, ~keycode.kbnumpad[3]], {
					noteline.set_start_offset(noteline.get_start_offset + delta.())
				});
				main.commands.add_shortcut([\editplayer, \noteline, \decrease_start_offset], [\kb, ~keycode.mod.ctrl, ~keycode.kbnumpad[1]], {
					noteline.set_start_offset(noteline.get_start_offset - delta.())
				});

				main.commands.add_shortcut([\editplayer, \noteline, \increase_end_offset], [\kb, ~keycode.mod.ctrl, ~keycode.kbnumpad[6]], {
					noteline.set_end_offset(noteline.get_end_offset + delta.())
				});
				main.commands.add_shortcut([\editplayer, \noteline, \decrease_end_offset], [\kb, ~keycode.mod.ctrl, ~keycode.kbnumpad[4]], {
					noteline.set_end_offset(noteline.get_end_offset - delta.())
				});
			};

			// cells 

			main.commands.add_enable([\editplayer, \add_cell_bar], [\kb, 0, ~keycode.numpad.plus], {
				editplayer.controller.add_cell_bar.() 
			});
			main.commands.add_enable([\editplayer, \remove_cell_bar], [\kb, ~keycode.mod.ctrl, ~keycode.numpad.plus], {
				editplayer.controller.remove_cell_bar.() 
			});

			main.commands.add_enable([\editplayer, \increase_first_note_dur], [\kb, ~keycode.mod.arrow, ~keycode.kbarrow.right], {
				editplayer.increase_first_note_dur() 
			});

			main.commands.add_enable([\editplayer, \decrease_first_note_dur], [\kb, ~keycode.mod.arrow, ~keycode.kbarrow.left], {
				editplayer.decrease_first_note_dur() 
			});

			main.commands.array_add_enable([\editplayer, \change_bank], [\kb, 0], ~keycode.kbnumpad, { arg idx; editplayer.set_bank(idx) });

			// save load column preset

			main.commands.add_enable([\editplayer, \load_column_preset], [\kb, ~keycode.mod.fx, ~keycode.kbfx[0]], { editplayer.load_column_preset });
			main.commands.add_enable([\editplayer, \save_column_preset], [\kb, ~keycode.mod.fx, ~keycode.kbfx[1]], { editplayer.save_column_preset });

			// playing

			main.commands.add_enable([\editplayer, \play_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[4]], { player.play_node });
			main.commands.add_enable([\editplayer, \midi_play_selected], [\midi, 0, ~keycode.cakewalk.button[5]], { player.play_node });
			main.commands.add_enable([\editplayer, \stop_selected], [\kb, ~keycode.mod.fx, ~keycode.kbfx[5]], { player.stop_node });
			main.commands.add_enable([\editplayer, \midi_stop_selected], [\midi, 0, ~keycode.cakewalk.button[4]], { player.stop_node });

			main.commands.add_enable([\editplayer, \play_repeat_selected], [\kb, ~keycode.mod.ctrlfx, ~keycode.kbfx[4]], { player.play_repeat_node });

			main.commands.add_enable([\editplayer, \panic], [\kb, ~keycode.mod.fx, ~keycode.kbfx[7]], { main.panic });

			main.commands.add_enable([\editplayer, \start_midi_liveplayer], [\kb, ~keycode.mod.altshift, ~keycode.kbsaalphanum["e"]], { editplayer.start_midi_liveplayer });

			// effects

			main.commands.add_enable([\editplayer, \add_effect], nil, { editplayer.load_effectnode });


			// select param
			selected = editplayer.get_paramlist.detectIndex { arg i; player.get_selected_param == i };
			selected.debug("param idx selected");
			editplayer.get_paramlist[selected ?? editplayer.get_param_offset].debug("param selected");
			editplayer.controller.select_param(selected ?? editplayer.get_param_offset);


			// midi keys
			"midi keys".debug;

			main.commands.add_enable([\editplayer, \increase_midi_knob_offset], [\kb, ~keycode.mod.arrow, ~keycode.kbarrow.down], { 
				editplayer.model.midi_knob_offset = (editplayer.model.midi_knob_offset + 8).max(0);
				editplayer.assign_midi;
			});

			main.commands.add_enable([\editplayer, \decrease_midi_knob_offset], [\kb, ~keycode.mod.arrow, ~keycode.kbarrow.up], { 
				editplayer.model.midi_knob_offset = (editplayer.model.midi_knob_offset - 8).min(4); //FIXME: compute max value
				editplayer.assign_midi;
			});

			//~make_midi_kb_control.(player, editplayer);

			// change panel
			"make_panel_shortcuts".debug;

			~make_panel_shortcuts.(main, \editplayer, cleanup:true);

			// make view
			"make view".debug;

			editplayer.recorder = ~make_midi_recorder.(player, main);

			editplayer.assign_midi;
			~make_editplayer_view.(parent, main, editplayer, player, param_order);
		}
	);
	ep.init(player, parent);
	ep;

};

)
