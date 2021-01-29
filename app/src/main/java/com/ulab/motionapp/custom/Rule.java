package com.ulab.motionapp.custom;

import android.util.Log;

import static java.lang.Math.abs;

public class Rule {

    public enum ruleName {IMPACT_TRAJECTORY, IMPACT_POSITION, IMPACT_ACCELERATION, SPEED, REGULARITY};

    public ruleName name;

    public Rule(ruleName name)
    {
        this.name = name;
    }

    public String analysisImpactTrajectory(Float [] yawTab, Float [] pitchTab)
    {
        Log.d("Rule Class", "analysisImpactTrajectory ");
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

        Float yawMeanStdDev = 0f;
        Float pitchMeanStdDev = 0f;

        for( int i = 0; i < 6; i++)
        {
            yawMeanStdDev +=  yawStdDev[i];
            pitchMeanStdDev +=  pitchStdDev[i];
        }

        yawMeanStdDev   /= 6;
        pitchMeanStdDev /= 6;

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

        //reset values
        DbIsPositive = false;
        DaIsPositive = false;


        return Pi;
    }

    public void analysisImpactPosition()
    {
        Log.d("Rule Class", "analysisImpactPosition ");
    }

    public void analysisImpactAcceleration()
    {
        Log.d("Rule Class", "analysisImpactAcceleration ");
    }

    public void analysisCorrelationSpeed()
    {
        Log.d("Rule Class", "analysisCorrelationSpeed ");
    }

    public void analysisRegularity()
    {
        Log.d("Rule Class", "analysisRegularity ");
    }

}
