package imgcompressor;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.awt.*;

import imgcompressor.Image.Img;
import imgcompressor.algorithms.FastDct8;
import imgcompressor.algorithms.SlowDct;
import imgcompressor.compressor.Compressor;

public class Main extends JFrame {

    private JButton buttonOpen;
    private JButton buttonCompress;
    private JButton buttonSave;
    private JLabel labelImage;
    private JLabel sizeKb;
    private JSlider slider;
    private JTextField sliderVal;
    private Img a;
    private Compressor b;

    private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    public Main() {
        super();
        this.setTitle("Image Compressor");
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        labelImage = new JLabel();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        c.gridheight = 3;
        c.gridx = 0;
        c.gridy = 0;
        this.add(labelImage, c);

        buttonOpen = new JButton("Open file");
        //button1.setBounds(200, 100, 100, 50);
        c.weightx = 0.5;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        buttonOpen.addActionListener(e -> {
            try {
                a = null;
                b = null;
                System.gc();
                a = new Img(this);
                labelImage.setIcon(new ImageIcon(a.image.getScaledInstance(-1, (int)dim.getHeight()/2, BufferedImage.SCALE_SMOOTH)));
                buttonCompress.setEnabled(true);
                buttonSave.setEnabled(true);
                sizeKb.setText("<html>Raw 8-bit size: "+String.format("%.2f",a.getSrcSizeKb())+" Kb"+"<br>Actual file size: "+String.format("%.2f",a.getSrcFileSizeKb())+" Kb"+"</html>");
                this.pack();
                this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        this.add(buttonOpen, c);
        
        buttonCompress = new JButton("Compress");
        buttonCompress.setEnabled(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 1;
        buttonCompress.addActionListener(e -> {
            b = new Compressor(a.getRGBArray(), (short) a.image.getHeight(), (short) a.image.getWidth());
            b.compress(slider.getValue(), 64);
            a.setRGBArray(b.getsRGBArray());
            sizeKb.setText("<html>Raw 8-bit size: " + String.format("%.2f", a.getSrcSizeKb()) + " Kb"
                    + "<br>Actual file size: " + String.format("%.2f", a.getSrcFileSizeKb()) + " Kb"
                    + "<br>Compressed File size: " + String.format("%.2f", b.getEncodeSizeKb()) + " Kb"
                    +"<br>Compression Ration: 1:"+String.format("%.2f",(a.getSrcSizeKb()/b.getEncodeSizeKb()))+ "</html>");
            this.pack();
        });
        this.add(buttonCompress, c);

        buttonSave = new JButton("Write to...");
        buttonSave.setEnabled(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        buttonSave.addActionListener(e -> {
            try {
                a.writeImg();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        this.add(buttonSave, c);
        
        sizeKb = new JLabel();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        this.add(sizeKb, c);
        //c.anchor = GridBagConstraints.NORTH;

        slider = new JSlider(JSlider.HORIZONTAL, 1, 100, 50);
        slider.setBorder(BorderFactory.createTitledBorder("Chose quality factor"));
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 4;
        slider.addChangeListener(e -> sliderVal.setText(String.valueOf(slider.getValue())));
        this.add(slider, c);

        sliderVal = new JTextField();
        sliderVal.setText("50");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.gridx = 1;
        c.gridy = 4;
        sliderVal.addActionListener(e -> {
            if (Integer.parseInt(sliderVal.getText()) > 100) sliderVal.setText("100");
            if (Integer.parseInt(sliderVal.getText()) < 1) sliderVal.setText("1");
            slider.setValue(Integer.parseInt(sliderVal.getText()));
        });
        this.add(sliderVal, c);
        

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(200, 100));
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);

        this.buttonOpen = new JButton("1");
    }

    public static void main(String[] args) {
        double[] test = {1,2,3,4};
        System.out.println(Arrays.toString(SlowDct.transform(test)));
        System.out.println(Arrays.toString(SlowDct.test(test)));
        //System.out.println(Arrays.toString(FastDct8.transform(test)));
        //System.out.println(Arrays.toString(FastDct8.transform(test)));
         try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main();
            }
        });
    }
}
