package uk.gov.justice.probation.courtcaseservice.pact.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "community-api")
@PactDirectory(value = "build/pacts")
class CommunityApiPactTest {

    @Pact(provider="community-api", consumer="court-case-service")
    public V4Pact getNsis(PactDslWithProvider builder) {

        var keyValueType = new PactDslJsonBody()
            .stringType("code", "description");

        var body = new PactDslJsonBody()
            .eachLike("nsis")
                .numberType("nsiId")
                .object("nsiType", keyValueType)
                .object("nsiSubType", keyValueType)
                .object("nsiStatus", keyValueType)
                .datetime("statusDateTime")
                .date("actualStartDate","yyyy-MM-dd")
            .closeArray();

        return builder
            .given("an NSI exists for CRN X320741 and conviction id 2500295345")
            .uponReceiving("a request for a NSIs by CRN and conviction ID")
            .path("/secure/offenders/crn/X320741/convictions/2500295345/nsis")
            .query("nsiCodes=BRE")
            .method("GET")
            .willRespondWith()
            .headers(Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .body(body)
            .status(200)
            .toPact(V4Pact.class);
    }

    @Pact(provider="community-api", consumer="court-case-service")
    public V4Pact getProbationStatusDetailCurrent(PactDslWithProvider builder) {

        var body = new PactDslJsonBody()
            .booleanType("preSentenceActivity")
            .booleanType("inBreach")
            .booleanType("awaitingPsr")
            .stringType("status");

        return builder
            .given("probation status detail is available for CRN X320741")
            .uponReceiving("a request for a CURRENT probation status detail by CRN")
            .path("/secure/offenders/crn/X320741/probationStatus")
            .method("GET")
            .willRespondWith()
            .headers(Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .body(body)
            .status(200)
            .toPact(V4Pact.class);
    }

    @Pact(provider="community-api", consumer="court-case-service")
    public V4Pact getProbationStatusDetailPreviouslyKnown(PactDslWithProvider builder) {

        var body = new PactDslJsonBody()
            .booleanType("preSentenceActivity")
            .booleanType("awaitingPsr")
            .date("previouslyKnownTerminationDate","yyyy-MM-dd")
            .stringType("status");

        return builder
            .given("probation status detail is available for CRN CRN40")
            .uponReceiving("a request for a PREVIOUSLY KNOWN probation status detail by CRN")
            .path("/secure/offenders/crn/CRN40/probationStatus")
            .method("GET")
            .willRespondWith()
            .headers(Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .body(body)
            .status(200)
            .toPact(V4Pact.class);
    }

    @PactTestFor(pactMethod = "getNsis")
    @Test
    void getNsis(MockServer mockServer) throws IOException {
        var httpResponse = Request
            .Get(mockServer.getUrl() + "/secure/offenders/crn/X320741/convictions/2500295345/nsis?nsiCodes=BRE")
            .execute()
            .returnResponse();

        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }

    @PactTestFor(pactMethod = "getProbationStatusDetailCurrent")
    @Test
    void getProbationStatusDetailCurrent(MockServer mockServer) throws IOException {
        var httpResponse = Request
            .Get(mockServer.getUrl() + "/secure/offenders/crn/X320741/probationStatus")
            .execute()
            .returnResponse();

        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }

    @PactTestFor(pactMethod = "getProbationStatusDetailPreviouslyKnown")
    @Test
    void getProbationStatusDetailPreviouslyKnown(MockServer mockServer) throws IOException {
        var httpResponse = Request
            .Get(mockServer.getUrl() + "/secure/offenders/crn/CRN40/probationStatus")
            .execute()
            .returnResponse();

        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }

    @Pact(provider="community-api", consumer="court-case-service")
    public V4Pact getProbationStatus(PactDslWithProvider builder) {

        var body = new PactDslJsonBody()
                .booleanType("awaitingPsr")
                .booleanType("inBreach")
                .booleanType("preSentenceActivity")
                .date("previouslyKnownTerminationDate", "yyyy-MM-dd")
                .stringType("status")
                ;

        return builder
                .given("an offender exists with CRN X320741")
                .uponReceiving("a request for probation status")
                .path("/secure/offenders/crn/X320741/probationStatus")
                .method("GET")
                .willRespondWith()
                .headers(Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .body(body)
                .status(200)
                .toPact(V4Pact.class);
    }

    @PactTestFor(pactMethod = "getProbationStatus")
    @Test
    void getProbationStatus(MockServer mockServer) throws IOException {
        var httpResponse = Request
                .Get(mockServer.getUrl() + "/secure/offenders/crn/X320741/probationStatus")
                .execute()
                .returnResponse();

        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }

    @Pact(provider="community-api", consumer="court-case-service")
    public V4Pact getCourtReportsByCrnAndConvictionId(PactDslWithProvider builder) {

        DslPart body = PactDslJsonArray.arrayMinLike(1)
            .numberTypes("courtReportId", "offenderId")
            .date("requestedDate", "yyyy-MM-dd'T'HH:mm:ss'Z'")
            .date("requiredDate", "yyyy-MM-dd'T'HH:mm:ss'Z'")
            .date("completedDate", "yyyy-MM-dd'T'HH:mm:ss'Z'")
            .object("courtReportType")
                .stringType("code")
                .stringType("description")
            .closeObject()
            .eachLike("reportManagers")
                .booleanType("active")
                .object("staff")
                    .stringTypes("code", "forenames", "surname")
                    .booleanType("unallocated")
                .closeObject()
            .closeArray();

        return builder
            .given("an offender exists with CRN X320741 with conviction ID 2500295345")
            .uponReceiving("a request for court reports for CRN and conviction ID")
            .path("/secure/offenders/crn/X320741/convictions/2500295345/courtReports")
            .method("GET")
            .willRespondWith()
            .headers(Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .body(body)
            .status(200)
            .toPact(V4Pact.class);
    }

    @PactTestFor(pactMethod = "getCourtReportsByCrnAndConvictionId")
    @Test
    void getCourtReportsByCrnAndConvictionId(MockServer mockServer) throws IOException {
        var httpResponse = Request
            .Get(mockServer.getUrl() + "/secure/offenders/crn/X320741/convictions/2500295345/courtReports")
            .execute()
            .returnResponse();

        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }
}
