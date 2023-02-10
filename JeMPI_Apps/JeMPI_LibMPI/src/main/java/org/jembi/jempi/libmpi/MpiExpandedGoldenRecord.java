package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.CustomGoldenRecord;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiExpandedGoldenRecord(CustomGoldenRecord customGoldenRecord,
                                      List<MpiPatient> mpiPatients) {
}
