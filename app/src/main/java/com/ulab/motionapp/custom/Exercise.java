package com.ulab.motionapp.custom;

import android.util.Log;

public class Exercise {

    public enum exerciseName{PUTTING_BASE, SPARE1, SPARE2, SPARE3}
    public enum ruleName{IMPACT_TRAJECTORY, IMPACT_POSITION, IMPACT_ACCELERATION, SPEED, REGULARITY}

    public String sport;
    public exerciseName name;
    public int reps;           //nombre de répétitions prévues dans l'exercice
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
    public ruleName [] rules;
    public boolean ringBuffer;
    public RingBuffer [] buffer;
    public int templateResume;

    public Exercise(exerciseName name)
    {
        //All this initialization process should be done by reading the appropriate BDD contents,
        //Hard-coded here for example purpose

        this.name = name;
        switch(this.name) {
            case PUTTING_BASE:
                exerciseConfig();
                XpertNodeConfig();
                bufferConfig();
                rulesConfig();
                break;

            case SPARE1:
                break;

            case SPARE2:
                break;

            case SPARE3:
                break;
        }
    }

    private void exerciseConfig()
    {
        this.reps = 5;
    }
    private void XpertNodeConfig()
    {
        this.nbrXpN = 1;
        this.nbrXpS = 0;
        this.nbrXpI = 0;
        this.nbrXpA = 0;
        this.nbrXpR = 0;

        if(this.nbrXpN > 0)
        {
            //Create array for Xpn Name, and instantiate objects for each XpN dedicated to the exercise
            this.nameXpN = new String [this.nbrXpN];
            this.XpN = new XpertNode[this.nbrXpN];

            for (int i = 0; i < this.nbrXpN; i++)
            {
                this.XpN[i] = new XpertNode(XpertNode.XpertNodeName.XpN);
                this.nameXpN[i] = "XpN" + (i+1) ;
                Log.d("XpertNodeConfig", "nameXpN[i] " + this.nameXpN[i] );
                Log.d("XpertNodeConfig", "XpN " + this.XpN[i].sensorMask );
            }
        }
    }

    private void bufferConfig()
    {
        //Activate exercise parameters ringBuffer & calibration
        this.ringBuffer = true;
        if(this.ringBuffer)
        {
            this.timeCalib = 3;

            this.buffer = new RingBuffer[getXpTotalNbr()];
            for (int i = 0; i < this.nbrXpN; i++)
            {
                this.buffer[i] = new RingBuffer(50, getXpNmask(i), timeCalib );
            }
        }
    }

    private void rulesConfig()
    {
        //Activate rules for PUTTING_BASE exercise
        this.nbrRules = 5;
        this.rules = new ruleName[nbrRules];
        this.rules[0] = ruleName.IMPACT_TRAJECTORY;
        this.rules[0] = ruleName.IMPACT_POSITION;
        this.rules[0] = ruleName.IMPACT_ACCELERATION;
        this.rules[0] = ruleName.SPEED;
        this.rules[0] = ruleName.REGULARITY;
    }

    public int getXpTotalNbr()
    {
        return ( this.nbrXpN + this.nbrXpS + this.nbrXpI + this.nbrXpA + this.nbrXpR );
    }
    public int getXpNmask(int XpnIdx)
    {
        return this.XpN[XpnIdx].sensorMask;
    }
}
