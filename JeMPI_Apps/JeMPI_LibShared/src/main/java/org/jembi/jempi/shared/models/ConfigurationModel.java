package org.jembi.jempi.shared.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConfigurationModel(
      Configuration configuration
) {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Configuration(
                        List<Field> auxInteractionFields,
                        List<Field> auxGoldenRecordFields,
                        List<Node> additionalNodes,
                        List<DemographicField> demographicFields,
                        Rules rules) {
        }

        public record Field(
                        String fieldName,
                        String fieldType,
                        Integer csvCol,
                        SourceDetail source,
                        @JsonProperty("default") boolean defaultValue) {
        }

        public record Node(
                        String nodeName,
                        List<Field> fields) {
        }

        public record DemographicField(
                        String fieldName,
                        String fieldType,
                        SourceDetail source,
                        String indexGoldenRecord,
                        String indexInteraction,
                        LinkMetaData linkMetaData) {
        }

        public record SourceDetail(
              int csvCol,
              String interactionField) {
        }

        public record LinkMetaData(
                        String comparison,
                        List<Double> comparisonLevels,
                        double m,
                        double u) {
        }

        public record Rules(
                        LinkRules link) {
        }

        public record Rule(
              List<String> vars,
              String text) {
        }

        public record LinkRules(
                        List<Rule> deterministic,
                        List<Rule> probabilistic) {
        }

}
