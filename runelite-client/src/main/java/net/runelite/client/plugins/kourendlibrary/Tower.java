package net.runelite.client.plugins.kourendlibrary;

import javax.swing.*;
import java.awt.*;

public class Tower implements Comparable
{
    private int numBooks;
    private TowerDirection direction;
    private boolean isTarget;
    private boolean isMost2;

    //Needs to be public so view can retain reference to object as it updates
    public JLabel label;

    public Tower(TowerDirection dir)
    {
        numBooks = 0;
        direction = dir;
        isTarget = false;
        isMost2 = false;

        label = new JLabel(getLabelDirectionFromEnum(direction) + numBooks);
    }

    public void reset()
    {
        isMost2 = false;
        isTarget = false;
        numBooks = 0;
        label.setText(getLabelDirectionFromEnum(direction) + numBooks);
        refreshLabel();
    }

    private String getLabelDirectionFromEnum(TowerDirection dir)
    {
        switch (dir)
        {
            case NE:
                return "Northeast: ";
            case NW:
                return "Northwest: ";
            case SW:
                return "Southwest: ";
            case MID:
                return "Middle: ";
            default:
                return "";
        }
    }

    public TowerDirection getDirection()
    {
        return direction;
    }

    public void addBook()
    {
        numBooks++;
        label.setText(getLabelDirectionFromEnum(direction) + numBooks);
    }

    public void setTarget(boolean targ)
    {
        isTarget = targ;
        refreshLabel();
    }

    public void setMost2(boolean most2)
    {
        isMost2 = most2;
        refreshLabel();
    }

    private void refreshLabel()
    {
        if (numBooks == 0)
        {
            label.setForeground(Color.white);
        }
        else if(isTarget)
        {
            label.setForeground(Color.green);
        }
        else if(isMost2)
        {
            label.setForeground(Color.yellow);
        }
        else
        {
            label.setForeground(Color.white);
        }
    }

    private boolean isMid()
    {
        return (direction == TowerDirection.MID);
    }

    @Override
    public int compareTo(Object o) {
        try {
            Tower comp = (Tower)o;
            if(this.isMid())
            {
                return -1;
            }
            else if (comp.isMid())
            {
                return 1;
            }
            else if(this.isTarget)
            {
                return 1;
            }
            else if(comp.isTarget)
            {
                return -1;
            }
            else if(comp.numBooks > this.numBooks)
            {
                return -1;
            }
            else if(comp.numBooks == this.numBooks)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        catch(Exception e)
        {
            return 0;
        }

    }
}