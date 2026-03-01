package documentsystem.managers;

import documentsystem.domain.*;
import documentsystem.storage.AppState;

import java.util.Iterator;
import java.util.Map;

public class AdminManager {

    private final AppState state;
    private final SubscriptionManager subscriptionManager;


    public AdminManager(AppState state) {
        this.state = state;
        this.subscriptionManager = new SubscriptionManager(state);

    }

    private Map<String, Category> categories() {
        return state.getCategories();
    }

    private Map<String, Document> documents() {
        return state.getDocuments();
    }

    private Map<String, SimpleUser> users() {
        return state.getUsers();
    }

    // Categories

    public void addCategory(Admin admin, String id, String name) {
        if (categories().containsKey(id)) {
            throw new RuntimeException("Category id already exists.");
        }
        categories().put(id, new Category(id, name));
        //admin gets the new category by default
        admin.getAllowedCategories().add(id);
    }

    public void renameCategory(Admin admin, String categoryId, String newName) {
        Category c = categories().get(categoryId);
        if (c == null) {
            throw new RuntimeException("Category not found.");
        }
        c.rename(newName);
    }


    public void deleteCategory(Admin admin, String categoryId) {
        if (!categories().containsKey(categoryId)) {
            throw new RuntimeException("Category not found.");
        }

        Iterator<Map.Entry<String, Document>> it = documents().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Document> entry = it.next();
            Document doc = entry.getValue();

            if (doc.getCategoryId().equals(categoryId)) {
                String deletedDocId = doc.getId();

                // cleanup subscriptions για το συγκεκριμένο doc
                subscriptionManager.removeSubscriptionsForDeletedDocument(deletedDocId);

                // remove deleted category from all users' allowed categories for consistency
                for (SimpleUser u : users().values()) {
                    if (u.getAllowedCategories() != null) {
                        u.getAllowedCategories().remove(categoryId);
                    }
                }

                // delete document
                it.remove();
            }
        }

        // delete category
        categories().remove(categoryId);
    }


    public void addAllowedCategoryToUser(Admin admin, SimpleUser user, String categoryId) {
        user.getAllowedCategories().add(categoryId);
    }

    public void removeAllowedCategoryFromUser(Admin admin, SimpleUser user, String categoryId) {
        user.getAllowedCategories().remove(categoryId);
    }


    // Users

    public void addUser(Admin admin, SimpleUser newUser) {
        String username = newUser.getUsername();
        if (users().containsKey(username)) {
            throw new RuntimeException("Username already exists.");
        }
        users().put(username, newUser);
    }

    public void deleteUser(Admin admin, String username) {
        SimpleUser user = users().get(username);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }

        user.clearSubscriptions();
        users().remove(username);
    }

    // Users (update)
    public void updateUser(Admin admin, String username, SimpleUser updatedUser) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required.");
        }
        if (updatedUser == null) {
            throw new RuntimeException("Updated user is null.");
        }
        if (!users().containsKey(username)) {
            throw new RuntimeException("User not found.");
        }

        //Username cannot be changed because it is used as a key
        if (updatedUser.getUsername() == null || !username.equals(updatedUser.getUsername())) {
            throw new RuntimeException("Username cannot be changed.");
        }

        // basic checks
        if (updatedUser.getPassword() == null || updatedUser.getPassword().isBlank()) {
            throw new RuntimeException("Password is required.");
        }
        if (updatedUser.getFirstName() == null || updatedUser.getFirstName().isBlank()) {
            throw new RuntimeException("First name is required.");
        }
        if (updatedUser.getLastName() == null || updatedUser.getLastName().isBlank()) {
            throw new RuntimeException("Last name is required.");
        }
        if (updatedUser.getAllowedCategories() == null || updatedUser.getAllowedCategories().isEmpty()) {
            throw new RuntimeException("Select at least 1 allowed category.");
        }

        // ensure all allowed categories exist
        for (String catId : updatedUser.getAllowedCategories()) {
            if (!categories().containsKey(catId)) {
                throw new RuntimeException("Category does not exist: " + catId);
            }
        }

        // We keep the subscriptions of the user
        SimpleUser old = users().get(username);
        updatedUser.clearSubscriptions();
        if (old.getSubscriptions() != null) {
            for (Subscription s : old.getSubscriptions()) {
                updatedUser.subscribe(s.getDocumentId(), s.getLastSeenVersion());
            }
        }

        users().put(username, updatedUser);
    }
}