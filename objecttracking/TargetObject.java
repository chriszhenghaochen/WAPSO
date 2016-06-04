package objecttracking;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * Class written by Chris Chen
 */
public class TargetObject {
	protected int x; // initial x
	protected int y; // initial y
	protected double height; // initial height
	protected double width; // initial width
	protected Rect rect; // initial rectangle
	// protected Mat histogram; // initial histogram
	protected List<Mat> histogram; // initial histogram
	protected Mat frame;

	public TargetObject(Rect rect, Mat frame) {
		this.rect = rect;
		this.frame = frame;
		this.x = (int) Math.round(rect.x + rect.width / 2);
		this.y = (int) Math.round(rect.y + rect.height / 2);
		this.height = Math.round(rect.height);
		this.width = Math.round(rect.width);

		Mat image = frame.submat(rect);
		try {
			this.histogram = Utilities.getHistogram(image);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public Rect getRect() {
		return rect;
	}

	public void setRect(Rect rect) {
		this.rect = rect;
	}

	public List<Mat> getHistogram() {
		return histogram;
	}

	public void setHistogram(List<Mat> histogram) {
		this.histogram = histogram;
	}

	public Mat getFrame() {
		return frame;
	}

	public void setFrame(Mat frame) {
		this.frame = frame;
	}

	@Override
	public String toString() {
		return "{" + x + ", " + y + ", " + width + "x" + height + "}";
	}
}
