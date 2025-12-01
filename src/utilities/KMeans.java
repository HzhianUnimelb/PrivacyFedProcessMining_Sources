package utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import optimization.Optimization;

public class KMeans {
    private int numClusters;
    private List<Optimization> models;
    private double[][] centroids;

    public KMeans(int numClusters, List<Optimization> models) {
        this.numClusters = numClusters;
        this.models = models;
        this.centroids = new double[numClusters][2]; // Assuming 2D points
       // for(BasicObject obj: models)
    }

    public List<List<Optimization>> fit(int maxIterations) {
        initializeCentroids();
        int[] labels = new int[models.size()];
       
        System.out.println();
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            boolean changed = assignClusters(labels);
            updateCentroids();

            if (!changed) {
                break; // Stop if no points changed clusters
            }
        }

        return getClusters(labels);
    }

    private void initializeCentroids() {
        Random rand = new Random();
        for (int i = 0; i < numClusters; i++) {
        	double[]point = models.get(rand.nextInt(models.size())).getBestMetric();
            centroids[i] = point;
        }
    }

    private boolean assignClusters(int[] labels) {
        boolean changed = false;
        for (int i = 0; i < models.size(); i++) {
            int closestCluster = findClosestCentroid(models.get(i));
            if (labels[i] != closestCluster) {
                labels[i] = closestCluster;
                changed = true;
            }
        }
        return changed;
    }

    private int findClosestCentroid(Optimization model) {
        double minDistance = Double.MAX_VALUE;
        int closestCluster = -1;

        for (int i = 0; i < centroids.length; i++) {
            double distance = euclideanDistance(model.getBestMetric(), centroids[i]);
            if (distance < minDistance) {
                minDistance = distance;
                closestCluster = i;
            }
        }
        return closestCluster;
    }
    public  List<List<Optimization>> getClusters(int[] labels) {
        List<List<Optimization>> clusters = new ArrayList<>(numClusters);
        
        // Initialize clusters
        for (int i = 0; i < numClusters; i++) {
            clusters.add(new ArrayList<>());
        }

        // Assign points to their respective clusters
        for (int i = 0; i < models.size(); i++) {
            clusters.get(labels[i]).add(models.get(i));
        }
        // Print the clusters
     /*   for (int i = 0; i < clusters.size(); i++) {
        	System.out.println("cluster_number "+i);
          for(Optimization a :clusters.get(i))
            System.out.println("("+a.getBestMetric()[0]+" "+a.getBestMetric()[1]+")");
        }*/
        return clusters;
    }
    private double euclideanDistance(double[] point1, double[] point2) {
        return Math.sqrt(Math.pow(point1[0] - point2[0], 2) + Math.pow(point1[1] - point2[1], 2));
    }

    private void updateCentroids() {
        double[][] newCentroids = new double[numClusters][2];
        int[] counts = new int[numClusters];

        for (int i =0;i<models.size();i++) {
            int cluster = findClosestCentroid(models.get(i));
			double []point = models.get(i).getBestMetric(); 
            newCentroids[cluster][0]+= point[0];
            newCentroids[cluster][1]+= point[1];
            counts[cluster]++;
        }

        for (int i = 0; i < numClusters; i++) {
            if (counts[i] != 0) {
                newCentroids[i][0] /= counts[i];
                newCentroids[i][1] /= counts[i];
            }
        }

        centroids = newCentroids;
    }

    public static void main(String[] args) {
    	Random random = new Random();
    	
       double points[][] = {
           {0.2, 0.1}, {0.9, 0.9},
            {0.8, 0.8}, {0.2, 0.3}, {0.3, 0.2}
        };
       List<double[]> lPoints = new ArrayList<double[]>();
       for (int i = 0; i < points.length; i++) {
          
    	   lPoints.add(points[i]); // Add the point to the list
       }
        
        int numClusters = 4;

        //KMeans kMeans = new KMeans(numClusters, lPoints);
       // kMeans.fit(100);
    }
}