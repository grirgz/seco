(

~midi_center = { arg main;
	var mc = (
		cc_states: MultiLevelIdentityDictionary,

		get_control_val: { arg self, ccpath;
			self.cc_states.at(*ccpath)
		},

		set_control_val: { arg self, ccpath, val;
			var path = ccpath ++ [val];
			self.cc_states.put(*path);
		},

		get_midi_control_handler: { arg midi_center, param;
			(
				blocked_kind: \sup,

				set_val: { arg self, val, cell=nil;
					if(self.blocked.not) {
						//TODO: implement cell setting
						param.set_norm_val(val)
						//TODO: recording of val ?
					};
					param.changed(\midi_val, midi_center.get_midi_val)
				},

				get_midi_val: { arg self;
					param.spec.map(midi_center.get_control_val(self.get_ccpath))
				},

				get_ccpath: { arg self;
					self.ccpath;
				},

				set_ccpath: { arg self, val;
					self.ccpath = val;
				},

				blocked: { arg self;
					var midi_val, param_val;

					midi_val = self.get_midi_val;
					param_val = param.get_val;

					case 
						{ midi_val > param_val } {
							self.blocked_kind = \sup;
							self.changed(\blocked);
						}
						{ midi_val < param_val } {
							self.blocked_kind = \inf;
							self.changed(\blocked);
						}
						{ true } {
							self.blocked_kind = \not;
							self.changed(\blocked);
						};

					self.blocked_kind == \not

				},

				refresh: { arg self;
					self.blocked;
					param.changed(\midi_val, midi_center.get_midi_val);
				}

			);
		},

		install_responders: { arg self;
			self.uninstall_responders;
			self.ccresp = List.new;
			~keycode.cakewalk.do { arg cctype;
				cctype.do { arg keycode, i;

					self.ccresp.add( CCResponder({ |src,chan,num,value|
							var ccpath = [cctype, i];
							var val = value/127;
							self.set_control_val(ccpath, val);
							main.commands.handle_cc(ccpath, val);
						},
						nil, // any source
						nil, // any channel
						keycode, // any CC number
						nil // any value
						)
					)
				}
			}

		},

		uninstall_responders: { arg self;
			self.ccresp.do { arg x; x.remove };
			self.ccresp = nil;
		}
	);
	mc;
};


)
