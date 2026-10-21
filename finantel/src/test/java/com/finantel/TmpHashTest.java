package com.finantel;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.file.*;

class TmpHashTest {
    @Test
    void generar() throws Exception {
        String hash = new BCryptPasswordEncoder().encode("goku123");
        Files.writeString(Path.of("hash.txt"), hash);
    }
}