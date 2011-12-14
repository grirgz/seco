(

~mixer_view = { arg parent, mixer, paramlist;
	var midi;
	var width = 1200;
	var row_layout;
	var sc_mixer;
	var mixer_messages;

	row_layout = GUI.vLayoutView.new(parent, Rect(0,0,(width+10),800));
	row_layout.background = Color.blue;


	mixer_messages = Dictionary.newFrom((
		paramlist: { arg self;
			var player;
			var master;
			var midi;
			
			//~midi_interface.clear_assigned(\slider);
			//~midi_interface.clear_assigned(\knob);

			//CCResponder.removeAll; // FIXME: must not remove other useful CC

			row_layout.removeAll;

			"paramlist begin".debug;
			player = (
				// fake player
				name: \mixer_panel,
				get_bank: 0
			);

			"0paramlist begin".debug;
			master = mixer.master;
			"1paramlist begin".debug;
			~make_control_view.(row_layout, mixer.make_param_display(\master, 0), master);
			"3paramlist begin".debug;

			//self.get_paramlist.debug("paramlist");
			self.get_paramlist.do { arg paramasso, i;
				var hihi2 = \rah;
				var player_name = paramasso.key.name;
				var param = paramasso.value;
				//paramasso.debug("paramasso");

				~make_control_view.(row_layout, mixer.make_param_display(param, i), param);

			};
			"end paramlist method".debug;
		}
	));

	sc_mixer = SimpleController(mixer);
	~init_controller.(sc_mixer, mixer_messages);
	mixer.refresh.();

	// remove func

	row_layout.onClose = {
		sc_mixer.remove;
	};

};

~make_mixer = { arg main, parent;
	var mixer;

	"iniyuyyy".debug;
	mixer = (
		model: (
			max_cells: 8, // not used
			param_offset: 0@0
		),

		master: ~make_volume_param.(\master, main),

		make_param_display: { arg self, param, idx;
			var dis;
			dis = (
				get_bank: { arg self;
					0; // not used;
				},
				selected: { arg self;
					mixer.model.param_offset.x;
				},
				max_cells: { arg self;
					mixer.model.max_cells;	
				},
				get_selected_cell: {
					param.get_selected_cell;
				},
				show_midibloc: true,
				name: { arg self;
					var pname;
					pname = mixer.get_paramlist[idx].key.name;
					pname;
				},
				noteline_numbeats: 32,
				cell_width: 30,
				width: 200,
				height: 30,
				name_width: { arg self;
					100;
				}
			);
			if(param == \master, {
				dis.name = "Master";
			});
			dis
		},

		get_paramlist: { arg self, offset=nil;
			var list = List.new;
			"begin get_paramlist".debug;
			offset = offset ?? self.model.param_offset;
			"1begin get_paramlist".debug;
			//main.model.parlive.debug("get_paramlist main.model.parlive");
			main.context.get_selected_node_set[offset.y..].do { arg nodegroup, i;
				if (nodegroup.name != \void_FIXME) { // FIXME: change groupname when it has children
					nodegroup.children.do { arg nodename;
						var node;
						if(nodename != \voidplayer, {
							//FIXME: check for other types of nodes
							//node.debug("get_paramlist:node");
							node = main.get_node(nodename);
							if(node.kind == \player) {
								list.add(node -> node.get_arg(\amp));
							}
						});
					};
				}
			};
			//list.debug("get_paramlist list");
			list;
		},

		assign_midi: { arg self;
			var param;
			"MAIS assign_midi bordel!!!".debug;
			main.midi_center.clear_assigned(\slider);
			main.midi_center.clear_assigned(\knob);
			self.get_paramlist.debug("c'est quoi cette paramlist de merde");
			self.get_paramlist.do { arg asso;
				var player = asso.key;
				var param = asso.value;
				"1MAIS assign_midi bordel!!!".debug;
				main.midi_center.assign_first(\slider, param);
				"2MAIS assign_midi bordel!!!".debug;
			};
			"3MAIS assign_midi bordel!!!".debug;
			main.commands.bind_param([\slider, 8], self.master);
			"4MAIS assign_midi bordel!!!".debug;

		},

		get_bank: { arg self;
			main.context.get_selected_bank
		},


		update_title: { arg self;
			main.set_window_title("mixer: bank:"++  self.get_bank ++ "; offset:" ++ self.model.param_offset.y);
		},

		refresh: { arg self;
			self.update_title;
			self.changed(\paramlist);
			//self.assign_midi;
		},

		init: { arg self;
			"ini".debug;

			main.commands.array_add_enable([\mixer, \select_offset], [\kb, 0], ~keycode.kbnumline, { arg x; 
				self.model.param_offset = 0@x;
				self.update_title;
				self.changed(\paramlist);
			});

			~make_panel_shortcuts.(main, \mixer);

			"ini2".debug;

			self.assign_midi;
			~mixer_view.(parent, mixer);
			"inifin".debug;


		}
	);
	mixer.init;
	mixer;


};



)
