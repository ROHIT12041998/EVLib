package Station;

import Events.DisChargingEvent;
import Events.ChargingEvent;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class WaitList
{
    private ArrayList<ChargingEvent> list1;
    private ArrayList<DisChargingEvent> list2;
    private int id;
    private static AtomicInteger idGenerator = new AtomicInteger(0);

    public WaitList(String n)
    {
        this.id = idGenerator.incrementAndGet();
        if ("charging".equals(n))
            list1 = new ArrayList<>();
        else if ("discharging".equals(n))
            list2 = new ArrayList<>();
    }

    /**
     * Returns the ChargingEvent in the given place.
     * @param index The place of the ChargingEvent in the list.
     * @return The ChargingEvent object.
     */
    public ChargingEvent peek(int index)
    {
        return list1.get(index);
    }

    /**
     * Returns the DisChargingEvent in the given place.
     * @param index The place of the DisChargingEvent in the list.
     * @return The DisChargingEvent object.
     */
    public DisChargingEvent get(int index)
    {
        return list2.get(index);
    }

    /**
     * Inserts a ChargingEvent in the list.
     * @param p The ChargingEvent to be inserted.
     */
    public void insertElement(ChargingEvent p)
    {
        list1.add(p);
    }

    /**
     * Deletes a ChargingEvent from the list.
     * @param m The ChargingEvent to be deleted.
     * @return True if the deletion was successfull, false if it was not.
     */
    public boolean deleteElement(ChargingEvent m)
    {
        return list1.remove(m);
    }

    /**
     * Returns the first ChargingEvent of the list.
     * @return The first ChargingEvent.
     */
    public ChargingEvent takeFirst()
    {
        return list1.get(0);
    }

    /**
     * Removes the first ChargingEvent.
     * @return True if the deletion was successfull, false if it was not.
     */
    public ChargingEvent removeFirst()
    {
        ChargingEvent e = list1.get(0);
        list1.remove(0);
        return e;
    }

    /**
     * Returns the size of the list for the ChargingEvent.
     * @return The size of ChargingEvent list.
     */
    public int size()
    {
        return list1.size();
    }

    /**
     * Inserts a DisChargingEvent to the list.
     * @param p The DisChargingEvent to be inserted.
     */
    public void insertElement(DisChargingEvent p)
    {
        list2.add(p);
    }

    /**
     * Deletes a DisChargingEvent from the list.
     * @param m The DisChargingEvent to be deleted.
     * @return True if it was successfull, false if it was not.
     */
    public boolean deleteElement(DisChargingEvent m)
    {
        return list2.remove(m);
    }

    /**
     * Returns the first DisChargingEvent of the list.
     * @return The first DisChargingEvent of the list.
     */
    public DisChargingEvent getFirst()
    {
        return list2.get(0);
    }

    /**
     * Removes the first DisChargingEvent of the list.
     * @return True if it was successfull, false if it was not.
     */
    public DisChargingEvent moveFirst()
    {
        DisChargingEvent e = list2.get(0);
        list2.remove(0);
        return e;
    }

    /**
     * Returns the size of the DisChargingEvent list.
     * @return The size of the DisChargingEvent list.
     */
    public int getSize()
    {
        return list2.size();
    }

    /**
     * Removes the ChargingEvent in the given position.
     * @param index The position of the ChargingEvent.
     * @return True if the deletion was successfull, false if it was not.
     */
    public ChargingEvent removeChargingEvent(int index)
    {
        ChargingEvent e = list1.get(index);
        list1.remove(index);
        return e;
    }

    /**
     * Removes the DisChargingEvent int he given position.
     * @param index The position of the DisChargingEvent.
     * @return True if the deletion was successfull, false if it was not.
     */
    public DisChargingEvent removeDisChargingEvent(int index)
    {
        DisChargingEvent e = list2.get(index);
        list2.remove(index);
        return e;
    }

    /**
     * @return The id of this WaitingList.
     */
    public int getId()
    {
        return id;
    }
}