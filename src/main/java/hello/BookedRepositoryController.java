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
import java.util.concurrent.TimeUnit;

@RestController
// handles requests to http://localhost:8080/bookedDate
public class BookedRepositoryController {

    private int MAX_BOOKING_DAYS = 3;

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
        return repository.findBy_idBetween(sdf.format(now), sdf.format(endDate));
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
        return repository.findBy_idBetween(sdf.format(now), sdf.format(endDate));
    }

    // credit: https://stackoverflow.com/questions/1555262/calculating-the-difference-between-two-java-date-instances
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public Booking createBooking(@Valid @RequestBody Booking booking) {
        booking.set_id(booking.getBookedDate());

        // Check that the reservation is within valid reservation periods
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

            // creates an entry for each day booked
            LocalDateTime local = LocalDateTime.now();
            Date start;
            Date end;
            try {
                start = sdf.parse(booking.getBookedDate());
                end = sdf.parse(booking.getDepartureDate());
            } catch (Exception e) {
                booking.setStatus("Not a valid date format: must be in \"yyyy/MM/dd\" format");
                return booking;
            }

            // Check the reserved interval isn't over 3 days
            if (getDateDiff(start, end, TimeUnit.DAYS) > 3.0){
                booking.setStatus(String.format("Reservation can be at max %2d days", MAX_BOOKING_DAYS));
                return booking;
            }

            c.setTime(start);
            for (int i = 0; i < MAX_BOOKING_DAYS; i++){
                if (c.getTime().before(end)) {
                    Booking individualDays = new Booking(booking, sdf.format(c.getTime()));
                    c.add(Calendar.DATE, 1);
                    // Sets session
                    String session = local.toString() + booking.getFullname();
                    booking.setSession(session);
                    booking.setStatus("Booked");
                    repository.save(booking);
                } else {
                    break;
                }
            }
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

    @RequestMapping(value = "/{session}", method = RequestMethod.PUT)
    public void modifyBookingById(@PathVariable("session") String session, @Valid @RequestBody Booking booking) {
        booking.set_id(booking.getBookedDate());
        List<Booking> b = repository.findBySession(session);
//        if (b.isPresent() && b.get().getSession() == booking.getSession()) {
        synchronized (this) {
            for (Booking oldDate:b) {
                deleteBooking(oldDate);
            }
            if (createBooking(booking).getStatus() != "Booked") {
                for (Booking oldDate:b) {
                    repository.save(oldDate);
                }
            }
        }
//        }
    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public void deleteBooking(@Valid @RequestBody Booking booking) {
        List<Booking> bookings = repository.findBySession(booking.get_id());
//        if (b.isPresent() && b.get().getSession() == booking.getSession()) {
//            repository.delete(b.get());
//        }
        for (Booking b:bookings) {
            repository.delete((b));
        }
    }
}


