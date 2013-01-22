(

~empty_note = (
	midinote: \rest,
	type: \rest,
	slotnum: \rest,
	sustain: 0.1,
	velocity: 0.8,
	emptynote: true,
	dur: 1.12345
);


~default_noteline3 = [ // FIXME: crash when no notes
	(
		midinote: \rest,
		type: \rest,
		sustain: 0.1,
		start_silence: 0.5,
		default_start_silence: 0.5,
		end_silence: 2.0,
		default_end_silence: 2.0,
		start_offset: 0,
		end_offset: 0,
		velocity: 0.8,
		dur: 0.5
	),
	(
		midinote: 68,
		sustain: 0.1,
		velocity: 0.8,
		dur: 1.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		velocity: 0.8,
		dur: 2.0
	),
];
~default_noteline = [ // FIXME: crash when no notes
	(
		midinote: \rest,
		type: \rest,
		sustain: 0.1,
		start_silence: 0.5,
		default_start_silence: 0.5,
		end_silence: 0.5,
		default_end_silence: 0.5,
		start_offset: 0,
		end_offset: 0,
		velocity: 0.8,
		dur: 0.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		velocity: 0.8,
		dur: 0.9
	),
	(
		midinote: 67,
		sustain: 0.1,
		velocity: 0.8,
		dur: 0.1
	),
	(
		midinote: 68,
		sustain: 0.1,
		velocity: 0.8,
		dur: 1.5
	),
	(
		midinote: 64,
		sustain: 0.1,
		velocity: 0.8,
		dur: 0.5
	),
	(
		midinote: 66,
		sustain: 0.1,
		velocity: 0.8,
		dur: 0.5
	),
];
~default_noteline2 = [
	(
		midinote: 64,
		sustain: 0.1,
		velocity: 0.8,
		dur: 0.5
	),
	(
		midinote: 65,
		sustain: 0.1,
		velocity: 0.8,
		dur: 0.4
	),
	(
		midinote: 66,
		sustain: 0.1,
		velocity: 0.8,
		dur: 0.9
	),
	(
		midinote: 67,
		sustain: 0.1,
		velocity: 0.8,
		dur: 0.1
	),
	(
		midinote: 68,
		sustain: 0.1,
		velocity: 0.8,
		dur: 1.5
	),
	(
		midinote: 69,
		sustain: 0.1,
		velocity: 0.8,
		dur: 0.6
	)
];

~default_sampleline = [ // FIXME: crash when no notes
	(
		slotnum: \rest,
		type: \rest,
		sustain: 0.1,
		start_silence: 0.5,
		default_start_silence: 0.5,
		end_silence: 2.0,
		default_end_silence: 2.0,
		start_offset: 0,
		end_offset: 0,
		velocity: 0.8,
		dur: 0.5
	),
	(
		slotnum: 0,
		sustain: 0.1,
		velocity: 0.8,
		dur: 1.5
	),
	(
		slotnum: 0,
		sustain: 0.1,
		velocity: 0.8,
		dur: 2.0
	),
];
~event_rel_to_abs = { arg li;	
	var res = List.new, elm, time;
	0.for(li.size-1) { arg x;
		x.debug("iter");
		elm = li[x].copy;
		if(x == 0) {
			elm.time = 0;
		} {
			elm.time = li[x-1].dur + res[x-1].time;
		};
		res.add(elm);
	};
	res;
};

~event_abs_to_rel = { arg li;	
	var res = List.new, elm, time;
	0.for(li.size-1) { arg x;
		x.debug("iter");
		elm = li[x].copy;
		if(x == 0) {
			//elm.time = 0;
		} {
			res[x-1].dur = elm.time - res[x-1].time;
		};
		res.add(elm);
	};
	res;
};


~merge_notetracks = { arg no1, no2;
	var ano1, ano2, res;
	var makeabs, makerel, time = 0, last = 0, elm;
	makerel = { arg li;
		var res = List.new, elm, time;
		0.for(li.size-1) { arg x;
			elm = li[x].copy;
			if( x == (li.size-1) ) {
			} {
				elm.dur = li[x+1].time - li[x].time
			};
			elm.time = nil; // tidy up
			res.add(elm);
		};
		res;
	};
	makeabs = { arg li;	
		var res = List.new, elm, time;
		0.for(li.size-1) { arg x;
			x.debug("iter");
			elm = li[x].copy;
			if(x == 0) {
				elm.time = 0;
			} {
				elm.time = li[x-1].dur + res[x-1].time;
			};
			res.add(elm);
		};
		res;
	};
	ano1 = makeabs.(no1);
	ano2 = makeabs.(no2);
	
	res = ano1 ++ ano2;
	res.sort({ arg a, b; 
		if(a.time == b.time) {
			// always put header in first in case of equality
			a.start_silence.notNil
		} {
			a.time < b.time
		}
	});
	res = makerel.(res);

	res = res[1..]; // remove double rest
	res[0].start_silence = res[0].dur;
	res[0].end_silence = res.last.dur;

	res;

};

~concat_notescores = { arg ns1, ns2;
	var shift = ns1.abs_end;
	var res = ns1.deepCopy;
	var newno;
	ns2.get_abs_notes.collect { arg no;
		newno = no.deepCopy;	
		newno.time = newno.time + shift;
		res.notes = res.notes.add(newno);
	};
	res.abs_end = shift + ns2.abs_end;
	res;
};


~make_notescore = { 
	(
		notes: List.new,
		book: Dictionary.new,
		notequant: nil, // used externally to quantify note time
		abs_start: 0,
		no_first_rest: false,

		sort_func: { arg a, b;
			a.time < b.time;
		},

		add_note: { arg self, note, abstime, num;
			var no = note.deepCopy;
			var id;
			if(num.notNil) {
				self.book[num] = no;
			};
			no.time = abstime;
			self.notes.add(no);
			id = self.notes.size-1;
			id;
		},

		add_noteoff: { arg self, num, abstime;
			var no = self.book[num];
			no.sustain = abstime - no.time;
			self.book.removeAt(num);
		},

		remove_note: { arg self, num;
			// warning: old note id returned by add_note will be corrupted
			self.notes.removeAt(num);
		},

		remove_notes_at_abstime: { arg self, abstime;
			self.notes = self.notes.reject { arg no; no.time == abstime }.asList;
		},

		remove_notes_playing_at_abstime: { arg self, abstime, constraint;
			self.notes = self.notes.reject { arg no;
				(abstime >= no.time) and: {
					(abstime < (no.time + no.sustain)) and: {
						if(constraint.notNil) {
							constraint.(no)
						} {
							true
						}
					}
				} 
			}.asList;
		},

		is_note_playing_at_abstime: { arg self, abstime;
			self.notes.any { arg no;
				(abstime >= no.time) and: {
					abstime < (no.time + no.sustain)
				}
			};
		},

		set_start: { arg self, abstime;
			self.abs_start = abstime;
		},

		compute_end: { arg self, set=false;
			var end = 0, tmpno = (sustain:1);
			var notes;
			notes = self.notes.copy.sort(self[\sort_func]);
			notes.do { arg no;
				if(no.time.isNil) {
					no.debug("ERROR: make_notescore: compute_end: note with time == nil");
				} {
					if(end < no.time) {
						end = no.time;
						tmpno = no;
					}
				}
			};
			end = end + tmpno.sustain;
			if(set) {  self.set_end(end); };
			end;
		};,

		set_end: { arg self, abstime;
			self.abs_end = abstime;
			self.notes.do { arg no;
				if(no.sustain.isNil) {
					no.sustain = abstime - no.time
				};
			};
		},

		get_end: { arg self;
			if(self.abs_end.isNil) {
				self.compute_end(false);
			} {
				self.abs_end;
			}
		},

		set_notes: { arg self, notes;
			self.notes = ~event_rel_to_abs.(notes);
			self.abs_start = 0;
			//self.abs_end = self.notes.last.dur + self.notes.last.time; //FIXME: why set abs_end ? does it break thing to not set it ?
		},

		set_abs_notes: { arg self, notes;
			self.notes = notes.asList;
			self.abs_start = 0;
			//self.abs_end = self.notes.last.dur + self.notes.last.time;
		},

		get_note: { arg self, note_id;
			self.notes[note_id];
		},

		get_note_by_index: { arg self, note_idx;
			self.notes[note_idx];
		},

		set_quant: { arg self, quant;
			self.quant = quant;
		},

		get_wait_note: { arg self;
			var no;
			no = ~empty_note.copy;
			no.dur = self.notes[0].time - self.abs_start;
			no;
		},

		get_rel_notes: { arg self, start=0, dur=nil;
			var notes, last, end;
			start = self.abs_start + start;
			end = if(dur.isNil) {
				if(self.abs_end.isNil) {
					self.compute_end(false);
				} {
					self.abs_end;
				}
			} {
				start + dur;
			};
			// add offset and last dur
			notes = self.notes.copy.sort(self[\sort_func]);
			notes = notes.select { arg no;
				//no.time >= start and: { no.time <= end }
				no.time >= start and: { no.time < end }
			};
			if(self.no_first_rest.not) {
				notes = [(
					midinote: \rest,
					type: \rest,
					slotnum: \rest,
					sustain: 0,
					time: start // FIXME: why time=start ? c'est sensé etre la premiere note
				)] ++ notes;
			};
			notes = ~event_abs_to_rel.(notes);
			last = notes.last;
			last.dur = end - last.time;
			if(last.dur < 0) {
				"GRAVE ERREUR, la derniere note est negative!!!".error;
			};

			// legacy vars:
			notes[0].start_silence = notes[0].dur;
			notes[0].start_offset = 0;
			notes[0].end_silence = last.dur;
			notes[0].end_offset = 0;

			notes;
		},

		cut_exceeding_notes: { arg self, start=0, dur=nil;
			self.notes = self.get_abs_notes(start, dur);
		},

		split_in_slices: { arg self, dur;
			var total_dur = self.abs_end - self.abs_start;			
			var numslices = (total_dur / dur).asInteger;
			var pos = self.abs_start;

			[total_dur, dur, numslices].debug("split_in_slices: totaldur, dur, numslices");
			numslices.collect { arg i;
				var res;
				res = self.deepCopy;
				res.abs_start = pos;
				pos = pos+dur;
				res.abs_end = pos;
				res;
			};
		},

		filter_by_slot: { arg self, slotnum;
			var res = self.deepCopy;
			//[slotnum, res.notes].debug("filter_by_slot: before");
			res.notes = res.notes.select { arg no;
				no.slotnum == slotnum
			};
			//res.notes.debug("filter_by_slot: after");
			res;
		},

		get_abs_notes: { arg self, start=0, dur=nil;
			var notes, last, end;
			start = self.abs_start + start;
			end = if(dur.isNil) {
				if(self.abs_end.isNil) {
					self.compute_end(false);
				} {
					self.abs_end;
				}
			} {
				start + dur;
			};

			notes = self.notes.copy.sort(self[\sort_func]);
			notes = notes.select { arg no;
				//no.time >= start and: { no.time <= end }
				no.time >= start and: { no.time < end }
			};
			notes;
		}
	)
};

~make_scoreset = {
	var scoreset = (
		notes: List.new,
		slice_start: 0,
		slice_dur: nil, // till end

		update_notes: { arg self;
			self.notes = self.notescore.get_rel_notes(self.slice_start, self.slice_dur);
			//self.notes.debug("make_notescore.update_notes:notes");
		},

		set_notescore: { arg self, ns;
			self.notescore = ns;
			self.update_notes;
		},

		get_note: { arg self, index;
			self.notescore.get_note_by_index(index)
		},

		get_notes: { arg self;
			self.notes;
		},

		set_current_slice: { arg self, start, dur;
			self.slice_start = start;
			self.slice_dur = dur;
			self.update_notes;
		},

		init: { arg self;
			debug("~make_scoreset:init");
			self.notescore = ~make_notescore.();
			self.notescore.set_notes(~default_noteline3);
			self.update_notes;

		}

	);
	scoreset.init;
	scoreset;
};

~make_scoreset_hack = { arg param;
	(
		parent: ~make_scoreset.(),

		start_offset: 0,
		end_offset: 0,
		notequant: nil,
		vnotes: [],
		history_len: 8,
		history: List.new,
		history_index: 0,
		archive_data: [\history_index, \history_len, \history, \next_notescore, \notescore],


		save_data: { arg self;
			var data = Dictionary.new;
			self.archive_data.do { arg key;
				data[key] = self[key];
			};
			data;
		},

		load_data: { arg self, data;
			self.archive_data.do { arg key;
				self[key] = data[key];
			};
		},

		update_notes: { arg self;
			var notes, qnotes;
			var realdur, normdur;
			var vnotes;
			var size;
			notes = self.notescore.get_rel_notes(self.slice_start, self.slice_dur);

			notes.collect({arg no, x; no.numero = x }); // debug purpose
			//notes.debug("update_note_dur: original notes");
			qnotes = notes.collect{ arg x;
				// quantify notes
				x = self.quantify_note(x);
				// set midinote to be compatible with noteline code
				if(x.midinote.isNil) {
					x.midinote = x.slotnum;
				};
				x;
			};
			//qnotes.debug("update_note_dur: qnotes");

			// troncate too long end silence
			normdur = self.total_dur(notes);
			if(self.notes_dur.notNil && (normdur != self.notes_dur)) {
				"fucking bug in recording length!!!!!!!!".debug;
				self.notes_dur.debug("or just unlimited recording... notes_dur");
				self.notes_dur = nil;
			};
			realdur = self.total_dur(qnotes);
			qnotes.last.dur = qnotes.last.dur - (realdur - (self.notes_dur ?? normdur));
			~mydur = self.total_dur(qnotes);
			[self.notes_dur, realdur, normdur, ~mydur].debug("duration::::: notes_dur, real, norm, end");

			if(qnotes.last.dur < 0) {
				"ERROR: mon hack degueux fonctionne pas vraiment et c'est la grosse merde".error;
			};

			// remove duplicates

			size = qnotes.size-1;
			vnotes = List.new;
			qnotes.do { arg no, x;
				if(x < size) {
					if((no.dur == 0) && (no.midinote == qnotes[x+1].midinote)) {
						// duplicate: don't add
						no.debug("found duplicate: don't add");
					} {
						vnotes.add(no);
					};
				} {
					vnotes.add(no);
				}
			};

			//vnotes.debug("update_note_dur: vnotes");
			self.notes = vnotes;

			param.changed(\notes);
			//param.debug("UPDATED NOTES !!!!");
		},

		quantify_note: { arg self, note, quant;
			var res;
			quant = quant ?? self.notescore.notequant;
			//note.debug("quantify_note input");
			res = note.deepCopy;
			if(quant.notNil) {
				res.dur = res.dur.round(quant);
				res;
			} {
				res;
			};
			//res.debug("quantify_note output");
		},

		//get_notes: { arg self;
		//	var no;
		//	no = self.vnotes.deepCopy;
		//	//no[0].dur+self.start_offset;
		//	//no.last.dur+self.end_offset;
		//	no;
		//},

		set_notes: { arg self, val;
			self.notescore.set_notes(val);
			self.update_notes;
			//self.notes = val;
			//self.update_note_dur;
		},

		set_notescore: { arg self, val;
			self.notescore = val;
			self.update_notes;
		},

		get_notescore: { arg self;
			self.notescore;
		},


		set_next_notes: { arg self, val, dur=nil;
			if(self.pat_finish_first.notNil) {
				// le pattern a deja commencé, assigner les notes tout de suite
				"**** set_next_notes: assigning next_notes as current notes immediately".debug;
				self.set_notes(val);
				self.pat_finish_first = nil;
			} {
				"**** set_next_notes: preparing next_notes".debug;
				self.next_notes = val;
			};
			self.notes_dur = dur;
		},

		set_slice_dur: { arg self, dur;
			self.slice_dur = dur;
		},

		add_to_history: { arg self, ns, slicedur=nil;
			var slices;
			if(slicedur.isNil) {
				slices = [ns];
			} {
				slices = ns.split_in_slices(slicedur);
			};
			self.history = (slices.reverse ++ self.history).keep(self.history_len);
			self.history_index = 0;
		},

		get_last_in_history: { arg self;
			self.history[0];
		},

		get_current_in_history: { arg self;
			self.history[self.history_index];
		},

		backward_in_history: { arg self;
			self.history_index = (self.history_index + 1).clip(0, self.history.size);
			self.set_next_notescore(self.history[self.history_index]);
			//self.history[self.history_index].debug("backward_in_history: notescore");
		},
		
		forward_in_history: { arg self;
			self.history_index = (self.history_index - 1).clip(0, self.history.size);
			self.set_next_notescore(self.history[self.history_index]);
			//self.history[self.history_index].debug("forward_in_history: notescore");
		},

		set_next_notescore_history: { arg self, val, dur=nil;
			self.add_to_history(val, dur);
			if(dur.isNil) {
				"set_next_notescore: dur is nil, wtf ?".debug;
			};
			if(self.pat_finish_first.notNil) {
				// le pattern a deja commencé, assigner les notes tout de suite
				"**** set_next_notes: assigning next_notes as current notes immediately".debug;
				self.set_notescore(self.get_last_in_history);
				self.pat_finish_first = nil;
			} {
				"**** set_next_notes: preparing next_notes".debug;
				self.next_notescore = self.get_last_in_history;
			};
			self.notes_dur = dur;
			self.slice_dur = dur;
		},

		set_next_notescore: { arg self, val, dur=nil;
			if(self.pat_finish_first.notNil) {
				// le pattern a deja commencé, assigner les notes tout de suite
				"**** set_next_notes: assigning next_notes as current notes immediately".debug;
				self.set_notescore(val);
				self.pat_finish_first = nil;
			} {
				"**** set_next_notes: preparing next_notes".debug;
				self.next_notescore = val;
			};
			self.notes_dur = dur;
			//self.slice_dur = dur;
		},

		set_next_notes_as_current_notes: { arg self;
			self.set_notes(self.next_notes);
			self.next_notes = nil;
			self.wait_note = nil;
		},

		forward_to_next_notescore: { arg self;
			self.set_notescore(self.next_notescore);
			self.next_notescore = nil;
			self.wait_note = nil;
		},

		set_wait_note: { arg self, note;
			"**** nline: set_wait_note".debug;
			self.wait_note = self.quantify_note(note);
			self.wait_note[\first_note] = true;
			self.wait_note.debug("wait_note");
			note.debug("originial note");
		},

		get_note: { arg self, param, idx;
			// notescore version
			var no;
			if( self.notes.size > 0 && {param.muted.not}) {
				if(idx == 0) {
					// s'il y a deja des next_notes lorsque la note 0 arrive (debut du pattern), c'est que le record a fini _avant_ le pattern
					// s'il n'y en a pas mais qu'il y a quand meme une wait_note c'est que le record va finir _apres_ le pattern
					// s'il n'y a ni l'un ni l'autre, c'est que c'est un banale debut de pattern, et cela doit continuer normalement
					if(self.next_notescore.notNil) {
						"******** recording finish first, using next_notes as current notes".debug;
						self.forward_to_next_notescore;
					} {
						if(self.wait_note.notNil) {
							"********* recording not yet finished".debug;
							"***** there is a wait note, using it as first note".debug;
							no = self.wait_note;
							self.wait_note = nil;
							self.pat_finish_first = true;
						} {
							no = self.notes[idx].deepCopy;
						};
					}
				} {
					no = self.notes[idx].deepCopy;
				};
				no;
			} {
				if(param.muted) {
					"noteline_param: get_note: muted!".inform;
				} {
					if(self.next_notescore.notNil) {
							"setting next_notes when no notes found".debug;
							self.forward_to_next_notescore;
					};
					"noteline_param: get_note: No notes".inform;
				};
				if(idx == 0) {
					~empty_note;
				} {
					nil;
				};
			}
		},
		get_note2: { arg self, param, idx;
			// note version
			var no;
			if( self.notes.size > 0 && {param.muted.not}) {
				if(idx == 0) {
					// s'il y a deja des next_notes lorsque la note 0 arrive (debut du pattern), c'est que le record a fini _avant_ le pattern
					// s'il n'y en a pas mais qu'il y a quand meme une wait_note c'est que le record va finir _apres_ le pattern
					// s'il n'y a ni l'un ni l'autre, c'est que c'est un banale debut de pattern, et cela doit continuer normalement
					if(self.next_notes.notNil) {
						"******** recording finish first, using next_notes as current notes".debug;
						self.set_next_notes_as_current_notes;
					} {
						if(self.wait_note.notNil) {
							"********* recording not yet finished".debug;
							"***** there is a wait note, using it as first note".debug;
							no = self.wait_note;
							self.wait_note = nil;
							self.pat_finish_first = true;
						} {
							no = self.notes[idx].deepCopy;
						};
					}
				} {
					no = self.notes[idx].deepCopy;
				};
				no;
			} {
				if(param.muted) {
					"noteline_param: get_note: muted!".inform;
				} {
					if(self.next_notes.notNil) {
							"setting next_notes when no notes found".debug;
							self.set_notes(self.next_notes);
							self.next_notes = nil;
							self.wait_note = nil;
					};
					"noteline_param: get_note: No notes".inform;
				};
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
			// manage start and end offset and silence and put result notes in self.vnotes
			var find_next, find_prev, delta, prevdelta, idx, previdx;
			var qnotes, normdur, realdur, size;
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

				// quantify notes
				self.notes.collect({arg no, x; no.numero = x }); // debug purpose
				self.notes.debug("update_note_dur: original notes");
				qnotes = self.notes.collect{ arg x; self.quantify_note(x) };
				qnotes.debug("update_note_dur: qnotes");
				
				// troncate too long end silence
				normdur = self.total_dur(self.notes);
				if(self.notes_dur.notNil && (normdur != self.notes_dur)) {
					"fucking bug in recording length!!!!!!!!".debug;
				};
				realdur = self.total_dur(qnotes);
				qnotes.last.dur = qnotes.last.dur - (realdur - (self.notes_dur ?? normdur));
				~mydur = self.total_dur(qnotes);
				[self.notes_dur, realdur, normdur, ~mydur].debug("duration::::: notes_dur, real, norm, end");

				if(qnotes.last.dur < 0) {
					"ERROR: mon hack degueux fonctionne pas vraiment et c'est la grosse merde".error;
				};

				// remove duplicates

				size = qnotes.size-1;
				self.vnotes = List.new;
				qnotes.do { arg no, x;
					if(x < size) {
						if((no.dur == 0) && (no.midinote == qnotes[x+1].midinote)) {
							// duplicate: don't add
							no.debug("found duplicate: don't add");
						} {
							self.vnotes.add(no);
						};
					} {
						self.vnotes.add(no);
					}
				};

				self.vnotes.debug("update_note_dur: vnotes");


				// old code to calculate offsets when recording non standard durations

				//#delta, idx = find_next.(self.notes[0].start_offset, self.notes);
				//#prevdelta, previdx = find_prev.(self.notes[0].end_offset, self.notes);
//				#delta, idx = find_next.(qnotes[0].start_offset, qnotes);
//				#prevdelta, previdx = find_prev.(qnotes[0].end_offset, qnotes);
//				[delta, idx, prevdelta, previdx].debug("delta, idx, prevdelta, previdx");
//				qnotes[0].dur = qnotes[0].start_silence + delta;
//				self.vnotes = [qnotes[0]] ++ qnotes[idx..previdx].deepCopy;
//				self.vnotes[self.vnotes.lastIndex].dur = qnotes[0].end_silence + prevdelta;
//				self.vnotes.debug("update_note_dur: vnotes");
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
					// quantify notes
					self.vnotes = self.vnotes.collect{ arg x; self.quantify_note(x) };
					self.vnotes.debug("update_note_dur: vnotes quant");
					self.changed(\notes);
				}
			};
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
			};
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
			var res;
			debug("get_start_silence");
			res = if(self.notes.size > 0) {
				self.notes[0].start_silence;
			} {
				0
			};
			res.debug;

		},

		get_end_silence: { arg self;
			var res;
			debug("get_end_silence");
			res = if(self.notes.size > 0) {
				self.notes[0].end_silence;
			} {
				0
			};
			res.debug;

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

		strip_note: { arg self, no;
			var resno = ();
			[\velocity, \dur, \sustain, \midinote, \slotnum].do { arg key;
				if(no[key].notNil) {
					resno[key] = no[key]
				}
			};
			resno;
		},

		get_notes_pattern: { arg self, notes, strip=true;
			notes = notes ?? self.get_notes;
			if(strip) {
				notes = notes.collect { arg no;
					var res;
					res = self.strip_note(no);
					if(res[\sustain] < 0) { 
						res[\sustain].debug("get_notes_pattern: negative sustain, fixing it");
						res[\sustain] = res[\sustain].abs;
					};
					res;
				};
			};
			Pseq(notes);
		},


		set_notequant: { arg self, val;
			self.notescore.notequant = val;
			self.update_notes;
		},

		get_notequant: { arg self, val;
			self.notescore.notequant;
		},

	)
};

)
