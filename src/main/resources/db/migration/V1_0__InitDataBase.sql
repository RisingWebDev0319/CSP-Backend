--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: app_user; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE app_user (
    id bigint NOT NULL,
    email character varying(255),
    locked boolean,
    name character varying(255),
    password character varying(255)
);


-- ALTER TABLE app_user OWNER TO csp;

--
-- Name: audit; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE audit (
    id bigint NOT NULL,
    user_signature character varying(255) NOT NULL,
    user_id bigint
);


-- ALTER TABLE audit OWNER TO csp;

--
-- Name: av_history_record; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE av_history_record (
    id bigint NOT NULL,
    edit_date date,
    therapist_id bigint,
    new_state_id bigint,
    prev_state_id bigint,
    user_id bigint
);


-- ALTER TABLE av_history_record OWNER TO csp;

--
-- Name: av_history_state; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE av_history_state (
    id bigint NOT NULL,
    date date NOT NULL,
    time_end bigint NOT NULL,
    time_start bigint NOT NULL,
    type character varying(255)
);


-- ALTER TABLE av_history_state OWNER TO csp;

--
-- Name: av_request; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE av_request (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    end_date date NOT NULL,
    start_date date NOT NULL
);


-- ALTER TABLE av_request OWNER TO csp;

--
-- Name: av_therapist_day_record; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE av_therapist_day_record (
    id bigint NOT NULL,
    date date NOT NULL,
    therapist_id bigint NOT NULL
);


-- ALTER TABLE av_therapist_day_record OWNER TO csp;

--
-- Name: av_therapist_request; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE av_therapist_request (
    id bigint NOT NULL,
    message character varying(255),
    status character varying(255),
    therapist_id bigint NOT NULL,
    av_request_id bigint
);


-- ALTER TABLE av_therapist_request OWNER TO csp;

--
-- Name: av_time_record; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE av_time_record (
    id bigint NOT NULL,
    time_end bigint,
    time_start bigint,
    type character varying(255),
    av_therapist_day_record_id bigint
);


-- ALTER TABLE av_time_record OWNER TO csp;

--
-- Name: calendar_event; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE calendar_event (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    capacity bigint,
    date_end date,
    date_start date,
    days bytea,
    time_clean integer,
    time_prep integer,
    time_processing integer,
    name character varying(255),
    "time" time without time zone,
    event_id bigint,
    room_id bigint,
    therapist_id bigint
);


-- ALTER TABLE calendar_event OWNER TO csp;

--
-- Name: calendar_event_equipments; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE calendar_event_equipments (
    calendar_event_id bigint NOT NULL,
    equipment_id bigint NOT NULL
);


-- ALTER TABLE calendar_event_equipments OWNER TO csp;

--
-- Name: client; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE client (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    entity_id character varying(255),
    external_id bigint,
    goals text,
    name character varying(255),
    reasons text,
    record_type character varying(255)
);


-- ALTER TABLE client OWNER TO csp;

--
-- Name: client_matching_confirmation; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE client_matching_confirmation (
    client_id bigint NOT NULL,
    session_id bigint NOT NULL,
    secret character varying(255)
);


-- ALTER TABLE client_matching_confirmation OWNER TO csp;

--
-- Name: concrete_calendar_event; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE concrete_calendar_event (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    calendar_event_id bigint,
    changed boolean NOT NULL,
    date date,
    time_clean integer,
    time_prep integer,
    time_processing integer,
    name character varying(255),
    "time" time without time zone,
    event_id bigint,
    room_id bigint,
    therapist_id bigint
);


-- ALTER TABLE concrete_calendar_event OWNER TO csp;

--
-- Name: concrete_calendar_event_clients; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE concrete_calendar_event_clients (
    concrete_calendar_event_id bigint NOT NULL,
    client_id bigint NOT NULL
);


-- ALTER TABLE concrete_calendar_event_clients OWNER TO csp;

--
-- Name: concrete_event; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE concrete_event (
    id bigint NOT NULL,
    client_id bigint NOT NULL,
    date date NOT NULL,
    time_clean integer,
    time_prep integer,
    time_processing integer,
    note character varying(255),
    service_id bigint,
    session_id bigint NOT NULL,
    state integer NOT NULL,
    sub_status character varying(255),
    "time" time without time zone NOT NULL,
    event_id bigint,
    room_id bigint,
    therapist_id bigint
);


-- ALTER TABLE concrete_event OWNER TO csp;

--
-- Name: concrete_event_change; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE concrete_event_change (
    id bigint NOT NULL,
    concrete_event_id bigint,
    event_code character varying(255) NOT NULL,
    new_value_id bigint,
    previous_value_id bigint
);


-- ALTER TABLE concrete_event_change OWNER TO csp;

--
-- Name: concrete_event_change_value; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE concrete_event_change_value (
    id bigint NOT NULL,
    client character varying(255) NOT NULL,
    date character varying(255) NOT NULL,
    duration_clean character varying(255),
    duration_prep character varying(255),
    duration_processing character varying(255),
    event character varying(255),
    note character varying(255),
    room character varying(255) NOT NULL,
    service character varying(255),
    session character varying(255) NOT NULL,
    sub_status character varying(255),
    therapist character varying(255) NOT NULL,
    "time" time without time zone NOT NULL
);


-- ALTER TABLE concrete_event_change_value OWNER TO csp;

--
-- Name: concrete_event_reconcile; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE concrete_event_reconcile (
    cost integer,
    estimate_id bigint,
    estimate_state integer NOT NULL,
    reconcile_state integer NOT NULL,
    concrete_event_id bigint NOT NULL,
    audit_id bigint,
    reconcile_id bigint
);


-- ALTER TABLE concrete_event_reconcile OWNER TO csp;

--
-- Name: concrete_event_sub_status; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE concrete_event_sub_status (
    id bigint NOT NULL,
    name character varying(255)
);


-- ALTER TABLE concrete_event_sub_status OWNER TO csp;

--
-- Name: equipment; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE equipment (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    capacity bigint,
    name character varying(255)
);


-- ALTER TABLE equipment OWNER TO csp;

--
-- Name: estimate; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE estimate (
    id bigint NOT NULL,
    client_id bigint NOT NULL,
    external_id bigint,
    sent boolean NOT NULL
);


-- ALTER TABLE estimate OWNER TO csp;

--
-- Name: estimate_concrete_event_reconcile; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE estimate_concrete_event_reconcile (
    estimate_id bigint NOT NULL,
    concrete_event_reconcile_id bigint NOT NULL
);


-- ALTER TABLE estimate_concrete_event_reconcile OWNER TO csp;

--
-- Name: event; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE event (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    external_id bigint,
    name character varying(255),
    price integer,
    time_clean integer,
    time_prep integer,
    time_processing integer
);


-- ALTER TABLE event OWNER TO csp;

--
-- Name: event_record; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE event_record (
    id bigint NOT NULL,
    capacity bigint,
    event_id bigint
);


-- ALTER TABLE event_record OWNER TO csp;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: csp
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- ALTER TABLE hibernate_sequence OWNER TO csp;

--
-- Name: hs_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_column (
    id bigint NOT NULL,
    editable boolean NOT NULL,
    permanent boolean NOT NULL,
    "position" integer,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    custom_column_id bigint,
    select_column_id bigint,
    hs_table_id bigint
);


-- ALTER TABLE hs_column OWNER TO csp;

--
-- Name: hs_condition_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_condition_column (
    id bigint NOT NULL,
    section_type character varying(255) NOT NULL
);


-- ALTER TABLE hs_condition_column OWNER TO csp;

--
-- Name: hs_condition_column_value; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_condition_column_value (
    id bigint NOT NULL,
    default_flag_color character varying(255) NOT NULL,
    title character varying(255) NOT NULL
);


-- ALTER TABLE hs_condition_column_value OWNER TO csp;

--
-- Name: hs_condition_column_value__hs_condition_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_condition_column_value__hs_condition_column (
    hs_condition_column_value_id bigint NOT NULL,
    hs_condition_column_id bigint NOT NULL
);


-- ALTER TABLE hs_condition_column_value__hs_condition_column OWNER TO csp;

--
-- Name: hs_custom_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_custom_column (
    id bigint NOT NULL,
    section_type character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL
);


-- ALTER TABLE hs_custom_column OWNER TO csp;

--
-- Name: hs_custom_column_value; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_custom_column_value (
    id bigint NOT NULL,
    permanent boolean NOT NULL,
    "position" integer NOT NULL,
    title character varying(255) NOT NULL,
    value bigint NOT NULL,
    hs_custom_column_id bigint
);


-- ALTER TABLE hs_custom_column_value OWNER TO csp;

--
-- Name: hs_flag_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_flag_column (
    id bigint NOT NULL,
    section_type character varying(255) NOT NULL
);


-- ALTER TABLE hs_flag_column OWNER TO csp;

--
-- Name: hs_flag_column_value; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_flag_column_value (
    id bigint NOT NULL,
    color character varying(255) NOT NULL,
    permanent boolean NOT NULL,
    title character varying(255) NOT NULL,
    hs_flag_column_id bigint
);


-- ALTER TABLE hs_flag_column_value OWNER TO csp;

--
-- Name: hs_row; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_row (
    id bigint NOT NULL,
    client_id bigint NOT NULL,
    section character varying(255) NOT NULL,
    session_id bigint NOT NULL
);


-- ALTER TABLE hs_row OWNER TO csp;

--
-- Name: hs_row_item; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_row_item (
    column_id bigint NOT NULL,
    color_value character varying(255),
    select_value bigint NOT NULL,
    text_value text,
    row_id bigint NOT NULL
);


-- ALTER TABLE hs_row_item OWNER TO csp;

--
-- Name: hs_select_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_select_column (
    id bigint NOT NULL,
    section_type character varying(255) NOT NULL
);


-- ALTER TABLE hs_select_column OWNER TO csp;

--
-- Name: hs_select_column_value; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_select_column_value (
    id bigint NOT NULL,
    permanent boolean NOT NULL,
    "position" integer NOT NULL,
    title character varying(255) NOT NULL,
    value bigint NOT NULL,
    hs_select_column_id bigint
);


-- ALTER TABLE hs_select_column_value OWNER TO csp;

--
-- Name: hs_table; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE hs_table (
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL
);


-- ALTER TABLE hs_table OWNER TO csp;

--
-- Name: ht_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ht_column (
    id bigint NOT NULL,
    editable boolean NOT NULL,
    permanent boolean NOT NULL,
    "position" integer,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    custom_column_id bigint
);


-- ALTER TABLE ht_column OWNER TO csp;

--
-- Name: ht_custom_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ht_custom_column (
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL
);


-- ALTER TABLE ht_custom_column OWNER TO csp;

--
-- Name: ht_custom_column_value; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ht_custom_column_value (
    id bigint NOT NULL,
    "position" bigint NOT NULL,
    title character varying(255) NOT NULL,
    value bigint NOT NULL,
    hs_custom_column_id bigint
);


-- ALTER TABLE ht_custom_column_value OWNER TO csp;

--
-- Name: ht_row; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ht_row (
    client_id bigint NOT NULL,
    session_id bigint NOT NULL
);


-- ALTER TABLE ht_row OWNER TO csp;

--
-- Name: ht_row_item; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ht_row_item (
    column_id bigint NOT NULL,
    select_value integer NOT NULL,
    text_value character varying(255),
    client_id bigint NOT NULL,
    session_id bigint NOT NULL
);


-- ALTER TABLE ht_row_item OWNER TO csp;

--
-- Name: preliminary_event; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE preliminary_event (
    service_id bigint NOT NULL,
    date date NOT NULL,
    clean integer,
    prep integer,
    processing integer,
    note character varying(255),
    state character varying(255),
    "time" time without time zone,
    datafk_client_id bigint NOT NULL,
    datafk_session_id bigint NOT NULL,
    matching_confirmation_client_id bigint,
    matching_confirmation_session_id bigint,
    room_id bigint,
    therapist_id bigint
);


-- ALTER TABLE preliminary_event OWNER TO csp;

--
-- Name: protocol; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE protocol (
    id bigint NOT NULL,
    external_id bigint,
    name character varying(255),
    package_sign boolean NOT NULL,
    price character varying(255)
);


-- ALTER TABLE protocol OWNER TO csp;

--
-- Name: reconcile; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE reconcile (
    id bigint NOT NULL,
    user_signature character varying(255) NOT NULL,
    user_id bigint
);


-- ALTER TABLE reconcile OWNER TO csp;

--
-- Name: remote_server_settings; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE remote_server_settings (
    id bigint NOT NULL,
    cron_expression character varying(255),
    cron_timezone character varying(255),
    update_time bigint NOT NULL,
    client_info_url character varying(255),
    clients_url character varying(255),
    events_url character varying(255),
    protocol_packages_url character varying(255),
    protocols_url character varying(255),
    services_url character varying(255),
    therapists_url character varying(255)
);


-- ALTER TABLE remote_server_settings OWNER TO csp;

--
-- Name: restriction; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE restriction (
    id bigint NOT NULL,
    linked_id bigint,
    name character varying(255),
    type character varying(255)
);


-- ALTER TABLE restriction OWNER TO csp;

--
-- Name: restriction_equipments; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE restriction_equipments (
    restriction_id bigint NOT NULL,
    equipment_id bigint NOT NULL
);


-- ALTER TABLE restriction_equipments OWNER TO csp;

--
-- Name: restriction_rooms; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE restriction_rooms (
    restriction_id bigint NOT NULL,
    room_id bigint NOT NULL
);


-- ALTER TABLE restriction_rooms OWNER TO csp;

--
-- Name: room; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE room (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    capacity bigint,
    name character varying(255)
);


-- ALTER TABLE room OWNER TO csp;

--
-- Name: room_booked_time; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE room_booked_time (
    id bigint NOT NULL,
    booked bigint,
    date character varying(255),
    room_id bigint
);


-- ALTER TABLE room_booked_time OWNER TO csp;

--
-- Name: service; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE service (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    external_id bigint,
    name character varying(255),
    price integer,
    time_clean integer,
    time_prep integer,
    time_processing integer
);


-- ALTER TABLE service OWNER TO csp;

--
-- Name: service_category; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE service_category (
    id bigint NOT NULL,
    name character varying(255)
);


-- ALTER TABLE service_category OWNER TO csp;

--
-- Name: services_categories_href; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE services_categories_href (
    service_category_id bigint NOT NULL,
    service_id bigint NOT NULL
);


-- ALTER TABLE services_categories_href OWNER TO csp;

--
-- Name: session; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE session (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    end_date date,
    name character varying(255) NOT NULL,
    start_date date
);


-- ALTER TABLE session OWNER TO csp;

--
-- Name: session_clients; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE session_clients (
    session_id bigint NOT NULL,
    client_id bigint NOT NULL
);


-- ALTER TABLE session_clients OWNER TO csp;

--
-- Name: session_therapists; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE session_therapists (
    session_id bigint NOT NULL,
    therapist_id bigint NOT NULL
);


-- ALTER TABLE session_therapists OWNER TO csp;

--
-- Name: ss_custom_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ss_custom_column (
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL
);


-- ALTER TABLE ss_custom_column OWNER TO csp;

--
-- Name: ss_custom_column_value; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ss_custom_column_value (
    id bigint NOT NULL,
    column_order bigint,
    title character varying(255),
    column_value bigint,
    custom_column_id bigint
);


-- ALTER TABLE ss_custom_column_value OWNER TO csp;

--
-- Name: ss_session_client_data; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ss_session_client_data (
    client_id bigint NOT NULL,
    session_id bigint NOT NULL
);


-- ALTER TABLE ss_session_client_data OWNER TO csp;

--
-- Name: ss_session_client_data_item; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ss_session_client_data_item (
    column_id bigint NOT NULL,
    select_value bigint,
    text_value character varying(255),
    client_id bigint NOT NULL,
    session_id bigint NOT NULL
);


-- ALTER TABLE ss_session_client_data_item OWNER TO csp;

--
-- Name: ss_table_column; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE ss_table_column (
    id bigint NOT NULL,
    title character varying(255),
    type character varying(255),
    custom_column_id bigint
);


-- ALTER TABLE ss_table_column OWNER TO csp;

--
-- Name: therapist; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE therapist (
    id bigint NOT NULL,
    archived boolean NOT NULL,
    email character varying(255),
    external_id character varying(255),
    name character varying(255)
);


-- ALTER TABLE therapist OWNER TO csp;

--
-- Name: therapist_events; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE therapist_events (
    therapist_id bigint NOT NULL,
    event_id bigint NOT NULL
);


-- ALTER TABLE therapist_events OWNER TO csp;

--
-- Name: therapist_preferred_rooms; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE therapist_preferred_rooms (
    therapist_id bigint NOT NULL,
    room_id bigint NOT NULL
);


-- ALTER TABLE therapist_preferred_rooms OWNER TO csp;

--
-- Name: therapist_service_categories; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE therapist_service_categories (
    therapist_id bigint NOT NULL,
    service_category_id bigint NOT NULL
);


-- ALTER TABLE therapist_service_categories OWNER TO csp;

--
-- Name: therapist_services; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE therapist_services (
    therapist_id bigint NOT NULL,
    service_id bigint NOT NULL
);


-- ALTER TABLE therapist_services OWNER TO csp;

--
-- Name: user_modules; Type: TABLE; Schema: public; Owner: csp; Tablespace: 
--

CREATE TABLE user_modules (
    user_id bigint NOT NULL,
    module character varying(255) NOT NULL
);


-- ALTER TABLE user_modules OWNER TO csp;

--
-- Name: app_user_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY app_user
    ADD CONSTRAINT app_user_pkey PRIMARY KEY (id);


--
-- Name: audit_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY audit
    ADD CONSTRAINT audit_pkey PRIMARY KEY (id);


--
-- Name: av_history_record_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY av_history_record
    ADD CONSTRAINT av_history_record_pkey PRIMARY KEY (id);


--
-- Name: av_history_state_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY av_history_state
    ADD CONSTRAINT av_history_state_pkey PRIMARY KEY (id);


--
-- Name: av_request_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY av_request
    ADD CONSTRAINT av_request_pkey PRIMARY KEY (id);


--
-- Name: av_therapist_day_record_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY av_therapist_day_record
    ADD CONSTRAINT av_therapist_day_record_pkey PRIMARY KEY (id);


--
-- Name: av_therapist_request_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY av_therapist_request
    ADD CONSTRAINT av_therapist_request_pkey PRIMARY KEY (id);


--
-- Name: av_time_record_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY av_time_record
    ADD CONSTRAINT av_time_record_pkey PRIMARY KEY (id);


--
-- Name: calendar_event_equipments_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY calendar_event_equipments
    ADD CONSTRAINT calendar_event_equipments_pkey PRIMARY KEY (calendar_event_id, equipment_id);


--
-- Name: calendar_event_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY calendar_event
    ADD CONSTRAINT calendar_event_pkey PRIMARY KEY (id);


--
-- Name: client_matching_confirmation_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY client_matching_confirmation
    ADD CONSTRAINT client_matching_confirmation_pkey PRIMARY KEY (client_id, session_id);


--
-- Name: client_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY client
    ADD CONSTRAINT client_pkey PRIMARY KEY (id);


--
-- Name: concrete_calendar_event_clients_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY concrete_calendar_event_clients
    ADD CONSTRAINT concrete_calendar_event_clients_pkey PRIMARY KEY (concrete_calendar_event_id, client_id);


--
-- Name: concrete_calendar_event_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY concrete_calendar_event
    ADD CONSTRAINT concrete_calendar_event_pkey PRIMARY KEY (id);


--
-- Name: concrete_event_change_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY concrete_event_change
    ADD CONSTRAINT concrete_event_change_pkey PRIMARY KEY (id);


--
-- Name: concrete_event_change_value_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY concrete_event_change_value
    ADD CONSTRAINT concrete_event_change_value_pkey PRIMARY KEY (id);


--
-- Name: concrete_event_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY concrete_event
    ADD CONSTRAINT concrete_event_pkey PRIMARY KEY (id);


--
-- Name: concrete_event_reconcile_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY concrete_event_reconcile
    ADD CONSTRAINT concrete_event_reconcile_pkey PRIMARY KEY (concrete_event_id);


--
-- Name: concrete_event_sub_status_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY concrete_event_sub_status
    ADD CONSTRAINT concrete_event_sub_status_pkey PRIMARY KEY (id);


--
-- Name: equipment_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY equipment
    ADD CONSTRAINT equipment_pkey PRIMARY KEY (id);


--
-- Name: estimate_concrete_event_reconcile_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY estimate_concrete_event_reconcile
    ADD CONSTRAINT estimate_concrete_event_reconcile_pkey PRIMARY KEY (estimate_id, concrete_event_reconcile_id);


--
-- Name: estimate_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY estimate
    ADD CONSTRAINT estimate_pkey PRIMARY KEY (id);


--
-- Name: event_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_pkey PRIMARY KEY (id);


--
-- Name: event_record_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY event_record
    ADD CONSTRAINT event_record_pkey PRIMARY KEY (id);


--
-- Name: hs_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_column
    ADD CONSTRAINT hs_column_pkey PRIMARY KEY (id);


--
-- Name: hs_condition_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_condition_column
    ADD CONSTRAINT hs_condition_column_pkey PRIMARY KEY (id);


--
-- Name: hs_condition_column_value__hs_condition_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_condition_column_value__hs_condition_column
    ADD CONSTRAINT hs_condition_column_value__hs_condition_column_pkey PRIMARY KEY (hs_condition_column_value_id, hs_condition_column_id);


--
-- Name: hs_condition_column_value_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_condition_column_value
    ADD CONSTRAINT hs_condition_column_value_pkey PRIMARY KEY (id);


--
-- Name: hs_custom_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_custom_column
    ADD CONSTRAINT hs_custom_column_pkey PRIMARY KEY (id);


--
-- Name: hs_custom_column_value_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_custom_column_value
    ADD CONSTRAINT hs_custom_column_value_pkey PRIMARY KEY (id);


--
-- Name: hs_flag_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_flag_column
    ADD CONSTRAINT hs_flag_column_pkey PRIMARY KEY (id);


--
-- Name: hs_flag_column_value_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_flag_column_value
    ADD CONSTRAINT hs_flag_column_value_pkey PRIMARY KEY (id);


--
-- Name: hs_row_item_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_row_item
    ADD CONSTRAINT hs_row_item_pkey PRIMARY KEY (column_id, row_id);


--
-- Name: hs_row_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_row
    ADD CONSTRAINT hs_row_pkey PRIMARY KEY (id);


--
-- Name: hs_select_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_select_column
    ADD CONSTRAINT hs_select_column_pkey PRIMARY KEY (id);


--
-- Name: hs_select_column_value_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_select_column_value
    ADD CONSTRAINT hs_select_column_value_pkey PRIMARY KEY (id);


--
-- Name: hs_table_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY hs_table
    ADD CONSTRAINT hs_table_pkey PRIMARY KEY (id);


--
-- Name: ht_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ht_column
    ADD CONSTRAINT ht_column_pkey PRIMARY KEY (id);


--
-- Name: ht_custom_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ht_custom_column
    ADD CONSTRAINT ht_custom_column_pkey PRIMARY KEY (id);


--
-- Name: ht_custom_column_value_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ht_custom_column_value
    ADD CONSTRAINT ht_custom_column_value_pkey PRIMARY KEY (id);


--
-- Name: ht_row_item_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ht_row_item
    ADD CONSTRAINT ht_row_item_pkey PRIMARY KEY (column_id, client_id, session_id);


--
-- Name: ht_row_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ht_row
    ADD CONSTRAINT ht_row_pkey PRIMARY KEY (client_id, session_id);


--
-- Name: linked_id_type_uk; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY restriction
    ADD CONSTRAINT linked_id_type_uk UNIQUE (linked_id, type);


--
-- Name: preliminary_event_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY preliminary_event
    ADD CONSTRAINT preliminary_event_pkey PRIMARY KEY (datafk_client_id, datafk_session_id, service_id);


--
-- Name: protocol_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY protocol
    ADD CONSTRAINT protocol_pkey PRIMARY KEY (id);


--
-- Name: reconcile_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY reconcile
    ADD CONSTRAINT reconcile_pkey PRIMARY KEY (id);


--
-- Name: remote_server_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY remote_server_settings
    ADD CONSTRAINT remote_server_settings_pkey PRIMARY KEY (id);


--
-- Name: restriction_equipments_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY restriction_equipments
    ADD CONSTRAINT restriction_equipments_pkey PRIMARY KEY (restriction_id, equipment_id);


--
-- Name: restriction_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY restriction
    ADD CONSTRAINT restriction_pkey PRIMARY KEY (id);


--
-- Name: restriction_rooms_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY restriction_rooms
    ADD CONSTRAINT restriction_rooms_pkey PRIMARY KEY (restriction_id, room_id);


--
-- Name: room_booked_time_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY room_booked_time
    ADD CONSTRAINT room_booked_time_pkey PRIMARY KEY (id);


--
-- Name: room_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY room
    ADD CONSTRAINT room_pkey PRIMARY KEY (id);


--
-- Name: service_category_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY service_category
    ADD CONSTRAINT service_category_pkey PRIMARY KEY (id);


--
-- Name: service_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_pkey PRIMARY KEY (id);


--
-- Name: services_categories_href_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY services_categories_href
    ADD CONSTRAINT services_categories_href_pkey PRIMARY KEY (service_category_id, service_id);


--
-- Name: session_clients_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY session_clients
    ADD CONSTRAINT session_clients_pkey PRIMARY KEY (session_id, client_id);


--
-- Name: session_name_dates_uk; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY session
    ADD CONSTRAINT session_name_dates_uk UNIQUE (name, start_date, end_date);


--
-- Name: session_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY session
    ADD CONSTRAINT session_pkey PRIMARY KEY (id);


--
-- Name: session_therapists_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY session_therapists
    ADD CONSTRAINT session_therapists_pkey PRIMARY KEY (session_id, therapist_id);


--
-- Name: ss_custom_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ss_custom_column
    ADD CONSTRAINT ss_custom_column_pkey PRIMARY KEY (id);


--
-- Name: ss_custom_column_value_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ss_custom_column_value
    ADD CONSTRAINT ss_custom_column_value_pkey PRIMARY KEY (id);


--
-- Name: ss_session_client_data_item_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ss_session_client_data_item
    ADD CONSTRAINT ss_session_client_data_item_pkey PRIMARY KEY (column_id, client_id, session_id);


--
-- Name: ss_session_client_data_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ss_session_client_data
    ADD CONSTRAINT ss_session_client_data_pkey PRIMARY KEY (client_id, session_id);


--
-- Name: ss_table_column_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY ss_table_column
    ADD CONSTRAINT ss_table_column_pkey PRIMARY KEY (id);


--
-- Name: therapist_events_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY therapist_events
    ADD CONSTRAINT therapist_events_pkey PRIMARY KEY (therapist_id, event_id);


--
-- Name: therapist_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY therapist
    ADD CONSTRAINT therapist_pkey PRIMARY KEY (id);


--
-- Name: therapist_preferred_rooms_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY therapist_preferred_rooms
    ADD CONSTRAINT therapist_preferred_rooms_pkey PRIMARY KEY (therapist_id, room_id);


--
-- Name: therapist_service_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY therapist_service_categories
    ADD CONSTRAINT therapist_service_categories_pkey PRIMARY KEY (therapist_id, service_category_id);


--
-- Name: therapist_services_pkey; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY therapist_services
    ADD CONSTRAINT therapist_services_pkey PRIMARY KEY (therapist_id, service_id);


--
-- Name: uk3fqfx4ddq21lgf8mcosrph66s; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY av_therapist_day_record
    ADD CONSTRAINT uk3fqfx4ddq21lgf8mcosrph66s UNIQUE (date, therapist_id);


--
-- Name: uk_1j9d9a06i600gd43uu3km82jw; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY app_user
    ADD CONSTRAINT uk_1j9d9a06i600gd43uu3km82jw UNIQUE (email);


--
-- Name: uk_4l8mm4fqoos6fcbx76rvqxer; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY room
    ADD CONSTRAINT uk_4l8mm4fqoos6fcbx76rvqxer UNIQUE (name);


--
-- Name: uk_8hbdwj94fo2sv9ib0utj06u52; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY service
    ADD CONSTRAINT uk_8hbdwj94fo2sv9ib0utj06u52 UNIQUE (external_id);


--
-- Name: uk_9rfb4a0j3jq5iwasecaj9qj5i; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY restriction
    ADD CONSTRAINT uk_9rfb4a0j3jq5iwasecaj9qj5i UNIQUE (name);


--
-- Name: uk_faaj30mspthno6k9c85j09788; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY therapist
    ADD CONSTRAINT uk_faaj30mspthno6k9c85j09788 UNIQUE (email);


--
-- Name: uk_g5wquuxklvswxb332b57fdg4; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY av_history_record
    ADD CONSTRAINT uk_g5wquuxklvswxb332b57fdg4 UNIQUE (prev_state_id);


--
-- Name: uk_h8rcqs3d8l36vl9e4cjcimeby; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY calendar_event
    ADD CONSTRAINT uk_h8rcqs3d8l36vl9e4cjcimeby UNIQUE (name);


--
-- Name: uk_jlsrbse73ku13qrpiug0kpx18; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY av_history_record
    ADD CONSTRAINT uk_jlsrbse73ku13qrpiug0kpx18 UNIQUE (new_state_id);


--
-- Name: uk_ktb15ruomkpvoy4u1tooos30a; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY concrete_event_change
    ADD CONSTRAINT uk_ktb15ruomkpvoy4u1tooos30a UNIQUE (new_value_id);


--
-- Name: uk_lqx9n1m5sctpa050y61g6laj6; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY concrete_event_change
    ADD CONSTRAINT uk_lqx9n1m5sctpa050y61g6laj6 UNIQUE (previous_value_id);


--
-- Name: uk_m6whtgwe6ya84f59coio1j1wh; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY equipment
    ADD CONSTRAINT uk_m6whtgwe6ya84f59coio1j1wh UNIQUE (name);


--
-- Name: uk_p2jtxcl2etjafo9ivq9b1apkd; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY event
    ADD CONSTRAINT uk_p2jtxcl2etjafo9ivq9b1apkd UNIQUE (external_id);


--
-- Name: uk_rq4iui706ylaju1tyhc8j6wo4; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY service_category
    ADD CONSTRAINT uk_rq4iui706ylaju1tyhc8j6wo4 UNIQUE (name);


--
-- Name: uk_svb1cmu0ip6vl02hka1w6wqon; Type: CONSTRAINT; Schema: public; Owner: csp; Tablespace: 
--

ALTER TABLE ONLY therapist
    ADD CONSTRAINT uk_svb1cmu0ip6vl02hka1w6wqon UNIQUE (external_id);


--
-- Name: fk1o8wlvm7apuofm5haxl8egk05; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY preliminary_event
    ADD CONSTRAINT fk1o8wlvm7apuofm5haxl8egk05 FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fk1pfxb0qskpkxrhx9l1p6olwwb; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_event_change
    ADD CONSTRAINT fk1pfxb0qskpkxrhx9l1p6olwwb FOREIGN KEY (new_value_id) REFERENCES concrete_event_change_value(id);


--
-- Name: fk22sv0g0citvunrmqto85h2qk9; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_event
    ADD CONSTRAINT fk22sv0g0citvunrmqto85h2qk9 FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fk23708mvastyded7aujbtp7bqu; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY av_history_record
    ADD CONSTRAINT fk23708mvastyded7aujbtp7bqu FOREIGN KEY (prev_state_id) REFERENCES av_history_state(id);


--
-- Name: fk25ihtktir135kd50yprjulxru; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY estimate_concrete_event_reconcile
    ADD CONSTRAINT fk25ihtktir135kd50yprjulxru FOREIGN KEY (estimate_id) REFERENCES estimate(id);


--
-- Name: fk2eeciyh7pxvbyxf6asvdsdtoj; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY session_therapists
    ADD CONSTRAINT fk2eeciyh7pxvbyxf6asvdsdtoj FOREIGN KEY (session_id) REFERENCES session(id);


--
-- Name: fk2q8kbxvb0ub1ddmfk3k1kg4ee; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY ss_session_client_data_item
    ADD CONSTRAINT fk2q8kbxvb0ub1ddmfk3k1kg4ee FOREIGN KEY (client_id, session_id) REFERENCES ss_session_client_data(client_id, session_id);


--
-- Name: fk379pltwwacs22dxxxj4o8qjt9; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY therapist_service_categories
    ADD CONSTRAINT fk379pltwwacs22dxxxj4o8qjt9 FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fk3yfwpc8qfcr2rjkus0wpruh87; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_calendar_event
    ADD CONSTRAINT fk3yfwpc8qfcr2rjkus0wpruh87 FOREIGN KEY (room_id) REFERENCES room(id);


--
-- Name: fk43vt9og5e3d8lm89uehvxts5s; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY estimate_concrete_event_reconcile
    ADD CONSTRAINT fk43vt9og5e3d8lm89uehvxts5s FOREIGN KEY (concrete_event_reconcile_id) REFERENCES concrete_event_reconcile(concrete_event_id);


--
-- Name: fk453gd25akwn638ymmrw8ae2f2; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_calendar_event_clients
    ADD CONSTRAINT fk453gd25akwn638ymmrw8ae2f2 FOREIGN KEY (concrete_calendar_event_id) REFERENCES concrete_calendar_event(id);


--
-- Name: fk4ixei60d08dnc7ieb3mtl0oqm; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY ht_row_item
    ADD CONSTRAINT fk4ixei60d08dnc7ieb3mtl0oqm FOREIGN KEY (client_id, session_id) REFERENCES ht_row(client_id, session_id);


--
-- Name: fk592wsdl2da13wvboos7hfvgfn; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY ss_custom_column_value
    ADD CONSTRAINT fk592wsdl2da13wvboos7hfvgfn FOREIGN KEY (custom_column_id) REFERENCES ss_custom_column(id);


--
-- Name: fk5hpekq7iseiglofo06y0d6731; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY restriction_rooms
    ADD CONSTRAINT fk5hpekq7iseiglofo06y0d6731 FOREIGN KEY (restriction_id) REFERENCES restriction(id);


--
-- Name: fk5jqgihg47sg6eix1uaxo7ttia; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY event_record
    ADD CONSTRAINT fk5jqgihg47sg6eix1uaxo7ttia FOREIGN KEY (event_id) REFERENCES event(id);


--
-- Name: fk5sgtr0ekjlmngh32n2sqnxt39; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY restriction_rooms
    ADD CONSTRAINT fk5sgtr0ekjlmngh32n2sqnxt39 FOREIGN KEY (room_id) REFERENCES room(id);


--
-- Name: fk64ufygprfj19r0mx727st9qe9; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY services_categories_href
    ADD CONSTRAINT fk64ufygprfj19r0mx727st9qe9 FOREIGN KEY (service_id) REFERENCES service(id);


--
-- Name: fk6b714fog3tvbw412ftd67b51l; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY session_therapists
    ADD CONSTRAINT fk6b714fog3tvbw412ftd67b51l FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fk6logf8eikjppcbtmtmxt6ttf4; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY services_categories_href
    ADD CONSTRAINT fk6logf8eikjppcbtmtmxt6ttf4 FOREIGN KEY (service_category_id) REFERENCES service_category(id);


--
-- Name: fk7n03uc3q2cnk3p9dmv2a8kmx3; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_event_reconcile
    ADD CONSTRAINT fk7n03uc3q2cnk3p9dmv2a8kmx3 FOREIGN KEY (concrete_event_id) REFERENCES concrete_event(id);


--
-- Name: fk8fh6le8orgyhw5iu1clroadun; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY therapist_preferred_rooms
    ADD CONSTRAINT fk8fh6le8orgyhw5iu1clroadun FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fk8t4wkb3ew151ktvikwjc8yo2g; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY hs_select_column_value
    ADD CONSTRAINT fk8t4wkb3ew151ktvikwjc8yo2g FOREIGN KEY (hs_select_column_id) REFERENCES hs_select_column(id);


--
-- Name: fk8yddkth6xkly2q1nfubpqiqwb; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY preliminary_event
    ADD CONSTRAINT fk8yddkth6xkly2q1nfubpqiqwb FOREIGN KEY (matching_confirmation_client_id, matching_confirmation_session_id) REFERENCES client_matching_confirmation(client_id, session_id);


--
-- Name: fk95821dmxvcbvj3aeahkaxsbph; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_event_reconcile
    ADD CONSTRAINT fk95821dmxvcbvj3aeahkaxsbph FOREIGN KEY (audit_id) REFERENCES audit(id);


--
-- Name: fk988k5vkfub4kwf7e59587v6i2; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY preliminary_event
    ADD CONSTRAINT fk988k5vkfub4kwf7e59587v6i2 FOREIGN KEY (room_id) REFERENCES room(id);


--
-- Name: fk98ancs9pjxkrkoonl3w22eddf; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY therapist_events
    ADD CONSTRAINT fk98ancs9pjxkrkoonl3w22eddf FOREIGN KEY (event_id) REFERENCES event_record(id);


--
-- Name: fk9jyy0hs8ysuv3ym15mh65ln7i; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_calendar_event_clients
    ADD CONSTRAINT fk9jyy0hs8ysuv3ym15mh65ln7i FOREIGN KEY (client_id) REFERENCES client(id);


--
-- Name: fk9pyr69epuo7t545dgnhmt8x2w; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY calendar_event
    ADD CONSTRAINT fk9pyr69epuo7t545dgnhmt8x2w FOREIGN KEY (room_id) REFERENCES room(id);


--
-- Name: fkcgxtrl53tb63sef77ksm5eov9; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY hs_row_item
    ADD CONSTRAINT fkcgxtrl53tb63sef77ksm5eov9 FOREIGN KEY (row_id) REFERENCES hs_row(id);


--
-- Name: fkd47ttd7m6jx1e1hvw8jwm49rm; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY preliminary_event
    ADD CONSTRAINT fkd47ttd7m6jx1e1hvw8jwm49rm FOREIGN KEY (datafk_client_id, datafk_session_id) REFERENCES ss_session_client_data(client_id, session_id);


--
-- Name: fkd66afghqa3679qjpnv61i18bi; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY av_therapist_request
    ADD CONSTRAINT fkd66afghqa3679qjpnv61i18bi FOREIGN KEY (av_request_id) REFERENCES av_request(id);


--
-- Name: fkeglehtfhyweuphrmdj635aoi1; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY reconcile
    ADD CONSTRAINT fkeglehtfhyweuphrmdj635aoi1 FOREIGN KEY (user_id) REFERENCES app_user(id);


--
-- Name: fkeoeran23urqfqvedfelx780y9; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_calendar_event
    ADD CONSTRAINT fkeoeran23urqfqvedfelx780y9 FOREIGN KEY (event_id) REFERENCES event(id);


--
-- Name: fkep1473c0r8ldliu2j8c01yikn; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_event
    ADD CONSTRAINT fkep1473c0r8ldliu2j8c01yikn FOREIGN KEY (room_id) REFERENCES room(id);


--
-- Name: fkevpa650gr6sn5wyew11bi0cvt; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY restriction_equipments
    ADD CONSTRAINT fkevpa650gr6sn5wyew11bi0cvt FOREIGN KEY (restriction_id) REFERENCES restriction(id);


--
-- Name: fkffkveb5c2xw8qlea6yls6ump2; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY ht_column
    ADD CONSTRAINT fkffkveb5c2xw8qlea6yls6ump2 FOREIGN KEY (custom_column_id) REFERENCES ht_custom_column(id);


--
-- Name: fkg0xgyn7g3t24sjqg5uncqprgr; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY session_clients
    ADD CONSTRAINT fkg0xgyn7g3t24sjqg5uncqprgr FOREIGN KEY (client_id) REFERENCES client(id);


--
-- Name: fkgc53lup535bpbr73lm6jws0ap; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY therapist_services
    ADD CONSTRAINT fkgc53lup535bpbr73lm6jws0ap FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fkhmme7qihwi6wh1i5iyadrnyco; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY hs_condition_column_value__hs_condition_column
    ADD CONSTRAINT fkhmme7qihwi6wh1i5iyadrnyco FOREIGN KEY (hs_condition_column_value_id) REFERENCES hs_condition_column_value(id);


--
-- Name: fki4hbprrli23dv1g23tob86pyu; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY therapist_events
    ADD CONSTRAINT fki4hbprrli23dv1g23tob86pyu FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fkj5d540hdj86cry8m11oxw4jhe; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY av_history_record
    ADD CONSTRAINT fkj5d540hdj86cry8m11oxw4jhe FOREIGN KEY (new_state_id) REFERENCES av_history_state(id);


--
-- Name: fkj6dlecsvre3v9hy7uo02ko2ef; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY calendar_event_equipments
    ADD CONSTRAINT fkj6dlecsvre3v9hy7uo02ko2ef FOREIGN KEY (equipment_id) REFERENCES equipment(id);


--
-- Name: fkj8vtvp3thggt9l78mkbxumijl; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY session_clients
    ADD CONSTRAINT fkj8vtvp3thggt9l78mkbxumijl FOREIGN KEY (session_id) REFERENCES session(id);


--
-- Name: fkjdu8m9dx7aeo4r9ljgb4vpnwv; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY calendar_event
    ADD CONSTRAINT fkjdu8m9dx7aeo4r9ljgb4vpnwv FOREIGN KEY (event_id) REFERENCES event(id);


--
-- Name: fkjtbd7b043v0wakrma0p40g210; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY hs_flag_column_value
    ADD CONSTRAINT fkjtbd7b043v0wakrma0p40g210 FOREIGN KEY (hs_flag_column_id) REFERENCES hs_flag_column(id);


--
-- Name: fkk6lr1yf6q16k4dlbx28tj02tr; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY therapist_preferred_rooms
    ADD CONSTRAINT fkk6lr1yf6q16k4dlbx28tj02tr FOREIGN KEY (room_id) REFERENCES room(id);


--
-- Name: fkkhjt3cg0ppwc5ms30jalcqmi3; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY ss_table_column
    ADD CONSTRAINT fkkhjt3cg0ppwc5ms30jalcqmi3 FOREIGN KEY (custom_column_id) REFERENCES ss_custom_column(id);


--
-- Name: fklpyyorufbupv5cxjqgilmmnwf; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY therapist_service_categories
    ADD CONSTRAINT fklpyyorufbupv5cxjqgilmmnwf FOREIGN KEY (service_category_id) REFERENCES service_category(id);


--
-- Name: fkm8wi55rwgswpjav3g1dckr9sq; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY av_history_record
    ADD CONSTRAINT fkm8wi55rwgswpjav3g1dckr9sq FOREIGN KEY (user_id) REFERENCES app_user(id);


--
-- Name: fkmdm6pj1uepxf6oktovj3lu2s1; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY user_modules
    ADD CONSTRAINT fkmdm6pj1uepxf6oktovj3lu2s1 FOREIGN KEY (user_id) REFERENCES app_user(id);


--
-- Name: fknhvxii21vr1bbcksueswabyan; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY hs_column
    ADD CONSTRAINT fknhvxii21vr1bbcksueswabyan FOREIGN KEY (hs_table_id) REFERENCES hs_table(id);


--
-- Name: fkod7f5wypfaydwvvdil8kafsum; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_event
    ADD CONSTRAINT fkod7f5wypfaydwvvdil8kafsum FOREIGN KEY (event_id) REFERENCES event(id);


--
-- Name: fkpevhgcrlivjdiq8e9fauuny3e; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY ht_custom_column_value
    ADD CONSTRAINT fkpevhgcrlivjdiq8e9fauuny3e FOREIGN KEY (hs_custom_column_id) REFERENCES ht_custom_column(id);


--
-- Name: fkpo9fg3hne5rn591kxt7x60to; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_event_change
    ADD CONSTRAINT fkpo9fg3hne5rn591kxt7x60to FOREIGN KEY (previous_value_id) REFERENCES concrete_event_change_value(id);


--
-- Name: fkpv4ff7v3tv9a47ngelh3gssng; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY hs_column
    ADD CONSTRAINT fkpv4ff7v3tv9a47ngelh3gssng FOREIGN KEY (select_column_id) REFERENCES hs_select_column(id);


--
-- Name: fkq8mv3jpxi2e4qnp4gmedyu1ns; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY hs_custom_column_value
    ADD CONSTRAINT fkq8mv3jpxi2e4qnp4gmedyu1ns FOREIGN KEY (hs_custom_column_id) REFERENCES hs_custom_column(id);


--
-- Name: fkqbsfqjbmv73n92k6sridcttad; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY av_therapist_day_record
    ADD CONSTRAINT fkqbsfqjbmv73n92k6sridcttad FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fkqciq1ssbe02w29eii0v2n05cm; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY hs_condition_column_value__hs_condition_column
    ADD CONSTRAINT fkqciq1ssbe02w29eii0v2n05cm FOREIGN KEY (hs_condition_column_id) REFERENCES hs_condition_column(id);


--
-- Name: fkqvlq392ogwf71kv549okgib0m; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY calendar_event
    ADD CONSTRAINT fkqvlq392ogwf71kv549okgib0m FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fkrd4drbxe4xj0kaiwpswma80nr; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY hs_column
    ADD CONSTRAINT fkrd4drbxe4xj0kaiwpswma80nr FOREIGN KEY (custom_column_id) REFERENCES hs_custom_column(id);


--
-- Name: fkreuxjqk75ckm0oss0upbi3s73; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY restriction_equipments
    ADD CONSTRAINT fkreuxjqk75ckm0oss0upbi3s73 FOREIGN KEY (equipment_id) REFERENCES equipment(id);


--
-- Name: fkrhlu8jod5baithc9arfdnu9j7; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY av_time_record
    ADD CONSTRAINT fkrhlu8jod5baithc9arfdnu9j7 FOREIGN KEY (av_therapist_day_record_id) REFERENCES av_therapist_day_record(id);


--
-- Name: fkrsgc1inp55r2yt55q59jh0yn3; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY calendar_event_equipments
    ADD CONSTRAINT fkrsgc1inp55r2yt55q59jh0yn3 FOREIGN KEY (calendar_event_id) REFERENCES calendar_event(id);


--
-- Name: fksc6v8gmwmyq0wfb98t1gclatx; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY therapist_services
    ADD CONSTRAINT fksc6v8gmwmyq0wfb98t1gclatx FOREIGN KEY (service_id) REFERENCES service(id);


--
-- Name: fksreiny273p7vvlgcggbs0nlu9; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_calendar_event
    ADD CONSTRAINT fksreiny273p7vvlgcggbs0nlu9 FOREIGN KEY (therapist_id) REFERENCES therapist(id);


--
-- Name: fkt5kuwkto0rr8ar82s7sqtyjp9; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY concrete_event_reconcile
    ADD CONSTRAINT fkt5kuwkto0rr8ar82s7sqtyjp9 FOREIGN KEY (reconcile_id) REFERENCES reconcile(id);


--
-- Name: fktj4d7u0en7qmymu3q7y52i5ku; Type: FK CONSTRAINT; Schema: public; Owner: csp
--

ALTER TABLE ONLY audit
    ADD CONSTRAINT fktj4d7u0en7qmymu3q7y52i5ku FOREIGN KEY (user_id) REFERENCES app_user(id);

--
-- PostgreSQL database dump complete
--

