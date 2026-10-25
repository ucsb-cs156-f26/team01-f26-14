package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockitoBean MenuItemReviewRepository menuItemReviewRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for GET /api/MenuItemReview/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().is(200));
  }

  // Authorization tests for GET /api/MenuItemReview?id=...

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview?id=7")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_by_id() throws Exception {
    MenuItemReview review =
        MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("student@example.com")
            .stars(5)
            .dateReviewed(LocalDateTime.parse("2026-10-21T10:00:00"))
            .comments("Great food!")
            .build();

    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.of(review));

    mockMvc.perform(get("/api/MenuItemReview?id=7")).andExpect(status().is(200));
  }

  // Authorization tests for POST /api/MenuItemReview/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/MenuItemReview/post")
                .param("itemId", "1")
                .param("reviewerEmail", "student@example.com")
                .param("stars", "5")
                .param("dateReviewed", "2026-10-21T10:00:00")
                .param("comments", "Great food!")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/MenuItemReview/post")
                .param("itemId", "1")
                .param("reviewerEmail", "student@example.com")
                .param("stars", "5")
                .param("dateReviewed", "2026-10-21T10:00:00")
                .param("comments", "Great food!")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Tests for GET /api/MenuItemReview/all

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menu_item_reviews() throws Exception {
    MenuItemReview review1 =
        MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("student1@example.com")
            .stars(5)
            .dateReviewed(LocalDateTime.parse("2026-10-21T10:00:00"))
            .comments("Great food!")
            .build();

    MenuItemReview review2 =
        MenuItemReview.builder()
            .itemId(2L)
            .reviewerEmail("student2@example.com")
            .stars(3)
            .dateReviewed(LocalDateTime.parse("2026-10-22T11:00:00"))
            .comments("Just okay")
            .build();

    ArrayList<MenuItemReview> expectedReviews = new ArrayList<>();
    expectedReviews.addAll(Arrays.asList(review1, review2));

    when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

    MvcResult response =
        mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedReviews);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for GET /api/MenuItemReview?id=...

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
    MenuItemReview review =
        MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("student@example.com")
            .stars(5)
            .dateReviewed(LocalDateTime.parse("2026-10-21T10:00:00"))
            .comments("Great food!")
            .build();

    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.of(review));

    MvcResult response =
        mockMvc.perform(get("/api/MenuItemReview?id=7")).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(review);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/MenuItemReview?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("MenuItemReview with id 7 not found", json.get("message"));
  }

  // Tests for POST /api/MenuItemReview/post

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menu_item_review() throws Exception {
    LocalDateTime dateReviewed = LocalDateTime.parse("2026-10-21T10:00:00");

    MenuItemReview review =
        MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("student@example.com")
            .stars(5)
            .dateReviewed(dateReviewed)
            .comments("Great food!")
            .build();

    when(menuItemReviewRepository.save(eq(review))).thenReturn(review);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/MenuItemReview/post")
                    .param("itemId", "1")
                    .param("reviewerEmail", "student@example.com")
                    .param("stars", "5")
                    .param("dateReviewed", "2026-10-21T10:00:00")
                    .param("comments", "Great food!")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).save(eq(review));

    String expectedJson = mapper.writeValueAsString(review);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_menu_item_review_with_low_rating() throws Exception {
    LocalDateTime dateReviewed = LocalDateTime.parse("2026-10-21T10:00:00");

    MenuItemReview review =
        MenuItemReview.builder()
            .itemId(2L)
            .reviewerEmail("student2@example.com")
            .stars(1)
            .dateReviewed(dateReviewed)
            .comments("Not great")
            .build();

    when(menuItemReviewRepository.save(eq(review))).thenReturn(review);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/MenuItemReview/post")
                    .param("itemId", "2")
                    .param("reviewerEmail", "student2@example.com")
                    .param("stars", "1")
                    .param("dateReviewed", "2026-10-21T10:00:00")
                    .param("comments", "Not great")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).save(eq(review));

    String expectedJson = mapper.writeValueAsString(review);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
