package hello;

import java.awt.print.Book;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

//import com.in28minutes.springboot.model.Course;
//import com.in28minutes.springboot.service.StudentService;

@RunWith(SpringRunner.class)
@DataJpaTest
@WebMvcTest(value = BookedRepositoryController.class, secure = false)
public class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingRepository bookingRepository;

    Booking booking = new Booking("jesse", "gmail", "", "2018/10/25", "2018/10/26", "");



    String validRequest =
            "{\"_id\":\"\"," +
            "\"fullname\":\"jesse\"," +
            "\"email\":[\"gmail\"," +
            "\"session\":\"\"," +
            "\"bookedDate\":[\"2018/10/25\"," +
            "\"departureDate\":\"2018/10/26\"," +
            "\"status\":[\"\",";

    @Test
    public void getAll() throws Exception {

//        Mockito.when(
////                bookingRepository.findAll()).thenReturn(booking);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(
                "/").accept(
                MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        System.out.println(result.getResponse());
        String expected = "{}";

        JSONAssert.assertEquals(expected, result.getResponse()
                .getContentAsString(), true);
    }

}
