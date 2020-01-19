package ca.freshstart.types;

import lombok.Data;

@Data
public class RestoreData {
    private String email;
    private String securityKey;
    private String password;
}