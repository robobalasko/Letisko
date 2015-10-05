
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import net.robobalasko.letiskoserv.data.AirportServerService;
import net.robobalasko.letiskoserv.navigacia.InvalidRouteDataException;
import net.robobalasko.letiskoserv.navigacia.InvalidRunwayDataException;
import net.robobalasko.letiskoserv.navigacia.InvalidWaypointDataException;

/**
 * Hlavná trieda letiskového serveru, ktorá nastaví logovanie chýb,
 * načíta z classpath priečinok, v ktorom sú .xml súbory s letiskami
 * a naštartuje letisková sever.
 * 
 * @author rbalasko
 */
public class Main {

    public static void main(String[] args)
            throws InvalidRunwayDataException,
            InvalidRouteDataException,
            InvalidWaypointDataException,
            IOException,
            URISyntaxException {
        // Nastavenie logovania
        Handler handler = new FileHandler("sever-main.log", 51200, 1);
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).addHandler(handler);
        // Načítanie jazykovej mutácie
        Locale locale = Locale.getDefault();
        ResourceBundle language = ResourceBundle.getBundle("languages/language", locale);
        // Načítanie priečinka z letiskami, štart servera
        File xmlDir = new File(ClassLoader.getSystemResource("xml").toURI());
        AirportServerService server = new AirportServerService(xmlDir, language);
    }

}
