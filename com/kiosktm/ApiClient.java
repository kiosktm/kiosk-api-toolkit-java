package com.kiosktm;

import com.kiosktm.AuthenticationException;
import com.kiosktm.InvalidProspectException;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @class ApiClient
 *
 * @description Client Object for Authenticating and Submitting Prospect to Kiosk 
 *              via the SmartRequest API.
 */
public class ApiClient {

    private String host;

    private String oauth_client_id;
    private String oauth_client_secret;

    private String bearer_token = null;
    private int bearer_token_expires;

    public ApiClient(String oauth_client_id, String oauth_client_secret, String host) {
        this.init(oauth_client_id, oauth_client_secret, host);
    }

    public ApiClient(String oauth_client_id, String oauth_client_secret) {
        this.init(oauth_client_id, oauth_client_secret, "https://api.smartrfi.kiosk.tm");
    }

    private void init(String oauth_client_id, String oauth_client_secret, String host) {
        this.setOauthClientId(oauth_client_id);
        this.setOauthClientSecret(oauth_client_secret);
        this.setHost(host);

        this.bearer_token_expires = this.getCurrentTimestamp();
    }

    public int getCurrentTimestamp() {
        return (int)(System.currentTimeMillis() / 1000L);
    }

    public void setOauthClientId(String oauth_client_id) { this.oauth_client_id = oauth_client_id; }
    public String getOauthClientId() { return this.oauth_client_id; }

    public void setOauthClientSecret(String oauth_client_secret) { this.oauth_client_secret = oauth_client_secret; }
    public String getOauthClientSecret() { return this.oauth_client_secret; }

    public void setHost(String host) { this.host = host; }
    public String getHost() { return this.host; }

    public static void main(String args[]) {
        if(args.length < 2) {
            System.out.println("Usage: java -jar KioskApiClient.jar <client_id> <client_secret>");
            System.exit(0);
        }

        ApiClient client = new ApiClient(args[0], args[1]);

        HashMap<String, String> prospect = new HashMap();

        prospect.put("FirstName", "#test");
        prospect.put("LastName", "TestOne");
        prospect.put("Email", "test+one@kiosk.tm");
//        prospect.put("Phone", "4158955327");
        prospect.put("ProgramOfInterest", "Mathiness");
//        prospect.put("ProgramOfInterest", "Math");

        try {
            HashMap<String, String> response = client.submitProspect(prospect);

            System.out.println(response.toString());
        } catch(AuthenticationException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch(InvalidProspectException e) {
            System.out.println("[ERROR] " + e.getMessage());

            for(Iterator i = e.getInvalidFields().iterator(); i.hasNext(); ) {
                System.out.println("[INVALID] " + (String)i.next());
            }
        }
    }

    public JSONObject call(String URI, JSONObject payload, String bearer_token) {
        try {
            URL url = new URL(this.getHost() + URI);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            if(bearer_token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + bearer_token);
            }

            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes());
            os.flush();
	        os.close();

            InputStream is;

            try {
                is = conn.getInputStream();
            } catch(IOException e) {
                is = conn.getErrorStream();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(is));

            String line;

            StringBuffer response = new StringBuffer();

            while((line = in.readLine()) != null) {
                response.append(line);
            }

            in.close();

            return new JSONObject(response.toString());
        } catch(IOException e) {
            return new JSONObject();
        } catch(JSONException e) {
            return new JSONObject();
        }
    }

    public String authenticate() throws AuthenticationException {
        return this.authenticate(false);
    }

    public String authenticate(boolean refresh) throws AuthenticationException {
        if(!refresh && this.bearer_token != null && this.getCurrentTimestamp() <= this.bearer_token_expires)
            return this.bearer_token;

        try {
            JSONObject payload = new JSONObject();

            payload.put("grant_type", "client_credentials");
            payload.put("client_id", this.getOauthClientId());
            payload.put("client_secret", this.getOauthClientSecret());

            JSONObject response = this.call("/oauth", payload, null);

            //TODO THROW AUTHENTICATION EXCEPTION ON ERROR
            this.bearer_token = response.getString("access_token");
            this.bearer_token_expires = this.getCurrentTimestamp() + response.getInt("expires_in");

            return this.bearer_token;
        } catch(JSONException e) {}
    }

    public HashMap<String, String> submitProspect(HashMap<String, String> prospect_fields) throws AuthenticationException, InvalidProspectException {
        bearer_token = this.authenticate();

        if(bearer_token == null) {
            HashMap error = new HashMap();

            error.put("status", "error");
            error.put("error", "AuthenticationError");

            return error;
        }

        try {
            JSONObject payload = new JSONObject(prospect_fields);

            JSONObject result = this.call("/prospect", payload, bearer_token);

            if(result.has("status") && result.get("status") instanceof String && result.getString("status").equals("ok")) {
                HashMap<String, String> response = new HashMap();

                response.put("status", "ok");
                response.put("id", (String)result.get("id"));
                response.put("prospect_id", (String)result.get("prospect_id"));

                return response;
            } else {
                ArrayList<String> invalidFields = new ArrayList<String>();

                JSONObject validation = result.getJSONObject("detail").getJSONObject("validation");

                for(Iterator fields = validation.keys(); fields.hasNext(); ) {
                    String field = (String)fields.next();

                    if(!validation.getBoolean(field)) {
                        invalidFields.add(field);
                    }
                }

                throw new InvalidProspectException(invalidFields);
            }
        } catch(JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

