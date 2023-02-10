package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.CustomPatient;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiExpandedPatient(CustomPatient patient,
                                 List<MpiGoldenRecord> mpiGoldenRecords) {
}
