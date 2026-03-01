package documentsystem.domain;

import java.util.*;

public class Admin extends Author {

    //constructor
    public Admin(String username,
                 String password,
                 String firstName,
                 String lastName,
                 Set<String> allowedCategories) {

        super(username, password, firstName, lastName, allowedCategories);
    }
}