(


~make_mixer = { arg main, parent, kb_handler;
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
					pname = mixer.get_paramlist[idx].key;
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
			offset = offset ?? self.model.param_offset;
			main.model.parlive.sortedKeysValuesDo( { arg key, val;
				if( ~compare_point.(key, offset).not, {
					val.data.do { arg pla;
						if(pla != 0, {
							list.add(pla -> main.model.livenodepool[pla].get_arg(\amp));
						});
					};
				})
			}, ~compare_point);
			list.debug("get_paramlist list");
		},

		refresh: { arg self;
			self.changed(\paramlist);
		},

		init: { arg self;
			"ini".debug;
			~kbnumline.do { arg kc, i; kb_handler[[0, kc]] = { 
				self.model.param_offset = 0@i;
				self.changed(\paramlist);
			}};

			~mixer_view.(parent, mixer);
			"inifin".debug;


		}
	);
	mixer.init;
	mixer;


};

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
				var player_name = paramasso.key;
				var param = paramasso.value;
				paramasso.debug("paramasso");

				midi = ~midi_interface.assign_first(\slider, param);
				param.midi = midi;
				~make_control_view.(row_layout, mixer.make_param_display(param, i), param, midi);

			};
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


)
