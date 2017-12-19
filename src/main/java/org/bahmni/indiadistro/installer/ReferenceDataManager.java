package org.bahmni.indiadistro.installer;


import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.bahmni.indiadistro.config.CSVUploadStatus;
import org.codehaus.jackson.map.ObjectMapper;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.bahmni.indiadistro.ModuleManager.MODULES_DIRECTORY;

public class ReferenceDataManager {
    private static final String referenceTermFilePath = "ref_terms.csv";
    private static final String conceptFilePath = "concepts.csv";
    private static final String conceptSetsFilePath = "concept_sets.csv";
    private static final String bahmniBaseUrl = "https://192.168.33.101/openmrs/ws/rest/v1/bahmnicore";


    public void uploadForModule(String moduleName) {
        File modulesDir = new File(MODULES_DIRECTORY, moduleName);
        uploadRefTerms(modulesDir);
        uploadConcepts(modulesDir);
        uploadConceptSets(modulesDir);
    }

    private void uploadRefTerms(File modulesDir) {
        try {
            uploadAndCheckStatus(modulesDir, referenceTermFilePath,
                    "admin/upload/referenceterms", "REF TERMS");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Problem while uploading the reference terms");
        }
    }

    private void uploadConcepts(File modulesDir) {
        try {
            uploadAndCheckStatus(modulesDir, conceptFilePath, "admin/upload/concept",
                    "Concepts");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Problem while uploading the Concepts");
        }
    }

    private void uploadConceptSets(File modulesDir) {
        try {
            uploadAndCheckStatus(modulesDir, conceptSetsFilePath, "admin/upload/conceptset",
                    "Concept Sets");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Problem while uploading the Concept Sets");
        }
    }

    private void uploadAndCheckStatus(File modulesDir, String filePath, String urlPath, String type) throws IOException, InterruptedException {
        File fileToUpload = new File(modulesDir, filePath);
        String uploadURL = String.format("%s/%s", bahmniBaseUrl, urlPath);

        CloseableHttpClient httpClient = createAcceptSelfSignedCertificateClient();
        HttpPost request = new HttpPost(URI.create(uploadURL));
        addBasicAuth(request);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", fileToUpload);
        HttpEntity fileEntity = builder.build();
        request.setEntity(fileEntity);

        CloseableHttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != statusCode) {
            throw new IOException(String.format("Unexpected Response code %s while uploading %s", statusCode, type));
        }

        Thread.sleep(1000);
        checkIfDone(type);
    }

    private void checkIfDone(String type) throws IOException, InterruptedException {
        String uploadStatusUrl = String.format("%s/%s", bahmniBaseUrl, "admin/upload/status");

        CloseableHttpClient httpClient = createAcceptSelfSignedCertificateClient();
        HttpGet request = new HttpGet(URI.create(uploadStatusUrl));
        addBasicAuth(request);

        CloseableHttpResponse httpResponse = httpClient.execute(request);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != statusCode) {
            throw new IOException(String.format("Unexpected Response code %s while checking status of %s ", statusCode, type));
        }
        String response = parseContentInputAsString(httpResponse.getEntity());
        ObjectMapper objectMapper = new ObjectMapper();
        CSVUploadStatus lastStatus = Arrays.asList(objectMapper.readValue(response, CSVUploadStatus[].class)).get(0);
        if ("IN_PROGRESS".equalsIgnoreCase(lastStatus.getStatus())) {
            System.out.println(String.format("The upload for %s is in progress", type));
            Thread.sleep(5000);
            checkIfDone(type);
        } else if ("COMPLETED_WITH_ERRORS".equalsIgnoreCase(lastStatus.getStatus())) {
            throw new RuntimeException(String.format("Problem while uploading %s. The error file is %s", lastStatus.getErrorFileName(), type));
        } else if ("COMPLETED".equalsIgnoreCase(lastStatus.getStatus())) {
            System.out.println(String.format("Upload for %s finished", type));
        }
    }

    private void addBasicAuth(HttpRequest request) throws UnsupportedEncodingException {
        String credentials = "superman" + ":" + "Admin123";
        byte[] base64Credentials = Base64.encodeBase64(credentials.getBytes("UTF-8"));
        request.addHeader("Authorization", "Basic " + new String(base64Credentials));
    }

    private String parseContentInputAsString(HttpEntity entity) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder responseString = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            responseString.append(inputLine);
        }
        reader.close();
        return responseString.toString();
    }

    private CloseableHttpClient createAcceptSelfSignedCertificateClient() {
        TrustSelfSignedStrategy trustStrategy = new TrustSelfSignedStrategy();
        SSLContext sslContext;
        try {
            sslContext = SSLContextBuilder.create().loadTrustMaterial(trustStrategy).build();
        } catch (Exception e) {
            throw new RuntimeException("Problem creating connection for CSV Upload");
        }
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        return HttpClients
                .custom()
                .setSSLSocketFactory(connectionFactory)
                .build();
    }
}