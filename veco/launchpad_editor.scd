
////////// seq/matrix editors

(

~class_track_matrix_controller = (
	new: { arg self;
		self = self.deepCopy;
	
		self.matrix = Order.new;
	
		self;
	},

	off_mode: \bottom,
	
	get_column: { arg self, col;
		self.matrix[col]
	},

	set_column_hook: { arg self, col, val;
		
	},

	is_blank: { arg self, row;
		self.fxlist[row].isNil;
	},

	set_column: { arg self, col, set;
		case 
			{ self.off_mode == \same and: {self.matrix[col] == set} } {
				self.matrix[col] = nil;
				self.set_column_hook(col, nil);
			}
			{ self.off_mode == \bottom and: {self.matrix[col] == set and: { set == 7 }} } {
				self.matrix[col] = nil;
				self.set_column_hook(col, nil);
			}
			{
				self.matrix[col] = set;
				self.set_column_hook(col, set);
			};
	},

);

~class_matrix_select_fx = (
	parent: ~class_track_matrix_controller,
	new: { arg self, fxlist;
		self = ~class_track_matrix_controller[\new].(self);
	
		self.set_fxlist(fxlist);
	
		self;
	},

	set_column_hook: { arg self, col, val;
		var ocol = col;
		var uname;
		var node;
		var fx;
		node = Veco.main.get_node_by_index(ocol);
		fx = self.fxlist[val];
		if(fx.notNil) {
			[node.uname, col,fx].debug("play!!");

			Ndef(node.uname+++\mixer).play(fx);
		} {
			Ndef(node.uname+++\mixer).play(0);
			[node.uname, col,fx].debug("stop!!");
			self.matrix[col] = nil;
		}
	},

	set_fxlist: { arg self, fxlist;
		fxlist = fxlist ?? [];
		self.fxlist = fxlist;
		
	},


);

~launchpad_scene_track_matrix = (
	parent: ~launchpad_scene,

	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	paint_mode: \fill,
	blank_color: \dimgreen,


	activate_display: { arg self, model;
		self.model = model;
		self.activate;
	},

	render_state: { arg self;
		self.gridsize.debug("render_state!!");
		self.gridsize.x.do { arg x;
			self.render_column(x);
		}
	},

	is_blank: { arg self, pos;
		self.model.is_blank(pos.y)	
	},

	render_pos: { arg self, pos;
		var opos;
		var comp;
		comp = if(self.paint_mode == \fill) {
			{ arg x, y;
				var val;
				val = self.model.get_column(x);
				if(val.notNil) {
					val <= y;
				} {
					false
				};
			}
		} {
			{ arg x, y;
				self.model.get_column(x) == y;
			}
		};
		opos = pos + self.controller.clip_offset;
		[opos, pos].debug("render_pos");
		if(comp.(opos.x, pos.y)) {
			self.set_led(pos, \red);
		} {
			if(self.is_blank(pos)) {
				self.set_led(pos, self.blank_color);
			} {
				self.set_led(pos, \off);
			}
		}
	},

	render_column: { arg self, x;
		self.gridsize.y.do { arg y;
			self.render_pos(Point(x,y));
		}
	},

	response_cc_on: { arg self, midi;
		self.sceneset.clip.response_cc_on(midi);
	},

	//response_cc_off: { arg self, midi;
	//	self.sceneset.clip.response_cc_off(midi);
	//},

	response_cc_off: { arg self, midi;
		"FUXXXXXXXXXXXXXXXXXXXXXXXXXX".debug;
		if(self.skip_first_time == true) {
			self.skip_first_time = false;
		} {
			self.sceneset.clip.response_cc_off(midi);
		};
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			opos = self.controller.clip_offset + pos;
			self.model.set_column(opos.x, pos.y);
			self.render_column(pos.x);
		}
	},

	response_off: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			//self.controller.get_codepad(pos).response_off;
		}
	},

);

~matrix_view = ~launchpad_scene_track_matrix.new({Veco.main});
~matrix_ctrl = ~class_matrix_select_fx.new;
~matrix_view.paint_mode = \normal;
//~matrix_view.activate_display(~matrix_ctrl);

);

(
8.do { arg x;
	x = x+1;
	BusDef('fx'+++x, \audio);
};
);

// END

//~matrix_view.controller
//BusDef('fx1')
//
//(
//~matrix_ctrl.set_fxlist([
//	BusDef('fx1'),
//	BusDef('fx2'),
//	BusDef('fx3'),
//	//BusDef('fx4'),
//]);
//~matrix_view.activate_display(~matrix_ctrl);
//)
//
//(
//Veco.main.nodes.keysValuesDo { arg key, val;
//	if(val.has_mixer) {
//		key.postln;
//		Ndef(key+++\mixer).play(0);
//	};
//
//}
//)
//
//(
//Veco.main.nodes.keysValuesDo { arg key, val;
//	if(val.has_mixer) {
//		key.postln;
//		Ndef(key+++\mixer).play(BusDef('fx1'));
//	};
//
//}
//)
//
//(
//	8.do { arg x;
//		
//		var val = Veco.main.get_node_by_index(x+Veco.main.clip_offset.x);
//		var key = val.uname;
//		
//		if(val.has_mixer) {
//			key.postln;
//			Ndef(key+++\mixer).play(BusDef('fx1'));
//		};
//	}
//)
//(
//	8.do { arg x;
//		
//		var val = Veco.main.get_node_by_index(x+Veco.main.clip_offset.x);
//		var key = val.uname;
//		
//		if(val.has_mixer) {
//			key.postln;
//			Ndef(key+++\mixer).play(0);
//		};
//	}
//)
