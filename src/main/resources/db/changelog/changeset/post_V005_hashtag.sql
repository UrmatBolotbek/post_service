CREATE TABLE hashtag
(
    id         bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    title      varchar(64) UNIQUE NOT NULL,
    created_at timestamptz DEFAULT current_timestamp,
    updated_at timestamptz DEFAULT current_timestamp
);

CREATE TABLE post_hashtag
(
    id         bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    post_id    bigint NOT NULL,
    hashtag_id   bigint NOT NULL,
    created_at timestamptz DEFAULT current_timestamp,
    updated_at timestamptz DEFAULT current_timestamp,

    CONSTRAINT fk_post_hashtag_id FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_hashtag_post_id FOREIGN KEY (hashtag_id) REFERENCES hashtag (id)
);

CREATE INDEX idx_hashtag on hashtag(title)