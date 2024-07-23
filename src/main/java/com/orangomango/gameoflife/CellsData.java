package com.orangomango.gameoflife;

import java.util.HashSet;

public class CellsData{
	private String name, description;
	private HashSet<Point> data;

	public CellsData(String name, String desc, HashSet<Point> data){
		this.name = name;
		this.description = desc;
		this.data = data;
	}

	public String getName(){
		return this.name;
	}

	public String getDescription(){
		return this.description;
	}

	public HashSet<Point> getData(){
		return this.data;
	}
}