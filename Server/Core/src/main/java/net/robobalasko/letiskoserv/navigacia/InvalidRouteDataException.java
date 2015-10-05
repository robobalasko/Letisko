package net.robobalasko.letiskoserv.navigacia;

/**
 * Trieda s podmienkou, ktorá je vyhodená ak už náhodou
 * práve pridávaná dráha do zoznamu dráv na letisku v tomto
 * zozname už existuje.
 * 
 * @author rbalasko
 */
public class InvalidRouteDataException extends Exception {

    public InvalidRouteDataException() {
    }

}
