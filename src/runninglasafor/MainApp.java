package runninglasafor;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static final String BUNDLE_NAME = "runninglasafor.resources.messages";
    private static final String ROOT_FXML = "/runninglasafor/views/RootLayout.fxml";

    public enum View { LOGIN, REGISTER, ACTIVITIES, ACCUMULATED, PROFILE, HISTORY }

    private static Locale currentLocale = new Locale("es");
    private static View currentView = View.LOGIN;
    private static boolean lightTheme = false;
    private static Stage primaryStage;

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static boolean isLightTheme() {
        return lightTheme;
    }

    public static void toggleTheme() {
        lightTheme = !lightTheme;
    }

    public static View getCurrentView() {
        return currentView;
    }

    public static void setCurrentView(View v) {
        if (v != null) currentView = v;
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
    }

    public static void changeLocale(Locale newLocale) {
        if (newLocale == null || newLocale.equals(currentLocale) || primaryStage == null) {
            return;
        }
        currentLocale = newLocale;
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(ROOT_FXML), getBundle());
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
            primaryStage.setTitle(getBundle().getString("app.title"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        ResourceBundle bundle = getBundle();
        Parent root = FXMLLoader.load(getClass().getResource(ROOT_FXML), bundle);
        Scene scene = new Scene(root);
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        } catch (Exception ignored) {
        }
        stage.setTitle(bundle.getString("app.title"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
