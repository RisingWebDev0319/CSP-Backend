package ca.freshstart.helpers.remote.types;

import ca.freshstart.types.IdIf;
import lombok.Data;

@Data
public class EventRemote implements IdIf<Long> {
    private Long id;
    private String name;
    private int prepTime;
    private int duration;
    private int cleanTime;
    private Float price;
}