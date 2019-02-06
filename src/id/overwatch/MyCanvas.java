package id.overwatch;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;

public class MyCanvas {

    private JLabel lbl;

    public MyCanvas(int rows, int cols) {
        var frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(cols + 50, rows + 50);
        lbl = new JLabel();
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    void update(Mat m) {
        var img = new ImageIcon(toImage(m));
        lbl.setIcon(img);
    }

    private static BufferedImage toImage(Mat m) {
        byte [] b = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, b);
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), BufferedImage.TYPE_3BYTE_BGR);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);  
        return image;
    }

}
