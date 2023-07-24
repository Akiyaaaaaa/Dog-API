package test.gli.DogService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DogServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DogServiceApplication.class, args);

		System.out.println();
		System.out.println("Application is running...");
	}

}
