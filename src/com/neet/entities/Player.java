package com.neet.entities;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.gamefromscratch.MainClass;

import game.neet.managers.Jukebox;

public class Player extends SpaceObject{
	
	private final int MAX_BULLETS = 4;
	private ArrayList<Bullet> bullets;
	private float flamex[];
	private float flamey[];
	
	private boolean left;
	private boolean right;
	private boolean up;
	
	private float maxSpeed;
	private float acceleration;
	private float deceleration;
	private float acceleratingTimer;
	
	private boolean hit;
	private boolean dead;
	
	private float hitTimer;
	private float hitTime;
	
	private Line2D.Float[] hitLines;		//lines shit is made of
	private Point2D.Float[] hitLinesVector;	//dirrections each line goes after hit	
	
	private long score;
	private int extraLives;
	private long requiredScore;
	
	//player constructor
	public Player(ArrayList<Bullet> bullets){
		
		this.bullets = bullets;
		
		//place in center of screen
		x = MainClass.WIDTH /2;
		y = MainClass.HEIGHT /2;
		
		maxSpeed = 300;
		acceleration = 200;
		deceleration = 10;
		
		shapex = new float[4];
		shapey = new float[4];
		radians = PI /2;
		rotationSpeed = 3;
		
		flamex = new float[3];
		flamey = new float[3];
		
		hit = false;
		hitTimer = 0;
		hitTime = 2;
		
		score = 0;
		extraLives = 3;
		requiredScore = 10000;
		
	}
	private void setShape(){
		
		//front
		shapex[0] = x + MathUtils.cos(radians) * 8;
		shapey[0] = y + MathUtils.sin(radians) * 8;
		
		//right
		shapex[1] = x + MathUtils.cos(radians - 4 * PI / 5) * 8;
		shapey[1] = y + MathUtils.sin(radians - 4 * PI / 5) * 8;
		
		//back
		shapex[2] = x + MathUtils.cos(radians + PI) * 5;
		shapey[2] = y + MathUtils.sin(radians + PI) * 5;
		
		//left
		shapex[3] = x + MathUtils.cos(radians + 4 * PI / 5) * 8;
		shapey[3] = y + MathUtils.sin(radians + 4 * PI / 5) * 8;

	}
	
	private void setFlame(){
		flamex[0] = x + MathUtils.cos(radians - 5 * PI / 6) * 5;
		flamey[0] = y + MathUtils.sin(radians - 5 * PI / 6) * 5;
		
		flamex[1] = x + MathUtils.cos(radians - PI) * (6 + acceleratingTimer * 50);
		flamey[1] = y + MathUtils.sin(radians - PI) * (6 + acceleratingTimer * 50);
		
		flamex[2] = x + MathUtils.cos(radians + 5 * PI / 6) * 5;
		flamey[2] = y + MathUtils.sin(radians + 5 * PI / 6) * 5;


	}
	
	public void setLeft(boolean b ){ left = b;}
	public void setRight(boolean b) {right = b;}
	public void setUp(boolean b) {
		
		//only called when up is just pressed
		if(b && !up){
			Jukebox.loop("thruster");
		}else if(!b || hit){
			Jukebox.stop("thruster");
		}
		up = b;
		
	}
	
	public void setPosition(float x, float y){
		super.setPosition(x, y);
		setShape();
	}
	public boolean isHit(){
		return hit;
	}
	public boolean isDead(){
		return dead;
	}
	
	public long getScore(){
		return score;
	}
	public int getLives(){
		return extraLives;
	}
	public void loseLife(){
		extraLives--;
	}
	public void incrementScore(long l){
		score += l;
	}
	public void reset(){
		//place in center of screen
		x = MainClass.WIDTH /2;
		y = MainClass.HEIGHT /2;
		//radians = PI /2;

		setShape();
		hit = dead = false;
		
	}
	public void shoot(){
		if(bullets.size() == this.MAX_BULLETS || hit){
			return;
		}
		bullets.add(new Bullet(x,y,radians));
		Jukebox.play("shoot");
	}
	
	public void hit(){
		
		//return if already hit
		if(hit) return;
		
		//zero out speed, turn controllers off
		hit = true;	//flag for update and draw
		dx = dy = 0;
		left = right = up = false;
		
		
		//construct hitlines from points array for player
		hitLines = new Line2D.Float[4];
		for(int i = 0, j = hitLines.length -1; i < hitLines.length; j = i++){
			hitLines[i] = new Line2D.Float(shapex[i], shapey[i], shapex[j], shapey[j]);
		}
		
		hitLinesVector = new Point2D.Float[4];
		hitLinesVector[0] = new Point2D.Float(MathUtils.cos(radians + 1.5f),MathUtils.sin(radians + 1.5f));
		hitLinesVector[1] = new Point2D.Float(MathUtils.cos(radians - 1.5f),MathUtils.sin(radians - 1.5f));
		hitLinesVector[2] = new Point2D.Float(MathUtils.cos(radians - 2.8f),MathUtils.sin(radians - 2.8f));
		hitLinesVector[3] = new Point2D.Float(MathUtils.cos(radians + 2.8f),MathUtils.sin(radians + 2.8f));

	}
	
	public void update(float dt){
		//NOTE: construct radians, then construct dx and dy. Then use them to set x and y
		
		
		//check if hit
		if(hit){
			hitTimer += dt;
			if(hitTimer > hitTime){
				dead = true;
				hitTimer = 0;
			}
			for(int i = 0 ; i < hitLines.length; i++){
				hitLines[i].setLine(
						hitLines[i].x1 + hitLinesVector[i].x * 10 * dt,
						hitLines[i].y1 + hitLinesVector[i].y * 10 * dt,
						hitLines[i].x2 + hitLinesVector[i].x * 10 * dt,
						hitLines[i].y2 + hitLinesVector[i].y * 10 * dt
				);
			}
			//if hit, don't draw all other lines
			return;
		}
		
		//check extra lives
		if(score >= requiredScore){
			extraLives++;
			requiredScore += 10000;
			Jukebox.play("extralife");
		}
		//turning
		if(left){
			radians += rotationSpeed * dt;
		}else if(right){
			radians -= rotationSpeed * dt;
		}
		
		//accelerating, booster on
		if(up){
			dx += MathUtils.cos(radians) * acceleration * dt;
			dy += MathUtils.sin(radians) * acceleration * dt;
			acceleratingTimer += 3 * dt;
			if(acceleratingTimer > .2f){	//length of flame
				acceleratingTimer = 0;
			}
		//not accelerating, booster off
		}else{
			acceleratingTimer = 0;
		}
		
		
		//deceleration
		float vec = (float)Math.sqrt(dx*dx + dy*dy);
		if(vec > 0){
			dx -= (dx / vec) * deceleration * dt;
			dy -= (dy / vec) * deceleration * dt;
		}
		
		//don't let speed exceed maxSpeed
		if(vec > maxSpeed){
			dx = (dx / vec) * maxSpeed;
			dy = (dy / vec) * maxSpeed;
		}
		
		//setPosition
		x += dx * dt;
		y += dy * dt;
		
		//call setShape to redraw shape
		setShape();
		
		//call setFlame to redraw flame
		if(up){
			setFlame();
		}
		//screen wrap
		wrap();
	}
	
	public void draw(ShapeRenderer sr){
		
		//set color
		sr.setColor(1,1,1,1);
		
		//begin drawing shape
		sr.begin(ShapeType.Line);
		
		//check if hit
		if(hit){
			for(int i = 0;i < hitLines.length; ++i){
				sr.line(hitLines[i].x1, hitLines[i].y1, hitLines[i].x2, hitLines[i].y2);
			}
			
			//don't draw normal ship if hit
			sr.end();
			return;
		}
		//draw shape
		for(int i = 0, j = shapex.length - 1; i < shapex.length; j = i++){
			sr.line(shapex[i], shapey[i], shapex[j],shapey[j]);
		}
		
		if(up){
			for(int i = 0, j = flamex.length - 1; i < flamex.length; j = i++){
				sr.line(flamex[i], flamey[i], flamex[j],flamey[j]);
			}
		}
		
		
		//flush shape to buffer
		sr.end();
		
	}

}
