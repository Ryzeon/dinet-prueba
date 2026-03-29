package me.ryzeon.dinet.orders.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 16:55</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderLoadControllerSecurityIT {

    private static final String SECRET = "e8bea9d9e2ed68c9ba71b70357fcc29c";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cargarSinJwt_responde401() throws Exception {
        mockMvc.perform(multipart("/pedidos/cargar")
                        .file(csvFile())
                        .header("Idempotency-Key", "chicha"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cargarConJwt_responde200() throws Exception {
        mockMvc.perform(multipart("/pedidos/cargar")
                        .file(csvFile())
                        .header("Idempotency-Key", "chicha")
                        .header("Authorization", "Bearer " + signHs256()))
                .andExpect(status().isOk());
    }

    private static MockMultipartFile csvFile() {
        return new MockMultipartFile(
                "file",
                "pedidos.csv",
                "text/csv",
                "numeropedido,clienteid,fechaentrega,estado,zonaentrega,requiererefrigeracion\n".getBytes());
    }

    private static String signHs256() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("test")
                .expirationTime(new Date(2524608000000L))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(SECRET.getBytes()));
        return jwt.serialize();
    }
}
