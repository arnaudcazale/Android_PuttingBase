package com.ulab.motionapp.custom;

import android.util.Log;

public class Exercise{

    public enum exerciseName{PUTTING_BASE, SPARE1, SPARE2, SPARE3}

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
    public Rule [] rules;
    public boolean ringBuffer;
    public RingBuffer [] buffer;
    public RingBuffer.RingBufferListener[] bufferListener;
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
                this.buffer[i] = new RingBuffer(50, getXpNmask(i), timeCalib);
                this.buffer[i].setListener(new RingBuffer.RingBufferListener() {
                    @Override
                    public void onBufferReady(int impactCount) {
                        Log.d("Exercise Listener", "onBufferReady " + impactCount );
                        applyRules(impactCount);
                    }
                });
            }
        }
    }

    private void rulesConfig()
    {
        //Activate rules for PUTTING_BASE exercise
        this.nbrRules = 5;
        this.rules = new Rule[this.nbrRules];
        this.rules[0] = new Rule(Rule.ruleName.IMPACT_TRAJECTORY);
        this.rules[1] = new Rule(Rule.ruleName.IMPACT_POSITION);
        this.rules[2] = new Rule(Rule.ruleName.IMPACT_ACCELERATION);
        this.rules[3] = new Rule(Rule.ruleName.SPEED);
        this.rules[4] = new Rule(Rule.ruleName.REGULARITY);
    }

    private void applyRules(int impactCount)
    {
        if(impactCount < 5)
        {
            String string = this.rules[0].analysisImpactTrajectory(this.buffer[0].yawTab, this.buffer[0].pitchTab);
            Log.d("Exercise", "applyRules " + string );

            //TODO implement other rules
            //this.rules[1].analysisImpactPosition();
            //this.rules[2].analysisImpactAcceleration();
            //this.rules[3].analysisCorrelationSpeed();

        }else if (impactCount == 5)
        {
            //TODO implement regularity
            //this.rules[4].analysisRegularity();
        }
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
