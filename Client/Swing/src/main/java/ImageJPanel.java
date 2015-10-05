
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Špeciálny JPanel s obrázkom v pozadí, ktorý
 * sa používa ako splash screen pre úvodné okno klienta.
 * 
 * @author rbalasko
 */
public class ImageJPanel extends JPanel {
    
    public ImageJPanel() {
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        BufferedImage backgroundSplash = null;
        try {
            backgroundSplash = ImageIO.read(getClass().getResourceAsStream("images/main-splash.png"));
        } catch (IOException ex) {
            Logger.getLogger(ImageJPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        g.drawImage(backgroundSplash, 0, 0, this);
    }
    
}
