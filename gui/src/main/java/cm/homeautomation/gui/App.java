package cm.homeautomation.gui;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author christoph
 */
public class App extends Application {

	@Override
	public void start(Stage primaryStage) {
		Button btn = new Button();
		btn.setText("Hallo");
		btn.setOnAction(new EventHandler<ActionEvent>() {

			
			public void handle(ActionEvent event) {
				System.out.println("Hello World! ");
			}
		});

		Button btn2 = new Button();
		btn2.setText("Exit");
		btn2.setOnAction(new EventHandler<ActionEvent>() {

			
			public void handle(ActionEvent event) {
				System.out.println("Hello World! ");
				System.exit(0);
			}
		});
		btn2.setLayoutX(100);
		btn2.setLayoutY(100);

		Pane root = new Pane();
		
		   root.setPrefWidth(320);
		    root.setPrefHeight(240);
		root.getChildren().add(btn);
		root.getChildren().add(btn2);

		Scene scene = new Scene(root, 300, 250);

		primaryStage.setTitle("Hello World!");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
