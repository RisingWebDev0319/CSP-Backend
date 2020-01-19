package ca.freshstart.data.availability.types;

import ca.freshstart.data.appUser.entity.AppUser;
import lombok.Data;

@Data
public class UserSimple {
    private String name;
    private String email;

    public UserSimple() {}

    public UserSimple(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public static UserSimple fromUser(AppUser user) {
        return new UserSimple(user.getName(), user.getEmail());
    }
}