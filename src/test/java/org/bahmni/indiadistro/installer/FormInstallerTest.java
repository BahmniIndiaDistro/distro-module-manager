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
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

import java.io.File;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.bahmni.indiadistro.installer.FormInstaller.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

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

        ConceptUUIDManager conceptUUIDManager = Mockito.mock(ConceptUUIDManager.class);
        when(conceptUUIDManager.updateUUIDs(anyString())).then(AdditionalAnswers.returnsFirstArg());
        formInstaller = new FormInstaller(conceptUUIDManager, applicationProperties);
    }

    @Test
    public void shouldUploadForm() throws Exception {
        String publishUrl = String.format(IE_PUBLISH_FORM_URL_FORMAT, "xyz");

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
}
