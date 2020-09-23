import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        primaryStage.setTitle("Simple chat");
        primaryStage.setScene(new Scene(root, 550, 400));
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(400);
//        primaryStage.setOnCloseRequest(event -> {
//            System.exit(0);
//        });
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
