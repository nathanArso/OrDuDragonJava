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
import java.net.*;
import java.util.ArrayList;

public class Main extends Application {
    public static final int CIRCLE_RADIUS = 10;
    public static final int PORT_MAP = 51005;
    public static final int PORT_POS = 51006;
    public static final int PORT_GAME = 51007;
    public static final int DELAI = 1000;
    public static final String ServerIP = "149.56.47.97";
    private InetSocketAddress adrMapServer;
    private InetSocketAddress adrPosServer;
    private InetSocketAddress adrGameServer;
    private ServerSocket socServer;
    private Socket socClient;
    private Socket socClientPos;
    private Socket socClientGame;

    private ArrayList<Noeud> noeuds = new ArrayList<Noeud>();
    private Group groupe = new Group();

    private BufferedReader mapReader;
    private BufferedReader posReader;
    private PrintWriter posWriter;
    Stage window;

    ArrayList<String> coords = new ArrayList<String>();
    ArrayList<String> links = new ArrayList<String>();

    ArrayList<String> positions = new ArrayList<String>();

    DataOutputStream PDFos;
    DataInputStream PDFis;

    public Noeud selectedNoeud;
    public int selectedID;

    public static void main(String args[]) {
        launch(args);
    }

    // definition d'un objet "runnable" qui pourra etre execute
    // par le UI Thread
    class deplacer implements Runnable {
        @Override
        public void run() {
            try {
                //TODO to complete
                PDFos = new DataOutputStream(socClientGame.getOutputStream());
                PDFis = new DataInputStream(socClientGame.getInputStream());

                PDFos.writeBytes("GOTO " + selectedID + "\n");

                System.out.println("Deplacement au noeud " + selectedID);

            }catch (UnknownHostException e){
                System.err.println("Hote introuvable.");
            }catch (IOException e){
                System.err.println("Erreur du socket de jeu.");
            }

        }
    }

    //Tache sans fin
    class TacheParallele implements Runnable {
        public void run() {
            String line = null;

            while (true) {
                //What happens every second (Refresh)
                try {
                    PDFos = new DataOutputStream(socClientGame.getOutputStream());
                    PDFos.writeBytes("NOOP\n"); //Indique au serveur quon est toujours la

                    posReader = new BufferedReader(new InputStreamReader(socClientPos.getInputStream()));
                    posWriter = new PrintWriter(new OutputStreamWriter(socClientPos.getOutputStream()));

                    line = posReader.readLine();

                    if (!line.equals("")) {
                        positions.add(line);
                        System.out.println(line);

                        refreshMap(line);
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

    private void refreshMap(String line){
        for (int i = 0; i < noeuds.size(); ++i) noeuds.get(i).setFill(Color.BLACK); //set color back to black

        String combs[] = line.split(" ");

        for (int i = 0; i < combs.length; ++i){
            String comb[] = combs[i].split(":");

            switch(comb[1]){
                case "J":
                    //Gérer le joueur.
                    noeuds.get(Integer.parseInt(comb[0])).setFill(Color.BLUE);
                    break;
                case "T":
                    //Gérer le troll
                    noeuds.get(Integer.parseInt(comb[0])).setFill(Color.RED);
                    break;
                case "G":
                    //Gérer Gobelin
                    noeuds.get(Integer.parseInt(comb[0])).setFill(Color.DARKRED);
                    break;
                case "P":
                    //Gérer pièce d'or
                    noeuds.get(Integer.parseInt(comb[0])).setFill(Color.GOLD);
                    break;
                case "M":
                    //Gérer Mountain Dew
                    noeuds.get(Integer.parseInt(comb[0])).setFill(Color.LIGHTGREEN);
                    break;
                case "D":
                    //Gerer Doritos
                    noeuds.get(Integer.parseInt(comb[0])).setFill(Color.ORANGE);
                    break;
                case "A":
                    //Gerer auberges
                    noeuds.get(Integer.parseInt(comb[0])).setFill(Color.MEDIUMPURPLE);
                    break;
                case "N":
                    //Gerer manoir
                    noeuds.get(Integer.parseInt(comb[0])).setFill(Color.PURPLE);
                    break;
                case "C":
                    //Gerer chateau
                    noeuds.get(Integer.parseInt(comb[0])).setFill(Color.BLUEVIOLET);
                    break;
            }
        }
    }

    public void gererClic(MouseEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        System.out.println("Position de la souris: " + x + ", " + y);

        for (int i = 0; i < noeuds.size(); i++){
            if (noeuds.get(i).x < x + CIRCLE_RADIUS && noeuds.get(i).x > x - CIRCLE_RADIUS && noeuds.get(i).y < y + CIRCLE_RADIUS && noeuds.get(i).y > y - CIRCLE_RADIUS ){
                selectedID = i;
                selectedNoeud = noeuds.get(i);
                Platform.runLater(new deplacer());
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        adrMapServer = new InetSocketAddress(ServerIP, PORT_MAP);
        socServer = new ServerSocket(PORT_MAP);
        socClient = new Socket();
        socClient.connect(adrMapServer);
        System.out.println("Client connecte.");

        adrPosServer = new InetSocketAddress(ServerIP, PORT_POS);
        adrGameServer = new InetSocketAddress(ServerIP, PORT_GAME);

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

        Scene gameScene = new Scene(groupe, 1600, 900);

        // assignation d'un gestionaire de clic
        window.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> gererClic(e));
        window.show();

        initializeMap();

        try {
            socClientPos = new Socket();
            socClientPos.connect(adrPosServer);
            socClientGame = new Socket();
            socClientGame.connect(adrGameServer);
        } catch (IOException io) {
            System.err.println("Erreur création socket position: " + io.getMessage());
        }

        try {
            PDFos = new DataOutputStream(socClientGame.getOutputStream());
            PDFos.writeBytes("HELLO zATTG\n");
        } catch(IOException io){
            System.err.println(io.getMessage());
        }

        // creation d'une tache parallele pour ne pas geler le UI Thread
        Thread t = new Thread(new TacheParallele());
        // un thread "daemon" s'arretera avec la fermeture de la fenetre
        t.setDaemon(true);
        t.start();

        window.setScene(gameScene);
        window.show();

    }

    private Group initializeMap() {

        groupe.getChildren().add(new ImageView(new Image("http://prog101.com/travaux/dragon/images/nowhereland.png"))); //Ajouter carte arriere plan

        Button quitButton = new Button("Quitter");
        quitButton.setLayoutX(10);
        quitButton.setLayoutY(10);

            quitButton.setOnAction(e -> {
                try {
                    PDFos.writeBytes("QUIT\n");
                    window.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });

        groupe.getChildren().add(quitButton);

        Button buildButton = new Button("Build");
        buildButton.setLayoutX(100);
        buildButton.setLayoutY(10);

        buildButton.setOnAction(e -> {
            try {
                PDFos.writeBytes("BUILD\n");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        groupe.getChildren().add(buildButton);
        for (int i = 0; i < coords.size(); ++i) generateCircle(i); //Genere les noeud
        for (int i = 0; i < links.size(); ++i) generateLine(i); //Genere les liaison.

        return groupe;
    }


    private void generateCircle(int index) {

        if (!coords.get(index).equals("")) {
            Noeud currentCircle = new Noeud();

            String info[] = coords.get(index).split(" ");
            int x = Integer.parseInt(info[1]);
            int y = Integer.parseInt(info[2]);

            noeuds.add(new Noeud(x, y, CIRCLE_RADIUS));
            currentCircle = noeuds.get(index);
            currentCircle.setStroke(Color.AZURE);
            currentCircle.setFill(Color.BLACK);
            currentCircle.setStrokeWidth(2);
            groupe.getChildren().add(currentCircle);
        }

    }

    private void generateLine(int index) {

        if (links.get(index) != null) {
            String info[] = links.get(index).split(" ");

            for (int i = 1; i < info.length; ++i) {
                double startX = noeuds.get(index).getCenterX();
                double startY = noeuds.get(index).getCenterY();
                double endX = noeuds.get(Integer.parseInt(info[i])).getCenterX();
                double endY = noeuds.get(Integer.parseInt(info[i])).getCenterY();

                Chemin currentLine = new Chemin(startX, startY, endX, endY);
                currentLine.setStroke(Color.BLACK);
                currentLine.setStrokeWidth(1);
                groupe.getChildren().add(currentLine);
            }

        }

    }
}
