import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.*;
import javafx.scene.input.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main extends Application {
    public static final int PORT = 51005;
    public static final String serverIP = "149.56.47.97";
    private InetSocketAddress adrServeur;
    private ServerSocket socServer;
    private Socket socClient;

    private BufferedReader reader;
    Stage window;

    ArrayList<String> coords = new ArrayList<String>();
    ArrayList<String> links = new ArrayList<String>();

    public static void main(String args[]) {
        launch(args);
    }

    // d�finition d'un objet "runnable" qui pourra �tre ex�cut�
    // par le UI Thread
    class changerCouleur implements Runnable {
        @Override
        public void run() {
            //TODO what happens when you click somewhere on map
        }
    }
    //T�che sans fin
    class TacheParallele implements Runnable {
        public void run() {
            while (true) {
                //TODO what happens every second (Refresh)
                /*
                // demande au UI Thread d'ex�cuter ce bout de code
                Platform.runLater(() -> c1.setFill(Color.GREEN));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {}

                // demande au UI Thread d'ex�cuter ce bout de code
                Platform.runLater(() -> c1.setFill(Color.RED));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {} */
            }
        }
    }

    public void gererClic(MouseEvent e) {
        int x = (int)e.getX();
        int y = (int)e.getY();
        System.out.println("Position de la souris: " + x + ", " + y);

        // demande au UI Thread d'ex�cuter ce bout de code
        Platform.runLater(new changerCouleur());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        adrServeur = new InetSocketAddress(serverIP, PORT);
        socServer = new ServerSocket(PORT);
        socClient = new Socket();
        socClient.connect(adrServeur);
        System.out.println("Client connecte.");

        keepServerInfo(socClient);

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

    private void keepServerInfo(Socket socClient){
        try {
            //Lit les information envoyer par le serveur (Info carte)
            reader = new BufferedReader(new InputStreamReader(socClient.getInputStream()));

            Boolean fini = false;

            //Affiche info pour la carte dans la console.
            //TODO prendre les informations pour afficher la carte plutot que d'afficher les valeurs dans la console.
            //Ceci doit etre dans un thread separe qui gere l'interface usage.
            System.out.println("Information pour afficher la carte: ");

            Boolean bLinks = false;

            //Fill arays avec les informations de la carte
            while (!fini) {

                String line = reader.readLine();

                if (!bLinks) {
                    coords.add(line);
                    if (line.equals("")){
                        bLinks = true;
                    }
                }
                else if (bLinks) links.add(line);

                if (line != null) {
                    System.out.println(line);
                } else if (line != null){
                    System.out.println(line);
                } else {
                    fini = true;
                }
            }

            reader.close();
        } catch(IOException io)
        {
            System.err.println("Erreur dans la lecture de la carte: " + io.getMessage());
        }

    }
    // Event clieck on connexion button
    private void logIn(TextField userNameTextField, TextField passwordTextField)
    {
        Group groupe = new Group();
        Scene gameScene = new Scene(groupe,1600,900);

        // assignation d'un gestionaire de clic
        window.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> gererClic(e));
        window.show();

        groupe = initializeMap(groupe);
        // cr�ation d'une t�che parall�le pour ne pas geler le UI Thread
        Thread t = new Thread(new TacheParallele());
        // un thread "d�mon" s'arr�tera avec la fermeture de la fen�tre
        t.setDaemon(true);
        t.start();

        window.setScene(gameScene);
        window.show();

    }

    private Group initializeMap(Group groupe) {

        ArrayList<Circle> circles = new ArrayList<Circle>();

        groupe.getChildren().add(new ImageView(new Image("carte.png"))); //Ajouter carte arriere plan

        for (int i = 0; i < coords.size(); ++i) generateCircle(groupe, i, circles); //Genere les noeud
        for (int i = 0; i < links.size(); ++i) generateLine(groupe, i, circles); //Genere les liaison.

        return groupe;
    }

    private void generateCircle(Group groupe, int index, ArrayList<Circle> circles){
        final int CIRCLE_RADIUS = 20;

        if (!coords.get(index).equals(""))
        {
            Circle currentCircle = new Circle();

            String info[] = coords.get(index).split(" ");
            int x = Integer.parseInt(info[1]);
            int y = Integer.parseInt(info[2]);

            circles.add(new Circle(x, y, CIRCLE_RADIUS));
            currentCircle = circles.get(index);
            currentCircle.setStroke(Color.BLACK);
            currentCircle.setFill(Color.RED);
            currentCircle.setStrokeWidth(5);
            groupe.getChildren().add(currentCircle);
        }

    }

    private void generateLine(Group groupe, int index, ArrayList<Circle> circles){

        if (links.get(index) != null)
        {
            String info[] = links.get(index).split(" ");

            for (int i = 1; i < info.length; ++i){
                double startX = circles.get(index).getCenterX();
                double startY = circles.get(index).getCenterY();
                double endX = circles.get(Integer.parseInt(info[i])).getCenterX();
                double endY = circles.get(Integer.parseInt(info[i])).getCenterY();

                Line currentLine = new Line(startX, startY, endX, endY);
                currentLine.setStroke(Color.RED);
                currentLine.setStrokeWidth(1);
                groupe.getChildren().add(currentLine);
            }

            ;
        }

    }
}
