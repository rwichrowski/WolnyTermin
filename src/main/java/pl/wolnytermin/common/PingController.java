package pl.wolnytermin.common;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tymczasowy endpoint — potwierdza, ze aplikacja wstala i odpowiada.
 * Do usuniecia, gdy pojawia sie prawdziwe kontrolery.
 */
@RestController
@RequestMapping("/api")
public class PingController {

    private final Clock clock;

    public PingController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of(
                "status", "ok",
                "time", OffsetDateTime.now(clock).toString());
    }
}
