package teachServer;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class Record extends JFrame implements ActionListener, Runnable {

    private static File[] jpgs;
    private String fileName; //文件的前缀
    private static int serialNum=0;
    private String imageFormat; //图像文件的格式
    private Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    private static String showname = null;
    private static int shownum=1;
    private Thread ko_Thread;
    private int flag;
    private JButton jb1 = new JButton("开始"),jb2= new JButton("停止");
    private JPanel jp1= new JPanel();
    private JLabel label= new JLabel();

    private void start(){
        if(ko_Thread==null){
            ko_Thread=new Thread(this);
            ko_Thread.start();
        }
    }

    private void stop(){
        if(ko_Thread!=null){
            ko_Thread = null;
        }
    }

    public Record(String s,String format) {
        fileName = s;
        imageFormat=format;
        this.setSize(300,100);
        this.setResizable(false);//窗体不能自由改变大小
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(jp1,BorderLayout.SOUTH);
        this.add(label,BorderLayout.CENTER);
        this.setVisible(true);
        jp1.setLayout(new GridLayout(1,4));
        jp1.add(jb1);
        jp1.add(jb2);
        jb1.addActionListener(this);
        jb2.addActionListener(this);
        ImageIcon icon = new ImageIcon();
        label.setIcon(icon);
        jp1.setVisible(true);
        label.setVisible(true);
    }

    private void snapShot() {
        try {
            //拷贝屏幕到一个BufferedImage对象screenshot
            BufferedImage screenshot = (new Robot()).createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
            serialNum++;
              //根据文件前缀变量和文件格式变量，自动生成文件名
            String name=fileName+String.valueOf(serialNum)+"."+imageFormat;
            File f = new File(name);
            //将screenshot对象写入图像文件
            ImageIO.write(screenshot, imageFormat, f);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object source=e.getSource();
        if(source==jb1){
            flag=0;//录制标记
            start();
        }
        if(source==jb2){
            stop();
        }
    }

    @Override
    public void run() {
        Thread thisThread=Thread.currentThread();
        while(ko_Thread==thisThread){
            if(flag==0)
                snapShot();
            if(flag==1){
                showname=shownum+".jpeg";
                if(shownum<serialNum){
                    shownum++;
                    if(!showname.equals("")){
                        try {
                            Movie.main(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    break;
                }

            }
            try{
                Thread.sleep(400);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        new Record("D:\\network\\video\\", "jpeg");
        jpgs = new File("D:\\network\\video\\").listFiles();
        serialNum = jpgs.length;
    }

}
