package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompanyHolidaysTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacationEntryRepository vacationEntryRepository;

    @Test
    void testAddAndRemoveCompanyHolidays() throws Exception {
        // Setup
        User trainer = new User();
        trainer.setName("Trainer One");
        trainer.setEmail("trainer1@example.com");
        trainer.setRole("berufstrainer");
        trainer.setPassword("pass");
        userRepository.save(trainer);

        User p1 = new User();
        p1.setName("P One");
        p1.setEmail("p1@example.com");
        p1.setRole("teilnehmer");
        p1.setAssignedTrainer(trainer);
        p1.setVacationDays(30.0);
        userRepository.save(p1);

        // Monday to Friday (5 days)
        String startDate = "2023-06-19"; 
        String endDate = "2023-06-23"; 

        // Log in as admin to obtain a session cookie
        HttpClient noRedirectClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=admin@admin.de&password=password"))
                .build();
        HttpResponse<String> loginResponse = noRedirectClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        String sessionCookie = loginResponse.headers().firstValue("Set-Cookie")
                .orElseThrow(() -> new IllegalStateException("No session cookie returned from login"))
                .split(";")[0];

        HttpClient client = HttpClient.newHttpClient();

        // 1. Add Holidays
        String form = "startDate=" + startDate + "&endDate=" + endDate + "&trainerId=" + trainer.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/addCompanyHolidays"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("Added Company Holidays for 1 participants", response.body());

        // Verify Deduction
        User p1Updated = userRepository.findById(p1.getId()).orElse(null);
        assertEquals(25.0, p1Updated.getVacationDays(), 0.1);

        // 2. Remove Holidays
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/removeCompanyHolidays"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", sessionCookie)
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("Removed Company Holidays for 1 participants", response.body());

        // Verify Refund
        p1Updated = userRepository.findById(p1.getId()).orElse(null);
        assertEquals(30.0, p1Updated.getVacationDays(), 0.1);

        // Verify Entries Deleted
        List<VacationEntry> entries = vacationEntryRepository.findByParticipantId(p1.getId());
        assertEquals(0, entries.size());
    }
}