package dnd.franchise.service;

import dnd.franchise.model.User;
import dnd.franchise.repository.UserRepository;
import dnd.franchise.config.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import dnd.franchise.dto.RegisterRequest;
import dnd.franchise.exception.UsernameAlreadyExistsException;
import dnd.franchise.dto.LoginRequest;
import dnd.franchise.dto.JwtResponse;

@Service
@Transactional
public class AuthService {
  private final UserRepository repo;
  private final PasswordEncoder enc;
  private final JwtTokenProvider tokenProvider;

  public AuthService(UserRepository repo,
                     PasswordEncoder enc,
                     JwtTokenProvider tokenProvider) {
    this.repo = repo;
    this.enc = enc;
    this.tokenProvider = tokenProvider;
  }

  public void register(RegisterRequest req) {
    repo.findByUsername(req.username())
        .ifPresent(u -> { throw new UsernameAlreadyExistsException(req.username()); });
    User u = new User();
    u.setUsername(req.username());
    u.setPassword(enc.encode(req.password()));
    repo.save(u);
  }

  public JwtResponse login(LoginRequest req) {
    User u = repo.findByUsername(req.username())
        .orElseThrow(() -> new BadCredentialsException("Invalid login"));
    if (!enc.matches(req.password(), u.getPassword())) {
      throw new BadCredentialsException("Invalid login");
    }
    // Create JWT using tokenProvider
    String token = tokenProvider.createToken(u.getUsername(), 24); // valid 24h
    return new JwtResponse(token);
  }
}
