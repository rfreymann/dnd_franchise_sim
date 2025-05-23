
package dnd.franchise.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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


  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest dto) {
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