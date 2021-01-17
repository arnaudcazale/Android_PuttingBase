package com.ulab.motionapp.custom;

import android.util.Log;

public class Exercise {

    public enum exerciseName{PUTTING_BASE, TEST};

    public String sport;
    public exerciseName name;
    public int nbrXpN;
    public int nbrXpS;
    public int nbrXpI;
    public int nbrXpA;
    public int nbrXpR;
    public String [] nameXpN;
    public String [] nameXpS;
    public String [] nameXpI;
    public String [] nameXpA;
    public String [] nameXpR;
    public XpertNode [] XpN;
    public XpertNode [] XpS;
    public XpertNode [] XpI;
    public XpertNode [] XpA;
    public XpertNode [] XpR;
    public int delayCalib;
    public int timeCalib;
    public int positionCalib;
    public int nbrRules;
    public boolean ringBuffer;
    public int templateResume;

    public Exercise(exerciseName name)
    {
        //All this initialization process should be done by reading the appropriate BDD contents,
        //Hard-coded here for example purpose
        this.name = name;
        XpertNodeConfig();
    }

    private void XpertNodeConfig()
    {

        switch(this.name)
        {
            case PUTTING_BASE:

                nbrXpN = 1;
                nbrXpS = 0;
                nbrXpI = 0;
                nbrXpA = 0;
                nbrXpR = 0;

                if(nbrXpN > 0)
                {
                    //Create array for Xpn Name, and instantiate objects for each XpN dedicated to the exercise
                    nameXpN = new String [nbrXpN];
                    XpN = new XpertNode[nbrXpN];

                    for (int i = 0; i < nbrXpN; i++)
                    {
                        XpN[i] = new XpertNode(XpertNode.XpertNodeName.XpN);
                        nameXpN[i] = "XpN" + (i+1) ;
                        Log.d("XpertNodeConfig", "nameXpN[i] " + nameXpN[i] );
                        Log.d("XpertNodeConfig", "XpN " + XpN[i].sensorMask );
                    }
                }
        }
    }

    public int getXpTotalNbr()
    {
        return nbrXpN + nbrXpS +nbrXpI +nbrXpA +nbrXpR;
    }
    public int getXpNmask(int XpnIdx)
    {
        return XpN[XpnIdx].sensorMask;
    }
}
