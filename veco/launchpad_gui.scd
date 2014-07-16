
(
~class_launchpad_gui = (
	new: { arg self;
		self = self.deepCopy;
	
		self.num_colors = ();

		self.scene = { ~launchpad_gui_current_scene };

		self;
	},

	
	gridpos_to_midi: { arg self, pos;
		Veco.main.clip.gridpos_to_midi(pos);
	},

	make_gui: { arg self;
		var rowlayout;
		if(self.window.notNil) {
			self.window.close;
		};
		self.window = Window.new;

		Veco.main.clip.colors.keysValuesDo { arg k, v;
			self.num_colors[v] = k;
		};

		self.gridlayout = VLayout.new;

		rowlayout = HLayout.new;
		self.gridlayout.add(rowlayout);
		9.collect({ arg x;
			var but = Button.new;
			var midinote = x + 104;
			but.fixedWidth = 50;
			but.fixedHeight = 20;
			rowlayout.add(but);
			but.action = { arg but;
				but.value.debug("but");
				if(but.value == 1) {
					[x, midinote].debug("mode hold button action on");
					self.scene.handle_modifiers_on(midinote);
					self.scene.response_cc_on(midinote);
				} {
					[x, midinote].debug("mode hold button action off");
					self.scene.handle_modifiers_off(midinote);
					self.scene.response_cc_off(midinote);
				}
			};
			but.states = [
				[" ", Color.white, Color.white],
				[" ", Color.black, Color.red],
			];

		});
		
		self.grid = 9.collect({ arg y;
			var rowlayout = HLayout.new;
			self.gridlayout.add(rowlayout);
			9.collect({ arg x;
				var but = Button.new;
				var midinote;
				but.fixedWidth = 50;
				but.fixedHeight = 50;
				[x, y].debug("button action");
				case 
					{ y == 0 and: { x < 8 }} {
						midinote = x + 104;
						but.mouseDownAction = {
							[x, y, midinote].debug("mode button action on");
							self.scene.handle_modifiers_on(midinote);
							self.scene.response_cc_on(midinote);
						};
						but.action = {
							[x, y, midinote].debug("mode button action off");
							self.scene.handle_modifiers_off(midinote);
							self.scene.response_cc_off(midinote);
						}
					}
					{ x == 8 and: {y == 0} } {
						// NOOP
					}
					{
						midinote = self.gridpos_to_midi(Point(x,y-1));
						but.mouseDownAction = {
							[x, y, midinote].debug("button action on");
							self.scene.response_on(midinote);
						};
						but.action = {
							[x, y, midinote].debug("button action off");
							self.scene.response_off(midinote);
						};
					};
				rowlayout.add(but);
				but;
			})
		});

		self.scene.update_display;

		self.window.layout = self.gridlayout;
		self.window.front;
	},


	colors: (
			//red: Color.red,
			red: Color.red,
			red2: Color.red.alpha_(0.5),
			dimred: Color.red.alpha_(0.25),
			green: Color.green,
			green2: Color.green.alpha_(0.5),
			dimgreen: Color.green.alpha_(0.25),
			yellow: Color.yellow,
			orange: Color.new255(249, 77, 0),
			amber: Color.new255(250, 214, 165),
			off: Color.gray,
	),

	color_to_gui_color: { arg self, color;
		if(color.isNumber) {
			color = self.num_colors[color];
		};
		self.colors[color];
	},

	set_button_led: { arg self, pos, color;
		
	},

	set_cc_led: { arg self, pos, color;
		// cc leds are mode leds (top row)
		if(pos >= 50) {
			pos = pos - 104;
		};
		self.set_led(Point(pos,-1), color)
		// TODO

	},

	set_mode_led: { arg self, pos, color;
		self.set_cc_led(pos, color)
	},

	pos_to_label: { arg self, pos;
		var x = pos.x;
		var y = pos.y;
		var res = " ";
		if(x == 8) {
			res = "<";
		};
		if(y == 0) {
			res = ["^", "v", "<", ">",  "clip", "seq", "edit", "mixer"][x];
		};
		res;
	},

	set_led: { arg self, pos, color;
		if(self.window.notNil) {
			pos.debug("POS!!!!");
			color = self.color_to_gui_color(color);
			Task{
				self.grid[pos.y+1][pos.x].states = [
					[self.pos_to_label(Point(pos.x,pos.y+1)), Color.black, color]
				]
			}.play(AppClock)
		}
	},
);

//~launchpad_gui.window.close;
~launchpad_gui = ~class_launchpad_gui.new;
//~launchpad_gui.make_gui
);

//~launchpad_gui.set_led(Point(1,1), \red)
