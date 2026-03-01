package documentsystem.domain;

import java.util.*;

public class Author extends SimpleUser {

    //constructor
    public Author(String username,
                  String password,
                  String firstName,
                  String lastName,
                  Set<String> allowedCategories) {

        super(username, password, firstName, lastName, allowedCategories);
    }
}