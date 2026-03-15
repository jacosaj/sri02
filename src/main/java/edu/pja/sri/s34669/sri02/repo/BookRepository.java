package edu.pja.sri.s34669.sri02.repo;

import edu.pja.sri.s34669.sri02.model.Book;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookRepository extends CrudRepository<Book, Long> {
    List<Book> findAll();
}
