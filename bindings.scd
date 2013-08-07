
// FIXME: use the same format than mouse
~mouse_bindings = (
	note_track: [
		["toggle_note", 0, \left_click],
		["resize_note", \shift, \left_click],
		["create_note", \ctrl, \left_click],
		["remove_note", \ctrl, \right_click],
		["set_end", 0, \right_click],
		["set_start", \ctrl, \right_click],
	],
	timeline_mouse_down: [ 
		["create_block", \ctrl, \left_click],
		["create_group_block", \ctrlalt, \left_click],
		["set_start", 0, \right_click],
		["add_last_block", \shift, \left_click],
		["remove_block", \ctrl, \right_click],
	],
	timeline_on_node: [ 
	],
);


~old_mouse_bindings = (
	toggle_note: [0, \left_click],
	resize_note: [\shift, \left_click],
	create_note: [\ctrl, \left_click],
	remove_note: [\ctrl, \right_click],
	set_end: [0, \right_click],
	//set_start: [\ctrl, \right_click],

);


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
		["decrease_gridlen", \kb, 0, \right],
		["increase_gridlen", \kb, 0, \left],
	],
	windowing: [
		["close_window", \kb, 0, \escape],
	],
	panels: [
		["show_side_panel", \kb, \alt, "s"],
		["open_nodematrix_panel", \kb, \alt, "n"],
		["edit_modulator", \kb, \alt, "m"],
		["edit_effects", \kb, \alt, "f"],
		["edit_line_tracks", \kb, \alt, "q"],
		["edit_player_tracks", \kb, \alt, "p"],
		["edit_group_tracks", \kb, \alt, "g"],
		["edit_external_player", \kb, \alt, "e"],
	],
	side_shared: [

		["set_midi_binding_mode.player", \kb, \shift,  \f9],
		["set_midi_binding_mode.global", \kb, \shift,  \f10],

		["edit_master_volume", \kb, \alt, "v"],
		["edit_tempo", \kb, \ctrlalt, "e"], // t open terminal
		["edit_quant", \kb, \ctrlalt, "q"],
		["edit_barrecord", \kb, \ctrlalt, "b"],
		["edit_wrapper", \kb, \alt, "w"],
		["open_timeline", \kb, \alt, "l"],
		["toggle_metronome", \kb, \ctrlalt, "m"],
		["play_group", \kb, \ctrl, \f5],
		["stop_group", \kb, \ctrl, \f6],
		["assign_midi_knob", \kb, \ctrlalt, "K"],
		["assign_global_midi_knob", \kb, \ctrlalt, "k"],
		["forward_in_record_history", \kb, 0, \end],
		["backward_in_record_history", \kb, 0, \home],
		["edit_selected_param", \kb, 0, \npenter],
		["alternate_edit_selected_param", \kb, \ctrl, \npenter],
	
	],
);

~bindings = (
	simple_edit_number: [
		["close_window",			\kb, 0, \escape],
		["validate_value",			\kb, 0, \enter],
	],
	matrix_chooser: 
		~common_bindings.windowing ++
		~common_bindings.playing ++ 
		[
			["edit_name", \kb, \alt, "r"],
			["remove_selected", \kb, \alt, \delete],
			["select_cell", \kb, 0, \kbpad8x4_flat],
			["select_bank", \kb, \ctrl, \kbnumpad],
			["select_row", \kb, \alt, \kbnumline],
			["select_column", \kb, 0, \kbnumpad],
			["create_batch", \kb, \alt, "c"],
			//["select_variant, \kb, \ctrl, \kbnumpad"], // use select_bank instead
			["select_section", \kb, \alt, \kbnumpad],
			["select_part", \kb, \ctrlalt, \kbnumpad],

	],
	nodematrix: 
		//~common_bindings.playing ++
		~common_bindings.panels ++
		//~common_bindings.side_shared ++
		~common_bindings.windowing ++
		[

			["rename_cell",							\kb, \alt, "r"],
			["copy_cell",							\kb, \ctrl, "c"],
			["cut_cell",							\kb, \ctrl, "x"],
			["paste_cell",							\kb, \ctrl, "v"],
			["select_cell",							\kb, 0, \kbpad8x4_flat],
			["select_row",							\kb, \alt, \kbnumline],
			["select_column",						\kb, 0, \kbnumpad],
			//["midi.select_row",							\midi, \hold, \midipads],
			//["midi.select_column",						\midi, 0, \midipads],
			//["select_row",							\midi, \hold, \midipads],
			//["select_column",						\midi, 0, \midipads],
			["select_cell",							\kb, 0, \kbpad8x4_flat],
			["play_cell",							\kb, 0, \f5],
			["play_row",							\kb, \ctrl, \f5],
			["play_part",							\kb, \ctrlshift, \f5],
			["stop_cell",							\kb, 0, \f6],
			["stop_part",							\kb, \ctrlshift, \f6],
			["panic",								\kb, 0, \f8],

			["select_variant", \kb, \ctrl, \kbnumpad],
			["select_section", \kb, \alt, \kbnumpad],
			//["select_part", \kb, \ctrlalt, \kbnumpad],
	],
	classinstr:
		~common_bindings.playing ++
		~common_bindings.panels ++
		~common_bindings.side_shared ++
		~common_bindings.windowing ++
		[
			["change_param_kind", \kb, \altshift, "s"],

	],

	/////////// param views

	array_view: 
		~common_bindings.windowing ++
		[
			["select_sheet", \kb, \alt, \kbnumline],
		],

	/////

	timeline: [
		["play_timeline", \kb, 0, \f5],
		["stop_timeline", \kb, 0, \f6],
		["close_timeline", \kb, 0, \escape],
		["forward_in_timeline", \kb, 0, \right],
		["backward_in_timeline", \kb, 0, \left],
	],
	track_notes: [
		["remove_notes", \kb, 0, \delete],
		["copy_notes", \kb, \ctrl, "c"],
		["cut_notes", \kb, \ctrl, "x"],
		["paste_notes", \kb, \ctrl, "v"],
		["paste_notes_in_place", \kb, \ctrlalt, "v"],
		["duplicate_score", \kb, \ctrl, "d"],
		["edit_noterange", \kb, \alt, "r"],
	],
	track_timeline: [
		["remove_notes", \kb, 0, \delete],
	],
	track_curve: [
		["remove_notes", \kb, 0, \delete],
		["copy_notes", \kb, \ctrl, "c"],
		["cut_notes", \kb, \ctrl, "x"],
		["paste_notes", \kb, \ctrl, "v"],
		["paste_notes_in_place", \kb, \ctrlalt, "v"],
		["duplicate_score", \kb, \ctrl, "d"],
	],
	track_custom_env: 
		~common_bindings.grid_display ++
		[
			["set_release_node", \kb, \ctrl, "r"],
			["set_loop_node", \kb, \ctrl, "l"],
		],
	group_tracks: 
		~common_bindings.side_shared ++
		~common_bindings.grid_display ++
		~common_bindings.panels ++ [
		["close_window", \kb, 0, \escape],
		["refresh_tracks", \kb, \altshift, "r"],
	],
	player_tracks: 
		~common_bindings.side_shared ++
		~common_bindings.panels ++
		~common_bindings.grid_display ++
		~common_bindings.playing ++ 
		[
			["close_window", \kb, 0, \escape],
			["select_scoresheet", \kb, \alt, \kbnumline],
			["save_scoresheet", \kb, 0, \f2],
		],
	line_tracks: 
		~common_bindings.side_shared ++
		~common_bindings.playing ++ 
		~common_bindings.windowing ++
		~common_bindings.panels ++
		[
			["decrease_gridstep_x", \kb, \ctrl, "h"],
			["increase_gridstep_x", \kb, \ctrl, "l"],
			["decrease_gridstep_y", \kb, \ctrl, "k"],
			["increase_gridstep_y", \kb, \ctrl, "j"],
			["decrease_gridlen", \kb, 0, "l"],
			["increase_gridlen", \kb, 0, "h"],
			["select_scoresheet", \kb, \alt, \kbnumline],
			["save_scoresheet", \kb, 0, \f2],
		],
	effects:
		~common_bindings.side_shared ++
		~common_bindings.playing ++
		~common_bindings.panels ++
	[
		["switch_body_view", \kb, \alt, "b"],
		["close_window", \kb, 0, \escape],
		["load_effect", \kb, 0, \f1],
		["select_param", \kb, 0, \kbpad8x4_flat],
		["select_player", \kb, 0, \kbnumpad],
		["edit_modulator", \kb, \alt, "m"],
		["change_param_kind", \kb, \altshift, "s"],
		["remove_effect", \kb, \alt, \delete],
		["edit_free_defer_time", \kb, \alt, "d"],
		["select_param_preset", \kb, \alt, \kbnumline],
	],
	modulator: 
		~common_bindings.side_shared ++
		~common_bindings.playing ++
		~common_bindings.panels ++
		[
		["switch_body_view", \kb, \alt, "b"],
		["close_window", \kb, 0, \escape],
		["load_modulator", \kb, 0, \f1],
		["select_param", \kb, 0, \kbpad8x4_flat],
		["select_player", \kb, 0, \kbnumpad],
		["edit_modulator", \kb, \alt, "m"],
		["change_param_kind", \kb, \altshift, "s"],
		["change_modulated_param_kind", \kb, \shift, "s"],
		//["change_modulator_mode", \kb, \altshift, "q"],
		["change_player_mode", \kb, \altshift, "q"],
		["change_mod_kind", \kb, \altshift, "d"],
		["remove_modulator", \kb, \alt, \delete],
		["disconnect_modulator", \kb, 0, \delete],
		["select_param_preset", \kb, \alt, \kbnumline],
		//["edit_selected_param", \kb, 0, \enter],

	],
	nodes_viewer: 
		~common_bindings.windowing ++
		[
			["play_node", \kb, 0, \kbpad8x4_flat],
		],
	side: 
		~common_bindings.side_shared ++
		~common_bindings.panels ++ [
		["add_modenv", \kb, \ctrlalt, "o"], // debug
		["rename_player", \kb, \alt, "r"],
		["stop_all_with_quant",					\kb, \ctrl, \f8],

		["enter_selected_subgroup", \kb, 0, \end],
		["go_parent_group", \kb, 0, \home],

		//["edit_external_player", \kb, \alt, "e"],
		//["open_timeline", \kb, \ctrlalt, "l"],
		//["edit_group_tracks", \kb, \alt, "g"],
		//["edit_player_tracks", \kb, \alt, "p"],
		//["edit_line_tracks", \kb, \alt, "q"],
		//["edit_modulator", \kb, \alt, "m"],
		//["edit_effects", \kb, \alt, "f"],

		["decrease_select_offset", \midi, 0, \begin],
		["increase_select_offset", \midi, 0, \end],
		["increase_samplekit_part", \midi, 0, \right],
		["decrease_samplekit_part", \midi, 0, \left],

		["midi.play_group", \midi, \hold, \play],
		["midi.stop_group", \midi, \hold, \stop],
		["play_selected", \kb, 0, \f5],
		["midi.play_selected", \midi, 0, \play],
		["stop_selected", \kb, 0, \f6],
		["stop_selected_quant", \kb, \ctrlshift, \f6],
		["midi.stop_selected", \midi, 0, \stop],
		["toggle_mute_selected", \kb, 0, \f7],
		//["unmute_selected", \kb, \ctrl, \f7],
		["toggle_solo_selected", \kb, \ctrl, \f7],
		["midi.toggle_solo_selected", \midi, 0, \pause],
		["panic", \kb, 0, \f8],


		["copy_node", \kb, \ctrl, "c"],
		["copy_node_children", \kb, \ctrlalt, "c"],
		["cut_node", \kb, \ctrl, "x"],
		["paste_node", \kb, \ctrl, "v"],
		//["copy_group", \kb, \ctrl, \kbnumpad],
		["remove_node", \kb, \alt, \delete],
		["remove_and_free_node", \kb, \altshift, \delete],

		["load_node_from_lib", \kb, 0, \f1],
		["create_default_node", \kb, \alt, "c"],
		["reload_player", \kb, \altshift, "r"],

		["add_effect", \kb, \ctrlshift, \f2],

		//["load_colpreset", \kb, \ctrl, \f1],
		//["save_colpreset", \kb, \ctrl, \f2],
		["quick_save_project", \kb, \ctrlaltshift, "s"],
		["quick_load_project", \kb, \ctrlaltshift, "l"],


		["load_preset", \kb, \ctrl, \f1],
		["save_preset", \kb, \ctrl, \f2],

		["select_param", \kb, 0, \kbpad8x4_flat],
		["pad_select_param", \midi, 0, \midipads],
		["midi.select_row", \midi, \hold, \midipads], // nodematrix

		["select_param_cell", \kb, \alt, \kbnumline],
		["matrix_select_param_cell", \kb, 0, \kbpad8x4_flat],

		["select_player", \kb, 0, \kbnumpad],
		["pad_select_player", \midi, 0, \midipads],
		["matrix_select_player", \kb, \alt, \kbnumline],


		["select_variant", \kb, \ctrl, \kbnumpad],
		["select_section", \kb, \alt, \kbnumpad],
		["select_part", \kb, \ctrlalt, \kbnumpad],

		//["forward_in_record_history", \kb, 0, \right],
		//["backward_in_record_history", \kb, 0, \left],

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
		["toggle_player_recording", \kb, \ctrlalt, "r"],
		["midi.toggle_player_recording", \midi, \record, \play],
		["toggle_param_recording", \midi, \record, \pause],

		["toggle_freeze_recording", \kb, \ctrlalt, "f"],

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



//	editplayer: [
//		["select_cell",							\kb, 0, \kbnumline],
//		["edit_value", 							\kb, 0, \enter],
//		["edit_value_mode.insert_number",		\kb, 0, \kbnumpad],
//		["edit_value_mode.insert_point",		\kb, 0, \point],
//		["edit_value_mode.cancel",				\kb, 0, \escape],
//		["edit_value_mode.ok",					\kb, 0, \enter],
//
//		["solo_selected", \kb, 0, \f7],
//		["unsolo_selected", \kb, \ctrl, \f7],
//		["unsolo_selected", \kb, \ctrl, \f7],
//		["add_effect",							\kb, \ctrl, \f1],
//		["increase_midi_knob_offset",			\kb, 0, \down],
//		["decrease_midi_knob_offset",			\kb, 0, \up],
//		["toggle_cc_recording",					\kb, \altshift, "r"],
//		["change_param_kind.recordbus",			\kb, \altshift, "u"],
//		["start_midi_liveplayer",			\kb, \altshift, "e"],
//		["param_set_pkey_mode",			\kb, \alt, "k"],
//		["param_unset_pkey_mode",			\kb, \altshift, "k"],
//
//	],
//	parlive: [
//		["select_header",							\kb, \alt, \kbnumline],
//		["show_panel.editplayer",							\kb, 0, \f12],
//		["create_new_livenode", \kb, \alt, "c"],
//	],
