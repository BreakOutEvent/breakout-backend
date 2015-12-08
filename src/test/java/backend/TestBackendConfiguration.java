package backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {SwaggerConfiguration.class})
public class TestBackendConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(BackendConfiguration.class, args);
    }

}