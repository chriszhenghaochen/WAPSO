package objecttracking;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * Class written by Chris Chen
 */
public class BoW {

	public static double detector(Mat img1, Mat img2) {
		// here I comment the gray scale, but it is still optional
		
		// Mat imageGray1 = new Mat(img1.height(), img1.width(),
		// CvType.CV_8UC2);
		// ;
		// Mat imageGray2 = new Mat(img2.height(), img2.width(),
		// CvType.CV_8UC2);

		// gray scale
		// Imgproc.cvtColor(img1, imageGray1, Imgproc.COLOR_BGR2GRAY);
		// Imgproc.cvtColor(img2, imageGray2, Imgproc.COLOR_BGR2GRAY);
		
		// fast detect to detect keypoints 
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);// SIFT
																				// ,SURF
																				// ,FAST,// ORB
		
		//use SIFT as extractor to extract keypoint 
		DescriptorExtractor descriptor = DescriptorExtractor
				.create(DescriptorExtractor.SIFT);// SIFT ,SURF ,FAST, ORB
		DescriptorMatcher matcher = DescriptorMatcher
				.create(DescriptorMatcher.FLANNBASED);// BRUTEFORCE,
														// FLANNBASED

		// first image
		Mat descriptors1 = new Mat();
		MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

		detector.detect(img1, keypoints1);
		descriptor.compute(img1, keypoints1, descriptors1);

		// second image
		Mat descriptors2 = new Mat();
		MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

		detector.detect(img2, keypoints2);
		descriptor.compute(img2, keypoints2, descriptors2);

		// matcher should include 2 different image's descriptors
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(descriptors1, descriptors2, matches);

		// calculate the max and min distance if keypoints
		double Max_distance = 0;
		double Min_distance = 1000;
		List<DMatch> matchesList = matches.toList();
		for (int i = 0; i < descriptors1.rows(); i++) {
			double tmp = matchesList.get(i).distance;
			if (tmp > Max_distance) {
				Max_distance = tmp;
			}
			if (tmp < Min_distance) {
				Min_distance = tmp;
			}
		}

		// find good match matrix
		List<DMatch> good_matches = new LinkedList<DMatch>();
		if (Min_distance > 100) {
			for (int i = 0; i < descriptors1.rows(); i++) {
				if (matchesList.get(i).distance <= 3 * Min_distance) {
					((LinkedList<DMatch>) good_matches).addLast(matchesList
							.get(i));
				}
			}
		} else {
			for (int i = 0; i < descriptors1.rows(); i++) {
				if (matchesList.get(i).distance <= 300) {
					((LinkedList<DMatch>) good_matches).addLast(matchesList
							.get(i));
				}
			}
		}

		double similarity1 = ((double) good_matches.size() / (double) descriptors1
				.rows());
		double similarity2 = 0;
		for (int i = 0; i < good_matches.size(); i++) {
			double tmp = 1000 - good_matches.get(i).distance;
			similarity2 += tmp;
		}
		similarity2 = similarity2 / good_matches.size()/1000;
		System.out.println(Max_distance);
		System.out.println(Min_distance);
		System.out.println(descriptors1.rows());
		System.out.println(good_matches.size());
		System.out.println(similarity1);
		System.out.println(similarity2);
		System.out.println(similarity1 * similarity2);
		Scalar RED = new Scalar(255, 0, 0);
		Scalar GREEN = new Scalar(0, 255, 0);
		
		// output image of distances of keypoints
		Mat outputImg = new Mat();
		MatOfByte drawnMatches = new MatOfByte();
		// this will draw all matches, works fine
		Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matches,
				outputImg, GREEN, RED, drawnMatches,
				Features2d.NOT_DRAW_SINGLE_POINTS);
		
		//show all keypoints detected by 1st images 
		Mat featuredImg = new Mat();
		Scalar kpColor = new Scalar(255, 159, 10);// this will be color of
													// keypoints
		// featuredImg will be the output of first image
		Features2d.drawKeypoints(img1, keypoints1, featuredImg, kpColor, 0);
		
		Highgui.imwrite(
				"E:/bachelor year/year2 semester2/summer project/match/matchsample"
						+ ".png", outputImg);
		Highgui.imwrite(
				"E:/bachelor year/year2 semester2/summer project/match/featuresample"
						+ ".png", featuredImg);
		return similarity1 * similarity2;
	}
}
