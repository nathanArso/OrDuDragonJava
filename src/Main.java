import com.sun.org.apache.xpath.internal.operations.Or;
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

import java.sql.*;

import oracle.jdbc.*;
import oracle.jdbc.pool.*;

import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.util.ArrayList;

public class Main extends Application {
    public static final int CIRCLE_RADIUS = 10;
    public static final int DELAI = 1000;
    public static final int PORT_MAP = 51005;
    public static final int PORT_POS = 51006;
    public static final int PORT_GAME = 51007;
    public static final String ServerIP = "149.56.47.97";

    private InetSocketAddress adrPosServer;
    private InetSocketAddress adrGameServer;
    private ServerSocket socServer;

    private Socket socClientPos;
    private Socket socClientGame;

    private BufferedReader posReader;
    private PrintWriter posWriter;
    Stage window;

    ArrayList<String> positions = new ArrayList<String>();

    DataOutputStream PDFos;
    BufferedReader PDFis;

    public int or = 0;
    public int MD = 0;
    public int Doritos = 0;

    public Noeud selectedNoeud;
    public int selectedID;

    String ClientIP;

    Label orLabel = new Label("Or: 0");
    Label mdLabel = new Label("Moutain Dew: 0");
    Label dLabel = new Label("Doritos: 0");

    boolean playerFree = true;
    boolean trollPrison = false;
    boolean goblinPrison = false;

    Button buildButton = new Button("Build");
    Button freeButton = new Button("Free");
    Button quitButton = new Button("Quitter");

    public static Socket socClient;
    private InetSocketAddress adrMapServer;

    public static ArrayList<String> coords = new ArrayList<String>();
    public static ArrayList<String> links = new ArrayList<String>();

    public static ArrayList<Noeud> noeuds = new ArrayList<Noeud>();

    public static Group groupe = new Group();

    public static BufferedReader mapReader;

    Connection conn = null;

    String linePDF = null;
    String line = null;

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
                    //PDFos = new DataOutputStream(socClientGame.getOutputStream());
                    //PDFis = new BufferedReader(new InputStreamReader(socClientGame.getInputStream()));

                    PDFos.writeBytes("GOTO " + selectedID + "\n");
                    PDFos.flush();
                    System.out.println("GOTO " + selectedID);

                    Platform.runLater(new reponse());

            } catch (UnknownHostException e) {
                System.err.println("Hote introuvable.");
            } catch (IOException e) {
                System.err.println("Erreur du socket de jeu.");
            }

        }
    }

    class reponse implements Runnable {
        @Override
        public void run() {
            try {
                linePDF = PDFis.readLine();
                //if(linePDF.equals("OK")) linePDF += " " + PDFis.readLine();
                System.out.println(linePDF);

                if (linePDF.startsWith("P")) {
                    CallableStatement addCapital = conn.prepareCall("{ call JOUEUR.MISEAJOURCAPITALPLUS}");
                    addCapital.executeUpdate();
                } else if (linePDF.startsWith("M")) {
                    CallableStatement addMD = conn.prepareCall("{ call JOUEUR.MISEAJOURMONTDEWPLUS}");
                    addMD.executeUpdate();
                } else if (linePDF.startsWith("D")) {
                    CallableStatement addD = conn.prepareCall("{ call JOUEUR.MISEAJOURDORITOSPLUS}");
                    addD.executeUpdate();
                } else if (linePDF.startsWith("AUB")) {
                    CallableStatement addAUB = conn.prepareCall("{ call JOUEUR.MISAJOURAUBERGEPLUS}");
                    addAUB.executeUpdate();
                } else if (linePDF.startsWith("MAN")) {
                    CallableStatement addMAN = conn.prepareCall("{ call JOUEUR.MISAJOURHOTELPLUS}");
                    addMAN.executeUpdate();
                } else if (linePDF.startsWith("CHA")) {
                    CallableStatement addCHA = conn.prepareCall("{ call JOUEUR.MISAJOURCHATEAUPLUS}");
                    addCHA.executeUpdate();
                } else if (linePDF.startsWith("T")) {
                    trollPrison = true;
                    playerFree = false;
                    System.out.println("Capture par un troll");
                } else if (linePDF.startsWith("G")) {
                    goblinPrison = true;
                    playerFree = false;
                    System.out.println("Capture par un goblin");
                }

                Platform.runLater(new CurrencyUI());
            } catch (SQLException sqe) {
                System.err.println("Erreur dans la modification de la BD: " + sqe);
            } catch (IOException ioe){
                System.err.println("Erreur dans la modification de la BD: " + ioe);
            }
        }
    }

    class CurrencyUI implements Runnable{
        public void run(){
            getOr();
            getDoritos();
            getMD();
            orLabel.setText("Or: " + or);
            mdLabel.setText("Mountain Dew: " + MD);
            dLabel.setText("Doritos: " + Doritos);
        }

        private void getOr() {
            try {
                CallableStatement getOr = conn.prepareCall("{ ? = call JOUEUR.GETCAPITAL}");
                getOr.registerOutParameter(1, Types.INTEGER);
                getOr.execute();
                or = getOr.getInt(1);
            } catch (SQLException sqle) {
                System.err.println("Erreur lors de la lecture du capital dans la BD: " + sqle);
            }
        }

        private void getMD() {
            try {
                CallableStatement getMd = conn.prepareCall("{ ? = call JOUEUR.GETDEW}");
                getMd.registerOutParameter(1, Types.INTEGER);
                getMd.execute();
                MD = getMd.getInt(1);
            } catch (SQLException sqle) {
                System.err.println("Erreur lors de la lecture des mountain dew dans la BD: " + sqle);
            }
        }

        private void getDoritos() {
            try {
                CallableStatement getDoritos = conn.prepareCall("{ ? = call JOUEUR.GETCAPITAL}");
                getDoritos.registerOutParameter(1, Types.INTEGER);
                getDoritos.execute();
                Doritos = getDoritos.getInt(1);
            } catch (SQLException sqle) {
                System.err.println("Erreur lors de l'ajout de capital dans la BD: " + sqle);
            }
        }
    }

    //Tache sans fin
    class TacheParallele implements Runnable {
        public void run() {


            while (true) {
                //(Refresh)
                try {
                    PDFos.writeBytes("NOOP\n"); //Indique au serveur quon est toujours la
                    PDFos.flush();

                    line = posReader.readLine();

                    if (!line.equals("")) {
                        positions.add(line);
                        System.out.println(line);

                        Platform.runLater(new refreshMap());
                    }
                } catch (IOException ioe) {
                    //TODO When game ends.
                    System.out.println("Erreur: " + ioe);
                }
                try {
                    Thread.sleep(DELAI);
                } catch (InterruptedException ie) {
                }
            }
        }
    }
    class refreshMap implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < Main.noeuds.size(); ++i) Main.noeuds.get(i).setFill(Color.BLACK); //set color back to black

            String combs[] = line.split(" ");

            for (int i = 0; i < combs.length; ++i) {
                String comb[] = combs[i].split(":");

                switch (comb[1]) {
                    case "J":
                        //Gérer le joueur.
                        Main.noeuds.get(Integer.parseInt(comb[0])).setFill(Color.BLUE);
                        break;
                    case "T":
                        //Gérer le troll
                        Main.noeuds.get(Integer.parseInt(comb[0])).setFill(Color.RED);
                        break;
                    case "G":
                        //Gérer Gobelin
                        Main.noeuds.get(Integer.parseInt(comb[0])).setFill(Color.DARKRED);
                        break;
                    case "P":
                        //Gérer pièce d'or
                        Main.noeuds.get(Integer.parseInt(comb[0])).setFill(Color.GOLD);
                        break;
                    case "M":
                        //Gérer Mountain Dew
                        Main.noeuds.get(Integer.parseInt(comb[0])).setFill(Color.LIGHTGREEN);
                        break;
                    case "D":
                        //Gerer Doritos
                        Main.noeuds.get(Integer.parseInt(comb[0])).setFill(Color.ORANGE);
                        break;
                    case "A":
                        //Gerer auberges
                        Main.noeuds.get(Integer.parseInt(comb[0])).setFill(Color.MEDIUMPURPLE);
                        break;
                    case "N":
                        //Gerer manoir
                        Main.noeuds.get(Integer.parseInt(comb[0])).setFill(Color.PURPLE);
                        break;
                    case "C":
                        //Gerer chateau
                        Main.noeuds.get(Integer.parseInt(comb[0])).setFill(Color.BLUEVIOLET);
                        break;
                }
            }
        }
    }

    public void gererClic(MouseEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        System.out.println("Position de la souris: " + x + ", " + y);

        for (int i = 0; i < noeuds.size(); i++) {
            if (noeuds.get(i).x < x + CIRCLE_RADIUS && noeuds.get(i).x > x - CIRCLE_RADIUS && noeuds.get(i).y < y + CIRCLE_RADIUS && noeuds.get(i).y > y - CIRCLE_RADIUS) {
                selectedID = i;
                selectedNoeud = noeuds.get(i);
                Platform.runLater(new deplacer());
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

       // socServer = new ServerSocket(PORT_MAP);

        System.out.println("Client connecte.");

        adrPosServer = new InetSocketAddress(ServerIP, PORT_POS);
        adrGameServer = new InetSocketAddress(ServerIP, PORT_GAME);
        adrMapServer = new InetSocketAddress(Main.ServerIP, Main.PORT_MAP);
        socClient = new Socket();

        socClient.connect(adrMapServer);
        Platform.runLater(new MapServer());

        window = primaryStage;

        //Declarations
        window.setTitle("L'or du dragon");
        Button logInButton = new Button("Connexion");
        Label titleLabel = new Label("L'or du dragon");
        Label usernameLabel = new Label("Username:");
        TextField userNameTextField = new TextField();
        userNameTextField.setText("ATTG");
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("GTTA");
        logInButton.setOnAction(e -> Platform.runLater(new logIn()));

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

    class connection implements Runnable {
        String url = "jdbc:oracle:thin:@205.237.244.251:1521:orcl";

        public void run(){
            try {
                OracleDataSource ods = new OracleDataSource();

                ods.setURL(url);
                ods.setUser("ATTG");
                ods.setPassword("GTTA");
                conn = ods.getConnection();
            } catch (SQLException sqle) {
                System.err.println("Erreur dans la connexion: " + sqle.getMessage());
            }
        }
    }


    // Event clieck on connexion button
    class logIn implements Runnable{

        @Override
        public void run() {
            Platform.runLater(new connection());

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

                PDFos = new DataOutputStream(socClientGame.getOutputStream());
                PDFis = new BufferedReader(new InputStreamReader(socClientGame.getInputStream()));
                posReader = new BufferedReader(new InputStreamReader(socClientPos.getInputStream()));
                posWriter = new PrintWriter(new OutputStreamWriter(socClientPos.getOutputStream()));
                ClientIP = socClientGame.getLocalAddress().getHostAddress();
                System.out.println("IP: " + ClientIP);
            } catch (IOException io) {
                System.err.println("Erreur création socket position: " + io.getMessage());
            }

            try {

                PDFos.writeBytes("HELLO zATTG " + ClientIP + "\n");
                PDFos.flush();
                System.out.println(posReader.readLine());
            } catch (IOException io) {
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
    }

    private Group initializeMap() {

        orLabel.setLayoutX(1000);
        orLabel.setLayoutY(820);
        orLabel.setFont(new Font(30));
        orLabel.setText("Or: " + or);

        mdLabel.setLayoutX(1120);
        mdLabel.setLayoutY(820);
        mdLabel.setFont(new Font(30));
        mdLabel.setText("Mountain Dew: " + MD);

        dLabel.setLayoutX(1400);
        dLabel.setLayoutY(820);
        dLabel.setFont(new Font(30));
        dLabel.setText("Doritos: " + Doritos);

        Platform.runLater(new Quit());
        Platform.runLater(new Free());
        Platform.runLater(new Build());

        groupe.getChildren().add(new ImageView(new Image("http://prog101.com/travaux/dragon/images/nowhereland.png"))); //Ajouter carte arriere plan
        groupe.getChildren().add(quitButton);
        groupe.getChildren().add(buildButton);
        groupe.getChildren().add(freeButton);
        groupe.getChildren().add(orLabel);
        groupe.getChildren().add(mdLabel);
        groupe.getChildren().add(dLabel);

        for (int i = 0; i < coords.size(); ++i) MapServer.generateCircle(i); //Genere les noeud
        for (int i = 0; i < links.size(); ++i) MapServer.generateLine(i); //Genere les liaison.

        return groupe;
    }

    class Quit implements Runnable {
        @Override
        public void run() {
            quitButton.setLayoutX(10);
            quitButton.setLayoutY(10);

            quitButton.setOnAction(e -> {
                try {
                    conn.close();
                    //Reset currencies in BD
                    PDFos.writeBytes("QUIT\n");
                    window.close();
                } catch (SQLException sqle) {
                    System.err.println("Erreur lors de la fermeture de la connection a la BD: " + sqle);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        }
    }

    class Free implements Runnable {
        @Override
        public void run() {
            freeButton.setLayoutX(200);
            freeButton.setLayoutY(10);

            freeButton.setOnAction(e -> {
                try {
                    if (trollPrison && MD > 0) {
                        removeMD();
                        PDFos.writeBytes("FREE\n");
                        playerFree = true;
                        trollPrison = false;
                    } else if (goblinPrison && Doritos > 0) {
                        removeDoritos();
                        PDFos.writeBytes("FREE\n");
                        playerFree = true;
                        goblinPrison = false;
                    } else if ((goblinPrison || trollPrison) && or >= 3) {
                        removeOr();
                        removeOr();
                        removeOr();
                        PDFos.writeBytes("FREE\n");
                        playerFree = true;
                        trollPrison = false;
                        goblinPrison = false;
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        }

        private void removeOr() {
            try {
                CallableStatement addCapital = conn.prepareCall("{ call JOUEUR.MISEAJOURCAPITALMOINS}");
                addCapital.executeUpdate();
            } catch (SQLException sqle) {
                System.err.println("Erreur lors de la reduction du capital dans la BD: " + sqle);
            }
            orLabel.setText("Or: " + or);
        }

        private void removeMD() {
            try {
                CallableStatement removeMD = conn.prepareCall("{ call JOUEUR.MISEAJOURMONTDEWMOINS}");
                removeMD.executeUpdate();
            } catch (SQLException sqle) {
                System.err.println("Erreur lors de la reduction de mountain dew dans la BD: " + sqle);
            }
            mdLabel.setText("Mountain Dew: " + MD);
        }

        private void removeDoritos() {
            try {
                CallableStatement removeD = conn.prepareCall("{ call JOUEUR.MISEAJOURDORITOSMOINS}");
                removeD.executeUpdate();
            } catch (SQLException sqle) {
                System.err.println("Erreur lors de la reduction des doritos dans la BD: " + sqle);
            }
            dLabel.setText("Doritos: " + Doritos);
        }
    }

    class Build implements Runnable {

        public void run(){
            buildButton.setLayoutX(100);
            buildButton.setLayoutY(10);

            buildButton.setOnAction(e -> {
                try {
                    if (or >= 3) {
                        removeOr();
                        removeOr();
                        removeOr();
                        PDFos.writeBytes("BUILD\n");
                        PDFis = new BufferedReader(new InputStreamReader(socClientGame.getInputStream()));
                        System.out.println(PDFis.readLine());
                    } else {
                        System.out.println("Pas asser d'or pour construire!");
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        }

        private void removeOr() {
            try {
                CallableStatement addCapital = conn.prepareCall("{ call JOUEUR.MISEAJOURCAPITALMOINS}");
                addCapital.executeUpdate();
            } catch (SQLException sqle) {
                System.err.println("Erreur lors de la reduction du capital dans la BD: " + sqle);
            }
            orLabel.setText("Or: " + or);
        }
    }
}
