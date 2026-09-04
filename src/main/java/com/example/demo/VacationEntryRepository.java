package com.example.demo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VacationEntryRepository extends CrudRepository<VacationEntry, Integer> {
    List<VacationEntry> findByParticipantId(Integer participantId);
    List<VacationEntry> findByTrainerId(Integer trainerId);
    List<VacationEntry> findByTrainerIdAndReason(Integer trainerId, String reason);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VacationEntry v WHERE v.id = :id")
    Optional<VacationEntry> findByIdWithLock(Integer id);

    @Query("SELECT COUNT(v) > 0 FROM VacationEntry v WHERE v.participant.id = :participantId AND v.reason = :reason AND v.startDate <= :endDate AND v.endDate >= :startDate")
    boolean existsOverlappingByParticipantIdAndReason(
            @Param("participantId") Integer participantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("reason") String reason);

    // Company holidays are deliberately excluded: they are mandatory closures that may legitimately
    // overlap a regular vacation request, so they must not block its approval.
    @Query("SELECT COUNT(v) > 0 FROM VacationEntry v WHERE v.participant.id = :participantId AND v.status = 'approved' AND (v.reason IS NULL OR v.reason <> 'Company Holiday') AND v.startDate <= :endDate AND v.endDate >= :startDate AND v.id <> :excludeId")
    boolean existsApprovedRegularOverlappingByParticipantId(
            @Param("participantId") Integer participantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Integer excludeId);

    @Query("SELECT v FROM VacationEntry v WHERE v.participant.id = :participantId AND v.status = 'approved' AND v.startDate <= :endDate AND v.endDate >= :startDate")
    List<VacationEntry> findApprovedOverlappingByParticipantId(
            @Param("participantId") Integer participantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
