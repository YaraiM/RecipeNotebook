package raisetech.RecipeNotebook.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CsrfController.class)
@WithMockUser(username = "user", roles = "USER")
class CsrfControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean
  CsrfToken csrfToken;

  @Test
  void CSRFトークンが取得できること() throws Exception {
    mockMvc.perform(get("/csrf-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.token").isString())
        .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"));

  }

}
