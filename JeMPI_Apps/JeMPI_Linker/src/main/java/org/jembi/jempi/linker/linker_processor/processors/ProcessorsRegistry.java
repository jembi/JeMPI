package org.jembi.jempi.linker.linker_processor.processors;

import org.jembi.jempi.linker.linker_processor.processors.TPTNProcessor.TPTNProcessor;
import org.jembi.jempi.linker.linker_processor.processors.field_equality_pair_match_processor.FieldEqualityPairMatchProcessor;
import org.jembi.jempi.linker.linker_processor.processors.linker_stats.LinkerStatsProcessor;
import org.jembi.jempi.shared.models.Interaction;

import java.util.List;
import java.util.stream.Collectors;

public final class ProcessorsRegistry {
    private final FieldEqualityPairMatchProcessor fieldEqualityPairMatchProcessor;
    private final TPTNProcessor tptnProcessor;
    private final LinkerStatsProcessor linkerStatsProcessor;
    public ProcessorsRegistry() {
        fieldEqualityPairMatchProcessor = new FieldEqualityPairMatchProcessor();
        tptnProcessor = new TPTNProcessor();
        linkerStatsProcessor =  new LinkerStatsProcessor();
    }

    private <T extends ISubProcessor> List<T> setLinkerIds(final List<T> processor, final String linkerId) {
        if (linkerId == null) {
            return processor;
        }
        return processor.stream().map(p -> (T) p.setLinkerId(linkerId)).collect(Collectors.toList());
    }
    public List<IDashboardDataProducer<?>> getDashboardDataProducerProcessors(final String linkerId) {
        List<IDashboardDataProducer<?>> processor = List.of(
                fieldEqualityPairMatchProcessor,
                tptnProcessor,
                linkerStatsProcessor
        );

        return setLinkerIds(processor, linkerId);

    }

    public List<IThresholdRangeSubProcessor> getThresholdProcessors(final Interaction originalInteractionIn, final String linkerId) {
        List<IThresholdRangeSubProcessor> processor = List.of(
                fieldEqualityPairMatchProcessor,
                tptnProcessor
        );

        return setLinkerIds(processor, linkerId).stream().map(p -> p.setOriginalInteraction(originalInteractionIn)).collect(Collectors.toList());
    }

    public List<IOnNotificationResolutionProcessor> getOnNotificationResolutionProcessors(final String linkerId) {
        List<IOnNotificationResolutionProcessor> processor = List.of(
                fieldEqualityPairMatchProcessor,
                tptnProcessor
        );

        return setLinkerIds(processor, linkerId);
    }

    public List<IOnNewInteractionProcessor> getOnNewInteractionProcessors(final String linkerId) {
        List<IOnNewInteractionProcessor> processor = List.of(
                linkerStatsProcessor
        );

        return setLinkerIds(processor, linkerId);
    }

}
