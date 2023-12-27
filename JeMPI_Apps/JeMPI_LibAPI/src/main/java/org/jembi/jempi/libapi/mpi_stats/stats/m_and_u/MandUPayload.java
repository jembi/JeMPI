package org.jembi.jempi.libapi.mpi_stats.stats.m_and_u;

import java.util.Map;

public class MandUPayload {
    public record MandUMetadata(String linkerId) { }
    public record MandU(float m, float u) { }
    public record Payload(MandUMetadata metadata, Map<String, MandU> fields) { }
}
