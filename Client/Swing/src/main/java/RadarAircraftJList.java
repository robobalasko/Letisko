
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.Timer;
import net.robobalasko.letisko.siet.AircraftHandler;
import net.robobalasko.letiskoserv.lietadla.Aircraft;
import net.robobalasko.letiskoserv.navigacia.AirportRouteTypesEnum;

public class RadarAircraftJList extends JList {
    
    private final DefaultListModel aircraftList;
    
    private final AircraftHandler aircraftHandler;
    
    private int selectedIndex;

    public RadarAircraftJList(AircraftHandler aircraftHandler,
            final String AirportIcao,
            final AircraftDataDisplayTypeEnum displayType) {
        aircraftList = new DefaultListModel();
        this.setModel(aircraftList);
        this.aircraftHandler = aircraftHandler;
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedIndex = getSelectedIndex();
                aircraftList.removeAllElements();
                for (Aircraft acft : RadarAircraftJList.this.aircraftHandler.getHandledAircraft()) {
                    switch (displayType) {
                        case DEPARTURE:
                            if (checkIncludeAircraftIntoDeps(acft, AirportIcao)) {
                                aircraftList.addElement(acft);
                            }
                            break;
                        case ARRIVAL:
                            if (checkIncludeAircraftIntoArrs(acft, AirportIcao)) {
                                aircraftList.addElement(acft);
                            }
                            break;
                    }
                }
                setSelectedIndex(selectedIndex);
            }
        });
        timer.start();
    }
    
    private boolean checkIncludeAircraftIntoDeps(Aircraft aircraft,
            String depAirportIcao) {
        return aircraft.getDepAirport().equals(depAirportIcao)
                && aircraft.getActualRoute().getRouteType() == AirportRouteTypesEnum.SID;
    }
    
    private boolean checkIncludeAircraftIntoArrs(Aircraft aircraft,
            String arrAirportIcao) {
        return aircraft.getArrAirport().equals(arrAirportIcao.toUpperCase())
                && aircraft.getActualRoute().getRouteType() == AirportRouteTypesEnum.STAR;
    }
    
}
