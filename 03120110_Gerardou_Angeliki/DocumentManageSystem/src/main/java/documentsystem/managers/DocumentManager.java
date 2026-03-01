package documentsystem.managers;

import documentsystem.domain.*;
import documentsystem.storage.AppState;

import java.util.*;

public class DocumentManager {

    private AppState state;
    private final SubscriptionManager subscriptionManager;

    public DocumentManager(AppState state) {
        this.state = state;
        this.subscriptionManager = new SubscriptionManager(state);
    }

    private Map<String, Document> getDocuments() {
        return state.getDocuments();
    }

    @Override
    public String toString() {
        return "DocumentManager{" +
                "documents=" + state.getDocuments().keySet() +
                '}';
    }

    //create a new document
    public void createDocument(SimpleUser user,
                               String id,
                               String title,
                               String categoryId,
                               String content) {

        // check for role compatibility
        if (!(user instanceof Author)) {
            throw new RuntimeException("Only authors can create documents.");
        }

        // check category exists
        if (!state.getCategories().containsKey(categoryId)) {
            throw new RuntimeException("Category does not exist.");
        }

        // check if the user has access to the category
        if (user.getAllowedCategories() == null || !user.getAllowedCategories().contains(categoryId)) {
            throw new RuntimeException("User does not have access to this category.");
        }

        // check duplicate id
        if (getDocuments().containsKey(id)) {
            throw new RuntimeException("Document id already exists.");
        }

        String createdAt = java.time.LocalDate.now().toString();

        Document document = new Document(id, title, user.getUsername(),
                categoryId, createdAt, content);

        getDocuments().put(id, document);
    }

    //update document
    public void updateDocumentContent(SimpleUser user, String documentId, String newContent) {


        if (!(user instanceof Author)) {
            throw new RuntimeException("Only authors can update documents.");
        }

        Document doc = getDocuments().get(documentId);
        if (doc == null) {
            throw new RuntimeException("Document not found.");
        }


        if (!user.getAllowedCategories().contains(doc.getCategoryId())) {
            throw new RuntimeException("User does not have access to this category.");
        }

        doc.addNewVersion(newContent);
    }

    //delete a document
    public void deleteDocument(SimpleUser user, String documentId) {
        if (!(user instanceof Author)) {
            throw new RuntimeException("Only authors can delete documents.");
        }

        Document doc = getDocuments().get(documentId);
        if (doc == null) {
            throw new RuntimeException("Document not found.");
        }

        if (!user.getAllowedCategories().contains(doc.getCategoryId())) {
            throw new RuntimeException("User does not have access to this category.");
        }

        subscriptionManager.removeSubscriptionsForDeletedDocument(documentId);
        getDocuments().remove(documentId);
    }



    //view document's content
    public DocumentVersion viewDocument(SimpleUser user, String documentId, Integer requestedVersion) {

        Document doc = getDocuments().get(documentId);
        if (doc == null) {
            throw new RuntimeException("Document not found.");
        }

        if (!user.getAllowedCategories().contains(doc.getCategoryId())) {
            throw new RuntimeException("User does not have access to this category.");
        }

        int latest = doc.getLatestVersionNumber();

        //simple user: latest
        if (!(user instanceof Author)) {
            return doc.getLatestVersion();
        }

        if (requestedVersion == null) {
            return doc.getLatestVersion();
        }

        int v = requestedVersion;

        // we allow only the latest, latest-1, latest-2
        if (v < latest - 2 || v > latest) {
            throw new RuntimeException("Authors can view only the latest version and up to 2 previous versions.");
        }

        // find the requested version in the list
        for (DocumentVersion ver : doc.getVersions()) {
            if (ver.getVersionNumber() == v) {
                return ver;
            }
        }

        throw new RuntimeException("Requested version not found.");
    }


    //search documents
    public List<Document> searchDocuments(SimpleUser user,
                                          String categoryId,
                                          String titleQuery,
                                          String authorQuery) {

        List<Document> results = new ArrayList<>();

        String titleQ = (titleQuery == null) ? "" : titleQuery.trim().toLowerCase();
        String authorQ = (authorQuery == null) ? "" : authorQuery.trim().toLowerCase();
        String catQ = (categoryId == null) ? "" : categoryId.trim();

        for (Document d : getDocuments().values()) {

            // access control: only docs in user's allowed categories
            if (user.getAllowedCategories() == null || !user.getAllowedCategories().contains(d.getCategoryId())) {
                continue;
            }

            // optional category filter
            if (!catQ.isEmpty() && !d.getCategoryId().equals(catQ)) {
                continue;
            }

            // optional title filter (contains, case-insensitive)
            if (!titleQ.isEmpty() && (d.getTitle() == null || !d.getTitle().toLowerCase().contains(titleQ))) {
                continue;
            }

            // optional author filter (contains, case-insensitive)
            if (!authorQ.isEmpty() && (d.getAuthorUsername() == null || !d.getAuthorUsername().toLowerCase().contains(authorQ))) {
                continue;
            }

            results.add(d);
        }

        return results;
    }
}