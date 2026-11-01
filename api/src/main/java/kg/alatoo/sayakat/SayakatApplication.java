package kg.alatoo.sayakat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SayakatApplication {

    public static void main(String[] args) {
        SpringApplication.run(SayakatApplication.class, args);
    }
}
