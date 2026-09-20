package com.garbaconnect.controller;

import com.garbaconnect.domain.entity.User;
import com.garbaconnect.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;


@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/test")
    public String test() {
        return "Public API Working";
    }
}