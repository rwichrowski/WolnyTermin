package pl.wolnytermin.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.time.Clock;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pl.wolnytermin.config.TimeConfig;

class ServiceStatusControllerTest {

    private final HealthEndpoint healthEndpoint = mock(HealthEndpoint.class);
    private final Clock fixed = Clock.fixed(Instant.parse("2026-01-15T09:00:00Z"), TimeConfig.APP_ZONE);
    private final ServiceStatusBroadcaster broadcaster = new ServiceStatusBroadcaster(healthEndpoint, fixed);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ServiceStatusController(broadcaster))
            .build();

    @Test
    void po_podlaczeniu_od_razu_wysyla_status_a_potem_kolejne_zmiany() throws Exception {
        when(healthEndpoint.health()).thenReturn(Health.up().build());

        MvcResult result = mvc.perform(get("/api/status/stream"))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("event:status")
                .contains("\"status\":\"UP\"")
                .contains("2026-01-15T10:00");

        when(healthEndpoint.health()).thenReturn(Health.down().build());
        broadcaster.broadcast();

        assertThat(result.getResponse().getContentAsString()).contains("\"status\":\"DOWN\"");
    }
}
