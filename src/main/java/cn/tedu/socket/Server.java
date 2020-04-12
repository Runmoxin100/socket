package cn.tedu.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * 通讯的服务端类
 */
public class Server {
    /*
    在服务端需要下面的两个属性:
    定义的serverSocket使用来获取服务端
    定义的数据是用来保存所有客户端发送过来的消息.
     */
    private ServerSocket serverSocket;
    private PrintWriter[] allOut = {};

    public Server() {
        // 在无参数构造方法中来启动一个服务端
        try {
            System.out.println("\t开启服务端......");
            serverSocket = new ServerSocket(8088);
            System.out.println("\t服务端成功开启......");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        /*
        accept()是在ServerSocket类中定义的一个阻塞方法,服务器对象调用该方法会返回一个socket实例
        客户端就是通过这个端口和服务端进行连接的,如果这个端口被系统其他程序占用则会抛出异常.
         */
        try {
            while (true) {
                System.out.println("\t等待客户端的连接......");
                Socket socket = serverSocket.accept();
                System.out.println("\t一个客户端已经成功连接......");
                // 用于交互的socket有了以后,就需要启动一个线程来完成交互
                ClientHandler handler = new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    class ClientHandler implements Runnable {
        // 在内部类中需要用到的属性
        // 首先需要一个socket,这是用于与服务端交互必须的客户端.
        // 还需要定义一个host,本机地址信息用来区分群聊中的具体的哪个客户端发送的消息
        private Socket socket;
        private String host;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            // 通过socket来获取客户端的地址,并获取地址的名字name
            host = socket.getInetAddress().getHostName();
        }

        @Override// 重写接口中的run方法
        public void run() {
            PrintWriter pw = null;
            // 在这个线程中我们要实现的功能有哪些?

            try {
                //1. 首先服务端应该需要读取客户端发送过来的消息
                InputStream inputStream = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(isr);
                //2. 服务器端还可以发送消息给客户端
                OutputStream outputStream = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(outputStream);
                BufferedWriter bw = new BufferedWriter(osw);
                pw = new PrintWriter(bw, true);
                //3. 将获得的输出流存入数组当中共享
                //为了解决并发安全问题,我们需要对这个数组进行上锁,不允许两个客户端同时使用该数组
                synchronized (allOut) {
                    allOut = Arrays.copyOf(allOut, allOut.length + 1);
                    allOut[allOut.length - 1] = pw;
                }
                // 打桩-日志: 显示上线的客户端的IP地址,并显示当前的在线人数.
                System.out.println("\t" + host + "上线了!!!");
                System.out.println("\t当前在线人数:" + allOut.length + "人");

                /*
                在这里使用缓冲字符输入流的readLine()方法来读取客户端发送过来的一行字符串信息时,
                如果客户端断开连接,此时由于客户端的系统不同,在这里的反应也不相同.
                通常windows系统断开连接的时候,readLine()方法会直接抛出异常,
                而linux客户端断开连接,这里会返回null.
                 */
                String line = null;
                while ((line = br.readLine()) != null) {
                    // 客户端说了什么,服务端需要先读取
                    System.out.println(host + "说" + line);
                    // 然后将消息发送给所有的客户端,通过遍历的方式发送给所有的客户端
                    // 同样的需要对数组进行上锁,保证一个客户端发送的消息发送给所有客户端以后
                    // 再去发送其他客户端发送的消息,解决并发安全的问题.
                    synchronized (allOut) {
                        for (int i = 0; i < allOut.length; i++) {
                            allOut[i].println(host + "说" + line);
                        }
                    }
                }
            } catch (IOException e) {
               //这里不要输出显示断开异常的信息
            } finally {
                /*
                处理客户端断开连接后的操作:
                将数组中下线的客户端从不敢数组中删除
                 */
                synchronized (allOut){
                    for (int i =0; i<allOut.length; i++){
                    /*
                    这里的判断是:当数组中的元素刚好等于这个客户端的高级字符输出流pw的时候
                    因为对应该pw的客户端已经下线了,所以需要从数组中清除这个输出流,不在给该客户端发送
                    群消息了,因为这时一个群聊的小程序,如果不删除会出现消息发不出去的问题.
                     */
                        if(allOut[i] == pw){
                        /*
                        从数组中清除下线客户的的方法,先遍历判断,然后将这个pw数组元素方法放到数组的末尾
                        最后对这个数组进行缩容就好
                         */
                        allOut[i] = allOut[allOut.length-1];
                        allOut = Arrays.copyOf(allOut,allOut.length-1);
                        System.out.println("\t"+host+"下线了!!!");
                        System.out.println("\t当前在线人数:"+allOut.length+"人");
                        }
                    }
                }
                // 最后需要关闭客户端
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
