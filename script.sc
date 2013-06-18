
(

~class_custom_tracks_controller = (
	new: { arg self, main, player, display, tracks;
		self = self.deepCopy;

		self.get_main = { main };
		self.get_player = {player};
		self.display = display;
		self.custom_tracks = {tracks};
		//self.custom_tracks.debug("bordel");
		//self.display = ~class_track_display.new;

	
		self;
	},

	get_tracks: { arg self;
		var res = List.new;
		debug("class_custom_tracks_controller.get_tracks");
		res = self.custom_tracks.collect { arg track;
			track.uname.debug("bordel");
			~make_recordline_track_controller.(track, self.display);
		};
		self.tracks = res;
		res;
	},

	make_bindings: { arg self;
	
		self.get_main.commands.parse_action_bindings(\player_tracks, 
			self.get_main.panels.side.get_shared_bindings ++ 
			self.display.get_bindings ++ 
			self.get_main.panels.side.get_windows_bindings ++ [
			[\close_window, {
				self.window.close;
			
			}],

			[\select_scoresheet, 8, { arg i;
				self.get_player.get_scoreset.select_sheet(i);
				self.tracks[0].changed(\label);
			}],

			[\save_scoresheet, {
				var player = self.get_player;
				~class_scoresheet_chooser.new(self.get_main, player, { arg data, ad, idx;
					var sheet;
					var scoreset = player.get_scoreset;
					var ns;
					ns = scoreset.get_notescore;
					scoreset.set_sheet(idx, ns);
					self.get_player.get_scoreset.select_sheet(idx);
					self.tracks[0].changed(\label);
				});
			}],

			[\play_selected, {
				self.get_player.play_node;
			}], 

			[\stop_selected, {
				self.get_player.stop_node;
			}],

			[\panic, {
				self.get_main.panic;
			}],
		]);
	},

	make_gui: { arg self;
		self.make_bindings;
		self.window = Window.new("Custom Tracks", Rect(300,300,900,300));
		self.window.view.keyDownAction = self.get_main.commands.get_kb_responder(\player_tracks, self);
		self.multitrack_view = ~class_multitrack_view.new(self);
		self.window.view.layout = self.multitrack_view.layout;

		self.window.front;
	},
);

~tracks = { arg player, tracks;
	var main = Mdef.main;
	var display = main.panels.side.track_display;
	tracks = tracks.collect { arg track;
		if(track.isSymbolWS or: { track.isString }) {
			main.get_node(track);
		} {
			track;
		}
	};
	~class_custom_tracks_controller.new(main, player, display, tracks)

};
)

"plop".isString

(
~mynode = Mdef.node("osc1_l1002");
~ctrl = ~tracks.(~mynode, [
	~mynode,
	"stereosampler_l1003"
]);
~ctrl.make_gui;
)
