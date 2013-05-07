
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A Koch Snowflake.
 *
 * @author David Rosenthal
 */
public class KochSnow extends Frame implements GLEventListener {

    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton(false);
    }
    //Instance aux vars
    GLProfile glprofile = null;  //Profile
    GLCapabilities glcapabilities = null;  //Capabilities
    GLCanvas glcanvas = null; //Canvas
    float[] vertexArr1 = new float[2];
    float[] vertexArr2 = new float[2];
    float[] vertexArr3 = new float[2];
    int recursions = 0;
    //Constructor
    double totalLength = 0;
    float totalArea = 0;

    public KochSnow(String args[]) {
        super("GL2Skeleton");
        glprofile = GLProfile.getDefault();
        glcapabilities = new GLCapabilities(glprofile);
        glcanvas = new GLCanvas(glcapabilities);

        glcanvas.addGLEventListener(this);

        add(glcanvas);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent windowevent) {
                remove(glcanvas);
                dispose();
                System.exit(0);
            }
        });

        setSize(640, 480);
        setVisible(true);

        vertexArr1[0] = Float.valueOf(args[0].trim()).floatValue();
        vertexArr1[1] = Float.valueOf(args[1].trim()).floatValue();
        vertexArr2[0] = Float.valueOf(args[2].trim()).floatValue();
        vertexArr2[1] = Float.valueOf(args[3].trim()).floatValue();
        vertexArr3[0] = Float.valueOf(args[4].trim()).floatValue();
        vertexArr3[1] = Float.valueOf(args[5].trim()).floatValue();
        recursions = Integer.parseInt(args[6]);
        totalArea = 0.5f * Math.abs((vertexArr1[0] - vertexArr3[0]) * (vertexArr2[1] - vertexArr1[1]) - (vertexArr1[0] - vertexArr2[0]) * (vertexArr3[1] - vertexArr1[1]));
        totalLength = Math.sqrt(Math.pow(vertexArr1[0] - vertexArr2[0], 2)
                + Math.pow(vertexArr1[1] - vertexArr2[1], 2))
                + Math.sqrt(Math.pow(vertexArr2[0] - vertexArr3[0], 2)
                + Math.pow(vertexArr2[1] - vertexArr3[1], 2))
                + Math.sqrt(Math.pow(vertexArr1[0] - vertexArr3[0], 2)
                + Math.pow(vertexArr1[1] - vertexArr3[1], 2));
//        for (int i = 0; i < vertexArr1.length; i++) {
//            System.out.println(vertexArr1[i]);
//            System.out.println(vertexArr2[i]);
//            System.out.println(vertexArr3[i]);
//        }
        System.out.println("# of Recursions: " + recursions);
        System.out.println("Area at 0 Recursions: " + totalArea);
        System.out.println("Length at 0 Recursions: " + totalLength);
    }

    public static void main(String[] args) {
        new KochSnow(args);
    }

    //Implementing GLEventListener methods
    @Override
    public void init(GLAutoDrawable glautodrawable) {
        System.out.println("Entering init();");
        GL2 gl = glautodrawable.getGL().getGL2();
        gl.glClearColor(.8f, .8f, .8f, 0f); //set to non-transparent black
    }

    @Override
    public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
        System.out.println("Entering reshape()");
        //Get the context
        GL2 gl2 = glautodrawable.getGL().getGL2();
        //Set up projection
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
        GLU glu = new GLU();
        glu.gluOrtho2D(0.0f, width, 0.0f, height);
        //Set up view
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();

    }
    /*Function setPoints(xyPoint startPoint, xyPoint endPoint, int iterationsLeft)
     * This function is called recursively.  It first checks to see if any more iterations
     * are needed. If so, it calculates the three additional points and calls itself again.
     * If no more iterations are needed, it just returns.*/

    @Override
    public void display(GLAutoDrawable glautodrawable) {
        System.out.println("Entering display");
        //Get context
        GL2 gl2 = glautodrawable.getGL().getGL2();
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
        float width = glautodrawable.getWidth();
        float height = glautodrawable.getHeight();
        gl2.glLoadIdentity();
        gl2.glBegin(GL2.GL_LINE_LOOP);
        /*run the recursion once for each side*/
        gl2.glVertex2f(vertexArr1[0], vertexArr1[1]);
        kochPoints(vertexArr1, vertexArr2, recursions, gl2);
        gl2.glVertex2f(vertexArr2[0], vertexArr2[1]);
        kochPoints(vertexArr2, vertexArr3, recursions, gl2);
        gl2.glVertex2f(vertexArr3[0], vertexArr3[1]);
        kochPoints(vertexArr3, vertexArr1, recursions, gl2);
        gl2.glEnd();

        System.out.println("Total Area at " + recursions + " recursions: " + totalArea);
        System.out.println("Total Length at " + recursions + " recursions: " + totalLength);
    }

    private void kochPoints(float[] start, float[] end, int recursionsLeft, GL2 gl2) {
        float[] tempFirstPoint = {0f, 0f};
        float[] tempSecondPoint = {0f, 0f};
        float[] tempThirdPoint = {0f, 0f};

        if (recursionsLeft != 0) {
            tempFirstPoint = getPointOne(start, end, tempFirstPoint); //A
            tempThirdPoint = getPointThree(start, end, tempThirdPoint); //C
            tempSecondPoint = getPointTwo(tempFirstPoint, tempThirdPoint, tempSecondPoint);
            //1/2(xa-xc)(yb-ya)-(xa-xb)(yc-ya)
            totalArea = totalArea
                    + 0.5f * ((tempFirstPoint[0] - tempThirdPoint[0]) * (tempSecondPoint[1] - tempFirstPoint[1])
                    - (tempFirstPoint[0] - tempSecondPoint[0]) * (tempThirdPoint[1] - tempFirstPoint[1]));
            totalLength = totalLength
                    + Math.sqrt(Math.pow(tempFirstPoint[0] - tempSecondPoint[0], 2)
                    + Math.pow(tempFirstPoint[1] - tempSecondPoint[1], 2));
            kochPoints(start, tempFirstPoint, recursionsLeft - 1, gl2);
            gl2.glVertex2f(tempFirstPoint[0], tempFirstPoint[1]);
            kochPoints(tempFirstPoint, tempSecondPoint, recursionsLeft - 1, gl2);
            gl2.glVertex2f(tempSecondPoint[0], tempSecondPoint[1]);
            kochPoints(tempSecondPoint, tempThirdPoint, recursionsLeft - 1, gl2);
            gl2.glVertex2f(tempThirdPoint[0], tempThirdPoint[1]);
            kochPoints(tempThirdPoint, end, recursionsLeft - 1, gl2);
        }
    }

    private float[] getPointOne(float[] startPoint, float[] endPoint, float[] tempPoint) {
        tempPoint[0] = startPoint[0] + (endPoint[0] - startPoint[0]) / 3;
        tempPoint[1] = startPoint[1] + (endPoint[1] - startPoint[1]) / 3;
        return tempPoint;
    }

    private float[] getPointTwo(float[] firstPoint, float[] lastPoint, float[] midPoint) {
        Double angle;//angle of kincidence
        Double hypot;//the length of the segment
        float segX;//the delta X of the middle line segment
        float segY;//the delta Y of the middle line segment
        Double delX;//X distance from reference point to middle point we want
        Double delY;//Y distance from reference point to middle point we want

        /*calculate the changes in X and Y axis*/
        segX = lastPoint[0] - firstPoint[0];
        segY = lastPoint[1] - firstPoint[1];

        /*treating this like a triangle, we find the hypotenuse length*/
        hypot = Math.sqrt(segX * segX + segY * segY);

        /*calculate the angle for the hypotenuse*/
        angle = Math.atan(segY / segX);

        /*calculate the midpoint of the segment.
         * This is used as a reference point*/
        midPoint[0] = firstPoint[0] + (segX) / 2;
        midPoint[1] = firstPoint[1] + (segY) / 2;

        /*Find the delta from reference point to final point*/
        delX = hypot * Math.sin(angle);
        delY = hypot * Math.cos(angle);
        /*Moving up and right, or straight up*/
        if ((segX >= 0) && (segY > 0)) {
            midPoint[0] = midPoint[0] + delX.floatValue();
            midPoint[1] = midPoint[1] - delY.floatValue();
        } else {
            /*Moving down and right, or straight right*/
            if ((segX > 0) && (segY <= 0)) {
                midPoint[0] = midPoint[0] + delX.floatValue();
                midPoint[1] = midPoint[1] - delY.floatValue();
            } else {
                /*Moving up and left, or straight left*/
                if ((segX < 0) && (segY >= 0)) {
                    midPoint[0] = midPoint[0] - delX.floatValue();
                    midPoint[1] = midPoint[1] + delY.floatValue();
                } else {
                    /*Moving down and left, or straight down*/
                    if ((segX <= 0) && (segY < 0)) {
                        midPoint[0] = midPoint[0] - delX.floatValue();
                        midPoint[1] = midPoint[1] + delY.floatValue();
                    }
                }
            }
        }
        return midPoint;
    }

    private float[] getPointThree(float[] startPoint, float[] endPoint, float[] tempPoint) {
        /*calculate 1/3 of X distance*/
        tempPoint[0] = startPoint[0] + 2 * (endPoint[0] - startPoint[0]) / 3;
        /*calculate 1/3 of Y distance*/
        tempPoint[1] = startPoint[1] + 2 * (endPoint[1] - startPoint[1]) / 3;

        return tempPoint;
    }

    @Override
    public void dispose(GLAutoDrawable glautodrawable) {
    }
}