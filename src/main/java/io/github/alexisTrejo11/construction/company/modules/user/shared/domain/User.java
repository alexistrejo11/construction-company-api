package io.github.alexisTrejo11.construction.company.modules.user.shared.domain;

import io.github.alexisTrejo11.construction.company.shared.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.EnumSet;
import java.util.Set;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends AbstractJpaEntity {
  @Column(nullable = false, unique = true, length = 150)
  private String email;
  @Column(name = "password_hash", length = 255)
  private String passwordHash;
  @Column(name = "first_name", length = 100)
  private String firstName;
  @Column(name = "last_name", length = 100)
  private String lastName;
  @Column(length = 20)
  private String phone;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserStatus status = UserStatus.INVITED;
  @ElementCollection(targetClass = UserRole.class)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 30)
  private Set<UserRole> roles = EnumSet.noneOf(UserRole.class);
}
