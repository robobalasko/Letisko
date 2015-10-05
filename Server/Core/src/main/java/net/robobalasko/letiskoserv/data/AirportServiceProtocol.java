package net.robobalasko.letiskoserv.data;

/**
 * Trieda definujúca protokol, ktorým medzi sebou klienti a server komunikujú
 *
 * @author rbalasko
 */
public class AirportServiceProtocol {

    /**
     * Ukončenie komunikcácie klienta so serverom.
     */
    public static final int END_COM = -1;

    /**
     * Server čaká na požiadavku od klienta.
     */
    public static final int WAITING = 0;

    /**
     * Požiadavka na odoslanie zoznamu dostupných letísk.
     */
    public static final int SEND_AIRPRT_LIST = 2;

    /**
     * Odpoveď po úspešnom odoslaní zoznamu dostupných letísk.
     */
    public static final int SENT_AIRPRT_LIST = 3;

    /**
     * Požiadavka na odoslanie dát o letisku.
     */
    public static final int SEND_AIRPRT_DATA = 4;

    /**
     * Odpoveď po úspešnom odoslaní dát o letisku.
     */
    public static final int SENT_AIRPRT_DATA = 5;

    /**
     * Požiadavka na odoslanie dát o lietadlách na radare.
     */
    public static final int SEND_AIRCFT_DATA = 6;

    /**
     * Odpoveď po úspešnom odoslaní dát o lietadlách na radare.
     */
    public static final int SENT_AIRCFT_DATA = 7;

    /**
     * Odpoveď po úspešnom odoslaní dát o rozmere obrazovky radaru.
     */
    public static final int SENT_GUISCR_DATA = 8;

    /**
     * Odpoveď po úspešnom prijatí požiadavky o rozmere obrazovky radaru.
     */
    public static final int RCVD_GUISCR_DATA = 9;

    /**
     * Odpoveď po úspešnom prijatí zoznamu modifikovaných lietadiel.
     */
    public static final int RCVD_MODACFT_DATA = 12;

    /**
     * Aktuálny stav komunikácie medzi vláknom a klientom.
     */
    private int state = WAITING;

    /**
     * Získa aktuálny stav komunikácie medzi vláknom a klientom
     *
     * @return Číselná reprezentácia stavu
     */
    public int getState() {
        return state;
    }

    /**
     * Zmení aktuálny stav komunikácie na požadovaný stav
     *
     * @param state Číselná reprezentácia stavu, na ktorý sa má aktuálny stav zmeniť
     */
    public void setState(int state) {
        this.state = state;
    }

}
