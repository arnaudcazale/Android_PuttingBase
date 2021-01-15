package com.ulab.motionapp.custom;

public class XpertNode {

    public enum XpertNodeName {XpN, XpS, XpI, XpA, XpR};

    private XpertNodeName name;
    private String parameters [];
    public byte sensorMask;
    /*sensorMask is coded into 1 byte: Heading/Gravity/Euler/Quaternions/Raw/Impact(LSB)
    example: 0x20 -> Heading notif are authorized
             0x01 -> Impact notifs are authorized*/

    public XpertNode (XpertNodeName name)
    {
        //All this initialization process should be done by reading the appropriate BDD contents,
        //Hard-coded here for example purpose

        this.name = name;

        switch(this.name)
        {
            case XpN:
                parameters = new String [7];
                sensorMask = 0b011001; // 0/Gravity/Euler/0/0/Impact
                break;
            /*case XpS:
                parameters = new String [9];
                break;
            case XpI:
            case XpA:
                parameters = new String [2];
                break;
            case XpR:
                parameters = new String [1];
                break;*/
        }

    }

}
