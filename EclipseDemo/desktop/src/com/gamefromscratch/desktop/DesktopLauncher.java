package com.gamefromscratch.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gamefromscratch.MainClass;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//new LwjglApplicationConfiguration();
		config.title = "Asteroids";
		config.width = 500;
		config.height = 400;
		config.useGL30 = false;
		config.resizable = true;		
		new LwjglApplication(new MainClass(),config);
		
	}
}
