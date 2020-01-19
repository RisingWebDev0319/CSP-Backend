package ca.freshstart.data.matching.types;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class CrossingData {

    private String date;

    private Long sessionId;

    private List<CrossingDataItem> items;
}
