package cn.tedu.socket01;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    //通讯客户端中的成员属性
    private Socket socket;

    /**
     * 在构造器中实例化Socket,传入参数地址和
     */
    public Client() {
        try {
            System.out.println("-------------------------------正在连接服务端");
            /**
             * Socket在实例化的过程中需要传入两个参数:
             * 1.服务器的地址信息,也就是IP地址
             * 2.服务端应用程序绑定的端口号
             * 注意:Socket实例化的过程就是连接服务端的过程,成功连接则实例化成功;
             * 连接失败,实例化过程就会抛出异常.
             * 我们通过IP地址找到网络上的服务端所在的计算机,通过端口找到该机器上的服务端应用程序
             */
            socket = new Socket("localhost",8088);
            System.out.println("-------------------------------连接服务端完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void start(){
        try {
            //先启动一个线程用于读取服务端发送过来的消息
            ServerHandler handler = new ServerHandler();
            Thread t = new Thread(handler);
            t.start();
            //发送消息给服务端
            OutputStream out = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(out);
            BufferedWriter bw = new BufferedWriter(osw);
            PrintWriter pw = new PrintWriter(bw,true);
            Scanner sc = new Scanner(System.in);
            //循环发送消息给服务端用户
            while(true){
                //在循环内提示客户端输入聊天信息
                System.out.println("请输入......");
                //接收用户输入的字符串
                String line = sc.nextLine();
                //通过高级字符输出流写出输入信息
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
    /**
     * 下面的这个类是一个实现类,用于实现Runnable接口
     * 为创建线程做准备,通过实现类的方式创建一个线程.
     * 需要重写接口的run方法来实现具体的功能或者行为.
     */
    class ServerHandler implements Runnable{
        /**
         * 我们在run方法中实现读取服务端发送的消息的功能
         */
        @Override
        public void run() {
            try {
                InputStream in = socket.getInputStream();//通过socket获取字节输入流
                InputStreamReader isr  = new InputStreamReader(in);//连接字符输入流
                BufferedReader br = new BufferedReader(isr);//连接字符缓冲流
                String line = null;
                while((line = br.readLine())!=null){
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
