package org.bahmni.indiadistro.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.bahmni.indiadistro.config.ApplicationProperties;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class HttpUtil {
    public static CloseableHttpClient createAcceptSelfSignedCertificateClient() {
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

    public static void addBasicAuth(HttpRequest request, ApplicationProperties applicationProperties) throws UnsupportedEncodingException {
        String credentials = String.format("%s:%s", applicationProperties.getOpenmrsAPIUserName(), applicationProperties.getOpenmrsAPIUserPassword());
        byte[] base64Credentials = Base64.encodeBase64(credentials.getBytes("UTF-8"));
        request.addHeader("Authorization", "Basic " + new String(base64Credentials));
    }

    public static String parseContentInputAsString(HttpEntity entity) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder responseString = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            responseString.append(inputLine);
        }
        reader.close();
        return responseString.toString();
    }

}
