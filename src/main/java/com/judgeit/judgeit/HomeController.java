
package com.judgeit.judgeit;

import org.springframework.stereotype.Controller;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProblemRepository problems;
    private final SubmissionRepository submissions;

    public HomeController(ProblemRepository problems, SubmissionRepository submissions) {
        this.problems = problems;
        this.submissions = submissions;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("problemCount", problems.count());
        model.addAttribute("submissionCount", submissions.count());
        model.addAttribute("acceptedCount", submissions.countByVerdictIgnoreCase("ACCEPTED"));
        model.addAttribute("recentProblems",
                problems.findAll(PageRequest.of(0, 4, Sort.by("id").descending())).getContent());
        return "index";
    }
}
