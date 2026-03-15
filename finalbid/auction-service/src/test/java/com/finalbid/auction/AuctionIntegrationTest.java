package com.finalbid.auction;

import com.finalbid.auction.dto.AuctionCreateRequest;
import com.finalbid.auction.dto.AuctionResponse;
import com.finalbid.auction.model.Category;
import com.finalbid.auction.model.Condition;
import com.finalbid.auction.model.DurationOption;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AuctionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String generateToken(String userId) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        return Jwts.builder()
                .subject(userId)
                .claim("username", "testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    void createAndGetAuction_Success() {
        // 1. Arrange: Create request and tokens
        String sellerId = UUID.randomUUID().toString();
        String bidderId = UUID.randomUUID().toString();
        String sellerToken = generateToken(sellerId);
        String bidderToken = generateToken(bidderId);
        
        AuctionCreateRequest request = new AuctionCreateRequest(
            "Integration Test Auction",
            "Full end-to-end test description.",
            Category.ELECTRONICS,
            Condition.NEW,
            new BigDecimal("100.00"),
            DurationOption.HOUR_1
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(sellerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuctionCreateRequest> entity = new HttpEntity<>(request, headers);

        // 2. Act: Create Auction
        ResponseEntity<AuctionResponse> createResponse = restTemplate.postForEntity(
            "/api/auctions", entity, AuctionResponse.class);

        // 3. Assert creation
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        UUID auctionId = createResponse.getBody().id();

        // 4. Act: Place Bid (as a different user)
        HttpHeaders bidHeaders = new HttpHeaders();
        bidHeaders.setBearerAuth(bidderToken);
        bidHeaders.setContentType(MediaType.APPLICATION_JSON);
        // Using BigDecimal in map to be safe
        HttpEntity<Map<String, Object>> bidEntity = new HttpEntity<>(
            Map.of("amount", new BigDecimal("110.00")), 
            bidHeaders
        );
        
        ResponseEntity<Void> bidRes = restTemplate.postForEntity(
            "/api/auctions/" + auctionId + "/bids",
            bidEntity,
            Void.class
        );

        assertEquals(HttpStatus.CREATED, bidRes.getStatusCode());

        // 5. Assert bid impact on auction
        ResponseEntity<AuctionResponse> getResponse = restTemplate.getForEntity(
            "/api/auctions/" + auctionId, AuctionResponse.class);
            
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(0, new BigDecimal("110.00").compareTo(getResponse.getBody().currentPrice()));
        assertEquals(1, getResponse.getBody().bidCount());
    }
}
