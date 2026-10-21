package com.judgeit.judgeit;

import static org.junit.jupiter.api.Assertions.*;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FrontendIntegrationTests {
    @Autowired Environment environment;
    @Autowired ProblemRepository problems;
    @Autowired SubmissionRepository submissions;
    @Autowired UserRepository users;
    private HttpClient client;
    private Problem problem;

    @BeforeEach
    void prepare() {
        client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        submissions.deleteAll();
        problems.deleteAll();
        users.deleteAll();
        problem = problems.save(new Problem("Sum <script>alert(1)</script>", "Easy", "Add two numbers.\nReturn the sum."));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + environment.getProperty("local.server.port")).resolve(path);
    }

    private HttpResponse<String> post(String path, Map<String, String> form) throws Exception {
        String body = form.entrySet().stream().map(entry ->
                URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void mainPagesRenderSharedNavigationAndEscapedData() throws Exception {
        for (String path : new String[]{"/", "/problems", "/problems/" + problem.getId(),
                "/problems/new", "/problems/edit/" + problem.getId(), "/submissions/new", "/submissions", "/login", "/register"}) {
            var response = get(path);
            assertEquals(200, response.statusCode(), path);
            assertTrue(response.body().contains("aria-label=\"Main navigation\""), path);
            assertTrue(response.body().contains("/css/judgeit.css"), path);
            assertTrue(response.body().contains("id=\"main\""), path);
        }
        String detail = get("/problems/" + problem.getId()).body();
        assertTrue(detail.contains("&lt;script&gt;"));
        assertFalse(detail.contains("<script>alert(1)</script>"));
        assertTrue(detail.contains("/submissions/new?problemId=" + problem.getId()));
    }

    @Test
    void solveLinkPreselectsTheProblemAndPreservesSourceOnValidationFailure() throws Exception {
        String editor = get("/submissions/new?problemId=" + problem.getId()).body();
        assertTrue(editor.matches("(?s).*<option[^>]*value=\"" + problem.getId() + "\"[^>]*selected=\"selected\".*"), editor);
        var rejected = post("/submissions", Map.of("language", "Java", "sourceCode", "// keep my work"));
        assertEquals(200, rejected.statusCode());
        assertTrue(rejected.body().contains("Please select a problem."));
        assertTrue(rejected.body().contains("// keep my work"));
        assertEquals(0, submissions.count());
    }

    @Test
    void rejectedEditCanBeCorrectedWithoutLosingItsUpdateRoute() throws Exception {
        String path = "/problems/update/" + problem.getId();
        var rejected = post(path, Map.of("title", "", "difficulty", "Easy", "description", "Corrected statement"));
        assertEquals(200, rejected.statusCode());
        assertTrue(rejected.body().contains("Problem title is required."));
        assertTrue(rejected.body().contains("action=\"" + path + "\""));
        var corrected = post(path, Map.of("title", "Updated title", "difficulty", "Medium", "description", "Corrected statement"));
        assertEquals(302, corrected.statusCode());
        assertTrue(get("/problems").body().contains("Problem updated successfully."));
        assertEquals("Updated title", problems.findById(problem.getId()).orElseThrow().getTitle());
    }

    @Test
    void savedSubmissionRendersResultAndUpdatesRealDashboardCounts() throws Exception {
        var saved = post("/submissions", Map.of("problemId", problem.getId().toString(), "language", "Java",
                "sourceCode", "// <script>unsafe</script>"));
        assertEquals(302, saved.statusCode());
        String location = saved.headers().firstValue("Location").orElseThrow();
        var resultResponse = get(location);
        assertEquals(200, resultResponse.statusCode());
        String result = resultResponse.body();
        assertTrue(result.contains("PENDING"));
        assertTrue(result.contains("&lt;script&gt;unsafe&lt;/script&gt;"));
        assertTrue(get("/submissions").body().contains("View result"));
        Submission submission = submissions.findAll().getFirst();
        submission.setVerdict("ACCEPTED");
        submissions.save(submission);
        String dashboard = get("/").body();
        assertEquals(3, dashboard.split("class=\"stat-value\">1</span>", -1).length - 1);
    }

    @Test
    void registrationLoginAndProfileUseTheSameShell() throws Exception {
        var register = post("/register", Map.of("name", "Demo Coder", "email", "demo@example.test", "password", "practice123"));
        assertEquals(302, register.statusCode());
        assertTrue(get("/login").body().contains("Account created."));
        var login = post("/login", Map.of("email", "demo@example.test", "password", "practice123"));
        assertEquals(302, login.statusCode());
        String profile = get("/profile").body();
        assertTrue(profile.contains("Demo Coder"));
        assertTrue(profile.contains("My profile"));
        assertFalse(profile.contains("Join JudgeIt"));
        assertTrue(post("/login", Map.of("email", "demo@example.test", "password", "wrong")).body().contains("Invalid email or password."));
    }

    @Test
    void deletingLinkedProblemShowsActionableFeedback() throws Exception {
        Submission submission = new Submission();
        submission.setProblem(problem);
        submission.setLanguage("Java");
        submission.setSourceCode("// solution");
        submissions.save(submission);
        assertEquals(302, post("/problems/delete/" + problem.getId(), Map.of()).statusCode());
        assertTrue(get("/problems").body().contains("linked to submissions or test cases"));
        assertTrue(problems.existsById(problem.getId()));
    }

    @Test
    void emptyWorkspaceOffersUsefulNextActions() throws Exception {
        problems.deleteAll();
        assertTrue(get("/").body().contains("A fresh start"));
        assertTrue(get("/problems").body().contains("Add your first coding problem"));
        assertTrue(get("/submissions/new").body().contains("Add a challenge first."));
        assertTrue(get("/submissions").body().contains("Your first attempt starts here."));
    }

    @Test
    void missingProblemsAndResultsReturnNotFound() throws Exception {
        assertEquals(404, get("/problems/9999999").statusCode());
        assertEquals(404, get("/submissions/9999999").statusCode());
        var error = client.send(HttpRequest.newBuilder(uri("/missing-page"))
                .header("Accept", "text/html").GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(404, error.statusCode());
        assertTrue(error.body().contains("This page could not be found."));
        assertTrue(error.body().contains("Main navigation"));
    }
}
