package com.ulab.motionapp;

import android.util.Log;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;

import static java.lang.Math.abs;


public class Exercise {

    private ExerciseListener listener;

    private int nbrSeries;
    private int currentSerie;
    public boolean isCalibrating;
    public boolean bufferImpact;
    private int calCount;
    private int bufferEulerCount;
    private int bufferRawCount;
    private boolean bufferEulerReady;
    private boolean bufferRawReady;
    private int bufferSize;

    public Float rollCalibration;
    public Float pitchCalibration;
    public Float yawCalibration;

    public CircularFifoQueue<Float> rollBuffer;
    public CircularFifoQueue<Float> pitchBuffer;
    public CircularFifoQueue<Float> yawBuffer;
    public CircularFifoQueue<Float> accZBuffer;

    public Float [][] rollTab;
    public Float [][] pitchTab ;
    public Float [][] yawTab;
    public Float [][] accZTab;

    public Float [] pitchMeanStdDev;
    public Float [] yawMeanStdDev;

    public String [] impactTrajectory;
    public Float [][] impactPosition;
    public String [] regularity;
    public String [] impactAcceleration;
    public Float [] speed;

    public Exercise(int nbrSeries, ExerciseListener listener)
    {
        this.listener = listener;

        this.nbrSeries = nbrSeries;
        this.currentSerie = 0;
        this.isCalibrating = true;
        this.bufferImpact = false;
        this.bufferSize = 50;
        this.calCount = 0;
        this.bufferEulerCount = bufferSize/2;
        this.bufferRawCount   = bufferSize/2;
        this.bufferEulerReady = false;
        this.bufferRawReady = false;
        this.rollCalibration  = 0f;
        this.pitchCalibration = 0f;
        this.yawCalibration   = 0f;

        this.rollBuffer  = new CircularFifoQueue<>(bufferSize);
        this.pitchBuffer = new CircularFifoQueue<>(bufferSize);
        this.yawBuffer   = new CircularFifoQueue<>(bufferSize);
        this.accZBuffer  = new CircularFifoQueue<>(bufferSize);

        this.rollTab  = new Float [this.nbrSeries][50];
        this.pitchTab = new Float [this.nbrSeries][50];
        this.yawTab   = new Float [this.nbrSeries][50];
        this.accZTab  = new Float [this.nbrSeries][50];

        this.pitchMeanStdDev = new Float [] {0f,0f,0f,0f,0f,0f};
        this.yawMeanStdDev = new Float [] {0f,0f,0f,0f,0f,0f};

        this.impactTrajectory = new String [this.nbrSeries];
        this.impactPosition = new Float [this.nbrSeries][2];
        this.regularity = new String [2];
        this.impactAcceleration = new String [this.nbrSeries];
        this.speed = new Float [this.nbrSeries];
    }

    public void addEulerData(Float rollData, Float pitchData, Float yawData)
    {
        if(bufferImpact)
        {
            //Wait next 25 value before save ringbuffer data into out tabs (impact is after 25 eme element in the output tab)
            //-> index -1 = tabOut[24], index +1 = tabOut[25]
            if(bufferEulerCount == 0) {
                bufferEulerReady = true;
                checkForBufferReady();
            }
            bufferEulerCount--;
        }

        //Add data to buffer (avoid adding data if buffer is waiting for other buffer to be full)
        if(!bufferEulerReady)
        {
            rollBuffer.add(rollData);
            pitchBuffer.add(pitchData);
            yawBuffer.add(yawData);
        }

        //Log.d("Exercise", "addEulerData" + "yawBuffer = " + yawBuffer);

        if(isCalibrating)
        {
            calCount++;
            if(calCount >= 50){
                calCount = 0;
                isCalibrating = false;
                mean();
                Log.d("Exercise", "addData = "  + " rollCalibration = " + rollCalibration + " pitchCalibration = " + pitchCalibration + " yawCalibration = " + yawCalibration);
            }
        }
    }

    public void addRawData(Float accX, Float accY, Float accZ)
    {
        if(bufferImpact)
        {
            //Wait next 25 value before save ringbuffer data into out tabs (impact is after 25 eme element in the output tab)
            //-> index -1 = tabOut[24], index +1 = tabOut[25]
            if(bufferRawCount == 0) {
                bufferRawReady = true;
                checkForBufferReady();
            }
            bufferRawCount--;
        }

        //Add data to buffer (avoid adding data if buffer is waiting for other buffer to be full)
        if(!bufferRawReady)
        {
            accZBuffer.add(accZ);
        }

        //Log.d("Exercise", "addRawData" + "accZBuffer = " + accZBuffer);
    }

    private void checkForBufferReady()
    {
        if(bufferEulerReady && bufferRawReady)
        {
            //Log.d("Exercise", "addRawData" + "accZBuffer = " + accZBuffer);
            //Log.d("Exercise", "addEulerData" + "yawBuffer = " + yawBuffer);
            resetFlags();
            copyBuffer();
            analysis();
        }
    }

    public void impactDetect()
    {
        //Log.d("Exercise impact detect", "yawBuffer->" + yawBuffer);
        //Log.d("Exercise impact detect", "accZBuffer->" + accZBuffer);

        if(currentSerie < nbrSeries) currentSerie++;
        else currentSerie = 1;
        bufferImpact = true;
        Log.d("Exercise impact detect", "impactDetect->" + currentSerie);
    }

    private void resetFlags()
    {
        bufferImpact = false;
        bufferEulerReady = false;
        bufferRawReady = false;
        bufferRawCount = bufferSize/2;
        bufferEulerCount = bufferSize/2;
    }

    private void analysis()
    {
        impactTrajectory[currentSerie-1] = analysisImpactTrajectory();
        impactPosition[currentSerie-1] = analysisImpactPosition();
        impactAcceleration[currentSerie-1] = analysisImpactAcceleration();
        speed[currentSerie-1] = analysisCorrelationSpeed();

        //Fire listeners
        if (listener != null){
            listener.onImpactTrajectoryChange(impactTrajectory[currentSerie-1]);
            listener.onImpactPositionChange(impactPosition[currentSerie-1][0], impactPosition[currentSerie-1][1]);
            listener.onImpactAccelerationChange(impactAcceleration[currentSerie-1]);
            listener.onSpeedChange(speed[currentSerie-1]);
        }

        Log.d("Exercise", "impactTrajectory "  +  impactTrajectory[currentSerie-1]);
        Log.d("Exercise", "impactPosition "  +  "Pyaw = " + impactPosition[currentSerie-1][0] + " Ppitch = " + impactPosition[currentSerie-1][1]);
        Log.d("Exercise", "impactAcceleration "  +  impactAcceleration[currentSerie-1]);
        Log.d("Exercise", "speed "  +  speed[currentSerie-1]);

        //Si on est dans le dernier coup, analyse de la regularité sur toutes les series
        if(currentSerie == nbrSeries)
        {
            regularity = analysisRegularity();
            Log.d("Exercise", "regularity "  +  "Yaw = " + regularity[0] + " Pitch = " + regularity[1]);

            if (listener != null){
                listener.onRegularityChange(regularity[0], regularity[1]);
            }
        }
    }

    private String analysisImpactTrajectory()
    {
        boolean [] DbIsPositive = new boolean [this.nbrSeries];
        boolean [] DaIsPositive = new boolean [this.nbrSeries];
        for(int i = 0; i < 3; i ++) {
            DbIsPositive[i] = false;
            DaIsPositive[i] = false;
        }
        String Pi = new String();

        Float [][] bfRollTab  = new Float [this.nbrSeries][3];
        Float [][] afRollTab  = new Float [this.nbrSeries][3];
        Float [][] bfPitchTab  = new Float [this.nbrSeries][3];
        Float [][] afPitchTab  = new Float [this.nbrSeries][3];
        Float [][] bfYawTab  = new Float [this.nbrSeries][3];
        Float [][] afYawTab  = new Float [this.nbrSeries][3];
        Float [][] pitchStdDev = new Float [this.nbrSeries][6];
        Float [][] yawStdDev = new Float [this.nbrSeries][6];


        //bf = index de -3 à -1 (before impact)
        bfYawTab[currentSerie-1][0]  = yawTab[currentSerie-1][22];
        bfYawTab[currentSerie-1][1]  = yawTab[currentSerie-1][23];
        bfYawTab[currentSerie-1][2]  = yawTab[currentSerie-1][24];

        bfPitchTab[currentSerie-1][0]  = pitchTab[currentSerie-1][22];
        bfPitchTab[currentSerie-1][1]  = pitchTab[currentSerie-1][23];
        bfPitchTab[currentSerie-1][2]  = pitchTab[currentSerie-1][24];

        //bf = index de +1 à +3 (after impact)
        afYawTab[currentSerie-1][0]  = yawTab[currentSerie-1][25];
        afYawTab[currentSerie-1][1]  = yawTab[currentSerie-1][26];
        afYawTab[currentSerie-1][2]  = yawTab[currentSerie-1][27];

        afPitchTab[currentSerie-1][0]  = pitchTab[currentSerie-1][22];
        afPitchTab[currentSerie-1][1]  = pitchTab[currentSerie-1][23];
        afPitchTab[currentSerie-1][2]  = pitchTab[currentSerie-1][24];

        //Standard deviation calculations
        Float meanYaw = 0.0f, meanPitch = 0f;

        for( int i = 0; i < 3; i++)
        {
            meanYaw += bfYawTab[currentSerie-1][i] + afYawTab[currentSerie-1][i];
            meanPitch += bfPitchTab[currentSerie-1][i] + afPitchTab[currentSerie-1][i];
        }

        meanYaw /= 6;
        meanPitch /= 6;

        //Log.e("Exercise", "meanYaw "  +  meanYaw);
        //Log.e("Exercise", "meanPitch "  +  meanPitch);

        yawStdDev[currentSerie-1][0] = abs(bfYawTab[currentSerie-1][0] - meanYaw);
        yawStdDev[currentSerie-1][1] = abs(bfYawTab[currentSerie-1][1] - meanYaw);
        yawStdDev[currentSerie-1][2] = abs(bfYawTab[currentSerie-1][2] - meanYaw);
        yawStdDev[currentSerie-1][3] = abs(afYawTab[currentSerie-1][0] - meanYaw);
        yawStdDev[currentSerie-1][4] = abs(afYawTab[currentSerie-1][1] - meanYaw);
        yawStdDev[currentSerie-1][5] = abs(afYawTab[currentSerie-1][2] - meanYaw);

        //Log.e("Exercise", "yawStdDev "  +  Arrays.toString(yawStdDev[currentSerie-1]));

        pitchStdDev[currentSerie-1][0] = abs(bfPitchTab[currentSerie-1][0] - meanPitch);
        pitchStdDev[currentSerie-1][1] = abs(bfPitchTab[currentSerie-1][1] - meanPitch);
        pitchStdDev[currentSerie-1][2] = abs(bfPitchTab[currentSerie-1][2] - meanPitch);
        pitchStdDev[currentSerie-1][3] = abs(afPitchTab[currentSerie-1][0] - meanPitch);
        pitchStdDev[currentSerie-1][4] = abs(afPitchTab[currentSerie-1][1] - meanPitch);
        pitchStdDev[currentSerie-1][5] = abs(afPitchTab[currentSerie-1][2] - meanPitch);

        //Log.e("Exercise", "pitchStdDev "  +  Arrays.toString(pitchStdDev[currentSerie-1]));

        yawMeanStdDev[currentSerie-1] = 0f;
        pitchMeanStdDev[currentSerie-1] = 0f;

        for( int i = 0; i < 6; i++)
        {
            yawMeanStdDev[currentSerie-1] +=  yawStdDev[currentSerie-1][i];
            pitchMeanStdDev[currentSerie-1] +=  pitchStdDev[currentSerie-1][i];
        }

        yawMeanStdDev[currentSerie-1]   /= 6;
        pitchMeanStdDev[currentSerie-1] /= 6;

        //Log.e("Exercise", "bfYawTab "  +  Arrays.toString(bfYawTab[currentSerie-1]));
        //Log.e("Exercise", "afYawTab "  +  Arrays.toString(afYawTab[currentSerie-1]));
        //Log.e("Exercise", "yawMeanStdDev " + yawMeanStdDev[currentSerie-1]);
        //Log.e("Exercise", "pitchMeanStdDev "  + pitchMeanStdDev[currentSerie-1]);

        //Rule 1:
        for(int i = 0; i < 3; i++) {
           if(bfYawTab[currentSerie-1][i] > 0) DbIsPositive[currentSerie-1] = true;
           if(afYawTab[currentSerie-1][i] > 0) DaIsPositive[currentSerie-1] = true;
        }

        //Rule 2:
        //If Da && Db == true
        if( DbIsPositive[currentSerie-1] && DaIsPositive[currentSerie-1]){
            Pi = "ext_ext";
        }
        //If Da && Db == false
        if( !DbIsPositive[currentSerie-1] && !DaIsPositive[currentSerie-1]){
            Pi = "int_int";
        }
        //If Da == true && Db == false
        if( DbIsPositive[currentSerie-1] && !DaIsPositive[currentSerie-1]){
            Pi = "int_ext";
        }
        //If Da == false && Db ==  true
        if( !DbIsPositive[currentSerie-1] && DaIsPositive[currentSerie-1]){
            Pi = "ext_int";
        }

        //reset values
        for(int i = 0; i < 3; i ++) {
            DbIsPositive[i] = false;
            DaIsPositive[i] = false;
        }

        return Pi ;
    }

    private Float[] analysisImpactPosition()
    {
        Float [] PyawTab = new Float [this.nbrSeries];
        Float [] PpitchTab = new Float [this.nbrSeries];
        Float [] returnArray = new Float [this.nbrSeries];

        //yaw/pitch = mean des index de -1 à +1 (before and after impact)
        PyawTab[currentSerie-1] = (yawTab[currentSerie-1][24] + yawTab[currentSerie-1][25]) / 2;
        PpitchTab[currentSerie-1] = (pitchTab[currentSerie-1][24] + pitchTab[currentSerie-1][25]) / 2;

        //Log.d("Exercise ", "PyawTab->" + PyawTab[currentSerie-1]);
        //Log.d("Exercise ", "PpitchTab->" + PpitchTab[currentSerie-1]);

        returnArray[0] = PyawTab[currentSerie-1];
        returnArray[1] = PpitchTab[currentSerie-1];

        return returnArray;
    }

    private String analysisImpactAcceleration()
    {
        String Accel = new String();
        Float [][] AccelTab = new Float [this.nbrSeries][10];

        for( int i = 0; i < 10; i++)
        {
            AccelTab[currentSerie-1][i] = accZTab[currentSerie-1][24-i];
        }

        //Log.d("Exercise ", "AccZTab->" + Arrays.toString(AccelTab[currentSerie-1]));

        //Find max
        Float maxVal = AccelTab[currentSerie-1][0];
        int idxMax = 0;
        for(int i = 1; i < AccelTab[currentSerie-1].length; i++)
        {
            if(AccelTab[currentSerie-1][i] > maxVal)
            {
                maxVal = AccelTab[currentSerie-1][i];
                idxMax = i;
            }
        }

        if(idxMax <= 1)
        {
            Accel = "positive";
        }else{
            Accel = "negative";
        }

        return Accel;
    }

    private Float analysisCorrelationSpeed()
    {
        Float mean;
        Float m = 0.375f;
        Float speedy;
        Float c;

        mean = ( accZTab[currentSerie-1][24] + accZTab[currentSerie-1][25] ) / 2;
        speedy = mean*0.02f; //Speed in m/s
        c = 0.5f*m*(speedy*speedy);

        //Log.d("Exercise ", "speed = " + speedy);
        //Log.d("Exercise ", "c = " + c);
        return c;
    }

    String [] analysisRegularity() {

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

        //keep 1 decimal only
        //Log.d("Exercise", "meanYawStdDev "  +  meanYawStdDev);
        //Log.d("Exercise", "meanPitchStdDev "  +  meanPitchStdDev);

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

        returnArray[0] = returnStringYaw;
        returnArray[1] = returnStringPitch;

        return returnArray;
    }

    private void copyBuffer()
    {
        //Log.d("Exercise", "copy");
        for(int i =0; i< bufferSize; i++) {
            //copy Element into output tab
            rollTab[currentSerie-1][i]  = rollBuffer.get(i) - rollCalibration;
            pitchTab[currentSerie-1][i] = pitchBuffer.get(i) - pitchCalibration;
            yawTab[currentSerie-1][i]   = yawBuffer.get(i) - yawCalibration;
            accZTab[currentSerie-1][i] = accZBuffer.get(i);
        }

        /*for(int i =0; i < nbrSeries; i++)
        {
            Log.d("Exercise copy", "rollTab->" + Arrays.toString(rollTab[i]));
            Log.d("Exercise copy", "pitchTab->" + Arrays.toString(pitchTab[i]));
            Log.d("Exercise copy", "yawTab->" + Arrays.toString(yawTab[i]));
            Log.d("Exercise copy", "AccZTab->" + Arrays.toString(accZTab[i]));
        }*/
    }

    private void mean()
    {
        Float meanRoll = 0f;
        Float meanPitch = 0f;
        Float meanYaw = 0f;
        for(int i=0; i<50;i++){
            meanRoll += rollBuffer.get(i);
            meanPitch += pitchBuffer.get(i);
            meanYaw += yawBuffer.get(i);
        }
        rollCalibration = meanRoll / 50;
        pitchCalibration = meanPitch / 50;
        yawCalibration = meanYaw / 50;
    }
}
