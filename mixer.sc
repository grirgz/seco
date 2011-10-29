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
			
			~midi_interface.clear_assigned(\slider);
			~midi_interface.clear_assigned(\knob);

			CCResponder.removeAll; // FIXME: must not remove other useful CC

			row_layout.removeAll;

			"paramlist begin".debug;
			player = (
				// fake player
				name: \mixer_panel,
				get_bank: 0
			);

			"0paramlist begin".debug;
			master = ~make_volume_param.(\master);
			"1paramlist begin".debug;
			master.midi = ~midi_interface.assign_master(master);
			"2paramlist begin".debug;
			~make_control_view.(row_layout, mixer.make_param_display(\master, 0), master, master.midi);
			"3paramlist begin".debug;

			self.get_paramlist.debug("paramlist");
			self.get_paramlist.do { arg paramasso, i;
				var hihi2 = \rah;
				var player_name = paramasso.key.name;
				var param = paramasso.value;
				paramasso.debug("paramasso");

				midi = ~midi_interface.assign_first(\slider, param);
				param.midi = midi;
				~make_control_view.(row_layout, mixer.make_param_display(param, i), param, midi);

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
			main.context.get_selected_node_set.do { arg nodegroup, i;
				if (nodegroup.name != \void_FIXME) { // FIXME: change groupname when it has children
					nodegroup.children.do { arg node;
						if(node.name != \void, {
							//FIXME: check for other types of nodes
							//node.debug("get_paramlist:node");
							list.add(node -> node.get_arg(\amp));
						});
					};
				}
			};
			//list.debug("get_paramlist list");
			list;
		},

		refresh: { arg self;
			self.changed(\paramlist);
		},

		init: { arg self;
			"ini".debug;

			main.commands.array_add_enable([\mixer, \select_offset], [\kb, 0], ~keycode.kbnumline, { arg x; 
				self.model.param_offset = 0@x;
				self.changed(\paramlist);
			});
			main.commands.add_enable([\mixer, \show_panel, \parlive], [\kb, ~keycode.mod.fx, ~keycode.kbfx[8]], { main.show_panel(\parlive) });
			main.commands.add_enable([\mixer, \show_panel, \score], [\kb, ~keycode.mod.fx, ~keycode.kbfx[10]], { main.show_panel(\score) });
			main.commands.add_enable([\mixer, \show_panel, \editplayer], [\kb, ~keycode.mod.fx, ~keycode.kbfx[11]], { main.show_panel(\editplayer) });
			"ini2".debug;

			~mixer_view.(parent, mixer);
			"inifin".debug;


		}
	);
	mixer.init;
	mixer;


};



)
