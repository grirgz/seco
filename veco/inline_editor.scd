
(

~class_number_editor_gui = (
	new: { arg self;
		self = self.deepCopy;
	
		
		self.spec = \freq.asSpec;
		self.current_val = 100;
	
		self;
	},

	new_pattern: { arg self, exit_function, val=0, spec=\freq;
		self = self.deepCopy;

		self.exit_function = { arg self, val; exit_function.(val) };
		self.spec = spec.asSpec;
		self.current_val = val;

		self.make_gui;
		Pfunc({ self.current_val });
		
	},

	make_spec_chooser_gui: { arg self;
		
		var layout;
		var speclist;
		var butlist;
		layout = VLayout.new;
		speclist = [
			\unipolar,
			\bipolar,
			\freq,
			\lofreq,
			\freqshift,
			\legato,
			\amp,
		];

		speclist.do { arg spec, sidx;
			var but;
			but = Button.new;
			but.states = [[spec.asString]];
			butlist.add(but);
			layout.add(but);
			but.action = {
				self.spec = spec.asSpec;
				butlist.do { arg but, idx; 
					if(idx != sidx) {
						but.value = 0 
					}
				}
			};
		};
		layout;

	},

	make_gui: { arg self;
		var layout;
		var mlayout;
		self.window = Window.new;
		self.slider = Slider.new;
		self.label_val = StaticText.new;
		mlayout = HLayout.new;

		layout = VLayout.new;

		self.slider.value = self.spec.unmap(self.current_val);
		self.label_val.string = self.current_val.asString;
		self.slider.action = { arg slider;
			self.slider.value.debug("SLIDER");
			self.current_val = self.spec.map(slider.value);
			self.label_val.string = self.current_val.asString;
		};
		self.window.onClose = self.window.onClose.addFunc({
			self.exit_function(self.current_val.debug("ONE").asString)
		});
		layout.add(self.slider);
		layout.add(self.label_val);

		mlayout.add(layout);
		mlayout.add(self.make_spec_chooser_gui);

		self.window.view.keyDownAction = { arg view, char, modifiers, u, k; 
			//[char, modifiers, u, k].debug("KEYBOARD INPUT");
			if( u == 27 ) { // Esc
				view.close() 
			};
		};

		self.window.layout = mlayout;
		self.window.front;
	},

);
//~editor = ~class_number_editor_gui.new;
//~editor.make_gui;
//~class_number_editor_gui.new_pattern(100)

	

~inline_editor = { arg cmd, start, end, line, col, code;
	~class_inline_editor.compute_code(cmd, start, end, line, col, code);
};

~class_inline_editor = (
	compute_code: { arg self, cmd, start, end, line, col, code;
		[start, end, line, col, code].debug("H--HHHHHHHHHHHHHH");
		self[(\edit_++cmd).asSymbol].(self, start, end, line, col, code);
	},

	escape_code: { arg self, code;
		var a;
		a = code;
		a = a.escapeChar($\\);
		a = a.replace($\n.asString, "\\n");
		a = a.replace($".asString, $\\ ++ $");
		a;
		
	},

	vim_exec: { arg self, cmd;
		"vim --servername scvim --remote-send '<Esc>%<Enter>'".format(cmd).unixCmd;
	},

	replace_vim_block: { arg self, start, end, line, col, text;
		var filename = "/tmp/scvim_inline_editor";
		var file;
		if(File.exists(filename).not) {
			("mkfifo " ++ filename).unixCmd;
		};
		file = File(filename, "w");
		"before writbefore writee".debug;
		file.write(text);
		"AFTER writbefore writee".debug;
		file.close;
		self.vim_exec(":call SCreplaceTextBlock(%, %, %, %)".format(start, end, line, col));
		"AFTER VIM".debug;
	},

	edit_pbind_asso: { arg self, line, col, code;
		var key, val;
		#key, val = self.get_pbind_asso(line, code);
		code = self.replace_in_line(code, val, "150");
		
	},

	edit_number: { arg self, start, end, line, col, code;
		var dyn_code;
		~inline_editor_on_exit = { arg new_val;
			var res_code = self.replace_number(line, col, code, new_val);
			//self.replace_vim_block(start, end, self.escape_code(res_code));
			self.replace_vim_block(start, end, line, col, res_code);
		};
		[start, end, line, col, code].debug("HHHHHHHHHHHHHHH");
		dyn_code = self.replace_number(line, col, code, {arg x; "~class_number_editor_gui.new_pattern(~inline_editor_on_exit, "++ x ++")"});
		debug("CURRES("++code++")");
		dyn_code.interpret;
	},

	find_token_limit: { arg self, line, col, charlist, direction=\left;
		var char, pos;
		block { arg break;
			10000.do { arg x;
				if(direction == \left) {
					pos = col - x;
				} {
					pos = col + x;
				};
				if(pos == 0) {
					"left: not found".debug;
					break.value;
				};
				char = line[pos];
				char.debug("current char");
				if( charlist.includes(char) ) {
					char.debug("continue");
					// continue
				} {
					char.debug("found limit");
					break.value;
				}
			}
		};
		if(direction == \left) {
			pos+1;
		} {
			pos-1;
		}
	},

	replace_number: { arg self, linenum, col, code, replace;
		var lines = code.split($\n);
		var leftpos, rightpos;
		var charlist = "-.0123456789";
		var line;
		var token;
		
		line = lines[linenum];

		leftpos = self.find_token_limit(line, col, charlist, \left);
		rightpos = self.find_token_limit(line, col, charlist, \right);
		token = line.copyRange(leftpos,rightpos);
		line = line.replace(token, replace.(token));
		lines[linenum] = line;
		code = lines.join($\n);
		code;
	},

	////////////
	
	get_line: { arg self, linenum, code;
		var lines = code.split($\n);
		lines[linenum]
	},

	get_current_number: { arg self, linenum, col, code;
		var line = self.get_line(linenum, code);
		var leftpos, rightpos;
		var charlist = "-.0123456789";

		leftpos = self.find_token_limit(line, col, charlist, \left);
		rightpos = self.find_token_limit(line, col, charlist, \right);
		line.copyRange(leftpos,rightpos);

	},


	get_pbind_asso: { arg self;
		
	},

);

)
