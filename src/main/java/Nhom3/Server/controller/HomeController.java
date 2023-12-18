package Nhom3.Server.controller;

import Nhom3.Server.service.General;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Base64;

@RestController
public class HomeController {
    @GetMapping("/home")
    public String home(){
        return "home";
    }
    public static void mains(String[] args) {
        // Độ dài của secret key (byte)
//        int keyLength = 256 / 8; // Ví dụ: 256 bit secret key
//
//        // Tạo một mảng byte ngẫu nhiên
//        byte[] key = new byte[keyLength];
//        SecureRandom secureRandom = new SecureRandom();
//        secureRandom.nextBytes(key);
//
//        // Mã hóa secret key thành dạng Base64 để sử dụng trong JWT
//        String base64Key = Base64.getEncoder().encodeToString(key);

//         In ra secret key dưới dạng Base64
//        System.out.println("JWT Secret Key: " + base64Key);

        System.out.println(General.checkValidPin("a345"));
    }
}
