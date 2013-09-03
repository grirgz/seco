~veco_dir_path = Platform.userExtensionDir +/+ "seco/seco/veco";

"=======================================================================".inform;
"loading Veco...".inform;
"=======================================================================".inform;
[
	//"tile",
	"buffer",
	"launchpad",
].do { arg file;
	("Loading " ++ file ++".scd...").inform;
	(~veco_dir_path +/+ file++".scd").load;
};
"Done loading.".inform;

//~veco  = ~class_veco_buffer_manager.new;
//~veco  = ~class_veco.new;


~scenestate = (
	state_edit: 0,
	//state_seq: Array2D.new(8,8),
	state_leds: Array2D.new(9,8),
	state_seq: Array2D.fromArray(8,8, Array.fill(8*8, 1)),
	state_mode_leds: Array.fill(8, \off),
	state_play: Array2D.new(8,8),
	state_buffers: Array2D.new(8,8),
	state_subbuffers: Array2D.new(8,8),
	cleanup_function: { 
		//~sceneset.tile.reset_play_state 
	}
);

//~sceneset = (
//	tile: ~launchpad_scene_tile.new,
//	subbuffer: ~launchpad_scene_subbuffer.new,
//	seq: ~launchpad_scene_seq.new,
//	active_row: 1,
//).putAll(~scenestate);

~sceneset = ~class_veco_sceneset.new;
~sceneset.putAll(~scenestate);
~veco = ~sceneset;

"debut activ".debug;
~sceneset.clip.activate;
"fin activ".debug;
