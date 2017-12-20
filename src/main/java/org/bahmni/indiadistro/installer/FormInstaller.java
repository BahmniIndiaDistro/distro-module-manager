package org.bahmni.indiadistro.installer;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.bahmni.indiadistro.model.BahmniForm;
import org.bahmni.indiadistro.model.BahmniFormResource;
import org.bahmni.indiadistro.model.FormTranslation;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.bahmni.indiadistro.ModuleManager.MODULES_DIRECTORY;
import static org.bahmni.indiadistro.util.HttpUtil.*;

public class FormInstaller {
    private static final String formDirectory = "forms";
    private static final String bahmniBaseURL = "https://localhost";
    private static final String openmrsFormURL = "/openmrs/ws/rest/v1/form";
    private static final String ieSaveFormURL = "/openmrs/ws/rest/v1/bahmniie/form/save";
    private static final String ieSaveTranslationsURL = "/openmrs/ws/rest/v1/bahmniie/form/saveTranslation";
    private final ObjectMapper objectMapper;

    public FormInstaller() {
        objectMapper = new ObjectMapper();
    }


    public void installForModule(String moduleName) {
        File moduleDirectory = new File(MODULES_DIRECTORY, moduleName);
        File formsDirectory = new File(moduleDirectory, formDirectory);

        File[] files = formsDirectory.listFiles();
        for (File file : files) {
            try {
                uploadForm(file);
            } catch (IOException e) {
                //log error
                System.out.println(String.format("Problem while uploading file %s as form", file.getName()));
            }
        }
    }

    private void uploadForm(File jsonFile) throws IOException {
        Map<String, Object> formData = objectMapper.readValue(jsonFile,
                new TypeReference<Map<String, Object>>() {
                });

        Map<String, Object> formJson = (Map) formData.get("formJson");
        FormTranslation[] translations = objectMapper.readValue((String) formData.get("translations"), FormTranslation[].class);

        String formName = (String) formJson.get("name");
        List<Map<String, Object>> resources = (List<Map<String, Object>>) formJson.get("resources");
        Map<String, Object> value = objectMapper.readValue((String) resources.get(0).get("value"), Map.class);

        String uuid = uploadFormMetadata(formName);
        BahmniFormResource formSaveResponse = uploadFormResource(formName, value, uuid);
        uploadFormTranslations(translations, formSaveResponse);
    }

    private String uploadFormMetadata(String formName) {
        System.out.println("Uploading form metadata");
        try {
            BahmniForm form = new BahmniForm(formName, "1", false);
            String formMetadataSaveResponse = postJSON(form, openmrsFormURL);
            return (String) objectMapper.readValue(formMetadataSaveResponse, Map.class).get("uuid");
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem while uploading form metadata for %s", formName), e);
        }
    }

    private BahmniFormResource uploadFormResource(String formName, Map<String, Object> value, String uuid) {
        try {
            System.out.println("Uploading form resource");
            value.put("uuid", uuid);

            BahmniForm bahmniForm = new BahmniForm();
            bahmniForm.setName(formName);
            bahmniForm.setUuid(uuid);

            BahmniFormResource formResource = new BahmniFormResource();
            formResource.setForm(bahmniForm);
            formResource.setValue(objectMapper.writeValueAsString(value));
            formResource.setUuid("");

            String formSaveResponse = postJSON(formResource, ieSaveFormURL);
            return objectMapper.readValue(formSaveResponse, BahmniFormResource.class);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem while uploading form metadata for %s", formName), e);
        }
    }

    private void uploadFormTranslations(FormTranslation[] translations, BahmniFormResource formSaveResponse) {
        System.out.println("Uploading form translations");
        Stream.of(translations).forEach(formTranslation -> {
            String version = formSaveResponse.getForm().getVersion();
            if (StringUtils.isNotBlank(version)) {
                formTranslation.setVersion(version);
            }
        });
        try {
            postJSON(translations, ieSaveTranslationsURL);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem while uploading form metadata for %s", formSaveResponse.getForm().getName()), e);
        }
    }

    private String postJSON(Object payload, String urlPath) throws IOException {
        CloseableHttpClient httpClient = createAcceptSelfSignedCertificateClient();
        HttpPost request = new HttpPost(URI.create(String.format("%s%s", bahmniBaseURL, urlPath)));
        addBasicAuth(request);
        request.addHeader("Content-Type", "application/json");
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(payload), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        CloseableHttpResponse httpResponse = httpClient.execute(request);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String responseText = parseContentInputAsString(httpResponse.getEntity());
        if (HttpStatus.SC_CREATED != statusCode) {
            throw new IOException(String.format("Unexpected Response code %s while uploading the form with message %s", statusCode, responseText));
        }
        return responseText;
    }

}
