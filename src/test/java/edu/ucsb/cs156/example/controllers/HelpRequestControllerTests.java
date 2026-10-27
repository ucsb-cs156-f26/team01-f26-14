package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockBean HelpRequestRepository helpRequestRepository;

  @MockBean UserRepository userRepository;

  // Tests for GET /api/HelpRequest/all

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_help_requests() throws Exception {

    HelpRequest request1 =
        HelpRequest.builder()
            .requesterEmail("student1@example.com")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 3")
            .requestTime(LocalDateTime.parse("2026-10-21T10:00:00"))
            .explanation("Need help debugging Java")
            .solved(false)
            .build();

    HelpRequest request2 =
        HelpRequest.builder()
            .requesterEmail("student2@example.com")
            .teamId("team02")
            .tableOrBreakoutRoom("Breakout Room 2")
            .requestTime(LocalDateTime.parse("2026-10-22T11:00:00"))
            .explanation("Need help with Spring Boot")
            .solved(true)
            .build();

    ArrayList<HelpRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(request1, request2));

    when(helpRequestRepository.findAll()).thenReturn(expectedRequests);

    MvcResult response =
        mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().isOk()).andReturn();

    verify(helpRequestRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  // Tests for POST /api/HelpRequest/post

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_help_request() throws Exception {

    LocalDateTime requestTime = LocalDateTime.parse("2026-10-21T10:00:00");

    HelpRequest request =
        HelpRequest.builder()
            .requesterEmail("student@example.com")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 3")
            .requestTime(requestTime)
            .explanation("Need help debugging Java")
            .solved(true)
            .build();

    when(helpRequestRepository.save(eq(request))).thenReturn(request);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/HelpRequest/post")
                    .param("requesterEmail", "student@example.com")
                    .param("teamId", "team01")
                    .param("tableOrBreakoutRoom", "Table 3")
                    .param("requestTime", "2026-10-21T10:00:00")
                    .param("explanation", "Need help debugging Java")
                    .param("solved", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).save(eq(request));

    String expectedJson = mapper.writeValueAsString(request);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  // Tests for GET /api/HelpRequest when ID does not exist

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/HelpRequest").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(7L));

    Map<String, Object> json = responseToJson(response);

    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 7 not found", json.get("message"));
  }

  // Tests for GET /api/HelpRequest when ID exists

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    HelpRequest request =
        HelpRequest.builder()
            .id(7L)
            .requesterEmail("student@example.com")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 3")
            .requestTime(LocalDateTime.parse("2026-10-21T10:00:00"))
            .explanation("Need help debugging Java")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(request));

    MvcResult response =
        mockMvc
            .perform(get("/api/HelpRequest").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(7L));

    String expectedJson = mapper.writeValueAsString(request);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  // Tests for PUT /api/HelpRequest when ID exists

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_update_an_existing_help_request() throws Exception {

    HelpRequest existingRequest =
        HelpRequest.builder()
            .id(7L)
            .requesterEmail("old@example.com")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 3")
            .requestTime(LocalDateTime.parse("2026-10-21T10:00:00"))
            .explanation("Original explanation")
            .solved(false)
            .build();

    HelpRequest updatedRequest =
        HelpRequest.builder()
            .id(7L)
            .requesterEmail("new@example.com")
            .teamId("team02")
            .tableOrBreakoutRoom("Breakout Room 2")
            .requestTime(LocalDateTime.parse("2026-10-22T11:00:00"))
            .explanation("Updated explanation")
            .solved(true)
            .build();

    HelpRequest incomingRequest =
        HelpRequest.builder()
            .id(999L)
            .requesterEmail("new@example.com")
            .teamId("team02")
            .tableOrBreakoutRoom("Breakout Room 2")
            .requestTime(LocalDateTime.parse("2026-10-22T11:00:00"))
            .explanation("Updated explanation")
            .solved(true)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(existingRequest));

    when(helpRequestRepository.save(eq(updatedRequest))).thenReturn(updatedRequest);

    MvcResult response =
        mockMvc
            .perform(
                put("/api/HelpRequest")
                    .param("id", "7")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incomingRequest))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(7L));
    verify(helpRequestRepository, times(1)).save(eq(updatedRequest));

    String expectedJson = mapper.writeValueAsString(updatedRequest);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  // Tests for PUT /api/HelpRequest when ID does not exist

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_cannot_update_a_nonexistent_help_request() throws Exception {

    HelpRequest incomingRequest =
        HelpRequest.builder()
            .requesterEmail("student@example.com")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 3")
            .requestTime(LocalDateTime.parse("2026-10-21T10:00:00"))
            .explanation("Need help debugging Java")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/HelpRequest")
                    .param("id", "999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incomingRequest))
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(999L));
    verify(helpRequestRepository, never()).save(any(HelpRequest.class));

    Map<String, Object> json = responseToJson(response);

    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 999 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_delete_an_existing_help_request() throws Exception {

    HelpRequest request = HelpRequest.builder().id(7L).build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(java.util.Optional.of(request));

    MvcResult response =
        mockMvc
            .perform(delete("/api/HelpRequest").param("id", "7").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(7L));
    verify(helpRequestRepository, times(1)).deleteById(eq(7L));

    assertEquals("record 7 deleted", response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_cannot_delete_a_nonexistent_help_request() throws Exception {

    when(helpRequestRepository.findById(eq(999L))).thenReturn(java.util.Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/HelpRequest").param("id", "999").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(999L));
    verify(helpRequestRepository, never()).deleteById(any(Long.class));

    assertEquals("record 999 not found", response.getResponse().getContentAsString());
  }
}
