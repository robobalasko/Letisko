package net.robobalasko.letiskoserv.navigacia;

/**
 * Trieda {@code VorWaypoint} rozširuje triedu {@link Waypoint}
 * o frekvenciu letového bodu, reprezentuje všesmerový maják VOR.
 *
 * @author rbalasko
 */
public final class VorWaypoint extends Waypoint {

    /**
     * Frekvencia VOR majáku v Mhz.
     */
    private double frequency;

    /**
     * Základný konštruktor nastavuje objketu {@code VorWaypoint} všetky parametre,
     * ktoré požaduje rodičovská trieda {@link Waypoint} aj spolu s frekvenciou rádiomajáku.
     *
     * @param name Názov letového bodu, ktoré sa zobrazuje na radare.
     * @param latitude Zemepisná výška letového bodu.
     * @param longitude Zemepisná šírka letového bodu.
     * @param frequency Frekvencia rádiomajáku v Mhz.
     *
     * @throws InvalidFrequencyException Vyhodená v prípade, že je nastavovaná frekvencia
     *         mimo rozsahu frekvenčného pásma pre VOR rádiomajáky.
     */
    public VorWaypoint(String name, double latitude, double longitude, double frequency)
            throws InvalidFrequencyException {
        super(name, latitude, longitude);
        setFrequency(frequency);
    }

    /**
     * Kontroluje, či zadaná frekvencia spadá do rozsahu pre VOR majáky.
     *
     * @param frequency Hodnota nastavovanej frekvencie.
     */
    private boolean checkFrequency(double frequency) {
        return frequency >= 108 && frequency <= 117.95;
    }

    /**
     * Získa nastavenú frekvenciu VOR majáku.
     * 
     * @return Desatinná hodnota frekvencie VOR majáku.
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * Nastavuje frekvenciu pre VOR maják spolu s kontrolou,
     * či frekvencia je v správnom frekvenčnom pásme.
     *
     * @param frequency Hodnota nastavovanej frekvencie
     *
     * @throws InvalidFrequencyException Vyhodená v prípade, že je nastavovaná
     *         frekvencia mimo rozsahu frekvenčného pásma pre VOR rádiomajáky.
     */
    public void setFrequency(double frequency) throws InvalidFrequencyException {
        if (!checkFrequency(frequency)) {
            throw new InvalidFrequencyException();
        }
        this.frequency = frequency;
    }

}
