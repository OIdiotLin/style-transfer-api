package com.oidiotlin.StyleTransfer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class App {
    private void testTensorFlow() throws Exception {
        try (Graph g = new Graph()) {
            final String value = "Hello from " + TensorFlow.version();

            // Construct the computation graph with a single operation, a constant
            // named "MyConst" with a value "value".
            try (Tensor t = Tensor.create(value.getBytes(StandardCharsets.UTF_8))) {
                // The Java API doesn't yet include convenience functions for adding operations.
                g.opBuilder("Const", "MyConst").setAttr("dtype", t.dataType()).setAttr("value", t).build();
            }

            // Execute the "MyConst" operation in a Session.
            try (Session s = new Session(g);
                 // Generally, there may be multiple output tensors,
                 // all of them must be closed to prevent resource leaks.
                 Tensor output = s.runner().fetch("MyConst").run().get(0)) {
                System.out.println(new String(output.bytesValue(), StandardCharsets.UTF_8));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);
        server.createContext("/stylize", new StylizeHandler());
        server.setExecutor(null);
        server.start();
    }

    private static class StylizeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            byte[] req = httpExchange.getRequestBody().readAllBytes();
            IntBuffer intBuf = ByteBuffer.wrap(req).asIntBuffer();
            int[] intValues = new int[intBuf.remaining()];
            intBuf.get(intValues);

            httpExchange.sendResponseHeaders(200, req.length);
            httpExchange.getResponseHeaders().add("Content-Type", "application/octet-stream");

            OutputStream os = httpExchange.getResponseBody();
            os.write(req);
            os.flush();
            os.close();
            httpExchange.close();
        }
    }
}
