package pl.wolnytermin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Wlacza obsluge @Scheduled — bez tego adnotacje na metodach sa ignorowane.
 * Na razie korzysta z tego strumien statusu (ServiceStatusBroadcaster).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
