package test.gli.DogTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import test.gli.DogService.DogServiceApplication;
import test.gli.DogService.model.Dog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ContextConfiguration(classes = DogServiceApplication.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DogControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  private List<String> allBreeds;

  @BeforeEach
  public void setUp() throws Exception {
    // Perform the getAll operation to retrieve all breeds
    ResultActions result = mockMvc.perform(get("/dog/breed"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    // Extract the breed names from the response JSON
    MvcResult mvcResult = result.andReturn();
    String responseJson = mvcResult.getResponse().getContentAsString();
    allBreeds = extractBreedNamesFromJson(responseJson);
  }

  @Test
  public void testGetAllBreedsEndpoint() throws Exception {
    // Perform assertions on the 'allBreeds' list obtained from the setup method
    // ...

    // You can also directly use 'allBreeds' in this test if needed
    // For example:
    assertTrue(allBreeds.contains("sheepdog"));
    assertTrue(allBreeds.contains("terrier"));
    assertTrue(allBreeds.contains("shiba"));
  }

  @Test
  public void testGetSubBreed() throws Exception {
    // Choose a specific breed from the 'allBreeds' list for testing
    String breed = "sheepdog";

    // Perform the getSubBreed operation using the selected breed
    ResultActions result = mockMvc.perform(get("/dog/breed/" + breed))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    String subBreedResponseJson = result.andReturn().getResponse().getContentAsString();
    List<String> subBreeds = extractBreedNamesFromJson(subBreedResponseJson);
    assertEquals(2, subBreeds.size());
    assertTrue(subBreeds.contains("sheepdog-english"));
    assertTrue(subBreeds.contains("sheepdog-shetland"));
  }

  @Test
  public void testGetShibaBreedImage() throws Exception {
    String breed = "shiba";

    ResultActions result = mockMvc.perform(get("/dog/breed/" + breed + "/images"))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));

    result.andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  public void testGetTerrierImages() throws Exception {
    String breed = "terrier";

    ResultActions result = mockMvc.perform(get("/dog/breed/" + breed + "/images"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    result.andExpect(jsonPath("$.length()").value(7))
        .andExpect(jsonPath("$[*].terrier-australian").exists())
        .andExpect(jsonPath("$[*].terrier-american").exists())
        .andExpect(jsonPath("$[*].terrier-border").exists())
        .andExpect(jsonPath("$[*].terrier-fox").exists())
        .andExpect(jsonPath("$[*].terrier-dandie").exists())
        .andExpect(jsonPath("$[*].terrier-cairn").exists())
        .andExpect(jsonPath("$[*].terrier-bedlington").exists());

  }

  @Test
  public void testCreateDog() throws Exception {
    // Create a sample Dog object for the request body
    Dog dog = new Dog();
    dog.setBreed("Munchkin");
    dog.setSubBreeds(Arrays.asList("Himalayan", "Germany"));
    dog.setImages(Collections.emptyList());

    String requestBody = asJsonString(dog);

    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/dog")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    result
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.breed").value("Munchkin"))
        .andExpect(jsonPath("$.subBreeds[0]").value("Himalayan"))
        .andExpect(jsonPath("$.subBreeds[1]").value("Germany"))
        .andExpect(jsonPath("$.images").isArray());
  }

  @Test
  public void testUpdateDog() throws Exception {
    Dog dog = new Dog();
    dog.setBreed("Munchkin");
    dog.setSubBreeds(Arrays.asList("Himalayan", "Germany"));
    dog.setImages(Collections.emptyList());

    String requestBody = asJsonString(dog);
    MvcResult create = mockMvc.perform(MockMvcRequestBuilders.post("/dog")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isOk())
        .andReturn();

    String createResponseJson = create.getResponse().getContentAsString();
    ObjectMapper objectMapper = new ObjectMapper();
    Dog createdDog = objectMapper.readValue(createResponseJson, Dog.class);

    // Update the dog
    Dog updatedDog = new Dog();
    updatedDog.setBreed("MunchkinUpdated");
    updatedDog.setSubBreeds(Arrays.asList("HimalayanUpdated", "GermanyUpdated"));
    updatedDog.setImages(Arrays.asList("image1", "image2"));

    String requestUpdate = asJsonString(updatedDog);
    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/dog/" + createdDog.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestUpdate))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    result.andExpect(jsonPath("$.id").value(createdDog.getId()))
        .andExpect(jsonPath("$.breed").value("MunchkinUpdated"))
        .andExpect(jsonPath("$.subBreeds[0]").value("HimalayanUpdated"))
        .andExpect(jsonPath("$.subBreeds[1]").value("GermanyUpdated"))
        .andExpect(jsonPath("$.images[0]").value("image1"))
        .andExpect(jsonPath("$.images[1]").value("image2"));
  }

  @Test
  public void testDeleteDog() throws Exception {
    // First, create a dog to delete
    Dog dog = new Dog();
    dog.setBreed("Munchkin");
    dog.setSubBreeds(Arrays.asList("Himalayan", "Germany"));
    dog.setImages(Collections.emptyList());

    String requestBody = asJsonString(dog);
    MvcResult create = mockMvc.perform(MockMvcRequestBuilders.post("/dog")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isOk())
        .andReturn();

    String createResponseJson = create.getResponse().getContentAsString();
    ObjectMapper objectMapper = new ObjectMapper();
    Dog createdDog = objectMapper.readValue(createResponseJson, Dog.class);

    // Delete the dog
    mockMvc.perform(MockMvcRequestBuilders.delete("/dog/" + createdDog.getId()))
        .andExpect(status().isOk());

    // Verify the dog is deleted
    mockMvc.perform(get("/dog/" + createdDog.getId()))
        .andExpect(status().isNotFound());
  }

  private List<String> extractBreedNamesFromJson(String json) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, List<String>>> breedList = objectMapper.readValue(json,
        new TypeReference<List<Map<String, List<String>>>>() {
        });
    return breedList.stream().flatMap(map -> map.keySet().stream()).collect(Collectors.toList());
  }

  private String asJsonString(Object object) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(object);
  }

}
