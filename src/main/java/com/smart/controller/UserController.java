package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	public UserRepository userRepository;

	@Autowired
	public ContactRepository contactRepository;

	//------------------------------------ common data for all handler -----------------------------------------------

	@ModelAttribute
	public void commonData(Model model, Principal principal) {
		String username = principal.getName();

		User user = userRepository.getUserByUserName(username);

		model.addAttribute("user", user);

	}

	//------------------------------------ Home dashboard handler -----------------------------------------------

	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");

		return "normal/user_dashboard";
	}

	//------------------------------------ Add contact handler -----------------------------------------------
	
	@GetMapping("/add_contact")
	public String addContact(Model model) {

		model.addAttribute("title", "Add contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact";
	}

	//------------------------------------ process add contact form -----------------------------------------------


	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			if (file.isEmpty()) {

				contact.setImage("contact.png");

			} else {
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			contact.setUser(user);
			user.getContact().add(contact);
			this.userRepository.save(user);

			session.setAttribute("message", new Message("Contact added successfully !! Add more :)", "success"));

		} catch (Exception e) {
			System.out.println("Error :" + e.getMessage());
			e.printStackTrace();

			session.setAttribute("message", new Message("Something went wrong!! Try again :(", "danger"));

		}

		return "normal/add_contact";
	}
	
	//------------------------------------ show all contact handler -----------------------------------------------

	// current page
	// contact per page 5

	@GetMapping("/show_contact/{page}")
	public String showContact(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "show contact");

		String name = principal.getName();

		User user = this.userRepository.getUserByUserName(name);
//		user.getContact();

		// current page
		// contact per page 5
		Pageable pageable = PageRequest.of(page, 6);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contact";
	}
	
	//------------------------------------ open contact details handler -----------------------------------------------


	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		Optional<Contact> findById = this.contactRepository.findById(cId);
		Contact contact = findById.get();
		
		String userName = principal.getName();

		User user = userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {		
		     model.addAttribute("contact", contact);
		     model.addAttribute("title",contact.getName());
		}

		return "normal/contact_details";
	}
	
	//------------------------------------delete contact handler -----------------------------------------------
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Principal principal, Model model, HttpSession session) throws IOException {
		
//		Optional<Contact> optionalContact = this.contactRepository.findById(cId);
		Contact contact = this.contactRepository.findById(cId).get();
		
		
		//remove image
		//img
		//contact.getimage
	
		File file = new ClassPathResource("static/img").getFile();
		File file2 = new File(file, contact.getImage());
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {	
			
			 file2.delete();
			 
		    this.contactRepository.delete(contact);   
		   
		    session.setAttribute("message", new Message("Contact deleted successfully :)","success"));
		}
		return "redirect:/user/show_contact/0";
	}
	
	//------------------------------------ open update handler -----------------------------------------------
	
	@GetMapping("/update_contact/{cId}")
	public String update_contact(@PathVariable("cId") Integer cId,Model model,Principal principal, HttpSession session){
		
		model.addAttribute("title","update contact");
		Contact contact = this.contactRepository.findById(cId).get();
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {	
		    model.addAttribute("contact", contact);
		    return "normal/update_contact";
	    }else {
	    	 session.setAttribute("message", new Message("You don't have permission to update this contact:)","danger"));
	        return "redirect:/user/show_contact/0";
	    }
		
    }
	
	//------------------------------------ update contact -----------------------------------------------

	@PostMapping("/process-update")
	public String processUpdate(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, Principal principal, HttpSession session) {
		

		try {
			
			Contact oldContact = this.contactRepository.findById(contact.getcId()).get();

			if (!file.isEmpty()) {
				
				// delete old image
				
				File file1 = new ClassPathResource("static/img").getFile();
				File file2 = new File(file1, oldContact.getImage());
				file2.delete();
				
				
                // update new image
				
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());				

			} else {
				contact.setImage(oldContact.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Contact update successfully :)", "success"));

		} catch (Exception e) {
			
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong!! Try again :(", "danger"));

		}

		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//------------------------------------ update contact -----------------------------------------------
	
	@GetMapping("/profile")
	public String profile(Model model) {
		model.addAttribute("title","profile page");
		return "normal/profile";
	}
	
	
	
}
