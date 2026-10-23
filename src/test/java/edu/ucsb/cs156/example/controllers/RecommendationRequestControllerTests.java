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
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for GET /api/RecommendationRequest/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/RecommendationRequest/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/RecommendationRequest/all")).andExpect(status().is(200));
  }

  // Authorization tests for POST /api/RecommendationRequest/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/RecommendationRequest/post")
                .param("requesterEmail", "student@example.com")
                .param("professorEmail", "professor@example.com")
                .param("explanation", "Requesting a recommendation")
                .param("dateRequested", "2026-10-21T10:00:00")
                .param("dateNeeded", "2026-11-15T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/RecommendationRequest/post")
                .param("requesterEmail", "student@example.com")
                .param("professorEmail", "professor@example.com")
                .param("explanation", "Requesting a recommendation")
                .param("dateRequested", "2026-10-21T10:00:00")
                .param("dateNeeded", "2026-11-15T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Tests for GET /api/RecommendationRequest/all

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendation_requests() throws Exception {
    RecommendationRequest request1 =
        RecommendationRequest.builder()
            .requesterEmail("student1@example.com")
            .professorEmail("professor1@example.com")
            .explanation("Graduate school recommendation")
            .dateRequested(LocalDateTime.parse("2026-10-21T10:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-11-15T00:00:00"))
            .done(false)
            .build();

    RecommendationRequest request2 =
        RecommendationRequest.builder()
            .requesterEmail("student2@example.com")
            .professorEmail("professor2@example.com")
            .explanation("Internship recommendation")
            .dateRequested(LocalDateTime.parse("2026-10-22T11:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-12-01T00:00:00"))
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(request1, request2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for POST /api/RecommendationRequest/post

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendation_request() throws Exception {
    LocalDateTime dateRequested = LocalDateTime.parse("2026-10-21T10:00:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2026-11-15T00:00:00");

    RecommendationRequest request =
        RecommendationRequest.builder()
            .requesterEmail("student@example.com")
            .professorEmail("professor@example.com")
            .explanation("Requesting a recommendation")
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(false)
            .build();

    when(recommendationRequestRepository.save(eq(request))).thenReturn(request);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/RecommendationRequest/post")
                    .param("requesterEmail", "student@example.com")
                    .param("professorEmail", "professor@example.com")
                    .param("explanation", "Requesting a recommendation")
                    .param("dateRequested", "2026-10-21T10:00:00")
                    .param("dateNeeded", "2026-11-15T00:00:00")
                    .param("done", "false")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).save(eq(request));

    String expectedJson = mapper.writeValueAsString(request);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_completed_recommendation_request() throws Exception {
    LocalDateTime dateRequested = LocalDateTime.parse("2026-10-21T10:00:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2026-11-15T00:00:00");

    RecommendationRequest request =
        RecommendationRequest.builder()
            .requesterEmail("student@example.com")
            .professorEmail("professor@example.com")
            .explanation("Requesting a recommendation")
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(true)
            .build();

    when(recommendationRequestRepository.save(eq(request))).thenReturn(request);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/RecommendationRequest/post")
                    .param("requesterEmail", "student@example.com")
                    .param("professorEmail", "professor@example.com")
                    .param("explanation", "Requesting a recommendation")
                    .param("dateRequested", "2026-10-21T10:00:00")
                    .param("dateNeeded", "2026-11-15T00:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).save(eq(request));

    String expectedJson = mapper.writeValueAsString(request);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
