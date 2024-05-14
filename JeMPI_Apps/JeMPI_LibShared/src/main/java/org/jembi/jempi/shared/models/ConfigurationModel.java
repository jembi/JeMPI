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
                        List<Field> uniqueInteractionFields,
                        List<Field> uniqueGoldenRecordFields,
                        List<Node> additionalNodes,
                        List<DemographicField> demographicFields,
                        Rules rules) {
        }

        public record Field(
                        String fieldName,
                        String fieldType,
                        Integer csvCol,
                        String source) {
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

        public record SourceDetail(int csvCol) {
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

        public record LinkRules(
                        DeterministicLink deterministic,
                        ProbabilisticLink probabilistic) {
        }

        public record DeterministicLink(
                        @JsonProperty("QUERY_LINK_DETERMINISTIC_A") QueryLink queryLinkDeterministicA,
                        @JsonProperty("QUERY_LINK_DETERMINISTIC_B") QueryLink queryLinkDeterministicB) {
        }

        public record ProbabilisticLink(
                        @JsonProperty("QUERY_LINK_PROBABILISTIC") QueryLink queryLinkProbabilistic) {
        }

        public record QueryLink(
                        List<String> vars,
                        String text) {
        }

}
