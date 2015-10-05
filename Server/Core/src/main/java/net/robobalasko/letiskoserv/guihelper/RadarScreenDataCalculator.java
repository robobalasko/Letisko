package net.robobalasko.letiskoserv.guihelper;

import net.robobalasko.letiskoserv.navigacia.Airport;
import net.robobalasko.letiskoserv.navigacia.Runway;
import net.robobalasko.letiskoserv.navigacia.Waypoint;

/**
 * Trieda, ktorá prepočítava skutočné dáta letísk
 * do súradníc použiteľných pre zobrazovanie na radarove obrazovke.
 * 
 * @author rbalasko
 */
public class RadarScreenDataCalculator {
    
    /**
     * Objekt letiska, ktorý obsahuje skutočné dáta k prepočítaniu.
     */
    private final Airport airportData;
    
    /**
     * Aktuálne horizontálne zväčšenie zobrazovania na radare.
     */
    private final double latScale;
    
    /**
     * Aktuálne vertikálne zväčšenie zobrazovania na radare.
     */
    private final double longScale;
    
    /**
     * Horný prepočítaný okraj riadenej oblasti letiska.
     */
    private final double screenTop;
    
    /**
     * Pravý prepočítaný okraj riadenej oblasti letiska.
     */
    private final double screenRight;
    
    /**
     * Dolný prepočítaný okraj riadenej oblasti letiska.
     */
    private double screenBottom;
    
    /**
     * Ľavý prepočítaný okraj riadenej oblasti letiska.
     */
    private double screenLeft;
    
    /**
     * Horizontálny koeficient použitý pre výpočty pozícií
     * jednotlivých objektov na obrazovke.
     */
    private final double latCoeficient;
    
    /**
     * Vertikálny koeficient použitý pre výpočty pozícií
     * jednotlivých objektov na obrazovke.
     */
    private final double longCoeficient;
    
    /**
     * Aktuálna šírka radarovej obrazovky.
     */
    private int screenWidth;
    
    /**
     * Akutálna výška radarovej obrazovky.
     */
    private int screenHeight;
    
    /**
     * Konštantna pre x-násobné zväčšenie hraničných súradníc letiska.
     */
    private final int AIRPORT_BORDERS_SCALE = 10;
    
    /**
     * Konštanta pre x-násobné zväčšnie koeficientov
     * pre výpočty pozícií bodov na radare.
     */
    private final int COEFICIENTS_SCALE = 100;
    
    /**
     * Konštanta pre x-násobné zväčšenie GPS súradníc
     * pevných bodov na radarovej obrazovke.
     */
    private final int GUI_GPS_SCALE = 10;
    
    /**
     * Korekcia horizontálne výpočítaných údajov pre radarovú obrazovku.
     * Používa sa na horizontálne vystredenie celého vzdušného
     * pristoru letiska do radarovej obrazovky.
     */
    private final int GUI_LAT_CORRECTION = 50;
    
    /**
     * Korekcia vypočítaných údajov pre radarovú obrazovku.
     * Používa sa na vertikálne vystredenie celého vzdušného
     * pristoru letiska do radarovej obrazovky.
     */
    private final int GUI_LONG_CORRECTION = 100;
    
    /**
     * Koeficient pre výpočet dĺžky dráhy na radarovej obrazovke.
     */
    private final double GUI_RWY_LENGTH_COEF = 0.01;
    
    /**
     * Základný konštruktor nastavuje nevyhnutné dáta pre
     * ostatné výpočty vykonávane v tejto triede.
     * 
     * Objekt tejto triedy je vytovrený hneď pri v konštruktore
     * objektu {@code RadarScreen}. Pre správnu funkčnosť je potrebné
     * pri vykresľovaní radarovej obrazovky v metóde paintComponent triedy
     * {@code RadarScreen} nastaviť tomuto objektu aktuálne hodnoty výšky
     * a šírky zobrazovanej na radare.
     * 
     * 
     * @param screenWidth Šírka radarového zobrazenia
     * @param screenHeight Výška radarového zobrazenia
     * 
     * @param airportData Načítané dáta skutočného letiska, ktoré sa prepočítavajú
     */
    public RadarScreenDataCalculator(Airport airportData, int screenWidth, int screenHeight) {
        this.airportData = airportData;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        screenTop = calculateAreaTopBorder();
        screenRight = calculateAreaRightBorder();
        latCoeficient = calculateLatCoeficient();
        longCoeficient = calculateLongCoeficient();
        latScale = 10;
        longScale = 6;
    }
    
    /**
     * Prepočítava horizontálnu pozíciu objektu na radarovej obrazovke.
     * 
     * @param realLatitude Skutočná zemepisná šírka objektu.
     * 
     * @return Vypočítaná hodnota ako reálne číslo.
     */
    public double calculateGUILatitude(double realLatitude) {
        return ((((screenTop - (realLatitude * GUI_GPS_SCALE))
                * screenHeight) / latCoeficient) * latScale) + GUI_LAT_CORRECTION;
    }
    
    /**
     * Prepočítava vertikálnu pozíciu objektu na radarovej obrazovke.
     * 
     * @param realLongitude Skutočná zemepisná výška objektu.
     * 
     * @return Vypočítaná hodnota ako reálne číslo.
     */
    public double calculateGUILongitude(double realLongitude) {
        return (screenWidth - GUI_LONG_CORRECTION - ((((screenRight - (realLongitude * GUI_GPS_SCALE))
                * screenWidth) / longCoeficient) * longScale));
    }
    
    /**
     * Zväčší zemepisný údaj vrchného okraja riadenej oblasti
     * toľko krát, aká je hodnota konštanty {@code AIRPORT_BORDERS_SCALE}.
     * 
     * @return Zväčšený údaj ako reálne číslo.
     */
    private double calculateAreaTopBorder() {
        return airportData.getAreaTopBorder() * AIRPORT_BORDERS_SCALE;
    }
    
    /**
     * Zväčší zemepisný údaj pravého okraja riadenej oblasti
     * toľko krát, aká je hodnota konštanty {@code AIRPORT_BORDERS_SCALE}.
     * 
     * @return Zväčšený údaj ako reálne číslo.
     */
    private double calculateAreaRightBorder() {
        return airportData.getAreaRightBorder() * AIRPORT_BORDERS_SCALE;
    }
    
    /**
     * Vypočíta koeficient používaný pre umiestňovanie objektov
     * na radarovej v horizontálnej polohe.
     * 
     * V podstate je to rozdiel medzi pravou a ľavou stranou
     * riadenej oblasti, ktorej výsledok je zväčšený o konštantu.
     * 
     * @return Hodnota koeficientu ako reálne číslo.
     */
    private double calculateLatCoeficient() {
        return (airportData.getAreaRightBorder() - airportData.getAreaLeftBorder()) * COEFICIENTS_SCALE;
    }
    
    /**
     * Vypočíta koeficient používaný pre umiestňovanie objektov
     * na radarovej vo vertikálnej polohe.
     * 
     * V podstate je to rozdiel medzi hornou a dolnou stranou
     * riadenej oblasti, ktorej výsledok je zväčšený o konštantu.
     * 
     * @return Hodnota koeficientu ako reálne číslo.
     */
    private double calculateLongCoeficient() {
        return (airportData.getAreaTopBorder() - airportData.getAreaBottomBorder()) * COEFICIENTS_SCALE;
    }
    
    /**
     * Vypočíta adekvátnu dĺžku dráhy zobrazovanú na radarovej
     * obrazovke so zmenšením podľa koeficientu {@code GUI_RWY_LENGTH_COEF}.
     * 
     * @param realLength Skutočná dĺžka dráhy v metroch.
     * 
     * @return Dĺžka dráhy pre radarovú obrazovku v pixeloch.
     */
    public double calculateGUIRunwayLength(double realLength) {
        return realLength * GUI_RWY_LENGTH_COEF;
    }
    
    /**
     * Prepočíta skutočný uhol dráhy na uhol vhodný pre výpočet
     * koncových súradníc dráhy na radare tak, aby bola zobrazená
     * v správnom uhle.
     * 
     * Ak je číslo dráhy viac ako 18, čo odpovedá 180° tak sa len
     * uhol dráhy odčíta od 360° čím sa získa naklonenie dráhy na radare
     * voči severnej strane. Na to aby sa dali vypočítať aj koncové rúradnice
     * dráhy, keďže sa čiary kreslia do obdĺžnika musíme vypočítať hodnoty sínusu
     * a cosínusu v pravouhlom trojuholníku, na ktoré potrebujeme hodnoty menšie ako 180°.
     * 
     * @param runwayNumber Číslo dráhy, pre ktorú sa má uhol vypočítať.
     * 
     * @return Vypočítaný uhol menší ako 180° .
     */
    private double calculateGUIRunwayAngle(double runwayNumber) {
        return 360 - ((runwayNumber * 10 >= 180) ? runwayNumber * 10
                                                 : runwayNumber * 10 + 180);
    }
    
    /**
     * Zo zadaných dát dĺžky dráhy a skutočnej dĺžky vypočíta dĺžku
     * priľahlej strany trojuholníka, podľa ktorého sa potom v ďalších
     * metódach vypočítavajú začiatočné a koncové body tak aby mala dráha
     * na obrazovke radaru požadovaný uhol.
     * 
     * @param runwayNumber Číslo dráhy, pre ktorú sa majú dáta vypočítať
     * @param realLength Skutočná dĺžka dráhy v metroch.
     * 
     * @return Dĺžka priľahlej strany k odvesne pravouhlého trojuholníka
     *         vypočítaná s preponou, ktorá je polovicou dĺžky dráhy zmenšenej
     *         pre GUI radaru.
     */
    private double calculateGUIAdjacentLength(double runwayNumber, double realLength) {
        double halfRunwayLength = calculateGUIRunwayLength(realLength) / 2;
        double runwayAngle = calculateGUIRunwayAngle(runwayNumber);
        double runwayAngnleCos = Math.cos(Math.toRadians(runwayAngle));
        return halfRunwayLength * runwayAngnleCos;
    }
    
    /**
     * Zo zadaných dát dĺžky dráhy a skutočnej dĺžky vypočíta dĺžku
     * protiľahlej strany trojuholníka, podľa ktorého sa potom v ďalších
     * metódach vypočítavajú začiatočné a koncové body tak aby mala dráha
     * na obrazovke radaru požadovaný uhol.
     * 
     * @param runwayNumber Číslo dráhy, pre ktorú sa majú dáta vypočítať
     * @param realLength Skutočná dĺžka dráhy v metroch
     * 
     * @return Dĺžka protiľahlej strany k odvesne pravouhlého trojuholníka
     *         vypočítaná s preponou, ktorá je polovicou dĺžky dráhy zmenšenej
     *         pre GUI radaru.
     */
    private double calculateGUIOpositeLength(double runwayNumber, double realLength) {
        double adjacentLength = calculateGUIAdjacentLength(runwayNumber, realLength);
        double adjacentSquared = Math.pow(adjacentLength, 2);
        double halfRunwayLength = calculateGUIRunwayLength(realLength) / 2;
        double runwayLengthSquared = Math.pow(halfRunwayLength, 2);
        return Math.sqrt(runwayLengthSquared - adjacentSquared);
    }
    
    /**
     * Vypočíta začiatočný bod X pre dráhu tak aby mala na obrazovke radaru
     * požadovaný uhol a aby GPS súradnica pre letisko bola presne v polovici dráhy.
     * 
     * @param guiLongitude GPS súradnica dráhy prepočítané pre GUI
     * @param runwayNumber Číslo dráhy, pre ktorú sa údaj počíta
     * @param realLength Skutočná dĺžka dráhy v metroch
     * 
     * @return Vracia začiatočnú súradnicu X dráhy, pre jej zobrazenie na radare
     */
    public double calculateRunwayStartX(double guiLongitude, double runwayNumber,
            double realLength) {
        double runwayLength = calculateGUIRunwayLength(realLength);
        double halfRunwayLength = runwayLength / 2;
        double halfRunwayLengthSquared = Math.pow(halfRunwayLength, 2);
        double adjacentLength = calculateGUIAdjacentLength(runwayNumber, realLength);
        double adjacentSquared = Math.pow(adjacentLength, 2);
        return guiLongitude - Math.sqrt(halfRunwayLengthSquared - adjacentSquared);
    }
    
    /**
     * Vypočíta začiatočný bod Y pre dráhu tak aby mala na obrazovke radaru
     * požadovaný uhol a aby GPS súradnica pre letisko bola presne v polovici dráhy.
     * 
     * @param guiLatitude GPS súradnice dráhy prepočítané pre GUI.
     * @param runwayNumber Číslo dráhy, pre ktorú sa údaj počíta.
     * @param realLength Skutočná dĺžka dráhy v metroch.
     * 
     * @return Vracia začiatočnú súradnicu Y dráhy, pre jej zobrazenie na radare.
     */
    public double calculateRunwayStartY(double guiLatitude, double runwayNumber,
            double realLength) {
        double halfRunwayLength = calculateGUIRunwayLength(realLength) / 2;
        double runwayAngle = calculateGUIRunwayAngle(runwayNumber);
        return guiLatitude - halfRunwayLength * Math.cos(Math.toRadians(runwayAngle));
    }
    
    /**
     * Vypočíta koncový bod X pre dráhu tak aby mala na obrazovke radaru
     * požadovaný uhol a aby GPS súradnica pre letisko bola presne v polovici dráhy.
     * 
     * @param guiLongitude GPS súradnice dráhy prepočítané pre GUI.
     * @param runwayNumber Číslo dráhy, pre ktorú sa údaj počíta.
     * @param realLength Skutočná dĺžka dráhy v metroch.
     * 
     * @return Vracia koncovú súradnicu X dráhy, pre jej zobrazenie na radare.
     */
    public double calculateRunwayEndX(double guiLongitude, double runwayNumber,
            double realLength) {
        double opositeLength = calculateGUIOpositeLength(runwayNumber, realLength);
        return guiLongitude + opositeLength;
    }
    
    /**
     * Vypočíta koncový bod Y pre dráhu tak aby mala na obrazovke radaru
     * požadovaný uhol a aby GPS súradnica pre letisko bola presne v polovici dráhy.
     * 
     * @param guiLatitude GPS súradnice dráhy prepočítané pre GUI.
     * @param runwayNumber Číslo dráhy, pre ktorú sa údaj počíta.
     * @param realLength Skutočná dĺžka dráhy v metroch.
     * 
     * @return Vracia koncovú súradnicu Y dráhy, pre jej zobrazenie na radare.
     */
    public double calculateRunwayEndY(double guiLatitude, double runwayNumber,
            double realLength) {
        double adjacentLength = calculateGUIAdjacentLength(runwayNumber, realLength);
        return guiLatitude + adjacentLength;
    }

    /**
     * Nastavuje objektu aktuálnu šírku radarovej obrazovky.
     * 
     * Túto metódu je potrebné zavolať z metódy {@code paintComponent}
     * v triede {@code RadarScreen} hneď ako prvú, keď je už známa
     * veľkosť obrazovky, pretože inak výpočty nebudú fungovať správne!
     * 
     * @param screenWidth Aktuálna šírka radarovej obrazovky.
     */
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    /**
     * Nastavuje objektu aktuálnu výšku radarovej obrazovky.
     * 
     * Túto metódu je potrebné zavolať z metódy {@code paintComponent}
     * v triede {@code RadarScreen} hneď ako prvú, keď je už známa
     * veľkosť obrazovky, pretože inak výpočty nebudú fungovať správne!
     * 
     * @param screenHeight Aktuálna výška radarovej obrazovky.
     */
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
    
    /**
     * Výpočet grafických dát pre zobrazovanie
     * na radarovej obrazovke, ktoré je závislé od veľkosti
     * zobrazovacej plochy.
     */
    public void calculateAirportGUIData() {
        airportData.setAreaPixelWidth(screenWidth);
        airportData.setAreaPixelHeight(screenHeight);
        // Výpočet pixelovej hodnoty stredového bodu letiska
        double realArptLatitude = airportData.getGpsCoordinates().getLatitude();
        double realArptLongitude = airportData.getGpsCoordinates().getLongitude();
        int guiArptLatitude = (int) calculateGUILongitude(realArptLongitude);
        int guiArptLongitude = (int) calculateGUILatitude(realArptLatitude);
        airportData.setPixelCoordX(guiArptLatitude);
        airportData.setPixelCoordY(guiArptLongitude);
        
        calculateRunwaysGUIData();
        calculateWaypointsGUIData();
    }
    
    /**
     * Vypočítava grafické dáta pre zobrazenie dráh letiska
     * na radarovej obrazovke.
     * 
     * @param guiArptLatitude Zemepisná šírka letiska prepočítaná
     *                        do pixelov na obrazovke.
     * @param guiArptLongitude Zemepisná výška letiska prepočítaná
     *                         do pixelov na obrazovke.
     */
    private void calculateRunwaysGUIData() {
        for (Runway rwy : airportData.getRunways()) {
            double runwayNumber = rwy.getRunwayNumber();
            double runwayLength = rwy.getRunwayLength();
            int runwayLat = (int) calculateGUILongitude(rwy.getRunwayLon());
            int runwayLon = (int) calculateGUILatitude(rwy.getRunwayLat());
            int startX = (int) calculateRunwayStartX(runwayLat, runwayNumber, runwayLength);
            int startY = (int) calculateRunwayStartY(runwayLon, runwayNumber, runwayLength);
            int endX = (int) calculateRunwayEndX(runwayLat, runwayNumber, runwayLength);
            int endY = (int) calculateRunwayEndY(runwayLon, runwayNumber, runwayLength);
            rwy.setPixelCoordStartX(startX);
            rwy.setPixelCoordStartY(startY);
            rwy.setPixelCoordEndX(endX);
            rwy.setPixelCoordEndY(endY);
        }
    }
    
    /**
     * Vypočítava grafické dáta pre zobrazenie cestovných
     * bodov na radarovej obrazovke.
     */
    private void calculateWaypointsGUIData() {
        for (Waypoint wpt : airportData.getWaypoinst()) {
            double wptLatitude = wpt.getGpsCoordinates().getLatitude();
            double wptLongitude = wpt.getGpsCoordinates().getLongitude();
            wpt.setPixelCoordX((int) calculateGUILongitude(wptLongitude));
            wpt.setPixelCoordY((int) calculateGUILatitude(wptLatitude));
        }
    }

}
