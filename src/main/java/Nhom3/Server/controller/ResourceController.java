package Nhom3.Server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/resource")
public class ResourceController {
    public static Path USER_AVATAR_PATH = Paths.get("upload_resource","user_avatar");
    public static int USER_AVATAR_LIMIT_MB_SIZE = 5;//5 mb
//    @Autowired
//    ResourceLoader resourceLoader;

    public boolean isSafeFilename(String filename) {
        // Define a regular expression pattern to match a safe filename
        String pattern = "^[a-zA-Z0-9-_.]+$";

        // Use the matches() method of the String class to check if the filename matches the pattern
        return filename.matches(pattern);
    }
    @GetMapping("/account/avatar/{filename}")
    public ResponseEntity<Resource> getAccountAvatar(@PathVariable(name="filename") String filename) {
//        Resource resource = resourceLoader.getResource("classpath:upload/baiviet/"+id+"/" + filename);

        if(!isSafeFilename(filename)){
            return ResponseEntity.notFound().build();
        }
        try{
            //get id;
//            System.out.println(filename);
//            int id = Integer.parseInt(filename.split("\\.")[0]);
            Resource resource = new FileSystemResource(new File(USER_AVATAR_PATH.resolve(filename).toString()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);

        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }
}
