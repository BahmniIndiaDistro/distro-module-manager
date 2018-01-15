package org.bahmni.indiadistro.installer;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpStatus;
import org.bahmni.indiadistro.TestUtil;
import org.bahmni.indiadistro.config.ApplicationProperties;
import org.bahmni.indiadistro.model.ConceptResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConceptUUIDUtilTest {
    private static Map<String, String> env;

    @BeforeClass
    public static void setUpClass() {
        env = TestUtil.loadTestProperties();
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldReplaceFormConceptUUIDsWithUUIDsFromServer() throws Exception {
        ApplicationProperties applicationProperties = new ApplicationProperties(env);

        String questionConcept = "QuestionConcept";
        String uuidForQuestionConcept = "UUIDForQuestionConcept";
        setupForConcept(questionConcept, uuidForQuestionConcept, applicationProperties);

        String answerConcept = "AnswerConcept";
        String uuidForAnswerConcept = "UUIDForAnswerConcept";
        setupForConcept(answerConcept, uuidForAnswerConcept, applicationProperties);

        String bodyWeight = "BodyWeight";
        String uuidForBodyWeight = "UUIDForBodyWeight";
        setupForConcept(bodyWeight, uuidForBodyWeight, applicationProperties);

        String initialFormValue = "{\"name\":\"FormWithCodedAnswer\",\"id\":2,\"uuid\":\"ab672f1e-cd55-4bcb-88e0-556d54f99a9b\",\"defaultLocale\":\"en\",\"controls\":[{\"type\":\"obsControl\",\"label\":{\"translationKey\":\"QUESTION_CONCEPT_1\",\"id\":\"1\",\"units\":\"\",\"type\":\"label\",\"value\":\"QuestionConcept\"},\"properties\":{\"mandatory\":false,\"notes\":false,\"addMore\":false,\"hideLabel\":false,\"controlEvent\":false,\"location\":{\"column\":0,\"row\":0},\"autoComplete\":false,\"multiSelect\":false,\"dropDown\":false},\"id\":\"1\",\"concept\":{\"name\":\"QuestionConcept\",\"uuid\":\"e415b059-9c2c-401c-bc65-967357abf639\",\"datatype\":\"Coded\",\"conceptClass\":\"Finding\",\"conceptHandler\":null,\"answers\":[{\"uuid\":\"acd3e749-417c-4e19-a9c1-8c3fd4dc1b6f\",\"name\":{\"display\":\"AnswerConcept\",\"uuid\":\"47e1e757-179a-486b-a1d1-016e71e44d6b\",\"name\":\"AnswerConcept\",\"locale\":\"en\",\"localePreferred\":true,\"conceptNameType\":\"FULLY_SPECIFIED\",\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.33.101/openmrs/ws/rest/v1/concept/acd3e749-417c-4e19-a9c1-8c3fd4dc1b6f/name/47e1e757-179a-486b-a1d1-016e71e44d6b\"},{\"rel\":\"full\",\"uri\":\"http://192.168.33.101/openmrs/ws/rest/v1/concept/acd3e749-417c-4e19-a9c1-8c3fd4dc1b6f/name/47e1e757-179a-486b-a1d1-016e71e44d6b?v=full\"}],\"resourceVersion\":\"1.9\"},\"names\":[{\"display\":\"AnswerConcept\",\"uuid\":\"8577e788-1bcc-4b5d-906f-6b00f5f54346\",\"name\":\"AnswerConcept\",\"locale\":\"en\",\"localePreferred\":false,\"conceptNameType\":\"SHORT\",\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.33.101/openmrs/ws/rest/v1/concept/acd3e749-417c-4e19-a9c1-8c3fd4dc1b6f/name/8577e788-1bcc-4b5d-906f-6b00f5f54346\"},{\"rel\":\"full\",\"uri\":\"http://192.168.33.101/openmrs/ws/rest/v1/concept/acd3e749-417c-4e19-a9c1-8c3fd4dc1b6f/name/8577e788-1bcc-4b5d-906f-6b00f5f54346?v=full\"}],\"resourceVersion\":\"1.9\"},{\"display\":\"AnswerConcept\",\"uuid\":\"47e1e757-179a-486b-a1d1-016e71e44d6b\",\"name\":\"AnswerConcept\",\"locale\":\"en\",\"localePreferred\":true,\"conceptNameType\":\"FULLY_SPECIFIED\",\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.33.101/openmrs/ws/rest/v1/concept/acd3e749-417c-4e19-a9c1-8c3fd4dc1b6f/name/47e1e757-179a-486b-a1d1-016e71e44d6b\"},{\"rel\":\"full\",\"uri\":\"http://192.168.33.101/openmrs/ws/rest/v1/concept/acd3e749-417c-4e19-a9c1-8c3fd4dc1b6f/name/47e1e757-179a-486b-a1d1-016e71e44d6b?v=full\"}],\"resourceVersion\":\"1.9\"}],\"displayString\":\"AnswerConcept\",\"resourceVersion\":\"2.0\",\"translationKey\":\"ANSWER_CONCEPT_1\"}],\"properties\":{\"allowDecimal\":null}},\"units\":null,\"hiNormal\":null,\"lowNormal\":null,\"hiAbsolute\":null,\"lowAbsolute\":null},{\"type\":\"obsControl\",\"label\":{\"translationKey\":\"BODY_WEIGHT_2\",\"id\":\"2\",\"units\":\"(Kg)\",\"type\":\"label\",\"value\":\"BodyWeight\"},\"properties\":{\"mandatory\":false,\"notes\":false,\"addMore\":false,\"hideLabel\":false,\"controlEvent\":false,\"location\":{\"column\":0,\"row\":1},\"abnormal\":false},\"id\":\"2\",\"concept\":{\"name\":\"BodyWeight\",\"uuid\":\"2398f2b3-a1a2-4b0f-8331-dcf249c6de6a\",\"datatype\":\"Numeric\",\"conceptClass\":\"Finding\",\"conceptHandler\":null,\"answers\":[],\"properties\":{\"allowDecimal\":false}},\"units\":\"Kg\",\"hiNormal\":null,\"lowNormal\":null,\"hiAbsolute\":null,\"lowAbsolute\":null}],\"events\":{},\"translationsUrl\":\"/openmrs/ws/rest/v1/bahmniie/form/translations\"}";

        List<Map<String, String>> initialConcepts = JsonPath.read(initialFormValue, "$..concept");
        assertEquals(2, initialConcepts.size());
        assertEquals(questionConcept, initialConcepts.get(0).get("name"));
        assertEquals("e415b059-9c2c-401c-bc65-967357abf639", initialConcepts.get(0).get("uuid"));
        assertEquals(bodyWeight, initialConcepts.get(1).get("name"));
        assertEquals("2398f2b3-a1a2-4b0f-8331-dcf249c6de6a", initialConcepts.get(1).get("uuid"));

        List<List<Map<String, String>>> initialSetMembers = JsonPath.read(initialFormValue, "$..setMembers");
        assertTrue(initialSetMembers.isEmpty());

        List<List<Map<String, Object>>> initialAnswers = JsonPath.read(initialFormValue, "$..answers");
        assertEquals(2, initialAnswers.size());
        assertEquals(1, initialAnswers.get(0).size());
        assertEquals(0, initialAnswers.get(1).size());

        Map<String, String> initialAnswerName = (Map<String, String>) initialAnswers.get(0).get(0).get("name");
        assertEquals(answerConcept, initialAnswerName.get("name"));
        assertEquals("acd3e749-417c-4e19-a9c1-8c3fd4dc1b6f", initialAnswers.get(0).get(0).get("uuid"));

        String updatedFormValue = ConceptUUIDUtil.updateUUIDs(initialFormValue, applicationProperties);

        List<Map<String, String>> updatedConcepts = JsonPath.read(updatedFormValue, "$..concept");
        assertEquals(2, updatedConcepts.size());
        assertEquals(questionConcept, updatedConcepts.get(0).get("name"));
        assertEquals("UUIDForQuestionConcept", updatedConcepts.get(0).get("uuid"));
        assertEquals(bodyWeight, updatedConcepts.get(1).get("name"));
        assertEquals("UUIDForBodyWeight", updatedConcepts.get(1).get("uuid"));

        List<List<Map<String, String>>> updatedSetMembers = JsonPath.read(updatedFormValue, "$..setMembers");
        assertTrue(updatedSetMembers.isEmpty());

        List<List<Map<String, Object>>> updatedAnswers = JsonPath.read(updatedFormValue, "$..answers");
        assertEquals(2, updatedAnswers.size());
        assertEquals(1, updatedAnswers.get(0).size());
        assertEquals(0, updatedAnswers.get(1).size());

        Map<String, String> updatedAnswerName = (Map<String, String>) updatedAnswers.get(0).get(0).get("name");
        assertEquals(answerConcept, updatedAnswerName.get("name"));
        assertEquals("UUIDForAnswerConcept", updatedAnswers.get(0).get(0).get("uuid"));
    }

    private void setupForConcept(String conceptName, String conceptUUID, ApplicationProperties applicationProperties) throws IOException {
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
