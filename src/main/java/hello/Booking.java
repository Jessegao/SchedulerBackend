package hello;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Booking {

    @Id private String _id;

    private String fullname;
    private String email;
    private String session;
    private String bookedDate;
    private String departureDate;
    private String status;

    public Booking(String fullname, String email, String session, String bookedDate,String departureDate, String status) {
        this.fullname = fullname;
        this.email = email;
        this.session = session;
        this.bookedDate = bookedDate;
        this.departureDate = departureDate;
//        this._id = format.format(bookedDate);
        this._id = bookedDate;
        this.status = status;
    }

    public Booking() {

    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getSession() {
        return session;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBookedDate() {
        return bookedDate;
    }

    public void setBookedDate(String bookedDate) {
        this.bookedDate = bookedDate;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ObjectId needs to be converted to string
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
}