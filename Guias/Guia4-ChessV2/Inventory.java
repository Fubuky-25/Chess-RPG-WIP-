import java.util.ArrayList;
import java.util.List;

/** Mochila del jugador: items conseguidos y todavia no equipados. */
public class Inventory
{
    private List<Item> items = new ArrayList<Item>();

    public void add(Item item)    { if (item != null) items.add(item); }
    public void remove(Item item) { items.remove(item); }
    public List<Item> getItems()  { return items; }
    public int size()             { return items.size(); }
    public void clear()           { items.clear(); }
}
