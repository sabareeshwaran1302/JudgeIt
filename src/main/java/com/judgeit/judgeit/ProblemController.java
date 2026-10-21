
package com.judgeit.judgeit;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;

@Controller
public class ProblemController {

    private final ProblemRepository problemRepository;

    public ProblemController(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    // Display all problems
    @GetMapping("/problems")
    public String showProblems(Model model) {
        List<Problem> problems = problemRepository.findAll();
        model.addAttribute("problems", problems);
        return "problems";
    }

    @GetMapping("/problems/{id}")
    public String showProblem(@PathVariable Long id, Model model) {
        model.addAttribute("problem", findProblem(id));
        return "problem-detail";
    }

    private Problem findProblem(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
    }

    // Show the Add Problem form
    @GetMapping("/problems/new")
    public String showAddProblemForm(Model model) {
        model.addAttribute("problem", new Problem());
        return "add-problem";
    }

    // Validate and save a new problem
    @PostMapping("/problems")
    public String addProblem(
            @Valid @ModelAttribute("problem") Problem problem,
            BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "add-problem";
        }

        problemRepository.save(problem);
        redirectAttributes.addFlashAttribute("successMessage", "Problem created successfully.");
        return "redirect:/problems";
    }

    // Show the Edit Problem form
    @GetMapping("/problems/edit/{id}")
    public String showEditProblemForm(
            @PathVariable Long id, Model model) {

        Problem problem = findProblem(id);

        model.addAttribute("problem", problem);
        return "edit-problem";
    }

    // Validate and update an existing problem
    @PostMapping("/problems/update/{id}")
    public String updateProblem(
            @PathVariable Long id,
            @Valid @ModelAttribute("problem") Problem formProblem,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        Problem existing = findProblem(id);
        // A rejected form has no ID; keep the update URL tied to the requested record.
        model.addAttribute("problemId", id);

        if (result.hasErrors()) {
            return "edit-problem";
        }

        existing.setTitle(formProblem.getTitle());
        existing.setDifficulty(formProblem.getDifficulty());
        existing.setDescription(formProblem.getDescription());

        problemRepository.save(existing);
        redirectAttributes.addFlashAttribute("successMessage", "Problem updated successfully.");
        return "redirect:/problems";
    }

    // Delete a problem
    @PostMapping("/problems/delete/{id}")
    public String deleteProblem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (problemRepository.existsById(id)) {
            try {
                problemRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Problem deleted successfully.");
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "This problem is linked to submissions or test cases and cannot be deleted.");
            }
        }

        return "redirect:/problems";
    }
}
