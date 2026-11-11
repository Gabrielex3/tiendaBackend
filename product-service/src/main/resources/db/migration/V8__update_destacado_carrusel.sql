-- Marcar productos destacados y en carrusel
UPDATE productos SET destacado = TRUE, en_carrusel = TRUE WHERE nombre IN ('Nike Air Max 90', 'Jordan 1 Retro High OG', 'Adidas Ultraboost 22');
UPDATE productos SET destacado = TRUE WHERE nombre IN ('New Balance 550', 'Nike Dunk Low');
