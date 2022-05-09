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
import java.awt.*;

public class Main extends JFrame {

    private JButton buttonOpen;
    private JButton buttonCompress;
    private JButton buttonSave;
    private JLabel labelImage;
    private JLabel stats;
    private JSlider slider;
    private JTextField sliderVal;
    private JTextField coefVal;
    private Compressor compressor;

    private Dimension ScreenDim = Toolkit.getDefaultToolkit().getScreenSize();

    public Main() {
        super();
        this.setTitle("Image Compressor");
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        //JLabel used to show image
        labelImage = new JLabel();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        c.gridheight = 4;
        c.gridx = 0;
        c.gridy = 0;
        this.add(labelImage, c);

        //JButton used for file opening
        buttonOpen = new JButton("Open file");
        c.weightx = 0.5;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        buttonOpen.addActionListener(e -> {
            compressor = new Compressor();

            //Hardcoded mcu size of 8
            compressor.openFile();
            labelImage.setIcon(new ImageIcon(
                    compressor.getResultImage().getScaledInstance(-1, (int) ScreenDim.getHeight() / 2, BufferedImage.SCALE_SMOOTH)));
            labelImage.repaint();
            buttonCompress.setEnabled(true);
            buttonSave.setEnabled(true);
            stats.setText("<html>Raw 8-bit size: " + String.format("%.2f", compressor.getSrcSizeKb()) + " Kb"
                    + "<br>Actual file size: " + String.format("%.2f", compressor.getSrcFileSizeKb()) + " Kb" + "</html>");
            this.pack();
            this.setLocation(ScreenDim.width / 2 - this.getSize().width / 2,
                    ScreenDim.height / 2 - this.getSize().height / 2);
            compressor.printStatsCoef();
        });

        this.add(buttonOpen, c);

        //JButton for compression
        buttonCompress = new JButton("Compress to..");
        buttonCompress.setEnabled(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 1;
        buttonCompress.addActionListener(e -> {
            compressor.compressImage(slider.getValue(), Integer.parseInt(coefVal.getText()));
            compressor.decompressImage();

            stats.setText("<html>Raw 8-bit size: " + String.format("%.2f", compressor.getSrcSizeKb()) + " Kb"
                    + "<br>Actual file size: " + String.format("%.2f", compressor.getSrcFileSizeKb()) + " Kb"
                    + "<br>Compressed file size: " + String.format("%.2f", compressor.getEncodeSizeKb()) + " Kb"
                    + "<br>Compression Ration: 1:" + String.format("%.2f", compressor.getCompresionRatioKB())
                    + "<br>Mean sqared error: " + String.format("%.2f", compressor.getMSE())
                    + "<br>PSNR: " + String.format("%.2f", compressor.getPSNR()) + " dB"
                    + "</html>");

            labelImage.setIcon(new ImageIcon(
                    compressor.getResultImage().getScaledInstance(-1, (int) ScreenDim.getHeight() / 2, BufferedImage.SCALE_SMOOTH)));
            labelImage.repaint();
            this.pack();
        });
        this.add(buttonCompress, c);

        //JButton for writing to file
        buttonSave = new JButton("Write to..");
        buttonSave.setEnabled(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        buttonSave.addActionListener(e -> {
        compressor.writeImage();
        });
        this.add(buttonSave, c);

        //JLabel for stats
        stats = new JLabel();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        this.add(stats, c);

        //JSlider for quality factor
        slider = new JSlider(JSlider.HORIZONTAL, 1, 100, 50);
        slider.setBorder(BorderFactory.createTitledBorder("Chose quality factor"));
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 5;
        slider.addChangeListener(e -> sliderVal.setText(String.valueOf(slider.getValue())));
        this.add(slider, c);

        //JTextField for DCT coeficients to keep. Hardcoded 1 to 64
        coefVal = new JTextField();
        coefVal.setText("64");
        coefVal.setBorder(BorderFactory.createTitledBorder("DCT coef's:"));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.gridx = 1;
        c.gridy = 4;
        coefVal.addActionListener(e -> {
            if (Integer.parseInt(coefVal.getText()) > 64)
                coefVal.setText("64");
            if (Integer.parseInt(coefVal.getText()) < 1)
                coefVal.setText("1");

        });
        this.add(coefVal, c);

        //JTextField for numeric quality control
        sliderVal = new JTextField();
        sliderVal.setBorder(BorderFactory.createTitledBorder("Quality:"));
        sliderVal.setText("50");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.gridx = 1;
        c.gridy = 5;
        sliderVal.addActionListener(e -> {
            if (Integer.parseInt(sliderVal.getText()) > 100)
                sliderVal.setText("100");
            if (Integer.parseInt(sliderVal.getText()) < 1)
                sliderVal.setText("1");
            slider.setValue(Integer.parseInt(sliderVal.getText()));
        });
        this.add(sliderVal, c);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(200, 100));
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
    }
    

    public static void main(String[] args) {
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
