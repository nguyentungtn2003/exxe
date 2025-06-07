package com.example.exe2update.controller;

// ChatController.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.exe2update.dto.Request.ChatRequest;
import com.example.exe2update.dto.Response.ChatResponse;
import com.example.exe2update.service.impl.ChatService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = chatService.askChatGPT(request.getMessage());
        System.out.println("ChatGPT reply: " + reply); // log câu trả lời
        return new ChatResponse(reply);
    }

}
