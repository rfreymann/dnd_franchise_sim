
package dnd.franchise.controller;

import lombok.RequiredArgsConstructor;

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

import lombok.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  private boolean verifyRecaptcha(String token) {
    String secret = "6Ld2F0crAAAAAE6U2softWy9G6_68yXgAfeDdPen";
    String url = "https://www.google.com/recaptcha/api/siteverify";

    RestTemplate restTemplate = new RestTemplate();
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("secret", secret);
    params.add("response", token);

    ResponseEntity<Map> response = restTemplate.postForEntity(url, params, Map.class);
    return (Boolean) response.getBody().get("success");
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest dto) {
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