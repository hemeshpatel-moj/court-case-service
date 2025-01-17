package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus.CONFIRMED_NO_RECORD;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus.UNCONFIRMED_NO_RECORD;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.getMutableList;


class HearingDefendantEntityTest {

    @Test
    void givenStandard_whenGetSurname_thenReturn() {
        var defendantEntity = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .defendantName("Mr Jeff BRIDGES")
                        .build())
                .build();

        assertThat(defendantEntity.getDefendantSurname()).isEqualTo("BRIDGES");
    }

    @Test
    void givenSingleString_whenGetSurname_thenReturn() {
        var defendantEntity = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .defendantName("BRIDGES")
                        .build())
                .build();

        assertThat(defendantEntity.getDefendantSurname()).isEqualTo("BRIDGES");
    }

    @Test
    void givenNullDefendantName_whenGetSurname_thenReturnEmptyString() {
        var defendantEntity = HearingDefendantEntity.builder().build();
        assertThat(defendantEntity.getDefendantSurname()).isEmpty();
    }

    @Test
    void givenEmptyDefendantName_whenGetSurname_thenReturnEmptyString() {
        var defendantEntity = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .defendantName("   ")
                        .build())
                .build();
        assertThat(defendantEntity.getDefendantSurname()).isEmpty();
    }

    @Test
    void givenNullOffender_andNoOffenderConfirmed_whenGetProbationStatus_thenReturnUnconfirmedNoRecord() {
        final HearingDefendantEntity defendant = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .offender(null)
                        .offenderConfirmed(false)
                        .build())
                .build();

        assertThat(defendant.getProbationStatusForDisplay()).isEqualTo(UNCONFIRMED_NO_RECORD);
    }

    @Test
    void givenNullOffender_andOffenderConfirmed_whenGetProbationStatus_thenReturnConfirmedNoRecord() {
        final HearingDefendantEntity defendant = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .offender(null)
                        .offenderConfirmed(true)
                        .build())
                .build();

        assertThat(defendant.getProbationStatusForDisplay()).isEqualTo(CONFIRMED_NO_RECORD);
    }

    @Test
    void givenOffender_whenGetProbationStatus_thenReturnOffenderProbationStatus() {
        final HearingDefendantEntity defendant = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .offender(OffenderEntity.builder()
                                .probationStatus(OffenderProbationStatus.CURRENT)
                                .build())
                        .build())
                .build();

        assertThat(defendant.getProbationStatusForDisplay()).isEqualTo(DefendantProbationStatus.CURRENT);
    }

    @Test
    void givenHearingDefendantUpdate_whenUpdate_shouldUpdateTheWithNewValues() {
        String defendantId = "test-defendantId";
        HearingDefendantEntity hearingDefendant = HearingDefendantEntity.builder()
            .defendantId(defendantId)
            .defendant(DefendantEntity.builder().defendantId(defendantId)
                .defendantName("test-defendant").build())
            .offences(getMutableList(List.of(OffenceEntity.builder().title("offence title").build())))
            .build();

        HearingDefendantEntity update = HearingDefendantEntity.builder()
            .defendantId(defendantId)
            .defendant(DefendantEntity.builder().defendantId(defendantId)
                .defendantName("Mr. updated test defendant").build())
            .offences(getMutableList(List.of(OffenceEntity.builder().title("updated offence title").build())))
            .build();

        hearingDefendant.update(update);

        assertThat(hearingDefendant).isEqualTo(update);
    }
}
