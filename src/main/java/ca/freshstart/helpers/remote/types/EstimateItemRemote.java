package ca.freshstart.helpers.remote.types;

import lombok.Data;

@Data
public class EstimateItemRemote {
    public Long serviceID; // NS service/items id (external service id)
    public String therapistID; // NS vendor id (external therapist id)
    public Float price; // price for item/service
    public String serviceDate; // date in MM-DD-YYYY format
    public String serviceTime; // time in HH:MM am/pm format

    public EstimateItemRemote() {
    }
}
