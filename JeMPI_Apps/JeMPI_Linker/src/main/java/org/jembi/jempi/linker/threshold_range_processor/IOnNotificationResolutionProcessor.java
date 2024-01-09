package org.jembi.jempi.linker.threshold_range_processor;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.NotificationResolutionProcessorData;

public interface IOnNotificationResolutionProcessor {
    boolean processOnNotificationResolution(NotificationResolutionProcessorData notificationResolutionProcessorData, LibMPI libMPI);
}
