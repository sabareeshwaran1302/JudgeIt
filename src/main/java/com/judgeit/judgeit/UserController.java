
package com.judgeit.judgeit;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Show registration page
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Register a new user
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {

        if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue(
                    "email",
                    "duplicate",
                    "An account with this email already exists."
            );
        }

        if (result.hasErrors()) {
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Account created. You can now log in.");

        return "redirect:/login";
    }

    // Show login page
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    // Process login
    @PostMapping("/login")
    public String loginUser(
            @ModelAttribute("user") User loginUser,
            Model model,
            HttpSession session) {

        User user = userRepository.findByEmail(loginUser.getEmail())
                .orElse(null);

        if (user == null ||
                !passwordEncoder.matches(
                        loginUser.getPassword(),
                        user.getPassword())) {

            model.addAttribute(
                    "loginError",
                    "Invalid email or password."
            );

            return "login";
        }

        session.setAttribute("loggedInUser", user);

        return "redirect:/profile";
    }

    // Show profile
    @GetMapping("/profile")
    public String showProfile(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        return "profile";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
