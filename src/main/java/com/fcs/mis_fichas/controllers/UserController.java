package com.fcs.mis_fichas.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/saludo")
    public String hola(){
        return "Hola";
    }

    @GetMapping("/despedida")
    public String adios(){
        return "Adios";
    }
}
