package test.gli.DogService.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import test.gli.DogService.model.Dog;
import test.gli.DogService.service.DogService;

@RestController
@AllArgsConstructor
@RequestMapping("/dog")
public class DogController {
  private DogService dogService;

  @GetMapping("/breed")
  public List<Map<String, List<String>>> getAllBreed() {
    return dogService.getAllBreeds();
  }

  @GetMapping("/breed/{breed}")
  public List<Map<String, List<String>>> getSubBreed(@PathVariable String breed) {
    return dogService.getBySubBreed(breed);
  }

  @GetMapping("/breed/{breed}/images")
  public List<Map<String, List<String>>> getImagesByBreed(@PathVariable String breed) {
    return dogService.getByBreed(breed);
  }

  @GetMapping("/{id}")
  public Dog getById(@PathVariable Long id) {
    return dogService.getById(id);
  }

  @PostMapping
  public Dog createDog(@RequestBody Dog dog) {
    return dogService.createDog(dog);
  }

  @PutMapping("/{id}")
  public Dog updateDog(@PathVariable Long id, @RequestBody Dog dog) {
    return dogService.updateDog(id, dog);
  }

  @DeleteMapping("/{id}")
  public void deleteDog(@PathVariable Long id) {
    dogService.deleteDog(id);
  }
}
