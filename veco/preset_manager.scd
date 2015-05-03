(

~launchpad_scene_preset_manager = (
	parent: ~launchpad_scene,

	mode_name: \mode_sendpad,
	display_mode: \send,

	set_model: { arg self, model, mode=\send;
		self.display_mode = mode;
		self.mymodel = model;
	},

	render_state: { arg self;
		8.do { arg x;
			self.render_column(x)
		}
	},

	render_column: { arg self, x;
		8.do { arg y;
			self.render_pos(Point(x,y));
		}
	},

	render_pos: { arg self, pos;
		var name;
		var send;
		var node;
		var gridpos = pos;
		var color;
		gridpos.debug("launchpad_scene_sendpad.render_pos: gridpos");
		color = switch(gridpos.y,
			0, {
				if(self.mymodel.getPreset(gridpos.x).notNil) {
					if(self.mymodel.isEnabled(gridpos.x)) {
						\green
					} {
						\green2
					}
				} {
					\off
				}
			},	// load
			1, \red,	// save
			2, {
				if(self.mymodel.getPreset(gridpos.x).notNil) {
					if(self.mymodel.isEnabled(gridpos.x)) {
						\green
					} {
						\yellow
					}
				} {
					\off
				}
			},	// disable
			// delete
			3, {
				if(self.mymodel.getPreset(gridpos.x).notNil) {
					\red
				} {
					\off
				}
			},
			{
				\off
			}
		);
		self.set_led(gridpos, color);
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		//self.unblink_led;
		//self.handle_grid_move(midi);
		//self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;

		//self.sceneset.clip.response_cc_off(midi);

		if(self.skip_first_time != false) {
			self.skip_first_time = false;
		} {
			self.sceneset.clip.response_cc_off(midi);
		}
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_sendpad.response_on");

		if(self.is_launcher_button(midinote)) {
			// NOOP
		} {
			if(self.modifiers[\session] == true) {
				//self.controller.clip_offset = pos * 8;
				//self.render_state;
			} {
				var name = self.controller.get_node_by_index( self.gridpos_to_abspos(pos).x ).uname;
				[name, pos].debug("launchpad_scene_sendpad.response_on: go!");
				switch(pos.y,
					// load
					0, {
						if(self.mymodel.getPreset(pos.x).notNil) {
							self.mymodel.load(pos.x);
						}
					},
					// save
					1, {
						self.mymodel.save(pos.x);
					},
					// disable
					2, {
						if(self.mymodel.isEnabled(pos.x)) {
							self.mymodel.disablePreset(pos.x);
						} {
							self.mymodel.enablePreset(pos.x);
						};
					},
					// delete
					3, {
						self.mymodel.erase(pos.x);
					}
				);
				self.render_column(pos.x);
			}
		}
	},

);


);

// END

//~preset_manager = ~launchpad_scene_preset_manager.new(Veco.main);
//~preset_manager.set_model(PresetListMorpher(ParamGroupDef('fxboard1-1'), 8));
//~preset_manager.activate();
//PresetListMorpher(ParamGroupDef('fxboard1-1')).presets
//PresetListMorpher(ParamGroupDef('fxboard1-1'), 8).presets
//PresetListMorpher(ParamGroupDef('fxboard1-1'), 8).disablePreset(1)
//PresetListMorpher(ParamGroupDef('fxboard1-1'), 8).enablePreset(1)
//PresetListMorpher(ParamGroupDef('fxboard1-1'), 8).isEnabled(1)
//~plm = PresetListMorpher(ParamGroupDef('fxboard1-1'), 8);
//~plm.enablePreset(1)
//~plm.disablePreset(1)
//~plm.disabledPresets
//~plm.isEnabled(1)
//PresetListMorpher(ParamGroupDef('fxboard1-1'), 8).disabledPresets
//ParamGroupDef('fxboard1-1').presets
//ParamGroupDef('fxboard1-1').presets.keys
//ParamGroupDef('fxboard1-1').presets
