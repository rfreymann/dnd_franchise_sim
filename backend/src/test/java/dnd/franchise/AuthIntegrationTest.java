package dnd.franchise;

import dnd.franchise.dto.RegisterRequest;
import dnd.franchise.dto.LoginRequest;
import dnd.franchise.dto.JwtResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void shouldRegisterLoginAndAccessFranchiseEndpoints() throws Exception {
        // 1. Register
        mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(new RegisterRequest("bob","pwd"))))
          .andExpect(status().isCreated());

        // 2. Login
        MvcResult result = mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(new LoginRequest("bob","pwd"))))
          .andExpect(status().isOk())
          .andReturn();

        String json = result.getResponse().getContentAsString();
        String token = mapper.readValue(json, JwtResponse.class).token();

        // 3. Access franchise without token → unauthorized
        mvc.perform(get("/api/franchise"))
           .andExpect(status().isUnauthorized());

        // 4. Access with token → OK
        mvc.perform(get("/api/franchise")
            .header("Authorization", "Bearer " + token))
           .andExpect(status().isOk());
    }
}