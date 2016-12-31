package com.neet.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamefromscratch.MainClass;
import com.neet.entities.Asteroid;
import com.neet.entities.Bullet;
import com.neet.entities.FlyingSaucer;
import com.neet.entities.Particle;
import com.neet.entities.Player;

import game.neet.managers.GameStateManager;
import game.neet.managers.Jukebox;
import game.neet.managers.Save;

public class PlayState extends GameState implements ControllerListener{

	Controller controller;
	
	private SpriteBatch sb;
	private ShapeRenderer sr;
	
	private BitmapFont font;
	private Player hudPlayer;
	
	private Player player;
	private ArrayList<Bullet> bullets;
	private ArrayList<Asteroid> asteroids;
	private ArrayList<Bullet> enemyBullets;
	
	private FlyingSaucer flyingSaucer;
	private float fsTimer;
	private float fsTime;
	
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
		controller = Controllers.getControllers().first();
		Controllers.addListener(this);
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
		
		fsTimer = 0;
		fsTime = 12;
		enemyBullets = new ArrayList<Bullet>();
		
		//set up music
		maxDelay = 1;
		minDelay = 0.25f;
		currentDelay = maxDelay;
		bgTimer = maxDelay;
		playLowPulse = true;
		//Jukebox.loop("soundtrack");
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
			flyingSaucer = null;
			Jukebox.stop("smallsaucer");
			Jukebox.stop("largesaucer");
			return;
		}
		
		//update player bullets
		for(int i = 0; i < bullets.size(); ++i){
			bullets.get(i).update(dt);
			
			if(bullets.get(i).shouldRemove()){
				bullets.remove(i);
				--i;
			}
		}
		
		//update flying saucer
		if(flyingSaucer == null){
			fsTimer += dt;
			if(fsTimer >= fsTime){
				fsTimer = 0;
				int type = MathUtils.random() < 0.5? FlyingSaucer.SMALL: FlyingSaucer.LARGE;
				int direction = MathUtils.random() < 0.5? FlyingSaucer.RIGHT : FlyingSaucer.LEFT;
				flyingSaucer = new FlyingSaucer(type,direction,player,enemyBullets);
			}
			
		//if there is a flying saucer already on screen
		}else{
			flyingSaucer.update(dt);
			if(flyingSaucer.shouldRemove()){
				flyingSaucer = null;
				Jukebox.stop("smallsaucer");
				Jukebox.stop("largesaucer");
			}
			
		}
		
		//update fs bullets
		for(int i = 0; i < enemyBullets.size(); ++i){
			enemyBullets.get(i).update(dt);
			if(enemyBullets.get(i).shouldRemove()){
				enemyBullets.remove(i);
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
		
		// player-flying saucer collision
		if(flyingSaucer != null){
			if(player.intersects(flyingSaucer)){
				player.hit();
				createParticles(player.getx(), player.gety());
				createParticles(flyingSaucer.getx(), flyingSaucer.gety());
				flyingSaucer = null;
				Jukebox.stop("smallsaucer");
				Jukebox.stop("largesaucer");
				Jukebox.play("explode");

			}
		}
		
		// bullet-flying saucer collision
		if(flyingSaucer != null){
			for(int i = 0; i < bullets.size(); ++i){
				Bullet b = bullets.get(i);
				if(flyingSaucer.contains(b.getx(), b.gety())){
					bullets.remove(i);
					--i;
					createParticles(flyingSaucer.getx(), flyingSaucer.gety());
					player.incrementScore(flyingSaucer.getScore());
					flyingSaucer = null;
					Jukebox.stop("smallsaucer");
					Jukebox.stop("largesaucer");
					Jukebox.play("explode");
					//if saucer dead, no need to check rest of bullets, (non crucial)
					break;
				}
			}
		}
		
		//player-enemy bullets collision
		if(!player.isHit()){
			for(int i = 0; i < enemyBullets.size(); ++i){
				Bullet b = enemyBullets.get(i);
				if(player.contains(b.getx(), b.gety())){
					player.hit();
					enemyBullets.remove(i);
					createParticles(player.getx(),player.gety());
					i--;
					Jukebox.play("explode");
					//non-crucial
					break;
				}
			}
		}
		
		//flying saucer-asteroid collision
		if(flyingSaucer != null){
			for(int i = 0; i < asteroids.size(); ++i){
				Asteroid a = asteroids.get(i);
				if(a.intersects(flyingSaucer)){
					asteroids.remove(i);
					--i;
					splitAsteroids(a);
					createParticles(a.getx(),a.gety());
					createParticles(flyingSaucer.getx(),flyingSaucer.gety());
					flyingSaucer = null;
					Jukebox.stop("smallsaucer");
					Jukebox.stop("largesaucer");
					Jukebox.play("explode");
					//non-crucial
					break;
				}
			}
		}
		
		//asteroid-enemy bullet collision
		for(int i = 0; i < enemyBullets.size(); ++i){
			Bullet b = enemyBullets.get(i);
			for(int j = 0; j < asteroids.size(); ++j){
				Asteroid a = asteroids.get(j);
				if(a.contains(b.getx(), b.gety())){
					enemyBullets.remove(i);
					--i;
					asteroids.remove(j);
					--j;
					splitAsteroids(a);
					createParticles(a.getx(), a.gety());
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
		
		//draw flying saucer
		if(flyingSaucer != null){
			flyingSaucer.draw(sr);
		}
		
		//draw fs bullets
		for(int i = 0; i < enemyBullets.size(); ++i){
			enemyBullets.get(i).draw(sr);
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
		
		/*Keyboard input*/
		
		player.setLeft(Gdx.input.isKeyPressed(Keys.LEFT));
		player.setRight(Gdx.input.isKeyPressed(Keys.RIGHT));
		player.setUp(Gdx.input.isKeyPressed(Keys.UP));
		if(Gdx.input.isKeyJustPressed(Keys.SPACE)){
			player.shoot();
			
		}
		
		/*GamePad input*/
		/*
		player.setRight(controller.getAxis(XBox360Pad.AXIS_RIGHT_X) > 0.2f);
		player.setLeft(controller.getAxis(XBox360Pad.AXIS_RIGHT_X) < -0.2f);
		player.setUp(controller.getAxis(XBox360Pad.AXIS_LEFT_Y) < -0.2f);
		*/
	}

	@Override
	public void dispose() {
		sb.dispose();
		sr.dispose();
		font.dispose();
		
		
	}

	@Override
	public boolean accelerometerMoved(Controller arg0, int arg1, Vector3 arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean axisMoved(Controller arg0, int arg1, float arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		
		
		/*
		if(buttonCode == 5)
			player.shoot();
		*/
		return false;
	}

	@Override
	public boolean buttonUp(Controller arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void connected(Controller arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnected(Controller arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean povMoved(Controller arg0, int arg1, PovDirection arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

}
