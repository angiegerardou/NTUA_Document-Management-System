package documentsystem.managers;

import documentsystem.domain.SimpleUser;

import java.util.*;

public class AuthManager {

    private final Map<String, SimpleUser> users;

    public AuthManager(Map<String, SimpleUser> users) {
        this.users = users;
    }

    //login user
    public SimpleUser login(String username, String password) {

        SimpleUser user = users.get(username);

        if (user == null) {
            return null;
        }

        if (Objects.equals(user.getPassword(), password)) {
            return user;
        }

        return null;
    }
}


