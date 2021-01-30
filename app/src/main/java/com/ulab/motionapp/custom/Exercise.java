package com.ulab.motionapp.custom;

import android.util.Log;

public class Exercise{

    public enum exerciseName{PUTTING_BASE, SPARE1, SPARE2, SPARE3}

    public interface ExerciseListener {

        void onImpactTrajectoryChanged(String trajectory);
        void onImpactPositionChanged(Float position []);
        void onImpactAccelerationChanged(String acceleration);
        void onImpactSpeedChanged(Float speed);
        void onRegularityChanged(String regularity []);
    }

    private ExerciseListener listener;

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
    public Float [] pitchMeanStdDev;
    public Float [] yawMeanStdDev;
    public boolean ringBuffer;
    public RingBuffer [] buffer;
    public RingBuffer.RingBufferListener[] bufferListener;
    public int templateResume;

    public Exercise(exerciseName name, ExerciseListener listener)
    {
        //All this initialization process should be done by reading the appropriate BDD contents,
        //Hard-coded here for example purpose
        this.listener = listener;
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
            this.buffer = new RingBuffer[getXpTotalNbr()];

            for (int i = 0; i < this.nbrXpN; i++)
            {
                this.buffer[i] = new RingBuffer(50, getXpNmask(i));
                this.buffer[i].setListener(new RingBuffer.RingBufferListener() {
                    @Override
                    public void onBufferReady(int impactCount) {
                        //Log.d("Exercise Listener", "onBufferReady " + impactCount );
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
        this.yawMeanStdDev = new Float [] {0f,0f,0f,0f,0f,0f};
        this.pitchMeanStdDev = new Float [] {0f,0f,0f,0f,0f,0f};
    }

    private void applyRules(int impactCount)
    {
        //RULE 1
        this.rules[0].analysisImpactTrajectory(this.buffer[0].yawTab, this.buffer[0].pitchTab);
        this.yawMeanStdDev[impactCount-1] = this.rules[0].output_yawMeanStdDev;
        this.pitchMeanStdDev[impactCount-1] = this.rules[0].output_pitchMeanStdDev;
        Log.d("Exercise", "RULE 1 " + this.rules[0].output_impactTrajectory );
        if (listener != null){
            String trajectory = this.rules[0].output_impactTrajectory;
            listener.onImpactTrajectoryChanged(trajectory);
        }

        //RULE 2
        this.rules[1].analysisImpactPosition(this.buffer[0].yawTab, this.buffer[0].pitchTab);
        Log.d("Exercise", "RULE 2 " + this.rules[1].output_impactPosition[0] + " " + this.rules[1].output_impactPosition[1] );
        if (listener != null){
            Float position [] = new Float [2];
            position[0] = this.rules[1].output_impactPosition[0];
            position[1] = this.rules[1].output_impactPosition[1];
            listener.onImpactPositionChanged(position);
        }

        //RULE 3
        this.rules[2].analysisImpactAcceleration(this.buffer[0].accZTab);
        Log.d("Exercise", "RULE 3 " +  this.rules[2].output_acceleration);
        if (listener != null){
            String acceleration = this.rules[2].output_acceleration;
            listener.onImpactAccelerationChanged(acceleration);
        }
        //RULE 4
        this.rules[3].analysisCorrelationSpeed(this.buffer[0].accZTab);
        Log.d("Exercise", "RULE 4 " + this.rules[3].output_speed);
        if (listener != null){
            Float speed = this.rules[3].output_speed;
            listener.onImpactSpeedChanged(speed);
        }

        if (impactCount == 5)
        {
            //RULE 5
            this.rules[4].analysisRegularity(5, this.yawMeanStdDev, this.pitchMeanStdDev);
            Log.d("Exercise", "RULE 5 " +  this.rules[4].output_regularity[0] + " " + this.rules[4].output_regularity[1]);

            if (listener != null){
                String regularity [] = new String [2];
                regularity[0] = this.rules[4].output_regularity[0];
                regularity[1] = this.rules[4].output_regularity[1];
                listener.onRegularityChanged(regularity);
            }
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
