import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class Requests {

    static final UUID USER1_ID = UUID.fromString("29d376f1-b8a9-4fc5-aec3-7bd33cb02047");
    static final UUID USER2_ID = UUID.fromString("ceab4eda-79a4-4bda-80fe-ec01593fb51a");
    static final UUID USER3_ID = UUID.fromString("fd62f5fe-faa6-4fed-8d8c-895a18a08e70");
    static final UUID ADDITIONAL_USER1_ID = UUID.fromString("c68c25a8-77a3-4e28-906f-aaace8ab1c0d");
    static final UUID ADDITIONAL_USER2_ID = UUID.fromString("a29ad355-2ca8-4d94-b46c-b52650d5415e");
    static final UUID PROJECT_ID = UUID.fromString("7be242da-8d8e-4c94-8e2d-56fa090c9fd1");
    static final UUID ADDITIONAL_PROJECT_ID = UUID.fromString("c0734eb2-e290-43bc-a94e-4e91663a73fc");
    static final String USER_SERVICE_URL = Optional.ofNullable(System.getenv("USER_SERVICE_URL"))
        .orElse("http://localhost:8080");
    static final String PROJECT_SERVICE_URL = Optional.ofNullable(System.getenv("PROJECT_SERVICE_URL"))
        .orElse("http://localhost:8080");
    static final String CREATE_USERS_COMMAND = "createUsers";
    static final String CREATE_ADDITIONAL_USERS_COMMAND = "createAdditionalUsers";
    static final String CREATE_PROJECT_COMMAND = "createProject";
    static final String CREATE_ADDITIONAL_PROJECT_COMMAND = "createAdditionalProject";

    public static void main(String[] args) {
        var commands = List.of(args);

        System.out.println("About to make some requests, commands: " + commands);
        System.out.println();

        var httpClient = HttpClient.newHttpClient();

        var user1 = new User(USER1_ID, "user1@email.com", "user1");
        var user2 = new User(USER2_ID, "user2@email.com", "user2");
        var user3 = new User(USER3_ID, "user3@email.com", "user3");

        var additionalUser1 = new User(ADDITIONAL_USER1_ID, "additional-user1@email.com", "additional-user1");
        var additionalUser2 = new User(ADDITIONAL_USER2_ID, "additional-user2@email.com", "additional-user2");

        if (commands.contains(CREATE_USERS_COMMAND)) {
            createUser(httpClient, user1);
            createUser(httpClient, user2);
            createUser(httpClient, user3);
        } else {
            System.out.printf("%s command not given, skipping users creation%n", CREATE_USERS_COMMAND);
        }
        System.out.println();
        if (commands.contains(CREATE_ADDITIONAL_USERS_COMMAND)) {
            createUser(httpClient, additionalUser1);
            createUser(httpClient, additionalUser2);
        } else {
            System.out.printf("%s command not given, skipping additional users creation%n", CREATE_ADDITIONAL_USERS_COMMAND);
        }
        System.out.println();
        if (commands.contains(CREATE_PROJECT_COMMAND)) {
            var project = new Project(PROJECT_ID,
                "project",
                "project-description",
                List.of(USER1_ID, USER2_ID, USER3_ID));
            createProject(httpClient, project);
        } else {
            System.out.printf("%s command not given, skipping project creation%n", CREATE_PROJECT_COMMAND);
        }
        System.out.println();
        if (commands.contains(CREATE_ADDITIONAL_PROJECT_COMMAND)) {
            var project = new Project(ADDITIONAL_PROJECT_ID,
                "additional-project",
                "additional-project-description",
                List.of(ADDITIONAL_USER1_ID, ADDITIONAL_USER2_ID));
            createProject(httpClient, project);
        } else {
            System.out.printf("%s command not given, skipping additional project creation%n", CREATE_ADDITIONAL_PROJECT_COMMAND);
        }

        System.out.println();
        System.out.println("All commands were executed!");
    }

    static void createUser(HttpClient client, User user) {
        try {
            var request = HttpRequest.newBuilder()
                .uri(new URI(USER_SERVICE_URL + "/users"))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(user.toJson()))
                .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Create user response:");
            System.out.println(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Problem while creating a user. Are you sure that %s url is correct?"
                .formatted(USER_SERVICE_URL), e);
        }
    }

    static void createProject(HttpClient client, Project project) {
        try {
            var request = HttpRequest.newBuilder()
                .uri(new URI(PROJECT_SERVICE_URL + "/projects"))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(project.toJson()))
                .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Create project response:");
            System.out.println(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Problem while creating a project. Are you sure that %s url is correct?"
                .formatted(PROJECT_SERVICE_URL), e);
        }
    }

    record User(UUID id, String email, String name) {

        String toJson() {
            return """
                {
                   "id": "%s",
                   "email": "%s",
                   "name": "%s"
                }
                """.formatted(id, email, name);
        }
    }

    record Project(UUID id, String name, String description, List<UUID> userIds) {

        String toJson() {
            var userIdsJson = userIds.stream()
                .map("\"%s\""::formatted)
                .collect(Collectors.joining(",", "[", "]"));
            return """
                {
                   "id": "%s",
                   "name": "%s",
                   "description": "%s",
                   "userIds": %s
                }
                """.formatted(id, name, description, userIdsJson);
        }
    }
}