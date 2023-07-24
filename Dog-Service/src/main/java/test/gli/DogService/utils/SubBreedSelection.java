package test.gli.DogService.utils;

import java.util.List;
import java.util.Map;

public interface SubBreedSelection {
  List<Map<String, List<String>>> selectSubBreeds(String breed, List<String> subBreeds);
}
