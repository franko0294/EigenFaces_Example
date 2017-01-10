package main;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//learn("C:/Users/franc/OneDrive/Documents/University/Year 3/Final Year Project/pics/jpg");
		learn("C:/Users/Francis/git/EigenFaces_Example/EigenFaces_Example/pics");
	}
	
	private static void learn(String pathname)
	{
		//Mat testmat = new Mat();
		
		Mat imagesMat = new Mat();
		
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
		
		for(int i = 0; i < imagesMat.rows(); i++)
		{
			
		}
		
		System.out.println("Done");
		
	}

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
	
	private static Mat read_images_mat(String pathname)
	{
		Mat imagesMat = null;
		boolean imagesMatInit = false;
		
		File folder = new File(pathname);
		File[] files = folder.listFiles();
		
		int row = 0;
		
		for (File file : files) {
			//System.out.println(file.toString());
			
			Mat image = Imgcodecs.imread(file.toString());
			
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
			
			
			//images.add(image);
			row++;
			
			//break;
		}
		
		return imagesMat;
	}
	
	private static Mat mean_image(ArrayList<Mat> images)
	{
		Mat sum = new Mat(images.get(0).rows(), images.get(0).cols(), CvType.CV_64FC1);
		
		for (Mat mat : images) {
			Core.add(mat, sum, sum);
		}
		
		Mat avg = mean_image(images);
		
		Core.divide(images.size(), sum, avg);
		
		return avg;
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
		
		output_matrix(covar, "covar.txt");
		
		Core.eigen(covar, snap_val, snap_vec);
		
		output_matrix(snap_val, "snap_val.txt");
		output_matrix(snap_vec, "snap_vec.txt");
		
		eig_vec = Mat.zeros(rows, cols, images.type());
		Mat eig_temp = new Mat(rows, cols, images.type());
		
		for(int i = 0; i < rows; i++)
		{
			Mat row = eig_vec.row(i);
			for(int j = 0; j < rows; j++)
			{
				Mat temp = new Mat();
				Core.multiply(images.row(j), new Scalar(snap_vec.get(j, i)), temp);
				Core.add(row, temp, row);
			}
			
			double[] data = new double[(int) (rows * cols)];
			
			//System.out.println(data.length);
			
			row.get(0, 0, data);
			
			eig_temp.put(i, 0, data);
		}
		
		eig_val = snap_val.diag();
		
		eig_vec = normalise(eig_temp);
		
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
			
			Mat normRow = new Mat();
			
			Core.normalize(row, normRow);
			
			//System.out.println("normRow:\n" + normRow.dump());
			
			Core.divide(row, normRow, row);
			
			//append row into new matrix
			double[] data = new double[(int) (rows * cols)];
			
			row.get(0, 0, data);
			
			norm.put(i, 0, data);
			
			//output_matrix(norm, "norm_" + i +".txt");
		}
		System.out.println(norm);
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
