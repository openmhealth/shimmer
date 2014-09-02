package org.openmhealth.shim.jawbone;


import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class JawboneShimTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testParse() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("jawbone-moves.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        //todo: Fix assertions here
    }
}
