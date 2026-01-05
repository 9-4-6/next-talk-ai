package com.gz.nexttalkai.controller;

import com.gz.nexttalkai.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    @PostMapping("/chat")
    public String chat(@RequestParam(value = "message") String message) {
        return chatService.chat(message);
    }
}
