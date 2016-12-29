# Kiosk API Toolkit for Java

The Kiosk API Toolkit for PHP is designed to allow Lead Vendors for Kiosk 
to be able to submit Prospect records to the Kiosk Prospect API.  The toolkit 
handles the OAuth2 authentication process, reusing the Bearer Token where 
possible, and provides a simple function that accepts a Prospect record in 
array format and submits it over the API.  

## Sample Prospect Submission

```java
import com.kiosktm.ApiClient;
import com.kiosktm.AuthenticationException;
import com.kiosktm.InvalidProspectException;
import java.util.HashMap;
import java.util.Iterator;

String client_id = "<CLIENT ID>";
String client_secret = "<CLIENT_SECRET>";

ApiClient client = new ApiClient(client_id, client_secret);

HashMap<String, String> prospect = new HashMap<String, String>();

prospect.put("FirstName", "#test");
prospect.put("LastName", "TestOne");
prospect.put("Email", "test+one@kiosk.tm");
prospect.put("Phone", "4158955327");
prospect.put("ProgramOfInterest", "Math");

try {
    HashMap<String, String> response = client.submitProspect(prospect);

    // Store "id" and "prospect_id" from response for future reference.
} catch(AuthenticationException e) {
    // Authentication Failed - Check Client ID and Secret
} catch(InvalidProspectException e) {
    // Prospect Validation Failed
}
```

## Sample Responses

The `submitProspect` method returns a HashMap with a "status" element.  
The "status" element with be either "ok" or "error".

### OK Response

Valid submissions with have a "status" of "ok" and will provide an "id" and 
"prospect_id".  It is critically important that these IDs are retained so 
that they can be used to investigate issues with missing leads.

```java
HashMap<String, String> response = client.submitProspect(prospect);

String id = response.get("id");
String prospect_id = response.get("prospect_id");
```

### Invalid Response

Invalid submissions will result in an InvalidProspectSubmission being thrown.  The 
invalid fields will be available as an ArrayList;

```java
try {
    HashMap<String, String> response = client.submitProspect(prospect);

    // ...
} catch(AuthenticationException e) {
    // ...
} catch(InvalidProspectException e) {
    System.out.println("Prospect Validation Failed");

    for(Iterator i = e.getInvalidFields().iterator(); i.hasNext(); ) {
        System.out.println("[INVALID] " + (String)i.next());
    }
}

```

