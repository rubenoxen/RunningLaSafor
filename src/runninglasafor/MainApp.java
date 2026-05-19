package runninglasafor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/runninglasafor/views/RootLayout.fxml"));
        Scene scene = new Scene(root);
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        } catch (Exception ignored) {
        }
        stage.setTitle("Running la Safor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
