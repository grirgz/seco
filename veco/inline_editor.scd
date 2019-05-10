
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

~class_seq_editor_gui = (
	new: { arg self;
		self = self.deepCopy;
	
		
		self.spec = \freq.asSpec;
		self.current_val = 100;
	
		self;
	},

	new_pattern: { arg self, exit_function, val, groupsize, supergroupsize, indentlevel;
		self = self.deepCopy;

		val.debug("seq new_pattern val");
		self.exit_function = { arg self, val; exit_function.(val) };
		self.current_val = val;
		self.groupsize = groupsize;
		self.supergroupsize = supergroupsize;
		self.indentlevel = indentlevel;

		self.make_gui;
		Prout({ 
			self.current_val.size.do { arg idx;
				self.current_val[idx].yield;
			}
		});
		
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

	format_seq: { arg self, val, groupsize=4, supergroupsize=2, indent=4;
		if(supergroupsize == 4) {
			val.clump(groupsize).clump(2).clump(2).collect{ arg supergroup;
				($\t ! indent).join ++ supergroup.collect({ arg group;
					group.collect({ arg subgroup;
						subgroup.collect({ arg item;
							item.asCompileString ++ ","
						}).join ++ " "
					}).join ++ " "
				}).join ++ $\n
			}.join
		} {
			val.clump(groupsize).clump(supergroupsize).collect{ arg supergroup;
				($\t ! indent).join ++ supergroup.collect({ arg group;
					group.collect({ arg item;
						item.asCompileString ++ ","
					}).join ++ " "
				}).join ++ $\n
			}.join
		}
	},

	make_gui: { arg self;
		var layout;
		var mlayout;
		var makebut = { arg step, idx;
			Button.new.states_([
				[ " " ],
				[ " ", Color.black, Color.green ],
			]).value_(step).action_({ arg but;
				self.current_val[idx] = but.value;
			}).maxWidth_(30)
		};
		self.window = Window.new;
		self.slider = Slider.new;
		self.label_val = StaticText.new;
		mlayout = HLayout.new;

		layout = VLayout.new;

		layout = {
			var counter = 0;
			VLayout(*self.current_val.clump(self.groupsize).clump(2).clump(2).collect{ arg supergroup;
				HLayout(*supergroup.collect({ arg group;
					HLayout(*group.collect({ arg subgroup;
						HLayout(*subgroup.collect({ arg item;
							var but = makebut.(item, counter);
							counter = counter+1;
							but;
						})).add(nil).spacing_(0)
					})).add(nil).margins_([10, 0]).spacing_(5)
				})).add(nil)
			}).add(nil)
		}.value;

		mlayout.add(
			layout
		);


		self.window.onClose = self.window.onClose.addFunc({
			self.exit_function(
				self.format_seq(
					self.current_val .debug("ONE"),
					self.groupsize,
					self.supergroupsize,
					self.indentlevel
				).debug("ONE end")
			)
		});

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

	

~inline_editor = { arg cmd, start, end, line, col, code, vistart, viend, visual;
	~class_inline_editor.compute_code(cmd, start, end, line, col, code, vistart, viend, visual);
};

~class_inline_editor = (
	compute_code: { arg self, cmd, start, end, line, col, code, vistart, viend, visual;
		[start, end, line, col, code, vistart, viend, visual].debug("H--HHHHHHHHHHHHHH");
		self[(\edit_++cmd).asSymbol].(self, start, end, line, col, code, vistart, viend, visual);
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

	replaceAt: { arg self, start, end, string;
		
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

	insert_after: { arg self, text;
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
		self.vim_exec(":r %".format(filename));
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
		debug("CURRES("++dyn_code++")");
		dyn_code.interpret;
	},

	analyse_seq: { arg self, code;
		var lines = code.split($\n);
		var indentlevel = lines[0].asArray.count({ arg x; x == $\t });
		var itemperline = lines[0].asArray.count({ arg x; x == $, });
		var groupsize, supergroupsize;
		#groupsize, supergroupsize = switch(itemperline,
			3, { [3,1] },
			4, { [4,1] },
			6, { [3,2] },
			8, { [4,2] },
			12, { [3,4] },
			16, { [4,4] }
		);
		[groupsize, supergroupsize, indentlevel].debug("analyse_seq");
	},

	edit_seq: { arg self, start, end, line, col, code, vistart, viend, visual;
		var dyn_code;
		~inline_editor_on_exit = { arg new_visual;
			var res_code = new_visual;
			//self.replace_vim_block(start, end, self.escape_code(res_code));
			// line is 0 because line variable is relative to start of code, not start of visual
			self.replace_vim_block(vistart, viend, 0, col, res_code);
		};
		[start, end, line, col, code].debug("HHHHHHHHHHHHHHH");
		dyn_code = self.replace_block(vistart - start, viend - start, code, { arg x; 
			var groupsize, supergroupsize, indentlevel;
			#groupsize, supergroupsize, indentlevel = self.analyse_seq(x);
			"~class_seq_editor_gui.new_pattern(~inline_editor_on_exit, %, %, %, %)".format(
				"[%]".format(x).debug("tointerpret").interpret,
				groupsize,
				supergroupsize,
				indentlevel
			)
		});
		debug("CURRES("++dyn_code++")");
		dyn_code.interpret;
		
	},

	edit_sample: { arg self;
		WindowDef(\filedialog).front(nil, { arg path; 
			path.debug("choosen path");
			path = BufDef.abspath_to_relpath(path.asString);
			path.debug("choosen path unresolved");
			self.insert_after("BufDef(\"%\")".format(path));
		})
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

	replace_block: { arg self, start, end, code, replace;
		var lines = code.split($\n);
		var block = List.new;
		(end - start).do {
			block.add(lines.removeAt(start))
		};
		block.add( lines[start] );
		lines[start] = replace.(block.join($\n));
		code = lines.join($\n);
		code;
	},

	get_number_token: { arg self, linenum, col, code, replace;
		var lines = code.split($\n);
		var leftpos, rightpos;
		var charlist = "-.0123456789";
		var line;
		var token, tokenstring;
		
		line = lines[linenum];

		leftpos = self.find_token_limit(line, col, charlist, \left);
		rightpos = self.find_token_limit(line, col, charlist, \right);
		tokenstring = line.copyRange(leftpos,rightpos);
		(
			all: code,
			line_leftpos: leftpos,
			line: line,
			linenum: linenum,
			col: col,
			line_rightpos: rightpos,
			string: token,
		);
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
