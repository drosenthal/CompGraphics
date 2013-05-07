
import java.awt.AWTException;
import java.awt.Point;
import java.nio.FloatBuffer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.*;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;
import com.jogamp.opengl.util.awt.TextRenderer;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Robot;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *  Project 3: Terrain Visualization.
 *
 * @author David Rosenthal
 */
public class contourLines extends JFrame implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{

    static
    {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton(false);
    }
    //Camera set-up
    GLU glu;
    Camera camera;
    Robot robot;
    TextRenderer renderer;
    private boolean targetReticule = false;
    private static String basename;
    private float[] lightAmbient =
    {
        0.2f, 0.2f, 0.2f, 1.0f
    };
    private float[] lightDiffuse =
    {
        0.8f, 0.8f, 0.8f, 1.0f
    };
    private float[] lightSpecular =
    {
        0.5f, 0.5f, 0.5f, 1.0f
    };
    private float[] lightPosition =
    {
        0.0f, 1500.0f, 0.0f, 1.0f
    };
    private static int low, high, step, numOfLines;
    private static float colorRStep, colorGStep, colorBStep;
    private static float lowR = 0,
            lowG = 0,
            lowB = 0,
            highR = 1,
            highG = 1,
            highB = 1;
    private static boolean drawContour = true,
            enableWireFrame = false,
            marker,
            auto = false;
    private boolean scaled, fileChanged;
    private float red = 1.0f,
            green = 0.0f,
            blue = 0.0f;
    //Perspective Variables
    double[] eyePos =
    {
        1, 44, 1
    },
            targetPos =
    {
        2, 44, 1
    },
            upVector =
    {
        0, 1, 0
    };
    double fov = 45,
            aspectRatio = 1,
            zNear = 0.1,
            zFar = 1000,
            dIncrement = .2,
            aIncrement = 0.1,
            phi = 0,
            theta = 90.0 / 180.0 * Math.PI,
            eyeDist = Math.sqrt(eyePos[0] * eyePos[0] + eyePos[1] * eyePos[1] + eyePos[2] * eyePos[2]);
    private int mouseLocationX;
    private int mouseLocationY;
    private int velocity = 20;
    private int theTerrain = 1, theContour = 2, base = 0;
    //Contour lines variables
    String baseName = "data/NED_86879038";
    private gridFloatReader map;
    private List<List<Point2D>> contour = new ArrayList<List<Point2D>>();
    //Instance aux vars
    GLProfile glprofile = null;  //Profile
    GLCapabilities glcapabilities = null;  //Capabilities
    GLCanvas glcanvas = null; //Canvas
    //Constructor

    public contourLines()
    {
        super("contourLines");
        //       args[0] = baseName;

        glprofile = GLProfile.getDefault();
        glcapabilities = new GLCapabilities(glprofile);
        glcanvas = new GLCanvas(glcapabilities);

        glcanvas.addGLEventListener(this);
        glcanvas.addKeyListener(this);
        glcanvas.addMouseListener(this);
        glcanvas.addMouseMotionListener(this);
        glcanvas.addMouseWheelListener(this);
        try
        {
            robot = new Robot();
        } catch (AWTException ex)
        {
            Logger.getLogger(contourLines.class.getName()).log(Level.SEVERE, null, ex);
        }
//Create the menu bar and add the necessary menus to it
        JMenuBar menuBar = new JMenuBar();

        //Set the menu bar
        setJMenuBar(menuBar);

        //File and Terrain menu
        JMenu fileMenu = new JMenu("File");
        JMenu terrainMenu = new JMenu("Terrain");
        menuBar.add(fileMenu);
        menuBar.add(terrainMenu);

        //Menu Items
        JMenuItem open = new JMenuItem("Open");
        open.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                // Create a file chooser
                JFileChooser fc = new JFileChooser();

                String result = JOptionPane.showInputDialog("Enter the basename(full path of file without extension) of the file that you want to open", basename);
                if (!result.isEmpty())
                {
                    basename = result;
                    map = new gridFloatReader(basename);
                    fileChanged = true;
                } else
                {
                    System.out.println("Did not enter a basename for a file.");
                }
            }
        });

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                // TODO Auto-generated method stub
                System.exit(0);
            }
        });

        JMenuItem toggleContour = new JMenuItem("Toggle Contour");
        toggleContour.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                // TODO Auto-generated method stub
                if (drawContour)
                {
                    drawContour = false;
                } else
                {
                    drawContour = true;
                }
            }
        });

        JMenuItem toggleWireframe = new JMenuItem("Toggle Wireframe");
        toggleWireframe.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                // TODO Auto-generated method stub
                if (enableWireFrame)
                {
                    enableWireFrame = false;
                } else
                {
                    enableWireFrame = true;
                }
            }
        });

        //Add the menu items to file and terrain menus
        fileMenu.add(open);
        fileMenu.add(exit);
        terrainMenu.add(toggleContour);
        terrainMenu.add(toggleWireframe);
        // make the canvas display at 60 fps
		/*Animator animator = new FPSAnimator(canvas, 60);
        animator.add(canvas);
        animator.start();*/

        //Add canvas to the Frame
        getContentPane().add(glcanvas);

        setSize(640, 480);
        setVisible(true);
    }

    public static void main(String[] args)
    {
        if (drawContour)
        {
            if (!args[1].equals("auto"))
            {
                basename = args[0];
                low = Integer.parseInt(args[1]);
                high = Integer.parseInt(args[2]);
                step = Integer.parseInt(args[3]);
                lowR = Integer.parseInt(args[4]);
                lowG = Integer.parseInt(args[5]);
                lowB = Integer.parseInt(args[6]);
                highR = Integer.parseInt(args[7]);
                highG = Integer.parseInt(args[8]);
                highB = Integer.parseInt(args[9]);
                marker = Boolean.parseBoolean(args[10]);
            } else
            {
                auto = true;
                basename = args[0];
                lowR = Integer.parseInt(args[2]);
                lowG = Integer.parseInt(args[3]);
                lowB = Integer.parseInt(args[4]);
                highR = Integer.parseInt(args[5]);
                highG = Integer.parseInt(args[6]);
                highB = Integer.parseInt(args[7]);
                marker = Boolean.parseBoolean(args[8]);
            }
        } else
        {
            basename = args[0];
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {

            public void run()
            {
                createAndShowGUI();
            }
        });
    }

    //Implementing GLEventListener methods
    @Override
    public void init(GLAutoDrawable glautodrawable)
    {
        map = new gridFloatReader(basename);
        glu = new GLU();
        GL2 gl2 = glautodrawable.getGL().getGL2();
        gl2.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, this.lightAmbient, 0);
        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, this.lightDiffuse, 0);
        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, this.lightSpecular, 0);
        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, this.lightPosition, 0);
        gl2.glEnable(GL2.GL_LIGHT0);
        gl2.glEnable(GL2.GL_LIGHTING);
        gl2.glEnable(GL2.GL_COLOR_MATERIAL);
        gl2.glShadeModel(GL2.GL_SMOOTH); /* enable smooth shading */
        gl2.glEnable(GL2.GL_DEPTH_TEST); /* enable z buffer */
        gl2.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl2.glColor3f(0.0f, 0.0f, 0.0f);

        renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 36));
        renderer.setColor(Color.black);
        camera = new Camera(eyePos[0], eyePos[1], eyePos[2],
                targetPos[0], targetPos[1], targetPos[2]);
        System.out.println(camera.getXLPos() + " " + camera.getYLPos()
                + camera.getZLPos());

        System.out.println("Entering init();");
//        camera.moveForward(-10);	

        gl2.glEnable(GL2.GL_DEPTH_TEST);

        initTerrain(gl2);
        glu.gluPerspective(fov, aspectRatio, zNear, zFar);
        glu.gluLookAt(camera.getXPos(), camera.getYPos(), camera.getZPos(),
                camera.getXLPos(), camera.getYLPos(), camera.getZLPos(),
                upVector[0], upVector[1], upVector[2]);
    }

    public void initTerrain(GL2 gl)
    {
        theTerrain = gl.glGenLists(1);

        //change all the colors to percentages
        lowR = lowR / 255;
        lowG = lowG / 255;
        lowB = lowB / 255;
        highR = highR / 255;
        highG = highG / 255;
        highB = highB / 255;

        //calculate the color step
        colorRStep = (highR - lowR) / map.getNRows();
        colorGStep = (highG - lowG) / map.getNRows();
        colorBStep = (highB - lowB) / map.getNRows();

        if (drawContour)
        {
            if (auto)
            {
                low = (int) map.minHeight();
                high = (int) map.maxHeight();
                step = -10;
            }

            //change all the colors to percentages
            lowR = lowR / 255;
            lowG = lowG / 255;
            lowB = lowB / 255;
            highR = highR / 255;
            highG = highG / 255;
            highB = highB / 255;
            gl.glClearColor(1.0f, 1.0f, 1.0f, 0f);
            if (step == 0)
            {
                System.out.println("Step cannot be 0!");
                return;
            } else if (step < 0)
            {
                step = (high - low) / Math.abs(step);
            }

            numOfLines = (high - low) / step;

            scaled = false;
//init the contour line
            for (int i = low; i < high; i += step)
            {
                contour.add(generateContourLine(i));
            }
            theContour = gl.glGenLists(2);
            gl.glNewList(theContour, GL2.GL_COMPILE);
            gl.glPushMatrix();
            createContour(gl);
            gl.glPopMatrix();
            gl.glEndList();
        }

        gl.glNewList(theTerrain, GL2.GL_COMPILE);
        gl.glPushMatrix();
        createTerrain(gl);
        gl.glPopMatrix();
        gl.glEndList();
    }

    @Override
    public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height)
    {
        System.out.println("Entering reshape()");
        //Get the context
        GL2 gl2 = glautodrawable.getGL().getGL2();
        //Set up projection
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();

        //  gl2.glOrtho(0, 320, 0, 283, 0, 10);

        aspectRatio = (double) 640 / (double) 480;



        if (aspectRatio * height < width)
        {
            gl2.glViewport(x, y, (int) (aspectRatio * height), height);
        } else
        {
            gl2.glViewport(x, y, width, (int) (width / aspectRatio));
        }

    }
    /*Function setPoints(xyPoint startPoint, xyPoint endPoint, int iterationsLeft)
     * This function is called recursively.  It first checks to see if any more iterations
     * are needed. If so, it calculates the three additional points and calls itself again.
     * If no more iterations are needed, it just returns.*/

    public static void createAndShowGUI()
    {
        contourLines s = new contourLines();
        s.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        s.pack();
        s.setVisible(true);
    }

    @Override
    public void display(GLAutoDrawable glautodrawable)
    {
        //Get context
        GL2 gl2 = glautodrawable.getGL().getGL2();

        //Set a color (redish - no other components)
        //     gl2.glColor3f(lowR, lowG, lowB);

        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();
        if (enableWireFrame)
        {
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        } else
        {
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        }
        glu.gluPerspective(fov, aspectRatio, zNear, zFar);
        glu.gluLookAt(camera.getXPos(), camera.getYPos(), camera.getZPos(),
                camera.getXLPos(), camera.getYLPos(), camera.getZLPos(),
                upVector[0], upVector[1], upVector[2]);

        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        red = lowR;
        green = lowG;
        blue = lowB;

        gl2.glCallList(theTerrain);

        if (drawContour)
        {
            gl2.glCallList(theContour);
        }
        if (targetReticule)
        {
            renderer.beginRendering(glautodrawable.getWidth(), glautodrawable.getHeight());
            renderer.draw("+", glautodrawable.getWidth() / 2, glautodrawable.getHeight() / 2);
            renderer.endRendering();
        }
        //Restore matrix
        gl2.glPopMatrix();

        if (marker)
        {
            gl2.glColor3f(0, 0, 0);
            gl2.glPointSize(5);
            gl2.glBegin(GL2.GL_POINTS);
            gl2.glVertex3d(map.maxHeightY(), 100, map.maxHeightX());
            gl2.glEnd();
        }
    }

    @Override
    public void dispose(GLAutoDrawable glautodrawable)
    {
    }

    public void createContour(GL2 gl2)
    {
        for (int i = 0; i < contour.size(); i++)
        {
            //change color of each contour

            gl2.glColor3f(0, 0, 0);
            float colorBlack[] =
            {
                0.0f, 0.0f, 0.0f, 1.0f
            };
            gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE,
                    colorBlack, 0);
            gl2.glBegin(GL2.GL_LINES);
            for (int j = 0; j < contour.get(i).size() - 1; j++)
            {
                if (!contour.get(i).isEmpty())
                {

                    int x = (int) contour.get(i).get(j).getX();
                    int z = (int) contour.get(i).get(j).getY();

                    double y = 100 * (map.getHeight((int) z, (int) x) / map.maxHeight());
                    gl2.glVertex3d(x, y, z);
                }
            }
            gl2.glEnd();
        }
    }

    public List<Point2D> generateContourLine(double c)
    {
        // holds the contour line that will be returned
        ArrayList<Point2D> contourLine = new ArrayList<Point2D>();

        // loop through all the cells
        for (int i = 0; i < map.getNRows() - 1; i++)
        {
            for (int j = 0; j < map.getNCols() - 1; j++)
            {
                double x1 = j;
                double y1 = i;
                double x2 = j + 1;
                double y2 = i + 1;

                // get the four points of a cell
                Point2D upperLeft = new Point2D.Double(x1, y2); // upper left point of cell
                Point2D upperRight = new Point2D.Double(x2, y2); //upper right point of cell
                Point2D bottomLeft = new Point2D.Double(x1, y1); // bottom left point of cell
                Point2D bottomRight = new Point2D.Double(x2, y1); // bottom right point of cell

                // evaluate the height at each point and check it against c
                double upperLeftVal = map.getHeight(i + 1, j);
                double upperRightVal = map.getHeight(i + 1, j + 1);
                double bottomLeftVal = map.getHeight(i, j);
                double bottomRightVal = map.getHeight(i, j + 1);

                // greater than flags
                boolean uLGreater = false;
                boolean uRGreater = false;
                boolean bLGreater = false;
                boolean bRGreater = false;

                double midX, midY;

                int greaterThanCount = 0;

                // how many points are greater than c?
                if (upperLeftVal >= c)
                {
                    greaterThanCount++;
                    uLGreater = true;
                }
                if (upperRightVal >= c)
                {
                    greaterThanCount++;
                    uRGreater = true;
                }
                if (bottomLeftVal >= c)
                {
                    greaterThanCount++;
                    bLGreater = true;
                }
                if (bottomRightVal >= c)
                {
                    greaterThanCount++;
                    bRGreater = true;
                }

                if (greaterThanCount == 1 || greaterThanCount == 3)
                {
                    //only one point is greater than c
                    if ((!uRGreater && !bLGreater && !bRGreater) || (uRGreater && bLGreater && bRGreater))
                    {
                        midX = (c - Math.min(upperLeftVal, upperRightVal)) / (Math.max(upperLeftVal, upperRightVal) - Math.min(upperLeftVal, upperRightVal)) + bottomLeft.getX();
                        midY = (c - Math.min(upperLeftVal, bottomLeftVal)) / (Math.max(upperLeftVal, bottomLeftVal) - Math.min(upperLeftVal, bottomLeftVal)) + bottomLeft.getY();
                        cornerDiagonal(contourLine, upperLeft, upperRight,
                                bottomLeft, bottomRight, uLGreater, uRGreater,
                                bLGreater, bRGreater, midX, midY);
                    } else if ((!uLGreater && !bLGreater && !bRGreater) || (uLGreater && bLGreater && bRGreater))
                    {
                        midX = (c - Math.min(upperLeftVal, upperRightVal)) / (Math.max(upperLeftVal, upperRightVal) - Math.min(upperLeftVal, upperRightVal)) + bottomLeft.getX();
                        midY = (c - Math.min(upperRightVal, bottomRightVal)) / (Math.max(upperRightVal, bottomRightVal) - Math.min(upperRightVal, bottomRightVal)) + bottomLeft.getY();
                        cornerDiagonal(contourLine, upperLeft, upperRight,
                                bottomLeft, bottomRight, uLGreater, uRGreater,
                                bLGreater, bRGreater, midX, midY);
                    } else if ((!uRGreater && !uLGreater && !bRGreater) || (uRGreater && uLGreater && bRGreater))
                    {
                        midX = (c - Math.min(bottomLeftVal, bottomRightVal)) / (Math.max(bottomLeftVal, bottomRightVal) - Math.min(bottomLeftVal, bottomRightVal)) + bottomLeft.getX();
                        midY = (c - Math.min(upperLeftVal, bottomLeftVal)) / (Math.max(upperLeftVal, bottomLeftVal) - Math.min(upperLeftVal, bottomLeftVal)) + bottomLeft.getY();
                        cornerDiagonal(contourLine, upperLeft, upperRight,
                                bottomLeft, bottomRight, uLGreater, uRGreater,
                                bLGreater, bRGreater, midX, midY);
                    } else if ((!uLGreater && !uRGreater && !bLGreater) || (uLGreater && uRGreater && bLGreater))
                    {
                        midX = (c - Math.min(bottomLeftVal, bottomRightVal)) / (Math.max(bottomLeftVal, bottomRightVal) - Math.min(bottomLeftVal, bottomRightVal)) + bottomLeft.getX();
                        midY = (c - Math.min(upperRightVal, bottomRightVal)) / (Math.max(upperRightVal, bottomRightVal) - Math.min(upperRightVal, bottomRightVal)) + bottomLeft.getY();
                        cornerDiagonal(contourLine, upperLeft, upperRight,
                                bottomLeft, bottomRight, uLGreater, uRGreater,
                                bLGreater, bRGreater, midX, midY);
                    }
                } else if (greaterThanCount == 2)
                {

                    if ((uLGreater && uRGreater) || (bLGreater && bRGreater))
                    {
                        double midY1 = (c - Math.min(upperLeftVal, bottomLeftVal)) / (Math.max(upperLeftVal, bottomLeftVal) - Math.min(upperLeftVal, bottomLeftVal)) + bottomLeft.getY();
                        double midY2 = (c - Math.min(upperRightVal, bottomRightVal)) / (Math.max(upperRightVal, bottomRightVal) - Math.min(upperRightVal, bottomRightVal)) + bottomLeft.getY();
                        horizontalLine(contourLine, upperLeft, upperRight, midY1, midY2);
                    } else if ((uLGreater && bLGreater) || (uRGreater && bRGreater))
                    {
                        double midX1 = (c - Math.min(bottomLeftVal, bottomRightVal)) / (Math.max(bottomLeftVal, bottomRightVal) - Math.min(bottomLeftVal, bottomRightVal)) + bottomLeft.getX();
                        double midX2 = (c - Math.min(upperLeftVal, upperRightVal)) / (Math.max(upperLeftVal, upperRightVal) - Math.min(upperLeftVal, upperRightVal)) + bottomLeft.getX();
                        verticalLine(contourLine, upperLeft, bottomLeft, midX1, midX2);
                    } else
                    {
                        if (uLGreater)
                        {
                            midX = (c - Math.min(upperLeftVal, upperRightVal)) / (Math.max(upperLeftVal, upperRightVal) - Math.min(upperLeftVal, upperRightVal)) + bottomLeft.getX();
                            midY = (c - Math.min(upperLeftVal, bottomLeftVal)) / (Math.max(upperLeftVal, bottomLeftVal) - Math.min(upperLeftVal, bottomLeftVal)) + bottomLeft.getY();
                            cornerDiagonal(contourLine, upperLeft, upperRight,
                                    bottomLeft, bottomRight, uLGreater, uRGreater,
                                    bLGreater, bRGreater, midX, midY);
                        }
                        if (uRGreater)
                        {
                            midX = (c - Math.min(upperLeftVal, upperRightVal)) / (Math.max(upperLeftVal, upperRightVal) - Math.min(upperLeftVal, upperRightVal)) + bottomLeft.getX();
                            midY = (c - Math.min(upperRightVal, bottomRightVal)) / (Math.max(upperRightVal, bottomRightVal) - Math.min(upperRightVal, bottomRightVal)) + bottomLeft.getY();
                            cornerDiagonal(contourLine, upperLeft, upperRight,
                                    bottomLeft, bottomRight, uLGreater, uRGreater,
                                    bLGreater, bRGreater, midX, midY);
                        }
                        if (bLGreater)
                        {
                            midX = (c - Math.min(bottomLeftVal, bottomRightVal)) / (Math.max(bottomLeftVal, bottomRightVal) - Math.min(bottomLeftVal, bottomRightVal)) + bottomLeft.getX();
                            midY = (c - Math.min(upperLeftVal, bottomLeftVal)) / (Math.max(upperLeftVal, bottomLeftVal) - Math.min(upperLeftVal, bottomLeftVal)) + bottomLeft.getY();
                            cornerDiagonal(contourLine, upperLeft, upperRight,
                                    bottomLeft, bottomRight, uLGreater, uRGreater,
                                    bLGreater, bRGreater, midX, midY);
                        }
                        if (bRGreater)
                        {
                            midX = (c - Math.min(bottomLeftVal, bottomRightVal)) / (Math.max(bottomLeftVal, bottomRightVal) - Math.min(bottomLeftVal, bottomRightVal)) + bottomLeft.getX();
                            midY = (c - Math.min(upperRightVal, bottomRightVal)) / (Math.max(upperRightVal, bottomRightVal) - Math.min(upperRightVal, bottomRightVal)) + bottomLeft.getY();
                            cornerDiagonal(contourLine, upperLeft, upperRight,
                                    bottomLeft, bottomRight, uLGreater, uRGreater,
                                    bLGreater, bRGreater, midX, midY);
                        }
                    }
                }
            }
        }

        return contourLine;
    }

    private void cornerDiagonal(ArrayList<Point2D> contourLine,
            Point2D upperLeft, Point2D upperRight, Point2D bottomLeft,
            Point2D bottomRight, boolean uLGreater, boolean uRGreater,
            boolean bLGreater, boolean bRGreater, double midX, double midY)
    {

        Point2D point1 = new Point2D.Double(midX, upperRight.getY());
        Point2D point2 = new Point2D.Double(upperRight.getX(), midY);
        contourLine.add(point1);
        contourLine.add(point2);
    }

    private void verticalLine(ArrayList<Point2D> contourLine,
            Point2D upperLeft, Point2D bottomLeft, double midX1, double midX2)
    {
        Point2D point1 = new Point2D.Double(midX1, upperLeft.getY());
        Point2D point2 = new Point2D.Double(midX2, bottomLeft.getY());
        contourLine.add(point1);
        contourLine.add(point2);
    }

    private void horizontalLine(ArrayList<Point2D> contourLine,
            Point2D upperLeft, Point2D upperRight, double midY1, double midY2)
    {
        Point2D point1 = new Point2D.Double(upperLeft.getX(), midY1);
        Point2D point2 = new Point2D.Double(upperRight.getX(), midY2);
        contourLine.add(point1);
        contourLine.add(point2);
    }

    public void createTerrain(GL2 gl)
    {
        float[] colorRed =
        {
            1.0f, 0.0f, 0.0f, 1.0f
        };
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE,
                colorRed, 0);
        gl.glBegin(GL2.GL_QUADS);
        generateTerrain(gl);
        gl.glEnd();
    }

    public void generateTerrain(GL2 gl)
    {
        // loop through all the cells
        for (int i = 0; i < map.getNRows() - 1; i++)
        {
            //     gl.glColor3f(red, green, blue);
            for (int j = 0; j < map.getNCols() - 1; j++)
            {
                double x1 = j;
                double y1 = i;
                double x2 = j + 1;
                double y2 = i + 1;

                // evaluate the height at each point
                double upperLeftHeight = 100 * (map.getHeight(i + 1, j) / map.maxHeight());
                double upperRightHeight = 100 * (map.getHeight(i + 1, j + 1) / map.maxHeight());
                double bottomLeftHeight = 100 * (map.getHeight(i, j) / map.maxHeight());
                double bottomRightHeight = 100 * (map.getHeight(i, j + 1) / map.maxHeight());
                // get the four points of a cell
                Point3d upperLeft = new Point3d(x1, y2, upperLeftHeight); // upper left point of cell
                Point3d upperRight = new Point3d(x2, y2, upperRightHeight); //upper right point of cell
                Point3d bottomLeft = new Point3d(x1, y1, bottomLeftHeight); // bottom left point of cell
                Point3d bottomRight = new Point3d(x2, y1, bottomRightHeight); // bottom right point of cell


                Vector3d normal = new Vector3d();
                if (upperLeft.getX() > 0 && upperLeft.getY() > 0 && upperLeft.getX() < 282 && upperLeft.getY() < 319)
                {
                    normal = normalize(upperLeft);
                }
                gl.glNormal3d(normal.getX(), normal.getZ(), normal.getY());
                gl.glVertex3d(upperLeft.getX(), upperLeft.getZ(), upperLeft.getY());

                gl.glColor3f(red + colorRStep, green + colorGStep, blue + colorBStep);

                if (bottomLeft.getX() > 0 && bottomLeft.getY() > 0 && bottomLeft.getX() < 282 && bottomLeft.getY() < 319)
                {
                    normal = normalize(bottomLeft);
                }
                gl.glNormal3d(normal.getX(), normal.getZ(), normal.getY());
                gl.glVertex3d(bottomLeft.getX(), bottomLeft.getZ(), bottomLeft.getY());

                if (bottomRight.getX() > 0 && bottomRight.getY() > 0 && bottomRight.getX() < 282 && bottomRight.getY() < 319)
                {
                    normal = normalize(bottomRight);
                }
                gl.glNormal3d(normal.getX(), normal.getZ(), normal.getY());
                gl.glVertex3d(bottomRight.getX(), bottomRight.getZ(), bottomRight.getY());

                gl.glColor3f(red, green, blue);
                if (upperRight.getX() > 0 && upperRight.getY() > 0 && upperRight.getX() < 282 && upperRight.getY() < 319)
                {
                    normal = normalize(upperRight);
                }
                gl.glNormal3d(normal.getX(), normal.getZ(), normal.getY());
                gl.glVertex3d(upperRight.getX(), upperRight.getZ(), upperRight.getY());
            }
            red += colorRStep;
            green += colorGStep;
            blue += colorBStep;
        }
    }

    /*Checks the point to see if it is a corner point, border point, or normal point
    by using map.getNRows and map.getNCols.  Then acquires all vector cross products
    for nearby points, and averages them together, returning the final averaged vector 
    
    
    IGNORE THE CORNERS AND BORDERS
     */
    private Vector3d normalize(Point3d point)
    {
        Vector3d finalVector;
        Point3d upperPoint = new Point3d(point.getX(), point.getY() + 1,
                100* map.getHeight((int) point.getX(), (int) point.getY() + 1)
                / map.maxHeight());

        Point3d lowerPoint = new Point3d(point.getX(), point.getY() - 1,
                100 * map.getHeight((int) point.getX(), (int) point.getY() - 1)
                / map.maxHeight());

        Point3d leftPoint = new Point3d(point.getX() - 1, point.getY(),
                100 * map.getHeight((int) point.getX() - 1, (int) point.getY())
                / map.maxHeight());

        Point3d rightPoint = new Point3d(point.getX() + 1, point.getY(),
                100 * map.getHeight((int) point.getX() + 1, (int) point.getY())
                / map.maxHeight());

        Vector3d upperVector = new Vector3d(0,
                1.0f, upperPoint.getZ() - point.getZ());

        Vector3d lowerVector = new Vector3d(0,
                -1.0f, lowerPoint.getZ() - point.getZ());

        Vector3d leftVector = new Vector3d(-1.0f,
                0.0f, leftPoint.getZ() - point.getZ());

        Vector3d rightVector = new Vector3d(1.0f,
                0.0f, rightPoint.getZ() - point.getZ());

        Vector3d upperLeftNormal = new Vector3d(),
                lowerLeftNormal = new Vector3d(),
                lowerRightNormal = new Vector3d(),
                upperRightNormal = new Vector3d();
        upperLeftNormal.cross(upperVector, leftVector);
        lowerLeftNormal.cross(leftVector, lowerVector);
        lowerRightNormal.cross(lowerVector, rightVector);
        upperRightNormal.cross(rightVector, upperVector);
        upperLeftNormal.normalize();
        lowerLeftNormal.normalize();
        lowerRightNormal.normalize();
        upperRightNormal.normalize();
        finalVector = new Vector3d();
        finalVector.add(upperLeftNormal, lowerLeftNormal);
        finalVector.add(lowerRightNormal);
        finalVector.add(upperRightNormal);
        finalVector.normalize();

        return finalVector;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        char key = e.getKeyChar();
        double currentPositionHeight = 100 * map.getHeight(
                (int) camera.getZPos(), (int) camera.getXPos()) / map.maxHeight();
        if (key == 'd')
        {
            camera.strafeRight(2, currentPositionHeight);
        }
        if (key == 'a')
        {
            camera.strafeLeft(2, currentPositionHeight);
        }
        if (key == 'w')
        {
            camera.moveForward(2, currentPositionHeight);
        }
        if (key == 's')
        {
            camera.moveForward(-2, currentPositionHeight);
        }
        if (key == ' ')
        {
            for (double i = currentPositionHeight + 5 + velocity; i >= currentPositionHeight + 5; i = i - 9.81)
            {
                System.out.println(i);
                camera.jump(i, currentPositionHeight);
                glcanvas.display();
            }
        }
        //redisplay
        glcanvas.display();
    }

    public void mousePressed(MouseEvent event)
    {
        if (event.getButton() == 1)
        {
            targetReticule = true;
        }
        System.out.println("Click");
        glcanvas.display();
    }

    public void mouseReleased(MouseEvent event)
    {
        if (event.getButton() == 1)
        {
            targetReticule = false;
        }
        System.out.println("Release");
        glcanvas.display();
    }

    public void mouseClicked(MouseEvent event)
    {
    }

    public void mouseMoved(MouseEvent event)
    {
        mouseLocationX = event.getXOnScreen();
        mouseLocationY = event.getYOnScreen();;
        if (mouseLocationY < 240)
        {
            camera.lookUp(.05);
        } else if (mouseLocationY > 240)
        {
            camera.lookDown(.05);
        }
        if (mouseLocationX < 320)
        {
            camera.lookLeft(.05);
        } else if (mouseLocationX > 320)
        {
            camera.lookRight(.05);
        }
        //redisplay
        robot.mouseMove(320, 240);
        glcanvas.display();
    }

    public void mouseDragged(MouseEvent event)
    {
        return;
    }

    public void mouseEntered(MouseEvent event)
    {
        return;
    }

    public void mouseExited(MouseEvent event)
    {
        return;
    }

    public void mouseWheelMoved(MouseWheelEvent event)
    {
        System.out.println(event);
        int notches = event.getWheelRotation();
        if (notches < 0)
        {
            fov = fov - 1;
        } else
        {
            fov = fov + 1;
        }
        glcanvas.display();
    }
}