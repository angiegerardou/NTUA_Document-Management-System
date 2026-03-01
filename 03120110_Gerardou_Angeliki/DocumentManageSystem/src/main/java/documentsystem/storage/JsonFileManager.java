package documentsystem.storage;

import documentsystem.domain.*;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileManager {

    private static final String BASE_PATH = "medialab/";

    // DTOs used only for JSON mapping (Jackson)
    private static class UserDTO {
        public String type;
        public String username;
        public String password;
        public String firstName;
        public String lastName;
        public Set<String> allowedCategories;
        public List<SubscriptionDTO> subscriptions;
    }

    private static class CategoryDTO {
        public String id;
        public String name;
    }

    private static class SubscriptionDTO {
        public String documentId;
        public int lastSeenVersion;
    }

    private static class DocumentDTO {
        public String id;
        public String title;
        public String authorUsername;
        public String categoryId;
        public String createdAt;
        public List<VersionDTO> versions;
    }
    private static class VersionDTO {
        public int versionNumber;
        public String content;
    }

    public JsonFileManager() {}

    public AppState loadAll() {

        Map<String, Category> categories = loadCategories();
        Map<String, Document> documents = loadDocuments();
        Map<String, SimpleUser> users = loadUsers();

        // ensure default admin exists
        if (!users.containsKey("medialab")) {
            users.put("medialab",
                    new Admin(
                            "medialab",
                            "medialab_2025",
                            "MediaLab",
                            "Admin",
                            new HashSet<>(categories.keySet()) // access to all categories
                    )
            );
        }

        return new AppState(users, documents, categories);
    }

    //-----------------------------------LOADS------------------------------------------------------------


    // Load categories from JSON file
    public Map<String, Category> loadCategories() {
        Map<String, Category> categories = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();

            File file = new File(BASE_PATH + "categories.json");
            if (!file.exists()) return categories;

            List<CategoryDTO> dtos = mapper.readValue(file, new TypeReference<List<CategoryDTO>>() {});
            for (CategoryDTO dto : dtos) {
                if (dto == null || dto.id == null || dto.name == null) continue;
                categories.put(dto.id, new Category(dto.id, dto.name));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;
    }


    // Load documents from JSON file
    public Map<String, Document> loadDocuments() {
        Map<String, Document> documents = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(BASE_PATH + "documents.json");
            if (!file.exists()) return documents;

            List<DocumentDTO> dtos = mapper.readValue(file, new TypeReference<List<DocumentDTO>>() {});

            for (DocumentDTO dto : dtos) {
                if (dto == null || dto.id == null) continue;
                if (dto.versions == null || dto.versions.isEmpty()) continue;

                // 1η version
                Document doc = new Document(
                        dto.id,
                        dto.title,
                        dto.authorUsername,
                        dto.categoryId,
                        dto.createdAt,
                        dto.versions.get(0).content
                );

                // υπόλοιπες versions
                for (int i = 1; i < dto.versions.size(); i++) {
                    doc.addNewVersion(dto.versions.get(i).content);
                }

                documents.put(dto.id, doc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return documents;
    }


    //Load users from JSON file
    public Map<String, SimpleUser> loadUsers() {
        Map<String, SimpleUser> users = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();

            File file = new File(BASE_PATH + "users.json");
            if (!file.exists()) {
                return users;
            }

            List<UserDTO> dtos = mapper.readValue(file, new TypeReference<List<UserDTO>>() {});

            for (UserDTO dto : dtos) {
                if (dto == null) continue;

                String type = dto.type;
                String username = dto.username;
                String password = dto.password;
                String firstName = dto.firstName;
                String lastName = dto.lastName;

                if (username == null || password == null || firstName == null || lastName == null) {
                    continue;
                }

                Set<String> allowed = (dto.allowedCategories == null) ? new HashSet<>() : new HashSet<>(dto.allowedCategories);

                SimpleUser userObj;
                if ("ADMIN".equals(type)) {
                    userObj = new Admin(username, password, firstName, lastName, allowed);
                } else if ("AUTHOR".equals(type)) {
                    userObj = new Author(username, password, firstName, lastName, allowed);
                } else {
                    userObj = new SimpleUser(username, password, firstName, lastName, allowed);
                }

                // subscriptions
                if (dto.subscriptions != null) {
                    for (SubscriptionDTO s : dto.subscriptions) {
                        if (s == null) continue;
                        if (s.documentId == null) continue;
                        userObj.subscribe(s.documentId, s.lastSeenVersion);
                    }
                }

                users.put(username, userObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

// helpers

    private static String extractString(String obj, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\"(.*?)\"")
                .matcher(obj);
        return m.find() ? m.group(1) : null;
    }

    private static Set<String> extractStringArray(String obj, String key) {
        Set<String> out = new HashSet<>();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\\[(.*?)]", java.util.regex.Pattern.DOTALL)
                .matcher(obj);

        if (!m.find()) return out;

        String inside = m.group(1).trim();
        if (inside.isEmpty()) return out;

        java.util.regex.Matcher item = java.util.regex.Pattern
                .compile("\"(.*?)\"")
                .matcher(inside);

        while (item.find()) out.add(item.group(1));
        return out;
    }

    private static ArrayList<Subscription> extractSubscriptions(String obj) {
        ArrayList<Subscription> subs = new ArrayList<>();

        int keyPos = obj.indexOf("\"subscriptions\"");
        if (keyPos < 0) return subs;

        int arrayStart = obj.indexOf('[', keyPos);
        if (arrayStart < 0) return subs;

        // find matching closing ']' for this '[' (bracket counting)
        int depth = 0;
        int arrayEnd = -1;
        for (int i = arrayStart; i < obj.length(); i++) {
            char ch = obj.charAt(i);
            if (ch == '[') depth++;
            else if (ch == ']') {
                depth--;
                if (depth == 0) {
                    arrayEnd = i;
                    break;
                }
            }
        }
        if (arrayEnd < 0) return subs;

        String inside = obj.substring(arrayStart + 1, arrayEnd).trim();
        if (inside.isEmpty()) return subs;

        // extract each subscription object { ... }
        java.util.regex.Matcher sm = java.util.regex.Pattern
                .compile("\\{(.*?)\\}", java.util.regex.Pattern.DOTALL)
                .matcher(inside);

        while (sm.find()) {
            String sObj = sm.group(1);
            String docId = extractString(sObj, "documentId");
            Integer lastSeen = extractInt(sObj, "lastSeenVersion");
            if (docId != null && lastSeen != null) {
                subs.add(new Subscription(docId, lastSeen));
            }
        }

        return subs;
    }

    private static Integer extractInt(String obj, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*(\\d+)")
                .matcher(obj);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }


    //--------------------------------------SAVE------------------------------------------------------------

    // Save categories to JSON file
    public void saveCategories(Map<String, Category> categories) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(BASE_PATH + "categories.json"),
                            categories.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Save document to JSON file

        //helper
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
    public void saveDocuments(Map<String, Document> documents) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);

            List<DocumentDTO> out = new ArrayList<>();

            for (Document d : documents.values()) {
                DocumentDTO dto = new DocumentDTO();
                dto.id = d.getId();
                dto.title = d.getTitle();
                dto.authorUsername = d.getAuthorUsername();
                dto.categoryId = d.getCategoryId();
                dto.createdAt = d.getCreatedAt();

                dto.versions = new ArrayList<>();
                for (DocumentVersion v : d.getVersions()) {
                    VersionDTO vd = new VersionDTO();
                    vd.versionNumber = v.getVersionNumber();
                    vd.content = v.getContent(); // Jackson χειρίζεται σωστά newlines/quotes κλπ
                    dto.versions.add(vd);
                }

                out.add(dto);
            }

            mapper.writeValue(new File(BASE_PATH + "documents.json"), out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Save users to JSON file
    public void saveUsers(Map<String, SimpleUser> users) {
        try (java.io.PrintWriter out = new java.io.PrintWriter(BASE_PATH + "users.json")) {

            out.println("[");
            int userCount = 0;

            for (SimpleUser u : users.values()) {

                out.println("  {");

                // type
                String type = "SIMPLE";
                if (u instanceof Admin) type = "ADMIN";
                else if (u instanceof Author) type = "AUTHOR";

                out.println("    \"type\": \"" + type + "\",");
                out.println("    \"username\": \"" + u.getUsername() + "\",");
                out.println("    \"password\": \"" + u.getPassword() + "\",");
                out.println("    \"firstName\": \"" + u.getFirstName() + "\",");
                out.println("    \"lastName\": \"" + u.getLastName() + "\",");

                // allowedCategories
                out.println("    \"allowedCategories\": [");
                int catCount = 0;
                for (String cat : u.getAllowedCategories()) {
                    out.print("      \"" + cat + "\"");
                    catCount++;
                    if (catCount < u.getAllowedCategories().size()) out.println(",");
                    else out.println();
                }
                out.println("    ],");

                // subscriptions
                out.println("    \"subscriptions\": [");
                int subCount = 0;
                for (Subscription s : u.getSubscriptions()) {
                    out.println("      {");
                    out.println("        \"documentId\": \"" + s.getDocumentId() + "\",");
                    out.println("        \"lastSeenVersion\": " + s.getLastSeenVersion());
                    out.print("      }");

                    subCount++;
                    if (subCount < u.getSubscriptions().size()) out.println(",");
                    else out.println();
                }
                out.println("    ]");

                out.print("  }");
                userCount++;
                if (userCount < users.size()) out.println(",");
                else out.println();
            }

            out.println("]");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAll(AppState state) {

        System.out.println("Saving... BASE_PATH = " + new java.io.File(BASE_PATH).getAbsolutePath());
        System.out.println("medialab exists? " + new java.io.File(BASE_PATH).exists());

        saveCategories(state.getCategories());
        saveDocuments(state.getDocuments());
        saveUsers(state.getUsers());
    }
}