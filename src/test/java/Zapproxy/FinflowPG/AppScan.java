package Zapproxy.FinflowPG;

import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ClientApi;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class AppScan {
    private static final String ZAP_ADDRESS = "localhost";
    private static final int ZAP_PORT = 8080;
    private static final String ZAP_API_KEY = "hkj1ubdftcd4annhe9ut3sn4d9"; // Change this if you have set the apikey in ZAP via Options / API

    private static final String URL = "https://api.bpay.cz/FinFlowPG.API/api/v1.0/";
    private static final String TARGET = "sessions/retrieve";

    public static void main(String[] args) {
        ClientApi api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);

        try {
            // Start spidering the target
            System.out.println("Spider : " + URL + TARGET);
            // It's not necessary to pass the ZAP API key again, already set when creating the ClientApi.
            ApiResponse resp = api.spider.scan(URL + TARGET, null, null, null, null);
            String scanid;
            int progress;

            // The scan now returns a scan id to support concurrent scanning
            scanid = ((ApiResponseElement) resp).getValue();

            // Poll the status until it completes
            while (true) {
                Thread.sleep(1001);
                progress = Integer.parseInt(((ApiResponseElement) api.spider.status(scanid)).getValue());
                System.out.println("Spider progress : " + progress + "%");
                if (progress >= 100) {
                    break;
                }
            }
            System.out.println("Spider complete");

            // Give the passive scanner a chance to complete
            Thread.sleep(2000);

            System.out.println("Active scan : " + URL + TARGET);
            resp = api.ascan.scan(URL + TARGET, "True", "False", null, null, null);

            // The scan now returns a scan id to support concurrent scanning
            scanid = ((ApiResponseElement) resp).getValue();

            // Poll the status until it completes
            while (true) {
                Thread.sleep(5000);
                progress = Integer.parseInt(((ApiResponseElement) api.ascan.status(scanid)).getValue());
                System.out.println("Active Scan progress : " + progress + "%");
                if (progress >= 100) {
                    break;
                }
            }
            System.out.println("Active Scan complete");

            System.out.println("Alerts:");
            String reportName = "reports/" + TARGET.replace('/', '.') + "_zap.xml";
            BufferedWriter bw = new BufferedWriter(new FileWriter(reportName,true));
            bw.write(new String(api.core.xmlreport()));
            bw.close();
            System.out.println(new String(api.core.xmlreport()));

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
