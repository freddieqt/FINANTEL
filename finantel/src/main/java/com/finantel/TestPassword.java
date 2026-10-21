package com.finantel;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String pass = encoder.encode("goku1234");

        System.out.println(pass);
    }
}