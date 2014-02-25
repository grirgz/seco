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
						//scores[key].all.dump;
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
			//"merdeiii2A".debug;
			node_or_clip = self.main.get_node_by_uname(node_uname);
			//"merdeiii2B".debug;
			if(node_or_clip.isNil) {
				//node_uname.debug("not a uname node");
				nil;
			} {

				if(node_or_clip.parent_buffer.notNil) {
					index = node_or_clip.get_index;
					//[node_uname, index].debug("is a nodeclip: name, idx");
					score = self.all[node_or_clip.parent_buffer.uname][name].get(index);
				} {
					//node_or_clip.uname.debug("is a node");
					score = self.all[node_or_clip.uname][name];
				};
				//"merde2C".debug;
				score;
			};
		},

		define: { arg self, node_uname, name, kind, spec, force=false;
			var make_score = { arg score_class;
				if(self.all[node_uname].isNil) {
					self.all[node_uname] = IdentityDictionary.new;
				};
				if(self.all[node_uname][name].isNil or: { force == true }) {
					//[kind, node_uname, name].debug("defining NEW score");
					self.all[node_uname][name] = score_class.new(node_uname, name, spec);
					if(force.not) {
						self.all[node_uname][name].load_data;
					}
				} {
					//[kind, node_uname, name].debug("defining NEW score (already exists so noop)");
				}
			};
			switch(kind,
				\audio, {
					make_score.(~class_score_audio_wrapper);
				},
				\notes, {
					make_score.(~class_score_notes_wrapper);
				},
				\preset, {
					make_score.(~class_score_preset_wrapper);
				}
			)
		},
	);


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
			//self.label.debug("class_score_audio: new");
			self.load_data;
		
			self;
		},

		is_freed: { arg self;
			BufferPool.is_freed(self.buf);
		},

		//////////// save/load

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
			//[self.buf, score.buf].debug("class_score_audio: load_data_from_score: copied buf pointer");
			self.dur = score.dur;
			self.save_data;
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

		//// wrappers methods

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
		parent: VecoLib.lib[\class_preset],

		data_path: { arg self; ~class_score_base.data_path },

		new: { arg self, parent, idx, subidx;
			var specs;
			var extra_specs = [];
			self = self.deepCopy;


			self.name = parent.name;
			self.node_uname = parent.node_uname;
			//parent.spec.debug("pSPECS");
			self.specs = parent.spec;
			self.index = idx;
			self.subindex = subidx;
			self.parent_collection = { parent };
			self.vals = IdentityDictionary.new;
		
			self.build_vals;
			self.build_pbind;
			self.is_present = false;


			//self.load_data; // doesnt exists, called on collection level

			self;
		},

		label: { arg self;
			"%.%.%".format(self.node_uname, self.index+1, self.name);
		},

		activate_display: { arg self, view;
			Veco.main.score_preset.activate_display(self.parent_collection, view);
		},

		duplicate_current_score: { arg self, view;
			Veco.main.choose_score_preset.activate_display(self, view, { arg scene, pos;
				var idx = pos.y;
				var subidx = pos.x;
				var win;
				self.score_preset_destination_pos = pos;
			});
		},

	);

	~class_score_notes = (
		parent: ~class_score_base,

		new: { arg self, parent, idx, subidx;
			self = self.deepCopy;

			self.name = parent.name;
			self.node_uname = parent.node_uname;
			self.specs = parent.spec;
			self.index = idx;
			//self.subindex = subidx;
			self.parent_collection = { parent };
			self.is_present = false;

			self;
		},

		label: { arg self;
			"%.%.%".format(self.node_uname, self.index+1, self.name);
		},

		//////////// save/load

		new_from_score: { arg self, parent, index, score;
			self = self.deepCopy;
		
			self.name = parent.name;
			self.node_uname = parent.node_uname;
			self.index = index;
			//self.subindex = subidx;
			self.parent_collection = { parent };
			self.is_present = false;

			self.load_data_from_score(score);
		
			self;
		},

		load_data_from_score: { arg self, score;
			//self.set_data(score.save_data_to_struct.deepCopy)
			self.set_event_score(score.event_score.deepCopy);
		},

		set_data: { arg self, data;
			var ns = ~make_notescore.();
			ns.set_notes(data);
			self.is_present = true;
			self.event_score = ns;
		},

		get_compile_string: { arg self;
			var notes = self.event_score.get_rel_notes.collect { arg note;
				"\t"++note.asCompileString++",\n";
			};
			"[\n%]".format(notes.join);
		},

		save_data: { arg self, data;
			self.parent_collection.save_data;
		},

		//// wrappers methods

		record: { arg self, record_dur, slice_dur, pat;

			pat = pat ?? { Pdef(~name) };

			self.recorder = {Veco.main.piano_recorder};
			self.recorder.set_note_pattern(pat);
			Veco.main.recorder_view.set_recorder(Veco.main.piano_recorder);
			Veco.main.piano_recorder.start_recording(record_dur, slice_dur, { arg recorder;
				self.activate_display;
				self.set_event_score(recorder.slices[max(recorder.slices.size-2, 0)]);
			});
			
		},

		stop_recording: { arg self;
			self.recorder.stop_recording;
		},

		cancel_recording: { arg self;
			self.recorder.cancel_recording;
		},

		pbind: { arg self, repeat=inf;
			//self.event_score.get_rel_notes; // TODO: is it needed ?
			Pbind(\freq, Pfunc({ arg ev; (ev[\event_key] ?? 1).midicps })) <>
				Pn(
					Plazy({
						if(self.event_score.isNil) {
							debug("silence");
							Event.silent(1);
						} {
							Pseq(self.event_score.get_cached_rel_notes)
						}
					}),
					repeat,
				);
		},

		set_event_score: { arg self, event_score;
			event_score.debug("class_score_notes:set_event_score:event_score");
			self.is_present = true;
			self.event_score = event_score;
			self.save_data;
		},

		activate_display: { arg self;
			Veco.main.score_notes.activate_display(self, self.recorder);
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
			//self.all.dump;
			[self.node_uname, self.name, self.all.indices, Veco.main.project_path, idx].debug("class_score_collection.get");

			res = self.all.at(idx);
			if(res.isNil) {
				var new = self.score_class.new(self, idx);
				self.all.put(idx, new);
				res = self.all.at(idx);
			};
			res;
		},

		is_score_present: { arg self, idx;
			var res = self.all.at(idx);
			if(res.isNil) {
				false
			} {
				res.is_present;
			}
		},

		set_data: { arg self, idx, data;
			self.get(idx).set_data(data);
		},
	);

	~class_score_2d_collection = (
		parent: ~class_score_collection,

		new: { arg self, node_uname, name, spec;
			self = self.deepCopy;

			self.name = name;
			self.node_uname = node_uname;
			self.spec = spec;
		
			self.all = Order.new;
			self.active_subindex = Order.new; //TODO: active_subindex not saved

			//self.load_data; // can't be called now because data file can't access to score because not finished created yet
			//self.score_class = class;
		
			self;
		},

		load_data_from_score: { arg self, idx, score;
			var res;
			var order;
			var src_idx = score.index;
			var src_order = score.parent_collection.all.at(src_idx);
			var newsub;
			//FIXME: should I and where to verify if dest score already exists so no overwriting occurs ?

			order = self.all.at(idx);
			if(order.isNil) {
				newsub = Order.new;
				self.all.put(idx, newsub);
				order = newsub;
			};
			src_order.do { arg src_score, subidx;
				if(src_score.notNil) {
					var dst_score = order.at(subidx);
					if(dst_score.isNil) {
						var new = self.score_class.new_from_score(self, idx, subidx, src_score);
						[self.node_uname, idx, subidx].debug("class_score_2d_collection: load_data_from_score");
						order.put(subidx, new);
						dst_score = new;
					} {
						dst_score.load_data_from_score(src_score);
					}
				};
			};

			[self.active_subindex[idx], score.parent_collection.active_subindex[src_idx], idx, src_idx].debug("before loading active subidx");
			self.set_active_subindex(idx, score.parent_collection.active_subindex[src_idx]);
			[self.active_subindex[idx], score.parent_collection.active_subindex[src_idx]].debug("after loading active subidx");

			self.save_data;

			res;
			nil; // FIXME: why return something ?
			
		},

		get_active_subindex: { arg self, idx;
			self.active_subindex[idx] ?? 0
		},

		set_active_subindex: { arg self, idx, val;
			[idx, val].debug("set_active_subindex");
			self.active_subindex[idx] = val;
			Pdef("%.%.%".format(self.node_uname, idx+1, self.name).asSymbol, self.get(idx, val).pbind);
		},

		set_active_subindexes: { arg self, order;
			order.do { arg subidx, idx;
				self.set_active_subindex(idx, subidx);
			}
		},

		get: { arg self, idx, subidx;
			var res;
			var subres;
			//self.all.dump;
			//[self.node_uname, self.name, self.all.indices, Veco.main.project_path, idx].debug("class_score_collection.get");

			//[idx, subidx].debug("scorecol.get: idx, subidx 1");
			if(subidx.isNil) {
				subidx = self.get_active_subindex(idx)
			};
			//[idx, subidx].debug("scorecol.get: idx, subidx 2");

			res = self.all.at(idx);
			if(res.isNil) {
				var newsub = Order.new;
				self.all.put(idx, newsub);
			};
			subres = self.all.at(idx).at(subidx);
			if(subres.isNil) {
				var new = self.score_class.new(self, idx, subidx);
				self.all.at(idx).put(subidx, new);
				subres = new;
			};
			subres;
		},

		getsub: { arg self, subidx;
			self.get(self.active_clip_index, subidx);
		},


		delete: { arg self, idx, subidx;
			if(idx.isNil) {
				idx.debug("idx is NIL !!!")
			} {
				self.all.put(idx, Order.new);
			}
		},

		is_score_present: { arg self, idx, subidx;
			var res = self.all.at(idx);
			subidx = subidx ?? { self.get_active_subindex(idx) };
			if(res.isNil) {
				false
			} {
				res = self.all.at(idx).at(subidx);
				if(res.isNil) {
					false
				} {
					res.is_present;
				}
			}
		},

		set_data: { arg self, idx, subidx, data;
			self.get(idx, subidx).set_data(data);
		},

	);

	~class_score_notes_wrapper = (
		parent: ~class_score_collection,

		score_class: ~class_score_notes,

		save_data: { arg self;
			File.use(self.data_path, "w", { arg file;
				var str = "";
				self.all.do { arg val, idx;
					if(val.notNil and: { val.is_present == true }) {
						str = str ++ (
							"\nSdef(%, %).set_data(%, %);\n"
						).format(
							self.node_uname.asCompileString,
							self.name.asCompileString,
							idx,
							val.get_compile_string
						);
					}
				};
				file.write(str);

			});
		},

		set_data: { arg self, index, data;
			self.get(index).set_data(data);
		},

		load_data: { arg self;
			self.data_path.load;
		},

		clone: { arg self, node_uname, name;
			~class_score_notes_wrapper.new(node_uname, name);
		},

		record: { arg self ... args;
			self.get_active_score.record(*args);
		},

		stop_recording: { arg self ... args;
			self.get_active_score.stop_recording(*args);
		},

		cancel_recording: { arg self ... args;
			self.get_active_score.cancel_recording(*args);
		},

		pbind: { arg self ... args;
			self.get_active_score.pbind(*args);
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
		parent: ~class_score_2d_collection,

		score_class: ~class_score_preset,

		clone: { arg self, node_uname, name;
			~class_score_preset_wrapper.new(node_uname, name);
		},

		load_data: { arg self;
			self.data_path.load;
		},

		rebuild: { arg self, clear_old=false;
			self.all.do { arg order;
				order.do { arg score;
					score.rebuild(clear_old);
				}
			};
			self.save_data;
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
				self.all.do { arg order, idx;

					if(order.notNil) {
						order.do { arg val, subidx;
						
							if(val.notNil and: { val.is_present == true }) {
								str = str ++ (
									"\nSdef(%, %).set_data(%, %, \n%\n);\n"
								).format(
									self.node_uname.asCompileString,
									self.name.asCompileString,
									idx, subidx,
									struct_to_string.(val.save_data_to_struct)
								);
							}
						}

					}
				};
				str = "%\n\nSdef(%,%).set_active_subindexes(Order.newFromIndices(%, %));\n".format(
					str,
					self.node_uname.asCompileString,
					self.name.asCompileString,
					self.active_subindex.array.asCompileString,
					self.active_subindex.indices.asCompileString,
				);
				file.write(str);

			});
		},

		morph: { arg self, key, idx_list, method=\pbind;
			var preset_list;
			preset_list = idx_list.collect { arg pos;
				var idx;
				var subidx;
				if(x.class == Point) {
					idx = pos.y;
					subidx = pos.x;
				} {
					idx = self.active_clip_index;
					subidx = pos;
				};
				[idx,subidx].debug("preset: morph: what!!");
				if(method == \pbind) {
					self.get(idx, subidx).pbind;
				} {
					self.get(idx, subidx).valpbind;
				}
			};
			//Sdef(self.node_uname, self.name, \preset, [key, \I])
			
			~ilmorphpat.(preset_list, key);
		},

		pbind: { arg self;
			//Pfunc { self.get_active_score.pbind };
			//Pdef('3.mypreset.2')
			self.get_active_score.pbind;
		},

		pdef: { arg self;
			Pdef("%.%".format(~namex, self.name).asSymbol);
		},

		specs: { arg self;
			self.get_active_score.specs;
		},

		edit: { arg self;
			self.get_active_score.edit;
		},

		get_val: { arg self, key;
			self.get_active_score.get_val(key);
		},

		set_val: { arg self, key, val;
			"2".debug;
			self.get_active_score.set_val(key, val);
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


	/////////////////////////

)

