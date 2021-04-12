package it.polimi.ingsw.model.match.player.personalBoard.faithPath;

import it.polimi.ingsw.model.exceptions.FaithPathCreationException;
import it.polimi.ingsw.model.match.Comunicator;

public class Cell
{
    private int winPoints;
    private int vaticanReportSection;

    /**
     * The constructor initializes the single basic Cell.
     * @param winPoints             sets the win points of the cell
     * @param vaticanReportSection  defines in which report section the Cell is (=0 if it's not in a section).
     * @throws FaithPathCreationException when a negative quantity of winPoints is received.
     */
    public Cell(int winPoints, int vaticanReportSection) throws FaithPathCreationException
    {
        if(winPoints <0)
            throw new FaithPathCreationException("Error in faith path creation: negative winPoints on a Cell.");
        this.winPoints = winPoints;
        this.vaticanReportSection = vaticanReportSection;
    }

    /**
     * This method specifies, in a Multiplayer match, that this Cell is not a VaticanReportCell.
     * @param comunicator has no use in this method
     * @return            false
     * @see VaticanReportCell
     */
    public boolean vaticanReport(Comunicator comunicator) {return false;}

    /**
     * This method specifies, in a Singleplayer match, that this Cell is not a VaticanReportCell.
     * @return            false
     * @see VaticanReportCell
     */
    public boolean singleVaticanReport() { return false; }

    /** @return the vatican report section associated to this Cell */
    public int getReportSection() { return vaticanReportSection; }

    /** @return the winPoints value of the Cell */
    public int getWinPoints() { return winPoints; }
}
