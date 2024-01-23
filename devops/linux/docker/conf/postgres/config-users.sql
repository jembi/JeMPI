CREATE TABLE IF NOT EXISTS users
(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY UNIQUE,
    given_name VARCHAR(255),
    family_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    username VARCHAR(255) UNIQUE
);

\dt;
