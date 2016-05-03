import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main extends Application {
    public static final int PORT = 51005;
    public static final String serverIP = "149.56.47.97";
    private InetSocketAddress adrServeur;
    private ServerSocket socServer;
    private Socket socClient;
    Stage window;

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        adrServeur = new InetSocketAddress(serverIP, PORT);
        socServer = new ServerSocket(PORT);
        socClient = new Socket();
        socClient.connect(adrServeur);
        System.out.println("Client connecte.");

        window = primaryStage;

        window.setTitle("L'or du dragon");
        Button logInButton = new Button("Connexion");
        Label logInLabel = new Label("Connexion");
        TextField userNameTextField = new TextField();
        TextField passwordTextField = new TextField();
        logInButton.setOnAction(e -> logIn(userNameTextField, passwordTextField));

        // LogIn Layout
        VBox logInLayout = new VBox(5);
        logInLayout.getChildren().add(logInLabel);
        logInLayout.getChildren().add(userNameTextField);
        logInLayout.getChildren().add(passwordTextField);
        logInLayout.getChildren().add(logInButton);

        Scene logInScene = new Scene(logInLayout, 300, 150);
        window.setScene(logInScene);
        window.show();
    }
    // Event clieck on connexion button
    private void logIn(TextField userNameTextField, TextField passwordTextField)
    {
        Group groupe = new Group();

        // Un cercle
        Circle cercle = new Circle(100, 100, 20);
        cercle.setStroke(Color.BLACK);
        cercle.setFill(null);
        cercle.setStrokeWidth(1);
        groupe.getChildren().add(cercle);

        // Un rectangle
        Rectangle rectangle = new Rectangle(200, 100, 50, 100);
        rectangle.setStroke(Color.GREEN);
        rectangle.setFill(Color.YELLOW);
        rectangle.setStrokeWidth(3);
        groupe.getChildren().add(rectangle);

        // Une ligne
        Line ligne = new Line(100, 100, 200, 100);
        ligne.setStroke(Color.RED);
        ligne.setStrokeWidth(1);
        groupe.getChildren().add(ligne);

        Scene gameScene = new Scene(groupe,1280,720);
        window.setScene(gameScene);
        window.show();

    }

}
