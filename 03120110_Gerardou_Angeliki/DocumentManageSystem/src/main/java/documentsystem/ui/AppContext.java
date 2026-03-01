package documentsystem.ui;

import documentsystem.domain.SimpleUser;
import documentsystem.managers.*;
import documentsystem.storage.AppState;
import documentsystem.storage.JsonFileManager;

public class AppContext {

    public final JsonFileManager fileManager;
    public final AppState state;

    public final AuthManager authManager;
    public final DocumentManager documentManager;
    public final AdminManager adminManager;
    public final SubscriptionManager subscriptionManager;

    public SimpleUser currentUser;

    public AppContext() {
        this.fileManager = new JsonFileManager();
        this.state = fileManager.loadAll();
        this.authManager = new AuthManager(state.getUsers());
        this.documentManager = new DocumentManager(state);
        this.adminManager = new AdminManager(state);
        this.subscriptionManager = new SubscriptionManager(state);
    }

    public void saveAll() {
        fileManager.saveAll(state);
    }
}