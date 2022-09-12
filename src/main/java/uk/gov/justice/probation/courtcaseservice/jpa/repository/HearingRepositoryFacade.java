package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
/**
 * An intermediary between the Service layer and the JPA repository intended to present a clean interface
 * to Service layer consumers, abstracting away custom code required for maintaining immutable records.
 */
public class HearingRepositoryFacade {
    private static final int MAX_YEAR_SUPPORTED_BY_DB = 294276;
    private static final int MIN_YEAR_SUPPORTED_BY_DB = -4712;

    private final OffenderRepository offenderRepository;
    private final OffenderRepositoryFacade offenderRepositoryFacade;
    private final HearingRepository hearingRepository;
    private final DefendantRepository defendantRepository;
    private final CaseCommentsRepository caseCommentsRepository;

    @Autowired
    public HearingRepositoryFacade(OffenderRepository offenderRepository, OffenderRepositoryFacade offenderRepositoryFacade,
                                   HearingRepository hearingRepository, DefendantRepository defendantRepository,
                                   CaseCommentsRepository caseCommentsRepository) {
        this.offenderRepository = offenderRepository;
        this.offenderRepositoryFacade = offenderRepositoryFacade;
        this.hearingRepository = hearingRepository;
        this.defendantRepository = defendantRepository;
        this.caseCommentsRepository = caseCommentsRepository;
    }

    public Optional<HearingEntity> findFirstByHearingIdOrderByIdDesc(String hearingId) {
        return hearingRepository.findFirstByHearingIdOrderByIdDesc(hearingId)
                .map(this::updateWithDefendants);
    }

    public Optional<HearingEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo, String listNo) {
        return hearingRepository.findByCourtCodeAndCaseNo(courtCode, caseNo, listNo)
                .map(this::updateWithDefendants);
    }

    public Optional<HearingEntity> findByHearingIdAndDefendantId(String hearingId, String defendantId) {

        return hearingRepository.findFirstByHearingIdOrderByIdDesc(hearingId)
                .map(hearingEntity -> findDefendant(hearingEntity, defendantId).isPresent() ? updateWithDefendants(hearingEntity) : null)
                .map(hearingEntity -> {
                    hearingEntity.getCourtCase().setCaseComments(caseCommentsRepository.findAllByCaseIdAndDeletedFalse(hearingEntity.getCaseId()));
                    return hearingEntity;
                });
    }

    @Deprecated
    /**
     * @deprecated Deprecated in favour of the version without createdAfter and createdBefore parameters as the lookup is
     * significantly more efficient without these constraints.
     */
    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {

        return (canIgnoreCreatedDates(createdAfter, createdBefore)
                        ? hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay)
                        : hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay, createdAfter, createdBefore))
                .stream()
                .map(this::updateWithDefendants)
                .collect(Collectors.toList());
    }

    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay) {
        return hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay)
                .stream()
                .map(this::updateWithDefendants)
                .collect(Collectors.toList());
    }

    public Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay) {
        return hearingRepository.findLastModifiedByHearingDay(courtCode, hearingDay);
    }

    public HearingEntity save(HearingEntity hearingEntity) {
        updateWithExistingEntities(hearingEntity);

        final var changedDefendants = getChangedDefendants(hearingEntity);

        offenderRepository.saveAll(getChangedOffenders(changedDefendants));
        defendantRepository.saveAll(changedDefendants);
        return hearingRepository.save(hearingEntity);
    }

    private void updateWithExistingEntities(HearingEntity hearingEntity) {
        hearingEntity.getHearingDefendants().forEach((HearingDefendantEntity hearingDefendantEntity) -> {
            hearingDefendantEntity.setDefendant(
                    hearingDefendantEntity.getDefendant()
                            .withOffender(Optional.ofNullable(hearingDefendantEntity.getDefendant().getOffender())
                                    .map(offenderRepositoryFacade::updateOffenderIfItExists)
                                    .orElse(null)));
        });
    }

    private List<OffenderEntity> getChangedOffenders(List<DefendantEntity> changedDefendantEntities) {
        return changedDefendantEntities.stream()
                .map(DefendantEntity::getOffender)
                .filter(Objects::nonNull)
                .filter(offenderEntity -> offenderRepository.findByCrn(offenderEntity.getCrn())
                        .map(existing -> !existing.equals(offenderEntity))
                        .orElse(true))
                .collect(Collectors.toList());
    }

    private List<DefendantEntity> getChangedDefendants(HearingEntity hearingEntity) {
        return hearingEntity.getHearingDefendants()
                .stream()
                .map(HearingDefendantEntity::getDefendant)
                .filter(existingDefendant -> defendantRepository.findFirstByDefendantIdOrderByIdDesc(existingDefendant.getDefendantId())
                        .map(existing -> !existing.equals(existingDefendant))
                        .orElse(true))
                .collect(Collectors.toList());
    }

    private boolean canIgnoreCreatedDates(LocalDateTime createdAfter, LocalDateTime createdBefore) {
        return (createdAfter == null && createdBefore == null)
                || (createdAfter.getYear() <= MIN_YEAR_SUPPORTED_BY_DB && createdBefore.getYear() >= MAX_YEAR_SUPPORTED_BY_DB);
    }

    private Optional<HearingDefendantEntity> findDefendant(HearingEntity hearingEntity, String defendantId) {
        return hearingEntity.getHearingDefendants()
                .stream()
                .filter(hearingDefendantEntity -> defendantId.equals(hearingDefendantEntity.getDefendantId()))
                .findFirst();
    }

    private HearingEntity updateWithDefendants(HearingEntity hearingEntity) {
        return hearingEntity.withHearingDefendants(hearingEntity.getHearingDefendants()
                .stream()
                .map(this::updateDefendantAndOffender)
                .collect(Collectors.toList()));
    }

    private HearingDefendantEntity updateDefendantAndOffender(HearingDefendantEntity hearingDefendantEntity) {
        return defendantRepository.findFirstByDefendantIdOrderByIdDesc(hearingDefendantEntity.getDefendantId())
                .map(defendant -> {
                    hearingDefendantEntity.setDefendant(defendant);
                    Optional.ofNullable(defendant.getCrn())
                            .map(crn -> offenderRepository.findByCrn(crn)
                                    .orElseThrow(() -> new RuntimeException(String.format("Unexpected state: Offender with CRN '%s' is specified on defendant '%s' but it does not exist", crn, defendant.getDefendantId())))
                            )
                            .ifPresent(defendant::setOffender);
                    return hearingDefendantEntity;
                })
                .orElseThrow(() -> {
                            throw new RuntimeException(String.format("Unexpected state: Defendant '%s' is specified on hearing '%s' but it does not exist", hearingDefendantEntity.getDefendantId(), Optional.ofNullable(hearingDefendantEntity.getHearing()).map(HearingEntity::getHearingId).orElse("<Error: Unable to determine hearingId>")));
                        }
                );
    }
}
