package ca.freshstart.applications.user;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdResponse;
import ca.freshstart.applications.user.types.ProfileResponse;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.PasswordEncryptor;
import ca.freshstart.helpers.ModelValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import static ca.freshstart.helpers.CspUtils.isNullOrEmpty;

@RestController
@RequiredArgsConstructor
public class UserController extends AbstractController {

    private final TherapistRepository therapistRepository;

    /**
     *  Return list of users, except super-user
     * @param pageId page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested users
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @Secured("ROLE_AUTH")
    public Collection<AppUser> users(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                     @RequestParam(value = "sort",required = false) String sort) {

        return userRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of users, except super-user
     * @return Count of users
     */
    @RequestMapping(value = "/users/count", method = RequestMethod.GET)
    @Secured("ROLE_AUTH")
    public Count usersCount() {
        return new Count(userRepository.count());
    }

    /**
     * Add new user
     * @param user User to add. Password is plain-text
     * @return user Id
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @Secured("ROLE_AUTH")
    public IdResponse addUser(@RequestBody AppUser user) {
        ModelValidation.validateUser(user);

        String plainPassword = user.getPassword();

        try {
            user.setPassword(PasswordEncryptor.encryptPassword(user.getPassword()));
            user = userRepository.save(user);
        } catch(DataIntegrityViolationException ex) {
            throw new ConflictException("User with such email exists");
        }
        mailService.sendBasic(user.getEmail(),"Password for your account: " + plainPassword);

        return new IdResponse(user.getId());
    }

    /**
     * Return information about current user
     * @return user info without password and modules
     */
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ProfileResponse getUserProfile() {
        ProfileResponse response = new ProfileResponse();

        AppUser user = getCurrentUser();

        response.setCurrentUser(user);
        response.setTheTherapist(therapistRepository.findByEmail(user.getEmail()).isPresent());

        return response;
    }

    /**
     * Return information about one user
     * @param userId id of user
     * @return User data
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    @Secured("ROLE_AUTH")
    public AppUser getUserById(@PathVariable("userId") Long userId) {
        return userRepository.findById(userId)
               .orElseThrow(() -> new NotFoundException("No such user"));
    }

    /**
     * Update user. Password is not in request (if not changed) or plain-text
     * @param userId id of user
     * @param userFrom Updated user
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.PUT)
    @ResponseStatus(value= HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_AUTH")
    public void updateUser(@PathVariable("userId") Long userId, @RequestBody AppUser userFrom) {
        if(userId == Constants.SUPER_USER_ID) {
            // attempt to change SUPER_USER
            throw new NotFoundException("No such user");
        }

        if(!isNullOrEmpty(userFrom.getEmail()) && !ModelValidation.isEmailValid(userFrom.getEmail())) {
            throw new BadRequestException("Invalid values in fields");
        }

        AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("No such user"));

        if(!isNullOrEmpty(userFrom.getPassword())) {
            user.setPassword(PasswordEncryptor.encryptPassword(userFrom.getPassword()));
        }

        if(!isNullOrEmpty(userFrom.getName())) {
            user.setName(userFrom.getName());
        }

        if(userFrom.getLocked() != null) {
            user.setLocked(userFrom.getLocked());
        }

        if(userFrom.getModules() != null) {
            user.setModules(userFrom.getModules());
        }

        try {
            userRepository.save(user);
        } catch(DataIntegrityViolationException ex) {
            throw new ConflictException("User with such email exists");
        }
    }

    /**
     * Delete user
     * @param userId id of user
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.DELETE)
    @ResponseStatus(value= HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_AUTH")
    public void deleteUserById(@PathVariable("userId") Long userId) {
        if(userId == Constants.SUPER_USER_ID) {
            // attempt to delete SUPER_USER
            throw new NotFoundException("No such user");
        }

        AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("No such user"));

        userRepository.delete(user);
    }
}