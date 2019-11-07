package fr.pcscol.printer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrinterController {

    @GetMapping
    public String helloGradle() {
        return "Hello Gradle!";
    }

}
