package com.orangomango.gameoflife;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.HashSet;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.file.FileReader;
import dev.webfx.platform.file.File;
import dev.webfx.platform.blob.Blob;
import dev.webfx.platform.blob.spi.BlobProvider;
import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.platform.resource.Resource;

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
	private File selectedFile;

	@Override
	public void start(Stage stage){
		this.stage = stage;

		FilePicker picker = FilePicker.create();
		picker.setGraphic(new ImageView(new Image(Resource.toUrl("/images/icon.png", MainApplication.class))));
		picker.selectedFileProperty().addListener((ob, oldV, newV) -> {
			this.selectedFile = newV;
		});

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

				this.world.put(xp, yp, e.getButton() == MouseButton.SECONDARY ? 1 : 0);
			}
		});

		canvas.setOnMouseDragged(e -> {
			if (e.getButton() == MouseButton.PRIMARY){
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

		Scene scene = new Scene(new VBox(5, picker.getView(), canvasPane), WIDTH, HEIGHT);
		scene.setFill(Color.BLACK);

		this.stage.setTitle("Game Of Life");
		this.stage.getIcons().add(new Image(Resource.toUrl("/images/icon.png", MainApplication.class)));
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
			if (this.paused && this.selectedFile != null){
				FileReader.create().readAsText(this.selectedFile).onSuccess(fileData -> {
					CellsData data = this.world.load(fileData);
					this.world.restore(data.getData());
					resetSettings();
					Console.log("==========\nFile loaded. Name: "+data.getName()+"\nDescription: "+data.getDescription()+"==========");
				});
			}
			this.keys.put(KeyCode.L, false);
		} else if (this.keys.getOrDefault(KeyCode.S, false)){ // Save file [S]
			if (this.paused){
				saveData(this.backup, "gameoflife.cells"); // TODO
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
		gc.fillText("Generation: "+this.genCount+"\nAlive: "+this.world.countAlive()+"\nPaused: "+this.paused, 20, 30);
	}

	private void saveData(HashSet<Point> data, String fileName){
		if (!fileName.endsWith(".cells")){
			fileName += ".cells";
		}

		StringBuilder builder = new StringBuilder();
		builder.append("!Name: "+fileName);
		builder.append("\n! Description\n");

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

		for (int i = 0; i < w; i++){
			for (int j = 0; j < h; j++){
				builder.append(data.contains(new Point(minX+i, minY+j)) ? "O" : ".");
			}
			builder.append("\n");
		}

		Blob textBlob = BlobProvider.get().createTextBlob(builder.toString());
		BlobProvider.get().exportBlob(textBlob, fileName);
	}

	public static void main(String[] args){
		launch(args);
	}
}
