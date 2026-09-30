package pl.wolnytermin.common;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Trzyma otwarte polaczenia SSE i co zadany interwal rozsyla do nich status aplikacji.
 *
 * Status liczymy raz na takt i wysylamy wszystkim — a nie osobna petla per klient —
 * zeby 100 otwartych kart nie oznaczalo 100 zapytan do bazy w health checku.
 */
@Component
public class ServiceStatusBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(ServiceStatusBroadcaster.class);

    /**
     * Po tym czasie serwer zamyka polaczenie. Przegladarkowy EventSource sam sie wtedy
     * polaczy ponownie, a my nie trzymamy w nieskonczonosc polaczen, ktore mogly umrzec
     * po cichu (np. klient stracil siec).
     */
    static final Duration EMITTER_TIMEOUT = Duration.ofMinutes(30);

    private final HealthEndpoint healthEndpoint;
    private final Clock clock;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public ServiceStatusBroadcaster(HealthEndpoint healthEndpoint, Clock clock) {
        this.healthEndpoint = healthEndpoint;
        this.clock = clock;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT.toMillis());
        emitter.onCompletion(() -> emitters.remove(emitter));
        // Timeout trzeba domknac jawnie. Bez complete() Spring potraktuje go jako
        // AsyncRequestTimeoutException i przy juz wyslanej odpowiedzi zaloguje WARN
        // przy kazdym wygasnieciu polaczenia. complete() wywola tez onCompletion.
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitters.remove(emitter));
        emitters.add(emitter);

        // Pierwszy status od razu, zeby klient nie czekal pelnego interwalu na cokolwiek.
        send(emitter, currentStatus());
        return emitter;
    }

    @Scheduled(fixedRateString = "${wolnytermin.status-stream.interval:5s}")
    public void broadcast() {
        if (emitters.isEmpty()) {
            return;
        }
        ServiceStatus status = currentStatus();
        emitters.forEach(emitter -> send(emitter, status));
    }

    ServiceStatus currentStatus() {
        String status = healthEndpoint.health().getStatus().getCode();
        return new ServiceStatus(status, OffsetDateTime.now(clock).toString());
    }

    private void send(SseEmitter emitter, ServiceStatus status) {
        try {
            emitter.send(SseEmitter.event()
                    .name("status")
                    .data(status, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            // Klient sie rozlaczyl. Nie wolamy completeWithError — kontener servletow
            // sam domknie polaczenie; wystarczy przestac do niego pisac.
            log.debug("Rozlaczony klient SSE: {}", e.getMessage());
            emitters.remove(emitter);
        }
    }
}
