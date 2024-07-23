package com.orangomango.gameoflife;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class World{
	public static int CELL_SIZE = 20;

	private HashSet<Point> aliveCells = new HashSet<>();

	public boolean update(){
		HashMap<Point, Integer> count = new HashMap<>(); // Neighbor count

		for (Point cell : this.aliveCells){
			for (int dx = -1; dx < 2; dx++){
				for (int dy = -1; dy < 2; dy++){
					if (dx == 0 && dy == 0) continue;
					Point neighbor = new Point(cell.getX()+dx, cell.getY()+dy);
					count.put(neighbor, count.getOrDefault(neighbor, 0)+1);
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

	public CellsData load(File file){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String name = "";
			String description = "";
			String data = "";

			String line;
			while ((line = reader.readLine()) != null){
				if (line.startsWith("!Name:")){
					name = line.split("!Name: ")[1];
				} else if (line.startsWith("!")){
					description += line.substring(1)+"\n";
				} else {
					data += line+"\n";
				}
			}

			HashSet<Point> cells = new HashSet<>();
			String[] lines = data.split("\n");

			for (int y = 0; y < lines.length; y++){
				char[] chars = lines[y].toCharArray();
				for (int i = 0; i < chars.length; i++){
					if (chars[i] == 'O'){
						cells.add(new Point(10+i, 10+y));
					}
				}
			}

			reader.close();

			return new CellsData(name, description, cells);
		} catch (IOException ex){
			ex.printStackTrace();
		}

		return null;
	}

	public void generateRandom(int w, int h){
		this.aliveCells.clear();
		for (int i = 0; i < w; i++){
			for (int j = 0; j < h; j++){
				if (Math.random() < 0.15){
					this.aliveCells.add(new Point(i, j));
				}
			}
		}
	}

	public HashSet<Point> backup(){
		return new HashSet<Point>(this.aliveCells);
	}

	public void restore(HashSet<Point> backup){
		this.aliveCells = new HashSet<Point>(backup);
	}

	public void reset(){
		this.aliveCells.clear();
	}

	public void put(int x, int y, int val){
		if (val == 1){
			this.aliveCells.add(new Point(x, y));
		} else {
			this.aliveCells.remove(new Point(x, y));
		}
	}

	public int countAlive(){
		return aliveCells.size();
	}
}