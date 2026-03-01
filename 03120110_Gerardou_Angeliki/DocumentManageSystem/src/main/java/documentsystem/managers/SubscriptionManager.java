package documentsystem.managers;

import documentsystem.domain.Document;
import documentsystem.domain.SimpleUser;
import documentsystem.domain.Subscription;
import documentsystem.storage.AppState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubscriptionManager {

    private final AppState state;

    public SubscriptionManager(AppState state) {
        this.state = state;
    }

    private Map<String, Document> documents() {
        return state.getDocuments();
    }

    private Map<String, SimpleUser> users() {
        return state.getUsers();
    }

    // add subscription
    public void addSubscription(SimpleUser user, Document document) {
        // access control: user must have access to the document's category
        if (user.getAllowedCategories() == null || !user.getAllowedCategories().contains(document.getCategoryId())) {
            throw new RuntimeException("User does not have access to this document's category.");
        }
        for (Subscription s : user.getSubscriptions()) {
            if (s.getDocumentId().equals(document.getId())) {
                return; // already subscribed
            }
        }
        int currentVersion = document.getLatestVersionNumber();
        user.getSubscriptions().add(new Subscription(document.getId(), currentVersion));
    }

    // remove subscription
    public void removeSubscription(SimpleUser user, String documentId) {
        user.getSubscriptions().removeIf(s -> s.getDocumentId().equals(documentId));
    }

    // get updated subscriptions (για login notification)
    public List<String> getUpdatedSubscriptions(SimpleUser user) {
        List<String> updated = new ArrayList<>();

        for (Subscription s : user.getSubscriptions()) {
            Document doc = documents().get(s.getDocumentId());
            if (doc == null) continue; // deleted doc -> ignore

            int latest = doc.getLatestVersionNumber();
            if (latest > s.getLastSeenVersion()) {
                updated.add(doc.getId());
            }
        }

        return updated;
    }

    // mark seen
    public void markSeen(SimpleUser user, Document document) {
        for (Subscription s : user.getSubscriptions()) {
            if (s.getDocumentId().equals(document.getId())) {
                s.setLastSeenVersion(document.getLatestVersionNumber());
                return;
            }
        }
    }

    // cleanup when a document is deleted
    public void removeSubscriptionsForDeletedDocument(String deletedDocId) {
        for (SimpleUser u : users().values()) {
            u.getSubscriptions().removeIf(s -> s.getDocumentId().equals(deletedDocId));
        }
    }
}