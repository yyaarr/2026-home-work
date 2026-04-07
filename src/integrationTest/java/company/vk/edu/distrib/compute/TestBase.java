package company.vk.edu.distrib.compute;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Contains utility methods for unit tests.
 *
 */
abstract class TestBase {
    private static final int VALUE_LENGTH = 1024;

    public static final Duration TIMEOUT = Duration.ofSeconds(5);

    static int randomPort() {
        return ThreadLocalRandom.current().nextInt(30000, 40000);
    }

    static String randomKey() {
        return Long.toHexString(ThreadLocalRandom.current().nextLong());
    }

    static byte[] randomValue() {
        final byte[] result = new byte[VALUE_LENGTH];
        ThreadLocalRandom.current().nextBytes(result);
        return result;
    }

    static String endpoint(int port) {
        return "http://localhost:" + port;
    }

    static String url(String endpoint, String id) {
        return endpoint + "/v0/entity?id=" + id;
    }

    protected abstract HttpClient getHttpClient();

    protected HttpResponse<byte[]> get(String endpoint, String key)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI(url(endpoint, key)))
                .timeout(Duration.ofSeconds(2))
                .build();
        return getHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    protected HttpResponse<Void> delete(String endpoint, String key)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(new URI(url(endpoint, key)))
                .timeout(TIMEOUT)
                .build();
        return getHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
    }

    protected HttpResponse<Void> upsert(String endpoint, String key, byte[] data)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofByteArray(data))
                .uri(new URI(url(endpoint, key)))
                .timeout(TIMEOUT)
                .build();
        return getHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
    }
}
