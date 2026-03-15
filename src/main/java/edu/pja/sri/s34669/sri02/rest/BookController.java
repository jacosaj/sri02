package edu.pja.sri.s34669.sri02.rest;

import edu.pja.sri.s34669.sri02.dto.BookDto;
import edu.pja.sri.s34669.sri02.model.Book;
import edu.pja.sri.s34669.sri02.repo.BookRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    public BookController(BookRepository bookRepository, ModelMapper modelMapper) {
        this.bookRepository = bookRepository;
        this.modelMapper = modelMapper;
    }

    private BookDto convertToDto(Book book) {
        return modelMapper.map(book, BookDto.class);
    }

    private Book convertToEntity(BookDto dto) {
        return modelMapper.map(dto, Book.class);
    }

    @GetMapping
    public ResponseEntity<Collection<BookDto>> getBooks() {
        List<Book> all = bookRepository.findAll();
        List<BookDto> result = all.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> getBookById(@PathVariable Long bookId) {
        Optional<Book> book = bookRepository.findById(bookId);
        if (book.isPresent()) {
            return new ResponseEntity<>(convertToDto(book.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity saveNewBook(@RequestBody BookDto dto) {
        Book entity = convertToEntity(dto);
        bookRepository.save(entity);
        HttpHeaders headers = new HttpHeaders();
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(entity.getId())
                .toUri();
        headers.add("Location", location.toString());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity updateBook(@PathVariable Long bookId,
                                     @RequestBody BookDto dto) {
        Optional<Book> current = bookRepository.findById(bookId);
        if (current.isPresent()) {
            dto.setId(bookId);
            bookRepository.save(convertToEntity(dto));
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{bookId}")
    public ResponseEntity patchBook(@PathVariable Long bookId,
                                    @RequestBody BookDto dto) {
        Optional<Book> current = bookRepository.findById(bookId);
        if (current.isPresent()) {
            Book book = current.get();
            if (dto.getTitle() != null) book.setTitle(dto.getTitle());
            if (dto.getAuthor() != null) book.setAuthor(dto.getAuthor());
            if (dto.getIsbn() != null) book.setIsbn(dto.getIsbn());
            if (dto.getPublishedYear() != null) book.setPublishedYear(dto.getPublishedYear());
            if (dto.getPrice() != null) book.setPrice(dto.getPrice());
            bookRepository.save(book);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity deleteBook(@PathVariable Long bookId) {
        boolean found = bookRepository.existsById(bookId);
        if (found) {
            bookRepository.deleteById(bookId);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
