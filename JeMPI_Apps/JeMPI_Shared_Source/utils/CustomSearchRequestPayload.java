package org.jembi.jempi.shared.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomSearchRequestPayload(
      List<SimpleSearchRequestPayload> $or,
      Integer offset,
      Integer limit,
      String sortBy,
      Boolean sortAsc) {

   public CustomSearchRequestPayload(
         final List<SimpleSearchRequestPayload> $or,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      this.$or = ObjectUtils.defaultIfNull($or, new ArrayList<>());
      this.offset = ObjectUtils.defaultIfNull(offset, 0);
      this.limit = ObjectUtils.defaultIfNull(limit, 10);
      this.sortBy = sortBy;
      this.sortAsc = sortAsc;
   }

}
