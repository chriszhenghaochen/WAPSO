package objecttracking;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

/**
 * Class written by Chris Chen
 */
public class Particle {
	public static Random rn = new Random();

	public TargetObject targetObject;

	public static double w = 1;
	public static double c1 = 2;
	public static double c2 = 2;

	public static double Cw = 0.1;
	public static double Cp = 0.3;
	public static double Cg = 0.6;

	protected static Particle gBest;
	protected static Particle gBestFirstFrame;
	protected static Particle gBestLastFrame;
	protected Particle pBest;

	protected Mat frame;
	protected int frameIndex;

	protected int x;
	protected int y;
	protected int xMax; // max x - video width
	protected int yMax; // max y - video height
	protected int xMin;
	protected int yMin;

	protected double vx; // velocity of x axis
	protected double vy; // velocity of y axis
	protected int searchingWindowSize; // a searching window is the window using
										// gBest from last frame as central
										// point

	protected double fitness;

	protected Rect rect;

	// Initialize a particle with random x and y
	public Particle(Mat frame, TargetObject targetObject,
			int searchingWindowSize) {
		this.frame = frame;
		this.searchingWindowSize = searchingWindowSize;
		this.targetObject = targetObject;
		this.xMax = (int) (frame.width() - targetObject.getWidth() / 2);
		this.yMax = (int) (frame.height() - targetObject.getHeight() / 2);
		this.xMin = (int) (targetObject.getWidth() / 2);
		this.yMin = (int) (targetObject.getHeight() / 2);
		vx = 0;
		vy = 0;
		fitness = 0;

		this.gBestLastFrame = new Particle(frame, targetObject.getX(),
				targetObject.getY(), targetObject, searchingWindowSize);
		this.gBest = new Particle(frame, targetObject.getX(),
				targetObject.getY(), targetObject, searchingWindowSize);
		this.relocate();
		this.pBest = this;
	}

	public Particle(Mat frame, int x, int y, TargetObject targetObject,
			int searchingWindowSize) {
		this.frame = frame;
		this.x = x;
		this.y = y;
		this.searchingWindowSize = searchingWindowSize;
		this.targetObject = targetObject;
		vx = 0;
		vy = 0;
		fitness = 0;
		this.rect = getRect(x, y);
	}

	private Rect getRect(int x, int y) {
		int x0 = (int) Math.round(x - 0.5 * targetObject.getWidth());
		int y0 = (int) Math.round(y - 0.5 * targetObject.getHeight());
		int x1 = (int) Math.round(x + 0.5 * targetObject.getWidth());
		int y1 = (int) Math.round(y + 0.5 * targetObject.getHeight());
		// modify x0,y0,x1y1 by frame of video
		Point p1 = new Point(x0, y0);
		Point p2 = new Point(x1, y1);
		return new Rect(p1, p2);
	}

	// randomly relocate particle
	public void relocate() {
		// Define searching window
		int xLowerBound = gBestLastFrame.x - searchingWindowSize;
		if (xLowerBound < xMin)
			xLowerBound = xMin;

		int xUpperBound = gBestLastFrame.x + searchingWindowSize;
		if (xUpperBound > xMax)
			xUpperBound = xMax;

		int yLowerBound = gBestLastFrame.y - searchingWindowSize;
		if (yLowerBound < yMin)
			yLowerBound = yMin;

		int yUpperBound = gBestLastFrame.y + searchingWindowSize;
		if (yUpperBound > yMax)
			yUpperBound = yMax;
		this.x = rn.nextInt(xUpperBound - xLowerBound + 1) + xLowerBound;
		this.y = rn.nextInt(yUpperBound - yLowerBound + 1) + yLowerBound;
		this.rect = getRect(x, y);
	}

	public void move(String algorithm) {
		/** calculate velocity **/
		// Define searching window
		int xLowerBound = gBestLastFrame.x - searchingWindowSize;
		if (xLowerBound < xMin)
			xLowerBound = xMin;

		int xUpperBound = gBestLastFrame.x + searchingWindowSize;
		if (xUpperBound > xMax)
			xUpperBound = xMax;

		int yLowerBound = gBestLastFrame.y - searchingWindowSize;
		if (yLowerBound < yMin)
			yLowerBound = yMin;

		int yUpperBound = gBestLastFrame.y + searchingWindowSize;
		if (yUpperBound > yMax)
			yUpperBound = yMax;

		if (Algorithm.PSO.equalsIgnoreCase(algorithm)) {
			vx = w * vx + c1 * Math.random() * (pBest.x - x) + c2
					* Math.random() * (gBest.x - x);
			vy = w * vy + c1 * Math.random() * (pBest.y - y) + c2
					* Math.random() * (gBest.y - y);
			x = (int) Math.round(x + vx);
			y = (int) Math.round(y + vy);
		} else {
			double r1 = Math.random();
			if (r1 >= Cw && r1 < Cp)
				x = this.pBest.x;
			else if (r1 >= Cp && r1 < Cg)
				x = gBest.x;
			else if (r1 >= Cg && r1 <= 1)
				x = rn.nextInt(xUpperBound - xLowerBound + 1) + xLowerBound;

			double r2 = Math.random();
			if (r2 >= Cw && r2 < Cp)
				y = this.pBest.x;
			else if (r2 >= Cp && r2 < Cg)
				y = gBest.y;
			else if (r2 >= Cg && r2 <= 1)
				y = rn.nextInt(yUpperBound - yLowerBound + 1) + yLowerBound;
		}

		if (x < xLowerBound)
			x = xLowerBound;
		if (x > xUpperBound)
			x = xUpperBound;
		if (y < yLowerBound)
			y = yLowerBound;
		if (y > yUpperBound)
			y = yUpperBound;

		rect = this.getRect(x, y);
	}

	public void update() {
		try {
			this.fitness = Utilities.compareParticles(gBestLastFrame, this);
			this.fitness += Utilities.compareParticles(gBestFirstFrame, this);
			this.fitness = this.fitness / 2;
			// this.fitness =
			// Utilities.compareImages(frame.submat(targetObject.rect),
			// frame.submat(this.rect));
			if (this.pBest == null || this.fitness > pBest.fitness) {
				this.pBest = clone(this);
			}
			if (gBest == null || this.fitness > gBest.fitness) {
				gBest = clone(this);
			}
		} catch (Exception e) {
			System.out.println(this);
			e.printStackTrace();
		}
	}

	/* call openCV to display the box on video screen */
	public void display() {
		int x0 = (int) Math.round(x - 0.5 * targetObject.getWidth());
		int y0 = (int) Math.round(y - 0.5 * targetObject.getHeight());
		int x1 = (int) Math.round(x + 0.5 * targetObject.getWidth());
		int y1 = (int) Math.round(y + 0.5 * targetObject.getHeight());
		// modify x0,y0,x1y1 by frame of video
		Point p1 = new Point(x0, y0);
		Point p2 = new Point(x1, y1);
		rect = new Rect(p1, p2);
		if (this.hasSamePosition(gBest)) {
			Core.rectangle(frame, p1, p2, new Scalar(0, 0, 255), 2, 8, 0);
		} else {
			Core.rectangle(frame, p1, p2, new Scalar(255, 255, 0), 2, 8, 0);
		}
	}

	public static Particle clone(Particle p) {
		Particle copy = new Particle(p.frame, p.x, p.y, p.targetObject, p.searchingWindowSize);
		copy.fitness = p.fitness;
		return copy;
	}

	public boolean hasSamePosition(Particle p) {
		if (this.x == p.x && this.y == p.y)
			return true;
		else
			return false;
	}

	public String toString() {
		String str = "x=" + x + ", y=" + y + "  rect=" + rect + " Fitness=" + fitness;
		return str;
	}

}
