package objecttracking;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Utilities {
	
	
	/**using square-root of sum of squared error
	 * Chris Chen
	 */	
	public static double SSE(Mat A, Mat B){
		
		if (A.rows() > 0 && A.rows() == B.rows() && A.cols() > 0 && A.cols() == B.cols()) {
	        // Calculate the L2 relative error between the 2 images.
	        double errorL2 = Core.norm(A, B, Core.NORM_L2);
	        // Convert to a reasonable scale, since L2 error is summed across all pixels of the image.
	        double similarity = errorL2 / (double)(A.rows() * A.cols());
	        return 100 - similarity;
	    }
	    else {
//	        cout << "WARNING: Images have a different size in 'getSimilarity()'." << endl;
	        return 0;  // Return a bad value
	    }
	}
	/** using PSNR: Peak signal-to-noise ratio
	 *  Chris Chen
	 */
	public static double getPSNR(Mat inputA, Mat inputB){
		Mat s1 = new Mat();
		Core.absdiff(inputA, inputB, s1);
		s1.convertTo(s1, CvType.CV_32F);
		s1 = s1.mul(s1);
		Scalar s = Core.sumElems(s1);
		double sse = s.val[0] + s.val[1] + s.val[2];
		
		 if(sse <= 1e-10) // for small values return zero
		        return 0;
		 
		 else{			 
			 double  mse =sse /(double)(inputA.channels() * inputB.total());
		     double psnr = 10.0*Math.log10((255*255)/mse);
		     return psnr;
		 }
	}
		

	/**
	 * Code ported to Java from an OpenCV C++ example: http://goo.gl/Xk6301
	 *
	 * (Author: Eric Liu)
	 * @param inputA input matrix A
	 * @param inputB input matrix B
     * @return a scalar value representing the MSSIM index
     */
	public static Scalar getMSSIM(Mat inputA, Mat inputB) {
		// Setup constants
		final double C1 = 6.5025;
		final double C2 = 58.5225;
		int d = CvType.CV_32F;

		Mat iA = new Mat();
		Mat iB = new Mat();
		inputA.convertTo(iA, d);
		inputB.convertTo(iB, d);

		Size kernelSize = new Size(11, 11);
		Mat imgBSquared = iB.mul(iB);
		Mat imgASquared = iA.mul(iA);
		Mat imgAMulB = iA.mul(iB);

		Mat muA = new Mat();
		Mat muB = new Mat();
		Imgproc.GaussianBlur(iA, muA, kernelSize, 1.5);
		Imgproc.GaussianBlur(iB, muB, kernelSize, 1.5);

		Mat muASquared = muA.mul(muA);
		Mat muBSquared = muB.mul(muB);
		Mat muAMulB = muA.mul(muB);

		Mat sigmaASquared = new Mat();
		Mat sigmaBSquared = new Mat();
		Mat sigmaAMulB = new Mat();

		Imgproc.GaussianBlur(imgASquared, sigmaASquared, kernelSize, 1.5);
		Core.subtract(sigmaASquared, muASquared, sigmaASquared);

		Imgproc.GaussianBlur(imgBSquared, sigmaBSquared, kernelSize, 1.5);
		Core.subtract(sigmaBSquared, muBSquared, sigmaBSquared);

		Imgproc.GaussianBlur(imgAMulB, sigmaAMulB, kernelSize, 1.5);
		Core.subtract(sigmaAMulB, muAMulB, sigmaAMulB);

		Mat t1 = new Mat();
		Mat t2 = new Mat();
		Mat t3 = new Mat();

		// Goal: t3 = ((2*mu1_mu2 + C1).*(2*sigma12 + C2))
		Core.multiply(muAMulB, new Scalar(2), t1);
		Core.add(t1, new Scalar(C1), t1);
		Core.multiply(sigmaAMulB, new Scalar(2), t2);
		Core.add(t2, new Scalar(C2), t2);
		Core.multiply(t1, t2, t3);

		// Goal: t1 =((mu1_2 + mu2_2 + C1).*(sigma1_2 + sigma2_2 + C2))
		Core.add(muASquared, muBSquared, t1);
		Core.add(t1, new Scalar(C1), t1);
		Core.add(sigmaASquared, sigmaBSquared, t2);
		Core.add(t2, new Scalar(C2), t2);
		Core.multiply(t1, t2, t1);

		// Goal ssim_map = t3./t1
		Mat ssimMap = new Mat();
		Core.divide(t3, t1, ssimMap);

		Scalar mssim = Core.mean(ssimMap);
		return mssim;
	}

	/*
		Coded by Chris Chen
	 */
	public static double compareParticles(Particle p1, Particle p2)
			throws Exception {
		Mat image1 = p1.frame.submat(p1.rect);
		Mat image2 = p2.frame.submat(p2.rect);
		return compareImages(image1, image2);
//		return ShapeSimilarity.compare(image1, image2);	
//		return BoW.detector(image1, image2);
	}


	/**
	 * Uses an image processing algorithm to compare two input image signals.
	 * It will compute the fitness value for each dimension of the given matrices
	 * and return the mean fitness value.
	 * @param img1 input image signal A
	 * @param img2 input image signal B
	 * @return the fitness value between 0.0 and 1.0 (higher is better)
	 * @throws Exception
     */
	public static double compareImages(Mat img1, Mat img2) throws Exception {
//		List<Mat> H1 = getHistogram(img1);
//		List<Mat> H2 = getHistogram(img2);
//		int size = H1.size();
//		double likelihood = 0;
//		for (int i = 0; i < size; i++) {
//			// --------------------------------------------------------------------------------------
//			likelihood += compareHistograms(H1.get(i), H2.get(i)); 	// BGR Histograms (Bhattacharyya)
////			likelihood += getMSSIM(H1.get(i), H2.get(i)).val[0];	// MSSIM index on Histograms				
//
//			
// 			// --------------------------------------------------------------------------------------
//		}
//		return likelihood / size; // Mean fitness-value
		
//		return getMSSIM(img1, img2).val[0];    // MSSIM index on Image signals
//		return getPSNR(img1, img2);
		return SSE(img1, img2);
	}

	/*
		Coded by Chris Chen
	 */
	public static double compareHistograms(Mat histogram1, Mat histogram2) {
		double text = Imgproc.compareHist(histogram1, histogram2, Imgproc.CV_COMP_BHATTACHARYYA);
		return 1.0 - text;
	}

	/*
		Coded by Chris Chen
	 */
	public static List<Mat> getHistogram(Mat img) throws Exception {
		Mat src = new Mat(img.height(), img.width(), CvType.CV_8UC2);
		Imgproc.cvtColor(img, src, Imgproc.COLOR_BGR2HSV);
		Vector<Mat> bgr_planes = new Vector<Mat>();
		Core.split(src, bgr_planes);

		MatOfInt histSize = new MatOfInt(256);
		final MatOfFloat histRange = new MatOfFloat(0f, 256f);
		boolean accumulate = false;
		Mat h_hist = new Mat();
		Mat s_hist = new Mat();
		Mat v_hist = new Mat();
		Imgproc.calcHist(bgr_planes, new MatOfInt(0), new Mat(), h_hist,
				new MatOfInt(16), new MatOfFloat(0f, 180f), accumulate);
		Imgproc.calcHist(bgr_planes, new MatOfInt(1), new Mat(), s_hist,
				new MatOfInt(8), new MatOfFloat(0f, 256f), accumulate);
		Imgproc.calcHist(bgr_planes, new MatOfInt(2), new Mat(), v_hist,
				new MatOfInt(8), new MatOfFloat(0f, 256f), accumulate);

		Core.normalize(h_hist, h_hist, 0, 1, Core.NORM_MINMAX, -1,
				new Mat());
		Core.normalize(s_hist, s_hist, 1, img.rows(), Core.NORM_MINMAX, -1,
				new Mat());
		Core.normalize(v_hist, v_hist, 1, img.rows(), Core.NORM_MINMAX, -1,
				new Mat());

		List<Mat> results = new ArrayList<Mat>();
		results.add(h_hist);
		results.add(s_hist);
		results.add(v_hist);
		return results;
	}
}
