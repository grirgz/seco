~common_bindings = (
	playing: [
		["play_selected", \kb, 0, \f5],
		["stop_selected", \kb, 0, \f6],
		["panic", \kb, 0, \f8],
	],
	grid_display: [
		["decrease_gridstep_x", \kb, \ctrl, \left],
		["increase_gridstep_x", \kb, \ctrl, \right],
		["decrease_gridstep_y", \kb, \ctrl, \down],
		["increase_gridstep_y", \kb, \ctrl, \up],
		["decrease_gridlen", \kb, 0, \left],
		["increase_gridlen", \kb, 0, \right],
	],
	windowing: [
		["close_window", \kb, 0, \escape],
	]
);

~bindings = (
	editplayer: [
		["select_cell",							\kb, 0, \kbnumline],
		["edit_value", 							\kb, 0, \enter],
		["edit_value_mode.insert_number",		\kb, 0, \kbnumpad],
		["edit_value_mode.insert_point",		\kb, 0, \point],
		["edit_value_mode.cancel",				\kb, 0, \escape],
		["edit_value_mode.ok",					\kb, 0, \enter],

		["solo_selected", \kb, 0, \f7],
		["unsolo_selected", \kb, \ctrl, \f7],
		["unsolo_selected", \kb, \ctrl, \f7],
		["add_effect",							\kb, \ctrl, \f1],
		["increase_midi_knob_offset",			\kb, 0, \down],
		["decrease_midi_knob_offset",			\kb, 0, \up],
		["toggle_cc_recording",					\kb, \altshift, "r"],
		["change_param_kind.recordbus",			\kb, \altshift, "u"],
		["start_midi_liveplayer",			\kb, \altshift, "e"],
		["param_set_pkey_mode",			\kb, \alt, "k"],
		["param_unset_pkey_mode",			\kb, \altshift, "k"],

	],
	parlive: [
		["select_header",							\kb, \alt, \kbnumline],
		["show_panel.editplayer",							\kb, 0, \f12],
		["create_new_livenode", \kb, \alt, "c"],
	],
	classinstr:
		~common_bindings.playing ++
		~common_bindings.windowing ++
		[

	],
	timeline: [
		["play_timeline", \kb, 0, \f5],
		["stop_timeline", \kb, 0, \f6],
		["close_timeline", \kb, 0, \escape],
	],
	group_tracks: [
		["close_window", \kb, 0, \escape],
	],
	player_tracks: ~common_bindings.playing ++ [
		["close_window", \kb, 0, \escape],
		["decrease_gridstep_x", \kb, \ctrl, \left],
		["increase_gridstep_x", \kb, \ctrl, \right],
		["decrease_gridstep_y", \kb, \ctrl, \down],
		["increase_gridstep_y", \kb, \ctrl, \up],
		["decrease_gridlen", \kb, 0, \left],
		["increase_gridlen", \kb, 0, \right],

	],
	line_tracks: 
		~common_bindings.playing ++ 
		~common_bindings.windowing ++
		[
			["decrease_gridstep_x", \kb, \ctrl, "h"],
			["increase_gridstep_x", \kb, \ctrl, "l"],
			["decrease_gridstep_y", \kb, \ctrl, "k"],
			["increase_gridstep_y", \kb, \ctrl, "j"],
			["decrease_gridlen", \kb, 0, "h"],
			["increase_gridlen", \kb, 0, "l"],
		],
	effects: ~common_bindings.playing ++ [
		["close_window", \kb, 0, \escape],
		["load_effect", \kb, 0, \f1],
		["select_param", \kb, 0, \kbpad8x4_flat],
		["select_player", \kb, 0, \kbnumpad],
		["edit_modulator", \kb, \alt, "m"],
		["change_param_kind", \kb, \altshift, "s"],
	],
	modulator: ~common_bindings.playing ++ [
		["close_window", \kb, 0, \escape],
		["load_modulator", \kb, 0, \f1],
		["select_param", \kb, 0, \kbpad8x4_flat],
		["select_player", \kb, 0, \kbnumpad],
		["edit_modulator", \kb, \alt, "m"],
		["change_param_kind", \kb, \altshift, "s"],
		["change_modulated_param_kind", \kb, \shift, "s"],
		["change_mod_kind", \kb, \altshift, "d"],

	],
	side: [
		["add_modenv", \kb, \ctrlalt, "o"], // debug

		["edit_external_player", \kb, \alt, "e"],
		["open_timeline", \kb, \ctrlalt, "l"],
		["edit_group_tracks", \kb, \alt, "g"],
		["edit_player_tracks", \kb, \alt, "p"],
		["edit_line_tracks", \kb, \alt, "q"],
		["edit_modulator", \kb, \alt, "m"],
		["edit_effects", \kb, \alt, "f"],

		["decrease_select_offset", \midi, 0, \begin],
		["increase_select_offset", \midi, 0, \end],
		["increase_samplekit_part", \midi, 0, \right],
		["decrease_samplekit_part", \midi, 0, \left],

		["midi.play_group", \midi, \hold, \play],
		["midi.stop_group", \midi, \hold, \stop],
		["play_group", \kb, \ctrl, \f5],
		["stop_group", \kb, \ctrl, \f6],
		["play_selected", \kb, 0, \f5],
		["midi.play_selected", \midi, 0, \play],
		["stop_selected", \kb, 0, \f6],
		["stop_selected_quant", \kb, \ctrlshift, \f6],
		["midi.stop_selected", \midi, 0, \stop],
		//["mute_selected", \kb, 0, \f7],
		//["unmute_selected", \kb, \ctrl, \f7],
		["solo_selected", \kb, 0, \f7],
		["unsolo_selected", \kb, \ctrl, \f7],
		["toggle_solo_selected", \midi, 0, \pause],
		["panic", \kb, 0, \f8],
		["edit_tempo", \kb, \ctrlalt, "e"], // t open terminal
		["edit_quant", \kb, \ctrlalt, "q"],
		["edit_barrecord", \kb, \ctrlalt, "b"],
		["edit_selected_param", \kb, 0, \enter],

		["edit_wrapper", \kb, \alt, "w"],

		["copy_node", \kb, \ctrl, "c"],
		["copy_node_children", \kb, \ctrlalt, "c"],
		["cut_node", \kb, \ctrl, "x"],
		["paste_node", \kb, \ctrl, "v"],
		//["copy_group", \kb, \ctrl, \kbnumpad],
		["remove_node", \kb, \ctrlshift, \f4],
		["remove_and_free_node", \kb, \ctrlaltshift, \f4],

		["load_node_from_lib", \kb, 0, \f1],
		["create_default_node", \kb, \alt, "c"],
		["reload_player", \kb, \altshift, "r"],

		["add_effect", \kb, \ctrlshift, \f2],

		["load_colpreset", \kb, \ctrl, \f1],
		["save_colpreset", \kb, \ctrl, \f2],

		["select_param", \kb, 0, \kbpad8x4_flat],
		["pad_select_param", \midi, 0, \midipads],

		["select_param_cell", \kb, \alt, \kbnumline],
		["matrix_select_param_cell", \kb, 0, \kbpad8x4_flat],

		["select_player", \kb, 0, \kbnumpad],
		["pad_select_player", \midi, 0, \midipads],
		["matrix_select_player", \kb, \alt, \kbnumline],


		["select_variant", \kb, \ctrl, \kbnumpad],
		["select_section", \kb, \alt, \kbnumpad],
		["select_part", \kb, \ctrlalt, \kbnumpad],

		["forward_in_record_history", \kb, 0, \right],
		["backward_in_record_history", \kb, 0, \left],

		["set_edit_mode.matrix", \kb, \ctrl,  \f10],
		["set_edit_mode.params", \kb, \ctrl,  \f9],

		["set_global_mode.param", \kb, 0, \f9],
		["set_global_mode.group", \kb, 0, \f10],
		["set_global_mode.liveplay", \kb, 0, \f11],
		["set_global_mode.mixer", \kb, 0, \f12],
		["midi.set_global_mode.param", \midi, 0, \b1],
		["midi.set_global_mode.group", \midi, 0, \b2],
		["midi.set_global_mode.liveplay", \midi, 0, \b3],
		["midi.set_global_mode.mixer", \midi, 0, \b4],

		["set_notequant", \kb, \ctrlaltshift, "q"],
		["add_cell_bar", \kb, 0, \npplus],
		["remove_cell_bar", \kb, \ctrl, \npplus],


		//["toggle_player_recording", \midi, 0, \record],
		["toggle_player_recording", \midi, 0, \record],
		["toggle_param_recording", \midi, \hold, \record],
		["toggle_metronome", \kb, \ctrlalt, "m"],

		//["change_param_kind.seq",			\kb, \altshift, "q"],
		//["change_param_kind.scalar",			\kb, \altshift, "s"],
		//["change_param_kind.recordbus",			\kb, \altshift, "d"],
		//["change_param_kind.bus",			\kb, \altshift, "f"],
		//["change_param_kind.seg",			\kb, \altshift, "g"],
		//["change_param_kind.pkey",			\kb, \altshift, "h"],

		["change_param_kind",			\kb, \altshift, "s"],
		["change_player_mode",			\kb, \altshift, "q"],
		//["change_player_mode.stepline",			\kb, \altshift, "q"],
		//["change_player_mode.noteline",			\kb, \altshift, "s"],
		//["change_player_mode.sampleline",			\kb, \altshift, "d"],
	]
);
