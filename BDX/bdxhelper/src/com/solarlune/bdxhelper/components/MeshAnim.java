package com.solarlune.bdxhelper.components;

import java.util.ArrayList;
import java.util.HashMap;

import com.nilunder.bdx.Component;
import com.nilunder.bdx.GameObject;
import com.nilunder.bdx.State;
import com.nilunder.bdx.utils.Timer;

public class MeshAnim extends Component <GameObject> {
	
	public static class Animation extends ArrayList<String>{
		public String name;
		public float fps;
		public boolean looping;

		public int playHead;
		public int playDir;

		public Animation(String name, float fps, boolean looping){
			this.name = name;
			this.fps = fps;
			this.looping = looping;
			playHead = 0;
			playDir = 1;
		}

		public String nextFrame(){
			if (onLastFrame()){
				if (looping)
					reset();
				else
					playHead -= playDir;
			}

			String frame = get(playHead); 

			playHead += playDir;

			return frame;
		}

		public boolean onLastFrame(){
			return playHead == size() || playHead == -1;
		}

		public void reset(){
			if (playDir > 0)
				playHead = 0;
			else
				playHead = size() - 1;
		}

	}
	
	public float speed;
	public HashMap<String, Animation> animations;
	public Animation active;
	public String currentMesh;
	
	private Timer ticker;
	
	public MeshAnim(GameObject g) {
		
		// Basically, this just "rides" SpriteAnim's animation process and 
		// replaces the GameObject's mesh accordingly instead of messing with UV values
		
		super(g);
		animations = new HashMap<String, Animation>();
		ticker = new Timer();
		speed = 1;
		state = play;
		currentMesh = "";
		
	}
		
	public void add(String name, int[] frames){	
		add(name, frames, 12, true);
	}
	
	public void add(String name, int[] frames, int fps, boolean looping) {
		Animation anim = new Animation(name, fps, looping);
		
		for (int i : frames) {
			
			String fin = name + Integer.toString(i);
			
			anim.add(fin);
			
		}
		
		animations.put(name, anim);
	}
	
	public void play(String name){
		Animation next = animations.get(name);

		if (active != next){
			active = next;
			ticker.done(true); // immediate play
		}

		if (!active.looping && active.onLastFrame()){
			active.reset();
			ticker.done(true);
		}

	}
	
	public void frame(int frame){
		active.playHead = frame; // Set the frame, and
		ticker.done(true); // Update the sprite immediately
	}

	public int frame(){
		return active.playHead;
	}
	
	public void showNextFrame(){
		active.playDir = speed * active.fps < 0 ? -1 : 1;
		
		String frame = active.nextFrame();
		
		if (currentMesh != frame) {
			currentMesh = frame;
			g.replaceModel(currentMesh);
		}
	}
	
	private State play = new State(){
		private float nz(float n){
			return n <= 0 ? Float.MIN_VALUE : n;
		}

		public void main(){
			if (active == null)
				return;

			ticker.interval = 1f / nz(Math.abs(active.fps) * Math.abs(speed));

			if (ticker.tick()){
				showNextFrame();
			}
		}
	};

}
