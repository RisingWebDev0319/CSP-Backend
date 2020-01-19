package ca.freshstart.applications.initialization;

import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MockData {
    private static final Logger logger = LoggerFactory.getLogger(MockData.class);

    private final TherapistRepository therapistRepository;

    public void mock() {
        logger.info("-- [ Start mocking common data ] --");

//        mockTherapistsForTesting();

        logger.info("-- [ End mocking common data ] --");
    }


    private void mockTherapistsForTesting() {
        Optional<Therapist> byEmail = therapistRepository.findByEmail("ilinyh.s@gmail.com");
        if (!byEmail.isPresent()) {
            Therapist therapist = new Therapist();
            therapist.setEmail("ilinyh.s@gmail.com");
            therapist.setExternalId("4967");
            therapist.setName("Sergey Valerievich");

            therapistRepository.save(therapist);
        }
        byEmail = therapistRepository.findByEmail("y.paulavets@gmail.com");
        if (!byEmail.isPresent()) {
            Therapist therapist = new Therapist();
            therapist.setEmail("y.paulavets@gmail.com");
            therapist.setExternalId("4968");
            therapist.setName("Yahor Alexandrovich");

            therapistRepository.save(therapist);
        }
    }

}