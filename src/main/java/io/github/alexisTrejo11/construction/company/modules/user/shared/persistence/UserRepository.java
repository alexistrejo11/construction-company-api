package io.github.alexisTrejo11.construction.company.modules.user.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
}
