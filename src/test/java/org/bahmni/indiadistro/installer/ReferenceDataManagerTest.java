package org.bahmni.indiadistro.installer;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpStatus;
import org.bahmni.indiadistro.TestUtil;
import org.bahmni.indiadistro.config.ApplicationProperties;
import org.bahmni.indiadistro.model.CSVUploadStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReferenceDataManagerTest {
    private ApplicationProperties applicationProperties;
    private ReferenceDataManager referenceDataManager;
    private ObjectMapper objectMapper;
    private static Map<String, String> env;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    private String csvUploadURLPrefix = "/openmrs/ws/rest/v1/bahmnicore/admin/upload/";


    @BeforeClass
    public static void setUpClass() {
        env = TestUtil.loadTestProperties();
    }

    @Before
    public void setUp() throws Exception {
        File src = temporaryFolder.newFolder("src");
        temporaryFolder.newFolder("src", "htn");
        temporaryFolder.newFile("src/htn/ref_terms.csv");
        temporaryFolder.newFile("src/htn/concepts.csv");
        temporaryFolder.newFile("src/htn/concept_sets.csv");

        env.put("INDIA_DISTRO_MODULES_DIR", src.getAbsolutePath());
        applicationProperties = new ApplicationProperties(env);
        objectMapper = new ObjectMapper();
        referenceDataManager = new ReferenceDataManager(applicationProperties);

    }

    @Test
    public void shouldUploadRefTermsAndWaitForItToFinish() throws Exception {
        stubForCSVUpload("referenceterms");
        stubForCSVUpload("concept");
        stubForCSVUpload("conceptset");
        stubForUploadStatusFinished("COMPLETED", null);

        referenceDataManager.uploadForModule("htn");

        verify(1, postRequestedFor(urlMatching(csvUploadURLPrefix + "concept")));
        verify(1, postRequestedFor(urlMatching(csvUploadURLPrefix + "referenceterms")));
        verify(1, postRequestedFor(urlMatching(csvUploadURLPrefix + "conceptset")));
        verify(3, getRequestedFor(urlMatching(csvUploadURLPrefix + "status")));
    }

    @Test
    public void shouldKeepCheckingTheStatusUntilCompleted() throws Exception {
        scenariosForRefTerms();
        scenariosForConcepts();
        scenariosForConceptSets();

        referenceDataManager.uploadForModule("htn");

        verify(1, postRequestedFor(urlMatching(csvUploadURLPrefix + "referenceterms")));
        verify(1, postRequestedFor(urlMatching(csvUploadURLPrefix + "concept")));
        verify(1, postRequestedFor(urlMatching(csvUploadURLPrefix + "concept")));
        verify(4, getRequestedFor(urlMatching(csvUploadURLPrefix + "status")));
    }

    @Test
    public void shouldThrowErrorIfUploadStatusIsCompletedWithError() throws Exception {
        stubForCSVUpload("referenceterms");
        stubForUploadStatusFinished("COMPLETED_WITH_ERRORS", "some.err");

        try {
            referenceDataManager.uploadForModule("htn");
        } catch (Exception e) {
            assertEquals(e.getClass(), RuntimeException.class);
            assertTrue(e.getMessage().startsWith("Problem while uploading REF TERMs"));
        }

        verify(0, postRequestedFor(urlMatching(csvUploadURLPrefix + "concept")));
        verify(1, postRequestedFor(urlMatching(csvUploadURLPrefix + "referenceterms")));
        verify(0, postRequestedFor(urlMatching(csvUploadURLPrefix + "conceptset")));
        verify(1, getRequestedFor(urlMatching(csvUploadURLPrefix + "status")));
    }

    private void stubForUploadStatusFinished(String status, String errorFileName) throws IOException {
        CSVUploadStatus uploadStatus = new CSVUploadStatus("1", errorFileName, status);
        stubFor(get(urlEqualTo(csvUploadURLPrefix + "status"))
                .withBasicAuth(applicationProperties.getOpenmrsAPIUserName(), applicationProperties.getOpenmrsAPIUserPassword())
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(objectMapper.writeValueAsString(Arrays.asList(uploadStatus)))
                )
        );
    }

    private void stubForCSVUpload(String type) {
        stubFor(post(urlEqualTo(csvUploadURLPrefix + type))
                .withBasicAuth(applicationProperties.getOpenmrsAPIUserName(), applicationProperties.getOpenmrsAPIUserPassword())
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK))
        );
    }

    private void scenariosForRefTerms() throws IOException {
        stubFor(post(urlEqualTo(csvUploadURLPrefix + "referenceterms"))
                .inScenario("CSV Upload")
                .whenScenarioStateIs(STARTED)
                .withBasicAuth(applicationProperties.getOpenmrsAPIUserName(), applicationProperties.getOpenmrsAPIUserPassword())
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK))
                .willSetStateTo("REF UPLOAD STARTED")
        );

        stubFor(get(urlEqualTo(csvUploadURLPrefix + "status"))
                .inScenario("CSV Upload")
                .whenScenarioStateIs("REF UPLOAD STARTED")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody("[{\"id\":\"1\",\"errorFileName\":null,\"status\":\"IN_PROGRESS\"}]")
                )
                .willSetStateTo("REF UPLOAD INPROGRESS")
        );
        stubFor(get(urlEqualTo(csvUploadURLPrefix + "status"))
                .inScenario("CSV Upload")
                .whenScenarioStateIs("REF UPLOAD INPROGRESS")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody("[{\"id\":\"1\",\"errorFileName\":null,\"status\":\"COMPLETED\"}]")
                )
                .willSetStateTo("REF UPLOAD FINISHED")
        );
    }

    private void scenariosForConcepts() throws IOException {
        stubFor(post(urlEqualTo(csvUploadURLPrefix + "concept"))
                .inScenario("CSV Upload")
                .whenScenarioStateIs("REF UPLOAD FINISHED")
                .withBasicAuth(applicationProperties.getOpenmrsAPIUserName(), applicationProperties.getOpenmrsAPIUserPassword())
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK))
                .willSetStateTo("CONCEPT UPLOAD STARTED")
        );

        stubFor(get(urlEqualTo(csvUploadURLPrefix + "status"))
                .inScenario("CSV Upload")
                .whenScenarioStateIs("CONCEPT UPLOAD STARTED")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody("[{\"id\":\"1\",\"errorFileName\":null,\"status\":\"COMPLETED\"}]")
                )
                .willSetStateTo("CONCEPT UPLOAD FINISHED")
        );
    }

    private void scenariosForConceptSets() throws IOException {
        stubFor(post(urlEqualTo(csvUploadURLPrefix + "conceptset"))
                .inScenario("CSV Upload")
                .whenScenarioStateIs("CONCEPT UPLOAD FINISHED")
                .withBasicAuth(applicationProperties.getOpenmrsAPIUserName(), applicationProperties.getOpenmrsAPIUserPassword())
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK))
                .willSetStateTo("CONCEPTSET UPLOAD STARTED")
        );

        stubFor(get(urlEqualTo(csvUploadURLPrefix + "status"))
                .inScenario("CSV Upload")
                .whenScenarioStateIs("CONCEPTSET UPLOAD STARTED")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody("[{\"id\":\"1\",\"errorFileName\":null,\"status\":\"COMPLETED\"}]")
                )
                .willSetStateTo("CONCEPTSET UPLOAD INPROGRESS")
        );
    }

}
