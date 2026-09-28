package com.movie_recommendation.controller;

import com.movie_recommendation.model.MovieMatch;
import com.movie_recommendation.service.MovieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }
@GetMapping("/search")
    public List<MovieMatch> searchMovies(@RequestParam String query) {
       return movieService.search(query);

    }

@GetMapping("/{title}/similar")
public List<MovieMatch> similarMovies(@PathVariable String title) {
        return movieService.similarMovies(title);
}
}
