s.quit

(
SynthDef(\lfo1, { arg out, freq;
	var sig = SinOsc.kr(freq);
	Out.kr(out, sig);
}).add;

SynthDef(\adsr1, { arg out, attack, gate=1, doneAction=0;
	var sig = EnvGen.kr(Env.adsr(attack,0.1,1,0.1), gate, doneAction:doneAction);
	Out.kr(out, sig);
}).add;

SynthDef(\osc1, { arg out, gate=1, freq, amp=0.1, ffreq=200, rq=0.1, attack=0.1, doneAction=2;
	var sig = LFSaw.ar(freq);
	var env = EnvGen.kr(Env.adsr(attack,0.1,1,0.1), gate, doneAction:doneAction);
	sig = RLPF.ar(sig, ffreq, rq);
	//sig = sig + SinOsc.ar(ffreq);
	//ffreq.poll;
	rq.poll;
	sig = sig * env;
	sig = sig ! 2;
	sig = sig * amp;
	Out.ar(out, sig);
}).add;

SynthDef(\comb1, { arg in, out, attack, maxdelaytime=0.4, delaytime=0.4, decaytime=2, gate=1, doneAction=2;
	//var sig = EnvGen.kr(Env.adsr(attack,0.1,1,0.1), gate, doneAction:doneAction);
	var sig;
	sig = In.ar(in, 2);
	sig = CombL.ar(sig, maxdelaytime, delaytime, decaytime);
	Out.ar(out, sig);
}).add;

)

(
	~make_modmixer = { arg name, rate=\kr, spec, kind=\normal;
		var sdname = (\modulation_mixer_ ++ name).asSymbol;
		sdname.debug("make_modmixer");

		SynthDef(sdname, { arg carrier, out, in1=0, range1=0, in2=0, range2=0, in3=0, range3=0;
			var sig1, sig2, sig3;
			var sig;
			var inrate;
			"bla".debug;
			if(kind == \normal) {
				inrate = { arg ... args; In.performList(rate, args) };
			} {
				//inrate = { arg ... args; InFeedback.performList(rate, args) };
				inrate = { arg ... args; In.performList(rate, args) };
			};
			"blai".debug;
			sig1 = inrate.(in1);
			"blaii".debug;
			sig2 = inrate.(in2);
			sig3 = inrate.(in3);
			"blauii".debug;
			
			sig = [
				spec.unmap(carrier),
				sig1 * range1,
				sig2 * range2,
				sig3 * range3,
			].sum;
			sig = spec.map(sig);
			//sig.poll;
			Out.perform(rate, out, sig);

		}).add;
		sdname;
	};

	~class_effect_manager = (
		effect_list: List.newClear(5),
		new: { arg self, player;
			self = self.deepCopy;
			self.get_player = { arg self; player };

			self;	
		},

		set_effect: { arg self, idx, effect_node;
			self.effect_list[idx] = effect_node;
		},

		get_effect: { arg self, idx;
			self.effect_list[idx]
		},

		swap_effect: { arg self, idx_source, idx_dest;
			var tmp = self.effect_list.removeAt(idx_source);
			self.effect_list.insert(idx_dest, tmp);
		},

		vpattern: { arg self;
			
		
		}

	);

	~mod = (
		name: \plop,
		uname: \plop,
		modulators: Dictionary.new,
		modulator_target: Dictionary.new,
		mod_kind: \note,

		new: { arg self;
			self = self.deepCopy;
		
			self.effects = ~class_effect_manager.new(self);
		
			self;
		},

		get_modulators: { arg self;
			self.modulators;
		},
		connect_modulator: { arg self, target, index, range, modname;
			if(self.modulator_target[target].isNil) {
				self.modulator_target[target] = Dictionary.new;
			};
			self.modulator_target[target][index] = (range:range, mod:modname);
		},
		add_modulator: { arg self, name, mod;
			self.modulators[name] = mod;
		},

		get_dur: { arg self;
			Pbind(
				\dur, 1,
			)
		},

		get_val: { arg self, name;
			 var dic = (
				ffreq: 400,
				rq: 0.6,
			 );
			 dic[name];
		},

		sourcepat: { arg self, pat;
			Pbind(
				\instrument, \osc1,
				//\freq, Pseq([200,400],inf),
				\freq, Pseq([200,400],2),
				//\legato, 1.5,
				\legato, 0.3,
				\ffreq, Pfunc{ arg ev;
					debug("main ffreq bus");
					ev[\ppatch].get_mod_bus(self.uname, \ffreq).asMap;
				},
				\rq, Pfunc{ arg ev;
					debug("main rq bus");
					ev[\ppatch].get_mod_bus(self.uname, \rq).asMap;
				},
			) <> pat;
		
		},

		vpattern: { arg self;
			var free_defer_time = 3; // FIXME: hardcoded
			var out_bus = 0;
			Pspawner({ arg spawner;
				var str;
				var main_note_pat;
				var mixer_list = List.new;
				var note_modulator_list = List.new;
				var pattern_modulator_list = List.new;
				var effect_list = List.new;
				var effect_pat_list = List.new;
				var effect_inbus_list = List.new;
				var effect_outbus_list = List.new;
				var synth_out_bus;
				var ppatch;
				var rate = \kr;
				var make_note_out_bus;
				var make_modulator_pattern, make_mixer_pattern;
				var walk_modulators;
				"vpattern".debug;

				ppatch = (
					note_bus: Dictionary.new,
					note_group: Dictionary.new,
					global_bus: Dictionary.new,
					global_group: Dictionary.new,
					noteon: List.new,
					get_mod_bus: { arg self, prefix, name;
						self.note_bus["mixer_%_%".format(prefix, name).asSymbol];
					},

				);

				///////// building effects patterns

				effect_list = self.effects.effect_list.reject({ arg ef; ef.isNil or:{ ef.disabled == true } });

				if(effect_list.size == 0) {
					synth_out_bus = out_bus;
				} {
					ppatch.global_bus[\synth] = Bus.audio(s, 2);
					synth_out_bus = ppatch.global_bus[\synth];
					effect_inbus_list.add(ppatch.global_bus[\synth]);
					(effect_list.size - 1).do { arg idx;
						var bus = Bus.audio(s, 2);
						ppatch.global_bus["effect_%".format(idx).asSymbol] = bus;
						effect_outbus_list.add(bus);
						effect_inbus_list.add(bus);
					};
					effect_outbus_list.add(out_bus);
				};

				effect_list.do { arg effect, idx;
					effect_pat_list.add( effect.vpattern <> Pbind(
						\ppatch, ppatch,
						\group, Pfunc{ arg ev; ev[\ppatch].global_group[\effects] },
						\addAction, \addToTail,
						\in, effect_inbus_list[idx],
						\out, effect_outbus_list[idx],
					))
				};

				///////// functions


				make_note_out_bus = { arg key, group_name;
					Pfunc{ arg ev;
						//var brate = if(rate == \kr) { \control } { \audio };
						var brate = \control;
						var bus = Bus.alloc(brate, s, 1);
						var pp = ev[\ppatch];
						//var pp = ppatch;
						var group;
						pp.note_bus[key] = bus;
						group = Group.new(pp.global_group[group_name]);
						ev[\group] = group;
						pp.note_group[key] = group;
						bus;
					}
				};

				make_modulator_pattern = { arg player, mod, key;
					var modpat;
					var out_bus_name;
					var brate = if(rate == \kr) { \control } { \audio };
					// key is modulator name
					out_bus_name = "mod_%_%".format(player.uname, mod.uname).asSymbol;

					if(mod.mod_kind == \pattern) {
						ppatch.global_bus[out_bus_name] = Bus.alloc(brate, s, 1);
						modpat = mod.sourcepat(
							Pbind(
								\ppatch, ppatch,
								\group, Pfunc{ arg ev; ppatch.global_group[\modulator] },
								\out, Pfunc{ arg ev; ppatch.global_bus[out_bus_name] }
							) <> mod.get_dur
						);
					} {
						modpat = mod.sourcepat(
							Pbind(
								\ppatch, ppatch,
								\out, make_note_out_bus.(out_bus_name, \modulator)
							) <> player.get_dur
						);
					};
					modpat;

				};

				make_mixer_pattern = { arg player, key, mods, kind=\normal;
					// key is pattern key name which is modulated
					var mixer;
					var mixer_synthdef_name;
					var mixerarglist = List.new;
					var mixer_group_name;
					var out_bus_name;

					if(kind == \normal) {
						mixer_group_name = \mixer;
					} {
						mixer_group_name = \fbmixer;
					};
					mixer_synthdef_name = ~make_modmixer.(key, rate, key.asSpec, kind);
					out_bus_name = "mixer_%_%".format(player.uname, key).asSymbol;

					mixerarglist = List[
						\instrument, mixer_synthdef_name,
						\ppatch, ppatch,
						\carrier, player.get_val(key),
						\out, make_note_out_bus.(out_bus_name, mixer_group_name),
					];

					mods.keysValuesDo { arg slotidx, modstruct, idx;
						var in_bus_name = "mod_%_%".format(player.name, player.modulators[modstruct.mod].uname).asSymbol;
						idx = idx + 1;
						mixerarglist = (mixerarglist ++ [
							(\in++idx).asSymbol, Pfunc{ arg ev;
								[in_bus_name, modstruct, idx].debug("mixer: in");
								if(player.modulators[modstruct.mod].mod_kind == \pattern) {
									ev[\ppatch].global_bus[in_bus_name];
								} {
									ev[\ppatch].note_bus[in_bus_name];
								}
							},
							(\range++idx).asSymbol, modstruct.range
						]).asList;
					};
					if(mods.size > 0) {
						mixer = Pbind(*mixerarglist) <> player.get_dur;
						mixer_list.add(mixer);
					};
				};

				///////// building modulators patterns

				walk_modulators = { arg player, kind=\feedback;
					player.modulators.keysValuesDo { arg key, mod;
						if(mod.mod_kind == \pattern) {
							pattern_modulator_list.add( make_modulator_pattern.(self, mod, key) )
						} {
							note_modulator_list.add( make_modulator_pattern.(self, mod, key) )
						};
						walk_modulators.( mod );
					};

					player.modulator_target.keysValuesDo { arg key, mods;
						make_mixer_pattern.( player, key, mods, kind )
					}
				};

				walk_modulators.( self, \normal );

				///////// building mixer patterns
				

				///////// building modulators patterns

				//self.modulators.keysValuesDo { arg key, mod;
				//	var modpat;
				//	var mkey = "%_%".format(self.name, key).asSymbol;
				//	var brate = if(rate == \kr) { \control } { \audio };

				//	mod.modulators.keysValuesDo { arg m_key, m_mod;
				//		var m_modpat;
				//		var m_mkey = "%_%_%".format(self.name, mod.name, m_key).asSymbol;
				//		var m_brate = if(rate == \kr) { \control } { \audio };

				//		if(m_mod.mod_kind == \pattern) {
				//			ppatch.global_bus[m_mkey] = Bus.alloc(m_brate, s, 1);
				//			m_modpat = m_mod.sourcepat(
				//				Pbind(
				//					\ppatch, ppatch,
				//					\group, Pfunc{ arg ev; ev[\ppatch].global_group[\modulator] },
				//					\out, Pfunc{ arg ev; ev[\ppatch].global_bus[m_mkey] }
				//				) <> mod.get_dur
				//			);
				//			pattern_modulator_list.add(m_modpat);
				//		} {
				//			m_modpat = mod.sourcepat(
				//				Pbind(
				//					\ppatch, ppatch,
				//					\out, Pfunc{ arg ev;
				//						var brate = if(rate == \kr) { \control } { \audio };
				//						var bus = Bus.alloc(brate, s, 1);
				//						var pp = ev[\ppatch];
				//						var group;
				//						[m_mkey].debug("mod: out");
				//						pp.note_bus[m_mkey] = bus;
				//						group = Group.new(pp.global_group[\modulator]);
				//						ev[\group] = group;
				//						pp.note_group[m_mkey] = group;
				//						bus;
				//					}
				//				) <> self.get_dur
				//			);
				//			note_modulator_list.add(m_modpat);

				//		};
				//	
				//	};

				//	if(mod.mod_kind == \pattern) {
				//		ppatch.global_bus[mkey] = Bus.alloc(brate, s, 1);
				//		modpat = mod.sourcepat(
				//			Pbind(
				//				\ppatch, ppatch,
				//				\group, Pfunc{ arg ev; ev[\ppatch].global_group[\modulator] },
				//				\out, Pfunc{ arg ev; ev[\ppatch].global_bus[mkey] }
				//			) <> self.get_dur
				//		);
				//		pattern_modulator_list.add(modpat);
				//	} {
				//		modpat = mod.sourcepat(
				//			Pbind(
				//				\ppatch, ppatch,
				//				\out, Pfunc{ arg ev;
				//					var brate = if(rate == \kr) { \control } { \audio };
				//					var bus = Bus.alloc(brate, s, 1);
				//					var pp = ev[\ppatch];
				//					var group;
				//					[mkey].debug("mod: out");
				//					pp.note_bus[mkey] = bus;
				//					group = Group.new(pp.global_group[\modulator]);
				//					ev[\group] = group;
				//					pp.note_group[mkey] = group;
				//					bus;
				//				}
				//			) <> self.get_dur
				//		);
				//		note_modulator_list.add(modpat);

				//	};
				//
				//};

				///////// building mixer patterns

				//self.modulator_target.keysValuesDo { arg key, mods;
				//	var mixer;
				//	var mixer_synthdef_name;
				//	var mixerarglist = List.new;
				//	mixer_synthdef_name = ~make_modmixer.(key, rate, key.asSpec);

				//	[rate, rate == \kr].debug("PUTOA");

				//	mixerarglist = List[
				//		\instrument, mixer_synthdef_name,
				//		\ppatch, ppatch,
				//		\carrier, self.get_val(key),
				//		\out, Pfunc{ arg ev;
				//			var brate = if(rate == \kr) { \control } { \audio };
				//			var bus = Bus.alloc(brate, s, 1);
				//			var pp = ev[\ppatch];
				//			var group;
				//			brate.debug("RATE");
				//			key.debug("mixer: out");
				//			pp.note_bus[key] = bus;
				//			group = Group.new(pp.global_group[\mixer]);
				//			ev[\group] = group;
				//			pp.note_group[key] = group;
				//			bus;
				//		}
				//	];

				//	mods.keysValuesDo { arg slotidx, modstruct, idx;
				//		var mkey = "%_%".format(self.name, modstruct.mod).asSymbol;
				//		idx = idx + 1;
				//		mixerarglist.add((\in++idx).asSymbol);
				//		mixerarglist.add(Pfunc{ arg ev;
				//			[mkey, idx].debug("mixer: in");
				//			if(self.modulators[modstruct.mod].mod_kind == \pattern) {
				//				ev[\ppatch].global_bus[mkey];
				//			} {
				//				ev[\ppatch].note_bus[mkey];
				//			}
				//		});

				//		mixerarglist.add((\range++idx).asSymbol);
				//		mixerarglist.add(modstruct.range);

				//	};
				//	if(mods.size > 0) {
				//		mixer = Pbind(*mixerarglist) <> self.get_dur;
				//		mixer_list.add(mixer);
				//	};
				//};

				///////// creating global groups

				ppatch.global_group[\modulator] = Group.new(s);
				ppatch.global_group[\mixer] = Group.after(ppatch.global_group[\modulator]);
				ppatch.global_group[\synth] = Group.after(ppatch.global_group[\mixer]);
				ppatch.global_group[\effects] = Group.after(ppatch.global_group[\synth]);

				///////// building main pattern

				main_note_pat = self.sourcepat(
					Pbind(
						\ppatch, ppatch,
						\doneAction, 14,
						\out, Pfunc { arg ev; synth_out_bus },
						\group, Pfunc { arg ev;
							var pp = ev[\ppatch];
							var group;
							var note_group;
							var note_bus;
							//var freq = ev[\freq].value(ev);
							debug("synth: group");
							group = Group.new(pp.global_group[\synth]);
							group.register;
							pp.note_group[self.name] = group;
							note_group = pp.note_group.copy;
							note_bus = pp.note_bus.copy;
							group.addDependant({ arg grp, status;
								[grp, status].debug("dependant");
								if(status == \n_end) {
									"freeing".debug;
									note_group.keysValuesDo { arg gname, gobj;
										gname.debug("free group");
										if(gname != self.name) {
											gobj.free
										}
									};
									note_bus.keysValuesDo { arg bname, bobj;
										bname.debug("free bus");
										bobj.free;
									};
								}
							});
							group;
						}
					) <> self.get_dur
				);
			

				"blai0".debug;
				pattern_modulator_list.do { arg pat;
					spawner.par(pat);
				};
				note_modulator_list.do { arg pat;
					spawner.par(pat);
				};
				"bla0".debug;
				mixer_list.do { arg pat;
					spawner.par(pat);
				};
				effect_pat_list.do { arg pat;
					spawner.par(pat);
				};
				//spawner.par(Ppar(note_modulator_list));
				//spawner.par(Ppar(mixer_list));

				"bla1".debug;
				str = CleanupStream(main_note_pat.asStream, {
					"cleanup".debug;
					spawner.suspendAll;
					{
						ppatch.global_group.keysValuesDo { arg gname, gobj;
							gname.debug("pattern group free");
							gobj.free;
						};
						ppatch.global_bus.keysValuesDo { arg bname, bobj;
							bname.debug("pattern bus free");
							bobj.free;
						};
					}.defer(free_defer_time); 
				});
				"bla2".debug;

				spawner.par(str);
				"bla3".debug;
			});
		}

	);
	~mod_lfo1 = ~mod.new;
	~mod_lfo1.uname = \bla;
	~mod_lfo1.mod_kind = \note;
	~mod_lfo1.sourcepat = { arg self, pat;
		Pbind(
			\instrument, \lfo1,
			//\freq, 1.5,
			\freq, Pfunc{ arg ev; ev[\ppatch].get_mod_bus(self.uname, \freq )},
		) <> pat;
	};
	~mod_lfo1.get_val = { arg self, name;
		 var dic = (
			ffreq: 4,
			rq: 0.6,
		 );
		 dic[name];
	};
	//~mod_lfo1.sourcepat = { arg self, pat;
	//	Pmono(\lfo1,
	//		//\freq, Pfunc{ arg ev; ev[\ppatch].get_mod_bus(self.uname, \freq )},
	//		\freq, 0.1,
	//	) <> pat;
	//};

	~mod_lfo2 = ~mod.new;
	~mod_lfo2.uname = \bla2;
	~mod_lfo2.mod_kind = \pattern;
	//~mod_lfo1.sourcepat = { arg self, pat;
	//	Pbind(
	//		\instrument, \lfo1,
	//		\freq, 1.5,
	//	) <> pat;
	//};
	~mod_lfo2.sourcepat = { arg self, pat;
		Pmono(\lfo1,
			//\freq, Pfunc{ arg ev; ev[\ppatch].get_mod_bus(self.uname, \freq )},
			\freq, 0.1,
		) <> pat;
	};

	~mod_lfo1.add_modulator(\lfo2, ~mod_lfo2);
	~mod_lfo1.connect_modulator(\freq, 0, 0.15, \lfo2);

	~effect_comb = ~mod.new;
	~effect_comb.vpattern = { arg self;
		Pmono(\comb1,
			\decaytime, 3,
		)
	};

	~player = ~mod.new;
	~player.add_modulator(\lfo1, ~mod_lfo1);
	~player.connect_modulator(\ffreq, 0, 0.15, \lfo1);
	~player.connect_modulator(\rq, 0, 0.04, \lfo1);

	~player.effects.set_effect(0, ~effect_comb);
	Pn(~player.vpattern, 3).trace.play;
	//~player.vpattern.play;


) 


(
 Ndef({
 	SinOsc.ar(200);

 }).play

)


(

{
var sig = SinOsc.ar(LFSaw.kr(0.5)*50+400) ! 2
}.play

)


a = Dictionary.new
a.size
a[\bla] = nil
