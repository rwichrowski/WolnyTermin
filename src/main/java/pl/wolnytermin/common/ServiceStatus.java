package pl.wolnytermin.common;

/**
 * Pojedyncza wiadomosc w strumieniu statusu — to samo, co zwraca /actuator/health
 * (UP, DOWN, ...), plus moment pomiaru.
 */
public record ServiceStatus(String status, String time) {
}
