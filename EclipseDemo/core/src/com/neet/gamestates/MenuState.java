package com.neet.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.gamefromscratch.MainClass;
import com.neet.entities.Asteroid;

import game.neet.managers.GameStateManager;
import game.neet.managers.Save;

public class MenuState extends GameState{

	private SpriteBatch sb;
	private ShapeRenderer sr;
	private BitmapFont titleFont;
	private BitmapFont font;
	
	private final String title = "Asteroids";
	
	private int currentItem;
	private String[] menuItems;
	
	private ArrayList<Asteroid> asteroids;
	public MenuState(GameStateManager gsm) {
		super(gsm);
		
	}

	@Override
	public void init() {
		currentItem = 0;

		sb = new SpriteBatch();
		sr = new ShapeRenderer();
		
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
				Gdx.files.internal("fonts/Hyperspace Bold.ttf"));
		
		titleFont = gen.generateFont(56);
		titleFont.setColor(Color.WHITE);
		font = gen.generateFont(20);
		
		menuItems = new String[]{
				"Play", "Highscores", "Quit"
		};
		Save.load();
		asteroids = new ArrayList<Asteroid>();
		for(int i = 0; i < 8; i++){
			asteroids.add(
				new Asteroid( MathUtils.random(MainClass.WIDTH), MathUtils.random(MainClass.HEIGHT), MathUtils.random(3) ) 
			);
		}
	}

	@Override
	public void update(float dt) {
		handleInput();
		
		for(int i = 0; i < asteroids.size(); ++i){
			asteroids.get(i).update(dt);
		}
	}

	@Override
	public void draw() {

		sb.setProjectionMatrix(MainClass.cam.combined);
		sr.setProjectionMatrix(MainClass.cam.combined);
		
		//draw asteroids
		for(int i = 0; i < asteroids.size(); ++i){
			asteroids.get(i).draw(sr);
		}
		sb.begin();
		
		
		//draw title
		float width = 300;
		GlyphLayout glyphLayout;
		glyphLayout = new GlyphLayout();
		glyphLayout.setText(titleFont, title);
		titleFont.draw(sb, glyphLayout, (MainClass.WIDTH - glyphLayout.width)/2 , 300);
		//draw menu
		for(int i = 0; i < menuItems.length; i++){
			glyphLayout.setText(font, menuItems[i]);
			
			if(currentItem == i)
				font.setColor(Color.RED);
			else
				font.setColor(Color.WHITE);
			font.draw(sb, menuItems[i], (MainClass.WIDTH - glyphLayout.width)/2, 180 - 35 * i);
		}
		sb.end();
		
	}

	@Override
	public void handleInput() {
		
		if(Gdx.input.isKeyJustPressed(Keys.UP)){
			if(currentItem > 0)
				currentItem--;		
		}
		if(Gdx.input.isKeyJustPressed(Keys.DOWN)){
			
			if(currentItem < menuItems.length - 1)
				currentItem++;
		}
		if(Gdx.input.isKeyJustPressed(Keys.ENTER)){
			select();
		}
	}
	
	private void select(){
		if(currentItem == 0){
			gsm.setState(GameStateManager.PLAY);
		}else if(currentItem == 1){
			gsm.setState(GameStateManager.HIGHSCORE);
		}else if(currentItem == 2){
			Gdx.app.exit();
		}
	}
	@Override
	public void dispose() {
		sb.dispose();
		sr.dispose();
		titleFont.dispose();
		font.dispose();
		
	}

}
