package test.gli.DogService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import org.springframework.boot.web.client.RestTemplateBuilder;

@Configuration
public class RestTemplateConfig {

  private static final int ALL_BREEDS_TIMEOUT = 5000;
  private static final int SUB_BREED_TIMEOUT = 2000;

  @Bean
  @Primary
  public RestTemplate allBreedsTimeout() {
    return createRestTemplate(ALL_BREEDS_TIMEOUT);
  }

  @Bean
  public RestTemplate subBreedTimeout() {
    return createRestTemplate(SUB_BREED_TIMEOUT);
  }

  private RestTemplate createRestTemplate(int timeout) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(timeout);
    requestFactory.setReadTimeout(timeout);
    return new RestTemplateBuilder().requestFactory(() -> requestFactory).build();
  }

}
