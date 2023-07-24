package test.gli.DogService.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import test.gli.DogService.model.Dog;
import test.gli.DogService.repository.DogRepository;
import test.gli.DogService.utils.ImageSelection;
import test.gli.DogService.utils.SheepdogBreedSelection;
import test.gli.DogService.utils.ShibaImageSelection;
import test.gli.DogService.utils.SubBreedSelection;
import test.gli.DogService.utils.TerrierImageSelection;

@Service // singleton pattern
public class DogService {

  @Autowired
  private RestTemplate restTemplate;

  private DogRepository dogRepository;
  private ImageSelection imageSelection;
  private SubBreedSelection subBreedSelection;
  @Autowired
  private TerrierImageSelection terrierImageSelection;

  private final RestTemplate allBreedsRestTemplate;
  private final RestTemplate subBreedRestTemplate;

  @Autowired
  public DogService(
      DogRepository dogRepository,
      @Qualifier("allBreedsTimeout") RestTemplate allBreedsRestTemplate,
      @Qualifier("subBreedTimeout") RestTemplate subBreedRestTemplate
  ) {
    this.dogRepository = dogRepository;
    this.allBreedsRestTemplate = allBreedsRestTemplate;
    this.subBreedRestTemplate = subBreedRestTemplate;
  }

  @Value("${dog.api.baseUrl}")
  private String url;

  public List<Map<String, List<String>>> getAllBreeds() {
    String endpoint = url + "/breeds/list/all";

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        endpoint,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<Map<String, Object>>() {
        });

    Map<String, Object> responseBody = response.getBody();
    List<Map<String, List<String>>> breedList = new ArrayList<>();

    if (responseBody != null && responseBody.containsKey("message")) {
      Object message = responseBody.get("message");
      if (message instanceof Map) {
        Map<String, Object> breedMap = (Map<String, Object>) message;

        breedList = breedMap.entrySet().stream()
            .map(entry -> {
              String breed = entry.getKey();
              Object subBreeds = entry.getValue();
              if (subBreeds instanceof List) {
                List<String> subBreedList = (List<String>) subBreeds;
                return new HashMap<String, List<String>>() {
                  {
                    put(breed, subBreedList);
                  }
                };
              } else {
                return null;
              }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
      }
    }

    List<String> existingBreeds = dogRepository.findDistinctBreeds();

    List<Dog> dogs = breedList.stream()
        .filter(data -> {
          String breed = data.keySet().iterator().next();
          return !existingBreeds.contains(breed);
        })
        .map(data -> {
          Dog dog = new Dog();
          dog.setBreed(data.keySet().iterator().next());
          dog.setSubBreeds(data.get(dog.getBreed()));
          dog.setImages(new ArrayList<>());
          return dog;
        })
        .collect(Collectors.toList());

    dogRepository.saveAll(dogs);

    return breedList;
  }

  public List<Map<String, List<String>>> getBySubBreed(String breed) {
    Optional<Dog> dogOptional = dogRepository.findByBreed(breed);
    if (dogOptional.isPresent()) {
      Dog dog = dogOptional.get();
      List<String> subBreeds = dog.getSubBreeds();
      List<Map<String, List<String>>> breedList = new ArrayList<>();
      subBreedSelection = new SheepdogBreedSelection();
      if (breed.equals("sheepdog")) {
        breedList = subBreedSelection.selectSubBreeds(breed, subBreeds);
        return breedList;
      } else {
        breedList.add(new HashMap<String, List<String>>() {
          {
            put(breed, subBreeds);
          }
        });
      }
      return breedList;
    }
    return Collections.emptyList();
  }

  public List<Map<String, List<String>>> getByBreed(String breed) {
    String endpoint = url + "/breed/" + breed + "/images";

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(endpoint, HttpMethod.GET, null,
        new ParameterizedTypeReference<Map<String, Object>>() {
        });

    Map<String, Object> responseBody = response.getBody();
    if (responseBody != null && responseBody.containsKey("message")) {
      Object message = responseBody.get("message");
      if (message instanceof List) {
        List<String> breeds = (List<String>) message;
        List<Map<String, List<String>>> breedsList = new ArrayList<>();

        if (breed.equals("shiba")) {
          imageSelection = new ShibaImageSelection();
          List<String> oddImages = imageSelection.selectImages(breeds);
          breedsList.add(new HashMap<String, List<String>>() {
            {
              put(breed, oddImages);
            }
          });
        } else if (breed.equals("terrier")) {
          Map<String, List<String>> subBreedMap = new HashMap<>();
          for (String imageUrl : breeds) {
            String subBreed = terrierImageSelection.extractSubBreed(imageUrl);
            subBreedMap.putIfAbsent(subBreed, new ArrayList<>());
            subBreedMap.get(subBreed).add(imageUrl);
          }

          // Convert subBreedMap to the desired format
          breedsList = subBreedMap.entrySet().stream()
              .map(entry -> new HashMap<String, List<String>>() {
                {
                  put(entry.getKey(), entry.getValue());
                }
              })
              .collect(Collectors.toList());
        } else {
          breedsList.add(new HashMap<String, List<String>>() {
            {
              put(breed, breeds);
            }
          });
        }

        for (Map.Entry<String, List<String>> entry : breedsList.get(0).entrySet()) {
          String breedName = entry.getKey();
          List<String> images = entry.getValue();
          Dog existingDog = dogRepository.findByBreedName(breedName);
          if (existingDog != null) {
            existingDog.getImages().addAll(images);
            dogRepository.save(existingDog);
          } else {
            Dog newDog = new Dog();
            newDog.setBreed(breedName);
            newDog.setImages(images);
            dogRepository.save(newDog);
          }
        }

        return breedsList;
      }
    }
    return Collections.emptyList();
  }

  public Dog getById(Long id) {
    Optional<Dog> searchDog = dogRepository.findById(id);
    if (searchDog.isPresent()) {
      return searchDog.get();
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found!");
    }
  }

  public Dog createDog(Dog dog) {
    if (dogRepository.existsByBreed(dog.getBreed())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Breed name already exists!");
    }
    return dogRepository.save(dog);
  }

  public Dog updateDog(Long id, Dog dog) {
    Dog existingDog = getById(id);
    if (existingDog != null) {
      if (!existingDog.getBreed().equals(dog.getBreed()) && dogRepository.existsByBreed(dog.getBreed())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Breed name already exists!");
      }
      existingDog.setBreed(dog.getBreed());
      existingDog.setSubBreeds(dog.getSubBreeds());
      existingDog.setImages(dog.getImages());
      return dogRepository.save(existingDog);
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found!");
    }
  }

  public void deleteDog(Long id) {
    Optional<Dog> optionalDog = dogRepository.findById(id);
    if (optionalDog.isPresent()) {
      Dog dog = optionalDog.get();
      dogRepository.delete(dog);
    } else {
      throw new IllegalArgumentException("Dog not found with ID: " + id);
    }
  }

}
