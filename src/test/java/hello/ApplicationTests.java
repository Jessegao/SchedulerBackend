package hello;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookingRepository bookingRepository;

	@Before
	public void deleteAllBeforeTests() throws Exception {
		bookingRepository.deleteAll();
	}

//	@Test
//	public void shouldReturnRepositoryIndex() throws Exception {
//
//		mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
//				jsonPath("$._links.booking").exists());
//	}

	@Test
	public void shouldCreateEntity() throws Exception {

		Date in = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
		Date now = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 1);
		String validDate = sdf.format(c.getTime());
		c.add(Calendar.DATE, 1);
		String validDepartureDate = sdf.format(c.getTime());

		mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(
				"{\"_id\": \"\"," +
				" \"fullname\":\"jesse\"," +
				" \"email\":\"gmail\"," +
				" \"session\":\"\"," +
				" \"bookedDate\":\"" + validDate + "\"," +
				" \"departureDate\":\"" + validDepartureDate + "\"," +
				" \"status\":\"\"}"
				)).andExpect(
						status().isOk()).andExpect(
				jsonPath("$._id").value(validDate)).andExpect(
				jsonPath("$.fullname").value("jesse")).andExpect(
				jsonPath("$.email").value("gmail")).andExpect(
				jsonPath("$.bookedDate").value(validDate)).andExpect(
				jsonPath("$.departureDate").value(validDepartureDate)).andExpect(
				jsonPath("$.status").value("Booked"));
	}

	@Test
	public void testConcurrentConflictingBooking() throws InterruptedException {
		Date in = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
		Date now = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 1);
		String validDate = sdf.format(c.getTime());
		c.add(Calendar.DATE, 1);
		String validDepartureDate = sdf.format(c.getTime());

		ArrayList<JSONObject> jsonReturns = new ArrayList<JSONObject>();

		runMultithreaded( new Runnable() {
							   public void run() {
								   try{
									   String str = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(
											   "{\"_id\": \"\"," +
													   " \"fullname\":\"jesse\"," +
													   " \"email\":\"gmail\"," +
													   " \"session\":\"\"," +
													   " \"bookedDate\":\"" + validDate + "\"," +
													   " \"departureDate\":\"" + validDepartureDate + "\"," +
													   " \"status\":\"\"}"
									   )).andReturn().getResponse().getContentAsString();
									   synchronized (jsonReturns) {
									   		jsonReturns.add(new JSONObject(str));
									   }
								   }
								   catch(Exception e)
								   {
									   e.printStackTrace();
								   }
							   }
						   }
		, 5);

		int counter = 0;
		for (JSONObject json:jsonReturns) {
			try {
				if (json.getString("status").equals("Booked")) {
					counter++;
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		// Ensures only one of the booking requests was successful
		assert (counter == 1);
	}

	@Test
	public void testTwoConcurrentNonConflictingBooking() throws InterruptedException {
		Date in = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
		Date now = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 1);
		String validDate = sdf.format(c.getTime());
		c.add(Calendar.DATE, 1);
		String validDepartureDate = sdf.format(c.getTime());

		ArrayList<JSONObject> jsonReturns = new ArrayList<JSONObject>();

		ArrayList<Runnable> requests = new ArrayList<>();

		requests.add(new Runnable() {
			public void run() {
				try{
					String str = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(
							"{\"_id\": \"\"," +
									" \"fullname\":\"jesse\"," +
									" \"email\":\"gmail\"," +
									" \"session\":\"\"," +
									" \"bookedDate\":\"" + validDate + "\"," +
									" \"departureDate\":\"" + validDepartureDate + "\"," +
									" \"status\":\"\"}"
					)).andReturn().getResponse().getContentAsString();
					synchronized (jsonReturns) {
						jsonReturns.add(new JSONObject(str));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		c.add(Calendar.DATE, 1);
		String validDepartureDate2 = sdf.format(c.getTime());
		requests.add(new Runnable() {
			public void run() {
				try{
					String str = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(
							"{\"_id\": \"\"," +
									" \"fullname\":\"jesse\"," +
									" \"email\":\"gmail\"," +
									" \"session\":\"\"," +
									" \"bookedDate\":\"" + validDepartureDate + "\"," +
									" \"departureDate\":\"" + validDepartureDate2 + "\"," +
									" \"status\":\"\"}"
					)).andReturn().getResponse().getContentAsString();
					synchronized (jsonReturns) {
						jsonReturns.add(new JSONObject(str));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		runMultithreaded(requests);

		int counter = 0;
		for (JSONObject json:jsonReturns) {
			try {
				if (json.getString("status").equals("Booked")) {
					counter++;
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		// Ensures both of the booking requests were successful
		assert (counter == 2);
	}

	@Test
	public void testManyDifferentConcurrentConflictingBooking() throws InterruptedException {
		Date in = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
		Date now = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 1);
		String validDate = sdf.format(c.getTime());
		c.add(Calendar.DATE, 1);
		String validDepartureDate = sdf.format(c.getTime());

		ArrayList<JSONObject> jsonReturns = new ArrayList<JSONObject>();

		ArrayList<Runnable> requests = new ArrayList<>();

		requests.add(new Runnable() {
			public void run() {
				try{
					String str = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(
							"{\"_id\": \"\"," +
									" \"fullname\":\"jesse\"," +
									" \"email\":\"gmail\"," +
									" \"session\":\"\"," +
									" \"bookedDate\":\"" + validDate + "\"," +
									" \"departureDate\":\"" + validDepartureDate + "\"," +
									" \"status\":\"\"}"
					)).andReturn().getResponse().getContentAsString();
					synchronized (jsonReturns) {
						jsonReturns.add(new JSONObject(str));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		c.add(Calendar.DATE, 1);
		String validDepartureDate2 = sdf.format(c.getTime());
		requests.add(new Runnable() {
			public void run() {
				try{
					String str = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(
							"{\"_id\": \"\"," +
									" \"fullname\":\"jesse\"," +
									" \"email\":\"gmail\"," +
									" \"session\":\"\"," +
									" \"bookedDate\":\"" + validDate + "\"," +
									" \"departureDate\":\"" + validDepartureDate2 + "\"," +
									" \"status\":\"\"}"
					)).andReturn().getResponse().getContentAsString();
					synchronized (jsonReturns) {
						jsonReturns.add(new JSONObject(str));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		c.add(Calendar.DATE, 1);
		String validDepartureDate3 = sdf.format(c.getTime());
		requests.add(new Runnable() {
			public void run() {
				try{
					String str = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(
							"{\"_id\": \"\"," +
									" \"fullname\":\"jesse\"," +
									" \"email\":\"gmail\"," +
									" \"session\":\"\"," +
									" \"bookedDate\":\"" + validDate + "\"," +
									" \"departureDate\":\"" + validDepartureDate3 + "\"," +
									" \"status\":\"\"}"
					)).andReturn().getResponse().getContentAsString();
					synchronized (jsonReturns) {
						jsonReturns.add(new JSONObject(str));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		runMultithreaded(requests);

		int counter = 0;
		for (JSONObject json:jsonReturns) {
			try {
				if (json.getString("status").equals("Booked")) {
					counter++;
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		// Ensures both of the booking requests were successful
		assert (counter == 1);
	}

	public static void runMultithreaded(List<Runnable>  runnable) throws InterruptedException
	{
		List<Thread> threadList = new LinkedList<Thread>();
		for(Runnable r:runnable)
		{
			threadList.add(new Thread(r));
		}
		for( Thread t :  threadList)
		{
			t.start();
		}
		for( Thread t :  threadList)
		{
			t.join();
		}
	}

	public static void runMultithreaded(Runnable  runnable, int threadCount) throws InterruptedException
	{
		List<Thread> threadList = new LinkedList<Thread>();
		for(int i = 0 ; i < threadCount; i++)
		{
			threadList.add(new Thread(runnable));
		}
		for( Thread t :  threadList)
		{
			t.start();
		}
		for( Thread t :  threadList)
		{
			t.join();
		}
	}

	@Test
	public void shouldRetrieveEntity() throws Exception {

		Date in = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
		Date now = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 1);
		String validDate = sdf.format(c.getTime());
		c.add(Calendar.DATE, 1);
		String validDepartureDate = sdf.format(c.getTime());

		MvcResult mvcResult = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(
				"{\"_id\": \"\"," +
						" \"fullname\":\"jesse\"," +
						" \"email\":\"gmail\"," +
						" \"session\":\"\"," +
						" \"bookedDate\":\"" + validDate + "\"," +
						" \"departureDate\":\"" + validDepartureDate + "\"," +
						" \"status\":\"\"}"
		)).andExpect(status().isOk()).andReturn();

//		String session = mvcResult.getResponse().getContentAsString();
		try {
			String session = mockMvc.perform(get("/30")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
			JSONArray jsonArray = new JSONArray(session);
			assert (jsonArray.length() == 1);
			assert (jsonArray.getJSONObject(0).getString("_id").equals(validDate));
//			assert (false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//
//
//	@Test
//	public void shouldUpdateEntity() throws Exception {
//
//		MvcResult mvcResult = mockMvc.perform(post("/people").content(
//				"{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}")).andExpect(
//						status().isCreated()).andReturn();
//
//		String location = mvcResult.getResponse().getHeader("Location");
//
//		mockMvc.perform(put(location).content(
//				"{\"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}")).andExpect(
//						status().isNoContent());
//
//		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
//				jsonPath("$.firstName").value("Bilbo")).andExpect(
//						jsonPath("$.lastName").value("Baggins"));
//	}
//
//	@Test
//	public void shouldPartiallyUpdateEntity() throws Exception {
//
//		MvcResult mvcResult = mockMvc.perform(post("/people").content(
//				"{\"firstName\": \"Frodo\", \"lastName\":\"Baggins\"}")).andExpect(
//						status().isCreated()).andReturn();
//
//		String location = mvcResult.getResponse().getHeader("Location");
//
//		mockMvc.perform(
//				patch(location).content("{\"firstName\": \"Bilbo Jr.\"}")).andExpect(
//						status().isNoContent());
//
//		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
//				jsonPath("$.firstName").value("Bilbo Jr.")).andExpect(
//						jsonPath("$.lastName").value("Baggins"));
//	}
//
//	@Test
//	public void shouldDeleteEntity() throws Exception {
//
//		MvcResult mvcResult = mockMvc.perform(post("/people").content(
//				"{ \"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}")).andExpect(
//						status().isCreated()).andReturn();
//
//		String location = mvcResult.getResponse().getHeader("Location");
//		mockMvc.perform(delete(location)).andExpect(status().isNoContent());
//
//		mockMvc.perform(get(location)).andExpect(status().isNotFound());
//	}
}