package Nhom3.Server.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestThreadConsumerProducer {

    public static void main(String[] args) {
        SharedData sd = new SharedData();
        new Producer(sd).start();
        new Thread(new Consumer(sd)).start();
    }
}

class Consumer implements Runnable {

    SharedData sd;

    public Consumer(SharedData sd) {
        this.sd = sd;
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            try {
                sd.consume();
            } catch (InterruptedException ex) {
                Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

class Producer extends Thread {

    SharedData sd;

    public Producer(SharedData sd) {
        this.sd = sd;
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            try {
                sd.produce((int) (Math.random() * 100));
            } catch (InterruptedException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

class SharedData {

    int data;
    boolean produced = false;

    public synchronized void produce(int data) throws InterruptedException {
        if (produced) {
            this.wait();
        }
        this.data = data;
        System.out.println("produce: " + data);
        produced = true;
        this.notify();
    }

    public synchronized void consume() throws InterruptedException {
        if (!produced) {
            this.wait();
        }
        System.out.println("consume: " + data);
        this.data = 0;
        produced = false;
        this.notify();
    }
}
