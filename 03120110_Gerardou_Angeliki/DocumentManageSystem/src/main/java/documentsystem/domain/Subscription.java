package documentsystem.domain;

import java.util.*;

/**
 * Represents a subscription of a user to a specific document.
 *
 * A subscription stores:
 * - the id of the document being watched
 * - the last version number the user has seen
 *
 * It is used to determine whether a document has been updated
 * since the user's last login.
 */

public class Subscription {
    private String documentId;
    private int lastSeenVersion;

    /**
     * Constructs a new Subscription.
     *
     * @param documentId       the id of the document being watched
     * @param lastSeenVersion  the version number last seen by the user
     */

    public Subscription(String documentId, int lastSeenVersion) {
        this.documentId = documentId;
        this.lastSeenVersion = lastSeenVersion;
    }

    /**
     * Returns the id of the document being watched.
     *
     * @return the document id
     */

    public String getDocumentId() {
        return documentId;
    }

    /**
     * Returns the last version number seen by the user.
     *
     * @return the last seen version
     */

    public int getLastSeenVersion() {
        return lastSeenVersion;
    }

    /**
     * Sets the last version number seen by the user.
     *
     * @param lastSeenVersion  the new last seen version
     */

    public void setLastSeenVersion(int lastSeenVersion) {
        this.lastSeenVersion = lastSeenVersion;
    }
}
