
public class Camera
{
    private double xPos;
    private double yPos;
    private double zPos;
    
    private double xLPos;
    private double yLPos;
    private double zLPos;
    
    private double pitch;
    private double yaw;
    
    public Camera()
    {
        xPos = 0;
        yPos = 0;
        zPos = 0;
        
        xLPos = 0;
        yLPos = 0;
        zLPos = 10;
    }
    
    public Camera(double xPos, double yPos, double zPos, double xLPos, double yLPos, double zLPos)
    {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        
        this.xLPos = xLPos;
        this.yLPos = yLPos;
        this.zLPos = zLPos;
    }
    
    public void setPitch(double pitch)
    {
    	this.pitch = pitch;
    }
    
    public void setYaw(double yaw)
    {
    	this.yaw = yaw;
    }

    public void updatePosition(double xPos, double yPos, double zPos)
    {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
    }
    
    public void lookPosition(double xLPos, double yLPos, double zLPos)
    {
        this.xLPos = xLPos;
        this.yLPos = yLPos;
        this.zLPos = zLPos;
    }

    
    // Moves the entity forward according to its pitch and yaw and the magnitude.
    
    public void moveForward(double magnitude, double mapHeight)
    {        // Spherical coordinates maths
        double xMovement = magnitude * Math.cos(pitch) * Math.cos(yaw); 
        double yMovement = magnitude * Math.sin(pitch);
        double zMovement = magnitude * Math.cos(pitch) * Math.sin(yaw);
              
        double xNew = xPos + xMovement;
        double yNew = yPos + yMovement;
        double zNew = zPos + zMovement;
        
        if(zNew <= 0.05)
            zNew = 1;
        
        if(zNew >= 319.5)
            zNew = 319;
        
        if(xNew <= 0.05)
            xNew = 1;
        
        if (xNew >=282.5)
            xNew = 282;
        
        if(mapHeight > yNew || mapHeight < yNew)
        {
            yNew = mapHeight+5;
        }
        updatePosition(xNew, yNew, zNew);
        updateLook();
    }
    
    public void strafeLeft(double magnitude, double mapHeight)
    {
//        double pitchTemp = pitch;
      //  pitch = 0;
        yaw = yaw - (0.5 * Math.PI);
        moveForward(magnitude, mapHeight);

//        pitch = pitchTemp;
        yaw = yaw + (0.5 * Math.PI);
        updateLook();
    }
    
    public void strafeRight(double magnitude, double mapHeight)
    {
//        double pitchTemp = pitch;
//        pitch = 0;

        yaw = yaw + (0.5 * Math.PI);
        moveForward(magnitude, mapHeight);
        yaw = yaw - (0.5 * Math.PI);

//        pitch = pitchTemp;
        updateLook();
    }
    public void jump(double velocity, double mapHeight)
    {
        if(yPos > mapHeight+5)
            updatePosition(xPos, yPos+velocity, zPos);
        else
            updatePosition(xPos, mapHeight + 5, zPos);
        updateLook();
    }
    public void lookUp(double magnitude)
    {
        pitchUp(magnitude);
        updateLook();
    }
    public void lookDown(double magnitude)
    {
        pitchDown(magnitude);
        updateLook();
    }
    
    public void lookLeft(double magnitude)
    {
        yawLeft(magnitude);
        updateLook();
    }
    
    public void lookRight(double magnitude)
    {
        yawRight(magnitude);
        updateLook();
    }
    
    public void updateLook()
    {
        if(pitch > 1.0)
        pitch = 0.99;
        
        if(pitch < -1.0)
        pitch = -0.99;
        double xLook = xPos + Math.cos(pitch) * Math.cos(yaw);
        double yLook = yPos + Math.sin(pitch);
        double zLook = zPos + Math.cos(pitch) * Math.sin(yaw);
        
        lookPosition(xLook, yLook, zLook);
    }
           
    
    /* -------Get commands--------- */
    
    public double getXPos()
    {
        return xPos;
    }
    
    public double getYPos()
    {
        return yPos;
    }
    
    public double getZPos()
    {
        return zPos;
    }
    
    public double getXLPos()
    {
        return xLPos;
    }
    
    public double getYLPos()
    {
        return yLPos;
    }
    
    public double getZLPos()
    {
        return zLPos;
    }
    
    public double getPitch()
    {
        return pitch;
    }
    
    public double getYaw()
    {
        return yaw;
    }
    
    /* --------------------------- */
    
    /* -------------- Pitch and Yaw commands --------------- */
    
    private void pitchUp(double amount)
    {
        this.pitch += amount;
    }
    
    private void pitchDown(double amount)
    {
        this.pitch -= amount;
    }
    
    private void yawRight(double amount)
    {
        this.yaw += amount;
    }
    
    private void yawLeft(double amount)
    {
        this.yaw -= amount;
    }
    
    /* ---------------------------------------------------- */
    
}
