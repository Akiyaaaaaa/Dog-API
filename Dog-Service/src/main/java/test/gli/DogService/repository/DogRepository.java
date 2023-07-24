package test.gli.DogService.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.gli.DogService.model.Dog;

@Repository
public interface DogRepository extends JpaRepository<Dog, Long> {
  Boolean existsByBreed(String breed);

  Optional<Dog> findByBreed(String breed);

  @Query("SELECT d FROM Dog d WHERE d.breed = :breedName")
  Dog findByBreedName(@Param("breedName") String breedName);

  @Query("SELECT DISTINCT d.breed FROM Dog d")
  List<String> findDistinctBreeds();

}
