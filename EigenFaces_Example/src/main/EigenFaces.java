package main;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class EigenFaces {
	
	private static boolean newEigens;
	private static Mat eig_vec;
	private static Mat eig_val;
	
	private static Mat imagesMat;
	private static Mat faces;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		learn("C:/Users/franc/git/EigenFaces_Example/EigenFaces_Example/pics");
		//learn("C:/Users/franc/OneDrive/Documents/University/Year 3/Final Year Project/pics/jpg");
		//learn("C:/Users/Francis/git/EigenFaces_Example/EigenFaces_Example/pics");
		//show_image(eig_vec.row(5));
		//show_image(imagesMat.row(5));
		show_images(eig_vec);
		
	}
	
	private static void show_images(Mat images)
	{
		int rows = images.rows();
		
		for(int i = 0; i < rows; i++)
		{
			show_image(images.row(i));		
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void show_image(Mat image)
	{
		if(image.rows() != 1 && image.cols() != (300 * 250))
		{
			//System.err.println("Size of image incorrect");
		}
		
		Mat pic = image.reshape(1, 300);
		
		//System.out.println(pic);
		
		BufferedImage bImage = Mat2BufferedImage(pic);
		
		displayImage(bImage);
	}
	
	private static BufferedImage Mat2BufferedImage(Mat m)
	{
		Mat final_img = new Mat();
		
		//Core.convertScaleAbs(m, final_img);
		
		m.copyTo(final_img);
		
		
		
		//System.out.println(final_img.submat(0, 5, 0, 5).dump());
		
		//output_matrix(final_img, "final_img.txt");
		
		if(final_img.type() != CvType.CV_8U)
		{
			final_img.convertTo(final_img, CvType.CV_8U);
		}
		
		//System.out.println(final_img.submat(0, 5, 0, 5).dump());
		
		int type = BufferedImage.TYPE_BYTE_GRAY;
	    if ( final_img.channels() > 1 ) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    }
	    
	    //System.out.println("Rows:" + final_img.rows() + ", \ncols: " + final_img.cols() + ", \nchannels: " + final_img.channels() + ",\ntotal: " + final_img.total());
	    
	    int bufferSize = final_img.channels()*final_img.cols()*final_img.rows();
	    byte [] b = new byte[bufferSize];
	    final_img.get(0,0,b); // get all the pixels
	    BufferedImage image = new BufferedImage(final_img.cols(),final_img.rows(), type);
	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	    
	    System.arraycopy(b, 0, targetPixels, 0, b.length);  
	    return image;
	}
	
	 public static void displayImage(Image img2)
	 {   
	     //BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
	     ImageIcon icon=new ImageIcon(img2);
	     JFrame frame=new JFrame();
	     frame.setLayout(new FlowLayout());        
	     frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);     
	     JLabel lbl=new JLabel();
	     lbl.setIcon(icon);
	     frame.add(lbl);
	     frame.setVisible(true);
	     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	 }
	
	private static void learn(String pathname)
	{
		//Mat testmat = new Mat();
		
		imagesMat = new Mat();
		
		//System.out.println("Reading images");
		//ArrayList<Mat> images = read_images(pathname);
		imagesMat = read_images_mat(pathname);
		
		
		//System.out.println("Averaging images");
		//Mat avg = mean_image(images);
		Mat avg = mean_image_mat(imagesMat);
		
		Mat evalues = new Mat();
		Mat evectors = new Mat();
		
		//System.out.println("Eigening images");
		eigen_images(imagesMat);
		//Core.eigen(avg, evalues, evectors);
		
		faces = new Mat();
		
		for(int i = 0; i < imagesMat.rows(); i++)
		{
			Mat column  = image2face(imagesMat.row(i), avg, eig_vec);
			
			double[] data = new double[(int) (column.rows() * column.cols())];
			
			//System.out.println(data.length);
			
			column.get(0, 0, data);
			
			faces.put(i, 0, data);
			
		}
		
		//output_matrix(eig_vec.row(0), "eig_vec.txt");
		
		System.out.println("Done");
		
	}

	private static Mat image2face(Mat image, Mat avg, Mat eig_vec2) {
		// TODO Auto-generated method stub
		
		Mat copy = new Mat();
		
		Core.subtract(image, avg, copy);
		
		//System.out.println(copy.dump());
		
		int rows = eig_vec2.rows();
		
		Mat face = Mat.zeros(rows, 1, eig_vec2.type());
		
		for(int i = 0; i < rows; i++)
		{
			double dotproduct = eig_vec2.row(i).dot(copy);
			
			//System.out.println(dotproduct);
			
			face.put(i, 0, dotproduct);
		}
		
		if(face.cols() != 1)
		{
			System.err.println("Face not a row vector");
		}
		
		return face;
	}
/*
	private static ArrayList<Mat> read_images(String pathname)
	{
		ArrayList<Mat> images = new ArrayList<>();
		BufferedImage img = null;
		
		File folder = new File(pathname);
		File[] listoffiles = folder.listFiles();
		
		for (File file : listoffiles) {
			try {
				img = ImageIO.read(file);
				byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
				
				Mat img_final = new Mat(img.getHeight(),img.getWidth(), CvType.CV_8UC3);
				
				//System.out.println("Putting pixels");
				img_final.put(0, 0, pixels);
				
				Imgproc.cvtColor(img_final, img_final, Imgproc.COLOR_RGB2GRAY);
				
				img_final.convertTo(img_final, CvType.CV_64FC1);
				
				images.add(img_final);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		return images;
	}
	*/
	private static Mat read_images_mat(String pathname)
	{
		Mat imagesMat = null;
		boolean imagesMatInit = false;
		
		File folder = new File(pathname);
		File[] files = folder.listFiles();
		
		int row = 0;
		
		for (File file : files) {
			//System.out.println(file.toString());
			
			Mat image = Imgcodecs.imread(file.toString(), Imgcodecs.IMREAD_GRAYSCALE);
			
			System.out.println(image);
			
			//show_image(image);
			
			if(image.channels() > 1)
			{
				Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);	
			}
			
			//output_matrix(image, (file + ".txt"));
			
			if(!imagesMatInit)
			{
				imagesMat = Mat.zeros(files.length, image.rows() * image.cols(), CvType.CV_64FC1);
				imagesMatInit = true;
				
				//System.out.println("About to output");
				
				//output_matrix(imagesMat, "zeroedimagesmat.txt");
			}
			
			//System.out.println(image.dump());
			
			image.convertTo(image, CvType.CV_64FC1);
			
			double[] data = new double[(int) (image.rows() * image.cols())];
			
			//System.out.println(data.length);
			
			image.get(0, 0, data);
			
			imagesMat.put(row, 0, data);
			
			//output_matrix(imagesMat, "imagesMat.txt");
			//show_image(imagesMat.row(row));
			
			//images.add(image);
			row++;
			
			//break;
		}
		
		return imagesMat;
	}
	
	private static Mat mean_image_mat(Mat imagesMat)
	{
		imagesMat.convertTo(imagesMat, CvType.CV_64FC1);
		
		Mat avgMat = new Mat(1, imagesMat.cols(), CvType.CV_64FC1);

		for (int i = 0; i < imagesMat.cols(); i++) {
			Mat column = imagesMat.col(i);
			
			//System.out.println(column.dump());
			
			double sum = 0;
			sum = Core.sumElems(column).val[0];
			//System.out.println(sum);
			
			//System.out.println("Sum = " + sum);
			double columnAvg = (double) sum / imagesMat.rows();
			//System.out.println("Average = " + columnAvg);
			
			avgMat.put(0, i, columnAvg);
		}
		
		//output_matrix(avgMat, "avgmat.txt");
		//System.out.println(avgMat.dump());
		
		return avgMat;
	}
	
	private static void eigen_images(Mat images)
	{
		newEigens = false;
		Mat copy = new Mat(images.rows(), images.cols(), images.type());
		
		images.copyTo(copy);
		
		int x = copy.rows();
		int y = copy.cols();
		
		copy = subtract_mean(copy);
		
		snapshot_eig(copy);
		
		
		//output_matrix(copy, "newcopy.txt");
	}
	
	private static Mat subtract_mean(Mat images)
	{
		Mat copy = new Mat(images.rows(), images.cols(), images.type());
		
		int rows = copy.rows();
		int cols = copy.cols();
		
		Mat avg = mean_image_mat(images);
		//output_matrix(avg, "avg_eigen.txt");
		for(int i = 0; i < rows; i++)
		{
			Mat row = new Mat();
			Core.subtract(images.row(i), avg, row);
			
			double[] data = new double[(int) (rows * cols)];
			
			//System.out.println(data.length);
			
			row.get(0, 0, data);
			
			copy.put(i, 0, data);
		}
		
		//output_matrix(copy, "subtracted_mean.txt");
		
		return copy;
	}
	
	private static void snapshot_eig(Mat images)
	{
		Mat snap_vec = new Mat();
		Mat snap_val = new Mat();
		
		Mat copy = new Mat();
		Mat covar = new Mat();
		Mat mean = new Mat();
		
		int rows = images.rows();
		int cols = images.cols();
		
		copy = images.t();
		Core.calcCovarMatrix(copy, covar, mean, Core.COVAR_COLS, images.type());
		
		//output_matrix(covar, "covar.txt");
		
		Core.eigen(covar, snap_val, snap_vec);
		
		//output_matrix(snap_val, "snap_val.txt");
		//output_matrix(snap_vec, "snap_vec.txt");
		
		eig_vec = Mat.zeros(rows, cols, images.type());
		Mat eig_temp = new Mat(rows, cols, images.type());
		
		for(int i = 0; i < rows; i++)
		{
			Mat row = eig_vec.row(i);
			for(int j = 0; j < rows; j++)
			{
				Mat temp = new Mat();
				Core.multiply(images.row(j), new Scalar(snap_vec.get(j, i)), temp);
				
				//System.out.println("multiplying by " + snap_vec.get(j, i)[0]);
				
				//output_matrix(temp, "temp.txt");
				
				Core.add(row, temp, row);
				
				//output_matrix(row, "row.txt");
			}
			
			double[] data = new double[(int) (rows * cols)];
			
			//System.out.println(data.length);
			
			//output_matrix(row, "row.txt");
			
			row.get(0, 0, data);
			
			eig_temp.put(i, 0, data);
		}
		
		eig_val = snap_val.diag();
		
		 
		Mat newmat = normalise(eig_temp);
		
		newmat.copyTo(eig_vec);
		
		//System.out.println(eig_vec.row(0).dump());
		
		newEigens = true;
	}
	
	private static Mat normalise(Mat matrix)
	{
		
		int rows = matrix.rows();
		int cols = matrix.cols();
		Mat norm = new Mat(rows, cols, matrix.type());
		
		for(int i = 0; i < rows; i++)
		{
			Mat row = matrix.row(i);
			
			//System.out.println(row.dump());
			
			//Core.normalize(row, row);
			
			double normdouble = Core.norm(row, Core.NORM_L2);
			
			//System.out.println("normRow: " + normRow.dump());
			
			Core.divide(normdouble, row, row);
			
			//System.out.println("row: " + row.dump());
			
			//append row into new matrix
			double[] data = new double[(int) (rows * cols)];
			
			row.get(0, 0, data);
			
			norm.put(i, 0, data);
			
			//output_matrix(norm, "norm_" + i +".txt");
		}
		//System.out.println(norm);
		//output_matrix(norm, "norm.txt");
		
		return norm;
	}
	
	public static void output_matrix(Mat matrix, String filename)
	{
		File output = new File(filename);
		
		String outputstring = matrix.dump();
		try {
			System.out.println("Writing file");
			FileWriter outputstream = new FileWriter(output);
			
			outputstream.write(outputstring);
			
			outputstream.close();
			
			System.out.println("Output completed");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
