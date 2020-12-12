package teachServer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server extends JFrame {

    private byte[] img = null;

    private boolean sendFile = false;
    ServerSocket server = null;

    public Server(){
        try {
            ServerSocket ss = new ServerSocket(8821);//绑定端口
            server = new ServerSocket(8080);
            new Thread(new Runnable() {//创建子线程
                @Override
                public void run() {
                    try {
                        System.out.println("等待连接1……");
                        while(true){
                            Socket s = ss.accept();//多线程连接
                            System.out.println("成功建立socket连接[端口号：" + ss.getLocalPort() + "]");
                            Transfer transfer = new Transfer(s);
                            transfer.start();
                        }

                    } catch (Exception e) {
                    	System.out.println("连接失败1");                    	
                        e.printStackTrace(); 
                    }finally{
                        try {
                            if(ss!=null){
                                ss.close();
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }

                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket socket = null;
                    try {
                    	System.out.println("等待连接2……");
                        while(true){
                            socket = server.accept();
                            System.out.println("成功建立socket连接[端口号：" + server.getLocalPort() + "]");
                            if (!sendFile){
                                new SendFile(socket).start();
                                sendFile = true;
                            }
                        }
                    } catch (IOException e) {
                    	System.out.println("连接失败2"); 
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    class SendFile extends Thread{

        Socket s;

        private SendFile(Socket socket){
            this.s = socket;
        }

        @Override
        public void run() {
            try {
                while (true){
//                	D:\network\tv\screen.gif
                    System.out.println("输入要发送的文件的绝对地址：");
                    Scanner scanner = new Scanner(System.in);//接收从键盘输入的数据
                    String filePath = scanner.next();	//文件路径
//                    String filePath = "D:\\network\\tv\\screen.gif";
                    File fi = new File(filePath);
//                    System.out.println(fi);
                    System.out.println("====== 开始发送 ======");
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
    				dis.readByte();
    				DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
    				DataOutputStream ps = new DataOutputStream(s.getOutputStream());
    				ps.writeUTF(fi.getName());
    				ps.flush();
    				ps.writeLong((long) fi.length());
    				ps.flush();
    				int bufferSize = 8192;
    				byte[] buf = new byte[bufferSize];
    				while (true) {
    					int read = 0;
    					if (fis != null) {
    					read = fis.read(buf);
    					}
    					if (read == -1) {
    					break;
    					}
    					ps.write(buf, 0, read);
    				}
    				ps.flush();
    				// 需关闭socket链接，不然客户端会等待server的数据过来
    				// 直到socket超时，导致数据不完整。
    				fis.close();
    				s.close();
    				System.out.println("====== 发送成功 ======");
                    
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    sendFile = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }

    static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    class Transfer extends Thread {
        Socket s;
        Robot robot;//测试自动化，自运行演示程序
        ObjectOutputStream oos;//对象的输出流，在另一台主机或进程上重构对象

        public Transfer(Socket s) {
            this.s = s;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
                sendFile = false;
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(20);
                    cutScreen();
                    oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(img);//将对象写入
                    oos.flush();//清空
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendFile = false;
            } finally {
                try {
                    if (s != null) {
                        s.close();
                        oos.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    sendFile = false;
                }
            }
        }

        void cutScreen() {
            try {
                BufferedImage buf = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();//创建字节输出流
                ImageIO.write((RenderedImage) buf, "png", baos);
                img = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    
    public static void main(String[] args) {
        new Server();
    }


}
