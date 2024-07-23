package com.orangomango.gameoflife;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.HashMap;
import java.util.HashSet;
import java.io.*;

public class MainApplication extends Application{
	private static int WIDTH = 1000;
	private static int HEIGHT = 700;

	private Stage stage;
	private World world;
	private HashMap<KeyCode, Boolean> keys = new HashMap<>();
	private boolean paused = true, showGrid = false;
	private int genCount = 0;
	private double offsetX, offsetY, dragX = -1, dragY = -1;
	private HashSet<Point> backup;

	@Override
	public void start(Stage stage){
		this.stage = stage;
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		CanvasPane canvasPane = new CanvasPane(canvas, (w, h) -> {
			WIDTH = (int)w;
			HEIGHT = (int)h;
		});

		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));

		canvas.setOnMousePressed(e -> {
			if (this.paused){
				int xp = (int)Math.floor((e.getX()-this.offsetX)/World.CELL_SIZE);
				int yp = (int)Math.floor((e.getY()-this.offsetY)/World.CELL_SIZE);

				this.world.put(xp, yp, e.getButton() == MouseButton.PRIMARY ? 1 : 0);
			}
		});

		canvas.setOnMouseDragged(e -> {
			if (e.getButton() == MouseButton.PRIMARY){
				int xp = (int)Math.floor((e.getX()-this.offsetX)/World.CELL_SIZE);
				int yp = (int)Math.floor((e.getY()-this.offsetY)/World.CELL_SIZE);

				this.world.put(xp, yp, e.getButton() == MouseButton.PRIMARY ? 1 : 0);
			} else if (e.getButton() == MouseButton.SECONDARY){
				if (this.dragX == -1 && this.dragY == -1){
					this.dragX = e.getX();
					this.dragY = e.getY();
				} else {
					this.offsetX += e.getX()-this.dragX;
					this.offsetY += e.getY()-this.dragY;
					this.dragX = e.getX();
					this.dragY = e.getY();
				}
			}
		});

		canvas.setOnMouseReleased(e -> {
			this.dragX = -1;
			this.dragY = -1;
		});

		canvas.setOnScroll(e -> {
			if (e.getDeltaY() > 0){
				World.CELL_SIZE += 1;
				this.offsetX -= 20;
				this.offsetY -= 20;
			} else if (e.getDeltaY() < 0){
				World.CELL_SIZE -= 1;
				this.offsetX += 20;
				this.offsetY += 20;
			}
		});

		this.world = new World();
		this.backup = this.world.backup();

		Timeline loop = new Timeline(new KeyFrame(Duration.millis(50), e -> {
			if (!this.paused){
				boolean same = this.world.update();
				if (same){
					this.paused = true;
				} else {
					this.genCount++;
				}
			}
		}));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();

		AnimationTimer renderLoop = new AnimationTimer(){
			@Override
			public void handle(long time){
				update(gc);
			}
		};
		renderLoop.start();

		Scene scene = new Scene(canvasPane, WIDTH, HEIGHT);
		scene.setFill(Color.BLACK);

		this.stage.setTitle("Game Of Life");
		this.stage.setScene(scene);
		this.stage.show();
	}

	private void resetSettings(){
		this.genCount = 0;
		this.offsetX = 0;
		this.offsetY = 0;
		World.CELL_SIZE = 20;
	}

	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);

		if (this.keys.getOrDefault(KeyCode.SPACE, false)){ // Pause/Resume [SPACE]
			if (this.paused){
				this.backup = this.world.backup(); // Make backup before the simulation starts
			}
			this.paused = !this.paused;
			this.keys.put(KeyCode.SPACE, false);
		} else if (this.keys.getOrDefault(KeyCode.R, false)){ // Reset [R]
			if (this.paused){
				this.world.reset();
				resetSettings();
			}
			this.keys.put(KeyCode.R, false);
		} else if (this.keys.getOrDefault(KeyCode.G, false)){ // Show/Hide grid [G]
			this.showGrid = !this.showGrid;
			this.keys.put(KeyCode.G, false);
		} else if (this.keys.getOrDefault(KeyCode.C, false)){ // Create backup [C]
			if (this.paused){
				this.backup = this.world.backup();
			}
			this.keys.put(KeyCode.C, false);
		} else if (this.keys.getOrDefault(KeyCode.V, false)){ // Restore backup [V]
			if (this.paused){
				this.world.restore(this.backup);
				resetSettings();
			}
			this.keys.put(KeyCode.V, false);
		} else if (this.keys.getOrDefault(KeyCode.L, false)){ // Load file [L]
			if (this.paused){
				FileChooser chooser = new FileChooser();
				chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plaintext files", "*.cells"));
				File file = chooser.showOpenDialog(this.stage);
				if (file != null){
					CellsData data = this.world.load(file);
					this.world.restore(data.getData());
					resetSettings();
					System.out.format("==========\nFile loaded. Name: %s\nDescription: %s==========", data.getName(), data.getDescription());
				}
			}
			this.keys.put(KeyCode.L, false);
		} else if (this.keys.getOrDefault(KeyCode.S, false)){ // Save file [S]
			if (this.paused){
				FileChooser chooser = new FileChooser();
				chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plaintext files", "*.cells"));
				File file = chooser.showSaveDialog(this.stage);
				if (file != null){
					saveData(this.backup, file);
					System.out.println("File saved");
				}
			}
			this.keys.put(KeyCode.S, false);
		} else if (this.keys.getOrDefault(KeyCode.Q, false)){ // Random [Q]
			if (this.paused){
				this.world.generateRandom(WIDTH/World.CELL_SIZE, HEIGHT/World.CELL_SIZE);
				resetSettings();
			}
			this.keys.put(KeyCode.Q, false);
		}

		if (this.showGrid){
			// Draw grid
			if (this.showGrid){
				gc.setStroke(Color.web("#B3B3B3"));
				gc.setLineWidth(1.5);
				for (double i = this.offsetX % World.CELL_SIZE; i <= WIDTH; i += World.CELL_SIZE){
					gc.strokeLine(i, 0, i, HEIGHT);
				}
				for (double i = this.offsetY % World.CELL_SIZE; i <= HEIGHT; i += World.CELL_SIZE){
					gc.strokeLine(0, i, WIDTH, i);
				}
			}
		}

		gc.save();
		gc.translate(this.offsetX, this.offsetY);
		this.world.render(gc);
		gc.restore();

		gc.setFill(Color.LIME);
		gc.fillText(String.format("Generation: %d\nAlive: %d\nPaused: %s", this.genCount, this.world.countAlive(), this.paused), 20, 30);
	}

	private void saveData(HashSet<Point> data, File file){
		if (!file.getName().endsWith(".cells")){
			file = new File(file.getParent(), file.getName()+".cells");
		}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write("!Name: "+file.getName());
			writer.write("\n! Description\n");

			int minX = Integer.MAX_VALUE;
			int minY = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int maxY = Integer.MIN_VALUE;

			for (Point p : data){
				minX = Math.min(minX, p.getX());
				minY = Math.min(minY, p.getY());
				maxX = Math.max(maxX, p.getX());
				maxY = Math.max(maxY, p.getY());
			}

			final int w = maxX-minX+1;
			final int h = maxY-minY+1;

			for (int j = 0; j < h; j++){
				for (int i = 0; i < w; i++){
					writer.write(data.contains(new Point(minX+i, minY+j)) ? "O" : ".");
				}
				writer.write("\n");
			}

			writer.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	public static void main(String[] args){
		launch(args);
	}
}