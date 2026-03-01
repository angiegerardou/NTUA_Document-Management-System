package documentsystem.domain;

import java.util.*;

public class SimpleUser {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Set<String> allowedCategories;
    private List<Subscription> subscriptions;

    //default constructor
    public SimpleUser() {}

    //constructor
    public SimpleUser(String username, String password, String firstName, String lastName,
                      Set<String> allowedCategories) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.allowedCategories = allowedCategories;
        this.subscriptions = new ArrayList<>();
    }

    //getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<String> getAllowedCategories() {
        return allowedCategories;
    }

    public void setAllowedCategories(Set<String> allowedCategories) {
        this.allowedCategories = allowedCategories;
    }

    public void clearSubscriptions() {
        if (subscriptions != null) {
            subscriptions.clear();
        }
    }


    public void subscribe(String documentId, int currentVersion) {
        subscriptions.add(new Subscription(documentId, currentVersion));
    }

    public void unsubscribe(String documentId) {
        subscriptions.removeIf(sub -> sub.getDocumentId().equals(documentId));
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }


    @Override
    public String toString() {
        return "SimpleUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", allowedCategories=" + allowedCategories +
                ", subscriptions=" + subscriptions +
                '}';
    }
}