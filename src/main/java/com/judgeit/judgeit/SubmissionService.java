package com.judgeit.judgeit;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;

    public SubmissionService(SubmissionRepository submissionRepository, ProblemRepository problemRepository) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional
    public Submission createSubmission(Long problemId, String language, String sourceCode) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + problemId));

        Submission submission = new Submission();
        submission.setProblem(problem);
        submission.setLanguage(language.trim());
        submission.setSourceCode(sourceCode);
        submission.setVerdict("PENDING");
        submission.setResultDetails(
                "Your code has been saved. Automated code execution and test-case evaluation are not configured yet.");

        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<Submission> getHistory() {
        return submissionRepository.findAllByOrderBySubmittedAtDesc();
    }

    @Transactional(readOnly = true)
    public Submission getSubmission(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
    }
}
