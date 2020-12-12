package learnClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {

    private JPanel panel;
    private static String ip;
    private byte[] img = null;
    DataOutputStream out = null;
    DataInputStream getMessageStream = null;
    private String sendMessage = "Windwos";

    public Client(){

        try {
            Socket socket = new Socket(ip, 8821);
            ClientSocket cs = new ClientSocket("127.0.0.1",8080);
            try {
    			cs.CreateConnection();
//    			System.out.print("连接服务器成功!" + "\n");
    			
    		} catch (Exception e) {
    			System.out.print("连接服务器失败，无法传输文件!" + "\n");
    			e.printStackTrace();
    		}
            new AcceptThread(socket,this).start();
            System.out.println("连接服务器成功 [端口号：" + socket.getPort() + "]");
            new FileThread(cs).start();
            System.out.println("连接服务器成功 [端口号：" + cs.getPort() + "]");
            panel = new JPanel();
            this.add(panel);
            this.setSize(1000, 800);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setLocationRelativeTo(null);
            this.setVisible(true);
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount()==2) {
                        Client.this.dispose();
                        if(Client.this.getExtendedState()==JFrame.MAXIMIZED_BOTH){
                            Client.this.setUndecorated(false);
                            Client.this.setSize(1000, 800);
                            Client.this.setExtendedState(JFrame.NORMAL);
                        }else {
                            Client.this.setExtendedState(JFrame.MAXIMIZED_BOTH);
                            Client.this.setUndecorated(true);
                        }
                        Client.this.setLocationRelativeTo(null);
                        Client.this.panel.repaint();
                        Client.this.setVisible(true);
                    }
                }
            });
//            getFile(socket1);
        }catch (IOException e) {
            e.printStackTrace();
        }



    }

    void startFlush(){
        Image image = getImg();
        Graphics g= panel.getGraphics();//创建图形上下文
        g.drawImage(image, 0, 0, this.getWidth()-2*this.getInsets().left, this.getHeight()-this.getInsets().top-this.getInsets().bottom, null);
    }

    Image getImg() {
        Image img = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(this.img);
            img = ImageIO.read(bais);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return img;
    }

    class AcceptThread extends Thread {
        Socket s;
        ObjectInputStream ois;
        Client Client;
        AcceptThread(Socket s,Client Client){
            this.s = s;
            this.Client = Client;
        }

        @Override
        public void run() {
            try {
                while(true){
                    Thread.sleep(10);
                    ois = new ObjectInputStream(s.getInputStream());
                    img = (byte[]) ois.readObject();
                    Client.startFlush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                try {
                    if(s!=null){
                        s.close();
                        ois.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
        }
    }
    
    
    
    class FileThread extends Thread{

        private ClientSocket cs;
        DataInputStream inputStream = null;

        public FileThread(ClientSocket cs){
            this.cs = cs;
//            System.out.println(cs.getPort());
            if (cs == null)
            	System.out.println("cs为空");
        }                       
        
        @Override
        public void run() {
            try {
//            	System.out.println("file?");
            	try {
        			cs.sendMessage(sendMessage);
        		} catch (Exception e) {
        			System.out.print("发送消息失败!" + "\n");
        		}
            	
            	try {
//            		System.out.println("inputStream?");
        			inputStream = cs.getMessageStream();
//        			System.out.println("读取消息: " + inputStream.readUTF());
        		} catch (Exception e) {
        			System.out.print("接收消息缓存错误\n");
        			e.printStackTrace();        			
        		}
            	if(inputStream != null){
            		//本地保存路径，文件名会自动从服务器端继承而来。
//            		System.out.println("开始保存了吗");
        			String savePath = "D:\\network\\";
        			int bufferSize = 8192;
        			byte[] buf = new byte[bufferSize];
        			int passedlen = 0;
        			long len=0;
//        			System.out.println("什么文件？" + inputStream.readUTF());
        			savePath += inputStream.readUTF();
//        			System.out.println("保存在哪呢？" + savePath);
        			DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
        			len = inputStream.readLong();
        			System.out.println("====== 开始接收文件 ======");
        			
        			while (true) {
        				int read = 0;
        				
        				if (inputStream != null) {
        					read = inputStream.read(buf);
        				}
        				
        				passedlen += read;
        				
        				if (read == -1) {
        					break;
        				}
        				System.out.print("| " +  (passedlen * 100/ len) + "% |");
        				fileOut.write(buf, 0, read);
        			}			
        			System.out.println();
//        			System.out.println("111");
        			System.out.println("====== 接收成功，长度为 [" + len + "字节] " + "文件存为 [" + savePath + "] ======");
        			fileOut.close();
            	}            	

                } catch (Exception e) {
                	System.out.println("接收发生错误" + "\n");
                	return;
                }

        }
    }

    public static void main(String[] args) {
        ip = JOptionPane.showInputDialog("ip", "localhost");
        new Client();
    }

}