topEnvironment.push; // this must be always executed in the toplevel Environment (FIXME: break current project when reinitilized)

////////////////////////////
// - used by Veco class to load projects
// - each project sit in its own environment, class_project_manager always manage the current project in the current environment
// - if you load a project when there is already a project loaded in the environment, the old one is destructed (free resources)
// - a project is a class_veco_sceneset (~veco, Veco.main) and a class_score_manager (~score_storage)
// - open_project in fact use ~veco.open_project which load all node (1.scd, etc) file then init file (init.scd)
// - when the view (launchpad) request a clip, it ask the node which create it from the associated file (1.1.scd) on demand
// 
// - class_project_manager is a kind of project slot handler that free previous project when a new one is loaded
// - switching projects is just replacing the launchpad midi responder with one of a different project and pushing the project env
///////////////////////////


// following variables deprecated, use Veco.extension_path and Veco.user_path
//~veco_dir_path = Platform.userExtensionDir +/+ "seco/seco/veco";
//~veco_code_path = "~/code/sc/seco/vlive/".standardizePath;

"=======================================================================".inform;
"loading Veco...".inform;
"=======================================================================".inform;

~make_class_responder = { arg self, parent, model, list, auto_refresh=true;
	var controller;

	controller = SimpleController(model);
	list.do { arg method;
		controller.put(method, { arg ... args; self[method].(self, *args) });
	};

	parent.onClose = parent.onClose.addFunc { controller.remove };

	if(auto_refresh) { model.refresh() };

	controller;
};

VecoLib.load_lib;

"~/seco/seco/synth.scd".standardizePath.load;
"~/.local/share/SuperCollider/Extensions/seco/seco/eventscore.scd".standardizePath.load;
[
	//"tile",
	"recorder",
	"buffer",
	"launchpad",
	"score",
	"sdef",
	"launchpad_editor",
	"launchpad_gui",
	"inline_editor",
].do { arg file;
	("Loading " ++ file ++".scd...").inform;
	(Veco.extension_path +/+ file++".scd").load;
};
"Done loading.".inform;

//~veco  = ~class_veco_buffer_manager.new;
//~veco  = ~class_veco.new;

~closer_responder = { arg view, char, modifiers, u, k; 
		//["tempo", modifiers, u].debug("KEYBOARD INPUT");
		if( u == 27 ) { view.close() };
		//if( u == ~keycode.kbspecial.enter ) { validate_action.(); close_window.(); };
};

BusDef(\null, \audio);
BusDef(\zero, \audio);

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


~class_project_manager = (
	new: { arg self;
		self = self.deepCopy;
	
		
	
		self;
	},

	reset_board: {
		if(~veco.notNil) {
			~veco.project_destructor;
		};
		~veco = ~class_veco_sceneset.new;
		~veco.putAll(~scenestate);
		//Veco.main = ~veco;
		~score_storage = ~class_score_manager.new;
	},

	open_project: { arg self, path, activate=true;
		path.debug("=========================######### Start loading project");
		self.reset_board;
		~veco.open_project(path); 

		if(activate != false) {
			~veco.clip.activate;
		};
		path.debug("=========================######### End loading project");
		
	},

	open_side_project: { arg self, path;
		// NOT USED!
		self.reset_side_slot(path);
		self.side_slot(path).open_project(path);
		if(self.projects[path].notNil) {
			
		};
		self.projects[path] = Environment.new;
	
	
	},
	
);

~veco_project_manager = ~class_project_manager.new;
//~veco_project_manager.reset_board;
