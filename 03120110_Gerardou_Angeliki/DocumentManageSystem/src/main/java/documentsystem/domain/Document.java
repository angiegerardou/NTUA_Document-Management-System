package documentsystem.domain;

import java.util.*;

public class Document {

    private String id;
    private String title;
    private String authorUsername;
    private String categoryId;
    private String createdAt;

    private List<DocumentVersion> versions;

    public Document(String id,
                    String title,
                    String authorUsername,
                    String categoryId,
                    String createdAt,
                    String initialContent) {

        this.id = id;
        this.title = title;
        this.authorUsername = authorUsername;
        this.categoryId = categoryId;
        this.createdAt = createdAt;

        this.versions = new ArrayList<>();
        // version 1
        this.versions.add(new DocumentVersion(1, initialContent));
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getLatestVersionNumber() {
        return versions.get(versions.size() - 1).getVersionNumber();
    }

    public DocumentVersion getLatestVersion() {
        return versions.get(versions.size() - 1);
    }

    public List<DocumentVersion> getVersions() {
        return versions;
    }

    // Τροποποίηση εγγράφου = νέα έκδοση (+1)
    public void addNewVersion(String newContent) {
        int nextVersion = getLatestVersionNumber() + 1;
        versions.add(new DocumentVersion(nextVersion, newContent));
    }
}
