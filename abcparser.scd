
(
~abcparser = Environment.make({ 
	var cur = currentEnvironment;

	~notedef = Dictionary.newFrom(("CDEFGABcdefgab".as(Array).collect { arg ch, idx; [ch, idx-7] }).flat);
	~notedef[$_] = \r;
	~digitdef = Dictionary.newFrom(("123456789".as(Array).collect { arg ch, idx; [ch, idx+1] }).flat);

	~is_terminal = { arg char;
		~notedef[char].notNil or: { [ $[, $] ].includes(char) }
	};

	~strip_spaces = { arg str;
		str.replace(" ", "")
	};

	~split_string = { arg str;
		var res = List.new;
		var accu = "";
		str.do { arg char;
			if( ~is_terminal.(char) ) {
				if(accu != "") {
					res.add(accu);
				};
				accu = "" ++ char;
			} {
				accu = accu ++ char;
			}
		};
		res.add(accu);
		res;
	};

	~extract_chords = { arg token_list;
		var res = List.new;
		var accu = List.new;
		var chord = false;
		token_list.do { arg token;
			if( chord ) {
				if( token == "]") {
					chord = false;
					res.add(accu);
					accu = List.new;
				} {
					accu.add(token)
				};
			} {
				if( token == "[") {
					chord = true;
				} {
					res.add(token)
				};
			};
		};
		//res.add(token);
		res;
	};

	~parse_note = { arg token;
		var pitch;
		var len = 1;
		var char;
		var divide = false;
		token = token.as(List).reverse;
		pitch = token.pop;
		pitch = ~notedef[pitch];
		while({ token.isEmpty.not }) {
			char = token.pop;	
			case
				{ char == $' } {
					pitch = pitch + 14;
				}
				{ char == $, } {
					pitch = pitch - 14;
				}
				{ char == $/ } {
					len = 1/2;
					divide = true;
				}
				{ ~digitdef[char].notNil } {
					if(divide) {
						len = 1 / ~digitdef[char];
					} {
						len = ~digitdef[char];
					}
				}
			
		};
		[pitch, len]
	};

	~parse_sentence = { arg token_list;
		var res_pitch = List.new, res_len = List.new;
		var pitch, len;
		token_list.do { arg token;
			if(token.isString) {
				#pitch, len = ~parse_note.(token);
				res_pitch.add(pitch);
				res_len.add(len);
			} {
				#pitch, len = ~parse_sentence.(token);
				//len = len.maxItem; // workaround to the multichannel expand bug
				res_pitch.add(pitch);
				res_len.add(len);
			}
		};
		[res_pitch, res_len]
	};
			
	~parseabc = { arg str;
		var res;
		res = ~strip_spaces.(str);
		res = ~split_string.(res);
		res = ~extract_chords.(res);
		res = ~parse_sentence.(res);
		res;
	};

	~add_legato = { arg dur;
		var legato = List.new;
		var maxi;
		dur = dur.collect { arg du;
			if(du.isSequenceableCollection) {
				maxi = du.maxItem;
				legato.add( du.collect { arg leg;
					leg / maxi
				});
				maxi;
			} {
				legato.add(1);
				du;
			}
		};
		[dur, legato]
	};

	~abc_to_pbind = { arg str, repeat=inf;
		var res, dur, legato;
		res = ~parseabc.(str);
		#dur, legato = ~add_legato.(res[1]);
		Pbind(
			\degree, Pseq(res[0],repeat),
			\legato, Pkey(\legato) * Pseq(legato,repeat),
			\dur, Pseq(dur,repeat)
		)
	};
	~pbind = { arg str, repeat=inf; cur.use{ ~abc_to_pbind.(str, repeat) } };
}).as(Event);

~abcpbind = { arg str, repeat=inf; ~abcparser[\pbind].(str, repeat) };
)
