package com.nickchenyx.util;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * port scanner
 *
 * @author nickChen
 * @date 2020/11/8
 */
public class Jdial {

    public static void main(String[] args) throws InterruptedException {
        ArgumentParser parser = ArgumentParsers.newFor("dail").build()
                .defaultHelp(true)
                .description("An example of server ports scanning.");
        parser.addArgument("--hostname")
                .setDefault("")
                .required(true)
                .type(String.class)
                .help("hostname to test");
        parser.addArgument("--start-port")
                .setDefault(80)
                .type(Integer.class)
                .help("the port on which the scanning starts");
        parser.addArgument("--end-port")
                .setDefault(100)
                .type(Integer.class)
                .help("the port from which the scanning ends");
        parser.addArgument("--timeout")
                .setDefault(200)
                .type(Integer.class)
                .help("timeout");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.printHelp();
            System.exit(1);
        }
        String hostname = ns.getString("hostname");
        Integer startPort = ns.getInt("start_port");
        Integer endPort = ns.getInt("end_port");
        Integer timeout = ns.getInt("timeout");

        testSingleThread(hostname, startPort, endPort, timeout);

//        testMultiThread(hostname, startPort, endPort, timeout);

//        testFiber(hostname, startPort, endPort, timeout);

    }

    static void testSingleThread(String hostname, int startPort, int endPort, int timeout) {
        long start = System.currentTimeMillis();
        for (int port = startPort; port < endPort; port++) {
            boolean open = isOpen(hostname, port, timeout);
            System.out.println("port: " + port + " is " + (open ? "open" : "close"));
        }
        System.out.println("time elapse: " + (System.currentTimeMillis() - start));
        // single thread for
        // time elapse: 5574
    }

    static void testMultiThread(String hostname, int startPort, int endPort, int timeout)
            throws InterruptedException {
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        CountDownLatch latch = new CountDownLatch(endPort - startPort + 1);
        for (int port = startPort; port <= endPort; port++) {
            int finalPort = port;
            executorService.submit(() -> {
                boolean open = isOpen(hostname, finalPort, timeout);
                if (open) {
                    queue.add(finalPort);
                }
                latch.countDown();
            });
        }

        latch.await();

        System.out.println("port open: " + queue.stream().map(String::valueOf).reduce((l, r) -> {
            return l + ", " + r;
        }));
        System.out.println("time elapse: " + (System.currentTimeMillis() - start));
        executorService.shutdown();

        // 3 thread
        // time elapse: 5557

        // 5 thread
        // time elapse: 5635

        // 10 thread
        // time elapse: 5336

        // 20 thread
        // time elapse: 3606

        // 30 thread
        // time elapse: 3495

        // 40 thread
        // time elapse: 2804

        // 50 thread
        // time elapse: 2309

        // 60 thread
        // time elapse: 1994

        // 70 thread
        // time elapse: 1661

        // 80 thread
        // time elapse: 1580

        // 200 thread
        // time elapse: 942

        // 500 thread
        // time elapse: 517

        // 1000 thread
        // time elapse: 434
    }

    /**
     * 使用  mvn compile dependency:properties exec:exec 运行
     */
    static void testFiber(String hostname, int startPort, int endPort, int timeout)
            throws InterruptedException {

        long start = System.currentTimeMillis();
        LinkedBlockingQueue<Integer> queue1 = new LinkedBlockingQueue<>();
        CountDownLatch latch1 = new CountDownLatch(endPort - startPort + 1);
        for (int port = startPort; port <= endPort; port++) {
            int finalPort = port;

            new Fiber<Void>(() -> {
                boolean opened = isOpenFiber(hostname, finalPort, timeout);
                // fiber 如果使用原生的 Socket 实现 connect，性能低下。在超过5万的数量级时，性能反而远远低于200个线程
                // boolean opened = isOpen(hostname, finalPort, timeout);
                if (opened) {
                    queue1.add(finalPort);
                }
                latch1.countDown();
            }).start();
        }

        latch1.await();
        System.out.println("port open: " + queue1.stream().map(String::valueOf).reduce((l, r) -> {
            return l + ", " + r;
        }));

        System.out.println("time elapse: " + (System.currentTimeMillis() - start));
    }

    /**
     * 注解 @Suspendable 是必须的
     */
    @Suspendable
    static boolean isOpenFiber(String host, int port, int timeout) {
        try (FiberSocketChannel channel = FiberSocketChannel.open()) {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            channel.connect(isa, timeout, TimeUnit.MILLISECONDS);
            return channel.isOpen();
        } catch (SuspendExecution | IOException | TimeoutException suspendExecution) {
//             suspendExecution.printStackTrace();
            return false;
        }
    }

    static boolean isOpen(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            socket.connect(isa, timeout);
            return socket.isConnected();
        } catch (IOException e) {
            // ignore
//            e.printStackTrace();
            return false;
        }
    }
}
