package pl.wolnytermin.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import pl.wolnytermin.config.TimeConfig;

class PingControllerTest {

    @Test
    void ping_zwraca_czas_z_wstrzyknietego_zegara() {
        Clock fixed = Clock.fixed(Instant.parse("2026-01-15T09:00:00Z"), TimeConfig.APP_ZONE);

        var response = new PingController(fixed).ping();

        assertThat(response).containsEntry("status", "ok");
        // 09:00 UTC w styczniu to 10:00 czasu warszawskiego
        assertThat(response.get("time")).startsWith("2026-01-15T10:00");
    }
}
