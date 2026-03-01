package documentsystem.ui;

import documentsystem.domain.SimpleUser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {

    private final AppContext ctx;
    private final Stage stage;

    public LoginView(AppContext ctx, Stage stage) {
        this.ctx = ctx;
        this.stage = stage;
    }

    public Scene build() {
        Label title = new Label("MediaLab Documents");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button loginBtn = new Button("Login");
        loginBtn.setDefaultButton(true);

        loginBtn.setOnAction(e -> {
            String u = usernameField.getText() == null ? "" : usernameField.getText().trim();
            String p = passwordField.getText() == null ? "" : passwordField.getText().trim();

            SimpleUser user = ctx.authManager.login(u, p);
            if (user == null) {
                errorLabel.setText("Wrong credentials.");
                return;
            }

            ctx.currentUser = user;

            // popup ενημέρωσης για updated watched docs (όπως ζητάει η εκφώνηση)
            var updated = ctx.subscriptionManager.getUpdatedSubscriptions(user);
            if (!updated.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Updates");
                alert.setHeaderText("Updated watched documents");
                alert.setContentText(updated.toString());
                alert.showAndWait();
            }

            //goes to MainView
            stage.setTitle("MediaLab Documents");
            stage.setScene(new MainView(ctx, stage).build());
            stage.setMaximized(true);
        });

        VBox root = new VBox(10, title, usernameField, passwordField, loginBtn, errorLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        return new Scene(root, 420, 260);
    }
}