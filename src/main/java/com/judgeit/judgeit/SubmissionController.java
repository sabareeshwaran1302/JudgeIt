package com.judgeit.judgeit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SubmissionController {
    private final SubmissionService submissionService;
    private final ProblemRepository problemRepository;

    public SubmissionController(SubmissionService submissionService, ProblemRepository problemRepository) {
        this.submissionService = submissionService;
        this.problemRepository = problemRepository;
    }

    @GetMapping("/submissions")
    public String history(Model model) {
        model.addAttribute("submissions", submissionService.getHistory());
        return "submissions";
    }

    @GetMapping("/submissions/new")
    public String showSubmitForm(@RequestParam(required = false) Long problemId, Model model) {
        model.addAttribute("problems", problemRepository.findAll());
        SubmissionForm form = new SubmissionForm();
        form.setProblemId(problemId);
        model.addAttribute("form", form);
        model.addAttribute("selectedProblemId", problemId);
        return "submit-code";
    }

    @PostMapping("/submissions")
    public String submit(@Valid @ModelAttribute("form") SubmissionForm form,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("problems", problemRepository.findAll());
            model.addAttribute("selectedProblemId", form.getProblemId());
            return "submit-code";
        }

        try {
            Submission saved = submissionService.createSubmission(
                    form.getProblemId(), form.getLanguage(), form.getSourceCode());
            return "redirect:/submissions/" + saved.getId();
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("problemId", "problem.invalid", "Please select an existing problem.");
            model.addAttribute("problems", problemRepository.findAll());
            model.addAttribute("selectedProblemId", form.getProblemId());
            return "submit-code";
        }
    }

    @GetMapping("/submissions/{id}")
    public String result(@PathVariable Long id, Model model) {
        model.addAttribute("submission", submissionService.getSubmission(id));
        return "submission-result";
    }

    public static class SubmissionForm {
        @NotNull(message = "Please select a problem.")
        private Long problemId;

        @NotBlank(message = "Please select a language.")
        @Pattern(regexp = "Java|Python|C|C\\+\\+|JavaScript", message = "Please select a supported language.")
        @Size(max = 40)
        private String language = "Java";

        @NotBlank(message = "Source code cannot be empty.")
        @Size(max = 20000, message = "Source code must not exceed 20,000 characters.")
        private String sourceCode;

        public Long getProblemId() { return problemId; }
        public void setProblemId(Long problemId) { this.problemId = problemId; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getSourceCode() { return sourceCode; }
        public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    }
}
