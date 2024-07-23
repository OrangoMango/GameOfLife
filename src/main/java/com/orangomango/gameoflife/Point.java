package com.orangomango.gameoflife;

import java.util.Objects;

public class Point{
	private int x, y;

	public Point(int x, int y){
		this.x = x;
		this.y = y;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	@Override
	public int hashCode(){
		return Objects.hash(this.x, this.y);
	}

	@Override
	public boolean equals(Object other){
		if (other instanceof Point p){
			return p.x == this.x && p.y == this.y;
		} else {
			return false;
		}
	}
}