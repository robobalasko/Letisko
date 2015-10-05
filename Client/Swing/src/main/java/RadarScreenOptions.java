

public class RadarScreenOptions {
    
    /**
     * Zobrazovanie názovo letových bodov na radare.
     */
    private boolean dispPointNames;
    
    /**
     * Zobrazovanie GPS súradníc letových bodov na radare.
     */
    private boolean dispPointGps;
    
    /**
     * Zobrazovanie frekvencie bodov typu VOR na radare.
     */
    private boolean dispPointFreq;
    
    /**
     * Zobrazovanie diaľkových kruhov od stredového bodu letiska na radare.
     */
    private boolean dispDistCircles;
	
	/**
	 * Zobrazovanie typu lietadla.
	 */
	private boolean dispAircraftType;
	
	/**
	 * Zobrazovanie aktuálnej výšky lietadiel.
	 */
	private boolean dispAircraftActualFlightLevel;
	
	/**
	 * Zobrazovanie maximálne povolenej výšky.
	 */
	private boolean dispAircraftFinalFlightLevel;
	
	/**
	 * Zobrazovanie aktuálnej rýchlosti lietadla.
	 */
	private boolean dispAircraftActualSpeed;
	
	/**
	 * Zobrazovanie maximálne povolenej rýchlosti.
	 */
	private boolean dispAircraftFinalSpeed;
    
    /**
     * Základný konštruktor určuje základné nastavenie
     * pre zobrazovanie na radare; defaultne sa zobrazujú
     * len názvy bodov a diaľkové kruhy od letiska.
     */
    public RadarScreenOptions() {
        dispPointNames = true;
        dispDistCircles = true;
		dispAircraftType = true;
		dispAircraftActualSpeed = true;
		dispAircraftActualFlightLevel = true;
    }

	public boolean isDispPointNames() {
		return dispPointNames;
	}

	public void setDispPointNames(boolean dispPointNames) {
		this.dispPointNames = dispPointNames;
	}

	public boolean isDispPointGps() {
		return dispPointGps;
	}

	public void setDispPointGps(boolean dispPointGps) {
		this.dispPointGps = dispPointGps;
	}

	public boolean isDispPointFreq() {
		return dispPointFreq;
	}

	public void setDispPointFreq(boolean dispPointFreq) {
		this.dispPointFreq = dispPointFreq;
	}

	public boolean isDispDistCircles() {
		return dispDistCircles;
	}

	public void setDispDistCircles(boolean dispDistCircles) {
		this.dispDistCircles = dispDistCircles;
	}

	public boolean isDispAircraftType() {
		return dispAircraftType;
	}

	public void setDispAircraftType(boolean dispAircraftType) {
		this.dispAircraftType = dispAircraftType;
	}

	public boolean isDispAircraftActualFlightLevel() {
		return dispAircraftActualFlightLevel;
	}

	public void setDispAircraftActualFlightLevel(boolean dispAircraftActualFlightLevel) {
		this.dispAircraftActualFlightLevel = dispAircraftActualFlightLevel;
	}

	public boolean isDispAircraftFinalFlightLevel() {
		return dispAircraftFinalFlightLevel;
	}

	public void setDispAircraftFinalFlightLevel(boolean dispAircraftFinalFlightLevel) {
		this.dispAircraftFinalFlightLevel = dispAircraftFinalFlightLevel;
	}

	public boolean isDispAircraftActualSpeed() {
		return dispAircraftActualSpeed;
	}

	public void setDispAircraftActualSpeed(boolean dispAircraftActualSpeed) {
		this.dispAircraftActualSpeed = dispAircraftActualSpeed;
	}

	public boolean isDispAircraftFinalSpeed() {
		return dispAircraftFinalSpeed;
	}

	public void setDispAircraftFinalSpeed(boolean dispAircraftFinalSpeed) {
		this.dispAircraftFinalSpeed = dispAircraftFinalSpeed;
	}

}
