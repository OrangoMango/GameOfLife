package com.orangomango.gameoflife;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

public class World{
	public static int CELL_SIZE = 20;

	private HashSet<Point> aliveCells = new HashSet<>();

	public World(){
		reset();
	}

	public void reset(){
		this.aliveCells.clear();
	}

	public void put(int x, int y){
		this.aliveCells.add(new Point(x, y));
	}

	public int countAlive(){
		return aliveCells.size();
	}

	public boolean update(){
		HashMap<Point, Integer> count = new HashMap<>();

		for (Point cell : this.aliveCells){
			for (int dx = -1; dx < 2; dx++){
				for (int dy = -1; dy < 2; dy++){
					if (dx == 0 && dy == 0) continue;
					Point neighbour = new Point(cell.getX()+dx, cell.getY()+dy);
					count.put(neighbour, count.getOrDefault(neighbour, 0)+1);
				}
			}
		}

		HashSet<Point> newAliveCells = new HashSet<>();
		for (Map.Entry<Point, Integer> entry : count.entrySet()){
			Point cell = entry.getKey();
			int nCount = entry.getValue();

			if (nCount == 3 || (nCount == 2 && this.aliveCells.contains(cell))){
				newAliveCells.add(cell);
			}
		}

		boolean same = this.aliveCells.equals(newAliveCells);
		this.aliveCells = newAliveCells;

		return same;
	}

	public void render(GraphicsContext gc){
		for (Point cell : this.aliveCells){
			gc.setFill(Color.WHITE);
			gc.fillRect(cell.getX()*CELL_SIZE, cell.getY()*CELL_SIZE, CELL_SIZE, CELL_SIZE);
		}
	}
}