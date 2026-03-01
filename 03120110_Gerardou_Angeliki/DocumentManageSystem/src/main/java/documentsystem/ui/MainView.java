package documentsystem.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import documentsystem.domain.Document;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;

public class MainView {

    private final AppContext ctx;
    private final Stage stage;

    public MainView(AppContext ctx, Stage stage) {
        this.ctx = ctx;
        this.stage = stage;
    }

    public Scene build() {

        javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();

        //  LEFT: Stats Panel
        VBox statsBox = new VBox(15);
        statsBox.setPadding(new Insets(15));
        statsBox.setAlignment(Pos.TOP_LEFT);

        Label helloLabel = new Label("Hello " + ctx.currentUser.getUsername() + "!");
        helloLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label title = new Label("System Info");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label categoriesLabel = new Label();
        categoriesLabel.setStyle("-fx-font-size: 16px;");

        Label documentsLabel = new Label();
        documentsLabel.setStyle("-fx-font-size: 16px;");

        Label watchedLabel = new Label();
        watchedLabel.setStyle("-fx-font-size: 16px;");

        updateStats(categoriesLabel, documentsLabel, watchedLabel);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button logoutBtn = new Button("Exit");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        logoutBtn.setOnAction(e -> {
            ctx.saveAll();
            ctx.currentUser = null;
            stage.setScene(new LoginView(ctx, stage).build());
        });

        statsBox.getChildren().addAll(
                helloLabel,
                title,
                categoriesLabel,
                documentsLabel,
                watchedLabel,
                spacer,
                logoutBtn
        );


        //  RIGHT: Actions Panel
        VBox actionsBox = new VBox(15);
        actionsBox.setPadding(new Insets(20));
        actionsBox.setAlignment(Pos.TOP_CENTER);
        actionsBox.setStyle("-fx-background-color: #f0f0f0; -fx-font-size: 17px;");

        Label actionsTitle = new Label("Actions");
        actionsTitle.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");

        Button listDocsBtn = new Button("Accessible Documents");
        Button createDocBtn = new Button("Create Document");
        Button mySubsBtn = new Button("My Subscriptions");
        mySubsBtn.setMaxWidth(Double.MAX_VALUE);
        createDocBtn.setMaxWidth(Double.MAX_VALUE);
        Button searchDocsBtn = new Button("Search Documents");
        searchDocsBtn.setMaxWidth(Double.MAX_VALUE);

        Button usersBtn = new Button("Users");
        usersBtn.setMaxWidth(Double.MAX_VALUE);
        Button categoriesBtn = new Button("Categories");
        categoriesBtn.setMaxWidth(Double.MAX_VALUE);

        boolean isAdminUser = ctx.currentUser instanceof documentsystem.domain.Admin;
        usersBtn.setVisible(isAdminUser);
        usersBtn.setManaged(isAdminUser);
        categoriesBtn.setVisible(isAdminUser);
        categoriesBtn.setManaged(isAdminUser);

        boolean canCreate = (ctx.currentUser instanceof documentsystem.domain.Admin)
                || (ctx.currentUser instanceof documentsystem.domain.Author);

        createDocBtn.setVisible(canCreate);
        createDocBtn.setManaged(canCreate);
        listDocsBtn.setMaxWidth(Double.MAX_VALUE);
        ListView<Object> docsList = new ListView<>();
        ListView<Document> subsList = new ListView<>();


        subsList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Document doc, boolean empty) {
                super.updateItem(doc, empty);
                if (empty || doc == null) {
                    setText(null);
                } else {
                    setText(doc.getTitle() + " (" + categoryLabel(doc.getCategoryId()) + ")");
                }
            }
        });

        Button unsubscribeBtn = new Button("Unsubscribe");
        unsubscribeBtn.setDisable(true);

        HBox subsBar = new HBox(10, unsubscribeBtn);
        subsBar.setAlignment(Pos.CENTER_LEFT);

        // ===== GROUP: Search Panel =====
        TextField titleQueryField = new TextField();
        titleQueryField.setPromptText("Title contains...");

        TextField authorQueryField = new TextField();
        authorQueryField.setPromptText("Author contains...");

        ComboBox<String> categoryFilterBox = new ComboBox<>();
        categoryFilterBox.setMaxWidth(Double.MAX_VALUE);
        categoryFilterBox.getItems().add(""); // σημαίνει "όλες"
        java.util.List<String> allowedCats2 = new java.util.ArrayList<>();
        for (String catId : ctx.currentUser.getAllowedCategories()) {
            if (ctx.state.getCategories().containsKey(catId)) allowedCats2.add(catId);
        }
        allowedCats2.sort(String.CASE_INSENSITIVE_ORDER);
        categoryFilterBox.getItems().addAll(allowedCats2);
        categoryFilterBox.getSelectionModel().select(0);

        Button runSearchBtn = new Button("Search");

        ListView<Document> resultsList = new ListView<>();
        resultsList.setPrefHeight(140);
        resultsList.setMaxHeight(160);
        resultsList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Document doc, boolean empty) {
                super.updateItem(doc, empty);
                if (empty || doc == null) {setText(null);
                    return;}
                setText(doc.getTitle() + " | " + doc.getAuthorUsername()
                        + " | " + categoryLabel(doc.getCategoryId())
                        + " | v" + doc.getLatestVersionNumber());
            }
        });

        // reuse viewer controls
        Label searchContentTitle = new Label();
        searchContentTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label searchContentVersion = new Label();

        TextArea searchContentArea = new TextArea();
        searchContentArea.setWrapText(true);
        searchContentArea.setEditable(false);
        searchContentArea.setPrefHeight(300);
        VBox.setVgrow(resultsList, Priority.NEVER);
        VBox.setVgrow(searchContentArea, Priority.ALWAYS);

        // Subscribe button
        Button searchSubscribeBtn = new Button("Subscribe");
        searchSubscribeBtn.setDisable(true);

        HBox searchBar = new HBox(10, runSearchBtn, searchSubscribeBtn);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        VBox searchPanel = new VBox(10,
                new Label("Search Filters"),
                categoryFilterBox,
                titleQueryField,
                authorQueryField,
                searchBar,
                new Label("Results"),
                resultsList,
                searchContentTitle,
                searchContentVersion,
                searchContentArea
        );
        searchPanel.setVisible(false);
        searchPanel.setManaged(false);


        // ===== GROUP: Subscriptions Panel =====
        VBox subsPanel = new VBox(10,
                subsList,
                subsBar
        );
        subsPanel.setVisible(false);
        subsPanel.setManaged(false);

        docsList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    setDisable(false);
                    return;
                }

                if (item instanceof String) { // category header
                    setText((String) item);
                    setStyle("-fx-font-weight: bold; -fx-background-color: #e8e8e8;");
                    setDisable(true); // μη clickable
                } else if (item instanceof Document doc) {
                    setText("  " + doc.getTitle()); // indent
                    setStyle("");
                    setDisable(false);
                }
            }
        });

        Label contentTitle = new Label();
        contentTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label contentVersion = new Label();
        ComboBox<Integer> versionPicker = new ComboBox<>();
        versionPicker.setVisible(false);
        versionPicker.setManaged(false);

        javafx.scene.control.TextArea contentArea = new javafx.scene.control.TextArea();
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setPrefHeight(200);

        Button editBtn = new Button("Edit");
        Button saveBtn = new Button("Save (new version)");
        Button cancelBtn = new Button("Cancel");
        Button deleteBtn = new Button("Delete");
        Button subscribeBtn = new Button("Subscribe");


        saveBtn.setDisable(true);
        cancelBtn.setDisable(true);
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);
        subscribeBtn.setDisable(true);

        HBox editBar = new HBox(10, editBtn, saveBtn, cancelBtn, deleteBtn, subscribeBtn);
        editBar.setAlignment(Pos.CENTER_LEFT);

        ListView<documentsystem.domain.SimpleUser> usersList = new ListView<>();

        //users panel
        usersList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(documentsystem.domain.SimpleUser u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) { setText(null); return; }

                String type = "SIMPLE";
                if (u instanceof documentsystem.domain.Admin) type = "ADMIN";
                else if (u instanceof documentsystem.domain.Author) type = "AUTHOR";

                setText(u.getUsername() + " (" + type + ")");
            }
        });

        Button addUserBtn = new Button("Add User");
        addUserBtn.setMaxWidth(Double.MAX_VALUE);

        VBox usersPanel = new VBox(10, usersList, addUserBtn);
        usersPanel.setVisible(false);
        usersPanel.setManaged(false);
        ListView<documentsystem.domain.Category> categoriesList = new ListView<>();

        //categories panel
        categoriesList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(documentsystem.domain.Category c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setText(null); return; }
                setText(c.getId() + " — " + c.getName());
            }
        });

        Button addCategoryBtn = new Button("Add Category");
        addCategoryBtn.setMaxWidth(Double.MAX_VALUE);

        VBox categoriesPanel = new VBox(10, categoriesList, addCategoryBtn);
        categoriesPanel.setVisible(false);
        categoriesPanel.setManaged(false);

        // ===== GROUP: Documents Panel =====
        VBox docsPanel = new VBox(10,
                docsList,
                contentTitle,
                contentVersion,
                versionPicker,
                contentArea,
                editBar
        );
        docsPanel.setVisible(false);
        docsPanel.setManaged(false);

        final String[] originalText = new String[1];

        docsList.setPrefHeight(400);
        VBox.setVgrow(docsList, Priority.ALWAYS);

        final javafx.scene.Node[] activePanel = {null};


        java.util.Map<javafx.scene.Node, Runnable> onClose = new java.util.HashMap<>();

        onClose.put(docsPanel, () -> {
            docsList.getSelectionModel().clearSelection();
            contentTitle.setText("");
            contentVersion.setText("");
            contentArea.setText("");
            versionPicker.getItems().clear();
            versionPicker.setVisible(false);
            versionPicker.setManaged(false);

            contentArea.setEditable(false);
            saveBtn.setDisable(true);
            cancelBtn.setDisable(true);
            editBtn.setText("Edit");
            editBtn.setDisable(true);
            deleteBtn.setDisable(true);
            subscribeBtn.setDisable(true);
        });

        onClose.put(subsPanel, () -> {
            subsList.getItems().clear();
            subsList.getSelectionModel().clearSelection();
            unsubscribeBtn.setDisable(true);
        });

        onClose.put(searchPanel, () -> {
            titleQueryField.clear();
            authorQueryField.clear();
            categoryFilterBox.getSelectionModel().select(0);
            resultsList.getItems().clear();

            searchContentTitle.setText("");
            searchContentVersion.setText("");
            searchContentArea.setText("");

            resultsList.getSelectionModel().clearSelection();
            searchSubscribeBtn.setDisable(true);
        });

        onClose.put(usersPanel, () -> {
            usersList.getItems().clear();
            usersList.getSelectionModel().clearSelection();
        });

        onClose.put(categoriesPanel, () -> {
            categoriesList.getItems().clear();
            categoriesList.getSelectionModel().clearSelection();
        });

        java.util.function.Consumer<javafx.scene.Node> togglePanel = (panel) -> {

            // αν πατάς το ίδιο panel -> κλείσε το
            if (activePanel[0] == panel) {
                panel.setVisible(false);
                panel.setManaged(false);

                Runnable close = onClose.get(panel);
                if (close != null) close.run();

                activePanel[0] = null;
                return;
            }

            // κλείσε ό,τι είναι ανοιχτό
            if (activePanel[0] != null) {
                javafx.scene.Node old = activePanel[0];
                old.setVisible(false);
                old.setManaged(false);

                Runnable close = onClose.get(old);
                if (close != null) close.run();
            }

            // άνοιξε το καινούριο
            panel.setVisible(true);
            panel.setManaged(true);
            activePanel[0] = panel;
        };

        java.util.function.BiFunction<documentsystem.domain.SimpleUser, String, Boolean> isSubscribed =
                (user, docId) -> user.getSubscriptions().stream().anyMatch(s -> s.getDocumentId().equals(docId));

        listDocsBtn.setOnAction(e -> {
            // toggle το panel
            togglePanel.accept(docsPanel);

            // αν έκλεισε, καθάρισε selection/content και φύγε
            if (!docsPanel.isVisible()) {
                docsList.getSelectionModel().clearSelection();
                contentTitle.setText("");
                contentVersion.setText("");
                contentArea.setText("");
                versionPicker.getItems().clear();
                versionPicker.setVisible(false);
                versionPicker.setManaged(false);
                return;
            }

            // (άνοιξε) => γέμισε τη λίστα
            docsList.getItems().clear();

            java.util.List<Document> accessible = new java.util.ArrayList<>();
            for (Document d : ctx.state.getDocuments().values()) {
                if (ctx.currentUser.getAllowedCategories().contains(d.getCategoryId())) {
                    accessible.add(d);
                }
            }

            accessible.sort(java.util.Comparator
                    .comparing(Document::getCategoryId, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Document::getTitle, String.CASE_INSENSITIVE_ORDER)
            );

            String currentCat = null;
            for (Document d : accessible) {
                if (currentCat == null || !currentCat.equalsIgnoreCase(d.getCategoryId())) {
                    currentCat = d.getCategoryId();
                    docsList.getItems().add(categoryLabel(currentCat).toUpperCase());
                }
                docsList.getItems().add(d);
            }

            if (docsList.getItems().isEmpty()) {
                docsList.getItems().add("(No accessible documents)");
            }
        });

        createDocBtn.setOnAction(e -> {

            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Create Document");

            TextField idField = new TextField();
            idField.setPromptText("Document id (unique)");

            TextField titleField = new TextField();
            titleField.setPromptText("Title");

            ComboBox<String> categoryBox = new ComboBox<>();
            categoryBox.setMaxWidth(Double.MAX_VALUE);

            // allowed categories of current user that exist in system
            java.util.List<String> allowedCats = new java.util.ArrayList<>();
            for (String catId : ctx.currentUser.getAllowedCategories()) {
                if (ctx.state.getCategories().containsKey(catId)) {
                    allowedCats.add(catId);
                }
            }
            allowedCats.sort(String.CASE_INSENSITIVE_ORDER);
            categoryBox.getItems().addAll(allowedCats);
            if (!allowedCats.isEmpty()) categoryBox.getSelectionModel().select(0);

            TextArea contentField = new TextArea();
            contentField.setPromptText("Content (use paragraphs with Enter)");
            contentField.setWrapText(true);
            contentField.setPrefRowCount(10);

            Label msg = new Label();
            msg.setStyle("-fx-text-fill: red;");

            Button createBtn = new Button("Create");
            Button cancelCreateBtn = new Button("Cancel");

            HBox buttons = new HBox(10, createBtn, cancelCreateBtn);
            buttons.setAlignment(Pos.CENTER_RIGHT);

            createBtn.setOnAction(ev -> {
                String id = idField.getText() == null ? "" : idField.getText().trim();
                String titleTxt = titleField.getText() == null ? "" : titleField.getText().trim();
                String catId = categoryBox.getValue();
                String contentTxt = contentField.getText() == null ? "" : contentField.getText();

                if (id.isEmpty() || titleTxt.isEmpty() || catId == null || catId.isBlank()) {
                    msg.setText("Please fill id, title and category.");
                    return;
                }

                try {
                    // createdAt γίνεται αυτόματα στο backend
                    ctx.documentManager.createDocument(ctx.currentUser, id, titleTxt, catId, contentTxt);

                    // refresh stats
                    updateStats(categoriesLabel, documentsLabel, watchedLabel);

                    // αν το docsPanel είναι ανοιχτό, κάνε refresh
                    if (docsPanel.isVisible()) {
                        listDocsBtn.fire(); // κλείνει
                        listDocsBtn.fire(); // ξανανοίγει (με refresh)
                    }

                    dialog.close();
                } catch (RuntimeException ex) {
                    msg.setText(ex.getMessage());
                }
            });

            cancelCreateBtn.setOnAction(ev -> dialog.close());

            VBox root = new VBox(10,
                    new Label("Document Id:"), idField,
                    new Label("Title:"), titleField,
                    new Label("Category:"), categoryBox,
                    new Label("Content:"), contentField,
                    msg,
                    buttons
            );
            root.setPadding(new Insets(15));
            root.setPrefWidth(450);

            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        });

        docsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, item) -> {
            if (!(item instanceof Document doc)) return;

            contentTitle.setText(doc.getTitle());

            boolean isAdmin = ctx.currentUser instanceof documentsystem.domain.Admin;
            boolean isAuthorOwner =
                    (ctx.currentUser instanceof documentsystem.domain.Author)
                            && ctx.currentUser.getUsername().equals(doc.getAuthorUsername());

            boolean canPickVersions = isAdmin || isAuthorOwner;

            boolean alreadySub = isSubscribed.apply(ctx.currentUser, doc.getId());
            subscribeBtn.setDisable(alreadySub);
            subscribeBtn.setVisible(!alreadySub);
            subscribeBtn.setManaged(!alreadySub);

            contentArea.setEditable(false);
            saveBtn.setDisable(true);
            cancelBtn.setDisable(true);
            editBtn.setText("Edit");
            originalText[0] = null;

            // edit: Admin or Author-owner
            boolean canEdit = isAdmin || isAuthorOwner;
            editBtn.setDisable(!canEdit);
            deleteBtn.setDisable(!canEdit);

            int latest = doc.getLatestVersionNumber();

            if (canPickVersions) {
                versionPicker.getItems().clear();

                int start = Math.max(1, latest - 2); // τελευταίες 3: latest, latest-1, latest-2
                for (int v = latest; v >= start; v--) {
                    versionPicker.getItems().add(v);
                }

                versionPicker.setVisible(true);
                versionPicker.setManaged(true);

                versionPicker.getSelectionModel().select(Integer.valueOf(latest));
            } else {
                versionPicker.setVisible(false);
                versionPicker.setManaged(false);
            }

            // show selected (or latest for simple)
            Integer requested = canPickVersions ? versionPicker.getValue() : null;
            var version = ctx.documentManager.viewDocument(ctx.currentUser, doc.getId(), requested);

            contentVersion.setText("Version: " + version.getVersionNumber());
            contentArea.setText(version.getContent());

            ctx.subscriptionManager.markSeen(ctx.currentUser, doc);
        });
        versionPicker.valueProperty().addListener((o, oldV, newV) -> {
            Object item = docsList.getSelectionModel().getSelectedItem();
            if (!(item instanceof Document doc) || newV == null) return;

            var version = ctx.documentManager.viewDocument(ctx.currentUser, doc.getId(), newV);
            contentVersion.setText("Version: " + version.getVersionNumber());
            contentArea.setText(version.getContent());
        });

        subsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, doc) -> {
            unsubscribeBtn.setDisable(doc == null);
        });

        resultsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, doc) -> {
            if (doc == null) return;

            // SimpleUser: latest, Author/Admin: εδώ δείχνουμε latest για απλότητα
            var ver = ctx.documentManager.viewDocument(ctx.currentUser, doc.getId(), null);

            searchContentTitle.setText(doc.getTitle() + " (" + categoryLabel(doc.getCategoryId()) + ")");
            searchContentVersion.setText("Version: " + ver.getVersionNumber());
            searchContentArea.setText(ver.getContent());

            boolean alreadySub = ctx.currentUser.getSubscriptions()
                    .stream().anyMatch(s -> s.getDocumentId().equals(doc.getId()));

            searchSubscribeBtn.setDisable(alreadySub);

            // mark seen αν είναι subscribed
            ctx.subscriptionManager.markSeen(ctx.currentUser, doc);
        });

        Runnable refreshUsers = () -> {
            if (!usersPanel.isVisible()) return;
            usersList.getItems().setAll(ctx.state.getUsers().values());
            usersList.getItems().sort(java.util.Comparator.comparing(documentsystem.domain.SimpleUser::getUsername, String.CASE_INSENSITIVE_ORDER));
        };

        usersList.setOnMouseClicked(ev -> {
            if (ev.getClickCount() < 1) return;
            var u = usersList.getSelectionModel().getSelectedItem();
            if (u == null) return;
            openUserDialog(false, u, stage, refreshUsers);
        });

        addUserBtn.setOnAction(ev -> openUserDialog(true, null, stage, refreshUsers));

        Runnable refreshCategories = () -> {
            if (!categoriesPanel.isVisible()) return;
            categoriesList.getItems().setAll(ctx.state.getCategories().values());
            categoriesList.getItems().sort(java.util.Comparator.comparing(documentsystem.domain.Category::getId, String.CASE_INSENSITIVE_ORDER));

            // update left stats γιατί αλλάζει #categories / #documents μετά από delete
            updateStats(categoriesLabel, documentsLabel, watchedLabel);
        };

        categoriesList.setOnMouseClicked(ev -> {
            if (ev.getClickCount() < 1) return;
            var c = categoriesList.getSelectionModel().getSelectedItem();
            if (c == null) return;
            openCategoryDialog(false, c, stage, refreshCategories);
        });

        addCategoryBtn.setOnAction(ev -> openCategoryDialog(true, null, stage, refreshCategories));

        editBtn.setOnAction(e -> {
            Object item = docsList.getSelectionModel().getSelectedItem();
            if (!(item instanceof Document doc)) return;
            if (editBtn.isDisable()) return;

            // enter edit mode
            originalText[0] = contentArea.getText();
            contentArea.setEditable(true);
            saveBtn.setDisable(false);
            cancelBtn.setDisable(false);
            editBtn.setText("Editing...");
            contentArea.requestFocus();
        });

        cancelBtn.setOnAction(e -> {
            contentArea.setText(originalText[0] == null ? "" : originalText[0]);
            contentArea.setEditable(false);
            saveBtn.setDisable(true);
            cancelBtn.setDisable(true);
            editBtn.setText("Edit");
        });

        saveBtn.setOnAction(e -> {
            Object item = docsList.getSelectionModel().getSelectedItem();
            if (!(item instanceof Document selected)) return;

            String newContent = contentArea.getText();
            if (newContent == null) newContent = "";

            // create New version
            ctx.documentManager.updateDocumentContent(ctx.currentUser, selected.getId(), newContent);

            // get updated doc from state (for new latest version)
            Document updated = ctx.state.getDocuments().get(selected.getId());
            int latest = updated.getLatestVersionNumber();

            // refresh versionPicker list if allowed (admin or author-owner)
            boolean isAdmin = ctx.currentUser instanceof documentsystem.domain.Admin;
            boolean isAuthorOwner =
                    (ctx.currentUser instanceof documentsystem.domain.Author)
                            && ctx.currentUser.getUsername().equals(updated.getAuthorUsername());
            boolean canPickVersions = isAdmin || isAuthorOwner;

            if (canPickVersions) {
                versionPicker.getItems().clear();
                int start = Math.max(1, latest - 2);
                for (int v = latest; v >= start; v--) versionPicker.getItems().add(v);
                versionPicker.getSelectionModel().select(Integer.valueOf(latest));
                versionPicker.setVisible(true);
                versionPicker.setManaged(true);
            } else {
                versionPicker.setVisible(false);
                versionPicker.setManaged(false);
            }

            // show latest
            var latestVersion = ctx.documentManager.viewDocument(ctx.currentUser, updated.getId(), null);
            contentVersion.setText("Version: " + latestVersion.getVersionNumber());
            contentArea.setText(latestVersion.getContent());

            // exit edit mode
            contentArea.setEditable(false);
            saveBtn.setDisable(true);
            cancelBtn.setDisable(true);
            editBtn.setText("Edit");
        });

        deleteBtn.setOnAction(e -> {
            Object item = docsList.getSelectionModel().getSelectedItem();
            if (!(item instanceof Document doc)) return;
            if (deleteBtn.isDisable()) return;

            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION
            );
            confirm.setTitle("Confirm delete");
            confirm.setHeaderText("Delete document?");
            confirm.setContentText("Are you sure you want to delete: " + doc.getTitle() + " (" + doc.getCategoryId() + ")");

            java.util.Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) return;

            // backend delete (does cleanup watches etc. if you implemented it there)
            ctx.documentManager.deleteDocument(ctx.currentUser, doc.getId());

            // clear UI
            contentTitle.setText("");
            contentVersion.setText("");
            contentArea.setText("");
            versionPicker.getItems().clear();
            versionPicker.setVisible(false);
            versionPicker.setManaged(false);

            contentArea.setEditable(false);
            saveBtn.setDisable(true);
            cancelBtn.setDisable(true);
            editBtn.setText("Edit");
            editBtn.setDisable(true);
            deleteBtn.setDisable(true);

            // refresh list by reusing the same action
            listDocsBtn.fire();
        });

        mySubsBtn.setOnAction(e -> {
            togglePanel.accept(subsPanel);

            // αν έκλεισε, το cleanup γίνεται από onClose, άρα απλά φύγε
            if (!subsPanel.isVisible()) return;

            // αν άνοιξε: γέμισε τη λίστα
            subsList.getItems().clear();
            for (var s : ctx.currentUser.getSubscriptions()) {
                Document d = ctx.state.getDocuments().get(s.getDocumentId());
                if (d != null) subsList.getItems().add(d);
            }
            unsubscribeBtn.setDisable(true);
        });

        unsubscribeBtn.setOnAction(e -> {
            Document selected = subsList.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            ctx.subscriptionManager.removeSubscription(ctx.currentUser, selected.getId());

            // refresh list
            subsList.getItems().remove(selected);
            unsubscribeBtn.setDisable(true);

            // update left stats (watchedLabel)
            updateStats(categoriesLabel, documentsLabel, watchedLabel);
        });

        subscribeBtn.setOnAction(e -> {
            Object item = docsList.getSelectionModel().getSelectedItem();
            if (!(item instanceof Document doc)) return;

            try {
                ctx.subscriptionManager.addSubscription(ctx.currentUser, doc);
                updateStats(categoriesLabel, documentsLabel, watchedLabel);
                subscribeBtn.setVisible(false);
                subscribeBtn.setManaged(false);

            } catch (RuntimeException ex) {
                Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        searchDocsBtn.setOnAction(e -> {
            togglePanel.accept(searchPanel);
            if (!searchPanel.isVisible()) return;

            // όταν ανοίγει, ξεκινά με empty αποτελέσματα (ή μπορείς να κάνεις auto-search)
            resultsList.getItems().clear();
        });

        runSearchBtn.setOnAction(e -> {
            String cat = categoryFilterBox.getValue();
            if (cat != null && cat.isBlank()) cat = null;

            String t = titleQueryField.getText();
            String a = authorQueryField.getText();

            var results = ctx.documentManager.searchDocuments(ctx.currentUser, cat, t, a);

            results.sort(java.util.Comparator
                    .comparing(Document::getCategoryId, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Document::getTitle, String.CASE_INSENSITIVE_ORDER)
            );

            resultsList.getItems().setAll(results);

            // clear viewer
            searchContentTitle.setText("");
            searchContentVersion.setText("");
            searchContentArea.setText("");
            searchSubscribeBtn.setDisable(true);
        });

        searchSubscribeBtn.setOnAction(e -> {
            Document doc = resultsList.getSelectionModel().getSelectedItem();
            if (doc == null) return;

            ctx.subscriptionManager.addSubscription(ctx.currentUser, doc);
            updateStats(categoriesLabel, documentsLabel, watchedLabel);

            searchSubscribeBtn.setDisable(true);
        });

        usersBtn.setOnAction(e -> {
            togglePanel.accept(usersPanel);
            if (!usersPanel.isVisible()) return;

            usersList.getItems().clear();
            usersList.getItems().addAll(ctx.state.getUsers().values());

            // προαιρετικό sort
            usersList.getItems().sort(java.util.Comparator.comparing(documentsystem.domain.SimpleUser::getUsername, String.CASE_INSENSITIVE_ORDER));
        });

        categoriesBtn.setOnAction(e -> {
            togglePanel.accept(categoriesPanel);
            if (!categoriesPanel.isVisible()) return;

            categoriesList.getItems().setAll(ctx.state.getCategories().values());
            categoriesList.getItems().sort(java.util.Comparator.comparing(documentsystem.domain.Category::getId, String.CASE_INSENSITIVE_ORDER));
        });

        actionsBox.getChildren().addAll(
                actionsTitle,
                listDocsBtn,
                searchDocsBtn,
                mySubsBtn,
                createDocBtn,
                usersBtn,
                categoriesBtn,
                searchPanel,
                docsPanel,
                subsPanel,
                usersPanel,
                categoriesPanel
        );

        javafx.scene.control.ScrollPane actionsScroll = new javafx.scene.control.ScrollPane(actionsBox);
        actionsScroll.setFitToWidth(true);
        actionsScroll.setFitToHeight(false); // only vertical scroll
        actionsScroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        actionsScroll.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);

        splitPane.getItems().addAll(statsBox, actionsScroll);
        splitPane.setDividerPositions(0.2);

        Scene scene = new Scene(splitPane);

        // Make SplitPane divider fixed (non-draggable)
        scene.windowProperty().addListener((obs, oldWin, newWin) -> {
            if (newWin == null) return;

            // run after first layout pass so dividers exist
            javafx.application.Platform.runLater(() -> {
                splitPane.applyCss();
                splitPane.layout();

                for (Node divider : splitPane.lookupAll(".split-pane-divider")) {
                    divider.setMouseTransparent(true);
                }

            });
        });

        return scene;
    }



    private void updateStats(Label categoriesLabel,
                             Label documentsLabel,
                             Label watchedLabel) {


        categoriesLabel.setText("Total categories: " + ctx.state.getCategories().size());
        documentsLabel.setText("Total documents: " + ctx.state.getDocuments().size());
        int watched = (ctx.currentUser == null) ? 0 : ctx.currentUser.getSubscriptions().size();
        watchedLabel.setText("Documents you watch: " + watched);
    }

    private void openUserDialog(boolean isAdd,
                                documentsystem.domain.SimpleUser existing,
                                javafx.stage.Stage ownerStage,
                                Runnable refreshUsersList) {

        boolean isAdmin = ctx.currentUser instanceof documentsystem.domain.Admin;
        if (!isAdmin) return;

        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle(isAdd ? "Add User" : "User Details");

        javafx.scene.control.TextField usernameField = new javafx.scene.control.TextField();
        javafx.scene.control.PasswordField passwordField = new javafx.scene.control.PasswordField();
        javafx.scene.control.TextField firstNameField = new javafx.scene.control.TextField();
        javafx.scene.control.TextField lastNameField = new javafx.scene.control.TextField();

        javafx.scene.control.ComboBox<String> roleBox = new javafx.scene.control.ComboBox<>();
        roleBox.getItems().addAll("SIMPLE", "AUTHOR", "ADMIN");
        roleBox.getSelectionModel().select(0);

        // Allowed categories multi-select
        javafx.scene.control.ListView<String> allowedCatsList = new javafx.scene.control.ListView<>();
        allowedCatsList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        java.util.List<String> allCats = new java.util.ArrayList<>(ctx.state.getCategories().keySet());
        allCats.sort(String.CASE_INSENSITIVE_ORDER);
        allowedCatsList.getItems().addAll(allCats);

        javafx.scene.control.Label msg = new javafx.scene.control.Label();
        msg.setStyle("-fx-text-fill: red;");

        javafx.scene.control.Button editBtn = new javafx.scene.control.Button("Edit");
        javafx.scene.control.Button saveBtn = new javafx.scene.control.Button(isAdd ? "Create" : "Save");
        javafx.scene.control.Button cancelBtn = new javafx.scene.control.Button("Cancel");
        javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button("Delete");

        // αρχικό state
        boolean[] editing = new boolean[]{ isAdd }; // add ανοίγει σε edit mode

        // φόρτωση δεδομένων αν είναι view/edit
        if (!isAdd && existing != null) {
            usernameField.setText(existing.getUsername());
            passwordField.setText(existing.getPassword());
            firstNameField.setText(existing.getFirstName());
            lastNameField.setText(existing.getLastName());

            String type = "SIMPLE";
            if (existing instanceof documentsystem.domain.Admin) type = "ADMIN";
            else if (existing instanceof documentsystem.domain.Author) type = "AUTHOR";
            roleBox.getSelectionModel().select(type);

            // select allowed categories
            if (existing.getAllowedCategories() != null) {
                for (String c : existing.getAllowedCategories()) {
                    int idx = allowedCatsList.getItems().indexOf(c);
                    if (idx >= 0) allowedCatsList.getSelectionModel().select(idx);
                }
            }
        }

        // username: για existing δεν επιτρέπουμε αλλαγή (γιατί είναι key στο map)
        usernameField.setDisable(!isAdd);

        // enable/disable fields ανά edit mode
        java.util.function.Consumer<Boolean> setEditable = (on) -> {
            passwordField.setDisable(!on);
            firstNameField.setDisable(!on);
            lastNameField.setDisable(!on);
            roleBox.setDisable(!on);
            allowedCatsList.setDisable(!on);

            saveBtn.setDisable(!on);
            editBtn.setDisable(isAdd); // στο add δεν έχει νόημα
            deleteBtn.setDisable(isAdd); // στο add δεν υπάρχει delete
        };

        setEditable.accept(editing[0]);

        editBtn.setOnAction(ev -> {
            editing[0] = true;
            setEditable.accept(true);
        });

        cancelBtn.setOnAction(ev -> dialog.close());

        deleteBtn.setOnAction(ev -> {
            if (existing == null) return;

            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION
            );
            confirm.setTitle("Confirm delete");
            confirm.setHeaderText("Delete user?");
            confirm.setContentText("Are you sure you want to delete: " + existing.getUsername());

            var res = confirm.showAndWait();
            if (res.isEmpty() || res.get() != javafx.scene.control.ButtonType.OK) return;

            ctx.adminManager.deleteUser((documentsystem.domain.Admin) ctx.currentUser, existing.getUsername());
            refreshUsersList.run();
            dialog.close();
        });

        saveBtn.setOnAction(ev -> {
            String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
            String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
            String fn = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
            String ln = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
            String role = roleBox.getValue();

            var selectedCats = new java.util.HashSet<>(allowedCatsList.getSelectionModel().getSelectedItems());

            if (username.isEmpty() || password.isEmpty() || fn.isEmpty() || ln.isEmpty()) {
                msg.setText("Fill username, password, first name, last name.");
                return;
            }
            if (selectedCats.isEmpty()) {
                msg.setText("Select at least 1 allowed category.");
                return;
            }

            try {
                if (isAdd) {
                    documentsystem.domain.SimpleUser newUser;
                    if ("ADMIN".equals(role)) {
                        newUser = new documentsystem.domain.Admin(username, password, fn, ln, selectedCats);
                    } else if ("AUTHOR".equals(role)) {
                        newUser = new documentsystem.domain.Author(username, password, fn, ln, selectedCats);
                    } else {
                        newUser = new documentsystem.domain.SimpleUser(username, password, fn, ln, selectedCats);
                    }

                    ctx.adminManager.addUser((documentsystem.domain.Admin) ctx.currentUser, newUser);
                    refreshUsersList.run();
                    dialog.close();
                    return;
                }

                // EDIT existing:
                // Αν αλλάξει ρόλος, αντικαθιστούμε object στο map για να αλλάξει class type σωστά.
                documentsystem.domain.SimpleUser old = existing;

                documentsystem.domain.SimpleUser updated;
                if ("ADMIN".equals(role)) {
                    updated = new documentsystem.domain.Admin(old.getUsername(), password, fn, ln, selectedCats);
                } else if ("AUTHOR".equals(role)) {
                    updated = new documentsystem.domain.Author(old.getUsername(), password, fn, ln, selectedCats);
                } else {
                    updated = new documentsystem.domain.SimpleUser(old.getUsername(), password, fn, ln, selectedCats);
                }

                ctx.adminManager.updateUser((documentsystem.domain.Admin) ctx.currentUser, old.getUsername(), updated);

                refreshUsersList.run();
                dialog.close();

            } catch (RuntimeException ex) {
                msg.setText(ex.getMessage());
            }
        });

        javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(10, editBtn, saveBtn, deleteBtn, cancelBtn);
        buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(10,
                new javafx.scene.control.Label("Username:"), usernameField,
                new javafx.scene.control.Label("Password:"), passwordField,
                new javafx.scene.control.Label("First name:"), firstNameField,
                new javafx.scene.control.Label("Last name:"), lastNameField,
                new javafx.scene.control.Label("Role:"), roleBox,
                new javafx.scene.control.Label("Allowed categories (Ctrl/Shift select):"),
                allowedCatsList,
                msg,
                buttons
        );
        root.setPadding(new javafx.geometry.Insets(15));
        root.setPrefWidth(420);
        allowedCatsList.setPrefHeight(160);

        dialog.setScene(new javafx.scene.Scene(root));
        dialog.showAndWait();
    }

    private void openCategoryDialog(boolean isAdd,
                                    documentsystem.domain.Category existing,
                                    javafx.stage.Stage ownerStage,
                                    Runnable refreshCategories) {

        boolean isAdmin = ctx.currentUser instanceof documentsystem.domain.Admin;
        if (!isAdmin) return;

        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle(isAdd ? "Add Category" : "Category Details");

        javafx.scene.control.TextField idField = new javafx.scene.control.TextField();
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();

        javafx.scene.control.Label msg = new javafx.scene.control.Label();
        msg.setStyle("-fx-text-fill: red;");

        javafx.scene.control.Button editBtn = new javafx.scene.control.Button("Edit");
        javafx.scene.control.Button saveBtn = new javafx.scene.control.Button(isAdd ? "Create" : "Save");
        javafx.scene.control.Button cancelBtn = new javafx.scene.control.Button("Cancel");
        javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button("Delete");

        boolean[] editing = new boolean[]{ isAdd }; // στο add ξεκινάμε σε edit mode

        if (!isAdd && existing != null) {
            idField.setText(existing.getId());
            nameField.setText(existing.getName());
        }

        // id δεν αλλάζει ποτέ
        idField.setDisable(true);
        if (isAdd) idField.setDisable(false); // στο add επιτρέπεται συμπλήρωση

        java.util.function.Consumer<Boolean> setEditable = (on) -> {
            nameField.setDisable(!on);

            saveBtn.setDisable(!on);
            editBtn.setDisable(isAdd);
            deleteBtn.setDisable(isAdd);
        };

        setEditable.accept(editing[0]);

        editBtn.setOnAction(ev -> {
            editing[0] = true;
            setEditable.accept(true);
        });

        cancelBtn.setOnAction(ev -> dialog.close());

        deleteBtn.setOnAction(ev -> {
            if (existing == null) return;

            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION
            );
            confirm.setTitle("Confirm delete");
            confirm.setHeaderText("Delete category?");
            confirm.setContentText("This will also delete all documents in this category.");

            var res = confirm.showAndWait();
            if (res.isEmpty() || res.get() != javafx.scene.control.ButtonType.OK) return;

            ctx.adminManager.deleteCategory((documentsystem.domain.Admin) ctx.currentUser, existing.getId());

            // refresh stats (docs count may change)
            // (θα το κάνουμε από έξω καλώντας updateStats)
            refreshCategories.run();
            dialog.close();
        });

        saveBtn.setOnAction(ev -> {
            String id = idField.getText() == null ? "" : idField.getText().trim();
            String name = nameField.getText() == null ? "" : nameField.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                msg.setText("Fill id and name.");
                return;
            }

            try {
                if (isAdd) {
                    ctx.adminManager.addCategory((documentsystem.domain.Admin) ctx.currentUser, id, name);
                    refreshCategories.run();
                    dialog.close();
                    return;
                }

                // EDIT: μόνο rename
                ctx.adminManager.renameCategory((documentsystem.domain.Admin) ctx.currentUser, existing.getId(), name);

                refreshCategories.run();
                dialog.close();

            } catch (RuntimeException ex) {
                msg.setText(ex.getMessage());
            }
        });

        javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(10, editBtn, saveBtn, deleteBtn, cancelBtn);
        buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(10,
                new javafx.scene.control.Label("Category Id:"), idField,
                new javafx.scene.control.Label("Category Name:"), nameField,
                msg,
                buttons
        );
        root.setPadding(new javafx.geometry.Insets(15));
        root.setPrefWidth(420);

        dialog.setScene(new javafx.scene.Scene(root));
        dialog.showAndWait();
    }

    private String categoryLabel(String categoryId) {
        if (categoryId == null) return "(Unknown category)";
        var c = ctx.state.getCategories().get(categoryId);
        if (c == null) return "(Deleted: " + categoryId + ")";
        return c.getName();
    }
}
