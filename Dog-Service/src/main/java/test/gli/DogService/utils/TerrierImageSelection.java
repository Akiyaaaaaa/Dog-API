package test.gli.DogService.utils;

import org.springframework.stereotype.Component;

@Component
public class TerrierImageSelection {
  // method to extract the sub-breed from the image url

  public String extractSubBreed(String imageUrl) {
    String[] parts = imageUrl.split("/");
    return parts[parts.length - 2];
  }
}
