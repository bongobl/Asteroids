package com.neet.gamestates;

import game.neet.managers.GameStateManager;

public abstract class GameState {

	protected GameStateManager gsm;
	protected GameState(GameStateManager gsm){
		this.gsm = gsm;
		init();
	}
	
	//methods that have to be implemented by subclasses
	public abstract void init();
	public abstract void update(float dt);
	public abstract void draw();
	public abstract void handleInput();
	public abstract void dispose();
}
