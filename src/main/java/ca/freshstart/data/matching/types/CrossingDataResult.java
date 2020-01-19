package ca.freshstart.data.matching.types;

import lombok.Data;

@Data
public class CrossingDataResult {

    CrossingData crossingData;

    /**
     * if BaseCrossEvent cross one of
     * ConcreteEvent or ConcreteCalendarEvent or AvTimeRecord
     * but not PreliminaryEvent (in confirmation state)
     */
    boolean criticalCrossing;

    public CrossingDataResult() {
    }

    public CrossingDataResult(CrossingData crossingData, boolean criticalCrossing) {
        this.crossingData = crossingData;
        this.criticalCrossing = criticalCrossing;
    }
}
