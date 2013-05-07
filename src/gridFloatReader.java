
import java.io.*;
import java.util.StringTokenizer;

public class gridFloatReader {

    public gridFloatReader(String basename) {
        //Create input stream objects
        try {
            if (verbose) {
                projectf = new FileInputStream(basename + ".prj");
            }
            headerf = new FileInputStream(basename + ".hdr");
            heightf = new FileInputStream(basename + ".flt");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        prjdata = new BufferedReader(new InputStreamReader(projectf));
        hdrdata = new BufferedReader(new InputStreamReader(headerf));
        heightdata = new DataInputStream(heightf);
        //Main processing method
        processfile();

    }

    public void processfile() {
        try {
            //Read prj part
            projection = prjdata.readLine();
            datum = prjdata.readLine();
            spheroid = prjdata.readLine();
            units = prjdata.readLine();
            zunits = prjdata.readLine();
            parameters = prjdata.readLine();
            //Read hdr part
            StringTokenizer nc = new StringTokenizer(hdrdata.readLine());
            nc.nextToken();
            ncolss = nc.nextToken();
            ncols = Integer.parseInt(ncolss);
            StringTokenizer nr = new StringTokenizer(hdrdata.readLine());
            nr.nextToken();
            nrowss = nr.nextToken();
            nrows = Integer.parseInt(nrowss);
            StringTokenizer xl = new StringTokenizer(hdrdata.readLine());
            xl.nextToken();
            xllCorners = xl.nextToken();
            xllCorner = Double.parseDouble(xllCorners);
            StringTokenizer yl = new StringTokenizer(hdrdata.readLine());
            yl.nextToken();
            yllCorners = yl.nextToken();
            yllCorner = Double.parseDouble(yllCorners);
            StringTokenizer cs = new StringTokenizer(hdrdata.readLine());
            cs.nextToken();
            cellsizes = cs.nextToken();
            cellsize = Double.parseDouble(cellsizes);
            yulcorner = yllCorner + cellsize * nrows;//Aux variable
            StringTokenizer nd = new StringTokenizer(hdrdata.readLine());
            nd.nextToken();
            nodatas = nd.nextToken();
            nodata = Integer.parseInt(nodatas);
            if (verbose) //Print file info
            {
                System.out.println("Cols:" + ncols + " x Rows:" + nrows + " = " + ncols * nrows + " points");
                System.out.println("Longitude: (" + xllCorner + "," + (xllCorner + ncols * cellsize) + ")");
                System.out.println("Latitude: (" + yllCorner + "," + (yllCorner + nrows * cellsize) + ")");
            }
            //Read the data
            maxHeight = Double.NEGATIVE_INFINITY;
            minHeight = Double.POSITIVE_INFINITY;
            height = new float[nrows][ncols];
            for (int i = 0; i < nrows; i++) {
                for (int j = 0; j < ncols; j++) {
                    height[i][j] = Float.intBitsToFloat(Integer.reverseBytes(heightdata.readInt())); //IEEE 32-bit float
                    //  System.out.println("Height at row: " + i + " Column: " + j + " " + height[i][j]);
                    if (height[i][j] == nodata) {
                        nodatacells++;
                    } else {
                        sumHeight += height[i][j];
                        datacells++;
                    }
                    if (height[i][j] > maxHeight) //New max height
                    {
                        maxHeight = height[i][j];
                        maxHeightyi = i;
                        maxHeightxi = j;
                        maxHeightX = longitude(maxHeightxi);
                        maxHeightY = latitude(maxHeightyi);
                    }
                    if (height[i][j] < minHeight) //New min height
                    {
                        minHeight = height[i][j];
                        minHeightyi = i;
                        minHeightxi = j;
                        minHeightX = longitude(j);
                        minHeightY = latitude(i);
                    }

                }
            }
            avgHeight = sumHeight / datacells;
            if (verbose) //Print data info
            {
                System.out.println("Max altitude is " + maxHeight + " m, " + m2f(maxHeight) + " ft Long(" + maxHeightX + ") Lat(" + maxHeightY + ")");
                System.out.println("Min altitude is " + minHeight + " m, " + m2f(minHeight) + " Long(" + minHeightX + ") Lat(" + minHeightY + ")");
                System.out.println("Avg altitude is " + avgHeight + " m, " + m2f(avgHeight) + " ft");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Calculate the latitude for height matrix element (i,j)
     * @param i
     */
    private double latitude(int i) {
        return yulcorner - (i + 0.5) * cellsize;
    }

    /**
     * Calculate the latitude for height matrix element (i,j)
     * @param j
     */
    private double longitude(int j) {
        return (j + 0.5) * cellsize + xllCorner;
    }

    public double f2m(double feet) {
        return feet * 0.3048;
    }

    public double m2f(double meter) {
        return meter * 3.2808399;
    }

    public float getHeight(int i, int j) {
        return height[i][j];
    }

    public int getNCols() {
        return ncols;
    }

    public int getNRows() {
        return nrows;
    }

    public double xllCorner() {
        return xllCorner;
    }

    public double yllCorner() {
        return yllCorner;
    }

    public double minHeight() {
        return minHeight;
    }

    public double maxHeight() {
        return maxHeight;
    }

    public double cellsize() {
        return cellsize;
    }

    public int minHeightX() {
        return minHeightxi;
    }

    public int minHeightY() {
        return minHeightyi;
    }

    public int maxHeightX() {
        return maxHeightxi;
    }

    public int maxHeightY() {
        return maxHeightyi;
    }
    //Instance variables
    private FileInputStream projectf, headerf, heightf;
    private BufferedReader prjdata, hdrdata;
    private DataInputStream heightdata;
//Metadata
    private String projection, datum, spheroid, units, zunits, parameters; //From prj file
    private String ncolss, nrowss, xllCorners, yllCorners, cellsizes, nodatas; //From hdr
    private int ncols, nrows, nodata, nodatacells, datacells;
    private double xllCorner, yllCorner, cellsize, yulcorner;
    private double maxHeight, minHeight, avgHeight, maxHeightX, maxHeightY, minHeightX, minHeightY, sumHeight;
    private int maxHeightxi, maxHeightyi, minHeightxi, minHeightyi;
    private boolean verbose = true;
    private float[][] height;

//This could be removed. Main is here only to test
    public static void main(String[] args) throws FileNotFoundException {
        new gridFloatReader("data/NED_86879038");
    }
}