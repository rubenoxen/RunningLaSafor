package runninglasafor.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class RootLayoutController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showLogin();
    }

    public void showLogin() {
        LoginController c = loadCenter("/runninglasafor/views/Login.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showRegister() {
        RegisterController c = loadCenter("/runninglasafor/views/Register.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showHome() {
        Label placeholder = new Label("Bienvenido. Pantalla principal pendiente.");
        placeholder.setStyle("-fx-font-size: 16; -fx-text-fill: #555;");
        StackPane wrapper = new StackPane(placeholder);
        rootPane.setCenter(wrapper);
    }

    public void showProfile() {
        showHome();
    }

    public void showHistory() {
        showHome();
    }

    public void showActivities() {
        showHome();
    }

    private <T> T loadCenter(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            rootPane.setCenter(view);
            return loader.getController();
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "No se ha podido cargar la vista: " + fxmlPath);
            a.showAndWait();
            return null;
        }
    }
}
