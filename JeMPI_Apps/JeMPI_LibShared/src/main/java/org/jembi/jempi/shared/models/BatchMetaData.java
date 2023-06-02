package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BatchMetaData(
        FileType fileType,
        String startDateTime,
        String fileName,
        String userName,
        Boolean delayLinker,
        String tag) {

    public BatchMetaData(final FileType type) {
        this(type,
        null,
        null,
        null,
        null,
        null);
    }

    public enum FileType {
        CSV(FileType.CSV_VALUE),
        XLS(FileType.XLS_VALUE);
        public static final int CSV_VALUE = 1;
        public static final int XLS_VALUE = 2;

        public final int type;

        FileType(final int type_) {
            this.type = type_;
        }
    }
}

