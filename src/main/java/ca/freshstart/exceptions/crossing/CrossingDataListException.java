package ca.freshstart.exceptions.crossing;

import ca.freshstart.data.matching.types.CrossingData;

import java.util.List;

public class CrossingDataListException extends RuntimeException {

    private List<CrossingData> crossingDataList;

    public CrossingDataListException(List<CrossingData> crossingDataList) {
        super();
        this.crossingDataList = crossingDataList;
    }

    public List<CrossingData> getCrossingDataList() {
        return crossingDataList;
    }
}
