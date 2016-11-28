package com.neet.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.gamefromscratch.MainClass;
import com.neet.entities.Asteroid;
import com.neet.entities.Bullet;
import com.neet.entities.Particle;
import com.neet.entities.Player;

import game.neet.managers.GameStateManager;
import game.neet.managers.Jukebox;
import game.neet.managers.Save;

public class PlayState extends GameState{

	
	private SpriteBatch sb;
	private ShapeRenderer sr;
	
	private BitmapFont font;
	private Player hudPlayer;
	
	private Player player;
	private ArrayList<Bullet> bullets;
	private ArrayList<Asteroid> asteroids;
	
	private ArrayList<Particle>  particles; 
	
	private int level;
	private int totalAsteroids;
	private int numAsteroidsLeft;
	
	//sound
	private float maxDelay;
	private float minDelay;
	private float currentDelay;
	private float bgTimer;
	private boolean playLowPulse;
	
	
	private float spawnTimer;
	public PlayState(GameStateManager gsm) {
		super(gsm);
		
	}
	
	@Override
	public void init() {
		spawnTimer = 0;
		
		sb = new SpriteBatch();
		sr = new ShapeRenderer();
		
		//set font
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/BankGothic Bold.ttf"));
		font = gen.generateFont(20);
		
		bullets = new ArrayList<Bullet>();
		player = new Player(bullets);
		asteroids = new ArrayList<Asteroid>();
		particles = new ArrayList<Particle>();
		
		level = 1;
		spawnAsteroids();
		
		hudPlayer = new Player(null);
		
		//set up music
		maxDelay = 1;
		minDelay = 0.25f;
		currentDelay = maxDelay;
		bgTimer = maxDelay;
		playLowPulse = true;
	}
	
	private void createParticles(float x, float y){
		for(int i = 0; i < 6; ++i){
			particles.add(new Particle(x,y));
		}
	}
	
	private void splitAsteroids(Asteroid a){
		createParticles(a.getx(), a.gety());
		numAsteroidsLeft--;
		currentDelay = ((maxDelay - minDelay) * numAsteroidsLeft / totalAsteroids) + minDelay;
		if(a.getType() != Asteroid.SMALL){
			asteroids.add(new Asteroid(a.getx(), a.gety(),a.getType() - 1));
			asteroids.add(new Asteroid(a.getx(), a.gety(),a.getType() - 1));
		}
		
	}
	private void spawnAsteroids(){
		
		asteroids.clear();
		
		int numToSpawn = 3 + level;
		totalAsteroids = numToSpawn * 7;
		numAsteroidsLeft = totalAsteroids;
		
		
		currentDelay = maxDelay;
		
		for(int i = 0; i < numToSpawn; ++i){
			float x;	//spawn position x
			float y;	//spawn position y
			
			float dx;	//delta x
			float dy;	//delta y
			float dist;
			
			do{
				x = MathUtils.random(MathUtils.random(MainClass.WIDTH));
				y = MathUtils.random(MathUtils.random(MainClass.HEIGHT));
				
				//check distance from player
				dx = x - player.getx();
				dy = y - player.gety();
				dist = (float)Math.sqrt(dx * dx + dy * dy);
			}while(dist < 100);
			
			asteroids.add(new Asteroid(x,y,Asteroid.LARGE));
		}
	}

	@Override
	public void update(float dt) {
		//input
		handleInput();
		
		//next level
		if(asteroids.size() == 0){
			spawnTimer += dt;
			if(spawnTimer > 2){
				spawnTimer = 0;
				level++;
				spawnAsteroids();
			}
		}
		//update player
		player.update(dt);
		if(player.isDead()){
			if(player.getLives() == 0){
				Jukebox.stopAll();
				Save.gd.setTentativeScore(player.getScore());
				gsm.setState(GameStateManager.GAMEOVER);
				return;
			}
			player.reset();
			player.loseLife();
			return;
		}
		
		//update bullets
		for(int i = 0; i < bullets.size(); ++i){
			bullets.get(i).update(dt);
			
			if(bullets.get(i).shouldRemove()){
				bullets.remove(i);
				--i;
			}
		}
		
		//update asteroids
		for(int i = 0; i < asteroids.size(); ++i){
			asteroids.get(i).update(dt);
			
			if(asteroids.get(i).shouldRemove()){
				asteroids.remove(i);
				--i;
			}
		}
		
		//update particles
		for(int i = 0; i < particles.size(); ++i){
			particles.get(i).update(dt);
			if(particles.get(i).shouldRemove()){
				particles.remove(i);
				--i;
			}
		}
		//check collision
		checkCollisions();
		
		//play background music
		bgTimer += dt;
		if(!player.isHit() && bgTimer >= currentDelay && spawnTimer == 0){
			if(playLowPulse){
				Jukebox.play("pulselow");
			}else{
				Jukebox.play("pulsehigh");
			}
			playLowPulse = !playLowPulse;
			bgTimer = 0;
		}
		
	}
	
	private void checkCollisions(){
		
		//player-asteroid collision
		if(!player.isHit()){
			for(int i = 0; i < asteroids.size(); ++i){
				Asteroid a = asteroids.get(i);
				//if player intersects asteroid
				if(player.intersects(a)){
					player.hit();
					asteroids.remove(i);
					i--;
					splitAsteroids(a);
					Jukebox.play("explode");
					//break, if collides with one asteroid, we don't need to check all others (non crucial)
					break;
				}
			}
		}
		//bullet-asteroid collision
		for(int i = 0; i < bullets.size(); ++i){
			Bullet b = bullets.get(i);
			for(int j = 0; j < asteroids.size(); ++j){
				Asteroid a = asteroids.get(j);
				//Asteroid a contains Bullet b
				if(a.contains(b.getx(), b.gety())){
					bullets.remove(i);
					--i;
					asteroids.remove(j);
					--j;
					splitAsteroids(a);
					//increment score
					player.incrementScore(a.getScore());
					
					Jukebox.play("explode");
					//break to avoid outOfBounds read for bullets (crucial)
					break;
				}
				
			}
		}
	}

	@Override
	public void draw() {
		
		sb.setProjectionMatrix(MainClass.cam.combined);
		sr.setProjectionMatrix(MainClass.cam.combined);

		//draw player
		player.draw(sr);
		
		//draw bullets
		for(int i = 0; i < bullets.size(); ++i){
			bullets.get(i).draw(sr);
		}
		
		//draw asteroids
		for(int i = 0; i < asteroids.size(); ++i){
			asteroids.get(i).draw(sr);
		}
		//draw particles
		for(int i = 0; i < particles.size(); ++i){
			particles.get(i).draw(sr);
		}
		
		//draw score
		sb.setColor(1,1,1,1);
		sb.begin();
		font.draw(sb, "Score: " + Long.toString(player.getScore()),10,MainClass.HEIGHT - 10);
		sb.end();
		
		//draw lives
		for(int i = 0; i <  player.getLives(); ++i){
			hudPlayer.setPosition(40 + i * 10, 363);
			hudPlayer.draw(sr);
		}
	}

	@Override
	public void handleInput() {
		// TODO Auto-generated method stub
		player.setLeft(Gdx.input.isKeyPressed(Keys.LEFT));
		player.setRight(Gdx.input.isKeyPressed(Keys.RIGHT));
		player.setUp(Gdx.input.isKeyPressed(Keys.UP));
		if(Gdx.input.isKeyJustPressed(Keys.SPACE)){
			player.shoot();
			
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
