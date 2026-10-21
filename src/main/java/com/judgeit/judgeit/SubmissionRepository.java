package com.judgeit.judgeit;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    long countByVerdictIgnoreCase(String verdict);

    // Templates read the problem after the service transaction has closed.
    @EntityGraph(attributePaths = "problem")
    List<Submission> findAllByOrderBySubmittedAtDesc();

    @Override
    @EntityGraph(attributePaths = "problem")
    Optional<Submission> findById(Long id);
}
