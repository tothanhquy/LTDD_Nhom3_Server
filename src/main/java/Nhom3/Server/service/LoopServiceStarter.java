package Nhom3.Server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class LoopServiceStarter implements ApplicationRunner {

    private final LoopService loopService;

    @Autowired
    public LoopServiceStarter(LoopService loopService) {
        this.loopService = loopService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Start a new thread with the LoopService
        new Thread(loopService).start();
    }
}
