/*
 * Copyright 2022 overwatch project
 * 
 * Website: https://github.com/lambdaprime/overwatch
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package id.overwatch;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.CvType;
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
        Map<Integer, Integer> map =
                Map.of(
                        CvType.CV_8UC3, BufferedImage.TYPE_3BYTE_BGR,
                        CvType.CV_8UC4, BufferedImage.TYPE_4BYTE_ABGR);
        if (m.type() == CvType.CV_8UC4) m = makeABGR(m);
        Integer type = map.get(m.type());
        if (type == null) throw new RuntimeException("Invalid image type");
        byte[] b = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, b);
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    private static Mat makeABGR(Mat m) {
        var mv = new LinkedList<Mat>();
        Core.split(m, mv);
        mv.addFirst(mv.removeLast());
        var r = new Mat();
        Core.merge(mv, r);
        return r;
    }
}
