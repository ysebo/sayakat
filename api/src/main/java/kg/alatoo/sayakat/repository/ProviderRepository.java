package kg.alatoo.sayakat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kg.alatoo.sayakat.entity.Provider;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByUserId(Long userId);
}
