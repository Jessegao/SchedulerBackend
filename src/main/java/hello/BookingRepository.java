package hello;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "bookedDate", path = "bookedDate")
public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findBy_id(String _id);

    List<Booking> findBy_idBetween(String start, String end);

    List<Booking> findBySession(String session);

    void deleteBySession(String session);
}