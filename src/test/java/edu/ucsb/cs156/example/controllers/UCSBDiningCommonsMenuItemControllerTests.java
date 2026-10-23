package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for GET /api/UCSBDiningCommonsMenuItem/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all")).andExpect(status().is(200));
  }

  // Authorization tests for POST /api/UCSBDiningCommonsMenuItem/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Tofu Banh Mi Sandwich")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Tofu Banh Mi Sandwich")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menu_items() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItem item1 =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Tofu Banh Mi Sandwich")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem item2 =
        UCSBDiningCommonsMenuItem.builder()
            .id(2L)
            .diningCommonsCode("portola")
            .name("Cream of Broccoli Soup")
            .station("Greens & Grains")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedItems = new ArrayList<>();
    expectedItems.addAll(Arrays.asList(item1, item2));

    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedItems);

    // act

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedItems);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menu_item() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItem item1 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Tofu Banh Mi Sandwich")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem savedItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Tofu Banh Mi Sandwich")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(eq(item1))).thenReturn(savedItem);

    // act

    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBDiningCommonsMenuItem/post")
                    .param("diningCommonsCode", "ortega")
                    .param("name", "Tofu Banh Mi Sandwich")
                    .param("station", "Entree Specials")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(item1);

    String expectedJson = mapper.writeValueAsString(savedItem);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_menu_item_by_id() throws Exception {

    UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();
    item.setId(1L);
    item.setDiningCommonsCode("ortega");
    item.setName("Tofu Banh Mi Sandwich");
    item.setStation("Entree Specials");

    when(ucsbDiningCommonsMenuItemRepository.findById(1L)).thenReturn(Optional.of(item));

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "1"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);

    assertEquals(mapper.writeValueAsString(item), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_gets_404_for_missing_menu_item() throws Exception {

    when(ucsbDiningCommonsMenuItemRepository.findById(123L)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "123"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(123L);

    Map<String, Object> json = responseToJson(response);

    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id 123 not found", json.get("message"));
  }

  @Test
  public void logged_out_user_cannot_get_menu_item_by_id() throws Exception {

    mockMvc
        .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "1"))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_update_menu_item() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItem originalItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Tofu Banh Mi Sandwich")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem incomingItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("portola")
            .name("Veggie Burger")
            .station("Grill")
            .build();

    UCSBDiningCommonsMenuItem expectedItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("portola")
            .name("Veggie Burger")
            .station("Grill")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(1L)).thenReturn(Optional.of(originalItem));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incomingItem))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(expectedItem);

    String expectedJson = mapper.writeValueAsString(expectedItem);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_update_nonexistent_menu_item() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItem incomingItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("portola")
            .name("Veggie Burger")
            .station("Grill")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(123L)).thenReturn(Optional.empty());

    // act

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incomingItem))
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(123L);

    Map<String, Object> json = responseToJson(response);

    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id 123 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_user_cannot_update_menu_item() throws Exception {

    mockMvc
        .perform(
            put("/api/UCSBDiningCommonsMenuItem")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  public void logged_out_user_cannot_update_menu_item() throws Exception {

    mockMvc
        .perform(
            put("/api/UCSBDiningCommonsMenuItem")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_menu_item() throws Exception {

    // arrange
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .id(15L)
            .diningCommonsCode("ortega")
            .name("Tofu Banh Mi Sandwich")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L))).thenReturn(Optional.of(item));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBDiningCommonsMenuItem").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);

    assertEquals("UCSBDiningCommonsMenuItem with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_delete_nonexistent_menu_item() throws Exception {

    // arrange
    when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBDiningCommonsMenuItem").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);

    Map<String, Object> json = responseToJson(response);

    assertEquals("UCSBDiningCommonsMenuItem with id 15 not found", json.get("message"));
  }
}
