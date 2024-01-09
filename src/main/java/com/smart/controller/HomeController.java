package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	public UserRepository userRepository;

	@GetMapping("/home")
	public String home(Model m) {
		m.addAttribute("title", "Home - smart contact manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About - smart contact manager");
		return "about";
	}

	@GetMapping("/signin")
	public String login(Model m) {
		m.addAttribute("title", "Login - smart contact manager");
		return "signin";
	}

	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register - smart contact manager");
		m.addAttribute("user", new User());
		return "signup";
	}


	@PostMapping("/do_register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result,@RequestParam("profileImage") MultipartFile file, Model model,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, HttpSession session) {
		try {
			if (result.hasErrors()) {
				model.addAttribute("user", user);
				return "signup";
			}
			User u1 = userRepository.getUserByUserName(user.getEmail());
			if (u1 != null) {
				session.setAttribute("message", new Message("Email Already Registered", "alert-danger"));
				model.addAttribute("user", user);
				return "signup";
			} 
			if (!agreement) {
				session.setAttribute("message", new Message("Please check the Terms and Conditions", "alert-danger"));
				model.addAttribute("user", user);
				return "signup";
			}
			if (file.isEmpty()) {

				user.setImageUrl("default.png");

			} else {
				user.setImageUrl(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			session.setAttribute("message", new Message("Register Successfully", "alert-success"));
			model.addAttribute("user", new User());
			user.setEnabled(true);	
			user.setRole("ROLE_USER");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			userRepository.save(user);
			return "signin";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Soething went wrong ! " + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}

}
