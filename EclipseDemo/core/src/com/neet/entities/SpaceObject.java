package com.neet.entities;

import com.gamefromscratch.MainClass;

public class SpaceObject {
	protected static final float PI = 3.14159f;
	
	protected float x;
	protected float y;
	
	protected float dx;
	protected float dy;
	
	protected float radians;
	protected float speed;
	protected float rotationSpeed;
	
	protected int width;
	protected int height;
	
	protected float[] shapex;
	protected float[] shapey;
	
	public float getx(){ return x;}
	public float gety(){return y;}
	
	public float[] getShapex(){return shapex;}
	public float[] getShapey(){return shapey;}
	
	public void setPosition(float x, float y){
		this.x = x;
		this.y = y;
	}
	public boolean intersects(SpaceObject other){
		float[] sx = other.getShapex();
		float[] sy = other.getShapey();
		for(int i = 0; i < sx.length; ++i){
			if(contains(sx[i],sy[i])){
				return true;
			}
		}
		return false;
	}
	public boolean contains(float x, float y){
		boolean b = false;
		//algorithm for if this object's polygon contains point (x,y)
		for(int i = 0, j = shapex.length -1; i < shapex.length; j = i++){
			if((shapey[i] > y) != (shapey[j] > y) &&  
					(x < (shapex[j] - shapex[i]) * 
					(y - shapey[i]) / (shapey[j] - shapey[i])
					+ shapex[i])){
				//then clause
				b = !b;
			}
		}
		return b;
	}
	protected void wrap(){
		if(x < 0) 
			x = MainClass.WIDTH;
		if(x > MainClass.WIDTH)
			x = 0;
		if(y < 0) 
			y = MainClass.HEIGHT;
		if(y > MainClass.HEIGHT)
			y = 0;
	}
}
