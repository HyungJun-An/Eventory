package com.eventory.auth.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
public class RedisTest {
    @GetMapping("/set")
    public String setSessionAttribute(@RequestParam String name, HttpSession session) {
        session.setAttribute("name", name);
        return "Session 'name' set to : " + name;
    }

    @GetMapping("/get")
    public String getSessionAttribute(HttpSession session) {
        String name = (String) session.getAttribute("name");

        if(name != null)
        {
            return "Session attribute 'name' is: " + name;
        } else {
            return "Session attribute 'name' is not set.";
        }
    }

    @GetMapping("/invalidate")
    public String invalidateSession(HttpSession session) {
        session.invalidate();
        return "Session invalidated.";
    }
}
