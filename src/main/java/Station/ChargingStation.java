package Station;

import Events.ParkingEvent;
import Sources.*;
import EV.Battery;
import EV.ElectricVehicle;
import Events.DisChargingEvent;
import Events.ChargingEvent;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.StopWatch;

public class ChargingStation
{
    private int id;
    private String name;
    private WaitList slow;
    private WaitList fast;
    private WaitList discharging;
    private WaitList exchange;
    private double chargingRatioSlow;
    private double chargingRatioFast;
    private double disChargingRatio;
    private ArrayList<Charger> chargers;
    private ArrayList<EnergySource> n;
    private ArrayList<DisCharger> dischargers;
    private ArrayList<Battery> batteries;
    private ArrayList<ExchangeHandler> exchangeHandlers;
    private ArrayList<ParkingSlot> parkingSlots;
    private Charger r;
    private EnergySource  c;
    private HashMap<String, Double> amounts;
    private double totalEnergy;
    private ArrayList<String> sources;
    private double unitPrice;
    private double disUnitPrice;
    private double exchangePrice;
    private boolean automaticQueueHandling;
    private int updateSpace;
    private long timeOfExchange;
    private StopWatch date;
    private long lastUpdate;
    private double inductiveChargingRatio;
    private static AtomicInteger idGenerator = new AtomicInteger(0);

    public ChargingStation(String name, String[] kinds, String[] source, double[][] energAm)
    {
        this.date = new StopWatch ();
        date.start ();
        this.amounts = new HashMap<> ();
        this.id = idGenerator.getAndIncrement();
        this.name = name;
        this.automaticQueueHandling = true;
        this.slow = new WaitList("charging");
        this.fast = new WaitList("charging");
        this.exchange = new WaitList("charging");
        this.discharging = new WaitList("discharging");
        this.chargers = new ArrayList<>();
        this.dischargers = new ArrayList<>();
        this.exchangeHandlers = new ArrayList<>();
        this.parkingSlots = new ArrayList<>();
        this.n = new ArrayList<>();
        this.sources = new ArrayList<>();
        this.sources.add("discharging");
        this.batteries = new ArrayList<>();
        for (int q=0; q<source.length; q++)
            sources.add(q, source[q]);
        this.chargingRatioSlow = 1;
        this.chargingRatioFast = 2;
        this.disChargingRatio = 1;
        this.inductiveChargingRatio = 0.5;
        this.updateSpace = 20;
        for (int i=0; i<source.length; i++)
        {
            if (source[i].equals("solar"))
            {
                c = new Solar(this, energAm[i]);
                n.add(i, c);
                amounts.put("solar", 0.0);
            }
            else if (source[i].equals("wind"))
            {
                c = new Wind(this, energAm[i]);
                n.add(i, c);
                amounts.put("wind", 0.0);
            }
            else if (source[i].equals("geothermal"))
            {
                c = new Geothermal(this, energAm[i]);
                n.add(i, c);
                amounts.put("geothermal", 0.0);
            }
            else if (source[i].equals("wave"))
            {
                c = new Wave(this, energAm[i]);
                n.add(i, c);
                amounts.put("wave", 0.0);
            }
            else if (source[i].equals("hydroelectric"))
            {
                c = new HydroElectric(this, energAm[i]);
                n.add(i, c);
                amounts.put("hydroelectric", 0.0);
            }
            else if (source[i].equals("nonrenewable"))
            {
                c = new NonRenewable(this, energAm[i]);
                n.add(i, c);
                amounts.put("nonrenewable", 0.0);
            }
        }
        for (int i=0; i<kinds.length; i++)
        {
            if (!kinds[i].equals("exchange"))
            {
                r = new Charger(this, kinds[i]);
                chargers.add(i, r);
            }
        }
        updateStorage ();
    }

    public ChargingStation(String name, String[] kinds, String[] source)
    {
        this.date = new StopWatch();
        date.start();
        this.amounts = new HashMap<>();
        this.id = idGenerator.getAndIncrement();
        this.name = name;
        this.slow = new WaitList("charging");
        this.fast = new WaitList("charging");
        this.exchange = new WaitList("charging");
        this.discharging = new WaitList("discharging");
        this.automaticQueueHandling = true;
        this.chargers = new ArrayList<>();
        this.dischargers = new ArrayList<>();
        this.batteries = new ArrayList<>();
        this.exchangeHandlers = new ArrayList<>();
        this.parkingSlots = new ArrayList<>();
        this.n = new ArrayList<>();
        this.sources = new ArrayList<>();
        this.sources.add("discharging");
        for (int q=0; q<source.length; q++)
            sources.add(q, source[q]);
        this.chargingRatioSlow = 1;
        this.chargingRatioFast = 2;
        this.disChargingRatio = 1;
        this.updateSpace = 20;
        this.inductiveChargingRatio = 0.5;
        for (int i=0; i<source.length; i++)
        {
            if (source[i].equals("solar"))
            {
                c = new Solar(this);
                n.add(i, c);
                amounts.put("solar",0.0);
            }
            else if (source[i].equals("wind"))
            {
                c = new Wind(this);
                n.add(i, c);
                amounts.put("wind", 0.0);
            }
            else if (source[i].equals("geothermal"))
            {
                c = new Geothermal(this);
                n.add(i, c);
                amounts.put("geothermal", 0.0);
            }
            else if (source[i].equals("wave"))
            {
                c = new Wave(this);
                n.add(i, c);
                amounts.put("wave",0.0);
            }
            else if (source[i].equals("hydroelectric"))
            {
                c = new HydroElectric(this);
                n.add(i, c);
                amounts.put("hydroelectric",0.0);
            }
            else if (source[i].equals("nonrenewable"))
            {
                c = new NonRenewable(this);
                n.add(i, c);
                amounts.put("nonrenewable", 0.0);
            }
        }
        for (int i=0; i<kinds.length; i++)
        {
            if (!kinds[i].equals("exchange"))
            {
                r = new Charger(this,kinds[i]);
                chargers.add(i, r);
            }
        }
        updateStorage ();
    }

    public ChargingStation(int id, String name)
    {
        this.date = new StopWatch();
        date.start();
        this.id = idGenerator.getAndIncrement();
        this.name = name;
        this.slow = new WaitList("charging");
        this.fast = new WaitList("charging");
        this.exchange = new WaitList("charging");
        this.discharging = new WaitList("discharging");
        this.parkingSlots = new ArrayList<>();
        this.amounts = new HashMap<>();
        this.chargers = new ArrayList<>();
        this.dischargers = new ArrayList<>();
        this.batteries = new ArrayList<>();
        this.exchangeHandlers = new ArrayList<>();
        this.dischargers = new ArrayList<> ();
        this.n = new ArrayList<>();
        this.sources = new ArrayList<>();
        this.sources.add("discharging");
        this.automaticQueueHandling = true;
        this.chargingRatioSlow = 1;
        this.chargingRatioFast = 2;
        this.disChargingRatio = 1;
        this.updateSpace = 20;
        this.inductiveChargingRatio = 0.5;
    }

    /**
     * @return The id of the ChargingStation object
     */
    public int reId()
    {
        return this.id;
    }


    /**
     * @return The elapsed time since the start of the charging station
     */

    public long getTime()
    {
        return date.getTime();
    }

    /**
     * Adds a ChargingEvent in the corresponding waiting ArrayList.
     * @param y The ChargingEvent that is going to be added.
     */
    public void updateQueue(ChargingEvent y)
    {
        switch (y.reKind())
        {
            case "exchange":
                exchange.insertElement(y);
                break;
            case "slow":
                slow.insertElement(y);
                break;
            case "fast":
                fast.insertElement(y);
                break;
            default:
                System.out.println ("");
                break;
        }
    }

    /**
     * Adds a DisChargingEvent in the waiting ArrayList.
     * @param d The DisChargingEvent that is going to be added.
     */
    public void updateDisChargingQueue(DisChargingEvent d)
    {
        discharging.insertElement(d);
    }

    /**
     * @return The WaitngArrayList object for the fast charging.
     */
    public WaitList reFast()
    {
        return fast;
    }

    /**
     * @return The WaitingArrayList object for the slow charging.
     */
    public WaitList reSlow()
    {
        return slow;
    }

    /**
     * @return The WaitingArrayList object for the exchange battery.
     */
    public WaitList reExchange()
    {
        return exchange;
    }

    /**
     * @return The WaitingArrayList object for the discharging.
     */
    public WaitList reDischarging()
    {
        return discharging;
    }

    /**
     * Checks for empty Charger according to the kind that is given.
     * @param k The kind of Charger that is asked.
     * @return Returns the id of the Charger in case there is any empty Charger
     * object, -2 if the charging station is not linked with any Charger object or -1 if all the Charger are busy.
     */
    public int checkChargers(String k)
    {
        for(int i=0; i<reChargers().length; i++)
        {
            if (k.equals(reChargers()[i].reKind()))
                if (!reChargers()[i].reBusy())
                    return reChargers()[i].reId();
        }
        if (reChargers().length == 0)
            return -2;
        return -1;
    }

    /**
     * Checks for any empty Discharger.
     * @return The id of the empty DisCharger, or -1 if there is not any empty, 
     * or -2 if the charging station is not linked with any DisCharger object
     */
    public int checkDisChargers()
    {
        for(int i=0; i<reDisChargers().length; i++)
        {
            if(!reDisChargers()[i].reBusy())
                return reDisChargers()[i].reId();
        }
        if (reDisChargers().length == 0)
            return -2;
        return -1;
    }

    /**
     * Checks for any empty exchange slot.
     * @return The number of the slot, or -1 if there is not any empty slot, 
     * or -2 if the charging station is not linked with any ExchangeHandler object.
     */
    public int checkExchangeHandlers()
    {
        for (int i=0; i<reExchangeHandlers().length; i++)
        {
            if (!reExchangeHandlers()[i].reBusy())
                return reExchangeHandlers()[i].reId();
        }
        if (reExchangeHandlers().length == 0)
            return -2;
        return -1;
    }

    /**
     *
     */
    public int checkParkingSlots()
    {
        for(ParkingSlot p: reParkingSlots())
        {
            if (!p.reBusy())
                return p.reId();
        }
        if (parkingSlots.size() == 0)
            return -2;
        return -1;
    }

    /**
     * @return Returns the position of the Battery, or -1 if there is not any Battery.
     */
    public int checkBatteries()
    {
        for (int i=0; i<reBatteries().size(); i++)
        {
            if (reBatteries().get(i).reRemAmount() != 0)
                return i;
        }
        if (reBatteries().size() == 0)
            return -2;
        return -1;
    }


    /**
     * @return Returns all the ExchangeHandler objects.
     */
    public ExchangeHandler[] reExchangeHandlers()
    {
        ExchangeHandler[] g = new ExchangeHandler[exchangeHandlers.size()];
        for(int i = 0; i< exchangeHandlers.size(); i++)
            g[i] = exchangeHandlers.get(i);
        return g;
    }

    /**
     * @return Returns all the ParkingSlot objects.
     */
    public ParkingSlot[] reParkingSlots()
    {
        ParkingSlot[] g = new ParkingSlot[parkingSlots.size()];
        for(int i = 0; i< parkingSlots.size(); i++)
            g[i] = parkingSlots.get(i);
        return g;
    }

    /**
     * adds a Charger to the ChargingStation.
     * @param y The Charger to be added.
     */
    public void addCharger(Charger y)
    {
        chargers.add(y);
    }

    /**
     * adds a Discharger to the ChargingStation.
     * @param y The DisCharger to be added.
     */
    public void addDisCharger(DisCharger y)
    {
        dischargers.add(y);
    }

    /**
     * Inserts a new ExchangeHandler in the charging station.
     * @param y The ExchangeHandler object to be added.
     */
    public void addExchangeHandler(ExchangeHandler y)
    {
        exchangeHandlers.add(y);
    }

    /**
     * adds a new EnergySource to the ChargingStation.
     * @param z The EnergySource is going to be added.
     */
    public void addEnergySource(EnergySource z)
    {
        n.add(z);
        if (z instanceof Solar)
        {
            sources.add("solar");
            amounts.put("solar", 0.0);
        }
        else if (z instanceof Wave)
        {
            sources.add("wave");
            amounts.put("wave", 0.0);
        }
        else if (z instanceof Wind)
        {
            sources.add("wind");
            amounts.put("wind", 0.0);
        }
        else if (z instanceof HydroElectric)
        {
            sources.add("hydroElectric");
            amounts.put("hydroelectric", 0.0);
        }
        else if (z instanceof Geothermal)
        {
            sources.add("geothermal");
            amounts.put("geothermal", 0.0);
        }
        else if (z instanceof NonRenewable)
        {
            sources.add("nonrenewable");
            amounts.put("nonrenewable", 0.0);
        }
    }

    /**
     * Deletes an EnergySource from the ChargingStation.
     * @param z The EnergySource is going to be removed.
     */
    public void deleteEnergySource(EnergySource z)
    {
        n.remove(z);
        if (z instanceof Solar)
        {
            amounts.remove("solar");
            sources.remove("solar");
        }
        else if (z instanceof Wave)
        {
            amounts.remove("wave");
            sources.remove("wave");
        }
        else if (z instanceof Wind)
        {
            amounts.remove("wind");
            sources.remove("wind");
        }
        else if (z instanceof HydroElectric)
        {
            amounts.remove("hydroelectric");
            sources.remove("hydroelectric");
        }
        else if (z instanceof NonRenewable)
        {
            amounts.remove("nonrenewable");
            sources.remove("nonrenewable");
        }
        else if (z instanceof Geothermal)
        {
            amounts.remove("geothermal");
            sources.remove("geothermal");
        }
    }

    /**
     * Sorts the energies sources according to the desire of the user.
     * @param energies It is a String array that defines the energies order.
     */
    public void customEnergySorting(String[] energies)
    {
        sources.clear();
        for(int i = 0; i < energies.length; i++)
            sources.add(i, energies[i]);
    }

    /**
     * Adds a Battery to the ChargingStation for the battery exchange function.
     * @param battery The Battery is going to be added.
     */
    public void joinBattery(Battery battery)
    {
        batteries.add(battery);
    }

    /**
     * Returns the batteries that are destined to be used for battery exchange.
     * @return A ArrayList with the Battery objects.
     */
    public ArrayList<Battery> reBatteries()
    {
        return batteries;
    }

    /**
     * Deletes a Battery from the batteries for the battery exchange function.
     * @param battery The battery that will be removed.
     * @return True if the deletion was successfull, false if it was unsuccessfull.
     */
    public boolean deleteBattery(Battery battery)
    {
        boolean remove = batteries.remove(battery);
        return remove;
    }

    /**
     * @return Returns the DisCharger objects of the ChargingStation.
     */
    public DisCharger[] reDisChargers()
    {
        DisCharger[] g = new DisCharger[dischargers.size()];
        for (int i = 0; i < dischargers.size(); i++)
            g[i] = dischargers.get(i);
        return g;
    }

    /**
     * Search for the Charger based on the given id.
     * @param id The id of the Charger which is asked.
     * @return The Charger object.
     */
    public Charger searchCharger(int id)
    {
        Charger y = null;
        for(int i=0; i<chargers.size(); i++)
        {
            if (chargers.get(i).reId() == id)
                y = chargers.get(i);
        }
        return y;
    }

    /**
     * Search for a DisCharger object based on the given id.
     * @param id The id of the DisCharger object which is asked.
     * @return The DisCharger object.
     */
    public DisCharger searchDischarger(int id)
    {
        DisCharger y = null;
        for(int i=0;i<dischargers.size();i++)
        {
            if (dischargers.get(i).reId() == id)
                y = dischargers.get(i);
        }
        return y;
    }

    /**
     * Search for an ExchangeHandler object based on the given id.
     * @param id The id of the ExchangeHandler object which is asked.
     * @return The ExchangeHandler object.
     */
    public ExchangeHandler searchExchangeHandler(int id)
    {
        ExchangeHandler y = null;
        for(int i=0; i<exchangeHandlers.size(); i++)
        {
            if (exchangeHandlers.get(i).reId() == id)
                y = exchangeHandlers.get(i);
        }
        return y;
    }

    /**
     * Search for an ParkingSlot object based on the given id.
     * @param id The id of the ParkingSlot object which is asked.
     * @return The ParkingSlot object.
     */
    public ParkingSlot searchParkingSlot(int id)
    {
        ParkingSlot y = null;
        for(int i=0; i<parkingSlots.size(); i++)
        {
            if (parkingSlots.get(i).reId() == id)
                y = parkingSlots.get(i);
        }
        return y;
    }

    /**
     * @return The array with the Charger objects.
     */
    public Charger[] reChargers()
    {
        Charger[] g = new Charger[chargers.size()];
        for(int i = 0; i< chargers.size(); i++)
            g[i] = chargers.get(i);
        return g;
    }

    /**
     * @return The array with the kind of energies.
     */
    public String[] reSources()
    {
        String[] g = new String[sources.size()];
        for(int i = 0; i<sources.size(); i++)
            g[i] = sources.get(i);
        return g;
    }

    /**
     * @return A HashMap object with the amounts of each kind of energy.
     */
    public HashMap<String, Double> reMap()
    {
        return amounts;
    }

    /**
     * Sets an amount of energy in a specific kind of energy.
     * @param source The kind of energy in which the energy will be added.
     * @param amount The amount of energy will be added.
     */
    public void setSpecificAmount(String source, double amount)
    {
        amounts.put(source, amount);
    }

    /**
     * @param source The source of energy.
     * @return The energy of the source.
     */
    public double reSpecificAmount(String source)
    {
        if (!amounts.containsKey(source))
            return 0.0;
        return amounts.get(source);
    }

    /**
     * Removes an amount of energy from the total energy of the ChargingStation.
     * @param energ The amount of energy which is going to be removed.
     */
    public void setTotalEnergy(double energ)
    {
        totalEnergy = totalEnergy - energ;
    }

    /**
     * @return The total energy of this ChargingStation.
     */
    public double reTotalEnergy()
    {
        return totalEnergy;
    }

    /**
     * Sets a charging ratio for the slow charging.
     * @param chargingRatio The charging ratio.
     */
    public void setChargingRatioSlow(double chargingRatio)
    {
        chargingRatioSlow = chargingRatio;
    }

    /**
     * @return The slow charging ratio of this ChargingStation.
     */
    public double reChargingRatioSlow()
    {
        return chargingRatioSlow;
    }

    /**
     * Sets the charging ratio of the fast charging.
     * @param chargingRatio The fast charging ratio.
     */
    public void setChargingRatioFast(double chargingRatio)
    {
        chargingRatioFast = chargingRatio;
    }

    /**
     * @return The fast charging ratio of this ChargingStation.
     */
    public double reChargingRatioFast()
    {
        return chargingRatioFast;
    }

    /**
     * Sets a discharging ratio.
     * @param disChargingRatio The discharging ratio.
     */
    public void setDisChargingRatio(double disChargingRatio)
    {
        this.disChargingRatio = disChargingRatio;
    }

    /**
     * @return The discharging ratio of this ChargingStation.
     */
    public double reDisChargingRatio()
    {
        return disChargingRatio;
    }

    /**
     * Sets the ratio of inductive charging.
     * @param inductiveChargingRatio The ratio of charging during inductive charging.
     */
    public void setInductiveChargingRatio(double inductiveChargingRatio)
    {
        this.inductiveChargingRatio = inductiveChargingRatio;
    }

    /**
     * @return The ratio of charging during inductive charging.
     */
    public double reInductiveRatio()
    {
        return inductiveChargingRatio;
    }

    /**
     * Searches for the EnergySource of the given source.
     * @param source The source for which the EnergySource object is asked.
     * @return An EnergySource object.
     */
    public EnergySource searchEnergySource(String source)
    {
        if ("solar".equals(source))
        {
            for (int i=0; i<sources.size(); i++)
            {
                if (n.get(i) instanceof Solar)
                return n.get(i);
            }
        }
        else if ("wind".equals(source))
        {
            for (int i=0; i<sources.size(); i++)
            {
                if (n.get(i) instanceof Wind)
                return n.get(i);
            }
        }
        else if ("wave".equals(source))
        {
            for (int i=0; i<sources.size(); i++)
            {
                if (n.get(i) instanceof Wave)
                return n.get(i);
            }
        }
        else if ("hydroelectric".equals(source))
        {
            for (int i=0; i<sources.size(); i++)
            {
                if (n.get(i) instanceof HydroElectric)
                return n.get(i);
            }
        }
        else if ("geothermal".equals(source))
        {
            for (int i=0; i<sources.size(); i++)
            {
                if (n.get(i) instanceof Geothermal)
                return n.get(i);
            }
        }
        else if ("nonrenewable".equals(source))
        {
            for (int i=0; i<sources.size(); i++)
            {
                if (n.get(i) instanceof NonRenewable)
                return n.get(i);
            }
        }
        return null;
    }

    /**
     * Sets a price for the energy unit.
     * @param price The price.
     */
    public void setUnitPrice(double price)
    {
        this.unitPrice = price;
    }

    /**
     * @return The price of the energy unit of this ChargingStation.
     */
    public double reUnitPrice()
    {
        return unitPrice;
    }

    /**
     * Sets a price for the energy unit in a DischargingEvent.
     * @param disUnitPrice The price of energy unit.
     */
    public void setDisUnitPrice(double disUnitPrice)
    {
        this.disUnitPrice = disUnitPrice;
    }

    /**
     * @return The price of the energy unit.
     */
    public double reDisUnitPrice()
    {
        return disUnitPrice;
    }

    /**
     * @return The price of a battery exchange.
     */
    public double reExchangePrice()
    {
        return exchangePrice;
    }

    /**
     * Sets the price for a battery exchange.
     * @param price The price the exchange costs.
     */
    public void setExchangePrice(double price)
    {
        exchangePrice = price;
    }

    /**
     * Adjust the management of the ArrayList.
     * @param value The choice of queue handling's. If true the ArrayList is handled
     * automatic by the library. If false the user have to handle the ArrayList.
     */
    public void setAutomaticQueueHandling(boolean value)
    {
        automaticQueueHandling = value;
    }

    /**
     * @return True if the ArrayList is handled automatic by the library.
     * False if the user has to handle the ArrayList.
     */
    public boolean reQueueHandling()
    {
        return automaticQueueHandling;
    }

    /**
     * Sets the space which will be among two storage's updates.
     * @param updateSpace The time space.
     */
    public  void setUpdateSpace(int updateSpace)
    {
        this.updateSpace = updateSpace;
    }

    /**
     * @return The time among each storage update.
     */
    public int reUpdateSpace()
    {
        return updateSpace;
    }

    /**
     * Checks the batteries which are for battery exchange to confirm which of them
     * need charging. After that charges those as the free Charger objects.
     * @param kind The kind of charging the user wants to charge the batteries.
     **/
    public void batteriesCharging(String kind)
    {
        ChargingEvent e;
        ElectricVehicle r;
        for(int i=0; i<batteries.size(); i++)
            if (batteries.get(i).reRemAmount() < batteries.get(i).reBatteryCapacity())
            {
                r = new ElectricVehicle(null, 0);
                r.vehicleJoinBattery(batteries.get(i));
                e = new ChargingEvent(this, r, batteries.get(i).reBatteryCapacity() - batteries.get(i).reRemAmount(), kind);
                if (checkChargers(e.reKind()) != -1)
                    e.execution ();
            }
    }

    /**
     * Returns an array with all the EnergySource objects of the ChargingStation.
     * @return The array with the EnergySource objects of theChargingStation.
     */
    public EnergySource[] reEnergySources()
    {
        EnergySource[] g = new EnergySource[n.size()];
        for(int i = 0; i<n.size(); i++)
            g[i] = n.get(i);
        return g;
    }

    /**
     * Sets the time a battery exchange service lasts.
     * @param time The time the battery exchange lasts.
     */
    public void setTimeofExchange(long time)
    {
        timeOfExchange = time;
    }

    /**
     * @return The time of the battery exchange.
     */
    public long reTimeOfExchange()
    {
        return timeOfExchange;
    }

    /**
     * This function is called every time the library wants to check if an update storage is needed. 
     * The function accordingly to the choice(updateSpace) of the user calculates how many spaces have 
     * passed since the last update storage. Then calls the updateStorage() function n times.
     */
    public void checkForUpdate()
    {
        long diff = getTime() - lastUpdate;
        int count = (int) diff / reUpdateSpace();
        for(int i = 1; i <= count; i++)
            updateStorage();
    }

    /**
     * Update the storage of the ChargingStation with the new amounts of energy 
     * of each source.
     */
    private void updateStorage()
    {
        double counter = 0;
        for (int j=0; j<reEnergySources().length; j++)
        {
            counter = counter + reEnergySources()[j].popAmount();
            if (reEnergySources()[j] instanceof Solar)
            setSpecificAmount("solar", (reSpecificAmount("solar") + searchEnergySource("solar").popAmount()));
				else if (reEnergySources()[j] instanceof Geothermal)
            setSpecificAmount("geothermal", (reSpecificAmount("geothermal") + searchEnergySource("geothermal").popAmount()));
				else if (reEnergySources()[j] instanceof NonRenewable)
            setSpecificAmount("nonrenewable", (reSpecificAmount("nonrenewable") + searchEnergySource("nonrenewable").popAmount()));
				else if (reEnergySources()[j] instanceof HydroElectric)
            setSpecificAmount("hydroelectric", (reSpecificAmount("hydroelectric") + searchEnergySource("hydroelectric").popAmount()));
				else if (reEnergySources()[j] instanceof Wave)
            setSpecificAmount("wave", (reSpecificAmount("wave") + searchEnergySource("wave").popAmount()));
				else if (reEnergySources()[j] instanceof Wind)
            setSpecificAmount("wind", (reSpecificAmount("wind") + searchEnergySource("wind").popAmount()));
        }
        setTotalEnergy(-counter);
        lastUpdate = getTime ();
    }

    /**
     * Distribute the energy across the ElectricVehicle objects. In case some 
     * ElectricVehicle objects arrive together and the ChargingStation's energy
     * is not enough, then this function is called and distribute the energy. 
     * Each ElectricVehicle takes a percentage of the total amount. 
     * The percentage is the energy the ElectricVehicle demands to the total 
     * energy of all the ElectricVehicle objects.
     * @param date The time the ElectricVehicle object arrived.
     */
    public void energyDistribution(long date)
    {
        double counter = 0;
        int counter1 = 0;
        boolean[] g;
        g = new boolean[reChargers().length];
        double max = -1;
        for (int i = 0;i < reChargers().length;i++)
            if (reChargers()[i].reChargingEvent() != null)
            {
                if ((reChargers()[i].reChargingEvent().reDateArrival() == date)&&(!reChargers()[i].reChargingEvent().reCondition().equals("finished")))
                {
                    counter = counter + reChargers()[i].reChargingEvent().reEnergyAmount();
                    g[i] = true;
                    counter1++;
                    if (max < reChargers()[i].reChargingEvent().reStock())
                        max = reChargers()[i].reChargingEvent().reStock();
                }
                else
                    g[i] = false;
            }
        if (counter1 == 1)
            return;
        for(int i = 0;i < g.length;i++)
            if (g[i])
            {
                reChargers()[i].reChargingEvent().setEnergyToBeReceived(max * (reChargers()[i].reChargingEvent().reEnergyAmount()/counter));
                if ("fast".equals(reChargers()[i].reChargingEvent().reKind()))
                    reChargers()[i].reChargingEvent().setChargingTime((int) (reChargers()[i].reChargingEvent().reEnergyToBeReceived()/reChargingRatioFast()) + 1);
                else
                    reChargers()[i].reChargingEvent().setChargingTime((int) ((reChargers()[i].reChargingEvent().reEnergyToBeReceived() + 1)/reChargingRatioSlow()));
            }
    }

    /**
     * Calculates the cost of a charging.
     * @param w The ChargingEvent that executed.
     * @return The cost of the charging.
     */
    public double calculatePrice(ChargingEvent w)
    {
        if (!"exchange".equals(w.reKind()))
            return w.reEnergyToBeReceived()*reUnitPrice();
        else
            return reExchangePrice();
    }

    /**
     * Calculates the cost of a charging.
     * @param w The ParkingEvent that executed.
     * @return The cost of the charging.
     */
    public double calculatePrice(ParkingEvent w)
    {
        return w.reEnergyToBeReceived()*reUnitPrice();
    }

    /**
     * Calculates the amount of time a Driver has to wait until his ElectricVehicle
     * can be charged. This calculation happens in case a Vehicle adds has to 
     * be added in the ArrayList.
     * @param y The ChargingEvent that has to wait.
     * @return The waiting time.
     */
    public long calWaitingTime(ChargingEvent y)
    {
        long[] counter1 = new long[reChargers().length];
        long[] counter2 = new long[reChargers().length];
        long r = getTime();
        int min = 10000000;
        if (!"exchange".equals (y.reKind ()))
            for (int i = 0; i < reChargers ().length; i++) {
                if (y.reKind () == reChargers()[i].reKind ()) {
                    long diff = r - reChargers()[i].reChargingEvent ().reStartTime();
                    if (min > reChargers()[i].reChargingEvent ().reChargingTime () - diff)
                        min = i;
                    counter1 [i] = reChargers()[i].reChargingEvent ().reChargingTime () - diff;
                }
            }
        else
            for (int i = 0; i<reExchangeHandlers().length; i++)
            {
                long diff = r - reExchangeHandlers()[i].reChargingEvent().reStartTime();
                if (min > reExchangeHandlers()[i].reChargingEvent().reChargingTime() - diff)
                    min = i;
                counter2[i] = reExchangeHandlers()[i].reChargingEvent().reChargingTime() - diff;
            }
        if ("slow".equals(y.reKind()))
        {
            WaitList o = reSlow();
            for (int i = 0;i < o.reSize() ;i++)
            {
                counter1[min] = counter1[min] + o.peek(i).reChargingTime();
                for(int j=0; j<reChargers().length; j++)
                    if ((counter1[j]<counter1[min])&&(counter1[j]!=0))
                        min = j;
            }
            return counter1[min];
        }
        if ("fast".equals(y.reKind()))
        {
            WaitList o = reFast();
            for(int i = 0; i < o.reSize() ;i++)
            {
                counter1[min] = counter1[min] + o.peek(i).reChargingTime();
                for(int j=0; j<reChargers().length; j++)
                    if ((counter1[j]<counter1[min])&&(counter1[j]!=0))
                        min = j;
            }
            return counter1[min];
        }
        if ("exchange".equals(y.reKind()))
        {
            WaitList o = reExchange();
            for(int i = 0; i < o.reSize();i++)
            {
                counter2[min] = counter2[min] + o.peek(i).reChargingTime();
                for(int j=0; j<reChargers().length; j++)
                    if ((counter2[j]<counter2[min])&&(counter2[j]!=0))
                        min = j;
            }
            return counter2[min];
        }
        return 0;
    }

    /**
     * @return The time the ElectricVehicle has to wait.
     */
    public long calDisWaitingTime()
    {
        long[] counter1 = new long[reDisChargers().length];
        long r = getTime();
        int min = 10000000;
        for (int i = 0; i<reDisChargers().length; i++)
        {
            long diff = r - reDisChargers()[i].reDisChargingEvent().reStartTime();
            if (min > reDisChargers()[i].reDisChargingEvent().reDisChargingTime() - diff)
                min = i;
            counter1[i] = reDisChargers()[i].reDisChargingEvent().reDisChargingTime() - diff;
        }
        WaitList o = reDischarging();
        for (int i = 0; i < o.rSize(); i++)
        {
            counter1[min] = counter1[min] + o.get(i).reDisChargingTime();
            for(int j=0; j<reDisChargers().length; j++)
                if ((counter1[j] < counter1[min])&&(counter1[j]!=0))
                    min = j;
        }
        return counter1[min];
    }
}