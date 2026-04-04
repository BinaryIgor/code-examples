package com.binaryigor.complexity_alternative.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping
    String home(Model model) {
        return "forward:/devices";
    }
}
