package com.ulab.motionapp.custom;

import android.util.Log;

import static java.lang.Math.abs;

public class Rule {

    public enum ruleName {IMPACT_TRAJECTORY, IMPACT_POSITION, IMPACT_ACCELERATION, SPEED, REGULARITY};

    public ruleName name;

    public String output_impactTrajectory;
    public Float [] output_impactPosition;
    public String output_acceleration;
    public Float output_speed;
    public String [] output_regularity;
    public Float output_pitchMeanStdDev;
    public Float output_yawMeanStdDev;

    public Rule(ruleName name)
    {
        this.name = name;
        this.output_impactTrajectory = "";
        this.output_impactPosition = new Float [2];
        this.output_acceleration = "";
        this.output_speed = 0f;
        this.output_regularity = new String[2];
        this.output_pitchMeanStdDev = 0f;
        this.output_yawMeanStdDev = 0f;
    }

    public void analysisImpactTrajectory(Float [] yawTab, Float [] pitchTab)
    {
        boolean DbIsPositive = false;
        boolean DaIsPositive = false;
        String Pi = "";

        Float [] bfRollTab  = new Float [3];
        Float [] afRollTab  = new Float [3];
        Float [] bfPitchTab  = new Float [3];
        Float [] afPitchTab  = new Float [3];
        Float [] bfYawTab  = new Float [3];
        Float [] afYawTab  = new Float [3];
        Float [] pitchStdDev = new Float [6];
        Float [] yawStdDev = new Float [6];


        //bf = index de -3 à -1 (before impact)
        bfYawTab[0]  = yawTab[22];
        bfYawTab[1]  = yawTab[23];
        bfYawTab[2]  = yawTab[24];

        bfPitchTab[0]  = pitchTab[22];
        bfPitchTab[1]  = pitchTab[23];
        bfPitchTab[2]  = pitchTab[24];

        //bf = index de +1 à +3 (after impact)
        afYawTab[0]  = yawTab[25];
        afYawTab[1]  = yawTab[26];
        afYawTab[2]  = yawTab[27];

        afPitchTab[0]  = pitchTab [22];
        afPitchTab[1]  = pitchTab [23];
        afPitchTab[2]  = pitchTab [24];

        //Standard deviation calculations
        Float meanYaw = 0.0f, meanPitch = 0f;

        for( int i = 0; i < 3; i++)
        {
            meanYaw += bfYawTab[i] + afYawTab[i];
            meanPitch += bfPitchTab[i] + afPitchTab[i];
        }

        meanYaw /= 6;
        meanPitch /= 6;

        //Log.e("Exercise", "meanYaw "  +  meanYaw);
        //Log.e("Exercise", "meanPitch "  +  meanPitch);

        yawStdDev[0] = abs(bfYawTab[0] - meanYaw);
        yawStdDev[1] = abs(bfYawTab[1] - meanYaw);
        yawStdDev[2] = abs(bfYawTab[2] - meanYaw);
        yawStdDev[3] = abs(afYawTab[0] - meanYaw);
        yawStdDev[4] = abs(afYawTab[1] - meanYaw);
        yawStdDev[5] = abs(afYawTab[2] - meanYaw);

        //Log.e("Exercise", "yawStdDev "  +  Arrays.toString(yawStdDev[currentSerie-1]));

        pitchStdDev[0] = abs(bfPitchTab[0] - meanPitch);
        pitchStdDev[1] = abs(bfPitchTab[1] - meanPitch);
        pitchStdDev[2] = abs(bfPitchTab[2] - meanPitch);
        pitchStdDev[3] = abs(afPitchTab[0] - meanPitch);
        pitchStdDev[4] = abs(afPitchTab[1] - meanPitch);
        pitchStdDev[5] = abs(afPitchTab[2] - meanPitch);

        //Log.e("Exercise", "pitchStdDev "  +  Arrays.toString(pitchStdDev[currentSerie-1]));

        for( int i = 0; i < 6; i++)
        {
            output_yawMeanStdDev +=  yawStdDev[i];
            output_pitchMeanStdDev +=  pitchStdDev[i];
        }

        this.output_yawMeanStdDev   /= 6;
        this.output_pitchMeanStdDev /= 6;

        //Log.e("Exercise", "bfYawTab "  +  Arrays.toString(bfYawTab[currentSerie-1]));
        //Log.e("Exercise", "afYawTab "  +  Arrays.toString(afYawTab[currentSerie-1]));
        //Log.e("Exercise", "yawMeanStdDev " + yawMeanStdDev[currentSerie-1]);
        //Log.e("Exercise", "pitchMeanStdDev "  + pitchMeanStdDev[currentSerie-1]);

        //Rule 1:
        for(int i = 0; i < 3; i++) {
            if(bfYawTab[i] > 0) DbIsPositive = true;
            if(afYawTab[i] > 0) DaIsPositive = true;
        }

        //Rule 2:
        //If Da && Db == true
        if( DbIsPositive && DaIsPositive){
            Pi = "ext_ext";
        }
        //If Da && Db == false
        if( !DbIsPositive && !DaIsPositive){
            Pi = "int_int";
        }
        //If Da == true && Db == false
        if( DbIsPositive && !DaIsPositive){
            Pi = "int_ext";
        }
        //If Da == false && Db ==  true
        if( !DbIsPositive && DaIsPositive){
            Pi = "ext_int";
        }

        this.output_impactTrajectory = Pi;
    }

    public void analysisImpactPosition(Float [] yawTab, Float [] pitchTab)
    {
        Float Pyaw;
        Float Ppitch;

        //yaw/pitch = mean des index de -1 à +1 (before and after impact)
        Pyaw = (yawTab[24] + yawTab[25]) / 2;
        Ppitch = (pitchTab[24] + pitchTab[25]) / 2;

        this.output_impactPosition[0] = Pyaw;
        this.output_impactPosition[1] = Ppitch;
    }

    public void analysisImpactAcceleration(Float [] accZTab)
    {
        String accel = "";
        Float [] AccelTab = new Float [10];

        for( int i = 0; i < 10; i++)
        {
            AccelTab[i] = accZTab[24-i];
        }

        //Find max
        Float maxVal = AccelTab[0];
        int idxMax = 0;
        for(int i = 1; i < AccelTab.length; i++)
        {
            if(AccelTab[i] > maxVal)
            {
                maxVal = AccelTab[i];
                idxMax = i;
            }
        }

        if(idxMax <= 1)
        {
            accel = "positive";
        }else{
            accel = "negative";
        }

        this.output_acceleration =  accel;
    }

    public void analysisCorrelationSpeed(Float [] accZTab)
    {
        Float mean;
        Float m = 0.375f;
        Float speedy;
        Float c;

        mean = ( accZTab[24] + accZTab[25] ) / 2;
        speedy = mean*0.02f; //Speed in m/s
        c = 0.5f*m*(speedy*speedy)*1000;

        output_speed = c;
    }

    public void analysisRegularity(int nbrSeries, Float [] yawMeanStdDev, Float [] pitchMeanStdDev)
    {
        String returnStringYaw ="", returnStringPitch = "";
        Float meanPitchStdDev = 0f, meanYawStdDev = 0f;
        String [] returnArray = new String [2];

        for( int i = 0; i < nbrSeries; i++)
        {
            meanYawStdDev +=   yawMeanStdDev[i];
            meanPitchStdDev += pitchMeanStdDev[i];
        }

        meanYawStdDev /= nbrSeries;
        meanPitchStdDev /= nbrSeries;

        //regularity on yaw
        if( (meanYawStdDev <= 0.5) ) {
            returnStringYaw = "Très bon";
        }else if( (meanYawStdDev > 0.5) && (meanYawStdDev <= 0.8) ){
            returnStringYaw = "Bon";
        }else if( (meanYawStdDev > 0.8) && (meanYawStdDev <= 1.3) ){
            returnStringYaw = "Moyen";
        }else if( (meanYawStdDev > 1.3) && (meanYawStdDev <= 1.8) ){
            returnStringYaw = "Faible";
        }else if( (meanYawStdDev > 1.8) ){
            returnStringYaw = "Très faible";
        }

        //regularity on pitch
        if( (meanPitchStdDev <= 0.5) ) {
            returnStringPitch = "Très bon";
        }else if( (meanPitchStdDev > 0.5) && (meanPitchStdDev <= 0.8) ){
            returnStringPitch = "Bon";
        }else if( (meanPitchStdDev > 0.8) && (meanPitchStdDev <= 1.3) ){
            returnStringPitch = "Moyen";
        }else if( (meanPitchStdDev > 1.3) && (meanPitchStdDev <= 1.8) ){
            returnStringPitch = "Faible";
        }else if( (meanPitchStdDev > 1.8) ){
            returnStringPitch = "Très faible";
        }

        this.output_regularity[0] = returnStringYaw;
        this.output_regularity[1] = returnStringPitch;

    }

}
