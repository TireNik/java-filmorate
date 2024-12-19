delete
from mpa_rating;
delete
from genres;

INSERT INTO GENRES (name) VALUES
('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик');

INSERT INTO MPA_RATING (rating_id, name) VALUES
(1,'G'),
(2,'PG'),
(3,'PG-13'),
(4,'R'),
(5,'NC-17');