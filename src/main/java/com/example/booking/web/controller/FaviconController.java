package com.example.booking.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaviconController {

	// Redirect /favicon.ico to a hosted restaurant-themed icon
	@GetMapping("/favicon.ico")
	public ResponseEntity<Void> favicon() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.LOCATION, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f374.png"); // 🍴
		return new ResponseEntity<>(headers, HttpStatus.FOUND);
	}
}

