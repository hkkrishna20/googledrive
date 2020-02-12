package com.friends.in.appbapp.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.friends.in.appbapp.model.User;


@Controller
public class UserController {

	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public ModelAndView showForm() {
		return new ModelAndView("userHome", "user", new User());
	}

	@RequestMapping(value = "/addUser", method = RequestMethod.POST)
	public String submit(@Valid @ModelAttribute("user") User user, BindingResult result, ModelMap model) {
		if (result.hasErrors()) {
			return "error";
		}
		model.addAttribute("name", user.getName());
		model.addAttribute("contactNumber", user.getContactNumber());
		model.addAttribute("id",user.getId());
		return "UserView";
	}

	@PostMapping("/searchDetails") // it only support port method
	public ModelAndView saveDetails(@RequestParam("Username") String username,
			@RequestParam("password") String password, HttpServletRequest request, HttpServletResponse response,
			Model model) throws GeneralSecurityException, IOException {
			ModelAndView mav = new ModelAndView("index");

		/*
		 *
		 * 
		 * File fileContent = new java.io.File(Environment.getExternalStorageDirectory()
		 * + "/DCIM/Camera/" + Files[count].getName());
		 * 
		 * FileContent mediaContent = new FileContent("image/jpeg", fileContent);
		 * 
		 * com.google.api.services.drive.model.File body = new
		 * com.google.api.services.drive.model.File();
		 * 
		 * body.setTitle(fileContent.getName()); body.setMimeType("image/jpeg");
		 * 
		 * body.setParents(Arrays.asList(new ParentReference().setId(folderId)));
		 * 
		 * com.google.api.services.drive.model.File file = drive.files().insert(body,
		 * mediaContent).execute();
		 */
		// write your code to save details
		return mav; // welcome is view name. It will call welcome.jsp
	}
}