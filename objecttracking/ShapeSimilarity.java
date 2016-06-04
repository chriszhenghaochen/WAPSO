package objecttracking;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * Class written by Chris Chen
 */
public class ShapeSimilarity {

	// convert the rgb matrix to canny matrix
	public static Mat cannyTransfer(Mat img) {
		Mat imageGray = new Mat();
		Mat imageCny = new Mat();
		Imgproc.cvtColor(img, imageGray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.Canny(imageGray, imageCny, 10, 100, 3, true);
		return imageCny;
	}

	// find all contours of 1 canny matrix
	public static List<MatOfPoint> getContour(Mat img) {
		Mat src = cannyTransfer(img);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);

		return contours;
	}

	public static double compare(Mat img1, Mat img2) {
		List<MatOfPoint> contours1 = getContour(img1);
		List<MatOfPoint> contours2 = getContour(img2);
		double result = 0;
		if (contours1.size() == 0) {
			return 0;
		}
		for (int i = 0; i < contours1.size(); i++) {
			double likehood = 1;
			for (int j = 0; j < contours2.size(); j++) {
				double tmp = Imgproc.matchShapes(contours1.get(i),
						contours2.get(j), Imgproc.CV_CONTOURS_MATCH_I1, 0);
				if (tmp < likehood && tmp != 0) {
					likehood = tmp;
				}
				System.out.println(likehood);
			}
			result += likehood;
		}
		return 1 - result / contours1.size();
	}
}
