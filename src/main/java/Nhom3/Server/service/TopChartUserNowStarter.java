package Nhom3.Server.service;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.TopChartUserNow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TopChartUserNowStarter implements ApplicationRunner {

    @Autowired
    AccountService accountService;

//    @Autowired
//    public TopChartUserNowStarter(LoopService loopService) {
//        this.accountService = loopService;
//    }
    @Override
    public void run(ApplicationArguments args) {
        // Start a new thread with the LoopService
        System.out.println("TopChartUserNowStarter");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AccountModel> items = accountService.getAll();
                ArrayList<TopChartUserNow.User> topUsers = new ArrayList<>(items.stream().map(e-> new TopChartUserNow.User(e.getId(),e.getMoneyNow())).toList());
                TopChartUserNow.setInit(topUsers);
            }
        }).start();
    }
}
