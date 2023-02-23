package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BatchMetaData(
        @JsonProperty("startDateTime") String startDateTime,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("userName") String userName,
        @JsonProperty("delayLinker") Boolean delayLinker,
        @JsonProperty("tag") String tag,
        @JsonProperty("fileType") FileType fileType) {

    public BatchMetaData(final FileType type) {
        this(type);
    }

    public enum FileType {
        CSV(FileType.CSV),
        XLS(FileType.XLS),
        public static final int CSV = 1;
        public static final int XLS = 2;

        public final int type;

        FileType(final int type_) {
            this.type = type_;
        }
    }

}

