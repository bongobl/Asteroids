package com.neet.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.gamefromscratch.MainClass;

import game.neet.managers.GameStateManager;
import game.neet.managers.Save;

public class HighScoreState extends GameState{

	private SpriteBatch sb;
	private BitmapFont font;
	private long[] highScores;
	private String[] names;
	
	public HighScoreState(GameStateManager gsm) {
		super(gsm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() {
		sb = new SpriteBatch();
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
				Gdx.files.internal("fonts/Hyperspace Bold.ttf")
		); 
		font = gen.generateFont(20);
		Save.load();
		highScores = Save.gd.getHighScores();
		names = Save.gd.getNames();
	}

	@Override
	public void update(float dt) {
		handleInput();
		
	}

	@Override
	public void draw() {
		// TODO Auto-generated method stub
		sb.setProjectionMatrix(MainClass.cam.combined);
		sb.begin();


		GlyphLayout glyphLayout = new GlyphLayout();
		glyphLayout.setText(font, "High Scores");
		font.draw(sb, "High Scores", (MainClass.WIDTH - glyphLayout.width)/2, 300);
		
		String s;
		for(int i = 0; i < highScores.length; ++i){
			s = String.format(
					"%2d. %7s %s", 
					i + 1,
					highScores[i],
					names[i]);
			glyphLayout.setText(font, s);
			font.draw(sb, s, (MainClass.WIDTH - glyphLayout.width)/2, 270 - 20 * i);

		}
		sb.end();
		
	}

	@Override
	public void handleInput() {
		if(Gdx.input.isKeyJustPressed(Keys.ENTER) || 
		   Gdx.input.isKeyJustPressed(Keys.ESCAPE)){
			gsm.setState(GameStateManager.MENU);
		}
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
