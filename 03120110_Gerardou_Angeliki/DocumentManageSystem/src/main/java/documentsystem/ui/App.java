package documentsystem.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        AppContext ctx = new AppContext();

        stage.setOnCloseRequest(e -> {
            ctx.saveAll();
        });

        stage.setTitle("MediaLab Documents - Login");
        stage.setScene(new LoginView(ctx, stage).build());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}