package org.jembi.jempi.linker.thresholdRangeProcessor.lib.rangeType;

public class RangeTypeFactory {

    public static RangeDetails StandardThresholdNotificationRangeBelow(float lowerValue, float upperValue){
        return new RangeDetails(lowerValue, upperValue, RangeTypeName.NOTIFICATION_RANGE_BELOW_THRESHOLD);
    }

    public static RangeDetails StandardThresholdNotificationRangeAbove(float lowerValue, float upperValue){
        return new RangeDetails(lowerValue, upperValue, RangeTypeName.NOTIFICATION_RANGE_ABOVE_THRESHOLD);
    }

    public static RangeDetails StandardThresholdAboveThreshold(float lowerValue, float upperValue){
        return new RangeDetails(lowerValue, upperValue, RangeTypeName.ABOVE_THRESHOLD);
    }
}
