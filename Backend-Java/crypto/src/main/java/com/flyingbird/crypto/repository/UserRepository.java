package com.flyingbird.crypto.repository;

import com.flyingbird.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /** Only enabled users — used by login so disabled accounts can't obtain a token. */
    Optional<User> findByUsernameAndEnabledTrue(String username);

    boolean existsByUsername(String username);
}
