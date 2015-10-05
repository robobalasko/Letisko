


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.JLabel;
import javax.swing.Timer;

public class ClockJLabel extends JLabel implements ActionListener {
    
    SimpleDateFormat dateFormat;
    
    public ClockJLabel() {
        super();
        dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        setText(dateFormat.format(new Date()));
        Timer timer = new Timer(1000, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setText(dateFormat.format(new Date()));
    }

}
