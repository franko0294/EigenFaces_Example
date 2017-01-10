package main;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class EigenFaces {

	private Mat eigenvalues;
	private Mat eigenvectors;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		learn("C:/Users/franc/OneDrive/Documents/University/Year 3/Final Year Project/pics/jpg");
	}
	
	private static void learn(String pathname)
	{
		//Mat testmat = new Mat();
		
		Mat imagesMat = new Mat();
		
		System.out.println("Reading images");
		//ArrayList<Mat> images = read_images(pathname);
		imagesMat = read_images_mat(pathname);
		
		
		System.out.println("Averaging images");
		//Mat avg = mean_image(images);
		Mat avg = mean_image_mat(imagesMat);
		
		Mat evalues = new Mat();
		Mat evectors = new Mat();
		
		System.out.println("Eigening images");
		//Core.eigen(avg, evalues, evectors);
		
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
		ArrayList<Mat> images = new ArrayList<>();
		Mat imagesMat;
		BufferedImage img = null;
		
		File folder = new File(pathname);
		File[] files = folder.listFiles();
		
		for (File file : files) {
			System.out.println(file.toString());
			Mat image = Imgcodecs.imread(file.toString());
			
			if(image.channels() > 1)
			{
				Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);	
			}
			
			//System.out.println(image.dump());
			
			image.convertTo(image, CvType.CV_64FC1);
			
			images.add(image);
			/*
			try {
				//img = ImageIO.read(file);
				//byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
				
				Mat image = Imgcodecs.imread(file.toString());
				
				//Mat img_final = new Mat();
				
				//System.out.println("Putting pixels");
				//img_final.put(0, 0, pixels);
				if(image.channels() > 1)
				{
					Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);	
				}
				
				image.convertTo(image, CvType.CV_8UC1);
				
				images.add(image);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
		imagesMat = Mat.zeros(images.size(), images.get(0).rows() * images.get(0).cols(), CvType.CV_64FC1);
		
		System.out.println(images.get(0).dump());
		
		for(int i = 0; i < images.size(); i++)
		{
			images.get(i).reshape((images.get(i).rows() * images.get(i).cols()), 1);
			
			byte[] data = new byte[(int) (images.get(i).rows() * images.get(i).cols())];
			
			images.get(i).get(0, 0, data);
			
			//System.out.println(images.get(i).dump());
			
			imagesMat.put(i, 0, data);
			//System.out.println(imagesMat.submat(0, 5, 0, 500).dump());
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
		
		//System.out.println(avgMat.dump());
		
		return avgMat;
	}
	
	private void eigen_images(ArrayList<Mat> images)
	{
		
	}
	
}
