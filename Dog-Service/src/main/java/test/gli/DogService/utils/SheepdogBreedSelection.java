package test.gli.DogService.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SheepdogBreedSelection implements SubBreedSelection {

  @Override
  public List<Map<String, List<String>>> selectSubBreeds(String breed, List<String> subBreeds) {
    return subBreeds.stream()
        .map(subBreed -> {
          String finalBreed = breed + "-" + subBreed.toLowerCase();
          return new HashMap<String, List<String>>() {
            {
              put(finalBreed, new ArrayList<>());
            }
          };
        })
        .collect(Collectors.toList());
  }
}
