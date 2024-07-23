package com.orangomango.gameoflife;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.HashMap;

public class MainApplication extends Application{
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 700;

	private World world;
	private HashMap<KeyCode, Boolean> keys = new HashMap<>();
	private boolean paused = true, showGrid = false;
	private int genCount = 0;
	private double offsetX, offsetY, dragX = -1, dragY = -1;

	@Override
	public void start(Stage stage){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);

		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));

		canvas.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY && this.paused){
				int xp = (int)Math.round((e.getX()-this.offsetX)/World.CELL_SIZE);
				int yp = (int)Math.round((e.getY()-this.offsetY)/World.CELL_SIZE);

				this.world.put(xp, yp);
			}
		});

		canvas.setOnMouseDragged(e -> {
			if (e.getButton() == MouseButton.SECONDARY){
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
			} else if (e.getDeltaY() < 0){
				World.CELL_SIZE -= 1;
			}
		});

		this.world = new World();

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

		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		scene.setFill(Color.BLACK);

		stage.setTitle("Game Of Life");
		stage.setScene(scene);
		stage.show();
	}

	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);

		if (this.keys.getOrDefault(KeyCode.SPACE, false)){
			this.paused = !this.paused;
			this.keys.put(KeyCode.SPACE, false);
		} else if (this.keys.getOrDefault(KeyCode.R, false)){
			this.world.reset();
			this.genCount = 0;
			this.offsetX = 0;
			this.offsetY = 0;
			World.CELL_SIZE = 20;
			this.keys.put(KeyCode.R, false);
		} else if (this.keys.getOrDefault(KeyCode.G, false)){
			this.showGrid = !this.showGrid;
			this.keys.put(KeyCode.G, false);
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

	public static void main(String[] args){
		launch(args);
	}
}