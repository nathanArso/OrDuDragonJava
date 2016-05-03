import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    Stage window;

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;

        window.setTitle("l'or du dragon");
        Button logInButton = new Button("connexion");
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

        Scene logInScene = new Scene(logInLayout, 300, 100);
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
