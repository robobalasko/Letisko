package net.robobalasko.letiskoserv.navigacia;

/**
 * Trieda s podmienkou, ktorá je vyhodená ak frekvencia
 * pre bod typu VOR nespadá do intervalu frekvencií alokovaných
 * pre tieto typy rádiomajákov.
 * 
 * @author rbalasko
 */
class InvalidFrequencyException extends Exception {

    public InvalidFrequencyException() {
    }
    
}
