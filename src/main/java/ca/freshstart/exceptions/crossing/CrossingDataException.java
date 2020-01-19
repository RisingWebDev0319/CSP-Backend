package ca.freshstart.exceptions.crossing;

import ca.freshstart.data.matching.types.CrossingData;

public class CrossingDataException extends RuntimeException {

    private CrossingData crossingData;

    public CrossingDataException(CrossingData crossingData) {
        super();
        this.crossingData = crossingData;
    }

    public CrossingData getCrossingData() {
        return crossingData;
    }
}
