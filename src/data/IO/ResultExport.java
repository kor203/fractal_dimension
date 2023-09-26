package data.IO;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class ResultExport {
    public static void export(int[][] boxAmounts, double[] results, String fileName, int minBoxSize, int maxBoxSize){
        try {
            FileWriter fileWriter = new FileWriter("box_cover.txt");
            fileWriter.write("Results for " + fileName + ".\n");
            fileWriter.write("Box size | Box amount\n");
            for (int i = 0; i < boxAmounts[0].length; i++) {
                fileWriter.write(boxAmounts[0][i] + " " + boxAmounts[1][i] + '\n');
            }
            fileWriter.close();
            fileWriter = new FileWriter("parameters.txt");
            fileWriter.write("Results for " + fileName + ".\n");
            fileWriter.write("For box size range: " + minBoxSize + " : " + maxBoxSize + ".\n");
            int firstSignificantDigit = (int) Math.ceil(-Math.log10(results[1]));
            fileWriter.write("Box dimension d = " + String.format("%." + (firstSignificantDigit + 1) + "f", results[0]) +
                    String.format(" (%.0f)", results[1] * Math.pow(10, firstSignificantDigit + 1)) + '\n');
            firstSignificantDigit = (int) Math.ceil(-Math.log10(results[3]));
            fileWriter.write("Slope a = " + String.format("%." + (firstSignificantDigit + 1) + "f", results[2]) +
                    String.format(" (%.0f)", results[3] * Math.pow(10, firstSignificantDigit + 1)) + '\n');
            firstSignificantDigit = (int) Math.ceil(-Math.log10(results[5]));
            fileWriter.write("Intercept b = " + String.format("%." + (firstSignificantDigit + 1) + "f", results[4]) +
                    String.format(" (%.0f)", results[5] * Math.pow(10, firstSignificantDigit + 1)) + '\n');
            System.out.println(Arrays.toString(results));
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
