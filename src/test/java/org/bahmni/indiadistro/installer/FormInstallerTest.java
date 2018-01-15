package org.bahmni.indiadistro.installer;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.bahmni.indiadistro.TestUtil;
import org.bahmni.indiadistro.config.ApplicationProperties;
import org.bahmni.indiadistro.model.BahmniForm;
import org.bahmni.indiadistro.model.BahmniFormResource;
import org.bahmni.indiadistro.model.ConceptResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.bahmni.indiadistro.installer.FormInstaller.*;

public class FormInstallerTest {
    private ApplicationProperties applicationProperties;
    private FormInstaller formInstaller;
    private static Map<String, String> env;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @BeforeClass
    public static void setUpClass() {
        env = TestUtil.loadTestProperties();
    }

    @Before
    public void setUp() throws Exception {
        File src = temporaryFolder.newFolder("src");
        temporaryFolder.newFolder("src", "htn", "forms");
        File file = temporaryFolder.newFile("src/htn/forms/forms.json");
        String formContent = Resources.toString(Resources.getResource("form.json"), Charsets.UTF_8);
        FileUtils.writeStringToFile(file, formContent, Charsets.UTF_8);

        env.put("INDIA_DISTRO_MODULES_DIR", src.getAbsolutePath());
        applicationProperties = new ApplicationProperties(env);

        formInstaller = new FormInstaller(applicationProperties);
    }

    @Test
    public void shouldUploadForm() throws Exception {
        String publishUrl = String.format(IE_PUBLISH_FORM_URL_FORMAT, "xyz");

        String questionConcept = "QuestionConcept";
        String uuidForQuestionConcept = "UUIDForQuestionConcept";
        stubForConceptUUID(questionConcept, uuidForQuestionConcept, applicationProperties);

        String answerConcept = "AnswerConcept";
        String uuidForAnswerConcept = "UUIDForAnswerConcept";
        stubForConceptUUID(answerConcept, uuidForAnswerConcept, applicationProperties);

        String bodyWeight = "BodyWeight";
        String uuidForBodyWeight = "UUIDForBodyWeight";
        stubForConceptUUID(bodyWeight, uuidForBodyWeight, applicationProperties);

        BahmniForm form = new BahmniForm("foo", "1", false);
        BahmniFormResource bahmniFormResource = new BahmniFormResource();
        bahmniFormResource.setForm(form);
        String saveFormResponse = new ObjectMapper().writeValueAsString(bahmniFormResource);

        setupJSONStub(OPENMRS_FORM_URL, HttpStatus.SC_CREATED, "{\"uuid\": \"xyz\"}");
        setupJSONStub(IE_SAVE_FORM_URL, HttpStatus.SC_OK, saveFormResponse);
        setupJSONStub(IE_SAVE_TRANSLATIONS_URL, HttpStatus.SC_OK, "");
        setupJSONStub(publishUrl, HttpStatus.SC_OK, "");

        formInstaller.installForModule("htn");

        verify(1, postRequestedFor(urlEqualTo(OPENMRS_FORM_URL)));
        verify(1, postRequestedFor(urlEqualTo(IE_SAVE_FORM_URL)));
        verify(1, postRequestedFor(urlEqualTo(IE_SAVE_TRANSLATIONS_URL)));
        verify(1, postRequestedFor(urlEqualTo(publishUrl)));

    }

    private void setupJSONStub(String url, int status, String responseBody) {
        stubFor(post(urlEqualTo(url))
                .withBasicAuth(applicationProperties.getOpenmrsAPIUserName(), applicationProperties.getOpenmrsAPIUserPassword())
                .willReturn(
                        aResponse().withStatus(status)
                                .withBody(responseBody)
                )
        );
    }

    private void stubForConceptUUID(String conceptName, String conceptUUID, ApplicationProperties applicationProperties) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ConceptResponse conceptResponse = new ConceptResponse(conceptName, conceptUUID);
        HashMap<String, Object> map = new HashMap<>();
        map.put("results", Arrays.asList(conceptResponse));

        String urlFormat = "/openmrs/ws/rest/v1/concept?q=%s&source=byFullySpecifiedName&v=custom:(uuid,name:(name))";
        stubFor(get(urlEqualTo(String.format(urlFormat, conceptName)))
                .withBasicAuth(applicationProperties.getOpenmrsAPIUserName(), applicationProperties.getOpenmrsAPIUserPassword())
                .willReturn(
                        aResponse().withStatus(HttpStatus.SC_OK)
                                .withBody(objectMapper.writeValueAsString(map))
                )
        );
    }
}
