package edu.pja.sri.s34669.sri02.rest;

import edu.pja.sri.s34669.sri02.dto.BookDto;
import edu.pja.sri.s34669.sri02.model.Book;
import edu.pja.sri.s34669.sri02.repo.BookRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    private EntityModel<BookDto> toModel(Book book) {
        BookDto dto = modelMapper.map(book, BookDto.class);
        return EntityModel.of(dto,
                linkTo(methodOn(BookController.class).getBookById(book.getId())).withSelfRel(),
                linkTo(methodOn(BookController.class).getBooks()).withRel("books"));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<BookDto>>> getBooks() {
        List<EntityModel<BookDto>> books = bookRepository.findAll().stream()
                .map(this::toModel)
                .toList();
        CollectionModel<EntityModel<BookDto>> result = CollectionModel.of(books,
                linkTo(methodOn(BookController.class).getBooks()).withSelfRel());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<EntityModel<BookDto>> getBookById(@PathVariable Long bookId) {
        return bookRepository.findById(bookId)
                .map(b -> ResponseEntity.ok(toModel(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> saveNewBook(@Valid @RequestBody BookDto dto) {
        Book entity = modelMapper.map(dto, Book.class);
        entity.setId(null);
        bookRepository.save(entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(entity.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<?> updateBook(@PathVariable Long bookId, @Valid @RequestBody BookDto dto) {
        if (!bookRepository.existsById(bookId)) return ResponseEntity.notFound().build();
        dto.setId(bookId);
        bookRepository.save(modelMapper.map(dto, Book.class));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{bookId}")
    public ResponseEntity<?> patchBook(@PathVariable Long bookId, @RequestBody BookDto dto) {
        return bookRepository.findById(bookId).map(book -> {
            if (dto.getTitle() != null) book.setTitle(dto.getTitle());
            if (dto.getAuthor() != null) book.setAuthor(dto.getAuthor());
            if (dto.getIsbn() != null) book.setIsbn(dto.getIsbn());
            if (dto.getPublishedYear() != null) book.setPublishedYear(dto.getPublishedYear());
            if (dto.getPrice() != null) book.setPrice(dto.getPrice());
            bookRepository.save(book);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId) {
        if (!bookRepository.existsById(bookId)) return ResponseEntity.notFound().build();
        bookRepository.deleteById(bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
