package hello;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Null;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
// handles requests to http://localhost:8080/bookedDate
public class BookedRepositoryController {
    @Autowired
    private BookingRepository repository;

    private List<Booking> getBookingsByDateRange(String start, String end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date now, endDate;
        try {
            now = sdf.parse(start);
            endDate = sdf.parse(end);
        } catch (Exception e) {
            System.out.println("Not a valid date format: must be in \"yyyy/MM/dd\" format");
            return new ArrayList<Booking>();
        }
        return repository.findByBookedDateBetween(sdf.format(now), sdf.format(endDate));
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Booking> getAllBookings() {
        return repository.findAll();
    }

    @RequestMapping(value = "/{timeInDays}", method = RequestMethod.GET)
    public List<Booking> getBookingsInterval(@PathVariable("timeInDays") int timeInDays) {
        Date in = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
        Date now = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DATE, timeInDays);
        Date endDate = c.getTime();
        return repository.findByBookedDateBetween(sdf.format(now), sdf.format(endDate));
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public Booking createBooking(@Valid @RequestBody Booking booking) {
        booking.set_id(booking.getBookedDate());

        // Check that the reservation is valid
        Date in = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
        Date now = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        try {
            Date bookingDate = sdf.parse(booking.getBookedDate());
            if (! bookingDate.after(now)) {
                booking.setStatus("Not a valid date: must book 1 day in advance!");
                return booking;
            }
            c.add(Calendar.MONTH, 1);
            if (c.getTime().before(bookingDate)) {
                booking.setStatus("Not a valid date: can only book up to 1 month in advance!");
            }
        } catch (Exception e) {
            booking.setStatus("Not a valid date format: must be in \"yyyy/MM/dd\" format");
            return booking;
        }

        synchronized (this){
            // Availability check
            if (repository.findById(booking.get_id()).isPresent()) {
                booking.setStatus("Not available");
                return booking;
            }
            // Sets session
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDateTime local = LocalDateTime.now();
            booking.setSession(local.toString());
            booking.setStatus("Booked");
            repository.save(booking);
        }

        return booking;
    }

//    @RequestMapping(value = "/bookings/{id}", method = RequestMethod.GET)
//    public Booking getBookingById(@PathVariable("id") String id) {
//        Optional<Booking> b = repository.findById(id);
//        if (b.isPresent()) {
//            Booking booking = b.get();
//            return new Booking("", "", "", booking.getBookedDate(), booking.getDepartureDate(), booking.getStatus());
//        }
//        return new Booking("", "", "", id, "", "Available");
//    }

//    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
//    public void modifyBookingById(@PathVariable("id") ObjectId id, @Valid @RequestBody Pets pets) {
//        pets.set_id(id);
//        repository.save(pets);
//    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public void deleteBooking(@Valid @RequestBody Booking booking) {
        Optional<Booking> b = repository.findById(booking.get_id());
        if (b.isPresent()) {
            repository.delete(b.get());
        }
    }
}


