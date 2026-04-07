package company.vk.edu.distrib.compute;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@ParameterizedClass
@ArgumentsSource(KVServiceFactoryArgumentsProvider.class)
public class ShardingTest extends TestBase {
    @Parameter
    KVServiceFactory kvServiceFactory;

    @Test
    void insert() {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            int port = randomPort();
            String endpoint = endpoint(port);
            KVService storage = kvServiceFactory.create(port);
            storage.start();
            try {
                String key = randomKey();
                byte[] value = randomValue();

                assertEquals(201, upsert(endpoint, key, value).statusCode());

                final HttpResponse<byte[]> response = get(endpoint, key);
                assertEquals(200, response.statusCode());
                assertArrayEquals(value, response.body());
            } finally {
                storage.stop();
            }
        });
    }

    @Test
    void insertEmpty() {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            int port = randomPort();
            String endpoint = endpoint(port);
            KVService storage = kvServiceFactory.create(port);
            storage.start();
            try {
                final String key = randomKey();
                final byte[] value = new byte[0];

                assertEquals(201, upsert(endpoint, key, value).statusCode());

                final HttpResponse<byte[]> response = get(endpoint, key);
                assertEquals(200, response.statusCode());
                assertArrayEquals(value, response.body());
            } finally {
                storage.stop();
            }
        });
    }

    @Test
    void upsert() {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            int port = randomPort();
            String endpoint = endpoint(port);
            KVService storage = kvServiceFactory.create(port);
            storage.start();
            try {
                final String key = randomKey();
                final byte[] value1 = randomValue();
                final byte[] value2 = randomValue();

                assertEquals(201, upsert(endpoint, key, value1).statusCode());

                assertEquals(201, upsert(endpoint, key, value2).statusCode());

                final HttpResponse<byte[]> response = get(endpoint, key);
                assertEquals(200, response.statusCode());
                assertArrayEquals(value2, response.body());
            } finally {
                storage.stop();
            }
        });
    }

    @Test
    void upsertEmpty() {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            int port = randomPort();
            String endpoint = endpoint(port);
            KVService storage = kvServiceFactory.create(port);
            storage.start();
            try {
                final String key = randomKey();
                final byte[] value = randomValue();
                final byte[] empty = new byte[0];

                assertEquals(201, upsert(endpoint, key, value).statusCode());

                assertEquals(201, upsert(endpoint, key, empty).statusCode());

                final HttpResponse<byte[]> response = get(endpoint, key);
                assertEquals(200, response.statusCode());
                assertArrayEquals(empty, response.body());
            } finally {
                storage.stop();
            }
        });
    }

    @Test
    void lifecycle2keys() {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            int port = randomPort();
            String endpoint = endpoint(port);
            KVService storage = kvServiceFactory.create(port);
            storage.start();
            try {
                final String key1 = randomKey();
                final byte[] value1 = randomValue();
                final String key2 = randomKey();
                final byte[] value2 = randomValue();

                assertEquals(201, upsert(endpoint, key1, value1).statusCode());

                assertArrayEquals(value1, get(endpoint, key1).body());

                assertEquals(201, upsert(endpoint, key2, value2).statusCode());

                assertArrayEquals(value1, get(endpoint, key1).body());
                assertArrayEquals(value2, get(endpoint, key2).body());

                assertEquals(202, delete(endpoint, key1).statusCode());

                assertEquals(404, get(endpoint, key1).statusCode());
                assertArrayEquals(value2, get(endpoint, key2).body());

                assertEquals(202, delete(endpoint, key2).statusCode());

                assertEquals(404, get(endpoint, key2).statusCode());
            } finally {
                storage.stop();
            }
        });
    }

    /*
    @ServiceTest(stage = 3, clusterSize = 2)
    void distribute(List<ServiceInfo> serviceInfos) throws Exception {
        final String key = randomId();
        final byte[] value = randomValue();

        // Insert
        assertEquals(HttpURLConnection.HTTP_CREATED, serviceInfos.get(0).upsert(key, value, 1, 1).statusCode());
        assertEquals(HttpURLConnection.HTTP_CREATED, serviceInfos.get(1).upsert(key, value, 1, 1).statusCode());

        // Stop all
        for (ServiceInfo serviceInfo : serviceInfos) {
            serviceInfo.stop();
        }

        int successCount = 0;
        // Check each
        for (ServiceInfo serviceInfo : serviceInfos) {
            serviceInfo.start();

            HttpResponse<byte[]> response = serviceInfo.get(key, 1, 1);
            if (response.statusCode() == HttpURLConnection.HTTP_OK && Arrays.equals(value, response.body())) {
                successCount++;
            }

            serviceInfo.stop();
        }

        assertEquals(1, successCount);
    }
    * */
}
