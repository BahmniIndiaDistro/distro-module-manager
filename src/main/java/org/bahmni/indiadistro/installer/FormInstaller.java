package org.bahmni.indiadistro.installer;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.indiadistro.config.ApplicationProperties;
import org.bahmni.indiadistro.model.BahmniForm;
import org.bahmni.indiadistro.model.BahmniFormResource;
import org.bahmni.indiadistro.util.StringUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.bahmni.indiadistro.util.HttpUtil.*;

public class FormInstaller {
    private static final String FORM_DIRECTORY = "forms";
    static final String OPENMRS_FORM_URL = "/openmrs/ws/rest/v1/form";
    static final String IE_SAVE_FORM_URL = "/openmrs/ws/rest/v1/bahmniie/form/save";
    static final String IE_SAVE_TRANSLATIONS_URL = "/openmrs/ws/rest/v1/bahmniie/form/saveTranslation";
    static final String IE_PUBLISH_FORM_URL_FORMAT = "/openmrs/ws/rest/v1/bahmniie/form/publish?formUuid=%s";

    private final ObjectMapper objectMapper;
    private ApplicationProperties applicationProperties;
    private static final Logger logger = LogManager.getLogger(FormInstaller.class);

    public FormInstaller(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        objectMapper = new ObjectMapper();
    }


    public void installForModule(String moduleName) {
        String message = String.format("Installing Forms for %s", moduleName);
        System.out.println(message);
        logger.info(message);
        File moduleDirectory = new File(applicationProperties.getIndiaDistroModulesDir(), moduleName);
        File formsDirectory = new File(moduleDirectory, FORM_DIRECTORY);

        File[] files = formsDirectory.listFiles();
        if (null == files) return;
        for (File file : files) {
            try {
                String debugMessage = String.format("Starting upload for file %s", file.getName());
                System.out.println(debugMessage);
                logger.debug(debugMessage);
                uploadForm(file);
            } catch (IOException e) {
                String errorMessage = String.format("Problem while uploading file %s as form", file.getName());
                logger.error(errorMessage);
                System.out.println(errorMessage);
            }
        }
    }

    private void uploadForm(File jsonFile) throws IOException {
        Map<String, Object> formData = objectMapper.readValue(jsonFile,
                new TypeReference<Map<String, Object>>() {
                });

        Map<String, Object> formJson = (Map) formData.get("formJson");
        List<Map<String, Object>> translations = (List<Map<String, Object>>) formData.get("translations");

        String formName = (String) formJson.get("name");
        List<Map<String, Object>> resources = (List<Map<String, Object>>) formJson.get("resources");
        Map<String, Object> value = objectMapper.readValue((String) resources.get(0).get("value"), Map.class);

        String uuid = uploadFormMetadata(formName);
        BahmniFormResource formSaveResponse = uploadFormResource(formName, value, uuid);
        uploadFormTranslations(translations, formSaveResponse);
        publishForm(formName, uuid);
    }

    private String uploadFormMetadata(String formName) {
        String message = "Uploading form metadata";
        System.out.println(message);
        logger.debug(message);
        try {
            BahmniForm form = new BahmniForm(formName, "1", false);
            String formMetadataSaveResponse = postToURL(form, OPENMRS_FORM_URL, HttpStatus.SC_CREATED);
            return (String) objectMapper.readValue(formMetadataSaveResponse, Map.class).get("uuid");
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem while uploading form metadata for %s", formName), e);
        }
    }

    private BahmniFormResource uploadFormResource(String formName, Map<String, Object> value, String uuid) {
        try {
            String message = "Uploading form resource";
            System.out.println(message);
            logger.debug(message);
            value.put("uuid", uuid);

            BahmniForm bahmniForm = new BahmniForm();
            bahmniForm.setName(formName);
            bahmniForm.setUuid(uuid);

            BahmniFormResource formResource = new BahmniFormResource();
            formResource.setForm(bahmniForm);
            formResource.setValue(objectMapper.writeValueAsString(value));
            formResource.setUuid("");
            String formSaveResponse = postToURL(formResource, IE_SAVE_FORM_URL, HttpStatus.SC_OK);
            return objectMapper.readValue(formSaveResponse, BahmniFormResource.class);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem while uploading form metadata for %s", formName), e);
        }
    }

    private void uploadFormTranslations(List<Map<String, Object>> translations, BahmniFormResource formSaveResponse) {
        String message = "Uploading form translations";
        logger.debug(message);
        System.out.println(message);
        translations.forEach(formTranslation -> {
            String version = formSaveResponse.getForm().getVersion();
            if (StringUtils.isNotBlank(version)) {
                formTranslation.put("version", version);
            }
        });
        try {
            postToURL(translations, IE_SAVE_TRANSLATIONS_URL, HttpStatus.SC_OK);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem while uploading form metadata for %s", formSaveResponse.getForm().getName()), e);
        }
    }

    private void publishForm(String formName, String uuid) {
        String message = "Publishing form";
        logger.debug(message);
        System.out.println(message);
        String iePublishFormURL = String.format(IE_PUBLISH_FORM_URL_FORMAT, uuid);
        try {
            postToURL(null, iePublishFormURL, HttpStatus.SC_OK);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem while publishing form %s", formName), e);
        }
    }

    private String postToURL(Object payload, String urlPath, int expectedCode) throws IOException {
        CloseableHttpClient httpClient = createAcceptSelfSignedCertificateClient();
        HttpPost request = new HttpPost(URI.create(formatURL(urlPath)));
        addBasicAuth(request, applicationProperties);
        if (null != payload) {
            request.addHeader("Content-Type", "application/json");
            StringEntity entity = new StringEntity(objectMapper.writeValueAsString(payload), ContentType.APPLICATION_JSON);
            request.setEntity(entity);
        }

        CloseableHttpResponse httpResponse = httpClient.execute(request);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String responseText = parseContentInputAsString(httpResponse.getEntity());
        if (expectedCode != statusCode) {
            throw new IOException(String.format("Unexpected Response code %s while uploading the form with message %s", statusCode, responseText));
        }
        return responseText;
    }

    private String formatURL(String urlPath) {
        return String.format("%s%s",
                StringUtil.ensureSuffix(applicationProperties.getOpenmrsBaseURL(), "/"),
                StringUtil.removePrefix(urlPath, "/"));
    }

}
