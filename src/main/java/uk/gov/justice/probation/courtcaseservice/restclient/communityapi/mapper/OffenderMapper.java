package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.*;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OffenderMapper {
    public Offender offenderFrom(CommunityApiOffenderResponse offenderResponse) {
        return Offender.builder()
                .crn(offenderResponse.getOtherIds().getCrn())
                .offenderManagers(
                        offenderResponse.getOffenderManagers().stream()
                                .map(this::buildOffenderManager)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private OffenderManager buildOffenderManager(CommunityApiOffenderManager offenderManager) {
        var staff = offenderManager.getStaff();
        return OffenderManager.builder()
            .forenames(staff.getForenames())
            .surname(staff.getSurname())
            .allocatedDate(offenderManager.getFromDate())
            .build();
    }

    public List<Conviction> convictionsFrom(CommunityApiConvictionsResponse convictionsResponse) {
        return convictionsResponse.getConvictions().stream()
                .map(this::buildConviction)
                .collect(Collectors.toList());
    }

    private Conviction buildConviction(CommunityApiConvictionResponse conviction) {
        return Conviction.builder()
                .convictionId(conviction.getConvictionId())
                .active(conviction.getActive())
                .convictionDate(conviction.getConvictionDate())
                .offences(conviction.getOffences().stream()
                    .map(offence -> new Offence(offence.getDetail().getDescription()))
                    .collect(Collectors.toList())
                )
                .sentence(Sentence.builder()
                        .description(conviction.getSentence().getDescription())
                        .length(conviction.getSentence().getOriginalLength())
                        .lengthUnits(conviction.getSentence().getOriginalLengthUnits())
                        .lengthInDays(conviction.getSentence().getLengthInDays())
                        .build()
                )
                .endDate(conviction.getConvictionDate().plus(conviction.getSentence().getLengthInDays(), ChronoUnit.DAYS))
                .build();
    }
}