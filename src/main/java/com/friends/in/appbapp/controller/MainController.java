package com.friends.in.appbapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = { "/thymeleaf" })
public class MainController {

	@RequestMapping(value = { "/testJsp" }, method = RequestMethod.GET)
	public String testJspView() {

		return "index";
	}

	@RequestMapping(value = { "/testThymeleaf" }, method = RequestMethod.GET)
	public String testThymeleafView() {

		return "th_index";
	}

	@RequestMapping(value = { "/testFreeMarker" }, method = RequestMethod.GET)
	public String testFreeMarkerView() {

		return "testFreeMarker";
	}

}