package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourtCaseRepository extends CrudRepository<CourtCaseEntity, Long> {

    @Query(value = "SELECT cc.*, null as first_created FROM court_case cc " +
        "where case_id  = :caseId " +
        "and deleted = false " +
        "order by created desc",
        nativeQuery = true)
    List<CourtCaseEntity> findByCaseIdOrderByCreatedDesc(String caseId);

    @Query(value = "SELECT cc.*, null as first_created FROM court_case cc " +
        "where case_id  = :caseId " +
        "and deleted = false " +
        "order by id desc LIMIT 1",
        nativeQuery = true)
    Optional<CourtCaseEntity> findFirstByCaseIdOrderByIdDesc(String caseId);

    @Query(value = "SELECT cc.*, null as first_created FROM court_case cc " +
        "where cc.case_no = :caseNo " +
        "and cc.court_code = :courtCode " +
        "and deleted = false " +
        "order by created desc LIMIT 1",
        nativeQuery = true)
    Optional<CourtCaseEntity> findFirstByCaseNoOrderByCreatedDesc(String courtCode, String caseNo);

    @Query(value = "select cc.*, grouped_cases.min_created as first_created from court_case cc " +
            "inner join (select max(created) as max_created,  min(created) as min_created, case_no, court_code from court_case group_cc " +
            "where group_cc.case_no = :caseNo " +
            "and group_cc.court_code = :courtCode " +
            "group by case_no, court_code) grouped_cases " +
            "on cc.case_no = grouped_cases.case_no " +
            "and cc.court_code = grouped_cases.court_code " +
            "where cc.created = grouped_cases.max_created " +
            "and cc.deleted = false",
            nativeQuery = true)
    Optional<CourtCaseEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo);

    @Query(value = "select cc.*, grouped_cases.min_created as first_created from court_case cc " +
                    "inner join " +
                    "   (select max(created) as max_created,  min(created) as min_created, case_id from court_case group_cc " +
                    "   where group_cc.case_id = :caseId " +
                    "   group by case_id) grouped_cases " +
                    "on cc.case_id = grouped_cases.case_id " +
                    "where cc.created = grouped_cases.max_created " +
                    "and cc.deleted = false",
        nativeQuery = true)
    Optional<CourtCaseEntity> findByCaseId(String caseId);

    @Query(value = "select cc.*, grouped_cases.min_created as first_created from court_case cc " +
            "inner join (select min(created) as min_created, max(created) as max_created, case_no, court_code from court_case group_cc " +
            "where (" +
                "(group_cc.created >= :createdAfter and group_cc.created < :createdBefore) " +
                "or (group_cc.created >= :createdBefore and group_cc.manual_update is true)" +
            ")" +
            "and group_cc.court_code = :courtCode " +
            "group by group_cc.case_no, group_cc.court_code) grouped_cases " +
            "on cc.case_no = grouped_cases.case_no " +
            "and cc.court_code = grouped_cases.court_code " +
            "where session_start_time >= :sessionStartAfter " +
            "and session_start_time < :sessionStartBefore " +
            "and cc.created = grouped_cases.max_created " +
            "and cc.deleted = false",
            nativeQuery = true)
    List<CourtCaseEntity> findByCourtCodeAndSessionStartTime(
            String courtCode,
            LocalDateTime sessionStartAfter,
            LocalDateTime sessionStartBefore,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore
    );

    @Query(value = "select cc.*, grouped_cases.min_created as first_created " +
        "from court_case cc " +
        "   inner join " +
        "       (select min(group_cc.created) as min_created, max(group_cc.created) as max_created, group_cc.case_id from court_case group_cc " +
        "           inner join hearing on hearing.court_case_id = group_cc.id  " +
        "           where ((group_cc.created >= :createdAfter and group_cc.created < :createdBefore) " +
        "             or (group_cc.created >= :createdBefore and group_cc.manual_update is true)) " +
        "           and hearing.court_code = :courtCode " +
        "           group by group_cc.case_id) grouped_cases " +
        "       on cc.case_id = grouped_cases.case_id " +
        "   inner join hearing h on h.court_case_id = cc.id " +
        "where h.hearing_day = :hearingDay " +
        "and cc.created = grouped_cases.max_created " +
        "and cc.deleted = false",
        nativeQuery = true)
    List<CourtCaseEntity> findByCourtCodeAndHearingDay(
        String courtCode,
        LocalDate hearingDay,
        LocalDateTime createdAfter,
        LocalDateTime createdBefore
    );


    @Query(value = "select cc.created " +
            "from court_case cc where court_code=:courtCode " +
            "   and (session_start_time between :sessionStartTimeStart and :sessionStartTimeEnd) " +
            "order by created desc limit 1",
            nativeQuery = true)
    Optional<LocalDateTime> findLastModified(String courtCode, LocalDateTime sessionStartTimeStart, LocalDateTime sessionStartTimeEnd);

    @Query(value = "select cc.created " +
        "from court_case cc " +
        "inner join hearing h on h.court_case_id = cc.id " +
        "where h.court_code = :courtCode " +
        "and h.hearing_day = :hearingDay " +
        "order by created desc limit 1",
        nativeQuery = true)
    Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay);

    @Query(value = "select cc.*, grouped_cases.min_created as first_created from court_case cc "
            + "inner join (select min(created) as min_created, max(created) as max_created, case_no, court_code from court_case group_cc "
            + "where crn = :crn "
            + "and case_no != :caseNo "
            + "group by case_no, court_code) grouped_cases "
            + "on cc.case_no = grouped_cases.case_no "
            + "and cc.court_code = grouped_cases.court_code "
            + "where session_start_time >= current_date "
            + "and cc.created = grouped_cases.max_created "
            + "and cc.deleted = false",
            nativeQuery= true)
    List<CourtCaseEntity> findOtherCurrentCasesByCrn(String crn, String caseNo);

    @Query(value = "SELECT cc.*, null as first_created FROM court_case cc "
        + "where cc.case_id != :caseId "
        + "and cc.session_start_time >= current_date "
        + "and cc.crn = :crn "
        + "and cc.deleted = false ",
        nativeQuery = true)
    List<CourtCaseEntity> findOtherCurrentCasesByCrnNotCaseId(String crn, String caseId);
}
