-- CREATE TABLE users
-- (
--     id           VARCHAR(36) DEFAULT uuid_generate_v4()::text NOT NULL,
--     created_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
--     updated_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
--     email        VARCHAR(100) NOT NULL,
--     password     VARCHAR(255) NOT NULL,
--     is_activated BOOLEAN      NOT NULL,
--     CONSTRAINT pk_users PRIMARY KEY (id)
-- );
--
-- ALTER TABLE users
--     ADD CONSTRAINT uc_users_email UNIQUE (email);
--
-- CREATE TABLE verifications
-- (
--     token             VARCHAR(255)                NOT NULL,
--     created_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
--     updated_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
--     code              VARCHAR(6),
--     expiry_time       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
--     verification_type VARCHAR(50)                 NOT NULL,
--     user_id           VARCHAR(255),
--     CONSTRAINT pk_verifications PRIMARY KEY (token)
-- );
--
-- ALTER TABLE verifications
--     ADD CONSTRAINT FK_VERIFICATIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

/*alter table users
    owner to ben1201;

alter table verifications
    owner to ben1201;*/
