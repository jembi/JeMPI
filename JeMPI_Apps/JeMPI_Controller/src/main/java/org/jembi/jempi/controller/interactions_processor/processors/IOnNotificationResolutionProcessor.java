package org.jembi.jempi.controller.interactions_processor.processors;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.NotificationResolutionProcessorData;

public interface IOnNotificationResolutionProcessor extends ISubProcessor {
    boolean processOnNotificationResolution(NotificationResolutionProcessorData notificationResolutionProcessorData, LibMPI libMPI) throws Exception;
}
