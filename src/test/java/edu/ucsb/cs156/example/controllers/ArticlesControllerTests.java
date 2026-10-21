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
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticleRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
}
