package com.gz.nexttalkai.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {



    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message") String message) {
        return null;
    }
}
