package me.ryzeon.dinet.orders.application.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class Sha256HasherTest {

    @Test
    void streamingDigest_matchesByteArrayDigest() throws Exception {
        byte[] data = "numeropedido,clienteid\nP1,C1\n".getBytes(StandardCharsets.UTF_8);
        String fromBytes = Sha256Hasher.hexDigest(data);
        String fromStream;
        try (var in = new ByteArrayInputStream(data)) {
            fromStream = Sha256Hasher.hexDigest(in);
        }
        assertThat(fromStream).isEqualTo(fromBytes);
    }
}
