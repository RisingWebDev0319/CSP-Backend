package ca.freshstart.applications.auth.types;

import lombok.Data;

@Data
public class RestoreRequest {
    private String email;
}