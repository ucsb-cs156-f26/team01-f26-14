package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticleRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticleRepository articleRepository;

  @MockitoBean UserRepository userRepository;

  // GET /api/articles/all authorization tests

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk());
  }

  // POST /api/articles/post authorization tests

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "Test Article")
                .param("url", "https://example.com")
                .param("explanation", "Helpful resource")
                .param("email", "student@ucsb.edu")
                .param("dateAdded", "2026-10-15T12:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "Test Article")
                .param("url", "https://example.com")
                .param("explanation", "Helpful resource")
                .param("email", "student@ucsb.edu")
                .param("dateAdded", "2026-10-15T12:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // GET all repository test

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {
    LocalDateTime date1 = LocalDateTime.parse("2026-10-15T12:00:00");
    LocalDateTime date2 = LocalDateTime.parse("2026-10-16T12:00:00");

    Article article1 =
        Article.builder()
            .id(1L)
            .title("First Article")
            .url("https://example.com/1")
            .explanation("First resource")
            .email("student@ucsb.edu")
            .dateAdded(date1)
            .build();

    Article article2 =
        Article.builder()
            .id(2L)
            .title("Second Article")
            .url("https://example.com/2")
            .explanation("Second resource")
            .email("student@ucsb.edu")
            .dateAdded(date2)
            .build();

    List<Article> expectedArticles = Arrays.asList(article1, article2);

    when(articleRepository.findAll()).thenReturn(expectedArticles);

    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    verify(articleRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // POST repository test

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {
    LocalDateTime dateAdded = LocalDateTime.parse("2026-10-15T12:00:00");

    Article article =
        Article.builder()
            .title("Test Article")
            .url("https://example.com")
            .explanation("Helpful resource")
            .email("student@ucsb.edu")
            .dateAdded(dateAdded)
            .build();

    when(articleRepository.save(eq(article))).thenReturn(article);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post")
                    .param("title", "Test Article")
                    .param("url", "https://example.com")
                    .param("explanation", "Helpful resource")
                    .param("email", "student@ucsb.edu")
                    .param("dateAdded", "2026-10-15T12:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articleRepository, times(1)).save(article);

    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // GET /api/articles?id=... tests

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/articles").param("id", "1")).andExpect(status().is(403));

    verify(articleRepository, never()).findById(1L);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id() throws Exception {
    Article article =
        Article.builder()
            .id(1L)
            .title("Test Article")
            .url("https://example.com")
            .explanation("Helpful resource")
            .email("student@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-15T12:00:00"))
            .build();

    when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "1"))
            .andExpect(status().isOk())
            .andReturn();

    verify(articleRepository, times(1)).findById(1L);

    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_get_by_id_not_found() throws Exception {
    when(articleRepository.findById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/articles").param("id", "999")).andExpect(status().isNotFound());

    verify(articleRepository, times(1)).findById(999L);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_get_by_id() throws Exception {
    Article article =
        Article.builder()
            .id(2L)
            .title("Admin Test Article")
            .url("https://example.com/admin")
            .explanation("Admin test")
            .email("admin@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-15T12:00:00"))
            .build();

    when(articleRepository.findById(2L)).thenReturn(Optional.of(article));

    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "2"))
            .andExpect(status().isOk())
            .andReturn();

    verify(articleRepository, times(1)).findById(2L);

    assertEquals(mapper.writeValueAsString(article), response.getResponse().getContentAsString());
  }

  // PUT /api/articles?id=... tests

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/articles")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/articles")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_update_article() throws Exception {
    Article existingArticle =
        Article.builder()
            .id(1L)
            .title("Old Title")
            .url("https://example.com/old")
            .explanation("Old explanation")
            .email("old@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-15T12:00:00"))
            .build();

    Article updatedArticle =
        Article.builder()
            .id(1L)
            .title("New Title")
            .url("https://example.com/new")
            .explanation("New explanation")
            .email("new@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-20T12:00:00"))
            .build();

    when(articleRepository.findById(1L)).thenReturn(Optional.of(existingArticle));
    when(articleRepository.save(eq(updatedArticle))).thenReturn(updatedArticle);

    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles")
                    .param("id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(updatedArticle))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articleRepository, times(1)).findById(1L);
    verify(articleRepository, times(1)).save(updatedArticle);

    assertEquals(
        mapper.writeValueAsString(updatedArticle), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_update_nonexistent_article() throws Exception {
    Article incomingArticle =
        Article.builder()
            .title("New Title")
            .url("https://example.com/new")
            .explanation("New explanation")
            .email("student@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-20T12:00:00"))
            .build();

    when(articleRepository.findById(999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            put("/api/articles")
                .param("id", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(incomingArticle))
                .with(csrf()))
        .andExpect(status().isNotFound());

    verify(articleRepository, times(1)).findById(999L);
  }
}
