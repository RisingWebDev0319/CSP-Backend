package ca.freshstart.applications.user.types;

import ca.freshstart.data.appUser.entity.AppUser;
import lombok.Data;

@Data
public class ProfileResponse {
    private AppUser currentUser;
    private boolean theTherapist;
}