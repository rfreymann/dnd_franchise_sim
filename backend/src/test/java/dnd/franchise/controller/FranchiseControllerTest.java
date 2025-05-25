package dnd.franchise.controller;

import dnd.franchise.model.Franchise;
import dnd.franchise.repository.FranchiseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FranchiseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FranchiseRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAndFetchFranchise() throws Exception {
        Franchise franchise = new Franchise("Mock Test Inc", 1500, 1000);

        String json = objectMapper.writeValueAsString(franchise);

        // POST
        mockMvc.perform(post("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Mock Test Inc"))
            .andExpect(jsonPath("$.funds").value(1500));

        // GET
        mockMvc.perform(get("/api/franchises"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").exists());
    }
}
