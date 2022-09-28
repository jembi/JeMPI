package org.jembi.jempi.emref;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public final class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(final String[] args) throws IOException {
        new Main().run();
    }

    private void run() throws IOException {
        LOGGER.info("EM Reference");
        final var task = new EMTask();
        final var path = "../../docker/tests/test-data/dcab-1/data-test-32-d-040000-160000-dcab-1.csv.gz";
        task.doIt(path, 0L, 200_000L);
    }

}

/*
../../docker/tests/test-data/dcab/data-test-32-d-020000-080000-dcab.csv.gz  80_000L, 20_000L
mHat:[0.7722054248052748,  0.8581007480325328,  0.9999997348125003, 0.9659105570775396,   0.8903441501044324, 0.9868545306737322,    0.966299030807308]
uHat:[0.05302758568384765, 0.07098393361866413, 0.5249540216242222, 0.011386081577224235, 0.7937593617938395, 0.0037159147222725324, 5.733055971736195E-5]
*/
