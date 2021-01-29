package com.ulab.motionapp.custom;

import android.util.Log;

public class Rule {

    public enum ruleName {IMPACT_TRAJECTORY, IMPACT_POSITION, IMPACT_ACCELERATION, SPEED, REGULARITY};

    public ruleName name;

    public Rule(ruleName name)
    {
        this.name = name;
    }

    public void analysisImpactTrajectory()
    {
        Log.d("Rule Class", "analysisImpactTrajectory ");
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
