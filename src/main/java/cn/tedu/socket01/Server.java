package cn.tedu.socket01;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {
    //定义成员属性
    private ServerSocket server ;
    //下面的数组是为了存放客户端写入的信息
    private PrintWriter[] allOut = {};

    public Server() {
        try {
            //在这里启动服务端
            System.out.println("--------------------------------开始启动服务端");
            server = new ServerSocket(8088);
            System.out.println("--------------------------------服务端启动完毕");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void start(){
        try {
            /**
             * accept()是在ServerSocket类中定义的一个阻塞方法,服务器对象调用该方法会返回一个socket实例
             * 客户端就是通过这个端口和服务端进行连接的,如果这个端口被系统其他程序占用则会抛出异常.
             */
            while(true){
                System.out.println("-----------------------------等待客户端连接");
                Socket socket = server.accept();
                System.out.println("-------------------------一个客户端已经连接");
                //启动一个线程来处理交互
                ClientHandler handler = new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
    /**
     * 在类内部创建内部类,并实现Runnable接口
     * 当一个类只被另一个类单独使用的情况下就可以将这个类定义为内部类,不需要再另外创建.
     */
    class ClientHandler implements  Runnable{
        private Socket socket;
        //host是为了记录客户端的地址信息
        private String host;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            //通过socket获取客户端的地址,并获取地址的名字
            host = socket.getInetAddress().getHostName();
        }

        public void run() {
            PrintWriter pw =null;
            try {
                //Step1:先获取输入流来读取客户端发送的消息
                InputStream in = socket.getInputStream();//字节输入流
                InputStreamReader isr = new InputStreamReader(in);//字符输入流
                BufferedReader br = new BufferedReader(isr);//字符缓冲流
                //Step2:获取输出流,用来给客户端回复消息
                OutputStream out = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out);
                BufferedWriter bw = new BufferedWriter(osw);
                pw = new PrintWriter(bw,true);//回车自动发送消息
                /**
                 * 将获得的输出流存入数组当中共享
                 * 为了解决并发安全问题,我们需要对这个数组进行上锁,不允许两个客户端同时使用该数组
                 */
                synchronized (allOut){
                    allOut = Arrays.copyOf(allOut,allOut.length+1);
                    allOut[allOut.length-1] = pw;
                }
                System.out.println("------------------------------------"+host+"上线了!!!");
                System.out.println("---------------------------当前在线人数:"+allOut.length+"人");
                /**
                 * 在这里使用缓冲字符输入流的readLine()方法来读取客户端发送过来的一行字符串信息时,
                 * 如果客户端断开连接,此时由于客户端的系统不同,在这里的反应也不相同.
                 * 通常windows系统断开连接的时候,readLine()方法会直接抛出异常,
                 * 而linux客户端断开连接,这里会返回null.
                 */
                String line = null;
                while((line = br.readLine())!=null){
                    System.out.println(host+"说:"+line);
                    //将消息发送给所有客户端
                    synchronized (allOut){
                        for(int i=0;i<allOut.length;i++){
                            allOut[i].println(host+"说:"+line);
                        }
                    }
                }
            } catch (Exception e) {

            }finally {
                /*
                处理客户端断开连接后的操作:
                将数组中下线的客户端从不敢数组中删除
                 */
                synchronized (allOut){
                    for(int i=0;i<allOut.length;i++){
                    /*
                    这里的判断是:当数组中的元素刚好等于这个客户端的高级字符输出流pw的时候
                    因为对应该pw的客户端已经下线了,所以需要从数组中清除这个输出流,不在给该客户端发送
                    群消息了,因为这时一个群聊的小程序,如果不删除会出现消息发不出去的问题.
                     */
                        if(allOut[i]==pw){
                        /*
                        从数组中清除下线客户的的方法,先遍历判断,然后将这个pw数组元素方法放到数组的末尾
                        最后对这个数组进行缩容就好
                         */
                            allOut[i] = allOut[allOut.length-1];
                            allOut = Arrays.copyOf(allOut,allOut.length-1);
                            System.out.println("-------------------------------"+host+"下线了!!!");
                            System.out.println("-----------------当前在线人数:"+allOut.length+"人");
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

