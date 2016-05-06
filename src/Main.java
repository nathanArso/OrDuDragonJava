import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Main extends Application {
    public static final int PORT_MAP = 51005;
    public static final int PORT_POS = 51006;
    public static final int DELAI = 1000;
    public static final String ServerIP = "149.56.47.97";
    private InetSocketAddress adrMapServer;
    private InetSocketAddress adrPosServer;
    private ServerSocket socServer;
    private Socket socClient;
    private Socket socClientPos;

    private BufferedReader mapReader;
    private BufferedReader posReader;
    private PrintWriter posWriter;
    Stage window;

    ArrayList<String> coords = new ArrayList<String>();
    ArrayList<String> links = new ArrayList<String>();

    ArrayList<String> positions = new ArrayList<String>();

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

    //Tache sans fin
    class TacheParallele implements Runnable {
        public void run() {
            String line = null;

            while (true) {
                //What happens every second (Refresh)
                try {
                    posReader = new BufferedReader(new InputStreamReader(socClientPos.getInputStream()));
                    posWriter = new PrintWriter(new OutputStreamWriter(socClientPos.getOutputStream()));

                    line = posReader.readLine();

                    if (!line.equals("")) {
                        positions.add(line);
                        System.out.println(line);
                    }
                } catch (IOException ioe) {
                    System.out.println("Erreur: " + ioe);
                }

                // demande au UI Thread d'executer ce bout de code
                Platform.runLater(() -> posWriter.write("")); //Envoye une ligne pour idniquer que nous sommes vivant
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    public void gererClic(MouseEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        System.out.println("Position de la souris: " + x + ", " + y);

        // demande au UI Thread d'executer ce bout de code
        Platform.runLater(new changerCouleur());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        adrMapServer = new InetSocketAddress(ServerIP, PORT_MAP);
        socServer = new ServerSocket(PORT_MAP);
        socClient = new Socket();
        socClient.connect(adrMapServer);
        System.out.println("Client connecte.");

        adrPosServer = new InetSocketAddress(ServerIP, PORT_POS);

        keepMapServerInfo();

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

    private void keepMapServerInfo() {
        try {
            //Lit les information envoyer par le serveur (Info carte)
            mapReader = new BufferedReader(new InputStreamReader(socClient.getInputStream()));

            Boolean fini = false;

            //TODO Ceci doit etre dans un thread separe qui gere l'interface usage.
            System.out.println("Information pour afficher la carte: ");

            Boolean bLinks = false;

            //Fill arays avec les informations de la carte
            while (!fini) {

                String line = mapReader.readLine();

                if (!bLinks) {
                    coords.add(line);
                    if (line.equals("")) {
                        bLinks = true;
                    }
                } else if (bLinks) links.add(line);

                if (line != null) {
                    System.out.println(line);
                } else if (line != null) {
                    System.out.println(line);
                } else {
                    fini = true;
                }
            }

            mapReader.close();
        } catch (IOException io) {
            System.err.println("Erreur dans la lecture de la carte: " + io.getMessage());
        }

    }

    // Event clieck on connexion button
    private void logIn(TextField userNameTextField, TextField passwordTextField) {

        Group groupe = new Group();
        Scene gameScene = new Scene(groupe, 1600, 900);

        // assignation d'un gestionaire de clic
        window.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> gererClic(e));
        window.show();

        groupe = initializeMap(groupe);

        try {
            socClientPos = new Socket();
            socClientPos.connect(adrPosServer);
        } catch (IOException io) {
            System.err.println("Erreur création socket position: " + io.getMessage());
        }


        // creation d'une tache parallele pour ne pas geler le UI Thread
        Thread t = new Thread(new TacheParallele());
        // un thread "daemon" s'arretera avec la fermeture de la fenetre
        t.setDaemon(true);
        t.start();

        window.setScene(gameScene);
        window.show();

    }

    private Group initializeMap(Group groupe) {

        ArrayList<Noeud> noeuds = new ArrayList<Noeud>();

        groupe.getChildren().add(new ImageView(new Image("carte.png"))); //Ajouter carte arriere plan

        for (int i = 0; i < coords.size(); ++i) generateCircle(groupe, i, noeuds); //Genere les noeud
        for (int i = 0; i < links.size(); ++i) generateLine(groupe, i, noeuds); //Genere les liaison.

        return groupe;
    }

    private void generateCircle(Group groupe, int index, ArrayList<Noeud> noeuds) {
        final int CIRCLE_RADIUS = 10;

        if (!coords.get(index).equals("")) {
            Noeud currentCircle = new Noeud();

            String info[] = coords.get(index).split(" ");
            int x = Integer.parseInt(info[1]);
            int y = Integer.parseInt(info[2]);

            noeuds.add(new Noeud(x, y, CIRCLE_RADIUS));
            currentCircle = noeuds.get(index);
            currentCircle.setStroke(Color.BLACK);
            currentCircle.setFill(Color.RED);
            currentCircle.setStrokeWidth(2);
            groupe.getChildren().add(currentCircle);
        }

    }

    private void generateLine(Group groupe, int index, ArrayList<Noeud> noeuds) {

        if (links.get(index) != null) {
            String info[] = links.get(index).split(" ");

            for (int i = 1; i < info.length; ++i) {
                double startX = noeuds.get(index).getCenterX();
                double startY = noeuds.get(index).getCenterY();
                double endX = noeuds.get(Integer.parseInt(info[i])).getCenterX();
                double endY = noeuds.get(Integer.parseInt(info[i])).getCenterY();

                Chemin currentLine = new Chemin(startX, startY, endX, endY);
                currentLine.setStroke(Color.RED);
                currentLine.setStrokeWidth(1);
                groupe.getChildren().add(currentLine);
            }

        }

    }
}
