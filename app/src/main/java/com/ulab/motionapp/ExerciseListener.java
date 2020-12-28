package com.ulab.motionapp;

public interface ExerciseListener {
    void onImpactTrajectoryChange(String trajectory);
    void onImpactPositionChange(Float yaw, Float pitch);
    void onImpactAccelerationChange(String acceleration);
    void onSpeedChange(Float speed);
    void onRegularityChange(String yaw, String pitch);
}
