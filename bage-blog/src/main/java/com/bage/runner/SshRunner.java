package com.bage.runner;
/*
* 测试 CommandLineRunner 实现项目启动时预处理功能，这段代码能在启动java时自动执行服务器上的bbb.sh脚本，bbb.sh能执行内网穿透指令
* */
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component//因为这个类要交给spring容器管理，所以一定要加这个注解
public class SshRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("执行内网穿透脚本");
        //java调用服务器上的bbb.sh执行内网穿透
        // Define the script path
        String scriptPath = "/data/bbb.sh";

        try {
            // Create a process builder to execute the script
            ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath);

            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Print the exit code
            System.out.println("Script executed with exit code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //直接使用java执行内网穿透，无需脚本 TODO 测试后不好使
//        try {
//            // Define the SSH command
//            String sshCommand = "ssh -CNg -L 6111:127.0.0.1:6111 root@region-42.seetacloud.com -p 16528";
//
//            // Create a process builder to execute the command
//            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", sshCommand);
//
//            // Start the process
//            Process process = processBuilder.start();
//
//            // Pass the password to the SSH command using the input stream
//            String password = "iWvwjee5rf\n"; // Include newline at the end
//            process.getOutputStream().write(password.getBytes());
//            process.getOutputStream().flush();
//
//            // Wait for the process to complete
//            int exitCode = process.waitFor();
//
//            // Print the exit code
//            System.out.println("SSH command executed with exit code: " + exitCode);
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }

    }
}
