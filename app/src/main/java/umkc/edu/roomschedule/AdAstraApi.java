package umkc.edu.roomschedule;

/**
 * Created by Santosh
 * <p/>
 * References:
 * SSL Handshake Issue
 * http://stackoverflow.com/a/6378872/769052 **
 * http://blog.crazybob.org/2010/02/android-trusting-ssl-certificates.html **
 * http://developer.android.com/training/articles/security-ssl.html
 * http://stackoverflow.com/q/7125275/769052
 * http://developer.android.com/training/articles/security-ssl.html
 */

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.net.*;
import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import retrofit.client.Client;


public class AdAstraApi {

    private String BaseUrl;
    private String ResponderUrl;
    private String responderParameters;
    private String fields;
    private String sortOrder;
    private String filter;
    private SSLSocketFactory factory;

    /*
     *Constructor sets BaseUrl, ResponderUrl and ResponderParameters properties
     */
    public AdAstraApi(SSLSocketFactory factory) throws UnsupportedEncodingException, IOException, JSONException {
        this.factory = factory;
        this.BaseUrl = "https://adastra.umkc.edu/Astra7/";
//        this.ResponderUrl = "~api/search/room?action=GET";
        this.ResponderUrl = "~api/calendar/calendarList?action=get";
//        this.responderParameters = "fields=RowNumber%2CId%2CRoomName%2CRoomDescription%2CRoomNumber%2CRoomTypeName%2CBuildingCode%2CBuildingName%2CCampusName%2CCapacity%2CBuildingRoomNumberRoomName%2CCanEdit%2CCanDelete&sortOrder=%2BBuildingRoomNumberRoomName";
        this.fields = URLEncoder.encode("ActivityName,ParentActivityName,Description,StartDate,EndDate,StartMinute,EndMinute,ActivityTypeCode,CampusName,BuildingCode,RoomNumber,RoomName",
                "UTF-8");
        this.sortOrder = "startDate";
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.HOUR, 30 * 24);
        Date end = calendar.getTime();

        SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String todayDate = parserSDF.format(today);
        String endDate = parserSDF.format(end);
        Log.d("AdAstra Dates", todayDate + " - " + endDate);
        this.filter = URLEncoder.encode("(((StartDate>=\"" + todayDate + "T00:00:00\")&&(EndDate<=\"" + endDate + "T00:00:00\"))&&(Location.Room.BuildingId in (\"23b3ef91-8fbc-48ce-a663-2d372438676c\")))");
        this.responderParameters = "fields=" + this.fields + "&sortOrder=" + this.sortOrder + "&filter=" + this.filter;
    }

    static SSLSocketFactory trustIFNetServer(Context context) {

        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            KeyStore ks = KeyStore.getInstance("BKS");

            InputStream in = context.getResources().openRawResource(R.raw.mystore);

            String keyPassword = "ez24get";

            ks.load(in, keyPassword.toCharArray());

            in.close();

            tmf.init(ks);

            TrustManager[] tms = tmf.getTrustManagers();

            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, tms, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String login() throws MalformedURLException, IOException, JSONException {
         /*
          * Open an HTTP Connection to the Logon.ashx page
          */
        HttpsURLConnection httpcon = (HttpsURLConnection) new URL(BaseUrl + "Logon.ashx").openConnection();
        httpcon.setSSLSocketFactory(factory);

        httpcon.setDoInput(true);
        httpcon.setDoOutput(true);
        httpcon.setRequestProperty("Content-Type", "application/json");
        httpcon.setRequestProperty("Accept", "application/json");
        httpcon.setRequestMethod("POST");
        httpcon.connect();
         /*
          * Output user credentials over HTTP Output Stream
          */

        //Client client = ClientBuilder.newClient();
        JSONObject json;
        InputStream is = new URL("http://umkclaw.link/doNOTdelete/AdAstraPassword.json").openStream();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = bufferedReader.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
            json = new JSONObject(jsonText);
            //JSONObject json = readJsonFromUrl("http://umkclaw.link/doNOTdelete/AdAstraPassword.json");
            //return json;
        } finally {
            is.close();
        }
        JSONObject ssoDetails = (JSONObject) json.get("data");
        System.out.println(ssoDetails);
        //outputBytes = "{'username': 'ajmia', 'password':'SDM@pawpaw'}".getBytes("UTF-8");
        byte[] outputBytes=ssoDetails.toString().getBytes("UTF-8");
        OutputStream os = httpcon.getOutputStream();
        os.write(outputBytes);
        os.close();
         /*
          * Call Function setCookie and pass the HttpUrlConnection. Set Function
          * will return a Cookie String used to authenticate user.
          */
        return setCookie(httpcon);
    }

    public String setCookie(HttpURLConnection httpcon) {

         /*
          * Process the HTTP Response Cookies from successful credentials
          */
        String headerName;
        ArrayList<String> cookies = new ArrayList<String>();

        for (int i = 1; (headerName = httpcon.getHeaderFieldKey(i)) != null; i++) {

            if (headerName.equals("Set-Cookie") && httpcon.getHeaderField(i) != "null") {
                cookies.add(httpcon.getHeaderField(i));
            }
        }
        httpcon.disconnect();
         /*
          * Filter cookies, create Session_ID Cookie
          */
        String cookieName = cookies.get(0);
        String cookie2 = cookies.get(1);
        String cookie1 = cookieName.substring(cookieName.indexOf("="), cookieName.indexOf(";") + 2);
        cookie2 = cookie2.substring(0, cookie2.indexOf(";"));
        cookieName = cookieName.substring(0, cookieName.indexOf("="));
        String cookie = cookieName + cookie1 + cookie2;
        return cookie;
    }

    public String ApiResponder(String cookie) throws MalformedURLException, IOException {
         /*
          * Create a new HTTP Connection request to responder, pass along Session_ID Cookie
          */
        Log.d("AdAstra Params", this.responderParameters);

        HttpsURLConnection httpcon = (HttpsURLConnection) ((new URL(this.BaseUrl + this.ResponderUrl).openConnection()));
        httpcon.setSSLSocketFactory(factory);
        httpcon.setDoOutput(true);
        httpcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpcon.setRequestProperty("Accept", "application/json");
        httpcon.setRequestProperty("Cookie", cookie);
        httpcon.setRequestMethod("POST");
        httpcon.connect();


        byte[] outputBytes = responderParameters.getBytes("UTF-8");
        OutputStream os = httpcon.getOutputStream();
        os.write(outputBytes);
        os.close();

         /*
          * Read/Output response from server
          */

        BufferedReader inreader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
        StringBuffer decodedString = new StringBuffer();
        String str = "";
        while ((str = inreader.readLine()) != null) {
//            System.out.println(str);
            decodedString.append(str);
        }

        inreader.close();
        httpcon.disconnect();

        return decodedString.toString();
    }

    public static void main(String[] args) throws Exception {
        AdAstraApi api = new AdAstraApi(SSLContext.getDefault().getSocketFactory());
        System.out.println(api.login());
        System.out.println(api.ApiResponder(api.login()));
    }
}