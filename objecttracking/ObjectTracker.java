package objecttracking;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.VideoCapture;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Primary class used in this research project.
 * Run the main method specified here.
 */
public class ObjectTracker {
	public static boolean DEBUG = false;
	public static boolean IS_LOOPING = false;

	public static int NUMBER_OF_PARTICLES = 20;
	public static int NUMBER_OF_ITERATIONS = 20;
	public static int SEARCHING_WINDOWS_SIZE = 30;

	private String filePath;
	private String algorithm;
	private int trackNumber;
	private int particles;
	private int iterations;
	private int searchWindowSize;

	public ObjectTracker(String filePath, String algorithm, int trackNumber,
						 int particles, int iterations, int searchWindowSize) {
		this.filePath = filePath;
		this.algorithm = algorithm;
		this.trackNumber = trackNumber;
		this.particles = particles;
		this.iterations = iterations;
		this.searchWindowSize = searchWindowSize;
	}

	public void run() {
		// Prepare window for use
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		VideoPanel panel1 = new VideoPanel();
		window.setContentPane(panel1);
		window.setBounds(0, 0, window.getWidth(), window.getHeight());
		window.setVisible(true);

		// Prepare data structures for video processing
		TargetObject[] targets = new TargetObject[trackNumber];
		Particle[] gbests = new Particle[trackNumber];
		Particle[] gBestFirstFrames = new Particle[trackNumber];
		Particle[] gBestLastFrames = new Particle[trackNumber];
		Particle particles[][] = new Particle[trackNumber][NUMBER_OF_PARTICLES];
		VideoCapture video = new VideoCapture(this.filePath);

		if (video.isOpened()) {

			ArrayList<Mat> frames = new ArrayList<>();
			int i = 0;
			int frameCount = 0;
			boolean isFinished = false; // Mechanism for detecting end of video
			int videoLengthInFrames = (int) video.get(7); // CV_CAP_PROP_FRAME_COUNT

			// Loop if setting says so, or until finished
			while (IS_LOOPING || !isFinished) {

				// Retrieve frame data as a matrix
				Mat frame = new Mat();
				video.read(frame);

				// Terminate if no data in matrix
				if (!frame.empty()) {
					if (DEBUG) {
						System.out.println("frame=" + frameCount++);
					}
					frames.add(frame);

					// Render frame to panel
					panel1.setimagewithMat(frame);
					window.setSize(frame.width(), frame.height());

					if (i == 0) {
						if (DEBUG) {
							System.out.println("frame width=" + frame.width() + ", frame height=" + frame.height());
						}
						panel1.addMat(frame);
						Rect[] objectRects = new Rect[trackNumber];

						for (int j = 0; j < trackNumber; j++) {
							objectRects[j] = panel1.rect();
							System.out.println("Select an area on the video by dragging with the mouse");
							while (objectRects[j] == null) {
								try {
									objectRects[j] = panel1.rect();
									Thread.sleep(15);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							targets[j] = new TargetObject(objectRects[j],
									frame.clone());
							Particle.gBestFirstFrame = new Particle(
									targets[j].frame, targets[j].x,
									targets[j].y, targets[j],
									this.searchWindowSize);
							gBestFirstFrames[j] = Particle
									.clone(Particle.gBestFirstFrame);
							for (int p = 0; p < this.particles; p++) {
								Particle particle = new Particle(frame,
										targets[j], this.searchWindowSize);
								particles[j][p] = particle;
								particle.update();
							}
							gbests[j] = Particle.clone(Particle.gBest);
							gBestLastFrames[j] = Particle
									.clone(Particle.gBestLastFrame);
							System.out.println(j + 1 + " objects selected");
							panel1.reset();
							j++;
						}
					} else {
						for (int j = 0; j < trackNumber; j++) {
							if (i == 1) {
								Particle.gBestLastFrame = Particle
										.clone(gBestLastFrames[j]);
								gbests[j] = Particle
										.clone(Particle.gBestLastFrame);
								Particle.gBest = Particle.clone(gbests[j]);
								// Particle.gBest = Particle
								// .clone(Particle.gBestLastFrame);
							}

							if (i >= 2) {
								Particle.gBest = Particle.clone(gbests[j]);
								gBestLastFrames[j] = Particle.clone(gbests[j]);
								Particle.gBestLastFrame = Particle
										.clone(gBestLastFrames[j]);
								// Particle.gBestLastFrame = new
								// Particle(target.frame, target.x, target.y,
								// target, SEARCHING_WINDOWS_SIZE);
								for (Particle particle : particles[j]) {
									particle.vx = 0;
									particle.vy = 0;
									particle.relocate();
									particle.pBest = particle;
									particle.fitness = 0;
								}
							}
							Particle.gBest.fitness = 0;

							for (Particle particle : particles[j]) {
								particle.frame = frame;
							}
							Particle.gBest.frame = frame;

							for (int x = 0; x < this.iterations; x++) {
								for (Particle particle : particles[j]) {
									particle.move(this.algorithm);
								}
								for (Particle particle : particles[j]) {
									particle.update();
								}
							}
							gbests[j] = Particle.clone(Particle.gBest);
							gBestLastFrames[j] = Particle
									.clone(Particle.gBestLastFrame);

							Particle.gBest.display();
							//System.out.println(Particle.gBest);
						}
					}
					panel1.setimagewithMat(frame);
					window.setVisible(true);
					window.repaint();
					i++;
				}

				// Detect end of video
				if (frameCount > videoLengthInFrames) {
					isFinished = true;
				}

				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.exit(0);
		}
	}

	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java ObjectTracker <path_to_video> <num_objects_to_track>");
		} else {
			if (args.length == 1) {
				new ObjectTracker(args[0], Algorithm.PSO, 1,
						NUMBER_OF_PARTICLES, NUMBER_OF_ITERATIONS, SEARCHING_WINDOWS_SIZE).run();
			} else if (args.length == 2) {
				new ObjectTracker(args[0], Algorithm.PSO, Integer.parseInt(args[1]),
						NUMBER_OF_PARTICLES, NUMBER_OF_ITERATIONS, SEARCHING_WINDOWS_SIZE).run();
			} else {
				System.err.println("Usage: java ObjectTracker <path_to_video> <num_objects_to_track>");
			}
		}
	}

}
