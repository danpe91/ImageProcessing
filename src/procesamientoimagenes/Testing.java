package procesamientoimagenes;

import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.Thread.UncaughtExceptionHandler;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPicker;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

// Test code and library got from:
// https://github.com/sarxos/webcam-capture
public class Testing {

    public boolean test() {

        try {
            Webcam webcam = Webcam.getDefault();
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            // creates test1.bmp
            WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_BMP);
            // creates test1.gif
            WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_GIF);
            ByteBuffer buffer = WebcamUtils.getImageByteBuffer(webcam, "jpg");
            System.out.println("Buffer length: " + buffer.capacity());

            webcam.open();
            ImageIO.write(webcam.getImage(), "jpg", new File("hello-world.jpg"));
            webcam.close();

        } catch (WebcamException | IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean testPanelBasic() {

        try {
            Webcam webcam = Webcam.getDefault();
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            WebcamPanel panel = new WebcamPanel(webcam);
            panel.setFPSDisplayed(true);
            panel.setDisplayDebugInfo(true);
            panel.setImageSizeDisplayed(true);
            panel.setMirrored(true);
            JFrame window = new JFrame("Test webcam panel");
            window.add(panel);
            window.setResizable(true);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.pack();
            window.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean testPanelAdvanced() {
        
        SwingUtilities.invokeLater(new WebcamViewerExample());
        return true;
    }
}

class WebcamViewerExample extends JFrame implements Runnable, WebcamListener, WindowListener, UncaughtExceptionHandler, ItemListener, WebcamDiscoveryListener, MouseListener {

    private static final long serialVersionUID = 1L;
    private Webcam webcam = null;
    private WebcamPanel panel = null;
    
    private WebcamPicker picker = null;

    @Override
    public void run() {
        Webcam.addDiscoveryListener(this);
        setTitle("Java Webcam Capture POC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        addWindowListener(this);
        picker = new WebcamPicker();
        picker.addItemListener(this);
        webcam = picker.getSelectedWebcam();
        if (webcam == null) {
            System.out.println("No webcams found...");
            System.exit(1);
        }
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.addWebcamListener(WebcamViewerExample.this);
        panel = new WebcamPanel(webcam, false);
        panel.setFPSDisplayed(true);
        panel.addMouseListener(this);
        add(picker, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        pack();
        setVisible(true);
        Thread t = new Thread() {
            @Override
            public void run() {
                panel.start();
            }
        };
        t.setName("example-starter");
        t.setDaemon(true);
        t.setUncaughtExceptionHandler(this);
        t.start();
    }

    @Override
    public void webcamOpen(WebcamEvent we) {
        System.out.println("webcam open");
    }

    @Override
    public void webcamClosed(WebcamEvent we) {
        System.out.println("webcam closed");
    }

    @Override
    public void webcamDisposed(WebcamEvent we) {
        System.out.println("webcam disposed");
    }

    @Override
    public void webcamImageObtained(WebcamEvent we) {
        //System.out.println("webcam viewer: Image Obtained");
    }

    @Override
    public void windowActivated(WindowEvent e) {
        System.out.println("webcam viewer activated");
        panel.resume();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        System.out.println("webcam viewer deacivated");
        panel.pause();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        webcam.close();
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
        System.out.println("webcam viewer opened");
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        System.out.println("webcam viewer resumed");
        panel.resume();
    }

    @Override
    public void windowIconified(WindowEvent e) {
        System.out.println("webcam viewer paused");
        panel.pause();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println(String.format("Exception in thread %s", t.getName()));
        e.printStackTrace();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getItem() != webcam) {
            if (webcam != null) {
                panel.stop();
                remove(panel);
                webcam.removeWebcamListener(this);
                webcam.close();
                webcam = (Webcam) e.getItem();
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcam.addWebcamListener(this);
                System.out.println("selected " + webcam.getName());
                panel = new WebcamPanel(webcam, false);
                panel.setFPSDisplayed(true);
                add(panel, BorderLayout.CENTER);
                pack();
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        panel.start();
                    }
                };
                t.setName("example-stoper");
                t.setDaemon(true);
                t.setUncaughtExceptionHandler(this);
                t.start();
            }
        }
    }

    @Override
    public void webcamFound(WebcamDiscoveryEvent event) {
        if (picker != null) {
            picker.addItem(event.getWebcam());
        }
    }

    @Override
    public void webcamGone(WebcamDiscoveryEvent event) {
        if (picker != null) {
            picker.removeItem(event.getWebcam());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("mouse clicked");
        WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_JPG);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("mouse pressed");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        System.out.println("mouse released");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        System.out.println("mouse entered");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        System.out.println("mouse exited");
    }
}
