package test.gli.DogService.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShibaImageSelection implements ImageSelection {
  @Override
  public List<String> selectImages(List<String> images) {
    return IntStream.range(0, images.size())
        .filter(i -> i % 2 == 0)
        .mapToObj(images::get)
        .collect(Collectors.toList());
  }
}
