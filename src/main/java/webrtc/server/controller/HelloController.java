package webrtc.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import webrtc.server.cache.GuavaCache;

import java.util.List;

@RestController
public class HelloController {

    @Autowired
    private GuavaCache cache;

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }

    @GetMapping("/clients")
    @ResponseBody
    public List clients() {
        List clients = (List) cache.get("clients");

        return clients;
    }
}
