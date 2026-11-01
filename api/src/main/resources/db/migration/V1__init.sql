CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('TOURIST', 'PROVIDER', 'ADMIN')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE poi (
    id                 BIGSERIAL PRIMARY KEY,
    external_id        VARCHAR(64)  NOT NULL UNIQUE,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    category           VARCHAR(30)  NOT NULL,
    tags               TEXT[]       NOT NULL DEFAULT '{}',
    region             VARCHAR(50),
    location           geometry(Point, 4326) NOT NULL,
    visit_duration_min INT          NOT NULL CHECK (visit_duration_min > 0),
    cost_kgs           INT          NOT NULL DEFAULT 0 CHECK (cost_kgs >= 0),
    opens_at           TIME,
    closes_at          TIME,
    season_from        SMALLINT     NOT NULL DEFAULT 1 CHECK (season_from BETWEEN 1 AND 12),
    season_to          SMALLINT     NOT NULL DEFAULT 12 CHECK (season_to BETWEEN 1 AND 12),
    popularity         SMALLINT     NOT NULL DEFAULT 3 CHECK (popularity BETWEEN 1 AND 5),
    image_url          VARCHAR(512),
    source             VARCHAR(50)
);
CREATE INDEX poi_location_gix ON poi USING GIST (location);
CREATE INDEX poi_category_idx ON poi (category);

CREATE TABLE provider (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL UNIQUE REFERENCES users (id),
    display_name VARCHAR(255) NOT NULL,
    description  TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE service_offering (
    id          BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(64) UNIQUE,
    provider_id BIGINT       NOT NULL REFERENCES provider (id),
    poi_id      BIGINT REFERENCES poi (id),
    type        VARCHAR(20)  NOT NULL CHECK (type IN ('ACCOMMODATION', 'GUIDE', 'TRANSPORT')),
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    price_kgs   INT          NOT NULL CHECK (price_kgs >= 0),
    price_unit  VARCHAR(20)  NOT NULL CHECK (price_unit IN ('PER_NIGHT', 'PER_DAY', 'PER_TRIP', 'PER_PERSON')),
    location    geometry(Point, 4326),
    capacity    INT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT accommodation_has_location CHECK (type <> 'ACCOMMODATION' OR location IS NOT NULL)
);
CREATE INDEX service_location_gix ON service_offering USING GIST (location);
CREATE INDEX service_provider_idx ON service_offering (provider_id);

CREATE TABLE travel_leg (
    from_key   VARCHAR(32) NOT NULL,
    to_key     VARCHAR(32) NOT NULL,
    duration_s INT         NOT NULL,
    distance_m INT         NOT NULL,
    provider   VARCHAR(20) NOT NULL,
    PRIMARY KEY (from_key, to_key)
);

CREATE TABLE trip_plan (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL REFERENCES users (id),
    status           VARCHAR(20) NOT NULL CHECK (status IN ('SOLVING', 'SOLVED', 'FAILED')),
    algorithm        VARCHAR(20) NOT NULL CHECK (algorithm IN ('TIMEFOLD', 'GREEDY')),
    start_date       DATE        NOT NULL,
    days             INT         NOT NULL CHECK (days BETWEEN 1 AND 21),
    budget_kgs       INT         NOT NULL CHECK (budget_kgs > 0),
    pace             VARCHAR(20) NOT NULL,
    interests        TEXT[]      NOT NULL DEFAULT '{}',
    start_poi_id     BIGINT      NOT NULL REFERENCES poi (id),
    end_poi_id       BIGINT      NOT NULL REFERENCES poi (id),
    score            VARCHAR(100),
    feasible         BOOLEAN,
    total_cost_kgs   INT,
    total_travel_min INT,
    total_value      INT,
    error            TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    solved_at        TIMESTAMPTZ
);
CREATE INDEX trip_plan_user_idx ON trip_plan (user_id);

CREATE TABLE trip_plan_day (
    id                  BIGSERIAL PRIMARY KEY,
    plan_id             BIGINT      NOT NULL REFERENCES trip_plan (id) ON DELETE CASCADE,
    day_index           INT         NOT NULL,
    date                DATE        NOT NULL,
    lodging_service_id  BIGINT REFERENCES service_offering (id),
    lodging_name        VARCHAR(255),
    travel_min          INT         NOT NULL,
    cost_kgs            INT         NOT NULL,
    end_min             INT         NOT NULL,
    UNIQUE (plan_id, day_index)
);

CREATE TABLE trip_plan_stop (
    id            BIGSERIAL PRIMARY KEY,
    plan_day_id   BIGINT NOT NULL REFERENCES trip_plan_day (id) ON DELETE CASCADE,
    seq           INT    NOT NULL,
    poi_id        BIGINT NOT NULL REFERENCES poi (id),
    arrival_min   INT    NOT NULL,
    departure_min INT    NOT NULL,
    UNIQUE (plan_day_id, seq)
);

CREATE TABLE booking_request (
    id               BIGSERIAL PRIMARY KEY,
    tourist_id       BIGINT      NOT NULL REFERENCES users (id),
    service_id       BIGINT      NOT NULL REFERENCES service_offering (id),
    plan_id          BIGINT REFERENCES trip_plan (id) ON DELETE SET NULL,
    date_from        DATE        NOT NULL,
    date_to          DATE        NOT NULL,
    guests           INT         NOT NULL DEFAULT 1 CHECK (guests > 0),
    message          TEXT,
    status           VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED')),
    provider_comment TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT booking_dates CHECK (date_to >= date_from)
);
CREATE INDEX booking_service_idx ON booking_request (service_id);
CREATE INDEX booking_tourist_idx ON booking_request (tourist_id);
