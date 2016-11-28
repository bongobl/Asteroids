package game.neet.managers;

import com.neet.gamestates.GameOverState;
import com.neet.gamestates.GameState;
import com.neet.gamestates.HighScoreState;
import com.neet.gamestates.MenuState;
import com.neet.gamestates.PlayState;

public class GameStateManager {
	
	private GameState gameState;
	public static final int MENU = 0;
	public static final int PLAY = 1;
	public static final int HIGHSCORE = 5;
	public static final int GAMEOVER = 12;
	
	
	public GameStateManager(){
		setState(MENU);
	}
	
	public void setState(int state){
		
		if(gameState != null)
			gameState.dispose();
		
		if(state == MENU){
			gameState = new MenuState(this);
		}
		if(state == PLAY){
			gameState = new PlayState(this);
		}
		if(state == HIGHSCORE){
			gameState = new HighScoreState(this);
		}
		if(state == GAMEOVER){
			gameState = new GameOverState(this);
		}
	}
	
	public void update(float dt){
		gameState.update(dt);
	}
	
	public void draw(){
		gameState.draw();
	}
}
