package org.jembi.jempi.linker.threshold_range_processor.lib.range_type;

public class RangeTypeFactory {

    public static RangeDetails standardThresholdNotificationRangeBelow(float lowerValue, float upperValue){
        return new RangeDetails(lowerValue, upperValue, RangeTypeName.NOTIFICATION_RANGE_BELOW_THRESHOLD);
    }

    public static RangeDetails standardThresholdNotificationRangeAbove(float lowerValue, float upperValue){
        return new RangeDetails(lowerValue, upperValue, RangeTypeName.NOTIFICATION_RANGE_ABOVE_THRESHOLD);
    }

    public static RangeDetails standardThresholdAboveThreshold(float lowerValue, float upperValue){
        return new RangeDetails(lowerValue, upperValue, RangeTypeName.ABOVE_THRESHOLD);
    }
}
