package net.robobalasko.letiskoserv.data;

/**
 * Trieda definujúca podmienku, ktorá je vyhodená ak sa už v zozname
 * ďalších bodov na trase lietadla žiadny bod nenachádza, ale lietadlo
 * ešte stále požaduje nejaký ďalší bod trasy.
 * 
 * @author rbalasko
 */
public class EmptyRouteException extends Exception {

    public EmptyRouteException() {
    }

}
