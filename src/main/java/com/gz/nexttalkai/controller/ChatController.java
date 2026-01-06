package com.gz.nexttalkai.controller;

import com.gz.nexttalkai.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message") String message) {
        return chatService.chat(message);
    }
}
