/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */


package minicp.util.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;


public class InputReader {

    private BufferedReader in;
    private StringTokenizer tokenizer;

    public InputReader(String file) {
        try {

            FileInputStream istream = new FileInputStream(file);
            in = new BufferedReader(new InputStreamReader(istream));
            tokenizer = new StringTokenizer("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer getInt() throws RuntimeException {
        if (!tokenizer.hasMoreTokens()) {
            try {
                String line;
                do {
                    line = in.readLine();
                    if (line == null) {
                        System.out.println("No more line to read");
                        throw new RuntimeException("End of file");
                    }
                    tokenizer = new StringTokenizer(line);
                } while (!tokenizer.hasMoreTokens());

            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
        return Integer.parseInt(tokenizer.nextToken());
    }

    public int[][] getMatrix(int n, int m) throws RuntimeException {
        int[][] matrix = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = getInt();
            }
        }
        return matrix;
    }


    public Integer[] getIntLine() throws RuntimeException {
        if (!tokenizer.hasMoreTokens()) {
            try {
                String line;
                do {
                    line = in.readLine();
                    if (line == null) {
                        System.out.println("No more line to read");
                        throw new RuntimeException("End of file");
                    }
                    tokenizer = new StringTokenizer(line);
                } while (!tokenizer.hasMoreTokens());

            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
        Integer[] res = new Integer[tokenizer.countTokens()];
        for (int i = 0; i < res.length; i++) {
            res[i] = Integer.parseInt(tokenizer.nextToken());
        }
        return res;
    }

    public String getString() throws RuntimeException {
        if (!tokenizer.hasMoreTokens()) {
            try {
                String line;
                do {
                    line = in.readLine();
                    if (line == null) {
                        System.out.println("No more line to read");
                        throw new RuntimeException("End of file");
                    }
                    tokenizer = new StringTokenizer(in.readLine());
                } while (!tokenizer.hasMoreTokens());

            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
        return tokenizer.nextToken();
    }

}
