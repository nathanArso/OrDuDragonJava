import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main extends Application {
    public static final int PORT = 51005;
    public static final String serverIP = "149.56.47.97";
    private InetSocketAddress adrServeur;
    private ServerSocket socServer;
    private Socket socClient;

    private BufferedReader reader;
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

        String info = null;
        //Lit les information envoyer par le serveur (Info carte)
        reader = new BufferedReader(new InputStreamReader(socClient.getInputStream()));

        Boolean fini = false;

        //Affiche info pour la carte dans la console.
        //TODO prendre les informations pour afficher la carte plutot que d'afficher les valeurs dans la console.
        //Ceci doit etre dans un thread separe qui gere l'interface usage.
        System.out.println("Information pour afficher la carte: ");
        while (!fini) {
            info = reader.readLine();

            if (info != null) {
                System.out.println(info);
            } else {
                fini = true;
            }
        }

        reader.close();
        window = primaryStage;

        //Declarations
        window.setTitle("L'or du dragon");
        Button logInButton = new Button("Connexion");
        Label titleLabel = new Label("L'or du dragon");
        Label usernameLabel = new Label("Username:");
        TextField userNameTextField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        logInButton.setOnAction(e -> logIn(userNameTextField, passwordField));

        //Format page.
        primaryStage.centerOnScreen();
        usernameLabel.setFont(new Font(10));
        passwordLabel.setFont(new Font(10));
        userNameTextField.setPromptText("Your username");
        passwordField.setPromptText("Your password");

        //Title label.
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        titleLabel.setTextFill(Color.GREEN); //Text color.
        titleLabel.setWrapText(true);

        // LogIn Layout
        VBox logInLayout = new VBox(5);
        logInLayout.getChildren().add(titleLabel);
        logInLayout.getChildren().add(usernameLabel);
        logInLayout.getChildren().add(userNameTextField);
        logInLayout.getChildren().add(passwordLabel);
        logInLayout.getChildren().add(passwordField);
        logInLayout.getChildren().add(logInButton);

        Scene logInScene = new Scene(logInLayout, 300, 180);
        window.setScene(logInScene);
        window.show();
    }
    // Event clieck on connexion button
    private void logIn(TextField userNameTextField, TextField passwordTextField)
    {
        Group groupe = new Group();
        Scene gameScene = new Scene(groupe,1280,720);
        window.setScene(gameScene);
        window.show();

    }

}
