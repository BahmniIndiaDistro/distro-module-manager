package org.bahmni.indiadistro.installer;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JsonProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.bahmni.indiadistro.config.ApplicationProperties;
import org.bahmni.indiadistro.model.ConceptResponse;
import org.bahmni.indiadistro.util.StringUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.bahmni.indiadistro.util.HttpUtil.*;

public class ConceptUUIDManager {
    private static final String CONCEPT_DETAILS_URL_FORMAT = "%s/openmrs/ws/rest/v1/concept?q=%s&source=byFullySpecifiedName&v=custom:(uuid,name:(name))";
    private ApplicationProperties applicationProperties;

    public ConceptUUIDManager(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public String updateUUIDs(String formValue) {
        JsonProvider jsonProvider = Configuration.defaultConfiguration().jsonProvider();
        Object document = jsonProvider.parse(formValue);

        List<Map<String, Object>> concepts = JsonPath.read(document, "$..concept");
        List<List<Map<String, Object>>> setMembers = JsonPath.read(document, "$..setMembers");
        List<List<Map<String, Object>>> conceptAnswers = JsonPath.read(document, "$..answers");

        concepts.forEach(concept -> updateUuidFor(concept, applicationProperties));
        setMembers.forEach(setMember -> setMember.forEach(member -> {
            updateUuidFor(member, applicationProperties);
        }));
        conceptAnswers.forEach(conceptAnswer -> conceptAnswer.forEach(answer -> {
            updateUuidFor(answer, applicationProperties);
        }));

        return jsonProvider.toJson(document);
    }

    private static void updateUuidFor(Map<String, Object> concept, ApplicationProperties applicationProperties) {
        String name;
        Object nameObject = concept.get("name");
        if (nameObject instanceof String) {
            //in case of concept name is string
            name = (String) nameObject;
        } else {
            //in case of answers name is a hashmap
            Map<String, String> nameMap = (Map<String, String>) nameObject;
            name = nameMap.get("name");
        }

        try {
            String encodedName = StringUtils.replaceAll(name, " ", "%20");
            String baseURL = StringUtil.removeSuffix(applicationProperties.getOpenmrsBaseURL(), "/");
            String conceptDetailURL = String.format(CONCEPT_DETAILS_URL_FORMAT, baseURL, encodedName);

            CloseableHttpClient httpClient = createAcceptSelfSignedCertificateClient();
            HttpGet request = new HttpGet(URI.create(conceptDetailURL));
            addBasicAuth(request, applicationProperties);

            CloseableHttpResponse httpResponse = httpClient.execute(request);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK != statusCode) {
                throw new RuntimeException(String.format("Unexpected Response code %s while updating uuid for concept %s ", statusCode, name));
            }

            String response = parseContentInputAsString(httpResponse.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<ConceptResponse>> results = objectMapper.readValue(response,
                    new TypeReference<Map<String, List<ConceptResponse>>>() {
                    });

            ConceptResponse conceptResponse = results.get("results").get(0);
            if (name.equals(StringUtils.trim(conceptResponse.getName().get("name")))) {
                concept.put("uuid", conceptResponse.getUuid());
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can not update UUID for the concept %s", name), e);
        }
    }

}
