DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS friendship CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa_rating CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS useful CASCADE;

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR(200) NOT NULL UNIQUE,
    login VARCHAR(200) NOT NULL,
    name VARCHAR(200) NOT NULL,
    birth_day DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS friendship (
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    friend_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    status BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS mpa_rating(
    rating_id INTEGER NOT NULL GENERATED  BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS films(
    film_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200) NOT NULL,
    releaseDate DATE NOT NULL,
    duration INT,
    rating_id INT
);

CREATE TABLE IF NOT EXISTS likes(
    film_id INT,
    user_id INT,
    PRIMARY KEY(user_id, film_id),
    FOREIGN KEY(film_id) REFERENCES films(film_id),
    FOREIGN KEY(user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS genres(
    genre_id INTEGER NOT NULL GENERATED  BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS film_genres(
    id INT AUTO_INCREMENT,
    film_id  INT,
    genre_id INT,
    FOREIGN KEY(film_id) REFERENCES films(film_id),
    FOREIGN KEY(genre_id) REFERENCES genres(genre_id)
    );

CREATE TABLE IF NOT EXISTS useful(
    useful_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    like_id INT,
    dislike_id INT,
    FOREIGN KEY(LIKE_id) REFERENCES users(user_id),
    FOREIGN KEY(dislike_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS reviews(
    review_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content VARCHAR(500),
    useful_id INT,
    is_positive BOOLEAN NOT NULL DEFAULT TRUE,
    user_id INT,
    film_id INT,
    FOREIGN KEY(film_id) REFERENCES films(film_id),
    FOREIGN KEY(useful_id) REFERENCES useful(useful_id),
    FOREIGN KEY(user_id) REFERENCES users(user_id)
);
