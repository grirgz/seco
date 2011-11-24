(

// ==========================================
// HELP FUNCTIONS
// ==========================================

~pedynscalar = { arg data, key, repeat = 100;
	Prout({ arg ev;
		repeat.do {
			ev = currentEnvironment[data][key].yield;
		};
	});
};

~pdynarray = { arg fun, userepeat=false;
	Prout({ arg ev;
		var idx;
		var val = 0;
		var repeat;
		userepeat.debug("=============================pdynarray:userepeat");
		ev[\is_noteline].debug("pdynarray:is_noteline");
		switch(userepeat,
			false, {
				repeat = inf;	
			},
			\noteline, {
				if( ev[\is_noteline] == true ) {
					repeat = ev[\repeat];
					if(repeat == 0) { repeat = inf };
				} {
					repeat = inf;
				};
			},
			\stepline, {
				if( ev[\is_noteline] == false ) {
					repeat = ev[\repeat];
					if(repeat == 0) { repeat = inf };
				} {
					repeat = inf;
				};
			}
		);
		repeat.debug("pdynarray:repeat");
		repeat.do {
			idx = 0;
			val = fun.(idx);
			//[val, idx].debug("pdynarray val idx");
			while( { val.notNil } , { 
				ev = val.yield;
				idx = idx + 1;
				val = fun.(idx);
			});
		}
	})
};


~make_event_key_reader = { arg argName, self;
	switch(argName, 
		\stepline, { 
			~pdynarray.( { arg idx; self.self.get_arg(argName)[idx] } );
		},
		\type, {
			Pif( Pkey(\stepline) > 0 , \note, \rest) // WTF with == ?????
		},
		//default:
		{
			//self.data[argName] = PatternProxy.new;
			Prout({ arg ev;
				var repeat = 100000;
				var argdata = self.get_arg(argName);
				var idx, val=0;
				repeat.do {
					switch( argdata.current_kind,
						\scalar, {
							ev = argdata.scalar.val.yield;
						},
						\seq, {
							idx = 0;
							val = argdata.seq.val[idx];
							while( { val.notNil } , { 
								ev = val.yield;
								idx = idx + 1;
								val = argdata.seq.val[idx];
							});
						}
					);
				}
			})
		}
	);
};

~player_get_arg = { arg self, argName;
	var ret;
	argName.dump;
	//self.get_args.do { arg an; an.debug("an====").dump };
	ret = if(self.get_args.includes(argName), {
		if([\type, \stepline].includes(argName), {
			self.data[argName];
		}, {
			//self.data[argName].source;
			self.data[argName];
		})
	}, {
		("ERROR: player: no such arg: " ++ argName ++ "!" ++ self).postln;
		nil;
	});
	//ret.debug("get_arg ret");
	ret;
};

~player_set_arg = { arg self, argName, val;
	if([\type, \stepline].includes(argName), {
		self.data[argName] = val;
	}, {
		//self.data[argName].source = val;
		self.data[argName] = val;
	})
};

// ==========================================
// SPECS
// ==========================================

// ControlSpec(minval = 0, maxval = 1, warp = 'lin', step = 0, default, units)
Spec.add(\dur, ControlSpec(4/128, 4, \lin, 4/64, 0.25, "s"));
Spec.add(\legato, ControlSpec(0, 1.2, \lin, 0, 0.707));
Spec.add(\sustain, ControlSpec(0.001, 5, \lin, 0, 0.2));
Spec.add(\repeat, ControlSpec(0, 100, \lin, 1, 0));
Spec.add(\pos, ControlSpec(0, 1, \lin, 0.0001, 0));
Spec.add(\amp, ControlSpec(0, 3, \lin, 0.0001, 0));

~get_spec = { arg argName, defname=nil, default_spec=\widefreq;
	var spec = nil;
	try { 
		spec = if( SynthDescLib.global.synthDescs[defname].metadata.specs[argName].notNil, {
			SynthDescLib.global.synthDescs[defname].metadata.specs[argName].asSpec;
		})
	};
	if(spec.isNil, {
		if( argName.asSpec.notNil, {
			spec = argName.asSpec;
		}, {
			spec = default_spec.asSpec;
		});
	});
	spec;
};

~get_special_spec = { arg argName, defname=nil, default_spec=\widefreq;
	var spec = nil;
	try { 
		spec = if( SynthDescLib.global.synthDescs[defname].metadata.specs[argName].notNil, {
			SynthDescLib.global.synthDescs[defname].metadata.specs[argName];
		})
	};
	if(spec.isNil) { [argName, defname].debug("get_special_spec: no spec found") };
	spec;
};

// ==========================================
// PARAM FACTORY
// ==========================================


~default_adsr = (
	attackTime:0.01,
	decayTime:0.1,
	sustainLevel:0.3,
	releaseTime:0.1,
	curve:0
);

~default_genadsr = (
	attackTime:0.1,
	decayTime:0.2,
	sustainLevel:0.3,
	releaseTime:0.4,
	curve:0,
	levelScale:1,
	levelBias: 0,
	timeScale: 1
);

~adsr_point_order = [ \attackTime, \decayTime, \sustainLevel, \releaseTime, \curve ];

~env_time = ControlSpec(0.000001, 5, 'exp', 0, 0.2, "s");
~spec_adsr = (
	attackTime:~env_time,
	decayTime:~env_time,
	sustainLevel:~env_time, //FIXME: not time
	releaseTime:~env_time,
	curve:ControlSpec(-9, 9, 'linear', 1, 0, "")
);


~adsr_event_to_env = { arg adsr;
	Env.performWithEnvir(\adsr, adsr);
};

~genadsr_event_to_env_array = { arg adsr;
	Env.performWithEnvir(\adsr, adsr).asArray ++ [adsr.levelScale, adsr.levelBias, adsr.timeScale]
};

~make_adsr_param = { arg name, player, default_value;
	var param;

	param = (
		val: ~default_adsr.deepCopy,
		name: name,
		classtype: \adsr,
		spec: ~spec_adsr,
		selected: 0,

		save_data: { arg self;
			var data = ();
			[\name, \classtype, \selected, \spec, \val].do {
				arg key;
				data[key] = self[key];
			};
			data;
		},

		load_data: { arg self, data;
			[\name, \classtype, \selected, \spec, \val].do {
				arg key;
				self[key] = data[key];
			};
		},

		select_param: { arg self;
			self.selected = 1;
			"env model select_param".debug;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			"env model deselect_param".debug;
			self.changed(\selected);
		},

		get_val: { arg self; self.val },

		get_param: { arg self, key;
			(
				name: key,
				spec: param.spec[key],
				classtype: { arg kself; self.classtype },
				selected: { arg kself;
					if(player.get_selected_param == name) { 1 } { 0 }
				},
				get_norm_val: { arg kself;
					param.spec[key].unmap( param.val[key] );
				},
				set_norm_val: { arg kself, val;
					param.val[key] = param.spec[key].map( val );
					param.changed(\val);
				},
				get_val: { arg kself;
					param.val[key];
				},
				set_val: { arg kself, val;
					param.val[key] = val;
					param.changed(\val);
				}
			)
		},

		refresh: { arg self;
			self.changed(\val);
			self.changed(\selected);
		},

		vpiano: { arg self;
			{
				~adsr_event_to_env.(self.val).asArray
			}
		},

		vpattern: { arg self;

			Pfunc({
				[ ~adsr_event_to_env.(self.val).asArray ]
			});
			//[ ~adsr_event_to_env.(self.val).asArray ]
		}

	);
	param;

};

~make_literal_param = { arg name, val;
	(
		name: name,
		classtype: \literal,
		get_val: val,

		refresh: { arg self;
			self.changed(\selected);
		},
		vpiano: val,
		vpattern: val
	);
};

~make_dur_param = { arg name, default_value;


};


~make_buf_param = { arg name, default_value, player, spec;

	var param;

	param = (
		val: nil,
		name: name,
		classtype: \buf,
		spec: nil,
		selected: 0,
		buffer: nil,

		save_data: { arg self;
			var data = ();
			[\name, \classtype, \selected, \spec, \val].do {
				arg key;
				data[key] = self[key];
			};
			data.debug("make_buf_param: save_data: data");
			data;
		},

		load_data: { arg self, data;
			[\name, \classtype, \selected, \spec].do {
				arg key;
				self[key] = data[key];
			};
			self.set_val(data[\val]);
		},

		select_param: { arg self;
			self.selected = 1;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		get_val: { arg self; self.val },

		new_buffer: { arg self, path;
			[player.uid, path].debug("new_buffer: player.uname, path");
			self.buffer = BufferPool.get_sample(player.uid, path);
		},

		set_val: { arg self, val;
			if(val != self.val, {
				self.new_buffer(val);
				self.val = val;
				self.changed(\val);
			});
			self.val.debug("set_val: val");
		},


		refresh: { arg self;
			self.changed(\val);
			self.changed(\selected);
		},

		vpiano: { arg self;
			{ self.buffer.bufnum };	
		},

		vpattern: { arg self;
			
			self.buffer.bufnum.debug("BUFNUM");	
			Pfunc({self.buffer.bufnum;});	
		}

	);
	param.set_val(default_value);
	param;

};

~empty_note = (
	midinote: \rest,
	sustain: 0.1,
	dur: 0.01
);

~default_noteline3 = [ // FIXME: crash when no notes
	(
		midinote: \rest,
		sustain: 0.1,
		start_silence: 0.5,
		default_start_silence: 0.5,
		end_silence: 2.0,
		default_end_silence: 2.0,
		start_offset: 0,
		end_offset: 0,
		dur: 0.5
	),
	(
		midinote: 68,
		sustain: 0.1,
		dur: 1.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 2.0
	),
];
~default_noteline = [ // FIXME: crash when no notes
	(
		midinote: \rest,
		sustain: 0.1,
		start_silence: 0.5,
		default_start_silence: 0.5,
		end_silence: 0.5,
		default_end_silence: 0.5,
		start_offset: 0,
		end_offset: 0,
		dur: 0.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 0.9
	),
	(
		midinote: 67,
		sustain: 0.1,
		dur: 0.1
	),
	(
		midinote: 68,
		sustain: 0.1,
		dur: 1.5
	),
	(
		midinote: 64,
		sustain: 0.1,
		dur: 0.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 0.5
	),
];
~default_noteline2 = [
	(
		midinote: 64,
		sustain: 0.1,
		dur: 0.5
	),
	(
		midinote: 65,
		sustain: 0.1,
		dur: 0.4
	),
	(
		midinote: 66,
		sustain: 0.1,
		dur: 0.9
	),
	(
		midinote: 67,
		sustain: 0.1,
		dur: 0.1
	),
	(
		midinote: 68,
		sustain: 0.1,
		dur: 1.5
	),
	(
		midinote: 69,
		sustain: 0.1,
		dur: 0.6
	)
];

~make_noteline_param = { arg name, default_value=[];
	var ret;
	ret = (
		name: name,
		classtype: \noteline,
		selected_cell: 0,
		selected: 0,
		default_val: default_value.asList,
		notes: ~default_noteline3.deepCopy,
		start_offset: 0,
		end_offset: 0,
		muted: false,
		archive_data: [\name, \classtype, \selected, \selected_cell, \default_val, \notes, \start_offset, \end_offset, \notequant],
		notequant: nil,
		vnotes: [],

		save_data: { arg self;
			var data = ();
			self.archive_data.do {
				arg key;
				data[key] = self[key];
			};
			[\seq].do { arg kind;
				data[kind] = ();
				[\val].do { arg key;
					data[kind][key] = self[kind][key];
				}
			};
			data;
		},

		load_data: { arg self, data;
			self.archive_data.do {
				arg key;
				self[key] = data[key];
			};
			[\seq].do { arg kind;
				[\val].do { arg key;
					 self[kind][key] = data[kind][key];
				}
			};
		},

		seq: (
			val: default_value.asList,
			change: { arg self, fun;
				self.val = fun.(self.val);
			}
		),
		get_cells: { arg self;
			self.seq.val;
		},
		get_selected_cell: { arg self;
			self.selected_cell;
		},
		select_param: { arg self;
			self.selected = 1;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		add_cells: { arg self, cells;
			self.seq.val.addAll(cells);
			self.changed(\cells);
		},

		remove_cells: { arg self, num;
			self.seq.val = self.seq.val[.. (self.seq.val.size - num - 1) ];
			self.changed(\cells);
		},

		set_val: { arg self, val;
			self.seq.val[ self.get_selected_cell.() ] = if(val > 1, { 1 },{ 0 });
		},

		get_notes: { arg self;
			var no;
			no = self.vnotes.deepCopy;
			//no[0].dur+self.start_offset;
			//no.last.dur+self.end_offset;
			no;
		},

		set_notes: { arg self, val;
			self.notes = val;
			self.update_note_dur;
		},

		get_note: { arg self, idx;
			var no;
			if( self.vnotes.size > 0 && {self.muted.not}) {
				no = self.vnotes[idx].deepCopy;
				if( no.notNil && (self.notequant.notNil) ) {
					no.dur = no.dur.round(self.notequant);
					no;
				} {
					no
				}
			} {
				"noteline_param: get_node: No notes".inform;
				if(idx == 0) {
					~empty_note;
				} {
					nil;
				};
			}
		},

		total_dur: { arg self, notes;
			var res=0;
			notes.do { arg x; res = x.dur + res; };
			res;
		},

		update_note_dur: { arg self;
			var find_next, find_prev, delta, prevdelta, idx, previdx;
			"update_note_dur".debug("start");
			if( self.notes.size > 2) {
				find_next = { arg dur, notes;
					var res=0, vidx=0, last=nil, delta=0, prevdelta=0;
					dur.debug("find_next: dur");
					if(dur == 0) {
						delta = 0;
						vidx = 0
					} {
						notes[1..].do { arg x, n;
							[n, res, vidx, last, dur].debug("begin n res vidx last");
							if( res < dur ) {
								res = x.dur + res;
								vidx = vidx + 1;
								last = x.dur;
							};
							[n, res, vidx, last].debug("end");
						};
						delta = res - dur;
					};
					[ delta, vidx+1 ];
				};

				find_prev = { arg dur, notes;
					var res=0, vidx=0, last=nil, delta=0, prevdelta=0;
					dur = self.total_dur( notes[1..(notes.lastIndex-1)] ).debug("total dur") - dur;
					dur.debug("find_prev: dur");

					notes[1..].do { arg x, n;
						[n, res, vidx, last].debug("begin n res vidx last");
						if( res <= dur ) {
							res = x.dur + res;
							vidx = vidx + 1;
							last = x.dur;
						};
						[n, res, vidx, last].debug("end");
					};
					delta = res - dur;
					if(last.isNil) {
						prevdelta = nil
					} {
						last.debug("last");
						prevdelta = last - delta;
					};
					[ prevdelta, vidx ];
				};

				#delta, idx = find_next.(self.notes[0].start_offset, self.notes);
				#prevdelta, previdx = find_prev.(self.notes[0].end_offset, self.notes);
				[delta, idx, prevdelta, previdx].debug("delta, idx, prevdelta, previdx");
				self.notes[0].dur = self.notes[0].start_silence + delta;
				self.vnotes = [self.notes[0]] ++ self.notes[idx..previdx].deepCopy;
				self.vnotes[self.vnotes.lastIndex].dur = self.notes[0].end_silence + prevdelta;
				self.vnotes.debug("update_note_dur: vnotes");
				self.changed(\notes);
			} {
				if(self.notes.size == 2) {
					self.vnotes = self.notes.deepCopy;
					if( (self.notes[0].start_silence + self.notes[0].end_silence) < 0.01 ) {
						"Protection anti infinite loop: setting end_silence to 0.5".error;
						self.notes[0].end_silence = 0.5
					};
					self.vnotes[0].dur = self.notes[0].start_silence;
					self.vnotes[self.vnotes.lastIndex].dur = self.notes[0].end_silence;
					self.vnotes.debug("update_note_dur: vnotes");
					self.changed(\notes);
				}
			}
					
		},

		set_start_offset: { arg self, val;
			var dur;
			if(self.notes.size > 2 ) {
				dur = self.total_dur( self.notes[1..(self.notes.lastIndex-1)] );
				if( val >= 0 && (val < (dur - self.notes[0].end_offset)) ) {
					[val, dur, self.notes[0].end_offset, dur - self.notes[0].end_offset].debug("set start_offset: val, dur, eo, dur-eo");
					self.notes[0].start_offset = val;
					self.update_note_dur;
				} {
					[val, dur, self.notes[0].end_offset, dur - self.notes[0].end_offset].debug("can't set start_offset: val, dur, eo, dur-eo");
				}
			} {
				"You are stupid!".debug;
			}
		},

		set_end_offset: { arg self, val;
			var dur;
			if(self.notes.size > 0) {
				dur = self.total_dur( self.notes[1..(self.notes.lastIndex-1)] );
				if( val >= 0 && (val < (dur - self.notes[0].start_offset)) ) {
					[val, dur, self.notes[0].end_offset, dur - self.notes[0].end_offset].debug("can't set end_offset: val, dur, so, dur-so");
					self.notes[0].end_offset = val;
					self.update_note_dur;
				} {
					[val, dur, self.notes[0].start_offset, dur - self.notes[0].start_offset].debug("can't set end_offset: val, dur, so, dur-so");
				}
			}
		},

		get_start_offset: { arg self;
			if(self.notes.size > 0) {
				self.notes[0].start_offset;
			}
		},

		get_end_offset: { arg self;
			if(self.notes.size > 0) {
				self.notes[0].end_offset;
			}
		},

		set_start_silence: { arg self, val;

			if(self.notes.size > 0 && (val >= 0)) {
				self.notes[0].start_silence = val;
				self.update_note_dur;
			} {
				[val, self.notes.size].debug("can't set start_silence: val, notessize");
			}
		},

		set_end_silence: { arg self, val;

			if(self.notes.size > 0 && (val >= 0)) {
				self.notes[0].end_silence = val;
				self.update_note_dur;
			} {
				[val, self.notes.size].debug("can't set end_silence: val, notessize");

			};
		},

		get_start_silence: { arg self;
			if(self.notes.size > 0) {
				self.notes[0].start_silence;
			}

		},

		get_end_silence: { arg self;
			if(self.notes.size > 0) {
				self.notes[0].end_silence;
			}

		},

		set_first_note_dur: { arg self, val;
			var res = 0, lastdur = 0, vidx = 0;
			if( self.notes.size > 0) {
				self.notes[0].dur = val;
				if( val < 0 ) {
					self.notes.do { arg x;
						if( res < val.neg ) {
							res = x.dur + res;
							vidx = vidx + 1;
							lastdur = x.dur;
						}
					};
					self.notes[0].virtual_start_idx = vidx -1;
					self.notes[0].virtual_start_dur = res - val.neg;
				}
			}
		},

		get_first_note_dur: { arg self;
			self.notes.size.debug("get_first_note_dur self.notes.size");
			if( self.notes.size > 0) {
				self.notes[0].dur;
			} {
				nil
			}
		},

		set_notequant: { arg self, val;
			self.notequant = val;
			self.changed(\notes);
		},

		tick: { arg idx; 
			//"TICK!".postln;
		},

		mute: { arg self, val;
			self.muted = val;
		},

		toggle_cell: { arg self, idx;
			var oldsel;
			[idx, self.get_cells].debug("make_control_param.select_cell idx, selg.get_cells");
			if( idx < self.get_cells.size, {
				//oldsel = self.selected_cell;
				self.selected_cell = idx;
				//self.changed(\selected_cell, oldsel);
				self.seq.val[ idx ] = ~toggle_value.(self.seq.val[ idx ]);
				self.changed(\val, self.selected_cell);
			})
		},
		refresh: { arg self;
			self.update_note_dur;
			self.get_notes.debug("noteline param refresh: get_notes");
			self.changed(\notes);
			self.changed(\selected);
		},
		vpattern: { arg self; 
			//"YE SOUI PREMIER!!!!".debug;
			"--------making vpattern of noteline".debug;
			~pdynarray.( { arg idx; self.tick; self.get_note(idx) }, \noteline );
			//self.get_note(0);
		};

	);
	ret;
};

~make_stepline_param = { arg name, default_value;
	var ret;
	ret = (
		name: name,
		classtype: \stepline,
		selected_cell: 0,
		selected: 0,
		default_val: default_value.asList,

		save_data: { arg self;
			var data = ();
			[\name, \classtype, \selected, \selected_cell, \default_val].do {
				arg key;
				data[key] = self[key];
			};
			[\seq].do { arg kind;
				data[kind] = ();
				[\val].do { arg key;
					data[kind][key] = self[kind][key];
				}
			};
			data;
		},

		load_data: { arg self, data;
			[\name, \classtype, \selected, \selected_cell, \default_val].do {
				arg key;
				self[key] = data[key];
			};
			[\seq].do { arg kind;
				[\val].do { arg key;
					 self[kind][key] = data[kind][key];
				}
			};
		},

		seq: (
			val: default_value.asList,
			change: { arg self, fun;
				self.val = fun.(self.val);
			}
		),
		get_cells: { arg self;
			self.seq.val;
		},
		get_selected_cell: { arg self;
			self.selected_cell;
		},
		select_param: { arg self;
			self.selected = 1;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		add_cells: { arg self, cells;
			self.seq.val.addAll(cells);
			self.changed(\cells);
		},

		remove_cells: { arg self, num;
			self.seq.val = self.seq.val[.. (self.seq.val.size - num - 1) ];
			self.changed(\cells);
		},

		set_val: { arg self, val;
			self.seq.val[ self.get_selected_cell.() ] = if(val > 1, { 1 },{ 0 });
		},



		tick: { arg idx;
		//"TICK!".postln;
		},

		toggle_cell: { arg self, idx;
			var oldsel;
			[idx, self.get_cells].debug("make_control_param.select_cell idx, selg.get_cells");
			if( idx < self.get_cells.size, {
				//oldsel = self.selected_cell;
				self.selected_cell = idx;
				//self.changed(\selected_cell, oldsel);
				self.seq.val[ idx ] = ~toggle_value.(self.seq.val[ idx ]);
				self.changed(\val, self.selected_cell);
			})
		},
		refresh: { arg self;
			self.changed(\cells);
			self.changed(\selected);
		},
		vpattern: { arg self; 
			"--------making vpattern of stepline".debug;
			~pdynarray.( { arg idx; 
				self.tick(idx); 
				if(self.seq.val[idx] == 1, { [idx, TempoClock.beats, TempoClock.beatInBar].debug("----------TICK step"); });
				self.seq.val[idx];
			}, \stepline );
			//Pseq([1,1,1,1],inf);
		};
	);
	ret;
};

~make_type_param = { arg name;
	var ret;
	ret = (
		name: name,
		classtype: \type,
		refresh: { arg self; },
		vpattern: { arg self; 
			Pfunc({ arg ev;
				if(ev[\muted]) {
					\rest
				} {
					if(ev[\stepline] > 0) {
						\note
					} {
						\rest
					}
				}
			})
		}
	);
	ret;
};

~make_volume_param = { arg name;
	var param = (
		classtype: \volume,

		save_data: { arg self;
			var data = Dictionary.new;
			data[\volume] = s.volume.volume;
		},

		load_data: { arg self, data;
			s.volume.volume = data[\volume];
		},

		get_norm_val: { arg self;
			self.spec.unmap(s.volume.volume)
		},
		set_norm_val: { arg self, val;
			s.volume.volume = self.spec.map(val);
		},
		get_cells: { arg self;
			[s.volume.volume]
		},
		refresh: { arg self;
			self.changed(\cells);
		},
		spec: \db.asSpec,
		selected: 1

	);
	param;

};

~make_control_param = { arg name, kind, default_value, spec;
	var param;
	var bar_length = 4;

	param = (
		name: name,
		classtype: \control,
		current_kind: kind,
		spec: spec,
		selected: 0,	 // bool
		selected_cell: 0,
		bar_length: bar_length,
		default_val: default_value,
		noteline: false,
		archive_data: [\name, \classtype, \current_kind, \spec, \selected, \selected_cell, \default_val, \noteline],

		save_data: { arg self;
			var data = ();
			self.archive_data.do {
				arg key;
				data[key] = self[key];
			};
			[\seq, \scalar, \preset].do { arg kind;
				data[kind] = ();
				[\val, \selected_cell].do { arg key;
					data[kind][key] = self[kind][key];
				}
			};
			data;
		},

		load_data: { arg self, data;
			self.archive_data.do {
				arg key;
				self[key] = data[key];
			};
			[\seq, \scalar, \preset].do { arg kind;
				[\val, \selected_cell].do { arg key;
					 self[kind][key] = data[kind][key];
				}
			};
		},

		seq: (
			val: if(default_value.isArray, { default_value.asList }, { (default_value ! bar_length).asList }),
			selected_cell: 0,
			initialized: false,

			init: { arg self, defval=default_value;
				param.default_val = defval;
				self.val = (defval ! bar_length).asList;
				self.initialized = true;
			},

			//FIXME: handle others "out of range" exceptions
			set_norm_val: { arg self, norm_val, idx=nil;
				idx = idx ?? self.selected_cell;
				self.val.wrapPut(idx, param.spec.map(norm_val));
				param.changed(\val, idx % self.val.size);
			},
			get_norm_val: { arg self, idx=nil;
				idx = idx ?? self.selected_cell;
				param.spec.unmap(self.val.wrapAt(idx));
			},
			set_val: { arg self, val, idx=nil;
				idx = idx ?? self.selected_cell;
				self.val.wrapPut(idx, val);
				param.changed(\val, idx % self.val.size);
			},
			get_val: { arg self, idx=nil;
				idx = idx ?? self.selected_cell;
				self.val.wrapAt(idx);
			},

			add_cells: { arg self, cells;
				self.val.addAll(cells);
				param.changed(\cells);
			},

			remove_cells: { arg self, num;
				self.val = self.val[.. (self.val.size - num - 1) ];
				param.changed(\cells);
			},

			get_cells: { arg self; self.val },

			select_cell: { arg self, idx;
				var oldsel = self.selected_cell;
				oldsel.debug("this is oldsel from seq");
				self.selected_cell = idx;
				param.changed(\selected_cell, oldsel); // view clear old selection and call get_selected_cell to get new selection
			},

			get_selected_cell: { arg self;
				self.selected_cell;
			},

			change: { arg self, fun;
				self.val = fun.(self.val);
				param.changed(\cells);
			}
		),

		scalar: (
			selected_cell: 0, // always 0
			val: if(default_value.isArray, { default_value[0] }, { default_value }),

			set_val: { arg self, val, idx=nil;
				self.val = val;
				param.changed(\val, 0);
			},
			get_val: { arg self; self.val },

			set_norm_val: { arg self, norm_val;
				self.val = param.spec.map(norm_val);
				param.changed(\val, 0);
			},
			get_norm_val: { arg self;
				param.spec.unmap(self.val);
			},

			get_cells: { arg self; [self.val] },

			select_cell: { arg self, idx; param.seq.selected_cell = idx }, // when changing kind, correct cell is selected in colselect mode
			get_selected_cell: { arg self; 0 },
			add_cells: {},
			remove_cells: {}
		),

		// preset subobject here
		//		need a corresponding spec

		select_param: { arg self;
			self.selected = 1;
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		// ============== polymorph API

		select_cell: { arg self, idx;
			idx.debug("called select_cell!!!");
			self[self.current_kind].select_cell(idx);
		},

		get_selected_cell: { arg self;
			self[self.current_kind].get_selected_cell
		},

		set_norm_val: { arg self, val;
			self[self.current_kind].set_norm_val(val)
		},

		set_val: { arg self, val;
			self[self.current_kind].set_val(val)
		},

		get_norm_val: { arg self;
			self[self.current_kind].get_norm_val
		},

		get_val: { arg self;
			self[self.current_kind].get_val
		},

		add_cells: { arg self, cells;
			self[self.current_kind].add_cells(cells)
		},

		remove_cells: { arg self, num;
			self[self.current_kind].remove_cells(num)
		},

		get_cells: { arg self;
			self[self.current_kind].get_cells
		},

		// ===================
		
		change_kind: { arg self, kind;
			self.current_kind = kind;
			if(kind == \seq && { self.seq.initialized.not } ) { self.seq.init(self.scalar.get_val) };
			[name, kind].debug("CHANGED KIND!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			self.changed(\kind);
		},

		refresh: { arg self;
			self.changed(\kind);
			self.changed(\selected);
			self.changed(\cells);
		},

		vpiano: { arg self;
			{
				switch(self.current_kind,
					\preset, { self.preset.val[self.preset.selected_cell] },
					//default
					{ self.scalar.val }
				);
			}
		},

		vpattern: { arg self; 
			var segf, scalf, pref;
			switch(self.name,
				\dur, {
					segf = { arg ev;
						ev[\noteline].dur * ev[\stretchdur];
					};
					scalf = pref = segf;
				},
				\freq, {
					segf = { arg ev;
						ev[\noteline].midinote.midicps;	
					};
					scalf = pref = segf;
				},
				\sustain, {
					segf = { arg ev;
						ev[\noteline].sustain;	
					};
					scalf = pref = segf;
				},
				// else
				{
					segf = { arg ev;
						//[ev[\elapsed], ev[\segdur], ev.dump].debug("in segggggggggggggggggg");
						self.seq.val.blendAt((ev[\elapsed]/ev[\segdur]) % (self.seq.val.size -1));
					};
					scalf = { arg ev; self.scalar.val };
					pref = { arg ev; self.preset.val[self.preset.selected_cell] };
				}
			);
			Prout({ arg ev;
				var repeat = 1000000;
				var idx, val=0;
				repeat.do {
					//[name, ev].debug("################################## prout!!!!");
					//ev.dump;
					switch( self.current_kind,
						\scalar, {
							//ev.debug("=========== in scalar ev");
							if(self.noteline, {
								ev = scalf.value(ev).yield;
							}, {
								ev = self.scalar.val.yield;
							});
							//ev.debug("=========== in scalar ev END");
						},
						\seq, {
							if(self.noteline, {
								//ev.debug("=========== in noteline ev");
								//segf.debug("=========== in noteline segf");
								//segf.value(ev).debug("=========== in noteline");
								ev = segf.value(ev).yield;
							}, {
								idx = 0;
								val = self.seq.val[idx];
								while( { val.notNil } , { 
									ev = val.yield;
									idx = idx + 1;
									val = self.seq.val[idx];
								});
							});
							//ev.debug("=========== in seq ev END");
						},
						\preset, {
							if(self.noteline, {
								ev = pref.value(ev).yield;
							}, {
								ev = self.preset.val[self.preset.selected_cell].yield
							});
						}
					);
				}
			});
			//Prout({
		//		inf.do { self.scalar.get_val.yield };
			//});
			//Pseq([self.get_val],inf);
			//Pseq([self.scalar.get_val],inf);
		}
	);
	// init
	param.preset = param.seq.deepCopy;
	param.seg = param.seq;

	// \dur special case
	if([\dur,\segdur, \stretchdur].includes(name), {
		param.change_kind(\preset);
		param.preset.val = List[ 4, 2, 1, 0.5, 0.25, 0.125, 0.0625 ];
		if(name == \stretchdur, {
			param.select_cell(2);
		}, {
			param.select_cell(4);
		});
	});

	// return object
	param;
};

// ==========================================
// PLAYER FACTORY
// ==========================================



~make_player_from_synthdef = { arg main, defname, data=nil;
	// changed messages: \redraw_node
	var player;
	var desc = SynthDescLib.global.synthDescs[defname];
	if(desc.isNil, {
		("ERROR: make_player_from_synthdef: SynthDef not found: "++defname).error
	});
	defname.debug("loading player from");
	desc.debug("synthDescs");
	player = (
		bank: 0,
		defname: defname,
		//uname: defname ++ UniqueID.next,
		uname: nil,
		noteline: false,
		node: EventPatternProxy.new,
		to_destruct: List.new,
		//name: defname,
		name: nil,
		kind: \player,
		uid: UniqueID.next,
		playlist: List.new,
		sourcepat: nil,
		selected_param: \stepline,
		wrapper: nil,
		sourcewrapper: nil,
		playing_state: \stop,
		muted: false,
		archive_data: [\control, \stepline, \adsr, \noteline, \buf],

		init: { arg self;

			self.data = {
					// use args and defaults values from synthdef to build data dict
					// if data dict given, deep copy it instead
					var dict;
					dict = Dictionary.new;
					if( data.isNil, {
						desc.controls.do({ arg control;
							var name = control.name.asSymbol;
							control.name.debug("making player data name");
							control.defaultValue.debug("making player data");
							control.defaultValue.isArray.debug("making player data");
							case
								{ (name == \adsr) || name.asString.containsStringAt(0, "adsr_") } {
									dict[name] = ~make_adsr_param.(
										name,
										self,
										control.defaultValue
									)
								}
								{ name == '?' } { 
									// skip
									// FIXME: what is it for ?
								}
								{ (name == \bufnum) || name.asString.containsStringAt(0, "bufnum_") } {
									dict[name] = ~make_buf_param.(name, "sounds/default.wav", self, ~get_special_spec.(name, defname));
									self.to_destruct.add(dict[name]);
								}
								//default
								{ true } { 
									dict[name] = ~make_control_param.(
										name,
										\scalar,
										control.defaultValue,
										~get_spec.(name, defname)
									)
								};
						});
					}, {
						dict = data.deepCopy;
					});
					dict;
			}.value;

			self.data[\noteline] = self.data[\noteline] ?? ~make_noteline_param.(\noteline);

			self.data[\dur] = self.data[\dur] ?? ~make_control_param.(\dur, \scalar, 0.5, ~get_spec.(\dur, defname));
			self.data[\segdur] = self.data[\segdur] ?? ~make_control_param.(\segdur, \scalar, 0.5, ~get_spec.(\dur, defname));
			self.data[\stretchdur] = self.data[\stretchdur] ?? ~make_control_param.(\stretchdur, \scalar, 1, ~get_spec.(\dur, defname));
			self.data[\legato] = self.data[\legato] ?? ~make_control_param.(\legato, \scalar, 0.5, ~get_spec.(\legato, defname));
			self.data[\sustain] = self.data[\sustain] ?? ~make_control_param.(\sustain, \scalar, 0.5, ~get_spec.(\sustain, defname));
			self.data[\repeat] = self.data[\repeat] ?? ~make_control_param.(\repeat, \scalar, 1, ~get_spec.(\repeat, defname));

			self.data[\stepline] = self.data[\stepline] ?? ~make_stepline_param.(\stepline, 1 ! 8 );
			self.data[\instrument] = self.data[\instrument] ?? ~make_literal_param.(\instrument, defname);
			self.data[\type] = ~make_type_param.(\type);

			//TODO: handle t_trig arguments

			self.sourcepat = {
				var dict = Dictionary.new;
				var list = List[];
				var prio;
				prio = [\repeat, \instrument, \stretchdur, \noteline, \stepline, \type, \dur, \segdur, \legato, \sustain];

				list.add(\elapsed); list.add(Ptime.new);
				list.add(\is_noteline); list.add(Pfunc({ self.noteline }));
				list.add(\muted); list.add(Pfunc({ self.muted }));
				prio.do { arg key;
					list.add(key); list.add( self.data[key].vpattern );
				};
				self.data.keys.difference(prio).do { arg key;
					list.add(key); list.add( self.data[key].vpattern );
				};
				list.debug("maked pbind list");
				//[\type, \stepline, \instrument].do { arg x; list.add(x); list.add(dict[x]) };
				//list.debug("maked pbind list");
				//Pbind(*list).dump;
				//Pbind(*list).trace;
				Pbind(*list);
			}.value;
		},

		set_playing_state: { arg self, state;
			self.playing_state = state;
			self.changed(\redraw_node);
		},

		get_playing_state: { arg self;
			switch(self.muted,
				true, {
					switch(self.playing_state,
						\play, { \mute },
						\stop, { \mutestop }
					)
				},
				false, {
					switch(self.playing_state,
						\play, { \play },
						\stop, { \stop }
					)
				}
			)
		},

		mute: { arg self, val=true;
			if(val != self.muted) {
				self.muted = val;
				self.changed(\redraw_node);
			}
		},

		get_piano: { arg self;
			var exclu, list = List[];
			exclu = [\instrument, \noteline, \amp, \freq, \stepline, \type, \dur, \segdur, \legato, \sustain];
			self.data.keys.difference(exclu).do { arg key;
				var val = self.data[key].vpiano ?? self.data[key].vpattern;
				list.add(key); list.add( val ) 
			};
			{ arg freq, veloc; 
				Synth(self.data[\instrument].vpiano, (
					[\freq, freq, \amp, self.data[\amp].vpiano.value * veloc ] ++
						list.collect(_.value)).debug("arg listHHHHHHHHHHHHHHHHHHHHHHHHHHH")) 
			}

		},

		destructor: { arg self;
			// FIXME: implement it correctly
			self.to_destruct.do { arg i;
				i.destructor;
			}
		},

		edit_wrapper: { arg self;
			var tmp, file;
			tmp = "tempfile -s .sc -p seco".unixCmdGetStdOut;
			tmp = tmp.split($\n)[0];
			tmp.debug("tmp");
			if(self.sourcewrapper.notNil) {
				file = File.new(tmp, "w");
				file.write(self.sourcewrapper);
				file.close;
			};
			("xterm -r -fn 10x20 -e \"vim -c 'set ft=supercollider' "++tmp++"\"").unixCmd({
				var file, code, res;
				file = File.new(tmp, "r");
				code = file.readAllString;
				file.close;
				code.debug("code");
				self.set_wrapper_code(code);
				File.delete(tmp);
			});
		},

		set_wrapper: { arg self, pat, code=nil;
			code.debug("set_wrapper");
			self.wrapper = pat;
			if(code.notNil) { self.sourcewrapper = code };
		},

		set_wrapper_code: { arg self, code;
			var res, env;
			env = (pat:self.sourcepat);
			res = env.use({code.interpret});
			res.postcs;
			if(res.notNil) {
				if(res == false) {
					self.set_wrapper(nil, code);
				} {
					self.set_wrapper(res, code);
				}
			} {
				"Interpretation of wrapper file FAILED!".error;
			};
		},

		set_noteline: { arg self, set;
			self.data.keysValuesDo { arg key, val;
				if(val.classtype == \control, {
					[val.name, set].debug("setting noteline");
					val.noteline = set
				})
			};
			self.noteline = set;
		},

		clone: { arg self;
			var pl;
			pl = ~make_player_from_synthdef.(main,defname);
			pl.load_data( self.save_data.deepCopy );
			pl;
		},
		map_arg: { arg self, argName, val;
			argName.debug("mapping hidden!!!");
			~get_spec.(argName, defname).map(val);
		},
		unmap_arg: { arg self, argName, val;
			~get_spec.(argName, defname).unmap(val);
		},

		get_args: { arg self;
			self.data.keys
		},

		set_bank: { arg self, bank;
			self.bank = bank;
			self.data.do { arg x; x.changed(\cells); };
		},

		get_bank: { arg self;
			self.bank;
		},

		save_data: { arg self;
			var argdat;
			var data = ();
			data.args = ();
			self.get_args.do { arg key;
				argdat = self.get_arg(key);	
				if(self.archive_data.includes(argdat.classtype), {
					data.args[key] = argdat.save_data
				})
			};
			data.name = defname;
			data.defname = defname;
			data.bank = self.bank;
			data.noteline = self.noteline;
			data.sourcewrapper = self.sourcewrapper;
			data;
		},

		load_data: { arg self, data;
			var argdat;
			self.get_args.do { arg key;
				argdat = self.get_arg(key);	
				if(self.archive_data.includes(argdat.classtype), {
					argdat.load_data( data.args[key] )
				})
			};
			self.bank = data.bank;
			self.set_wrapper_code(data.sourcewrapper);
			self.set_noteline(data.noteline ?? false);
		},

		save_column_preset: { arg self;
			var data = ();
			data.defname = self.defname;
			self.data.keysValuesDo { arg key, val;
				if([\control].includes(val.classtype) ) {
					data[key] = val.get_val;
				};
			};
			data;
		},

		load_column_preset: { arg self, data, kind=\seq;
			self.data.keysValuesDo { arg key, val;
				if( data[key].notNil ) {
					if( val.current_kind == kind ) {
						[key, data[key]].debug("load_column_preset");
						val.set_val(data[key]);
					}
				};
			};
		},

		as_event: { arg self;
			var ev = ();
			self.data.keysValuesDo { arg key, val;
				ev[key] = val.get_val;
			};
			ev;
		},

		vpattern: { arg self;
			self.wrapper.debug("vpattern called: wrapper");
			if(self.wrapper.notNil) {
				self.wrapper;
			} {
				self.sourcepat;
			}
		},

		vpattern_loop: { arg self;
			Pn(self.vpattern, inf);
		},

		prepared_node: { arg self;
			self.node.source = self.vpattern;
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++setting source");
			self.node;
		},

		play_node: { arg self;
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++setting source(inf)");
			main.playmanager.play_node(self.uname);
			//self.node.play;
		},

		stop_node: { arg self;
			main.playmanager.stop_node(self.uname);
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++stop");
			//self.node.source = nil;
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++niling source");
		},

		play_repeat_node: { arg self; 
			var rep;
			rep = self.data[\repeat].get_val;
			self.data[\repeat].set_val(0);
			self.node.play;
			fork {
				1.wait; //FIXME: use quant
				self.data[\repeat].set_val(rep);
			};
		},

		select_param: { arg self, name;
			var oldsel;
			if( self.data[name].notNil ) {
				oldsel = self.selected_param;
				name.debug("player selected_param");
				self.selected_param = name;
				self.data[oldsel].changed(\selected);
				self.data[name].changed(\selected);
			} {
				[self.uname, name].debug("can't select param: not found");
			}
		},

		get_selected_param: { arg self;
			self.selected_param;
		},

		get_arg: ~player_get_arg,
		set_arg: ~player_set_arg

	);
	player.init;
	player;
};

~make_player_from_colpreset = { arg main, data;
	var pl;
	pl = ~make_player_from_synthdef.(main, data.defname);
	pl.load_column_preset(data, \scalar);
	pl;
};

~make_player_from_patfun = { arg patfun, data=nil;
	var player;
	player = (
		init: { arg self;

			self.data = {
					// use args and defaults values from synthdef to build data dict
					// if data dict given, deep copy it instead
					var dict;
					dict = Dictionary.new;
					if( data.isNil, {
						patfun.argNames.do({ arg argName, idx;
							dict[argName] = patfun.defaultArgs[idx];
						});
					}, {
						dict = data.deepCopy;
					});
					dict;
			}.value;

			self.node.source = patfun.valueArray( patfun.argNames.collect({ arg argName;
				~make_event_key_reader.(argName, self)
			}));
		},
		patfun: { arg self; patfun; },
		clone: { arg self;
			~make_player_from_patfun.(patfun, self.data);
		},
		map_arg: { arg self, argName, val;
			// TODO: how to get synthdef spec
			~get_spec.(argName).map(val);
		},
		unmap_arg: { arg self, argName, val;
			~get_spec.(argName).unmap(val);
		},
		node: EventPatternProxy.new,
		get_arg: ~player_get_arg,
		set_arg: ~player_set_arg
	);
	player.init;
	player;
};


~make_player = { arg main, instr, data=nil;
	var player = nil;
	case
		{ instr.isSymbol || instr.isString } {
			player = ~make_player_from_synthdef.(main,instr.asSymbol, data);
		} 
		{ instr.isFunction } {
			player = ~make_player_from_patfun.(instr, data);
		}
		{ ("ERROR: player type not recognized:"++instr).postln }
	;
	player;
};

// ==========================================
// PARPLAYER FACTORY
// ==========================================

~empty_pattern = Pn((freq:\rest, dur:0.0001),1);

~make_empty_player = (
	name: \voidplayer,
	uname: \voidplayer,
	get_playing_state: \stop,
	kind: \player
);

~empty_player = ~make_empty_player; // compat

~make_empty_groupnode = {(
	
	children: SparseArray.newClear(~general_sizes.children_per_groupnode, \voidplayer),
	kind: \parnode,
	name: \void,
	uname: \void,
	get_playing_state: \stop,

	refresh: { arg self;
		var children;
		children = SparseArray.newClear(~general_sizes.children_per_groupnode, ~make_empty_player);
		self.changed(\redraw, self, children);
	}

)};

~make_empty_parnode = {
	var res = ~make_empty_groupnode.();
	res.kind = \parnode;
	res;
};

~make_empty_seqnode = {
	var res = ~make_empty_groupnode.();
	res.kind = \seqnode;
	res;
};

~make_groupplayer = { arg main, children=List[];
	// changed messages: \redraw, \redraw_node
	var pplayer;
	pplayer = (
		//children: SparseArray.newClear(8, ~empty_player),
		children: SparseArray.newClear(~general_sizes.children_per_groupnode, \voidplayer),
		kind: \parnode,
		name: \new,
		uname: \new,
		data: Dictionary.new,
		archive_data: [\children, \kind, \name],
		playlist: List.new,
		playing_state: \stop,
		muted: false,

		init: { arg self;
			self.data[\repeat] = self.data[\repeat] ?? ~make_control_param.(\repeat, \scalar, 1, ~get_spec.(\repeat));
			self.get_arg(\repeat).get_val.debug("init repeat.get_val");
		},

		set_playing_state: { arg self, state;
			self.playing_state = state;
			self.changed(\redraw_node);
		},

		get_playing_state: { arg self;
			switch(self.muted,
				true, {
					switch(self.playing_state,
						\play, { \mute },
						\stop, { \mutestop }
					)
				},
				false, {
					switch(self.playing_state,
						\play, { \play },
						\stop, { \stop }
					)
				}
			)
		},

		mute: { arg self, val=true;
			if(val != self.muted) {
				self.muted = val;
				self.changed(\redraw_node);
			}
		},

		set_children_name: { arg self, index, name;
			self.children[index] = name;
			//self.changed(\children, index, main.get_node(name)); //TODO: use individual containers
			self.refresh;
		},

		refresh: { arg self;
			self.changed(\redraw, self, self.get_view_children);
		},

		get_children_nodes: { arg self;
			var list, pl;
			list = List.new;
			self.children.reject({arg x; x == \voidplayer}).do { arg child;
				pl = main.get_node(child);
				if(pl.notNil) { 
					if(pl.kind == \parnode || (pl.kind == \seqnode)) {
						list.add(pl.prepared_node) 
					} {
						list.add(pl.prepared_node) 
					};
				}
			};
			list;
		},

		get_children: { arg self;
			var res;
			res = self.children.reject({ arg na; [\void, \voidplayer].includes(na) }).collect { arg na; main.get_node(na) };
			res = res.reject(_.isNil); // in bankplayer, some children don't exists
			res.collect(_.uname).debug("groupplayer: get_children");
			res.asList;
		},

		get_view_children: { arg self;
			var res;
			res = self.children.collect { arg na; 
				if( [\void, \voidplayer].includes(na) ) {
					~empty_player
				} {
					main.get_node(na)
				}
			};
			res.collect(_.uname).debug("groupplayer: get_view_children");
			res.asList;
		},

		get_children_nodes2: { arg self;
			var list, pl;
			list = List.new;
			self.children.do { arg child;
				pl = main.get_node(child);
				if(pl.notNil) { 
					list.add(pl.prepared_node) 
				}
			};
			list;
		},

		get_children_sources: { arg self;
			var list, pl;
			list = List.new;
			self.children.do { arg child;
				pl = main.get_node(child);
				if(pl.notNil) { 
					if(pl.kind == \parnode || (pl.kind == \seqnode)) {
						list.add(pl.vpattern3) 
					} {
						list.add(pl.vpattern) 
					};
				}
			};
			list;
		},

		ppattern: { arg self, list, repeat=1;
			repeat.debug("ppattern.repeat");
			Ppar(list, repeat);
		},

		prepared_node: { arg self;
			self.node.source = self.vpattern;
			self.node;
		},

		new_self: { arg self, main, children=List[];
			~make_parplayer.(main, children);
		},

		vpattern0: { arg self, noreplay=true;
			var list;
			self.uname.debug("==================== making list pattern");
			list = self.get_children_nodes;
			list.debug("vpattern list");
			if(noreplay) { 
				list = list.reject({arg x;
					if( x.class == EventPatternProxy ) {
						x.player.isPlaying
					} {
						false
					};
				})
			};
			list.debug("vpattern list2");
			self.get_arg(\repeat).get_val.debug("vpattern repeat.get_val");
			if( list.size > 0 ) {
				if(self.get_arg(\repeat).get_val == 0) {
					self.ppattern(list, inf);
				} {
					self.ppattern(list, self.get_arg(\repeat).get_val);
				}
			} {
				self.uname.debug("empty list pattern");
				~empty_pattern;
			}
		},

		vpattern2: { arg self, noreplay=true;
			self.uname.debug("vpattern2");
			Ppar(self.get_children_nodes2).postcs;
		},

		vpattern3: { arg self, noreplay=true;
			self.uname.debug("vpattern3");
			Ppar(self.get_children_sources);
		},

		vpattern: { arg self, noreplay=true;
			var repeat;
			repeat = self.get_arg(\repeat).get_val.debug("vpattern repeat.get_val");
			self.uname.debug("vpattern");
			
			Pn(~par_spawner.(main.playmanager, self.get_children), repeat);
		},

		vpattern_loop: { arg self;
			Pn(self.vpattern, inf);
		},

		play_node: { arg self;
			//TODO: don't play subpattern if already playing
			self.uname.debug("playing groupnode");
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++setting source(inf)");
			main.playmanager.play_node(self.uname);
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++play");
		},

		stop_node: { arg self;
			main.playmanager.stop_node(self.uname);
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++stop");
			//FIXME: must niling source ?
			//self.get_children.do { arg n; n.stop_node };
			self.uname.debug("++++++++++++++++++++++++++++++++++++++++++++++++++stoping child nodes");
		},

		clone: { arg self;
			var pl;
			pl = self.new_self(main);
			pl.load_data( self.save_data.deepCopy );
			pl
		},

		get_args: { arg self;
			self.data.keys;
		},

		get_arg: { arg self, argu;
			self.data[argu];
		},
	
		save_data: { arg self;
			var data = ();
			var argdat;
			self.archive_data.do { arg key;
				data[key] = self[key];
			};
			data.args = ();
			self.get_args.do { arg key;
				argdat = self.get_arg(key);	
				if([\control, \stepline, \adsr, \noteline].includes(argdat.classtype), {
					data.args[key] = argdat.save_data
				})
			};
			data;
		},

		load_data: { arg self, data;
			var argdat;
			self.archive_data.do { arg key;
				self[key] = data[key];
			};
			self.get_args.do { arg key;
				argdat = self.get_arg(key);	
				if([\control, \noteline, \stepline, \adsr].includes(argdat.classtype), {
					argdat.load_data( data.args[key] )
				})
			};
		},

		node: EventPatternProxy.new
	);
	// no init
	pplayer;

};

~make_parplayer = { arg main, children=List[];
	var obj;
	obj = ~make_groupplayer.(main, children);
	obj.init();
	obj;
};

~make_seqplayer = { arg main, children=List[];
	var pplayer;
	var obj;
	obj = ~make_groupplayer.(main, children);
	pplayer = (
		kind: \seqnode,

		init: { arg self;
			self.data[\repeat] = self.data[\repeat] ?? ~make_control_param.(\repeat, \scalar, 1, ~get_spec.(\repeat));
			self.get_arg(\repeat).get_val.debug("init repeat.get_val");
		},

		vpattern: { arg self;
			var repeat;
			repeat = self.get_arg(\repeat).get_val;
			repeat.debug("ppattern.repeat");
			~seq_spawner.(main.playmanager, self.get_children, repeat);
		},

		new_self: { arg self, main, children=List[];
			~make_seqplayer.(main, children);
		}
	
	);
	pplayer.keysValuesDo { arg key, val;
		obj[key] = val;
	};
	obj.init();
	obj;

};


// ==========================================
// BANKPLAYER FACTORY
// ==========================================

~make_seqbank_player = { arg main, bank;

	var sbank;
	sbank = ~make_parplayer.(main);
	sbank.uname = ("seqbank" ++ bank).asSymbol;
	sbank.name = ("seqbank" ++ bank).asSymbol;
	sbank.children = 8.collect { arg i; ("seq"++(bank*8 + i)).asSymbol };

};

~make_parbank_player = { arg main, bank;

	var sbank;
	sbank = ~make_seqplayer.(main);
	sbank.uname = ("parbank" ++ bank).asSymbol;
	sbank.name = ("parbank" ++ bank).asSymbol;
	sbank.children = 8.collect { arg i; ("par"++(bank*8 + i)).asSymbol };

};

//a = ~make_control_param.(\repeat, \scalar, 1, ~get_spec.(\repeat))
//a.get_val

// ==========================================
// PLAYMANAGER
// ==========================================

~find_children = { arg main, node;
	var res = Set[];
	node.uname.debug("find_children: entering");
	if([\parnode, \seqnode].includes(node.kind) ) {
		node.get_children.do { arg child;
			child.uname.debug("processing child");
			if(child.uname.isNil) {
				"child is nil".debug;
			} {
				"aa".debug;
				if( res.includes(child.uname) ) {
					child.uname.debug("find_children: loop");
				} {
					"ba".debug;
					res.add(child);
					"ca".debug;
					if([\parnode, \seqnode].includes(child.kind) ) {
						"da".debug;
						res.addAll(~find_children.(main, child));
					};
				};
			};
		};
	};
	//[node.uname, res].debug("result for");
	res; // return real nodes (not names)
};

~make_playmanager = { arg main;
	
	var obj;
	obj = (
		top_nodes: Dictionary.new,
		children_nodes: Set.new,
		solomuted_nodes: Set.new,

		play_node: { arg self, nodename;
			var esp, sc, children;
			nodename.debug("pm: play_node");
			[self.top_nodes, self.children_nodes].debug("pm: state");
			~notNildo.(main.get_node(nodename), { arg node;
				children = ~find_children.(main, node);
				if( self.top_nodes.keys.includes(nodename) ) {
					nodename.debug("pm: play_node: already playing, unmuting children");
					children.do { arg child;
						child.mute(false);
					};
				} {
					if( self.children_nodes.includes(nodename) ) {
						nodename.debug("pm: play_node: unmute");
						node.mute(false);
					} {
						nodename.debug("pm: play_node: play!");
						children.do { arg child;
							child.mute(false);
						};
						node.mute(false);
						node.node.source = node.vpattern_loop;
						node.node.play;
						node.set_playing_state(\play);
						esp = node.node.player;
						//node.debug("owww!");
						children.collect(_.uname).debug("pm: play_node: children");
						sc = SimpleController(esp);
						self.top_nodes[nodename] = (
							esp: esp,
							sc: sc
						);
						sc.put(\stopped, {
							nodename.debug("pm: stop handler called");
							self.top_nodes.removeAt(nodename);
							node.mute(false);
							node.set_playing_state(\stop);
							children.do { arg child;
								self.children_nodes.remove(child.uname);
								child.set_playing_state(\stop);
							};
							children.collect(_.uname).debug("pm: stop handler: children removed");
							sc.remove;
							[self.top_nodes, self.children_nodes].debug("pm: end state");
						});
					}
				}
			}); 
			[self.top_nodes, self.children_nodes].debug("pm: end state");
		},

		stop_node: { arg self, nodename;
			var children;
			nodename.debug("pm: stop_node");
			[self.top_nodes, self.children_nodes].debug("pm: state");
			~notNildo.(main.get_node(nodename), { arg node;
				if( self.top_nodes.keys.includes(nodename) ) {
					nodename.debug("pm: stop_node: stoping!");
					self.top_nodes[nodename].esp.stop;
				} {
					if( self.children_nodes.includes(nodename) ) {
						nodename.debug("pm: stop_node: mute");
						node.mute(true);
					} {
						nodename.debug("pm: stop_node: not playing, individually stopping children");
						children = ~find_children.(main, node);
						children.do { arg child; 
							child.node.stop;
						};
					}
				}
			}); 

			[self.top_nodes, self.children_nodes].debug("pm: end state");
		},

		solo_node: { arg self, nodename;
			var children, smn = Set[];
			self.top_nodes.keys.union(self.children_nodes).do { arg nname;
				if(nodename != nname) {
					~notNildo.(main.get_node(nname), { arg node;
						if(node.muted.not) {
							smn.add(nname);
							node.mute(true);
						}
					});
				}
			};
			self.solomuted_nodes = self.solomuted_nodes.union(smn);
			main.get_node(nodename).mute(false);
		},

		unsolo_node: { arg self;
			self.solomuted_nodes.do { arg nname;
				main.get_node(nname).mute(false);
			};
			self.solomuted_nodes = Set.new;
		},


		add_childnode: { arg self, nodename;
			nodename.debug("add_childnode");
			self.children_nodes.add(nodename);
		},

		remove_childnode: { arg self, nodename;
			nodename.debug("remove_childnode");
			self.children_nodes.remove(nodename);
		}
	);
	obj;


};

~par_spawner = { arg pm, par; //par: node list
	par.do{ arg no; no.uname.debug("par_spawner: node"); };
	Pspawner({ |spawner|
		var streams = List.new;
		par.do { |node, i|
			var stream;
			node.uname.debug("par_spawner: inside: node");
			stream = CleanupStream(node.vpattern.asStream, { // use vpattern to avoid Pn(vpattern)
				node.uname.debug("&&&&&&&&&&&&&&&&par_spawner: ending stream");
				streams.remove(stream);
				if(streams.isEmpty) {
					node.uname.debug("&&&&&&&&&&&&&&&&par_spawner: all stream ended");
					spawner.suspendAll;
					par.do{ arg no; 
						no.uname.debug("&&&&&&&&&&&&&par_spawner: stop");
						node.set_playing_state(\stop);
						pm.remove_childnode(no.uname);
					};
				} {
					node.uname.debug("&&&&&&&&&&&&&&&&par_spawner: respawning stream");
					spawner.par(Pn(node.vpattern, inf))
				};
			});
			streams.add(stream);
			pm.add_childnode(node.uname);
			node.set_playing_state(\play);
			node.uname.debug("play");
			spawner.par(stream);
		};
	});
};

~seq_spawner = { arg pm, seq, repeat;
	seq.do{ arg no; no.uname.debug("seq_spawner: node"); };
	Pspawner({ |spawner|
		seq.do { |node, i|
			var stream;
			seq.do{ arg no; pm.add_childnode(no.uname) };
			repeat.do {
				node.set_playing_state(\play);
				spawner.seq(node.vpattern);
				node.set_playing_state(\stop);
			};
			seq.do{ arg no; pm.remove_childnode(no.uname) };
		};
	});
};

)
