package pl.wolnytermin.common;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Strumien statusu aplikacji w formacie Server-Sent Events.
 * Polaczenie zostaje otwarte, a serwer co kilka sekund dopisuje zdarzenie "status".
 */
@RestController
@RequestMapping("/api/status")
public class ServiceStatusController {

    private final ServiceStatusBroadcaster broadcaster;

    public ServiceStatusController(ServiceStatusBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return broadcaster.subscribe();
    }
}
