package Nhom3.Server.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

public class General {
    public static String getRandomString(int targetStringLength) {

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
        return generatedString;
    }
    public static boolean checkValidNumberPhone(String numberPhone){
        String regex = "^(84[35789])(\\d{8})$";
        return Pattern.matches(regex, numberPhone);
    }
    public static String hashPassword(String rawPassword){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // Mã hóa mật khẩu
        return passwordEncoder.encode(rawPassword);
    }
    public static boolean verifyPassword(String rawPassword,String encodedPassword){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    public static boolean checkValidPassword(String password){
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%?&])[A-Za-z\\d@$!%?&]{8,}$";
        return Pattern.matches(regex, password);
    }
    public static boolean checkValidPin(String pin){
        String regex = "^(\\d{4})$";
        return Pattern.matches(regex, pin);
    }
    public static String ValidPasswordConstrain = "\\nMật Khẩu:\\nÍt nhất 8 ký tự.\\nChứa ít nhất một chữ hoa và một chữ thường.\\nChứa ít nhất một chữ số và một ký tự đặc biệt.";

    public static String addDotIntoNumber(String numberString) {
        // Create a DecimalFormat instance
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        // Format the number string with commas
        String formattedString = decimalFormat.format(Long.parseLong(numberString));
        return formattedString;
    }
    public static String addDotIntoNumber(long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        // Format the date
        return sdf.format(new Date(milliseconds));
    }
    public static void mains(String arg[]){
        System.out.println(addDotIntoNumber(1674950400000L));
    }

}
