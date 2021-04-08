package it.polimi.ingsw.model.match.player.personalBoard.warehouse;

import it.polimi.ingsw.model.essentials.PhysicalResource;
import it.polimi.ingsw.model.essentials.ResType;
import it.polimi.ingsw.model.exceptions.InvalidOperationException;
import it.polimi.ingsw.model.exceptions.NegativeQuantityException;
import it.polimi.ingsw.model.exceptions.NotEnoughResourcesException;
import it.polimi.ingsw.model.exceptions.ShelfInsertException;

import java.util.*;

public interface Warehouse
{
    // returns the resource extracted from the chosen shelf.
    PhysicalResource take(int shelf, int numresources) throws NotEnoughResourcesException, NegativeQuantityException;

    //inserts the resource into the chosen shelf.
    boolean moveInShelf(PhysicalResource res, int shelf) throws ShelfInsertException, NegativeQuantityException, InvalidOperationException;

    //receives the resource to remove from the marketBuffer, and does it one by one.
    boolean cleanMarketBuffer(PhysicalResource res) throws NegativeQuantityException;

    //returns the current warehouse mapped state.
    Map<ResType, Integer> getWarehouse();

    //returns the perfect disposition of the warehouse, with empty space for the empty shelves.
    List<PhysicalResource> getWarehouseDisposition();

    //switches two shelves' content.
    boolean switchShelf(int shelf1, int shelf2) throws ShelfInsertException, NegativeQuantityException, InvalidOperationException, NotEnoughResourcesException;

    //returns the marketBuffer content.
    List<PhysicalResource> getBuffer();

    //adds the resources split in single quantity to the marketBuffer.
    boolean marketDraw(PhysicalResource res) throws NegativeQuantityException;

    //clears the marketBuffer and returns his previous size.
    int discardRemains();

    //returns the quantity of resources of the requested type.
    int getNumberOf(ResType type);

    //returns the size of the specified shelf: 1 to 3 for concrete, 1 to 5 for extra.
    int getShelfSize(int shelf);
}
