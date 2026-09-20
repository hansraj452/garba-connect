
package com.garbaconnect.controller;

import com.garbaconnect.domain.entity.User;
import com.garbaconnect.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;


@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/reports")
    public String reports() {
        return "All Reports";
    }

    @PatchMapping("/users/{id}/suspend")
    public String suspend(@PathVariable String id) {
        return "User Suspended";
    }

    @DeleteMapping("/users/{id}")
    public String delete(@PathVariable String id) {
        return "User Deleted";
    }
}