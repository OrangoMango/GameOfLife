package com.orangomango.gameoflife;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;

public class UiButton{
	private static final int SIZE = 32;
	private static final Image IMAGE = new Image(UiButton.class.getResourceAsStream("/buttons.png"));

	private double x, y;
	private Runnable onClick;
	private int frameIndex;

	public UiButton(double x, double y, int frameIndex, Runnable onClick){
		this.x = x;
		this.y = y;
		this.frameIndex = frameIndex;
		this.onClick = onClick;
	}

	public boolean click(double x, double y){
		Rectangle2D rect = new Rectangle2D(this.x, this.y, SIZE, SIZE);
		if (rect.contains(x, y)){
			this.onClick.run();
			return true;
		}

		return false;
	}

	public void updatePosition(double x, double y){
		this.x = x;
		this.y = y;
	}

	public void render(GraphicsContext gc){
		gc.drawImage(IMAGE, 1+this.frameIndex*34, 1, 32, 32, this.x, this.y, SIZE, SIZE);
	}
}