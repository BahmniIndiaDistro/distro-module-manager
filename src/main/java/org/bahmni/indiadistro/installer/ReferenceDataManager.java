package org.bahmni.indiadistro.installer;


import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.indiadistro.config.ApplicationProperties;
import org.bahmni.indiadistro.model.CSVUploadStatus;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static org.bahmni.indiadistro.util.HttpUtil.*;
import static org.bahmni.indiadistro.util.StringUtil.removePrefix;
import static org.bahmni.indiadistro.util.StringUtil.removeSuffix;

public class ReferenceDataManager {
    private static final String REFERENCE_DATA_DIRECTORY = "reference-data";
    private static final String REFERENCE_TERM_FILE_PATH = "reference_terms.csv";
    private static final String CONCEPT_FILE_PATH = "concepts.csv";
    private static final String CONCEPT_SETS_FILE_PATH = "concept_sets.csv";
    private static final String DRUGS_FILE_PATH = "drugs.csv";

    private static final String BAHMNI_CORE_CONTEXT_PATH = "openmrs/ws/rest/v1/bahmnicore";
    private static final String REF_TERM_UPLOAD_URL_PATH = "admin/upload/referenceterms";
    private static final String CONCEPT_UPLOAD_URL_PATH = "admin/upload/concept";
    private static final String CONCEPT_SET_UPLOAD_URL_PATH = "admin/upload/conceptset";
    private static final String DRUG_UPLOAD_URL_PATH = "admin/upload/drug";
    private static final String UPLOAD_STATUS_URL_PATH = "admin/upload/status";

    private ApplicationProperties applicationProperties;

    private static final Logger logger = LogManager.getLogger(ReferenceDataManager.class);

    public ReferenceDataManager(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void uploadForModule(String moduleName) {
        String message = String.format("Installing Reference Data for %s", moduleName);
        System.out.println(message);
        logger.info(message);
        File moduleDirectory = new File(applicationProperties.getIndiaDistroModulesDir(), moduleName);
        File refDataDirectory = new File(moduleDirectory, REFERENCE_DATA_DIRECTORY);

        uploadRefTerms(refDataDirectory);
        uploadConcepts(refDataDirectory);
        uploadConceptSets(refDataDirectory);
        uploadDrugs(refDataDirectory);
    }

    private void uploadRefTerms(File modulesDir) {
        try {
            uploadAndCheckStatus(modulesDir, REFERENCE_TERM_FILE_PATH, REF_TERM_UPLOAD_URL_PATH, "REF TERMs");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Problem while uploading the reference terms", e);
        }
    }

    private void uploadConcepts(File modulesDir) {
        try {
            uploadAndCheckStatus(modulesDir, CONCEPT_FILE_PATH, CONCEPT_UPLOAD_URL_PATH, "Concepts");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Problem while uploading the Concepts", e);
        }
    }

    private void uploadConceptSets(File modulesDir) {
        try {
            uploadAndCheckStatus(modulesDir, CONCEPT_SETS_FILE_PATH, CONCEPT_SET_UPLOAD_URL_PATH, "Concept Sets");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Problem while uploading the Concept Sets", e);
        }
    }

    private void uploadDrugs(File modulesDir) {
        try {
            if (!new File(modulesDir, DRUGS_FILE_PATH).exists()){
                String message = "No Drugs to upload";
                System.out.println(message);
                logger.debug(message);
                return;
            }
            uploadAndCheckStatus(modulesDir, DRUGS_FILE_PATH, DRUG_UPLOAD_URL_PATH, "Drug");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Problem while uploading the Concept Sets", e);
        }
    }

    private void uploadAndCheckStatus(File modulesDir, String filePath, String urlPath, String type) throws IOException, InterruptedException {
        File fileToUpload = new File(modulesDir, filePath);
        String uploadURL = formatURL(urlPath);

        CloseableHttpClient httpClient = createAcceptSelfSignedCertificateClient();
        HttpPost request = new HttpPost(URI.create(uploadURL));
        addBasicAuth(request, applicationProperties);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", fileToUpload);
        HttpEntity fileEntity = builder.build();
        request.setEntity(fileEntity);

        CloseableHttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != statusCode) {
            throw new IOException(String.format("Unexpected Response code %s while uploading %s", statusCode, type));
        }

        Thread.sleep(applicationProperties.getWaitIntervalForCSVUpload());
        checkIfDone(type);
    }

    private void checkIfDone(String type) throws IOException, InterruptedException {
        String uploadStatusUrl = formatURL(UPLOAD_STATUS_URL_PATH);

        CloseableHttpClient httpClient = createAcceptSelfSignedCertificateClient();
        HttpGet request = new HttpGet(URI.create(uploadStatusUrl));
        addBasicAuth(request, applicationProperties);

        CloseableHttpResponse httpResponse = httpClient.execute(request);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != statusCode) {
            throw new IOException(String.format("Unexpected Response code %s while checking status of %s ", statusCode, type));
        }
        String response = parseContentInputAsString(httpResponse.getEntity());
        ObjectMapper objectMapper = new ObjectMapper();
        CSVUploadStatus lastStatus = Arrays.asList(objectMapper.readValue(response, CSVUploadStatus[].class)).get(0);
        if ("IN_PROGRESS".equalsIgnoreCase(lastStatus.getStatus())) {
            String message = String.format("The upload for %s is in progress", type);
            System.out.println(message);
            logger.debug(message);
            Thread.sleep(applicationProperties.getWaitIntervalForCSVUpload());
            checkIfDone(type);
        } else if ("COMPLETED_WITH_ERRORS".equalsIgnoreCase(lastStatus.getStatus())) {
            throw new RuntimeException(String.format("Problem while uploading %s. The error file is %s", type, lastStatus.getErrorFileName()));
        } else if ("COMPLETED".equalsIgnoreCase(lastStatus.getStatus())) {
            String message = String.format("Upload for %s finished", type);
            logger.debug(message);
            System.out.println(message);
        }
    }

    private String formatURL(String urlPath) {
        return String.format("%s/%s/%s",
                removeSuffix(applicationProperties.getOpenmrsBaseURL(), "/"),
                removePrefix(removeSuffix(BAHMNI_CORE_CONTEXT_PATH, "/"), "/"),
                removePrefix(urlPath, "/"));
    }
}
