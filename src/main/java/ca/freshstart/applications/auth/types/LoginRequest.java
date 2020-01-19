package ca.freshstart.applications.auth.types;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;

    public boolean isValid() {
        return username != null && password != null;
    }
}