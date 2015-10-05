package net.robobalasko.letiskoserv.navigacia;

/**
 * Trieda s podmienkou, ktorá je vyhodená ak sa daná
 * letová trasa podľa zadaného názvu v zozname trás letiska nenájde.
 * 
 * @author rbalasko
 */
public class RouteNotLoadedException extends Exception {

    public RouteNotLoadedException() {
    }

}
