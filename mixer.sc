(


~compare_point = { arg a, b;
	case
		{ a.x == b.x } { a.y < b.y }
		{ a.x < b.x }
};

~make_mixer = { arg main, parent, kb_handler;
	var mixer;

	mixer = (
		model: (
			param_offset: 0@0
		),

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
			~kbnumline.do { arg kc, i; kb_handler[[0, kc]] = { 
				self.model.param_offset = 0@i;
				self.changed(\paramlist);
			}};

			~mixer_view.(parent, mixer);


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

	~midi_interface.clear_assigned(\slider);
	~midi_interface.clear_assigned(\knob);

	CCResponder.removeAll; // FIXME: must not remove other useful CC

	mixer_messages = Dictionary.newFrom((
		paramlist: { arg self;
			var player;
			var master;
			var midi;
			player = (
				// fake player
				name: \mixer_panel,
				get_bank: 0
			);

			master = ~make_volume_param.(\master);
			master.midi = ~midi_interface.assign_master(master);
			~make_control_view.(row_layout, player, master, master.midi, \Master, 100);

			self.get_paramlist.do { arg paramasso, i;
				var player_name = paramasso.key;
				var param = paramasso.value;

				midi = ~midi_interface.assign_first(\slider, param);
				param.midi = midi;
				~make_control_view.(row_layout, player, param, midi, player_name, 100);

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
