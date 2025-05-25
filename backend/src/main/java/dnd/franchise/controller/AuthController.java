
package dnd.franchise.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.validation.Valid;

import dnd.franchise.dto.LoginRequest;
import dnd.franchise.dto.RegisterRequest;
import dnd.franchise.dto.JwtResponse;
import dnd.franchise.service.AuthService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

import lombok.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @Value("${recaptcha.secret}")
  private String recaptchaSecret;

  private boolean verifyRecaptcha(String token) {
    
    String url = "https://www.google.com/recaptcha/api/siteverify";

    RestTemplate restTemplate = new RestTemplate();
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("secret", recaptchaSecret);
    params.add("response", token);

    ResponseEntity<Map> response = restTemplate.postForEntity(url, params, Map.class);

    Map<String, Object> body = response.getBody();
    System.out.println("reCAPTCHA verification response: " + body);

    if (body == null || !Boolean.TRUE.equals(body.get("success"))) {
      System.out.println("reCAPTCHA failed: " + body.get("error-codes"));
      return false;
    }

    return true;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest dto) {
    System.out.println("received token: " + dto.recaptchaToken());

    if (!verifyRecaptcha(dto.recaptchaToken())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid reCAPTCHA");
    }
    try {
      authService.register(dto);
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (IllegalArgumentException ex) {
      // Username exists → 409 Conflict oder 400 Bad Request
      return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(Map.of("error", ex.getMessage()));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
    return ResponseEntity.ok(authService.login(req));
  }
}