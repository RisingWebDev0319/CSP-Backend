package ca.freshstart.data.matching.types;

import ca.freshstart.data.matching.entity.ClientMatchingConfirmation;
import ca.freshstart.data.suggestions.entity.PreliminaryEvent;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * pack of mised data - matching it not send to confirm and confirmation
 */
@Data
@ToString
public class ClientServiceMatchingData {

    private Long clientId;

    private String clientName;

    private List<PreliminaryEvent> matchingData = new ArrayList<>();

    private ClientMatchingConfirmation confirmationData;
}
