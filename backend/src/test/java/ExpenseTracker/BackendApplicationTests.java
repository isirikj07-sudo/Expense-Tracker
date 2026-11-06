package ExpenseTracker;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import java.net.*;
import java.net.http.*;
import java.util.*;
import tools.jackson.databind.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
class BackendApplicationTests {
 @Value("${local.server.port}") int port;
 ObjectMapper json=new ObjectMapper();
 class Client {
  HttpClient http=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
  String csrf;
  Client() throws Exception {csrf=json.readTree(call("GET","/api/csrf",null).body()).get("token").asText();}
  HttpResponse<String> call(String method,String path,String body) throws Exception {
   var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).header("Content-Type","application/json");
   if(csrf!=null)b.header("X-XSRF-TOKEN",csrf);
   return http.send(b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
  }
  String register() throws Exception {String email=UUID.randomUUID()+"@example.com";assertEquals(200,call("POST","/api/auth/register","{\"name\":\"Test User\",\"email\":\""+email+"\",\"password\":\"password123\"}").statusCode());return email;}
 }
 String entry(String amount){return "{\"description\":\"Groceries\",\"amount\":"+amount+",\"type\":\"EXPENSE\",\"category\":\"Food & dining\",\"date\":\"2026-11-06\",\"notes\":\"Weekly shop\"}";}
 @Test void accountSessionAndLogout() throws Exception {
  Client c=new Client();assertEquals(401,c.call("GET","/api/auth/me",null).statusCode());String email=c.register();
  assertFalse(c.call("GET","/api/auth/me",null).body().contains("password"));
  assertEquals(200,c.call("POST","/api/auth/logout",null).statusCode());assertEquals(401,c.call("GET","/api/auth/me",null).statusCode());
  assertEquals(401,c.call("POST","/api/auth/login","{\"email\":\""+email+"\",\"password\":\"wrongpassword\"}").statusCode());
  assertEquals(200,c.call("POST","/api/auth/login","{\"email\":\""+email+"\",\"password\":\"password123\"}").statusCode());
 }
 @Test void transactionLifecycleAndMonthFiltering() throws Exception {
  Client c=new Client();c.register();var created=c.call("POST","/api/transactions",entry("123.45"));assertEquals(200,created.statusCode(),created.body());long id=json.readTree(created.body()).get("id").asLong();
  assertEquals(1,json.readTree(c.call("GET","/api/transactions?month=2026-11",null).body()).size());
  assertEquals(0,json.readTree(c.call("GET","/api/transactions?month=2026-10",null).body()).size());
  assertEquals(200,c.call("PUT","/api/transactions/"+id,entry("200.00")).statusCode());
  assertEquals(200.0,json.readTree(c.call("GET","/api/transactions?month=2026-11",null).body()).get(0).get("amount").asDouble());
  assertEquals(200,c.call("DELETE","/api/transactions/"+id,null).statusCode());assertEquals(0,json.readTree(c.call("GET","/api/transactions?month=2026-11",null).body()).size());
 }
 @Test void accountsCannotAccessEachOthersData() throws Exception {
  Client a=new Client();a.register();long id=json.readTree(a.call("POST","/api/transactions",entry("10")).body()).get("id").asLong();
  Client b=new Client();b.register();assertEquals(0,json.readTree(b.call("GET","/api/transactions?month=2026-11",null).body()).size());
  assertEquals(404,b.call("PUT","/api/transactions/"+id,entry("50")).statusCode());assertEquals(404,b.call("DELETE","/api/transactions/"+id,null).statusCode());
  long budget=json.readTree(a.call("POST","/api/budgets","{\"month\":\"2026-11\",\"category\":\"Shopping\",\"amount\":100}").body()).get("id").asLong();
  assertEquals(404,b.call("DELETE","/api/budgets/"+budget,null).statusCode());
 }
 @Test void budgetUpsertAndDelete() throws Exception {
  Client c=new Client();c.register();String body="{\"month\":\"2026-11\",\"category\":\"Housing\",\"amount\":1000}";
  assertEquals(200,c.call("POST","/api/budgets",body).statusCode());assertEquals(200,c.call("POST","/api/budgets",body.replace("1000","2000")).statusCode());
  var list=json.readTree(c.call("GET","/api/budgets?month=2026-11",null).body());assertEquals(1,list.size());assertEquals(2000,list.get(0).get("amount").asInt());
  assertEquals(200,c.call("DELETE","/api/budgets/"+list.get(0).get("id").asLong(),null).statusCode());
 }
 @Test void rejectsInvalidInputAndMissingCsrf() throws Exception {
  Client c=new Client();c.register();for(String amount:List.of("0","-1","1.001"))assertEquals(400,c.call("POST","/api/transactions",entry(amount)).statusCode());
  assertEquals(400,c.call("POST","/api/transactions",entry("1").replace("Food & dining","Unknown")).statusCode());
  assertEquals(400,c.call("GET","/api/transactions?month=invalid",null).statusCode());
  c.csrf=null;assertEquals(403,c.call("POST","/api/transactions",entry("1")).statusCode());
 }
 @Test void servesFrontend() throws Exception {Client c=new Client();assertTrue(c.call("GET","/",null).body().contains("ledger"));assertEquals(200,c.call("GET","/app.js",null).statusCode());}
}
