package Sources;

import Station.ChargingStation;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class EnergySource
{
    private int id;
    private ChargingStation station;
    private static AtomicInteger idGenerator = new AtomicInteger(0);

    public EnergySource(ChargingStation station)
    {
        this.id = idGenerator.getAndIncrement();
        this.station = station;
    }

    public abstract double popAmount();


    public abstract void insertAmount(double am);

    /**
     * Returns the id of the EnergySource.
     * @return The id of the EnergySource.
     */
    public int reId()
    {
        return id;
    }

    /**
     * Returns the ChargingStation of the EnergySource.
     * @return The ChagingStation of the EnergySource.
     */
    public ChargingStation reStation()
    {
        return station;
    }
}
