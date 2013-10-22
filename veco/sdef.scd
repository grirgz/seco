// scores
// - samples
// - presets
// - array
// - var
// - env
// - notes
// - wavetables
// - samplekit
// - audiorecord
// 
// - knob preset macro group
// - waveshape
// - impulse response
// - velocity/added data
// - curves/modulator



(
	~class_score_manager = ( 
		all: IdentityDictionary.new,

		new: { arg self;
			self = self.deepCopy;
		
			self.main = { Veco.main };
			
		
			self;
		},

		load_data_from_clip: { arg self, src_clip, dst_clip;
			var src_score_storage = ~veco_clipboard_score_storage;
			[src_score_storage.all.keys, src_clip.node_uname, self.all.keys, dst_clip.node_uname]
						.debug("class_score_manager: load_data_from_clip: begin");

			if(src_score_storage.all[src_clip.node_uname].notNil) {

				src_score_storage.all[src_clip.node_uname].keysValuesDo { arg key, val;
					if(val.notNil) {
						var src_score = val.get(src_clip.get_index);
						var scores = self.all[dst_clip.node_uname];
						var score_col;
						var dst_score;
						if(scores.isNil) {
							[dst_clip.node_uname,key].debug("Warning, no Dict of score collection in the dest");
							self.all[dst_clip.node_uname] = IdentityDictionary.new;
							scores = self.all[dst_clip.node_uname];
						};
						
						if(scores[key].isNil) {
							[dst_clip.node_uname,key].debug("Warning, no score collection in the dest clip");
							scores[key] = val.clone(dst_clip.node_uname, key);
						};
						scores[key].all.dump;
						scores[key].all.indices.debug("dest indices");
						[self.all.keys, dst_clip.node_uname, scores.keys, key, scores[key].isNil]
							.debug("class_score_manager: load_data_from_clip");
						scores[key].load_data_from_score(dst_clip.get_index, src_score);
					}
				};

			};
		},

		load_clip_data: { arg self, clip;
			if(self.all[clip.node_uname].notNil) {
				self.all[clip.node_uname].keysValuesDo { arg key, val;
					if(val.notNil) {
						var src_score = val.get(clip.get_index);
						//src_score.load_data; // already called if newly created, but otherwise ?
					}
				};
			}
		},

		get: { arg self, node_uname, name;
			var node;
			var node_or_clip;
			var score;
			var index;
			"merdeiii2A".debug;
			node_or_clip = self.main.get_node_by_uname(node_uname);
			"merdeiii2B".debug;
			if(node_or_clip.isNil) {
				node_uname.debug("not a uname node");
				nil;
			} {

				if(node_or_clip.parent_buffer.notNil) {
					index = node_or_clip.get_index;
					[node_uname, index].debug("is a nodeclip: name, idx");
					score = self.all[node_or_clip.parent_buffer.uname][name].get(index);
				} {
					node_or_clip.uname.debug("is a node");
					score = self.all[node_or_clip.uname][name];
				};
				"merde2C".debug;
				score;
			};
		},

		define: { arg self, node_uname, name, kind, spec;
			var make_score = { arg score_class;
				if(self.all[node_uname].isNil) {
					self.all[node_uname] = IdentityDictionary.new;
				};
				if(self.all[node_uname][name].isNil) {
					[kind, node_uname, name].debug("defining NEW score");
					self.all[node_uname][name] = score_class.new(node_uname, name, spec);
					self.all[node_uname][name].load_data;
				} {
					[kind, node_uname, name].debug("defining NEW score (already exists so noop)");
				}
			};
			switch(kind,
				\audio, {
					make_score.(~class_score_audio_wrapper);
				},
				\preset, {
					make_score.(~class_score_preset_wrapper);
				}
			)
		},
	);

	~score_storage = ~class_score_manager.new;

	//// scores

	~class_score_base = (
		label: { arg self;
			"%.%.%".format(self.node_uname, self.index+1, self.name);
		},

		data_path: { arg self;
			Veco.main.data_path +/+ "%.%.%.aiff".format(self.node_uname, self.index+1, self.name);
		},
	);

	~class_score_audio = (
		parent: ~class_score_base,
		new: { arg self, parent, index;
			self = self.deepCopy;
		
			self.name = parent.name;
			self.node_uname = parent.node_uname;
			self.index = index;
			self.recclock = TempoClock.default;
			//self.overlap_time = 0.20;
			self.overlap_time = ~veco_record_overlap_time;
			self.label.debug("class_score_audio: new");
			self.load_data;
		
			self;
		},

		new_from_score: { arg self, parent, index, score;
			self = self.deepCopy;
		
			self.name = parent.name;
			self.node_uname = parent.node_uname;
			self.index = index;
			self.recclock = TempoClock.default;
			//self.overlap_time = 0.20;
			self.overlap_time = ~veco_record_overlap_time;
			self.load_data_from_score(score);
		
			self;
		},

		is_freed: { arg self;
			BufferPool.is_freed(self.buf);
		},

		load_data_from_score: { arg self, score;
			var buf;
			//if(score.buf.isNil) {
			//	"score.buf is nil, trying from file"
			//} {
			//	buf = score.buf;
			//};
			// TODO: check if buffer was freed then copy file
			self.buf = score.buf;
			BufferPool.retain(self.buf, \veco_audio, self.name);
			[self.buf, score.buf].debug("class_score_audio: load_data_from_score: copied buf pointer");
			self.dur = score.dur;
			self.save_data;
		},

		bufnum: { arg self;
			if(self.buf.notNil) {
				//[self.label, self.buf].debug("requested bufnum");
				self.buf.bufnum
			} {
				self.label.debug("no buffer to play");
				0
			}
		},

		pbufnum: { arg self;
			Pfunc { self.bufnum };
		},

		pdur: { arg self;
			Pfunc { self.dur };
		},

		psustain: { arg self;
			//Pfunc { self.dur / self.recclock.tempo + self.overlap_time };
			Pfunc { self.dur / self.recclock.tempo };
		},

		sustain: { arg self;
			//Pfunc { self.dur / self.recclock.tempo + self.overlap_time };
			self.dur / self.recclock.tempo
		},

		save_data: { arg self;
			[self.data_path, self.buf].debug("write audio file");
			//self.buf.write(self.data_path, "wav");
			self.buf.write(self.data_path);
		},

		load_data: { arg self;
			if(File.exists(self.data_path)) {
				self.buf = BufferPool.read(\veco_audio, self.name, self.data_path, action: { arg buf;
					self.dur = buf.numFrames / buf.sampleRate * self.recclock.tempo;
					self.dur = self.dur.trunc;
					[buf, self.data_path, self.dur].debug("buffer loaded");
				});
			} {
				[self.data_path, self.node_uname, self.name].debug("no file to load");
			}
		},

		record: { arg self, dur=8;
			self.record_dur = dur;
			//self.buf.free; //TODO: backup
			BufferPool.release(self.buf, \veco_audio); //TODO: backup
			self.buf = BufferPool.alloc(\veco_audio, self.name, s.sampleRate * (dur / self.recclock.tempo + self.overlap_time), 2); 
			self.recorder = ~audio_recorder.new(self.buf, dur);
			self.dur = dur;
			self.recorder.start_recording({
				Task({

					1.wait;
					self.save_data;
					nil;
				}).play;
				nil;
			});
			
		},
	);

	~class_score_preset = (
		parent: ~class_score_base,
		new: { arg self, parent, idx;
			var specs;
			var extra_specs = [];
			self = self.deepCopy;


			self.name = parent.name;
			self.node_uname = parent.node_uname;
			parent.spec.debug("pSPECS");
			self.specs = parent.spec;
			self.index = idx;
			self.parent_collection = { parent };
		
			self.build_vals;
			self.build_pbind;


			//self.load_data; // doesnt exists, called on collection level

			self;
		},

		new_from_score: { arg self, parent, index, score;
			var specs;
			var extra_specs = [];
			self = self.deepCopy;


			self.name = parent.name;
			self.node_uname = parent.node_uname;
			parent.spec.debug("pSPECS from score");
			self.specs = parent.spec;
			self.index = index;
		
			self.build_vals;
			self.build_pbind;

			self.load_data_from_score(score);
		
			self;
		},

		load_data_from_score: { arg self, score;
			
		},

		load_data_from_struct: { arg self, struct;
			self.specs = struct.specs;
			self.data = struct.data;
		},

		save_data_to_struct: { arg self;
			var specs, data;
			specs = self.specs;
			data = self.specs.clump(2).collect { arg pair;
				var key = pair[0];
				var val = self.vals[key];
				[key, val.get_val]
			};
			(
				version: 1,
				specs: specs,
				data: data.flatten,
			)
		},

		save_data: { arg self;
			self.parent_collection.save_data;
		},

		set_data: { arg self, data;
			self.data = data;
			self.specs = data.specs;
			self.build_vals;
			data.data.clump(2).do { arg pair;
				var key = pair[0];
				var val = pair[1];
				self.vals[key].set_val(val);
			};
		},

		build_vals: { arg self;
			var specs = self.specs;
			specs.debug("SPECS");
			self.specs.debug("SPECS: sfou dmagueul");
			if(specs.isSymbol) {
				specs = self.synthdef_name_to_specs(specs)
			};

			//if(extra_specs.isSymbol) {
			//	specs = self.synthdef_name_to_specs(specs)
			//};
			//specs = specs ++ extra_specs;
			self.specs = specs;
			self.vals = IdentityDictionary.new;
			specs.clump(2).do { arg pair;
				var key = pair[0];
				var spec = pair[1];
				if(spec.class == Association) {
					//
					switch(spec.key,
						\array, {
							self.vals[key] = ~class_control_array.new(key, spec)
						},
						\env, {
							self.vals[key] = ~class_control_env.new(key, spec)
						},
						\adsr, {

						}
					
					);
				} {
					self.vals[key] = ~class_control_scalar.new(key, spec);
				};
			};
			self.vals.keys.debug("VALS");
			
		},

		label: { arg self;
			"%.%.%".format(self.node_uname, self.index+1, self.name);
		},

		synthdef_name_to_specs: { arg self, defname;
			var reject = [\doneAction, \out, \gate, '?'];
			var res = ~synthdef_name_to_specs.(defname);
			res = res.clump(2).reject({ arg x; x[0].dump.postln; reject.includes(x[0]) }).flatten;
			res;
		},


		edit: { arg self;
			~class_node_preset_view.new(self);
		},

		build_pbind: { arg self;
			var binds = List.new;
			self.specs.clump(2).do { arg pair;
				var key = pair[0];
				var val = self.vals[key];
				[pair, key, val, self.vals.keys].debug("PAIR");
				if(val.notNil) {
					[pair].debug("PAIRiiii");
					binds.add(key);
					binds.add(val.vpattern);
				} {

				}
			};
			//binds = binds.flat;
			self.pbind = Pbind(*binds);
			self.pbind;
		},
	);

	//// score collections

	~class_score_collection = (
		new: { arg self, node_uname, name, spec;
			self = self.deepCopy;

			self.name = name;
			self.node_uname = node_uname;
			self.spec = spec;
		
			self.all = Order.new;
			//self.load_data; // can't be called now because data file can't access to score because not finished created yet
			//self.score_class = class;
		
			self;
		},

		data_path: { arg self;
			Veco.main.data_path +/+ "%.%.scd".format(self.node_uname, self.name);
		},

		clone: { arg self, node_uname, name;
			~class_score_collection.new(node_uname, name);
		},

		load_data_from_score: { arg self, idx, score;
			var res;

			res = self.all.at(idx);
			if(res.isNil) {
				var new = self.score_class.new_from_score(self, idx, score);
				[self.node_uname, idx].debug("class_score_collection: load_data_from_score");
				self.all.put(idx, new);
				res = self.all.at(idx);
			} {
				res.load_data_from_score(score);
			};
			res;
			
		},

		at_default: { arg self;
			self.get(0)
		},

		active_clip_index: { arg self;
			Veco.main.get_node_by_uname(self.node_uname).active_clip_index;
		},

		get_active_score: { arg self;
			var node;
			node = Veco.main.get_node_by_uname(self.node_uname);
			self.get(node.active_clip_index);
		},

		get: { arg self, idx;
			var res;
			self.all.dump;
			[self.node_uname, self.name, self.all.indices, Veco.main.project_path, idx].debug("class_score_collection.get");

			res = self.all.at(idx);
			if(res.isNil) {
				var new = self.score_class.new(self, idx);
				self.all.put(idx, new);
				res = self.all.at(idx);
			};
			res;
		},

		set_data: { arg self, idx, data;
			self.get(idx).set_data(data);
		},
	);

	~class_score_audio_wrapper = (
		parent: ~class_score_collection,

		score_class: ~class_score_audio,

		clone: { arg self, node_uname, name;
			~class_score_audio_wrapper.new(node_uname, name);
		},

		pbufnum: { arg self;
			Pfunc { self.get_active_score.bufnum };
		},

		pdur: { arg self;
			Pfunc { self.get_active_score.dur };
		},

		psustain: { arg self;
			Pfunc { self.get_active_score.sustain };
		},
	);

	~class_score_preset_wrapper = (
		parent: ~class_score_collection,

		score_class: ~class_score_preset,

		clone: { arg self, node_uname, name;
			~class_score_preset_wrapper.new(node_uname, name);
		},

		load_data: { arg self;
			self.data_path.load;
		},

		save_data: { arg self;
			var array_to_string = { arg arr;
				var str = "";
				//arr.do {
				//	str

				//}
				arr.asCompileString;

			};
			var struct_to_string = { arg struct;
				var str = List.new;
				str.add("\t(");
				struct.keysValuesDo { arg key, val;
					str.add("\t\t%: %,".format(key, val.asCompileString));
				};
				str.add("\t)");
				str = str.join("\n");
				str;
			};
			File.use(self.data_path, "w", { arg file;
				var str = "";
				self.all.do { arg val, idx;
					if(val.notNil) {
						str = str ++ (
							"\nSdef(%, %).set_data(%, \n%\n);\n"
						).format(
							self.node_uname.asCompileString,
							self.name.asCompileString,
							idx, 
							struct_to_string.(val.save_data_to_struct)
						);
					}
				};
				file.write(str);

			});
		},

		pbind: { arg self;
			//Pfunc { self.get_active_score.pbind };
			self.get_active_score.pbind;
		},

		edit: { arg self;
			self.get_active_score.edit;
		},
	);


	//////////////////////////////

	~veco_record_overlap_time = 0.20;

	~audio_recorder = (
		new: { arg self, buf, dur;
			self = self.deepCopy;
		
			
			self.recclock = TempoClock.default;
			self.recclock.debug("gnill");
			self.is_recording = false;
			self.record_dur = dur;
			self.buf = buf;
			//self.buf = Buffer.alloc(s, s.sampleRate * ~record_duration / self.recclock.tempo, 2); 
		
			self;
		},

		start_immediate_recording: { arg self;
			var quant;
			var visual_timer;
			var visual_timer_player;
			var timer_start_beat;
			var next_time_on_grid;
			var overlap_time = ~veco_record_overlap_time;
			var dur;
			"in start_immediate_recording".debug;
			quant = Pdef.defaultQuant;
			dur = self.record_dur;
			if(self.buf.notNil) {
				self.recnode = Pbind(
					\instrument, \record_input, 
					\bufnum, self.buf,
					\dur, dur,
					\sustain, dur / self.recclock.tempo + overlap_time,
					\monitor, Pn(1,1)
				).trace.play(quant:quant);
				//).trace.play;
				[self.recclock.beats, self.recclock.nextTimeOnGrid(quant) , dur, self.recclock.nextTimeOnGrid(quant) + dur].debug("sched");
				self.recclock.schedAbs(self.recclock.nextTimeOnGrid(quant) + dur, { self.stop_recording });

				visual_timer = Task({ 
					var x=self.recclock.beatInBar.asInteger;
					debug("RAHHHHHHHH");
					10.do {
						[self.recclock.beats, self.recclock.beatInBar, self.recclock.nextTimeOnGrid(quant)].debug("RAHHHHHHHH2");

						Veco.main.launchpad.set_mode_led((x-1).wrap(0,4), \off);
						Veco.main.launchpad.set_mode_led(x, \green);
						1.wait;
						x = x+1 % 4;
					};
				});
				//visual_timer.schedAbs(self.recclock, quant:[1,-4]);

				next_time_on_grid = self.recclock.nextTimeOnGrid(quant);
				timer_start_beat = next_time_on_grid - min((next_time_on_grid - self.recclock.beats), 4);
				[next_time_on_grid, self.recclock.beats, timer_start_beat].debug("timer_start_beat");
				self.recclock.schedAbs(
					timer_start_beat,
					{visual_timer.play}
				);
				// FIXME: move rendering code outside
				self.recclock.schedAbs(self.recclock.nextTimeOnGrid(quant), { 
					visual_timer.stop;
					4.do { arg x;
						Veco.main.launchpad.set_mode_led(x, \red);
					};
					nil;
				});
			} {
				"make_audio_recorder: buf is nil".debug;
			};
		},

		start_recording: { arg self, finish;
			debug("start_recording");
			self.start_time = self.recclock.beats;
			self.start_immediate_recording;
			//self.event_score = ~make_notescore.();
			self.is_recording = true;
			self.finish = finish;
		},

		stop_recording: { arg self;
			debug("stop_recording");
			self.stop_time = self.recclock.beats;
			//self.event_score.set_end(self.stop_time - self.start_time);
			// FIXME: move rendering code outside
			4.do { arg x;
				Veco.main.launchpad.set_mode_led(x, \off);
			};
			self.finish;
			self.is_recording = false;
		},

		add_event_on: { arg self, event;
			var time;
			if(self.is_recording) {
				time = self.recclock.beats - self.start_time;
				self.event_score.add_note(event, time, event.event_key)
			}
		},

		add_event_off: { arg self, key;
			var time;
			if(self.is_recording) {
				time = self.recclock.beats - self.start_time;
				self.event_score.book.debug("BOOK");
				self.event_score.add_noteoff(key, time)
			}
		},
	);

	///// views

	~class_control_scalar_view = (
		new: { arg self, controller;
			self = self.deepCopy;

			self.controller = { controller };
		
			self;
		},

		make_layout: { arg self;
			
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = 0.001;
			name = self.controller.label;
			spec = self.controller.spec;
			spec = spec ?? name.asSpec ?? \widefreq.asSpec;

			label = StaticText.new;
			label.string = name.asString;
			name.debug("make_layout:name");
			label.minWidth = 160;

			val = StaticText.new;
			val.minWidth = 100;
			val.string = self.controller.get_val.round(prec);

			slider = Slider.new(nil, Rect(20, 60, 150, 20));
			slider.value = spec.unmap(self.controller.get_val);

			slider.action = { arg sli;
				self.controller.set_val(spec.map(sli.value));
				val.string = self.controller.get_val.round(prec);
			};

			layout = HLayout.new(
				label, slider, val
			);
			self.layout = layout;
			layout;
		},
	);

	~class_control_array_view = (
		new: { arg self, controller;
			self = self.deepCopy;

			self.controller = { controller };
		
			self;
		},

		make_layout: { arg self;
			
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = 0.001;
			name = self.controller.label;
			spec = self.controller.spec.value[1];
			spec = spec ?? name.asSpec ?? \widefreq.asSpec;

			label = StaticText.new;
			label.string = name.asString;
			name.debug("make_layout:name");
			label.minWidth = 160;

			val = StaticText.new;
			val.minWidth = 100;
			val.string = self.controller.get_val;

			//slider = Slider.new(nil, Rect(20, 60, 150, 20));
			slider = MultiSliderView.new(nil, Rect(20, 60, 150, 20));
			slider.value = spec.unmap(self.controller.get_val);

			slider.action = { arg sli;
				self.controller.set_val(spec.map(sli.value));
				val.string = self.controller.get_val.round(prec).asCompileString;
			};

			layout = VLayout.new(
				label, slider, val
			);
			self.layout = layout;
			layout;
		},
	);

	~class_control_env_view = (
		new: { arg self, controller;
			self = self.deepCopy;
		
			self.controller = {controller};
		
			self;
		},


		update_label: { arg self;
			self.env_label.string_(
				"%".format(
					~env_to_string.(self.controller.get_env),
				)
			);
		},
		
		update_env: { arg self;
			self.env_view.value_(self.controller.get_norm_xy);
		},

		update_knobs: { arg self;
			var index = self.env_view.index;
			var levels = self.controller.get_env.levels;
			var times = self.controller.get_env.times;
			var level_spec = self.controller.level_spec;
			var time_spec = self.controller.time_spec;
			if(levels[index].notNil) {
				self.knob_level.value = level_spec.unmap(levels[index]);
			};
			if(times[index].notNil) {
				self.knob_time.value = time_spec.unmap(times[index]);
			};
		},

		make_layout: { arg self;
			var node_name, name, spec;
			var win, val, slider, label, layout;
			var prec = 0.001;
			var knob_layout;
			var knob_size = 15;
			var level_spec, time_spec, curve_spec;
			name = self.controller.label;

			level_spec = self.controller.level_spec;
			time_spec = self.controller.time_spec;
			curve_spec = self.controller.curve_spec;

			label = StaticText.new;
			label.string = name.asString;
			label.minWidth = 160;

			val = StaticText.new;
			val.minWidth = 100;
			val.string = self.controller.get_val;
			

			slider = EnvelopeView.new(nil, Rect(0, 0, 230, 80))
				.drawLines_(true)
				.selectionColor_(Color.red)
				.drawRects_(true)
				.step_(0)
				.thumbSize_(10)
				.keepHorizontalOrder_(true)
				.value_(self.controller.get_norm_xy);
			self.env_view = slider;
			self.env_view.selectIndex(1);
			self.env_view.action = { arg env;
				if(env.index == 0 and: { env.x > 0 }) {
					env.x = 0;
				};
				self.controller.set_norm_xy(env.value);
				self.update_label;
				self.update_knobs;
			};
			self.env_view.mouseDownAction = { arg view, x, y, mod;
				self.mouse_down_point = Point(x,y);
				self.mouse_down_index = view.index;
				self.mouse_down_time_scale = self.controller.time_scale;
			};
			self.env_view.mouseUpAction = { arg view, x, y, mod;
				self.update_knobs;
				if(mod.isShift) {
					self.env_view.selectIndex(self.mouse_down_index)
				}
			};
			self.env_view.mouseMoveAction = { arg view, x, y, mod;
				var val;
				if(mod.isShift) {
					val = x - self.mouse_down_point.x;
					self.controller.time_scale = self.mouse_down_time_scale + (val/500);
					self.update_env;
				}
			};
			self.env_view.keyDownAction = {
				self.update_knobs;
			};

			self.knob_level = Knob.new
				.mode_(\vert)
				.maxWidth_(knob_size);
			self.knob_time = Knob.new
				.mode_(\vert)
				.maxWidth_(knob_size);
			self.knob_curve = Knob.new
				.mode_(\vert)
				.maxWidth_(knob_size);

			self.knob_level.action = { arg knob;
				var index = self.env_view.index;
				var levels = self.controller.get_env.levels;
				if(levels[index].notNil) {
					levels[index] = level_spec.map(knob.value);
					self.controller.get_env.levels = levels;
					self.env_view.y = knob.value;
					self.update_label;
				};
			};
			self.knob_time.action = { arg knob;
				var index = self.env_view.index;
				var times = self.controller.get_env.times;
				if(times[index].notNil) {
					times[index] = time_spec.map(knob.value);
					self.controller.get_env.times = times;
					self.env_view.value_(self.controller.get_norm_xy);
					self.env_view.selectIndex(index);
					self.update_label;
				}
			};
			self.knob_curve.action = { arg knob;
				self.controller.get_env.curves = curve_spec.map(knob.value);
				self.env_view.curves = curve_spec.map(knob.value);
				self.update_label;
			};

			self.env_label = StaticText.new
				.font_(Font("Arial",10))
				.string_("--");

			knob_layout = HLayout.new(
				self.knob_level,
				self.knob_time,
				self.knob_curve,
				self.env_label
			);


			self.update_label;
			self.update_knobs;

			layout = VLayout.new(
				label, slider, knob_layout
			);
			self.layout = layout;
			layout;
			
		},

		make_window: { arg self;
			~windowize.(self.make_layout);
		},
	);


	~class_node_preset_view = (
		new: { arg self, controller;
			self = self.deepCopy;
		
			self.controller = { controller };
			self.make_window;
		
			self;
		},

		make_window: { arg self;
			var win;
			var layout;
			//win = Window.new("preset", Rect(200,200,500,550));
			win = Window.new("Preset Editor -- node: %, index: %".format(self.controller.node_uname, self.controller.index));
			win.view.keyDownAction = ~closer_responder;

			self.layout = layout = HLayout.new;
			self.slider_layout = VLayout.new;
			self.array_layout = VLayout.new;

			layout.add(self.slider_layout);
			layout.add(self.array_layout);

			self.controller.vals.do { arg ctrl;
				switch(ctrl.kind,
					\array, {
						self.array_layout.add(~class_control_array_view.new(ctrl).make_layout);
					},
					\env, {
						self.array_layout.add(~class_control_env_view.new(ctrl).make_layout);
					},
					\scalar, {
						self.slider_layout.add(~class_control_scalar_view.new(ctrl).make_layout);
					},
					{
						ctrl.kind.debug("Error, % kind not recognised");
					}
				);
			};

			self.array_layout.add(nil);
			self.slider_layout.add(nil);

			win.onClose = {
				self.controller.save_data;

			};
			win.layout = layout;
			win.front;
			
		},
	);

	///// controls

	~class_control_scalar = (
		kind: \scalar,
		new: { arg self, name, spec;
			self = self.deepCopy;
		
			self.name = name;
			self.spec = spec;
			self.val = spec.default;
		
			self;
		},

		make_layout: { arg self;
			~class_control_scalar_view.new(self).make_layout;
		},

		label: { arg self;
			self.name;
		},

		get_val: { arg self;
			self.val;	
		},

		set_val: { arg self, val;
			self.val = val;
		},

		vpattern: { arg self;
			Pfunc { self.get_val };
		},
	);

	~class_control_array = (
		kind: \array,
		new: { arg self, name, spec;
			self = self.deepCopy;
		
			self.name = name;
			self.spec = spec;
			self.val = spec.value[2] ?? { spec.value[1].default ! spec.value[0] };
		
			self;
		},

		label: { arg self;
			self.name;
		},

		get_val: { arg self;
			self.val;	
		},

		set_val: { arg self, val;
			self.val = val;
		},

		vpattern: { arg self;
			Pfunc { [ self.get_val ] };
		},
	);
	
	~class_control_env = (
		kind: \env,
		new: { arg self, name, spec;
			self = self.deepCopy;
		
			spec = spec.value;
			self.spec = spec ?? [3, \freq.asSpec, ControlSpec(0.01,8,\exp,0,0.1), ControlSpec(-9,9,\lin,0,0), Env([100,200,50],[0.1,0.1])];

			self.name = name;

			self.spec[1] = self.spec[1] ?? \unipolar.asSpec;
			self.spec[2] = self.spec[2] ?? ControlSpec(0.01,8,\exp,0,0.1);
			self.spec[3] = self.spec[3] ?? ControlSpec(-9,9,\lin,0,0);

			self.get_env = self.spec[4] ?? { Env(
				self.spec[1].default ! self.spec[0], 
				self.spec[2].default ! self.spec[0],
				self.spec[3].default,
			) };

			self.level_spec = self.spec[1];
			self.time_spec = self.spec[2];
			self.curve_spec = self.spec[3];
		
			self;
		},

		get_val: { arg self;
			self.get_env;
		},

		set_val: { arg self, val;
			self.get_env = val;
		},

		label: { arg self;
			self.name;
		},

		get_xy: { arg self;
			var x = List.new;
			var y = List.new;
			var env = self.get_env;
			env.levels.do { arg lvl, n;
				y.add(lvl);
				if(n == 0) {
					x.add(0);
				} {
					x.add(env.times[n-1])
				}
			};
			[x.integrate.asArray,y.asArray]
		},
		time_scale: 1/2,

		get_norm_xy: { arg self, time_scale;
			var x = List.new;
			var y = List.new;
			var env = self.get_env;
			var times, levels;
			var res;
			time_scale = time_scale ?? self.time_scale;
			times = self.time_spec.unmap(env.times);
			levels = self.level_spec.unmap(env.levels);
			x = times;
			y = levels;
			x = x.insert(0, 0);
			res = [x.integrate.asArray * time_scale,y.asArray];
			res
		},

		set_norm_xy: { arg self, val, time_scale;
			var times, levels;
			time_scale = time_scale ?? self.time_scale;
			#times, levels = val;
			times = times.copy;
			times.removeAt(0);
			times = times / time_scale;
			times = times.differentiate;
			times = self.time_spec.map(times);
			levels = self.level_spec.map(levels);
			self.get_env.times = times;
			self.get_env.levels = levels;
		},

		edit: { arg self;
			~env_editor.new(self).make_window;
		},

		vpattern: { arg self;
			Pfunc { self.get_env }
		},

	);

	/////

	~get_spec = { arg argName, defname=nil, default_spec=\widefreq;
		var spec = nil;
		//if(argName.beginsWith("adsr") or: argName.beginsWith("env"))
		try { 
			spec = if( SynthDescLib.global.synthDescs[defname].metadata.specs[argName].notNil, {
				var sp;
				sp = SynthDescLib.global.synthDescs[defname].metadata.specs[argName];
				if(sp.class == Association) {
					// \env -> [\freq.asSpec, \dur.asSpec]
					// \array -> [8, \unipolar.asSpec, default_val]
					sp;
				} {
					sp.asSpec;
				};
			})
		};
		if(spec.isNil, {
			if( argName.asSpec.notNil, {
				spec = argName.asSpec;
			}, {
				spec = default_spec.asSpec;
			});
		});
		[argName, spec].debug("get_spec");
		spec;
	};
		
	~synthdef_name_to_specs = { arg defname;
		var specs;
		specs = List.new;

		SynthDescLib.global.synthDescs[defname].controls.do { arg x; 
			var spec;
			specs.add(x.name);
			spec = ~get_spec.(x.name, defname).copy;
			if(spec.class != Association and: { x.defaultValue.isSequenceableCollection }) {
				specs.add(\array -> [x.defaultValue.size, \unipolar.asSpec, x.defaultValue]);
			} {
				if(spec.class == Association) {
					switch(spec.key,
						\env, {
							//spec.value[4] = x.defaultValue; // TODO: array to env
							specs.add(spec.copy);
						},
						\array, {
							spec.value[2] = x.defaultValue.copy;
							specs.add(spec.copy);
						}
					)
				} {
					spec.default = x.defaultValue;
					specs.add(spec);
				}
			};
		};
		specs;
	};

	~windowize = { arg layout;
		var win;
		win = Window.new;
		win.layout = layout;
		win.front;
	};
	~env_to_string = { arg env;
		var prec = 5;
		"Env(%, %, %)".format(
			env.levels.collect({ arg x; x.asFloat.asStringPrec(prec)}),
			env.times.collect({arg x; x.asFloat.asStringPrec(prec)}),
			env.curves
		);
	};
)

