package com.example.carmmunity_capstone.util;

public interface EmailSender {
    void send(String to, String subject, String text);
}
