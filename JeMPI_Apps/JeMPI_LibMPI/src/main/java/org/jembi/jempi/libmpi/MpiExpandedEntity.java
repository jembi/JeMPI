package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.CustomEntity;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiExpandedEntity(CustomEntity patient,
                                List<MpiGoldenRecord> mpiGoldenRecordList) {
}
