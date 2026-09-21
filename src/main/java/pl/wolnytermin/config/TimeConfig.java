package pl.wolnytermin.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cala aplikacja dziala na jednej strefie czasowej (Europe/Warsaw) — zgodnie z zalozeniami MVP.
 * Clock jest beanem, zeby testy silnika dostepnosci mogly podstawic ustalony czas
 * zamiast polegac na zegarze systemowym.
 */
@Configuration
public class TimeConfig {

    public static final ZoneId APP_ZONE = ZoneId.of("Europe/Warsaw");

    @Bean
    public Clock clock() {
        return Clock.system(APP_ZONE);
    }
}
