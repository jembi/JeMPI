package org.jembi.jempi.controller.interactions_processor.lib.range_type;

public class RangeTypeFactory {

    protected RangeTypeFactory() { }
    public static RangeDetails standardThresholdNotificationRangeBelow(final float lowerValue, final float upperValue) {
        return new RangeDetails(lowerValue, upperValue, RangeTypeName.NOTIFICATION_RANGE_BELOW_THRESHOLD);
    }

    public static RangeDetails standardThresholdNotificationRangeAbove(final float lowerValue, final float upperValue) {
        return new RangeDetails(lowerValue, upperValue, RangeTypeName.NOTIFICATION_RANGE_ABOVE_THRESHOLD);
    }

    public static RangeDetails standardThresholdAboveThreshold(final float lowerValue, final float upperValue) {
        return new RangeDetails(lowerValue, upperValue, RangeTypeName.ABOVE_THRESHOLD);
    }
}
