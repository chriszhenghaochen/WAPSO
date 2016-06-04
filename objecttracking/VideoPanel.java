package objecttracking;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

/**
 * Class written by Chris Chen
 */
class VideoPanel extends JPanel implements MouseListener {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	public int x0 = 0;
	public int y0 = 0;
	public int x1 = 0;
	public int y1 = 0;
	public Mat frame;

	// Create a constructor method
	public VideoPanel() {
		super();
		this.addMouseListener(this);
	}

	public void reset() {
		x0 = 0;
		y0 = 0;
		x1 = 0;
		y1 = 0;
	}

	private BufferedImage getimage() {
		return image;
	}

	public void setimage(BufferedImage newimage) {
		image = newimage;
		return;
	}

	public void setimagewithMat(Mat newimage) {
		image = this.matToBufferedImage(newimage);
		return;
	}

	/**
	 * Converts/writes a Mat into a BufferedImage.
	 * 
	 * @param matrix
	 *            Mat of type CV_8UC3 or CV_8UC1
	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
	 */
	public BufferedImage matToBufferedImage(Mat matrix) {
		int cols = matrix.cols();
		int rows = matrix.rows();
		int elemSize = (int) matrix.elemSize();
		byte[] data = new byte[cols * rows * elemSize];
		int type;
		matrix.get(0, 0, data);
		switch (matrix.channels()) {
		case 1:
			type = BufferedImage.TYPE_BYTE_GRAY;
			break;
		case 3:
			type = BufferedImage.TYPE_3BYTE_BGR;
			// bgr to rgb
			byte b;
			for (int i = 0; i < data.length; i = i + 3) {
				b = data[i];
				data[i] = data[i + 2];
				data[i + 2] = b;
			}
			break;
		default:
			return null;
		}
		BufferedImage image2 = new BufferedImage(cols, rows, type);
		image2.getRaster().setDataElements(0, 0, cols, rows, data);
		return image2;
	}

	public Rect rect() {
		if (!(x0 != 0 && y0 != 0 && x1 != 0 && y1 != 0)) {
			return null;
		}
		Core.rectangle(frame, new Point(x0, y0), new Point(x1, y1), new Scalar(
				0, 255, 0), 2, 8, 0);
		return new Rect(new Point(x0, y0), new Point(x1, y1));

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// BufferedImage temp=new BufferedImage(640, 480,
		// BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage temp = getimage();
		// Graphics2D g2 = (Graphics2D)g;
		if (temp != null)
			g.drawImage(temp, 0, 0, temp.getWidth(), temp.getHeight(), this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void addMat(Mat frame) {
		this.frame = frame;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		int x = e.getX();
		int y = e.getY();
		if (x0 == 0 && y0 == 0) {
			x0 = x;
			y0 = y;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if (x0 != 0 && y0 != 0 && x1 == 0 && y1 == 0) {
			int x = e.getX();
			int y = e.getY();
			x1 = x;
			y1 = y;
			System.out.println("2 points selected");
			Core.rectangle(frame, new Point(x0, y0), new Point(x1, y1),
					new Scalar(0, 0, 255), 2, 8, 0);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}