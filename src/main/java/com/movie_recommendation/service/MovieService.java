package com.movie_recommendation.service;


import com.movie_recommendation.model.Movie;
import com.movie_recommendation.model.MovieData;
import com.movie_recommendation.model.MovieMatch;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class MovieService {
    private final EmbeddingModel embeddingModel;
    private final JsonMapper jsonMapper ;
    private final List<Movie> moviesEmbedding = new ArrayList<>();
    public MovieService(EmbeddingModel embeddingModel, JsonMapper jsonMapper) {
        this.embeddingModel = embeddingModel;
        this.jsonMapper = jsonMapper;
    }
    @PostConstruct
    public void initializeMovies() throws IOException {
        ClassPathResource resource = new ClassPathResource("movies.json");
        InputStream inputStream = resource.getInputStream();
        List<MovieData> movieDataList = jsonMapper.readValue(
                inputStream, new TypeReference<List<MovieData>>() {
                }
        );
        for (MovieData movieData : movieDataList) {
            float[] embedding = embeddingModel.embed(movieData.getDescription());
            Movie movie = new Movie(
                    movieData.getTitle(),
                    movieData.getDescription(),
                    embedding
            );
            moviesEmbedding.add(movie);
        }
        inputStream.close();
        System.out.println(moviesEmbedding.size() + "movies loaded with embeddings.");
    for (Movie movie : moviesEmbedding) {
        System.out.println(Arrays.toString(movie.getEmbedding()));
    }

    }
public List<MovieMatch> search(String query){
      float[] userQueryEmbedding =  embeddingModel.embed(query);
      List<MovieMatch> matches = new ArrayList<>();
for (Movie movie : moviesEmbedding) {
    double similarity = cosineSimilarity(userQueryEmbedding,movie.getEmbedding());
MovieMatch match = new MovieMatch(
        movie.getTitle(),
        movie.getDescription(),
        similarity
);

matches.add(match);
}
sortBySimilarity(matches);

return topKMatches(matches,3);
}

private Movie findMovie(String title){
        for (Movie movie : moviesEmbedding) {
            if (movie.getTitle().equals(title)) {
                return movie;
            }
        }
        throw new IllegalArgumentException("No such movie");
}

public List<MovieMatch> similarMovies(String title){
        Movie selectedMovie = findMovie(title);
        List<MovieMatch> matches = new ArrayList<>();

        for (Movie movie : moviesEmbedding) {
            if (movie.getTitle().equals(title)) {
                continue;
            }

            double similarity = cosineSimilarity(
                    selectedMovie.getEmbedding(),
                    movie.getEmbedding()
            );
            MovieMatch match = new MovieMatch(
                    movie.getTitle(),
                    movie.getDescription(),
                    similarity
            );
            matches.add(match);
        }
        sortBySimilarity(matches);
        return topKMatches(matches,3);
}

private double cosineSimilarity(float[] embedding1, float[] embedding2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += (embedding1[i] * embedding2[i]);
            normA += (embedding1[i] * embedding1[i]);
            normB += (embedding2[i] * embedding2[i]);
        }
        if(normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
}
private void sortBySimilarity(List<MovieMatch> matches) {
    Collections.sort(matches,
            (first, second) -> Double.compare(
                    second.getMatch(),
                    first.getMatch()
            ));
}
private List<MovieMatch> topKMatches(List<MovieMatch> matches, int k){
        List<MovieMatch> topKMatches = new ArrayList<>();
        int numOfMatches = Math.min(k, matches.size());
        for (int i = 0; i < numOfMatches; i++) {
            topKMatches.add(matches.get(i));
        }
        return topKMatches;
}
}
