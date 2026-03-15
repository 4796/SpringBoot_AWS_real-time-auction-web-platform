package com.finalbid.auction.controller;

import com.finalbid.auction.dto.AuctionCreateRequest;
import com.finalbid.auction.service.AuctionService;
import com.finalbid.auction.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalbid.auction.ratelimit.RateLimitingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuctionController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitingFilter.class)
)
@Import(com.finalbid.auction.config.SecurityConfig.class)
class AuctionControllerJsonTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuctionService auctionService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void createAuction_InvalidInput_ReturnsProblemDetails() throws Exception {
        AuctionCreateRequest invalidRequest = new AuctionCreateRequest(
            "", // Blank title
            "Description",
            null, // Missing category
            null, // Missing condition
            BigDecimal.ZERO, // Invalid price
            null // Missing duration
        );

        mockMvc.perform(post("/api/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.detail").exists());
    }
}
