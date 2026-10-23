package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
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
import org.springframework.http.MediaType;
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

  // Tests for GET /api/RecommendationRequest?id=7

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/RecommendationRequest").param("id", "7")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_id_exists() throws Exception {
    LocalDateTime dateRequested = LocalDateTime.parse("2026-10-21T10:00:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2026-11-15T00:00:00");

    RecommendationRequest request =
        RecommendationRequest.builder()
            .requesterEmail("student@example.com")
            .professorEmail("professor@example.com")
            .explanation("Graduate school recommendation")
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(request));

    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));

    String expectedJson = mapper.writeValueAsString(request);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_get_by_id_when_id_does_not_exist() throws Exception {
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  // Tests for PUT /api/RecommendationRequest?id=7

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/RecommendationRequest")
                .param("id", "7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/RecommendationRequest")
                .param("id", "7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_update_a_recommendation_request() throws Exception {
    RecommendationRequest existingRequest =
        RecommendationRequest.builder()
            .requesterEmail("old-student@example.com")
            .professorEmail("old-professor@example.com")
            .explanation("Old explanation")
            .dateRequested(LocalDateTime.parse("2026-10-20T10:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-11-10T00:00:00"))
            .done(false)
            .build();

    existingRequest.setId(7L);

    RecommendationRequest incomingRequest =
        RecommendationRequest.builder()
            .requesterEmail("new-student@example.com")
            .professorEmail("new-professor@example.com")
            .explanation("Updated recommendation request")
            .dateRequested(LocalDateTime.parse("2026-10-22T12:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-12-15T00:00:00"))
            .done(true)
            .build();

    RecommendationRequest expectedRequest =
        RecommendationRequest.builder()
            .id(7L)
            .requesterEmail("new-student@example.com")
            .professorEmail("new-professor@example.com")
            .explanation("Updated recommendation request")
            .dateRequested(LocalDateTime.parse("2026-10-22T12:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-12-15T00:00:00"))
            .done(true)
            .build();

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(existingRequest));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/RecommendationRequest")
                    .param("id", "7")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incomingRequest))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    verify(recommendationRequestRepository, times(1)).save(eq(existingRequest));

    String expectedJson = mapper.writeValueAsString(expectedRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_cannot_update_a_nonexistent_recommendation_request() throws Exception {
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    RecommendationRequest incomingRequest =
        RecommendationRequest.builder()
            .requesterEmail("student@example.com")
            .professorEmail("professor@example.com")
            .explanation("Updated explanation")
            .dateRequested(LocalDateTime.parse("2026-10-22T12:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-12-15T00:00:00"))
            .done(true)
            .build();

    MvcResult response =
        mockMvc
            .perform(
                put("/api/RecommendationRequest")
                    .param("id", "7")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incomingRequest))
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }
}
