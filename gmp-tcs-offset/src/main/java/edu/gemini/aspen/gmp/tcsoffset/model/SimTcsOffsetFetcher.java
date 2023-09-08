package edu.gemini.aspen.gmp.tcsoffset.model;

import java.io.*;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

/**
 * A TCS Offset Fetcher that obtains its information from a file,
 * for simulation purposes.
 */
class SimTcsOffsetFetcher implements TcsOffsetFetcher {
    private static final Logger LOG = Logger.getLogger(SimTcsOffsetFetcher.class.getName());

    protected static final int TCS_CTX_LENGTH = 39;

    private final List<double[]> _simData;
    private int _currentPos = 0;


    public SimTcsOffsetFetcher(InputStream stream) {
        if (stream == null) {
            throw new RuntimeException("Not a valid simulation data stream");
        }
        _simData = new ArrayList<double[]>();
        try {
            readSimulationFile(stream);
        } catch (IOException e) {
            throw new RuntimeException("Problems reading data stream", e);
        }
    }

    private void readSimulationFile(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(stream)));
        String line;
        while ((line = br.readLine()) != null) {
            if (isEmptyLine(line)) { //an empty line, discard it
                continue;
            }

            if (isComment(line)) { //a comment, discard it
                continue;
            }

            String[] values = line.split("\\s");
            if (values.length < TCS_CTX_LENGTH - 1) {
                LOG.warning("Not enough elements for TCS Offset on line :" + line);
                continue;
            }

            double result[] = new double[TCS_CTX_LENGTH];
            //We store the elements starting in position 1. The
            //element in position 0 is reserved for time
            for (int i = 1; i < TCS_CTX_LENGTH; i++) {
                result[i] = Double.parseDouble(values[i - 1]);
            }
            _simData.add(result);
        }

        br.close();
        stream.close();
    }

    private boolean isEmptyLine(String line) {
        return line.matches("\\s*$");
    }

    private boolean isComment(String line) {
        return line.matches("\\s*#.*");
    }

    public double[] getTcsOffset() throws TcsOffsetException {
        if (_simData.isEmpty()) {
            return null;
        }

        double[] result = _simData.get(_currentPos++);

        //store the timestamp in position 0, as seconds since 1970
        result[0] = System.currentTimeMillis() / 1000.0;
        //If the next position is beyond the available data, restart
        if (_currentPos >= _simData.size()) {
            _currentPos = 0;
        }
        return result;

    }
}
