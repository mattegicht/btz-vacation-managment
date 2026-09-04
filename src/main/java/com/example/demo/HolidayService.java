package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class HolidayService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public double calculateWorkingDays(LocalDate start, LocalDate end) {
        return workingDaysIn(start, end).size();
    }

    /**
     * The working days in [start, end] — weekdays that are not Bavarian public holidays. Returned
     * as dates rather than a count so callers can subtract the days a participant has already been
     * charged for before counting, and so a range only costs one call to the holiday API.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Set<LocalDate> workingDaysIn(LocalDate start, LocalDate end) {
        Set<LocalDate> workingDays = new HashSet<>();
        if (start != null && end != null) {
            Set<LocalDate> holidays = new HashSet<>();
            try {
                for (int year = start.getYear(); year <= end.getYear(); year++) {
                    String url = "https://get.api-feiertage.de/?years=" + year + "&states=by";
                    String response = restTemplate.getForObject(url, String.class);
                    if (response == null || response.isBlank()) {
                        throw new RuntimeException("Empty response from holiday API for year " + year);
                    }
                    JsonNode feiertage = MAPPER.readTree(response).path("feiertage");
                    if (feiertage.isArray()) {
                        for (JsonNode holiday : feiertage) {
                            holidays.add(LocalDate.parse(holiday.path("date").asText()));
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch public holidays from API", e);
            }

            LocalDate current = start;
            while (!current.isAfter(end)) {
                DayOfWeek day = current.getDayOfWeek();
                if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !holidays.contains(current)) {
                    workingDays.add(current);
                }
                current = current.plusDays(1);
            }
        }
        return workingDays;
    }
}
