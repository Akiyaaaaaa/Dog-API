package test.gli.DogService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    @Bean
    public Docket api() {
      return new Docket(DocumentationType.SWAGGER_2)
              .select()
              .apis(RequestHandlerSelectors.basePackage("test.gli.DogService"))
              .paths(PathSelectors.any())
              .build()
              .apiInfo(apiInfo());
}

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("Dog API")
                .description("This API is used for listing all of dog breeds and sub-breeds from https://dog.ceo/api")
                .version("1.0.0")
                .build();
}
}
