package com.ulab.motionapp.custom;

import android.util.Log;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class RingBuffer {

    public int bufferSize;
    public int bufferMask;
    public boolean isCalibrating;
    private int calCount;
    private int bufferEulerCount;
    private int bufferAccCount;
    private boolean bufferEulerReady;
    private boolean bufferAccReady;

    public int frq;

    public int delayCalib;
    public int timeCalib;

    public boolean bufferImpact;

    public Float rollCalibration;
    public Float pitchCalibration;
    public Float yawCalibration;

    public CircularFifoQueue<Float> rollBuffer;
    public CircularFifoQueue<Float> pitchBuffer;
    public CircularFifoQueue<Float> yawBuffer;
    public CircularFifoQueue<Float> accZBuffer;

    public Float [] rollTab;
    public Float [] pitchTab ;
    public Float [] yawTab;
    public Float [] accZTab;

    public RingBuffer(int bufferSize, int mask, int timeCalib){

        this.bufferSize = bufferSize;
        this.bufferMask = mask;
        this.timeCalib = timeCalib;
        this.bufferEulerCount = bufferSize/2;
        this.bufferAccCount   = bufferSize/2;
        this.isCalibrating = true;
        this.bufferSize = 50;
        this.calCount = 0;
        this.bufferEulerReady = false;
        this.bufferAccReady = false;

        this.frq = 50;   //sample frq = 50 Hz

        this.rollCalibration  = 0f;
        this.pitchCalibration = 0f;
        this.yawCalibration   = 0f;

        this.bufferImpact = false;

        this.rollCalibration  = 0f;
        this.pitchCalibration = 0f;
        this.yawCalibration   = 0f;

        this.rollBuffer  = new CircularFifoQueue<>(bufferSize);
        this.pitchBuffer = new CircularFifoQueue<>(bufferSize);
        this.yawBuffer   = new CircularFifoQueue<>(bufferSize);
        this.accZBuffer  = new CircularFifoQueue<>(bufferSize);

        this.rollTab  = new Float [bufferSize];
        this.pitchTab = new Float [bufferSize];
        this.yawTab   = new Float [bufferSize];
        this.accZTab  = new Float [bufferSize];
    }

    public void impactDetect()
    {
        this.bufferImpact = true;
        Log.d("RingBuffer", "impactDetect = " + this.bufferImpact);
    }

    public void addEulerData(Float rollData, Float pitchData, Float yawData)
    {
        if(this.bufferImpact)
        {
            //Wait next 25 value before save ringbuffer data into out tabs (impact is after 25 eme element in the output tab)
            //-> index -1 = tabOut[24], index +1 = tabOut[25]
            if(this.bufferEulerCount == 0) {
                this.bufferEulerReady = true;
                checkForBufferReady();
            }
            this.bufferEulerCount--;
        }

        //Add data to buffer (avoid adding data if buffer is waiting for other buffer to be full)
        if(!this.bufferEulerReady)
        {
            this.rollBuffer.add(rollData);
            this.pitchBuffer.add(pitchData);
            this.yawBuffer.add(yawData);
        }

        //Log.d("Exercise", "addEulerData" + "yawBuffer = " + yawBuffer);

        if(this.isCalibrating)
        {
            this.calCount++;
            if(this.calCount >= timeCalib * 50){
                this.calCount = 0;
                this.isCalibrating = false;
                mean();
                Log.d("Exercise", "addData = "  + " rollCalibration = " + this.rollCalibration + " pitchCalibration = " + this.pitchCalibration + " yawCalibration = " + this.yawCalibration);
            }
        }
    }

    public void addAccData(Float accX, Float accY, Float accZ)
    {
        if(this.bufferImpact)
        {
            //Wait next 25 value before save ringbuffer data into out tabs (impact is after 25 eme element in the output tab)
            //-> index -1 = tabOut[24], index +1 = tabOut[25]
            if(this.bufferAccCount == 0) {
                this.bufferAccReady = true;
                checkForBufferReady();
            }
            this.bufferAccCount--;
        }

        //Add data to buffer (avoid adding data if buffer is waiting for other buffer to be full)
        if(!this.bufferAccReady)
        {
            this.accZBuffer.add(accZ);
        }
    }

    private void checkForBufferReady()
    {
        if( this.bufferEulerReady && this.bufferAccReady )
        {
            resetFlags();
            //TODO applyRules();
        }
    }

    private void mean()
    {
        Float meanRoll = 0f;
        Float meanPitch = 0f;
        Float meanYaw = 0f;

        for(int i=0; i<this.bufferSize;i++){

            meanRoll += this.rollBuffer.get(i);
            meanPitch += this.pitchBuffer.get(i);
            meanYaw += this.yawBuffer.get(i);
        }
        this.rollCalibration = meanRoll / (this.timeCalib*this.frq);
        this.pitchCalibration = meanPitch / (this.timeCalib*this.frq);
        this.yawCalibration = meanYaw / (this.timeCalib*this.frq);
    }

    private void resetFlags()
    {
        this.bufferImpact = false;
        this.bufferEulerReady = false;
        this.bufferAccReady = false;
        this.bufferAccCount = this.bufferSize/2;
        this.bufferEulerCount = this.bufferSize/2;
    }

}
