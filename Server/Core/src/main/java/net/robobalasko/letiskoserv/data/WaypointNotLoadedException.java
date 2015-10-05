package net.robobalasko.letiskoserv.data;

/**
 * Trieda definujúca podmienku, ktorá je vyhodená ak sa nepodarí
 * nájsť v zozname bodov letiska požadovaný bod podľa jeho názvu.
 * 
 * @author rbalasko
 */
public class WaypointNotLoadedException extends Exception {

    public WaypointNotLoadedException() {
    }

}
