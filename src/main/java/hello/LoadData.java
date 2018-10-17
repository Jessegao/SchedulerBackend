package hello;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class LoadData {

    @Bean
    CommandLineRunner initDatabase(BookingRepository repository) {
        return args -> {
            // if log cannot be detected, use this solution
            //https://stackoverflow.com/questions/14866765/building-with-lomboks-slf4j-and-intellij-cannot-find-symbol-log/43253630
//            log.info("Preloading " + repository.save(new Booking("Bilbo Baggins", "burglar@gmail.com", "10/14/2018", "10/15/2018")));
//            log.info("Preloading " + repository.save(new B("Frodo Baggins", "thief")));
        };
    }
}