package ca.freshstart.data.matching.types;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Holds info about clietns matching records grpuped by dates
 */
@Data
@ToString
public class ClientSimpleMatchingInfo {

    private String date;

    private List<ClientSimpleMatchingInfoItem> items;

}
