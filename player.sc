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

~pdynarray = { arg fun, repeat=100000;
	Prout({ arg ev;
		var idx;
		var val = 0;
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

// ==========================================
// PARAM FACTORY
// ==========================================


~default_adsr = (
	attackTime:0.1,
	decayTime:0.2,
	sustainLevel:0.3,
	releaseTime:0.4,
	curve:0
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
	Env.performWithEnvir(\adsr, adsr)
};


~make_adsr_param = { arg name, default_value;
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
			self.changed(\selected);
		},
		deselect_param: { arg self;
			self.selected = 0;
			self.changed(\selected);
		},

		get_val: { arg self; self.val },

		get_param: { arg self, key;
			(
				name: key,
				spec: param.spec[key],
				classtype: { arg kself; self.classtype },
				selected: { arg kself; self.selected },
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
		},

		vpiano: { arg self;
			{
				~adsr_event_to_env.(self.val).asArray
			}
		},

		vpattern: { arg self;

			Pfunc({
				[ ~adsr_event_to_env.(self.val).asArray ]
			})
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

~default_noteline = [ // FIXME: crash when no notes
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
		notes: ~default_noteline.deepCopy,
		start_offset: 0,
		end_offset: 0,

		save_data: { arg self;
			var data = ();
			[\name, \classtype, \selected, \selected_cell, \default_val, \notes, \start_offset, \end_offset].do {
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
			[\name, \classtype, \selected, \selected_cell, \default_val, \notes, \start_offset, \end_offset].do {
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

		set_start_offset: { arg self, offset;
			self.start_offset = offset;
			self.changed(\notes);
		},

		get_start_offset: { arg self;
			self.start_offset;
		},

		set_end_offset: { arg self, offset;
			self.end_offset = offset;
			self.changed(\notes);
		},

		get_end_offset: { arg self;
			self.end_offset;
		},

		get_notes: { arg self;
			var no;
			no = self.notes.deepCopy;
			no[0].dur+self.start_offset;
			no.last.dur+self.end_offset;
			no;
		},

		get_note: { arg self, idx;
			var no;
			case 
				{ idx == 0 } { 
					no = self.notes[0].deepCopy;
					no.dur = no.dur+self.start_offset;
				} 
				{ idx == (self.notes.size-1) } {
					no = self.notes.last.deepCopy;
					no.dur = no.dur+self.end_offset;
				}
				{ true } {
					self.notes[idx];
				}
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
			self.changed(\notes);
		},
		vpattern: { arg self; 
			"YE SOUI PREMIER!!!!".debug;
			~pdynarray.( { arg idx; self.tick; self.get_note(idx) } );
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
		},
		vpattern: { arg self; 
			~pdynarray.( { arg idx; 
				self.tick(idx); 
				if(self.seq.val[idx] == 1, { [idx, TempoClock.beats, TempoClock.beatInBar].debug("----------TICK step"); });
				self.seq.val[idx];
			} )
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
		vpattern: { arg self; Pif( Pkey(\stepline) > 0 , \note, \rest) } // WTF with == ?????
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

			//FIXME: handle others "out of range" exceptions
			set_norm_val: { arg self, norm_val, idx=nil;
				idx = idx ?? self.selected_cell;
				self.val.wrapPut(idx, param.spec.map(norm_val));
				param.changed(\val, idx % self.val.size);
			},
			get_norm_val: { arg self, idx=nil;
				idx = idx ?? self.selected_cell;
				param.spec.unmap(self.val[idx]);
			},
			set_val: { arg self, val, idx=nil;
				idx = idx ?? self.selected_cell;
				self.val[idx] = val;
				param.changed(\val, idx);
			},
			get_val: { arg self, idx=nil;
				idx = idx ?? self.selected_cell;
				self.val[idx];
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

			select_cell: { arg self, idx; },
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
			})
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

~make_player_from_synthdef = { arg defname, data=nil;
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
		node: EventPatternProxy.new,

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
										control.defaultValue
									)
								}
								{ name == '?' } { 
									// skip
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

			self.data[\stepline] = self.data[\stepline] ?? ~make_stepline_param.(\stepline, 1 ! 8 );
			self.data[\instrument] = self.data[\instrument] ?? ~make_literal_param.(\instrument, defname);
			self.data[\type] = ~make_type_param.(\type);

			//TODO: handle t_trig arguments

			self.node.source = {
				var dict = Dictionary.new;
				var list = List[];
				var prio;
				prio = [\instrument, \stretchdur, \noteline, \stepline, \type, \dur, \segdur, \legato, \sustain];

				list.add(\elapsed); list.add(Ptime.new);
				prio.do { arg key;
					list.add(key); list.add( self.data[key].vpattern );
				};
				self.data.keys.difference(prio).do { arg key;
					list.add(key); list.add( self.data[key].vpattern );
				};
				list.debug("maked pbind list");
				//[\type, \stepline, \instrument].do { arg x; list.add(x); list.add(dict[x]) };
				//list.debug("maked pbind list");
				Pbind(*list).dump;
			}.value;
		},

		get_piano: { arg self;
			var exclu, list = List[];
			exclu = [\instrument, \noteline, \amp, \freq, \stepline, \type, \dur, \segdur, \legato, \sustain];
			self.data.keys.difference(exclu).do { arg key;
				list.add(key); list.add( self.data[key].vpiano ) 
			};
			{ arg freq, veloc; 
				Synth(self.data[\instrument].vpiano, (
					[\freq, freq, \amp, self.data[\amp].vpiano.value * veloc ] ++
						list.collect(_.value)).debug("arg listHHHHHHHHHHHHHHHHHHHHHHHHHHH")) 
			}

		},

		set_noteline: { arg self, set;
			self.data.keysValuesDo { arg key, val;
				if(val.classtype == \control, {
					[val.name, set].debug("setting noteline");
					val.noteline = set
				})
			};
		},

		clone: { arg self;
			var pl;
			pl = ~make_player_from_synthdef.(defname);
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
				if([\control, \stepline, \adsr, \noteline].includes(argdat.classtype), {
					data.args[key] = argdat.save_data
				})
			};
			data.name = defname;
			data.bank = self.bank;
			data;
		},

		load_data: { arg self, data;
			var argdat;
			self.get_args.do { arg key;
				argdat = self.get_arg(key);	
				if([\control, \noteline, \stepline, \adsr].includes(argdat.classtype), {
					argdat.load_data( data.args[key] )
				})
			};
			self.bank = data.bank;
		},

		as_event: { arg self;
			var ev = ();
			self.data.keysValuesDo { arg key, val;
				ev[key] = val.get_val;
			};
			ev;
		},

		get_arg: ~player_get_arg,
		set_arg: ~player_set_arg

	);
	player.init;
	player;
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
Spec.add(\dur, ControlSpec(4/128, 4, \lin, 4/64, 0.25, "s"));
Spec.add(\legato, ControlSpec(0, 1.2, \lin, 0, 0.707));
Spec.add(\sustain, \legato);

~make_player = { arg instr, data=nil;
	var player = nil;
	case
		{ instr.isSymbol || instr.isString } {
			player = ~make_player_from_synthdef.(instr.asSymbol, data);
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

~make_parplayer = { arg plist;
	var pplayer;
	pplayer = (
		init: { arg self;
			self.node.source = Ppar(plist);
		},
		node: EventPatternProxy.new
	);

};


)
