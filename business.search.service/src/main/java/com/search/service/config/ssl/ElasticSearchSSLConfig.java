package com.search.service.config.ssl;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableConfigurationProperties(EsProperties.class)
public class ElasticSearchSSLConfig {

    @Bean
    public RestHighLevelClient initElasticClient() throws KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException {
        SSLContext sslContext = getSslContext();
        RestHighLevelClient client = new RestHighLevelClient(RestClient
                .builder(new HttpHost("elasticsearch-siol-es-http.siolbca-dev.svc.cluster.local", 9200, "https"))
                //port number is given as 443 since its https schema
                .setHttpClientConfigCallback((HttpAsyncClientBuilder httpClientBuilder) -> httpClientBuilder
                        .setSSLContext(sslContext)
                        // 禁用抢先认证的方式
                        .disableAuthCaching()
                        // 设置认证请求头
                        .setDefaultHeaders(getAuthHeaders())
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        // 设置默认的账号密码
                        .setDefaultCredentialsProvider(getCredentialsProvider()))
                .setRequestConfigCallback(
                        requestConfig -> requestConfig.setConnectTimeout(5000).setSocketTimeout(120000)));
        System.out.println("elasticsearch client inited");
        return client;
    }

    private SSLContext getSslContext() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
        KeyStore truststore = getKeyStoreP12();
        SSLContextBuilder sslBuilder = SSLContexts.custom()
                .loadTrustMaterial(truststore, (x509Certificates, s) -> true);
        SSLContext sslContext = sslBuilder.build();
        return sslContext;
    }

    private List<Header> getAuthHeaders() {
        Header[] defaultHeaders =
                new Header[]{new BasicHeader("Authorization",
                        "Bearer u6iuAxZ0RG1Kcm5jVFI4eU4tZU9aVFEwT2F3")};
        return Stream.of(defaultHeaders).collect(Collectors.toList());
    }

    private CredentialsProvider getCredentialsProvider() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "G0D1g6TurJ79pcxr1065pU0U"));
        return credentialsProvider;
    }

    private KeyStore getKeyStoreP12() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        Path trustStorePath = Paths.get("/path/to/truststore.p12");
        KeyStore truststore = KeyStore.getInstance("pkcs12");
        String keyStorePass = null;
        try (InputStream is = Files.newInputStream(trustStorePath)) {
            truststore.load(is, keyStorePass.toCharArray());
        }
        return truststore;
    }

    private RestHighLevelClient initElasticClientP12() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        Path trustStorePath = Paths.get("/path/to/your/truststore.p12");
        Path keyStorePath = Paths.get("/path/to/your/keystore.p12");
        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        String trustStorePass = null;
        String keyStorePass = null;
        try (InputStream is = Files.newInputStream(trustStorePath)) {
            trustStore.load(is, trustStorePass.toCharArray());
        }
        try (InputStream is = Files.newInputStream(keyStorePath)) {
            keyStore.load(is, keyStorePass.toCharArray());
        }
        SSLContextBuilder sslBuilder = SSLContexts.custom()
                .loadTrustMaterial(trustStore, (x509Certificates, s) -> true)
                .loadKeyMaterial(keyStore, keyStorePass.toCharArray());
        final SSLContext sslContext = sslBuilder.build();
        RestHighLevelClient client = new RestHighLevelClient(RestClient
                .builder(new HttpHost("elasticsearch-siol-es-http.siolbca-dev.svc.cluster.local", 9200, "https"))
                //port number is given as 443 since its https schema
                .setHttpClientConfigCallback((HttpAsyncClientBuilder httpClientBuilder) -> httpClientBuilder
                        .setSSLContext(sslContext)
                        // 禁用抢先认证的方式
                        .disableAuthCaching()
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .setDefaultCredentialsProvider(getCredentialsProvider()))
                .setRequestConfigCallback(
                        requestConfig -> requestConfig.setConnectTimeout(5000).setSocketTimeout(120000)));
        System.out.println("elasticsearch client inited");
        return client;
    }

}
