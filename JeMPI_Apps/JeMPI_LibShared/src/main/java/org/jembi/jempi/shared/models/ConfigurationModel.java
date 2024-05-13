package org.jembi.jempi.shared.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigurationModel {

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
            String source,
            String defaultVal) {
    }

    public record Node(
            String nodeName,
            List<Field> fields) {
    }

    public record DemographicField(
            String fieldName,
            String fieldType,
            Integer csvCol,
            String source,
            String defaultVal,
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
            LinkRules link) {
    }

    public record LinkRules(
            DeterministicLink deterministic,
            ProbabilisticLink probabilistic) {
    }

    public record DeterministicLink(
            QueryLink queryLinkDeterministicA,
            QueryLink queryLinkDeterministicB) {
    }

    public record ProbabilisticLink(
            QueryLink queryLinkProbabilistic) {
    }

    public record QueryLink(
            List<String> vars,
            String text) {
    }

}
