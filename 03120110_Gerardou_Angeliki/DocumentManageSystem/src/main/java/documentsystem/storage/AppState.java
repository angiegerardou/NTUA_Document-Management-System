package documentsystem.storage;

import documentsystem.domain.*;

import java.util.*;

public class AppState {

    private Map<String, SimpleUser> users;
    private Map<String, Document> documents;
    private Map<String, Category> categories;

    public AppState(Map<String, SimpleUser> users,
                    Map<String, Document> documents,
                    Map<String, Category> categories) {

        this.users = users;
        this.documents = documents;
        this.categories = categories;
    }

    public Map<String, SimpleUser> getUsers() {
        return users;
    }

    public Map<String, Document> getDocuments() {
        return documents;
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    @Override
    public String toString() {
        return "AppState{" +
                "users=" + users.keySet() +
                ", documents=" + documents.keySet() +
                ", categories=" + categories.keySet() +
                '}';
    }
}
