package com.example.demo;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface VacationEntryRepository extends CrudRepository<VacationEntry, Integer> {
    List<VacationEntry> findByParticipantId(Integer participantId);
    List<VacationEntry> findByTrainerId(Integer trainerId);
}
