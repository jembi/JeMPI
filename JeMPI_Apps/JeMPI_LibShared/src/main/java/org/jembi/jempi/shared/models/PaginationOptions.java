package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ObjectUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaginationOptions(
      Integer offset,
      Integer limit,
      String sortBy,
      Boolean sortAsc) {
   public PaginationOptions(
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      this.offset = ObjectUtils.defaultIfNull(offset, 0);
      this.limit = ObjectUtils.defaultIfNull(limit, 10);
      this.sortBy = ObjectUtils.defaultIfNull(sortBy, "uid");
      this.sortAsc = ObjectUtils.defaultIfNull(sortAsc, false);
   }
}
