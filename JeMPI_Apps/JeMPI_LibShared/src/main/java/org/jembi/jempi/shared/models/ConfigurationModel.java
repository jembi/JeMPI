package org.jembi.jempi.shared.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.config.input.Source;

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
                        List<String> nameValidationFields,
                        Rules rules) {
        }

        public record Field(
                        String fieldName,
                        String fieldType,
                        Integer csvCol,
                        Source source,
                        @JsonProperty("default") boolean defaultValue) {
        }

        public record Node(
                        String nodeName,
                        List<Field> fields) {
        }

        public record DemographicField(
                        String fieldName,
                        String fieldType,
                        Source source,
                        String indexGoldenRecord,
                        String indexInteraction,
                        LinkMetaData linkMetaData) {
        }

        public record LinkMetaData(
                        String comparison,
                        List<Double> comparisonLevels,
                        double m,
                        double u) {
        }

        public record Rules(
                        LinkRules link,
                        LinkRules matchNotification,
                        LinkRules validate) {
        }

        public record DeterministicRule(
              List<String> vars,
              String text) {
        }

        public record ProbabilisticRule(
              List<String> vars,
              String text,
              double linkThreshold,
              double marginWindowSize,
              ReviewThresholdRange reviewThresholdRange) {
        }

        public record ReviewThresholdRange(
              double low,
              double high) {
        }

        public record LinkRules(
                        List<DeterministicRule> deterministic,
                        List<ProbabilisticRule> probabilistic) {
        }

}
