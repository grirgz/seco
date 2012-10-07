// ==========================================
// NODE MANAGER
// ==========================================

~make_node_manager = { arg main;
	(
		model: { main.model },

		default_newnode: [\libnode, \pulsepass],

		get_node: main[\get_node],
		node_exists: main[\node_exists],
		make_livenodename_from_libnodename: main[\make_livenodename_from_libnodename],

		find_free_name: main[\find_free_name],
		make_newlivenodename_from_livenodename: main[\make_newlivenodename_from_livenodename],
		make_livenode_from_libnode: main[\make_livenode_from_libnode],
		duplicate_livenode: main[\duplicate_livenode],

		make_groupplayer: { arg self, name, kind=\par;
			var pl;
			if(kind == \par) {
				pl = ~make_parplayer.(main);
			} {
				pl = ~make_seqplayer.(main);
			};
			pl.name = name;
			pl.uname = name;
			main.add_node(pl);
			pl;
		},

		start_tempo_recorder: { arg self, player, kind=\limited;
			// TODO: handle when recording finish (play recorded track along what is playing ?)
			var finish = {
				"node_manager: start_tempo_recorder: finish action".debug;
				main.play_manager.set_recording(false);
			};
			kind = \unlimited; // DEBUG;
			if(main.play_manager.is_recording.not) {
				case
					{ player.subkind == \nodesampler } {
						main.play_manager.set_recording(true);
						player.name.debug("start_tempo_recorder: nodesampler");
						self.recorder = ~make_midi_recorder.(player, main);
						self.recorder.player_start_tempo_recording(finish, kind);
					}
					{ player.kind == \player && (player.name != \voidplayer) } {
						main.play_manager.set_recording(true);
						player.name.debug("start_tempo_recorder: player");
						if(player.is_audiotrack) {
							"start_tempo_recorder:audio recorder player!!".debug;
							//self.recorder = ~make_audio_recorder.(player, main);
							self.recorder = ~make_unlimited_audio_recorder.(player, main); // TODO: use kind
							self.recorder.player_start_tempo_recording(finish);
						} {
							if(player.get_mode == \stepline) {
								if(player.get_arg(\sampleline).notNil) {
									player.set_mode(\sampleline);
								} {
									player.set_mode(\noteline); 
								}
							};
							self.recorder = ~make_midi_recorder.(player, main);
							self.recorder.player_start_tempo_recording(finish, kind);
						}
					}
					{
						"INFO: grouplive: start_tempo_recorder: selected cell is not a player".inform;
					}
			} {
				"hmatrix: already recording".debug;
			};
		},

		cancel_recording: { arg self;
			if(main.play_manager.is_recording == true) {
				main.play_manager.set_recording(false);
				self.recorder.cancel_recording; // FIXME: bug with self.recording being nil
			} {
				"hmatrix: not recording".debug;
			};
		},

		toggle_recording: { arg self, player;
			if(main.play_manager.is_recording) {
				"node_manager: toggle_recording: stop_unlimited_recording".debug;
				//self.cancel_recording;
				self.recorder.stop_unlimited_recording;
			} {
				"node_manager: toggle_recording: start_tempo_recorder".debug;
				self.start_tempo_recorder(player);
			}
		},

		start_cc_recorder: { arg self, player;
			// TODO: handle when recording finish (play recorded track along what is playing ?)
			var finish = {
				main.play_manager.set_recording(false);
				//main.play_manager.keep_recording_session = true; // to play in sync with recording clock (see play_manager.start_new_session)
				//player.play_node;
			};
			if(main.play_manager.is_recording.not) {
				if(player.kind == \player && (player.name != \voidplayer)) {
					main.play_manager.set_recording(true);
					player.name.debug("start_cc_recorder: player");
					self.cc_recorder = ~make_midi_cc_recorder.(player, main);
					self.cc_recorder.player_start_tempo_recording(finish);
				} {
					"INFO: grouplive: start_cc_recorder: selected cell is not a player".inform;
				}
			} {
				"node_manager: already cc recording".debug;
			};
		},

		cancel_cc_recording: { arg self;
			if(main.play_manager.is_recording == true) {
				self.cc_recorder.cancel_recording;
				main.play_manager.set_recording(false);
			} {
				"node_manager: not cc recording".debug;
			};
		},

		start_midi_liveplayer: { arg self, player;
			if(
				((player.kind == \player) && (player.name != \voidplayer))
				or: { (player.kind == \parnode) && (player.subkind == \nodesampler)}
			) {
				if(player.is_audiotrack == true) {
					"start_midi_liveplayer:not midi".debug;
				} {
					"start_midi_liveplayer: OK".debug;
					self.midi_liveplayer = ~make_midi_liveplayer.(player, main);
					self.midi_liveplayer.start_liveplay;
				}
			} {
				"INFO: grouplive: start_midi_liveplayer: selected cell is not a player".inform;
			}
		},

		stop_midi_liveplayer: { arg self;
			if(self.midi_liveplayer.notNil) {
				self.midi_liveplayer.stop_liveplay;
				main.midi_center.install_pad_responders; // FIXME: find a way to not have to call this
			} {
				"Midi liveplayer not started: can't stop it".debug;
			}
		},

		set_default_group: { arg self, node_name;
			var group;
			group = main.get_node(node_name);
			group.name.debug("set_default_group");
			if(group.notNil) {
				self.default_group = group;
			}
		},

		add_node_to_default_group: { arg self, node;
			self.default_group.add_children(node.name);

		},

		reload_player: { arg self, player;
			var newplayer = player.clone;
			newplayer.uname = player.uname;
			newplayer.name = player.name;
			main.add_node(newplayer);
			newplayer;
		},

		duplicate_node: { arg self, nodename, newname;
			var pl;
			pl = self.get_node(nodename).clone;
			pl.name = newname;
			pl.uname = newname;
			//TODO: clone children
			main.add_node(pl);
			pl.uname;
		},

		duplicate_node_recursive: { arg self, nodename, newname;
			var pl;
			pl = self.get_node(nodename).clone;
			pl.name = newname;
			pl.uname = newname;
			//TODO: clone children
			pl.children = pl.children.collect { arg childname;
				var newchildname;
				if(childname == \voidplayer) {
					\voidplayer
				} {
					newchildname = main.make_newlivenodename_from_livenodename(childname);
					self.duplicate_node_recursive(childname, newchildname);
					newchildname
				}
			};
			pl.refresh;
			main.add_node(pl);
			pl.uname;
		},

		load_libnode: { arg self, action;
			~choose_libnode.(main, { arg libnodename, livenodename; 
				var nodename;
				"load_libnode: first func".debug;
				self.default_newnode = [\libnode, libnodename];
				nodename = main.make_livenode_from_libnode(libnodename);
				action.(nodename)

			}, { arg livenodename;
				var nodename;
				self.default_newnode = [\livenode, livenodename];
				nodename = main.duplicate_livenode(livenodename);
				action.(nodename)
			});
		},

		create_default_livenode: { arg self;
			var livenodename;
			switch(self.default_newnode[0],
				\libnode, {
					livenodename = main.make_livenode_from_libnode(self.default_newnode[1]);
				},
				\livenode, {
					livenodename = main.duplicate_livenode(self.default_newnode[1]);
				}
			);
			livenodename;
		},

		save_column_preset: { arg self, player;
			//TODO: handle synth args change
			var datalist;
			datalist = main.model.colpresetlib[player.defname];
			if( datalist.isNil ) {
				main.model.colpresetlib[player.defname] = SparseArray.newClear(4*8*10, \empty);
				datalist = main.model.colpresetlib[player.defname];
			};
			datalist = datalist.collect { arg d; 
				if( d == \empty ) { d } { d.name }
			};
			~save_player_column.(main, "SAVE column preset", player, datalist, { arg sel, offset;
				var name;
				if( sel == \empty ) {
					name = player.defname ++ "_c" ++ UniqueID.next;
				} {
					name = sel;
				};
				main.model.colpresetlib[player.defname][offset] = player.save_column_preset;
				main.model.colpresetlib[player.defname][offset].name = name;
				main.save_presetlib;
			}, { arg offset, newname;
				// rename action
				if(main.model.colpresetlib[player.defname][offset] != \empty) {
					main.model.colpresetlib[player.defname][offset].name = newname;
					main.save_presetlib;
				};
			});
		},

		load_column_preset: { arg self, player;
			var datalist;
			datalist = main.model.colpresetlib[player.defname];
			if( datalist.isNil ) {
				main.model.colpresetlib[player.defname] = SparseArray.newClear(4*8*10, \empty);
				datalist = main.model.colpresetlib[player.defname];
			};
			datalist = datalist.collect { arg d; 
				if( d == \empty ) { d } { d.name }
			};
			~save_player_column.(main, "LOAD column preset", player, datalist, { arg sel, offset;
				// load action
				if( sel == \empty ) {
					"load_column_preset: Can't load empty preset".error;
				} {
					player.load_column_preset(main.model.colpresetlib[player.defname][offset]);
				}
			}, { arg offset, newname;
				// rename action
				if(main.model.colpresetlib[player.defname][offset] != \empty) {
					main.model.colpresetlib[player.defname][offset].name = newname;
					main.save_presetlib;
				};
			});
		},

		make_fxnode_from_libnode: { arg self, libnodename;
			var livenodename;
			var player;
			livenodename = self.make_livenodename_from_libnodename(libnodename);
			player = ~make_player.(main, main.model.effectpool[libnodename]);
			player.debug("maked effect");
			player.name = livenodename;
			player.uname = livenodename;
			main.add_node(player);
			player.uname.debug("make_fxnode_from_libnode: player.uname");
		},

		duplicate_fxnode: { arg self, livenodename;
			//TODO
		},

		load_effectnode: { arg self, player, action;
			var livenode;
			~choose_effect.(main, { arg libnodename, livenodename; 
				//self.model.default_newnode = [\libnode, libnodename];
				livenodename = self.make_fxnode_from_libnode(libnodename);
				player.add_effect(livenodename);
				action.();
			}, { arg livenodename;
				//self.model.default_newnode = [\livenode, livenodename];
				livenodename = self.duplicate_fxnode(livenodename);
				livenodename.debug("actionpreset");
				player.add_effect(livenodename);
				action.();
			});
		},

		copy_node: { arg self, node;
			var uname, address;
			uname = node.uname;
			uname.debug("copied node1");
			if(uname == \void || (uname == \voidplayer)) {
				"Can't copy empty player".error;
			} {
				uname.debug("copied node2");
				main.model.clipboard = uname;
				main.model.clipboard.debug("copied node");
			};
		},

		paste_node: { arg self;
			var node;
			if( main.model.clipboard.isNil ) {
				"Can't paste: clipboard is empty".error;
			} {
				node = main.get_node(main.model.clipboard);
				if( node.kind == \player ) {
					self.duplicate_livenode(main.model.clipboard);
				} {
					"paste_node: paste groupnode: not implemented".debug;
					nil;
				};
			};

		},

		freeze_gui: { arg self, set=true;
			self.model.freeze_gui = set;
		},

		mpdef: { arg self, name, pat, instr=nil;
			var node;

			if(pat.notNil) {
					case
						{ pat.class == Pbind } {
							if(main.node_exists(name)) {
								node = main.get_node(name);
								node.set_input_pattern(pat);
								if(self.model.freeze_gui) { main.panels.side.set_current_player(node); };
								Pdef(name, node.vpattern);
							} {
								node = ~make_player_from_pbind.(main, pat);
								if(node.notNil) {
									node.name = name;
									node.uname = name;
									main.add_node(node);
									main.node_manager.add_node_to_default_group(node);
									main.panels.side.set_current_player(node);
									//main.focus_mpdef(node);
									node.get_arg(\repeat).set_val(0);
									Pdef(name, node.vpattern);
								} {
									"ERROR: player could not be created".debug;
									nil;
								};
							}

						}
						{ pat.class == Pchain } {
							if(main.node_exists(name)) {
								node = main.get_node(name);
								node.set_input_pattern(pat);
								main.panels.side.set_current_player(node);
								Pdef(name, node.vpattern);
							} {
								node = ~make_player_from_pchain.(main, pat, instr);
								if(node.notNil) {
									node.name = name;
									node.uname = name;
									main.add_node(node);
									main.node_manager.add_node_to_default_group(node);
									main.panels.side.set_current_player(node);
									//main.focus_mpdef(node);
									Pdef(name, node.vpattern);
								} {
									"ERROR: player could not be created".debug;
									nil;
								};
							}

						}
						{ pat.class == Ppar } {

							if(main.node_exists(name)) {
								node = main.get_node(name);
								node.set_input_pattern(pat);
								main.panels.side.set_current_group(node);
								Pdef(name, node.vpattern);
							} {
								node = ~make_parplayer.(main);
								if(node.notNil) {
									node.name = name;
									node.uname = name;
									main.add_node(node);
									node.set_input_pattern(pat);
									main.panels.side.set_current_group(node);
									//main.focus_mpdef(node);
									Pdef(name, node.vpattern);
								} {
									"ERROR: player could not be created".debug;
									nil;
								};
							}
						}
						// TODO: implement method to define expsets
						//{ pat.class == Pseq } {

						//	if(main.node_exists(name)) {
						//		node = main.get_node(name);
						//		node.set_input_pattern(pat);
						//		main.panels.side.set_current_group(node);
						//		Pdef(name, node.vpattern);
						//	} {
						//		node = ~make_parplayer.(main);
						//		if(node.notNil) {
						//			node.name = name;
						//			node.uname = name;
						//			main.add_node(node);
						//			node.set_input_pattern(pat);
						//			main.panels.side.set_current_group(node);
						//			//main.focus_mpdef(node);
						//			Pdef(name, node.vpattern);
						//		} {
						//			"ERROR: player could not be created".debug;
						//			nil;
						//		};
						//	}
						//}
						{
							"INFO: class not understood, using direct input pattern".debug;
							if(main.node_exists(name)) {
								node = main.get_node(name);
								node.set_input_pattern(pat);
								main.panels.side.set_current_player(node);
								Pdef(name, node.vpattern);
							} {
								node = ~make_player_from_pattern.(main, pat);
								if(node.notNil) {
									node.name = name;
									node.uname = name;
									main.add_node(node);
									main.node_manager.add_node_to_default_group(node);
									main.panels.side.set_current_player(node);
									//main.focus_mpdef(node);
									node.get_arg(\repeat).set_val(0);
									Pdef(name, node.vpattern);
								} {
									"ERROR: player could not be created".debug;
									nil;
								};
							};
						}
			} {
				Pdef(name)
			}
		},

		mdefenv: { arg self, name, pat;
			var node;
			if(pat.notNil) {
					case
						{ pat.class == Pbind } {
							if(main.node_exists(name)) {
								node = main.get_node(name);
								node.set_input_pattern(pat);
								node.set_env_mode(true);
								main.panels.side.set_current_player(node);
								Pdef(name, node.vpattern);
							} {
								node = ~make_player_from_pbind.(main, pat);
								if(node.notNil) {
									node.set_env_mode(true);
									node.name = name;
									node.uname = name;
									main.add_node(node);
									main.node_manager.add_node_to_default_group(node);
									main.panels.side.set_current_player(node);
									//main.focus_mpdef(node);
									Pdef(name, node.vpattern);
								} {
									"ERROR: player could not be created".debug;
									nil;
								};
							}

						}
						{
							"ERROR: class not understood".debug;
							Pdef(name, pat);
						}
			} {
				Pdef(name)
			}
		},

		mdefsampler: { arg self, name, samples;
			var node;
			if(main.node_exists(name)) {
				node = main.get_node(name);
				node.set_samplechildren(samples);
			} {
				node = ~make_nodesampler.(main);
				node.set_samplechildren(samples);
				node.name = name;
				node.uname = node.name;
				main.add_node(node);
				main.node_manager.add_node_to_default_group(node);
			}
		},

		mdef_scoreset: { arg self, name;
			var node;
			node = self.get_node(name);
			switch(node.get_mode,
				\noteline, { node.get_arg(\noteline).scoreset },
				\nodeline, { node.get_arg(\nodeline).scoreset },
				{ node.get_arg(\noteline).scoreset },
			)
		},

		mdef_scorepat: { arg self, name, notes=nil, strip=true;
			notes = notes ?? self.mdef_scoreset(name).get_notes_pattern(notes, strip);
		},

		add_node: main[\add_node]
	)
};

// ==========================================
// PLAY MANAGER
// ==========================================

~find_children = { arg main, node;
	var res = Set[];
	node.uname.debug("find_children: entering");
	if([\parnode, \seqnode].includes(node.kind) or: {node.subkind == \nodesampler} ) {
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
		recording: false,
		tempo: TempoClock.default.tempo,
		visual_metronome_enabled: true,

		myclock: TempoClock.default,

		expset_manager: ~exclusive_play_set.(),

		start_pos: 0,
		play_length: 16,
		record_length: 8,
		syncclap_dur: 4,
		use_metronome: false,
		keep_recording_session: false,
		get_clock: { arg self; self.myclock },
		archive_data: [\play_length, \record_length, \use_metronome],

		reset_state: { arg self;
			self.top_nodes = Dictionary.new;
			self.children_nodes = Set.new;
			self.solomuted_nodes = Set.new;
		},

		refresh: { arg self;
			self.changed(\head_state, \stop);
			self.changed(\pos);
			self.changed(\quant);
			self.changed(\visual_metronome);
			self.changed(\tempo, self.get_clock.tempo);
		},

		save_data: { arg self;
			var data = ~save_archive_data.(self, self.archive_data);
			data[\tempo] = self.get_tempo;
			data[\quant] = self.get_quant;
			data;
		},

		load_data: { arg self, data;
			~load_archive_data.(self, self.archive_data, data);
			self.set_tempo(data[\tempo]);
			self.set_quant(data[\quant]);
		},

		is_near_end: { arg self;
			self.get_rel_beat > (self.get_record_length - 1 - self.myclock.beatsPerBar)
		},

		get_rel_beat: { arg self;
			[self.get_clock.beats, self.start_pos, self.get_record_length,
				((self.get_clock.beats - self.start_pos) % self.get_record_length)
			].debug("beats, spos, reclen, relbeat");
			((self.get_clock.beats - self.start_pos) % self.get_record_length)
		},

		is_recording: { arg self;
			self.recording == true
		},

		set_recording: { arg self, val;
			self.recording = val;
			if(self.is_recording) {
				self.changed(\head_state, \prepare);
			} {
				if(self.is_playing) {
					self.changed(\head_state, \play);
				} {
					"play_manager: set_recording: stop_counter".debug;
					self.changed(\stop_counter);
					self.changed(\head_state, \stop);
				}
			};
		},

		get_record_length: { arg self;
			self.record_length;
		},

		get_record_length_in_seconds: { arg self;
			self.get_record_length / self.myclock.tempo;
		},

		set_record_length: { arg self, val;
			val.debug("play_manager.set_record_length");
			self.record_length = val;
			self.changed(\pos);
		},

		set_bpm_tempo: { arg self, val;
			self.myclock.tempo = val/60;
			self.tempo = val/60;
			self.changed(\tempo);
		},

		set_tempo: { arg self, val;
			self.myclock.tempo = val;
			self.tempo = val;
			self.changed(\tempo);
		},

		get_bpm_tempo: { arg self;
			self.myclock.tempo * 60;
		},

		get_tempo: { arg self;
			self.myclock.tempo;
		},

		get_quant: { arg self;
			EventPatternProxy.defaultQuant;
		},

		set_quant: { arg self, val;
			EventPatternProxy.defaultQuant = val;
			self.changed(\quant);
		},

		start_new_session: { arg self;
			if(self.is_playing || self.keep_recording_session) {
				"start_new_session: already playing".debug;
				self.keep_recording_session = false;
				self.changed(\visual_metronome);
			} {
				"start_new_session: new session!!".debug;
				if(self.myclock != TempoClock.default) {
					//self.myclock.stop // TODO: make sure there is no ressource leak
					//FIXME: this stop cause bug, why ?
				};
				self.myclock = TempoClock.new(self.tempo);
				self.myclock.permanent = true;
				self.myclock.beats.debug("start_new_session: new clock beats");
				self.myclock.hash.debug("hash");
				self.start_pos = 0;
				self.changed(\visual_metronome);
				//self.start_visual_metronome;
			}
		},

		start_metronome: { arg self, clock, dur;
			// called in midi.sc: preclap
			var oldclock;
			//oldclock = self.myclock;
			//self.myclock = clock;
			self.start_pos = 0;
			self.set_record_length(dur);
			Task {
				//self.changed(\visual_metronome);
				//self.start_visual_metronome;
				self.start_audio_metronome(clock, dur);
				dur.wait;
				//self.changed(\visual_metronome)
				//self.stop_visual_metronome;
				//self.myclock = oldclock;
			}.play(clock, quant:1);
		},

		start_unlimited_metronome: { arg self, clock;
			Task {
				self.start_audio_metronome(clock, inf);
			}.play(clock, quant:1);
		},

		start_audio_metronome: { arg self, clock, dur;
			self.audio_metronome = Pbind(\instrument, \metronome,
				\freq, 440,
				\sustain, 0.1,
				\dur, Pn(1,dur)
			).play(clock, quant:1);
		},

		stop_audio_metronome: { arg self;
			self.audio_metronome.stop;
		},

		enable_visual_metrome: { arg self, val=true;
			self.visual_metronome_enabled = val;
			self.changed(\visual_metronome);
		},

		start_visual_metronome: { arg self;
			self.get_rel_beat.debug("pm: start_visual_metronome");
			self.visual_metronome_enabled = true;
			self.changed(\visual_metronome);
		},

		stop_visual_metronome: { arg self;
			self.get_rel_beat.debug("pm: stop_visual_metronome");
			self.visual_metronome_enabled = false;
			self.changed(\visual_metronome);
		},

		is_playing: { arg self;
			self.top_nodes.size > 0
		},

		node_is_playing: { arg self, node;
			self.top_nodes.keys.includes(node.uname) || self.children_nodes.includes(node.uname)
		},

		play_node: { arg self, nodename;
			var esp, sc, children, quant;
			nodename.debug("pm: play_node");
			[self.top_nodes, self.children_nodes].debug("pm: state");
			~notNildo.(main.get_node(nodename), { arg node;
				self.start_new_session;
				children = ~find_children.(main, node);
				if( self.top_nodes.keys.includes(nodename) ) {

					/////==== already playing: unmuting children ====/////

					nodename.debug("pm: play_node: already playing, unmuting children");
					children.do { arg child;
						child.mute(false);
					};
				} {
					if( self.children_nodes.includes(nodename) ) {

						/////==== already playing as a children: unmuting ====/////

						nodename.debug("pm: play_node: unmute");
						node.mute(false);
					} {

						/////==== not playing: play it ====/////

						nodename.debug("pm: play_node: play!");

						// stoping nodes in exclusive group
						debug("6666666666666666666666666666666666666666 play_manager.play_node: expset 66666666666666");

						self.expset_manager.get_nodes_to_stop(nodename, self.children_nodes.union(self.top_nodes.keys))
								.debug("66 all nodes to stop");
						[nodename, self.children_nodes.union(self.top_nodes.keys)].debug("66 nodename, playing nodes");
						self.expset_manager.set_dict.debug("66 set_dict");

						self.expset_manager.get_nodes_to_stop(nodename, self.children_nodes.union(self.top_nodes.keys)).do { arg node_to_stop;
							node_to_stop.debug("66 node_to_stop");
							self.stop_node(node_to_stop, true);
						};

						// unmuting

						children.do { arg child;
							child.mute(false);
						};
						node.mute(false);

						// playing

						self.get_rel_beat.debug("pm: play node");

						node.node.source = node.vpattern_loop;
						quant = if(self.is_playing) { self.get_quant } { 1 };
						node.node.play(self.get_clock,quant:quant);
						node.set_playing_state(\play);
						
						// registering

						children.collect(_.uname).debug("pm: play_node: children");

						esp = node.node.player;
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
							if(self.is_playing.debug("isplaying").not) {
								self.changed(\head_state, \stop);
							};
							sc.remove;
							[self.top_nodes, self.children_nodes].debug("pm: end state");
						});
					}
				}
			}); 
			if(self.is_playing.debug("is_playing---------------------------------")) { self.changed(\head_state, \play) };
			[self.top_nodes, self.children_nodes].debug("pm: end state");
		},

		stop_node: { arg self, nodename, use_quant = false;
			var children;
			var stop_action;
			if(use_quant.isNil) { use_quant = false };
			nodename.debug("pm: stop_node");
			[self.top_nodes, self.children_nodes].debug("pm: state");
			~notNildo.(main.get_node(nodename), { arg node;
				if( self.top_nodes.keys.includes(nodename) ) {
					nodename.debug("pm: stop_node: stoping!");
					stop_action = { self.top_nodes[nodename].esp.stop };
				} {
					if( self.children_nodes.includes(nodename) ) {
						nodename.debug("pm: stop_node: mute");
						stop_action = { node.mute(true) };
					} {
						nodename.debug("pm: stop_node: not playing, individually stopping children");
						children = ~find_children.(main, node);
						stop_action = {
							children.do { arg child; 
								child.node.stop;
							};
						}
					}
				};
				if(use_quant) {
					"stop using quant".debug;
					self.get_clock.play(stop_action, self.get_quant);
				} {
					stop_action.();
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

		is_in_solo_mode: { arg self;
			self.solomuted_nodes.size != 0
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

~make_song_manager = { arg main;

	(
		current_song: main.node_manager.make_groupplayer(\song1, \seq),
		name_tree: [\s, \part, \sect, \var],
		type_tree: [\seq, \par, \seq, \par],

		change_part: { arg self, num; 
			self.get_path([num,nil,nil], true);
		},

		change_section: { arg self, num;
			self.get_path([nil,num,nil], true);
		},

		change_variant: { arg self, num;
			self.get_path([nil,nil,num], true);
		},

		get_current_group: { arg self, num;
			self.print_tree;
			self.get_path([nil,nil,nil]);
		},

		unvoid_child: { arg self, group, childname, index, pathlevel, prefix;
			var newgroup;
			childname.debug("unvoid_child");
			if(childname == \voidplayer) {
				newgroup = main.node_manager.make_groupplayer((prefix ++ index).asSymbol, self.type_tree[pathlevel]);
				newgroup.uname.debug("---------- unvoid_child: CREATING new group uname");
				group.set_children_name(index-1, newgroup.uname);
				[group.uname, group.children, index].debug("unvoid_child: group, children, index");
				newgroup;
			} {
				main.get_node(childname);
			};
		},

		get_child: { arg self, group, index, pathlevel, prefix, select=false;
			var newgroup;
			if(index.notNil) {
				if(index == 0) {
					group;
				} {
					if(select) {
						group.set_selected_child_index(index-1);
					};
					self.unvoid_child(group, group.get_childname_by_index(index-1), index, pathlevel, prefix);
				}
			} {
				self.unvoid_child(group, group.get_selected_childname, group.selected_child_index+1, pathlevel, prefix);
			}
		},
		
		get_path: { arg self, path, select=false;
			var child, current_group, index, newgroup, old_group;
			var prefix;
			path.debug("song_manager: get_path: path");
			current_group = self.current_song;
			prefix = "s1";
			if(path.size < 4) {
				block { arg break;
					path.do { arg index, pathlevel;
						if(index == 0) {
							break.value;
						} {
							pathlevel = pathlevel + 1;
							prefix = prefix ++ "_" ++ self.name_tree[pathlevel];
							old_group = current_group;
							current_group = self.get_child(current_group, index, pathlevel, prefix, select);
							prefix = prefix ++ (old_group.selected_child_index + 1);
						}
					};
				};
				current_group.uname.debug("song_manager: get_path: returned group");
				current_group;
			} {
				"ERROR: path array too long".debug;
			}
		},

		get_selected_section: { arg self;
			self.get_path([nil,nil])	
		},

		get_selected_part: { arg self;
			self.get_path([nil])	
		},

		get_current_path: { arg self;
			var group, path, childname;
			path = List.new;
			group = self.current_song;
			3.do {
				childname = group.get_selected_childname;
				childname.debug("get_current_path:selected_childname");
				if(childname == \voidplayer) {
					path.add(childname)
				} {
					group = main.get_node(childname);
					path.add(childname);
				};
			};
			path;
		},

		print_tree: { arg self;
			"========== print_tree =========================".postln;
			self.print_tree_helper;
		},

		print_tree_helper: { arg self, root, level=1;
			root = root ?? self.current_song;
			level.do{post("   ")};
			"% (%)".format(root.uname, root.identityHash).postln;
			level = level+1;
			root.children.do { arg childname;
				if(childname == \voidplayer) {
					level.do{post("   ")};
					childname.postln; 
				} {
					self.print_tree_helper(main.get_node(childname), level);
				}
			}
		}
	)

};
