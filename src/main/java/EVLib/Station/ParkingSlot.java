package EVLib.Station;

import java.util.concurrent.atomic.AtomicInteger;

public class ParkingSlot {
    private int id;
    private ParkingEvent e;
    private final ChargingStation station;
    private static final AtomicInteger idGenerator = new AtomicInteger(0);
    private boolean inSwitch;
    private volatile boolean running = true;
    private String name;

    public ParkingSlot(ChargingStation station)
    {
        this.id = idGenerator.incrementAndGet();
        this.station = station;
        inSwitch = true;
        this.name = "ParkingSlot " + String.valueOf(id);
    }

    /**
     * Sets a name for the ParkingSlot.
     * @param name The name to be set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return The name of the ParkingSlot.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Executes the inductive charging phase of a parking slot. It works like the ChargingEvent.
     */
    void parkingVehicle() {
        running = true;
        Thread ch = new Thread(new Runnable()
        {
            @Override
            public void run() {
                e.setParkingTime(e.getParkingTime());
                if (e.getCondition().equals("charging")) {
                    e.setChargingTime(e.getChargingTime());
                    long timestamp1 = System.currentTimeMillis();
                    long timestamp2;
                    do {
                        timestamp2 = System.currentTimeMillis();
                    } while (running && (timestamp2 - timestamp1 < e.getChargingTime()));
                    synchronized (this) {
                        e.getElectricVehicle().getBattery().setRemAmount(e.getEnergyToBeReceived() + e.getElectricVehicle().getBattery().getRemAmount());
                        if (e.getElectricVehicle().getDriver() != null)
                            e.getElectricVehicle().getDriver().setDebt(e.getElectricVehicle().getDriver().getDebt() + e.getEnergyToBeReceived() * station.getInductivePrice());
                        System.out.println("The inductive charging " + e.getId() + " completed successfully");
                    }
                }
                e.setCondition("parking");
                long diff = e.getParkingTime() - e.getChargingTime();
                long timestamp1 = System.currentTimeMillis();
                long timestamp2;
                do {
                    timestamp2 = System.currentTimeMillis();
                } while (running && (timestamp2 - timestamp1 < diff));
                synchronized (this) {
                    System.out.println("The parking " + e.getId() + " completed successfully");
                    e.setCondition("finished");
                    ParkingEvent.parkLog.add(e);
                    setParkingEvent(null);
                }
            }
        });
        if(station.getDeamon())
            ch.setDaemon(true);
        ch.setName("ParkingSlot: " + String.valueOf(id));
        ch.start();
    }

    /**
     * Enables or disables the inductive charging of a parking slot.
     */
    public void setInSwitch(boolean inSwitch)
    {
        this.inSwitch = inSwitch;
    }

    /**
     *
     * @return If the inductive charging is enabled or not, for this parking slot.
     */
    public boolean getInSwitch()
    {
        return inSwitch;
    }

    /**
     * @return The ParkingEvent which is linked with the ParkingSlot.
     */
    public ParkingEvent getParkingEvent() {
        return e;
    }

    /**
     * Sets a ParkingEvent in the Charger.
     *
     * @param ev The ParkingEvent to be linked with the ParkingSlot.
     */
    void setParkingEvent(ParkingEvent ev) {
        this.e = ev;
    }

    /**
     * @return The id of the ParkingSlot object.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id for this ParkingSlot.
     * @param id The id to be set.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Stops the operation of the ParkingSlot.
     */
    public void stopParkingSlot()
    {
        running = false;
    }
}
