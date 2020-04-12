package cn.tedu.socket;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * 通讯的客户端类
 */
public class Client {
    private Socket socket;

    public Client() {
        /**
         * Socket在实例化的过程中需要传入两个参数:
         * 1.服务器的地址信息,也就是IP地址
         * 2.服务端应用程序绑定的端口号
         * 注意:Socket实例化的过程就是连接服务端的过程,成功连接则实例化成功;
         * 连接失败,实例化过程就会抛出异常.
         * 我们通过IP地址找到网络上的服务端所在的计算机,通过端口找到该机器上的服务端应用程序
         */
        try {
            System.out.println("\t正在连接服务端......");
            socket = new Socket("localhost", 8088);
            System.out.println("\t已经成功连接服务端......");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(){
        try {
            // 客户端发送消息需要先启动一个线程
            ServerHandler handler = new ServerHandler();
            Thread t = new Thread(handler);
            t.start();
            // 要发送消息需要先获取字符输出流
            OutputStream out = socket.getOutputStream();
            OutputStreamWriter  osw = new OutputStreamWriter(out);
            BufferedWriter bw = new BufferedWriter(osw);
            PrintWriter pw = new PrintWriter(bw, true);
            // 有了字符输出流,用户就可以输入消息并发送消息给好友了
            Scanner scanner = new Scanner(System.in);
            // 循环发送消息给客户端用户
            while(true){
                // 在循环内提示客户端输入聊天信息
                //接收用户输入的字符串
                //通过高级字符输出流写出输入信息
                System.out.println("请输入...");
                String line = scanner.nextLine();
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    class ServerHandler implements Runnable{
        @Override
        public void run() {
            try {
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader  br = new BufferedReader(isr);
                String line = null;
                while((line = br.readLine()) != null){
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
