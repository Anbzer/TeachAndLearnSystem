package teachServer;
import org.jim2mov.core.DefaultMovieInfoProvider;
import org.jim2mov.core.ImageProvider;
import org.jim2mov.core.Jim2Mov;
import org.jim2mov.core.MovieInfoProvider;
import org.jim2mov.utils.MovieUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class Movie {

    public static void main(String[] args) throws Exception{       
        final File[] jpgs = new File("D:\\network\\video\\").listFiles();
        System.out.println("共有" + jpgs.length + "张待合成图片");
        System.out.println("======检测完毕，开始合成视频======");
        //对文件名进行排序(文件名中的数字越小,生成视频的帧数越靠前)
        Arrays.sort(jpgs,new Comparator<File>(){
            @Override
            public int compare(File file1, File file2) {
                String numberName1 = file1.getName().replace(".jpeg", "");
                String numberName2 = file2.getName().replace(".jpeg", "");
                return Integer.valueOf(numberName1) - Integer.valueOf(numberName2);
            }
        });

        DefaultMovieInfoProvider provider = new DefaultMovieInfoProvider("teach.avi");
        provider.setFPS(1);
        provider.setNumberOfFrames(jpgs.length);
        provider.setMWidth(100);
        provider.setMHeight(200);
        new Jim2Mov(new ImageProvider() {
            @Override
            public byte[] getImage(int i) {
                try {
                    return MovieUtils.convertImageToJPEG((jpgs[i]), 1.0f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        },provider,null).saveMovie(MovieInfoProvider.TYPE_AVI_MJPEG);
        
        System.out.println("======合成成功======");

    }



}
