package dnd.franchise.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "users")
public class User {
  @Id @GeneratedValue Long id;
  @Column(unique = true, nullable = false) String username;
  @Column(nullable = false) String password; // BCrypt
}