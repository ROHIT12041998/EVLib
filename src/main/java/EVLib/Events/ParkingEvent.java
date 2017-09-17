package EVLib.Events;

import EVLib.EV.ElectricVehicle;
import EVLib.Station.ChargingStation;
import EVLib.Station.ParkingSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ParkingEvent {

    private int id;
    private static AtomicInteger idGenerator = new AtomicInteger(0);
    private long parkingTime;
    private ElectricVehicle vehicle;
    private ChargingStation station;
    private long timeOfCharging;
    private double amountOfEnergy;
    private double energyToBeReceived;
    private int parkingSlotId;
    private long timestamp1;
    private long timestamp2;
    private String condition;
    private double cost;
    public static List<ParkingEvent> parkLog = new ArrayList<>();

    public ParkingEvent(ChargingStation station, ElectricVehicle vehicle, long parkingTime)
    {
        this.id = idGenerator.incrementAndGet();
        this.station = station;
        this.vehicle = vehicle;
        this.parkingSlotId = -1;
        this.condition = "arrived";
        this.parkingTime = parkingTime;
    }

    public ParkingEvent(ChargingStation station, ElectricVehicle vehicle, long parkingTime, double amountOfEnergy)
    {
        this.id = idGenerator.incrementAndGet();
        this.vehicle = vehicle;
        this.station = station;
        this.amountOfEnergy = amountOfEnergy;
        this.parkingSlotId = -1;
        this.condition = "arrived";
        this.parkingTime = parkingTime;
    }

    /**
     * @return The ElectricVehicle object.
     */
    public ElectricVehicle getElectricVehicle()
    {
        return vehicle;
    }

    /**
     * Executes the preprocessing phase. Checks for any ParkingSlot,
     * calculates the energy to be given to the Vehicle and calculates the charging time.
     * If there is not any empty ParkingSlot the ChargingEvent is charecterized as "nonExecutable".
     */
    public void preProcessing()
    {
        int qwe = station.checkParkingSlots();
        if ((qwe != -1) && (qwe != -2)) {
            parkingSlotId = qwe;
            ParkingSlot ps = station.searchParkingSlot(parkingSlotId);
            if (ps.getInSwitch()&&(vehicle.getBattery().getActive())) {
                if (amountOfEnergy < station.getTotalEnergy()) {
                    if (amountOfEnergy <= (vehicle.getBattery().getCapacity() - vehicle.getBattery().getRemAmount()))
                        energyToBeReceived = amountOfEnergy;
                    else
                        energyToBeReceived = vehicle.getBattery().getCapacity() - vehicle.getBattery().getRemAmount();
                } else {
                    if (station.getTotalEnergy() <= (vehicle.getBattery().getCapacity() - vehicle.getBattery().getRemAmount()))
                        energyToBeReceived = station.getTotalEnergy();
                    else
                        energyToBeReceived = vehicle.getBattery().getCapacity() - vehicle.getBattery().getRemAmount();
                    if (energyToBeReceived == 0) {
                        setCondition("parking");
                        ps.setParkingEvent(this);
                        ps.changeSituation();
                        return;
                    }
                }
                setChargingTime((long) ((energyToBeReceived) / station.getInductiveRatio()));
                if (timeOfCharging > parkingTime) {
                    energyToBeReceived = parkingTime * station.getInductiveRatio();
                    timeOfCharging = parkingTime;
                }
                setCondition("charging");
                cost = station.calculatePrice(this);
                ps.setParkingEvent(this);
                ps.changeSituation();
            }
            else
            {
                setCondition("parking");
                ps.setParkingEvent(this);
                ps.changeSituation();
            }
        }
        else
            setCondition("nonExecutable");
    }

    /**
     * Executes the parking/charging phase of a vehicle.
     */
    public void execution()
    {
        if (condition.equals("parking"))
        {
            station.searchParkingSlot(parkingSlotId).setCommitTime(parkingTime);
            setParkingTime(parkingTime);
            timestamp2 = System.currentTimeMillis();
            station.searchParkingSlot(parkingSlotId).parkingVehicle();
            parkLog.add(this);
        }
        else if (condition.equals("charging"))
        {
            station.searchParkingSlot(parkingSlotId).setCommitTime(parkingTime);
            setParkingTime(parkingTime);
            timestamp1 = System.currentTimeMillis();
            station.searchParkingSlot(parkingSlotId).setChargingTime(timeOfCharging);
            double sdf;
            sdf = energyToBeReceived;
            HashMap<String, Double> keys = new HashMap<>(station.getMap());
            for (HashMap.Entry<String, Double> energy : keys.entrySet()) {
                if (energyToBeReceived < station.getMap().get(energy.getKey())) {
                    double ert = station.getMap().get(energy.getKey()) - sdf;
                    station.setSpecificAmount(energy.getKey(), ert);
                    break;
                } else {
                    sdf -= energy.getValue();
                    station.setSpecificAmount(energy.getKey(), 0);
                }
            }
            station.searchParkingSlot(parkingSlotId).parkingVehicle();
            parkLog.add(this);
        }
    }

    /**
     * @return The ChargingStation the event is going to be executed.
     */
    public ChargingStation getStation()
    {
        return station;
    }

    /**
     * @return The energy to be given to the ElectricVehicle.
     */
    public double getEnergyToBeReceived()
    {
        return energyToBeReceived;
    }

    /**
     * Sets the value of e, as the energy to be given in the ElectricVehicle.
     * @param e Energy to be given.
     */
    public void setEnergyToBeReceived(double e) {
        energyToBeReceived = e;
    }

    /**
     * @return The elapsed charging time of the ParkingEvent.
     */
    public long getElapsedChargingTime()
    {
        long diff = System.currentTimeMillis() - timestamp1;
        if (timeOfCharging - diff >= 0)
            return timeOfCharging - diff;
        else
            return 0;
    }

    /**
     * @return The charging time of the ElectricVehicle.
     */
    public long getChargingTime()
    {
        return timeOfCharging;
    }

    /**
     * @return The parking time.
     */
    public long getParkingTime()
    {
        return parkingTime;
    }

    /**
     * Sets the charging time of the ParkingEvent.
     * @param time The charging time.
     */
    public void setChargingTime(long time)
    {
        timestamp1 = System.currentTimeMillis();
        this.timeOfCharging = time;
    }

    /**
     * Sets the id of the ParkingSlot the ParkingEvent is going to be executed.
     * @param id The id of ParkingSlot.
     */
    public void setParkingSlotId(int id)
    {
        this.parkingSlotId = id;
    }

    /**
     * Sets the condition of ParkingEvent.
     * @param condition Value of the condition.
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    /**
     * @return The condition of ParkingEvent.
     */
    public String getCondition()
    {
        return condition;
    }

    /**
     * @return The elapsed time the vehicle will be parking/charging.
     */
    public long getElapsedParkingTime()
    {
        long diff = System.currentTimeMillis() - timestamp2;
        if (parkingTime - diff >= 0)
            return parkingTime - diff;
        else
            return 0;
    }

    /**
     * Sets the parking time for the ElectricVehicle.
     * @param parkingTime The parking time.
     */
    public void setParkingTime(long parkingTime)
    {
        timestamp2 = System.currentTimeMillis();
        this.parkingTime = parkingTime;
    }

    /**
     * @return The id of this ParkingEvent.
     */
    public int getId()
    {
        return id;
    }

    /**
     * @return The energy the ElectricVehicle asked.
     */
    public double getAmountOfEnergy()
    {
       return amountOfEnergy;
    }

    /**
     * @return The cost of this ParkingEvent.
     */
    public double getCost()
    {
        return cost;
    }
}