package ca.freshstart.helpers.remote.types;

import ca.freshstart.types.IdIf;
import lombok.Data;

@Data
public class TherapistRemote implements IdIf<String> {
    private String id;
    private String name;
    private String email;
}