package com.hva.sequential;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Main {
    private static final int SEED = 10;
    private static final int SIZE = 10;
    private static Integer[] array = new Integer[SIZE];

    public static void main(String[] args) {
        initializeArray();
        bubbleSort(array);
        System.out.println("Sorted array");

        int n = array.length;
        for (int i=0; i<n; ++i)
            System.out.print(array[i] + " ");
        System.out.println();
    }

    private static void bubbleSort(Integer arr[]) {
        int n = arr.length;
        for (int i = 0; i < n-1; i++)
            for (int j = 0; j < n-i-1; j++)
                if (arr[j] > arr[j+1])
                {
                    // swap arr[j+1] and arr[i]
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
    }

    private static void initializeArray() {
        Random rand = new Random(SEED);
        for (int i = 0; i < SIZE; i++) {
            array[i] = rand.nextInt(SIZE);
        }

        Arrays.sort(array, Collections.reverseOrder());
    }
}