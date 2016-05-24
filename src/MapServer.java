import javafx.scene.Group;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by User on 23/05/2016.
 */

class MapServer implements Runnable {

    public void run() {
        try {

            //Lit les information envoyer par le serveur (Info carte)
            Main.mapReader = new BufferedReader(new InputStreamReader(Main.socClient.getInputStream()));

            Boolean fini = false;

            Boolean bLinks = false;

            //Fill arays avec les informations de la carte
            while (!fini) {

                String line = Main.mapReader.readLine();

                if (!bLinks) {
                    Main.coords.add(line);
                    if (line.equals("")) {
                        bLinks = true;
                    }
                } else if (bLinks) Main.links.add(line);

                if (line != null) {
                    System.out.println(line);
                } else if (line != null) {
                    System.out.println(line);
                } else {
                    fini = true;
                }
            }

            Main.mapReader.close();
        } catch (IOException io) {
            System.err.println("Erreur dans la lecture de la carte: " + io.getMessage());
        }
    }

    public static void generateCircle(int index) {

        if (!Main.coords.get(index).equals("")) {
            Noeud currentCircle = new Noeud();

            String info[] = Main.coords.get(index).split(" ");
            int x = Integer.parseInt(info[1]);
            int y = Integer.parseInt(info[2]);

            Main.noeuds.add(new Noeud(x, y, Main.CIRCLE_RADIUS));
            currentCircle = Main.noeuds.get(index);
            currentCircle.setStroke(Color.AZURE);
            currentCircle.setFill(Color.BLACK);
            currentCircle.setStrokeWidth(2);
            Main.groupe.getChildren().add(currentCircle);
        }

    }

    public static void generateLine(int index) {

        if (Main.links.get(index) != null) {
            String info[] = Main.links.get(index).split(" ");

            for (int i = 1; i < info.length; ++i) {
                double startX = Main.noeuds.get(index).getCenterX();
                double startY = Main.noeuds.get(index).getCenterY();
                double endX = Main.noeuds.get(Integer.parseInt(info[i])).getCenterX();
                double endY = Main.noeuds.get(Integer.parseInt(info[i])).getCenterY();

                Chemin currentLine = new Chemin(startX, startY, endX, endY);
                currentLine.setStroke(Color.BLACK);
                currentLine.setStrokeWidth(1);
                Main.groupe.getChildren().add(currentLine);
            }

        }

    }

}
